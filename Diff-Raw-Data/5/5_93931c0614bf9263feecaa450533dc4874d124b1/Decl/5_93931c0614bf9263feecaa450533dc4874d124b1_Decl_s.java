 package xtc.oop;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.FileReader;
 
 import java.util.*;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 
 import xtc.tree.Printer;
 
 import xtc.lang.JavaFiveParser;
 //our imports
 import xtc.oop.helper.Bubble;
 
 
 /** A Java file Scope analyzer
  * For each static scope, prints
  * 		Enter scope at <filename>:<line>:<column>
  * upon entering the scope.
  *
  * @author Calvin Hawkes
  * @version 1.0
  */
 
 public class Decl extends xtc.util.Tool
 {
 
 
     public static String findFile(String query) {
         String sep = System.getProperty("file.separator");
         String cp = System.getProperty("java.class.path");
         cp = ".";
 
         query = query.replace(".",sep).concat(".java");
 
         return findFile(cp, query);
     }
 
     //can return File if necessary
     public static String findFile(String cp, String query) {

 	File f = new File(cp);
 	File [] files = f.listFiles();
 	for(int i = 0; i < files.length; i++) {
 	    //System.out.println(files[i]);
 	    if(files[i].isDirectory()) {
 		String a = findFile(files[i].getAbsolutePath(), query);
 		if(!a.equals(""))
 		    return a;
 	    }
	    else if(files[i].getAbsolutePath().endsWith(query))
 		return files[i].getAbsolutePath();
 	}
 	return "";
     }
 
 
 
     public Decl()
     {
         // Nothing to do.
     }
 
     public String getName()
     {
         return "Java Scope Analyzer";
     }
 
     public String getCopy()
     {
 
         return "My Group";
     }
 
     public void init()
     {
         super.init();
 
         runtime.
             bool("printClassH", "printClassH", false, "print the .h that is interpreted from given AST").
             bool("printClassCC", "printClassCC", false, "Print Java AST.");
     }
 
     public Node parse(Reader in, File file) throws IOException, ParseException
     {
         JavaFiveParser parser = new JavaFiveParser(in, file.toString(), (int)file.length());
         Result result = parser.pCompilationUnit(0);
 
         return (Node)parser.value(result);
     }
 
     public void process(Node node)
     {
         //construct inheritance tree!
         new Visitor()
         {//{{{
 
             //assemble the forces
             ArrayList<String> dataFields = new ArrayList<String>();
             ArrayList<String> methods = new ArrayList<String>();
             String className = "";
             String tempString = "";
             int counter = 0;
 
             public void visitFieldDeclaration(GNode n){
                 dataFields.add("");
                 visit(n);
                 //dataField.add("\n");
             }
 
             public void visitDimensions(GNode n) {
                 visit(n);
             }
 
             public void visitModifiers(GNode n){
                 visit(n);
             }
 
             public void visitMethodDeclaration(GNode n){
                 methods.add("");
                 visit(n);
                 String name = n.getString(3);
                 methods.set(methods.size()-1,methods.get(methods.size()-1)+" "+name);
             }
 
             public void visitVoidType(GNode n){
                 visit(n);
                 Node parent1 = (Node)n.getProperty("parent1");
                 Node parent2 = (Node)n.getProperty("parent2");
                 if ((parent1.getName().equals("MethodDeclaration")) &&
                         (parent2.getName().equals("ClassBody"))){
                     methods.set(methods.size()-1,methods.get(methods.size()-1)+" void");
                         }
             }
 
             public void visitModifier(GNode n){
                 visit(n);
                 Node parent1 = (Node)n.getProperty("parent1");
                 Node parent2 = (Node)n.getProperty("parent2");
                 if ((parent1.getName().equals("MethodDeclaration")) &&
                         (parent2.getName().equals("ClassBody"))){
                     String name = n.getString(0);
                     methods.set(methods.size()-1,methods.get(methods.size()-1)+" "+name);
                         }
             }
 
             public void visitDeclarators(GNode n) {
                 visit(n);
             }
 
             public void visitCompilationUnit(GNode n) {
 
                 Bubble object = new Bubble("Object", null);
                 //Creating Object's Vtable
                 object.add2Vtable("Class __isa;");
                 object.add2Vtable("int32_t (*hashCode)(Object);");
                 object.add2Vtable("bool (*equals)(Object, Object);");
                 object.add2Vtable("Class (*getClass)(Object);");
                 object.add2Vtable("String (*toString)(Object);"); 
                 bubbleList.add(object); 
                 visit(n);
                 //link Object bubble to children and vice versa
                 for(Bubble b: bubbleList){
                     if(b.getName() == null){
                         System.out.println("NULL RETURNED FROM GETNAME");
                     }
                     System.out.println(b.getName());
                     if(!(b == object) && b.parentToString() == null){
                         b.setParent(object);
                         object.addChild(b.getName());
                     }
                 }
 
                 /*
                    runtime.console().pln("CLASS NAME:");
                    runtime.console().pln(className);
                    runtime.console().pln("DATA FIELDS:");
                    for(String a : dataFields){
                    runtime.console().pln(a);
                    }
                    runtime.console().pln("METHOD HEADERS:");
                    for(String a : methods){
                    runtime.console().pln(a);
                    }
                    runtime.console().p("\n").flush();
                    */
                 for(Bubble b: bubbleList){
                     System.out.println(b);
                     System.out.println(b.getName());
                     System.out.println(b.childrenToString());
                     System.out.println(b.parentToString());
                     System.out.println("--------XXX-------");
                 }
                 
                 populateVTables(object);
                 
                 for(Bubble b: bubbleList){
                     b.printVtable();
                 }
             }
             
             //recursive call to populate all vtables in bubbleList
             public void populateVTables(Bubble root){
                 for(Bubble b : bubbleList){
                     if (b.getParent() == root){
                         //creating child's vTable
                         for(String s : root.getVtable()) //getting parent's vtable
                             b.add2Vtable(s);
                         for(String s : b.getMethods()) //adding new methods to vtable
                             b.add2Vtable(s);
                             
                         //recursively setting child's vtables
                         populateVTables(b);
                     }
                
                 }
             }
 
             public void visitDeclarator(GNode n) {
                 visit(n);
                 Node parent1 = (Node)n.getProperty("parent1");
                 Node parent2 = (Node)n.getProperty("parent2");
                 if ((parent1.getName().equals("FieldDeclaration")) &&
                         (parent2.getName().equals("ClassBody"))){
                     String name = n.getString(0);
                     dataFields.set(dataFields.size()-1,dataFields.get(dataFields.size()-1)+" "+name);
                         }
 
             }
 
             public void visitIntegerLiteral(GNode n) {
                 visit(n);
             }
 
             public void visitClassBody(GNode n){
                 visit(n);
             }
 
             //ArrayList<String> methods = new ArrayList<String>();
             //ArrayList<String> dataFields = new ArrayList<String>();
             ArrayList<String> children = new ArrayList<String>();
             String name;
             Bubble parent;
             //String parent;
 
 
             public void visitClassDeclaration(GNode n){
                 bubbleList.add(new Bubble(n.getString(1), null));
                 visit(n);
                 //get parent
                 //if none: parent = object
                 className = n.getString(1);
                 String parentName = "";
                 //get inheritance
                 if (!n.hasProperty("parent_class")){
                     n.setProperty("parent_class", "Object");
                 }
                 parentName = (String)n.getProperty("parent_class");
 
                 Boolean parentFound = false;
                 Bubble parent = null;
                 for(Bubble b : bubbleList){
                     //if the bubble has already been added by a child
                     if(b.getName().equals(parentName)){
                         //want to set the child field of this bubble with my name
                         parent = b;
                         parentFound = true;
                         b.addChild(className);
                     }
                 }
 
                 if(!parentFound){
                     parent = new Bubble(parentName, className);
                     bubbleList.add(parent);
                 }
 
                 //if classname in bubbleList
                 //set the data fields
                 Boolean bubbleExists = false;
                 for(Bubble b : bubbleList){
                     if(b.getName().equals(className)) {
                         b.setMethods(methods.toArray(new String[methods.size()]));
                         b.setDataFields(dataFields.toArray(new String[dataFields.size()]));
                         if(parent != null) //it won't ever be null, but just to make compiler happy :P
                             b.setParent(parent);
                         bubbleExists = true;
                     }
                 }
                 //else: make that node
                 if(!bubbleExists){
                     Bubble temp = new Bubble(className,
                             methods.toArray(new String[methods.size()]),
                             dataFields.toArray(new String[dataFields.size()]),
                             parent, null);
                     bubbleList.add(temp);
                 }
             }
 
             public void visitExtension(GNode n){
                 visit(n);
             }
 
             public void visitFormalParameters(GNode n){
 
                 visit(n);
                 Node parent1 = (Node)n.getProperty("parent1");
                 Node parent2 = (Node)n.getProperty("parent2");
 
                 if ((parent1.getName().equals("MethodDeclaration")) &&
                         (parent2.getName().equals("ClassBody"))){
                     methods.set(methods.size()-1,methods.get(methods.size()-1)+"(");
                         }
 
                 //TODO this ending parens is out of order- is it necessary? need to discuss what format we need/want these in
                 if ((parent1.getName().equals("MethodDeclaration")) &&
                         (parent2.getName().equals("ClassBody"))){
                     methods.set(methods.size()-1,methods.get(methods.size()-1)+")");
                         }
             }
 
             public void visitFormalParameter(GNode n) {
                 visit(n);
                 Node parent1 = (Node)n.getProperty("parent1");
                 Node parent2 = (Node)n.getProperty("parent2");
                 if ((parent1.getName().equals("MethodDeclaration")) &&
                         (parent2.getName().equals("ClassBody"))){
                     String name = n.getString(3);
                     methods.set(methods.size()-1,methods.get(methods.size()-1)+" "+name);
                         }
             }
 
             public void visitQualifiedIdentifier(GNode n){
                 visit(n);
                 //for(String s : n.properties())
                 //    System.out.println(s);
                 Node parent1 = (Node)n.getProperty("parent1");
                 Node parent2 = (Node)n.getProperty("parent2");
                 //System.out.println(parent1);
                 //System.out.println(parent2);
                 if ((parent1.getName().equals("FieldDeclaration")) &&
                         (parent2.getName().equals("ClassBody"))){
                     String name = n.getString(0);
                     dataFields.set(dataFields.size()-1,dataFields.get(dataFields.size()-1)+" "+name);
                         }
                 if ((parent1.getName().equals("MethodDeclaration")) &&
                         (parent2.getName().equals("ClassBody"))){
                     String name = n.getString(0);
                     methods.set(methods.size()-1,methods.get(methods.size()-1)+" "+name);
                         }
                 if ((parent1.getName().equals("Extension")) &&
                         (parent2.getName().equals("ClassDeclaration"))){
                     String name = n.getString(0);
                     parent2.setProperty("parent_class", name);
                         }
                 boolean inList = false;
                 for(Bubble b : bubbleList){
                     if(b.getName().equals(n.getString(n.size()-1))){
                         inList = true;
                     }
                     //System.out.println(b);
                 }
 
                 if(!inList && !n.getString(n.size()-1).equals("String")){
                     System.out.println("about to call findFile:" + n.getString(n.size()-1));
                     String path = d.findFile(n.getString(n.size()-1));
                     if(!path.equals("")){
                         System.out.println(path);
                         try{
                             d.process(path);
                         } catch (Exception e) {System.out.println(e);}
                     }
                 }
             }
 
             public void visitImportDeclaration(GNode n){
                 visit(n);
             }
 
             public void visitForStatement(GNode n)
             {
                 visit(n);
             }
 
             public void visitBasicForControl(GNode n)
             {
                 visit(n);
             }
 
             public void visitPrimitiveType(GNode n) {
                 visit(n);
             }
 
             public void visitType(GNode n)
             {
                 visit(n);
             }
 
             public void visitExpressionList(GNode n)
             {
                 visit(n);
             }
 
             public void visitRelationalExpression(GNode n)
             {
                 visit(n);
             }
 
             public void visit(Node n)
             {
 
                 int counter = 1;
                 if(n.hasProperty("parent0")) {
                     Node temp = (Node)n.getProperty("parent0");
 
                     while(temp != null) {
                         //System.out.println(temp);
                         //temp = (Node)temp.getProperty("parent0");
 
 
                         n.setProperty("parent"+(counter++), temp.getProperty("parent0"));
                         temp = (Node)temp.getProperty("parent0");
                         //if(n.getProperty("parent2") == null)
                         //System.out.println(temp);
                     }
                 }
                 //don't need this, but not deleting.
                 for (String s : n.properties()) {
                     //System.out.println(n.getProperty(s));
                 }
 
                 for (Object o : n){
                     if (o instanceof Node){
                         ((Node)o).setProperty("parent_name", n.getName() );
                         ((Node)o).setProperty("parent0", n );
                         dispatch((Node)o);
                     }
                 }
             }//}}}
         }.dispatch(node);
     }
 
     /**
      * Run the thing with the specified command line arguments.
      *
      * @param args The command line arguments.
      */
     static Decl d;
     ArrayList<Bubble> bubbleList = new ArrayList<Bubble>();
     public static void main(String[] args)
     {
         //System.out.println(System.getProperty("java.class.path"));
         //Calvin and ALott
         /*
         String[] dependencies = <><><><><>;
         new Decl().run(args);
             Decl().run(finddependencies)
             for depend in dependencies:
                 Decl().run(constructBubbles, depend)
         */
 
         /*
         new Decl().run(args);
         */
         d = new Decl();
         d.init();
         d.prepare();
         for(int i = 0; i< args.length; i++){
             ////String [] names = args[i].split("\\.");
             //String theName = names[names.length-1] + ".java";
             try{
                 d.process(args[i]);
             } catch (Exception e) {System.out.println(e);}
         }
         
         //for(int i=0; i<bubbleList.size(); i++)
             //System.out.println
     }
 }
 
 
