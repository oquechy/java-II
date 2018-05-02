package ru.spbau.mit.oquechy.ftp.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ru.spbau.mit.oquechy.ftp.types.FileInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GUIController {

    private final static String SERVER_INIT_DIR = ".";

    private final static String FILE_NOT_CHOSEN = "The destination wasn't selected. Download cancelled.";
    private final static String DIRECTORY_CHOSEN = "The destination file can't be a directory. Choose regular file.";
    private final static String DOWNLOAD_TIP = "Now you can choose the file " +
            "and click the button on the right to download it.";
    @FXML
    public TextField hostname;
    @FXML
    public GridPane grid;
    @FXML
    public Label tip;
    @FXML
    public Button listButton;
    @FXML
    private TreeView<FileInfo> treeView;
    private FTPClient ftpClient;

    public void savingFile() {
        String src = getPath(treeView.getSelectionModel().getSelectedItem());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        Window window = treeView.getScene().getWindow();
        File dst = fileChooser.showSaveDialog(window);
        if (dst == null) {
            showChooseFileAlert(Alert.AlertType.WARNING, FILE_NOT_CHOSEN);
        } else if (dst.isDirectory()) {
            showChooseFileAlert(Alert.AlertType.ERROR, DIRECTORY_CHOSEN);
        } else {
            try {
                ftpClient.get(src, dst.getAbsolutePath());
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                Label label = new Label("File \"" + src + "\"\n was saved to \"" + dst + "\"!");
                label.setWrapText(true);
                alert.getDialogPane().setContent(label);
                alert.setTitle("Saving");
                alert.setHeaderText(null);
                alert.showAndWait();
            } catch (IOException e) {
                showIOError();
                finish();
            }
        }
    }

    private String getPath(TreeItem<FileInfo> selectedItem) {
        StringBuilder stringBuilder = new StringBuilder();
        for (; selectedItem.getParent() != null; selectedItem = selectedItem.getParent()) {
            stringBuilder.append(new StringBuilder(selectedItem.getValue().name).reverse().toString())
                    .append(File.separator);
        }
        return stringBuilder.append(selectedItem.getValue().name).reverse().toString();
    }

    private void showChooseFileAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Saving");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void hostnameReceived() {
        hostname.setDisable(true);
        listButton.setDisable(true);
        String host = hostname.getText();
        try {
            ftpClient = FTPClient.start(host);
            treeView.setRoot(new DynamicFileTreeItem());
            tip.setText(DOWNLOAD_TIP);
        } catch (IOException e) {
            showIOError();
            finish();
        }
    }

    public void closeConnection() {
        try {
            if (ftpClient != null) {
                ftpClient.close();
            }
        } catch (IOException e) {
            showIOError();
            finish();
        }
    }

    private void showIOError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Socket error");
        alert.setHeaderText(null);
        alert.setContentText("Can't reach the server. Application will be closed");
        alert.showAndWait();
    }

    private void finish() {
        if (ftpClient != null) {
            try {
                ftpClient.close();
            } catch (IOException ignored) { /*Error is already shown*/ }
        }
        treeView.getScene().getWindow().hide();
    }

    private class DynamicFileTreeItem extends TreeItem<FileInfo> {
        private boolean loaded = false;
        private String path;

        public DynamicFileTreeItem() {
            this(new FileInfo(SERVER_INIT_DIR, true), SERVER_INIT_DIR);
        }

        public DynamicFileTreeItem(FileInfo s, String path) {
            super(s);
            this.path = path;
        }

        @Override
        public ObservableList<TreeItem<FileInfo>> getChildren() {
            if (!loaded) {
                loaded = true;
                super.getChildren().setAll(buildChildren(this));
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            return !getValue().isDirectory;
        }

        private ObservableList<DynamicFileTreeItem> buildChildren(DynamicFileTreeItem treeItem) {
            FileInfo file = treeItem.getValue();
            if (file.isDirectory) {
                try {
                    List<FileInfo> files = ftpClient.list(path);
                    ObservableList<DynamicFileTreeItem> children = FXCollections.observableArrayList();

                    for (FileInfo childFile : files) {
                        children.add(new DynamicFileTreeItem(childFile, path + File.separator + childFile.name));
                    }

                    path = null;
                    return children;
                } catch (IOException e) {
                    showIOError();
                    finish();
                }
            }

            return FXCollections.emptyObservableList();
        }

    }
}
