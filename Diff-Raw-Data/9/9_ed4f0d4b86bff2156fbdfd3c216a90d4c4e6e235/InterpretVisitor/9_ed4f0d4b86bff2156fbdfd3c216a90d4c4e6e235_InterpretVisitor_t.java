 package Interpreter;
 import viz.*;
 import java.util.*;
 
 public class InterpretVisitor implements VizParserVisitor, VizParserTreeConstants, UpdateReasons
 {
 	private QuestionFactory questionFactory;
 	private XAALConnector connector;
 	public static final int LINE_NUMBER_END = -1;
 
 	public void setQuestionFactory(QuestionFactory questionFactory)
 	{
 		this.questionFactory = questionFactory;
 	}
 	
 	public void setXAALConnector(XAALConnector xc)
 	{
 		this.connector = xc;
 	}
 
 	public void update(int lineNumber, int reason)
 	{
 		System.out.println("Update on " + lineNumber);
 		//System.out.println(Global.getCurrentSymbolTable().toString());
 		questionFactory.addQuestion(lineNumber, reason);
 	}
 	public Object visit(SimpleNode node, Object data)
 	{
 		int id = node.getId();
 		Object retVal = null;
 
 		switch (id)
 		{
 			case JJTPROGRAM:
 				handleProgram((ASTProgram)node);
 				break;
 			case JJTDECLARATIONLIST:
 				handleDeclarationList((ASTDeclarationList)node);
 				break;
 			case JJTDECLARATION:
 				retVal = handleDeclaration((ASTDeclaration)node);
 				break;
 			case JJTVARDECL:
 				handleVarDecl((ASTVarDecl)node);
 				break;
 			case JJTARRAYDECLARATION:
 				handleArrayDeclaration((ASTArrayDeclaration)node);
 				break;
 			case JJTSTATEMENTLIST:
 				handleStatementList((ASTStatementList)node);
 				break;
 			case JJTSTATEMENT:
 				handleStatement((ASTStatement)node);
 				break;
 			case JJTCALL:
 				retVal = handleCall((ASTCall)node);
 				break;
 			case JJTVAR:
 				retVal = handleVar((ASTVar)node);
 				break;
 			case JJTASSIGNMENT:
 				handleAssignment((ASTAssignment)node);
 				break;
 			case JJTEXPRESSION:
 				retVal = handleExpression((ASTExpression)node);
 				break;
 			case JJTARGS:
 				retVal = handleArgs((ASTArgs)node);
 				break;
 			case JJTOP:
 				retVal = handleOp((ASTOp)node);
 				break;
 			case JJTNUM:
 				retVal = handleNum((ASTNum)node);
 				break;
 			case JJTFUNCTION:
 				handleFunction((ASTFunction)node);
 				break;
 			default:
 				System.out.println("Unimplemented");
 		}
 		return retVal;
 	}
 	
 	public void handleProgram(ASTProgram node)
 	{
 		//System.out.println("visiting program");
 		Global.setCurrentSymbolTable(Global.getSymbolTable()); //set current symbol table to the global one
 		update(1, UPDATE_REASON_BEGIN);
 		
 		//Drawing Stuff
 		connector.addScope(Global.getSymbolTable(), "Global", null);
 		
 		node.jjtGetChild(0).jjtAccept(this, null);
 		update(LINE_NUMBER_END, UPDATE_REASON_END);
 		System.out.println("Done");
 	}
 	
 	public void handleDeclarationList(ASTDeclarationList node)
 	{
 		//System.out.println("Visiting declList");
 		int numDecls = node.jjtGetNumChildren();
 		for (int i = 0; i < numDecls; i++)
 		{
 			//A Declaration returned false which means we ran.
 			if(((Boolean)node.jjtGetChild(i).jjtAccept(this, null)) == false)
 			{
 				return;
 			}
 		}
 	}
 	
 	//returning false means we're done executing now.
 	public Boolean handleDeclaration(ASTDeclaration node)
 	{
 		//System.out.println("Visiting decl");
 		SimpleNode child = (SimpleNode) node.jjtGetChild(0);
 		if (child.getId() == JJTFUNCTION)
 		{
 			ASTFunction main = Global.getFunction("main");
 			
			connector.startSnap(Global.getFunction("main").getLineNumber());
 			connector.startPar();
 				connector.showScope("main");
 			connector.endPar();
 			connector.endSnap();
 			main.jjtAccept(this, null);
 			return false;
 		}
 		else
 		{
 			node.jjtGetChild(0).jjtAccept(this, null);
 			//update();
 			return true;
 		}	
 		
 	}
 	
 	public void handleVarDecl(ASTVarDecl node)
 	{
 		//System.out.println("Visiting var decl");
 		String name = node.getName();
 		node.setLineNumber(((SimpleNode)node.jjtGetParent()).getLineNumber());
 		if (node.getIsArray())
 		{
 			//FIXME
 			System.out.println("Array declaration unimplemented");
 		}
 		else
 		{
 			Integer value =(Integer) node.jjtGetChild(0).jjtAccept(this, null);
 			SymbolTable s = Global.getCurrentSymbolTable();
 			s.setValue(name, value);
 			
 			//Drawing Stuff
 			connector.addVariable(s.getVariable(name), name, s.getName());
 			
 			//If we're not in Global, this should be a snapshot
 			if (Global.getCurrentSymbolTable() != Global.getSymbolTable())
 			{
 				System.out.println("Adding a varDecl not in global");
 				connector.startSnap(node.getLineNumber());
 					connector.startPar();
						connector.showVar(Global.getCurrentSymbolTable().getVariable(name));
 					connector.endPar();
 				connector.endSnap();
 			}
 			
 		}	
 	}
 	
 	public void handleFunction(ASTFunction node)
 	{	
 		//Get the function's symbol table, set it's previous to the
 		// calling function's, and then set it to current.
 		
 		
 		SymbolTable currentSymbolTable = node.getSymbolTable();
 		for (String p : node.getParameters())
 		{
			ByValVariable v = new ByValVariable(-255);
			v.setParam();
			currentSymbolTable.put(p, v);
 		}
 		currentSymbolTable.setPrevious(Global.getCurrentSymbolTable());
 		Global.setCurrentSymbolTable(currentSymbolTable);
 
 		//Drawing Stuff:
 		connector.addScope(currentSymbolTable, currentSymbolTable.getName(), "Global");
 		System.out.println("Added scope " + currentSymbolTable.getName());
 		//Drawing the actually running
 		connector.startSnap(node.getLineNumber());
 			connector.startPar();
 			HashMap<String, Variable> localVars = currentSymbolTable.getLocalVariables();
 			for (String key : localVars.keySet())
 			{
 				connector.showVar(localVars.get(key));
 			}
 			connector.endPar();
 		connector.endSnap();
 			
 		
 		System.out.println("Executing function: " + node.getName());
 		update(node.getLineNumber(), UPDATE_REASON_FUNCTION);
 		node.jjtGetChild(0).jjtAccept(this, null);
 		leaveScope();
 	}
 	public void handleArrayDeclaration(ASTArrayDeclaration node)
 	{
 	
 	}
 	
 	public void handleStatementList(ASTStatementList node)
 	{
 		int numStatements = node.jjtGetNumChildren();
 		for (int i = 0; i < numStatements; i++)
 		{
 			node.jjtGetChild(i).jjtAccept(this, null);
 		}
 	}
 	
 	public void handleStatement(ASTStatement node)
 	{
 		node.jjtGetChild(0).jjtAccept(this, null);
 		if (((SimpleNode)node.jjtGetChild(0)).getId() == JJTCALL)
 		{
 			((ASTCall)(node.jjtGetChild(0))).setLineNumber(node.getLineNumber());
 		}
 		//System.out.println(node.getCode());
 		
 		//Drawing
 		connector.startSnap(node.getLineNumber());
 			connector.startPar();
 			connector.endPar();
 		connector.endSnap();
 		update(node.getLineNumber(), UPDATE_REASON_STATEMENT);
 	}
 	
 	public Integer handleCall(ASTCall node)
 	{
 		//Get the correct function head node
 		ASTFunction fun = Global.getFunction(node.getName());
 		System.out.println("Calling: " + fun.getName());
 		//Get the parameters and put the correct values in the symbolTable
 		SymbolTable st = fun.getSymbolTable();
 		ArrayList<String> parameters = fun.getParameters();
 		ArrayList<Integer> args = (ArrayList<Integer>) node.jjtGetChild(0).jjtAccept(this, null);
 		for (int i = 0; i < args.size(); i++)
 		{
 			st.setValue(parameters.get(i), args.get(i));
 		}
 		
 		//Drawing Stuff
 		connector.startSnap(node.getLineNumber());
 			connector.startPar();
 			
 			connector.showScope(node.getName());
 			HashMap<String, Variable> localVars = Global.getCurrentSymbolTable().getLocalVariables();
 			for (String key : localVars.keySet())
 			{
 				connector.showVar(localVars.get(key));
 			}
 			
 			connector.endPar();
 		connector.endSnap();
 				
 		fun.jjtAccept(this, null);
 		return 0;
 	}
 	
 	public Integer handleVar(ASTVar node)
 	{
 		if (node.getIsArray())
 		{
 			System.out.println(" Array not implemented");
 			return 99;
 		}
 		return Global.getCurrentSymbolTable().get(node.getName());
 	}
 	
 	public void handleAssignment(ASTAssignment node)
 	{
 		String name = node.getName();
 		Integer value = (Integer)node.jjtGetChild(1).jjtAccept(this, null);
 
 		Global.getCurrentSymbolTable().setValue(name, value);
 	}
 	
 	public Integer handleExpression(ASTExpression node)
 	{
  		Integer value = (Integer) node.jjtGetChild(0).jjtAccept(this, null);
  		return value;
  	}
  	
  	public ArrayList<Integer> handleArgs(ASTArgs node)
  	{
  		ArrayList<Integer> args = new ArrayList<Integer>();
  		int numArgs = node.jjtGetNumChildren();
  		for (int i = 0; i < numArgs; i++)
  		{
  			args.add((Integer)node.jjtGetChild(i).jjtAccept(this, null));
  		}
  		return args;
  	}
 	
 	public Integer handleOp(ASTOp node)
 	{
 		if (node.getOp().equals("+"))
 		{
 			return (Integer)node.jjtGetChild(0).jjtAccept(this, null) +
 				(Integer)node.jjtGetChild(1).jjtAccept(this, null);
 		}
 		else if (node.getOp().equals("-"))
 		{
 			return (Integer)node.jjtGetChild(0).jjtAccept(this, null) -
 				(Integer)node.jjtGetChild(1).jjtAccept(this, null);
 		}
 		return 0;
 	}
 	
 	public Integer handleNum(ASTNum node)
 	{
 		return node.getValue();
 	}
 	public Object visit(ASTProgram node, Object data)
 	{
 		handleProgram(node);
 		return null;
 	}
 	
 	public Object visit(ASTDeclarationList node, Object data)
 	{
 		handleDeclarationList(node);
 		return null;
 	}
 	
 	public Object visit(ASTDeclaration node, Object data)
 	{
 		handleDeclaration(node);
 		return null;
 	}
 	
 	public Object visit(ASTVarDecl node, Object data)
 	{
 		handleVarDecl(node);
 		return null;
 	}
 	
 	//FIXME
 	public Object visit(ASTArrayDeclaration node, Object data)
 	{
 		handleArrayDeclaration(node);
 		return null;
 	}
 	
 	public Object visit(ASTFunction node, Object data)
 	{	
 		handleFunction(node);
 		return null;
 	}
 	
 	public Object visit(ASTStatementList node, Object data)
 	{
 		handleStatementList(node);
 		return null;
 	}
   	public Object visit(ASTStatement node, Object data)
   	{
   		handleStatement(node);
   		return null;
   	}
   	public Object visit(ASTCall node, Object data)
   	{
   		return handleCall(node);
   	}
   	public Object visit(ASTVar node, Object data)
   	{
   		return handleVar(node);
   	}
   	public Object visit(ASTAssignment node, Object data)
   	{
   		handleAssignment(node);
   		return null;
   	}
  	public Object visit(ASTExpression node, Object data)
  	{
 		return handleExpression(node);
  	}
   	public Object visit(ASTArgs node, Object data)
   	{
   		return handleArgs(node);
   	}
   	public Object visit(ASTOp node, Object data)
   	{
   		return handleOp(node);
   	}
   	public Object visit(ASTNum node, Object data)
   	{
   		return handleNum(node);
   	}
   	
   	public void leaveScope()
   	{
   		System.out.println("Leaving scope " + Global.getCurrentSymbolTable().getName());
   		Global.setCurrentSymbolTable(Global.getCurrentSymbolTable().getPrevious());
   	}
 }
