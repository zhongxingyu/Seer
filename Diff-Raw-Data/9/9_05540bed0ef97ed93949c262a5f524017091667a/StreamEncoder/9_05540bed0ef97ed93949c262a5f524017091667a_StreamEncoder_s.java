 package com.vertica.jdbc.nativebinary;
 
 import java.io.IOException;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.CharBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.WritableByteChannel;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetEncoder;
 import java.util.Collections;
 import java.util.List;
 
 public class StreamEncoder {
 	private static final byte BYTE_ZERO = (byte)0;
 	private static final byte BYTE_FULL = (byte)0xFF;
 	private static final byte BYTE_LF = (byte)0x0A;
 	private static final byte BYTE_CR = (byte)0x0D;
 	
 	private static final int MAX_CHAR_LENGTH = 65000;
 	
 	// TODO: maybe this needs to be a configurable setting, but I don't know how important it is.
 	private static final int NUM_ROWS_TO_BUFFER = 1000;
     
     private int columnCount;
     private int rowMaxSize;
     private int rowHeaderSize;
     
     private ByteBuffer buffer;
     
     private Charset charset;
     
 	PipedOutputStream pipedOutputStream;
 	WritableByteChannel channel;
 	
     private final List<ColumnSpec> columns;
     private final BitSet rowNulls;
         
     
     public void close() throws IOException {
       flushAndClose();
     }
     
     
     public StreamEncoder(List<ColumnSpec> columns, PipedInputStream inputStream) throws IOException
 	{
 		this.columns = Collections.unmodifiableList(columns);
 		this.columnCount = this.columns.size();
 		this.rowNulls = new BitSet(this.columnCount);
 		
     	this.charset = Charset.forName("UTF-8");
 
     	CharBuffer charBuffer = CharBuffer.allocate(MAX_CHAR_LENGTH);
     	CharsetEncoder charEncoder = charset.newEncoder();
     	
 		this.pipedOutputStream = new PipedOutputStream(inputStream);
 		this.channel = Channels.newChannel(pipedOutputStream);
 		
 		this.rowHeaderSize = 4 + this.rowNulls.numBytes();
 		this.rowMaxSize = this.rowHeaderSize;
 
 		for (ColumnSpec column : columns) {
 			switch (column.type) {
 				case CHAR:
 				case VARCHAR:
 					column.setCharBuffer(charBuffer);
 					column.setCharEncoder(charEncoder);
 					break;
 				default:
 					break;
 			}
 			switch (column.type) {
 				case VARCHAR:
 				case VARBINARY:
 					this.rowMaxSize += MAX_CHAR_LENGTH;
 					break;
 				default:
 					this.rowMaxSize += column.bytes;
 					break;
 			}
 		}
 
 		this.buffer = ByteBuffer.allocate(this.rowMaxSize * NUM_ROWS_TO_BUFFER);
 		this.buffer.order(ByteOrder.LITTLE_ENDIAN);
 		this.buffer.clear();
 
 		for (ColumnSpec column : columns) {
 			column.setMainBuffer(buffer);
 		}
 	}
     
 	public void writeHeader() throws IOException {
 		// File signature
 		buffer.put("NATIVE".getBytes(charset)).put(BYTE_LF).put(BYTE_FULL).put(BYTE_CR).put(BYTE_LF).put(BYTE_ZERO);
 
 		// Header area length (5 bytes for next three puts + (4 * N columns))
 		buffer.putInt(5 + (4 * columnCount));
 
 		// NATIVE file version
 		buffer.putShort((short)1);
 
 		// Filler (Always 0)
 		buffer.put(BYTE_ZERO);
 
 		// Number of columns
 		buffer.putShort((short)columnCount);
 		
 		for (ColumnSpec column : columns) {
 			buffer.putInt(column.bytes);
 		}   
 	}
 	
 	public void writeRow(Object[] row) throws IOException {
 		if (row == null) {
 			flushAndClose();
 			return;
 		} else if(row.length < columnCount) {
 			throw new IllegalArgumentException("Invalid incoming row for given column spec.");
 		}
 		
 		rowNulls.clear();
 		checkAndFlushBuffer();
 
 		int rowDataSize = 0;
 
 		// record the start of this row so we can come back and update the size and nulls
 		int rowDataSizeFieldPosition = buffer.position();
 		buffer.putInt(rowDataSize);
 		int rowNullsFieldPosition = buffer.position();
 		rowNulls.writeBytesTo(buffer);
 
 		for (int i = 0; i < columnCount; i++) {
 			ColumnSpec colSpec = columns.get(i);
 			Object value = row[i];
 			
 			if (value == null) {
 				rowNulls.setBit(i);
 			} else {
 				colSpec.encode(value);
 				rowDataSize += colSpec.bytes;
 			}
 		}
 
 		// Now fill in the row header
 		buffer.putInt(rowDataSizeFieldPosition, rowDataSize);
 		rowNulls.writeBytesTo(rowNullsFieldPosition, buffer);
     
 	}
 	
 	private void flushAndClose() throws IOException {
 		flushBuffer();
     channel.close();
     pipedOutputStream.flush();
     pipedOutputStream.close();
 	}
 	
 	private void checkAndFlushBuffer() throws IOException {
 		if (buffer.position() + rowMaxSize > buffer.capacity()) {
 			flushBuffer();
 		}
 	}
 
 	private void flushBuffer() throws IOException {
 		buffer.flip();
 		channel.write(buffer);
 		buffer.clear();
 	}
 
 	private class BitSet {
 		private byte[]	bytes;
 		private boolean dirty = false;
 		private int numBits;
 		private int numBytes;
 
 		private BitSet(int numBits) {
 			this.numBits = numBits;
 			this.numBytes = (int) Math.ceil((double) numBits / 8.0d);
 			bytes = new byte[this.numBytes];
 		}
 
 		private void setBit(int bitIndex) {
 			if (bitIndex < 0 || bitIndex >= numBits) {
 				throw new IllegalArgumentException("Invalid bit index");
 			}
 
 			int byteIdx = (int) Math.floor((double) bitIndex / 8.0d);
 
 			int bitIdx = bitIndex - (byteIdx * 8);
			bytes[byteIdx] |= (1 << bitIdx);
 			
 			dirty = true;
 		}
 
 		private void clear() {
 			if (dirty) {
 				for (int i = 0; i < numBytes; i++) {
 					bytes[i] = BYTE_ZERO;
 				}
 				dirty = false;
 			}
 		}
 		
 		private int numBytes() {
 			return bytes.length;
 		}
 
 		private void writeBytesTo(ByteBuffer buf) {
 			buf.put(bytes);
 		}
 		private void writeBytesTo(int index, ByteBuffer buf) {
 			for (int i = 0; i < bytes.length; i++) {
 				buf.put(index + i, bytes[i]);
 			}
 		}
 	}
 
 }
