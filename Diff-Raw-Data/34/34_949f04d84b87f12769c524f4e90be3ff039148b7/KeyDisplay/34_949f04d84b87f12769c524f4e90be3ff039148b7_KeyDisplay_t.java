 package scenes.Main;
 
 import logic.Consts;
 import logic.Consts.Lasers;
 import logic.Engine;
 import logic.Consts.DataDir;
 
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 
 import com.badlogic.gdx.scenes.scene2d.Group;
 
 public class KeyDisplay extends Group {
 
 	Texture KeyTex;
 	Texture WheelTex;
 	
 	Sprite[] keys;
 	Sprite disc;
 	Sprite frame;
 	
	public KeyListener inputListener;
	
 	public static void loadAssets()
 	{
 		Engine.assets.load(DataDir.Ui + "frame.png", Texture.class);
 		Engine.assets.load(DataDir.Ui + "keys.png", Texture.class);
 		Engine.assets.load(DataDir.Ui + "disc.png", Texture.class);
 	}
 	
 	public KeyDisplay()
 	{
 		super();
 		
 		frame = new Sprite(Engine.assets.get(DataDir.Ui + "frame.png", Texture.class));
 		frame.setSize(240, 75);
 		
 		keys = new Sprite[Consts.Lasers.values().length];
 		KeyTex = Engine.assets.get(DataDir.Ui + "keys.png", Texture.class);
 
 		for (int i = 0, x = 88, y = 12; i < keys.length; i++, x += KeyTex.getWidth() >> 2)
 		{
 			Sprite key = new Sprite(KeyTex);
 			key.setScale(.25f, 1.0f);
 			//odd should be black
 			if ((i & 0x0001) == 0x0001) 
 			{
 				key.setU(.5f); key.setU2(.75f);
 				key.setPosition(x-key.getRegionWidth(), y + 12);
 			}
 			//even are white (that is since we're counting from 0)
 			else 
 			{
 				key.setU(0f); key.setU2(.25f);
 				key.setPosition(x-key.getRegionWidth(), y);
 			}
 			keys[i] = key;
 		}
 		disc = new Sprite(Engine.assets.get(DataDir.Ui + "disc.png", Texture.class));
 		disc.setU(0);
 		disc.setU2(1/3f);
 		disc.setScale(1/3f, 1.0f);
 		disc.setPosition(20-disc.getRegionWidth(), 14);
 		
		inputListener = new KeyListener(keys, disc);
 	}
 	
 	public void draw(SpriteBatch batch, float alpha)
 	{
 		frame.draw(batch);
 		for (Sprite k : keys)
 		{
 			k.draw(batch);
 		}
 		disc.draw(batch);
 	}
 	
 	//input listener that swaps the key's images when a key is pressed
 	private static class KeyListener extends InputListener
 	{
		private Sprite[] keys;
		private Sprite disc;
 		
 		public KeyListener(Sprite[] keys, Sprite disc)
 		{
 			this.keys = keys;
 			this.disc = disc;
 		}
 
 		@Override
 		public boolean keyDown(InputEvent event, int keycode)
 		{
 			Lasers laser = Lasers.valueOf(keycode);
 			if (laser != null)
 			{
 				int i = laser.ordinal();
 				Sprite image = this.keys[i];
 				if ((i & 0x0001) == 0x0001) 
 				{
 					image.setU(.75f); image.setU2(1.0f);
 				}
 				else
 				{
 					image.setU(.25f); image.setU2(.5f);	
 				}
 				return true;
 			}
 			if (keycode == Keys.LEFT)
 			{
 				disc.setU(1/3f);
 				disc.setU2(2/3f);
 			}
 			if (keycode == Keys.RIGHT)
 			{
 				disc.setU(2/3f);
 				disc.setU2(1f);
 			}
 			
 			return false;
 		}
 		
 		@Override
 		public boolean keyUp(InputEvent event, int keycode)
 		{
 			Lasers laser = Lasers.valueOf(keycode);
 			if (laser != null)
 			{
				int i = laser.ordinal();
				Sprite image = this.keys[i];
				if ((i & 0x0001) == 0x0001) 
				{
					image.setU(.5f); image.setU2(.75f);
				}
				else
				{
					image.setU(0f); image.setU2(.25f);	
				}
 				return true;
 			}
 			if (keycode == Keys.LEFT || keycode == Keys.RIGHT)
 			{
 				disc.setU(0f);
 				disc.setU2(1/3f);
 			}
 			return false;
 		}
 		
 	}
 }
