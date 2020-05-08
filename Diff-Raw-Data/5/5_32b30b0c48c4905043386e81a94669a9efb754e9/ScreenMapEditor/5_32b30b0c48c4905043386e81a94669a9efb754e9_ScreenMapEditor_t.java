 package game.screen;
 
 import game.Game;
 import game.Map;
 import game.entity.Entity;
 import game.entity.EntityAmmo;
 import game.entity.EntityBox;
 import game.entity.EntityCloud;
 import game.entity.EntityExplosion;
 import game.entity.EntitySign;
 import game.entity.Player;
 import game.mapeditor.tools.Tool;
 import game.mapeditor.tools.ToolBox;
 import game.mapeditor.tools.ToolPencil;
 import game.mapeditor.tools.ToolReplace;
 import game.tile.Tile;
 import game.triggers.Trigger;
 import game.triggers.TriggerGameWin;
 import game.triggers.TriggerPlate;
 import game.utils.FileSaver;
 import game.utils.MathHelper;
 import game.utils.SpriteSheet;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 public class ScreenMapEditor extends Screen {
 
 	private static final int MENU_NONE = 0;
 	private static final int MENU_TILE = 1;
 	private static final int MENU_TOOL = 2;
 	private static final int MENU_ENTITY = 3;
 	private static final int MENU_TRIGGER = 4;
 	
 	private static final int MODE_TILE = 0;
 	private static final int MODE_ENTITY = 1;
 	private static final int MODE_TRIGGER = 2;
 
 	private ArrayList<Tool> toolRegistry = new ArrayList<Tool>();
 	private ArrayList<Entity> entityRegistry = new ArrayList<Entity>();
 	private ArrayList<Trigger> triggerRegistry = new ArrayList<Trigger>();
 	private ArrayList<Entity> entities = new ArrayList<Entity>();
 	private ArrayList<Trigger> triggers = new ArrayList<Trigger>();
 	public Tile[][] tiles = new Tile[Game.XTILES][Game.YTILES];
 	private int openMenu = 0, mapVersion = 1, mode = 0, mx, my, pmx = 0, pmy = 0, linkmode = 0;
 	private boolean isPlacingTile = true, showGrid = true, snapToGrid = true, showTriggers = false, triggerMode = false;
 
 	private Tool currentTool = null;
 	public Tile currentTile = null;
 	private Entity currentEntity = null;
 	private Trigger currentTrigger = null;
 	private Trigger sendTrigger = null;
 	private Trigger receiveTrigger = null;
 
 	public ScreenMapEditor(int width, int height, SpriteSheet sheet) {
 		super(width, height, sheet);
 		init();
 	}
 
 	public ScreenMapEditor(int width, int height, SpriteSheet sheet,
 			Tile[][] tiles, ArrayList<Entity> entities) {
 		super(width, height, sheet);
 		this.tiles = tiles;
 		this.entities = entities;
 		init();
 	}
 
 	private void init() {
 
 		for (int x = 0; x < Game.WIDTH / 32; x++) {
 			for (int y = 0; y < (Game.HEIGHT - 64) / 32; y++) {
 				tiles = Map.setTileAt(tiles, x, y, 0);
 			}
 		}
 		toolRegistry.add(new ToolPencil("Pencil", 1));
 		toolRegistry.add(new ToolReplace("Replacer", 0));
 		toolRegistry.add(new ToolBox("Rectangle", 6));
 		entityRegistry.add(new EntityBox());
 		entityRegistry.add(new EntityAmmo());
 		entityRegistry.add(new EntityExplosion());
 		entityRegistry.add(new EntitySign());
 		entityRegistry.add(new Player());
 		entityRegistry.add(new EntityCloud());
 		triggerRegistry.add(new TriggerPlate());
 		triggerRegistry.add(new TriggerGameWin());
 
 		addButton("selectTile", new Rectangle(10, 520, 64, 64));
 		addButton("selectTool", new Rectangle(104, 520, 64, 64));
 		addButton("selectEntity", new Rectangle(194, 520, 64, 64));
 		addButton("selectTriggers", new Rectangle(298, 520, 64, 64));
 		addButton("toggleGrid", new Rectangle(760, 579, 32, 32));
 		addButton("toggleMode", new Rectangle(685, 520, 33, 32));
 		addButton("toggleSnap",new Rectangle(685, 579, 32, 32));
 		addButton("toggleTriggers", new Rectangle(723, 579, 32, 32));
 		
 		addButton("save", new Rectangle(760, 515, 32, 32));
 		addButton("open", new Rectangle(760, 547, 32, 32));
 
 		currentTool = toolRegistry.get(0);
 		currentTile = Tile.tiles[1];
 		currentEntity = entityRegistry.get(0);
 		currentTrigger = triggerRegistry.get(0);
 
 	}
 
 	private void drawTileSelection(int x, int y, int texpos, String text,
 			Graphics g) {
 		g.drawImage(game.sheetTiles.getImage(texpos), x, y, 64, 64, game);
 		g.drawImage(sheet.getImage(14), x, y, 32, 32, game);
 		g.drawImage(sheet.getImage(15), x + 32, y, 32, 32, game);
 		g.drawImage(sheet.getImage(30), x, y + 32, 32, 32, game);
 		g.drawImage(sheet.getImage(31), x + 32, y + 32, 32, 32, game);
 		game.getFontRenderer().drawString(text, x + 15, y + 65, 1);
 
 	}
 
 	private void drawToolSelection(int x, int y, int texpos, String text,
 			Graphics g) {
 		g.drawImage(game.sheetUI.getImage(texpos), x, y, 64, 64, game);
 		g.drawImage(sheet.getImage(14), x, y, 32, 32, game);
 		g.drawImage(sheet.getImage(15), x + 32, y, 32, 32, game);
 		g.drawImage(sheet.getImage(30), x, y + 32, 32, 32, game);
 		g.drawImage(sheet.getImage(31), x + 32, y + 32, 32, 32, game);
 		game.getFontRenderer().drawString(text, x + 15, y + 65, 1);
 
 	}
 
 	private void drawEntitySelection(int x, int y, int texpos, String text,
 			Graphics g) {
 		g.drawImage(
 				currentEntity instanceof EntityExplosion ? game.sheetExplosions
 						.getImage(texpos) : game.sheetEntities.getImage(texpos),
 				x, y, 64, 64, game);
 		g.drawImage(sheet.getImage(14), x, y, 32, 32, game);
 		g.drawImage(sheet.getImage(15), x + 32, y, 32, 32, game);
 		g.drawImage(sheet.getImage(30), x, y + 32, 32, 32, game);
 		g.drawImage(sheet.getImage(31), x + 32, y + 32, 32, 32, game);
 		game.getFontRenderer().drawString(text, x + 15, y + 65, 1);
 
 	}
 	private void drawTriggerSelection(int x, int y, int texpos, String text,
 			Graphics g) {
 		g.drawImage(this.game.sheetTriggers.getImage(texpos), x, y, 64, 64, game);
 		g.drawImage(sheet.getImage(14), x, y, 32, 32, game);
 		g.drawImage(sheet.getImage(15), x + 32, y, 32, 32, game);
 		g.drawImage(sheet.getImage(30), x, y + 32, 32, 32, game);
 		g.drawImage(sheet.getImage(31), x + 32, y + 32, 32, 32, game);
 		game.getFontRenderer().drawString(text, x + 15, y + 65, 1);
 
 	}
 
 	private static void drawMenuBox(int x, int y, int width, int height, Graphics g) {
 		g.setColor(new Color(255, 255, 255, 155));
 		g.fillRect(x, y + height / 2, 64, height / 2);
 		g.fillRect(x, y - height / 2, width, height);
 	}
 
 	private void drawMap(Graphics g) {
 		for(int x = 0; x < tiles.length; x++)
 		{
 			for(int y = 0; y < tiles[x].length; y++)
 			{
 				
 			Tile t = tiles[x][y];
 			t.tick();
 			g.drawImage(game.sheetTiles.getImage(t.sprite), x * 32, y * 32 ,32, 32, game);
 			g.setColor(Color.white);
 			if (showGrid)
 			{
 				g.drawRect(x * 32, y * 32, 32, 32);
				if(new Rectangle(x * 32 , y* 32, 32, 32).contains(mx, my))
 				{
 					g.setColor(new Color(255,255,255,155));
					g.fillRect(x * 32 , y * 32, 32, 32);
 				}
 				}
 			}
 				
 		}
 		try{
 		for(Trigger t : triggers)
 		{
 			g.drawImage(game.sheetTriggers.getImage(t.sprite), t.x, t.y,32,32, game);
 			if(showTriggers && !(t.x == 0 && t.y == 0)){
 			g.setColor(Color.cyan);
 			g.drawLine(t.x, t.y, t.lx, t.ly);
 			}
 		}
 		}catch(Exception e){}
 		for (Entity e : entities) {
 			e.render(g);
 		}
 
 	}
 	
 	@Override
 	public void tick()
 	{		
 		try{
 		Container container = game.getParent();
 		Container previous = container;
 		while (container != null)
 		{
 		    previous = container;
 		    container = container.getParent();
 		}
 		
 		if (previous instanceof JFrame)
 		{
 		 Point p = ((JFrame)previous).getMousePosition();
 		  mx = (int) p.getX()-10;
 		  my = (int) p.getY()-10;
 		}
 		}
 		catch(NullPointerException e)
 		{
 			//Exited screen
 		}
 		
 		if(pmx != mx || pmy != my)
 		{
 			pmx = mx;
 			pmy = my;
 		}
 
 
 	}
 
 	private void drawUI(Graphics g) {
 
 		if(game.settings.getSetting("Debug").equals("ON"))
 		{
 			game.getFontRenderer().drawString("ROWS: "+tiles.length+" DEFAULT:"+Game.XTILES, 400, 520, 1);
 			game.getFontRenderer().drawString("COLS: "+tiles[0].length+" DEFAULT:"+Game.YTILES, 400, 530, 1);
 			game.getFontRenderer().drawString("SIZE: "+tiles[0].length*tiles.length+" DEFAULT:"+Game.XTILES*Game.YTILES, 400, 540, 1);
 		}
 		g.setColor(new Color(255, 255, 255, 155));
 		g.fillRect(0, 514, Game.WIDTH, 96);
 		drawTileSelection(10, 520, currentTile.sprite, "Tile", g);
 		drawToolSelection(104, 520, currentTool.getSprite(), "Tool", g);
 		drawEntitySelection(194, 520, currentEntity.sprite, "Entity", g);
 		drawTriggerSelection(298,520,currentTrigger.sprite, "Trigger", g);
 		g.drawImage(game.sheetUI.getImage(2), 760, 515, 32, 32, game);
 		g.drawImage(game.sheetUI.getImage(3), 760, 547, 32, 32, game);
 		g.drawImage(game.sheetUI.getImage(showGrid ? 5 : 4), 760, 579, 32, 32,
 				game);
 		g.drawImage(game.sheetUI.getImage(snapToGrid ? 21 : 22), 685, 579, 32, 32,
 				game);
 		g.drawImage(game.sheetUI.getImage(linkmode == 0 ? showTriggers ? 23 : 24 : 25), 723, 579, 32, 32,
 				game);
 		
 	
 		g.setColor(Color.BLACK);
 		g.drawRect(685, 520, 32, 32);
 		g.drawRect(685, 579, 32, 32);
 		g.drawRect(760, 579, 32, 32);
 		g.drawRect(723, 579, 32, 32);
 		
 		g.setColor(new Color(0, 0, 0, 135));
 		g.fillRect(685, 520, 33, 32);
 	
 		
 		
 		game.getFontRenderer().drawString(mode == MODE_ENTITY ? "ENT" : mode == MODE_TILE ? "TILE" : "TRIG", 685,
 				527, 1);
 		game.getFontRenderer().drawString("MODE", 685, 537, 1);
 		if (openMenu == MENU_TILE) {
 			drawMenuBox(10, 320, 400, 200, g);
 			int x = 0;
 			int y = 0;
 			for (Tile t : Tile.tiles) {
 				g.setColor(Color.BLACK);
 				g.drawRect(13 + (42 * x), 225 + (42 * y), 33, 34);
 				g.setColor(new Color(0, 0, 0, 135));
 				g.fillRect(13 + (42 * x), 225 + (42 * y), 33, 34);
 				g.drawImage(game.sheetTiles.getImage(t.sprite), 14 + (42 * x),
 						227 + (42 * y), 32, 32, game);
 				x++;
 				if (45 * x > 400) {
 					y++;
 					x = 0;
 				}
 			}
 		}
 		if (openMenu == MENU_TOOL) {
 			drawMenuBox(104, 450, 300, 50, g);
 			int i = 0;
 			for (Tool t : toolRegistry) {
 				g.setColor(Color.BLACK);
 				g.drawRect(112 + (42 * i), 434, 32, 32);
 				g.setColor(new Color(0, 0, 0, 135));
 				g.fillRect(112 + (42 * i), 434, 32, 32);
 				g.drawImage(game.sheetUI.getImage(t.getSprite()),
 						112 + (42 * i), 436, 32, 32, game);
 				i++;
 			}
 		}
 		if (openMenu == MENU_ENTITY) {
 			drawMenuBox(194, 450, 300, 50, g);
 			int i = 0;
 			for (Entity e : entityRegistry) {
 				g.setColor(Color.BLACK);
 				g.drawRect(212 + (42 * i), 434, 32, 32);
 				g.setColor(new Color(0, 0, 0, 135));
 				g.fillRect(212 + (42 * i), 434, 32, 32);
 				g.drawImage(
 						e instanceof EntityExplosion ? game.sheetExplosions
 								.getImage(e.sprite) : game.sheetEntities
 								.getImage(e.sprite), 212 + (42 * i), 436, 32,
 						32, game);
 				i++;
 			}
 		}
 		if (openMenu == MENU_TRIGGER) {
 			drawMenuBox(298, 450, 300, 50, g);
 			int i = 0;
 			for (Trigger t : triggerRegistry) {
 				g.setColor(Color.BLACK);
 				g.drawRect(308 + (42 * i), 434, 32, 32);
 				g.setColor(new Color(0, 0, 0, 135));
 				g.fillRect(308 + (42 * i), 434, 32, 32);
 				g.drawImage(game.sheetTriggers.getImage(t.sprite),
 						308 + (42 * i), 436, 32, 32, game);
 				i++;
 			}
 		}	
 		for (int i = 0; i < getButtons().keySet().size(); i++) {
 			Rectangle rec = (Rectangle)  getButtons().keySet().toArray()[i];
 			if (rec.contains(mx, my)) {
 				try{
 				if(openMenu == MENU_TOOL)
 					game.getFontRenderer().drawString(toolRegistry.get(Integer.parseInt(getButtons().get(rec))).getToolName(), mx, my, 1);
 				}catch(NumberFormatException e)
 				{
 					break;
 				}
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void render(Graphics g) {
 		drawMap(g);
 		drawUI(g);
 	}
 
 	public Tile getTileAt(int x, int y) {
 		return Map.getTileAt(tiles, x, y);
 	}
 
 	public void setTileAt(int x, int y, Tile tile) {
 		/*if (game.settings.getSetting("UseAdvancedTilePlacement").equals("ON")) {
 			for (int i = 0; i < tiles.keySet().size(); i++) {
 				Rectangle rec = (Rectangle) tiles.keySet().toArray()[i];
 				if (rec.contains(x, y)) {
 					tiles.put(rec, tile);
 					return;
 				}
 			}
 		} else {
 			Rectangle rec = new Rectangle(MathHelper.round(x, 16 * Game.SCALE),
 					MathHelper.round(y, 16 * Game.SCALE), 16 * Game.SCALE,
 					16 * Game.SCALE);
 
 			tiles.put(rec, tile);
 		}*/
 		tiles = Map.setTileAt(tiles, x, y, tile);
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		isPlacingTile = true;
 		super.mousePressed(arg0);
 		if(isPlacingTile && openMenu == MENU_NONE)
 		{
 			if(mode == MODE_TILE)
 			{
 				currentTool.onToolUsed(arg0.getX()/(800/25), arg0.getY()/(514/16), this);
 				return;
 			}
 			if(mode == MODE_ENTITY)
 			{
 				try {
 					Entity newent = currentEntity.getClass().newInstance();
 					if(snapToGrid)
 					{
 							newent.x = MathHelper.round(arg0.getX(), 16 * Game.SCALE);
 							newent.y = MathHelper.round(arg0.getY(), 16 * Game.SCALE);	
 					}else
 					{
 						newent.x = arg0.getX();
 						newent.y = arg0.getY();
 					}
 
 					newent.game = game;
 					entities.add(newent);
 				} catch (Exception e) {
 					Game.log("Unable to place entity - (Invalid entity) "+e.getLocalizedMessage());
 					e.printStackTrace();
 				}
 				return;
 			}
 			if(mode == MODE_TRIGGER)
 			{
 				if(arg0.getButton() == 1)
 				{
 				try {
 					Trigger newTrigger = currentTrigger.getClass().newInstance();
 					if(snapToGrid)
 					{
 							newTrigger.x = MathHelper.round(arg0.getX(), 16 * Game.SCALE);
 							newTrigger.y = MathHelper.round(arg0.getY(), 16 * Game.SCALE);	
 					}else
 					{
 						newTrigger.x = arg0.getX();
 						newTrigger.y = arg0.getY();
 					}
 					newTrigger.game = game;
 					triggers.add(newTrigger);
 				} catch (Exception e) {
 					Game.log("Unable to place trigger - (Invalid Trigger) "+e.getLocalizedMessage());
 					e.printStackTrace();
 				}
 				return;
 				}else
 				{
 					
 					if(triggerMode)
 					{
 						System.out.println("Select second trigger");
 						for(Trigger t : triggers)
 						{
 							if(new Rectangle(arg0.getX(), arg0.getY(), 32, 32).contains(t.x, t.y))
 							{
 								System.out.println("Trigger selected!");
 								System.out.println(sendTrigger);
 								System.out.println(t);
 								sendTrigger.lx = t.x;
 								sendTrigger.ly = t.y;
 								triggerMode = false;
 								break;
 							}
 							
 						}
 						System.out.println("No trigger found");
 						triggerMode = false;
 					}else
 					{
 						System.out.println("Select trigger");
 						for(Trigger t : triggers)
 						{
 							if(new Rectangle(arg0.getX(), arg0.getY(), 32, 32).contains(t.x, t.y))
 							{
 								System.out.println("Trigger selected!");
 								sendTrigger = t;
 								break;
 							}
 						}
 						triggerMode = true;
 					}
 					
 					
 				}
 			}
 		}
 
 	}
 
 	@SuppressWarnings("unused")
 	@Override
 	public void postAction(String name) {
 		isPlacingTile = false;
 		if (name.equals("save")) {
 			saveMap();
 			return;
 		}
 		if (name.equals("open")) {
 			openFile();
 			return;
 		}
 		if (name.equals("selectTile")) {
 			if (openMenu == MENU_TILE) {
 				openMenu = MENU_NONE;
 				int x = 0;
 				int y = 0;
 				for (Tile t : Tile.tiles) {
 					removeButton(new Rectangle(14 + (42 * x), 227 + (42 * y),
 							32, 32));
 					x++;
 					if (45 * x > 400) {
 						y++;
 						x = 0;
 					}
 				}
 				return;
 			} else {
 				openMenu = MENU_TILE;
 				int i = 0;
 				int x = 0;
 				int y = 0;
 				for (Tile t : Tile.tiles) {
 					addButton("" + i, new Rectangle(14 + (42 * x),
 							227 + (42 * y), 32, 32));
 					i++;
 					x++;
 					if (45 * x > 400) {
 						y++;
 						x = 0;
 					}
 				}
 
 				return;
 			}
 
 		}
 		if (name.equals("selectTool")) {
 			if (openMenu == MENU_TOOL) {
 				openMenu = MENU_NONE;
 				int i = 0;
 				for (Tool t : toolRegistry) {
 					removeButton(new Rectangle(112 + (42 * i), 434, 32, 32));
 					i++;
 				}
 			} else {
 				openMenu = MENU_TOOL;
 				int i = 0;
 				for (Tool t : toolRegistry) {
 					addButton("" + i,
 							new Rectangle(112 + (42 * i), 434, 32, 32));
 					i++;
 				}
 			}
 
 			return;
 		}
 		if (name.equals("selectEntity")) {
 			if (openMenu == MENU_ENTITY) {
 				openMenu = MENU_NONE;
 				int i = 0;
 				for (Entity e : entityRegistry) {
 					removeButton(new Rectangle(212 + (42 * i), 434, 32, 32));
 					i++;
 				}
 			} else {
 				openMenu = MENU_ENTITY;
 				int i = 0;
 				for (Entity e : entityRegistry) {
 					addButton(i + "",
 							new Rectangle(212 + (42 * i), 434, 32, 32));
 					i++;
 				}
 			}
 
 			return;
 		}
 		if (name.equals("selectTriggers")) {
 			if (openMenu == MENU_TRIGGER) {
 				openMenu = MENU_NONE;
 				int i = 0;
 				for (Trigger t : triggerRegistry) {
 					removeButton(new Rectangle(112 + (42 * i), 434, 32, 32));
 					i++;
 				}
 			} else {
 				openMenu = MENU_TRIGGER;
 				int i = 0;
 				for (Trigger t : triggerRegistry) {
 					addButton("" + i,
 							new Rectangle(112 + (42 * i), 434, 32, 32));
 					i++;
 				}
 			}
 
 			return;
 		}
 		if (name.equals("toggleGrid")) {
 			showGrid = !showGrid;
 			return;
 		}
 		if(name.equals("toggleSnap"))
 		{
 			snapToGrid = !snapToGrid;
 			return;
 		}
 		if(name.equals("toggleTriggers"))
 		{
 			showTriggers = !showTriggers;
 			return;
 		}
 		if(name.equals("toggleMode")) {
 			mode++;
 			if(mode > MODE_TRIGGER)
 				mode = MODE_TILE;
 			return;
 		}
 		if (openMenu == MENU_TOOL) {
 			currentTool = toolRegistry.get(Integer.parseInt(name));
 			openMenu = MENU_NONE;
 			return;
 		}
 		if (openMenu == MENU_TILE) {
 			int x = 0, y = 0;
 			currentTile = Tile.tiles[Integer.parseInt(name)];
 			for (Tile t : Tile.tiles) {
 				removeButton(new Rectangle(14 + (42 * x), 227 + (42 * y),
 						32, 32));
 				x++;
 				if (45 * x > 400) {
 					y++;
 					x = 0;
 				}
 			}
 			openMenu = MENU_NONE;
 		}
 		if (openMenu == MENU_ENTITY) {
 			currentEntity = entityRegistry.get(Integer.parseInt(name));
 			for (Entity e : entityRegistry) {
 				removeButton(new Rectangle(212 + (42 * i), 434, 32, 32));
 				i++;
 			}
 			openMenu = MENU_NONE;
 		}
 		if(openMenu == MENU_TRIGGER)
 		{
 			currentEntity = entityRegistry.get(Integer.parseInt(name));
 			for (Trigger t : triggerRegistry) {
 				removeButton(new Rectangle(112 + (42 * i), 434, 32, 32));
 				i++;
 			}
 			openMenu = MENU_NONE;
 		}
 
 	}
 
 	private void openFile() {
 		JFileChooser fileChooser = new JFileChooser(new File(
 				FileSaver.getCleanPath() + "/maps/"));
 		fileChooser.setAcceptAllFileFilterUsed(false);
 		FileNameExtensionFilter filter = new FileNameExtensionFilter(
 				"Substrate Map File", "smf");
 		fileChooser.setFileFilter(filter);
 
 		if (fileChooser.showOpenDialog(new JFrame("Open")) == JFileChooser.APPROVE_OPTION) {
 			File file = fileChooser.getSelectedFile();
 			Map loadedMap = FileSaver.loadMapFile(file.getAbsolutePath());
 			this.tiles = loadedMap.tiles;
 			try {
 				this.entities = FileSaver.serialToEntity(loadedMap.entities,
 						game);
 			} catch (ClassNotFoundException | InstantiationException
 					| IllegalAccessException e) {
 				Game.log("Could not parse entities... Invalid entity");
 				e.printStackTrace();
 			}
 			for(Trigger t : loadedMap.triggers)
 			{
 				System.out.println("Trigger: "+t+" at "+t.x+","+t.y);
 			}
 			this.triggers = loadedMap.triggers;
 			this.mapVersion = Integer.parseInt(loadedMap.version);
 			
 		}
 
 	}
 
 	private void saveMap() {
 		JFileChooser fileChooser = new JFileChooser(new File(
 				FileSaver.getCleanPath() + "/maps/"));
 		fileChooser.setAcceptAllFileFilterUsed(false);
 		FileNameExtensionFilter filter = new FileNameExtensionFilter(
 				"Substrate Map File", "smf");
 		fileChooser.setFileFilter(filter);
 		if (fileChooser.showSaveDialog(new JFrame("Save")) == JFileChooser.APPROVE_OPTION) {
 			File file = fileChooser.getSelectedFile();
 
 			Map savedMap = new Map(file.getName().replace("_", " ")
 					.replace(".smf", ""), "NYI", (mapVersion + 1) + "", tiles,
 					FileSaver.entityToSerial(entities), triggers);
 			savedMap.isLevel = true;
 			savedMap.isLocked = false;
 
 			FileSaver.saveMapFile(savedMap,!file.getAbsolutePath().contains(".smf") ? file.getAbsolutePath() + ".smf" : file.getAbsolutePath());
 
 		}
 	}
 
 }
