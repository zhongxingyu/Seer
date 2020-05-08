 package com.vaguehope.cmstoad;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 public class IntTest {
 
 	private static final int SOURCE_FILE_COUNT = 3;
 	private static final int SOURCE_FILE_MIN_LENGTH = 10000;
 
 	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
 
 	protected static final PrintStream OUT = System.out;
 	protected static final PrintStream ERR = System.err;
 
 	private File dir;
 	private Random rand;
 
 	@BeforeClass
 	public static void beforeClass () throws Exception {
 		ProviderHelper.initProvider();
 	}
 
 	@Before
 	public void before () throws Exception {
 		this.dir = this.temporaryFolder.getRoot();
 		this.rand = new Random();
 	}
 
 	@Test
	public void itEncryptesAndDecryptsAFileWithMultipleKeyPairs () throws Exception {
 		Map<String, PublicKey> publicKeys = new HashMap<String, PublicKey>();

 		List<TestKeyPair> keys = new ArrayList<TestKeyPair>();
 		keys.add(new TestKeyPair("desu", 1024, this.dir).putPublicKey(publicKeys));
 		keys.add(new TestKeyPair("foobar", 2048, this.dir).putPublicKey(publicKeys));
 
 		List<File> sources = makeRandomFiles(SOURCE_FILE_COUNT, SOURCE_FILE_MIN_LENGTH);
 		new Encrypt(publicKeys, sources, this.dir).run(OUT, ERR);
 
 		for (TestKeyPair kp : keys) {
 			List<File> crypted = new ArrayList<File>();
 			for (File source : sources) {
 				crypted.add(encryptedFileFromSource(source));
 				decryptedFileFromSource(source).delete();
 			}
 			new Decrypt(kp.asPrivateKeyMap(), crypted, this.dir).run(OUT, ERR);
 
 			for (File source : sources) {
 				assertFilesEqual(source, decryptedFileFromSource(source));
 			}
 		}
 	}
 
 	private File encryptedFileFromSource (File source) {
 		return new File(this.dir, source.getName() + C.ENCRYPTED_FILE_EXT);
 	}
 
 	private File decryptedFileFromSource (File source) {
 		return new File(this.dir, source.getName() + C.ENCRYPTED_FILE_EXT + C.DECRYPTED_FILE_EXT);
 	}
 
 	private List<File> makeRandomFiles (int count, int aproxLength) throws IOException {
 		List<File> sources = new ArrayList<File>();
 		for (int i = 0; i < count; i++) {
 			File f = this.temporaryFolder.newFile();
 			writeRandomData(f, aproxLength);
 			sources.add(f);
 		}
 		return sources;
 	}
 
 	private void writeRandomData (File file, int aproxLength) throws IOException {
 		OutputStream out = new FileOutputStream(file);
 		try {
 			byte[] buffer = new byte[1024];
 			int bytesWritten = 0;
 			while (bytesWritten < aproxLength) {
 				this.rand.nextBytes(buffer);
 				out.write(buffer);
 				bytesWritten += buffer.length;
 			}
 		}
 		finally {
 			out.close();
 		}
 	}
 
 	private static void assertFilesEqual (File expected, File actual) throws IOException {
 		byte[] expectedData = FileUtils.readFileToByteArray(expected);
 		byte[] actualData = FileUtils.readFileToByteArray(actual);
 		assertThat(actualData, equalTo(expectedData));
 	}
 
 	private static class TestKeyPair {
 
 		private final String name;
 		private final PublicKey publicKey;
 		private final PrivateKey privateKey;
 
 		public TestKeyPair (String name, int keysize, File dir) throws IOException {
 			this.name = name;
 			new KeyGen(name, keysize, dir).run(OUT, ERR);
 			this.publicKey = KeyHelper.readKey(new File(dir, KeyHelper.publicKeyName(name)), PublicKey.class);
 			this.privateKey = KeyHelper.readKey(new File(dir, KeyHelper.privateKeyName(name)), PrivateKey.class);
 		}
 
 		public Map<String, PrivateKey> asPrivateKeyMap () {
 			Map<String, PrivateKey> privateKeys = new HashMap<String, PrivateKey>();
 			putPrivateKeys(privateKeys);
 			return privateKeys;
 		}
 
 		public TestKeyPair putPublicKey (Map<String, PublicKey> publicKeys) {
 			publicKeys.put(getName(), getPublicKey());
 			return this;
 		}
 
 		public TestKeyPair putPrivateKeys (Map<String, PrivateKey> privateKeys) {
 			privateKeys.put(getName(), getPrivateKey());
 			return this;
 		}
 
 		public String getName () {
 			return this.name;
 		}
 
 		public PublicKey getPublicKey () {
 			return this.publicKey;
 		}
 
 		public PrivateKey getPrivateKey () {
 			return this.privateKey;
 		}
 
 	}
 
 }
