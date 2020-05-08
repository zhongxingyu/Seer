 package me.corriekay.pppopp3.ponyville;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import me.corriekay.pppopp3.Mane;
 import me.corriekay.pppopp3.modules.Equestria;
 import me.corriekay.pppopp3.utils.Utils;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.NBTCompressedStreamTools;
 import net.minecraft.server.NBTTagByte;
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.NBTTagFloat;
 import net.minecraft.server.NBTTagInt;
 import net.minecraft.server.NBTTagList;
 import net.minecraft.server.NBTTagLong;
 import net.minecraft.server.NBTTagString;
 import net.minecraft.server.PlayerInventory;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class Pony {
 	
 	private final File datFile;
 	private final NBTTagCompound c;
 	private HashMap<String,Inventory> remoteChests = new HashMap<String,Inventory>();
 	private HashMap<Inventory,String> rcWorlds = new HashMap<Inventory,String>();
 	private OfflinePlayer op;
 	
 	public Pony(Player pone)throws FileNotFoundException{
 		this(pone.getName());
 	}
 	public Pony(String pone)throws FileNotFoundException{
 		datFile = new File(Mane.getInstance().getDataFolder()+File.separator+"Players",pone);
 		c = NBTCompressedStreamTools.a(new FileInputStream(datFile));
 		loadRemoteChest();
 		op = Bukkit.getOfflinePlayer(pone);
 	}
 	public World getRCWorld(Inventory i){
 		return Bukkit.getWorld(rcWorlds.get(i));
 	}
 	/*getters*/
 	public OfflinePlayer getPlayer(){
 		return op;
 	}
 	public String getName(){
 		return c.getString("name");
 	}
 	public boolean isMuted(){
 		return c.getByte("muted") == 1;
 	}
 	public HashSet<String> getSilenced(){
 		HashSet<String> silenced = new HashSet<String>();
 		NBTTagList list = c.getList("silenced");
 		for(int i = 0; i<list.size();i++){
 			silenced.add(((NBTTagString)list.get(i)).toString());
 		}
 		return silenced;
 	}
 	public boolean isGodMode(){
 		return c.getByte("god") == 1;
 	}
 	public boolean isInvisible(){
 		return c.getByte("invisible") == 1;
 	}
 	public String getNickname(){
 		return c.getString("nickname");
 	}
 	public ArrayList<String> getNickHistory(){
 		ArrayList<String> nicks = new ArrayList<String>();
 		NBTTagList list = c.getList("nickhistory");
 		for(int i = 0; i<list.size(); i++){
 			nicks.add(((NBTTagString)list.get(i)).toString());
 		}
 		return nicks;
 	}
 	public String getChatChannel(){
		return c.getString("chatChannel");
 	}
 	public HashSet<String> getListeningChannels(){
 		HashSet<String> chans = new HashSet<String>();
 		NBTTagList list = c.getList("listenchannels");
 		for(int i = 0; i<list.size();i++){
 			chans.add(((NBTTagString)list.get(i)).toString());
 		}
 		return chans;
 	}
 	public boolean isPonySpy(){
 		return c.getByte("ponyspy") == 1;
 	}
 	public String getGroup(){
 		return c.getString("group");
 	}
 	public ArrayList<String> getPerms(){
 		ArrayList<String> perms = new ArrayList<String>();
 		NBTTagList list = c.getList("perms");
 		for(int i = 0; i<list.size(); i++){
 			perms.add(((NBTTagString)list.get(i)).toString());
 		}
 		return perms;
 	}
 	public ArrayList<String> getIps(){
 		ArrayList<String> ips = new ArrayList<String>();
 		NBTTagList list = c.getList("ipaddress");
 		for(int i = 0; i<list.size(); i++){
 			ips.add(((NBTTagString)list.get(i)).toString());
 		}
 		return ips;
 	}
 	public String getHornLeft(){
 		return getHorn().getString("left");
 	}
 	public String getHornRight(){
 		return getHorn().getString("right");
 	}
 	public boolean getHornOn(){
 		return getHorn().getByte("ison") == 1;
 	}
 	public org.bukkit.inventory.PlayerInventory getInventory(String name){
 		PlayerInventory inv = new PlayerInventory(null);
 		NBTTagList list = c.getCompound("inventories").getList(name);
 		if(list != null){
 			inv.b(list);
 		}
 		return new CraftInventoryPlayer(inv);
 	}
 	public static void getWorldStats(Player p,String worldname){
 		Pony pony = Ponyville.getPony(p);
 		NBTTagCompound compound = pony.c.getCompound("worlds").getCompound(worldname);
 		if(!pony.c.getCompound("worlds").hasKey(worldname)){
 			for(PotionEffect e : p.getActivePotionEffects()){
 				p.removePotionEffect(e.getType());
 			}
 			p.setHealth(20);
 			p.setFoodLevel(20);
 			p.setLevel(0);
 			p.setTotalExperience(0);
 			p.setExhaustion(0);
 			return;
 		}
 		
 		//set potion effects
 		NBTTagList effects = compound.getList("potioneffects");
 		ArrayList<PotionEffect> effectsList = new ArrayList<PotionEffect>();
 		for(int i = 0; i < effects.size(); i++){
 			NBTTagCompound effect = (NBTTagCompound)effects.get(i);
 			byte amp = effect.getByte("Amplifier");
 			byte id = effect.getByte("Id");
 			int time = effect.getInt("Duration");
 			effectsList.add(new PotionEffect(PotionEffectType.getById(id),time,amp));
 		}
 		for(PotionEffect pe : p.getActivePotionEffects()){
 			p.removePotionEffect(pe.getType());
 		}
 		p.addPotionEffects(effectsList);
 		
 		float exhaustion, exp, saturation;
 		int foodlvl, health, level, totalxp;
 		exhaustion = compound.getFloat("exhaustion");
 		exp = compound.getFloat("exp");
 		saturation = compound.getFloat("saturation");
 		foodlvl = compound.getInt("foodlvl");
 		health = compound.getInt("health");
 		level = compound.getInt("level");
 		totalxp = compound.getInt("totalxp");
 		p.setExhaustion(exhaustion);
 		p.setExp(exp);
 		p.setSaturation(saturation);
 		p.setFoodLevel(foodlvl);
 		p.setHealth(health);
 		p.setLevel(level);
 		p.setTotalExperience(totalxp);
 	}
 	public void loadRemoteChest(){
 		//TODO
 		NBTTagCompound rcCompound = c.getCompound("remotechest");
 		String[] worlds = new String[]{"world","badlands"};
 		for(String world : worlds){
 			NBTTagList invlist = rcCompound.getList(world);
 			Inventory inv = Bukkit.createInventory(Bukkit.getPlayerExact(getName()),54);
 			for(int i = 0; i<invlist.size(); i++){
 				NBTTagCompound itemC = (NBTTagCompound)invlist.get(i);
 				int index = itemC.getInt("index");
 				ItemStack is = ItemStack.a(itemC);
 				CraftItemStack cis = new CraftItemStack(is);
 				inv.setItem(index, cis);
 			}
 			rcWorlds.put(inv, world);
 			remoteChests.put(world,inv);
 		}
 	}
 	public Inventory getRemoteChest(World w){
 		return remoteChests.get(w.getName());
 	}
 	public String getEmoteName(){
 		return getEmote().getString("name");
 	}
 	public String getEmoteSender(){
 		return getEmote().getString("sender");
 	}
 	public String getEmoteReceiver(){
 		return getEmote().getString("receiver");
 	}
 	public String getEmoteServer(){
 		return getEmote().getString("server");
 	}
 	public boolean getEmotePrivate(){
 		return getEmote().getByte("private") == 1;
 	}
 	public boolean getEmoteSilent(){
 		return getEmote().getByte("silent") == 1;
 	}
 
 	public String getFirstLogon(){
 		return c.getString("firstlogon");
 	}
 	public String getLastLogon(){
 		return c.getString("lastlogon");
 	}
 	public String getLastLogout(){
 		return c.getString("lastlogout");
 	}
 	public Location getBackWarp(){
 		return getWarp(getOtherWarps().getCompound("back"));
 	}
 	public Location getHomeWarp(){
 		return getWarp(getOtherWarps().getCompound("home"));
 	}
 	public Location getOfflineWarp(){
 		return getWarp(getOtherWarps().getCompound("offline"));
 	}
 	public Location getNamedWarp(String warp){
 		return getWarp(getWarps().getCompound(warp));
 	}
 	public boolean isBanned(){
 		return getBans().getByte("banned") == 1;
 	}
 	/**
 	 * @return
 	 * 0: not banned/unbanned
 	 * 1: tempbanned
 	 * 2: permabanned
 	 */
 	public int getBanType(){
 		return getBans().getInt("bantype");
 	}
 	public String getBanReason(){
 		return getBans().getString("banreason");
 	}
 	public long getUnbanTime(){
 		return getBans().getLong("unbantime");
 	}
 	public ArrayList<String> getNotes(){
 		ArrayList<String> notes = new ArrayList<String>();
 		NBTTagList list = c.getList("notes");
 		for(int i = 0; i<list.size(); i++){
 			notes.add(((NBTTagString)list.get(i)).toString());
 		}
 		return notes;
 	}
 	
 	/*private getters*/
 	private NBTTagCompound getHorn(){
 		return c.getCompound("horn");
 	}
 	private NBTTagCompound getEmote(){
 		return c.getCompound("emote");
 	}
 	private NBTTagCompound getWarps(){
 		return c.getCompound("warps");
 	}
 	private NBTTagCompound getOtherWarps(){
 		return getWarps().getCompound("other");
 	}
 	private NBTTagCompound getBans(){
 		return c.getCompound("ban");
 	}
 	private Location getWarp(NBTTagCompound compound){
 		try {
 			String w;
 			long x,y,z;
 			float p,yaw;
 			w = compound.getString("world");
 			x = compound.getLong("x");
 			y = compound.getLong("y");
 			z = compound.getLong("z");
 			p = c.getFloat("pitch");
 			yaw = c.getFloat("yaw");
 			Location l = new Location(Bukkit.getWorld(w),x,y,z);
 			l.setPitch(p);
 			l.setYaw(yaw);
 			return l;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	private NBTTagCompound getLocationCompound(Location l){
 		String w;
 		long x,y,z;
 		float p,yaw;
 		w = l.getWorld().getName();
 		x = (long) l.getX();
 		y = (long) l.getY();
 		z = (long) l.getZ();
 		p = l.getPitch();
 		yaw = l.getYaw();
 		NBTTagCompound loc = new NBTTagCompound();
 		loc.set("world",new NBTTagString("world",w));
 		loc.set("x", new NBTTagLong("x",x));
 		loc.set("y",new NBTTagLong("y",y));
 		loc.set("z",new NBTTagLong("z",z));
 		loc.set("pitch",new NBTTagFloat("pitch",p));
 		loc.set("yaw",new NBTTagFloat("yaw",yaw));
 		return loc;
 	}
 	
 	/*Setters*/
 	
 	public void setName(String arg){
 		c.set("name", new NBTTagString("name",arg));
 	}
 	public void setMuted(boolean flag){
 		c.set("muted", new NBTTagByte("muted",getBoolByte(flag)));
 	}
 	public void setSilenced(HashSet<String> set){
 		c.set("silenced", getList(set));
 	}
 	public void setGodMode(boolean flag){
 		c.set("god", new NBTTagByte("god",getBoolByte(flag)));
 	}
 	public void setInvisible(boolean flag){
 		c.set("invisible", new NBTTagByte("invisible",getBoolByte(flag)));
 	}
 	public void setNickname(String arg){
 		c.set("nickname",new NBTTagString("nickname",arg));
 	}
 	public void setNickHistory(ArrayList<String> set){
 		c.set("nickhistory", getList(set));
 	}
 	public void setChatChannel(String arg){
 		c.set("chatchannel",new NBTTagString("chatchannel",arg));
 	}
 	public void setListenChannels(HashSet<String> set){
 		c.set("listenchannels",getList(set));
 	}
 	public void setPonySpy(boolean flag){
 		c.set("ponyspy", new NBTTagByte("ponyspy",getBoolByte(flag)));
 	}
 	public void setGroup(String arg){
 		c.set("group", new NBTTagString("group",arg));
 	}
 	public void setPerms(ArrayList<String> set){
 		c.set("perms", getList(set));
 	}
 	public void setIps(ArrayList<String> set){
 		c.set("ipaddress",getList(set));
 	}
 	public void setInventory(org.bukkit.inventory.PlayerInventory inv,String name){
 		c.getCompound("inventories").set(name, ((CraftInventoryPlayer)inv).getInventory().a(new NBTTagList()));
 	}
 	public static void setWorldStats(Player p, String worldName){
 		Pony pony = Ponyville.getPony(p);
 		NBTTagCompound compound = new NBTTagCompound();
 		
 		 //potion effects
 		NBTTagList effectList = new NBTTagList();
 		ArrayList<PotionEffect> effects = (ArrayList<PotionEffect>) p.getActivePotionEffects();
 		for(PotionEffect pe : effects){
 			NBTTagCompound eCompound = new NBTTagCompound();
 			eCompound.setByte("Amplifier",(byte)(pe.getAmplifier()));
 		    eCompound.setByte("Id", (byte)(pe.getType().getId()));
 		    eCompound.setInt("Duration", (int)(pe.getDuration()));
 		    effectList.add(eCompound);
 		}
 		compound.set("potioneffects",effectList);
 		
 		//hunger, health, saturation, etc.
 		float exhaustion, exp, saturation;
 		int foodlvl, health, level, totalxp; 
 		exhaustion = p.getExhaustion();
 		exp = p.getExp();
 		saturation = p.getSaturation();
 		foodlvl = p.getFoodLevel();
 		health = p.getHealth();
 		level = p.getLevel();
 		totalxp = p.getTotalExperience();
 		compound.setFloat("exhaustion",exhaustion);
 		compound.setFloat("exp",exp);
 		compound.setFloat("saturation",saturation);
 		compound.setInt("foodlvl", foodlvl);
 		compound.setInt("health", health);
 		compound.setInt("level", level);
 		compound.setInt("totalxp", totalxp);
 		pony.c.getCompound("worlds").set(worldName, compound);
 		
 	}
 	public void saveRemoteChest(World w){
 		if(w == null){
 			System.out.println("WARNING WORLD IS NULL! player: "+getPlayer().getName());
 		}
 		NBTTagList remotechest = new NBTTagList();
 		Inventory inv = getRemoteChest(w);
 		for(int index = 0; index < inv.getContents().length; index++){
 			CraftItemStack cis = (CraftItemStack) inv.getItem(index);
 			if(cis!=null){
 				ItemStack is = cis.getHandle();
 				NBTTagCompound c = new NBTTagCompound();
 				c = is.save(c);
 				c.set("index", new NBTTagInt("index",index));
 				remotechest.add(c);
 			} 
 		}
 		c.getCompound("remotechest").set(Equestria.get().getParentWorld(w).getName().toLowerCase(),remotechest);
 	}
 	public void setEmoteName(String arg){
 		getEmote().set("name", new NBTTagString("name",arg));
 	}
 	public void setEmoteSender(String arg){
 		getEmote().set("sender", new NBTTagString("name",arg));
 	}
 	public void setEmoteReceiver(String arg){
 		getEmote().set("receiver", new NBTTagString("name",arg));
 	}
 	public void setEmoteServer(String arg){
 		getEmote().set("server", new NBTTagString("name",arg));
 	}
 	public void setEmotePrivate(boolean flag){
 		getEmote().set("private",new NBTTagByte("private",getBoolByte(flag)));
 	}
 	public void setEmoteSilent(boolean flag){
 		getEmote().set("silent",new NBTTagByte("silent",getBoolByte(flag)));
 	}
 	public void setFirstLogon(String arg){
 		c.set("firstlogon",new NBTTagString("firstlogon",arg));
 	}
 	public void setLastLogon(String arg){
 		c.set("lastlogon",new NBTTagString("lastlogon",arg));
 	}
 	public void setLastLogout(String arg){
 		c.set("lastlogout",new NBTTagString("lastlogout",arg));
 	}
 	public void setBackWarp(Location loc){
 		getOtherWarps().set("back",getLocationCompound(loc));
 	}
 	public void setHomeWarp(Location loc){
 		getOtherWarps().set("home",getLocationCompound(loc));
 	}
 	public void setOfflineWarp(Location loc){
 		getOtherWarps().set("offline",getLocationCompound(loc));
 	}
 	public void setNamedWarp(String warpname, Location loc){
 		getWarps().set(warpname, getLocationCompound(loc));
 	}
 	public void setBanned(boolean flag){
 		getBans().set("banned", new NBTTagByte("banned",getBoolByte(flag)));
 	}
 	/**
 	 * @param bantype
 	 * 0: not banned/unbanned
 	 * 1: tempbanned
 	 * 2: permabanned
 	 */
 	public void setBanType(int bantype){
 		getBans().set("bantype",new NBTTagInt("bantype",bantype));
 	}
 	public void setBanReason(String reason){
 		getBans().set("banreason", new NBTTagString("banreason",reason));
 	}
 	public void setUnbanTime(long arg){
 		getBans().set("unbantime", new NBTTagLong("unbantime",arg));
 	}
 	public void setNotes(ArrayList<String> set){
 		c.set("notes", getList(set));
 	}
 	
 	/*Utility methods*/
 	
 	private byte getBoolByte(boolean arg){
 		if(arg){
 			return 1;
 		} else {
 			return 0;
 		}
 	}
 	private NBTTagList getList(Collection<String> col){
 		NBTTagList list = new NBTTagList();
 		for(String s : col){
 			NBTTagString ts = new NBTTagString("", s);
 			list.add(ts);
 		}
 		return list;
 	}
 	public boolean save(){
 		try {
 			NBTCompressedStreamTools.a(c, new FileOutputStream(datFile));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	public static Pony moveToPonyville(Player pone) {
 		File datFile = new File(Mane.getInstance().getDataFolder()+File.separator+"Players",pone.getName());
 		if(datFile.exists()){
 			try {
 				return new Pony(pone);
 			} catch (Exception e) {
 				//TODO print error to Pony Logger
 				return null;
 			}
 		}
 		try {
 			datFile.createNewFile();
 		} catch (IOException e) {
 			//TODO print error to Pony Logger
 			return null;
 		}
 		NBTTagCompound c = new NBTTagCompound();
 		//set basics
 		c.set("name", new NBTTagString("name", pone.getName()));
 		c.set("muted", new NBTTagByte("muted", (byte)0));
 		c.set("silenced", new NBTTagList());
 		c.set("god", new NBTTagByte("god",(byte)0));
 		c.set("invisible",new NBTTagByte("invisible",(byte)0));
 		c.set("nickname", new NBTTagString("nickname",pone.getName()));
 		c.set("nickHistory", new NBTTagList());
		c.set("chatChannel", new NBTTagString("chatChannel","equestria"));
 		NBTTagList lc = new NBTTagList();
 		lc.add(new NBTTagString("channel","equestria"));
 		c.set("listenChannels",lc);
 		c.set("ponyspy", new NBTTagByte("ponyspy",(byte)0));
 		c.set("group", new NBTTagString("group","filly"));
 		c.set("perms", new NBTTagList());
 		NBTTagList ip = new NBTTagList();
 		ip.add(new NBTTagString("ip",getIp(pone)));
 		c.set("ipaddress", ip);
 		
 		//Horn
 		NBTTagCompound horn = new NBTTagCompound();
 		horn.set("left",new NBTTagString("left", "horn help"));
 		horn.set("right", new NBTTagString("right","horn help"));
 		horn.set("ison", new NBTTagByte("ison",(byte)0));
 		c.set("horn", horn);
 		
 		//set inventories
 		c.set("inventories", new NBTTagCompound());
 		
 		//set world settings
 		c.set("worlds",new NBTTagCompound());
 		
 		//set Remote Chest
 		NBTTagCompound rcCompound = new NBTTagCompound();
 		rcCompound.set("world", new NBTTagList());
 		rcCompound.set("badlands", new NBTTagList());
 		c.set("remotechest", rcCompound);
 		
 		
 		//emote
 		NBTTagCompound emote = new NBTTagCompound();
 		emote.set("name", new NBTTagString("name","none"));
 		emote.set("server", new NBTTagString("server","none"));
 		emote.set("sender", new NBTTagString("sender","none"));
 		emote.set("receiver", new NBTTagString("receiver","none"));
 		emote.set("private",new NBTTagByte("private",(byte)1));
 		emote.set("silent",new NBTTagByte("silent",(byte)1));
 		c.set("emote", emote);
 		
 		//login
 		c.set("firstlogon", new NBTTagString("firstlogon",Utils.getDate(pone.getFirstPlayed())));
 		c.set("lastlogon", new NBTTagString("lastlogon","n/a"));
 		c.set("lastlogout", new NBTTagString("lastlogout","n/a"));
 		
 		//warps
 		NBTTagCompound warps, other, back, home, offline;
 		warps = new NBTTagCompound();
 		other = new NBTTagCompound();
 		back = new NBTTagCompound();
 		home = new NBTTagCompound();
 		offline = new NBTTagCompound();
 		other.set("back", back);
 		other.set("home",home);
 		other.set("offline",offline);
 		home.set("other", other);
 		c.set("warps", warps);
 		
 		//ban
 		NBTTagCompound ban = new NBTTagCompound();
 		ban.set("banned", new NBTTagByte("banned",(byte)0));
 		ban.set("bantype", new NBTTagByte("bantype",(byte)0));
 		ban.set("banreason", new NBTTagString("banreason","n/a"));
 		ban.set("unbantime", new NBTTagLong("unbantime",0));
 		c.set("ban", ban);
 		c.set("notes", new NBTTagList());
 		
 		//save
 		try {
 			System.out.println("saving file");
 			NBTCompressedStreamTools.a(c, new FileOutputStream(datFile));
 		} catch (Exception e) {
 			return null;
 		}
 		try {
 			return new Pony(pone);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	private static String getIp(Player player){
 		return player.getAddress().toString().substring(1,player.getAddress().toString().indexOf(":"));
 	}
 }
