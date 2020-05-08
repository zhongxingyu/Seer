 /*
  * //
  * // Copyright (C) 2009 Boutros-Labs(German cancer research center) b110-it@dkfz.de
  * //
  * //
  * //    This program is free software: you can redistribute it and/or modify
  * //    it under the terms of the GNU General Public License as published by
  * //    the Free Software Foundation, either version 3 of the License, or
  * //    (at your option) any later version.
  * //
  * //    This program is distributed in the hope that it will be useful,
  * //    but WITHOUT ANY WARRANTY; without even the implied warranty of
  * //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * //
  * //    You should have received a copy of the GNU General Public License
  * //    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  *
  */
 
 package cellHTS.classes;
 
 import org.apache.tapestry5.ioc.internal.util.TapestryException;
 
 import java.io.*;
 import java.util.zip.*;
 import java.util.Enumeration;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 /**
  *
  * * this class provides shell tools such as copying or ziping of files. It should be platform independant
  * 
  * Created by IntelliJ IDEA.
  * User: pelz
  * Date: 11.11.2008
  * Time: 17:16:35
  *       
  *
  */
 public class ShellEnvironment {
     /**
      *
      * deprecated method for executing a command on the command line and streaming the results
      * as string
      *
      * @param command  the command to be executed on the command line
      * @return returns the output of the shell command
      */
     public static String executeCommand(String command) {
         String returnString = "";
         try {
             Runtime rt = Runtime.getRuntime();
 
             Process pr = rt.exec(command);
 
             BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
 
             String line = null;
 
             while ((line = input.readLine()) != null) {
                 returnString.concat(line);
             }
 
             int exitVal = pr.waitFor();
             returnString.concat("Exited with error code " + exitVal);
 
         } catch (Exception e) {
             String exceptionText = "error executing shell command" + e.toString() + "\n";
             TapestryException shellCommandException = new TapestryException(exceptionText, null);
             throw shellCommandException;
 
         }
         return returnString;
     }
 
     /**
      *
      * copy two input streams
      *
      * @param in
      * @param out
      * @throws IOException
      */
     private static final void copyInputStream(InputStream in, OutputStream out)
             throws IOException {
         byte[] buffer = new byte[1024];
         int len;
 
         while ((len = in.read(buffer)) >= 0)
             out.write(buffer, 0, len);
 
         in.close();
         out.close();
     }
     //this extracts zip files with internal directory structure     p
     public static ArrayList<File> unzip(String[]errorMsg,String filename, String pathToExtract) {
         ArrayList<File> returnFiles = new ArrayList<File>();
         Enumeration entries;
         ZipFile zipFile;
         errorMsg[0]="false";
         errorMsg[1]="";
         //this is for mac os x created archives ..mac creates a meta info folder which we want to skip
         Pattern pat = Pattern.compile("__MACOSX/");
         Pattern pat2 = Pattern.compile(".DS_Store");
         //TODO: found another mac archive pattern ._FILENAME   ????
         try {
             zipFile = new ZipFile(filename);
             entries = zipFile.entries();
 
             while (entries.hasMoreElements()) {
                 ZipEntry entry = (ZipEntry) entries.nextElement();
                 String name = entry.getName();
                 Matcher m = pat.matcher(name);
                 if(m.find()) {
                     //skip the mac meta data
                     continue;
                 }
                 m = pat2.matcher(name);
                 if(m.find()) {
                     //skip the mac meta data
                     continue;
                 }
                 String dirString=pathToExtract + File.separator+entry.getName();
                 boolean isDir=false;
                 if(!entry.isDirectory()) {
                     dirString = (new File(dirString)).getParent();
                 }
                 else {
                     isDir=true;
                 }
                 File dir = new File(dirString);
                 //create dirs if not already created
                 if(!dir.exists()) {
                     dir.mkdirs();
                 }
                 if(!isDir) {
                     String fullName = pathToExtract + File.separator + entry.getName();
                     returnFiles.add(new File(pathToExtract + File.separator + entry.getName()));
                     copyInputStream(zipFile.getInputStream(entry),
                         new BufferedOutputStream(new FileOutputStream(fullName)));
                 }
             }
 
             zipFile.close();
         } catch (IOException ioe) {
             String exceptionText = "sometings wrong with the zip archive: is it a zip file at all?\n"; //" + ioe.toString() + "\n";
             //TapestryException archiveException = new TapestryException(exceptionText, null);
             //throw archiveException;
             errorMsg[0]="true";
             errorMsg[1]=exceptionText;
         }
         return returnFiles;
 
     }
     //this extracts zip files without internal directory structure...just extract the files
     public static ArrayList<File> plainUnzip(String[]errorMsg,String filename, String pathToExtract) {
         ArrayList<File> returnFiles = new ArrayList<File>();
         Enumeration entries;
         ZipFile zipFile;
         errorMsg[0]="false";
         errorMsg[1]="";
         //this is for mac os x created archives ..mac creates a meta info folder which we want to skip
         Pattern pat = Pattern.compile("__MACOSX/");
         Pattern pat2 = Pattern.compile(".DS_Store");
         //TODO: found another mac archive pattern ._FILENAME   ????
         try {
             zipFile = new ZipFile(filename);
             entries = zipFile.entries();
 
             while (entries.hasMoreElements()) {
                 ZipEntry entry = (ZipEntry) entries.nextElement();
                 String name = entry.getName();
                 Matcher m = pat.matcher(name);
                if(m.find()) {
                     //skip the mac meta data
                     continue;
                 }
                 m = pat2.matcher(name);
                 if(m.find()) {
                     //skip the mac meta data
                     continue;
                 }
                 String[]tmpArr=entry.getName().split(File.separator);
                 String entryName=tmpArr[tmpArr.length-1];
 
                 String fullName = pathToExtract + File.separator + entryName;
                 returnFiles.add(new File(pathToExtract + File.separator + entryName));
                 copyInputStream(zipFile.getInputStream(entry),
                 new BufferedOutputStream(new FileOutputStream(fullName)));
 
             }
 
             zipFile.close();
         } catch (IOException ioe) {
             String exceptionText = "sometings wrong with the zip archive: is it a zip file at all?\n"; //" + ioe.toString() + "\n";
             //TapestryException archiveException = new TapestryException(exceptionText, null);
             //throw archiveException;
             errorMsg[0]="true";
             errorMsg[1]=exceptionText;
             ioe.printStackTrace();
         }
         return returnFiles;
 
     }
 
 
     //here is the code for the method
     //rootDir will be the new root folder you want to give ...it must be a dir in the full path e.g.
     // /tmp/cellHTS2/CHTS56476/.... CHTS56476
     //TODO:this method has some serious limitations , you have to provide a rootDir which is the same
     //TODO: as the last in dir2zip name
     public static boolean zipDir(String dir2zip, ZipOutputStream zos, String rootDir) {
         try {
             //create a new File object based on the directory we provide
 
             File zipDir = new File(dir2zip);
             //get a listing of the directory content
             String[] dirList = zipDir.list();
             byte[] readBuffer = new byte[2156];
             int bytesIn = 0;
             //loop through dirList, and zip the files
             for (int i = 0; i < dirList.length; i++) {
 
                 File f = new File(zipDir, dirList[i]);
                 if (f.isDirectory()) {
                     //if the File object is a directory, call this
                     //function again to add its content recursively
                     String filePath = f.getPath();
                     zipDir(filePath, zos, rootDir);
                     //loop again
                     continue;
                 }
                 //if we reached here, the File object f was not a directory
                 //create a FileInputStream on top of f
 
                 FileInputStream fis = new FileInputStream(f);
                 // create a new zip entry
 
                 //we work with absolute pathes but we want to disregard them here..cut abs path
                 //this is a big limitation
                 String[] tmpArr = f.getAbsolutePath().split(rootDir);
                 String path = tmpArr[1];
 
                 path = path.replaceFirst(File.separator, "");
                 String newPath = rootDir + File.separator + path;
 
                 ZipEntry anEntry = new ZipEntry(newPath);
                 //place the zip entry in the ZipOutputStream object
                 zos.putNextEntry(anEntry);
                 //now write the content of the file to the ZipOutputStream
                 while ((bytesIn = fis.read(readBuffer)) != -1) {
                     zos.write(readBuffer, 0, bytesIn);
                 }
                 //close the Stream
                 fis.close();
             }
         }
 
         catch (Exception e) {
             //handle exception
             return false;
         }
         return true;
 
     }
     //zip files without directory structure
     //zipDirs param is the internal directory a certain files[i] will be stored at in the zipfile
     //this param can be null
     public static boolean zipFiles(String[] files,String []zipDirs,String outputFile) {
         int read = 0;
          FileInputStream in;
          byte[] data = new byte[1024];
          try {
                 // connect Zip-Archive with stream
                 ZipOutputStream out =
                   new ZipOutputStream(new FileOutputStream(outputFile));
                 // Archivierungs-Modus setzen
                 out.setMethod(ZipOutputStream.DEFLATED);
                 // Hinzufgen der einzelnen Eintrge
                 for (int i=0; i < files.length; i++) {
                   try {
                     if(files[i]==null) {
                         continue;
                     }
                     // Eintrag fr neue Datei anlegen
 
                     //cut off directory structure
                     String[]filesArr = files[i].split(File.separator);
                     String pureFilename = filesArr[filesArr.length-1];
                     if(zipDirs[i]!=null) {
                         pureFilename=zipDirs[i]+File.separator+pureFilename;
                     }
                     ZipEntry entry = new ZipEntry(pureFilename);
                     in = new FileInputStream(files[i]);
                     // Neuer Eintrag dem Archiv hinzufgen
                     out.putNextEntry(entry);
                     // Hinzufgen der Daten zum neuen Eintrag
                     while((read = in.read(data, 0, 1024)) != -1)
                       out.write(data, 0, read);
                     out.closeEntry(); // Neuen Eintrag abschlieen
                     in.close();
                   }
                   catch(Exception e) {
                     e.printStackTrace();
                       return false;
                   }
                 }
                 out.close();
               }
               catch(IOException ex) {
                 ex.printStackTrace();
                   return false;
               }
         return true;
     }
 
     
 
 
 }
 
 
 
