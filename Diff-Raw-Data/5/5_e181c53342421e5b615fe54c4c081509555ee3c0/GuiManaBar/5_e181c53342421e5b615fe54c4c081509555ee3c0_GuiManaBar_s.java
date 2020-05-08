 package hunternif.mc.dota2items.client.gui;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.core.EntityStats;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.Gui;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.attributes.AttributeInstance;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.ResourceLocation;
 import net.minecraftforge.client.event.RenderGameOverlayEvent;
 import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.event.ForgeSubscribe;
 
 import org.lwjgl.opengl.GL11;
 
 public class GuiManaBar extends Gui {
 	private static final ResourceLocation texture = new ResourceLocation(Dota2Items.ID+":textures/gui/mana.png");
 	
 	private static final int HIGHLIGHT_TIME = 10; // [ticks]
 	/** Let's call the 10 discrete mana units on screen "drops". */
 	private static final int HALF_DROPS_COUNT = 20;
 	
 	private Minecraft mc;
 	private int prevMana = 0;
 	private long lastChange = 0;
 	
 	public static int yPos;
 	
 	public GuiManaBar(Minecraft mc) {
 		this.mc = mc;
 	}
 	
 	@ForgeSubscribe
	public void onRenderArmor(RenderGameOverlayEvent event) {
 		// Only interested in Post-ExperienceBar events (the end of overlay rendering)
		if (event.isCancelable() || event.type != ElementType.EXPERIENCE || mc.thePlayer.capabilities.isCreativeMode) {
 			return;
 		}
 		EntityStats stats = Dota2Items.mechanics.getOrCreateEntityStats(mc.thePlayer);
 		if (stats.getMaxMana() == 0) {
 			return;
 		}
 		float halfDrop = (float)stats.getMaxMana() / (float)HALF_DROPS_COUNT;
 		int mana = MathHelper.floor_float((float)stats.getMana() / halfDrop);
 		long ticksSinceLastChange = mc.thePlayer.ticksExisted - lastChange;
 		boolean highlight = ticksSinceLastChange <= HIGHLIGHT_TIME && ticksSinceLastChange / 3 % 2 == 1;
 		
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		GL11.glDisable(GL11.GL_LIGHTING);
 		mc.renderEngine.func_110577_a(texture);
 		
 		int width = event.resolution.getScaledWidth();
 		int height = event.resolution.getScaledHeight();
 
 		int left = width / 2 - 91;
 		int top = height - 39;
 		
 		// Account for health bars:
 		AttributeInstance attrMaxHealth = this.mc.thePlayer.func_110148_a(SharedMonsterAttributes.field_111267_a);
 		float healthMax = (float)attrMaxHealth.func_111126_e();
 		float absorb = this.mc.thePlayer.func_110139_bj();
 		int healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F / 10.0F);
 		int rowHeight = Math.max(10 - (healthRows - 2), 3);
 		top -= healthRows * rowHeight;
 
 		// Account for armor:
 		if (ForgeHooks.getTotalArmorValue(mc.thePlayer) > 0) {
 			top -= 10;
 		}
 		yPos = top;
 
 		int regen = -1;
 
 		for (int i = 0; i < 10; ++i) {
 			int idx = i * 2 + 1;
 
 			int x = left + i * 8;
 			int y = top;
 			if (i == regen) {
 				y -= 2;
 			}
 
 			drawTexturedModalRect(x, y, (highlight ? 9 : 0), 0, 9, 9);
 
 			if (highlight) {
 				if (idx < prevMana) {
 					drawTexturedModalRect(x, y, 54, 0, 9, 9);
 				} else if (idx == prevMana) {
 					drawTexturedModalRect(x, y, 63, 0, 9, 9);
 				}
 			}
 
 			if (idx < mana) {
 				drawTexturedModalRect(x, y, 36, 0, 9, 9);
 			} else if (idx == mana) {
 				drawTexturedModalRect(x, y, 45, 0, 9, 9);
 			}
 		}
 		
 		if (prevMana != mana) {
 			lastChange = mc.thePlayer.ticksExisted;
 		}
 		prevMana = mana;
 	}
 }
