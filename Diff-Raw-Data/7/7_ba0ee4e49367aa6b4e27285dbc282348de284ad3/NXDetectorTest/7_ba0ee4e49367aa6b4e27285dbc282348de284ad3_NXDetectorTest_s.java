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
 
 package gda.device.detector;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.anyBoolean;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.matches;
 import static org.mockito.Mockito.inOrder;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import gda.data.nexus.tree.NexusTreeProvider;
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.detector.nxdata.NXDetectorDataAppender;
 import gda.device.detector.nxdetector.AsyncNXCollectionStrategy;
 import gda.device.detector.nxdetector.NXFileWriterPlugin;
 import gda.device.detector.nxdetector.NXPlugin;
 import gda.device.detector.nxdetector.NXPluginBase;
 import gda.jython.ICurrentScanInformationHolder;
 import gda.jython.InterfaceProvider;
 import gda.scan.ScanInformation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InOrder;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class NXDetectorTest {
 
 	private NXDetector det;
 
 	@Mock
 	private AsyncNXCollectionStrategy collectionStrategy;
 
 	@Mock
 	private NXFileWriterPlugin fileWriter;
 
 	@Mock
 	private NXPlugin plugin;
 
 	@Mock
 	private ICurrentScanInformationHolder currentScanInformationHolder;
 
 	private List<NXDetectorDataAppender> appenderList0;
 
 	private List<NXDetectorDataAppender> appenderList1;
 
 	private List<NXDetectorDataAppender> appenderList2;
 
 	@Before
 	public void setUp() {
 		InterfaceProvider.setCurrentScanInformationHolderForTesting(currentScanInformationHolder);
 
 		when(collectionStrategy.getName()).thenReturn("a");
 		when(fileWriter.getName()).thenReturn("b");
 		when(plugin.getName()).thenReturn("c");
 
 		appenderList0 = mockAppenderList(10);
 		appenderList1 = mockAppenderList(10);
 		appenderList2 = mockAppenderList(10);
 
 		det = new NXDetector("nxdet", collectionStrategy,Arrays.asList((NXPluginBase)fileWriter, plugin));
 	}
 
 	private void configureStreamsForSinglePoint() throws InterruptedException, DeviceException {
 		when(collectionStrategy.read(anyInt())).thenReturn(asList(appenderList0.get(0)));
 		when(fileWriter.read(anyInt())).thenReturn(asList(appenderList1.get(0)));
 		when(plugin.read(anyInt())).thenReturn(asList(appenderList2.get(0)));
 	}
 
 	@Test
 	public void testConfiguration() {
 		assertEquals("nxdet", det.getName());
 		assertEquals(collectionStrategy, det.getCollectionStrategy());
 		assertEquals(Arrays.asList((NXPluginBase)fileWriter, plugin), det.getAdditionalPluginList());
 		assertEquals(Arrays.asList(collectionStrategy, fileWriter, plugin), det.getPluginList());
 	}
 
 	@Test
 	public void testGetPluginMap() {
 
 		Map<String, NXPluginBase> expected = new HashMap<String, NXPluginBase>();
 		expected.put("a", collectionStrategy);
 		expected.put("b", fileWriter);
 		expected.put("c", plugin);
 
 		assertEquals(expected, det.getPluginMap());
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testSetCollectionStrategyWithDuplicatePluginName() {
 		when(collectionStrategy.getName()).thenReturn("c");
 		det = new NXDetector();
 		det.setAdditionalPluginList(Arrays.asList((NXPluginBase)fileWriter, plugin));
 		det.setCollectionStrategy(collectionStrategy);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testSetAdditionalPluginListWithDuplicateCollectionStrategyName() {
 		when(collectionStrategy.getName()).thenReturn("b");
 		det = new NXDetector();
 		det.setCollectionStrategy(collectionStrategy);
 		det.setAdditionalPluginList(Arrays.asList((NXPluginBase)fileWriter, plugin));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testSetAdditionalPluginListWithDuplicateNames() {
 		when(plugin.getName()).thenReturn("b");
 		det = new NXDetector();
 		det.setAdditionalPluginList(Arrays.asList((NXPluginBase)fileWriter, plugin));
 	}
 
 	// Scannable
 
 	@Test
 	public void testGetInputNames() {
 		assertArrayEquals(new String[0], det.getInputNames());
 	}
 
 	@Test
 	public void testGetExtraNames() {
 		when(collectionStrategy.getInputStreamNames()).thenReturn(Arrays.asList("a", "b"));
 		when(fileWriter.getInputStreamNames()).thenReturn(Arrays.asList("c"));
 		when(plugin.getInputStreamNames()).thenReturn(Arrays.asList("d", "e"));
 		assertArrayEquals(new String[] { "a", "b", "c", "d", "e" }, det.getExtraNames());
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testGetExtraNamesWithDuplicate() {
 		when(collectionStrategy.getInputStreamNames()).thenReturn(Arrays.asList("a", "b"));
 		when(fileWriter.getInputStreamNames()).thenReturn(Arrays.asList("c"));
 		when(plugin.getInputStreamNames()).thenReturn(Arrays.asList("d", "a"));
 		det.getExtraNames();
 	}
 
 	@Test
 	public void testGetOutputFormat() {
 		when(collectionStrategy.getInputStreamFormats()).thenReturn(Arrays.asList("a", "b"));
 		when(fileWriter.getInputStreamFormats()).thenReturn(Arrays.asList("c"));
 		when(plugin.getInputStreamFormats()).thenReturn(Arrays.asList("d", "e"));
 		assertArrayEquals(new String[] { "a", "b", "c", "d", "e" }, det.getOutputFormat());
 	}
 
 	@Test
 	public void testCreatesOwnFiles() throws DeviceException {
 		assertFalse(det.createsOwnFiles());
 	}
 
 	@Test
 	public void testAtScanStart() throws Exception {
 		ScanInformation scanInfo = mock(ScanInformation.class);
 		when(currentScanInformationHolder.getCurrentScanInformation()).thenReturn(scanInfo );
 		when(collectionStrategy.getNumberImagesPerCollection(3.)).thenReturn(2);
 
 		det.setCollectionTime(3.);
 		det.atScanStart();
 
 		InOrder inOrder = inOrder(collectionStrategy, fileWriter, plugin);
 		inOrder.verify(collectionStrategy).setGenerateCallbacks(anyBoolean()); // ignorese value
 		inOrder.verify(collectionStrategy).prepareForCollection(3., 2, scanInfo);
 		inOrder.verify(fileWriter).prepareForCollection(2, scanInfo);
 		inOrder.verify(plugin).prepareForCollection(2, scanInfo);
 
 	}
 
 	@Test
 	public void testAtScanStartNoCallbacksRequired() throws Exception {
 		det.atScanStart();
 		verify(collectionStrategy).setGenerateCallbacks(false);
 	}
 
 	@Test
 	public void testAtScanStartWithCallbacksRequired() throws Exception {
 
 		when(collectionStrategy.willRequireCallbacks()).thenReturn(false);
 		when(fileWriter.willRequireCallbacks()).thenReturn(true);
 		when(plugin.willRequireCallbacks()).thenReturn(false);
 
 		det.atScanStart();
 
 		verify(collectionStrategy).setGenerateCallbacks(true);
 
 	}
 
 	@Test
 	public void testAtScanLineStart() throws Exception {
 		det.atScanLineStart();
 		InOrder inOrder = inOrder(collectionStrategy, fileWriter, plugin);
 		inOrder.verify(collectionStrategy).prepareForLine();
 		inOrder.verify(fileWriter).prepareForLine();
 		inOrder.verify(plugin).prepareForLine();
 	}
 
 	@Test
 	public void testCollectData() throws Exception {
 		det.collectData();
 		verify(collectionStrategy).collectData();
 	}
 
 	@Test
 	public void testWaitWhileBusy() throws Exception {
 		det.waitWhileBusy();
 		verify(collectionStrategy).waitWhileBusy();
 	}
 
 	@Test
 	public void testPositionCallableTypeWithFileWriterNotReturningFilepaths() throws Exception {
 
 		configureStreamsForSinglePoint();
 		when(fileWriter.appendsFilepathStrings()).thenReturn(false);
 
 		det.atScanStart();
 		NexusTreeProvider data = det.getPositionCallable().call();
 
 		assertTrue(data instanceof NXDetectorData);
 		assertFalse(data instanceof NXDetectorDataWithFilepathForSrs);
 
 	}
 
 	@Test
 	public void testPositionCallableTypeWithFileWriterReturningFilepaths() throws Exception {
 
 		configureStreamsForSinglePoint();
 		when(fileWriter.appendsFilepathStrings()).thenReturn(true);
 
 		det.atScanStart();
 		NexusTreeProvider data = det.getPositionCallable().call();
 
 		assertTrue(data instanceof NXDetectorData);
 		assertTrue(data instanceof NXDetectorDataWithFilepathForSrs);
 
 	}
 
 	@Test
 	public void testPositionCallableAppendingWithSinglePointStream() throws Exception {
 
 		configureStreamsForSinglePoint();
 
 		det.atScanStart();
 		NexusTreeProvider returnedData = det.getPositionCallable().call();
 
 		checkNXDetectorDataGeneration(returnedData, appenderList0.get(0), appenderList1.get(0), appenderList2.get(0));
 
 	}
 
 	@Test
 	public void testReadoutWithSinglePointStream() throws Exception {
 
 		configureStreamsForSinglePoint();
 
 		det.atScanStart();
 		NexusTreeProvider returnedData = det.readout();
 
 		checkNXDetectorDataGeneration(returnedData, appenderList0.get(0), appenderList1.get(0), appenderList2.get(0));
 
 	}
 
 	@Test
 	public void testPositionCallableAppendingWithStreamsReturningChunks() throws Exception {
 
 		when(collectionStrategy.read(anyInt())).thenReturn(appenderList0);
 		when(fileWriter.read(anyInt())).thenReturn(appenderList1);
 		when(plugin.read(anyInt())).thenReturn(appenderList2);
 
 		det.atScanStart();
 
 		List<NexusTreeProvider> dataList = new ArrayList<NexusTreeProvider>();
 		Callable<NexusTreeProvider> callable0 = det.getPositionCallable();
 		dataList.add(callable0.call());
 		Callable<NexusTreeProvider> callable1 = det.getPositionCallable();
 		Callable<NexusTreeProvider> callable2 = det.getPositionCallable();
 		dataList.add(callable1.call());
 		dataList.add(callable2.call());
 		Callable<NexusTreeProvider> callable3 = det.getPositionCallable();
 		Callable<NexusTreeProvider> callable4 = det.getPositionCallable();
 		Callable<NexusTreeProvider> callable5 = det.getPositionCallable();
 		dataList.add(callable3.call());
 		dataList.add(callable4.call());
 		dataList.add(callable5.call());
 		Callable<NexusTreeProvider> callable6 = det.getPositionCallable();
 		dataList.add(callable6.call());
 		Callable<NexusTreeProvider> callable7 = det.getPositionCallable();
 		Callable<NexusTreeProvider> callable8 = det.getPositionCallable();
 		dataList.add(callable7.call());
 		dataList.add(callable8.call());
 		Callable<NexusTreeProvider> callable9 = det.getPositionCallable();
 		dataList.add(callable9.call());
 
 		for (int i = 0; i < dataList.size(); i++) {
 			checkNXDetectorDataGeneration(dataList.get(i), appenderList0.get(i), appenderList1.get(i),
 					appenderList2.get(i));
 		}
 
 	}
 
 	@Test
 	public void testGetStatusIDLE() throws Exception {
 		when(collectionStrategy.getStatus()).thenReturn(Detector.IDLE);
 		assertEquals(Detector.IDLE, det.getStatus());
 	}
 
 	@Test
 	public void testGetStatusBUSY() throws Exception {
 		when(collectionStrategy.getStatus()).thenReturn(Detector.BUSY);
 		assertEquals(Detector.BUSY, det.getStatus());
 	}
 
 	@Test
 	public void testGetStatusFAULT() throws Exception {
 		when(collectionStrategy.getStatus()).thenReturn(Detector.FAULT);
 		assertEquals(Detector.FAULT, det.getStatus());
 	}
 
 	@Test
 	public void testAtScanLineEnd() throws Exception {
 		det.atScanLineEnd();
 		InOrder inOrder = inOrder(collectionStrategy, fileWriter, plugin);
		inOrder.verify(collectionStrategy).completeLine();
 		inOrder.verify(fileWriter).completeLine();
 		inOrder.verify(plugin).completeLine();
 	}
 
 	@Test
 	public void testAtScanEnd() throws Exception {
 		det.atScanEnd();
 		InOrder inOrder = inOrder(collectionStrategy, fileWriter, plugin);
		inOrder.verify(collectionStrategy).completeCollection();
 		inOrder.verify(fileWriter).completeCollection();
 		inOrder.verify(plugin).completeCollection();
 	}
 
 	@Test
 	public void testStop() throws Exception {
 		det.stop();
 		InOrder inOrder = inOrder(collectionStrategy, fileWriter, plugin);
 		inOrder.verify(collectionStrategy).stop();
 		inOrder.verify(fileWriter).stop();
 		inOrder.verify(plugin).stop();
 	}
 
 	@Test
 	public void testAtCommandFailure() throws Exception {
 		det.atCommandFailure();
 		InOrder inOrder = inOrder(collectionStrategy, fileWriter, plugin);
 		inOrder.verify(collectionStrategy).atCommandFailure();
 		inOrder.verify(fileWriter).atCommandFailure();
 		inOrder.verify(plugin).atCommandFailure();
 	}
 
 	private static List<NXDetectorDataAppender> asList(NXDetectorDataAppender... appenders) {
 		return Arrays.asList(appenders);
 	}
 
 	private static List<NXDetectorDataAppender> mockAppenderList(int length) {
 		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
 		for (int i = 0; i < length; i++) {
 			appenders.add(mock(NXDetectorDataAppender.class));
 		}
 		return appenders;
 	}
 
 	static private void checkNXDetectorDataGeneration(NexusTreeProvider returnedData, NXDetectorDataAppender appender0,
 			NXDetectorDataAppender appender1, NXDetectorDataAppender appender2) throws DeviceException {
 
 		ArgumentCaptor<NXDetectorData> dataCaptor = ArgumentCaptor.forClass(NXDetectorData.class);
 
 		InOrder inOrder = inOrder(appender0, appender1, appender2);
 		inOrder.verify(appender0).appendTo(dataCaptor.capture(), matches("nxdet"));
 		inOrder.verify(appender1).appendTo(dataCaptor.getValue(), "nxdet");
 		inOrder.verify(appender2).appendTo(dataCaptor.getValue(), "nxdet");
 
 		assertEquals(returnedData, dataCaptor.getValue());
 
 	}
 
 }
