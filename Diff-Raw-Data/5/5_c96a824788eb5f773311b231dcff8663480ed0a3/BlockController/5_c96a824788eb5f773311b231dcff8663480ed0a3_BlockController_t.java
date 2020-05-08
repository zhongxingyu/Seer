 
 package net.specialattack.modjam.block;
 
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.specialattack.modjam.PacketHandler;
 import net.specialattack.modjam.client.gui.GuiBasicController;
 import net.specialattack.modjam.client.gui.GuiController;
 import net.specialattack.modjam.client.render.BlockRendererConsole;
 import net.specialattack.modjam.tileentity.TileEntityController;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockController extends Block {
 
     private int renderId;
 
     public BlockController(int blockId) {
         super(blockId, Material.piston);
         this.renderId = RenderingRegistry.getNextAvailableRenderId();
         RenderingRegistry.registerBlockHandler(this.renderId, new BlockRendererConsole(this.renderId));
     }
 
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float posX, float posY, float posZ) {
         TileEntity tile = world.getBlockTileEntity(x, y, z);
 
         if (tile != null && tile instanceof TileEntityController) {
             TileEntityController controller = (TileEntityController) tile;
 
             if (player.isSneaking()) {
                int meta = world.getBlockMetadata(x, y, z);
                if (meta == 1) {
                    controller.startStop();
                }
             }
             else {
                 if (world.isRemote) {
                     int meta = world.getBlockMetadata(x, y, z);
                     if (meta == 0) {
                         FMLClientHandler.instance().displayGuiScreen(player, new GuiBasicController(controller));
                     }
                     else if (meta == 1) {
                         FMLClientHandler.instance().displayGuiScreen(player, new GuiController(controller));
                     }
                 }
                 else {
                     if (player instanceof EntityPlayerMP) {
                         Packet packet = PacketHandler.createPacket(5, tile);
                         if (packet != null) {
                             ((EntityPlayerMP) player).playerNetServerHandler.sendPacketToPlayer(packet);
                         }
                     }
                 }
             }
         }
 
         return true;
     }
 
     @Override
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
         super.onBlockPlacedBy(world, x, y, z, entity, stack);
         world.setBlockMetadataWithNotify(x, y, z, stack.getItemDamage(), 0);
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     @Override
     @SideOnly(Side.CLIENT)
     public void getSubBlocks(int itemId, CreativeTabs tab, List list) {
         list.add(new ItemStack(itemId, 1, 0));
         list.add(new ItemStack(itemId, 1, 1));
     }
 
     @Override
     public TileEntity createTileEntity(World world, int metadata) {
         return new TileEntityController();
     }
 
     @Override
     public boolean hasTileEntity(int metadata) {
         return true;
     }
 
     @Override
     public boolean renderAsNormalBlock() {
         return false;
     }
 
     @Override
     public boolean isOpaqueCube() {
         return false;
     }
 
     @Override
     public int getRenderType() {
         return this.renderId;
     }
 
 }
