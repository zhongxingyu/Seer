 package d4rk.mc;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import d4rk.mc.event.listener.InventoryHelper;
 import d4rk.mc.inventory.Click;
 import d4rk.mc.inventory.OperationList;
 import d4rk.mc.inventory.SelectBestToolForBlock;
 import d4rk.mc.util.Vec2D;
 import d4rk.mc.util.Vec3D;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.Block;
 import net.minecraft.src.Chunk;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityArrow;
 import net.minecraft.src.EntityChicken;
 import net.minecraft.src.EntityCow;
 import net.minecraft.src.EntityCreeper;
 import net.minecraft.src.EntityFireball;
 import net.minecraft.src.EntityGhast;
 import net.minecraft.src.EntityGolem;
 import net.minecraft.src.EntityItem;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityMob;
 import net.minecraft.src.EntityPig;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntitySheep;
 import net.minecraft.src.EntitySlime;
 import net.minecraft.src.EntitySquid;
 import net.minecraft.src.EntityVillager;
 import net.minecraft.src.EntityXPOrb;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.NetClientHandler;
 import net.minecraft.src.NetHandler;
 import net.minecraft.src.Packet;
 import net.minecraft.src.PathPoint;
 import net.minecraft.src.PlayerControllerMP;
 import net.minecraft.src.Session;
 import net.minecraft.src.Vec3;
 import net.minecraft.src.World;
 
 public class PlayerWrapper {
 	public Minecraft mc = null;
 	public PlayerControllerMP ctrl = null;
 	public EntityPlayer player = null;
 	
 	private MoveHelper moveHelper = null;
 
 	private boolean isMining = true;
 	private int side = 0;
 	private BlockWrapper currentBlock = null;
 	private BlockWrapper lastBlock = null;
 	
 	public PlayerWrapper(EntityPlayer player, PlayerControllerMP ctrl) {
 		this.player = player;
 		this.ctrl = ctrl;
 		this.mc = Minecraft.getMinecraft();
 		this.moveHelper = new MoveHelper(player);
 	}
 	
 	public void update(EntityPlayer player, PlayerControllerMP ctrl) {
 		this.player = player;
 		this.ctrl = ctrl;
 		this.mc = Minecraft.getMinecraft();
 		this.moveHelper = new MoveHelper(player);
 	}
 	
 	/**
 	 * @return mc != null && ctrl != null && player != null && player.worldObj == mc.theWorld;
 	 */
 	public boolean isOk() {
 		return mc != null && ctrl != null && player != null && player.worldObj == mc.theWorld;
 	}
 	
 	/**
 	 * Simply adds a Packet to the send queue.
 	 */
 	public void send(Packet p) {
 		Minecraft.getMinecraft().getNetHandler().addToSendQueue(p);
 	}
 	
 	/**
 	 * @return The Chunk in which the player is currently inside.
 	 */
 	public Chunk getChunk() {
 		return player.worldObj.getChunkFromBlockCoords((int)Math.floor(player.posX), (int)Math.floor(player.posZ));
 	}
 	
 	public boolean selectToolForBlock(BlockWrapper block) {
 		(new SelectBestToolForBlock(this, block)).doOperation();
		return block.getMaterial().isToolNotRequired() || ((getCurrentItem() == null)
 				? new ItemStack(Block.dirt)
				: getCurrentItem()).canHarvestBlock(block.getBlock());
 	}
 	
 	/**
 	 * Selects a specified itemID in the hotbar.
 	 * @return true if the item was found and selected.
 	 */
 	public boolean selectItem(int id) {
 		player.inventory.setCurrentItem(id, 0, false, false);
 		if(this.getCurrentItemID() == id)
 			return true;
 		return false;
 	}
 
 	/**
 	 * Selects a specified item in the hotbar.
 	 * @return true if the item was found and selected.
 	 */
 	public boolean selectItem(Item item) {
 		return this.selectItem(item.itemID);
 	}
 	
 	/**
 	 * @return null if current item is empty
 	 */
 	public ItemStack getCurrentItem() {
 		return player.inventory.getCurrentItem();
 	}
 	
 	/**
 	 * @return itemID of the item currently held by the player, 0 if he has
 	 *         nothing in his hand.
 	 */
 	public int getCurrentItemID() {
 		try {
 			return player.inventory.getCurrentItem().itemID;
 		} catch(NullPointerException e) {
 			return 0;
 		}
 	}
 	
 	public boolean hasOpenInventoryGUI() {
 		return player.openContainer != null;
 	}
 	
 	public int getOpenWindowId() {
 		return (hasOpenInventoryGUI()) ? player.openContainer.windowId : 0;
 	}
 	
 	public void swapInInventory(int a, int b) {
 		windowClick(a, 0, false);
 		windowClick(b, 0, false);
 		windowClick(a, 0, false);
 	}
 	
 	/**
 	 * @param inventorySlot
 	 */
 	public void windowClick(int inventorySlot) {
 		windowClick(inventorySlot, 0, false);
 	}
 	
 	/**
 	 * @param inventorySlot
 	 * @param holdingShift
 	 */
 	public void windowClick(int inventorySlot, boolean holdingShift) {
 		windowClick(inventorySlot, 0, holdingShift);
 	}
 	
 	/**
 	 * @param inventorySlot
 	 * @param mouseClick 0 = left, 1 = right
 	 * @param holdingShift
 	 */
 	public void windowClick(int inventorySlot, int mouseClick, boolean holdingShift) {
 //		Hack.log("[PlayerWrapper] Try window click [slot=" + inventorySlot +
 //				"] [mouse=" + mouseClick + "] [shift=" + holdingShift + "]");
 		ctrl.windowClick(getOpenWindowId(), inventorySlot, mouseClick, (holdingShift) ? 1 : 0, player);
 	}
 	
 	/**
 	 * @return {@code new Vec3D(this.player)}
 	 */
 	public Vec3D getPosition() {
 		return new Vec3D(this.player);
 	}
 	
 	/**
 	 * Needed for the '~' character. It is used as current position.<br>
 	 * If all parameters are empty, they are all considered to be '~' characters.
 	 * 
 	 * @throws NumberFormatException
 	 */
 	public Vec3D getPosition(String X, String Y, String Z) {
 		Vec3D vec = new Vec3D(player);
 		if(X.isEmpty() && Y.isEmpty() && Z.isEmpty())
 			return vec;
 		vec.x = X.equals("~") ? vec.x : X.startsWith("~") ? vec.x + Double.valueOf(X.substring(1)) : Double.valueOf(X);
 		vec.y = Y.equals("~") ? vec.y : Y.startsWith("~") ? vec.y + Double.valueOf(Y.substring(1)) : Double.valueOf(Y);
 		vec.z = Z.equals("~") ? vec.z : Z.startsWith("~") ? vec.z + Double.valueOf(Z.substring(1)) : Double.valueOf(Z);
 		return vec;
 	}
 	
 	/**
 	 * @deprecated Use the {@link BlockWrapper} class instead!
 	 */
 	public int getBlockID(Vec3D v) {
 		if(v == null) return 0;
 		return player.worldObj.getBlockId(v.getX(), v.getY(), v.getZ());
 	}
 	
 	/**
 	 * @deprecated Use the ScriptTask {@link d4rk.mc.playerai.script.TaskRightClick rightclick} instead!
 	 */
 	public boolean tryPlaceBlock(Vec3D pos, int side) {
 		Vec3D v = new BlockWrapper(pos, player.worldObj).getSideCoords(side);
 		v.sub(new Vec3D(player));
 		v.setLen(1);
 		ctrl.onPlayerRightClick(player, player.worldObj, player.getCurrentEquippedItem(),
 								pos.getX(), pos.getY(), pos.getZ(), side, v.getVec3());
 		return true;
 	}
 	
 	/**
 	 * @deprecated Use the ScriptTask {@link d4rk.mc.playerai.script.TaskRightClick rightclick} instead!
 	 */
 	public void rightClickBlock(BlockWrapper block) {
 		this.rightClickBlock(block, block.getNearestSide(this.getPosition()));
 	}
 
 	/**
 	 * @deprecated Use the ScriptTask {@link d4rk.mc.playerai.script.TaskRightClick rightclick} instead!
 	 */
 	public void rightClickBlock(BlockWrapper block, int side) {
 		Vec3D v = block.getSideCoords(side);
 		v.sub(new Vec3D(player));
 		v.setLen(1);
 		ctrl.onPlayerRightClick(player, player.worldObj, player.getCurrentEquippedItem(),
 								block.x, block.y, block.z, side, v.getVec3());
 	}
 	
 	public boolean rightClickEntity(Entity target) {
 		if(!this.canReach(target)) return false;
 		if(!this.ctrl.func_78768_b(this.player, target)) {
 			ItemStack item = this.player.inventory.getCurrentItem();
             if (item != null && this.ctrl.sendUseItem(player, player.worldObj, item))
                 Minecraft.getMinecraft().entityRenderer.itemRenderer.resetEquippedProgress2();
             return true;
 		}
 		return false;
 	}
 	
 	public double getDistance(Vec3D v) {
 		return this.getPosition().sub(v).getLen();
 	}
 	
 	public double getDistance(BlockWrapper block) {
 		return getDistance(block, block.getNearestSide(getPosition()));
 	}
 	
 	public double getDistance(BlockWrapper block, int side) {
 		return block.getSideCoords(side).dist(new Vec3D(player));
 	}
 
 	public boolean canSee(Entity e) {
 		return e == null ? false : player.getEntitySenses().canSee(e);
 	}
 	
 	public boolean canMoveTo(Entity target) {
 		return canMoveTo(new Vec3D(target));
 	}
 	
 	public boolean canMoveTo(Vec3D pos) {
 		return player.getNavigator().getPathToXYZ(pos.x, pos.y, pos.z) == null ? false : true;
 	}
 	
 	public boolean canMoveTo(BlockWrapper pos) {
 		return player.getNavigator().getPathToXYZ(pos.x, pos.y, pos.z) == null ? false : true;
 	}
 	
 	public boolean canReach() {
 		return canReach(currentBlock, side);
 	}
 	
 	public boolean canReach(BlockWrapper block) {
 		return canReach(block, block.getNearestSide(this.getPosition()));
 	}
 	
 	public boolean canReach(BlockWrapper pos, int s) {
 		if(pos == null) return false;
 		return ctrl.getBlockReachDistance()-0.5F > ((pos.getSideCoords(s).dist(new Vec3D(player))));
 	}
 	
 	/**
 	 * This only checks the players reach distance (assuming 5 for entity's) and the distance to the entity.
 	 * @return false if Entity is null or the Entity is out of the reach distance, true otherwise.
 	 */
 	public boolean canReach(Entity e) {
 		if(e == null) return false;
 		return (5*5) > player.getDistanceSqToEntity(e); // assuming 5 blocks reach distance for entity's
 	}
 	
 	/**
 	 * Checks if a bot is currently mining or not.
 	 */
 	public boolean isMining() {
 		return isMining;
 	}
 	
 	/**
 	 * Selects a block and a side for the function startMining()
 	 */
 	public void selectBlock(BlockWrapper block, int side) {
 		this.stopMining();
 		this.lastBlock = currentBlock;
 		this.currentBlock = block.clone();
 		this.side = side;
 	}
 	
 	/**
 	 * Selects a block and the nearest side for the function startMining()
 	 */
 	public void selectBlock(BlockWrapper block) {
 		this.selectBlock(block, block.getNearestSide(this.getPosition()));
 	}
 	
 	/**
 	 * Needed for bot mining, because it disables manual mining.
 	 */
 	public void startMining() {
 		if(!isMining && currentBlock != null) {
 			ctrl.clickBlock(currentBlock.x, currentBlock.y, currentBlock.z, side);
 			Hack.isAutoMining = true;
 			isMining = true;
 		}
 	}
 	
 	/**
 	 * Needed to stop bot mining. Enables manual mining again.
 	 */
 	public void stopMining() {
 		if(isMining) {
 			ctrl.onStoppedUsingItem(player);
 			Hack.isAutoMining = false;
 			isMining = false;
 		}
 	}
 	
 	/**
 	 * Updates the bot mining progress. Also calls startMining by itself.
 	 */
 	public void updateMining() {
 		mc.mcProfiler.startSection("mining");
 		if(currentBlock.isMineable()) {
 			try {
 				this.startMining();
 				ctrl.onPlayerDamageBlock(currentBlock.x, currentBlock.y, currentBlock.z, side);
 				mc.effectRenderer.addBlockHitEffects(currentBlock.x, currentBlock.y, currentBlock.z, side);
 				player.swingItem();
 			} catch(Exception e) {
 				//pass
 			}
 		}
 		mc.mcProfiler.endSection();
 	}
 
 	/**
 	 * @deprecated use the methods of the BlockWrapper class instead!
 	 */
 	public Vec3D getNearestBlock(Vec3D a, Vec3D b) {
 		return getNearestBlock(a, b, defaultIgnoreList, false);
 	}
 
 	/**
 	 * @deprecated use the methods of the BlockWrapper class instead!
 	 */
 	public Vec3D getNearestBlock(Vec3D ppos, Vec3D a, Vec3D b) {
 		return getNearestBlock(ppos, a, b, defaultIgnoreList, false);
 	}
 
 	/**
 	 * @deprecated use the methods of the BlockWrapper class instead!
 	 */
 	public Vec3D getNearestBlock(Vec3D a, Vec3D b, int[] array) {
 		return getNearestBlock(a, b, array, false);
 	}
 
 	/**
 	 * @deprecated use the methods of the BlockWrapper class instead!
 	 */
 	public Vec3D getNearestBlock(Vec3D ppos, Vec3D a, Vec3D b, int[] array) {
 		return getNearestBlock(ppos, a, b, array, false);
 	}
 
 	/**
 	 * @deprecated use the methods of the BlockWrapper class instead!
 	 */
 	public Vec3D getNearestBlock(Vec3D a, Vec3D b, int[] array, boolean onlySpecified) {
 		return getNearestBlock(new Vec3D(player), a, b, array, onlySpecified);
 	}
 	
 	/**
 	 * @deprecated use the methods of the BlockWrapper class instead!
 	 */
 	public Vec3D getNearestBlock(Vec3D ppos, Vec3D a, Vec3D b, int[] array, boolean onlySpecified) {
 		Vec3D A = new Vec3D(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
 		Vec3D B = new Vec3D(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
 		double dist = 9999D;
 		Vec3D pos = null;
 		for(int x=(int)A.x;x<=(int)B.x;x++) {
 			for(int y=(int)A.y;y<=(int)B.y;y++) {
 				for(int z=(int)A.z;z<=(int)B.z;z++) {
 					Vec3D tmp = new Vec3D(ppos.x+x, ppos.y+y, ppos.z+z);
 					if(onlySpecified == isOneOf(getBlockID(tmp), array) && tmp.dist(ppos) < dist) {
 						pos = tmp;
 						dist = tmp.dist(ppos);
 					}
 				}
 			}
 		}
 		return pos;
 	}
 	
 	public static boolean isOneOf(int id, int[] array) {
 		for(int check : array) if(check == id) return true; return false;
 	}
 
 	public int blockSideFromString(String str) {
 		try {
 			return Integer.valueOf(str);
 		} catch (Exception e) {}
 		if(str.equalsIgnoreCase("top"))
 			return 1;
 		else if(str.equalsIgnoreCase("bottom"))
 			return 0;
 		else if(str.equalsIgnoreCase("+x"))
 			return POSX;
 		else if(str.equalsIgnoreCase("+z"))
 			return POSZ;
 		else if(str.equalsIgnoreCase("-x"))
 			return NEGX;
 		else if(str.equalsIgnoreCase("-z"))
 			return NEGZ;
 		return 1;
 	}
 	// end block functions
 
 	public boolean hasPath() {
 		return moveHelper.hasPath();
 	}
 	
 	public boolean findPath(Entity e) {
 		return moveHelper.findPath(e);
 	}
 	
 	public boolean findPath(Vec3D pos) {
 		return moveHelper.findPath(pos);
 	}
 	
 	public boolean followPath() {
 		return moveHelper.followPath();
 	}
 	
 	public void addVelocity(Vec3D vec) {
 		moveHelper.addVelocity(vec);
 	}
 	
 	public void addVelocityToEntity(Entity e) {
 		moveHelper.addVelocity(Vec3D.getMiddle(e).sub(new Vec3D(player)));
 	}
 	
 	public int getLookDirXZ() {
 		Vec3 lookVec = player.getLookVec();
 		Vec3D v = new Vec3D(lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);
 		if(Math.abs(v.x)>=Math.abs(v.z))
 			return (v.x>0) ? POSX : NEGX;
 		if (Math.abs(v.z)>Math.abs(v.x))
 			return (v.z>0) ? POSZ : NEGZ;
 		return 1;
 	}
 	
 	public int getLookDir() {
 		Vec3 lookVec = player.getLookVec();
 		Vec3D v = new Vec3D(lookVec.xCoord, lookVec.yCoord, lookVec.zCoord);
 		if(Math.abs(v.x)>Math.abs(v.z) && Math.abs(v.x)>Math.abs(v.y))
 			return (v.x>0) ? POSX : NEGX;
 		if (Math.abs(v.z)>Math.abs(v.x) && Math.abs(v.z)>Math.abs(v.y))
 			return (v.z>0) ? POSZ : NEGZ;
 		if (Math.abs(v.y)>Math.abs(v.x) && Math.abs(v.y)>Math.abs(v.z))
 			return (v.y>0) ? UP : DOWN;
 		return 1;
 	}
 	
 	public Vec3D getLookVec() {
 		return new Vec3D(player.getLookVec());
 	}
 	
 	public void lookAt(Vec3D pos) {
 		this.lookAt(pos, 180);
 	}
 	
 	public void lookAt(Vec3D pos, float maxRotate) { // magic, do not touch
 		if(pos == null || !isOk()) return;
 		Vec2D ePos = new Vec2D(pos.x,pos.z);
 		Vec2D pPos = new Vec2D(player.posX,player.posZ);
 		Vec2D dist = ePos.clone().sub(pPos);
 		
 		player.rotationYaw = player.rotationYaw % 360;
 		float nRotationYaw = player.rotationYaw;
 		float nRotationPitch = player.rotationPitch;
 		
 		// seitlich drehen
 		if(dist.getLen()>0) {
 			if((dist.clone().angle(new Vec2D(-1,0))*57.2957795)>90)
 				nRotationYaw = (float) (dist.clone().angle(new Vec2D(0,1))*-57.2957795);
 			else
 				nRotationYaw = (float) (dist.clone().angle(new Vec2D(0,1))*57.2957795);
 		}
 		
 		// oben / unten drehen
 		Vec2D vert = new Vec2D(dist.getLen(), pos.y-(player.posY));
 		if(vert.y<0)
 			nRotationPitch = (float) (vert.angle(new Vec2D(1,0))*57.2957795);
 		else
 			nRotationPitch = (float) (vert.angle(new Vec2D(1,0))*-57.2957795);
 
 		// diff berechnen und an maxRotate angleichen
 		float diffYaw = nRotationYaw-player.rotationYaw;
 		float diffYaw2 = nRotationYaw-player.rotationYaw-360;
 		float diffPitch = nRotationPitch-player.rotationPitch;
 		if(Math.abs(diffPitch)>maxRotate)
 			diffPitch = (diffPitch / Math.abs(diffPitch)) * maxRotate;
 		if(Math.abs(diffYaw2)<Math.abs(diffYaw))
 			diffYaw = diffYaw2;
 		if(Math.abs(diffYaw)>maxRotate)
 			diffYaw = (diffYaw / Math.abs(diffYaw)) * maxRotate;
 		
 		player.rotationYaw+=diffYaw;
 		player.rotationPitch+=diffPitch;
 	}
 	
 	public void lookAt(BlockWrapper block, float maxRotate) {
 		this.lookAt(block.getSideCoords(block.getNearestSide(this.getPosition())), maxRotate);
 	}
 	
 	public void lookAt(Entity e, float maxRotate) {
 		if(e == null) return;
 		this.lookAt(Vec3D.getMiddle(e), maxRotate);
 	}
 	
 	/**
 	 * @deprecated use lookAt(e, maxRotate) instead!
 	 */
 	public void focusEntity(Entity e, float maxRotate) {
 		this.lookAt(e, maxRotate);
 	}
 	
 	/**
 	 * @return index of the Entity in the list nearest to the player.
 	 */
 	public int getNearest(List<? extends Entity> l) {
 		int index = 0;
 		double dist = 999999;
 		for(int i=0;i<l.size();i++) {
 			double sqDist = this.player.getDistanceSqToEntity(l.get(i));
 			if(dist > sqDist) {
 				dist = sqDist;
 				index = i;
 			}
 		}
 		return index;
 	}
 	
 	public Entity getNearestMob(double rad) {
 		return getNearestEntity(rad, MOB);
 	}
 	
 	public Entity getNearestEntity(double rad) {
 		return getNearestEntity(rad, ALL);
 	}
 	
 	public Entity getNearestEntity(double rad, int flag) {
 		if(!isOk()) return null;
 		List Entities = player.worldObj.loadedEntityList;
 		Entity nearest = null;
 		double nearestSquareDist = rad*rad;
     	for (int k = 0; k < Entities.size(); k++)
 			if(Entities.get(k)!=null) {
 				Entity e = (Entity) Entities.get(k);
 				if(e == player) continue;
 				if(!isDesiredEntity(e, flag)) continue;
 				double squareDist = e.getDistanceSqToEntity(player);
 				if(squareDist<nearestSquareDist) {
 					nearest = e;
 					nearestSquareDist = squareDist;
 				}
 			}
     	return nearest;
 	}
 	
 	public ArrayList<Entity> getEntities(double rad, int flag) {
 		if(!isOk()) return null;
 		List Entities = player.worldObj.loadedEntityList;
 		ArrayList<Entity> list = new ArrayList<Entity>();
 		double dist = rad*rad;
     	for (int k = 0; k < Entities.size(); k++)
 			if(Entities.get(k)!=null) {
 				Entity e = (Entity) Entities.get(k);
 				if(e == player) continue;
 				if(!isDesiredEntity(e, flag)) continue;
 				double squareDist = e.getDistanceSqToEntity(player);
 				if(squareDist<dist)
 					list.add(e);
 			}
     	return list;
 	}
 	
 	public boolean isMob(Entity e) {
 		if((e instanceof EntityMob) || (e instanceof EntitySlime) || (e instanceof EntityGhast))
 			return (!e.isDead && ((EntityLiving)e).getHealth()>0);
 		return false;
 	}
 	
 	public boolean isAnimal(Entity e) {
 		if((e instanceof EntitySheep)||(e instanceof EntityChicken)||(e instanceof EntityPig)
 		 ||(e instanceof EntityCow)||(e instanceof EntitySquid))
 			return (!e.isDead && ((EntityLiving)e).getHealth()>0);
 		return false;
 	}
 	
 	public boolean isVillager(Entity e) {
 		if(e instanceof EntityVillager)
 			return !e.isDead;
 		return false;
 	}
 	
 	public boolean isItemdrop(Entity e) {
 		if(e instanceof EntityItem)
 			return !e.isDead;
 		return false;
 	}
 	
 	public boolean isExperience(Entity e) {
 		if(e instanceof EntityXPOrb)
 			return !e.isDead;
 		return false;
 	}
 	
 	public boolean isProjectile(Entity e) {
 		if((e instanceof EntityArrow)||(e instanceof EntityFireball))
 			return !e.isDead;
 		return false;
 	}
 	
 	public boolean isGolem(Entity e) {
 		if(e instanceof EntityGolem)
 			return !e.isDead;
 		return false;
 	}
 	
 	public boolean isDesiredEntity(Entity e, int flag) {
 		if(e == null) 											  		return false;
 		if(flag == ALL) 										  		return true;
 		if((flag & MOB) == MOB) if(isMob(e)) 					  		return true;
 		if((flag & ANIMAL) == ANIMAL) if(isAnimal(e))			  		return true;
 		if((flag & VILLAGER) == VILLAGER) if(isVillager(e)) 	  		return true;
 		if((flag & ITEMDROP) == ITEMDROP) if(isItemdrop(e)) 	 	 	return true;
 		if((flag & XPORB) == XPORB) if(isExperience(e)) 		 	 	return true;
 		if((flag & PROJECTILE) == PROJECTILE) if(isProjectile(e)) 		return true;
 		if((flag & GOLEM) == GOLEM) if(isGolem(e)) 	  		      		return true;
 		if((flag & CREEPER) == CREEPER) if(e instanceof EntityCreeper)	return true;
 		return false;
 	}
 	
 	public static int[] defaultIgnoreList = new int[] {
 		0, // Air
 		Block.waterMoving.blockID,
 		Block.waterStill.blockID,
 		Block.lavaMoving.blockID,
 		Block.lavaStill.blockID,
 		Block.torchWood.blockID,
 		Block.bedrock.blockID,
 		Block.chest.blockID
 	};
 
 	public static final int UP   = 0;
 	public static final int DOWN = 1;
 	
 	public static final int POSY = 0;
 	public static final int NEGY = 1;
 	public static final int POSZ = 2;
 	public static final int NEGZ = 3;
 	public static final int POSX = 4;
 	public static final int NEGX = 5;
 
 	public static final int ALL 		= 0xFFFF;
 	public static final int MOB 		= 0x0001;
 	public static final int ANIMAL		= 0x0002;
 	public static final int ITEMDROP 	= 0x0004;
 	public static final int PROJECTILE 	= 0x0008;
 	public static final int VILLAGER 	= 0x0010;
 	public static final int GOLEM 		= 0x0020;
 	public static final int XPORB       = 0x0040;
 	public static final int CREEPER     = 0x0080;
 }
