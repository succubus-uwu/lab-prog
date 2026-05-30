package axl.itmo.client.gui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * Singleton that holds the currently selected locale and exposes StringBindings
 * so all UI labels update automatically when the locale is switched at runtime.
 *
 * Uses getResourceAsStream instead of ResourceBundle.getBundle to avoid
 * NullPointerException with JavaFX module-path class loading.
 */
public class LocaleManager {

    // Locale constants MUST be declared before INSTANCE to avoid null during static init
    public static final Locale RU    = new Locale("ru", "RU");
    public static final Locale SL    = new Locale("sl", "SI");
    public static final Locale LT    = new Locale("lt", "LT");
    public static final Locale EN_NZ = new Locale("en", "NZ");

    public static final List<Locale> SUPPORTED = List.of(RU, SL, LT, EN_NZ);

    private static final LocaleManager INSTANCE = new LocaleManager();

    private final ObjectProperty<Locale>           currentLocale = new SimpleObjectProperty<>(RU);
    private final ObjectProperty<ResourceBundle>   bundle        = new SimpleObjectProperty<>();

    private LocaleManager() {
        currentLocale.addListener((obs, old, loc) -> bundle.set(loadBundle(loc)));
        bundle.set(loadBundle(RU));
    }

    public static LocaleManager getInstance() { return INSTANCE; }

    // ── Loading ───────────────────────────────────────────────────────────

    /**
     * Loads a .properties file for the given locale using Class.getResourceAsStream,
     * which works reliably regardless of the JVM's class-loader hierarchy.
     * Falls back through language+country → language → default (no suffix).
     */
    private ResourceBundle loadBundle(Locale locale) {
        String[] candidates = {
            locale.getLanguage() + "_" + locale.getCountry(),
            locale.getLanguage(),
            ""
        };
        for (String suffix : candidates) {
            String path = "/axl/itmo/client/gui/i18n/messages"
                    + (suffix.isEmpty() ? "" : "_" + suffix) + ".properties";
            try (InputStream is = LocaleManager.class.getResourceAsStream(path)) {
                if (is == null) continue;
                Properties props = new Properties();
                props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                return new PropertyResourceBundleWrapper(props);
            } catch (IOException ignored) {}
        }
        // Absolute fallback: empty bundle — keys will render as themselves
        return new PropertyResourceBundleWrapper(new Properties());
    }

    // ── Public API ────────────────────────────────────────────────────────

    public ObjectProperty<Locale> localeProperty() { return currentLocale; }
    public Locale getLocale()                       { return currentLocale.get(); }
    public void   setLocale(Locale locale)          { currentLocale.set(locale); }
    public ResourceBundle getBundle()               { return bundle.get(); }

    public String get(String key) {
        try   { return bundle.get().getString(key); }
        catch (Exception e) { return key; }
    }

    /** Returns a StringBinding that re-resolves {@code key} whenever the locale changes. */
    public StringBinding binding(String key) {
        return Bindings.createStringBinding(() -> get(key), bundle);
    }

    public String formatNumber(Number n) {
        return NumberFormat.getNumberInstance(getLocale()).format(n);
    }

    public String formatDate(Date d) {
        if (d == null) return "";
        return DateFormat.getDateInstance(DateFormat.MEDIUM, getLocale()).format(d);
    }

    public String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "";
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(getLocale()).format(dt);
    }

    public String localeDisplayName(Locale locale) {
        return locale.getDisplayName(locale);
    }

    // ── Inner class ───────────────────────────────────────────────────────

    private static class PropertyResourceBundleWrapper extends ResourceBundle {
        private final Properties props;

        PropertyResourceBundleWrapper(Properties props) {
            this.props = props;
        }

        @Override
        protected Object handleGetObject(String key) {
            return props.getProperty(key);
        }

        @Override
        public Enumeration<String> getKeys() {
            return Collections.enumeration(props.stringPropertyNames());
        }
    }
}
