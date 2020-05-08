 /*
 Prime number generator written in Java
 Copyright (C) 2012 Michael Cheah
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 
 import java.io.*;
 import java.util.ArrayList;
 /**
  * A very unoptimized Prime number generator written in Java, based on my original idea
  * from when I just started programming.
  *
  * @author Michael Cheah
  * @version 1.1
  */
 public class PrimeGenerator {
 
     /**
      * @param args the command line arguments
      */
     private ArrayList<Long> primes;
     private long maxRange;
 
     /**
      * Default constructor
      */
     public PrimeGenerator() {
         primes = new ArrayList<Long>();
         primes.add(2l);
     }
     
     public PrimeGenerator(long maxRange) {
         this();
         this.setMaxRange(maxRange);
     }
 
     
     public long getMaxRange() {
         return maxRange;
     }
     
     public void setMaxRange(long range) {
         maxRange = range;
     }
     
     public ArrayList<Long> getPrimes() {
         return primes;
     }
     
     public String toString() {
         StringBuilder buffer = new StringBuilder();
         int lineBreak = 0;
         for(Long n : primes) {
             buffer.append(n);
             buffer.append('\t');
             lineBreak++;
             if( lineBreak%10 == 0 ) {
                 buffer.append('\n');
                 lineBreak = 0;
             }
         }
         return buffer.toString();
     }
     
     
     private void findAllPrimeInRange(boolean realtime) {
         long nextPrime = -1;
         while( (nextPrime = getNextPrime()) != -1) {
             primes.add(nextPrime);
             if(realtime)
                 System.out.println(nextPrime);
         }
         
         return;
     }
     
     //This is the method to find our next prime number
     private long getNextPrime() {
         int currIndex = primes.size() - 1;
         long currPrime = primes.get(currIndex);
         boolean currNumIsPrime = true;
         for(long i = currPrime + 1; i <= maxRange; i++) {
             currNumIsPrime = true;
             for(int j = 0; j < currIndex; j++) {
                 if(i % primes.get(j) == 0) {
                     currNumIsPrime = false;
                     break;
                 }
             }
             if(currNumIsPrime) {
                 return i;
             }
         }
         
         return -1;
     }
     
 
     private long getUserSpecifiedRange()
     {
         long maxRange = -1;
         System.out.println(  "This program will compute the prime number until the upper limit you specify. \n"
                            + "Beware that large primes can take a VERY long time to compute. \n"
                            + "================================================================================");
         while(maxRange < 2 || maxRange > Long.MAX_VALUE) {
             try {
                 BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                 System.out.print("Please enter the upper limit of prime number you would like to compute: ");
                 String userinput = input.readLine();
                 maxRange = Long.parseLong(userinput);
             }
             catch(NumberFormatException NFE) {
                 System.out.println("Invalid number entered. Valid range is between 2 and " + Long.MAX_VALUE);
             }
             catch(java.io.IOException IOE){
                 System.err.println("Input/Output Exception Occured!");
             }
         }
         return maxRange;
     }
     
     
     public static void main(String[] args) {
         
         PrimeGenerator myGenerator = new PrimeGenerator();
         boolean realtime = false;
         myGenerator.maxRange = myGenerator.getUserSpecifiedRange();
         System.out.println("Starting......");
         if(myGenerator.maxRange >= 500000) {
             System.out.println("The range entered is quite large. It can take quite some time to compute all the result.");
             try{
                 boolean validInput = false;
                 while( !validInput ) {
                 System.out.print("Print prime numbers as soon as they are computed? (y/n): ");
                 BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                 String userinput = input.readLine();
                 if( userinput.equalsIgnoreCase("y") ) {
                     realtime = true;
                     validInput = true;
                     System.out.println("The prime numbers within the range are:\n2");
                 }
                 else if(userinput.equalsIgnoreCase("n"))
                     validInput = true;
                 }
             }
             catch(java.io.IOException IOE) {
                 System.err.println("Input/Output Exception Occured!");
             }
         }
         myGenerator.findAllPrimeInRange(realtime);
         if(!realtime) {
             System.out.println("The prime numbers within the range are:");
             System.out.println(myGenerator);
         }
     }
 
 }
