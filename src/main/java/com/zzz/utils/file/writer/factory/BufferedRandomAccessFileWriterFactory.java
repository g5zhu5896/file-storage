package com.zzz.utils.file.writer.factory;

import com.zzz.utils.file.writer.BufferedRandomAccessFileWriter;
import com.zzz.utils.file.writer.FileWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class BufferedRandomAccessFileWriterFactory implements FileWriterFactory {

    @Override
    public FileWriter fileWriter(File file, Charset charset) throws IOException {
        return new BufferedRandomAccessFileWriter(file, charset);
    }
}
