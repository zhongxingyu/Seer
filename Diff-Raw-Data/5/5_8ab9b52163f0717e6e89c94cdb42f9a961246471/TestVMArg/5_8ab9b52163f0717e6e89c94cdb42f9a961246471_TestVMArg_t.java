 package org.eclipse.equinox.frameworkadmin.tests;
 
 import org.eclipse.core.runtime.Platform;
 
 import java.io.File;
 import java.io.IOException;
 import org.eclipse.equinox.internal.provisional.frameworkadmin.FrameworkAdminRuntimeException;
 import org.eclipse.equinox.internal.provisional.frameworkadmin.Manipulator;
 
 public class TestVMArg  extends FwkAdminAndSimpleConfiguratorTest {
 	private Manipulator m;
 
 	public TestVMArg(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws  Exception {
 		super.setUp();
 		m = createMinimalConfiguration(TestEclipseDataArea.class.getName());
 	}
 	
 	public void testVMInsideInstall() throws FrameworkAdminRuntimeException, IOException {
 		//Test VM path in the install folder
 		File jreLocation = new File(m.getLauncherData().getLauncher().getParentFile(), "jre");
 		m.getLauncherData().setJvm(jreLocation);
 		m.save(false);
 		assertNotContent(new File(getInstallFolder(), "eclipse.ini"), jreLocation.getAbsolutePath());
 		assertContent(new File(getInstallFolder(), "eclipse.ini"), "jre");
 		assertContent(m.getLauncherData().getLauncherConfigLocation(), "-vm");
 		assertContent(m.getLauncherData().getLauncherConfigLocation(), "jre");
 		assertNotContent(m.getLauncherData().getLauncherConfigLocation(), "file:");
 		m.load();
 		assertEquals(jreLocation, m.getLauncherData().getJvm());
 		
 		m.getLauncherData().setJvm(null);
 		m.save(false);
 		assertNotContent(m.getLauncherData().getLauncherConfigLocation(), "-vm");
 		assertNotContent(m.getLauncherData().getLauncherConfigLocation(), "jre");
 	}
 	
 	public void testVMOutsideInstall() throws FrameworkAdminRuntimeException, IOException {
 		//Test VM path in the install folder
 		File jreLocation = new File(m.getLauncherData().getLauncher().getParentFile(), "../../jre").getCanonicalFile();
 		m.getLauncherData().setJvm(jreLocation);
 		m.save(false);
		assertContent(new File(getInstallFolder(), "eclipse.ini"), jreLocation.getAbsolutePath().replace('\\','/'));
 		assertContent(m.getLauncherData().getLauncherConfigLocation(), "-vm");
 		assertContent(m.getLauncherData().getLauncherConfigLocation(), "jre");
 		assertNotContent(m.getLauncherData().getLauncherConfigLocation(), "file:");
 		m.load();
 		assertEquals(jreLocation, m.getLauncherData().getJvm());
 	}
 	
 	public void test269502() throws FrameworkAdminRuntimeException, IOException {
 		//Test VM path in the install folder
		String winPath = "c:/ibm5sr3/bin";
 		String linuxPath = "/Users/Pascal/ibm5sr3/bin";
 		String chosenPath = Platform.getOS().equals("win32") ? winPath : linuxPath; 
 		File jreLocation =  new File(chosenPath);
 		m.getLauncherData().setJvm(jreLocation);
 		m.save(false);
 		assertContent(new File(getInstallFolder(), "eclipse.ini"), chosenPath);
 		assertContent(m.getLauncherData().getLauncherConfigLocation(), "-vm");
 		assertContent(m.getLauncherData().getLauncherConfigLocation(), chosenPath);
 		assertNotContent(m.getLauncherData().getLauncherConfigLocation(), "file:");
 		m.load();
 		assertEquals(jreLocation, m.getLauncherData().getJvm());
 	}
 }
