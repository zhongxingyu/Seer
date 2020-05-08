 import com.sun.jna.Pointer;
 
 ///The class implementing gameplay logic.
 public class AI extends BaseAI
 {
   public String username()
   {
     return "Shell AI";
   }
   public String password()
   {
     return "password";
   }
 
   //This function is called each time it is your turn
   //Return true to end your turn, return false to ask the server for updated information
   public boolean run()
   {
     for(int ii=0;ii<creatures.length;ii++)
     {
       if (creatures[ii].getOwner() == playerID())
       {
         int plantIn = getPlantAtLocation(creatures[ii].getX()+1,creatures[ii].getY());
         if ((plantIn == -1 || (plantIn !=1 && plants[plantIn].getSize()==0)) && getCreatureAtLocation(creatures[ii].getX()+1,creatures[ii].getY()) == -1)
         {
           if(0<=creatures[ii].getX()+1 && creatures[ii].getX()+1<mapWidth() && 0<=creatures[ii].getY() && creatures[ii].getY()<mapHeight())
           {
            if (creatures[ii].getCurrentHealth()>healthPerMove() && creatures[ii].getMovementLeft()>0)
             {
               creatures[ii].move(creatures[ii].getX()+1,creatures[ii].getY());
             }
           }
         }      
         plantIn = getPlantAtLocation(creatures[ii].getX()+1,creatures[ii].getY());
         int creatIn = getCreatureAtLocation(creatures[ii].getX()+1,creatures[ii].getY());
         if (plantIn != -1 && plants[plantIn].getSize()>0 && creatures[ii].getCanEat()==1)
         {
           creatures[ii].eat(plants[plantIn].getX(),plants[plantIn].getY());
         }
         else if (creatIn!=-1 && creatures[creatIn].getOwner()!=playerID() && creatures[ii].getCanEat()==1)
         {
           creatures[ii].eat(creatures[creatIn].getX(),creatures[creatIn].getY());
         }
         else if (creatIn!=-1 && creatures[creatIn].getOwner()==playerID() && creatures[ii].getCanBreed()==1 && creatures[creatIn].getCanBreed()==1)
         {
          if (creatures[ii].getCurrentHealth()>healthPerBreed() && creatures[creatIn].getCurrentHealth()>healthPerBreed())
           {
             creatures[ii].breed(creatures[creatIn]);
           }
         }
       }
     }
     return true;
   }
 
 
   //This function is called once, before your first turn
   public void init() {}
 
   //This function is called once, after your last turn
   public void end() {}
   
   
   public AI(Pointer c)
   {
     super(c);
   }
 }
