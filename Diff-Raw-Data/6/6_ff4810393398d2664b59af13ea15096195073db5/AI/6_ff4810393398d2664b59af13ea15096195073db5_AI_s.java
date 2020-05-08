 import com.sun.jna.Pointer;
 
 ///The class implementing gameplay logic.
 class AI extends BaseAI
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
     for(int i = 0; i < bots.length; i++)
     {
       if(bots[i].getOwner() == playerID())
       {
         bots[i].talk("It is turn: "+Integer.toString(turnNumber()) + " And my id is: "+bots[i].getId()+" Hello World!");
       }
     }
     return true;
   }
 
 
   //This function is called once, before your first turn
   public void init() {}
   
   
   public AI(Pointer c)
   {
     super(c);
   }
 }
