package com.zzz.utils.file.reader.factory;

import com.zzz.utils.file.reader.BufferedRandomAccessFileReader;
import com.zzz.utils.file.reader.FileReader;

import java.io.IOException;
import java.nio.charset.Charset;

public class BufferedRandomAccessFileReaderFactory implements FileReaderFactory {
    @Override
    public FileReader fileReader(String file, Charset charset) throws IOException {
        return new BufferedRandomAccessFileReader(file, charset);
    }
}
