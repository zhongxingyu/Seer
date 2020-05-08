 package org.eclipse.emf.compare.tests.util;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
import java.util.Arrays;
 
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.filebuffers.ITextFileBuffer;
 import org.eclipse.core.filebuffers.ITextFileBufferManager;
 import org.eclipse.core.filebuffers.manipulation.ConvertLineDelimitersOperation;
 import org.eclipse.core.filebuffers.manipulation.FileBufferOperationRunner;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 
 /**
  * File utilities for Unit tests getting expected results from the JUnit project.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public final class FileUtils {
 	private FileUtils() {
 		// prevents instantiation.
 	}
 
 	/**
 	 * Resolves a URL to a location string as a file system absolute path.
 	 * 
 	 * @param urlString
 	 * 			An URL string, possibly using the platform:/ format.
 	 * @return
 	 * 			An absolute file system path corresponding to <code>urlString</code>.
 	 * @throws IOException
 	 * 			Thrown if an I/O operation has failed or been interrupted.
 	 * @throws MalformedURLException
 	 * 			If urlString is an invalid URL.
 	 */
 	public static String resolveURLToLocationString(final String urlString)
 			throws IOException, MalformedURLException {
 		final String fileName = FileLocator.resolve(new URL(urlString)).getFile();
 		return fileName;
 	}
 
 	/**
 	 * Return the file contents as a string. This method also converts the file's 
 	 * line separator to the current system's line separator if 
 	 * <code>convertNewLines</code> is set to <code>true</code>.
 	 * 
 	 * @param urlString
 	 * 			An URL string, possibly using the platform:/ format.
 	 * @param convertNewLines
 	 * 			Convert the file line separators to the system line separator.
 	 * @return
 	 * 			A String representing the file contents.
 	 * @throws IOException
 	 * 			Thrown if an I/O operation has failed or been interrupted.
 	 * @throws MalformedURLException
 	 * 			If urlString is an invalid URL
 	 * @throws CoreException
 	 * 			If an error occurs while reading the file.
 	 */
 	public static String getFileContents(final String urlString,
 			final boolean convertNewLines) throws MalformedURLException, IOException,
 			CoreException {
 
 		final ITextFileBufferManager textBufferMgr = FileBuffers
 				.getTextFileBufferManager();
 		final String fileName = resolveURLToLocationString(urlString);
 
 		final IPath path = new Path(fileName);
 		textBufferMgr.connect(path, new NullProgressMonitor());
 		final ITextFileBuffer textFileBuffer = textBufferMgr
 				.getTextFileBuffer(path);
 
 		if (convertNewLines) {
 			new FileBufferOperationRunner(textBufferMgr, null).execute(
 					new IPath[] { path }, new ConvertLineDelimitersOperation(
 							System.getProperty("line.separator")),
 					new NullProgressMonitor());
 		}
 
 		final String result = textFileBuffer.getDocument().get();
 		textBufferMgr.disconnect(path, new NullProgressMonitor());
 		return result;
 	}
 	
 	/**
 	 * Lists all subdirectories contained within a given folder, with the exception
 	 * of directories starting with a "." or directories named "CVS".
 	 * 
 	 * @param aDirectory
 	 * 			Directory from which we need to list subfolders.
 	 * @return
 	 * 			Array composed by all <code>aDirectory</code> subfolders.
 	 */
 	public static File[] listDirectories(File aDirectory) {
 		File[] directories = null;
 		
 		if (aDirectory.exists() && aDirectory.isDirectory()) {
 			directories = aDirectory.listFiles(new FileFilter() {
 				public boolean accept(File file) {
 					return file.isDirectory() 
 						&& !file.getName().startsWith(".")
 						&& !file.getName().equals("CVS");
 				}
 			});
 		}
		Arrays.sort(directories);
 		return directories;
 	}
 }
