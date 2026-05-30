package axl.itmo.client.gui.render;

import axl.itmo.client.gui.util.ColorRegistry;
import axl.itmo.common.model.Coordinates;
import axl.itmo.common.model.Person;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Canvas that visualises the Person collection using graphical primitives.
 *
 * Layout rules:
 *   canvas_x  = coordinates.x / MAX_X * (width  - 2*PAD) + PAD
 *   canvas_y  = height - (Math.abs(coordinates.y % (height - 2*PAD)) + PAD)
 *   radius    = clamp(person.height / 8, 6, 50)
 *
 * Each person owned by a different user gets a distinct colour via ColorRegistry.
 * New/updated persons animate with a scale-in effect; removed ones scale out.
 */
public class CollectionCanvas extends Canvas {

    private static final double MAX_X   = 862.0;
    private static final double PAD     = 30.0;
    private static final double ANIM_IN  = 0.5; // seconds
    private static final double ANIM_OUT = 0.3;

    private static class AnimState {
        Person person;
        double scale   = 0.0;
        double targetScale = 1.0;
        boolean removing = false;
        long startNano  = System.nanoTime();

        AnimState(Person p) { this.person = p; }
    }

    private final Map<Integer, AnimState> states = new LinkedHashMap<>();
    private final List<Person> current = new CopyOnWriteArrayList<>();
    private Consumer<Person> clickCallback;
    private Integer hoveredId = null;
    private final AnimationTimer timer;

    public CollectionCanvas(double width, double height) {
        super(width, height);
        timer = new AnimationTimer() {
            @Override public void handle(long now) { tick(now); }
        };
        timer.start();

        setOnMouseMoved(e -> {
            hoveredId = findPersonAt(e.getX(), e.getY());
        });
        setOnMouseClicked(e -> {
            Integer id = findPersonAt(e.getX(), e.getY());
            if (id != null && clickCallback != null) {
                states.get(id);
                Person p = current.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
                if (p != null) clickCallback.accept(p);
            }
        });
    }

    public void setClickCallback(Consumer<Person> cb) {
        this.clickCallback = cb;
    }

    /**
     * Called from FX thread after every push update.
     * Computes which persons are new, changed, or removed and sets up animations.
     */
    public void update(List<Person> persons) {
        Set<Integer> newIds = new HashSet<>();
        for (Person p : persons) {
            newIds.add(p.getId());
            AnimState s = states.get(p.getId());
            if (s == null) {
                // Brand-new
                AnimState ns = new AnimState(p);
                ns.scale = 0;
                ns.targetScale = 1;
                ns.startNano = System.nanoTime();
                states.put(p.getId(), ns);
            } else {
                // Possibly updated
                s.person = p;
                if (s.removing) {
                    // was being removed, now back
                    s.removing = false;
                    s.targetScale = 1;
                    s.startNano = System.nanoTime();
                }
            }
        }
        // Mark removed
        for (AnimState s : states.values()) {
            if (!newIds.contains(s.person.getId()) && !s.removing) {
                s.removing = true;
                s.startNano = System.nanoTime();
                s.targetScale = 0;
            }
        }
        current.clear();
        current.addAll(persons);
    }

    private void tick(long nowNano) {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth(), h = getHeight();

        gc.setFill(Color.web("#1e1e2e"));
        gc.fillRect(0, 0, w, h);

        // Grid lines
        gc.setStroke(Color.web("#313244"));
        gc.setLineWidth(0.5);
        for (double x = PAD; x < w; x += 60) { gc.strokeLine(x, 0, x, h); }
        for (double y = PAD; y < h; y += 60) { gc.strokeLine(0, y, w, y); }

        // Axis labels
        gc.setFill(Color.web("#6c7086"));
        gc.setFont(Font.font(10));
        gc.fillText("x→", w - 20, h - 5);
        gc.fillText("↑y", 5, 15);

        List<Integer> toRemove = new ArrayList<>();

        for (AnimState s : states.values()) {
            double elapsed = (nowNano - s.startNano) / 1_000_000_000.0;
            if (s.removing) {
                s.scale = 1.0 - Math.min(elapsed / ANIM_OUT, 1.0);
                if (s.scale <= 0) { toRemove.add(s.person.getId()); continue; }
            } else {
                s.scale = Math.min(elapsed / ANIM_IN, 1.0);
            }
            drawPerson(gc, s, w, h);
        }
        toRemove.forEach(states::remove);
    }

    private void drawPerson(GraphicsContext gc, AnimState s, double canvasW, double canvasH) {
        Person p = s.person;
        Coordinates coords = p.getCoordinates();
        if (coords == null) return;

        double cx = toCanvasX(coords.getX(), canvasW);
        double cy = toCanvasY(coords.getY(), canvasH);
        double r  = clampRadius(p.getHeight()) * s.scale;
        if (r < 0.5) return;

        Color base = ColorRegistry.forOwner(p.getOwnerId());
        boolean hovered = Objects.equals(hoveredId, p.getId());

        // Shadow
        gc.setFill(base.deriveColor(0, 1, 0.5, 0.3));
        gc.fillOval(cx - r + 3, cy - r + 3, r * 2, r * 2);

        // Body
        gc.setFill(hovered ? base.brighter() : base);
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Stroke
        gc.setStroke(Color.WHITE.deriveColor(0, 1, 1, 0.4));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

        // Label (name, truncated)
        if (r > 12) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(Math.min(r * 0.5, 12)));
            String label = p.getName().length() > 8 ? p.getName().substring(0, 7) + "…" : p.getName();
            gc.fillText(label, cx - r * 0.8, cy + 4);
        }
    }

    private Integer findPersonAt(double mx, double my) {
        double canvasW = getWidth(), canvasH = getHeight();
        for (AnimState s : states.values()) {
            Person p = s.person;
            Coordinates c = p.getCoordinates();
            if (c == null) continue;
            double cx = toCanvasX(c.getX(), canvasW);
            double cy = toCanvasY(c.getY(), canvasH);
            double r  = clampRadius(p.getHeight()) * s.scale;
            double dx = mx - cx, dy = my - cy;
            if (dx * dx + dy * dy <= r * r) return p.getId();
        }
        return null;
    }

    private double toCanvasX(double x, double w) {
        return x / MAX_X * (w - 2 * PAD) + PAD;
    }

    private double toCanvasY(long y, double h) {
        double range = h - 2 * PAD;
        return h - (Math.abs(y % (long) range) + PAD);
    }

    private double clampRadius(float height) {
        return Math.max(6, Math.min(height / 8.0, 50));
    }

    public void stopTimer() { timer.stop(); }
}
