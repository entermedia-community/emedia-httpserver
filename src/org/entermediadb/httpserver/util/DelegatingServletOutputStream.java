package org.entermediadb.httpserver.util;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class DelegatingServletOutputStream extends ServletOutputStream {
    private final OutputStream delegate;

    public DelegatingServletOutputStream(OutputStream delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate OutputStream must not be null");
        }
        this.delegate = delegate;
    }

    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException("Write listeners are not supported");
    }
} 