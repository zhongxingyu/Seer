 package com.szas.server.gwt.client;
 
 import java.util.ArrayList;
 
 public final class ToSyncElementsHolder {
 	public String className;
 	public ArrayList<Object> elementsToSync; // ArrayList<LocalTuple<T>>
	public long lastTimestamp;
 }
