package axl.itmo.client.gui.network;

import axl.itmo.common.dto.CommandRequest;
import axl.itmo.common.dto.CommandResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

/**
 * Thread-safe wrapper around the binary serialisation protocol.
 * All public methods may be called from any thread; network I/O is always
 * performed on the calling thread (use JavaFX Task / background thread at
 * the call site).
 */
public class NetworkService {

    private final String host;
    private final int port;

    private SocketChannel channel;
    private String login;
    private String password;

    public NetworkService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void setCredentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() { return login; }

    /** Opens a new connection if none exists or current one is closed. */
    public synchronized void ensureConnected() throws IOException {
        if (channel != null && channel.isOpen() && channel.isConnected()) return;
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        if (!channel.connect(new InetSocketAddress(host, port))) {
            long deadline = System.currentTimeMillis() + 5000;
            while (!channel.finishConnect()) {
                if (System.currentTimeMillis() > deadline)
                    throw new IOException("Connection timed out");
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            }
        }
    }

    public synchronized void disconnect() {
        if (channel != null) {
            try { channel.close(); } catch (IOException ignored) {}
            channel = null;
        }
    }

    /**
     * Sends a request and waits for a response synchronously.
     * Must be called from a background thread (not the FX application thread).
     */
    public synchronized CommandResponse execute(String commandName, Object argument) throws IOException {
        ensureConnected();
        CommandRequest req = new CommandRequest(commandName, argument);
        req.setLogin(login);
        req.setPassword(password);
        send(channel, req);
        try {
            return receive(channel);
        } catch (ClassNotFoundException e) {
            throw new IOException("Protocol error: " + e.getMessage(), e);
        }
    }

    /** Executes on a new daemon thread and delivers the result to {@code callback} on the same thread. */
    public void executeAsync(String commandName, Object argument,
                             Consumer<CommandResponse> onSuccess,
                             Consumer<Exception> onError) {
        Thread t = new Thread(() -> {
            try {
                CommandResponse r = execute(commandName, argument);
                onSuccess.accept(r);
            } catch (Exception e) {
                onError.accept(e);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── Low-level send/receive ────────────────────────────────────────────────

    static void send(SocketChannel ch, CommandRequest request) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(request);
        }
        byte[] data = baos.toByteArray();
        ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
        buf.putInt(data.length);
        buf.put(data);
        buf.flip();
        while (buf.hasRemaining()) {
            int w = ch.write(buf);
            if (w == 0) { try { Thread.sleep(10); } catch (InterruptedException ignored) {} }
        }
    }

    static CommandResponse receive(SocketChannel ch) throws IOException, ClassNotFoundException {
        ByteBuffer lenBuf = ByteBuffer.allocate(4);
        while (lenBuf.hasRemaining()) {
            int r = ch.read(lenBuf);
            if (r == -1) throw new IOException("Connection closed by server");
            if (r == 0) { try { Thread.sleep(10); } catch (InterruptedException ignored) {} }
        }
        lenBuf.flip();
        int length = lenBuf.getInt();

        ByteBuffer dataBuf = ByteBuffer.allocate(length);
        while (dataBuf.hasRemaining()) {
            int r = ch.read(dataBuf);
            if (r == -1) throw new IOException("Connection closed by server");
            if (r == 0) { try { Thread.sleep(10); } catch (InterruptedException ignored) {} }
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(dataBuf.array()))) {
            return (CommandResponse) ois.readObject();
        }
    }
}
