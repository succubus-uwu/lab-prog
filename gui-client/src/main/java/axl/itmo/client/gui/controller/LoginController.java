package axl.itmo.client.gui.controller;

import axl.itmo.client.gui.GUIClientApp;
import axl.itmo.client.gui.network.NetworkService;
import axl.itmo.client.gui.util.LocaleManager;
import axl.itmo.common.dto.CommandResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Locale;

// FlowLayout, BorderLayout, GridLayout, BoxLayout, CardLayout, GroupLayout

public class LoginController {

    @FXML private Label    titleLabel;
    @FXML private Label    hostLabel;
    @FXML private Label    portLabel;
    @FXML private Label    loginLabel;
    @FXML private Label    passwordLabel;
    @FXML private Label    localeLabelNode;
    @FXML private Label    errorLabel;
    @FXML private TextField    hostField;
    @FXML private TextField    portField;
    @FXML private TextField    loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button   loginButton;
    @FXML private Button   registerButton;
    @FXML private ComboBox<String> localeCombo;

    private final LocaleManager lm = LocaleManager.getInstance();

    @FXML
    public void initialize() {
        bindLabels();
        lm.localeProperty().addListener((obs, old, loc) -> bindLabels());

        // Populate locale combo
        for (Locale locale : LocaleManager.SUPPORTED) {
            localeCombo.getItems().add(lm.localeDisplayName(locale));
        }
        localeCombo.getSelectionModel().select(0);
        localeCombo.setOnAction(e -> {
            int idx = localeCombo.getSelectionModel().getSelectedIndex();
            if (idx >= 0) lm.setLocale(LocaleManager.SUPPORTED.get(idx));
        });
    }

    private void bindLabels() {
        titleLabel.textProperty().bind(lm.binding("login.title"));
        hostLabel.textProperty().bind(lm.binding("login.server.host"));
        portLabel.textProperty().bind(lm.binding("login.server.port"));
        loginLabel.textProperty().bind(lm.binding("login.login"));
        passwordLabel.textProperty().bind(lm.binding("login.password"));
        loginButton.textProperty().bind(lm.binding("login.button.login"));
        registerButton.textProperty().bind(lm.binding("login.button.register"));
        localeLabelNode.textProperty().bind(lm.binding("locale.label"));
    }

    @FXML
    private void onLogin() {
        doAuth("login");
    }

    @FXML
    private void onRegister() {
        doAuth("register");
    }

    private void doAuth(String command) {
        String login    = loginField.getText().trim();
        String password = passwordField.getText();
        String host     = hostField.getText().trim();
        String portStr  = portField.getText().trim();

        if (login.isEmpty() || password.isEmpty()) {
            showError(lm.get("login.error.empty"));
            return;
        }

        int port;
        try { port = Integer.parseInt(portStr); }
        catch (NumberFormatException e) { showError("Invalid port"); return; }

        loginButton.setDisable(true);
        registerButton.setDisable(true);
        hideError();

        NetworkService net = new NetworkService(host, port);
        net.setCredentials(login, password);

        final String pw = password;
        net.executeAsync(command, null,
            response -> Platform.runLater(() -> {
                loginButton.setDisable(false);
                registerButton.setDisable(false);
                if (response.isSuccess()) {
                    // Message format: "Login successful:42" or "User registered successfully:42"
                    String msg = response.getMessage();
                    try {
                        int colon = msg.lastIndexOf(':');
                        if (colon >= 0) GUIClientApp.setCurrentUserId(Long.parseLong(msg.substring(colon + 1).trim()));
                    } catch (Exception ignored) {}
                    GUIClientApp.storePassword(pw);
                    GUIClientApp.openMainWindow(net, login, host, port);
                } else {
                    showError(response.getMessage());
                }
            }),
            ex -> Platform.runLater(() -> {
                loginButton.setDisable(false);
                registerButton.setDisable(false);
                showError(lm.get("error.network") + ": " + ex.getMessage());
            })
        );
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }
}
