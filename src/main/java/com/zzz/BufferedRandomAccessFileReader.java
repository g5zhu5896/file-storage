package com.zzz;

import lombok.Getter;

import java.io.*;

@Getter
public class BufferedRandomAccessFileReader implements Closeable {
    private LineNumberReader reader;
    private RandomAccessFile raf;

    public BufferedRandomAccessFileReader(String file, String charset) throws IOException {
        raf = new RandomAccessFile(file, "r");
        reader = new LineNumberReader(new InputStreamReader(new FileInputStream(raf.getFD()), charset));
    }

    @Override
    public void close() throws IOException {
        reader.close();
        raf.close();
    }
}
