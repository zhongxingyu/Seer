 package gui;
 
 import game.Game;
 import game.Settings;
 import game.highscore.HighscoreGui;
 import game.highscore.HighscoreManager;
 
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 import level.Editor;
 import level.Generator;
 import network.Discover;
 import sound.Soundmanager;
 
 /**
  * @author mauriceschleusinger
  * 
  */
 public class GUI implements ActionListener {
 	/**
 	 * new MenuBar which contains the menu-tabs and elements
 	 */
 	private JMenuBar menubar;
 	/**
 	 * new tab named "Spiel"
 	 */
 	private JMenu spiel;
 	/**
 	 * new item in Tab "Spiel" named "starten"
 	 */
 	private JMenuItem starten;
 	/**
 	 * new item in Tab "Spiel" named "beenden"
 	 */
 	private JMenuItem beenden;
 	/**
 	 * new item in tab "Spiel" named highscore
 	 */
 	private JMenuItem highscore;
 	/**
 	 * new tab named "Optionen"
 	 */
 	private JMenu optionen;
 	/**
 	 * new tab named "Optionen"
 	 */
 	private JMenuItem delScore;
 	/**
 	 * new item in Tab "Optionen" named "spname"
 	 */
 	private JMenuItem spname;
 	/**
 	 * new item in Tab "Optionen" named "groesse"
 	 */
 	private JMenuItem groesse;
 	/**
 	 * new tab named "Netzwerk"
 	 */
 	private JMenu netzwerk;
 	/**
 	 * new item in Tab "Netzwerk" named "startserver"
 	 */
 	private JMenuItem startserver;
 	/**
 	 * new item in Tab "Netzwerk" named "stopserver"
 	 */
 	private JMenuItem stopserver;
 	/**
 	 * new item in Tab "Netzwerk" named "findserver"
 	 */
 	private JMenuItem findserver;
 	/**
 	 * new tab named "leveleditor"
 	 */
 	private JMenu leveleditor;
 	/**
 	 * new item in Tab "leveleditor" named "offnen"
 	 */
 	private JMenuItem offnen;
 
 	private Container frame;
 	private Discover discover;
 	private Object[] old_list;
 	private JButton connectButton;
 	private Game game;
 	private JCheckBoxMenuItem sound;
 	private JMenuItem connectIP;
 	private JMenuItem generate;
 	private JMenuItem about;
 
 	/**
 	 * @param frame
 	 */
 	public GUI(Container frame, Game g) {
 		this.game = g;
 
 		this.frame = frame;
 
 		// Create Menubar
 		this.menubar = new JMenuBar();
 
 		// Buttons for "Spiel"
 		this.spiel = new JMenu("Spiel");
 		this.starten = new JMenuItem("Neustarten");
 		this.starten.addActionListener(this);
 		this.beenden = new JMenuItem("Beenden");
 		this.beenden.addActionListener(this);
 		this.highscore = new JMenuItem("Highscore");
 		this.highscore.addActionListener(this);
 
 		this.generate = new JMenuItem("Map generieren");
 		this.generate.addActionListener(this);
 
 		// this.spiel.add(this.starten);
 		this.spiel.add(this.highscore);
 		this.spiel.add(this.generate);
 		this.spiel.add(this.beenden);
 		this.menubar.add(this.spiel);
 
 		// Buttons for "Netzwerk"
 		this.netzwerk = new JMenu("Netzwerk");
 		/*
 		 * this.startserver = new JMenuItem("Server starten"); this.stopserver =
 		 * new JMenuItem("Server beenden"); this.findserver = new
 		 * JMenuItem("Server suchen"); this.netzwerk.add(this.startserver);
 		 * this.netzwerk.add(this.stopserver);
 		 * this.netzwerk.add(this.findserver);
 		 */
 		this.findserver = new JMenuItem("Server suchen");
 		this.findserver.addActionListener(this);
 
 		this.connectIP = new JMenuItem("Internet");
 		this.connectIP.addActionListener(this);
 
 		this.netzwerk.add(this.findserver);
 		this.netzwerk.add(this.connectIP);
 		this.menubar.add(this.netzwerk);
 
 		// Buttons for "Optionen"
 		this.optionen = new JMenu("Optionen");
 		this.delScore = new JMenuItem("Highscore l\u00F6schen");
 		this.delScore.addActionListener(this);
 
 		// Button for Sound
 		this.sound = new JCheckBoxMenuItem("Sound");
 		this.sound.addActionListener(this);
 		Settings s = Settings.getInstance();
 		this.sound.setSelected(Soundmanager.getInstance().enabled());
 
 		this.optionen.add(this.sound);
 
 		/*
 		 * this.spname = new JMenuItem("Spielername"); this.groesse = new
 		 * JMenuItem("Groesse"); this.optionen.add(this.spname);
 		 * this.optionen.add(this.groesse);
 		 */
 		this.optionen.add(this.delScore);
 		this.menubar.add(this.optionen);
 
 		// Buttons for "Leveleditor"
 
 		this.leveleditor = new JMenu("Leveleditor");
 		this.offnen = new JMenuItem("\u00D6ffnen");
 		this.offnen.addActionListener(this);
 		this.leveleditor.add(this.offnen);
 
 		this.menubar.add(this.leveleditor);
 
		this.about = new JMenuItem("/u00FCber");
 		this.about.addActionListener(this);
 		this.menubar.add(this.about);
 
 		// set Menubar
 		if (this.frame instanceof JFrame) {
 			((JFrame) this.frame).setJMenuBar(this.menubar);
 		} else {
 			((JApplet) this.frame).setJMenuBar(this.menubar);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * this method is called if a button is pressed
 	 * 
 	 * @see
 	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// If the "restart"-button is pressed the game asks to restart the game
 		if (arg0.getSource() == this.starten) {
 			Object[] options = { "Neustart", "Abbrechen" };
 			JOptionPane question = new JOptionPane(
 					"Spiel neustarten? Der aktuelle Fortschritt geht verloren");
 			question.setOptions(options);
 			JDialog dialog = question.createDialog(this.frame, "Achtung");
 			dialog.setVisible(true);
 			Object obj = question.getValue();
 			if (obj.equals(options[0])) {
 				// restarts the game (not working yet)
 
 				this.game.stop();
 				this.game.start();
 			}
 			dialog.dispose();
 			// If the exit-button is pressed the game asks to exit the game
 		} else if (arg0.getSource() == this.beenden) {
 			Object[] options = { "Beenden", "Abbrechen" };
 			JOptionPane question = new JOptionPane(
 					"Spiel beenden? Der aktuelle Fortschritt geht verloren");
 			question.setOptions(options);
 			JDialog dialog = question.createDialog(this.frame, "Achtung");
 			dialog.setVisible(true);
 			Object obj = question.getValue();
 			// ends the game and closes the JFrame
 			if (obj.equals(options[0])) {
 				System.exit(0);
 			}
 			dialog.dispose();
 		}
 
 		// if "offnen" is pressed
 
 		else if (arg0.getSource() == this.offnen) {
 
 			Editor ed = new Editor(this.frame);
 
 		}
 		if (arg0.getSource() == this.highscore) {
 
 			HighscoreGui hg = new HighscoreGui(this.frame);
 			hg.createAndShowHighscore();
 
 		}
 		if (arg0.getSource() == this.delScore) {
 			int choice;
 			choice = JOptionPane
 					.showConfirmDialog(
 							this.frame,
 							"Sind Sie sicher, dass sie den Highscore l\u00F6schen m\u00F6chten?",
 							"Highscore l\u00F6schen", JOptionPane.YES_NO_OPTION);
 			if (choice == 0) {
 				HighscoreManager hm = new HighscoreManager();
 				hm.getScores().clear();
 				hm.updateScoreFile();
 			}
 		}
 
 		if (arg0.getSource() == this.findserver) {
 			new Serverbrowser(this.frame);
 		}
 		if (arg0.getSource() == this.connectIP) {
 			new ConnectToIp(this.frame);
 		}
 		if (arg0.getSource() == this.sound) {
 			if (this.sound.isSelected()) {
 				Soundmanager.getInstance().enable();
 			} else {
 				Soundmanager.getInstance().disable();
 			}
 			Settings.getInstance().set("sound", this.sound.isSelected());
 		}
 
 		if (arg0.getSource() == this.generate) {
 			Generator g = new Generator();
 			g.generateMap(11, 11);
 			this.game.init("genMap");
 		}
 
 		if (arg0.getSource() == this.about) {
 			new About(this.frame);
 		}
 	}
 }
