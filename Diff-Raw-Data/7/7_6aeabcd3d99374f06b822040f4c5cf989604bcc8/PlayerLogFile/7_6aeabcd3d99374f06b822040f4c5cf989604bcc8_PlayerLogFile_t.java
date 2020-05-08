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
 
 import psy.util.TimeRange;
 
 public class PlayerLogFile extends File{
     HashMap<Date, Integer> sessions;
     BufferedReader br;
     //This writer appends
     BufferedWriter bw1;
     //This writer overwrites
     BufferedWriter bw2;
     
     @SuppressWarnings("unchecked")
     public PlayerLogFile(String pathname) throws FileNotFoundException, IOException{
         super(pathname);
         sessions = new HashMap();
         loadSessions();
         br = new BufferedReader(new FileReader(this));
         bw1 = new BufferedWriter(new FileWriter(this, true));
         bw2 = new BufferedWriter(new FileWriter(this, false));
     }
     
     //Returns true if loading successful, false if an error was caught
     private boolean loadSessions(){
         while(true){
             String line;
             try{
                 line = br.readLine();
             }catch(IOException e){
                 break;
             }catch(NullPointerException e){
                 break;
             }
             if(line==null) break;
             else if (line.trim().equals("")) continue;
             String[] data = line.split(",");
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
         //Save any invalid data that was detected and fixed when loading
         return saveSessions();
     }
     
     //Returns true if saving was successful, false if an error was caught
     private boolean saveSessions(){
         for(Date key : sessions.keySet()){
             try{
                 bw2.write(key.getTime() + "," + sessions.get(key));
                 bw2.newLine();
             }catch(IOException e){
                 continue;
             }
         }
         try{
             bw2.flush();
             return true;
         }catch(IOException e){
             return false;
         }catch(NullPointerException e){
             return false;
         }
         
     }
     
     //Returns true if addition was successful, false if an error was caught
     public boolean addSession(long time, int len){
         try{
             bw1.write("" + time + "," + len);
             bw1.newLine();
             bw1.flush();
         }catch(IOException e){
             return false;
         }
         return true;
     }
     
     public String tallyActivityPercent(Date start, Date end, int hour){
        long time = -1;
         for(Date date : sessions.keySet()){
            if(time==-1) time = date.getTime();
             if(matchesConditions(date, start, end, hour))
                 time+=sessions.get(date);
         }
        if(time == -1) return "There is no record of that player.";
         long startLong = new Long(start.getTime());
         long dateLong = new Long((new Date()).getTime());
         long timeDiff = dateLong - startLong;
         timeDiff /= 1000;
         timeDiff /= 60;
         if(hour != -1)
             timeDiff /= 24;
         return "" + ((double)time)/timeDiff + "%";
     }
     
     @SuppressWarnings("deprecation")
     private boolean matchesConditions(Date date, Date start, Date end, int hour){
         if(!date.before(end))
             return false;
         if(start != null && !date.after(start))
             return false;
         if(hour != -1 && date.getHours() != hour)
             return false;
         return true;
     }
 }
