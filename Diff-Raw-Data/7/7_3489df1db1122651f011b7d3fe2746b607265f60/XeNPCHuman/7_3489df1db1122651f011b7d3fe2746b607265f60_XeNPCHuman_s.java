 package me.supermaxman.xenpc.objects;
 
 import me.supermaxman.xenpc.main.TickTask;
 import me.supermaxman.xenpc.main.XeNPC;
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.Packet;
 import net.minecraft.server.Packet18ArmAnimation;
 import net.minecraft.server.PathEntity;
 import net.minecraft.server.Vec3D;
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Bukkit;
 import org.bukkit.EntityEffect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.BlockFace;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.craftbukkit.entity.CraftLivingEntity;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.util.Vector;
 
 public class XeNPCHuman {
 	private final String name;
 	private final int UID;
 	private final XeNPCBase entity;
 	private int health;
 	private LivingEntity target;
 	private boolean isGrounded;
 	private boolean hasAttacked = false;
 	private boolean pvp = true;
 	private boolean isFalling = false;
 	private int attackDelay = 20;
     private PathEntity path;
     private String owner;
 	public XeNPCHuman(XeNPCBase entity, int UID, String name, String owner) {
 		this.name = ChatColor.stripColor(name);
 	    this.UID = UID;
 	    this.entity = entity;
 	    this.entity.setNPC(this);
 	    this.owner = owner;
 	}
 	
 	
 	
 	
 	
 	public void setPVP(Boolean bool){
 		this.pvp = bool;
 	}
 	
 	public boolean getPVP(){
 		return this.pvp;
 	}
 	
 	public void setHealth(int health){
 		this.health = health;
 	}
 	
 	public void damage(int d) {
 		this.health = this.health - d;
 		this.getPlayer().playEffect(EntityEffect.HURT);
 		if(this.health<=0){
 			this.die();
 		}
 	}
 	
 	public void die(){
 		this.getPlayer().playEffect(EntityEffect.DEATH);
 		WorldServer ws = ((CraftWorld) this.getWorld()).getHandle();
 		this.dropInventory();
         ws.removeEntity(this.entity);
 		Manager.npcs.remove(UID);
 	}
 	
 	
 	
 	public XeNPCBase getHandle() {
 	    return this.entity;
 	}
 	
     public void dropInventory(){
     	for(ItemStack i: this.getInventory().getContents()){
         	if(i!=null){
         	this.getWorld().dropItem(this.getLocation(), i);
         	this.getInventory().remove(i);
         	}
     	}
     	
     }   
 	
     public String getOwner() {
 		return this.owner;
 	}
     
 	public PlayerInventory getInventory() {
 		return this.getPlayer().getInventory();
 	}
 
     public ItemStack getItemInHand() {
         return this.getPlayer().getItemInHand();
     }
 
     public Location getLocation() {
         return this.getPlayer().getLocation();
     }
     
     public Player getPlayer() {
         return (Player) this.entity.getBukkitEntity();
     }
     
     public World getWorld() {
         return this.getPlayer().getWorld();
     }
     
 
     public void setItemInHand(ItemStack item) {
         this.getPlayer().setItemInHand(item);
     }
     
     public void teleport(double x, double y, double z, float yaw, float pitch) {
         this.entity.setLocation(x, y, z, yaw, pitch);
     }
     
     public void teleport(Location loc) {
         boolean multiworld = loc.getWorld() != this.getWorld();
         this.getPlayer().teleport(loc);
         if (multiworld) {
             ((CraftServer) Bukkit.getServer()).getHandle().players
                     .remove(this.entity);
         }
     }
     
 	
 	public String getName() {
 		return this.name;
 	}
 
 	public int getUID() {
 		return this.UID;
 	}
 	
 	public LivingEntity getTarget() {
 		return this.target;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null || getClass() != obj.getClass()) {
 			return false;
 		}
 		XeNPCHuman other = (XeNPCHuman) obj;
 		return UID == other.UID;
 	}
 	
     
 	public void doTick(){
 		if(this.target!=null){
 			if(this.target.isDead()){
 				this.target=null;
 			}
 		}
 		followOwner();
 		if(this.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getType()!=Material.AIR&&isFalling==true){
 			this.teleport(this.getLocation().getX(),this.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getLocation().add(0, 1, 0).getY(),this.getLocation().getZ(), this.getLocation().getYaw(), this.getLocation().getPitch());
 			isFalling = false;
 		}
 		if(this.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getType()==Material.AIR){
 			this.teleport(this.getLocation().add(0, -0.5, 0));
 			isFalling = true;
 		}
 		if(this.getPlayer().getItemInHand().getType()==Material.BOW){
 			for(Entity e : this.getPlayer().getNearbyEntities(15, 10, 15)){
 				if(e instanceof Monster){
 					this.target = (LivingEntity) e;
 		            TickTask.faceEntity(this, target);
 		    			if(attackDelay == 20){
 		    				attackDelay = 0;
 		    				this.attackEntity(((CraftEntity)target).getHandle());
 		    			}else{
 		    				attackDelay++;
 		    			}
 		    			break;
 		    		}
 				}
 		}else if(this.getPlayer().getItemInHand().getType()!=Material.AIR){
 			for(Entity e : this.getPlayer().getNearbyEntities(5, 5, 5)){
 				if(e instanceof Monster){
 					this.target = (LivingEntity) e;
 		            TickTask.faceEntity(this, target);
 		    			if(attackDelay == 20){
 		    				attackDelay = 0;
 		    				this.attackEntity(((CraftEntity)target).getHandle());
 		    			}else{
 		    				attackDelay++;
 		    			}
 		    			break;
 		    		}
 				}
 		}
 
 		
 	}
 	
     private void attackEntity(net.minecraft.server.Entity entity) {
     	if(this.getItemInHand().getType()!=Material.AIR&&this.target!=null){
         if (this.getItemInHand().getType()==Material.BOW) {
         	ItemStack i = this.getItemInHand();
             TickTask.faceEntity(this, target);
             if(!i.containsEnchantment(Enchantment.ARROW_INFINITE)){
             	if(loseItem(Material.ARROW)){
             		damageItem(i, 1);
             		this.entity.makeInaccuracies();;
             		this.getPlayer().launchProjectile(Arrow.class);
             	}
             }else{
         			damageItem(i, 1);
         			this.entity.makeInaccuracies();;
         			this.getPlayer().launchProjectile(Arrow.class);
             }
         } else {        	
         	Location location = this.getLocation();
         	final World world = location.getWorld();
         	for (Player ply : Bukkit.getServer().getOnlinePlayers()) {
         		if (ply == null || ply.equals(this.getPlayer()) || world != ply.getWorld()) {
         			continue;
         		}
         		if (location.distanceSquared(ply.getLocation()) > 100) {
         			continue;
         		}
         		Packet packet = new Packet18ArmAnimation(this.entity, 1);
         		((CraftPlayer) ply).getHandle().netServerHandler.sendPacket((packet));
         	}
             LivingEntity other = target;
         	ItemStack i = this.getItemInHand();
 			damageItem(i, 1);
             other.damage(this.entity.inventory.a(entity));
         }
         hasAttacked = true;
     	}
     }
     
     private synchronized void followOwner(){
     	Player p = XeNPC.plugin.getServer().getPlayerExact(this.owner);
    	
     	if(p!=null){
             Location ploc = p.getLocation();
             Location nloc = this.getLocation();
             Vector pv = ploc.toVector();
             Vector nv = nloc.toVector();
             double x = this.entity.x;
             double y = this.entity.y;
             double z = this.entity.z;
     		if(pv.getX()>nv.getX()){
     			x = x+0.1;
     		}else if(pv.getX()<nv.getX()){
     			x = x-0.1;
     		}
     		if(pv.getZ()>nv.getZ()){
     			z = z+0.1;
     		}else if(pv.getZ()<nv.getZ()){
     			z = z-0.1;
     		}
     		if(pv.getY()>nv.getY()){
     			y = y+1.1;
     		}
     		this.entity.move(x, y, z);
    		
     	}
     }
     
     
     
     private void damageItem(ItemStack i, int amt){
     	i.setDurability((short)(i.getDurability()+amt));
     }
 	private boolean loseItem(Material mat){
 		ItemStack[] items = this.getInventory().getContents();
 		if(this.getInventory().contains(mat)){
 			
 		for(ItemStack it : this.getInventory().getContents()){
 			if (it==null){
 			}else if ((it.getType()==mat)&&(it.getAmount()>1)){
 				it.setAmount(it.getAmount()-1);
 			}else if((it.getType()==mat)&&(it.getAmount()==1)){
 				this.getInventory().removeItem(it);
 			}
 		}
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 }
