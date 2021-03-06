 package test.ccn.library.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.DigestInputStream;
 import java.security.DigestOutputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Random;
 
 import javax.xml.stream.XMLStreamException;
 
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.parc.ccn.Library;
 import com.parc.ccn.data.ContentName;
 import com.parc.ccn.data.ContentObject;
 import com.parc.ccn.library.CCNLibrary;
 import com.parc.ccn.library.io.CCNInputStream;
 import com.parc.ccn.library.io.CCNOutputStream;
 import com.parc.ccn.library.io.CCNVersionedInputStream;
import com.parc.ccn.library.profiles.SegmentationProfile;
 import com.parc.ccn.library.profiles.VersionMissingException;
 import com.parc.ccn.library.profiles.VersioningProfile;
 
 public class CCNVersionedInputStreamTest {
 	
 	static ContentName defaultStreamName;
 	static ContentName firstVersionName;
 	static int firstVersionLength;
	static int firstVersionMaxSegment;
 	static byte [] firstVersionDigest;
 	static ContentName middleVersionName;
 	static int middleVersionLength;
	static int middleVersionMaxSegment;
 	static byte [] middleVersionDigest;
 	static ContentName latestVersionName;
 	static int latestVersionLength;
	static int latestVersionMaxSegment;
 	static byte [] latestVersionDigest;
 	static CCNLibrary outputLibrary;
 	static CCNLibrary inputLibrary;
	static final int MAX_FILE_SIZE = 1024*1024; // 1 MB
 	static final int BUF_SIZE = 4096;
 	
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		Random randBytes = new Random(); // doesn't need to be secure
 		outputLibrary = CCNLibrary.open();
 		inputLibrary = CCNLibrary.open();
 		
 		// Write a set of output
 		defaultStreamName = ContentName.fromNative("/test/stream/versioning/LongOutput.bin");
 		
 		firstVersionName = VersioningProfile.versionName(defaultStreamName);
		firstVersionLength = randBytes.nextInt(MAX_FILE_SIZE);
		firstVersionMaxSegment = (int)Math.ceil(firstVersionLength/SegmentationProfile.DEFAULT_BLOCKSIZE);
 		firstVersionDigest = writeFileFloss(firstVersionName, firstVersionLength, randBytes);
 		
 		middleVersionName = VersioningProfile.versionName(defaultStreamName);
		middleVersionLength = randBytes.nextInt(MAX_FILE_SIZE);
		middleVersionMaxSegment = (int)Math.ceil(middleVersionLength/SegmentationProfile.DEFAULT_BLOCKSIZE);
 		middleVersionDigest = writeFileFloss(middleVersionName, middleVersionLength, randBytes);
 
 		latestVersionName = VersioningProfile.versionName(defaultStreamName);
		latestVersionLength = randBytes.nextInt(MAX_FILE_SIZE);
		latestVersionMaxSegment = (int)Math.ceil(latestVersionLength/SegmentationProfile.DEFAULT_BLOCKSIZE);
 		latestVersionDigest = writeFileFloss(latestVersionName, latestVersionLength, randBytes);
 		
 	}
 	
 	public static byte [] writeFileFloss(ContentName completeName, int fileLength, Random randBytes) throws XMLStreamException, IOException, NoSuchAlgorithmException {
 		CCNOutputStream stockOutputStream = new CCNOutputStream(completeName, outputLibrary);
 		
 		DigestOutputStream digestStreamWrapper = new DigestOutputStream(stockOutputStream, MessageDigest.getInstance("SHA1"));
 		byte [] bytes = new byte[BUF_SIZE];
 		int elapsed = 0;
 		int nextBufSize = 0;
 		boolean firstBuf = true;
 		System.out.println("Writing file: " + completeName + " bytes: " + fileLength);
 		while (elapsed < fileLength) {
 			nextBufSize = ((fileLength - elapsed) > BUF_SIZE) ? BUF_SIZE : (fileLength - elapsed);
 			randBytes.nextBytes(bytes);
 			digestStreamWrapper.write(bytes, 0, nextBufSize);
 			elapsed += nextBufSize;
 			if (firstBuf) {
 				startReader(completeName, fileLength);
 				firstBuf = false;
 			}
 			System.out.println(completeName + " wrote " + elapsed + " out of " + fileLength + " bytes.");
 		}
 		digestStreamWrapper.close();
 		System.out.println("Finished writing file " + completeName);
 		return digestStreamWrapper.getMessageDigest().digest();
 	}
 	
 	public static void startReader(final ContentName completeName, final int fileLength) {
 		new Thread(){
 	        public void run() {
 	           try {
 				readFile(completeName, fileLength);
 			} catch (Exception e) {
 				e.printStackTrace();
 				Assert.fail("Class setup failed! " + e.getClass().getName() + ": " + e.getMessage());
 			} 
 	        }
 	    }.start();
 	}
 	
 	public static byte [] readFile(ContentName completeName, int fileLength) throws XMLStreamException, IOException {
 		CCNInputStream inputStream = new CCNInputStream(completeName);
 		System.out.println("Reading file : " + completeName);
 		return readFile(inputStream, fileLength);
 	}
 	
 	public static byte [] readFile(InputStream inputStream, int fileLength) throws IOException, XMLStreamException {
 		
 		DigestInputStream dis = null;
 		try {
 			dis = new DigestInputStream(inputStream, MessageDigest.getInstance("SHA1"));
 		} catch (NoSuchAlgorithmException e) {
 			Library.logger().severe("No SHA1 available!");
 			Assert.fail("No SHA1 available!");
 		}
 		int elapsed = 0;
 		int read = 0;
 		byte [] bytes = new byte[BUF_SIZE];
 		while (elapsed < fileLength) {
 			read = dis.read(bytes);
 			elapsed += read;
 			if (read == 0) {
 				System.out.println("Ran out of things to read at " + elapsed + " bytes out of " + fileLength);
 				break;
 			}
 			System.out.println(" read " + elapsed + " bytes out of " + fileLength);
 		}
 		return dis.getMessageDigest().digest();
 	}
 	
 	@Test
 	public void testCCNVersionedInputStreamContentNameLongPublisherKeyIDCCNLibrary() {
 		try {
 			// we can make a new library; as long as we don't use the outputLibrary it should work
			CCNVersionedInputStream vfirst = 
				new CCNVersionedInputStream(firstVersionName, 
						((3 > firstVersionMaxSegment) ? firstVersionMaxSegment : 3), outputLibrary.getDefaultPublisher(), inputLibrary);
			CCNVersionedInputStream vlatest = new CCNVersionedInputStream(defaultStreamName, 
					((3 > latestVersionMaxSegment) ? latestVersionMaxSegment : 3), outputLibrary.getDefaultPublisher(), inputLibrary);
 			testArgumentRunner(vfirst, vlatest);
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 			Assert.fail("XMLStreamException: " + e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			Assert.fail("IOException: " + e.getMessage());
 		}
 	}
 
 	@Test
 	public void testCCNVersionedInputStreamContentNamePublisherKeyIDCCNLibrary() {
 		try {
 			// we can make a new library; as long as we don't use the outputLibrary it should work
 			CCNVersionedInputStream vfirst = new CCNVersionedInputStream(firstVersionName, outputLibrary.getDefaultPublisher(), inputLibrary);
 			CCNVersionedInputStream vlatest = new CCNVersionedInputStream(defaultStreamName, outputLibrary.getDefaultPublisher(), inputLibrary);
 			testArgumentRunner(vfirst, vlatest);
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 			Assert.fail("XMLStreamException: " + e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			Assert.fail("IOException: " + e.getMessage());
 		}
 	}
 
 	@Test
 	public void testCCNVersionedInputStreamContentName() {
 		try {
 			// we can make a new library; as long as we don't use the outputLibrary it should work
 			CCNVersionedInputStream vfirst = new CCNVersionedInputStream(firstVersionName);
 			CCNVersionedInputStream vlatest = new CCNVersionedInputStream(defaultStreamName);
 			testArgumentRunner(vfirst, vlatest);
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 			Assert.fail("XMLStreamException: " + e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			Assert.fail("IOException: " + e.getMessage());
 		}
 	}
 
 	@Test
 	public void testCCNVersionedInputStreamContentNameCCNLibrary() {
 		
 		try {
 			CCNVersionedInputStream vfirst = new CCNVersionedInputStream(firstVersionName, inputLibrary);
 			CCNVersionedInputStream vlatest = new CCNVersionedInputStream(defaultStreamName, inputLibrary);
 			testArgumentRunner(vfirst, vlatest);
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 			Assert.fail("XMLStreamException: " + e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			Assert.fail("IOException: " + e.getMessage());
 		}
 	}
 	
 	protected void testArgumentRunner(CCNVersionedInputStream vfirst,
 									  CCNVersionedInputStream vlatest) {
 		try {
 			Assert.assertEquals(vfirst.baseName(), firstVersionName);
 			Assert.assertEquals(VersioningProfile.versionRoot(vfirst.baseName()), defaultStreamName);
 			byte b = (byte)vfirst.read();
 			if (b != b) {
 				// suppress warning...
 			}
 			Assert.assertEquals(VersioningProfile.getVersionAsTimestamp(firstVersionName), 
 								VersioningProfile.getVersionAsTimestamp(vfirst.baseName()));
 			Assert.assertEquals(VersioningProfile.getVersionAsTimestamp(firstVersionName),
 							    vfirst.getVersionAsTimestamp());
 
 			System.out.println("Opened stream on latest version, expected: " + latestVersionName + " got: " + 
 								vlatest.baseName());
 			b = (byte)vlatest.read();
 			System.out.println("Post-read: Opened stream on latest version, expected: " + latestVersionName + " got: " + 
 					vlatest.baseName());
 			Assert.assertEquals(vlatest.baseName(), latestVersionName);
 			Assert.assertEquals(VersioningProfile.versionRoot(vlatest.baseName()), defaultStreamName);
 			Assert.assertEquals(VersioningProfile.getVersionAsLong(latestVersionName), 
 					VersioningProfile.getVersionAsLong(vlatest.baseName()));
 			Assert.assertEquals(VersioningProfile.getVersionAsTimestamp(latestVersionName), 
 								VersioningProfile.getVersionAsTimestamp(vlatest.baseName()));
 			Assert.assertEquals(VersioningProfile.getVersionAsTimestamp(latestVersionName),
 					vlatest.getVersionAsTimestamp());
 		
 		} catch (IOException e) {
 			e.printStackTrace();
 			Assert.fail("IOException: " + e.getMessage());
 		} catch (VersionMissingException e) {
 			e.printStackTrace();
 			Assert.fail("VersionMissingException: " + e.getMessage());
 		}
 
 	}
 
 	@Test
 	public void testCCNVersionedInputStreamContentNameInt() {
 		try {
 			// we can make a new library; as long as we don't use the outputLibrary it should work
			CCNVersionedInputStream vfirst = new CCNVersionedInputStream(firstVersionName, ((4 > firstVersionMaxSegment) ? firstVersionMaxSegment : 4));
			CCNVersionedInputStream vlatest = new CCNVersionedInputStream(defaultStreamName, ((4 > latestVersionMaxSegment) ? latestVersionMaxSegment : 4));
 			testArgumentRunner(vfirst, vlatest);
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 			Assert.fail("XMLStreamException: " + e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			Assert.fail("IOException: " + e.getMessage());
 		}
 	}
 
 	@Test
 	public void testCCNVersionedInputStreamContentObjectCCNLibrary() {
 		try {
 			// we can make a new library; as long as we don't use the outputLibrary it should work
 			ContentObject firstVersionBlock = inputLibrary.get(firstVersionName, 1000);
 			ContentObject latestVersionBlock = inputLibrary.getLatest(defaultStreamName, 1000);
 			CCNVersionedInputStream vfirst = new CCNVersionedInputStream(firstVersionBlock, inputLibrary);
 			CCNVersionedInputStream vlatest = new CCNVersionedInputStream(latestVersionBlock, inputLibrary);
 			testArgumentRunner(vfirst, vlatest);
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 			Assert.fail("XMLStreamException: " + e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			Assert.fail("IOException: " + e.getMessage());
 		}
 	}
 
 	@Test
 	public void testReadByteArray() {
 		// Test other forms of read in superclass test.
 		try {
 			CCNVersionedInputStream vfirst = new CCNVersionedInputStream(firstVersionName, inputLibrary);
 			byte [] readDigest = readFile(vfirst, firstVersionLength);
 			Assert.assertArrayEquals(firstVersionDigest, readDigest);
 			CCNVersionedInputStream vmiddle = new CCNVersionedInputStream(middleVersionName, inputLibrary);
 			readDigest = readFile(vmiddle, middleVersionLength);
 			Assert.assertArrayEquals(middleVersionDigest, readDigest);
 			CCNVersionedInputStream vlatest = new CCNVersionedInputStream(defaultStreamName, inputLibrary);
 			readDigest = readFile(vlatest, latestVersionLength);
 			Assert.assertArrayEquals(latestVersionDigest, readDigest);
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 			Assert.fail("XMLStreamException: " + e.getMessage());
 		} catch (IOException e) {
 			e.printStackTrace();
 			Assert.fail("IOException: " + e.getMessage());
 		}
 	}
 
 }
