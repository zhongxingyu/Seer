 /*
  * Conventions:
  *  -the root package name is "Default Package", not "" as before
  */
 
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
 import xtc.oop.helper.Mubble;
 import xtc.oop.helper.Pubble;
 import xtc.oop.helper.Field;
 
 import xtc.oop.StructureParser;
 import xtc.oop.ImplementationParser;
 //
 import java.util.regex.*; //ut oh...Grimm's gonna be pissed
 import java.io.FileWriter; //until we use xtc's printer
 import java.io.BufferedWriter; //or better yet unix does it for us
 
 public class NewTranslator extends xtc.util.Tool{
 
     public static ArrayList<Bubble> bubbleList; // Classes
     public static ArrayList<Pubble> pubbleList; // Packages
     public static ArrayList<Mubble> mubbleList; // Methods
     public static ArrayList<Mubble> langList;   // java_lang Methods (don't want to print them)
     public static ArrayList<String> fileNames; //names of files passed as args into NewTranslator
 
     public String getName()
     {
         return "LSD: Language Switching Device";
     }
 
     public String getCopy()
     {
 
         return "Ninja assassins: DK, Calvin, Andrew*2";
     }
 
     public void init(){
         super.init();
     }
 
     public Node parse(Reader in, File file) throws IOException, ParseException
     {
         JavaFiveParser parser = new JavaFiveParser(in, file.toString(), (int)file.length());
         Result result = parser.pCompilationUnit(0);
 
         return (Node)parser.value(result);
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
 
 
     public void process(Node node){
 
         StructureParser s = new StructureParser(this, pubbleList, mubbleList, bubbleList, langList);
         s.dispatch(node);
 
         //Pass methods down the inheritance tree
 		Bubble obj = new Bubble();
 		for(Bubble b: bubbleList){
 			//System.out.println("b dot get name " + b.getName());
 			if(b.getName().equals("Object"))
 				obj = b;
 		}
 		//System.out.println("object    " + obj.getName());
 		//System.out.println("Childrennnnnnn");
 
 		for(Bubble child : bubbleList){
 			//System.out.println("V_V_V_V_V_V_V_V__V__V_V_");
 			//System.out.println(child.getName());
 			if(!(child.getName().equals("String") || child.getName().equals("Object"))){
 				if(child.getParentBubble().getName().equals("Object")){
 					//System.out.println("Calling the method V_V_V_V_V_V_V_V_V_V_V_V_V_V_V_V_V_V");
 					child.mangleBetweenClasses();
 					child.inheritAndResolveDataFields(bubbleList);
 				}
 			}
 		}
 
 
 
 
         //Not sure if we need this:
         ArrayList<String> parsed = new ArrayList<String>();
         ImplementationParser i = new ImplementationParser(this, pubbleList, mubbleList, bubbleList, langList, parsed);
         i.dispatch(node);
 
         //find assignments for datafields here
         i.resolveDatafieldAssignments();
     }
 
     public static void main (String [] args)
     {
         /* new Translator
          * new StructParser //was Decl
          * new ImplParser   //was Impl
          */
 
         NewTranslator t = new NewTranslator();
         t.prepStructures();
         t.init();
         t.prepare();
         for(int i = 0; i< args.length; i++){
             try{ //TODO put in flag to not system exit also maybe change to run
                 t.process(args[i]);
                 fileNames.add(parseFileName(args[i]));
             } catch (Exception e) {System.out.println(e);}
         }
 
         //At this point, pubbleList contains all the packages but they aren't linked together
         t.constructPackageTree();
 
         if(false) //printing out bubble and methods/method parent
         {
             for(Bubble b : bubbleList){
                 if(!(b.getName().equals("String") || b.getName().equals("Object"))){
                     System.out.println("Bubble " + b.getName());
                     for(Mubble m : b.getMubbles()){
                         System.out.println("\tMubble: " + m.getName());
                         System.out.println("\tParent: " + m.getClassName() + "\n");
                     }
                 }
             }
             System.out.println("=======================================================");
         }
 
 		/*
         for(Bubble b: bubbleList){
             if(!(b.getName().equals("String") || b.getName().equals("Object"))){
                 //System.out.println("Inheriting Methods: " + b.getName());
                 if(b.getName().equals("testStaticMethods"))
                     b.inheritMethods();
             }
         }
 		*/
 
         if(false) //printing out bubble and methods/method parent
         {
             System.out.println("=======================================================");
             for(Bubble b : bubbleList){
                 if(!(b.getName().equals("String") || b.getName().equals("Object"))){
                     System.out.println("Bubble " + b.getName());
                     for(Mubble m : b.getMubbles()){
                         System.out.println("\tMubble: " + m.getName());
                         if(true) //print out info about mubble's fields
                         {
                             for(Field f: m.getParameters()){
                                 System.out.println("\t\t" + f.getType() + " " + f.getName());
                             }
 
                         }
                         System.out.println("\tisPrivate: " + m.isPrivate());
                         System.out.println("\tClass: " + m.getClassName() + "\n");
                     }
                 }
             }
         }
 
 
         //Print structure <- use for testing dependencies, inheritance
         if(false)
             for(Pubble p : pubbleList){
                 System.out.println("^||^||^||^||^||^||^||^||^||^||^");
                 System.out.println("Package: ////" + p.getName() + "////");
                 if(p.getParent() == null)
                     System.out.println("No parent");
                 else
                     System.out.println("Parent: " + p.getParent().getName());
                 System.out.println("Children: ");
                 for(Pubble c : p.getChildren()){
                     System.out.println("\t-" + c.getName());
                 }
                 System.out.println("Classes: ");
                 for(Bubble b : p.getBubbles()){
                     System.out.println("\t- " + b.getName());
                 }
             }
 
 
         //Printing Contents of Everything
         if(false)
             for(Pubble p : pubbleList)
             {
                 System.out.println("Package: " + p.getName());
                 for(Bubble b : p.getBubbles())
                 {
                     System.out.println("\tClass: " + b.getName());
                     for(Mubble m : b.getMubbles())
                     {
                         System.out.println("\t\tMethod: " + m.getName());
                         System.out.println("\t\tflag: " + m.getFlag());
                         System.out.println("\t\t{\n \t\t" + m.getCode() + "\n\t\t}");
                     }
                 }
             }
 
         /* Lets Print a .H!!! */
 
        if(false){
             System.out.println("==========================================================");
             System.out.println("=====================  " + fileNames.get(0) + ".h  =====================");
             Pubble root = new Pubble();
             for(Pubble p : pubbleList){
                 if(p.getName().equals("Default Package"))
                     root = p;
             }
             String doth = root.getH();
             System.out.println(doth);
             System.out.println("===========================================================");
             System.out.println("=====================  " + fileNames.get(0) + ".cc  =====================");
             String dotc = root.getCC(fileNames.get(0)); //passing the name of the input file as a parameter
             System.out.println(dotc);
             System.out.println("===========================================================");
         }
 
         if(false){ //print constructors code
             for(Bubble b : bubbleList){
                 for(Mubble m : b.getMubbles())
                     if(m.isConstructor())
                         System.out.println("CODE: " + m.getCode());
             }
         }
 
         /* Output to File */
        if(true){
             Pubble root = new Pubble();
             for(Pubble p : pubbleList){
                 if(p.getName().equals("Default Package"))
                     root = p;
             }
             String doth = root.getH();
             String dotc = root.getCC(fileNames.get(0));
             //Write .h to file
             String hFile = "../oop-project/tests/translated/" + fileNames.get(0) + ".h";
             String cFile = "../oop-project/tests/translated/" + fileNames.get(0) + ".cc";
 
             try{
                 //writing .h
                 System.out.println("writing .h to " + hFile);
                 File out = new File(hFile);
                 FileWriter hstream = new FileWriter(out);
                 BufferedWriter hwrite = new BufferedWriter(hstream);
                 hwrite.write(doth);
                 hwrite.close();
 
                 //writing. cc
                 System.out.println("writing .cc to " + cFile);
                 out = new File(cFile);
                 FileWriter cstream = new FileWriter(out);
                 BufferedWriter cwrite = new BufferedWriter(cstream);
                 cwrite.write(dotc);
                 cwrite.close();
             }
             catch (IOException e) {
                 // Print out the exception that occurred
                 System.out.println("Unable to create "+ fileNames.get(0) + ": "+e.getMessage());
             }
         }
     }
 
 
     //************************HELPER METHODS***********************//
     public void constructPackageTree()
         //constructs package tree from pubblelist
     {
         boolean rootFound = false;
         Pubble root = new Pubble();
         //set root to Default Package
 
         for(Pubble p : pubbleList){
             //System.out.println(p.getName());
             if(p.getName().equals("Default Package")){
                 root = p;
                 rootFound = true;
             }
         }
         if(!rootFound){ //this should never happen... just in case, create it
             root = new Pubble("Default Package", null);
             pubbleList.add(root);
         }
 
 
         //for all pubbles, link children to it. (search on parent name)
         //also, if your parent doesn't exist, create it
         //we create a copy of the list to iterate through to avoid concurrentModError
         ArrayList<Pubble> pubbleListCopy = new ArrayList<Pubble>(pubbleList);
         for(Pubble p : pubbleListCopy){
             String name = p.getName();
             if((p.getParent()==null || p.getParent().equals(""))
                     && !name.equals("Default Package")){
                 //get lineage
                 String[] lineage = name.split(" ");
 
                 //remove " " (~_~;)
                 int scount = 0;
                 for(String s : lineage){
                     if(s.equals(" "))
                         scount++;
                 }
                 String[] nlineage = new String[lineage.length - scount];
                 int ind = 0;
                 for(int i = 0; i< lineage.length; i ++){
                     if(!(lineage[i].equals(" "))){
                         nlineage[ind] = lineage[i];
                         ind++;
                     }
                 }
                 //" " are now removed ^_^
 
                 if(nlineage.length == 1){
                     p.setParent(root);
                     root.addChild(p);
                 }
                 else{
                     Pubble parent = findParent(lineage);
                     parent.addChild(p);
                 }
                     }
         }
 
         //now remove "" named packages
         ArrayList<Pubble> pubbleListCopy2 = (ArrayList<Pubble>)pubbleList.clone();
         for(Pubble p : pubbleListCopy2){
             if(p.getName().equals("")){
                 for(Pubble c: p.getChildren()){
                     p.getParent().addChild(c);
                 }
                 p.getParent().removeChild(p);
                 pubbleList.remove(p);
             }
         }
     }
 
       // Parses the filename from the arg given to the compiler
       //ex. ../oop-project/tests/testFor.java ====> testFor
       public static String parseFileName(String arg)
       {
            int index = arg.lastIndexOf("/");
            String file = arg.substring(index + 1);
            //System.out.println("******    " + file);
            index = file.lastIndexOf(".");
            file = file.substring(0, index);
            //System.out.println("******    " + file);
            return file;
       }
 
     public Pubble findParent(String[] lineage){
         /* Say we are given:
          * xtc oop helper
          * And want to find/create the parent, then return it.
          * Special case for "Default Package"
          */
         //find root
         Pubble root = new Pubble();
         boolean rootFound = false;
         //set root to Default Package
         for(Pubble p : pubbleList){
             if(p.getName().equals("Default Package")){
                 root = p;
                 rootFound = true;
             }
         }
         if(!rootFound)//serious problem
             System.out.println("Watch out buddy, we don't have a root package");
 
         String parentName = "";
         for(int i = 0; i < lineage.length-1; i++){
             parentName += lineage[i] + " ";
         }
         parentName = parentName.trim();
         boolean parentFound = false;
         Pubble parent = new Pubble();
         for(Pubble p : pubbleList){
             if(p.getName().trim().equals(parentName)) {
                 parent = p;
                 parentFound = true;
             }
         }
 
         if(!parentFound){//create it
             String[] parentLineage = parentName.split(" ");
             if(parentLineage.length==1){
                 parent = new Pubble(parentName, root);
                 root.addChild(parent);
             }
             else{
                 Pubble gparent = findParent(parentLineage);
                 parent = new Pubble(parentName, gparent);
                 gparent.addChild(parent);
             }
         }
         pubbleList.add(parent);
         return parent;
     }
 
     public void prepStructures(){
 
         /*
          * create root Pubble
          * create empty BubbleList
          * create empty MubbleList
          *
          * create Object and String and Array(?)  Bubbles
          */
         pubbleList = new ArrayList<Pubble>();
         bubbleList = new ArrayList<Bubble>();
         mubbleList = new ArrayList<Mubble>();
         langList = new ArrayList<Mubble>();
         fileNames = new ArrayList<String>();
 
 
         pubbleList.add(new Pubble("Default Package", null));
         //pre-load Object Bubble
         Bubble object = new Bubble("Object");
 
         object.setIsFilled(true);
         object.setIsBuilt(true);
         //Creating Object's Vtable
         //      Each of these will get a mubble, which is added to the Bubble
         /*object.add2Vtable("Class __isa;");
           delete
           object.add2Vtable("int32_t (*hashCode)(Object);");
           object.add2Vtable("bool (*equals)(Object, Object);");
           object.add2Vtable("Class (*getClass)(Object);");
           object.add2Vtable("String (*toString)(Object);"); */
 
         Mubble m1 = new Mubble("__delete");
         object.addMubble(m1);
         m1.setReturnType("void");
         m1.setFlag('n');
 
         Mubble m2 = new Mubble("hashCode");
         object.addMubble(m2);
         m2.setReturnType("int32_t");
         m2.setFlag('n');
         Mubble m3 = new Mubble("equals");
         object.addMubble(m3);
         m3.addParameter(new Field("dummy", "Object"));
         m3.setReturnType("bool");
         m3.setFlag('n');
         Mubble m4 = new Mubble("getClass");
         object.addMubble(m4);
         m4.setReturnType("Class");
         m4.setFlag('n');
         Mubble m5 = new Mubble("toString");
         object.addMubble(m5);
         m5.setReturnType("String");
         m5.setFlag('n');
         bubbleList.add(object);
 
 
         //pre-load String Bubble
         Bubble string = new Bubble("String");
         string.setIsFilled(true);
         string.setIsBuilt(true);
         //Creating Object's Vtable
         /*
            string.add2Vtable("Class __isa;");
            delete
            string.add2Vtable("int32_t (*hashCode)(String);");
            string.add2Vtable("bool (*equals)(String, Object);");
            string.add2Vtable("Class (*getClass)(String);");
            string.add2Vtable("String (*toString)(String);");
            string.add2Vtable("int32_t (*length)(String);");
            string.add2Vtable("char (*charAt)(String, int_32_t);"); */
         Mubble n1 = new Mubble("__delete");
         string.addMubble(n1);
         n1.setReturnType("void");
         n1.setFlag('w');
         Mubble n2 = new Mubble("hashCode");
         string.addMubble(n2);
         n2.setReturnType("int32_t");
         n2.setFlag('w');
         Mubble n3 = new Mubble("equals");
         string.addMubble(n3);
         n3.setReturnType("bool");
         n3.addParameter(new Field("dummy", "Object"));
         n3.setFlag('w');
         Mubble n4 = new Mubble("getClass");
         string.addMubble(n4);
         n4.setReturnType("Class");
         n4.setFlag('i');
         Mubble n5 = new Mubble("toString");
         string.addMubble(n5);
         n5.setReturnType("String");
         n5.setFlag('w');
         Mubble n6 = new Mubble("length");
         string.addMubble(n6);
         n6.setReturnType("int32_t");
         n6.setFlag('n');
         Mubble n7 = new Mubble("charAt");
         string.addMubble(n7);
         n7.setReturnType("char");
         n7.addParameter(new Field("dummy", "int32_t"));
         n7.setFlag('n');
         bubbleList.add(string);
     }
 }
 
