import com.sun.xml.internal.messaging.saaj.packaging.mime.util.LineInputStream;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ReadFolder2 {

    public static void main(String[] args) {

        String path;
        File file;
        Scanner scanner = new Scanner(System.in);
        System.out.println("【确保目录存在执行权限】");
        System.out.println("输入需要遍历的目录：");
        while (true) {
            path = scanner.next();
            file = new File(path);
            if (file.exists()) {
                break;
            } else {
                System.out.println("目录不存在，请重新输入：");
            }
        }

        BigDecimal startTime = new BigDecimal(System.currentTimeMillis());
        FileList fileList = new FileList();

        fileList.readDir(file);

        BigDecimal endTime = new BigDecimal(System.currentTimeMillis());
        BigDecimal readTime = endTime.subtract(startTime).divide(BigDecimal.valueOf(1000));
        System.out.println();

        String s = "程序执行耗时：" + readTime + "s\n";
        System.out.println(s);
    }
}

class FileList {
    private final static int FIRST_COUNT = 3;
    // 所有文件总数
    static volatile AtomicLong fileTotalCount = new AtomicLong();
    // 空文件夹数量
    static volatile AtomicLong emptyFolderCount = new AtomicLong();
    // 文件夹
    private static volatile AtomicReference<List<File>> folders = new AtomicReference<>(Collections.emptyList());
    // 线程数
    private final static int THREAD_COUNT = 10;

    void readDir(File parentFile) {

        // 1、首先遍历三层的目录结构
        File[] listFiles = parentFile.listFiles();
        if (!(listFiles != null && listFiles.length > 0)) {
            return;
        }
        forReadLevel3(Arrays.asList(listFiles), FIRST_COUNT);
//        folders.get().forEach(i -> System.out.println(i.getAbsoluteFile()));
//        System.out.println(folders.get().size());

        // 2、使用线程池去分别遍历
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
        List<File> files = folders.get();
        if (!files.isEmpty()) {
            List<CompletableFuture> cfs = files.stream().map(f -> CompletableFuture.runAsync(() -> new FileThread(f).find2(), pool)).collect(Collectors.toList());
            CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()])).thenAccept(res -> {
                String s = "所有文件总数：" + fileTotalCount + "\n"
                        + "空文件夹个数：" + emptyFolderCount;
                System.out.println(s);
            }).join();

//                files.forEach(f -> pool.submit(new FileThread(f)));
        }
        pool.shutdown();

    }

    private void forReadLevel3(List<File> parentFiles, int level) {
        if (level <= 0) {
            folders.set(parentFiles);
            return;
        }

        List<File> fileList = new ArrayList<>();
        for (File file: parentFiles) {
            // 软连接校验
            if (isSymbolicLink(file)) continue;

            if (!file.isDirectory()) {
                FileList.fileTotalCount.getAndIncrement();
            } else {
                List<File> fileChildList = getFileChildList(file);
                if (fileChildList.isEmpty()) {
                    FileList.emptyFolderCount.getAndIncrement();
                } else {
                    fileList.addAll(fileChildList);
                }
            }
        }

        if (fileList.size() > 0) {
            this.forReadLevel3(fileList, level-1);
        }
    }

    private static List<File> getFileChildList(File file) {
        File[] listFiles = file.listFiles();
        if (!(listFiles != null && listFiles.length > 0)) {
            return Collections.emptyList();
        }
        return Arrays.asList(listFiles);
    }

    static boolean isSymbolicLink(File f) {

//        =================== 分割线 =======================
//        不可用
//        Runtime runTime = Runtime.getRuntime();
//        try {
//            Process exec = runTime.exec("ls -l " + f.getAbsolutePath());
//            InputStreamReader inputStreamReader = new InputStreamReader(exec.getInputStream());
//            LineNumberReader line = new LineNumberReader(inputStreamReader);
//            String res = line.readLine();
//            if (res != null) {
//                char c = res.charAt(0);
//                if (c == 'l') {
////                    System.out.println(f.getAbsolutePath());
//                    return true;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        =================== 分割线 =======================
//        不可用
//        try {
//            // toPath()存在同步锁
//            Path path = f.toPath().toRealPath();
//            return !path.equals(f.toPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        =================== 分割线 =======================
//        /Users 19s
//        try {
//            return !f.getCanonicalPath().equals(f.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        =================== 分割线 =======================
//        /Users 10s
        String path = f.getAbsolutePath();
        Path p = Paths.get(path);
        if (Files.isSymbolicLink(p)) {
//            System.out.println(p);
            return true;
        }
        return false;
    }
}

class FileThread {

    private File file;

    FileThread(File file) {
        super();
        this.file = file;
    }

    void find2() {
        Path path = FileSystems.getDefault().getPath(this.file.getPath());
        try {
            Files.walk(path).forEach(i -> {
                File file = i.toFile();
                // 软连接校验
                if (FileList.isSymbolicLink(file)) return;

                if (file.isFile()) {
                    FileList.fileTotalCount.getAndIncrement();
                } else {
                    String[] dirs = file.list();
                    if (!(dirs != null && dirs.length > 0)) {
                        FileList.emptyFolderCount.getAndIncrement();
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("读取失败：" + e.getMessage());
        }

    }

}
