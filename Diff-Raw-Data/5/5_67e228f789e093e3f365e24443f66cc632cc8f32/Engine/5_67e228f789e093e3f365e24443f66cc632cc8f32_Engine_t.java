 package net.Company;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Label;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.CanvasGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 public class Engine extends BasicGame {
 
 	public Engine(String title) {
 		super(title);
 	}
 
 	/* Settings */
 	public static int AA = 0;
 	public static boolean VSync = true;
 	public static boolean Debug = false;
 	public static int targetFrames = 120;
 	public static boolean Fullscreen = false;
 	public static boolean Resizable = false;
 
 	public static String title = "Game Engine";
 
 	public static int width = 612;
 	public static int height = 384;
 	public static int cenX = width / 2;
 	public static int cenY = height / 2;
 
 	public static int mouseX;
 	public static int mouseY;
 
 	public static CompanyGame game;
 
 	public static ConfigurationManager config;
 
 	boolean hasInitialized = false;
 
 	public static JFrame gameFrame;
 
 	boolean paused = false;
 
 	public static CanvasGameContainer app;
 
 	//public static ScalableGame scaleable;
 
 	public static void setup(String title, CompanyGame game, int x, int y, boolean force) {
 		try {
 			Engine.game = game;
 			Engine.title = title;
 			Engine.width = x;
 			Engine.height = y;
 			cenX = width / 2;
 			cenY = height / 2;
 
 			JFrame frame = null;
 			frame = new JFrame(EngineUtils.isInstalling() ? "Installing!" : "Loading!");
 			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			frame.getContentPane().add(new Label(EngineUtils.isInstalling() ? "Installing!" : "Loading!"), BorderLayout.CENTER);
 			frame.pack();
 			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 			int w = frame.getSize().width;
 			int h = frame.getSize().height;
 			int fx = (dim.width-w)/2;
 			int fy = (dim.height-h)/2;
 			frame.setLocation(fx, fy);
 
 			System.out.println(EngineUtils.getAppDir());
 			EngineUtils.downloadNatives(force);
 
 			config = new ConfigurationManager();
 			config.load();
 			while (EngineUtils.hasGotNatives == false) {
 				if(!frame.isVisible())
 					frame.setVisible(true);
 			}
 
 			frame.dispose();
 
 			System.setProperty(
 					"org.lwjgl.librarypath",
 					EngineUtils.getAppDir() + "/natives/"
 							+ EngineUtils.getOs() + "");
 
 			gameFrame = new JFrame(title);
 			gameFrame.setSize(width, height);
 			gameFrame.setLocationRelativeTo(null);
 
 			//scaleable = new ScalableGame(new Engine(title), width, height);
 			app = new CanvasGameContainer(new Engine(title));
 			app.getContainer().setVSync(VSync);
 			app.getContainer().setMultiSample(AA);
 			app.getContainer().setVerbose(Debug);
 			app.getContainer().setTargetFrameRate(targetFrames);
 			app.getContainer().setShowFPS(Debug);
 			app.getContainer().setAlwaysRender(true);
 
 			gameFrame.add(app);
 			gameFrame.setResizable(true);
 
 			gameFrame.addWindowListener(new WindowAdapter() {
 				@Override
 				public void windowClosing(WindowEvent e) {
 					gameFrame.dispose();
 					app.getContainer().exit();
 					System.exit(0);
 				}
 			});
 			gameFrame.setVisible(true);
 
 			app.start();
 		} catch (UnsatisfiedLinkError e) {
 			setup(title,game,width,height,true);
 		} catch (SlickException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	@Override
 	public void render(GameContainer arg0, Graphics arg1) {
 		if (!hasInitialized)
 			return;
 		game.render(arg0, arg1);
 	}
 
 	@Override
 	public void init(GameContainer arg0) {
 
 		arg0.getInput().addMouseListener(this);
 		arg0.getInput().addKeyListener(this);
 		game.init(arg0);
 		hasInitialized = true;
 	}
 
 	@Override
 	public void update(GameContainer arg0, int arg1) {
 		if (!hasInitialized)
 			return;
 		if(mouseX != arg0.getInput().getMouseX() || mouseY != arg0.getInput().getMouseY()) {
 			mouseX = arg0.getInput().getMouseX();
 			mouseY = arg0.getInput().getMouseY();
 		}
 		game.update(arg0, arg1);
 	}
 
 	@Override
 	public boolean closeRequested() {
 		config.save();
 		return game.close();
 	}
 
 	public void setPaused(boolean paused) {
 		this.paused = paused;
 	}
 
 	public boolean getPaused() {
 		return paused;
 	}
 
 
 	/* Input Listeners */
 
 	@Override
 	public boolean isAcceptingInput() {
 		return Engine.game.isAcceptingInput();
 	}
 
 	@Override
 	public void mouseClicked(int arg0, int arg1, int arg2, int arg3) {
 		Engine.game.mouseClicked(arg0, arg1, arg2, arg3);
 	}
 
 	@Override
 	public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {
		Engine.game.mouseDragged(arg0, arg1, arg2, arg3);
 	}
 
 	@Override
 	public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {
		Engine.game.mouseMoved(arg0, arg1, arg2, arg3);
 	}
 
 	@Override
 	public void mousePressed(int arg0, int arg1, int arg2) {
 		Engine.game.mousePressed(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void mouseReleased(int arg0, int arg1, int arg2) {
 		Engine.game.mouseReleased(arg0, arg1, arg2);
 	}
 
 	@Override
 	public void mouseWheelMoved(int arg0) {
 		Engine.game.mouseWheelMoved(arg0);
 	}
 
 	@Override
 	public void keyPressed(int arg0, char arg1) {
 		Engine.game.keyPressed(arg0, arg1);
 	}
 
 	@Override
 	public void keyReleased(int arg0, char arg1) {
 		Engine.game.keyReleased(arg0, arg1);
 	}
 }
