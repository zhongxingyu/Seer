 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.client;
 
 import cpw.mods.fml.common.ObfuscationReflectionHelper;
 import monnef.core.MonnefCorePlugin;
 import monnef.core.block.ContainerMonnefCore;
 import monnef.core.utils.ColorHelper;
 import monnef.core.utils.GuiHelper;
 import net.minecraft.client.gui.Gui;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.client.renderer.RenderHelper;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.inventory.Container;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.util.StatCollector;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import static monnef.core.client.ResourcePathHelper.ResourceTextureType.GUI;
 import static monnef.core.utils.GuiHelper.COLOR_DARK_GRAY;
 import static monnef.core.utils.GuiHelper.COLOR_GRAY;
 import static monnef.core.utils.GuiHelper.COLOR_WHITE;
 import static monnef.core.utils.GuiHelper.EnumFillRotation;
 import static monnef.core.utils.GuiHelper.drawPixel;
 import static monnef.core.utils.GuiHelper.setTessellatorColor;
 
 public abstract class GuiContainerMonnefCore extends GuiContainer {
     private static ArrayList<String> tooltips = new ArrayList<String>();
     protected int x;
     protected int y;
     private String backgroundTexture = "guimachine.png";
     private ResourceLocation backgroundTextureResource;
     private String modId;
     protected final ContainerMonnefCore myContainer;
     protected int lastMouseButtonProcessed = -1;
 
     public GuiContainerMonnefCore(Container container) {
         super(container);
         myContainer = (ContainerMonnefCore) container;
         ySize = myContainer.getYSize();
         xSize = myContainer.getXSize();
         refreshXY();
         setupModIdByCallerClass();
     }
 
     private void setupModIdByCallerClass() {
         modId = PackageToModIdRegistry.searchModIdFromCurrentPackage(getCallerDepthForPackageIdentification()); // <- mind depth if refactoring!
     }
 
     /**
      * Every non-end class should return super's value increased by 1.
      *
      * @return How deep in caller structure has to be walked to get correct package.
      */
     public int getCallerDepthForPackageIdentification() {
         return 2;
     }
 
     @Override
     public void initGui() {
         super.initGui();
         refreshXY();
     }
 
     protected boolean usesDoubleTexture() {
         return false;
     }
 
     protected String getContainerTitle() {
         return null;
     }
 
     @Override
     protected void drawGuiContainerForegroundLayer(int par1, int par2) {
         super.drawGuiContainerForegroundLayer(par1, par2);
         if (getContainerTitle() != null) {
             fontRenderer.drawString(getContainerTitle(), 8, 4, COLOR_DARK_GRAY);
         }
         fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8 + myContainer.getXPlayerInvShift(), ySize - 96 + 4 + myContainer.getYPlayerInvShift(), COLOR_DARK_GRAY);
     }
 
     @Override
     protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
         refreshXY();
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         bindBackgroundTexture();
         drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
     }
 
     protected void bindBackgroundTexture() {
         this.mc.renderEngine.bindTexture(getBackgroundTextureResource());
     }
 
     private ResourceLocation getBackgroundTextureResource() {
         if (backgroundTextureResource == null) {
             if (getModId().equals(""))
                 MonnefCorePlugin.Log.printWarning("Class " + this.getClass().getSimpleName() + " seems to not have properly set modId.");
             backgroundTextureResource = ResourcePathHelper.assembleAndCreate(getBackgroundTexture(), getModId(), GUI);
         }
         return backgroundTextureResource;
     }
 
     public static void drawPlasticBox(GuiContainerMonnefCore gui, int x, int y, int width, int height) {
         gui.drawHorizontalLine(x, width - 2, y, COLOR_DARK_GRAY);
         gui.drawVerticalLine(x, y, height - 1, COLOR_DARK_GRAY);
 
         gui.drawHorizontalLine(x + 1, width - 2, y + height - 1, COLOR_WHITE);
         gui.drawVerticalLine(x + width - 1, y, height - 1, COLOR_WHITE);
 
         drawPixel(x + width - 1, y, COLOR_GRAY);
         drawPixel(x, y + height - 1, COLOR_GRAY);
     }
 
     @Override
     public void drawHorizontalLine(int x, int width, int y, int color) {
         super.drawHorizontalLine(x, x + width, y, color);
     }
 
     @Override
     protected void drawVerticalLine(int x, int y, int height, int color) {
         super.drawVerticalLine(x, y, y + height, color);
     }
 
     public void drawBottomUpBar(int x, int y, int value, int width, int height, ColorHelper.IntColor topColor, ColorHelper.IntColor bottomColor) {
         drawPlasticBox(this, x, y, width, height);
         if (value != 0) {
             drawGradientRectFromDown(this, x + 1, y + 1, width - 2, height - 2, topColor, bottomColor, EnumFillRotation.TOP_DOWN, height - 2);
         }
         GuiHelper.drawRect(x + 1, y + 1, width - 2, height - 2 - value, COLOR_GRAY);
         GL11.glColor4f(1, 1, 1, 1);
     }
 
     @Override
    public void drawScreen(int mousex, int mousey, float par3) {
        super.drawScreen(mousex, mousey, par3);
        drawTooltips(mousex, mousey);
     }
 
     public List<String> fillTooltips(GuiContainer gui, int mousex, int mousey, List<String> currenttip) {
         return currenttip;
     }
 
     private void drawTooltips(int mousex, int mousey) {
         tooltips.clear();
         fillTooltips(this, mousex, mousey, tooltips);
         drawToolTip(tooltips, mousex, mousey);
     }
 
     // based on GuiContainer
     public void drawToolTip(List<String> tooltip, int x, int y) {
         GL11.glDisable(GL12.GL_RESCALE_NORMAL);
         RenderHelper.disableStandardItemLighting();
         GL11.glDisable(GL11.GL_LIGHTING);
         GL11.glDisable(GL11.GL_DEPTH_TEST);
 
         if (!tooltip.isEmpty()) {
             int maximalWidth = 0;
             Iterator iterator = tooltip.iterator();
 
             while (iterator.hasNext()) {
                 String currentText = (String) iterator.next();
                 int stringWidth = this.fontRenderer.getStringWidth(currentText);
 
                 if (stringWidth > maximalWidth) {
                     maximalWidth = stringWidth;
                 }
             }
 
             int xcoord = x + 12;
             int ycoord = y - 12;
             int yoffset = 8;
 
             if (tooltip.size() > 1) {
                 yoffset += 2 + (tooltip.size() - 1) * 10;
             }
 
             int color1 = -267386864;
             this.drawGradientRect(xcoord - 3, ycoord - 4, xcoord + maximalWidth + 3, ycoord - 3, color1, color1);
             this.drawGradientRect(xcoord - 3, ycoord + yoffset + 3, xcoord + maximalWidth + 3, ycoord + yoffset + 4, color1, color1);
             this.drawGradientRect(xcoord - 3, ycoord - 3, xcoord + maximalWidth + 3, ycoord + yoffset + 3, color1, color1);
             this.drawGradientRect(xcoord - 4, ycoord - 3, xcoord - 3, ycoord + yoffset + 3, color1, color1);
             this.drawGradientRect(xcoord + maximalWidth + 3, ycoord - 3, xcoord + maximalWidth + 4, ycoord + yoffset + 3, color1, color1);
             int color2 = 1347420415;
             int color3 = (color2 & 16711422) >> 1 | color2 & -16777216;
             this.drawGradientRect(xcoord - 3, ycoord - 3 + 1, xcoord - 3 + 1, ycoord + yoffset + 3 - 1, color2, color3);
             this.drawGradientRect(xcoord + maximalWidth + 2, ycoord - 3 + 1, xcoord + maximalWidth + 3, ycoord + yoffset + 3 - 1, color2, color3);
             this.drawGradientRect(xcoord - 3, ycoord - 3, xcoord + maximalWidth + 3, ycoord - 3 + 1, color2, color2);
             this.drawGradientRect(xcoord - 3, ycoord + yoffset + 2, xcoord + maximalWidth + 3, ycoord + yoffset + 3, color3, color3);
 
             for (int i = 0; i < tooltip.size(); ++i) {
                 String currTooltip = tooltip.get(i);
 
                 this.fontRenderer.drawStringWithShadow(currTooltip, xcoord, ycoord, -1);
 
                 if (i == 0) {
                     ycoord += 2;
                 }
 
                 ycoord += 10;
             }
         }
     }
 
     protected void refreshXY() {
         x = (width - xSize) / 2;
         y = (height - ySize) / 2;
     }
 
     protected void drawGradientRectWithSize(int x, int y, int width, int height, int colorA, int colorB) {
         super.drawGradientRect(x, y, x + width, y + height, colorA, colorB);
     }
 
     public void drawGradientRect(Gui gui, int x, int y, int width, int height, ColorHelper.IntColor topColor, ColorHelper.IntColor bottomColor, EnumFillRotation rotation) {
         drawGradientRectInternal(x, y, x + width, y + height, topColor, bottomColor, rotation, zLevel);
     }
 
     public void drawGradientRectFromDown(Gui gui, int x, int y, int width, int height, ColorHelper.IntColor topColor, ColorHelper.IntColor bottomColor, EnumFillRotation rotation, int maxHeight) {
         int r = maxHeight - height;
         drawGradientRectInternal(x, y + r, x + width, y + maxHeight, topColor, bottomColor, rotation, zLevel);
     }
 
     // heavily based on Gui.drawGradientRect
     private void drawGradientRectInternal(int x, int y, int x2, int y2, ColorHelper.IntColor colorTop, ColorHelper.IntColor colorBottom, EnumFillRotation rotation, double zLevel) {
         if (rotation == EnumFillRotation.LEFT_RIGHT) {
             ColorHelper.IntColor tmp = colorTop;
             colorTop = colorBottom;
             colorBottom = tmp;
         }
 
         GL11.glDisable(GL11.GL_TEXTURE_2D);
         GL11.glEnable(GL11.GL_BLEND);
         GL11.glDisable(GL11.GL_ALPHA_TEST);
         GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
         GL11.glShadeModel(GL11.GL_SMOOTH);
 
         Tessellator tessellator = Tessellator.instance;
         tessellator.startDrawingQuads();
         setTessellatorColor(tessellator, colorTop);
         tessellator.addVertex((double) x2, (double) y, zLevel);
         if (rotation == EnumFillRotation.LEFT_RIGHT)
             setTessellatorColor(tessellator, colorBottom);
         tessellator.addVertex((double) x, (double) y, zLevel);
         setTessellatorColor(tessellator, colorBottom);
         tessellator.addVertex((double) x, (double) y2, zLevel);
         if (rotation == EnumFillRotation.LEFT_RIGHT)
             setTessellatorColor(tessellator, colorTop);
         tessellator.addVertex((double) x2, (double) y2, zLevel);
         tessellator.draw();
 
         GL11.glShadeModel(GL11.GL_FLAT);
         GL11.glDisable(GL11.GL_BLEND);
         GL11.glEnable(GL11.GL_ALPHA_TEST);
         GL11.glEnable(GL11.GL_TEXTURE_2D);
     }
 
     public String getBackgroundTexture() {
         return backgroundTexture;
     }
 
     public void setBackgroundTexture(String backgroundTexture) {
         this.backgroundTexture = backgroundTexture;
     }
 
     protected String getModId() {
         return modId;
     }
 
     private void setModId(String modId) {
         this.modId = modId.toLowerCase();
     }
 
     public boolean isMouseInRect(int mouseX, int mouseY, int guiX, int guiY, int guiWidth, int guiHeight) {
         int x = (width - xSize) / 2;
         int y = (height - ySize) / 2;
         int mouseXinGui = mouseX - x;
         int mouseYinGui = mouseY - y;
         return mouseXinGui >= guiX && mouseXinGui < guiX + guiWidth &&
                 mouseYinGui >= guiY && mouseYinGui < guiY + guiHeight;
     }
 
     @Override
     public void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
         if (usesDoubleTexture()) {
             float uCoef = 0.00390625F / 2;
             float vCoef = 0.00390625F / 2;
             Tessellator tessellator = Tessellator.instance;
             tessellator.startDrawingQuads();
             tessellator.addVertexWithUV((double) (x + 0), (double) (y + height), (double) this.zLevel, (double) ((float) (u + 0) * uCoef), (double) ((float) (v + height) * vCoef));
             tessellator.addVertexWithUV((double) (x + width), (double) (y + height), (double) this.zLevel, (double) ((float) (u + width) * uCoef), (double) ((float) (v + height) * vCoef));
             tessellator.addVertexWithUV((double) (x + width), (double) (y + 0), (double) this.zLevel, (double) ((float) (u + width) * uCoef), (double) ((float) (v + 0) * vCoef));
             tessellator.addVertexWithUV((double) (x + 0), (double) (y + 0), (double) this.zLevel, (double) ((float) (u + 0) * uCoef), (double) ((float) (v + 0) * vCoef));
             tessellator.draw();
         } else {
             super.drawTexturedModalRect(x, y, u, v, width, height);
         }
     }
 
     @Override
     protected void mouseClicked(int x, int y, int mouseButton) {
         lastMouseButtonProcessed = mouseButton;
         super.mouseClicked(x, y, mouseButton);
         if (mouseButton == 0) return;
         for (int l = 0; l < this.buttonList.size(); ++l) {
             GuiButton button = (GuiButton) this.buttonList.get(l);
 
             if (button instanceof AdvancedButton && ((AdvancedButton) button).mousePressedAdvanced(mc, x, y, mouseButton)) {
                 setSelectedButton(button);
                 playButtonSound();
                 actionPerformed(button);
             }
         }
     }
 
     protected void setSelectedButton(GuiButton button) {
         ObfuscationReflectionHelper.setPrivateValue(GuiScreen.class, this, button, "selectedButton");
     }
 
     private void playButtonSound() {
         this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
     }
 }
