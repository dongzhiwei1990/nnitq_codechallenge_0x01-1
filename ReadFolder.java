import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class ReadFolder {

    public static void main(String[] args) {
        if (args.length <= 0) {
            throw new RuntimeException("缺少参数");
        }
        BigDecimal startTime = new BigDecimal(System.currentTimeMillis());

//        System.out.println("微信昵称：邕城里的大男孩");
//        System.out.println("CPU型号：i3-8100 CPU @ 3.60GHz");
//        System.out.println("MEM：16G");
        System.out.println("操作系统：" + System.getProperty("os.name"));
        System.out.println("JDK版本：" + System.getProperty("java.version"));

        String path = args[0];
        readDir(path);

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
        long emptyFolderCount = 0;

        Stack<File> stack = new Stack<>();
        stack.addAll(Arrays.asList(listFiles));
        while(!stack.isEmpty()) {
                File firstFile = stack.pop();
                if (firstFile.isFile()) {
                    fileTotalCount++;
                } else {
                    File[] files = firstFile.listFiles();
                    if (files != null && files.length > 0) {
                        stack.addAll(Arrays.asList(files));
                    } else {
                        emptyFolderCount++;
                    }
                }

        }

        String s = "所有文件总数：" + fileTotalCount + "\n" +
                    "空文件夹个数：" + emptyFolderCount;
        System.out.println(s);
    }
}
