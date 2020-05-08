 package htmlscanner;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 
 import scanner.ScanResult;
 
 
 public class Main {
 
 	@SuppressWarnings("rawtypes")
 	public static void printMap( Map map) {
 		for (Object entObject : map.entrySet()) {
 			Entry entry = (Entry) entObject;
 			//System.out.println(String.format(
//					"key (%s): '%s' value (%s): '%s' ", entry.getKey()
//							.getClass(), entry.getKey(), entry.getValue()
//							.getClass(), entry.getValue()));
 		}
 	}
 
 	public static void main(String[] args) {
 		Automaton au = new Automaton();
 		au.init("simpleconf.yaml");
 		/**/
 		String input = "ab1 10. bb0b. 10.11 100101 a";
 		ScanResult result = au.scan(input);
 		//System.out.println("tokens:");
 		for (Token t : result.getTokens()) {
 			//System.out.println(t);
 		}
 		/**/
 	}
 }
