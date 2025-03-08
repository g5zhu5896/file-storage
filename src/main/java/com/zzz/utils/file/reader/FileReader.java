package com.zzz.utils.file.reader;

import java.io.Closeable;
import java.io.IOException;

public interface FileReader extends Closeable {
    long getPosition() throws IOException;

    void setPosition(long position) throws IOException;

    String readLine() throws IOException;

    int read(char cbuf[]) throws IOException;

}
