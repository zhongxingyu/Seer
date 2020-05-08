 /******************************************************************************
  * DeploySOLODHTML.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004, 2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.utils;
 
 import java.net.*;
 import java.util.*;
 import java.util.regex.*;
 import java.util.zip.*;
 import java.io.*;
 import java.text.SimpleDateFormat;
 import org.openlaszlo.utils.FileUtils.*;
 import org.openlaszlo.xml.internal.XMLUtils.*;
 import org.openlaszlo.compiler.*;
 import org.openlaszlo.server.LPS;
 
 
 
 public class DeployMain {
 
     private static String[] USAGE = {
         "Usage: lzdeploy [OPTION]... FILE...",
         "",
         "Options:",
         "-D<name>=<value>",
         "  Set the name/var property to value (See Compiler.getProperties).",
         "-D<name>",
         "  Short for -Dname=true.",
         "--wrapperonly",
         "  Only emit html wrapper page.",
        "--runtime=[swf7|swf8|swf9|dhtml]",
        "  Compile to swf7, swf8, swf9, dhtml",
         "--output pathname",
         "  The name of the output file to write.",
         "--title titlestring",
         "  The title of the application to use in the wrapper.",
         "-v",
         "  Write progress information to standard output.",
         "--help",
         "  Prints this message.",
 
     };
 
     public static void usage () {
         System.err.println("Usage: lzdeploy [OPTION]... file...");
         System.err.println("Try `lzdeploy --help' for more information.");
         System.exit(1);
     }
 
     static boolean verbose = false;
 
     public static void main(String args[])
       throws IOException
     {
 
         boolean wrapperonly = false;
         String outfile = null;
         String sourcepath = null;
         String title = null;;
         String runtime = "dhtml";
 
         Properties compilationProperties =  new Properties();
         for (int i = 0; i < args.length; i++) {
             String arg = args[i].intern();
             if (arg.startsWith("-")) {
                 if (arg.startsWith("--runtime=")) {
                     String value = arg.substring("--runtime=".length());
                     runtime = value;
                     if ((new HashSet(org.openlaszlo.compiler.Compiler.KNOWN_RUNTIMES)).contains(value)) {
                         compilationProperties.setProperty(CompilationEnvironment.RUNTIME_PROPERTY, value);
                     } else {
                         System.err.println("Invalid value for --runtime");
                         System.exit(1);
                     }
                 } else if (arg == "--output") {
                     outfile = args[++i];
                 } else if (arg == "--title") {
                     title = args[++i];
                 } else if (arg == "-v") {
                     verbose = true;
                 } else if (arg == "--wrapperonly") {
                     wrapperonly = true;
                 } else if (arg.startsWith("-D")) {
                     String key = arg.substring(2);
                     String value = "true";
                     int offset = key.indexOf('=');
                     if (offset >= 0) {
                         value = key.substring(offset + 1).intern();
                         key = key.substring(0, offset);
                     }
                     compilationProperties.setProperty(key, value);
                     LPS.setProperty(key, value);
                 } else if (arg == "--help") {
                     for (int j = 0; j < USAGE.length; j++) {
                         System.err.println(USAGE[j]);
                     }
                     System.exit(0);
                 } else {
                     usage();
                 }
                 continue;
             }
             sourcepath = args[i];
         }
 
         if (sourcepath == null) {
             usage();
         }
 
         if (title == null) {
             File sourcefile = new File(sourcepath);
             title = sourcefile.getName();
         }
 
         if (outfile == null) {
             outfile = sourcepath + ".zip";
         }
 
         if (sourcepath == null) {
             usage();
         }
 
         // Create  a directory in /tmp to assemble SOLO deploy assets
         File tmpdir = File.createTempFile("foo", "bar").getParentFile();
 
         if (verbose) {
             System.err.println(
             "DeploySOLODHTML.deploy(wrapperonly: "+wrapperonly + 
             " null,\n"+
             " null,\n" + 
             " null,\n "+
             " sourcepath: "+sourcepath+"\n"+
             "new FileOutputStream("+outfile+"),\n"+
             "tmpdir: "+tmpdir+",\n"+
             "title: "+title+",\n"+
             compilationProperties);
         }
 
         // Don't add the output zip file to the new zip archive!
         HashMap skipfiles = new HashMap();
         skipfiles.put(new File(outfile).getCanonicalPath(), "true");
 
         try {
             if ("dhtml".equals(runtime)) {
                 System.exit(DeploySOLODHTML.deploy(wrapperonly,
                                                    null,
                                                    null,
                                                    null,
                                                    sourcepath,
                                                    new FileOutputStream(outfile),
                                                    tmpdir,
                                                    title,
                                                    compilationProperties,
                                                    skipfiles));
            } else if ("swf10".equals(runtime) || "swf8".equals(runtime) || "swf7".equals(runtime)) {
                 System.exit(DeploySOLOSWF.deploy(runtime,
                                                  wrapperonly,
                                                    null,
                                                    null,
                                                    null,
                                                    sourcepath,
                                                    new FileOutputStream(outfile),
                                                    tmpdir,
                                                    title,
                                                    compilationProperties,
                                                    skipfiles));
             } else {
                 System.err.println("SOLO deployment for runtime "+runtime+" is not yet supported.");
                 System.exit(1);
             }
         } catch (IOException e) {
             System.err.println(e);
             System.exit(1);
         }
             
     }
 
 }
