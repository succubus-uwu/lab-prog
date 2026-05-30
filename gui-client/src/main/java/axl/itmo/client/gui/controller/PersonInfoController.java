package axl.itmo.client.gui.controller;

import axl.itmo.client.gui.network.NetworkService;
import axl.itmo.client.gui.util.Dialogs;
import axl.itmo.client.gui.util.LocaleManager;
import axl.itmo.common.model.Location;
import axl.itmo.common.model.Person;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PersonInfoController {

    @FXML private Label titleLabel;
    @FXML private Label idKey;
    @FXML private Label idVal;
    @FXML private Label nameKey;
    @FXML private Label nameVal;
    @FXML private Label heightKey;
    @FXML private Label heightVal;
    @FXML private Label birthdayKey;
    @FXML private Label birthdayVal;
    @FXML private Label passportKey;
    @FXML private Label passportVal;
    @FXML private Label eyeKey;
    @FXML private Label eyeVal;
    @FXML private Label locKey;
    @FXML private Label locVal;
    @FXML private Label createdKey;
    @FXML private Label createdVal;
    @FXML private Label ownerKey;
    @FXML private Label ownerVal;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;

    private Person         person;
    private NetworkService network;
    private Stage          stage;
    private ObservableList<Person> allPersons;
    private MainController mainCtrl;
    private final LocaleManager lm = LocaleManager.getInstance();

    @FXML
    public void initialize() {
        bindLabels();
        lm.localeProperty().addListener((obs, old, loc) -> {
            bindLabels();
            if (person != null) fillValues();
        });
    }

    private void bindLabels() {
        titleLabel .textProperty().bind(lm.binding("info.title"));
        idKey      .textProperty().bind(lm.binding("info.id"));
        nameKey    .textProperty().bind(lm.binding("info.name"));
        heightKey  .textProperty().bind(lm.binding("info.height"));
        birthdayKey.textProperty().bind(lm.binding("info.birthday"));
        passportKey.textProperty().bind(lm.binding("info.passport"));
        eyeKey     .textProperty().bind(lm.binding("info.eye.color"));
        locKey     .textProperty().bind(lm.binding("info.location"));
        createdKey .textProperty().bind(lm.binding("info.created"));
        ownerKey   .textProperty().bind(lm.binding("info.owner"));
        editButton  .textProperty().bind(lm.binding("info.button.edit"));
        deleteButton.textProperty().bind(lm.binding("info.button.delete"));
        closeButton .textProperty().bind(lm.binding("info.button.close"));
    }

    public void initData(Person p, String currentLogin, NetworkService network,
                         Stage stage, ObservableList<Person> allPersons,
                         MainController mainCtrl) {
        this.person      = p;
        this.network     = network;
        this.stage       = stage;
        this.allPersons  = allPersons;
        this.mainCtrl    = mainCtrl;

        fillValues();

        boolean isOwner = p.getOwnerId() == axl.itmo.client.gui.GUIClientApp.getCurrentUserId();
        editButton  .setVisible(isOwner);
        deleteButton.setVisible(isOwner);
    }

    private void fillValues() {
        if (person == null) return;
        idVal      .setText(String.valueOf(person.getId()));
        nameVal    .setText(person.getName());
        heightVal  .setText(lm.formatNumber(person.getHeight()));
        birthdayVal.setText(lm.formatDate(person.getBirthday()));
        passportVal.setText(person.getPassportID() != null ? person.getPassportID() : "—");
        eyeVal     .setText(person.getEyeColor() != null ? person.getEyeColor().name() : "—");
        ownerVal   .setText(String.valueOf(person.getOwnerId()));
        createdVal .setText(lm.formatDateTime(person.getCreationDate()));

        Location loc = person.getLocation();
        if (loc != null) {
            String locStr = (loc.getName() != null ? loc.getName() + " " : "")
                    + "(" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")";
            locVal.setText(locStr);
        } else {
            locVal.setText("—");
        }
    }

    @FXML private void onEdit() {
        stage.close();
        mainCtrl.openEditDialog(person);
    }

    @FXML private void onDelete() {
        if (!Dialogs.confirm(lm.get("dialog.confirm.delete"))) return;
        network.executeAsync("remove_by_id", person.getId(),
            r -> Platform.runLater(() -> {
                if (r.isSuccess()) stage.close();
                else Dialogs.error(r.getMessage());
            }),
            ex -> Platform.runLater(() -> Dialogs.error(ex.getMessage())));
    }

    @FXML private void onClose() { stage.close(); }

    private void showError(String msg) { Dialogs.error(msg); }
}
