 /*
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 Copyright (C) 2005 Marco Aurélio Graciotto Silva <magsilva@gmail.com>
 */
 
 package net.sf.ideais.repository;
 
 import net.sf.ideais.repository.RepositoryError;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.tigris.subversion.javahl.ClientException;
 import org.tigris.subversion.javahl.InputInterface;
 import org.tigris.subversion.javahl.OutputInterface;
 import org.tigris.subversion.javahl.Revision;
 import org.tigris.subversion.javahl.SVNAdmin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * Configuration to access a Subversion repository. It's a decorator for
  * SVNAdmin, a administration related Subversion class.
  * 
  * The SVNAdmin, is just a wrapper around JNI interface for administrative
  * commands, with all arguments sent as method parameters (the object has no
  * internal state that interfere with the methos execution). In theory, it
  * (the SVNAdmin) could be safely shared by all SubversionRepository
  * instances, but JNI is strange and sharing it causes some exceptions.
  */
 public class SubversionRepository extends AbstractRepository
 {
 	/**
 	 * Commons Logging instance.
 	 */
 	private static Log log = LogFactory.getLog(SubversionRepository.class);
 
 	/**
 	 * Type identifier string.
 	 */
 	public static final String TYPE = "svn";
 
 	/**
 	 * Internal class which implements the OutputInterface to write the data to a file. This class
 	 * was extracted from SVNTests.java (Subversion 1.1.4). Copyright (c) 2000-2004 Collab.Net. All
 	 * rights reserved.
 	 */
 	public class FileOutputer implements OutputInterface
 	{
 		/**
 		 * The output file stream.
 		 */
 		FileOutputStream myStream;
 
 		/**
 		 * Create the file output stream object.
 		 * 
 		 * @param outputName
 		 *            the file to write the data to
 		 * @throws IOException
 		 */
 		public FileOutputer(File outputName) throws IOException
 		{
 			myStream = new FileOutputStream(outputName);
 		}
 
 		/**
 		 * Write the bytes in data to java.
 		 * 
 		 * @param data
 		 *            the data to be writtem
 		 * @throws IOException
 		 *             throw in case of problems.
 		 */
 		public int write(byte[] data) throws IOException
 		{
 			myStream.write(data);
 			return data.length;
 		}
 
 		/**
 		 * Close the output file.
 		 * 
 		 * @throws IOException
 		 *             throw in case of problems.
 		 */
 		public void close() throws IOException
 		{
 			myStream.close();
 		}
 	}
 
 	/**
 	 * Internal class implements the OutputInterface, but ignores the data This class was extracted
 	 * from SVNTests.java (Subversion 1.1.4). Copyright (c) 2000-2004 Collab.Net. All rights
 	 * reserved.
 	 */
 	public class IgnoreOutputer implements OutputInterface
 	{
 		/**
 		 * Gets the data and send to void.
 		 * 
 		 * @param data
 		 *            the data to be written
 		 * @throws IOException
 		 *             throw in case of problems.
 		 */
 		public int write(byte[] data) throws IOException
 		{
 			return data.length;
 		}
 
 		/**
 		 * Close the output.
 		 * 
 		 * @throws IOException
 		 *             throw in case of problems.
 		 */
 		public void close() throws IOException
 		{
 		}
 	}
 
 	/**
 	 * Internal class which implements the InputInterface to read the data from a file. This class
 	 * was extracted from SVNTests.java (Subversion 1.1.4). Copyright (c) 2000-2004 Collab.Net. All
 	 * rights reserved.
 	 */
 	public class FileInputer implements InputInterface
 	{
 		/**
 		 * Input file stream.
 		 */
 		FileInputStream myStream;
 
 		/**
 		 * Create a new object.
 		 * 
 		 * @param inputName
 		 *            the file from which the data is read
 		 * @throws IOException
 		 */
 		public FileInputer(File inputName) throws IOException
 		{
 			myStream = new FileInputStream(inputName);
 		}
 
 		/**
 		 * Read the number of data.length bytes from input.
 		 * 
 		 * @param data
 		 *            array to store the read bytes.
 		 * @throws IOException
 		 *             throw in case of problems.
 		 */
 		public int read(byte[] data) throws IOException
 		{
 			return myStream.read(data);
 		}
 
 		/**
 		 * Close the input file.
 		 * 
 		 * @throws IOException
 		 *             throw in case of problems.
 		 */
 		public void close() throws IOException
 		{
 			myStream.close();
 		}
 	}
 
 	/**
 	 * Create a new Subversion repository.
 	 */
 	public SubversionRepository()
 	{
 		super();
 	}
 
 	/**
 	 * Change the repository address.
 	 * 
 	 * @param url
 	 *            A valid URL.
 	 * @throws RepositoryError
 	 *             If the URL is invalid.
 	 */
 	public void setLocation(String url)
 	{
 		if (url.startsWith(File.separator)) {
 			File dir = new File(url);
 			if (dir.isDirectory() && dir.canRead() && dir.canWrite()) {
 				url = "file://" + url;
 			} else {
 				throw new RepositoryError("exception.repository.invalidLocation");
 			}
 		} else {
 			try {
 				new URL(url);
 
 			} catch (MalformedURLException e) {
 				throw new RepositoryError("exception.repository.invalidLocation", e);
 			}
 		}
 
 		super.setLocation(url);
 	}
 
 	/**
 	 * Get the repository type (svn, cvs, etc). The repository type must be supported/registered at
 	 * RepositoryTransactionFactory.
 	 * 
 	 * @return The repository type.
 	 */
 	public String getType()
 	{
 		return TYPE;
 	}
 
 	/**
 	 * If the location is of the type "file", return the real physical location of the repository in
 	 * the server. Otherwise, return null;
 	 * 
 	 * @return The pathname for the repository if it's local, null otherwise.
 	 */
 	private String getLocalPath()
 	{
 		try {
 			URL url = new URL(getLocation());
 			if (url.getProtocol().equals("file")) {
 				return url.getPath();
 			}
 		} catch (MalformedURLException e) {
 			// This will never happen, as the location is always checked when first set.
 			throw new RepositoryError("exception.repository.invalidLocation", e);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Initialize a repository (if possible). The backend used is the fsfs (it has less dependencies
 	 * than bdb and it's easier to admin - aka doesn't require recover).
 	 * 
 	 * @throws RepositoryError
 	 *             If the location is not an URL with the "file" protocol or an error occurs while
 	 *             creating the repository.
 	 */
 	public void init()
 	{
 		if (getLocalPath() == null) {
 			throw new UnsupportedOperationException("exception.repository.init");
 		}
 
 		// Only a local path can be initialized.
 		if (getLocalPath() == null) {
 			return;
 		}
 		// If the directory exists, the repository probably is already initialized.
 		if (new File(getLocalPath()).exists()) {
 			return;
 		}
 
 		try {
			SVNAdmi svnAdmin = new SVNAdmin();
 
 			svnAdmin.create(getLocalPath(), false, false, null, SVNAdmin.FSFS);
 
 			svnAdmin.dispose();
 		} catch (ClientException e) {
 			throw new RepositoryError("exception.repository.init", e);
 		}
 	}
 
 	/**
 	 * Dump the repository to a file.
 	 * 
 	 * @return The file with the dump.
 	 * @throws RepositoryError
 	 *             If the location is not an URL with the "file" protocol or an error occurs while
 	 *             dumping the repository.
 	 */
 	public File dump()
 	{
 		File dumpFilename = super.dump();
 
 		if (getLocalPath() == null) {
 			throw new UnsupportedOperationException("exception.repository.dump");
 		}
 
 		try {
 			SVNAdmin svnAdmin = new SVNAdmin();
 
 			OutputInterface dump = new FileOutputer(dumpFilename);
 			OutputInterface errors = new IgnoreOutputer();
 			svnAdmin.dump(getLocalPath(), dump, errors, Revision.START, Revision.HEAD, true);
 
 			svnAdmin.dispose();
 			return dumpFilename;
 		} catch (IOException e) {
 			throw new RepositoryError("exception.repository.dump", e);
 		} catch (ClientException e) {
 			throw new RepositoryError("exception.repository.dump", e);
 		}
 
 	}
 
 	/**
 	 * Load a dump in the repository. Use this with caution!
 	 * 
 	 * @throws RepositoryError
 	 *             If the location is not an URL with the "file" protocol or an error occurs while
 	 *             loading the dump.
 	 */
 	public void load(File dump)
 	{
 		super.load(dump);
 
 		if (getLocalPath() == null) {
 			throw new UnsupportedOperationException("exception.repository.load");
 		}
 
 		try {
 			SVNAdmin svnAdmin = new SVNAdmin();
 
 			InputInterface load = new FileInputer(dump);
 			OutputInterface errors = new IgnoreOutputer();
 			svnAdmin.load(getLocalPath(), load, errors, false, false, null);
 
 			svnAdmin.dispose();
 		} catch (IOException e) {
 			throw new RepositoryError("exception.repository.load", e);
 		} catch (ClientException e) {
 			throw new RepositoryError("exception.repository.load", e);
 		}
 	}
 
 	/**
 	 * Check if the repository is ready for use.
 	 * 
 	 * @return True if ready, false otherwise.
 	 */
 	public static boolean isReady(boolean force) throws Throwable
 	{
 		if (force) {
 			log
 					.debug("Checking SubversionRepository (and trying to setup automatically if necessary)");
 			try {
 				Class.forName("org.tigris.subversion.javahl.SVNAdmin");
 				return true;
 			} catch (NoClassDefFoundError ncdfe) {
 				log.debug("Setting the classpath (ncdfe)");
 				System.setProperty("java.class.path", System.getProperty("java.class.path")
 						+ ":/usr/lib/svn-javahl/svn-javahl.jar");
 			} catch (ClassNotFoundException cnfe) {
 				log.debug("Setting the classpath (cnfe)");
 				System.setProperty("java.class.path", System.getProperty("java.class.path")
 						+ ":/usr/lib/svn-javahl/svn-javahl.jar");
 			} catch (UnsatisfiedLinkError ule) {
 				log.debug("Setting the library path");
 				System.setProperty("java.library.path", System.getProperty("java.library.path")
 						+ ":/usr/lib/svnjavahl.so");
 			}
 		}
 
 		Class.forName("org.tigris.subversion.javahl.SVNAdmin");
 		return true;
 	}
 }
