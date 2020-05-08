 package tablingassigner;
 
 import java.io.*;
 import java.util.*;
 
 /**
  *  This is a temporary driver class to write out available tabling slots
  *  without using any kind of filtering algorithms. It takes in as inputs two
  *  CSV files: a list of Students and a list of TimeConflicts. From these, it
  *  generates a CSV file of TablingSlots.
  *
  *  @author Keien Ohta
  */
 
 public class TablingAssignerTemp {
     public static void run(String dir) {
         ScheduleReader reader = new ScheduleReader();
         String studentFile = dir + "members.csv";
         HashSet<Student> studentSet = null;
 
         System.out.println("Deserializing file " + studentFile + ":");
         studentSet = reader.processStudents(studentFile);
 
         String tcFile = dir + "schedules.csv";
         System.out.println("Deserializing file " + tcFile + ":");
         HashMap tcMap = null;
         tcMap = reader.processTimeConflicts(tcFile);
 
 
         try {
             String outputFileName = dir + "available_slots.csv";
             BufferedWriter tsWriter = new BufferedWriter(new FileWriter(new File(outputFileName)));
 
             System.out.println("Reading time conflicts for each student and writing to file");
             for (Student student : studentSet) {
                 student.getTimeConflicts(tcMap);
                 for (TablingSlot ts : student.availableSlots(0)) {
                     if (TablingSlot.tablingDays().contains(ts.dayOfWeek())) {
                         tsWriter.write(student.SSID() + "," +
                             ts.dayOfWeek() + "," +
                             ts.startTime() + "," +
                             ts.endTime());
                         tsWriter.newLine();
                     }
                 }
             }
             System.out.println();
             tsWriter.close();
         }
         catch (IOException e) {
             System.err.println(e);
             System.exit(1);
         }
 
     }
     public static void main(String[] args) {
        run();
     }
 }
