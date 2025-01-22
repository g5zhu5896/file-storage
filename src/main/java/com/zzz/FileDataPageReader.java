package com.zzz;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FileDataPageReader {
    private Cfg cfg;
    private String path;

    /**
     * 请用这个类创建 {@link FileDataPageReaderBuilder}
     *
     * @param path
     */
    private FileDataPageReader(String path) {
        this.path = path;
        String cfgStr = FileUtil.readUtf8String(new File(buildPath(this.path, Constants.CFG_FILE_NAME)));
        this.cfg = JSON.parseObject(cfgStr, Cfg.class);

    }

    /**
     * 分页查询
     *
     * @param page 第几页
     * @param size 一页多少条
     * @return
     */
    public List<String> readPage(Integer page, Integer size) {
        Integer startIndex = (page - 1) * size + 1;
        return read(startIndex, size);
    }

    /**
     * 分页查询
     *
     * @param page     第几页
     * @param size     一页多少条
     * @param function 可以对文件内一行的数据进行处理并返回，返回的list中的对象为此方法返回的对象
     * @param <T>
     * @return
     */
    public <T> List<T> readPage(Integer page, Integer size, Function<String, T> function) {
        Integer startIndex = (page - 1) * size + 1;
        return read(startIndex, size, function);
    }

    /**
     * 读取所有数据
     *
     * @return
     */
    public List<String> readAll() {
        List<String> result = read(1, cfg.getTotalSize());
        return result;
    }

    /**
     * @param function 可以对文件内一行的数据进行处理并返回，返回的list中的对象为此方法返回的对象
     * @param <T>
     * @return
     */
    public <T> List<T> readAll(Function<String, T> function) {
        List<T> result = read(1, cfg.getTotalSize(), function);
        return result;
    }


    /**
     * @param startIndex 起始行数
     * @param size       从起始行数往后查多少条
     * @return
     */
    public List<String> read(Integer startIndex, Integer size) {
        List<String> result = read(startIndex, size, line -> {
            return line;
        });
        return result;
    }

    public Integer getTotalSize(){
        return cfg.getTotalSize();
    }

    /**
     * @param startIndex 起始行数
     * @param size       从起始行数往后查多少条
     * @param function   可以对文件内一行的数据进行处理并返回，返回的list中的对象为此方法返回的对象
     * @param <T>
     * @return
     */
    public <T> List<T> read(Integer startIndex, Integer size, Function<String, T> function) {
        if (startIndex > cfg.getTotalSize()) {
            return Lists.newArrayList();
        }

        List<T> result = Lists.newArrayListWithCapacity(size.intValue());
        Integer currentRow = (startIndex - 1) % cfg.getFileMaxSize() + 1;
        read(result, currentRow, getFileStartIndex(startIndex), size, function);
        return result;
    }

    /**
     * @param result
     * @param currentRow     从当前文件中哪一行开始读
     * @param fileStartIndex 当前文件的起始索引
     * @param readSize       需要读取的行数
     * @throws IOException
     */
    private <T> void read(List<T> result, Integer currentRow, Integer fileStartIndex, Integer readSize, Function<String, T> function) {
        Integer curReadSize = 0;
        try (BufferedRandomAccessFileReader contentReader = createContentReader(fileStartIndex);
             BufferedRandomAccessFileReader indexReader = createIndexReader(fileStartIndex)) {
            String content = "";
            Long seek = computeSeek(indexReader, currentRow, fileStartIndex);
            contentReader.getRaf().seek(seek);
            while ((content = contentReader.getReader().readLine()) != null) {
                result.add(function.apply(content));
                currentRow++;
                curReadSize++;
                //读到需要的记录数就返回
                if (curReadSize.equals(readSize)) {
                    return;
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("文件读取异常", e);
        }
        fileStartIndex += cfg.getFileMaxSize();
        //如果没有更多的记录也返回,当前行如果没达到文件最大值一般也意味着没有更多的记录了
        if (fileStartIndex <= cfg.getTotalSize() && currentRow - 1 == cfg.getFileMaxSize()) {
            read(result, 1, fileStartIndex, readSize - curReadSize, function);
        }
    }

    /**
     * 获取数据当前行在文件中的偏移量
     *
     * @param indexReader    索引文件reader
     * @param currentRow     当前文件中数据所在行
     * @param fileStartIndex 当前文件的起始索引
     * @return
     */
    private Long computeSeek(BufferedRandomAccessFileReader indexReader, Integer currentRow, Integer fileStartIndex) throws IOException {
        Long curIndexFix = 0l;
        Integer lastIndexColumn = 1;
        Long curIndexSeek = 0l;
        //假设cfg.getColumnIndexWidthMap=1->3,4->5, 10->6,currentRow=6
        for (Map.Entry<Integer, Long> entry : cfg.getColumnIndexWidthMap().entrySet()) {
            Integer column = entry.getKey();
            if (column < fileStartIndex) {
                //用于过滤不属于当前文件的行宽度,当真实行数比当前文件的起始索引小，表示该行属于前面文件的
                continue;
            } else {
                //找到当前文件对应的行后，需要把column从当前文件第一行开始对应的行数
                column -= fileStartIndex - 1;
            }
            if (column.equals(currentRow)) {
                //step3 假设currentRow=10而不是6: curIndexSeek=9,curIndexFix=5 lastIndexColumn=4 column=10,currentRow=10,10==10 ,curIndexSeek=9+30=39
                curIndexSeek += (currentRow - lastIndexColumn) * curIndexFix;
                curIndexFix = entry.getValue();
                lastIndexColumn = currentRow;
            } else if (column < currentRow) {
                if (curIndexFix.equals(0l)) {
                    //step1:column=1,currentRow=6 1<6 curIndexFix=null,得curIndexFix=3 lastIndexColumn=1
                    curIndexFix = entry.getValue();
                    lastIndexColumn = column;
                } else {
                    //step2: curIndexFix=3 lastIndexColumn=1 column=4,currentRow=6,4<6 curIndexFix!=null,curIndexSeek=9,curIndexFix=5 lastIndexColumn=4
                    curIndexSeek += (column - lastIndexColumn) * curIndexFix;
                    curIndexFix = entry.getValue();
                    lastIndexColumn = column;
                }
            } else if (column > currentRow) {
                //step3: curIndexSeek=9,curIndexFix=5 lastIndexColumn=4 column=10,currentRow=6,10>6 ,curIndexSeek=9+10=19
                curIndexSeek += (currentRow - lastIndexColumn) * curIndexFix;
                lastIndexColumn = currentRow;
                break;
            }
        }
        //假设cfg.getColumnIndexWidthMap=1->3,currentRow=6
        //此时step1  的column = 1 ,currentRow=6 ,curIndexFix=3,lastIndexColumn=1,curIndexSeek=0
        // 而因为getColumnIndexWidthMap不存在比column=1 更大的行索引宽度
        // 所以不会触发step3,也就不会计算 lastIndexColumn=1->currentRow=6 的偏移量，所以需要补充计算
        if (lastIndexColumn < currentRow) {
            curIndexSeek += (currentRow - lastIndexColumn) * curIndexFix;
        }
        indexReader.getRaf().seek(curIndexSeek);
        char[] chars = new char[curIndexFix.intValue()];
        indexReader.getReader().read(chars);
        return new Long(new String(chars));
    }

    //数据文件reader
    private BufferedRandomAccessFileReader createContentReader(Integer fileStartIndex) throws IOException {
        String filePath = buildPath(path, buildFileName(fileStartIndex, Constants.CONTENT_FILE_SUFFIX));
        BufferedRandomAccessFileReader reader = new BufferedRandomAccessFileReader(filePath, cfg.getCharset());
        return reader;
    }

    //索引文件reader
    private BufferedRandomAccessFileReader createIndexReader(Integer fileStartIndex) throws IOException {
        String filePath = buildPath(path, buildFileName(fileStartIndex, Constants.INDEX_FILE_SUFFIX));
        BufferedRandomAccessFileReader reader = new BufferedRandomAccessFileReader(filePath, cfg.getCharset());
        return reader;
    }

    /**
     * 根据要查询行数计算出文件记录的开始索引
     *
     * @param startIndex 要查询的开始行数
     */
    private Integer getFileStartIndex(Integer startIndex) {
        return startIndex - (startIndex - 1) % cfg.getFileMaxSize();
    }

    String buildPath(String filePath, String fileName) {
        return filePath + File.separator + fileName;
    }

    String buildFileName(Integer fileStartIndex, String suffix) {
        //以文件记录的 开始行-结束行 作为文件名
        String fileName = (fileStartIndex) + "-" + (fileStartIndex + cfg.getFileMaxSize() - 1);
        return fileName.concat(suffix);
    }

    public static FileDataPageReaderBuilder builder() {
        return new FileDataPageReaderBuilder();
    }


    public static final class FileDataPageReaderBuilder {

        private FileDataPageReaderBuilder() {
        }

        public FileDataPageReader build(String path) {
            FileDataPageReader fileDataPageReader = new FileDataPageReader(path);
            return fileDataPageReader;
        }
    }
}
