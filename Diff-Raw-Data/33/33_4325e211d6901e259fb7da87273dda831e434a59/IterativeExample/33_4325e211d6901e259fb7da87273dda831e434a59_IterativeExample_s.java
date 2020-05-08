 package com.mycompany.testproject.iteratives;
 
 
 public class IterativeExample {
     
     
     static int[] numbers = new int[] {1,2,3,4,5,6,7,8,9,10};
     /**
      * Factorial function used to test for loops
      * @param number
      * @return 
      */
     public static int factorialForFunction(int number){
         int sum = number;
         for (int i = number-1; i > 0; i--){
             sum *= i;
         }
         return sum;
     }
     
     /**
      * Factorial function used to test while loops
      * @param number
      * @return 
      */
     public static int factorialWhileFunction(int number){
         int sum = number;
         while (number > 0) {
             sum *= number;
             number --;
         }
         return sum;
     }
     
    public static int factorialForEachFunctionTen(){
         int sum = numbers[0];
         for (int i : numbers) {
             sum *= i;
         }
         return sum;
     }
     
     
     public static int iterativeFunction(int number) {
         while (number != 1) {
             if (number % 2 == 0) {
                 number /= 2;
             } else {
                 number *= 3;
                 number += 1;
             }
         }
         return number;
     }
     
     
     
 }
