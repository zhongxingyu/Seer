 package assets.fyresmodjam;
 
 import java.util.List;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.client.resources.I18n;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityPotion;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.util.Icon;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 
 public class ItemMysteryPotion extends Item {
 
 	public static Icon[] icons = null;
 	
 	public ItemMysteryPotion(int par1) {
 		super(par1);
 	}
 	
 	@SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister) {
 		if(icons == null) {
 			icons = new Icon[13];
 			for(int i = 0; i < 13; i++) {icons[i] = par1IconRegister.registerIcon("fyresmodjam:mysteryPotion_" + (i + 1));}
 		}
 		
         this.itemIcon = icons[0];
     }
 	
 	@SideOnly(Side.CLIENT)
     public Icon getIconFromDamage(int par1)  {
         return par1 < 13 ? icons[par1] : icons[0];
     }
 	
 	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
         for(int i = 0; i < 13; i++) {par3List.add(new ItemStack(par1, 1, i));}
     }
 	
 	public String getItemDisplayName(ItemStack par1ItemStack) {
 		String name = "Mystery Potion #" + (par1ItemStack.getItemDamage() + 1);
 		
 		if(par1ItemStack.getItemDamage() < 12 && Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().theWorld.isRemote) {
 			if(Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.getEntityData().hasKey("PotionKnowledge")) {
 				if(Minecraft.getMinecraft().thePlayer.getEntityData().getIntArray("PotionKnowledge")[par1ItemStack.getItemDamage()] != -1) {
 					Potion potion = Potion.potionTypes[MysteryPotionData.potionValues[par1ItemStack.getItemDamage()]];
 					name = I18n.func_135053_a(potion.getName()) + " Potion";
 					
 					if(!potion.isInstant()) {
 						int time = MysteryPotionData.potionDurations[par1ItemStack.getItemDamage()];
 						name += " (" + time + " seconds)";
 					}
 				}
 			}
 		} else if(par1ItemStack.getItemDamage() >= 12) {
 			name = "Wildcard Potion";
 		}
 		
         return name;
     }
 	
 	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
         return EnumAction.drink;
     }
 	
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
         par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
         return par1ItemStack;
     }
 	
 	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
         return 32;
     }
 	
 	public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
         if(!par3EntityPlayer.capabilities.isCreativeMode) {--par1ItemStack.stackSize;}
         
         if(par1ItemStack.getItemDamage() < 12) {
 	        if(!par2World.isRemote) {
 	        	int value = MysteryPotionData.potionValues[par1ItemStack.getItemDamage()];
 	        	
 	        	if(!Potion.potionTypes[value].isInstant()) {
	        		par3EntityPlayer.addPotionEffect(new PotionEffect(value, MysteryPotionData.potionDurations[par1ItemStack.getItemDamage()] * 20, 1, false));
 	        	} else {
 	        		Potion.potionTypes[value].affectEntity(par3EntityPlayer, par3EntityPlayer, 1, 1);
 	        	}
 	        	
 	        	if(!par3EntityPlayer.getEntityData().hasKey("PotionKnowledge")) {par3EntityPlayer.getEntityData().setIntArray("PotionKnowledge", new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});}
 	        	par3EntityPlayer.getEntityData().getIntArray("PotionKnowledge")[par1ItemStack.getItemDamage()] = 1;
 	        	PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_POTION_KNOWLEDGE, new Object[] {par3EntityPlayer.getEntityData().getIntArray("PotionKnowledge")}), (Player) par3EntityPlayer);
 	        } else if(!par3EntityPlayer.getEntityData().hasKey("PotionKnowledge") || par3EntityPlayer.getEntityData().getIntArray("PotionKnowledge")[par1ItemStack.getItemDamage()] == -1) {
 	        	Potion potion = Potion.potionTypes[MysteryPotionData.potionValues[par1ItemStack.getItemDamage()]];
 				String name = I18n.func_135053_a(potion.getName()) + " Potion";
 				
 				if(!potion.isInstant()) {
 					int time = MysteryPotionData.potionDurations[par1ItemStack.getItemDamage()];
 					name += " (" + time + " seconds)";
 				}
 				
 	        	Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("\u00A7oYou learnt Mystery Potion #" + (par1ItemStack.getItemDamage() + 1) + " was a " + name + "!");
 	        }
         } else if(!par2World.isRemote) {
         	int value = ModjamMod.r.nextInt(Potion.potionTypes.length);
        	while(Potion.potionTypes[value] == null) {value = ModjamMod.r.nextInt(Potion.potionTypes.length);}
         	
         	if(!Potion.potionTypes[value].isInstant()) {
        		par3EntityPlayer.addPotionEffect(new PotionEffect(value, (5 + ModjamMod.r.nextInt(26)) * 20, 1, false));
         	} else {
         		Potion.potionTypes[value].affectEntity(par3EntityPlayer, par3EntityPlayer, 1, 1);
         	}
         }
         
         return par1ItemStack;
     }
 	
 	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
         return false;
     }
 
 }
