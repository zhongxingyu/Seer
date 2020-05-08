 package org.qooxdoo.charless.build.config;
 
 import java.io.File;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.qooxdoo.charless.build.config.Manifest;
 
 
 import junit.framework.TestCase;
 
 public class ManifestTest extends TestCase {
 	
 	public void testReadInfo() throws Exception {
 		File manifest = new File(this.getClass().getResource("/Manifest.json").getPath());
 		Manifest m = Manifest.read(manifest);
 	
 		Map<String,String> expected = new LinkedHashMap<String,String>() {{
 			put("name","qooxdoo.application.name");
 			put("summary","qooxdoo.application.summary");
 			put("description","qooxdoo.application.description");
 			put("version","0.1");
 			
 		}};
 		for (String key: expected.keySet()) {
 			assertEquals(expected.get(key),(String)m.info(key));
 		}
 		// Shortcuts
 		assertEquals(expected.get("name"),m.getName());
 		assertEquals(expected.get("summary"),m.getSummary());
 		assertEquals(expected.get("description"),m.getDescription());
 		assertEquals(expected.get("version"),m.getVersion());
 		
 	}
 	
 	public void testReadProvides() throws Exception {
 		File manifest = new File(this.getClass().getResource("/Manifest.json").getPath());
 		Manifest m = Manifest.read(manifest);
 	
 		Map<String,String> expected = new LinkedHashMap<String,String>() {{
 			put("namespace"   , "qooxdoo.application.namespace");
 			put("encoding"    , "project.build.sourceEncoding");
 			put("class"       , "qooxdoo.application.sourceDirectory");
 			put("resource"    , "qooxdoo.application.resourcesDirectory");
 			put("translation" , "qooxdoo.application.translationDirectory");
 			put("type"        , "application");
 		}};
 		for (String key: expected.keySet()) {
			assertEquals(expected.get(key),(String)m.info(key));
 		}
 		// Shortcuts
 		assertEquals(expected.get("namespace"),m.getNamespace());
 		assertEquals(expected.get("encoding"),m.getEncoding());
 		assertEquals(expected.get("class"),m.getKlass());
 		assertEquals(expected.get("resource"),m.getResource());
 		assertEquals(expected.get("translation"),m.getTranslation());
 		assertEquals(expected.get("type"),m.getType());
 		
 	}
 	
 }
