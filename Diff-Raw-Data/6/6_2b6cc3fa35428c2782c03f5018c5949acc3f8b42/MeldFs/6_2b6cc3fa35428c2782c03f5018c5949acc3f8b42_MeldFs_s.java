 package warrenfalk.meldfs;
 
 import java.io.IOException;
 import java.nio.file.DirectoryStream;
 import java.nio.file.FileSystem;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import warrenfalk.fuselaj.DirBuffer;
 import warrenfalk.fuselaj.Errno;
 import warrenfalk.fuselaj.FileInfo;
 import warrenfalk.fuselaj.FuselajFs;
 import warrenfalk.fuselaj.FilesystemException;
 import warrenfalk.fuselaj.Stat;
 
 public class MeldFs extends FuselajFs {
 	SourceFs[] sources;
 	String[] fuseArgs;
 	ExecutorService threadPool;
 	
 	final Path rootPath = nfs.getPath(".").normalize();
 	
 	public MeldFs(Path mountLoc, Path[] sources, boolean debug, String options) {
 		super(true);
 		this.sources = SourceFs.fromPaths(sources);
 		ArrayList<String> arglist = new ArrayList<String>();
 		if (debug)
 			arglist.add("-d");
 		arglist.add(mountLoc.toAbsolutePath().toString());
 		if (options != null && options.length() > 0) {
 			arglist.add("-o");
 			arglist.add(options);
 		}
 		fuseArgs = arglist.toArray(new String[arglist.size()]);
 		threadPool = Executors.newCachedThreadPool();
 	}
 	
 	private Path parentOf(Path path) {
 		Path parent = path.getParent();
 		if (parent == null)
 			parent = rootPath;
 		return parent;
 	}
 	
 	int run() {
 		return run(fuseArgs);
 	}
 
 	/** Entry point for the MeldFs process
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		// TODO: Debugging code follows
 		boolean debug = true;
 		FileSystem fs = FileSystems.getDefault();
 		Path mountPoint = fs.getPath("/dev/shm/test");
 		if (!Files.exists(mountPoint))
 			Files.createDirectories(mountPoint);
 		Path[] devices = new Path[] {
 				fs.getPath("/home/warren/test/0"),
 				fs.getPath("/home/warren/test/1"),
 				fs.getPath("/home/warren/test/2"),
 				fs.getPath("/home/warren/test/3"),
 				fs.getPath("/home/warren/test/4"),
 				fs.getPath("/home/warren/test/5"),
 		};
 		//String options = "allow_other,use_ino,big_writes";
 		String options = "allow_other,big_writes";
 		for (Path device : devices) {
 			if (!Files.exists(device))
 				Files.createDirectories(device);
 		}
 		
 		MeldFs mfs = new MeldFs(mountPoint, devices, debug, options);
 		int exitCode = mfs.run();
 		System.exit(exitCode);
 	}
 	
 	@Override
 	protected void getattr(Path path, Stat stat) throws FilesystemException {
 		try {
 			// attempt to find entry with that name
 			Path file = getLatestFile(path);
 			if (file == null)
 				throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 			os_stat(file.toAbsolutePath(), stat);
 		}
 		catch (InterruptedException ie) {
 			throw new FilesystemException(Errno.InterruptedSystemCall);
 		}
 	}
 	
 	@Override
 	protected void mkdir(Path path, int mode) throws FilesystemException {
 		// find which device contains the parent directory and create there
 		// if more than one device contains the parent, create on the device with the most recently modified
 		try {
 			Path dir = getLatestFile(path);
 			if (dir != null)
 				throw new FilesystemException(Errno.FileExists);
 			Path parent = parentOf(path);
 			Path parentDir = getLatestFile(parent);
 			if (parentDir == null)
 				throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 			dir = parentDir.resolve(path.getFileName());
 			os_mkdir(dir, mode);
 		}
 		catch (InterruptedException ie) {
 			throw new FilesystemException(Errno.InterruptedSystemCall);
 		}
 	}
 	
 	@Override
 	protected void rmdir(final Path path) throws FilesystemException {
 		try {
 			final AtomicInteger sync = new AtomicInteger(sources.length);
 			final AtomicInteger found = new AtomicInteger(0);
 			final AtomicInteger deleted = new AtomicInteger(0);
 			for (int i = 0; i < sources.length; i++) {
 				final int index = i;
 				threadPool.execute(new Runnable() {
 					@Override
 					public void run() {
 						SourceFs source = sources[index];
 						Path sourceLoc = source.root.resolve(path);
 						if (Files.exists(sourceLoc)) {
 							found.incrementAndGet();
 							try {
 								os_rmdir(sourceLoc);
 								deleted.incrementAndGet();
 							}
 							catch (FilesystemException fs) {
 								// TODO: figure out what to do here.
 								// We've tried to remove one of the instances of the directory, but at least one failed for some reason
 							}
 						}
 						if (0 == sync.decrementAndGet()) {
 							synchronized (sync) {
 								sync.notify();
 							}
 						}
 							
 					}
 				});
 			}
 			synchronized (sync) {
 				sync.wait();
 			}
 			if (found.intValue() == 0)
 				throw new FilesystemException(Errno.NoSuchFileOrDirectory);
 			// TODO: we should probably return the error message from the os_rmdir that failed
 			if (found.intValue() != deleted.intValue())
 				throw new FilesystemException(Errno.IOError);
 		}
 		catch (InterruptedException e) {
 			throw new FilesystemException(Errno.InterruptedSystemCall);
 		}
 	}
 	
 	@Override
 	protected void opendir(Path path, FileInfo fi) throws FilesystemException {
 		try {
 			Path dir = getLatestFile(path);
 			if (!Files.isDirectory(dir))
 				throw new FilesystemException(Errno.NotADirectory);
 			FileHandle.open(fi, path);
 		}
 		catch (InterruptedException e) {
 			throw new FilesystemException(Errno.InterruptedSystemCall);
 		}
 	}
 	
 	@Override
 	protected void readdir(Path path, DirBuffer dirBuffer, FileInfo fileInfo) throws FilesystemException {
 		FileHandle fh = FileHandle.get(fileInfo.getFileHandle());
 		String[] names;
 		if (fh.data instanceof Path) {
 			final Path dirpath = (Path)fh.data;
 			final HashSet<String> items = new HashSet<String>();
 			final AtomicInteger sync = new AtomicInteger(sources.length);
 			for (int i = 0; i < sources.length; i++) {
 				final int index = i;
 				threadPool.execute(new Runnable() {
 					@Override
 					public void run() {
 						SourceFs source = sources[index];
 						try {
 							Path p = source.root.resolve(dirpath);
 							if (Files.isDirectory(p)) {
 								try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
									items.add(".");
									items.add("..");
 									for (Path item : stream) {
 										String itemName = item.getFileName().toString();
 										synchronized (items) {
 											items.add(itemName);
 										}
 									}
 								}
 							}
 							if (0 == sync.decrementAndGet()) {
 								synchronized (sync) {
 									sync.notify();
 								}
 							}
 						}
 						catch (IOException ioe) {
 							source.onIoException(ioe);
 						}
 					}
 				});
 			}
 			try {
 				synchronized (sync) {
 					sync.wait();
 				}
 			}
 			catch (InterruptedException e) {
 				throw new FilesystemException(Errno.InterruptedSystemCall);
 			}
 			names = items.toArray(new String[items.size()]);
 			Arrays.sort(names);
 			fh.data = names;
 		}
 		else {
 			names = (String[])fh.data;
 		}
 		for (long i = dirBuffer.getPosition(); i < names.length; i++) {
 			if (dirBuffer.putDir(names[(int)i], i + 1))
 				return;
 		}
 	}
 	
 	@Override
 	protected void releasedir(Path path, FileInfo fi) throws FilesystemException {
 		FileHandle.release(fi);
 	}
 	
 	/** Return the real path the the file if on one device, or the path to the most recently
 	 * modified version of the file if on multiple devices
 	 * @param path
 	 * @return
 	 * @throws InterruptedException
 	 */
 	private Path getLatestFile(final Path path) throws InterruptedException {
 		final Path[] files = new Path[sources.length];
 		final long[] modTimes = new long[sources.length];
 		final AtomicInteger sync = new AtomicInteger(sources.length);
 		for (int i = 0; i < sources.length; i++) {
 			final int index = i;
 			threadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					SourceFs source = sources[index];
 					Path sourceLoc = source.root.resolve(path);
 					if (Files.exists(sourceLoc)) {
 						try {
 							modTimes[index] = Files.getLastModifiedTime(sourceLoc).toMillis();
 							files[index] = sourceLoc;
 						}
 						catch (IOException ioe) {
 							source.onIoException(ioe);
 						}
 					}
 					if (0 == sync.decrementAndGet()) {
 						synchronized (sync) {
 							sync.notify();
 						}
 					}
 						
 				}
 			});
 		}
 		synchronized (sync) {
 			sync.wait();
 		}
 		long max = 0L;
 		Path result = null;
 		for (int i = 0; i < sources.length; i++) {
 			Path file = files[i];
 			if (file == null)
 				continue;
 			long mod = modTimes[i];
 			if (mod > max) {
 				result = file;
 				max = mod;
 			}
 		}
 		return result;
 	}
 	
 }
