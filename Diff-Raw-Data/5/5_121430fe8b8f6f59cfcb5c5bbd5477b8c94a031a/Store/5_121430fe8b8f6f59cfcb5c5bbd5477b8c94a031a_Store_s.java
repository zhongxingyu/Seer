 /**
  * Store.java
  *
  * An instruction representing a STORE-X in the Abstract Machine Language.
  */
 
 package wam.code;
 
 import wam.ast.Node;
 import wam.exception.*;
 import wam.vm.*;
 
 public class Store extends Instruction {
     /**
      * The id this instruction is supposed to store.
      */
     private final String id;
 
     /**
      * Create a new Store instruction.
      *
      * @param node @see Instruction
      * @param id The id to store
      */
     public Store(Node node, String id) {
         super(node, Type.STORE);
         this.id = id;
     }
 
     @Override
     public String toString() {
         return String.format("STORE-%s", id);
     }
 
     @Override
     public void execute(Configuration config) throws OperandMisMatchException {
         Operand operand = config.getOperands().pop();
 
         if (!operand.getType().equals(Operand.Type.Integer))
             throw new OperandMisMatchException(Operand.Type.Integer.toString(), operand.getType().toString());
 
         config.getState().set(id, ((IntegerOperand)operand).getValue());
     }
 }
