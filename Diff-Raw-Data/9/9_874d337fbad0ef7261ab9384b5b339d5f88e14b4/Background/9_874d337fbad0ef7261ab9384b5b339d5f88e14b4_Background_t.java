 package core;
 
 import org.lwjgl.util.glu.GLU;
 import org.newdawn.slick.opengl.Texture;
 import helpers.*;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import java.io.FileInputStream;
 
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Owner
  * Date: 24/04/13
  * Time: 8:50 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Background {
     public Texture texture;
 
     public Background() {
        texture = TextureHelper.LoadTexture("png", "images/B.png");
 //        try {
 //            texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("images/B.png"));
 //        } catch (Exception e) {
 //            e.printStackTrace();
 //            System.exit(0);
 //        }
     }
 
     public void createBackground() {
 
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
         glLoadIdentity();
         glPushMatrix();
 
         glPushAttrib(GL_ENABLE_BIT);
         glEnable(GL_TEXTURE_2D);
         glDisable(GL_DEPTH_TEST);
         glDisable(GL_LIGHTING);
         glDisable(GL_BLEND);
 
         glColor4f(1,1,1,1.0f);
 
         // Render the front quad
         glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
         glBegin(GL_QUADS);
         glTexCoord2f(0, 0); glVertex3f(-1.0f, 1.0f, -1.0f);
         glTexCoord2f(1, 0); glVertex3f(-1.0f, 1.0f, 1.0f);
         glTexCoord2f(1, 1); glVertex3f(1.0f, 1.0f, 1.0f);
         glTexCoord2f(0, 1); glVertex3f(1.0f, 1.0f, -1.0f);
         glEnd();
 
         // Render the left quad
         glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
         glBegin(GL_QUADS);
         glTexCoord2f(0, 0); glVertex3f(-1.0f, -1.0f, 1.0f);
         glTexCoord2f(1, 0); glVertex3f(-1.0f, 1.0f, 1.0f);
         glTexCoord2f(1, 1); glVertex3f(-1.0f, 1.0f, -1.0f);
         glTexCoord2f(0, 1); glVertex3f(-1.0f, -1.0f, -1.0f);
         glEnd();
 
         // Render the back quad
         glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
         glBegin(GL_QUADS);
         glTexCoord2f(0, 0); glVertex3f(-1.0f, -1.0f, -1.0f);
         glTexCoord2f(1, 0); glVertex3f(-1.0f, -1.0f, 1.0f);
         glTexCoord2f(1, 1); glVertex3f(1.0f, -1.0f, 1.0f);
         glTexCoord2f(0, 1); glVertex3f(1.0f, -1.0f, -1.0f);
 
         glEnd();
 
         // Render the right quad
         glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
         glBegin(GL_QUADS);
         glTexCoord2f(0, 0); glVertex3f(1.0f, -1.0f, 1.0f);
         glTexCoord2f(1, 0); glVertex3f(1.0f, 1.0f, 1.0f);
         glTexCoord2f(1, 1); glVertex3f(1.0f, 1.0f, -1.0f);
         glTexCoord2f(0, 1); glVertex3f(1.0f, -1.0f, -1.0f);
         glEnd();
 
         // Render the top quad
         glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
         glBegin(GL_QUADS);
         glTexCoord2f(0, 1); glVertex3f(1.0f, -1.0f, 1.0f);
         glTexCoord2f(0, 0); glVertex3f(1.0f, 1.0f, 1.0f);
         glTexCoord2f(1, 0); glVertex3f(-1.0f, 1.0f, 1.0f);
         glTexCoord2f(1, 1); glVertex3f(-1.0f, -1.0f, 1.0f);
         glEnd();
 
         // Render the bottom quad
         glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
         glBegin(GL_QUADS);
         glTexCoord2f(0, 0); glVertex3f(1.0f, -1.0f, -1.0f);
         glTexCoord2f(0, 1); glVertex3f(1.0f, 1.0f, -1.0f);
         glTexCoord2f(1, 1); glVertex3f(-1.0f, 1.0f, -1.0f);
         glTexCoord2f(1, 0); glVertex3f(-1.0f, -1.0f, -1.0f);
         glEnd();
 
         // Restore enable bits and matrix
         glPopAttrib();
         glPopMatrix();
     }
 }
