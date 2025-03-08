package com.zzz.utils.file.writer;

import java.io.Closeable;
import java.io.IOException;

public interface FileWriter extends Closeable {
    long getPosition() throws IOException;

    void setPosition(long position) throws IOException;

    void writeLine(String data) throws IOException;

    void write(String data) throws IOException;

    void flush() throws IOException;
}
