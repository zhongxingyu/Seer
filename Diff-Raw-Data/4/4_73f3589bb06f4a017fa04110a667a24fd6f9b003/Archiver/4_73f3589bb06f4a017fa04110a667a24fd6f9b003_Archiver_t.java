 package org.genericsystem.core;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.nio.channels.OverlappingFileLockException;
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.NavigableMap;
 import java.util.NavigableSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 /**
  * @author Nicolas Feybesse
  * 
  */
 public class Archiver {
 
 	// private static final Logger log = LoggerFactory.getLogger(Archiver.class);

 	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
 
 	private Engine engine;
 	private File directory;
 	private FileOutputStream lockFile;
 
 	public Archiver(Engine engine, String directoryPath) {
 		this.engine = engine;
 		prepareAndLockDirectory(directoryPath);
 		ObjectInputStream inputStream = getInputStream();
 		if (inputStream != null)
 			new SnapshotLoader(inputStream).loadSnapshot();
 		else
 			((EngineImpl) engine).restoreEngine();
 	}
 
 	private void prepareAndLockDirectory(String directoryPath) {
 		if (directoryPath == null)
 			return;
 		File directory = new File(directoryPath);
 		if (directory.exists()) {
 			if (!directory.isDirectory())
 				throw new IllegalStateException("Datasource path : " + directoryPath + " is not a directory");
 		} else if (!directory.mkdirs())
 			throw new IllegalStateException("Can't make directory : " + directoryPath);
 		try {
 			lockFile = new FileOutputStream(directoryPath + File.separator + Statics.LOCK_FILE_NAME);
 			lockFile.getChannel().tryLock();
 			this.directory = directory;
 		} catch (OverlappingFileLockException e) {
 			throw new IllegalStateException("Locked directory : " + directoryPath);
 		} catch (Exception e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	private ObjectInputStream getInputStream() {
 		if (lockFile != null) {
 			NavigableMap<Long, File> snapshotsMap = snapshotsMap();
 			if (!snapshotsMap.isEmpty())
 				try {
 					return new ObjectInputStream(new SnapshotZipInputStream(directory.getAbsolutePath(), Statics.getFilename(snapshotsMap.lastKey())));
 				} catch (IOException e) {
 					throw new IllegalStateException(e);
 				}
 		}
 		return null;
 	}
 
 	private NavigableMap<Long, File> snapshotsMap() {
 		NavigableMap<Long, File> snapshotsMap = new TreeMap<Long, File>();
 		for (File file : directory.listFiles()) {
 			String filename = file.getName();
 			if (!file.isDirectory() && filename.endsWith(Statics.ZIP_EXTENSION)) {
 				filename = filename.substring(0, filename.length() - Statics.ZIP_EXTENSION.length());
 				if (filename.matches(Statics.MATCHING_REGEX))
 					try {
 						snapshotsMap.put(getTimestamp(filename), file);
 					} catch (ParseException pe) {
 						throw new IllegalStateException(pe);
 					}
 			}
 		}
 		return snapshotsMap;
 	}
 
 	private void doSnapshot(Engine engine) {
 		// log.info("START SNAPSHOT");
 		long ts = engine.pickNewTs();
 		// AbstractContext context = new Transaction(engine);
 		saveSnapshot(new Transaction(engine), buildNewTemporarySnapshot(ts));
 		confirmTemporarySnapshot(ts);
 		// log.info("END SNAPSHOT");
 	}
 
 	private static void saveSnapshot(AbstractContext context, OutputStream out) {
 		try {
 			ObjectOutputStream oos = new ObjectOutputStream(out);
 			NavigableSet<Generic> orderGenerics = context.orderDependencies(context.getEngine());
 			for (Generic orderGeneric : orderGenerics)
 				writeGeneric(((GenericImpl) orderGeneric), oos);
 			oos.flush();
 			oos.close();
 		} catch (IOException ioe) {
 			throw new IllegalStateException(ioe);
 		}
 	}
 
 	private static void writeGeneric(GenericImpl generic, ObjectOutputStream out) throws IOException {
 		writeTs(generic, out);
 		if (generic.isEngine())
 			return;
 		out.writeInt(generic.getMetaLevel());
 		out.writeObject(generic.getValue());
 		writeAncestors(generic.getSupers(), out);
 		writeAncestors(generic.getComponents(), out);
		out.writeObject(GenericImpl.class.equals(generic.getClass()) ? null : generic.getClass());
 		out.writeBoolean(generic.automatic);
 	}
 
 	private static void writeTs(Generic generic, ObjectOutputStream out) throws IOException {
 		out.writeLong(((GenericImpl) generic).getDesignTs());
 		out.writeLong(((GenericImpl) generic).getBirthTs());
 		out.writeLong(((GenericImpl) generic).getLastReadTs());
 		out.writeLong(((GenericImpl) generic).getDeathTs());
 	}
 
 	private static void writeAncestors(Snapshot<Generic> dependencies, ObjectOutputStream out) throws IOException {
 		out.writeInt(dependencies.size());
 		for (Generic dependency : dependencies)
 			out.writeLong(((GenericImpl) dependency).getDesignTs());
 	}
 
 	private OutputStream buildNewTemporarySnapshot(long ts) {
 		try {
 			return new SnapshotZipOutputStream(directory.getAbsolutePath(), Statics.getFilename(ts));
 		} catch (FileNotFoundException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	private void confirmTemporarySnapshot(long ts) {
 		String filename = directory.getAbsolutePath() + File.separator + Statics.getFilename(ts);
 		new File(filename + Statics.ZIP_EXTENSION + Statics.PART_EXTENSION).renameTo(new File(filename + Statics.ZIP_EXTENSION));
 		manageOldSnapshots();
 	}
 
 	private void manageOldSnapshots() {
 		NavigableMap<Long, File> snapshotsMap = snapshotsMap();
 		long lastTs = snapshotsMap.lastKey();
 		long firstTs = snapshotsMap.firstKey();
 		long ts = firstTs;
 		for (long snapshotTs : new TreeSet<Long>(snapshotsMap.keySet()))
 			if (snapshotTs != lastTs && snapshotTs != firstTs)
 				if ((snapshotTs - ts) < minInterval((lastTs - snapshotTs)))
 					removeSnapshot(snapshotsMap, snapshotTs);
 				else
 					ts = snapshotTs;
 	}
 
 	private static long minInterval(long periodNumber) {
 		return (long) Math.floor(periodNumber / Statics.ARCHIVER_COEFF);
 	}
 
 	private static void removeSnapshot(NavigableMap<Long, File> snapshotsMap, long ts) {
 		snapshotsMap.get(ts).delete();
 		snapshotsMap.remove(ts);
 	}
 
 	private static long getTimestamp(final String filename) throws ParseException {
 		return Long.parseLong(filename.substring(filename.lastIndexOf("---") + 3));
 	}
 
 	public void close() {
 		if (lockFile != null)
 			try {
 				scheduler.shutdown();
 				doSnapshot(engine);
 				lockFile.close();
 				lockFile = null;
 			} catch (IOException e) {
 				throw new IllegalStateException(e);
 			}
 	}
 
 	public void startScheduler() {
 		if (lockFile != null)
 			if (Statics.SNAPSHOTS_PERIOD > 0L) {
 				// log.info("STARTS SCHEDULER : " + Statics.SNAPSHOTS_PERIOD);
 				scheduler.scheduleAtFixedRate(new Runnable() {
 					@Override
 					public void run() {
 						doSnapshot(engine);
 					}
 				}, Statics.SESSION_TIMEOUT, Statics.SNAPSHOTS_PERIOD, TimeUnit.MILLISECONDS);
 			}
 	}
 
 	private class SnapshotLoader extends HashMap<Long, Generic> {
 		private static final long serialVersionUID = 3139276947667714316L;
 
 		private ObjectInputStream inputstream;
 
 		private SnapshotLoader(ObjectInputStream inputStream) {
 			inputstream = inputStream;
 		}
 
 		private void loadSnapshot() {
 			try {
 				Engine engine = loadEngine();
 				for (;;)
 					loadGeneric(engine);
 			} catch (EOFException ignore) {
 			} catch (Exception e) {
 				throw new IllegalStateException(e);
 			}
 		}
 
 		private Engine loadEngine() throws IOException, ClassNotFoundException {
 			long[] ts = loadTs(inputstream);
 			((EngineImpl) engine).restoreEngine(ts[0], ts[1], ts[2], ts[3]);
 			put(ts[0], engine);
 			return engine;
 		}
 
 		private void loadGeneric(Engine engine) throws IOException, ClassNotFoundException {
 			long[] ts = loadTs(inputstream);
 			int metaLevel = inputstream.readInt();
 			Serializable value = (Serializable) inputstream.readObject();
 			Generic[] supers = loadAncestors(inputstream);
 			Generic[] components = loadAncestors(inputstream);
 			Generic generic = engine.getFactory().newGeneric((Class<?>) inputstream.readObject());
 			put(ts[0], ((GenericImpl) generic).restore(value, metaLevel, ts[0], ts[1], ts[2], ts[3], supers, components, inputstream.readBoolean()).plug());
 		}
 
 		private Generic[] loadAncestors(ObjectInputStream in) throws IOException {
 			int length = in.readInt();
 			Generic[] ancestors = new Generic[length];
 			for (int index = 0; index < length; index++)
 				ancestors[index] = get(in.readLong());
 			return ancestors;
 		}
 
 		private long[] loadTs(ObjectInputStream in) throws IOException {
 			long[] ts = new long[4];
 			ts[0] = in.readLong(); // designTs
 			ts[1] = in.readLong(); // birthTs
 			ts[2] = in.readLong(); // lastReadTs
 			ts[3] = in.readLong(); // deathTs
 			return ts;
 		}
 
 	}
 
 	private static class SnapshotZipOutputStream extends OutputStream {
 
 		private ZipOutputStream zipOutputStream;
 
 		private SnapshotZipOutputStream(String directoryPath, String fileName) throws FileNotFoundException {
 			zipOutputStream = new ZipOutputStream(new FileOutputStream(directoryPath + File.separator + fileName + Statics.ZIP_EXTENSION + Statics.PART_EXTENSION));
 			ZipEntry zipEntry = new ZipEntry(fileName + Statics.SNAPSHOT_EXTENSION);
 			try {
 				zipOutputStream.putNextEntry(zipEntry);
 			} catch (IOException e) {
 				throw new IllegalStateException(e);
 			}
 		}
 
 		@Override
 		public void write(int b) throws IOException {
 			zipOutputStream.write(b);
 		}
 
 		@Override
 		public void flush() throws IOException {
 			zipOutputStream.flush();
 		}
 
 		@Override
 		public void close() throws IOException {
 			zipOutputStream.close();
 		}
 
 	}
 
 	private static class SnapshotZipInputStream extends InputStream {
 
 		private ZipInputStream zipInputStream;
 
 		private SnapshotZipInputStream(String directoryPath, String fileName) throws FileNotFoundException {
 			zipInputStream = new ZipInputStream(new FileInputStream(new File(directoryPath + File.separator + fileName + Statics.ZIP_EXTENSION)));
 			try {
 				zipInputStream.getNextEntry();
 			} catch (IOException e) {
 				throw new IllegalStateException(e);
 			}
 		}
 
 		@Override
 		public int read() throws IOException {
 			return zipInputStream.read();
 		}
 
 		@Override
 		public void close() throws IOException {
 			zipInputStream.close();
 		}
 	}
 }
