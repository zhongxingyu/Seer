 package chrisclark13.minecraft.artificing.item;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 
 import org.lwjgl.input.Keyboard;
 
 import chrisclark13.minecraft.artificing.Artificing;
 import chrisclark13.minecraft.artificing.lib.GuiIds;
 import chrisclark13.minecraft.artificing.lib.Reference;
 import chrisclark13.minecraft.artificing.lib.Strings;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 
 public class ItemGuideBook extends ItemArtificingGeneral {
 
     public final static String[] GUIDE_NAMES = { "Starting" , "Runes" };
 
     @SideOnly(Side.CLIENT)
     private Icon[]               icons;
 
     public ItemGuideBook(int id) {
 
         super(id);
         this.setMaxStackSize(1);
         this.setUnlocalizedName(Strings.BOOK_NAME);
         this.setCreativeTab(Artificing.tabArtificing);
         this.setHasSubtypes(true);
     }
 
     @Override
     public String getUnlocalizedName(ItemStack itemstack) {
 
         int meta = MathHelper.clamp_int(itemstack.getItemDamage(), 0, 3);
         return super.getUnlocalizedName() + GUIDE_NAMES[meta];
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIconFromDamage(int meta) {
 
         int j = MathHelper.clamp_int(meta, 0, 3);
         return icons[j];
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconregister) {
 
         icons = new Icon[GUIDE_NAMES.length];
 
         for (int i = 0; i < GUIDE_NAMES.length; ++i) {
             icons[i] = iconregister.registerIcon(Reference.MOD_ID + ":" + Strings.BOOK_NAME
                     + GUIDE_NAMES[i]);
         }
     }
 
     @Override
     public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
 
         if (!itemstack.isItemEqual(new ItemStack(ModItems.guideBook, 1, 0))) {
             return itemstack;
 
         } else {
             player.openGui(Artificing.instance, GuiIds.STARTING_BOOK, world, 0, 0, 0);
         }
 
         return itemstack;
     }
 
     @Override
     public String getItemDisplayName(ItemStack itemStack) {
 
         int meta = MathHelper.clamp_int(itemStack.getItemDamage(), 0, 3);
 
         switch (meta) {
             case 0:
                 return EnumChatFormatting.LIGHT_PURPLE + super.getItemDisplayName(itemStack);
             case 1:
                 return EnumChatFormatting.AQUA + super.getItemDisplayName(itemStack);
             default:
                 return EnumChatFormatting.WHITE + super.getItemDisplayName(itemStack);
         }
     }
 
     @SuppressWarnings({ "unchecked" , "rawtypes" })
     @Override
     @SideOnly(Side.CLIENT)
     public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean debug) {
         int meta = MathHelper.clamp_int(itemStack.getItemDamage(), 0, 3);
         switch (meta) {
 
             case 0:
                 list.add("Author: Cyntain of Minecraftia");
                 list.add("");
                 list.add("Press " + "6" + "shift" + "7" + " to see more information");
                 
                 if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
                         || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
 
                     list.clear();
                     list.add(EnumChatFormatting.LIGHT_PURPLE + super.getItemDisplayName(itemStack));
                     list.add("Author: Cyntain I");
                     list.add("");
                     list.add("This book gives information about how to get");
                     list.add("started with Artificing; including recipes,");
                     list.add("what is what, and a guide to the world!");
                     break;
                 } else {
                     break;
                 }
 
             case 1:
                 list.add("Author: Chris Clark of The 4th Wall");
                 list.add("");
                 list.add("Press " + "6" + "shift" + "7" + " to see more information");
 
                 if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
                         || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
 
                     list.clear();
                     list.add(EnumChatFormatting.AQUA + super.getItemDisplayName(itemStack));      
                     list.add("Author: Chris Clark XIII");
                     list.add("");
                     list.add("This book gives information about the");
                     list.add("different runes. This includes what they");
                     list.add("do, What effects they do, and more!");
                     break;
                 } else {
                     break;
                 }
         }
 
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void getSubItems(int id, CreativeTabs creativeTab, List list) {
 
         for (int meta = 0; meta < 2; ++meta) {
             list.add(new ItemStack(id, 1, meta));
         }
     }
 }
