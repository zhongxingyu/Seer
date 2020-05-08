 package org.csgames.tse;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.*;
 
 public class QueryParser {
 	private static final Pattern RE_CALC = Pattern.compile("\\{CALC ([^}]+)\\}", Pattern.CASE_INSENSITIVE);
 	
 	public Query parse(String query) {
 		query = preprocess(query);
 		System.out.println("PRE: " + query);
 		String[] tokens = tokenize(query);
 		
 		for (String s : tokens) {
 			System.out.println("Token: " + s);
 		}
 		
 		List<QueryItem> items = parse(tokens);
 		return new Query(items);
 	}
 	
 	private String[] tokenize(String query) {
 		int position = 0;
 		boolean inQuotes = false;
 		ArrayList<String> words = new ArrayList<String>();
 		
 		StringBuilder sb = new StringBuilder();
 		while (position < query.length()) {
 			char c = query.charAt(position++);
 			
 			if (inQuotes) {
 				if (c == '"') {
 					words.add(sb.toString());
 					sb.setLength(0);
 				} else {
 					sb.append(c);
 				}
 			} else if (c == ' ') {
 				words.add(sb.toString());
 				sb.setLength(0);
 			} else if (c != '"') {
 				sb.append(c);
 			}
 			
 			if (c == '"') {
 				inQuotes = !inQuotes;
 			}
 		}
 		
 		if (sb.length() > 0) {
 			words.add(sb.toString());
 		}
 		
 		return words.toArray(new String[words.size()]);
 	}
 	
 	private List<QueryItem> parse(String[] tokens) {
 		return new ArrayList<QueryItem>();
 	}
 	
 	private String preprocess(String query) {
 		// Extract CALC operations
 		CalcParser calc = new CalcParser();
 		Matcher m = RE_CALC.matcher(query);
 		
 		StringBuilder sb = new StringBuilder();
 		
 		int position = 0;
 		while (!m.hitEnd()) {
 			if (m.find()) {
 				// Add everything before the match to the StringBuilder
 				sb.append(query.substring(position, m.start()));
 				position = m.start();
 				
 				// Extract the match, process it, append to the StringBuilder
 				String match = m.group(1);
 				sb.append(calc.solve(match));
 				
 				// Advance the position by the length of the match
 				position += match.length() + "{CALC }".length();
 			}
 		}
 		
 		// Add the rest of the query to the end of the QueryParser
 		if (position >= 0 && position < query.length()) {
 			sb.append(query.substring(position));
 		}
 		
 		return sb.toString();
 	}
 	
 	public static void main(String[] argv) {
 		QueryParser q = new QueryParser();
 		System.out.println(q.parse("\"the * example\"{4} {CALC 8*(8+9/3)}..{CALC 90000/10+1} OR *park NOT bench"));
 		System.out.println(q.parse("Test {CALC 8+1+3+5/2} oaeu {calc 4+2}"));
 		System.out.println(q.parse("an example {CALC 3}"));
 		System.out.println(q.parse("{CALC 3} an example"));
 	}
 }
