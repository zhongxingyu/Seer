 package xdki113r.redstonekit.common;
 
import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.Explosion;
 import net.minecraft.world.World;
  
 public class EntityRedGrenade extends EntityItem
 {
 	private EntityPlayer thrower;
 	
     protected double bounceFactor;
     protected double bounceSlowFactor;
     protected int fuse;
     protected boolean exploded;
     protected double initialVelocity;
 	
     public ItemStack item;
     
    private int health;
    
 	public EntityRedGrenade(World par1World)
     {
         super(par1World);
         bounceFactor = 0.15D;
         bounceSlowFactor = 0.8D;
         initialVelocity = 1.0D;
         setSize(0.25F, 0.25F);
         exploded = false;
         fuse = 50;
        health = 5;
         yOffset = 0.0F;
         item = new ItemStack(RedstoneKit.redstoneGrenade, 1, 0);
     }
 
 	@Override
 	public ItemStack getEntityItem()
 	{
 		return item;
 	}
 	
     public EntityRedGrenade(World par1World, double par2, double par4, double par6)
     {
         super(par1World, par2, par4, par6);
         setPosition(par2, par4, par6);
     }
     
     public EntityRedGrenade(World par1World, EntityPlayer par2Player)
     {
         this(par1World);
         this.thrower = par2Player;
         setAngles(par2Player.rotationYaw, 0.0F);
         double d = -MathHelper.sin((par2Player.rotationYaw * (float)Math.PI) / 180F);
         double d1 = MathHelper.cos((par2Player.rotationYaw * (float)Math.PI) / 180F);
         motionX = initialVelocity * d * (double)MathHelper.cos((par2Player.rotationPitch / 180F) * (float)Math.PI);
         motionY = -initialVelocity * (double)MathHelper.sin((par2Player.rotationPitch / 180F) * (float)Math.PI);
         motionZ = initialVelocity * d1 * (double)MathHelper.cos((par2Player.rotationPitch / 180F) * (float)Math.PI);
 
         if (par2Player.ridingEntity != null && (par2Player.ridingEntity instanceof EntityLiving))
         {
             par2Player = (EntityPlayer)par2Player.ridingEntity;
         }
 
         motionX += par2Player.motionX;
         motionY += par2Player.onGround ? 0.0D : par2Player.motionY;
         motionZ += par2Player.motionZ;
         setPosition(par2Player.posX + d * 0.8D, par2Player.posY + (double)par2Player.getEyeHeight(), par2Player.posZ + d1 * 0.8D);
         prevPosX = posX;
         prevPosY = posY;
         prevPosZ = posZ;
     }
     
     @Override
     public boolean isInRangeToRenderDist(double d)
     {
         return true;
     }
     
     protected boolean canTriggerWalking()
     {
         return false;
     }
     
     /**
      * Called to update the entity's position/logic.
      */
     public void onUpdate()
     {
         double d = motionX;
         double d1 = motionY;
         double d2 = motionZ;
         prevPosX = posX;
         prevPosY = posY;
         prevPosZ = posZ;
         moveEntity(motionX, motionY, motionZ);
         boolean flag = false;
 
         if (motionX == 0.0D && d != 0.0D)
         {
             motionX = -bounceFactor * d;
             motionY = bounceSlowFactor * d1;
             motionZ = bounceSlowFactor * d2;
 
             if (Math.abs(d) > 0.1D)
             {
                 flag = true;
             }
         }
 
         if (motionY == 0.0D && d1 != 0.0D)
         {
             motionX = bounceSlowFactor * d;
             motionY = -bounceFactor * d1;
             motionZ = bounceSlowFactor * d2;
 
             if (Math.abs(d1) > 0.1D)
             {
                 flag = true;
             }
         }
 
         if (motionZ == 0.0D && d2 != 0.0D)
         {
             motionX = bounceSlowFactor * d;
             motionY = bounceSlowFactor * d1;
             motionZ = -bounceFactor * d2;
 
             if (Math.abs(d2) > 0.1D)
             {
                 flag = true;
             }
         }
 
         if (flag)
         {
         }
 
         motionY -= 0.04D;
         motionX *= 0.99D;
         motionY *= 0.99D;
         motionZ *= 0.99D;
         handleExplode();
     }
     
     protected void handleExplode()
     {
         if (fuse-- <= 0)
         {
             explode();
         }
     }
     
     protected void explode()
     {
         if (!exploded)
         {
             exploded = true;
             Explosion explosion = new Explosion(worldObj, null, posX, (float)posY, (float)posZ, 3F);
             explosion.doExplosionA();
             worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 4F, (1.0F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
             if(RedstoneKit.grenadeExplode)
             {
                 explosion.doExplosionB(true);
             }
             for (int i = 0; i < 32; i++)
             {
                 worldObj.spawnParticle("explode", posX, posY, posZ, worldObj.rand.nextDouble() - 0.5D, worldObj.rand.nextDouble() - 0.5D, worldObj.rand.nextDouble() - 0.5D);
                 worldObj.spawnParticle("smoke", posX, posY, posZ, worldObj.rand.nextDouble() - 0.5D, worldObj.rand.nextDouble() - 0.5D, worldObj.rand.nextDouble() - 0.5D);
             }
             isDead = true;
         }
     }
 
     public boolean canBeCollidedWith()
     {
         return true;
     }
 
     public boolean attackEntityFrom(DamageSource damagesource, float f)
     {
        if (this.isEntityInvulnerable())
        {
            return false;
        }
        else if (this.getEntityItem() != null && this.getEntityItem().itemID == Item.netherStar.itemID && damagesource.isExplosion())
        {
            return false;
        }
        else
        {
            this.setBeenAttacked();
            this.health = (int)((float)this.health - f);

            if (this.health <= 0)
            {
                this.setDead();
            }

            return false;
        }
     }
     
     public void yoloExplosion()
     {
     }
     
     public void writeEntityToNBT(NBTTagCompound nbttagcompound)
     {
         super.writeEntityToNBT(nbttagcompound);
         nbttagcompound.setByte("Fuse", (byte)fuse);
     }
 
     
     public void readEntityFromNBT(NBTTagCompound nbttagcompound)
     {
         super.readEntityFromNBT(nbttagcompound);
         fuse = nbttagcompound.getByte("Fuse");
     }
     
     public void onCollideWithPlayer(EntityPlayer entityplayer) {}
 
     public float getEyeHeight()
     {
         return height;
     }
 }
