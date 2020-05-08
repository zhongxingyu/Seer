 package com.jpii.navalbattle.pavo.grid;
 
 public class Id {
 	short superId, subId;
 	public Id(short superId, short subId) {
 		/*if ((Integer.MAX_VALUE-1) / superId >= 65536) {
 			System.out.println((Integer.MAX_VALUE-1) / superId+"er");
 			throw new java.lang.IllegalArgumentException("Identificator is too high. Please discard excess identifications.");
 		}*/
 		this.superId = superId;
 		this.subId = subId;
 	}
 	public Id(int superId, int subId) {
 		this((short)superId,(short)subId);
 	}
 	public void setSuperId(short id) {
		superId = id;
 	}
 	public void setSubId(short id) {
		subId = id;
 	}
 	public void setSuperId(int id) {
		superId = (short)id;
 	}
 	public void setSubId(int id) {
		subId = (short)id;
 	}
 	public short getSuperId() {
 		return superId;
 	}
 	public short getSubId() {
 		return subId;
 	}
 	public int getMutexId() {
		return Math.abs(superId * 65536) + subId;
 	}
 }
