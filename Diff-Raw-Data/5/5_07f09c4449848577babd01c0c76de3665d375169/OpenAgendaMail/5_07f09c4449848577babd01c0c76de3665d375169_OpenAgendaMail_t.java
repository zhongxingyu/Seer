 package openagendamail;
 
 import java.util.Properties;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import openagendamail.data.EmailAgendaItemProvider;
 import openagendamail.file.LogFile;
 import openagendamail.util.BuildAgendaRunnable;
 import openagendamail.util.EmailSenderRunnable;
 import openagendamail.util.FirstAndThirdRunnable;
 import openagendamail.util.OamTools;
 
 /**
  * The main class for AgendaMail.
  *
  * @author adam
  * @date Jan 1, 2013
  * Last Updated: May 9th, 2013
  */
 public class OpenAgendaMail {
 
     /** A version string. */
     public static final String VERSION = "v1.8";
 
     /** The date of the last update to the system. */
     private static final String LAST_UPDATED = "May 9th, 2013";
 
 
     /**
      * The Main method.
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 
         // print out software version info
         StringBuilder bldr = new StringBuilder(System.lineSeparator());
         bldr.append("OpenAgendaMail - A program by Adam Anderson");
         bldr.append(System.lineSeparator());
         bldr.append("Version:  " + VERSION);
         bldr.append(System.lineSeparator());
         bldr.append("Last Updated:  " + LAST_UPDATED);
         System.out.println(bldr.toString());
         LogFile.getLogFile().log(bldr.toString());
 
         // VALIDATE ARGUMENTS
         if (args.length != 1){
             printUsage();
             System.exit(0);
         }
         if (!(args[0].toLowerCase().equals("1stand3rd") || args[0].toLowerCase().equals("week-based") ||
                 args[0].toLowerCase().equals("-h") || args[0].toLowerCase().equals("--help") | args[0].toLowerCase().equals("one-shot"))) {
             printUsage();
             LogFile.getLogFile().log("Invalid argument provided:  " + args[0]);
             System.exit(0);
         }
 
         // BEGIN EXECUTION
         switch (args[0].toLowerCase()) {
             case "1stand3rd":
                 // This block is called when an agenda is required for the 1st and 3rd Sundays of the month.
                 LogFile.getLogFile().log("Application Started in 1stAndThird mode.");
                 executeFirstAndThirdMode();
                 break;
 
             case "week-based":
                 // This block is called when an agenda is required on a weekly recurring basis on the same day (ie
                 // every week or every two weeks etc.)
                 LogFile.getLogFile().log("Application Started in week-based mode.");
                 executeWeekBasedMode();
                 break;
 
             case "one-shot":
                 LogFile.getLogFile().log("Application started in one-shot mode.");
                 executeOneShot();
                 break;
 
             default:
                 System.exit(1);
                 break;
         }
     }
 
     /** Starts the application in week-based mode. */
     private static void executeWeekBasedMode(){
         Properties props = OamTools.PROPS;
         long frequencyInSeconds = Integer.valueOf(props.getProperty("weeks.between.meetings", "1")) * OamTools.ONE_WEEK_IN_SECONDS;
         long secondsUntilAgendaIsDue = OamTools.getSecondsUntilSpecifiedDay(OamTools.getDayOfWeek(props.getProperty("send.day", "tue")));
         LogFile.getLogFile().log("Time until send:  " + OamTools.getCountdownString(secondsUntilAgendaIsDue));
 
         // Schedule the agenda building.
         BuildAgendaRunnable builder = new BuildAgendaRunnable(new EmailAgendaItemProvider(true));
         ScheduledExecutorService buildExecutor = Executors.newSingleThreadScheduledExecutor();
         if (props.getProperty("debug", "false").toLowerCase().equals("true")){
             buildExecutor.scheduleWithFixedDelay(builder, 0, frequencyInSeconds, TimeUnit.SECONDS);
         } else {
             buildExecutor.scheduleWithFixedDelay(builder, secondsUntilAgendaIsDue, frequencyInSeconds, TimeUnit.SECONDS);
         }
 
         // Schedule the agenda sending.
         EmailSenderRunnable sender = OamTools.buildAgendaEmailSender(null);
         ScheduledExecutorService sendExecutor = Executors.newSingleThreadScheduledExecutor();
         if (props.getProperty("debug", "false").toLowerCase().equals("true")){
             sendExecutor.scheduleWithFixedDelay(sender, 60, frequencyInSeconds, TimeUnit.SECONDS);
         } else {
             sendExecutor.scheduleWithFixedDelay(sender, secondsUntilAgendaIsDue + OamTools.SECONDS_IN_FOUR_HOURS, frequencyInSeconds, TimeUnit.SECONDS);
         }
 
         // if enabled, schedule the reminder email
         if ((props.getProperty("reminders.on", "false")).toLowerCase().equals("true")){
             long secondsUntilReminder = OamTools.getSecondsUntilSpecifiedDay(OamTools.getDayOfWeek(props.getProperty("reminder.day", "mon")));
             EmailSenderRunnable reminder = OamTools.buildReminderSender();
             ScheduledExecutorService reminderExecutor = Executors.newSingleThreadScheduledExecutor();
 
             if (props.getProperty("debug", "false").equals("true")){
                 System.out.println("Debug scheduling reminder...");
                 reminderExecutor.scheduleWithFixedDelay(reminder, 30, frequencyInSeconds, TimeUnit.SECONDS);
             } else {
                 reminderExecutor.scheduleWithFixedDelay(reminder, secondsUntilReminder, frequencyInSeconds, TimeUnit.SECONDS);
             }
         }
     }
 
     /** 
      * Runs the application a single time to generate a single agenda right now.  Running the app in this way schedules
      * no repetition.
      */
     private static void executeOneShot() {
         BuildAgendaRunnable builder;
         if (OamTools.PROPS.getProperty("debug", "false").toLowerCase().equals("true")){
            // Create agenda, but don't delete emails.
             builder = new BuildAgendaRunnable(new EmailAgendaItemProvider(false));
         } else {
            // Create agenda and delete emails.
             builder = new BuildAgendaRunnable(new EmailAgendaItemProvider(true));
         }
         ScheduledExecutorService buildExecutor = Executors.newSingleThreadScheduledExecutor();
         buildExecutor.schedule(builder, 0, TimeUnit.MILLISECONDS);
 
         // Send the agenda after a 60 second delay.  This should take advantage of J7's new File System listening
         // Capabilities.
         EmailSenderRunnable sender = OamTools.buildAgendaEmailSender(null);
         ScheduledExecutorService sendExecutor = Executors.newSingleThreadScheduledExecutor();
         sendExecutor.schedule(sender, 90, TimeUnit.SECONDS);
     }
 
     
     /** Starts the scheduling for meetings that are on the 1st and 3rd of a given day of the week within a month. */
     private static void executeFirstAndThirdMode() {
         // Get time until the day that we need to send the agenda.
         int dayofweek = OamTools.getDayOfWeek(OamTools.PROPS.getProperty("send.day", "tue"));
         long secondsUntilAgendaIsDue = OamTools.getSecondsUntilSpecifiedDay(dayofweek);
         LogFile.getLogFile().log("Time until send:  " + OamTools.getCountdownString(secondsUntilAgendaIsDue));
 
         // Schedule the day check.
         FirstAndThirdRunnable firstAndThird = new FirstAndThirdRunnable();
         ScheduledExecutorService checkerExecutor = Executors.newSingleThreadScheduledExecutor();
         if (OamTools.PROPS.getProperty("debug", "false").toLowerCase().equals("true")){
             checkerExecutor.scheduleWithFixedDelay(firstAndThird, 5, secondsUntilAgendaIsDue, TimeUnit.SECONDS);
         } else {
             // Schedules the first and third runnable to be run once a week starting on the next 'send day' at midnight.
             checkerExecutor.scheduleWithFixedDelay(firstAndThird, secondsUntilAgendaIsDue, OamTools.ONE_WEEK_IN_SECONDS, TimeUnit.SECONDS);
         }
     }
 
     
     /** Prints out the proper usage of the application to the command prompt. */
     private static void printUsage(){
         System.out.println("\nUsage:");
 
         // week-based mode
         System.out.println("   AgendaMail requires a flag at the command line.  To run in");
         System.out.println("   week-based mode at the command prompt type:");
         System.out.println("      'java -jar OpenAgendaMail.jar week-based' but without quotes.\n\n");
 
         // first and thrid
         System.out.println("   To run in 1st and 3rd week mode, at the command prompt type:");
         System.out.println("      'java -jar OpenAgendaMail.jar 1stand3rd' but without quotes.\n\n");
         
         // one shot
         System.out.println("   To run run the program once with no scheduled repetition, at the command prompt type:");
         System.out.println("      'java -jar OpenAgendaMail.jar one-shot' but without quotes.\n\n");
 
         System.out.println("   To show this help type:");
         System.out.println("      'java -jar OpenAgendaMail.jar -h' but without quotes.\n");
         System.out.println("   or type:");
         System.out.println("      'java -jar OpenAgendaMail.jar --help' but without quotes.\n\n");
     }
 }
