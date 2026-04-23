package axl.itmo.client.net;

import axl.itmo.common.dto.CommandResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Receives command responses from the server.
 */
public class ResponseReceiver {
    /**
     * Receives a command response from the server.
     *
     * @param channel the socket channel
     * @return the command response
     * @throws IOException if receiving fails
     * @throws ClassNotFoundException if response class is not found
     */
    public CommandResponse receiveResponse(SocketChannel channel) throws IOException, ClassNotFoundException {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        while (lengthBuffer.hasRemaining()) {
            int read = channel.read(lengthBuffer);
            if (read == -1) throw new IOException("Connection closed");
            if (read == 0) {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
            }
        }
        lengthBuffer.flip();
        int length = lengthBuffer.getInt();
        
        ByteBuffer dataBuffer = ByteBuffer.allocate(length);
        while (dataBuffer.hasRemaining()) {
            int read = channel.read(dataBuffer);
            if (read == -1) throw new IOException("Connection closed");
            if (read == 0) {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
            }
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(dataBuffer.array());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (CommandResponse) ois.readObject();
    }
}
