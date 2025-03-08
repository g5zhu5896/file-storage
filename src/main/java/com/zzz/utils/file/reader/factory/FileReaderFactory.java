package com.zzz.utils.file.reader.factory;

import com.zzz.utils.file.reader.FileReader;

import java.io.IOException;
import java.nio.charset.Charset;

public interface FileReaderFactory {
    FileReader fileReader(String file, Charset charset) throws IOException;
}
