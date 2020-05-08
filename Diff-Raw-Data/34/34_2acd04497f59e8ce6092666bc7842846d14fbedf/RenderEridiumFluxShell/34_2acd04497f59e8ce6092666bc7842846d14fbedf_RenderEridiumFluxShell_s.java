 package com.isocraft.client.render;
 
 import net.minecraft.client.renderer.entity.Render;
 import net.minecraft.entity.Entity;
 import net.minecraft.util.ResourceLocation;
 import net.minecraftforge.common.util.ForgeDirection;
 
 import org.lwjgl.opengl.GL11;
 
 import com.isocraft.entity.EntityEridiumFluxShell;
 import com.isocraft.lib.EntityInfo;
 
 public class RenderEridiumFluxShell extends Render {
 
 	ModelEridiumFluxShell model;
 	
 	public RenderEridiumFluxShell() {
 		model = new ModelEridiumFluxShell();
 	}
 
 	@Override
 	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
 		
 		EntityEridiumFluxShell ef = (EntityEridiumFluxShell) entity;
 		
 		GL11.glPushMatrix();
 		GL11.glDisable(GL11.GL_LIGHTING);

 		GL11.glTranslated(x, y - 0.4, z);
		

 		renderManager.renderEngine.bindTexture(this.getEntityTexture(ef));
 		float factor = (float) (1.0 / 16.0);
		GL11.glScalef((float) 0.5, (float) 0.5, (float) 1);
		
 		float transX = (float) 0;
 		float transY = (float) 0.9;
		float transZ = (float) 0;	
		GL11.glTranslatef(transX, transY, transZ);
 		
 		if (ef.dir == ForgeDirection.NORTH || ef.dir == ForgeDirection.SOUTH){
			
 			GL11.glRotatef((float) ef.angle, 0, 0, 1);
 			GL11.glTranslatef(-transX, -transY, -transZ);
 			
 			for (int i = 0; i < ef.len; ++i){
 				model.render(ef, 0f, 0f, 0f, 0f, 0f, factor);
 				if (ef.dir == ForgeDirection.NORTH){
 					GL11.glTranslatef(0f, 0f, (float) -1);
 				} else {
 					GL11.glTranslatef(0f, 0f, (float) 1);	
 				}
 			}
 
 		} else if (ef.dir == ForgeDirection.EAST || ef.dir == ForgeDirection.WEST){
			
 			GL11.glRotatef((float) ef.angle, 1, 0, 0);
 			GL11.glRotatef((float) 90, 0, 1, 0);
 			GL11.glTranslatef(-transX, -transY, -transZ);
 
 			for (int i = 0; i < ef.len; ++i){
 				model.render(ef, 0f, 0f, 0f, 0f, 0f, factor);
 				if (ef.dir == ForgeDirection.EAST){
 					GL11.glTranslatef(0f, 0f, (float) 1);
 				} else {
 					GL11.glTranslatef(0f, 0f, (float) -1);	
 				}
 			}
 			
 		} else if (ef.dir == ForgeDirection.UP || ef.dir == ForgeDirection.DOWN){
			
 			GL11.glRotatef((float) ef.angle, 0, 1, 0);
 			GL11.glRotatef((float) 90, 1, 0, 0);	
 			GL11.glTranslatef(-transX, -transY, -transZ);
 
 			for (int i = 0; i < ef.len; ++i){
 				model.render(ef, 0f, 0f, 0f, 0f, 0f, factor);
 				if (ef.dir == ForgeDirection.UP){
 					GL11.glTranslatef(0f, 0f, (float) -1);
 				} else {
 					GL11.glTranslatef(0f, 0f, (float) 1);	
 				}
 			}
 			
 		}
 		
 		GL11.glPopMatrix();
 	}
 	
 	@Override
 	protected ResourceLocation getEntityTexture(Entity var1) {
 		return EntityInfo.EridiumFLuxShell_loc;
 	}
 
 }
