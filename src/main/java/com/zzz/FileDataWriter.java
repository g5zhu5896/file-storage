package com.zzz;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import com.zzz.constatns.Constants;
import com.zzz.entity.Cfg;
import com.zzz.enums.WriteMode;
import com.zzz.utils.file.writer.FileWriter;
import com.zzz.utils.file.writer.factory.BufferedRandomAccessFileWriterFactory;
import com.zzz.utils.file.writer.factory.FileWriterFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.TreeMap;

public class FileDataWriter implements Closeable {
    /**
     * 每个文件的最大写入数量
     */
    private Integer fileMaxSize;
    /**
     * 当前文件的当前写入行
     */
    private Integer currentRow = 1;
    /**
     * 总行数
     */
    private Integer totalSize = 0;
    /**
     * 编码
     */
    private String charset;

    private String path;

    /**
     * 索引文件索引间固定宽度大小
     */
    private Long indexFixedWidth = 0l;

    /**
     * 索引文件索引间固定宽度因子
     * 默认基于写入的第一条数据的宽度+3
     */
    private Long indexFixedWidthFactor = 3l;

    /**
     * 行和索引宽度的映射
     * key 为行
     * value 为行对应索引的固定宽度
     */
    TreeMap<Integer, Long> columnIndexWidthMap = Maps.newTreeMap();

    /**
     * 用于写数据
     */
    private FileWriter contentRafWriter;
    /**
     * 用于写入索引， 索引对应行在文件中所在文件指针偏移量，可以基于索引快速定位到行所在位置
     */
    private FileWriter indexRafWriter;


    private FileWriterFactory fileFactory;

    /**
     * 请用这个类创建 {@link FileDataWriterBuilder}
     *
     * @param path
     */
    private FileDataWriter(String path, WriteMode writeMode, FileWriterFactory fileFactory) throws IOException {
        this.path = path;
        this.fileFactory = fileFactory;
        File file = new File(path);
        if (file.exists()) {
            if (WriteMode.NOT_ALLOW_REPEATED.equals(writeMode)) {
                throw new IllegalArgumentException(path + "  文件已存在，不可重复写入");
            } else if (WriteMode.OVERRIDE.equals(writeMode)) {
                FileUtil.del(file);
            } else if (WriteMode.APPEND.equals(writeMode)) {
                String cfgStr = FileUtil.readUtf8String(new File(buildPath(this.path, Constants.CFG_FILE_NAME)));
                Cfg cfg = JSON.parseObject(cfgStr, Cfg.class);
                this.totalSize = cfg.getTotalSize();
                this.indexFixedWidth = cfg.getNewIndexFixedWidth();
                this.currentRow = cfg.getNewCurrentRow();
                this.columnIndexWidthMap = cfg.getColumnIndexWidthMap();

                //build配置的 charset、fileMaxSize、indexFixedWidthFactor将失效
                this.charset = cfg.getCharset();
                this.fileMaxSize = cfg.getFileMaxSize();
                this.indexFixedWidthFactor = cfg.getIndexFixedWidthFactor();

                createWriter();
                contentRafWriter.setPosition(cfg.getNewDataFilePointer());
                indexRafWriter.setPosition(cfg.getNewIndexFilePointer());
            }
        }
    }

    private FileDataWriter(String path, WriteMode writeMode) throws IOException {
        this(path, writeMode, new BufferedRandomAccessFileWriterFactory());
    }

    public void write(String data) throws IOException {
        if (contentRafWriter == null) {
            //第一条数据的长度*1000作为固定宽度
            createWriter();
        }
        //当行数大于文件最大行数长度，则需要创建一个新的文件
        if (currentRow > fileMaxSize) {
            try {
                closeWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
            createWriter();
        }
        if (contentRafWriter != null) {
            //写入前当前行的偏移量
            long filePointer = contentRafWriter.getPosition();
            //写入数据
            contentRafWriter.writeLine(data);
            //不实时flush，否则每行数据量小的时候写入性能太慢，但同时也会导致出现问题丢失的数据会变多。
            // 不过如果真的会出现问题丢失数据，丢多丢少无所谓了,毕竟没做事务和异常处理机制。
//            contentRafWriter.flush();

            //计算写入后行偏移量的宽度
            int curIndexWith = (contentRafWriter.getPosition() + "").length();
            if (curIndexWith > indexFixedWidth) {
                //每次都比当前的宽度多3位,可以考虑把这个改为外部配置,也可以是fileMaxFile的字符串长度
                indexFixedWidth = curIndexWith + indexFixedWidthFactor;
                columnIndexWidthMap.put(totalSize + 1, indexFixedWidth);
            }

            String numberFormat = "%0" + indexFixedWidth + "d";
            //以固定宽度将索引写入文件,后续就可以基于要读取的行数算出索引在第几个字节的地方,读取到索引后再基于索引读取具体的行
            indexRafWriter.write(String.format(numberFormat, filePointer));
            //换行后索引文件看起来更清晰，调试时可以打开看数据
//            indexRafWriter.writeLine(String.format(numberFormat, filePointer));

//            indexRafWriter.flush();

            currentRow++;
            totalSize++;
        }

    }

    private void createWriter() {
        //数据文件writer
        contentRafWriter = createWriter(createContentFile());
        //索引文件writer
        indexRafWriter = createWriter(createIndexFile());
    }


    private FileWriter createWriter(File newFile) {
        try {
            FileWriter write = fileFactory.fileWriter(newFile, Charset.forName(charset));
            return write;
        } catch (IOException e) {
            throw new IllegalArgumentException(newFile.getAbsolutePath() + "文件读取异常", e);
        }
    }

    //创建数据文件
    private File createContentFile() {
        //以文件记录的开始行-结束行作为文件名
        File file = buildFile(Constants.CONTENT_FILE_SUFFIX);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    //创建索引文件
    private File createIndexFile() {
        //以文件记录的开始行-结束行作为文件名
        File file = buildFile(Constants.INDEX_FILE_SUFFIX);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    File buildFile(String suffix) {
        Integer startFileIndex = totalSize + 1 - (totalSize) % fileMaxSize;
        String fileName = (startFileIndex) + "-" + (startFileIndex - 1 + fileMaxSize);
        File file = new File(buildPath(this.path, fileName.concat(suffix)));
        return file;
    }

    private String buildPath(String filePath, String fileName) {

        return filePath + File.separator + fileName;
    }

    public void closeWriter() throws IOException {
        if (contentRafWriter != null) {
            contentRafWriter.close();
            indexRafWriter.close();
            currentRow = 1;
            indexFixedWidth = 0l;
            contentRafWriter = null;
            indexRafWriter = null;
        }
    }

    @Override
    public void close() throws IOException {
        //写入配置文件
        Cfg cfg = new Cfg();
        cfg.setFileMaxSize(fileMaxSize);
        cfg.setTotalSize(totalSize);
        cfg.setCharset(charset);
        cfg.setColumnIndexWidthMap(columnIndexWidthMap);
        cfg.setNewIndexFixedWidth(indexFixedWidth);
        cfg.setNewCurrentRow(currentRow);
        cfg.setIndexFixedWidthFactor(indexFixedWidthFactor);
        cfg.setNewDataFilePointer(contentRafWriter.getPosition());
        cfg.setNewIndexFilePointer(indexRafWriter.getPosition());
        FileUtil.writeUtf8String(JSON.toJSONString(cfg), new File(buildPath(this.path, Constants.CFG_FILE_NAME)));

        closeWriter();
    }


    public static FileDataWriterBuilder builder() {
        return new FileDataWriterBuilder();
    }

    public static final class FileDataWriterBuilder {
        private Integer fileMaxSize = 10000;
        private String charset = "UTF-8";
        /**
         * 索引文件索引间固定宽度因子
         * 默认基于写入的第一条数据的宽度+3
         */
        private Long indexFixedWidthFactor = 3l;
        private WriteMode writeMode = WriteMode.NOT_ALLOW_REPEATED;

        private FileWriterFactory fileFactory = new BufferedRandomAccessFileWriterFactory();


        private FileDataWriterBuilder() {
        }

        public FileDataWriterBuilder withFileMaxSize(Integer fileMaxSize) {
            this.fileMaxSize = fileMaxSize;
            return this;
        }

        public FileDataWriterBuilder withWriteMode(WriteMode writeMode) {
            this.writeMode = writeMode;
            return this;
        }

        public FileDataWriterBuilder withCharset(String charset) {
            this.charset = charset;
            return this;
        }

        public FileDataWriterBuilder withIndexFixedWidthFactor(Long indexFixedWidthFactor) {
            this.indexFixedWidthFactor = indexFixedWidthFactor;
            return this;
        }

        public FileDataWriterBuilder withFileFactory(FileWriterFactory fileFactory) {
            this.fileFactory = fileFactory;
            return this;
        }

        public FileDataWriter build(String path) throws IOException {
            FileDataWriter fileDataWriter = new FileDataWriter(path, writeMode, fileFactory);
            if (!WriteMode.APPEND.equals(writeMode) || fileDataWriter.totalSize == 0) {
                fileDataWriter.charset = this.charset;
                fileDataWriter.fileMaxSize = this.fileMaxSize;
                fileDataWriter.indexFixedWidthFactor = this.indexFixedWidthFactor;
            }
            return fileDataWriter;
        }
    }
}
