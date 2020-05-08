 package amd.tsino.bootstrap;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class EtagDatabase {
 	private static EtagDatabase instance;
 	private final static String ETAGS_FILENAME = "etags.json";
 	private final static String HASHES_FILENAME = "hashes.json";
 	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
 	private Map<String, String> etags;
 	private Map<String, String> hashes;
 
 	public static void closeSilently(Closeable closeable) {
 		if (closeable != null) {
 			try {
 				closeable.close();
 			} catch (IOException localIOException) {
 			}
 		}
 	}
 
 	public static String copyAndDigest(InputStream inputStream,
 			OutputStream outputStream) throws IOException,
 			NoSuchAlgorithmException {
 		MessageDigest digest = MessageDigest.getInstance("MD5");
 		byte[] buffer = new byte[65536];
 		try {
 			int read = inputStream.read(buffer);
 			while (read >= 1) {
 				digest.update(buffer, 0, read);
 				outputStream.write(buffer, 0, read);
 				read = inputStream.read(buffer);
 			}
 		} finally {
 			closeSilently(inputStream);
 			closeSilently(outputStream);
 		}
 
 		return String.format("%1$032x",
 				new Object[] { new BigInteger(1, digest.digest()) });
 	}
 
 	public static String formatEtag(String etag) {
 		if (etag == null || "".equals(etag))
 			etag = "-";
 		else if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
 			etag = etag.substring(1, etag.length() - 1);
 		}
 		return etag;
 	}
 
 	public synchronized static EtagDatabase getInstance() {
 		if (instance == null) {
 			instance = new EtagDatabase();
 		}
 		return instance;
 	}
 
 	private static String getMD5(File file) {
 		DigestInputStream stream = null;
 		try {
 			stream = new DigestInputStream(new FileInputStream(file),
 					MessageDigest.getInstance("MD5"));
 			byte[] buffer = new byte[65536];
 
 			int read = stream.read(buffer);
 			while (read >= 1)
 				read = stream.read(buffer);
 		} catch (Exception ignored) {
 			return null;
 		} finally {
 			closeSilently(stream);
 		}
 		return String.format("%1$032x", new Object[] { new BigInteger(1, stream
 				.getMessageDigest().digest()) });
 	}
 
 	private EtagDatabase() {
 		etags = readFile(ETAGS_FILENAME);
 		if (etags == null) {
 			etags = new HashMap<String, String>();
 		}
 		hashes = readFile(HASHES_FILENAME);
 		if (hashes == null) {
 			hashes = new HashMap<String, String>();
 		}
 
 		cleanup();
 	}
 
 	public synchronized void cleanup() {
 		List<String> toRemove = new ArrayList<String>();
 		for (Map.Entry<String, String> entry : hashes.entrySet()) {
 			File file = new File(entry.getKey());
 			if (!file.exists()) {
 				toRemove.add(entry.getKey());
 			}
 		}
 		for (Map.Entry<String, String> entry : etags.entrySet()) {
 			if (!hashes.containsKey(entry.getKey())) {
 				toRemove.add(entry.getKey());
 			}
 		}
 		for (String file : toRemove) {
 			hashes.remove(file);
 			etags.remove(file);
 			Bootstrap.getInstance().log("Removing etag for: %s", file);
 		}
 	}
 
 	public synchronized String getEtag(File target) {
 		String md5 = getMD5(target);
 		if (md5 != null) {
 			if (md5.equals(hashes.get(target.getAbsolutePath()))) {
 				return formatEtag(etags.get(target.getAbsolutePath()));
 			}
 		}
 		return "-";
 	}
 
 	@SuppressWarnings("unchecked")
 	private Map<String, String> readFile(String name) {
 		Map<String, String> db = null;
 		File file = new File(Bootstrap.getInstance().getWorkingDir(), name);
 		try {
 			if (file.isFile()) {
 				db = gson.fromJson(FileUtils.readFileToString(file), Map.class);
 			}
		} catch (Exception ex) {
 			Bootstrap.getInstance().error(ex);
 		}
 		return db;
 	}
 
 	public synchronized void saveDatabase() {
 		writeFile(HASHES_FILENAME, hashes);
 		writeFile(ETAGS_FILENAME, etags);
 	}
 
 	public synchronized void setEtag(File target, String etag, String hash) {
 		hashes.put(target.getAbsolutePath(), hash);
 		etags.put(target.getAbsolutePath(), etag);
 	}
 
 	private void writeFile(String name, Map<String, String> db) {
 		File file = new File(Bootstrap.getInstance().getWorkingDir(), name);
 		try {
 			FileUtils.writeStringToFile(file, gson.toJson(db));
		} catch (Exception ex) {
 			Bootstrap.getInstance().error(ex);
 		}
 	}
 }
