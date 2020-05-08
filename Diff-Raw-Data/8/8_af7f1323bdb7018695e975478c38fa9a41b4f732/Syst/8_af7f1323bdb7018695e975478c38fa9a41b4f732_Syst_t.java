 package robot;
 
 // Libraries
 import arena.*;
 import operation.*;
 import stackable.*;
 import exception.*;
 
 /**
  * <b>Assembly functions - class Syst</b><br>
  * Implements the commands MOVE, DRAG,
  * DROP, HIT, LOOK and get from the World
  * if it has permission for executing 
  * the correspondent action.
  *
  * @author Karina Suemi 
  * @author Renato Cordeiro
  * @author Vinícius Silva 
  * @see Ctrl
  * @see arena.World
  */
 final public class Syst
 {
     // No instances of this class allowed
     private Syst() {} 
     
     /**
      * General-model funcion action.
      * Takes out the top of the main stack,
      * generates an operand object and does
      * a system call. Pushes all the system
      * answers in the Virtual Machine main 
      * stack. 
      * <p>
      * If the command needs no argument, 
      * does not pop the stack.
      * 
      * @param rvm  Virtual Machine.
      * @param type Command type.
      *
      * @throws WrongTypeException
      * @throws InvalidOperationException 
      */
     private static void action(RVM rvm, String type)
         throws WrongTypeException,
                InvalidOperationException 
     {
         Stackable arg = null;
         if(!(type.equals("SEE")))
             arg = rvm.DATA.pop();
         Operation op;
         
         try { 
             op = new Operation(rvm, type, arg);
         }
         catch (InvalidOperationException e) {
             throw new WrongTypeException(type);
         }
         
         Stackable[] stk = World.ctrl(op);
         for(int i = 0; i < stk.length; i++) rvm.DATA.push(stk[i]);
         rvm.syscall = true;
     }
     
     /**
      * Assembly funcion MOVE. <br>
      * Makes a syscall, requesting for a 
      * movement in the arena.
      * 
      * @param  rvm Virtual Machine.
      * @throws WrongTypeException
      * @throws InvalidOperationException 
      */
     static void MOVE(RVM rvm)
         throws WrongTypeException,
                InvalidOperationException
     {
         action(rvm, "MOVE");
     }
     
     /**
      * Assembly funcion DRAG. <br>
      * Makes a syscall, requesting for  
      * taking an item from the arena.
      * 
      * @param  rvm Virtual Machine.
      * @throws WrongTypeException
      * @throws InvalidOperationException 
      */
     static void DRAG(RVM rvm)
         throws WrongTypeException,
                InvalidOperationException
     {
         action(rvm, "DRAG");
     }
     
     /**
      * Assembly funcion DROP. <br>
      * Makes a syscall, requesting for  
      * taking leaving an item in the 
      * arena.
      * 
      * @param  rvm Virtual Machine.
      * @throws WrongTypeException
      * @throws InvalidOperationException 
      */
     static void DROP(RVM rvm)
         throws WrongTypeException,
                InvalidOperationException
     {
         action(rvm, "DROP");
     }
     
     /**
      * Assembly funcion HIT. <br>
      * Makes a syscall, requesting to
      * attack a robot or a scenario
      * in the arena.
      * 
      * @param  rvm Virtual Machine.
      * @throws WrongTypeException
      * @throws InvalidOperationException 
      */
     static void HIT(RVM rvm)
         throws WrongTypeException,
                InvalidOperationException
     {
         Stackable atk = null;
         Stackable num = null;
         
         atk = rvm.DATA.pop();
         num = rvm.DATA.pop();
         int ndirs = (int) ((Num)num).getNumber();
         
         Stackable[] args = new Stackable[ndirs+2];
         args[0] = atk; args[1] = num;
         for(int i = 2; i < ndirs+2; i++)
             args[i] = rvm.DATA.pop();
         
         Operation op;
         try { 
             op = new Operation(rvm, "HIT", args);
         }
         catch (InvalidOperationException e) {
             throw new WrongTypeException("HIT");
         }
         
         Stackable[] stk = World.ctrl(op);
         for(int i = 0; i < stk.length; i++) rvm.DATA.push(stk[i]);
         rvm.syscall = true;
     }
     
     /**
      * Assembly funcion LOOK. <br>
      * Makes a syscall, requesting to
      * get a terrain for being analysed.
      * 
      * @param  rvm Virtual Machine.
      * @throws WrongTypeException
      * @throws InvalidOperationException 
     * @see    Check
      */
     static void LOOK(RVM rvm)
         throws WrongTypeException,
                InvalidOperationException
     {
         action(rvm, "LOOK");
     }
     
     /**
      * Assembly funcion SEE. <br>
      * Makes a syscall, requesting to
      * get a neighborhood for being 
      * analysed.
      * 
      * @param  rvm Virtual Machine.
      * @throws WrongTypeException
      * @throws InvalidOperationException 
     * @see    Check
      */
     static void SEE(RVM rvm)
         throws WrongTypeException,
                InvalidOperationException
     {
         action(rvm, "SEE");
     }
     
     /**
      * Assembly funcion ASK. <br>
      * Makes a syscall, requesting to
      * get info about the robot from 
      * the arena's control.
      * 
      * @param  rvm Virtual Machine.
      * @throws WrongTypeException
      * @throws InvalidOperationException 
     * @see    Check
      */
     static void ASK(RVM rvm)
         throws WrongTypeException,
                InvalidOperationException
     {
         action(rvm, "ASK");
     }
 }
