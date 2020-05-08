 package de.tr0llhoehle.buschtrommel.test;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.tr0llhoehle.buschtrommel.HashFuncWrapper;
 import de.tr0llhoehle.buschtrommel.LocalShareCache;
 import de.tr0llhoehle.buschtrommel.models.Host;
 import de.tr0llhoehle.buschtrommel.models.LocalShare;
 import de.tr0llhoehle.buschtrommel.models.Message;
 import de.tr0llhoehle.buschtrommel.network.FileTransferAdapter;
 import de.tr0llhoehle.buschtrommel.network.ITransferProgress;
 import de.tr0llhoehle.buschtrommel.test.mockups.FileContentMock;
 
 /**
  * Tests that transfer a file from one instance to another
  * @author Tobias Sturm
  *
  */
 public class TestTransferSystem {
 
 	LocalShareCache sendersShares;
 	LocalShareCache receiversShares;
 	FileTransferAdapter sender;
 	FileTransferAdapter receiver;
 	java.io.File tmpFile;
 	String hashA;
 	
 	@Before
 	public void setUp() throws Exception {
 		LocalShareCache sendersShares = new LocalShareCache();
 		hashA = HashFuncWrapper.hash(FileContentMock.contentA.getBytes(Message.ENCODING));
 		sendersShares.newShare(new LocalShare(hashA, FileContentMock.contentA.length(), -1, "file abc", "meta", "abc.in"));
 		FileContentMock.writeFileContent("abc.in", FileContentMock.contentA);
 		
 		LocalShareCache receiversShares = new LocalShareCache();		
 		
		sender = new FileTransferAdapter(sendersShares , 8007);
 		
		receiver = new FileTransferAdapter(receiversShares , 8008);
 		tmpFile = new java.io.File("tmp.out");
 		Thread.sleep(500);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		sender.close();
 		receiver.close();
 		(new java.io.File("abc.in")).delete();
 		tmpFile.delete();
 	}
 
 	@Test(timeout=150000)
 	public void testNormalTransfer() throws IOException, InterruptedException {
 		Host senderHost = new Host(InetAddress.getLocalHost(), "sender", sender.getPort());
 		
 		ITransferProgress progress = receiver.DownloadFile(hashA, senderHost , FileContentMock.contentA.length(), tmpFile);
 		Thread.sleep(1000);
 		while(progress.isActive()) {
 			Thread.sleep(100);
 		}
 		assertEquals(FileContentMock.getFileContent("abc.in"), FileContentMock.getFileContent(tmpFile.getAbsolutePath()));
 		FileContentMock.getFileContent(tmpFile.getAbsolutePath());
 	}
 
 }
