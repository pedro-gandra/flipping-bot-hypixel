package me.pedrogandra.bazaarbot.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HypixelApiClient {

    private static final String BASE_URL = "https://api.hypixel.net/v2";
    private static final String USER_AGENT = "Mozilla/5.0";

    // Método para buscar o JSON do Bazaar
    public JsonObject getBazaarData() throws Exception {
        String endpoint = BASE_URL + "/skyblock/bazaar";
        URL url = new URL(endpoint);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Erro ao acessar API Hypixel: Código " + responseCode);
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
        );
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
            throw new RuntimeException("A requisição foi bem-sucedida mas retornou sucesso = false.");
        }

        return json.getAsJsonObject("products");
    }
}
