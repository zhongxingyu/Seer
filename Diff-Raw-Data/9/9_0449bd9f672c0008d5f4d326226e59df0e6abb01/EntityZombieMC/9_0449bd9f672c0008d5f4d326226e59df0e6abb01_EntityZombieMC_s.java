 package net.minecraft.src;
 
 public class EntityZombieMC extends EntityZombie
 {
 	public EntityZombieMC(World par1World)
 	{
 		super(par1World);
 		this.tasks.clearTasks();
 		this.targetTasks.clearTasks();
 		this.tasks.addTask(0, new EntityAISwimming(this));
         //this.tasks.addTask(1, new EntityAIBreakDoor(this));
         //this.tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, this.moveSpeed, false));
         //this.tasks.addTask(3, new EntityAIAttackOnCollide(this, EntityVillager.class, this.moveSpeed, true));
         //this.tasks.addTask(4, new EntityAIMoveTwardsRestriction(this, this.moveSpeed));
         //this.tasks.addTask(5, new EntityAIMoveThroughVillage(this, this.moveSpeed, false));
 		this.tasks.addTask(1, new EntityAITempt(this, 0.25F, mod_MLZ.defenseBeacon.shiftedIndex, false));
 		this.tasks.addTask(1, new EntityAITempt(this, 0.25F, mod_MLZ.miningBeacon.shiftedIndex, false));
         this.tasks.addTask(6, new EntityAIWander(this, this.moveSpeed));
         this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
         this.tasks.addTask(7, new EntityAILookIdle(this));
        this.texture = "/mczombie.png";
         
         //tough
         this.attackStrength = 10;
 	}
 	
 	public void onLivingUpdate()
     {
 		super.onLivingUpdate();
 		super.extinguish();
     }
 }
