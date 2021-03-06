 /* Generated By:JavaCC: Do not edit this line. mas2j.java */
 package jIDE.parser;
 
 import java.util.*;
 import java.io.*;
 
 public class mas2j implements mas2jConstants {
 
     PrintWriter out;
     boolean noOut = false; // do not output anything to files, just parse
     String soc;
     String envClass;
     String controlClass;
     boolean debug = false;
 
     String architecture = "Centralised";
     String destDir  = "."+File.separator;
     String saciJar  = "saci.jar";
     String saciHome = null;
     String jasonJar = "jason.jar";
     String log4jJar = "log4j.jar";
     String javaHome = File.separator;
 
     String controlPart = null; // this string has the ag control creation (to be added at the end of the script)
 
     Map    agArchFiles;
     Map    agClassFiles;
     Map    agASFiles;
 
     // Run the parser
     public static void main (String args[]) {
 
       String name;
       mas2j parser;
 
       if (args.length==2) {
         name = args[0];
         System.err.println("mas2j: reading from file " + name + " ..." );
                 try {
                   parser = new mas2j(new java.io.FileInputStream(name));
           parser.setJasonJar(args[1]+"/bin/jason.jar");
           parser.setLog4jJar(args[1]+"/lib/log4j.jar");
           parser.setSaciJar(args[1]+"/lib/saci.jar");
           parser.setDestDir(new File(".").getAbsolutePath());
                 } catch(java.io.FileNotFoundException e){
                   System.err.println("mas2j: file \"" + name + "\" not found.");
                   return;
         }
       } else {
                 System.out.println("mas2j: usage must be:");
                 System.out.println("      java mas2j <MASConfFile> <JasonHome>");
                 System.out.println("Output to file <MASName>.xml");
         return;
       }
 
       // parsing
       try {
                 parser.mas();
                 System.out.println("mas2j: "+name+" parsed successfully!\n");
         parser.writeScripts();
 
         int step = 1;
         System.out.println("To run your MAS:");
         //System.out.println("  1. chmod u+x *.sh");
         System.out.println("  "+step+". compile the java files (script ./compile-"+parser.soc+".sh)");
         step++;
         if (parser.architecture.equals("Saci")) {
              System.out.println("  "+step+". run saci (script ./saci-"+parser.soc+".sh)");
              step++;
         }
         System.out.println("  "+step+". run your agents (script ./"+parser.soc+".sh)");
       }
       catch(ParseException e){
                 System.err.println("mas2j: parsing errors found... \n" + e);
       }
     }
 
     public void setDestDir(String d) {
         if (d != null) {
             destDir = d;
             if (destDir.length() > 0) {
                 if (! destDir.endsWith( File.separator )) {
                     destDir += File.separator;
                 }
             }
         }
     }
 
     public void setNoOut(boolean noOut) {
         this.noOut = noOut;
     }
 
     public String getOutputFile() {
         return destDir+soc+".xml";
     }
 
     public void setOut(String id) {
         try {
             if (noOut) {
                 out = new PrintWriter(new StringWriter());
                 //System.out.println("mas2j: output to null"); 
             } else {
                 out = new PrintWriter(new FileWriter(destDir+id+".xml"));
                 //System.out.println("mas2j: output to file " + id + ".xml ..."); 
             }
         } catch (Exception e) {
             System.err.println("mas2j: could not open " + id + ".xml\".");
         }
     }
 
     public void close() {
                 out.close();
     }
 
     public void setJasonJar(String s) {
         jasonJar = s;
     }
     public void setLog4jJar(String s) {
         log4jJar = s;
     }
     public void setSaciJar(String s) {
         saciJar = s;
         try {
                 saciHome = new File(saciJar).getParent().toString();
         } catch (Exception e) {
                 saciHome = null;
         }
     }
 
     public void setJavaHome(String s) {
         if (s != null) {
                 javaHome = new File(s).getAbsolutePath();
                 if (! javaHome.endsWith(File.separator)) {
                         javaHome += File.separator;
                 }
             }
     }
 
     public String getArchitecture() {
         return architecture;
     }
 
     public String getEnvClass() {
         return envClass;
     }
 
     public String getSocName() {
         return soc;
     }
 
     public String getControlClass() {
         return controlClass;
     }
 
     public void setControlClass(String sControl) {
                 controlClass = sControl;
     }
 
     public void debugOn() {
                 debug = true;
     }
 
     public void debugOff() {
                 debug = false;
     }
 
     public Map getAgArchFiles() {
         return agArchFiles;
     }
 
     public Map getAgClassFiles() {
         return agClassFiles;
     }
 
     public Map getAgASFiles() {
         return agASFiles;
     }
 
     public Set getAllUserJavaFiles() {
                                 Set files = new HashSet();
                                 files.addAll(getAgArchFiles().values());
                                 files.addAll(getAgClassFiles().values());
                                 if (getEnvClass() != null) {
                                         files.add(getEnvClass().replace('.', '/'));
                                 }
                                 return files;
         }
 
     public Set getAllUserJavaDirectories() {
                                 Set directories = new HashSet();
                                 Iterator ifiles = getAllUserJavaFiles().iterator();
                                 while (ifiles.hasNext()) {
                                         String dir = new File(ifiles.next()+".java").getParent();
                                         if (dir == null) { // no parent
                                                 dir = ".";
                                         }
                                         directories.add(dir);
                                 }
                                 return directories;
         }
 
     public void writeInit() {
             out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         String extraSlash = "";
         if (System.getProperty("os.name").indexOf("indows") > 0) {
             extraSlash = "/";
         }
         //if (saciHome != null) {
                 //out.println("<!DOCTYPE saci SYSTEM \"file:"+extraSlash+saciHome+File.separator+"applications.dtd\">");
                     //out.println("<?xml-stylesheet href=\"file:"+extraSlash+saciHome+File.separator+"applications.xsl\" type=\"text/xsl\" ?>");
                 //} else {
                     out.println("<?xml-stylesheet href=\"http://www.inf.furb.br/~jomi/jason/saci/applications.xsl\" type=\"text/xsl\" ?>");
                 //}
         out.println("<saci>");
                 out.println("<application id=\""+ soc +"\">");
 
                 out.println("<script id=\"run\">\n");
 
                 out.println("\t<killSocietyAgents society.name=\""+ soc +"\" />");
                 out.println("\t<killFacilitator society.name=\""+ soc +"\" />");
                 out.println("\t<startSociety society.name=\""+ soc +"\" />\n");
 
                 out.println("\t<killSocietyAgents society.name=\""+ soc +"-env\" />");
                 out.println("\t<killFacilitator society.name=\""+ soc +"-env\" />");
                 out.println("\t<startSociety society.name=\""+ soc +"-env\" />\n");
     }
 
     public void writeEnd() {
         if (controlPart != null) {
                 out.println(controlPart);
             }
                 out.println("\n</script>");
                 out.println("</application>");
             out.println("</saci>");
     }
 
     public String getFullClassPath() {
         String clPath = "\"$CLASSPATH\"";
         String indelim = "\"";
         String outdelim = "";
         if (System.getProperty("os.name").indexOf("indows") > 0) {
             clPath = "%CLASSPATH%";
                 indelim = "";
                     outdelim = "\"";
         }
 
                 String dDir = destDir;
                 if (dDir.endsWith(File.separator)) {
                         dDir = dDir.substring(0, dDir.length()-1);
                 }
         return outdelim+
                "."+File.pathSeparator+
                indelim+jasonJar+indelim+File.pathSeparator+
                indelim+saciJar+indelim+File.pathSeparator+
                indelim+log4jJar+indelim+File.pathSeparator+
                indelim+dDir+indelim+File.pathSeparator+
                clPath+
                outdelim;
     }
 
     public void writeScripts() {
         try {
 
                         String classPath = getFullClassPath();
 
                         String dirsToCompile = "";
                         Iterator i = getAllUserJavaDirectories().iterator();
                         while (i.hasNext()) {
                                 dirsToCompile += " " + i.next() + File.separator + "*.java";
                         }
 
 
                 PrintWriter out;
 
             // -- windows scripts
             if (System.getProperty("os.name").indexOf("indows") > 0) {
                 out = new PrintWriter(new FileWriter(destDir+soc+".bat"));
                 out.println("@echo off\n");
                 out.println("rem this file was generated by mas2j parser\n");
                 if (javaHome != null) {
                         out.println("set PATH="+javaHome+"bin;%PATH%\n");
                 }
                 if (architecture.equals("Saci")) {
                     out.println("java -classpath "+classPath+" "+saci.tools.runApplicationScript.class.getName()+" \""+soc+".xml\"");
                 } else if (architecture.equals("Centralised")) {
                     out.println("java -classpath "+classPath+" "+jIDE.RunCentralisedMAS.class.getName()+" \""+soc+".xml\" ");
                 }
                 out.close();
 
 
                 out = new PrintWriter(new FileWriter(destDir+"compile-"+soc+".bat"));
                 out.println("@echo off\n");
                 out.println("rem  this file was generated by mas2j parser\n");
                 if (javaHome != null) {
                         out.println("set PATH="+javaHome+"bin;%PATH%\n");
                 }
                 if (dirsToCompile.length() > 0) {
                         out.println("echo compiling user classes...");
                         out.println("javac -classpath "+classPath+" "+dirsToCompile+"\n\n");
                     } else {
                         out.println("echo no files to compile...");
                     }
                 out.println("echo ok");
                 out.close();
 
 
                 if (architecture.equals("Saci")) {
                     out = new PrintWriter(new FileWriter(destDir+"saci-"+soc+".bat"));
                     out.println("@echo off");
                     out.println("rem this file was generated by mas2j parser\n");
                     if (javaHome != null) {
                         out.println("set PATH="+javaHome+"bin;%PATH%\n");
                     }
                     out.println("set CLASSPATH="+classPath+"\n");
                     //out.println("cd \""+saciHome+"\"");
                     //out.println("saci &"); 
                    out.println("java -Djava.security.policy=jar:file:"+saciJar+"!/policy saci.tools.SaciMenu");
                     out.close();
                 }
             } else {
                 // ---- unix scripts
                 // the script to run the MAS                   
                 out = new PrintWriter(new FileWriter(destDir+soc+".sh"));
                 out.println("#!/bin/sh\n");
                 out.println("# this file was generated by mas2j parser\n");
                 if (javaHome != null) {
                         out.println("export PATH="+javaHome+"bin:$PATH\n");
                 }
                 if (architecture.equals("Saci")) {
                     out.println("java -classpath "+classPath+" "+saci.tools.runApplicationScript.class.getName()+" \""+soc+".xml\"");
                 } else if (architecture.equals("Centralised")) {
                     out.println("java -classpath "+classPath+" "+jIDE.RunCentralisedMAS.class.getName()+" \""+soc+".xml\"");
                 }
                 out.close();
 
 
                 //out = new PrintWriter(new FileWriter(destDir+"c"+soc+".sh"));
                 out = new PrintWriter(new FileWriter(destDir+"compile-"+soc+".sh"));
                 out.println("#!/bin/sh\n");
                 out.println("# this file was generated by mas2j parser\n");
                 if (javaHome != null) {
                         out.println("export PATH="+javaHome+"bin:$PATH\n");
                 }
                 if (dirsToCompile.length() > 0) {
                         out.println("echo -n \"        compiling user classes...\"");
                          out.println("# compile files "+getAllUserJavaFiles());
                          out.println("# on "+getAllUserJavaDirectories());
                         out.println("javac -classpath "+classPath+" "+dirsToCompile+"\n");
                     } else {
                         out.println("echo -n \"        no files to compile...\"");
                     }
                 out.println("chmod u+x *.sh");
                 out.println("echo ok");
                 out.close();
 
 
                 if (architecture.equals("Saci")) {
                     out = new PrintWriter(new FileWriter(destDir+"saci-"+soc+".sh"));
                     out.println("#!/bin/sh");
                     out.println("# this file was generated by mas2j parser\n");
                     if (javaHome != null) {
                         out.println("export PATH="+javaHome+"bin:$PATH\n");
                     }
                     //out.println("CURDIR=`pwd`");
                     //out.println("cd "+destDir);
                     //out.println("APPDIR=`pwd`");
                     //out.println("export CLASSPATH=$APPDIR:$CURDIR:"+classPath);
                     out.println("export CLASSPATH="+classPath+"\n");
                     //out.println("cd \""+saciHome+"\""); 
                     //out.println("./saci &"); 
                    out.println("java -Djava.security.policy=jar:file:"+saciJar+"!/policy saci.tools.SaciMenu");
                     out.close();
                 }
             }
         } catch (Exception e) {
             System.err.println("mas2j: could not write " + soc + ".sh");
             e.printStackTrace();
         }
     }
 
     String getOptsStr(Map opts) {
         String s = "";
         Iterator i = opts.keySet().iterator();
         while (i.hasNext()) {
             if (s.length() == 0) {
                 s = " options ";
             }
             String key = (String)i.next();
             s += key + "=" + opts.get(key);
             if (i.hasNext()) {
                 s += ",";
             }
         }
         return s;
     }
 
 /* Configuration Grammar */
   final public void mas() throws ParseException {
     jj_consume_token(MAS);
     soc = sId();
                                  setOut(soc);
                                  writeInit();
     jj_consume_token(34);
     infra();
     environment();
     control();
     agents();
     jj_consume_token(35);
                                  writeEnd();
                                  close();
   }
 
   final public void infra() throws ParseException {
                                  Token t;
                                  architecture = "Centralised";
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case ARCH:
     case INFRA:
       switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
       case ARCH:
         jj_consume_token(ARCH);
                                  System.err.println("The id <architecture> was replaced by <infrastructure> in .mas2j syntax, please use the new id.");
         break;
       case INFRA:
         jj_consume_token(INFRA);
         break;
       default:
         jj_la1[0] = jj_gen;
         jj_consume_token(-1);
         throw new ParseException();
       }
       jj_consume_token(36);
       t = jj_consume_token(INFRAV);
                                  architecture = t.image;
       break;
     default:
       jj_la1[1] = jj_gen;
       ;
     }
   }
 
   final public void agents() throws ParseException {
                               agArchFiles = new HashMap();
                               agClassFiles = new HashMap();
                               agASFiles = new HashMap();
     jj_consume_token(AGS);
     jj_consume_token(36);
     label_1:
     while (true) {
       agent();
       switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
       case ASID:
         ;
         break;
       default:
         jj_la1[2] = jj_gen;
         break label_1;
       }
     }
   }
 
   final public void agent() throws ParseException {
                               Token agName;
                               File source;
                               Token qty; Token value; String host; Map opts;
     agName = jj_consume_token(ASID);
                               out.print("\t<startAgent ");
                               out.print("\n\t\tname=\""+agName.image+"\" ");
                               out.print("\n\t\tsociety.name=\""+soc+"\" ");
                               source = new File(destDir+agName.image+".asl");
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case ASID:
     case PATH:
       source = fileName();
       break;
     default:
       jj_la1[3] = jj_gen;
       ;
     }
     opts = ASoptions();
                               String agClass = jason.asSemantics.Agent.class.getName();
                               String agArchClass = jason.architecture.CentralisedAgArch.class.getName();
 
                               if (architecture.equals("Saci")) {
                                   agArchClass = jason.architecture.SaciAgArch.class.getName();
                               }
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case ASAGARCHCLASS:
       jj_consume_token(ASAGARCHCLASS);
       agArchClass = className();
                               agArchFiles.put(agName.image, agArchClass.replace('.', '/'));
       break;
     default:
       jj_la1[4] = jj_gen;
       ;
     }
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case ASAGCLASS:
       jj_consume_token(ASAGCLASS);
       agClass = className();
                               agClassFiles.put(agName.image, agClass.replace('.', '/'));
       break;
     default:
       jj_la1[5] = jj_gen;
       ;
     }
                               agASFiles.put(agName.image, source.getAbsolutePath());
                               out.print("\n\t\tclass=\""+agArchClass+"\"");
                               out.print("\n\t\targs=\""+agClass+" '"+source.getAbsolutePath()+"' "+getOptsStr(opts)+"\"");
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case 37:
       jj_consume_token(37);
       qty = jj_consume_token(NUMBER);
                               out.print("\n\t\tqty=\""+qty.image+"\" ");
       break;
     default:
       jj_la1[6] = jj_gen;
       ;
     }
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case AT:
       jj_consume_token(AT);
       host = sId();
                               out.print("\n\t\thost=\""+host+"\" ");
       break;
     default:
       jj_la1[7] = jj_gen;
       ;
     }
     jj_consume_token(38);
                               out.println("/>");
   }
 
   final public File fileName() throws ParseException {
                               String path = "";
                               Token t;
                               String i;
                               String ext = ".asl";
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case PATH:
       t = jj_consume_token(PATH);
                               path = t.image;
       break;
     default:
       jj_la1[8] = jj_gen;
       ;
     }
     i = sId();
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case 39:
       jj_consume_token(39);
       ext = sId();
                               ext = "." + ext;
       break;
     default:
       jj_la1[9] = jj_gen;
       ;
     }
                               if (!path.startsWith(File.separator)) {
                                 path = destDir + path;
                               }
                               {if (true) return new File( path + i + ext);}
     throw new Error("Missing return statement in function");
   }
 
   final public String className() throws ParseException {
                             Token c; String p = "";
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case CLASSID:
       c = jj_consume_token(CLASSID);
       break;
     case ASID:
       c = jj_consume_token(ASID);
       break;
     default:
       jj_la1[10] = jj_gen;
       jj_consume_token(-1);
       throw new ParseException();
     }
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case 39:
       jj_consume_token(39);
       p = className();
                              {if (true) return c.image + "." + p;}
       break;
     default:
       jj_la1[11] = jj_gen;
       ;
     }
                              {if (true) return c.image;}
     throw new Error("Missing return statement in function");
   }
 
   final public Map ASoptions() throws ParseException {
                              Map opts = new HashMap();
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case 40:
       jj_consume_token(40);
       opts = procOption(opts);
       label_2:
       while (true) {
         switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
         case 41:
           ;
           break;
         default:
           jj_la1[12] = jj_gen;
           break label_2;
         }
         jj_consume_token(41);
         opts = procOption(opts);
       }
       jj_consume_token(42);
       break;
     default:
       jj_la1[13] = jj_gen;
       ;
     }
     if (controlClass != null) {
       // if there is some control, all agents have sync option
       opts.put("synchronised","true");
     }
     if (debug) {
       opts.put("verbose", "2");
     }
     {if (true) return opts;}
     throw new Error("Missing return statement in function");
   }
 
   final public Map procOption(Map opts) throws ParseException {
                             Token opt; Token oval;
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case ASOEE:
       opt = jj_consume_token(ASOEE);
       jj_consume_token(43);
       oval = jj_consume_token(ASOEEV);
                                       opts.put(opt.image,oval.image);
       break;
     case ASOIB:
       opt = jj_consume_token(ASOIB);
       jj_consume_token(43);
       oval = jj_consume_token(ASOIBV);
                                       opts.put(opt.image,oval.image);
       break;
     case ASOSYNC:
       opt = jj_consume_token(ASOSYNC);
       jj_consume_token(43);
       oval = jj_consume_token(ASOBOOL);
                                       opts.put(opt.image,oval.image);
       break;
     case ASONRC:
       opt = jj_consume_token(ASONRC);
       jj_consume_token(43);
       oval = jj_consume_token(NUMBER);
                                       opts.put(opt.image,oval.image);
       break;
     case ASOV:
       opt = jj_consume_token(ASOV);
       jj_consume_token(43);
       oval = jj_consume_token(NUMBER);
                                       opts.put(opt.image,oval.image);
       break;
     default:
       jj_la1[14] = jj_gen;
       jj_consume_token(-1);
       throw new ParseException();
     }
                                        {if (true) return opts;}
     throw new Error("Missing return statement in function");
   }
 
   final public void environment() throws ParseException {
                               String host = null; envClass = null;
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case ENV:
       jj_consume_token(ENV);
       jj_consume_token(36);
       envClass = className();
       switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
       case AT:
         jj_consume_token(AT);
         host = sId();
         break;
       default:
         jj_la1[15] = jj_gen;
         ;
       }
       break;
     default:
       jj_la1[16] = jj_gen;
       ;
     }
                               out.print("\t<startAgent ");
                               out.print("\n\t\tname=\"environment\" ");
                               out.print("\n\t\tsociety.name=\""+soc+"-env\" ");
 
                               String fEnvClass;
                               if (envClass == null) {
                                   fEnvClass = jason.environment.Environment.class.getName();
                               } else {
                                   fEnvClass = envClass;
                               }
                               if (architecture.equals("Saci")) {
                                   out.print("\n\t\targs=\""+fEnvClass+"\" ");
                                   fEnvClass = jason.environment.SaciEnvironment.class.getName();
                               }
 
                               out.print("\n\t\tclass=\""+fEnvClass+"\" ");
                               //out.print("\n\t\tclasspath=\"file:"+new File(".").getAbsolutePath()+"/#"+getASClasspathURL()+"\" "); 
                               if (host != null) {
                                       out.print("\n\t\thost=\""+host+"\" ");
                                   }
                               out.println("/>");
   }
 
   final public void control() throws ParseException {
                               String host =  null;
                               controlClass = null;
                               controlPart = null;
     switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
     case CONTROL:
       jj_consume_token(CONTROL);
       jj_consume_token(36);
       controlClass = className();
       switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
       case AT:
         jj_consume_token(AT);
         host = sId();
         break;
       default:
         jj_la1[17] = jj_gen;
         ;
       }
       break;
     default:
       jj_la1[18] = jj_gen;
       ;
     }
                               if (debug) {
                                 controlClass = jason.control.ExecutionControlGUI.class.getName();
                               }
                               if (controlClass != null) {
                                       controlPart =  "\t<startAgent ";
                                       controlPart += "\n\t\tname=\"controller\" ";
                                       controlPart += "\n\t\tsociety.name=\""+soc+"-env\" ";
 
                                               String tArgs = "";
                                       String fControlClass = controlClass;
                                       if (architecture.equals("Saci")) {
                                           fControlClass = jason.control.SaciExecutionControl.class.getName();
                                           tArgs = "\n\t\targs=\""+controlClass+"\"";
                                       }
                                   controlPart += tArgs;
 
                                       controlPart += "\n\t\tclass=\""+fControlClass+"\" ";
 
                                       if (host != null) {
                                          controlPart += "\n\t\thost=\""+host+"\" ";
                                       }
                                       controlPart += "/>";
                               }
   }
 
 /* string from ID */
   final public String sId() throws ParseException {
                  Token t;
     t = jj_consume_token(ASID);
                    {if (true) return(t.image);}
     throw new Error("Missing return statement in function");
   }
 
   public mas2jTokenManager token_source;
   SimpleCharStream jj_input_stream;
   public Token token, jj_nt;
   private int jj_ntk;
   private int jj_gen;
   final private int[] jj_la1 = new int[19];
   static private int[] jj_la1_0;
   static private int[] jj_la1_1;
   static {
       jj_la1_0();
       jj_la1_1();
    }
    private static void jj_la1_0() {
       jj_la1_0 = new int[] {0x3000,0x3000,0x4000000,0x14000000,0x1000000,0x800000,0x0,0x800,0x10000000,0x0,0xc000000,0x0,0x0,0x0,0x3a8000,0x800,0x200,0x800,0x400,};
    }
    private static void jj_la1_1() {
       jj_la1_1 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,0x20,0x0,0x0,0x80,0x0,0x80,0x200,0x100,0x0,0x0,0x0,0x0,0x0,};
    }
 
   public mas2j(java.io.InputStream stream) {
     jj_input_stream = new SimpleCharStream(stream, 1, 1);
     token_source = new mas2jTokenManager(jj_input_stream);
     token = new Token();
     jj_ntk = -1;
     jj_gen = 0;
     for (int i = 0; i < 19; i++) jj_la1[i] = -1;
   }
 
   public void ReInit(java.io.InputStream stream) {
     jj_input_stream.ReInit(stream, 1, 1);
     token_source.ReInit(jj_input_stream);
     token = new Token();
     jj_ntk = -1;
     jj_gen = 0;
     for (int i = 0; i < 19; i++) jj_la1[i] = -1;
   }
 
   public mas2j(java.io.Reader stream) {
     jj_input_stream = new SimpleCharStream(stream, 1, 1);
     token_source = new mas2jTokenManager(jj_input_stream);
     token = new Token();
     jj_ntk = -1;
     jj_gen = 0;
     for (int i = 0; i < 19; i++) jj_la1[i] = -1;
   }
 
   public void ReInit(java.io.Reader stream) {
     jj_input_stream.ReInit(stream, 1, 1);
     token_source.ReInit(jj_input_stream);
     token = new Token();
     jj_ntk = -1;
     jj_gen = 0;
     for (int i = 0; i < 19; i++) jj_la1[i] = -1;
   }
 
   public mas2j(mas2jTokenManager tm) {
     token_source = tm;
     token = new Token();
     jj_ntk = -1;
     jj_gen = 0;
     for (int i = 0; i < 19; i++) jj_la1[i] = -1;
   }
 
   public void ReInit(mas2jTokenManager tm) {
     token_source = tm;
     token = new Token();
     jj_ntk = -1;
     jj_gen = 0;
     for (int i = 0; i < 19; i++) jj_la1[i] = -1;
   }
 
   final private Token jj_consume_token(int kind) throws ParseException {
     Token oldToken;
     if ((oldToken = token).next != null) token = token.next;
     else token = token.next = token_source.getNextToken();
     jj_ntk = -1;
     if (token.kind == kind) {
       jj_gen++;
       return token;
     }
     token = oldToken;
     jj_kind = kind;
     throw generateParseException();
   }
 
   final public Token getNextToken() {
     if (token.next != null) token = token.next;
     else token = token.next = token_source.getNextToken();
     jj_ntk = -1;
     jj_gen++;
     return token;
   }
 
   final public Token getToken(int index) {
     Token t = token;
     for (int i = 0; i < index; i++) {
       if (t.next != null) t = t.next;
       else t = t.next = token_source.getNextToken();
     }
     return t;
   }
 
   final private int jj_ntk() {
     if ((jj_nt=token.next) == null)
       return (jj_ntk = (token.next=token_source.getNextToken()).kind);
     else
       return (jj_ntk = jj_nt.kind);
   }
 
   private java.util.Vector jj_expentries = new java.util.Vector();
   private int[] jj_expentry;
   private int jj_kind = -1;
 
   public ParseException generateParseException() {
     jj_expentries.removeAllElements();
     boolean[] la1tokens = new boolean[44];
     for (int i = 0; i < 44; i++) {
       la1tokens[i] = false;
     }
     if (jj_kind >= 0) {
       la1tokens[jj_kind] = true;
       jj_kind = -1;
     }
     for (int i = 0; i < 19; i++) {
       if (jj_la1[i] == jj_gen) {
         for (int j = 0; j < 32; j++) {
           if ((jj_la1_0[i] & (1<<j)) != 0) {
             la1tokens[j] = true;
           }
           if ((jj_la1_1[i] & (1<<j)) != 0) {
             la1tokens[32+j] = true;
           }
         }
       }
     }
     for (int i = 0; i < 44; i++) {
       if (la1tokens[i]) {
         jj_expentry = new int[1];
         jj_expentry[0] = i;
         jj_expentries.addElement(jj_expentry);
       }
     }
     int[][] exptokseq = new int[jj_expentries.size()][];
     for (int i = 0; i < jj_expentries.size(); i++) {
       exptokseq[i] = (int[])jj_expentries.elementAt(i);
     }
     return new ParseException(token, exptokseq, tokenImage);
   }
 
   final public void enable_tracing() {
   }
 
   final public void disable_tracing() {
   }
 
 }
