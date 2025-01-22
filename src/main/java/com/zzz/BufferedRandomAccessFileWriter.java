package com.zzz;

import lombok.Getter;

import java.io.*;

@Getter
public class BufferedRandomAccessFileWriter implements Closeable {
    private BufferedWriter writer;
    private RandomAccessFile raf;

    public BufferedRandomAccessFileWriter(File file, String charset) throws IOException {
        raf = new RandomAccessFile(file, "rw");

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(raf.getFD()), charset);
        writer = new BufferedWriter(outputStreamWriter);
    }

    @Override
    public void close() throws IOException {
        writer.close();
        raf.close();
    }
}
