 import java.io.*;
 import java.util.*;
 
 public class ExprStack {
 
 	//variable name stack
     private static Stack<String> stack = new Stack<String>();
 	//if and dowhile jump logic
     private static Stack<String> labelstack = new Stack<String>();
     private static Stack<String> dowhilestack = new Stack<String>();
 	//function stack
 	private static Stack<String> functionstack = new Stack<String>();
 	//tiny node list
 	private static ArrayList<TinyNode> tinylist = new ArrayList<TinyNode>();
 	//counters
     private static int registercount = 1;
     private static int tinyregcount = 1;
     private static int labelcount = 1;
     private static int tinylabelcount = 1;
 	private static int argcounter = 0;
 	//if and dowhile labels
 	private static String endiflabel = "";
 	private static String dowhilelabel = "";
 
 	////////////////////////
 	//
 	//	globalPush
 	//
 	//
 	////////////////////////
 	public static void globalPush() {
 	
 		functionstack.push("");
 		TinyNode tnode1 = new TinyNode("push", "", "");
 		tinylist.add(tnode1);
 		functionstack.push("");
 		TinyNode tnode2 = new TinyNode("push", "", "r0");
 		tinylist.add(tnode2);
 		functionstack.push("");
 		TinyNode tnode3 = new TinyNode("push", "", "r1");
 		tinylist.add(tnode3);
 		functionstack.push("");
 		TinyNode tnode4 = new TinyNode("push", "", "r2");
 		tinylist.add(tnode4);
 		functionstack.push("");
 		TinyNode tnode5 = new TinyNode("push", "", "r3");
 		tinylist.add(tnode5);
 	}
 	
 	////////////////////////
 	//
 	//	functionLabel
 	//
 	//
 	////////////////////////
 	public static void functionLabel(String id, String parameters_list) {
 	
 		//IR
 		IRNode irnode = new IRNode("FUNCTION", id, "", parameters_list);
 		irnode.printNode();
 		
 		//Tiny
 		TinyNode tnode1 = new TinyNode("jsr ", "", "main");		
 		tinylist.add(tnode1);
 		TinyNode tnode2 = new TinyNode("sys ", "", "halt");		
 		tinylist.add(tnode2);		
 		TinyNode tnode3 = new TinyNode("label ", "", id);		
 		tinylist.add(tnode3);		
 		TinyNode tnode4 = new TinyNode("link ", "", "5");		
 		tinylist.add(tnode4);
 	}
 
 	////////////////////////
 	//
 	//	functionEnd
 	//
 	//
 	////////////////////////
 	public static void functionEnd() {
 	
 		TinyNode tnode = new TinyNode("end", "", "");		
 		tinylist.add(tnode);
 	}
 
 	////////////////////////
 	//
 	//	functionPush
 	//
 	//
 	////////////////////////
 	public static void functionPush(String expression) {
 	
 		functionstack.push(expression);
 		TinyNode tnode2 = new TinyNode("push", "", expression);
 		tinylist.add(tnode2);
 		argcounter++;
 	}
 
 	////////////////////////
 	//
 	//	functionRP
 	//
 	//
 	////////////////////////
 	public static void functionRP() {
 	
 		functionstack.push("");
 		TinyNode tnode = new TinyNode("push", "", "");
 		tinylist.add(tnode);
 	}
 	
 	////////////////////////
 	//
 	//	functionCall
 	//
 	//
 	////////////////////////
 	public static void functionCall(String id) {
 	
 		
 		functionstack.push("");
 		TinyNode tnode8 = new TinyNode("push", "", "r0");
 		tinylist.add(tnode8);
 		functionstack.push("");
 		TinyNode tnode7 = new TinyNode("push", "", "r1");
 		tinylist.add(tnode7);
 		functionstack.push("");
 		TinyNode tnode6 = new TinyNode("push", "", "r2");
 		tinylist.add(tnode6);
 		functionstack.push("");
 		TinyNode tnode5 = new TinyNode("push", "", "r3");
 		tinylist.add(tnode5);
 		TinyNode tnode = new TinyNode("jsr", "", id);
 		tinylist.add(tnode);
 		TinyNode tnode1 = new TinyNode("pop", "", "r3");
 		tinylist.add(tnode1);
 		TinyNode tnode2 = new TinyNode("pop", "", "r2");
 		tinylist.add(tnode2);
 		TinyNode tnode3 = new TinyNode("pop", "", "r1");
 		tinylist.add(tnode3);
 		TinyNode tnode4 = new TinyNode("pop", "", "r0");
 		tinylist.add(tnode4);
 		TinyNode tnode9 = new TinyNode("pop", "", "");
 		for (int i=0;i<argcounter;argcounter--) {
 			tinylist.add(tnode9);
 		}
 		//create new register
 		String t_res = newTinyReg();
 		
 		//pop return value		
 		TinyNode tnode10 = new TinyNode("pop", "", t_res);
 		tinylist.add(tnode10);
 
 		stack.push(t_res);
 		
 	}
 
 	////////////////////////
 	//
 	//	functionReturn
 	//
 	//
 	////////////////////////
 	public static void functionReturn(String id) {
 	
 		TinyNode tnode1 = new TinyNode("unlnk", "", "");
 		tinylist.add(tnode1);
 		TinyNode tnode2 = new TinyNode("ret", "", "");
 		tinylist.add(tnode2);
 	}
 
 	////////////////////////
 	//
 	//	functionPop
 	//
 	//
 	////////////////////////
 	public static void functionPop(String ret) {
 	
 		functionstack.pop();
 		TinyNode tnode1 = new TinyNode("pop", "", "");		
 		tinylist.add(tnode1);
 		ret = functionstack.pop();
 		TinyNode tnode2 = new TinyNode("pop", "", ret);		
 		tinylist.add(tnode2);
 	}
 
 	////////////////////////
 	//
 	//	addOperator
 	//
 	//
 	////////////////////////
     public static void addOperator(String op) {
 
 		String res = newRegister();
 		String t_res = newTinyReg();
 
 		String op2 = stack.pop();
 		String op1 = stack.pop();
 		stack.push(res);
 
 		String optype = "I";
 		String tiny_ot = "i";
 		String instruction = "";
 		String tiny_instr = "";
 		
 		//replace $T with r
 		op1 = op1.replace("$T","r");
 		op2 = op2.replace("$T","r");
 
 		//lower number by 1
 		if (op1.contains("r")) {
 		
 			op1 = "r" + Integer.toString(op1.compareTo("r0")-1);
 		}
 		if (op2.contains("r")) {
 		
 			op2 = "r" + Integer.toString(op2.compareTo("r0")-1);
 		}
 		
 		if (op.equals("+")) {
 
 			instruction = "ADD"; 
 			tiny_instr	= "add";
 		}
 
 		else if (op.equals("-")) {
 
 			instruction = "SUB";
 			tiny_instr	= "sub"; 
 		}
 
 		else if (op.equals("*")) {
 
 			instruction = "MULT";
 			tiny_instr	= "mul"; 
 		}
 
 		else if (op.equals("/")) {
 
 			instruction = "DIV";
 			tiny_instr	= "div"; 
 		}	
 
 		
 
 		IRNode irnode = new IRNode(instruction+optype, op1, op2, res);
 		irnode.printNode();
 		TinyNode t1node = new TinyNode("move", op1, t_res);
 		tinylist.add(t1node);		
 		//t1node.printNode();
 		TinyNode t2node = new TinyNode(tiny_instr+tiny_ot, op2, t_res);
 		tinylist.add(t2node);		
 		//t2node.printNode();
     }
 
 	////////////////////////
 	//
 	//	evaluateExpr
 	//
 	//
 	////////////////////////
     public static void evaluateExpr() {
 
 		String res = stack.pop();
 		String op1 = stack.pop();

		if (op1.contains("$T")) {		
			//replace $T with r
			op1 = op1.replace("$T","r");
 		
			//lower number by 1
			if (op1.contains("r")) {
 		
				op1 = "r" + Integer.toString(op1.compareTo("r0")-1);
			}

		} else {
			op1 = "r" + Integer.toString(op1.compareTo("r0"));
 		}
 		String optype = "I";
 		IRNode irnode = new IRNode("STORE"+optype, op1, "", res);
 		irnode.printNode();
 		TinyNode tnode = new TinyNode("move", op1, res);
 		tinylist.add(tnode);		
 		//tnode.printNode();
     }
 
 	////////////////////////
 	//
 	//	evaluateWrite
 	//
 	//
 	////////////////////////
 	public static void evaluateWrite(String id_list, String type) { //need to change back to only id_list input
 
 		String l_type = "";
 		//loop through args
 		String[] ids = id_list.split(",");
 		for (int i=0;i<ids.length;i++) {
 			//find type
 			if (type.contains("STRING")) {
 				l_type = "s";
 			}
 			if (type.contains("INT")) {
 				l_type = "i";
 			}
 			if (type.contains("FLOAT")) {
 				l_type = "r";
 			}
 			//create node
 			IRNode irnode = new IRNode("WRITE"+l_type, "", "", ids[i]);
 			irnode.printNode();
 			TinyNode tnode = new TinyNode("sys", "write"+l_type, ids[i]);
 			tinylist.add(tnode);
 		}		
     }
 	
 	////////////////////////
 	//
 	//	evaluateRead
 	//
 	//
 	////////////////////////
 	public static void evaluateRead(String id_list, String type) {
 
 		String l_type = "";
 		//loop through args
 		String[] ids = id_list.split(",");
 		for (int i=0;i<ids.length;i++) {
 			//find type
 			if (type.contains("STRING")) {
 				l_type = "s";
 			}
 			if (type.contains("INT")) {
 				l_type = "i";
 			}
 			if (type.contains("FLOAT")) {
 				l_type = "r";
 			}
 			//create node
 			IRNode irnode = new IRNode("READ"+l_type, "", "", ids[i]);
 			irnode.printNode();
 			TinyNode tnode = new TinyNode("sys", "read"+l_type, ids[i]);
 			tinylist.add(tnode);
 		}		
     }
 
 	////////////////////////
 	//
 	//	evaluateIf
 	//
 	//
 	////////////////////////
 	public static void evaluateIf(String cond) {
 
 		String label = newLabel();
 		String t_label = newTinyLabel();
 		
 		String t_res = newTinyReg();
 	
 		String compop = "";
 		String lhe = "";
 		String rhe = "";
 	
 		if (cond.contains("TRUE")) {
 			lhe = "0";
 			rhe = "0";
 			compop = "jne";
 		}
 	
 		if (cond.contains("!=")) {
 			
 			//split into left and right hand expressions
 			lhe = cond.split("!=")[0];
 			rhe = cond.split("!=")[1];
 			compop = "jeq";
 			
 		} else if (cond.contains(">=")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split(">=")[0];
 			rhe = cond.split(">=")[1];
 			compop = "jlt";
 
 		} else if (cond.contains("<=")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split("<=")[0];
 			rhe = cond.split("<=")[1];
 			compop = "jgt";
 		
 		} else if (cond.contains(">")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split(">")[0]; 
 			rhe = cond.split(">")[1]; 
 			compop = "jlt";
 
 		} else if (cond.contains("<")) {
 	
 			//split into left and right hand expressions
 			lhe = cond.split("<")[0]; 
 			rhe = cond.split("<")[1]; 
 			compop = "jgt";
 
 		} else if (cond.contains("=")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split("=")[0];
 			rhe = cond.split("=")[1];
 			compop = "jne";
 
 		}
 
 		//push if-statement label onto stack
 		
 
 		//add label node for endif
 		TinyNode tnode = new TinyNode("label", "", t_label);
 		labelstack.push(t_label);
 		//tinylist.add(tnode);
 
 		//add mov node(s)
 		TinyNode t1node = new TinyNode("move", rhe, t_res);
 		tinylist.add(t1node);
 
 		//add cmpi node
 		TinyNode t2node = new TinyNode("cmpi", lhe, t_res);
 		tinylist.add(t2node);
 
 		//add jne node
 		TinyNode t3node = new TinyNode(compop, "", "label"+tinylabelcount);
 		tinylist.add(t3node);
 		
 		
 	}
 
 	////////////////////////
 	//
 	//	evaluateElseIf
 	//
 	//
 	////////////////////////
 	public static void evaluateElseIf(String cond) {
 
 		String label = newLabel();
 		String t_label = newTinyLabel();
 
 		String t_res = newTinyReg();
 
 		String compop = "";
 			
 		String lhe = "";
 		String rhe = "";
 
 		
 		endiflabel = labelstack.pop();
 		
 		labelstack.push(endiflabel);
 
 		TinyNode tnode0 = new TinyNode("jmp", "", endiflabel);
 		tinylist.add(tnode0);
 
 
 		if (cond.contains("TRUE")) {
 			lhe = "0";
 			rhe = "0";
 			compop = "jne";
 		}
 		
 		if (cond.contains("!=")) {
 			
 			//split into left and right hand expressions
 			lhe = cond.split("!=")[0];
 			rhe = cond.split("!=")[1];
 			compop = "jeq";
 			
 		} else if (cond.contains(">=")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split(">=")[0];
 			rhe = cond.split(">=")[1];
 			compop = "jlt";
 
 		} else if (cond.contains("<=")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split("<=")[0];
 			rhe = cond.split("<=")[1];
 			compop = "jgt";
 		
 		} else if (cond.contains(">")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split(">")[0];
 			rhe = cond.split(">")[1]; 
 			compop = "jlt";
 
 		} else if (cond.contains("<")) {
 	
 			//split into left and right hand expressions
 			lhe = cond.split("<")[0]; 
 			rhe = cond.split("<")[1]; 
 			compop = "jgt";
 
 		} else if (cond.contains("=")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split("=")[0];
 			rhe = cond.split("=")[1];
 			compop = "jne";
 
 		}
 
 		//add label node
 		IRNode irnode = new IRNode("LABEL", "", "", label);
 		irnode.printNode();
 		TinyNode tnode = new TinyNode("label", "", t_label);
 		tinylist.add(tnode);
 
 		//add mov node(s)
 		TinyNode t1node = new TinyNode("move", rhe, t_res);
 		tinylist.add(t1node);
 		
 		//add cmpi node
 		TinyNode t2node = new TinyNode("cmpi", lhe, t_res);
 		tinylist.add(t2node);
 
 		//add compop node
 		TinyNode t3node = new TinyNode(compop, "", "label"+tinylabelcount);
 		tinylist.add(t3node);
 		
 	}
 
 	////////////////////////
 	//
 	//	labelEndIf
 	//
 	//
 	////////////////////////
 	public static void labelEndIf() {
 		
 		String label = newLabel();
 		String t_label = newTinyLabel();
 		
 		endiflabel = labelstack.pop();
 
 		//add label node
 		IRNode irnode = new IRNode("LABEL", "", "", label);
 		irnode.printNode();
 		TinyNode tnode1 = new TinyNode("label", "", t_label);
 		tinylist.add(tnode1);
 		TinyNode tnode2 = new TinyNode("label", "", endiflabel);
 		tinylist.add(tnode2);
 
 	}
 
 	////////////////////////
 	//
 	//	labelDoWhile
 	//
 	//
 	////////////////////////
 	public static void labelDoWhile() {
 
 		String label = newLabel();
 		String t_label = newTinyLabel();
 
 		//add dowhile label to stack
 		dowhilestack.push(t_label);
 
 		//add label node
 		IRNode irnode = new IRNode("LABEL", "", "", label);
 		irnode.printNode();
 		TinyNode tnode = new TinyNode("label", "", t_label);
 		tinylist.add(tnode);
 
 		
 		
 	}
 
 	////////////////////////
 	//
 	//	evaluateDoWhile
 	//
 	//
 	////////////////////////
 	public static void evaluateDoWhile(String cond) {
 
 		String t_res = newTinyReg();
 		String compop = "";
 			
 		String lhe = "";
 		String rhe = "";
 		
 		dowhilelabel = dowhilestack.pop();
 		
 
 
 		//dowhile equalities work in the intuitive way
 
 		if (cond.contains("!=")) {
 			
 			//split into left and right hand expressions
 			lhe = cond.split("!=")[0];
 			rhe = cond.split("!=")[1];
 			compop = "jne";
 			
 		} else if (cond.contains(">=")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split(">=")[0];
 			rhe = cond.split(">=")[1];
 			compop = "jgt";
 
 		} else if (cond.contains("<=")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split("<=")[0];
 			rhe = cond.split("<=")[1];
 			compop = "jlt";
 		
 		} else if (cond.contains(">")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split(">")[0];
 			rhe = cond.split(">")[1];
 			compop = "jgt";
 
 		} else if (cond.contains("<")) {
 	
 			//split into left and right hand expressions
 			lhe = cond.split("<")[0];
 			rhe = cond.split("<")[1];
 			compop = "jlt";
 
 		} else if (cond.contains("=")) {
 
 			//split into left and right hand expressions
 			lhe = cond.split("=")[0];
 			rhe = cond.split("=")[1];
 			compop = "jeq";
 
 		}
 
 		//add mov node(s)
 		TinyNode t1node = new TinyNode("move", rhe, t_res);
 		tinylist.add(t1node);
 		
 		//add cmpi node
 		TinyNode t2node = new TinyNode("cmpi", lhe, t_res);
 		tinylist.add(t2node);
 
 		//add jump node
 		TinyNode t3node = new TinyNode(compop, "", dowhilelabel);
 		tinylist.add(t3node);
 		
 		
 	}
 
 	////////////////////////
 	//
 	//	addLiteral
 	//
 	//
 	////////////////////////
     public static void addLiteral(String lit) {
 		
 		String op1 = lit;
 		String res = newRegister();
 		String t_res = newTinyReg();
 
 		//replace $T with r
 		op1 = op1.replace("$T","r");
 		
 		//lower number by 1
 		if (op1.contains("r")) {
 		
 			op1 = "r" + Integer.toString(op1.compareTo("r0")-1);
 		}
 
 		String optype = "I";
 
 		IRNode irnode = new IRNode("STORE"+optype, op1, "", res); 
 		irnode.printNode();
 		TinyNode tnode = new TinyNode("move", op1, res); 
 		tinylist.add(tnode);		
 		//tnode.printNode();
 		stack.push(res);
     }
 	
 	////////////////////////
 	//
 	//	printTinyList
 	//
 	//
 	////////////////////////
 	public static void printTinyList(){
 
 		for (int i = 0; i < tinylist.size(); i++) {
 
 			//check the indexes with the list, and print
 			TinyNode node = tinylist.get(i);
 			node.printNode();
 		}
 		System.out.println("sys halt");
     }
 
 	////////////////////////
 	//
 	//	addRIdentifier
 	//
 	//
 	////////////////////////
     public static void addRIdentifier(String id) {
 		
 		stack.push(id);
     }
 
 	////////////////////////
 	//
 	//	addLIdentifier
 	//
 	//
 	////////////////////////
     public static void addLIdentifier(String id) {
 		
 		stack.push(id);
     }
 
 	////////////////////////
 	//
 	//	newRegister
 	//
 	//
 	////////////////////////
     public static String newRegister() {
 
 		return "$T"+Integer.toString(registercount++);
     }
 
 	////////////////////////
 	//
 	//	newTinyReg
 	//
 	//
 	////////////////////////
     public static String newTinyReg() {
 
 		return "r"+Integer.toString((tinyregcount++)-1);
     }
 
 	////////////////////////
 	//
 	//	newLabel
 	//
 	//
 	////////////////////////
 	 public static String newLabel() {
 
 		return "label"+Integer.toString(labelcount++);
     }
 
 	////////////////////////
 	//
 	//	newTinyLabel
 	//
 	//
 	////////////////////////
     public static String newTinyLabel() {
 
 		return "label"+Integer.toString(tinylabelcount++);
     }
 	
 }
