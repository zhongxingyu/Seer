 /*--------------------------------------------------------------------------
  *  Copyright 2010 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // Kmer.java
 // Since: 2010/11/10
 //
 //--------------------------------------
 package org.utgenome.format.fasta;
 
 import org.utgenome.gwt.utgb.client.bio.ACGTEncoder;
 
 /**
  * K-mer integer representation of ACGT.
  * 
  * The difference with {@link CompactACGT} is this K-mer representation is modifiable to facilitate genome sequence
  * editing.
  * 
  * @author leo
  * 
  */
 public class Kmer implements GenomeSequence {
 
 	private byte[] sequence2bit;
 	private int size;
 
 	private Kmer(byte[] sequence2bit, int size) {
 		this.sequence2bit = sequence2bit;
 		this.size = size;
 	}
 
 	public Kmer(Kmer other) {
 		this.sequence2bit = new byte[other.sequence2bit.length];
 		this.size = other.size;
 		// copy
 		for (int i = 0; i < sequence2bit.length; ++i) {
 			sequence2bit[i] = other.sequence2bit[i];
 		}
 	}
 
 	public Kmer(CompactACGT acgt) {
 		this.size = acgt.length();
 		sequence2bit = new byte[acgt.sequence.length];
 		for (int i = 0; i < sequence2bit.length; ++i) {
 			sequence2bit[i] = 0;
 		}
 
 		for (int i = 0; i < acgt.length(); ++i) {
 			// TODO byte copy
 			set(i, acgt.charAt(i));
 		}
 	}
 
 	public Kmer(String acgt) {
 		this.size = acgt.length();
 		sequence2bit = new byte[this.size / 4 + (this.size % 4 == 0 ? 0 : 1)];
 		for (int i = 0; i < sequence2bit.length; ++i) {
 			sequence2bit[i] = 0;
 		}
 		for (int i = 0; i < acgt.length(); ++i) {
 			set(i, acgt.charAt(i));
 		}
 	}
 
 	public int toInt() {
 		return toInt(size);
 	}
 
 	public int toInt(int firstKmer) {
 		if (firstKmer > 30)
 			throw new IllegalArgumentException("The size must be <= 30: K = " + firstKmer);
 		if (firstKmer > size) {
 			throw new IllegalArgumentException("K must be less than or equal to " + size);
 		}
 
 		int kmerInteger = 0;
 		final int lastIndex = firstKmer / 4 + (firstKmer % 4 == 0 ? 0 : 1);
 		final int K = firstKmer;
 		for (int i = 0; i < lastIndex; i++) {
 			if (i > 0) {
 				kmerInteger <<= 8;
 			}
 
 			int shiftSize = 0;
 			if (i == lastIndex - 1) {
 				int offset = K % 4;
 				if (offset > 0)
 					shiftSize = 8 - offset * 2;
 			}
 			int mask = 0xFF;
 			mask >>>= shiftSize;
 
 			kmerInteger |= (sequence2bit[i] >>> shiftSize) & mask;
 		}
 
 		return kmerInteger;
 	}
 
 	public int length() {
 		return size;
 	}
 
 	public char charAt(int index) {
 		final int offset = index % 4;
 		byte code = (byte) (sequence2bit[index / 4] >>> (6 - offset * 2) & 0x03);
 		return ACGTEncoder.toBase(code);
 	}
 
 	public void set(int index, char acgt) {
 		setACGT(sequence2bit, index, acgt);
 	}
 
 	public void set(int index, String acgt) {
 		set(index, acgt.charAt(0));
 	}
 
 	public Kmer delete(int deletePos, int deleteLen) {
 
 		if (deletePos < 0)
 			throw new IllegalArgumentException("deletePos must be >= 0");
 
 		if (deletePos + deleteLen > size) {
 			deleteLen = size - deletePos;
 		}
 		final int newSize = size - deleteLen;
 
 		// prepare the new array for the inserted sequence 
 		byte[] new2bitArray = new byte[newSize / 4 + (newSize % 4 == 0 ? 0 : 1)];
 		for (int i = 0; i < new2bitArray.length; ++i)
 			new2bitArray[i] = 0;
 
 		// paste the prefix
 		final int prefixBlockSize = deletePos / 4 + (deletePos % 4 == 0 ? 0 : 1);
 		for (int i = 0; i < prefixBlockSize; i++) {
 
 			int mask = 0xFF;
 			if (i == prefixBlockSize - 1) {
 				int offset = deletePos % 4;
 				if (offset > 0) {
 					int shiftSize = 8 - offset * 2;
 					mask >>>= shiftSize;
 					mask <<= shiftSize;
 				}
 			}
 
 			new2bitArray[i] = (byte) (sequence2bit[i] & mask);
 		}
 
 		// paste the suffix
 		final int suffixStart = deletePos + deleteLen;
 		for (int i = suffixStart; i < size; ++i) {
 			setACGT(new2bitArray, i - deleteLen, charAt(i));
 		}
 
 		this.sequence2bit = new2bitArray;
 		this.size = newSize;
 
 		return this;
 	}
 
 	public Kmer append(String acgt) {
 		return insert(this.size, acgt);
 	}
 
 	public Kmer insert(int insertPos, String acgt) {
 
 		if (insertPos < 0)
 			throw new IllegalArgumentException("insertPos must be >= 0");
 
 		final int insertLen = acgt.length();
 		final int newSize = size + insertLen;
 
 		// prepare the new array for the inserted sequence 
 		byte[] new2bitArray = new byte[newSize / 4 + (newSize % 4 == 0 ? 0 : 1)];
 		for (int i = 0; i < new2bitArray.length; ++i)
 			new2bitArray[i] = 0;
 
 		// paste the prefix
 		final int prefixBlockSize = insertPos / 4 + (insertPos % 4 == 0 ? 0 : 1);
 		for (int i = 0; i < prefixBlockSize; i++) {
 
 			int mask = 0xFF;
 			if (i == prefixBlockSize - 1) {
 				int offset = insertPos % 4;
 				if (offset > 0) {
 					int shiftSize = 8 - offset * 2;
 					mask >>>= shiftSize;
 					mask <<= shiftSize;
 				}
 			}
 
 			new2bitArray[i] = (byte) (sequence2bit[i] & mask);
 		}
 
 		// paste the inserted sequence
 		for (int i = 0; i < acgt.length(); i++) {
 			final int blockPos = (insertPos + i) / 4;
 			final int blockOffset = (insertPos + i) % 4;
 			setACGT(new2bitArray, insertPos + i, acgt.charAt(i));
 		}
 
 		// paste the suffix
 		for (int i = insertPos; i < size; ++i) {
 			setACGT(new2bitArray, i + insertLen, charAt(i));
 		}
 
 		this.sequence2bit = new2bitArray;
 		this.size = newSize;
 		return this;
 	}
 
 	private static void setACGT(byte[] array, int index, char acgt) {
 		int code = ACGTEncoder.to2bitCode(acgt) & 0x03;
 		int shiftSize = 6 - (index % 4) * 2;

		final int blockIndex = index / 4;

		array[blockIndex] &= ~(0x03 << shiftSize);
		array[blockIndex] |= (byte) (code << shiftSize);
 	}
 
 	private static void setACGTCode(byte[] array, int index, int code) {
 		array[index / 4] |= (byte) ((code & 0x03) << (6 - (index % 4) * 2));
 	}
 
 	public Kmer reverseComplement() {
 		byte[] rc = new byte[sequence2bit.length];
 
 		int rIndex = 0;
 		for (int i = size - 1; i >= 0; --i, rIndex++) {
 			final int offset = i % 4;
 			int code = (sequence2bit[i / 4] >>> (6 - offset * 2)) & 0x03;
 			setACGTCode(rc, rIndex, ~code);
 		}
 
 		return new Kmer(rc, size);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder s = new StringBuilder();
 		for (int i = 0; i < size; i++) {
 			s.append(this.charAt(i));
 		}
 		return s.toString();
 
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!Kmer.class.isInstance(obj))
 			return false;
 
 		Kmer other = Kmer.class.cast(obj);
 
 		if (this.size != other.size)
 			return false;
 
 		for (int i = 0; i < this.sequence2bit.length; ++i) {
 			if (this.sequence2bit[i] != other.sequence2bit[i])
 				return false;
 		}
 
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 
 		int h = 3;
 		for (int i = 0; i < this.sequence2bit.length; ++i)
 			h += sequence2bit[i] * 1997;
 
 		return h;
 	}
 
 }
