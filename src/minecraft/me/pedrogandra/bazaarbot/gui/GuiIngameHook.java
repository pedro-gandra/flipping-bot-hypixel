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
	    double bestBuy = item.getBestBuy();
	    double bestSell = item.getBestSell();
	    double spread = bestBuy - bestSell;
	    double hourlyLiquidity = item.getHourlyLiquidity();

	    int x = 500;
	    int y = 10;
	    int width = 180;
	    drawCenterTextRect("Total de items: " + bz.getCurrentItems().size(), x, y, width, 15, 0x90000000, 0xFFFFFF);
	    y+=15;
	    drawCenterTextRect("Item: " + item.getDisplayName(), x, y, width, 15, 0x90000000, 0xFFFFFF);
	    y+=15;
	    drawCenterTextRect("Price: " + bestSell, x, y, width, 15, 0x90000000, 0xFFFFFF);
	    y+=15;
	    drawCenterTextRect("Spread: " + spread, x, y, width, 15, 0x90000000, 0x00FF00);
	    y+=15;
	    drawCenterTextRect("Margin: " + (bestBuy/bestSell), x, y, width, 15, 0x90000000, 0xFF5555);
	    y+=15;
	    drawCenterTextRect("items per hour: " + hourlyLiquidity, x, y, width, 15, 0x90000000, 0xFF5555);
	    y+=15;
	    drawCenterTextRect("Perfect profit: " + hourlyLiquidity*spread, x, y, width, 15, 0x90000000, 0xFF5555);
	}

}
