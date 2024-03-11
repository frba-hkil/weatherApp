package com.myprojects.weather;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Scanner;

public class Weather {

    public static JSONObject getCoordinates(String location) throws IOException, InterruptedException {

        String fLocation = location.replaceAll(" ", "+");
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://geocoding-api.open-meteo.com/v1/search?name="
                        + fLocation
                        + "&count=1&language=en&format=json")).GET().build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if(response.statusCode() != 200) {
            System.out.println("Error. got response code: " + response.statusCode());
            client.close();
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(response.body());
        while(scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();

        JSONParser jsonParser = new JSONParser();
        try {
            return (JSONObject) jsonParser.parse(stringBuilder.toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static WeatherData getWeatherData(double lat, double lng) throws IOException, InterruptedException, ParseException {
        URI uri = URI.create("https://api.open-meteo.com/v1/forecast?latitude="
                + String.valueOf(lat)
                + "&longitude="
                + String.valueOf(lng)
                + "&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,wind_speed_10m&timezone=auto");

        HttpResponse<InputStream> response = getResponse(uri);
        StringBuilder builder = new StringBuilder();
        if (response != null) {
            Scanner scanner = new Scanner(response.body());
            while(scanner.hasNext()) {
                builder.append(scanner.nextLine());
            }
            scanner.close();
        }
        JSONParser parser = new JSONParser();
        JSONObject hourly = (JSONObject) ((JSONObject) parser.parse(builder.toString())).get("hourly");
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        String ftime = time.format(timeFormatter);

        int timeIndex = getTimeIndex(ftime, hourly);

        WeatherData weatherData = new WeatherData(
                (Double) getWeatherParam(timeIndex, hourly, "temperature_2m")
                , (Long) getWeatherParam(timeIndex, hourly, "weather_code")
                , (Double) getWeatherParam(timeIndex, hourly, "wind_speed_10m")
                , (Long) getWeatherParam(timeIndex, hourly, "relative_humidity_2m")
                , (Long) getWeatherParam(timeIndex, hourly, "precipitation_probability")
        );
        hourly.clear();
        return weatherData;
    }

    private static int getTimeIndex(String time, JSONObject hourly) {
        JSONArray times = (JSONArray) hourly.get("time");
        for(int i = 0; i < times.size(); i++) {
            if(time.equalsIgnoreCase(times.get(i).toString())) {
                return i;
            }
        }
        return -1;
    }

    private static Object getWeatherParam(int index, JSONObject hourly, String key) {
        JSONArray array = (JSONArray) hourly.get(key);
        return array.get(index);
    }

    private static HttpResponse<InputStream> getResponse(URI uri) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if(response.statusCode() != 200) {
            System.out.println("Response code: "+ response.statusCode());
            client.close();
            return null;
        }

        return response;
    }
}
