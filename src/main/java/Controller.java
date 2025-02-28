import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

public class Controller {

    @FXML
    private RadioButton bOkno;
    @FXML
    private RadioButton bPlik;
    @FXML
    private ToggleGroup radioButtons;
    @FXML
    private TextArea unencryptedText;

    @FXML
    private TextArea encryptedText;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField key;

    private final DES des = new DES();
    private byte[] encodedM;
    private byte[] unencryptedTextBytes;

    public void generateKey(ActionEvent actionEvent) {
        key.setText("1D34BA02C0AFE567");
    }

    // Sprawdza czy tekst jest w formacie HEX i sklada sie z 16 znakow.
    private boolean testKey(String string) {
        return string.matches("[0-9A-Fa-f]{16}");
    }

    // Wczytuje klucz z pliku txt, zapisany w formacie HEX w jednej linii
    public void loadKey(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        fileChooser.getExtensionFilters().add(extensionFilter);
        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        try {
            String keyValue = Files.readString(selectedFile.toPath());
            if (testKey(keyValue)) {
                key.setText(keyValue);
            } else {
                showAlert("Klucz zawarty w pliku jest bledny. Klucz musi byc zapisany w jednej linii, " +
                        "w formacie HEX i skladac sie z 16 znakow.");
            }

        } catch (IOException e) {
            showAlert("Nie udalo sie otworzyc pliku.");
        }
    }

    // Zapisuje podany klucz do pliku txt
    public void saveKey(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        fileChooser.getExtensionFilters().add(extensionFilter);
        File selectedFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        try {
            String keyValue = key.getText();
            if (testKey(keyValue)) {
                Files.writeString(selectedFile.toPath(), keyValue, StandardOpenOption.CREATE);
            } else {
                showAlert("Podany klucz jest bledny. Klucz musi byc zapisany w formacie HEX" +
                        " i skladac sie z 16 znakow.");
            }

        } catch (IOException e) {
            showAlert("Nie udalo sie utworzyc pliku.");
        }
    }


    // Wyswietla alert z przekazana trescia
    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd!");
        alert.setContentText(content);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public void loadUnencrypted(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        try (
                FileInputStream fileInputStream = new FileInputStream(selectedFile)
        ) {
            byte[] data = new byte[fileInputStream.available()];

            fileInputStream.read(data);
            unencryptedTextBytes = data;
            String readText = new String(data, StandardCharsets.UTF_8);
            System.out.println(unencryptedTextBytes.length);
            System.out.println("to" + Arrays.toString(unencryptedTextBytes));
            unencryptedText.setText(readText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void saveUnencrypted(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        try (
                OutputStream fileOutputStream = new FileOutputStream(selectedFile)
        ) {
            fileOutputStream.write(Objects.requireNonNullElseGet(unencryptedTextBytes,
                    () -> unencryptedText.getText().getBytes()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveEncrypted(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        try (
                OutputStream fileOutputStream = new FileOutputStream(selectedFile)
        ) {
            fileOutputStream.write(Objects.requireNonNullElseGet(encodedM,
                    () -> encryptedText.getText().getBytes()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadEncrypted(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        try (
                FileInputStream fileInputStream = new FileInputStream(selectedFile)
        ) {
            byte[] data = new byte[fileInputStream.available()];

            fileInputStream.read(data);
            encodedM = data;
            String readText = new String(data, StandardCharsets.UTF_8);
            encryptedText.setText(readText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void encrypt(ActionEvent actionEvent) {
        if (!unencryptedText.getText().isEmpty() && testKey(key.getText())) {
            des.byteKey = des.hexBin(key.getText());
            if (bOkno.isSelected()) {
                encodedM = des.encrypt(unencryptedText.getText().getBytes());
            } else if (bPlik.isSelected()) {
                if (unencryptedTextBytes != null) {
                    encodedM = des.encrypt(unencryptedTextBytes);
                } else {
                    showAlert("Nie wczytano pliku.");
                }
            }

            encryptedText.setText(des.binHex(encodedM));
        } else {
            showAlert("Najpierw wprowadz tekst.");
        }
    }

    public void decrypt(ActionEvent actionEvent) {
        if (!encryptedText.getText().isEmpty() && testKey(key.getText())) {
            des.byteKey = des.hexBin(key.getText());
            byte[] decoded = des.decrypt(encodedM);
            System.out.println("to" + Arrays.toString(decoded));
            System.out.println(decoded.length);
            unencryptedTextBytes = decoded;
            unencryptedText.setText(new String(decoded));
        } else {
            showAlert("Najpierw wprowadz tekst.");
        }
    }
}
