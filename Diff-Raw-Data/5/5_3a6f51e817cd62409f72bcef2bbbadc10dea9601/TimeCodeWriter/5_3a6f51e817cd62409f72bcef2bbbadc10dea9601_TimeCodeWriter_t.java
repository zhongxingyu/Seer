 package de.isolation;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 /**
  * easy way to generate time codes for Auphonic
  */
 public class TimeCodeWriter {
     static long start;
     static BufferedWriter out;
     public static void main(String args[]) {
         BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
         String input;
 
         try {
             System.out.println("Welcome to TimeCodeWriter, hit <enter> to start tracking.");
             // first, wait for time tracking to start
             while ((input = buffer.readLine()) != null)    {
                 String fileName = "TimeCodes_" +
                         new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".txt";
                 System.out.println("Time tracking starts now, writing to "+fileName);
                 System.out.println("Hit <enter> to generate an entry");
                 System.out.println("Type a line with only 'e' in it and hit <enter> to stop tracking.");
                 FileWriter fileWriter = new FileWriter(fileName);
                 out = new BufferedWriter(fileWriter);
                 log(0, "");
                 start = System.currentTimeMillis();
                 // now log time code for each enter, until user says "end"
                 while ((input = buffer.readLine()) != null)    {
                     log(System.currentTimeMillis() - start, input);
                     if (input.equals("e")) {
                         System.out.println("\n\nGreat, you're done! Now add your notes to "+fileName);
                         System.out.println("Thank you for using TimeCodeWriter! Listen to einschlafen-podcast.de to relax after your hard work!");
                         out.close();
                         System.exit(0);
                     }
                 }
             }
         } catch (IOException e) {
             // uh oh...
             e.printStackTrace();
         }
     }
 
     private static void log(long current, String input) throws IOException {
         String f2 = String.format("%02d:%02d:%02d.%03d",
                 TimeUnit.MILLISECONDS.toHours(current),
                TimeUnit.MILLISECONDS.toMinutes(current) % 60,
                TimeUnit.MILLISECONDS.toSeconds(current) % 60,
                 current % 1000
         );
         System.out.print(f2 + " ");
         out.write(f2+" "+input+"\n");
     }
 }
