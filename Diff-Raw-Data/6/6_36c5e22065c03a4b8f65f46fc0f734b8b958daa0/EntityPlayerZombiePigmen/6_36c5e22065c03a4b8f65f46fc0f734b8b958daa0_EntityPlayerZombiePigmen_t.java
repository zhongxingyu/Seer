 package com.nekokittygames.modjam.UnDeath;
 
 import java.util.List;
 import java.util.UUID;
 
 import cpw.mods.fml.relauncher.ReflectionHelper;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.attributes.AttributeInstance;
 import net.minecraft.entity.ai.attributes.AttributeModifier;
 import net.minecraft.entity.monster.EntityPigZombie;
 import net.minecraft.entity.monster.EntityZombie;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.util.StringUtils;
 import net.minecraft.world.World;
 
 public class EntityPlayerZombiePigmen extends EntityPlayerZombie {
 	public static int EntityID;
 	private static final UUID field_110189_bq = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
     private static final AttributeModifier field_110190_br = (new AttributeModifier(field_110189_bq, "Attacking speed boost", 0.45D, 0)).func_111168_a(false);
     private static final ResourceLocation Pigoverlay=new ResourceLocation("undeath","textures/entity/playerPigZombie.png");
     /** Above zero if this PigZombie is Angry. */
     private int angerLevel;
 
     /** A random delay until this PigZombie next makes a sound. */
     private int randomSoundDelay;
     private Entity field_110191_bu;
 	public EntityPlayerZombiePigmen(World par1World) {
 		super(par1World);
 		this.isImmuneToFire = true;
 	}
 	private String LayeredName;
     @SideOnly(Side.CLIENT)
 	public String getLayeredName() {
 		if(LayeredName==null)
 			BuildLayeredName();
     	return LayeredName;
 	}
	@Override
	public void InitFromPlayer(EntityPlayer par7EntityPlayer) {
		// TODO Auto-generated method stub
		super.InitFromPlayer(par7EntityPlayer);
		//this.setZombieName("nekosune");
	}
 	@SideOnly(Side.CLIENT)
 	public void setLayeredName(String layeredName) {
 		LayeredName = layeredName;
 	}
 	@SideOnly(Side.CLIENT)
 	public void BuildLayeredName()
 	{
 		LayeredName="skins/" + StringUtils.stripControlCodes(getZombieName())+"/pigzombie";
 	}
 	@SideOnly(Side.CLIENT)
 	public ResourceLocation[] getSkins()
 	{
 		return new ResourceLocation[] {this.func_110306_p(),Pigoverlay};
 	}
 	@SideOnly(Side.CLIENT)
 	private static String getSkinName(String par0Str) {
 		return "pzskins/" + StringUtils.stripControlCodes(par0Str);
 	}
 	 @SideOnly(Side.CLIENT)
     public static ResourceLocation func_110299_g(String par0Str)
     {
         return new ResourceLocation("pzcloaks/" + StringUtils.stripControlCodes(par0Str));
     }
 	 @SideOnly(Side.CLIENT)
     public static ResourceLocation func_110305_h(String par0Str)
     {
         return new ResourceLocation("pzskull/" + StringUtils.stripControlCodes(par0Str));
     }
 	
 	 protected void func_110147_ax()
 	    {
 	        super.func_110147_ax();
 	        this.func_110148_a(field_110186_bp).func_111128_a(0.0D);
 	        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.5D);
 	        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(5.0D);
 	    }
 	 
 	 protected boolean isAIEnabled()
 	    {
 	        return false;
 	    }
 	 
 	 public void onUpdate()
 	    {
 	        if (this.field_110191_bu != this.entityToAttack && !this.worldObj.isRemote)
 	        {
 	            AttributeInstance attributeinstance = this.func_110148_a(SharedMonsterAttributes.field_111263_d);
 	            attributeinstance.func_111124_b(field_110190_br);
 
 	            if (this.entityToAttack != null)
 	            {
 	                attributeinstance.func_111121_a(field_110190_br);
 	            }
 	        }
 
 	        this.field_110191_bu = this.entityToAttack;
 
 	        if (this.randomSoundDelay > 0 && --this.randomSoundDelay == 0)
 	        {
 	            this.playSound("mob.zombiepig.zpigangry", this.getSoundVolume() * 2.0F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 1.8F);
 	        }
 
 	        super.onUpdate();
 	    }
 	 
 	 
 	 public boolean getCanSpawnHere()
 	    {
 	        return this.worldObj.difficultySetting > 0 && this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty() && !this.worldObj.isAnyLiquid(this.boundingBox);
 	    }
 	 
 	 public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
 	    {
 	        super.writeEntityToNBT(par1NBTTagCompound);
 	        par1NBTTagCompound.setShort("Anger", (short)this.angerLevel);
 	    }
 	 
 	 public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
 	    {
 	        super.readEntityFromNBT(par1NBTTagCompound);
 	        this.angerLevel = par1NBTTagCompound.getShort("Anger");
 	    }
 	 
 	 protected Entity findPlayerToAttack()
 	    {
 	        return this.angerLevel == 0 ? null : super.findPlayerToAttack();
 	    }
 	 
 	 public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
 	    {
 	        if (this.isEntityInvulnerable())
 	        {
 	            return false;
 	        }
 	        else
 	        {
 	            Entity entity = par1DamageSource.getEntity();
 
 	            if (entity instanceof EntityPlayer)
 	            {
 	                List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(32.0D, 32.0D, 32.0D));
 
 	                for (int i = 0; i < list.size(); ++i)
 	                {
 	                    Entity entity1 = (Entity)list.get(i);
 
 	                    if (entity1 instanceof EntityPlayerZombiePigmen)
 	                    {
 	                        EntityPlayerZombiePigmen entitypigzombie = (EntityPlayerZombiePigmen)entity1;
 	                        entitypigzombie.becomeAngryAt(entity);
 	                    }
 	                    if (entity1 instanceof EntityPigZombie)
 	                    {
 	                    	EntityPigZombie entitypigzombie = (EntityPigZombie)entity1;
 	                        entitypigzombie.attackEntityFrom(DamageSource.causePlayerDamage(attackingPlayer), 0);
 	                    }
 	                }
 
 	                this.becomeAngryAt(entity);
 	            }
 
 	            return super.attackEntityFrom(par1DamageSource, par2);
 	        }
 	    }
 
 	    /**
 	     * Causes this PigZombie to become angry at the supplied Entity (which will be a player).
 	     */
 	    private void becomeAngryAt(Entity par1Entity)
 	    {
 	        this.entityToAttack = par1Entity;
 	        this.angerLevel = 400 + this.rand.nextInt(400);
 	        this.randomSoundDelay = this.rand.nextInt(40);
 	    }
 
 	    /**
 	     * Returns the sound this mob makes while it's alive.
 	     */
 	    protected String getLivingSound()
 	    {
 	        return "mob.zombiepig.zpig";
 	    }
 
 	    /**
 	     * Returns the sound this mob makes when it is hurt.
 	     */
 	    protected String getHurtSound()
 	    {
 	        return "mob.zombiepig.zpighurt";
 	    }
 
 	    /**
 	     * Returns the sound this mob makes on death.
 	     */
 	    protected String getDeathSound()
 	    {
 	        return "mob.zombiepig.zpigdeath";
 	    }
 
 	    /**
 	     * Drop 0-2 items of this living's type. @param par1 - Whether this entity has recently been hit by a player. @param
 	     * par2 - Level of Looting used to kill this mob.
 	     */
 	    protected void dropFewItems(boolean par1, int par2)
 	    {
 	        int j = this.rand.nextInt(2 + par2);
 	        int k;
 
 	        for (k = 0; k < j; ++k)
 	        {
 	            this.dropItem(Item.rottenFlesh.itemID, 1);
 	        }
 
 	        j = this.rand.nextInt(2 + par2);
 
 	        for (k = 0; k < j; ++k)
 	        {
 	            this.dropItem(Item.goldNugget.itemID, 1);
 	        }
 	    }
 
 	    /**
 	     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
 	     */
 	    public boolean interact(EntityPlayer par1EntityPlayer)
 	    {
 	        return false;
 	    }
 
 	    protected void dropRareDrop(int par1)
 	    {
 	        this.dropItem(Item.ingotGold.itemID, 1);
 	    }
 
 	    /**
 	     * Returns the item ID for the item the mob drops on death.
 	     */
 	    protected int getDropItemId()
 	    {
 	        return Item.rottenFlesh.itemID;
 	    }
 
 }
