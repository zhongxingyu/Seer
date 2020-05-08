 package de.hanneseilers.mensa_sh;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 /**
  * Manager class for file caching
  * @author hannes
  *
  */
 public class CacheManager {
 
 	/**
 	 * Prefix of cached files
 	 */
 	private static String filePrefix = "cache_";
 	
 	/**
 	 * Checks for a cached text file 
 	 * @param ctx
 	 * @param filename
 	 * @return File text or null if file doesn't exsist
 	 */
 	public static String readCachedFile(Context ctx, String filename){
 		return readCachedFile(ctx, filename, true);
 	}
 	
 	/**
 	 * Checks for a cached text file 
 	 * @param ctx
 	 * @param filename
 	 * @param useCacheHoldeTime If true, cache hold time is consired wheter to read cached file or not
 	 * if false, cached file is always read
 	 * @return File text or null if file doesn't exsist
 	 */
 	public static String readCachedFile(Context ctx, String filename, boolean useCacheHoldTime){
 		try{
 			
 			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
			long cacheHoldTime = Long.parseLong(( sharedPref.getString("CACHE_HOLD_TIME", "-1") )) * 60 * 60 * 1000;
 			
 			// try to open file
 			filename = filePrefix + filename;
 			FileInputStream fin = ctx.openFileInput(filename);
 			DataInputStream in = new DataInputStream(fin);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			
 			File f = ctx.getFileStreamPath(filename);
 			Long timeDiff = System.currentTimeMillis() - f.lastModified();
 			
 			// read lines of file
 			String ret = "";
 			String line;
			System.out.println( ">Time diff:" + timeDiff + " cache hold time:" + cacheHoldTime );
 			if( in != null && (timeDiff < cacheHoldTime || !useCacheHoldTime) ){
 				while( (line = br.readLine()) != null ){
 					ret += line;
 				}
 				return ret;
 			}
 			
 		} catch(Exception e){}
 		return null;
 	}
 	
 	/**
 	 * Writes data to a chached text file
 	 * @param ctx
 	 * @param filename
 	 * @param text
 	 */
 	public static void writeChachedFile(Context ctx, String filename, String text){
 		// write data to file
 		FileOutputStream fos;
 		try {
 			filename = filePrefix + filename;
 			fos = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
 			fos.write(text.getBytes());			
 		} catch (FileNotFoundException e) {
 		} catch (IOException e) {}
 	}
 	
 	/**
 	 * Clears all cached files
 	 * @param ctx
 	 */
 	public static void clearAll(Context ctx){
 		// delete files
 		for( File f : getCachedFiles(ctx) ){
 			f.delete();
 		}
 	}
 	
 	/**
 	 * @param ctx
 	 * @return Cache size in kB
 	 */
 	public static int getCacheSize(Context ctx){
 		// count file sizes
 		int cacheSize = 0;
 		
 		for( File f : getCachedFiles(ctx)){
 			cacheSize += (f.length()/1024);
 		}
 		
 		return cacheSize;
 	}
 	 
 	/**
 	 * @param ctx
 	 * @return All cached files
 	 */
 	public static File[] getCachedFiles(Context ctx){
 		 // generate filename filter
 		FilenameFilter filter = new FilenameFilter() {			
 			@Override
 			public boolean accept(File dir, String filename) {
 				return filename.startsWith(filePrefix);
 			}
 		};
 		
 		// return files
 		return ctx.getFilesDir().listFiles(filter);
 	}
 	 
 	
 }
