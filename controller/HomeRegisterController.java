package controller;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.FaceDetectionMain;
import model.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * The controller associated with the HomeRegister view of our application.
 */
public class HomeRegisterController {
	// Human Resources password
	private final String realHRPassword = "rootUser";
	// Type of register for the database
	private final String registerType = "register";

	@FXML
	private Button startRecoButton;
	@FXML
	private PasswordField hRpassword;
	@FXML
	private TextField IDField;
	@FXML
	private TextField nameField;
	@FXML
	private TextField roleField;
	@FXML
	private TextField departmentField;
	@FXML
	private TextField emailField;
	@FXML
	private CheckBox managerPermissionBox;

	// Reference to main
	FaceDetectionMain main;

	// New worker details for the database
	private String name;
	private String ID;
	private String role;
	private String department;
	private String email;

	private boolean managerPermissions;

	public void setMain(FaceDetectionMain main) {
		this.main = main;
	}

	// Event Listener on Button[#settingsButton].onMouseClicked
	@FXML
	public void settingsClick(MouseEvent event) throws IOException {
		main.createProperties();
	}

	// Event Listener on Button[#startRecoButton].onMouseClicked
	@FXML
	public void startRecoClick(MouseEvent event) throws IOException {
		if (checkInputs()) {
			ID = IDField.getText();
			name = nameField.getText();
			role = roleField.getText();
			department = departmentField.getText();
			managerPermissions = managerPermissionBox.isSelected();
			email = emailField.getText();
			Worker user = new Worker(Integer.parseInt(ID), name, role, department, managerPermissions, email);
			main.startReco(user, registerType);
		}
	}

	// Verify admin password
	public boolean checkAdminPassword() {
		String adminUserTry = hRpassword.getText();
		return realHRPassword.equals(adminUserTry);
	}

	private boolean checkInputs() {
		if (!checkAdminPassword()) {
			Utils.errorWaitAlert("HR password incorrect");
			return false;
		} else if (nameField.getText().isEmpty()) {
			Utils.errorWaitAlert("Name must not be empty");
			return false;
		} else if (!Utils.checkIsInteger(IDField.getText())) {
			Utils.errorWaitAlert("ID must be number");
			return false;
		} else if (!validateEmailAddress(emailField.getText())) {
			Utils.errorWaitAlert("Invalid Email Address");
			return false;
		} else if (roleField.getText().isEmpty()) {
			Utils.errorWaitAlert("Role must be not empty");
			return false;
		} else if (departmentField.getText().isEmpty()) {
			Utils.errorWaitAlert("Department must be not empty");
			return false;
		}

		return true;
	}

	public boolean validateEmailAddress(String emailAddress) {
		Pattern regexPattern;
		Matcher regMatcher;
		regexPattern = Pattern.compile("^[(a-zA-Z-0-9-\\_\\+\\.)]+@[(a-z-A-Z-0-9-\\.)]+\\.[(a-zA-z)]{2,3}$");
		regMatcher = regexPattern.matcher(emailAddress);
		if (regMatcher.matches()) {
			return true;
		} else {
			return false;
		}
	}

}
