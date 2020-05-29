import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Collectors;

public class ReadFolder2 {

    // 所有文件总数
    private static volatile AtomicLong fileTotalCount = new AtomicLong();
    // 空文件夹数量
    private static volatile AtomicLong emptyFolderCount = new AtomicLong();
    // 总文件夹数量
    private static volatile AtomicLong folderTotalCount = new AtomicLong();
    // 文件夹
    private static volatile AtomicReference<List<File>> folders = new AtomicReference<>();
    // 线程数
    private final static int THREAD_COUNT = 5;

    public static void main(String[] args) {

        BigDecimal startTime = new BigDecimal(System.currentTimeMillis());

        String path = args[0];
//        String path = "/Users/sinry";
        File file = new File(path);
        FileList fileList = new FileList();
        fileList.readDir(file);

        BigDecimal endTime = new BigDecimal(System.currentTimeMillis());
        BigDecimal readTime = endTime.subtract(startTime).divide(BigDecimal.valueOf(1000));
        System.out.println();

        String s = "程序执行耗时：" + readTime + "s\n";
        System.out.println(s);
    }

    private static class FileList {
        private void readDir(File parentFile) {

            // 1、首先遍历三层的目录结构
            File[] listFiles = parentFile.listFiles();
            if (!(listFiles != null && listFiles.length > 0)) {
                return;
            }
            forReadLevel3(Arrays.asList(listFiles), 3);
//            ReadFolder2.folders.get().forEach(i -> System.out.println(i.getAbsoluteFile()));
            System.out.println(ReadFolder2.folders.get().size());

            // 2、使用线程池去分别遍历
            ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
            List<File> files = folders.get();
            if (!files.isEmpty()) {
                List<CompletableFuture> cfs = files.stream().map(f -> CompletableFuture.runAsync(() -> new FileThread(f).run(), pool)).collect(Collectors.toList());
                CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()])).thenAccept(res -> {
                    String s =
                            "所有文件总数：" + ReadFolder2.fileTotalCount + "\n" +
                                    "空文件夹个数：" + ReadFolder2.emptyFolderCount + "\n" +
                                    "总文件夹数量：" + ReadFolder2.folderTotalCount;
                    System.out.println(s);
                }).join();

//                files.forEach(f -> pool.submit(new FileThread(f)));
            }
            pool.shutdown();

//            String s =
//                    "所有文件总数：" + fileTotalCount + "\n" +
//                            "空文件夹个数：" + emptyFolderCount;
//            System.out.println(s);

        }

        private void forReadLevel3(List<File> parentFiles, int level) {
            if (level <= 0) {
                folders.set(parentFiles);
                return;
            }

            List<File> fileList = new ArrayList<>();
            for (File file: parentFiles) {
                if (!file.isDirectory()) {
                    ReadFolder2.fileTotalCount.getAndIncrement();
                } else {
                    List<File> fileChildList = getFileChildList(file);
                    ReadFolder2.folderTotalCount.getAndIncrement();
                    if (fileChildList.isEmpty()) {
                        ReadFolder2.emptyFolderCount.getAndIncrement();
                    } else {
                        fileList.addAll(fileChildList);
                    }
                }
            }

            if (fileList.size() > 0) {
                this.forReadLevel3(fileList, level-1);
            }
        }

        private List<File> getFileChildList(File file) {
            File[] listFiles = file.listFiles();
            if (!(listFiles != null && listFiles.length > 0)) {
                return Collections.emptyList();
            }
            return Arrays.asList(listFiles);
        }
    }

    private static class FileThread
//            implements Runnable
    {

        private File file;

        FileThread(File file) {
            super();
            this.file = file;
        }

//        @Override
        public void run() {
            this.find();
        }

        private void find () {
            File[] listFiles = this.file.listFiles();
            if (!(listFiles != null && listFiles.length > 0)) {
                return;
            }
            Stack<File> stack = new Stack<>();
            stack.addAll(Arrays.asList(listFiles));
            while (!stack.isEmpty()) {
                File firstFile = stack.pop();
                if (!firstFile.isDirectory()) {
                    ReadFolder2.fileTotalCount.getAndIncrement();
                } else {
                    File[] files = firstFile.listFiles();
                    ReadFolder2.folderTotalCount.getAndIncrement();
                    if (files != null && files.length > 0) {
                        stack.addAll(Arrays.asList(files));
                    } else {
                        ReadFolder2.emptyFolderCount.getAndIncrement();
                    }
                }
            }
        }

    }
}
