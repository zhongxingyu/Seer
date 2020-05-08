 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.detector.addetector.filewriter;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.inOrder;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import gda.configuration.properties.LocalProperties;
 import gda.data.NumTracker;
 import gda.data.PathConstructor;
 import gda.device.detector.areadetector.v17.NDFile;
 import gda.device.detector.areadetector.v17.NDPluginBase;
 import gda.jython.ICurrentScanInformationHolder;
 import gda.jython.InterfaceProvider;
 import gda.observable.ObservableUtil;
 import gda.scan.ScanInformation;
 import gda.util.TestUtils;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InOrder;
 
 
 // TODO: Tests here are minimal
 
 public class SingleImagePerFileWriterWithNumTrackerTest {
 
 	private NDPluginBase mockNDPluginBase;
 	
 	private NDFile mockNdFile;
 
 	private SingleImagePerFileWriterWithNumTracker writer;
 
 	private void setGdaVarDir(String path) {
 		LocalProperties.set(LocalProperties.GDA_VAR_DIR, path);
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		mockNDPluginBase = mock(NDPluginBase.class);
 		mockNdFile = mock(NDFile.class);
 		when(mockNdFile.getPluginBase()).thenReturn(mockNDPluginBase);
 		when(mockNdFile.filePathExists()).thenReturn(true);
 		when(mockNdFile.isWriteStatusErr()).thenReturn(false);
 		when(mockNdFile.createWriteStatusObservable()).thenReturn(new ObservableUtil<Short>());
 		LocalProperties.set(PathConstructor.getDefaultPropertyName(), "path/to/datadir");
 		configureScanInformationHolder();
 	}
 
 	private void configureScanInformationHolder() {
 		ScanInformation scanInfo = mock(ScanInformation.class);
 		ICurrentScanInformationHolder currentScanHolder = mock(ICurrentScanInformationHolder.class);
 		when(currentScanHolder.getCurrentScanInformation()).thenReturn(scanInfo);
 		when(scanInfo.getScanNumber()).thenReturn((long) 12345);
 		InterfaceProvider.setCurrentScanInformationHolderForTesting(currentScanHolder);
 	}
 	
 	@Test
 	public void testGetFileTemplateDefault() throws Exception {
 		setGdaVarDir(TestUtils.setUpTest(this.getClass(), "testGetFileTemplateDefault", true));
 		createWriter();
 		assertEquals("%s%s%05d.tif", writer.getFileTemplate());
 	}
 
 
 	@Test
 	public void testGetFilePathDefault() throws Exception {
 		setGdaVarDir(TestUtils.setUpTest(this.getClass(), "testGetFilePathDefault", true));
 		createWriter();
 		assertEquals("path/to/datadir/snapped-data/detname", writer.getFilePath());
 	}
 	
 	@Test
 	public void testGetFileNameDefault() throws Exception {
 		setGdaVarDir(TestUtils.setUpTest(this.getClass(), "testGetFileNameDefault", true));
 		createWriter();
 		assertEquals("", writer.getFileName());
 	}
 	
 	@Test
 	public void testPrepareforCollectionAdvancesImageNumber() throws Exception {
 		setGdaVarDir(TestUtils.setUpTest(this.getClass(), "testPrepareforCollectionSetsNextNumberDefault", true));
 		createWriter();
 
 		writer.prepareForCollection(1, null);
		writer.completeCollection();
 		writer.prepareForCollection(1, null);
		writer.completeCollection();
 		writer.prepareForCollection(1, null);
 
 		InOrder inOrder = inOrder(mockNdFile);
 		inOrder.verify(mockNdFile).setFileNumber(1);
 		inOrder.verify(mockNdFile).setFileNumber(2);
 		inOrder.verify(mockNdFile).setFileNumber(3);
 	}
 	@Test
 	public void testPrepareforCollectionIsUsingNumTracker() throws Exception {
 		setGdaVarDir(TestUtils.setUpTest(this.getClass(), "testPrepareforCollectionSetsNextNumberDefault", true));
 		createWriter();
 		NumTracker numTracker = new NumTracker("detname_numtracker");
 
 		numTracker.setFileNumber(100);
 		writer.prepareForCollection(1, null);
		writer.completeCollection();
 		writer.prepareForCollection(1, null);
 
 		InOrder inOrder = inOrder(mockNdFile);
 		inOrder.verify(mockNdFile).setFileNumber(101);
 		inOrder.verify(mockNdFile).setFileNumber(102);
 	}
 
 	private void createWriter() throws Exception {
 		writer = new SingleImagePerFileWriterWithNumTracker("detname");
 		writer.setNdFile(mockNdFile);
 		writer.setNumTrackerExtension("detname_numtracker");
 		writer.afterPropertiesSet();
 	}
 }
