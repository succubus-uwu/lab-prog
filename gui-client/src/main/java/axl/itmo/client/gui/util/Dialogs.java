package axl.itmo.client.gui.util;

import javafx.scene.control.*;

import java.util.Optional;

/**
 * Factory for localized JavaFX dialogs.
 * All titles and button labels are resolved from the current LocaleManager locale,
 * so they update automatically when the user switches language.
 */
public final class Dialogs {

    private Dialogs() {}

    private static LocaleManager lm() {
        return LocaleManager.getInstance();
    }

    // ── Non-blocking informational ────────────────────────────────────────

    public static void info(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(lm().get("dialog.title.info"));
        a.setHeaderText(null);
        a.setContentText(message);
        localizeButtons(a);
        a.show();
    }

    public static void error(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(lm().get("dialog.title.error"));
        a.setHeaderText(null);
        a.setContentText(message);
        localizeButtons(a);
        a.show();
    }

    // ── Blocking confirmation ─────────────────────────────────────────────

    /** Shows a modal confirmation dialog. Returns true if the user clicked OK. */
    public static boolean confirm(String message) {
        ButtonType ok     = new ButtonType(lm().get("dialog.button.ok"),     ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(lm().get("dialog.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ok, cancel);
        a.setTitle(lm().get("dialog.title.confirm"));
        a.setHeaderText(null);
        Optional<ButtonType> result = a.showAndWait();
        return result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE;
    }

    // ── Text input ────────────────────────────────────────────────────────

    /**
     * Shows a localized text-input dialog.
     * The result converter of TextInputDialog uses ButtonData.OK_DONE,
     * so replacing button types with custom ones still returns the entered text.
     */
    public static Optional<String> input(String prompt) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle(lm().get("dialog.title.input"));
        d.setHeaderText(prompt);
        d.setContentText(null);
        d.getDialogPane().getButtonTypes().setAll(
                new ButtonType(lm().get("dialog.button.ok"),     ButtonBar.ButtonData.OK_DONE),
                new ButtonType(lm().get("dialog.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        return d.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static void localizeButtons(Alert alert) {
        for (ButtonType bt : alert.getButtonTypes()) {
            // Replace default English "OK" button label with locale-aware text
            if (bt.getButtonData() == ButtonBar.ButtonData.OK_DONE
                    || bt == ButtonType.OK) {
                ((Button) alert.getDialogPane().lookupButton(bt))
                        .setText(lm().get("dialog.button.ok"));
            }
        }
    }
}
