 package mods.firstspring.dimchest;
 
 import net.minecraft.client.model.ModelChest;
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.tileentity.TileEntity;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 public class TileEntityDimChestRenderer extends TileEntitySpecialRenderer
 {
 	/** The Dim Chest Chest's model. */
 	private ModelChest theDimChestModel = new ModelChest();
 
 	/**
 	 * Helps to render Dim Chest.
 	 */
 	public void renderDimChest(TileEntityDimChest tileentitydimchest, double par2, double par4, double par6, float par8)
 	{
 		int orient = 0;
 		int metadata = tileentitydimchest.blockMetadata;
 		if (tileentitydimchest.func_70309_m())
 		{
 			metadata = tileentitydimchest.getBlockMetadata();
 			orient = tileentitydimchest.orient;
 		}
 		if(metadata == 0)
			this.bindTextureByName("/mods/firstspring/dimchest/textures/blocks/LinkChest.png");
 		if(metadata == 1)
			this.bindTextureByName("/mods/firstspring/dimchest/textures/blocks/DimChest.png");
 		GL11.glPushMatrix();
 		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		GL11.glTranslatef((float)par2, (float)par4 + 1.0F, (float)par6 + 1.0F);
 		GL11.glScalef(1.0F, -1.0F, -1.0F);
 		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
 		short var10 = 0;
 
 		if (orient == 2)
 		{
 			var10 = 180;
 		}
 
 		if (orient == 3)
 		{
 			var10 = 0;
 		}
 
 		if (orient == 4)
 		{
 			var10 = 90;
 		}
 
 		if (orient == 5)
 		{
 			var10 = -90;
 		}
 
 		GL11.glRotatef((float)var10, 0.0F, 1.0F, 0.0F);
 		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
 		float var11 = tileentitydimchest.prevLidAngle + (tileentitydimchest.lidAngle - tileentitydimchest.prevLidAngle) * par8;
 		var11 = 1.0F - var11;
 		var11 = 1.0F - var11 * var11 * var11;
 		this.theDimChestModel.chestLid.rotateAngleX = -(var11 * (float)Math.PI / 2.0F);
 		this.theDimChestModel.renderAll();
 		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
 		GL11.glPopMatrix();
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 	}
 
 	public void renderTileEntityAt(TileEntity par1TileEntity, double par2, double par4, double par6, float par8)
 	{
 		this.renderDimChest((TileEntityDimChest)par1TileEntity, par2, par4, par6, par8);
 	}
 }
