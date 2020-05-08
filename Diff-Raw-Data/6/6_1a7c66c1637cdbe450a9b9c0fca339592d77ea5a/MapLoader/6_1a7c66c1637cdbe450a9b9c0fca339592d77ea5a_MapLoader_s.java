 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileFilter;
 
 /**
  * Liest die Level aus der Datei aus und verpackt sie in ein int- Array
  * 
  * @author Tobias Korfmacher
  */
 public class MapLoader {
 	public static boolean twoPlayerSet, botSet;
 	static int n = 13;
 	static int level = 1;
 	static int iconSatz = 1;
 
 	/* METHODEN: */
 
 	// laden-Methode:
 	/**
 	 * @param i
 	 * legt das zu ladene Level fest
 	 * @return map - Objekt
 	 */
 	public static int[][] laden(int i) { // mit parameter i als level nummer
 		twoPlayerSet = Menue.getMultiplayer();
 		botSet = Menue.getBot();
 
 		if (!Menue.theme) {
 			Menue.sound.playTheme();
 			Menue.theme = true;
 		}
 
 		int c = 0;
 
 		int k = 0, l = 0;
 		int[][] map = new int[n][n];
 		String filename = "src/Maps/Level-" + i + ".txt";
 
 		try {
 			FileReader f = new FileReader(filename);
 
 			iconSatz = Character.getNumericValue(f.read());
 
 			while ((c = f.read()) != -1) {
 				if (Character.getNumericValue(c) != -1) {
 					if (Character.getNumericValue(c) != 58) {
 						map[k][l] = Character.getNumericValue(c);
 					} else {
 						f.read();
 					}
 
 					if (k < n - 1) {
 						k++;
 					}
 
 					else if (l < n - 1) {
 						k = 0;
 						l++;
 					}
 
 				}
 
 			}
 
 			if (twoPlayerSet) {
				map[n - 2][n - 2] = 10; // hulk2 aus map auslesen lassen
 				map[1][n - 2] = 2;
 			}
 
 			if (botSet) {
 				map[n - 2][n - 2] = 10;
 				map[1][n - 2] = 2;
 			}
 
 			Menue.set_mapLoaded(true);
 			f.close();
 		}
 
 		catch (IOException e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		return map;
 	}
 
 	// level_speichern-Methode 1:
 	public static void level_speichern(int[][] map, String levelname) {
 
 		String[] line = new String[n];
 		String path = "src/Maps/" + levelname + ".txt";
 		for (int i = 0; i < n; i++) {
 			line[i] = "";
 		}
 
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < n; j++) {
 
 				line[i] += map[i][j];
 				if (j < n - 1) {
 					line[i] += ":";
 				}
 			}
 		}
 
 		BufferedWriter out = null;
 		try {
 			out = new BufferedWriter(new FileWriter(path));
 			out.write("" + iconSatz);
 			out.newLine();
 			for (int i = 0; i < n; i++) {
 				out.write(line[i]);
 				out.newLine();
 			}
 
 		}
 
 		catch (IOException e) {
 			System.out.println(e);
 		}
 
 		finally {
 			try {
 				if (out != null)
 					out.close();
 			}
 
 			catch (Exception ex) {
 			}
 
 		}
 
 	}
 
 	// level_speichern-Methode 2:
 	public static void level_speichern(int[][] map) {// �ber filechooser
 
 		String[] line = new String[n];
 		// JFileChooser-Objekt erstellen
 		JFileChooser chooser = new JFileChooser(new File("./src/Maps"));
 		// Dialog zum Speichern von Dateien anzeigen
 		int state = chooser.showSaveDialog(null);
 		if (state == JFileChooser.APPROVE_OPTION) {
 
 			String path = chooser.getSelectedFile().getAbsolutePath();
 			path += ".txt";
 			for (int i = 0; i < n; i++) {
 				line[i] = "";
 			}
 
 			for (int i = 0; i < n; i++) {
 				for (int j = 0; j < n; j++) {
 
 					line[i] += map[i][j];
 					if (j < n - 1) {
 						line[i] += ":";
 					}
 					System.out.println(line[i]);
 				}
 
 			}
 
 			BufferedWriter out = null;
 			try {
 				out = new BufferedWriter(new FileWriter(path));
 				out.write("" + iconSatz);
 				out.newLine();
 				for (int i = 0; i < n; i++) {
 					out.write(line[i]);
 					out.newLine();
 				}
 
 			}
 
 			catch (IOException e) {
 				System.out.println(e);
 			}
 
 			finally {
 				try {
 					if (out != null)
 						out.close();
 				}
 
 				catch (Exception ex) {
 				}
 
 			}
 		} else
 
 			System.out.println("Auswahl abgebrochen");
 	}
 
 	// level_laden-Methode:
 	public static int[][] level_laden() {
 		twoPlayerSet = Menue.getMultiplayer();
 		botSet = Menue.getBot();
 
 		JFileChooser fc = new JFileChooser();
 		fc.setMultiSelectionEnabled(false);
 		fc.setCurrentDirectory(new File("./src/Maps"));
 		fc.setFileFilter(new FileFilter() {
 
 			public boolean accept(File f) {
 				return f.isDirectory()
 						|| f.getName().toLowerCase().endsWith(".txt");
 			}
 
 			public String getDescription() {
 				return "BomberHulk - Maps(*.txt)";
 			}
 		});
 		int state = fc.showOpenDialog(null);
 		if (state == JFileChooser.APPROVE_OPTION) {
 
 			File file = fc.getSelectedFile();
 			String levelname = file.getName();
 			MapLoader.set_level(get_level_nummer(levelname));
 			int c = 0;
 
 			int k = 0, l = 0;
 			int[][] map = new int[n][n];
 
 			try {
 				FileReader f = new FileReader(file);
 
 				System.out.println("Spielfeld eingelesen:"); // Test
 				iconSatz = Character.getNumericValue(f.read());
 				set_iconSatz(iconSatz);
 				System.out.println("IconSatz: " + iconSatz);
 
 				while ((c = f.read()) != -1) {
 					if (Character.getNumericValue(c) != -1) {
 						if (Character.getNumericValue(c) != 58) {
 							map[k][l] = Character.getNumericValue(c);
 						}
 
 						else {
 							f.read();
 						}
 
 						System.out.print(map[k][l] + ", "); // Test
 
 						if (k < n - 1) {
 							k++;
 						}
 
 						else if (l < n - 1) {
 							System.out.println(); // Test
 							k = 0;
 							l++;
 						}
 
 					}
 
 				}
 
 				System.out.println(); // Test
 
 				if (twoPlayerSet) {
 					map[n - 2][n - 2] = 10;
 					map[1][n - 2] = 2;
 				}
 
 				if (botSet) {
 					map[n - 2][n - 2] = 10;
 					map[1][n - 2] = 2;
 				}
 
 				System.out.println(); // Test
 
 				f.close();
 			}
 
 			catch (IOException e) {
 				System.err.println(e.getMessage());
 				e.printStackTrace();
 				System.exit(1);
 			}
 
 			//			Menue.set_mapLoaded(true);
 			return map;
 
 		}
 
 		else
 			System.out.println("Auswahl abgebrochen");
 
 		return null;
 	}
 
 	/* setter & getter: */
 
 	// get_n-Methode:
 	public static int get_n() {
 		return n;
 	}
 
 	// get_level-Methode:
 	public static int get_level() {
 		return level;
 	}
 
 	// get_iconSatz-Methode:
 	public static int get_iconSatz() {
 		return iconSatz;
 	}
 
 	public static int get_iconSatzLevel(String filename) {
 		int satz = 1;
 		try {
 			FileReader f = new FileReader(filename);
 
 			System.out.println("Spielfeld eingelesen:"); // Test
 
 			satz = Character.getNumericValue(f.read());
 			f.close();
 		}
 
 		catch (IOException e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 			System.exit(1);
 		}
 		return satz;
 	}
 
 	// set_iconSatz-Methode:
 	public static void set_iconSatz(int iconSatz) {
 		MapLoader.iconSatz = iconSatz; // Icon-Satz speichern
 	}
 
 	// get_level_nummer-Methode:
 	public static int get_level_nummer(String levelname) {
 		levelname = levelname.replace("Level-", "");
 		levelname = levelname.replace(".txt", "");
 		return Integer.parseInt(levelname);
 	}
 
 	// set_level-Methode:
 	public static void set_level(int level) {
 		MapLoader.level = level; // level speichern
 	}
 
 	public static int get_icon_x(int[][] map, int i) {
 		int found = 0;
 		for (int a = 0; a < MapLoader.get_n(); a++) {
 			for (int b = 0; b < MapLoader.get_n(); b++) {
 				if (map[a][b] == i)
 					found = a;
 			}
 		}
 
 		return found;
 
 	}
 
 	public static int get_icon_y(int[][] map, int i) {
 		int found = 0;
 		for (int a = 0; a < MapLoader.get_n(); a++) {
 			for (int b = 0; b < MapLoader.get_n(); b++) {
 				if (map[a][b] == i)
 					found = b;
 			}
 		}
 
 		return found;
 	}
 
 }
