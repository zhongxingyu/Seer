 package edu.berkeley.gamesman.database;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * There should be some way to not have to repeat the code in MemoryCachedDatabase
  * 
  * @author dnspies
  */
 public class SynchronizedMemoryCachedDatabase extends
 		SynchronizedMemoryDatabase {
 
 	private File myFile;
 
 	@Override
 	public void initialize(String location, boolean solve) {
 		myFile = new File(location);
 		if (solve) {
 			super.initialize(location, true);
 			readingOnly = false;
 		} else {
 			readingOnly = true;
 			try {
 				InputStream fis = new FileInputStream(myFile);
 				int confLength = 0;
 				for (int i = 24; i >= 0; i -= 8) {
 					confLength <<= 8;
 					confLength |= fis.read();
 				}
 				byte[] b = new byte[confLength];
 				fis.read(b);
 				if (conf == null) {
 					conf = Configuration.load(b);
 				}
 				super.initialize(location, false);
 				if (conf.getProperty("gamesman.db.compression", "none").equals(
 						"gzip")) {
 					int bufferSize = conf.getInteger("zip.bufferKB", 1 << 12) << 10;
 					fis = new GZIPInputStream(fis, bufferSize);
 				}
 				long firstByte = 0;
 				for (int i = 56; i >= 0; i -= 8) {
 					firstByte <<= 8;
 					firstByte |= fis.read();
 				}
 				while (firstByte < firstByte())
 					firstByte += fis.skip(firstByte() - firstByte);
 				int n = 0;
				while (n >= 0 && n < maxBytes)
					n += fis.read(memoryStorage, n, maxBytes - n);
 				fis.close();
 			} catch (IOException e) {
 				Util.fatalError("IO Error", e);
 			} catch (ClassNotFoundException e) {
 				Util.fatalError("Class Not Found", e);
 			}
 		}
 	}
 
 	@Override
 	public void close() {
 		if (!readingOnly) {
 			try {
 				myFile.createNewFile();
 				OutputStream fos = new FileOutputStream(myFile);
 				byte[] b = conf.store();
 				for (int i = 24; i >= 0; i -= 8)
 					fos.write(b.length >>> i);
 				fos.write(b);
 				if (conf.getProperty("gamesman.db.compression", "none").equals(
 						"gzip")) {
 					int bufferSize = conf.getInteger("zip.bufferKB", 1 << 12) << 10;
 					fos = new GZIPOutputStream(fos, bufferSize);
 				}
 				for (int i = 56; i >= 0; i -= 8)
 					fos.write((int) (firstByte() >>> i));
 				fos.write(memoryStorage);
 				fos.close();
 			} catch (IOException e) {
 				Util.fatalError("IO Error", e);
 			}
 		}
 	}
 }
