 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 
 public class SystemeExpert {
 	
 	
 	public static void main(String args[])
 			throws ExpertException, IOException{
 		List<Fact> baseFacts = new LinkedList<Fact>();
 		Engine engine = parseFile("");
 		
 	}
 	
 	public static Engine parseFile(String fileURL) throws ExpertException, IOException {
 		FileInputStream fis = new FileInputStream(fileURL);
 		InputStreamReader isr = new InputStreamReader(fis);
 		BufferedReader br = new BufferedReader(isr);
 		String line = br.readLine().trim();
 		boolean readBF = false;
 		boolean readGoal = false;
 		boolean readRules = false;
 		List<Fact> baseFacts = new LinkedList<Fact>();
 		List<Fact> goals = new LinkedList<Fact>();
 		List<Rule> rules = new LinkedList<Rule>();
 		while (line != null) {
			line = line.trim();
 			if (ends(line)) {
 				readBF = false;
 				readGoal = false;
 				readRules = false;
 			} else if (optionalString("#BF", line)) {
 				readBF = true;
 			} else if (readBF) {
 				baseFacts.add(new Fact(line));
 			} else if (optionalString("#Goal", line)) {
 				readGoal = true;
 			} else if (readGoal) {
 				goals.add(new Fact(line));
 			} else if (optionalString("#Rules", line)) {
 				readRules = true;
 			} else if (readRules && requiredString("$Rule:", line)) {
 				readRules = parseRule(rules, br);
 				continue; // skip the readLine()
 			}
 			
			line = br.readLine();
 		}
 		
 		Engine engine = new Engine(baseFacts);
 		engine.setRules(rules);
 		//TODO: validate?
 		return engine;
 	}
 
 	private static boolean parseRule(List<Rule> rules, BufferedReader br)
 			throws IOException, ExpertException {
 		boolean readIf = false;
 		boolean readThen = false;
 		String line = br.readLine().trim();
 		List<Fact> rf = new LinkedList<Fact>();
 		Fact df = null;
 		while (line != null
 				&& !optionalString("$Rule:", line)
 				&& !ends(line)) {
 			if (!readIf && !readThen && requiredString("IF", line)) {
 				readIf = true;
 				stuffFact(line, rf, readIf, "IF");
 			} else if (readIf && optionalString("THEN", line)) {
 				readIf = false;
 				readThen = true;
 				df = stuffFact(line, rf, readIf, "THEN");
			} else if ((readIf || readThen) && optionalString("AND", line)) {
 				stuffFact(line, rf, readIf, "AND");
 			}
 			line = br.readLine().trim();
 		}
 		//TODO:check if everything is OK
 		rules.add(new Rule(rf, df));
 		return !ends(line);
 	}
 
 	private static Fact stuffFact(String line, List<Fact> rf,
 			boolean readIf, String toRemove) throws ExpertException {
 		line = line.replace(toRemove, "").trim();
 		Fact fact = new Fact(line);
 		if (readIf)
 			rf.add(fact);
 		return fact;
 	}
 
 	private static boolean requiredString(String string, String line)
 			throws ExpertException {
 		if (!optionalString(string, line))
 			throw new ExpertException("Expected String not found:" + string);
 		else
 			return true;
 	}
 
 	private static boolean ends(String line) {
 		return line.startsWith("#End");
 	}
 	
 	private static boolean optionalString(String string, String line) {
 		if (!line.startsWith(string))
 			return false;
 		else
 			return true;
 	}
 }
