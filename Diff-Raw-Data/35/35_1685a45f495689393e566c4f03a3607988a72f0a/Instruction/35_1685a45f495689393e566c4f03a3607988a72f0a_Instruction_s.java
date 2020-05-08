 package zinara.ast.instructions;
 
 import zinara.ast.ASTNode;
 import zinara.ast.instructions.Instruction;
 import zinara.code_generator.Genx86;
 import zinara.exceptions.InvalidCodeException;
 import zinara.exceptions.TypeClashException;
 
 import java.io.IOException;
 
 public abstract class Instruction extends ASTNode {
     public String nextInst;
     public String break_label;
     public String continue_label;
     public abstract String toString();
     public abstract void tox86(Genx86 generate)
 	throws InvalidCodeException,IOException;
     
     public void checkNoReturns(){};
 
     public void set_breaks_continues(CodeBlock code,
 				     String break_label,
 				     String continue_label){
 	Instruction inst;
 
 	for (int i = 0; i < code.getBlock().size(); i++) {
 	    inst = (Instruction)code.getBlock().get(i);
 	    
	    if ((inst instanceof Return)||
 		(inst instanceof If)){
 		inst.break_label = break_label;
 		inst.continue_label = continue_label;
 	    }
 	}
     }
 
 }
