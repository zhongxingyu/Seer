 package org.openstack.burrow.example.syslog;
 
 public class LogEntry {
     private String time;
     private String level;
     private String tag;
     private String body;
 
     //TODO: Allow configuration of entry parsing via syslog format string
     //For now, assume RSYSLOG_TraditionalFileFormat
     public static LogEntry fromRawEntry(String raw) throws MalformedEntryException {
         String[] split = raw.split(" ", 4);
         if (split.length < 4) throw new MalformedEntryException();
 
         return new LogEntry(split[0], split[1], split[2], split[3]);
     }
 
     public LogEntry(String time, String level, String tag, String body) {
         this.time = time;
         this.level = level;
         this.tag = tag;
         this.body = body;
     }
 
     public String getTimeStamp() {
         return time;
     }
 
     public String getLevel() {
         return level;
     }
 
     public String getTag() {
         return tag;
     }
 
     public String getBody() {
         return body;
     }
 }
