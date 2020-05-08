 package hunternif.mc.dota2items.entity;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 
 public class EntityWrapper extends Entity {
 	private static final int DATA_ENTITY_ID = 30;
 	/** If entityID is not set within this time frame, kill this wrapper. */
 	private static final int MAX_TIME_WAITING_FOR_ENTITY = 80;
 	
 	protected Entity entity;
 	
 	public EntityWrapper(World world) {
 		super(world);
 	}
 	
 	public EntityWrapper(Entity entity) {
 		this(entity.worldObj);
 		setEntity(entity);
 	}
 	
 	public void setEntity(Entity entity) {
 		this.entity = entity;
 		if (entity != null) {
 			dataWatcher.updateObject(DATA_ENTITY_ID, Integer.valueOf(entity.entityId));
 			updatePosition();
 		}
 	}
 	public Entity getEntity() {
 		return entity;
 	}
 
 	@Override
 	protected void entityInit() {
 		dataWatcher.addObject(DATA_ENTITY_ID, Integer.valueOf(entity == null ? -1 : entity.entityId));
 	}
 
 	@Override
 	protected void readEntityFromNBT(NBTTagCompound tag) {}
 
 	@Override
 	protected void writeEntityToNBT(NBTTagCompound tag) {}
 	
 	@Override
 	public void onEntityUpdate() {
 		super.onEntityUpdate();
 		if (entity != null) {
 			updatePosition();
 		} else if (ticksExisted < MAX_TIME_WAITING_FOR_ENTITY) {
 			// Try getting entity:
 			int entityID = dataWatcher.getWatchableObjectInt(DATA_ENTITY_ID);
 			setEntity(worldObj.getEntityByID(entityID));
 		} else {
 			setDead();
 		}
 	}
 	
 	private void updatePosition() {
 		this.yOffset = entity.yOffset;
 		this.ySize = entity.ySize;
 		this.width = entity.width;
 		this.height = entity.height;
		this.motionX = entity.motionX;
		this.motionY = entity.motionY;
		this.motionZ = entity.motionZ;
 		setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
 		this.boundingBox.setBB(entity.boundingBox);
 	}
 }
