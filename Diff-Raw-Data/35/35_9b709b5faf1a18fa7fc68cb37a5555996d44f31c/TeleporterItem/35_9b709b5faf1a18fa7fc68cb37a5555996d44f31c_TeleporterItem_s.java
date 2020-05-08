 package teamm.mods.virtious.item;
 
 import java.util.List;
 import java.util.Random;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 import teamm.mods.virtious.Virtious;
 import teamm.mods.virtious.entity.item.EntityStickyBomb;
 import teamm.mods.virtious.network.StickyBombPacket;
 import teamm.mods.virtious.world.VirtiousTeleporter;
 import teamm.mods.virtious.world.biome.BiomeGenVirtiousOcean;
 import teamm.mods.virtious.world.gen.VirtiousGenAmberTree;
 import teamm.mods.virtious.world.gen.component.RuinedTempleHall;
 import teamm.mods.virtious.world.gen.component.RuinedTempleHallBroken;
 import teamm.mods.virtious.world.gen.component.RuinedTempleHallCollapsed;
 import teamm.mods.virtious.world.gen.component.RuinedTempleHallCracked;
 import teamm.mods.virtious.world.gen.component.RuinedTempleIntersectChest;
 import teamm.mods.virtious.world.gen.component.RuinedTempleIntersectEmpty;
 import teamm.mods.virtious.world.gen.component.RuinedTempleIntersectTrap;
 import teamm.mods.virtious.world.gen.component.RuinedTempleMain;
 import teamm.mods.virtious.world.gen.component.RuinedTempleOreRoom;
 import teamm.mods.virtious.world.gen.component.RuinedTempleStairs;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.world.World;
 
 public class TeleporterItem extends VirtiousItem{
 
 	public TeleporterItem(int id) {
 		super(id);
 	}
     
 	@Override
 	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int i, int j, int k, int par7, float par8, float par9, float par10)
 	{
 		if(!world.isRemote){
			player.addChatMessage("Metadata: " + world.getBlockMetadata(i, j, k));
 			
			EntityStickyBomb bomb = new EntityStickyBomb(world, i, j+2, k, player);
			world.spawnEntityInWorld(bomb);
 		}
 		return false;
 		
 	}
 
 	@Override
 	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player){
 		
 		if(player.isSneaking() && (player instanceof EntityPlayerMP)){
 			EntityPlayerMP playermp = (EntityPlayerMP) player;
 			
 			if(player.dimension == Virtious.dimensionID)
 			{
 				playermp.mcServer.getConfigurationManager().transferPlayerToDimension(playermp, 0,
 						new VirtiousTeleporter(playermp.mcServer.worldServerForDimension(0)));
 			} else {
 				playermp.mcServer.getConfigurationManager().transferPlayerToDimension(playermp, Virtious.dimensionID,
 						new VirtiousTeleporter(playermp.mcServer.worldServerForDimension(Virtious.dimensionID)));
 			}
 		}
 		
 		
 		return item;
 	}
 	
     public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
     {
     	par3List.add(EnumChatFormatting.BLUE + "Too lazy to make a portal? Shift, then right click to insta-warp.");
     }
 }
