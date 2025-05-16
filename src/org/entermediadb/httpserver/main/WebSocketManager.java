package org.entermediadb.httpserver.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import org.entermediadb.httpserver.http.HttpRequest;

public class WebSocketManager {
    private static final String WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    public WebSocketManager(Socket socket) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    public static boolean isWebSocketUpgradeRequest(HttpRequest request) {
        if( "GET".equals(request.getMethod()) )
        {
        	if( "websocket".equalsIgnoreCase(request.getHeader("Upgrade")) )
			{
			   Map names = request.getHeaders();
               if( names.containsKey("Sec-WebSocket-Key") && names.containsKey("Sec-WebSocket-Version") )
               {
            	   return true;
               }
			} 
        }
        return false;
    }

    public void handleWebSocketUpgrade(HttpRequest request) throws IOException {
        String secWebSocketKey = request.getHeader("Sec-WebSocket-Key");
        String acceptKey = generateWebSocketAcceptKey(secWebSocketKey);

        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 101 Switching Protocols\r\n");
        response.append("Upgrade: websocket\r\n");
        response.append("Connection: Upgrade\r\n");
        response.append("Sec-WebSocket-Accept: ").append(acceptKey).append("\r\n");
        response.append("\r\n");

        out.write(response.toString().getBytes());
        out.flush();
        System.out.println("WebSocket connection established");
    }

    private String generateWebSocketAcceptKey(String secWebSocketKey) {
        try {
            String concatenated = secWebSocketKey + WEBSOCKET_GUID;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(concatenated.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }

    public void handleWebSocketFrame() throws IOException {
        // Read the first two bytes
        byte[] frameHeader = new byte[2];
        if (in.read(frameHeader) != 2) return;

        boolean fin = (frameHeader[0] & 0x80) != 0;
        int opcode = frameHeader[0] & 0x0F;
        boolean masked = (frameHeader[1] & 0x80) != 0;
        int payloadLength = frameHeader[1] & 0x7F;

        // Handle different payload lengths
        if (payloadLength == 126) {
            byte[] lengthBytes = new byte[2];
            in.read(lengthBytes);
            payloadLength = ByteBuffer.wrap(lengthBytes).getShort();
        } else if (payloadLength == 127) {
            byte[] lengthBytes = new byte[8];
            in.read(lengthBytes);
            payloadLength = (int) ByteBuffer.wrap(lengthBytes).getLong();
        }

        // Read mask if present
        byte[] mask = new byte[4];
        if (masked) {
            in.read(mask);
        }

        // Read payload
        byte[] payload = new byte[payloadLength];
        in.read(payload);

        // Unmask if necessary
        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (payload[i] ^ mask[i % 4]);
            }
        }

        // Handle different opcodes
        switch (opcode) {
            case 0x1: // Text frame
                String message = new String(payload);
                System.out.println("Received WebSocket message: " + message);
                sendTextMessage("Server received: " + message);
                break;
            case 0x8: // Close frame
                sendCloseFrame();
                socket.close();
                break;
            case 0x9: // Ping frame
                sendPongFrame();
                break;
        }
    }

    public void sendTextMessage(String message) throws IOException {
        byte[] payload = message.getBytes();
        
        // Create frame header
        byte[] frame = new byte[payload.length + 2];
        frame[0] = (byte) 0x81; // FIN + Text frame
        frame[1] = (byte) payload.length; // Payload length

        // Copy payload
        System.arraycopy(payload, 0, frame, 2, payload.length);
        
        out.write(frame);
        out.flush();
    }

    public void sendCloseFrame() throws IOException {
        byte[] frame = {(byte) 0x88, 0x00}; // Close frame
        out.write(frame);
        out.flush();
    }

    public void sendPongFrame() throws IOException {
        byte[] frame = {(byte) 0x8A, 0x00}; // Pong frame
        out.write(frame);
        out.flush();
    }

    public boolean isSocketClosed() {
        return socket.isClosed();
    }

    public void closeSocket() throws IOException {
        socket.close();
    }
} 