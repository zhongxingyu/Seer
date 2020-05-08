 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.LinkedList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class DownloadTask {
 	private int threadNumber = 5; //default thread number
 	private ExecutorService pool; //We use thread pool
 	private String path = ""; //the resource url
 	private static File file; // Point to the storage file
 	private Lock poolLock = new ReentrantLock(); 
 	private Condition allDone = poolLock.newCondition();
 	private int doneNumber = 0;
 	private LinkedList<File> files;
 
 	public int getThreadNumber() {
 		return threadNumber;
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public static File getFile() {
 		return file;
 	}
 
 	public DownloadTask(String path, String filename) {
 		this.path = path;
 		file = new File(filename);
 		this.pool = Executors.newFixedThreadPool(this.threadNumber);
 	}
 
 	public DownloadTask(String path, String filename, int threadNumber) {
 		this.path = path;
 		file = new File(filename);
 		this.threadNumber = threadNumber;
 		this.pool = Executors.newFixedThreadPool(this.threadNumber);
 	}
 
 	public boolean download() {
 		URL url = null;
 		try {
 			url = new URL(path);
 		} catch (MalformedURLException e2) {
 			e2.printStackTrace();
 		}
 		URLConnection openConnection = null;
 		int totalLength = 0;
 		int partLength = 0;
 		try {
 			openConnection = url.openConnection();
 			openConnection.connect();
 			totalLength = openConnection.getContentLength();
 			partLength = totalLength / threadNumber;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		for (int i = 0; i < threadNumber; i++) {
 			final int seq = i;
 			final int finalPartLength = partLength;
 			final File logfile = new File(file.getName() +"_log");
 			if (i < threadNumber - 1) {
 				pool.execute(new Runnable() {
 
 					@Override
 					public void run() {
 						download(path, finalPartLength * seq, finalPartLength
								* (seq + 1) - 1, logfile, seq);
 
 					}
 				});
 			} else {
 				final int finalTotalLength = totalLength;
 				pool.execute(new Runnable() {
 					@Override
 					public void run() {
 						download(path, finalPartLength * seq,
								finalTotalLength - 1, logfile, seq);
 					}
 				});
 			}
 		}
 
 		poolLock.lock();
 		try {
 			while (doneNumber < threadNumber) {
 				try {
 					allDone.await();
 					pool.shutdown();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		} finally {
 			poolLock.unlock();
 		}
 		return true;
 
 	}
 
 	private void download(String path, int start, int end, File logfile, int seq) {
 		URL url = null;
 		try {
 			url = new URL(path);
 		} catch (MalformedURLException e1) {
 			e1.printStackTrace();
 		}
 		HttpURLConnection openConnection = null;
 		InputStream bs = null;
 		RandomAccessFile fs = null;
 		byte[] buf = new byte[8096];
 		int size = 0;
 		long count = DownloadHelper.readOffset(seq); 
 		try {
 			openConnection = (HttpURLConnection) url.openConnection();
 			openConnection.setRequestProperty("RANGE", "bytes=" + start + "-"
 					+ end);
 			openConnection.connect();
 			bs = openConnection.getInputStream();
 			fs = new RandomAccessFile(file, "rw");
 			int off = start;
 			RandomAccessFile fos = new RandomAccessFile(logfile, "rw");
 			while ((size = bs.read(buf)) != -1) {
 				fs.seek(off);
 				fs.write(buf, 0, size);
 				count += size;
 				DownloadHelper.writeOffSet(seq, count);
 				off += size;
 			}
 			poolLock.lock();
 			try {
 				doneNumber++;
 				allDone.signalAll();
 			} finally {
 				poolLock.unlock();
 				fos.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				bs.close();
 				fs.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
