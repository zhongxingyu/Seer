 package fart.dungeoncrawler.mapeditor;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.JPanel;
 import javax.swing.event.MouseInputListener;
 import javax.xml.XMLConstants;
 
 import fart.dungeoncrawler.Tilemap;
 
 import nu.xom.*;
 
 /**
  * This is the main editor panel which handles userinput and saving/loading of maps. 
  * @author Timo
  *
  */
 @SuppressWarnings("serial")
 public class MEPanel extends JPanel implements MouseInputListener
 {
 	private MapEditor me;
 	private ImageManager imgmgr;
 	
 	private char walls[][];
 	private int objects[][];
 	private Point highlight = new Point(0,0);
 	
 	private HashMap<Point, MapToInfo> mapToInfoList;
 	
 	public MEPanel(MapEditor mapeditor, ImageManager imgmgr)
 	{
 		super();
 		if((this.me = mapeditor) == null)
 		{
 			System.err.println("MapEditor Object in MEPanel is null");
 			System.exit(1);
 		}
 		
 		if((this.imgmgr = imgmgr) == null)
 		{
 			System.err.println("ImageManager Object in MEPanel is null");
 			System.exit(1);
 		}
 		
 		this.setPreferredSize(new Dimension(me.WIDTH*me.TILE_SIZE, me.HEIGHT*me.TILE_SIZE));
 
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 
 		init();
 	}
 	
 	/**
 	 * Initializes a new map.
 	 */
 	public void init()
 	{
 		mapToInfoList = new HashMap<Point, MapToInfo>();
 		
 		walls = new char[me.WIDTH][me.HEIGHT];
 		objects = new int[me.WIDTH][me.HEIGHT];
 		
 		for(int j=0; j<me.HEIGHT; j++)
 		{
 			for(int i=0; i<me.WIDTH; i++)
 			{
 				// Borders = '#' (wall)
 				if(i==0 || j==0 || i == me.WIDTH-1 || j == me.HEIGHT-1)
 					walls[i][j] = '#';
 				else
 					walls[i][j] = ' ';
 				
 				objects[i][j] = -1;
 			}
 		}
 		
 		repaint();
 	}
 	
 	/**
 	 * Draws everything inside the panel.
 	 */
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		Graphics2D g2d = (Graphics2D) g;
 		
 		// Draw walls and grass
 		for(int j=0; j<me.HEIGHT; j++)
 		{
 			for(int i=0; i<me.WIDTH; i++)
 			{
 				if(walls[i][j] == '#')
 					g2d.drawImage(imgmgr.getImage("wall"), null, i*me.TILE_SIZE, j*me.TILE_SIZE);
 				else
 					g2d.drawImage(imgmgr.getImage("grass"), null, i*me.TILE_SIZE, j*me.TILE_SIZE);
 			
 				if(objects[i][j] != -1)
 					g2d.drawImage(imgmgr.getImage(String.valueOf(objects[i][j])), null, i*me.TILE_SIZE, j*me.TILE_SIZE);
 			}
 		}
 		
 		g2d.setColor(Color.GRAY);
 		
 		// Draw vertical lines
 		for(int i=1; i<me.WIDTH; i++)
 			g2d.drawLine(i*me.TILE_SIZE-1, 0, i*me.TILE_SIZE-1, me.HEIGHT*me.TILE_SIZE);
 		
 		// Draw horizontal lines
 		for(int i=1; i<me.HEIGHT; i++)
 			g2d.drawLine(0, i*me.TILE_SIZE-1, me.WIDTH*me.TILE_SIZE, i*me.TILE_SIZE-1);
 	
 		// Draw highlighting rectangle
 		g2d.setColor(Color.RED);
 		g2d.drawRect((int)highlight.getX()-1, (int)highlight.getY()-1, me.TILE_SIZE, me.TILE_SIZE);
 	}
 	
 	/**
 	 * Saves the current map to disc in XML-format.
 	 * @param outputFile name of the output file
 	 * @param mapName name of the map
 	 */
 	public void saveMap(String outputFile, String mapName)
 	{
 		// Descriptions
 		Element desc_width;
 		Element desc_height;
 		Element desc_passable;
 		Element desc_damage;
 		Element desc_spritesheet;
 		Element desc_isRanged;
 		Element desc_aggroRange;
 		Element desc_attackRange;
 		Element desc_type;
 		Element desc_level;
 		Element desc_element;
 		
 		
 		ArrayList<Element> desc_list = new ArrayList<Element>();
 		Element description;
 		
 		// Portal Description
 		description = new Element("object");
 		description.addAttribute(new Attribute("name", "portal"));
 		description.addAttribute(new Attribute("id", "0"));
 		desc_spritesheet = new Element("spritesheet");
 		desc_spritesheet.appendChild("res/tp.png");
 		description.appendChild(desc_spritesheet);
 		desc_width = new Element("colWidth");
 		desc_width.appendChild("32");
 		description.appendChild(desc_width);
 		desc_height = new Element("colHeight");
 		desc_height.appendChild("32");
 		description.appendChild(desc_height);
 		desc_passable = new Element("passable");
 		desc_passable.appendChild("1");
 		description.appendChild(desc_passable);
 		desc_list.add(description);
 		
 		// Goal Description
 		description = new Element("object");
 		description.addAttribute(new Attribute("name", "goal"));
 		description.addAttribute(new Attribute("id", "1"));
 		desc_spritesheet = new Element("spritesheet");
 		desc_spritesheet.appendChild("res/goal.png");
 		description.appendChild(desc_spritesheet);
 		desc_width = new Element("colWidth");
 		desc_width.appendChild("32");
 		description.appendChild(desc_width);
 		desc_height = new Element("colHeight");
 		desc_height.appendChild("32");
 		description.appendChild(desc_height);
 		desc_passable = new Element("passable");
 		desc_passable.appendChild("1");
 		description.appendChild(desc_passable);
 		desc_list.add(description);
 		
 		// Trap(fire) Description
 		description = new Element("object");
 		description.addAttribute(new Attribute("name", "fire"));
 		description.addAttribute(new Attribute("id", "2"));
 		desc_spritesheet = new Element("spritesheet");
 		desc_spritesheet.appendChild("res/trap.png");
 		description.appendChild(desc_spritesheet);
 		desc_width = new Element("colWeight");
 		desc_width.appendChild("32");
 		description.appendChild(desc_width);
 		desc_height = new Element("colHeight");
 		desc_height.appendChild("32");
 		description.appendChild(desc_height);
 		desc_damage = new Element("damage");
 		desc_damage.appendChild("1");
 		description.appendChild(desc_damage);
 		desc_list.add(description);
 		
 		// MeleeEnemy Description
 		description = new Element("actor");
 		description.addAttribute(new Attribute("name", "meleenemy"));
 		description.addAttribute(new Attribute("id", "3"));
 		desc_spritesheet = new Element("spritesheet");
 		desc_spritesheet.appendChild("res/enemy1.png");
 		description.appendChild(desc_spritesheet);
 		desc_isRanged = new Element("isRanged");
 		desc_isRanged.appendChild("0");
 		description.appendChild(desc_isRanged);
 		desc_aggroRange = new Element("aggroRange");
 		desc_aggroRange.appendChild("96");
 		description.appendChild(desc_aggroRange);
 		desc_attackRange = new Element("attackRange");
 		desc_attackRange.appendChild("16");
 		description.appendChild(desc_attackRange);
 		desc_type = new Element("type");
 		desc_type.appendChild("3");
 		description.appendChild(desc_type);
 		desc_level = new Element("level");
 		desc_level.appendChild("1");
 		description.appendChild(desc_level);
 		desc_element = new Element("element");
 		desc_element.appendChild("1");
 		description.appendChild(desc_element);
 		desc_list.add(description);
 		
 		// BossEnemy Description
 		description = new Element("actor");
 		description.addAttribute(new Attribute("name", "bossenemy"));
 		description.addAttribute(new Attribute("id", "4"));
 		desc_spritesheet = new Element("spritesheet");
 		desc_spritesheet.appendChild("res/enemy1.png");
 		description.appendChild(desc_spritesheet);
 		desc_isRanged = new Element("isRanged");
 		desc_isRanged.appendChild("0");
 		description.appendChild(desc_isRanged);
 		desc_aggroRange = new Element("aggroRange");
 		desc_aggroRange.appendChild("96");
 		description.appendChild(desc_aggroRange);
 		desc_attackRange = new Element("attackRange");
 		desc_attackRange.appendChild("32");
 		description.appendChild(desc_attackRange);
 		desc_type = new Element("type");
 		desc_type.appendChild("3");
 		description.appendChild(desc_type);
 		desc_level = new Element("level");
 		desc_level.appendChild("2");
 		description.appendChild(desc_level);
 		desc_element = new Element("element");
 		desc_element.appendChild("1");
 		description.appendChild(desc_element);
 		desc_list.add(description);
 		
 		// NPCshop Description
 		description = new Element("actor");
 		description.addAttribute(new Attribute("name", "npcshop"));
 		description.addAttribute(new Attribute("id", "5"));
 		desc_spritesheet = new Element("spritesheet");
 		desc_spritesheet.appendChild("res/shop.png");
 		description.appendChild(desc_spritesheet);
 		desc_level = new Element("level");
 		desc_level.appendChild("1");
 		description.appendChild(desc_level);
 		desc_element = new Element("element");
 		desc_element.appendChild("0");
 		description.appendChild(desc_element);
 		desc_list.add(description);
 				
 		// NPCtalking Description
 		description = new Element("actor");
 		description.addAttribute(new Attribute("name", "npctalking"));
 		description.addAttribute(new Attribute("id", "6"));
 		desc_spritesheet = new Element("spritesheet");
 		desc_spritesheet.appendChild("res/shop.png");
 		description.appendChild(desc_spritesheet);
 		desc_level = new Element("level");
 		desc_level.appendChild("1");
 		description.appendChild(desc_level);
 		desc_element = new Element("element");
 		desc_element.appendChild("0");
 		description.appendChild(desc_element);
 		desc_list.add(description);
 		
 		// NPCquest Description
 		description = new Element("actor");
 		description.addAttribute(new Attribute("name", "npcquest"));
 		description.addAttribute(new Attribute("id", "8"));
 		desc_spritesheet = new Element("spritesheet");
 		desc_spritesheet.appendChild("res/shop.png");
 		description.appendChild(desc_spritesheet);
 		desc_level = new Element("level");
 		desc_level.appendChild("1");
 		description.appendChild(desc_level);
 		desc_element = new Element("element");
 		desc_element.appendChild("0");
 		description.appendChild(desc_element);
 		desc_list.add(description);
 		
 		String mapString = "\n";
 		for(int j=0; j<walls[j].length; j++)
 		{
 			mapString += "\t\t";
 			for(int i=0; i<walls.length; i++)
 			{
 				mapString += walls[i][j];
 			}
 			mapString += "\n";
 		}
 		mapString += "\t";
 		// END OF DESCRIPTIONS
 		
 		// Root Element level
 		Element map = new Element("map");
 		{
 			// Elements to append to map
 			Element map_name = new Element("name");
 			map_name.appendChild(mapName);
 			map.appendChild(map_name);
 			
 			Element map_width = new Element("width");
 			map_width.appendChild("32");
 			map.appendChild(map_width);
 			
 			Element map_height = new Element("height");
 			map_height.appendChild("20");
 			map.appendChild(map_height);
 			
 			Element map_tiles = new Element("tiles");
 			Attribute map_tiles_space = new Attribute("xml:space", XMLConstants.XML_NS_URI, "preserve");
 			map_tiles.addAttribute(map_tiles_space);			
 			map_tiles.appendChild(mapString);
 			map.appendChild(map_tiles);
 			
 			Element descriptions = new Element("descriptions");
 			{
 				// Elements to append to descriptions
 				for(int i=0; i<desc_list.size(); i++)
 					descriptions.appendChild(desc_list.get(i));
 			}
 			map.appendChild(descriptions);
 			
 			Element gameobjects = new Element("gameobjects");
 			{
 				// Elements to append to gameobjects
 				Element elObjects = new Element("objects");
 				Element elTraps = new Element("traps");
 				Element elActors = new Element("actors");
 				Element elItems = new Element("items");
 				
 				for(int j=0; j<objects[0].length; j++)
 				{
 					for(int i=0; i<objects.length; i++)
 					{
 						if(objects[i][j] != -1)
 						{
 							Element posX = new Element("positionX");
 							posX.appendChild(String.valueOf(i));
 							Element posY = new Element("positionY");
 							posY.appendChild(String.valueOf(j));
 							Element newObj;
 						
 							if(objects[i][j] == 0)
 							{
 								newObj =  new Element("object");
 								newObj.addAttribute(new Attribute("id", "0"));
 								newObj.appendChild(posX);
 								newObj.appendChild(posY);
 								
 								MapToInfo mapToInfo = mapToInfoList.get(new Point(i,j));
 								Element mapToName = new Element("mapToName");
 								mapToName.appendChild(mapToInfo.getMapToName());
 								newObj.appendChild(mapToName);
 								
 								Element mapToX = new Element("mapToX");
 								mapToX.appendChild(mapToInfo.getMapToX());
 								newObj.appendChild(mapToX);
 								
 								Element mapToY = new Element("mapToY");
 								mapToY.appendChild(mapToInfo.getMapToY());
 								newObj.appendChild(mapToY);
 								
 								elObjects.appendChild(newObj);
 							}
 							
 							else if(objects[i][j] == 1)
 							{
 								newObj =  new Element("object");
 								newObj.addAttribute(new Attribute("id", "1"));
 								newObj.appendChild(posX);
 								newObj.appendChild(posY);
 								
 								elObjects.appendChild(newObj);
 							}
 							
 							else if(objects[i][j] == 2)
 							{
 								newObj =  new Element("trap");
 								newObj.addAttribute(new Attribute("id", "2"));
 								newObj.appendChild(posX);
 								newObj.appendChild(posY);
 								
 								elTraps.appendChild(newObj);
 							}
 							
 							else if(objects[i][j] == 3)
 							{
 								newObj =  new Element("actor");
 								newObj.addAttribute(new Attribute("id", "3"));
 								newObj.appendChild(posX);
 								newObj.appendChild(posY);
 								
 								elActors.appendChild(newObj);
 							}
 							
 							else if(objects[i][j] == 4)
 							{
 								newObj =  new Element("actor");
 								newObj.addAttribute(new Attribute("id", "4"));
 								newObj.appendChild(posX);
 								newObj.appendChild(posY);
 								
 								MapToInfo mapToInfo = mapToInfoList.get(new Point(i,j));
 								Element mapToName = new Element("mapToName");
 								mapToName.appendChild(mapToInfo.getMapToName());
 								newObj.appendChild(mapToName);
 								
 								Element mapToX = new Element("mapToX");
 								mapToX.appendChild(mapToInfo.getMapToX());
 								newObj.appendChild(mapToX);
 								
 								Element mapToY = new Element("mapToY");
 								mapToY.appendChild(mapToInfo.getMapToY());
 								newObj.appendChild(mapToY);
 								
 								elActors.appendChild(newObj);
 							}
 							
 							else if(objects[i][j] == 5)
 							{
 								newObj =  new Element("actor");
 								newObj.addAttribute(new Attribute("id", "5"));
 								newObj.appendChild(posX);
 								newObj.appendChild(posY);
 								
 								elActors.appendChild(newObj);
 							}
 							
 							else if(objects[i][j] == 6)
 							{
 								newObj =  new Element("actor");
 								newObj.addAttribute(new Attribute("id", "6"));
 								newObj.appendChild(posX);
 								newObj.appendChild(posY);
 								
 								elActors.appendChild(newObj);
 							}
 							
 							else if(objects[i][j] == 7)
 							{
 								newObj =  new Element("object");
 								newObj.addAttribute(new Attribute("id", "7"));
 								newObj.appendChild(posX);
 								newObj.appendChild(posY);
 								
 								elObjects.appendChild(newObj);
 							}
 							
 							else if(objects[i][j] == 8)
 							{
 								newObj =  new Element("actor");
 								newObj.addAttribute(new Attribute("id", "8"));
 								newObj.appendChild(posX);
 								newObj.appendChild(posY);
 								
								Element newQuest = new Element("quest");
								newQuest.appendChild("0");
								newObj.appendChild(newQuest);
								
								newQuest = new Element("quest");
								newQuest.appendChild("1");
								newObj.appendChild(newQuest);
								
 								elObjects.appendChild(newObj);
 							}
 							
 							else if(objects[i][j] >= 100)
 							{
 								newObj = new Element("item");
 								newObj.addAttribute(new Attribute("id", String.valueOf(objects[i][j])));
 								newObj.appendChild(posX);
 								newObj.appendChild(posY);
 								
 								elItems.appendChild(newObj);
 							}
 						}
 					}
 				}
 				gameobjects.appendChild(elObjects);
 				gameobjects.appendChild(elTraps);
 				gameobjects.appendChild(elActors);
 				gameobjects.appendChild(elItems);
 			}
 			map.appendChild(gameobjects);
 		}
 		Document doc = new Document(map);
 		
 		try
 		{
 			FileOutputStream fop = new FileOutputStream(outputFile);
 			Serializer serializer = new Serializer(fop, "ISO-8859-1");
 			serializer.setIndent(8);
 			serializer.setMaxLength(64);
 			serializer.write(doc);
 		} catch (IOException e)
 		{
 			System.err.println("Could not save map");
 		}
 	}
 	
 	/**
 	 * Loads a map in the editor.
 	 * @param inputFile path to the file that should be loaded
 	 */
 	public void loadMap(String inputFile)
 	{
 		File source = new File(inputFile);
 		
 		// Loads the XML File into "map"
 		Document map = null;
 		try
 		{
 			Builder builder = new Builder(true);
 			map = builder.build(source);
 		}
 		catch (ValidityException ex)
 		{
 			map = ex.getDocument();
 		}
 		catch (ParsingException ex)
 		{
 			System.err.println("This file is not well-formed.");
 			ex.printStackTrace();
 			System.exit(1);
 		}
 		catch (IOException ex)
 		{
 			System.err.println("Could not read file " + inputFile);
 			System.exit(1);
 		}
 		
 		Element current;
 		
 		current = map.getRootElement().getChildElements("name").get(0);
 		me.getMEMenuBar().setMapName(current.getChild(0).getValue());
 		current = map.getRootElement().getChildElements("width").get(0);
 		int mapwidth = Integer.parseInt(current.getChild(0).getValue());
 		Tilemap.setWidth(mapwidth);
 		current = map.getRootElement().getChildElements("height").get(0);
 		int mapheight = Integer.parseInt(current.getChild(0).getValue());
 		Tilemap.setHeight(mapheight);
 		
 		if((mapheight != 20) || (mapwidth != 32))
 		{
 			System.err.println("Could not load map. Reason: Mapwidth "+
 								"is not 32 or mapheight is not 20");
 			return;
 		}
 		
 		this.init();
 		
 		String tiles = map.getRootElement().getChildElements("tiles").get(0).getValue();
 		int i=0, j=0;
 		char curChar;
 	
 		// go through tiles string
 		for(int k=0; k<tiles.length(); k++)
 		{
 			curChar = tiles.charAt(k);
 			
 			// If read character is a new line and we filled a line,
 			// go into next line
 			if(curChar == '\n' && i==32)
 			{
 				j++;
 				i=0;
 			}
 			// If read character is neither a new line nor a tab,
 			// fill character into array
 			else if(curChar == ' ')
 			{
 				walls[i][j] = ' ';
 				i++;
 			}
 			else if(curChar == '#')
 			{
 				walls[i][j] = '#';
 				i++;
 			}
 		}
 
 		current = map.getRootElement().getChildElements("gameobjects").get(0);
 		for(i=0; i<current.getChildElements().size(); i++)
 		{
 			Element tmp = current.getChildElements().get(i);
 			for(j=0; j<tmp.getChildElements().size(); j++)
 			{
 				Element tmp2 = tmp.getChildElements().get(j);
 				
 				int posX = Integer.parseInt(tmp2.getChildElements().get(0).getValue());
 				int posY = Integer.parseInt(tmp2.getChildElements().get(1).getValue());
 				objects[posX][posY] = Integer.parseInt(tmp2.getAttribute(0).getValue());
 				
 				if((objects[posX][posY] == 0) || (objects[posX][posY] == 4))
 				{
 					mapToInfoList.put(new Point(posX, posY),
 										new MapToInfo(tmp2.getChildElements().get(2).getValue(),
 													tmp2.getChildElements().get(3).getValue(),
 													tmp2.getChildElements().get(4).getValue()));
 				}
 			}
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e)
 	{
 		int tileX = (int)e.getX()/me.TILE_SIZE;
 		int tileY = (int)e.getY()/me.TILE_SIZE;
 		
 		if(tileX<0)
 			tileX = 0;
 		else if(tileX>=me.WIDTH)
 			tileX = me.WIDTH-1;
 		
 		if(tileY<0)
 			tileY = 0;
 		else if(tileY>=me.HEIGHT)
 			tileY = me.HEIGHT-1;
 				
 		highlight.setLocation(tileX*me.TILE_SIZE, tileY*me.TILE_SIZE);
 		
 		if(me.getMEToolbar().getCurrentID().equals("grass"))
 		{
 			if((tileX!=0) && (tileX!=me.WIDTH-1) && (tileY!=0) && (tileY!=me.HEIGHT-1))
 			{
 				walls[tileX][tileY] = ' ';
 				objects[tileX][tileY] = -1;
 				mapToInfoList.remove(new Point(tileX, tileY));
 			}
 		}
 		else if(me.getMEToolbar().getCurrentID().equals("wall"))
 		{
 			walls[tileX][tileY] = '#';
 			objects[tileX][tileY] = -1;
 			mapToInfoList.remove(new Point(tileX, tileY));
 		}
 		else
 		{
 			try
 			{
 				int getID = Integer.parseInt(me.getMEToolbar().getCurrentID());
 				
 				// only overwrite value, if it's not the border of the map
 				if((tileX!=0) && (tileX!=me.WIDTH-1) && (tileY!=0) && (tileY!=me.HEIGHT-1))
 				{
 					// if object is a portal, get mapTo information
 					if((getID == 0) || (getID == 4))
 					{
 						MEObjectSettings st = new MEObjectSettings();
 						if(!st.wasCancelled() && st.getMapToInfo().isInformationValid())
 						{
 							mapToInfoList.remove(new Point(tileX, tileY));
 							objects[tileX][tileY] = getID;
 							walls[tileX][tileY] = ' ';
 							mapToInfoList.put(new Point(tileX, tileY), st.getMapToInfo());
 						}
 					}
 					else
 					{
 						objects[tileX][tileY] = getID;
 						walls[tileX][tileY] = ' ';
 						mapToInfoList.remove(new Point(tileX, tileY));
 					}
 				}
 				// except for portals, they are allowed everywhere
 				else if(getID == 0)
 				{
 					// get mapTo information
 					MEObjectSettings st = new MEObjectSettings();
 					if(!st.wasCancelled() && st.getMapToInfo().isInformationValid())
 					{
 						mapToInfoList.remove(new Point(tileX, tileY));
 						objects[tileX][tileY] = getID;
 						walls[tileX][tileY] = ' ';
 						mapToInfoList.put(new Point(tileX, tileY), st.getMapToInfo());
 					}
 				}
 			}
 			catch(NumberFormatException ex) { }
 		}
 		
 		repaint();
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e)
 	{
 		mouseClicked(e);
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent e)
 	{
 		int tileX = (int)e.getX()/me.TILE_SIZE;
 		int tileY = (int)e.getY()/me.TILE_SIZE;
 		
 		if(tileX<0)
 			tileX = 0;
 		else if(tileX>=me.WIDTH)
 			tileX = me.WIDTH-1;
 		
 		if(tileY<0)
 			tileY = 0;
 		else if(tileY>=me.HEIGHT)
 			tileY = me.HEIGHT-1;
 		
 		highlight.setLocation(tileX*me.TILE_SIZE, tileY*me.TILE_SIZE);
 		
 		repaint();
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) { }
 
 	@Override
 	public void mouseExited(MouseEvent e) { }
 
 	@Override
 	public void mousePressed(MouseEvent e) { }
 
 	@Override
 	public void mouseReleased(MouseEvent e) { }
 }
