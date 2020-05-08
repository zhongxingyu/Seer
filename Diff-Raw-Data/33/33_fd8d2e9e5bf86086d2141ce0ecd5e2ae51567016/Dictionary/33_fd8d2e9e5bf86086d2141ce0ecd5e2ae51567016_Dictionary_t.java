 package com.lzwcompressor;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 
 public class Dictionary {
 	private TreeMap<Integer, String> dico;
 	private int index;
 	private final int maxSize;
 
 	public Dictionary() {
 		dico = new TreeMap<Integer, String>();
 		setIndex(0);
		maxSize = (1 << 12);
 	}
 
 	public Dictionary(int maxSize) {
 		dico = new TreeMap<Integer, String>();
 		setIndex(0);
 		this.maxSize = maxSize;
 	}
 
 	public static String readFileAsString(String filePath)
 			throws java.io.IOException {
 		StringBuilder fileData = new StringBuilder();
 		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 		char[] buf = new char[1024];
 		int numRead = 0;
 		while ((numRead = reader.read(buf)) != -1) {
 			fileData.append(buf, 0, numRead);
 		}
 		reader.close();
 		return fileData.toString();
 	}
 
 	public void display() {
 		Set<Integer> keySet = dico.keySet();
 		Iterator<Integer> it = keySet.iterator();
 		int nomCurr;
 		while (it.hasNext()) {
 			nomCurr = it.next();
 			System.out.println(nomCurr + " ==> " + dico.get(nomCurr));
 		}
 	}
 
 	public void init() {
 		for (char i = 0; i < 256; i++) {
 			dico.put((int) i, String.valueOf(i));
 		}
 		setIndex(256);
 	}
 
 	public String getValue(int key) {
 		return dico.get(key);
 	}
 
 	public int getKey(String value) {
 		Set<Entry<Integer, String>> entryset = dico.entrySet();
 		Iterator<Entry<Integer, String>> it = entryset.iterator();
 		int res = -1;
 		boolean found = false;
 		while (it.hasNext() && !found) {
 			Entry<Integer, String> elem = it.next();
 			if (elem.getValue().equals(value)) {
 				res = elem.getKey();
 				found = true;
 			}
 		}
 		return res;
 	}
 
 	public void put(int key, String value) {
 		if (getSize() < maxSize) {
 			dico.put(key, value);
 			setIndex(key + 1);
 		}
 	}
 
 	public int getIndex() {
 		return index;
 	}
 
 	public void setIndex(int index) {
 		this.index = index;
 	}
 
 	public boolean containsValue(String value) {
 		return dico.containsValue(value);
 	}
 
 	public boolean containsKey(int key) {
 		return dico.containsKey(key);
 	}
 
 	private int getSize() {
 		return dico.size();
 	}
 
 }
