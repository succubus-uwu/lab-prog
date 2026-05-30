package axl.itmo.client.gui.network;

import axl.itmo.common.dto.CommandRequest;
import axl.itmo.common.dto.CommandResponse;
import axl.itmo.common.model.Person;
import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.function.Consumer;

/**
 * Background thread that maintains a dedicated push-subscription connection to the server.
 * On startup it sends a SUBSCRIBE command; the server then pushes CommandResponse(data=collection)
 * after every mutating operation. Each push is forwarded to the supplied callback on the FX thread.
 */
public class PushListener {

    private final String host;
    private final int port;
    private final String login;
    private final String password;
    private final Consumer<List<Person>> onUpdate;
    private final Consumer<String> onError;

    private volatile boolean running = false;
    private Thread thread;
    private SocketChannel channel;

    public PushListener(String host, int port, String login, String password,
                        Consumer<List<Person>> onUpdate, Consumer<String> onError) {
        this.host = host;
        this.port = port;
        this.login = login;
        this.password = password;
        this.onUpdate = onUpdate;
        this.onError = onError;
    }

    public void start() {
        running = true;
        thread = new Thread(this::run, "push-listener");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        if (channel != null) {
            try { channel.close(); } catch (IOException ignored) {}
        }
        if (thread != null) thread.interrupt();
    }

    private void run() {
        while (running) {
            try {
                connect();
                listenLoop();
            } catch (Exception e) {
                if (!running) break;
                String msg = e.getMessage();
                Platform.runLater(() -> onError.accept("Push connection lost: " + msg + ". Reconnecting…"));
                try { Thread.sleep(3000); } catch (InterruptedException ignored) { break; }
            }
        }
    }

    private void connect() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        if (!channel.connect(new InetSocketAddress(host, port))) {
            long deadline = System.currentTimeMillis() + 5000;
            while (!channel.finishConnect()) {
                if (System.currentTimeMillis() > deadline)
                    throw new IOException("Push connection timed out");
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            }
        }

        // Send SUBSCRIBE command
        CommandRequest req = new CommandRequest("subscribe", null);
        req.setLogin(login);
        req.setPassword(password);
        NetworkService.send(channel, req);

        // Wait for ACK
        CommandResponse ack;
        try {
            ack = NetworkService.receive(channel);
        } catch (ClassNotFoundException e) {
            throw new IOException("Protocol error during subscribe ACK", e);
        }
        if (!ack.isSuccess()) {
            throw new IOException("Server rejected subscription: " + ack.getMessage());
        }
    }

    private void listenLoop() throws IOException {
        while (running) {
            CommandResponse push;
            try {
                push = NetworkService.receive(channel);
            } catch (ClassNotFoundException e) {
                throw new IOException("Protocol deserialization error", e);
            }
            if (push.isSuccess() && "push_update".equals(push.getMessage())
                    && push.getData() != null) {
                List<Person> snapshot = List.copyOf(push.getData());
                Platform.runLater(() -> onUpdate.accept(snapshot));
            }
        }
    }
}
