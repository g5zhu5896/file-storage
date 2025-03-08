package com.zzz.utils.file.reader;

import java.io.*;
import java.nio.charset.Charset;

public class BufferedRandomAccessFileReader implements FileReader {
    private LineNumberReader reader;
    private RandomAccessFile raf;

    public BufferedRandomAccessFileReader(String file, Charset charset) throws IOException {
        raf = new RandomAccessFile(file, "r");
        reader = new LineNumberReader(new InputStreamReader(new FileInputStream(raf.getFD()), charset.toString()));
    }

    @Override
    public void close() throws IOException {
        reader.close();
        raf.close();
    }

    @Override
    public long getPosition() throws IOException {
        return raf.getFilePointer();
    }

    @Override
    public void setPosition(long position) throws IOException {
        raf.seek(position);
    }

    @Override
    public String readLine() throws IOException {
        return reader.readLine();
    }

    @Override
    public int read(char cbuf[]) throws IOException {
        return reader.read(cbuf);
    }
}
