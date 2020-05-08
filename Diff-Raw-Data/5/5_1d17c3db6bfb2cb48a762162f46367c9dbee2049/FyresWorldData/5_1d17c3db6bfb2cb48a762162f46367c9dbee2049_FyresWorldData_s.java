 package assets.fyresmodjam;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldSavedData;
 import net.minecraft.world.WorldServer;
 import net.minecraft.world.storage.MapStorage;
 
 public class FyresWorldData extends WorldSavedData {
 
 	public static String[] validDisadvantages = {/*"Illiterate",*/ "Tougher Mobs", "Weak", "Explosive Traps"};
 	public static String[] disadvantageDescriptions = {/*"Item names are unreadable",*/ "-25% damage to hostile enemies", "-25% melee damage", "Traps trigger explosions on failed disarms"};
 	
 	public static String key = "FyresWorldData";
 	
 	public static int[] potionValues = null;
 	public static int[] potionDurations = null;
 	
 	public static String currentDisadvantage = null;
 
 	public FyresWorldData() {
 		super(key);
 		
 		checkWorldData();
 	}
 
 	public FyresWorldData(String key) {
 		super(key);
 	}
 
 	public static FyresWorldData forWorld(World world) {
 		MapStorage storage = world.perWorldStorage;
 		FyresWorldData result = (FyresWorldData) storage.loadData(FyresWorldData.class, key);
 		if (result == null) {result = new FyresWorldData(); storage.setData(key, result); }
 		return result;
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbttagcompound) {
 		if(nbttagcompound.hasKey("values")) {potionValues = nbttagcompound.getIntArray("values");}
 		if(nbttagcompound.hasKey("durations")) {potionDurations = nbttagcompound.getIntArray("durations");}
 		if(nbttagcompound.hasKey("currentDisadvantage")) {currentDisadvantage = nbttagcompound.getString("currentDisadvantage");}
 		checkWorldData();
 	} 
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbttagcompound) {
 		checkWorldData();
 		nbttagcompound.setIntArray("values", potionValues);
 		nbttagcompound.setIntArray("durations", potionDurations);
 		nbttagcompound.setString("currentDisadvantage", currentDisadvantage);
 	}
 	
 	private void checkWorldData() {
 		if(potionValues == null) {
 			potionValues = new int[12];
 			
 			for(int i = 0; i < 12; i++) {
 				int i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);
 				while(Potion.potionTypes[i2] == null) {i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);}
 				
 				//boolean skip = false;
 				//for(int i3 = 0; i3 < potionValues.length; i3++) {if(potionValues[i] == i3) {skip = true; break;}}
 				//if(skip) {continue;}
 						
 				potionValues[i] = i2;// break;
 			}
 		} else {
 			for(int i = 0; i < 12; i++) {
 				if(Potion.potionTypes[potionValues[i]] == null) {
 					int i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);
 					while(Potion.potionTypes[i2] == null) {i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);}
 					potionValues[i] = i2;
 				}
 			}
 		}
 		
 		if(potionDurations == null) {potionDurations = new int[12];}
 		for(int i = 0; i < 12; i++) {if(potionDurations[i] != 0) {continue;} potionDurations[i] = 5 + ModjamMod.r.nextInt(26);}
 		
 		boolean changeDisadvantage = currentDisadvantage == null;
 		
 		if(!changeDisadvantage) {
 			boolean valid = false;
 			for(String s : validDisadvantages) {if(s.equals(currentDisadvantage)) {valid = true; break;}}
 			changeDisadvantage = !valid;
 		}
 		
 		if(changeDisadvantage) {
 			currentDisadvantage = validDisadvantages[ModjamMod.r.nextInt(validDisadvantages.length)];
 			
 			/*MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
 			
 			for(int i = 0; i < server.worldServers.length; i++) {
 				WorldServer s = FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[i];
 				
 				if(s == null) {continue;}
 				
 				for(Object o : s.loadedEntityList) {
 					if(o == null || o instanceof EntityPlayer) {continue;}
 					EntityStatHelper.processEntity((Entity) o, ModjamMod.r);
 				}
 			}*/
 		}
 	}
 }
