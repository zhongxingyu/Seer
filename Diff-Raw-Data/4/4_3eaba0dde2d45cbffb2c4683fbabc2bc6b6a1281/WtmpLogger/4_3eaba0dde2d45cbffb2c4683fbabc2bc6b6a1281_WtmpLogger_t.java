 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.log.api;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 
 public class WtmpLogger extends AbstractLogger {
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(WtmpLogger.class);
 	private final File dataDir;
 	private final String path;
 
 	public WtmpLogger(LoggerSpecification spec, LoggerFactory factory) {
 		super(spec, factory);
 		dataDir = new File(System.getProperty("araqne.data.dir"), "araqne-log-api");
 		dataDir.mkdirs();
 
 		path = spec.getConfig().get("path");
 
 		// try migration at boot
 		File oldLastFile = getLastLogFile();
 		if (oldLastFile.exists()) {
 			Map<String, LastPosition> lastPositions = LastPositionHelper.readLastPositions(oldLastFile);
 			setStates(LastPositionHelper.serialize(lastPositions));
 			oldLastFile.renameTo(new File(oldLastFile.getAbsolutePath() + ".migrated"));
 		}
 	}
 
 	private WtmpEntryParser buildParser(String server) {
 		if (server == null)
 			return new WtmpEntryParserLinux();
 
 		server = server.toLowerCase();
 
 		if (server.equals("solaris"))
 			return new WtmpEntryParserSolaris();
 		else if (server.equals("aix"))
 			return new WtmpEntryParserAix();
 		else if (server.equals("hpux"))
 			return new WtmpEntryParserHpUx();
 
 		return new WtmpEntryParserLinux();
 	}
 
 	@Override
 	protected void runOnce() {
 		Map<String, LastPosition> lastPositions = LastPositionHelper.deserialize(getStates());
 		LastPosition inform = lastPositions.get(path);
 		if (inform == null)
 			inform = new LastPosition(path);
 		long pos = inform.getPosition();
 
 		File wtmpFile = new File(path);
 		if (!wtmpFile.exists()) {
 			slog.debug("araqne log api: logger [{}] wtmp file [{}] doesn't exist", getFullName(), path);
 			return;
 		}
 
 		if (!wtmpFile.canRead()) {
 			slog.debug("araqne log api: logger [{}] wtmp file [{}] no read permission", getFullName(), path);
 			return;
 		}
 
 		// log rotated case, reset read offset
 		if (wtmpFile.length() < pos) {
 			pos = 0;
 		}
 
 		WtmpEntryParser parser = buildParser(getConfigs().get("server"));
 		int blockSize = parser.getBlockSize();
 
 		RandomAccessFile raf = null;
 		try {
 
 			raf = new RandomAccessFile(wtmpFile, "r");
 			raf.seek(pos);
 			byte[] block = new byte[blockSize];
 
 			while (true) {
 				raf.readFully(block);
 
 				WtmpEntry e = parser.parseEntry(ByteBuffer.wrap(block));
 
 				Map<String, Object> data = new HashMap<String, Object>();
 				data.put("type", e.getType().toString());
 				data.put("host", e.getHost());
 				data.put("pid", e.getPid());
 				data.put("session", e.getSession());
 				data.put("user", e.getUser());
 				
				data.put("device", e.getDeviceName());
				data.put("inittab_id", e.getInitTabId());
 
 				write(new SimpleLog(e.getDate(), getFullName(), data));
 				pos += blockSize;
 			}
 		} catch (EOFException e) {
 			// ignore
 		} catch (Throwable t) {
 			slog.error("araqne log api: logger [" + getFullName() + "] cannot load wtmp file [" + path + "]", t);
 		} finally {
 			if (raf != null) {
 				try {
 					raf.close();
 				} catch (IOException e) {
 				}
 			}
 			inform.setPosition(pos);
 			lastPositions.put(path, inform);
 			setStates(LastPositionHelper.serialize(lastPositions));
 		}
 	}
 
 	protected File getLastLogFile() {
 		return new File(dataDir, "wtmp-" + getName() + ".lastlog");
 	}
 }
