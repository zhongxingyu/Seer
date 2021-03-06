 package maxpowa.tukmc;
 
 import static maxpowa.tukmc.TukMCReference.BOX_EFFECT_OUTLINE_COLOR;
 import static maxpowa.tukmc.TukMCReference.BOX_HIGHLIGHT_COLOR;
 import static maxpowa.tukmc.TukMCReference.BOX_INNER_COLOR;
 import static maxpowa.tukmc.TukMCReference.BOX_OUTLINE_COLOR;
 import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.AIR;
 import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ALL;
 import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.CROSSHAIRS;
 import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FOOD;
 import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HEALTH;
 import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HELMET;
 import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.PORTAL;
 import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
 import static org.lwjgl.opengl.GL11.GL_BLEND;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
 import static org.lwjgl.opengl.GL11.GL_DST_COLOR;
 import static org.lwjgl.opengl.GL11.GL_FLAT;
 import static org.lwjgl.opengl.GL11.GL_GREATER;
 import static org.lwjgl.opengl.GL11.GL_LEQUAL;
 import static org.lwjgl.opengl.GL11.GL_LIGHTING;
 import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_DST_COLOR;
 import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_COLOR;
 import static org.lwjgl.opengl.GL11.GL_SMOOTH;
 import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.GL_ZERO;
 import static org.lwjgl.opengl.GL11.glBindTexture;
 import static org.lwjgl.opengl.GL11.glBlendFunc;
 import static org.lwjgl.opengl.GL11.glColor3f;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glDepthFunc;
 import static org.lwjgl.opengl.GL11.glDepthMask;
 import static org.lwjgl.opengl.GL11.glDisable;
 import static org.lwjgl.opengl.GL11.glEnable;
 import static org.lwjgl.opengl.GL11.glPopMatrix;
 import static org.lwjgl.opengl.GL11.glPushMatrix;
 import static org.lwjgl.opengl.GL11.glScalef;
 import static org.lwjgl.opengl.GL11.glShadeModel;
 import static org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL;
 
 import java.awt.Color;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import tarun1998.thirstmod.client.StatsHolder;
 
 import maxpowa.codebase.client.ClientUtils;
 import maxpowa.codebase.common.ColorCode;
 import maxpowa.codebase.common.CommonUtils;
 import maxpowa.codebase.common.FormattingCode;
 import maxpowa.tukmc.McMMOIntegration.LevelUpData;
 import maxpowa.tukmc.McMMOIntegration.SkillData;
 import maxpowa.tukmc.McMMOIntegration.SkillData.UsageType;
 import net.minecraft.client.Minecraft;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.boss.BossStatus;
 import net.minecraft.world.chunk.Chunk;
 import net.minecraft.scoreboard.Score;
 import net.minecraft.scoreboard.ScoreObjective;
 import net.minecraft.scoreboard.ScorePlayerTeam;
 import net.minecraft.scoreboard.Scoreboard;
 import net.minecraft.src.ModLoader;
 import net.minecraft.stats.StatBase;
 import net.minecraft.stats.StatFileWriter;
 import net.minecraft.stats.StatList;
 import net.minecraft.util.Direction;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.FoodStats;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.world.EnumSkyBlock;
 import net.minecraft.world.World;
 import net.minecraft.client.gui.FontRenderer;
 import net.minecraft.client.gui.GuiPlayerInfo;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemBow;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.block.material.Material;
 import net.minecraft.util.MathHelper;
 import net.minecraft.client.multiplayer.NetClientHandler;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.RenderEngine;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.client.gui.ScaledResolution;
 import net.minecraft.client.gui.achievement.GuiStats;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.util.StatCollector;
 import net.minecraft.client.renderer.Tessellator;
 
 import net.minecraftforge.client.ForgeHooksClient;
 import net.minecraftforge.client.GuiIngameForge;
 import net.minecraftforge.client.event.RenderGameOverlayEvent;
 import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.common.IShearable;
 import net.minecraftforge.common.MinecraftForge;
 
 public class GuiIngame extends GuiIngameForge {
 
 	private int tooltipOpenFor;
 	private long rendersElapsed = 0;
 	private final Minecraft mc;
 	private String recordPlaying = "";
 	private int recordPlayingUpFor = 0;
 	private boolean recordIsPlaying = false;
 	private final GuiNewChat presistentChatGui;
 	private EntityPlayer p;
 	private World world;
 	private final RenderBlocks itemRenderBlocks = new RenderBlocks();
 	private static int update = 0;
     private final Random rand = new Random();
 	private int lastItem = 0;
 	private int tooltipSize = 0;
 	private int lastHealth = 0;
 	private int lastFood = 0;
 	private int lastXP = 0;
     private long debugUpdateTime = Minecraft.getSystemTime();
     
 	RenderItem ir = new RenderItem();
     
     //Flags to toggle the rendering of certain aspects of the HUD, valid conditions
     //must be met for them to render normally. If those conditions are met, but this flag
     //is false, they will not be rendered.
     public static boolean renderHelmet = true;
     public static boolean renderPortal = true;
     public static boolean renderHotbar = true;
     public static boolean renderCrosshairs = true;
     public static boolean renderBossHealth = true;
     public static boolean renderHealth = true;
     public static boolean renderArmor = true;
     public static boolean renderFood = true;
     public static boolean renderAir = true;
     public static boolean renderExperience = true;
 
     private ScaledResolution res = null;
     private RenderGameOverlayEvent eventParent;    
 
 	public GuiIngame() {
 		super(CommonUtils.getMc());
 		mc = CommonUtils.getMc();
 		presistentChatGui = new GuiNewChat(mc);
 	}
 
 	@Override
 	public void renderGameOverlay(final float partialTicks, final boolean hasScreen, final int mouseX, final int mouseY) {
 		if (Config.get(Config.NODE_CUSTOM_BARS)) {
 			++rendersElapsed;
 			res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
 	        eventParent = new RenderGameOverlayEvent(partialTicks, res, mouseX, mouseY);
 			
 	        if (pre(ALL)) return;
 			
 			final int height = res.getScaledHeight();
 			final int width = res.getScaledWidth();
 			final FontRenderer fr = mc.fontRenderer;
 			mc.entityRenderer.setupOverlayRendering();
 	        GL11.glEnable(GL11.GL_BLEND);
 
 			smoothBars();
 			
 			if (rendersElapsed == 10 && mod_TukMC.updateChecker && mod_TukMC.updateText != null && mod_TukMC.updateVersion != null && !mod_TukMC.updateVersion.equalsIgnoreCase(mod_TukMC.TK_VERSION)) {
 				mc.displayGuiScreen(new GuiUpdate(mc, false));
 			}
 	        
 	        if (Minecraft.isFancyGraphicsEnabled())
 	        {
 	            renderVignette(mc.thePlayer.getBrightness(partialTicks), width, height);
 	        }
 	        else
 	        {
 	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 	        }
 
 	        if (renderHelmet) renderHelmet(res, partialTicks, hasScreen, mouseX, mouseY);
 
 	        if (renderPortal && !mc.thePlayer.isPotionActive(Potion.confusion))
 	        {
 	            renderPortal(width, height, partialTicks);
 	        }
 	        
 	        if (!mc.playerController.enableEverythingIsScrewedUpMode())
 	        {
 	            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 	            zLevel = -90.0F;
 	            rand.setSeed((long)(updateCounter * 312871));
 	            mc.renderEngine.bindTexture("/gui/icons.png");
 
 	            if (renderCrosshairs) renderCrosshairs(width, height);
 	            if (renderBossHealth) drawBossBar(fr, width, height);
 
 	            if (this.mc.playerController.shouldDrawHUD())
 	            {
 	                if (renderHealth) renderHealth(width, height);
 	                if (renderFood)   renderFood(width, height);
 	                if (renderAir)    renderAir(width, height);
 	                if (renderExperience) renderExperience(width, height);
 	            }
 	            if (renderHotbar) renderHotbar(width, height, partialTicks);
 	        }
 	        
 	        GL11.glPushMatrix();
 	        GL11.glEnable(GL11.GL_BLEND);
 	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 	        GL11.glDisable(GL11.GL_ALPHA_TEST);
	        
			drawGenericStuff(fr, width, height, partialTicks);
 
 			drawLeftBar(fr, width, height);
 
 			drawRightBar(fr, width, height);
 
 			drawRecordDisplay(fr, width, height, partialTicks);
 
 			if (Config.get(Config.NODE_DANGER_DISPLAY) || Config.get(Config.NODE_TOP_BAR) || mc.gameSettings.showDebugInfo) {
 				final int posX = MathHelper.floor_double(mc.thePlayer.posX);
 				final int posY = MathHelper.floor_double(mc.thePlayer.posY);
 				final int posZ = MathHelper.floor_double(mc.thePlayer.posZ);
 				final Chunk chunk = mc.theWorld.getChunkFromBlockCoords(posX, posZ);
 				final String biomeName = chunk.getBiomeGenForWorldCoords(posX & 15, posZ & 15, mc.theWorld.getWorldChunkManager()).biomeName;
 				int blockLight = 0;
 				if (posY > 0 && posY < 256) {
 					blockLight = chunk.getSavedLightValue(EnumSkyBlock.Block, posX & 15, posY, posZ & 15);
 				}
 				final int direction = MathHelper.floor_double(mc.thePlayer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
 	
 				drawDebugInfo(fr, width, height, posX, posY, posZ, chunk, biomeName, blockLight, direction);
 				drawTopBar(fr, ir, width, height, biomeName);
 				drawDangerZone(fr, width, height, blockLight, ir);
 			}
 
 			drawMCMMO(fr, width, height);
 			
 			drawBlockAtPointer(fr, ir, width, height);
 
 			drawArrowCount(fr, width, height);
 
 			drawBossBar(fr, width, height);
 
 			drawBuffs(fr, width, height);
 			
 			drawStatsBoard(fr,width,height);
 
 			modIntegration(fr, width, height);
 			
 			tooltip: {
 				if (!(CommonUtils.getMc().currentScreen instanceof GuiChat) && (KeyRegister.showTooltipKB.pressed || Config.get(Config.NODE_TOOLTIP_ALWAYS_ON) || (Config.get(Config.NODE_TOOLTIPS) && (tooltipOpenFor > 0)))) {
 					final ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
 					if (stack == null) break tooltip;
 					final int loc = mc.thePlayer.inventory.currentItem;
 
 					int x = width / 2 - 88 + loc * 20;
 					int y = height - 20;
 
 					final List<String> tokensList = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
 					tooltipSize = (tokensList.size() * 12)-12;
 					if (tokensList.isEmpty()) break tooltip;
 
 					glPushMatrix();
 					glDisable(GL_DEPTH_TEST);
 
 					int lenght = 12;
 					for (final String s : tokensList)
 						lenght = Math.max(lenght, fr.getStringWidth(s));
 
 					
 					final int yOffset = 0;
 					if (Config.get(Config.NODE_TOOLTIPS) || Config.get(Config.NODE_TOOLTIP_ALWAYS_ON)) {
 						if (mc.playerController.isNotCreative()) {
 							y = y-20;
 							x = width/2-((lenght+4)/2);
 						} else {
 							x = width/2-((lenght+4)/2);
 						}
 					}
 					
 					drawDoubleOutlinedBox(x, y - tokensList.size() * 12 - 5, lenght + 4, tokensList.size() * 12, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 					int i = 1;
 					for (String s : tokensList) {
 						if (i == 1) s = "\u00a7" + Integer.toHexString(stack.getRarity().rarityColor) + s;
 						else s = "\u00a77" + s;
 						if (i == 1) fr.drawStringWithShadow(s, x + 2, y - (tokensList.size() + 1) * 12 + i * 12 - 3, 0xFFFFFF);
 						else fr.drawString(s, x + 2, y - (tokensList.size() + 1) * 12 + i * 12 - 3, 0xFFFFFF);
 						++i;
 					}
 					glEnable(GL_DEPTH_TEST);
 					glPopMatrix();
 				} else {
 					tooltipSize = 0;
 				}
 			}
 
 	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 	        GL11.glDisable(GL11.GL_LIGHTING);
 	        GL11.glEnable(GL11.GL_ALPHA_TEST);
 			GL11.glPopMatrix();
 			
 	        GL11.glEnable(GL11.GL_BLEND);
 	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 	        GL11.glDisable(GL11.GL_ALPHA_TEST);
 
 	        renderChat(width, height);
 
 	        renderPlayerList(width, height);
 
 	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 	        GL11.glDisable(GL11.GL_LIGHTING);
 	        GL11.glEnable(GL11.GL_ALPHA_TEST);
 		} else {
 			defaultHUD(partialTicks, hasScreen, mouseX, mouseY);
 		}
 	}
 	
     protected void renderChat(final int width, final int height)
     {
         GL11.glPushMatrix();
         mc.mcProfiler.startSection("chat");
 		presistentChatGui.drawChat(getUpdateCounter());
         mc.mcProfiler.endSection();
         GL11.glPopMatrix();
     }
 
 	protected void renderExperience(final int width, final int height) {
 		final int lvl = mc.thePlayer.experienceLevel;
 		final String lvlStr = ColorCode.BRIGHT_GREEN + "" + lvl;
 		if (lvl > 0) {
 			drawDoubleOutlinedBox(width / 2 - mc.fontRenderer.getStringWidth(lvlStr) / 2 - 1, height - 32, mc.fontRenderer.getStringWidth(lvlStr) + 2, 10, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			mc.fontRenderer.drawStringWithShadow(lvlStr, width / 2 - (mc.fontRenderer.getStringWidth(lvlStr) / 2), height - 31, 0xFFFFFF);
 		}
 		drawDoubleOutlinedBox(width / 2 + 10, height - 29, 80, 4, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 		drawSolidGradientRect(width / 2 + 10, height - 29, lastXP, 4, 0x05d714, 0x8fea96);
 		glPushMatrix();
 		glScalef(0.5F, 0.5F, 0.5F);
 		final int relativeXP = (int) Math.floor(mc.thePlayer.experience * mc.thePlayer.xpBarCap());
 		final String lvlXP = ColorCode.BRIGHT_GREEN + "" + relativeXP;
 		mc.fontRenderer.drawStringWithShadow(lvlXP + "/" + mc.thePlayer.xpBarCap(), (int) (width + 120 - (mc.fontRenderer.getStringWidth(lvlXP + "/" + mc.thePlayer.xpBarCap()))), height * 2 - 58, 0xFFFFFF);
 		glPopMatrix();
 	}
 	
 	protected void renderAir(final int width, final int height) {
         if (pre(AIR)) return;
         mc.mcProfiler.startSection("air");
 
 		if (Config.get(Config.NODE_PLAIN_STATUS)) {
 	        final int left = width / 2 + 91;
 	        final int top = height - 49;
 	
 	        if (mc.thePlayer.isInsideOfMaterial(Material.water))
 	        {
 	            final int air = mc.thePlayer.getAir();
 	            final int full = MathHelper.ceiling_double_int((double)(air - 2) * 10.0D / 300.0D);
 	            final int partial = MathHelper.ceiling_double_int((double)air * 10.0D / 300.0D) - full;
 	
 	            for (int i = 0; i < full + partial; ++i)
 	            {
 	                drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
 	            }
 	        }
 	
 	        mc.mcProfiler.endSection();
 	        post(AIR);
 		} else {
 			if (mc.thePlayer.isInsideOfMaterial(Material.water)) {
 				final int record = recordIsPlaying ? 20 : (5 + tooltipSize);
 				final int air = mc.thePlayer.getAir() + 20;
 				drawDoubleOutlinedBox(width / 2 - 80, height - 60 - record, 160, 5, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 				drawSolidGradientRect(width / 2 - 80, height - 60 - record, air / 2, 5, air < 60 ? 0xff1818 : 0x18cbff, air < 60 ? 0xff8c8c : 0x8ce5ff);
 				final String airStr = "Air:";
 				final int offset = (int) (air >= 60 ? 0 : Math.sin(rendersElapsed) * 10);
 				mc.fontRenderer.drawStringWithShadow(airStr, width / 2 - mc.fontRenderer.getStringWidth(airStr) / 2 + offset, height - 72 - record, 0xFFFFFF);
 			}
 		}
 	}
 
 	public void renderFood(final int width, final int height) {
         if (pre(FOOD)) return;
         mc.mcProfiler.startSection("food");
 
 		if (Config.get(Config.NODE_PLAIN_STATUS)) {
 	        final int left = width / 2 + 91;
 	        final int top = height - 39;
 	        final boolean unused = false;// Unused flag in vanilla, seems to be part of a 'fade out' mechanic
 
 	        final FoodStats stats = mc.thePlayer.getFoodStats();
 	        final int level = stats.getFoodLevel();
 	        final int levelLast = stats.getPrevFoodLevel();
 
 	        for (int i = 0; i < 10; ++i)
 	        {
 	            final int idx = i * 2 + 1;
 	            final int x = left - i * 8 - 9;
 	            int y = top;
 	            int icon = 16;
 	            byte backgound = 0;
 
 	            if (mc.thePlayer.isPotionActive(Potion.hunger))
 	            {
 	                icon += 36;
 	                backgound = 13;
 	            }
 	            if (unused) backgound = 1; //Probably should be a += 1 but vanilla never uses this
 
 	            if (mc.thePlayer.getFoodStats().getSaturationLevel() <= 0.0F && updateCounter % (level * 3 + 1) == 0)
 	            {
 	                y = top + (rand.nextInt(3) - 1);
 	            }
 
 	            this.drawTexturedModalRect(x, y, 16 + backgound * 9, 27, 9, 9);
 
 	            if (unused)
 	            {
 	                if (idx < levelLast)
 	                {
 	                    drawTexturedModalRect(x, y, icon + 54, 27, 9, 9);
 	                }
 
 	                if (idx == levelLast)
 	                {
 	                    drawTexturedModalRect(x, y, icon + 63, 27, 9, 9);
 	                }
 	            }
 
 	            if (idx < level)
 	            {
 	                drawTexturedModalRect(x, y, icon + 36, 27, 9, 9);
 	            }
 
 	            if (idx == level)
 	            {
 	                drawTexturedModalRect(x, y, icon + 45, 27, 9, 9);
 	            }
 	        }
 		} else {
 	    	drawDoubleOutlinedBox(width / 2 - 90, height - 29, 80, 4, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 				final int food = mc.thePlayer.getFoodStats().getFoodLevel();
 			int foodHeal = 0;
 			boolean overkill = false;
 			if (food != 20) {
 				int barWidth = 0;
 				final ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
 				if (stack != null) {
 					final Item item = stack.getItem();
 					if (item != null && item instanceof ItemFood) {
 						foodHeal = ((ItemFood) item).getHealAmount();
 						barWidth = Math.min(20, food + foodHeal);
 						if (food + foodHeal > 20) overkill = true;
 					}
 				}
 				if (barWidth > 0 && Config.get(Config.NODE_FOOD_PREDICT)) drawSolidGradientRect(width / 2 - 90, height - 29, barWidth * 4, 4, overkill ? 0 : 0xd82424, 0x901414);
 			}
 			drawSolidGradientRect(width / 2 - 90, height - 29, lastFood, 4, hasPotion(Potion.hunger) ? 0x0c1702 : 0x6a410b, hasPotion(Potion.hunger) ? 0x1d3208 : 0x8e5409);
 			glPushMatrix();
 			glScalef(0.5F, 0.5F, 0.5F);
 			if (foodHeal > 0 && Config.get(Config.NODE_FOOD_PREDICT)) mc.fontRenderer.drawString("Will Heal: " + foodHeal + (overkill ? " (Waste " + (food + foodHeal - 20) + ")" : ""), width - 178, height * 2 - 57, 0xFFFFFF);
 	
 			mc.fontRenderer.drawStringWithShadow((food < 5 ? ColorCode.RED : "") + "" + food, width - 33, height * 2 - 58, 0xFFFFFF);
 			glPopMatrix();
 		}
 
         mc.mcProfiler.endSection();
         post(FOOD);
 	}
 
 	protected void renderHotbar(final int width, final int height, final float partialTicks) {
 		if (Config.get(Config.NODE_ITEMS_BACKGROUND)) drawDoubleOutlinedBox(width / 2 - 90, height - 22, 180, 20, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 
 		final InventoryPlayer inv = mc.thePlayer.inventory;
 		for (int i = 0; i < 9; ++i) {
 			final int i1 = width / 2 - 88 + i * 20;
 			final int i2 = height - 20;
 			final boolean isHighlight = inv.currentItem == i;
 			final boolean isSlot = inv.mainInventory[i] != null;
 			
 			if (inv.currentItem != lastItem) {
 				tooltipOpenFor = 30;
 				lastItem = inv.currentItem;
 			}
 
 			if (isSlot) drawDoubleOutlinedBox(i1, i2, 16, 16, isHighlight ? BOX_HIGHLIGHT_COLOR : BOX_INNER_COLOR, inv.mainInventory[i].hasEffect() && !isHighlight ? BOX_EFFECT_OUTLINE_COLOR : BOX_OUTLINE_COLOR);
 			else if (isHighlight) if (!Config.get(Config.NODE_ITEMS_BACKGROUND)) drawDoubleOutlinedBox(i1 + 2, i2 + 2, 12, 12, BOX_INNER_COLOR, BOX_OUTLINE_COLOR, BOX_HIGHLIGHT_COLOR);
 			else drawDoubleOutlinedBox(i1 + 1, i2 + 1, 14, 14, BOX_HIGHLIGHT_COLOR, BOX_HIGHLIGHT_COLOR);
 		}
 		glEnable(GL_RESCALE_NORMAL);
 		RenderHelper.enableGUIStandardItemLighting();
 		glDisable(GL_BLEND);
 		for (int i = 0; i < 9; ++i) {
 			final int i1 = width / 2 - 88 + i * 20;
 			final int i2 = height - 20;
 
 			renderSlot(i, i1, i2, width, mc.fontRenderer);
 		}
 		RenderHelper.disableStandardItemLighting();
 		glDisable(GL_RESCALE_NORMAL);
 		glEnable(GL_BLEND);
 	}
 
 	private void smoothBars() {
 		if (mc.thePlayer == null) return;
 
 		if (mc.getSystemTime() < this.debugUpdateTime) {
 			return;
 		}
 
 		this.debugUpdateTime = mc.getSystemTime();
 		final int health = (int) Math.round(((double)mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth())*180);
 		final int food = mc.thePlayer.getFoodStats().getFoodLevel()*4;
 		final int xp = (int) (mc.thePlayer.experience * 80);
 		
 		if (lastFood == 0 || !Config.get(Config.NODE_SMOOTH_TRANSITION)) {
 			lastFood = food;
 		} else if (lastFood > food) {
 			--lastFood;
 		} else if (lastFood < food) {
 			lastFood++;
 		} else if (lastFood == food) {
 			//
 		}
 		
 		if (lastHealth == 0 || !Config.get(Config.NODE_SMOOTH_TRANSITION)) {
 			lastHealth = health;
 		} else if (lastHealth > health) {
 			--lastHealth;
 		} else if (lastHealth < health) {
 			lastHealth++;
 		} else if (lastHealth == health) {
 			//
 		}
 		
 		if (lastXP == 0 || !Config.get(Config.NODE_SMOOTH_TRANSITION)) {
 			lastXP = xp;
 		} else if (lastXP > xp) {
 			--lastXP;
 		} else if (lastXP < xp) {
 			lastXP++;
 		} else if (lastXP == xp) {
 			//nothing.
 		}
 	}
 	
     protected void renderCrosshairs(final int width, final int height)
     {
         if (pre(CROSSHAIRS)) return;
         GL11.glEnable(GL11.GL_BLEND);
         GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
         drawTexturedModalRect(width / 2 - 7, height / 2 - 7, 0, 0, 16, 16);
         GL11.glDisable(GL11.GL_BLEND);
         post(CROSSHAIRS);
     }
 
 	private void drawBuffs(final FontRenderer fr, final int width, final int height) {
 		final Collection<PotionEffect> potions = mc.thePlayer.getActivePotionEffects();
 		int xPotOffset = 0;
 		int yPotOffset = 0;
 		int itr = 0;
 		if (Config.get(Config.NODE_BUFFS)) for (final PotionEffect effect : potions) {
 			final Potion pot = Potion.potionTypes[effect.getPotionID()];
 			if (itr % 8 == 0) {
 				xPotOffset = 0;
 				yPotOffset += 1;
 			}
 			final String effectStr = Potion.getDurationString(effect);
 			drawDoubleOutlinedBox(width - 30 - xPotOffset * 21, height - 9 - yPotOffset * 28, fr.getStringWidth(effectStr) / 2 + 2, 8, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			drawDoubleOutlinedBox(width - 30 - xPotOffset * 21, height - 26 - yPotOffset * 28, 18, 18, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 			GL11.glDisable(GL11.GL_LIGHTING);
 			glDisable(GL_DEPTH_TEST);
 			final int index = pot.getStatusIconIndex();
 			mc.renderEngine.bindTexture("/gui/inventory.png");
 			if (pot.hasStatusIcon()) drawTexturedModalRect(width - 30 - xPotOffset * 21, height - 26 - yPotOffset * 28, 0 + index % 8 * 18, 198 + index / 8 * 18, 18, 18);
 			glEnable(GL_DEPTH_TEST);
 
 			final String level = StatCollector.translateToLocal("enchantment.level." + (effect.getAmplifier() + 1));
 
 			if (level.length() < 5 && !level.equals(StatCollector.translateToLocal("enchantment.level.1"))) fr.drawStringWithShadow(level, width - 29 - xPotOffset * 21, height - 25 - yPotOffset * 28, 0xFFFFFF);
 
 			glPushMatrix();
 			glScalef(0.5F, 0.5F, 0.5F);
 			fr.drawStringWithShadow((effect.getIsAmbient() ? ColorCode.RED : "") + effectStr, (width - 29 - xPotOffset * 21) * 2, (height - 6 - yPotOffset * 28) * 2, 0xFFFFFF);
 			glPopMatrix();
 			++itr;
 			++xPotOffset;
 		}
 	}
 
 	protected void renderPlayerList(final int width, final int height) {
 		final FontRenderer fr = mc.fontRenderer;
 		ScoreObjective scoreobjective = this.mc.theWorld.getScoreboard().func_96539_a(1);
 
 		if (scoreobjective != null)
 		{
 			this.drawScoreboardSidebar(scoreobjective, height, width, fr);
 		}
 
 		scoreobjective = this.mc.theWorld.getScoreboard().func_96539_a(0);
 		
 		if (mc.gameSettings.keyBindPlayerList.pressed && (!mc.isIntegratedServerRunning() || mc.thePlayer.sendQueue.playerInfoList.size() > 1)) {
 			final String sip = mc.getServerData().serverIP;
 			final String sname = mc.getServerData().serverName;
 			final String sdisp = sname + (mc.getServerData().isHidingAddress() ? "" : " - " + sip);
 			mc.renderEngine.bindTexture("/font/default.png");
 			final NetClientHandler var37 = mc.thePlayer.sendQueue;
 			final List var39 = var37.playerInfoList;
 			final int var13 = var37.currentServerMaxPlayers;
 			int var40 = var13;
 			int var38;
 			
 			drawDoubleOutlinedBox(width/2-(mc.fontRenderer.getStringWidth(sdisp)/2)-1, 21, mc.fontRenderer.getStringWidth(sdisp)+2, 10, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			mc.fontRenderer.drawStringWithShadow(sdisp, width/2-(mc.fontRenderer.getStringWidth(sdisp)/2), 22, 0xFFFFFF);
 
 			for (var38 = 1; var40 > 20; var40 = (var13 + var38 - 1) / var38)
 				++var38;
 
 			int var16 = 300 / var38;
 
 			if (var16 > 150) var16 = 150;
 
 			final int var17 = (width - var38 * var16) / 2;
 			final byte var44 = 22;
 			drawDoubleOutlinedBox(var17-2, var44 - 2+15, var16 * var38 + 3, 9 * var40 + 3, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			int var19;
 			int var20;
 			int var47;
 
 			for (var19 = 0; var19 < var13; ++var19) {
 				var20 = var17 + var19 % var38 * var16;
 				var47 = var44 + var19 / var38 * 9;
 				drawOutlinedBox(var20, var47+15, var16 - 1, 8, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 				GL11.glEnable(GL11.GL_ALPHA_TEST);
 
 				if (var19 < var39.size()) {
 					final GuiPlayerInfo var46 = (GuiPlayerInfo) var39.get(var19);
 					
                     final GuiPlayerInfo guiplayerinfo = (GuiPlayerInfo)var39.get(var19);
                     final ScorePlayerTeam scoreplayerteam = this.mc.theWorld.getScoreboard().getPlayersTeam(guiplayerinfo.name);
                     String name = ScorePlayerTeam.func_96667_a(scoreplayerteam, guiplayerinfo.name);
                     
                     Integer dist = null;
 					try {
 						dist = Math.round(p.getDistanceToEntity(world.getPlayerEntityByName(var46.name)));
 					} catch (final Exception e) {
 						//nuthin
 					}
 					if (dist != null && dist != 0) {
 						name = (name + " - " + dist + "m");
 					}
 
                     if (scoreobjective != null)
                     {
                         final int j3 = var20 + fr.getStringWidth(name) + 5;
                         final int l3 = var20 + var16 - 12 - 5;
 
                         if (l3 - j3 > 5)
                         {
                             final Score score = scoreobjective.getScoreboard().func_96529_a(guiplayerinfo.name, scoreobjective);
                             final String s4 = EnumChatFormatting.YELLOW + "" + score.func_96652_c();
                             fr.drawStringWithShadow(s4, l3 - fr.getStringWidth(s4) - 10, var47+15, 16777215);
                         }
                     }
 					fr.drawStringWithShadow(name, var20, var47+15, 16777215);
 					mc.renderEngine.bindTexture("/gui/icons.png");
 					final byte var50 = 0;
 					byte var49;
 
 					if (var46.responseTime < 0) var49 = 5;
 					else if (var46.responseTime < 150) var49 = 0;
 					else if (var46.responseTime < 300) var49 = 1;
 					else if (var46.responseTime < 600) var49 = 2;
 					else if (var46.responseTime < 1000) var49 = 3;
 					else var49 = 4;
 
 					zLevel += 100.0F;
 					drawTexturedModalRect(var20 + var16 - 12, var47+15, 0 + var50 * 10, 176 + var49 * 8, 10, 8);
 					GL11.glPushMatrix();
 					GL11.glScalef(0.5F, 0.5F, 0.5F);
 					GL11.glDisable(GL11.GL_DEPTH_TEST);
 					final String ms = var46.responseTime + " ms.";
 					fr.drawStringWithShadow(ms, (var20 + var16 - 9 - fr.getStringWidth(ms) / 2 ) * 2, (var47 +15) * 2, 16777215);
 					glEnable(GL11.GL_DEPTH_TEST);
 					glPopMatrix();
 					zLevel -= 100.0F;
 				}
 			}
 		}
 	}
 
 	private void drawBossBar(final FontRenderer fr, final int width, final int height) {
 		if (BossStatus.bossName != null && BossStatus.statusBarLength > 0 && Config.get(Config.NODE_BOSS_BAR)) {
 			mc.renderEngine.bindTexture("/font/default.png");
 			final int yoffset = 15;
 			final int xoffset = 6;
 			if (Config.get(Config.NODE_BOTTOM_ADORNMENTS)) {
 				drawDoubleOutlinedBox(width / 2 - 126 + xoffset, 34+yoffset, 5, 5, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 				drawDoubleOutlinedBox(width / 2 + 121 + xoffset, 34+yoffset, 5, 5, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 				drawOutlinedBox(width / 2 - 119 + xoffset, 36+yoffset, 238, 1, BOX_OUTLINE_COLOR, BOX_INNER_COLOR);
 				glPushMatrix();
 				glDisable(GL_DEPTH_TEST);
 				glScalef(0.5F, 0.5F, 0.5F);
 				drawSolidRect(width - 243 + xoffset, 71+yoffset, width + 240, 75, BOX_OUTLINE_COLOR);
 				glEnable(GL_DEPTH_TEST);
 				glPopMatrix();
 			}
 			BossStatus.statusBarLength--;
 			drawOutlinedBox(width / 2 - fr.getStringWidth(BossStatus.bossName) / 2 - 3 + xoffset, 20+yoffset, fr.getStringWidth(BossStatus.bossName) + 6 + xoffset, 14, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			drawDoubleOutlinedBox(width / 2 - 91 + xoffset, 31+yoffset, 182 + xoffset, 10, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			final int renderHealth = (int) (BossStatus.healthScale * 182);
 			drawSolidGradientRect(width / 2 - 91 + xoffset, 31+yoffset, renderHealth + xoffset, 10, 0, 0x25092e);
 			fr.drawStringWithShadow(ColorCode.PURPLE + BossStatus.bossName, width / 2 - fr.getStringWidth(BossStatus.bossName) / 2 + xoffset+3, 21+yoffset, 0xFFFFFF);
 			final String hp = (BossStatus.healthScale < 0.1 ? ColorCode.RED : "") + "" + Math.round(BossStatus.healthScale * 100) + "%";
 			if (!(BossStatus.healthScale < 0)) fr.drawStringWithShadow(hp, width / 2 - fr.getStringWidth(hp) / 2 + xoffset+2, 32+yoffset, 0xFFFFFF);
 		} else if (BossStatus.bossName != null && BossStatus.statusBarLength > 0 && !Config.get(Config.NODE_BOSS_BAR)) {
 			final int xoffset = 7;
 			--BossStatus.statusBarLength;
 			mc.renderEngine.bindTexture("/gui/icons.png");
 			final FontRenderer var1 = this.mc.fontRenderer;
 			final ScaledResolution var2 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
 			final int var3 = var2.getScaledWidth();
 			final short var4 = 182;
 			final int var5 = var3 / 2 - var4 / 2;
 			final int var6 = (int)(BossStatus.healthScale * (float)(var4 + 1));
 			final byte var7 = 30;
 			this.drawTexturedModalRect(var5+xoffset, var7, 0, 74, var4, 5);
 			this.drawTexturedModalRect(var5+xoffset, var7, 0, 74, var4, 5);
 
 			if (var6 > 0)
 			{
 				this.drawTexturedModalRect(var5+xoffset, var7, 0, 79, var6, 5);
 			}
 
 			final String var8 = BossStatus.bossName;
 			var1.drawStringWithShadow(var8, var3 / 2 - var1.getStringWidth(var8) / 2 + xoffset + 2, var7 - 1, 16777215);
 			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		}
 	}
 
 	private void drawArrowCount(final FontRenderer fr, final int width, final int height) {
 		if (mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBow && Config.get(Config.NODE_SHOW_ARROWS) && !mc.playerController.isInCreativeMode()) {
 			glPushMatrix();
 			glScalef(0.5F, 0.5F, 0.5F);
 			int allArrows = 0;
 			for (final ItemStack stack : mc.thePlayer.inventory.mainInventory)
 				if (stack != null && stack.itemID == Item.arrow.itemID) allArrows += stack.stackSize;
 			final String arrowStr = (allArrows <= 8 && !Config.get(Config.NODE_COLORBLIND_MODE) ? ColorCode.RED : "") + "Arrows: " + allArrows;
 			final int arrowStrWidth = fr.getStringWidth(arrowStr);
 			mc.renderEngine.bindTexture("/font/default.png");
 			fr.drawStringWithShadow(arrowStr, width - arrowStrWidth / 2, height - 21, 0xFFFFFF);
 			glPopMatrix();
 		}
 	}
 
     private static List<String> itemDisplayNameMultiline(final ItemStack itemstack, final GuiContainer gui, final boolean includeHandlers)
     {
         List<String> namelist = null;
         try
         {
             namelist = itemstack.getTooltip(Minecraft.getMinecraft().thePlayer, includeHandlers && Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
         }
         catch(final Exception exception) {}
 
         if(namelist == null)
             namelist = new ArrayList<String>();
 
         if(namelist.size() == 0)
             namelist.add("Unnamed");
 
         if(namelist.get(0) == null || namelist.get(0).equals(""))
             namelist.set(0, "Unnamed");
 
         namelist.set(0, "\247"+Integer.toHexString(itemstack.getRarity().rarityColor)+namelist.get(0));
         for(int i = 1; i < namelist.size(); i++)
             namelist.set(i, "\u00a77"+namelist.get(i));
 
         return namelist;
     }
 	
     private static String itemDisplayNameShort(final ItemStack itemstack)
     {
         final List<String> list = itemDisplayNameMultiline(itemstack, null, false);
         return list.get(0);
     }
     
     private static ArrayList<ItemStack> getIdentifierItems(final World world, final EntityPlayer player, final MovingObjectPosition hit)
     {
         final int x = hit.blockX;
         final int y = hit.blockY;
         final int z = hit.blockZ;
         final Block blockUnderMouse = Block.blocksList[world.getBlockId(x, y, z)];
         
         ArrayList<ItemStack> items = new ArrayList<ItemStack>();
         
         if(items.size() > 0)
             return items;
         
         ItemStack pick = null;
         try {
         	pick = blockUnderMouse.getPickBlock(hit, world, x, y, z);
         } catch (final Exception e) {
         	pick = null;
         }
         if(pick != null)
             items.add(pick);
         
         try
         {
             items.addAll(blockUnderMouse.getBlockDropped(world, x, y, z, world.getBlockMetadata(x, y, z), 0));
         }
         catch(final Exception e){}
         if(blockUnderMouse instanceof IShearable)
         {
             final IShearable shearable = (IShearable)blockUnderMouse;
             if(shearable.isShearable(new ItemStack(Item.shears), world, x, y, z))
             {
                 items.addAll(shearable.onSheared(new ItemStack(Item.shears), world, x, y, z, 0));
             }
         }
         
         if(items.size() == 0)
         	try {
         		items.add(0, new ItemStack(blockUnderMouse, 1, world.getBlockMetadata(x, y, z)));
         	} catch (final Exception e) {
         		items = null;
         	}
         
         return items;
     }
 
 	private void drawBlockAtPointer(final FontRenderer fr, final RenderItem ir, final int width, final int height) {
 		if (Config.get(Config.NODE_BLOCK_DISPLAY) && (mc.renderViewEntity.rayTrace(5, 1.0F) != null) && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
 	        
             final ArrayList<ItemStack> items = getIdentifierItems(world, mc.thePlayer, mc.objectMouseOver);
             
             String itemname = null;
             ItemStack stack = null;
             if (items != null) {
 	            for(int i = 0; i < items.size(); i++)
 	            {
 	                try
 	                {
 	                    final String s = itemDisplayNameShort(items.get(i));
 	                    if(s != null && !s.endsWith("Unnamed"))
 	                    {
 	                        itemname = s;
 	                        stack = items.get(i);
 	                        break;
 	                    }
 	                }
 	                catch(final Exception e){}
 	            }
             } else {
             	return;
             }
             if(itemname == null)
                 return;
 
             itemname = itemname + (!Config.get(Config.NODE_BLOCK_DISPLAY_ID) ? "" : (" (" + stack.itemID + (stack.getItemDamage() == 0 ? "" : ":" + stack.getItemDamage()) + ")"));
             
 			drawDoubleOutlinedBox(width-50-fr.getStringWidth(itemname), 25-1+10, 22+fr.getStringWidth(itemname), 18, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			fr.drawString(itemname, width-30-fr.getStringWidth(itemname), 29+10, 0xFFFFFF);
 			
 			GL11.glPushMatrix();
 	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
 	        RenderHelper.enableGUIStandardItemLighting();
 	        ir.renderItemAndEffectIntoGUI(fr, this.mc.renderEngine, stack, width-49-fr.getStringWidth(itemname), 25+10);
 	        RenderHelper.disableStandardItemLighting();
 	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
 	        GL11.glPopMatrix();
 	        		        
 		}
 	}
 
 	private void drawDangerZone(final FontRenderer fr, final int width, final int height,
 			final int blockLight, final RenderItem ir) {
 
 		if (mc.isSingleplayer())
 		{
 			p = mc.getIntegratedServer().getConfigurationManager().getPlayerForUsername(mc.thePlayer.username);
 			if (p != null) {
 				world = mc.getIntegratedServer().worldServerForDimension(p.dimension);
 			}
 		} else {
 			p = mc.thePlayer;
 			world = mc.theWorld;
 		}
 		
 		if (blockLight < 7 && Config.get(Config.NODE_DANGER_DISPLAY) && !world.isDaytime() && world.difficultySetting != 0) {
 			mc.renderEngine.bindTexture("/font/default.png");
 			final String light = (Config.get(Config.NODE_COLORBLIND_MODE) ? "" : ColorCode.RED) + "Danger Zone!";
 			final int lightLenght = fr.getStringWidth(light);
 			drawDoubleOutlinedBox(39, 25+10, lightLenght + 20, 16, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			RenderHelper.enableGUIStandardItemLighting();
 			ir.renderItemIntoGUI(fr, mc.renderEngine, new ItemStack(Item.skull, 1, 4), 40, 25+10);
 			RenderHelper.disableStandardItemLighting();
 			fr.drawStringWithShadow(light, 56, 29+10, 0xFFFFFF);
 		}
 	}
 
 	private void drawTopBar(final FontRenderer fr, final RenderItem ir, final int width, final int height, final String biomeName) {
 		String time = new SimpleDateFormat("h:mm a").format(new Date()).toString();
 		if (Config.get(Config.NODE_24HR_CLOCK)) {
 			time = new SimpleDateFormat("HH:mm").format(new Date()).toString();
 		}
 		int invSlots = 0;
 		for (final ItemStack element : mc.thePlayer.inventory.mainInventory)
 			if (element == null) ++invSlots;
 
 		int armoroffset = 15;
 		
 		if (Config.get(Config.NODE_TOP_BAR)) armoroffset = 0;
 		
 		if (Config.get(Config.NODE_STATUS_DISPLAY) && !(mc.gameSettings.keyBindPlayerList.pressed && !mc.isSingleplayer())) IC2Integration.renderTopBar(mc, width, armoroffset);
 		
 		if (Config.get(Config.NODE_TOP_BAR)) {
 			mc.renderEngine.bindTexture("/font/default.png");
 			String dir = "Unknown";
 			int rot = 0;
 			
 			if (Config.get(Config.NODE_DIRECTION)) {
 				rot = Math.abs(Math.round(mc.thePlayer.rotationYaw % 360));
 				if (rot >= 45 && rot < 135) {
 					dir = "West";
 				} else if (rot >= 135 && rot < 225) {
 					dir = "North";
 				} else if (rot >= 225 && rot < 315) {
 					dir = "East";
 				} else {
 					dir = "South" ;
 				} 
 			}
 			
 			if (Config.get(Config.NODE_DEGREES)) dir = dir + " (" + rot + ")";
 
 			final String topData = biomeName + " | " + time + (Config.get(Config.NODE_DIRECTION) ? " | " + dir : "") + (Config.get(Config.NODE_INV_SLOT) ? " | Inv: " + invSlots : "");
 			final int size = fr.getStringWidth(topData);
 
 			if (Config.get(Config.NODE_CHEAT_COMPASSCLOCK)) {
 				drawDoubleOutlinedBox(width / 2 - size / 2 - 24, -1, 18, 18, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 				if ((size % 2)==1) {
 					drawDoubleOutlinedBox(width / 2 + size / 2 + 5, -1, 18, 18, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 					RenderHelper.enableGUIStandardItemLighting();
 					ir.renderItemIntoGUI(fr, mc.renderEngine, new ItemStack(Item.pocketSundial), width / 2 + size / 2 + 6, 0);
 					RenderHelper.disableStandardItemLighting();
 				} else {
 					drawDoubleOutlinedBox(width / 2 + size / 2 + 4, -1, 18, 18, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 					RenderHelper.enableGUIStandardItemLighting();
 					ir.renderItemIntoGUI(fr, mc.renderEngine, new ItemStack(Item.pocketSundial), width / 2 + size / 2 + 5, 0);
 					RenderHelper.disableStandardItemLighting();
 				}
 
 				RenderHelper.enableGUIStandardItemLighting();
 				ir.renderItemIntoGUI(fr, mc.renderEngine, new ItemStack(Item.compass), width / 2 - size / 2 - 23, 0);
 				RenderHelper.disableStandardItemLighting();
 			}
 
 			drawDoubleOutlinedBox(width / 2 - size / 2 - 4, -1, size + 6, 15, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			this.drawCenteredString(fr, topData, width / 2, 3, 0xFFFFFF);
 		}
 	}
 
 	private void drawMCMMO(final FontRenderer fr, final int width, final int height) {
 		if (Config.get(Config.NODE_MCMMO)) {
 			final LevelUpData lvlData = McMMOIntegration.getActiveLevelUpData();
 			if (lvlData != null) {
 				mc.renderEngine.bindTexture("/font/default.png");
 				final String levelUp = ColorCode.RED + "Level Up!";
 				final String skillLeveledUp = ColorCode.YELLOW + lvlData.getSkill() + ": [" + lvlData.getLevel() + "]";
 				glPushMatrix();
 				glScalef(2F, 2F, 2F);
 				fr.drawStringWithShadow(levelUp, width / 4 - fr.getStringWidth(levelUp) / 2, 15, 0xFFFFFF);
 				glPopMatrix();
 				fr.drawStringWithShadow(skillLeveledUp, width / 2 - fr.getStringWidth(skillLeveledUp) / 2, 52, 0xFFFFFF);
 			}
 
 			int maxSize = 20;
 			for (final SkillData skillData : McMMOIntegration.skillData)
 				maxSize = Math.max(fr.getStringWidth((skillData.type == UsageType.READY ? skillData.getTool().charAt(0) + skillData.getTool().substring(1).toLowerCase() : skillData.getName()) + ": " + skillData.type.getName()), maxSize);
 
 			if (McMMOIntegration.skillData.size() > 0) drawDoubleOutlinedBox(10, 58, maxSize + 4, McMMOIntegration.skillData.size() * 11 + 6, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			int i = 0;
 			for (final SkillData skillData : McMMOIntegration.skillData) {
 				fr.drawStringWithShadow((skillData.type == UsageType.READY ? skillData.getTool().charAt(0) + skillData.getTool().substring(1).toLowerCase() : skillData.getName()) + ": " + skillData.type.getName(), 12, 60 + i * 12, 0xFFFFFF);
 				++i;
 			}
 		}
 	}
 
 	private void drawDebugInfo(final FontRenderer fr, final int width, final int height,
 			final int posX, final int posY, final int posZ, final Chunk chunk, final String biomeName,
 			final int blockLight, final int direction) {
 		if (mc.gameSettings.showDebugInfo) {
 			mc.renderEngine.bindTexture("/font/default.png");
 			glPushMatrix();
 			fr.drawStringWithShadow("Minecraft " + mod_TukMC.MC_VERSION + " (" + mc.debug + ")", 2, 2, 0xFFFFFF);
 			fr.drawStringWithShadow(mc.debugInfoRenders(), 2, 12, 0xFFFFFF);
 			fr.drawStringWithShadow(mc.getEntityDebug(), 2, 22, 0xFFFFFF);
 			fr.drawStringWithShadow(mc.debugInfoEntities(), 2, 32, 0xFFFFFF);
 			fr.drawStringWithShadow(mc.getWorldProviderName(), 2, 42, 0xFFFFFF);
 			final long maxMemory = Runtime.getRuntime().maxMemory();
 			final long totalMemory = Runtime.getRuntime().totalMemory();
 			final long freeMemory = Runtime.getRuntime().freeMemory();
 			final long usedMemory = totalMemory - freeMemory;
 			String string = "Used memory: " + usedMemory * 100L / maxMemory + "% (" + usedMemory / 1024L / 1024L + "MB) of " + maxMemory / 1024L / 1024L + "MB";
 			drawString(fr, string, width - fr.getStringWidth(string) - 2, 2, 14737632);
 			string = "Allocated memory: " + totalMemory * 100L / maxMemory + "% (" + totalMemory / 1024L / 1024L + "MB)";
 			drawString(fr, string, width - fr.getStringWidth(string) - 2, 12, 14737632);
 			drawString(fr, String.format("x: %.5f (%d) // c: %d (%d)", Double.valueOf(mc.thePlayer.posX), Integer.valueOf(posX), Integer.valueOf(posX >> 4), Integer.valueOf(posX & 15)), 2, 64, 14737632);
 			drawString(fr, String.format("y: %.3f (feet pos, %.3f eyes pos)", Double.valueOf(mc.thePlayer.boundingBox.minY), Double.valueOf(mc.thePlayer.posY)), 2, 72, 14737632);
 			drawString(fr, String.format("z: %.5f (%d) // c: %d (%d)", Double.valueOf(mc.thePlayer.posZ), Integer.valueOf(posZ), Integer.valueOf(posZ >> 4), Integer.valueOf(posZ & 15)), 2, 80, 14737632);
 			drawString(fr, "f: " + direction + " (" + Direction.directions[direction] + ") / " + MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw), 2, 88, 14737632);
 
 			if (mc.theWorld != null && mc.theWorld.blockExists(posX, posY, posZ)) drawString(fr, "lc: " + (chunk.getTopFilledSegment() + 15) + " b: " + biomeName + " bl: " + blockLight + " sl: " + chunk.getSavedLightValue(EnumSkyBlock.Sky, posX & 15, posY, posZ & 15) + " rl: " + chunk.getBlockLightValue(posX & 15, posY, posZ & 15, 0), 2, 96, 14737632);
 			drawString(fr, String.format("ws: %.3f, fs: %.3f, g: %b, fl: %d", Float.valueOf(mc.thePlayer.capabilities.getWalkSpeed()), Float.valueOf(mc.thePlayer.capabilities.getFlySpeed()), Boolean.valueOf(mc.thePlayer.onGround), Integer.valueOf(mc.theWorld.getHeightValue(posX, posZ))), 2, 104, 14737632);
 			glPopMatrix();
 		}
 	}
 
 	private void drawRecordDisplay(final FontRenderer fr, final int width, final int height, final float par1) {
 		if (recordIsPlaying && Config.get(Config.NODE_MUSIC)) {
 			final float color = recordPlayingUpFor - par1;
 			int colorValue = (int) (color * 256.0F / 20.0F);
 			int colorRgb = 0xFFFFFF;
 			if (colorValue > 255) colorValue = 255;
 			if (colorValue > 0) {
 				colorRgb = Color.HSBtoRGB(color / 50.0F, 0.7F, 0.6F) & 16777215;
 				final Color colorInstance = new Color(colorRgb);
 				final int length = fr.getStringWidth(recordPlaying);
 
 				drawDoubleOutlinedBox(width / 2 - length / 2 - 20, height - 70, length + 40, 20, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 				mc.renderEngine.bindTexture("/particles.png");
 				glDisable(GL_DEPTH_TEST);
 				glColor3f(colorInstance.getRed() / 255F, colorInstance.getGreen() / 255F, colorInstance.getBlue() / 255F);
 				drawTexturedModalRect(width / 2 - length / 2 - 18, height - 68, 0, 64, 16, 16);
 				glColor3f(colorInstance.getRed() / 255F, colorInstance.getGreen() / 255F, colorInstance.getBlue() / 255F);
 				drawTexturedModalRect(width / 2 + length / 2, height - 68, 0, 64, 16, 16);
 				glEnable(GL_DEPTH_TEST);
 				mc.renderEngine.bindTexture("/font/default.png");
 				fr.drawStringWithShadow(recordPlaying, width / 2 - length / 2, height - 65, colorRgb);
 			}
 
 			if (recordPlayingUpFor <= 0) recordIsPlaying = false;
 			mc.renderEngine.bindTexture("/font/default.png");
 		}
 	}
 
 	private void drawRightBar(final FontRenderer fr, final int width, final int height) {
 		if (Config.get(Config.NODE_RIGHT_BAR)) {
 			mc.renderEngine.bindTexture("/font/default.png");
 			int xoffset = 0;
 			if (width - 183 <= (width / 2 + 90)) xoffset = (width-183)-(width/2+90);
 			drawDoubleOutlinedBox(width - 180-xoffset, height - 20, 140, 16, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			fr.drawStringWithShadow("FPS: " + ClientUtils.getFPS(), width - 176-xoffset, height - 16, 0xFFFFFF);
 			final String ping = mc.isSingleplayer() ? "N/A (SP)" : ClientUtils.getPing() + " ms." + (mc.isIntegratedServerRunning() ? " (LAN)" : " (MP)");
 			fr.drawStringWithShadow(ping, width - 44 - fr.getStringWidth(ping)-xoffset, height - 16, 0xFFFFFF);
 		}
 	}
 
 	private void drawLeftBar(final FontRenderer fr, final int width, final int height) {
 		if (Config.get(Config.NODE_LEFT_BAR)) {
 			GL11.glPushMatrix();
 			mc.renderEngine.bindTexture("/font/default.png");
 			int xoffset = 0;
 			if (183 >= (width / 2 - 90)) {
 				xoffset = 183-(width/2-90);
 				drawDoubleOutlinedBox(40-xoffset, height - 20, 140, 16, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			} else {
 				drawDoubleOutlinedBox(40, height - 20, 140, 16, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			}
 			fr.drawStringWithShadow("Online: " + mc.thePlayer.sendQueue.playerInfoList.size(), 44-xoffset, height - 16, 0xFFFFFF);
 			String status = "";
 			final int fallDmg = MathHelper.ceiling_float_int(mc.thePlayer.fallDistance - 3.0F);
 			if (mc.thePlayer.isSneaking()) status = "Sneaking";
 			if (mc.thePlayer.isSprinting()) status = "Sprinting";
 			else if (mc.thePlayer.getFoodStats().getFoodLevel() <= 6) status = ColorCode.RED + "Can't Sprint";
 			if (mc.thePlayer.capabilities.isFlying) status = "Flying";
 			else if (fallDmg > 0 && !mc.thePlayer.capabilities.isCreativeMode) status = "Falling: " + ColorCode.RED + fallDmg;
 			final String stat = (status.equals("") ? mc.thePlayer.username : status);
 			fr.drawStringWithShadow(stat, 176 - fr.getStringWidth(stat)-xoffset, height - 16, 0xFFFFFF);
 			GL11.glPopMatrix();
 		}
 	}
 
 	private void drawGenericStuff(final FontRenderer fr, final int width, final int height, final float par1) {
 		glEnable(GL_BLEND);
 
 		if (Config.get(Config.NODE_BOTTOM_ADORNMENTS)) {
 			drawDoubleOutlinedBox(6, height - 98, 5, 5, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			drawDoubleOutlinedBox(width - 10, height - 98, 5, 5, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			drawOutlinedBox(50, height - 13, width - 100, 1, BOX_OUTLINE_COLOR, BOX_INNER_COLOR);
 			drawOutlinedBox(8, height - 13, 40, 1, BOX_OUTLINE_COLOR, BOX_INNER_COLOR);
 			drawOutlinedBox(8, height - 92, 1, 80, BOX_OUTLINE_COLOR, BOX_INNER_COLOR);
 			drawOutlinedBox(width - 48, height - 13, 40, 1, BOX_OUTLINE_COLOR, BOX_INNER_COLOR);
 			drawOutlinedBox(width - 8, height - 92, 1, 80, BOX_OUTLINE_COLOR, BOX_INNER_COLOR);
 			glPushMatrix();
 			glScalef(0.5F, 0.5F, 0.5F);
 			drawSolidRect(15, height * 2 - 27, 23, height * 2 - 23, BOX_OUTLINE_COLOR);
 			drawSolidRect(width * 2 - 18, height * 2 - 27, width * 2 - 13, height * 2 - 23, BOX_OUTLINE_COLOR);
 			drawSolidRect(14, height * 2 - 186, 20, height * 2 - 185, BOX_OUTLINE_COLOR);
 			drawSolidRect(width * 2 - 18, height * 2 - 186, width * 2 - 10, height * 2 - 185, BOX_OUTLINE_COLOR);
 			glPopMatrix();
 		}
 	}
 
 	public void renderHealth(final int width, final int height) {
         if (pre(HEALTH)) return;
         mc.mcProfiler.startSection("health");
 
 		if (Config.get(Config.NODE_PLAIN_STATUS)) {
 	        boolean highlight = mc.thePlayer.hurtResistantTime / 3 % 2 == 1;
 	
 	        if (mc.thePlayer.hurtResistantTime < 10)
 	        {
 	            highlight = false;
 	        }
 	
 	        final int health = mc.thePlayer.getHealth();
 	        final int healthLast = mc.thePlayer.prevHealth;
 	        final int left = width / 2 - 91;
 	        final int top = height - 39;
 	
 	        int regen = -1;
 	        if (mc.thePlayer.isPotionActive(Potion.regeneration))
 	        {
 	            regen = this.updateCounter % 25;
 	        }
 	
 	        for (int i = 0; i < 10; ++i)
 	        {
 	            final int idx = i * 2 + 1;
 	            int iconX = 16;
 	            if (mc.thePlayer.isPotionActive(Potion.poison)) iconX += 36;
 	            else if (mc.thePlayer.isPotionActive(Potion.wither)) iconX += 72;
 	
 	            final int x = left + i * 8;
 	            int y = top;
 	            if (health <= 4) y = top + rand.nextInt(2);
 	            if (i == regen) y -= 2;
 	
 	            byte iconY = 0;
 	            if (mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) iconY = 5;
 	
 	            drawTexturedModalRect(x, y, 16 + (highlight ? 9 : 0), 9 * iconY, 9, 9);
 	
 	            if (highlight)
 	            {
 	                if (idx < healthLast)
 	                    drawTexturedModalRect(x, y, iconX + 54, 9 * iconY, 9, 9);
 	                else if (idx == healthLast)
 	                    drawTexturedModalRect(x, y, iconX + 63, 9 * iconY, 9, 9);
 	            }
 	
 	            if (idx < health)
 	                drawTexturedModalRect(x, y, iconX + 36, 9 * iconY, 9, 9);
 	            else if (idx == health)
 	                drawTexturedModalRect(x, y, iconX + 45, 9 * iconY, 9, 9);
 	        }
 		} else {
 			drawDoubleOutlinedBox(width / 2 - 90, height - 42, 180, 10, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			int healthBottom = hasPotion(Potion.regeneration) ? 0xd82424 : 0;
 			final int healthTop = hasPotion(Potion.wither) ? BOX_INNER_COLOR : 0x901414;
 			if (hasPotion(Potion.poison)) healthBottom = 0x375d12;
 			final int hp = mc.thePlayer.getHealth();
 //			int hitp = (int) Math.round(((double)hp / mc.thePlayer.getMaxHealth())*180);
 			drawSolidGradientRect(width / 2 - 90, height - 42, lastHealth, 10, healthBottom, healthTop);
 			glPushMatrix();
 			glScalef(0.5F, 0.5F, 0.5F);
 			if (!hasPotion(Potion.wither)) mc.fontRenderer.drawStringWithShadow((hp < 5 ? ColorCode.RED : "") + "" + hp, width + 168, height * 2 - 84, 0xFFFFFF);
 			glPopMatrix();
 		}
         mc.mcProfiler.endSection();
         post(HEALTH);
 	}
 	
 	private void modIntegration(final FontRenderer fr, final int width, final int height) {			
 		final boolean shouldDrawHUD = mc.playerController.shouldDrawHUD();
 
 		if (shouldDrawHUD) {
 			//XXX: MOD COMPATIBILITY
 			//Thirst Mod
 			if (ModLoader.isModLoaded("ThirstMod")) {
 				final StatsHolder tmstats = StatsHolder.getInstance();
 				final int thirst = tmstats.level;
 				final int barWidth = ((thirst)*4);
 				drawDoubleOutlinedBox(width / 2 - 90, height - 49, 80, 4, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 				drawSolidGradientRect(width / 2 - 90, height - 49, barWidth, 4, tmstats.isPoisoned ? 0x8AB500 : 0x1786FB, tmstats.isPoisoned ? 0x719500 : 0x0035FA);
 				glPushMatrix();
 				glScalef(0.5F, 0.5F, 0.5F);
 				fr.drawStringWithShadow((thirst < tmstats.saturation ? ColorCode.RED : "") + "" + thirst, width - 33, height * 2 - 98, 0xFFFFFF);
 				glPopMatrix();
 			}
 		}
 	}
 	
 	private int writeStat(final StatBase sb) {
 		return mc.statFileWriter.writeStat(sb);
 	}
 	
 	private void drawStatsBoard(final FontRenderer fr, final int width, final int height) {
 		if (!(CommonUtils.getMc().currentScreen instanceof GuiChat) && !(mc.gameSettings.keyBindPlayerList.pressed && !mc.isSingleplayer()) && Config.get(Config.NODE_STATBAR)) {
 			final String deathstat = "Deaths: " + mod_TukMC.deaths;
 			final String mobkillstat = "Mob Kills: " + (Integer.valueOf((StatList.getOneShotStat(2023).func_75968_a(writeStat(StatList.getOneShotStat(2023))).replace(",", "")))-mod_TukMC.negativeMobKills);
 			final String pkillstat = "Player Kills: " + (Integer.valueOf((StatList.getOneShotStat(2024).func_75968_a(writeStat(StatList.getOneShotStat(2024))).replace(",", "")))-mod_TukMC.negativePKills);
 			
 			final int max1 = Math.max(fr.getStringWidth(deathstat), fr.getStringWidth(mobkillstat));
 			final int max2 = Math.max(max1, fr.getStringWidth(pkillstat));
 			
 			drawDoubleOutlinedBox(-1, height/2-22, max2+2, 33, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 			
 			fr.drawString(deathstat, 1, height/2-20, 0xFFFFFF);
 			fr.drawString(mobkillstat, 1, height/2-9, 0xFFFFFF);
 			fr.drawString(pkillstat, 1, height/2+2, 0xFFFFFF);		
 		}
 	}
 
 	private void drawScoreboardSidebar(final ScoreObjective par1ScoreObjective, final int par2, final int par3, final FontRenderer par4FontRenderer)
 	{
 		final Scoreboard scoreboard = par1ScoreObjective.getScoreboard();
 		final Collection collection = scoreboard.func_96534_i(par1ScoreObjective);
 
 		if (collection.size() <= 15)
 		{
 			int k = par4FontRenderer.getStringWidth(par1ScoreObjective.getDisplayName());
 			String s;
 
 			for (final Iterator iterator = collection.iterator(); iterator.hasNext(); k = Math.max(k, par4FontRenderer.getStringWidth(s)))
 			{
 				final Score score = (Score)iterator.next();
 				final ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.func_96653_e());
 				s = ScorePlayerTeam.func_96667_a(scoreplayerteam, score.func_96653_e()) + ": " + EnumChatFormatting.RED + score.func_96652_c();
 			}
 
 			final int l = collection.size() * par4FontRenderer.FONT_HEIGHT;
 			final int i1 = par2 / 2 + l / 3;
 			final byte b0 = 3;
 			final int j1 = par3 - k - b0;
 			int k1 = 0;
 			final Iterator iterator1 = collection.iterator();
 
 			while (iterator1.hasNext())
 			{
 				final Score score1 = (Score)iterator1.next();
 				++k1;
 				final ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.func_96653_e());
 				final String s1 = ScorePlayerTeam.func_96667_a(scoreplayerteam1, score1.func_96653_e());
 				final String s2 = EnumChatFormatting.RED + "" + score1.func_96652_c();
 				final int l1 = i1 - k1 * par4FontRenderer.FONT_HEIGHT;
 				final int i2 = par3 - b0 + 2;
 				//                this.drawDoubleOutlinedBox(j1 - 2, l1, i2, l1 + par4FontRenderer.FONT_HEIGHT, BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 				par4FontRenderer.drawString(s1, j1, l1, 0xFFFFFF);
 				par4FontRenderer.drawString(s2, i2 - par4FontRenderer.getStringWidth(s2), l1, 3648127);
 
 				if (k1 == collection.size())
 				{
 					final String s3 = par1ScoreObjective.getDisplayName();
 					this.drawDoubleOutlinedBox(j1 - 2, l1 - par4FontRenderer.FONT_HEIGHT - 1, i2, l1 - 1 - 93 + ((k1-1)*(par4FontRenderer.FONT_HEIGHT+6)), BOX_INNER_COLOR, BOX_OUTLINE_COLOR);
 
 					par4FontRenderer.drawString(s3, j1 + k / 2 - par4FontRenderer.getStringWidth(s3) / 2, l1 - par4FontRenderer.FONT_HEIGHT, 3648127);
 				}
 			}
 		}
 	}
 
 	public void drawDoubleOutlinedBox(final int x, final int y, final int width, final int height, final int color, final int outlineColor) {
 		drawDoubleOutlinedBox(x, y, width, height, color, outlineColor, color);
 	}
 
 	public void drawDoubleOutlinedBox(final int x, final int y, final int width, final int height, final int color, final int outlineColor, final int outline2Color) {
 		glPushMatrix();
 		glScalef(0.5F, 0.5F, 0.5F);
 		drawSolidRect(x * 2 - 2, y * 2 - 2, (x + width) * 2 + 2, (y + height) * 2 + 2, color);
 		drawSolidRect(x * 2 - 1, y * 2 - 1, (x + width) * 2 + 1, (y + height) * 2 + 1, outlineColor);
 		drawSolidRect(x * 2, y * 2, (x + width) * 2, (y + height) * 2, outline2Color);
 		glPopMatrix();
 	}
 
 	public void drawOutlinedBox(final int x, final int y, final int width, final int height, final int color, final int outlineColor) {
 		glPushMatrix();
 		glScalef(0.5F, 0.5F, 0.5F);
 		drawSolidRect(x * 2 - 2, y * 2 - 2, (x + width) * 2 + 2, (y + height) * 2 + 2, outlineColor);
 		drawSolidRect(x * 2 - 1, y * 2 - 1, (x + width) * 2 + 1, (y + height) * 2 + 1, color);
 		glPopMatrix();
 	}
 
 	public void drawSolidRect(final int vertex1, final int vertex2, final int vertex3, final int vertex4, final int color) {
 		glPushMatrix();
 		final Color color1 = new Color(color);
 		final Tessellator tess = Tessellator.instance;
 		glDisable(GL_TEXTURE_2D);
 		tess.startDrawingQuads();
 		tess.setColorOpaque(color1.getRed(), color1.getGreen(), color1.getBlue());
 		tess.addVertex(vertex1, vertex4, zLevel);
 		tess.addVertex(vertex3, vertex4, zLevel);
 		tess.addVertex(vertex3, vertex2, zLevel);
 		tess.addVertex(vertex1, vertex2, zLevel);
 		tess.draw();
 		glEnable(GL_TEXTURE_2D);
 		glPopMatrix();
 	}
 
 	public void drawSolidGradientRect(final int x, final int y, final int width, final int height, final int color1, final int color2) {
 		drawSolidGradientRect0(x * 2, y * 2, (x + width) * 2, (y + height) * 2, color1, color2);
 	}
 
 	public void drawSolidGradientRect0(final int vertex1, final int vertex2, final int vertex3, final int vertex4, final int color1, final int color2) {
 		glPushMatrix();
 		glScalef(0.5F, 0.5F, 0.5F);
 		final Color color1Color = new Color(color1);
 		final Color color2Color = new Color(color2);
 		glDisable(GL_TEXTURE_2D);
 		glDisable(GL_ALPHA_TEST);
 		glShadeModel(GL_SMOOTH);
 		final Tessellator tess = Tessellator.instance;
 		tess.startDrawingQuads();
 		tess.setColorOpaque(color1Color.getRed(), color1Color.getGreen(), color1Color.getBlue());
 		tess.addVertex(vertex1, vertex4, zLevel);
 		tess.addVertex(vertex3, vertex4, zLevel);
 		tess.setColorOpaque(color2Color.getRed(), color2Color.getGreen(), color2Color.getBlue());
 		tess.addVertex(vertex3, vertex2, zLevel);
 		tess.addVertex(vertex1, vertex2, zLevel);
 		tess.draw();
 		glShadeModel(GL_FLAT);
 		glEnable(GL_ALPHA_TEST);
 		glEnable(GL_TEXTURE_2D);
 		glPopMatrix();
 	}
 
 	private void renderSlot(final int slot, final int x, final int y, final float ticks, final FontRenderer font) {
 		renderInventorySlot(slot, x, y, ticks);
 		final RenderEngine render = mc.renderEngine;
 		final RenderItem itemRenderer = new RenderItem();
 		final ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
 
 		if (stack != null) {
 			if (ForgeHooksClient.renderInventoryItem(new RenderBlocks(), render, stack, itemRenderer.renderWithColor, zLevel, (float)x, (float)y)) return;
 
 			final int dmg = stack.getItemDamageForDisplay();
 			final int color = (int) Math.round(255.0D - dmg * 255.0D / stack.getMaxDamage());
 			final int shiftedColor = Config.get(Config.NODE_COLORBLIND_MODE) ? 0xFFFFFF : 255 - color << 16 | color << 8;
 			final Color shiftedColor1 = new Color(shiftedColor);
 
 			if (stack != null && stack.hasEffect()) {
 				glDepthFunc(GL_GREATER);
 				glDisable(GL_LIGHTING);
 				glDepthMask(false);
 				render.bindTexture("/misc/glint.png");
 				zLevel -= 50.0F;
 				glEnable(GL_BLEND);
 				if (mc.thePlayer.inventory.currentItem == slot) glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 				else glBlendFunc(GL_DST_COLOR, GL_DST_COLOR);
 				if (slot == mc.thePlayer.inventory.currentItem) {
 					if (!stack.isItemDamaged() || Config.get(Config.NODE_COLORBLIND_MODE)) glColor4f(0.5F, 0.25F, 0.8F, 0.4F);
 					else glColor4f(shiftedColor1.getRed() / 255F, shiftedColor1.getGreen() / 255F, shiftedColor1.getBlue() / 255F, 0.4F);
 					renderGlint(x * 431278612 + y * 32178161, x, y, 16, 16);
 				}
 				glDisable(GL_BLEND);
 				glDepthMask(true);
 				zLevel += 50.0F;
 				glEnable(GL_LIGHTING);
 				glDepthFunc(GL_LEQUAL);
 			}
 			itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, stack, x, y);
 
 			glDisable(GL_LIGHTING);
 			glDisable(GL_DEPTH_TEST);
 
 			final int offset = -10;
 
 			IC2Integration.renderSlots(stack, font, offset, dmg, x, y, shiftedColor);
 
 			if (stack.stackSize > 1) {
 				final String size = ""+stack.stackSize;
 				final int sizeWidth = font.getStringWidth(size);
 				glPushMatrix();
 				glScalef(0.5F, 0.5F, 0.5F);
 				font.drawStringWithShadow(size, (x + 16 - sizeWidth / 2) * 2, (y + 12 - offset) * 2, 0xFFFFFF);
 				glScalef(1F, 1F, 1F);
 				glPopMatrix();
 			}
 
 			glEnable(GL_LIGHTING);
 			glEnable(GL_DEPTH_TEST);
 		}
 	}
 
     private void renderHelmet(final ScaledResolution res, final float partialTicks, final boolean hasScreen, final int mouseX, final int mouseY)
     {
         if (pre(HELMET)) return;
 
         final ItemStack itemstack = this.mc.thePlayer.inventory.armorItemInSlot(3);
 
         if (this.mc.gameSettings.thirdPersonView == 0 && itemstack != null && itemstack.getItem() != null)
         {
             if (itemstack.itemID == Block.pumpkin.blockID)
             {
                 renderPumpkinBlur(res.getScaledWidth(), res.getScaledHeight());
             }
             else
             {
                 itemstack.getItem().renderHelmetOverlay(itemstack, mc.thePlayer, res, partialTicks, hasScreen, mouseX, mouseY);
             }
         }
 
         post(HELMET);
     }
 
     /**
      * Renders the specified item of the inventory slot at the specified location. Args: slot, x, y, partialTick
      */
 	@Override
     public void renderInventorySlot(final int par1, final int par2, final int par3, final float par4)
     {
         final ItemStack itemstack = this.mc.thePlayer.inventory.mainInventory[par1];
 		final RenderItem itemRenderer = new RenderItem();
 
         if (itemstack != null)
         {
             final float f1 = (float)itemstack.animationsToGo - par4;
 
             if (f1 > 0.0F)
             {
                 GL11.glPushMatrix();
                 final float f2 = 1.0F + f1 / 5.0F;
                 GL11.glTranslatef((float)(par2 + 8), (float)(par3 + 12), 0.0F);
                 GL11.glScalef(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
                 GL11.glTranslatef((float)(-(par2 + 8)), (float)(-(par3 + 12)), 0.0F);
             }
 
             GL11.glPushMatrix();
             itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, itemstack, par2, par3);
             GL11.glPopMatrix();
             
             if (f1 > 0.0F)
             {
                 GL11.glPopMatrix();
             }
         }
     }
 	private void defaultHUD(final float par1, final boolean par2, final int par3, final int par4) {
 		super.renderGameOverlay(par1, par2, par3, par4);
 		presistentChatGui.drawChat(getUpdateCounter());
 	}
 
 	@Override
 	public void setRecordPlayingMessage(final String record) {
 		recordPlaying = record;
 		recordPlayingUpFor = 60;
 		recordIsPlaying = true;
 	}
 
 	@Override
 	public void updateTick() {
 		if (recordPlayingUpFor > 0) --recordPlayingUpFor;
 		if (tooltipOpenFor > 0) --tooltipOpenFor;
 		
 		super.updateTick();
 		update++;
 	}
 
 	// The method in GuiIngame is private, full override was necessary.
 	// I don't know what some of the params are, so I left them all as parX
 	private void renderGlint(final int par1, final int par2, final int par3, final int par4, final int par5) {
 		for (int i = 0; i < 2; ++i) {
 			final float var7 = 0.00390625F;
 			final float var8 = 0.00390625F;
 			final float var9 = Minecraft.getSystemTime() % (3000 + i * 1873) / (3000.0F + i * 1873) * 256F;
 			final float var10 = 0F;
 			final float var12 = i == 1 ? -1F : 4F;
 			final Tessellator tess = Tessellator.instance;
 			tess.startDrawingQuads();
 			tess.addVertexWithUV(par2, par3 + par5, zLevel, (var9 + par5 * var12) * var7, (var10 + par5) * var8);
 			tess.addVertexWithUV(par2 + par4, par3 + par5, zLevel, (var9 + par4 + par5 * var12) * var7, (var10 + par5) * var8);
 			tess.addVertexWithUV(par2 + par4, par3 + 0, zLevel, (var9 + par4) * var7, var10 * var8);
 			tess.addVertexWithUV(par2 + 0, par3 + 0, zLevel, var9 * var7, var10 * var8);
 			tess.draw();
 		}
 	}
 
 	// Hopefully to clean code, will be used a fair bit
 	public boolean hasPotion(final Potion pot) {
 		return mc.thePlayer.isPotionActive(pot.id);
 	}
 
 	@Override
 	public void renderPumpkinBlur(final int par1, final int par2) {
 		glDisable(GL_DEPTH_TEST);
 		glDepthMask(false);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		glColor4f(1F, 1F, 1F, 1F);
 		glDisable(GL_ALPHA_TEST);
 		glBindTexture(GL_TEXTURE_2D, mc.renderEngine.getTexture("%blur%/misc/pumpkinblur.png"));
 		final Tessellator tess = Tessellator.instance;
 		tess.startDrawingQuads();
 		tess.addVertexWithUV(0D, par2, -90D, 0D, 1D);
 		tess.addVertexWithUV(par1, par2, -90D, 1D, 1D);
 		tess.addVertexWithUV(par1, 0D, -90D, 1D, 0D);
 		tess.addVertexWithUV(0D, 0D, -90D, 0D, 0D);
 		tess.draw();
 		glDepthMask(true);
 		glEnable(GL_DEPTH_TEST);
 		glEnable(GL_ALPHA_TEST);
 		glColor4f(1F, 1F, 1F, 1F);
 	}
 
 	@Override
 	public void renderVignette(float par1, final int par2, final int par3) {
 		par1 = 1.0F - par1;
 		if (par1 < 0.0F) par1 = 0.0F;
 		if (par1 > 1.0F) par1 = 1.0F;
 
 		prevVignetteBrightness = (float) (prevVignetteBrightness + (par1 - prevVignetteBrightness) * 0.01);
 		glDisable(GL_DEPTH_TEST);
 		glDepthMask(false);
 		glBlendFunc(GL_ZERO, GL_ONE_MINUS_SRC_COLOR);
 		glColor4f(prevVignetteBrightness, prevVignetteBrightness, prevVignetteBrightness, 1F);
 		glBindTexture(GL_TEXTURE_2D, mc.renderEngine.getTexture("%blur%/misc/vignette.png"));
 		final Tessellator tess = Tessellator.instance;
 		tess.startDrawingQuads();
 		tess.addVertexWithUV(0D, par3, -90D, 0D, 1D);
 		tess.addVertexWithUV(par2, par3, -90D, 1D, 1D);
 		tess.addVertexWithUV(par2, 0D, -90D, 1D, 0D);
 		tess.addVertexWithUV(0D, 0D, -90D, 0D, 0D);
 		tess.draw();
 		glDepthMask(true);
 		glEnable(GL_DEPTH_TEST);
 		glColor4f(1F, 1F, 1F, 1F);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 	}
 	
 	protected void renderPortal(final int width, final int height, final float partialTicks) {
         if (pre(PORTAL)) return;
 
         final float f1 = mc.thePlayer.prevTimeInPortal + (mc.thePlayer.timeInPortal - mc.thePlayer.prevTimeInPortal) * partialTicks;
 
         if (f1 > 0.0F)
         {
             renderPortalOverlay(f1, width, height);
         }
 
         post(PORTAL);
 	}
 
 	/**
 	 * Renders the portal overlay. Args: portalStrength, width, height
 	 */
 	@Override
 	public void renderPortalOverlay(float par1, final int par2, final int par3)
 	{
 		if (par1 < 1.0F)
 		{
 			par1 *= par1;
 			par1 *= par1;
 			par1 = par1 * 0.8F + 0.2F;
 		}
 
 		GL11.glDisable(GL11.GL_ALPHA_TEST);
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		GL11.glDepthMask(false);
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, par1);
 		this.mc.renderEngine.bindTexture("/terrain.png");
 		final Icon icon = Block.portal.getBlockTextureFromSide(1);
 		final float f1 = icon.getMinU();
 		final float f2 = icon.getMinV();
 		final float f3 = icon.getMaxU();
 		final float f4 = icon.getMaxV();
 		final Tessellator tessellator = Tessellator.instance;
 		tessellator.startDrawingQuads();
 		tessellator.addVertexWithUV(0.0D, (double)par3, -90.0D, (double)f1, (double)f4);
 		tessellator.addVertexWithUV((double)par2, (double)par3, -90.0D, (double)f3, (double)f4);
 		tessellator.addVertexWithUV((double)par2, 0.0D, -90.0D, (double)f3, (double)f2);
 		tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, (double)f1, (double)f2);
 		tessellator.draw();
 		GL11.glDepthMask(true);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 		GL11.glEnable(GL11.GL_ALPHA_TEST);
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 	}
 
 	@Override
 	public GuiNewChat getChatGUI() {
 		return presistentChatGui;
 	}
 	
     //Helper macros
     private boolean pre(final ElementType type)
     {
         return MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(eventParent, type));
     }
     private void post(final ElementType type)
     {
         MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(eventParent, type));
     }
 	
 }
