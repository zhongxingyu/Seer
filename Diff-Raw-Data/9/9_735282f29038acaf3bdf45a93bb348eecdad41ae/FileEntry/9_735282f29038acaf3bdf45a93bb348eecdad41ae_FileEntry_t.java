 /*
  * Copyright (c) 2001-2002, Mikael Stldal
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
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.xml.parsers.*;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import org.xml.sax.*;
 
 import nu.staldal.lagoon.util.*;
 import nu.staldal.util.Utils;
 
 
 /**
  * A file in the sitemap.
  *
  * Contains information on how to (re)build a single file in a website.
  *
  * @see nu.staldal.lagoon.core.Sitemap
  */
 class FileEntry extends EntryWithSource implements SitemapEntry, FileTarget
 {
    private static final boolean DEBUG = true;
 
     private Producer myProducer;
 	private OutputEntry outputEntry;
 
     private final FileStorage targetStorage;
 	private final File tempDir;
 	private final String targetURL;
 
     private String currentSourceURL;
     private String currentTargetURL;
     private String currentTargetDir;
     private String currentTargetName;
     private long targetLastMod;
     private String newTarget;
 	private Vector tempFiles;
 	
 	private Vector targetThreads;
 
 	
     /**
      * Constructor.
      *
      * @param processor the LagoonProcessor.
      * @param sitemap  the Sitemap.
      * @param targetURL  the file to create, may contain wildcard anywhere,
      *                   must be pseudo-absolute.
      * @param sourceURL  the file to use, may contain wildcard in filename,
 	 *                  must absolute or pseudo-absolute, may be <code>null</code>.
      * @param sourceRootDir  absolute path to the source directory
 	 * @param tempDir	where to store temporary files
      */
     public FileEntry(LagoonProcessor processor, Sitemap sitemap, 
 					 String targetURL, String sourceURL,
                      File sourceRootDir, File tempDir)
         throws LagoonException
     {
 		super(processor, sitemap, sourceURL, sourceRootDir);	
 		
         this.targetStorage = processor.getTargetLocation();
 		this.tempDir = tempDir;
 		this.targetURL = targetURL;
 		
         this.myProducer = null;
 		this.outputEntry = null;
 
         this.currentSourceURL = null;
         this.currentTargetURL = null;
         this.targetLastMod = -1;
         this.newTarget = null;
 		
 		this.targetThreads = new Vector();
     }
 
 
     /**
      * Set the ByteStreamProducer that produces the final output for this
      * FileEntry.
      * Used during initialization.
      */
     void setMyProducer(Producer prod)
     {
         myProducer = prod;
     }
 
 
     void setMyOutput(OutputEntry outputEntry)
     {
         this.outputEntry = outputEntry;
     }
 
 	
     public boolean build(boolean always)
         throws IOException
     {
 		if (sourceURL == null)
         {   // no main source
 	        currentSourceURL = null;
             currentTargetURL = targetURL;
             return buildFile(always);
         }
         else if (Utils.absoluteURL(sourceURL))
         {   // absolute URL
 	        currentSourceURL = sourceURL;
             currentTargetURL = targetURL;
             return buildFile(always);
         }
         else if (Wildcard.isWildcard(sourceURL))
         {   // main source is a wildcard pattern
 			int slash = sourceURL.lastIndexOf('/');
 			String sourceDirURL = sourceURL.substring(0, slash+1);
 			String sourceMask = sourceURL.substring(slash+1);
 			File sourceDir = new File(sourceRootDir, sourceDirURL); 
             
 			String[] files = sourceDir.list();
 			if (files == null)
 			{
 				throw new FileNotFoundException(
 					sourceDir.getAbsolutePath() + " (directory not found)");	
 			}
 			boolean success = true;
             for (int i = 0; i < files.length; i++)
             {				
                 File currentSourceFile = new File(sourceDir, files[i]);
                 if (!currentSourceFile.isFile()) continue;
 
                 String part = Wildcard.matchWildcard(sourceMask, files[i]);
                 if (part == null) continue;
 
 				currentSourceURL = sourceDirURL + files[i];
 
                 currentTargetURL =
                     Wildcard.instantiateWildcard(targetURL, part);
                 if (!buildFile(always)) success = false;
             }
 			return success;
         }
         else
         {   // main source is a regular file
 	        currentSourceURL = sourceURL;
             currentTargetURL = targetURL;
             return buildFile(always);
         }
     }
 	
 
     private boolean buildFile(boolean always)
         throws IOException
     {
         if (DEBUG) System.out.println("buildFile: " + currentTargetURL);
 
         targetLastMod = targetStorage.fileLastModified(currentTargetURL);
 
         if (always || (targetLastMod <= 0))
         {
             return buildAlways();
         }
 
 		boolean success = true;
         boolean updated = false;
         try {
             updated = myProducer.hasBeenUpdated(targetLastMod);
         }
         catch (LagoonException e)
         {
             reportException(e);
 			success = false;
         }
         catch (IOException e)
         {
             reportException(e);
 			success = false;
         }
 
         if (updated)
         {
             if (!buildAlways()) success = false;
         }
 		return success;
     }
 	
 
     /**
      * The actual building of this file.
      * Used after any dependency checking indicates the file needs rebuilding.
      */
     private boolean buildAlways()
         throws IOException
     {
         processor.log.println("Building: " + currentTargetURL);
 		
 		int slash = currentTargetURL.lastIndexOf('/');
 		currentTargetDir = currentTargetURL.substring(0, slash+1);
 		currentTargetName = currentTargetURL.substring(slash+1);
 
         String thisTargetURL;
         OutputHandler out = null;
         String exceptionType = null;
         boolean bailOut = false;
 		boolean success = true;
 		
 		ByteStreamProducer theProducer;
 		if (outputEntry == null)
 		{
 			theProducer = (ByteStreamProducer)myProducer;	
 		}
 		else
 		{
 			outputEntry.setNext((XMLStreamProducer)myProducer);
 			outputEntry.setSourceManager(this);
 			theProducer = outputEntry.getByteProducer();
 		}
 
 		tempFiles = new Vector();
 		
 		newTarget = currentTargetName;
 
         do {
 			thisTargetURL = currentTargetDir + newTarget;
             newTarget = null;
 	        out = targetStorage.createFile(thisTargetURL);
 			
 			try {
                 theProducer.start(out.getOutputStream(), this);
                 exceptionType = null; // no exception thrown
             }
             catch (Exception e)
             {
 				success = false;
 				if (DEBUG) e.printStackTrace();	
 				
 				String thisExceptionType = e.getClass().getName();
 
 				// the same type of exception thrown twice in a row
 				if (thisExceptionType.equals(exceptionType))
 				{
 					bailOut = true;
 				}
 				exceptionType = thisExceptionType;
 
                 e = reportException(e);
 
 				if (out != null) out.discard();				
 
 				if (e instanceof RuntimeException)
 				{
 					throw (RuntimeException)e;
 				}
 
                 if (bailOut)
                 {
 					processor.err.println("Error building " + currentTargetURL
 						+ ": Too many exceptions, bailing out");
 					break;
 				}
 				else
 				{
 					continue;
 				}
             }
 
             out.commit();
         } while (newTarget != null);
 
         byte[] buf = new byte[8192];
 		
 		for (int i = 0; i<tempFiles.size(); i++)
 		{
 			String path = (String)tempFiles.elementAt(i);
 			File tempFile = new File(tempDir, "temp" + i);
 			FileInputStream fis = new FileInputStream(tempFile);
 			
 			OutputHandler oh = targetStorage.createFile(path);
 	
 			try {
     	    	while (true)
         		{
             		int bytesRead = fis.read(buf);
             		if (bytesRead < 1) break;
             		oh.getOutputStream().write(buf, 0, bytesRead);
         		}
 	        	fis.close();
 				tempFile.delete();
 			}
 			catch (IOException e)
 			{
 				success = false;
 				reportException(e);
 				oh.discard();
 				break;
 			}
 			oh.commit();			
 		}
 		
 		for (Enumeration enum = targetThreads.elements(); 
 			 enum.hasMoreElements(); )
 		{
 			Thread t = (Thread)enum.nextElement();
 			try {
 				if (t != null) t.join();
 			}
 			catch (InterruptedException e) {}
 		}
 		targetThreads.clear();
 		
 		return success;
     }
 
 
 	private Exception reportException(Exception e)
 	{
 		if (e instanceof RuntimeException)
 		{
 			return e;	
 		}
 		if (e instanceof SAXParseException)
 		{
 			SAXParseException spe = (SAXParseException)e;
 			Exception ee = spe.getException();
 			if (ee instanceof RuntimeException)
 			{
 				return ee;
 			}
 			String sysId = (spe.getSystemId() == null)
 				? ("(" + currentTargetURL + ")"): spe.getSystemId();
 			processor.err.println(sysId + ":" + spe.getLineNumber()
 				+ ":" + spe.getColumnNumber() + ": " + spe.getMessage());
 		}
 		else if (e instanceof SAXException)
 		{
 			SAXException se = (SAXException)e;
 			Exception ee = se.getException();
 			if (ee instanceof RuntimeException)
 			{
 				return ee;
 			}
			else if (ee != null)
			{
				processor.err.println("Error building " + currentTargetURL
					+ ": " + ee.toString());
    			if (DEBUG) ee.printStackTrace(System.out);
			}
 			else
 			{
 				processor.err.println("Error building " + currentTargetURL
 					+ ": " + se.getMessage());
     			if (DEBUG) se.printStackTrace(System.out);
 			}
 		}
 		else if (e instanceof IOException)
 		{
 			processor.err.println("Error building " + currentTargetURL
 					+ ": " + e.toString());
 			if (DEBUG) e.printStackTrace(System.out);
 		}
 		else
 		{
 			processor.err.println("Error building " + currentTargetURL + ":");
 			e.printStackTrace(processor.err);
 		}
 		return e;
 	}
 
 
 	// Partial SourceManager implemenation
    
     public String getSourceURL()
         throws FileNotFoundException
 	{
         if (currentSourceURL == null)
             throw new FileNotFoundException("no source file specified");
 
 		return currentSourceURL;		
 	}
 	
 	
 	// FileTarget implemenation
 
     public String getCurrentTargetURL()
     {
         return currentTargetURL;
     }
 
     public void newTarget(String filename, boolean prependFilename)
     {
 		if (prependFilename)
 		{
 			this.newTarget = currentTargetName + '_' + filename;	
 		}
 		else
 		{
 			this.newTarget = filename;
 		}
     }
 
     public OutputHandler newAsyncTarget(String filename, 
 			boolean prependFilename)
 		throws IOException
 	{
 		if (filename.charAt(0) != '/' && !prependFilename)
 		{
 			filename = currentTargetDir + filename;
 		}
 		else if (filename.charAt(0) != '/' && prependFilename)
 		{			
 			filename = currentTargetDir + currentTargetName + '_' + filename;
 		}
 			
 		if (DEBUG) System.out.println("New async target: " + filename);
 		
 		if (targetStorage.isReentrant())
 		{
 			return targetStorage.createFile(filename);
 		}
 		else
 		{
 			tempFiles.addElement(filename);			
 			File currentFile = new File(tempDir, "temp" + (tempFiles.size()-1));	
 			return new TempOutputHandler(currentFile,
 				new FileOutputStream(currentFile));
 		}
 	}	
 
 
     private static void sleepUntilInterrupted()
     {
         try {
             while (true)
                 Thread.sleep(1000*60); // Sleep one minute
         }
         catch (InterruptedException e) {}
     }
 
 	
 	private Thread mainThread;
 	private ContentHandler asyncSAX;
 	private Exception asyncException;
 	
     public ContentHandler newAsyncTargetWithOutput(
 			String filename, boolean prependFilename, String outputName)
 		throws java.io.IOException, SAXException
 	{
 		mainThread = Thread.currentThread();
 		asyncSAX = null;
 		asyncException = null;
 		
 		final OutputHandler oh = newAsyncTarget(filename, prependFilename);
 		
 		final OutputEntry outputEntry = sitemap.lookupOutput(outputName);
 		if (outputEntry == null) throw new LagoonException(
 			"Output entry " + outputName + " not found in Sitemap");
 
 		outputEntry.setSourceManager(this);
 				
 		outputEntry.setNext(new XMLStreamProducer() {
 		    public void start(ContentHandler sax, Target target)
         		throws SAXException, IOException
 			{
 				asyncSAX = sax;
 				mainThread.interrupt();
 			}
 			
 			public void init()
 			{
 				throw new RuntimeException("Invalid context");	
 			}
 
 			public boolean hasBeenUpdated(long lastBuild) 
 			{
 				throw new RuntimeException("Invalid context");	
 			}
 		});
 
 		Thread targetThread = new Thread(new Runnable() {
 			public void run()
 			{
 				if (DEBUG) System.out.println("TargetThread just started");
 				try {
 					try {
 						outputEntry.getByteProducer().start(
 							oh.getOutputStream(), 
 							FileEntry.this);
 					}
 					catch (Exception e)
 					{
 						oh.discard();
 						throw e;
 					}
 					oh.commit();
 				}
 				catch (Exception e)
 				{
 					asyncException = e;
 					mainThread.interrupt();
 				}
 				if (DEBUG) System.out.println("TargetThread about to end");
 			}
 		}, "TargetThread");
 		targetThreads.addElement(targetThread);
 		targetThread.start();
 
 		if (DEBUG) System.out.println("Waiting for TargetThread...");
 	 	sleepUntilInterrupted();
 		if (DEBUG) System.out.println("...finished waiting for TargetThread");
 		
 		if (asyncException != null)
 		{
 			if (asyncException instanceof IOException)
 				throw (IOException)asyncException;
 			else if (asyncException instanceof SAXException)
 				throw (SAXException)asyncException;
 			else if (asyncException instanceof RuntimeException)
 				throw (RuntimeException)asyncException;
 			else
 				throw new SAXException(asyncException);
 		}
 		
 		return asyncSAX;
 	}	
 
 
 	public boolean isWildcard()
     {
         return Wildcard.isWildcard(sourceURL);
 	}
 
 
 	static class TempOutputHandler extends OutputHandler
 	{
 		private File currentFile;
 		
 		TempOutputHandler(File currentFile, OutputStream out)
 		{
 			super(out);
 			this.currentFile = currentFile;
 		}
 		
 		public void commit()
 			throws java.io.IOException
 		{
 			out.close();
 		}
 
 		public void discard()
 			throws java.io.IOException
 		{
 			out.close();
 			if (!currentFile.exists()) return;
 			if (currentFile.delete())
 			{
 				return;
 			}
 			else
 			{
 				throw new IOException("Unable to delete file: " + currentFile);
 			}
 		}
 	}
 	
 }
 
