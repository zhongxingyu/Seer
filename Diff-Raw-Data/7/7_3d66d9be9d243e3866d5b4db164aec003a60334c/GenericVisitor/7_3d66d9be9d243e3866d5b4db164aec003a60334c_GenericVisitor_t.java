 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /**
  * Generic AST Visitor
  *
  * @author ptw@pobox.com
  *
  * Walks the abstract syntaxt tree returned by the parser,
  * guaranteeing to visit every node.  Analyzers, transformers and code
  * generators can subclass this and specialize visitors to walk
  * particular parts of syntax tree
  */
 
 package org.openlaszlo.sc;
 import java.io.*;
 import java.util.*;
 
 import org.openlaszlo.sc.parser.*;
 
 public class GenericVisitor implements ASTVisitor {
   // Just the options part of the Translator -- for include
   // processing.  This should probably be remodularized.
 
   // Holds #pragmas and other options
   Compiler.OptionMap options = new Compiler.OptionMap();
 
   public Compiler.OptionMap getOptions() {
     return this.options;
   }
 
   public void setOptions(Compiler.OptionMap options) {
     this.options = options;
   }
 
   public Boolean evaluateCompileTimeConditional(SimpleNode node) {
     // Don't!
     return null;
   }
 
   public SimpleNode visitProgram(SimpleNode node, SimpleNode[] directives) {
     for (int i = 0, len = directives.length; i < len; i++) {
       SimpleNode directive = directives[i];
       SimpleNode[] children = directive.getChildren();
       if (directive instanceof ASTDirectiveBlock) {
         // Not sure why this isn't just a statement list
         directives[i] = visitDirectiveBlock(directive, children);
       } else if (directive instanceof ASTProgram) {
         // This happens if we visit an AST that has already had its
         // includes expanded
         directives[i] = visitProgram(directive, children);
       } else if (directive instanceof ASTIfDirective) {
         directives[i] = visitIfStatement(directive, children);
       } else if (directive instanceof ASTIncludeDirective) {
         String userfname = (String)((ASTLiteral)children[0]).getValue();
         directives[i] = translateInclude(userfname);
       } else if (directive instanceof ASTPragmaDirective) {
         directives[i] = visitPragmaDirective(directive, children);
       } else if (directive instanceof ASTPassthroughDirective) {
         directives[i] = visitPassthroughDirective(directive, children);
       } else if ((directive instanceof ASTFunctionDeclaration) ||
                  (directive instanceof ASTClassDefinition) ||
                  (directive instanceof ASTStatement)) {
         directives[i] = visitStatement(directive);
       } else if (directive instanceof ASTModifiedDefinition) {
         directives[i] = visitModifiedDefinition(directive, directive.getChildren());
       } else {
         directives[i] = visitExpression(directive, false);
       }
     }
     return node;
   }
 
   // Include translation is not handled in the parser so we can track
   // them for incremental compiling.
   SimpleNode translateInclude(String userfname) {
     File file = includeNameToFile(userfname);
     String source = includeFileToSourceString(file, userfname);
 
     try {
       ParseResult result = parseFile(file, userfname, source);
       SimpleNode program = result.parse;
       return visitProgram(program, program.getChildren());
     }
     catch (ParseException e) {
       System.err.println("" + e + " while compiling " + file.getAbsolutePath());
       throw e;
     }
   }
 
   static class ParseResult {
     SimpleNode parse;
     boolean hasIncludes;
 
     ParseResult(SimpleNode parse, boolean hasIncludes) {
       this.parse = parse;
       this.hasIncludes = hasIncludes;
     }
 
     public boolean equals(Object o) {
       if (o != null && o instanceof ParseResult) {
         ParseResult pr = (ParseResult)o;
         return parse.equals(pr.parse) && hasIncludes == pr.hasIncludes;
       }
       return false;
     }
   }
 
   static java.util.regex.Pattern includePattern =
     java.util.regex.Pattern.compile(".*#\\s*include\\s*\".*", java.util.regex.Pattern.DOTALL);
 
   ParseResult parseFile(File file, String userfname, String source) {
     if (Compiler.CachedParses == null) {
       Compiler.CachedParses = new ScriptCompilerCache();
     }
     String sourceKey = file.getAbsolutePath();
     String sourceChecksum = "" + file.lastModified(); // source;
     ParseResult entry = (ParseResult)Compiler.CachedParses.get(sourceKey, sourceChecksum);
     if ((entry == null)
         || options.getBoolean(Compiler.VALIDATE_CACHES)
         ) {
       boolean hasIncludes = includePattern.matcher(source).matches();
       if (options.getBoolean(Compiler.PROGRESS)) {
         // Even though code generation is re-run
         // for every file, just print this for
         // files that are re-parsed, to indicate
         // what's being changed.
         System.err.println("Compiling " + userfname + "...");
       }
       SimpleNode program = (new Compiler.Parser()).parse(source);
       // We could not cache if we knew we were only compiling once...
       ParseResult realentry = new ParseResult(program, hasIncludes);
       Compiler.CachedParses.put(sourceKey, sourceChecksum, realentry);
       if ((entry != null) && options.getBoolean(Compiler.VALIDATE_CACHES)) {
         if (! realentry.equals(entry)) {
           System.err.println("Bad parse cache for " + sourceKey + ": " + entry + " != " + realentry);
         }
       }
       entry = realentry;
     }
     return entry;
   }
 
   File includeNameToFile(String userfname) {
     try {
       String fname = userfname;
 
       if (options.containsKey(Compiler.RESOLVER)) {
         fname = ((lzsc.Resolver)options.get(Compiler.RESOLVER)).resolve(userfname);
       }
       return new File(new File(fname).getCanonicalPath());
     }
     catch (IOException e) {
       throw new CompilerError("error reading include: " + e);
     }
   }
 
   String includeFileToSourceString(File file, String userfname) {
     String source;
     try {
       FileInputStream stream = new FileInputStream(file);
       try {
         int n = stream.available();
         byte[] b = new byte[n];
         stream.read(b);
         source = "#file " + userfname + "\n#line 1\n" + new String(b, "UTF-8");
       }
       finally {
         stream.close();
       }
     }
     catch (FileNotFoundException e) {
       throw new CompilerError("error reading include: " + e);
     }
     catch (UnsupportedEncodingException e) {
       throw new CompilerError("error reading include: " + e);
     }
     catch (IOException e) {
       throw new CompilerError("error reading include: " + e);
     }
     return source;
   }
 
   //
   // Statements
   //
 
   public SimpleNode visitStatement(SimpleNode node) {
     return visitStatement(node, node.getChildren());
   }
 
   public SimpleNode visitStatement(SimpleNode node, SimpleNode[] children) {
     // Are we doing OO programming yet?
     if (node instanceof ASTPragmaDirective) {
       return visitPragmaDirective(node, children);
     } else if (node instanceof ASTPassthroughDirective) {
       return visitPassthroughDirective(node, children);
     } else if (node instanceof ASTClassDefinition) {
       return visitClassDefinition(node, children);
     } else if (node instanceof ASTStatementList) {
       return visitStatementList(node, children);
     } else if (node instanceof ASTFunctionDeclaration) {
       return visitFunctionDeclaration(node, children);
     } else if (node instanceof ASTMethodDeclaration) {
       return visitMethodDeclaration(node, children);
     } else if (node instanceof ASTStatement) {
       int len = children.length;
       assert len == 0 || len == 1;
       // an empty statement, introduced by an extra ";", has no children
       if (len == 1) {
         children[0] = visitStatement(children[0], children[0].getChildren());
       }
       return node;
     } else if (node instanceof ASTLabeledStatement) {
       return visitLabeledStatement(node, children);
     } else if (node instanceof ASTVariableDeclaration) {
       return visitVariableDeclaration(node, children);
     } else if (node instanceof ASTVariableDeclarationList) {
       return visitVariableDeclarationList(node, children);
     } else if (node instanceof ASTVariableStatement) {
       return visitVariableStatement(node, children);
     } else if (node instanceof ASTIfStatement) {
       return visitIfStatement(node, children);
     } else if (node instanceof ASTIfDirective) {
       return visitIfDirective(node, children);
     } else if (node instanceof ASTWhileStatement) {
       return visitWhileStatement(node, children);
     } else if (node instanceof ASTDoWhileStatement) {
       return visitDoWhileStatement(node, children);
     } else if (node instanceof ASTForStatement) {
       return visitForStatement(node, children);
     } else if (node instanceof ASTForVarStatement) {
       return visitForVarStatement(node, children);
     } else if (node instanceof ASTForInStatement) {
       return visitForInStatement(node, children);
     } else if (node instanceof ASTForVarInStatement) {
       return visitForVarInStatement(node, children);
     } else if (node instanceof ASTContinueStatement) {
       return visitContinueStatement(node, children);
     } else if (node instanceof ASTBreakStatement) {
       return visitBreakStatement(node, children);
     } else if (node instanceof ASTReturnStatement) {
       return visitReturnStatement(node, children);
     } else if (node instanceof ASTWithStatement) {
       return visitWithStatement(node, children);
     } else if (node instanceof ASTTryStatement) {
       return visitTryStatement(node, children);
     } else if (node instanceof ASTThrowStatement) {
       return visitThrowStatement(node, children);
     } else if (node instanceof ASTSwitchStatement) {
       return visitSwitchStatement(node, children);
     } else if (node instanceof ASTModifiedDefinition) {
       return visitModifiedDefinition(node, children);
     } else if (node instanceof Compiler.PassThroughNode) {
       return node;
     } else {
       // Not a statement, must be an expression
       return visitExpression(node, false);
     }
   }
 
   public SimpleNode visitPragmaDirective(SimpleNode node, SimpleNode[] children) {
     return node;
   }
 
   public SimpleNode visitPassthroughDirective(SimpleNode node, SimpleNode[] children) {
     return node;
   }
 
   public SimpleNode visitIfDirective(SimpleNode node, SimpleNode[] children) {
     return visitIfStatement(node, children);
   }
 
   public SimpleNode visitDirectiveBlock(SimpleNode node, SimpleNode[] children) {
     return visitStatementList(node, children);
   }
 
   public SimpleNode visitStatementList(SimpleNode node, SimpleNode[] children) {
     for (int i = 0, len = children.length; i < len; i++) {
       SimpleNode stmt = children[i];
       children[i] = visitStatement(stmt);
     }
     return node;
   }
 
   public SimpleNode visitLabeledStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode label = children[0];
     // Identifiers are a shorthand for a literal string, should
     // not be evaluated (or remapped).  [Maybe call visitLiteral?]
     assert label instanceof ASTIdentifier;
     SimpleNode stmt = children[1];
     children[1] = visitStatement(stmt);
     return node;
   }
 
   public SimpleNode visitVariableDeclarationList(SimpleNode node, SimpleNode[] children) {
     for (int i = 0, len = children.length ; i < len; i++) {
       SimpleNode child = children[i];
       children[i] = visitStatement(child);
     }
     return node;
   }
 
   public SimpleNode visitVariableStatement(SimpleNode node, SimpleNode[] children) {
     for (int i = 0, len = children.length; i < len; i++) {
       SimpleNode child = children[i];
       children[i] = visitStatement(child);
     }
     return node;
   }
 
   public SimpleNode visitVariableDeclaration(SimpleNode node, SimpleNode[] children) {
     int len = children.length;
     assert len == 1 || len == 2;
     ASTIdentifier id = (ASTIdentifier)children[0];
     children[0] = translateIdentifier(id, AccessMode.DECLARE);
     if (len == 2) {
       SimpleNode initValue = children[1];
       children[1] = visitExpression(initValue);
     }
     return node;
   }
 
   public SimpleNode visitIfStatement(SimpleNode node, SimpleNode[] children) {
     int len = children.length;
     assert len == 2 || len == 3;
     SimpleNode test = children[0];
     children[0] = visitExpression(test);
     SimpleNode thenStmt = children[1];
     children[1] = visitStatement(thenStmt);
     if (len == 3) {
       SimpleNode elseStmt = children[2];
       children[2] = visitStatement(elseStmt);
     }
     return node;
   }
 
   public SimpleNode visitContinueStatement(SimpleNode node, SimpleNode[] children) {
     int len = children.length;
     assert len == 0 || len == 1;
     if (len == 1) {
       SimpleNode label = children[0];
       // Identifiers are a shorthand for a literal string, should
       // not be evaluated (or remapped).  [Maybe call visitLiteral?]
       assert label instanceof ASTIdentifier;
     }
     return node;
   }
 
   public SimpleNode visitBreakStatement(SimpleNode node, SimpleNode[] children) {
     int len = children.length;
     assert len == 0 || len == 1;
     if (len == 1) {
       SimpleNode label = children[0];
       // Identifiers are a shorthand for a literal string, should
       // not be evaluated (or remapped).  [Maybe call visitLiteral?]
       assert label instanceof ASTIdentifier;
     }
     return node;
   }
 
   public SimpleNode visitWhileStatement(SimpleNode node, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode test = children[0];
     children[0] = visitExpression(test);
     SimpleNode body = children[1];
     children[1] = visitStatement(body);
     return node;
   }
 
   public SimpleNode visitDoWhileStatement(SimpleNode node, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode body = children[0];
     children[0] = visitStatement(body);
     SimpleNode test = children[1];
     children[1] = visitExpression(test);
     return node;
   }
 
   public SimpleNode visitForStatement(SimpleNode node, SimpleNode[] children) {
     assert children.length == 4;
    SimpleNode init = children[0];
    children[0] = visitExpression(init);
     SimpleNode test = children[1];
     children[1] = visitExpression(test);
     SimpleNode step = children[2];
     children[2] = visitStatement(step);
     SimpleNode body = children[3];
     children[3] = visitStatement(body);
     return node;
   }
 
   public SimpleNode visitForVarStatement(SimpleNode node, SimpleNode[] children) {
     assert children.length == 4;
     SimpleNode var = children[0];
     children[0] = visitStatement(var);
     SimpleNode test = children[1];
     children[1] = visitExpression(test);
     SimpleNode step = children[2];
     children[2] = visitStatement(step);
     SimpleNode body = children[3];
     children[3] = visitStatement(body);
     return node;
   }
 
   public SimpleNode visitForInStatement(SimpleNode node, SimpleNode[] children) {
     assert children.length == 3;
     ASTIdentifier id = (ASTIdentifier)children[0];
     children[0] = translateIdentifier(id, AccessMode.MODIFY);
     SimpleNode obj = children[1];
     children[1] = visitExpression(obj);
     SimpleNode body = children[2];
     children[2] = visitStatement(body);
     return node;
   }
 
   public SimpleNode visitForVarInStatement(SimpleNode node, SimpleNode[] children) {
     assert children.length == 4;
     SimpleNode var = children[0];
     children[0] = visitStatement(var);
 //     SimpleNode _ = children[1]; // Parser incorrectly accepts an init value
     SimpleNode obj = children[2];
     children[2] = visitExpression(obj);
     SimpleNode body = children[3];
     children[3] = visitStatement(body);
     return node;
   }
 
   public SimpleNode visitReturnStatement(SimpleNode node, SimpleNode[] children) {
     assert children.length == 1;
     SimpleNode value = children[0];
     children[0] = visitExpression(value);
     return node;
   }
 
   public SimpleNode visitWithStatement(SimpleNode node, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode expr = children[0];
     SimpleNode stmt = children[1];
     children[0] = visitExpression(expr);
     children[1] = visitStatement(stmt);
     return node;
   }
 
   public SimpleNode visitTryStatement(SimpleNode node, SimpleNode[] children) {
     int len = children.length;
     assert len == 2 || len == 3;
     SimpleNode block = children[0];
     children[0] = visitStatement(block);
     if (len == 2) {
       // Could be catch or finally clause
       SimpleNode catfin = children[1];
       if (catfin instanceof ASTCatchClause) {
         children[1] = visitCatchClause(catfin, catfin.getChildren());
       } else {
         assert catfin instanceof ASTFinallyClause;
         children[1] = visitFinallyClause(catfin, catfin.getChildren());
       }
     } else if (len == 3) {
       SimpleNode cat = children[1];
       assert cat instanceof ASTCatchClause;
       children[1] = visitCatchClause(cat, cat.getChildren());
       SimpleNode fin = children[2];
       assert fin instanceof ASTFinallyClause;
       children[2] = visitFinallyClause(fin, fin.getChildren());
     }
     return node;
   }
 
   public SimpleNode visitCatchClause(SimpleNode node, SimpleNode[] children) {
     assert children.length == 2;
     ASTIdentifier id = (ASTIdentifier)children[0];
     // Treat the catch identifier as a binding.  This is not quite
     // right, need to integrate with variable analyzer, but this is
     // the one case in ECMAScript where a variable does have block
     // extent.
     children[0] = translateIdentifier(id, AccessMode.DECLARE);
     SimpleNode body = children[1];
    children[1] = visitStatement(body);
     return node;
   }
 
   public SimpleNode visitFinallyClause(SimpleNode node, SimpleNode[] children) {
     assert children.length == 1;
     SimpleNode body = children[0];
     children[0] = visitStatement(body);
     return node;
   }
 
   public SimpleNode visitThrowStatement(SimpleNode node, SimpleNode[] children) {
     assert children.length == 1;
     SimpleNode expr = children[0];
     children[0] = visitExpression(expr);
     return node;
   }
 
   public SimpleNode visitSwitchStatement(SimpleNode node, SimpleNode[] children) {
     SimpleNode expr = children[0];
     children[0] = visitExpression(expr);
     for (int i = 1, len = children.length; i < len; i++) {
       SimpleNode clause = children[i];
       if (clause instanceof ASTDefaultClause) {
         children[i] = visitDefaultClause(clause, clause.getChildren());
       } else {
         assert clause instanceof ASTCaseClause : "case clause expected";
         children[i] = visitCaseClause(clause, clause.getChildren());
       }
     }
     return node;
   }
 
   public SimpleNode visitDefaultClause(SimpleNode node, SimpleNode[] children) {
     int len = children.length;
     assert len == 0 || len == 1;
     if (len == 1) {
       SimpleNode body = children[0];
       children[0] = visitStatement(body);
     }
     return node;
   }
 
   public SimpleNode visitCaseClause(SimpleNode node, SimpleNode[] children) {
     int len = children.length;
     assert len == 1 || len == 2;
     SimpleNode caseExpr = children[0];
     children[0] = visitExpression(caseExpr);
     if (len == 2) {
       SimpleNode body = children[1];
       children[1] = visitStatement(body);
     }
     return node;
   }
 
   //
   // Expressions
   //
 
   public SimpleNode visitExpression(SimpleNode node) {
     return visitExpression(node, true);
   }
 
   /* This function, unlike the other expression visitors, can be
      applied to any expression node, so it dispatches based on the
      node's class. */
   public SimpleNode visitExpression(SimpleNode node, boolean isReferenced) {
     // Are we doing OO programming yet?
     SimpleNode[] children = node.getChildren();
     SimpleNode newNode = null;
 
     if (node instanceof ASTIdentifier) {
       newNode = visitIdentifier(node, isReferenced, children);
     }
     else if (node instanceof ASTLiteral) {
       newNode = visitLiteral(node, isReferenced, children);
     }
     else if (node instanceof ASTExpressionList) {
       newNode = visitExpressionList(node, isReferenced, children);
     }
     else if (node instanceof ASTEmptyExpression) {
       newNode = visitEmptyExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTThisReference) {
       newNode = visitThisReference(node, isReferenced, children);
     }
     else if (node instanceof ASTArrayLiteral) {
       newNode = visitArrayLiteral(node, isReferenced, children);
     }
     else if (node instanceof ASTObjectLiteral) {
       newNode = visitObjectLiteral(node, isReferenced, children);
     }
     else if (node instanceof ASTFunctionExpression) {
       newNode = visitFunctionExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTFunctionCallParameters) {
       newNode = visitFunctionCallParameters(node, isReferenced, children);
     }
     else if (node instanceof ASTPropertyIdentifierReference) {
       newNode = visitPropertyIdentifierReference(node, isReferenced, children);
     }
     else if (node instanceof ASTPropertyValueReference) {
       newNode = visitPropertyValueReference(node, isReferenced, children);
     }
     else if (node instanceof ASTCallExpression) {
       newNode = visitCallExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTSuperCallExpression) {
       newNode = visitSuperCallExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTNewExpression) {
       newNode = visitNewExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTPostfixExpression) {
       newNode = visitPostfixExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTUnaryExpression) {
       newNode = visitUnaryExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTBinaryExpressionSequence) {
       newNode = visitBinaryExpressionSequence(node, isReferenced, children);
     }
     else if (node instanceof ASTAndExpressionSequence) {
       newNode = visitAndExpressionSequence(node, isReferenced, children);
     }
     else if (node instanceof ASTOrExpressionSequence) {
       newNode = visitOrExpressionSequence(node, isReferenced, children);
     }
     else if (node instanceof ASTConditionalExpression) {
       newNode = visitConditionalExpression(node, isReferenced, children);
     }
     else if (node instanceof ASTAssignmentExpression) {
       newNode = visitAssignmentExpression(node, isReferenced, children);
     }
     else {
       throw new CompilerImplementationError("unknown expression: \"" + (new ParseTreePrinter()).text(node) + "\"", node);
     }
     return newNode;
   }
 
   public static class AccessMode {
     public static final AccessMode DECLARE = new AccessMode();
     public static final AccessMode EVALUATE = new AccessMode();
     public static final AccessMode MODIFY = new AccessMode();
 
     private AccessMode() {}
   }
 
   // Hook for subclasses
   public ASTIdentifier translateIdentifier(ASTIdentifier id, AccessMode mode) {
     return id;
   }
 
   public SimpleNode visitIdentifier(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     return translateIdentifier((ASTIdentifier)node, AccessMode.EVALUATE);
   }
 
   public SimpleNode visitLiteral(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     return node;
   }
 
   public SimpleNode visitExpressionList(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     // all but last expression is not referenced
     int i = 0, len = children.length - 1;
     for ( ; i < len; i++) {
       children[i] = visitExpression(children[i], false);
     }
     children[len] = visitExpression(children[len], isReferenced);
     return node;
   }
 
   public SimpleNode visitEmptyExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     return node;
   }
 
   // Hook for subclasses
   public ASTThisReference translateThisReference(ASTThisReference node) {
     return node;
   }
 
   public SimpleNode visitThisReference(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     return translateThisReference((ASTThisReference)node);
   }
 
   public SimpleNode visitArrayLiteral(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     for (int i = 0, len = children.length; i <len; i++) {
       children[i] = visitExpression(children[i], isReferenced);
     }
     return node;
   }
 
   public SimpleNode visitObjectLiteral(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     boolean isKey = true;
     for (int i = 0, len = children.length; i < len; i++) {
       SimpleNode item = children[i];
       if (isKey && (item instanceof ASTIdentifier)) {
         // Identifiers are a shorthand for a literal string, should
         // not be evaluated (or remapped).  [Maybe call visitLiteral?]
         ;
       } else {
         children[i] = visitExpression(item);
       }
       isKey = (! isKey);
     }
     return node;
   }
 
   public SimpleNode visitFunctionCallParameters(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     for (int i = 0, len = children.length; i < len; i++) {
       children[i] = visitExpression(children[i]);
     }
     return node;
   }
 
   public SimpleNode visitPropertyIdentifierReference(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode expr = children[0];
     children[0] = visitExpression(expr);
     SimpleNode id = children[1];
     // Identifiers are a shorthand for a literal string, should
     // not be evaluated (or remapped).  [Maybe call visitLiteral?]
     assert id instanceof ASTIdentifier;
     return node;
   }
 
   public SimpleNode visitPropertyValueReference(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode expr = children[0];
     children[0] = visitExpression(expr);
     SimpleNode index = children[1];
     children[1] = visitExpression(index);
     return node;
   }
 
   // Hook for subclasses
   public SimpleNode noteCallSite(SimpleNode node) {
     return node;
   }
 
   public SimpleNode visitCallExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode fnexpr = children[0];
     children[0] = visitExpression(fnexpr);
     ASTFunctionCallParameters args = (ASTFunctionCallParameters)children[1];
     children[1] = visitFunctionCallParameters(args, isReferenced, args.getChildren());
     return noteCallSite(node);
   }
 
   public SimpleNode visitSuperCallExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 3;
     SimpleNode id = children[0];
     if (id instanceof ASTEmptyExpression) {
       ;
     } else {
       children[0] = translateIdentifier((ASTIdentifier)id, AccessMode.EVALUATE);
     }
     SimpleNode callapply = children[1];
     ASTFunctionCallParameters args = (ASTFunctionCallParameters)children[2];
     children[2] = visitFunctionCallParameters(args, isReferenced, args.getChildren());
     return noteCallSite(node);
   }
 
   public SimpleNode visitNewExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode fnexpr = children[0];
     children[0] = visitExpression(fnexpr);
     SimpleNode maybeArgs = children[1];
     if (maybeArgs instanceof ASTEmptyExpression) {
       ;
     } else {
       ASTFunctionCallParameters args = (ASTFunctionCallParameters)maybeArgs;
       children[1] = visitFunctionCallParameters(args, isReferenced, args.getChildren());
     }
     return noteCallSite(node);
   }
 
   public SimpleNode visitPrefixExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode op = children[0];
     assert op instanceof ASTOperator;
     SimpleNode ref = children[1];
     children[1] = visitExpression(ref);
     return node;
   }
 
   public SimpleNode visitPostfixExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode ref = children[0];
     children[0] = visitExpression(ref);
     SimpleNode op = children[1];
     assert op instanceof ASTOperator;
     return node;
   }
 
   public SimpleNode visitUnaryExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 2;
     int op = ((ASTOperator)children[0]).getOperator();
     // The parser does not actually generate ASTPrefixExpression
     if (ParserConstants.INCR == (op) || ParserConstants.DECR == (op)) {
       return visitPrefixExpression(node, isReferenced, children);
     }
     SimpleNode arg = children[1];
     children[1] = visitExpression(arg);
     return node;
   }
 
   public SimpleNode visitBinaryExpressionSequence(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 3;
     SimpleNode a = children[0];
     children[0] = visitExpression(a);
     SimpleNode op = children[1];
     assert op instanceof ASTOperator;
     SimpleNode b = children[2];
     children[2] = visitExpression(b);
     return node;
   }
 
   public SimpleNode visitAndExpressionSequence(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode a = children[0];
     children[0] = visitExpression(a);
     SimpleNode b = children[1];
     children[1] = visitExpression(b);
     return node;
   }
 
   public SimpleNode visitOrExpressionSequence(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 2;
     SimpleNode a = children[0];
     children[0] = visitExpression(a);
     SimpleNode b = children[1];
     children[1] = visitExpression(b);
     return node;
   }
 
   public SimpleNode visitConditionalExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 3;
     SimpleNode test = children[0];
     children[0] = visitExpression(test);
     SimpleNode a = children[1];
     children[1] = visitExpression(a);
     SimpleNode b = children[2];
     children[2] = visitExpression(b);
     return node;
   }
 
   public SimpleNode visitAssignmentExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     assert children.length == 3;
     SimpleNode lhs = children[0];
     children[0] = visitExpression(lhs);
     SimpleNode op = children[1];
     assert op instanceof ASTOperator;
     SimpleNode rhs = children[2];
     children[2] = visitExpression(rhs);
     return node;
   }
 
   //
   // Functions (and methods)
   //
 
   public SimpleNode visitFunctionExpression(SimpleNode node, boolean isReferenced, SimpleNode[] children) {
     return translateFunction(node, false, children);
   }
 
   public SimpleNode visitFunctionDeclaration(SimpleNode node, SimpleNode[] ast) {
     return translateFunction(node, true, ast);
   }
 
   public SimpleNode visitMethodDeclaration(SimpleNode node, SimpleNode[] ast) {
     return translateMethod(node, true, ast);
   }
 
   SimpleNode translateFunction(SimpleNode node, boolean useName, SimpleNode[] children) {
     return translateFunction(node, useName, children, false);
   }
 
   SimpleNode translateMethod(SimpleNode node, boolean useName, SimpleNode[] children) {
     return translateFunction(node, useName, children, true);
   }
 
   SimpleNode translateFunction(SimpleNode node, boolean useName, SimpleNode[] children, boolean isMethod) {
     return translateFunctionInternal(node, useName, children, isMethod);
   }
 
   // Helper for above
   SimpleNode translateFunctionInternal(SimpleNode node, boolean useName, SimpleNode[] children, boolean isMethod) {
     int len = children.length;
     assert len == 2 || len == 3;
     // node can be any of:
     //   ASTFunctionDeclaration(name, formals, body)
     //   ASTMethodDeclaration(name, formals, body)
     //   ASTFunctionExpression(name, formals, body)
     //   ASTFunctionExpression(formals, body)
     // Handle the two arities:
     SimpleNode formals;
     SimpleNode body;
     if (len == 3) {
       formals = children[1];
       children[1] = visitFormalParameterList(formals, formals.getChildren());
       body = children[2];
     } else {
       formals = children[0];
       children[0] = visitFormalParameterList(formals, formals.getChildren());
       body = children[1];
     }
 
     SimpleNode[] stmtArray = body.getChildren();
     for (int i = 0, ilim = stmtArray.length; i < ilim; i++) {
       SimpleNode stmt = stmtArray[i];
       stmtArray[i] = visitStatement(stmt);
     }
 
     return node;
   }
 
   SimpleNode visitFormalParameterList(SimpleNode node, SimpleNode[] children) {
     for (int i = 0, len = children.length; i < len; ) {
       ASTIdentifier param = (ASTIdentifier)children[i];
       children[i] = translateIdentifier(param, AccessMode.DECLARE);
       if (i++ < len) {
         SimpleNode init = children[i];
         if (init instanceof ASTFormalInitializer) {
           children[i] = visitFormalInitializer(init, init.getChildren());
           i++;
         }
       }
     }
     return node;
   }
 
   SimpleNode visitFormalInitializer(SimpleNode node, SimpleNode[] children) {
     assert children.length == 1;
     SimpleNode initExpr = children[0];
     children[0] = visitExpression(initExpr);
     return node;
   }
 
   // Visitor for class/mixin/interfacs
   public SimpleNode visitClassDefinition(SimpleNode node, SimpleNode[] children) {
     int len = children.length;
     assert len >= 4;
     ASTIdentifier kindID = (ASTIdentifier)children[0];
     String kindName = kindID.getName();
     assert "class".equals(kindName) || "mixin".equals(kindName) || "interface".equals(kindName);
     ASTIdentifier classID = (ASTIdentifier)children[1];
     children[1] = translateIdentifier(classID, AccessMode.DECLARE);
     SimpleNode superID = children[2];
     if (! (superID instanceof ASTEmptyExpression)) {
       translateIdentifier((ASTIdentifier)superID, AccessMode.EVALUATE);
     }
     SimpleNode mixinsList = children[3];
     if (! (mixinsList instanceof ASTEmptyExpression)) {
       assert mixinsList instanceof ASTMixinsList;
       SimpleNode[] mixins = mixinsList.getChildren();
       for (int j = 0, jlim = mixins.length; j < jlim; j++) {
         ASTIdentifier mixinID = (ASTIdentifier)mixins[j];
         mixins[j] = translateIdentifier(mixinID, AccessMode.EVALUATE);
       }
     }
     for (int i = 4; i < len; i++) {
       SimpleNode stmt = children[i];
       children[i] = visitStatement(stmt);
     }
     return node;
   }
 
   public SimpleNode visitModifiedDefinition(SimpleNode node, SimpleNode[] children) {
     assert children.length == 1;
     children[0] = visitStatement(children[0]);
     return node;
   }
 
 }
 
 /**
  * @copyright Copyright 2009 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
 
