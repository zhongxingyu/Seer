 package tests;
 
 import graphics.Color;
 import graphics.ShaderEffect;
 import graphics.SpriteBatch;
 import graphics.Texture2D;
 import graphics.TextureFont;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import math.Vector2;
 
 import org.jdom.JDOMException;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import utils.Camera2D;
 import utils.Rectangle;
 import content.ContentManager;
 import content.TextureFontLoader;
 
 public class FontTest {
 	
 	private static final String font = "textureFont.xml";
 	
 	
 
 	private static SpriteBatch graphics;
 	private static Camera2D camera;
 	private static DisplayMode mode;
 	private static TextureFont textureFont;
 	
 	public static void main(String[] args) throws LWJGLException, FileNotFoundException, IOException, JDOMException {
 		Display.create();
 		mode = Display.getDisplayMode();	
 		
 		graphics = new SpriteBatch(new Rectangle(0, 0, mode.getWidth(), mode.getHeight()));
 		textureFont = ContentManager.loadFont("arialb20.xml");
 		
 		float angle = 0.01f;
 		while(!Display.isCloseRequested()) {
 			graphics.clearScreen(new Color(Color.Teal, 0.0f));
 			angle += 0.01f;
 			
 			graphics.begin(null);
 			
 			
 			graphics.drawString(textureFont, "Normal text yay how awesome!", new Vector2(0,0), Color.Green);
 			graphics.drawString(textureFont, "This is a,\nmultiline! hehe string", new Vector2(0,100), Color.Gold);
 			graphics.drawString(textureFont, "This line has an orgin!", new Vector2(0,200), Color.Red, new Vector2(50, 0));
 			graphics.drawString(textureFont, "This line is scaled!", new Vector2(0,300), Color.Cyan, Vector2.Zero, new Vector2(2f,2f));
 			graphics.drawString(textureFont, "This line is also scaled!", new Vector2(340,0), Color.Yellow, Vector2.Zero, new Vector2(4f,1.5f),0.0f, false);
 			graphics.drawString(textureFont, "This line is mirrored :O", new Vector2(340,100), Color.Yellow, Vector2.Zero, Vector2.One, 0.0f, true);
 			graphics.drawString(textureFont, "This line has a rectangle around it :P", new Vector2(300,200), Color.Brown, Vector2.Zero, new Vector2(1f,1f));
 			graphics.drawString(textureFont, "This line is rotated", new Vector2(400,260), Color.Crimson, Vector2.Zero, new Vector2(1f,1f), angle, false);
			Vector2 strVec = textureFont.meassureString("This line is rotated and offseted!");
 			
 			
			graphics.drawString(textureFont, "This line is rotated\n and offseted!", new Vector2(800,260), Color.ForestGreen, new Vector2(strVec.X / 2,5), new Vector2(1f,1f), angle, false);
 			strVec = textureFont.meassureString("This line is rotated scaled and centered!");
 			
 			
 			graphics.drawString(textureFont, "This line is rotated scaled and centered!", new Vector2(700,260), Color.Crimson, new Vector2(strVec.X / 2 ,5), new Vector2(2f,1f), angle, false);
 			
 			
 			
 			graphics.draw(Texture2D.getPixel(), new Rectangle(300,200, (int)(strVec.X + 0.5f), (int)strVec.Y), new Color(Color.Orange, 0.5f), null);
 			
 			
 			graphics.end();
 	
 			Display.update();
 			Display.sync(60);
 		}
 		
 		Display.destroy();
 		
 	}
 	
 }
