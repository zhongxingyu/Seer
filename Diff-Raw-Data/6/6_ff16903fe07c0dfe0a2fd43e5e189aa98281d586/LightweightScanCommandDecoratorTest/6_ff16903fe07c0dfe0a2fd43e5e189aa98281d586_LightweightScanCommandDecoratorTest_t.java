 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.sda.navigator.decorator;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.io.IExtendedMetadata;
 
 
 
 public class LightweightScanCommandDecoratorTest {
 	
 	private String srsFileName = System.getProperty("SRSNavigatorTestFile");
 	private String nxsFileName = System.getProperty("NXSNavigatorTestFile");
 	
 	private static final Logger logger = LoggerFactory.getLogger(LightweightScanCommandDecoratorTest.class);
 	
 	@Test
 	public void testSRSMetaDataLoader(){
 		LightweightScanCommandDecorator scd = new LightweightScanCommandDecorator();
 		IExtendedMetadata metaData = scd.srsMyMetaDataLoader(srsFileName);
 		
 		assertEquals(metaData.getScanCommand(),"scan chi 90 -90 -1 Waittime 0.5");
 	}
 	
 	@Test
 	public void testGetHDF5TitleAndScanCmd(){
 		LightweightScanCommandDecorator scd = new LightweightScanCommandDecorator();
 		
 		try {
 			String[][] listTitlesAndScanCmd = scd.getMyHDF5TitleAndScanCmd(nxsFileName);
			assertEquals(listTitlesAndScanCmd[0][0],"");
			assertEquals(listTitlesAndScanCmd[1][0],"\nScanCmd1: [scan DCMFPitch -0.12 0.12 0.0040 counter 1.0 BPM1IN]");
 		} catch (Exception e) {
 			logger.error("Could not load NXS Title/ScanCmd: ", e);
 		}
 		
 	
 	}
 }
