 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package plantgame.models;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import plantgame.utils.FruitsEnum;
 import plantgame.utils.QualitiesEnum;
 
 /**
  *
  * @author aadu
  */
 public class Fruits extends TimerTask{
     
     private FruitsEnum fruitType = null;
     private int fruitId;
     
     private int NumberOfTimesWater;
     private int NumberOfTimesFertilize;
     private QualitiesEnum quality;
     private boolean isGrown;
     
    Fruits(FruitsEnum type, int id)
     {
         fruitId = id;
         fruitType = type;
         
         //When fruits object is created it is not grown yet
         isGrown = false;
         quality = QualitiesEnum.ROTTEN;
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
     
     public void startGrowing(){
       //DEBUG
       System.out.println("Fruits creating new timer");
       
       Timer plantGrowingTimer = new Timer();
       
       //Schedule the plant to reach fruition at a given delay
       plantGrowingTimer.schedule(this, this.fruitType.getTimeToGrow());
     }
     
     @Override
     public void run(){
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
         
         //Start a new timer that will count down until the fruit is rotten
         Timer plantRottingTimer = new Timer();
         plantRottingTimer.schedule(this, this.fruitType.getTimeToRot());
       }
 
       
     }
 }
