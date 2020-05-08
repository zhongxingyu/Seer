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
 
 // import org.xml.sax.SAXException;
 
 import nu.staldal.xtree.*;
 import nu.staldal.lagoon.util.LagoonUtil;
 
 
 /**
  * The main worker class of the Lagoon core.
  *
  * Initialized with InputStream for sitemap,
  * a source dir and a target storage URL.
  * Then building the website may be done several times,
  * until destroy() is invoked.
  *
  * This class is not thread-safe. The methods must not
  * be invoked concurrently from different threads.
  */
 public class LagoonProcessor
 {
 	private static final boolean DEBUG = false;
 
     private final String targetURL;
     private final FileStorage targetLocation;
     private File repositoryDir;
 
     private final Hashtable classDict;
     private final Hashtable paramDict;
     private final Hashtable filestorageDict;
 
     protected Sitemap sitemap;
 
 
     /**
      * Constructor.
      *
      * @param targetURL  where to put the generated files,
 	 *                   must be an absolute URL or a local file path
      */
     public LagoonProcessor(String targetURL)
         throws IOException, LagoonException
     {
         this.targetURL = targetURL;
 
         classDict = new Hashtable();
         paramDict = new Hashtable();
         filestorageDict = new Hashtable();
 
         targetLocation = createFileStorage(targetURL);
     }
 
     /**
      * Ask if the file storage needs a password.
      *
      */
     public boolean needPassword()
     {
         return targetLocation.needPassword();
     }
 
     /**
      * Initialize this processor.
 	 *
	 * The InputStream for the sitemap is not used after
	 * this method returns.
     *
      * @param sitemapTree  the Sitemap as an XTree
      * @param sourceDir  where the source files are
      * @param password  password to access the target storage, or
      *                  <code>null</code> if not nessesary.
      */
     public void init(Element sitemapTree, File sourceDir, String password)
         throws IOException, LagoonException, AuthenticationException			
     {
    		sitemap = new Sitemap(sitemapTree);
 
 		File workDir = new File(System.getProperty("user.home"), ".lagoon");
 		
 		if (!workDir.exists())
 		{
 			if (!workDir.mkdir())
 				throw new IOException("Unable to create directory: "
 					+ workDir);
 		}
 		else
 		{
 			if (!workDir.isDirectory())
 			{
 				throw new IOException(
 					"Unable to create directory (a file with that name exists): "
 					+ workDir);
 			}
 		}
 		
 		repositoryDir = new File(workDir, sitemap.getSiteName());
 		if (!repositoryDir.exists())
 		{
 			if (!repositoryDir.mkdir())
 				throw new IOException("Unable to create directory: "
 					+ repositoryDir);
 		}
 		else
 		{
 			if (!repositoryDir.isDirectory())
 			{
 				throw new IOException(
 					"Unable to create directory (a file with that name exists): "
 					+ repositoryDir);
 			}
 		}
 
         targetLocation.open(targetURL, this, password);
 
    		sitemap.init(this, sitemapTree, sourceDir, targetLocation);
     }
 
     /**
      * Perform the building of the website.
      * May be invoked multiple times.
      * Synchronous, returns when the building is complete.
      *
      * @param force force a rebuild of all files, otherwise dependency
      *        checking is used to check which files that needs rebuilding.
      */
     public void build(boolean force)
         throws IOException
     {
         for (Enumeration e = sitemap.getEntries(); e.hasMoreElements(); )
         {
             SitemapEntry ent = (SitemapEntry)e.nextElement();
             ent.build(force);
         }
     }
 
 	/**
 	 * Dispose this object and release any resources it holds.
 	 * This causes the FileStorage to be closed.
 	 */
 	public void destroy()
         throws IOException
 	{
 		targetLocation.close();
 	}
 
 
     /**
      * Read from a file in the repository.
      * Read from the returned InputStream and close() it.
      *
      * @param key  the key to locate the file
      *
      * @return an InputStream to read the file from, or <code>null</code>
      * if the file wasn't found.
      */
     public InputStream readFileFromRepository(String key)
     {
         return readFileFromRepository(null, key);
     }
 
     InputStream readFileFromRepository(String dir, String key)
     {
         File theDir = (dir == null)
                     ? repositoryDir
                     : new File(repositoryDir, dir);
 
         try {
             return new FileInputStream(new File(theDir, key));
         }
         catch (FileNotFoundException e)
         {
             return null;
         }
     }
 
     /**
      * Store a file in the repository.
      * Write to the returned OutputStream and close() it.
      *
      * @param key  the key to locate the file
      *
      * @return  an OutputStream to write to the file
      */
     public OutputStream storeFileInRepository(String key)
         throws IOException
     {
         return storeFileInRepository(null, key);
     }
 
     OutputStream storeFileInRepository(String dir, String key)
         throws IOException
     {
         File theDir = (dir == null)
                     ? repositoryDir
                     : new File(repositoryDir, dir);
         theDir.mkdir();
 
         return new FileOutputStream(new File(theDir, key));
     }
 
     /**
      * Get an object from the repository.
      *
      * @param key  the key to locate the object
      *
      * @return  the object, or <code>null</code> if not found
      */
     public Object getObjectFromRepository(String key)
         throws IOException
     {
         return getObjectFromRepository(null, key);
     }
 
     Object getObjectFromRepository(String dir, String key)
         throws IOException
     {
         InputStream is = readFileFromRepository(dir, key);
         if (is == null) return null;
 
         ObjectInputStream ois = new ObjectInputStream(is);
         try {
             return ois.readObject();
         }
         catch (ClassNotFoundException e)
         {
             if (DEBUG) System.out.println(e);
             return null;
         }
         catch (ObjectStreamException e)
         {
             if (DEBUG) System.out.println(e);
             return null;
         }
         catch (ClassCastException e)
         {
             if (DEBUG) System.out.println(e);
             return null;
         }
 		finally
 		{
 			is.close();	
 		}
     }
 
     /**
      * Store an object into the repository.
      *
      * @param key  the key to locate the object
      * @param obj  the object to store, must be Serializable
      */
     public void putObjectIntoRepository(String key, Object obj)
         throws IOException
     {
         putObjectIntoRepository(null, key, obj);
     }
 
     void putObjectIntoRepository(String dir, String key, Object obj)
         throws IOException
     {
         OutputStream os = storeFileInRepository(dir, key);
 
         ObjectOutputStream oos = new ObjectOutputStream(os);
         try {
 			oos.writeObject(obj);
 		}
 		finally
 		{
 			oos.close();
 		}
     }
 
 
     /**
      * Create a new producer.
      *
      * @param cat  the producer category (format, transform, source,
 	 *             read, parse or process).
      * @param type the producer type, use "" for default.
      *
      * @return  a new Producer
      *          or <code>null</code> if it cannot be found.
      */
     public Producer createProducer(String cat, String type)
         throws LagoonException
     {
         Class cls = (Class)classDict.get(cat + ":" + type);
 
         if (cls == null)
         try
         {
 			String fileName = "/nu/staldal/lagoon/producer/" + cat
 				+ ((type == "") ? "" : ("-" + type));
 
 			InputStream is = getClass().getResourceAsStream(fileName);
 
 			if (is == null) return null;
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			String className = br.readLine();
 			if (className == null)
 				throw new LagoonException(
 					"Illegal Producer config file: " + fileName);
 
 			String baseClassName =
 				"nu.staldal.lagoon.core." +
 				Character.toUpperCase(cat.charAt(0)) + cat.substring(1);
 
 			try {
 				cls = Class.forName(className);
 				if (!Class.forName(baseClassName).isAssignableFrom(cls))
 					throw new LagoonException(
 						cat + " class must derive from " + baseClassName);
 
 				classDict.put(cat + ":" + type, cls);
 			}
 			catch (ClassNotFoundException e)
 			{
 				throw new LagoonException(
 					"Producer class cannot be found:" + e.getMessage());
 			}
 
 			Hashtable params = new Hashtable();
 
 			for (;;)
 			{
 				String s = br.readLine();
 				if (s == null) break;
 
 				int colon = s.indexOf(':');
 				if (colon < 1)
 					throw new LagoonException(
 						"Illegal producer config file: " + fileName);
 
 				String paramName = s.substring(0, colon).trim();
 				String paramValue = s.substring(colon+1).trim();
 
 				params.put(paramName, paramValue);
 			}
 			br.close();
 
 			paramDict.put(cat + ":" + type, params);
 		}
         catch (IOException e)
         {
             throw new LagoonException(
                 "Unable to read producer config file: " + e.toString());
         }
 
         try {
             Producer prod = (Producer)cls.newInstance();
 
             Hashtable params = (Hashtable)paramDict.get(cat + ":" + type);
 
             for (Enumeration e = params.keys(); e.hasMoreElements(); )
             {
                 String name = (String)e.nextElement();
                 String value = (String)params.get(name);
                 prod.addParam(name, value);
             }
 
             return prod;
         }
         catch (IllegalAccessException e)
         {
             throw new LagoonException(
                 "Unable to instantiate producer class (illegal access): " +
                 e.getMessage());
         }
         catch (InstantiationException e)
         {
             throw new LagoonException(
                 "Unable to instantiate producer class (instantiation failed): " +
                 e.getMessage());
         }
     }
 
 
     /**
      * Create a new file storage
      *
      * @param url  the URL
      *
      * @return  a new FileStorage
      *          or <code>null</code> if it cannot be found.
      */
     public FileStorage createFileStorage(String url)
         throws LagoonException
     {
         Class cls = null;
 
         String prefix;
         int colon = url.indexOf(':');
         if (colon < 1)
         {
 			prefix = "";
             cls = (Class)filestorageDict.get("");
         }
         else
         {
             prefix = url.substring(0, colon);
             cls = (Class)filestorageDict.get(prefix);
             if (cls == null)
             {
                 cls = (Class)filestorageDict.get("");
             }
         }
 
         if (cls == null)
         try
         {
 			String fileName = "/nu/staldal/lagoon/filestorage/FileStorage"
 				+ ((prefix == "") ? "" : ("-" + prefix));
 
 			InputStream is = getClass().getResourceAsStream(fileName);
 
 			if (is == null)
 			{
 				fileName = "/nu/staldal/lagoon/filestorage/FileStorage";
 
 				is = getClass().getResourceAsStream(fileName);
 
 				prefix = "";
 			}
 
 			if (is == null) return null;
 
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			String className = br.readLine();
 			br.close();
 			if (className == null)
 				throw new LagoonException(
 					"Illegal FileStorage config file: " + fileName);
 
 			String baseClassName = "nu.staldal.lagoon.core.FileStorage";
 
 			try {
 				cls = Class.forName(className);
 				if (!Class.forName(baseClassName).isAssignableFrom(cls))
 					throw new LagoonException(
 						"file storage class must derive from "
 						+ baseClassName);
 
 				filestorageDict.put(prefix, cls);
 			}
 			catch (ClassNotFoundException e)
 			{
 				throw new LagoonException(
 					"FileStorage class cannot be found:" + e.getMessage());
 			}
 		}
         catch (IOException e)
         {
             throw new LagoonException(
                 "Unable to read producer config file: " + e.toString());
         }
 
         try {
             return (FileStorage)cls.newInstance();
         }
         catch (IllegalAccessException e)
         {
             throw new LagoonException(
                 "Unable to instantiate file storage class (illegal access): "
                 + e.getMessage());
         }
         catch (InstantiationException e)
         {
             throw new LagoonException(
                 "Unable to instantiate file storage class (instantiation failed): "
                 + e.getMessage());
         }
     }
 
 }
