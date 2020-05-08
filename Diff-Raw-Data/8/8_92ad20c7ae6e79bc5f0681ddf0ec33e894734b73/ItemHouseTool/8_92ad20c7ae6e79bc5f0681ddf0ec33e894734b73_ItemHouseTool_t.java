 package mrkirby153.MscHouses.items;
 
 import java.util.ArrayList;
 
 import mrkirby153.MscHouses.api.FuelRegistry;
 import mrkirby153.MscHouses.api.IHouseItem;
 import mrkirby153.MscHouses.api.MaterialRegistry;
 import mrkirby153.MscHouses.configuration.ConfigurationSettings;
 import mrkirby153.MscHouses.core.MscHouses;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * 
  * Msc Houses
  *
  * ItemHouseTool
  *
  * @author mrkirby153
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public class ItemHouseTool extends Item {
 	private IInventory inventory;
 	public ItemHouseTool(int par1) {
 		super(par1);
 		this.setCreativeTab(MscHouses.tabHouse);
 		this.setMaxDamage(ConfigurationSettings.House_Tool_damage);
 	}
 
 	@Override
 	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
 		MovingObjectPosition p = this.getMovingObjectPositionFromPlayer(world, player, true);
		if(p != null)
			if(world.getBlockTileEntity(p.blockX, p.blockY, p.blockZ) != null)
				inventory = (IInventory) world.getBlockTileEntity(p.blockX, p.blockY, p.blockZ);
 		if(inventory != null){
 			boolean fuel = false;
 			if(player.capabilities.isCreativeMode){
 				fuel = true;
 			}
 			int moduelId = 0;
 			int modifyerMaterial = 0;
 			if(inventory.getStackInSlot(0) != null){
 				if(inventory.getStackInSlot(0).getItem() instanceof IHouseItem){
 					((IHouseItem) inventory.getStackInSlot(0).getItem()).geneateInit(world, p.blockX, p.blockY, p.blockZ);
 				}else{
 					if(inventory.getStackInSlot(0).getItem() == MscHouses.moduel)
 						moduelId = inventory.getStackInSlot(0).getItemDamage();
 				}
 			}
 			if(inventory.getStackInSlot(2) != null){
 				modifyerMaterial = MaterialRegistry.materialLookup(inventory.getStackInSlot(2).getItemDamage());
 			}
 
 			if(inventory.getStackInSlot(1) != null){
 				ArrayList<Item> valid = FuelRegistry.getValidFuelsAsArrayList();
 				if(valid.contains(inventory.getStackInSlot(1).getItem())){
 					fuel = true;
 				}else{
 					if(!world.isRemote){
 						player.sendChatToPlayer(ChatMessageComponent.func_111066_d("No valid fuel detected in inventory"));
 					}
 				}
 			}
 			if(fuel){
 				if(!player.capabilities.isCreativeMode){
 					inventory.setInventorySlotContents(0, null);
 					inventory.setInventorySlotContents(1, null);
 					inventory.setInventorySlotContents(2, null);
 					player.addPotionEffect(new PotionEffect(15, 200, 0, true));
 					player.addPotionEffect(new PotionEffect(9, 200, 0, true));
 				}
 				buildHouse(moduelId, modifyerMaterial, p.blockX, p.blockY, p.blockZ, world);

 			}
 
 		}
 
 		return item;
 	}
 
 	private void buildHouse(int moduelId, int materialId, int x, int y, int z, World world){
 		switch(moduelId){
 		case 1: MscHouses.h.hut(x, y, z, world, materialId); break;
 		case 2: MscHouses.h.ninebynine(world, x, y, z, materialId); break;
 		case 3: MscHouses.h.ninbynineDelux(world, x, y, z, materialId); break;
 		case 4: MscHouses.h.netherAlter(world, x, y, z); break;
 		case 5: MscHouses.h.enchanter(world, x, y, z); break;
 		}
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		// TODO Auto-generated method stub
 		itemIcon = iconRegister.registerIcon("mschouses:HouseTool");
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean hasEffect(ItemStack par1ItemStack) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 }
