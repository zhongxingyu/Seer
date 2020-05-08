 package zinara.ast.expression;
 import zinara.code_generator.*;
 
 import zinara.ast.type.Type;
 import zinara.symtable.*;
 
 public class Identifier extends LValue {
     private String identifier;
     private SymTable symtable;
 
     public Identifier (String id, SymTable st) {
 	identifier = id;
 	symtable = st;
     }
 
     public String getIdentifier() { return identifier; }
     public SymTable getSymTable() { return symtable; }
     public SymValue getSymValue(){
 	return symtable.getSymbol(identifier);
     }
 
     public Type getType() {
	if (type != null) return type;
	type = symtable.getSymValueForId(identifier).getType();
 	return type;
     }
     public String toString() { return identifier; }
 
     public String tox86(Genx86 generate){
 	return generate.data_offset()+"+"+getSymValue().getDesp();
     }
 }
