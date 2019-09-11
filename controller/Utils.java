package controller;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Mat;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

/**
 * Provide general methods for functions.
 */
public final class Utils {

	/*
	 * string that must be Integer
	 */
	public static boolean checkIsInteger(String string) {
		if (string.isEmpty())
			return false;
		char[] chararr = string.toCharArray();
		for (char c : chararr) {
			if (c < '0' || c > '9')
				return false;
		}
		return true;
	}

	/*
	 * Popup error Alert
	 */
	public static void errorWaitAlert(String errorMessage) {
			Alert alert = new Alert(AlertType.ERROR, errorMessage, ButtonType.OK);
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.showAndWait();
	}

	/*
	 * Popup info alert
	 */
	public static void infoWaitAlert(String message) {
			Alert info = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
			info.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			info.showAndWait();
	}

	/*
	 * Convert a Mat object (OpenCV) to image
	 */
	public static Image mat2Image(Mat frame) {
		try {
			return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		} catch (Exception e) {
			System.err.println("Cannot convert the Mat object: " + e);
			return null;
		}
	}

	/*
	 * Update the UI for camera
	 */
	public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
		Platform.runLater(() -> {
			property.set(value);
		});
	}

	/*
	 * Needed for the mat2image method
	 */
	private static BufferedImage matToBufferedImage(Mat original) {
		// init
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);

		if (original.channels() > 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}


}
