 package silentAbyss.entity.projectile;
 
 import silentAbyss.core.util.ModDamageSources;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.projectile.EntityLargeFireball;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 
 public class EntityMeteor extends EntityLargeFireball {
 	
 	public EntityMeteor(World world) {
 		
 		super(world);
 	}
 
 	public EntityMeteor(World world, double x, double y, double z) {
 		
 		super(world, x, y, z, 0, -1, 0);
 	}
 
 	@Override
 	protected void onImpact(MovingObjectPosition mop) {
 
 		if (!this.worldObj.isRemote) {
 			if (mop.entityHit != null) {
 				mop.entityHit.attackEntityFrom(ModDamageSources.meteor, 5.0f);
 			}
 			
 			this.worldObj.newExplosion((Entity)null, this.posX, this.posY, this.posZ, 1.0f, false, false);
 			this.setDead();
 		}
 	}
 	
 	@Override
 	public void readEntityFromNBT(NBTTagCompound tags) {
 		
 		super.readEntityFromNBT(tags);
 		this.setDead();
 	}
 
 }
