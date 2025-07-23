package me.pedrogandra.flippingbot.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import me.pedrogandra.flippingbot.FlippingBot;
import me.pedrogandra.flippingbot.auction.AuctionItem;
import me.pedrogandra.flippingbot.bazaar.BazaarItem;
import me.pedrogandra.flippingbot.module.AutoBIN;
import me.pedrogandra.flippingbot.module.AutoBazaar;
import me.pedrogandra.flippingbot.module.Module;
import me.pedrogandra.flippingbot.utils.IOManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.*;
import net.minecraft.util.ChatComponentText;

public class GuiIngameHook extends GuiIngame {
	
	private Minecraft mc = Minecraft.getMinecraft();
	private FontRenderer fr = mc.fontRendererObj;
	private FlippingBot bb = FlippingBot.instance;
	private GuiUtil gu = new GuiUtil();
	public static AutoBazaar bz;
	public static AutoBIN bin;
	private IOManager io = new IOManager();
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
	      GuiScreen current = mc.currentScreen;
	      
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
	    
	    int textWidth = fr.getStringWidth(text);
	    int textHeight = fr.FONT_HEIGHT;
	    
	    int x = rectX + (rectWidth - textWidth) / 2;
	    int y = rectY + (rectHeight - textHeight) / 2;
	    
	    fr.drawString(text, x, y, textColor);
	}
	
	public void renderAuctionItems() {
		ArrayList<AuctionItem> list = AutoBIN.itemList;
		int scaledWidth = new ScaledResolution(mc).getScaledWidth();
		int scaledHeight = new ScaledResolution(mc).getScaledHeight();
		int x = (int) (scaledWidth*0.70);
		int y = 0;
		drawRect(x, y, scaledWidth, scaledHeight, 0x90000000);
		y+=10;
		int size = list.size();
		fr.drawString("Total de itens: " + size, x+10, y, 0xFFFFFF);
		y+=25;
		for(int i = AutoBIN.displayListStart; i < size; i++) {
			if(y+80 >= scaledHeight) break;
			int newX = x + 10;
			AuctionItem item = list.get(i);
			String name = item.getName();
			String rarity = item.getRarity();
			float price = item.getPrice();
			ArrayList<Integer> gs = item.getGearScore();
			ArrayList<String> specs = item.getSpecs();
			int lvl = item.getLevel();
			newX = fr.drawString(i +". " + item.getName() + " - ", newX, y, 0xFFFFFF);
			newX+=4;
			newX = fr.drawStringScale(rarity, newX, y, gu.color(rarity), 0.9f);
			if(item.isExcludeRecomb()) {
				newX+=4;
				newX = fr.drawStringScale("(no recomb)", newX, y, gu.color("gray"), 0.85f);
			}
			newX = x+12;
			if(lvl!=0) {
				y+=fr.FONT_HEIGHT+4;
				fr.drawStringScale("Level: " + lvl + "+", newX, y, gu.color("gray"), 0.85f);
			}
			if(!gs.isEmpty()) {
				y+=fr.FONT_HEIGHT+4;
				fr.drawStringScale("Gear Score: ("+ gs.get(0) + ") / (" + gs.get(1) + ")", newX, y, gu.color("gray"), 0.85f);
			}
			if(!specs.isEmpty()) {
				for(String s : specs) {
					y+=fr.FONT_HEIGHT+4;
					fr.drawStringScale(s, newX, y, gu.color("yellow"), 0.85f);
				}
			}
			y+=fr.FONT_HEIGHT+4;
			DecimalFormat df = new DecimalFormat("#,###");
			String priceFormat = df.format(item.getPrice());
			fr.drawString(priceFormat, newX, y, 0x0aec20);
			y+=fr.FONT_HEIGHT+10;
			drawRect(x, y, scaledWidth, y+1, 0x60FFFFFF);
			y+=10;
		}
		
	}
	
	private void renderItemCards() {
		int index = bz.getCurrentIndex();
		BazaarItem item = bz.getItemAt(index);
	    if (item == null) return;
	    BazaarItem.QuickStatus q = item.getQuickStatus();
	    double bestBuy = item.getBestBuy();
	    double bestSell = item.getBestSell();
	    double spread = bestBuy - bestSell;
	    double hourlyLiquidity = item.getHourlyLiquidity();
	    int nb = item.validation.buyOrdersCount;
	    int ns = item.validation.sellOrdersCount;

	    int x = 650;
	    int y = 10;
	    int width = 180;
	    drawCenterTextRect("Total de items: " + bz.getCurrentItems().size(), x, y, width, 15, 0x90000000, 0xFFFFFF);
	    y+=15;
	    drawCenterTextRect("Item: " + item.getDisplayName(), x, y, width, 15, 0x90000000, 0xFFFFFF);
	    y+=15;
	    drawCenterTextRect("Price: " + io.formatDouble(bestSell), x, y, width, 15, 0x90000000, 0xFFFFFF);
	    y+=15;
	    drawCenterTextRect("Spread: " + io.formatDouble(spread), x, y, width, 15, 0x90000000, 0x00FF00);
	    y+=15;
	    drawCenterTextRect("Margin: " + io.formatDouble(bestBuy/bestSell), x, y, width, 15, 0x90000000, 0xFF5555);
	    y+=15;
	    drawCenterTextRect("items per hour: " + io.formatDouble(hourlyLiquidity), x, y, width, 15, 0x90000000, 0xFF5555);
	    y+=15;
	    drawCenterTextRect("Perfect profit: " + io.formatDouble(hourlyLiquidity*spread), x, y, width, 15, 0x90000000, 0xFF5555);
	    y+=15;
	    drawCenterTextRect("Order increase: " + nb + " - " + ns, x, y, width, 15, 0x90000000, 0xFF5555);
	}

}
