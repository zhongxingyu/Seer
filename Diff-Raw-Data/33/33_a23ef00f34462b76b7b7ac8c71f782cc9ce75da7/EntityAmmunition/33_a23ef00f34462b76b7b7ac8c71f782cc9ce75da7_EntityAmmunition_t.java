 package net.medsouz.omg.entity;
 
 import java.util.List;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.medsouz.omg.model.ModelM16;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockGlass;
 import net.minecraft.block.BlockPane;
 import net.minecraft.client.Minecraft;
 import net.minecraft.enchantment.EnchantmentThorns;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.IProjectile;
 import net.minecraft.entity.monster.EntityEnderman;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.packet.Packet70GameEvent;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 
 public class EntityAmmunition extends Entity implements IProjectile{
 
 	private EntityLivingBase shootingEntity;
 	private int ticksInAir = 0;
 	private double damage;
 
 	public EntityAmmunition(World par1World){
 		super(par1World);
 		this.renderDistanceWeight = 10.0D;
         this.setSize(0.5F, 0.5F);
 	}
 	
 	public EntityAmmunition(World par1World, EntityLivingBase par2EntityLivingBase, float par3) {
 	        super(par1World);
 	        this.renderDistanceWeight = 10.0D;
 	        this.shootingEntity = par2EntityLivingBase;
 
 	        this.setSize(0.5F, 0.5F);
 	        this.setLocationAndAngles(par2EntityLivingBase.posX, par2EntityLivingBase.posY + (double)par2EntityLivingBase.getEyeHeight(), par2EntityLivingBase.posZ, par2EntityLivingBase.rotationYaw, par2EntityLivingBase.rotationPitch);
 	        this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
	        //this.posY -= 0.10000000149011612D;
 	        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
 	        this.setPosition(this.posX, this.posY, this.posZ);
	        this.yOffset = 0.5F;
 	        this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI)) * par3;
 	        this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI)) * par3;
 	        this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI)) * par3;
 	        //this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, par3 * 1.5F, 1.0F);
 	    }
 
 	    /**
 	     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
 	     */
 	    public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8)
 	    {
 	        
 	    }
 
 	    @SideOnly(Side.CLIENT)
 
 	    /**
 	     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
 	     * posY, posZ, yaw, pitch
 	     */
 	    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9)
 	    {
 	        this.setPosition(par1, par3, par5);
 	        this.setRotation(par7, par8);
 	    }
 
 	    @SideOnly(Side.CLIENT)
 
 	    /**
 	     * Sets the velocity to the args. Args: x, y, z
 	     */
 	    public void setVelocity(double par1, double par3, double par5)
 	    {
 	        this.motionX = par1;
 	        this.motionY = par3;
 	        this.motionZ = par5;
 
 	        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
 	        {
 	            float f = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
 	            this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(par1, par5) * 180.0D / Math.PI);
 	            this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(par3, (double)f) * 180.0D / Math.PI);
 	            this.prevRotationPitch = this.rotationPitch;
 	            this.prevRotationYaw = this.rotationYaw;
 	            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
 	        }
 	    }
 	
 	public void onUpdate(){
 		super.onUpdate();
 		
         if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
         {
             float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
             this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
             this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f) * 180.0D / Math.PI);
         }
 
             ++this.ticksInAir;
             Vec3 vec3 = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX, this.posY, this.posZ);
             Vec3 vec31 = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
             MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks_do_do(vec3, vec31, false, true);
             vec3 = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX, this.posY, this.posZ);
             vec31 = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
 
             if (movingobjectposition != null)
             {
                 vec31 = this.worldObj.getWorldVec3Pool().getVecFromPool(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
             }
 
             Entity entity = null;
             List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
             double d0 = 0.0D;
             int l;
             float f1;
 
             for (l = 0; l < list.size(); ++l)
             {
                 Entity entity1 = (Entity)list.get(l);
 
                 if (entity1.canBeCollidedWith() && (entity1 != this.shootingEntity || this.ticksInAir >= 5))
                 {
                     f1 = 0.3F;
                     AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand((double)f1, (double)f1, (double)f1);
                     MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec3, vec31);
 
                     if (movingobjectposition1 != null)
                     {
                         double d1 = vec3.distanceTo(movingobjectposition1.hitVec);
 
                         if (d1 < d0 || d0 == 0.0D)
                         {
                             entity = entity1;
                             d0 = d1;
                         }
                     }
                 }
             }
 
             if (entity != null)
             {
                 movingobjectposition = new MovingObjectPosition(entity);
             }
 
             if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer)
             {
                 EntityPlayer entityplayer = (EntityPlayer)movingobjectposition.entityHit;
 
                 if (entityplayer.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).canAttackPlayer(entityplayer))
                 {
                     movingobjectposition = null;
                 }
             }
 
             float f2;
             float f3;
 
             if (movingobjectposition != null)
             {
                 if (movingobjectposition.entityHit != null)
                 {
                 	DamageSource damagesource = null;
                     if (this.shootingEntity == null)
                     {
                         damagesource = DamageSource.causeThrownDamage(this, this);
                     }
                     else
                     {
                         damagesource = DamageSource.causeThrownDamage(this, this.shootingEntity);
                     }
                     movingobjectposition.entityHit.attackEntityFrom(damagesource, 3.5f);
                     worldObj.removeEntity(this);
                 }
                 else
                 {
                     Block type = Block.blocksList[worldObj.getBlockId(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ)];
                     for(int x = 0; x < 3; x++){//spawn 3 particles
                     	worldObj.spawnParticle("tilecrack_"+type.blockID+"_"+this.worldObj.getBlockMetadata(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ), this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ);
                     }
                     if(type instanceof BlockGlass || type instanceof BlockPane){
                     	if(Math.random() < 0.2D){//20% chance
                     		worldObj.destroyBlock(movingobjectposition.blockX, movingobjectposition.blockY, movingobjectposition.blockZ, false);
                     	}
                     }
                     worldObj.playSoundAtEntity(this, type.stepSound.getBreakSound(), 1, 1);
                     worldObj.removeEntity(this);
                 }
             }
 
             this.posX += this.motionX;
             this.posY += this.motionY;
             this.posZ += this.motionZ;
             f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
             this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
 
             for (this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f2) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
             {
                 ;
             }
 
             while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
             {
                 this.prevRotationPitch += 360.0F;
             }
 
             while (this.rotationYaw - this.prevRotationYaw < -180.0F)
             {
                 this.prevRotationYaw -= 360.0F;
             }
 
             while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
             {
                 this.prevRotationYaw += 360.0F;
             }
 
             this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
             this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
             float f4 = 0.99F;
            f1 = 0.01F;
 
             if (this.isInWater())
             {
                 for (int j1 = 0; j1 < 4; ++j1)
                 {
                     f3 = 0.25F;
                     this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double)f3, this.posY - this.motionY * (double)f3, this.posZ - this.motionZ * (double)f3, this.motionX, this.motionY, this.motionZ);
                 }
 
                 f4 = 0.8F;
             }
 
             this.motionX *= (double)f4;
             this.motionY *= (double)f4;
             this.motionZ *= (double)f4;
             this.motionY -= (double)f1;
             this.setPosition(this.posX, this.posY, this.posZ);
             this.doBlockCollisions();
         }
 
 	@Override
 	protected void entityInit() {
 		
 	}
 
 	@Override
 	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
 		
 	}
 
 	@Override
 	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
 		
 	}
 
 }
