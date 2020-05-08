 package zinara.ast.instructions;
 import zinara.code_generator.*;
 
 import zinara.ast.expression.Expression;
 
import java.io.IOException;

 public class Continue extends Instruction{
     public Continue(){}
 
     public String toString() {
 	return "<Continue>";
     }
 
    public void tox86(Genx86 generator)
    throws IOException{
	generator.write(generator.jump(continue_label));
     }
 }
