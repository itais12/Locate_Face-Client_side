package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import main.FaceDetectionMain;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * The controller associated with the FaceDetection view of our application.
 */

public class FaceDetectionController {

	// Haar-cascade classifier
	private final String classifierPath = "resources/haarcascades/haarcascade_frontalface_alt.xml";

	// dim of face picture to save
	private final double picHeight = 80;
	private final double picWidth = 80;

	// face cascade classifier
	private CascadeClassifier faceCascade;

	// mode frame period
	private final long registerFramePeriod = 250;
	private final long identifyFramePeriod = 200;

	// private String filePath;
	private FaceDetectionMain main;
	private boolean isRegister;
	private boolean tabletMode;

	// number of face pictures to get
	private int amountOfPictures;
	private int currentAmountPict;

	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	@FXML
	private Text amountText;
	
	// a timer for acquiring the video stream and timeout
	private ScheduledExecutorService cameraTimer;
	private ScheduledExecutorService stopTimer;
	private final long stopAfter8Sec = 8;

	// the OpenCV object that performs the video capture
	private VideoCapture capture;

	private int absoluteFaceSize;

	private ArrayList<Mat> matImages;

	@FXML
	public void initialize() {
		this.currentAmountPict = 0;
	}

	// Init the controller
	public void init(int amountOfPictures, boolean isRegister, boolean tabletMode) throws IOException {
		this.isRegister = isRegister;
		this.tabletMode = tabletMode;
		this.amountOfPictures = amountOfPictures;
		amountText.setText(currentAmountPict+"/"+amountOfPictures);
		capture = new VideoCapture();
		faceCascade = new CascadeClassifier();
		// faceCascade load
		faceCascade.load(classifierPath);

		absoluteFaceSize = 0;
		matImages = new ArrayList<>();
		// set a fixed width for the frame
		originalFrame.setFitWidth(600);
		// preserve image ratio
		originalFrame.setPreserveRatio(true);
		startCamera();
	}

	public void setMain(FaceDetectionMain main) {
		this.main = main;
	}

	// Start camera and timers
	protected void startCamera() throws IOException {
		// start the video capture
		if (!capture.isOpened()) {
			if (tabletMode)
				capture.open(1 + Videoio.CAP_DSHOW); // for front camera in tablet
			else
				capture.open(0 + Videoio.CAP_DSHOW); // only one camera
		}

		// check if the video stream available
		if (capture.isOpened()) {
			// grab a frame
			Runnable frameGrabber = new Runnable() {

				@Override
				public void run() {

					// effectively grab and process a single frame
					Mat frame = grabFrame();
					// convert and show the frame
					Image imageToShow = Utils.mat2Image(frame);
					updateImageView(originalFrame, imageToShow);
					System.out
							.println("currentFram= " + currentAmountPict + "numOfPicturePerPerson" + amountOfPictures);

					if (currentAmountPict >= amountOfPictures) {
						//change faceCounter to green
						amountText.setFill(Color.GREEN);
						// stop the timer
						stopAcquisition();

						Platform.runLater(() -> {
							main.sendToServer(matImages);
						});
					}
				}
			};

			cameraTimer = Executors.newSingleThreadScheduledExecutor();

			if (!isRegister) {
				cameraTimer.scheduleAtFixedRate(frameGrabber, 0, identifyFramePeriod, TimeUnit.MILLISECONDS);
				Runnable stopWork = new Runnable() {

					@Override
					public void run() {
						stopAcquisition();
						Platform.runLater(() -> {
							Utils.errorWaitAlert("The Identify took more than 8 seconds");

							main.startApp();
						});
					}
				};
				stopTimer = Executors.newSingleThreadScheduledExecutor();
				stopTimer.scheduleAtFixedRate(stopWork, stopAfter8Sec, 1, TimeUnit.SECONDS);
			} else
				cameraTimer.scheduleAtFixedRate(frameGrabber, 0, registerFramePeriod, TimeUnit.MILLISECONDS);

		} else {
			Utils.errorWaitAlert("Failed to open the camera connection.\n Check tablet mode in settings");
			main.startApp();
		}
	}

	// Get a frame from the video stream
	private Mat grabFrame() {
		Mat frame = new Mat();

		// check if the capture is open
		if (capture.isOpened()) {
			try {
				// read the current frame
				capture.read(frame);
				if (tabletMode)
					Core.rotate(frame, frame, Core.ROTATE_180); // front camera in tablet show upside down

				if (!frame.empty()) {
					// face detection
					detectAndDisplay(frame);
				}

			} catch (Exception e) {
				// camera failure
				System.err.println("Exception during the image elaboration: " + e);
			}
		}

		return frame;
	}

	// face detection and tracking
	public void detectAndDisplay(Mat frame) {
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();
		Mat equalizeFrame = new Mat();

		// convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, equalizeFrame);

		Mat filtered = new Mat(grayFrame.size(), CvType.CV_8U);
		Imgproc.bilateralFilter(equalizeFrame, filtered, 0, 20.0, 2.0);
	
		// compute minimum face size (30% of the frame height)
		if (absoluteFaceSize == 0) {
			int height = filtered.rows();
			if (Math.round(height * 0.3f) > 0) {
				absoluteFaceSize = Math.round(height * 0.3f);
			}
		}

		// detect faces
		this.faceCascade.detectMultiScale(filtered, faces, 1.3, 5,
				0 | Objdetect.CASCADE_SCALE_IMAGE | Objdetect.CASCADE_FIND_BIGGEST_OBJECT,
				new Size(absoluteFaceSize, absoluteFaceSize), new Size());

		// rectangle on the faces and save gray face frame
		Rect[] facesArray = faces.toArray();
		for (Rect rect : facesArray) {
			Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 255, 0)); // frame is Mat
			Rect rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
			Mat faceImage = new Mat(filtered, rectCrop);

			Size sz = new Size(picWidth, picHeight);
			Imgproc.resize(faceImage, faceImage, sz);

			if (!isRegister || getSimilarity(faceImage)) {
				matImages.add(faceImage);
				currentAmountPict++;
				amountText.setText(currentAmountPict+"/"+amountOfPictures);

			}
		}

	}

	private boolean getSimilarity(Mat image) {
		for (Mat mat : matImages) {
			// Calculate the L2 relative error between the 2 images.
			double errorL2 = Core.norm(mat, image, Core.NORM_L2);
			// Scale the value since L2 is summed across all pixels.
			double similarity = errorL2 / (double) (mat.rows() * mat.cols());
			if (similarity < 0.3)
				return false;
		}
		return true;
	}

	// Stop the the camera and release all the timers
	private void stopAcquisition() {

		if (this.cameraTimer != null && !this.cameraTimer.isShutdown()) {
			try {
				// stop the timer
				this.cameraTimer.shutdown();
				this.cameraTimer.awaitTermination(500, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the cameraTimer" + e);
			}
		}
		if (this.stopTimer != null && !this.stopTimer.isShutdown()) {
			try {
				// stop the timer
				this.stopTimer.shutdown();
				this.stopTimer.awaitTermination(500, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the stoptimer" + e);
			}
		}
		if (this.capture.isOpened()) {
			// release the camera
			this.capture.release();
		}
	}

	// Update the ImageView in the JavaFX main thread
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	// On application close, stop the timers and the camera
	public void setClosed() {
		this.stopAcquisition();
	}

	public ArrayList<Mat> getMatImages() {
		return matImages;
	}
}
