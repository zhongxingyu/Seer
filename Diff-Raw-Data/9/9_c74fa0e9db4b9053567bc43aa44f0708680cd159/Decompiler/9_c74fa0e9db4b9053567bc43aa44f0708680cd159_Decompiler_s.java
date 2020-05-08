 package com.sdc;
 
 import com.beust.jcommander.JCommander;
 import com.sdc.java.JavaClassVisitor;
 import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
 //import org.objectweb.asm.util.TraceClassVisitor;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 //import java.io.InputStream;
 //import java.io.PrintWriter;
 
 //import com.sdc.cpp.CppClassVisitor;
 
 public class Decompiler {
     public static void main(String[] args) throws IOException {
         DecompilerParameters decompilerParameters = new DecompilerParameters();
         final JCommander jCommander = new JCommander(decompilerParameters, args);
 
         if (decompilerParameters.isHelp()) {
             jCommander.usage();
             return;
         }
 
         if (decompilerParameters.getClassName() == null && decompilerParameters.getClassPath() == null) {
             System.out.println("One of the following option is required: -c, -p. Use --help for more usage information.");
             return;
         }
 
         final int tabSize = decompilerParameters.getTabSize();
         final int textWidth = decompilerParameters.getTextWidth();
 
         ClassReader cr;
 
         if (decompilerParameters.getClassName() != null) {
             cr = new ClassReader(decompilerParameters.getClassName());
         } else {
             cr = new ClassReader(new FileInputStream(decompilerParameters.getClassPath()));
         }
 
        ClassVisitor cv;
 //        ClassVisitor cv = new TraceClassVisitor(new PrintWriter(System.out));
 
         if (decompilerParameters.getLanguage().equals("java")) {
             cv = new JavaClassVisitor(textWidth, tabSize);
         }/* else if (decompilerParameters.getLanguage().equals("cpp")) {
             cv = new CppClassVisitor(textWidth, tabSize);
         }*/ else {
             System.out.println("Specify one of the valid output language. Use --help for more usage information.");
             return;
         }
 
         cr.accept(cv, 0);
     }
 }
