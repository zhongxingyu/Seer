 package be.uclouvain.sinf1225.gourmet.utils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import be.uclouvain.sinf1225.gourmet.Gourmet;
 import android.graphics.BitmapFactory;
 import android.graphics.Bitmap;
 import android.util.Log;
 
 public class GourmetFiles
 {
 	/**
 	 * Copy image to disk, in png format. 
 	 * @param newFilePath new file path. File type should be .png. Will be converted to system-independant path.
 	 * @param existingFilePath existing image from api. (path is system-independant)
 	 */
 	public static void copyImageToDisk(String newFilePath, String existingFilePath)
 	{
 		List<String> subDirTree = new ArrayList<String>();
 		String fileName = dbPathToSystemIndependantPath(newFilePath, subDirTree);
 		copyImageToDisk(subDirTree, fileName, existingFilePath);
 	}
 	
 	/**
 	 * Copy image to disk, in png format. 
 	 * @param subdirTree system-independant path to the dir of the new file.
 	 * @param fileName new filename. Should be a png.
 	 * @param existingFilePath existing image from api.
 	 */
 	private static void copyImageToDisk(List<String> subdirTree, String fileName, String existingFilePath)
 	{
 		// Create a path where we will place our private file on external
 		// storage.
 		File dir = Gourmet.getAppContext().getFilesDir();
 		String subdir = "";
 		for (String subdirT : subdirTree)
 			subdir += File.separator + subdirT;
 		if (!subdir.equals(""))
 			new File(dir + subdir).mkdirs();
 		File file = new File(dir + subdir, fileName);
 
 		try
 		{
 			OutputStream os = new FileOutputStream(file);
 			BitmapFactory.decodeFile(existingFilePath).compress(Bitmap.CompressFormat.PNG, 100, os);
 			os.close();
 		}
 		catch (IOException e)
 		{
 			Log.w("ExternalStorage", "Error writing " + file, e);
 		}
 	}
 	
 	/**
 	 * Export a file from ressource 
 	 * @param subdirTree system-independant path to the dir of the file.
 	 * @param fileName filename
 	 * @param res ressource of the file
 	 */
 	private static void exportFileFromResToDisk(List<String> subdirTree, String fileName, int res)
 	{
 		// Create a path where we will place our private file on external
 		// storage.
 		File dir = Gourmet.getAppContext().getFilesDir();
 		String subdir = "";
 		for(String subdirT: subdirTree)
 			subdir += File.separator+subdirT;
 		if(!subdir.equals(""))
 			new File(dir+subdir).mkdirs();
 		File file = new File(dir+subdir, fileName);
 
 		try
 		{
 			InputStream is = Gourmet.getAppContext().getResources().openRawResource(res);
 			OutputStream os = new FileOutputStream(file);
 			byte[] data = new byte[is.available()];
 			is.read(data);
 			os.write(data);
 			is.close();
 			os.close();
 		}
 		catch (IOException e)
 		{
 			Log.w("ExternalStorage", "Error writing " + file, e);
 		}
 	}
 
 	/**
 	 * Export a file from ressource
 	 * Convert DB path to android path.
 	 * @param path unix-style path to the file.
 	 * @param res Ressource of the file
 	 */
 	public static void exportFileFromResToDisk(String path, int res)
 	{
 		List<String> subDirTree = new ArrayList<String>();
 		String fileName = dbPathToSystemIndependantPath(path, subDirTree);
 		exportFileFromResToDisk(subDirTree, fileName, res);
 	}
 	
 	/**
 	 * Delete a file from storage
 	 * @param subdirTree system-independant path to the dir of the file.
 	 * @param fileName filename.
 	 */
 	private static void deleteExternalStoragePrivateFile(List<String> subdirTree, String fileName)
 	{
 		String subdir = "";
 		for(String subdirT: subdirTree)
 			subdir += File.separator+subdirT;
 		File file = new File(Gourmet.getAppContext().getFilesDir()+subdir, fileName);
 		if (file != null)
 			file.delete();
 	}
 	
 	/**
 	 * Delete a file from storage
 	 * Convert DB path to android path.
 	 * @param path unix-style path to the file.
 	 */
 	public static void deleteExternalStoragePrivateFile(String path)
 	{
 		List<String> subDirTree = new ArrayList<String>();
 		String fileName = dbPathToSystemIndependantPath(path, subDirTree);
 		deleteExternalStoragePrivateFile(subDirTree, fileName);
 	}
 	
 	private static String dbPathToSystemIndependantPath(String basePath, List<String> futureSubDirTree)
 	{
 		StringTokenizer st = new StringTokenizer(basePath, "/");
 		List<String> subdirTreeList = new ArrayList<String>();
 		while(st.hasMoreElements())
 		{
 			String t = st.nextToken();
 			subdirTreeList.add(t);
 		}
		futureSubDirTree = subdirTreeList.subList(0, subdirTreeList.size()-1);
 		return subdirTreeList.get(subdirTreeList.size()-1);
 	}
 	
 	/**
 	 * Convert db file path to real filepath in SD card
 	 */
 	public static String getRealPath(String path)
 	{
 		StringTokenizer st = new StringTokenizer(path, "/");
 		String rpath = "";
 		while(st.hasMoreElements())
 		{
 			String t = st.nextToken();
 			rpath += File.separator+t;
 		}
 		return Gourmet.getAppContext().getFilesDir()+rpath;
 	}
 }
