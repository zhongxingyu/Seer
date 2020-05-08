 package com.madpc.coffee.block;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 import com.madpc.coffee.gui.GuiCoffeeMaker;
 import com.madpc.coffee.tileentity.TileEntityCoffeeMaker;
 
 public class BlockCoffeeMaker extends BlockContainer {
     
     public BlockCoffeeMaker(int par1) {
         super(par1, Material.rock);
     }
     
     @Override
     public TileEntity createNewTileEntity(World world) {
         return new TileEntityCoffeeMaker();
     }
     
     @Override
     public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
         if (worldObj.isRemote) {
             return true;
         } else {
             TileEntityCoffeeMaker entity = (TileEntityCoffeeMaker) worldObj.getBlockTileEntity(x, y, z);
             
             if (entity != null) {
                 //if (worldObj.isRemote) {
                 // Complex server stuff...
                 //} else
                 Minecraft.getMinecraft().displayGuiScreen(new GuiCoffeeMaker(player, entity));
             }
             
             return true;
         }
     }
 }
