 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author YtwYtw
  */
public class DigitCounter implements OdometerInterface {
 
     public static int digitss;
     protected int[] DigitsArray;
     
     public DigitCounter (int digits) {
         int i=0;
         digitss = digits;
         if (digits == 0 || digits < 0) {
             throw new IllegalArgumentException("Digits must be greater than ZERO");
         }
         int[] DigitsArray = new int[(digits+1)];
         for (i = 0 ; i < (digits+1) ; i++ ) {
             DigitsArray[i] = 0;
         }
         DigitsArray[digits] = 1;
         this.DigitsArray = DigitsArray;
 //        System.out.println("New counter created");
     }
 
     public void increment() {
         if (DigitsArray[0] < 9) {
             DigitsArray[0] = DigitsArray[0] + 1;
         }
         else {
             DigitsArray[0] = 0;
             DigitsArray[1] = DigitsArray [1] + 1;
             CheckTen();
         }
     }
 
     public void decrement() {
         if (DigitsArray[0] > 0) {
             DigitsArray[0] = DigitsArray[0] - 1;
         }
         else {
             DigitsArray[0] = 9;
             DigitsArray[1] = DigitsArray [1] - 1;
             CheckZero();
         }
     }
 
     public void reset() {
         int i = 0;
         for (i = 0 ; i < digitss ; i++ ) {
             DigitsArray[i] = 0;
         }
         DigitsArray[digitss]=1;
     }       
         public void CheckZero() {
         int temp = 0;
         for (temp = 0; temp < digitss ; temp++){
             if (DigitsArray[temp] < 0) {
                 DigitsArray[temp] = 9;
                 DigitsArray[temp+1] = DigitsArray[temp+1] - 1;
             }
         }
         DigitsArray[(digitss)] = 1;
     }
     
     public void CheckTen() {
         int temp = 0;
         for (temp=0;temp<digitss;temp++){
             if (DigitsArray[temp] > 9) {
                 DigitsArray[temp] = 0;
                 DigitsArray[temp+1] = DigitsArray[temp+1] + 1;
             }
         }
     }
 
     public String display() {
         String temp=" ";int i;
         for (i=(digitss-1);i > -1;i--) {
  //           System.out.println("String Adder now is" + DigitsArray[i]);
             temp = temp + "" + DigitsArray[i];
         }
         return temp;
     }
 }
 
 
 
