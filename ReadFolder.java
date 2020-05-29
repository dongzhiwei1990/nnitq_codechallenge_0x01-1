import java.io.File;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ReadFolder {

    public static void main(String[] args) {

//        if (args.length <= 0) {
//            throw new RuntimeException("缺少参数");
//        }
        BigDecimal startTime = new BigDecimal(System.currentTimeMillis());

//        System.out.println("微信昵称：邕城里的大男孩");
//        System.out.println("CPU型号：i3-8100 CPU @ 3.60GHz");
//        System.out.println("MEM：16G");
        System.out.println("操作系统：" + System.getProperty("os.name"));
        System.out.println("JDK版本：" + System.getProperty("java.version"));

//        String path = args[0];
        String path = "/Users/sinry";
        readDir2(path);

        BigDecimal endTime = new BigDecimal(System.currentTimeMillis());
        BigDecimal readTime = endTime.subtract(startTime).divide(BigDecimal.valueOf(1000));
        System.out.println("程序执行耗时：" + readTime + "s");
    }

    private static void readDir(String path) {
        File f = new File(path);
        System.out.println("读取目录：" + f.getPath());

        File[] listFiles = f.listFiles();
        if (listFiles == null) return;
        if (!f.isDirectory() || listFiles.length <= 0) return;

        // 所有文件总数
        long fileTotalCount = 0;
        // 空文件夹数量
        AtomicLong emptyFolderCount = new AtomicLong();
        Stack<File> stack = new Stack<>();
        stack.addAll(Arrays.asList(listFiles));
        while (!stack.isEmpty()) {
            File firstFile = stack.pop();
            if (firstFile.isFile()) {
                fileTotalCount++;
            } else {
                File[] files = listFiles(firstFile);
//                File[] files = firstFile.listFiles();
                if (files != null && files.length > 0) {
                    stack.addAll(Arrays.asList(files));
                } else {
                    emptyFolderCount.getAndIncrement();
                }
            }
        }
        String s = "所有文件总数：" + fileTotalCount + "\n" +
                "空文件夹个数：" + emptyFolderCount;
        System.out.println(s);
    }

    private static void readDir2(String path) {
//        File f = new File(path);
        System.out.println("读取目录：" + path);
//
//        File[] listFiles = f.listFiles();
//        if (listFiles == null) return;
//        if (!f.isDirectory() || listFiles.length <= 0) return;
//
        // 所有文件总数
        AtomicLong fileTotalCount = new AtomicLong();
        // 空文件夹数量
        AtomicLong emptyFolderCount = new AtomicLong();

        Path start = FileSystems.getDefault().getPath(path);
        try {
//            Files.walk(start).parallel().forEach(System.out::println);
            Files.walk(start).forEach(i -> {
                File file = i.toFile();
                if (file.isFile()) {
                    fileTotalCount.getAndIncrement();
                } else {
                    String[] dirs = file.list();
                    if (!(dirs != null && dirs.length > 0)) {
                        emptyFolderCount.getAndIncrement();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        String s = "所有文件总数：" + fileTotalCount + "\n" +
                "空文件夹个数：" + emptyFolderCount;
        System.out.println(s);
    }

    private static File[] listFiles(File file) {
        String[] list = file.list();
        if (list == null) return null;

        return Arrays.stream(list)
                .map(i -> new File(file, i))
                .toArray(File[]::new);
    }
}
