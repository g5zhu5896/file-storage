package com.zzz.utils.file.reader;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class BufferNioByteChannelFileReader implements FileReader {

    private SeekableByteChannel channel;
    private BufferedReader bufferedReader;

    public BufferNioByteChannelFileReader(String file, Charset charset) throws IOException {
        channel = Files.newByteChannel(new File(file).toPath());
        bufferedReader = new BufferedReader(new InputStreamReader(Channels.newInputStream(channel), charset));
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
    public String readLine() throws IOException {
        return bufferedReader.readLine();
    }

    @Override
    public int read(char[] cbuf) throws IOException {
        return bufferedReader.read(cbuf);
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
        channel.close();
    }
}
