package com.zzz.utils.file.writer;


import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

/**
 * 用Files.newByteChannel 写文件
 */
public class BufferNioByteChannelFileWriter extends AbstractBufferFileWriter {
    private SeekableByteChannel channel;

    public BufferNioByteChannelFileWriter(File file, Charset charset) throws IOException {
        super(charset);
        channel = Files.newByteChannel(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        init(new OutputStreamWriter(Channels.newOutputStream(channel), charset));
    }

    @Override
    long getFilePosition() throws IOException {
        return channel.position();
    }

    @Override
    public void setPosition(long position) throws IOException {
        channel.position(position);
    }

    @Override
    public void close() throws IOException {
        super.close();
        channel.close();
    }


}
