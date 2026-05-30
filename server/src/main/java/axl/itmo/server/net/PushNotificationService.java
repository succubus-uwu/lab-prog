package axl.itmo.server.net;

import axl.itmo.common.dto.CommandResponse;
import axl.itmo.common.model.Person;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages push subscriptions and broadcasts collection updates to all subscribers.
 * A subscriber is a client SocketChannel that has sent a SUBSCRIBE command.
 * On any mutating operation (add/update/remove/clear) the server calls broadcast()
 * which serialises the full current collection and writes it to every subscribed channel.
 */
public class PushNotificationService {

    private final Set<SocketChannel> subscribers = ConcurrentHashMap.newKeySet();
    private final ResponseSender responseSender = new ResponseSender();

    public void addSubscriber(SocketChannel channel) {
        subscribers.add(channel);
    }

    public void removeSubscriber(SocketChannel channel) {
        subscribers.remove(channel);
    }

    public void broadcast(List<Person> collection) {
        if (subscribers.isEmpty()) return;
        CommandResponse update = new CommandResponse(true, "push_update", List.copyOf(collection));
        Set<SocketChannel> dead = ConcurrentHashMap.newKeySet();
        for (SocketChannel ch : subscribers) {
            try {
                synchronized (ch) {
                    responseSender.sendResponse(ch, update);
                }
            } catch (Exception e) {
                dead.add(ch);
            }
        }
        if (!dead.isEmpty()) {
            subscribers.removeAll(dead);
        }
    }

    public int subscriberCount() {
        return subscribers.size();
    }
}
