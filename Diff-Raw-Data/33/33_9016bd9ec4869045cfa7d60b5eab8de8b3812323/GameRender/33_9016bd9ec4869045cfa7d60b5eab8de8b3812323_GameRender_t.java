 //This class renders the game for the client using data acquired from ClientStreamRender.
 //It also operates in-game UI
 //Added 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.swing.ImageIcon;
 import javax.swing.SwingUtilities;
 
 
 public class GameRender implements MouseMotionListener, MouseListener, MouseWheelListener, KeyListener {
 	ScreenManager sm;
 	
 	final int RED_GOAL_RIGHT = -1;
 	final int BLUE_GOAL_RIGHT = 1;
 	
 	int screenWidth;
 	int screenHeight;
 	double mapheight;
 	double mapwidth;
 	
 	double aspectRatio;
 	
 	int faction = 1;
 	
 	int playerNumber;
 	
 	int keyArraySize = 10;
 	
 	private Font font1;
 	private Font font2;
 	private Font font3;
 	
 	boolean debugInfo;
 	boolean mouseClick = false;
 	
 	boolean running;
 	
 	boolean[] keyArray;
 	
 	private static int trigScale = 1;//accuracy of trigonometry tables, in entries per degree
 	public static float[] sin;
 	public static float[] cos;
 	public static float[] tan;
 	
 	double magnification = 1;
 	double gameTime = 0;
 	int gamePeriod=0;
 	double screenXFactor = 1;
 	double screenYFactor = 1;
 	
 	double chargeTime;
 	
 	double leftEdgeX;
 	double topEdgeY;
 	double fieldWidth;
 	
 	Point mouse;
 	Point center;
 	Point screenloc;
 	Point finalPos;
 	Point mouseWorld;
 	
 	BufferedImage fieldImage;
 	BufferedImage bluePlayer;
 	BufferedImage redPlayer;
 	BufferedImage blueActivePlayer;
 	BufferedImage redActivePlayer;
 	BufferedImage ballImage;
 	
 	Robot robot;
 	
 	Random generator;
 	int ballPossessor;
 	int[] playership;
 	int[] displayedUnit;
 	int[] ballArray;
 	
 	int[][] playerArray;
 	
 	int blueScore = 0;
 	int redScore = 0;
 	int goalPosition;
 	
 	ClientStreamReader recieveDataHandler;
 	ObjectOutputStream sender;
 
 	//CLASS OUTLINE//
 	public void gameRender(ScreenManager screenmanager, ClientStreamReader reciever, ObjectOutputStream sendStream) {
 		
 		sm = screenmanager;
 		recieveDataHandler = reciever;
 		sender = sendStream;
 		
 		init();
 		
 		while(running){
 			Graphics2D g = sm.getGraphics();
 			
 			try{
 				networkTransmit();
 			}catch(Exception e){
 				System.err.println("network transmission");
 				e.printStackTrace();
 			}
 			
 			try{
 				renderThings(g);
 			}catch(Exception e){
 				System.err.println("render things");
 				e.printStackTrace();
 			}
 			
 			try{
 				renderUI(g);
 			}catch(Exception e){
 				System.err.println("render ui");
 				e.printStackTrace();
 			}
 			
 			g.dispose();
 			
 			try{
 				sendData();
 			}catch(Exception e){
 				System.err.println("send commands");
 				e.printStackTrace();
 			}
 			
 			//update the screen
 			sm.update();
 
 			try {
 				Thread.sleep(2);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private void init(){
 		running = true;
 		
 		mapheight= 6000;
 		mapwidth = 6000;
 		debugInfo = false;
 		
 		double toRadian = Math.PI/(180*trigScale);
 		
 		sin = new float[(90*trigScale) + 1];
 		for(int i=0;i<sin.length;i++){
 			sin[i] = ((float)Math.sin(((double)i) * toRadian));
 		}
 		
 		//cosine
 		cos = new float[(90*trigScale) + 1];
 		for(int i=0;i<cos.length;i++){
 			cos[i] = ((float)Math.cos(((double)i) * toRadian));
 		}
 		
 		//tangent
 		tan = new float[(90*trigScale) + 1];
 		for(int i=0;i<tan.length;i++){
 			tan[i] = sin[i]/cos[i];
 		}
 		
 		playerNumber = 1;
 		
 		try{
 			robot = new Robot();
 		}catch(Exception e){
 			System.err.println("robot init");
 			System.err.println(e);
 		}
 		
 		finalPos = new Point();
 		mouse = new Point();
 		mouseWorld = new Point();
 		screenloc = new Point();
 		center = new Point();
 		
 		Window w = sm.getFullScreenWindow();
 		w.setBackground(Color.BLACK);
 		w.setForeground(Color.WHITE);
 		
 		screenHeight = w.getHeight();
 		screenWidth = w.getWidth();
 		aspectRatio = screenWidth/(double) (screenHeight);
 		
 		screenXFactor = screenWidth/(double) (mapwidth);
 		screenYFactor = screenHeight/(double) (mapheight);
 		
 		/* initializing font styles */
 		int fontSize = (int)Math.round(/*Font Size:*/12.0/**/*screenWidth*.001);
 		font1 = new Font("Arial", Font.BOLD, fontSize);
 		
 		fontSize = (int)Math.round(/*Font Size:*/6.0/**/*screenWidth*.001);
 		font2 = new Font("Arial", Font.BOLD, fontSize);
 		
 		fontSize = (int)Math.round(/*Font Size:*/8.0/**/*screenWidth*.001);
 		font3 = new Font("Arial", Font.BOLD, fontSize);
 
 		center.x = sm.getWidth() / 2;
 		center.y = sm.getHeight() / 2;
 		
 		mouse.x = center.x;
 		mouse.y = center.y;
 		
 		w.addMouseMotionListener(this);
 		w.addMouseListener(this);
 		w.addMouseWheelListener(this);
 		w.addKeyListener(this);
 		
 		generator = new Random();
 		
 		generateImages();
 		
 		keyArray = new boolean[keyArraySize];
 		
 		playerNumber = recieveDataHandler.playerNumber;
 		
 	}
 	
 	private void networkTransmit() throws IOException{
 		//retrieve arrays
 		gameTime= recieveDataHandler.gameTime;
 		gamePeriod = recieveDataHandler.gamePeriod;
 		playerArray = recieveDataHandler.playerArray;
 		ballArray = recieveDataHandler.ballArray;
 		ballPossessor = recieveDataHandler.ballPossessor;
 		chargeTime = recieveDataHandler.chargeTime;
 		blueScore = recieveDataHandler.blueScore;
 		redScore = recieveDataHandler.redScore;
 		goalPosition = recieveDataHandler.goalPosition;
 	}
 	
 	private void renderThings(Graphics2D g){
 		g.clearRect(0,0,screenWidth,screenHeight);
 		
 		g.drawImage(fieldImage, 0, 0, null);
 		
 		int redPosition;
 		int bluePosition;
 		if(goalPosition == BLUE_GOAL_RIGHT){
 			redPosition = (int)worldXToScreen(leftEdgeX + fieldWidth*.9);
 			bluePosition = (int)worldXToScreen(leftEdgeX);
 		} else {
 			redPosition = (int)worldXToScreen(leftEdgeX + fieldWidth*.9);
 			bluePosition = (int)worldXToScreen(leftEdgeX);
 		}
 		
 		g.setFont(font1);
 		g.setColor(Color.RED);
 		g.drawString("Red Score:" + redScore, bluePosition, (int)(screenHeight*.025));
 		g.setColor(Color.BLUE);
 		g.drawString("Blue Score:" + blueScore, redPosition, (int)(screenHeight*.025));
 		g.setColor(Color.WHITE);
 		g.drawString(timeToString(gameTime), (int)(screenWidth*.9622)/2, (int)(screenHeight*.025));
 		g.drawString("Period: "+gamePeriod, (int)(screenWidth*.35), (int)(screenHeight*.025));
 		g.setFont(font2);
 		
 
 		//entity draw loop
 		for(int i=0; i<playerArray.length; i++){
 			
 			int[] entity = playerArray[i];
 			
 			AffineTransform xform = new AffineTransform();
 			Image image = imageIdToImage((int) (entity[3] + Math.floor(i/5)), i);
 			
 			xform.translate(worldXToScreen(entity[0]), worldYToScreen(entity[1]));
 			xform.scale(400/(255*magnification), 400/(255*magnification));
 			xform.rotate(Math.toRadians(360 - entity[2]));
 			xform.translate(-(image.getWidth(null))/2, -(image.getHeight(null))/2);
 			
 			//draws the entity
 			g.drawImage(image, xform, null);
 			
 			//debug line
 			g.setColor(Color.RED);
 			g.drawLine(worldXToScreen(entity[0]), worldYToScreen(entity[1]), (int)worldXToScreen(entity[0] + cos(entity[2])*300), (int)worldYToScreen(entity[1] + sin(entity[2])*300));
 			
 			//if you possess the ball, draw it attached to you
 			if(ballPossessor == i) {
 				xform = new AffineTransform();
 				
 				xform.translate(worldXToScreen(entity[0] + cos(entity[2])*300), worldYToScreen(entity[1] + sin(entity[2])*300));
 				xform.scale(400/(255*magnification), 400/(255*magnification));
 				xform.translate(-(ballImage.getWidth(null))/2, -(ballImage.getHeight(null))/2);
 				
 				//draws the ball
 				g.drawImage(ballImage, xform, null);
 				
 				//if you're charging a shot, draw the meter
 				if(chargeTime > 0){
 					int meterDiameter = (int) (1000*screenYFactor);
 					BufferedImage meterImage = new BufferedImage(meterDiameter, meterDiameter, BufferedImage.TYPE_4BYTE_ABGR);
 					
 					Graphics2D mI = meterImage.createGraphics();
 					mI.setColor(Color.getHSBColor((float)(1-(chargeTime/GameMechanics.maxChargeTime))/3, 1,(float)Math.min(-2*(chargeTime/GameMechanics.maxChargeTime)+2.5, 1)));
 					mI.fillArc(0, 0, meterDiameter, meterDiameter, 90 ,(int)(360*(chargeTime/GameMechanics.maxChargeTime)));
 									
 					mI.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0));
 					final double METER_INNER_DIAMETER_MULTIPLER=0.2;
 					mI.fillArc((int)(meterDiameter*METER_INNER_DIAMETER_MULTIPLER)/2, (int)(meterDiameter*METER_INNER_DIAMETER_MULTIPLER)/2, (int)(meterDiameter*(1-METER_INNER_DIAMETER_MULTIPLER)), (int)(meterDiameter*(1-METER_INNER_DIAMETER_MULTIPLER)),0,360); 
 				
 					g.drawImage(meterImage, worldXToScreen(entity[0])-meterDiameter/2 , worldYToScreen(entity[1]) -meterDiameter/2, null);
 					
 				}
 			}
 		}
 		
 		if(ballPossessor == -1){//ball draw
 			AffineTransform xform = new AffineTransform();
 			
 			xform.translate(worldXToScreen(ballArray[0]), worldYToScreen(ballArray[1]));
 			xform.scale(400/(255*magnification), 400/(255*magnification));
 			xform.translate(-(ballImage.getWidth(null))/2, -(ballImage.getHeight(null))/2);
 			
 			//draws the ball
 			g.drawImage(ballImage, xform, null);
 		}
 		
 		BufferedImage test;
 		ArrayList<String>text = new ArrayList<String>();
 		text.add("BLUE");
 		text.add("GOAL");
		text.add("SCORED");
		test = generateSplashScreen(250, Color.BLUE,(float) .5, Color.BLACK,text);
		g.drawImage(test,screenWidth/2-test.getWidth()/2, screenHeight/2-test.getWidth()/2, null);
 		
 		
 		g.setColor(Color.RED);
 		g.drawString(mouseWorld.x + ", " + mouseWorld.y,  worldXToScreen(mouseWorld.x), worldYToScreen(mouseWorld.y));
 	}//end operate entities
 	
 	private void renderUI(Graphics2D g){
 		g.setFont(font1);
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		
 	}
 	
 	private void sendData(){
 		try {
 			sender.writeInt(mouseWorld.x);
 			sender.writeInt(mouseWorld.y);
 			
 			for(int i=0;i<keyArraySize;i++){
 				sender.writeBoolean(keyArray[i]);
 			}
 			
 			sender.flush();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	//reposition the mouse using robot, scroll the screen if possible
 	private synchronized void containMouse(int x, int y){
 		Window w = sm.getFullScreenWindow();
 		if(robot != null && w.isShowing()){
 			finalPos.x = x;
 			finalPos.y = y;
 			
 			SwingUtilities.convertPointToScreen(finalPos, w);
 
 			robot.mouseMove(x, y);
 		}
 	}
 	
 	private void generateImages(){//creates pre-built buffered images for the field and players
 		BufferedImage protoImage = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_3BYTE_BGR);
 		Graphics2D g = protoImage.createGraphics();
 		
 		g.setColor(Color.WHITE);
 		
 		//The base measurement
 		fieldWidth = 8000;
 		double fieldHeight = fieldWidth*0.68;
 		
 		
 		leftEdgeX = (mapwidth - fieldWidth)/2;
 		topEdgeY = (mapheight + fieldHeight)/2;
 		
 		//MAIN RECTS
 		//base field rect
 		g.fillRect(worldXToScreen(leftEdgeX), worldYToScreen(topEdgeY), (int)(fieldWidth*screenYFactor), (int)(fieldHeight*screenYFactor));
 		
 		//corners
 		//top right
 		g.setColor(Color.BLACK);
 		Polygon poly = new Polygon();
 		
 		poly.xpoints = new int[]{worldXToScreen(leftEdgeX), worldXToScreen(fieldWidth*.1 + leftEdgeX), worldXToScreen(leftEdgeX)};
 		poly.ypoints = new int[]{worldYToScreen(topEdgeY), worldYToScreen(topEdgeY), worldYToScreen(topEdgeY - fieldHeight*.15)};
 		poly.npoints = 3;
 		
 		g.fillPolygon(poly);
 		
 		//bottom left
 		poly.xpoints = new int[]{worldXToScreen(leftEdgeX), worldXToScreen(leftEdgeX), worldXToScreen(leftEdgeX + fieldWidth*.1)};
 		poly.ypoints = new int[]{worldYToScreen(topEdgeY - fieldHeight*.85), worldYToScreen(topEdgeY - fieldHeight), worldYToScreen(topEdgeY - fieldHeight)};
 		
 		g.fillPolygon(poly);
 		
 		//bottom right
 		poly.xpoints = new int[]{worldXToScreen(leftEdgeX + fieldWidth), worldXToScreen(leftEdgeX + fieldWidth), worldXToScreen(leftEdgeX + fieldWidth*.9)};
 		poly.ypoints = new int[]{worldYToScreen(topEdgeY - fieldHeight*.85), worldYToScreen(topEdgeY - fieldHeight), worldYToScreen(topEdgeY - fieldHeight)};
 		
 		g.fillPolygon(poly);
 		
 		//top right
 		poly.xpoints = new int[]{worldXToScreen(leftEdgeX + fieldWidth), worldXToScreen(leftEdgeX + fieldWidth), worldXToScreen(leftEdgeX + fieldWidth*.9)};
 		poly.ypoints = new int[]{worldYToScreen(topEdgeY - fieldHeight*.15), worldYToScreen(topEdgeY), worldYToScreen(topEdgeY)};
 		
 		g.fillPolygon(poly);
 		
 		//goals
 		//left goal
 		g.setColor(Color.WHITE);
 		
 		double goalDepth = fieldWidth*.053;
 		double goalHeight = goalDepth*2;
 		
 		g.fillRect(worldXToScreen(leftEdgeX - goalDepth), worldYToScreen(topEdgeY-fieldHeight/2+goalHeight/2), (int)(goalDepth*screenYFactor), (int)(goalHeight*screenYFactor));
 		
 		//right goal
 		g.fillRect(worldXToScreen(leftEdgeX+fieldWidth), worldYToScreen(topEdgeY-fieldHeight/2+goalHeight/2), (int)(goalDepth*screenYFactor), (int)(goalHeight*screenYFactor));
 		
 		//LINES
 		//centerline
 		double lineThickness = screenHeight*.04;
 		g.setColor(Color.RED);
 		
 		g.fillRect(worldXToScreen(leftEdgeX + fieldWidth/2 - lineThickness/2), worldYToScreen(topEdgeY), (int)(lineThickness*screenYFactor), (int)(fieldHeight*screenYFactor));
 		
 		//left goal arc
 		double circleRadius = fieldWidth*.25;
 		
 		g.fillArc(worldXToScreen(leftEdgeX - circleRadius/2), 
 				worldYToScreen(topEdgeY - fieldHeight/2 + circleRadius/2), 
 				(int)(circleRadius*screenYFactor), 
 				(int)(circleRadius*screenYFactor), 270, 180);
 		g.setColor(Color.WHITE);
 		g.fillArc(worldXToScreen(leftEdgeX - circleRadius/2 + lineThickness/2), 
 				worldYToScreen(topEdgeY - fieldHeight/2 - lineThickness/2 + circleRadius/2), 
 				(int)((circleRadius - lineThickness*.75)*screenYFactor), 
 				(int)((circleRadius - lineThickness*.75)*screenYFactor), 270, 180);
 		
 		//right goal arc
 		g.setColor(Color.RED);
 		g.fillArc(worldXToScreen(leftEdgeX + fieldWidth - circleRadius/2), 
 				worldYToScreen(topEdgeY - fieldHeight/2 + circleRadius/2), 
 				(int)(circleRadius*screenYFactor), 
 				(int)(circleRadius*screenYFactor), 90, 180);
 		g.setColor(Color.WHITE);
 		g.fillArc(worldXToScreen(leftEdgeX + fieldWidth - circleRadius/2 + lineThickness/2), 
 				worldYToScreen(topEdgeY - fieldHeight/2 - lineThickness/2 + circleRadius/2), 
 				(int)((circleRadius - lineThickness*.75)*screenYFactor), 
 				(int)((circleRadius - lineThickness*.75)*screenYFactor), 90, 180);
 		
 		//left goal box
 		double goalBoxHeight = fieldWidth*.1328;
 		double goalBoxWidth = fieldWidth*.066;
 		g.setColor(Color.BLUE);
 		g.fillRect(worldXToScreen(leftEdgeX), worldYToScreen(topEdgeY - fieldHeight/2 + goalBoxHeight/2), 
 				(int)(goalBoxWidth*screenYFactor), (int)(goalBoxHeight*screenYFactor));
 		g.setColor(Color.WHITE);
 		g.fillRect(worldXToScreen(leftEdgeX), worldYToScreen(topEdgeY - fieldHeight/2 + goalBoxHeight/2 - lineThickness*1.25), 
 				(int)(goalBoxWidth*screenYFactor - lineThickness/4), (int)(goalBoxHeight*screenYFactor - lineThickness/2));
 		
 		//right goal box
 		g.setColor(Color.BLUE);
 		g.fillRect(worldXToScreen(leftEdgeX + fieldWidth - goalBoxWidth), worldYToScreen(topEdgeY - fieldHeight/2 + goalBoxHeight/2), 
 				(int)(goalBoxWidth*screenYFactor), (int)(goalBoxHeight*screenYFactor));
 		g.setColor(Color.WHITE);
 		g.fillRect(worldXToScreen(leftEdgeX + fieldWidth + lineThickness*1.5 - goalBoxWidth), worldYToScreen(topEdgeY - fieldHeight/2 + goalBoxHeight/2 - lineThickness*1.25), 
 				(int)(goalBoxWidth*screenYFactor - lineThickness/4), (int)(goalBoxHeight*screenYFactor - lineThickness/2));
 		
 		//left goal line
 		g.setColor(Color.BLUE);
 		g.fillRect(worldXToScreen(leftEdgeX - lineThickness), worldYToScreen(topEdgeY - fieldHeight/2 + goalHeight/2), (int)(lineThickness*screenYFactor), (int)(goalHeight*screenYFactor));
 		
 		g.fillRect(worldXToScreen(leftEdgeX + fieldWidth), worldYToScreen(topEdgeY - fieldHeight/2 + goalHeight/2), (int)(lineThickness*screenYFactor), (int)(goalHeight*screenYFactor));
 		
 		fieldImage = protoImage;
 		
 		//drawing player entities
 		
 		final int PlayerSizeX=25;
 		final int PlayerSizeY=40;
 		final int ballDiameter=15;
 		final int activeArcDiameter = 60;
 		final double ACTIVE_INNER_DIAMETER_MULTIPLER = .1;
 		BufferedImage BluePlayerTemp = new BufferedImage(PlayerSizeX, PlayerSizeY, BufferedImage.TYPE_3BYTE_BGR);
 		Graphics2D BPT = BluePlayerTemp.createGraphics();
 	
 		BPT.setColor(Color.BLUE);
 		
 		BPT.fillRect(0, 0, PlayerSizeX, PlayerSizeY);
 		
 		BPT.dispose();
 		bluePlayer = BluePlayerTemp;
 			
 		BufferedImage BluePlayerActiveTemp = new BufferedImage(activeArcDiameter, activeArcDiameter, BufferedImage.TYPE_4BYTE_ABGR);
 		Graphics2D BPAT = BluePlayerActiveTemp.createGraphics();
 	
 		BPAT.setColor(Color.YELLOW);
 		
 		BPAT.fillArc(0,0,activeArcDiameter,activeArcDiameter,0,360);
 		
 		BPAT.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0));
 		BPAT.fillArc((int)(activeArcDiameter*ACTIVE_INNER_DIAMETER_MULTIPLER)/2, (int)(activeArcDiameter*ACTIVE_INNER_DIAMETER_MULTIPLER)/2, (int)(activeArcDiameter*(1-ACTIVE_INNER_DIAMETER_MULTIPLER)), (int)(activeArcDiameter*(1-ACTIVE_INNER_DIAMETER_MULTIPLER)),0,360);
 		
 		BPAT.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
 		BPAT.setColor(Color.BLUE);
 		
 		BPAT.fillRect((activeArcDiameter - PlayerSizeX)/2, (activeArcDiameter - PlayerSizeY)/2, PlayerSizeX, PlayerSizeY);
 		
 		BPAT.dispose();
 		blueActivePlayer = BluePlayerActiveTemp;
 		
 		BufferedImage RluePlayerTemp = new BufferedImage(PlayerSizeX, PlayerSizeY, BufferedImage.TYPE_3BYTE_BGR);
 		Graphics2D RPT = RluePlayerTemp.createGraphics();
 	
 		RPT.setColor(Color.RED);
 		
 		RPT.fillRect(0, 0, PlayerSizeX, PlayerSizeY);
 		
 		RPT.dispose();
 		redPlayer = RluePlayerTemp;
 			
 		BufferedImage RedPlayerActiveTemp = new BufferedImage(activeArcDiameter, activeArcDiameter, BufferedImage.TYPE_4BYTE_ABGR);
 		Graphics2D RPAT = RedPlayerActiveTemp.createGraphics();
 	
 		RPAT.setColor(Color.YELLOW);
 		
 		RPAT.fillArc(0,0,activeArcDiameter,activeArcDiameter,0,360);
 
 		RPAT.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 0));
 		RPAT.fillArc((int)(activeArcDiameter*ACTIVE_INNER_DIAMETER_MULTIPLER)/2, (int)(activeArcDiameter*ACTIVE_INNER_DIAMETER_MULTIPLER)/2, (int)(activeArcDiameter*(1-ACTIVE_INNER_DIAMETER_MULTIPLER)), (int)(activeArcDiameter*(1-ACTIVE_INNER_DIAMETER_MULTIPLER)),0,360);
 		
 		RPAT.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
 		RPAT.setColor(Color.RED);
 		
 		RPAT.fillRect((activeArcDiameter - PlayerSizeX)/2, (activeArcDiameter - PlayerSizeY)/2, PlayerSizeX, PlayerSizeY);
 		
 		RPAT.dispose();
 		redActivePlayer = RedPlayerActiveTemp;
 		
 		//drawing the ball
 		BufferedImage ballTemp = new BufferedImage(ballDiameter,ballDiameter, BufferedImage.TYPE_4BYTE_ABGR);
 		Graphics2D BT = ballTemp.createGraphics();
 		BT.setColor(Color.BLUE);
 		BT.fillArc(0, 0, ballDiameter, ballDiameter, 0, 360);
 		
 		BT.dispose();
 		ballImage = ballTemp;
 	}
 	
 	//translates a world coordinate into a screen coordinate with magnification factored in
 	int worldXToScreen(double worldX){
 		return (int) ((screenWidth - screenHeight)*.5 + (worldX/mapwidth)*screenHeight);
 	}
 	int worldYToScreen(double worldY){
 		return (int) ((1 - worldY/mapheight)*screenHeight);
 	}
 	
 	double screenXToWorld(double screenX){
 		return ((screenX - (screenWidth - screenHeight)/2)*(mapwidth/screenHeight));
 	}
 	double screenYToWorld(double screenY){
 		return (1 - screenY/screenHeight)*mapheight;
 	}
 	
 	//Convert seconds into a string for rendering
 	String timeToString(double time){
 		String text = new String();
 		int minutes = (int)time/60;
 		double seconds = time%60;
 		
 		BigDecimal bd = new BigDecimal(String.valueOf(seconds)).setScale(2, BigDecimal.ROUND_HALF_UP);
 		seconds = bd.doubleValue();
 		
 		
 		if(minutes<10)
 		{
 			text = "0" + minutes;
 		}else{	
 			text =  "" + minutes;
 		}
 		if(seconds<10)
 		{
 			text=text+ " : 0" + seconds;
 		}else{
 			text=text+ " : " + seconds;
 			
 		}
 		return text;
 	}
 	
	BufferedImage generateSplashScreen(int width, Color bg, float opacity, Color text, ArrayList<String> strings){
		
		BufferedImage temp = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
 		
 		Graphics2D splash = temp.createGraphics();
 		
		Font boldFont = new Font("Arial", Font.BOLD, 72);
			
 		splash.setColor(bg);
		splash.setFont(boldFont);
		FontMetrics fm = splash.getFontMetrics();
		int height =(int) strings.size()*fm.getHeight()+fm.getHeight()-fm.getMaxAscent();	
		
		temp = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		splash = temp.createGraphics();
 		
		splash.setColor(bg);
		splash.setFont(boldFont);
		fm = splash.getFontMetrics();

 		splash.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
 		
 		splash.fillRect(0,0,width, height);
 		
 		splash.setColor(text);
 		splash.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
 		
 		for(int i = 0; i<strings.size(); i ++){
			Rectangle2D bounds = fm.getStringBounds(strings.get(i),splash);
			double textHeight = fm.getAscent();
			splash.drawString(strings.get(i), (int)(width/2-bounds.getCenterX()),(int)(height/2+3*textHeight/2-fm.getAscent()+(i-Math.floor(strings.size()/2))*textHeight   ));//+((i-strings.size()/2)*textHeight)));
 		}
 		
 		return temp;
 	}
 	/*custom trigonometry functions*/
 	//cosine
 	public double cos(int a){
 		//normalizing
 		if(a>360){
 			a %= 360;
 		}else if(a<0){
 			a %= 360;
 			a += 360;
 		}
 		
 		//return
 		if(a <= 90){
 			return cos[a];
 		}else if(a <= 180){
 			return -cos[180 - a];
 		}else if(a <= 270){
 			return -cos[a - 180];
 		}else{
 			return cos[360 - a];
 		}
 	}
 	
 	//sine
 	public double sin(int a){
 		//normalizing
 		if(a>360){
 			a %= 360;
 		}else if(a<0){
 			a %= 360;
 			a += 360;
 		}
 		
 		//return
 		if(a <= 90){
 			return sin[a];
 		}else if(a <= 180){
 			return sin[180 - a];
 		}else if(a <= 270){
 			return -sin[a - 180];
 		}else{
 			return -sin[360 - a];
 		}
 	}
 	
 	//tan
 	public double tan(int a){
 		//normalizing
 		if(a>360){
 			a %= 360;
 		}else if(a<0){
 			a += 360;
 		}
 		
 		//return
 		if(a <= 90){
 			return tan[a];
 		}else if(a <= 180){
 			return -tan[a - 90];
 		}else if(a <= 270){
 			return tan[a - 180];
 		}else{
 			return -sin[a - 270];
 		}
 	}
 	
 	//custom mathematical square function
 	double sqr(double i){
 		return i*i;
 	}
 	
 	//mouse listener methods
 	public void mouseDragged(MouseEvent e){
 		mouse.x = e.getX();
 		mouse.y = e.getY();
 		
 		mouseWorld.x = (int) screenXToWorld(mouse.x);
 		mouseWorld.y = (int) screenYToWorld(mouse.y);
 	}
 
 	public synchronized void mouseMoved(MouseEvent e){
 		mouse.x = e.getX();
 		mouse.y = e.getY();
 		
 		mouseWorld.x = (int) screenXToWorld(mouse.x);
 		mouseWorld.y = (int) screenYToWorld(mouse.y);
 	}
 	
 public void mousePressed(MouseEvent e) {
 		if(SwingUtilities.isLeftMouseButton(e)){
 			keyArray[0] = true;
 		} else if (SwingUtilities.isRightMouseButton(e)){
 			keyArray[1] = true;
 		}
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 		if(SwingUtilities.isLeftMouseButton(e)){
 			keyArray[0] = false;
 		} else if (SwingUtilities.isRightMouseButton(e)){
 			keyArray[1] = false;
 		}
 	}
 	
 	public void mouseEntered(MouseEvent e) {}
 	
 	public void mouseClicked(MouseEvent e) {}
 
 	public void mouseExited(MouseEvent e) {}
 
 	public void mouseWheelMoved(MouseWheelEvent e) {
 	
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		switch(e.getKeyCode()){
 		case KeyEvent.VK_ESCAPE ://escape key ends the script
 			running = false;
 			break;
 		case KeyEvent.VK_W :
 			keyArray[2] = true;
 			break;
 		case KeyEvent.VK_A :
 			keyArray[3] = true;
 			break;
 		case KeyEvent.VK_S :
 			keyArray[4] = true;
 			break;
 		case KeyEvent.VK_D :
 			keyArray[5] = true;
 			break;
 		case KeyEvent.VK_SHIFT :
 			keyArray[6] = true;
 			break;
 		case KeyEvent.VK_CONTROL :
 			keyArray[7] = true;
 			break;
 		case KeyEvent.VK_ALT :
 			keyArray[8] = true;
 			break;
 		case KeyEvent.VK_SPACE :
 			keyArray[9] = true;
 			break;
 		default :
 			break;
 		}
 	}
 	
 	public void keyReleased(KeyEvent e){
 		
 		switch(e.getKeyCode()){
 		case KeyEvent.VK_W :
 			keyArray[2] = false;
 			break;
 		case KeyEvent.VK_A :
 			keyArray[3] = false;
 			break;
 		case KeyEvent.VK_S :
 			keyArray[4] = false;
 			break;
 		case KeyEvent.VK_D :
 			keyArray[5] = false;
 			break;
 		case KeyEvent.VK_SHIFT :
 			keyArray[6] = false;
 			break;
 		case KeyEvent.VK_CONTROL :
 			keyArray[7] = false;
 			break;
 		case KeyEvent.VK_ALT :
 			keyArray[8] = false;
 			break;
 		case KeyEvent.VK_SPACE :
 			keyArray[9] = false;
 			break;
 		default :
 			break;
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {}
 	
 	Image imageIdToImage(int id, int player){
 		switch(id + faction){
 		case 1:
 			if(player == playerNumber){
 				return blueActivePlayer;
 			}else{
 				return bluePlayer;
 			}
 		case 2:
 			if(player == playerNumber){
 				return redActivePlayer;
 			}else{
 				return redPlayer;
 			}
 		default:
 			System.out.println("returned null from imageIdToImage!");
 			return null;
 		}
 	}
 }
