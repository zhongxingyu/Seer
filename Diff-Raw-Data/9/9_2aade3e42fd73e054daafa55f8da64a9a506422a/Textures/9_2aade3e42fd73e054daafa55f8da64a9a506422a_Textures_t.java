 package mods.minecraft.darth.dc.lib;
 
 import mods.minecraft.darth.dc.core.util.ResourceLocationUtil;
 import net.minecraft.client.renderer.texture.TextureMap;
 import net.minecraft.util.ResourceLocation;
 
 public class Textures
 {
 
     public static final String GUI_SHEET_LOCATION = "textures/gui/";
     
     public static final ResourceLocation VANILLA_BLOCK_TEXTURE_SHEET = TextureMap.field_110575_b;
     public static final ResourceLocation VANILLA_ITEM_TEXTURE_SHEET = TextureMap.field_110576_c;
    public static final ResourceLocation VANILLA_INVENTORY = new ResourceLocation("textures/gui/container/inventory.png");
     
     public static final ResourceLocation GUI_SA1 = ResourceLocationUtil.getResourceLocation(GUI_SHEET_LOCATION + "SAInventory.png");
 }
