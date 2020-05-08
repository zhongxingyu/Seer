 package com.jpqr.checklist;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.os.Environment;
 
 public class Checklist {
 
 	public static final String DEFAULT_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/Checklists/";
 	private String mTitle;
 	private ArrayList<String> mItems;
 	private File mFile;
 
 	public Checklist() {
 		this.mTitle = "";
 		this.mItems = new ArrayList<String>();
 		this.mFile = new File(DEFAULT_DIRECTORY, "untitled.txt");
 	}
 
 	public Checklist(String path) throws IOException, FileNotFoundException {
 		this.mFile = new File(path);
 		this.mTitle = mFile.getName();
 		this.mItems = new ArrayList<String>();
 
 		BufferedReader reader = new BufferedReader(new FileReader(mFile));
 		String line;
 		while ((line = reader.readLine()) != null) {
 			mItems.add(line);
 		}
 		reader.close();
 	}
 
 
 	public void toFile() throws IOException {
		mFile.renameTo(new File(mFile.getParentFile().getPath(), mTitle));
 		
 		BufferedWriter writer = new BufferedWriter(new FileWriter(mFile));
 		for (String item : mItems) {
 			writer.write(item + "\r\n");
 		}
 		writer.close();
 	}
 
 	public ArrayList<String> getItems() {
 		return mItems;
 	}
 
 	public void setItems(ArrayList<String> items) {
 		this.mItems = items;
 	}
 
 	public void setItems(String items) {
 		this.mItems = new ArrayList<String>(Arrays.asList(items.split("\r\n")));
 	}
 
 	public String getTitle() {
 		return mTitle;
 	}
 
 	public void setTitle(String mTitle) {
 		this.mTitle = mTitle;
 	}
 
 	public void add(String item) {
 		mItems.add(item);
 	}
 
 	public String get(int index) {
 		return mItems.get(index);
 	}
 
 	public void remove(int index) {
 		mItems.remove(index);
 	}
 
 	public boolean remove(String item) {
 		return mItems.remove(item);
 	}
 }
