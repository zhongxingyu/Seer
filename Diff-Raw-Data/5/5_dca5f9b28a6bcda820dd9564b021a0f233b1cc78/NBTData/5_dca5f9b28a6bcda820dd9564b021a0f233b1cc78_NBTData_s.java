 package fuj1n.globalChestMod.client.nbt;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.nbt.CompressedStreamTools;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.world.MinecraftException;
 import cpw.mods.fml.common.FMLLog;
 
 public class NBTData {
 
 	private final FMLLog logger = new FMLLog();
 
 	private final File NBTLocation;
 	private final File NBTFile;
 
 	private final String prefix;
 	private final String postfix;
 
 	private final long milliTime = System.currentTimeMillis();
 
 	public NBTData(String fileName, String postfix) {
 		this.postfix = postfix;
 		prefix = fileName;
 		if (MinecraftServer.getServer() != null && MinecraftServer.getServer().isDedicatedServer()) {
 			NBTLocation = new File("null");
 			NBTFile = new File("null");
 		} else {
 			NBTLocation = new File(Minecraft.getMinecraftDir(), "globalChestMod");
 			NBTFile = new File(NBTLocation, fileName + postfix + ".dat");
 			NBTLocation.mkdirs();
 			try {
 				if (!NBTFile.exists()) {
 					NBTFile.createNewFile();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			setSessionLock();
 		}
 	}
 
 	private void setSessionLock() {
 		try {
 			File file = new File(NBTLocation, "sessionlock" + prefix + postfix + ".lock");
 			DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file));
 
 			try {
 				dataoutputstream.writeLong(milliTime);
 			} finally {
 				dataoutputstream.close();
 			}
 		} catch (IOException ioexception) {
 			ioexception.printStackTrace();
 			throw new RuntimeException("Failed to check session lock, aborting");
 		}
 	}
 
 	public void checkSessionLock() throws MinecraftException {
 		try {
			File file = new File(NBTLocation, "sessionlock" + postfix + ".lock");
 			DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));
 			try {
 				if (datainputstream.readLong() != milliTime) {
 					throw new MinecraftException("The save is being accessed from another location, aborting");
 				}
 			} finally {
 				datainputstream.close();
 			}
 		} catch (IOException ioexception) {
 			throw new MinecraftException("Failed to check session lock, aborting");
 		}
 	}
 
 	public void clearSessionLock() {
		File file = new File(NBTLocation, "sessionlock" + postfix + ".lock");
 		if (file.exists()) {
 			file.delete();
 		}
 	}
 
 	public NBTTagCompound getNBTTagCompound() {
 		File file = NBTFile;
 		if (file.exists()) {
 			try {
 				NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file));
 				return nbttagcompound;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return new NBTTagCompound();
 	}
 
 	public void saveNBTData(NBTTagCompound NBTData) {
 		try {
 			checkSessionLock();
 		} catch (MinecraftException e1) {
 			System.out.println("Session lock check returned false.");
 			return;
 		}
 		try {
 			if (NBTData != null) {
 				File file = NBTFile;
 
 				if (file.exists()) {
 					file.delete();
 					file.createNewFile();
 				} else {
 					file.createNewFile();
 				}
 
 				CompressedStreamTools.writeCompressed(NBTData, new FileOutputStream(file));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static class filenames {
 		public static final String NAME_GLOBALCHEST = "globalChestData";
 	}
 
 }
