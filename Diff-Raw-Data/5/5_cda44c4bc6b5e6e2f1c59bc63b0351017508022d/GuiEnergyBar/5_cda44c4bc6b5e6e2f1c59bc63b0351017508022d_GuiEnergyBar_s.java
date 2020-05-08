 package com.isocraft.client.gui.utils;
 
 import java.util.Arrays;
 
 import net.minecraft.client.Minecraft;
 
 import com.isocraft.client.gui.GuiISOCraft;
 import com.isocraft.lib.Reference;
 import com.isocraft.tileentity.TileEntityISOCraftMachine;
 
 public class GuiEnergyBar {
 
 	public static void draw(GuiISOCraft gui, TileEntityISOCraftMachine tile, Minecraft mc, int xpos, int ypos, int xcur, int ycur) {
 		mc.getTextureManager().bindTexture(Reference.Gui_Utils_loc);
 		double e = ((double) tile.getEnergyStored(null) / tile.getMaxEnergyStored(null));
 		int energyPos = (int) Math.floor(e * 58);
		gui.drawTexturedModalRect(xpos, ypos - energyPos, 0, (57 - energyPos), 5, energyPos);
 
		String tooltip = tile.getEnergyStored(null) + "/12000 rf";
 		gui.drawTooltip(Arrays.asList(new String[] { tooltip }), 164, 13, xcur, ycur, 5, 58);
 	}
 }
