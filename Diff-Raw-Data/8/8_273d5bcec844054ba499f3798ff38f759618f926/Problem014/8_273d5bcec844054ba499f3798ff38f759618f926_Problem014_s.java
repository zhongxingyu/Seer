 package net.euler;
 
 /**
  * Longest Collatz sequence
  *
  * The following iterative sequence is defined for the set of positive integers:
  *
  *   n → n/2 (n is even)
  *   n → 3n + 1 (n is odd)
  *
  * Using the rule above and starting with 13, we generate the following sequence:
  * 13 → 40 → 20 → 10 → 5 → 16 → 8 → 4 → 2 → 1
  *
  * It can be seen that this sequence (starting at 13 and finishing at 1) contains 10 terms.
  * Although it has not been proved yet (Collatz Problem), it is thought that all starting numbers finish at 1.
  * Which starting number, under one million, produces the longest chain?
  *
  * User: Alexandros Bantis
  * Date: 3/6/13
  * Time: 11:14 PM
  */
 public class Problem014 {
 
   public int getMaxCollatz(int ceiling) {
     int maxSeqLength = 0;
     int maxN = 0;
    int currentCollatzSeqLength = 0;
    for (int i = 1; i < ceiling; i++) {
       currentCollatzSeqLength = getCollatzSequenceLength(i);
       if (currentCollatzSeqLength > maxSeqLength) {
         maxSeqLength = currentCollatzSeqLength;
         maxN = i;
       }
     }
     return maxN;
   }
 
  public int getCollatzSequenceLength(long n) {
     int terms = 1;
     while (n != 1) {
       terms++;
       if (n % 2 == 0)
         n /= 2;
       else
         n = n * 3 + 1;
     }
     return terms;
   }
 
 }
