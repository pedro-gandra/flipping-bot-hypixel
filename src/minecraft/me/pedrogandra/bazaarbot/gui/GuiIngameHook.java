package me.pedrogandra.bazaarbot.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import me.pedrogandra.bazaarbot.BazaarBot;
import me.pedrogandra.bazaarbot.api.util.BazaarItem;
import me.pedrogandra.bazaarbot.module.AutoBazaar;
import me.pedrogandra.bazaarbot.module.Module;
import me.pedrogandra.bazaarbot.utils.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.ChatComponentText;

public class GuiIngameHook extends GuiIngame {
	
	private Minecraft mc = Minecraft.getMinecraft();
	private BazaarBot bb = BazaarBot.instance;
	private AutoBazaar bz = AutoBazaar.instance;
	public static BazaarItem item;
	private static int currentItemIndex = 0;
	
	public GuiIngameHook(Minecraft mcIn) {
		super(mcIn);
	}
	
	public void renderGameOverlay(float p_175180_1_){
	      super.renderGameOverlay(p_175180_1_);
	      
	      String text = bb.name + " v" + bb.version + " by " + bb.creator;
	      
	      drawCenterTextRect(text, 0, 0, 200, 25, 0x90000000, 0xAE0000);
	      
	      renderArrayList();
	      renderItemCards();
	} 
	
	private void renderArrayList() {
		int yCount = 29;
		for (Module m : bb.moduleManager.getModules()) {
			m.onRender();
			if(m.isToggled()) {
				String nm = m.getName();
				drawCenterTextRect(nm, 4, yCount, 96, 19, 0x90000000, 0xFFFFFF);
				yCount+=23;
			}
			
		}
	}
	
	public void drawCenterTextRect(String text, int rectX, int rectY, int rectWidth, int rectHeight, int rectColor, int textColor) {
	    drawRect(rectX, rectY, rectX + rectWidth, rectY + rectHeight, rectColor);
	    
	    int textWidth = Wrapper.fr.getStringWidth(text);
	    int textHeight = Wrapper.fr.FONT_HEIGHT;
	    
	    int x = rectX + (rectWidth - textWidth) / 2;
	    int y = rectY + (rectHeight - textHeight) / 2;
	    
	    Wrapper.fr.drawString(text, x, y, textColor);
	}
	
	
	private void renderItemCards() {
		int index = bz.getCurrentIndex();
		BazaarItem item = bz.getSpecificItem(index);
	    if (item == null) return;
	    BazaarItem.QuickStatus q = item.getQuickStatus();

	    int x = 10;
	    int y = 120;
	    int width = 180;
	    int height = 90;
	    drawRect(x, y, x + width, y + height, 0x80000000);
	    
	    drawCenterTextRect("Item: " + item.getProductId(), x, y + 10, width, 15, 0x90000000, 0xFFFFFF);
	    drawCenterTextRect("Buy: " + q.getBuyPrice(), x, y + 30, width, 15, 0x90000000, 0x00FF00);
	    drawCenterTextRect("Sell: " + q.getSellPrice(), x, y + 50, width, 15, 0x90000000, 0xFF5555);
	    drawCenterTextRect("Spread: " + (q.getSellPrice() - q.getBuyPrice()), x, y + 70, width, 15, 0x90000000, 0xFFFF00);
	}

}
