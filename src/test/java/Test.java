import com.zzz.FileDataPageReader;
import com.zzz.FileDataWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        String path = "test";

//        test(path + File.separator + 1000009 + "_" + 1000009, 1000009, 1000009);

//        test(path, 1000, 1000);

        Integer fileMaxSize = 10000;
        Integer totalSize = 10000;
        Integer pageSize = 160;
        for (int i = 0; i < 100; i++) {
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 2, 2d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 1, 2d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 1, 2d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 2, 2d);
            totalSize++;
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 2, 2d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 1, 2d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 1, 2d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 2, 2d);
            fileMaxSize++;
        }
        fileMaxSize = 20000;
        totalSize = 20000;
        pageSize = 160;
        for (int i = 0; i < 100; i++) {
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 2, 1.5d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 1, 1.7d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 1, 1.8d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 2, 1.9d);
            totalSize++;
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 2, 2.1);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 1, 2.3);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 1, 3.7);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 2, 3.4);
            fileMaxSize++;
        }

        pageSize = 160;
        fileMaxSize = 1000000;
        totalSize = 1000000;
        for (int i = 0; i < 10; i++) {
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d);
            totalSize++;
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d);
            fileMaxSize++;
        }
        executor.shutdown();
    }


    private static void test(String path, Integer totalSize, Integer fileMaxSize, Integer pageSize, Double totalSizeFactor) {
        executor.submit(() -> {
            int realTotalSize = (int) (totalSize * totalSizeFactor);
            if (!new File(path).exists()) {
                //写入文件
                try (FileDataWriter fileDataWriter = FileDataWriter.builder().withFileMaxSize(fileMaxSize).build(path)) {
                    for (Long i = 1L; i <= realTotalSize; i++) {
                        fileDataWriter.write(i + "");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FileDataPageReader fileDataPageReader = FileDataPageReader.builder().build(path);
            Integer lastNum = 0;

            //指定起始行读取
            Integer size = 100;
            Integer startIndex = totalSize - size;
            Integer firstNum = null;
            for (int i = 0; i < size; i++) {
                List<String> strings = fileDataPageReader.read(startIndex, size);
                System.out.println(startIndex + " " + strings.size());
                System.out.println(strings);
                lastNum = null;
                Integer curFistNum = new Integer(strings.get(0));
                if (firstNum != null) {
                    if (curFistNum - firstNum != 1) {
                        throw new IllegalArgumentException("读取错误,不同startIndex的第一条数据应该只差1");
                    }
                }
                firstNum = curFistNum;
                for (String string : strings) {
                    Integer curNum = new Integer(string);
                    if (lastNum == null) {
                        lastNum = curNum;
                        continue;
                    }
                    if (curNum - lastNum != 1) {
                        throw new IllegalArgumentException("读取错误,应该是连续的数字");
                    }
                    lastNum = curNum;
                }
                startIndex++;
            }

            //从文件分页读取
            Integer page = 1;
            lastNum = 0;
            boolean first = true;
            while (true) {
                List<String> strings = fileDataPageReader.readPage(page, pageSize);
                if (strings.isEmpty()) {
                    if (lastNum != realTotalSize) {
                        throw new IllegalArgumentException("未正确读取到最后一行");
                    }
                    break;
                }
                System.out.println(page + " " + strings.size());
                page++;
                System.out.println(strings);
                for (String string : strings) {
                    Integer curNum = new Integer(string);
                    if (first) {
                        if (curNum != 1) {
                            throw new IllegalArgumentException("读取错误,首行应该是1");
                        }
                        first = false;
                    }
                    if (curNum - lastNum != 1) {
                        throw new IllegalArgumentException("读取错误,应该是连续的数字");
                    }
                    lastNum = curNum;
                }
            }
        });
    }

}
