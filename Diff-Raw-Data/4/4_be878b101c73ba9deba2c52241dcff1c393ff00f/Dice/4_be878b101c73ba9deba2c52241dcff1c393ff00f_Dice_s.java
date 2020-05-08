 package ch22.ex22_05;
 
 import java.util.Random;
 
 class Dice {
   private Random random;
 
   Dice() {
     random = new Random();
   }
 
  public short hrowDice() {
    return (short)random.nextInt(6) + 1;
   }
 }
 
