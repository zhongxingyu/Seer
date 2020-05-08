 package blueMaggot;
 
 import gfx.GameOverlay;
 import inputhandler.InputHandler;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import entity.Tank;
 import entity.weapon.Gun;
 
 import networking.ConnectionDelegate;
 import networking.ConnectionManager;
 import networking.NetworkObject;
 import networking.OnlineCityScape;
 
 import baseGame.BaseGame;
 import baseGame.Rendering.Renderer;
 import blueMaggot.maps.cityScape;
 
 public class Game extends BaseGame implements ConnectionDelegate {
 	private GameListener gameListener;
 	public boolean didTick = false;
 	public static int ALPHA_MASK = -1;
 	private GameOverlay overlay;
 	// network stuff
 	public boolean online = false;
 	public ArrayList<byte[]> keyStrokes;
 	public byte[] keyStrokestoDo;
 	private OnlineCityScape onlineLevel;
 	private ConnectionManager connection;
 
 	public BlueMaggot blueMaggot;
 	private InputHandler handler = new InputHandler();
 
 	public DecimalFormat formater;
 	public cityScape level;
 
 	public Game() {
 		handler = new InputHandler();
 		formater = new DecimalFormat("#00000");
 
 		addKeyListener(handler);
 		// DatagramPacket e;
 	}
 
 	public Game(BlueMaggot blueMaggot) {
 		gameListener = blueMaggot;
 		overlay = new GameOverlay();
 		this.blueMaggot = blueMaggot;
 		blueMaggot.inputReal = handler;
 
 		formater = new DecimalFormat("#00000");
 		addKeyListener(handler);
 	}
 
 	@Override
 	public void onUpdate(double deltaTime) {
 
 		handler.tick(deltaTime);
 		deltaTime *= 0.0625;
 
 		if (!(GameState.getInstance().isPaused() || GameState.getInstance().isGameOver())) {
 			level.tick(deltaTime);
 			didTick = true;
 		}
 	}
 
 	@Override
 	public void onDraw(Renderer renderer) {
 
 		if (blueMaggot != null)
 			blueMaggot.tick(renderer.getGraphics());
 
 		// renderer.clearAllPixelData(Color.WHITE.getRGB());
 		level.onDraw(renderer);
 	}
 
 	@Override
 	public void onUppdateUI(Renderer renderer) {
 
 		if (GameState.getInstance().getPlayers() == null || GameState.getInstance().getPlayers().size() < 2)
 			return;
 
 		GameState state = GameState.getInstance();
 		Tank player1 = state.players.get(0);
 		Tank player2 = state.players.get(1);
 
 		// if ((level != null && state.getPlayers().size() > 1 &&
 		// overlay.needUppdate(player1.getScore(),
 		// player2.getScore(), player1.getLife(), player2.getLife(),
 		// player1.getCurrentWeapon(),
 		// player2.getCurrentWeapon()))) {
 		overlay.paintOverlay(renderer.getGraphics(), player1.getScore(), player2.getScore(), player1.getLife(), player2.getLife(), player1.getCurrentWeapon(),
 				player2.getCurrentWeapon());
 
 		// }
 	}
 
 	// }
 
 	public void startReuglarGame() {
 		GameState.getInstance().init();
 		System.out.println("starting level: " + GameState.getInstance().getSelectedLevelBackground().getName().split("_")[0]);
 		level = new cityScape(this, handler);
 		level.init();
 
 		init(GameState.getInstance().getWidth(), GameState.getInstance().getHeight(), 60);
 		GameState.getInstance().setRunning(true);
 
 	}
 
 	/* network stuff */
 	public void initConnection(boolean isHost, String addr) {
 
 		GameState.getInstance().init();
 		if(level != null)
 			level = null;
 		
 		System.out.println("initiating connection");
 
 		if (connection != null){
 			connection.endConnection();
 		}
 
 		connection = new ConnectionManager(this);
 		if (isHost) {
 			GameState.getInstance().setPlayerNumber(2);
 			connection.becomeHost();
 			GameState.getInstance().setHost(true);
 		} else {
 			GameState.getInstance().setPlayerNumber(1);
 			connection.joinGame(addr);
 			GameState.getInstance().setHost(false);
 		}
 	}
 
 	@Override
 	public void connectionFailed(String message) {
 		System.out.println("connection failed: " + message);
 		gameListener.ConnectionFailed(message);
 	}
 
 	@Override
 	public void readData(byte[] data) {
 
 		String gameData = new String(data);
 		String[] parts = gameData.split("\\@");
 
 		String gameState = parts[0];
 		String[] properties = gameState.split("\\'");
 		// System.out.println("gamestate: " + gameState);
 		Tank player1;
 		Tank player2;
 
 		if (level != null && level.getPlayers().size() > 1) {
 			GameState state = GameState.getInstance();
 			int score1 = Integer.parseInt(properties[2]);
 			int score2 = Integer.parseInt(properties[0]);
 			int life1 = Integer.parseInt(properties[3]);
 			int life2 = Integer.parseInt(properties[1]);
 
 			Gun gun1 = Gun.valueOf(properties[4]);
 			Gun gun2 = Gun.valueOf(properties[5]);
 
 			String nick1 = properties[6];
 			String nick2 = properties[7];
 			boolean gameOver = false;
 			if(!state.isHost())
 			gameOver = Boolean.parseBoolean(properties[8]);
 			
 			if(gameOver)
 				state.setGameOver(gameOver);
 			
 			if (!state.isHost()) {
 
 				player2 = GameState.getInstance().players.get(1);
 				player2.setScore(score1);
 				player2.setLife(life2);
 				player2.setCurrentWeapon(gun2);
				state.setPlayer2Nick(nick2);
 
 			} else {
 				player2 = GameState.getInstance().players.get(0);
 				player2.setScore(score1);
 				player2.setLife(life1);
 				player2.setCurrentWeapon(gun1);
				state.setPlayer1Nick(nick1);
 			}
 
 			// state.setPlayers(level.getPlayers());
 
 		}
 
 		if (data.length > 0 && parts.length>0) {
 
 			onlineLevel.catchResponse(parts[1]);
 
 		}
 
 	}
 
 	@Override
 	public byte[] onWrite() {
 
 		GameState state = GameState.getInstance();
 		int index1 = 0;
 		int index2 = 0;
 		if (state.getPlayerNumber() == 1)
 			index1 = 1;
 		else
 			index2 = 1;
 
 		String gameState = "";
 
 		if (state.getPlayers() != null && state.getPlayers().size() > 1)
 			gameState = state.getPlayers().get(index1).getScore() + "'" + state.getPlayers().get(index2).getLife() + "'"
 					+ state.getPlayers().get(index2).getScore() + "'" + state.players.get(index2).getLife() + "'" + state.players.get(0).getCurrentWeaponName()
 					+ "'" + state.players.get(1).getCurrentWeaponName() + "'" + state.getPlayer2Nick() + "'" + state.getPlayer1Nick() ;
 
 		if(GameState.getInstance().isHost())
 			gameState += "'" + GameState.getInstance().isGameOver();
 		gameState += "@";
 
 		String msgBody = "";
 
 		List<NetworkObject> objects = onlineLevel.getNetworkObjectList();
 		List<Integer> deadKeys = new ArrayList<Integer>();
 
 		for (int i = 0; i < objects.size(); i++) {
 
 			NetworkObject obj = objects.get(i);
 			if (obj.shouldBeSent()) {
 				String objectString = obj.getObject();
 
 				if (objectString != null && !objectString.equals(""))
 					;
 				msgBody += "?" + objectString;
 
 				if (obj.isRemoved()) {
 					deadKeys.add(obj.getId());
 					onlineLevel.getNetworkObjectList().remove(i);
 					i--;
 				}
 			}
 		}
 		for (Integer key : deadKeys) {
 			onlineLevel.getNetworkObjects().remove(key);
 		}
 		String msgHeader = "1" + to5DigitString(msgBody.length() + gameState.length());
 		return (msgHeader + gameState + msgBody).getBytes();
 	}
 
 	private String to5DigitString(double x) {
 		if (x >= 0) {
 			return formater.format(x);
 		} else {
 			return "-" + formater.format(Math.abs(x)).substring(1);
 		}
 	}
 
 	@Override
 	public boolean shouldRead() {
 		return onlineLevel != null;
 	}
 
 	@Override
 	public boolean shouldWrite() {
 		boolean temp = didTick;
 		didTick = false;
 		return (onlineLevel != null) && temp && onlineLevel.getNetworkObjects().size() > 0;
 	}
 
 	@Override
 	public void startOnlineGame() {
 		GameState.getInstance().init();
 		onlineLevel = new OnlineCityScape(this, handler);
 		onlineLevel.init();
 		level = onlineLevel;
 		init(GameState.getInstance().getWidth(), GameState.getInstance().getHeight(), 60);
 		GameState.getInstance().setRunning(true);
 		GameState.getInstance().setPaused(false);
 		GameState.getInstance().setGameOver(false);
 		GameState.getInstance().players = level.getPlayers();
 	}
 
 	@Override
 	public void setLevel(String terrain, String backGround) {
 		System.out.println(terrain + " " + backGround);
 		GameState state = GameState.getInstance();
 		File terrainFile = null;
 		File backGroundFile = null;
 		if ((terrainFile = new File(terrain)).exists() && (backGroundFile = new File(backGround)).exists()) {
 			state.setSelectedLevelTerrain(new File(terrain));
 			state.setSelectedLevelBackground(new File(backGround));
 		} else {
 			gameListener.ConnectionFailed("Could not find level file. Terrain:" + terrainFile.getName() + " Background:" + backGroundFile.getName());
 
 		}
 	}
 }
