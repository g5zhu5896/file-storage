package com.zzz.utils.file.writer.factory;

import com.zzz.utils.file.writer.FileWriter;
import com.zzz.utils.file.writer.BufferNioByteChannelFileWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class BufferNioByteChannelFileWriterFactory implements FileWriterFactory {

    @Override
    public FileWriter fileWriter(File file, Charset charset) throws IOException {
        return new BufferNioByteChannelFileWriter(file, charset);
    }
}
