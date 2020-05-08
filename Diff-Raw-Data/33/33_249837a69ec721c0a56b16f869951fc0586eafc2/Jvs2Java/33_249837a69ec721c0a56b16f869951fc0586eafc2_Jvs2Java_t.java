 /**************************************************************
 * Philippe VIENNE, Copyright (C) 2011.  All rights reserved. *
 **************************************************************/
 package org.javascool.core;
 
 // Used to report a throwable
 import java.lang.reflect.InvocationTargetException;
 
 /** Implémente le mécanisme de base de traduction d'un code Jvs en code Java standard.
  * <p>Les erreurs de traduction sont affichées dans la console.</p>
  *
  * @see <a href="Jvs2Java.java.html">source code</a>
  * @serial exclude
  */
 public class Jvs2Java extends Translator {
   // @bean
   public Jvs2Java() {}
 
   /** Définit un mécanisme spécifique de traduction en plus du mécanisme standard.
    * @param progletTranslator Le mécanisme de traduction spécifique d'une proglet donnée.
    * @return Cet objet, permettant de définir la construction <tt>Jvs2Java translator = new Jvs2Java().setProgletTranslator(..)</tt>.
    */
   public Jvs2Java setProgletTranslator(Translator progletTranslator) {
     // @bean-parameter(Translator, progletTranslator, w);
     this.progletTranslator = progletTranslator;
     return this;
   }
   private Translator progletTranslator = null;
 
   /** Définit le nom complet du package de la proglet pour ce mécanismes de traduction.
    * @param progletPackageName Le nom complet du package de la proglet.
    * @return Cet objet, permettant de définir la construction <tt>Jvs2Java translator = new Jvs2Java().setProgletPackageName(..)</tt>.
    */
   public Jvs2Java setProgletPackageName(String progletPackageName) {
     // @bean-parameter(String, progletPackageName, w);
     this.progletPackageName = progletPackageName;
     return this;
   }
   private String progletPackageName = null;
 
   @Override
   public String translate(String jvsCode) {
     String text = jvsCode.replace((char) 160, ' ');
     // Ici on ajoute
     if(!text.replaceAll("[ \n\r\t]+", " ").matches(".*void[ ]+main[ ]*\\([ ]*\\).*")) {
       if(text.replaceAll("[ \n\r\t]+", " ").matches(".*main[ ]*\\([ ]*\\).*")) {
         System.out.println("Attention: il faut mettre \"void\" devant \"main()\" pour que le programme puisque se compiler");
         text = text.replaceFirst("main[ ]*\\([ ]*\\)", "void main()");
       } else {
         System.out.println("Attention: il faut un block \"void main()\" pour que le programme puisque se compiler");
         text = "\nvoid main() {\n" + text + "\n}\n";
       }
     }
     String[] lines = text.split("\n");
     StringBuilder head = new StringBuilder();
     StringBuilder body = new StringBuilder();
     // Here is the translation loop
     {
       int i = 1;
       // Copies the user's code
       for(String line : lines) {
         if(line.matches("^\\s*(import|package)[^;]*;\\s*$")) {
           head.append(line);
           body.append("//").append(line).append("\n");
           if(line.matches("^\\s*package[^;]*;\\s*$"))
             System.out.println("Attention: on ne peut normalement pas définir de package Java en JavaScool\n le programme risque de ne pas s'exécuter correctement");
         } else
           body.append(line).append("\n");
         i++;
       }
       // Imports proglet's static methods
       head.append("import static java.lang.Math.*;");
       head.append("import static org.javascool.macros.Macros.*;");
       head.append("import static org.javascool.macros.Stdin.*;");
       head.append("import static org.javascool.macros.Stdout.*;");
       if(progletPackageName != null)
         head.append("import static ").append(progletPackageName).append(".Functions.*;");
       if(progletTranslator != null)
         head.append(progletTranslator.getImports());
       // Declares the proglet's core as a Runnable in the Applet
       uid++;
       head.append("public class JvsToJavaTranslated").append(uid).append(" implements Runnable{");
       head.append("  private static final long serialVersionUID = ").append(uid).append("L;");
       head.append("  public void run() {");
       head.append("   try{ main(); } catch(Throwable e) { ");
       head.append("    if (e.toString().matches(\".*Interrupted.*\"))System.out.println(\"\\n-------------------\\nProggramme arrêté !\\n-------------------\\n\");");
       head.append("    else System.out.println(\"\\n-------------------\\nErreur lors de l'exécution de la proglet\\n\"+org.javascool.core.Jvs2Java.report(e)+\"\\n-------------------\\n\");}");
       head.append("}");
     }
     String finalBody = body.toString().
                       //- replaceAll("repeat\\(([^\\)]*)\\)", "/* @repeat */ for(int jvsInternalCounter = 0; jvsInternalCounter < $1; jvsInternalCounter++)").
                        replaceAll("(while.*\\{)", "$1 sleep(1);");
     if(progletTranslator != null)
         finalBody = progletTranslator.translate(finalBody);
     System.err.println(
                 "\n-------------------\nCode java généré\n-------------------\n" +
                 head.toString().replaceAll("([{;])", "$1\n") + "\n" + finalBody + "}" +
                 "\n----------------------------------------------------------\n");
     return head.toString() + finalBody + "}";
   }
   /** Renvoie le nom de la dernière classe Java générée lors de la traduction. */
   public String getClassName() {
     return "JvsToJavaTranslated" + uid;
   }
   // Counter used to increment the serialVersionUID in order to reload the different versions of the class
   private static int uid = 0;
 
   /** Rapporte une erreur survenue lors de l'exécution d'un prograamme Jvs.
    * @param error L'erreur ou exception à rapporter.
    * @return Le rapport d'erreur.
    */
   public static String report(Throwable error) {
     if(error instanceof InvocationTargetException)
       return report(error.getCause());
     String s = error.toString()+"\n";
     for(int i = 0; i < 4 && i < error.getStackTrace().length; i++) {
       String s_i = "" + error.getStackTrace()[i];
       if (s_i.startsWith("JvsToJavaTranslated"))
 	s += s_i.replaceFirst("JvsToJavaTranslated[0-9]*", "") + "\n";
     }
     return s;
   }
 
   /** Lanceur de la conversion Jvs en Java.
    * @param usage <tt>java org.javascool.core.Jvs2Java input-file [output-file]</tt>
    */
   public static void main(String[] usage) {
     // @main
     if(usage.length > 0)
       org.javascool.tools.FileManager.save(usage.length > 1 ? usage[1] : "stdout:", new Jvs2Java().translate(org.javascool.tools.FileManager.load(usage[0])));
   }
 }
