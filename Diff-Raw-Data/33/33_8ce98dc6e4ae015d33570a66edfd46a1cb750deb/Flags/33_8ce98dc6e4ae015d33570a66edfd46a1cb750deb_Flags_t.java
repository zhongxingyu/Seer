 //  Flags.java
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 public class Flags extends Number implements Cloneable{
 	protected int flags = 0;
 	
 	public Flags (int source) {
 		flags = source;
 	}
 	
 	public static Flags makeFlags(byte[] source) {
 		return new Flags ((int) source[0]);
 	}
 	
 	public static Flags makeFlags(Number source) {
 		return new Flags (source.intValue());
 	}
 	
 	public Object clone() {
 		return new Flags(flags);
 	}
 	
 	public boolean equals(Object obj) {
 /*		if(obj instanceof Flags){
 			return flags == ((Flags)obj).intValue();
 		} else { */
 			if (obj instanceof Number)
 				return flags == ((Number)obj).intValue();
 			else
 				return false;
 	//	}
 	}
 	/*
 	public int toInt() {
 		return flags;
 	} */
 	
 	public Integer toInteger() {
 		return new Integer(flags);
 	}
	
	public static boolean getFlag(int reg, int flags) {
 		int tmp = flags >>> reg;
 		tmp = tmp & 1;
		return (tmp == 1);	
	}

	public boolean getFlag(int reg) {
		return getFlag(reg, flags);
 	}
 	
 	public byte[] toBytes () {
 		return new byte[] {(byte)flags};
 	}
 	
 	public Flags changeFlag(int reg, boolean value) {
 		if(value == getFlag(reg))
 			return this;
 		else {
 			int tmp = 1 << reg;
 			return new Flags(flags + (value?tmp:(-tmp)));
 		 }
 	}
 	
 	public MutableFlags mutableClone () {
 		return new MutableFlags(flags);
 	}
 	
 	public int intValue() {
 		return flags;
 	}
 	
 	public long longValue() {
 		return (long)flags;
 	}
 	
 	public float floatValue() {
 		return (float)flags;
 	}
 	
 	public double doubleValue() {
 		return (double)flags;
 	}
 	
 	public byte byteValue() {
 		return (byte)flags;
 	}
 	
 	public short shortValue() {
 		return (short)flags;
 	}
 	
 	public Flags and(int otherFlags) {
 		return new Flags (flags & otherFlags);
 	}
 	
 	public Flags and(Number otherFlags) {
 		return and(otherFlags.intValue());
 	}
 	
 	public Flags or(int otherFlags) {
 		return new Flags (flags | otherFlags);
 	}
 	
 	public Flags or(Number otherFlags) {
 		return or(otherFlags.intValue());
 	}
 	
 	public Flags xor(int otherFlags) {
 		return new Flags (flags ^ otherFlags);
 	}
 	
 	public Flags xor(Number otherFlags) {
 		return xor(otherFlags.intValue());
 	}
 	
 	public String toString() {
 		return this.getClass().getName() + " : " + flags;
 	}
 	
 }
