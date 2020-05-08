 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package escapeee;
 
 /**
  *
  * @author Eric
  */
 public class Player {
     String name;
     double highscore = 0;
     
     public void setName(String theName){
         this.name = theName;
     }
     
     public void displayPlayerName(){
         System.out.println("My name is " + this.name);
     }
     
     public void displayScore(){
         System.out.println("Your score is " + this.highscore);
     }
     
     public long getTime(long startTime, long endTime){
         //long startTime = 10;
         
         //long endTime = 5;
         
        /* 
         * rkj - Needed two primitive variables -1
         *       No explicit type conversion -1
         *       Unit test not working -1
         */
        
         if (startTime > endTime){
             System.out.println("TIME ERROR");
             return -1;
         }
             
         long theTime = endTime - startTime;
         if (theTime < 0){
             System.out.println("NEGATIVE VALUE");
             return -1;
         }
         long timeToBeat = theTime * 1;
         
         System.out.println("You beat the game in " + theTime + "\nThe time to beat is " + timeToBeat);
         return timeToBeat;
     
     }
     
     public float getAgeInMonths(int ageInYears){
         if(ageInYears < 0 || ageInYears > 1000){
             System.out.println("Invalid Age - Too Old or Too Young");
             return -1;
         }
         float ageInWeeks = ageInYears / 52;
         float ageInMonths = ageInWeeks * 4;
         return ageInMonths; 
     }
     
     public float getWinPercent( float gamesStarted, float gamesFinished){
 	if (gamesStarted < 1 || gamesFinished < 0){
 	System.out.println("INVALID WIN PERCENT");
 	return -1;
         }
        
         
 	float getWinPercent = gamesFinished / gamesStarted;
 	float trueWinPercent = getWinPercent * 100;
 	 
         
         System.out.println("Your winning percentage is " + trueWinPercent);
         return trueWinPercent;
 }
 
 
 }
