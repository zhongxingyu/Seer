 package uk.co.homletmoo.hm001;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.*;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.opengl.PNGImageData;
 import org.newdawn.slick.opengl.TextureLoader;
 
 public class Render {
 	
 	private boolean fog = true;
 	private int fogToggleTimer = 30;
 	
 	private float camX = 0, camY = 0, camZ = 0;
 	double camRotX = 0, camRotY = 0;
 	
 	public void init()
 	{
 		glMatrixMode(GL_PROJECTION);
 			glLoadIdentity();
 			glOrtho(0, Attr.DISPLAY_WIDTH, 0, Attr.DISPLAY_HEIGHT, 1, -1);
 		glMatrixMode(GL_MODELVIEW);
 		
 		try {
 			Tex.icon = TextureLoader.getTexture("PNG", new FileInputStream("src/res/favicon.png"));
 			Tex.logo = TextureLoader.getTexture("PNG", new FileInputStream("src/res/HMXLV2.png"));
 			Display.setIcon(assignIcon("src/res/favicon.png"));
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		glEnable(GL_DEPTH_TEST);
 	    	glDepthFunc(GL_LESS);
 		
 		glMatrixMode(GL_PROJECTION);
 			glLoadIdentity();
 			gluPerspective(75, Attr.DISPLAY_WIDTH / Attr.DISPLAY_HEIGHT, 1, Attr.SIZE);
 		glMatrixMode(GL_MODELVIEW);
 
		glCullFace(GL_BACK);
 		
 		glEnable(GL_BLEND);
 			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		
 		glEnable(GL_FOG);
             FloatBuffer fogColor = BufferUtils.createFloatBuffer(4);
             fogColor.put(0).put(0).put(0).put(1).flip();
 
             glFogi(GL_FOG_MODE, GL_EXP);
             glFog(GL_FOG_COLOR, fogColor);
             glFogf(GL_FOG_DENSITY, 0.000175f);
             glHint(GL_FOG_HINT, GL_DONT_CARE);
             glClearColor(0, 0, 0, 1);
         
         glEnable(GL_POINT_SMOOTH);
 		glPointSize(2);
 		
 		Prim.initLists();
 	}
 	
 	public void render(Vector<Renderable> stack, Input input)
 	{
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		
 		if(fogToggleTimer++ > 30 && Keyboard.isKeyDown(Keyboard.KEY_F))
 		{
 			if(fog)
 			{
 				glDisable(GL_FOG);
 				glDisable(GL_POINT_SMOOTH);
 				glPointSize(1);
 				fog = false;
 			} else
 			{
 				glEnable(GL_FOG);
 				glEnable(GL_POINT_SMOOTH);
 				glPointSize(2);
 				fog = true;
 			}
 
 			fogToggleTimer = 0;
 		}
 		
 		camRotX -= input.mouseDX * Attr.SENS_X;
 		double radRotX = degToRad(camRotX);
 		double[] vecRotXZ = radToVec(radRotX);
 		
 		camRotY += input.mouseDY * Attr.SENS_Y;
 		if(camRotY < -90)
 			camRotY = -90;
 		else if(camRotY > 90)
 			camRotY = 90;
 		double radRotY = degToRad(camRotY);
 		double[] vecRotY = radToVec(radRotY);
 		
 		if(input.keys[Attr.P_C_JUMP])
 			camY += Attr.P_SPEED;
 		if(input.keys[Attr.P_C_CROUCH])
 			camY -= Attr.P_SPEED;
 		if(input.keys[Attr.P_C_FORWARD])
 		{
 			camX += vecRotXZ[0] * Attr.P_SPEED;
 			camZ += vecRotXZ[1] * Attr.P_SPEED;
 		}
 		if(input.keys[Attr.P_C_BACKWARD])
 		{
 			camX -= vecRotXZ[0] * Attr.P_SPEED;
 			camZ -= vecRotXZ[1] * Attr.P_SPEED;
 		}
 		if(input.keys[Attr.P_C_LEFT])
 		{
 			camX += vecRotXZ[1] * Attr.P_SPEED;
 			camZ -= vecRotXZ[0] * Attr.P_SPEED;
 		}
 		if(input.keys[Attr.P_C_RIGHT])
 		{
 			camX -= vecRotXZ[1] * Attr.P_SPEED;
 			camZ += vecRotXZ[0] * Attr.P_SPEED;
 		}
 		
 		Iterator<Renderable> i = stack.iterator();
 		while(i.hasNext())
 		{
 			Renderable r = i.next();
 			glLoadIdentity();
 			gluLookAt(Attr.HALFSIZE, Attr.HALFSIZE, Attr.HALFSIZE, (float) (Attr.HALFSIZE + vecRotXZ[0] / 2), (float) (Attr.HALFSIZE + vecRotY[0]), (float) (Attr.HALFSIZE + vecRotXZ[1] / 2), 0, 1, 0);
 			glTranslatef(r.x - camX, r.y - camY, r.z - camZ);
 			glScalef(r.width, r.height, r.depth);
 			glColor3f(r.r, r.g, r.b);
 			if(r.tex != null)
 			{
 				glEnable(GL_TEXTURE_2D);
 				r.tex.bind();
 			} else
 				glDisable(GL_TEXTURE_2D);
 			
 			switch(r.type)
 			{
 				case CUBE:
 					glCallList(Prim.listCube);
 				break;
 				
 				case POINT:
 					glCallList(Prim.listPoint);
 				break;
 			}
 		}
 	}
 	
 	private ByteBuffer[] assignIcon(String path)
 	{
 		try {
 			ByteBuffer buffIcon = new PNGImageData().loadImage(new FileInputStream(path));
 			ByteBuffer[] buffIcons = new ByteBuffer[] { buffIcon };
 			return buffIcons;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			System.exit(0);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		return null;
 	}
 	
 	private double degToRad(double degrees)
 	{
 		double radians = degrees * (Math.PI / 180);
 		return radians;
 	}
 	
 	private double[] radToVec(double radians)
 	{
 		double[] vector = new double[] {Math.sin(radians), Math.cos(radians)};
 		return vector;
 	}
 }
