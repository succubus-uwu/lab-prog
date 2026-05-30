package axl.itmo.client.gui.util;

import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Assigns a stable Color to each ownerId so that all objects of the same user
 * are drawn in the same colour across the canvas and across sessions.
 */
public class ColorRegistry {

    private static final List<Color> PALETTE = List.of(
            Color.web("#e74c3c"),  // red
            Color.web("#3498db"),  // blue
            Color.web("#2ecc71"),  // green
            Color.web("#f39c12"),  // orange
            Color.web("#9b59b6"),  // purple
            Color.web("#1abc9c"),  // teal
            Color.web("#e67e22"),  // dark-orange
            Color.web("#2980b9")   // dark-blue
    );

    private static final Map<Long, Color> REGISTRY = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT = new AtomicInteger(0);

    private ColorRegistry() {}

    public static Color forOwner(long ownerId) {
        return REGISTRY.computeIfAbsent(ownerId,
                id -> PALETTE.get(NEXT.getAndIncrement() % PALETTE.size()));
    }

    /** Semi-transparent version for hover highlights */
    public static Color highlight(long ownerId) {
        return forOwner(ownerId).deriveColor(0, 1, 1.3, 0.7);
    }
}
