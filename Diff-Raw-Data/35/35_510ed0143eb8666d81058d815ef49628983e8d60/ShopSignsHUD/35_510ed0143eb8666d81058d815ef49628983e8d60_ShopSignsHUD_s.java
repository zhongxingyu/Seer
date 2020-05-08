 package com.qzx.au.hud;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockSign;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntitySign;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.world.World;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import org.lwjgl.opengl.GL11;
 
 import com.qzx.au.util.UI;
 
 @SideOnly(Side.CLIENT)
 public class ShopSignsHUD {
 	private UI ui = new UI();
 	private Pattern shopSignBuy = Pattern.compile("B[0-9.]+");
 	private Pattern shopSignSell = Pattern.compile("S[0-9.]+");
 	private Pattern shopSignID = Pattern.compile("(X[0-9]+|:[0-9]+)");
 
 	public ShopSignsHUD(){}
 
 	//////////
 
 	public void draw(Minecraft mc, ScaledResolution screen, EntityPlayer player){
 		GL11.glPushMatrix();
 
 		World world = mc.isIntegratedServerRunning() ? mc.getIntegratedServer().worldServerForDimension(player.dimension) : mc.theWorld;
 		if(world == null) return;
 
 		// block at cursor
 		if(Cfg.enable_shop_signs_hud){
 			if(mc.objectMouseOver != null){
 				if(mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE){
 					int blockID = world.getBlockId(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
 					if(blockID > 0){
 						Block block = Block.blocksList[blockID];
 						int inspectX = mc.objectMouseOver.blockX;
 						int inspectY = mc.objectMouseOver.blockY;
 						int inspectZ = mc.objectMouseOver.blockZ;
 						if(block != null){
 							if(block instanceof BlockSign){
 								try {
 									TileEntity tileEntity = world.getBlockTileEntity(inspectX, inspectY, inspectZ);
 									if(tileEntity instanceof TileEntitySign){
 										String[] signText = ((TileEntitySign)tileEntity).signText;
										int quantity = Integer.parseInt(signText[1].trim()); // second line = quantity
 										if(quantity > 0){
 											float buyPrice = 0.0F, sellPrice = 0.0F;
 											signText[2] = signText[2].replace(" ", "");
 											Matcher matchBuy = shopSignBuy.matcher(signText[2]); // third line = buy price
 											Matcher matchSell = shopSignSell.matcher(signText[2]); // third line = sell price
 											if(matchBuy.find())
												buyPrice = Float.parseFloat(signText[2].substring(matchBuy.start()+1, matchBuy.end()));
 											if(matchSell.find())
												sellPrice = Float.parseFloat(signText[2].substring(matchSell.start()+1, matchSell.end()));
 											if(buyPrice > 0 || sellPrice > 0){
 												int itemID = 0, itemDamage = 0;
 												signText[3] = signText[3].trim();
 												if(signText[3].charAt(0) == 'X'){
 													Matcher matchID = shopSignID.matcher(signText[3]); // fourth line = item name or ID
 													if(matchID.find()){
														itemID = Integer.parseInt(signText[3].substring(matchID.start()+1, matchID.end()));
 														if(itemID > 0)
 															if(matchID.find())
 																if(signText[3].charAt(matchID.start()) == ':')
																	itemDamage = Integer.parseInt(signText[3].substring(matchID.start()+1, matchID.end()));
 													}
 												} else {
 // TODO: query item by name in signText[3]
 												}
 
 												ItemStack itemstack = new ItemStack(itemID, quantity, itemDamage);
 												int centerX = screen.getScaledWidth()/2;
 												int centerY = screen.getScaledHeight()/2;
 												int offset = 32;
 												this.ui.setCursor(centerX, centerY-offset);
 
 												if(itemID > 0 && itemstack != null){
 													// show item name above sign
 													String itemName = ItemUtils.getDisplayName(itemstack);
 													this.ui.drawString(UI.ALIGN_CENTER, itemName, "<Unknown>", 0xffffff, 0);
 
 													// show item icon above sign
 													RenderItem itemRenderer = new RenderItem();
 													itemRenderer.zLevel = 200.0F;
 													GL11.glTranslatef(0.0F, 0.0F, 32.0F);
 													GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 													GL11.glEnable(32826); // GL_RESCALE_NORMAL_EXT + GL_RESCALE_NORMAL_EXT
 													RenderHelper.enableStandardItemLighting();
 													RenderHelper.enableGUIStandardItemLighting();
 													try {
 														itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, centerX-8, centerY-(offset+19));
 														#ifdef MC147
 														itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, centerX-8, centerY-(offset+19));
 														#else
 														itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, centerX-8, centerY-(offset+19), null);
 														#endif
 													} catch(Exception e){
 														Failure.log("shop signs, render icon");
 													}
 													RenderHelper.disableStandardItemLighting();
 													GL11.glDisable(32826); // GL_RESCALE_NORMAL_EXT + GL_RESCALE_NORMAL_EXT
 												} else {
 													// show item name and quantity above sign
 													this.ui.drawString(UI.ALIGN_CENTER, signText[3], "<Unknown>", 0xffffff, 0);
 													this.ui.lineBreak(-13);
 													this.ui.drawString(UI.ALIGN_CENTER, String.format("%d", quantity), 0xffffff, 0);
 												}
 
 												// show buy/sell prices below sign, and price per unit
 												this.ui.setCursor(centerX, centerY-(offset+32));
 												if(buyPrice > 0){
 													String buy = (quantity > 1
 														? String.format("You pay: $%.2f ($%.2f each)", buyPrice, buyPrice/(float)quantity)
 														: String.format("You pay: $%.2f", buyPrice));
 													this.ui.x = centerX - mc.fontRenderer.getStringWidth(buy)/2;
 													this.ui.drawString("You pay: $", 0xaaaaaa);
 													this.ui.drawString(String.format("%.2f", buyPrice), 0xff6666);
 													if(quantity > 1){
 														this.ui.drawString(" (", 0xaaaaaa);
 														this.ui.drawString(String.format("%.2f", buyPrice/(float)quantity), 0xff6666);
 														this.ui.drawString(" each)", 0xaaaaaa);
 													}
 													this.ui.lineBreak(-10);
 												}
 												if(sellPrice > 0){
 													String sell = (quantity > 1
 														? String.format("You make: $%.2f ($%.2f each)", sellPrice, sellPrice/(float)quantity)
 														: String.format("You make: $%.2f", sellPrice));
 													this.ui.x = centerX - mc.fontRenderer.getStringWidth(sell)/2;
 													this.ui.drawString("You make: $", 0xaaaaaa);
 													this.ui.drawString(String.format("%.2f", sellPrice), 0x66ff66);
 													if(quantity > 1){
 														this.ui.drawString(" (", 0xaaaaaa);
 														this.ui.drawString(String.format("%.2f", sellPrice/(float)quantity), 0x66ff66);
 														this.ui.drawString(" each)", 0xaaaaaa);
 													}
 												}
 
 												// buy/sell help
 												this.ui.setCursor(centerX, centerY+offset);
 												if(buyPrice > 0){
 													this.ui.drawString(UI.ALIGN_CENTER, "Right-click (use item) to buy", 0xaaaaaa, 0);
 													this.ui.lineBreak(10);
 												}
 												if(sellPrice > 0)
 													this.ui.drawString(UI.ALIGN_CENTER, "Left-click (attack) to sell", 0xaaaaaa, 0);
 											}
 										}
																	
 									}
 								} catch (Exception e){
 									Failure.log("shop signs");
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		GL11.glPopMatrix();
 	}
 }
