 package com.jpii.navalbattle.pavo.grid;
 
 public class Id {
 	short superId, subId;
	int mutexId;
 	public Id(short superId, short subId) {
 		/*if ((Integer.MAX_VALUE-1) / superId >= 65536) {
 			System.out.println((Integer.MAX_VALUE-1) / superId+"er");
 			throw new java.lang.IllegalArgumentException("Identificator is too high. Please discard excess identifications.");
 		}*/
 		this.superId = superId;
 		this.subId = subId;
		updateMutex();
 	}
 	public Id(int superId, int subId) {
 		this((short)superId,(short)subId);
 	}
	private void updateMutex() {
		mutexId = ((int)Math.abs(((int)superId) * 65536)) + subId;
	}
 	public void setSuperId(short id) {
		if (id != superId) {
			superId = id;
			updateMutex();
		}
 	}
 	public void setSubId(short id) {
		if (id != subId) {
			subId = id;
			updateMutex();
		}
 	}
 	public void setSuperId(int id) {
		if (id != superId) {
			superId = (short)id;
			updateMutex();
		}
 	}
 	public void setSubId(int id) {
		if (id != subId) {
			subId = (short)id;
			updateMutex();
		}
 	}
 	public short getSuperId() {
 		return superId;
 	}
 	public short getSubId() {
 		return subId;
 	}
 	public int getMutexId() {
		return mutexId;
 	}
 }
