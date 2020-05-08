 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tablingassigner;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import java.io.*;
 import java.util.*;
 
 /**
  *
  * @author hiralmehta
  */
 public class TablingAssigner {
     
     public static final int PREF_LEVEL = 0;
     public static final int CM_REQ_TABLING = 1;
    public static final int CHAIR_REQ_TABLING = 1;
    public static final int EXEC_REQ_TABLING = 1;
     
 
     /** The number of times an assignment has to force someone out of their 
      * assigned slot. Once this gets too high, this will terminate without a 
      * solution.
      */
     private static int _distressCounter = 0;
     
     public static final int MAX_DISTRESS = 1000;
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         run("");
     }
 
     public static void run(String dir) {
         HashSet<Student> students = ScheduleReader.processStudents(dir + "/members.csv");
         
         HashMap<Integer, HashSet<TimeConflict>> tcMap = ScheduleReader.processTimeConflicts(dir + "/schedules.csv");
         
         for (Student student : students) {
             student.getTimeConflicts(tcMap);
         }
         
         
         Student curStudent = findMCV(students);
         
         while (curStudent != null) {
             TablingSlot ts = findLCV(curStudent);
             if (ts != null) {
                 curStudent.assignTablingSlot(ts);
             } else { // no available slots
 //                System.out.println(curStudent.SSID());
 //                System.out.println(curStudent.availableSlots(PREF_LEVEL).size());
 //                for(TimeConflict tc : curStudent.tcSet()) {
 //                    System.out.println(tc.daysOfWeek() + " " + tc.startTime() + "-" + tc.endTime());
 //                }
                 if (curStudent.availableSlots(PREF_LEVEL).isEmpty()) {
                     // skip student
                     System.out.println(curStudent.name() + " cannot be assigned any tabling slot.");
                     System.out.println("Continuing without assigning " + curStudent.name() + "...");
                     students.remove(curStudent);
                     
                 } else if (curStudent.availableSlots(PREF_LEVEL).size() < curStudent.requiredTabling()) {
                         throw new IllegalArgumentException(curStudent.name() + 
                                 " is only available for " + curStudent.availableSlots(PREF_LEVEL).size() + 
                                 " slots, but he/she is required to table for " + curStudent.requiredTabling() + 
                                 " slots.");
                 } else {
    
 //                    System.out.println(curStudent.name());
 //                    for (TablingSlot s : curStudent.availableSlots(PREF_LEVEL)) {
 //                        System.out.println(s.dayOfWeek() + " " + s.startTime());
 //                    }
                     
                     TablingSlot fullSlot = randomElement(curStudent.availableSlots(PREF_LEVEL));
                     if (fullSlot.containsStudentWeakly(curStudent) != null) {
                         curStudent.unassignTablingSlot(curStudent.assignedSlotWithHalfHour(fullSlot.containsStudentWeakly(curStudent)));
                     }
                     fullSlot.makeSpace();
                     curStudent.assignTablingSlot(fullSlot);
                     _distressCounter++;
                     if (_distressCounter > MAX_DISTRESS) {
                         throw new IllegalArgumentException("A solution doens't seem to be possible.");
                     }
                 }
             }
             
             
             curStudent = findMCV(students);
             
             
         }
         
         try {
             String outputFileName = dir + "/initial_schedule.csv";
             BufferedWriter tsWriter = new BufferedWriter(new FileWriter(new File(outputFileName)));
 
             System.out.println("Writing initial schedule to file");
             for (Student student : students) {
                 for (TablingSlot ts : student.assignedTablingSlots()) {
                     tsWriter.write(student.SSID() + "," + ts.dayOfWeek() + "," + ts.startTime() + "," + ts.endTime());
                     tsWriter.newLine();
                 }
             }
             tsWriter.close();
         }
 
         catch (IOException e) {
             System.err.println(e);
             System.exit(1);
         }
     }
     
     /** Find the student with the fewest available options.
      * Breaks ties randomly.
      * @param students - The set of students to choose from.
      * @return 
      */
     public static Student findMCV(Set<Student> students) {
 
         ArrayList<Student> mcStudents = new ArrayList<Student>();
         int leastSlots = Integer.MAX_VALUE;
         
         for (Student student : students) {
             if (!student.isFullyAssigned()) {
                 int numOfSlots = student.currentAvailableSlots(PREF_LEVEL).size();
                 if (numOfSlots < leastSlots) {
                     mcStudents.clear();
                     mcStudents.add(student);
                     leastSlots = numOfSlots;
                 } else if (numOfSlots == leastSlots) {
                     mcStudents.add(student);
                 }
             }
         }
         return randomElement(mcStudents);
     }
     
     
     /** Find Least Constrained Value. I.e. find the tabling slot that you can 
      * assign STUDENT that has the highest remaining capacity.
      * Note, this is not the same thing as the fewest students as different
      * slots may have different capacities.
      * Breaks ties randomly.
      * @param student
      * @return 
      */
     public static TablingSlot findLCV(Student student) {
         int maxCapacity = Integer.MIN_VALUE;
         List<TablingSlot> lcSlots = new ArrayList<TablingSlot>();
         
         for (TablingSlot ts : student.currentAvailableSlots(PREF_LEVEL)) {
             if (ts.remainingCapacity() > maxCapacity) {
                 maxCapacity = ts.remainingCapacity();
                 lcSlots.clear();
                 lcSlots.add(ts);
             } else if (ts.remainingCapacity() == maxCapacity) {
                 lcSlots.add(ts);
             }
         }
         
         return randomElement(lcSlots);
         
     }
     
     
     public static <E> E randomElement(Collection<E> clln) {
         if (clln == null) {
             throw new NullPointerException("clln in null");
         }
         
         if (clln.isEmpty()) {
             return null;
         }        
         
         int eltNum = (int) Math.ceil(Math.random()*clln.size());
         
         if (eltNum == 0) { // This is basically impossible, but putting this in anyway.
             return randomElement(clln);
         }
         
         
         Iterator<E> iter = clln.iterator();
         
         for (int i = 1; i < eltNum; i++) {
             iter.next();
         }
         
         return iter.next();
     }
 }
