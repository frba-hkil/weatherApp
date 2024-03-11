package com.myprojects.weather;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class HelloController {
    @FXML
    private TextField searchText;
    private WeatherData data;
    @FXML
    private Text temp;
    @FXML
    private Text weatherDesc;
    @FXML
    private ImageView weatherIcon;
    @FXML
    private Text humidity;
    @FXML
    private Text rainProb;
    @FXML
    private Text wind;

    @FXML
    public void renderWeatherData() throws IOException, InterruptedException, ParseException, URISyntaxException {

        JSONObject coordinates = Weather.getCoordinates(searchText.getText());
        double lat = 0, lng = 0;

        if (coordinates != null) {
            JSONArray a = (JSONArray) coordinates.get("results");
            JSONObject object = (JSONObject) a.getFirst();
            lat = (double) object.get("latitude");
            lng = (double) object.get("longitude");
        }

        data = Weather.getWeatherData(lat, lng);
        System.out.println(data);
        temp.setText(String.valueOf((int)data.temp()) + " °C");
        String weatherDescription = getWeatherDescription(data.code());
        weatherDesc.setText(weatherDescription);

        String iconName = weatherDescription + ".png";
        String weatherIconFilePath = Paths.get(getClass().getResource(iconName).toURI()).toString();

        Image image = new Image("file:" + weatherIconFilePath);
        weatherIcon.setImage(image);
        humidity.setText(String.valueOf(data.hmdty()) + "%");
        rainProb.setText(String.valueOf(data.precprob()) + "%");
        wind.setText(String.valueOf(data.windspd()) + "km/h");
    }

    private String getWeatherDescription(Long code) {
        int icode = code.intValue();

        return switch (icode) {
            case 0, 1 -> "clear sky";
            case 2 -> "partly cloudy";
            case 3 -> "cloudy";
            case 45, 48 -> "foggy";
            case 61, 63, 65 -> "rainy";
            case 95, 96 -> "thunderstorm";
            default -> "what weather is this?";
        };
    }
}