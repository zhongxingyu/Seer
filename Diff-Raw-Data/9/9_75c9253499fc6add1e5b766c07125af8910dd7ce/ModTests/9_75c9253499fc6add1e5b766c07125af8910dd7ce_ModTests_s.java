 package algorithms.verifiers.Main;
 
 import main.Logger;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import algorithms.params.Parameters.Type;
 import algorithms.verifiers.MainVerifier;
 
 public class ModTests {
 
 	private final Logger logger = new MockedLogger();
 	
 	//****************SHUFFLE*******************
 
 	@Test
 	public void VerifyShuffleModGroupTest() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportModGroup/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportModGroup/default").getFile(),
 				Type.MIXING, "default", 1, true, true, false));
 	}
 
 	@Test
 	public void VerifyShuffleModGroupSmallTest() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("exportSmall/protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("exportSmall/default").getFile(),
 				Type.MIXING, "default", 1, true, true, false));
 	}
 
 	@Test
 	public void VerifyShuffleModGroup3Test() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("exportMod3/protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("exportMod3/default").getFile(),
 				Type.MIXING, "default", 1, true, true, false));
 	}
 	
 	@Test
 	public void VerifyShuffleModWidth3lTest() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportModWidth3/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportModWidth3/default").getFile(),
 				Type.MIXING, "default", 3, true, true, false));
 	}
 	
 	@Test
 	public void VerifyShuffleExportDecTest1() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w1ModGroup/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w1ModGroup/default").getFile(),
 				Type.MIXING, "default", 1, true, true, false));
 	}
 	
 	
 	@Test
 	public void VerifyShuffleExportDecTest3() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w3ModGroup/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w3ModGroup/default").getFile(),
 				Type.MIXING, "default", 3, true, true, false));
 	}
 	
 	
 	//******************DECRYPTION*******************
 
 	@Test
 	public void VerifyDecModGroup3Test() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("exportMod3/protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("exportMod3/default").getFile(),
 				Type.MIXING, "default", 1, false, false, true));
 	}
 	
 	@Test
 	public void VerifyDecExportDecTest1() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w1ModGroup/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w1ModGroup/default").getFile(),
 				Type.MIXING, "default", 1, false, false, true));
 	}
 	
 	
 	@Test
 	public void VerifyDecExportDecTest3() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w3ModGroup/protInfo.xml").getFile(),
 				getClass().getClassLoader()
 						.getResource("exportsDecOutputs/w3ModGroup/default").getFile(),
 				Type.MIXING, "default", 3, false, false, true));
 	}
 
 	
 
 	public static class MockedLogger extends Logger {
 		public MockedLogger() {
 			super(false);
 		}
 
 		@Override
 		public void sendLog(String message, Severity severity) {
 		}
 
 	}
 	
 	//******************CCPOS*******************
 	
 	@Test
	public void VerifyCcposModGroup_W3Test() throws Exception {
 
 		MainVerifier mainVer = new MainVerifier(logger);
 		Assert.assertTrue(mainVer.verify(getClass().getClassLoader()
 				.getResource("exportCcpos/mod1023_max5_n3_w1/protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("exportCcpos/mod1023_max5_n3_w1/default").getFile(),
 				Type.MIXING, "default", 1, false, true, false));
 	}
 
 }
