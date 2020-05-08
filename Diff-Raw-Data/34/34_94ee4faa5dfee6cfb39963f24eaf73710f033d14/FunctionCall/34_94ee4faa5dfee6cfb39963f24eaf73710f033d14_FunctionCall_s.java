 package zinara.ast.expression;
 import zinara.code_generator.*;
 
 import java.util.ArrayList;
 
 import zinara.ast.type.Type;
 import zinara.ast.type.FunctionType;
 
 public class FunctionCall extends Expression {
     private String name;
     private ArrayList arguments; // arraylist of ????
     private SymTable symtable;
 
     public FunctionCall(String name, ArrayList s, SymTable st) { 
 	this.name = name;
 	arguments = s;
 	symtable = st;
	type = symtable.getSymValueForId(identifier).getType();
     }
 
     public Type getType() { return ((FunctionType)type).getReturnType(); }
 
     public String toString() { return name + "(" + arguments + ")"; }
 
     public String tox86(Genx86 generate){
         return "";
     }
 }
