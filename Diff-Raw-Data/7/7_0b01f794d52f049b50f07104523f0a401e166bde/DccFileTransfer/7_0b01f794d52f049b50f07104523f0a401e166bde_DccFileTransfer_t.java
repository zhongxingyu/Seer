 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.snertlab.xdccBee.irc;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 
 /**
  * @author snert
  * 
  */
 public class DccFileTransfer extends Thread {
 	// TODO: Transfer Resume
 
 	private Socket s;
 	private InputStream in;
 
 	private String host;
 	private int port;
 	private long size;
 	private String filename;
 	private String fullFilename;
 	private long progress;
	private long fileSizeOnResume;
 	private String nick;
 	private long startTime;
 	private boolean resume;
 
 	public DccFileTransfer(String host, int port, long size, String nick,
 			String filename, boolean resume) throws Exception {
 		this.host = host;
 		this.port = port;
 		this.size = size;
 		this.filename = filename;
 		this.nick = nick;
 		this.resume = resume;
 		setDaemon(true);
 		setPriority(Thread.MIN_PRIORITY);
 	}
 
 	/**
 	 * @deprecated Please now use start(String downloadDirFilename)
 	 * @see start(String downloadDirFilename)
 	 */
 	@Deprecated
 	public synchronized void start() {
 		super.start();
 	}
 
 	public synchronized void start(String downloadDirFilename) {
 		fullFilename = downloadDirFilename + filename;
 		super.start();
 	}
 
 	public void run() {
 		try {
 			openSocket();
 		} catch (Exception exc) {
 			close();
 			throw new RuntimeException(exc.toString());
 		}
 	}
 
 	private void openSocket() throws IOException {
 		close();
 		s = new Socket(host, port);
 		s.setSoTimeout(1000 * 60 * 15);
 		in = s.getInputStream();
 		ship();
 	}
 
 	private void ship() throws IOException {
 		BufferedOutputStream foutput = null;
 		try {
 			startTime = System.currentTimeMillis();
 			BufferedInputStream input = new BufferedInputStream(
 					s.getInputStream());
 			BufferedOutputStream output = new BufferedOutputStream(
 					s.getOutputStream());
 
 			if (resume) {
 				File f = new File(fullFilename);
 				progress = f.length();
				fileSizeOnResume = progress;
 			}
 			foutput = new BufferedOutputStream(new FileOutputStream(
 					fullFilename, resume));
 
 			byte[] inBuffer = new byte[1024];
 			byte[] outBuffer = new byte[4];
 			int bytesRead = 0;
 			while (((bytesRead = input.read(inBuffer, 0, inBuffer.length)) != -1)) {
 				foutput.write(inBuffer, 0, bytesRead);
 				progress += bytesRead;
 				// Send back an acknowledgement of how many bytes we have got so
 				// far.
 				outBuffer[0] = (byte) ((progress >> 24) & 0xff);
 				outBuffer[1] = (byte) ((progress >> 16) & 0xff);
 				outBuffer[2] = (byte) ((progress >> 8) & 0xff);
 				outBuffer[3] = (byte) ((progress >> 0) & 0xff);
 				output.write(outBuffer);
 				output.flush();
 			}
 		} catch (Exception e) {
 			if (isInterrupted()) { // thread was interrupted => is ok was called
 									// from close
 				// nothing
 			} else { // real error so throw
 				throw new RuntimeException(e);
 			}
 		} finally {
 			try {
 				foutput.flush();
 				foutput.close();
 				close();
 			} catch (Exception anye) {
 				// Do nothing.
 			}
 		}
 	}
 
 	public void close() {
 		try {
 			if (s != null) {
 				if (!s.isClosed()) {
 					s.close();
 				}
 			}
 			if (in != null) {
 				in.close();
 			}
 		} catch (Exception exc) {
 			// nothing
 		}
 		interrupt();
 	}
 
 	public String getNick() {
 		return nick;
 	}
 
 	public long getProgress() {
 		return progress;
 	}
 
 	public long getSize() {
 		return size;
 	}
 
 	public long getTransferRate() {
 		long time = (System.currentTimeMillis() - startTime) / 1000;
 		if (time <= 0) {
 			return 0;
 		}
		if (resume) {
			return (getProgress() - fileSizeOnResume) / time;
		}
 		return getProgress() / time;
 	}
 
 	public boolean isResume() {
 		return resume;
 	}
 
 	public String getFilename() {
 		return filename;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 }
