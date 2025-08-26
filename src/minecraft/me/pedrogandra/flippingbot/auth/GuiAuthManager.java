package me.pedrogandra.flippingbot.auth;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiAuthManager extends GuiScreen {

    private GuiScreen parent;
    private List<AuthAccount> accounts;
    private int selectedIndex = -1;

    public GuiAuthManager(GuiScreen parent) {
        this.parent = parent;
        this.accounts = AuthManager.getAccounts();
    }

    @SuppressWarnings("unchecked")
    public void initGui() {
        this.buttonList.clear();

        for (int i = 0; i < accounts.size(); i++) {
            AuthAccount acc = accounts.get(i);
            this.buttonList.add(new GuiButton(100 + i, this.width / 2 - 100, 30 + i * 25, acc.getUsername()));
        }

        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height - 60, "Adicionar nova conta"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height - 30, "Voltar"));
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 100 && button.id < 100 + accounts.size()) {
            AuthAccount selected = accounts.get(button.id - 100);
            AuthManager.setCurrentAccount(selected);
            mc.displayGuiScreen(parent);
        }

        if (button.id == 1) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        AuthAccount acc = new MicrosoftAuthenticator().login();
                        AuthManager.addAccount(acc);
                        AuthStorage.saveAccounts(AuthManager.getAccounts());
                        mc.displayGuiScreen(new GuiAuthManager(parent)); // Recarrega lista
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        if (button.id == 2) {
            mc.displayGuiScreen(parent);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Gerenciador de Contas Microsoft", this.width / 2, 10, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

