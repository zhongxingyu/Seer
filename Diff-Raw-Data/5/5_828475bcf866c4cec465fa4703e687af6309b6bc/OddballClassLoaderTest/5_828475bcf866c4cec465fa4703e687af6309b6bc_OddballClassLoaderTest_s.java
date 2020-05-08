 package org.oddjob.ant;
 
 import java.io.File;
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 import org.apache.log4j.Logger;
 import org.oddjob.ConsoleCapture;
 import org.oddjob.Oddjob;
 import org.oddjob.OddjobLookup;
 import org.oddjob.OddjobSrc;
 import org.oddjob.OurDirs;
 import org.oddjob.arooa.ArooaDescriptor;
 import org.oddjob.arooa.ArooaType;
 import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
 import org.oddjob.arooa.convert.ArooaConversionException;
 import org.oddjob.arooa.life.InstantiationContext;
 import org.oddjob.arooa.parsing.ArooaElement;
 import org.oddjob.arooa.reflect.ArooaClass;
 import org.oddjob.arooa.reflect.ArooaPropertyException;
 import org.oddjob.arooa.xml.XMLConfiguration;
 import org.oddjob.io.FilesType;
 import org.oddjob.oddballs.DirectoryOddball;
 import org.oddjob.oddballs.Oddball;
 import org.oddjob.oddballs.OddballsDescriptorFactory;
 import org.oddjob.state.ParentState;
 import org.oddjob.util.URLClassLoaderType;
 
 public class OddballClassLoaderTest extends TestCase {
 
 	private static final Logger logger = Logger.getLogger(
 			OddballClassLoaderTest.class);
 	
 	@Override
 	protected void setUp() throws Exception {
 		logger.info("---------------  " + getName() + " ---------------");
 	}
 	
 	public void testLoadOddball() throws ClassNotFoundException, IOException {
 		
 		OddjobSrc oddjobHome = new OddjobSrc();
 		
 		FilesType oddjobLib = new FilesType();
 		oddjobLib.setFiles(oddjobHome.oddjobSrcBase() + "/lib/*.jar");
 		
 		File[] files = oddjobLib.toFiles();
 		assertTrue(files.length > 0);
 		
 		File[] all = new File[files.length + 1];
 		all[0] = new File(oddjobHome.oddjobSrcBase() + "/run-oddjob.jar");
 		System.arraycopy(files, 0, all, 1, files.length);
 		
 		URLClassLoaderType loaderType = new URLClassLoaderType();
 		loaderType.setFiles(all);
 		loaderType.setParent(getClass().getClassLoader());
 		
 		OurDirs base = new OurDirs();
 		
 		Oddball oddball = new DirectoryOddball().createFrom(
 				base.base(), loaderType.toValue());
 	
 		ArooaDescriptor descriptor = oddball.getArooaDescriptor();
 		
 		ArooaClass cl = descriptor.getElementMappings(
 				).mappingFor(
 						new ArooaElement("ant"), 
 						new InstantiationContext(ArooaType.COMPONENT, null));
 		
 		assertNotNull(cl);
 	}
 		
 	public void testClassLoaderOfTasksCreated() 
 	throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
 		
 		ClassLoader existing = Thread.currentThread().getContextClassLoader();
 		
 		Thread.currentThread().setContextClassLoader(null);
 		
 		try {
 			OddjobSrc oddjobHome = new OddjobSrc();
 			
 			FilesType oddjobLib = new FilesType();
 			oddjobLib.setFiles(oddjobHome.oddjobSrcBase() + "/lib/*.jar");
 			
 			File[] files = oddjobLib.toFiles();
 			assertTrue(files.length > 0);
 			
 			File[] all = new File[files.length + 1];
 			all[0] = new File(oddjobHome.oddjobSrcBase() + "/run-oddjob.jar");
 			System.arraycopy(files, 0, all, 1, files.length);
 			
 			URLClassLoaderType loaderType = new URLClassLoaderType();
 			loaderType.setFiles(all);
 			loaderType.setNoInherit(true);
 			
 			ClassLoader loader = loaderType.toValue();
 			
 			OurDirs base = new OurDirs();
 	
 			Class<?> ojClass = loader.loadClass(
 					Oddjob.class.getName());
 			
 			Runnable oddjob = (Runnable) ojClass.newInstance();
 	
 			Object oddball = loader.loadClass(
 					OddballsDescriptorFactory.class.getName()).newInstance(); 
 			
 			BeanUtilsPropertyAccessor accessor = 
 				new BeanUtilsPropertyAccessor();
 	
 			accessor.setProperty(oddball, "files", 
 					new File[] { base.base() } );
 			accessor.setProperty(oddjob, "descriptorFactory", oddball);
 					
 			accessor.setProperty(oddjob, "file", 
 					base.relative("test/files/classloader-test.xml"));
 			
 			ConsoleCapture console = new ConsoleCapture();
 			console.capture(Oddjob.CONSOLE);
 			
 			oddjob.run();		
 			
 			console.close();
 			console.dump(logger);
 						
 			String[] lines = console.getLines();
 			
 			assertEquals(2, lines.length);
 
 			
 			assertEquals("     [echo] Oddball ClassLoader", 
 					lines[0].substring(0, 31));
 			
 		}
 		finally {
 			Thread.currentThread().setContextClassLoader(existing);
 		}
 	}
 	
 	String EOL = System.getProperty("line.separator");
 	
 	public void testClassLoaderOfSubProject() 
 	throws ClassNotFoundException, IOException, ArooaPropertyException, ArooaConversionException {
 		
 		OurDirs base = new OurDirs();
 
 		OddjobSrc oddjobHome = new OddjobSrc();
 		
 		FilesType oddjobLib = new FilesType();
 		oddjobLib.setFiles(oddjobHome.oddjobSrcBase() + "/lib/*.jar");
 		
 		File[] files = oddjobLib.toFiles();
 		assertTrue(files.length > 0);
 		
 		URLClassLoaderType loaderType = new URLClassLoaderType();
 		loaderType.setFiles(files);
 		loaderType.setParent(getClass().getClassLoader());
 		
 		Oddjob oddjob = new Oddjob();
 		oddjob.setClassLoader(loaderType.toValue());
 		
 		OddballsDescriptorFactory oddball = 
 			new OddballsDescriptorFactory();
 		oddball.setFiles(new File[] { base.base() });
 		oddjob.setDescriptorFactory(oddball);
 		
 		
 		String xml = 
 			"<oddjob>" +
 			" <job>" +
 			"  <ant id='myant'>" +
 			"   <output>" +
 			"    <identify id='ant-output'>" +
 			"     <value>" +
 			"      <buffer/>" +
 			"     </value>" +
 			"    </identify>" +
 			"   </output>" +
 			"   <tasks>" +
 			"    <xml>" +
 			"     <tasks>" +
 			"      <ant antfile='" + 
 						base.relative("test/files/ant-oddjob.xml") + "' " +
 						"dir='" + base.relative("test/files") + "'/>" +
 			"     </tasks>" +
 			"    </xml>" +
 			"   </tasks>" +
 			"  </ant>" +
 			" </job>" +
 			"</oddjob>";
 			
 		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
 		
 		ConsoleCapture console = new ConsoleCapture();
 		console.capture(Oddjob.CONSOLE);
 		
 		oddjob.run();
 
 		assertEquals(ParentState.COMPLETE, 
 				oddjob.lastStateEvent().getState());
 		
 		console.close();
 		console.dump(logger);
 		
 		String buffer = new OddjobLookup(oddjob).lookup(
 				"ant-output", String.class);
 		
 		assertTrue(buffer.contains("COMPLETE"));
 		
 		ClassLoader classLoader = new OddjobLookup(oddjob).lookup(
 				"myant.class.classLoader", ClassLoader.class);
 		
 		
 		
 		String[] lines = console.getLines();
 		
 		assertEquals(1, lines.length);
 		
 		assertEquals(classLoader.toString() + EOL, lines[0]);
 	}
 
 
 }
