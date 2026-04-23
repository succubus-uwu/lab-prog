package axl.itmo.server.net;

import axl.itmo.common.dto.CommandResponse;
import axl.itmo.server.logging.ServerLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Sends command responses to client connections.
 */
public class ResponseSender {
    private final ServerLogger logger = ServerLogger.getInstance();

    /**
     * Sends a CommandResponse to the socket channel.
     *
     * @param socketChannel the client connection
     * @param response the command response
     */
    public void sendResponse(SocketChannel socketChannel, CommandResponse response) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(response);
            oos.flush();
            byte[] data = baos.toByteArray();
            
            ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
            buffer.putInt(data.length);
            buffer.put(data);
            buffer.flip();
            
            while (buffer.hasRemaining()) {
                int written = socketChannel.write(buffer);
                if (written == 0) {
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                }
            }
            logger.logResponseSent(socketChannel.socket().getInetAddress().getHostAddress(), response.isSuccess());
        } catch (IOException e) {
            logger.logError("Error sending response", e);
        }
    }
}
