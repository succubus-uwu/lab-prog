package axl.itmo.client.gui.controller;

import axl.itmo.client.gui.GUIClientApp;
import axl.itmo.client.gui.network.NetworkService;
import axl.itmo.client.gui.network.PushListener;
import axl.itmo.client.gui.render.CollectionCanvas;
import axl.itmo.client.gui.util.Dialogs;
import axl.itmo.client.gui.util.LocaleManager;
import axl.itmo.common.dto.CommandResponse;
import axl.itmo.common.model.Person;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {

    // ── Toolbar ───────────────────────────────────────────────────────────
    @FXML private Label     appTitleLabel;
    @FXML private Label     currentUserLabel;
    @FXML private Label     localeLabelNode;
    @FXML private ComboBox<String> localeCombo;
    @FXML private Button    logoutButton;

    // ── Filter / sort bar ─────────────────────────────────────────────────
    @FXML private Label     filterColLabel;
    @FXML private Label     filterValLabel;
    @FXML private Label     sortColLabel;
    @FXML private ComboBox<String> filterColCombo;
    @FXML private TextField filterField;
    @FXML private ComboBox<String> sortColCombo;
    @FXML private ToggleButton sortDirButton;
    @FXML private Button    applyButton;
    @FXML private Button    clearFilterButton;
    @FXML private Button    editSelButton;
    @FXML private Button    deleteSelButton;

    // ── Table ─────────────────────────────────────────────────────────────
    @FXML private TableView<Person> tableView;
    @FXML private TableColumn<Person, String> colId;
    @FXML private TableColumn<Person, String> colName;
    @FXML private TableColumn<Person, String> colCoordX;
    @FXML private TableColumn<Person, String> colCoordY;
    @FXML private TableColumn<Person, String> colHeight;
    @FXML private TableColumn<Person, String> colBirthday;
    @FXML private TableColumn<Person, String> colPassport;
    @FXML private TableColumn<Person, String> colEyeColor;
    @FXML private TableColumn<Person, String> colLocName;
    @FXML private TableColumn<Person, String> colLocX;
    @FXML private TableColumn<Person, String> colLocY;
    @FXML private TableColumn<Person, String> colLocZ;
    @FXML private TableColumn<Person, String> colCreated;
    @FXML private TableColumn<Person, String> colOwner;

    // ── Canvas area ───────────────────────────────────────────────────────
    @FXML private Label  canvasTitle;
    @FXML private Pane   canvasPane;

    // ── Commands panel labels ─────────────────────────────────────────────
    @FXML private Label  cmdTitleLabel;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnRemove;
    @FXML private Button btnClear;
    @FXML private Button btnShow;
    @FXML private Button btnInfo;
    @FXML private Button btnRemoveFirst;
    @FXML private Button btnReorder;
    @FXML private Button btnCountHeight;
    @FXML private Button btnCountPassport;
    @FXML private Button btnPrintPassport;
    @FXML private Button btnScript;
    @FXML private Button btnHistory;
    @FXML private Button btnHelp;
    @FXML private Button btnSave;

    // ── Status bar ────────────────────────────────────────────────────────
    @FXML private Label statusLabel;
    @FXML private Label pushStatusLabel;

    // ── State ─────────────────────────────────────────────────────────────
    private NetworkService network;
    private String currentUser;
    private String host;
    private int    port;
    private PushListener pushListener;
    private final ObservableList<Person> allPersons     = FXCollections.observableArrayList();
    private final ObservableList<Person> displayPersons = FXCollections.observableArrayList();
    private final List<String>  commandHistory = new ArrayList<>();
    private CollectionCanvas    canvas;
    private final LocaleManager lm = LocaleManager.getInstance();

    private static final List<String> COL_KEYS = List.of(
            "col.id","col.name","col.coord.x","col.coord.y","col.height",
            "col.birthday","col.passport","col.eye.color","col.loc.name",
            "col.loc.x","col.loc.y","col.loc.z","col.created","col.owner");

    // ── Initialisation ────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupCanvas();
        setupTable();
        setupLocaleCombo();
        bindAllLabels();
        lm.localeProperty().addListener((obs, old, loc) -> {
            bindAllLabels();
            rebuildFilterCombos();
            tableView.refresh();
        });
        tableView.setItems(displayPersons);
    }

    public void initData(NetworkService network, String login, String host, int port) {
        this.network     = network;
        this.currentUser = login;
        this.host        = host;
        this.port        = port;

        currentUserLabel.textProperty().unbind();
        currentUserLabel.setText(MessageFormat.format(lm.get("main.user"), login));

        String connText = MessageFormat.format(lm.get("main.status.connected"), host, port);
        statusLabel.textProperty().unbind();
        statusLabel.setText(connText);

        startPushListener();
        refreshCollection(); // silent initial load
    }

    // ── Canvas ────────────────────────────────────────────────────────────

    private void setupCanvas() {
        canvas = new CollectionCanvas(900, 480);
        canvas.setClickCallback(this::onCanvasPersonClick);
        canvasPane.getChildren().add(canvas);
    }

    private void onCanvasPersonClick(Person p) {
        openPersonInfo(p);
    }

    // ── Table ─────────────────────────────────────────────────────────────

    private void setupTable() {
        colId      .setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getId())));
        colName    .setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        colCoordX  .setCellValueFactory(cd -> {
            var c = cd.getValue().getCoordinates();
            return new SimpleStringProperty(c != null ? lm.formatNumber(c.getX()) : "");
        });
        colCoordY  .setCellValueFactory(cd -> {
            var c = cd.getValue().getCoordinates();
            return new SimpleStringProperty(c != null ? String.valueOf(c.getY()) : "");
        });
        colHeight  .setCellValueFactory(cd -> new SimpleStringProperty(lm.formatNumber(cd.getValue().getHeight())));
        colBirthday.setCellValueFactory(cd -> new SimpleStringProperty(lm.formatDate(cd.getValue().getBirthday())));
        colPassport.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getPassportID() != null ? cd.getValue().getPassportID() : ""));
        colEyeColor.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getEyeColor() != null ? cd.getValue().getEyeColor().name() : ""));
        colLocName .setCellValueFactory(cd -> {
            var l = cd.getValue().getLocation();
            return new SimpleStringProperty(l != null && l.getName() != null ? l.getName() : "");
        });
        colLocX    .setCellValueFactory(cd -> {
            var l = cd.getValue().getLocation();
            return new SimpleStringProperty(l != null ? lm.formatNumber(l.getX()) : "");
        });
        colLocY    .setCellValueFactory(cd -> {
            var l = cd.getValue().getLocation();
            return new SimpleStringProperty(l != null ? String.valueOf(l.getY()) : "");
        });
        colLocZ    .setCellValueFactory(cd -> {
            var l = cd.getValue().getLocation();
            return new SimpleStringProperty(l != null ? lm.formatNumber(l.getZ()) : "");
        });
        colCreated .setCellValueFactory(cd -> new SimpleStringProperty(lm.formatDateTime(cd.getValue().getCreationDate())));
        colOwner   .setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getOwnerId())));

        tableView.setRowFactory(tv -> {
            TableRow<Person> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) openEditDialog(row.getItem());
            });
            return row;
        });
    }

    private void rebuildFilterCombos() {
        List<String> colNames = COL_KEYS.stream().map(lm::get).collect(Collectors.toList());
        int selFil = filterColCombo.getSelectionModel().getSelectedIndex();
        int selSrt = sortColCombo.getSelectionModel().getSelectedIndex();
        filterColCombo.getItems().setAll(colNames);
        sortColCombo  .getItems().setAll(colNames);
        filterColCombo.getSelectionModel().select(Math.max(selFil, 0));
        sortColCombo  .getSelectionModel().select(Math.max(selSrt, 0));
    }

    // ── Locale combo ──────────────────────────────────────────────────────

    private void setupLocaleCombo() {
        for (Locale loc : LocaleManager.SUPPORTED) {
            localeCombo.getItems().add(lm.localeDisplayName(loc));
        }
        localeCombo.getSelectionModel().select(0);
        localeCombo.setOnAction(e -> {
            int idx = localeCombo.getSelectionModel().getSelectedIndex();
            if (idx >= 0) lm.setLocale(LocaleManager.SUPPORTED.get(idx));
        });
        rebuildFilterCombos();
    }

    // ── Label bindings ────────────────────────────────────────────────────

    private void bindAllLabels() {
        appTitleLabel.textProperty().bind(lm.binding("main.title"));
        localeLabelNode.textProperty().bind(lm.binding("locale.label"));
        logoutButton.textProperty().bind(lm.binding("main.logout"));

        filterColLabel.textProperty().bind(lm.binding("main.filter.column"));
        filterValLabel.textProperty().bind(lm.binding("main.filter.value"));
        sortColLabel.textProperty().bind(lm.binding("main.sort.column"));
        applyButton.textProperty().bind(lm.binding("main.button.apply"));
        clearFilterButton.textProperty().bind(lm.binding("main.button.clear"));
        editSelButton.textProperty().bind(lm.binding("main.button.edit.selected"));
        deleteSelButton.textProperty().bind(lm.binding("main.button.delete.selected"));
        sortDirButton.textProperty().bind(
                sortDirButton.isSelected() ? lm.binding("main.sort.desc") : lm.binding("main.sort.asc"));

        canvasTitle.textProperty().bind(lm.binding("main.canvas.title"));
        cmdTitleLabel.textProperty().bind(lm.binding("main.commands.title"));

        btnAdd         .textProperty().bind(lm.binding("cmd.add"));
        btnUpdate      .textProperty().bind(lm.binding("cmd.update"));
        btnRemove      .textProperty().bind(lm.binding("cmd.remove"));
        btnClear       .textProperty().bind(lm.binding("cmd.clear"));
        btnShow        .textProperty().bind(lm.binding("cmd.show"));
        btnInfo        .textProperty().bind(lm.binding("cmd.info"));
        btnRemoveFirst .textProperty().bind(lm.binding("cmd.remove.first"));
        btnReorder     .textProperty().bind(lm.binding("cmd.reorder"));
        btnCountHeight .textProperty().bind(lm.binding("cmd.count.height"));
        btnCountPassport.textProperty().bind(lm.binding("cmd.count.passport"));
        btnPrintPassport.textProperty().bind(lm.binding("cmd.print.passport"));
        btnScript      .textProperty().bind(lm.binding("cmd.script"));
        btnHistory     .textProperty().bind(lm.binding("cmd.history"));
        btnHelp        .textProperty().bind(lm.binding("cmd.help"));
        btnSave        .textProperty().bind(lm.binding("cmd.save"));

        // Column headers
        List<TableColumn<Person,String>> cols = List.of(colId,colName,colCoordX,colCoordY,colHeight,
                colBirthday,colPassport,colEyeColor,colLocName,colLocX,colLocY,colLocZ,colCreated,colOwner);
        for (int i = 0; i < cols.size(); i++) {
            cols.get(i).textProperty().bind(lm.binding(COL_KEYS.get(i)));
        }
    }

    // ── Collection management ─────────────────────────────────────────────

    private void updateCollection(List<Person> persons) {
        allPersons.setAll(persons);
        applyCurrentFilter();
        canvas.update(persons);
    }

    private void applyCurrentFilter() {
        String filterCol   = filterColCombo.getSelectionModel().getSelectedItem();
        String filterVal   = filterField.getText().trim().toLowerCase();
        String sortCol     = sortColCombo.getSelectionModel().getSelectedItem();
        boolean descending = sortDirButton.isSelected();

        List<String> colNames = COL_KEYS.stream().map(lm::get).collect(Collectors.toList());
        int filterIdx = colNames.indexOf(filterCol);
        int sortIdx   = colNames.indexOf(sortCol);

        // ── Streams API: filter then sort ────────────────────────────────
        var stream = allPersons.stream();

        if (!filterVal.isEmpty() && filterIdx >= 0) {
            final int fi = filterIdx;
            stream = stream.filter(p -> getFieldString(p, fi).toLowerCase().contains(filterVal));
        }

        if (sortIdx >= 0) {
            final int si = sortIdx;
            Comparator<Person> cmp = Comparator.comparing(p -> getFieldString(p, si));
            if (descending) cmp = cmp.reversed();
            stream = stream.sorted(cmp);
        }

        displayPersons.setAll(stream.collect(Collectors.toList()));
    }

    private String getFieldString(Person p, int colIndex) {
        return switch (colIndex) {
            case 0  -> String.valueOf(p.getId());
            case 1  -> p.getName() != null ? p.getName() : "";
            case 2  -> p.getCoordinates() != null ? String.valueOf(p.getCoordinates().getX()) : "";
            case 3  -> p.getCoordinates() != null ? String.valueOf(p.getCoordinates().getY()) : "";
            case 4  -> String.valueOf(p.getHeight());
            case 5  -> p.getBirthday() != null ? p.getBirthday().toString() : "";
            case 6  -> p.getPassportID() != null ? p.getPassportID() : "";
            case 7  -> p.getEyeColor() != null ? p.getEyeColor().name() : "";
            case 8  -> p.getLocation() != null && p.getLocation().getName() != null ? p.getLocation().getName() : "";
            case 9  -> p.getLocation() != null ? String.valueOf(p.getLocation().getX()) : "";
            case 10 -> p.getLocation() != null ? String.valueOf(p.getLocation().getY()) : "";
            case 11 -> p.getLocation() != null ? String.valueOf(p.getLocation().getZ()) : "";
            case 12 -> p.getCreationDate() != null ? p.getCreationDate().toString() : "";
            case 13 -> String.valueOf(p.getOwnerId());
            default -> "";
        };
    }

    // ── Push listener ─────────────────────────────────────────────────────

    private void startPushListener() {
        pushListener = new PushListener(host, port,
                network.getLogin(), getPassword(),
                this::updateCollection,
                msg -> pushStatusLabel.setText(msg));
        pushListener.start();
        pushStatusLabel.setText("Push: active");
    }

    private String getPassword() {
        // We need to store password temporarily for push listener.
        // It's available via NetworkService which keeps it internally.
        // We access it via reflection-free approach: store in GUIClientApp.
        return GUIClientApp.getPassword();
    }

    // ── Filter / sort actions ─────────────────────────────────────────────

    @FXML private void onApplyFilter()  { applyCurrentFilter(); }

    @FXML private void onClearFilter() {
        filterField.clear();
        filterColCombo.getSelectionModel().selectFirst();
        sortColCombo  .getSelectionModel().selectFirst();
        sortDirButton.setSelected(false);
        applyCurrentFilter();
    }

    @FXML private void onToggleSortDir() {
        sortDirButton.textProperty().unbind();
        sortDirButton.textProperty().bind(
                sortDirButton.isSelected() ? lm.binding("main.sort.desc") : lm.binding("main.sort.asc"));
    }

    // ── Edit / delete from table ──────────────────────────────────────────

    @FXML private void onEditSelected() {
        Person sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showInfo(lm.get("error.select.first")); return; }
        openEditDialog(sel);
    }

    @FXML private void onDeleteSelected() {
        Person sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showInfo(lm.get("error.select.first")); return; }
        if (sel.getOwnerId() != resolveCurrentUserId()) { showInfo(lm.get("error.not.owner")); return; }
        confirmAndDelete(sel.getId());
    }

    // ── Commands ──────────────────────────────────────────────────────────

    @FXML private void onLogout() {
        if (pushListener != null) pushListener.stop();
        if (canvas != null) canvas.stopTimer();
        network.disconnect();
        GUIClientApp.showLogin();
    }

    @FXML private void cmdAdd() {
        openEditDialog(null);
    }

    @FXML private void cmdUpdate() {
        Person sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showInfo(lm.get("error.select.first")); return; }
        openEditDialog(sel);
    }

    @FXML private void cmdRemove() { onDeleteSelected(); }

    @FXML private void cmdClear() {
        if (Dialogs.confirm(lm.get("dialog.confirm.clear"))) {
            execAsync("clear", null, this::showResult);
        }
    }

    private void refreshCollection() {
        network.executeAsync("show", null,
            r -> Platform.runLater(() -> {
                if (r.isSuccess() && r.getData() != null) updateCollection(r.getData());
            }),
            ex -> {}); // silent
    }

    @FXML private void cmdShow() {
        execAsync("show", null, r -> {
            if (r.isSuccess() && r.getData() != null) updateCollection(r.getData());
            // Show count in status bar instead of alert
            String count = r.getData() != null ? String.valueOf(r.getData().size()) : "0";
            statusLabel.setText(MessageFormat.format(lm.get("main.status.connected"), host, port)
                    + "  |  " + count + " " + lm.get("main.status.records"));
        });
    }

    @FXML private void cmdInfo()         { execAsync("info",   null, this::showResult); }
    @FXML private void cmdRemoveFirst()  { execAsync("remove_first", null, this::showResult); }
    @FXML private void cmdReorder()      { execAsync("reorder", null, this::showResult); }
    @FXML private void cmdPrintPassport(){ execAsync("print_field_descending_passport_id", null, this::showResult); }
    @FXML private void cmdHelp()         { execAsync("help", null, this::showResult); }
    @FXML private void cmdSave()         { execAsync("save", null, this::showResult); }

    @FXML private void cmdHistory() {
        if (commandHistory.isEmpty()) { showInfo(lm.get("dialog.history.empty")); return; }
        showInfo(String.join("\n", commandHistory));
    }

    @FXML private void cmdCountHeight() {
        Dialogs.input(lm.get("dialog.height.prompt")).ifPresent(val -> {
            try { execAsync("count_less_than_height", Float.parseFloat(val), this::showResult); }
            catch (NumberFormatException e) { Dialogs.error(lm.get("edit.validation.height")); }
        });
    }

    @FXML private void cmdCountPassport() {
        Dialogs.input(lm.get("dialog.passport.prompt"))
               .ifPresent(val -> execAsync("count_greater_than_passport_id", val, this::showResult));
    }

    @FXML private void cmdScript() {
        Dialogs.input(lm.get("dialog.script.prompt"))
               .ifPresent(path -> execAsync("execute_script", path, this::showResult));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void execAsync(String cmd, Object arg, java.util.function.Consumer<CommandResponse> cb) {
        commandHistory.add(cmd);
        network.executeAsync(cmd, arg,
            r  -> Platform.runLater(() -> cb.accept(r)),
            ex -> Platform.runLater(() -> showError(lm.get("error.network") + ": " + ex.getMessage())));
    }

    private void showResult(CommandResponse r) {
        if (r.isSuccess()) Dialogs.info(r.getMessage());
        else               Dialogs.error(r.getMessage());
    }

    private void showInfo(String msg)  { Dialogs.info(msg); }
    private void showError(String msg) { Dialogs.error(msg); }

    private void confirmAndDelete(int id) {
        if (Dialogs.confirm(lm.get("dialog.confirm.delete"))) {
            execAsync("remove_by_id", id, this::showResult);
        }
    }

    private long resolveCurrentUserId() {
        // Find first person owned by current user (by login match is not possible here,
        // so we check allPersons for any person whose owner matches ownerId stored in app).
        return GUIClientApp.getCurrentUserId();
    }

    void openEditDialog(Person existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/axl/itmo/client/gui/fxml/edit_person.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            GUIClientApp.applyStylesheet(stage.getScene());
            EditPersonController ctrl = loader.getController();
            ctrl.initData(existing, currentUser, network, stage, allPersons);
            stage.showAndWait();
        } catch (IOException e) {
            showError("Failed to open edit dialog: " + e.getMessage());
        }
    }

    private void openPersonInfo(Person p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/axl/itmo/client/gui/fxml/person_info.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            GUIClientApp.applyStylesheet(stage.getScene());
            PersonInfoController ctrl = loader.getController();
            ctrl.initData(p, currentUser, network, stage, allPersons, this);
            stage.show();
        } catch (IOException e) {
            showError("Failed to open info: " + e.getMessage());
        }
    }
}
