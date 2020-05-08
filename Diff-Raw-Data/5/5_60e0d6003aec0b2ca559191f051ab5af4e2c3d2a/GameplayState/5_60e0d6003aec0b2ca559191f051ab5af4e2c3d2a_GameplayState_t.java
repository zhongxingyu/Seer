 package org.ssg.Cambridge;
 
 import java.util.ArrayList;
 import java.io.*;
 import java.net.MalformedURLException;
 
 import net.java.games.input.Controller;
 
 import org.ini4j.*;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.AngelCodeFont;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import paulscode.sound.SoundSystem;
 import paulscode.sound.SoundSystemConfig;
 
 public class GameplayState extends BasicGameState implements KeyListener {
 	private GlobalData data;
 
 	final String RESDIR = "res/";
 	
 	final String TENNIS = "TENNIS";
 	final String SQUASH = "SQUASH";
 	final String SOCCER = "SOCCER";
 	final String HOCKEY = "HOCKEY";
 	
 	public int stateID = 1;
 	
 	int gameType;
 	int NUMGAMES;
 	String NAME;
 	float gameStartCountdown;
 	float GAMESTARTCOUNTDOWN = 2400f;
 	int gameStartStage;//3 2 1 0 Used for playing the boop boop BEEP sound at the start
 	//-1 for unlimited
 //	int scoreLimit = 1;
 //	int timeLimit = 10000;//in ms
 	float timef;
 	int time;
 	int GAMEOVERCOUNTDOWN = 2000;
 	int gameOverCountdown;
 	int gameOverStage;//unused currently, might want a final horn
 	
 	Image tempImage;
 	
 	public float SCREENWIDTH;
 	public float SCREENHEIGHT;
 	public int ACTIONCAM;
 	public float maxZoom;
 	public float camWidth, camHeight;//Dimensions of the camera box
 	
 	public int FIELDWIDTH;// = 1600;
 	public int FIELDHEIGHT;// = 900;
 	public int FULLCOURT;// = 1;//1 is full court, 0 is halfs
 	public int GOALSIZE;
 	public int GOALTYPE;
 	public float KICKRANGE;// = 50f;
 	public float BALLSIZE = 20f;
 	public int postWidth;//Is actually the size of goalie box
 	public Goal[] goals;
 	public Color[] teamColors;
 	
 	private int minX, minY;//The top left bounds of active objects
 	private int maxX, maxY;// bottom right bounds
 	float tempX, tempY;//Width and height of camera 'box'
 	private float targetViewX, targetViewY;
 	private float viewX, viewY;//Top left corner of the camera
 	private float scaleFactor, targetScaleFactor;
 	private int boundingWidth = 100;
 	private boolean shouldRender;
 	private float scoreBarAlpha;
 	
 	boolean started;//Has the game started/the intro animation ended
 	
 	private float[][][] teamPositions;
 	private float[][] playerStartPositions;
 	private int[][] playerCharacters;//Passed to gameoverstate to draw what players are on each team
 	
 	AngelCodeFont font, font_white, font_small, font_large;
 	Image triangle, hemicircleL, hemicircleR, slice, slice_tri, slice_wide, slice_twin;
 	Image goalScroll1, goalScroll2, goalScroll1v, goalScroll2v, goalScroll;
 	Image goldgoal_arrow;
 	float arrowTheta, arrowThetaTarget;
 	int scrollX;//For the "GOAL" scroll
 	int scrollY;
 	int scrollXDir, scrollYDir;
 	float resetVelocity[], targetX, targetY;
 	
 	Ball ball;
 	Player p1, p2;
 	ArrayList<Player> players;
 	
 	float slowMoFactor;
 	
 	float[] kickFloat;//Unit vector to set ball velocity after kicking
 	float[] spinFloat;//vector used to store orthogonal projection of player's v on kickFloat
 	float[] tempTrailArr;//Used in trail drawing, size 4
 	
 	int centreX;
 	int centreY;
 	
 	int scores[];//Scores for p1 and p2
 	boolean scored;//Did a goal just get scored
 	
 	SoundSystem mySoundSystem;
 	CambridgeController c1, c2, c3, c4;
 	boolean c1Exist, c2Exist, c3Exist, c4Exist;
 	
 	float deltaf;
 	boolean temp;
 	float tempf;
 	float[] tempArr;
 	
 	public GameplayState(int i, boolean renderon) {
 		stateID = i;
 		shouldRender = renderon;
 	}
 	
 	public void setShouldRender(boolean shouldRender) {
 		this.shouldRender = shouldRender;
 	}
 	
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 
 		data = ((Cambridge) sbg).getData();
 		mySoundSystem = data.mySoundSystem();
 		
 		c1 = data.getC()[0];
 		c2 = data.getC()[1];
 		c3 = data.getC()[2];
 		c4 = data.getC()[3];
 		c1Exist = (c1 != null);
 		c2Exist = (c2 != null);
 		c3Exist = (c3 != null);
 		c4Exist = (c4 != null);
 		
 		try {
 			font = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0.png"));
 			font_white = new AngelCodeFont(RESDIR + "8bitoperator.fnt", new Image(RESDIR + "8bitoperator_0_white.png"));
 			font_small = new AngelCodeFont(RESDIR + "8bitoperator_small.fnt", new Image(RESDIR + "8bitoperator_small_0.png"));
 			font_large = new AngelCodeFont(RESDIR + "8bitoperator_large.fnt", new Image(RESDIR + "8bitoperator_large_0.png"));
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Fonts not loaded properly. Uh oh. Spaghettio.");
 			e.printStackTrace();
 		}
 		
 		triangle = new Image(RESDIR + "triangle.png");
 		hemicircleL = new Image(RESDIR + "hemicircleL.png");
 		hemicircleR = new Image(RESDIR + "hemicircleR.png");
 		slice = new Image(RESDIR + "slice.png");
 		slice_tri = new Image(RESDIR + "slice_tri.png");
 		slice_wide = new Image(RESDIR + "slice_wide.png");
 		slice_twin = new Image(RESDIR + "slice_twin.png");
 		goalScroll1 = new Image(RESDIR + "goal.png");
 		goalScroll2 = new Image(RESDIR + "goal_own.png");
 		goalScroll1v = new Image(RESDIR + "goal_v.png");
 		goalScroll2v = new Image(RESDIR + "goal_own_v.png");
 		goalScroll = goalScroll1;
 		goldgoal_arrow = new Image(RESDIR + "goldgoal_arrow.png");
 
 		tempImage = new Image(data.screenWidth(), data.screenHeight());//for copying the screen into and passing to the pause menu
 		
 		teamColors = new Color[] {
 				Color.white,
 				Color.white,
 				Color.white,
 				Color.white
 		};
 		
 //		initFields(gc);
 	}
 
 	public void initFields(GameContainer gc) throws SlickException {
 		
 		Ini ini, userIni;
 		
 		float[] playerConsts = new float[8];
 		float[] ballConsts = new float[3];
 		
 		gameType = data.gameType();
 		
 		try {
 			ini = new Ini(new File(RESDIR + "config.cfg"));
 			userIni = new Ini(new File(RESDIR + "user_config.cfg"));
 			
 			NUMGAMES = ini.get("CONF","NUMGAMES", int.class);
 			SCREENWIDTH = data.screenWidth();
 			SCREENHEIGHT = data.screenHeight();
 			ACTIONCAM = data.actionCam() ? 1 : 0;
 			
 			Ini.Section section = ini.get(""+gameType);
 			
 			NAME = section.get("NAME");
 			FIELDWIDTH = section.get("FIELDWIDTH", int.class);
 			FIELDHEIGHT = section.get("FIELDHEIGHT", int.class);
 			GOALSIZE = section.get("GOALSIZE", int.class);
 			postWidth = GOALSIZE+150;
 			if(postWidth>FIELDHEIGHT)
 				postWidth = FIELDHEIGHT;
 			GOALTYPE = section.get("GOALTYPE", int.class);
 			FULLCOURT = section.get("FULLCOURT", int.class);
 			KICKRANGE = section.get("KICKRANGE", float.class);
 			
 			playerConsts[0] = section.get("VELMAG", float.class);
 			playerConsts[1] = section.get("POWERVELMAG", float.class);
 			playerConsts[2] = section.get("KICKCOOLDOWN", float.class);
 			playerConsts[3] = section.get("MAXPOWER", float.class);
 			playerConsts[4] = section.get("POWERCOOLDOWN", float.class);
 			playerConsts[5] = section.get("NORMALKICK", float.class);
 			playerConsts[6] = section.get("POWERKICK", float.class);
 			playerConsts[7] = KICKRANGE;
 			
 			ballConsts[0] = section.get("CURVESCALE", float.class);
 			ballConsts[1] = section.get("BOUNCEDAMP", float.class);
 			ballConsts[2] = section.get("FLOORFRICTION", float.class);
 			
 		} catch (InvalidFileFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		minX = FIELDWIDTH/2-(int)SCREENWIDTH/2;
 		minY = FIELDHEIGHT/2-(int)SCREENHEIGHT/2;
 		maxX = FIELDWIDTH/2+(int)SCREENWIDTH/2;
 		maxY = FIELDHEIGHT/2+(int)SCREENHEIGHT/2;
 		viewX = minX;
 		viewY = minX;
 		targetViewX = viewX;
 		targetViewY = viewY;
 		scaleFactor = 1;
 		camWidth = FIELDWIDTH;
 		camHeight = FIELDHEIGHT;
 		
 		if(maxZoom == 0){
 			maxZoom = 2;
 //			if(GOALTYPE == 0 || GOALTYPE == 7){
 //				maxZoom = 2;
 //			}else{
 //				maxZoom = 1;
 //			}
 		}
 		
 		teamColors = new Color[] {
 				Color.cyan,
 				Color.orange,
 				Color.magenta,
 				Color.green
 		};
 		
 		arrowTheta = 0;
 		arrowThetaTarget = 0;
 		
 		int randomNum = (int)(Math.random()*2);
 		initGoals(randomNum);
 		
 		// Creating the ball before the players
 		ball = new Ball(0, ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem);
 		
 		int[] team1lim, team2lim;
 		if(FULLCOURT==1){
 			team1lim = new int[]{0,FIELDWIDTH, 0, FIELDHEIGHT};
 			team2lim = team1lim;
 		}else {
 			team1lim = new int[]{0,FIELDWIDTH/2, 0, FIELDHEIGHT};
 			team2lim = new int[]{FIELDWIDTH/2, FIELDWIDTH, 0, FIELDHEIGHT};
 		}
 		
 		if(NAME.equals(TENNIS)){
 			teamPositions = new float[2][][];
 			teamPositions[0] = new float[][] {
 					{ 80, FIELDHEIGHT/2 },
 					{ 80, FIELDHEIGHT/2 - 150 },
 					{ 80, FIELDHEIGHT/2 + 150 },
 			};
 			teamPositions[1] = new float[][] {
 					{ FIELDWIDTH-80, FIELDHEIGHT/2 },
 					{ FIELDWIDTH-80, FIELDHEIGHT/2 - 150 },
 					{ FIELDWIDTH-80, FIELDHEIGHT/2 + 150 },
 			};
 		}else if(NAME.equals("GOLDENGOAL")){
 			teamPositions = new float[4][][];
 			teamPositions[0] = new float[][] {
 					{ FIELDWIDTH/4, FIELDHEIGHT/2},
 					{ FIELDWIDTH/4, FIELDHEIGHT/2 -150},
 					{ FIELDWIDTH/4, FIELDHEIGHT/2 +150},
 			};
 			teamPositions[1] = new float[][]{
 					{ FIELDWIDTH*3/4, FIELDHEIGHT/2},
 					{ FIELDWIDTH*3/4, FIELDHEIGHT/2-150},
 					{ FIELDWIDTH*3/4, FIELDHEIGHT/2+150},
 			};
 			teamPositions[2] = new float[][]{
 					{ FIELDWIDTH/2, FIELDHEIGHT/4},
 					{ FIELDWIDTH/2-150, FIELDHEIGHT/4},
 					{ FIELDWIDTH/2+150, FIELDHEIGHT/4},
 			};
 			teamPositions[3] =  new float[][]{
 					{ FIELDWIDTH/2, FIELDHEIGHT*3/4},
 					{ FIELDWIDTH/2-150, FIELDHEIGHT*3/4 },
 					{ FIELDWIDTH/2+150, FIELDHEIGHT*3/4 },
 			};
 		}else if (NAME.equals("FOURSQUARE")){
 			teamPositions = new float[4][][];
 			teamPositions[0] = new float[][] {
 					{ FIELDWIDTH/6, FIELDHEIGHT/6},
 					{ FIELDWIDTH/6 + 150, FIELDHEIGHT/6},
 					{ FIELDWIDTH/6, FIELDHEIGHT/6 +150},
 			};
 			teamPositions[1] = new float[][]{
 					{ FIELDWIDTH*5/6, FIELDHEIGHT*5/6},
 					{ FIELDWIDTH*5/6-150, FIELDHEIGHT*5/6},
 					{ FIELDWIDTH*5/6, FIELDHEIGHT*5/6-150},
 			};
 			teamPositions[2] = new float[][]{
 					{ FIELDWIDTH*5/6, FIELDHEIGHT/6},
 					{ FIELDWIDTH*5/6, FIELDHEIGHT/6+150},
 					{ FIELDWIDTH*5/6-150, FIELDHEIGHT/6},
 			};
 			teamPositions[3] =  new float[][]{
 					{ FIELDWIDTH/6, FIELDHEIGHT*5/6},
 					{ FIELDWIDTH/6, FIELDHEIGHT*5/6-150},
 					{ FIELDWIDTH/6+150, FIELDHEIGHT*5/6},
 			};
 		}else{
 			teamPositions = new float[2][][];
 			teamPositions[0] = new float[][] {
 					{ FIELDWIDTH/2-250, FIELDHEIGHT/2 },
 					{ FIELDWIDTH/2-250, FIELDHEIGHT/2 - 100 },
 					{ FIELDWIDTH/2-250, FIELDHEIGHT/2 + 100 },
 			};
 			teamPositions[1] = new float[][] {
 					{ FIELDWIDTH/2+250, FIELDHEIGHT/2 },
 					{ FIELDWIDTH/2+250, FIELDHEIGHT/2 - 100 },
 					{ FIELDWIDTH/2+250, FIELDHEIGHT/2 + 100 },
 			};
 		}
 		
 		playerStartPositions = new float[4][2];
 		
 		int[] teamCounter = new int[4];
 		for(int i=0;i<teamCounter.length;i++)
 			teamCounter[i] = 0;
 		
 		int playerCounter = 0;
 		
 		// Setting start positions for each player
 		for (CambridgePlayerAnchor a : data.playerAnchors()) {
 			if (a.initiated()) {
 //				if (a.getTeam() == 0) { //0 is team 1
 //					playerStartPositions[playerCounter] = team1Positions[teamCounter[0]]; 
 //					teamCounter[0]++;
 //				} else if (a.getTeam() == 1) { //1 is team 2
 //					playerStartPositions[playerCounter] = team2Positions[teamCounter[1]];
 //					teamCounter[1]++;
 //				}
 				playerStartPositions[playerCounter] = teamPositions[a.getTeam()][teamCounter[a.getTeam()]];
 				teamCounter[a.getTeam()]++;
 			}
 			playerCounter++;
 		}
 		
 		playerCharacters = new int[4][];
 		playerCharacters[0] = new int[teamCounter[0]];
 		playerCharacters[1] = new int[teamCounter[1]];
 		playerCharacters[2] = new int[teamCounter[2]];
 		playerCharacters[3] = new int[teamCounter[3]];
 		
 		for(int i=0;i<teamCounter.length;i++)
 			teamCounter[i] = 0;
 		
 		// Setting player colors
 		// NEED TO COME BACK TO THIS FOR FOURSQUARE
 		Color[] playerColors = new Color[4];
 		for (int i = 0; i < data.playerAnchors().length; i++) {
 			if (data.playerAnchors()[i].getTeam() != -1) {
 				playerColors[i] = teamColors[data.playerAnchors()[i].getTeam()];
 				playerCharacters[data.playerAnchors()[i].getTeam()][teamCounter[data.playerAnchors()[i].getTeam()]] = data.playerAnchors()[i].getCharacter();
 				teamCounter[data.playerAnchors()[i].getTeam()]++;
 			} else {
 				playerColors[i] = Color.white;
 			}
 		}
 		
 		// Creating player objects
 		players = new ArrayList<Player>();
 		int[] p1Controls = new int[]{Input.KEY_UP, Input.KEY_DOWN, Input.KEY_LEFT, Input.KEY_RIGHT, Input.KEY_PERIOD, Input.KEY_COMMA};
 		int[] p2Controls = new int[]{Input.KEY_T, Input.KEY_G, Input.KEY_F, Input.KEY_H, Input.KEY_2, Input.KEY_1};
 		
 		for (int i = 0; i < data.playerAnchors().length; i++) {
 			switch(data.playerAnchors()[i].getCharacter()) {
 			case 0:
 				players.add(new PlayerBack(
 						data.playerAnchors()[i].playerNum(),
 						data.playerAnchors()[i].getTeam(),
 						playerConsts,
 						new int[]{FIELDWIDTH,FIELDHEIGHT},
 						data.playerAnchors()[i].getKeyboard() == 0 ? p1Controls : p2Controls, // Controller players will also get p2Controls.
 						data.playerAnchors()[i].controller(),
 						playerStartPositions[i],
 						data.playerAnchors()[i].getTeam() == 0 ? team1lim : team2lim, //TERRIBLE TERRBILE THINGS. Default is team2lim
 						playerColors[i],
 						mySoundSystem,
 						"slow1",
 						slice,
 						slice_wide,
 						ball));
 				break;
 			case 1:
 				players.add(new PlayerDash(
 						data.playerAnchors()[i].playerNum(),
 						data.playerAnchors()[i].getTeam(),
 						playerConsts,
 						new int[]{FIELDWIDTH,FIELDHEIGHT},
 						data.playerAnchors()[i].getKeyboard() == 0 ? p1Controls : p2Controls, // Controller players will also get p2Controls.
 						data.playerAnchors()[i].controller(),
 						playerStartPositions[i],
 						data.playerAnchors()[i].getTeam() == 0 ? team1lim : team2lim, //TERRIBLE TERRBILE THINGS. Default is team2lim
 						playerColors[i],
 						mySoundSystem,
 						"slow1",
 						slice,
 						slice_tri,
 						ball,
 						hemicircleL));
 				break;
 			case 2:
 				players.add(new PlayerEnforcer(
 						data.playerAnchors()[i].playerNum(),
 						data.playerAnchors()[i].getTeam(),
 						playerConsts,
 						new int[]{FIELDWIDTH,FIELDHEIGHT},
 						data.playerAnchors()[i].getKeyboard() == 0 ? p1Controls : p2Controls, // Controller players will also get p2Controls.
 						data.playerAnchors()[i].controller(),
 						playerStartPositions[i],
 						data.playerAnchors()[i].getTeam() == 0 ? team1lim : team2lim, //TERRIBLE TERRBILE THINGS. Default is team2lim
 						playerColors[i],
 						mySoundSystem,
 						"slow1",
 						slice,
 						ball));
 				break;
 			case 3:
 				players.add(new PlayerNeo(
 						data.playerAnchors()[i].playerNum(),
 						data.playerAnchors()[i].getTeam(),
 						playerConsts,
 						new int[]{FIELDWIDTH,FIELDHEIGHT},
 						data.playerAnchors()[i].getKeyboard() == 0 ? p1Controls : p2Controls, // Controller players will also get p2Controls.
 						data.playerAnchors()[i].controller(),
 						playerStartPositions[i],
 						data.playerAnchors()[i].getTeam() == 0 ? team1lim : team2lim, //TERRIBLE TERRBILE THINGS. Default is team2lim
 						playerColors[i],
 						mySoundSystem,
 						"slow"+(i+1),
 						slice));
 				break;
 			case 4:
 				players.add(new PlayerNeutron(
 						data.playerAnchors()[i].playerNum(),
 						data.playerAnchors()[i].getTeam(),
 						playerConsts,
 						new int[]{FIELDWIDTH,FIELDHEIGHT},
 						data.playerAnchors()[i].getKeyboard() == 0 ? p1Controls : p2Controls, // Controller players will also get p2Controls.
 						data.playerAnchors()[i].controller(),
 						playerStartPositions[i],
 						data.playerAnchors()[i].getTeam() == 0 ? team1lim : team2lim, //TERRIBLE TERRBILE THINGS. Default is team2lim
 						playerColors[i],
 						mySoundSystem,
 						"slow1",
 						slice,
 						ball));
 				break;
 			case 5:
 				players.add(new PlayerTricky(
 						data.playerAnchors()[i].playerNum(),
 						data.playerAnchors()[i].getTeam(),
 						playerConsts,
 						new int[]{FIELDWIDTH,FIELDHEIGHT},
 						data.playerAnchors()[i].getKeyboard() == 0 ? p1Controls : p2Controls, // Controller players will also get p2Controls.
 						data.playerAnchors()[i].controller(),
 						playerStartPositions[i],
 						data.playerAnchors()[i].getTeam() == 0 ? team1lim : team2lim, //TERRIBLE TERRBILE THINGS. Default is team2lim
 						playerColors[i],
 						mySoundSystem,
 						"slow1",
 						slice,
 						ball));
 				((PlayerTricky)players.get(players.size()-1)).setFakeBall(new BallFake(1, ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem));
 				players.add(new PlayerDummy(
 						data.playerAnchors()[i].playerNum(),
 						data.playerAnchors()[i].getTeam(),
 						playerConsts,
 						new int[]{FIELDWIDTH,FIELDHEIGHT},
 						data.playerAnchors()[i].getKeyboard() == 0 ? p1Controls : p2Controls, // Controller players will also get p2Controls.
 						data.playerAnchors()[i].controller(),
 						playerStartPositions[i],
 						data.playerAnchors()[i].getTeam() == 0 ? team1lim : team2lim, //TERRIBLE TERRBILE THINGS. Default is team2lim
 						playerColors[i],
 						mySoundSystem,
 						"slow1",
 						slice));
 				((PlayerTricky)players.get(players.size()-2)).setDummy((PlayerDummy)players.get(players.size()-1));
 				break;
 			case 6:
 				players.add(new PlayerTwin(
 						data.playerAnchors()[i].playerNum(),
 						data.playerAnchors()[i].getTeam(),
 						playerConsts,
 						new int[]{FIELDWIDTH,FIELDHEIGHT},
 						data.playerAnchors()[i].getKeyboard() == 0 ? p1Controls : p2Controls, // Controller players will also get p2Controls.
 						data.playerAnchors()[i].controller(),
 						playerStartPositions[i],
 						data.playerAnchors()[i].getTeam() == 0 ? team1lim : team2lim, //TERRIBLE TERRBILE THINGS. Default is team2lim
 						playerColors[i],
 						mySoundSystem,
 						"slow1",
 						slice,
 						slice_twin,
 						0,
 						hemicircleL,
 						ball));
 				players.add(new PlayerTwin(
 						data.playerAnchors()[i].playerNum(),
 						data.playerAnchors()[i].getTeam(),
 						playerConsts,
 						new int[]{FIELDWIDTH,FIELDHEIGHT},
 						data.playerAnchors()[i].getKeyboard() == 0 ? p1Controls : p2Controls, // Controller players will also get p2Controls.
 						data.playerAnchors()[i].controller(),
 						new float[] {playerStartPositions[i][0], playerStartPositions[i][1]+1},
 						data.playerAnchors()[i].getTeam() == 0 ? team1lim : team2lim, //TERRIBLE TERRBILE THINGS. Default is team2lim
 						playerColors[i],
 						mySoundSystem,
 						"slow1",
 						slice,
 						slice_twin,
 						1,
 						hemicircleR,
 						ball));
 				((PlayerTwin)players.get(players.size()-2)).setTwin((PlayerTwin)players.get(players.size()-1));
 				((PlayerTwin)players.get(players.size()-1)).setTwin((PlayerTwin)players.get(players.size()-2));
 				break;
 			case 7:
 				players.add(new PlayerTwoTouch(
 						data.playerAnchors()[i].playerNum(),
 						data.playerAnchors()[i].getTeam(),
 						playerConsts,
 						new int[]{FIELDWIDTH,FIELDHEIGHT},
 						data.playerAnchors()[i].getKeyboard() == 0 ? p1Controls : p2Controls, // Controller players will also get p2Controls.
 						data.playerAnchors()[i].controller(),
 						playerStartPositions[i],
 						data.playerAnchors()[i].getTeam() == 0 ? team1lim : team2lim, //TERRIBLE TERRBILE THINGS. Default is team2lim
 						playerColors[i],
 						mySoundSystem,
 						"slow1",
 						slice,
 						ball,
 						new Ball(1, ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem)));
 				break;
 			}
 		}
 
 //<<<<<<< Updated upstream
 		
 ////		Ball predictor  = new Ball(1, ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem);
 ////		PlayerTwoTouch p1 = new PlayerTwoTouch(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball, predictor);
 //=======
 //		int[] p2Controls = new int[]{Input.KEY_T, Input.KEY_G, Input.KEY_F, Input.KEY_H, Input.KEY_2, Input.KEY_1};
 //		int[] p1Controls = new int[]{Input.KEY_UP, Input.KEY_DOWN, Input.KEY_LEFT, Input.KEY_RIGHT, Input.KEY_PERIOD, Input.KEY_COMMA};
 ////		PlayerTwoTouch p1 = new PlayerTwoTouch(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball);
 //>>>>>>> Stashed changes
 ////		PlayerTwin p1L = new PlayerTwin(0, playerConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, slice_twin, 0, hemicircleL, ball);
 ////		PlayerTwin p1R = new PlayerTwin(0, playerConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, p1Controls, c1, new float[]{p1L.getX(),p1L.getY()+1}, p1lim, Color.orange, mySoundSystem, "slow1", slice, slice_twin, 1, hemicircleR, ball);
 ////		p1L.setTwin(p1R);
 ////		p1R.setTwin(p1L);
 //<<<<<<< Updated upstream
 //		//PlayerNeo p1 = new PlayerNeo(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice);
 //		//PlayerNeutron p1 = new PlayerNeutron(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball);
 //		p1 = new PlayerBack(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, slice_wide, ball);
 //		//PlayerDash p1 = new PlayerDash(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, slice_tri, ball, hemicircleL);
 //		//PlayerEnforcer p1 = new PlayerEnforcer(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball);
 //=======
 //		PlayerNeo p1 = new PlayerNeo(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice);
 ////		PlayerNeutron p1 = new PlayerNeutron(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball);
 ////		PlayerBack p1 = new PlayerBack(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, slice_wide, ball);
 ////		PlayerDash p1 = new PlayerDash(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, slice_tri, ball, hemicircleL);
 ////		PlayerEnforcer p1 = new PlayerEnforcer(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball);
 //>>>>>>> Stashed changes
 ////		PlayerTricky p1 = new PlayerTricky(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice, ball);
 ////		p1.setFakeBall(new BallFake(1, ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem));
 ////		PlayerDummy p1D1 = new PlayerDummy(0, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p1Controls, c1, p1Start, p1lim, Color.orange, mySoundSystem, "slow1", slice);
 ////		p1.setDummy(p1D1);
 //<<<<<<< Updated upstream
 ////		Ball predictor2  = new Ball(1, ballConsts, new int[]{FIELDWIDTH, FIELDHEIGHT}, goals, new float[]{FIELDWIDTH/2, FIELDHEIGHT/2}, GOALSIZE,  mySoundSystem);
 ////		PlayerTwoTouch p2 = new PlayerTwoTouch(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice, ball, predictor2);
 //		p2 = new PlayerNeo(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice);
 //		//PlayerDash p2 = new PlayerDash(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice, slice_tri, ball, hemicircleL);
 //		//PlayerEnforcer p2 = new PlayerEnforcer(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow1", slice, ball);
 //=======
 ////		PlayerTwoTouch p2 = new PlayerTwoTouch(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice, ball);
 //		PlayerNeo p2 = new PlayerNeo(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice);
 ////		PlayerDash p2 = new PlayerDash(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice, slice_tri, ball, hemicircleL);
 ////		PlayerEnforcer p2 = new PlayerEnforcer(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow1", slice, ball);
 //>>>>>>> Stashed changes
 ////		PlayerBack p2 = new PlayerBack(1, playerConsts, new int[]{FIELDWIDTH,FIELDHEIGHT}, p2Controls, c2, p2Start, p2lim, Color.cyan, mySoundSystem, "slow2", slice, slice_wide, ball);
 //		
 ////		System.out.println(p1+" "+p2);
 		
 		
 		for(Player p: players){
 			p.setPlayers(players);
 		}
 		
 		ball.setPlayers(players);
 		
 		Input input = gc.getInput();
 		//input.addKeyListener(ball);
 		for(Player p: players)
 			input.addKeyListener(p);
 		//ball.inputStarted();
 		//p1.inputStarted();
 		//p2.inputStarted();
 		
 		kickFloat = new float[]{0f,0f};
 		spinFloat = new float[]{0f,0f};
 
 		scores = new int[]{-999,-999,-999,-999};
 		for(CambridgePlayerAnchor a: data.playerAnchors())
 			if(a.initiated())
 				scores[a.getTeam()] = 0;
 		
 		tempTrailArr = new float[]{0,0,0,0};
 				
 		scrollX = 2000;
 		scrollY = 2000;
 		scrollXDir = 0;
 		scrollYDir = 0;
 		resetVelocity = new float[]{0,-1f};
 		targetX = FIELDWIDTH/2;
 		targetY = 0;
 		
 		slowMoFactor = 1f;
 //		gc.getGraphics().setAntiAlias(true);
 		
 		tempArr = new float[2];
 		
 		resetPositions();
 	}
 	
 	public void initGoals(int randomNum){
 		if(GOALTYPE == 0){//Left and Right goals
 			goals = new Goal[2];
 			goals[0] = new Goal(-4,FIELDHEIGHT/2-GOALSIZE/2, -25, GOALSIZE, -1 , 0, 0, teamColors[0].darker());
 			goals[1] = new Goal(FIELDWIDTH+5, FIELDHEIGHT/2-GOALSIZE/2, 25, GOALSIZE, 1, 0, 1, teamColors[1].darker());
 		}else if(GOALTYPE == -1){//One sided goals. Squash
 			goals = new Goal[1];
 			goals[0] = new Goal(0, 0, -25, FIELDHEIGHT, -1, 0, randomNum, teamColors[randomNum].darker());
 		}else if(GOALTYPE == 1){//Horizontal Squash Goals
 			goals = new Goal[1];
 			goals[0] = new Goal(0, FIELDHEIGHT, FIELDWIDTH, -25, 0, 1, randomNum, teamColors[randomNum].darker());
 		}else if(GOALTYPE == 2){//FOURSQUARE STYLE GOALS
 			int[] teamCounter = new int[4];
 			for(int i=0;i<teamCounter.length;i++)
 				teamCounter[i] = 0;
 			
 			for (CambridgePlayerAnchor a : data.playerAnchors())
 				if (a.initiated())
 					teamCounter[a.getTeam()]++;
 			
 			int numTeams = 0;
 			for(int i=0; i<teamCounter.length; i++)
 				if(teamCounter[i]>0)
 					numTeams++;
 			
 			goals = new Goal[numTeams*2];
 			int goalDex = 0;
 			
 			if(teamCounter[0] > 0){
 				goals[goalDex] = new Goal(0,-4,FIELDWIDTH/2,-25,0,-1,0, teamColors[0].darker());
 				goals[goalDex+1] = new Goal(-4,0,-25,FIELDHEIGHT/2,-1,0,0, teamColors[0].darker());
 				goalDex+=2;
 			}
 			if(teamCounter[2] > 0){
 				goals[goalDex] = new Goal(FIELDWIDTH/2,-4,FIELDWIDTH/2,-25,0,-1,2, teamColors[2].darker());
 				goals[goalDex+1] = new Goal(FIELDWIDTH+5,0,25,FIELDHEIGHT/2,1,0,2, teamColors[2].darker());
 				goalDex+=2;
 			}
 			if(teamCounter[1] > 0){
 				goals[goalDex] = new Goal(FIELDWIDTH+5,FIELDHEIGHT/2,25,FIELDHEIGHT/2,1,0,1, teamColors[1].darker());
 				goals[goalDex+1] = new Goal(FIELDWIDTH/2,FIELDHEIGHT+5,FIELDWIDTH/2,25,0,1,1, teamColors[1].darker());
 				goalDex+=2;
 			}
 			if(teamCounter[3] > 0){
 				goals[goalDex] = new Goal(-4,FIELDHEIGHT/2,-25,FIELDHEIGHT/2,-1,0,3, teamColors[3].darker());
 				goals[goalDex+1] = new Goal(0,FIELDHEIGHT+5,FIELDWIDTH/2,25,0,1,3, teamColors[3].darker());
 				goalDex+=2;
 			}
 		}else if(GOALTYPE == 7){//crazyking/goldengoal style goals
 			goals = new Goal[1];
 			goals[0] =  new Goal(FIELDWIDTH/2-GOALSIZE/2, FIELDHEIGHT+5, GOALSIZE, 25, 0, 1, 0, Color.yellow.darker(.3f));
 			arrowTheta = (float)Math.atan2(goals[0].getMinY()/2f+goals[0].getMaxY()/2f-FIELDHEIGHT/2f, goals[0].getMinX()/2f+goals[0].getMaxX()/2f-FIELDWIDTH/2f)*180f/(float)Math.PI;
 			arrowThetaTarget = arrowTheta;
 		}
 	}
 	
 	public void resetPositions(){
 		
 		mySoundSystem.backgroundMusic("BGM", "BGMMenu.ogg" , true);
 		mySoundSystem.setVolume("BGM", data.ambientSound()/10f);
 		
 		int randomNum = (int)(Math.random()*2);
 //
 //		float[] p1Start = {FIELDWIDTH/2-250, FIELDHEIGHT/2};
 //		float[] p2Start = {FIELDWIDTH/2+250, FIELDHEIGHT/2};
 //		
 		
 		if(NAME.equals(TENNIS)){
 //			p1Start = new float[]{120, FIELDHEIGHT/2};
 //			p2Start = new float[]{FIELDWIDTH-120, FIELDHEIGHT/2};
 			ball.setPos(350+randomNum*(FIELDWIDTH-700), FIELDHEIGHT/2+150-300*randomNum);
 		}else if(NAME.equals(SQUASH)){
 //			p1Start = new float[]{100, FIELDHEIGHT/2-300};
 //			p2Start = new float[]{100, FIELDHEIGHT/2+300};
 			//ball.setPos(HEIGHT/2-200, FIELDHEIGHT/2-300+600*randomNum);
 			ball.setPos(FIELDWIDTH/2-FIELDWIDTH*3/8+FIELDWIDTH*6/8*randomNum, FIELDHEIGHT/2+200);
 		}else{
 			ball.setPos(FIELDWIDTH/2, FIELDHEIGHT/2);
 		}
 		
 		ball.setVel(ball.getVel(), 0);
 		scored = false;
 		ball.setScored(false);
 		ball.cancelAcc();
 		ball.clearLocked();
 		ball.setReadyForGust(false);
 		
 //		p1.setPos(p1Start[0], p1Start[1]);
 //		p2.setPos(p2Start[0], p2Start[1]);
 		for (Player p : players) {
 			p.resetPos();
 		}
 
 		scores[0] = -999;
 		scores[1] = -999;
 		scores[2] = -999;
 		scores[3] = -999;
 		
 		for(CambridgePlayerAnchor a: data.playerAnchors())
 			if(a.initiated())
 				scores[a.getTeam()] = 0;
 		
 		timef = (float)data.timeLimit()*1000f;
 		time = (int)timef;
 		
 		gameStartCountdown = GAMESTARTCOUNTDOWN;//4: Ready, 3: Set: 2: Go 1: Play Ball 0
 		gameStartStage = 3;
 		
 		gameOverCountdown = GAMEOVERCOUNTDOWN;
 		gameOverStage = 3;
 		
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
 
 		//initFields(gc);
 	}
 
 	@Override
 	public void leave(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		
 	}
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		if (!shouldRender)
 			return;
 		
 		g.setAntiAlias(true);
 		g.scale(scaleFactor,  scaleFactor);
 		g.translate(-viewX, -viewY);
 
 		//Background
 		g.setColor(Color.white);
 		g.fillRect(-2000, -2000, FIELDWIDTH+4000, FIELDHEIGHT+4000);
 		g.setColor(Color.black);
 		g.fillRect(0,0, FIELDWIDTH, FIELDHEIGHT);
 		g.setColor(Color.white);
 		
 		//Draw camera box for debug
 		//g.setColor(Color.green);
 		//g.drawRect(viewX, viewY, tempX, tempY);
 		//g.setColor(Color.white);
 		
 		//Draw Field
 		drawField(g);
 		
 		if(!data.actionCam()){
 			//fill in the UI boxes at top so there's not halfwhite/halfblack ugliness
 			g.resetTransform();
 			
 			g.setColor(Color.white.scaleCopy(scoreBarAlpha));
 			
 			g.fillRect(data.screenWidth()/2-data.screenWidth()/6, 15, data.screenWidth()/3, font.getHeight("0")+font_small.getHeight("0")+15);
 			if(NAME.equals("FOURSQUARE") || NAME.equals("GOLDENGOAL")){
 				g.fillRect(data.screenWidth()/2-data.screenWidth()/6-data.screenWidth()/7, 25, data.screenWidth()/7, font.getHeight("0")+20);
 				g.fillRect(data.screenWidth()/2+data.screenWidth()/6, 25, data.screenWidth()/7, font.getHeight("0")+20);
 				g.fillRect(data.screenWidth()/2-data.screenWidth()/6-data.screenWidth()*2/7, 25, data.screenWidth()/7, font.getHeight("0")+20);
 				g.fillRect(data.screenWidth()/2+data.screenWidth()/6+data.screenWidth()/7, 25, data.screenWidth()/7, font.getHeight("0")+20);
 			}else{
 				g.fillRect(data.screenWidth()/2-data.screenWidth()/3, 25, data.screenWidth()/6, font.getHeight("0")+20);
 				g.fillRect(data.screenWidth()/2+data.screenWidth()/6, 25, data.screenWidth()/6, font.getHeight("0")+20);
 			}
 			g.scale(scaleFactor,  scaleFactor);
 			g.translate(-viewX, -viewY);
 		}
 		
 		//Draw Players
 		for(Player p: players){
 			p.render( g, BALLSIZE, triangle, font_small, data.playerIdDisplay());
 		}
 		
 //		g.drawLine(p1.getX(), p1.getY(), p1.getX()+spinFloat[0]*100, p1.getY()+spinFloat[1]*100);
 		
 		//Draw ball
 		if(!scored){
 			g.setColor(Color.red);
 			g.rotate(ball.getX(), ball.getY(), ball.getTheta());
 			g.fillRect(ball.getX()-BALLSIZE/2, ball.getY()-BALLSIZE/2, BALLSIZE, BALLSIZE);
 			g.rotate(ball.getX(), ball.getY(), -ball.getTheta());
 			g.setColor(Color.white);
 		}
 		
 		//draw goal scroll
 		if(gameOverCountdown >= GAMEOVERCOUNTDOWN)
 			g.drawImage(goalScroll, scrollX, scrollY);
 		
 		//draw bars
 		g.setColor(Color.white);
 		g.fillRect(-1000, -1000, FIELDWIDTH+2000, 1000);
 		g.fillRect(-1000, 0, 1000, FIELDHEIGHT);
 		g.fillRect(-1000, FIELDHEIGHT, FIELDWIDTH+2000, 1000);
 		g.fillRect(FIELDWIDTH, 0, 1000, FIELDHEIGHT);
 		
 		//Draw goals
 		for(Goal goal: goals){
 			g.setColor(goal.getColor());
 			g.fillRect(goal.getX(), goal.getMinY(), goal.getWidth(), goal.getHeight());
 		}
 		//g.fillRect(FIELDWIDTH-10, FIELDHEIGHT/2-GOALWIDTH/2, 15, GOALWIDTH);
 		
 
 		
 		//Draw Header
 		//drawHeader(g);
 		
 //		Debug ruler
 //		g.setColor(Color.red);
 //		for(int i=0;i<900; i+=50){
 //			g.drawString(""+i, 100, i);
 //		}
 		
 		g.resetTransform();
 		
 //		Debug Ruler 2
 //		g.setColor(Color.green);
 //		for(int i=0;i<900; i+=50){
 //			g.drawString(""+i, 100, i);
 //		}
 		
 		drawUI(g);
 		
 		drawGameOver(g);
 		
 		drawGameStart(g);
 		
 	}
 	
 	public void drawHeader(Graphics g){
 //		//Draw Text
 //		g.setFont(font);
 //		if(GOALTYPE == 1 || GOALTYPE == -1){//Squash or FourSquare
 //			g.rotate(0,0,-90);
 //			g.drawString("PLAY "+NAME, -FIELDHEIGHT/2 - font.getWidth("PLAY "+NAME)/2, FIELDWIDTH);
 //			g.rotate(0,0,90);
 //			g.drawString("FERNANDO TORANGE", 0, -font.getHeight("0")-10);
 //			g.drawString("DIDIER DROGBLUE", 0 , FIELDHEIGHT);
 //			
 //			g.drawString(""+scores[1], FIELDWIDTH-font.getWidth(""+scores[1]) , -font.getHeight("0")-10);
 //			g.drawString(""+scores[0], FIELDWIDTH-font.getWidth(""+scores[0]),  FIELDHEIGHT);
 //		}else if(GOALTYPE==2){//Same as above but more spacing, for onesquare
 //			g.rotate(0,0,-90);
 //			g.drawString("PLAY "+NAME, -FIELDHEIGHT/2 - font.getWidth("PLAY "+NAME)/2, FIELDWIDTH+10);
 //			g.rotate(0,0,90);
 //			g.drawString("FERNANDO TORANGE", 0, -font.getHeight("0")-30);
 //			g.drawString("DIDIER DROGBLUE", 0 , FIELDHEIGHT+12);
 //			
 //			g.drawString(""+scores[1], FIELDWIDTH-font.getWidth(""+scores[1]) , -font.getHeight("0")-30);
 //			g.drawString(""+scores[0], FIELDWIDTH-font.getWidth(""+scores[0]),  FIELDHEIGHT+12);
 //		}else{
 //			//Draw game mode
 //			g.drawString("PLAY "+NAME, FIELDWIDTH/2 - font.getWidth("PLAY "+NAME)/2, FIELDHEIGHT);
 //			
 //			//Draw Player Names. have these be randomized for now, maybe based on character select
 //			g.drawString("TEAM 1", 0, -font.getHeight("0")-10);
 //			g.drawString("TEAM 2", FIELDWIDTH - font.getWidth("TEAM 2"), -font.getHeight("0")-10);
 //			
 ////			//Draw scores
 ////			g.drawString(""+scores[0], FIELDWIDTH/2-font.getWidth(""+scores[0])-20-100, -font.getHeight("0")-10);
 ////			g.drawString(""+scores[1], FIELDWIDTH/2+20+100,  -font.getHeight("0")-10);
 //////			g.drawString(":", FIELDWIDTH/2-font.getWidth(":")/2, -font.getHeight("0")-14);
 ////			
 ////			//Draw Timer
 ////			if(data.timeLimit()>0){
 ////				g.drawString(""+(time/1000), FIELDWIDTH/2-font.getWidth(""+time/1000)/2, -font.getHeight("0")-32);
 ////				g.setFont(font_small);
 ////				g.setColor(Color.black);
 ////				g.drawString(String.format("%03d",(time-time/1000*1000)), FIELDWIDTH/2-font_small.getWidth(String.format("%03d",(time-time/1000*1000)))/2, -font_small.getHeight("0")-8);
 ////			}else{ 
 ////				g.drawString(""+0, FIELDWIDTH/2-font.getWidth(""+0)/2, -font.getHeight("0")-32);
 ////				g.setFont(font_small);
 ////				g.setColor(Color.black);
 ////				g.drawString(String.format("%03d",0), FIELDWIDTH/2-font_small.getWidth(String.format("%03d",0))/2, -font_small.getHeight("0")-8);
 ////			}
 //		}
 	}
 	
 	public void drawField(Graphics g){
 		//Draw the field markings
 		g.setColor(Color.white);
 		g.setLineWidth(1);
 		g.drawRect(0,0,FIELDWIDTH, FIELDHEIGHT);
 		g.setLineWidth(5);
 		if(NAME.equals(SOCCER)){
 			g.drawLine(FIELDWIDTH/2,0,FIELDWIDTH/2,FIELDHEIGHT);
 			g.drawOval(FIELDWIDTH/2-150, FIELDHEIGHT/2-150, 300, 300);
 			g.drawLine(FIELDWIDTH/2, 0, FIELDWIDTH/2, FIELDHEIGHT);
 			g.drawOval(-50, FIELDHEIGHT/2-190,380,380);
 			g.drawOval(FIELDWIDTH+50, FIELDHEIGHT/2-190,-380,380);
 			g.setColor(Color.black);//Fill in goalie box
 			g.fillRect(0,FIELDHEIGHT/2-postWidth/2-100,250,postWidth+200);
 			g.fillRect(FIELDWIDTH, FIELDHEIGHT/2-postWidth/2-100,-250,postWidth+200);
 			g.setColor(Color.white);//Goalie box
 			g.drawRect(0,FIELDHEIGHT/2-postWidth/2-100,250,postWidth+200);
 			g.drawRect(FIELDWIDTH, FIELDHEIGHT/2-postWidth/2-100,-250,postWidth+200);
 			g.drawRect(0,FIELDHEIGHT/2-postWidth/2,100,postWidth);
 			g.drawRect(FIELDWIDTH, FIELDHEIGHT/2-postWidth/2,-100,postWidth);
 		}else if (NAME.equals(HOCKEY)){
 			g.drawLine(FIELDWIDTH/2,0,FIELDWIDTH/2,FIELDHEIGHT);
 			g.drawRect(FIELDWIDTH/3, 0, FIELDWIDTH/3, FIELDHEIGHT);
 			g.drawOval(-40,FIELDHEIGHT/2-GOALSIZE/2,80,GOALSIZE);
 			g.drawOval(FIELDWIDTH+40, FIELDHEIGHT/2-GOALSIZE/2,-80,GOALSIZE);
 			g.fillOval(80+FIELDWIDTH/12-10, 80+FIELDWIDTH/12-10, 20, 20);
 			g.fillOval(80+FIELDWIDTH/12-10, FIELDHEIGHT-80-FIELDWIDTH/12-10, 20, 20);
 			g.fillOval(FIELDWIDTH-80-FIELDWIDTH/12-10,80+FIELDWIDTH/12-10,20,20);
 			g.fillOval(FIELDWIDTH-80-FIELDWIDTH/12-10,FIELDHEIGHT-80-FIELDWIDTH/12-10,20,20);
 			g.fillOval(FIELDWIDTH/2-200, 80+FIELDWIDTH/12-10, 20, 20);
 			g.fillOval(FIELDWIDTH/2-200, FIELDHEIGHT-80-FIELDWIDTH/12-10, 20, 20);
 			g.fillOval(FIELDWIDTH/2+200,80+FIELDWIDTH/12-10,-20,20);
 			g.fillOval(FIELDWIDTH/2+200,FIELDHEIGHT-80-FIELDWIDTH/12-10,-20,20);
 			g.setLineWidth(3);
 			g.setColor(Color.black);
 			g.fillOval(FIELDWIDTH/2-FIELDWIDTH/12, FIELDHEIGHT/2-FIELDWIDTH/12, FIELDWIDTH/6, FIELDWIDTH/6);
 			g.setColor(Color.white);
 			g.drawOval(FIELDWIDTH/2-FIELDWIDTH/12, FIELDHEIGHT/2-FIELDWIDTH/12, FIELDWIDTH/6, FIELDWIDTH/6);
 			g.drawOval(80,80,FIELDWIDTH/6,FIELDWIDTH/6);
 			g.drawOval(80,FIELDHEIGHT-80,FIELDWIDTH/6,-FIELDWIDTH/6);
 			g.drawOval(FIELDWIDTH-80,80,-FIELDWIDTH/6,FIELDWIDTH/6);
 			g.drawOval(FIELDWIDTH-80,FIELDHEIGHT-80,-FIELDWIDTH/6,-FIELDWIDTH/6);
 			g.setLineWidth(5);
 		}else if(NAME.equals(TENNIS)){
 			g.drawRect(150,150,FIELDWIDTH-300,FIELDHEIGHT-300);
 			g.drawLine(FIELDWIDTH/2, 150, FIELDWIDTH/2, FIELDHEIGHT-150);
 			g.drawRect(150,225,FIELDWIDTH-300,FIELDHEIGHT-450);
 			g.drawRect(350,225,FIELDWIDTH-700,FIELDHEIGHT-450);
 			g.drawLine(350, FIELDHEIGHT/2, FIELDWIDTH-350, FIELDHEIGHT/2);
 		}else if(NAME.equals(SQUASH)){
 			g.drawLine(0, FIELDHEIGHT/2+100, FIELDWIDTH, FIELDHEIGHT/2+100);
 			g.drawLine(FIELDWIDTH/2, FIELDHEIGHT/2+100, FIELDWIDTH/2, FIELDHEIGHT);
 			g.drawRect(0, FIELDHEIGHT/2+100, 200, 200);
 			g.drawRect(FIELDWIDTH, FIELDHEIGHT/2+100, -200, 200);
 //			
 //			g.drawLine(FIELDWIDTH/2-100, 0, FIELDWIDTH/2-100, FIELDHEIGHT);
 //			g.drawLine(0, FIELDHEIGHT/2, FIELDWIDTH/2-100, FIELDHEIGHT/2);
 //			g.drawRect(FIELDWIDTH/2-100, 0, -200, 200);
 //			g.drawRect(FIELDWIDTH/2-100,FIELDHEIGHT,-200,-200);
 		}else if(NAME.equals("GOLDENGOAL")){
 			g.drawLine(0, 0, FIELDWIDTH, FIELDHEIGHT);
 			g.drawLine(0, FIELDHEIGHT, FIELDWIDTH, 0);
 			g.setColor(Color.black);
 //			g.fillRect(FIELDWIDTH/2-200, FIELDHEIGHT/2-200, 400, 400);
 			g.fillOval(FIELDWIDTH/2 - 350, FIELDHEIGHT/2 - 350, 700, 700);
 			g.setColor(Color.white);
 			//g.drawRect(FIELDWIDTH/2-150, FIELDHEIGHT/2-150, 300, 300);
 			g.drawOval(FIELDWIDTH/2 - 350, FIELDHEIGHT/2 - 350, 700, 700);
 			g.rotate(FIELDWIDTH/2, FIELDHEIGHT/2, arrowTheta);
 			g.drawImage(goldgoal_arrow.getScaledCopy((int)(FIELDHEIGHT/2f), (int)(FIELDHEIGHT/2f)), FIELDWIDTH/2f-FIELDHEIGHT/4f, FIELDHEIGHT/2f-FIELDHEIGHT/4f, new Color(60,60,60,100));
 			g.rotate(FIELDWIDTH/2, FIELDHEIGHT/2, -arrowTheta);
 		}else if(NAME.equals("FOURSQUARE")){
 			g.drawLine(0, FIELDHEIGHT/2, FIELDWIDTH, FIELDHEIGHT/2);
 			g.drawLine(FIELDWIDTH/2, 0, FIELDWIDTH/2, FIELDHEIGHT);
 			g.setColor(Color.black);
 			g.fillOval(FIELDWIDTH/2-100, FIELDHEIGHT/2-100, 200, 200);
 			g.setColor(Color.white);
 			g.drawOval(FIELDWIDTH/2-100, FIELDHEIGHT/2-100, 200, 200);
 		}
 	}
 	
 	public void drawUI(Graphics g){
 
 		//Draw boxes
 		g.setLineWidth(2);
 		if(data.actionCam())
 			g.setColor(new Color(1,1,1,140));
 		else
 			g.setColor(new Color(255,255,255,90));
 		
 		g.fillRect(data.screenWidth()/2-data.screenWidth()/6, 15, data.screenWidth()/3, font.getHeight("0")+font_small.getHeight("0")+15);
 		if(NAME.equals("FOURSQUARE") || NAME.equals("GOLDENGOAL")){
 			g.fillRect(data.screenWidth()/2-data.screenWidth()/6-data.screenWidth()/7, 25, data.screenWidth()/7, font.getHeight("0")+20);
 			g.fillRect(data.screenWidth()/2+data.screenWidth()/6, 25, data.screenWidth()/7, font.getHeight("0")+20);
 			g.fillRect(data.screenWidth()/2-data.screenWidth()/6-data.screenWidth()*2/7, 25, data.screenWidth()/7, font.getHeight("0")+20);
 			g.fillRect(data.screenWidth()/2+data.screenWidth()/6+data.screenWidth()/7, 25, data.screenWidth()/7, font.getHeight("0")+20);
 		}else{
 			g.fillRect(data.screenWidth()/2-data.screenWidth()/3, 25, data.screenWidth()/6, font.getHeight("0")+20);
 			g.fillRect(data.screenWidth()/2+data.screenWidth()/6, 25, data.screenWidth()/6, font.getHeight("0")+20);
 		}
 		
 		if(data.actionCam())
 			g.setColor(Color.white);
 		else
 			g.setColor(Color.black);
 		//Score boxes
 		if(NAME.equals("FOURSQUARE") || NAME.equals("GOLDENGOAL")){
 			g.drawRect(data.screenWidth()/2-data.screenWidth()/6-data.screenWidth()/7, 25, data.screenWidth()/7, font.getHeight("0")+20);
 			g.drawRect(data.screenWidth()/2+data.screenWidth()/6, 25, data.screenWidth()/7, font.getHeight("0")+20);
 			g.drawRect(data.screenWidth()/2-data.screenWidth()/6-data.screenWidth()*2/7, 25, data.screenWidth()/7, font.getHeight("0")+20);
 			g.drawRect(data.screenWidth()/2+data.screenWidth()/6+data.screenWidth()/7, 25, data.screenWidth()/7, font.getHeight("0")+20);
 		}else{
 			g.drawRect(data.screenWidth()/2-data.screenWidth()/3, 25, data.screenWidth()/6, font.getHeight("0")+20);
 			g.drawRect(data.screenWidth()/2+data.screenWidth()/6, 25, data.screenWidth()/6, font.getHeight("0")+20);
 
 		}
 		
 		//Timer boxes
 		g.drawRect(data.screenWidth()/2-data.screenWidth()/6, 15, data.screenWidth()/3, font.getHeight("0")+font_small.getHeight("0")+15);
 		
 		//Draw scores
 		g.setFont(font_white);		
 		if(NAME.equals("FOURSQUARE") || NAME.equals("GOLDENGOAL")){
 			if(scores[0]>=0){
 				g.setColor(data.actionCam() ? teamColors[0] : teamColors[0].darker(.4f));
 				g.drawString(""+scores[0], data.screenWidth()/2-data.screenWidth()/6-data.screenWidth()*3/14-font.getWidth(""+scores[0])/2, 28);
 			}if(scores[1]>=0){
 				g.setColor(data.actionCam() ? teamColors[1] : teamColors[1].darker(.4f));
 				g.drawString(""+scores[1], data.screenWidth()/2-data.screenWidth()/6-data.screenWidth()/14-font.getWidth(""+scores[1])/2, 28);
 			}if(scores[2]>=0){
 				g.setColor(data.actionCam() ? teamColors[2] : teamColors[2].darker(.4f));
 				g.drawString(""+scores[2], data.screenWidth()/2+data.screenWidth()/6+data.screenWidth()/14-font.getWidth(""+scores[2])/2, 28);
 			}if(scores[3]>=0){
 				g.setColor(data.actionCam() ? teamColors[3] : teamColors[3].darker(.4f));
 				g.drawString(""+scores[3], data.screenWidth()/2+data.screenWidth()/6+data.screenWidth()*3/14-font.getWidth(""+scores[3])/2, 28);
 			}
 		}else{
 			g.drawString(""+scores[0], data.screenWidth()/2-data.screenWidth()*3f/12f-font.getWidth(""+scores[0])/2, 28);
 			g.drawString(""+scores[1], data.screenWidth()/2+data.screenWidth()*3f/12f-font.getWidth(""+scores[1])/2, 28);
 	//		g.drawString(":", FIELDWIDTH/2-font.getWidth(":")/2, -font.getHeight("0")-14);
 		}
 		
 		if(data.actionCam())
 			g.setColor(Color.white);
 		else
 			g.setColor(Color.black);
 		//Draw Timer
 		if(data.timeLimit()>0){
 			g.drawString(""+(time/1000), data.screenWidth()/2-font.getWidth(""+time/1000)/2, 10);
 			g.setFont(font_small);
 			g.drawString(String.format("%03d",(time-time/1000*1000)), data.screenWidth()/2-font_small.getWidth(String.format("%03d",(time-time/1000*1000)))/2, 10+font.getHeight("0")+5);
 		}else{ 
 			g.drawString("99", data.screenWidth()/2-font.getWidth("99")/2, 10);
 			g.setFont(font_small);
 			g.drawString(String.format("%03d",999), data.screenWidth()/2-font_small.getWidth(String.format("%03d",999))/2, 10+font.getHeight("0")+5);
 		}
 	}
 	
 	public void drawGameOver(Graphics g){
 		//draw game over animation
 		if(gameOverCountdown < GAMEOVERCOUNTDOWN){//If the gameover countdown has started
 			g.setColor(new Color(0,0,0, 1f - (float)(gameOverCountdown-50)/(float)GAMEOVERCOUNTDOWN));
 			g.fillRect(0, 0, data.screenWidth(), data.screenHeight());
 			g.setFont(font_large);
 			g.setColor(Color.white);
 			g.drawString("FINISH!", data.screenWidth()/2f-font_large.getWidth("FINISH!")/2f, data.screenHeight()/2f - font_large.getHeight("0")/2 - 15);
 		}
 	}
 
 	public void drawGameStart(Graphics g){
 		//Draw the game start animation
 		if(gameStartCountdown > GAMESTARTCOUNTDOWN*2f/3f){
 			g.setColor(Color.white);
 			tempf = (GAMESTARTCOUNTDOWN-gameStartCountdown)/(GAMESTARTCOUNTDOWN/3f)*(data.screenWidth()+200);
 			g.fillRect(tempf ,0,data.screenWidth(), data.screenHeight()/6f);
 			g.fillRect(-tempf ,data.screenHeight(),data.screenWidth(), -data.screenHeight()/6f);
 			g.fillRect(0,data.screenHeight()/6f, data.screenWidth(), data.screenHeight()*2f/3f);
 			g.setColor(Color.black);
 			g.setFont(font_large);
 			g.drawString("READY", data.screenWidth()/2f-font_large.getWidth("READY")/2f , data.screenHeight()/2f-font_large.getHeight("0")/2-15);
 		}else if(gameStartCountdown > GAMESTARTCOUNTDOWN/3f){
 			g.setColor(Color.white);
 			tempf = -(GAMESTARTCOUNTDOWN*2f/3f-gameStartCountdown)/(GAMESTARTCOUNTDOWN/3f)*(data.screenWidth()+200);
 			g.fillRect(tempf ,data.screenHeight()/6f,data.screenWidth(), data.screenHeight()/6f);
 			g.fillRect(-tempf ,data.screenHeight()*5f/6f,data.screenWidth(), -data.screenHeight()/6f);
 			g.fillRect(0,data.screenHeight()/3f, data.screenWidth(), data.screenHeight()/3f);
 			g.setColor(Color.black);
 			g.setFont(font_large);
 			g.drawString("SET", data.screenWidth()/2f-font_large.getWidth("SET")/2f , data.screenHeight()/2f-font_large.getHeight("0")/2-15);
 		}else if(gameStartCountdown > GAMESTARTCOUNTDOWN*3f/12f){
 			g.setColor(Color.white);
 			g.fillRect(0,data.screenHeight()/3f, data.screenWidth()/2f, data.screenHeight()/3f);
 			g.fillRect(data.screenWidth()/2f,data.screenHeight()/3f, data.screenWidth()/2f, data.screenHeight()/3f);
 			g.setColor(Color.black);
 			g.setFont(font_large);
 			g.drawString("PLAY", data.screenWidth()/2f-font_large.getWidth("PLAY BALL!")/2f , data.screenHeight()/2f-font_large.getHeight("0")/2-15);
 			g.drawString("BALL!", data.screenWidth()/2f-font_large.getWidth("PLAY BALL!")/2f + font_large.getWidth("PLAY0"), data.screenHeight()/2f-font_large.getHeight("0")/2-15);
 		}else if(gameStartCountdown > 0){
 			tempf = 0;
 			g.setColor(new Color(255, 255, 255, 1f-(GAMESTARTCOUNTDOWN/4f-gameStartCountdown)/(GAMESTARTCOUNTDOWN/4f)));
 			g.fillRect(-tempf,data.screenHeight()/3f, data.screenWidth()/2f, data.screenHeight()/3f);
 			g.fillRect(data.screenWidth()/2f+tempf,data.screenHeight()/3f, data.screenWidth()/2f, data.screenHeight()/3f);
 			g.setColor(new Color(1,1,1,1f-(GAMESTARTCOUNTDOWN/4f-gameStartCountdown)/(GAMESTARTCOUNTDOWN/4f)));
 			g.setFont(font_large);
 			g.drawString("PLAY", data.screenWidth()/2f-font_large.getWidth("PLAY BALL!")/2f-tempf , data.screenHeight()/2f-font_large.getHeight("0")/2-15);
 			g.drawString("BALL!", data.screenWidth()/2f-font_large.getWidth("PLAY BALL!")/2f + font_large.getWidth("PLAY0") +tempf, data.screenHeight()/2f-font_large.getHeight("0")/2-15);
 		}
 	}
 	
 	public boolean scoreBarClear(){//Determines if a player is in the area of the score bars
 		boolean b = true;
 		for(Player p: players){
 //			System.out.println(p.getY());
 //			System.out.println((font.getHeight("0")+font_small.getHeight("0")+30)*scaleFactor);
 //			System.out.println("viewY " + viewY);
 //			System.out.println("scale "+scaleFactor);
 			//transform from inner coords to screen coords with (y-viewY)*scaleFactor
 			if(((p.getY()-p.getKickRange()/2)-viewY)*scaleFactor<(font.getHeight("0")+font_small.getHeight("0")+30))
 				b = false;
 		}
 		if((ball.getY()-viewY)*scaleFactor<(font.getHeight("0")+font_small.getHeight("0")+31))
 			b = false;
 		if(goals[0].getMinY()<10)
 			b = false;
 		return b;
 	}
 	
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
 		
 //		System.out.println(delta);
 		
 //		System.out.println(time);
 
 		if((data.timeLimit() > 0 && time == 0) || (data.scoreLimit() > 0 && (scores[0] >= data.scoreLimit() || scores[1] >= data.scoreLimit()|| scores[2] >= data.scoreLimit()|| scores[3] >= data.scoreLimit()))){
 			gameOverCountdown -= delta;
 		}
 		
 		if(gameOverCountdown <= 0){
 //			System.out.println("SCOREEND");
 			setShouldRender(false);
 			((GameOverState)sbg.getState(data.GAMEOVERSTATE)).setScores(scores);
 			((GameOverState)sbg.getState(data.GAMEOVERSTATE)).setColors(teamColors);
 			((GameOverState)sbg.getState(data.GAMEOVERSTATE)).setCharacters(playerCharacters);
 			((GameOverState)sbg.getState(data.GAMEOVERSTATE)).setShouldRender(true);
 			gc.getInput().clearKeyPressedRecord();
 			sbg.enterState(data.GAMEOVERSTATE);
 		}
 
 		deltaf = (float)delta;
 		
 		slowMoFactor = 1f;
 		for(Player p: players){
 			if(p.slowMoFactor()>slowMoFactor){
 				slowMoFactor = p.slowMoFactor();
 				break;
 			}
 		}
 		
 		//System.out.println(slowMoFactor);
 		
 		if(slowMoFactor>1f){
 			deltaf=deltaf/slowMoFactor;
 			ball.setSlowOn(true);
 			for(Player p : players)
 				p.setSlowMo(true);
 		}else{
 			ball.setSlowOn(false);
 			for(Player p: players)
 				p.setSlowMo(false);
 		}
 		
 		if(gameStartCountdown <= GAMESTARTCOUNTDOWN/4f){
 			timef -= deltaf;
 			if(timef<0){
 				timef=0;
 			}
 		}
 		time = (int)timef;
 		
 		if(gameStartCountdown>0){
 			gameStartCountdown -= delta;
 			if(gameStartCountdown < 0)
 				gameStartCountdown = 0;
 		}
 		
 		if(gameStartStage == 3){
 			mySoundSystem.quickPlay( true, "BuzzLow.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			gameStartStage --;
 		}else if(gameStartStage == 2){
 			if(gameStartCountdown <= GAMESTARTCOUNTDOWN*2f/3f){
 				mySoundSystem.quickPlay( true, "BuzzLow.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 				gameStartStage --;
 			}
 		}else if(gameStartStage == 1){
 			if(gameStartCountdown <= GAMESTARTCOUNTDOWN/3f){
 				mySoundSystem.quickPlay( true, "BuzzHigh.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 				gameStartStage --;
 			}
 		}
 		
 		//For the score bar fill, if someone is under it
 		if(scoreBarClear()){
 			scoreBarAlpha = approachTarget(scoreBarAlpha, 1f, delta/360f);
 		}else{
 			scoreBarAlpha = approachTarget(scoreBarAlpha, 0f, delta/240f);
 		}
 		 
 		Input input = gc.getInput();
 		
 		boolean startPressed = false;
 //		if(c1.exists() && c1.getStart() || c2.exists()&&c2.getStart() || c3.exists()&&c3.getStart() || c4.exists()&&c4.getStart())
 		for (CambridgePlayerAnchor a: data.playerAnchors()) {
 			if (a.start(gc, delta)) {
 				startPressed = true;
 			}
 		}
 		
 		if(input.isKeyPressed(Input.KEY_U)){
 			reset(gc);
 		}else if(input.isKeyPressed(Input.KEY_I)){
 			gameType = (gameType+1)%NUMGAMES;
 			reset(gc);
 //		}else if (input.isKeyPressed(Input.KEY_1)){
 //			gameType = 0;
 //			reset(gc);
 //		}else if (input.isKeyPressed(Input.KEY_2) && NUMGAMES>=2){
 //			gameType = 1;
 //			reset(gc);
 //		}else if (input.isKeyPressed(Input.KEY_3) && NUMGAMES>=3){
 //			gameType = 2;
 //			reset(gc);
 //		}else if (input.isKeyPressed(Input.KEY_4) && NUMGAMES>=4){
 //			gameType = 3;
 //			reset(gc);
 //		}else if (input.isKeyPressed(Input.KEY_5) && NUMGAMES>=5){
 //			gameType = 4;
 //			reset(gc);
 //		}else if (input.isKeyPressed(Input.KEY_6) && NUMGAMES>=6){
 //			gameType = 5;
 //			reset(gc);
 //		}else if (input.isKeyPressed(Input.KEY_7) && NUMGAMES>=7){
 //			gameType = 6;
 //			reset(gc);
 //		}else if (input.isKeyPressed(Input.KEY_8) && NUMGAMES>=8){
 //			gameType = 7;
 //			reset(gc);
 //		}else if (input.isKeyPressed(Input.KEY_9) && NUMGAMES>=9){
 //			gameType = 8;
 //			reset(gc);
 		}else if( input.isKeyPressed(Input.KEY_P)){
 			if(maxZoom<2)
 				maxZoom+=.2f;
 		}else if( input.isKeyPressed(Input.KEY_O)){
 			if(maxZoom>.6)
 				maxZoom-=.2f;
 		}else if( input.isKeyPressed(Input.KEY_ESCAPE) || startPressed){
 			mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			setShouldRender(false);
 			((GameOverState)sbg.getState(data.GAMEOVERSTATE)).setScores(scores);//For if you force game over from the pause menu
 			((GameOverState)sbg.getState(data.GAMEOVERSTATE)).setColors(teamColors);
 			((GameOverState)sbg.getState(data.GAMEOVERSTATE)).setCharacters(playerCharacters);
 			gc.getGraphics().copyArea(tempImage, 0, 0);
 			((MenuPauseState)sbg.getState(data.MENUPAUSESTATE)).setImage(tempImage);
 			((MenuPauseState)sbg.getState(data.MENUPAUSESTATE)).setShouldRender(true);
 			input.clearKeyPressedRecord();
 			sbg.enterState(data.MENUPAUSESTATE);
 		}
 		
 		scrollX+=2f*deltaf*scrollXDir;
 		if(scrollX>FIELDWIDTH+500 || scrollX<0-goalScroll.getWidth()-500)
 			scrollXDir=0;
 		
 		scrollY+=2f*deltaf*scrollYDir;
 		if(scrollY>FIELDHEIGHT+500 || scrollY<0-goalScroll.getHeight()-500)
 			scrollYDir=0;
 		
 		arrowTheta = approachTarget(arrowTheta, arrowThetaTarget, delta/2f);
 		
 		ball.update(deltaf);
 		
 		//Put ball back in play
		if(scored){
 			if(Math.abs(ball.getX()-targetX)<15f && Math.abs(ball.getY()-targetY)<15f){
 				temp = true;
 				for(Player p: players){if(dist(p) < p.getKickRange()/2){temp = false;}}
				if(temp && gameOverCountdown >= GAMEOVERCOUNTDOWN){
 					ball.setVel(resetVelocity, .5f);
 					ball.setPos(targetX, targetY);
 					ball.setScored(false);
 					ball.setSoundCoolDown(50);
 					scored = false;
 					mySoundSystem.quickPlay( true, slowMoFactor>1f? "BallLaunchSlow.ogg": "BallLaunch.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 					//System.out.println("BALL IN PLAY");
 				}else{
 					ball.setVel(resetVelocity, 0);
 				}
 			}
 		}
 		
 		//Scoring a goal
 		if((ball.getX()<=0 || ball.getX()>=FIELDWIDTH || ball.getY()<=0 || ball.getY()>=FIELDHEIGHT) && !scored){
 			//Will put ball in from top if score on left and bottom if score on right
 			for(Goal goal: goals){
 				//For vertical goal
 				if(ball.getY()>goal.getMinY() && ball.getY()<goal.getMaxY() && sameDir(ball.getVelX(), goal.getXDir())){
 //					//System.out.println("VERT GOAL");
 					if(ownGoal(goal)){//Own Goal
 						mySoundSystem.quickPlay( true, "GoalOwnScored.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 						goalScroll = goalScroll2;
 						for (int i = 0; i < scores.length; i++) {
 							if (i != ball.getLastKicker()) {
 								scores[i]++;
 							}
 						}
 					}else{ //Not Own Goal
 						mySoundSystem.quickPlay( true, "GoalScored.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 						goalScroll = goalScroll1;
 						scores[ball.getLastKicker()]++;
 					}
 					
 					scored = true;
 					scrollX = goal.getX()+((goal.getXDir() < 0 ? goalScroll.getWidth() + 100 : 100)*(goal.getXDir()));
 					scrollY = goal.getMinY()+goal.getHeight()/2-goalScroll.getHeight()/2;
 					scrollXDir = -goal.getXDir();
 					scrollYDir = 0;
 				}
 				//For horizontal goals
 				if(ball.getX()>goal.getMinX() && ball.getX()<goal.getMaxX() && sameDir(ball.getVelY(), goal.getYDir())){
 //					//System.out.println("HORZ GOAL");
 					if(ownGoal(goal)){//Own Goal
 						mySoundSystem.quickPlay( true, "GoalOwnScored.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 						goalScroll = goalScroll2v;
 						for (int i = 0; i < scores.length; i++) {
 							if (i != ball.getLastKicker()) {
 								scores[i]++;
 							}
 						}
 					}else{
 						mySoundSystem.quickPlay( true, "GoalScored.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 						goalScroll = goalScroll1v;
 						scores[ball.getLastKicker()]++;
 					}
 					scored = true;
 					scrollX = goal.getMinX()+goal.getWidth()/2-goalScroll.getWidth()/2;
 					scrollY = goal.getMinY()+((goal.getYDir() < 0 ? goalScroll.getHeight() + 100 : 100)*(goal.getYDir()));
 					scrollXDir = 0;
 					scrollYDir = -goal.getYDir();
 				}
 
 			}
 			
 			if(scored){
 				if(NAME.equals(TENNIS)){//Put in at side from player baseline, as a kind of serve.
 					if(Math.random()>.5f){
 						targetX = FIELDWIDTH-150;
 						targetY = FIELDHEIGHT;
 						resetVelocity[0] = 0;
 						resetVelocity[1] = -1f;
 					}else if(ball.getLastKicker()==1){
 						targetX = 150;
 						targetY = 0;
 						resetVelocity[0] = 0;
 						resetVelocity[1] = 1f;
 					}
 				}else if(NAME.equals(SOCCER) || NAME.equals(HOCKEY)){//If two goals, put in from sides at center
 					targetX = FIELDWIDTH/2;
 					if(Math.random()>.5f){
 						targetY=0;
 						resetVelocity[0] = 0;
 						resetVelocity[1] = 1f;
 					}else{
 						targetY=FIELDHEIGHT;
 						resetVelocity[0] = 0;
 						resetVelocity[1] = -1f;
 					}
 				}else if(NAME.equals(SQUASH)){//Squash, put in from opposite wall
 					targetX = FIELDWIDTH-10;
 					targetY = FIELDHEIGHT/2;
 					resetVelocity[0] = -1f;
 					resetVelocity[1] = 0;
 				}else if(GOALTYPE==2){//OneSquare, put in middle
 					targetX = FIELDWIDTH/2;
 					targetY = FIELDHEIGHT/2;
 					resetVelocity[0]=0;
 					resetVelocity[1]=0;
 				}else if(NAME.equals("GOLDENGOAL")){
 //					if(goals[0].getXDir()==-1){
 //						targetX = 0;
 //						targetY = (goals[0].getMinY()+goals[0].getMaxY())/2f;
 //					}else if(goals[0].getXDir()==1){
 //						targetX = FIELDWIDTH;
 //						targetY = (goals[0].getMinY()+goals[0].getMaxY())/2f;						
 //					}else if(goals[0].getYDir()==-1){
 //						targetX = (goals[0].getMinX()+goals[0].getMaxX())/2f;
 //						targetY = 0;
 //					}else if(goals[0].getYDir()==1){
 //						targetX = (goals[0].getMinX()+goals[0].getMaxX())/2f;
 //						targetY = FIELDHEIGHT;	
 //					}
 //					resetVelocity[0] = -goals[0].getXDir();
 //					resetVelocity[1] = -goals[0].getYDir();
 					targetX = FIELDWIDTH/2;
 					targetY = FIELDHEIGHT/2;
 					resetVelocity[0] = 0;
 					resetVelocity[1] = 0;
 					
 					if(Math.random()>.5){//vert goal
 						tempf = (float)Math.random();
 						tempArr[0] = Math.max(GOALSIZE, FIELDHEIGHT*(float)Math.random()*.6f);
 						goals[0].reinit(
 								tempf>.5f? -4: FIELDWIDTH+5,
 								(int)((FIELDHEIGHT-tempArr[0])*Math.random()),
 								tempf>.5f? -25: 25,
 								(int)tempArr[0],
 								tempf>.5f?-1:1, 0,
 								0, goals[0].getColor());
 					}else{//horz goal
 						tempf = (float)Math.random();
 						tempArr[0] = Math.max(GOALSIZE, FIELDWIDTH*(float)Math.random()*.6f);
 						goals[0].reinit(
 								(int)((FIELDWIDTH-tempArr[0])*Math.random()),
 								tempf>.5? -4: FIELDHEIGHT+5,
 								(int)tempArr[0],
 								tempf>.5f? -25:25,
 								0, tempf>.5f?-1:1,
 								0, goals[0].getColor());
 					}
 				}
 				
 				arrowThetaTarget = (float)Math.atan2(goals[0].getMinY()/2f+goals[0].getMaxY()/2f-FIELDHEIGHT/2f, goals[0].getMinX()/2f+goals[0].getMaxX()/2f-FIELDWIDTH/2f)*180f/(float)Math.PI;
 				
 				ball.setVel(new float[]{(targetX-ball.getX()),(targetY-ball.getY())}, 1f);
 				ball.setCurve(new float[]{0f,0f}, 0f);
 				ball.cancelAcc();
 				ball.setReadyForGust(false);
 				//ball.setAssistTwin(-1,-1);
 				//Scoring a goal pulls out of slowmo
 				for(Player p: players){
 					if(p.slowMoFactor()>1f)
 						p.setPower();
 				}
 				ball.setScored(true);//just long enough for it to reach reset
 			}
 		}
 
 		//Kicking the ball
 		for(Player p: players){
 			if(gameStartCountdown < GAMESTARTCOUNTDOWN/4f && gameOverCountdown >= GAMEOVERCOUNTDOWN)//if the game over countdown has started, no more player movement
 				p.update(deltaf);
 			if(p.isKicking() && !scored ){
 				if(dist(p)<p.getKickRange()/2 /* && ball.canBeKicked(p.getPlayerNum())*/) {//Perform a kick
 					//Use prevX to prevent going through the player
 					kickFloat[0] = (ball.getPrevX()-p.getX());
 					kickFloat[1] = (ball.getPrevY()-p.getY());
 
 					unit(kickFloat);
 //					tempf = 0;//Used to store the amount of player velocity added to the kick
 					if(sameDir(p.getVel()[0], kickFloat[0])){
 						kickFloat[0] += p.getKick()[0];
 //						tempf += p.getKick()[0]*p.getKick()[0];
 					}
 					if(sameDir(p.getVel()[1], kickFloat[1])){
 						kickFloat[1] += p.getKick()[1];
 //						tempf += p.getKick()[1]*p.getKick()[1];
 					}
 					unit(kickFloat);
 					tempf = Math.abs(dot(p.getVel(), kickFloat));
 					ball.setVel(new float[]{kickFloat[0], kickFloat[1]}, p.isPower()? p.POWERVELMAG*p.kickStrength() : .2f+p.getVelMag()*tempf*p.kickStrength() );
 
 					spinFloat = normal(p.getCurve(), kickFloat);
 					//System.out.println(p.kickStrength() + "-" + (p.kickStrength()*0.5f+(float)Math.sqrt(tempf)*0.5f));
 					ball.setCurve(spinFloat, mag(spinFloat)*p.curveStrength());
 					
 //					System.out.println("kickFloat:"+kickFloat[0]+", "+kickFloat[1]);
 //					System.out.println("spinFloat:"+spinFloat[0]+", "+spinFloat[1]);
 					
 //					System.out.println("Ball:"+ball.curveMag+", "+ball.curveAcc[0]+", "+ball.curveAcc[1]);
 					
 					//So PlayerNeo doesn't play the slow version of the power kick when kicking out of own slowmo
 					//Pretty hacky though
 					if(slowMoFactor>1f){
 						tempf = 0;
 						for(Player joueur: players){
 							if(joueur.slowMoFactor()>1f){
 								tempf++;
 							}
 						}
 						if(tempf == 1 && p.slowMoFactor()>1f)//If you made the slowmo and only you and now you're kicking
 							slowMoFactor = 1f;
 					}
 					
 					if(p.flashKick()){//If you want the kick flash and sound effect
 						mySoundSystem.quickPlay( true, slowMoFactor>1f?"PowerKickSlow.ogg":"PowerKick.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 						p.setLastKick(ball.getPrevX(), ball.getPrevY(), ball.getPrevX()+kickFloat[0], ball.getPrevY()+kickFloat[1], 1f);//player stores coordinates of itself and ball at last kicking event;
 						p.setPower();
 					}else{
 						mySoundSystem.quickPlay( true, slowMoFactor>1f?"KickBumpSlow.ogg":"KickBump.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 					}
 					
 					p.setKicking(ball);//really this does resetKicking()
 					ball.cancelAcc();//Cancels any speeding up or slowing down. Does not affect curve
 					ball.setReadyForGust(false);
 					ball.setLastKicker(p.getTeamNum());
 					ball.clearLocked();					
 				}
 //				 else{
 //					if(dist(p) > p.getKickRange()/2)
 //						ball.setCanBeKicked(p.getPlayerNum(), true);
 //				}
 			}else if(!p.isKicking() && !scored && !(p instanceof PlayerTwoTouch && ball.locked(p.getPlayerNum())) && !(p instanceof PlayerBack && p.isPower())){
 				if(dist(p)<p.getKickRange()/2){//Nudge it out of the way
 					//Don't use prevX, to prevent weird sliding
 					kickFloat[0] = (ball.getX()-p.getX());
 					kickFloat[1] = (ball.getY()-p.getY());
 					unit(kickFloat);
 					tempf = (float)Math.atan2(kickFloat[1], kickFloat[0]);
 					ball.setPos(p.getX()+p.getKickRange()/2f*(float)Math.cos(tempf), p.getY()+p.getKickRange()/2f*(float)Math.sin(tempf));
 //					ball.setVel(new float[]{kickFloat[0], kickFloat[1]}, ball.getVelMag());
 					ball.setLastKicker(p.getTeamNum());
 					//It shouldn't reset anything because this was added to prevent players from double tapping and clearing their own powers
 				}
 			}
 		}
 		
 		//Camera code
 		tempX = ball.getX();
 		tempY = ball.getY();
 		for(Player p: players){
 			tempX = Math.min(tempX, p.getX());
 			tempY = Math.min(tempY, p.getY());
 		}
 		minX = (int)tempX;
 		minY = (int)tempY;
 		
 		tempX = ball.getX();
 		tempY = ball.getY();
 		for(Player p: players){
 			tempX = Math.max(tempX, p.getX());
 			tempY = Math.max(tempY, p.getY());
 		}
 		maxX = (int)tempX;
 		maxY = (int)tempY;
 		
 		if(ACTIONCAM == 1){
 			tempX = maxX - minX;//dimensions of the camera's viewing "box"
 			tempY = maxY - minY;
 		}else{
 			tempX = FIELDWIDTH;
 			tempY = FIELDHEIGHT;
 		}
 		//Scale the box so it's in the ratio of the window
 		if(tempX/tempY > SCREENWIDTH/SCREENHEIGHT){
 			tempY = (SCREENHEIGHT/SCREENWIDTH * tempX);
 		}else{
 			tempX = (SCREENWIDTH/SCREENHEIGHT * tempY);
 		}
 		
 		//Limit the zoom to 2x
 		if(tempX < SCREENWIDTH/maxZoom){
 			tempX = SCREENWIDTH/maxZoom;
 			tempY = SCREENHEIGHT/maxZoom;
 		}
 
 		if(data.actionCam()){
 			tempX = (tempX*1.4f);
 			tempY = (tempY*1.4f);
 			
 			targetViewX = minX - (((int)tempX - (maxX - minX))/2);
 			targetViewY = minY - (((int)tempY - (maxY - minY))/2);
 			
 			if(viewX != targetViewX)
 				viewX += (targetViewX-viewX)*deltaf/300f;
 			if(viewY != targetViewY)
 				viewY += (targetViewY-viewY)*deltaf/300f;
 			
 			targetScaleFactor = SCREENWIDTH/tempX;
 			
 			if(scaleFactor != targetScaleFactor)
 				scaleFactor += (targetScaleFactor - scaleFactor)*deltaf/240f;
 			
 		}else{
 			tempX = tempX * 1.2f;
 			tempY = tempY * 1.2f;
 			
 			viewX = -(int)(tempX-FIELDWIDTH)/2;
 			viewY = -(int)(tempY-FIELDHEIGHT)/2;
 			scaleFactor = SCREENWIDTH/tempX;
 		}
 		//System.out.println(scaleFactor);
 		
 		camWidth = tempX;
 		camHeight = tempY;
 	}
 	
 	public void reset(GameContainer gc) throws SlickException{
 		if(mySoundSystem.playing("slow1"))
 			mySoundSystem.pause("slow1");
 		if(mySoundSystem.playing("slow2"))
 			mySoundSystem.pause("slow2");
 		//mySoundSystem.quickPlay( true, "MenuThud.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 		gameStartCountdown = GAMESTARTCOUNTDOWN;
 		gameStartStage = 3;
 //		initFields(gc);
 		resetPositions();
 	}
 
 	public boolean ownGoal(Goal goal){
 		if(GOALTYPE == 7){
 			return false;//no own goals in crazyking
 		}else{
 			return ball.getLastKicker() == goal.getTeam();
 		}
 	}
 	
 	public float dist(Player p){//dist to ball
 		return (float)Math.sqrt((p.getX()-ball.getX())*(p.getX()-ball.getX()) + (p.getY()-ball.getY())*(p.getY()-ball.getY()));
 	}
 	
 	public float dist(float x, float y){//dist to ball
 		return (float)Math.sqrt((x-ball.getX())*(x-ball.getX()) + (y-ball.getY())*(y-ball.getY()));
 	}
 	
 	public float dist(float[] a, float[]b){
 		return (float)Math.sqrt((a[0]-b[0])*(a[0]-b[0])+(a[1]-b[1])*(a[1]-b[1]));
 	}
 	
 	public float dist(float a, float b, float c, float d){
 		return (float)Math.sqrt((c-a)*(c-a)+(d-b)+(d-b));
 	}
 	
 	public float dot(float[] a, float[] b){
 		return a[0]*b[0]+a[1]*b[1];
 	}
 	
 	public float mag(float[] a){
 		return (float)Math.sqrt(a[0]*a[0]+a[1]*a[1]);
 	}
 	
 	public float[] normalNeg(float[] v, float[] w){//orthogonal proj v on w, negative
 		tempX = dot(v,w)/mag(w);//Repurposing this as a temp calculation holder
 		return new float[]{-v[0]+tempX*w[0], -v[1]+tempX*w[1]};
 	}
 	
 	public float[] normal(float[] v, float[] w){
 		if(mag(w)>0){
 			tempX = dot(v,w)/mag(w);//Repurposing this as a temp calculation holder
 			return new float[]{v[0]-tempX*w[0], v[1]-tempX*w[1]};
 		}else{
 			return new float[]{0,0};
 		}
 	}
 	
 	//Parallel component of u on v, written to w
 	public void parallelComponent(float[] u, float[] v, float[] w){
 		tempf = (u[0]*v[0]+u[1]*v[1])/mag(v)/mag(v);
 		w[0] = v[0]*tempf;
 		w[1] = v[1]*tempf;		
 	}
 	
 	public boolean sameDir(float vx, int dir){
 		if(vx == 0)
 			return false;
 		return vx/Math.abs(vx) == (float)dir;
 	}
 	
 	public boolean sameDir(float vx, float dir){
 		if(vx == 0)
 			return false;
 		return vx/Math.abs(vx) == dir/Math.abs(dir);
 	}
 	
 	public void unit(float[] f){
 		if(f[0]==0 && f[1]==0){
 			return;
 		}else if (f[0]==0 && f[1]!=0 ){
 			f[1]=f[1]/Math.abs(f[1]);
 		}else if( f[0]!=0 && f[1]==0){
 			f[0]=f[0]/Math.abs(f[0]);
 		}else{
 			tempf = (float)Math.sqrt(f[0]*f[0]+f[1]*f[1]); 
 			f[0]/= tempf;
 			f[1]/= tempf;
 		}
 	}
 	
 	public float approachTarget(float val, float target, float inc){
 
 		if(val<target){
 			val+=inc;
 			if(val>target)
 				val=target;
 		}
 		if(val>target){
 			val-=inc;
 			if(val<target)
 				val=target;
 		}
 
 		return val;
 	}
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 }
