 package com.vaguehope.cmstoad;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.security.Key;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PEMWriter;
 
 public final class KeyHelper {
 
 	private KeyHelper () {
 		throw new AssertionError();
 	}
 
 	public static void writeKey (Key k, File f) throws IOException {
 		PEMWriter w = new PEMWriter(new FileWriter(f));
 		try {
 			w.writeObject(k);
 		}
 		finally {
 			w.close();
 		}
 	}
 
 	public static <T extends Key> T readKey (File f, Class<T> type) throws IOException {
 		PEMReader r = new PEMReader(new FileReader(f));
 		try {
			Object obj = null;
			for (int i = 0; i < 100; i++) { // FIXME Bad hack to avoid comments at start of PEM file.
				obj = r.readObject();
				if (obj != null) break;
			}
			if (obj == null) throw new IOException("Failed to load key '" + f.getAbsolutePath() + "'.");
 			if (type.isAssignableFrom(PrivateKey.class)) {
 				if (obj instanceof KeyPair) return (T) ((KeyPair) obj).getPrivate();
 			}
 			else if (type.isAssignableFrom(PublicKey.class)) {
 				if (obj instanceof KeyPair) return (T) ((KeyPair) obj).getPublic();
 			}
 			return type.cast(obj);
 		}
 		finally {
 			r.close();
 		}
 	}
 
 	public static <T extends Key> Map<String, T> readKeys (List<File> files, Class<T> type) throws IOException {
 		Map<String, T> keys = new HashMap<String, T>();
 		for (File file : files) {
 			T key = KeyHelper.readKey(file, type);
 			keys.put(KeyHelper.keyBaseName(file), key);
 		}
 		return keys;
 	}
 
 	public static String keyBaseName (File file) {
 		String name = file.getName();
 		return name.substring(0, name.indexOf("."));
 	}
 
 	public static String publicKeyName (String baseName) {
 		return baseName + ".public.pem";
 	}
 
 	public static String privateKeyName (String baseName) {
 		return baseName + ".private.pem";
 	}
 
 }
