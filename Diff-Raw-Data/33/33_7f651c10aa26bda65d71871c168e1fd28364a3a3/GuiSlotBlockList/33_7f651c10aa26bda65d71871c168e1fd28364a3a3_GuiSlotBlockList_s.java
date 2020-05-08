 package org.yogpstop.tof;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.GuiSlot;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 public class GuiSlotBlockList extends GuiSlot {
 	private static final List<ItemStack> blocklist = new ArrayList<ItemStack>();
 	private GuiSelectBlock parent;
	public int currentblockid;
 	public int currentmeta;
 
 	static {
 		for (int i = 0; i < 4096; i++) {
 			if (Block.blocksList[i] != null) {
 				if (Item.itemsList[i] != null) {
 					if (!(Block.blocksList[i].getBlockName() == null && Item.itemsList[i]
 							.getItemName() != null)) {
 						Block.blocksList[i].getSubBlocks(i, null, blocklist);
 					}
 				} else {
 					Block.blocksList[i].getSubBlocks(i, null, blocklist);
 				}
 			}
 		}
 		for (int i = 0; i < blocklist.size(); i++) {
 			if (blocklist.get(i).itemID == Block.stone.blockID) {
 				blocklist.remove(i);
 				i--;
 				continue;
 			}
 			for (int j = 0; j < TimesOreForge.setting.size(); j++) {
 				if (blocklist.get(i).itemID == TimesOreForge.setting.get(j).blockID
 						&& blocklist.get(i).getItemDamage() == TimesOreForge.setting
 								.get(j).meta) {
 					blocklist.remove(i);
 					i--;
 					continue;
 				}
 			}
 		}
 	}
 
 	public GuiSlotBlockList(Minecraft par1Minecraft, int par2, int par3,
 			int par4, int par5, int par6, GuiSelectBlock parents) {
 		super(par1Minecraft, par2, par3, par4, par5, par6);
 
 		parent = parents;
 	}
 
 	@Override
 	protected int getSize() {
 		return blocklist.size();
 	}
 
 	@Override
 	protected void elementClicked(int var1, boolean var2) {
		currentblockid = blocklist.get(var1).itemID;
 		currentmeta = blocklist.get(var1).getItemDamage();
 	}
 
 	@Override
 	protected boolean isSelected(int var1) {
 		return blocklist.get(var1).itemID == currentblockid
 				&& currentmeta == blocklist.get(var1).getItemDamage();
 	}
 
 	@Override
 	protected void drawBackground() {
 		this.parent.drawDefaultBackground();
 	}
 
 	@Override
 	protected void drawSlot(int var1, int var2, int var3, int var4,
 			Tessellator var5) {
		String name = TimesOreForge.getname(blocklist.get(var1).itemID,
 				blocklist.get(var1).getItemDamage());
 		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name,
 				(this.parent.width - Minecraft.getMinecraft().fontRenderer
 						.getStringWidth(name)) / 2, var3 + 1, 0xFFFFFF);
 	}
 }
