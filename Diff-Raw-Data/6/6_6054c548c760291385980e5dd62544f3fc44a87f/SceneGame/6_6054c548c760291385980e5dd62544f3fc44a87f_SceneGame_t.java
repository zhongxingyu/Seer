 package com.vloxlands.scene;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.awt.Font;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 
 import com.vloxlands.Vloxlands;
 import com.vloxlands.game.Game;
 import com.vloxlands.settings.Tr;
 import com.vloxlands.ui.Container;
 import com.vloxlands.ui.IGuiElement;
 import com.vloxlands.ui.IGuiEvent;
 import com.vloxlands.ui.Label;
 import com.vloxlands.ui.TextButton;
 import com.vloxlands.util.RenderAssistant;
 
 public class SceneGame extends Scene
 {
 	boolean paused;
 	
 	@Override
 	public void init()
 	{
 		Game.currentGame.resetCamera();
		worldActive = true;
 		glClearColor(0.5f, 0.8f, 0.85f, 1);
 		paused = false;
 		
 		Label l = new Label(0, 100, Display.getWidth(), 60, Tr._("pause"));
 		l.font = l.font.deriveFont(Font.BOLD, 60f);
 		content.add(l);
 		
 		Container c = new Container(Display.getWidth() / 2 - TextButton.WIDTH / 2 - 40, Display.getHeight() / 2 - 120, TextButton.WIDTH + 80, 120 + TextButton.HEIGHT * 3);
 		
 		TextButton b = new TextButton(TextButton.WIDTH / 2 + 40, 50, Tr._("mainmenu"));
 		b.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.currentMap = null;
 				Game.currentGame.setScene(new SceneMainMenu());
 			}
 		});
 		c.add(b);
 		
 		b = new TextButton(TextButton.WIDTH / 2 + 40, 120, Tr._("settings"));
 		b.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.currentGame.addScene(new SceneSettings());
 			}
 		});
 		c.add(b);
 		
 		b = new TextButton(TextButton.WIDTH / 2 + 40, 190, Tr._("quitGame"));
 		b.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Vloxlands.exit();
 			}
 		});
 		c.add(b);
 		
 		content.add(c);
 		
 		unlockScene();
 	}
 	
 	@Override
 	public void onTick()
 	{
 		super.onTick();
 		
 		uiActive = paused;
 	}
 	
 	@Override
 	public void handleMouseWorld(int x, int y, int flag)
 	{
		if (paused) return;
		
 		if (Mouse.isButtonDown(1))
 		{
 			if (!Mouse.isGrabbed()) Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
 			
 			Game.currentGame.mouseGrabbed = true;
 			if (Mouse.isButtonDown(1)) Game.currentGame.rotateCamera();
 			else Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
 		}
 		else Game.currentGame.mouseGrabbed = false;
 	}
 	
 	@Override
 	public void handleKeyboard(int key, char chr, boolean down)
 	{
 		if (key == Keyboard.KEY_ESCAPE && down)
 		{
 			paused = !paused;
 		}
 	}
 	
 	@Override
 	public void render()
 	{
 		if (!paused) return;
 		
 		glEnable(GL_BLEND);
 		glColor4f(IGuiElement.gray.x, IGuiElement.gray.y, IGuiElement.gray.z, IGuiElement.gray.w);
 		glBindTexture(GL_TEXTURE_2D, 0);
 		RenderAssistant.renderRect(0, 0, Display.getWidth(), Display.getHeight());
 		
 		super.render();
 		
 		glDisable(GL_BLEND);
 	}
 }
