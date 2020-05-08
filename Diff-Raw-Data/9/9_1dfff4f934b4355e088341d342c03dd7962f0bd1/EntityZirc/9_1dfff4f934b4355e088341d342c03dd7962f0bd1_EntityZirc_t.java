 package epic.zirc;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.monster.EntityBlaze;
 import net.minecraft.entity.projectile.EntityThrowable;
 import net.minecraft.entity.projectile.EntityWitherSkull;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.registry.IThrowableEntity;
 
 public class EntityZirc extends EntityThrowable
 {
 	private boolean dropped;
 	public EntityZirc(World par1World)
 	{
 		super(par1World);
 	}
 
 	public EntityZirc(World par1World, EntityLivingBase par2EntityLivingBase)
 	{
 		super(par1World, par2EntityLivingBase);
 		//this is called
 		dropped=false;
 	}
 
 	public EntityZirc(World par1World, double par2, double par4, double par6)
 	{
 		super(par1World, par2, par4, par6);
 	}
 
 	/**
 	 * Called when this EntityThrowable hits a block or entity.
 	 */
 	protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
 	{
 		if (par1MovingObjectPosition.entityHit != null)
 		{
 			byte b0 = 3; //damage
 			//damage set death message  LATER!
 			par1MovingObjectPosition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)b0);
 			//par1MovingObjectPosition.entityHit.setFire(15);
 			dropped=true;
 
 		}
 		else
 		{
 			//par1MovingObjectPosition.hitVec.normalize();
 			//this.motionY*=-1;
 			//System.out.println(par1MovingObjectPosition.sideHit);
			if (dropped==false) {
				System.out.println(dropped);
				dropped=true;
                float f = this.rand.nextFloat() * 0.8F + 0.1F;
                float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
                float f2 = this.rand.nextFloat() * 0.8F + 0.1F;
 				EntityItem entityitem = new EntityItem(this.worldObj,
 						(double)this.posX,
 						(double)this.posY,
 						(double)this.posZ,
 						new ItemStack(epic.zirc.Zirc.zirconiumGem));
 				this.worldObj.spawnEntityInWorld(entityitem);
 			}
 			this.setDead();
 		}
 
 		if (par1MovingObjectPosition.entityHit != null&&!this.worldObj.isRemote)
 		{
 			this.setDead();
 		}
 
 		if (!this.worldObj.isRemote) //when it touches the ground??? change so it can be picked up
 		{
 			this.setDead();
 		}
 	}
 
 
 }
