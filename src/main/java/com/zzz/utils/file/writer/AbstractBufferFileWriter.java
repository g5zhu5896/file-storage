package com.zzz.utils.file.writer;


import sun.security.action.GetPropertyAction;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.AccessController;

/**
 * 用 BufferWriter 写文件
 * 支持不按行进行 flush
 *
 * 由于使用了缓冲区，没有 flush,从文件中获取的写入位置不会包含缓冲区待写入数据写入后的位置，所以需要自己记录缓冲区写入后的偏移量
 */
public abstract class AbstractBufferFileWriter implements FileWriter,Closeable {
    private BufferedWriter writer;

    //字符缓冲区总大小
    private int nChars = 8192;
    //待flush的字符缓冲区大小
    private int nextChar;
    //换行符
    private String lineSeparator;

    void init(OutputStreamWriter outputStreamWriter){
        writer = new BufferedWriter(outputStreamWriter, nChars);
        nextChar = 0;
        lineSeparator = AccessController.doPrivileged(
                new GetPropertyAction("line.separator"));
    }

    abstract long getFilePosition() throws IOException;

    @Override
    public long getPosition() throws IOException {
        //由于没有flush的话,raf.getFilePointer() 获取不到待写入数据写入后的位置，所以需要加上待写入数据写入移动的位置nextChar
        return getFilePosition() + nextChar;
    }

    @Override
    public void writeLine(String data) throws IOException {
        write(data);
        write(lineSeparator);
    }

    @Override
    public void write(String data) throws IOException {
        int len = data.length();

        //如果写入的数据大于缓冲区
        if (len > nChars || nextChar + len == nChars) {
            writer.write(data);
            countBufferPos(data);
            flush();
        } else {
            if (nextChar + len > nChars) {
                flush();
            }
            writer.write(data);
            countBufferPos(data);
        }
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
        nextChar = 0;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    /**
     * 计算缓冲区待写入数据写入后在文件中的位置
     *
     * @param s
     */
    private void countBufferPos(String s) {
        int len = s.length();
        nextChar = (nextChar + len) % nChars;
    }

}
