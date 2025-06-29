package me.pedrogandra.bazaarbot.auth;

import java.io.*;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class AuthStorage {

    private static final File FILE = new File("auth/accounts.json");

    // Salva a lista de contas no arquivo
    public static void saveAccounts(List<AuthAccount> accounts) {
        try {
            if (!FILE.getParentFile().exists()) {
                FILE.getParentFile().mkdirs();
            }

            JSONArray array = new JSONArray();
            for (AuthAccount acc : accounts) {
                JSONObject obj = new JSONObject();
                obj.put("username", acc.getUsername());
                obj.put("uuid", acc.getUuid());
                obj.put("accessToken", acc.getAccessToken());
                obj.put("refreshToken", acc.getRefreshToken());
                obj.put("expiresAt", acc.getExpiresAt());
                obj.put("xboxUhs", acc.getXboxUhs());
                array.put(obj);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE));
            writer.write(array.toString(2));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Carrega contas do arquivo (se existir)
    public static List<AuthAccount> loadAccounts() {
        List<AuthAccount> list = new ArrayList<AuthAccount>();

        if (!FILE.exists()) {
            return list;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            JSONArray array = new JSONArray(json.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String username = obj.getString("username");
                String uuid = obj.getString("uuid");
                String accessToken = obj.getString("accessToken");
                String refreshToken = obj.getString("refreshToken");
                long expiresAt = obj.getLong("expiresAt");
                String xboxUhs = obj.getString("xboxUhs");

                AuthAccount account = new AuthAccount(username, uuid, accessToken, refreshToken, expiresAt, xboxUhs);
                list.add(account);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}