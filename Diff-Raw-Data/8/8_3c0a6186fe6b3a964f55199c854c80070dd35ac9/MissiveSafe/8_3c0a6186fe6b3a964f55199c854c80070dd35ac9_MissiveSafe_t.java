 package com.barchart.missive.core;
 
 public abstract class MissiveSafe extends Missive implements TagMapSafe {
 
 	@Override
 	public <V> void set(Tag<V> tag, V value) throws MissiveException {
		super.set(tag, value);
 	}
 
 }
