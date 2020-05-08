 package com.vloxlands.game;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.gluPerspective;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.Color;
 
 import com.vloxlands.scene.Scene;
 import com.vloxlands.util.FontAssistant;
 import com.vloxlands.util.GUIAssistant;
 import com.vloxlands.util.RenderAssistant;
 
 public class Game
 {
 	public static Game currentGame;
 
 	float pos = 0;
 	int i = 0;
 
 	long start = 0;
 	int frames = 21;
 	boolean showFPS = false;
 
 	Scene scene;
 
 	public void gameLoop()
 	{
 		if (start == 0) start = System.currentTimeMillis();
 
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_DEPTH_TEST);
 
 		gluPerspective((float) 30, Display.getWidth() / (float) Display.getHeight(), 0.001f, 100);
 		glPushMatrix();
 		{
 			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
 			glTranslated(0, 0, pos);
 
 			if (Keyboard.isKeyDown(Keyboard.KEY_UP))
 			{
 				pos += 0.1f;
 			}
 
 			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
 			{
 				pos -= 0.1f;
 			}
 			glColor3f(0.5f, 0.5f, 1.0f);
 
 			glPointSize(10);
 			glColor4d(1, 1, 1, 1);
 
 			glBegin(GL_POINTS);
 			{
 				glVertex3f(0, 0, 0);
 			}
 			glEnd();
 		}
 		glPopMatrix();
 
 		GUIAssistant.handleMouse();
 
 		if (scene != null) scene.update();
 
 		RenderAssistant.set2DRenderMode(true);
 
 		GUIAssistant.renderComponents();
 
		if (Keyboard.isKeyDown(Keyboard.KEY_F4)) showFPS = !showFPS;
 		if (showFPS) RenderAssistant.renderText(0, 0, getFPS() + "", Color.white, FontAssistant.GAMEFONT.deriveFont(30f));
 
 		RenderAssistant.set2DRenderMode(false);
 
 		Display.update();
 		Display.sync(60);
 
 		frames++;
 
 	}
 
 	public static void initGame()
 	{
 		currentGame = new Game();
 	}
 
 	public void setScene(Scene s)
 	{
 		GUIAssistant.clearComponents();
 
 		scene = s;
 		scene.init();
 	}
 
 	public int getFPS()
 	{
 		return Math.round(frames / ((System.currentTimeMillis() - start) / 1000f));
 	}
 
 	public static void initGLSettings()
 	{
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_DEPTH_TEST);
 		glEnable(GL_ALPHA_TEST);
 	}
 }
