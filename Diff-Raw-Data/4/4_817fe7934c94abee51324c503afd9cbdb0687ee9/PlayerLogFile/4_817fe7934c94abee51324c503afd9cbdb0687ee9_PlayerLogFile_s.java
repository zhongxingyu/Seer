 package psy.ActivityHistory;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Date;
 import java.util.Stack;
 import java.util.EmptyStackException;
 
 import psy.util.TimeRange;
 
 public class PlayerLogFile{
     private HashMap<Date, Integer> sessions;
     private Date firstSession;
     private File file;
     
     public PlayerLogFile(String pathname){
         this(new File(pathname));
     }
     
     @SuppressWarnings("unchecked")
     public PlayerLogFile(File file){
         try{
             file.createNewFile();
         }catch(Exception e){
         }
         sessions = new HashMap();
         firstSession = null;
         loadSessions();
         firstSession = getFirstSession();
     }
     
     //Returns true if loading successful, false if an error was caught
     private boolean loadSessions(){
         BufferedReader br = reader();
         while(true){
             String line = new String();
             try{
                 line = br.readLine();
             }catch(IOException e){
                 break;
             }catch(NullPointerException e){
                 break;
             }
             if(line==null) break;
             else if (line.trim().equals("")) continue;
             String[] data = line.split(":");
             Date date;
             Integer len;
             try{
                 date = new Date(new Long(data[0]));
                 if(data.length<2 || data[1].trim().equals("")) len = 15;
                 else len = new Integer(data[1]);
             }catch(NumberFormatException e){
                 continue;
             }
             sessions.put(date, len);
         }
         //Save any changes made when fixing invalid data
         return saveSessions();
     }
     
     //Returns true if saving was successful, false if an error was caught
     @SuppressWarnings("unchecked")
     private boolean saveSessions(){
         BufferedWriter bw = writer(false);
         for(Date key : sessions.keySet()){
             try{
                 bw.write(key.getTime() + ":" + sessions.get(key));
                 bw.newLine();
             }catch(IOException e){
                 continue;
             }
         }
         try{
             bw.flush();
         }catch(IOException e){
             return false;
         }catch(NullPointerException e){
             return false;
         }
         try{
             bw.close();
         }catch(Exception e){
             e.printStackTrace();
             return false;
         }
         return true;
     }
     
     //Returns true if addition was successful, false if an error was caught
     public boolean addSession(long time, int len){
         BufferedWriter bw = writer(true);
         try{
             bw.write("" + time + ":" + len);
             bw.newLine();
             bw.flush();
         }catch(IOException e){
             return false;
         }
         return true;
     }
     
     //Returns true if removal was successful, false if an error was caught
     public boolean removeSessions(TimeRange range){
         for(Date date : sessions.keySet())
             if(range.includes(date))
                 sessions.remove(date);
         return saveSessions();
     }
     
     @SuppressWarnings("deprecation")
     public String tallyActivityTotal(TimeRange range){
         if(range.getStart() == null) range.setStart(firstSession);
         int time = 0;
         for(Date date : sessions.keySet()){
             if(range.includes(date) || range.getStart().equals(firstSession))
                 time+=sessions.get(date);
         }
         if(time == -1 || range.getStart() == null) return "There is no record of that player.";
         int hours = time / 60, minutes = time % 60;
         return "" + hours + "hours" + minutes + "minutes";
     }
     
     @SuppressWarnings("deprecation")
     public double tallyActivityPercent(TimeRange range, int hour){
         if(range.getStart() == null) range.setStart(firstSession);
         int time = 0;
         for(Date date : sessions.keySet()){
             if((range.includes(date) || range.getStart().equals(firstSession)) && (hour == -1 || date.getHours() == hour))
                 time+=sessions.get(date);
         }
         if(time == -1 || range.getStart() == null) return -1;
         long startLong = new Long(range.getStart().getTime());
        long dateLong = new Long((new Date()).getTime());
        long timeDiff = dateLong - startLong;
         timeDiff /= 1000;
         timeDiff /= 60;
         if(hour != -1)
             timeDiff /= 24;
         time *= 100;
         double percent = ((double)time)/timeDiff;
         percent *= 100;
         percent = Math.round(percent);
         percent /= 100;
         return percent;
     }
     
     private BufferedReader reader(){
         try{
             return new BufferedReader(new FileReader(file));
         }catch(Exception e){
             e.printStackTrace();
             return null;
         }
     }
     
     private BufferedWriter writer(boolean append){
         try{
             return new BufferedWriter(new FileWriter(file, append));
         }catch(Exception e){
             e.printStackTrace();
             return null;
         }
     }
     
     @SuppressWarnings("deprecation")
     private boolean matchesConditions(Date date, Date start, Date end, int hour){
         if(!date.before(end))
             return false;
         if(start != null && !date.after(start) && !date.equals(start))
             return false;
         if(hour != -1 && date.getHours() != hour)
             return false;
         return true;
     }
     
     private Date getFirstSession(){
         Date first = new Date();
         for(Date date : sessions.keySet())
             if(date.before(first))
                 first = date;
         return first;
     }
 }
