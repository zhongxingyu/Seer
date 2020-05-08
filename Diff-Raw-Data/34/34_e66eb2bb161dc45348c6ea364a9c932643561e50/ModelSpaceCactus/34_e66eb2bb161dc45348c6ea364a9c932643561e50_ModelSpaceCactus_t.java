 package mods.anotherWorld.common.dimension.tyteonblocks.render;
 
 import net.minecraft.client.model.ModelBase;
 import net.minecraft.client.model.ModelRenderer;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SideOnly(Side.CLIENT)
 public class ModelSpaceCactus extends ModelBase
 {
     public ModelRenderer Base;
     public ModelRenderer L_Top;
     public ModelRenderer R_Top;
     public ModelRenderer B_Top;
     public ModelRenderer F_Top;
     
     public ModelSpaceCactus()
     {
     	
         this.Base = (new ModelRenderer(this, 0, 0)).setTextureSize(64, 64);
         this.Base.addBox(0F, 0F, 0F, 14, 24, 14);
         this.Base.rotationPointX = 1.0F;
         this.Base.rotationPointY = -8.0F;
         this.Base.rotationPointZ = 1.0F;
         
         
         this.F_Top = (new ModelRenderer(this, 0, 39)).setTextureSize(64, 64);
         this.F_Top.addBox(0F, 0F, 0F, 12, 8, 1);
         this.F_Top.rotationPointX = 2.0F;
         this.F_Top.rotationPointY = -16.0F;
         this.F_Top.rotationPointZ = 1.0F;
         
         this.B_Top = (new ModelRenderer(this, 0, 39)).setTextureSize(64, 64);
         this.B_Top.addBox(0F, 0F, 0F, 12, 8, 1);
         this.B_Top.rotationPointX = 2.0F;
         this.B_Top.rotationPointY = -16.0F;
         this.B_Top.rotationPointZ = 14.0F;
        
        this.R_Top = (new ModelRenderer(this, 27, 39)).setTextureSize(64, 64);
        this.R_Top.addBox(0F, 0F, 0F, 1, 8, 10);
        this.R_Top.rotationPointX = 2.0F;
        this.R_Top.rotationPointY = -16.0F;
        this.R_Top.rotationPointZ = 3.0F;
        
        this.L_Top = (new ModelRenderer(this, 27, 39)).setTextureSize(64, 64);
        this.L_Top.addBox(0F, 0F, 0F, 1, 8, 10);
        this.L_Top.rotationPointX = 14.0F;
        this.L_Top.rotationPointY = -16.0F;
        this.L_Top.rotationPointZ = 3.0F;

         	
     }
 
     /**
      * This method renders out all parts of the chest model.
      */
     public void renderAll()
     {
         this.Base.render(0.0625F);
         this.F_Top.render(0.0625F);
         this.B_Top.render(0.0625F);
        this.L_Top.render(0.0625F);
        this.R_Top.render(0.0625F);
     }
 }
