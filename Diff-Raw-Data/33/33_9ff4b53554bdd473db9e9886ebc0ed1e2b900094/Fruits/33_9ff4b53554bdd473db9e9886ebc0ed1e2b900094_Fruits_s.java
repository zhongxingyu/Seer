 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package plantgame.models;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import plantgame.utils.FruitsEnum;
 import plantgame.utils.QualitiesEnum;
 import plantgame.utils.Constants;
 
 /**
  *
  * @author aadu
  */
 public class Fruits {
     
     private FruitsEnum fruitType = null;
     
     private int NumberOfTimesWater;
     private int NumberOfTimesFertilize;
     private QualitiesEnum quality;
     private boolean isGrown;
     private long startingTimeToGrow;
     private int id; // we need id to track which fruits have be sold by the user
     private double price = 0.0; // we need this to set the price of a fruit to be sold
     
     public Fruits(FruitsEnum type, int id)
     {
        
         fruitType = type;
         this.id = id;
         //When fruits object is created it is not grown yet
         isGrown = false;
         quality = QualitiesEnum.GROWING;
         //When fruits object is created it has not been watered or fertilized yet
         NumberOfTimesFertilize = 0;
         NumberOfTimesWater = 0;
         startingTimeToGrow = 0;
     }
     
     public Fruits()
     {
     }
     
     public FruitsEnum getType(){
       return fruitType;
     }
     
     public String getStatus(){
       if (isGrown ){
         return "Grown";
       }
       return "Growing";
     }
     
     public int getNumberOfTimesWater()
     {
         return NumberOfTimesWater;
     }
     
     public void setNumberOfTimesWater(int num)
     {
         NumberOfTimesWater = num;
     }
         
     public int getNumberOfTimesFertilize()
     {
         return NumberOfTimesFertilize;
     }
     public long getStartingTimeToGrow() {
         return startingTimeToGrow;
     }
     public void setStartingTimeToGrow() {
         startingTimeToGrow = System.currentTimeMillis();
     }
     
     public void setNumberOfTimesFertilize(int num)
     {
         NumberOfTimesFertilize = num;
     }
     
     public void setQuality(QualitiesEnum q){
       quality = q;
     }
     
     public QualitiesEnum getQuality(){
       return quality;
     }
     
     public int getId(){
       return id;
     }
     
     public double getPrice(){
       return price;
     }
     
     public void setPrice(double p){
       price = p;
     }
     
     public void startGrowing(){
       //DEBUG
       System.out.println("Fruits creating new timers "+this.fruitType.getTimeToGrow());
       
       try{
         Timer plantGrowingTimer = new Timer();
         Timer plantRottingTimer = new Timer();
         
         
         //Schedule the plant to reach fruition at a given delay and then
         //rot after a delay
         plantGrowingTimer.schedule(
                 new TimerTask(){
                   public void run(){
                     timerExpire();
                   }
                 }
                 ,this.fruitType.getTimeToGrow());
                 
          plantGrowingTimer.schedule(
                 new TimerTask(){
                   public void run(){
                     timerExpire();
                   }
                 }
                 ,this.fruitType.getTimeToGrow() + this.fruitType.getTimeToRot());               
 
       }
       catch (Exception e){
         //DEBUG
         System.out.println("Fruits grow timer terminated with exception");
         System.out.println(e.getStackTrace());
       }
       
     }
     
     private void timerExpire(){
       //TODO this happens when the fruit finishes growing
       //need to add code 
       
       //If this happens then it means the fruit has rotted
       if (this.isGrown){
         this.quality = QualitiesEnum.ROTTEN;
       }
       //If this happens then it means the fruit has grown
       else{
         //DEBUG
         System.out.println("Fruit "+this.fruitType.getName()+" finished growing");
 
         //The fruit is grown
         this.isGrown = true;
 
         //Determine the fruit's quality based on how many waterings and fertilizerings were applied
         this.quality = QualitiesEnum.getQuality(this.NumberOfTimesWater, this.NumberOfTimesFertilize);   
       }
     }
 }
