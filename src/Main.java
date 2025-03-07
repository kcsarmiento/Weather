import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            String city;
            do {
                // Retrieve user input
                System.out.println("===================================================");
                System.out.print("Enter City (Say No to Quit): ");
                city = scanner.nextLine();

                if (city.equalsIgnoreCase("No")) break;

                // Get location data
                JSONObject cityLocationData = getLocationData(city);
                if (cityLocationData == null) {
                    System.out.println("Error: Unable to retrieve location data.");
                    continue;
                }

                double latitude = (double) cityLocationData.get("latitude");
                double longitude = (double) cityLocationData.get("longitude");

                displayWeatherData(latitude, longitude);
            } while (!city.equalsIgnoreCase("No"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject getLocationData(String city) {
        city = city.replaceAll(" ", "+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                city + "&count=1&language=en&format=json";

        try {
            // Fetch API response
            HttpURLConnection apiConnection = fetchApiResponse(urlString);

            // Check if the connection was successful
            if (apiConnection == null || apiConnection.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // Read the response
            String jsonResponse = readApiResponse(apiConnection);

            // Parse the response
            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(jsonResponse);

            // Check if "results" key exists and is not empty
            JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
            if (locationData == null || locationData.isEmpty()) {
                System.out.println("Error: No location data found for city: " + city);
                return null;
            }

            return (JSONObject) locationData.get(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void displayWeatherData(double latitude, double longitude) {
        try {
            // Fetch API response
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                    "&longitude=" + longitude + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m";
            HttpURLConnection apiConnection = fetchApiResponse(url);

            // Check if the connection was successful
            if (apiConnection == null || apiConnection.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return;
            }

            // Read the response
            String jsonResponse = readApiResponse(apiConnection);

            // Parse the response
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
            JSONObject currentWeatherJson = (JSONObject) jsonObject.get("current");

            // Display weather data
            String time = (String) currentWeatherJson.get("time");
            System.out.println("Current Time: " + time);

            double temperature = (double) currentWeatherJson.get("temperature_2m");
            System.out.println("Current Temperature (°C): " + temperature);

            long relativeHumidity = (long) currentWeatherJson.get("relative_humidity_2m");
            System.out.println("Relative Humidity: " + relativeHumidity + "%");

            double windSpeed = (double) currentWeatherJson.get("wind_speed_10m");
            System.out.println("Wind Speed (m/s): " + windSpeed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readApiResponse(HttpURLConnection apiConnection) {
        try {
            // Read response
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(apiConnection.getInputStream());

            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();

            return resultJson.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            // Create connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
