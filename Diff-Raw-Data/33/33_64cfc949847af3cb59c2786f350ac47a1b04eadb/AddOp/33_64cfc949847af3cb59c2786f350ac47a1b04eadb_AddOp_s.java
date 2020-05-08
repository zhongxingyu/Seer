 package machines.triv.instructions;
 
 import java.util.EmptyStackException;
 
 import machines.types.*;
 
 public class AddOp extends AbstractInstruction
 {
 
   public AddOp(AbstractMachine machine)
   {
     
     super(machine);
     
   }
 
   @Override
   public void executeInstruction()
   {
     int e1 = 0;
     int e2 = 0;
     
     try {
       e1 = m.pop().getInt();
       e2 = m.pop().getInt();
       
      //m.stack.get
     }
     catch (EmptyStackException e) {
       System.out.println("Can't pop from the stack... nothing there.");
     }
     
     m.push(new StackType(e1 + e2));
   }
 
 }
