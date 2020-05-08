 package riskyspace.view;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import riskyspace.logic.ViewEventController;
 import riskyspace.model.Planet;
 import riskyspace.model.Player;
 import riskyspace.model.Position;
 import riskyspace.model.Resource;
 import riskyspace.model.ShipType;
 import riskyspace.model.Territory;
 import riskyspace.model.World;
 
 /**
  * 
  * @author flygarn
  * 
  */
 public class SpriteMap {
 	/*
 	 * Planet Textures
 	 */
 	private static Map<Integer, Image> metalplanets = new HashMap<Integer, Image>();
 	private static Map<Integer, Image> gasplanets = new HashMap<Integer, Image>();
 	/*
 	 * Path Textures
 	 */
 	private static Map<String, Image> pathTextures = new HashMap<String, Image>();
 	
 	/*
 	 * Ship Sprites
 	 */
 	private static Map<String, Sprite> shipSprites = new HashMap<String, Sprite>();
 	
 	/*
 	 * All planets with randomized textures
 	 */
 	private static Map<Position, Sprite> allPlanets = new HashMap<Position, Sprite>();
 	
 	private Map<Position, Sprite> fogOfWar = new HashMap<Position, Sprite>();
 	private Map<Position, Sprite> colonies = new HashMap<Position, Sprite>();
 	private Map<Position, List<Sprite>> paths = new HashMap<Position, List<Sprite>>();
 	private Map<Position, Sprite> fleets = new HashMap<Position, Sprite>();
 	private Map<Position, Sprite> colonizers = new HashMap<Position, Sprite>();
 	
 	private Map<Position, Sprite> planets = new HashMap<Position, Sprite>();
 	
 	private static World world = null;
 	private static ViewEventController vec = null;
 	
 	public static void init(World world, ViewEventController vec) {
 		SpriteMap.world = world;
 		SpriteMap.vec = vec;
 		setTextures();
 		setPlanetSprites();
 	}
 	
 	public static void setPlanetSprites() {
 		for (Position pos : world.getContentPositions()) {
 			if (world.getTerritory(pos).hasPlanet()) {
 				Planet p = world.getTerritory(pos).getPlanet();
 				if (p.getType() == Resource.METAL) {
 					int index = (int) (Math.random()*4);
 					allPlanets.put(pos, new Sprite(metalplanets.get(index), 0.5f, 0, 0.5f));
 				} else if (p.getType() == Resource.GAS) {
 					int index = (int) (Math.random()*3);
 					allPlanets.put(pos, new Sprite(gasplanets.get(index), 0.5f, 0, 0.5f));
 				}
 			}
 		}
 	}
 	
 	public static SpriteMap getSprites(Player player) {
 		SpriteMap map = new SpriteMap();
 		for (Position pos : world.getContentPositions()) {
 			Territory terr = world.getTerritory(pos);
 			if (terr.hasColony()) {
 				BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
 				Graphics g = img.createGraphics();
 				g.setColor(terr.getColony().getOwner() == Player.BLUE ? Color.BLUE : Color.RED);
 				g.fillOval(5, 5, 90, 90);
 				map.colonies.put(pos, new Sprite(img, 0.5f, 0, 0.5f));
 			}
 			if (terr.hasPlanet()) {
 				map.planets.put(pos, allPlanets.get(pos));
 			}
 			if (terr.hasColonizer()) {
 				map.colonizers.put(pos, shipSprites.get("COLONIZER_" + terr.controlledBy()));
 			}
 			if (terr.hasFleet()) {
 				if (terr.getFleetsFlagships() != ShipType.COLONIZER) {
 					map.fleets.put(pos, shipSprites.get(terr.getFleetsFlagships() + "_" + terr.controlledBy()));
 				}
 			}
 		}
 		Position[][] paths = vec.getPaths();
 		for (int i = 0; i < paths.length; i++) {
 			if (paths[i].length > 1) {
 				for (int j = 0; j < paths[i].length; j++) {
 					if (!map.paths.containsKey(paths[i][j])) {
 						map.paths.put(paths[i][j], new ArrayList<Sprite>());
 					}
 					Sprite sprite = null;
 					if (j == 0) {
 						sprite = new Sprite(SpriteMap.pathTextures.get("START"), 0, 0, 1);
 						sprite.setRotation(getRotation(null, paths[i][j], paths[i][j+1]));
 					} else if (j == paths[i].length - 1) {
 						sprite = new Sprite(SpriteMap.pathTextures.get("HEAD"), 0, 0, 1);
 						sprite.setRotation(getRotation(paths[i][j-1], paths[i][j], null));
 					} else if (paths[i][j-1].getCol() != paths[i][j+1].getCol() && paths[i][j-1].getRow() != paths[i][j+1].getRow()) {
 						sprite = new Sprite(SpriteMap.pathTextures.get("TURN"), 0, 0, 1);
 						sprite.setRotation(getRotation(paths[i][j-1], paths[i][j], paths[i][j+1]));
 					} else {
 						sprite = new Sprite(SpriteMap.pathTextures.get("STRAIGHT"), 0, 0, 1);
 						sprite.setRotation(getRotation(paths[i][j-1], paths[i][j], paths[i][j+1]));
 					}
 					map.paths.get(paths[i][j]).add(sprite);
 				}
 			}
 		}
 		return map;
 	}
 	
 	private static double getRotation(Position previous, Position current, Position next) {
 		/*
 		 * Variables for determining where the previous and next positions
 		 * are relative to the current position
 		 */
 		boolean prevAbove = false;
 		boolean prevUnder = false;
 		boolean prevLeft = false;
 		boolean prevRight = false;
 		
 		boolean nextRight = false;
 		boolean nextUnder = false;
 		boolean nextLeft = false;
 		boolean nextAbove = false;
 		
 		/*
 		 * Only calculate values if there is a previous
 		 * respective next position in.
 		 */
 		if (previous != null) {
 			prevAbove = previous.getRow() < current.getRow();
 			prevUnder = previous.getRow() > current.getRow();
 			prevLeft = previous.getCol() < current.getCol();
 			prevRight = previous.getCol() > current.getCol();
 		}
 		if (next != null) {
 			nextRight = next.getCol() > current.getCol();
 			nextUnder = next.getRow() > current.getRow();
 			nextLeft = next.getCol() < current.getCol();
 			nextAbove = next.getRow() < current.getRow();
 		}
 		/*
 		 * If previous is null then only determine rotation based on 
 		 * next position.
 		 * >> Path is always of length 2 at least, therefore no point can
 		 * have neither previous or next location.
 		 */
 		if (previous == null) {
 			if (nextAbove) {
 				return 3*Math.PI/2;
 			} else if (nextUnder) {
 				return Math.PI/2;
 			} else if (nextLeft) {
 				return Math.PI;
 			} else if (nextRight) {
 				return 0;
 			}
 		}
 		/*
 		 * If next is null then only determine rotation based on 
 		 * previous position.
 		 */
 		if (next == null) {
 			if (prevAbove) {
 				return Math.PI/2;
 			} else if (prevUnder) {
 				return 3*Math.PI/2;
 			} else if (prevLeft) {
 				return 0;
 			} else if (prevRight) {
 				return Math.PI;
 			}
 		}
 		/*
 		 * Return rotation based on where the previous and next locations are.
 		 */
 		if (prevAbove) {
 			if (nextUnder) {
 				return Math.PI/2;
 			} else if (nextLeft) {
 				return Math.PI/2;
 			} else if (nextRight) {
 				return Math.PI;
 			}
 		} else if (nextAbove) {
 			if (prevUnder) {
 				return Math.PI/2;
 			} else if (prevLeft) {
 				return Math.PI/2;
 			} else if (prevRight) {
 				return Math.PI;
 			}
 		} else if (prevUnder) {
 			if (nextAbove) {
 				return Math.PI/2;
 			} else if (nextLeft) {
 				return 0;
 			} else if (nextRight) {
 				return 3*Math.PI/2;
 			}
 		} else if (nextUnder) {
 			if (prevAbove) {
 				return Math.PI/2;
 			} else if (prevLeft) {
 				return 0;
 			} else if (prevRight) {
 				return 3*Math.PI/2;
 			}
 		}
 		/*
 		 * Return 0 to make the compiler happy, will never run
 		 * unless previous == current || current == next which
 		 * is wrong usage.
 		 */
 		return 0;
 	}
 	
 	private static void setTextures() {
 		/*
 		 * Planets
 		 */
 		metalplanets.put(0, Toolkit.getDefaultToolkit().getImage("res/icons/planets/metalplanet_0.png"));
 		metalplanets.put(1, Toolkit.getDefaultToolkit().getImage("res/icons/planets/metalplanet_1.png"));
 		metalplanets.put(2, Toolkit.getDefaultToolkit().getImage("res/icons/planets/metalplanet_2.png"));
 		metalplanets.put(3, Toolkit.getDefaultToolkit().getImage("res/icons/planets/metalplanet_3.png"));
 		gasplanets.put(0, Toolkit.getDefaultToolkit().getImage("res/icons/planets/gasplanet_0.png"));
 		gasplanets.put(1, Toolkit.getDefaultToolkit().getImage("res/icons/planets/gasplanet_1.png"));
 		gasplanets.put(2, Toolkit.getDefaultToolkit().getImage("res/icons/planets/gasplanet_2.png"));
 		
 		/*
 		 * Ships Blue Player
 		 */
 		shipSprites.put("SCOUT_BLUE", new Sprite(Toolkit.getDefaultToolkit().getImage("res/icons/blue/scout.png"), 0, 0.5f, 0.5f));
 		shipSprites.put("HUNTER_BLUE", new Sprite(Toolkit.getDefaultToolkit().getImage("res/icons/blue/hunter.png"), 0, 0.5f, 0.5f));
 		shipSprites.put("DESTROYER_BLUE", new Sprite(Toolkit.getDefaultToolkit().getImage("res/icons/blue/destroyer.png"), 0, 0.5f, 0.5f));
		shipSprites.put("COLONIZER_BLUE", new Sprite(Toolkit.getDefaultToolkit().getImage("res/icons/blue/colonizer.png"), 0.5f, 0.5f, 0.5f));
 		
 		/*
 		 * Ships Red Player
 		 */
 		shipSprites.put("SCOUT_RED", new Sprite(Toolkit.getDefaultToolkit().getImage("res/icons/red/scout.png"), 0, 0.5f, 0.5f));
 		shipSprites.put("HUNTER_RED", new Sprite(Toolkit.getDefaultToolkit().getImage("res/icons/red/hunter.png"), 0, 0.5f, 0.5f));
 		shipSprites.put("DESTROYER_RED", new Sprite(Toolkit.getDefaultToolkit().getImage("res/icons/red/destroyer.png"), 0, 0.5f, 0.5f));
		shipSprites.put("COLONIZER_RED", new Sprite(Toolkit.getDefaultToolkit().getImage("res/icons/red/colonizer.png"), 0.5f, 0.5f, 0.5f));
 		
 		/*
 		 * Path Arrows
 		 */
 		pathTextures.put("HEAD", Toolkit.getDefaultToolkit().getImage("res/head.png"));
 		pathTextures.put("START", Toolkit.getDefaultToolkit().getImage("res/start.png"));
 		pathTextures.put("STRAIGHT", Toolkit.getDefaultToolkit().getImage("res/straight.png"));
 		pathTextures.put("TURN", Toolkit.getDefaultToolkit().getImage("res/turn.png"));
 	}
 	
 	/**
 	 * Draw this sprite map
 	 * @param g The graphics context to draw on.
 	 * @param squareSize Size of one square in pixels
 	 * @param offsetX The number of squares on the left and right
 	 * sides of the world.
 	 * @param offsetY The number of squares on the top and bottom
 	 * sides of the world.
 	 */
 	public void draw(Graphics g, int squareSize, int offsetX, int offsetY) {
 		for (Position pos : colonies.keySet()) {
 			colonies.get(pos).draw(g, calcX(pos, offsetX, squareSize), calcY(pos, offsetY, squareSize), squareSize);
 		}
 		for (Position pos : planets.keySet()) {
 			planets.get(pos).draw(g, calcX(pos, offsetX, squareSize), calcY(pos, offsetY, squareSize), squareSize);
 		}
 		for (Position pos : paths.keySet()) {
 			for (Sprite sprite : paths.get(pos)) {
 				sprite.draw(g, calcX(pos, offsetX, squareSize), calcY(pos, offsetY, squareSize), squareSize);
 			}
 		}
 		for (Position pos : fleets.keySet()) {
 			fleets.get(pos).draw(g, calcX(pos, offsetX, squareSize), calcY(pos, offsetY, squareSize), squareSize);
 		}
 		for (Position pos : colonizers.keySet()) {
 			colonizers.get(pos).draw(g, calcX(pos, offsetX, squareSize), calcY(pos, offsetY, squareSize), squareSize);
 		}
 	}
 
 	private int calcX(Position pos, int offsetX, int squareSize) {
 		return (pos.getCol()-1 + offsetX)*squareSize;
 	}
 
 	private int calcY(Position pos, int offsetY, int squareSize) {
 		return (pos.getRow()-1 + offsetY)*squareSize;
 	}
 }
