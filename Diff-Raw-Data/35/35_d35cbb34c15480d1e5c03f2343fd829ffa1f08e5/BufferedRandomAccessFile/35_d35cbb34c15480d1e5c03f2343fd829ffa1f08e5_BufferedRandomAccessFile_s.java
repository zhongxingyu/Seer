 
 package com.github.erasmux.AndLibUtils;
 
 import java.io.*;
 
 /// Wrapper for RandomAccessFile to compensate for its slow reading performance if reading
 /// the bytes one by one. Notice this only buffers the input, writting is still one byte
 /// at a time. Interface might also be lacking, as I only added the functions I needed.
 public class BufferedRandomAccessFile {
 
     private static final int DefaultBufferSize = 1024;
 
     private RandomAccessFile raf_;
     private long pos_ = 0;
     private int buffered_ = 0;
     private int bufInd_ = 0;
     private byte[] buf_ = new byte[DefaultBufferSize];
 
     public BufferedRandomAccessFile(File file, String mode) throws FileNotFoundException {
         raf_ = new RandomAccessFile(file, mode);
     }
 
     public BufferedRandomAccessFile(String file, String mode) throws FileNotFoundException {
         raf_ = new RandomAccessFile(file, mode);
     }
 
     public void setBufferSize(int newBufSize) {
         buf_ = new byte[newBufSize];
     }
 
     public byte readByte() throws IOException {
         if (bufInd_ >= buffered_) {
             buffered_ = raf_.read(buf_);
             bufInd_ = 0;
             if (buffered_ <= 0)
                 throw new EOFException();
         }
         pos_++;
         return buf_[bufInd_++];
     }
 
     public int readUnsignedByte() throws IOException {
         byte signed = readByte();
         return signed < 0 ? 0x100 + signed : signed;
     }
 
     public int readUnsignedShort() throws IOException {
         return readUnsignedByte() |
             (readUnsignedByte() << 8);
     }
 
     public long readUnsignedInt() throws IOException {
         return readUnsignedByte() |
             (readUnsignedByte() << 8) |
             (readUnsignedByte() << 16) |
             ((long)readUnsignedByte() << 24);        
     }
 
     public void writeByte(byte b) throws IOException {
         if (bufInd_ < buffered_) {
             raf_.seek(pos_);
             buffered_ = 0;
             bufInd_ = 0;
         }
         raf_.write(b);
         pos_++;
     }
 
     public void writeUnsignedByte(int v) throws IOException {
         v = v & 0xFF;
         byte b = v < 0x80 ? (byte) v : (byte)( v - 0x100 );
         writeByte(b);
     }
 
     public void writeUnsignedShort(int v) throws IOException {
         writeUnsignedByte(v);
         writeUnsignedByte(v >> 8);
     }
 
     public void writeUnsignedInt(long v) throws IOException {
         writeUnsignedByte((int)  v);
         writeUnsignedByte((int) (v >> 8));
         writeUnsignedByte((int) (v >> 16));
         writeUnsignedByte((int) (v >> 24));
     }
 
     public void skipBytes(int bytes) throws IOException {
         bufInd_ += bytes;
         if (bufInd_ > buffered_) {
             raf_.skipBytes(bufInd_ - buffered_);
             buffered_ = 0;
             bufInd_ = 0;
         }
         pos_ += bytes;
     }
 
     public void seek(long ofs) throws IOException {
         if (ofs != pos_) {
             raf_.seek(ofs);
             pos_ = ofs;
             buffered_ = 0;
             bufInd_ = 0;
         }
     }
 
     public long getFilePointer() {
         return pos_;
     }
 
 }
