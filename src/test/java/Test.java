import com.zzz.FileDataPageReader;
import com.zzz.FileDataWriter;
import com.zzz.enums.WriteMode;
import com.zzz.utils.file.reader.factory.FileReaderFactory;
import com.zzz.utils.file.reader.factory.NioFileReaderFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    static ExecutorService executor = Executors.newFixedThreadPool(1);

    private static FileReaderFactory fileFactory;

    public static void main(String[] args) {
        String path = "test";
        fileFactory = new NioFileReaderFactory();
        deleteFolder(path);
        Long start = System.currentTimeMillis();
        test(path, WriteMode.NOT_ALLOW_REPEATED);
        Long time1 = (System.currentTimeMillis() - start) / 1000;

//        deleteFolder(path);
//        start = System.currentTimeMillis();
//        test(path, WriteMode.APPEND);
//        Long time2 = (System.currentTimeMillis() - start) / 1000;
//
//        deleteFolder(path);
//        fileFactory = new BufferedRandomAccessFileFactory();
//        start = System.currentTimeMillis();
//        test(path, WriteMode.NOT_ALLOW_REPEATED);
//        Long time3 = (System.currentTimeMillis() - start) / 1000;
//
//        deleteFolder(path);
//        start = System.currentTimeMillis();
//        test(path, WriteMode.APPEND);
//        Long time4 = (System.currentTimeMillis() - start) / 1000;
        System.out.println(time1);
//        System.out.println(time2);
//        System.out.println(time3);
//        System.out.println(time4);
        executor.shutdown();
    }

    private static void test(String path, WriteMode writeMode) {
        //        test(path + File.separator + 20000 + "_" + 20000, 20000, 20000, 159, 1.7d, writeMode);
        Integer fileMaxSize = 10000;
        Integer totalSize = 10000;
        Integer pageSize = 160;
        for (int i = 0; i < 100; i++) {
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 2, 2d, writeMode);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 1, 2d, writeMode);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d, writeMode);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 1, 2d, writeMode);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 2, 2d, writeMode);
            totalSize++;
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 2, 2d, writeMode);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 1, 2d, writeMode);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d, writeMode);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 1, 2d, writeMode);
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 2, 2d, writeMode);
            fileMaxSize++;
        }
        fileMaxSize = 30000;
        totalSize = 20000;
        pageSize = 160;
        for (int i = 0; i < 100; i++) {
            test(path + File.separator + totalSize + "_1.5_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 2, 1.5d, writeMode);
            test(path + File.separator + totalSize + "_1.7_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 1, 1.7d, writeMode);
            test(path + File.separator + totalSize + "_2_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d, writeMode);
            test(path + File.separator + totalSize + "_1.8_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 1, 1.8d, writeMode);
            test(path + File.separator + totalSize + "_1.9_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 2, 1.9d, writeMode);
            totalSize++;
            test(path + File.separator + totalSize + "_2.1_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 2, 2.1, writeMode);
            test(path + File.separator + totalSize + "_2.3_" + fileMaxSize, fileMaxSize, totalSize, pageSize - 1, 2.3, writeMode);
            test(path + File.separator + totalSize + "_2_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d, writeMode);
            test(path + File.separator + totalSize + "_3.7_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 1, 3.7, writeMode);
            test(path + File.separator + totalSize + "_3.4_" + fileMaxSize, fileMaxSize, totalSize, pageSize + 2, 3.4, writeMode);
            fileMaxSize++;
        }

        pageSize = 160;
        fileMaxSize = 1000000;
        totalSize = 1000000;
        for (int i = 0; i < 10; i++) {
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d, writeMode);
            totalSize++;
            test(path + File.separator + totalSize + "_" + fileMaxSize, fileMaxSize, totalSize, pageSize, 2d, writeMode);
            fileMaxSize++;
        }
    }

    private static void test(String path, Integer totalSize, Integer fileMaxSize, Integer pageSize,
                             Double totalSizeFactor, WriteMode writeMode) {
//        executor.execute(() -> {
        int realTotalSize = (int) (totalSize * totalSizeFactor);
        if (!new File(path).exists()) {
            if (WriteMode.APPEND.equals(writeMode)) {
                Integer halfTotalSize = realTotalSize / 2;
                //写入文件
                try (FileDataWriter fileDataWriter = FileDataWriter.builder().withFileMaxSize(fileMaxSize).withWriteMode(writeMode).build(path)) {
                    for (Integer i = 1; i < halfTotalSize; i++) {
                        fileDataWriter.write(i + "");
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException("写入异常",e);
                }
                try (FileDataWriter fileDataWriter = FileDataWriter.builder().withFileMaxSize(fileMaxSize).withWriteMode(writeMode).build(path)) {
                    for (Integer i = halfTotalSize; i <= realTotalSize; i++) {
                        fileDataWriter.write(i + "");
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException("写入异常",e);
                }
            } else {
                //写入文件
                try (FileDataWriter fileDataWriter = FileDataWriter.builder().withFileMaxSize(fileMaxSize).withWriteMode(writeMode).build(path)) {
                    for (Integer i = 1; i <= realTotalSize; i++) {
                        fileDataWriter.write(i + "");
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException("写入异常",e);
                }
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
//        });
    }

    public static void deleteFolder(String folderPath) {
        Path path = Paths.get(folderPath);
        if (Files.exists(path)) {
            try {
                Files.walk(path) // 遍历文件夹及其子文件夹
                        .sorted((p1, p2) -> -p1.compareTo(p2)) // 反向排序，先删除子文件
                        .forEach(p -> {
                            try {
                                Files.delete(p); // 删除文件或空文件夹
                            } catch (IOException e) {
                                System.err.println("无法删除: " + p + ", 原因: " + e.getMessage());
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
