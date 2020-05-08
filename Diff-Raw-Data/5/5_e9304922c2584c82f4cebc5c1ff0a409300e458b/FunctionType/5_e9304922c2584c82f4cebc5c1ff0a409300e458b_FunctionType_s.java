 package zinara.ast.type;
 
 import java.util.ArrayList;
 
 import zinara.ast.instructions.CodeBlock;
 
 public class FunctionType extends Type {
     public ArrayList argsTypes; // arraylist of Type
     public Type returnType;
     public CodeBlock codeBlock;
 
     public FunctionType(ArrayList al, Type rt, CodeBlock cb) { 
 	this.argsTypes = al;
 	this.returnType = rt;
 	this.codeBlock = cb;
     }
 
     public String toString() {
 	String ret = "";
 	for (int i = 0; i < argsTypes.size(); i++)
	    ret += (Type)argsTypes.get(i) + " -> ";
	return (ret + returnType);
     }
 
     public Type getType() { return this; }
 }
