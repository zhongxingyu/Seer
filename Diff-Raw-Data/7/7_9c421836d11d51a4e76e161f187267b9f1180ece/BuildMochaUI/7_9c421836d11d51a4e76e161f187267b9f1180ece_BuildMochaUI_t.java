 package com.polaropposite.mochaui.build;
 
 import com.yahoo.platform.yui.compressor.CssCompressor;
 import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
 import net.contentobjects.jnotify.JNotify;
 import net.contentobjects.jnotify.JNotifyException;
 import org.mozilla.javascript.ErrorReporter;
 import org.mozilla.javascript.EvaluatorException;
 
 import java.io.*;
 
 public class BuildMochaUI {
     private String mootoolsCore = "mootools-1.2.2-core-yc.js";
     private long mootoolsCoreMod = 0;
     private String mootoolsMore = "mootools-1.2.2-more-yc.js";
     private long mootoolsMoreMod = 0;
     private long mootoolsScriptsMod = 0;
     private String[] mootoolsScripts = {
             "Core/Core.js",
             "Window/Window.js",
             "Window/Modal.js",
             "Components/Tabs.js",
             "Layout/Layout.js",
             "Layout/Dock.js"
     };
     private boolean forceCopy = false;
 
     private void removeOldMooTools(String from) {
         File dir = new File(from);
         String[] files = dir.list();
 
         int size = files.length;
         for (int i = 0; i < size; i++) {
             String jsname = files[i];
             File file = new File(from + File.separator + jsname);
             if (file.isFile() && jsname.indexOf("mootools-") >= 0) {
                 if (!jsname.equals(mootoolsCore) && !jsname.equals(mootoolsMore)) {
                     try {
                         if (file.delete()) {
                             System.out.printf("\n   [delete] deleted file %s", jsname);
                         }
                     } catch (Exception e) {
                         System.err.printf("\n    [Error] failed to delete file %s", jsname);
                     }
                 }
             }
         }
     }
 
     private String readFile(String fileName) {
         File file = new File(fileName);
         StringBuffer contents = new StringBuffer();
         BufferedReader reader = null;
 
         try {
             reader = new BufferedReader(new FileReader(file));
             String text;
 
             // repeat until all lines is read
             while ((text = reader.readLine()) != null) {
                 contents.append(text)
                         .append(System.getProperty("line.separator"));
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (reader != null) {
                     reader.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return contents.toString();
     }
 
     private void writeFile(String filename, String contents) {
         Writer output = null;
         try {
           output = new BufferedWriter(new FileWriter(filename));
           output.write( contents );
         }
         catch (IOException e) {
             e.printStackTrace();
         } finally {
             if(output!=null) try {
                 output.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }        
     }
 
     public void copyFile(String from, String to) {
         File source = new File(from);
         File destination = new File(to);
         if (!destination.exists() || source.lastModified() > destination.lastModified() || forceCopy) {
             FileInputStream fis = null;
             FileOutputStream fos = null;
             try {
                 System.out.printf("\n     [copy] Copying 1 file to %s",to);
                 if (destination.exists()) destination.delete();
 
                 fis = new FileInputStream(source);
                 fos = new FileOutputStream(destination);
 
                 byte[] buf = new byte[1024];
                 int i;
                 while ((i = fis.read(buf)) != -1) {
                     fos.write(buf, 0, i);
                 }
             }
             catch (Exception e) {
                 System.err.printf("\n    [Error] failed to copy file %s", to);
             }
             finally {
                 try {
                     if (fis != null) fis.close();
                     if (fos != null) fos.close();
                    new File(to).setLastModified(new File(from).lastModified());
                 } catch(Exception e) {
                     System.err.printf("\n    [Error] failed to copy file %s", to);
                 }
             }
         }
     }
 
     private void copyHTML(String from,String to) {
         System.out.printf("\n     [copy] Copying 1 file to %s",to);
         String src=readFile(from);
 
         String tag="<!--MOCHAUI-->";
         if(src.indexOf(tag)>-1) {
             int firstPos=src.indexOf(tag);
             int lastPos=src.indexOf(tag,firstPos+1)+tag.length();
 
             if(firstPos<lastPos) {
                 String part1=src.substring(0,firstPos);                  // before tag
                 String part2=src.substring(lastPos);                     // after tag
                 String partRep=src.substring(firstPos,lastPos + tag.length()); // section we are replacing
 
                 int size=mootoolsScripts.length;
                 boolean hasMocha = false;
                 for(int i=0;i<size;i++) {
                     String script=mootoolsScripts[i];
                     if(partRep.indexOf(script)>-1) {
                         hasMocha=true;
                         break;
                     }
                 }
 
                 src=part1;
                 String indent = part1.substring( part1.lastIndexOf('\n') + 1 );
                 src += "<script type=\"text/javascript\" src=\"scripts/" + mootoolsCore + "\"></script>\n";
                 src += indent + "<script type=\"text/javascript\" src=\"scripts/" + mootoolsMore + "\"></script>\n";
                 if(hasMocha) src += indent + "<script type=\"text/javascript\" src=\"scripts/mocha.js\"></script>";
                 src+=part2;
             }
         }
 
         writeFile(to,src);
         new File(to).setLastModified(new File(from).lastModified());
     }
 
     private void mkdir(String name) {
         File dir = new File(name);
         if (!dir.exists()) {
             if (dir.mkdir())
                 System.out.printf("\n    [mkdir] %s", name);
 
         }
     }
 
     private void compressJsFile(String from, String to) {
         try {
             InputStreamReader in = new InputStreamReader(new FileInputStream(from), "UTF-8");
             JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {
                 public void warning(String message, String sourceName,
                         int line, String lineSource, int lineOffset) {
                     if (line < 0) {
                         System.err.printf("\n  [WARNING] " + message);
                     } else {
                         System.err.printf("\n  [WARNING] " + line + ':' + lineOffset + ':' + message);
                     }
                 }
 
                 public void error(String message, String sourceName,
                         int line, String lineSource, int lineOffset) {
                     if (line < 0) {
                         System.err.printf("\n    [ERROR] " + message);
                     } else {
                         System.err.printf("\n    [ERROR] " + line + ':' + lineOffset + ':' + message);
                     }
                 }
 
                 public EvaluatorException runtimeError(String message, String sourceName,
                         int line, String lineSource, int lineOffset) {
                     error(message, sourceName, line, lineSource, lineOffset);
                     return new EvaluatorException(message);
                 }
             });
 
 
             // Close the input stream first, and then open the output stream,
             // in case the output file should override the input file.
             in.close();
             in = null;
 
             Writer out = new OutputStreamWriter(new FileOutputStream(to), "UTF-8");
             compressor.compress(out, -1, false, false, false, false);            
            out.close();
            
             System.out.printf("\n [compress] compressed %s to %s",from,to);
         } catch(Exception e) {
             System.err.printf("\n    [ERROR] failed to compress %s", to);
             
         }
     }
 
     private void compressCssFile(String from, String to) {
         try {
             InputStreamReader in = new InputStreamReader(new FileInputStream(from), "UTF-8");
             CssCompressor compressor = new CssCompressor(in);
 
             in.close();
             in = null;
 
             Writer out = new OutputStreamWriter(new FileOutputStream(to), "UTF-8");
             compressor.compress(out, -1);
 
             System.out.printf("\n [compress] compressed %s", to);
         } catch(Exception e) {
             System.err.printf("\n    [ERROR] failed to compress %s", to);
         }
     }
     
     private void copyMooTools(String from, String to) {
         String s=File.separator;
         copyFile(from + s + mootoolsCore,to + s + mootoolsCore);
         copyFile(from + s + mootoolsMore,to + s + mootoolsMore);
     }
 
     private void copyResources(String from, String to, String fileType, boolean clear, boolean compress, String[] exclude) {
         // determine source and destination base paths
         String s = File.separator;
         if(!to.endsWith(s)) to += s;
         File dir = new File(to);
 
         // make sure base destination path exists
         mkdir(to);
 
         // remove js files that no longer exist
         if (clear) {
             String[] files = dir.list();
             int size = files.length;
             for (int i = 0; i < size; i++) {
                 File file = new File(dir.toString() + s + files[i]);
                 if (!file.isDirectory()) {
                     String srcFile = from + s + files[i].replace(to, "");
                     File file2 = new File(srcFile);
 
                     // does the file exist, and is the directory not on the exclude list
                     if (!file2.exists()) {
                         boolean deleteIt = false;
                         if (exclude != null) {
 
                         } else deleteIt = true;
 
                         if (deleteIt) {
                             if (file.delete())
                                 System.out.printf("   [delete] Deleted %s\n", files[i]);
                         }
                     }
                 }
             }
         }
 
         dir = new File(from);
         String[] files = dir.list();
         if(files!=null) {
             int size = files.length;
             for (int i = 0; i < size; i++) {
                 File file = new File(dir.toString() + s + files[i]);
                 String fromFile = from + s + files[i];
                 String toFile = to + fromFile.replace(from + s, "");
                 File file2 = new File(toFile);
 
                 // if it is a file
                 if (file.isFile()) {
                     // make sure path exists
                     String toPath = toFile.replace(file2.getName(), "");
                     mkdir(toPath);
 
                     // if target does not exist or is older then copy/compress
                     if ((!file2.exists() || file2.lastModified() < file.lastModified()) && fromFile.indexOf('.') > 0 || forceCopy) {
 
                         // if it is not already compressed
                         if (toFile.indexOf(".min.") < 0 && toFile.indexOf("-yc.") < 0 && fromFile.endsWith("." + fileType) && compress) {
                             // need to compress the files
                             if (fileType.equals("css"))
                                 compressCssFile(fromFile, toFile);
                             else
                                 compressJsFile(fromFile, toFile);
                         } else {
                             if (fromFile.endsWith(".html")) {
                                 copyHTML(fromFile, toFile);
                             } else {
                                 // copy the file
                                 copyFile(fromFile, toFile);
                             }
                         }
                     }
                 } else {
                     // if it is a directory make it
                     copyResources(from + s + files[i], to + files[i], fileType, clear, compress, exclude);
                     mkdir(toFile);
                 }
             }
         } else {            
             System.err.printf("\n  [WARNING] directory %s is empty", from);
         }
     }
 
     public void Build(String mochaPath,boolean force) throws IOException {
         forceCopy = force;
         String s = File.separator;
 
         // make sure path has trailing directory separator
         if(!mochaPath.endsWith("/") && !mochaPath.endsWith("\\")) mochaPath+=s;
         
         // resource directories
         File mooTools = new File(mochaPath+"src/core");
         String pluginsDir = new File(mochaPath+"src/plugins").getCanonicalPath();
         String themesDir = new File(mochaPath+"src/themes").getCanonicalPath();
         String demoDir = new File(mochaPath+"src/demo").getCanonicalPath();
 
         // detect mootool filenames, so replace in demo section can happen
         String[] files = mooTools.list();
         int size = files.length;
         for (int i = 0; i < size; i++) {
             File file = new File(mooTools.toString() + s + files[i]);
             String jsname = file.getName();
             if (jsname.indexOf("mootools-") >= 0 && jsname.endsWith(".js")) {
                 if (jsname.indexOf("-core") > 0) {
                     mootoolsCoreMod = file.lastModified();
                     mootoolsCore = jsname;
                 }
                 if (jsname.indexOf("-more") > 0) {
                     mootoolsMoreMod = file.lastModified();
                     mootoolsMore = jsname;
                 }
                 if (mootoolsCoreMod > 0 && mootoolsMoreMod > 0) break;
             }
         }
 
         // destination script folders
         String demoJS = new File(mochaPath+"demo/scripts").getCanonicalPath();
         String buildJS = new File(mochaPath+"build").getCanonicalPath();
 
         //------------------------------------------------------
         // now copy themes and plugins to demo
         mkdir(mochaPath+"demo");
         copyResources(demoDir, new File(mochaPath+"demo").getCanonicalPath(), "js", true, false, new String[]{"plugins", "themes"});
         copyResources(pluginsDir, new File(mochaPath+"demo/plugins").getCanonicalPath(), "js", true, false, null);
         copyResources(themesDir, new File(mochaPath+"demo/themes").getCanonicalPath(), "js", true, false, null);
         removeOldMooTools(demoJS);
         copyMooTools(mooTools.getCanonicalPath(), new File(mochaPath+"demo/scripts").getCanonicalPath());
 
         //------------------------------------------------------
         // now copy themes and plugins to the build folder
         mkdir(mochaPath+"build");
         copyResources(pluginsDir, new File(mochaPath+"build/plugins").getCanonicalPath(), "js", true, true, null);
         copyResources(themesDir, new File(mochaPath+"build/themes").getCanonicalPath(), "css", true, true, null);
         removeOldMooTools(buildJS);
         copyMooTools(mooTools.getCanonicalPath(), new File(mochaPath+"build").getCanonicalPath());
 
 
         //------------------------------------------------------
         // build script libraries
         String licenseFile = new File(mochaPath+"MIT-LICENSE.txt").getCanonicalPath();
         String authorsFile = new File(mochaPath+"Authors.txt").getCanonicalPath();
 
         // make sure license files are in the same folder as mocha.js in demo
         copyFile(licenseFile, mochaPath+"demo/scripts/MIT-LICENSE.txt");
         copyFile(authorsFile, mochaPath+"demo/scripts/AUTHORS.txt");
 
         // make sure license files are in the same folder as mocha.js in build
         copyFile(licenseFile, mochaPath+"build/MIT-LICENSE.txt");
         copyFile(authorsFile, mochaPath+"build/AUTHORS.txt");
 
 
         //----------------------------------------------------------
         // create the demo mocha.js that is not compressed
         File dest = new File(mochaPath+"demo/scripts/mocha.js");
         String dir1 = new File(mochaPath+"src/core").getCanonicalPath() + s;
 
         // first see if any of the files have changed, get the oldest script
         size = mootoolsScripts.length;
         for (int i = 0; i < size; i++) {
             File src = new File(dir1 + mootoolsScripts[i]);
             if (mootoolsScriptsMod == 0 || src.lastModified() > mootoolsScriptsMod) {
                 mootoolsScriptsMod = src.lastModified();
             }
         }
 
         if (!dest.exists() || dest.lastModified() < mootoolsScriptsMod) {
             // clear the demo mocha.js
             System.out.printf("\n [building] %s", dest.getCanonicalPath());
             if (dest.exists()) dest.delete();
 
 
             // append the license file to the mocha.js, add js comments to keep from being removed by compressor
             String license = readFile(licenseFile);
             OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest.getCanonicalFile()), "UTF-8");
 
             out.append("/*!\n");
             out.append(license);
             out.append("\n*/\n");
 
             // create the demo mocha.js that is not compressed
             size = mootoolsScripts.length;
             for (int i = 0; i < size; i++) {
                 String text = readFile(dir1 + mootoolsScripts[i]);
                 System.out.printf("\n[appending] %s", dest.getCanonicalPath());
                 out.append(text);
             }
             out.close();
             out = null;
 
             dest.setLastModified(mootoolsScriptsMod);
         }
 
         // we need to compress so fire off a command to execute the yuicompressor
         File toJSFile = new File(mochaPath+"build/mocha.js");
         if (toJSFile.lastModified() < dest.lastModified()) {
             String toJS = toJSFile.getCanonicalPath();
             String fromJS = dest.getCanonicalPath();
 
             compressJsFile(fromJS, toJS);
             toJSFile.setLastModified(dest.lastModified());
         }
     }
 
     public static void Rebuild(String mochaPath, boolean force) {
         try {
             new BuildMochaUI().Build(mochaPath, force);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static int Watch(String path, boolean force) {
         try {
             if(path==null || path.isEmpty()) path = ".";
 
             // initialize file change notifications
             String fullPath = new File(path+"src/").getCanonicalPath();
             int mask =  JNotify.FILE_CREATED  |
                         JNotify.FILE_DELETED  |
                         JNotify.FILE_MODIFIED |
                         JNotify.FILE_RENAMED;
 
             return JNotify.addWatch(fullPath, mask, true, new FileChangeWatcher(path,force));
         } catch (IOException e) {
             e.printStackTrace();
         }
         return -1;
     }
 
     public static boolean StopWatch(int watchId) {
         try {
             JNotify.removeWatch(watchId);
             return true;
         } catch (JNotifyException e) {
             e.printStackTrace();
         }
         return false;
     }
 }
 
