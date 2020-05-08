 package zinara.ast.instructions;
 import zinara.code_generator.*;
 
 import zinara.ast.expression.Expression;
 
import java.io.IOException;

 public class Break extends Instruction{
     public Break(){}
 
     public String toString() {
 	return "<Break>";
     }
 
    public void tox86(Genx86 generator)
    throws IOException{
	generator.write(generator.jump(break_label));
     }
 }
