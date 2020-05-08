 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: CLIDefinitionGenerator.java,v 1.1 2006-05-31 21:50:08 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.cli.tools;
 
 
 import com.sun.identity.cli.annotation.DefinitionClassInfo;
 import com.sun.identity.cli.annotation.Macro;
 import com.sun.identity.cli.annotation.ResourceStrings;
 import com.sun.identity.cli.annotation.SubCommandInfo;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 /**
  * This class generates a CLI Definition class from another annotated class.
  */
 public class CLIDefinitionGenerator {
     private static Map<String, String> mapLongToShortOptionName = 
         new HashMap<String, String>();
 
     /**
      * Generates CLI Definition class.
      *
      * @param argv Array of annotated class names.
      */
     public static void main(String[] argv) {
        String srcDirectory = argv[0];
 
         for (int i = 1; i < argv.length; i++) {
             String className = argv[i];
 
             try {
                 Class clazz = Class.forName(className);
                 Field pdtField = clazz.getDeclaredField("product");
 
                 if (pdtField != null) {
                     DefinitionClassInfo classInfo = pdtField.getAnnotation(
                         DefinitionClassInfo.class);
 
                     try {
                         PrintStream rbOut = createResourcePrintStream(
                            srcDirectory, classInfo);
                         getCommonResourceStrings(rbOut, clazz);
                         rbOut.println("product-name=" +
                             classInfo.productName());
                         getCommands(className, clazz, rbOut);
                         rbOut.close();
                     } catch (IOException ioe) {
                         ioe.printStackTrace();
                     }
                 } else {
                     throw new Exception("Incorrect Definiton, " +
                         "class=" + className + " missing product field"); 
                 }
             } catch(Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     private static void getCommonResourceStrings(
         PrintStream rbOut,
         Class clazz
     ) throws Exception {
         Field field = clazz.getDeclaredField("resourcestrings");
 
         if (field != null) {
             ResourceStrings resStrings = field.getAnnotation(
                 ResourceStrings.class);
             rbOut.println(resStrings.string());
         }
     }
 
     private static PrintStream createResourcePrintStream(
        String srcDirectory,
         DefinitionClassInfo classInfo
     ) throws IOException {
        int idx = srcDirectory.lastIndexOf(File.separatorChar);
        String dir = srcDirectory.substring(0, idx+1) + "resources";
        String filePath = dir + File.separator + classInfo.resourceBundle() +
            ".properties";
         File rbFile = new File(filePath);
         rbFile.createNewFile();
         FileOutputStream rbStream = new FileOutputStream(rbFile);
         return new PrintStream(rbStream);
     }
 
     private static void getCommands(
         String className,
         Class clazz,
         PrintStream rbOut
     ) throws Exception {
         Field[] fields = clazz.getDeclaredFields();
         for (Field fld : fields) {
             SubCommandInfo info = fld.getAnnotation(SubCommandInfo.class);
             if (info != null) {
                 if ((info.implClassName() == null) ||
                     (info.description() == null)
                 ) {
                     throw new Exception("Incorrect Definition, " +
                         "class=" + className + " field=" + fld.toString());
                 }
 
                 String mandatoryOptions = info.mandatoryOptions();
                 String optionalOptions = info.optionalOptions();
                 String optionAliases = info.optionAliases();
 
                 if ((info.macro() != null) && (info.macro().length() > 0)) {
                     Field fldMarco = clazz.getDeclaredField(info.macro());
                     Macro macroInfo =(Macro)fldMarco.getAnnotation(Macro.class);
                     mandatoryOptions += "@" + macroInfo.mandatoryOptions();
                     optionalOptions += "@" + macroInfo.optionalOptions();
                     optionAliases += "@" + macroInfo.optionAliases();
                 }
                 
                 validateOption(mandatoryOptions);
                 validateOption(optionalOptions);
 
                 String subcmdName = fld.getName().replace('_', '-');
 
                 String resPrefix = "subcmd-" + subcmdName;
                 rbOut.println(resPrefix +  "=" + info.description());
                 createResourceForOptions(resPrefix + "-",
                     mandatoryOptions, rbOut);
                 createResourceForOptions(resPrefix + "-",
                     optionalOptions, rbOut);
                 addResourceStrings(info.resourceStrings(), rbOut);
             }
         }
     }
     
     private static void validateOption(String options) {
         StringTokenizer st = new StringTokenizer(options, "@");
         while (st.hasMoreTokens()) {
             String token = st.nextToken();
             int idx = token.indexOf('|');
             String longName = token.substring(0, idx);
             String shortName = token.substring(idx+1, idx+2);
 
             String test = (String)mapLongToShortOptionName.get(longName);
             if (test == null) {
                 mapLongToShortOptionName.put(longName, shortName);
             } else if (!test.equals(shortName)) {
                 throw new RuntimeException(
                     "Mismatched names: " + longName + "-> " + test + ", " +
                     shortName);
             }
         }
     }
 
     private static void addResourceStrings(String res, PrintStream rbOut) {
         StringTokenizer st = new StringTokenizer(res, "\n");
         while (st.hasMoreTokens()) {
             rbOut.println(st.nextToken());
         }
     }
 
     private static void createResourceForOptions(
         String prefix,
         String options,
         PrintStream rbOut
     ) {
         StringTokenizer st = new StringTokenizer(options, "@");
         while (st.hasMoreTokens()) {
             StringTokenizer t = new StringTokenizer(st.nextToken(), "|");
             String opt = t.nextToken();
             String shortOpt = t.nextToken();
             String optionType = t.nextToken();
             String description = t.nextToken();
 
             rbOut.println(prefix + opt + "=" + description);
         }
     }
 }
