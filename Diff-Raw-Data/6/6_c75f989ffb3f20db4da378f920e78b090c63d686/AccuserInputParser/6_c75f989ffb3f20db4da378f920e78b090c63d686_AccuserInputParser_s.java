 package brown.puzzles.liarliar;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import brown.puzzles.InputParser;
 
 /**
  * @author Matt Brown
  * @date Feb 18, 2010
  */
 public class AccuserInputParser implements InputParser<Collection<Accuser>> {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see brown.puzzles.InputParser#parseFile(java.io.File)
 	 */
 	public Collection<Accuser> parseFile(File file) throws IOException {
 		Scanner sc = null;
 
 		Collection<Accuser> parsed = null;
 
 		try {
 			sc = new Scanner(file);
 
 			parsed = parse(sc);
 
 		}
 		finally {
 			if (sc != null) sc.close();
 		}
 
 		return parsed;
 	}
 
 	private Collection<Accuser> parse(Scanner sc) {
 
 		Collection<Accuser> parsed = new ArrayList<Accuser>();
 		Map<String, Accuser> map = new HashMap<String, Accuser>();
 
 		final int numLines = sc.nextInt();
 
 		for (int i = 0; i < numLines; i++) {
 			final String name = sc.next();
 			final int accusations = sc.nextInt();
 
			Accuser acc = new Accuser(name);
			map.put(name, acc);
 
 			for (int j = 0; j < accusations; j++) {
 				String newAccusedName = sc.next();
 
 				if (!map.containsKey(newAccusedName)) {
 					map.put(newAccusedName, new Accuser(newAccusedName));
 				}
 				acc.accuse(map.get(newAccusedName));
 			}
 			parsed.add(acc);
 		}
 
 		return parsed;
 	}
 }
