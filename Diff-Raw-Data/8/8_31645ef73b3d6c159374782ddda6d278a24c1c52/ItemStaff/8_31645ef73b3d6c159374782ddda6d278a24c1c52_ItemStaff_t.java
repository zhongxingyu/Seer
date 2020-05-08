 package mods.themike.modjam.items;
 
 import java.util.List;
 import java.util.Random;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import mods.themike.modjam.ModJam;
 import mods.themike.modjam.rune.IRune;
 import mods.themike.modjam.rune.RuneRegistry;
 import mods.themike.modjam.utils.ColorUtils;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class ItemStaff extends ItemMulti {
 
 	
 	protected static String[] subNames = new String[]{"apprentice", "mage"};
 	private static Icon[] subIcons = new Icon[subNames.length];
 	
 	public ItemStaff(int par1) {
 		super(par1);
 		this.hasSubtypes = true;
 		this.setUnlocalizedName("itemStaff");
 		this.setCreativeTab(ModJam.tab);
 	}
 	
 	@Override
 	public Icon getIconFromDamage(int metadata) {
 		return subIcons[metadata];
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void updateIcons(IconRegister reg) {
 		for(int par1 = 0; par1 < subNames.length; par1++) {
 			subIcons[par1] = reg.registerIcon("mikejam:staff" + subNames[par1]);
 		}
 	}
 	
 	@Override
 	public void getSubItems(int ID, CreativeTabs tabs, List list) {
 		for(int par1 = 0; par1 < subNames.length; par1++) {
 			ItemStack stack = new ItemStack(ID, 1, par1);
 			stack.setTagCompound(new NBTTagCompound());
 			list.add(stack);
 		}
 	}
 	
 	@Override
 	public String getUnlocalizedName(ItemStack stack) {
 		if(stack.getItemDamage() >= 0 && stack.getItemDamage() < subNames.length) {
 			return "staff." + subNames[stack.getItemDamage()];
 		}
 		return null;
 	}
 	
 	@Override 
 	public boolean hasEffect(ItemStack stack) {
 		if(stack.getItemDamage() == 1) {
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
 		if(stack.getItemDamage() == 0) {
 			list.add("Can bind up to Level 25.");
 		} else {
 			list.add("Can bind all levels.");
 		}
 		if(stack.getTagCompound().getTag("item") != null) {
 			ItemStack runeStack = ItemStack.loadItemStackFromNBT((NBTTagCompound) stack.getTagCompound().getTag("item"));
 			if(stack.getTagCompound().getTag("item") != null && runeStack != null) {
 				list.add(ColorUtils.applyColor(14) + LanguageRegistry.instance().getStringLocalization("rune." + RuneRegistry.getrunes()[runeStack.getItemDamage()].getName() + ".name") + ".");
 			}
 		}
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
 		super.onItemRightClick(stack, world, player);
 		ItemStack newStack = stack.copy();
 		
		if(!world.isRemote && !player.isSneaking() && stack.getTagCompound().getTag("item") == null) {
				player.sendChatToPlayer(ColorUtils.applyColor(12) + "No Rune selected!");
		}
 		if(!world.isRemote && !player.isSneaking() && stack.getTagCompound().getTag("item") != null) {
 			ItemStack runeStack = ItemStack.loadItemStackFromNBT((NBTTagCompound) stack.getTagCompound().getTag("item"));
 			if(runeStack != null && player.experienceLevel  >= RuneRegistry.getrunes()[runeStack.getItemDamage()].getLevel()) {
 				IRune rune = RuneRegistry.getrunes()[runeStack.getItemDamage()];
 				player.addExperienceLevel(- RuneRegistry.getrunes()[runeStack.getItemDamage()].getLevel());
 				rune.onUse(player);
 				int uses = runeStack.getTagCompound().getInteger("uses");
 				if(uses == 1) {
 					NBTTagCompound tag = new NBTTagCompound();
 					newStack.getTagCompound().setTag("item", tag);
 				} else {
 					runeStack.getTagCompound().setInteger("uses", uses - 1);
 					NBTTagCompound tag = new NBTTagCompound();
 					runeStack.writeToNBT(tag);
 					newStack.getTagCompound().setTag("item", tag);
 				}
 			} else if(runeStack != null) {
 				player.sendChatToPlayer(ColorUtils.applyColor(14) + "Not enough XP to use this rune!");
			} 
 		}
 		if(world.isRemote && !player.isSneaking() && stack.getTagCompound() != null) {
 			if(stack.getTagCompound().getTag("item") != null) {
 				ItemStack runeStack = ItemStack.loadItemStackFromNBT((NBTTagCompound) stack.getTagCompound().getTag("item"));
 				if(runeStack != null && player.experienceLevel  >= RuneRegistry.getrunes()[runeStack.getItemDamage()].getLevel()) {
 					player.playSound("mods.mikejam.sounds.sucess", 1.0F, 1.0F);
 				} else if(runeStack != null) {
 					player.playSound("mods.mikejam.sounds.failure", 1.0F, 1.0F);
 				}
 			}
 		}
 		if(player.isSneaking()) {
 			player.openGui(ModJam.instance, 1, world, (int) player.posX, (int) player.posY, (int) player.posZ);
 		}
 		return newStack;
 	}
 
 }
