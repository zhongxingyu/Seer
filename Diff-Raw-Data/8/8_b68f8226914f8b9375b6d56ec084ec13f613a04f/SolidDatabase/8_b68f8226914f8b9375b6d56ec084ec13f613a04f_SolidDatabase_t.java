 package edu.berkeley.gamesman.database;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * Database that does nor permit seeking without being entirely read into memory.
  * (Used for Hadoop or GZIP).
  * 
  * @author Patrick Horn
  */
 public abstract class SolidDatabase extends Database {
 
 	private List<byte[]> readContents;
 	
 	private String uri;
 	
 	protected OutputStream outputStream;
 
 	/** Set to false if you want an empty configuration header
 	 * (used for SplitDatabase.) Make sure to set 'conf' yourself.
 	 */
 	public boolean storeConfiguration = true;
 
 	/** Default constructor */
 	SolidDatabase() {
 	}
 	
 	@Override
 	public void close() {
 		if (readContents != null) {
 			readContents.clear();
 			readContents = null;
 		}
 		if (outputStream != null) {
 			try {
 				outputStream.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			outputStream = null;
 		}
 	}
 
 	@Override
 	public void flush() {
 		try {
 			outputStream.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected abstract InputStream openInputStream(String uri) throws IOException;
 	protected abstract OutputStream openOutputStream(String uri) throws IOException;
 
 	private synchronized void initializeReadDatabase() throws IOException {
 		if (readContents == null) {
			List<byte[]> readContents = new ArrayList<byte[]>();
 			InputStream inputStream = openInputStream(this.getUri());
 
 			long readLength = 0;
 			byte[] tmpArray = new byte[4];
 			inputStream.read(tmpArray);
 			int conflen = ByteBuffer.wrap(tmpArray).getInt();
 			byte[] storedConf = new byte[conflen];
 			inputStream.read(storedConf);
 			if (conf == null) {
 				try {
 					conf = Configuration.load(storedConf);
 				} catch (ClassNotFoundException e) {
 					Util.fatalError("Failed to read header from SolidDatabase", e);
 				}
 			}
 			String compression = conf.getProperty("gamesman.db.compression", null);
 			if (compression != null) {
 				if (compression.equals("gzip")) {
 					inputStream = new GZIPInputStream(inputStream);
 				} else {
 					Util.fatalError("Unknown compression method '"+compression+"'");
 				}
 			}
 
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			byte[]buf = new byte[65536];
 			while (true) {
 				long toRead = 65536;
 				if (((toRead + readLength)%Integer.MAX_VALUE) < toRead) {
 					toRead = ((toRead + readLength)%Integer.MAX_VALUE);
 				}
 				int count = inputStream.read(buf, 0, (int)toRead);
 				if (count > 0) {
 					baos.write(buf, 0, count);
 					readLength += count;
 				}
 				if (count <= 0 || readLength == Integer.MAX_VALUE) {
 					readContents.add(baos.toByteArray());
 					if (count <= 0) {
 						break;
 					}
 					baos = new ByteArrayOutputStream();
 				}
 			}
 			if (readLength <= 0) {
 				throw new RuntimeException("ReadLength <= 0 in SolidDatabase");
 			}
			this.readContents = readContents;
 		}
 	}
 	
 	@Override
 	public void getBytes(byte[] arr, int off, int len) {
 		throw new RuntimeException("getBytes without position argument is not supported in SolidDatabase");
 	}
 
 	@Override
 	public void getBytes(long loc, byte[] arr, int off, int len) {
 		if (readContents == null) {
 			try {
 				initializeReadDatabase();
 			} catch (IOException e) {
 				Util.fatalError("Failed to initialized read SolidDatabase", e);
 			}
 		}
 		if ((int)(((long)len + loc)%Integer.MAX_VALUE) < len) {
 			int toRead = (int)(((long)len + loc)%Integer.MAX_VALUE);
 			getBytes(loc, arr, off, toRead);
 			getBytes(loc+toRead, arr, off+toRead, len-toRead);
			return;
 		}
		this.readContents.get((int)loc/Integer.MAX_VALUE);
 		byte[] inputArr = this.readContents.get((int)loc/Integer.MAX_VALUE);
 		int inputOff = (int)(loc%Integer.MAX_VALUE);
 		/*
 		if (inputOff + len > inputArr.length || inputOff + len < 0) {
 			len = inputArr.length - inputOff;
 		}
 		*/
 		System.arraycopy(inputArr, inputOff, arr, off, len);
 	}
 
 	@Override
 	public void initialize(String uri) {
 		this.uri = uri;
 	}
 
 	@Override
 	public synchronized void putBytes(byte[] arr, int off, int len) {
 		try {
 			if (outputStream == null) {
 				outputStream = this.openOutputStream(this.getUri());
 				String compression = conf.getProperty("gamesman.db.compression", null);
 				if (compression != null) {
 					if (compression.equals("gzip")) {
 					} else {
 						conf.deleteProperty("gamesman.db.compression");
 					}
 				}
 				byte[] storedConf;
 				if (storeConfiguration) {
 					storedConf = conf.store();
 				} else {
 					storedConf = new byte[0];
 				}
 				byte[] tmpArray = new byte[4];
 				ByteBuffer.wrap(tmpArray).putInt(storedConf.length);
 				outputStream.write(tmpArray);
 				if (compression != null) {
 					if (compression.equals("gzip")) {
 						outputStream = new GZIPOutputStream(outputStream);
 					}
 				}
 				outputStream.write(storedConf);
 			}
 			outputStream.write(arr, off, len);
 		} catch (IOException e) {
 			Util.fatalError("Failed to write "+len+"bytes to SolidDatabase", e);
 		}
 	}
 
 	@Override
 	public void seek(long loc) {
 		throw new RuntimeException("Seeking is not supported in SolidDatabase");
 	}
 
 	/**
 	 * @return The filename that was originally passed into initialize().
 	 */
 	public String getUri() {
 		return uri;
 	}
 
 }
