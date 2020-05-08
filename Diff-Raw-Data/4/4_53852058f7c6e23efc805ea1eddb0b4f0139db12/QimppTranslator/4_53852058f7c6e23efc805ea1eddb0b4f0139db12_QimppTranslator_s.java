 /*
  * xtc - The eXTensible Compiler
  * Copyright (C) 2012 Robert Grimm
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * version 2 as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
  * USA.
  */
 
 package qimpp;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.PrintWriter;
 
 import xtc.lang.JavaFiveParser;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 import xtc.tree.Printer;
 
 import xtc.util.Tool;
 
 /**
  * A translator from (a subset of) Java to (a subset of) C++.
  *
  * @author QIMPP
  * @version 0.1
  */
 public class QimppTranslator extends Tool {
   
   GNode currentClass, currentMethod;
   CPPAST cppast;
       
   /** Create a new translator. */
   public QimppTranslator() {
     cppast = new CPPAST();
   }
 
   public String getName() {
     return "Java to C++ Translator";
   }
 
   public String getCopy() {
     return "(C) 2012 qimpp";
   }
 
   public void init() {
     super.init();
   }
 
   public void prepare() {
     super.prepare();
 
     // Perform consistency checks on command line arguments.
   }
 
   public File locate(String name) throws IOException {
     File file = super.locate(name);
     if (Integer.MAX_VALUE < file.length()) {
       throw new IllegalArgumentException(file + ": file too large");
     }
     return file;
   }
 
   public Node parse(Reader in, File file) throws IOException, ParseException {
     JavaFiveParser parser =
       new JavaFiveParser(in, file.toString(), (int)file.length());
     Result result = parser.pCompilationUnit(0);
     return (Node)parser.value(result);
   }
   
   public void run(String[] args){
     super.run(args);
     cppast.printAST();
   }
 
   public void process(Node node) {
     new Visitor() {
 
       public void visitBlock(GNode n) {
         visit(n);
       }
 
       public void visitCallExpression(GNode n) {
       
         visit(n);
       }
 
       public void visitClassBody(GNode n){
         visit(n);
       }
         
       public void visitClassDeclaration(GNode n) {
         currentClass = cppast.addClass(n.getString(1));
         visit(n);
       }
       
       public void visitCompilationUnit(GNode n) {
         visit(n);
         cppast.printAST();
       }
       
       public void visitConstructorDeclaration(GNode n) {
         GNode constructor = cppast.addConstructor(currentClass);
         cppast.addConstructorInstruction(n.getGeneric(5).getGeneric(0), constructor);
       }
 
       public String visitDeclarator(GNode n) {
         return n.getString(0);
       }
 
       public void visitExpressionStatement(GNode n) {
         visit(n);
       }
 
       public void visitExpression(GNode n){
         visit(n);
       }        
                       
       public void visitFieldDeclaration(GNode n) {
         String type = visitType(n.getGeneric(1));
         GNode declarators = n.getGeneric(2);
         for(int i = 0; i < declarators.size(); i++){
           String name = visitDeclarator(declarators.getGeneric(i));
           cppast.addField(name, type, currentClass);
         }
       }
  
       public void visitFormalParameters(GNode n){
         visit(n);
       }
 
       public void visitNewClassExpression(GNode n) {
         visit(n);
       }  
 
       public void visitMethodDeclaration(GNode n) {
         visit(n);
       }
      
      public void visitClassDeclaration(GNode n) {
        visit(n);
      }
 
       public void visitStringLiteral(GNode n){
         visit(n);
       }
         
       public String visitType(GNode n) {
         GNode identifier = n.getGeneric(0);
         if(identifier.hasName("PrimitiveIdentifier")){
           return Type.primitiveType(identifier.getString(0));
         } else {
           return Type.qualifiedIdentifier(identifier.getString(0));
         }
       }
  
       public void visit(Node n) {
         for (Object o : n) if (o instanceof Node) dispatch((Node)o);
       }
 
     }.dispatch(node);
   }
 
   /**
    * Run the translator with the specified command line arguments.
    *
    * @param args The command line arguments.
    */
   public static void main(String[] args) {
     new QimppTranslator().run(args);   
   }
 
 }
