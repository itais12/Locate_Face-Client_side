package main;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Base64;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import controller.Utils;
import model.Worker;

public class ServerTransportRest {
	// Gson convert object to json and back.
	private final Gson gson = new Gson();
	// Error Messages from server
	private final String IDENTIFY_SUCCESS1 = "Error: The worker already checked-in 3 time today";
	private final String IDENTIFY_SUCCESS2 = "Error: No check-in was found";

	// type of Identification for the database
	private final String identifyInType = "in";
	private final String identifyOutType = "out";
	private final String recognition = "recognition";

	private Worker worker;
	private boolean isRegister;

	private RestTemplate restTemplate;
	private String base_url;
	private String current_url;
	private String sendJson;
	private JsonObject json;

	public ServerTransportRest(String base_url) {
		this.base_url = base_url;
		this.restTemplate = buildSSLRestTemplate();
	}

	public RestTemplate buildSSLRestTemplate() {
		CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
				.build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);

		return new RestTemplate(requestFactory);
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void BuildRequestJson(String sendType, Worker worker, int amountOfPictures, boolean isRegister,
			ArrayList<Mat> matImages) {
		this.isRegister = isRegister;

		json = new JsonObject();
		json.addProperty("worker", gson.toJson(worker));
		json.addProperty("amountOfPictures", amountOfPictures);

		if (identifyInType.equals(sendType) || identifyOutType.equals(sendType)) {
			current_url = base_url + recognition;
			json.addProperty("reqType", sendType);
		} else
			current_url = base_url + sendType;

		for (int currentface = 0; currentface < amountOfPictures; currentface++) {
			JsonObject image = new JsonObject();
			byte[] bytes = MatToByteArr(matImages.get(currentface));
			image.addProperty("image", Base64.getEncoder().encodeToString(bytes));

			json.addProperty(String.valueOf(currentface), image.toString());
		}

		sendJson = json.toString();
	}

	// Server Rest network.
	public boolean sendRecvRest() {
		boolean operationSucceed = false;
		System.out.println("Client connected");
		try {
			json = this.restTemplate.postForObject(current_url, sendJson, JsonObject.class);
			JsonElement responseElement = json.get("response");
			if (responseElement == null) {
				worker = gson.fromJson(json, Worker.class);
				System.out.println("User return from server:" + worker.getWorkerId());
				if (worker != null) {
					operationSucceed = true;
					if (isRegister) {
						Utils.infoWaitAlert("Your worker id: " + this.worker.getWorkerId() + "\nYour pinCode is "
								+ this.worker.getPinCode());
					} else
						Utils.infoWaitAlert("Your worker id: " + this.worker.getWorkerId() + "\nYour name is: "
								+ this.worker.getName());
				}
			} else {
				String response = responseElement.getAsString();
				if (IDENTIFY_SUCCESS1.equals(response) || IDENTIFY_SUCCESS2.equals(response)) {
					operationSucceed = true;
					Utils.infoWaitAlert(response);
				} else {
					Utils.errorWaitAlert(response);
				}
			}

		} catch (RestClientException e) {
			Utils.errorWaitAlert(e.getMessage());
		} catch (JsonSyntaxException e) {
			Utils.errorWaitAlert(e.getMessage());
		}
		return operationSucceed;
	}

	public static byte[] MatToByteArr(Mat mat) {
		MatOfByte bytemat = new MatOfByte();
		Imgcodecs.imencode(".pgm", mat, bytemat);
		byte[] bytes = bytemat.toArray();
		return bytes;
	}

	public void checkConnection() {
		restTemplate.headForHeaders(base_url);
	}

}