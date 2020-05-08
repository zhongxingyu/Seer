 package com.macrohuang.fileq.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import com.macrohuang.fileq.FileQueue;
 import com.macrohuang.fileq.codec.Codec;
 import com.macrohuang.fileq.codec.impl.KryoCodec;
 import com.macrohuang.fileq.conf.Config;
 import com.macrohuang.fileq.util.FileNameUtil;
 import com.macrohuang.fileq.util.NumberBytesConvertUtil;
 
 public abstract class AbstractFileQueueImpl<E> implements FileQueue<E> {
 	private Config config;
 	private final AtomicInteger objectCount;
 	protected Codec<E> codec;
 	private final AtomicLong writeNumber;
 	protected AtomicLong writePosition;
 	private final AtomicLong readNumber;
 	protected AtomicLong readPosition;
 	protected MappedByteBuffer writeMappedByteBuffer;
 	protected static final int META_SIZE = 16;
 	protected static final int CHECKSUM_SIZE = 16;
 	protected static final int magic = 1314520;
 	protected static final byte[] LEADING_HEAD = NumberBytesConvertUtil.int2ByteArr(magic);
 	private MappedByteBuffer queueMetaBuffer;
 	private RandomAccessFile readStream;
 	private RandomAccessFile writeStream;
 	protected FileChannel readChannel;
 	protected FileChannel writeChannel;
 	private FileChannel metaChannel;
 	private RandomAccessFile metaAccessFile;
 	private static final int SIZE_OF_QUEUE_META = 36;
 
 	public AbstractFileQueueImpl(Config config) {
 		codec = new KryoCodec<E>();
 		objectCount = new AtomicInteger(0);
 		writeNumber = new AtomicLong(0);
 		readNumber = new AtomicLong(0);
 		writePosition = new AtomicLong(0);
 		readPosition = new AtomicLong(0);
 		this.config = config;
 		init();
 	}
 
 	protected final void init() {
		File filePathFile = new File(config.getQueueFilePath());
		if (!filePathFile.exists())
			filePathFile.mkdir();
 		try {
 			if (config.isInit()) {
 				for (File file : filePathFile.listFiles()) {
 					file.delete();
 				}
 			}
 			File meta = new File(config.getQueueFilePath() + File.separator + Config.META_FILE_NAME);
 			boolean first = false;
 			if (!meta.exists()) {
 				meta.createNewFile();
 				first = true;
 			}
 			metaAccessFile = new RandomAccessFile(meta, "rw");
 			metaChannel = metaAccessFile.getChannel();
 			queueMetaBuffer = metaChannel.map(MapMode.READ_WRITE, 0, SIZE_OF_QUEUE_META);
 			if (!first && !config.isInit()) {
 				writeNumber.set(queueMetaBuffer.getLong());
 				writePosition.set(queueMetaBuffer.getLong());
 				readNumber.set(queueMetaBuffer.getLong());
 				readPosition.set(queueMetaBuffer.getLong());
 				objectCount.set(queueMetaBuffer.getInt());
 			} else {
 				queueMetaBuffer.put(NumberBytesConvertUtil.long2ByteArr(0L));
 				queueMetaBuffer.put(NumberBytesConvertUtil.long2ByteArr(0L));
 				queueMetaBuffer.put(NumberBytesConvertUtil.long2ByteArr(0L));
 				queueMetaBuffer.put(NumberBytesConvertUtil.long2ByteArr(0L));
 				queueMetaBuffer.put(NumberBytesConvertUtil.int2ByteArr(0));
 			}
 			File outputFile = new File(FileNameUtil.getFileName(config, writeNumber.get()));
 			if (!outputFile.exists())
 				outputFile.createNewFile();
 			writeStream = new RandomAccessFile(outputFile, "rw");
 			writeChannel = writeStream.getChannel();
 			writeMappedByteBuffer = writeChannel.map(MapMode.READ_WRITE, 0, config.getSizePerFile());
 
 			File inputFile = new File(FileNameUtil.getFileName(config, readNumber.get()));
 			if (!inputFile.exists())
 				inputFile.createNewFile();
 			readStream = new RandomAccessFile(inputFile, "r");
 			readChannel = readStream.getChannel();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Config getConfig() {
 		return config;
 	}
 
 	public void setConfig(Config config) {
 		this.config = config;
 		init();
 	}
 
 	@Override
 	public boolean remain() {
 		return readPosition == writePosition;
 	}
 
 	@Override
 	public int size() {
 		return objectCount.get();
 	}
 
 	@Override
 	public abstract void add(E e);
 
 	protected abstract E peekInner(boolean remove, long timeout);
 
 	@Override
 	public E remove() {
 		if (readPosition.get() == writePosition.get())
 			return null;
 		return peekInner(true, 0L);
 	}
 
 	@Override
 	public E peek() {
 		if (readPosition.get() == writePosition.get())
 			return null;
 		return peekInner(false, 0L);
 	}
 
 	@Override
 	public void clear() {
 		objectCount.getAndSet(0);
 		readPosition.getAndSet(writePosition.get());
 	}
 
 	@Override
 	public E take() throws InterruptedException {
 		while (readNumber.get() >= writeNumber.get() && readPosition.get() >= writePosition.get()) {
 			Thread.sleep(100);
 		}
 		return peekInner(true, 0L);
 	}
 
 	@Override
 	public E take(long timeout, TimeUnit unit) throws InterruptedException {
 		if (readNumber.get() == writeNumber.get() && readPosition.get() == writePosition.get()) {
 			Thread.sleep(TimeUnit.MILLISECONDS.convert(timeout, unit));
 		}
 		if (readPosition.get() == writePosition.get()) {
 			return null;
 		}
 		return peekInner(true, TimeUnit.MILLISECONDS.convert(timeout, unit));
 	}
 
 	@Override
 	public void close() {
 		try {
 			metaAccessFile.close();
 			metaChannel.close();
 			readChannel.close();
 			readStream.close();
 			writeChannel.close();
 			writeStream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public boolean delete(File file) {
 		boolean success = true;
 		if (file.isDirectory()) {
 			for (File file2 : file.listFiles()) {
 				success &= delete(file2);
 			}
 		}
 		success &= file.delete();
 		return success;
 	}
 
 	@Override
 	public boolean delete() {
 		close();
 		return delete(new File(config.getQueueFilePath()));
 	}
 
 	protected final void increateWriteNumber() throws IOException {
 		queueMetaBuffer.putLong(0, writeNumber.incrementAndGet());
 		// writeMappedByteBuffer.force();
 		writeChannel.close();
 		writeStream.close();
 		File outputFile = new File(FileNameUtil.getFileName(config, writeNumber.get()));
 		if (!outputFile.exists())
 			outputFile.createNewFile();
 		writeStream = new RandomAccessFile(outputFile, "rw");
 		writeChannel = writeStream.getChannel();
 		writePosition.set(0L);
 		writeMappedByteBuffer = writeChannel.map(MapMode.READ_WRITE, 0, config.getSizePerFile());
 	}
 
 	protected final void increateReadNumber() throws IOException {
 		queueMetaBuffer.putLong(16, readNumber.incrementAndGet());
 		readChannel.close();
 		readStream.close();
 		File inputFile = new File(FileNameUtil.getFileName(config, readNumber.get()));
 		if (!inputFile.exists())
 			inputFile.createNewFile();
 		readStream = new RandomAccessFile(inputFile, "r");
 		readChannel = readStream.getChannel();
 		readPosition.set(0L);
 	}
 
 	protected final void updateWriteMeta() {
 		queueMetaBuffer.position(8);
 		queueMetaBuffer.put(NumberBytesConvertUtil.long2ByteArr(writePosition.get()));
 		queueMetaBuffer.position(32);
 		queueMetaBuffer.put(NumberBytesConvertUtil.int2ByteArr(objectCount.incrementAndGet()));
 	}
 
 	protected final void updateReadMeta() {
 		queueMetaBuffer.putLong(24, readPosition.get());
 		queueMetaBuffer.putInt(32, objectCount.decrementAndGet());
 	}
 
 	protected final int getFileSize() {
 		return config.getSizePerFile();
 	}
 }
