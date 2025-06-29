package me.pedrogandra.bazaarbot.auth;

public class AuthAccount {

    private final String username;
    private final String uuid;
    private String accessToken;
    private String refreshToken;
    private long expiresAt;

    private final String xboxUhs;

    public AuthAccount(String username, String uuid, String accessToken, String refreshToken, long expiresAt, String xboxUhs) {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.xboxUhs = xboxUhs;
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public String getXboxUhs() {
        return xboxUhs;
    }

    public boolean isTokenExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    public void updateTokens(String newAccessToken, String newRefreshToken, long newExpiresAt) {
        this.accessToken = newAccessToken;
        this.refreshToken = newRefreshToken;
        this.expiresAt = newExpiresAt;
    }

    @Override
    public String toString() {
        return username + " (" + uuid + ")";
    }
}
