 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: FileFind.java,v 1.4 2003-03-25 17:49:01 shahbaz.javeed Exp $
  */
 
 package com.netspective.commons.io;
 
 import com.netspective.commons.text.TextUtils;
 
 import java.io.*;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 public class FileFind
 {
     public static final int FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_FIRST = 1;
     public static final int FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_DURING = FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_FIRST * 2;
     public static final int FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_LAST = FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_DURING * 2;
     public static final int FINDINPATHFLAG_SEARCH_FILE_MAY_BE_DIRECTORY = FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_LAST * 2;
     public static final int FINDINPATHFLAG_SEARCH_RECURSIVELY = FINDINPATHFLAG_SEARCH_FILE_MAY_BE_DIRECTORY * 2;
 
 	public static final int FINDINPATHFLAG_DEFAULT = FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_DURING;
 
     /**
      * Return 'true' if the given file appears to be a compressed file
      * (with either .jar or .zip extension).
      * <P>
      * @param fileName a file being investigated
      * @return true if the 'fileName' could be a .jar or .zip file
      **/
     public static boolean isJarFile(String fileName)
     {
         File f = new File(fileName);
         if (TextUtils.isEmpty(f.getName())) return false;
         String fileExt = getExtension(f).toLowerCase();
         return (fileExt.equals("jar") || fileExt.equals("zip"));
     }
 
     /**
      * Return the extension portion of the file's name.
      * The extension is everything after last dot unless this dot is
      * before any path separator (path separator depends on the
      * platform, specifically is taken from the Java System property
      * "file.separator").
      * <P>
      * @param f is a file whose file extension is being looked for
      * @return a file extension found in the end of 'f', or an empty
      *         string if there is no extension
      **/
     public static String getExtension(File f)
     {
         if (f != null)
         {
             String filename = f.getName();
             int pos = filename.lastIndexOf('.');
             if (pos > 0 && pos < filename.length() - 1)
                 return filename.substring(pos + 1);
         }
         return "";
     }
 
     /**
      * Return the extension portion of the file's name which is given as
      * a String.
      * <P>
      * @param f is a file whose file extension is being looked for
      * @return a file extension found in the end of 'f', or an empty
      *         string if there is no extension
      * @see #getExtension(File)
      **/
     public static String getExtension(String f)
     {
         return getExtension(new File(f));
     }
 
     /**
      * Return true if 'fileName' exists as an entry in the 'jarFile'.
      * If 'perhapsDirectory' is true look also for jar entries starting
      * with 'fileName' followed by "/".
      * <P>
      * @param jarFile a compressed file where to look for the 'fileName'
      * @param fileName a file name to be looked for
      * @param perhapsDirectory if true the 'fileName' is also considered
      *        as a directory name in the jar file's entries
      * @return true if 'fileName' found in 'jarFile'
      **/
     public static boolean existsInJarFile(File jarFile, String fileName, boolean perhapsDirectory)
     {
         try
         {
             ZipFile zf = new ZipFile(jarFile);
             if (zf.getEntry(fileName) != null) return true;
             if (perhapsDirectory)
             {
                 if (!fileName.endsWith("/")) fileName = fileName + "/";
                 String name;
                 for (Enumeration en = zf.entries(); en.hasMoreElements();)
                 {
                     name = ((ZipEntry) en.nextElement()).getName();
                     if (name.startsWith(fileName)) return true;
                 }
             }
         }
         catch (Exception e)
         {
         }
         return false;
     }
 
     /**
      * Read the contents of an 'entry' from a compressed (jar, zip) file.
      * <P>
      * @param jarFile a compressed file
      * @param entryName an entry to be read and returned
      * @return contents of the <tt>entryName</tt> as stored in <tt>jarFile</tt>
      **/
     public static byte[] getJarEntry(File jarFile, String entryName)
             throws FileNotFoundException, IOException
     {
 
         // find if the wanted entry exists and how big it is
         ZipFile zf = new ZipFile(jarFile);
         ZipEntry ze = zf.getEntry(entryName);
         if (ze == null)
             throw new FileNotFoundException("'" + entryName + "' was not found in '" +
                     jarFile.getAbsolutePath() + "'.");
         if (ze.isDirectory())
             throw new IOException("'" + entryName + "' found in '" +
                     jarFile.getAbsolutePath() + "' is a directory.");
         int size = (int) ze.getSize();
         if (size == 1)
             throw new IOException("'" + entryName + "' was found in '" +
                     jarFile.getAbsolutePath() +
                     "' but it reports unknown size.");
         zf.close();
 
         // extract the entry
         ZipInputStream zis = new ZipInputStream
                 (new BufferedInputStream
                         (new FileInputStream(jarFile)));
 
         int rb = 0;
         int chunk = 0;
         byte[] buf = new byte[size];  // here we read contents in
 
         while ((ze = zis.getNextEntry()) != null)
         {
             if (!ze.getName().equals(entryName)) continue;
 
             // yes, that's the entry we want to read...
             while ((size - rb) > 0)
             {
                 chunk = zis.read(buf, rb, size - rb);
                 if (chunk == -1) break;
                 rb += chunk;
             }
             zf.close();
             return buf;
         }
         throw new FileNotFoundException("'" + entryName + "' was not found in '" +
                 jarFile.getAbsolutePath() + "'");
     }
 
     public static class FileFindResults
     {
         private String[] searchPaths;
         private String searchFileName;
         private boolean searchJarsEarly;
         private boolean searchJarsDuring;
         private boolean searchJarsLate;
         private boolean searchFileMayBeDirectory;
 	    private boolean searchRecursive;
         private int foundFileInPathItem;
         private File foundFile;
         private boolean foundFileAbsoluteAndDoesntExist;
         private boolean foundFileInJar;
 
         public FileFindResults(String[] searchPaths, String searchFileName, int flags)
         {
             this.searchPaths = searchPaths;
             this.searchFileName = searchFileName;
             this.searchJarsEarly = (flags & FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_FIRST) != 0;
             this.searchJarsDuring = (flags & FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_DURING) != 0;
             this.searchJarsLate = (flags & FINDINPATHFLAG_SEARCH_INSIDE_ARCHIVES_LAST) != 0;
             this.searchFileMayBeDirectory = (flags & FINDINPATHFLAG_SEARCH_FILE_MAY_BE_DIRECTORY) != 0;
 	        this.searchRecursive = (flags & FINDINPATHFLAG_SEARCH_RECURSIVELY) != 0;
             this.foundFileInPathItem = -1;
 
             File file = new File(searchFileName);
             if (file.isAbsolute())
             {
                 if (file.exists())
                 {
                     foundFile = file;
                     return;
                 }
 
                 foundFile = null;
                 foundFileAbsoluteAndDoesntExist = true;
                 return;
             }
         }
 
         public FileFindResults(String searchPaths, String searchPathsDelim, String searchFileName, int flags)
         {
             this(TextUtils.split(searchPaths, searchPathsDelim, false), searchFileName, flags);
         }
 
         public String getSearchFileName()
         {
             return searchFileName;
         }
 
         public String[] getSearchPaths()
         {
             return searchPaths;
         }
 
         /**
          * Returns true if a file was found in the path. This will return true if the file found was either a physical
          * file or was found inside a JAR file.
          */
         public boolean isFileFound()
         {
             return foundFile != null;
         }
 
         /**
          * Returns true if a file was found in a JARF file the path.
          */
         public boolean isFoundFileInJar()
         {
             return foundFileInJar;
         }
 
         /**
          * Returns the physical file or the JAR file that was found. If it's a JAR file, then the actual file will be
          * inside the JAR file.
          */
         public File getFoundFile()
         {
             return foundFile;
         }
 
         /**
          * Returns the path index number in which the file was found.
          */
         public int getFoundFileInPathItem()
         {
             return foundFileInPathItem;
         }
 
         /**
          * Returns true if the original search file was provided as an absolute file (which means paths searching was
          * skipped) but the actual absolute file does not exist.
          */
         public boolean isFoundFileAbsoluteAndDoesntExist()
         {
             return foundFileAbsoluteAndDoesntExist;
         }
 
         /**
          * Given a path item index, see if it's a JAR file and if the file we're looking for exists in that JAR file.
          */
         protected boolean locatedInJar(int searchPathItemIndex)
         {
             File potentialJarFile = new File(searchPaths[searchPathItemIndex]);
             if (potentialJarFile.isFile())
             {
                 if (isJarFile(potentialJarFile.getAbsolutePath()) && existsInJarFile(potentialJarFile, searchFileName, searchFileMayBeDirectory))
                 {
                     foundFile = potentialJarFile;
                     foundFileInJar = true;
                     foundFileInPathItem = searchPathItemIndex;
                     return true;
                 }
             }
 
             return false;
         }
 
         /**
          * Perform the file find operation.
          */
         public void locate()
         {
             if (isFileFound() || isFoundFileAbsoluteAndDoesntExist())
                 return;
 
             if (searchJarsEarly)
             {
                 for (int i = 0; i < searchPaths.length; i++)
                 {
                     if (locatedInJar(i))
                         return;
                 }
             }
 
             for (int i = 0; i < searchPaths.length; i++)
             {
                 File searchPath = new File(searchPaths[i]);
 
                 if (searchJarsDuring && locatedInJar(i))
                     return;
 
                 File findFile = new File(searchPath, searchFileName);
                 if (findFile.exists())
                 {
                     foundFile = findFile;
                     foundFileInPathItem = i;
                     return;
                 }
 	            else
                 {
 	                if (searchPath.isDirectory()) {
 		                // Do a breadth first search if the file isnt found ...
 		                RegexpFileFilter fileFilter = new RegexpFileFilter("^" + searchFileName + "$", true);
 		                List searchCandidates = new ArrayList();
 		                List dynamicallyUpdatedCandidates = new ArrayList();
 		                File[] initialCandidates = searchPath.listFiles(fileFilter);
 
 		                for (int j = 0; j < initialCandidates.length; j ++)
 			                searchCandidates.add(initialCandidates[j]);
 
 		                Iterator candidate = searchCandidates.listIterator();
 		                while (candidate.hasNext() || dynamicallyUpdatedCandidates.size() > 0)
 		                {
 							if (! candidate.hasNext())
 							{
 								// We're at the end of the initial set of directories...
 								// Populate them with the ones we found during our search ...
 								searchCandidates.clear();
 								searchCandidates.addAll(dynamicallyUpdatedCandidates);
 								dynamicallyUpdatedCandidates.clear();
 								candidate = searchCandidates.listIterator();

								if (! candidate.hasNext()) continue;
 							}
 
 			                File theCandidate = (File) candidate.next();
 
 			                if (theCandidate.isDirectory())
 			                {
 				                File findFileInCandidate = new File(theCandidate, searchFileName);
 				                if (findFileInCandidate.exists())
 				                {
 					                foundFile = findFileInCandidate;
 					                foundFileInPathItem = i;
 					                return;
 				                }
 
 				                File[] moreCandidates = theCandidate.listFiles(fileFilter);
 
 				                for (int c = 0; c < moreCandidates.length; c ++)
 					                dynamicallyUpdatedCandidates.add(moreCandidates[c]);
 			                }
 			                else
 			                {
 				                // This must be a file...
 				                if (theCandidate.getName().equals(searchFileName))
 				                {
 					                // Exact match found...
 					                foundFile = theCandidate;
 					                foundFileInPathItem = i;
 					                return;
 				                }
 			                }
 		                }
 	                }
                 }
             }
 
             if (searchJarsLate)
             {
                 for (int i = 0; i < searchPaths.length; i++)
                 {
                     if (locatedInJar(i))
                         return;
                 }
             }
         }
     }
 
     public static final FileFindResults findInPath(String[] searchPaths, String searchFileName, int flags)
     {
         if (searchPaths == null || searchPaths.length == 0 || TextUtils.isEmpty(searchFileName))
             return null;
 
         FileFindResults results = new FileFindResults(searchPaths, searchFileName, flags);
         results.locate();
         return results;
     }
 
     public static final FileFindResults findInPath(String searchPaths, String searchPathsDelim, String searchFileName, int flags)
     {
         if (TextUtils.isEmpty(searchPaths) || TextUtils.isEmpty(searchFileName))
             return null;
 
         FileFindResults results = new FileFindResults(searchPaths, searchPathsDelim, searchFileName, flags);
         results.locate();
         return results;
     }
 
     public static final FileFindResults findInClasspath(String searchFileName, int flags)
     {
         return findInPath(System.getProperty("java.class.path"), System.getProperty("path.separator"), searchFileName, flags);
     }
 
 }
