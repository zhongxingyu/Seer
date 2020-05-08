 package spacewars.game;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Line2D;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import spacewars.game.model.GameElement;
 import spacewars.game.model.GameState;
 import spacewars.game.model.Link;
 import spacewars.game.model.Map;
 import spacewars.game.model.Player;
 import spacewars.game.model.Ship;
 import spacewars.game.model.Star;
 import spacewars.game.model.buildings.Building;
 import spacewars.game.model.buildings.BuildingType;
 import spacewars.game.model.buildings.LaserCanon;
 import spacewars.game.model.buildings.Mine;
 import spacewars.game.model.buildings.Relay;
 import spacewars.game.model.buildings.Shipyard;
 import spacewars.game.model.buildings.SolarStation;
 import spacewars.game.model.planets.HomePlanet;
 import spacewars.game.model.planets.MineralPlanet;
 import spacewars.gamelib.Button;
 import spacewars.gamelib.Game;
 import spacewars.gamelib.GameTime;
 import spacewars.gamelib.Key;
 import spacewars.gamelib.Keyboard;
 import spacewars.gamelib.Mouse;
 import spacewars.gamelib.Screen;
 import spacewars.gamelib.geometrics.Vector;
 
 public class SpaceWars extends Game
 {
     public static final boolean             DEBUG = true;
     /**
      * Game instance
      */
     private static SpaceWars                instance;
     /**
      * Random object
      */
     private final Random                    random;
     /**
      * The stars in the background
      */
     private final List<Star>                stars;
     /**
      * The player
      */
     private Player                          player;
     /**
      * The game state
      */
     private GameState                       gameState;
     /**
      * Game element that is currently selected by players mouse
      */
     private GameElement                     selected;
     /**
      * The type of the building that will be built
      */
     private BuildingType                    buildingType;
     /**
      * Building object that will be built
      */
     private Building                        buildingToBePlaced;
     /**
      * Can the building object <code>toBuild</code> really be built? Or can't it
      * because of collision
      */
     private boolean                         buildingIsPlaceable;
     /**
      * The links of the building that will be built
      */
     private final List<Link<Building>>      linksToBuildings;
     /**
      * The links of a mine to reachable mineral planets
      */
     private final List<Link<MineralPlanet>> linksToMineralPlanets;
     /**
      * Current scroll position
      */
     private Vector                          scrollPosition;
     
     private SpaceWars()
     {
         this.random = new Random();
         this.buildingType = BuildingType.NOTHING;
         this.scrollPosition = new Vector();
         this.stars = new LinkedList<Star>();
         this.linksToBuildings = new LinkedList<>();
         this.linksToMineralPlanets = new LinkedList<>();
     }
     
     public static SpaceWars getInstance()
     {
         if (instance == null)
         {
             instance = new SpaceWars();
         }
         return instance;
     }
     
     public GameElement getSelected()
     {
         return selected;
     }
     
     public GameState getGameState()
     {
         return gameState;
     }
     
     @Override
     protected void initialize()
     {
         Screen screen = Screen.getInstance();
         
         screen.setTitle("Space Wars");
         screen.setIcon("icon.png");
         screen.setSize(new Dimension(800, 600));
         // screen.setSize(null);
         
         // init game state
         createMap();
         createStars();
         createPlayers();
         createShips();
         
         returnToHomePlanet();
         
         // show screen
         screen.setVisible(true);
     }
     
     private void createMap()
     {
         Map map = MapFactory.loadMap("map1.png");
         gameState = new GameState(map);
     }
     
     private void createStars()
     {
         for (int i = 0; i < gameState.getMap().getNumStars(); i++)
         {
             final int x = random.nextInt(Screen.getInstance().getSize().width);
             final int y = random.nextInt(Screen.getInstance().getSize().height);
             final int layer = random.nextInt(gameState.getMap().getNumLayers());
             
             stars.add(new Star(x, y, layer));
         }
     }
     
     private void createPlayers()
     {
         final List<Player> players = gameState.getPlayers();
         
         players.add(new Player(1, Color.BLUE, getGameState().getMap().getHomePlanetPositions().get(1)));
         players.add(new Player(2, Color.MAGENTA, getGameState().getMap().getHomePlanetPositions().get(2)));
         
         player = players.get(0);
     }
     
     private void createShips()
     {
         final int NUM_SHIPS = 500;
         final List<Ship> ships = gameState.getShips();
         final Vector home = gameState.getMap().getHomePlanetPositions().get(player.getId());
         
         for (int i = 0; i < NUM_SHIPS; i++)
         {
             ships.add(new Ship(player, new Vector(home), random.nextDouble() * Math.PI * 2));
         }
     }
     
     @Override
     public void update(GameTime gameTime)
     {
         // navigate
         scroll();
         
         if (Keyboard.getState().isKeyPressed(Key.H))
         {
             returnToHomePlanet();
         }
         
         // move ships
         for (Ship ship : gameState.getShips())
         {
             ship.update(gameTime);
         }
         
         // select
         select();
         
         if (DEBUG)
         {
             // change player (only debug)
             changePlayer();
         }
         
         // build
         setBuildMode();
         build();
         
         // check which buildings are on the energy net
         checkEnergyAvailability();
         
         // update energy flow every 20th frame
         final int UPDATE_RATE = 20;
         if (gameTime.getTicks() % UPDATE_RATE == 0)
         {
             updateEnergyFlow();
         }
         
         // TODO: kai
         // amount of res and energy
         if (gameTime.isSecond())
         {
             System.out.println("true");
             for (Building building : gameState.getBuildings())
             {
                 if (building.isPlaced())
                 {
                     // building.setHasEnergy(true);
                     if (building.hasEnergy())
                     {
                         // building.setHasEnergy();
                        if (building instanceof Mine)
                         {
                            final Mine mine = (Mine) building;
                            
                            player.removeEnergy(mine.getEnergyConsumPerMin() / 60);
                            player.addMinerals(mine.getResPerMin() / 60);
                         }
                     }
                     
                     if (building instanceof SolarStation)
                     {
                         ((SolarStation) building).update();
                         player.addEnergy(((SolarStation) building).getEnergyPerMin() / 60);
                     }
                 }
             }
         }
     }
     
     /**
      * Finds the connected buildings to the <code>buildingToBePlaced</code> and
      * computes the lines between.
      */
     private void computeLinksToBuildings()
     {
         linksToBuildings.clear();
         linksToMineralPlanets.clear();
         
         // create links to other buildings that are reachable
         for (Building building : gameState.getBuildings())
         {
             if (building.isReachableFrom(buildingToBePlaced))
             {
                 final Vector p1 = buildingToBePlaced.getPosition();
                 final Vector p2 = building.getPosition();
                 final Line2D line = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
                 final boolean collision = checkCollision(line, building);
                 
                 linksToBuildings.add(new Link<>(building, line, collision));
             }
         }
         
         // sort links by by lenght
         Collections.sort(linksToBuildings, new Comparator<Link<Building>>()
         {
             @Override
             public int compare(Link<Building> l1, Link<Building> l2)
             {
                 final double distance1 = l1.getLinkedElement().getPosition().distance(buildingToBePlaced.getPosition());
                 final double distance2 = l2.getLinkedElement().getPosition().distance(buildingToBePlaced.getPosition());
                 if (distance1 > distance2) return 1;
                 if (distance1 < distance2) return -1;
                 return 0;
             }
         });
         
         // filter links
         if (buildingToBePlaced instanceof Mine)
         {
             // compute links to reachable mineral planets
             computeLinksToMineralPlanets();
             
             // remove too long links
             boolean removeRest = false;
             for (Iterator<Link<Building>> iterator = linksToBuildings.iterator(); iterator.hasNext();)
             {
                 final Link<Building> link = iterator.next();
                 final Building building = link.getLinkedElement();
                 
                 // only treat the players building
                 if (!removeRest && building.getPlayer() == player)
                 {
                     // take every link to relays, solar stations or buildings
                     // with no links
                     if (building instanceof Relay || building instanceof SolarStation)
                     {
                         removeRest = !link.isCollision();
                         continue;
                     }
                 }
                 
                 iterator.remove();
             }
         }
         else if (buildingToBePlaced instanceof Relay || buildingToBePlaced instanceof SolarStation)
         {
             for (Iterator<Link<Building>> iterator = linksToBuildings.iterator(); iterator.hasNext();)
             {
                 final Building building = iterator.next().getLinkedElement();
                 
                 // only treat the players building
                 if (building.getPlayer() == player)
                 {
                     // take every link to relays, solar stations or buildings
                     // with no links
                     if (building instanceof Relay) continue;
                     if (building instanceof SolarStation) continue;
                     if (building.getLinks().isEmpty()) continue;
                 }
                 
                 iterator.remove();
             }
         }
         else if (buildingToBePlaced instanceof LaserCanon || buildingToBePlaced instanceof Shipyard)
         {
             // remove too long links
             boolean removeRest = false;
             for (Iterator<Link<Building>> iterator = linksToBuildings.iterator(); iterator.hasNext();)
             {
                 final Link<Building> link = iterator.next();
                 final Building building = link.getLinkedElement();
                 
                 // only treat the players building
                 if (!removeRest && building.getPlayer() == player)
                 {
                     // take every link to relays, solar stations or buildings
                     // with no links
                     if (building instanceof Relay || building instanceof SolarStation)
                     {
                         removeRest = !link.isCollision();
                         continue;
                     }
                 }
                 
                 iterator.remove();
             }
         }
     }
     
     private void computeLinksToMineralPlanets()
     {
         final Mine mine = (Mine) buildingToBePlaced;
         
         // create links to mineral planets that are reachable
         for (MineralPlanet planet : gameState.getMap().getMineralPlanets())
         {
             if (mine.canMine(planet))
             {
                 final Vector p1 = buildingToBePlaced.getPosition();
                 final Vector p2 = planet.getPosition();
                 final Line2D line = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
                 final boolean collision = checkCollision(line, planet);
                 
                 linksToMineralPlanets.add(new Link<>(planet, line, collision));
             }
         }
     }
     
     /**
      * Checks whether a specified element collides with another game element.
      * 
      * @param element element that should be tested on collisions
      * @return <code>true</code> if there was a collision
      */
     private boolean checkCollision(GameElement element)
     {
         // check collision with mineral planets
         for (MineralPlanet m : gameState.getMap().getMineralPlanets())
         {
             if (element.doesCollideWith(m)) { return true; }
         }
         
         // check collision with home planets
         for (Player p : gameState.getPlayers())
         {
             if (element.doesCollideWith(p.getHomePlanet())) { return true; }
         }
         
         // check collision with buildings
         for (Building b : gameState.getBuildings())
         {
             if (element.doesCollideWith(b)) { return true; }
             
             // check collision with links
             for (GameElement linked : b.getLinks())
             {
                 final Vector p1 = b.getPosition();
                 final Vector p2 = linked.getPosition();
                 final Line2D line = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
                 
                 if (element.doesCollideWith(line)) { return true; }
             }
         }
         
         // no collision
         return false;
     }
     
     /**
      * Checks whether a specified line collides with another game element. The
      * game element the line is connected with is an exception.
      * 
      * @param line the line
      * @param reachableElement the game element the line is connected with
      * @return <code>true</code> if there was a collision
      */
     private boolean checkCollision(Line2D line, GameElement reachableElement)
     {
         // check collision with buildings
         for (Building b : gameState.getBuildings())
         {
             if (b != reachableElement && b.doesCollideWith(line)) { return true; }
         }
         
         // check collision with planets
         for (MineralPlanet m : gameState.getMap().getMineralPlanets())
         {
             if (m.doesCollideWith(line)) { return true; }
         }
         
         // check collision with home planets
         for (Player p : gameState.getPlayers())
         {
             if (p.getHomePlanet().doesCollideWith(line)) { return true; }
         }
         
         // no collision
         return false;
     }
     
     /**
      * Check for every building if it is on the energy net
      */
     private void checkEnergyAvailability()
     {
         final List<SolarStation> solars = new LinkedList<>();
         
         // reset checked for energy flag
         for (Building building : getGameState().getBuildings())
         {
             building.setCheckedForEngery(false);
             
             // collect solars
             if (building instanceof SolarStation)
             {
                 final SolarStation solar = (SolarStation) building;
                 solars.add(solar);
             }
         }
         
         // checks recursively
         for (SolarStation solar : solars)
         {
             checkEnergyAvailability(solar);
         }
     }
     
     /**
      * Checks recursively if a building is somehow connected to a solar station.
      * 
      * @param check the building to check
      */
     private void checkEnergyAvailability(Building check)
     {
         if (!check.isCheckedForEngery())
         {
             check.setCheckedForEngery(true);
             for (Building linked : check.getLinks())
             {
                 linked.setHasEnergy(true);
                 checkEnergyAvailability(linked);
             }
         }
     }
     
     private void updateEnergyFlow()
     {
         /*
          * TODO: winki
          * - Korrekter Energiefluss
          */
     }
     
     private void returnToHomePlanet()
     {
         Vector p = gameState.getMap().getHomePlanetPositions().get(player.getId());
         Screen.getInstance().getViewport().setCentralPosition(p.x, p.y);
     }
     
     /**
      * This method is only to test multiplayer behaviour
      */
     private void changePlayer()
     {
         if (Keyboard.getState().isKeyPressed(Key.F1))
         {
             player = gameState.getPlayers().get(0);
         }
         else if (Keyboard.getState().isKeyPressed(Key.F2))
         {
             player = gameState.getPlayers().get(1);
         }
     }
     
     private void setBuildMode()
     {
         if (Keyboard.getState().isKeyPressed(Key.R) || Keyboard.getState().isKeyPressed(Key.D1))
         {
             buildingType = BuildingType.RELAY;
         }
         else if (Keyboard.getState().isKeyPressed(Key.M) || Keyboard.getState().isKeyPressed(Key.D2))
         {
             buildingType = BuildingType.MINE;
         }
         else if (Keyboard.getState().isKeyPressed(Key.S) || Keyboard.getState().isKeyPressed(Key.D3))
         {
             buildingType = BuildingType.SOLAR;
         }
         else if (Keyboard.getState().isKeyPressed(Key.L) || Keyboard.getState().isKeyPressed(Key.D4))
         {
             buildingType = BuildingType.LASER_CANON;
         }
         else if (Keyboard.getState().isKeyPressed(Key.Y) || Keyboard.getState().isKeyPressed(Key.D5))
         {
             buildingType = BuildingType.SHIPYARD;
         }
         else if (Keyboard.getState().isKeyPressed(Key.ESCAPE))
         {
             buildingType = BuildingType.NOTHING;
             buildingToBePlaced = null;
             selected = null;
         }
     }
     
     private void select()
     {
         // check if mouse is on building/planet
         if (Mouse.getState().isButtonPressed(Button.LEFT))
         {
             final Vector mousescreen = Mouse.getState().getVector();
             final Vector mouseworld = Screen.getInstance().getViewport().transformScreenToWorld(mousescreen);
             
             for (GameElement element : gameState.getBuildings())
             {
                 if (element.isHit(mouseworld))
                 {
                     selected = element;
                     return;
                 }
             }
             
             for (GameElement element : gameState.getMap().getMineralPlanets())
             {
                 if (element.isHit(mouseworld))
                 {
                     selected = element;
                     return;
                 }
             }
             
             HomePlanet homePlanet = player.getHomePlanet();
             if (homePlanet.isHit(mouseworld))
             {
                 selected = homePlanet;
                 return;
             }
             
             // select nothing
             selected = null;
         }
     }
     
     private void build()
     {
         if (buildingType != BuildingType.NOTHING)
         {
             // deselct
             selected = null;
             
             final Vector mouse = Mouse.getState().getVector();
             final Vector position = Screen.getInstance().getViewport().transformScreenToWorld(mouse);
             
             // prebuild object that should be placed
             switch (buildingType)
             {
                 case RELAY:
                     buildingToBePlaced = new Relay(player, position);
                     break;
                 
                 case MINE:
                     buildingToBePlaced = new Mine(player, position);
                     break;
                 
                 case SOLAR:
                     buildingToBePlaced = new SolarStation(player, position);
                     break;
                 
                 case LASER_CANON:
                     buildingToBePlaced = new LaserCanon(player, position);
                     break;
                 
                 case SHIPYARD:
                     buildingToBePlaced = new Shipyard(player, position);
                     break;
                 
                 default:
                     break;
             }
             
             // check collisions
             buildingIsPlaceable = !checkCollision(buildingToBePlaced);
             buildingToBePlaced.setPlaceable(buildingIsPlaceable);
             
             if (buildingIsPlaceable)
             {
                 // calculate connections
                 computeLinksToBuildings();
                 
                 // can build mines only if there is minimum one mineral planet
                 // reachable
                 if (buildingToBePlaced instanceof Mine && linksToMineralPlanets.isEmpty())
                 {
                     buildingIsPlaceable = false;
                     buildingToBePlaced.setPlaceable(buildingIsPlaceable);
                     return;
                 }
                 
                 // effectively build
                 if (Mouse.getState().isButtonReleased(Button.LEFT))
                 {
                     // place this building
                     buildingToBePlaced.place();
                     gameState.getBuildings().add(buildingToBePlaced);
                     
                     // add links
                     for (Link<Building> link : linksToBuildings)
                     {
                         if (!link.isCollision())
                         {
                             final Building building = link.getLinkedElement();
                             buildingToBePlaced.getLinks().add(building);
                             building.getLinks().add(buildingToBePlaced);
                         }
                     }
                     
                     buildingToBePlaced = null;
                 }
             }
         }
     }
     
     private void scroll()
     {
         final Vector m = Mouse.getState().getVector();
         
         if (/*buildingType == BuildingType.NOTHING &&*/Mouse.getState().isButtonDragged(Button.LEFT))
         {
             final int dx = Mouse.getState().getDeltaX();
             final int dy = Mouse.getState().getDeltaY();
             
             Screen.getInstance().getViewport().move(dx, dy);
         }
         else if (Mouse.getState().isButtonDown(Button.RIGHT))
         {
             if (Mouse.getState().isButtonPressed(Button.RIGHT))
             {
                 scrollPosition.set(m.x, m.y);
             }
             
             final int DIVISOR = 10;
             final Vector delta = m.sub(scrollPosition);
             final int dx = delta.x / DIVISOR;
             final int dy = delta.y / DIVISOR;
             
             Screen.getInstance().getViewport().move(-dx, -dy);
         }
     }
     
     // TODO: make a copy of the gamestate before rendering. If not, there can
     // appear ConcurrentModificationExceptions because two threads (game thread
     // and awt thread) iterate over the same list at the same time
     @Override
     public void render(Graphics2D g)
     {
         {
             // render stars in the background
             renderStars(g);
         }
         // add viewport translation and scale to the world rendering
         final AffineTransform viewport = Screen.getInstance().getViewport().getWorldToScreenTransform();
         final AffineTransform original = g.getTransform();
         g.setTransform(viewport); // render world relative to viewport
         {
             // render world (game state, map...)
             renderWorld(g);
         }
         g.setTransform(original); // reset transform
         {
             // render heads up display
             renderHud(g);
             
             if (DEBUG)
             {
                 renderDebug(g);
             }
         }
     }
     
     private void renderStars(Graphics2D g)
     {
         // render stars
         final float TRANSPARENCY = 0.4f;
         final Composite original = g.getComposite();
         g.setColor(Color.WHITE);
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, TRANSPARENCY));
         for (Star star : stars)
         {
             final int DEEP_DELTA = 2;
             final int DEEP_FACTOR = 1;
             final double FACTOR = 0.5;
             final int SIZE = (int) ((gameState.getMap().getNumLayers() - star.getLayer()) * FACTOR);
             
             final Vector o = Screen.getInstance().getViewport().getOriginPosition();
             final int screenw = Screen.getInstance().getSize().width;
             final int screenh = Screen.getInstance().getSize().height;
             
             int x = (o.x / (1 + DEEP_DELTA + star.getLayer() * DEEP_FACTOR) + star.getPosititon().x - SIZE / 2) % screenw;
             int y = (o.y / (1 + DEEP_DELTA + star.getLayer() * DEEP_FACTOR) + star.getPosititon().y - SIZE / 2) % screenh;
             if (x < 0) x += screenw;
             if (y < 0) y += screenh;
             
             g.fillOval(x, y, SIZE, SIZE);
         }
         g.setComposite(original);
     }
     
     private void renderWorld(Graphics2D g)
     {
         // render connection lines between the buildings
         if (buildingToBePlaced != null && buildingIsPlaceable)
         {
             g.setColor(Color.RED);
             for (Link<Building> link : linksToBuildings)
             {
                 final Line2D line = link.getLine();
                 
                 g.setColor(link.isCollision() ? Color.RED : Color.WHITE);
                 g.draw(line);
             }
             
             if (!linksToMineralPlanets.isEmpty())
             {
                 final int STROKE_WIDTH = 2;
                 final Stroke stroke = g.getStroke();
                 g.setStroke(new BasicStroke(STROKE_WIDTH));
                 g.setColor(Color.GREEN);
                 for (Link<MineralPlanet> link : linksToMineralPlanets)
                 {
                     final Line2D line = link.getLine();
                     
                     g.draw(line);
                 }
                 g.setStroke(stroke);
             }
         }
         
         // render game state
         gameState.render(g);
         
         // render building thats should be built
         if (buildingToBePlaced != null)
         {
             final float TRANSPARENCY = 0.4f;
             
             Composite original = g.getComposite();
             g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, TRANSPARENCY));
             buildingToBePlaced.render(g);
             g.setComposite(original);
         }
     }
     
     /**
      * Renders the heads up display.
      * 
      * @param g the {@code Graphics2D} object
      */
     private void renderHud(Graphics2D g)
     {
         /*
          *  TODO: kai
          *  
          *  - Anzeige der Ressourcen: Energie, Mineralien
          *  - Anzeige des aktuell ausgewhlten GameElements (Tipp: getSelected())
          *  - Siehe The Space Game
          *  
          */
         
         Dimension dimension = Screen.getInstance().getSize();
         @SuppressWarnings("unused")
         int minerals = player.getMinerals();
         @SuppressWarnings("unused")
         int score = player.getScore();
         int hudX = (int) dimension.getWidth() - 501;
         int hudY = (int) dimension.getHeight() - 101;
         int maxEnergy = 0;
         
         for (Building building : getGameState().getBuildings())
         {
             if (building instanceof SolarStation)
             {
                 maxEnergy += ((SolarStation) building).getMaxEnergy();
                 System.out.println(player.getEnergy());
                 System.out.println("player max ener " + player.getMaxEnergy());
             }
         }
         
         player.setMaxEnergy(maxEnergy);
         
         g.setColor(Color.white);
         
         g.drawRect(hudX, hudY, 500, 100);
         g.drawRect(hudX + 10, hudY + 10, 95, 20);
         if (maxEnergy != 0)
         {
             g.fillRect(hudX + 10, hudY + 10, (int) (100.0 / maxEnergy * player.getEnergy()), 20);
         }
         
     }
     
     /**
      * Renders the debug information.
      * 
      * @param g the {@code Graphics2D} object
      */
     private void renderDebug(Graphics2D g)
     {
         // render debug info
         final int DX = 10;
         final int DY = 22;
         final int LINE_HEIGHT = 14;
         
         g.setColor(Color.WHITE);
         g.drawString("FPS: " + getGameTime().getFrameRate(), DX, 0 * LINE_HEIGHT + DY);
         g.drawString("Mouse: " + Mouse.getState().getX() + ", " + Mouse.getState().getY(), DX, 1 * LINE_HEIGHT + DY);
         g.drawString("Mouse delta: " + Mouse.getState().getDeltaX() + ", " + Mouse.getState().getDeltaY(), DX, 2 * LINE_HEIGHT + DY);
         g.drawString("Viewport origin: " + Screen.getInstance().getViewport().getOriginPosition().x + ", " + Screen.getInstance().getViewport().getOriginPosition().y, DX, 3 * LINE_HEIGHT + DY);
         g.drawString("Viewport central: " + Screen.getInstance().getViewport().getCentralPosition().x + ", " + Screen.getInstance().getViewport().getCentralPosition().y, DX, 4 * LINE_HEIGHT + DY);
         g.drawString("Buildingtype: " + buildingType, 10, 5 * LINE_HEIGHT + DY);
         g.drawString("Running slowly: " + getGameTime().isRunningSlowly(), DX, 6 * LINE_HEIGHT + DY);
         g.drawString("Ticks: " + getGameTime().getTicks(), DX, 7 * LINE_HEIGHT + DY);
         g.drawString("Amount of Minerals: " + player.getMinerals(), 10, 8 * LINE_HEIGHT + DY);
         g.drawString("Amount of Energy: " + player.getEnergy(), 10, 9 * LINE_HEIGHT + DY);
     }
 }
