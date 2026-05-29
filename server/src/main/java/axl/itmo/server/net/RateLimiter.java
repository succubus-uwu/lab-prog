package axl.itmo.server.net;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter to restrict the number of requests per minute per client.
 */
public class RateLimiter {

    private volatile int maxRequestsPerMinute = 100;
    
    private final ConcurrentHashMap<String, ClientRecord> clients = new ConcurrentHashMap<>();

    public void setMaxRequestsPerMinute(int max) {
        this.maxRequestsPerMinute = max;
    }

    public int getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    public boolean isAllowed(String clientId) {
        if (maxRequestsPerMinute <= 0) return true;

        long currentMinute = Instant.now().getEpochSecond() / 60;

        ClientRecord record = clients.compute(clientId, (key, val) -> {
            if (val == null || val.minute != currentMinute) {
                return new ClientRecord(currentMinute, 1);
            }
            val.count.incrementAndGet();
            return val;
        });

        if (clients.size() > 100) {
            cleanup(currentMinute);
        }

        return record.count.get() <= maxRequestsPerMinute;
    }

    private void cleanup(long currentMinute) {
        clients.entrySet().removeIf(entry -> entry.getValue().minute != currentMinute);
    }

    private static class ClientRecord {
        long minute;
        AtomicInteger count;

        ClientRecord(long minute, int initialCount) {
            this.minute = minute;
            this.count = new AtomicInteger(initialCount);
        }
    }
}
