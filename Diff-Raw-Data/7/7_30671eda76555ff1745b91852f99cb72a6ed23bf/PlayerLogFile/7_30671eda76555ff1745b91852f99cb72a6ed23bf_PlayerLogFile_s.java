 package psy.ActivityHistory;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import psy.util.TimeRange;
 
 public class PlayerLogFile{
     private ArrayList<TimeRange> sessions;
     private Date firstSession;
     private File file;
     
     /**
      * Creates and wraps a new {@link File} from a pathname.
      * @param pathname The path of the file to be created
      */
     public PlayerLogFile(String pathname) throws FileNotFoundException, IOException{
         this(new File(pathname));
     }
     
     /**
      * Wraps a pre-existing {@link File}.
      * @param file The file to be wrapped
      */
     public PlayerLogFile(File initFile) throws FileNotFoundException, IOException{
         file = initFile;
         file.createNewFile();
         sessions = new ArrayList<TimeRange>();
         firstSession = null;
         loadSessions();
         firstSession = getFirstSession();
     }
     
     //Returns true if loading successful, false if an error was caught
     private void loadSessions() throws FileNotFoundException, IOException{
         BufferedReader br = new BufferedReader(new FileReader(file));
         while(true){
             String line = new String();
                 line = br.readLine();
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
             sessions.add(new TimeRange(date, len*60000));
         }
         br.close();
         //Save any changes made when fixing invalid data
         saveSessions();
     }
     
     private void saveSessions() throws IOException{
         BufferedWriter bw = writer(false);
         for(TimeRange session : sessions){
             bw.write(session.getStart().getTime() + ":" + session.lengthInMinutes());
             bw.newLine();
         }
         bw.flush();
         bw.close();
     }
     
     /**Adds a session to the file
      * @param time The time of the survey
      * @param len The survey interval or session length
      */
     public void addSession(long time, int len) throws IOException{
         sessions.add(new TimeRange(new Date(time), len*60000));
     	BufferedWriter bw = writer(true);
         bw.write("" + time + ":" + len);
         bw.newLine();
         bw.flush();
     }
     
     /**Removes sessions from within a TimeRange
      * @param range Delete sessions from within this range
      */
     public void removeSessions(TimeRange range) throws IOException{
     	ArrayList<TimeRange> matches = getSessions(range);
     	for(TimeRange match : matches){
     		sessions.remove(match);
     	}
         saveSessions();
     }
     
     public ArrayList<TimeRange> getSessions(TimeRange range){
     	ArrayList<TimeRange> matches = new ArrayList<TimeRange>();
     	for(TimeRange session : sessions){
           	TimeRange overlap = range.overlap(session);
           	if(overlap!=null){
           		matches.add(overlap);
           	}
           }
     	return matches;
     }
     
     public String tallyActivityTotal(TimeRange range){
         if(range.getStart() == null) range.setStart(firstSession);
         int time = 0;
     	for(TimeRange session : sessions){
           	TimeRange overlap = range.overlap(session);
           	if(overlap!=null)
                 time+=overlap.lengthInMinutes();
         }
         if(time == -1 || range.getStart() == null) return ActivityHistory.messages.getString("errors.playerNotFound");
         int hours = time / 60, minutes = time % 60;
         return "" + hours + " hours, " + minutes + " minutes";
     }
     
     @SuppressWarnings("deprecation")
     public double tallyActivityPercent(TimeRange range, int hour){
         if(range.getStart() == null) range.setStart(firstSession);
         double time = 0;
         for(TimeRange session : sessions){
             if(hour == -1 || session.getStart().getHours() == hour){
               	TimeRange overlap = range.overlap(session);
               	if(overlap!=null)
                     time+=overlap.lengthInMinutes();
             }
         }
         if(time == 0) return -1;
         
         //Calculate activity percent to two decimal places
         double percent;
         if(hour == -1)
         	percent = time/range.lengthInMinutes();
         else
             //Compensate for only 1 hour a day being searched
         	percent = 24.0*time/range.lengthInMinutes();
         percent *= 100;
         percent = Math.round(percent);
         percent /= 100;
         return percent;
     }
     
     private BufferedWriter writer(boolean append) throws FileNotFoundException, IOException{
         return new BufferedWriter(new FileWriter(file, append));
     }
     
     private Date getFirstSession(){
         Date first = new Date();
        Date[] dates = new Date[sessions.size()];
         dates = sessions.toArray(dates);
         for(int i=0; i<dates.length; i++){
            if(dates[i].before(first))
                first = dates[i];
         }
         return first;
     }
 }
