package me.pedrogandra.bazaarbot.gui;

import me.pedrogandra.bazaarbot.BazaarBot;
import me.pedrogandra.bazaarbot.module.Module;
import me.pedrogandra.bazaarbot.utils.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;

public class GuiIngameHook extends GuiIngame {

	private BazaarBot bb = BazaarBot.instance;
	
	public GuiIngameHook(Minecraft mcIn) {
		super(mcIn);
	}
	
	public void renderGameOverlay(float p_175180_1_){
	      super.renderGameOverlay(p_175180_1_);
	      
	      String text = bb.name + " v" + bb.version + " by " + bb.creator;
	      
	      drawCenterTextRect(text, 0, 0, 200, 25, 0x90000000, 0xAE0000);
	      
	      renderArrayList();
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

}
