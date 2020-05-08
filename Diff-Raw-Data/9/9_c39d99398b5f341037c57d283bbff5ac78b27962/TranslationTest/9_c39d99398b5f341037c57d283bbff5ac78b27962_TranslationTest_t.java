 package com.rx201.dx.translator.test;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 
 import lombok.val;
 
 import org.jf.dexlib.DexFile;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import uk.ac.cam.db538.dexter.dex.AuxiliaryDex;
 import uk.ac.cam.db538.dexter.dex.Dex;
 import uk.ac.cam.db538.dexter.hierarchy.builder.HierarchyBuilder;
 
import com.rx201.dx.translator.DexCodeGeneration;

 @RunWith(Parameterized.class)
 public class TranslationTest {
 
 //	private static File frameworkDir = new File("framework-2.3-lite");
 	private static File testsDir = new File("../cg_test/tests/");
	private static File frameworkDir = new File("framework");
 //	private static File testsDir = new File("cg_test/android-apps/");
 	
 	private static HierarchyBuilder hierarchyBuilder;
 	
 	@BeforeClass 
 	public static void onlyOnce() throws IOException {
 	    // build runtime class hierarchy
 	    hierarchyBuilder = new HierarchyBuilder();
 	    
 	    System.out.println("Scanning framework");
 	    hierarchyBuilder.importFrameworkFolder(frameworkDir);
 	}
 	
 	@Parameters(name = "{1}")
 	public static Collection<Object[]> data() {
 		ArrayList<Object[]> tests = new ArrayList<Object[]>();
 		
 		for (String file : testsDir.list(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".apk");
 			}
 		})) {
 			tests.add(new Object[] {new File(testsDir.getAbsolutePath(), file ), file});
 		}
 		Collections.sort(tests, new Comparator<Object[]>() {
 
 			@Override
 			public int compare(Object[] arg0, Object[] arg1) {
 				File f0 = (File)arg0[0];
 				File f1 = (File)arg1[0];
 				return f0.getName().compareTo(f1.getName());
 			}
 		});
 		return tests;
 	}
 	
 	private File file;
 	
 	public TranslationTest(File file, String filename) {
 		this.file = file;
 	}
 	
 	@Test
 	public void test() throws IOException{
 
 		System.out.println("Scanning application");
 	    val fileApp = new DexFile(file);
 	    val fileAux = new DexFile("dexter_aux/build/libs/dexter_aux.dex");
 	    
 	    System.out.println("Building hierarchy");
 	    val buildData = hierarchyBuilder.buildAgainstApp(fileApp, fileAux);
 	    val hierarchy = buildData.getValA();
 	    val renamerAux = buildData.getValB();
 	    
 	    System.out.println("Parsing application");
 	    AuxiliaryDex dexAux = new AuxiliaryDex(fileAux, hierarchy, renamerAux); 
 	    Dex dexApp = new Dex(fileApp, hierarchy, dexAux);
 	    
 	    System.out.println("Instrumenting application");
 	    DexCodeGeneration.DEBUG = false;
 	    // dexApp.instrument(false);
 	    
 	    System.out.println("Recompiling application");
 	    dexApp.writeToFile();
 	    
 	    System.out.println("DONE");
 	}
 	
 }
