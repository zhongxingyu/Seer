 package mods.alice.villagerblock;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import mods.alice.villagerblock.block.BlockVillager;
 import mods.alice.villagerblock.block.BlockVillagerSlab;
 import mods.alice.villagerblock.inventory.ContainerAutoMerchant;
 import mods.alice.villagerblock.item.ItemVillagerBlockelize;
 import mods.alice.villagerblock.item.block.ItemBlockVillager;
 import mods.alice.villagerblock.tileentity.TileEntityVillager;
 import mods.alice.villagerblock.utility.ModLogger;
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class CommonProxy implements IGuiHandler
 {
 	void init()
 	{
 		NetworkRegistry network = NetworkRegistry.instance();
 		assert (network != null) : "No NetworkRegistry instance";
 
 		loadLanguages();
 		addBlocks();
 		addItems();
 		addRecipes();
 
 		network.registerGuiHandler(VillagerBlock.me, this);
 	}
 
 	void addBlocks()
 	{
 		Block b;
 
 		ItemManager.addBlock(new BlockVillager());
 		ItemManager.addBlock(new BlockVillagerSlab());
 
 		b = ItemManager.getBlock(BlockVillager.class);
 		GameRegistry.registerBlock(b, ItemBlockVillager.class, "Villager");
 		b = ItemManager.getBlock(BlockVillagerSlab.class);
 		GameRegistry.registerBlock(b, "VillagerSlab");
 
 		GameRegistry.registerTileEntity(TileEntityVillager.class, "VillagerBlock");
 	}
 
 	void addItems()
 	{
 		ItemManager.addItem(new ItemVillagerBlockelize());
 	}
 
 	void addRecipes()
 	{
 		Block b;
 		CraftingManager craft = CraftingManager.getInstance();
 		Item i;
 		ItemStack itemStack;
 		Object recipe[];
 
 		assert (craft != null) : " No CraftingManager instance";
 
 		recipe = new Object[9];
 		recipe[0] = " DE";
 		recipe[1] = " BD";
 		recipe[2] = "B  ";
 		recipe[3] = Character.valueOf('D');
 		recipe[4] = Item.diamond;
 		recipe[5] = Character.valueOf('E');
 		recipe[6] = Block.blockEmerald;
 		recipe[7] = Character.valueOf('B');
 		recipe[8] = Item.blazeRod;
 		i = ItemManager.getItem(ItemVillagerBlockelize.class);
 		itemStack = new ItemStack(i, 1, 0);
 		craft.addRecipe(itemStack, recipe);
 
 		/*
 		recipe = new Object[3];
 		recipe[0] = new ItemStack(ItemManager.itemVillagerBlockelize, 1, 42);
 		recipe[1] = Item.emerald;
 		recipe[2] = Item.emerald;
 		itemStack = new ItemStack(ItemManager.itemVillagerBlockelize);
 		craft.addShapelessRecipe(itemStack, recipe);
 		*/
 
 		b = ItemManager.getBlock(BlockVillager.class);
 		recipe = new Object[1];
 		recipe[0] = b;
 		itemStack = new ItemStack(b, 1, 0);
 		craft.addShapelessRecipe(itemStack, recipe);
 	}
 
 	void loadLanguages()
 	{
 		InputStream languageListFile;
 		InputStreamReader reader;
 		LanguageRegistry lang = LanguageRegistry.instance();
 		LineNumberReader lineReader;
 		List<String> languageList;
 		Matcher m;
 		Pattern r;
 		String line, matchedLine;
 
 		languageListFile = CommonProxy.class.getResourceAsStream("/mods/alice/villagerblock/lang/languages.txt");
 		if(languageListFile == null)
 		{
 			ModLogger.warning("Language list is not found. Trying to load en_US language...");
 			loadLang(lang, "en_US");
 
 			return;
 		}
		reader = new InputStreamReader(languageListFile, StandardCharsets.US_ASCII);
 		lineReader = new LineNumberReader(reader);
 
 		languageList = new ArrayList<String>();
 		r = Pattern.compile("([a-z]{2}_[A-Z]{2})");
 
 		for(;;)
 		{
 			try
 			{
 				line = lineReader.readLine();
 			}
 			catch(IOException e)
 			{
 				break;
 			}
 
 			if(line == null)
 			{
 				break;
 			}
 			if(line.length() > 0)
 			{
 				m = r.matcher(line);
 				if(m.find())
 				{
 					matchedLine = m.group();
 
 					languageList.add(matchedLine);
 				}
 			}
 		}
 		try
 		{
 			lineReader.close();
 			reader.close();
 			languageListFile.close();
 		}
 		catch(IOException e)
 		{
 		}
 
 		if(languageList.size() > 0)
 		{
 			for(String langName : languageList)
 			{
 				ModLogger.config("Loading language %s...", langName);
 				loadLang(lang, langName);
 			}
 		}
 	}
 
 	static void loadLang(LanguageRegistry lang, String langName)
 	{
 		String langPath;
 
 		langPath = String.format("/mods/alice/villagerblock/lang/%s.txt", langName);
 		lang.loadLocalization(langPath, langName, false);
 	}
 
 	// IGuiHandler
 
 	@Override
 	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
 		TileEntity villagerBlock;
 
 		villagerBlock = world.getBlockTileEntity(x, y, z);
 		if((villagerBlock == null) || !(villagerBlock instanceof TileEntityVillager))
 		{
 			return null;
 		}
 
 		return new ContainerAutoMerchant(player.inventory, (TileEntityVillager)villagerBlock, world);
 	}
 
 	@Override
 	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
 		return null;
 	}
 }
