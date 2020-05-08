 package com.sis.io;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.sis.io.adapter.AdapterInterface;
 import com.sis.io.adapter.Hdfs;
 import com.sis.io.adapter.Local;
 import com.sis.io.adapter.S3;
 import com.sis.io.adapter.SimpleHttp;
 import com.sis.system.Profiler;
 import com.sis.system.XmlConfig;
 
 /**
  * SmartCDN is a wrapper for our hadoop dfs cluster
  * 
  * TODO refactor.
  */
 public class SmartCDN {
 	private static final Logger logger = Logger.getLogger(SmartCDN.class);
 	
 	public static URI getCategoryRootURI(String category) {
 		List<String> list = XmlConfig.getSingleton().getValueList("cdn/bucket/" + category);
 		
 		try {
 //			return new URI(list.get((int) Math.round(Math.random() * list.size()) - 1));
 			
 			String randomString = list.get(0); // TODO
 			
 			return new URI(randomString);
 		} catch (URISyntaxException e) {
 			logger.error("malformed URI.", e);
 			
 			return null;
 		}
 	}
 	
 	private static final HashMap<String, AdapterInterface> adapters = new HashMap<String, AdapterInterface>(); 
 	
 	static {
 		adapters.put("hdfs", new Hdfs());
 		adapters.put("file", new Local());
 		adapters.put("http", new SimpleHttp());
 		adapters.put("s3", new S3());
 	}
 	
 	private static AdapterInterface getAdapterInterfaceByScheme(String scheme) {
 		return adapters.get(scheme);
 	}
 	
 	public static boolean copyFile(URI srcuri, URI dsturi) {
 		logger.debug("copyFile(" + srcuri.toString() + ", " + dsturi.toString() + ").");
 		
 		Profiler profiler = new Profiler("CDN/copyFile");
 		profiler.start();
 		
 		try {
 			if (srcuri.getHost() == null || dsturi.getHost() == null ||
 				!srcuri.getHost().equalsIgnoreCase(dsturi.getHost()) ||
 				!srcuri.getScheme().equalsIgnoreCase(dsturi.getScheme())) {
 				
 				AdapterInterface srcadapter = getAdapterInterfaceByScheme(srcuri.getScheme());
 				AdapterInterface dstadapter = getAdapterInterfaceByScheme(dsturi.getScheme());
 				InputStream srcstream = srcadapter.openFile(srcuri);
 				OutputStream dststream = dstadapter.createFile(dsturi); 
 
 				while (true) {
 					byte b[] = new byte[16384];
 					
 					int length = srcstream.read(b);
 					
 					if (length <= 0) {
 						break;
 					}
 					
 					dststream.write(b, 0, length);
 				}
 	
 				srcstream.close();
 				dststream.close();
 			} else {
 				AdapterInterface srcadapter = getAdapterInterfaceByScheme(srcuri.getScheme());
 				
 				srcadapter.copyFile(srcuri, dsturi);
 			}
 		} catch(IOException e) {
 			logger.error("copy file from " + srcuri.toString() + " to " + dsturi.toString() + " failed!", e);
 			return false;
 		} finally {
 			profiler.stop();
 		}
 		
 		return true;
 	}
 	
 	public static boolean copyFile(String src, String dst) {
 		try {
 			URI srcuri = new URI(src);
 			URI dsturi = new URI(dst);
 			
 			return copyFile(srcuri, dsturi);
 		} catch (URISyntaxException e) {
 			return false;
 		}
 	}
 	
 	public static boolean moveFile(URI srcuri, URI dsturi) {
 		logger.debug("moveFile(" + srcuri.toString() + ", " + dsturi.toString() + ").");
 		
 		Profiler profiler = new Profiler("CDN/moveFile");
 		profiler.start();
 		
 		try {
			if ((
					srcuri.getHost() != null && 
					!srcuri.getHost().equalsIgnoreCase(dsturi.getHost())
				) || !srcuri.getScheme().equalsIgnoreCase(dsturi.getScheme())) {
 				if (copyFile(srcuri, dsturi)) {
 					deleteFile(srcuri);
 				}
 			} else {
 				AdapterInterface srcadapter = getAdapterInterfaceByScheme(srcuri.getScheme());
 				
 				srcadapter.moveFile(srcuri, dsturi);
 			}
 		} catch (IOException e) {
 			logger.error("moveFile(" + srcuri.toString() + ", " + dsturi.toString() + ") failed!", e);
 		} finally {
 			profiler.stop();
 		}
 		
 		return false;
 	}
 	
 	public static boolean moveFile(String src, String dst) {
 		try {
 			URI srcuri = new URI(src);
 			URI dsturi = new URI(dst);
 			
 			return moveFile(srcuri, dsturi);
 		} catch (URISyntaxException e) {
 			return false;
 		}
 	}
 	
 	public static OutputStream createFile(URI uri) throws IOException {
 		logger.debug("createFile(" + uri.toString() + ").");
 		
 		return getAdapterInterfaceByScheme(uri.getScheme()).createFile(uri);
 	}
 	
 	public static OutputStream appendFile(URI uri) throws IOException {
 		logger.debug("appendFile(" + uri.toString() + ").");
 		
 		return getAdapterInterfaceByScheme(uri.getScheme()).appendFile(uri);
 	}
 
 	public static InputStream getFile(URI uri) throws IOException {
 		logger.debug("getFile(" + uri.toString() + ").");
 		
 		return getAdapterInterfaceByScheme(uri.getScheme()).openFile(uri);
 	}
 	
 	public static void deleteFile(URI uri) throws IOException {
 		logger.debug("deleteFile(" + uri.toString() + ").");
 		
 		getAdapterInterfaceByScheme(uri.getScheme()).deleteFile(uri);
 	}
 	
 	public static boolean fileExists(URI uri) throws IOException {
 		logger.debug("fileExists(" + uri.toString() + ").");
 		
 		return getAdapterInterfaceByScheme(uri.getScheme()).exists(uri);
 	}
 	
 	/** PLEASE DO NOT USE THIS METHOD ON LARGE FILES! ONLY SMALL TEXT FILES! */
 	public static String getFileContent(URI internalUri) throws IOException {
 		logger.debug("getFileContent(" + internalUri.toString() + ").");
 		
 		InputStream is = getFile(internalUri);
 		InputStreamReader isr = new InputStreamReader(is);
 		BufferedReader br = new BufferedReader(isr);
 
 		StringBuilder sb = new StringBuilder();
 
 		String line = null;
 
 		while ((line = br.readLine()) != null) {
 			sb.append(line);
 			sb.append('\n');
 		}
 
 		br.close();
 		isr.close();
 		is.close();
 
 		return sb.toString();
 	}
 	
 	/** PLEASE DO NOT USE THIS METHOD ON LARGE FILES! ONLY SMALL FILES! */
 	public static String getFileContent(String internalUri) throws IOException, URISyntaxException {
 		return getFileContent(new URI(internalUri));
 	}
 
 	/** PLEASE DO NOT USE THIS METHOD ON LARGE FILES! ONLY SMALL TEXT FILES! utf-8 aware */
 	public static void setFileContent(URI internalUri, String data) throws IOException {
 		logger.debug("setFileContent(" + internalUri.toString() + "), about to write " + data.length() + " characters.");
 
 		OutputStream os = createFile(internalUri);
 
 		os.write(data.getBytes("UTF-8"));
 		os.close();
 	}
 	
 	/** PLEASE DO NOT USE THIS METHOD ON LARGE FILES! ONLY SMALL FILES! */
 	public static void setFileContent(String internalUri, String data) throws IOException, URISyntaxException {
 		setFileContent(new URI(internalUri), data);
 	}
 
 	public static InputStream getFile(String path) throws IOException, URISyntaxException {
 		return getFile(new URI(path));
 	}
 }
