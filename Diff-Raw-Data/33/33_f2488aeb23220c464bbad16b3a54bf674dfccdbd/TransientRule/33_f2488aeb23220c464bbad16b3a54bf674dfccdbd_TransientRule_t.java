 package rules;
 
 import java.util.Vector;
 
 public class TransientRule extends Rule{
 
 	public TransientRule(Predicate _head, Literal_list _body) {
 		super(_head, _body);
 
 	}
 
 	
 	public String evaluate() {
 		Vector<TransientRule> aux = this.checkValidity();
 		String output = "";
		if(head.testforAgrFunction() != -1)
 			if(body.size()>1)
				output = head.getArgFunction(head.testforAgrFunction()).forAgrFunction(head,body);
 			else
 				output = "Error one argument at the body. The type of the variable should be given";
 		else{
 			if(body.curent_host() != -1){
 				Current_host host = (Current_host) body.elementAt(body.curent_host());
 				Variable var = host.getVariable();
 				head.changeVariable(var, head.containsVarIndex(var));
 				body.removeAt(body.curent_host());
 			}
 			
 			for(int i = 0; i < aux.size(); i++) { 
 				output += aux.elementAt(i).evaluate();
 			}
 			
 			output += head.evaluate(false,true);
 			output += " :- node(Self_),time(Now_)";
 			output += body.toString();
 			output += ".";
 		}
 		return output;
 	}
 }
