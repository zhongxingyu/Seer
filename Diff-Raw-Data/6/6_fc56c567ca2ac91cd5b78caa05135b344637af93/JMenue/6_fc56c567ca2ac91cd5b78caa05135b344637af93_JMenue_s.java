 package Bomberman;
 
 import java.awt.FlowLayout;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import multiplayer.MyClientSocket;
 import multiplayer.MyServerSocket;
 
 public class JMenue extends JFrame implements ActionListener {
 
 	/**
 	 * Spielfeldgroesse
 	 */
 	public static int mapWidth = 19;
 	public static int mapHeight = 19;
 	/**
 	 * Kachelgroesse
 	 */
 	public static final int tileWidth = 32;
 	public static final int tileHeight = 32;
 	/**
 	 * Zahl der Level
 	 */
 	public static int max = 16;
 
 	public static JFeld feld;
 	public static JJFrame frame;
 	static Figur bm1, bm2;
 	public static Sounds sound = new Sounds();
 	public static boolean stopper = true;
 
 	/**
 	 * Zeichnet das Feld immer wieder neu
 	 */
 	static javax.swing.Timer t = new javax.swing.Timer(1, new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			frame.repaint();
 		}
 	});
 
 	private static final long serialVersionUID = 1L;
 
 	public JMenue(String t) {
 		sound.start();
 		setTitle(t);
 		LayoutManager manager = new FlowLayout();
 		setLayout(manager);
 
 		JButton spielstarten1P = new JButton("Einzelspieler");
 		spielstarten1P.setActionCommand("go1");
 		spielstarten1P.addActionListener(this);
 		add(spielstarten1P);
 		JButton spielstarten2P = new JButton("Mehrspieler");
 		spielstarten2P.setActionCommand("go2");
 		spielstarten2P.addActionListener(this);
 		add(spielstarten2P);
 		JButton spielstarten3P = new JButton("Netzwerk (Host)");
 		spielstarten3P.setActionCommand("go3");
 		spielstarten3P.addActionListener(this);
 		add(spielstarten3P);
 		JButton spielstarten4P = new JButton("Netzwerk (Client)");
 		spielstarten4P.setActionCommand("go4");
 		spielstarten4P.addActionListener(this);
 		add(spielstarten4P);
 		JButton tut = new JButton("Tutorial");
 		tut.setActionCommand("go5");
 		tut.addActionListener(this);
 		add(tut);
 		JButton spielbeenden = new JButton("Spiel beenden");
 		spielbeenden.setActionCommand("exit");
 		spielbeenden.addActionListener(this);
 		add(spielbeenden);
 		pack();
 		setLocationRelativeTo(null);
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	public void actionPerformed(ActionEvent arg0) {
 
 		// Spiel beenden
 		if (arg0.getActionCommand().equals("exit")) {
 			System.exit(0);
 		}
 		// Spiel starten 1P
 		if (arg0.getActionCommand().equals("go1")) {
 			// Spielfeld auslesen
 			setVisible(false);
 			stopper = false;// fuer Menuesound
 			String nr = null;
 			int zahl = 0;
 			boolean check = true;
 			while (nr == null || Math.abs(zahl) > max || !check) {
 				check = true;
 				nr = JOptionPane
 						.showInputDialog("Waehlen Sie ein Level X aus. (1-"
 								+ max
 								+ ")\nTipp: Waehle -X fuer eine zufaellige Verteilung der zerstoerbaren Bloecke.");
 				// abbrechen
 				if (nr == null) {
 					break;
 				}
 				try {
 					zahl = Integer.parseInt(nr);
 					if (nr.substring(0, 1).equals("0") && !nr.equals("0")) {
 						JOptionPane.showMessageDialog(null,
 								"Kein gueltiges Level.");
 						check = false;
 					}
 					if (nr.equals("0")) {
 						nr = "" + (int) (Math.random() * (JMenue.max) + 1);
 					}
 					if (nr.equals("-0")) {
 						nr = "-" + (int) (Math.random() * (JMenue.max) + 1);
 					}
 				} catch (NumberFormatException e) {
 					JOptionPane
 							.showMessageDialog(null, "Kein gueltiges Level.");
 				}
 			}
 			if (nr != null) {
 				Mapreader create = new Mapreader(nr);
 				feld = new JFeld(create.getWidth(), create.getHeight(),
 						tileWidth, tileHeight, nr, false);
 				frame = new JJFrame(mapWidth, mapHeight, tileWidth, tileHeight,
 						feld, nr);
 				bm1 = new Figur(1, 1, 1);
 				new Control(frame, bm1, feld, 0, null);
 				t.start();
 			} else {
 				setVisible(true);
 			}
 		}
 		/**
 		 * 2 Player Spiel starten
 		 */
 		if (arg0.getActionCommand().equals("go2")) {
 			// Spielfeld auslesen
 			setVisible(false);
 			stopper = false;// fuer Menuesound
 			String nr = null;
 			int zahl = 0;
 			boolean check = true;
			while (nr == null || Math.abs(zahl) < 1 || Math.abs(zahl) > max
					|| !check) {
 				check = true;
 				nr = JOptionPane
 						.showInputDialog("Waehlen Sie ein Level X aus. (1-"
 								+ max
 								+ ")\nTipp: Waehle -X fuer eine zufaellige Verteilung der zerstoerbaren Bloecke.");
 				if (nr == null) {
 					break;
 				}
 				try {
 					zahl = Integer.parseInt(nr);
 
					if (nr.substring(0, 1).equals("0")) {
 						JOptionPane.showMessageDialog(null,
 								"Kein gueltiges Level.");
 						check = false;
 
 					}
 					if (nr.equals("0")) {
 						nr = "" + (int) (Math.random() * (JMenue.max) + 1);
 					}
 					if (nr.equals("-0")) {
 						nr = "-" + (int) (Math.random() * (JMenue.max) + 1);
 					}
 				} catch (NumberFormatException e) {
 					JOptionPane
 							.showMessageDialog(null, "Kein gueltiges Level.");
 				}
 			}
 			if (nr != null) {
 				Mapreader create = new Mapreader(nr);
 				feld = new JFeld(create.getWidth(), create.getHeight(),
 						tileWidth, tileHeight, nr, true);
 				frame = new JJFrame(mapWidth, mapHeight, tileWidth, tileHeight,
 						feld, nr);
 				bm1 = new Figur(1, 1, 1);
 				bm2 = new Figur(mapHeight - 2, mapWidth - 2, 2);
 				new Control(frame, bm1, feld, 0, null);
 				new Control(frame, bm2, feld, 1, null);
 				t.start();
 			} else {
 				setVisible(true);
 			}
 
 		}
 
 		/**
 		 * Multiplayer Host
 		 */
 		if (arg0.getActionCommand().equals("go3")) {
 			// Spielfeld auslesen
 			setVisible(false);
 			stopper = false;// fuer Menuesound
 			String nr = "1";
 
 			// statisches Level: 1
 			Mapreader create = new Mapreader(nr);
 			feld = new JFeld(create.getWidth(), create.getHeight(), tileWidth,
 					tileHeight, nr, true);
 			frame = new JJFrame(mapWidth, mapHeight, tileWidth, tileHeight,
 					feld, nr);
 			bm1 = new Figur(1, 1, 1);
 			bm2 = new Figur(mapHeight - 2, mapWidth - 2, 2);
 
 			// Instanz des Server-Sockets erstellen
 			MyServerSocket sSocket = new MyServerSocket(bm2);
 
 			Control control = new Control(frame, bm1, feld, 0, sSocket);
 			// übergabe der Control-Instanz an das Server-Socket
 			sSocket.setControl(control);
 
 			t.start();
 
 		}
 
 		/**
 		 * Multiplayer Client
 		 */
 		if (arg0.getActionCommand().equals("go4")) {
 			// Spielfeld auslesen
 			setVisible(false);
 			stopper = false;// fuer Menuesound
 			String nr = "1";
 
 			// statisches Level: 1
 			Mapreader create = new Mapreader(nr);
 			feld = new JFeld(create.getWidth(), create.getHeight(), tileWidth,
 					tileHeight, nr, true);
 			frame = new JJFrame(mapWidth, mapHeight, tileWidth, tileHeight,
 					feld, nr);
 			bm1 = new Figur(1, 1, 1);
 			bm2 = new Figur(mapHeight - 2, mapWidth - 2, 2);
 
 			// Abfrage für die IP-Adresse der Servers
 			String ip = JOptionPane.showInputDialog(frame, "IP des Hosts:",
 					"IP", JOptionPane.QUESTION_MESSAGE);
 
 			// Instanz des Client-Sockets erstellen
 			MyClientSocket cSocket = new MyClientSocket(ip, bm1);
 
 			Control control = new Control(frame, bm2, feld, 0, cSocket);
 			// Control-Instanz an den Client-Socket übergeben
 			cSocket.setControl(control);
 			t.start();
 
 		}
 	}
 
 }
