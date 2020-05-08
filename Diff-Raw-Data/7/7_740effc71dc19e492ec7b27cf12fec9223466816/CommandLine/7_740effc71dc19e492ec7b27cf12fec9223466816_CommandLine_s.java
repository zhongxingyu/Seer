 // Copyright (C) 2003-2004, 2013  Carl Pulley
 // 
 // This program is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 // 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 // 
 // You should have received a copy of the GNU General Public License
 // along with this program. If not, see <http://www.gnu.org/licenses/>.
 
 package example;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.util.HashSet;
 import java.util.Set;
 import org.apache.ecs.xml.XML;
 import org.apache.ecs.xml.XMLDocument;
 import japa.parser.ast.body.ClassOrInterfaceDeclaration;
 import japa.parser.ast.CompilationUnit;
 import japa.parser.ast.PackageDeclaration;
 import japa.parser.ast.type.ClassOrInterfaceType;
 import japa.parser.ast.visitor.VoidVisitorAdapter;
 import japa.parser.JavaParser;
 
 public class CommandLine {
 
     public static void main(String[] args) throws Exception {
         System.out.println("started ...");
         XMLDocument feedback = new XMLDocument();
         feedback.addElement((XML)(new XML("feedback").setPrettyPrint(true)));
         int minArgSize = 2;
         if (args == null || args.length == 0) {
             throw new Exception("Usage: CommandLine <XML file> <java source file>+");
         } // end of if-then
         String outputFile = args[0];
         BufferedWriter outputF = new BufferedWriter(new FileWriter(outputFile));
         if (args.length < minArgSize) {
             feedback.addElement(error("No answer classes supplied!"));
         } else {
             String[] argSources = new String[args.length - 1];
             for(int i = 1; i < args.length; i++) {
                 argSources[i-1] = args[i];
             }
             Set<Class> answerClass = new HashSet<Class>();
             for(int nos = 0; nos < argSources.length; nos++) {
                 argSources[nos] = argSources[nos].trim();
                 File javaSrc = new File(argSources[nos]);
                 if (!javaSrc.exists()) {
                     feedback.addElement(error(argSources[nos] + " does not exist!"));
                     continue;
                 }
                 if (!javaSrc.canRead()) {
                     feedback.addElement(error(argSources[nos] + " exists, but can not be read!"));
                     continue;
                 }
                 FileInputStream in = new FileInputStream(argSources[nos]);
                 CompilationUnit cu;
                 try {
                     // Parse the Java source file
                     cu = JavaParser.parse(in);
                 } finally {
                     in.close();
                 } // end of try-catch
                 // Analyse declared classes in Java source file
                 SourceVisitor visitor = new SourceVisitor();
                 visitor.visit(cu, null);
                 if (visitor.getClasses().isEmpty()) {
                     feedback.addElement(((XML)(new XML("ignored").setPrettyPrint(true))).addXMLAttribute("file", argSources[nos]).addElement("java source file contains no class declarations!"));
                 } else {
                     for(String fqClass: visitor.getClasses()) {
                         try {
                             Class clazz = Class.forName(fqClass);
                             if (!Question.class.isAssignableFrom(clazz)) {
                                 feedback.addElement(((XML)(new XML("ignored").setPrettyPrint(true))).addXMLAttribute("file", argSources[nos]).addXMLAttribute("class", fqClass).addElement("it is not a subclass of any Question class!"));
                             } else if (fqClass.matches(".*ModelAnswer[0-9]+")) {
                                 feedback.addElement(((XML)(new XML("ignored").setPrettyPrint(true))).addXMLAttribute("file", argSources[nos]).addXMLAttribute("class", fqClass).addElement("Answer class names may **not** start with the name `ModelAnswer`!"));
                             } else {
                                 answerClass.add(clazz);
                             } // end of if-then-else
                         } catch(NoClassDefFoundError exn) {
                             feedback.addElement(warning(argSources[nos], "Could not find the class " + fqClass + " [" + exn.getMessage() + "]"));
                         } catch(ClassNotFoundException exn) {
                             feedback.addElement(warning(argSources[nos], "Could not load the class " + fqClass + " [" + exn.getMessage() + "]"));
                         } catch(Throwable exn) {
                             feedback.addElement(warning(argSources[nos], "An error occurred in loading the class " + fqClass + " [" + exn.getMessage() + "]"));
                         } // end of try-catch
                     } // end of for-loop
                 } // end of if-then-else
             } // end of for-loop
             if (answerClass.isEmpty()) {
                 feedback.addElement(warning("Could not find any answer classes!"));
             } else {
                 Set<Question> answers = new HashSet<Question>();
                 for(Class clazz: answerClass) {
                     if (clazz != null) {
                         try {
                             if (Question1.class.isAssignableFrom(clazz)) {
                                 try {
                                     answers.add((Question)(clazz.getConstructor(new Class[]{Object.class}).newInstance(new Object[]{null})));
                                 } catch(NoSuchMethodException exn) {
                                     feedback.addElement(warning(clazz.getName(), "The required constructor function does not exist, so we could not create an instance of `" + clazz.getName() + "`. Check that:\n* your constructor functions are public\n* and that they have the correct signatures (see the `Question1` class)."));
                                 } catch(java.lang.reflect.InvocationTargetException exn) {
                                     feedback.addElement(warning(clazz.getName(), "The required constructor function does not exist, so we could not create an instance of `" + clazz.getName() + "`. Check that:\n* your constructor functions are public\n* and that they have the correct signatures (see the `Question1` class)."));
                                 } // end of try-catch
                             } else {
                                 if (Question2.class.isAssignableFrom(clazz)) {
                                     try {
                                         answers.add((Question)(clazz.getConstructor(new Class[]{Question1.class}).newInstance(new Question1[]{null})));
                                     } catch(NoSuchMethodException exn) {
                                         feedback.addElement(warning(clazz.getName(), "The required constructor function does not exist, so we could not create an instance of `" + clazz.getName() + "`. Check that:\n* your constructor functions are public\n* and that they have the correct signatures (see the `Question2` class)."));
                                     } catch(java.lang.reflect.InvocationTargetException exn) {
                                         feedback.addElement(warning(clazz.getName(), "The required constructor function does not exist, so we could not create an instance of `" + clazz.getName() + "`. Check that:\n* your constructor functions are public\n* and that they have the correct signatures (see the `Question2` class)."));
                                     } // end of try-catch
                                 } else {
                                     if (Question3.class.isAssignableFrom(clazz)) {
                                         try {
                                             answers.add((Question)(clazz.getConstructor(new Class[]{Question2.class}).newInstance(new Question2[]{null})));
                                         } catch(NoSuchMethodException exn) {
                                             feedback.addElement(warning(clazz.getName(), "The required constructor function does not exist, so we could not create an instance of `" + clazz.getName() + "`. Check that:\n* your constructor functions are public\n* and that they have the correct signatures (see the `Question3` class)."));
                                         } catch(java.lang.reflect.InvocationTargetException exn) {
                                             feedback.addElement(warning(clazz.getName(), "The required constructor function does not exist, so we could not create an instance of `" + clazz.getName() + "`. Check that:\n* your constructor functions are public\n* and that they have the correct signatures (see the `Question3` class)."));
                                         } // end of try-catch
                                     } else {
                                         feedback.addElement(warning(clazz.getName(), clazz.getName() + " is not recognised!"));
                                     } // end of if-then-else
                                 } // end of if-then-else
                             } // end of if-then-else
                         } catch(IllegalAccessException exn) {
                             feedback.addElement(warning(clazz.getName(), "Could not create an instance of " + clazz.getName()));
                         } catch(InstantiationException exn) {
                             feedback.addElement(warning(clazz.getName(), "Could not create an instance of " + clazz.getName()));
                         } catch(Throwable exn) {
                             feedback.addElement(warning(clazz.getName(), "Could not create an instance of " + clazz.getName()));
                         } // end of try-catch
                     } // end of if-then
                 } // end of for-loop
                 FeedbackResult results = new CourseworkTests(answers);
                 feedback = results.toXML(feedback);
             } // end of if-then-else
         } // end of if-then-else
         outputF.write(feedback.toString());
         outputF.close();
         System.out.println("... finished!");
     } // end of main method
 
     private static XML error(String msg) {
         return ((XML)(new XML("error").setPrettyPrint(true))).addElement(msg);
     } // end of method usage
 
     private static XML warning(String msg) {
         return ((XML)(new XML("warning").setPrettyPrint(true))).addElement(msg);
     } // end of method usage
     
     private static XML warning(String name, String msg) {
         return ((XML)(new XML("warning").setPrettyPrint(true))).addXMLAttribute("file", name).addElement(msg);
     } // end of method usage
 
     private static class SourceVisitor extends VoidVisitorAdapter {
         private String pkg = "";
         private Set<String> classes = new HashSet<String>();
 
         public void visit(PackageDeclaration n, Object arg) {
             pkg = n.getName().getName() + ".";
         } // end of method visit
 
         public void visit(ClassOrInterfaceDeclaration n, Object arg) {
             classes.add(pkg + n.getName());
         } // end of method visit
 
         public Set<String> getClasses() {
             return classes;
         } // end of method getClasses
     } // end of inner class SourceVisitor
 
 } // end of class CommandLine
