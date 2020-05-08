 package main;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 public class Database {
 	private boolean attributesFinished;
 	private LinkedList<String> attributes = new LinkedList<String>();
 	private LinkedList<LinkedList<Integer>> list = new LinkedList<LinkedList<Integer>>();
 
 	public void addAttribute(String attribute) {
 		if (attributesFinished) {
 			System.out.println("should not add attributes anymore");
 		}
 
 		attributes.add(attribute);
 	}
 
 	public Record newRecord() {
 		attributesFinished = true;
 
 		return new Record(attributes);
 	}
 
 	public void add(Record record) {
 		if (!record.isValid()) {
 			System.out.println("adding invalid record");
 		}
 
 		list.add(record.getValues());
 	}
 
 	public String toString() {
		return attributes.toString() + "\n" + list.toString();
 	}
 }
