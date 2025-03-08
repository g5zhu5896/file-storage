package com.zzz.utils.file.writer;


import java.io.*;
import java.nio.charset.Charset;

/**
 * 用  RandomAccessFile 写文件
 */
public class BufferedRandomAccessFileWriter extends AbstractBufferFileWriter {
    private RandomAccessFile raf;


    public BufferedRandomAccessFileWriter(File file, Charset charset) throws IOException {
        raf = new RandomAccessFile(file, "rw");
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(raf.getFD()), charset);
        init(outputStreamWriter);
    }

    @Override
    long getFilePosition() throws IOException {
        return raf.getFilePointer();
    }

    @Override
    public void setPosition(long position) throws IOException {
        raf.seek(position);
    }

    @Override
    public void close() throws IOException {
        super.close();
        raf.close();
    }

}
