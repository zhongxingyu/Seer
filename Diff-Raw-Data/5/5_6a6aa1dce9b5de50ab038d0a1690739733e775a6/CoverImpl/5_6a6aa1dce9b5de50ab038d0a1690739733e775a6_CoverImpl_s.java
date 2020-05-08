 package immibis.core.covers;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import net.minecraft.server.AxisAlignedBB;
 import net.minecraft.server.Block;
 import net.minecraft.server.EntityHuman;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.MovingObjectPosition;
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.NBTTagList;
 import net.minecraft.server.Packet;
 import net.minecraft.server.Packet250CustomPayload;
 import net.minecraft.server.TileEntity;
 import net.minecraft.server.Vec3D;
 import net.minecraft.server.World;
 
 public class CoverImpl
 {
     LinkedList parts;
     public BlockCoverableBase wrappedBlock;
     public double hollow_edge_size;
     public TileEntity te;
 
     public CoverImpl(TileEntity var1, double var2)
     {
         this.parts = new LinkedList();
         this.te = var1;
         this.hollow_edge_size = var2;
     }
 
     public CoverImpl(TileEntity var1)
     {
         this(var1, 0.25D);
     }
 
     void harvestBlock(World var1, EntityHuman var2, int var3, int var4, int var5, int var6)
     {
         var2.c(0.025F);
         TileEntity var7 = var1.getTileEntity(var3, var4, var5);
 
         if (var7 != null && var7 instanceof ICoverableTile)
         {
             if (var6 == -2)
             {
                 if (this.wrappedBlock != null)
                 {
                     this.wrappedBlock.harvestBlockMultipart(var1, var2, var3, var4, var5, var1.getData(var3, var4, var5));
                     ((TileCoverableBase)var7).convertToMultipartBlockInPlace();
                 }
             }
             else if (var6 >= 0)
             {
                 CoverImpl var8 = ((ICoverableTile)var7).getCoverImpl();
 
                 if (var6 < var8.parts.size())
                 {
                     Part var9 = (Part)var8.parts.get(var6);
 
                     if (var9 != null)
                     {
                         CoverSystemProxy.blockMultipart.a(var1, var3, var4, var5, new ItemStack(CoverSystemProxy.blockMultipart.id, 1, var9.type.id));
                         var8.parts.remove(var6);
 
                         if (var8.parts.size() == 0 && this.wrappedBlock == null)
                         {
                             var1.setRawTypeId(var3, var4, var5, 0);
                         }
 
                         var1.notify(var3, var4, var5);
                     }
                 }
             }
         }
     }
 
     public void writeToNBT(NBTTagCompound var1)
     {
         NBTTagList var2 = new NBTTagList();
         Iterator var3 = this.parts.iterator();
 
         while (var3.hasNext())
         {
             Part var4 = (Part)var3.next();
             var2.add(var4.writeToNBT());
         }
 
         var1.set("Covers", var2);
     }
 
     public void readFromNBT(NBTTagCompound var1)
     {
         this.parts.clear();
         NBTTagList var2 = var1.getList("Covers");
 
         if (var2 != null)
         {
             for (int var3 = 0; var3 < var2.size(); ++var3)
             {
                 this.parts.add(Part.readFromNBT(var2.get(var3)));
             }
         }
     }
 
     public MovingObjectPosition collisionRayTrace(World var1, int var2, int var3, int var4, Vec3D var5, Vec3D var6)
     {
         int var7 = 0;
         var5 = var5.add((double)(-var2), (double)(-var3), (double)(-var4));
         var6 = var6.add((double)(-var2), (double)(-var3), (double)(-var4));
        double var8 = var6.d(var5) + 1.0D;
         Part var10 = null;
         MovingObjectPosition var11 = null;
         int var12 = -1;
         MovingObjectPosition var16;
 
         for (Iterator var13 = this.parts.iterator(); var13.hasNext(); ++var7)
         {
             Part var14 = (Part)var13.next();
             AxisAlignedBB var15 = var14.getBoundingBox();
             var16 = var15.a(var5, var6);
 
             if (var16 != null)
             {
                double var17 = var16.pos.d(var5);
 
                 if (var17 < var8)
                 {
                     var11 = var16;
                     var8 = var17;
                     var10 = var14;
                     var12 = var7;
                 }
             }
         }
 
         if (var10 == null)
         {
             return null;
         }
         else
         {
             AxisAlignedBB var19 = var10.aabb;
             byte var21 = 0;
             Vec3D var20 = var11.pos;
 
             if (var20.a <= var19.a)
             {
                 var21 = 4;
             }
             else if (var20.a >= var19.d)
             {
                 var21 = 5;
             }
             else if (var20.b <= var19.b)
             {
                 var21 = 0;
             }
             else if (var20.b >= var19.e)
             {
                 var21 = 1;
             }
             else if (var20.c <= var19.c)
             {
                 var21 = 2;
             }
             else if (var20.c >= var19.f)
             {
                 var21 = 3;
             }
 
             var16 = new MovingObjectPosition(var2, var3, var4, var21, var20.add((double)var2, (double)var3, (double)var4));
             var16.subHit = var12;
             return var16;
         }
     }
 
     public boolean addPart(Part var1)
     {
         if (!this.canPlace(var1.type, var1.pos))
         {
             return false;
         }
         else
         {
             this.parts.add(var1);
             return true;
         }
     }
 
     public boolean canPlaceCentre(double var1)
     {
         AxisAlignedBB var3 = Part.getBoundingBox(EnumPosition.Centre, var1);
         Iterator var4 = this.parts.iterator();
         Part var5;
 
         do
         {
             if (!var4.hasNext())
             {
                 return true;
             }
 
             var5 = (Part)var4.next();
         }
         while (!var5.getBoundingBox().a(var3));
 
         return false;
     }
 
     public boolean canPlace(PartType var1, EnumPosition var2)
     {
         Iterator var3 = this.parts.iterator();
         Part var4;
 
         do
         {
             if (!var3.hasNext())
             {
                 return true;
             }
 
             var4 = (Part)var3.next();
 
             if (var4.pos == var2)
             {
                 return false;
             }
         }
         while (var4.pos.clazz == var2.clazz || !var4.getBoundingBox().a(Part.getBoundingBox(var2, var1.size)));
 
         return false;
     }
 
     public void getCollidingBoundingBoxes(World var1, int var2, int var3, int var4, AxisAlignedBB var5, ArrayList var6)
     {
         Iterator var7 = this.parts.iterator();
 
         while (var7.hasNext())
         {
             Part var8 = (Part)var7.next();
             var6.add(var8.getBoundingBox().c((double)var2, (double)var3, (double)var4));
         }
     }
 
     public boolean isSideOpen(int var1)
     {
         AxisAlignedBB var2 = AxisAlignedBB.a(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);
 
         switch (var1)
         {
             case 0:
                 var2.b = 0.0D;
                 break;
 
             case 1:
                 var2.e = 1.0D;
                 break;
 
             case 2:
                 var2.c = 0.0D;
                 break;
 
             case 3:
                 var2.f = 1.0D;
                 break;
 
             case 4:
                 var2.a = 0.0D;
                 break;
 
             case 5:
                 var2.d = 1.0D;
         }
 
         Iterator var3 = this.parts.iterator();
         Part var4;
 
         do
         {
             if (!var3.hasNext())
             {
                 return true;
             }
 
             var4 = (Part)var3.next();
         }
         while (var4.type.clazz == EnumPartClass.HollowPanel || var4.pos == EnumPosition.Centre || !var4.getBoundingBox().a(var2));
 
         return false;
     }
 
     public boolean isSideOpen(int var1, int var2, int var3, int var4, int var5, int var6)
     {
         AxisAlignedBB var7 = AxisAlignedBB.a(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);
 
         if (var4 < var1)
         {
             var7.a = 0.0D;
         }
 
         if (var4 > var1)
         {
             var7.d = 1.0D;
         }
 
         if (var5 < var2)
         {
             var7.b = 0.0D;
         }
 
         if (var5 > var2)
         {
             var7.e = 1.0D;
         }
 
         if (var6 < var3)
         {
             var7.c = 0.0D;
         }
 
         if (var6 > var3)
         {
             var7.f = 1.0D;
         }
 
         Iterator var8 = this.parts.iterator();
         Part var9;
 
         do
         {
             if (!var8.hasNext())
             {
                 return true;
             }
 
             var9 = (Part)var8.next();
         }
         while (var9.type.clazz == EnumPartClass.HollowPanel || var9.pos == EnumPosition.Centre || !var9.getBoundingBox().a(var7));
 
         return false;
     }
 
     public void writeDescriptionPacket(DataOutputStream var1) throws IOException
     {
         var1.writeShort(this.wrappedBlock == null ? 0 : this.wrappedBlock.id);
         var1.writeShort(this.parts.size());
         Iterator var2 = this.parts.iterator();
 
         while (var2.hasNext())
         {
             Part var3 = (Part)var2.next();
             var1.writeByte(var3.pos.ordinal());
             var1.writeShort(var3.type.id);
         }
     }
 
     public void readDescriptionPacket(DataInputStream var1) throws IOException
     {
         short var2 = var1.readShort();
         this.wrappedBlock = var2 == 0 ? null : (BlockCoverableBase)Block.byId[var2];
         short var3 = var1.readShort();
         this.parts.clear();
 
         for (int var4 = 0; var4 < var3; ++var4)
         {
             EnumPosition var5 = EnumPosition.values()[var1.readByte()];
             int var6 = var1.readShort() & 65535;
             this.parts.add(new Part((PartType)CoverSystemProxy.parts.get(Integer.valueOf(var6)), var5));
         }
     }
 
     public void copyPartsTo(CoverImpl var1)
     {
         Iterator var2 = this.parts.iterator();
 
         while (var2.hasNext())
         {
             Part var3 = (Part)var2.next();
             var1.addPart(var3);
         }
     }
 
     public Packet getDefaultDescriptionPacket()
     {
         try
         {
             ByteArrayOutputStream var1 = new ByteArrayOutputStream();
             DataOutputStream var2 = new DataOutputStream(var1);
             var2.writeInt(this.te.x);
             var2.writeInt(this.te.y);
             var2.writeInt(this.te.z);
             this.writeDescriptionPacket(var2);
             Packet250CustomPayload var3 = new Packet250CustomPayload();
             var3.tag = "ImmibisCoreCDP";
             var3.data = var1.toByteArray();
             var3.length = var3.data.length;
             return var3;
         }
         catch (IOException var4)
         {
             var4.printStackTrace();
             return null;
         }
     }
 }
