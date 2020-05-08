 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 
 /**
  * 
  * @author Tobias Korfmacher
  *
  */
 
 public class Map extends JPanel {
 	// Deklaration & Initialisierung:
 	private static final long serialVersionUID = 1L;
 	int n = MapLoader.get_n(); 					// Spielfeldgroesse
 	static int[][] map; 						// Spielfeld
 	Bombe bomb[][] = new Bombe[n][n]; 			// Bomben erstellen
 	int bomb_x, bomb_y;
 	JLabel[][] label = new JLabel[n][n]; 		// JLabel-Array erstellen
 	public static boolean twoPlayerset;		//Multiplayerabfrage
 
 	// Konstruktor:
 	public Map(int[][] map) {
 		Map.map = map;
 		twoPlayerset = Menue.getMultiplayer();
 		for (int x = 0; x < n; x++) {
 			for (int y = 0; y < n; y++) {
 				bomb[x][y] = new Bombe();
 			}
 			
 		}
 		
 	}
 
 	// move_Hulk-Methode:
 	/**
 	 * Setzt die Icons "Hulk" und "Weg", die bei einer Bewegung verändert werden
 	 * 
 	 * @param x		X-Koordinate im Map-Array, um welche die Spielfigur bewegt werden soll
 	 * @param y		Y-Koordinate im Map-Array, um welche die Spielfigur bewegt werden soll
 	 * @param z		zu bewegende Spielfigur (1 := Spieler 1, 2:= Spieler 2 ...)
 	 */
 	public void move_Hulk(int x, int y, int z) {
 		/* Grafische Fortbewegung der Spielfigur: */
 		if (map[Menue.get_hulk(z).get_x()][Menue.get_hulk(z).get_y()] == 1
 			|| map[Menue.get_hulk(z).get_x()][Menue.get_hulk(z).get_y()] == 10) { 	// falls das Feld das Hulk-Icon (1 oder 10) beinhaltet,... 
 			map[Menue.get_hulk(z).get_x()][Menue.get_hulk(z).get_y()] = 2; 			// ...weise dem Feld das Weg-Icon (2) zu
 		}
 		
 		if (map[Menue.get_hulk(z).get_x() + x][Menue.get_hulk(z).get_y() + y] == 12		// falls das nchste Feld das Bomben-Item-Icon enthlt...
 			&& Menue.get_hulk(z).get_max_bomben() < 8) {								// ...und der Spieler noch nicht bei max. 8 Bomben angelangt ist...
 			Menue.get_hulk(z).set_max_bomben(Menue.get_hulk(z).get_max_bomben() + 1);	// ...dann erhoehe die maximale Anzahl an Bomben um 1
 		}
 		
 		if (map[Menue.get_hulk(z).get_x() + x][Menue.get_hulk(z).get_y() + y] == 15) {		// falls das nchste Feld das Bomben-Item-Icon enthlt...
 			Menue.get_hulk(z).set_bomben_radius(Menue.get_hulk(z).get_bomben_radius()+1);	// ...dann erhoehe die maximale Anzahl an Bomben um 1
 		}
 		
 		if (z == 1) {
 			map[Menue.get_hulk(z).get_x() + x][Menue.get_hulk(z).get_y() + y] = 1; 	// weise dem naechsten Feld das Hulk-Icon zu
 		}
 		
 		else if (z == 2) {
 			map[Menue.get_hulk(z).get_x() + x][Menue.get_hulk(z).get_y() + y] = 10; // weise dem naechsten Feld das Hulk-Icon zu
 		}
 		
 		/* Logische Fortbewegung der Spielfigur: */
 		Menue.get_hulk(z).set_x(Menue.get_hulk(z).get_x() + x); 					// setze horizontale Hulk-Position weiter
 		Menue.get_hulk(z).set_y(Menue.get_hulk(z).get_y() + y); 					// setze vertikale Hulk-Position weiter
 
 		System.out.println("Neue Position des " + z + "ten Spielers: " + Menue.get_hulk(z).get_x()	// Test
 							+ "te Spalte, " + Menue.get_hulk(z).get_y() + "te Zeile"); 				// Test
 		System.out.println();																		// Test		
 	}
 
 	// bombe_legen-Methode:
 	public void bombe_legen(int Spieler) {
 		bomb_x = Menue.get_hulk(Spieler).get_x();
 		bomb_y = Menue.get_hulk(Spieler).get_y();
 		bomb[bomb_x][bomb_y] = new Bombe(bomb_x, bomb_y);
 
 		add(bomb[bomb_x][bomb_y]); 					// Bombe hinzufuegen
 		bomb[bomb_x][bomb_y].aktivieren(Spieler, Menue.get_hulk(Spieler).get_bomben_radius()); 	// Bombe aktivieren
 		map[bomb_x][bomb_y] = 5; 					// Bombe darstellen
 	}
 
 	// refresh-Methode:
 	public void refresh() {
 		GridBagConstraints[][] gbc_label = new GridBagConstraints[n][n];
 		System.out.println("Spielfeld:"); 	// Test
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < n; j++) {
 				label[i][j] = new JLabel(); // fuer jedes einzele Arrayelement wird ein neues Label erstellt
 				label[i][j].setHorizontalAlignment(SwingConstants.CENTER);
 
 				System.out.print(map[i][j] + ", "); // Test
 				if (j == n - 1) {
 					System.out.println();
 				}
 
 				label_laden(label[i][j], map[i][j]); 				// laedt in das Label das Bild (Map hat die Werte ob Hulk, etc..)
 				gbc_label[i][j] = new GridBagConstraints();
 				gbc_label[i][j].anchor = GridBagConstraints.CENTER;
 				gbc_label[i][j].fill = GridBagConstraints.NONE;
 				gbc_label[i][j].gridwidth = 1;
 				gbc_label[i][j].gridheight = 1;
 				gbc_label[i][j].insets = new Insets(0, 0, 0, 0);
 				gbc_label[i][j].gridx = i;
 				gbc_label[i][j].gridy = j;
 				add(label[i][j], gbc_label[i][j]); 					// Label auf Panel laden
 				label[i][j].repaint(); 								// Zeichnen
 				label[i][j].setVisible(true); 						// Sichtbarkeit setzen
 			}
 
 		}
 
 		System.out.println();
 
 		updateUI();
 	}
 
 	// init-Methode:
 	public void init() {
		setBounds(100, 100, 650, 680); // Panel erstellen
 
 		setBorder(new EmptyBorder(3, 3, 3, 3));
 		GridBagLayout gbl_contentPane = new GridBagLayout();
 		gbl_contentPane.columnWidths = new int[] { 1, 1, 1, 1, 0 };
 		gbl_contentPane.rowHeights = new int[] { 1, 0, 0 };
 		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0,
 				Double.MIN_VALUE };
 		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
 		setLayout(gbl_contentPane);
 
 		GridBagConstraints[][] gbc_label = new GridBagConstraints[n][n];
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < n; j++) {
 				label[i][j] = new JLabel(); 			// fuer jedes einzele Arrayelement wird ein neues Label erstellt
 				label[i][j].setHorizontalAlignment(SwingConstants.CENTER);
 				label_laden(label[i][j], map[i][j]); 	// laedt in das Label das Bild (Map hat die Werte ob Hulk, etc..)
 				gbc_label[i][j] = new GridBagConstraints();
 				gbc_label[i][j].anchor = GridBagConstraints.CENTER;
 				gbc_label[i][j].fill = GridBagConstraints.NONE;
 				gbc_label[i][j].gridwidth = 1;
 				gbc_label[i][j].gridheight = 1;
 				gbc_label[i][j].insets = new Insets(0, 0, 0, 0);
 				gbc_label[i][j].gridx = i;
 				gbc_label[i][j].gridy = j;
 				add(label[i][j], gbc_label[i][j]); 		// Label auf Panel laden
 			}
 
 		}
 
 	}
 
 	// label_laden-Methode:
 	public void label_laden(JLabel label, int i) {
 
 		switch (i) {
 		case 0: // Block/Bomben-Item
 			label.setIcon(new ImageIcon(Map.class
 					.getResource("/Pics/Block.png")));
 			break;
 		
 		case 1: // Hulk
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/Hulk.png"))); 	// Grafik-Quelle:
 																					// http://openclipart.org/detail/168616/hunk-by-bedpanner
 			break;
 
 		case 2: // Weg
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/Weg.png")));
 			break;
 
 		case 3: // Block
 			label.setIcon(new ImageIcon(Map.class
 					.getResource("/Pics/Block.png"))); 	// Grafik-Quelle:
 														// http://openclipart.org/detail/151531/char-somua-s35-by-spadassin-151531
 			break;
 
 		case 4: // Mauer (nicht zerstoerbar, eventuell ueber Hulkmodus)
 			label.setIcon(new ImageIcon(Map.class
 					.getResource("/Pics/Mauer.png"))); 	// Grafik-Quelle:
 														// http://openclipart.org/detail/3318/house-by-barretr
 			break;
 
 		case 5: // Bombe
 			label.setIcon(new ImageIcon(Map.class
 					.getResource("/Pics/Bombe.png")));	// Grafik-Quelle:
 														// http://openclipart.org/detail/165734/dynamite-by-magnesus
 			break;
 
 		case 6: // Explosion
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/EXP.png"))); 	// Grafik-Quelle:
 																					// http://openclipart.org/detail/122959/pow-by-viscious-speed
 			break;
 
 		case 7: // Ausgang
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/Exit.png")));	// Grafik-Quelle:
 																					// http://openclipart.org/detail/126307/panneau-sortie--traffic-sign-exit-by-lmproulx
 			break;
 
 		case 8: // Block/Ausgang	
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/Block.png")));
 			break;
 
 		case 9: // Block/Flammen-Item 	
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/Block.png")));
 			break;
 			
 		case 10: // 2. Spieler
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/Hulk2.png"))); // selbst gemalt :D
 			break;
 			
 		case 11: // Explosion/Bomben-Item 	
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/EXP.png")));
 			break;
 			
 		case 12: // Bomben-Item
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/Bomben-Item.png")));
 			break;
 			
 		case 13: // Explosion/Ausgang 	
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/EXP.png")));
 			break;
 		
 		case 14: // Explosion/Flammen-Item 	
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/EXP.png")));
 			break;
 			
 		case 15: // Flammen-Item 	
 			label.setIcon(new ImageIcon(Map.class.getResource("/Pics/Flammen-Item.png")));
 			break;
 		}
 		
 	}
 
 	// get_map-Methode:
 	public static int[][] get_map() {
 		return map;
 	}
 
 	// set_map-Methode:
 	public static void set_map(int[][] map) {
 		Map.map = map;
 	}
 
 }
