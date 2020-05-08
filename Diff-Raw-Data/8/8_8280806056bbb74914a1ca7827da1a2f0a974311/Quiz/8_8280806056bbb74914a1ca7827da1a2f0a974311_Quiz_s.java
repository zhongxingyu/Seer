 import java.io.BufferedReader;
 import java.io.Console;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Quiz {
 
 	int quizPosition = 0;
 	static ArrayList<String[]> values = new ArrayList<String[]>();
 
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
 		System.out
 				.println("Herlichen Glckwunsch, Sie haben das Quiz erfolgreich beendet!");
 
 		System.out.println("Mchten Sie das Quiz erneut starten?");
 		System.out.println("Geben Sie JA oder NEIN ein!");
 
 		Console console = System.console();
 		String answer = console.readLine("Antwort: ");
 		System.out.println("Text: " + answer);
 		checkName(answer);
 	}
 
	public static Boolean checkName(String name) {
 
 		if (name.equals("JA")) {
 			System.out.println(name + " ich mchte das Quiz wiederholen!");
 
 			quiz(0);
 
			return true;
 		} else {
 			System.out.println("Nein ich mchte das Quiz nicht wiederholen!");
 			System.out.println("Sie haben das Quiz beendet!");
			return false;
 
 		}
 	}
 
 }
