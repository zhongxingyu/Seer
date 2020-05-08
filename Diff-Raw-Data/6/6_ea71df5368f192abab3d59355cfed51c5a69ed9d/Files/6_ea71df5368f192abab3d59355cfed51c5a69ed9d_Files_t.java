 package net.meisen.general.genmisc.types;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.FileNameMap;
 import java.net.URI;
 import java.net.URLConnection;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import net.meisen.general.genmisc.resources.Resource;
 import net.meisen.general.genmisc.resources.ResourceInfo;
 import net.meisen.general.genmisc.unicode.UnicodeReader;
 
 /**
  * Utility class which provides standard functionalities concerning {@link File}
  * and file-system dependent tasks.
  * 
  * @author pmeisen
  * 
  */
 public class Files {
 	/**
 	 * The name of the mime-types definitions
 	 */
 	public final static String MIMETYPES_FILENAME = "mimetypes.properties";
 	/**
 	 * All defined extension, mime-types pairs
 	 */
 	public final static Map<String, String> MIMETYPES = new HashMap<String, String>();
 	static {
 
 		// check for a file
 		InputStream in = Files.class.getResourceAsStream(MIMETYPES_FILENAME);
 
 		// get the default
 		if (in == null) {
 			final String path = "/"
 					+ Files.class.getPackage().getName().replace('.', '/')
 					+ "/mimetypes/" + MIMETYPES_FILENAME;
 			in = Files.class.getResourceAsStream(path);
 		}
 
 		// still null
 		if (in == null) {
 			System.err.println(Files.class.getName() + ": Could not find '"
 					+ MIMETYPES_FILENAME + "' on class-path");
 		}
 
 		try {
 			final Properties props = Streams.readPropertiesFromStream(in);
 
 			for (Entry<Object, Object> entry : props.entrySet()) {
 				MIMETYPES.put(entry.getKey().toString(), entry.getValue()
 						.toString());
 			}
 		} catch (final IOException e) {
 			System.err.println(Files.class.getName()
 					+ ": Could not determine any mime-types (Message: "
 					+ e.getLocalizedMessage() + "'");
 		}
 	}
 
 	/**
 	 * The default mime-type used if not determinable by the file-name
 	 */
 	public final static String DEFAULT_MIMETYPE = "text/plain";
 
 	/**
 	 * This function checks a path to a directory, if it exists and if it is a
 	 * directory
 	 * 
 	 * @param pathName
 	 *            the path to be checked
 	 * @return <code>null</code> if the check was not successful or if the
 	 *         passed path name was <code>null</code>, otherwise the canonical
 	 *         path (see {@link Files#getCanonicalPath(String)})
 	 */
 	public static String checkDirectory(final String pathName) {
 		try {
 			return checkDirectory(pathName, false);
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * This function checks a path to a directory, if it exists and if it is a
 	 * directory
 	 * 
 	 * @param pathName
 	 *            the path to be checked
 	 * @param createIfNotExists
 	 *            tries to create the directory if it does not exist
 	 * @return <code>null</code> if the check was not successful or if the
 	 *         passed path name was <code>null</code>, otherwise the canonical
 	 *         path (see {@link Files#getCanonicalPath(String)})
 	 * @throws IOException
 	 *             if the folder could not be created and if asked for it
 	 */
 	public static String checkDirectory(final String pathName,
 			boolean createIfNotExists) throws IOException {
 		String checkedFolder = null;
 
 		if (pathName == null)
 			return null;
 		final File path = new File(pathName);
 
 		if (!path.exists())
 			checkedFolder = null;
 		else if (!path.isDirectory())
 			checkedFolder = null;
 		else
 			checkedFolder = getCanonicalPath(path);
 
 		if (checkedFolder == null && createIfNotExists) {
 
 			// lets try to create it
 			final File createFolder = new File(pathName);
 
 			if (!createFolder.mkdirs()) {
 				throw new IOException("The folder '" + pathName
 						+ "' cannot be accessed");
 			}
 			return getCanonicalPath(path);
 		} else
 			return checkedFolder;
 	}
 
 	/**
	 * Deletes all files and sub-directories under the specified directory. <br/>
	 * <br/>
	 * <b>Note:</b><br/>
 	 * The function doesn't check if the passed <code>dir</code> is really a
 	 * directory, therefore a file will be deleted as well.
 	 * 
 	 * @param dir
 	 *            the directory to delete
 	 * @return <code>true</code> if all deletions were successful, if a deletion
 	 *         fails, the method stops attempting to delete and returns
 	 *         <code>false</code>
 	 */
 	public static boolean deleteDir(final File dir) {
 
 		if (!dir.exists()) {
 			return true;
 		} else if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean success = deleteDir(new File(dir, children[i]));
 				if (!success) {
 					return false;
 				}
 			}
 		}
 
 		// The directory is now empty so delete it
 		return dir.delete();
 	}
 
 	/**
 	 * Deletes a <code>Collection</code> of files and directories
 	 * 
 	 * @param files
 	 *            the <code>Collection</code> of files to be deleted
 	 * 
 	 * @return <code>true</code> if all deletions were successful, if a deletion
 	 *         fails, the method stops attempting to delete and returns
 	 *         <code>false</code>
 	 */
 	public static boolean bulkDeleteFiles(final Collection<File> files) {
 		boolean success = true;
 
 		// make sure that we have some files
 		if (files == null) {
 			return true;
 		}
 
 		// delete the files
 		for (final File file : files) {
 			if (file != null && file.exists()) {
 				success = file.delete();
 				if (!success) {
 					return false;
 				}
 			}
 		}
 
 		return success;
 	}
 
 	/**
 	 * Deletes a <code>Collection</code> of files and directories
 	 * 
 	 * @param files
 	 *            the <code>Collection</code> of files to be deleted
 	 * 
 	 * @return <code>true</code> if all deletions were successful, if a deletion
 	 *         fails, the method stops attempting to delete and returns
 	 *         <code>false</code>
 	 */
 	public static boolean bulkDeleteFileNames(final Collection<String> files) {
 
 		// if there are no files we are done directly
 		if (files == null) {
 			return true;
 		}
 
 		// create a collection of files
 		final Collection<File> fileList = new ArrayList<File>();
 		for (final String file : files) {
 			fileList.add(new File(file));
 		}
 
 		// delete the files
 		return bulkDeleteFiles(fileList);
 	}
 
 	/**
 	 * Returns all files of the specified directory and all files in sub-folders
 	 * 
 	 * @param dir
 	 *            the directory to get the files from
 	 * @param fileList
 	 *            the list of files the new files should be appended to, if
 	 *            <code>null</code> a new list will be created
 	 * @return the {@link List} of all files within the directory or a
 	 *         subdirectory
 	 */
 	public static List<File> getFilelist(final File dir,
 			final List<File> fileList) {
 		return getFilelist(dir, fileList, (FileFilter) null);
 	}
 
 	/**
 	 * Returns all files of the specified directory and all files in
 	 * sub-folders, which have a specified file-name.
 	 * 
 	 * @param dir
 	 *            the directory to delete
 	 * @param fileList
 	 *            the list of files the new files should be appended to, if
 	 *            <code>null</code> a new list will be created
 	 * @param searchFileName
 	 *            the file-name to search for
 	 * @return the {@link List} of all files within the directory or a
 	 *         subdirectory having the specified <code>searchFileName</code>
 	 */
 	public static List<File> getFilelist(final File dir,
 			final List<File> fileList, final String searchFileName) {
 
 		// a null file can never be found
 		if (searchFileName == null) {
 			return new ArrayList<File>();
 		}
 
 		// create a filter
 		final FileFilter filter = new FileFilter() {
 
 			@Override
 			public boolean accept(final File file) {
 				return isFileEqual(file, searchFileName);
 			}
 		};
 
 		return getFilelist(dir, fileList, filter);
 	}
 
 	/**
 	 * Returns all files of the specified directory and all files in
 	 * sub-folders, which have a specified file-name.
 	 * 
 	 * @param dir
 	 *            the directory to get the files from
 	 * @param fileList
 	 *            the list of files the new files should be appended to, if
 	 *            <code>null</code> a new list will be created
 	 * @param searchPattern
 	 *            the <code>Pattern</code> the files have to apply to
 	 * 
 	 * @return the {@link List} of all files within the directory or a
 	 *         subdirectory matching the specified <code>Pattern</code>
 	 */
 	public static List<File> getFilelist(final File dir,
 			final List<File> fileList, final Pattern searchPattern) {
 
 		// a null file can never be found
 		if (searchPattern == null) {
 			return new ArrayList<File>();
 		}
 
 		// create a filter
 		final FileFilter filter = new FileFilter() {
 
 			private final String dirRegEx = "\\Q" + Files.getCanonicalPath(dir)
 					+ "\\E";
 
 			@Override
 			public boolean accept(final File file) {
 				final String filePath = Files.getCanonicalPath(file);
 				final String subFilePath = filePath.replaceFirst(dirRegEx, "");
 
 				return searchPattern.matcher(subFilePath).matches();
 			}
 		};
 
 		return getFilelist(dir, fileList, filter);
 	}
 
 	/**
 	 * Checks if a file has the specified <code>cmpFileName</code>. The
 	 * comparison is not as easy as it might look like. The reason for this are
 	 * the different OS with different case-sensitive and insensitive file-name
 	 * rules. Therefore this function tries to answer equality based on the OS
 	 * rule, i.e.:<br />
 	 * <br />
 	 * 
 	 * <b>Windows OS:</b><br />
 	 * The file C:\sample.file equals the file-name Sample.file or SaMpLe.file <br />
 	 * <br />
 	 * <b>Linux:</b><br />
 	 * The file C:\sample.file equals the file-name sample.file but not any
 	 * other case
 	 * 
 	 * @param file
 	 *            the {@link File} to be checked
 	 * @param cmpFileName
 	 *            the file-name to check for
 	 * @return <code>true</code> if the file-name is equal, otherwise
 	 *         <code>false</code>
 	 */
 	public static boolean isFileEqual(final File file, final String cmpFileName) {
 
 		if (file == null) {
 			return false;
 		} else if (cmpFileName == null) {
 			return false;
 		}
 
 		final String fileName = file.getName();
 		if (cmpFileName.equalsIgnoreCase(fileName)) {
 			final String cFilePath = getCanonicalPath(file.getParent());
 			final String cFile = getCanonicalPath(file);
 			final String cConfigFile = getCanonicalPath(cFilePath + cmpFileName);
 
 			return cConfigFile.equals(cFile);
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Returns all files of the specified directory and all files in
 	 * sub-folders, which are accepted by the passed filter
 	 * 
 	 * @param dir
 	 *            the directory to delete
 	 * @param fileList
 	 *            the list of files the new files should be appended to, if
 	 *            <code>null</code> a new list will be created
 	 * @param filter
 	 *            a {@link FileFilter} which can be used to filter the returned
 	 *            file list, can be <code>null</code> if all files should be
 	 *            returned
 	 * @return the {@link List} of all files within the directory or a
 	 *         subdirectory
 	 */
 	public static List<File> getFilelist(final File dir, List<File> fileList,
 			final FileFilter filter) {
 		if (fileList == null) {
 			fileList = new ArrayList<File>();
 		}
 
 		if (!dir.exists()) {
 			return fileList;
 		}
 
 		// check sub-directories or add the file
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				final File file = new File(dir, children[i]);
 				fileList = getFilelist(file, fileList, filter);
 			}
 		} else {
 
 			// check the file against the filter
 			if (filter == null || filter.accept(dir)) {
 				fileList.add(dir);
 			}
 		}
 
 		return fileList;
 	}
 
 	/**
 	 * Gets the {@link File} instances of one directory, without any
 	 * sub-directories
 	 * 
 	 * @param dir
 	 *            the directory to get the {@link File} instances from
 	 * @return the {@link List} of {@link File} instances in the passed
 	 *         directory
 	 */
 	public static List<File> getCurrentFilelist(final File dir) {
 		final List<File> fileList = new ArrayList<File>();
 
 		if (!dir.exists())
 			return fileList;
 
 		// check sub- diretories or add the file
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				final File file = new File(dir, children[i]);
 
 				if (file.isFile())
 					fileList.add(file);
 			}
 		} else {
 			fileList.add(dir);
 		}
 
 		return fileList;
 	}
 
 	/**
 	 * Gets all the direct sub-directories of a directory
 	 * 
 	 * @param dir
 	 *            the directory to get the sub-directories for
 	 * @return the {@link List} of {@link File} instances in the passed
 	 *         directory
 	 */
 	public static List<File> getCurrentSubDirectories(final File dir) {
 		final List<File> dirList = new ArrayList<File>();
 
 		if (!dir.exists()) {
 			return dirList;
 		}
 
 		// check sub- diretories or add the file
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				final File file = new File(dir, children[i]);
 
 				if (file.isDirectory())
 					dirList.add(file);
 			}
 		}
 
 		return dirList;
 	}
 
 	/**
 	 * Gets a list of all the content (sub-directories, files and all content of
 	 * the sub-directories) of a specific directory
 	 * 
 	 * @param dir
 	 *            the directory to determine the content from
 	 * @return the list of the content
 	 */
 	public static List<File> getDirectoryContent(final File dir) {
 		return getDirectoryContent(dir, null);
 	}
 
 	private static List<File> getDirectoryContent(final File dir,
 			List<File> contentList) {
 
 		// don't add the start directory
 		if (contentList == null) {
 			contentList = new ArrayList<File>();
 		} else {
 			contentList.add(dir);
 		}
 
 		if (!dir.exists()) {
 			return contentList;
 		}
 
 		// check sub-directories or add the file
 		if (dir.isDirectory()) {
 
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				final File file = new File(dir, children[i]);
 
 				contentList = getDirectoryContent(file, contentList);
 			}
 		}
 
 		return contentList;
 	}
 
 	/**
 	 * This function checks if the specified file exists and if it can be
 	 * accessed
 	 * 
 	 * @param fileNameAndPath
 	 *            the absolute path including the filename to the file
 	 * @param checkWrite
 	 *            <code>true</code> if write access should be checked also,
 	 *            otherwise <code>false</code>
 	 * @return the canonical file (see {@link File#getCanonicalPath()}, if the
 	 *         file cannot be resolved or cannot be accessed <code>null</code>
 	 *         is returned
 	 */
 	public static String checkFile(final String fileNameAndPath,
 			boolean checkWrite) {
 		if (fileNameAndPath == null)
 			return null;
 
 		return checkFile(new File(fileNameAndPath), checkWrite);
 	}
 
 	/**
 	 * This function checks if the specified file exists and if it can be
 	 * accessed
 	 * 
 	 * @param file
 	 *            the file to be checked
 	 * @param checkWrite
 	 *            <code>true</code> if write access should be checked also,
 	 *            otherwise <code>false</code>
 	 * @return the canonical file (see {@link File#getCanonicalPath()}, if the
 	 *         file cannot be resolved or cannot be accessed <code>null</code>
 	 *         is returned
 	 */
 	public static String checkFile(final File file, boolean checkWrite) {
 		if (file == null)
 			return null;
 
 		if (!file.exists())
 			return null;
 		else if (!file.isFile())
 			return null;
 		else if (!file.canRead())
 			return null;
 		else if (checkWrite && !file.canWrite())
 			return null;
 
 		return getCanonicalPath(file);
 	}
 
 	/**
 	 * Converts a path into its canonical path version
 	 * {@link File#getCanonicalPath()}
 	 * 
 	 * @param pathName
 	 *            the path name to convert
 	 * @return the converted path with ending {@link File#separatorChar},
 	 *         <code>null</code> if the path name could not be converted or if
 	 *         it was <code>null</code>
 	 */
 	public static String getCanonicalPath(final String pathName) {
 		if (pathName == null)
 			return null;
 
 		return getCanonicalPath(new File(pathName));
 	}
 
 	/**
 	 * Converts a path into its canonical path version
 	 * {@link File#getCanonicalPath()}
 	 * 
 	 * @param path
 	 *            the file to be converted
 	 * @return the converted path with ending {@link File#separatorChar},
 	 *         <code>null</code> if the path name could not be converted or if
 	 *         it was <code>null</code>
 	 */
 	public static String getCanonicalPath(final File path) {
 		if (path == null)
 			return null;
 
 		// try to get the canonical path
 		try {
 			return path.getCanonicalPath()
 					+ (path.isFile() ? "" : File.separatorChar);
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * This function is used to resolve an absolute or relative path (latter is
 	 * resolved if passed). If the passed path is absolute nothing is changed
 	 * and the {@link URI} of the passed path is returned, if the passed file is
 	 * relative it is resolved against the passed root directory (i.e.
 	 * <code>rootDir</code>).
 	 * 
 	 * <b>This method should only be used, if a directory is involved (i.e. path
 	 * is a directory) or if the file that should be resolved to it's
 	 * root-directory exists</b>
 	 * 
 	 * @param path
 	 *            the relative or absolute path
 	 * @param rootDir
 	 *            the root directory used to resolve the <code>path</code> if it
 	 *            is relative
 	 * @return the absolute path of the passed <code>path</code>
 	 */
 	public static URI getAbsoluteFileURI(final String path, String rootDir) {
 
 		// lets check the file
 		File file = new File(path);
 
 		// get the absolute URI
 		URI uri = file.toURI();
 		if (!file.isAbsolute()) {
 			if (!rootDir.endsWith("" + File.separatorChar)
 					&& !path.startsWith("" + File.separatorChar)) {
 				rootDir += File.separatorChar;
 			}
 			final File rootFile = new File(rootDir + path);
 			uri = rootFile.toURI();
 		} else {
 			uri = file.toURI();
 		}
 
 		return uri;
 	}
 
 	/**
 	 * Copies all the files of a specific location to a destination directory,
 	 * whereby the specified files are truncated by a specific path and placed
 	 * into same folder structure on destination. <br/>
 	 * <br/>
 	 * For example:<br/>
 	 * <ul>
 	 * <li>
 	 * <code>files</code>:
 	 * <ul>
 	 * <li>C:\myfolder\sub\A.FILE</li>
 	 * <li>C:\myfolder\B.FILE</li>
 	 * </ul>
 	 * </li>
 	 * <li><code>destDir</code> (must be a directory):
 	 * <ul>
 	 * <li>C:\tmp\</li>
 	 * </ul>
 	 * </li>
 	 * <li><code>truncater</code> (must be a directory):
 	 * <ul>
 	 * <li>C:\myfolder\</li>
 	 * </ul>
 	 * </li>
 	 * </ul>
 	 * Will create the following structure in <code>C:\tmp\</code>:
 	 * <ul>
 	 * <li>C:\tmp\sub\A.FILE</li>
 	 * <li>C:\tmp\B.FILE</li>
 	 * </ul>
 	 * 
 	 * @param files
 	 *            the files to copy
 	 * @param destDir
 	 *            the destination directory
 	 * @param truncater
 	 *            the path the specified files should be truncated by
 	 * @throws IOException
 	 *             if a file cannot be read or written
 	 */
 	public static void copyFiles(final Collection<File> files,
 			final File destDir, final File truncater) throws IOException {
 
 		if (truncater == null || !truncater.isDirectory()) {
 			throw new IllegalArgumentException("The passed truncater '"
 					+ truncater + "' is null or not a directory.");
 		}
 
 		final String truncaterPath = getCanonicalPath(truncater);
 		final String regEx = "^" + Pattern.quote(truncaterPath);
 
 		for (final File file : files) {
 
 			if (!file.isFile()) {
 				continue;
 			}
 
 			// truncate the part
 			final String filePath = getCanonicalPath(file.getParent());
 			final File destFolder = new File(destDir, filePath.replaceAll(
 					regEx, ""));
 			checkDirectory(getCanonicalPath(destFolder), true);
 
 			// determine the destination
 			final File destFile = new File(destFolder, file.getName());
 
 			// copy the file
 			copyFile(file, destFile);
 		}
 	}
 
 	/**
 	 * Copies all the specified files into the destination directory
 	 * 
 	 * @param files
 	 *            the files to be copied
 	 * @param destDir
 	 *            the destination directory
 	 * @throws IOException
 	 *             if a file cannot be read or written
 	 */
 	public static void copyFiles(final Collection<File> files,
 			final File destDir) throws IOException {
 		for (final File file : files) {
 
 			// determine the destination
 			final File destFile = new File(destDir, file.getName());
 
 			// copy the file
 			copyFile(file, destFile);
 		}
 	}
 
 	/**
 	 * Copies the <code>sourceDir</code> to the <code>destDir</code>
 	 * 
 	 * @param sourceDir
 	 *            the source directory to be copied
 	 * @param destDir
 	 *            the destination directory to copy to
 	 * @throws IOException
 	 *             if an error occurs
 	 */
 	public static void copyDirectory(final File sourceDir, final File destDir)
 			throws IOException {
 
 		if (sourceDir.isDirectory()) {
 
 			// if directory not exists, create it
 			if (!destDir.exists()) {
 				destDir.mkdir();
 			}
 
 			// list all the directory contents
 			final String files[] = sourceDir.list();
 
 			for (final String file : files) {
 
 				// construct the src and dest file structure
 				final File srcFile = new File(sourceDir, file);
 				final File destFile = new File(destDir, file);
 
 				// recursive copy
 				copyDirectory(srcFile, destFile);
 			}
 
 		} else {
 			copyFile(sourceDir, destDir);
 		}
 	}
 
 	/**
 	 * Copies the <code>sourceFile</code> to the <code>destFile</code>
 	 * 
 	 * @param sourceFile
 	 *            the file to be copied
 	 * @param destFile
 	 *            the destination file to copy to
 	 * @throws IOException
 	 *             if an error occurs
 	 */
 	public static void copyFile(final File sourceFile, final File destFile)
 			throws IOException {
 
 		// use bytes stream to support all file types
 		final InputStream in = new FileInputStream(sourceFile);
 
 		// copy the stream
 		copyStreamToFile(in, destFile);
 	}
 
 	/**
 	 * Creates a file based on a <code>InputStream</code>. The stream will be
 	 * closed by this method and has to be recreated if used any further.
 	 * 
 	 * @param sourceStream
 	 *            the input stream which should be copied
 	 * @param destFile
 	 *            the output file
 	 * @throws IOException
 	 *             if the file cannot be accessed
 	 */
 	public static void copyStreamToFile(final InputStream sourceStream,
 			final File destFile) throws IOException {
 
 		// use bytes stream to support all file types
 		final OutputStream out = new FileOutputStream(destFile);
 
 		// copy the file content in bytes
 		final byte[] buffer = new byte[1024];
 		int length;
 		while ((length = sourceStream.read(buffer)) > 0) {
 			out.write(buffer, 0, length);
 		}
 
 		// write the information
 		Streams.closeIO(sourceStream);
 		Streams.closeIO(out);
 	}
 
 	/**
 	 * Zips a specific directory and all it sub-directories
 	 * 
 	 * @param sourceDir
 	 *            the source directory, which should be zipped
 	 * @param zipFile
 	 *            the zip-file to be created
 	 * @throws IOException
 	 *             if the directory cannot be accessed or the zip-file cannot be
 	 *             created
 	 */
 	public static void zipDirectory(final File sourceDir, final File zipFile)
 			throws IOException {
 
 		// get all the files to be zipped
 		final List<File> files = getFilelist(sourceDir, null);
 
 		// create a buffer for the files
 		final byte[] readBuffer = new byte[1024];
 		int bytesIn = 0;
 
 		// open the ZipStream
 		final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
 				zipFile));
 		for (final File file : files) {
 
 			// create a stream for the file
 			final FileInputStream fis = new FileInputStream(file);
 
 			// create the entry
 			final String fullPath = getCanonicalPath(sourceDir.getPath());
 			final String entry = file.getPath().replaceAll(
 					"^" + Pattern.quote(fullPath), "");
 
 			// create a new zip entry
 			final ZipEntry anEntry = new ZipEntry(entry);
 
 			// place the zip entry in the ZipOutputStream object
 			zos.putNextEntry(anEntry);
 
 			// now write the content of the file to the ZipOutputStream
 			while ((bytesIn = fis.read(readBuffer)) != -1) {
 				zos.write(readBuffer, 0, bytesIn);
 			}
 
 			// close the Stream
 			Streams.closeIO(fis);
 		}
 
 		// finalize the zip archive
 		zos.close();
 	}
 
 	/**
 	 * Writes a string to a <code>File</code>
 	 * 
 	 * @param file
 	 *            the <code>File</code> to write the <code>content</code> to
 	 * @param content
 	 *            the content to write to the <code>file</code>
 	 * @param encoding
 	 *            the encoding used by the file to write to
 	 * @throws IOException
 	 *             if the content could not be written to the file
 	 */
 	public static void writeToFile(final File file, final String content,
 			final String encoding) throws IOException {
 		final String fileName = getCanonicalPath(file);
 		writeToFile(fileName, content, encoding);
 	}
 
 	/**
 	 * Writes a string to a <code>File</code>
 	 * 
 	 * @param file
 	 *            the file to write the <code>content</code> to
 	 * @param content
 	 *            the content to write to the <code>file</code>
 	 * @param encoding
 	 *            the encoding used by the file to write to
 	 * @throws IOException
 	 *             if the content could not be written to the file
 	 */
 	public static void writeToFile(final String file, final String content,
 			final String encoding) throws IOException {
 		final FileOutputStream fos = new FileOutputStream(file);
 
 		final OutputStreamWriter ow;
 		if (Charset.isSupported(encoding)) {
 			ow = new OutputStreamWriter(fos, encoding);
 		} else {
 			System.err
 					.println("The specified encoding '"
 							+ encoding
 							+ "' is not supported, the default encoding will be used instead.");
 
 			ow = new OutputStreamWriter(fos);
 		}
 		final BufferedWriter out = new BufferedWriter(ow);
 		out.write(content);
 		Streams.closeIO(out);
 		Streams.closeIO(ow);
 		Streams.closeIO(fos);
 	}
 
 	/**
 	 * Reads a <code>file</code> as string
 	 * 
 	 * @param file
 	 *            the file (complete path) to open
 	 * @return the content of the file as string
 	 * 
 	 * @throws FileNotFoundException
 	 *             if the specified file could not be found
 	 * @throws IOException
 	 *             if the file could not be read
 	 */
 	public static String readFromFile(final String file)
 			throws FileNotFoundException, IOException {
 		return readFromFile(new File(file));
 	}
 
 	/**
 	 * Reads a {@link File} as string
 	 * 
 	 * @param file
 	 *            the file (complete path) to open
 	 * @return the content of the file as string
 	 * 
 	 * @throws FileNotFoundException
 	 *             if the specified file could not be found
 	 * @throws IOException
 	 *             if the file could not be read
 	 */
 	public static String readFromFile(final File file)
 			throws FileNotFoundException, IOException {
 
 		// get the stream to the file and read it
 		final FileInputStream stream = new FileInputStream(file);
 		final String data = Streams.readFromStream(stream);
 
 		// close the stream
 		Streams.closeIO(stream);
 
 		return data;
 	}
 
 	/**
 	 * Removes the extension (if there is one) from the specified
 	 * <code>File</code>. It is not checked if the passed <code>file</code>
 	 * really refers to a <code>File</code> or a <code>Directory</code>.
 	 * 
 	 * @param file
 	 *            the <code>File</code> to trim the extension from
 	 * @return the filename without any extension (i.e. anything behind the last
 	 *         .), <code>null</code> if <code>file</code> was <code>null</code>
 	 */
 	public static String removeExtension(final File file) {
 		if (file == null) {
 			return null;
 		} else {
 			return removeExtension(file.getName());
 		}
 	}
 
 	/**
 	 * Removes the extension (if there is one) from the specified
 	 * <code>fileName</code>. If the <code>fileName</code> ends with a
 	 * <code>separatorChar</code> the file won't be modified (i.e. just the
 	 * <code>separatorChar</code> is removed).
 	 * 
 	 * @param fileName
 	 *            the name of the file to trim the extension from
 	 * @return the filename without any extension (i.e. anything behind the last
 	 *         .), <code>null</code> if <code>fileName</code> was
 	 *         <code>null</code>
 	 * 
 	 * @see File#separatorChar
 	 */
 	public static String removeExtension(final String fileName) {
 		final char fs = File.separatorChar;
 		String postFileName = Files.getCanonicalPath(fileName);
 
 		// check null and the end of the fileName
 		if (postFileName == null) {
 			return null;
 		} else if (postFileName.endsWith("" + fs)) {
 			postFileName = postFileName.substring(0, postFileName.length() - 1);
 		}
 
 		// make sure that we have a filename and not something else, like a
 		// full path
 		if (postFileName.matches("^.*[\\Q" + fs + "\\E].+$")) {
 			postFileName = postFileName
 					.substring(postFileName.lastIndexOf(fs) + 1);
 		}
 
 		// check if we still have an ending FileSeparator -> Drive
 		if (postFileName.lastIndexOf(fs) == postFileName.length() - 1) {
 			return null;
 		}
 
 		// remove the extension
 		final int pos = postFileName.lastIndexOf('.');
 
 		if (pos == -1) {
 			return postFileName;
 		} else {
 			return postFileName.substring(0, pos);
 		}
 	}
 
 	/**
 	 * Reads a property <code>File</code>. Make sure that the properties do not
 	 * contain any UTF-8 characters, otherwise have a look at the see section.
 	 * 
 	 * @param file
 	 *            the property <code>File</code> to be read
 	 * @return the <code>Properties</code> read from the <code>File</code>
 	 * @throws IOException
 	 *             if the <code>File</code> could not be read
 	 * 
 	 * @see UnicodeReader
 	 */
 	public static Properties readProperties(final File file) throws IOException {
 		final FileInputStream fis = new FileInputStream(file);
 		return Streams.readPropertiesFromStream(fis);
 	}
 
 	/**
 	 * Copies a <code>Resource</code> to a <code>File</code>.
 	 * 
 	 * @param resource
 	 *            the path of the <code>Resource</code> to be copied
 	 * @param destFile
 	 *            the destination
 	 * @return <code>true</code> if the <code>Resource</code> was copied,
 	 *         otherwise <code>false</code>
 	 * @throws IOException
 	 */
 	public static boolean copyResourceToFile(final String resource,
 			final String destFile) throws IOException {
 
 		// check the report to be tested
 		final File file = new File(destFile);
 		return copyResourceToFile(resource, file);
 	}
 
 	/**
 	 * Copies a <code>Resource</code> to a <code>File</code>.
 	 * 
 	 * @param resource
 	 *            the path of the <code>Resource</code> to be copied
 	 * @param destFile
 	 *            the destination as <code>File</code>
 	 * @return <code>true</code> if the <code>Resource</code> was copied,
 	 *         otherwise <code>false</code>
 	 * @throws IOException
 	 */
 	public static boolean copyResourceToFile(final String resource,
 			final File destFile) throws IOException {
 		final ResourceInfo resFileInfo = new ResourceInfo(resource, true);
 		return copyResourceToFile(resFileInfo, destFile);
 	}
 
 	/**
 	 * Copies a <code>Resource</code> to a <code>File</code>.
 	 * 
 	 * @param resource
 	 *            the <code>ResourceInfo</code> of the <code>Resource</code> to
 	 *            be copied
 	 * @param destFile
 	 *            the destination <code>File</code>
 	 * @return <code>true</code> if the <code>Resource</code> was copied,
 	 *         otherwise <code>false</code>
 	 * @throws IOException
 	 * 
 	 * @see ResourceInfo
 	 */
 	public static boolean copyResourceToFile(final ResourceInfo resource,
 			final File destFile) throws IOException {
 
 		// nothing to copy
 		if (!resource.exists()) {
 			return false;
 		} else {
 
 			// get the stream and copy it
 			final InputStream stream = Resource.getResourceAsStream(resource);
 			Files.copyStreamToFile(stream, destFile);
 			Streams.closeIO(stream);
 
 			return true;
 		}
 	}
 
 	/**
 	 * Determines the extension of the specified <code>File</code>
 	 * 
 	 * @param file
 	 *            the <code>File</code> to get the extension from
 	 * @return the extension of the passed <code>File</code>, or the empty
 	 *         string if no extension was specified, will never return
 	 *         <code>null</code>
 	 */
 	public static String getExtension(final File file) {
 		if (file == null) {
 			return "";
 		} else {
 			return getExtension(file.getName());
 		}
 	}
 
 	/**
 	 * Determines the extension of the name of the file
 	 * 
 	 * @param fileName
 	 *            the name of a file to get the extension from
 	 * @return the extension of the passed <code>File</code>, or the empty
 	 *         string if no extension was specified, will never return
 	 *         <code>null</code>
 	 */
 	public static String getExtension(final String fileName) {
 
 		if (fileName == null) {
 			return "";
 		} else {
 			final int pos = fileName.lastIndexOf('.');
 			if (pos == -1) {
 				return "";
 			} else {
 				return fileName.substring(pos + 1);
 			}
 		}
 	}
 
 	/**
 	 * Determines the mime-type of the passed <code>File</code>, based on the
 	 * extension of the file and not the content.
 	 * 
 	 * @param file
 	 *            the <code>File</code> to determine the mime-type of
 	 * @return the mime-type of the specified file, or the default mime-type if
 	 *         not determinable
 	 * 
 	 * @see Files#DEFAULT_MIMETYPE
 	 */
 	public static String getMimeType(final File file) {
 		if (file == null) {
 			return DEFAULT_MIMETYPE;
 		} else {
 			return getMimeType(file.getName());
 		}
 	}
 
 	/**
 	 * Determines the mime-type of the passed file-name, based on the extension
 	 * of the file and not the content.
 	 * 
 	 * @param fileName
 	 *            the file-name to determine the mime-type for
 	 * @return the mime-type of the specified file-name, or the default
 	 *         mime-type if not determinable
 	 * 
 	 * @see Files#DEFAULT_MIMETYPE
 	 */
 	public static String getMimeType(final String fileName) {
 
 		if (fileName == null || fileName.equals("")) {
 			return DEFAULT_MIMETYPE;
 		} else {
 
 			// check the extension
 			final String ext = getExtension(fileName);
 			final String defMimeType = MIMETYPES.get(ext);
 
 			if (defMimeType == null) {
 				final FileNameMap fileNameMap = URLConnection.getFileNameMap();
 				final String mimeType = fileNameMap.getContentTypeFor(fileName);
 
 				return "".equals(mimeType) || mimeType == null ? DEFAULT_MIMETYPE
 						: mimeType;
 			} else {
 				return defMimeType;
 			}
 		}
 	}
 
 	/**
 	 * Guesses the encoding of the passed file. If not guessable the default
 	 * encoding will be returned, which can be <code>null</code>. If
 	 * <code>null</code> is passed the system's default encoding (
 	 * <code>file.encoding</code>) will be used.
 	 * 
 	 * @param file
 	 *            the file to guess the encoding for
 	 * @param defaultEncoding
 	 *            the default encoding used when not guessable, can be
 	 *            <code>null</code>
 	 * 
 	 * @return the guessed encoding
 	 * 
 	 * @throws IOException
 	 *             if the file cannot be accessed
 	 * 
 	 * @see System#getProperties()
 	 */
 	public static String guessEncoding(final String file,
 			final String defaultEncoding) throws IOException {
 		return guessEncoding(new File(file), defaultEncoding);
 	}
 
 	/**
 	 * Guesses the encoding of the passed file. If not guessable the default
 	 * encoding will be returned, which can be <code>null</code>. If
 	 * <code>null</code> is passed the system's default encoding (
 	 * <code>file.encoding</code>) will be used.
 	 * 
 	 * @param file
 	 *            the file to guess the encoding for
 	 * @param defaultEncoding
 	 *            the default encoding used when not guessable, can be
 	 *            <code>null</code>
 	 * 
 	 * @return the guessed encoding
 	 * 
 	 * @throws IOException
 	 *             if the file cannot be accessed
 	 * 
 	 * @see System#getProperties()
 	 */
 	public static String guessEncoding(final File file,
 			final String defaultEncoding) throws IOException {
 		final InputStream inStream = new FileInputStream(file);
 		return Streams.guessEncoding(inStream, defaultEncoding);
 	}
 
 	/**
 	 * Checks if the {@code file} is somewhere in the {@code directory}.
 	 * <i>Somewhere</i> means that it might be in a sub-folder.
 	 * 
 	 * @param file
 	 *            the file to be checked, cannot be {@code null}
 	 * @param directory
 	 *            the directory should be in
 	 * 
 	 * @return {@code true} if the {@code file} is directly or in a
 	 *         sub-directory of the specified {@code directory}, otherwise
 	 *         {@code false}
 	 */
 	public static boolean isInDirectory(final String file,
 			final String directory) {
 		return isInDirectory(new File(file), new File(directory));
 	}
 
 	/**
 	 * Checks if the {@code file} is somewhere in the {@code directory}.
 	 * <i>Somewhere</i> means that it might be in a sub-folder.
 	 * 
 	 * @param file
 	 *            the file to be checked, cannot be {@code null}
 	 * @param directory
 	 *            the directory should be in
 	 * 
 	 * @return {@code true} if the {@code file} is directly or in a
 	 *         sub-directory of the specified {@code directory}, otherwise
 	 *         {@code false}
 	 */
 	public static boolean isInDirectory(final File file, final String directory) {
 		return isInDirectory(file, new File(directory));
 	}
 
 	/**
 	 * Checks if the {@code file} is somewhere in the {@code directory}.
 	 * <i>Somewhere</i> means that it might be in a sub-folder.
 	 * 
 	 * @param file
 	 *            the file to be checked, cannot be {@code null}
 	 * @param directory
 	 *            the directory should be in
 	 * 
 	 * @return {@code true} if the {@code file} is directly or in a
 	 *         sub-directory of the specified {@code directory}, otherwise
 	 *         {@code false}
 	 */
 	public static boolean isInDirectory(final String file, final File directory) {
 		return isInDirectory(new File(file), directory);
 	}
 
 	/**
 	 * Checks if the {@code file} is somewhere in the {@code directory}.
 	 * <i>Somewhere</i> means that it might be in a sub-folder.
 	 * 
 	 * @param file
 	 *            the file (can be a file or a directory) to be checked, cannot
 	 *            be {@code null}
 	 * @param directory
 	 *            the directory should be in
 	 * 
 	 * @return {@code true} if the {@code file} is directly or in a
 	 *         sub-directory of the specified {@code directory}, otherwise
 	 *         {@code false}
 	 */
 	public static boolean isInDirectory(final File file, final File directory) {
 
 		// the file and directory should exist and should be of correct type
 		if (!file.exists() || !directory.exists()) {
 			return false;
 		} else if (!directory.isDirectory()) {
 			return false;
 		}
 
 		// next get the canonical representations
 		final String canFile = getCanonicalPath(file);
 		final String canDir = getCanonicalPath(directory);
 
 		// check the prefix
 		return canFile.startsWith(canDir);
 	}
 }
