/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ultralytics_cloud;

import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import org.json.JSONArray;

/**
 *
 * @author cezerilab
 */
public class TestOkHTTP {

    public static void main(String[] args) throws IOException {
        String url = "https://api.ultralytics.com/v1/predict/kVciu58rzBTi71XqNsDZ";
        String apiKey = "8fd4f78b7b2e2a0cd8ed8cdc26e9061225ff500d13";
        String imagePath = "D:\\DATASETS\\perihan_yolo_ds\\yolo_format\\detect\\images\\test\\fig_1.jpg";

//        OkHttpClient client = new OkHttpClient();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Bağlantı zaman aşımı
                .readTimeout(30, TimeUnit.SECONDS) // Okuma zaman aşımı
                .writeTimeout(30, TimeUnit.SECONDS) // Yazma zaman aşımı
                .build();
        // Create request body
        JSONObject data = new JSONObject();
        data.put("size", 640);
        data.put("confidence", 0.25);
        data.put("iou", 0.45);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", new File(imagePath).getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), new File(imagePath)))
                .addFormDataPart("data", data.toString())
                .build();

        // Create request
        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-api-key", apiKey)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Print inference results
            JSONObject json = new JSONObject(response.body().string());
            System.out.println(json.toString(2));
            // "data" dizisini al
            JSONArray dataArray = json.getJSONArray("data");

            // Her bir tespit edilen nesne için döngü
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject object = dataArray.getJSONObject(i);

                // Bounding box bilgilerini al
                double xcenter = object.getDouble("xcenter");
                double ycenter = object.getDouble("ycenter");
                double width = object.getDouble("width");
                double height = object.getDouble("height");
                int classIndex = object.getInt("class");
                double confidence = object.getDouble("confidence");

                System.out.println("Nesne " + (i + 1) + " Bounding Box:");
                System.out.println("class: " + classIndex);
                System.out.println("confidence: " + confidence);
                System.out.println("xcenter: " + xcenter);
                System.out.println("ycenter: " + ycenter);
                System.out.println("width: " + width);
                System.out.println("height: " + height);
                System.out.println("");
            }
        }
    }
}
