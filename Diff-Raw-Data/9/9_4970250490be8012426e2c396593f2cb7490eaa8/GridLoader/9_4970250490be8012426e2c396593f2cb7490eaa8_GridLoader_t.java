 package propra2012.gruppe33.bomberman.graphics.rendering.scenegraph.grid;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Diese Klasse laedt aus einer Textdatei Zeilenweise die Karte aus. Kann
  * IOExceptions werfen.
  * 
  * @author Malte Schmidt
  */
 public final class GridLoader {
 
 	/**
 	 * Berechnet die Map aus der Textdatei und gibt ein Array mit dem Inhalt der
 	 * Map zurueck. Wirft IOException, wenn die Karte nicht geladen werden kann,
 	 * z.B. wenn die File nicht geladen werden kann.
 	 * 
 	 * @param input
 	 *            Die zu ladene Map.
 	 * @return Gibt ein Char-array zurueck mit den geladenen Werten der Map.
 	 * @throws IOException
 	 *             Wenn zu ladene Karte leer oder nicht kompatibel (z.B. Wenn
 	 *             die Strings unterschiedlich lang sind.
 	 */
 	public static char[][] load(InputStream input) throws IOException {
 		if (input == null) {
 			throw new NullPointerException("input");
 		}
 
 		// Eine Zeile der Karte.
 		String line;
 
 		// Die map
 		char[][] map;
 
 		// Liste in der Zwischengespeichert wird
 		List<String> cache = new LinkedList<String>();
 
 		// Laden des files
 		BufferedReader br = new BufferedReader(new InputStreamReader(input));
 
 		try {
 			// Lies Zeile fuer Zeile
 			while ((line = br.readLine()) != null) {
 				cache.add(line);
 			}
 
 			// Nichts zu lesen
 			if (cache.isEmpty()) {
 				throw new IOException("Empty grid");
 			}
 		} finally {
 			// Schliesse den Stream
 			br.close();
 		}
 
 		// Neues array erstellen
 		map = new char[cache.size()][];
 
 		// Alles in das char array kopieren
 		int i = 0, len = -1;
 		for (String row : cache) {
 			if (len == -1) {
 				len = row.length();
 			} else if (len != row.length()) {
 				throw new IOException("Zeilen nicht alle gleich lang!");
 			}
 
 			map[i++] = row.toCharArray();
 		}
 		return map;
 	}
 
 	// Muss nicht erstellbar sein.
 	private GridLoader() {
 	}
 
 	public static char[][] generate(char[][] map, long seed) {
 		// int blockcount = Math.round((map.length * map[0].length) * 0.8f);
 		Random ran = new Random(seed);
		for (int y = 1; y < map.length - 1; y++) {
			for (int x = 1; x < map[0].length - 1; x++) {
				if (!nextTo(map, x, y, 's') && map[y][x] != '1') {
 					if (ran.nextInt(10 - nextToCount(map, x, y)) > 2) {
						(int)map[y][x] += 1000;
 					}
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Get's one field of an array and controls whether their is a field of the
 	 * expected type next to it.
 	 * 
 	 * @param map
 	 *            the array
 	 * @param x
 	 *            x coordinate of the field
 	 * @param y
 	 *            y coordinate of the field
 	 * @param typ
 	 *            the expected type
 	 * @return true, for their is a block next to it and false, for their is no
 	 *         block of teh expected typ
 	 */
 	private static boolean nextTo(char[][] map, int x, int y, char typ) {
 		if (map[y - 1][x] == typ) {
 			return true;
 		} else if (map[y][x + 1] == typ) {
 			return true;
 		} else if (map[y + 1][x] == typ) {
 			return true;
 		} else if (map[y][x - 1] == typ) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Get's one field of an array and counts the number of destructible blocks
 	 * around it.
 	 * 
 	 * @param map
 	 *            the array
 	 * @param x
 	 *            x coordinate of the field
 	 * @param y
 	 *            y coordinate of the field
 	 * @return the number of destructible blocks around the field.
 	 */
 	private static int nextToCount(char[][] map, int x, int y) {
 		int count = 0;
 		if (map[y - 1][x] >= (char) 1000) {
 			count++;
 		}
 		if (map[y][x + 1] >= (char) 1000) {
 			count++;
 		}
 		if (map[y + 1][x] >= (char) 1000) {
 			count++;
 		}
 		if (map[y][x - 1] >= (char) 1000) {
 			count++;
 		}
 		return count;
 	}
 }
