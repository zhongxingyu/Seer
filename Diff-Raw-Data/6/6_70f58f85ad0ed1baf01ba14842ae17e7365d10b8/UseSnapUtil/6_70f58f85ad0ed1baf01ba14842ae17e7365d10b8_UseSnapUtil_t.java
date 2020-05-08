 package no.webtech.serialize.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import no.webtech.serialize.coreapi.QueryHolder;
 import no.webtech.serialize.rvoapi.Rvo;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class UseSnapUtil {
 	
 	private static Logger logger = LoggerFactory.getLogger(UseSnapUtil.class);
 	
 	public static StringBuffer rvaToStringBuffer(Rvo rf) {
 		StringBuffer sb = new StringBuffer();
 		Set<String> s = rf.getAttributeNames();
 		List<Map<String, String>> l = rf.getRows();
 		for (Iterator<Map<String, String>> iterator = l.iterator(); iterator.hasNext();) {
 			Map<String, String> map = (Map<String, String>) iterator.next();
 			for (Iterator<String> itr = s.iterator(); itr.hasNext();) {
 				String attribute = itr.next();
 				sb.append(attribute).append("=").append(map.get(attribute)).append(";");
 				// System.out.print(attribute + "=" + map.get(attribute) + ";");
 			}
 			sb.append("\n");
 		}
 		return sb;
 	}
 	
 	public static String rvaToString(Rvo rf) {
 		return rvaToStringBuffer(rf).toString();
 	}
 
 	public static Serializable fromFile(QueryHolder ql) {
		String fileName = serializedObjectFileName(ql);		
 		byte[] ba = loadFromFile(fileName);
		logger.debug("File: " + fileName + " Loaded buffer size: " + (ba!=null? ba.length:"<ba is null>"));
 		if (ba == null) return null;
 		return (Serializable) TheObject.createInstance(ba, 0).getObject();		
 	}
 	
 	public static String serializedObjectFileName(QueryHolder ql) {
 		return serializedObjectFileName(ql.queryFingerprint());
 	}
 	
 	private static String serializedObjectFileName(String id) {
 		return System.getProperty("supersnap.dir", "src/test/data/snap_") + id + ".ser";
 	}
 
 	public static void dumpToFile(Serializable rf, QueryHolder qsh){
 		dumpToFile(rf,qsh.queryFingerprint(),"Resultset from select: "+qsh.getQuery());
 	}
 	
 	private static void dumpToFile(Serializable rf, String id, String comment) {
 		StringBuffer sb = SuperSnapUtil.createSerializedFormattedOutput(rf, id);
 		String fn = serializedObjectFileName(id);
 		File f = new File(fn);
 		if (f.exists()){
 			logger.warn("Warning: File exists, will be replaced");
 		}
 		
 		// TODO: Fix if more than last directory does not exist...
 		// Create directory for f if not exists
 		File dirn = f.getParentFile();
 		if (!dirn.exists()) {
 			logger.info("Make directory " + dirn.getAbsolutePath());
 			boolean b = dirn.mkdir();
 			if (!b) {
 				logger.error("Could not create directory, maybe parent-directories does not exist, try create parent dirs. This will fail!");
 				throw new IllegalArgumentException("Could not create directory, maybe parent-directories does not exist, try create parent dirs. This will fail!");
 			}
 		}
 		
 		OutputStream os;
 		try {
 			os = new FileOutputStream(f);
 			if (comment != null) {
 				os.write(comment.getBytes());
 				os.write('\n');
 			}
 			os.write(sb.toString().getBytes());
 			os.flush();
 			os.close();
 			logger.info("Created file " + f.getPath());
 		} catch (IOException e) {
 			logger.error("dumpToFile(Serializable rf, String id, String comment): "+stringifyException(e));
 		}
 	}
 	
 	private static byte[] loadFromFile(String fn) {
 		File f = new File(fn);
 		byte[] ba = null;
 		if (f.exists()) {
 			int len = (int) f.length();
 			ba = new byte[len];
 			FileInputStream fis = null;
 			try {
 				fis = new FileInputStream(f);
 				int i = 0;
 				int rest = len - i;
 				while (rest > 0 && (i = fis.read(ba, i, rest)) != -1) {
 					rest -= i;
 				}
 			} catch (IOException e) {
 				logger.error("loadFromFile(String fn): "+stringifyException(e));
 			} finally {
 				try {
 					if (fis != null)
 						fis.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return ba;
 	}
 	
 	private static String stringifyException(Exception e) {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		PrintStream pr = new PrintStream(baos);
 		e.printStackTrace(pr);
 		pr.close();
 		String ret = new String(baos.toByteArray());
 		try {
 			baos.close();
 		} catch (IOException e1) { // Will never happen since
 			e1.printStackTrace();
 		}
 		return ret;
 	}
 }
