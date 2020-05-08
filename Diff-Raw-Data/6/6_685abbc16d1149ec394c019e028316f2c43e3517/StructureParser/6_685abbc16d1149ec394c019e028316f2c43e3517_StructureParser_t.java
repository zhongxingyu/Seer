 package xtc.oop;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.FileReader;
 
 import java.util.*; //ut oh, is Grimm going to be mad?
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 import xtc.tree.Printer;
 
 import xtc.lang.JavaFiveParser;
 
 //OUR IMPORTS
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.io.BufferedReader;
 import java.io.Reader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 
 import xtc.util.SymbolTable;
 
 
 import xtc.oop.helper.Bubble;    //NEED TO UPDATE TO OUR NEW DATA STRUCTURES
 import xtc.oop.helper.Mubble;
 import xtc.oop.helper.Pubble;
 import xtc.oop.helper.Field;
 
 public class StructureParser extends xtc.tree.Visitor //aka Decl
 {
 
     public static ArrayList<Bubble> bubbleList;
     public static ArrayList<Pubble> pubbleList;
     public static ArrayList<Mubble> mubbleList;
     public static ArrayList<Mubble> langList;
     public static ArrayList<String> parsed; //keeps track of what ASTs have been parsed
     public Pubble curPub;
     public Bubble curBub;
     public Mubble curMub;
     //public varTable SymbolTable;
     //public funcTable SymbolTable;
     public SymbolTable table;
 
     public Field curField;
 
     public NewTranslator t; //used for the parse method
 
     public StructureParser(NewTranslator t, ArrayList<Pubble> packageTree, ArrayList<Mubble> mubbleList, ArrayList<Bubble> bubbleList, ArrayList<Mubble> langList)
     {
         this.t = t;
         this.pubbleList = packageTree;
         this.mubbleList = mubbleList;
         this.langList = langList;
         this.bubbleList = bubbleList;
         //this.parsed = parsed;
     }
 
 
     public static String findFile(String query) {//{{{
 
         String sep = System.getProperty("file.separator");
         String cp = System.getProperty("java.class.path");
         //Hardcoded as the working directory, otherwise real classpath
         cp = ".";
 
         query = query.replace(".",sep).concat(".java");
         //System.out.println("+++++"+query);
         return findFile(cp, query);
     }//}}}
 
     public static String findFile(String cp, String query) {//{{{
         String sep = System.getProperty("file.separator");
         File f = new File(cp);
         File [] files = f.listFiles();
         for(int i = 0; i < files.length; i++) {
             //System.out.println(sep+(cp.equals(".") ? "\\\\" : "")+cp+sep);
             //////////////////////////////////////
             //Hardcoding that sep is / and cp is .
             //////////////////////////////////////
             //System.out.println(query);
             if(files[i].isDirectory()) {
                 String a = findFile(files[i].getAbsolutePath(), query);
                 if(!a.equals(""))
                     return a;
             }
             else if(files[i].getAbsolutePath().replaceAll("/\\./",sep).endsWith(query))
                 return files[i].getAbsolutePath();
         }
         return "";
     }//}}}
 
     public void visit(Node n)//{{{
     {
         int counter = 1;
         if(n.hasProperty("parent0")) {
             Node temp = (Node)n.getProperty("parent0");
 
             while(temp != null) {
                 n.setProperty("parent"+(counter++), temp.getProperty("parent0"));
                 temp = (Node)temp.getProperty("parent0");
             }
         }
 
         for (Object o : n){
             if (o instanceof Node){
                 ((Node)o).setProperty("parent_name", n.getName() );
                 ((Node)o).setProperty("parent0", n );
                 dispatch((Node)o);
             }
         }
     }
     //}}}
 
     public void visitClassDeclaration(GNode n){
         //n.getString(0) is the Modifiers node
         //n.getString(1) is the name of the class
         String className = n.getString(1);
         boolean found = false;
         for(Bubble b : bubbleList){
             if(b.getName().equals(className)) {
                 curBub = b;
                 found = true;
             }
         }
         if (!found){
             curBub = new Bubble(className);
         }
         table = curBub.getTable();
         curBub.setParentPubble(curPub); //curBub's package is curPub
 
         //find Object bubble and set curBub's parent to object (it will get overwritten if it needs to)
         Bubble ob = new Bubble();
         for(Bubble b : bubbleList){
             if(b.getName().equals("Object")) {
                 ob = b;
             }
         }
 
         curBub.setParentBubble(ob);
 
         visit(n);
         table = null;
 
         //add in the delete method (note it needs code as well)
         Mubble n1 = new Mubble("__delete");
         n1.setReturnType("void");
         n1.setFlag('w');
         n1.addCode("delete __this;");
         curBub.addMubble(n1);
         //curBub should be complete here, all it's dataFields, methods, children bubbles, package..etc
         curBub.setIsBuilt(true);
 
         bubbleList.add(curBub);
         curPub.addBubble(curBub);
     }
 
 
 
     boolean dataField = false;
     public void visitFieldDeclaration(GNode n){
         Node parent0 = (Node)n.getProperty("parent0");
 
         if(parent0.hasName("ClassBody")) //then this is a datafield
         {
             curField = new Field();
             dataField = true;
         }
         visit(n);
 
         if(dataField)
         {
             dataField = false;
             curBub.addField(curField);
         }
 
         if(false) System.out.println("curField.name: " + curField.name);
     }
 
     public void visitDimensions(GNode n) {
         //will set curfield as an array and with correct details if appropriate
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
         Node parent3 = (Node)n.getProperty("parent3");
         if ((parent0.hasName("Type")) &&
                 (parent1.hasName("FieldDeclaration")))
         {
             curField.setIsArray(true);
 
             //count dimensions
             int count = 0;
             for(Object o : n){
                 if(o instanceof String && ((String)o).equals("[")){
                     count++;
                 }
             }
             curField.setArrayDims(count);
         }
 
     }
 
     public void visitModifiers(GNode n){
         visit(n);
 
     }
 
     public void visitConstructorDeclaration(GNode n){
         String name = n.getString(2);
         Mubble freshMubble = new Mubble(name);
         freshMubble.setConstructor(true);
 
         curMub = freshMubble;
 
         SymbolTable.Scope current = table.current();
         String name2 = name;
         while (current.isDefined(name2)) {
             name2 = "_" + name;
         }
         current.define(name2, "constructor");
         if (!name2.equals(name)) {
             curMub.mangleName(name2);
         }
 
 
         visit(n);
 
         curMub.setBubble(curBub);
         curMub.setPackage(curPub);
 
         mubbleList.add(curMub);
         curBub.addMubble(curMub);
         if(curMub.isConstructor())
             System.out.println("there is totally a constructor");
 
 
     }
 
     public void visitMethodDeclaration(GNode n){
 
         /*
          * get method name
          * Create a new mubble
          * Visit
          * Add this new mubble to curbub
          * Also add to mubble list
          */
 
         //Create a Mubble to add
         Mubble freshMubble;
         String name = n.getString(3);
         if (name == "static"){
             name = n.getString(4);
             freshMubble = new Mubble(name);
             freshMubble.setStatic(true);
         }
         else{
             freshMubble = new Mubble(name);
         }
         curMub = freshMubble;
         SymbolTable.Scope current = table.current();
         String name2 = name;
         while (current.isDefined(name2)) {
             name2 = "_" + name2;
         }
         current.define(name2, "method");
         if (!name2.equals(name)) {
             curMub.mangleName(name2);
         }
 
         visit(n);
 
         curMub.setBubble(curBub);
         curMub.setPackage(curPub);
         curMub.setParameters();
 
         mubbleList.add(curMub);
 
         curBub.addMubble(curMub);
 
     }
 
     public void visitModifier(GNode n){
         visit(n);
 
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
         Node parent3 = (Node)n.getProperty("parent3");
 
         //System.out.println("parented");
 
         
         if  (parent1 != null && parent2 != null &&          //if its part of a method
                 (parent1.hasName("MethodDeclaration")) &&
                 (parent2.hasName("ClassBody")))
         {
             //todo: Don't think collects all the modifiers for a method, just the first
             //what if it is a public static ....method
             String visibility = n.getString(0);
             curMub.setVisibility(visibility);
         }
         else if (parent1 != null && parent3 != null &&      //if its part of the parameters
                 (parent1.hasName("FormalParameter")) &&
                 (parent3.hasName("MethodDeclaration")))
         {
             String modifier = n.getString(0);
             curField.addModifier(modifier);
         }
         else if(dataField)                                  //if its a modifier for a dataField
         {
             String modifier = n.getString(0);
             curField.addModifier(modifier);
         }
 
     }
 
 
     public void visitDeclarators(GNode n) {
         SymbolTable.Scope current = table.current();
         // assuming Declarators' parent is always FieldDeclaration
         Node parent0 = (Node)n.getProperty("parent0");
         String type = parent0.getNode(1).getNode(0).getString(0);
         for (Object o : n) {
             Node ch = (Node)o; // does not check already defined name
             current.define(ch.getString(0), type);
         }
 
         visit(n);
     }
 
     public void visitDeclarator(GNode n) {
         //TODO fact check with an AST (see testAssignmt..a.java)
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
 
         if ((parent1.hasName("FieldDeclaration")) && 
                 (parent2.hasName("ClassDeclaration"))){
             if(!n.getNode(2).equals("null")) //if the 3rd child of Declarator is not null, an assignment has occured    
             { 
                 curField.setHasAssignment(true);
                 curField.setAssignment(n); //save the node so we can re-parse it later
             }
         }
 
         if(dataField) //if this is a dataField
         {
             curField.name = n.getString(0);
             if(!n.getNode(2).equals("null")) //if an assignment has occured
             {
                 curField.setHasAssignment(true);
                 curField.setAssignment(n); //save the node so we can parse it later
             }
                 
         }
     }
 
     public void visitIntegerLiteral(GNode n) {
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
 
         if ((parent0.hasName("ConcreteDimensions")) &&
                 (parent2.hasName("ClassDeclaration"))){
             //TODO what do we do about full declarations? e.g.
             //String[] SA = new String[4];
             //int a = 5;
             //Bubble b = new Bubble(param1, param2, param3)
             //THIS IS A BIG TODO
             //Answer = they go in a static block,
             //or are put in the constructors for the object
             //still a big TODO- check out javap for how java does it.
 
        }
     }
 
     public void visitClassBody(GNode n){
         visit(n);
     }
 
     public void visitFormalParameters(GNode n){
         Node parent0 = (Node)n.getProperty("parent0");
         if (parent0.hasName("ConstructorDeclaration")) {
             table.enter(parent0.getString(2));
         }
         else if (parent0.hasName("MethodDeclaration")) {
             table.enter(parent0.getString(3));
         }
         visit(n);
         table.exit();
     }
 
     public void visitFormalParameter(GNode n) {
 
 
         Field tempField = new Field();
         curField = tempField;
 
         String name = n.getString(3);
         curField.setName(name);
 
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
 
         /*
         if(parent1.hasName("ConstructorDeclaration")){
             System.out.println("V_V_V_V_V_V_V_V_V_V_");
             System.out.println(curField.name + " :: " + curField.type);
             System.out.println("V_V_V_V_V_V_V_V_V_V_");
         }
         */
 
         curMub.addParameter(curField);
     }
 
     public void visitCompilationUnit(GNode n){
         /*
          * Finding the Pubble that this bad boy should go in.
          * If it doesn't exist, curPub is simply added to the list
          * If it does exits, curPub is set to the correct pubble in visitPackageDecl
          */
 
         curPub = new Pubble(); //may change down the line if the Pubble already exists
         boolean DP = false;
         if(n.getNode(0) == null){//no package explicitly declared, DP
             for(Pubble p : pubbleList){
                 if(p.getName().equals("Default Package")){
                     curPub = p;
                     DP = true;
                 }
             }
         }
         visit(n);
 
         /*//{{{
           if(curPub.getName()==null){
           System.out.println("God dammit who's adding this bullshit null pubble");
           System.out.println("V_V_V_ inspect null package _V_V_V_V");
           for(Bubble b : curPub.getBubbles())
           {
           System.out.println("\tClass: " + b.getName());
           for(Mubble m : b.getMubbles())
           {
           System.out.println("\t\tMethod: " + m.getName());
           System.out.println("\t\t{\n \t\t" + m.getCode() + "\n\t\t}");
 
           }
           }
 
           curPub.setName("Default Package");
           }
           *///}}}
 
         //now add the pubble if it's not already in the list
         boolean inList = false;
         for(Pubble p : pubbleList){
             if(p == curPub)
                 inList = true;
         }
         if(!inList)
             pubbleList.add(curPub);
 
         //Check if any bubbles haven't been built (we have not parsed their AST yet)
         for(Bubble b : bubbleList){
             if (!(b.isBuilt())){
                 try{ // r u serious, java?
                     String fileName = findFile(b.getName());
                     File f = new File(fileName);
                     FileInputStream fi = new FileInputStream(f);
                     Reader in = new BufferedReader(new InputStreamReader(fi));
                     Node leNode=  t.parse(in, f);
                     this.dispatch(leNode);
                 }catch(Exception e){System.out.println("error: " + e); }
             }
         }
     }
 
     public void visitQualifiedIdentifier(GNode n){
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
 
         //finding inheritance
         if ((parent1.hasName("Extension")) &&
                 (parent2.hasName("ClassDeclaration"))){
             String parentName = n.getString(0);
 
             boolean parentFound = false;
             Bubble parent = new Bubble();
             for(Bubble b : bubbleList){
                 if(b.hasName(parentName)){             //if the parent is already in bubbleList
                     parent = b;
                     parentFound = true;
                 }
             }
 
             if(!parentFound){
                 parent = new Bubble(parentName);      //if parent isn't found, create it
                 bubbleList.add(parent);
             }
 
             parent.addBubble(curBub);  //add myself as my parent's child
             curBub.setParentBubble(parent); //set my parent
                 }
 
         if(dataField) // if this is a dataField
             curField.setType(n.getString(0));
 
         //finding the package we are in -> set curPub correctly
         //NOTE: there is no curBub at this point, so we must set curBub to the
         //right pakcage in class Decl.
         if (parent0.hasName("PackageDeclaration")){
             //looping through something like...
             /*QualifiedIdentifier(
               "xtc",
               "oop",
               "helper"
               )
               */
             String name;
             String packageName = "";
             for(int i=0; i<n.size(); i++){
                 name = n.getString(i);
                 packageName += " " + name;
             }
 
             curPub.setName(packageName);
 
             //check to see if this package is already in pubbleList
             //we can assume this step happens only at the beginning of an AST-
             //I.E. it is safe to throw away the current pubble, and replace it
             //with the "correct" one;
 
             Pubble packPub = new Pubble();//will be overwritten but compiler is scared it won't
             Boolean inPubbleList = false;
             for(Pubble p : pubbleList)
             {
                 if(p.getName().equals(packageName))
                 {
                     inPubbleList = true;
                     packPub = p;
                 }
             }
 
             //if its not in the packageList, create a new Pubble, and add it to pubbleList
             if(!inPubbleList)
             {
                 packPub = new Pubble(packageName);
                 pubbleList.add(packPub);
             }
             curPub = packPub; //necessary? *doesn't seem to hurt at least*
         }
         //get return type for methods
         if ((parent0.hasName("Type")) &&
                 (parent1.hasName("MethodDeclaration")))
         {
             String type = n.getString(0);
             curMub.setReturnType(type);
         }
 
         //get parameter type for methods
         if ((parent0.hasName("Type")) &&
                 (parent1.hasName("FormalParameter")))
         {
             String type = n.getString(0);
             curField.setType(type);
         }
 
 
     }
 
     public void visitImportDeclaration(GNode n){
         visit(n);
     }
 
     public void visitForStatement(GNode n)
     {
         //varTable.enter("for");
         //funcTable.enter("for");
         table.enter("for");
         visit(n);
         //varTable.exit();
         //funcTable.exit();
         table.exit();
     }
 
     public void visitWhileStatement(GNode n) {
         visit(n);
     }
 
     public void visitDoWhileStatement(GNode n) {
         visit(n);
     }
 
     public void visitSwitchStatement(GNode n) {
         visit(n);
     }
 
     public void visitBlock(GNode n) {
         Node parent0 = (Node)n.getProperty("parent0");
         boolean hasEntered = false;
         if (parent0.hasName("WhileStatement")) {
             //varTable.enter("while");
             //funcTable.enter("while");
             table.enter("while");
             hasEntered = true;
         }
         if (parent0.hasName("DoWhileStatement")) {
             //varTable.enter("dowhile");
             //funcTable.enter("dowhile");
             table.enter("dowhile");
             hasEntered = true;
         }
         if (parent0.hasName("ConditionalStatement")) {
             //varTable.enter("if-else");
             //funcTable.enter("if-else");
             table.enter("if-else");
             hasEntered = true;
         }
         if (parent0.hasName("Block")) {
             //varTable.enter("block");
             //funcTable.enter("block");
             table.enter("block");
             hasEntered = true;
         }
         if (parent0.hasName("SwitchStatement")) {
             //varTable.enter("switch");
             //funcTable.enter("switch");
             table.enter("switch");
             hasEntered = true;
         }
         if (parent0.hasName("TryCatchFinallyStatement")) {
             table.enter("try-finally");
             hasEntered = true;
         }
         if (parent0.hasName("CatchClause")) {
             table.enter("catch");
             hasEntered = true;
         }
         visit(n);
         //varTable.exit();
         //funcTable.exit();
 
         if(hasEntered)
             table.exit();
     }
 
     public void visitBasicForControl(GNode n)
     {
         visit(n);
     }
 
 
     boolean debugPrimitiveType = false;
     public void visitPrimitiveType(GNode n) {
         visit(n);
         Node parent0 = (Node)n.getProperty("parent0");
         Node parent1 = (Node)n.getProperty("parent1");
         Node parent2 = (Node)n.getProperty("parent2");
 
         //finding inheritance
         if ((parent0.hasName("Type")) &&
                 (parent1.hasName("FormalParameter"))){
             String type = n.getString(0);
             curField.setType(type);
                 }
 
         if ((parent0.hasName("Type")) &&
                 (parent1.hasName("FieldDeclaration"))){
                 String type = n.getString(0);
                 curField.setType(type);
                 }
 
 
         if ((parent0.hasName("Type")) && //if the return type for a method is a primitive type
                 (parent1.hasName("MethodDeclaration"))){
             String type = n.getString(0);
             if(debugPrimitiveType) System.out.println("visitPrimativeType, type: " + type);
             curMub.setReturnType(type);
                 }
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
 
     public void visitVoidType(GNode n) {
         Node parent0 = (Node)n.getProperty("parent0");
         if (parent0.hasName("MethodDeclaration")) {
             curMub.setReturnType("void");
         }
         visit(n);
     }
 
 }
