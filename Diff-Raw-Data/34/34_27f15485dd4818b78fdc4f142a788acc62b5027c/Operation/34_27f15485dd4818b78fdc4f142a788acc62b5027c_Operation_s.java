 package operation;
 
 // Libraries
 import arena.*;
 import robot.*;
 import exception.*;
 import stackable.*;
 
 /**
 * Class Operation.
  * Validates the type and arguments of
 * a given operation. Also, is an interface
 * betwen the arena and a RVM.
  * 
  * @author Karina Suemi 
  * @author Renato Cordeiro
  * @author Vinícius Silva 
 * @see RVM 
 * @see World
  */
 public class Operation
 {
     final private   Stackable arg;
     final private   String action;
     final private   RVM robot;
     private boolean rightType;
     
     /**
      * Class constructor.
     * @param Action to be executed
     * @param Required arguments
      * 
      * @throws WrongTypeException
      * @throws InvalidOperationException
      */
     public Operation(RVM robot, String action, Stackable arg)
         throws InvalidOperationException,
                WrongTypeException
     {
         this.arg    = arg;
         this.robot  = robot;
         this.action = action;
         
         switch (this.action)
         {
             case "MOVE" : rightType = MOVE(); break;
             case "DRAG" : rightType = DRAG(); break;    
             case "DROP" : rightType = DROP(); break;
             case "HIT"  : rightType = HIT (); break;
             case "LOOK" : rightType = LOOK(); break;
             case "SEE"  : rightType = SEE (); break; 
             default     : throw new InvalidOperationException(this.action);
         }
         if(!rightType)
         {
             throw new WrongTypeException("argument to operation " + 
                 this.action + ", but argument was " + arg.toString());
         }
     }
     
     // Getters
     public String    getAction   () { return this.action; }
     public Stackable getArgument () { return this.arg;    }
     
     // Verify arguments
     private boolean MOVE () { return this.arg instanceof Direction; }
     private boolean DRAG () { return this.arg instanceof Direction; }
     private boolean DROP () { return this.arg instanceof Direction; }
     private boolean HIT  () { return this.arg instanceof Direction; }
     private boolean LOOK () { return this.arg instanceof Direction; }
     private boolean SEE  () { return true;                          }
 }
