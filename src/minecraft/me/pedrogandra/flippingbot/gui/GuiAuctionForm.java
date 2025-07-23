package me.pedrogandra.flippingbot.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.pedrogandra.flippingbot.auction.AuctionItem;
import me.pedrogandra.flippingbot.module.AutoBIN;
import me.pedrogandra.flippingbot.utils.IOManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.Gui;

public class GuiAuctionForm extends GuiScreen {

    private GuiTextField name;
    private GuiTextField rarity;
    private GuiTextField level;
    private GuiButton excludeRecomb;
    private GuiTextField gearScore;
    private GuiTextField specs;
    private GuiTextField price;
    private GuiButton saveButton;
    private GuiButton cancelButton;
    private int actualWidth, actualHeight;
    private int idItem = -1;
    private IOManager io = new IOManager();
    private AuctionItem item;
    public boolean lockScreen;

    private boolean isRecombExcluded;

    public GuiAuctionForm() {}
    
    public GuiAuctionForm(int idItem) {
    	this.idItem = idItem;
    	this.item = AutoBIN.itemList.get(idItem);
    	this.isRecombExcluded = item.isExcludeRecomb();
    }

    private void loadItem(AuctionItem item) {
        if (item != null) {
            if (item.getName() != null && !item.getName().isEmpty())
                name.setText(item.getName());

            if (item.getRarity() != null && !item.getRarity().isEmpty())
                rarity.setText(item.getRarity());

            if (item.getLevel() > 0)
                level.setText(String.valueOf(item.getLevel()));

            if (item.getGearScore() != null && !item.getGearScore().isEmpty()) {
                String gs = item.getGearScore().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                gearScore.setText(gs);
            }

            if (item.getSpecs() != null && !item.getSpecs().isEmpty()) {
                specs.setText(String.join(",", item.getSpecs()));
            }

            if (item.getPrice() > 0) {
            	DecimalFormat df = new DecimalFormat("#,###");
    			String priceFormat = df.format(item.getPrice());
                price.setText(priceFormat);
            }

        }
    }


    @Override
    public void initGui() {
    	actualWidth = 500;
    	actualHeight = 400;
        int cx = width / 2;
        int cy = height/2;
        int w = 350;
        int h = 20;
        int spacing = 35;

        int y = cy - actualHeight/3;

        this.name = new GuiTextField(0, this.fontRendererObj, cx - w / 2, y, w, h);
        y += spacing;
        this.rarity = new GuiTextField(1, this.fontRendererObj, cx - w / 2, y, w, h);
        y += spacing;
        this.level = new GuiTextField(2, this.fontRendererObj, cx - w / 2, y, w, h);
        y += spacing;
        this.gearScore = new GuiTextField(3, this.fontRendererObj, cx - w / 2, y, w, h);
        y += spacing;
        this.specs = new GuiTextField(4, this.fontRendererObj, cx - w / 2, y, w, h);
        y += spacing;
        this.price = new GuiTextField(5, this.fontRendererObj, cx - w / 2, y, w, h);
        y += spacing;
        
        this.excludeRecomb = new GuiButton(6, cx - w/4, y, w/2, h, getRecombText());

        this.buttonList.clear();
        
        y += spacing;

        this.cancelButton = new GuiButton(8, cx - 102, cy + actualHeight/2 - 35, 100, h, "Cancel");
        this.saveButton = new GuiButton(7, cx + 2, cy + actualHeight/2 - 35, 100, h, "Save");

        this.buttonList.add(excludeRecomb);
        this.buttonList.add(cancelButton);
        this.buttonList.add(saveButton);
        
        if(this.idItem != -1) {
        	loadItem(item);
        }
        
        lockScreen=true;
    }

    private String getRecombText() {
        return "Recomb: " + (isRecombExcluded ? "No" : "Yes");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 6) {
            isRecombExcluded = !isRecombExcluded;
            excludeRecomb.displayString = getRecombText();
        } else if (button.id == 7) {
            AuctionItem item = getAuctionItemFromForm();
            if(item!=null) {
	            if(this.idItem!=-1)
	            	AutoBIN.itemList.set(idItem, item);
	            else
	            	AutoBIN.itemList.add(item);
	            AutoBIN.instance.saveItemList();
	            io.sendChat("Item was saved");
            }
            lockScreen = false;
            mc.displayGuiScreen(null);
        } else if(button.id == 8) {
        	lockScreen = false;
            mc.displayGuiScreen(null);
        }
    }

    private AuctionItem getAuctionItemFromForm()  {
    	try {
	        String n = name.getText().trim();
	        String r = rarity.getText().trim().toUpperCase();
	
	        int lvl = 0;
	        if(level.getText() != "")
	        	lvl = Integer.parseInt(level.getText().trim());
	
	        int[] gs = Arrays.stream(gearScore.getText().split(","))
	                .filter(s -> !s.isEmpty())
	                .mapToInt(s -> {
	                    try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
	                }).toArray();
	
	        String[] spc = Arrays.stream(specs.getText().split(","))
	                .map(String::trim)
	                .filter(s -> !s.isEmpty())
	                .toArray(String[]::new);
	
	        float pr = Float.parseFloat(price.getText().trim().replace(".", ""));
	        return new AuctionItem(n, r, isRecombExcluded, lvl, gs, spc, pr);
	        
    	} catch(Exception e) {
    		io.sendError("Invalid information for item");
    		e.printStackTrace();
    		return null;
    	}
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int cx = width / 2;
        int cy = height / 2;
        int spacing = 35;

        drawRect(cx - actualWidth/2, cy - actualHeight/2, cx + actualWidth/2, cy + actualHeight/2, 0xAA000000);
        drawCenteredString(this.fontRendererObj, "Item Form", cx, cy - actualHeight/2 + 10, 0xFFFFFF);

        int labelX = cx - actualWidth/2 + 25;
        int y = cy - actualHeight/3;

        int newX = this.fontRendererObj.drawString("Name:", labelX, y + 6, 0xFFFFFF);
        name.xPosition = newX + 10;
        name.setMaxStringLength(256);
        name.drawTextBox();
        y += spacing;

        newX = this.fontRendererObj.drawString("Rarity:", labelX, y + 6, 0xFFFFFF);
        rarity.xPosition = newX + 10;
        rarity.drawTextBox();
        y += spacing;

        newX = this.fontRendererObj.drawString("Level:", labelX, y + 6, 0xFFFFFF);
        level.xPosition = newX + 10;
        level.drawTextBox();
        y += spacing;

        newX = this.fontRendererObj.drawString("Gear Score:", labelX, y + 6, 0xFFFFFF);
        gearScore.xPosition = newX + 10;
        gearScore.drawTextBox();
        y += spacing;

        newX = this.fontRendererObj.drawString("Specs:", labelX, y + 6, 0xFFFFFF);
        specs.xPosition = newX + 10;
        specs.setMaxStringLength(256);
        specs.drawTextBox();
        y += spacing;

        newX = this.fontRendererObj.drawString("Price:", labelX, y + 6, 0xFFFFFF);
        price.xPosition = newX + 10;
        price.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!name.textboxKeyTyped(typedChar, keyCode) &&
            !rarity.textboxKeyTyped(typedChar, keyCode) &&
            !level.textboxKeyTyped(typedChar, keyCode) &&
            !gearScore.textboxKeyTyped(typedChar, keyCode) &&
            !specs.textboxKeyTyped(typedChar, keyCode) &&
            !price.textboxKeyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        name.mouseClicked(mouseX, mouseY, mouseButton);
        rarity.mouseClicked(mouseX, mouseY, mouseButton);
        level.mouseClicked(mouseX, mouseY, mouseButton);
        gearScore.mouseClicked(mouseX, mouseY, mouseButton);
        specs.mouseClicked(mouseX, mouseY, mouseButton);
        price.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        name.updateCursorCounter();
        rarity.updateCursorCounter();
        level.updateCursorCounter();
        gearScore.updateCursorCounter();
        specs.updateCursorCounter();
        price.updateCursorCounter();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
