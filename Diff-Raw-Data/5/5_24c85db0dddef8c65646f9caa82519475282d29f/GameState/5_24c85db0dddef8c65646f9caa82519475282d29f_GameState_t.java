 package vooga.rts.state;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observer;
 import vooga.rts.action.InteractiveAction;
 import vooga.rts.commands.Command;
 import vooga.rts.commands.DragCommand;
 import vooga.rts.controller.Controller;
 import vooga.rts.gamedesign.sprite.gamesprites.Projectile;
 import vooga.rts.gamedesign.sprite.gamesprites.Resource;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.buildings.Building;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.buildings.Garrison;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.units.Soldier;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.units.Unit;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.units.Worker;
 import vooga.rts.gamedesign.state.DetectableState;
 import vooga.rts.gamedesign.strategy.production.CanProduce;
 import vooga.rts.gamedesign.weapon.Weapon;
 import vooga.rts.gui.menus.GameMenu;
 import vooga.rts.map.GameMap;
 import vooga.rts.player.HumanPlayer;
 import vooga.rts.player.Player;
 import vooga.rts.player.Team;
 import vooga.rts.resourcemanager.ResourceManager;
 import vooga.rts.util.Camera;
 import vooga.rts.util.DelayedTask;
 import vooga.rts.util.FrameCounter;
 import vooga.rts.util.Information;
 import vooga.rts.util.Location;
 import vooga.rts.util.Location3D;
 import vooga.rts.util.Pixmap;
 import vooga.rts.util.PointTester;
 
 /**
  * The main model of the game. This keeps track of all the players, the
  * humanplayer associated with the local game, plus the map.
  * 
  * @author Challen Herzberg-Brovold
  * 
  */
 
 public class GameState extends SubState implements Controller {
 
 	private final static int DEFAULT_NODE_SIZE = 8;
 	private Map<Integer, Team> myTeams;
 	private static GameMap myMap;
 	private HumanPlayer myHumanPlayer;
 	private List<Player> myPlayers;
 	// private Resource r;
 	// private Building building;
 	// private UpgradeBuilding upgradeBuilding;
 	private PointTester pt;
 
 	private FrameCounter myFrames;
 
 	private Rectangle2D myDrag;
 	private Resource r;
 
 	public GameState(Observer observer) {
 		super(observer);
 		myTeams = new HashMap<Integer, Team>();
 		myPlayers = new ArrayList<Player>();
 		// myMap = new GameMap(8, new Dimension(512, 512));
 		pt = new PointTester();
 		myFrames = new FrameCounter(new Location(100, 20));
 		setupGame();
 	}
 
 	@Override
 	public void update(double elapsedTime) {
 		myMap.update(elapsedTime);
 
 		for (Player p : myPlayers) {
 			p.update(elapsedTime);
 		}
 
 		yuckyUnitUpdate(elapsedTime);
 
 		myFrames.update(elapsedTime);
 	}
 
 	@Override
 	public void paint(Graphics2D pen) {
 		pen.setBackground(Color.BLACK);
 		myMap.paint(pen);
 		r.paint(pen);
 		// a bit odd, but we need to paint the other players before we paint
 		// HumanPlayer because
 		// HumanPlayer contains the gameMenu
 		for (Player p : myPlayers) {
 			if (!(p instanceof HumanPlayer)) {
 				p.paint(pen);
 			}
 		}
 		myHumanPlayer.paint(pen);
 		if (myDrag != null) {
 			pen.draw(myDrag);
 			// pen.draw(worldShape);
 		}
 		Camera.instance().paint(pen);
 		myFrames.paint(pen);
 
 	}
 
 	@Override
 	public void receiveCommand(Command command) {
 		// If it's a drag, we need to do some extra checking.
 		if (command instanceof DragCommand) {
 			myDrag = ((DragCommand) command).getScreenRectangle();
 			if (myDrag == null) {
 				return;
 			}
 		}
 		sendCommand(command);
 	}
 
 	@Override
 	public void sendCommand(Command command) {
 		myHumanPlayer.sendCommand(command);
 	}
 
 	/**
 	 * Adds a player to the game
 	 * 
 	 * @param player
 	 *            to add
 	 * @param teamID
 	 *            of the player.
 	 */
 	public void addPlayer(Player player, int teamID) {
 		myPlayers.add(player);
 		if (myTeams.get(teamID) == null) {
 			addTeam(teamID);
 		}
 		myTeams.get(teamID).addPlayer(player);
 	}
 
 	public void addTeam(int teamID) {
 		myTeams.put(teamID, new Team(teamID));
 	}
 
 	public void addPlayer(int teamID) {
 		Player result;
 		if (myPlayers.size() == 0) {
 			myHumanPlayer = new HumanPlayer(teamID);
 			result = myHumanPlayer;
 		} else {
 			result = new Player(teamID);
 		}
 		addPlayer(result, teamID);
 	}
 
 	private DelayedTask test;
 	private DelayedTask occupyPukingTest;
 
 	public void setupGame() {
 		addPlayer(1);
 		Unit worker = new Worker(new Pixmap(ResourceManager.getInstance()
 				.<BufferedImage> getFile("images/scv.gif", BufferedImage.class)),
 				new Location3D(100, 100, 0), new Dimension(75, 75), null, 1,
 				200, 40, 5);
 		Information i1 = new Information("Worker", "I am a worker. I am sent down from Denethor, son of Ecthelion ", null, "images/scv.png");
 		worker.setInfo(i1);
 		myHumanPlayer.add(worker);
 		
 		Unit a = new Soldier();
 		Projectile proj = new Projectile(new Pixmap(ResourceManager
 				.getInstance().<BufferedImage> getFile("images/bullet.png",
 						BufferedImage.class)), a.getWorldLocation(),
 				new Dimension(30, 30), 2, 10, 6);
 		a.getAttackStrategy().addWeapons(
 				new Weapon(proj, 400, a.getWorldLocation(), 1));
 		Information i2 = new Information("Marine", "I am a soldier of Nunu.", null, "buttons/marine.png");
		a.setInfo(i2);
 		myHumanPlayer.add(a);
 		addPlayer(2);
 		
 		Unit c = new Soldier(new Location3D(1200, 500, 0), 2);
 		c.setHealth(150);
 		// myHumanPlayer.add(c);
 		myPlayers.get(1).add(c);
 		
 		Building b = new Building(new Pixmap(ResourceManager.getInstance()
 				.<BufferedImage> getFile("images/factory.png", BufferedImage.class)),
 				new Location3D(700, 700, 0), new Dimension(100, 100), null, 1,
 				300, InteractiveEntity.DEFAULT_BUILD_TIME);
 		b.setProductionStrategy(new CanProduce());
 		((CanProduce) b.getProductionStrategy()).addProducable(new Soldier());
 		((CanProduce) b.getProductionStrategy()).createProductionActions(b);
 		((CanProduce) b.getProductionStrategy()).setRallyPoint(new Location3D(
 				600, 500, 0));
		Information i = new Information("Barracks", "This is a barracks that can make awesome pies", null, "buttons/marine.png");
 		b.setInfo(i);
 		System.out.println(b.getInfo().getName());
 		myHumanPlayer.add(b);
 
 		Garrison garrison = new Garrison(new Pixmap(ResourceManager
 				.getInstance().<BufferedImage> getFile("images/barracks.jpeg",
 						BufferedImage.class)), new Location3D(300, 300, 0),
 				new Dimension(100, 100), null, 1, 300,
 				InteractiveEntity.DEFAULT_BUILD_TIME);
 		garrison.getOccupyStrategy().addValidClassType(new Soldier());
 		garrison.getOccupyStrategy().createOccupyActions(garrison);
 		myHumanPlayer.add(garrison);
 
 		myMap = new GameMap(8, new Dimension(512, 512));
 
 		r = new Resource(new Pixmap(ResourceManager.getInstance()
 				.<BufferedImage> getFile("images/mineral.gif", BufferedImage.class)),
 				new Location3D(200, 300, 0), new Dimension(50, 50), 0, 200,
 				"mineral");
 
 		final Building f = b;
 		test = new DelayedTask(3, new Runnable() {
 			@Override
 			public void run() {
 				f.getAction((new Command("I am a pony"))).apply();
 				test.restart();
 			}
 		});
 
 		final Garrison testGarrison = garrison;
 		occupyPukingTest = new DelayedTask(10, new Runnable() {
 			@Override
 			public void run() {
 				if (testGarrison.getOccupyStrategy().getOccupiers().size() > 0) {
 					System.out.println("will puke!");
 					testGarrison.getAction(new Command("deoccupy")).apply();
 				}
 				occupyPukingTest.restart();
 			}
 		});
 	}
 
 	private void yuckyUnitUpdate(double elapsedTime) {
 		List<InteractiveEntity> p1 = myTeams.get(1).getUnits();
 		List<InteractiveEntity> p2 = myTeams.get(2).getUnits();
 		for (InteractiveEntity u1 : p1) {
 			if (u1 instanceof Worker && r != null) {
 				((Worker) u1).gather(r);
 			}
 			for (InteractiveEntity u2 : p2) {
 				u2.getAttacked(u1);
 				u1.getAttacked(u2);
 			}
 		}
 		r.update(elapsedTime);
 		// }
 		test.update(elapsedTime);
 		// now even yuckier
 		for (int i = 0; i < p1.size(); ++i) {
 			if (p1.get(i) instanceof Unit) {
 				for (int j = i + 1; j < p1.size(); ++j) {
 					((Unit) p1.get(i)).occupy(p1.get(j));
 				}
 			}
 		}
 		occupyPukingTest.update(elapsedTime);
 	}
 
 	public static GameMap getMap() {
 		return myMap;
 	}
 }
