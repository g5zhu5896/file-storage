package com.zzz.utils.file.writer;


import java.io.*;
import java.nio.charset.Charset;

public class BufferedRandomAccessFileWriter implements FileWriter {
    private BufferedWriter writer;
    private RandomAccessFile raf;

    public BufferedRandomAccessFileWriter(File file, Charset charset) throws IOException {
        raf = new RandomAccessFile(file, "rw");

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(raf.getFD()), charset);
        writer = new BufferedWriter(outputStreamWriter);
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
    public void writeLine(String data) throws IOException {
        writer.write(data);
        writer.newLine();
    }

    @Override
    public void write(String data) throws IOException {
        writer.write(data);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
        raf.close();
    }


}
