 package com.quanleimu.activity.test;
 
 import java.io.File;
 import java.util.List;
 
 import com.quanleimu.util.Util;
 
 import android.test.AndroidTestCase;
 
 public class SaveLoadFileTest extends AndroidTestCase {
 	public void setUp()
 	{
 		try {
 			super.setUp();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void testSaveAndLoadSer()
 	{
 		//First check 
 		
 		String tmpDir = System.currentTimeMillis() + "";
 		
 		String ss = new String("abcd");
 		String path = Util.saveSerializableToPath(getContext(), tmpDir, "abc.ser", ss);
 		assertNotNull(path);
 		assertTrue(new File(path).exists());
 		
 		List<String> files = Util.listFiles(getContext(), tmpDir);
 		assertEquals(1, files.size());
 		assertEquals(files.get(0), path);
 		
 		String ssResult = (String) Util.loadSerializable(path);
 		assertEquals(ssResult, ss);
 		
 		File f = new File(path);
 		f.delete();
 	}
 	
 	public void testSaveAndLoadString()
 	{
 		String tmpDir = System.currentTimeMillis() + "";
 		
 		String ss = new String("abcd");
 		String path = Util.saveDataToFile(getContext(), tmpDir, "abc.dat", ss.getBytes());
 		assertNotNull(path);
 		assertTrue(new File(path).exists());
 		
		String path2 = Util.saveDataToFile(getContext(), tmpDir, "cd.dat", ss.getBytes());
		assertNotNull(path2);
		assertTrue(new File(path2).exists());
		
 		List<String> files = Util.listFiles(getContext(), tmpDir);
		assertEquals(2, files.size());
 		assertEquals(files.get(0), path);
		assertEquals(files.get(1), path2);
 		
 		String ssResult = new String(Util.loadData(path));
 		assertEquals(ssResult, ss);
 		
 		File f = new File(path);
 		f.delete();
 	}
 }
