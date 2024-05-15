/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ultralytics_cloud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 *
 * @author cezerilab
 */
public class TestHTTPPostRequests {

    public static void main(String[] args) {
        try {
            URL url = new URL("https://api.ultralytics.com/v1/predict/kVciu58rzBTi71XqNsDZ");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-api-key", "8fd4f78b7b2e2a0cd8ed8cdc26e9061225ff500d13");
            connection.setDoOutput(true);

            JSONObject data = new JSONObject();
            data.put("size", 640);
            data.put("confidence", 0.25);
            data.put("iou", 0.45);

            String path="D:\\DATASETS\\perihan_yolo_ds\\yolo_format\\detect\\images\\test";
            File imageFile = new File(path+"/fig_0.jpg");
            FileInputStream fileInputStream = new FileInputStream(imageFile);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(data.toString().getBytes());
            outputStream.write("\r\n".getBytes());
            outputStream.write("image".getBytes());
            outputStream.write(fileInputStream.readAllBytes());
            outputStream.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONObject jsonResponse = new JSONObject(response.toString());
                System.out.println(jsonResponse.toString(2));
            } else {
                System.out.println("POST request failed with response code " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
