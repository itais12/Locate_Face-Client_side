package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.springframework.web.client.RestClientException;
import controller.FaceDetectionController;
import controller.HomeIdentificationController;
import controller.HomeRegisterController;
import controller.SettingsController;
import controller.Utils;
import model.Worker;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * This application get worker details and start a video stream then try to find
 * any human face in a frame with Haar classifier. then send
 * them to server and present the server results.
 */
public class FaceDetectionMain extends Application {

	private boolean isRegister;
	// Amount of face images to get
	private int amountOfPictures;
	// Server details - get from properties file
	private String host;
	private int port;
	private boolean tabletMode;
	private boolean settingsFileExist;
	// private ServerTransport transport;
	private ServerTransportRest transport;

	// JavaFX
	static Stage stage;
	static Scene scene;

	// Objects to send the server
	private String sendType;
	private Worker worker;
	private String base_url;

	public static void main(String[] args) {
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		this.settingsFileExist = false;
		File propertiesFile = new File("config.properties");
		try {
			if (!propertiesFile.exists()) {
				createProperties();
			} else {
				readSettings();
				startApp();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	// Change frame to application mode
	public void startApp() {
		try {
			FXMLLoader loader;
			// load the FXML resource
			if (isRegister) {
				loader = changeScreen("/view/HomeRegister.fxml", new BorderPane(), "Register");
				HomeRegisterController regiController = loader.getController();
				regiController.setMain(this);
			} else {
				loader = changeScreen("/view/HomeIdentification.fxml", new BorderPane(), "Identification");
				HomeIdentificationController identiController = loader.getController();
				identiController.setMain(this);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void readSettings() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream("config.properties");
			// load a properties file
			prop.load(input);

			// get the property value and print it out
			isRegister = "Register".equalsIgnoreCase(prop.getProperty("mode"));
			host = prop.getProperty("host");
			port = Integer.parseInt(prop.getProperty("port"));
			tabletMode = Boolean.valueOf(prop.getProperty("tablet"));
			amountOfPictures = Integer.parseInt(prop.getProperty("numOfPicture"));
			settingsFileExist = true;
			base_url = "https://" + host + ":" + port + "/";
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void createProperties() throws IOException {
		FXMLLoader loader = changeScreen("/view/Settings.fxml", new GridPane(), "Settings");
		SettingsController controller = loader.getController();
		controller.setMain(this);
		// when properties file not exist dont show "back" button
		if (!settingsFileExist) {
			controller.hideBackButton();
		} else {
			controller.setInputs(host, port, tabletMode);
			controller.showBackButton();
		}
	}

	// start recognize
	public void startReco(Worker user, String sendType) {
		try {
			transport = new ServerTransportRest(base_url);
			transport.checkConnection();

			this.sendType = sendType;
			this.worker = user;
			// open detection frame
			FXMLLoader loader = changeScreen("/view/FaceDetection.fxml", new BorderPane(),
					"Face Detection and Tracking");
			// initial the controller
			FaceDetectionController controller = loader.getController();
			controller.setMain(this);
			controller.init(amountOfPictures, isRegister, tabletMode);

			// set the proper behavior on closing the application
			stage.setOnCloseRequest((new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					controller.setClosed();
				}
			}));
		} catch (RestClientException e) {
			Utils.errorWaitAlert("Connection with server failed");
		} catch (IOException e) {
			Utils.errorWaitAlert(e.getMessage());
		}
	}

	public void sendToServer(ArrayList<Mat> matImages) {
		transport.BuildRequestJson(sendType, worker, amountOfPictures, isRegister, matImages);
		transport.sendRecvRest();

		// Back start screen
		this.startApp();
	}

	// Generic function to change frame. Parent is the base class for all scene
	// parts.
	public <L extends Parent> FXMLLoader changeScreen(String fxmlPath, L layout, String sceneTitle) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource(fxmlPath));
		layout = loader.load();
		layout.setStyle("-fx-background-color: whitesmoke;");
		scene = new Scene(layout, 800, 600);
		stage.setTitle(sceneTitle);
		stage.setScene(scene);
		// show the GUI
		stage.show();
		return loader;
	}

}
