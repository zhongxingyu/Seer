 import java.io.BufferedReader;
 import java.io.Console;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Quiz {
 
 	int quizPosition = 0;
 	static ArrayList<String[]> values = new ArrayList<String[]>();
 	static String name = "";
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		BufferedReader reader;
 		String zeile = null;
 
 		try {
 			reader = new BufferedReader(new FileReader("fragen.txt"));
 			zeile = reader.readLine();
 
 			while (zeile != null) {
 				values.add(zeile.split(";"));
 				zeile = reader.readLine();
 			}
 			// System.out.println(values.size());
 
 		} catch (IOException e) {
 			System.err.println("Error2 :" + e);
 		}
 
 		// showQuestions(values);
 		name();
 	}
 
 	public static void name() {
 
 		Console console = System.console();
		name = console.readLine("Geben Sie Ihren Namen an: ");
 		System.out.println("Hallo " + name);
 
		System.out.println("Ein kleines Quiz...");
 		quiz(0);
 	}
 
 	public static void quiz(int position) {
 		if (position >= (values.size() - 1)) {
 			quizend();
 			return;
 		}
 		System.out.println(values.get(position)[0]);
 		Console console = System.console();
 		String myString = console.readLine("Antwort: ");
 
 		if (myString.equalsIgnoreCase(values.get(position + 1)[0])) {
 			System.out.println("Richtig");
 			position = position + 2;
 			quiz(position);
 		}
 
 		else {
 			quiz(position);
 		}
 
 	}
 
 	public static void quizend() {
 		System.out.println("Herlichen Glueckwunsch " + name);
 		System.out.println("du hast das Quiz erfolgreich beendet!");
 
 		System.out.println("Moechtest du das Quiz erneut starten?");
 		System.out.println("Geben Sie JA oder NEIN ein!");
 
 		Console console = System.console();
 		String answer = console.readLine("Antwort: ");
 		System.out.println("Text: " + answer);
 
 		checkName(answer);
 	}
 
 	public static void checkName(String ans) {
 
 		if (ans.equals("JA")) {
 			System.out.println(ans + " ich moechte das Quiz wiederholen!");
 
 			quiz(0);
 
 		} else {
 			System.out.println("Nein ich moechte das Quiz nicht wiederholen!");
 			System.out.println(name + " hat das Quiz beendet!");
 
 		}
 	}
 
 }
