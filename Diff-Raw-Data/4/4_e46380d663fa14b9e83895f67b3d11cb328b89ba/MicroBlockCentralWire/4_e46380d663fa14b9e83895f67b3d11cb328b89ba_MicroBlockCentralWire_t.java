 
 package me.heldplayer.api.Smartestone.micro.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.heldplayer.api.Smartestone.micro.IMicroBlock;
 import me.heldplayer.api.Smartestone.micro.IMicroBlockMaterial;
 import me.heldplayer.api.Smartestone.micro.MicroBlockAPI;
 import me.heldplayer.api.Smartestone.micro.MicroBlockInfo;
 import me.heldplayer.api.Smartestone.micro.rendering.RenderFaceHelper;
 import me.heldplayer.api.Smartestone.micro.rendering.ReusableRenderFace;
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.World;
 import net.minecraftforge.client.event.DrawBlockHighlightEvent;
 import net.minecraftforge.common.ForgeDirection;
 
 public class MicroBlockCentralWire extends MicroBlockImpl {
 
     private int position;
 
     public MicroBlockCentralWire(String typeName) {
         super(typeName);
         this.renderBounds = new double[] { 0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D };
 
         this.position = 0;
     }
 
     @Override
     public AxisAlignedBB getBoundsInBlock(MicroBlockInfo info) {
         return AxisAlignedBB.getAABBPool().getAABB(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);
     }
 
     @Override
     public boolean isMaterialApplicable(IMicroBlockMaterial material) {
         return material.getClass() == WireMaterial.class;
     }
 
     @Override
     public ReusableRenderFace[] getRenderFaces(MicroBlockInfo info) {
         int data = info.getData();
         boolean bottom = ((data >> 0) & 0x1) == 1;
         boolean top = ((data >> 1) & 0x1) == 1;
         boolean north = ((data >> 2) & 0x1) == 1;
         boolean south = ((data >> 3) & 0x1) == 1;
         boolean west = ((data >> 4) & 0x1) == 1;
         boolean east = ((data >> 5) & 0x1) == 1;
 
         ArrayList<ReusableRenderFace> faceList = new ArrayList<ReusableRenderFace>();
 
         AxisAlignedBB aabb = this.getBoundsInBlock(info);
 
         ReusableRenderFace[] faces = new ReusableRenderFace[6];
 
         for (int i = 0; i < faces.length; i++) {
             faces[i] = RenderFaceHelper.getAFace();
             faces[i].setValues(aabb, i);
             faces[i].icon = info.getMaterial().getIcon(i, data);
             faces[i].renderPass = info.getMaterial().getRenderPass();
             faceList.add(faces[i]);
         }
 
         boolean split = false;
         if (bottom && !north && !south && !west && !east) {
             faces[2].startV = 0.0D;
             faces[3].startV = 0.0D;
             faces[4].startV = 0.0D;
             faces[5].startV = 0.0D;
             faces[0].offset = 1.0D;
         }
         else {
             split = true;
         }
         if (top && !north && !south && !west && !east) {
             faces[2].endV = 1.0D;
             faces[3].endV = 1.0D;
             faces[4].endV = 1.0D;
             faces[5].endV = 1.0D;
             faces[1].offset = 0.0D;
         }
         else {
             split = true;
         }
         if (split) {
             if (bottom) {
                 for (int i = 2; i <= 5; i++) {
                     ReusableRenderFace face = RenderFaceHelper.getAFace();
                     face.setValues(i, (i % 2) == 0 ? 0.25D : 0.75D, 0.25D, 0.75D, 0.0D, 0.75D);
                     face.icon = info.getMaterial().getIcon(i, data);
                     face.renderPass = info.getMaterial().getRenderPass();
                     faceList.add(face);
                 }
                 faces[0].offset = 0.0D;
             }
             if (top) {
                 for (int i = 2; i <= 5; i++) {
                     ReusableRenderFace face = RenderFaceHelper.getAFace();
                     face.setValues(i, (i % 2) == 0 ? 0.25D : 0.75D, 0.25D, 0.75D, 0.25D, 1.0D);
                     face.icon = info.getMaterial().getIcon(i, data);
                     face.renderPass = info.getMaterial().getRenderPass();
                     faceList.add(face);
                 }
                 faces[1].offset = 1.0D;
             }
         }
 
         split = false;
         if (north && !top && !bottom && !west && !east) {
             faces[0].startV = 0.0D;
             faces[1].startV = 0.0D;
             faces[4].startU = 0.0D;
             faces[5].startU = 0.0D;
             faces[2].offset = 0.0D;
         }
         else {
             split = true;
         }
         if (south && !top && !bottom && !west && !east) {
             faces[0].endV = 1.0D;
             faces[1].endV = 1.0D;
             faces[4].endU = 1.0D;
             faces[5].endU = 1.0D;
             faces[3].offset = 1.0D;
         }
         else {
             split = true;
         }
         if (split) {
             if (north) {
                 for (int i = 0; i <= 1; i++) {
                     ReusableRenderFace face = RenderFaceHelper.getAFace();
                     face.setValues(i, (i % 2) == 0 ? 0.25D : 0.75D, 0.25D, 0.75D, 0.0D, 0.25D);
                     face.icon = info.getMaterial().getIcon(i, data);
                     face.renderPass = info.getMaterial().getRenderPass();
                     faceList.add(face);
                 }
                 for (int i = 4; i <= 5; i++) {
                     ReusableRenderFace face = RenderFaceHelper.getAFace();
                     face.setValues(i, (i % 2) == 0 ? 0.25D : 0.75D, 0.0D, 0.25D, 0.25D, 0.75D);
                     face.icon = info.getMaterial().getIcon(i, data);
                     face.renderPass = info.getMaterial().getRenderPass();
                     faceList.add(face);
                 }
                 faces[2].offset = 0.0D;
             }
             if (south) {
                 for (int i = 0; i <= 1; i++) {
                     ReusableRenderFace face = RenderFaceHelper.getAFace();
                     face.setValues(i, (i % 2) == 0 ? 0.25D : 0.75D, 0.25D, 0.75D, 0.75D, 1.0D);
                     face.icon = info.getMaterial().getIcon(i, data);
                     face.renderPass = info.getMaterial().getRenderPass();
                     faceList.add(face);
                 }
                 for (int i = 4; i <= 5; i++) {
                     ReusableRenderFace face = RenderFaceHelper.getAFace();
                     face.setValues(i, (i % 2) == 0 ? 0.25D : 0.75D, 0.75D, 1.0D, 0.25D, 0.75D);
                     face.icon = info.getMaterial().getIcon(i, data);
                     face.renderPass = info.getMaterial().getRenderPass();
                     faceList.add(face);
                 }
                 faces[3].offset = 1.0D;
             }
         }
 
         split = false;
         if (west && !top && !bottom && !north && !south) {
             faces[0].startU = 0.0D;
             faces[1].startU = 0.0D;
             faces[2].startU = 0.0D;
             faces[3].startU = 0.0D;
             faces[4].offset = 0.0D;
         }
         else {
             split = true;
         }
         if (east && !top && !bottom && !north && !south) {
             faces[0].endU = 1.0D;
             faces[1].endU = 1.0D;
             faces[2].endU = 1.0D;
             faces[3].endU = 1.0D;
             faces[5].offset = 1.0D;
         }
         else {
             split = true;
         }
         if (split) {
             if (west) {
                 for (int i = 0; i <= 3; i++) {
                     ReusableRenderFace face = RenderFaceHelper.getAFace();
                     face.setValues(i, (i % 2) == 0 ? 0.25D : 0.75D, 0.0D, 0.25D, 0.25D, 0.75D);
                     face.icon = info.getMaterial().getIcon(i, data);
                     face.renderPass = info.getMaterial().getRenderPass();
                     faceList.add(face);
                 }
                 faces[4].offset = 0.0D;
             }
             if (east) {
                 for (int i = 0; i <= 3; i++) {
                     ReusableRenderFace face = RenderFaceHelper.getAFace();
                     face.setValues(i, (i % 2) == 0 ? 0.25D : 0.75D, 0.75D, 1.0D, 0.25D, 0.75D);
                     face.icon = info.getMaterial().getIcon(i, data);
                     face.renderPass = info.getMaterial().getRenderPass();
                     faceList.add(face);
                 }
                 faces[5].offset = 1.0D;
             }
         }
         split = false;
 
         return faceList.toArray(new ReusableRenderFace[0]);
     }
 
     @Override
     public void onBlockUpdate(MicroBlockInfo info, World world, int x, int y, int z) {
         position++;
         int data = info.getData();
         int origData = data;
         int power = 0;
         int origPower = origData >> 6;
 
         data = 0;
 
         power = world.getStrongestIndirectPower(x, y, z);
         if (power == 15) {
             power = power << 4;
         }
 
         for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
             ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
 
             if (canConnectTo(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, this)) {
                 data |= (1 << i);
             }
         }
 
         for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
             ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
 
             Block block = Block.blocksList[world.getBlockId(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)];
 
            if (block == null) {
                continue;
            }

             if (block.blockID == MicroBlockAPI.microBlockId) {
                 IMicroBlock tile = (IMicroBlock) world.getBlockTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
 
                 if (tile == null) {
                     continue;
                 }
 
                 List<MicroBlockInfo> infos = tile.getSubBlocks();
 
                 for (MicroBlockInfo currInfo : infos) {
                     if (currInfo.getType().equals(this)) {
                         int wirePower = currInfo.getData() >> 6;
                         if (wirePower > 1 && wirePower - 1 > origPower) {
                             if (power < wirePower - 1) {
                                 power = wirePower - 1;
                             }
                         }
                         break;
                     }
                 }
             }
         }
 
         if (origPower > power) {
             power = 0;
         }
 
         data |= (power << 6);
 
         if (data != origData && position == 1) {
             info.setData(data);
             world.notifyBlocksOfNeighborChange(x, y, z, MicroBlockAPI.microBlockId);
             ((IMicroBlock) world.getBlockTileEntity(x, y, z)).resendTileData();
         }
 
         position--;
     }
 
     @Override
     public void drawHitbox(DrawBlockHighlightEvent event, MicroBlockInfo info) {}
 
     @Override
     public int onItemUse(EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
         return 0;
     }
 
     public static boolean canConnectTo(World world, int x, int y, int z, MicroBlockCentralWire wireType) {
         Block block = Block.blocksList[world.getBlockId(x, y, z)];
 
         if (block == null) {
             return false;
         }
 
         if (block.blockID == MicroBlockAPI.microBlockId) {
             IMicroBlock tile = (IMicroBlock) world.getBlockTileEntity(x, y, z);
 
             if (tile == null) {
                 return false;
             }
 
             List<MicroBlockInfo> infos = tile.getSubBlocks();
 
             for (MicroBlockInfo info : infos) {
                 if (info.getType().equals(wireType)) {
                     return true;
                 }
             }
         }
         else if (block.canProvidePower()) {
             return true;
         }
 
         return false;
     }
 
     @Override
     public int getPowerOutput(MicroBlockInfo info, int side) {
         return info.getData() >> 6;
     }
 
 }
