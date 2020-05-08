 package com.turt2live.antishare.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.turt2live.antishare.util.ASUtils.EntityPattern;
 import com.turt2live.antishare.util.generic.MobPattern;
 
 public class TestASUtils {
 
 	private File testDirectory = new File("testingdirectory");
 	private List<String> knownFiles = new ArrayList<String>();
 	private CopyOnWriteArrayList<String> selectiveNames = new CopyOnWriteArrayList<String>();
 	private List<String> commas = new ArrayList<String>();
 	private Map<String, String> names = new HashMap<String, String>();
 
 	@Before
 	public void setUp() throws IOException{
 		// Runs before @Test
 		testDirectory.mkdirs();
 		for(int i = 0; i < 10; i++){
 			File file = new File(testDirectory, "file" + i + ".txt");
 			file.createNewFile();
 			knownFiles.add(file.getName());
 			File folder = new File(testDirectory, "folder" + i);
 			folder.mkdirs();
 			knownFiles.add(folder.getName());
 			for(int x = 0; x < 10; x++){
 				file = new File(folder, "file" + x + ".txt");
 				file.createNewFile();
 			}
 		}
 
 		// Initial names
 		selectiveNames.add("file1.txt");
 		selectiveNames.add("fileX.txt");
 
 		// Commas
 		for(int i = 0; i < 3; i++){
 			commas.add("c" + i);
 		}
 
 		// Names
 		names.put("blaze", "blaze");
 		names.put("cavespider", "cave spider");
 		names.put("cave spider", "cave spider");
 		names.put("chicken", "chicken");
 		names.put("cow", "cow");
 		names.put("creeper", "creeper");
 		names.put("enderdragon", "ender dragon");
 		names.put("ender dragon", "ender dragon");
 		names.put("enderman", "enderman");
 		names.put("ghast", "ghast");
 		names.put("giant", "giant");
 		names.put("irongolem", "iron golem");
 		names.put("iron golem", "iron golem");
 		names.put("mushroomcow", "mooshroom");
 		names.put("mushroom cow", "mooshroom");
 		names.put("mooshroom", "mooshroom");
 		names.put("ocelot", "ocelot");
 		names.put("cat", "ocelot");
 		names.put("pig", "pig");
 		names.put("pigzombie", "pigman");
 		names.put("zombiepigman", "pigman");
 		names.put("pig zombie", "pigman");
 		names.put("zombie pigman", "pigman");
 		names.put("pigman", "pigman");
 		names.put("sheep", "sheep");
 		names.put("silverfish", "silverfish");
 		names.put("skeleton", "skeleton");
 		names.put("slime", "slime");
 		names.put("magmacube", "magma cube");
 		names.put("magma cube", "magma cube");
 		names.put("spider", "spider");
 		names.put("snowman", "snowman");
 		names.put("squid", "squid");
 		names.put("villager", "villager");
 		names.put("testificate", "villager");
 		names.put("wolf", "wolf");
 		names.put("zombie", "zombie");
 		names.put("witch", "witch");
 		names.put("wither", "wither boss");
 		names.put("witherboss", "wither boss");
 		names.put("wither boss", "wither boss");
 		names.put("bat", "bat");
 	}
 
 	@After
 	public void tearDown(){
 		// Runs after @Test
 		ASUtils.wipeFolder(testDirectory, null);
 	}
 
 	@Test
 	public void testSendToPlayer(){
 		// TODO
 	}
 
 	@Test
 	public void testGetBoolean(){
 		// True
 		assertTrue(ASUtils.getBoolean("true"));
 		assertTrue(ASUtils.getBoolean("t"));
 		assertTrue(ASUtils.getBoolean("on"));
 		assertTrue(ASUtils.getBoolean("active"));
 		assertTrue(ASUtils.getBoolean("1"));
 
 		// False
 		assertFalse(ASUtils.getBoolean("false"));
 		assertFalse(ASUtils.getBoolean("f"));
 		assertFalse(ASUtils.getBoolean("off"));
 		assertFalse(ASUtils.getBoolean("inactive"));
 		assertFalse(ASUtils.getBoolean("0"));
 
 		// Invalid
 		assertNull(ASUtils.getBoolean("thisShouldBeNull"));
 		assertNull(ASUtils.getBoolean("not-a-boolean"));
 		assertNull(ASUtils.getBoolean(null));
 		assertNull(ASUtils.getBoolean(""));
 		assertNull(ASUtils.getBoolean(" "));
 		assertNull(ASUtils.getBoolean("		")); // Has a tab character in it
 
 		// Case insensitivity
 		assertTrue(ASUtils.getBoolean("TrUe"));
 		assertFalse(ASUtils.getBoolean("FaLsE"));
 		assertNull(ASUtils.getBoolean("NullValue"));
 	}
 
 	@Test
 	public void testGetGameMode(){
 		// Creative
 		assertEquals(GameMode.CREATIVE, ASUtils.getGameMode("creative"));
 		assertEquals(GameMode.CREATIVE, ASUtils.getGameMode("c"));
 		assertEquals(GameMode.CREATIVE, ASUtils.getGameMode("1"));
 
 		// Survival
 		assertEquals(GameMode.SURVIVAL, ASUtils.getGameMode("survival"));
 		assertEquals(GameMode.SURVIVAL, ASUtils.getGameMode("s"));
 		assertEquals(GameMode.SURVIVAL, ASUtils.getGameMode("0"));
 
 		// Adventure
 		assertEquals(GameMode.ADVENTURE, ASUtils.getGameMode("adventure"));
 		assertEquals(GameMode.ADVENTURE, ASUtils.getGameMode("a"));
 		assertEquals(GameMode.ADVENTURE, ASUtils.getGameMode("2"));
 
 		// Invalid
 		assertNull(ASUtils.getGameMode("NotGameMode"));
 		assertNull(ASUtils.getGameMode("  "));
 		assertNull(ASUtils.getGameMode("		")); // Has tab
 		assertNull(ASUtils.getGameMode(null));
 
 		// Case insensitivity
 		assertEquals(GameMode.CREATIVE, ASUtils.getGameMode("CreaTIve"));
 		assertEquals(GameMode.SURVIVAL, ASUtils.getGameMode("SurviVAL"));
 		assertEquals(GameMode.ADVENTURE, ASUtils.getGameMode("ADVenTUre"));
 	}
 
 	@Test
 	public void testBlockToString(){
 		// TODO
 	}
 
 	@Test
 	public void testMaterialToString(){
 		assertEquals("1:*", ASUtils.materialToString(Material.STONE, false));
 		assertEquals("1", ASUtils.materialToString(Material.STONE, true));
 		assertNull(ASUtils.materialToString(null, false));
 		assertNull(ASUtils.materialToString(null, true));
 	}
 
 	@Test
 	public void testGetWool(){
 		assertEquals("35:1", ASUtils.getWool("orange wool"));
 		assertEquals("35:0", ASUtils.getWool("white wool"));
 		assertEquals("35:2", ASUtils.getWool("magenta wool"));
 		assertEquals("35:3", ASUtils.getWool("light_blue wool"));
		assertEquals("35:4", ASUtils.getWool("light blue wool"));
		assertEquals("35:5", ASUtils.getWool("yellow wool"));
 		assertEquals("35:5", ASUtils.getWool("lime wool"));
 		assertEquals("35:6", ASUtils.getWool("pink wool"));
 		assertEquals("35:7", ASUtils.getWool("gray wool"));
 		assertEquals("35:8", ASUtils.getWool("light_gray wool"));
 		assertEquals("35:8", ASUtils.getWool("light gray wool"));
 		assertEquals("35:9", ASUtils.getWool("cyan wool"));
 		assertEquals("35:10", ASUtils.getWool("purple wool"));
 		assertEquals("35:11", ASUtils.getWool("blue wool"));
 		assertEquals("35:12", ASUtils.getWool("brown wool"));
 		assertEquals("35:13", ASUtils.getWool("green wool"));
 		assertEquals("35:14", ASUtils.getWool("red wool"));
 		assertEquals("35:15", ASUtils.getWool("black wool"));
 		assertEquals("35:notacolor", ASUtils.getWool("notacolor wool"));
 		assertEquals("35:0", ASUtils.getWool("wool"));
 		assertNull(ASUtils.getWool("notAColor"));
 		assertNull(ASUtils.getWool(null));
 	}
 
 	@Test
 	// ASUtils.getEntityName(Entity)
 	public void testGetEntityNameFromEntity(){
 		// TODO
 	}
 
 	@Test
 	public void testGetEntityNameFromString(){
 		for(String test : names.keySet()){
 			String proper = names.get(test);
 			assertEquals(proper, ASUtils.getEntityName(test));
 			assertEquals(proper, ASUtils.getEntityName(test.toUpperCase()));
 			assertEquals(proper, ASUtils.getEntityName(proper));
 			assertEquals(proper, ASUtils.getEntityName(proper.toUpperCase()));
 		}
 		assertNull(ASUtils.getEntityName("NotAMob"));
 		assertNull(ASUtils.getEntityName((String) null));
 	}
 
 	@Test
 	public void testAllEntities(){
 		assertTrue(ASUtils.allEntities().size() == 42);
 	}
 
 	@Test
 	public void testFindGameModePlayers(){
 		// TODO
 	}
 
 	@Test
 	public void testCommas(){
 		assertEquals("c0, c1, c2", ASUtils.commas(commas));
 		assertEquals("no one", ASUtils.commas(new ArrayList<String>()));
 		assertEquals("no one", ASUtils.commas(null));
 	}
 
 	@Test
 	public void testGamemodeAbbreviation(){
 		assertEquals("GM = C", ASUtils.gamemodeAbbreviation(GameMode.CREATIVE, false));
 		assertEquals("GM = S", ASUtils.gamemodeAbbreviation(GameMode.SURVIVAL, false));
 		assertEquals("GM = A", ASUtils.gamemodeAbbreviation(GameMode.ADVENTURE, false));
 		assertEquals("C", ASUtils.gamemodeAbbreviation(GameMode.CREATIVE, true));
 		assertEquals("S", ASUtils.gamemodeAbbreviation(GameMode.SURVIVAL, true));
 		assertEquals("A", ASUtils.gamemodeAbbreviation(GameMode.ADVENTURE, true));
 		assertNull(ASUtils.gamemodeAbbreviation(null, false));
 		assertNull(ASUtils.gamemodeAbbreviation(null, true));
 	}
 
 	@Test
 	public void testFileSafeName(){
 		assertEquals("000", ASUtils.fileSafeName("000"));
 		assertEquals("-0-", ASUtils.fileSafeName("@0@"));
 		assertEquals("AaA", ASUtils.fileSafeName("AaA"));
 		assertEquals("A-A", ASUtils.fileSafeName("A!A"));
 		assertEquals("---", ASUtils.fileSafeName("---"));
 	}
 
 	@Test
 	public void testWipeFolder() throws Exception{
 		// Wipe selective files
 		ASUtils.wipeFolder(testDirectory, selectiveNames);
 
 		// Verify
 		if(!testDirectory.exists()){
 			throw new Exception("Folder deleted too early");
 		}
 		for(File file : testDirectory.listFiles()){
 			String fname = file.getName();
 			if(selectiveNames.contains(fname)){
 				throw new Exception("Failure to delete file: " + fname);
 			}else if(!knownFiles.contains(fname)){
 				throw new Exception("Unexpected file: " + fname);
 			}
 		}
 
 		// Wipe all items
 		ASUtils.wipeFolder(testDirectory, null);
 
 		// Verify
 		if(!testDirectory.exists()){
 			throw new Exception("Folder deleted too early");
 		}
 		for(File file : testDirectory.listFiles()){
 			if(file.isFile()){
 				throw new Exception("File should be deleted: " + file.getName());
 			}
 		}
 
 		// Wipe nothing
 		ASUtils.wipeFolder(null, null);
 		ASUtils.wipeFolder(null, selectiveNames);
 
 		// == Nested files/folders ==
 
 		// Wipe selective items
 		ASUtils.wipeFolder(testDirectory, selectiveNames);
 
 		// Verify
 		if(!testDirectory.exists()){
 			throw new Exception("Folder deleted too early");
 		}
 		for(File file : testDirectory.listFiles()){
 			String fname = file.getName();
 			if(selectiveNames.contains(fname)){
 				throw new Exception("Failure to delete file: " + fname);
 			}else if(!knownFiles.contains(fname)){
 				throw new Exception("Unexpected file: " + fname);
 			}
 		}
 
 		// Wipe folder names too
 		selectiveNames.add("folder1");
 		selectiveNames.add("folderX");
 		ASUtils.wipeFolder(testDirectory, selectiveNames);
 
 		// Verify
 		if(!testDirectory.exists()){
 			throw new Exception("Folder deleted too early");
 		}
 		for(File file : testDirectory.listFiles()){
 			String fname = file.getName();
 			if(selectiveNames.contains(fname)){
 				throw new Exception("Failure to delete file: " + fname);
 			}else if(!knownFiles.contains(fname)){
 				throw new Exception("Unexpected file: " + fname);
 			}
 		}
 
 		// Wipe all items
 		ASUtils.wipeFolder(testDirectory, null);
 
 		// Verify
 		if(!testDirectory.exists()){
 			throw new Exception("Folder deleted too early");
 		}
 		assertTrue(testDirectory.listFiles().length == 0);
 	}
 
 	@Test
 	// ASUtils.giveTool(Material, Player)
 	public void testGiveTool(){
 		// TODO
 	}
 
 	@Test
 	// ASUtils.giveTool(Material, Player, int)
 	public void testGiveToolWithSlot(){
 		// TODO
 	}
 
 	@Test
 	public void testMultipleBlocks(){
 		// TODO
 	}
 
 	@Test
 	public void testGetMobPattern(){
 		assertTrue(ASUtils.getMobPattern(EntityPattern.SNOW_GOLEM) instanceof MobPattern);
 		assertTrue(ASUtils.getMobPattern(EntityPattern.IRON_GOLEM) instanceof MobPattern);
 		assertTrue(ASUtils.getMobPattern(EntityPattern.WITHER) instanceof MobPattern);
 		assertNull(ASUtils.getMobPattern(null));
 	}
 
 }
