package me.pedrogandra.bazaarbot.auth;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONObject;

public class SessionManager {

    private final List<AuthAccount> accounts = new CopyOnWriteArrayList<AuthAccount>();
    private final MicrosoftAuthenticator authenticator;

    public SessionManager(MicrosoftAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void addAccount(AuthAccount account) {
        accounts.add(account);
    }

    public List<AuthAccount> getAllAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public AuthAccount getAccountByUsername(String username) {
        for (int i = 0; i < accounts.size(); i++) {
            AuthAccount acc = accounts.get(i);
            if (acc.getUsername().equalsIgnoreCase(username)) {
                return acc;
            }
        }
        return null;
    }

    public AuthAccount getValidAccount(String username) throws Exception {
        AuthAccount acc = getAccountByUsername(username);
        if (acc == null) return null;

        // Verifica se o access token expirou
        if (System.currentTimeMillis() > acc.getExpiresAt()) {
            JSONObject refreshed = authenticator.refreshAccessToken(acc.getRefreshToken());

            acc.updateTokens(
                refreshed.getString("access_token"),
                refreshed.getString("refresh_token"),
                System.currentTimeMillis() + refreshed.getLong("expires_in") * 1000L
            );
        }

        return acc;
    }

    public void clearAllAccounts() {
        accounts.clear();
    }

    public boolean removeAccount(String username) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getUsername().equalsIgnoreCase(username)) {
                accounts.remove(i);
                return true;
            }
        }
        return false;
    }
}
