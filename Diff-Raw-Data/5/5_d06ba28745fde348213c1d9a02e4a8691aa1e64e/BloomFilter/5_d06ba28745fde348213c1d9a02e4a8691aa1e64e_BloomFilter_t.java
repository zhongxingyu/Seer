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
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.LongBuffer;
 import java.util.BitSet;
 
 public class BloomFilter<T> {
 	private final int numOfBits;
 	private final int numOfHashFunction;
 	private final HashFunction<T> firstFunction;
 	private final HashFunction<T> secondFunction;
 	private BitSet bitmap;
 
 	@SuppressWarnings("unchecked")
 	public BloomFilter() {
 		this(GeneralHashFunction.stringHashFunctions[2], GeneralHashFunction.stringHashFunctions[1]);
 	}
 
 	@SuppressWarnings("unchecked")
 	public BloomFilter(long capacity) {
 		this(0.001, capacity, GeneralHashFunction.stringHashFunctions[2], GeneralHashFunction.stringHashFunctions[1]);
 	}
 
 	public BloomFilter(HashFunction<T> first, HashFunction<T> second) {
 		this(0.001, 1000000L, first, second);
 	}
 
 	public BloomFilter(double errorRate, long capacity, HashFunction<T> first, HashFunction<T> second) {
 		OptimumFinder opt = new OptimumFinder(errorRate, capacity);
 		this.firstFunction = first;
 		this.secondFunction = second;
 		this.numOfHashFunction = opt.numOfHashFunction;
 		this.numOfBits = opt.numOfBits;
 		this.bitmap = new BitSet(numOfBits);
 	}
 
 	public BloomFilter(double errorRate, long capacity, HashFunction<T> first, HashFunction<T> second, BitSet bitmap) {
 		OptimumFinder opt = new OptimumFinder(errorRate, capacity);
 		this.firstFunction = first;
 		this.secondFunction = second;
 		this.numOfHashFunction = opt.numOfHashFunction;
 		this.numOfBits = opt.numOfBits;
 		this.bitmap = bitmap;
 	}
 
 	public void add(T key) {
 		int firstHashCode = firstFunction.hashCode(key);
 		int secondHashCode = secondFunction.hashCode(key);
 
 		for (int i = 0; i < numOfHashFunction; i++) {
 			int index = getIndex(firstHashCode, secondHashCode, i);
 			this.bitmap.set(index);
 		}
 	}
 
 	public boolean contains(T key) {
 		int firstHashCode = firstFunction.hashCode(key);
 		int secondHashCode = secondFunction.hashCode(key);
 
 		for (int i = 0; i < numOfHashFunction; i++) {
 			int index = getIndex(firstHashCode, secondHashCode, i);
 			if (this.bitmap.get(index) == false)
 				return false;
 		}
 		return true;
 	}
 
 	public BitSet getBitmap() {
 		return bitmap;
 	}
 
 	public void load(InputStream is) throws IOException {
 		DataInputStream dis = new DataInputStream(is);
 		int length = dis.readInt();
 
 		// try jdk 7 acceleration
 		try {
 			LongBuffer buf = LongBuffer.allocate(length / 8 + 1);
 			try {
 				while (true) {
 					long l = dis.readLong();
 					buf.put(l);
 				}
 			} catch (EOFException eof) {
 			}
 
 			buf.flip();
 			this.bitmap = BitSet.valueOf(buf);
 		} catch (NoSuchMethodError e) {
 			BitSet set = new BitSet(length);
 			int p = 0;
 			try {
 				while (true) {
 					long l = dis.readLong();
 					for (int i = 63; i >= 0; i--) {
 						if (p >= length)
 							break;
 
 						set.set(p, ((l >> i) & 1) == 1);
 						p++;
 					}
 				}
 			} catch (EOFException eof) {
 				// ignore
 			}
 			this.bitmap = set;
 		}
 	}
 
 	public long streamLength() {
 		try {
 			long[] words = bitmap.toLongArray();
			return words.length * 8 + 4;
 		} catch (NoSuchMethodError e) {
 			int count = 0;
 			long wrote = 4;
 			for (int i = 0; i < bitmap.length(); i++) {
 				if (count++ == 63) {
 					wrote += 8;
 					count = 0;
 				}
 			}
 
 			if (bitmap.length() % 64 != 0) {
 				wrote += 8;
 			}
 			return wrote;
 		}
 	}
 
 	public long save(OutputStream os) throws IOException {
 		DataOutputStream dos = new DataOutputStream(os);
 		dos.writeInt(bitmap.length());
 
 		// try jdk 7 accelration first
 		try {
 			long[] words = bitmap.toLongArray();
 			for (long word : words)
 				dos.writeLong(word);
			return words.length * 8 + 4;
 		} catch (NoSuchMethodError e) {
 			int count = 0;
 
 			long l = 0;
 			long wrote = 4;
 			for (int i = 0; i < bitmap.length(); i++) {
 				l <<= 1;
 				l |= bitmap.get(i) ? 1 : 0;
 
 				if (count++ == 63) {
 					dos.writeLong(l);
 					wrote += 8;
 					l = 0;
 					count = 0;
 				}
 			}
 
 			if (bitmap.length() % 64 != 0) {
 				l <<= 64 - count;
 				dos.writeLong(l);
 				wrote += 8;
 			}
 			return wrote;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return String.format("BloomFilter-[%d KB, %d hashFunctions (%s, %s)]", this.numOfBits / 8 / 1024, this.numOfHashFunction,
 				this.firstFunction.toString(), this.secondFunction.toString());
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
 
 }
