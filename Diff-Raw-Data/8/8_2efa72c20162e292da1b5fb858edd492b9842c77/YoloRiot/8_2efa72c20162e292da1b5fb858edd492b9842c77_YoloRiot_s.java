 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 import map.LevelData;
 import map.LevelData.LevelName;
 
 import javax.swing.*;
 
 import java.awt.BorderLayout;
 import java.awt.event.*;
 
 import map.Map;
 import model.Model;
 
 public class YoloRiot extends JFrame implements ActionListener {
 	public static final int SCREEN_WIDTH = 1200;
 	public static final int SCREEN_HEIGHT = 720;
 
 	private static final int TICK = 5;
 
 	private ScreenPanel screen;
 	private Model model;
 	private Map map;
 
 	private ScreenPanel mainScreen;
 	private MapPanel mapPanel;
 	private ItemPanel itemPanel;
 	private StartScreen startScreen;
 	private LoseScreen loseScreen;
 	private WinScreen winScreen;
 
 	private SoundFactory sounds; // use the sounds.playSound(filename) to play a
 									// sound
 
 	private YoloMouse mouse;
 	private YoloKeyboard key;
 
 	private boolean startS = true, mainS = false;
 	public boolean lost = false;
 	public boolean won = false;
 
 	Timer t = new Timer(1000, this);
 	Timer startTimer;
 
 	public YoloRiot() {
 		setTitle("Yolo Riot");
 		setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
 
 		startScreen = new StartScreen();
 		winScreen = new WinScreen ();
 		loseScreen = new LoseScreen ();
 		add(startScreen);
 		startScreen.setVisible(true);
 
		itemPanel = new ItemPanel(model);
		mapPanel = new MapPanel(model, map);
		screen = new ScreenPanel(model, map, mapPanel);
 		
 		makeNewGame ();
		//screen.add(mapPanel);
 		screen.setOpaque(false);
 		screen.setVisible(false);
 		itemPanel.setVisible(false);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		screen.setVisible(false);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		//pack ();
 		setFocusable(true);
 		setVisible(true);
 		
 		startNewGame ();
 	}
 	
 	private void makeNewGame () {
 		if (itemPanel != null) remove(itemPanel);
 		if (screen != null) remove(screen);
 		
 		model = new Model(this);	
 		LevelData startLevel = new LevelData(LevelName.START);
 		map = new Map(startLevel);
 		
 		mapPanel = new MapPanel(model, map);
 		screen = new ScreenPanel(model, map, mapPanel);
 		
 		mouse = new YoloMouse(model, mapPanel);
 		key = new YoloKeyboard(model.getPlayer());
 		sounds = new SoundFactory();
 
 		addMouseListener(mouse);
 		addMouseMotionListener(mouse);
 		addMouseWheelListener(mouse);
 		addKeyListener(key);
 		
 		add(itemPanel, BorderLayout.WEST);
 		add(screen, BorderLayout.EAST);	
 	}
 	
 	private void startNewGame () {
 		t = new Timer(TICK, this);
 		startTimer = new Timer(1000, this);
 		startTimer.start();
 	}
 
 	public void gameLoop() {
 		if (startS) {
 			startScreen.repaint();
 		} else if (lost) {
 			loseScreen.repaint();
 		} else if (won) {
 			winScreen.repaint ();
 			startTimer.stop();
 			try {
 				Thread.sleep(3000);
 			} catch (InterruptedException e) {}
 			makeNewGame ();
 			startNewGame ();
 		} else {
 			model.tick();
 			key.update();
 			screen.repaint();
 		}
 	}
 
 	public static void main(String[] args) {
 		new YoloRiot();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if (won == true) {
 			
 		}
 		
 		if (event.getSource() == startTimer && startS) {
 			startScreen.setVisible(false);
 			screen.setVisible(true);
 			itemPanel.setVisible(true);
 			startS = false;
 			startTimer.stop();
 			t.start();
 		}
 		gameLoop();
 	}
 }
