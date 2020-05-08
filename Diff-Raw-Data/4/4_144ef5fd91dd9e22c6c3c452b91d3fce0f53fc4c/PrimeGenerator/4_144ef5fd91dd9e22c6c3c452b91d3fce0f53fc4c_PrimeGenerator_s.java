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
  * PrimeGenerator is a very unoptimized prime number generator written in Java,
  * based on the idea that a number is a prime number if it's indivisible by
  * any other prime numbers that came before it.
  *
  * PrimeGenerator can generate prime numbers in the range of 2 to Long.MAX_VALUE
  * (2^63 - 1). However, generation of prime numbers become slower as the size of
  * the numbers increases.
  *
  * @author Michael Cheah
  * @version 1.1
  */
 public class PrimeGenerator {
 
     private ArrayList<Long> primes;
     private long maxRange;
 
     /**
      * Construct a PrimeGenerator object with a maxRange of 0
      */
     public PrimeGenerator() {
         primes = new ArrayList<Long>();
         primes.add(2l);
     }
     
     /**
      * Construct a PrimeGenerator object with a maximum range indicated by the
      * parameter.
      *
      * @param maxRange The maximum range to be set
      */
     public PrimeGenerator(long maxRange) {
         this();
         this.setMaxRange(maxRange);
     }
 
     /**
      * Returns the maximum range of this PrimeGenerator
      *
      * @return The maximum range set
      */
     public long getMaxRange() {
         return maxRange;
     }
     
     /**
      * Set the maximum range of this PrimeGenerator
      *
      * @param range The maximum range to be set for this PrimeGenerator
      */
     public void setMaxRange(long range) {
         maxRange = range;
     }
     
     /**
      * Returns the prime numbers generated with this PrimeGenerator
      *
      * @return an array list of primes
      */
     public ArrayList<Long> getPrimes() {
         return primes;
     }
     
     /**
      * Returns a String representation of all the generated prime number by
      * this PrimeGenerator
      *
      * The result is formatted as a maximum of 10 prime numbers in a row,
      * separated by tabs
      *
      * @return a String representation of the result
      */    
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
         //Remove trailing newline from string if exist
         if( lineBreak%10 == 0 ) {
             buffer.deleteCharAt(buffer.length() -1);
         }
         return buffer.toString();
     }
     
     /**
      * Find all the prime numbers in the specified range
      *
      * @param realtime determine whether the prime numbers should be printed
      *                 to the standard output in realtime
      */
     public void findAllPrimeInRange(boolean realtime) {
         long nextPrime = -1;
         while( (nextPrime = getNextPrime()) != -1) {
             primes.add(nextPrime);
             if(realtime)
                 System.out.println(nextPrime);
         }
         
         return;
     }
     
     /**
      * Returns the next prime number in the specified range.
      *
      * @return the next prime number in the specified range. If none found
      *         or the maximum range has been reached, returns -1
      */
     public long getNextPrime() {
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
     
     /**
      * Prompt the users to input the maximum range. The minimum valid range
      * is 2, and the maximum is Long.MAX_VALUE
      *
      * @return a valid maximum range specified by the user
      */
     private long getUserSpecifiedRange()
     {
         long maxRange = -1;
         System.out.println(  "This program will compute the prime number until "
                            + "the upper limit you specify. \n"
                            + "Beware that large primes can take a VERY long "
                            + "time to compute. \n"
                            + "================================================"
                            + "================================");
         while(maxRange < 2 || maxRange > Long.MAX_VALUE) {
             try {
                 BufferedReader input = new BufferedReader(
                                        new InputStreamReader(System.in));
                 System.out.print(  "Please enter the upper limit of prime "
                                  + "number you would like to compute: ");
                 String userinput = input.readLine();
                 maxRange = Long.parseLong(userinput);
             }
             catch(NumberFormatException NFE) {
                 System.out.println(  "Invalid number entered. Valid range is "
                                    + "between 2 and " + Long.MAX_VALUE);
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
         
         /*
         * Check whether range is large. If so, ask the users whether realtime
         * flag should be set.
         */
         if(myGenerator.maxRange >= 500000) {
             System.out.println(  "The range entered is quite large. It can take"
                                + " quite some time to compute all the result.");
             try{
                 boolean validInput = false;
                 while( !validInput ) {
                     System.out.print(  "Print prime numbers as soon as "
                                      + "they are computed? (y/n): ");
                     BufferedReader input = new BufferedReader(
                                            new InputStreamReader(System.in));
                     String userinput = input.readLine();
                     if( userinput.equalsIgnoreCase("y") ) {
                         realtime = true;
                         validInput = true;
                         System.out.println(  "The prime numbers within the "
                                            + "range are:\n2");
                     }
                     else if(userinput.equalsIgnoreCase("n"))
                         validInput = true;
                 }
             }
             catch(java.io.IOException IOE) {
                 System.err.println("Input/Output Exception Occured!");
             }
         }
         
         /* Find all primes and print results */
         myGenerator.findAllPrimeInRange(realtime);
         if(!realtime) {
             System.out.println("The prime numbers within the range are:");
             System.out.println(myGenerator);
         }
     }
 
 }
