 /**
 Name: Joel Obando
 Class: CSc 2310: Introduction to programming
 Filename: TestClock.java
 Date written: April, 19, 2010
 
 Description:
 This class tests the behavior of the Clock class. This is a static class and
 does not need to be instantiated. This class takes and verifies all the
 user input before passing it to the clock class. The class SimpleIO.java
 from JAVA PROGRAMMING: FROM THE BEGINNING, by K. N. King has been used
 to acquire user input.
  */
 package project2;
 
 public class TestClock {
 
    private static Clock testingClock; //class variable of clock to be tested.
 
     /**
      *Start the testing of the clock class. It will not until the users
      * decides to quit the program.
      * @param tClock The clock to be tested.
      */
     public static void startTest(Clock tClock) {
         testingClock = tClock;
         String welcome =
                 "Welcome to the clock testing program. \n\n"
                 + "Commands:\n"
                 + "---------\n"
                 + "a = advance (increase time by one second)\n"
                 + "c = calibrate (set to current time)\n"
                 + "q = quit\n"
                 + "r = reset (set hours, minutes, and second to zero)\n"
                 + "s = set (allow user to set time)\n"
                 + "t = toggle (switch from 24-hour clock to 12-hour clock, "
                 + "or vice-versa)\n"
                 + "\n"
                 + testingClock
                 + "\n"
                 + "";
         System.out.println(welcome);
         while (true) {
             SimpleIO.prompt("Enter command (a, c, q, r, s, or t):");
             validateInput(SimpleIO.readLine());
         }
     }
 
     /**
      * Increases time by one second
      */
     private static void advance() {
         testingClock.advance();
     }
 
     /**
      * Sets clock to current time
      */
     private static void calibrate() {
         testingClock.calibrate();
     }
 
     /**
      * Resets time to 00:00:00
      */
     private static void reset() {
         testingClock.reset();
     }
 
     /**
      * Tests the hours, minutes and seconds to make sure they are within a
      * regular clock constraints.
      * @param number Number to be verified.
      * @param place What is being verified.
      * 'h' = hours
      * 'm' = minutes
      * 's' = seconds
      * @return Boolean results depending if the input is within constrains.
      */
     private static boolean validateNumber(int number, char place) {
         switch (place) {
             case 'h':
                 return number < 24 ? true : false;
             case 'm':
                 return number < 60 ? true : false;
             case 's':
                 return number < 60 ? true : false;
             default:
                 return false;
         }
     }
 
     /**
      * Calls Set method of the clock class. All user input is verified.
      *Asks the user for the amount of hours, minutes and seconds to be stored.
      *It makes sure that the user enters a integer in each case. If it is not,
      *the loop will continue and ask again for input until the user
      *enters a valid number.
      */
     private static void set() {
         int hours = 0;
         int minutes = 0;
         int seconds = 0;
         //Asks the user for the amount of hours to be stored.
         while (true) {
             SimpleIO.prompt("Enter hours:");
             try {
                 hours = Integer.parseInt(SimpleIO.readLine());
                 if (validateNumber(hours, 'h')) {
                     break;
                 }
                System.out.println("Input must be between 0 and 23; please try again.");
                 continue;
             } catch (Exception e) {
                System.out.println("Input was not an integer; please try again.");
                 continue;
             }
         }
         //Asks the user for the amount of minutes to be stored.
         while (true) {
             SimpleIO.prompt("Enter minutes:");
             try {
                 minutes = Integer.parseInt(SimpleIO.readLine());
                 if (validateNumber(minutes, 'm')) {
                     break;
                 }
                System.out.println("Input must be between 0 and 59; please try again.");
                 continue;
             } catch (Exception e) {
                System.out.println("Input was not an integer; please try again.");
                 continue;
             }
         }
         //Asks the user for the amount of seconds to be stored.
         while (true) {
             SimpleIO.prompt("Enter seconds:");
             try {
                 seconds = Integer.parseInt(SimpleIO.readLine());
                 if (validateNumber(seconds, 's')) {
                     break;
                 }
                System.out.println("Input must be between 0 and 59; please try again.");
                 continue;
             } catch (Exception e) {
                System.out.println("Input was not an integer; please try again.");
                 continue;
             }
         }
         //Passes user input to the clock class
         testingClock.set(hours, minutes, seconds);
     }
 
     private static void toggleClockMode() {
         // Switches from 24-hour clock to 12-hour clock or vice-versa
         testingClock.toggleClockMode();
     }
 
     private static void quit() {
         System.exit(0);
     }
 
     /**
     * Verifies all user input. It makes sure that all input is either a, c, q,
     * r, s, or t and calls the corresponding methods. Input can be in lower or
      * upper case.
      * @param input command to be executed.
      */
     private static void validateInput(String input) {
         char key;
         try {
             input = input.length() > 1 ? "0" : input;
             key = input.charAt(0);
         } catch (Exception e) {
             key = '0';
         }
 
         switch (key) {
             case 'a':
                 advance();
                 break;
             case 'c':
                 calibrate();
                 break;
             case 'q':
                 quit();
                 break;
             case 'r':
                 reset();
                 break;
             case 's':
                 set();
                 break;
             case 't':
                 toggleClockMode();
                 break;
             case 'A':
                 advance();
                 break;
             case 'C':
                 calibrate();
                 break;
             case 'Q':
                 quit();
                 break;
             case 'R':
                 reset();
                 break;
             case 'S':
                 set();
                 break;
             case 'T':
                 toggleClockMode();
                 break;
             default:
                 System.out.println("Illegal command; please try again.");
                 break;
         }
     }
 }
