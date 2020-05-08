 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council
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
 
 package gda.scan;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyDouble;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.inOrder;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import gda.MockFactory;
 import gda.TestHelpers;
 import gda.configuration.properties.LocalProperties;
 import gda.data.scan.datawriter.DataWriter;
 import gda.data.scan.datawriter.DefaultDataWriterFactory;
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.detector.DetectorWithReadout;
 import gda.device.scannable.DummyScannable;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.concurrent.Callable;
 
 import junitx.framework.FileAssert;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.mockito.InOrder;
 
 /**
  * Class to test writing of nexus files during a scan
  */
 public class ConcurrentScanTest {
 
 	interface DetectorWithReadoutDetector extends Detector, DetectorWithReadout{}
 	static String testScratchDirectoryName;
 
 	protected Scannable lev4;
 	protected Scannable lev5a;
 	protected Scannable lev5b;
 	protected Scannable lev6;
 	protected Scannable lev6b;
 
 	protected DetectorWithReadoutDetector detlev9a;
 	protected DetectorWithReadoutDetector detlev9b;
 	protected DetectorWithReadoutDetector detlev5;
 
 	/**
 	 * Setups of environment for the tests
 	 * 
 	 * @throws Exception
 	 *             if setup fails
 	 */
 	@Before
 	public void setUp() throws Exception {
 		lev4 = MockFactory.createMockScannable("lev4", 4);
 		lev5a = MockFactory.createMockScannable("lev5a", 5);
 		lev5b = MockFactory.createMockScannable("lev5b", 5);
 		lev6 = MockFactory.createMockScannable("lev6", 6);
 		lev6b = MockFactory.createMockScannable("lev6b", 6);
 		detlev9a = createDetector("detlev9a", 9);
 		detlev9b = createDetector("detlev9b", 9);
 		detlev5 = createDetector("detlev5", 5);
 
 		setLocalProperties();
 	}
 
 	/**
 	 * Verify the appropriate bits on a scannable are called in a scan for command: scan smoved 0 10 1 sread
 	 * 
 	 * @throws InterruptedException
 	 * @throws Exception
 	 */
 	@Test
 	public void testScan() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testScan", true);
 		setLocalProperties();
 
 		Scannable smoved = MockFactory.createMockScannable("smoved");
 		Scannable sread = MockFactory.createMockScannable("sread");
 
 		Object[] args = new Object[] { smoved, 0., 10., 1., sread, detlev9a, 2. };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 
 		verify(smoved, times(11)).getPosition();
 		verify(smoved, times(11)).atPointStart();
 		verify(smoved, times(11)).atPointEnd();
 		verify(smoved, times(1)).atScanLineStart();
 		verify(smoved, times(1)).atScanEnd();
 		verify(smoved, times(1)).atScanLineStart();
 		verify(smoved, times(1)).atScanLineEnd();
 
 		verify(sread, times(11)).getPosition();
 		verify(sread, never()).asynchronousMoveTo(anyObject());
 		verify(sread, never()).asynchronousMoveTo(anyObject());
 		verify(sread, times(11)).atPointStart();
 		verify(sread, times(11)).atPointEnd();
 		verify(sread, times(1)).atScanLineStart();
 		verify(sread, times(1)).atScanEnd();
 		verify(sread, times(1)).atScanLineStart();
 		verify(sread, times(1)).atScanLineEnd();
 
 		verify(detlev9a, times(11)).readout();
 		verify(detlev9a, never()).asynchronousMoveTo(anyObject());
 		verify(detlev9a, never()).asynchronousMoveTo(anyObject());
 		verify(detlev9a, times(11)).atPointStart();
 		verify(detlev9a, times(11)).atPointEnd();
 		verify(detlev9a, times(1)).atScanLineStart();
 		verify(detlev9a, times(1)).atScanEnd();
 		verify(detlev9a, times(1)).atScanLineStart();
 		verify(detlev9a, times(1)).atScanLineEnd();
 
 		InOrder inOrder = inOrder(smoved);
 		inOrder.verify(smoved).asynchronousMoveTo(0.);
 		inOrder.verify(smoved).asynchronousMoveTo(1.);
 		inOrder.verify(smoved).asynchronousMoveTo(2.);
 		inOrder.verify(smoved).asynchronousMoveTo(3.);
 		inOrder.verify(smoved).asynchronousMoveTo(4.);
 		inOrder.verify(smoved).asynchronousMoveTo(5.);
 		inOrder.verify(smoved).asynchronousMoveTo(6.);
 		inOrder.verify(smoved).asynchronousMoveTo(7.);
 		inOrder.verify(smoved).asynchronousMoveTo(8.);
 		inOrder.verify(smoved).asynchronousMoveTo(9.);
 		inOrder.verify(smoved).asynchronousMoveTo(10.);
 	}
 
 	protected void setLocalProperties() {
 		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");
 		LocalProperties.set(LocalProperties.GDA_SCAN_CONCURRENTSCAN_READOUT_CONCURRENTLY, "false"); // default as interpreted by ConcurrentScan
 	}
 
 	protected void verifyBigScanAtScanStart(InOrder inOrder) throws DeviceException {
 		inOrder.verify(lev4).atScanStart();
 		inOrder.verify(lev5a).atScanStart();
 		inOrder.verify(lev5b).atScanStart();
 		inOrder.verify(lev6).atScanStart();
 		inOrder.verify(lev6b).atScanStart();
 		inOrder.verify(detlev9a).atScanStart(); // TODO: honour detector level for: atScanStart()?
 		inOrder.verify(detlev9b).atScanStart();
 		inOrder.verify(detlev5).atScanStart();
 	}
 
 	protected void verifyBigScanAtScanLineStart(InOrder inOrder) throws DeviceException {
 		inOrder.verify(lev4).atScanLineStart();
 		inOrder.verify(lev5a).atScanLineStart();
 		inOrder.verify(lev5b).atScanLineStart();
 		inOrder.verify(lev6).atScanLineStart();
 		inOrder.verify(lev6b).atScanLineStart();
 		inOrder.verify(detlev9a).atScanLineStart();
 		inOrder.verify(detlev9b).atScanLineStart();
 		inOrder.verify(detlev5).atScanLineStart();
 	}
 
 	protected void verifyBigScanAtPointStart(InOrder inOrder) throws DeviceException {
 		inOrder.verify(lev4).atPointStart();
 		inOrder.verify(lev5a).atPointStart();
 		inOrder.verify(lev5b).atPointStart();
 		inOrder.verify(lev6).atPointStart();
 		inOrder.verify(lev6b).atPointStart();
 		inOrder.verify(detlev9a).atPointStart();// TODO: honour detector level for: atPointStart()?
 		inOrder.verify(detlev9b).atPointStart();
 		inOrder.verify(detlev5).atPointStart();
 	}
 
 	protected void verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(InOrder inOrder, double pos5a, double pos5b, double pos6)
 			throws DeviceException, InterruptedException {
 		// Note: technically we don't care about the order of these pairs
 		inOrder.verify(lev5a).atLevelMoveStart();
 		inOrder.verify(lev5b).atLevelMoveStart();
 		inOrder.verify(detlev5).waitForReadoutCompletion();
 		inOrder.verify(lev5a).asynchronousMoveTo(pos5a);
 		inOrder.verify(lev5b).asynchronousMoveTo(pos5b);
 		inOrder.verify(detlev5).collectData();
 		inOrder.verify(lev5a).waitWhileBusy();
 		inOrder.verify(lev5b).waitWhileBusy();
 		inOrder.verify(detlev5).waitWhileBusy();
 		inOrder.verify(lev6).atLevelMoveStart();
 		inOrder.verify(lev6).asynchronousMoveTo(pos6);
 		inOrder.verify(lev6).waitWhileBusy();
 		inOrder.verify(lev6b).waitWhileBusy();
 		inOrder.verify(detlev9a).waitForReadoutCompletion();
 		inOrder.verify(detlev9b).waitForReadoutCompletion();
 		inOrder.verify(detlev9a).collectData();
 		inOrder.verify(detlev9b).collectData();
 		inOrder.verify(detlev9a).waitWhileBusy();
 		inOrder.verify(detlev9b).waitWhileBusy();
 		
 	}
 	protected void verifyBigScanMoveLevel4Scannables(InOrder inOrder, double pos4)
 			throws DeviceException, InterruptedException {
 		inOrder.verify(lev4).atLevelMoveStart();
 		inOrder.verify(lev4).asynchronousMoveTo(pos4);
 		inOrder.verify(lev4).waitWhileBusy();
 	}
 
 	protected void verifyBigScanGetPosition(InOrder inOrder) throws DeviceException {
 		inOrder.verify(lev4).getPosition();
 		inOrder.verify(lev5a).getPosition();
 		inOrder.verify(lev5b).getPosition();
 		inOrder.verify(lev6).getPosition();
 		inOrder.verify(lev6b).getPosition();
 	}
 	
 	protected void verifyBigScanReadout(InOrder inOrder) throws DeviceException {
 		inOrder.verify(detlev9a).readout();
 		inOrder.verify(detlev9b).readout();
 		inOrder.verify(detlev5).readout();
 	}
 
 	protected void verifyBigScanAtPointEnd(InOrder inOrder) throws DeviceException {
 		inOrder.verify(lev4).atPointEnd();
 		inOrder.verify(lev5a).atPointEnd();
 		inOrder.verify(lev5b).atPointEnd();
 		inOrder.verify(lev6).atPointEnd();
 		inOrder.verify(lev6b).atPointEnd();
 		inOrder.verify(detlev9a).atPointEnd();// TODO: honour detector level for: atPointStart()?
 		inOrder.verify(detlev9b).atPointEnd();
 		inOrder.verify(detlev5).atPointEnd();
 	}
 
 	protected void verifyBigScanAtScanLineEnd(InOrder inOrder) throws DeviceException {
 		inOrder.verify(lev4).atScanLineEnd();
 		inOrder.verify(lev5a).atScanLineEnd();
 		inOrder.verify(lev5b).atScanLineEnd();
 		inOrder.verify(lev6).atScanLineEnd();
 		inOrder.verify(lev6b).atScanLineEnd();
 		inOrder.verify(detlev9a).atScanLineEnd(); // TODO: honour detector level for: atScanLineEnd()?
 		inOrder.verify(detlev9b).atScanLineEnd();
 		inOrder.verify(detlev5).atScanLineEnd();
 	}
 
 	protected void verifyBigScanAtScanEnd(InOrder inOrder) throws DeviceException {
 		inOrder.verify(lev4).atScanEnd();
 		inOrder.verify(lev5a).atScanEnd();
 		inOrder.verify(lev5b).atScanEnd();
 		inOrder.verify(lev6).atScanEnd();
 		inOrder.verify(lev6b).atScanEnd();
 		inOrder.verify(detlev9a).atScanEnd(); // TODO: honour detector level for: atScanEnd()?
 		inOrder.verify(detlev9b).atScanEnd();
 		inOrder.verify(detlev5).atScanEnd();
 	}
 	
 	protected InOrder runBigScan() throws InterruptedException, Exception {
 		setLocalProperties();
 		new ConcurrentScan(
 				new Object[] { lev4, 0., 1., 1., lev5a, 1., lev5b, 2., lev6, 3., lev6b, detlev9a, 2., detlev9b, detlev5, 2.5 })
 				.runScan();
 		return inOrder(lev4, lev5a, lev5b, lev6, lev6b, detlev9a, detlev9b, detlev5);
 	}
 
 	protected InOrder runBigTwoDimensionScan() throws InterruptedException, Exception {
 		setLocalProperties();
 		new ConcurrentScan(new Object[] { lev4, 0., 1., 1., lev5a, 10., 11., 1., lev5b, 2., lev6, 3., lev6b, detlev9a, 2., detlev9b,
 				detlev5, 2.5 }).runScan();
 		return inOrder(lev4, lev5a, lev5b, lev6, lev6b, detlev9a, detlev9b, detlev5);
 	}
 
 	@Test
 	public void testBigScanAtScanStartAndEndAndPrepareForCollection() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(),
 				"testBigScanAtScanStartAndEndAndPrepareForCollection", true);
 		InOrder inOrder = runBigScan();
 
 		inOrder.verify(detlev9a).prepareForCollection();
 		inOrder.verify(detlev9b).prepareForCollection();
 		inOrder.verify(detlev5).prepareForCollection();
 		verifyBigScanAtScanStart(inOrder);
 		verifyBigScanAtScanEnd(inOrder);
 	}
 
 	@Test
 	public void testBigScanAtScanLineStartAndEnd() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testBigScanAtScanLineStartAndEnd",
 				true);
 		InOrder inOrder = runBigScan();
 
 		verifyBigScanAtScanLineStart(inOrder);
 		verifyBigScanAtScanLineEnd(inOrder);
 	}
 
 	@Test
 	public void testBigScanAtPointStartAndEnd() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testBigScanAtPointStartAndEnd",
 				true);
 		InOrder inOrder = runBigScan();
 
 		verifyBigScanAtPointStart(inOrder);
 		verifyBigScanAtPointEnd(inOrder);
 
 		verifyBigScanAtPointStart(inOrder);
 		verifyBigScanAtPointEnd(inOrder);
 	}
 
 	@Test
 	public void testBigScanLevelConcurrency() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testBigScanLevelConcurrency", true);
 		InOrder inOrder = runBigScan();
 
 		verifyBigScanMoveLevel4Scannables(inOrder, 0.);
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  1., 2., 3.);
 		verifyBigScanMoveLevel4Scannables(inOrder, 1.);
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  1., 2., 3.);
 
 	}
 
 	@Test
 	public void testBigScanLevelReadoutAndGetPosition() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(),
 				"testBigScanLevelReadoutAndGetPosition", true);
 		InOrder inOrder = runBigScan();
 
 		verifyBigScanGetPosition(inOrder);
 		verifyBigScanReadout(inOrder);
 		verifyBigScanGetPosition(inOrder);
 		verifyBigScanReadout(inOrder);
 
 	}
 
 	@Test
 	public void testBigScanWholeMalarchy() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testBigScanWholeMalarchy", true);
 		InOrder inOrder = runBigScan();
 
 		inOrder.verify(detlev9a).setCollectionTime(2.0); // TODO: Called in the constructor !!!???
 		inOrder.verify(detlev5).setCollectionTime(2.5); // TODO: Called in the constructor !!!???
 
 		inOrder.verify(detlev9a).prepareForCollection();
 		inOrder.verify(detlev9b).prepareForCollection();
 		inOrder.verify(detlev5).prepareForCollection();
 		verifyBigScanAtScanStart(inOrder);
 
 		verifyBigScanAtScanLineStart(inOrder);
 
 		verifyBigScanAtPointStart(inOrder);
 		verifyBigScanMoveLevel4Scannables(inOrder, 0.);
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  1., 2., 3.);
 
 		verifyBigScanGetPosition(inOrder);
 		verifyBigScanReadout(inOrder);
 		verifyBigScanAtPointEnd(inOrder);
 
 		verifyBigScanAtPointStart(inOrder);
 		verifyBigScanMoveLevel4Scannables(inOrder, 1.);
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  1., 2., 3.);
 		verifyBigScanGetPosition(inOrder);
 		verifyBigScanReadout(inOrder);
 		verifyBigScanAtPointEnd(inOrder);
 
 		verifyBigScanAtScanLineEnd(inOrder);
 
 		verifyBigScanAtScanEnd(inOrder);
 
 		inOrder.verify(detlev9a).endCollection();
 		inOrder.verify(detlev5).endCollection();
 
 		verify(detlev9a, times(1)).setCollectionTime(anyDouble());
 		verify(detlev9b, never()).setCollectionTime(anyDouble());
 		verify(detlev5, times(1)).setCollectionTime(anyDouble());
 		verify(detlev9a, times(1)).prepareForCollection();
 		verify(detlev9b, times(1)).prepareForCollection();
 		verify(detlev5, times(1)).prepareForCollection();
 		verify(detlev9a, times(1)).endCollection();
 		verify(detlev9b, times(1)).endCollection();
 		verify(detlev5, times(1)).endCollection();
 
 		verify(lev4, times(2)).atPointStart();
 		verify(lev5a, times(2)).atPointStart();
 		verify(lev5b, times(2)).atPointStart();
 		verify(lev6, times(2)).atPointStart();
 		verify(lev6b, times(2)).atPointStart();
 		verify(detlev9a, times(2)).atPointStart();
 		verify(detlev9b, times(2)).atPointStart();
 		verify(detlev5, times(2)).atPointStart();
 		verify(lev4, times(2)).atPointEnd();
 		verify(lev5a, times(2)).atPointEnd();
 		verify(lev5b, times(2)).atPointEnd();
 		verify(lev6, times(2)).atPointEnd();
 		verify(lev6b, times(2)).atPointEnd();
 		verify(detlev9a, times(2)).atPointEnd();
 		verify(detlev9b, times(2)).atPointEnd();
 		verify(detlev5, times(2)).atPointEnd();
 		
 	}
 
 	@Test
 	public void testBigTwoDimensionalScanWholeMalarchy() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testBigTwoDimensionalScanWholeMalarchy", true);
 
 		InOrder inOrder = runBigTwoDimensionScan();
 		// lev4, 0., 1., 1., lev5aa, 10., 11., 1., lev5a, 1., lev5b, 2., lev6, 3., detlev9a, 2.
 
 		inOrder.verify(detlev9a).setCollectionTime(2.0); // TODO: Called in the constructor !!!???
 		inOrder.verify(detlev5).setCollectionTime(2.5); // TODO: Called in the constructor !!!???
 
 		inOrder.verify(detlev9a).prepareForCollection();
 		inOrder.verify(detlev9b).prepareForCollection();
 		inOrder.verify(detlev5).prepareForCollection();
 		verifyBigScanAtScanStart(inOrder);
 
 		// First line lev4 = 0.
 		verifyBigScanMoveLevel4Scannables(inOrder, 0.);
 
 		verifyBigScanAtScanLineStart(inOrder);
 		verifyBigScanAtPointStart(inOrder);
 		
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  10., 2., 3.);
 		verifyBigScanGetPosition(inOrder);
 		verifyBigScanReadout(inOrder);
 		verifyBigScanAtPointEnd(inOrder);
 
 		verifyBigScanAtPointStart(inOrder);
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  11., 2., 3.);
 		verifyBigScanGetPosition(inOrder);
 		verifyBigScanReadout(inOrder);
 		verifyBigScanAtPointEnd(inOrder);
 
 		verifyBigScanAtScanLineEnd(inOrder);
 
 		// Second line lev4 = 1.
 		
 		verifyBigScanMoveLevel4Scannables(inOrder, 1.);
 
 		verifyBigScanAtScanLineStart(inOrder);
 		verifyBigScanAtPointStart(inOrder);
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  10., 2., 3.);
 		verifyBigScanGetPosition(inOrder);
 		verifyBigScanReadout(inOrder);
 		verifyBigScanAtPointEnd(inOrder);
 
 		verifyBigScanAtPointStart(inOrder);
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  11., 2., 3.);
 		verifyBigScanGetPosition(inOrder);
 		verifyBigScanReadout(inOrder);
 		verifyBigScanAtPointEnd(inOrder);
 
 		verifyBigScanAtScanLineEnd(inOrder);
 
 		verifyBigScanAtScanEnd(inOrder);
 
 		inOrder.verify(detlev9a).endCollection();
 		inOrder.verify(detlev9b).endCollection();
 		inOrder.verify(detlev5).endCollection();
 
 		verify(detlev9a, times(1)).setCollectionTime(anyDouble());
 		verify(detlev9b, never()).setCollectionTime(anyDouble());
 		verify(detlev5, times(1)).setCollectionTime(anyDouble());
 		//verify(detlev9a, times(1)).prepareForCollection(); TODO: fails http://jira.diamond.ac.uk/browse/GDA-4635
 		//verify(detlev5, times(1)).prepareForCollection(); TODO: fails http://jira.diamond.ac.uk/browse/GDA-4635
 		verify(detlev9a, times(1)).endCollection();
 		verify(detlev9b, times(1)).endCollection();
 		verify(detlev5, times(1)).endCollection();
 
 	}
 
 	@Test
 	public void testLevelConcurrencyNotAllMoving() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testLevelConcurrencyNotAllMoving",
 				true);
 		setLocalProperties();
 
 		Object[] args = new Object[] { lev4, 0., 1., 1., lev5a, 1., lev6, 3., lev5b, };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 
 		InOrder inOrder;
 
 		// The first point (special case in code)
 		inOrder = inOrder(lev4, lev5a, lev5b, lev6);
 		inOrder.verify(lev4).atLevelMoveStart();
 		inOrder.verify(lev4).asynchronousMoveTo(0.);
 		inOrder.verify(lev4).waitWhileBusy();
 		// Note: technically we don't care about the order of these pairs
 		inOrder.verify(lev5a).atLevelMoveStart();
 		inOrder.verify(lev5a).asynchronousMoveTo(1.);
 		inOrder.verify(lev5a).waitWhileBusy();
 		inOrder.verify(lev5b).waitWhileBusy();
 		inOrder.verify(lev6).atLevelMoveStart();
 		inOrder.verify(lev6).asynchronousMoveTo(3.);
 		inOrder.verify(lev6).waitWhileBusy();
 		inOrder.verify(lev6).getPosition();
 
 		// The second point (illustrative of all later points)
 		inOrder.verify(lev4).atLevelMoveStart();
 		inOrder.verify(lev4).asynchronousMoveTo(1.);
 		inOrder.verify(lev4).waitWhileBusy();
 		inOrder.verify(lev5a).atLevelMoveStart();
 		inOrder.verify(lev5a).asynchronousMoveTo(1.);
 		inOrder.verify(lev5a).waitWhileBusy();
 		inOrder.verify(lev5b).waitWhileBusy();
 		inOrder.verify(lev6).atLevelMoveStart();
 		inOrder.verify(lev6).asynchronousMoveTo(3.);
 		inOrder.verify(lev6).waitWhileBusy();
 		inOrder.verify(lev4).getPosition();
 
 		verify(lev5b, never()).atLevelMoveStart();
 		verify(lev4, times(2)).atLevelMoveStart();
 		verify(lev5a, times(2)).atLevelMoveStart();
 		verify(lev6, times(2)).atLevelMoveStart();
 	}
 
 	/**
 	 * Tests that lower/different level Scannables do not interfere with a multiple scans
 	 */
 	@Test
 	public void testAllOrderingInMultipleScans() throws Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testAllOrderingInMultipleScans",
 				true);
 		setLocalProperties();
 
 		Object[] args = new Object[] { lev6, 0., 1., 1., lev5b, 10., 12., 2., lev4, 30., lev5a };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 		InOrder inOrder = inOrder(lev4, lev5a, lev5b, lev6);
 
 		// start the overall scan
 		inOrder.verify(lev6).atScanStart();
 		inOrder.verify(lev5b).atScanStart();
 		inOrder.verify(lev4).atScanStart();
 		inOrder.verify(lev5a).atScanStart();
 
 		// first point, outer loop
 		inOrder.verify(lev6).atLevelMoveStart();
 		inOrder.verify(lev6).asynchronousMoveTo(0.);
 		inOrder.verify(lev6).waitWhileBusy();
 
 		// First point, inner loop
 		inOrder.verify(lev6).atScanLineStart();
 		inOrder.verify(lev5b).atScanLineStart();
 		inOrder.verify(lev4).atScanLineStart();
 		inOrder.verify(lev5a).atScanLineStart();
 		inOrder.verify(lev4).atLevelMoveStart();
 		inOrder.verify(lev4).asynchronousMoveTo(30.);
 		inOrder.verify(lev4).waitWhileBusy();
 		inOrder.verify(lev5b).atLevelMoveStart();
 		inOrder.verify(lev5b).asynchronousMoveTo(10.);
 		inOrder.verify(lev5b).waitWhileBusy();
 		inOrder.verify(lev5a).waitWhileBusy();
 
 		// second point, inner loop
 		inOrder.verify(lev4).atLevelMoveStart();
 		inOrder.verify(lev4).asynchronousMoveTo(30.);
 		inOrder.verify(lev4).waitWhileBusy();
 		inOrder.verify(lev5b).atLevelMoveStart();
 		inOrder.verify(lev5b).asynchronousMoveTo(12.);
 		inOrder.verify(lev5b).waitWhileBusy();
 		inOrder.verify(lev5a).waitWhileBusy();
 
 		// finish inner loop
 		inOrder.verify(lev6).atScanLineEnd();
 		inOrder.verify(lev5b).atScanLineEnd();
 		inOrder.verify(lev4).atScanLineEnd();
 		inOrder.verify(lev5a).atScanLineEnd();
 
 		// next point of the outer loop
 		inOrder.verify(lev6).atLevelMoveStart();
 		inOrder.verify(lev6).asynchronousMoveTo(1.);
 		inOrder.verify(lev6).waitWhileBusy();
 
 		// First point, inner loop
 		inOrder.verify(lev6).atScanLineStart();
 		inOrder.verify(lev5b).atScanLineStart();
 		inOrder.verify(lev4).atScanLineStart();
 		inOrder.verify(lev5a).atScanLineStart();
 		inOrder.verify(lev4).atLevelMoveStart();
 		inOrder.verify(lev4).asynchronousMoveTo(30.);
 		inOrder.verify(lev4).waitWhileBusy();
 		inOrder.verify(lev5b).atLevelMoveStart();
 		inOrder.verify(lev5b).asynchronousMoveTo(10.);
 		inOrder.verify(lev5b).waitWhileBusy();
 		inOrder.verify(lev5a).waitWhileBusy();
 
 		// second point, inner loop
 		inOrder.verify(lev4).atLevelMoveStart();
 		inOrder.verify(lev4).asynchronousMoveTo(30.);
 		inOrder.verify(lev4).waitWhileBusy();
 		inOrder.verify(lev5b).atLevelMoveStart();
 		inOrder.verify(lev5b).asynchronousMoveTo(12.);
 		inOrder.verify(lev5b).waitWhileBusy();
 		inOrder.verify(lev5a).waitWhileBusy();
 
 		// finish inner loop
 		inOrder.verify(lev6).atScanLineEnd();
 		inOrder.verify(lev5b).atScanLineEnd();
 		inOrder.verify(lev4).atScanLineEnd();
 		inOrder.verify(lev5a).atScanLineEnd();
 
 		// finish outer loop
 		inOrder.verify(lev6).atScanEnd();
 		inOrder.verify(lev5b).atScanEnd();
 		inOrder.verify(lev4).atScanEnd();
 		inOrder.verify(lev5a).atScanEnd();
 
 		// check frequencies
 		verify(lev6, times(1)).atScanStart();
 		verify(lev5a, times(1)).atScanStart();
 		verify(lev5b, times(1)).atScanStart();
 		verify(lev4, times(1)).atScanStart();
 
 		verify(lev6, times(2)).atLevelMoveStart(); // only twice as this is part of the outer scan
 		verify(lev5a, never()).atLevelMoveStart();
 		verify(lev5b, times(4)).atLevelMoveStart();
 		verify(lev4, times(4)).atLevelMoveStart();
 
 		verify(lev6, times(4)).atPointStart();
 		verify(lev5a, times(4)).atPointStart();
 		verify(lev5b, times(4)).atPointStart();
 		verify(lev4, times(4)).atPointStart();
 
 		verify(lev6, times(4)).atPointEnd();
 		verify(lev5a, times(4)).atPointEnd();
 		verify(lev5b, times(4)).atPointEnd();
 		verify(lev4, times(4)).atPointEnd();
 
 		verify(lev6, times(2)).atScanLineStart();
 		verify(lev5a, times(2)).atScanLineStart();
 		verify(lev5b, times(2)).atScanLineStart();
 		verify(lev4, times(2)).atScanLineStart();
 
 		verify(lev6, times(2)).atScanLineEnd();
 		verify(lev5a, times(2)).atScanLineEnd();
 		verify(lev5b, times(2)).atScanLineEnd();
 		verify(lev4, times(2)).atScanLineEnd();
 
 		verify(lev6, times(1)).atScanEnd();
 		verify(lev5a, times(1)).atScanEnd();
 		verify(lev5b, times(1)).atScanEnd();
 		verify(lev4, times(1)).atScanEnd();
 	}
 
 	@Test
 	public void testWithZieScannables() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testWithZieScannables", true);
 		setLocalProperties();
 
 		Scannable zie = MockFactory.createMockZieScannable("zie", 4);
 		Object[] args = new Object[] { lev4, 0., 1., 1., zie };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 
 		InOrder inOrder;
 		inOrder = inOrder(lev4, zie);
 
 		// The first point (special case in code)
 		inOrder.verify(lev4).atLevelMoveStart();
 		inOrder.verify(lev4).asynchronousMoveTo(0.);
 		inOrder.verify(lev4).waitWhileBusy();
 		inOrder.verify(zie).getPosition();
 		inOrder.verify(lev4).getPosition();
 
 		// The second point (illustrative of all later points)
 		inOrder.verify(lev4).atLevelMoveStart();
 		inOrder.verify(lev4).asynchronousMoveTo(1.);
 		inOrder.verify(lev4).waitWhileBusy();
 		inOrder.verify(zie).getPosition();
 		inOrder.verify(lev4).getPosition();
 
 		verify(lev4, times(2)).atLevelMoveStart();
 		verify(zie, never()).atLevelMoveStart();
 		verify(zie, never()).asynchronousMoveTo(anyObject());
 	}
 
 	@Test
 	public void testFailsWithZieScannableThatReturnsSomething() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(),
 				"testFailsWithZieScannableThatReturnsSomething", true);
 		setLocalProperties();
 
 		Scannable zie = MockFactory.createMockZieScannable("zie", 4);
 		Object[] args = new Object[] { lev4, 0., 1., 1., zie };
 		when(zie.getPosition()).thenReturn("should_have_returned_null");
 		ConcurrentScan scan = new ConcurrentScan(args);
 
 		try {
 			scan.runScan();
 			Assert.fail("Exception expected");
 		} catch (Exception e) {
 			Assert.assertEquals(
					"Exception during scan collection: DeviceException: Scannable zie has no input or extra names defined. Its getPosition method should return null/None but returned: 'should_have_returned_null'.",
 					e.getMessage());
 		}
 	}
 
 	@Test
 	public void testLevelConcurrencyAcrossLoops() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testLevelConcurrencyAcrossLoops",
 				true);
 		setLocalProperties();
 
 		Object[] args = new Object[] { lev5a, 0., 1., 1., lev5b, 10., lev6, 30. };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 
 		InOrder inOrder = inOrder(lev5a, lev5b, lev6);
 
 		// First point
 		inOrder.verify(lev5a).atLevelMoveStart();
 		inOrder.verify(lev5b).atLevelMoveStart();
 		inOrder.verify(lev5a).asynchronousMoveTo(0.);
 		inOrder.verify(lev5b).asynchronousMoveTo(10.);
 		inOrder.verify(lev5a).waitWhileBusy();
 		inOrder.verify(lev5b).waitWhileBusy();
 		inOrder.verify(lev6).atLevelMoveStart();
 		inOrder.verify(lev6).asynchronousMoveTo(30.);
 
 		// Second point
 		inOrder.verify(lev5a).atLevelMoveStart();
 		inOrder.verify(lev5b).atLevelMoveStart();
 		inOrder.verify(lev5a).asynchronousMoveTo(1.);
 		inOrder.verify(lev5b).asynchronousMoveTo(10.);
 		inOrder.verify(lev5a).waitWhileBusy();
 		inOrder.verify(lev5b).waitWhileBusy();
 		inOrder.verify(lev6).atLevelMoveStart();
 		inOrder.verify(lev6).asynchronousMoveTo(30.);
 
 		verify(lev5a, times(2)).atLevelMoveStart();
 		verify(lev5b, times(2)).atLevelMoveStart();
 		verify(lev6, times(2)).atLevelMoveStart();
 	}
 
 	@Test
 	public void testAtCommandFailureForOkayScan() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testAtCommandFailureForOkayScan",
 				true);
 		setLocalProperties();
 
 		Object[] args = new Object[] { lev4, 0., 1., 1., lev5a, 1., lev5b, 2., lev6, 3. };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 
 		for (Scannable scn : Arrays.asList(lev4, lev5a, lev5b, lev6)) {
 			verify(scn, times(0)).atCommandFailure();
 		}
 	}
 
 	@Test
 	public void testAtCommandFailureForScanException() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(),
 				"testAtCommandFailureForScanException", true);
 		setLocalProperties();
 		Scannable failer = MockFactory.createMockScannable("failer");
 		doThrow(new DeviceException("Planned failure for test")).when(failer).asynchronousMoveTo(anyObject());
 
 		Object[] args = new Object[] { lev4, 0., 1., 1., lev5a, 1., failer, 2., lev6, 3. };
 		ConcurrentScan scan = new ConcurrentScan(args);
 
 		try {
 			scan.runScan();
 			Assert.fail("Exception expected");
 		} catch (Exception e) {
			assertEquals("Exception during scan collection: DeviceException: Planned failure for test", e.getMessage());
 		}
 
 		for (Scannable scn : Arrays.asList(lev4, lev5a, failer, lev6)) {
 			verify(scn, times(1)).atCommandFailure();
 		}
 	}
 
 	@Test
 	public void testNestedConcurrent() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testNestedConcurrent", true);
 		setLocalProperties();
 
 		MultiRegionScan multiRegionScan = new MultiRegionScan();
 		multiRegionScan.addScan(new ConcurrentScan(new Object[] { lev5a, 0., 1.0, 0.1 }));
 		multiRegionScan.addScan(new ConcurrentScan(new Object[] { lev5a, 1.0, 0, 0.1 }));
 		multiRegionScan.runScan();
 	}
 
 	@Test
 	public void testScanPositionProvider() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testScanPositionProvider", true);
 		setLocalProperties();
 
 		ScanPositionProvider positions = ScanPositionProviderFactory.create(new Double[] { 0., 1., 3., 4., 5. });
 
 		Object[] args = new Object[] { lev5a, positions };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		DataWriter writer_spy = spy(DefaultDataWriterFactory.createDataWriterFromFactory());
 		scan.setDataWriter(writer_spy);
 		scan.runScan();
 
 		verify(writer_spy, times(5)).addData(any(ScanDataPoint.class));
 		InOrder inOrder = inOrder(lev5a, writer_spy);
 
 		inOrder.verify(lev5a).asynchronousMoveTo(0.);
 		inOrder.verify(lev5a).asynchronousMoveTo(1.);
 		inOrder.verify(lev5a).asynchronousMoveTo(3.);
 		inOrder.verify(lev5a).asynchronousMoveTo(4.);
 		inOrder.verify(lev5a).asynchronousMoveTo(5.);
 		inOrder.verify(writer_spy).completeCollection();
 	}
 
 	/**
 	 * This test is performed here as the resulting file depends on on ConcurrentScan, DataPoint and SrsDataFile.
 	 */
 	@Test
 	public void testSrsFileWriting() throws Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testSrsFileWriting", true);
 		setLocalProperties();
 		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "SrsDataFile");
 		LocalProperties.set("gda.data.scan.datawriter.dataFormat.SrsDataFile.aligncolumns", "False");
 		Scannable lev7 = MockFactory.createMockScannable("lev7", 7);
 
 		when(lev4.getInputNames()).thenReturn(new String[] { "lev4" });
 		when(lev5a.getInputNames()).thenReturn(new String[] { "lev5a" });
 		when(lev5b.getInputNames()).thenReturn(new String[] { "lev5b_returns_a_string" });
 		when(lev6.getInputNames()).thenReturn(new String[] { "lev6" });
 		when(lev7.getInputNames()).thenReturn(new String[] { "lev7" });
 
 		when(lev4.getOutputFormat()).thenReturn(new String[] { "%f" });
 		when(lev5a.getOutputFormat()).thenReturn(new String[] { "%i" });
 		when(lev5b.getOutputFormat()).thenReturn(new String[] { "%s" });
 		when(lev6.getOutputFormat()).thenReturn(new String[] { "% 5.5" });
 		when(lev7.getOutputFormat()).thenReturn(new String[] { "%-5.5" });
 
 		when(lev4.getPosition()).thenReturn(0.123456789);
 		when(lev5a.getPosition()).thenReturn(1234);
 		when(lev5b.getPosition()).thenReturn("12string34");
 		when(lev6.getPosition()).thenReturn(210.123456789);
 		when(lev7.getPosition()).thenReturn(-210.123456789);
 
 		Object[] args = new Object[] { lev4, 0., 2., 1., lev5a, 1., lev5b, 2., lev6, 3., lev7 };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 		System.out.print(scan.getDataWriter().getCurrentFileName());
 
 		FileAssert.assertEquals(new File("testfiles/gda/scan/ConcurrentScanTest/testSrsFileWriting_expected.dat"),
 				new File(testScratchDirectoryName + "/Data/1.dat"));
 	}
 
 	/**
 	 * This test is performed here as the resulting file depends on on ConcurrentScan, DataPoint and SrsDataFile.
 	 */
 	@Test
 	public void testCounterTimer() throws Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testCounterTimer", true);
 		setLocalProperties();
 		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "SrsDataFile");
 		LocalProperties.set("gda.data.scan.datawriter.dataFormat.SrsDataFile.aligncolumns", "False");
 
 		Scannable simpleScannable2 = TestHelpers.createTestScannable("SimpleScannable2", 0., new String[] {},
 				new String[] { "simpleScannable2" }, 0, new String[] { "%5.2g" }, null);
 
 		Detector ct = createDetector("ct", 9);
 
 		Object[] args = new Object[] { simpleScannable2, 0., 2., 1., ct };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 		System.out.print(scan.getDataWriter().getCurrentFileName());
 
 		verify(ct, never()).setCollectionTime(anyDouble());
 
 		args = new Object[] { simpleScannable2, 0., 2., 1., ct, .1 };
 		scan = new ConcurrentScan(args);
 		scan.runScan();
 		System.out.print(scan.getDataWriter().getCurrentFileName());
 
 		verify(ct).setCollectionTime(.1);
 		verify(ct, never()).asynchronousMoveTo(anyObject());
 
 		FileAssert.assertEquals(new File("testfiles/gda/scan/ConcurrentScanTest/testCounterTimer_expected.dat"),
 				new File(testScratchDirectoryName + "/Data/1.dat"));
 	}
 
 	@Test
 	public void testWithDetectorsNoPosition() throws Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testWithDetectorsNoPosition", true);
 		setLocalProperties();
 
 		new ConcurrentScan(new Object[] { lev4, 0., 2., 1., detlev9a, detlev9b }).runScan();
 
 		verify(detlev9a, never()).setCollectionTime(anyDouble());
 		verify(detlev9a, never()).asynchronousMoveTo(anyObject());
 		verify(detlev9b, never()).setCollectionTime(anyDouble());
 		verify(detlev9b, never()).asynchronousMoveTo(anyObject());
 
 	}
 
 	@Test
 	public void testWithDetectorsSingleStartPosition() throws Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(),
 				"testWithDetectorsSingleStartPosition", true);
 		setLocalProperties();
 
 		Scannable scn = MockFactory.createMockScannable("scn");
 		new ConcurrentScan(new Object[] { scn, 0., 2., 1., detlev9a, .1, detlev9b, .2, detlev5 }).runScan();
 
 		verify(detlev9a).setCollectionTime(.1);
 		verify(detlev9a, never()).asynchronousMoveTo(anyObject());
 		verify(detlev9b).setCollectionTime(.2);
 		verify(detlev9b, never()).asynchronousMoveTo(anyObject());
 		verify(detlev5, never()).setCollectionTime(anyDouble());
 		verify(detlev5, never()).asynchronousMoveTo(anyObject());
 	}
 
 	@Test
 	@Ignore
 	public void testWithDetectorsSingleStartPositionAndScanSetToSetCollectionTimeAtEachStep() throws Exception {
 		// Code not written yet
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(),
 				"testWithDetectorsSingleStartPositionAndScanSetToSetCollectionTimeAtEachStep", true);
 		setLocalProperties();
 
 		new ConcurrentScan(new Object[] { lev4, 0., 2., 1., detlev9a, .1, detlev9b, .2, detlev5 }).runScan();
 
 		verify(detlev9a, times(2)).setCollectionTime(.1);
 		verify(detlev9a, never()).asynchronousMoveTo(anyObject());
 		verify(detlev9b, times(2)).setCollectionTime(.2);
 		verify(detlev9b, never()).asynchronousMoveTo(anyObject());
 		verify(detlev5, never()).setCollectionTime(anyDouble());
 		verify(detlev5, never()).asynchronousMoveTo(anyObject());
 	}
 
 	private DetectorWithReadoutDetector createDetector(String name, int level) throws DeviceException {
 		DetectorWithReadoutDetector det = mock(DetectorWithReadoutDetector.class, name);
 		when(det.getName()).thenReturn(name);
 		when(det.getInputNames()).thenReturn(new String[] {});
 		when(det.getExtraNames()).thenReturn(new String[] { "ct1", "ct2" });
 		when(det.getOutputFormat()).thenReturn(new String[] { "%5.2g", "%5.3g" });
 		when(det.getLevel()).thenReturn(level);
 		when(det.readout()).thenReturn(new Double[] { 1.0, 2.0 });
 		when(det.isBusy()).thenReturn(false);
 		return det;
 	}
 
 	@Test
 	public void testMultielementScannables() throws Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testMultielementScannables", true);
 		setLocalProperties();
 		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "SrsDataFile");
 		LocalProperties.set("gda.data.scan.datawriter.dataFormat.SrsDataFile.aligncolumns", "False");
 
 		Scannable multiScannable = MockFactory.createMockScannable("multi", new String[] { "x", "y", "z" },
 				new String[] {}, new String[] { "%4.1f", "%4.1f", "%4.1f" }, 1, new Double[] { 0., 0., 0. });
 
 		Double[] start = new Double[] { 0., 0., 0. };
 		Double[] stop = new Double[] { 1., 1., 1. };
 		Double[] goodStep = new Double[] { 0.1, 0.1, 0.1 };
 		Double[] zeroStep = new Double[] { 0., 0., 0. };
 		Double[] partialStep = new Double[] { 0.1, 0., 0. };
 		Double[] inconsistentStep = new Double[] { 0.1, 1., 0. };
 
 		// should run
 		Object[] args = new Object[] { multiScannable, start, stop, goodStep };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 
 		// should fail
 		try {
 			args = new Object[] { multiScannable, start, stop, partialStep };
 			scan = new ConcurrentScan(args);
 			scan.runScan();
 			fail("zero step scan should have failed");
 		} catch (Exception e) {
 		}
 
 		// should fail
 		try {
 			args = new Object[] { multiScannable, start, stop, zeroStep };
 			scan = new ConcurrentScan(args);
 			scan.runScan();
 			fail("zero step scan should have failed");
 		} catch (Exception e) {
 		}
 
 		// should also fail
 		try {
 			args = new Object[] { multiScannable, start, stop, inconsistentStep };
 			scan = new ConcurrentScan(args);
 			scan.runScan();
 			fail("inconsistent step scan should have failed");
 		} catch (Exception e) {
 		}
 	}
 
 	@Test
 	public void testImplicitScanObject() {
 
 		try {
 			DummyScannable testScannable = new DummyScannable("test");
 			testScannable.setUpperGdaLimits(50.0);
 
 			ImplicitScanObject iso = new ImplicitScanObject(testScannable, 0, 50., 10.0);
 			iso.calculateScanPoints();
 
 			assertEquals(6, iso.getNumberPoints());
 
 			assertEquals(null, iso.arePointsValid());
 
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 
 	}
 
 	@Test
 	public void testExplicitScanObject() {
 
 		try {
 			DummyScannable testScannable = new DummyScannable("test");
 			testScannable.setUpperGdaLimits(50.0);
 
 			ExplicitScanObject iso = new ExplicitScanObject(testScannable,
 					ScanPositionProviderFactory.create(new Double[] { 0., 10., 20., 30., 40., 50. }));
 			assertEquals(6, iso.getNumberPoints());
 			assertEquals(null, iso.arePointsValid());
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void testRedoScanLine() throws InterruptedException, Exception {
 		setLocalProperties();
 		doThrow(new RedoScanLineThrowable("Beam drop testing Throwable")).doNothing().when(lev6).atScanLineEnd();
 		Object[] args = new Object[] { lev4, 2.0, 3.0, 1.0, lev6, 0., 1., 0.1 };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 		verify(lev4, times(33)).getPosition();
 	}
 
 	@Test
 	public void testConcurrentScanWithWithScannableThetReturnsCallable() throws Exception {
 		// NOTE: Not a PositionCallableProvider so should not be processed in pipeline!
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(),
 				"testConcurrentScanWithWithScannableThetReturnsCallable", true);
 		setLocalProperties();
 
 		Scannable smoved = MockFactory.createMockScannable("smoved");
 		Scannable sread = MockFactory.createMockScannable("sread");
 		class PositionCallable implements Callable<Double> {
 
 			@Override
 			public Double call() throws Exception {
 				return 1.23;
 			}
 
 		}
 		Callable<Double> callable = new PositionCallable();
 
 		Scannable later = MockFactory.createMockScannable("later", new String[] {}, new String[] { "later" },
 				new String[] { "%f" }, 5, callable);
 		// String name, String[] inputNames, String[] extraNames,
 		// String[] outputFormat, int level, Object position)
 
 		Object[] args = new Object[] { smoved, 0., 10., 1., sread, later };
 		ConcurrentScan scan = new ConcurrentScan(args);
 		scan.runScan();
 
 	}
 	
 	@Test
 	public void testReportDevicesByLevel() throws Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(),
 				"testReportDevicesByLevel", true);
 		setLocalProperties();
 		 Scannable mon5 = MockFactory.createMockScannable("mon5", 5);
 		 Scannable mon9 = MockFactory.createMockScannable("mon9", 9);
 		ConcurrentScan scan = new ConcurrentScan(
 				new Object[] { lev4, 0., 1., 1., lev5a, 1., lev5b, 2., lev6, 3., mon5, detlev9a, 2., detlev5, 2.5, mon9});
 		assertEquals("| lev4 | lev5a, lev5b, *detlev5 | lev6 | *detlev9a || mon5, mon9 |", scan.reportDevicesByLevel());
 	}
 	
 
 }
