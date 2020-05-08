 package rules;
 
 import parser.Token;
 
 public class AgrFunction extends Function {
 
 	public AgrFunction(Token name, Term_List arguments) {
 		super(name, arguments);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public String forAgrFunction(Predicate head,Literal_list body){
 		String auxHead = "";
 		auxHead = head.getName().evaluate()+"_"+this.getToken().toString();
 		Term_List fun_arg = this.getTerm_List();
 		auxHead += "(Self_,"+ fun_arg.elementAt(0).evaluate()+ ",Now_)";
 		String result = auxHead + " :- node(Self_),time(Now_)";
 		result += body.toString(1)+","+ body.elementAt(body.size()-1).simpleEvaluate()+".";
 		result += head.getName().evaluate();
 		result += "(Self_," + this.evaluate() + ",Now_)";
 		result += " :- node(Self_),time(Now_),"+this.evaluate()+ " := #"; 
 		result += this.getToken().toString();
 		result += (this.getToken().toString().equals("count")) ? " { " : " [ ";
 		result += auxHead;
 		result += (this.getToken().toString().equals("count")) ? " }." : " ].";
 		return result;
 	}
 	
 	@Override
 	public String evaluate() {
 		// TODO Auto-generated method stub
 		String val = name.toString();
 		String newVal = Character.toUpperCase(val.charAt(0))+val.substring(1);
 		String arg = arguments.toString();
 		newVal +=  arg.substring(0, arg.length()-1);
 		return newVal;
 	}
 }
