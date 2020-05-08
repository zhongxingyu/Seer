 package me.foxtopia.fizzix;
 
 import java.util.Arrays;
 
 import org.bouncycastle.asn1.cms.MetaData;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockGrass;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.ModMetadata;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @Mod(modid = "Fizzix", name = "Fizzix", version = "1.0")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class Fizzix {
 
 	public static Block Grass;
 	public static Block Dirt;
 	public static Block Clay;
 	public static Block SoulSand;
 	public static Block CobbleStone;
 	 public Fizzix()
 	 {
 		
 	 }
 	 @PreInit
 	 public void preInit(FMLPreInitializationEvent event)
 	 {
 		 Block.blocksList[3] = null;
 		 Block.blocksList[4] = null;
 		 Block.blocksList[82] = null;
 		 Block.blocksList[88] = null;
 		 
 		 
 		 Dirt = new fzDirt(3,Material.ground).setUnlocalizedName("dirt").setHardness(0.5F).setStepSound(Block.soundGravelFootstep);
 		 CobbleStone = new fzCobbleStone(4,Material.rock).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("stonebrick");
 		 Clay = new fzClay(82,Material.clay).setHardness(0.6F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("clay");
 		 SoulSand = new fzSoulSand(88,Material.sand).setHardness(0.5F).setStepSound(Block.soundSandFootstep).setUnlocalizedName("hellsand");
 		 
 		 GameRegistry.registerBlock(Dirt,"dirt");
 		 GameRegistry.registerBlock(CobbleStone,"stonebrick");
 		 GameRegistry.registerBlock(Clay,"clay");
 		 GameRegistry.registerBlock(SoulSand,"hellsand");
 		 
 		try {
 			Class<?> modClass = Class.forName("net.minecraft.block.Block");
 			try {
				final Field field = modClass.getDeclaredField("dirt");
 				field.setAccessible(true);
 				try {
 					field.set(field,Dirt);
 				} catch (IllegalArgumentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} catch (NoSuchFieldException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		 
 	 }
 	
 }
