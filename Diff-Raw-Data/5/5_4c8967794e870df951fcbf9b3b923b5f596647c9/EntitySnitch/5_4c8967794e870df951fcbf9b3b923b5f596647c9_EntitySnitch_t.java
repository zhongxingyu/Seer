 package assets.quidcraft.entities;
 
 import java.util.List;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityFlying;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.passive.IAnimals;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 import assets.quidcraft.Quidcraft;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class EntitySnitch extends EntityFlying implements IAnimals{
 	
 	private static double speedFactor = 1.0D;
 	public EntitySnitch(World world)
     {
         super(world);
         setSize(0.1F, 0.1F);
     }
 	
 	public EntitySnitch(World world,EntityPlayer entityplayer)
     {
         this(world);
         setLocationAndAngles(entityplayer.posX, entityplayer.posY
 				+ (double) entityplayer.getEyeHeight(), entityplayer.posZ,
 				entityplayer.rotationYaw, entityplayer.rotationPitch);
 		posX -= MathHelper.cos((rotationYaw / 180F) * 3.141593F) * 0.16F;
 		posY -= 0.10000000149011612D;
 		posZ -= MathHelper.sin((rotationYaw / 180F) * 3.141593F) * 0.16F;
 		setPosition(posX, posY, posZ);
     }
 	
 	public EntitySnitch(World world, double i, double j, double k)
     {
         this(world);
         double x = i;
         double y = j;
         double z = k;
         setPosition(i,j,k);
         prevPosX = posX;
         prevPosY = posY;
         prevPosZ = posZ;
     }
 	@Override
 	protected boolean isAIEnabled()
     {
         return true;
     }
 	@Override
 	protected void func_110147_ax()
     {
         super.func_110147_ax();
         this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(speedFactor);
     }
 	@Override
 	public void onEntityUpdate()
     {
 		if(ridingEntity != null && ridingEntity.isDead)
         {
             ridingEntity = null;
         }
         ticksExisted++;
         prevDistanceWalkedModified = distanceWalkedModified;
         prevPosX = posX;
         prevPosY = posY;
         prevPosZ = posZ;
         prevRotationPitch = rotationPitch;
         prevRotationYaw = rotationYaw;
         if(isSprinting())
         {
             int i = MathHelper.floor_double(posX);
             int j = MathHelper.floor_double(posY - 0.20000000298023224D - (double)yOffset);
             int k = MathHelper.floor_double(posZ);
             int j1 = worldObj.getBlockId(i, j, k);
             if(j1 > 0)
             {
                 worldObj.spawnParticle((new StringBuilder()).append("tilecrack_").append(j1).toString(), posX + ((double)rand.nextFloat() - 0.5D) * (double)width, boundingBox.minY + 0.10000000000000001D, posZ + ((double)rand.nextFloat() - 0.5D) * (double)width, -motionX * 4D, 1.5D, -motionZ * 4D);
             }
         }
         if(handleWaterMovement())
         {
             if(!inWater)
             {
                 float f = MathHelper.sqrt_double(motionX * motionX * 0.20000000298023224D + motionY * motionY + motionZ * motionZ * 0.20000000298023224D) * 0.2F;
                 if(f > 1.0F)
                 {
                     f = 1.0F;
                 }
                 worldObj.playSoundAtEntity(this, "random.splash", f, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                 float f1 = MathHelper.floor_double(boundingBox.minY);
                 for(int l = 0; (float)l < 1.0F + width * 20F; l++)
                 {
                     float f2 = (rand.nextFloat() * 2.0F - 1.0F) * width;
                     float f4 = (rand.nextFloat() * 2.0F - 1.0F) * width;
                     worldObj.spawnParticle("bubble", posX + (double)f2, f1 + 1.0F, posZ + (double)f4, motionX, motionY - (double)(rand.nextFloat() * 0.2F), motionZ);
                 }
 
                 for(int i1 = 0; (float)i1 < 1.0F + width * 20F; i1++)
                 {
                     float f3 = (rand.nextFloat() * 2.0F - 1.0F) * width;
                     float f5 = (rand.nextFloat() * 2.0F - 1.0F) * width;
                     worldObj.spawnParticle("splash", posX + (double)f3, f1 + 1.0F, posZ + (double)f5, motionX, motionY, motionZ);
                 }
 
             }
             fallDistance = 0.0F;
             inWater = true;
             //fire = 0;
         } else
         {
             inWater = false;
         }
         if(handleLavaMovement())
         {
             setOnFireFromLava();
         }
         if(posY < -64D)
         {
             kill();
         }
         if(!worldObj.isRemote)
         {
             setFlag(2, ridingEntity != null);
         }
         
         updateEntityActionState();
     }
 	
 	protected void updateEntityActionState(){
		if(worldObj.isRemote)
			return;
 		if(ticksToChangeDirection <= 0){
 			randomX = (rand.nextDouble()*10.0) - 5.0;
 			randomY = (rand.nextDouble()*10.0) - 5.0;
 			randomZ = (rand.nextDouble()*10.0) - 5.0;
 			ticksToChangeDirection = rand.nextInt(50);
 		}
 		else
 			ticksToChangeDirection--;
 		
 		// find closest seeker
 		// fly away, if too far, fly randomly
		target = this.worldObj.getClosestPlayerToEntity(this, 16F);
 		waypointX = posX;
 		waypointY = posY;
 		waypointZ = posZ;
 		
 		// evade
 		if (target != null) {
 			waypointX = posX - 50 * (target.posX - posX + randomX);
 			waypointY = posY - 50 * (target.posY - posY + randomY);
 			waypointZ = posZ - 50 * (target.posZ - posZ + randomZ);
 		}
 
 		double d = waypointX - posX;
 		double d1 = waypointY - posY;
 		double d2 = waypointZ - posZ;
 		double d3 = MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
 
 		motionX = (d / d3) * speedFactor;
 		motionY = (d1 / d3) * speedFactor;
 		motionZ = (d2 / d3) * speedFactor;
 
 		if (target == null) {
 			motionX = 0;
 			motionY = 0;
 			motionZ = 0;
 		}
 			
 		double dx = waypointX - posX;
         double dy = waypointY - posY;
         double dz = waypointZ - posZ;
         double dh = (double)MathHelper.sqrt_double(dx*dx + dy*dy);
 		float yaw = (float)((Math.atan2(dz, dx)*(-180)) / 3.1415927410125732D);// - 90F;
 		
 		float pitch = (float)((Math.atan2(dy, dh)*(180)) / 3.1415927410125732D);
 		setRotation(yaw, pitch);
 		
 		
 		//update wings
 		if(wingsUp && wingFlap < 0F){
 			wingFlap+=0.4F;
 			if(wingFlap>=0F)
 				wingsUp=false;
 		}
 		else if(!wingsUp && wingFlap > -1.2F){
 			wingFlap-=0.4F;
 			if(wingFlap<=-1.2F)
 				wingsUp=true;
 		}
 		
 	}
 	@Override
 	public boolean interact(EntityPlayer entityplayer)
     {
 		ItemStack itemstack = entityplayer.inventory.getCurrentItem();
 		if(this.getDistanceToEntity(entityplayer) <= 2.5F && itemstack != null && itemstack.itemID == Quidcraft.SnitchGlove.itemID)
         {
         	if(entityplayer.inventory.addItemStackToInventory(new ItemStack(Quidcraft.Snitch, 1))){
         		entityplayer.onItemPickup(this, 1);
         		setDead();
         		return true;
         	}
         	else
         		return false;
         }
         else
             return false;
         
     }
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean isInRangeToRenderVec3D(Vec3 vec3d)
     {
         return true;
     }
 	@Override
 	@SideOnly(Side.CLIENT)
     public boolean isInRangeToRenderDist(double d)
     {
         return true;
     }
     @Override
 	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
     	super.writeEntityToNBT(nbttagcompound);
 		nbttagcompound.setShort("wingFlap", (short) wingFlap);
 		nbttagcompound.setBoolean("wingsUp", wingsUp);
 	}
     @Override
 	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
     	super.readEntityFromNBT(nbttagcompound);
 		wingFlap = nbttagcompound.getShort("wingFlap");
 		wingsUp = nbttagcompound.getBoolean("wingsUp");
 	}
     @Override
 	public boolean canBreatheUnderwater()
     {
         return true;
     }
 	
 	private int ticksIdle = 0;
 	private int ticksToChangeDirection = 0;
 	private Entity target;
 	private double waypointX = posX;
 	private double waypointY = posY;
 	private double waypointZ = posZ;
 	private double randomX;
 	private double randomY;
 	private double randomZ;
 	public float wingFlap=0F;
 	private boolean wingsUp=false;
 }
