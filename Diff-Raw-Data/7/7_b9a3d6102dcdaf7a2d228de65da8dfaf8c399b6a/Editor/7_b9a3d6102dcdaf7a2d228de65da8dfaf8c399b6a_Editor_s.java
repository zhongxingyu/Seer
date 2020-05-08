 package level;
 
 import game.Main;
 import game.MouseHandler;
 import graphics.Image;
 import graphics.Sprite;
 
 import java.awt.Canvas;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.Scanner;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 /**
  * 
  * @author kyra
  * 
  */
 
 public class Editor extends Canvas implements ActionListener {
 
 	/**
 	 * new MenuBar named "menubar"
 	 */
 
 	private JMenuBar menubar;
 
 	/**
 	 * new JMenu named "level"
 	 */
 
 	private JMenu level;
 
 	/**
 	 * new JMenu named "komponente"
 	 */
 
 	private JMenu komponente;
 	/**
 	 * new JMenuItem named "laden"
 	 */
 	private JMenuItem laden;
 
 	/**
 	 * new JMenuItem named "speichern"
 	 */
 	private JMenuItem speichern;
 	/**
 	 * new JMenuItem named "neu"
 	 */
 	private JMenuItem neu;
 
 	private JMenuItem breakable;
 
 	private JMenuItem unbreakable;
 
 	private JMenuItem player;
 
 	private JMenuItem finish;
 
 	private JMenuItem empty;
 	/**
 	 * new JFrame named editorframe
 	 */
 	private JFrame editorframe;
 
 	private int element;
 
 	private String filename;
 	private Image images[][];
 	int x = 0, type, y = 0;
 
 	private Scanner maps;
 
 	/**
 	 * default constructor for Editor, sets frame and buttons
 	 */
 
 	public Editor(Container frame) {
 
 		/**
 		 * create new JFrame called "Leveleditor" and set properties
 		 */
 
 		JFrame editorframe = new JFrame("Leveleditor");
 
 		editorframe.setTitle("Leveleditor");
 		editorframe.setSize(500, 500);
 		editorframe.setLocationRelativeTo(frame);
 		editorframe.setVisible(true);
 
 		MouseHandler mousehandler = new MouseHandler();
 		editorframe.addMouseListener(mousehandler);
 
 		/**
 		 * create new JMenuBar
 		 */
 
 		this.menubar = new JMenuBar();
 
 		/**
 		 * create new JMenu "Level" and JMenuItems "laden", "speichern", "neu"
 		 */
 		this.level = new JMenu("Level");
 
 		this.laden = new JMenuItem("Laden");
 		this.laden.addActionListener(this);
 
 		this.speichern = new JMenuItem("Speichern");
 		this.speichern.addActionListener(this);
 
 		this.neu = new JMenuItem("Neu");
 		this.neu.addActionListener(this);
 
 		this.level.add(this.laden);
 		this.level.add(this.speichern);
 		this.level.add(this.neu);
 		this.menubar.add(this.level);
 
 		this.komponente = new JMenu("Komponente");
 
		this.breakable = new JMenuItem("Zerstörbare Wand");
 		this.breakable.addActionListener(this);
 
		this.unbreakable = new JMenuItem("Unzerstörbare Wand");
 		this.unbreakable.addActionListener(this);
 
 		this.finish = new JMenuItem("Ziel");
 		this.finish.addActionListener(this);
 
 		this.player = new JMenuItem("Spieler");
 		this.player.addActionListener(this);
 
 		this.empty = new JMenuItem("Komponente entfernen");
 		this.empty.addActionListener(this);
 
 		this.komponente.add(this.breakable);
 		this.komponente.add(this.unbreakable);
 		this.komponente.add(this.player);
 		this.komponente.add(this.finish);
 		this.komponente.add(this.empty);
 		this.menubar.add(this.komponente);
 
 		/**
 		 * set menubar
 		 */
 
 		editorframe.setJMenuBar(this.menubar);
 
 		this.loadMap("Map3");
 
 	}
 
 	/**
 	 * override actionPerformed(ActionEvent arg0) to determine what happens if a
 	 * button is pressed
 	 */
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 
 		// if "laden" is pressed, create FileChooser
 		if (arg0.getSource() == this.laden) {
 			JFileChooser fileChooser = new JFileChooser();
 
 			File f = new File(Main.class.getResource("/ressources/maps/")
 					.getPath());
 			fileChooser.setCurrentDirectory(f);
 			int returnVal = fileChooser.showOpenDialog(this.editorframe);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				this.loadMap(fileChooser.getSelectedFile().getName());
 			}
 
 		}
 
 		// if "speichern" is pressed, create FileChooser
 
 		if (arg0.getSource() == this.speichern) {
 
 			JFileChooser fileChooser = new JFileChooser();
 
 			File f = new File(Main.class.getResource("/ressources/maps/")
 					.getPath());
 			fileChooser.setCurrentDirectory(f);
 			int returnVal = fileChooser.showSaveDialog(this.editorframe);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				// save file
 			}
 
 			// if "neu" is pressed, load default Map
 			if (arg0.getSource() == this.neu) {
 				this.loadMap("Map1");
 
 			}
 
 			// if "unbreakable" is pressed
 
 			if (arg0.getSource() == this.unbreakable) {
 				this.element = 2;
 			}
 
 			// if "breakable" is pressed
 			if (arg0.getSource() == this.breakable) {
 				this.element = 1;
 			}
 
 			// if "player" is pressed
 			if (arg0.getSource() == this.player) {
 				this.element = 3;
 			}
 
 			// if "empty" is pressed
 			if (arg0.getSource() == this.empty) {
 				this.element = 0;
 			}
 
 			// if "finish" is pressed
 			if (arg0.getSource() == this.finish) {
 				this.element = 4;
 
 			}
 		}
 	}
 
 	/**
 	 * method to draw Map
 	 */
 
 	public void loadMap(String filename) {
 
 		try {
 
 			this.maps = new Scanner(
 					Main.class.getResourceAsStream("/ressources/maps/"
 							+ filename));
 			while (this.maps.hasNextLine()) {
 				String text = this.maps.nextLine();
 				for (this.x = 0; this.x < text.length(); this.x++) {
 					this.element = Integer.parseInt("" + text.charAt(this.x));
 					if (this.element == 0) {
 						this.images = Sprite.load("background.png", 100, 100);
 					} else if (this.element == 1) {
 						this.images = Sprite.load("w1.png", 100, 100);
 					} else if (this.element == 2) {
 						this.images = Sprite.load("wall.png", 100, 100);
 					} else if (this.element == 3) {
 						this.images = Sprite.load("bomberman.png", 55, 90);
 
 						// Game.staticBackground.add(this.images =
 						// Sprite.load("background.png", 100, 100));
 					} else if (this.element == 4) {
 						this.images = Sprite.load("finish.png", 100, 100);
 					} else if (this.element == 5) {
 						//
 
 					}
 
 				}
 				this.y++;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void setComponent(int x, int y) {
 		this.x = x;
 		this.y = y;
 
 		// set position
 		if (this.element == 0) {
 			this.images = Sprite.load("background.png", 100, 100);
 		} else if (this.element == 1) {
 			this.images = Sprite.load("w1.png", 100, 100);
 		} else if (this.element == 2) {
 			this.images = Sprite.load("wall.png", 100, 100);
 		} else if (this.element == 3) {
 			this.images = Sprite.load("bomberman.png", 55, 90);
 		} else if (this.element == 4) {
 			this.images = Sprite.load("finish.png", 100, 100);
 		} else if (this.element == 5) {
 			//
 
 		}
 
 	}
 
 }
