package com.zzz.utils.file.writer;


import sun.security.action.GetPropertyAction;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.AccessController;

/**
 * 用 BufferWriter 写文件
 * 支持不按行进行 flush
 * <p>
 * 由于使用了缓冲区，没有 flush,从文件中获取的写入位置不会包含缓冲区待写入数据写入后的位置，所以需要自己记录缓冲区写入后的偏移量
 * <p>
 * 其实可以全靠自己计算文件中的偏移量(类似 bufferByteOffset 变量的计算)，这样就可以不依赖如RandomAccessFile来获取文件中当前写入位置的偏移量。也就无需区分待写入数据和已写入数据了
 */
public abstract class AbstractBufferFileWriter implements FileWriter, Closeable {
    private BufferedWriter writer;

    //字符缓冲区总大小
    private int nChars = 8192;
    //待flush的字符缓冲区大小
    private int nextChar;
    //待flush的字符缓冲区写入后占用文件的偏移量
    private int bufferByteOffset;

    //换行符
    private String lineSeparator;
    private Charset charset;

    public AbstractBufferFileWriter(Charset charset) {
        this.charset = charset;
        nextChar = 0;
        bufferByteOffset = 0;
        lineSeparator = AccessController.doPrivileged(
                new GetPropertyAction("line.separator"));
    }

    void init(OutputStreamWriter outputStreamWriter) {
        writer = new BufferedWriter(outputStreamWriter, nChars);
    }

    abstract long getFilePosition() throws IOException;

    @Override
    public long getPosition() throws IOException {
        //由于没有flush的话,raf.getFilePointer() 获取不到待写入数据写入后的位置，所以需要加上待写入数据写入移动的位置nextChar
        return getFilePosition() + bufferByteOffset;
    }

    @Override
    public void writeLine(String data) throws IOException {
        write(data);
        write(lineSeparator);
    }

    @Override
    public void write(String data) throws IOException {
        int len = data.length();

        //如果写入的数据本身大于缓冲区的数据，又或者写入完刚好缓冲区满了，都需要在 write() 触发一次 flush() ,否则会导致获取到的已写入数据的文件偏移量是错的
        //因为这种情况写入后会触发writer.flushBuffer(),但不会将数据刷盘。所以获取到的文件偏移量不会包含被 flushBuffer() 后的数据
        if (len > nChars || nextChar + len == nChars) {
            writer.write(data);
            countBufferPos(data);
            flush();
        } else {
            //如果写入后的数据会让缓冲区溢出，在write()前先触发一次 flush(),让当前的数据直接刷盘,否则会导致获取到的已写入数据的文件偏移量是错的
            //其实这个也可以在write() 后触发 flush() ,但是这样会导致调用两次writer.flushBuffer(),不过性能不一定更差。
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
        bufferByteOffset = 0;
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
        bufferByteOffset += s.getBytes(charset).length;
    }

}
