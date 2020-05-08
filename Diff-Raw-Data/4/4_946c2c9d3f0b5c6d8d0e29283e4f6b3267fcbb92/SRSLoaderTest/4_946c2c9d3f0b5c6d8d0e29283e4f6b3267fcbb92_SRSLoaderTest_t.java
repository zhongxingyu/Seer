 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import gda.analysis.io.ScanFileHolderException;
 
 import java.util.Collection;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 
 /**
  * Test for SRS Loader
  */
 public class SRSLoaderTest {
 
 	/**
 	 * Test Loader
 	 */
 	public SRSLoaderTest() {
 
 	}
 
 	/**
 	 * This method tests for SciSoft trac #496
 	 */
 	@Test
 	public void testSS49() {
 		try {
 			DataHolder dh;
 			String testfile1 = "testfiles/gda/analysis/io/SRSLoaderTest/96356.dat";
 			dh = new SRSLoader(testfile1).loadFile();
 
 			// now the file is loaded, check to make sure that it holds the right data
 			assertEquals("There is not the correct number of axis in the file", 7, dh.size());
 			int dt = dh.getDataset(6).getDtype();
 			if (dt == AbstractDataset.FLOAT32)
 				assertEquals("The file does not contain NANs", Float.NaN, dh.getDataset(6).getDouble(1), 10.);
 			if (dt == AbstractDataset.FLOAT64)
 				assertEquals("The file does not contain NANs", Double.NaN, dh.getDataset(6).getDouble(1), 10.);
 			assertEquals("The file does not contain data as well", 0.1, dh.getDataset(0).getDouble(1), 1.);
 		} catch (ScanFileHolderException e) {
 			fail("Couldn't load the file");
 		}
 
 	}
 	
 	@Test
 	public void testSRSLoaderLoop() throws Exception {
 		
 		DataHolder dh;
 		String testfile1 = "testfiles/gda/analysis/io/SRSLoaderTest/optics_april20110402.dat";
 		// Dodgy old spec file, SRSLoader should reject it!
 		
 		dh = new SRSLoader(testfile1).loadFile();
 		
 		final int size = dh.getList().size();
 		if (size!=0) fail("Test file optics_april20110402.dat cannot be parsed!");
 	}
 
 	@Test
 	public void testLoaderFactory() throws Exception {
 		DataHolder dh = LoaderFactory.getData("testfiles/gda/analysis/io/SRSLoaderTest/96356.dat", null);
         if (dh==null || dh.getNames().length<1) throw new Exception();
 		assertEquals("There is not the correct number of axis in the file", 7, dh.size());
 		int dt = dh.getDataset(6).getDtype();
 		if (dt == AbstractDataset.FLOAT32)
 			assertEquals("The file does not contain NANs", Float.NaN, dh.getDataset(6).getDouble(1), 10.);
 		if (dt == AbstractDataset.FLOAT64)
 			assertEquals("The file does not contain NANs", Double.NaN, dh.getDataset(6).getDouble(1), 10.);
 		assertEquals("The file does not contain data as well", 0.1, dh.getDataset(0).getDouble(1), 1.);
 	}
 	
 	private DataHolder testStoringString(String testfile, boolean storeStrings) throws ScanFileHolderException{
 		DataHolder dh;
 		SRSLoader srsLoader = new SRSLoader(testfile);
 		srsLoader.setStoreStringValues(storeStrings);
 		dh = srsLoader.loadFile();
 		return dh;
 	}
 
 	private DataHolder testStoringStringNotValidImages(boolean storeStrings) throws ScanFileHolderException{
 		return testStoringString("testfiles/gda/analysis/io/SRSLoaderTest/testStoringStringNotValidImages.dat", storeStrings);
 	}
 
 	private DataHolder testStoringStringValidImages(boolean storeStrings) throws ScanFileHolderException{
 		return testStoringString("testfiles/gda/analysis/io/SRSLoaderTest/testStoringStringValidImages.dat", storeStrings);
 	}
 	
 	@Test
 	public void testStoringStringFalseNotValidImages() throws ScanFileHolderException {
 		DataHolder dh = testStoringStringNotValidImages(false);
 		ILazyDataset dataset = dh.getLazyDataset("filename");
 		assertNull(dataset);
 	}
 
 	@Test
 	public void testStoringStringTrueNotValidImages() throws ScanFileHolderException {
 		DataHolder dh = testStoringStringNotValidImages(true);
 		IDataset dataset = (IDataset)dh.getLazyDataset("filename");
 		assertNotNull(dataset);
 		assertEquals(3, dataset.getSize());
 		assertEquals(1, (dataset.getShape()).length);
 		assertEquals(3, (dataset.getShape())[0]);
 		assertEquals( "testfiles/gda/analysis/io/SRSLoaderTest/NotValidImage3.tif", dataset.getObject(2));
 	}
 	
 	@Test
 	public void testStoringStringFalseValidImages() throws ScanFileHolderException {
 		DataHolder dh = testStoringStringValidImages(false);
 		{
 			ILazyDataset dataset = dh.getLazyDataset("filename");
 			assertNull(dataset);
 		}
 		ILazyDataset dataset_image = dh.getLazyDataset("filename_image");
 		assertNotNull(dataset_image);
 		assertEquals(3, (dataset_image.getShape()).length);
 		assertEquals(3, (dataset_image.getShape())[0]);
 		{
 			//take slice from first file
 			IDataset slice = dataset_image.getSlice( new int[]{0,0,0}, new int[]{1,195,1475}, new int[]{1,1,1});
 			slice.squeeze();
 			int[] shape = slice.getShape();
 			shape.toString();
 		}
 		{
 			//take slice from second file - which is of a different size.
 			IDataset slice = dataset_image.getSlice( new int[]{1,0,0}, new int[]{2,195,1024}, new int[]{1,1,1});
 			slice.squeeze();
 			int[] shape = slice.getShape();
 			shape.toString();
 		}
 	}
 
 	@Test
 	public void testStoringStringTrueValidImages() throws ScanFileHolderException {
 		DataHolder dh = testStoringStringValidImages(true);
 		IDataset dataset = (IDataset)dh.getLazyDataset("filename");
 		assertNotNull(dataset);
 		assertEquals(3, dataset.getSize());
 		assertEquals(1, (dataset.getShape()).length);
 		assertEquals(3, (dataset.getShape())[0]);
 		assertEquals( "testfiles/gda/analysis/io/SRSLoaderTest/simple_data.dat", dataset.getObject(2));
 	}
 
 	
 	@Test
 	public void testWhiteSpaceBeforeNumberss() throws ScanFileHolderException {
 		DataHolder dh;
 		String testfile1 = "testfiles/gda/analysis/io/SRSLoaderTest/16446.dat";
 		dh = new SRSLoader(testfile1).loadFile();
 
 		assertEquals(4, dh.size());
 		assertEquals("DCMFPitch", dh.getNames()[0]);
 	}
 
 	/**
 	 * This method tests for B16 data's new-fangled metadata
 	 */
 	@Test
 	public void testB16data() {
 		try {
 			DataHolder dh;
 			String testfile1 = "testfiles/gda/analysis/io/SRSLoaderTest/34146.dat";
 			dh = new SRSLoader(testfile1).loadFile();
 
 			// now the file is loaded, check to make sure that it holds the right data
 			assertEquals("There is not the correct number of axis in the file", 2, dh.size());
 			assertEquals("The file does not contain correct data", 2.0, dh.getDataset(0).getDouble(1), 1e-5);
 			IMetaData mdo = dh.getMetadata();
 			try {
 			Collection<String> mdNames = mdo.getMetaNames();
 			assertEquals("Loaded incorrect number of metadata items", 11, mdNames.size());
 			assertTrue("Metadata item missing", mdo.getMetaValue("cmd")!=null);
 			assertTrue("Metadata item missing", mdo.getMetaValue("SRSTIM")!=null);
 			assertEquals("SRS metadata item wrong", "95827", mdo.getMetaValue("SRSTIM"));
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				Assert.fail("Could not find metadata");
 				
 			}
 		} catch (ScanFileHolderException e) {
 			fail("Couldn't load the file");
 		}
 
 	}
 
 }
