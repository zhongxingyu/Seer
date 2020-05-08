 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 /**
  * 
  * @author Kolja Salewski
  *
  */
 public class Menue implements KeyListener {
 
 	// Deklaration & Initialisierung:
 	/**
 	 * Hauptframe des Programmes
 	 */
 	private JFrame frame;
 	
 	/**
 	 * Button im Leistenmenue
 	 * (Schliesst das Programm)
 	 */
 	private final Action_Beenden Action_Beenden = new Action_Beenden(); 	// Aktion zum Beenden des
 																			// Spiels erstellen
 	
 	/**
 	 * Button im Leistenmenue
 	 * (Neustart des Spieles)
 	 */
 	private final Action_Neu Action_Neu = new Action_Neu(); 	// Aktion zum Neustart des
 																// Spiels erstellen
 	
 	/**
 	 * Button im Leistenmenue
 	 * (Wechsel zum Singleplayer-Modus)
 	 */
 	private final Action_Singleplayer Action_Singleplayer = new Action_Singleplayer(); 	// Aktion zum Wechsel in den Singleplayer-Modus erstellen
 	
 	/**
 	 * Button im Leistenmenue
 	 * (Wechsel zum Multiplayer-Modus)
 	 */
 	private final Action_Multiplayer Action_Multiplayer = new Action_Multiplayer(); 	// Aktion zum Wechsel in den Multiplayer-Modus erstellen
 	
 	public static boolean twoPlayer = false;															
 	private static int[][] map; 		// Internes Spielfeld
 	
 	/**
 	 * Objekt der Map()-Klasse; enthaelt die Daten des Spielfeldes;
 	 */
 	private static Map game; 			// Grafisches Spielfeld
 	
 	/**
 	 * enthalet die Informationen ueber die Spielerposition ((x,y)-Koordinate)
 	 */
 	private static Hulk hulk1 , hulk2;	// Spielfiguren
 	
 	private static int[] a;
 	public boolean spiel_neugestartet;
 
 	// Konstruktor:
 	/**
 	 * {@code initialize()} legt die Panels in das JFrame {@code frame()} & erstellt die grafische Oberflaeche des Spieles
 	 */
 	public Menue() {
 		spiel_neugestartet = false;
 		hulk1 = new Hulk(1,1,1);
 		hulk2 = new Hulk(9,9,10);
 		
 		map = MapLoader.laden(1); 
 		game = new Map(map);
 		
 		initialize();
 		a = new int[3];
 		
 	}
 
 	// Methode zum Initialisieren des Spielfelds:
 	
 	/**
 	 * Initialisiert das Spielfeld (Panels, frames etc.), 
 	 */
 	private void initialize() {
 		frame = new JFrame(); // Fenster erstellen
 		frame.setTitle("Bomberhulk"); // Fenstertitel setzen
 		frame.setBounds(100, 100, 550, 605); 	// Fenstergroesse einstellen
 												// (x-Position, y-Position,
 												// Breite, Hoehe)
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 	// Programm beim
 																// Schliessen
 																// des Fensters
 																// beenden
 		frame.setResizable(false); // Fenster soll nicht skalierbar sein
 
 		JMenuBar menuBar = new JMenuBar(); 	// Menueleiste erstellen
 		frame.setJMenuBar(menuBar); 		// Menueleiste hinzufuegen
 
 		menuBar.isFocusable();
 
 		JMenu mnSpiel = new JMenu("Spiel"); // Menuepunkt "Spiel" erstellen
 		menuBar.add(mnSpiel); 				// Menuepunkt "Spiel" hinzufuegen
 
 		JMenuItem mntmNeu = new JMenuItem("Neu"); 	// Untermenuepunkt "Neu"
 													// erstellen
 		mnSpiel.add(mntmNeu); 						// Untermenuepunkt "Neu" hinzufuegen
 		mntmNeu.setAction(Action_Neu); 				// Aktion "Action_Neu" hinzufuegen
 
 		JMenuItem mntmBeenden = new JMenuItem("Beenden"); 	// Untermenuepunkt
 															// "Beenden"
 															// erstellen
 		mnSpiel.add(mntmBeenden); 							// Untermenuepunkt "Beenden" hinzufuegen
 		mntmBeenden.setAction(Action_Beenden); 				// Aktion "Action_Beenden"
 															// hinzufuegen
 		
 		JMenu mnModus = new JMenu("Modus"); // Menuepunkt "Modus" erstellen
 		menuBar.add(mnModus); 				// Menuepunkt "Modus" hinzufuegen
 		
 		JMenuItem mntmSingleplayer = new JMenuItem("Singleplayer"); 	// Untermenuepunkt "Singleplayer"
 																		// erstellen
 		mnModus.add(mntmSingleplayer); 									// Untermenuepunkt "Singleplayer" hinzufuegen
 		mntmSingleplayer.setAction(Action_Singleplayer); 						// Aktion "Action_Singleplayer" hinzufuegen
 		
 		JMenuItem mntmMultiplayer = new JMenuItem("Multiplayer"); 		// Untermenuepunkt "Multiplayer"
 																		// erstellen
 		mnModus.add(mntmMultiplayer); 									// Untermenuepunkt "Multiplayer" hinzufuegen
 		mntmMultiplayer.setAction(Action_Multiplayer); 							// Aktion "Action_Multiplayer" hinzufuegen		
 
 		frame.add(game); 	// Spielfeld hinzufuegen
 		game.init(); 		// Spielfeld zeichnen
 
 		game.addKeyListener(this); 	// Keylistener zum Spielfeld hinzufuegen
 		game.setFocusable(true); 	// Spielfeld fokussierbar machen
 		game.requestFocus(); 		// Fokus auf Spielfeld setzen
 	}
 
 	// keyPressed-Methode:
 	/**
 	 * Horcht, ob eine Taste gedrueckt wurde und wertet die Aktion gegebenfalls aus.
 	 * Gueltige Aktionen sind: Hoch-, Links-, Rechts-, Runtertaste (Bewegung) und Leertaste (Bombe)
 	 * 
 	 */
 	public void keyPressed(KeyEvent Key) {
 		
		// oben:
 		if (Key.getKeyCode() == KeyEvent.VK_UP) {
 			System.out.println("oben S1"); // Test
 			System.out.println();
 
 			a[0] = 0;
 			a[1] = -1;
 			a[2] = 1;
 
 		}
 
 		// Pfeiltaste links:
		if (Key.getKeyCode() == KeyEvent.VK_LEFT) {
 			System.out.println("links S1"); // Test
 			System.out.println();
 
 			a[0] = -1;
 			a[1] = 0;
 			a[2] = 1;
 
 		}
 
 		// Pfeiltaste rechts:
		if (Key.getKeyCode() == KeyEvent.VK_RIGHT) {
 			System.out.println("rechts S1"); // Test
 			System.out.println();
 
 			a[0] = 1;
 			a[1] = 0;
 			a[2] = 1;
 
 		}
 
 		// Pfeiltaste unten:
		if (Key.getKeyCode() == KeyEvent.VK_DOWN) {
 			System.out.println("unten S1"); // Test
 			System.out.println();
 
 			a[0] = 0;
 			a[1] = 1;
 			a[2] = 1;
 
 		}
 
 		// Leertaste:
		if (Key.getKeyCode() == KeyEvent.VK_SPACE) {
 			System.out.println("Bombe S1"); // Test
 			System.out.println();
 
 
 			game.bombe_legen(1);
 			game.removeAll(); 	// entferne alle bisherigen Komponenten vom Panel
 			game.refresh();		// zeichne alle Komponenten des Panels neu
 
 		}
 		
 		//Key-Methoden fuer 2. Spieler
		if (Key.getKeyCode() == KeyEvent.VK_W && twoPlayer){	//Taste oben
 			System.out.println("Oben S2");
 			System.out.println();
 			
 			a[0]=0;
 			a[1]=-1;
 			a[2]=2;
 			
 		}
 		
		if (Key.getKeyCode() == KeyEvent.VK_A && twoPlayer){	//Taste links
 			System.out.println("Links S2");
 			System.out.println();
 			
 			a[0]=-1;
 			a[1]=0;
 			a[2]=2;
 			
 		}
 		
		if (Key.getKeyCode() == KeyEvent.VK_S && twoPlayer){	//Taste unten
 			System.out.println("Unten S2");
 			System.out.println();
 			
 			a[0]=0;
 			a[1]=1;
 			a[2]=2;
 			
 		}
 		
		if (Key.getKeyCode() == KeyEvent.VK_D && twoPlayer ){	//Taste rechts
 			System.out.println("Rechts S2");
 			System.out.println();
 			
 			a[0]=1;
 			a[1]=0;
 			a[2]=2;
 			
 		}
 		
		if (Key.getKeyCode() == KeyEvent.VK_E && twoPlayer){
 			System.out.println("Bombe S2"); // Test
 			System.out.println();
 
 
 			game.bombe_legen(2);
 			game.removeAll(); 	// entferne alle bisherigen Komponenten vom Panel
 			game.refresh();		// zeichne alle Komponenten des Panels neu
 		}
 		
 		if (Key.getKeyCode() != KeyEvent.VK_SPACE && Key.getKeyCode() != KeyEvent.VK_E) {
 			// wenn Spieler 1 Bewegungen durchfuehrt
 			if (a[2] == 1){
 				// Bewegung Spieler 1
 				if (Map.map[hulk1.get_x() + a[0]][hulk1.get_y() + a[1]] == 2) { 	// falls
 																					// das
 																					// naechste
 																					// Feld
 																					// ein
 																					// Weg-Feld
 																					// ist
 
 					game.move_Hulk(a[0], a[1], a[2]); // bewege Hulk auf dem Spielfeld
 					game.removeAll(); 			// entferne alle bisherigen Komponenten vom
 												// Panel
 					game.refresh(); 			// zeichne alle Komponenten des Panels neu
 				}
 				
 				// Sieg Spieler 1
 				else if (Map.map[hulk1.get_x() + a[0]][hulk1.get_y() + a[1]] == 7) { 	// falls
 																						// das
 																						// naechste
 																						// Feld
 																						// das
 																						// Ziel-Feld
 																						// ist
 					System.out.println("Spieler 1 hat gewonnen"); // Test
 					System.out.println();
 
 					System.out.println("Spiel neugestartet"); // Test
 					System.out.println();
 		
 					// Hulk zurueckpositionieren:
 					reset_Hulk();
 				
 					// Gelegte Bomben entfernen:
 					for (int x=0; x<11; x++) {
 						for (int y=0; y<11; y++) {
 							game.bomb[x][y].liegt = false;
 						}
 						
 					}				
 				
 					// Spielfeld intern reinitialisieren:
 					Map.set_map(MapLoader.laden(1));					
 
 					// Spielfeld grafisch reinitialisieren:
 					game.removeAll();
 					game.refresh();
 				}
 				
 				// Niederlage Spieler 1				
 				else if (Map.map[hulk1.get_x() + a[0]][hulk1.get_y() + a[1]] == 6) { 	// falls
 																						// das
 																						// naechste
 																						// Feld
 																						// ein
 																						// Explosions-Feld
 																						// ist
 					System.out.println("Spieler 1 hat verloren"); // Test
 					System.out.println();
 					
 					System.out.println("Spiel neugestartet"); // Test
 					System.out.println();
 					
 					// Hulk zurueckpositionieren:
 					reset_Hulk();
 					
 					// Gelegte Bomben entfernen:
 					for (int x=0; x<11; x++) {
 						for (int y=0; y<11; y++) {
 							game.bomb[x][y].liegt = false;
 						}
 					}			
 					
 					// Spielfeld intern reinitialisieren:
 					Map.set_map(MapLoader.laden(1));
 					
 					// Spielfeld grafisch reinitialisieren:
 					game.removeAll();
 					game.refresh();
 				}
 			}
 				
 			// wenn Spieler 2 Bewegung durchfuehrt	
 			if (a[2] == 2) {
 				// Bewegung Spieler 2
 				if (Map.map[hulk2.get_x() + a[0]][hulk2.get_y() + a[1]] == 2) { 	// falls
 																					// das
 																					// naechste
 																					// Feld
 																					// ein
 																					// Weg-Feld
 																					// ist
 					
 					game.move_Hulk(a[0], a[1], a[2]); 	// bewege Hulk auf dem Spielfeld
 					game.removeAll(); 					// entferne alle bisherigen Komponenten vom
 														// Panel
 					game.refresh(); 					// zeichne alle Komponenten des Panels neu
 				}
 				
 				// Sieg Spieler 2
 				else if (Map.map[hulk2.get_x() + a[0]][hulk2.get_y() + a[1]] == 7) { 	// falls
 																						// das
 																						// naechste
 																						// Feld
 																						// das
 																						// Ziel-Feld
 																						// ist
 					System.out.println("Spieler 2 hat gewonnen"); // Test
 					System.out.println();
 					
 					System.out.println("Spiel neugestartet"); // Test
 					System.out.println();
 					
 					// Hulk zurueckpositionieren:
 					reset_Hulk();
 					
 					
 					// Gelegte Bomben entfernen:
 					for (int x=0; x<11; x++) {
 						for (int y=0; y<11; y++) {
 							game.bomb[x][y].liegt = false;
 						}
 					}				
 					
 					// Spielfeld intern reinitialisieren:
 					Map.set_map(MapLoader.laden(1));
 					
 					// Spielfeld grafisch reinitialisieren:
 					game.removeAll();
 					game.refresh();										
 				}
 			
 			
 				// Niederlage Spieler 2
 				else if (Map.map[hulk2.get_x() + a[0]][hulk2.get_y() + a[1]] == 6) { // falls
 																					// das
 																					// naechste
 																					// Feld
 																					// ein
 																					// Explosions-Feld
 																					// ist
 					System.out.println("Spieler 2 hat verloren"); // Test
 					System.out.println();
 	
 					System.out.println("Spiel neugestartet"); // Test
 					System.out.println();
 					
 					// Hulk zurueckpositionieren:
 					reset_Hulk();
 	
 					// Gelegte Bomben entfernen:
 					for (int x=0; x<11; x++) {
 						for (int y=0; y<11; y++) {
 							game.bomb[x][y].liegt = false;
 						}
 						
 					}			
 					
 					// Spielfeld intern reinitialisieren:
 					Map.set_map(MapLoader.laden(1));
 	
 					// Spielfeld grafisch reinitialisieren:
 					game.removeAll();
 					game.refresh();
 				}
 				
 			}
 			
 		}
 		
 	}	
 
 	public void keyTyped(KeyEvent Key) {
 	}
 
 	public void keyReleased(KeyEvent Key) {
 	}
 
 	/**
 	 * 
 	 * @return	Array mit neuer Spielfigurenposition
 	 */
 	public static int[] get_newPos() {
 		return a;
 	}
 	
 	static void reset_Hulk(){
 		hulk1.set_x(1);
 		hulk1.set_y(1);
 		
 		hulk2.set_x(9);
 		hulk2.set_y(9);
 	}
 	
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Beenden", beendet das Programm
 	 * @author Kolja Salewski
 	 *
 	 */
 	private class Action_Beenden extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		public Action_Beenden() {
 			putValue(NAME, "Beenden");
 			putValue(SHORT_DESCRIPTION, "Beenden des Spiels");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			System.exit(0);
 		}
 
 	}
 
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Neu", setzt das Spielfeld auf Anfang
 	 * @author Kolja Salewski
 	 *
 	 */
 	private class Action_Neu extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		public Action_Neu() {
 			putValue(NAME, "Neu");
 			putValue(SHORT_DESCRIPTION, "Neustart des Spiels");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			System.out.println("Spiel neugestartet"); // Test
 			System.out.println();
 
 			// Hulk zurueckpositionieren:
 			reset_Hulk();
 
 			// Gelegte Bomben entfernen:
 			for (int x=0; x<11; x++) {
 				for (int y=0; y<11; y++) {
 					game.bomb[x][y].liegt = false;
 				}
 			}		
 			
 			// Spielfeld intern reinitialisieren:
 			Map.set_map(MapLoader.laden(1));
 
 			// Spielfeld grafisch reinitialisieren:
 			game.removeAll();
 			game.refresh();
 		}
 
 	}
 	
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Singleplayer", wechselt zum Singleplayer-Modus
 	 * @author Kolja Salewski
 	 *
 	 */
 	private class Action_Singleplayer extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		public Action_Singleplayer() {
 			putValue(NAME, "Singleplayer");
 			putValue(SHORT_DESCRIPTION, "Wechsel in Singleplayer-Modus");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (twoPlayer == true) {
 				twoPlayer = false;
 				
 				System.out.println("Singleplayer-Modus aktiviert");	// Test
 				System.out.println();								// Test
 				
 				// Hulk zurueckpositionieren:
 				reset_Hulk();
 				
 				// Gelegte Bomben entfernen:
 				for (int x=0; x<11; x++) {
 					for (int y=0; y<11; y++) {
 						game.bomb[x][y].liegt = false;
 					}
 				}	
 				
 				map = MapLoader.laden(1); 
 				Map.set_map(map);
 				game.removeAll();
 				game.refresh(); 		// Spielfeld zeichnen
 			}
 			
 		}
 
 	}
 	
 	/**
 	 * Klasse fuer Menuebuttonorganisation "Multiplayer", wechselt zum Multiplayer-Modus
 	 * @author Kolja Salewski
 	 *
 	 */
 	private class Action_Multiplayer extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 
 		public Action_Multiplayer() {
 			putValue(NAME, "Multiplayer");
 			putValue(SHORT_DESCRIPTION, "Wechsel in Multiplayer-Modus");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (twoPlayer == false) {
 				twoPlayer = true;
 				
 				System.out.println("Multiplayer-Modus aktiviert");	// Test
 				System.out.println();								// Test
 				
 				// Hulk zurueckpositionieren:
 				reset_Hulk();
 				
 				// Gelegte Bomben entfernen:
 				for (int x=0; x<11; x++) {
 					for (int y=0; y<11; y++) {
 						game.bomb[x][y].liegt = false;
 					}
 				}	
 				
 				map = MapLoader.laden(1); 
 				Map.set_map(map);
 				game.removeAll();
 				game.refresh(); 		// Spielfeld zeichnen
 			}
 			
 		}
 
 	}
 
 	/**
 	 * Wird in der Map-Klasse verwendet
 	 * @return Spielfeldobjekt game
 	 */
 	public static Map get_game() {
 		return game;
 	}
 
 	/**
 	 * 
 	 * @param game Setzt das Spielfeldobjekt
 	 */
 	public void set_game(Map game) {
 		Menue.game = game;
 	}
 
 	/**
 	 * @param a Spielernummer (1,2, etc.)
 	 * @return gewuenschtes Spielerobjekt
 	 */
 	public static Hulk get_hulk(int a) {	
 		if (a == 1)	return hulk1;
 		if (a == 2) return hulk2;
 		else return null;
 	}
 
 	/**
 	 * 
 	 * @param hulk Aktualisiert die Position der Spielfigur in der Menue-Klasse
 	 */
 	public void set_hulk1(Hulk hulk) {
 		Menue.hulk1 = hulk;
 	}
 	
 	/**
 	 * 
 	 * @param hulk2	 Hulk-Objekt 2. Spieler
 	 */
 	public static void setHulk2(Hulk hulk2) {
 		Menue.hulk2 = hulk2;
 	}
 	
 	/**
 	 * 
 	 * @return Map-Array (Positionen der Icons im Spielfeld)
 	 */
 	public static int[][] get_map() {
 		return map;
 	}
 	
 	public static boolean getMultiplayer(){
 		return twoPlayer;
 	}
 
 	// main-Methode:
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Menue window = new Menue();
 					window.frame.setVisible(true);
 				}
 
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		});
 		
 	}
 
 }
