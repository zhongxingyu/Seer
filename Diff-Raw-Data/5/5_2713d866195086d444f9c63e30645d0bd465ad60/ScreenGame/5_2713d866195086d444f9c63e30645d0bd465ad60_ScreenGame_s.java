 package game.screen;
 
 import game.Controls;
 import game.Game;
 import game.Map;
 import game.entity.Entity;
 import game.entity.Player;
 import game.tile.Tile;
 import game.triggers.Trigger;
 import game.triggers.TriggerPlate;
 import game.utils.FileSaver;
 import game.utils.MathHelper;
 import game.utils.SpriteSheet;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ScreenGame extends Screen
 {
 
 	public Player player;
 	public ArrayList<Entity> entities = new ArrayList<Entity>();
 	public ArrayList<Trigger> triggers = new ArrayList<Trigger>();
 	public Map map;
 
 	int velx = 0, vely = 0, w, h, px, py;
 	public int xScroll = 0;
 	public int yScroll = 0;
 	int mapSize = 0;
 	public boolean drawDebugBoxes = false;
 
 	public ScreenGame(int width, int height, SpriteSheet sheet, Map mapfile)
 	{
 		super(width, height, sheet);
 		this.w = width;
 		this.h = height;
 		initMap(mapfile);
 
 	}
 
 	public void initMap(Map mapfile)
 	{
 		
 		this.map = mapfile;
 		try
 		{
 			entities = FileSaver.serialToEntity(mapfile.entities, game);
 			Game.log("Entity array size: " + entities.size());
 		} catch (ClassNotFoundException | InstantiationException
 				| IllegalAccessException e)
 		{
 			Game.log("Sad trumpet noise");
 			e.printStackTrace();
 		}
 		for (Entity ent : entities)
 		{
 			if (ent instanceof Player)
 			{		
 				System.out.println("Found player instance at " + ent.x + ","
 						+ ent.y + ".");
 				px = ent.x;
 				py = ent.y;
 				ent.forRemoval = true;
 				break;
 			}
 		}
 		 triggers = mapfile.triggers;
 		 
 		 for(Trigger t: triggers)
 		 {
 			 if(t.sg == null)
 				 t.sg = this;	 
 		 }
 	}
 
 	public void spawnEntity(Entity entity)
 	{
 		entities.add(entity);
 	}
 
 	@Override
 	public void tick()
 	{
 		if (player == null)
 		{
 			player = new Player(this);
 			player.setPos(px, py);
 			entities.add(player);
 		}
 
 		player.tryMoveEntity(velx, vely);
 	}
 
 	@Override
 	public void init(Game game)
 	{
 		Game.log("Initializing");
 		drawDebugBoxes = game.settings.getSetting("ExtendedDebug").equals("ON");
 		super.init(game);
 	}
 
 	public Tile getTileAt(int x, int y)
 	{
 		return Map.getTileAt(map, x /(800/25),y /(514/16));
 	}
 	
 	/**
 	 * Converts from entity coordinates to tile coordinates.
 	 * @param x
 	 * @param y
 	 * @param tile
 	 */
 	public void setTileAtEnt(int x, int y, int tile)
 	{
 		map = Map.setTileAt(map, x/(800/25), y/(514/16), tile);
 	}
 
 	public void setTileAt(int x, int y, int tile)
 	{
 		map = Map.setTileAt(map, x, y, tile);
 	}
 
 	public Entity getEntityInBox(Rectangle rec)
 	{
 		for (Entity ent : entities)
 		{
			if (rec.contains(new Point(ent.x, ent.y)))
 			{
 				return ent;
 			}
 		}
 		return null;
 	}
 
 	public ArrayList<Entity> getEntitiesInBox(Rectangle rec)
 	{
 		ArrayList<Entity> ents = new ArrayList<Entity>();
 		for (Entity ent : entities)
 		{
			if (rec.contains(new Rectangle(ent.x, ent.y, 32, 32)))
 			{
 				ents.add(ent);
 			}
 		}
 		return ents;
 	}
 
 	@Override
 	public void render(final Graphics g) {
 		
 		if (player == null)
 			return;
 		for(int x = 0; x < map.tiles.length; x++)
 		{
 			for(int y = 0; y < map.tiles[x].length; y++)
 			{
 				Tile t = map.tiles[x][y];
 				t.tick();
 				g.drawImage(game.sheetTiles.getImage(t.sprite), (x * 32) - xScroll, (y * 32) - yScroll, 32, 32, game);
 				if(drawDebugBoxes)
 				{
 					g.setColor(t.isPassable() ? Color.GREEN : Color.RED);
 					g.drawRect(x * 32, y * 32, 32, 32);
 				}
 
 			}
 		}
 		if(triggers != null)
 		{
 			for(Trigger t : triggers)
 			{
 				t.tick();
 				if(t.drawnInPlay)
 				g.drawImage(game.sheetTriggers.getImage(t.sprite), t.x, t.y, 32, 32, game);
 			}
 		}
 		for (int i = 0; i < entities.size(); i++ ) {
 			Entity e = entities.get(i);
 			if (e.game == null) {
 				e.game = game;
 				Game.log("Entity " + e
 						+ " game instance was null. Game instance is now "
 						+ e.game);
 			}
 			if (e.forRemoval)
 				entities.remove(i);
 			e.tick();
 			e.render(g);
 			g.setColor(Color.RED);
 			if(drawDebugBoxes)
 			g.drawRect(e.x , e.y, 32, 32);
 		}
 		
 		if (game.settings.getSetting("Debug").equals("ON") && player != null) {
 			game.getFontRenderer().drawString(
 					"DX:" + velx + " DY:" + vely + " SX:" + xScroll + " SY:"
 							+ yScroll + " WX:" + Game.WIDTH + " WY:"
 							+ Game.HEIGHT, 260, 0, 1);
 			game.getFontRenderer().drawString(
 					"X:" + player.x + " Y:" + player.y + " ROT:"
 							+ player.getOrientation() + " HP:"
 							+ player.getHealth() + " AMM:" + player.getAmmo()
 							+ " TIMERS:GUN:" + player.ammocooldown+" SW:"+w+" SH:"+h, 260, 10, 1);
 		}
 		g.setColor(new Color(155, 155, 155, 142));
 		g.fillRect(0, game.getHeight()-88, game.getWidth(), game.getHeight());
 		if (player.getHealth() > 0) {
 			g.drawImage(game.sheetUI.getImage(33), 16, game.getHeight() - 64, 32, 32, game);
 			for (int i = 0; i < player.getHealth() - 1; i++) {
 				g.drawImage(game.sheetUI.getImage(34), 32 + (32 * i), game.getHeight() - 64,32, 32, game);
 			}
 
 			g.drawImage(game.sheetUI.getImage(35), (32 * player.getHealth()),game.getHeight() - 64, 32, 32, game);
 		}
 		if (player.getAmmo() > 0) {
 			g.drawImage(game.sheetUI.getImage(17), 16, game.getHeight() - 32, 32, 32, game);
 			for (int i = 0; i < player.getAmmo() - 1; i++) {
 				g.drawImage(game.sheetUI.getImage(18), 32 + (32 * i), game.getHeight() - 32,32, 32, game);
 			}
 
 			g.drawImage(game.sheetUI.getImage(19), (32 * player.getAmmo()),game.getHeight() - 32, 32, 32, game);
 			if (player.ammocooldown != 0) {
 				g.drawImage(game.sheetUI.getImage(16),32 + (32 * player.getAmmo()), game.getHeight() - 32, 32, 32, game);
 				game.getFontRenderer().drawString("" + player.ammocooldown / 60,32 + (32 * player.getAmmo()), game.getHeight() - 30, 1);
 			}
 		}
 		
 		if(player.hurtTimer > 0)
 		{
 			g.setColor(new Color(255,0,0,40+player.hurtTimer));
 			g.fillRect(0, 0, 800, 600);
 		}
 		if(map.hint != null)
 		game.getFontRenderer().drawString(map.hint, map.tx, map.ty, map.ts);
 		if(drawDebugBoxes)
 		{
 			game.getFontRenderer().drawString("PlYR", player.x, player.y, 1);
 			game.getFontRenderer().drawString("HEALTH: "+player.getHealth()+"\nAMMO: "+player.getAmmo(), 200, 540, 1);
 		}
 		
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e)
 	{
 		player.keyPressed(e);
 		super.keyPressed(e);
 		if (e.getKeyCode() == game.controls.getKey(Controls.CONTROL_UP))
 		{
 			vely = -1;
 		}
 		if (e.getKeyCode() == game.controls.getKey(Controls.CONTROL_DOWN))
 		{
 			vely = 1;
 		}
 		if (e.getKeyCode() == game.controls.getKey(Controls.CONTROL_LEFT))
 		{
 			velx = -1;
 		}
 		if (e.getKeyCode() == game.controls.getKey(Controls.CONTROL_RIGHT))
 		{
 			velx = 1;
 
 		}
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e)
 	{
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e)
 	{
 
 		player.keyReleased(e);
 		if (e.getKeyCode() == game.controls.getKey(Controls.CONTROL_UP))
 		{
 			vely = 0;
 		}
 		if (e.getKeyCode() == game.controls.getKey(Controls.CONTROL_DOWN))
 		{
 			vely = 0;
 		}
 		if (e.getKeyCode() == game.controls.getKey(Controls.CONTROL_LEFT))
 		{
 			velx = 0;
 		}
 		if (e.getKeyCode() == game.controls.getKey(Controls.CONTROL_RIGHT))
 		{
 			velx = 0;
 		}
 	}
 
 }
