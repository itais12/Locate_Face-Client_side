package controller;

import java.io.IOException;
import java.util.Properties;
import main.FaceDetectionMain;

import java.io.FileOutputStream;
import java.io.OutputStream;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * The controller associated with the Settings view of our application.
 */
public class SettingsController {
	// Admin Password
	private final String realAdminPassword = "rootUser";
	// amount of face picture to get in every mode.
	private final String numOfPictureRegister = 15 + "";
	private final String numOfPictureIdentify = 10 + "";
	private final String propertiesFileName = "config.properties";
	// reference to main
	FaceDetectionMain main;

	@FXML
	private PasswordField adminPasswordField;
	@FXML
	private TextField hostField;
	@FXML
	private TextField portField;
	@FXML
	private Button identifyButton;
	@FXML
	private Button registerButton;
	@FXML
	private Button backButton;
	@FXML
	private CheckBox tabletBox;

	public void setMain(FaceDetectionMain main) {
		this.main = main;
	}

	public void setInputs(String host, int port, boolean tabletMode) {
		hostField.setText(host);
		portField.setText(String.valueOf(port));
		tabletBox.setSelected(tabletMode);
	}

	// Event Listener on Button[#setIdentifyMode].onMouseClicked
	@FXML
	public void identifyModeClick(MouseEvent event) throws IOException {
		if (checkInputs()) {
			buildPropertiesFile("identify");
			main.readSettings();
			main.startApp();
		}
	}

	// Event Listener on Button[#setRegisterMode].onMouseClicked
	@FXML
	public void registerModeClick(MouseEvent event) throws IOException {
		if (checkInputs()) {
			buildPropertiesFile("register");
			main.readSettings();
			main.startApp();
		}
	}

	private boolean checkInputs() {
		if (!checkAdminPassword()) {
			Utils.errorWaitAlert("Admin password incorrect");
			return false;
		} else if (hostField.getText().isEmpty()) {
			Utils.errorWaitAlert("Host must not be empty");
			return false;
		} else if (!Utils.checkIsInteger(portField.getText())) {
			Utils.errorWaitAlert("Port must be integer");
			return false;
		}

		return true;
	}

	public boolean checkAdminPassword() {
		String adminUserTry = adminPasswordField.getText();
		return realAdminPassword.equals(adminUserTry);
	}

	// Event Listener on Button[#backButton].onMouseClicked
	@FXML
	public void backClicked(MouseEvent event) throws IOException {
		main.readSettings();
		main.startApp();
	}

	public void buildPropertiesFile(String newMode) {

		Properties prop = new Properties();
		OutputStream output = null;
		String host = hostField.getText();

		if (host.isEmpty())
			host = "localhost";

		try {

			output = new FileOutputStream(propertiesFileName);

			// set the properties value
			prop.setProperty("mode", newMode);
			prop.setProperty("host", host);
			prop.setProperty("port", portField.getText());
			prop.setProperty("tablet", tabletBox.isSelected() + "");
			switch (newMode) {
			case "register":
				prop.setProperty("numOfPicture", numOfPictureRegister);
				break;
			default:
				prop.setProperty("numOfPicture", numOfPictureIdentify);
			}
			// save properties to project root folder
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// when there is no frame to back
	public void hideBackButton() {
		backButton.setVisible(false);
	}

	// when there is frame to back
	public void showBackButton() {
		backButton.setVisible(true);
	}
}
