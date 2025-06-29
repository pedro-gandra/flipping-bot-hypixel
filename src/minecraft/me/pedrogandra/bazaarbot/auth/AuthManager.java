package me.pedrogandra.bazaarbot.auth;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.util.ArrayList;
import java.util.List;

public class AuthManager {
	
	private static final MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
	private static final SessionManager sessionManager = new SessionManager(authenticator);
	private static final List<AuthAccount> accounts = new ArrayList<AuthAccount>();
    private static AuthAccount currentAccount = null;

    // Adiciona nova conta (ex: após login)
    public static void addAccount(AuthAccount account) {
        accounts.add(account);
        sessionManager.addAccount(account);
        setCurrentAccount(account);
    }

    // Define a conta ativa e troca a sessão do Minecraft
    
    public static void setCurrentAccount(AuthAccount account) {
        try {
            AuthAccount valid = sessionManager.getValidAccount(account.getUsername());
            if (valid != null) {
                currentAccount = valid;
                Minecraft.getMinecraft().session = new Session(
                    valid.getUsername(),
                    valid.getUuid(),
                    valid.getAccessToken(),
                    "msa"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            currentAccount = null;
        }
    }

    // Conta atualmente ativa
    public static AuthAccount getCurrentAccount() {
        return currentAccount;
    }

    // Todas as contas salvas (futuramente carregadas do disco)
    public static List<AuthAccount> getAccounts() {
        return accounts;
    }

    // Remove uma conta da lista
    public static void removeAccount(AuthAccount account) {
        accounts.remove(account);
        if (currentAccount == account) {
            currentAccount = null;
            // Aqui você pode setar uma conta padrão ou desconectar
        }
    }

    // Verifica se a conta ativa está com token válido
    public static boolean isLoggedIn() {
        return currentAccount != null && !currentAccount.isTokenExpired();
    }

    // Força atualização da sessão (caso renovemos token, por exemplo)
    public static void updateSession() {
        if (currentAccount != null) {
            Minecraft.getMinecraft().session = new Session(
                currentAccount.getUsername(),
                currentAccount.getUuid(),
                currentAccount.getAccessToken(),
                "msa"
            );
        }
    }
}