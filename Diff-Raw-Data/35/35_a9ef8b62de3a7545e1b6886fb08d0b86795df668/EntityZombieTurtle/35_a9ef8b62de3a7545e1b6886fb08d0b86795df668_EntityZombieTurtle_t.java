 package me.thegeekyguy101.TurtleMod.entity.monster;
 
 import me.thegeekyguy101.TurtleMod.TurtleMod;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.EntityAIAttackOnCollide;
 import net.minecraft.entity.ai.EntityAIBreakDoor;
 import net.minecraft.entity.ai.EntityAIHurtByTarget;
 import net.minecraft.entity.ai.EntityAILookIdle;
 import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
 import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
 import net.minecraft.entity.ai.EntityAISwimming;
 import net.minecraft.entity.ai.EntityAIWander;
 import net.minecraft.entity.ai.EntityAIWatchClosest;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.passive.EntityVillager;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 
 public class EntityZombieTurtle extends EntityMob {
 
 	public EntityZombieTurtle(World par1World) {
 		super(par1World);
 		this.getNavigator().setBreakDoors(true);
 		this.tasks.addTask(0, new EntityAISwimming(this));
 		this.tasks.addTask(1, new EntityAIBreakDoor(this));
 		this.tasks.addTask(2, new EntityAIAttackOnCollide(this,
				EntityPlayer.class, 0.5D, false));
 		this.tasks.addTask(3, new EntityAIAttackOnCollide(this,
				EntityVillager.class, 0.5D, true));
 		this.tasks.addTask(5,
				new EntityAIMoveThroughVillage(this, 0.5D, false));
		this.tasks.addTask(6, new EntityAIWander(this, 0.5D));
 		this.tasks.addTask(7, new EntityAIWatchClosest(this,
 				EntityPlayer.class, 8.0F));
 		this.tasks.addTask(7, new EntityAILookIdle(this));
 		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
 		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this,
 				EntityPlayer.class, 0, true));
 	}
 	
 	protected boolean isAIEnabled()
     {
         return true;
     }
 	
 	protected void applyEntityAttributes()
     {
 		super.applyEntityAttributes();
         this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(20.0D);
         this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.20000000298023224D);
         this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(3.0D);
     }
 	
 	public int getDropItemId() {
 		return TurtleMod.turtleShell.itemID;
 	}
 	
 	protected void dropFewItems(boolean par1, int par2) {
 		int var3 = this.rand.nextInt(3) + this.rand.nextInt(1 + par2);
 		int var4;
 
 		for (var4 = 0; var4 < var3; ++var4) {
 			this.dropItem(TurtleMod.turtleShell.itemID, 1);
 		}
 
 		var3 = this.rand.nextInt(3) + 1 + this.rand.nextInt(1 + par2);
 
 		for (var4 = 0; var4 < var3; ++var4) {
 			if (this.isBurning()) {
 
 			} else {
 				this.dropItem(TurtleMod.Turtleleather.itemID, 1);
 			}
 		}
 	}
 	
 	/**
      * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
      * use this to react to sunlight and start to burn.
      */
     public void onLivingUpdate()
     {
         if (this.worldObj.isDaytime() && !this.worldObj.isRemote && !this.isChild())
         {
             float f = this.getBrightness(1.0F);
 
             if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)))
             {
                 boolean flag = true;
                 ItemStack itemstack = this.getCurrentItemOrArmor(4);
 
                 if (itemstack != null)
                 {
                     if (itemstack.isItemStackDamageable())
                     {
                         itemstack.setItemDamage(itemstack.getItemDamageForDisplay() + this.rand.nextInt(2));
 
                         if (itemstack.getItemDamageForDisplay() >= itemstack.getMaxDamage())
                         {
                             this.renderBrokenItemStack(itemstack);
                             this.setCurrentItemOrArmor(4, (ItemStack)null);
                         }
                     }
 
                     flag = false;
                 }
 
                 if (flag)
                 {
                     this.setFire(8);
                 }
             }
         }
 
         super.onLivingUpdate();
     }
 	
 	/**
 	 * Returns the sound this mob makes while it's alive.
 	 */
 	protected String getLivingSound() {
 		return "turtlemod:mob.turtle.living";
 	}
 
 	/**
 	 * Returns the sound this mob makes when it is hurt.
 	 */
 	protected String getHurtSound() {
 		return "turtlemod:mob.turtle.hurt";
 	}
 
 	/**
 	 * Returns the sound this mob makes on death.
 	 */
 	protected String getDeathSound() {
 		return "turtlemod:mob.turtle.death";
 	}
 	
 }
