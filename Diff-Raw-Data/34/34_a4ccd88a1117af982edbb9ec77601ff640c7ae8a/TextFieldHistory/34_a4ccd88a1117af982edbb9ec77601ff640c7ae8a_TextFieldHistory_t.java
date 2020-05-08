 package fr.frozen.iron.client.components;
 
 import java.util.LinkedList;
 
 public class TextFieldHistory {
 	
 	protected static int MAX_HISTORY = 10;
 	
 	protected static TextFieldHistory instance = new TextFieldHistory();
 	
 	public static TextFieldHistory getInstance() {
 		return instance;
 	}
 	
 	protected LinkedList<String> history;
 	protected int index;
 	
 	protected TextFieldHistory() {
 		history = new LinkedList<String>();
 		index = -1;
 	}
 	
 	public synchronized void addString(String str) {
 		if (history.size() == 0 || !history.get(0).equals(str)) {
 			if (history.size() >= MAX_HISTORY) {
 				history.removeLast();
 			}
 			history.push(str);
 		}
 	}
 	
 	public synchronized String getPrevious() {
 		if (history.size() == 0) return "";
 		
 		index = Math.min(index + 1, history.size() - 1);
 
 		if (index < 0) {
 			index = 0;
 		}
 		
 		String result = history.get(index);
 		return result;
 	}
 	
 	public synchronized String getNext() {
 
 		index --;
 		
 		if (index < 0) {
 			return "";
 		}
 		String result = history.get(index);
 		return result;	
 	}
 	
 	public synchronized void resetIndex() {
 		index = -1;
 	}
 }
