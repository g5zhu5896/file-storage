package com.zzz.utils.file.writer.factory;

import com.zzz.utils.file.writer.FileWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public interface FileWriterFactory {

    FileWriter fileWriter(File file, Charset charset) throws IOException;
}
