package axl.itmo.server.net;

import axl.itmo.common.dto.CommandRequest;
import axl.itmo.server.logging.ServerLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Reads command requests from client connections.
 */
public class RequestReader {
    private final ServerLogger logger = ServerLogger.getInstance();

    /**
     * Reads a CommandRequest from the socket channel.
     *
     * @param socketChannel the client connection
     * @return the command request, or null if error occurs
     */
    public CommandRequest readRequest(SocketChannel socketChannel) {
        try {
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            while (lengthBuffer.hasRemaining()) {
                int read = socketChannel.read(lengthBuffer);
                if (read == -1) return null;
                if (read == 0) {
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                }
            }
            lengthBuffer.flip();
            int length = lengthBuffer.getInt();
            
            ByteBuffer dataBuffer = ByteBuffer.allocate(length);
            while (dataBuffer.hasRemaining()) {
                int read = socketChannel.read(dataBuffer);
                if (read == -1) return null;
                if (read == 0) {
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                }
            }
            
            ByteArrayInputStream bais = new ByteArrayInputStream(dataBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);
            CommandRequest request = (CommandRequest) ois.readObject();
            logger.logRequestReceived(request.getCommandName(), socketChannel.socket().getInetAddress().getHostAddress());
            return request;
        } catch (ClassNotFoundException e) {
            logger.logError("Invalid command object received", e);
            return null;
        } catch (IOException e) {
            logger.logError("Error reading request", e);
            return null;
        }
    }
}
