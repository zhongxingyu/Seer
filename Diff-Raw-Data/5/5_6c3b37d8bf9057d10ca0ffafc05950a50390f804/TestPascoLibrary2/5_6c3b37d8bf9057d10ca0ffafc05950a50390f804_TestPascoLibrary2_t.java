 package org.concord.sensor.pasco;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import org.concord.sensor.pasco.datasheet.ByteBufferStreamReversed;
 import org.concord.sensor.pasco.datasheet.PasportSensorDataSheet;
 import org.concord.sensor.pasco.datasheet.Printer;
 import org.concord.sensor.pasco.jna.PascoChannel;
 import org.concord.sensor.pasco.jna.PascoDevice;
 import org.concord.sensor.pasco.jna.PascoException;
 import org.concord.sensor.pasco.jna.PascoLibrary;
 
 
 public class TestPascoLibrary2 {
 	private static final Logger logger = Logger.getLogger(TestPascoLibrary2.class.getCanonicalName());
 
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws PascoException 
 	 */
 	public static void main(String[] args) throws InterruptedException, IOException, PascoException {
 		System.out.println( "Test Pasco USB sensor lib" );
 		
 		PascoLibrary library = new PascoLibrary();
 
 		library.initLibrary();
 		library.init();
 		
 		library.start();
 		
 		PascoDevice[] devices = null;
 		
 		// On a PS-2100
 		//  This takes ~0.5 seconds once the interface is initialized with a PS-2100
 		//  if it is the first time then it takes 2.6 seconds
 		Date start = new Date();
 		for(int i=0; i<50; i++){
 			devices = library.getDevices();
 			Thread.sleep(100);
 			
 			if(devices.length > 0){
 				break;
 			}
 		}
 		System.out.println("Found " + devices.length + " devices after " + 
 				((new Date()).getTime() - start.getTime())*0.001 + "seconds");
 		
 		if(devices == null){
 			System.out.println("Unexepected state");
 			return;
 		}
 		
 		int i=0;
 		for (PascoDevice pascoDevice : devices) {
 			System.out.println("Scanning Device: " + i++);
 			PascoChannel[] channels = pascoDevice.getChannels();
 			PasportSensorDataSheet[] dataSheets = new PasportSensorDataSheet [channels.length];
 			int j=0;
 			int attachedChannels = 0;
 			for (PascoChannel channel : channels) {
 				int channelIdx = j++;
 				System.out.println("  Scanning Channel: " + channelIdx);
 				if(!channel.getExist()) {
 					System.out.println("    no sensor attached");
 					continue;
 				}
 				
 				attachedChannels++;
 				System.out.println("    name: " + channel.getName());
 				System.out.println("    min rate: " + PascoChannel.convertRate(channel.getSampleRateMinimum()) + " s/sample");
 				System.out.println("    max rate: " + PascoChannel.convertRate(channel.getSampleRateMaximum()) + " s/sample");
 				System.out.println("    default rate: " + PascoChannel.convertRate(channel.getSampleRateDefault()) + " s/sample");
 				int sampleSize = channel.getSampleSize();
 				System.out.println("    sample size: " + sampleSize);
 				System.out.println("    datasheet size: " + channel.getSensorDataSheetSize());
 				byte [] dataSheetBuf = new byte[channel.getSensorDataSheetSize()];
 				channel.readSensorDataSheet(dataSheetBuf, dataSheetBuf.length);
 				PasportSensorDataSheet dataSheet = new PasportSensorDataSheet(new ByteBufferStreamReversed(dataSheetBuf, 0, dataSheetBuf.length));
 				dataSheets[channelIdx] = dataSheet;
 				System.out.println("    datasheet:");
 				Printer dsPrinter = new Printer("      ");
 				dataSheet.print(dsPrinter);
 				dsPrinter.printToSysout();
 				// dumpBuffer(dataSheetBuf, dataSheetBuf.length);
 				byte [] sample = new byte[sampleSize];
 				channel.getOneSample(sample);
 				System.out.println("    one sample:");
 				dataSheet.printSample(sample, 0, "      ");
 
 				int msPeriod = (int)(PascoChannel.convertRate(channel.getSampleRateDefault())*100);
 				sampleSize = channel.startContinuousSampling(msPeriod);
 				System.out.println("    started sampling, sample size: " + sampleSize);
 				byte [] samples = new byte[sampleSize*100];
 				for(int k=0; k<25; k++){
 					int bytesRead = channel.getSampleData(sampleSize, samples, 100);
 					System.out.println("    sampleData " + bytesRead + " bytes");
 					int numSamples = bytesRead / sampleSize;
 					int offset = 0;
 					for(int l=0; l<numSamples; l++){
 						dataSheet.printSample(samples, offset, "      ");
 						offset += sampleSize;
 					}
 					Thread.sleep(msPeriod);
 				}
 				channel.stopContinuousSampling();
 			}
 			
 			if(attachedChannels > 1){
 				collectAllChannels(dataSheets, channels);
 			} else {
 				System.out.println("    Not enough sensors (" + attachedChannels + ") to do a multichannel test.");
 			}
 			
 		}
 		
 //		// Connect to the sensor
 //		success = pasco.PasOpenInterface( );
 //		System.out.println( "Open Interface: success=" + success );
 //		
 //    	int id = pasco.PasGetIfaceType();
 //    	System.out.println("Device interface type = " + id);
 //
 //		// Dump the sensor's datasheet
 //		byte[] dsHdr = new byte[16];
 //		// DSHeader dsHdr = new DSHeader();
 //		// success = pasco.PasReadDatasheet( dsHdr.getPointer(), dsHdr.size() );
 //		success = pasco.PasReadDatasheet(dsHdr, 16);
 //		System.out.println( "Read Datasheet Header: success=" + success );
 //		for (byte b : dsHdr) {
 //			System.out.print(b);
 //		}
 //		System.out.println("");
 //		if (success == 0) {
 //			System.out.println("Couldn't read a valid data sheet!");
 //			return;
 //		}
 //		PasportSensorDataSheet ds = readDataSheet(pasco);
 ////		System.out.println( "SensorID = " + dsHdr.sensorID + ", Max DS Size = " + dsHdr.maxSize + ", DS Size = " + dsHdr.dsSize + ", Ext DS size = " + dsHdr.extDsSize);
 //		System.out.println( "SensorID = " + ds.id + ", Max DS Size = " + ds.maxDataSheetSize + ", DS Size = " + ds.dataSheetLength + ", Ext DS size = " + ds.extendedDataSheetLength);
 //		// DataSheetData data = new DataSheetData();
 //		System.out.println(ds.getStringView());
 //		byte[] data;
 ////		data = new byte[dsHdr.dsSize + dsHdr.extDsSize];
 ////		success = pasco.PasReadDatasheet( data.getPointer(), dsHdr.dsSize + dsHdr.extDsSize );
 ////		if( success == 1) {
 ////			dumpBuffer( data, dsHdr.dsSize + dsHdr.extDsSize );
 ////		}
 //		
 //		// Do a few one-shot reads
 //		for( i = 0; i < 5; i++ )
 //		{
 //			data = new byte[sampleSize];
 //			success = pasco.PasReadOneSample( data, sampleSize );
 //			System.out.println( String.format("Read sample %d of %d, success=%d\n", i+1, 5, success) );
 //			dumpBuffer( data, sampleSize );
 //			Thread.sleep( 1000 );
 //		}
 //		
 //		// Start continuous sampling
 //		success = pasco.PasStartSampling( sampleSize, samplePeriod );
 //		System.out.println( String.format("Start continuous sampling, success=%d\n", success) );
 //		
 //		// Gather and dump the sample data
 //		data = new byte[100];
 //		for( i = 0; i < 50; i++ )
 //		{
 //			success = pasco.PasReadSampleData( data, 100 );
 //			if( success != 0 )
 //				dumpBuffer( data, success );
 //			Thread.sleep( 100 );
 //		}
 //		
 //		// Stop continuous sampling
 //		success = pasco.PasStopSampling( );
 //		System.out.println( String.format("Stop continuous sampling, success=%d\n", success ));
 //		
 //		// Show sensor and interface connection status for the next ten seconds
 //		for( i = 0; i < 10; i++ )
 //		{
 //			System.out.println( String.format("Connection Status: sensor=%d, iface=%d\n", pasco.PasIsSensorConnected( ), pasco.PasIsInterfaceConnected( ) ));
 //			Thread.sleep( 1000 );
 //		}
 //		
 //		System.out.println( "Done testing Pasco USB sensor lib" );
 //
 //	    System.exit(0);
 		library.stop();
 		library.delete();
 
 	}
 
 	private static void collectAllChannels(PasportSensorDataSheet[] dataSheets, PascoChannel[] channels) throws PascoException, InterruptedException {
 		// try to collect from all channels at the same time
 		// find smallest default period
 		float minDefaultPeriod = Float.MAX_VALUE;
 		for(PasportSensorDataSheet dataSheet: dataSheets){
 			if(dataSheet == null){
 				continue;
 			}
 			if(dataSheet.getDefaultPeriod() < minDefaultPeriod){
 				minDefaultPeriod = dataSheet.getDefaultPeriod();
 			}
 		}
 		
 		// make sure that smallest period isn't smaller than the min period of any channel
 		for(PasportSensorDataSheet dataSheet: dataSheets){
 			if(dataSheet == null){
 				continue;
 			}
 			if(dataSheet.getMinPeriod() > minDefaultPeriod){
 				minDefaultPeriod = dataSheet.getMinPeriod();
 			}
 		}
 
 		// Finally as a sanity check make sure the minDefaultPeriod is not larger than the max of any channel
 		for(PasportSensorDataSheet dataSheet: dataSheets){
 			if(dataSheet == null){
 				continue;
 			}
 			if(minDefaultPeriod > dataSheet.getMaxPeriod()){
 				System.err.println("Can't find a default period that is in the range of all the channels");
 				minDefaultPeriod = Float.NaN;
 			}
 		}
 
 		if(Float.isNaN(minDefaultPeriod)){
 			// go on to next device
 			return;
 		}
 		
 		System.out.println("Starting collection on all channels with period: " + minDefaultPeriod);
 
		int msPeriod = (int)(minDefaultPeriod*1000);
 		
 		// start all the channels
 		int[] sampleSizes = new int[channels.length];
 		byte[][] samples = new byte[channels.length][];
 		for(int i=0; i<channels.length; i++){
 			if(dataSheets[i] == null){
 				continue;
 			}
 			sampleSizes[i] = channels[i].startContinuousSampling(msPeriod);
 			samples[i] = new byte[sampleSizes[i]*100];
 		}
 
 		for(int i=0; i<25; i++){
 			for(int j=0; j<channels.length; j++){
 				if(dataSheets[j] == null){
 					continue;
 				}
 				int bytesRead = channels[j].getSampleData(sampleSizes[j], samples[j], 100);
 				System.out.println("    sampleData " + bytesRead + " bytes");
 				int numSamples = bytesRead / sampleSizes[j];
 				int offset = 0;
 				for(int k=0; k<numSamples; k++){
 					dataSheets[j].printSample(samples[j], offset, "      ");
 					offset += sampleSizes[j];
				}
 			}
 			Thread.sleep(msPeriod);
 		}
 
 		// stop all channels
 		for(int i=0; i<channels.length; i++){
 			if(dataSheets[i] == null){
 				continue;
 			}
 			channels[i].stopContinuousSampling();
 		}
 	}
 	
 	private static void dumpBuffer (byte[] pvBuf, int lLen)
 	{
 	    byte[] pbBuf = pvBuf;
 		int lBytes = 1;
 		
 	    // for each 16 byte row
 	    for (int nRow = 0; nRow <= (lLen-1)/16; nRow++)
 	    {
 	        String pszStr = "";
 			
 	        // fill in the hex view of the data
 	        int nCol;
 	        for (nCol = 0; nCol < 16; nCol+=lBytes)
 	        {
 	            switch (lBytes)
 	            {
 					case 1: // show as bytes
 					default:
 						if (nRow*16+nCol < lLen)
 							pszStr += String.format("%02x ", pbBuf[nRow*16+nCol]);
 						else
 							pszStr += String.format("   ");
 						break;
 						
 					case 2: // show as shorts
 						if (nRow*16+nCol < lLen)
 							pszStr += String.format ("%04x ", pbBuf[nRow*16+nCol]);
 						else
 							pszStr += String.format("     ");
 						break;
 						
 					case 4: // show as longs
 						if (nRow*16+nCol < lLen)
 							pszStr += String.format ("%08x ", pbBuf[nRow*16+nCol]);
 						else
 							pszStr += String.format ("         ");
 						break;
 	            }
 	        }
 			
 	        // fill in the char view of the data
 	        for (nCol = 0; nCol < 16; nCol++)
 	        {
 	            if (nRow*16+nCol < lLen)
 	            {
 	                byte ch = pbBuf[nRow*16+nCol];
 	                if (ch < 0x20 || ch >= 0x7f)
 	                    pszStr += ".";
 	                else
 	                    pszStr += String.format("%c", ch);
 	            }
 	            else
 	                pszStr += " ";
 	        }
 			
 			System.out.println(String.format( "      %05x: %s", nRow*0x10, pszStr));
 	    }
 	}
 	
 }
