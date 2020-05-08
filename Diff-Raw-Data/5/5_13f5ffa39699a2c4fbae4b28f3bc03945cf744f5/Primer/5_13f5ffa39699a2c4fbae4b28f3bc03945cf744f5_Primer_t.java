 package model;
 
 import java.lang.Math;
 import java.util.ArrayList;
 import model.TestResult.PassState;
 
 /**
  *
  * @author 1002852m & 1002858t
  */
 public class Primer {
     
     private String code;
     
     public Primer(String c) {
         code = c;
     }
     public String getCode() {
         return code;
     }
     public void setCode(String c) {
        code = c;
     }
     
     
     public TestResult goodLength() {
     	/*
     	 * True if the primer is of an appropriate length, btwn 20 and 30 bases.
     	 */
         if (code.length() >= 20 && code.length() <= 30)
             return new TestResult(PassState.PASS, "The primer has a length of " + 
                     code.length() + " bases, which is within the optimal range "
                     + "of 20 to 30 bases.");
         else if (code.length() >= 17 && code.length() <= 33) 
             return new TestResult(PassState.CLOSEFAIL, "The primer has a length"
                     + " of " + code.length() + " bases, which is just outside the"
                     + " optimal range of 20 to 30 bases.");
         else return new TestResult(PassState.FAIL, ("The primer's length should be "
                 + "between 20 and 30 bases, current length is " + 
                 String.valueOf(code.length())));
         
     }
     
     public Integer getMeltingTemp() {
         
         int at = 0; int gc = 0;
         for (int i = 0; i < code.length(); i++) {
             char c = code.charAt(i);
             if (c == 'a' || c == 't')
                 at++;
             else if (c == 'g' || c == 'c')
                 gc++;
         }
         return ((2*at) + (4*gc));
     }
     
     public TestResult meltingTemp() {
     	/*
     	 * Returns true if the formula to calculate melting temp does not return
     	 * a value outside the range of 50-65.
     	 */
         int meltTemp = getMeltingTemp();
         
         if (meltTemp >= 50 && meltTemp <= 65)
             return new TestResult(PassState.PASS, "The primer's melting temperature of "
                     + Integer.toString(meltTemp) + "\u2013 is within the "
                     + "bounds of 50-65\u2013");
         else if (meltTemp >= 45 && meltTemp <= 69) {
             return new TestResult(PassState.CLOSEFAIL, "The primer's "
                     + "melting temperature of " + Integer.toString(meltTemp)
                     + "\u2013 is just outside the bounds of 50-65\u2013");
         }
         else
             return new TestResult(PassState.FAIL, ("Melting "
                     + "temperature should be between 50-65\u2013, current "
                     + "temperature: " + Integer.toString(meltTemp) + "\u2013"));
     }
     
     public boolean matches(int i, String x) {
             if (x.substring(i, Math.min(x.length(),
                     (i + code.length()))).equals(code)) return true;
             return false;
     } 
     
 
 
     public TestResult gcContent(){
 
         /* count g and c and store in 'gcCount'
          * if gcCount ratio is between 0.4 and 0.6 (inclusive):  passes test
          * else: fails test and returns actual % gc content
          */
 
         double gcCount = 0;
    
         for(int i = 0; i < code.length(); i++) {
             if(code.charAt(i) == 'g' || code.charAt(i) == 'c')
                 gcCount++;
         }
         
         double ratio = gcCount/(double) code.length();  
         if (ratio >= 0.4 && ratio <= 0.6) {
             return new TestResult(PassState.PASS, "GC content of this primer sits at "
                     + String.format("%.2f", ratio*100) + "%, this rests within the "
                     + "recommended bounds of 40-60%.");
         }
         else if (ratio >= 0.36 && ratio <= 0.64) {
             return new TestResult(PassState.CLOSEFAIL, "GC content of"
                     + " this primer sits at " + String.format("%.2f", ratio*100)
                     + "%, this lies just outside the recommended bounds of 40%"
                     + "-60%");
         }
         else {
             return new TestResult(PassState.FAIL, ("GC content in primer should sit "
                     + "between 40% and 60%. The current percentage is " 
                     + String.format("%.2f", ratio*100)+"%."));
         }
 
     }
 
     public TestResult repetition(){
 
         /* for each base in 'code'
          * if it is the same as previous base: increment reps
          * if reps passes 3: fails test and returns repeated base
          * if for is completed without a fail: passed test
          */
 
         char current = code.charAt(0);
         int reps = 1;
 
         for(int i = 1; i < code.length(); i++){
             if(current == code.charAt(i)){
                 reps++;
                 if(reps > 5){
                     return new TestResult(PassState.FAIL, 
                             ("Base " + String.valueOf(current) + 
                             " repeats "+ reps + " times in a row. Ideally, bases"
                             + " should repeat a maximum of 3 times in a row."));
                 }
                 if(reps > 3){
                     return new TestResult(PassState.CLOSEFAIL,
                             "Base " + String.valueOf(current) +
                             "repeats" + reps + " times in a row, just over the"
                             + "ideal maximum for repeating bases of 3 in a row.");
                 }
             }
             else {
                 current = code.charAt(i);
                 reps = 1;
             }
         }   
     
         return new TestResult(PassState.PASS, "The primer does not "
                 + "contain too many instances of any given base in a row.");
 
     }
 
     public TestResult lastLetter(){
         
         /* if last letter is not a g or c: fails test
          * always returns last letter, pass or fail
          */
 
         char last = code.charAt(code.length() - 1);
         boolean p = false;
 
         if (last == 'g' || last == 'c')
             return new TestResult(PassState.PASS, "Last base of the "
                     + "primer is a " + last + ".");
         else return new TestResult(PassState.FAIL, "Last base of the"
                 + " primer is not g or c.");
     }
 
     public TestResult pairAnneal(Primer p){
         
         /* returns true if highest number of consecutive complementary base
          * pairs is less than 4, false otherwise
          */
         
         int maxMatches = 0; //highest number of consecutive matches found so far
         
         String max, min;    //max is the longest primer, min is the shortest
         
         if (code.length() >= p.getCode().length()){
             max = code;
             min = p.getCode();
         }else{
             max = p.getCode();
             min = code;
         }
             
         // This segment compares the primers when the start of the longer primer
         // joins somewhere in the middle of the shorter primer with the end of
         // the longer primer and the start of the shorter primer unjoined.
         //
         // i.e.     agtcatcg
         //       acatca
         int minStart = Math.max(min.length() - 4, 0);
         String minCheck, maxCheck;
         int matches;
         while (minStart != 0){
             minCheck = min.substring(minStart);
             maxCheck = max.substring(0, minCheck.length());
             if ((matches = checkMatches(minCheck, maxCheck)) > maxMatches)
                 maxMatches = matches;
             minStart--;
         }
         
         // This segment compares the primers when the shorter primer joins the
         // longer primer somewhere in the middle so that both ends of the
         // shorter primer are joined to the longer primer.
         //
         // i.e.      agatcgattgcagt
         //              agctaac
         int maxEnd = min.length() - 1;
         int maxStart = 0;
         while (maxEnd < max.length()){
             maxCheck = max.substring(maxStart, maxEnd + 1);
             if ((matches = checkMatches(min, maxCheck)) > maxMatches)
                 maxMatches = matches;
             maxStart++;
             maxEnd++;
         }
         
         // This segment compares the primers when the start of the shorter
         // primer joins somewhere in the middle of the longer primer with the
         // start of the longer primer and the end of the shorter primer 
         // unjoined.
         //
         // i.e.      agtacgtaggtc
         //                  tccagtac
         int minEnd = min.length() - 2;
         while (minEnd >= 4){
             minCheck = min.substring(0, minEnd + 1);
             maxCheck = max.substring(maxStart);
             if ((matches = checkMatches(minCheck, maxCheck)) > maxMatches)
                 maxMatches = matches;
             maxStart++;
             minEnd--;
         }
         
         if (maxMatches >= 6)
             return (new TestResult(PassState.FAIL, "The primers' "
                    + "bases anneal to each other in " + maxMatches + 
                     " places."));
         else if (maxMatches >= 4)
             return (new TestResult(PassState.CLOSEFAIL, "Primers may"
                     + " not anneal to each other to a significant degree, but "
                     + "there are " + maxMatches + "instances where bases from "
                     + "each primer anneal to each other."));
         else
             return new TestResult(PassState.PASS, "Primers will not "
                     + "anneal to each other to a significant degree.");
         // change to return useful info about matches
     }
     
     public TestResult selfAnneal(){
     	/*
     	 * Returns true if at no point can the string be "folded over"
          * and have 4 or more bases on each side complement each other. 
     	 */
         int maxMatches = 0;
         String front, back;
         int difference;
     
         int split = 4;
         
         if (code.length() <= 8){
             return (new TestResult(PassState.PASS, "The primer will not self anneal."));
         } else {
             while (split <= code.length() - 4){
                 front = code.substring(0,split);
                 back = reverse(code.substring(split));
                 if (front.length() > back.length()){
                     difference = front.length() - back.length();
                     front = front.substring(difference);
                 } else if (back.length() > front.length()){
                     difference = back.length() - front.length();
                     back = back.substring(difference);
                 }
 
                 maxMatches = Math.max(maxMatches, checkMatches(front, back));
                 split++;
             }
         }
         if (maxMatches >= 6) {
             return (new TestResult(PassState.FAIL, "The primer bases anneal to "
                     + "each other in " + maxMatches + " places, well above the"
                    + " recommended limit of 4."));
         }
         else if(maxMatches >= 4) {
             return (new TestResult(PassState.CLOSEFAIL, "The primer bases anneal"
                     + " to each other in " + maxMatches + " places, just above "
                     + "the recommended limit of 4."));
         }
         else {
             return (new TestResult(PassState.PASS, "The primer will not self "
                     + "anneal."));
         }
 }
     
     public int checkMatches(String min, String max){
         
         /* Method to return the highest number of consecutive complementary
          * bases in two primer subsequences
          */
         
         int matches = 0;
         
         for(int i = 0; i < max.length(); i++){
             if (max.charAt(i) == Sequence.complement(min.charAt(i)))
                 matches++;
             else
                 matches = 0;    // chain of matches broken
         }
         return matches;
     }
     
     public static String reverse(String s) {
         if (s.length() <= 1) { 
             return s;
         }
         return reverse(s.substring(1, s.length())) + s.charAt(0);
     }
     
     public String toString() {
         return code;
     }
     
     public boolean equals(Primer p) {
         return (this.code.equals(p.getCode()));
     }
     public TestResult test() {
     	/*
     	 * Tests a primer against all the primer rule tests,
     	 * passes if all true and returns relevant comments if
     	 * not.
     	 */
         TestResult t = new TestResult(PassState.PASS, "");
         t.add(meltingTemp());
         t.add(gcContent());
         t.add(repetition());                                       
         t.add(goodLength());                               
         t.add(selfAnneal());
         return t;
     }
 }
