package com.zzz.utils.file.reader.factory;

import com.zzz.utils.file.reader.FileReader;
import com.zzz.utils.file.reader.BufferNioByteChannelFileReader;

import java.io.IOException;
import java.nio.charset.Charset;

public class BufferNioByteChannelFileReaderFactory implements FileReaderFactory {
    @Override
    public FileReader fileReader(String file, Charset charset) throws IOException {
        return new BufferNioByteChannelFileReader(file, charset);
    }

}
