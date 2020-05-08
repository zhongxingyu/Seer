 
 package me.heldplayer.plugins.nei.mystcraft.client;
 
 import me.heldplayer.plugins.nei.mystcraft.Objects;
 import me.heldplayer.plugins.nei.mystcraft.PluginNEIMystcraft;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.item.crafting.IRecipe;
 import codechicken.nei.api.API;
 import codechicken.nei.api.IConfigureNEI;
 import codechicken.nei.forge.GuiContainerManager;
 import codechicken.nei.recipe.DefaultOverlayHandler;
 
 public class NEIConfig implements IConfigureNEI {
 
     public static InkMixerRecipeHandler inkMixer;
     public static Class<? extends GuiContainer> guiInkMixerClass;
 
     public static WritingDeskRecipeHandler writingDesk;
     public static Class<? extends GuiContainer> guiWritingDeskClass;
 
     public static ShapelessMystcraftRecipeHandler shapelessMystcraft;
     public static Class<? extends IRecipe> recipeLinkingbookClass;
 
     public static MystTooltipHandler tooltipHandler;
 
     @Override
     public void loadConfig() {
        if (PluginNEIMystcraft.mystcraft == null) {
            return;
        }

         Integrator.initialize(PluginNEIMystcraft.mystcraft);
 
         NEIConfig.inkMixer = new InkMixerRecipeHandler();
         API.registerRecipeHandler(NEIConfig.inkMixer);
         API.registerUsageHandler(NEIConfig.inkMixer);
 
         API.registerGuiOverlay(NEIConfig.guiInkMixerClass, "inkmixer");
         API.registerGuiOverlayHandler(NEIConfig.guiInkMixerClass, new DefaultOverlayHandler(), "inkmixer");
 
         NEIConfig.writingDesk = new WritingDeskRecipeHandler();
         API.registerRecipeHandler(NEIConfig.writingDesk);
         API.registerUsageHandler(NEIConfig.writingDesk);
 
         API.registerGuiOverlay(NEIConfig.guiWritingDeskClass, "writingdesk");
         API.registerGuiOverlayHandler(NEIConfig.guiWritingDeskClass, new DefaultOverlayHandler(), "writingdesk");
 
         NEIConfig.shapelessMystcraft = new ShapelessMystcraftRecipeHandler();
         API.registerRecipeHandler(NEIConfig.shapelessMystcraft);
         API.registerUsageHandler(NEIConfig.shapelessMystcraft);
 
         NEIConfig.tooltipHandler = new MystTooltipHandler();
         GuiContainerManager.addTooltipHandler(NEIConfig.tooltipHandler);
         GuiContainerManager.addInputHandler(NEIConfig.tooltipHandler);
     }
 
     @Override
     public String getName() {
         return Objects.MOD_NAME;
     }
 
     @Override
     public String getVersion() {
         return "@VERSION@";
     }
 
 }
