 package edu.rochester.cif.cifreader;
 
 import edu.rochester.cif.cifreader.external.ReaderDoorSense;
 import edu.rochester.cif.cifreader.external.ReaderDoorStrike;
 import edu.rochester.cif.cifreader.external.ReaderEvent;
 import edu.rochester.cif.cifreader.external.ReaderEventListener;
 import edu.rochester.cif.cifreader.external.ReaderMode;
 import edu.rochester.cif.cifreader.external.ReaderEvent.ReaderEventType;
 import edu.rochester.cif.threading.MultiplePausableThreadPoolExecutor;
 import edu.rochester.cif.threading.StoppableQueueThread;
 import gnu.io.CommPortIdentifier;
 import gnu.io.NoSuchPortException;
 import gnu.io.PortInUseException;
 import gnu.io.SerialPort;
 import gnu.io.UnsupportedCommOperationException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.TooManyListenersException;
 import java.util.Vector;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.commons.configuration.ConfigurationRuntimeException; //import org.apache.commons.logging.Log;
 //import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.Logger;
 
 public class Reader {
 
 	private CommPortIdentifier[] commPorts;
 	// private CommPortIdentifier currentCommPort;
 	private SerialPort myPort;
 	private final CommandParser commandParser = new CommandParser();
 	private final Maintenance maintenance = new Maintenance();
 	private SerialWriter serialWriter;
 	private SerialReader serialReader;
 	protected static Logger logger = Logger.getLogger(Reader.class);
 
 	private long lastReceiveTimeMillis = 0;
 	private long lastEepromTimeMillis = 0;
 	private long lastStatusTimeMillis = 0;
 	private long lastCountTimeMillis = 0;
 	private long lastStoreTimeMillis = 0;
 
 	private final Object gotStatus = new Object();
 	private final Object gotEeprom = new Object();
 	private final Object gotCount = new Object();
 	private final Object gotAnything = new Object();
 	private final Object gotStoreResponse = new Object();
 
 	private String fw_name = "";
 	private String fw_ver_s = "";
 	private float fw_ver = 0.0f;
 	private ReaderMode reader_mode = ReaderMode.READER_MODE_UNKNOWN;
 	private ReaderDoorStrike reader_strike = ReaderDoorStrike.READER_DOOR_STRIKE_UNKNOWN;
 	private ReaderDoorSense reader_sense = ReaderDoorSense.READER_DOOR_SENSE_UNKNOWN;
 	private String stored_prefix = "";
 	private String stored_postfix = "";
 	private ReaderMode stored_mode = ReaderMode.READER_MODE_UNKNOWN;
 	private int door_open_duration = 0;
 	private int id_length = 0;
 	private int storable_ids = 0;
 	private String[] stored_ids = new String[0];
 	private String lastFail = null;
 	private String lastStore = null;
 
 	private Vector<ReaderEventListener> listeners = new Vector<ReaderEventListener>();
 
 	private final MultiplePausableThreadPoolExecutor eventHandlerThreadPool;
 
 	public Reader() {
 		// this.updateComPorts();
 		this.commandParser.doRun();
		//TODO: We want SerialWriter to always exist, but possibly be paused.
 		Thread mThread = new Thread(maintenance, "Reader Maintenance");
 		mThread.start();
 		eventHandlerThreadPool = new MultiplePausableThreadPoolExecutor(Executors.defaultThreadFactory(), 2, 10);
 	}
 
 	public long timeSinceLastReceive() {
 		return System.currentTimeMillis() - lastReceiveTimeMillis;
 	}
 
 	public void addEventListener(ReaderEventListener listener) {
 		if (!this.listeners.contains(listener)) {
 			this.listeners.add(listener);
 		}
 	}
 
 	public void removeEventListener(ReaderEventListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	private void raiseEvent(ReaderEvent event) {
 		// TODO: Use thread pool for this.
 		for (ReaderEventListener l : this.listeners) {
 			try {
 				eventHandlerThreadPool.execute(new ReaderEventRunner(event, l));
 				// l.readerEvent(event);
 			} catch (Exception e) {
 				logger.error("Issue propagating reader event", e);
 			}
 		}
 	}
 
 	// TODO: How do we handle shutdown? (no one ever calls us. We feel so
 	// unwanted.)
 	private class Maintenance implements Runnable {
 
 		boolean doReset = false;
 		boolean doShutdown = false;
 		private Thread myThread = null;
 
 		@Override
 		public void run() {
 			boolean gotConn = false;
 			myThread = Thread.currentThread();
 			while (!doShutdown) {
 				if (!gotConn) {
 					updateComPorts();
 					for (CommPortIdentifier commPort : Reader.this.commPorts) {
 						logger.info("Attempting connection to port " + commPort.getName());
 						if (gotConn = attemptConnection(commPort)) {
 							break;
 						}
 					}
 				}
 				if (!gotConn) {
 					try {
 						Thread.sleep(5000l);
 					} catch (InterruptedException ie) {
 						// Do nothing
 					}
 				}
 				long timeElapsed = Reader.this.timeSinceLastReceive();
 				long pingTimeout = Cfg.getLong(Cfg.READER_TIMEOUT_DO_PING);
 				long restartTimeout = Cfg.getLong(Cfg.READER_TIMEOUT_RESET);
 				if ((timeElapsed < pingTimeout) && (!doReset)) {
 					try {
 						synchronized (gotAnything) {
 							gotAnything.wait(Math.max(pingTimeout - timeElapsed, 1));
 						}
 					} catch (InterruptedException e) {
 					}
 				} else if ((timeElapsed < restartTimeout) && (!doReset)) {
 					Reader.this.sendPing();
 					try {
 						synchronized (gotAnything) {
 							gotAnything.wait(Math.max(Math.min(restartTimeout - timeElapsed, 250), 1));
 						}
 					} catch (InterruptedException e) {
 					}
 				} else {
 					gotConn = false;
 					if (myPort != null) {
 						if (serialWriter != null) {
 							serialWriter.stop();
 							serialWriter = null;
 						}
 						if (serialReader != null) {
 							serialReader.stop();
 							serialReader = null;
 						}
 						myPort.close();
 						myPort = null;
 					}
 				}
 			}
 			myThread = null;
 		}
 
 		public void resetConnection() {
 			doReset = true;
 			if (myThread != null) {
 				myThread.interrupt();
 			}
 		}
 
 		public void stop() {
 			doShutdown = true;
 			if (myThread != null) {
 				myThread.interrupt();
 			}
 		}
 
 	}
 
 	private boolean attemptConnection(CommPortIdentifier commPort) {
 		try {
 			myPort = (SerialPort) commPort.open(Cfg.getString(Cfg.READER_RXTX_OWNER), Cfg
 					.getInt(Cfg.READER_RXTX_TIMEOUT));
 			myPort.setSerialPortParams(Cfg.getInt(Cfg.READER_BAUD), Cfg.getInt(Cfg.READER_BITS), Cfg
 					.getInt(Cfg.READER_STOP), SerialPort.PARITY_NONE);
 			myPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
 			myPort.setDTR(false);
 			lastReceiveTimeMillis = 0;
 			lastCountTimeMillis = 0;
 			lastEepromTimeMillis = 0;
 			lastStatusTimeMillis = 0;
 			long readyTime = System.currentTimeMillis() + Cfg.getLong(Cfg.READER_DTR_LOWER);
 			logger.info("Opened " + commPort.getName());
 			this.serialWriter = new SerialWriter(myPort.getOutputStream());
 			this.serialReader = new SerialReader(myPort.getName() + " Reader", myPort, commandParser);
 			this.serialWriter.doRun();
 			long timeLeft;
 			while ((timeLeft = readyTime - System.currentTimeMillis()) > 0) {
 				try {
 					Thread.sleep(timeLeft);
 				} catch (InterruptedException ie) {
 					// Do nothing
 				}
 			}
 			myPort.setDTR(true);
 			logger.info("Allowing card reader to initialize");
 			readyTime = System.currentTimeMillis() + Cfg.getLong(Cfg.READER_BOOTLOADER_WAIT);
 			while ((timeLeft = readyTime - System.currentTimeMillis()) > 0) {
 				try {
 					Thread.sleep(timeLeft);
 				} catch (InterruptedException ie) {
 					// Do nothing
 				}
 			}
 			this.serialReader.doRun();
 			this.requestStatus();
 			this.requestEepromDump();
 			this.requestStorableIDsUpdate();
 			long waitTimeout = Cfg.getLong(Cfg.READER_RESPONSE_WAIT);
 			long startTime = System.currentTimeMillis();
 			long stopTime = startTime + waitTimeout;
 			while (lastStatusTimeMillis == 0) {
 				if (stopTime < System.currentTimeMillis()) {
 					throw new IOException("Didn't get timely status response from reader.");
 				}
 				try {
 					synchronized (gotStatus) {
 						gotStatus.wait(Math.max(stopTime - System.currentTimeMillis(),1));
 					}
 				} catch (InterruptedException e) {
 					// Don't worry about it
 				} catch (IllegalArgumentException a) {
 					// We put in a negative time.
 				}
 			}
 			startTime = System.currentTimeMillis();
 			stopTime = startTime + waitTimeout;
 			while (lastEepromTimeMillis == 0) {
 				if (stopTime < System.currentTimeMillis()) {
 					throw new IOException("Didn't get timely eeprom dump from reader.");
 				}
 				try {
 					synchronized (gotEeprom) {
 						gotEeprom.wait(Math.max(stopTime - System.currentTimeMillis(),1));
 					}
 				} catch (InterruptedException e) {
 					// Don't worry about it
 				}
 			}
 			startTime = System.currentTimeMillis();
 			stopTime = startTime + waitTimeout;
 			while (lastCountTimeMillis == 0) {
 				if (stopTime < System.currentTimeMillis()) {
 					throw new IOException("Didn't get timely storable ID count from reader.");
 				}
 				try {
 					synchronized (gotCount) {
 						gotCount.wait(Math.max(stopTime - System.currentTimeMillis(),1));
 					}
 				} catch (InterruptedException e) {
 					// Don't worry about it
 				}
 			}
 			// If we got here, reader is up & running. Let's go home!
 			return true;
 		} catch (ConfigurationRuntimeException e) {
 			logger.error("Unable to get port configuration", e);
 		} catch (PortInUseException e) {
 			logger.error("Port already in use", e);
 		} catch (UnsupportedCommOperationException e) {
 			logger.error("Issue with port", e);
 		} catch (IOException e) {
 			logger.error("Issue with port", e);
 		} catch (TooManyListenersException e) {
 			logger.error("Weird -- too many ears", e);
 		} catch (Exception e) {
 			logger.error("Had an unanticipated issue while attempting to connect.", e);
 		}
 		if (myPort != null) {
 			if (serialWriter != null) {
 				serialWriter.stop();
 				serialWriter = null;
 			}
 			if (serialReader != null) {
 				serialReader.stop();
 				serialReader = null;
 			}
 			myPort.close();
 			myPort = null;
 		}
 		return false;
 	}
 
 	public void resetArduino() {
 		this.myPort.setDTR(false);
 		long t = System.currentTimeMillis() + 1000;
 		long duration;
 		Object waitObj = new Object();
 		synchronized (waitObj) {
 			while ((duration = t - System.currentTimeMillis()) > 0) {
 				try {
 					waitObj.wait(duration);
 				} catch (InterruptedException ie) {
 					// Do nothing
 				}
 			}
 		}
 		this.myPort.setDTR(true);
 		t = System.currentTimeMillis() + 1000;
 		synchronized (waitObj) {
 			while ((duration = t - System.currentTimeMillis()) > 0) {
 				try {
 					waitObj.wait(duration);
 				} catch (InterruptedException ie) {
 					// Do nothing
 				}
 			}
 		}
 		this.requestStatus();
 		this.requestEepromDump();
 		this.requestStorableIDsUpdate();
 	}
 
 	private void updateComPorts() {
 		String[] portValues = Cfg.getStringArray(Cfg.READER_PORTS);
 		Vector<CommPortIdentifier> commPorts = new Vector<CommPortIdentifier>(portValues.length);
 		for (String portValue : portValues) {
 			for (String port : portValue.split(":")) {
 				File f = new File(port);
 				try {
 					port = f.getCanonicalPath();
 				} catch (IOException e) {
 					// Probably a windows COM port. Stupid CP/CMS conventions!
 					logger.info(String.format(
 							"Unable to get canonical path of requested port (%s). Are you on windows?", port), e);
 				}
 				try {
 					CommPortIdentifier i = CommPortIdentifier.getPortIdentifier(port);
 					commPorts.add(i);
 				} catch (NoSuchPortException e) {
 					// A port that we cannot use; Oh well
 					logger.warn(String.format("Unable to use requested port (%s).", port), e);
 				}
 			}
 		}
 		if (commPorts.size() == 0) {
 			logger.error("You don't have any comm ports. I can't do telepathy.");
 		}
 		this.commPorts = new CommPortIdentifier[commPorts.size()];
 		commPorts.toArray(this.commPorts);
 	}
 
 	private class CommandParser extends StoppableQueueThread<String> {
 
 		public CommandParser() {
 			super("Reader Response Parser");
 		}
 
 		private Logger logger = Logger.getLogger(CommandParser.class);
 
 		@Override
 		public void cleanup() {
 			// None required
 		}
 
 		@Override
 		public void process(String data) {
 			// if (data.equals("\u00FF")) {
 			// throw new InterruptedException();
 			// }
 			String payload = data.substring(1);
 			String parts[];
 			ReaderEvent ev;
 			logger.debug("Processing " + data);
 			switch (data.charAt(0)) {
 			case 'D':
 				Reader.logger.warn("FIRMWARE_DEBUG: " + payload);
 				break;
 			case 'S':
 				parts = payload.split(":");
 				fw_name = parts[0];
 				fw_ver_s = parts[1];
 				try {
 					fw_ver = Float.parseFloat(fw_ver_s);
 				} catch (NumberFormatException e) {
 					Reader.logger.warn("Unable to parse firmware version: " + fw_ver_s);
 				}
 				switch (parts[2].charAt(0)) {
 				case 'A':
 					reader_mode = ReaderMode.READER_MODE_ADMIN;
 					break;
 				case 'N':
 					reader_mode = ReaderMode.READER_MODE_NORMAL;
 					break;
 				case 'U':
 					reader_mode = ReaderMode.READER_MODE_UNLOCKED;
 					// TODO: Add code for unlocked mode (what todo?)
 				default:
 					reader_mode = ReaderMode.READER_MODE_UNKNOWN;
 				}
 				Reader.logger.info(reader_mode.toString());
 				switch (parts[2].charAt(1)) {
 				case 'L':
 					reader_strike = ReaderDoorStrike.READER_DOOR_STRIKE_LOCKED;
 					break;
 				case 'U':
 					reader_strike = ReaderDoorStrike.READER_DOOR_STRIKE_UNLOCKED;
 					break;
 				default:
 					reader_strike = ReaderDoorStrike.READER_DOOR_STRIKE_UNKNOWN;
 				}
 				Reader.logger.info(reader_strike.toString());
 				switch (parts[2].charAt(2)) {
 				case 'O':
 					reader_sense = ReaderDoorSense.READER_DOOR_SENSE_OPEN;
 					break;
 				case 'C':
 					reader_sense = ReaderDoorSense.READER_DOOR_SENSE_CLOSED;
 					break;
 				default:
 					reader_sense = ReaderDoorSense.READER_DOOR_SENSE_UNKNOWN;
 				}
 				Reader.logger.info(reader_sense.toString());
 				synchronized (gotStatus) {
 					lastStatusTimeMillis = System.currentTimeMillis();
 					gotStatus.notifyAll();
 				}
 				raiseEvent(new ReaderEvent(Reader.this, ReaderEventType.READER_EVENT_UPDATE, "S"));
 				break;
 			case 'P':
 				serialWriter.queueImmediate("p");
 				break;
 			case 'p':
 				Reader.logger.trace("Card reader pinged alive");
 				break;
 			case 'O':
 				reader_sense = ReaderDoorSense.READER_DOOR_SENSE_OPEN;
 				Reader.logger.info(reader_sense.toString());
 				break;
 			case 'C':
 				reader_sense = ReaderDoorSense.READER_DOOR_SENSE_CLOSED;
 				Reader.logger.info(reader_sense.toString());
 				break;
 			case 'H':
 				logger.debug("Processing " + data + " as " + payload);
 				parts = payload.split("\\|");
 				stored_prefix = parts[0];
 				stored_postfix = parts[1];
 				switch (parts[2].charAt(0)) {
 				case 'A':
 					stored_mode = ReaderMode.READER_MODE_ADMIN;
 					break;
 				case 'N':
 					stored_mode = ReaderMode.READER_MODE_NORMAL;
 					break;
 				default:
 					stored_mode = ReaderMode.READER_MODE_UNKNOWN;
 					break;
 				}
 				door_open_duration = Integer.parseInt(parts[3], 16);
 				id_length = Integer.parseInt(parts[4], 16);
 				stored_ids = new String[Integer.parseInt(parts[5], 16)];
 				for (int i = 0; i < stored_ids.length; i++) {
 					stored_ids[i] = parts[6].substring(i * id_length, ((i + 1) * id_length) - 1);
 				}
 				synchronized (gotEeprom) {
 					lastEepromTimeMillis = System.currentTimeMillis();
 					gotEeprom.notifyAll();
 				}
 				raiseEvent(new ReaderEvent(Reader.this, ReaderEventType.READER_EVENT_UPDATE, "H"));
 				break;
 			case 'i':
 				storable_ids = Integer.parseInt(payload, 16);
 				synchronized (gotCount) {
 					lastCountTimeMillis = System.currentTimeMillis();
 					gotCount.notifyAll();
 				}
 				break;
 			case 'A':
 				Reader.logger.info("Pre-Approved ID entered the lab.");
 				raiseEvent(new ReaderEvent(Reader.this, ReaderEventType.READER_EVENT_ID_NOTICE, payload));
 				break;
 			case '?':
 				ev = new ReaderEvent(Reader.this, Integer.parseInt(payload.substring(0, 2), 16), payload.substring(2));
 				raiseEvent(ev);
 				break;
 			case 'F':
 				Reader.logger.error("EEPROM Storage Issue: " + payload);
 				raiseEvent(new ReaderEvent(Reader.this, ReaderEventType.READER_EVENT_ERROR, payload));
 				lastFail = payload;
 				synchronized (gotStoreResponse) {
 					lastStoreTimeMillis = System.currentTimeMillis();
 					gotStoreResponse.notifyAll();
 				}
 				break;
 			case 'E':
 				lastStore = payload;
 				raiseEvent(new ReaderEvent(Reader.this, ReaderEventType.READER_EVENT_SETTING, payload));
 				synchronized (gotStoreResponse) {
 					lastStoreTimeMillis = System.currentTimeMillis();
 					gotStoreResponse.notifyAll();
 				}
 			}
 			lastReceiveTimeMillis = System.currentTimeMillis();
 			doNotifyAll(gotAnything);
 			logger.trace("done Processing " + data);
 		}
 
 		@Override
 		protected Logger getLogger() {
 			return this.logger;
 		}
 
 		// @Override
 		// protected String getTerminatingData() {
 		// return "\u00FF";
 		// }
 	}
 
 	private static void doNotifyAll(Object obj) {
 		synchronized (obj) {
 			obj.notifyAll();
 		}
 	}
 
 	public void sendAllow(int nonce) {
 		logger.debug(String.format("Allowing nonce %d.", nonce));
 		this.serialWriter.queueImmediate("G" + Integer.toHexString(nonce));
 	}
 
 	public void sendDeny(int nonce) {
 		logger.debug(String.format("Denying nonce %d.", nonce));
 		this.serialWriter.queueImmediate("B" + Integer.toHexString(nonce));
 	}
 
 	public void sendBadLCC(int nonce) {
 		logger.debug(String.format("Bad LCC from nonce %d.", nonce));
 		if (fw_ver > 0.1f) {
 			this.serialWriter.queueImmediate("U" + Integer.toHexString(nonce));
 		} else {
 			this.sendDeny(nonce);
 		}
 	}
 
 	public void sendPing() {
 		this.serialWriter.queueImmediate("P");
 	}
 
 	public void requestStatus() {
 		this.serialWriter.queueImmediate("S");
 	}
 
 	public void requestEepromDump() {
 		this.serialWriter.queueImmediate("H");
 	}
 
 	public void requestStorableIDsUpdate() {
 		this.serialWriter.queueImmediate("i");
 	}
 
 	public void setIdLength(int length) {
 		this.serialWriter.queueImmediate("L" + Integer.toHexString(length));
 		this.requestEepromDump();
 	}
 
 	public void setIdPrefix(String prefix) {
 		this.serialWriter.queueImmediate("R" + prefix);
 		this.requestEepromDump();
 	}
 
 	public void setIdPostfix(String postfix) {
 		this.serialWriter.queueImmediate("O" + postfix);
 		this.requestEepromDump();
 	}
 
 	public void setMode(ReaderMode mode, boolean saveToEeprom) {
 		if (mode == ReaderMode.READER_MODE_ADMIN) {
 			if (saveToEeprom) {
 				this.serialWriter.queueImmediate("a");
 			} else {
 				this.serialWriter.queueImmediate("A");
 			}
 		} else if (mode == ReaderMode.READER_MODE_NORMAL) {
 			if (saveToEeprom) {
 				this.serialWriter.queueImmediate("n");
 			} else {
 				this.serialWriter.queueImmediate("N");
 			}
 		} else if (mode == ReaderMode.READER_MODE_UNLOCKED) {
 			if (fw_ver >= 0.2) {
 				if (saveToEeprom) {
 					throw new IllegalArgumentException("Reader unlocks cannot be saved to EEPROM.");
 				} else {
 					this.serialWriter.queueImmediate("u");
 				}
 			} else {
 				throw new IllegalArgumentException("Reader firmware is too old to be unlocked this way.");
 			}
 		} else {
 			throw new IllegalArgumentException("I cannot set the reader to that mode.");
 		}
 	}
 
 	public void programIds(String[] ids) throws IOException {
 		if (ids.length > this.storable_ids) {
 			throw new IllegalArgumentException("Too many IDs to program");
 		}
 		try {
 			this.settingWaitResponse("C" + Integer.toHexString(ids.length));
 			for (String string : ids) {
 				this.settingWaitResponse("I" + string);
 			}
 			this.settingWaitResponse("F");
 		} catch (Exception e) {
 			logger.error("Unable to store IDs.", e);
 			this.resetArduino();
 			throw new IOException(e);
 		}
 	}
 
 	public synchronized void settingWaitResponse(String setting) throws IOException {
 		this.settingWaitResponse(setting, Cfg.getLong(Cfg.READER_RESPONSE_WAIT));
 	}
 
 	public synchronized void settingWaitResponse(String setting, long timeout) throws IOException {
 		ResponseWaiter rW = new ResponseWaiter(setting);
 		this.serialWriter.queue(setting);
 		try {
 			rW.waitForResponse(timeout);
 		} catch (TimeoutException e) {
 			logger.warn(e.getMessage());
 			throw new IOException("Timed out waiting for reader response", e);
 		}
 	}
 
 	/**
 	 * Syncronous command sending
 	 * 
 	 * @author isaac
 	 * 
 	 */
 	private class ResponseWaiter implements ReaderEventListener {
 
 		public final String command;
 		private String failError = null;
 		private boolean endSuccess = false;
 
 		public ResponseWaiter(String command) {
 			this.command = command;
 		}
 
 		public void waitForResponse(long timeout) throws IOException, TimeoutException {
 			long waitEnd = System.currentTimeMillis() + timeout;
 			long waitTime = timeout;
 			Reader.this.addEventListener(this);
 			while ((failError == null) && (!endSuccess) && (waitTime > 0)) {
 				try {
 					synchronized (command) {
 						command.wait(waitTime);
 					}
 				} catch (InterruptedException e) {
 					// Do nothing
 				}
 				// wait() sometimes spuriously ends
 				waitTime = Math.max(System.currentTimeMillis() - waitEnd, 0);
 			}
 			Reader.this.removeEventListener(this);
 			if (failError != null) {
 				throw new IOException(failError);
 			} else if (!endSuccess) {
 				if (fw_ver >= 0.2) {
 					throw new TimeoutException("Timed out waiting on echo for: " + command);
 					// otherwise, it was probably accepted
 				}
 			}
 		}
 
 		@Override
 		public void readerEvent(ReaderEvent ev) {
 			if (ev.type == ReaderEventType.READER_EVENT_ERROR) {
 				if (ev.data.startsWith(command)) {
 					synchronized (command) {
 						failError = ev.data;
 						command.notify();
 					}
 				}
 			} else if (ev.type == ReaderEventType.READER_EVENT_SETTING) {
 				if (ev.data.startsWith(command)) {
 					synchronized (command) {
 						endSuccess = true;
 						command.notify();
 					}
 				}
 			}
 		}
 	}
 
 	public String getFwName() {
 		return fw_name;
 	}
 
 	public String getFwVerString() {
 		return fw_ver_s;
 	}
 
 	public float getFwVer() {
 		return fw_ver;
 	}
 
 	public int getStorableIDCount() {
 		return storable_ids;
 	}
 
 	public String[] getStoredIds() {
 		return stored_ids.clone();
 	}
 
 	/**
 	 * Gets mode currently in effect (Reader RAM)
 	 * 
 	 * @return card reader mode in effect
 	 */
 	public ReaderMode getMode() {
 		return reader_mode;
 	}
 
 	public ReaderDoorStrike getStrikeStatus() {
 		return reader_strike;
 	}
 
 	public ReaderDoorSense getDoorSense() {
 		return reader_sense;
 	}
 
 	/**
 	 * Get ID prefix stored in EEPROM
 	 * 
 	 * @return
 	 */
 	public String getPrefix() {
 		return stored_prefix;
 	}
 
 	/**
 	 * Get ID postfix stored in EEPROM
 	 * 
 	 * @return stored card reader mode (takes effect on reader reboot/client
 	 *         reconnect)
 	 */
 	public String getPostfix() {
 		return stored_postfix;
 	}
 
 	/**
 	 * Gets mode stored in EEPROM (not necessarily currently in effect)
 	 * 
 	 * @return
 	 */
 	public ReaderMode getStoredMode() {
 		return stored_mode;
 	}
 
 	/**
 	 * Get door open duration stored in EEPROM
 	 * 
 	 * @return Door open duration in seconds
 	 */
 	public int getDoorOpenDuration() {
 		return door_open_duration;
 	}
 
 	public long getMillisSinceLastReceipt() {
 		return System.currentTimeMillis() - this.lastReceiveTimeMillis;
 	}
 
 }
