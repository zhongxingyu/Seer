 package Interpreter;
 
 import java.util.*;
 import viz.*;
 public class RandomizingVisitor2<T> implements VizParserTreeConstants,
 		VizParserVisitor
 {
 	Random rand = new Random();
 	
 	Class<T> varClass = null;
 	
 	final String[] possVarNames = {"g","i", "j", "k", "m","n", "v", "w"};
 	
 	final String[] arrVarNames = { "a", "b", "c", "d", "e", "f"};
 	final String[] paramNames = {"x", "y", "z" };	
 	final int minVarDeclsInGlobal = 3;
 	final int maxVarDeclsInGlobal = 3;
 	
 	final int minIntInDecl = 1;
 	final int maxIntInDecl = 5;
 	
 	final int minVarDeclsInMain = 1;
 	final int maxVarDeclsInMain = 1;
 	
 	final int minFooParams = 2;
 	final int maxFooParams = 3;
 	
 	final int minFooVarDecls = 1;
 	final int maxFooVarDecls = 1;
 	
 	final int minFooAOStmts = 3;
 	final int maxFooAOStmts = 4;
 	
 	final int minArrayIndex = 0;
 	final int maxArrayIndex = 5;
 	
 	final double chanceOfNumToVar = 1.0/10.0;
 	final double chanceOfAssignToOp = 1.5/10.0;
 	final double chanceOfPlusToMinus = 1.0/2.0;
 	
 	final double chanceOfArrayInMain = 1.0/2.0;
 	final double chanceOfArrayInFoo = 1.0/2.0;
 	
 	final double chanceOfParamAsVar = 1.0/2.0;
 	//this is actually 25% as this is half of the time a param isn't used as var
 	final double chanceOfArgAsVar = 1.0/2.0;
 	
 	
 	InterestingCases intrCase;
 	
 	
 	
 	/**
 	 * 
 	 * @param clazz the subclass of AbstractVariable that you want the randomizer to use
 	 */
 	public RandomizingVisitor2(Class<T> clazz)
 	{
 		varClass = clazz;
 		this.intrCase = getIntrCase();
 		
 	}
 	
 	public RandomizingVisitor2(Class<T> clazz, boolean lazyEval)
 	{
 		varClass = clazz;
		if (lazyEval)
			this.intrCase = getIntrCaseLazy();
		else
			this.intrCase = getIntrCase();
 	}
 	
 	
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
 	public Object visit(ASTProgram node, Object data) {
 		SymbolTable localTable= Global.getSymbolTable();
 		
 		int numVarDecls = numOfGlobalVarDecls();
 		
 		//create VarDecls
 		for (int i = 0; i < numVarDecls; i++)
 		{
 			 ArrayList<String> badNames = localTable.getCurrentVarNamesArray();
 			 
 			 String newName = getNewVarName(badNames);
 			 int value = randomDeclInt();
 			 
 			 ASTVarDecl newVarDecl = createVarDecl(newName, value);
 			 node.addLogicalChild(newVarDecl, i);
 			 
 			 localTable.put(newName, new ByValVariable(value));
 		}
 		
 		int numArrayDecls = numOfGlobalArrayDecls();
 		
 		for (int i = 0; i < numArrayDecls; i++)
 		{
 			ArrayList<String> badNames = localTable.getCurrentVarNamesArray();
 			//the array name here CAN'T be a param name
 			for (int j = 0; j < paramNames.length; j++)
 			{
 				badNames.add(paramNames[j]);
 			}
 			
 			String newName = getNewArrayName(badNames);
 			
 			ASTVarDecl arrayDecl = createArrayDecl(newName);
 			
 			node.addLogicalChild(arrayDecl, numVarDecls + i);
 			
 			localTable.put(newName, ByValVariable.createArrayVariable());
 		}
 		
 		visitMain(findChildFuncOfProg(node, "main"));
 		visitFoo(findChildFuncOfProg(node, "foo"));
 		
 		return node.childrenAccept(this, data);
 	}
 
 	@Override
 	public Object visit(ASTDeclarationList node, Object data) {
 		return node.childrenAccept(this, data);
 	}
 
 	@Override
 	public Object visit(ASTDeclaration node, Object data) {
 		node.childrenAccept(this, null);
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTVarDecl node, Object data) {
 		
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTArrayDeclaration node, Object data) {
 		
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTFunction node, Object data) {
 		
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTStatementList node, Object data) {
 		return node.childrenAccept(this, data);
 	}
 
 	@Override
 	public Object visit(ASTStatement node, Object data) {
 	
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTCall node, Object data) {
 		
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTVar node, Object data) {
 		
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTAssignment node, Object data) {
 		
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTExpression node, Object data) {
 		
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTArgs node, Object data) {
 		
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTOp node, Object data) {
 		
 		return null;
 	}
 
 	@Override
 	public Object visit(ASTNum node, Object data) {
 		
 		return null;
 	}
 	
 	private void visitMain(ASTFunction main)
 	{
 		SymbolTable localTable = main.getSymbolTable();
 		int numVars = -1;
 		
 		/* THIS WILL PROBABLY  BE USED LATER 
 		if( this.intrCase == InterestingCases.Shadowing)
 		{
 			ASTVarDecl decl = createShadowedArrayDecl(localTable);
 			main.addLogicalChild(decl, 0);
 			
 			numVars = 1;
 		}
 		else
 		{*/
 		numVars = numOfMainVarDecls();
 		for (int i = 0; i < numVars; i++)
 		{
 			ArrayList<String> badNames = localTable.getLocalVarNamesArray();
 			badNames.addAll(localTable.getCurrentVarNamesArray(VarRetrRest.ArrayOnly));
 			String name = getNewVarName(badNames);
 			int value = randomDeclInt();
 			 
 			ASTVarDecl newVarDecl = createVarDecl(name, value);
 			main.addLogicalChild(newVarDecl, i);
 			 
 			localTable.put(name, new ByValVariable(value));
 		}
 		
 		
 		if (arrayInMain())
 		{
 			ArrayList<String> badNames = localTable.getLocalVarNamesArray();
 			String name = getNewArrayName(badNames);
 			
 			ASTVarDecl newVarDecl = createArrayDecl(name);
 			main.addLogicalChild(newVarDecl, numVars);
 			
 			localTable.put(name, ByValVariable.createArrayVariable());
 			
 			//increment this so the foo call is in the right place
 			numVars++;
 		}
 		//}
 		
 		
 		int numOfParams = numOfFooParams();
 		addParamsToFoo(numOfParams);
 		
 		ASTCall fooCall = ASTCall.createCall("foo");
 		main.addLogicalChild(fooCall, numVars);
 		
 		
 		ArrayList<ASTVar> args = createArgs(numOfParams, localTable);
 		
 		
 		HashMap<String,String> pa = new HashMap<String, String>();
 		int i = 0;
 		for( ASTVar v : args)
 		{
 if (XAALScripter.debug) {			System.out.println("AAADDINNG");
 }			pa.put(paramNames[i], v.getCodeRaw());
 			fooCall.addArg(v);
 		}
 		fooCall.addArgs(args);
 if (XAALScripter.debug) {		System.out.println("YYY " + fooCall.getArgs().size());
 }		
 		Global.setCurrentParamToArg(pa);
 	}
 	
 	private void visitFoo(ASTFunction foo)
 	{
 		SymbolTable localTable = foo.getSymbolTable();
 		
 		ArrayList<String> params = foo.getParameters();
 		for(String p : params)
 		{
 			Variable v = null;
 			try {
 				v = (Variable)varClass.newInstance();
 			} catch (InstantiationException e) {
 				
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				
 				e.printStackTrace();
 			}
 			
 			if (varClass != ByRefVariable.class && varClass != ByNameVariable.class)
 				v.setValue(-255);
 			
 			v.setParam();
 			
 			localTable.put(p, v);
 		}
 		
 		int numVarDecls = numOfFooVarDecls();
 		{
 			int i = 0;
 			
 			//create shadowed array.
 			if (intrCase == InterestingCases.Shadowing)
 			{
 				ASTVarDecl var = createShadowedArrayDecl(localTable);
 				foo.addLogicalChild(var, 0);
 				
 				localTable.put(var.getName(), ByValVariable.createArrayVariable());
 				
 				
 				var = createShadowedVarDecl(localTable);
 				foo.addLogicalChild(var,1);
 				
 				//increment spot in node
 				i = 2;
 			}
 		
 			for(; i < numVarDecls; i++)
 			{
 				ArrayList<String> badVars = localTable.getLocalVarNamesArray();
 				badVars = getBadLHSNames(localTable, badVars);
 				String name = getNewVarName(badVars);
 				int value = randomDeclInt();
 				
 				ASTVarDecl v = createVarDecl(name, value);
 				foo.addLogicalChild(v, i);
 				
 				localTable.put(name, new ByValVariable(value));
 			}
 		}
 		//one array is plenty
 		if (arrayInFoo() && intrCase != InterestingCases.Shadowing)
 		{
 			ArrayList<String> badVars = localTable.getLocalVarNamesArray();
 			String name = getNewArrayName(badVars);
 			
 			ASTVarDecl v = createArrayDecl(name);
 			foo.addLogicalChild(v, numVarDecls);
 			
 			localTable.put(name, ByValVariable.createArrayVariable());
 			
 			//increment so the AO statements are in the right place
 			numVarDecls++;
 		}
 		
 		
 		//figure out the safe index vars and add them
 		ArrayList<String> safeIndexVars = new ArrayList<String>();
 		String safeVar = getRandomItem(localTable.getCurrentVarNamesArray(VarRetrRest.NotArrayOnly));
 		safeIndexVars.add(safeVar);
 		
 		
 		/*
 		 * //Variable safeVarAsVar = localTable.getVariable(safeVar);
 		//if its a reference, also add the referenced variable
 		if (safeVarAsVar instanceof ByRefVariable)
 		{
 			ByRefVariable refVar = (ByRefVariable)safeVarAsVar;
 			refVar.getRef()
 		}
 		*/
 		
 		int numAOStmts = numOfFooAOStmts();
 		
 		//this pos to use the ArrayIndex
 		int posForArrayIndex = randomNum(0, numAOStmts-1);
 		
 		for (int i = 0; i < numAOStmts; i++)
 		{
 			ASTAssignment assign = null;
 			
 			if (i == posForArrayIndex)// the operation with the array index
 			{
 				assign = createIndexedAssign(localTable, safeIndexVars);
 			
 			}
 			else //non array index action
 			{
 				/*// we don't want basic assigns anymore
 				if(assignOrOp()) //basic assignment
 				{
 					assign = createBasicAssign(localTable, safeIndexVars);
 				}
 				else //assign with op
 				{*/
 					assign = createOpAssign(localTable, safeIndexVars);
 				//}
 			}
 			
 			foo.addLogicalChild(assign, numVarDecls + i);
 		}
 	}
 	
 	private ASTVarDecl createVarDecl(String name, int value)
 	{
 		return ASTVarDecl.createVarDecl(name, value);
 	}
 	
 	private ASTVarDecl createArrayDecl(String name)
 	{
 		ArrayList<Integer> values = createArrayList();
 		return ASTVarDecl.createArrayDecl(name, values);
 	}
 	
 	private ArrayList<Integer> createArrayList()
 	{
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 		
 		int numElems = numOfArrayElems();
 		
 		for (int i = 0; i < numElems; i++)
 		{
 			ret.add(new Integer(randomDeclInt()));
 		}
 		
 		return ret;
 	}
 	
 	private ASTVar createVar(SymbolTable table)
 	{
 		return createVar(table, null);
 	}
 	
 	private ASTVar createVar(SymbolTable table, ArrayList<String> bannedNames)
 	{
 		return createVar(table, bannedNames, false);
 	}
 	
 	private ASTVar createVar(SymbolTable table, ArrayList<String> bannedNames, boolean indexIsNum)
 	{
 		ArrayList<String> origVarNames = table.getCurrentVarNamesArray();
 		ArrayList<String> varNames = new ArrayList<String>();
 		
 		if (bannedNames != null)
 		{
 			for(String v : origVarNames)
 			{
 				if (!bannedNames.contains(v))
 					varNames.add(v);
 			}
 		}
 		else
 		{
 			varNames = origVarNames;
 		}
 		
 		String randomVar = getRandomItem(varNames);
 		
 		Variable v = table.getVariable(randomVar);
 		
 		if (v.getIsArray())
 		{
 			ArrayList<String> nonArrayVars = table.getCurrentVarNamesArray(VarRetrRest.NotArrayOnly);
 			
 			testNonArrayVars(nonArrayVars, table);
 			if (indexIsNum == true) //index must be a num
 			{
 				return ASTVar.createVarWithIndex(randomVar, randomArrayIndex());
 			}
 			else //index will be a var
 			{
 				return ASTVar.createVarWithIndex(randomVar, getRandomItem(nonArrayVars));
 			}
 			
 		}
 
 		return ASTVar.createVar(randomVar);
 	}
 	
 	//TODO: make it so the aliased params are in positions other than 0 and 1
 	private ArrayList<ASTVar> createArgs(int numOfParams, SymbolTable table)
 	{
 		ArrayList<ASTVar> ret = new ArrayList<ASTVar>();
 		int i = 0;
 		
 		if(intrCase == InterestingCases.Aliasing)
 		{
 			//add first one
 			ASTVar var1 = createVar(table);
 			ret.add(var1);
 			
 			//create second one based on first
 			ASTVar var2 = null;
 			if (var1.getIsArray())// it's indexed so get the index
 			{
 				ASTVar indexVar = (ASTVar) var1.jjtGetChild(0).jjtGetChild(0);
 				var2 = ASTVar.createVarWithIndex(var1.getName(), indexVar.getName());
 			}
 			else // its not an array
 			{
 				var2 = ASTVar.createVar(var1.getName());
 			}
 			ret.add(var2);
 			
 			i = 2;
 			
 		}
 		else if (intrCase == InterestingCases.Shadowing)
 		{
 			ArrayList<String> arrays = table.getCurrentVarNamesArray(VarRetrRest.ArrayOnly);
 			ArrayList<String> indexVars = table.getCurrentVarNamesArray(VarRetrRest.NotArrayOnly);
 			
 			testArrayEmpty(arrays);
 			testArrayVars(arrays, table);
 			testNonArrayVars(indexVars, table);
 			
 			String name = getRandomItem(arrays);
 			String index = getRandomItem(indexVars);
 			
 			ASTVar var1 = ASTVar.createVarWithIndex(name, index);
 			ret.add(var1);
 			
 			i = 1;
 			
 		}
 
 		for (; i < numOfParams; i++)
 		{
 			ASTVar var = createVar(table);
 			ret.add(var);
 		}
 		
 		
 		return ret;
 	}
 	/*
 	private String createSafeIndexVar(SymbolTable localTable)
 	{
 		ArrayList<String> safeVars = localTable.getCurrentVarNamesArray(VarRetrRest.NotArrayOnly);
 		
 		//testSafeIndexVars(safeVars, localTable);
 		
 		return getRandomItem(safeVars);
 	}*/
 	/*
 	private ArrayList<String> createSafeIndexRefs(ArrayList<String> safeIndexs, SymbolTable localTable)
 	{
 		ArrayList<String> ret = new ArrayList<String>();
 		
 		for (String s : safeIndexs)
 		{
 			Variable v = localTable.getVariable(s);
 			if (v instanceof ByRefVariable)
 			{
 				String name = localTable.getNameByVariable(v);
 				if(name != null)
 				{
 					ret.add(name);
 				}	
 				else
 				{
 					throw new AssumptionFailedException();
 				}
 				
 			}
 		}
 		
 		return ret;
 	}*/
 	
 	private ASTAssignment createBasicAssign(SymbolTable localTable, ArrayList<String> badNames)
 	{
 		ArrayList<String> badLHSNames = getBadLHSNames(localTable, badNames);
 		
 		if (paramAsVar())
 			badLHSNames = localTable.getCurrentVarNamesArray(VarRetrRest.NotParamOnly);
 		//TODO doesn't use arrays because its hard to tell if they're safe
 		else if (argAsVar())
 		{
 			if (getNonArrayArgsInScope(localTable).size() > 0)
 				badLHSNames = nonArgsVarList(localTable);
 		}
 		
 		ASTVar lhs = createVar(localTable, badLHSNames, true);
 		ASTNum rhs = ASTNum.createNum(randomDeclInt());
 		
 		return ASTAssignment.createAssignment(lhs, rhs);
 	}
 	
 	private ASTAssignment createOpAssign(SymbolTable localTable, ArrayList<String> badNames)
 	{
 		return createOpAssign(localTable, badNames, null);
 	}
 	
 	private ASTAssignment createOpAssign(SymbolTable localTable, ArrayList<String> badNames, 
 			ASTVar lhs)
 	{
 		if (lhs == null)
 		{
 			if (paramAsVar())
 			{	
 				ArrayList<String> nonParams = localTable.getCurrentVarNamesArray(VarRetrRest.NotParamOnly);
 				lhs = createVar(localTable, nonParams, true);
 			}
 			
 			else if (argAsVar())
 			{
 				int nonArrayArgsSize = getNonArrayArgsInScope(localTable).size();
 				if (nonArrayArgsSize > 0)
 					lhs = createVar(localTable, nonArgsVarList(localTable), true);
 				else
 					lhs = createVar(localTable, badNames, true);
 			}
 			else
 			{
 				lhs = createVar(localTable, badNames, true);
 			}
 			
 		}
 		
 		Node rhs1 = createOperand(localTable);
 		Node rhs2 = createOperand(localTable);
 		
 		ValidOperations operator = ValidOperations.Sub;
 		if (plusOrMinus())
 			operator = ValidOperations.Add;
 		
 		ASTOp op = ASTOp.createOp(rhs1, rhs2, operator);
 		
 		return ASTAssignment.createAssignment(lhs, op);
 	}
 	
 	private ASTAssignment createIndexedAssign(SymbolTable localTable, ArrayList<String> safeVars)
 	{
 		ArrayList<String> arrays = localTable.getCurrentVarNamesArray(VarRetrRest.ArrayOnly);
 		
 		testArrayEmpty(arrays);
 		
 		testArrayVars(arrays, localTable);
 		
 		ASTVar lhs = ASTVar.createVarWithIndex(getRandomItem(arrays), getRandomItem(safeVars));
 		
 		return createOpAssign(localTable, null, lhs);
 	}
 	
 	private Node createOperand(SymbolTable localTable)
 	{
 		ArrayList<String> badNames = null;
 		
 		if (numOrVar()) //num
 		{
 			return ASTNum.createNum(randomDeclInt());
 		}
 		
 		//var
 		if (paramAsVar())
 			badNames = localTable.getCurrentVarNamesArray(VarRetrRest.NotParamOnly);
 		else if (argAsVar())
 		{
 			
 			if (getNonArrayArgsInScope(localTable).size() > 0)
 				badNames = nonArgsVarList(localTable);
 		}
 		return createVar(localTable, badNames, true);
 	}
 	
 	/**
 	 * must be called at the BEGINNING of a function, not later.
 	 * @param localTable
 	 */
 	private ASTVarDecl createShadowedArrayDecl(SymbolTable localTable)
 	{
 		ArrayList<String> arrays = localTable.getCurrentVarNamesArray(VarRetrRest.ArrayOnly);
 		
 		testArrayEmpty(arrays);
 		
 		String name = getNewVarFromList(arrays);
 		
 		ASTVarDecl ret = createArrayDecl(name);
 		
 		return ret;
 	}
 	
 	/**
 	 * must be called at the BEGINNING of a function or right after shadowed array
 	 * declaration, not later. Adds to local symbol table
 	 * 
 	 */
 	private ASTVarDecl createShadowedVarDecl(SymbolTable localTable)
 	{
 		ArrayList<String> vars = localTable.getCurrentVarNamesArray(VarRetrRest.NotParamOrArrayOnly);
 		
 		testArrayEmpty(vars);
 		
 		String name = getNewVarFromList(vars);
 		int value = randomDeclInt();
 		
 		ASTVarDecl ret = createVarDecl(name, value);
 		
 		localTable.put(name, new ByValVariable(value));
 		
 		return ret;
 	}
 	
 	
 	
 	private void addParamsToFoo(int numToAdd)
 	{
 		ASTFunction foo = Global.getFunctions().get("foo");
 		
 		String[] params = new String[numToAdd];
 		
 		for(int i = 0; i < numToAdd; i++)
 		{
 			params[i] = paramNames[i];
 		}
 		
 		foo.addParams(params);
 	}
 	
 	/**
 	 * random number between min and max inclusive
 	 * @param min
 	 * @param max
 	 * @return
 	 */
 	private int randomNum(int min, int max)
 	{
 		return rand.nextInt(max+1-min) + min;
 	}
 	
 	private int numOfGlobalVarDecls()
 	{
 		return randomNum(minVarDeclsInGlobal, maxVarDeclsInGlobal);
 	}
 	
 	private int numOfGlobalArrayDecls()
 	{
 		return 1;
 	}
 	
 	private int numOfMainVarDecls()
 	{
 		return randomNum(minVarDeclsInMain, maxVarDeclsInMain);
 	}
 	
 	private int numOfArrayElems()
 	{
 		return 6;
 	}
 	
 	private int numOfFooParams()
 	{
 		return randomNum(minFooParams, maxFooParams);
 	}
 	
 	private int numOfFooVarDecls()
 	{
 		return randomNum(minFooVarDecls, maxFooVarDecls);
 	}
 	
 	private int numOfFooAOStmts()
 	{
 		return randomNum(minFooAOStmts, maxFooAOStmts);
 	}
 	
 	private int randomDeclInt()
 	{
 		return randomNum(minIntInDecl, maxIntInDecl);
 	}
 	
 	private int randomArrayIndex()
 	{
 		return randomNum(minArrayIndex, maxArrayIndex);
 	}
 	
 	/**
 	 * This function gets a new name from a list of possible names, excluding 
 	 * the banned names.
 	 * @param bannedNames
 	 * @return
 	 */
 	private String getNewVarName(ArrayList<String> bannedNames)
 	{
 		ArrayList<String> possNames = new ArrayList<String>();
 		
 		for(String name : this.possVarNames)
 		{
 			if( !bannedNames.contains(name))
 				possNames.add(name);
 		}
 		
 		return getRandomItem(possNames);
 		
 	}
 	
 	private String getNewArrayName(ArrayList<String> bannedNames)
 	{
 		ArrayList<String> possNames = new ArrayList<String>();
 		
 		for(String name : this.arrVarNames)
 		{
 			if( !bannedNames.contains(name))
 				possNames.add(name);
 		}
 		
 		return getRandomItem(possNames);
 	}
 	
 	private String getNewVarFromList (ArrayList<String> choices)
 	{
 		return getRandomItem(choices);
 	}
 	
 	private ArrayList<String> nonArgsVarList(SymbolTable localTable)
 	{
 		ArrayList<String> ret = new ArrayList<String>();
 		
 		ArrayList<String> vars = localTable.getCurrentVarNamesArray();
 		
 		for (String v : vars)
 		{
 			if (!Global.getCurrentParamToArg().containsValue(v) || 
 					localTable.getVariable(v).getIsArray())
 			{
 				ret.add(v);
 			}
 		}
 		
 		//ret.addAll(localTable.getCurrentVarNamesArray(VarRetrRest.ArrayOnly));
 		
 		return ret;
 	}
 	
 	private ArrayList<String> getNonArrayArgs()
 	{
 		ArrayList<String> ret = new ArrayList<String>();
 		for (String s : Global.getCurrentParamToArg().values())
 		{
 			if (s.length() == 1)
 				ret.add(s);
 		}
 		
 		return ret;
 	}
 	
 	private ArrayList<String> getNonArrayArgsInScope(SymbolTable localTable)
 	{
 		ArrayList<String> args = getNonArrayArgs();
 		ArrayList<String> local = localTable.getCurrentVarNamesArray(VarRetrRest.NotArrayOnly);
 		ArrayList<String> ret = new ArrayList<String>();
 		for (String s : args)
 		{
 			if (local.contains(s))
 				ret.add(s);
 		}
 		
 		testNonArrayVars(ret, localTable);
 		
 		return ret;
 	}
 	
 	private <S> S getRandomItem(ArrayList<S> items)
 	{
 		return items.get(rand.nextInt(items.size()));
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
 	
 	private boolean arrayInMain()
 	{
 		return binDecision(chanceOfArrayInMain);
 	}
 	
 	private boolean paramAsVar()
 	{
 		return binDecision(chanceOfParamAsVar);
 	}
 	
 	private boolean arrayInFoo()
 	{
 		return binDecision(chanceOfArrayInFoo);
 	}
 	
 	private boolean argAsVar()
 	{
 		return binDecision(chanceOfArgAsVar);
 	}
 	private boolean binDecision(double probability)
 	{
 		
 		double test = rand.nextDouble();
 		
 		return test <= probability;
 	}
 	
 	/**
 	 * method to get an interesting case. The numbers are hardcoded in for now.
 	 * @return an InterestingCases value
 	 */
 	private InterestingCases getIntrCase()
 	{
 		double probability = rand.nextDouble();
 		
 		if (probability <= .45)
 		{
 			return InterestingCases.Aliasing;
 		}
 		else if (probability <= .90)
 		{
 			return InterestingCases.Shadowing;
 		}
 		else
 		{
 			return InterestingCases.None;
 		}
 		
 	}
 	
 	private InterestingCases getIntrCaseLazy()
 	{
 		double probability = rand.nextDouble();
 		if (probability <= .90)
 		{
 			return InterestingCases.Shadowing;
 		}
 		else
 		{
 			return InterestingCases.None;
 		}
 	}
 	
 	private ArrayList<String> getBadLHSNames(SymbolTable localTable, ArrayList<String> badNames)
 	{
 		ArrayList<String> ret = new ArrayList<String>(badNames);
 		
 		ArrayList<String> arrays = localTable.getCurrentVarNamesArray(VarRetrRest.ArrayOnly);
 		
 		testArrayVars(arrays, localTable);
 		
 		ret.addAll(arrays);
 		
 		return ret;
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
 	
 	private void testNonArrayVars(ArrayList<String> vars, SymbolTable symbols)
 	{
 		for(String v : vars)
 		{
 			if (symbols.getVariable(v).getIsArray())
 				throw new AssumptionFailedException();
 		}
 	}
 	/*
 	private void testSafeIndexVars(ArrayList<String> vars, SymbolTable symbols)
 	{
 		for (String v : vars)
 		{
 			if (symbols.getVariable(v).getIsArray())
 				throw new AssumptionFailedException();
 		}
 	}*/
 	
 	private void testArrayVars(ArrayList<String> vars, SymbolTable symbols)
 	{
 		for (String v: vars)
 		{
 			if (!symbols.getVariable(v).getIsArray())
 				throw new AssumptionFailedException();
 		}
 	}
 	
 	private void testArrayEmpty(ArrayList<String> vars)
 	{
 		if (vars.size() == 0)
 			throw new AssumptionFailedException();
 	
 	}
 	
 }
