 package holo.essentrika.states;
 
 import holo.essentrika.EssentrikaMain;
 import holo.essentrika.grid.IGenerator;
 import holo.essentrika.grid.IPowerReciever;
 import holo.essentrika.map.World;
 import holo.essentrika.modules.IModule;
 import holo.essentrika.modules.ModuleCreator;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.FontUtils;
 
 public class GameState extends BasicGameState
 {
 	private final int stateID;
 	StateBasedGame game;
 	public static final int tileValue = 20;
 	public static final float differenceChange = 0.7F;
 	public static final int startingMoney = 0;
 	public static final int startingRequirement = 0;
 
 	public static double timer = 10000;
 	public static int requiredPoweredTiles;
 	public static int money;
	public static float requiredDifference = 1.0F;
 	public static double totalTime;
 	public static int totalMoney;
 	
 	IModule selectedModule = null;
 	ArrayList<Integer[]> selectedModuleUpgrades = new ArrayList<Integer[]>(10);
 	int[] selectedModuleCoords = new int[]{0, 0};
 	
 	public static World world;
 	int[] cameraCoords = new int[]{0, 0};
 	
 	int screenWidth;
 	int screenHeight;
 	
 	Music music;
 	
 	int poweredTiles;
 	
 	
 	public GameState(int stateID, StateBasedGame game, boolean load)
 	{
 		this.stateID = stateID;
 		this.game = game;
 		GameState.world = new World(load);
 		if (!load)
 		{
 			money = startingMoney;
 			requiredPoweredTiles = startingRequirement;
 			totalTime = 0;
 			totalMoney = 0;
 		}
 		updateAllModules(true);
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame game)throws SlickException 
 	{
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics g)throws SlickException 
 	{
 		g.scale(EssentrikaMain.defaultWidth / gc.getWidth(), EssentrikaMain.defaultHeight / gc.getHeight());
 		g.setBackground(Color.black);
 		g.clear();
 		g.setColor(Color.white);
 //		g.drawRect(0, 0 + gc.getHeight() / 16, gc.getWidth(), gc.getHeight() - gc.getHeight() / 16 - gc.getHeight() / 8);
 		
 		int moduleWidth = gc.getWidth() / 64;
 		int moduleHeight = (gc.getHeight() - gc.getHeight() / 16 - gc.getHeight() / 8) / 64;
 		
 		for(int i = 0; i < gc.getWidth() / 64; ++i)
 		{
 			float x = i * 64;
 			for (int j = 0; j < (gc.getHeight() - gc.getHeight() / 16 - gc.getHeight() / 8) / 64; ++j)
 			{
 				float y = gc.getHeight() / 16 + j * 64;
 				g.drawRect(x, y, 64, 64);
 				Shape box = new Rectangle(x, y, 64, 64);
 				Image sprite = world.getModuleAt(i - moduleWidth / 2 + cameraCoords[0], j - moduleHeight / 2 + cameraCoords[1]).getIcon(world, i - moduleWidth / 2 + cameraCoords[0], j - moduleHeight / 2 + cameraCoords[1]);
 				g.drawImage(sprite, x, y);
 				g.draw(box);
 			}
 		}
 
 		Font font = gc.getDefaultFont();
 		
 		FontUtils.drawLeft(font, "Funds: " + money, gc.getWidth() / 8, 0);
 		FontUtils.drawLeft(font, "Time Until Next Update: " + (String.valueOf(timer / 1000)).substring(0, 3), gc.getWidth() / 8, font.getLineHeight());
 		
 		String req = (int)(requiredPoweredTiles + requiredDifference) + " sections must be powered";
 		String cur = poweredTiles + " sections have been powered";
 		
 		FontUtils.drawCenter(font, req, gc.getWidth() / 2 - font.getWidth(req) / 2, 0, font.getWidth(req));
 		FontUtils.drawCenter(font, cur, gc.getWidth() / 2 - font.getWidth(cur) / 2, font.getLineHeight(), font.getWidth(cur));
 		
 		int x;
 		int y;
 		
 		if (selectedModule != null)
 		{
 			Image sprite = selectedModule.getIcon(world, selectedModuleCoords[0], selectedModuleCoords[1]);
 			g.drawImage(sprite, gc.getWidth() / 50, gc.getHeight() - gc.getHeight() / 9);
 			
 			x = gc.getWidth() / 50 + 70;
 			y = gc.getHeight() - gc.getHeight() / 9;
 			int textY = gc.getHeight() - gc.getHeight() / 9;
 			
 			String title = selectedModule.getModuleName() + " (" + selectedModuleCoords[0] + "," + -selectedModuleCoords[1] + ")";
 			FontUtils.drawLeft(font, title, x, textY - font.getLineHeight());
 			
 			int textWidth = font.getWidth(title);
 			
 			int lineY = textY;
 			if(selectedModule instanceof IGenerator)
 			{
 				IGenerator module = (IGenerator)selectedModule;
 				String max = "Max Power: " + module.powerGenerated();
 				String used = "Current Output: " + module.currentPower();
 				FontUtils.drawLeft(font, max, x, lineY);
 				lineY += font.getLineHeight();
 				FontUtils.drawLeft(font, used, x, lineY);
 				textWidth = Math.max(textWidth, Math.max(font.getWidth(max), font.getWidth(used)));
 			}
 			
 			if (selectedModule instanceof IPowerReciever)
 			{
 				IPowerReciever mod = (IPowerReciever)selectedModule;
 				lineY += font.getLineHeight();
 				String required = "Power Needed: " + mod.requiredPower();
 				String used = "Current Power: " + mod.currentPowerLevel();
 				String connected = "Connected to Grid: " + mod.isConnectedToPowerGrid(world, selectedModuleCoords[0], selectedModuleCoords[1]);
 				FontUtils.drawLeft(font, required, x, lineY);
 				lineY += font.getLineHeight();
 				FontUtils.drawLeft(font, used, x, lineY);
 				lineY += font.getLineHeight();
 //				FontUtils.drawLeft(font, connected, x, lineY);
 				textWidth = Math.max(textWidth, Math.max(font.getWidth(required), Math.max(font.getWidth(used), font.getWidth(connected))));
 			}
 			
 			x += 5 + textWidth;
 			g.drawLine(x, gc.getHeight() - gc.getHeight() / 8, x, gc.getHeight());
 			
 			x += 5;
 
 			selectedModuleUpgrades.clear();
 			
 			if (selectedModule.getUpgrades() != null)
 			{
 				FontUtils.drawLeft(font, "Upgrades", x, textY - font.getLineHeight());
 				x += 5 + font.getWidth("Upgrades");
 				for(Integer moduleID: selectedModule.getUpgrades())
 				{
 					IModule module = ModuleCreator.createModule(moduleID);
 					sprite = module.getIcon(world, selectedModuleCoords[0], selectedModuleCoords[1]);
 					g.drawImage(sprite, x, y);
 					selectedModuleUpgrades.add(new Integer[]{x, y, x + sprite.getWidth(), y + sprite.getHeight()});
 					x += 70;
 					FontUtils.drawLeft(font, module.getModuleName(), x, textY);
 					if(selectedModule.getKeyFromUpgradeID(module.getID()) >= 0)
 						FontUtils.drawLeft(font, "(" + Input.getKeyName(selectedModule.getKeyFromUpgradeID(module.getID())) + ")", x, textY - font.getLineHeight());
 					lineY = textY + font.getLineHeight();
 					FontUtils.drawLeft(font, "$" + selectedModule.getUpgradeCost(module), x, lineY);
 					textWidth = Math.max(font.getWidth(module.getModuleName()), font.getWidth("$" + selectedModule.getUpgradeCost(module)));
 					
 					if(module instanceof IGenerator)
 					{
 						IGenerator mod = (IGenerator)module;
 						lineY += font.getLineHeight();
 						String max = "Max Power: " + mod.powerGenerated();
 						FontUtils.drawLeft(font, max, x, lineY);
 						textWidth = Math.max(textWidth, font.getWidth(max));
 					}
 					
 					x += 5 + textWidth;
 				}
 			}
 		}
 		
 		String locationTitle = "Camera Location";
 		String location = cameraCoords[0] + "," + -cameraCoords[1];
 		
 		
 		y = gc.getHeight() / 32 - font.getLineHeight();
 		x = gc.getWidth() - Math.max(font.getWidth(location), font.getWidth(locationTitle));
 		FontUtils.drawLeft(font, locationTitle, x, y);
 		y = gc.getHeight() / 32;
 		FontUtils.drawLeft(font, location, x, y);
 		
 		
 		if (getBoxFromMouseCoords(gc.getInput().getMouseX(), gc.getInput().getMouseY()) != null)
 		{
 			int[] coord = getBoxFromMouseCoords(gc.getInput().getMouseX(), gc.getInput().getMouseY());
 			String mLocationTitle = "Cursor Location";
 			String mLocation = coord[0] + "," + -coord[1];
 
 			y = gc.getHeight() / 32 - font.getLineHeight();
 			x -= Math.max(font.getWidth(mLocationTitle), font.getWidth(mLocation));
 			FontUtils.drawLeft(font, mLocationTitle, x, y);
 			y = gc.getHeight() / 32;
 			FontUtils.drawLeft(font, mLocation, x, y);
 		}
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)throws SlickException 
 	{
 		screenWidth = gc.getWidth();
 		screenHeight = gc.getHeight();
 		
 		timer -= delta;
 		totalTime += delta;
 		
 		if (timer <= 0)
 		{
 			updateAllModules(false);
 			timer += 10000;
 		}
 		
 	}
 	
 	public void updateAllModules(boolean first)
 	{
 		ArrayList<Long> keys = new ArrayList<Long>();
 		for(Long coord : world.getKeySet())
 		{
 			keys.add(coord);
 		}
 		
 		poweredTiles = 0;
 		if(!first)
 		{
 			requiredPoweredTiles += fastfloor(requiredDifference);
 			requiredDifference += differenceChange;
 		}
 		
 		for (Long coord: keys)
 		{
 			int x = fastfloor(coord >> 24);
 			int y = fastfloor(coord - (x << 24)) - (1 << 24) / 2;
 			if (x < 0)
 			{
 				y = -((1 << 24) - y);
 				++x;
 			}
 			IModule module = world.getModuleAt(coord);
 			
 			module.update(world, x, y);
 			
 			if (module instanceof IPowerReciever)
 			{
 				IPowerReciever mod = (IPowerReciever)module;
 				if (mod.requiredPower() - mod.currentPowerLevel() <= 0)
 				{
 					poweredTiles += mod.getLandValue();
 					
 					if(!first)
 					{
 						money += tileValue * mod.getLandValue();
 						totalMoney += tileValue * mod.getLandValue();
 					}
 				}
 			}
 		}
 		
 		if(poweredTiles < requiredPoweredTiles)
 		{
 			game.enterState(EssentrikaMain.GAMEOVERSTATEID);
 		}
 	}
 	
 	public int[] getBoxFromMouseCoords(int x, int y)
 	{
 		int moduleWidth = screenWidth / 64;
 		int moduleHeight = (screenHeight - screenHeight / 16 - screenHeight / 8) / 64;
 		
 		if (y < (screenHeight - screenHeight / 16 - screenHeight / 8) && y > screenHeight / 16)
 		{
 			int i = x / 64;
 			int j = (y - screenHeight / 16) / 64;
 			
 			return new int[]{i - moduleWidth / 2 + cameraCoords[0], j - moduleHeight / 2 + cameraCoords[1]};
 		}
 		return null;
 	}
     
     public void mouseClicked(int b, int x, int y, int clickCount) 
     {
     	if (getBoxFromMouseCoords(x, y) != null)
     	{
     		selectedModuleCoords = getBoxFromMouseCoords(x, y);
     		selectedModule = world.getModuleAt(selectedModuleCoords[0], selectedModuleCoords[1]);
     	}
     	else
     	{
     		int i = 0;
     		for(Integer[] coord : selectedModuleUpgrades)
     		{
     			int boxX = coord[0];
     			int boxY = coord[1];
     			if (x > boxX && x < boxX + 64
     					&& y > boxY && y < boxY + 64)
     			{
     				int moduleID = selectedModule.getUpgrades().get(i);
     				try
 					{
 						IModule module = ModuleCreator.createModule(moduleID);
 						int cost = selectedModule.getUpgradeCost(module);
 						if (cost <= money)
 						{
 							world.setModule(module, selectedModuleCoords[0], selectedModuleCoords[1]);
 							selectedModule = module;
 							money -= cost;
 							return;
 						}
 					} catch (SlickException e)
 					{
 						e.printStackTrace();
 					}
     			}
     			++i;
     		}
     	}
     }
 	
 	@Override
     public void keyPressed(int key, char c) 
     {
 		if (key == Input.KEY_LEFT)
 			--cameraCoords[0];
 		else if (key == Input.KEY_RIGHT)
 			++cameraCoords[0];
 		else if (key == Input.KEY_DOWN)
 			++cameraCoords[1];
 		else if (key == Input.KEY_UP)
 			--cameraCoords[1];
 		else if (key == Input.KEY_ESCAPE)
 		{
 			world.save();
 			game.enterState(EssentrikaMain.MENUSTATEID);
 		}
 		
 		if(selectedModule != null)
 		{
 			int id = selectedModule.getUpgradeFromKey(key);
 			if(id >= 0)
 			{
 				try
 				{
 					IModule module = ModuleCreator.createModule(id);
 					int cost = selectedModule.getUpgradeCost(module);
 					if (cost <= money)
 					{
 						world.setModule(module, selectedModuleCoords[0], selectedModuleCoords[1]);
 						selectedModule = module;
 						money -= cost;
 					}
 				} catch (SlickException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
     }
 
 	@Override
 	public int getID() 
 	{
 		return this.stateID;
 	}
 	
 	private int fastfloor(double x) 
 	{
 		return x > 0 ? (int) x : (int) x - 1;
 	}
 }
