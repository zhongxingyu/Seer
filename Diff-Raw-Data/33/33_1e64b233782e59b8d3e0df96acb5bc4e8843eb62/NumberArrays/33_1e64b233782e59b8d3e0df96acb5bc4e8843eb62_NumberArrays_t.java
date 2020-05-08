 package com.kellyfj.codingkata.arrays;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 public class NumberArrays {
 
     /**
      * Initialize min and max as minimum and maximum of the first two
      * elements respectively. For rest of the elements, pick them in pairs and
      * compare their maximum and minimum with max and min respectively.
      * http://www.geeksforgeeks.org/maximum-and-minimum-in-an-array/
      * 
      * @param arr
      * @return
      */
     public static MinMax getMinMax(int[] arr) {
         MinMax minmax = new MinMax();
 
         minmax.comparisons=1;
         if (arr[0] > arr[1]) {
             minmax.max = arr[0];
             minmax.min = arr[1];
         } else {
             minmax.min = arr[0];
             minmax.max = arr[1];
         }
        int i = 1;
     
         // In the while loop, pick elements in pair and compare the pair with current min/max
         while (i < arr.length - 1) {
             minmax.comparisons++;
             if (arr[i] > arr[i + 1]) {
                 minmax.comparisons++;
                 if (arr[i] > minmax.max) minmax.max = arr[i];
                 minmax.comparisons++;
                 if (arr[i + 1] < minmax.min) minmax.min = arr[i + 1];
             } else {
                 minmax.comparisons++;
                 if (arr[i + 1] > minmax.max)  minmax.max = arr[i + 1];
                 minmax.comparisons++;
                 if (arr[i] < minmax.min)  minmax.min = arr[i];
             }
             i += 2; //Increment the index by 2 as two elements are processed
         }
 
         return minmax;
     }
 
     public static class MinMax {
 
         public int comparisons;
         public int min;
         public int max;
 
     }
 
     /**
      * http://codercareer.blogspot.com/2011/10/no-09-numbers-with-given-sum.html
      * Given an array, please determine whether it contains three numbers whose
      * sum equals to 0.
      */
     public static boolean doesSumOfTwoEqualN_orderNlogN(int[] array, int target) {
         if (array.length < 2)
             throw new IllegalArgumentException("Array length should be 2 or greater");
         Arrays.sort(array);
 
         int startPointer = 0;
         int endPointer = array.length - 1;
         while (startPointer < endPointer) {
             int sum = array[startPointer] + array[endPointer];
             if (sum == target) {
                 System.out.println("Numbers [" + array[startPointer] + "] and [" + array[endPointer] + "] sum to ["
                         + target + "]");
                 return true;
             } else {
                 if (sum > target) {
                     endPointer--;
                 } else {
                     startPointer++;
                 }
             }
         }
         return false;
     }
 
     public static boolean doesSumOfTwoEqualN_orderN(int[] array, int sumOfTwoTarget) {
         if (array.length < 2)
             throw new IllegalArgumentException("Array length should be 2 or greater");
         Map<Integer, Integer> numberCount = new HashMap<Integer, Integer>();
 
         // Pre-process
         for (int i = 0; i < array.length; i++) {
             int number = array[i];
             if (numberCount.containsKey(number)) {
                 Integer c = numberCount.get(number);
                 c++;
                 numberCount.put(number, c);
             } else {
                 numberCount.put(number, 1);
             }
         }
 
         // Check against Map
         for (int i = 0; i < array.length; i++) {
             int firstNum = array[i];
             int complement = sumOfTwoTarget - array[i];
             if (numberCount.containsKey(complement)) {
                 if (firstNum == complement) {
                     int count = numberCount.get(complement);
                     if (count >= 2)
                         return true;
                 } else {
                     return true;
                 }
 
             }
         }
 
         return false;
     }
 
     public static boolean doesSumOfThreeEqualN(int[] array, int target) {
         if (array.length < 3)
             throw new IllegalArgumentException("At least 3 elements expected in the array");
         Arrays.sort(array);
         int pointer = 0;
         while (pointer < array.length - 1) {
             int sumOfTwoTarget = target - array[pointer];
             boolean res = doesSumOfTwoEqualN_orderN(array, sumOfTwoTarget);
             if (res) {
                 return true;
             }
             pointer++;
         }
         return false;
     }
 
     /**
      * http://stackoverflow.com/questions/2070359/finding-three-elements-in-an-array-whose-sum-is-closest-to-an-given-number
      * @param A
      * @param target
      * @return
      */
     public static boolean doesSumOfThreeEqualN_faster(int[] A, int target) {
         if (A.length < 3)
             throw new IllegalArgumentException("At least 3 elements expected in the array");
         Arrays.sort(A);
         for (int i = 0; i < A.length - 2; i++) {
             int j = i+1; // Start where i is.
             int k = A.length-1; // Start at the end of the array.
 
             while (k >= j) {
                 int sum = A[i] + A[j] + A[k];
                 if ( sum == target ) return true;
 
                 // We didn't match. Let's try to get a little closer:
                 if (sum > target)
                     k--;
                 else
                     j++;
             }
             // When the while-loop finishes, j and k have passed each other and
             // there's no more useful combinations that we can try with this i.
         }
         return false;
     }
     
     /**
      * Randomly shuffle object in an array
      * @param a
      */
     public static void shuffle(Object[] a) {
         Random rnd = new Random();
         int mid = a.length / 2;
         for (int i = mid; i < a.length; i++) {
             int lo = rnd.nextInt(mid);
             Object buffer = a[lo];
             a[lo] = a[i];
             a[i] = buffer;
         }
     }
 
     public static void printArray(Object[] a) {
         for (int i = 0; i < a.length; i++) {
             System.out.print(a[i].toString() + " ");
         }
         System.out.println();
     }
 
     public static void printArray(int[] a) {
         for (int i = 0; i < a.length; i++) {
             System.out.print(a[i] + " ");
         }
         System.out.println();
     }
 
     
     public static int lengthOfLongestMonotonicSeries(int[] input) {
         int maxLength = 1;
         int currLength = 1;
         for (int i = 0; i < input.length - 1; i++) {
 
             if (input[i] == input[i + 1] - 1) {
                 currLength++;
                 maxLength = currLength > maxLength ? currLength : maxLength;
             } else {
                 currLength = 0;
             }
         }
 
         return maxLength;
     }
 
     public static void dutchNationalFlagBasic(int[] input) {
         int numZeroes = 0;
         int numOnes = 0;
         int numTwos = 0;
 
         for (int i = 0; i < input.length; i++) {
             if (input[i] == 0)
                 numZeroes++;
             else if (input[i] == 1)
                 numOnes++;
             else if (input[i] == 2)
                 numTwos++;
         }
 
         for (int i = 0; i < numZeroes; i++)
             input[i] = 0;
         for (int i = numZeroes; i < numZeroes + numOnes; i++)
             input[i] = 1;
         for (int i = numZeroes + numOnes; i < numZeroes + numOnes + numTwos; i++)
             input[i] = 2;
 
     }
 
     public static void dutchNationalFlagOnePass_Order1AdditionalSpace(int[] a, int mid) {
         int startIndex = 0;
         int endIndex = a.length - 1;
 
         for (int i = 0; i <= endIndex;) {
             if (a[i] < mid) {
                 swap(a, startIndex, i);
                 i++;
                 startIndex++;
             } else if (a[i] > mid) {
                 swap(a, endIndex, i);
                 endIndex--;
                 // do not increment i. We have to revisit this again
             } else {
                 i++;
             }
         }  
 
     }
 
     public static int findIndexOfItemInSortedArray(int[] a, int lookFor) {
         int leftPointer=0;
         int rightPointer=a.length-1;
         int result=-1;
         
         while(leftPointer <= rightPointer) {
             int pivot = leftPointer+((rightPointer-leftPointer) / 2);
             int pivotValue = a[pivot];
             if(pivotValue > lookFor) {
                 rightPointer = pivot-1; //Reduce search space 
             } else if (pivotValue == lookFor) {
                 result=pivot; //Record solution and keep searching to the left
                 rightPointer = pivot -1;
             } else {  //pivot < lookfor
                 leftPointer = pivot+1;
             }
         }
         
         return result;
     }
     
     private static void swap(int[] a, int startIndex, int i) {
         int temp;
         temp = a[i];
         a[i] = a[startIndex];
         a[startIndex] = temp;
     }
 
     public static int countBinaryOnes(int number) {
         String binaryString = Integer.toBinaryString(number);
         return binaryString.length() - binaryString.replace("1", "").length();
     }
 
     public static String getBinaryRepresentation(int number) {
         int b[] = new int[32];
         int i = 0;
         while (number != 0) {
             i++;
             b[i] = number % 2;
             number = number / 2;
         }
         StringBuilder sb = new StringBuilder();
         for (int j = i; j > 0; j--) {
             sb.append(b[j]);
         }
         return sb.toString();
     }
 
     public static List<Set<Integer>> powerSet(int[] array) {
         List<Set<Integer>> returnList = new ArrayList<Set<Integer>>();
         if (array.length == 0) {
             returnList.add(new HashSet<Integer>());
             return returnList;
         }
 
         int head = array[0];
         int[] rest = new int[] {};
         if (array.length >= 2)
             rest = Arrays.copyOfRange(array, 1, array.length);
 
         for (Set<Integer> aSet : powerSet(rest)) {
             Set<Integer> newSet = new HashSet<Integer>();
             newSet.add(head);
             newSet.addAll(aSet);
             returnList.add(newSet);
             returnList.add(aSet);
         }
         return returnList;
     }
 
     public static List<Set<Integer>> setsOfSizeK(int[] array, int k) {
         List<Set<Integer>> returnList = new ArrayList<Set<Integer>>();
         if (array.length == 0) {
             returnList.add(new HashSet<Integer>());
             return returnList;
         }
 
         int head = array[0];
         int[] rest = new int[] {};
         if (array.length >= 2)
             rest = Arrays.copyOfRange(array, 1, array.length);
 
         for (Set<Integer> s : powerSet(rest)) {
             Set<Integer> newSet = new HashSet<Integer>();
             newSet.add(head);
             newSet.addAll(s);
             if (newSet.size() == k)
                 returnList.add(newSet);
             if (s.size() == k)
                 returnList.add(s);
         }
         return returnList;
     }
 
 
     public static void printPowerSet(List<Set<Integer>> listOfSets) {
         System.out.println("POWERSET");
         for (Set<Integer> set : listOfSets) {
             System.out.print("{");
             for (Integer i : set) {
                 System.out.print(i + " ");
             }
             System.out.println("}");
         }
     }
 
     public static double calcSquareRoot(double num) {
         double guess = 1;
         double diff = Math.abs(guess * guess - num);
         while (diff > 0.0001) {
             diff = Math.abs(guess * guess - num);
             guess = (guess + num / guess) / 2;
             // System.out.println("New Guess is [" + guess + "]");
         }
         return guess;
     }
 
     public static List<List<Integer>> findCombosToReachTargetSum(List<Integer> numbers, int target) {
         List<List<Integer>> ret = findCombosToReachTargetSum(numbers, target, new ArrayList<Integer>());
         return ret;
     }
 
     private static List<List<Integer>> findCombosToReachTargetSum(List<Integer> numbers, 
             int target, List<Integer> intermed) {
         int intermedSum = 0;
         for (int i : intermed)
             intermedSum += i;
 
         List<List<Integer>> ret = new ArrayList<List<Integer>>();
         if (intermedSum == target) {
             ret = new ArrayList<List<Integer>>();
             ret.add(intermed);
         }
 
         if (intermedSum > target)
             return null;
 
         // Pick a number
         for (int i = 0; i < numbers.size(); i++) {
             int n = numbers.get(i);
 
             // Start adding the other numbers until we reach conditions above
             ArrayList<Integer> remaining = new ArrayList<Integer>();
             for (int j = i + 1; j < numbers.size(); j++)
                 remaining.add(numbers.get(j));
 
             ArrayList<Integer> intermedCopy = new ArrayList<Integer>(intermed);
             intermedCopy.add(n);
             
             List<List<Integer>> temp = findCombosToReachTargetSum(remaining, 
                     target, intermedCopy);
             if (temp != null) {
                 ret.addAll(temp);
             }
         }
 
         return ret;
     }
     
     public static int[] merge(int[] a, int[] b) {
         int[] answer = new int[a.length + b.length];
         int i = 0, j = 0, k = 0;
 
         while (i < a.length && j < b.length)
         {
             if (a[i] < b[j])       
                 answer[k++] = a[i++];
             else        
                 answer[k++] = b[j++];               
         }
 
         //a[] longer than b[]
         while (i < a.length)  
             answer[k++] = a[i++];
 
         //b[] longer than a[]
         while (j < b.length)    
             answer[k++] = b[j++];
 
         return answer;
     }
     
     public static Integer[] mergeRemovingDupes(int[] a, int[] b) {
         Set<Integer> s = new HashSet<Integer>();
         
         for(int i: a) {
             if(!s.contains(i))
                 s.add(i);
         }
         for (int i : b) {
             if(!s.contains(i))
                 s.add(i);
         }
 
         return s.toArray(new Integer[0]);
     }
 }
