package ru.spbau.mit.oquechy.ftp.client.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ru.spbau.mit.oquechy.ftp.client.FTPClient;
import ru.spbau.mit.oquechy.ftp.types.FileInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Implementation of handlers for UI elements.
 */
public class GUIController {

    private final static String SERVER_INIT_DIR = ".";

    private final static String SRC_NOT_CHOSEN = "File is not selected.";
    private final static String DST_NOT_CHOSEN = "The destination wasn't selected. Download cancelled.";
    private final static String DIRECTORY_CHOSEN = "The destination file can't be a directory. Choose regular file.";
    private final static String DOWNLOAD_STARTED = "Download was started in the background";
    private final static String DOWNLOAD_TIP = "Now you can choose the file " +
            "and click the button on the right to download it.";
    @FXML
    private TextField hostname;

    @FXML
    private Label tip;

    @FXML
    private Button listButton;

    @FXML
    private TreeView<FileInfo> treeView;

    private FTPClient ftpClient;

    @FXML
    private void savingFile() {
        TreeItem<FileInfo> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showChooseFileAlert(Alert.AlertType.WARNING, SRC_NOT_CHOSEN);
            return;
        }

        String src = getPath(selectedItem);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        Window window = treeView.getScene().getWindow();
        File dst = fileChooser.showSaveDialog(window);
        if (dst == null) {
            showChooseFileAlert(Alert.AlertType.WARNING, DST_NOT_CHOSEN);
        } else if (dst.isDirectory()) {
            showChooseFileAlert(Alert.AlertType.ERROR, DIRECTORY_CHOSEN);
        } else {
            showChooseFileAlert(Alert.AlertType.INFORMATION, DOWNLOAD_STARTED);
            Task task = new Task<Void>() {
                @Override
                public Void call() {
                    try {
                        ftpClient.get(src, dst.getAbsolutePath());
                    } catch (IOException e) {
                        showIOError();
                        finish();
                    }
                    return null;
                }
            };
            task.setOnSucceeded(event -> showSavingConfirmation(src, dst));
            new Thread(task).start();
        }
    }

    private void showSavingConfirmation(String src, File dst) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Label label = new Label("File \"" + src + "\"\n was saved to \"" + dst + "\"!");
        label.setWrapText(true);
        alert.getDialogPane().setContent(label);
        alert.setTitle("Saving");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private String getPath(TreeItem<FileInfo> selectedItem) {
        StringBuilder stringBuilder = new StringBuilder();
        for (; selectedItem.getParent() != null; selectedItem = selectedItem.getParent()) {
            stringBuilder.append(new StringBuilder(selectedItem.getValue().getName()).reverse().toString())
                    .append(File.separator);
        }
        return stringBuilder.append(selectedItem.getValue().getName()).reverse().toString();
    }

    private void showChooseFileAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Saving");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void hostnameReceived() {
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

    /**
     * Called to close the connection when session is over.
     */
    public void closeConnection() {
        try {
            if (ftpClient != null) {
                ftpClient.close();
            }
        } catch (IOException e) {
            showIOError();
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

    /**
     *  {@link TreeItem} for dynamic load of server's structure.
     */
    private class DynamicFileTreeItem extends TreeItem<FileInfo> {
        private boolean loaded = false;
        private String path;

        /**
         * Creates root node of the {@link TreeView}, which
         * stores {@link FileInfo} with path to server's running directory.
         */
        public DynamicFileTreeItem() {
            this(new FileInfo(SERVER_INIT_DIR, true), SERVER_INIT_DIR);
        }

        /**
         * Creates child node of structure.
         *
         * @param fileInfo which node links to
         * @param path relative path from root node
         */
        public DynamicFileTreeItem(FileInfo fileInfo, String path) {
            super(fileInfo);
            this.path = path;
        }

        /**
         * Sends the query to server and returns list of child files.
         */
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
            return !getValue().isDirectory();
        }

        private ObservableList<DynamicFileTreeItem> buildChildren(DynamicFileTreeItem treeItem) {
            FileInfo file = treeItem.getValue();
            if (file.isDirectory()) {
                try {
                    List<FileInfo> files = ftpClient.list(path);
                    ObservableList<DynamicFileTreeItem> children = FXCollections.observableArrayList();

                    for (FileInfo childFile : files) {
                        children.add(new DynamicFileTreeItem(childFile, path + File.separator + childFile.getName()));
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
