 package industrialscience.modules.research.frontend;
 
 import industrialscience.ISModel;
 import industrialscience.TextureGenerator;
 import industrialscience.modules.ResearchModule;
 import industrialscience.modules.research.backend.Research;
 import industrialscience.modules.research.backend.ResearchObject;
 import industrialscience.modules.research.backend.Researchstep;
 import industrialscience.modules.research.frontend.TileEntities.CopierTile;
 import industrialscience.modules.research.frontend.TileEntities.ResearchDeskTile;
 import industrialscience.modules.research.frontend.models.ResearchCopierModel;
 import industrialscience.modules.research.frontend.models.ResearchDeskModel;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public enum ResearchBlockType {
     COPIER(
             "Research Copier",
             "copier.png",
             ResearchCopierModel.class,
             CopierTile.class,
             new TextureGenerator() {
                 private Icon bottom;
                 private Icon top;
                 private Icon side;
 
                 @Override
                 public Icon getIcon(int i) {
                     switch (i) {
                         case 0:
                             return bottom;
                         case 1:
                             return top;
                         default:
                             return side;
                     }
 
                 }
 
                 @Override
                 public void registerIcons(IconRegister par1IconRegister) {
                     side = par1IconRegister
                             .registerIcon("industrialscience:vannila_researchtable_sides");
                     bottom = par1IconRegister
                             .registerIcon("industrialscience:vannila_researchtable_bottom");
                    top = par1IconRegister.registerIcon("planks_jungle");
 
                 }
 
             },
             new Research(
                     "Copying",
                     new String[] { "Writing", "Wooden things" },
                     Research.RESEARCH_CATEGORY + "_blocks",
                     null,
                     new Researchstep[] {
                             new Researchstep(
                                     0,
                                     new ResearchObject(
                                             new ItemStack(Item.paper)),
                                     "You have looked at your Researchbook and thought, how cool it would be to share and save your knowledge. You want to think more about this idea"),
                             new Researchstep(
                                     1,
                                     new ResearchObject(new ItemStack(
                                             ResearchModule.researchNote)),
                                     "Yout think the best way to copy researches would be just read your written results and write them down again. So simple.") },
                     null, null)), RESEARCHDESK("Research Desk",
             "researchdesk.png", ResearchDeskModel.class,
             ResearchDeskTile.class, new TextureGenerator() {
                 private Icon bottom;
                 private Icon top;
                 private Icon side;
 
                 @Override
                 public Icon getIcon(int i) {
                     switch (i) {
                         case 0:
                             return bottom;
                         case 1:
                             return top;
                         default:
                             return side;
                     }
 
                 }
 
                 @Override
                 public void registerIcons(IconRegister par1IconRegister) {
                     side = par1IconRegister
                             .registerIcon("industrialscience:vannila_researchtable_sides");
                     bottom = par1IconRegister
                             .registerIcon("industrialscience:vannila_researchtable_bottom");
                    top = par1IconRegister.registerIcon("planks_oak");
 
                 }
 
             }, null);
     private Class<? extends ISModel> model;
     private String friendlyname;
     private String modelfile;
     public TextureGenerator texturegen;
 
     public String getReadableName() {
         return friendlyname;
     }
 
     public String getModelfile() {
         return modelfile;
     }
 
     public Class<? extends TileEntity> getTileentity() {
         return tileentity;
     }
 
     public Research getResearch() {
         return research;
     }
 
     public Class<? extends TileEntity> tileentity;
     private Research research;
 
     private ResearchBlockType(String name, String modelfile,
             Class<? extends ISModel> model,
             Class<? extends TileEntity> tileentity,
             TextureGenerator texturegen, Research research) {
         friendlyname = name;
         this.modelfile = modelfile;
         this.tileentity = tileentity;
         this.research = research;
         this.texturegen = texturegen;
         this.model = model;
     }
 
     public static TileEntity getEntity(int metadata) {
         try {
             TileEntity te = values()[metadata].getTileentity().newInstance();
             return te;
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public static Icon getIcon(int i, int metadata) {
         return values()[metadata].texturegen.getIcon(i);
     }
 
     public static void registerIcons(IconRegister par1IconRegister) {
         for (ResearchBlockType researchBlockType : ResearchBlockType.values()) {
             researchBlockType.texturegen.registerIcons(par1IconRegister);
         }
 
     }
 
     public Class<? extends ISModel> getModel() {
         return model;
     }
 
     public static void register(Block researchBlock, String Prefix) {
         GameRegistry.registerBlock(researchBlock, ItemResearchBlock.class,
                 Prefix + researchBlock.getUnlocalizedName());
         for (ResearchBlockType typ : ResearchBlockType.values()) {
             GameRegistry.registerTileEntityWithAlternatives(
                     typ.getTileentity(), Prefix + typ.name(), typ.name());
             LanguageRegistry.addName(
                     new ItemStack(researchBlock, 1, typ.ordinal()),
                     typ.getReadableName());
         }
         
     }
 }
