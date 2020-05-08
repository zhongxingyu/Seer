 package pset1;
 
 import org.apache.bcel.Repository;
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.classfile.Method;
 import org.apache.bcel.generic.ClassGen;
 import org.apache.bcel.generic.ConstantPoolGen;
 import org.apache.bcel.generic.INVOKEVIRTUAL;
 import org.apache.bcel.generic.Instruction;
 import org.apache.bcel.generic.InstructionHandle;
 import org.apache.bcel.generic.InstructionList;
 import org.apache.bcel.generic.InstructionTargeter;
 import org.apache.bcel.generic.MethodGen;
 import org.apache.bcel.generic.BranchInstruction;
 
 public class GraphGenerator {
   CFG createCFG(String className) throws ClassNotFoundException {
     CFG cfg = new CFG();
     JavaClass jc = Repository.lookupClass(className);
     ClassGen cg = new ClassGen(jc);
     ConstantPoolGen cpg = cg.getConstantPool();
     
     for (Method m: cg.getMethods()){
       MethodGen mg = new MethodGen(m, cg.getClassName(), cpg);
       InstructionList il = mg.getInstructionList();
       InstructionHandle[] handles = il.getInstructionHandles();
       
       for(InstructionHandle ih: handles){
         int position = ih.getPosition();
         cfg.addNode(position, m, jc);
         Instruction instr = ih.getInstruction(); 
         
         // adding edges to instructions/nodes
         // ignore branching instructions (jsr[_w], *switch)
         InstructionHandle instr_next = ih.getNext();
         
         // if statement
         BranchInstruction bi = null;
 //        if (instr.toString().indexOf("goto") != -1){
 //        	bi = (BranchInstruction) instr;
 //        	InstructionHandle if_ih = bi.getTarget();
 //        	int imm_pos = if_ih.getPosition();
 //        	int a = if_ih.getPosition(); 
 //        	cfg.addEdge(position, m, jc, if_ih.getPosition(), m, jc);
 //        }
         if (instr instanceof BranchInstruction){
         	bi = (BranchInstruction) instr;
         	InstructionHandle if_ih = bi.getTarget();
 ;        	int npos = if_ih.getPosition();
         	cfg.addEdge(position, m, jc, npos, m, jc);
         }
         
        if ((instr_next != null) && (instr.toString().indexOf("goto") == -1)){
             int nextPosition = instr_next.getPosition();
             cfg.addEdge(position, m, jc, nextPosition, m, jc);
         }
       }
     }
     
     return cfg;
   }
 
   public static void main(String[] a ) throws ClassNotFoundException {
     CFG cfg = new GraphGenerator().createCFG("pset1.Test2");
     cfg.printDB();
   }
 }
