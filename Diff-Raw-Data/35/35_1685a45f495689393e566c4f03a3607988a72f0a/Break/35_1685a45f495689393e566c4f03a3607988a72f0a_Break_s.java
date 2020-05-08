 package zinara.ast.instructions;
 import zinara.code_generator.*;
 
 import zinara.ast.expression.Expression;
 
 public class Break extends Instruction{
     public Break(){}
 
     public String toString() {
 	return "<Break>";
     }
 
    public void tox86(Genx86 generate){
     }
 }
