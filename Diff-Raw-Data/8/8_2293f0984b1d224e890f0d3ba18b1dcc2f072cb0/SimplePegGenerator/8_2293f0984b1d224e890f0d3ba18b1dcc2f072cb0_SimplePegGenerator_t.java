 /*******************************************************************************
  * Copyright (c) 2003-2012 Bob Foster.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Bob Foster - initial API and implementation
  *******************************************************************************/
  
 package org.genantics.peggen;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.HashSet;
 
 /**
  * Generates a PEG parser in the style of Parser.
  * 
  * <p>Ignore the many IDE warnings.
  * Use of == to compare strings is valid when comparing
  * String literals and variables set from String literals,
  * and much faster.</p>
  * 
  * @author Bob Foster
  */
 public class SimplePegGenerator extends PegNodeVisitor implements Generator {
 
 	protected boolean verbose;
 	protected String packageName;
 	protected String rngresult;
 	protected String setresult;
 	protected String sresult;
 	protected char cresult;
 	protected int loc;
 	protected char[] in;
 	protected String startRule;
 	protected String tab;
 	protected String className;
 	protected PrintWriter writer;
 	protected String indent;
 	
 	protected boolean suppressRule;
 	protected boolean append;
 	protected int count;
 
 	/**
 	 * Generate parser from parse tree and input text.
 	 * @param grammar Root of parse tree.
 	 * @param in Input text.
 	 * @param writer PrintWriter to receive output.
 	 * @param className Name of generated class.
 	 * @param startRule Name of start rule or null if first
 	 * rule is to be used.
 	 * @param tab Tab character or spaces.
 	 */
 	public void generate(Node grammar, char[] in, PrintWriter writer, String packageName, String className, String startRule, String tab) {
 		this.in = in;
 		this.writer = writer;
 		this.packageName = packageName;
 		this.className = className;
 		this.startRule = startRule;
 		this.tab = tab;
 		indent = "";
 		loc = 0;
     preprocess(grammar);
 		visit(grammar);
 	}
 	
 	/**
 	 * For unit testing. Generate anything but entire grammar.
 	 */
 	void generate(Node node, String input, PrintWriter writer, String tab) {
 		this.in = input.toCharArray();
 		this.writer = writer;
 		this.tab = tab;
 		indent = "";
 		loc = 0;
 		count = -1;
 		visit(node);
 	}
 	
 	/**
 	 * For unit testing.
 	 */
 	void reset(String input) {
 		in = input.toCharArray();
 	}
 	
 	/**
 	 * For unit testing.
 	 */
 	String[] getCharClass() {
 		return new String[] {setresult, rngresult};
 	}
 	
 	/**
 	 * For unit testing.
 	 */
 	char getChar() {
 		return cresult;
 	}
 	
 	/**
 	 * For unit testing.
 	 */
 	void setVerbose(boolean verbose) {
 		this.verbose = verbose;
 	}
   
   HashSet<String> allRules = new HashSet<String>();
   HashSet<String> BNFRules = new HashSet<String>();
   
   /**
    * Preprocess Definition and BNFDefinition nodes and add them to the
    * allRules and xRules sets as appropriate. This information could
    * be used to detect missing rule definitions, but there is no
    * provision for error handling in the API. It is used to detect
    * when WS rule needs to be generated.
    * 
    * TODO Add missing rule error detection.
    * 
    * @param node to visit
    */
   void preprocess(Node node) {
     if (node.name == "Definition" || node.name == "BNFDefinition") {
       Node ident = node.child;
       expect(ident, "Identifier");
       String name = new String(in, ident.offset, ident.length).trim();
       allRules.add(name);
       if (node.name == "BNFDefinition")
         BNFRules.add(name);
     } else {
       for (Node child = node.child; child != null; child = child.next)
         preprocess(child);
     }
   }
 	
 	void collectClass(Node node) {
 		// Class <- '['~ (!']' Range)* ']'~ Spacing
 		StringBuffer rng = new StringBuffer();
 		StringBuffer set = new StringBuffer();
 		for (Node range = node.child; range != null; range = range.next) {
 			Node c1 = range.child;
 			expect(c1, "Char");
 			Node c2 = c1.next;
 			if (c2 != null) {
 				expect(c2, "Char");
 				visit(c1);
 				rng.append(cresult);
 				visit(c2);
 				rng.append(cresult);
 			}
 			else {
 				visit(c1);
 				set.append(cresult);
 			}
 		}
 		rngresult = rng.toString();
 		setresult = set.toString();
 	}
 	
 	void visitClass(Node node) {
 		collectClass(node);
 		boolean hasrng = rngresult.length() > 0;
 		boolean hasset = setresult.length() > 0;
 		if (hasrng || hasset) {
 			if (hasrng && hasset) {
 				genSet();
 				writer.print(indent);
 				writer.print("if (!match) ");
 				leftBrace();
 				genRange();
 				rightBrace();
 			}
 			else if (hasrng) {
 				genRange();
 			}
 			else {
 				genSet();
 			}
       if (inBNFRule)
         callWS();
 		}
 	}
 	
 	void genSet() {
 		if (setresult.length() == 1) {
 			writer.print(indent);
 			writer.print("match = matchChar(\'");
 			writer.print(escapeChar(setresult.charAt(0), false));
 			writer.println("\');");
 		}
 		else {
 			writer.print(indent);
 			writer.print("match = matchSet(\"");
 			writer.print(escapeLiteral(setresult));
 			writer.println("\");");
 		}
 	}
 	
 	void genRange() {
 		writer.print(indent);
 		writer.print("match = matchRange(\"");
 		writer.print(escapeLiteral(rngresult));
 		writer.println("\");");
 	}
 	
   boolean inBNFRule = false;
   
 	void visitDefinition(Node node) {
 		// Definition <- Identifier DEFSUPPRESS? LEFTARROW Expression
 		Node ident = node.child;
 		expect(ident, "Identifier");
 		String name = new String(in, ident.offset, ident.length).trim();
     
     inBNFRule = node.name == "BNFDefinition";
 		
 		if (verbose) System.out.println(name+" <-");
 		
 		writer.println();
 		writer.print(indent);
 		writer.print("protected boolean rule");
 		writer.print(name);
 		writer.print("(Node parent) ");
 		leftBrace();
 		
 		// body
     
     // This can be a bit confusing. Originally, suppress was designed as
     // ~n (conditional suppress) or ~~ (always suppress). But the latter
     // looked messy, so it was changed to ~n (conditional) or ~ (always).
     // The old syntax is still allowed.
 		Node expr = ident.next;
 		suppressRule = false;
 		count = -1;
 		loc = 0;
 		if (expr != null && expr.name == "DEFSUPPRESS") {
 			// DEFSUPPRESS <- SUPPRESS (SUPPRESS / NUM)?
 			Node suppress = expr.child;
 			expect(suppress, "SUPPRESS");
 			suppressRule = true;
 			Node bodysuppress = suppress.next;
 			if (bodysuppress != null) {
 				if (bodysuppress.name == "SUPPRESS")
 					;
 				else {
 					int begin = suppress.offset+suppress.length;
 					int end = expr.offset+expr.length;
 					String num = strip(new String(in, bodysuppress.offset, bodysuppress.length));
 					count = Integer.parseInt(num);
 					suppressRule = false;
 				}
 			}
 			expr = expr.next;
 		}
 		
 		// two styles of rule
 		// ??could be a third style for lexical-only rules
 		
 		if (suppressRule) {
 			if (expr != null) {
 				writer.print(indent);
 				writer.println("int inmark = inpos;");
 				writer.print(indent);
 				writer.println("int outmark = outpos;");
 				writer.print(indent);
 				writer.println("boolean match = true;");
 			
 				visit(expr);
 
 				writer.print(indent);
 				writer.print("if (!match) ");
 				leftBrace();
 				writer.print(indent);
 				writer.println("inpos = inmark;");
 				writer.print(indent);
 				writer.println("outpos = outmark;");
 				writer.print(indent);
 				writer.println("return false;");
 				rightBrace();
 			}
 			writer.print(indent);
 			writer.println("return true;");
 			
 		}
 		else {
 			writer.print(indent);
 			writer.println("int outstart = outpos;");
 			writer.print(indent);
 			writer.print("if (sameRule(\"");
 			writer.print(name);
 			writer.println("\")) return out[outstart].success;");
 			writer.print(indent);
 			writer.print("Node rule = new Node(\"");
 			writer.print(name);
 			writer.println("\", parent, inpos);");
 			writer.print(indent);
 			writer.println("out[outpos++] = rule;");
 			writer.println();
 			if (count >= 0) {
 				writer.print(indent);
 				writer.println("int count = 0;");
 			}
 			
 			if (expr != null) {
 				writer.print(indent);
 				writer.println("int inmark = inpos;");
 				writer.print(indent);
 				writer.println("int outmark = outpos;");
 				writer.print(indent);
 				writer.println("boolean match = true;");
 			
 				visit(expr);
 
 				writer.print(indent);
 				writer.print("if (!match) ");
 				leftBrace();
 				writer.print(indent);
 				writer.println("inpos = inmark;");
 				writer.print(indent);
 				writer.println("outpos = outmark;");
 				writer.print(indent);
 				writer.println("return fail(rule, outstart);");
 				rightBrace();
 			}
 			
 			if (count >= 0) {
 				writer.print(indent);
 				writer.print("rule.remove = count < ");
 				writer.print(count);
 				writer.println(";");
 			}
 			writer.print(indent);
 			writer.println("return succeed(rule);");
 		}
 		
 		rightBrace();
 	}
 	
 	protected void expectNonNull(Node expr, String string) {
 		if (expr == null) throw new IllegalArgumentException("Found null, expecting "+string);
 	}
 
 	protected void leftBrace() {
 		writer.println("{");
 		indentIn();
 	}
 	
 	protected void rightBrace() {
 		indentOut();
 		writer.print(indent);
 		writer.println("}");
 	}
 
 	protected void rightBraceNoLn() {
 		indentOut();
 		writer.print(indent);
 		writer.print("}");
 	}
 
 	void visitDEFSUPPRESS(Node node) {
 		// DEFSUPPRESS <- SUPPRESS (SUPPRESS / [1-9][0-9]*)?
 	}
 	
 	void visitDOT(Node node) {
 		// DOT <- '.'~ Spacing~
 		writer.print(indent);
 		writer.println("match = matchAny();");
 	}
 
 	void visitExpression(Node node) {
 		// Expression~2 <- Sequence (SLASH~ Sequence)*
 		int loc = ++this.loc;
 		if (count >= 0) {
 			writer.print(indent);
 			writer.print("int markCount");
 			writer.print(loc);
 			writer.println(" = count;");
 		}
 		save(loc);
 		boolean first = true;
 		for (Node sequence = node.child; sequence != null; sequence = sequence.next) {
 			if (first) {
 				first = false;
 				visit(sequence);
 			}
 			else {
 				writer.print(indent);
 				writer.print("if (!match) ");
 				leftBrace();
 				if (count >= 0) {
 					writer.print(indent);
 					writer.print("count = markCount");
 					writer.print(loc);
 					writer.println(";");
 				}
 				restore(loc);
 				
 				visit(sequence);
 				
 				rightBrace();
 			}
 		}
 	}
 	
 	protected void save(int i) {
 		saveIn(i);
 		saveOut(i);
 	}
 	
 	protected void restore(int i) {
 		restoreIn(i);
 		restoreOut(i);
 	}
 	
 	protected void saveIn(int i) {
 		writer.print(indent);
 		writer.print("int inmark");
 		writer.print(i);
 		writer.println(" = inpos;");
 	}
 
 	protected void saveOut(int i) {
 		writer.print(indent);
 		writer.print("int outmark");
 		writer.print(i);
 		writer.println(" = outpos;");
 	}
 	
 	protected void restoreIn(int i) {
 		writer.print(indent);
 		writer.print("inpos = inmark");
 		writer.print(i);
 		writer.println(";");
 	}
 	
 	protected void restoreOut(int i) {
 		writer.print(indent);
 		writer.print("outpos = outmark");
 		writer.print(i);
 		writer.println(";");
 	}
 
 	void visitGrammar(Node node) {
 		// Grammar <- Spacing Definition+ EndOfFile
 		expect(node, "Grammar");
 		if (packageName != null && packageName.length() > 0) {
 			writer.print("package ");
 			writer.print(packageName);
 			writer.println(";");
 			writer.println();
 		}
 		writer.println("import org.genantics.peggen.Node;");
 		writer.println();
 		writer.println("import java.util.LinkedList;");
 		writer.println("import java.util.List;");
 		writer.println();
 		writer.print("public class ");
 		writer.print(className);
 		writer.print(" ");
 		addExtendsImplements();
 		leftBrace();
 		writer.print(indent);
 		writer.println("private Node[] out;");
 		writer.print(indent);
 		writer.println("private char[] in;");
 		writer.print(indent);
 		writer.println("private int inpos;");
 		writer.print(indent);
 		writer.println("private int inend;");
 		writer.print(indent);
 		writer.println("private int outpos;");
 		writer.print(indent);
 		writer.println("private int outend;");
 		writer.print(indent);
 		writer.println("private LinkedList errors;");
     writer.println("private Node lastFail;");
 		writer.println();
 		writer.print(indent);
 		writer.println("private static final int INITIAL_OUT_SIZE = 100;");
 		writer.println();
 		insertTopMethods();
 		writer.print(indent);
 		writer.println("/**");
 		writer.print(indent);
 		writer.println(" * Parse language according to grammar and return parse tree.");
 		writer.print(indent);
 		writer.println(" * @param input String containing language.");
 		writer.print(indent);
 		writer.println(" */");
 		writer.print(indent);
 		writer.print("public Node[] parseLanguage(String input) ");
 		leftBrace();
 		writer.print(indent);
 		writer.println("char[] buf = input.toCharArray();");
 		writer.print(indent);
 		writer.println("return parseLanguage(buf, 0, buf.length);");
 		rightBrace();
 		writer.println();
 		writer.print(indent);
 		writer.println("/**");
 		writer.print(indent);
 		writer.println(" * Parse language according to grammar and return parse tree.");
 		writer.print(indent);
 		writer.println(" * @param buf char[] containing language input.");
 		writer.print(indent);
 		writer.println(" * @param start Offset in buf.");
 		writer.print(indent);
 		writer.println(" * @param length Number of chars in buf.");
 		writer.print(indent);
 		writer.println(" */");
 		writer.print(indent);
 		writer.print("public Node[] parseLanguage(char[] buf, int start, int length) ");
 		leftBrace();
 		writer.print(indent);
 		writer.println("out = new Node[INITIAL_OUT_SIZE];");
 		writer.print(indent);
 		writer.println("outpos = 0;");
 		writer.print(indent);
 		writer.println("outend = INITIAL_OUT_SIZE;");
 		writer.print(indent);
 		writer.println("in = buf;");
 		writer.print(indent);
 		writer.println("inpos = start;");
 		writer.print(indent);
 		writer.println("inend = start + length;");
 		
 		if (startRule == null) {
 			Node defn = node.child;
 			for (; defn.name != "Definition" && defn.name != "BNFDefinition"; defn = defn.next)
 				continue;
 			if (defn == null) error("No definitions in grammar");
 			Node ident = defn.child;
 			expect(ident, "Identifier");
 			startRule = new String(in, ident.offset, ident.length).trim();
 		}
 		
 		writer.print(indent);
 		writer.print("if (");
 		writer.print(ruleName(startRule));
 		writer.print("(null)) ");
 		leftBrace();
 		writer.print(indent);
 		writer.println("return pack();");
 		rightBrace();
 		writer.print(indent);
 		writer.print("else ");
 		leftBrace();
 		writer.print(indent);
 		writer.println("error();");
 		writer.print(indent);
 		writer.println("return null;");
 		rightBrace();
 		rightBrace();
 		writer.println();
 		writer.print(indent);
 		writer.println("/**");
 		writer.print(indent);
 		writer.println(" * Returns list of error messages if parse failed,");
 		writer.print(indent);
 		writer.println(" * or null if parse succeeded (except that if");
 		writer.print(indent);
 		writer.println(" * the error rule is used it is possible for a");
 		writer.print(indent);
 		writer.println(" * parse with errors to succeed).");
 		writer.print(indent);
 		writer.println(" */");
 		writer.print(indent);
 		writer.print("public List getErrors() ");
 		leftBrace();
 		writer.print(indent);
 		writer.println("return errors;");
 		rightBrace();
 		
 		// Generate definitions
 		for (Node child = node.child; child != null; child = child.next)
 			visit(child);
 		
 		generateBoilerPlate();
 		
 		rightBrace();
 	}
 
 	protected void addExtendsImplements() {
 	}
 
 	protected void insertTopMethods() {
 	}
 
 	protected String ruleName(String startRule) {
 		return "rule"+startRule;
 	}
 
 	protected void error(String string) {
 		throw new IllegalArgumentException(string);
 	}
 
 	protected void expect(Node node, String name) {
 		if (node == null || !name.equals(node.name))
 			throw new IllegalArgumentException("Expecting "+name);
 	}
 
 	protected void indentIn() {
 		indent += tab;
 	}
 
 	protected void indentOut() {
 		indent = indent.substring(0, indent.length()-tab.length());
 	}
 	
 	void visitError(Node node) {
 		expect(node, "Error");
 		collectLiteral(node.child);
 		writer.print(indent);
 		writer.println("error();");
 		writer.print(indent);
 		writer.println("match = true;");
 	}
 
 	void visitIdentifier(Node node) {
 		// this is only reached for nonterminal identifiers used in rule bodies
 		String id = strip(new String(in, node.offset, node.length));
 		writer.print(indent);
 		writer.print("match = ");
 		writer.print(ruleName(id));
 		printlnArg();
     if (inBNFRule && !BNFRules.contains(id))
       callWS();
 		if (count >= 0) {
 			writer.print(indent);
 			writer.println("if (match) count++;");
 		}
 	}
 	
 	/**
 	 * Strip string of lexical artifacts
 	 * whitespace and comments.
 	 */
 	protected String strip(String lex) {
 		for (int i = 0; i < lex.length(); i++) {
 			char c = lex.charAt(i);
 			if (c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == '#')
 				return lex.substring(0, i);
 		}
 		return lex;
 	}
 	
 	protected void collectLiteral(Node literal) {
 		expect(literal, "Literal");
 		StringBuffer buf = new StringBuffer();
 		for (Node child = literal.child; child != null; child = child.next) {
 			expect(child, "Char");
 			visit(child);
 			buf.append(cresult);
 		}
 		sresult = buf.toString();
 	}
 
 	void visitLiteral(Node node) {
 		// Literal <- [']~ (!['] Char)* [']~ Spacing
 		// / ["]~ (!["] Char)* ["]~ Spacing
 		collectLiteral(node);
 		if (sresult.length() > 0) {
 			if (sresult.length() == 1) {
 				writer.print(indent);
 				writer.print("match = matchChar('");
 				writer.print(escapeChar(sresult.charAt(0), false));
 				writer.println("');");
 			}
 			else {
 				writer.print(indent);
 				writer.print("match = matchLiteral(\"");
 				writer.print(escapeLiteral(sresult));
 				writer.println("\");");
 			}
       if (inBNFRule)
         callWS();
 		}
 	}
     
   void callWS() {
     writer.print(indent);
     writer.println("if (match)");
     writer.print(indent);
     writer.print(tab);
     writer.print(ruleName("WS"));
     printlnArg();
   }
 
 	void visitPrefix(Node node) {
 		// Prefix~2 <- (AND / NOT)? Suffix
 		Node andnot = node.child;
 		boolean not = andnot.name == "NOT";
 		Node suffix = andnot.next;
 		
 		int mark = ++loc;
 
 		save(mark);
 		
 		visit(suffix);
 		
 		restore(mark);
 		
 		if (not) {
 			writer.print(indent);
 			writer.println("match = !match;");
 		}
 	}
 	
 	void visitPrimary(Node node) {
 		// Primary~ <- Identifier !LEFTARROW
 		// / &'(' Term
 		// / Literal / Class / DOT
 		
 		// never visited
 	}
 	
 	void visitTerm(Node node) {
 		expect(node, "Term");
 		if (node.child != null) {
 			// () is allowed
 			writer.print(indent);
 			leftBrace();
 
 			visit(node.child);
 			
 			rightBrace();
 		}
 	}
 	
 	protected String escapeLiteral(String s) {
 		StringBuffer buf = new StringBuffer();
 		for (int i = 0, n = s.length(); i < n; i++) {
 			buf.append(escapeChar(s.charAt(i), true));
 		}
 		return buf.toString();
 	}
 	
 	protected String escapeChar(char c, boolean toliteral) {
 		String result;
 		if (c == '\r')
 			result = "\\r";
 		else if (c == '\n')
 			result = "\\n";
 		else if (c == '\t')
 			result = "\\t";
 		else if (c == '\\')
 			result = "\\\\";
 		else if (c == '\'' && !toliteral)
 			result = "\\'";
 		else if (c == '"' && toliteral)
 			result = "\\\"";
 		else
 			result = ""+c;
 		return result;
 	}
 
 	void printlnArg() {
 		writer.print("(");
 		if (suppressRule)
 			writer.print("parent");
 		else
 			writer.print("rule");
 		writer.println(");");
 	}
 
 	void visitSequence(Node node) {
 		// Sequence~2 <- Prefix*
 		boolean first = true;
 		for (Node prefix = node.child; prefix != null; prefix = prefix.next) {
 			if (first) {
 				first = false;
 				visit(prefix);
 			}
 			else {
 				writer.print(indent);
 				writer.print("if (match) ");
 				leftBrace();
 				
 				visit(prefix);
 				
 				rightBrace();
 			}
 		}
 	}
 
 	void visitSuffix(Node node) {
 		// Suffix~2 <- SuppressPrimary (QUESTION / STAR / PLUS)?
 		Node sp = node.child;
 		Node post = sp.next;
 		if (post.name == "QUESTION") {
 			visit(sp);
 			writer.print(indent);
 			writer.println("match = true;");
 		}
 		else if (post.name == "STAR") {
 			printSTAR(sp);
 		}
 		else if (post.name == "PLUS") {
 			visit(sp);
 			writer.print(indent);
 			writer.print("if (match) ");
 			leftBrace();
 			printSTAR(sp);
 			rightBrace();
 		}
 		else
 			expect(post, "STAR");
 	}
 	
 	protected void printSTAR(Node e) {
 		writer.print(indent);
 		writer.print("do ");
 		leftBrace();
 		visit(e);
 		rightBraceNoLn();
 		writer.println(" while(match);");
 		writer.print(indent);
 		writer.println("match = true;");
 	}
 
 	void visitSuppressPrimary(Node node) {
 		// SuppressPrimary~2 <- Primary SUPPRESS?
 		Node primary = node.child;
 		Node suppress = primary.next;
 		int mark = ++loc;
 		
 		saveOut(mark);
 		
 		visit(primary);
 		
 		restoreOut(mark);
 	}
   
   protected String eol;
   
   protected void generateArray(String[] array) {
 		writer.println();
 		
     if (eol == null) {
       StringWriter sw = new StringWriter();
       PrintWriter pw = new PrintWriter(sw);
       pw.println();
       pw.close();
       eol = sw.getBuffer().toString();
     }
 		
 		StringBuffer buf = new StringBuffer();
 		for (int i = 0; i < array.length; i++) {
 			String s = array[i];
 			for (int j = 0, n = s.length(); j < n; j++) {
 				char c = s.charAt(j);
 				if (c == '\t')
 					buf.append(tab);
 				else if (c == '\n')
 					buf.append(eol);
 				else
 					buf.append(c);
 			}
 			writer.print(indent);
 			writer.print(buf.toString());
 			buf.setLength(0);
 		}
   }
   
 	protected void generateBoilerPlate() {
     if (!BNFRules.isEmpty() && !allRules.contains("WS"))
       generateArray(WS);
     generateArray(PLATE);
 	}
 	
 	protected String[] getBoilerPlate() {
 		return PLATE;
 	}
 	
   protected static final String[] WS = {
   "protected boolean ruleWS(Node parent) {\n",
  "  rule$WS(parent);\n",
  "  return true;\n",
   "}\n",
   };
 
 	protected static final String[] PLATE = new String[] {
     "protected int[] indentStack = new int[1];\n",
     "protected int indentIndex = 0;\n",
     "protected int curIndent = 0;\n",
     "protected int indentPos = 0;\n",
     "protected int tabSpaces = 8;\n",
     "\n",
     "protected boolean rule$Indent(Node parent) {\n",
     "  if (inpos == indentPos && curIndent > indentStack[indentIndex]) {\n",
     "  	if (indentIndex == indentStack.length - 1) {\n",
     "  	  int[] tmp = new int[indentStack.length * 2];\n",
     "  	  System.arraycopy(indentStack, 0, tmp, 0, indentIndex+1);\n",
     "  	  indentStack = tmp;\n",
     "  	}\n",
     "  	indentStack[++indentIndex] = curIndent;\n",
     "  	return true;\n",
     "  }\n",
     "  return false;\n",
     "}\n",
     "\n",
     "protected boolean rule$Outdent(Node parent) {\n",
     "	if (inpos == indentPos && curIndent < indentStack[indentIndex]\n",
     "	    && curIndent <= indentStack[indentIndex-1]) {\n",
     "	  indentIndex--;\n",
     "	  return true;\n",
     "	}\n",
     "	return false;\n",
     "}\n",
     "\n",
     "protected boolean rule$WS(Node parent) {\n",
     "  int start = -1;\n",
    "  int savePos = inpos;\n",
     "  boolean match = true;\n",
     "  while (match) {\n",
     "    while(matchSet(\" \\t\\r\"))\n",
     "  	  ;\n",
     "    if (match = matchChar('\\n'))\n",
     "  	  start = inpos;\n",
     "  }\n",
     "  if (start >= 0) {\n",
     "    curIndent = 0;\n",
     "  	for (int i = start; i < inpos; i++) {\n",
     "  	  if (in[i] == ' ')\n",
     "  	    curIndent++;\n",
     "  	  else if (in[i] == '\\t')\n",
     "  	  	curIndent += tabSpaces;\n",
     "  	}\n",
     "  	indentPos = inpos;\n",
     "  }\n",
    "  return savePos != inpos;\n",
     "}\n",
     "\n",
     "protected boolean rule$Error(Node parent) {\n",
     "  error();\n",
     "  return true;\n",
     "}\n",
     "\n",
     "private void error() {\n",
 		"  if (errors == null)\n",
 		"    errors = new LinkedList();\n",
     "  int pos = inpos;\n",
     "  if (lastFail != null && lastFail.offset > pos)\n",
     "    pos = lastFail.offset;\n",
     "  if (pos >= in.length)\n",
     "    pos = in.length - 1;\n",
     "  errors.add(\"Parse error at line \"+countLines(pos)+\":\");\n",
 		"  errors.add(collectErrorString(pos));\n",
     "  errors.add(indicateCharPos(pos));\n",
     "}\n",
     "\n",
     "private boolean eol(char c) {\n",
     "  return c == '\\r' || c == '\\n';\n",
     "}\n",
     "\n",
     "private int countLines(int pos) {\n",
     "  int line = 1;\n",
     "  for (int i = 0; i < pos; i++) {\n",
     "    char c = in[i];\n",
     "    if (eol(c))\n",
     "      line++;\n",
     "  }\n",
     "  return line;\n",
     "}\n",
     "\n",
     "private String indicateCharPos(int pos) {\n",
     "  StringBuilder sb = new StringBuilder();\n",
     "  for (int i = pos-1 : pos; i >= 0; i--) {\n",
 		"    char c = in[i];\n",
 		"    if (eol(c))\n",
     "      break;\n",
     "    else\n",
     "      sb.append(' ');\n",
     "  }\n",
     "  sb.append('^');\n",
     "  return sb.toString();\n",
     "}\n",
     "\n",
 	  "private String collectErrorString(int pos) {\n",
 		"  StringBuilder buf = new StringBuilder();\n",
     "  int start = pos >= in.length ? in.length-1 : pos;\n",
     "  for (; start >= 0; start--) {\n",
 		"    char c = in[start];\n",
 		"    if (eol(c))\n",
     "      break;\n",
     "  }\n",
     "  if (start < 0)\n",
     "    start = 0;\n",
     "  else if (start > 0)\n",
     "    start++;\n",
     "  for (int i = start; i < pos; i++)\n",
     "    buf.append(in[i]);\n",
 		"  for (int i = pos; i < inend; i++) {\n",
 		"    char c = in[i];\n",
 	  "    if (eol(c))\n",
     "      break;\n",
     "    else\n",
 		"      buf.append(c);\n",
 		"  }\n",
 		"  return buf.toString();\n",
     "}\n",
     "\n",
 		"private boolean succeed(Node rule) {\n",
 		"  rule.success = true;\n",
 		"  rule.length = inpos - rule.offset;\n",
 		"  rule.nextout = outpos;\n",
     "  if (lastFail != null && rule.offset >= lastFail.offset)\n",
     "    lastFail = null;\n",
 		"  return true;\n",
 		"}\n",
 		"\n",
 		"private boolean fail(Node rule, int outstart) {\n",
 		"  outpos = outstart;\n",
 		"  inpos = rule.offset;\n",
     "  if (lastFail == null || lastFail.offset < rule.offset)\n",
     "    lastFail = rule;\n",
 		"  return false;\n",
 		"}\n",
 		"\n",
 		"private boolean sameRule(String name) {\n",
 		"  ensureOut();\n",
 		"  Node node = out[outpos];\n",
 		"  if (node != null && node.name == name && node.offset == inpos) {\n",
 		"    outpos = node.nextout;\n",
 		"    inpos = node.offset + node.length;\n",
 		"    return true;\n",
 		"  }\n",
 		"  return false;\n",
 		"}\n",
 		"\n",
 		"private void ensureOut() {\n",
 		"  if (outpos == out.length) {\n",
 		"    Node[] tmp = new Node[outpos<<1];\n",
 		"    System.arraycopy(out,0,tmp,0,outpos);\n",
 		"    out = tmp;\n",
 		"  }\n",
 		"}\n",
 		"\n",
 		"private Node[] pack() {\n",
 		"  out = Node.pack(out, outpos);\n",
 		"  outpos = out.length;\n",
 		"  return out;\n",
 		"}\n",
 		"\n",
 		"private boolean matchAny() {\n",
 		"  if (inpos == inend) return false;\n",
 		"  inpos++;\n",
 		"  return true;\n",
 		"}\n",
 		"\n",
 		"private boolean matchSet(String set) {\n",
 		"  if (inpos == inend) return false;\n",
 		"  if (set.indexOf(in[inpos]) >= 0) {\n",
 		"    inpos++;\n",
 		"    return true;\n",
 		"  }\n",
 		"  return false;\n",
 		"}\n",
 		"\n",
 		"private boolean matchRange(String range) {\n",
 		"  if (inpos == inend) return false;\n",
 		"  char c = in[inpos];\n",
 		"  for (int i = 0, n = range.length(); inpos < inend && i < n; i += 2) {\n",
 		"    if (range.charAt(i) <= c && c <= range.charAt(i+1)) {\n",
 		"      inpos++;\n",
 		"      return true;\n",
 		"    }\n",
 		"  }\n",
 		"  return false;\n",
 		"}\n",
 		"\n",
 		"private boolean matchChar(char c) {\n",
 		"  if (inpos == inend || in[inpos] != c) return false;\n",
 		"  inpos++;\n",
 		"  return true;\n",
 		"}\n",
 		"\n",
 		"private boolean matchLiteral(String literal) {\n",
 		"  int inmark = inpos;\n",
 		"  int i = 0;\n",
 		"  int len = literal.length();\n",
 		"  while (inpos < inend && i < len && literal.charAt(i) == in[inpos]) {\n",
 		"    i++;\n",
 		"    inpos++;\n",
 		"  }\n",
 		"  if (i < literal.length()) {\n",
 		"    inpos = inmark;\n",
 		"    return false;\n",
 		"  }\n",
 		"  return true;\n",
 		"}\n",
 	};
 	
 	void visitChar(Node node) {
 		String rep = new String(in, node.offset, node.length);
 		cresult = PegUtil.decodeChar(rep);
 	}
 	
 	char testVisitChar(Node node, String input) {
 		in = input.toCharArray();
 		visitChar(node);
 		return cresult;
 	}
 }
 
