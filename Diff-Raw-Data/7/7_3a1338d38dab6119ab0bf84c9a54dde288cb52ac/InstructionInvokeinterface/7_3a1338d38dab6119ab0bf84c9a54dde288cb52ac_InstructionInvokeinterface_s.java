 package sword.java.class_analyzer.code.instructions;
 
 import sword.java.class_analyzer.FileError;
 import sword.java.class_analyzer.code.ByteCodeInterpreter;
 import sword.java.class_analyzer.code.IncompleteInstructionException;
 import sword.java.class_analyzer.pool.ConstantPool;
 import sword.java.class_analyzer.pool.InterfaceMethodEntry;
 
 public class InstructionInvokeinterface extends AbstractInvokeInstruction<InterfaceMethodEntry> {
 
     private final int mCount;
 
     public InstructionInvokeinterface(byte[] code, int index, ConstantPool pool, ByteCodeInterpreter interpreter) throws IllegalArgumentException,
             IncompleteInstructionException, FileError {
         super(code, index, pool, InterfaceMethodEntry.class, interpreter);
         mCount = (code[index + 3]) & 0xFF;
     }
 
     @Override
     public String disassemble() {
         return "invokeinterface\t(" + mCount + ") " + methodToString();
     }
 }
