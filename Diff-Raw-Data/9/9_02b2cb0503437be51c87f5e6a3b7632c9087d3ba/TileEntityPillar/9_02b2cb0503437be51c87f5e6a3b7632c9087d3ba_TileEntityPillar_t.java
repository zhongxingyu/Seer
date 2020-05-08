 package assets.fyresmodjam;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class TileEntityPillar extends TileEntity {
 
 	public static String[] validBlessings = {"Miner", "Lumberjack", "Warrior", "Hunter", "Swamp", "Thief", "Ninja", "Mechanic", "Alchemist", "Scout", "Guardian"/*, "Healer"*/, "Vampire", "Inferno", "Diver", "Berserker", "Loner"};
	public static String[] blessingDescriptions = {"+25% breaking speed on stone and iron blocks, and +20% damage with pickaxes", "+25% breaking speed on wooden blocks, and +15% damage with axes", "+20% melee damage", "+20% projectile damage", "Attacks will slow enemies", "Enemies have a chance to drop gold nuggets", "While sneaking you are invisble and attacks on enemies with full health do double damage", "@@\u00A7ePASSIVE - \u00A7oYou disarm traps 3x as often and have 2x the chance to salvage disarmed traps.@@\u00A7eACTIVE - \u00A7oOnce per day, you may disarm and salvage target trap for free.", "All potions act like wildcard potions", "You can see traps without sneaking, but take 25% more damage from traps", "Take 20% less damage from all sources"/*, "2x healing rate"*/, "Heal 7% of damage dealt to enemies and, in direct sunlight, you take 20% more damage and deal 20% less damage", "You don't take fire damage, do +35% damage while on fire, and take damage when wet", "You can breathe underwater", "@@\u00A7ePASSIVE - \u00A7oKills are added to berserk counter. (10 max)@@\u00A7eACTIVE - \u00A7oTurn on/off berserk mode. While berserk mode is active, you do 30% more damage, and berserk counter ticks down every 2 seconds", "The lower your health, the higher your damage, to a maximum of +35%"};
 	
 	public String blessing = null;
 	
     public TileEntityPillar() {}
 
     public void updateEntity() {
     	super.updateEntity();
     	
     	if(worldObj.isRemote) {
     		/*EntityPlayer player = ;
     		
     		if(player != null && player.getEntityData().hasKey("Blessing") && player.getEntityData().getString("Blessing").equals(blessing)) {
     			for(int i = 0; i < 2; i++) {this.worldObj.spawnParticle("portal", this.xCoord + ModjamMod.r.nextDouble(), this.yCoord + ModjamMod.r.nextDouble() * 2, this.zCoord + ModjamMod.r.nextDouble(), (ModjamMod.r.nextDouble() - 0.5D) * 2.0D, -ModjamMod.r.nextDouble(), (ModjamMod.r.nextDouble() - 0.5D) * 2.0D);}
     		}*/
     		
     		spawnParticles();
     		
     		if(ModjamMod.pillarGlow) {this.worldObj.updateAllLightTypes(this.xCoord, this.yCoord, this.zCoord);}
     	}
     }
     
     @SideOnly(Side.CLIENT)
     public void spawnParticles() {
     	EntityPlayer player = Minecraft.getMinecraft().thePlayer;
 		
 		if(player != null && player.getEntityData().hasKey("Blessing") && player.getEntityData().getString("Blessing").equals(blessing)) {
 			for(int i = 0; i < 2; i++) {this.worldObj.spawnParticle("portal", this.xCoord + ModjamMod.r.nextDouble(), this.yCoord + ModjamMod.r.nextDouble() * 2, this.zCoord + ModjamMod.r.nextDouble(), (ModjamMod.r.nextDouble() - 0.5D) * 2.0D, -ModjamMod.r.nextDouble(), (ModjamMod.r.nextDouble() - 0.5D) * 2.0D);}
 		}
     }
     
     public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
     	super.writeToNBT(par1NBTTagCompound);
     	if(blessing == null) {this.blessing = validBlessings[ModjamMod.r.nextInt(validBlessings.length)];}
     	par1NBTTagCompound.setString("Blessing", blessing);
     }
 
     public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
     	super.readFromNBT(par1NBTTagCompound);
     	blessing = par1NBTTagCompound.hasKey("Blessing") ? par1NBTTagCompound.getString("Blessing") : validBlessings[ModjamMod.r.nextInt(validBlessings.length)];
     }
 
     public Packet getDescriptionPacket() {
         NBTTagCompound tag = new NBTTagCompound();
         this.writeToNBT(tag);
         return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, tag);
     }
 
     public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {this.readFromNBT(packet.data);}
     
     @SideOnly(Side.CLIENT)
     public AxisAlignedBB getRenderBoundingBox() {
     	//fixes rendering bug, but on downside, always renders, even off screen
         return INFINITE_EXTENT_AABB;
     }
 }
