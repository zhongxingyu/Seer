 
 package me.heldplayer.mods.Smartestone.block;
 
 import java.util.Random;
 
 import me.heldplayer.mods.Smartestone.item.ItemRotator;
 import me.heldplayer.mods.Smartestone.packet.Packet2SerializeableTile;
 import me.heldplayer.mods.Smartestone.packet.PacketHandler;
 import me.heldplayer.mods.Smartestone.tileentity.TileEntityRotatable;
 import me.heldplayer.mods.Smartestone.util.Direction;
 import me.heldplayer.mods.Smartestone.util.Rotation;
 import me.heldplayer.mods.Smartestone.util.Side;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockPistonBase;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.IconFlipped;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public abstract class BlockMulti extends Block {
 
     protected static Random rnd = new Random();
     public static int renderId = 0;
 
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     protected Icon missing;
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     protected Icon[] bottom;
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     protected Icon[] top;
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     protected Icon[] front;
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     protected Icon[] back;
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     protected Icon[] left;
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     protected Icon[] right;
 
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     protected Icon[][] icons;
     protected Icon[][] flipped;
 
     protected int side = 0;
 
     public BlockMulti(int id, Material material) {
         super(id, material);
 
         this.setHardness(1.0F);
     }
 
     public abstract String getIdentifier(int meta);
 
     public boolean canBeRotated(int meta) {
         return true;
     }
 
     public boolean canBeRedirected(int meta) {
         return true;
     }
 
     public void setBlockBoundsForItemRender(int meta) {
         this.setBlockBounds(0.0F, 0.0F, 0.F, 1.0F, 1.0F, 1.0F);
     }
 
     @Override
     public boolean hasComparatorInputOverride() {
         return true;
     }
 
     @Override
     public abstract int getComparatorInputOverride(World world, int x, int y, int z, int side);
 
     @Override
     public int getRenderType() {
         return renderId;
     }
 
     @Override
     public int damageDropped(int meta) {
         return meta;
     }
 
     @Override
     public void breakBlock(World world, int x, int y, int z, int blockId, int meta) {
         if (world.isRemote) {
             return;
         }
 
         TileEntityRotatable tile = (TileEntityRotatable) world.getBlockTileEntity(x, y, z);
 
         if (tile != null && tile instanceof IInventory) {
             IInventory inventory = (IInventory) tile;
             for (int i = 0; i < inventory.getSizeInventory(); i++) {
                 ItemStack stack = inventory.getStackInSlot(i);
 
                 if (stack != null) {
                     float xMotion = rnd.nextFloat() * 0.8F + 0.1F;
                     float yMotion = rnd.nextFloat() * 0.8F + 0.1F;
                     float zMotion = rnd.nextFloat() * 0.8F + 0.1F;
 
                     while (stack.stackSize > 0) {
                         int size = rnd.nextInt(21) + 10;
 
                         if (size > stack.stackSize) {
                             size = stack.stackSize;
                         }
 
                         stack.stackSize -= size;
                         EntityItem item = new EntityItem(world, (x + xMotion), (y + yMotion), (z + zMotion), new ItemStack(stack.itemID, size, stack.getItemDamage()));
 
                         if (stack.hasTagCompound()) {
                             item.setEntityItemStack(stack);
                         }
 
                         item.motionX = ((float) rnd.nextGaussian() * 0.05F);
                         item.motionY = ((float) rnd.nextGaussian() * 0.05F + 0.2F);
                         item.motionZ = ((float) rnd.nextGaussian() * 0.05F);
                         world.spawnEntityInWorld(item);
                     }
                 }
             }
 
             world.func_96440_m(x, y, z, blockId);
         }
 
         super.breakBlock(world, x, y, z, blockId, meta);
     }
 
     @Override
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     public Icon getIcon(int side, int meta) {
         return this.icons[side][meta];
     }
 
     @Override
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side) {
         //CommonProxy.log.info("== Icon");
         TileEntityRotatable tile = (TileEntityRotatable) world.getBlockTileEntity(x, y, z);
 
         if (tile == null) {
             return this.missing;
         }
 
         Direction direction = tile.direction;
         Rotation rotation = tile.rotation;
         Side theSide = Side.getSide(side);
         Side relSide = direction.getRelativeSide(theSide);
 
         int meta = world.getBlockMetadata(x, y, z);
         Icon[] array = null;
 
         int index = rotation.getTextureIndex(theSide, direction);
 
         if (rotation.getTextureFlipped(relSide, direction)) {
             array = this.flipped[index];
         }
         else {
             array = this.icons[index];
         }
 
         //CommonProxy.log.info("Using the" + (direction.getTextureFlipped(relSide, rotation) ? " flipped" : "") + " icon array " + index + " for side " + side);
 
         return array == null ? this.missing : array[meta];
     }
 
     @Override
     public void onBlockAdded(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (this.hasTileEntity(meta)) {
            world.setBlockTileEntity(x, y, z, this.createTileEntity(world, meta));
        }
         super.onBlockAdded(world, x, y, z);
     }
 
     @Override
     public int onBlockPlaced(World world, int x, int y, int z, int side, float posX, float posY, float posZ, int meta) {
         if (side == 1) {
             side = 0;
         }
         else if (side == 0) {
             side = 1;
         }
 
         this.side = side;
         return super.onBlockPlaced(world, x, y, z, side, posX, posY, posZ, meta);
     }
 
     @Override
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack) {
         TileEntityRotatable tile = (TileEntityRotatable) world.getBlockTileEntity(x, y, z);
 
         if (tile == null) {
             return;
         }
 
         int rotation = BlockPistonBase.determineOrientation(world, x, y, z, entity);
 
         tile.direction = Direction.getDirection(rotation);
         tile.rotation = Rotation.DEFAULT;
 
         if (itemStack.hasDisplayName()) {
             tile.customName = itemStack.getDisplayName();
         }
     }
 
     @Override
     public boolean onBlockEventReceived(World world, int x, int y, int z, int blockId, int eventId) {
         if (world.isRemote) {
             return false;
         }
 
         super.onBlockEventReceived(world, x, y, z, blockId, eventId);
         TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 
         if (tileEntity != null) {
             tileEntity.receiveClientEvent(blockId, eventId);
         }
 
         return true;
     }
 
     @Override
     @SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
     public void registerIcons(IconRegister register) {
         this.icons = new Icon[][] { this.bottom, this.top, this.front, this.back, this.left, this.right };
         this.flipped = new Icon[6][16];
 
         for (int i = 0; i < this.icons.length; i++) {
             for (int j = 0; j < this.icons.length; j++) {
                 this.flipped[i][j] = new IconFlipped(this.icons[i][j], true, false);
             }
         }
     }
 
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int clickedSide, float posX, float posY, float posZ) {
         ItemStack clickedStack = player.getCurrentEquippedItem();
 
         if (clickedStack != null && clickedStack.getItem() instanceof ItemRotator) {
             if (!this.hasTileEntity(world.getBlockMetadata(x, y, z))) {
                 return false;
             }
 
             if (world.isRemote) {
                 return true;
             }
 
             TileEntityRotatable tile = (TileEntityRotatable) world.getBlockTileEntity(x, y, z);
 
             if (tile == null) {
                 return false;
             }
 
             int meta = world.getBlockMetadata(x, y, z);
 
             if (player.isSneaking() && this.canBeRedirected(meta)) {
                 tile.direction = tile.direction.next();
             }
             else if (this.canBeRotated(meta)) {
                 tile.rotation = tile.rotation.next();
             }
 
             try {
                 Packet2SerializeableTile packet = new Packet2SerializeableTile(tile);
                 ((EntityPlayerMP) player).playerNetServerHandler.netManager.addToSendQueue(PacketHandler.instance.createPacket(packet));
             }
             catch (ClassCastException e) {}
 
             return true;
         }
 
         return false;
     }
 
 }
