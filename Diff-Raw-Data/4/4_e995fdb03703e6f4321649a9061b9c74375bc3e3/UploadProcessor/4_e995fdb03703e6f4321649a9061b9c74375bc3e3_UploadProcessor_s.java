 /*
  * Copyright 2012,2013 Robert Huitema robert@42.co.nz
  * 
  * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
  * 
  * FreeBoard is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * FreeBoard is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FreeBoard. If not, see <http://www.gnu.org/licenses/>.
  */
 package nz.co.fortytwo.freeboard.installer;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.JTextArea;
 
 import org.apache.log4j.Logger;
 
 /**
  * Uploads hex files to the arduinos
  * 
  * @author robert
  * 
  */
 public class UploadProcessor {
 
 	Logger logger = Logger.getLogger(UploadProcessor.class);
 	// Properties config = null;
 	private boolean manager = false;
 	private JTextArea textArea;
 
 	public UploadProcessor() throws Exception {
 
 	}
 
 	public UploadProcessor(boolean manager, JTextArea textArea) throws Exception {
 		this.manager = manager;
 		this.textArea = textArea;
 	}
 
 	public void processUpload(File hexFile, String commPort, String device, String dudeDir) throws Exception {
 		// make a file
 		if (!hexFile.exists()) {
 			if (manager) {
 				System.out.print("No file at " + hexFile.getAbsolutePath() + "\n");
 			}
 			logger.error("No file at " + hexFile.getAbsolutePath());
 		}
 		
 			processHexFile(hexFile,commPort,device, dudeDir);
 	
 	}
 	
 
 	/**
 	 * Upload the Hex file to the named port
 	 * 
 	 * @param hexFile
 	 * @param device 
 	 * @throws Exception
 	 */
 	public void processHexFile(File hexFile, String commPort, String device, String dudeDir) throws Exception {
 
 		if (manager) {
 			System.out.print("Uploading:" + hexFile.getPath() + "\n");
 		}
 
 		logger.debug("Uploading hex file:" + hexFile.getPath());
 		// start by running avrdude
 		//ArduIMU
 		//tools/avrdude -patmega328p -carduino -P/dev/ttyUSB0 -b57600 -D -q -q -v -v -v -v -Uflash:w:FreeBoardIMU.cpp.hex:i -C$ARDUINO_HOME/hardware/tools/avrdude.conf
 		
 		//tools/avrdude -patmega1280 -carduino -P/dev/ttyUSB0 -b57600 -D -v -v -v -v -Uflash:w:FreeBoardPLC.hex:a -C$ARDUINO_HOME/hardware/tools/avrdude.conf
 		
 			String pDevice = "-p"+device;
 			String avrdude = "avrdude";
 			String avrType = ":a";
 			if(device.equals("atmega328p")){
 				avrType=":i";
 			}
		executeAvrdude(hexFile, Arrays.asList(dudeDir + "/" + avrdude, pDevice, "-carduino", "-P" + commPort, "-b57600", "-D", "-v", "-v", "-v",
				"-Uflash:w:"+hexFile.getName()+avrType, "-C" + dudeDir + "/avrdude.conf"));
 
 	}
 
 	/**
 	 * Executes avrdude with relevant params
 	 * 
 	 * @param config2
 	 * @param hexFile
 	 * @param argList
 	 * @param chartName
 	 * @param list
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	private void executeAvrdude(File hexFile, List<String> argList) throws IOException, InterruptedException {
 
 		ProcessBuilder pb = new ProcessBuilder(argList);
 		pb.directory(hexFile.getParentFile());
 		//pb.inheritIO();
 		if (manager) {
 			ForkWorker fork = new ForkWorker(textArea, pb);
 			fork.execute();
 			//fork.doInBackground();
 			while (!fork.isDone()) {
 				Thread.currentThread().sleep(500);
 				// System.out.print(".");
 			}
 			if(fork.getResult()==0){
 				System.out.print("Avrdude completed normally, and the code has been uploaded\n");
 			}else{
 				System.out.print("ERROR: avrdude did not complete normally, and the device may not work correctly\n");
 			}
 		} else {
 			Process p = pb.start();
 			p.waitFor();
 			if (p.exitValue() > 0) {
 				if (manager) {
 					System.out.print("ERROR: avrdude did not complete normally\n");
 				}
 				logger.error("avrdude did not complete normally");
 				return;
 			} else {
 				System.out.print("Completed upload\n");
 			}
 		}
 
 	}
 
 }
