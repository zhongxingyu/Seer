 package game;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JDialog;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 /**
  * @author mauriceschleusinger
  * 
  */
 public class GUI implements ActionListener {
 	/**
 	 * Using the Java Actionlistener
 	 */
 	private JMenuBar menubar;
 	/**
 	 * new MenuBar which contains the menu-tabs and elements
 	 */
 	private JMenu spiel;
 	/**
 	 * new tab named "Spiel"
 	 */
 	private JMenuItem starten;
 	/**
 	 * new item in Tab "Spiel" named "starten"
 	 */
 	private JMenuItem beenden;
 	/**
 	 * new item in Tab "Spiel" named "beenden"
 	 */
 	private JMenu optionen;
 	/**
 	 * new tab named "Optionen"
 	 */
 	private JMenuItem spname;
 	/**
 	 * new item in Tab "Optionen" named "spname"
 	 */
 	private JMenuItem groesse;
 	/**
 	 * new item in Tab "Optionen" named "groesse"
 	 */
 	private JMenu netzwerk;
 	/**
 	 * new tab named "Netzwerk"
 	 */
 	private JMenuItem startserver;
 	/**
 	 * new item in Tab "Netzwerk" named "startserver"
 	 */
 	private JMenuItem stopserver;
 	/**
 	 * new item in Tab "Netzwerk" named "stopserver"
 	 */
 	private JMenuItem findserver;
 	/**
 	 * new item in Tab "Netzwerk" named "findserver"
 	 */
 	private Launcher frame;
 
 	/**
 	 * @param frame
 	 */
 	public GUI(Launcher frame) {
 
 		this.frame = frame;
 
 		// Create Menubar
 		this.menubar = new JMenuBar();
 
 		// Buttons for "Spiel"
 		this.spiel = new JMenu("Spiel");
 		this.starten = new JMenuItem("Neustarten");
 		this.starten.addActionListener(this);
 		this.beenden = new JMenuItem("Beenden");
 		this.beenden.addActionListener(this);
 		// this.spiel.add(this.starten);
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
		this.netzwerk.add(new JMenuItem("noch nicht verfgbar"));
 		this.menubar.add(this.netzwerk);
 
 		// Buttons for "Optionen"
 		this.optionen = new JMenu("Optionen");
 		/*
 		 * this.spname = new JMenuItem("Spielername"); this.groesse = new
 		 * JMenuItem("Groesse"); this.optionen.add(this.spname);
 		 * this.optionen.add(this.groesse);
 		 */
		this.optionen.add(new JMenuItem("noch nicht verfgbar"));
 		this.menubar.add(this.optionen);
 
 		// set Menubar
 		this.frame.setJMenuBar(this.menubar);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	// this method is called if a button is pressed
 	public void actionPerformed(ActionEvent arg0) {
 		// If the "restart"-button is pressed the game asks to restart the game
 		if (arg0.getSource() == this.starten) {
 			Object[] options = { "Neustart", "abbrechen" };
 			JOptionPane question = new JOptionPane(
 					"Spiel neustarten? Der aktuelle fortschritt geht verloren");
 			question.setOptions(options);
 			JDialog dialog = question.createDialog(this.frame, "Achtung");
 			dialog.setVisible(true);
 			Object obj = question.getValue();
 			if (obj.equals(options[0])) {
 				// restarts the game (not working yet)
 
 				this.frame.game.stop();
 				this.frame.game.start();
 			}
 			dialog.dispose();
 			question.disable();
 			// If the exit-button is pressed the game asks to exit the game
 		} else if (arg0.getSource() == this.beenden) {
 			Object[] options = { "beenden", "abbrechen" };
 			JOptionPane question = new JOptionPane(
 					"Spiel beenden? Der aktuelle fortschritt geht verloren");
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
 
 	}
 }
