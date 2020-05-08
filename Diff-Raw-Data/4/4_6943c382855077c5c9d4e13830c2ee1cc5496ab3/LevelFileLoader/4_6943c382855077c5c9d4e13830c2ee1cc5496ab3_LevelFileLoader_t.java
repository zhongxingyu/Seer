 package graindcafe.tribu;
 
 import graindcafe.tribu.signs.TribuSign;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.inventory.ItemStack;
 
 public class LevelFileLoader {
 
 	private Set<String> levels;
 	private Tribu plugin;
 
 	public LevelFileLoader(Tribu instance) {
 		plugin = instance;
 		levels = new HashSet<String>();
 		levels.clear();
 		// File dir = new File(Constants.levelFolder);
 		File dir = new File(Constants.levelFolder);
 		if (!dir.exists()) {
 			plugin.LogInfo(plugin.getLocale("Info.LevelFolderDoesntExist"));
 			String[] levelFolders = Constants.levelFolder.split("/");
 			String tmplevelFolder = "";
 			for (byte i = 0; i < levelFolders.length; i++) {
 				tmplevelFolder = tmplevelFolder.concat(levelFolders[i] + File.separatorChar);
 				dir = new File(tmplevelFolder);
				if(!dir.mkdir())
 					plugin.LogSevere(plugin.getLocale("Severe.TribuCantMkdir"));
 			}
 		}
 		File[] files = dir.listFiles();
 		plugin.LogInfo(String.format(plugin.getLocale("Info.LevelFound"), String.valueOf(files == null ? 0 :files.length)));
 		if (files != null) {
 			for (File file : files) {
 				levels.add(file.getName().substring(0, file.getName().lastIndexOf(".")));
 			}
 		}
 
 	}
 
 	public boolean deleteLevel(String name) {
 		File file = new File(Constants.levelFolder + "/" + name + ".lvl");
 		if (file.exists()) {
 			boolean result = file.delete();
 			if (!result) {
 				plugin.LogWarning(plugin.getLocale("Warning.IOErrorOnFileDelete"));
 			} else {
 				levels.remove(name);
 			}
 			return result;
 		}
 		return false;
 	}
 
 	public boolean exists(String name) {
 		File file = new File(Constants.levelFolder + "/" + name + ".lvl");
 		return (file.exists());
 	}
 
 	public Set<String> getLevelList() {
 		return levels;
 	}
 
 	public TribuLevel loadLevel(String name) {
 		TribuLevel level = null;
 		try {
 
 			File file = new File(Constants.levelFolder + "/" + name + ".lvl");
 			if (!file.exists()) {
 				return null;
 			}
 			FileInputStream fstream = new FileInputStream(file);
 			DataInputStream in = new DataInputStream(fstream);
 			int version = in.readByte();
 
 			if (version == 1) {
 				// set the file version
 				new FileOutputStream(file).write(2);
 				version = 2;
 
 				// set sign count = 0
 				new FileOutputStream(file, true).write(0);
 				in.reset();
 			}
 			if(version == 2 )
 			{
 				// set the file version
 				new FileOutputStream(file).write(3);
 				version = 3;
 
 				// set package count = 0
 				new FileOutputStream(file, true).write(0);
 				in.reset();
 			}
 			if (version != Constants.LevelFileVersion) {
 				fstream.close();
 				plugin.LogSevere(plugin.getLocale("Severe.WorldInvalidFileVersion"));
 				return null;
 			}
 			World world = plugin.getServer().getWorld(in.readUTF());
 			if (world == null) {
 				fstream.close();
 				plugin.LogSevere(plugin.getLocale("Severe.WorldDoesntExist"));
 				return null;
 			}
 			double sx, sy, sz; // spawn coords
 			double dx, dy, dz; // Death coords
 			float sYaw, dYaw;
 			sx = in.readDouble();
 			sy = in.readDouble();
 			sz = in.readDouble();
 			sYaw = in.readFloat();
 
 			dx = in.readDouble();
 			dy = in.readDouble();
 			dz = in.readDouble();
 			dYaw = in.readFloat();
 
 			Location spawn = new Location(world, sx, sy, sz, sYaw, 0.0f);
 			Location death = new Location(world, dx, dy, dz, dYaw, 0.0f);
 
 			level = new TribuLevel(name, spawn);
 			level.setDeathSpawn(death);
 
 			int spawncount = in.readInt();
 
 			Location pos;
 			String spawnName;
 
 			for (int i = 0; i < spawncount; i++) {
 				sx = in.readDouble();
 				sy = in.readDouble();
 				sz = in.readDouble();
 				sYaw = in.readFloat();
 				spawnName = in.readUTF();
 				pos = new Location(world, sx, sy, sz, sYaw, 0.0f);
 				level.addZombieSpawn(pos, spawnName);
 			}
 			int count = in.readInt();
 			for (int i = 0; i < count; i++) {
 				if (!level.addSign(TribuSign.LoadFromStream(plugin, world, in))) {
 					plugin.LogWarning(plugin.getLocale("Warning.UnableToAddSign"));
 				}
 			}
 			count= in.readInt();
 			int iCount;
 			Package n;
 			for (int i = 0; i < count; i++) {
 				
 				n= new Package();
 				int strC=in.readInt();
 				char[] c=new char[strC];
 				byte k=0;
 				while(k<strC){
 					c[k]=in.readChar();
 					k++;
 				}
 				n.setName(new String(c));
 				iCount=in.readInt();
 				for (int j = 0; j < iCount; j++) {
 					n.addItem(in.readInt(), in.readByte(), in.readShort());
 				}
 				level.addPackage(n);
 			}
 			
 
 		} catch (Exception e) {
 			plugin.LogSevere(String.format(plugin.getLocale("Severe.ErrorDuringLevelLoading"), Tribu.getExceptionMessage(e)));
 			level = null;
 		}
 
 		return level;
 	}
 
 	public TribuLevel loadLevelIgnoreCase(String name) {
 		for (String level : levels) {
 			if (level.equalsIgnoreCase(name))
 				name = level;
 		}
 		return loadLevel(name);
 	}
 
 	public TribuLevel newLevel(String name, Location spawn) {
 		return new TribuLevel(name, spawn);
 	}
 
 	public boolean saveLevel(TribuLevel level) {
 		if (level == null) {
 			return true; // Sorta successful since a save isn't really needed
 							// and nothing failed
 		}
 
 		if (!level.hasChanged()) {
 			return true; // No need to save since the level hasn't changed
 		}
 
 		FileOutputStream out;
 		DataOutputStream o;
 		try {
 			out = new FileOutputStream(Constants.levelFolder + "/" + level.getName() + ".lvl", false);
 			o = new DataOutputStream(out);
 			Location spawn = level.getInitialSpawn();
 			Location death = level.getDeathSpawn();
 
 			o.writeByte(Constants.LevelFileVersion);
 
 			o.writeUTF(spawn.getWorld().getName());
 			o.writeDouble(spawn.getX());
 			o.writeDouble(spawn.getY());
 			o.writeDouble(spawn.getZ());
 			o.writeFloat(spawn.getYaw());
 
 			o.writeDouble(death.getX());
 			o.writeDouble(death.getY());
 			o.writeDouble(death.getZ());
 			o.writeFloat(death.getYaw());
 
 			HashMap<String, Location> zombieSpawns = level.getSpawns();
 			Set<Entry<String, Location>> set = zombieSpawns.entrySet();
 
 			o.writeInt(set.size());
 			for (Entry<String, Location> zspawn : set) {
 				o.writeDouble(zspawn.getValue().getX());
 				o.writeDouble(zspawn.getValue().getY());
 				o.writeDouble(zspawn.getValue().getZ());
 				o.writeFloat(zspawn.getValue().getYaw());
 				o.writeUTF(zspawn.getKey());
 			}
 			TribuSign[] signs = level.getSigns();
 			if (signs == null) {
 				o.writeInt(0);
 			} else {
 				o.writeInt(signs.length);
 				for (int i = 0; i < signs.length; i++) {
 					signs[i].SaveToStream(o);
 				}
 			}
 			o.writeInt(level.getPackages().size());
 			for(Package n : level.getPackages())
 			{
 				o.writeInt(n.getName().length());
 				o.writeChars(n.getName());
 				o.writeInt(n.getItemStacks().size());
 				for(ItemStack is : n.getItemStacks())
 				{
 					o.writeInt(is.getTypeId());
 					o.writeByte(is.getData().getData());
 					o.writeShort(is.getAmount());
 				}
 			}
 			o.flush();
 			o.close();
 			out.close();
 		} catch (Exception e) {
 			plugin.LogSevere(String.format(plugin.getLocale("Severe.ErrorDuringLevelSaving"), Tribu.getExceptionMessage(e)));
 			return false;
 		}
 		levels.add(level.getName());
 		return true;
 	}
 }
