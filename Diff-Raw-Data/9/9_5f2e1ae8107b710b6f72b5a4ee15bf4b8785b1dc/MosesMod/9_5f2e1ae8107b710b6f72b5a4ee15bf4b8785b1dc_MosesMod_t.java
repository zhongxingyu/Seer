 package hunternif.mc.moses;
 
 import hunternif.mc.moses.block.TransparentBlock;
 import hunternif.mc.moses.data.MosesBlockProvider;
 import hunternif.mc.moses.item.BurntStaffOfMoses;
 import hunternif.mc.moses.item.StaffOfMoses;
 import hunternif.mc.moses.material.MaterialWaterBlocker;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MathHelper;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.item.ItemExpireEvent;
 import net.minecraftforge.event.entity.item.ItemTossEvent;
 import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid=MosesMod.ID, name=MosesMod.NAME, version=MosesMod.VERSION)
 @NetworkMod(clientSideRequired=true, serverSideRequired=true)
 public class MosesMod {
 	public static final String ID = "MosesMod";
 	public static final String NAME = "Moses Mod";
	public static final String VERSION = "1.1.2";
 	public static final String CHANNEL = ID;
 	
 	public static final String KEY_PASSAGE_HALF_WIDTH = "mosesPassageHalfWidth";
 	public static final String KEY_PASSAGE_LENGTH = "mosesPassageLength";
 	
 	public static List<Item> itemList = new ArrayList<Item>();
 	private static int staffOfMosesId;
 	public static Item staffOfMoses;
 	private static int burntStaffOfMosesId;
 	public static Item burntStaffOfMoses;
 	private static int passageHalfWidth;
 	public static double passageLength;
 	private static int waterBlockerID;
 	public static Block waterBlocker;
 	public static Material materialWaterBlocker = new MaterialWaterBlocker();
 	
 	public static MosesBlockProvider mosesBlockProvider = new MosesBlockProvider();
 	
 	public static Configuration config;
 	
 	public List<EntityItem> tossedSticks = new ArrayList<EntityItem>();
 	public List<EntityItem> tossedStaffs = new ArrayList<EntityItem>();
 	public TickHandler tickHandler = new TickHandler();
 	
 	@Instance(ID)
 	public static MosesMod instance;
 	
 	@SidedProxy(clientSide="hunternif.mc.moses.MosesClientProxy", serverSide="hunternif.mc.moses.MosesCommonProxy")
 	public static MosesCommonProxy proxy;
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 		proxy.registerSounds();
 		MinecraftForge.EVENT_BUS.register(this);
 		
 		config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		
 		staffOfMosesId = config.getItem("staffOfMoses", 26999).getInt();
 		burntStaffOfMosesId = config.getItem("burntStaffOfMoses", 26998).getInt();
 		waterBlockerID = config.getBlock("waterBlocker", 2699).getInt();
 		
 		Property propHalfWidth = config.get(Configuration.CATEGORY_GENERAL, KEY_PASSAGE_HALF_WIDTH, 2);
 		propHalfWidth.comment = "Maximum width of a passage is 2*N + 1, where N is this value.";
 		passageHalfWidth = propHalfWidth.getInt(2);
 		
 		Property propLength = config.get(Configuration.CATEGORY_GENERAL, KEY_PASSAGE_LENGTH, 64.0d);
 		propLength.comment = "Maximum length of one passage.";
 		passageLength  = propLength.getDouble(64.0d);
 		
 		config.save();
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event) {
 		staffOfMoses = new StaffOfMoses(staffOfMosesId).setCreativeTab(CreativeTabs.tabTools).setUnlocalizedName("staffOfMoses");
 		LanguageRegistry.addName(staffOfMoses, "Staff Of Moses");
 		itemList.add(staffOfMoses);
 		((StaffOfMoses)staffOfMoses).passageHalfWidth = passageHalfWidth;
 		((StaffOfMoses)staffOfMoses).maxPassageLength = passageLength;
 		
 		burntStaffOfMoses = new BurntStaffOfMoses(burntStaffOfMosesId).setCreativeTab(CreativeTabs.tabTools).setUnlocalizedName("burntStaffOfMoses");
 		LanguageRegistry.addName(burntStaffOfMoses, "Burnt Staff Of Moses");
 		itemList.add(burntStaffOfMoses);
 		((BurntStaffOfMoses)burntStaffOfMoses).passageHalfWidth = passageHalfWidth;
 		((BurntStaffOfMoses)burntStaffOfMoses).maxPassageLength = passageLength;
 		
 		waterBlocker = new TransparentBlock(waterBlockerID, materialWaterBlocker).setStepSound(Block.soundGlassFootstep).setUnlocalizedName("waterBlocker").setCreativeTab(CreativeTabs.tabMisc);
 		GameRegistry.registerBlock(waterBlocker, "waterBlocker");
 		LanguageRegistry.addName(waterBlocker, "Water Blocker");
 		
 		proxy.registerRenderers();
		TickRegistry.registerTickHandler(tickHandler, Side.SERVER);
 		GameRegistry.registerPlayerTracker(new PlayerTracker());
 	}
 	
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 	}
 	
 	@ForgeSubscribe
 	public void onItemToss(ItemTossEvent event) {
 		ItemStack stack = event.entityItem.getEntityItem(); 
 		if (stack.itemID == Item.stick.itemID) {
 			tossedSticks.add(event.entityItem);
 		} else if (stack.itemID == staffOfMoses.itemID) {
 			tossedStaffs.add(event.entityItem);
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onItemExpire(ItemExpireEvent event) {
 		tossedSticks.remove(event.entityItem);
 	}
 	
 	@ForgeSubscribe
 	public void onItemPickup(EntityItemPickupEvent event) {
 		tossedSticks.remove(event.item);
 	}
 	
 	public class TickHandler implements ITickHandler {
 		@Override
 		public void tickStart(EnumSet<TickType> type, Object... tickData) {
 			if (type.contains(TickType.PLAYER)) {
 				// Turn sticks on fire on leaves into Staff of Moses
 				for (EntityItem stick : tossedSticks) {
 					int x = MathHelper.floor_double(stick.posX);
 					int y = MathHelper.floor_double(stick.posY);
 					int z = MathHelper.floor_double(stick.posZ);
 					boolean foundFire = stick.worldObj.getBlockId(x, y, z) == Block.fire.blockID;
 					boolean foundBush = stick.worldObj.getBlockId(x, y-1, z) == Block.leaves.blockID;
 					if (stick.isBurning() && foundFire && foundBush) {
 						tossedSticks.remove(stick);
 						stick.setEntityItemStack(new ItemStack(staffOfMoses));
 						stick.extinguish();
 						stick.worldObj.setBlockToAir(x, y, z);
 						stick.worldObj.playSoundAtEntity(stick, MosesSounds.MOSES, 1, 1);
 						break;
 					}
 				}
 				// Turn Staff of Moses in lava into Burnt Staff of Moses
 				for (EntityItem staff : tossedStaffs) {
 					int x = MathHelper.floor_double(staff.posX);
 					int y = MathHelper.floor_double(staff.posY);
 					int z = MathHelper.floor_double(staff.posZ);
 					int blockID = staff.worldObj.getBlockId(x, y, z);
 					if (blockID == Block.lavaMoving.blockID || blockID == Block.lavaStill.blockID) {
 						tossedStaffs.remove(staff);
 						staff.setEntityItemStack(new ItemStack(burntStaffOfMoses));
 						staff.extinguish();
 						staff.worldObj.setBlockToAir(x, y, z);
 						staff.worldObj.playSoundAtEntity(staff, MosesSounds.BURNT_STAFF, 1, 1);
 						break;
 					}
 				}
 			}
 		}
 		@Override
 		public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 		}
 		@Override
 		public EnumSet<TickType> ticks() {
 			return EnumSet.of(TickType.PLAYER);
 		}
 		@Override
 		public String getLabel() {
 			return null;
 		}
 	}
 }
