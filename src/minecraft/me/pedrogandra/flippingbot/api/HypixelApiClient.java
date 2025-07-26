package me.pedrogandra.flippingbot.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HypixelApiClient {

    private static final String BASE_URL = "https://api.hypixel.net/v2";
    private static final String USER_AGENT = "Mozilla/5.0";
    public static String API_KEY;

    public JsonObject getBazaarData() throws Exception {
        String endpoint = BASE_URL + "/skyblock/bazaar";
        return makeRequest(endpoint, false).getAsJsonObject("products");
    }

    public JsonArray getItemData() throws Exception {
        String endpoint = BASE_URL + "/resources/skyblock/items?key=" + API_KEY;
        return makeRequest(endpoint, true).getAsJsonArray("items");
    }
    
    public JsonArray getAuctionData() throws Exception {
        String endpoint = BASE_URL + "/skyblock/auctions?page=0";
        return makeRequest(endpoint, false).getAsJsonArray("auctions");
    }
    
    public String getPlayerName(String uuid) throws Exception {
        String endpoint = BASE_URL + "/player?key=" + API_KEY + "&uuid=" + uuid;
        JsonObject data = makeRequest(endpoint, true).getAsJsonObject("player");
        return data.get("displayname").getAsString();
    }

    private JsonObject makeRequest(String endpoint, boolean authRequired) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        if (authRequired && API_KEY != null) {
            connection.setRequestProperty("API-Key", API_KEY);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Erro ao acessar API Hypixel: Código " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();
        connection.disconnect();
        
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response.toString());
        JsonObject json = element.getAsJsonObject();

        if (!json.get("success").getAsBoolean()) {
            throw new RuntimeException("Requisição retornou sucesso = false.");
        }

        return json;
    }
}
