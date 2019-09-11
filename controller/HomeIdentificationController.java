package controller;

import java.io.IOException;
import main.FaceDetectionMain;
import model.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;

/**
 * The controller associated with the HomeIdentification view of our
 * application.
 */

public class HomeIdentificationController {
	// type of Identification for the database
	private final String identifyInType = "in";
	private final String identifyOutType = "out";
	
	@FXML
	private PasswordField pinCodeField;

	// reference to main
	FaceDetectionMain main;

	public void setMain(FaceDetectionMain main) {
		this.main = main;
	}

	// Event Listener on Button[#inTypeButton].onMouseClicked
	@FXML
	public void startInTypeRecoClick(MouseEvent event) throws IOException {
		startReco(identifyInType);
	}

	// Event Listener on Button[#outTypeButton].onMouseClicked
	@FXML
	public void startOutTypeRecoClick(MouseEvent event) throws IOException {
		startReco(identifyOutType);
	}

	// Get pincode ,create worker and change frame
	public void startReco(String sendType) throws IOException {
		if (Utils.checkIsInteger(pinCodeField.getText())) {
			Worker user = new Worker(pinCodeField.getText() + "");
			main.startReco(user, sendType);
		} else {
			Utils.errorWaitAlert("PinCode must be a number");
		}
	}

	// Event Listener on Button[#settingsButton].onMouseClicked
	@FXML
	public void SettingsClicked(MouseEvent event) throws IOException {
		main.createProperties();
	}

}
