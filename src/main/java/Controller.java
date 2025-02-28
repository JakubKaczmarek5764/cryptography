import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
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
    private TextArea signature;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField keyP;
    @FXML
    private TextField keyH;
    @FXML
    private TextField keyQ;
    @FXML
    private TextField keyV;
    @FXML
    private TextField keyA;

    private BigInteger[] encodedM = new BigInteger[2];
    private byte[] unencryptedTextBytes;



    public void loadKey(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setTitle("Wybierz plik z kluczem publicznym");
        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        try {
            String[] keyValues = Files.readString(selectedFile.toPath()).split("\n");
            if (keyValues.length == 4) {
                keyP.setText(keyValues[0]);
                keyQ.setText(keyValues[1]);
                keyH.setText(keyValues[2]);
                keyV.setText(keyValues[3]);
            } else {
                showAlert("Klucz zawarty w pliku jest bledny.", Alert.AlertType.ERROR);
            }


        } catch (IOException e) {
            showAlert("Nie udalo sie otworzyc pliku.", Alert.AlertType.ERROR);
        }
        fileChooser.setTitle("Wybierz plik z kluczem prywatnym");
        selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        try {
            String keyValue = Files.readString(selectedFile.toPath());
            if (true) {     // tbc
                keyA.setText(keyValue);
            } else {
                showAlert("Klucz zawarty w pliku jest bledny.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            showAlert("Nie udalo sie otworzyc pliku.", Alert.AlertType.ERROR);
        }
    }

    // Zapisuje podany klucz do pliku txt
    public void saveKey(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setTitle("Zapisz plik z kluczem publicznym");
        File selectedFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        try {
            String keyValue = keyP.getText()+"\n"+keyQ.getText()+"\n"+keyH.getText()+"\n"+keyV.getText();
            if (true) {
                Files.writeString(selectedFile.toPath(), keyValue, StandardOpenOption.CREATE);
            } else {
                showAlert("Podany klucz jest bledny.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            showAlert("Nie udalo sie utworzyc pliku.", Alert.AlertType.ERROR);
        }
        fileChooser.setTitle("Zapisz plik z kluczem prywatnym");
        selectedFile = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        try {
            String keyValue = keyA.getText();
            if (true) {
                Files.writeString(selectedFile.toPath(), keyValue, StandardOpenOption.CREATE);
            } else {
                showAlert("Podany klucz jest bledny.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            showAlert("Nie udalo sie utworzyc pliku.", Alert.AlertType.ERROR);
        }
    }


    // Wyswietla alert z przekazana trescia
    private void showAlert(String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
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
                FileWriter fileWriter = new FileWriter(selectedFile);
        ) {
            if(encodedM[0].toString() != null) {
                fileWriter.write(encodedM[0].toString(16)+"\n"+encodedM[1].toString(16));
            }
            else {
                fileWriter.write(signature.getText());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadEncrypted(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());


        try {
            String[] lines = Files.readString(selectedFile.toPath()).split("\n");
            if (lines.length == 2) {
                signature.setText(lines[0]+"\n"+lines[1]);
                encodedM[0] = new BigInteger(lines[0], 16);
                encodedM[1] = new BigInteger(lines[1], 16);
            } else {
                showAlert("Klucz zawarty w pliku jest bledny.", Alert.AlertType.ERROR);
            }


        } catch (IOException e) {
            showAlert("Nie udalo sie otworzyc pliku.", Alert.AlertType.ERROR);
        }
    }

    public void sign(ActionEvent actionEvent) {
        if (!unencryptedText.getText().isEmpty()) {
            Schnorr schnorr = loadDataToSchnorr();
            if (bOkno.isSelected()) {
                encodedM = schnorr.podpisz(unencryptedText.getText().getBytes());
            } else if (bPlik.isSelected()) {
                if (unencryptedTextBytes != null) {
                    encodedM = schnorr.podpisz(unencryptedTextBytes);
                } else {
                    showAlert("Nie wczytano pliku.", Alert.AlertType.ERROR);
                }
            }

            signature.setText(encodedM[0].toString(16)+"\n"+encodedM[1].toString(16));
        } else {
            showAlert("Najpierw wprowadz tekst.", Alert.AlertType.ERROR);
        }
    }

    public void verify(ActionEvent actionEvent) {
        if (!signature.getText().isEmpty() && !unencryptedText.getText().isEmpty()) {
            Schnorr schnorr = loadDataToSchnorr();

            String[] lines = signature.getText().split("\n");
            System.out.println(lines[0]+"\n"+lines[1]);
            encodedM[0] = new BigInteger(lines[0], 16);
            encodedM[1] = new BigInteger(lines[1], 16);
            if (bOkno.isSelected()) {
                if(schnorr.weryfikuj(unencryptedText.getText().getBytes(), encodedM) && (lines[0].startsWith("0") || lines[1].startsWith("0"))) {
                    showAlert("Podpis się nie zgadza", Alert.AlertType.INFORMATION);
                }   else if (schnorr.weryfikuj(unencryptedText.getText().getBytes(), encodedM)) {
                    showAlert("Podpis się zgadza", Alert.AlertType.INFORMATION);
                }
                else {
                    showAlert("Podpis się nie zgadza", Alert.AlertType.INFORMATION);
                }
            } else if (bPlik.isSelected()) {
                if (unencryptedTextBytes != null) {
                    if(schnorr.weryfikuj(unencryptedTextBytes, encodedM) && (lines[0].startsWith("0") || lines[1].startsWith("0"))) {
                        showAlert("Podpis się nie zgadza", Alert.AlertType.INFORMATION);
                    } else if (schnorr.weryfikuj(unencryptedTextBytes, encodedM)) {
                        showAlert("Podpis się zgadza", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Podpis się nie zgadza", Alert.AlertType.INFORMATION);
                    }
                } else {
                    showAlert("Nie wczytano pliku.", Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Najpierw wprowadz tekst.", Alert.AlertType.ERROR);
        }
    }

    private Schnorr loadDataToSchnorr() {
        Schnorr schnorr = new Schnorr();
        schnorr.p = new BigInteger(keyP.getText(), 16);
        schnorr.q = new BigInteger(keyQ.getText(), 16);
        schnorr.h = new BigInteger(keyH.getText(), 16);
        schnorr.v = new BigInteger(keyV.getText(), 16);
        if (keyA.getLength() != 0){
            schnorr.a = new BigInteger(keyA.getText(), 16);
        }

        return schnorr;
    }

    public void generateKey(ActionEvent actionEvent) {
        Schnorr schnorr = new Schnorr();
        schnorr.generateKey();
        keyP.setText(schnorr.p.toString(16));
        keyH.setText(schnorr.h.toString(16));
        keyQ.setText(schnorr.q.toString(16));
        keyV.setText(schnorr.v.toString(16));
        keyA.setText(schnorr.a.toString(16));
    }
}
