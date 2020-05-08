 package soofw.trk;
 
 import android.util.Log; // FIXME
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 class TaskList {
 	String lastFilter = "";
 	File file = null;
 	ArrayList<Task> mainList = new ArrayList<Task>();
 	ArrayList<Task> filterList = new ArrayList<Task>();
 	ArrayList<String> tagList = new ArrayList<String>();
 	ArrayList<String> complexTagList = new ArrayList<String>();
 	ArrayList<String> tagFilters = new ArrayList<String>();
 
 	TaskList(File file) {
 		this.file = file;
 	}
 
 	void read() {
 		try {
 			String line = null;
 			BufferedReader reader = new BufferedReader(new FileReader(this.file));
 
 			while(true) {
 				line = reader.readLine();
 				if(line == null) break;
 
 				this.mainList.add(new Task(line));
 			}
 			Collections.sort(this.mainList);
 			this.filterList.addAll(this.mainList);
 			this.generateTagList();
 
 			reader.close();
 		} catch(FileNotFoundException e) {
 			Log.e("TRK", e.getMessage());
 			this.tutorial();
 		} catch(IOException e) {
 			// FIXME
 			Log.e("TRK", e.getMessage());
 		}
 	}
 	void write() {
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));
 
 			for(int i = 0; i < this.mainList.size(); i++) {
 				writer.write(this.mainList.get(i).source + "\n");
 			}
 
 			writer.flush();
 			writer.close();
 		} catch(IOException e) {
 			// FIXME
 			Log.e("TRK", e.getMessage());
 		}
 	}
 	void tutorial() {
 		this.add("Tag by due date 12/31");
 		this.add("Tag by a project +garden");
 		this.add("Tag by a location @home");
 		this.add("Tag by anything #life");
 		this.add("Tag by everything +shopping @store #life");
 		this.add("Tag by priority !1");
 		this.add("Or tag by low priority !0");
 		this.add("Use subtags +work/tpsreports @office/garbage");
 		this.add("Type to search or add");
 		this.add("Long press to edit");
 		this.add("Long press to filter +by @the #tags");
 		this.add("Slide out from the left to filter");
 		this.filterList.addAll(this.mainList);
 	}
 
 	void add(String source) {
 		this.mainList.add(new Task(source));
 		this.update();
 	}
 	void add(Task source) {
 		this.mainList.add(source);
 		this.update();
 	}
 	void set(int id, String source) {
 		this.mainList.set(id, new Task(source));
 		this.update();
 	}
 	void set(int id, Task source) {
 		this.mainList.set(id, source);
 		this.update();
 	}
 	void remove(Task task) {
 		this.mainList.remove(task);
 		this.update();
 	}
 	Task get(int id) {
 		return this.mainList.get(id);
 	}
 	int indexOf(Task task) {
 		return this.mainList.indexOf(task);
 	}
 
 	void update() {
 		Collections.sort(this.mainList);
 		this.generateTagList();
		// FIXME bug/26
		for(int i = 0; i < this.tagFilters.size(); i++) {
			if(!this.tagList.contains(this.tagFilters.get(i))) {
				this.tagFilters.remove(i);
				i--;
			}
		}
 	}
 
 	void generateTagList() {
 		this.tagList.clear();
 		this.complexTagList.clear();
 
 		for(int i = 0; i < this.mainList.size(); i++) {
 			String[] tags = this.mainList.get(i).getTags();
 			for(int j = 0; j < tags.length; j++) {
 				char type = tags[j].charAt(0);
 				switch(type) {
 					case '+':
 					case '@':
 					case '#':
 						String[] subtags = tags[j].substring(1).split("/");
 						if(!this.complexTagList.contains(tags[j])) {
 							this.complexTagList.add(tags[j]);
 						}
 						for(int k = 0; k < subtags.length; k++) {
 							if(this.tagList.contains(type + subtags[k])) continue;
 							this.tagList.add(type + subtags[k]);
 						}
 						break;
 					default:
 						if(this.tagList.contains(tags[j])) continue;
 						this.tagList.add(tags[j]);
 				}
 			}
 		}
 		Collections.sort(this.tagList, new Comparator<String>() {
 			@Override
 			public int compare(String one, String two) {
 				Calendar c1 = Task.matcherToCalendar(Task.re_date.matcher(one));
 				Calendar c2 = Task.matcherToCalendar(Task.re_date.matcher(two));
 				if(c1 != null && c2 == null) {
 					return -1;
 				} else if(c1 == null && c2 != null) {
 					return 1;
 				} else if(c1 != null && c2 != null) {
 					if(c1 != null && c2 != null && !c1.equals(c2)) {
 						return c1.compareTo(c2);
 					}
 				}
 
 				if(one.charAt(0) == '!' && two.charAt(0) == '!') {
 					return two.compareTo(one);
 				}
 
 				return one.toLowerCase().compareTo(two.toLowerCase());
 			}
 		});
 		Collections.sort(this.complexTagList, new Comparator<String>() {
 			@Override
 			public int compare(String one, String two) {
 				if(one.charAt(0) == '!' && two.charAt(0) == '!') {
 					return two.compareTo(one);
 				}
 
 				return one.toLowerCase().compareTo(two.toLowerCase());
 			}
 		});
 	}
 
 	void addTagFilter(String tag) {
 		if(!this.tagFilters.contains(tag)) {
 			this.tagFilters.add(tag);
 		}
 	}
 	void removeTagFilter(String tag) {
 		if(this.tagFilters.contains(tag)) {
 			this.tagFilters.remove(tag);
 		}
 	}
 	boolean hasTagFilter(String tag) {
 		return this.tagFilters.contains(tag);
 	}
 	void clearTagFilters() {
 		this.tagFilters.clear();
 	}
 
 	void filter(String search) {
 		this.lastFilter = search;
 		this.filterList.clear();
 		for(int i = 0; i < this.mainList.size(); i++) {
 			if(!this.mainList.get(i).contains(search)) continue;
 			if(this.tagFilters.size() > 0) {
 				boolean add = false;
 				for(int j = 0; j < this.tagFilters.size(); j++) {
 					if(this.mainList.get(i).matches(this.tagFilters.get(j))) {
 						add = true;
 						break;
 					}
 				}
 				if(!add) continue;
 			}
 
 			this.filterList.add(this.mainList.get(i));
 		}
 	}
 	void filter() {
 		this.filter(this.lastFilter);
 	}
 }
