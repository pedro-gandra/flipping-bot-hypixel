package me.pedrogandra.bazaarbot.utils;

import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class MCUtils {
	
	private final Minecraft mc = Minecraft.getMinecraft();
	
	public static String cleanText(String text) {
	    return text.replaceAll("§[0-9a-fk-or]", "");
	}
	
	public static void clickButton(String label) {
		
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        if (currentScreen == null) return;

        for (Object obj : currentScreen.buttonList) {
            if (obj instanceof GuiButton) {
                GuiButton button = (GuiButton) obj;
                if (button.displayString != null && cleanText(button.displayString).equalsIgnoreCase(label)) {
                	try {
                        Method method = GuiScreen.class.getDeclaredMethod("actionPerformed", GuiButton.class);
                        method.setAccessible(true);
                        method.invoke(currentScreen, button);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
    }
}
