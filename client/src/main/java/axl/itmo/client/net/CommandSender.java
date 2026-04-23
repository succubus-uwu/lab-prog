package axl.itmo.client.net;

import axl.itmo.common.dto.CommandRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Sends command requests to the server.
 */
public class CommandSender {
    private final String serverHost;
    private final int serverPort;

    public CommandSender(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    /**
     * Sends a command request to the server.
     *
     * @param request the command request
     * @param channel the socket channel
     * @throws IOException if sending fails
     */
    public void sendCommand(CommandRequest request, SocketChannel channel) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        oos.flush();
        byte[] data = baos.toByteArray();
        
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        
        while (buffer.hasRemaining()) {
            int written = channel.write(buffer);
            if (written == 0) {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
            }
        }
    }
}
