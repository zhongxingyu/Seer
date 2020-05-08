 package japa.parser.ast.visitor;
 
 import japa.parser.ast.CompilationUnit;
 import japa.parser.ast.body.ClassOrInterfaceDeclaration;
 import japa.parser.ast.body.MethodDeclaration;
 import japa.parser.ast.scope.GlobalScope;
 import japa.parser.ast.scope.LocalScope;
 import japa.parser.ast.scope.Scope;
 import japa.parser.ast.stmt.BlockStmt;
 import japa.parser.ast.symbol.ClassSymbol;
 import japa.parser.ast.symbol.MethodSymbol;
 
 
 /**
  * First visitor
  * Finds classes and methods and sets their scopes
  * Create new scope
  * Add to symbol table
  * Pass data back to AST
 * Ensure the scope is correct fot the tree to continue
  */
 public final class TypingVisitor extends VoidVisitorAdapter {
 
     Scope currentScope;
 
    int a, b, c, d;

     @Override
     public void visit(CompilationUnit n, Object arg) {
         /* Create a global scope */
         currentScope = new GlobalScope();
         n.setData(currentScope);
         /* Only one thing at this level so no need to reset*/
         super.visit(n, arg);
     }
 
     @Override
     public void visit(ClassOrInterfaceDeclaration n, Object arg) {
 
         /* ClassSymbol is also a scope */
         ClassSymbol symbol = new ClassSymbol(n.getName(), currentScope, null);
         /* Pass back to tree */
         n.setData(symbol);
 //        currentScope = (Scope) n.getData();
 
         /* Add to symbol table*/
         currentScope.define(symbol);
         /* Update scope to allow recursive descent to continue */
         currentScope = symbol;
         super.visit(n, arg);
         /* Reset scope to allow*/
         currentScope = currentScope.getEnclosingScope();
     }
 
     @Override
     public void visit(MethodDeclaration n, Object arg) {
         String name = n.getName();
         /* Normally would check this but YOLO */
         String type = n.getType().toString();
 
         MethodSymbol symbol = new MethodSymbol(name, null, currentScope);
         /* Add method to symbol table */
         currentScope.define(symbol);
         n.setData(symbol);
         currentScope = symbol;
         super.visit(n, arg);
         currentScope = currentScope.getEnclosingScope();
     }
 
     @Override
     public void visit(BlockStmt n, Object arg) {
         currentScope = new LocalScope(currentScope);
         n.setData(currentScope);
         super.visit(n, arg);
         currentScope = currentScope.getEnclosingScope();
     }
 }
