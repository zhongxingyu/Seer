 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.Enumeration;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
 // Autor T. K 
 // 
 public class MapEditor {
 	final static int n = MapLoader.get_n();
 	private static int[][] map = new int[n][n];
 	JFrame frame;
 	protected static boolean saved = false;
 	private static int power;
 	ImageIcon pic;
 	static int iconSatz;
 	private static boolean abfrageHulk1 = false;
 	private static boolean abfrageHulk2 = false;
 	private static boolean abfrageAusgang = false;
 	private static File f;
 	private static String eingabe;
 	private static boolean exist = false;
 	private static JButton feld[][] = new JButton[n][n];
 	private static int oldLevel = MapLoader.get_level();
 
 	/* Konstruktor: */
 	public MapEditor() {
 		edit();
 	}
 
 	/* METHODEN: */
 
 	// edit-Methode:
 	public int edit() {
 
 		int anzahlIcons = 9;
 		boolean richtigeAbfrage = false;
 
 		do {
 			eingabe = JOptionPane.showInputDialog(null,
 					"Bitte geben Sie die Level-Nummer ein:\nNicht 1 oder 2.",
 					"Levelnummer", JOptionPane.OK_CANCEL_OPTION);
 
 			if (eingabe == null) {
 				System.out.println("Boo");
 				richtigeAbfrage = true;
 				// Editor abbrechen!
 				return 0;
 			} else if (eingabe.equals("")) {
 				richtigeAbfrage = false;
 			} else {
 				MapLoader.set_level(Integer.parseInt(eingabe));
 				richtigeAbfrage = true;
 			}
 		} while (richtigeAbfrage != true);
 
 		// datei existiert?
 		String dateiName = "src/Maps/Level-";
 		dateiName = dateiName + eingabe + ".txt";
 
 		f = new File(dateiName);
 		if (f.exists() == true) {
 
 			MapLoader.set_iconSatz(MapLoader.get_iconSatzLevel(dateiName));
 			iconSatz = MapLoader.get_iconSatzLevel(dateiName);
 			MapLoader.set_iconSatz(iconSatz);
 			map = MapLoader.laden(MapLoader.get_level());
 			exist = true;
 		}
 
 		else {
 			int iconAbfrage = JOptionPane.showConfirmDialog(null,
 					"Mchten Sie den ersten Icon-Satz nutzen?", "Icon-Satz?",
 					JOptionPane.YES_NO_OPTION);
 			switch (iconAbfrage) {
 			case 0:
 				MapLoader.set_iconSatz(1);
 				iconSatz = 1;
 				break;
 			case 1:
 				MapLoader.set_iconSatz(2);
 				iconSatz = 2;
 				break;
 			}
 
 			for (int i = 0; i < n; i++) {// Map mit Mauern umranden!
 
 				map[i][0] = 4;
 				map[0][i] = 4;
 				map[n - 1][i] = 4;
 				map[i][n - 1] = 4;
 
 			}
 			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {// Rest mit weg fllen	
 					if (map[i][j] != 4)
 						map[i][j] = 2;
 
 				}
 			}
 
 			MapLoader.level_speichern(map, "Level-" + eingabe);
 		}
 		frame = new JFrame();
 		frame.setBounds(100, 100, 450, 300);
 		frame.setTitle("Map-Editor"); // Fenstertitel setzen
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.getContentPane().setLayout(new BorderLayout(0, 0));
 
 		final String levelnummer = "Level-" + eingabe;
 		JLabel levelname = new JLabel(levelnummer, JLabel.CENTER);
 
 		frame.getContentPane().add(levelname, BorderLayout.BEFORE_FIRST_LINE);
 		JButton save_button = new JButton("Level speichern");
 		ActionListener save = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (new File("/Maps/Level-" + eingabe + ".txt").exists()) {
 					int dateiAbfrage = JOptionPane.showConfirmDialog(null,
 							"Die Datei existiert bereits.\n"
 									+ "Mchten Sie die Datei berschreiben?",
 							"berschreiben?", JOptionPane.YES_NO_OPTION);
 
 					if (dateiAbfrage == 0) {
 
 					}
 
 					else {
 						String ersatzNummer = JOptionPane.showInputDialog(null,
 								"Geben Sie die einen ersatznamen ein!",
 								"ersatz", JOptionPane.PLAIN_MESSAGE);
 
 						MapLoader.level_speichern(map, "Level-" + ersatzNummer);
 						saved = true;
 					}
 
 				}
 
 				else {
 					MapLoader.level_speichern(map, levelnummer);
 					saved = true;
 				}
 
 				Menue.spiel_neustarten();
 			}
 
 		};
 		frame.setVisible(true);
 		JPanel place = new JPanel();
 		frame.setResizable(false); // Fenster soll nicht skalierbar sein
 
 		JButton exit_button = new JButton("Editor Beenden");
 		ActionListener exit = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				if (saved) {
 					frame.setVisible(false);
 					MapLoader.set_level(oldLevel);
 				}
 
 				else {
 					int eingabe = JOptionPane.showConfirmDialog(null,
 							"Wollen Sie die neue Map speichern?",
 							"Gespeichert?", JOptionPane.YES_NO_CANCEL_OPTION);
 
 					switch (eingabe) {
 					case 0:
 						MapLoader.level_speichern(map, levelnummer);
 						frame.setVisible(false);
 						MapLoader.set_level(oldLevel);
 						break;
 					case 1:
 						frame.setVisible(false);
 						f.delete();//  funktioniert noch nicht
 						MapLoader.set_level(oldLevel);
 						break;
 					case 2: // abbrechen nichts machen
 						break;
 					}
 				}
 			}
 		};
 
 		JButton test_button = new JButton("Testen");
 		ActionListener test = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				// map editor soll das aktuelle spiel in hauptfenster ffnen und
 				// spielen
 
 			}
 
 		};
 
 		JButton freigabe_button = new JButton("Level freigeben");
 		ActionListener freigabe = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				// level implentieren wenn es luft!
 
 			}
 
 		};
 
 		exit_button.addActionListener(exit);
 		save_button.addActionListener(save);
 		test_button.addActionListener(test);
 		test_button.addActionListener(freigabe);
 		place.add(exit_button);
 		place.add(save_button);
 		place.add(test_button);
 		place.add(freigabe_button);
 
 		frame.getContentPane().add(place, BorderLayout.SOUTH);
 		// Icons
 		JRadioButton icon[] = new JRadioButton[anzahlIcons];
 		final ButtonGroup buttonGroup = new ButtonGroup();
 		icon[0] = new JRadioButton("Hulk", true);
 		icon[1] = new JRadioButton("Weg");
 		icon[2] = new JRadioButton("Block");
 		icon[3] = new JRadioButton("Mauer");
 		icon[4] = new JRadioButton("Ausgang");
 		icon[5] = new JRadioButton("Block-Ausgang");
 		icon[6] = new JRadioButton("Block/Flammen-Item");
 		icon[7] = new JRadioButton("Block/Bomben-Item");
 		icon[8] = new JRadioButton("2.Spieler");
 
 		// eventuell noch mehrere
 		JPanel radioPanel = new JPanel(new GridLayout(anzahlIcons, 1));
 		for (int k = 0; k < anzahlIcons; k++) {
 			buttonGroup.add(icon[k]);
 			radioPanel.add(icon[k]);
 		}
 
 		frame.getContentPane().add(radioPanel, BorderLayout.LINE_START);
 		frame.pack();
 		JPanel buttonPanel = new JPanel(new GridLayout(n, n));
 
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < n; j++) {
 				final int a = i;
 				final int b = j;
 				ActionListener list = new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 
						if (true) {
 							String name = getSelectedButton(buttonGroup)
 									.getText();
 							if (name != null) {
 								if (name == "Hulk") {
 									if (!abfrageHulk1) {
 										power = 1;
 										pic = new ImageIcon(
 												Map.class.getResource("/Pics/"
 														+ iconSatz
 														+ "/Hulk.png"));
 										abfrageHulk1 = true;
 									} else {
 										System.out
 												.println("Hulk1 wurde schon gesetzt");
 										//									int neu = JOptionPane.showConfirmDialog(null,
 										//											"Neue Position?", "Bereits gesetz",
 										//											JOptionPane.YES_NO_OPTION);
 										power = 2;
 										pic = new ImageIcon(
 												Map.class
 														.getResource("/Pics/"
 																+ iconSatz
 																+ "/Weg.png"));
 									}
 								} else if (name == "Weg") {
 									power = 2;
 									pic = new ImageIcon(
 											Map.class.getResource("/Pics/"
 													+ iconSatz + "/Weg.png"));
 								} else if (name == "Block") {
 									power = 4;
 									pic = new ImageIcon(
 											Map.class.getResource("/Pics/"
 													+ iconSatz + "/Block.png"));
 								} else if (name == "Mauer") {
 									power = 3;
 									pic = new ImageIcon(
 											Map.class.getResource("/Pics/"
 													+ iconSatz + "/Mauer.png"));
 								} else if (name == "Ausgang") {
 									if (!abfrageAusgang) {
 										power = 7;
 										pic = new ImageIcon(
 												Map.class.getResource("/Pics/"
 														+ iconSatz
 														+ "/Exit.png"));
 										abfrageAusgang = true;
 									} else {
 
 										System.out
 												.println("Ausgang wurde schon gesetzt");
 										power = 2;
 										pic = new ImageIcon(
 												Map.class
 														.getResource("/Pics/"
 																+ iconSatz
 																+ "/Weg.png"));
 									}
 								} else if (name == "Block-Ausgang") {
 									if (!abfrageAusgang) {
 										power = 8;
 										pic = new ImageIcon(
 												Map.class.getResource("/Pics/"
 														+ iconSatz
 														+ "/Exit.png"));
 									} else {
 
 										System.out
 												.println("Ausgang wurde schon gesetzt");
 									}
 								} else if (name == "Block/Flammen-Item") {
 									power = 9;
 									pic = new ImageIcon(
 											Map.class
 													.getResource("/Pics/Flammen-Item.png"));
 								} else if (name == "Block/Bomben-Item") {
 									power = 12;
 									pic = new ImageIcon(
 											Map.class
 													.getResource("/Pics/Bomben-Item.png"));
 								} else if (name == "2.Spieler") {
 									if (!abfrageHulk2) {
 										power = 10;
 
 										pic = new ImageIcon(
 												Map.class.getResource("/Pics/"
 														+ iconSatz
 														+ "/Hulk2.png"));
 										abfrageHulk2 = true;
 									} else {
 
 										System.out
 												.println("Hulk2 wurde schon gesetzt");
 										power = 10;
 										pic = new ImageIcon(
 												Map.class
 														.getResource("/Pics/"
 																+ iconSatz
 																+ "/Weg.png"));
 									}
 								}
 
 								map[a][b] = power;
 
 								feld[a][b].setIcon(pic); // ...Grafik auf Button
 
 								Menue.spiel_neustarten();
 							}
 						}
 					}
 				};
 
 				feld[i][j] = new JButton();
 				if (exist) {
 					pic = getPic(map[i][j]);
 					feld[i][j].setIcon(pic);
 				}
 
 				feld[i][j].setPreferredSize(new Dimension(50, 50));// Button-Gre
 				feld[i][j].addActionListener(list);
 				buttonPanel.add(feld[i][j]);
 
 			}
 			frame.getContentPane().add(buttonPanel);
 		}
 
 		frame.pack();
 
 		return 1;
 	}
 
 	/* setter & getter: */
 
 	// getSelectedButton-Methode:
 	public static JRadioButton getSelectedButton(ButtonGroup group) {
 		Enumeration<AbstractButton> e = group.getElements();
 		while (e.hasMoreElements()) {
 			AbstractButton b = e.nextElement();
 			if (b.isSelected())
 				return (JRadioButton) b;
 		}
 		return null;
 	}
 
 	public static ImageIcon getPic(int i) {
 		ImageIcon pic = null;
 		switch (i) {
 
 		case 1: // Hulk
 			pic = new ImageIcon(Map.class.getResource("/Pics/" + iconSatz
 					+ "/Hulk.png"));
 			break;
 
 		case 2: // Weg
 			pic = new ImageIcon(Map.class.getResource("/Pics/" + iconSatz
 					+ "/Weg.png"));
 			break;
 
 		case 4: // Block
 			pic = new ImageIcon(Map.class.getResource("/Pics/" + iconSatz
 					+ "/Mauer.png"));
 			break;
 
 		case 3: // Mauer (nicht zerstoerbar)
 			pic = new ImageIcon(Map.class.getResource("/Pics/" + iconSatz
 					+ "/Block.png"));
 			break;
 
 		case 7: // Ausgang
 			pic = new ImageIcon(Map.class.getResource("/Pics/" + iconSatz
 					+ "/Exit.png"));
 			break;
 
 		case 8: // Block/Ausgang	
 			pic = new ImageIcon(Map.class.getResource("/Pics/" + iconSatz
 					+ "/Exit.png"));
 			break;
 
 		case 9: // Block/Flammen-Item 	
 			pic = new ImageIcon(Map.class.getResource("/Pics/Flammen-Item.png"));
 			break;
 
 		case 10: // 2. Spieler
 
 			pic = new ImageIcon(Map.class.getResource("/Pics/" + iconSatz
 					+ "/Hulk2.png"));
 
 			break;
 
 		case 12:
 			// Bombe
 
 			pic = new ImageIcon(Map.class.getResource("/Pics/" + iconSatz
 					+ "/Hulk2.png"));
 
 		}
 		return pic;
 	}
 
 }
