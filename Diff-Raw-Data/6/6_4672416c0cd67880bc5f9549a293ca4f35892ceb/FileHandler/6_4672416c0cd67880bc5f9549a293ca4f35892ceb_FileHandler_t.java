 package se.mickelus.customgen.newstuff;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.minecraft.nbt.CompressedStreamTools;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.server.MinecraftServer;
 import net.minecraftforge.common.BiomeDictionary.Type;
 import se.mickelus.customgen.Constants;
 import se.mickelus.customgen.MLogger;
 
 public class FileHandler {
 
 	/**
 	 * Parse a gen file, returning a Gen object.
 	 * @param name the name of the gen
 	 * @return
 	 */
 	private static Gen parseGen(String name, String resourcePack) {
 		NBTTagCompound nbt;
 		FileInputStream stream;
 		File file = new File(String.format(Constants.FILE_PATH, resourcePack, name));
 		
 		Gen gen = null;
 				
 		
 		try {
 			stream = new FileInputStream(file);
 		} catch (FileNotFoundException e) {
 			System.out.printf("Unable to load file \"%s\" in pack \"%s\".\n", name, resourcePack);
 			return null;
 		}
 		
 		try {
 			nbt = CompressedStreamTools.readCompressed(stream);
 		} catch (IOException e) {
 			System.out.printf("Unable to parse nbt from file \"%s\" in pack \"%s\".\n", name, resourcePack);
 			return null;
 		}
 		
 		if(nbt.getString(Gen.NAME_KEY) != name.substring(0, name.lastIndexOf('.'))) {
 			nbt.setString(Gen.NAME_KEY, name.substring(0, name.lastIndexOf('.')));
 		}
 		if(nbt.getString(Gen.RESOURCEPACK_KEY) != resourcePack) {
 			nbt.setString(Gen.RESOURCEPACK_KEY, resourcePack);
 		}
 		
 		gen = Gen.readFromNBT(nbt);
 		
 		
 		
 		return gen;
 	}
 	
 	/**
 	 * Parse all gen files in all resource packs and returns them in an array.
 	 * @return An array of gens from all resource packs
 	 */
 	public static Gen[] parseAllGens() {
 		List<Gen> genList = new LinkedList<Gen>();
 		Gen[] genArray;
 		MLogger.log("parsing gens");
 		File packsFolder = new File(Constants.PACKS_PATH);
 		
 		if(!packsFolder.isDirectory()) {
 			MLogger.log("Unable to parse gens: resourcepacks/assets directory missing.\n");
 			return null;
 		}
 		
 		String[] packNames = new File(Constants.PACKS_PATH).list();
 		
 		for (int i = 0; i < packNames.length; i++) {
 			File genDir = new File(String.format(Constants.GENS_PATH, packNames[i]));
 			MLogger.logf("path: %s", genDir.getAbsolutePath());
 			if(genDir.isDirectory()) {
 				System.out.printf("Resourcepack \"%s\" has gens!\n", packNames[i]);
 				String[] genNames = genDir.list();
 				for (int j = 0; j < genNames.length; j++) {
 					
 					System.out.println("file:" + genNames[j]);
					Gen gen = parseGen(genNames[j], packNames[i]);
					if(gen != null) {
						genList.add(gen);
					}
					
 				}
 			}
 			
 		}
 		
 		genArray = new Gen[genList.size()];
 		genList.toArray(genArray);
 		return genArray;
 	}
 	
 	/**
 	 * Save a gen to a file
 	 * @return
 	 */
 	public static boolean saveGenToFile(Gen gen) {	
 		File file = new File(String.format(Constants.FILE_PATH, gen.getResourcePack(), gen.getName()+Constants.FILE_EXT));
 		FileOutputStream stream;
 		NBTTagCompound nbt;
 		
 		if(!file.exists()) {
 			file.getParentFile().mkdirs();
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				System.out.printf("Unable to create file for gen \"%s\" in pack \"%s\".\n", gen.getName(), gen.getResourcePack());
 				return false;
 			}
 		}
 		
 		try {
 			stream = new FileOutputStream(file);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			System.out.printf("Unable to open file for gen \"%s\" in pack \"%s\".\n", gen.getName(), gen.getResourcePack());
 			return false;
 		}
 		
 		nbt = gen.writeToNBT(true);
 		
 		try {
 			CompressedStreamTools.writeCompressed(nbt, stream);
 		} catch (IOException e) {
 			System.out.printf("Unable to write to file for gen \"%s\" in pack \"%s\".\n", gen.getName(), gen.getResourcePack());
 			return false;
 		}
 		
 		try {
 			stream.close();
 		} catch (IOException e) {
 			System.out.printf("Unable to close stream after writing gen \"%s\" in pack \"%s\".\n", gen.getName(), gen.getResourcePack());
 		}
 		
 		return true;
 	}
 }
