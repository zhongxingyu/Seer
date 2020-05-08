 package me.foxtopia.fizzix;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bouncycastle.asn1.cms.MetaData;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import forestry.api.core.*;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockDirt;
 import net.minecraft.block.BlockGrass;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.logging.LogAgent;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
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
 
 @Mod(modid = "Fizzix", name = "Fizzix",dependencies="after:Forestry", version = "1.2")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class Fizzix {
 
 	
 	 public HashMap<String,Integer> modFences = new HashMap<String,Integer>();
 	 public static Integer ForestryBlockIdA;
 	 public static Integer ForestryBlockIdB;
 	 
 	 public Fizzix()
 	 {
 		
 	 }
 	 @PreInit
 	 public void preInit(FMLPreInitializationEvent event) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException
 	 {
  
 		try {
 			Class<?> modClass = Class.forName("net.minecraft.block.Block");
 				//Dirt
 				  Field fieldDirt = modClass.getDeclaredField("field_71979_v");
 			      fieldDirt.setAccessible(true);
 				  Field modifiersField = Field.class.getDeclaredField("modifiers");
 			      modifiersField.setAccessible(true);
 			      modifiersField.setInt(fieldDirt, fieldDirt.getModifiers() & ~Modifier.FINAL);
 			      Block.blocksList[3] = null;
 			      fieldDirt.set(null,(new fzDirt(3)).setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("dirt"));
 				
 				//Cobble
 			      Field fieldCobble = modClass.getDeclaredField("field_71978_w");
 			      fieldCobble.setAccessible(true);
 				  Field modifiersFieldCobble = Field.class.getDeclaredField("modifiers");
 				  modifiersFieldCobble.setAccessible(true);
 				  modifiersFieldCobble.setInt(fieldCobble, fieldCobble.getModifiers() & ~Modifier.FINAL);
 				  Block.blocksList[4] = null;
 				  fieldCobble.set(fieldCobble,(new fzCobbleStone(4,Material.rock).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("stonebrick")));
 					
 				
 				//Clay
 			      Field fieldClay = modClass.getDeclaredField("field_72041_aW");
 			      fieldClay.setAccessible(true);
 				  Field modifiersFieldClay = Field.class.getDeclaredField("modifiers");
 				  modifiersFieldClay.setAccessible(true);
 				  modifiersFieldClay.setInt(fieldClay, fieldClay.getModifiers() & ~Modifier.FINAL);
 				  Block.blocksList[82] = null;
 				  fieldClay.set(fieldClay,(new fzClay(82,Material.clay).setHardness(0.6F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("clay")));
 	
 				
 				//SoulSand
 			      Field fieldhSand = modClass.getDeclaredField("field_72013_bc");
 			      fieldhSand.setAccessible(true);
 				  Field modifiersFieldhSand = Field.class.getDeclaredField("modifiers");
 				  modifiersFieldhSand.setAccessible(true);
 				  modifiersFieldhSand.setInt(fieldhSand, fieldhSand.getModifiers() & ~Modifier.FINAL);
 				  Block.blocksList[88] = null;
 				  fieldhSand.set(fieldhSand,(new fzSoulSand(88,Material.sand).setHardness(0.5F).setStepSound(Block.soundSandFootstep).setUnlocalizedName("hellsand")));
 	
				if (!Loader.isModLoaded("Forestry"))
				{
					ForestryBlockIdA = Block.fence.blockID;
					ForestryBlockIdB = Block.netherFence.blockID;
					return;
				}
 			Class<?> ForestryConfig = Class.forName("forestry.core.config.ForestryBlock");
 				Field fences1 = ForestryConfig.getDeclaredField("fences1");
 			    FMLLog.fine("Forestry Located :" + fences1.getName(), (Object[])null);
 				
 				ForestryBlockIdA =(Integer)(((Block) fences1.get(fences1)).blockID);
 				Field fences2 = ForestryConfig.getDeclaredField("fences2");
 				 FMLLog.fine("Forestry Located :" + fences2.getName(), (Object[])null);
 					
 				ForestryBlockIdB =(Integer)(((Block) fences2.get(fences2)).blockID);
 				
 				}  catch (ClassNotFoundException e) {
 				
 					  
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (NoSuchFieldException e) {
 					// TODO Auto-generated catch block
 					
 					e.printStackTrace();
 				} catch (SecurityException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		
 		 
 		 
 	 }
 	
 }
