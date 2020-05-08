 package soofw.trk;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 class Task implements Comparable<Task> {
 	final static Pattern re_tag      = Pattern.compile("(^|\\s)([\\@\\#\\+]([\\w\\/]+))");
 	final static Pattern re_at       = Pattern.compile("(^|\\s)(\\@([\\w\\/]+))");
 	final static Pattern re_hash     = Pattern.compile("(^|\\s)(\\#([\\w\\/]+))");
 	final static Pattern re_plus     = Pattern.compile("(^|\\s)(\\+([\\w\\/]+))");
 	final static Pattern re_priority = Pattern.compile("(^|\\s)(\\!(\\d))");
 	final static Pattern re_date     = Pattern.compile("((\\d{1,2})/(\\d{1,2})(/(\\d{2}))*([@ ](\\d{1,2})(:(\\d{1,2}))*(am|pm)*)*)");
 	final static Pattern re_flags    = Pattern.compile("^([x*]+)\\s+");
 	final static int DONE  = 1 << 0;
 	final static int NOW   = 1 << 1;
 	final static int LATER = 1 << 2;
 
 	String source = null;
 	String pretty = null;
 	String sortVal = null;
 	String searchVal = null;
 	int flags = 0;
 	int priority = 0;
 	Calendar calendar = null;
 	ArrayList<String> tags = new ArrayList<String>();
 
 	Task(String source) {
 		this.source = source.trim();
 		this.pretty = this.source;
 
 		this.searchVal = this.pretty = this.pretty.replaceAll(re_flags.pattern(), "");
 		this.pretty = this.pretty.replaceAll(re_date.pattern(), "");
 		this.sortVal = this.pretty = this.pretty.replaceAll(re_priority.pattern(), "");
 		this.pretty = this.pretty.replaceAll(re_tag.pattern(), "");
 		this.pretty = this.pretty.replaceAll("\\s+", " ");
 		this.pretty = this.pretty.trim();
 
 		this.sortVal = this.sortVal.toLowerCase();
 
 		// find its flags
 		Matcher m = re_flags.matcher(this.source);
 		if(m.find()) {
 			if(m.group(1).contains("x")) {
 				this.flags |= Task.DONE;
 			}
 			if(m.group(1).contains("*")) {
 				this.flags |= Task.NOW;
 			}
 		}
 
 		// find the priority
 		m = re_priority.matcher(this.source);
 		if(m.find()) {
 			this.priority = Integer.parseInt(m.group(2).substring(1));
 			if(this.priority == 0) { // !0 is actually -1 priority
 				this.priority  = -1;
 			}
 			this.tags.add(m.group(2));
 		}
 
 		// find the date
 		this.calendar = Task.matcherToCalendar(re_date.matcher(this.source));
 
 		this.addTags(re_date.matcher(this.source), 0);
 		this.addTags(re_tag.matcher(this.source), 2);
 	}
 
 	@Override
 	public String toString() {
 		return this.pretty;
 	}
 
 	@Override
 	public int compareTo(Task other) {
 		if((this.flags & Task.NOW) != (other.flags & Task.NOW)) {
 			if((this.flags & Task.NOW) == Task.NOW) {
 				return -1;
 			} else {
 				return 1;
 			}
 		}
 
 		if(this.priority != other.priority) {
 			return other.priority - this.priority;
 		}
 
 		if(this.calendar != null && other.calendar == null) {
 			return -1;
 		} else if(this.calendar == null && other.calendar != null) {
 			return 1;
 		} else if(this.calendar != null && other.calendar != null) {
 			int cmp = this.calendar.compareTo(other.calendar);
 			if(cmp != 0) {
 				return cmp;
 			}
 		}
 
 		return this.sortVal.compareTo(other.sortVal);
 	}
 
 	void addFlag(int flag) {
 		this.flags |= flag;
 		this.updateFlags();
 	}
 	void removeFlag(int flag) {
 		this.flags &= (~flag);
 		this.updateFlags();
 	}
 	void setFlag(int flag, boolean to) {
 		if(to) {
 			this.addFlag(flag);
 		} else {
 			this.removeFlag(flag);
 		}
 	}
 	boolean getFlag(int flag) {
 		return (this.flags & flag) == flag;
 	}
 	void toggleFlag(int flag) {
 		this.setFlag(flag, !this.getFlag(flag));
 	}
 	void updateFlags() {
 		this.source = this.source.replaceAll(re_flags.pattern(), "");
 		String strflags = "";
 		if((this.flags & Task.DONE) == Task.DONE) {
 			strflags += "x";
 		}
 		if((this.flags & Task.NOW) == Task.NOW) {
 			strflags += "*";
 		}
		this.source = strflags + " " + this.source;
 	}
 
 	void addTags(Matcher m, int group) {
 		while(m.find()) {
 			this.tags.add(m.group(group));
 		}
 	}
 	String[] getTags() {
 		String[] temp = new String[this.tags.size()];
 		this.tags.toArray(temp);
 		return temp;
 	}
 	int getNumTags() {
 		return this.tags.size();
 	}
 	boolean hasTags() {
 		return !this.tags.isEmpty();
 	}
 
 	boolean contains(String search) {
 		if(search.isEmpty()) {
 			return true;
 		}
 
 		String[] words = search.toLowerCase().split(" ");
 		for(int i = 0; i < words.length; i++) {
 			char type = words[i].charAt(0);
 			switch(type) {
 				case '!':
 					if(!this.matches(words[i])) {
 						return false;
 					}
 					break;
 
 				case '+': case '#': case '@':
 					if(!Pattern.matches("(^|.*\\s)\\" + type + "[\\w\\/]*" + words[i].substring(1) + "[\\w]*(\\s.*|\\/.*|$)",
 								this.searchVal.toLowerCase())) {
 						return false;
 					}
 					break;
 
 				default:
 					if(!this.searchVal.toLowerCase().contains(words[i])) {
 						return false;
 					}
 			}
 		}
 		return true;
 	}
 	boolean matches(String tag) {
 		tag = tag.toLowerCase();
 		char type = tag.charAt(0);
 		String content = tag.substring(1);
 		String regex = null;
 
 		switch(type) {
 			case '!':
 				regex = "(^|.*\\s)(\\!" + content + ")(\\s.*|$)";
 				break;
 
 			case '+': case '#': case '@':
 				regex = "(^|.*\\s)\\" + type + "([\\w\\/]*\\/)?(" + content + ")(\\s.*|\\/.*|$)";
 				break;
 
 			default:
 				regex = "(^|.*\\s)(" + tag + ")(\\s.*|$)";
 		}
 
 		return Pattern.matches(regex, this.searchVal.toLowerCase());
 	}
 
 
 	static Calendar matcherToCalendar(Matcher m) {
 		if(m.find()) {
 			Calendar temp = Calendar.getInstance();
 
 			temp.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1);
 			temp.set(Calendar.DATE, Integer.parseInt(m.group(3)));
 
 			if(m.group(5) != null) {
 				temp.set(Calendar.YEAR, Integer.parseInt(m.group(5)) + 2000);
 			}
 
 			if(m.group(7) != null) {
 				temp.set(Calendar.HOUR, Integer.parseInt(m.group(7)));
 			} else {
 				temp.set(Calendar.HOUR, 11);
 			}
 			if(m.group(9) != null) {
 				temp.set(Calendar.MINUTE, Integer.parseInt(m.group(9)));
 			} else {
 				temp.set(Calendar.MINUTE, 59);
 			}
 			if(m.group(10) != null) {
 				temp.set(Calendar.AM_PM, m.group(10).toLowerCase().equals("pm") ? Calendar.PM : Calendar.AM);
 			} else {
 				temp.set(Calendar.AM_PM, Calendar.PM);
 			}
 
 			return temp;
 		}
 
 		return null;
 	}
 }
