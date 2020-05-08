 package Interpreter;
 
 import java.util.*;
 
 public class RandomizingVisitor implements VizParserVisitor, VizParserTreeConstants {
 
 	
 	String[] possVars = {"g","m","n", "v", "w"};
 	
 	final double chanceOfNumToVar = 1.0/10.0;
 	final double chanceOfAssignToOp = 1.5/10.0;
 	final double chanceOfPlusToMinus = 1.0/2.0;
 	final double chanceOfArrayDecl = 1.0/1.0;
 	final double chanceOfArrayToVarOp = 1.5/10.0;
 	
 	final int largestPossibleRandomInt = 5;
 	
 	
 	final int maxVarDeclsInGlobal = 3;
 	final int minVarDeclsInGlobal = 1;
 	
 	
 	
 	@Override
 	public Object visit(SimpleNode node, Object data) {
 		
 		if (node instanceof ASTProgram)
 			this.visit((ASTProgram)node, null);
 		else if (node instanceof ASTFunction)
 			this.visit((ASTFunction)node, null);
 		else if (node instanceof ASTDeclarationList)
 			this.visit((ASTDeclarationList)node, null);
 		else if (node instanceof ASTStatementList)
 			this.visit((ASTStatementList)node, null);
 		else if (node instanceof ASTDeclaration)
 			this.visit((ASTDeclaration)node, null);
 		return null;
 	}
 
 	@Override
 	/**
 	 * TODO: THIS RESTRICTS THE SECOND FUNCTION NAME TO FOO
 	 */
 	public Object visit(ASTProgram node, Object data) {
 		ASTDeclarationList innerDecl = (ASTDeclarationList)node.jjtGetChild(0);
 		
 		// add 1-3 var decls
 		Random r = new Random();
 		int numOfVars = r.nextInt(maxVarDeclsInGlobal) + minVarDeclsInGlobal;
 		
 		SymbolTable symbols = Global.getSymbolTable();
 		for (int i = 0; i < numOfVars; i++)
 		{
 			String varName = getRandomItem(possVars);
 			while(symbols.get(varName) != -255)
 			{
 				varName = getRandomItem(possVars);
 			}
 			
 			createVarDecl(innerDecl,varName, r.nextInt(largestPossibleRandomInt)+1, i, ASTDeclaration.class, true);
 		}
 		
 		//TODO: should an array decl always appear or just sometimes?
 		
 		if (addArrayDecl())
 		{ 
 			
 			int arrayElems = 6;
 			
 			int[] values = new int[arrayElems];
 			
 			for (int i = 0; i < arrayElems; i++)
 			{
 				values[i] = randNum();
 			}
 			
 			String varName = getRandomItem(possVars);
 			while(symbols.get(varName) != -255)
 			{
 				varName = getRandomItem(possVars);
 			}
 			
 			createArrayDecl(innerDecl, varName, values, numOfVars, ASTDeclaration.class, true);
 			
 			
 		}
 	
 		visitMain(findChildFuncOfProg(node, "main"), null);
 		visitFunc(findChildFuncOfProg(node, "foo"), null);
 		node.childrenAccept(this, null);
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTVarDecl node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTFunction node, Object data) {
 		/* do nothing for now
 		if (node.getName().equals("main"))
 		{
 			visitMain(node, data);
 		}
 		else
 		{
 			visitFunc(node, data);
 		}
 		*/ 
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTCall node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTVar node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTExpression node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public Object visit(ASTDeclarationList node, Object data) {
 		node.childrenAccept(this, null);
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTDeclaration node, Object data) {
 		node.childrenAccept(this, null);
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTNum node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public Object visit(ASTArrayDeclaration node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTStatementList node, Object data) {
 		node.childrenAccept(this, null);
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTStatement node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTOp node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public Object visit(ASTAssignment node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTArgs node, Object data) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	/**
 	 * this limits the entire randomizer to one function other than main
 	 * @param node
 	 * @param data
 	 * @return
 	 */
 	private Object visitMain(ASTFunction node, Object data)
 	{
 		ASTStatementList innerStatement = (ASTStatementList)node.jjtGetChild(0);
 		// add 0-2 var decls
 		Random r = new Random();
 		int numOfVars = r.nextInt(3);
 		
 		SymbolTable symbols = node.getSymbolTable();
 		for (int i = 0; i < numOfVars; i++)
 		{
 			//TODO: how do we decide which var names to use in here?
 			String varName = getRandomItem(possVars);
 			while(symbols.get(varName, true) != -255)
 			{
 				varName = getRandomItem(possVars);
 			}
 			
 			createVarDecl(innerStatement,varName, r.nextInt(5)+1, i, ASTStatement.class);
 			
 		}
 		// TODO: add 0-1 array decl
 		
 		// add funcCall
 		ASTStatement surroundingStmt = new ASTStatement(JJTSTATEMENT);
 		surroundingStmt.jjtSetParent(innerStatement);
 		innerStatement.jjtAddChild(surroundingStmt, innerStatement.jjtGetNumChildren());
 		
 		
 		ASTCall call = new ASTCall(JJTCALL);
 		call.jjtSetParent(surroundingStmt);
 		surroundingStmt.jjtAddChild(call, surroundingStmt.jjtGetNumChildren());
 		
 		HashMap<String, ASTFunction> funcs = Global.getFunctions();
 		
 		Set<String> keys = funcs.keySet();
 		String callName = null;
 		Iterator<String> iter = keys.iterator();
 		while(iter.hasNext())
 		{
 			callName = iter.next();
 			if (!callName.equals("main"))
 				break;
 		}
 		
 		call.setName(callName);
 		
 		//decide on the number of params the second func will have.
 		
 		HashSet<String> varNames = node.getSymbolTable().getCurrentVarNames();
 		
 		//if true 3 params, else 2
 		if (r.nextBoolean())
 		{
 			String[] varNameArray = new String[varNames.size()]; 
 			varNames.toArray(varNameArray);
 			
 			//TODO: get it so there's repeated params sometimes
 			for (int i = 0; i < 3; i++)
 			{
 				ASTVar var = new ASTVar(JJTVAR);
 				
 				createFooArg(var,node.getSymbolTable());
 				
 				call.addArg(var);
 			}
 
 			funcs.get(callName).addParams("x","y","z");
 		}
 		else
 		{
 			String[] varNameArray = new String[varNames.size()]; 
 			varNames.toArray(varNameArray);
 			
 			//TODO: get it so there's repeated params more often
 			for (int i = 0; i < 2; i++)
 			{
 				ASTVar var = new ASTVar(JJTVAR);
 
 				createFooArg(var,node.getSymbolTable());
 				
 				call.addArg(var);			
 			}
 			
 			funcs.get(callName).addParams("x", "y");
 		}
 		
 		
 		
 		return null;
 	}
 	
 	private Object visitFunc(ASTFunction node, Object data)
 	{
 		ASTStatementList innerStmtList = (ASTStatementList)node.jjtGetChild(0);
 		
 		SymbolTable symbols = node.getSymbolTable();
 		
 		//have to add params to the the SymbolTable
 		for (String param : node.getParameters())
 		{
 			symbols.put(param, new ByValVariable(0));
 		}
 		
 		// add 0-1 var decls
 		Random r = new Random();
 		int numOfVars = r.nextInt(2);
 				
 		for (int i = 0; i < numOfVars; i++)
 		{
 			String varName = getRandomItem(possVars);
 			while(symbols.get(varName, true) != -255)
 			{
 				varName = getRandomItem(possVars);
 			}
 			
 			createVarDecl(innerStmtList,varName, r.nextInt(5)+ 1, i, ASTStatement.class);
 		}
 		
 		//choose a "safe variable" for use by the array index
 		
 		int numVars = symbols.getCurrentVarNames().size();
 		String[] symbolNames = new String[numVars];
 		symbols.getCurrentVarNames().toArray(symbolNames);
 		
 		String safeVarName = getRandomItem(symbolNames);
 		Variable safeVar = symbols.getVariable(safeVarName);
 		
 		while(safeVar.getIsArray())
 		{
 			safeVarName = getRandomItem(symbolNames);
 			safeVar = symbols.getVariable(safeVarName);
 		}
 		
 		
 		//start making some crazy assignment statements
 		
 		
 		// between 4 - 6 assignment statements
 		int numOfAssgnStmts = r.nextInt(3) + 4;
 		
 		//...one of which will be an array
 		int arrayStmt = r.nextInt(numOfAssgnStmts);
 		
 		for (int i = 0; i < numOfAssgnStmts; i++)
 		{
 			if (i == arrayStmt)
 			{
 				createOpAssign(innerStmtList, symbols, numOfVars + i, safeVarName, true);
 			}
 			else // its not an array statement
 			{
 				if (assignOrOp()) //a basic assignment statement
 				{
 					createBasicAssign(innerStmtList, symbols, numOfVars + i, safeVarName);
 				}
 				else // assignment with operations
 				{
 					createOpAssign(innerStmtList, symbols, numOfVars + i, safeVarName, false);
 				}
 			}
 		}
 		
 		
 		return null;
 	}
 	
 	private void createBasicAssign(ASTStatementList parent, SymbolTable symbols, 
 			int index, String safeVar)
 	{
 		ASTStatement stmt = new ASTStatement(JJTSTATEMENT);
 		stmt.jjtSetParent(parent);
 		parent.jjtAddChild(stmt, index);
 		
 		ASTExpression enclosingExp = new ASTExpression(JJTEXPRESSION);
 		enclosingExp.jjtSetParent(stmt);
 		stmt.jjtAddChild(enclosingExp, 0);
 		
 		ASTAssignment assign = new ASTAssignment(JJTASSIGNMENT);
 		assign.jjtSetParent(enclosingExp);
 		enclosingExp.jjtAddChild(assign, 0);
 		
 		ASTVar var = new ASTVar(JJTVAR);
 		var.jjtSetParent(assign);
 		assign.jjtAddChild(var, 0);
 		
 		String[] varNames = new String[symbols.getCurrentVarNames().size()];
 		symbols.getCurrentVarNames().toArray(varNames);
 		
 		// Tom added this to fix some null problems
 		String randomName = getRandomItem(varNames);
		Variable testVar = symbols.getVariable(randomName);
		while (randomName.equals(safeVar) || testVar.getIsArray())
 		{
 			randomName = getRandomItem(varNames);
			testVar = symbols.getVariable(randomName);
 		}
 		
 		var.setName(randomName);
 		assign.setName(randomName);
 		
 		ASTExpression numExp = new ASTExpression(JJTEXPRESSION);
 		numExp.jjtSetParent(assign);
 		assign.jjtAddChild(numExp, 1);
 		
 		ASTNum num = new ASTNum(JJTNUM);
 		num.jjtSetParent(numExp);
 		numExp.jjtAddChild(num, 0);
 		
 		num.setValue(randNum());
 	}
 	
 	private void createOpAssign(ASTStatementList parent, SymbolTable symbols, 
 			int index, String safeVar, boolean lhsArray)
 	{
 		ASTStatement stmt = new ASTStatement(JJTSTATEMENT);
 		stmt.jjtSetParent(parent);
 		parent.jjtAddChild(stmt, index);
 		
 		ASTExpression enclosingExp = new ASTExpression(JJTEXPRESSION);
 		enclosingExp.jjtSetParent(stmt);
 		stmt.jjtAddChild(enclosingExp, 0);
 		
 		ASTAssignment assign = new ASTAssignment(JJTASSIGNMENT);
 		assign.jjtSetParent(enclosingExp);
 		enclosingExp.jjtAddChild(assign, 0);
 		
 		ASTVar var = new ASTVar(JJTVAR);
 		var.jjtSetParent(assign);
 		assign.jjtAddChild(var, 0);
 		
 		createLHSOpExp(var, symbols, safeVar, lhsArray);
 		assign.setName(var.getName());
 		
 		ASTExpression rhsExp = new ASTExpression(JJTEXPRESSION);
 		rhsExp.jjtSetParent(assign);
 		assign.jjtAddChild(rhsExp, 1);
 		
 		ASTOp opExp = new ASTOp(JJTOP);
 		opExp.jjtSetParent(rhsExp);
 		rhsExp.jjtAddChild(opExp, 0);
 		
 		
 		createRHSOpExp(opExp, symbols);
 		
 	}
 	
 	private void createLHSOpExp(ASTVar var, SymbolTable symbols,
 			String safeVar, boolean lhsArray)
 	{
 		String[] varNames = new String[symbols.getCurrentVarNames().size()];
 		symbols.getCurrentVarNames().toArray(varNames);
 		
 		//again, Tom added this to fix some null problems
 		String randomName = getRandomItem(varNames);
 		Variable testVar = symbols.getVariable(randomName);
 		
 		if (lhsArray) //array value!
 		{
 			while (!testVar.getIsArray())
 			{
 				randomName = getRandomItem(varNames);
 				testVar = symbols.getVariable(randomName);
 			}
 			var.setName(randomName);
 			var.setIsArray(true);
 			
 			ASTExpression exp = new ASTExpression(JJTEXPRESSION);
 			exp.jjtSetParent(var);
 			var.jjtAddChild(exp, 0);
 			
 			ASTVar innerVar = new ASTVar(JJTVAR);
 			innerVar.jjtSetParent(exp);
 			exp.jjtAddChild(innerVar, 0);
 			
 			innerVar.setName(safeVar);
 		}
 		else //non array value
 		{
 			while (randomName.equals(safeVar) || testVar.getIsArray())
 			{
 				randomName = getRandomItem(varNames);
 				testVar = symbols.getVariable(randomName);
 			}
 		}
 		
 		var.setName(randomName);
 	}
 	
 	private void createRHSOpExp(ASTOp opExp, SymbolTable symbols)
 	{
 		if (plusOrMinus())
 			opExp.setOp("+");
 		else
 			opExp.setOp("-");
 		
 		createOperand(opExp, symbols, 0);
 		createOperand(opExp, symbols, 1);
 	}
 	
 	/**
 	 * 
 	 * @param opExp
 	 * @param symbols
 	 * @param index the index in the opExp, either 0 or 1
 	 */
 	private void createOperand(ASTOp opExp, SymbolTable symbols, int index)
 	{
 		String[] varNames = new String[symbols.getCurrentVarNames().size()];
 		symbols.getCurrentVarNames().toArray(varNames);
 		
 		if(numOrVar()) //num
 		{
 			ASTNum num = new ASTNum(JJTNUM);
 			num.jjtSetParent(opExp);
 			opExp.jjtAddChild(num, index);
 			
 			num.setValue(randNum());
 		}
 		else //var
 		{
 			ASTVar midVar = new ASTVar(JJTVAR);
 			midVar.jjtSetParent(opExp);
 			opExp.jjtAddChild(midVar, index);
 			String varName = getRandomItem(varNames);
 			
 			if (arrayOrVarOperand())// an array index
 			{
 				midVar.setIsArray(true);//its an array, duh
 				
 				Variable test = symbols.getVariable(varName);
 				while(!test.getIsArray())
 				{
 					varName = getRandomItem(varNames);
 					test = symbols.getVariable(varName);
 				}
 				
 				midVar.setName(varName);
 				
 				ASTExpression varChild = new ASTExpression(JJTEXPRESSION);
 				varChild.jjtSetParent(midVar);
 				midVar.jjtAddChild(varChild, 0);
 				
 				ASTNum indexStmt = new ASTNum(JJTNUM);
 				indexStmt.jjtSetParent(varChild);
 				varChild.jjtAddChild(indexStmt, 0);
 				
 				Random r = new Random();
 				int indexNum = r.nextInt(6);
 				indexStmt.setValue(indexNum);
 			}
 			else //just a var
 			{
 				midVar.setName(varName);
 			}
 			
 		}
 	}
 	
 	private <T> T getRandomItem(T[] array)
 	{
 		Random r = new Random();
 		int rand = r.nextInt(array.length);
 		return array[rand];
 	}
 
 	/**
 	 * 
 	 * @return random int between 1 and the largestPossibleRandomInt final, inclusive.
 	 */
 	private int randNum()
 	{
 		Random r = new Random();
 		return r.nextInt(largestPossibleRandomInt) + 1;
 	}
 	
 	
 	
 	private <T> void createVarDecl(Node parent, String varName, 
 			int value, int indexInParent, T surroundingClass)
 	{
 		createVarDecl(parent, varName, value, indexInParent, surroundingClass, false);
 	}
 	
 	/**
 	 * Creates an ASTVarDecl in parent. Assumes that the parent is one step below an 
 	 * ASTFunction or an ASTProgram. 
 	 * @param parent the node that will contain the new ASTVarDecl.
 	 * @param varName name of the variable
 	 * @param value an integer that will be held in the variable.
 	 * @param indexInParent the index in whatever list parent is.
 	 * @param surroundingClass either ASTStatement or ASTDeclaration, depending on what you want
 	 */
 	private <T> void createVarDecl(Node parent, String varName, 
 			int value, int indexInParent, T surroundingClass, boolean addSafely)
 	{
 		Node surroundingNode = null;
 		if (surroundingClass == ASTDeclaration.class)
 			surroundingNode = new ASTDeclaration(JJTDECLARATION);
 		else
 			surroundingNode = new ASTStatement(JJTSTATEMENT);
 		
 		surroundingNode.jjtSetParent(parent);
 		if (addSafely)
 			parent.jjtAddChildSafe(surroundingNode, indexInParent);
 		else
 			parent.jjtAddChild(surroundingNode, indexInParent);
 		
 		
 		ASTVarDecl var = new ASTVarDecl(JJTVARDECL);
 
 		
 		var.jjtSetParent(surroundingNode);
 		surroundingNode.jjtAddChild(var, 0);
 		
 		var.setName(varName);
 		
 		ASTExpression exp = new ASTExpression(JJTEXPRESSION);
 
 		
 		exp.jjtSetParent(var);
 		var.jjtAddChild(exp, 0);
 		
 		ASTNum num = new ASTNum(JJTNUM);
 	
 		
 		num.jjtSetParent(exp);
 		exp.jjtAddChild(num, 0);
 		
 		num.setValue(value);
 		
 		if (parent.jjtGetParent() instanceof ASTProgram)
 			Global.getSymbolTable().put(varName, new ByValVariable(value));
 		else
 			((ASTFunction)parent.jjtGetParent()).getSymbolTable().put(
 					varName, new ByValVariable(value));
 	}
 	
 	private <T> void createArrayDecl(Node parent, String varName, 
 			int[] values, int indexInParent, T surroundingClass, boolean addSafely)
 	{
 		Node surroundingNode = null;
 		if (surroundingClass == ASTDeclaration.class)
 			surroundingNode = new ASTDeclaration(JJTDECLARATION);
 		else
 			surroundingNode = new ASTStatement(JJTSTATEMENT);
 		
 		surroundingNode.jjtSetParent(parent);
 		if (addSafely)
 			parent.jjtAddChildSafe(surroundingNode, indexInParent);
 		else
 			parent.jjtAddChild(surroundingNode, indexInParent);
 		
 		ASTVarDecl varDecl = new ASTVarDecl(JJTVARDECL);
 		varDecl.jjtSetParent(surroundingNode);
 		surroundingNode.jjtAddChild(varDecl, 0);
 		
 		varDecl.setIsArray(true);
 		varDecl.setName(varName);
 		
 		ASTArrayDeclaration arrayDecl = new ASTArrayDeclaration(JJTARRAYDECLARATION);
 		arrayDecl.jjtSetParent(varDecl);
 		varDecl.jjtAddChild(arrayDecl, 0);
 		
 		for (int i = 0; i < values.length; i++)
 		{
 			ASTNum numNode = new ASTNum(JJTNUM);
 			numNode.jjtSetParent(arrayDecl);
 			arrayDecl.jjtAddChild(numNode, i);
 			
 			numNode.setValue(values[i]);
 		}
 		
 		Variable arrayVar = new ByValVariable(0);
 		
 		arrayVar.setArray();
 		if (parent.jjtGetParent() instanceof ASTProgram)
 			Global.getSymbolTable().put(varName, arrayVar);
 		else
 			((ASTFunction)parent.jjtGetParent()).getSymbolTable().put(
 					varName, arrayVar);
 	}
 	
 	private void createFooArg(ASTVar var, SymbolTable symbols)
 	{
 		String[] varNameArray = new String[symbols.getCurrentVarNames().size()]; 
 		symbols.getCurrentVarNames().toArray(varNameArray);
 	
 		if (arrayOrVarOperand())
 		{
 			String randomName = null;
 			Variable testVar = null;
 			for (int j = 0; j < varNameArray.length; j++)
 			{
 				randomName = varNameArray[j];
 				testVar = symbols.getVariable(randomName);
 				if (testVar.getIsArray())
 					break;
 			}
 			
 			var.setName(randomName);
 			var.setIsArray(true);
 			
 			ASTExpression innerExp = new ASTExpression(JJTEXPRESSION);
 			innerExp.jjtSetParent(var);
 			var.jjtAddChild(innerExp, 0);
 			
 			ASTVar indexVar = new ASTVar(JJTVAR);
 			indexVar.jjtSetParent(innerExp);
 			innerExp.jjtAddChild(indexVar, 0);
 			
 			randomName = getRandomItem(varNameArray);
 			testVar = symbols.getVariable(randomName);
			while(testVar.getIsArray())
 			{
 				 randomName = getRandomItem(varNameArray);
 				 testVar = symbols.getVariable(randomName);
 			}
 			
 			indexVar.setName(randomName);
 		}
 		else
 		{
 			String randomName = getRandomItem(varNameArray);
 			Variable testVar = symbols.getVariable(randomName);
 			while(testVar.getIsArray())
 			{
 				 randomName = getRandomItem(varNameArray);
 				 testVar = symbols.getVariable(randomName);
 			}
 			//check for arrays
 			var.setName(randomName);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return true for num, false for var.
 	 */
 	private boolean numOrVar()
 	{
 		return binDecision(chanceOfNumToVar);
 	}
 	
 	/**
 	 * 
 	 * @return true for assignment with operators, 
 	 * false for basic assignment.
 	 */
 	private boolean assignOrOp()
 	{
 		return binDecision(chanceOfAssignToOp);
 	}
 	
 	private boolean plusOrMinus()
 	{
 		return binDecision(chanceOfPlusToMinus);
 	}
 	
 	private boolean arrayOrVarOperand()
 	{
 		return binDecision(chanceOfArrayToVarOp);
 	}
 	
 	private boolean addArrayDecl()
 	{
 		return binDecision(chanceOfArrayDecl);
 	}
 	
 	private boolean binDecision(double probability)
 	{
 		Random r = new Random();
 		double test = r.nextDouble();
 		
 		return test <= probability;
 	}
 	
 	private ASTFunction findChildFuncOfProg(ASTProgram node, String name)
 	{
 		ASTFunction ret = null;
 		
 		ASTDeclarationList list = (ASTDeclarationList)node.jjtGetChild(0);
 		
 		int numChild = list.jjtGetNumChildren();
 		
 		for (int i = 0; i < numChild; i++)
 		{
 			ASTDeclaration decl = (ASTDeclaration)list.jjtGetChild(i);
 			
 			Node child = decl.jjtGetChild(0);
 			if (child instanceof ASTFunction)
 			{
 				ASTFunction func = (ASTFunction)child;
 				
 				if (func.getName().equals(name))
 				{
 					ret = func;
 					break;
 				}
 			}
 			
 		}
 		
 		return ret;
 	}
 	
 }
