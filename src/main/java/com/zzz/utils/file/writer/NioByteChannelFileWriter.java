package com.zzz.utils.file.writer;


import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class NioByteChannelFileWriter implements FileWriter {
    private SeekableByteChannel channel;
    private BufferedWriter writer;

    public NioByteChannelFileWriter(File file, Charset charset) throws IOException {
        channel = Files.newByteChannel(file.toPath());
        writer = new BufferedWriter(new OutputStreamWriter(Channels.newOutputStream(channel), charset));
    }

    @Override
    public long getPosition() throws IOException {
        return channel.position();
    }

    @Override
    public void setPosition(long position) throws IOException {
        channel.position(position);
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
        channel.close();
    }


}
