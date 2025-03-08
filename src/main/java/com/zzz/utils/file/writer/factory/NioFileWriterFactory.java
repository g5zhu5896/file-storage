package com.zzz.utils.file.writer.factory;

import com.zzz.utils.file.writer.FileWriter;
import com.zzz.utils.file.writer.NioByteChannelFileWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class NioFileWriterFactory implements FileWriterFactory {

    @Override
    public FileWriter fileWriter(File file, Charset charset) throws IOException {
        return new NioByteChannelFileWriter(file, charset);
    }
}
