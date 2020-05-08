 /*
  * Copyright (c) 2001, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lagoon.core;
 
 import java.io.*;
 import java.util.*;
 
 import org.xml.sax.*;
 
 import nu.staldal.lagoon.util.Wildcard;
 
 /**
  * A file in the sitemap.
  *
  * Contains information on how to (re)build a single file in a website.
  *
  * @see nu.staldal.lagoon.core.Sitemap
  */
 class FileEntry implements SourceManager, FileTarget
 {
     private static final boolean DEBUG = false;
 
     private ByteStreamProducer myProducer;
 
     private final FileStorage targetStorage;
     private final File sourceRootDir;
     private final java.net.URL sourceRootDirURL;
 
     private final File sourceDir;
     private final String sourceDirURL;
     private final String sourceFile;
     private final String targetURL;
 
     private File currentSourceFile;
     private String currentSourceName;
     private String currentTargetURL;
     private long targetLastMod;
     private String newTarget;
 
     /**
      * Constructor.
      *
      * @param targetURL  the file to create, may contain wildcard anywhere,
      *                   must start with '/'.
      * @param source  the file to use, may contain wildcard in filename,
 	 *                must start with '/', may be <code>null</code>.
      * @param sourceRootDir  absolute path to the source directory
      * @param targetStorage  where to store generated files
      */
     public FileEntry(String targetURL, String source,
                      File sourceRootDir, FileStorage targetStorage)
         throws LagoonException
     {
         this.myProducer = null;
 
         this.targetStorage = targetStorage;
 
         String absPath = sourceRootDir.getAbsolutePath();
         this.sourceRootDir = new File(absPath);
         if (!this.sourceRootDir.isDirectory())
             throw new LagoonException(
                 "sourceRootDir must be an existing directory: "
                 + sourceRootDir);
 
         // we should use File.toURL() here, but it's Java2
 		absPath = absPath.replace(File.separatorChar, '/');
         if (!absPath.endsWith("/")) absPath = absPath + "/";
         try {
             sourceRootDirURL = new java.net.URL("file:" +
 	            ((absPath.charAt(0) == '/') ? "//" : "///") +
 		        absPath);
         }
         catch (java.net.MalformedURLException e)
         {
             throw new LagoonException(
                 "Unable to transform source root into URL: "
                 + e.getMessage());
         }
 
         if (source == null)
         {
             this.sourceDir = null;
             this.sourceFile = null;
             this.sourceDirURL = null;
         }
         else
         {
             if (source.charAt(0) != '/')
                 throw new LagoonException("source must start with \'/\'");
             int slash = source.lastIndexOf('/');
 
             this.sourceDirURL = source.substring(0, slash+1);
 
             String sourcePath = source.substring(1, slash+1);
             if (sourcePath.length() == 0)
             {
                 this.sourceDir = this.sourceRootDir;
             }
             else
             {
                 this.sourceDir = new File(sourceRootDir,
                     sourcePath.replace('/', File.separatorChar));
             }
 
             String sourceFilename = source.substring(slash+1);
             this.sourceFile = (sourceFilename.length() == 0)
                 ? null
                 : sourceFilename;
         }
 
         this.targetURL = targetURL;
 
         this.currentSourceFile = null;
         this.currentSourceName = null;
         this.currentTargetURL = null;
         this.targetLastMod = -1;
         this.newTarget = null;
     }
 
 
     /**
      * Set the ByteStreamProducer that produces the final output for this
      * FileEntry.
      * Used during initialization.
      */
     void setMyProducer(ByteStreamProducer prod)
     {
         myProducer = prod;
     }
 
 
     /**
      * Builds this particular file.
      *
      * @param always  always build the file, overriding dependency checking
      */
     public void build(boolean always)
         throws IOException
     {
         // System.out.println("Building " + target + " from " + source +
         //                   (always ? " always" : ""));
 
         if (sourceDir == null)
         {   // no main source file
             currentSourceFile = null;
             currentSourceName = null;
             currentTargetURL = targetURL;
             buildFile(always);
         }
         else if (sourceFile == null)
         {   // main source is a directory
             currentSourceFile = sourceDir;
             currentSourceName = sourceFile;
             currentTargetURL = targetURL;
             buildFile(always);
         }
         else if (Wildcard.isWildcard(sourceFile))
         {   // main source is a wildcard pattern
             String[] files = sourceDir.list();
             for (int i = 0; i < files.length; i++)
             {
                 currentSourceFile = new File(sourceDir, files[i]);
                 if (!currentSourceFile.isFile()) continue;
 
                 currentSourceName = files[i];
 
                 String part = Wildcard.matchWildcard(sourceFile, files[i]);
                 if (part == null) continue;
 
                 currentTargetURL =
                     Wildcard.instantiateWildcard(targetURL, part);
                 buildFile(always);
             }
         }
         else
         {   // main source is a regular file
             currentSourceFile = new File(sourceDir, sourceFile);
             currentSourceName = sourceFile;
             currentTargetURL = targetURL;
             buildFile(always);
         }
 
     }
 
     private void buildFile(boolean always)
         throws IOException
     {
         // System.out.println("buildFile: " + currentTargetURL);
 
         targetLastMod = targetStorage.fileLastModified(currentTargetURL);
 
         if (always || (targetLastMod <= 0))
         {
             buildAlways();
             return;
         }
 
         boolean updated = false;
         try {
             updated = myProducer.hasBeenUpdated(targetLastMod);
         }
         catch (LagoonException e)
         {
             reportException(e);
         }
         catch (IOException e)
         {
             reportException(e);
         }
 
         if (updated)
         {
             buildAlways();
         }
     }
 
     /**
      * The actual building of this file.
      * Used after any dependency checking indicates the file needs rebuilding.
      */
     private void buildAlways()
         throws IOException
     {
         System.out.println("Building: " + currentTargetURL);
 
 		int slash = currentTargetURL.lastIndexOf('/');
 		String currentTargetDir = currentTargetURL.substring(0, slash+1);
 		String currentTargetName = currentTargetURL.substring(slash+1);
 
         String thisTargetURL;
         OutputStream out = null;
         String exceptionType = null;
         boolean bailOut = false;
 
 		newTarget = currentTargetName;
 
         do {
 			thisTargetURL = currentTargetDir + newTarget;
             newTarget = null;
             try {
 	            out = targetStorage.createFile(thisTargetURL);
                 myProducer.start(out, this);
                 out.close();
                 out = null;
                 exceptionType = null; // no exception thrown
             }
             catch (Exception e)
             {
 				String thisExceptionType = e.getClass().getName();
 
 				// the same type of exception thrown twice in a row
 				if (thisExceptionType.equals(exceptionType))
 				{
 					bailOut = true;
 				}
 				exceptionType = thisExceptionType;
 
 				try {
 					if (out != null)
 					{
 						out.close();
 						out = null;
 					}
 				}
 				finally
 				{
                 	targetStorage.discardFile();
 				}
 
 				if (e instanceof RuntimeException)
 				{
 					throw (RuntimeException)e;
 				}
 
                 reportException(e);
 
                 if (bailOut)
                 {
 					System.out.println("Too many exceptions, bailing out");
 					break;
 				}
 				else
 				{
 					continue;
 				}
             }
 
            targetStorage.commitFile();
         } while (newTarget != null);
     }
 
 
 	private void reportException(Exception e)
 	{
 		if (e instanceof SAXParseException)
 		{
 			SAXParseException spe = (SAXParseException)e;
 			String sysId = (spe.getSystemId() == null)
 				? "source": spe.getSystemId();
 			System.out.println(sysId + ":" + spe.getLineNumber()
 				+ ":" + spe.getColumnNumber() + ": " + spe.getMessage());
 		}
 		else if (e instanceof SAXException)
 		{
 			SAXException se = (SAXException)e;
 			Exception ee = se.getException();
 			if (ee != null)
 			{
 				System.out.println(ee.toString());
 			    if (DEBUG) ee.printStackTrace(System.out);
 			}
 			else
 			{
 				System.out.println(se.getMessage());
     			if (DEBUG) se.printStackTrace(System.out);
 			}
 		}
 		else if (e instanceof IOException)
 		{
 			System.out.println(e.toString());
 			if (DEBUG) e.printStackTrace(System.out);
 		}
 		else
 		{
 			e.printStackTrace(System.out);
 		}
 	}
 
 
     public String getTargetPath()
     {
         return targetURL;
     }
 
 
 	// SourceManager implemenation
 
     public File getRootDir()
     {
         return sourceRootDir;
     }
 
     public java.net.URL getRootDirURL()
     {
         return sourceRootDirURL;
     }
 
 
     public InputStream openSource()
         throws FileNotFoundException
     {
         return new FileInputStream(getSource());
     }
 
 
     public File getSource()
         throws FileNotFoundException
     {
         if (currentSourceFile == null)
             throw new FileNotFoundException("no source file specified");
 
         return currentSourceFile;
     }
 
     public String getSourcePath()
         throws FileNotFoundException
     {
         if (currentSourceFile == null)
             throw new FileNotFoundException("no source file specified");
 
         return sourceDirURL + currentSourceName;
 	}
 
     public java.net.URL getSourceURL()
         throws FileNotFoundException
     {
         try {
             return new java.net.URL(sourceRootDirURL,
                 getSourcePath().substring(1));
         }
         catch (java.net.MalformedURLException e)
         {
             throw new Error("Unable to create file: URL object: "
                 + getSourcePath());
         }
 	}
 
     public boolean sourceHasBeenUpdated(long when)
     {
         File file;
         try {
             file = getSource();
         }
         catch (FileNotFoundException e)
         {
             return false;
         }
 
         long sourceDate = file.lastModified();
 
         return ((sourceDate > 0) // source exist
                 &&
                 // will also build if (when == -1) (i.e. unknown)
                 (sourceDate > when));
     }
 
 
     public boolean fileHasBeenUpdated(String name, long when)
         throws FileNotFoundException
     {
         File file = getFile(name);
         long sourceDate = file.lastModified();
 
         return ((sourceDate > 0) // source exsist
                 &&
                 // will also build if (when == -1) (i.e. unknown)
                 (sourceDate > when));
     }
 
 
     public InputStream openFile(String name)
         throws FileNotFoundException
     {
         return new FileInputStream(getFile(name));
     }
 
 
     public File getFile(String filename)
         throws FileNotFoundException
     {
         return new File(sourceRootDir,
             getFilePath(filename).substring(1).replace('/', File.separatorChar));
     }
 
 
     public String getFilePath(String filename)
         throws FileNotFoundException
     {
         if (filename.charAt(0) == '/')
             return filename;
         else
         {
             if (currentSourceFile == null)
                 throw new FileNotFoundException("no source file specified");
 
             return sourceDirURL + filename;
         }
 	}
 
     public java.net.URL getFileURL(String filename)
         throws FileNotFoundException
     {
         try {
             return new java.net.URL(sourceRootDirURL,
                 getFilePath(filename).substring(1));
         }
         catch (java.net.MalformedURLException e)
         {
             throw new Error("Unable to create file: URL object: "
                 + getFilePath(filename));
         }
 	}
 
     public String getFilePathRelativeTo(String name, String base)
     {
         if (name.charAt(0) == '/')
             return name;
         else
         {
             if (base.charAt(0) != '/')
                 throw new IllegalArgumentException(
                     "base must be pseudo-absolute");
 
             int slash = base.lastIndexOf('/');
             String baseDir = base.substring(0, slash+1);
 
             return baseDir + name;
         }
     }
 
 
 	// FileTarget implemenation
 
     public String getCurrentTargetPath()
     {
         return currentTargetURL;
     }
 
     public void newTarget(String filename)
     {
         this.newTarget = filename;
     }
 
 }
