 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
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
 
 import gda.configuration.properties.LocalProperties;
 import gda.device.DeviceException;
 import gda.device.timer.FrameSet;
 import gda.util.Gaussian;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.util.io.FileUtils;
 
 /**
  * A Dummy DA.Server implementation
  */
 public class DummyDAServer extends DAServer {
 	private static Logger logger = LoggerFactory.getLogger(DummyDAServer.class);
	private static int key = 0;
	private HashMap<Integer, String> handles;
 	private int count = 0;
	private boolean fail = false;
 	private boolean connected = true;
 	private int memorySize = 1024 * 512 * 512; // should be able to do 1kx1kx100 frames
 	private int runnum = 0;
 	private String previousFirstLetter = "";
 	private String firstLetter;
 	private String headerFileName;
 	private TimeFrameGenerator timeFrameGenerator = new TimeFrameGenerator();
 	private volatile String currentState = "IDLE";
 	private int currentFrameNumber = 0;
 	private int currentCycleNumber = 0;
 	private volatile boolean stopRun = false;
 	private int cycles = 1;
 	private Vector<FrameSet> timeFrameProfile = new Vector<FrameSet>();
 	private int old_data_size;
 	private boolean nonRandomTestData = false;
 	private String dataFile;
 	private int numberOfScalers = 4;
 	private int mcaGrades = 1;
 	private String resMode = "";
 	private ArrayList<Integer> listOfAllowedBits = new ArrayList<Integer>();
 	private Gaussian[] gaussians = new Gaussian[6];
 	private Gaussian middlegaussian;
 	private int gaussiansused = 0;
 	private boolean xspressMcaCommandReceived = false;
 	private boolean xspressFullMcaCommandReceived = false;
 	private boolean xspressScalerCommandReceived = false;
 	private long[] scalerData = null;
 	private int[] xspressMcaData = null;
 	private int[] xspressFullMcaData = null;
 	private int[] xspressScalerData = null;
 	private int scanPointCount;
 	private long[][] scanPointDataFileContent;
 	
 	/**
 	 * initialise array etc.
 	 */
 	public DummyDAServer() {
 		handles = new HashMap<Integer, String>(20);
 		for (int i = 1; i <= 12; i++)
 			listOfAllowedBits.add(new Integer(i));
 		listOfAllowedBits.add(1024);
 	}
 
 	@Override
 	public boolean isConnected() {
 		return connected;
 	}
 
 	@Override
 	public void connect() {
 		logger.info("DummyDAServer {} being connected", getName());
 		connected = true;
 	}
 
 	@Override
 	public void reconnect() {
 		logger.info("DummyDAServer {} being reconnected", getName());
 		connected = true;
 	}
 
 	@Override
 	public void configure() {
 		connect();
 		doStartupScript();
 	}
 
 	private void doStartupScript() {
 		if (isConnected() && startupCommands.size() != 0)
 			for (String command : startupCommands)
 				sendCommand(command);
 	}
 
 	@Override
 	public Object sendCommand(String command) {
 		Object rc = -1;
 		// set fail true for the next command only (for JUNIT tests)
 		// return in this block of code otherwise fail will be reset to false
 		logger.info("DAServer command: " + command);
 		if (command.startsWith("Fail")) {
 			fail = true;
 			return 0;
 		}
 		// TimeFrameGenerator commands in this section
 		else if (command.startsWith("tfg"))
 			rc = parseTFGCommand(command, rc);
 		else if (command.contains("module open 'tfg_times' header")) {
 			handles.put(++key, "scaler");
 			rc = (fail) ? -1 : key;
 		}
 		// xspress specific commands dealt with in this section
 		else if (command.startsWith("xspress"))
 			rc = parseXspressCommand(command);
 		// HeaderFileWriter specific commands dealt with in this section
 		else if (command.startsWith("file"))
 			rc = parseFileCommand(command);
 		// GDHist commands in this section
 		else if (command.startsWith("gdhist open") || command.startsWith("gdscaler open")) {
 			handles.put(++key, "gdhist");
 			rc = (fail) ? -1 : key;
 		} 
 		else if (command.startsWith("vvhist open")) {
 			handles.put(++key, "vvhist");
 			rc = (fail) ? -1 : key;
 		} 
 		else if (command.startsWith("vvhist"))
 			rc = (fail) ? -1 : key;
 		else if (command.startsWith("vtdc"))
 			rc = (fail) ? -1 : key;
 		else if (command.startsWith("scaler")) {
 			handles.put(++key, "scaler");
 			rc = (fail) ? -1 : key;
 		} 
 		else if (command.startsWith("~"))
 			rc = (fail) ? -1 : 0;
 		else if (command.startsWith("gdhist get-mem-size") || command.startsWith("gdscaler get-mem-size")
 				|| command.startsWith("vvhist get-mem-size")) {
 			rc = (fail) ? -1 : memorySize;
 		}
 		// Unified commands in this section
 		else if (command.contains("clear")) {
 			count = 0;
 			xspressFullMcaCommandReceived = false;
 			xspressScalerCommandReceived = false;
 			rc = (fail) ? -1 : 0;
 		} 
 		else if (command.startsWith("enable"))
 			rc = (fail) ? -1 : 0;
 		else if (command.startsWith("disable"))
 			rc = (fail) ? -1 : 0;
 		else if (command.contains("read"))
 			rc = parseReadCommand(command);
 		else if (command.startsWith("close-all")) {
 			handles.clear();
 			count = 0;
 			xspressFullMcaCommandReceived = false;
 			xspressScalerCommandReceived = false;
 			rc = (fail) ? -1 : 0;
 		} 
 		else if (command.startsWith("close")) {
 			StringTokenizer tokenizer = new StringTokenizer(command);
 			tokenizer.nextToken(); // close
 			int handle = Integer.valueOf(tokenizer.nextToken());
 			handles.remove(handle);
 			count = 0;
 			xspressScalerCommandReceived = false;
 			rc = (fail) ? -1 : 0;
 		}
 		fail = false;
 		return rc;
 	}
 
 	protected Object parseReadCommand(String command) {
 		Object rc;
 		StringTokenizer tokenizer = new StringTokenizer(command);
 		tokenizer.nextToken(); // command
 		int x = Integer.valueOf(tokenizer.nextToken());
 		int y = Integer.valueOf(tokenizer.nextToken());
 		int t = Integer.valueOf(tokenizer.nextToken());
 		int dx = Integer.valueOf(tokenizer.nextToken());
 		int dy = Integer.valueOf(tokenizer.nextToken());
 		int dt = Integer.valueOf(tokenizer.nextToken());
 		int handle = -1;
 		String filename = "";
 		String host = "";
 		String user = "";
 		String password = "";
 		while (tokenizer.hasMoreTokens()) {
 			String token = tokenizer.nextToken();
 			if ("from".equals(token))
 				handle = Integer.valueOf(tokenizer.nextToken());
 			else if ("to-local-file".equals(token))
 				filename = tokenizer.nextToken();
 			else if ("to-remote-file".equals(token))
 				filename = tokenizer.nextToken();
 			else if ("on".equals(token))
 				host = tokenizer.nextToken();
 			else if ("user".equals(token))
 				user = tokenizer.nextToken();
 			else if ("password".equals(token))
 				password = tokenizer.nextToken();
 			else if ("intel".equals(token))
 				continue; // do nothing for now
 			else if ("motorola".equals(token))
 				continue; // do nothing for now
 			else if ("raw".equals(token))
 				continue; // do nothing for now
 			else if ("float".equals(token))
 				continue; // do nothing for now
 			else
 				System.out.println("Token '" + token + "' not handled by DummyDAServer: Please report!");
 		}
 		if (command.contains("to-local-file") && filename != null) {
 			// remove leading and trailing quotes
 			filename = filename.substring(1, filename.length() - 1);
 			writeDummyFile(x, y, t, dx, dy, dt, handle, filename);
 		} else if (command.contains("to-remote-file") && filename != null) {
 			// remove leading and trailing quotes
 			filename = filename.substring(1, filename.length() - 1);
 			writeRemoteDummyFile(x, y, t, dx, dy, dt, handle, filename, host, user, password);
 		}
 		rc = (fail) ? -1 : 0;
 		return rc;
 	}
 
 	protected Object parseFileCommand(String command) {
 		Object rc;
 		if (command.contains("query")) {
 			runnum--;
 		}
 		StringTokenizer tokenizer = new StringTokenizer(command);
 		String token = tokenizer.nextToken();
 		while (!token.equals("first")) {
 			token = tokenizer.nextToken();
 		}
 
 		firstLetter = tokenizer.nextToken().substring(1, 2);
 		if (!previousFirstLetter.equals(firstLetter)) {
 			runnum = 0;
 			previousFirstLetter = firstLetter;
 		}
 		if (runnum > 99) {
 			runnum = 0;
 			if (firstLetter.equals("Z")) {
 				firstLetter = "A";
 			} else {
 				char let = (char) (firstLetter.charAt(0) + 1);
 				firstLetter = "" + let;
 			}
 		}
 		Calendar date = Calendar.getInstance();
 		int day = date.get(Calendar.DAY_OF_MONTH);
 		headerFileName = firstLetter + ((runnum < 10) ? "0" + runnum : runnum) + "000."
 				+ Integer.toHexString((date.get(Calendar.MONTH) + 1)).toUpperCase() + ((day < 10) ? "0" + day : day);
 		previousFirstLetter = firstLetter;
 		runnum++;
 		// }
 		rc = (fail) ? -1 : headerFileName;
 		return rc;
 	}
 
 	protected Object parseXspressCommand(String command) {
 		Object rc;
 		StringTokenizer tokenizer = new StringTokenizer(command);
 		tokenizer.nextToken(); // xspress command
 		String subCommand = tokenizer.nextToken(); // sub-command
 		if ("format-run".equals(subCommand)) {
 			parseXspressFormatRunCommand(tokenizer);
 			rc = (fail) ? -1 : 0;
 		} 
 		else if ("get-res-mode".equals(subCommand))
 			rc = (fail) ? -1 : resMode;
 		else if ("get-res-bins".equals(subCommand))
 			rc = (fail) ? -1 : mcaGrades;
 		else if ("set-window".equals(subCommand))
 			rc = (fail) ? -1 : 0;
 		else if ("set-roi".equals(subCommand))
 			rc = (fail) ? -1 : 0;
 		else if ("set-reset".equals(subCommand))
 			rc = (fail) ? -1 : 0;
 		else if ("set-glitch".equals(subCommand))
 			rc = (fail) ? -1 : 0;
 		else if ("set-input".equals(subCommand))
 			rc = (fail) ? -1 : 0;
 		else if ("read-frame".equals(subCommand))
 			rc = (fail) ? -1 : 0;
 		else if ("config".equals(subCommand)) {
 			tokenizer.nextToken(); // system-name
 			tokenizer.nextToken(); // path-name
 			Integer.valueOf(tokenizer.nextToken()); // number of detectors
 			rc = (fail) ? -1 : 0;
 		} 
 		else if ("open-mca".equals(subCommand)) {
 			if (!fail)
 				handles.put(++key, "xspress mca");
 			rc = (fail) ? -1 : key;
 		} 
 		else if ("open-scalers".equals(subCommand)) {
 			if (!fail)
 				handles.put(++key, "xspress scaler");
 			rc = (fail) ? -1 : key;
 		} 
 		// for xspress1
 		else if ("set-windows".equals(subCommand)) 
 			rc = (fail) ? -1 : 0;
 		else
 			rc = -1;
 		return rc;
 	}
 
 	protected Object parseTFGCommand(String command, Object rc) {
 		if (command.contains("tfg read status")) {
 			String state = currentState;
 			logger.debug("DummyDAServer responding with state: " + state);
 			rc = (fail) ? -1 : state;
 		} else if (command.contains("tfg init")) {
 			timeFrameGenerator.stop();
 			rc = (fail) ? -1 : 0;
 		} else if (command.contains("tfg read frame")) {
 			rc = (fail) ? -1 : currentFrameNumber;
 		} else if (command.contains("tfg read lap")) {
 			rc = (fail) ? -1 : currentCycleNumber;
 		} else if (command.contains("tfg start")) {
 			// generate dummy data
 			timeFrameGenerator.start();
 			currentState = "RUNNING";
 			rc = (fail) ? -1 : 0;
 		} else if (command.contains("tfg arm")) {
 			rc = (fail) ? -1 : 0;
 		} else if (command.contains("tfg setup-port")) {
 			rc = (fail) ? -1 : 0;
 		} else if (command.contains("tfg cont")) {
 			timeFrameGenerator.restart();
 			rc = (fail) ? -1 : 0;
 		} else if (command.contains("tfg options auto-cont")) {
 			// what to do here?
 			rc = (fail) ? -1 : 0;
 		} else if (command.contains("tfg setup-groups")) {
 			timeFrameProfile.removeAllElements();
 			StringTokenizer tokenizer2 = new StringTokenizer(command, "\n");
 			String token;
 			while (tokenizer2.hasMoreTokens()) {
 				token = tokenizer2.nextToken();
 				if (token.startsWith("tfg")) {
 					StringTokenizer tokenizer = new StringTokenizer(token);
 					tokenizer.nextToken(); // tfg
 					tokenizer.nextToken(); // setup-groups
 					token = tokenizer.nextToken(); // cycles, ext-inhibit or
 					// ext-start
 					if ("cycles".equals(token)) {
 						cycles = Integer.valueOf(tokenizer.nextToken()); // cycle
 					} else if ("ext-inhibit".equals(token)) {
 						continue; // ext-inhibit
 					} else if ("ext-start".equals(token)) {
 						continue; // ext-start
 					}
 				} else if (!(token.startsWith("-1"))) {
 					StringTokenizer tokenizer = new StringTokenizer(token);
 					int frames = Integer.valueOf(tokenizer.nextToken()); // frames
 					double deadTime = Double.valueOf(tokenizer.nextToken()); // deadtime(sec)
 					double liveTime = Double.valueOf(tokenizer.nextToken()); // livetime(sec)
 					tokenizer.nextToken(); // dead pulse
 					tokenizer.nextToken(); // live pulse
 					int deadPause = Integer.valueOf(tokenizer.nextToken()); // deadpause
 					int livePause = Integer.valueOf(tokenizer.nextToken()); // livepause
 					timeFrameProfile.add(new FrameSet(frames, deadTime * 1000, liveTime * 1000, 0, 0, deadPause,
 							livePause));
 				}
 			}
 			rc = (fail) ? -1 : 0;
 		} else if (command.contains("tfg generate")) {
 			rc = parseTFGGenerateCommand(command);
 		}
 
 		else if (command.contains("tfg open-cc")) {
 			handles.put(++key, "scaler");
 			rc = (fail) ? -1 : key;
 		}
 		return rc;
 	}
 
 	protected void parseXspressFormatRunCommand(StringTokenizer tokenizer) {
 		tokenizer.nextToken(); // system-name
 		while (tokenizer.hasMoreTokens()) {
 			String token = tokenizer.nextToken();
 			if ("res-min-div-8".equals(token)) {
 				mcaGrades = 16;
 				resMode = token;
 			} 
 			else if ("res-none".equals(token)) {
 				mcaGrades = 1;
 				resMode = token;
 			} 
 			else if ("res-thres".equals(token)) {
 				mcaGrades = 2; // bad and good
 				resMode = token;
 				// then skip the next token as it will be the threshold level
 				tokenizer.nextToken();
 			}
 			else if ("res-log".equals(token)) {
 				mcaGrades = 16;
 				resMode = token;
 			} 
 			else if ("res-top".equals(token)) {
 				mcaGrades = 128;
 				resMode = token;
 			} 
 			else if ("res-bot".equals(token)) {
 				mcaGrades = 128;
 				resMode = token;
 			} 
 			else if ("res-min".equals(token)) {
 				mcaGrades = 128;
 				resMode = token;
 			} 
 			else if ("nbits-adc4".equals(token))
 				continue; // do nothing for now
 			else if ("nbits-adc8".equals(token))
 				continue; // do nothing for now
 			else if ("nbits-adc14".equals(token))
 				continue; // do nothing for now
 			else if ("no-tfg".equals(token))
 				continue; // do nothing for now
 			else if ("no-scaler".equals(token))
 				continue; // do nothing for now
 			else if ("no-mca".equals(token))
 				continue; // do nothing for now
 			else if (listOfAllowedBits.contains(Integer.parseInt(token)))
 				continue; // do nothing for now
 			else
 				logger.error("Token '" + token + "' not handled by DummyDAServer.");
 		}
 	}
 
 	protected Object parseTFGGenerateCommand(String command) {
 		Object rc;
 		timeFrameProfile.removeAllElements();
 		StringTokenizer tokenizer = new StringTokenizer(command);
 		tokenizer.nextToken(); // tfg
 		tokenizer.nextToken(); // generate
 		int frames = Integer.valueOf(tokenizer.nextToken()); // frames
 		cycles = Integer.valueOf(tokenizer.nextToken()); // cycle
 		double deadTime = Double.valueOf(tokenizer.nextToken()); // deadtime(sec)
 		double liveTime = Double.valueOf(tokenizer.nextToken()); // livetime(sec)
 		tokenizer.nextToken(); // pause
 		timeFrameProfile.add(new FrameSet(frames, deadTime * 1000, liveTime * 1000, 0, 0, 0, 0));
 		rc = (fail) ? -1 : 0;
 		return rc;
 	}
 
 	// this is for scaler data
 	@Override
 	public long[] getLongBinaryData(String command, int data_size) {
 		int x = 0, y = 0, t = 0, dx = 0, dy = 0, dt = 0;
 		StringTokenizer tokenizer = new StringTokenizer(command);
 		tokenizer.nextToken(); // read
 		x = Integer.valueOf(tokenizer.nextToken());
 		y = Integer.valueOf(tokenizer.nextToken());
 		t = Integer.valueOf(tokenizer.nextToken());
 		dx = Integer.valueOf(tokenizer.nextToken());
 		dy = Integer.valueOf(tokenizer.nextToken());
 		dt = Integer.valueOf(tokenizer.nextToken()); // timeframes
 		int handle = -1;
 		while (tokenizer.hasMoreTokens()) {
 			String token = tokenizer.nextToken();
 			if ("from".equals(token))
 				handle = Integer.valueOf(tokenizer.nextToken());
 			else
 				continue; // do nothing for now
 		}
 		String memoryType = handles.get(handle);
 		if ("scaler".equals(memoryType))
 			scalerData = createDummyScalerData(x, y, t, dx, dy, dt);
 		else if ("gdhist".equals(memoryType))
 			scalerData = createDummyHistData(x, y, t, dx, dy, dt);
 		else if ("vvhist".equals(memoryType))
 			scalerData = createDummyHistData(x, y, t, dx, dy, dt);
 		return scalerData;
 	}
 
 	// this is for xspress mca and xspress scaler data
 	@Override
 	public int[] getIntBinaryData(String command, int data_size) {
 		int x = 0, y = 0, t = 0, dx = 0, dy = 0, dt = 0;
 		StringTokenizer tokenizer = new StringTokenizer(command);
 		tokenizer.nextToken(); // read
 		x = Integer.valueOf(tokenizer.nextToken());
 		y = Integer.valueOf(tokenizer.nextToken());
 		t = Integer.valueOf(tokenizer.nextToken());
 		dx = Integer.valueOf(tokenizer.nextToken());
 		dy = Integer.valueOf(tokenizer.nextToken());
 		dt = Integer.valueOf(tokenizer.nextToken()); // timeframes
 		int handle = -1;
 		while (tokenizer.hasMoreTokens()) {
 			String token = tokenizer.nextToken();
 			if ("from".equals(token))
 				handle = Integer.valueOf(tokenizer.nextToken());
 			else
 				continue; // do nothing for now
 		}
 		String memoryType = handles.get(handle);
 
 		if ("xspress mca".equals(memoryType)) {
 			int tmp = Math.max(dx, dy);
 			if (DUMMY_XSPRESS2_MODE.XSPRESS2_FULL_MCA == xspressMode) {
 				if (xspressFullMcaCommandReceived == false) {
 					xspressFullMcaCommandReceived = true;
 					xspressFullMcaData = createXspressDummyMcaData(data_size);
 				}
 				return xspressFullMcaData;
 			}
 			if (xspressMcaCommandReceived == false) {
 				xspressMcaCommandReceived = true;
 				xspressMcaData = createXspressDummyMcaData(0, tmp / mcaGrades, mcaGrades);
 			}
 			return xspressMcaData;
 		}
 
 		if ("xspress scaler".equals(memoryType)) {
 			if (xspressScalerCommandReceived == false) {
 				xspressScalerCommandReceived = true;
 				xspressScalerData = createXspressDummyScalerData(x, y, t, dx, dy, dt);
 			}
 			return xspressScalerData;
 		}
 		return null;
 	}
 
 	@Override
 	public double[] getBinaryData(String command, int data_size) {
 		double[] data = new double[data_size];
 		long[] ldata = getLongBinaryData(command, data_size);
 
 		if (data_size == ldata.length + 1) {
 			data[0] = 100000; // Live time
 			for (int i = 1; i <= ldata.length; i++)
 				data[i] = ldata[i - 1];
 		} 
 		else {
 			for (int i = 0; i < data.length; i++)
 				data[i] = ldata[i];
 		}
 		return data;
 	}
 
 	/**
 	 * dummy close the server, resets open paths
 	 * 
 	 * @throws DeviceException
 	 */
 	@Override
 	public void close() throws DeviceException {
 		timeFrameGenerator = null;
 		count = 0;
 		xspressFullMcaCommandReceived = false;
 		xspressScalerCommandReceived = false;
 		handles.clear();
 		key = 0;
 	}
 
 	private class TimeFrameGenerator implements Runnable {
 		Thread runner;
 
 		public synchronized void start() {
 			runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
 			currentState = "RUNNING";
 			runner.start();
 		}
 
 		public void restart() {
 			// to break out of wait() calls in loop
 			runner.interrupt();
 		}
 
 		public void stop() {
 			stopRun = true;
 			// check OK to interrupt
 			if (runner != null && runner.isAlive() && !runner.isInterrupted())
 				runner.interrupt();
 		}
 
 		@Override
 		public synchronized void run() {
 			try {
 				currentState = "RUNNING";
 				for (currentCycleNumber = cycles; currentCycleNumber > 0; currentCycleNumber--) {
 					currentFrameNumber = 0;
 					for (FrameSet frameSet : timeFrameProfile) {
 						for (int i = 0; i < frameSet.getFrameCount(); i++) {
 							if (stopRun)
 								throw new InterruptedException("Stopping run");
 							if (frameSet.getDeadPause() !=0) {
 								try {
 									currentState = "PAUSED";
 									wait();
 								} catch (InterruptedException e) {
 									// do nothing
 								}
 								currentState = "RUNNING";
 							}
 							waitDouble(frameSet.getRequestedDeadTime());
 							currentFrameNumber++;
 							if (stopRun)
 								throw new InterruptedException("Stopping run");
 							if (frameSet.getLivePause() == 1) {
 								try {
 									currentState = "PAUSED";
 									wait();
 								} catch (InterruptedException e) {
 									// do nothing
 								}
 								currentState = "RUNNING";
 							}
 							waitDouble(frameSet.getRequestedLiveTime());
 							currentFrameNumber++;
 							if (stopRun)
 								throw new InterruptedException("Stopping run");
 						}
 					}
 				}
 			} catch (InterruptedException e) {
 				// no one seems to care
 			} finally {
 				stopRun = false;
 				currentState = "IDLE";
 				currentFrameNumber = 0;
 				currentCycleNumber = 0;
 				logger.debug("DummyTfg stopped");
 			}
 		}
 	}
 
 	/**
 	 * Waits for a non-integral number of milli seconds by converting the value to be used in the two parameter version
 	 * of wait()
 	 * 
 	 * @param milliSeconds
 	 * @throws InterruptedException
 	 */
 	private void waitDouble(double milliSeconds) throws InterruptedException {
 		if (milliSeconds == 0)
 			return;
 		double mS = Math.floor(milliSeconds);
 		double nS = (milliSeconds - mS) * 1.0E6;
 		synchronized (this) {
 			wait((int) mS, (int) nS);
 		}
 	}
 
 	/**
 	 * @return the nonRandomTestData
 	 */
 	public boolean isNonRandomTestData() {
 		return nonRandomTestData;
 	}
 
 	/**
 	 * @param nonRandomTestData
 	 *            the nonRandomTestData to set
 	 */
 	public void setNonRandomTestData(boolean nonRandomTestData) {
 		this.nonRandomTestData = nonRandomTestData;
 	}
 
 	/**
 	 * Creates dummy data for a single detector. The data is a pair of overlapping Gaussians. The peak positions depend
 	 * on which detector is specified so that window and gain setting code can be tested.
 	 * 
 	 * @param detector
 	 *            the detector number
 	 * @return suitable dummy data
 	 */
 	private int[] createXspressDummyMcaData(int detector, int mcaChannels, int mcaGrades) {
 		int[] data = new int[mcaChannels * mcaGrades];
 		Gaussian gaussianOne;
 		Gaussian gaussianTwo;
 		double noiseLevel = 0.2 + 0.01 * detector;
 		for (int k = 0; k < mcaGrades; k++) {
 			gaussianOne = new Gaussian(1600.0 + 100.0 * detector, (mcaGrades - k) * 20.0, 1000.0);
 			gaussianTwo = new Gaussian(1000.0 + 100.0 * detector, (mcaGrades - k) * 20.0, 500.0);
 			for (int i = 0; i < mcaChannels; i++) {
 				data[i + k * mcaChannels] = 1;
 				data[i + k * mcaChannels] = (int) (gaussianOne.yAtX(i) * (1.0 - Math.random() * noiseLevel) + gaussianTwo
 						.yAtX(i) * (1.0 - Math.random() * noiseLevel));
 			}
 		}
 		return data;
 	}
 
 	private int[] createXspressDummyMcaData(int size) {
 		int[] data = new int[size];
 		int last = 10000;
 		for (int i = 0; i < data.length; i++) {
 			final int delta = (int) ((Math.random() - 0.5d) * 100);
 			last = last + delta;
 			data[i] = last;
 		}
 		return data;
 	}
 
 	private long[] createDummyScalerData(int x, int y, int t, int dx, int dy, int dt) {
 		try {
 			if (this.dataFile == null)
 				return createDummyArray(x, y, t, dx, dy, dt);
 			long[] data;
 			if (scanPointDataFileContent == null)
 				scanPointDataFileContent = readScanDataFile();
 			if (scanPointCount >= scanPointDataFileContent.length) {
 				// If we are asking for more point than scan data in file, the just show straight line.
 				data = scanPointDataFileContent[scanPointDataFileContent.length - 1];
 			} 
 			else
 				data = scanPointDataFileContent[scanPointCount];
 			++scanPointCount;
 			return data;
 		} catch (Exception ne) {
 			logger.error("Ion Chamber error", ne);
 			return null;
 		}
 	}
 
 	private long[] createDummyArray(int x, int y, int t, int dx, int dy, int dt) {
 		long[] data;
 		data = new long[dx * dy * dt];
 		int l = 0;
 		for (int k = x; k < x + dx; k++) {
 			for (int j = y; j < y + dy; j++) {
 				for (int i = t; i < t + dt; i++) {
 					if (nonRandomTestData)
 						data[l] = i + j + (k * 10) + count;
 					else
 						data[l] = (long) ((Math.random() + 1) * Math.pow(10, (dt - i)));
 					l++;
 				}
 			}
 		}
 		return data;
 	}
 
 	private long[][] readScanDataFile() {
 		File file;
 		if (dataFile.contains("/"))
 			file = new File(dataFile);
 		else
 			file = new File(LocalProperties.get(LocalProperties.GDA_CONFIG) + "/testing/" + dataFile);
 		try {
 			final List<String> lines = FileUtils.readFileAsList(file);
 			final List<long[]> data = new ArrayList<long[]>(31);
 			for (String line : lines) {
 				if (line == null)
 					continue;
 				if (line.startsWith("#"))
 					continue;
 				if ("".equals(line.trim()))
 					continue;
 				final String[] dataLine = line.split(" ");
 				long[] dblData = new long[dataLine.length];
 				for (int i = 0; i < dataLine.length; i++)
 					dblData[i] = Math.round(Double.parseDouble(dataLine[i]));
 				data.add(dblData);
 			}
 			return data.toArray(new long[data.size()][3]);
 		} catch (Exception e) {
 			logger.error("Cannot read " + file + " as string list.", e);
 		}
 		return null;
 	}
 
 	private long[] createDummyHistData(int x, int y, int t, int dx, int dy, int dt) {
 		int data_size = dx * dy * dt;
 		long[] data = new long[data_size];
 		if (nonRandomTestData) {
 			int l = 0;
 			for (int k = t; k < t + dt; k++) {
 				for (int j = y; j < y + dy; j++) {
 					for (int i = x; i < x + dx; i++) {
 						data[l] = i + j + (k * 10) + count;
 						l++;
 					}
 				}
 			}
 		} 
 		else {
 			if (gaussians[0] == null || gaussiansused > 13 || data_size != old_data_size) { // initialise them on the																		// fly
 				old_data_size = data_size;
 				gaussiansused = 0;
 				// one for the background
 				gaussians[0] = new Gaussian(data_size / 3, data_size, 400.0);
 				// some for the show
 				for (int j = 1; j < gaussians.length; j++) {
 					gaussians[j] = new Gaussian(data_size * j / gaussians.length + (Math.random() - 0.5) * data_size
 							/ (20 * gaussians.length), data_size / (40 * gaussians.length), (Math.random() + 1) * 800);
 				}
 				middlegaussian = new Gaussian(dx / 2 + dx / 10 * Math.random(), dx / 7, 4.0);
 			}
 
 			gaussiansused++;
 
 			int l = 0;
 			for (int k = t; k < t + dt; k++) { // timeframe loop
 				for (int j = y; j < y + dy; j++) { // y loop
 					// one for the noise
 					data[l] = (long) (count + Math.random() * (count + 1) / (Math.sqrt(count / 50) + 0.1));
 					for (int i = x; i < x + dx; i++) { // x loop
 						for (Gaussian element : gaussians)
 							data[l] += element.yAtX(l) * (count + 1) / 10;
 						data[l] *= middlegaussian.yAtX(i);
 						l++;
 					}
 				}
 			}
 		}
 		return data;
 	}
 
 	private int[] createXspressDummyScalerData(int x, int y, int t, int dx, int dy, int dt) {
 		Random generator = new Random();
 		long[] scaler = new long[numberOfScalers];
 		int[] data = new int[dx * dy * dt];
 		int l = 0;
 		if (nonRandomTestData) {
 			for (int k = t; k < t + dt; k++) {
 				scaler[0] = 25000 + k * 100;
 				scaler[1] = 199 + k * 10;
 				scaler[2] = 21050 + k * 100;
 				scaler[3] = 22000 + k * 100;
 				for (int j = y; j < y + dy; j++)
 					for (int i = x; i < x + dx; i++, l++)
 						data[l] = (int) scaler[i];
 			}
 		} else {
 			for (int k = t; k < t + dt; k++) {
 				for (int j = y; j < y + dy; j++) {
 					scaler[0] = generator.nextInt(1000000);// all counts
 					scaler[3] = 400000000;// set time to 5sec at 12.5e-9 clock ticks
 					// the number of resets should be approx 10% time scaler
 					scaler[1] = (scaler[3] / 100) * (generator.nextInt(9) + 1);
 					// the windowed counts to be 10-90% all scaler.
 					scaler[2] = (scaler[0] / 100) * (generator.nextInt(80) + 10);
 					for (int i = x; i < x + dx; i++, l++)
 						data[l] = (int) scaler[i];
 				}
 			}
 		}
 		return data;
 	}
 
 	/**
 	 * Writes a dummy data file - the data is written in the same format as is used by the real daserver.
 	 * 
 	 * @param x
 	 * @param y
 	 * @param frame
 	 * @param mcaGradesmcaChannels
 	 * @param numberOfDetectors
 	 * @param numberOfFrames
 	 * @param mcaHandle
 	 * @param filename
 	 * @return the name of the file
 	 */
 	@SuppressWarnings("unused")
 	private String writeDummyFile(int x, int y, int frame, int mcaGradesmcaChannels, int numberOfDetectors,
 			int numberOfFrames, int mcaHandle, String filename) {
 		int[] data;
 		byte[] bytes = new byte[mcaGradesmcaChannels * 4];
 		try {
 			File f = new File(filename);
 			FileOutputStream fos = new FileOutputStream(f);
 			DataOutputStream dos = new DataOutputStream(fos);
 			for (int j = 0; j < numberOfDetectors; j++) {
 				data = createXspressDummyMcaData(j, mcaGradesmcaChannels / mcaGrades, mcaGrades);
 				for (int l = 0; l < (mcaGradesmcaChannels * 4); l += 4) {
 					bytes[l + 0] = (byte) (data[l / 4] >>> 24);
 					bytes[l + 1] = (byte) (data[l / 4] >>> 16);
 					bytes[l + 2] = (byte) (data[l / 4] >>> 8);
 					bytes[l + 3] = (byte) (data[l / 4]);
 				}
 				dos.write(bytes);
 			}
 			dos.close();
 			filename = f.getCanonicalPath();
 		} catch (IOException e) {
 			logger.error("writeDummyData: IOException while writing file " + filename);
 		}
 		return filename;
 	}
 
 	/**
 	 * @param x
 	 * @param y
 	 * @param frame
 	 * @param mcaGradesmcaChannels
 	 * @param numberOfDetectors
 	 * @param numberOfFrames
 	 * @param mcaHandle
 	 * @param filename
 	 * @param host
 	 * @param user
 	 * @param password
 	 * @return null
 	 */
 	@SuppressWarnings("unused")
 	private String writeRemoteDummyFile(int x, int y, int frame, int mcaGradesmcaChannels, int numberOfDetectors,
 			int numberOfFrames, int mcaHandle, String filename, String host, String user, String password) {
 		// to be implemented
 		return null;
 	}
 
 	/**
 	 * @return Returns the mofoil2Data.
 	 */
 	public String getDataFile() {
 		return dataFile;
 	}
 
 	/**
 	 * @param dataFile
 	 *            The data to set.
 	 */
 	public void setDataFile(String dataFile) {
 		this.dataFile = dataFile;
 	}
 
 	/**
 	 * Called for classes using DummyDAServer in fileData mode.
 	 */
 	public void resetScanPointCount() {
 		scanPointCount = 0;
 		scanPointDataFileContent = null;
 	}
 
 	private DUMMY_XSPRESS2_MODE xspressMode = DUMMY_XSPRESS2_MODE.XSPRESS2_SINGLE_MCA;
 
 	/**
 	 * @return Returns the xspressMode.
 	 */
 	public DUMMY_XSPRESS2_MODE getXspressMode() {
 		return xspressMode;
 	}
 
 	/**
 	 * @param xspressMode
 	 *            The xspressMode to set.
 	 */
 	public void setXspressMode(DUMMY_XSPRESS2_MODE xspressMode) {
 		this.xspressMode = xspressMode;
 	}
 
 	@Override
 	public float[] getFloatBinaryData(String message, int ndata) {
 		long[] binaryData = getLongBinaryData(message, ndata);
 		float[] floatbd = new float[ndata];
 		if (binaryData.length < floatbd.length)
 			logger.error(String.format("command %s did not deliver %d values", message, ndata));
 		for (int j = 0; j < floatbd.length; j++)
 			floatbd[j] =  binaryData[j];
 		return floatbd;
 	}
 
 	public int getMemorySize() {
 		return memorySize;
 	}
 	
 }
