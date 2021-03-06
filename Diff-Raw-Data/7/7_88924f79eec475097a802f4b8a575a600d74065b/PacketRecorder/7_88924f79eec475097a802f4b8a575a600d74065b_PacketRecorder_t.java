 /*----------------------------------------------------------------------------------------
  * This file is part of the
  * WSN visualization framework SpyGlass. Copyright (C) 2004-2007 by the SwarmNet (www.swarmnet.de)
  * project SpyGlass is free software; you can redistribute it and/or modify it under the terms of
  * the BSD License. Refer to spyglass-licence.txt file in the root of the SpyGlass source tree for
  * further details.
  * ---------------------------------------------------------------------------------------
  */
 package de.uniluebeck.itm.spyglass.core;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 
 import de.uniluebeck.itm.spyglass.gateway.FileReaderGateway;
 import de.uniluebeck.itm.spyglass.gateway.Gateway;
 import de.uniluebeck.itm.spyglass.packet.IShellToSpyGlassPacketBroker;
 import de.uniluebeck.itm.spyglass.packet.PacketFactory;
 import de.uniluebeck.itm.spyglass.packet.PacketReader;
 import de.uniluebeck.itm.spyglass.packet.SpyglassPacket;
 import de.uniluebeck.itm.spyglass.packet.SpyglassPacketException;
 import de.uniluebeck.itm.spyglass.util.SpyglassLoggerFactory;
 
 // --------------------------------------------------------------------------------
 /**
  * Instances of this class can record provided packages to files located in the local file system.
  * The recorded packages can be played back, too.<br>
  * Since it extends the {@link PacketReader} it can be used as ordinary {@link PacketReader} with
  * extended functionality within SpyGlass.
  * 
  * @author Sebastian Ebers
  * @see SpyglassPacket
  */
 public class PacketRecorder extends IShellToSpyGlassPacketBroker {
 
 	// ----------------------------------------------------------------
 	/** Object to log status and error messages within the PacketRecorder */
 	static final Logger log = SpyglassLoggerFactory.getLogger(PacketRecorder.class);
 
 	// ----------------------------------------------------------------
 	/**
 	 * Indicates whether the incoming packages are currently recorded
 	 */
 	private boolean record = false;
 
 	// ----------------------------------------------------------------
 	/**
 	 * The queue where packets are dropped by the packet dispatcher and which is maintained
 	 * concurrently
 	 */
 	private ConcurrentLinkedQueue<SpyglassPacket> recordingQueue = null;
 
 	// ----------------------------------------------------------------
 	/** The thread used to consume packets from the packet queue */
 	private Thread packetConsumerThread = null;
 
 	// ----------------------------------------------------------------
 	/** The path to the file the packages are recorder */
 	private String recordFileString = null;
 
 	// ----------------------------------------------------------------
 	/** The path to the directory where the record files are located */
 	private final String recordDirectory = new File("./record/").getAbsoluteFile().toString();
 
 	// ----------------------------------------------------------------
 	/** The time stamp of the last packed delivery when reading from a file */
 	private long lastPlaybackPacketTimestamp = -1;
 
 	// ----------------------------------------------------------------
 	/** The time stamp of the last packed read from a file */
 	private long lastPlaybackPacketDeliveryTimestamp = -1;
 
 	// ----------------------------------------------------------------
 	/**
 	 * The string to the file which was last selected by the user for recording (this will be needed
 	 * to check whether the user has to be asked to append content)
 	 */
 	private String lastSelectedRecordFilePath = null;
 
 	// --------------------------------------------------------------------------------
 	/** Object to secure packet reading and manipulations at the input source */
 	private Object inputStreamMutex = new Object();
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Constructor
 	 */
 	public PacketRecorder() {
 		recordingQueue = new ConcurrentLinkedQueue<SpyglassPacket>();
 		readFromFile = false;
 		delayMillies = 0;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Activates or deactivates the recording mode
 	 * 
 	 * @param enable
 	 *            if <code>true</code> the recording mode will be activated, if <code>false</code>
 	 *            the recording mode will be deactivated
 	 * @return <code>true</code> if the recording mode was activated, <code>false</code> otherwise
 	 * 
 	 */
 	public boolean setRecording(final boolean enable) {
 
 		if (enable) {
 
 			// if no record file was selected, let the user select one and the user denies to
 			// select a file, the recording will be aborted
 
 			if (getRecordFilePath() == null) {
 				log.info("No file selected to be used to record the packages.\r\n The recording will be aborted!");
 				this.record = false;
 			}
 
 			if (recordFileString != null) {
 
 				final File file = new File(recordFileString);
 
 				if (isWritable(file)) {
 
 					// Check if the file already exists and if it differs from the previous chosen
 					// one.
 					// If so, the user can decide to append the information, to overwrite the file
 					// or to abort the recording
 					final int result = checkAppend(file);
 
 					// the user decided to abort selecting a file and nothing is to be done
 					if (result == 2) {
 						// in case a recording process is already running it will not be aborted
 						return this.record;
 					}
 
 					startPacketConsumerThread((result == 0));
 					this.record = true;
 				}
 			}
 
 		}
 
 		// if recording is to be disabled, stop the thread and clean up
 		else {
 
 			this.record = false;
 			if ((packetConsumerThread != null) && !packetConsumerThread.isInterrupted()) {
 				packetConsumerThread.interrupt();
 			}
 			recordingQueue.clear();
 			packetConsumerThread = null;
 		}
 
 		return this.record;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Returns if a file is writable
 	 * 
 	 * @param file
 	 *            the file to be checked
 	 * @return <code>true</code> if the file is writable, <code>false</code> otherwise
 	 */
 	private boolean isWritable(final File file) {
 		// check if it is a file at all (this should always be the case but we want to make sure)
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (final IOException e) {
				log.error("The recording file could not be created", e);
			}
		}
 		if (!file.isFile()) {
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					MessageDialog.openError(null, "Invalid file", "No valid file for recording specified.\r\nPlease choose a different one");
 				}
 			});
 			return false;
 		}
 
 		// if it is a file, it has to be writable
 		else if (!file.canWrite()) {
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					MessageDialog.openError(null, "Write protection activated",
 							"The file cannot be written.\r\nPlease disable write protection or choose a different file!");
 				}
 			});
 			return false;
 		}
 		return true;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Checks if the selected recording file already exists and if it is was already selected. If it
 	 * was not selected but it already exists, a dialog window will show up to ask the user whether
 	 * the content is to be appended to the file or not. Alternatively, the user can abort selecting
 	 * the file at all.
 	 * 
 	 * @param file
 	 *            the file to be checked
 	 * @return <ul>
 	 *         <li><tt>0</tt> if the content is to be appended</li>
 	 *         <li><tt>1</tt> if the files content is to be replaced with the new one</li>
 	 *         <li><tt>2</tt> if the selection is to be aborted</li>
 	 *         </ul>
 	 */
 	private int checkAppend(final File file) {
 		int result = 0;
 
 		// Check if the file already exists and if it differs from the previous chosen one.
 		// If so, the user can decide to append the information, to overwrite the file or to
 		// abort the recording
 		if (file.exists() && (file.length() > 0) && ((lastSelectedRecordFilePath == null) || !recordFileString.equals(lastSelectedRecordFilePath))) {
 			result = new MessageDialog(Display.getCurrent().getActiveShell(), "Append or Replace", null,
 					"The file already exists. Shall the new information be appended or shall the file be replaced?", SWT.ICON_QUESTION, new String[] {
 							"Append", "Replace", "Abort" }, 0).open();
 			lastSelectedRecordFilePath = recordFileString;
 		}
 		return result;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Starts the thread which consumes the packets which have been pushed into the recording queue
 	 * previously
 	 */
 	private void startPacketConsumerThread(final boolean append) {
 
 		packetConsumerThread = new Thread() {
 			@SuppressWarnings("synthetic-access")
 			@Override
 			public void run() {
 				try {
 
 					FileOutputStream recordFileWriter = new FileOutputStream(getRecordFileString(), append);
 					final ConcurrentLinkedQueue<SpyglassPacket> queue = getPacketQueue();
 					SpyglassPacket packet = null;
 					while (!isInterrupted()) {
 						byte[] data = null;
 						synchronized (queue) {
 
 							if (queue.isEmpty()) {
 								try {
 									queue.wait();
 								} catch (final InterruptedException e) {
 									log
 											.debug("The packet recorder's packet consumer thread was interrupted while waiting for a notification of the arrival of a new packet");
 									interrupt();
 								}
 							}
 							packet = queue.poll();
 
 						}
 						if (packet != null) {
 							data = packet.serialize();
 							recordFileWriter.write(data.length);
 							recordFileWriter.write(data);
 							recordFileWriter.flush();
 						}
 					}
 					queue.clear();
 					recordFileWriter.close();
 					recordFileWriter = null;
 				} catch (final IOException e) {
 					log.error("Error while recording a packet", e);
 				}
 			}
 		};
 		packetConsumerThread.start();
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Returns the path to the file which is used for recording the input
 	 * 
 	 * @return the path to the file which is used for recording the input
 	 */
 	private String getRecordFileString() {
 		return recordFileString;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Returns the queue where the packages which are to be recorded are temporarily stored
 	 * 
 	 * @return the queue where the packages which are to be recorded are temporarily stored
 	 */
 	private ConcurrentLinkedQueue<SpyglassPacket> getPacketQueue() {
 		return recordingQueue;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Sets the file which is used for recording the input
 	 * 
 	 * @param path
 	 *            the path to the file which is used for recording the input
 	 */
 	public void setRecordFile(final String path) {
 		if (path != null) {
 			// if the recording is currently in process, stop it, replace the output file and
 			// restart the recording again
 			if (isRecord()) {
 				setRecording(false);
 				recordFileString = path;
 				setRecording(true);
 			} else {
 				// otherwise just set the output file
 				recordFileString = path;
 			}
 		}
 	}
 
 	// --------------------------------------------------------------------------
 	/**
 	 * Returns the file where the recoded data has to be saved.<br>
 	 * If no file was specified, yet, a dialog will be opened to let the user choose one.
 	 */
 	private String getRecordFilePath() {
 		if (recordFileString == null) {
 			setRectordFile();
 		}
 		return recordFileString;
 	}
 
 	// --------------------------------------------------------------------------
 	/**
 	 * Opens a dialog to select a file which will be used to store the recorded packets.
 	 */
 	public void setRectordFile() {
 		setRecordFile(selectRecodingFileByUser());
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Opens a message dialog for the user to select the recording file
 	 * 
 	 * @return the path to the file selected by the user
 	 */
 	private String selectRecodingFileByUser() {
 		final FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
 		fd.setFilterExtensions(new String[] { "*.rec" });
 		fd.setFilterPath(recordDirectory);
 		String path = null;
 		boolean conflictingFileSelected = false;
 		do {
 			path = fd.open();
 			if (path != null) {
 				if (!path.endsWith(".rec")) {
 					path += ".rec";
 				}
 
 				conflictingFileSelected = equalsPlaybackFilePath(path);
 				if (isReadFromFile() && conflictingFileSelected) {
 
 					MessageDialog.openError(null, "The file is already in use",
 							"Sorry, the chosen file is already in use for playback. please choose a different one ");
 
 				}
 
 				// if a file conflict was detected but the readFromFile mode is disabled, set the
 				// playback file to null
 				else if (!isReadFromFile() && equalsPlaybackFilePath(path)) {
 					((FileReaderGateway) getGateway()).setFile(null);
 					conflictingFileSelected = false;
 				}
 			}
 		} while (conflictingFileSelected);
 		return path;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Returns whether a provided path equals the one of the playback file
 	 * 
 	 * @param path
 	 *            a path e.g. to the recording file
 	 * @return <code>true</code> if the provided path equals the one of the playback file
 	 */
 	private boolean equalsPlaybackFilePath(final String path) {
 		File in = null;
 		if ((getGateway() instanceof FileReaderGateway) && (path != null)) {
 			in = ((FileReaderGateway) getGateway()).getFile();
 			return ((in != null) && in.equals(new File(path)));
 		}
 		return false;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Records a provided packet in recording mode, discards them in normal mode
 	 * 
 	 * @param packet
 	 *            the packet to be recorded
 	 */
 	private void handlePacket(final SpyglassPacket packet) {
 
 		if (packet != null) {
 
 			if (!record) {
 				return;
 			}
 			synchronized (recordingQueue) {
 				recordingQueue.offer(packet);
 				recordingQueue.notify();
 			}
 		}
 	}
 
 	// --------------------------------------------------------------------------------
 	@Override
 	public void push(final SpyglassPacket packet) {
 		// the packets could be pushed in the super classes queue but when the readFromFile time
 		// lasts to long, an out of memory exception might occur
 		if (!readFromFile) {
 			handlePacket(packet);
 			super.push(packet);
 		}
 	}
 
 	// --------------------------------------------------------------------------------
 	@Override
 	public SpyglassPacket getNextPacket() throws SpyglassPacketException, InterruptedException {
 
 		SpyglassPacket packet = null;
 
 		// this do while is needed, to guarantee that valid packets are returned
 		do {
 			// if a file was chosen as input source ...
 			if (readFromFile) {
 				packet = getNextPlaybackPacket();
 			} else {
 				packet = super.getNextPacket();
 			}
 		} while (packet == null);
 
 		return packet;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Returns the next packet which is found in the currently selected playback file
 	 * 
 	 * @return a {@link SpyglassPacket}
 	 * @throws SpyglassPacketException
 	 * @throws InterruptedException
 	 */
 	private SpyglassPacket getNextPlaybackPacket() throws SpyglassPacketException, InterruptedException {
 
 		SpyglassPacket packet = null;
 
 		// get a mutual exclusion lock which will prevent the input stream to be accessed externally
 		synchronized (inputStreamMutex) {
 
 			// get the input stream which will deliver the packets
 			InputStream playbackFileReader = null;
 			if ((getGateway() == null) || ((playbackFileReader = getGateway().getInputStream()) == null)) {
 				// initialize it by instructions of the user since it was not set previously
 				setPlayBackFile(selectPlayBackFileByUser());
 			}
 
 			// if the input stream is still not available generate an error message
 			if (playbackFileReader == null) {
 				log.error("The gateway which delivers the packets was not initialized correcly.\r\n" + "Please specify the input source.");
 				return null;
 			}
 
 			packet = getNextPacketFromInputStream(playbackFileReader);
 		}
 
 		// Hold back the packet at least for delayMillis
 		final long now = System.currentTimeMillis();
 		final long sleep1 = delayMillies - (now - lastPlaybackPacketDeliveryTimestamp);
 		if (sleep1 > 0) {
 			Thread.sleep(sleep1);
 		}
 		if (packet != null) {
 			final long currentPacketTimestamp = packet.getTime().getMillis();
 			final long alreadyWaited = System.currentTimeMillis() - lastPlaybackPacketDeliveryTimestamp;
 			elapsPacketTimestampDifference(currentPacketTimestamp, alreadyWaited);
 
 			// this is done to enable the user to cut the packet stream read from a file
 			handlePacket(packet);
 			lastPlaybackPacketDeliveryTimestamp = System.currentTimeMillis();
 		}
 
 		return packet;
 
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Sleeps until the time difference between the last packet which was delivered and the current
 	 * one's is elapsed. The time difference is computed by comparing the two packet's time stamps.<br>
 	 * If the time stamp is smaller than the one of the last packet, it will not be stored.
 	 * Technically, the 'last' packet may not be the last packet but the one which had the biggest
 	 * time stamp from the ones which were already delivered!
 	 * 
 	 * @param currentPacketTimestamp
 	 *            the currently processed packet's time stamp
 	 * @param alreadyWaited
 	 *            the time which already elapsed
 	 * @throws InterruptedException
 	 */
 	private void elapsPacketTimestampDifference(final long currentPacketTimestamp, final long alreadyWaited) throws InterruptedException {
 		if (lastPlaybackPacketTimestamp != -1) {
 			final long packetDiff = currentPacketTimestamp - lastPlaybackPacketTimestamp;
 			// may be the packets where received in the wrong order...
 			if (packetDiff > 0) {
 				if (packetDiff - alreadyWaited > 0) {
 					Thread.sleep(packetDiff - alreadyWaited);
 				}
 				lastPlaybackPacketTimestamp = currentPacketTimestamp;
 			}
 		}
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Returns the next {@link SpyglassPacket} to be found in the provided input stream
 	 * 
 	 * @param playbackFileReader
 	 *            an input streams containing {@link SpyglassPacket}s
 	 * @return the next {@link SpyglassPacket} to be found in the provided input stream
 	 * @throws SpyglassPacketException
 	 * @throws InterruptedException
 	 */
 	private SpyglassPacket getNextPacketFromInputStream(final InputStream playbackFileReader) throws SpyglassPacketException, InterruptedException {
 
 		SpyglassPacket packet = null;
 		try {
 			final int next;
 			byte[] packetData;
 			if ((next = playbackFileReader.read()) != -1) {
 				packetData = new byte[next];
 				playbackFileReader.read(packetData);
 				packet = PacketFactory.createInstance(packetData);
 			} else {
 				Display.getDefault().syncExec(new Runnable() {
 					@Override
 					public void run() {
 						MessageDialog.openInformation(null, "No more data", "The end of the playback file was reached!");
 					}
 				});
 				inputStreamMutex.wait();
 			}
 
 		} catch (final IOException e) {
 			log.error("Error while reading a new packet...", e);
 		}
 		return packet;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Returns whether the currently provided files are read from a file or handed over from iShell
 	 * 
 	 * @return <code>true</code> if the currently provided files are read from a file or handed over
 	 *         from iShell
 	 */
 	@Override
 	public boolean isReadFromFile() {
 		return readFromFile;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Enables or disables the readFromFile mode
 	 * 
 	 * @param enable
 	 *            <code>true</code> if the readFromFile mode is to be enabled, <code>false</code>
 	 *            otherwise
 	 * @return <code>true</code> if the readFromFile mode is enabled, <code>false</code> otherwise
 	 */
 	@Override
 	public boolean setReadFromFile(final boolean enable) {
 		if (!enable) {
 			readFromFile = false;
 		} else {
 
 			if (enable && (getPlaybackFile() == null)) {
 				// let the user select a new playback file
 				setPlayBackFile(selectPlayBackFileByUser());
 			} else {
 				readFromFile = true;
 			}
 		}
 		synchronized (inputStreamMutex) {
 			inputStreamMutex.notifyAll();
 		}
 		return readFromFile;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Returns the file currently selected for readFromFile
 	 * 
 	 * @return the file currently selected for readFromFile
 	 */
 	private File getPlaybackFile() {
 		if (getGateway() instanceof FileReaderGateway) {
 			return ((FileReaderGateway) getGateway()).getFile();
 		}
 		return null;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Returns whether a provided path equals the one of the recording file
 	 * 
 	 * @param path
 	 *            a path e.g. to the playback file
 	 * @return <code>true</code> if the provided path equals the one of the recording file
 	 */
 	private boolean equalsRecordingFilePath(final String path) {
 		if ((path != null) && (recordFileString != null)) {
 			return (new File(path).equals(new File(recordFileString)));
 		}
 		return false;
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Returns whether the incoming packets are currently recorded
 	 * 
 	 * @return <code>true</code> if the incoming packets are currently recorded
 	 */
 	public boolean isRecord() {
 		return record;
 	}
 
 	// --------------------------------------------------------------------------
 	/**
 	 * Sets the file to be used to readFromFile the previously recorded packages.
 	 * 
 	 * @param path
 	 *            the path to the file
 	 * @return <code>true</code> if a readFromFile file was set successfully
 	 */
 	public boolean setPlayBackFile(final String path) {
 
 		if (path != null) {
 
 			boolean isConflictingFileSelected = equalsRecordingFilePath(path);
 			if (isRecord() && isConflictingFileSelected) {
 				Display.getDefault().syncExec(new Runnable() {
 					@Override
 					public void run() {
 						MessageDialog.openError(null, "The file is already in use",
 								"Sorry, the chosen file is already in use for recording.\r\nPlayback will not be started.");
 					}
 				});
 
 			}
 			// if a file conflict was detected but the recording mode is disabled, set the
 			// recording file to null
 			else if (isConflictingFileSelected && !isRecord()) {
 				recordFileString = null;
 				isConflictingFileSelected = false;
 			}
 
 			if (!isConflictingFileSelected) {
 
 				// get a mutex lock which will prevent the input stream to be accessed externally
 				synchronized (inputStreamMutex) {
 					// check whether the current Gateway is capable of processing a file
 					Gateway gw = getGateway();
 					if ((gw == null) || (!(gw instanceof FileReaderGateway))) {
 
 						// if not, create a usable gateway and
 						final FileReaderGateway frgw = new FileReaderGateway();
 						frgw.setFile(new File(path));
 						setGateway(frgw);
 						gw = frgw;
 					}
 
 					((FileReaderGateway) gw).setFile(new File(path));
 
 					setReadFromFile(getGateway().getInputStream() != null);
 				}
 			}
 		} else {
 			setReadFromFile(false);
 		}
 		return isReadFromFile();
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * Opens a message dialog for the user to select the readFromFile file
 	 * 
 	 * @return the path to the file selected by the user
 	 */
 	private String selectPlayBackFileByUser() {
 		final FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
 		fd.setFilterExtensions(new String[] { "*.rec" });
 		fd.setFilterPath(recordDirectory);
 		boolean isConflictingFileSelected = false;
 		String path;
 		do {
 			path = fd.open();
 			isConflictingFileSelected = equalsRecordingFilePath(path);
 			if (isRecord() && isConflictingFileSelected) {
 				MessageDialog.openError(null, "The file is already in use",
 						"Sorry, the chosen file is already in use for recording. Please choose a diferent one.");
 
 			}
 			// if a file conflict was detected but the recording mode is disabled, set the
 			// recording file to null
 			else if (isConflictingFileSelected && !isRecord()) {
 				recordFileString = null;
 				isConflictingFileSelected = false;
 			}
 		} while (isConflictingFileSelected);
 		return path;
 	}
 
 	// --------------------------------------------------------------------------------
 	@Override
 	public void reset() throws IOException {
 		log.info("Reset requested");
 		delayMillies = 0;
 		super.reset();
 		synchronized (inputStreamMutex) {
 
 			if (isReadFromFile() && !isRecord()) {
 				MessageDialog.openInformation(null, "Reset Playbak", "The playback will be started from the beginning of the file.");
 			}
 
 			else if (isReadFromFile() && isRecord()) {
 				setRecording(!MessageDialog.openQuestion(null, "Reset Recorder", "The playback will be started from the beginning of the file.\r\n"
 						+ "Do you want to disable recording?"));
 			}
 
 			else if (isRecord()) {
 				setRecording(!MessageDialog.openQuestion(null, "Reset Recorder", "Do you want to disable recording?"));
 			}
 
 			// setPlayback(false);
 			recordFileString = null;
 			lastPlaybackPacketDeliveryTimestamp = -1;
 			lastPlaybackPacketTimestamp = -1;
 			lastSelectedRecordFilePath = null;
 			getPacketQueue().clear();
 			Gateway gw = null;
 			if ((gw = getGateway()) != null) {
 				if (gw instanceof FileReaderGateway) {
 					((FileReaderGateway) gw).reset();
 				}
 			}
 			inputStreamMutex.notifyAll();
 		}
 	}
 
 	// --------------------------------------------------------------------------------
 	/**
 	 * @param args
 	 * @throws IOException
 	 * @throws SpyglassPacketException
 	 * @throws InterruptedException
 	 */
 	public static void main(final String[] args) throws IOException, SpyglassPacketException, InterruptedException {
 		final PacketRecorder recorder = new PacketRecorder();
 		recorder.setDelayMillies(0);
 		recorder.setPlayBackFile("record/record1.rec");
 		SpyglassPacket packet = null;
 		int i = 0;
 		while ((packet = recorder.getNextPacket()) != null) {
 			log.debug(packet.getSenderId() + " Nr: " + (++i));
 		}
 	}
 
 }
