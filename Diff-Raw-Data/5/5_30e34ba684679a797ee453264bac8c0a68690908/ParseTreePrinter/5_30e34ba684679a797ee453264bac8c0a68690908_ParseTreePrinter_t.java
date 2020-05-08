 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
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
 
   // For debugging
   public static final boolean DEBUG_NODE_OUTPUT = false;
 
   boolean compress;
   boolean trackLines;
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
     this(false, false);
   }
   
   public ParseTreePrinter(boolean compress) {
     this(compress, false);
   }
 
   public ParseTreePrinter(boolean compress, boolean obfuscate) {
     this(compress, obfuscate, false);
   }
 
   // TODO: [2007-11-21 dda] if compress/obfuscate are on, probably
   // can turn off generation of annotations.
   public ParseTreePrinter(boolean compress, boolean obfuscate, boolean trackLines) {
     this.compress = compress;
     this.trackLines = trackLines;
     // Set whitespace
     this.SPACE = compress ? "" : " ";
     this.NEWLINE = obfuscate ? "" : "\n";
     // Set punctuation
     this.COMMA = "," + SPACE;
     this.COLON = ":" + SPACE;
     this.ASSIGN = SPACE + "=" + SPACE;
     this.CONDITIONAL = SPACE + "?" + SPACE;
     this.ALTERNATIVE = SPACE + ":" + SPACE;
     this.OPENPAREN = SPACE + "(";
     this.CLOSEPAREN = ")" + SPACE;
     this.SEMI = ";";
     this.OPTIONAL_SEMI = (compress && "\n".equals(NEWLINE)) ? NEWLINE : SEMI;
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
   
   public String delimit(String phrase, boolean force) {
     // Strip the phrase of annotations so we can look at the first char
     String plain = unannotate(phrase);
     if (plain.length() > 0) {
       return ((('(' != plain.charAt(0)) && force)?" ":SPACE) + phrase;
     }
     return phrase;
   }
   
   public String delimit(String phrase) {
     return delimit(phrase, true);
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
             sep = SEMI + (compress ? SPACE : NEWLINE);
           } else {
             sep = (compress ? SPACE : NEWLINE);
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
     
     OperatorNames = (String[])on.toArray(OperatorNames);
   }
   
   public String visitAssignmentExpression(SimpleNode node, String[] children) {
     int thisPrec = prec(((ASTOperator)node.get(1)).getOperator(), false);
     assert children.length == 3;
     children[2] = maybeAddParens(thisPrec, node.get(2), children[2], true);
     return children[0] + SPACE + children[1] + delimit(children [2], false);
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
     return "return" + delimit(children[0]);
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
       children[i] = delimit(maybeAddParens(thisPrec, node.get(i), children[i]), false);
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
       sb.append(delimit(child, required || opChar == unannotate(child).charAt(0)));
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
     return doFunctionDeclaration(node, children, this.compress ? false : true, false);
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
     String txt = "";
     // Add location information if not compressing
     if ((!this.compress) && (node.filename != null) && (node.beginLine != 0)) {
       txt = ("\n/* -*- file: " + Compiler.getLocationString(node) + " -*- */\n" );
     }
     txt += "function" + (useName ? (" " + name) : "") + OPENPAREN + args + CLOSEPAREN;
     if (!inmixin) {
       txt += makeBlock(body);
     }
     else {
       // This is an interface - no body needed
       txt += SEMI;
     }
     return txt;
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
           if (compress && l > 0) {
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
     return join(COMMA, children);
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
   
   public List makeTranslationUnits(SimpleNode node) {
     return makeTranslationUnits(visit(node));
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
   public static final char ANNOTATE_OP_LINENUM = 'L';
   public static final char ANNOTATE_OP_NODENAME = 'N';
   public static final char ANNOTATE_OP_NODEEND = 'n';
   public static final char ANNOTATE_OP_TEXT = 'T';
   
   /**
    * Prefix line number annotation to a string.
    */
   public String lnum(SimpleNode node, String str) {
     int linenum = node.getLineNumber();
     if (linenum < 0)
       return str;
     if (str.length() > 0 && str.charAt(0) == ANNOTATE_MARKER)
       return str;
 
     StringBuffer sb = new StringBuffer();
     sb.append(makeAnnotation(ANNOTATE_OP_LINENUM, String.valueOf(linenum)));
     sb.append(str);
     String result = sb.toString();
 
     if (DEBUG_NODE_OUTPUT)
       result = annotateNode(node, result);
 
     return result;
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
 
   /**
    * Add annotations, which look like \u0001 opchar operand \u0001
    * opchar is a single character.
    */
   public String makeAnnotation(char op, String operand) {
     return String.valueOf(ANNOTATE_MARKER) + op + operand + ANNOTATE_MARKER;
   }
   
   public abstract static class AnnotationProcessor {
     public abstract String notify(char op, String operand);
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
           case ANNOTATE_OP_LINENUM:
             return "#line " + operand + ": ";
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
 
   public List makeTranslationUnits(String annotated) {
     final ArrayList tunits = new ArrayList();
     final TranslationUnit defaulttu = new TranslationUnit(true);
 
     tunits.add(defaulttu);
 
     if (DEBUG_NODE_OUTPUT)
       System.out.println("ANNOTATED OUTPUT:\n" + printableAnnotations(annotated));
 
     AnnotationProcessor ap = new AnnotationProcessor() {
         TranslationUnit curtu = defaulttu;
         boolean atBol = true;
         int linenumDiff = 0;
 
         public String notify(char op, String operand) {
           switch (op) {
           case ANNOTATE_OP_TEXT:
             curtu.addText(operand);
             if (trackLines) {
               if (operand.endsWith("\n")) {
                 atBol = true;
               }
               else if (operand.length() > 0) {
                 atBol = false;
               }
             }
             return "";
           case ANNOTATE_OP_LINENUM:
             int linenum = Integer.parseInt(operand);
             if (trackLines && atBol && linenum != 0) {
               int newdiff = curtu.getTextLineNumber() - linenum;
               if (newdiff != linenumDiff) {
                 curtu.addText("/* -*- file: #" + linenum + " -*- */\n");
                 linenumDiff = newdiff + 1;  // account for the line just added
               }
             }
             curtu.setInputLineNumber(linenum);
             return "";
           case ANNOTATE_OP_CLASSNAME:
             curtu = new TranslationUnit();
             curtu.setName(operand);
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
