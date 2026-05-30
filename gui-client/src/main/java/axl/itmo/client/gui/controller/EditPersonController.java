package axl.itmo.client.gui.controller;

import axl.itmo.client.gui.network.NetworkService;
import axl.itmo.client.gui.util.LocaleManager;
import axl.itmo.common.model.Color;
import axl.itmo.common.model.Coordinates;
import axl.itmo.common.model.Location;
import axl.itmo.common.model.Person;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class EditPersonController {

    @FXML private Label        titleLabel;
    @FXML private Label        nameLabel;
    @FXML private Label        coordXLabel;
    @FXML private Label        coordYLabel;
    @FXML private Label        heightLabel;
    @FXML private Label        birthdayLabel;
    @FXML private Label        passportLabel;
    @FXML private Label        eyeColorLabel;
    @FXML private Label        locNameLabel;
    @FXML private Label        locXLabel;
    @FXML private Label        locYLabel;
    @FXML private Label        locZLabel;
    @FXML private Label        validationLabel;
    @FXML private TextField    nameField;
    @FXML private TextField    coordXField;
    @FXML private TextField    coordYField;
    @FXML private TextField    heightField;
    @FXML private DatePicker   birthdayPicker;
    @FXML private TextField    passportField;
    @FXML private ComboBox<Color> eyeColorCombo;
    @FXML private TextField    locNameField;
    @FXML private TextField    locXField;
    @FXML private TextField    locYField;
    @FXML private TextField    locZField;
    @FXML private Button       saveButton;
    @FXML private Button       cancelButton;

    private Person         editing;
    private String         currentLogin;
    private NetworkService network;
    private Stage          stage;
    private ObservableList<Person> allPersons;
    private final LocaleManager lm = LocaleManager.getInstance();

    @FXML
    public void initialize() {
        eyeColorCombo.getItems().addAll(Color.values());
        eyeColorCombo.getSelectionModel().selectFirst();
        bindLabels();
        lm.localeProperty().addListener((obs, old, loc) -> bindLabels());
    }

    private void bindLabels() {
        saveButton  .textProperty().bind(lm.binding("edit.button.save"));
        cancelButton.textProperty().bind(lm.binding("edit.button.cancel"));
        nameLabel   .textProperty().bind(lm.binding("edit.name"));
        coordXLabel .textProperty().bind(lm.binding("edit.coord.x"));
        coordYLabel .textProperty().bind(lm.binding("edit.coord.y"));
        heightLabel .textProperty().bind(lm.binding("edit.height"));
        birthdayLabel.textProperty().bind(lm.binding("edit.birthday"));
        passportLabel.textProperty().bind(lm.binding("edit.passport"));
        eyeColorLabel.textProperty().bind(lm.binding("edit.eye.color"));
        locNameLabel .textProperty().bind(lm.binding("edit.loc.name"));
        locXLabel    .textProperty().bind(lm.binding("edit.loc.x"));
        locYLabel    .textProperty().bind(lm.binding("edit.loc.y"));
        locZLabel    .textProperty().bind(lm.binding("edit.loc.z"));
    }

    public void initData(Person person, String login, NetworkService network,
                         Stage stage, ObservableList<Person> allPersons) {
        this.editing     = person;
        this.currentLogin = login;
        this.network     = network;
        this.stage       = stage;
        this.allPersons  = allPersons;

        boolean isEdit = person != null;
        titleLabel.textProperty().bind(lm.binding(isEdit ? "edit.title.edit" : "edit.title.add"));

        if (isEdit) populate(person);
    }

    private void populate(Person p) {
        nameField.setText(p.getName());
        if (p.getCoordinates() != null) {
            coordXField.setText(String.valueOf(p.getCoordinates().getX()));
            coordYField.setText(String.valueOf(p.getCoordinates().getY()));
        }
        heightField.setText(String.valueOf(p.getHeight()));
        if (p.getBirthday() != null) {
            birthdayPicker.setValue(p.getBirthday().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());
        }
        passportField.setText(p.getPassportID() != null ? p.getPassportID() : "");
        if (p.getEyeColor() != null) eyeColorCombo.setValue(p.getEyeColor());
        if (p.getLocation() != null) {
            locNameField.setText(p.getLocation().getName() != null ? p.getLocation().getName() : "");
            locXField.setText(String.valueOf(p.getLocation().getX()));
            locYField.setText(String.valueOf(p.getLocation().getY()));
            locZField.setText(String.valueOf(p.getLocation().getZ()));
        }
    }

    @FXML
    private void onSave() {
        Person p = buildPerson();
        if (p == null) return;

        String cmd = (editing != null) ? "update" : "add";
        network.executeAsync(cmd, p,
            r -> Platform.runLater(() -> {
                if (r.isSuccess()) {
                    stage.close();
                } else {
                    showValidation(r.getMessage());
                }
            }),
            ex -> Platform.runLater(() -> showValidation(lm.get("error.network") + ": " + ex.getMessage()))
        );
    }

    @FXML
    private void onCancel() {
        stage.close();
    }

    private Person buildPerson() {
        // ── Name ──────────────────────────────────────────────────────────
        String name = nameField.getText().trim();
        if (name.isEmpty()) { showValidation(lm.get("edit.validation.name")); return null; }

        // ── Coordinates ───────────────────────────────────────────────────
        double coordX;
        long   coordY;
        try { coordX = Double.parseDouble(coordXField.getText().trim()); }
        catch (NumberFormatException e) { showValidation(lm.get("edit.validation.coord.x")); return null; }
        if (coordX > 862) { showValidation(lm.get("edit.validation.coord.x")); return null; }
        try { coordY = Long.parseLong(coordYField.getText().trim()); }
        catch (NumberFormatException e) { showValidation(lm.get("edit.validation.coord.y")); return null; }

        // ── Height ────────────────────────────────────────────────────────
        float height;
        try { height = Float.parseFloat(heightField.getText().trim()); }
        catch (NumberFormatException e) { showValidation(lm.get("edit.validation.height")); return null; }
        if (height <= 0) { showValidation(lm.get("edit.validation.height")); return null; }

        // ── Birthday ──────────────────────────────────────────────────────
        Date birthday = null;
        LocalDate ld = birthdayPicker.getValue();
        if (ld != null) birthday = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // ── Passport ─────────────────────────────────────────────────────
        String passport = passportField.getText().trim();
        if (passport.isEmpty()) passport = null;

        // ── Eye color ─────────────────────────────────────────────────────
        Color eyeColor = eyeColorCombo.getValue();

        // ── Location ──────────────────────────────────────────────────────
        Location location = null;
        String locName = locNameField.getText().trim();
        if (!locName.isEmpty() || !locXField.getText().isBlank()) {
            try {
                double lx = locXField.getText().isBlank() ? 0 : Double.parseDouble(locXField.getText().trim());
                long   ly = locYField.getText().isBlank() ? 0 : Long.parseLong(locYField.getText().trim());
                double lz = locZField.getText().isBlank() ? 0 : Double.parseDouble(locZField.getText().trim());
                location = new Location();
                location.setName(locName.isEmpty() ? null : locName);
                location.setX(lx); location.setY(ly); location.setZ(lz);
            } catch (NumberFormatException e) {
                showValidation("Invalid location coordinates");
                return null;
            }
        }

        // ── Build Person ──────────────────────────────────────────────────
        Person p = new Person(
                editing != null ? editing.getId() : 0,
                name, new Coordinates(coordX, coordY), null,
                height, birthday, passport, eyeColor, location);
        return p;
    }

    private void showValidation(String msg) {
        validationLabel.setText(msg);
        validationLabel.setVisible(true);
    }
}
