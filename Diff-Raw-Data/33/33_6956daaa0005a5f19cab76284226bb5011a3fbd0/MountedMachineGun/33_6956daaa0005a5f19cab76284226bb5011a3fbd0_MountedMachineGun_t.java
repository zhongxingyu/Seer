 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 /**
  * Write a description of class MountedMachineGun here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class MountedMachineGun extends German
 {
     private static int MAG_SIZE = 200;
     private int fireRate = 15;
     private int shotClock = 0;
     private int magazineLeft = MAG_SIZE;
     private int targetx;
     private int targety;
     private int ydirection = 10;
     private int xdirection = 20;
     private int standingX;
     private int runTimer =0;
     public MountedMachineGun(int standingX, int startTargetX, int startTargetY){
         targetx = startTargetX;
         targety = startTargetY;
         this.standingX = standingX;
         setImage("enemy-running-left-1.png");
     }
     
     /**
      * Act - do whatever the MountedMachineGun wants to do. This method is called whenever
      * the 'Act' or 'Run' button gets pressed in the environment.
      */
     public void act() 
     {
      if(getWorld() != null){
        if(!isPlanted() && !isRetreating())
            NPCAdvance(1);
        else if (!isRetreating()){
            // Add your action code here.
            if(shotClock % fireRate == 0){
                ((HumanWorld)this.getWorld()).addBullet(this, targetx, targety);
            }
           // updateTargetX();
            updateTargetY();
            shotClock++;
      }
    }
     }    
     
     public boolean isPlanted(){
         return standingX == getX();
     }
     public void updateTargetX(){
         targetx += xdirection;
     }
     
     public void animateBack(){
         setLocation(getX() +1, getY());
         setRetreat(true);
         runTimer++;
         if (runTimer==24)
         {
              setImage("enemy-running-left-4.png");
              getImage().mirrorHorizontally();
              runTimer=0;  
         }
         if (runTimer==0){ setImage("enemy-running-left-1.png");  getImage().mirrorHorizontally();}
         if (runTimer==8) {setImage("enemy-running-left-2.png");getImage().mirrorHorizontally();}  
         if (runTimer==16) {setImage("enemy-running-left-3.png");getImage().mirrorHorizontally();}
     }
     
     public void updateTargetY(){
         targety += ydirection;
         if(targety > getWorld().getHeight() || targety <= 0){
             ydirection = -1 * ydirection;   
             xdirection = -1 * xdirection;
         }
             
     }
 }
