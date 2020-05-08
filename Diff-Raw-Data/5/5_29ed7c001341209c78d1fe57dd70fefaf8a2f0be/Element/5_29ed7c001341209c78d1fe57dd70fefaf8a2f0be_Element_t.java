 import java.util.*;
 
 /**
 * This class stores an element of a Coxeter group of rank "rank" as a
 * signed permutation, oneLine.  The methods contained in this class
 * can preform elementary operations on the element.
 * @author Tyson Gern
 * @version 0.1
 */
 class Element {
     private int[] oneLine; // The signed permutation of the element
     private int rank; // The rank of the Coxeter group containing the
                       // element
  
     /**
      * This constructs an element from a signed permutation.
      * @param input The signed permutation
      */
     public Element(int[] input) {
         rank = input.length;
         oneLine = new int[rank];
         int sign = 1;
 
         boolean[] number = new boolean[rank];
         for (int i = 0; i < rank; i++) number[i] = false;
         
         for(int i = 0; i < rank; i++) {
             if (Math.abs(input[i]) > rank || input[i] == 0 || number[Math.abs(input[i]) - 1]) {
                 throw new IllegalArgumentException("Invalid permutation");
             }
             oneLine[i] = input[i];
             number[Math.abs(input[i]) - 1] = true;
             if (oneLine[i] < 0) {
                 sign *= -1;
             }
         }
         if (sign == -1) {
             throw new IllegalArgumentException("Invalid type");
         }
     }
     
     /**
      * This constructs an empty element of a particular rank.
      * @param rank The rank of the element
      */
     private Element(int rank) {
         this.rank = rank;
         oneLine = new int[rank];
     }
 
     /**
      * This method gets the rank of the element
      * @return The rank
      */
     public int getRank() {
         return rank;
     }
 
     /**
      * This method decides if two elements of a Coxeter group are
      * equal.
      * @param other The other element
      * @return true if the two elements are equal
      */
     public boolean equals(Element other) {
         if (rank != other.rank) return false;
 
         for (int i = 0; i < rank; i++) {
             if (oneLine[i] != other.oneLine[i]) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * This method gets the sign of the signed of the number that
      * origin maps to.
     * @param origin The origin number
      * @return The sign of the element applied to origin
      */
     public int getSign(int origin) {
        return Math.abs(oneLine[origin - 1]) / oneLine[origin - 1];
     }
 
     /**
      * This method gets the number that origin maps to.
      * @param origin The origen number
      * @return The element applied to origin
      */
     public int mapsTo(int origin) {
         if ((origin >= 1) & (origin <= rank))
             return oneLine[origin - 1];
         return 0;
     }
 
     /**
      * This method gets the number that maps to target.
      * @param target The target number
      * @return The number that element maps to target
      */
     public int mapsFrom(int target) {
         for(int i = 0; i < rank; i++) {
             if(oneLine[i] == target)
                 return i + 1;
         }
         return 0;
     }
 
     /**
      * This method gets the inverse
      * @return The inverse of element
      */
     public Element findInverse() {
         Element inverse = new Element(this.rank);
         for(int i = 0; i < rank; i++) {
             inverse.oneLine[Math.abs(this.oneLine[i]) - 1] = this.getSign(i + 1) * (i + 1);
         }
         return inverse;
     }
 
     /**
      * This method inverts the element
      * @return Nothing
      */
     public void invert() {
         this.oneLine = findInverse().oneLine;
     }
 
     /**
      * This method prints the one line signed permutation of the
      * element.
      * @return Nothing
      */
     public void printPerm() {
         System.out.print("[" + oneLine[0]);
         for (int i = 1; i < rank; i++) {
             System.out.print(", " + oneLine[i]);
         }
         System.out.print("]\n");
     }
 
     /**
      * This method multiplies the element on the right by a generator,
      * s.
      * @param s The generator
      * @return Nothing
      */
     private void rightMultiplyS(int s) {
         if (s <= rank && s >= 1) {
             int temp;
             int sign = 1;
 
             if (s == 1) {
                 sign = -1;
                 s = 2;
             }
 
             temp = oneLine[s-2];
             oneLine[s-2] = sign * oneLine[s-1];
             oneLine[s-1] = sign * temp;
         }
     }
 
     /**
      * This method multiplies the element on the left by a generator,
      * s.
      * @param s The generator
      * @return Nothing
      */
     private void leftMultiplyS(int s) {
         if (s <= rank && s >= 1) {
             int val1;
             int val2;
             int loc1 = 0;
             int loc2;
             int sign = 1;
 
             if (s == 1) {
                 sign = -1;
             }
 
             while (Math.abs(oneLine[loc1]) != s && Math.abs(oneLine[loc1]) != s + 1) {
                 loc1++;
             }
             val1 = oneLine[loc1];
             loc2 = loc1 + 1;
 
             while (Math.abs(oneLine[loc2]) != s && Math.abs(oneLine[loc2]) != s + 1) {
                 loc2++;
             }
             val2 = oneLine[loc2];
             oneLine[loc1] = sign * val2;
             oneLine[loc2] = sign * val1;
         }
     }
 
     /**
      * This method tells if an element has a reduced expression ending
      * in two noncommution generators.
      * @return true if the element is right bad
      */
     public boolean isRightBad() {
         if (-1 * oneLine[1] > oneLine[0] && -1 * oneLine[0] > oneLine[2]) return false; // 13
         if (oneLine[1] > oneLine[2] && -1 * oneLine[2] > oneLine[0]) return false; // 31
         
         for(int j = 0; j < rank - 3; j++) {
             if (oneLine[j] > oneLine[j + 1] && oneLine[j + 1] > oneLine[j + 2]) return false; //321
             if (oneLine[j] > oneLine[j + 2] && oneLine[j + 2] > oneLine[j + 1]) return false; //312
             if (oneLine[j + 1] > oneLine[j] && oneLine[j] > oneLine[j + 2]) return false; //231
         }
 
         return true;
     }
 
     /**
      * This method tells if an element has a reduced expression
      * beginning in two noncommution generators.
      * @return true if the element is left bad
      */
     public boolean isLeftBad() {
         return findInverse().isRightBad();
     }
 
     /**
      * This method tells if an element has a reduced expression
      * beginning or ending in two noncommution generators, or if the
      * element is a product of commuting generators.
      * @return true if the element is bad
      */
     public boolean isBad() {
         if (commutingGenerators()) {
             return false;
         }
         return (isRightBad() && isLeftBad());
     }
 
     /**
      * This method tells if an element is a product of commuting
      * generators.
      * @return true if the element is a product of commuting
      * generators
      */
     public boolean commutingGenerators() {
         int j = 0;
         if (oneLine[0] == 1) {
             j = 1;
         }
         else if (oneLine[0] == -1) {
             if (oneLine[1] != -2) {
                 return false;
             }
             j = 2;
         }
         else if (oneLine[0] == 2) {
             if (oneLine[1] != 1) {
                 return false;
             }
             j = 2;
         }
         else if (oneLine[0] == -2) {
             if (oneLine[1] != -1) {
                 return false;
             }
             j = 2;
         }
         else {
             return false;
         }
         
         while (j < rank - 2) {
             if (oneLine[j] > j + 2) {
                 return false;
             }
             else if (oneLine[j] == j + 1) {
                 j += 1;
             }
             else { // oneLine[j] = j + 2
                 if (oneLine[j + 1] != j + 1) return false;
                 j += 2;
             }
         }
             
         return true;
     }
     
     /**
      * This method gets the length of the element
      * @return the length of the element.
      */
     public int length() {
         return countInv(1) + countInv(-1);
     }
 
     /**
      * This method counts the number of inversions in the signed
      * permutation of the element.
      * @return the number of inversions
      */
     private int countInv() {
         return countInv(1);
     }
 
     /**
      * This method counts the number of inversions in the signed
      * permutation of the element, allowing for the multiplication of
      * factor (usually -1).
      * @return the number of inversions, taking the factor into
      * account.
      */
     private int countInv(int factor) {
         return countInv(factor, 0, rank);
     }
 
     /**
      * This is a helper method for countInv(int).  It counts the
      * number of inversions between start and end in the signed
      * permutation of the element, allowing for the multiplication of
      * factor (usually -1).
      * @return the number of inversions, taking the factor into
      * account.
      */
     private int countInv(int factor, int start, int end) {
         if (end - start <= 1) {
             return 0;
         }
         else if (end - start == 2) {
             if (factor * oneLine[start] > oneLine[end - 1]) {
                 return 1;
             }
             return 0;
         }
         int middle = (start + end) / 2;
         return countInv(factor, start, middle) + countInv(factor, middle, end) + mergeInv(factor, start, end);
     }
 
     /**
      * This method counts the number of inversions between start and
      * end in the signed permutation of the element tha happen between
      * the first and second half of the array, allowing for the
      * multiplication of factor (usually -1).
      * @return the number of inversions, taking the factor into
      * account.
      */
     private int mergeInv(int factor, int start, int end) {
         int middle = (start + end) / 2;
         int count = 0;
 
         for (int i = start; i < middle; i++) {
             int value = factor*oneLine[i];
             for (int j = middle; j < end; j++) {
                 if (value > oneLine[j]) {
                     count++;
                 }
             }
         }
 
         return count;
     }
 
     /**
      * This method gets the right descent set of the element.
      * @return the right descent set.
      */
     public BoundedSet rightDescent() {
         BoundedSet right = new BoundedSet(1, rank);
 
         for (int i = 1; i <= rank; i++) {
             if (isRightDescent(i)) {
                 right.add(i);
             }
         }
 
         return right;
     }
 
     /**
      * This method returns true if s is in the right descent set of
      * the elemtent, false otherwise.
      * @return true is s is a descent of the element
      */
     private boolean isRightDescent(int s) {
         if (s == 1) {
             return (-1 * oneLine[1] > oneLine[0]);
         }
         if (s >= 2 && s <= rank) {
             return (oneLine[s - 2] > oneLine[s - 1]);
         }
         return false;
     }
 
     /**
      * This method gets the left descent set of the element.
      * @return the left descent set.
      */
     public BoundedSet leftDescent() {
         return findInverse().rightDescent();
     }
 
     /**
      * This method creates a CoxeterElement reduced expression from a
      * signed permutation.
      * @return a reduced expression
      */
     public CoxeterElement findRE() {
         ArrayList<Integer> generator = new ArrayList<Integer> ();
         Element permutation = new Element(oneLine);
         
         while (permutation.length() != 0) {
             for (int i = permutation.rank; i >= 1; i--) {
                 if (permutation.isRightDescent(i)) {
                     generator.add(i);
                     permutation.rightMultiplyS(i);
                 }
             }
         }
 
         int length = generator.size();
         int[] genArray = new int[length];
         for (int i = 0; i < length; i++) {
             genArray[length - 1 - i] = generator.get(i).intValue();
         }
 
         CoxeterElement redExp = new CoxeterElement(genArray, permutation.rank);
         return redExp;
         
     }
 }
