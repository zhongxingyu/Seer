 /*
  * Copyright 2010 NCHOVY, Inc.
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
 package org.araqne.bloomfilter;
 
 import java.io.DataInputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.WritableByteChannel;
 import java.util.BitSet;
 
 public class BloomFilter2<T> {
 	private int numOfBits;
 	private int numOfHashFunction;
 	private final HashFunction<T> firstFunction;
 	private final HashFunction<T> secondFunction;
 	private Bitmap bitmap;
 
 	@SuppressWarnings("unchecked")
 	public BloomFilter2() {
 		this(GeneralHashFunction.stringHashFunctions[2], GeneralHashFunction.stringHashFunctions[1]);
 	}
 
 	@SuppressWarnings("unchecked")
 	public BloomFilter2(long capacity) {
 		this(0.001, capacity, GeneralHashFunction.stringHashFunctions[2], GeneralHashFunction.stringHashFunctions[1]);
 	}
 
 	public BloomFilter2(HashFunction<T> first, HashFunction<T> second) {
 		this(0.001, 1000000L, first, second);
 	}
 
 	public BloomFilter2(double errorRate, long capacity, HashFunction<T> first, HashFunction<T> second) {
 		OptimumFinder opt = new OptimumFinder(errorRate, capacity);
 		this.firstFunction = first;
 		this.secondFunction = second;
 		attach(new Bitmap(opt.numOfBits), opt.numOfBits, opt.numOfHashFunction);
 	}
 
 	public BloomFilter2(double errorRate, long capacity, HashFunction<T> first, HashFunction<T> second, BitSet bitmap) {
 		OptimumFinder opt = new OptimumFinder(errorRate, capacity);
 		this.firstFunction = first;
 		this.secondFunction = second;
 		attach(new Bitmap(opt.numOfBits), opt.numOfBits, opt.numOfHashFunction);
 	}
 
 	@SuppressWarnings("unchecked")
 	public BloomFilter2(double errorRate, int capacity) {
 		this(errorRate, capacity, GeneralHashFunction.stringHashFunctions[2], GeneralHashFunction.stringHashFunctions[1]);
 	}
 
 	// for version 1 compatibility
 	@SuppressWarnings("unchecked")
 	public BloomFilter2(double errorRate, int capacity, ByteBuffer bb) {
 		OptimumFinder opt = new OptimumFinder(errorRate, capacity);
 		this.firstFunction = GeneralHashFunction.stringHashFunctions[2];
 		this.secondFunction = GeneralHashFunction.stringHashFunctions[1];
 		attach(new Bitmap(opt.numOfBits, bb), opt.numOfBits, opt.numOfHashFunction);
 	}
 
 	@SuppressWarnings("unchecked")
 	public BloomFilter2(int numOfBits, int numOfHashFuncs, ByteBuffer bb) {
 		this.firstFunction = GeneralHashFunction.stringHashFunctions[2];
 		this.secondFunction = GeneralHashFunction.stringHashFunctions[1];
 		this.attach(new Bitmap(numOfBits, bb), numOfBits, numOfHashFuncs);
 	}
 
 	public HashValue<T> getHashValue(T key) {
 		return new HashValue<T>(key, firstFunction, secondFunction);
 	}
 
 	public void add(HashValue<T> v) {
 		for (int i = 0; i < numOfHashFunction; i++) {
 			int index = getIndex(v.getFirstHashCode(), v.getSecondHashCode(), i);
 			this.bitmap.set(index);
 		}
 	}
 
 	public void add(T key) {
 		HashValue<T> v = new HashValue<T>(key, firstFunction, secondFunction);
 
 		add(v);
 	}
 
 	public boolean contains(HashValue<T> v) {
 		for (int i = 0; i < numOfHashFunction; i++) {
 			int index = getIndex(v.getFirstHashCode(), v.getSecondHashCode(), i);
 			if (this.bitmap.get(index) == false)
 				return false;
 		}
 		return true;
 	}
 
 	public boolean contains(T key) {
 		HashValue<T> v = new HashValue<T>(key, firstFunction, secondFunction);
 
 		return contains(v);
 	}
 
 	public Bitmap getBitmap() {
 		return bitmap;
 	}
 
 	public void load(InputStream is) throws IOException {
 		DataInputStream dis = new DataInputStream(is);
 		int length = dis.readInt();
 
 		if (length < 0) {
 			// length field means version
 			int version = -length;
 			if (version == 2) {
 				int numOfHashFunc = dis.readInt();
 				int numOfBits = dis.readInt();
 				int streamLength = dis.readInt();
 
 				int longCount = streamLength / 64;
 				if (streamLength % 64 != 0)
 					longCount++;
 
 				ByteBuffer bb = ByteBuffer.allocate(longCount * 8);
 				try {
 					dis.readFully(bb.array());
 				} catch (EOFException eof) {
 				}
 
 				bb.limit(bb.capacity());
 				this.attach(new Bitmap(numOfBits, bb), numOfBits, numOfHashFunc);
 				return;
 			} else {
 				throw new IllegalArgumentException("unsupported version: " + version);
 			}
 		} else {
 			// version 1 load
 			// try jdk 7 acceleration
 			ByteBuffer bb = ByteBuffer.allocate(length / 8 + 1);
 			try {
 				dis.readFully(bb.array());
 			} catch (EOFException eof) {
 			}
 			bb.flip();
 
 			this.bitmap = new Bitmap(length, bb);
 		}
 	}
 
 	private void attach(Bitmap bm, int numOfBits, int numOfHash) {
 		this.bitmap = bm;
 		this.numOfBits = numOfBits;
 		this.numOfHashFunction = numOfHash;
 	}
 
 	public long streamLength() {
 		return streamLength(false);
 	}
 
 	public long streamLength(boolean noaccel) {
 		return bitmap.getByteLength() + getStreamHeaderLength();
 	}
 
 	private int getStreamHeaderLength() {
 		return 4 * 4;
 	}
 
 	public long save(OutputStream os) throws IOException {
 		ByteBuffer hdr = ByteBuffer.allocate(getStreamHeaderLength());
 		hdr.putInt(-2); // version
 		hdr.putInt(numOfHashFunction);
 		hdr.putInt(numOfBits);
 		hdr.putInt(bitmap.length());
 		hdr.flip();
 
 		WritableByteChannel newChannel = Channels.newChannel(os);
 		newChannel.write(hdr);
 		ByteBuffer bytes = bitmap.getBytes();
		int pos = bytes.position();
		bytes.position(0);
 		int wrote = newChannel.write(bytes);
		bytes.position(pos);
 		return wrote + getStreamHeaderLength();
 	}
 
 	@Override
 	public String toString() {
 		return String.format("BloomFilter2-[%d KB, %d hashFunctions (%s, %s)]", this.numOfBits / 8 / 1024,
 				this.numOfHashFunction, this.firstFunction.toString(), this.secondFunction.toString());
 	}
 
 	private int getIndex(int firstHashCode, int secondHashCode, int i) {
 		int index = (firstHashCode + (i * secondHashCode)) % this.numOfBits;
 		return (index < 0) ? -index : index;
 	}
 
 	private static class OptimumFinder {
 		private int numOfBits;
 		private int numOfHashFunction;
 
 		private OptimumFinder(double errorRate, long capacity) {
 			numOfBits = Integer.MAX_VALUE;
 			numOfHashFunction = 1;
 			int m = 0;
 
 			for (int k = 1; k < 20; k++) {
 				m = (int) (k * capacity * -1.0 / java.lang.Math.log(1.0 - java.lang.Math.pow(errorRate, (1.0 / k))));
 
 				if (m < numOfBits) {
 					numOfBits = m;
 					numOfHashFunction = k;
 				}
 			}
 
 			assert numOfBits > capacity;
 			assert numOfHashFunction > 1;
 		}
 	}
 
 	public int getHashFuncCount() {
 		return numOfHashFunction;
 	}
 
 }
