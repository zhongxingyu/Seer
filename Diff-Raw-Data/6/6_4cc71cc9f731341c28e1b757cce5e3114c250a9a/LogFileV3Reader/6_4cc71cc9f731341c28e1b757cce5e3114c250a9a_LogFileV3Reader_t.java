 /*
  * Copyright 2014 Eediom Inc.
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
 
 package org.araqne.logstorage.file;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import org.araqne.codec.Base64;
 import org.araqne.logstorage.Crypto;
 import org.araqne.logstorage.LogCryptoProfile;
 import org.araqne.storage.api.FilePath;
 import org.araqne.storage.api.StorageInputStream;
import org.araqne.storage.api.StorageUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class LogFileV3Reader implements Closeable {
 	private final Logger logger = LoggerFactory.getLogger("org.araqne.logstorage.file.LogFileV3Reader");
 
 	private static final int FILE_VERSION = 3;
 	
 	private final FilePath indexPath;
 	private final FilePath dataPath;
 	private final FilePath keyPath;
 
 	private List<IndexBlockV3Header> indexBlockHeaders = new ArrayList<IndexBlockV3Header>();
 
 	private String cipher;
 	private String digest;
 	private byte[] cipherKey;
 	private byte[] digestKey;
 	private LogCryptoProfile crypto;
 
 	private long totalCount;
 
 	private StorageInputStream dataStream;
 
 	private String compressionMethod;
 	
 	private int currentBlockIndex;
 
 	// TODO refine this interface
 	public static class LogBlockV3 {
 		private IndexBlockV3Header indexBlock;
 		private DataBlockV3 dataBlock;
 		
 		public long getMinTime() {
 			return indexBlock.minTime;
 		}
 		
 		public long getMaxTime() {
 			return indexBlock.maxTime;
 		}
 		
 		public long getMinId() {
 			return dataBlock.getMinId();
 		}
 		
 		public long getMaxId() {
 			return dataBlock.getMaxId();
 		}
 		
 		public byte getVersion() {
 			return dataBlock.getVersion();
 		}
 
 		public byte getFlag() {
 			return dataBlock.getFlag();
 		}
 
 		public int getOriginalSize() {
 			return dataBlock.getOriginalSize();
 		}
 
 		public byte[] getIv() {
 			return dataBlock.getIv();
 		}
 
 		public byte[] getSignature() {
 			return dataBlock.getSignature();
 		}
 
 		public long getIndexFp() {
 			return indexBlock.fp;
 		}
 		
 		public long getDataFp() {
 			return dataBlock.getDataFp();
 		}
 
 		public int getCompressedSize() {
 			return dataBlock.getCompressedSize();
 		}
 		
 		public int getLogOffsetCount() {
 			return dataBlock.getLogOffsetCount();
 		}
 
 		public int getLogOffset(int idx) {
 			return dataBlock.getLogOffset(idx);
 		}
 
 		public ByteBuffer getDataBuffer() {
 			return dataBlock.getDataBuffer();
 		}
 
 		public byte[] getCompressedBuffer() {
 			return dataBlock.getCompressedBuffer();
 		}
 		
 		public String getDataHash() {
 			return dataBlock.getDataHash();
 		}
 	}
 
 	public LogFileV3Reader(FilePath indexPath, FilePath dataPath, FilePath keyPath, LogCryptoProfile crypto) throws IOException {
 		this.indexPath = indexPath;
 		this.dataPath = dataPath;
 		this.keyPath = keyPath;
 		this.crypto = crypto;
 
 		loadIndexFile();
 		loadDataFile();
 		loadKeyFile();
 	}
 	
 	public int getBlockCount() {
 		return indexBlockHeaders.size();
 	}
 	
 	public boolean hasNextBlock() {
 		return currentBlockIndex < indexBlockHeaders.size();
 	}
 	
 	public LogBlockV3 nextBlock() {
 		if (!hasNextBlock())
 			throw new NoSuchElementException();
 		
 		LogBlockV3 block = new LogBlockV3();
 		block.indexBlock = indexBlockHeaders.get(currentBlockIndex);
 		
 		currentBlockIndex ++;
 		
 		try {
 			block.dataBlock = loadDataBlock(block.indexBlock);
 		} catch (Throwable t) {
 			
 		}
 		
 		return block;
 	}
 	
 	public LogBlockV3 getBlock(int idx) throws IOException {
 		seek(idx);
 		return nextBlock();
 	}
 	
 	public void seek(int idx) throws IOException {
 		if (idx < 0 || idx >= indexBlockHeaders.size())
 			throw new IOException("invalid seek index");
 		
 		currentBlockIndex = idx;		
 	}
 
 	@Override
 	public void close() throws IOException {
 		ensureClose(dataStream);
 	}
 	
 	private void ensureClose(Closeable c) {
 		if (c != null)
 			try {
 				c.close();
 			} catch (IOException e) {
 			}
 	}
 	
 	private void readFully(InputStream in, ByteBuffer bb) throws IOException {
 		int total = 0;
 		int limit = bb.limit();
 		while (total < limit) {
 			int ret = in.read(bb.array(), total, limit - total);
 			if (ret < 0)
 				throw new IOException("ByteBuffer underflow");
 			total += ret;
 		}
 	}
 
 	private byte[] readAllBytes(FilePath f) throws IOException {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		InputStream is = null;
 		try {
 			is = f.newInputStream();
 			byte[] b = new byte[8096];
 			while (true) {
 				int count = is.read(b);
 				if (count < 0)
 					break;
 				bos.write(b, 0, count);
 			}
 
 			return bos.toByteArray();
 		} finally {
 			if (is != null)
 				is.close();
 		}
 	}
 
 	private void loadIndexFile() throws IOException {
 		BufferedInputStream indexReader = null;
 		StorageInputStream indexStream = null;
 		
 		try {
 			indexStream = indexPath.newInputStream();
 			LogFileHeader indexFileHeader = LogFileHeader.extractHeader(indexStream);
 			if (indexFileHeader.version() != FILE_VERSION)
 				throw new InvalidLogFileHeaderException("version not match, index file " + indexPath.getAbsolutePath());
 
 			long length = indexStream.length();
 			long pos = indexFileHeader.size();
 
 			indexReader = new BufferedInputStream(indexPath.newInputStream());
 			indexReader.skip(pos);
 
 			ByteBuffer bb = ByteBuffer.allocate(IndexBlockV3Header.ITEM_SIZE);
 			while (pos < length) {
 				try {
 					readFully(indexReader, bb);
 				} catch (IOException e) {
 					break;
 				}
 				IndexBlockV3Header header = new IndexBlockV3Header(bb.getLong(), bb.getLong(), bb.getLong(), bb.getInt(),
 						totalCount + 1);
 				header.fp = pos;
 				header.ascLogCount = totalCount;
 				totalCount += header.logCount;
 				indexBlockHeaders.add(header);
 				pos += IndexBlockV3Header.ITEM_SIZE;
 				bb.clear();
 			}
 
 			long t = 0;
 			for (int i = indexBlockHeaders.size() - 1; i >= 0; i--) {
 				IndexBlockV3Header h = indexBlockHeaders.get(i);
 				h.dscLogCount = t;
 				t += h.logCount;
 			}
 
 			logger.trace("araqne logstorage: {} has {} blocks, {} logs.",
 					new Object[] { indexPath.getName(), indexBlockHeaders.size(), totalCount });
 		} finally {
			StorageUtil.ensureClose(indexStream);
			StorageUtil.ensureClose(indexReader);
 		}
 	}
 	
 	private void loadDataFile() throws IOException {
 		this.dataStream = dataPath.newInputStream();
 		LogFileHeader dataFileHeader = LogFileHeader.extractHeader(dataStream);
 		if (dataFileHeader.version() != FILE_VERSION)
 			throw new InvalidLogFileHeaderException("version not match, data file");
 
 		byte[] ext = dataFileHeader.getExtraData();
 
 		compressionMethod = new String(ext, 4, ext.length - 4).trim();
 		if (compressionMethod.length() == 0)
 			compressionMethod = null;
 	}
 
 	private void loadKeyFile() throws IOException {
 		if (crypto == null || keyPath == null || !keyPath.exists())
 			return;
 
 		byte[] b = readAllBytes(keyPath);
 		try {
 			b = Crypto.decrypt(b, crypto.getPrivateKey());
 		} catch (Exception e) {
 			throw new IOException("cannot decrypt key file", e);
 		}
 
 		String line = new String(b);
 		String[] tokens = line.split(",");
 
 		if (!tokens[0].equals("v1"))
 			throw new IllegalStateException("unsupported key file version: " + tokens[0]);
 
 		if (!tokens[1].isEmpty())
 			cipher = tokens[1];
 
 		if (!tokens[2].isEmpty())
 			digest = tokens[2];
 
 		if (!tokens[3].isEmpty())
 			cipherKey = Base64.decode(tokens[3]);
 
 		if (!tokens[4].isEmpty())
 			digestKey = Base64.decode(tokens[4]);
 	}
 	
 	private DataBlockV3 loadDataBlock(IndexBlockV3Header h) throws IOException {
 		DataBlockV3Params p = new DataBlockV3Params();
 		p.indexHeader = h;
 		p.dataStream = dataStream;
 		p.dataPath = dataPath;
 		p.compressionMethod = compressionMethod;
 		p.cipher = cipher;
 		p.cipherKey = cipherKey;
 		p.digest = digest;
 		p.digestKey = digestKey;
 		DataBlockV3 b = new DataBlockV3(p);
 		return b;
 	}
 }
