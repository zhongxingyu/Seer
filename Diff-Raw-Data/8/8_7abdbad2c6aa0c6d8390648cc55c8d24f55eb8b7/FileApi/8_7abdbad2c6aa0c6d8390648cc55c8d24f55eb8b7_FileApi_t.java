 package com.zenesis.qx.remote;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.zenesis.qx.remote.annotations.ExplicitProxyOnly;
 import com.zenesis.qx.remote.annotations.Method;
 import com.zenesis.qx.remote.annotations.Property;
 import com.zenesis.qx.remote.annotations.Remote.Array;
 
 /**
  * Provides a filing system API within a fixed root; unlike the AppFile implementation, any collection
  * of file information is not shipped back to the client as Proxied instances because this can build
  * up a large amount of tracked instances on the server that are difficult to free up.  
  * 
  * @author john
  *
  */
 @ExplicitProxyOnly
 public class FileApi implements Proxied {
 	
 	private static final Logger log = Logger.getLogger(FileApi.FileType.class); 
 
 	/*
 	 * Type of file in FileInfo
 	 */
 	public enum FileType {
 		FILE, FOLDER
 	}
 
 	/*
 	 * Holds extended info about a file; this is not a Proxied object because we do not
 	 * want it's instance to be tracked (otherwise it will remain in server memory for
 	 * the duration of the session)
 	 */
 	public static final class FileInfo {
 		public final String name;
 		public final String absolutePath;
 		public final FileType type;
 		public final long size;
 		public final long lastModified;
 		public final boolean exists;
 		
 		protected FileInfo(File file, String rootAbsPath) {
 			this.name = file.getName();
 			this.type = file.isDirectory() ? FileType.FOLDER : FileType.FILE;
 			this.size = file.length();
 			this.lastModified = file.lastModified();
 			this.exists = file.exists();
 			String absPath = file.getAbsolutePath();
 			if (!absPath.startsWith(rootAbsPath))
 				throw new IllegalArgumentException("File is not within root, rootAbsPath=" + rootAbsPath + ", file=" + file.getAbsolutePath());
 			absolutePath = file.getAbsolutePath().substring(rootAbsPath.length() - 1);
 		}
 	}
 
 	/*
 	 * Filter used to select candidate files for listFilenames/listFileInfo 
 	 */
 	protected static final FileFilter LIST_FILES_FILTER = new FileFilter() {
 		@Override
 		public boolean accept(File file) {
 			return (file.isDirectory() || file.isFile()) && !file.isHidden() && file.getName().charAt(0) != '.';
 		}
 	};
 	
 	// Map of mime types vs filename extensions
 	private static HashMap<String, String[]> s_mimeTypes;
 
 	// Root folder
 	private final File rootDir;
 	
 	// Root URL
 	@Property
 	private final String rootUrl;
 	
 	// Absolute path of the root folder, with trailing slash
 	private final String rootAbsPath;
 	
 	// List of active uploads, indexed by ID
 	private HashMap<String, UploadingFile> uploading = new HashMap<String, UploadingFile>();
 	
 	/**
 	 * Constructor
 	 * @param rootDir
 	 * @throws IOException
 	 */
 	public FileApi(File rootDir, String rootUrl) {
 		rootDir.mkdirs();
 		if (!rootDir.exists() || !rootDir.isDirectory())
 			throw new IllegalArgumentException("FileApi must have a root directory, not " + rootDir.getAbsolutePath());
 		this.rootDir = rootDir.getAbsoluteFile();
 		rootAbsPath = rootDir.getAbsolutePath() + File.separatorChar;
 		if (rootUrl != null) {
 			if (rootUrl.length() == 0 || rootUrl.charAt(rootUrl.length() - 1) != '/')
 				rootUrl += '/';
 		}
 		this.rootUrl = rootUrl;
 	}
 	
 	/**
 	 * Returns a File for a given path; the path must be within the root, otherwise null is returned
 	 * @param path
 	 * @return a File, or null if the path was outside of the root
 	 */
 	public File getFile(String path) {
 		if (path.length() > 0 && path.charAt(path.length() - 1) == '/')
 			path = path.substring(0, path.length() - 1);
 		int pos;
 		while ((pos = path.indexOf("//")) > -1)
 			path = path.substring(0, pos) + path.substring(pos + 1);
 		File file = new File(rootDir, path).getAbsoluteFile();
 		if (!isValidFile(file)) {
 			log.warn("Invalid path=" + path + ", rootDir=" + rootAbsPath);
 			return null;
 		}
 		return file;
 	}
 
 	/**
 	 * Checks whether the file is valid (ie within the root folder)
 	 * @param file
 	 * @return
 	 */
 	public boolean isValidFile(File file) {
 		String absPath = file.getAbsolutePath();
 		if (file.isDirectory())
			absPath += File.separatorChar;
 		if (!absPath.startsWith(rootAbsPath))
 			return false;
 		return true;
 	}
 	
 	/**
 	 * Returns a list of FileInfo for the contents of a directory
 	 * @param path
 	 * @return null if the path was not a valid directory
 	 */
 	@Method
 	public FileInfo[] listFileInfos(String path) {
 		File dir = getFile(path);
 		if (dir == null || !dir.isDirectory())
 			return null;
 		ArrayList<FileInfo> result = new ArrayList<FileInfo>();
 		for (File file : dir.listFiles(LIST_FILES_FILTER))
 			result.add(new FileInfo(file, rootAbsPath));
 		return result.toArray(new FileInfo[result.size()]);
 	}
 	
 	/**
 	 * Returns a list of filenames in a directory
 	 * @param path
 	 * @return null if the path was not a valid directory
 	 */
 	@Method
 	public String[] listFilenames(String path) {
 		File dir = getFile(path);
 		if (dir == null || !dir.isDirectory())
 			return null;
 		ArrayList<String> result = new ArrayList<String>();
 		for (File file : dir.listFiles(LIST_FILES_FILTER))
 			result.add(file.getName());
 		return result.toArray(new String[result.size()]);
 	}
 	
 	/**
 	 * Returns FileInfo for a given file/folder
 	 * @param path
 	 * @return null of the path was not valid
 	 */
 	@Method
 	public FileInfo getFileInfo(String path) {
 		File file = getFile(path);
 		if (file == null)
 			return null;
 		return new FileInfo(file, rootAbsPath);
 	}
 	
 	/**
 	 * Returns FileInfo for a given file
 	 * @param file
 	 * @return null if the file is not within the root folder
 	 */
 	public FileInfo getFileInfo(File file) {
 		if (!isValidFile(file))
 			return null;
 		return new FileInfo(file, rootAbsPath);
 	}
 	
 	/**
 	 * Renames the file; typically only works on the same physical filing system and will fail
 	 * if the dest already exists
 	 * @param strSrc
 	 * @param strDest
 	 * @return false if the rename failed
 	 */
 	@Method
 	public boolean renameTo(String strSrc, String strDest) {
 		File src = getFile(strSrc);
 		File dest = getFile(strDest);
 		if (src == null || dest == null)
 			return false;
 		dest.getParentFile().mkdirs();
 		return src.renameTo(dest);
 	}
 	
 	/**
 	 * Moves the file; works across filing systems and tries to complete if at all possible.  If the
 	 * source filing system is readonly, it will be a copy.  Returns false if one or both paths were invalid
 	 * @param strSrc
 	 * @param strDest
 	 * @return true if it succeeded
 	 * @throws IOException
 	 */
 	@Method
 	public boolean moveTo(String strSrc, String strDest) throws IOException {
 		File src = getFile(strSrc);
 		File dest = getFile(strDest);
 		if (src == null || dest == null)
 			return false;
 		moveTo(src, dest);
 		return true;
 	}
 	
 	/**
 	 * Copies the file; works across filing systems and tries to complete if at all possible.  Returns false if 
 	 * one or both paths were invalid
 	 * @param strSrc
 	 * @param strDest
 	 * @return true if it succeeded
 	 * @throws IOException
 	 */
 	@Method
 	public boolean copyTo(String strSrc, String strDest) throws IOException {
 		File src = getFile(strSrc);
 		File dest = getFile(strDest);
 		if (src == null || dest == null)
 			return false;
 		copyTo(src, dest);
 		return true;
 	}
 	
 	/**
 	 * Deletes a file; the path must refer to a file or an empty directory
 	 * @param path
 	 * @return false if the path was invalid or could not be deleted
 	 */
 	@Method
 	public boolean deleteFile(String path) {
 		File file = getFile(path);
 		if (file == null)
 			return false;
 		return file.delete();
 	}
 	
 	/**
 	 * Deletes a file, or if a directory will delete recursively
 	 * @param path
 	 * @return false if the path was invalid or could not be deleted
 	 */
 	@Method
 	public boolean deleteRecursive(String path) {
 		File file = getFile(path);
 		if (file == null)
 			return false;
 		if (file.isFile())
 			return file.delete();
 		deleteRecursiveInternal(file);
 		return !file.exists();
 	}
 	
 	/**
 	 * Creates a directory and any parent folders
 	 * @param path
 	 * @return false if the path was invalid or the directory could not be created (eg a file with the same
 	 * name already exists); true if the directory was created or if it already existed
 	 */
 	@Method
 	public FileInfo createFolder(String path) {
 		File file = getFile(path);
 		if (file != null) {
 			file.mkdirs();
 			if (file.exists() && file.isDirectory())
 				return getFileInfo(file);
 		}
 		return null;
 	}
 	
 	/**
 	 * Tests whether a file exists
 	 * @param path
 	 * @return false if it does not exist or the path was invalid
 	 */
 	@Method
 	public boolean exists(String path) {
 		File file = getFile(path);
 		return file != null && file.exists();
 	}
 	
 	/**
 	 * Detects the type of the file
 	 * @param path
 	 * @return
 	 */
 	@Method
 	public FileType getType(String path) {
 		File file = getFile(path);
 		if (file == null)
 			return null;
 		return file.isDirectory() ? FileType.FOLDER : file.isFile() ? FileType.FILE : null; 
 	}
 
 	/**
 	 * Called when an upload begins
 	 * @param upfile
 	 * @throws IOException
 	 */
 	public void beginUploadingFile(UploadingFile upfile) throws IOException {
 		uploading.put(upfile.getUploadId(), upfile);
 	}
 
 	/**
 	 * Called when an upload completes
 	 * @param upfile
 	 * @param success
 	 * @return
 	 * @throws IOException
 	 */
 	public File endUploadingFile(UploadingFile upfile, boolean success) throws IOException {
 		if (!success) {
 			uploading.remove(upfile.getUploadId());
 			return null;
 		}
 
 		String uploadFolder = null;
 		Object obj = upfile.getParams().get("uploadFolder");
 		if (obj instanceof String) {
 			uploadFolder = (String)obj;
 		} else {
 			uploadFolder = "/";
 		}
 		
 		File dest = getFile(uploadFolder + "/" + upfile.getOriginalName());
 		moveTo(upfile.getFile(), dest);
 		return dest;
 	}
 
 	/**
 	 * Returns the UploadingFile2 instance for a given upload ID (if it's still uploading)
 	 * @param uploadId
 	 * @return
 	 */
 	public UploadingFile getUploadingFile(String uploadId) {
 		UploadingFile upfile = uploading.get(uploadId);
 		return upfile;
 	}
 	
 	/**
 	 * Removes a file from the list of currently uploading files
 	 * @param uploadId
 	 */
 	public void clearUploadedFile(String uploadId) {
 		uploading.remove(uploadId);
 	}
 	
 	/**
 	 * Moves a file or directory if at all possible, copying and deleting the original if necessary.  Supports
 	 * recursively moving folders as well as files
 	 * @param src
 	 * @param dest
 	 * @throws IOException
 	 */
 	protected void moveTo(File src, File dest) throws IOException {
 		if (src.isDirectory()) {
 			if (dest.isFile())
 				throw new IOException("Cannot move " + src.getAbsolutePath() + " to " + dest.getAbsolutePath() + " because dest is a file");
 			dest.mkdirs();
 			if (!dest.exists())
 				throw new IOException("Cannot create folder " + dest.getAbsolutePath());
 			File[] files = src.listFiles();
 			boolean ok = true;
 			for (File file : files) {
 				moveTo(file, new File(dest, file.getName()));
 				if (file.exists())
 					ok = false;
 			}
 			if (ok)
 				src.delete();
 			return;
 		}
 		
 		if (!src.isFile())
 			throw new IOException("Not a file or directory: " + src.getAbsolutePath());
 		
 		if (dest.exists()) {
 			if (dest.isDirectory())
 				dest = new File(dest, src.getName());
 			if (dest.exists())
 				dest.delete();
 		}
 		
 		if (dest.renameTo(dest))
 			return;
 		
 		copyTo(src, dest);
 		src.delete();
 	}
 	
 	/**
 	 * Copies a file or directory if at all possible, copying and deleting the original if necessary.  Supports
 	 * recursively copying folders as well as files
 	 * @param src
 	 * @param dest
 	 * @throws IOException
 	 */
 	protected void copyTo(File src, File dest) throws IOException {
 		if (src.isFile()) {
 			if (dest.isDirectory())
 				throw new IOException("Cannot copy " + src.getAbsolutePath() + " to " + dest.getAbsolutePath() + " because dest is a directory");
 			FileOutputStream os = null;
 			FileInputStream is = null;
 			dest.getParentFile().mkdirs();
 			try {
 				os = new FileOutputStream(dest);
 				is = new FileInputStream(src);
 				byte[] buffer = new byte[1024 * 32];
 				int len;
 				while ((len = is.read(buffer)) > -1)
 					os.write(buffer, 0, len);
 				is.close();
 				is = null;
 				os.close();
 				os = null;
 			}catch(IOException e) {
 				if (is != null)
 					try { is.close(); } catch(IOException e2) {}
 				if (os != null)
 					try { os.close(); } catch(IOException e2) {}
 				throw e;
 			}
 		} else {
 			if (dest.isFile())
 				throw new IOException("Cannot copy " + src.getAbsolutePath() + " to " + dest.getAbsolutePath() + " because dest is a file");
 			dest.mkdirs();
 			File[] files = src.listFiles();
 			if (files != null)
 				for (int i = 0; i < files.length; i++) {
 					File destChild = new File(dest, files[i].getName());
 					copyTo(files[i], destChild);
 				}
 		}
 	}
 	
 	/**
 	 * Does a recursive delete of the directory
 	 * @param dir
 	 */
 	protected void deleteRecursiveInternal(File dir) {
 		File[] files = dir.listFiles();
 		if (files != null)
 			for (File file : files)
 				if (file.isDirectory())
 					deleteRecursiveInternal(file);
 				else
 					file.delete();
 		dir.delete();
 	}
 	
 	/**
 	 * @return the rootDir
 	 */
 	public File getRootDir() {
 		return rootDir;
 	}
 
 	/**
 	 * @return the rootUrl
 	 */
 	public String getRootUrl() {
 		return rootUrl;
 	}
 
 	/**
 	 * Returns the mapping between mime type and file extensions 
 	 * @return
 	 */
 	@Method(cacheResult=true, array=Array.NATIVE)
 	public Map<String, String[]> getMimeTypes() {
 		if (s_mimeTypes == null)
 			return Collections.EMPTY_MAP;
 		return s_mimeTypes;
 	}
 	
 	/**
 	 * Sets the global mime types lookup; the map should contain MIME type (eg "text/plain") as keys and
 	 * an array of extensions as the value, the extensions should include the "."
 	 * @param mimeTypes
 	 */
 	public static void setMimeTypes(HashMap<String, String[]> mimeTypes) {
 		s_mimeTypes = mimeTypes;
 	}
 }
