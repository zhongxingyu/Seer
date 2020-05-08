 package jk_5.quakecraft;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.IProjectile;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.*;
 import net.minecraft.world.World;
 
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class EntityBullet extends Entity implements IProjectile {
 
     private int xTile = -1;
     private int yTile = -1;
     private int zTile = -1;
     private int inTile = 0;
     private int inData = 0;
     private boolean inGround = false;
     public EntityPlayer shootingEntity;
     private int ticksInAir = 0;
 
    public EntityBullet(World par1World){
        super(par1World);
    }

     public EntityBullet(World world, EntityPlayer player){
         super(world);
         this.shootingEntity = player;
         float par3 = 0.8F;
         this.setSize(0.1F, 0.1F);
         this.setLocationAndAngles(player.posX, player.posY + player.getEyeHeight(), player.posZ, player.rotationYaw, player.rotationPitch);
         this.posX -= MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
         this.posY -= 0.2D;
         this.posZ -= MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
         this.setPosition(posX, posY, posZ);
         this.yOffset = 0.0F;
         this.motionX = -MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI);
         this.motionZ = MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI);
         this.motionY = -MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI);
         this.setThrowableHeading(motionX, motionY, motionZ, par3 * 1.5F, 1.0F);
     }
 
     @Override
     protected void entityInit(){
         this.dataWatcher.addObject(16, (byte) 0);
     }
 
     @Override
     public void setThrowableHeading(double x, double y, double z, float yaw, float pitch){
         float var9 = MathHelper.sqrt_double(x * x + y * y + z * z);
         x /= var9;
         y /= var9;
         z /= var9;
         x += rand.nextGaussian() * 0.007499999832361937D * pitch;
         y += rand.nextGaussian() * 0.007499999832361937D * pitch;
         z += rand.nextGaussian() * 0.007499999832361937D * pitch;
         x *= yaw;
         y *= yaw;
         z *= yaw;
         motionX = x;
         motionY = y;
         motionZ = z;
         float var10 = MathHelper.sqrt_double(x * x + z * z);
         prevRotationYaw = rotationYaw = (float) (Math.atan2(x, z) * 180.0D / Math.PI);
         prevRotationPitch = rotationPitch = (float) (Math.atan2(y, var10) * 180.0D / Math.PI);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9){
         this.setPosition(par1, par3, par5);
         this.setRotation(par7, par8);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void setVelocity(double par1, double par3, double par5){
         motionX = par1;
         motionY = par3;
         motionZ = par5;
         if(prevRotationPitch == 0.0F && prevRotationYaw == 0.0F){
             float var7 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
             prevRotationYaw = rotationYaw = (float) (Math.atan2(par1, par5) * 180.0D / Math.PI);
             prevRotationPitch = rotationPitch = (float) (Math.atan2(par3, var7) * 180.0D / Math.PI);
             prevRotationPitch = rotationPitch;
             prevRotationYaw = rotationYaw;
             this.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
         }
     }
 
     @Override
     public void onUpdate(){
         super.onUpdate();
         if(ticksInAir > 600){
             this.setDead();
         }
         if(shootingEntity == null){
             List players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(posX - 1, posY - 1, posZ - 1, posX + 1, posY + 1, posZ + 1));
             Iterator i = players.iterator();
             double closestDistance = Double.MAX_VALUE;
             EntityPlayer closestPlayer = null;
             while(i.hasNext()){
                 EntityPlayer e = (EntityPlayer) i.next();
                 double distance = e.getDistanceToEntity(this);
                 if(distance < closestDistance){
                     closestPlayer = e;
                 }
             }
             if(closestPlayer != null){
                 shootingEntity = closestPlayer;
             }
         }
         if(prevRotationPitch == 0.0F && prevRotationYaw == 0.0F){
             float var1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
             prevRotationYaw = rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
             prevRotationPitch = rotationPitch = (float) (Math.atan2(motionY, var1) * 180.0D / Math.PI);
         }
         int var16 = worldObj.getBlockId(xTile, yTile, zTile);
         if(var16 > 0){
             Block.blocksList[var16].setBlockBoundsBasedOnState(worldObj, xTile, yTile, zTile);
             AxisAlignedBB var2 = Block.blocksList[var16].getCollisionBoundingBoxFromPool(worldObj, xTile, yTile, zTile);
             if(var2 != null && var2.isVecInside(worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ))){
                 inGround = true;
             }
         }
         if(inGround){
             int var18 = worldObj.getBlockId(xTile, yTile, zTile);
             int var19 = worldObj.getBlockMetadata(xTile, yTile, zTile);
             if(var18 == inTile && var19 == inData){
                 this.setDead();
             }
         }else{
             ++ticksInAir;
             if(ticksInAir > 1 && ticksInAir < 3){
                 worldObj.spawnParticle("flame", posX + smallGauss(0.1D), posY + smallGauss(0.1D), posZ + smallGauss(0.1D), 0D, 0D, 0D);
                 for(int particles = 0; particles < 3; particles++){
                     this.doFiringParticles();
                 }
             }
             Vec3 var17 = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
             Vec3 var3 = worldObj.getWorldVec3Pool().getVecFromPool(posX + motionX, posY + motionY, posZ + motionZ);
             MovingObjectPosition var4 = worldObj.rayTraceBlocks_do_do(var17, var3, false, true);
             var17 = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
             var3 = worldObj.getWorldVec3Pool().getVecFromPool(posX + motionX, posY + motionY, posZ + motionZ);
             if(var4 != null){
                 var3 = worldObj.getWorldVec3Pool().getVecFromPool(var4.hitVec.xCoord, var4.hitVec.yCoord, var4.hitVec.zCoord);
             }
             Entity var5 = null;
             List var6 = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
             double var7 = 0.0D;
             Iterator var9 = var6.iterator();
             float var11;
             while(var9.hasNext()){
                 Entity var10 = (Entity) var9.next();
                 if(var10.canBeCollidedWith() && (var10 != shootingEntity || ticksInAir >= 5)){
                     var11 = 0.3F;
                     AxisAlignedBB var12 = var10.boundingBox.expand(var11, var11, var11);
                     MovingObjectPosition var13 = var12.calculateIntercept(var17, var3);
                     if(var13 != null){
                         double var14 = var17.distanceTo(var13.hitVec);
                         if(var14 < var7 || var7 == 0.0D){
                             var5 = var10;
                             var7 = var14;
                         }
                     }
                 }
             }
             if(var5 != null){
                 var4 = new MovingObjectPosition(var5);
             }
             if(var4 != null){
                 this.onImpact(var4);
             }
             posX += motionX;
             posY += motionY;
             posZ += motionZ;
             this.setPosition(posX, posY, posZ);
             this.doBlockCollisions();
         }
     }
 
     private void doFiringParticles(){
         worldObj.spawnParticle("mobSpellAmbient", posX + smallGauss(0.1D), posY + smallGauss(0.1D), posZ + smallGauss(0.1D), 0.5D, 0.5D, 0.5D);
         worldObj.spawnParticle("flame", posX, posY, posZ, gaussian(motionX), gaussian(motionY), gaussian(motionZ));
     }
 
     @Override
     public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound){
         par1NBTTagCompound.setShort("xTile", (short) xTile);
         par1NBTTagCompound.setShort("yTile", (short) yTile);
         par1NBTTagCompound.setShort("zTile", (short) zTile);
         par1NBTTagCompound.setByte("inTile", (byte) inTile);
         par1NBTTagCompound.setByte("inData", (byte) inData);
         par1NBTTagCompound.setByte("inGround", (byte) (inGround ? 1 : 0));
     }
 
     @Override
     public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound){
         xTile = par1NBTTagCompound.getShort("xTile");
         yTile = par1NBTTagCompound.getShort("yTile");
         zTile = par1NBTTagCompound.getShort("zTile");
         inTile = par1NBTTagCompound.getByte("inTile") & 255;
         inData = par1NBTTagCompound.getByte("inData") & 255;
         inGround = par1NBTTagCompound.getByte("inGround") == 1;
     }
 
     @Override
     protected boolean canTriggerWalking(){
         return false;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public float getShadowSize(){
         return 0.0F;
     }
 
     @Override
     public boolean canAttackWithItem(){
         return false;
     }
 
     public void setIsCritical(boolean par1){
         byte var2 = dataWatcher.getWatchableObjectByte(16);
         if(par1){
             dataWatcher.updateObject(16, (byte) (var2 | 1));
         }else{
             dataWatcher.updateObject(16, (byte) (var2 & -2));
         }
     }
 
     public boolean getIsCritical(){
         byte var1 = dataWatcher.getWatchableObjectByte(16);
         return (var1 & 1) != 0;
     }
 
     private void onImpact(MovingObjectPosition mop){
         if(mop.typeOfHit == EnumMovingObjectType.ENTITY && mop.entityHit != null){
             if(mop.entityHit == shootingEntity) return;
             this.onImpact(mop.entityHit);
         }else if(mop.typeOfHit == EnumMovingObjectType.TILE){
             this.setDead();
         }
     }
 
     private void onImpact(Entity mop){
         if(mop == shootingEntity && ticksInAir > 3){
             shootingEntity.attackEntityFrom(DamageSource.causePlayerDamage(shootingEntity), 1);
             this.setDead();
         }else{
             if(mop instanceof EntityLivingBase){
                 mop.attackEntityFrom(new DamageSourceRailgun(this.shootingEntity), ((EntityLivingBase) mop).getMaxHealth());
             }else{
                 mop.attackEntityFrom(new DamageSourceRailgun(this.shootingEntity), 1);
                 mop.setDead();
             }
         }
         spawnHitParticles("magicCrit", 8);
         this.setDead();
     }
 
     private void spawnHitParticles(String string, int i){
         for(int particles = 0; particles < i; particles++){
             worldObj.spawnParticle(string, posX, posY - (string.equals("portal") ? 1 : 0), posZ, gaussian(motionX), gaussian(motionY), gaussian(motionZ));
         }
     }
 
     private double smallGauss(double d){
         return (worldObj.rand.nextFloat() - 0.5D) * d;
     }
 
     private double gaussian(double d){
         return d + d * ((rand.nextFloat() - 0.5D) / 4);
     }
 }
