 package codegen;
 import ast.*;
 import java.util.*;
 import visitor.*;
 /* This is currently a total mess and should be farmed out a little bit, Should write IR nodes for every
  * ast node but much more organized, but this is for a later time and not oh god behind schedule crunch time
  *
  * TODO: make this not embarassing
  */
 public class Codegen implements AstVisitor
 {
 	private Map<String,String> typedefs;
 	public Codegen(Program p)
 	{
 		typedefs = new HashMap<String,String>();
 	}
 	public void accept(Program p)
 	{
 		//TODO
 	}
 	public void accept(Graph g)
 	{
		//TODO: fill in typedefs from the graph
 	}
 	public void accept(OpExp exp)
 	{
 		//should be handled from OpDef
 	}
 	public void accept(OpDef def)
 	{
 		//write our helper methods...
 		CheckShape(def);
 		CheckGuard(def);
 		Apply(def);
 
 		//TODO: generate the kernel that calls the apply above
 		//Kernel needs to handle getting the values for the apply. I'm not sure on how to do this right now
 		//Elixir paper explains it a bit but Im not sure I follow the non-slow version.. :(
 		//print variables so we have them all
 		List<String> declared = new ArrayList<String>();
 		for(Tuple t : def.exp.tuples) {
 			for(Attribute at : t.attributes) {
 				if(declared.contains(at.id.id))
 					continue;
 				System.out.printf("%s %s;\n", typedefs.get(at.id.id), at.var.id);
 			}
 		}
 		
 	}
 	public void Apply(OpDef def)
 	{
 		List<Tuple> node_items = new ArrayList<Tuple>();
 		List<Tuple> edge_items = new ArrayList<Tuple>();
 		List<Attribute> attributes = new ArrayList<Attribute>();
 		for(Tuple t: def.exp.tuples){
 			for(Attribute at : t.attributes)
 				attributes.add(at);
 			switch(t.type) {
 			case NODES:
 				node_items.add(t);
 				break;
 			case EDGES:
 				edge_items.add(t);
 				break;
 			}
 		}
 		//Generate the apply
 		StringBuilder argbuilder = new StringBuilder(""); //for the args coming in
 		StringBuilder parambuilder = new StringBuilder(""); //for params to function calls(keep the order right)
 		for (int i=0; i<attributes.size();i++) {
 			Attribute at = attributes.get(i);
 			argbuilder.append(typedefs.get(at.id.id)+ " " + at.var.id + ((i < attributes.size()-1) ? "," : ""));
 			parambuilder.append(at.var.id + ((i < attributes.size()-1) ? "," : " "));
 		}
 		System.out.printf("__device__ inline void _apply_%s(%s)\n{", def.id.id, argbuilder);
 		emit("if(!_checkshape_"+def.id.id+"("+parambuilder+")) return;");
 		//TODO: decide how locking works
 		//TODO: Sort nodes for locking
 		//TODO: Lock
 
 		//guard check
 		emit("if(_checkguard_"+def.id.id+"("+parambuilder+")) {");
 		for(Assignment assignment : def.exp.assignments)
 			assignment.visit(this);
 		emit("}"); //close to if checkguard
 		//TODO:Emit unlocks
 		emit("}\n");
 	}
 	public void CheckGuard(OpDef def)
 	{
 		/*TODO: This needs to go somewhere and be shared, probably in an OpDef IR node*/
 		List<Tuple> node_items = new ArrayList<Tuple>();
 		List<Tuple> edge_items = new ArrayList<Tuple>();
 		List<Attribute> attributes = new ArrayList<Attribute>();
 		for(Tuple t: def.exp.tuples){
 			for(Attribute at : t.attributes)
 				attributes.add(at);
 			switch(t.type) {
 			case NODES:
 				node_items.add(t);
 				break;
 			case EDGES:
 				edge_items.add(t);
 				break;
 			}
 		}
 		StringBuilder argbuilder = new StringBuilder("");
 		for (int i=0; i<attributes.size();i++) {
 			Attribute at = attributes.get(i);
 			argbuilder.append(typedefs.get(at.id.id)+ " " + at.var.id + ((i < attributes.size()-1) ? "," : ""));
 		}
 		emit("__device__ inline int _checkguard_"+def.id.id+"("+argbuilder+")\n{");
 		emit("return ");
 		def.exp.bexp.visit(this);
 		emit(":\n}\n");
 
 
 	}
 	public void CheckShape(OpDef def)
 	{
 
 		/*
 		 * Should emit something like __device__ int _checkshape_op([records])
 		 * { return ...}
 		 *
 		 *
 		 */
 		//emit highly unoptimized code, let someone else handle that noise
 		
 		//Categorize
 		List<Tuple> node_items = new ArrayList<Tuple>();
 		List<Tuple> edge_items = new ArrayList<Tuple>();
 		List<Attribute> attributes = new ArrayList<Attribute>();
 		for(Tuple t: def.exp.tuples){
 			for(Attribute at : t.attributes)
 				attributes.add(at);
 			switch(t.type) {
 			case NODES:
 				node_items.add(t);
 				break;
 			case EDGES:
 				edge_items.add(t);
 				break;
 			}
 		}
 		//python where are you my love :'(
 		StringBuilder argbuilder = new StringBuilder("");
 		for (int i=0; i<attributes.size();i++) {
 			Attribute at = attributes.get(i);
 			argbuilder.append(typedefs.get(at.id.id)+ " " + at.var.id + ((i < attributes.size()-1) ? "," : ""));
 		}
 		emit("__device__ inline int _checkshape_"+def.id.id+"("+argbuilder+")\n{");
 		for (int i=0;i<node_items.size();i++) {
 			for (int j=0;j<node_items.size();j++) {
 				if(i == j) continue;
 				String ni = get_prop_name(node_items.get(i),"Node");
 				String nj = get_prop_name(node_items.get(j),"Node");
 				emit("if("+nj+"=="+ni+") return FALSE;");
 			}
 		}
 		for (int i=0;i<edge_items.size();i++) {
 			//emit code checking Edge(src,dst)
 			String src = get_prop_name(edge_items.get(i),"src");
 			String dst = get_prop_name(edge_items.get(i),"dst");
 			emit("if(!Edge("+src+","+dst+")) return FALSE;");
 		}
 		emit("return TRUE;\n}\n");
 	}
 	private void emit(String s)
 	{
 		System.out.print(s);
 	}
 
 
 
 
 	//Util methods for finding the src,dst of edges
 	private String get_prop_name(Tuple t,String prop)
 	{
 		for(Attribute at : t.attributes)
 			if(at.var.id.equals(prop))
 				return at.id.id;
 		return "!!!!Error!!!!";
 	}
 
 	public void accept(BoolAnd exp)
 	{
 		exp.lhs.visit(this);
 		emit(" && ");
 		exp.rhs.visit(this);
 	}
 	public void accept(BoolOr exp)
 	{
 		exp.lhs.visit(this);
 		emit(" || ");
 		exp.rhs.visit(this);
 	}
 	public void accept(BoolEq exp)
 	{
 		exp.lhs.visit(this);
 		emit(" == ");
 		exp.rhs.visit(this);
 	}
 	public void accept(BoolIn exp)
 	{
 		emit("("+exp.id.id+".count(");
 		exp.exp.visit(this);
 		emit(") > 0)");
 	}
 	public void accept(ast.Set exp)
 	{
 		//TODO
 	}
 	public void accept(Empty exp)
 	{
 		//TODO
 	}
 	public void accept(IntConst exp)
 	{
 		emit(""+exp.value);
 	}
 	public void accept(Var exp)
 	{
 		emit(exp.name.id);
 	}
 	public void accept(LessThan exp)
 	{
 		exp.lhs.visit(this);
 		emit(" < ");
 		exp.rhs.visit(this);
 	}
 	public void accept(True exp)
 	{
 		emit("TRUE");
 	}
 	public void accept(False exp)
 	{
 		emit("FALSE");
 	}
 	public void accept(Times exp)
 	{
 		exp.lhs.visit(this);
 		emit(" * ");
 		exp.rhs.visit(this);
 	}
 	public void accept(Plus exp)
 	{
 		exp.lhs.visit(this);
 		emit(" + ");
 		exp.rhs.visit(this);
 	}
 	public void accept(Minus exp)
 	{
 		exp.lhs.visit(this);
 		emit(" - ");
 		exp.rhs.visit(this);
 	}
 	public void accept(Div exp)
 	{
 		exp.lhs.visit(this);
 		emit(" / ");
 		exp.rhs.visit(this);
 	}
 	public void accept(Not exp)
 	{
 		emit("!");
 		exp.exp.visit(this);
 	}
 	public void accept(Intersection exp)
 	{
 		//TODO:Make sure out Set class supports & for intersection
 		exp.lhs.visit(this);
 		emit(" & ");
 		exp.rhs.visit(this);
 	}
 	public void accept(Union exp)
 	{
 		//TODO:Make sure out Set class supports | for union
 		exp.lhs.visit(this);
 		emit(" | ");
 		exp.rhs.visit(this);
 	}
 	public void accept(If exp)
 	{
 		emit("(");
 		exp.condition.visit(this);
 		emit("?");
 		exp.tcase.visit(this);
 		emit(":");
 		exp.fcase.visit(this);
 		emit(")");
 	}
 	public void accept(Tuple t)
 	{
 		//shouldn't happen
 	}
 	public void accept(SetType s)
 	{
 		//TODO
 	}
 	public void accept(BaseType b)
 	{
 		//TODO
 	}
 	public void accept(ForEach f)
 	{
 		//TODO
 	}
 	public void accept(Iterate f)
 	{
 		//TODO
 	}
 	public void accept(For f)
 	{
 		//TODO
 	}
 	public void accept(AcidStatement a)
 	{
 		//TODO
 	}
 	public void accept(Identifier id)
 	{
 		//TODO (might not be needed anywhere)
 	}
 	public void accept(AttributeDef def)
 	{
 		//TODO
 	}
 	public void accept(ActionDef def)
 	{
 		//TODO
 	}
 	public void accept(Attribute att)
 	{
 		//TODO
 	}
 	public void accept(Assignment assign)
 	{
 		emit(assign.id.id +" = ");
 		assign.exp.visit(this);
 		emit(";");
 	}
 	public void accept(Global global)
 	{
 		global.type.visit(this);
 		emit(" "+global.name.id+";");
 	}
 	public void accept(SchedExp exp)
 	{
 		//TODO
 	}
 	public void accept(JoinStatement stm)
 	{
 		stm.s1.visit(this);
 		stm.s2.visit(this);
 	}
 
 
 }
