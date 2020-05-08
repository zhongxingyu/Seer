 package com.game;
 
 import java.awt.Font;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.HashMap;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 public class RenderEngine
 {
 	private static HashMap<String, Texture> list;
 	private static int currentSize;
 	
 	private static TrueTypeFont font;
 	private static TrueTypeFont customFont;
 	
 	public static void init()
 	{
 		list = new HashMap<String, Texture>();
 		initFont();
 	}
 	
 	private static void initFont()
 	{
 		font = new TrueTypeFont(new Font("Times New Roman", Font.PLAIN, 20), true);
 	}
 	
 	
 	public static void loadTexture(String location)
 	{
 		if(location.endsWith("png"))
 		{
 			loadTexture("png", location);
 		}
 		if(location.endsWith("jpg"))
 		{
 			loadTexture("jpg", location);
 		}
 	}
 	
 	public static void loadTexture(String type, String location)
 	{
 		if(list.containsKey(location))
 		{
 			return;
 		}
 		
 		try
 		{
 			list.put(location, loadTextureToApp(type, location));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private static Texture loadTextureToApp(String type, String location) throws Exception
 	{
 		return TextureLoader.getTexture(type, new FileInputStream(
 				new File("bin/res/" + location)));
 	}
 	
 	public static boolean bindTexture(String s)
 	{
 		if(!list.containsKey(s))
 		{
 			loadTexture(s);
 		}
 		
 		if(list.containsKey(s))
 		{
 			bindTexture(getTexture(s));
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private static void bindTexture(Texture tex)
 	{
 		currentSize = tex.getImageWidth();
 		tex.bind();
 	}
 	
 	public static Texture getTexture(String s)
 	{
 		return list.get(s);
 	}
 	
 	
 	public static void drawTexture(int x, int y, int width, int height,
 			int x2, int y2, int width2, int height2)
 	{
 		glEnable(GL_TEXTURE_2D);
 		
 		drawSquareWithTexture(x, y, width, height, x2, y2, width2, height2);
 		
 		glDisable(GL_TEXTURE_2D);
 	}
 	
 	public static void drawTransparentTexture(int x, int y, int width, int height,
 			int x2, int y2, int width2, int height2)
 	{
 		glEnable (GL_BLEND);
 		glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		glEnable(GL_TEXTURE_2D);
 		
 		drawSquareWithTexture(x, y, width, height, x2, y2, width2, height2);
 		
 		glDisable(GL_TEXTURE_2D);
 		glDisable(GL_BLEND);
 	}
 	
 	public static void drawSquareWithTexture(int x, int y, int width, int height,
 			int x2, int y2, int width2, int height2)
 	{
 		float tX = (x2 + 0.4f) / currentSize;
 		float tY = (y2 + 0.4f) / currentSize;
 		float tW = (width2 - 0.8f) / currentSize;
 		float tH = (height2 - 0.8f) / currentSize;
 		
 		glBegin(GL_QUADS);
 		{
 			glTexCoord2d(tX, tY);
 			glVertex2d(x, y);
 			glTexCoord2d(tX + tW, tY);
 			glVertex2d(x + width, y);
 			glTexCoord2d(tX + tW, tY + tH);
 			glVertex2d(x + width, y + height);
 			glTexCoord2d(tX, tY + tH);
 			glVertex2d(x, y + height);
 		}
 		glEnd();
 	}
 	
 	public static void drawSqare(int x, int y, int width, int height)
 	{
 		glBegin(GL_QUADS);
 		{
 			glVertex2f(x, y);
 			glVertex2f(x + width, y);
 			glVertex2f(x + width, y + height);
 			glVertex2f(x, y + height);
 		}
 		glEnd();
 	}
 	
 	public static void fillRect(int x, int y, int width, int height)
 	{
 		drawSqare(x, y, width, height);
 	}
 	
 	public static void fillTransparentRect(int x, int y, int width, int height)
 	{
 		glEnable(GL_BLEND);
 		drawSqare(x, y, width, height);
 		glDisable(GL_BLEND);
 	}
 	
 	public static TrueTypeFont getCustomFont(Font f, boolean antiAlias)
 	{
 		return new TrueTypeFont(f, antiAlias);
 	}
 	
 	public static void makeCustomFont(Font f, boolean antiAlias)
 	{
 		customFont = new TrueTypeFont(f, antiAlias);
 	}
 	
 	public static void setCustomFont(TrueTypeFont newFont)
 	{
 		customFont = newFont;
 	}
 	
 	public static void drawText(int x, int y, String text, Color c)
 	{
 		drawFont(font, x, y, text, c);
 	}
 	
 	public static void drawCustomText(int x, int y, String text, Color c)
 	{
 		if(customFont != null)
 		{
 			drawFont(customFont, x, y, text, c);
 		}
 		else
 		{
 			System.err.println("No custom font");
 		}
 	}
 	
 	private static void drawFont(TrueTypeFont font, int x, int y, String text, Color c)
 	{
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		glEnable(GL_BLEND);
 		font.drawString(x, y, text, c);
 		glDisable(GL_BLEND);
 	}
 	
 	public static int getTextLength(String text)
 	{
 		return font.getWidth(text);
 	}
 	
 	public static int getTextHeight(String text)
 	{
 		return font.getHeight(text);
 	}
 	
 	public static int getCustomTextLength(String text)
 	{
 		return customFont.getWidth(text);
 	}
 	
 	public static int getCustomTextHeight(String text)
 	{
 		return customFont.getHeight(text);
 	}
 	
 	
 	public static void setScale(float x, float y)
 	{
 		glScalef(x, y, 0);
 	}
 	
 	public static void setTranslation(float x, float y)
 	{
 		glTranslatef(x, y, 0);
 	}
 	
 	public static void setRotation(float x, float y, float z)
 	{
		glRotatef(x, 1, 0, 0);
		glRotatef(y, 0, 1, 0);
		glRotatef(z, 0, 0, 1);
 	}
 	
 	public static void push()
 	{
 		glPushMatrix();
 	}
 	
 	public static void pop()
 	{
 		glPopMatrix();
 	}
 	
 	public static void setGLColor(float red, float green, float blue, float alpha)
 	{
 		glColor4f(red, green, blue, alpha);
 	}
 
 	public static void resetColor()
 	{
 		setGLColor(1, 1, 1, 1);
 	}
 }
