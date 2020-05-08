 package misc;
 
 import gui.MainFrame;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 public class Help {
 	private static Help help;
 	private Map<String, String> tableOfContents;
 	private Map<String, String> tags;
 
 	private Help() {
 		// LinkedHashMap used for ordering
 		tableOfContents = new LinkedHashMap<String, String>();
 		tableOfContents.put("Introduction", "index.html");
 		tableOfContents.put("Interpreting", "interpreting.html");
 		tableOfContents.put("Comments", "comments.html");
 		tableOfContents.put("Program", "program.html");
 		tableOfContents.put("Functions", "functions.html");
 		tableOfContents.put("Statement", "statement.html");
 		tableOfContents.put("Expression", "expression.html");
 		tableOfContents.put("Assignment", "assignment.html");
 		tableOfContents.put("While Statement", "while.html");
 		tableOfContents.put("If Statement", "if.html");
 		tableOfContents.put("Assert", "assert.html");
 		tableOfContents.put("Assume", "assume.html");
 		tableOfContents.put("Ensure", "ensure.html");
 		tableOfContents.put("Axiom", "axiom.html");
 		tableOfContents.put("Invariant", "invariant.html");
 		tableOfContents.put("Quantified Expressions", "quantified_expression.html");
 		tableOfContents.put("Variables", "variables.html");
 		tableOfContents.put("Literal", "literal.html");
 		tableOfContents.put("Identifier", "identifier.html");
 		tableOfContents.put("Boolean Operations", "boolean_operations.html");
 		tableOfContents.put("Integer Operations", "integer_operations.html");
 		
 
 		tags = new HashMap<String, String>();
 		tags.put("assert", "assert.html");
 		tags.put("assertion", "assert.html");
 		tags.put("assign", "assignment.html");
 		tags.put("assignment", "assignment.html");
 		tags.put("write", "assignment.html");
 		tags.put("assume", "assume.html");
 		tags.put("axiom", "axiom.html");
 		tags.put("bool", "boolean_operations.html");
 		tags.put("boolean", "boolean_operations.html");
 		tags.put("logical", "boolean_operations.html");
 		tags.put("comment", "comments.html");
 		tags.put("ensure", "ensure.html");
 		tags.put("expression", "expression.html");
 		tags.put("function", "functions.html");
 		tags.put("main", "functions.html");
 		tags.put("return", "functions.html");
 		tags.put("identifier", "identifier.html");
 		tags.put("name", "identifier.html");
 		tags.put("if", "if.html");
 		tags.put("else", "if.html");
 		tags.put("conditional", "if.html");
 		tags.put("help", "index.html");
 		tags.put("integer", "integer_operations.html");
 		tags.put("interpret", "interpreting.html");
 		tags.put("run", "interpreting.html");
 		tags.put("invariant", "invariant.html");
 		tags.put("literal", "literal.html");
 		tags.put("constant", "literal.html");
 		tags.put("program", "program.html");
 		tags.put("syntax", "program.html");
 		tags.put("quantifier", "quantified_expression.html");
 		tags.put("exists", "quantified_expression.html");
 		tags.put("forall", "quantified_expression.html");
 		tags.put("statement", "statement.html");
 		tags.put("variable", "variables.html");
 		tags.put("array", "variables.html");
 		tags.put("declaration", "variables.html");
 		tags.put("loop", "while.html");
 		tags.put("while", "while.html");
 	}
 
 	public String[] getTableOfContents() {
 		return tableOfContents.keySet().toArray(new String[0]);
 	}
 
 	public String getHelpByTOC(String tocItem) {
 		if (tableOfContents.containsKey(tocItem)) {
 			return loadHelpFile(tableOfContents.get(tocItem));
 		} else {
 			return loadHelpFile("index.html");
 		}
 	}
 
 	public String getHelpByTag(String tag) {
 		return loadHelpFile(tags.get(tag));
 	}
 
 	public String[] getTags() {
 		return tags.keySet().toArray(new String[0]);
 	}
 	
 	private String getFileAsString(String filename) {
 		InputStream in = MainFrame.class.getResourceAsStream(filename);
 		if (in != null) {
 			BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
 			String line = null;
 			String fileText = new String();
 			try {
 				while ((line = inReader.readLine()) != null) {
					fileText += line;
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return fileText;
 		} else {
 			return null;
 		}
 	}
 
 	private String loadHelpFile(String filename) {
 		String helpText = getFileAsString("doc/" + filename);
 		if (helpText != null) {
 			return helpText;
 		} else {
 			return "<html><head><title>File not found</title></head><body>Helpfile not found.</body></html>";
 		}
 	}
 
 	public String getSmallStyle() {
 		String smallStyle = getFileAsString("doc/small.css");
 		if (smallStyle != null) {
 			return smallStyle;
 		} else {
 			return "";
 		}
 	}
 	
 	public String getDefaultStyle() {
 		String defaultStyle = getFileAsString("doc/default.css");
 		if(defaultStyle != null) {
 			return defaultStyle;
 		} else {
 			return "";
 		}
 	}
 
 	public static Help getInstance() {
 		return help == null ? (help = new Help()) : help;
 	}
 }
