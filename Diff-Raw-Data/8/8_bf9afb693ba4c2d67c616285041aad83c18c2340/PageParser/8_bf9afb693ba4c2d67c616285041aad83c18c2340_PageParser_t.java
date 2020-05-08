 package uk.co.brotherlogic.jarpur;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import uk.co.brotherlogic.jarpur.replacers.ContainerReplacer;
 import uk.co.brotherlogic.jarpur.replacers.ForReplacer;
 import uk.co.brotherlogic.jarpur.replacers.IfReplacer;
 import uk.co.brotherlogic.jarpur.replacers.PlainReplacer;
 import uk.co.brotherlogic.jarpur.replacers.Replacer;
 import uk.co.brotherlogic.jarpur.replacers.SimpleReplacer;
 
 public class PageParser {
 
 	private final Pattern elementFinder = Pattern.compile("(\\%\\%.*?\\%\\%)");
 
 	public Replacer parsePage(Object ref, String pageText) {
 
 		Replacer allReplacements = new ContainerReplacer();
 
 		Matcher matcher = elementFinder.matcher(pageText);
 		Map<Integer, String> typeMap = new TreeMap<Integer, String>();
 		List<Integer> startList = new LinkedList<Integer>();
 		while (matcher.find()) {
 			typeMap.put(matcher.start(), matcher.group(1));
 			startList.add(matcher.start());
 		}
 
 		int i = 0;
 		int startPoint = 0;
 		while (i < typeMap.keySet().size()) {
 			allReplacements.addReplacer(new PlainReplacer(pageText.substring(
 					startPoint, startList.get(i))));
 			startPoint = startList.get(i);
 
 			// First determine if this is an ended type of list
 			String paramText = typeMap.get(startList.get(i));
 			String firstParam = paramText.substring(2, paramText.length() - 2)
 					.split("\\s+")[0];
 
 			boolean ender = false;
 			for (String paramTexts : typeMap.values()) {
 				if (paramTexts.startsWith("%%end" + firstParam))
 					ender = true;
 			}
 
 			if (ender) {
 				// Find the end point
 				int foundCount = 1;
 				for (int j = i + 1; j < startList.size(); j++) {
 					if (typeMap.get(startList.get(j)).startsWith(
 							"%%" + firstParam))
 						foundCount++;
 					if (typeMap.get(startList.get(j)).startsWith(
 							"%%end" + firstParam))
 						foundCount--;
 
 					if (foundCount == 0) {
 						if (firstParam.startsWith("for")) {
 							allReplacements.addReplacer(new ForReplacer(
 									paramText, parsePage(ref, pageText
 											.substring(startPoint
 													+ paramText.length() + 4,
 													startList.get(j)))));
 
 							startPoint = startList.get(j)
 									+ "%%endfor%%".length();
 						} else if (firstParam.startsWith("if")) {
 
 							allReplacements.addReplacer(new IfReplacer(
 									paramText, parsePage(ref, pageText
 											.substring(startPoint
 													+ paramText.length(),
 													startList.get(j)))));
 
 							startPoint = startList.get(j)
 									+ "%%endif%%".length();
 						} else
 							System.err.println("Unknown: " + paramText);
 						i = j;
 						break;
 
 					}
 				}
 			} else {
				allReplacements.addReplacer(new SimpleReplacer(paramText
 						.substring(2, paramText.length() - 2)));
 				startPoint += paramText.length();
 			}
 
 			i++;
 		}
 
 		allReplacements.addReplacer(new PlainReplacer(pageText
 				.substring(startPoint)));
 
 		return allReplacements;
 	}
 
 	public static void main(String[] args) throws Exception {
 		String pageText = "";
 		BufferedReader reader = new BufferedReader(
 				new FileReader(
 						"/Users/simon/local/code/jarpur/src/main/java/uk/co/brotherlogic/mdbweb/HomePage.html"));
 		for (String line = reader.readLine(); line != null; line = reader
 				.readLine())
 			pageText += line;
 
 		PageParser parser = new PageParser();
 		Replacer fullPage = parser.parsePage(null, pageText);
 	}
 }
