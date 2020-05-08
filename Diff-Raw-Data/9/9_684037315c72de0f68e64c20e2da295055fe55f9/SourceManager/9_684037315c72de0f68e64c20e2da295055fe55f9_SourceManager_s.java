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
 
 /**
  * Defines operations a Producer can do on the source file tree.
  */
 public interface SourceManager
 {
 		
 	/**
      * Get the URL representing the main source.
 	 * Might be a file or a directory.
      *
 	 * @return an absolute or pseudo-absolute URL, never <code>null</code> 
      * @throws FileNotFoundException if the main source file is not specified
      */
     public String getSourceURL()
         throws FileNotFoundException;
 
 	
     /**
      * Open an auxiallary source file.
      * This might e.g. be used to read the stylesheet for an
      * XSLT transformation.
 	 *
 	 * @param url  URL to the file, if relative it's searched for relative to
 	 * the main source file (or a FileNotFoundException is thrown if
 	 * there is no main source file).
 	 *
      * @return an InputStream to the file.
      */
     public InputStream openFile(String url)
         throws FileNotFoundException, IOException;
 
 
 	/**
      * Get a File object representing the given file or directory.
 	 *
 	 * @param url  URL to the file, if relative it's searched for relative to
 	 * the main source file (or a FileNotFoundException is thrown if
 	 * there is no main source file).
      *
      * @return <code>null</code> if URL is absolute with another scheme than
 	 *  file or res.  
      */
     public File getFile(String url)
         throws FileNotFoundException;
 
 
 	/**
      * Get a TrAX/JAXP Source object representing the given file.
 	 *
 	 * @param url  URL to the file, if relative it's searched for relative to
 	 * the main source file (or a FileNotFoundException is thrown if
 	 * there is no main source file).
 	 * @param target  The current target.
      *
      * @throws FileNotFoundException if the main source file is not specified
      */
     public javax.xml.transform.Source getFileAsJAXPSource(
 			String url, Target target)
         throws FileNotFoundException;
 		
 
 	/**
      * Parse the file as XML and deliver SAX2 events.
 	 *
 	 * @param url  URL to the file, if relative it's searched for relative to
 	 * the main source file (or a FileNotFoundException is thrown if
 	 * there is no main source file).
	 * @param ch  The SAX2 ContentHandler to deliver events to.
 	 * @param target  The current target.
      *
      * @throws FileNotFoundException if the main source file is not specified
      */
 	public void getFileAsSAX(String url, org.xml.sax.ContentHandler sax, 
 							 Target target)
 		throws FileNotFoundException, IOException, org.xml.sax.SAXException;	
 
 		
     /**
      * Get an URL representing the given file or directory.
 	 *
 	 * @param url  URL to the file, if relative it's searched for relative to
 	 * the main source file (or a FileNotFoundException is thrown if
 	 * there is no main source file).
 	 *
 	 * @return an absolute or pseudo-absolute URL
 	 * 		   (the url parameter unchanged unless it's relative)
      */
     public String getFileURL(String url)
         throws FileNotFoundException;
 
 
     /**
      * Check if the specified file has been updated since the specified time.
 	 *
 	 * @param url  URL to the file, if relative it's searched for relative to
 	 * the main source file (or a FileNotFoundException is thrown if
 	 * there is no main source file).
      *
      * @param when  the time
      */
     public boolean fileHasBeenUpdated(String url, long when)
         throws FileNotFoundException, IOException, LagoonException;
 
 }
 
