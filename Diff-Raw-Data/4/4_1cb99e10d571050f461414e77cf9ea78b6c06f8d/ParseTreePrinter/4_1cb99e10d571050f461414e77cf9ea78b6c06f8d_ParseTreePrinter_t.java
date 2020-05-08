 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2009 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 /***
  * ParseTreePrinter.java
  * Author: Oliver Steele, P T Withington, Don Anderson
  * Description: unparses the AST into Javascript.
  * Subclassed to handle Javascript variants.
  * This is a bottom up (depth-first) unparser.
  * To track line numbers from the source nodes, or
  * to separate the compilation units by classes,
  * 'annotations' are inserted into the result Strings,
  * which can be either stripped or used to collect
  * line number or class information.
  */
 
 package org.openlaszlo.sc;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.Pattern;
 import java.text.SimpleDateFormat;
 import java.text.DecimalFormat;
 
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.sc.parser.*;
 import org.openlaszlo.sc.Translator;
 import org.openlaszlo.sc.Compiler.Ops;
 import org.openlaszlo.sc.Compiler.PassThroughNode;
 
 // Values
 import org.openlaszlo.sc.Values;
 
 // Instructions
 import org.openlaszlo.sc.Instructions;
 import org.openlaszlo.sc.Instructions.Instruction;
 import org.openlaszlo.sc.InstructionPrinter;
 
 // This class supports the Javascript translator
 public class ParseTreePrinter {
 
   /** Configuration options for an instance of the ParseTreePrinter.
    * Nicer than a mess of constructor parameters.
    */
   public static class Config {
     boolean compress = false;
     public Config setCompress(boolean value) { compress = value; return this; }
 
     boolean obfuscate = false;
     public Config setObfuscate(boolean value) { obfuscate = value; return this; }
 
     boolean trackLines = false;
     public Config setTrackLines(boolean value) { trackLines = value; return this; }
 
     String dumpLineAnnotationsFile = null;
     public Config setDumpLineAnnotationsFile(String value) { dumpLineAnnotationsFile = value; return this; }
 
     // For debugging, for now, must be set by hand
     public static final boolean DEBUG_NODE_OUTPUT = false;
     public static final boolean DEBUG_LINE_NUMBER = false;
   }
   Config config;
 
   String SPACE;
   String NEWLINE;
   String COMMA;
   String COLON;
   String ASSIGN;
   String CONDITIONAL;
   String ALTERNATIVE;
   String OPENPAREN;
   String CLOSEPAREN;
   String SEMI;
   String OPTIONAL_SEMI;
   String OPENCURLY;
   String CLOSECURLY;
 
   private String currentClassName = null;
   
   public ParseTreePrinter() {
     this(new Config());
   }
   
   public ParseTreePrinter(boolean compress) {
     this(new Config().setCompress(compress));
   }
 
   public ParseTreePrinter(boolean compress, boolean obfuscate) {
     this(new Config().setCompress(compress).setObfuscate(obfuscate));
   }
 
   // TODO: [2007-11-21 dda] if compress/obfuscate are on, probably
   // can turn off generation of annotations.
   public ParseTreePrinter(Config config) {
     this.config = config;
 
     // Set whitespace
     this.SPACE = config.compress ? "" : " ";
     this.NEWLINE = config.obfuscate ? "" : "\n";
     // Set punctuation
     this.COMMA = "," + SPACE;
     this.COLON = ":" + SPACE;
     this.ASSIGN = SPACE + "=" + SPACE;
     this.CONDITIONAL = SPACE + "?" + SPACE;
     this.ALTERNATIVE = SPACE + ":" + SPACE;
     this.OPENPAREN = SPACE + "(";
     this.CLOSEPAREN = ")" + SPACE;
     this.SEMI = ";";
     this.OPTIONAL_SEMI = (config.compress && "\n".equals(NEWLINE)) ? NEWLINE : SEMI;
   }
   
   public void print(SimpleNode node) {
     print(node, System.out);
   }
   
   public void print(SimpleNode node, OutputStream output) {
     PrintStream where = new PrintStream(output);
     print(node, where);
   }
   
   public void print(SimpleNode node, PrintStream where) {
     where.println(visit(node));
   }
   
   public String delimit(String phrase, boolean force, boolean parenMultiline) {
     // Strip the phrase of annotations so we can look at the first char
     String plain = unannotate(phrase);
     if (plain.length() > 0) {
       boolean hasParen = '(' == plain.charAt(0);
       phrase = ((!hasParen && force)?" ":SPACE) + phrase;
       if (!hasParen && parenMultiline && plain.indexOf('\n') >= 0) {
         phrase = "(" + phrase + ")";
       }
     }
     return phrase;
   }
   
   public String delimit(String phrase) {
     return delimit(phrase, true, false);
   }
 
   public String delimitWithParen(String phrase) {
     return delimit(phrase, true, true);
   }
 
   public String elideSemi(String phrase) {
     // Strip the phrase of annotations so we can look at the ending
     if (unannotate(phrase).endsWith(SEMI)) {
       // We don't want to lose the annotations, none will contain SEMI though.
       int semipos = phrase.lastIndexOf(SEMI);
       return phrase.substring(0, semipos) + phrase.substring(semipos + SEMI.length());
     }
     return phrase;
   }
 
   public String makeBlock(String body) {
     body = elideSemi(body);
     // NEWLINE is for debug/readability, so our code is not _all_ on
     // one line
     return "{" + NEWLINE + elideSemi(body) + (unannotate(body).endsWith("}") ? "" : NEWLINE) + "}";
   }
 
   public static String join(String token, String[] strings) {
     StringBuffer sb = new StringBuffer();
     int l = strings.length - 1;
     for (int x = 0; x < l; x++) {
       sb.append(strings[x]);
       sb.append(token);
     }
     if (l >= 0) {
       sb.append(strings[l]);
     }
     return(sb.toString());
   }
 
   /**
    * Return a list of strings with any empty or null strings removed
    */
   public String[] removeEmptyStrings(String[] strings) {
     int len = strings.length;
     List list = new ArrayList();
     for (int i = 0; i < len; i++) {
       if (strings[i] != null && unannotate(strings[i]).trim().length() > 0) {
         list.add(strings[i]);
       }
     }
     return (String[])list.toArray(new String[0]);
   }
 
   /**
    * Perform any actions that need to happen before
    * children are visited.  May be overridden.
    */
   public SimpleNode previsit(SimpleNode node) {
     if (node instanceof PassThroughNode) {
       node = ((PassThroughNode)node).realNode;
     }
     else if (node instanceof ASTClassDefinition) {
       // classname is needed when emitting constructors for interstitials.
       currentClassName = ((ASTIdentifier)(node.getChildren()[1])).getName();
     }
     return node;
   }
 
   private String visit(SimpleNode node) {
     node = previsit(node);
     int size = node.size();
     SimpleNode[] childnodes = node.getChildren();
     String[] children = new String[size];
     for (int i = 0; i < size; i++) {
       SimpleNode n = childnodes[i];
       if (n instanceof PassThroughNode) {
         n = childnodes[i] = ((PassThroughNode)n).realNode;
       }
       children[i] = visit(n) ;
     }
     
     Class nt = node.getClass();
     
     // Are we doing OOP yet?
     if (node instanceof ASTProgram ||
         node instanceof ASTStatementList ||
         node instanceof ASTDirectiveBlock) {
       // Conditional join
       StringBuffer sb = new StringBuffer();
       String sep = "";
       int l = children.length;
       for (int x = 0; x < l; x++) {
         String child = children[x];
         // Elide empty nodes
         String childRaw = unannotate(child);
         if (! "".equals(childRaw)) {
           sb.append(sep);
           sb.append(child);
           if (! childRaw.endsWith(SEMI)) {
             sep = SEMI + (config.compress ? SPACE : NEWLINE);
           } else {
             sep = (config.compress ? SPACE : NEWLINE);
           }
         }
       }
       return(lnum(node, sb.toString()));
     }
     if (node instanceof ASTStatement) {
       assert children.length == 1;
       String child = children[0];
         String childRaw = unannotate(child);
       // Ensure an expression becomes a statement by appending an
       // explicit semicolon
       if ((! "".equals(childRaw)) &&
           (! childRaw.endsWith(SEMI))) {
         return lnum(node, child + SEMI);
       } else {
         return lnum(node, child);
       }
     }
     if (node instanceof ASTAssignmentExpression) {
       return lnum(node, visitAssignmentExpression(node, children));
     }
     if (node instanceof ASTCallExpression) {
       return lnum(node, visitCallExpression(node, children));
     }
     if (node instanceof ASTSuperCallExpression) {
       return lnum(node, visitSuperCallExpression(node, children));
     }
     if (node instanceof ASTConditionalExpression) {
       return lnum(node, visitConditionalExpression(node, children));
     }
     if (node instanceof ASTEmptyExpression) {
       return lnum(node, visitEmptyExpression(node, children));
     }
     if (node instanceof ASTForVarInStatement) {
       return lnum(node, visitForVarInStatement(node, children));
     }
     if (node instanceof ASTForInStatement) {
       return lnum(node, visitForInStatement(node, children));
     }
     if (node instanceof ASTForVarStatement || node instanceof ASTForStatement) {
       return lnum(node, visitForVarStatement(node, children));
     }
     if (node instanceof ASTNewExpression) {
       return lnum(node, visitNewExpression(node, children));
     }
     if (node instanceof ASTIfStatement || node instanceof ASTIfDirective) {
       return lnum(node, visitIfStatement(node, children));
     }
     if (node instanceof ASTPragmaDirective) {
       return lnum(node, visitPragmaDirective(node, children));
     }
     if (node instanceof ASTPassthroughDirective) {
       return lnum(node, visitPassthroughDirective(node, children));
     }
     if (node instanceof ASTPostfixExpression) {
       return lnum(node, visitPostfixExpression(node, children));
     }
     if (node instanceof ASTPropertyIdentifierReference) {
       return lnum(node, visitPropertyIdentifierReference(node, children));
     }
     if (node instanceof ASTPropertyValueReference) {
       return lnum(node, visitPropertyValueReference(node, children));
     }
     if (node instanceof ASTReturnStatement) {
       return lnum(node, visitReturnStatement(node, children));
     }
     if (node instanceof ASTThisReference) {
       return lnum(node, visitThisReference(node, children));
     }
     if (node instanceof ASTContinueStatement) {
       return lnum(node, visitContinueStatement(node, children));
     }
     if (node instanceof ASTBreakStatement) {
       return lnum(node, visitBreakStatement(node, children));
     }
     if (node instanceof ASTUnaryExpression) {
       return lnum(node, visitUnaryExpression(node, children));
     }
     if (node instanceof ASTWithStatement) {
       return lnum(node, visitWithStatement(node, children));
     }
     if (node instanceof ASTDoWhileStatement) {
       return lnum(node, visitDoWhileStatement(node, children));
     }
     if (node instanceof ASTWhileStatement) {
       return lnum(node, visitWhileStatement(node, children));
     }
     if (node instanceof ASTSwitchStatement) {
       return lnum(node, visitSwitchStatement(node, children));
     }
     if (node instanceof ASTCaseClause) {
       return lnum(node, visitCaseClause(node, children));
     }
     if (node instanceof ASTDefaultClause) {
       return lnum(node, visitDefaultClause(node, children));
     }
     if (node instanceof ASTArrayLiteral) {
       return lnum(node, visitArrayLiteral(node, children));
     }
     if (node instanceof ASTBinaryExpressionSequence) {
       return lnum(node, visitBinaryExpressionSequence(node, children));
     }
     if (node instanceof ASTExpressionList ||
         node instanceof ASTFunctionCallParameters) {
       return lnum(node, visitExpressionList(node, children));
     }
     if (node instanceof ASTFormalParameterList) {
       return lnum(node, visitFormalParameterList(node, children));
     }
     if (node instanceof ASTAndExpressionSequence) {
       return lnum(node, visitAndOrExpressionSequence(true, node, children));
     }
     if (node instanceof ASTOrExpressionSequence) {
       return lnum(node, visitAndOrExpressionSequence(false, node, children));
     }
     if (node instanceof ASTFunctionDeclaration) {
       return lnum(node, visitFunctionDeclaration(node, children));
     }
     if (node instanceof ASTFunctionExpression) {
       return lnum(node, visitFunctionExpression(node, children));
     }
     if (node instanceof ASTIdentifier) {
       return lnum(node, visitIdentifier(node, children));
     }
     if (node instanceof ASTLiteral) {
       return lnum(node, visitLiteral(node, children));
     }
     if (node instanceof ASTLabeledStatement) {
       return lnum(node, visitLabeledStatement(node, children));
     }
     if (node instanceof ASTObjectLiteral) {
       return lnum(node, visitObjectLiteral(node, children));
     }
     if (node instanceof ASTOperator) {
       return lnum(node, visitOperator(node, children));
     }
     if (node instanceof ASTVariableStatement) {
       return visitVariableStatement(node, children);
     }
     if (node instanceof ASTVariableDeclaration) {
       return lnum(node, visitVariableDeclaration(node, children));
     }
     if (node instanceof ASTVariableDeclarationList) {
       return visitVariableDeclarationList(node, children);
     }
     if (node instanceof ASTTryStatement) {
       return lnum(node, visitTryStatement(node, children));
     }
     if (node instanceof ASTCatchClause) {
       return lnum(node, visitCatchClause(node, children));
     }
     if (node instanceof ASTFinallyClause) {
       return lnum(node, visitFinallyClause(node, children));
     }
     if (node instanceof ASTThrowStatement) {
       return lnum(node, visitThrowStatement(node, children));
     }
     if (node instanceof ASTClassDefinition) {
       return lnum(node, visitClassDefinition(node, children));
     }
     if (node instanceof ASTModifiedDefinition) {
       return lnum(node, visitModifiedDefinition(node, children));
     }
     if (node instanceof ASTFormalInitializer) {
       return lnum(node, visitFormalInitializer(node, children));
     }
     return lnum(node, defaultVisitor(node, children));
   }
   
   public String defaultVisitor(SimpleNode node, String[] children) {
     throw new CompilerError("Don't know how to unparse: \u00AB" + node.toString() + "(" + join(COMMA, children) + ")\u00BB");
   }
   
   // Copied (and massaged) from Parser.jjt
   public static String[] OperatorNames = {};
   static {
     ArrayList on = new ArrayList();
     // TODO: [2005-11-17 ptw] Not quite right, but javacc doesn't
     // tell us the range of its Ops
     for (int i = 0; i < 256; i++) { on.add("<" + Integer.toString(i) + ">"); }
     on.set(Ops.LPAREN, "(");
     on.set(Ops.LBRACKET, "[");
     on.set(Ops.DOT, ".");
     on.set(Ops.ASSIGN, "=");
     on.set(Ops.COMMA, ",");
     on.set(Ops.GT, ">");
     on.set(Ops.LT, "<");
     on.set(Ops.BANG, "!");
     on.set(Ops.TILDE, "~");
     on.set(Ops.HOOK, "?");
     on.set(Ops.COLON, ":");
     on.set(Ops.EQ, "==");
     on.set(Ops.LE, "<=");
     on.set(Ops.GE, ">=");
     on.set(Ops.NE, "!=");
     on.set(Ops.SEQ, "===");
     on.set(Ops.SNE, "!==");
     on.set(Ops.SC_OR, "||");
     on.set(Ops.SC_AND, "&&");
     on.set(Ops.INCR, "++");
     on.set(Ops.DECR, "--");
     on.set(Ops.PLUS, "+");
     on.set(Ops.MINUS, "-");
     on.set(Ops.STAR, "*");
     on.set(Ops.SLASH, "/");
     on.set(Ops.BIT_AND, "&");
     on.set(Ops.BIT_OR, "|");
     on.set(Ops.XOR, "^");
     on.set(Ops.REM, "%");
     on.set(Ops.LSHIFT, "<<");
     on.set(Ops.RSIGNEDSHIFT, ">>");
     on.set(Ops.RUNSIGNEDSHIFT, ">>>");
     on.set(Ops.PLUSASSIGN, "+=");
     on.set(Ops.MINUSASSIGN, "-=");
     on.set(Ops.STARASSIGN, "*=");
     on.set(Ops.SLASHASSIGN, "/=");
     on.set(Ops.ANDASSIGN, "&=");
     on.set(Ops.ORASSIGN, "|=");
     on.set(Ops.XORASSIGN, "^=");
     on.set(Ops.REMASSIGN, "%=");
     on.set(Ops.LSHIFTASSIGN, "<<=");
     on.set(Ops.RSIGNEDSHIFTASSIGN, ">>=");
     on.set(Ops.RUNSIGNEDSHIFTASSIGN, ">>>=");
     
     on.set(Ops.IN, "in");
     on.set(Ops.INSTANCEOF, "instanceof");
     on.set(Ops.TYPEOF, "typeof");
     on.set(Ops.DELETE, "delete");
     on.set(Ops.VOID, "void");
     on.set(Ops.NEW, "new");
     on.set(Ops.CAST, "cast");
     on.set(Ops.IS, "is");
     
     OperatorNames = (String[])on.toArray(OperatorNames);
   }
   
   public String visitAssignmentExpression(SimpleNode node, String[] children) {
     int thisPrec = prec(((ASTOperator)node.get(1)).getOperator(), false);
     assert children.length == 3;
     children[2] = maybeAddParens(thisPrec, node.get(2), children[2], true);
     return children[0] + SPACE + children[1] + delimit(children [2], false, false);
   }
   public String visitCallExpression(SimpleNode node, String[] children) {
     int thisPrec = prec(Ops.LPAREN, true);
     children[0] = maybeAddParens(thisPrec, node.get(0), children[0], true);
     return children[0] + "(" + children[1] + ")";
   }
   public String visitSuperCallExpression(SimpleNode node, String[] children) {
     // Same as above
     return "super" +
       (node.get(0) instanceof ASTEmptyExpression?"":("." + children[0])) +
       (node.get(1) instanceof ASTEmptyExpression?"":("." + children[1]))  +
       "(" + children[2] + ")";
   }
   public String visitConditionalExpression(SimpleNode node, String[] children) {
     int thisPrec = prec(Ops.COLON, false);
     for (int i = 0; i < children.length; i++) {
       children[i] = maybeAddParens(thisPrec, node.get(i), children[i]);
     }
     return children[0] + CONDITIONAL + children[1] + ALTERNATIVE + children[2];
   }
   public String visitEmptyExpression(SimpleNode node, String[] children) {
     return "";
   }
   public String visitForVarInStatement(SimpleNode node, String[] children) {
     return "for" + OPENPAREN + "var " + children[0] + " in " + children[2] + CLOSEPAREN + makeBlock(children[3]);
   }
   public String visitForInStatement(SimpleNode node, String[] children) {
     return "for" + OPENPAREN + children[0] + " in " + children[1] + CLOSEPAREN + makeBlock(children[2]);
   }
   public String visitForVarStatement(SimpleNode node, String[] children) {
     // Need explicit semi because init clause may be empty
     return "for" + OPENPAREN + elideSemi(children[0]) + SEMI + children[1] + SEMI + children[2] + CLOSEPAREN + makeBlock(children[3]);
   }
   public String visitIfStatement(SimpleNode node, String[] children) {
     if (children.length == 2) {
       return "if" + OPENPAREN + children[0] + CLOSEPAREN + makeBlock(children[1]);
     } else if (children.length == 3) {
       return "if" + OPENPAREN + children[0] + CLOSEPAREN + makeBlock(children[1]) +
         SPACE + "else" + SPACE + makeBlock(children[2]);
     }
     return defaultVisitor(node, children);
   }
   public String visitNewExpression(SimpleNode node, String[] children) {
     int thisPrec = prec(Ops.NEW, true);
     SimpleNode c = node.get(0);
     children[0] = maybeAddParens(thisPrec, c, children[0]);
     return "new " + children[0] + "(" + children[1] + ")";
   }
   public String visitPragmaDirective(SimpleNode node, String[] children) {
     return "#pragma " + children[0];
   }
   public String visitPassthroughDirective(SimpleNode node, String[] children) {
     return ((ASTPassthroughDirective)node).getText();
   }
   public String visitPostfixExpression(SimpleNode node, String[] children) {
     int op = ((ASTOperator)node.get(1)).getOperator();
     int thisPrec = prec(op, true);
     children[0] = maybeAddParens(thisPrec, node.get(0), children[0]);
     return children[0] + children[1];
   }
   public String visitPropertyIdentifierReference(SimpleNode node, String[] children) {
     // These have prec of 0 even though they don't have ops
     int thisPrec = 0;
     for (int i = 0; i < children.length; i++) {
       children[i] = maybeAddParens(thisPrec, node.get(i), children[i], true);
     }
     return children[0] + "." + children[1];
   }
   public String visitPropertyValueReference(SimpleNode node, String[] children) {
     // These have prec of 0 even though they don't have ops
     int thisPrec = 0;
     children[0] = maybeAddParens(thisPrec, node.get(0), children[0], true);
     return children[0] + "[" + children[1] + "]";
   }
   public String visitReturnStatement(SimpleNode node, String[] children) {
     return "return" + delimitWithParen(children[0]);
   }
   public String visitThisReference(SimpleNode node, String[] children) {
     return "this";
   }
   public String visitContinueStatement(SimpleNode node, String[] children) {
     return "continue" + (children.length > 0 ? delimit(children[0]) : "");
   }
   public String visitBreakStatement(SimpleNode node, String[] children) {
     return "break" + (children.length > 0 ? delimit(children[0]) : "");
   }
   public String visitLabeledStatement(SimpleNode node, String[] children) {
     return children[0] + ":" + delimit(children[1]);
   }
   public String visitUnaryExpression(SimpleNode node, String[] children) {
     // Prefix and Unary are the same node
     int op = ((ASTOperator)node.get(0)).getOperator();
     boolean letter = java.lang.Character.isLetter(OperatorNames[op].charAt(0));
     int thisPrec = prec(op, true);
     children[1] = maybeAddParens(thisPrec, node.get(1), children[1]);
     return children[0] + (letter ? " " : "") + children[1];
   }
   public String visitWithStatement(SimpleNode node, String[] children) {
     return "with" + OPENPAREN + children[0] + CLOSEPAREN + makeBlock(children[1]);
   }
   public String visitWhileStatement(SimpleNode node, String[] children) {
     return "while" + OPENPAREN + children[0] + CLOSEPAREN + makeBlock(children[1]);
   }
   public String visitDoWhileStatement(SimpleNode node, String[] children) {
     return "do" + makeBlock(children[0]) + SPACE + "while" + OPENPAREN + children[1] + ")";
   }
   
   public String visitDefaultClause(SimpleNode node, String[] children) {
     return "default:" + NEWLINE + (children.length > 0 ? (children[0] + OPTIONAL_SEMI) : "");
   }
   public String visitCaseClause(SimpleNode node, String[] children) {
     return "case" + delimit(children[0]) + ":" + NEWLINE +
       (children.length > 1 ? (children[1] + OPTIONAL_SEMI) : "");
   }
   public String visitSwitchStatement(SimpleNode node, String[] children) {
     String body = "";
     for (int i = 1, len = children.length; i < len; i++) {
       body += children[i];
     }
     return "switch" + OPENPAREN + children[0] + CLOSEPAREN + makeBlock(body);
   }
   
   
   // TODO: [2005-11-15 ptw] Make this a simple lookup table based on
   // the operator
   public int prec(int op, boolean unary) {
     String n = OperatorNames[op];
     String classes[][] = {
       {"(", "[", ".", "new"},
       {"!", "~", "-", "+", "--", "++", "typeof", "void", "delete"},
       {"*", "/", "%"},
       {"+", "-"},
       {"<<", ">>", ">>>"},
       // TODO: [2007-12-13 dda] "in" moved below to compensate for SWF9 3rd party compiler precedence bug.
       //{"<", "<=", ">", ">=", "instanceof", "in", "is", "cast"},
       // TODO: [2008-03-27 ptw] Flex compiler wants "cast" to be named "as"
       {"<", "<=", ">", ">=", "instanceof", "is", "cast", "as"},
       {"==", "!=", "===", "!=="},
       {"&"}, {"^"}, {"|"}, {"&&"}, {"||"}, {"?", ":"},
       {"in", "=", "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", ">>>=", "&=", "^=", "|="},
       {","}};
     for (int i = (unary ? 0 : 2), il = classes.length; i < il; i++) {
       for (int j = 0, jl = classes[i].length; j <  jl; j++) {
         if (classes[i][j].equals(n)) {
           return -i;
         }
       }
     }
   assert false : "unknown operator: " + n;
     return 1;
   }
   
   public String visitArrayLiteral(SimpleNode node, String[] children) {
     int thisPrec = prec(Ops.COMMA, false);
     for (int i = 0; i < children.length; i++) {
       children[i] = maybeAddParens(thisPrec, node.get(i), children[i], false);
     }
     return "[" + join(COMMA, children) + "]";
   }
   
   public String maybeAddParens(int parentPrec, SimpleNode node, String nodeRep) {
     return maybeAddParens(parentPrec, node, nodeRep, false);
   }
   
   // Set assoc to true if the sub-expression appears in a place
   // where operator associativity implies the parens, e.g. on the
   // left operand of a binary operator that is left-to-right
   // associative.  (It is always safe to leave it false, you will
   // just end up with extra parens where you don't need them, which
   // will impact compression but not correctness.)
   public String maybeAddParens(int parentPrec, SimpleNode node, String nodeRep, boolean assoc) {
     int thisPrec = Integer.MAX_VALUE;
     if (node instanceof ASTBinaryExpressionSequence ||
         node instanceof ASTAssignmentExpression) {
       thisPrec = prec(((ASTOperator)node.get(1)).getOperator(), false);
     } else if (node instanceof ASTUnaryExpression) {
       thisPrec = prec(((ASTOperator)node.get(0)).getOperator(), true);
     } else if (node instanceof ASTPostfixExpression) {
       thisPrec = prec(((ASTOperator)node.get(1)).getOperator(), true);
     } else if (node instanceof ASTAndExpressionSequence) {
       thisPrec = prec(Ops.SC_AND, false);
     } else if (node instanceof ASTOrExpressionSequence) {
       thisPrec = prec(Ops.SC_OR, false);
     } else if (node instanceof ASTConditionalExpression) {
       thisPrec = prec(Ops.COLON, false);
     } else if (node instanceof ASTNewExpression) {
       thisPrec = prec(Ops.NEW, true);
     } else if (node instanceof ASTCallExpression ||
                node instanceof ASTSuperCallExpression) {
       thisPrec = prec(Ops.LPAREN, true);
     } else if (node instanceof ASTPropertyValueReference) {
       thisPrec = prec(Ops.LBRACKET, true);
     } else if (node instanceof ASTPropertyIdentifierReference) {
       thisPrec = prec(Ops.DOT, true);
     } else if (node instanceof ASTExpressionList) {
       thisPrec = prec(Ops.COMMA, false);
     } else if (// Our compiler is broken -- if one of these shows up
                // in an expression, it had to have been in an
                // expression list initially
                node instanceof ASTFunctionExpression ||
                node instanceof ASTFunctionDeclaration) {
       thisPrec = prec(Ops.ASSIGN, false);
     } else if (node instanceof ASTObjectLiteral ||
                node instanceof ASTArrayLiteral ||
                node instanceof ASTIdentifier ||
                node instanceof ASTThisReference ||
                node instanceof ASTLiteral) {
       ;
     } else {
       System.err.println("No prec for " + node + " in " + Compiler.nodeString(node));
       (new CompilerException()).printStackTrace();
     }
     
     if (assoc ? (thisPrec < parentPrec) : (thisPrec <= parentPrec)) {
       nodeRep = "(" + nodeRep + ")";
     }
     return nodeRep;
   }
   
   public String visitAndOrExpressionSequence(boolean isAnd, SimpleNode node, String[] children) {
     int thisPrec = prec(isAnd ? Ops.SC_AND : Ops.SC_OR, false);
     children[0] = maybeAddParens(thisPrec, node.get(0), children[0], true);
     for (int i = 1; i < children.length; i++) {
       children[i] = delimit(maybeAddParens(thisPrec, node.get(i), children[i]), false, false);
     }
     return join(isAnd ? (SPACE + "&&") : (SPACE + "||"), children);
   }
   
   public String visitExpressionList(SimpleNode node, String[] children) {
     int thisPrec = prec(Ops.COMMA, false);
     for (int i = 0; i < children.length; i++) {
       children[i] = maybeAddParens(thisPrec, node.get(i), children[i]);
     }
     return join(COMMA, children);
   }
   
   public String visitFormalParameterList(SimpleNode node, String[] children) {
     int thisPrec = prec(Ops.COMMA, false);
     // TODO: [2007-12-21 dda] FormalInitializer should be a child of identifier
     // to help fix this oddity.
     StringBuffer sb = new StringBuffer();
     for (int i = 0; i < children.length; i++) {
       if (i > 0 && !unannotate(children[i]).startsWith(ASSIGN)) {
         sb.append(COMMA);
       }
       sb.append(children[i]);
     }
     return sb.toString();
   }
   
   public String visitBinaryExpressionSequence(SimpleNode node, String[] children) {
     int thisPrec = prec(((ASTOperator)node.get(1)).getOperator(), false);
     for (int i = 0; i < children.length; i += (i==0?2:1)) {
       children[i] = maybeAddParens(thisPrec, node.get(i), children[i], i == 0);
     }
     
     String op = children[1];
     String opRaw = unannotate(op);
     char opChar = opRaw.charAt(opRaw.length() - 1);
     StringBuffer sb = new StringBuffer();
     boolean required = java.lang.Character.isLetter(opRaw.charAt(0));
     String space = required?" ":SPACE;
     sb.append(children[0]);
     for (int x = 2; x < (children.length); x++) {
       String child = children[x];
       sb.append(space);
       sb.append(op);
       // Disambiguate `a + ++b`, `a++ + b` etc.
       sb.append(delimit(child, required || opChar == unannotate(child).charAt(0), false));
     }
     return(sb.toString());
   }
   
   // This is overridden for SWF9
   public String visitModifiedDefinition(SimpleNode node, String[] children) {
     // In JavascriptGenerator 'static' is handled elsewhere.
     return children[0];
   }
   
   public String visitFormalInitializer(SimpleNode node, String[] children) {
     return ASSIGN + children[0];
   }
   
   public String visitFunctionDeclaration(SimpleNode node, String[] children) {
     return doFunctionDeclaration(node, children, true, false);
   }
   
   public String visitFunctionExpression(SimpleNode node, String[] children) {
     // Elide optional name if compressing, otherwise leave it for debugging
     return doFunctionDeclaration(node, children, config.compress ? false : true, false);
   }
   
   String doFunctionDeclaration(SimpleNode node, String[] children, boolean useName, boolean inmixin) {
     String name, args, body;
     if (children.length == 2) {
       name = "";
       args = children[0];
       body = children[1];
     } else if (children.length == 3) {
       name = children[0];
       args = children[1];
       body = children[2];
     } else {
       return defaultVisitor(node, children);
     }
     String txt = "function" + (useName ? (" " + name) : "") + OPENPAREN + args + CLOSEPAREN;
     txt += functionReturnType(node);
     if (!inmixin) {
       txt += makeBlock(body);
     }
     else {
       // This is an interface - no body needed
       txt += SEMI;
     }
     // When functions go out of scope we should tell
     // any readers to forget the current line number info.
     return txt + forceBlankLnum();
   }
 
   // By default, return types are ignored
   public String functionReturnType(SimpleNode node) {
     return "";
   }
 
   public String visitClassDefinition(SimpleNode node, String[] children) {
     // Should never be called for plain Javascript, these are stripped out
     throw new CompilerException("ClassDefinition found in printing Javascript AST");
   }
   
   public String visitIdentifier(SimpleNode node, String[] children) {
     ASTIdentifier id = (ASTIdentifier)node;
     if (id.isConstructor()) {
       return currentClassName;
     } else {
       return id.getName();
     }
   }
   
   static Double zero = new Double(0);
   
   public String visitLiteral(SimpleNode node, String [] children) {
     Object value = ((ASTLiteral)node).getValue();
     if (value instanceof String) {
       return ScriptCompiler.quote((String)value);
     }
     if (value instanceof Double) {
       // Make integers compact
       Double n = (Double)value;
       long l = n.longValue();
       if ((double)l == n.doubleValue()) {
         if (l == 0 ) {return "0";}
         else {
           String d = Long.toString(l);
           if (config.compress && l > 0) {
             String h = "0x" + Long.toHexString(l);
             if (h.length() <= d.length()) {
               return h;
             }
           }
           return d;
         }
       }
     }
     return "" + value;
   }
   
   public String visitObjectLiteral(SimpleNode node, String[] children) {
     StringBuffer s = new StringBuffer("{");
     int len = children.length - 1;
     int thisPrec = prec(Ops.COMMA, false);
     for (int i = 0; i < len; i++) {
       if (i % 2 != 0) {
         children[i] = maybeAddParens(thisPrec, node.get(i), children[i], false);
         s.append(children[i]);
         s.append(COMMA);
       } else {
         s.append(children[i]);
         s.append(COLON);
       }
     }
     if (len > 0) {
       children[len] = maybeAddParens(thisPrec, node.get(len), children[len], false);
       s.append(children[len]);
     }
     s.append("}");
     return s.toString();
   }
   
   public String visitOperator(SimpleNode op, String[] children) {
     int operator = ((ASTOperator)op).getOperator();
     return OperatorNames[operator];
   }
   
   public String visitVariableStatement(SimpleNode node, String[] children) {
     assert children.length == 1;
     // Ensure an expression becomes a statement by appending an
     // explicit semicolon
     return "var " + children[0] + SEMI;
   }
 
   public String visitVariableDeclaration(SimpleNode node, String[] children) {
     if (children.length > 1) {
       int thisPrec = prec(Ops.ASSIGN, false);
       assert children.length == 2;
       children[1] = maybeAddParens(thisPrec, node.get(1), children[1], true);
       return children[0] + ASSIGN + children[1];
     } else {
       return children[0];
     }
   }
   
   public String visitVariableDeclarationList(SimpleNode node, String[] children) {
     // As declarations are processed by the generator, they
     // are sometimes replaced by ASTEmptyExpressions.
     // The resulting empty strings need to be removed
     // to avoid emitting adjacent commas.
     return join(COMMA, removeEmptyStrings(children));
   }
 
   public String visitTryStatement(SimpleNode node, String[] children) {
     if (children.length == 2) {
       return "try" + SPACE + makeBlock(children[0]) + NEWLINE + children[1];
     } else if (children.length == 3) {
       return "try" + SPACE + makeBlock(children[0]) + NEWLINE + children[1] + NEWLINE + children[2];
     }
     return defaultVisitor(node, children);
   }
   public String visitCatchClause(SimpleNode node, String[] children) {
     return "catch" + OPENPAREN + children[0] + CLOSEPAREN + makeBlock(children[1]);
   }
   public String visitFinallyClause(SimpleNode node, String[] children) {
     return "finally" + SPACE + makeBlock(children[0]);
   }
   public String visitThrowStatement(SimpleNode node, String[] children) {
     return "throw" + delimit(children[0]);
   }
   
   public List makeTranslationUnits(SimpleNode node, SourceFileMap sources) {
     return makeTranslationUnits(visit(node), sources);
   }
 
   public static String unparse(SimpleNode node) {
     return (new ParseTreePrinter()).text(node);
   }
   
   public String text(SimpleNode node) {
     return unannotate(visit(node));
   }
   
   public String annotatedText(SimpleNode node) {
     return printableAnnotations(visit(node));
   }
   
   public static final char ANNOTATE_MARKER = '\u0001';
   // note: number codes are reserved for annotation to streams
   public static final char ANNOTATE_OP_CLASSNAME = 'C';
   public static final char ANNOTATE_OP_CLASSEND = 'c';
   public static final char ANNOTATE_OP_INSERTSTREAM = 'i';
   public static final char ANNOTATE_OP_FILE_LINENUM = 'f';
   public static final char ANNOTATE_OP_FILE_LINENUM_FORCE = 'F';
   public static final char ANNOTATE_OP_NODENAME = 'N';
   public static final char ANNOTATE_OP_NODEEND = 'n';
   public static final char ANNOTATE_OP_TEXT = 'T';
   
   public static class DecodedAnnotation {
     char op;
     String operand;
   }
 
   /**
    * Return the first annotation only.
    */
   public DecodedAnnotation firstAnnotation(String str) {
     final DecodedAnnotation ann = new DecodedAnnotation();
     AnnotationProcessor ap = new AnnotationProcessor() {
         public String notify(char op, String operand) {
           switch (op) {
           case ANNOTATE_OP_TEXT:
             break;
           default:
             ann.op = op;
             ann.operand = operand;
             break;
           }
           return "";
         }
       };
     // Only look at the first annotation
     ap.setLimit(1);
     ap.process(str);
     if (ann.operand == null)
       return null;
     else
       return ann;
   }
 
   /**
    * Return true if the node gives new useful line information
    * beyond what is in the annotation.
    */
   public boolean fileLineNumberNeeded(DecodedAnnotation ann, SimpleNode node) {
     if (ann == null ||
         (ann.op != ANNOTATE_OP_FILE_LINENUM &&
          ann.op != ANNOTATE_OP_FILE_LINENUM_FORCE)) {
       return false;
     }
     int nodeLinenum = node.getLineNumber();
     String nodeFilename = node.getFilename();
     int annLinenum = extractLineNumber(ann.operand);
     String annFilename = extractFileName(ann.operand);
 
     if (nodeFilename == null || nodeFilename.length() == 0) {
       nodeFilename = "";
       nodeLinenum = 0;
     }
     if (annFilename == null || annFilename.length() == 0) {
       annFilename = "";
       annLinenum = 0;
     }
     return (!nodeFilename.startsWith("[") &&
             (!annFilename.equals(nodeFilename) || annLinenum != nodeLinenum));
   }
 
   /**
    * Prefix line number annotation to a string.
    */
   public String lnum(SimpleNode node, String str) {
     if (!config.trackLines)
       return str;
 
     // If we are not already at an annotation at the same file/line number,
     // then produce one.
     String result = "";
     if (str.length() <= 1 || str.charAt(0) != ANNOTATE_MARKER ||
         fileLineNumberNeeded(firstAnnotation(str), node)) {
       // TODO: [2008-05-18 dda] If there is already a line annotation
       // here, consider replacing it, rather than adding to it so we
       // don't balloon the size of the annotated string.  But for now,
       // we wish to push the real line number intelligence to the end
       // of processing.
       result = annotateFileLineNumber(Compiler.getLocationString(node), false);
     }
     result += str;
 
     if (Config.DEBUG_NODE_OUTPUT)
       result = annotateNode(node, result);
 
     return result;
   }
 
   /**
    * Return an annotation that forces a blank line number
    * at this point.
    */
   public String forceBlankLnum() {
     if (!config.trackLines) {
       return "";
     } else {
       return NEWLINE + annotateFileLineNumber("", true);
     }
   }
 
   public String annotateClass(String classnm, String str) {
     StringBuffer sb = new StringBuffer();
     sb.append(makeAnnotation(ANNOTATE_OP_CLASSNAME, classnm));
     sb.append(str);
     sb.append(makeAnnotation(ANNOTATE_OP_CLASSEND, ""));
     return sb.toString();
   }
 
   // This is only useful for debugging - we can annotate
   // the output with information about the node that created it
   public String annotateNode(SimpleNode node, String str) {
     String nodenm = node.getClass().getName();
     int dot;
     if ((dot = nodenm.lastIndexOf('.')) >= 0)
       nodenm = nodenm.substring(dot+1);
     StringBuffer sb = new StringBuffer();
     sb.append(makeAnnotation(ANNOTATE_OP_NODENAME, nodenm));
     sb.append(str);
     sb.append(makeAnnotation(ANNOTATE_OP_NODEEND, ""));
     return sb.toString();
   }
 
   // The text is directed to a numbered stream.  0 should be avoided,
   // we may use it as a synonym for the 'default' text stream.
   public String annotateStream(int streamNum, String str) {
     assert (streamNum >= 0 && streamNum <= 9);
     return makeAnnotation((char)('0' + streamNum), str);
   }
 
   // This marker will be replaced by the contents of the designated stream.
   public String annotateInsertStream(int streamNum) {
     StringBuffer sb = new StringBuffer();
     sb.append(makeAnnotation(ANNOTATE_OP_INSERTSTREAM, String.valueOf(streamNum)));
     return sb.toString();
   }
 
   // TODO: [2008-05-18 dda] line number annotations contain the full
   // text of the file name plus a line number.  This can be wasteful
   // in string space (it is not currently possible to compile the full
   // LFC with trackLines on).  A better approach would be to use a
   // number in place of the file name, the number being an index in a
   // dictionary of names, kept in the instance of the
   // ParseTreePrinter.
   //
   public String annotateFileLineNumber(String fileLineNumber, boolean force
 ) {
     // TODO: [2008-05-21 dda] FORCE no longer needed, it
     // and all it implies could be removed
 
     char op = force ? ANNOTATE_OP_FILE_LINENUM_FORCE : ANNOTATE_OP_FILE_LINENUM;
     return makeAnnotation(op, fileLineNumber);
   }
 
   /**
    * Add annotations, which look like \u0001 opchar operand \u0001
    * opchar is a single character.
    */
   public String makeAnnotation(char op, String operand) {
     return String.valueOf(ANNOTATE_MARKER) + op + operand + ANNOTATE_MARKER;
   }
   
   public abstract static class AnnotationProcessor {
     int limit = -1;
     public abstract String notify(char op, String operand);
     /** Limit the number of annotations to process, -1 means no limit */
     public void setLimit(int limit) {
       assert(limit != 0);
       this.limit = limit;
     }
     public String process(String annotated) {
       int alen = annotated.length();
       StringBuffer sb = new StringBuffer();
       int endann = -1;
       int startann = annotated.indexOf(ANNOTATE_MARKER);
       while (startann >= 0) {
         String outstr = annotated.substring(endann+1, startann);
         sb.append(outstr);
         notify(ANNOTATE_OP_TEXT, outstr);
         // The minimum annotation has three chars: marker, op, marker
         if (alen < startann + 3) {
           throw new IllegalArgumentException("missing annotation marker");
         }
         char op = annotated.charAt(startann+1);
         endann = annotated.indexOf(ANNOTATE_MARKER, startann+2);
         if (endann < 0) {
           // unbalanced annotations
           throw new IllegalArgumentException("bad line number annotations:");
         }
         sb.append(notify(op, annotated.substring(startann+2, endann)));
         if (limit >= 0) {
           if (--limit <= 0) {
             return "";
           }
         }
         startann = annotated.indexOf(ANNOTATE_MARKER, endann+1);
       }
       String outstr = annotated.substring(endann+1);
       sb.append(outstr);
       notify(ANNOTATE_OP_TEXT, outstr);
       return sb.toString();
     }
   }
     
   public String unannotate(String annotated) {
     AnnotationProcessor ap = new AnnotationProcessor() {
         public String notify(char op, String operand) {
           switch (op) {
           case ANNOTATE_OP_TEXT:
             return operand;
           }
           return "";
         }
       };
     return ap.process(annotated);
   }
 
   public String printableAnnotations(String annotated) {
     final LinkedList nodestack = new LinkedList();
     AnnotationProcessor ap = new AnnotationProcessor() {
         public String notify(char op, String operand) {
           switch (op) {
           case ANNOTATE_OP_TEXT:
             return operand;
           case ANNOTATE_OP_FILE_LINENUM:
             return "#fileline " + operand + ": ";
           case ANNOTATE_OP_FILE_LINENUM_FORCE:
             return "#filelineforce " + operand + ": ";
           case ANNOTATE_OP_CLASSNAME:
             return "#class " + operand + ": ";
           case ANNOTATE_OP_CLASSEND:
             return "#endclass";
           case ANNOTATE_OP_INSERTSTREAM:
             return "#insertstream " + operand + ": ";
           case ANNOTATE_OP_NODENAME:
             nodestack.addLast(operand);
             return "#node " + operand + ": ";
           case ANNOTATE_OP_NODEEND:
             String nodenm = (String)nodestack.removeLast();
             return "#endnode " + nodenm;
           case '0': case '1': case '2': case '3': case '4':
           case '5': case '6': case '7': case '8': case '9':
             return "#stream " + op + ": " + operand;
           }
           return "";
         }
       };
     return ap.process(annotated);
   }
 
   public int extractLineNumber(String str) {
     int linenumPos = str.indexOf('#');
     int linenumEnd = str.indexOf('.', linenumPos+1);
     if (linenumPos < 0) {
       return 0;
     }
     if (linenumEnd < 0) {
       linenumEnd = str.length();
     }
     return Integer.parseInt(str.substring(linenumPos+1, linenumEnd));
   }
 
   public String extractFileName(String str) {
     int linenumPos = str.indexOf('#');
     if (linenumPos >= 0) {
       return str.substring(0, str.indexOf('#'));
     }
     else {
       return str;
     }
   }
 
   public static class LineNumberState {
     String filename = "";
     boolean hasfile = false;
     int linenum = Integer.MIN_VALUE;
     int linediff = Integer.MIN_VALUE;
     public String toString() {
       return "LineNumberState(\"" + filename + "\", hasfile=" + hasfile + ", line=" + linenum + ", diff=" + linediff + ")";
     }
   }
 
   public boolean isActualFile(String str) {
     // TODO: handle Compiler. etc.
     return (!str.equals("") && !str.startsWith("["));
   }
 
   public LineNumberState getLineNumberState(TranslationUnit tu, String operand) {
     LineNumberState lnstate = new LineNumberState();
     lnstate.filename = extractFileName(operand);
     lnstate.hasfile = isActualFile(lnstate.filename);
     if (lnstate.hasfile) {
       lnstate.linenum = extractLineNumber(operand);
       lnstate.linediff = tu.getTextLineNumber() - lnstate.linenum;
     }
     return lnstate;
   }
 
   public List makeTranslationUnits(String annotated, final SourceFileMap sources) {
     if (config.dumpLineAnnotationsFile != null) {
      String newname = Compiler.emitFile(config.dumpLineAnnotationsFile, printableAnnotations(annotated));
      System.err.println("Created " + newname);
     }
     if (Config.DEBUG_NODE_OUTPUT) {
       System.out.println("ANNOTATED OUTPUT:\n" + printableAnnotations(annotated));
     }
 
     final ArrayList tunits = new ArrayList();
     final TranslationUnit defaulttu = new TranslationUnit(true);
 
     tunits.add(defaulttu);
 
     AnnotationProcessor ap = new AnnotationProcessor() {
         TranslationUnit curtu = defaulttu;
         boolean atBol = true;
         LineNumberState curLstate = new LineNumberState();
 
         /** source locations are recorded to show error messages.
          * We want to err on the side of recording more, not less.
          */
         public boolean shouldRecordSourceLocation(LineNumberState state) {
           return (state.hasfile && state.linenum != Integer.MIN_VALUE);
         }
 
         /** source locations that are shown are used by the debugger.
          */
         public boolean shouldShowSourceLocation(LineNumberState os,
                                                 LineNumberState ns,
                                                 char op,
                                                 boolean atBol) {
           boolean fileSame = os.filename.equals(ns.filename);
           boolean lineSame = (os.linediff == ns.linediff);
 
           boolean showSrcloc = false;
 
           // Show source location if we are tracing linenums and the
           // file is the same and we're either at the beginning of a
           // line or we have a real filename or we're at the beginning
           // of line.  There are many compiler substitutions within
           // statements, and we don't want to break up output lines
           // with pointless srclocs.
 
           if (!fileSame && config.trackLines) {
 
             // We need to emit at the beginning of the line,
             // even if the file has changed.  If we break up lines,
             // we may alter the meaning of the javascript -
             // 'return foo' can become 
             //    return
             //    foo
             // (two separate statements).
 
             if (atBol && (ns.hasfile || os.hasfile)) {
               showSrcloc = true;
             }
           }
           // Show source location if we are 'forced' to and have a name
           // No check for atBol here, a LINENUM_FORCE should only be used
           // in cases where it is safe to break lines.
 
           else if (op == ANNOTATE_OP_FILE_LINENUM_FORCE &&
                    ns.filename.length() > 0) {
             showSrcloc = true;
           }
           // Otherwise, at the beginning of a line, show it if it has changed.
           else if (atBol && config.trackLines && ns.linenum > 0 &&
                    (!lineSame || !fileSame)) {
             showSrcloc = true;
           }
 
           // If debugging, indicate the reasons we are or are not showing loc
           if (Config.DEBUG_LINE_NUMBER && config.trackLines) {
 
             String shorthand = showSrcloc ? "L: " : "!L: ";
             if (!ns.hasfile) {
               shorthand += "!file ";
             }
             if (!fileSame) {
               shorthand += "!fsame ";
             }
             if (!lineSame) {
               shorthand += "!lsame ";
             }
             if (op == ANNOTATE_OP_FILE_LINENUM_FORCE) {
               shorthand += "force ";
             }
             curtu.addText("/* " + shorthand + "*/");
           }
 
           return showSrcloc;
         }
 
         public String notify(char op, String operand) {
           switch (op) {
           case ANNOTATE_OP_TEXT:
             if (Config.DEBUG_LINE_NUMBER) {
               int nl = operand.indexOf('\n');
               if (nl >= 0) {
                 int curline = curtu.getTextLineNumber();
                 operand = operand.substring(0, nl) +
                   "   /* #" + curline + " */" +
                   operand.substring(nl);
               }
             }
             curtu.addText(operand);
             if (operand.endsWith("\n")) {
               atBol = true;
             }
             else if (operand.length() > 0) {
               atBol = false;
             }
             return "";
 
           /* We always emit the FILE_LINENUM_FORCE annotations (they appear
            * at beginning of functions) but plain old line number
            * annotations are emitted only if the line information
            * cannot be determined from the previous 'file: ' marker
            * and simple incrementing.  We keep track of the difference
            * between the line number we would generate and the number
            * of lines we've actually output, if the difference
            * changes, we know an observer of the output would be off
            * and it's time to output a line number marker.
            */
           case ANNOTATE_OP_FILE_LINENUM_FORCE:
           case ANNOTATE_OP_FILE_LINENUM:
             LineNumberState newLstate = getLineNumberState(curtu, operand);
 
             if (shouldRecordSourceLocation(newLstate)) {
               curtu.setInputLineNumber(newLstate.linenum, sources.byName(newLstate.filename));
             }
             if (shouldShowSourceLocation(curLstate, newLstate, op, atBol)) {
               String srcloc = atBol ? "" : "\n";
               if (op == ANNOTATE_OP_FILE_LINENUM_FORCE) {
                 srcloc += "/* -*- file: " + operand + " -*- */\n";
               } else if (newLstate.filename.length() == 0) {
                 srcloc += "/* -*- file: -*- */\n";
               } else if (curLstate.filename.equals(newLstate.filename)) {
                 srcloc += "/* -*- file: #" + newLstate.linenum + " -*- */\n";
               }
               else {
                 srcloc += "/* -*- file: " + operand + " -*- */\n";
               }
               curtu.addText(srcloc);
               newLstate.linediff++; // compensate for line just added
               curLstate = newLstate;
             }
             return "";
           case ANNOTATE_OP_CLASSNAME:
             curtu = new TranslationUnit();
             curtu.setName(operand);
             curtu.setIsClass(true);
             tunits.add(curtu);
             return "";
           case ANNOTATE_OP_INSERTSTREAM:
             // Since the contents of each stream is not fully formed yet,
             // we can't insert it verbatim yet.  TranslationUnit will
             // handle the insertion in a final pass.
             curtu.addInsertStreamMarker(Integer.parseInt(operand));
             return "";
           case ANNOTATE_OP_CLASSEND:
             curtu = defaulttu;
             return "";
           case '0': case '1': case '2': case '3': case '4':
           case '5': case '6': case '7': case '8': case '9':
             int streamNum = ((op) - '0');
             curtu.addStreamText(streamNum, operand);
             return "";
           }
           return "";
         }
       };
 
     ap.process(annotated);
 
     return (tunits);
   }
 
 }
