package org.entermediadb.httpserver.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.entermediadb.httpserver.http.HttpResponse;

public class ChunkedOutputStream extends OutputStream
{
	protected OutputStream toclientstream;
	protected boolean fieldClosed;
	
	public boolean isClosed()
	{
		return fieldClosed;
	}

	public void setClosed(boolean inClosed)
	{
		fieldClosed = inClosed;
	}

	public OutputStream toClientStream()
	{
		return toclientstream;
	}
	protected HttpResponse fieldHttpResponse;
	public HttpResponse getHttpResponse()
	{
		return fieldHttpResponse;
	}

	public ChunkedOutputStream(HttpResponse inResponse, OutputStream intoclientstream)
	{
		toclientstream = intoclientstream;
		fieldHttpResponse = inResponse;
	}
        
         @Override
         public void write(int b) throws IOException {
        	 getHttpResponse().writeHeaders();
             if (getHttpResponse().isChunked()) {
                 // Write single byte as a chunk
                 String chunkSize = Integer.toHexString(1) + "\r\n";
                 toclientstream.write(chunkSize.getBytes(StandardCharsets.UTF_8));
                 toclientstream.write(b);
                 toclientstream.write("\r\n".getBytes(StandardCharsets.UTF_8));
             } else {
                 toclientstream.write(b);
             }
         }

         @Override
         public void write(byte[] b, int off, int len) throws IOException {
        	 getHttpResponse().writeHeaders();
             if (getHttpResponse().isChunked()) {
                 // Write chunk
                 String chunkSize = Integer.toHexString(len) + "\r\n";
                 toclientstream.write(chunkSize.getBytes(StandardCharsets.UTF_8));
                 toclientstream.write(b, off, len);
                 toclientstream.write("\r\n".getBytes(StandardCharsets.UTF_8));
             } else {
                 toclientstream.write(b, off, len);
             }
         }

         @Override
         public void flush() throws IOException {
             toclientstream.flush();
         }

//         public void end() throws IOException {
//        	 if (getHttpResponse().isChunked()) {
//                 // Write final chunk
//                 toclientstream.write("0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
//                 toclientstream.flush();
//                 return;
//             }
//        	 if( !isClosed() )
//        	 {
//        		 close();
//        		 //toclientstream.close();
//        		 setClosed(true);
//        	 }
//         }
         @Override
         public void close() throws IOException {
//        	 if (getHttpResponse().isChunked()) {
//        		 end();
//        		 return;
//             }
        	 getHttpResponse().writeHeaders();
        	 if( !isClosed() )
        	 {
	             setClosed(true);
	             toclientstream.close();
        	 }
         }
         
         
}
