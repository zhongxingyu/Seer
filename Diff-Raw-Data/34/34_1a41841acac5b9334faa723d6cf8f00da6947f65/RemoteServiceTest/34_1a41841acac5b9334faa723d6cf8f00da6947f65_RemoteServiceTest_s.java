 package org.jvnet.hudson.plugins.fortify360;
 
 import java.io.*;
 import java.util.ArrayList;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.jvnet.hudson.plugins.fortify360.FPRSummary;
 import org.jvnet.hudson.plugins.fortify360.RemoteService;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.FilenameUtils;
 
 public class RemoteServiceTest {
 
 	// if the machine don't have reportGenerator, we will by-pass these test cases
 	private boolean noReportGenerator;
 	
 	@Before
 	public void setUp() throws Exception {
 		noReportGenerator = false;
 		
 		try {
 			String os = System.getProperty("os.name");
 			String image = os.matches("Win.*|.*win.*") ? "reportGenerator.bat" : "reportGenerator";
 	
 			ArrayList<String> cmd = new ArrayList<String>();
 			cmd.add(image);
 			cmd.add("-help"); // no such option, but this is fine
 			
 			ProcessBuilder pb = new ProcessBuilder(cmd);
 			Process proc = pb.start();
 			proc.waitFor();
 			
 		} catch (IOException e) {
 			if ( e.getMessage().startsWith("CreateProcess:") ) {
 				noReportGenerator = true;
 				System.out.println("Test bypassed because reportGenerator was not found");
 			}
 		}
 
 	}
 
 	private File resourceToFile(String filename) throws IOException {
 		InputStream in = null;
 		OutputStream out = null;
 		try {
 			File tmp = File.createTempFile("test", "." + FilenameUtils.getExtension(filename));
 			tmp.deleteOnExit();
 			in = this.getClass().getClassLoader().getResourceAsStream(filename);
 			out = new FileOutputStream(tmp);
 			IOUtils.copy(in, out);
 			return tmp;
 		} finally {
 			IOUtils.closeQuietly(in);
 			IOUtils.closeQuietly(out);
 		}
 	}
 
 	@Test
 	public void testInvoke() throws Exception {
 		if ( !noReportGenerator ) {
 			String fpr = "WebGoat_57.fpr";
 			File fprFile = resourceToFile(fpr);
 			File path = fprFile.getParentFile();
 			RemoteService service = new RemoteService(fprFile.getName(), null, null);
 			FPRSummary summary = service.invoke(path, null);
 			double nvs = summary.getNvs();
 			Integer count = summary.getFailedCount();
 			System.out.println("NVS = " + nvs);
 			System.out.println("Fail Count = " + count);
 			assertEquals(95.52, nvs, 0.1);
			assertNull(count);
 		}
 	}
 
 	@Test
 	public void testInvoke2() throws Exception {
 		if ( !noReportGenerator ) {
 			String fpr = "WebGoat_Audited.fpr";
 			File fprFile = resourceToFile(fpr);
 			File path = fprFile.getParentFile();
 			RemoteService service = new RemoteService(fprFile.getName(), "Likely", "[fortify priority order]:high category:/SQL Injection|Cross-Site Scripting/");
 			FPRSummary summary = service.invoke(path, null);
 			double nvs = summary.getNvs();
 			Integer count = summary.getFailedCount();
 			System.out.println("NVS = " + nvs);
 			System.out.println("Fail Count = " + count);
 			assertEquals(413.25, nvs, 0.1);
 			assertEquals(139, count);
 		}
 	}
 	
 	@Test
 	public void testInvoke3() throws Exception {
 		if ( !noReportGenerator ) {
 			String fpr = "test1.fpr";
 			File fprFile = resourceToFile(fpr);
 			File path = fprFile.getParentFile();
 			RemoteService service = new RemoteService(fprFile.getName(), "Fortify Priority Order", "[fortify priority order]:high category:/SQL Injection|Cross-Site Scripting/");
 			FPRSummary summary = service.invoke(path, null);
 			double nvs = summary.getNvs();
 			Integer count = summary.getFailedCount();
 			System.out.println("NVS = " + nvs);
 			System.out.println("Fail Count = " + count);
 			assertEquals(50.0, nvs, 0.1);
			assertNull(count);
 		}
 	}
 	
 	@Test
 	public void testInvoke4() throws Exception {
 		if ( !noReportGenerator ) {
 			String fpr = "test1.fpr";
 			File fprFile = resourceToFile(fpr);
 			File path = fprFile.getParentFile();
 			RemoteService service = new RemoteService(fprFile.getName(), "Fortify Priority Order", "category:j2ee bad practices\\: leftover debug code");
 			FPRSummary summary = service.invoke(path, null);
 			double nvs = summary.getNvs();
 			Integer count = summary.getFailedCount();
 			System.out.println("NVS = " + nvs);
 			System.out.println("Fail Count = " + count);
 			assertEquals(50.0, nvs, 0.1);
 			assertEquals(2, count);
 		}
 	}
 	
 	@Test
 	public void testInvoke5() throws Exception {
 		if ( !noReportGenerator ) {
 			String fpr = "test1.fpr";
 			File fprFile = resourceToFile(fpr);
 			File path = fprFile.getParentFile();
 			RemoteService service = new RemoteService(fprFile.getName(), "Likely", "category:j2ee bad practices\\: leftover debug code");
 			FPRSummary summary = service.invoke(path, null);
 			double nvs = summary.getNvs();
 			Integer count = summary.getFailedCount();
 			System.out.println("NVS = " + nvs);
 			System.out.println("Fail Count = " + count);
 			assertEquals(30.0, nvs, 0.1);
			assertNull(count);
 		}
 	}
 }
