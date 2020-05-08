 package ruby.bamboo.render.magatama;
 
 import java.nio.FloatBuffer;
 import java.util.Random;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.Sphere;
 
 import ruby.bamboo.entity.magatama.EntityGravityHole;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.ActiveRenderInfo;
 import net.minecraft.client.renderer.GLAllocation;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.client.renderer.entity.Render;
 import net.minecraft.entity.Entity;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityEndPortal;
 import net.minecraft.util.ResourceLocation;
 
 public class RenderGravityHole extends Render{
 	private static final ResourceLocation TEX_END_SKY = new ResourceLocation("textures/environment/end_sky.png");
     private static final ResourceLocation TEX_END_PORTAL = new ResourceLocation("textures/entity/end_portal.png");
 	private static final Random RAND = new Random(31100L);
 	private Sphere sphere = new Sphere();
     FloatBuffer floatBuffer = GLAllocation.createDirectFloatBuffer(16);
     public RenderGravityHole(){
     	sphere.setTextureFlag(true);
     }
 	@Override
 	public void doRender(Entity entity, double d0, double d1, double d2,
 			float f, float f1) {
 		this.render((EntityGravityHole) entity, d0, d1, d2);
 	}
 	private void render (EntityGravityHole entity, double posX, double posY, double posZ){
 	
 	    float viewerPosX = (float)this.renderManager.viewerPosX;
 	    float viewerPosY = (float)this.renderManager.viewerPosY;
 	    float viewerPosZ = (float)this.renderManager.viewerPosZ;
 	    GL11.glDisable(GL11.GL_LIGHTING);
 	    RAND.setSeed(31100L);
	    float f4 = 26F;
 	
 	    for (int i = 0; i < 16; ++i)
 	    {
 	        GL11.glPushMatrix();
 	        float f5 = (float)(16 - i);
 	        float scale = 0.0625F;
 	        float color = 1.0F / (f5 + 1.0F);
 	
 	        if (i == 0)
 	        {
 	            this.func_110776_a(TEX_END_SKY);
 	            color = 0.1F;
 	            f5 = 65.0F;
 	            scale = 0.125F;
 	            GL11.glEnable(GL11.GL_BLEND);
 	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 	            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
 	    		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
 	        }
 	
 	        if (i == 1)
 	        {
 	            this.func_110776_a(TEX_END_PORTAL);
 	            GL11.glEnable(GL11.GL_BLEND);
 	            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
 	            scale = 0.5F;
 	        }
 
 	        float f8 = (float)(-(posY + (double)f4));
 	        float f9 = f8 + ActiveRenderInfo.objectY;
 	        float f10 = f8 + f5 + ActiveRenderInfo.objectY;
 	        float f11 = f9 / f10;
	        f11 += (float)(posY + (double)f4);
 	        GL11.glTranslatef(viewerPosX, f11, viewerPosZ);
 	        GL11.glTexGeni(GL11.GL_S, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_OBJECT_LINEAR);
 	        GL11.glTexGeni(GL11.GL_T, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_OBJECT_LINEAR);
 	        GL11.glTexGeni(GL11.GL_R, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_OBJECT_LINEAR);
 	        GL11.glTexGeni(GL11.GL_Q, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);
 	        GL11.glTexGen(GL11.GL_S, GL11.GL_OBJECT_PLANE, this.func_76907_a(1.0F, 0.0F, 0.0F, 0.0F));
 	        GL11.glTexGen(GL11.GL_T, GL11.GL_OBJECT_PLANE, this.func_76907_a(0.0F, 0.0F, 1.0F, 0.0F));
 	        GL11.glTexGen(GL11.GL_R, GL11.GL_OBJECT_PLANE, this.func_76907_a(0.0F, 0.0F, 0.0F, 1.0F));
 	        GL11.glTexGen(GL11.GL_Q, GL11.GL_EYE_PLANE, this.func_76907_a(0.0F, 1.0F, 0.0F, 0.0F));
 	        GL11.glEnable(GL11.GL_TEXTURE_GEN_S);
 	        GL11.glEnable(GL11.GL_TEXTURE_GEN_T);
 	        GL11.glEnable(GL11.GL_TEXTURE_GEN_R);
 	        GL11.glEnable(GL11.GL_TEXTURE_GEN_Q);
 	        GL11.glPopMatrix();
 	        GL11.glMatrixMode(GL11.GL_TEXTURE);
 	        GL11.glPushMatrix();
 	        GL11.glLoadIdentity();
 	        GL11.glTranslatef(0.0F, (float)(Minecraft.getSystemTime() % 700000L) / 700000.0F, 0.0F);
 	        scale=entity.getScale();
 	        GL11.glScalef(scale, scale, scale);
 	        GL11.glTranslatef(0.5F, 0.5F, 0.0F);
 	        GL11.glRotatef((float)(i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
 	        GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
 	        GL11.glTranslatef(-viewerPosX, -viewerPosZ, -viewerPosY);
 	        f9 = f8 + ActiveRenderInfo.objectY;
 	        GL11.glTranslatef(ActiveRenderInfo.objectX * f5 / f9, ActiveRenderInfo.objectZ * f5 / f9, -viewerPosY);
 
 	        GL11.glPopMatrix();
 	        GL11.glMatrixMode(GL11.GL_MODELVIEW);
 	        f11 = RAND.nextFloat() * 0.5F + 0.1F;
 	        float f12 = RAND.nextFloat() * 0.5F + 0.4F;
 	        float f13 = RAND.nextFloat() * 0.5F + 0.5F;
 	        if (i == 0)
 	        {
 	            f13 = 1.0F;
 	            f12 = 1.0F;
 	            f11 = 1.0F;
 	        }
 	        GL11.glPushMatrix();
 	        GL11.glColor4f(f11 * color, f12 * color, f13 * color, 1.0F);
 	        GL11.glScalef(1.0F,1.0F,1.0F);
 	        GL11.glTranslatef((float)posX, (float)posY+(entity.height/2), (float)posZ);
 			sphere.draw(entity.getScale(), 16, 16);
 			GL11.glPopMatrix();
 	    }
 	
 	    GL11.glDisable(GL11.GL_BLEND);
 	    GL11.glDisable(GL11.GL_TEXTURE_GEN_S);
 	    GL11.glDisable(GL11.GL_TEXTURE_GEN_T);
 	    GL11.glDisable(GL11.GL_TEXTURE_GEN_R);
 	    GL11.glDisable(GL11.GL_TEXTURE_GEN_Q);
 	    GL11.glEnable(GL11.GL_LIGHTING);
 	}
 	
 	private FloatBuffer func_76907_a(float par1, float par2, float par3, float par4)
 	{
 	    this.floatBuffer.clear();
 	    this.floatBuffer.put(par1).put(par2).put(par3).put(par4);
 	    this.floatBuffer.flip();
 	    return this.floatBuffer;
 	}
 	
 	@Override
 	protected ResourceLocation func_110775_a(Entity entity) {
 		return null;
 	}
 
 }
