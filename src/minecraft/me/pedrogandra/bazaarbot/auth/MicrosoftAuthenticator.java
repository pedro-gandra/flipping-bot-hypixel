package me.pedrogandra.bazaarbot.auth;

import java.awt.Desktop;
import java.io.*;
import java.net.*;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

public class MicrosoftAuthenticator {

    private static final String CLIENT_ID = "0c8b8112-7b05-4ec9-bb44-e5afc1a5f048";
    private static final String REDIRECT_URI = "http://localhost:4672/callback";
    private static final String CLIENT_SECRET = "38h8Q~THwDUKWVE4REGQK7ycNg2LZxwIjHOacb49";
    private static final int PORT = 4672;

    public AuthAccount login() throws Exception {
    	String authUrl = "https://login.live.com/oauth20_authorize.srf" +
    		    "?client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
    		    "&response_type=code" +
    		    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") +
    		    "&scope=" + URLEncoder.encode("XboxLive.signin offline_access", "UTF-8") +
    		    "&prompt=select_account";

        final String[] codeHolder = new String[1];

        Thread serverThread = new Thread(new Runnable() {
            public void run() {
                try {
                    codeHolder[0] = waitForCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();

        Desktop.getDesktop().browse(new URI(authUrl));

        while (codeHolder[0] == null) {
            Thread.sleep(100);
        }

        String code = codeHolder[0];
        System.out.println("Código OAuth recebido: " + code);

        JSONObject tokenObj = exchangeCodeForToken(code);
        String accessToken = tokenObj.getString("access_token");
        String refreshToken = tokenObj.getString("refresh_token");
        long expiresIn = tokenObj.getLong("expires_in") * 1000L;
        long expiresAt = System.currentTimeMillis() + expiresIn;

        JSONObject xboxAuth = authenticateXboxLive(accessToken);
        String xboxUhs = xboxAuth.getString("uhs");
        String xstsToken = xboxAuth.getString("xstsToken");

        String mcAccessToken = authenticateMinecraft(xstsToken, xboxUhs);

        JSONObject profile = getMinecraftProfile(mcAccessToken);
        String username = profile.getString("name");
        String uuid = profile.getString("id");

        return new AuthAccount(username, uuid, mcAccessToken, refreshToken, expiresAt, xboxUhs);
    }

    private String waitForCode() throws Exception {
        ServerSocket server = new ServerSocket(PORT);
        Socket client = server.accept();

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

        String codeLine = "";
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.contains("GET") && line.contains("/callback?code=")) {
                codeLine = line;
                break;
            }
        }

        out.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nVocê pode fechar esta janela.");
        out.flush();

        client.close();
        server.close();

        int start = codeLine.indexOf("/callback?code=") + 15;
        int end = codeLine.indexOf(" ", start);
        return URLDecoder.decode(codeLine.substring(start, end), "UTF-8");
    }

    private JSONObject exchangeCodeForToken(String code) throws Exception {
        String params = "client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
                "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8") +
                "&grant_type=authorization_code" +
                "&code=" + URLEncoder.encode(code, "UTF-8") +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");

        return postJson("https://login.live.com/oauth20_token.srf", params, false);
    }

    private JSONObject authenticateXboxLive(String accessToken) throws Exception {
        JSONObject payload = new JSONObject();
        JSONObject props = new JSONObject();
        props.put("AuthMethod", "RPS");
        props.put("SiteName", "user.auth.xboxlive.com");
        props.put("RpsTicket", "d=" + accessToken);
        payload.put("Properties", props);
        payload.put("RelyingParty", "http://auth.xboxlive.com");
        payload.put("TokenType", "JWT");

        JSONObject response = postJson("https://user.auth.xboxlive.com/user/authenticate", payload.toString(), true);
        String token = response.getString("Token");
        String uhs = response.getJSONObject("DisplayClaims").getJSONArray("xui").getJSONObject(0).getString("uhs");

        JSONObject xstsPayload = new JSONObject();
        JSONObject xstsProps = new JSONObject();
        xstsProps.put("SandboxId", "RETAIL");
        JSONArray userTokens = new JSONArray();
        userTokens.put(token);
        xstsProps.put("UserTokens", userTokens);
        xstsPayload.put("Properties", xstsProps);
        xstsPayload.put("RelyingParty", "rp://api.minecraftservices.com/");
        xstsPayload.put("TokenType", "JWT");

        JSONObject xstsResponse = postJson("https://xsts.auth.xboxlive.com/xsts/authorize", xstsPayload.toString(), true);
        String xstsToken = xstsResponse.getString("Token");

        JSONObject result = new JSONObject();
        result.put("uhs", uhs);
        result.put("xstsToken", xstsToken);
        return result;
    }

    private String authenticateMinecraft(String xstsToken, String uhs) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("identityToken", "XBL3.0 x=" + uhs + ";" + xstsToken);

        JSONObject response = postJson("https://api.minecraftservices.com/authentication/login_with_xbox", payload.toString(), true);
        return response.getString("access_token");
    }

    private JSONObject getMinecraftProfile(String mcAccessToken) throws Exception {
        URL url = new URL("https://api.minecraftservices.com/minecraft/profile");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }
        reader.close();

        return new JSONObject(json.toString());
    }

    private JSONObject postJson(String urlString, String body, boolean isJson) throws Exception {
        URL url = new URL(urlString);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        if (isJson) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
        } else {
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        }

        OutputStream os = conn.getOutputStream();
        os.write(body.getBytes("UTF-8"));
        os.flush();
        os.close();

        int status = conn.getResponseCode();

        InputStream is;
        if (status >= 200 && status < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
            // Ler resposta de erro para debug
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(is));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            throw new IOException("HTTP " + status + " - " + errorResponse.toString());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }
        reader.close();

        return new JSONObject(json.toString());
    }
    
    public JSONObject refreshAccessToken(String refreshToken) throws Exception {
        String params = "client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
                "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8") +
                "&grant_type=refresh_token" +
                "&refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8") +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");

        JSONObject tokenObj = postJson("https://login.live.com/oauth20_token.srf", params, false);

        return tokenObj;
    }
}
