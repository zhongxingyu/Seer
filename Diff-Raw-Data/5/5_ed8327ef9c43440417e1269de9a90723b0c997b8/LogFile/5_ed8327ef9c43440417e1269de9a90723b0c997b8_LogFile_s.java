 package com.github.davidmoten.logan;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.input.Tailer;
 import org.apache.commons.io.input.TailerListener;
 
 import com.google.common.annotations.VisibleForTesting;
 
 /**
  * Starts a thread using a given {@link ExecutorService} to load all logs from a
  * {@link File} and then monitor the file for new lines and load them too as
  * they arrive.
  * 
  * @author dave
  * 
  */
 public class LogFile {
 
 	private static Logger log = Logger.getLogger(LogFile.class.getName());
 
 	private static AtomicLong counter = new AtomicLong();
 
 	private final File file;
 	private final long checkIntervalMs;
 	private Runnable tailer;
 	private final LogParser parser;
 	private final ExecutorService executor;
 	private final String source;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param file
 	 * @param checkIntervalMs
 	 * @param parser
 	 * @param executor
 	 */
 	public LogFile(File file, String source, long checkIntervalMs,
 			LogParser parser, ExecutorService executor) {
 		this.file = file;
 		this.source = source;
 		this.checkIntervalMs = checkIntervalMs;
 		this.parser = parser;
 		this.executor = executor;
 		createFileIfDoesntExist(file);
 	}
 
 	@VisibleForTesting
 	static void createFileIfDoesntExist(File file) {
 		if (!file.exists())
 			try {
 				if (!file.createNewFile())
 					throw new RuntimeException("could not create file: " + file);
 			} catch (IOException e) {
 				throw new RuntimeException("could not create file: " + file, e);
 			}
 	}
 
 	private static int BUFFER_SIZE = 64 * 4096;
 
 	/**
 	 * Starts a thread that tails a file from the start and reports extracted
 	 * info from the lines to the database.
 	 * 
 	 * @param factory
 	 */
 	public void tail(Data data, boolean follow) {
 
 		TailerListener listener = createListener(data);
 
 		if (follow)
 			// tail from the start of the file and watch for future changes
 			tailer = new Tailer(file, listener, checkIntervalMs, false,
 					BUFFER_SIZE);
 
 		else
 			tailer = new MyTailer(file, listener, BUFFER_SIZE);
 
 		// start in separate thread
 		log.info("starting tailer thread");
 		executor.execute(tailer);
 	}
 
 	/**
 	 * Stops the tailer (and thus its thread).
 	 */
 	public void stop() {
 		if (tailer != null)
 			if (tailer instanceof Tailer)
 				((Tailer) tailer).stop();
 			else if (tailer instanceof MyTailer)
 				((MyTailer) tailer).stop();
 	}
 
 	private synchronized static void incrementCounter() {
 		if (counter.incrementAndGet() % 1000 == 0)
			log.info(counter + " log lines persisted");
 	}
 
 	private TailerListener createListener(final Data data) {
 		return new TailerListener() {
 			private final Data db = data;
 
 			@Override
 			public void fileNotFound() {
 				log.warning("file not found");
 			}
 
 			@Override
 			public void fileRotated() {
 				log.info("file rotated");
 			}
 
 			@Override
 			public synchronized void handle(String line) {
 				log.fine(new StringBuilder().append(source).append(":")
 						.append(line).toString());
 				try {
 					LogEntry entry = parser.parse(source, line);
 					if (entry != null) {
 						db.add(entry);
						log.fine("persisted");
 						incrementCounter();
 					}
 				} catch (Throwable e) {
 					log.log(Level.WARNING, e.getMessage(), e);
 				}
 			}
 
 			@Override
 			public void handle(Exception e) {
 				log.log(Level.WARNING, "handle exception " + e.getMessage(), e);
 			}
 
 			@Override
 			public void init(Tailer tailer) {
 				log.info("init");
 			}
 		};
 	}
 }
