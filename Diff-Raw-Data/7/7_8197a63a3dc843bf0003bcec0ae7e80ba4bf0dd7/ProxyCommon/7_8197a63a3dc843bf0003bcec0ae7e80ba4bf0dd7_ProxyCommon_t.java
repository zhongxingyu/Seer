 package denoflionsx.DenPipes.Proxy;
 
 import buildcraft.BuildCraftTransport;
 import buildcraft.transport.ItemPipe;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.GameRegistry;
 import denoflionsx.DenPipes.API.DenPipesAPI;
 import denoflionsx.DenPipes.API.Interfaces.IDenPipeAddon;
 import denoflionsx.DenPipes.Config.Tuning;
 import denoflionsx.DenPipes.Core.DenPipesCore;
 import denoflionsx.denLib.Lib.denLib;
 import denoflionsx.denLib.Mod.denLibMod;
 import java.io.File;
 import java.util.ArrayList;
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 
 public class ProxyCommon {
 
     public void print(String msg) {
         FMLLog.info("[" + "@NAME@" + "]: " + msg);
     }
 
     public void severe(String msg) {
         FMLLog.severe("[" + "@NAME@" + "]: " + msg);
     }
 
     public Configuration getParentConfig() {
         return Tuning.config;
     }
 
     public void setupConfig(FMLPreInitializationEvent event) {
         File configDir = new File(event.getModConfigurationDirectory().getAbsolutePath() + "/denoflionsx/" + "@NAME@" + "/");
         File configFile = new File(configDir.getAbsolutePath() + "/" + event.getSuggestedConfigurationFile().getName());
         denLibMod.Proxy.makeDirs(configDir);
         Tuning.config = new Configuration(configFile);
         denLibMod.tuning.registerTunableClass(Tuning.class);
     }
 
     public void findInternalAddons(File source) {
        DenPipesCore.proxy.print("Starting addon load...");
         ArrayList<Object> addons = denLib.FileUtils.getClassesInJar(source, IDenPipeAddon.class);
        for (IDenPipeAddon a : DenPipesAPI.addons){
            addons.add(a);
        }
        DenPipesAPI.addons.clear();
         for (Object o : addons) {
             DenPipesAPI.addons.add((IDenPipeAddon) o);
         }
         DenPipesCore.proxy.print("Done. " + addons.size() + " addons loaded.");
     }
 
     public void registerPipeRendering(ItemPipe pipe) {
     }
 
     public void registerPipeRecipe(ItemPipe pipe, int amount, ItemStack material) {
         GameRegistry.addRecipe(new ItemStack(pipe, amount, 0), new Object[]{"XXX", "MGM", "XXX", Character.valueOf('G'), Block.glass, Character.valueOf('M'), material});
     }
 
     public void registerPipeRecipe(ItemPipe pipe, ItemStack material) {
         this.registerPipeRecipe(pipe, 8, material);
     }
 
     public void registerWaterproofPipeRecipe(ItemPipe pipe, ItemPipe pipeToUpgrade) {
         GameRegistry.addShapelessRecipe(new ItemStack(pipe), new Object[]{new ItemStack(pipeToUpgrade), new ItemStack(BuildCraftTransport.pipeWaterproof)});
     }
 }
