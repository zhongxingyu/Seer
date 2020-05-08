 // modified by Melissa Olson, Tanmaya Godbole, and Sriratana Sutasirisap - Spring 2011
 
 package proj;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import proj.AbstractSyntax.*;
 import proj.AbstractSyntax.Variable.VarType;
 import static proj.Messages.*;
 import static proj.Util.myAssert;
 
 public class Interpreter {
     private static final int STACK_PRINT_CONST = 10;
     private static final int STACK_SIZE = 16384; // For simplicity, it's non-growing.
     private boolean debug;
     private List<LambdaValue> lambdaStack = new ArrayList<LambdaValue>();
     private Value[] stack;
     private Variable[] debugStack;
     private int basePtr;
 
     private RuntimeStack runtimeStack;
 
     private Program prog;
 
     // Avoid passing id down caller chain!
     //private Lambda curLambda;
 
     public Interpreter(boolean debug)
     {
         this.debug = debug;
         stack = new Value[STACK_SIZE];
         basePtr = 0;
         if (debug) {
             debugStack = new Variable[STACK_SIZE];
         }
 
         runtimeStack = new RuntimeStack();
     }
     
     private void printLastStackFrame()
     {
         for (int i = basePtr; i < basePtr + STACK_PRINT_CONST; i++) {
             if (debugStack != null) {
                 System.out.print(debugStack[i] + ": ");
             }
         }
     }
 
     
     
     public void runProgram(Program prog)
         throws InterpreterRuntimeError
     {
 	this.prog = prog; //added later
         evaluateDeclarations(prog.getGlobals());
        
         // Shift the Base Pointer to the end of Globals:
         basePtr = prog.getNumGlobals();
         
         // Find the 'main' function and run it!
         Function main = Util.findFunction(prog.getFunctions(), "main");
         if (main == null) {
             throw new InterpreterRuntimeError(-1, MAIN_NOT_FOUND);
         }     
 
         //jz adding new stack stuff
         runtimeStack.addRecord(new ActivationRecord("main"));
 
         runStatement(main.getBody());
     }
 
     
     public Value callRealFunction(Call call, List<Value> args)
         throws InterpreterRuntimeError
     {
         Value result = null;
         
         basePtr += call.getStackOffset();
 
         //jz just create and add a new activation record here
         ActivationRecord ar = new ActivationRecord(call.getFunction().getName());
         //jz just create and add a new activation record here
         //jz add new record to the stack
         runtimeStack.addRecord(ar);
         //jz add new record to the stack
         
         List<Declaration> params = call.getFunction().getParams();
         
         if (args.size() != params.size()) {
             throw new InterpreterRuntimeError(call.getLineNum(), INV_NUM_ARGS, 
                     call.getFunction().getName(), params.size(), args.size());
         }
         
         for (int i = 0, size = args.size(); i < size; i++) {
             setVarValue(params.get(i).getVariable(), args.get(i)); //need to worry about this!
 
             /*
             //jz add everything to that activation record
             ar.addVarValue(params.get(i).getVariable(), args.get(i));
             //jz add everything to that activation record
             */
         }
 
         runStatement(call.getFunction().getBody());
         
         if (debug) {
             printLastStackFrame();
         }
         
         // NOTE: By convention, the return value shall be assigned the FIRST address:
         if (call.getFunction().isVoid() == false) {
             result = stack[basePtr];
             if (result == null) {
                 throw new InterpreterRuntimeError(call.getLineNum(), 
                         FUNCTION_DID_NOT_RETURN_VALUE, call.getFunction().getName());
             }
         }
 
         //jz next thing pop off
         runtimeStack.removeRecord();
         //jz next thing pop off
 
         basePtr -= call.getStackOffset();
         return result; //this result corresponds to the result of the actualy function you're calling
     }
     // added later by MST - binds the values of argumenst to the parameters, and runs the body of the constructor
     public void callConstructor(Constructor c, List<Value> args) throws InterpreterRuntimeError 
     {
 	List<Declaration> params = c.getParams();
 
 	if (args.size() != params.size()) {
             throw new InterpreterRuntimeError(c.getLineNum(), INV_NUM_ARGS, 
                     "constructor" , params.size(), args.size());
         }
 
 	for (int i = 0, size = args.size(); i < size; i++) {
             setVarValue(params.get(i).getVariable(), args.get(i)); //need to worry about this!
         }
        runStatement(c.getBody());
     }
     
     // added later by MST - gets the Function from the object function, then executes the function.
     public Value callOOFunction(ObjFunction objFunc, List<Value> args) throws InterpreterRuntimeError
     {
         MyClass c = Util.findClass(prog.getClasses(), objFunc.getClassName());
         Function f = null;
         List<Function> fList = c.getFunctions();
         for(Function func : fList){
            if(func.getName().equals(objFunc.getFuncName()))
               f = func;
          } 
         Value result = null;   
         basePtr += objFunc.getStackOffset();
         if(f == null)
           throw new InterpreterRuntimeError(objFunc.getLineNum(), UNDEF_FUNCTION, "object function");
         
         
         List<Declaration> params = f.getParams();
         
         if (args.size() != params.size()) {
             throw new InterpreterRuntimeError(objFunc.getLineNum(), INV_NUM_ARGS, 
                     f.getName(), params.size(), args.size());
         }
         
         for (int i = 0, size = args.size(); i < size; i++) {
             setVarValue(params.get(i).getVariable(), args.get(i)); //need to worry about this!
         }
         
         // Now, execute the actual Function body:
         runStatement(f.getBody());
         
         // NOTE: By convention, the return value shall be assigned the FIRST address:
         if (f.isVoid() == false) {
         	Declaration returnDec = f.getReturnDecl();
         	Variable v = returnDec.getVariable();
         	int address = v.getAddress();
             	result = stack[basePtr + address];
             	if (result == null) {
                 	throw new InterpreterRuntimeError(objFunc.getLineNum(), 
                         FUNCTION_DID_NOT_RETURN_VALUE, f.getName());
             }
         } 
         basePtr -= objFunc.getStackOffset();
         return result; //this result corresponds to the result of the actualy function you're calling
     }
 
     /**
      * @return true: this is a 'return' statement; false otherwise.
      */
 
     public boolean runStatement(Statement s) throws InterpreterRuntimeError {
         if (s instanceof Assignment) {
             Assignment ass = (Assignment) s;
 
             Value val = runExpression(ass.getSource());
             LValue lval = ass.getTarget();
             
             if (lval instanceof ListTupleReference) {
                 ListTupleReference ref = (ListTupleReference)lval;
                 ListIndexPair pair = verifyListTupleRefGetIndex(ref);
                 
                 // Make sure we don't assign any values to the tuples:
                 if (pair.getValueOfListOrTuple().getType() != BaseType.LIST) {
                     throw new InterpreterRuntimeError(ref.getLineNum(), 
                     		TUPLE_CANT_BE_LVAL, ref.getListVar().getName());
                 }
                 // NOTE: No type checking to be done here, because
                 // the list elements are generic object.
                 pair.setValueOfElement(val);
             } else if (lval instanceof Variable) {
                 // No type checking to be done here, because we rely
                 // on the static type checking to make sure it's correct.
                 Variable var = (Variable)lval;
                 setVarValue(var, val);
             } else {
                 throw new UnsupportedOperationException(
                         "Unrecognized lvalue class: " + lval.getClass());
             }
             return false;
         }
 
         if (s instanceof Conditional) {
             Conditional cond = (Conditional) s;
             Value ifResult = runExpression(cond.getTest());
             if (ifResult.getType() != BaseType.BOOL) {
                 throw new InterpreterRuntimeError(cond.getTest().getLineNum(), 
                 		IF_MUST_EVAL_TO_BOOL, ifResult.getType());
             }
 
             if (ifResult.boolValue()) {
                 runStatement(cond.getThenbranch());
             } else if (cond.getElsebranch() != null) {
                 runStatement(cond.getElsebranch());
             }
             return false;
         }
 
         if (s instanceof Loop) {
             Loop loop = (Loop) s;
 
             while (true) {
                 Value test = runExpression(loop.getTest());
 
                 if (test.getType() != BaseType.BOOL) {
                     throw new InterpreterRuntimeError(loop.getTest().getLineNum(), 
                     		WHILE_MUST_EVAL_TO_BOOL, test.getType());
                 }
                 if (test.boolValue() == false) {
                     break;
                 }
                 // When we run the statements, we need to check
                 // for the return and abort the loop if that is the case.
                 boolean doReturn = runStatement(loop.getBody());
                 if (doReturn) {
                     return true;
                 }
             }
             return false;
         }
 
         if (s instanceof Call) {
             runExpression((Call)s);
             return false;
         }
 
         if (s instanceof Return) {
             Return ret = (Return) s;
             // Some may be just a VOID return:
             if (ret.getResult() != null) {
             	Value val = runExpression(ret.getResult());
             	// NOTE: Static check is expected to fully
             	// cover the function return value;
 
                 //jz this is the old thing, don't use it in the new model 
             	setVarValue(ret.getTarget(), val);
 
                 //jz return value set
                 runtimeStack.getRecord().setReturn(val);
                 //jz return value set
             }
             // We need to indicate that we return from the method:
             return true;
         }
         
         if (s instanceof DeclContainer) {
             // We need to evaluate the expressions in the declarations, 
             // and also reserve the stack locations for these variables.
             evaluateDeclarations(((DeclContainer)s).getDeclarations());
             return false;
         }
         
         
         if (s instanceof Expression) { 
             runExpression((Expression)s);
             return false;
         }
         
         if (s instanceof ForEach) {
             ForEach forEach = (ForEach)s;
             Generator gen = forEach.getGenerator();
             Value listVal = runExpression(gen.getList());
             
             if (listVal.getType() != BaseType.LIST && listVal.getType() != BaseType.TUPLE) {
                 throw new InterpreterRuntimeError(gen.getList().getLineNum(), 
                         GENERATOR_EXPECTING_LIST, listVal.getType());
             }
             
             List<Value> vals = listVal.subListValue();
             for (int i = 0, size = vals.size(); i < size; i++) {
                 setGeneratorVariables(gen, vals.get(i));
                 boolean result = runStatement(forEach.getBody());
                 if (result) {
                     return true;
                 }
             }
             return false;
         }
 
         if (s instanceof Block) {
             // We just need to execute all the statements in the block; that's it:
             for (Statement subSt : ((Block)s).getMembers()) {
                 boolean result = runStatement(subSt);
                 if (result) {
                     return true;
                 }
             }
             return false;
         }
         // added later by MST to run the instantiation of a class
 	if (s instanceof MyObject) {
 	    MyObject obj = (MyObject)s;
 	    MyClass c = Util.findClass(prog.getClasses(), obj.getType());
 	    Constructor cons = c.getConstructor();
 	    List<Value> args = evaluateExpList(obj.getArgs());
 	    callConstructor(cons, args);
 	    return false; //dummy return
 	}
 
         /*
         if (s instanceof Skip) {
             return false;
         }
         */
         if (s instanceof ShowStack) {
             System.out.println("PRINTING STACK AT " + ((ShowStack)s).id);
             runtimeStack.printStack();
             return false;
         }
 
         throw new UnsupportedOperationException(
                 "Unrecognized Statement class: " + s.getClass());
     }
     
     private void evaluateDeclarations(List<Declaration> declarations)
         throws InterpreterRuntimeError
     {
         for (Declaration decl : declarations) {
             Value val = null;
             if (decl.getInitValue() != null) {
                 val = runExpression(decl.getInitValue());
             }
             setVarValue(decl.getVariable(), val);
         }
     }
 
     public Value runExpression(Expression exp) throws InterpreterRuntimeError {
         if (exp instanceof Value) {
             return (Value) exp;
         }
 
         if (exp instanceof Variable) {
             Variable var = (Variable) exp;
             return getVarValue(var);
         }
 
         if (exp instanceof Unary) {
             Unary un = (Unary) exp;
             Value val = runExpression(un.getTerm());
             return Util.applyUnary(un, val);
         }
 
         if (exp instanceof Binary) {
             Binary bin = (Binary) exp;
             Value val1 = runExpression(bin.getTerm1());
             Value val2 = runExpression(bin.getTerm2());
             return Util.applyBinary(bin, val1, val2);
         }
 
         if (exp instanceof ListTupleReference) {
             ListTupleReference ref = (ListTupleReference)exp;
             ListIndexPair pair = verifyListTupleRefGetIndex(ref);
             return pair.getValueOfElement();
         }
 
         if (exp instanceof ListTupleExpression) {
             ListTupleExpression ltExp = (ListTupleExpression)exp;
             List<Value> valMembers = evaluateExpList(ltExp.getMembers());
             
             return (ltExp.isTuple()) ? new TupleValue(valMembers) :  
                 new ListValue(valMembers);
         }
 
         if (exp instanceof ListComprehension) {
             return runListComprehension((ListComprehension)exp);
         }
 
         if (exp instanceof LambdaDef) {
             LambdaDef lambda = (LambdaDef)exp;
             // We need to create the Lambda context and initialize
             // the sourced variables:
             LambdaValue val = new LambdaValue(lambda, debug);
             initLambdaContext(lambda.getExpression(), val);
             /*
             lambdaStack.add(val);
             Value result = runExpression(lambda.getExpression());
             lambdaStack.remove(lambdaStack.size() - 1);
             */
             return val;
         }
 
         if (exp instanceof Call) {
             
             Call call = (Call) exp;
             List<Value> args = evaluateExpList(call.getArgs());
             BuiltinFunctions.Name bfName = call.getBuiltinFunctName();        
             if (bfName != null) {
                 return BuiltinFunctions.run(bfName, args);
             } else if (call.getFunction() != null) {
                 //jz create AR here and pass it in?
                 return callRealFunction(call, args);
 	    } else {
                 LambdaValue lambdaVal;
                 String name;
                 if (call.getLambda() != null) {
                     // If the call contains the Lambda, create the Lambda value
                     // right before evaluating the call:
                     lambdaVal = (LambdaValue)runExpression(call.getLambda());
                     name = "<LAMBDA>";
                 } else {
                     // Fetch the Lambda value from the Variable:
                     Value val = getVarValue(call.getVar());
 		    if(val instanceof FuncArgValue){ //added later
 			FuncArgValue newVal = (FuncArgValue)val;
 			Function methodToCall = Util.findFunction(prog.getFunctions(), newVal.getMethodName());
 			//did not check if parameters and return value match...should give runtime errors if there is a mismatch
 			//call.setFunction(methodToCall, 0);
 			call.setFunctWithoutOffset(methodToCall);
 			return callRealFunction(call, args);
 	 	    }
                     else if (val instanceof LambdaValue == false) {
                         throw new InterpreterRuntimeError(
                                 call.getLineNum(), EXPECTING_LAMBDA, val); //added later - this is what's causing the runtime error
                     }
                     lambdaVal = (LambdaValue)val;
                     name = call.getVar().getName();
                 }
                 
                 // Ok, we should have a Lambda Value by now. 
                 // All we need to do is to call it:
                 List<Variable> lambdaVars = lambdaVal.getLambda().getVars();
                 if (lambdaVars.size() != args.size()) {
                     throw new InterpreterRuntimeError(call.getLineNum(), INV_NUM_ARGS, 
                             name, lambdaVars.size(), args.size());
                 }
                 
                 // Ok, we need to put the Lambda Value into the stack:
                 lambdaStack.add(lambdaVal);
 
                 for (int i = 0, size = args.size(); i < size; i++) {
                     setVarValue(lambdaVars.get(i), args.get(i));
                 }
 
                 // Execute the actual Lambda Expression:
                 Value result = runExpression(lambdaVal.getLambda().getExpression());
                 // Remove the Lambda from the stack:
                 lambdaStack.remove(lambdaStack.size() - 1);
 
                 return result;            
             }
         }
 	
         // added later by MST - to run the first class function
 	if(exp instanceof FuncArg){
 		FuncArg funcArg = (FuncArg) exp;
 	        FuncArgValue val = new FuncArgValue(funcArg.getName(), funcArg.getArgs());
 		return val; 
 	}
 	
         // added later by MST - to run the object function
 	if (exp instanceof ObjFunction) {
 	    ObjFunction of = (ObjFunction)exp;
 	    List<Expression> unevaluated = of.getArgs();
 	    List<Value> args = evaluateExpList(unevaluated);
 	    return callOOFunction(of, args);
 	}
 	
         throw new UnsupportedOperationException(
                 "Found invalid expression class: " + exp.getClass());
     }
 
     public void initLambdaContext(Expression exp, LambdaValue inOutCtx) 
         throws InterpreterRuntimeError 
     {
         if (exp instanceof Value) {
             return;
         }
 
         if (exp instanceof Variable) {
             Variable var = (Variable) exp;
             myAssert(var.getVarType() == VarType.LAMBDA, 
                     "Found non-lambda variable within Lambda expression");
                 
             if (var.getLambdaSource() != null) {
                 // Copy the value from the source into the Context:
                 Value val = getVarValue(var.getLambdaSource());
                 inOutCtx.setCtxValue(var.getAddress(), var, val);
             }
             return;
         }
 
         if (exp instanceof Unary) {
             initLambdaContext(((Unary)exp).getTerm(), inOutCtx);
             return;
         }
 
         if (exp instanceof Binary) {
             Binary bin = (Binary) exp;
             initLambdaContext(bin.getTerm1(), inOutCtx);
             initLambdaContext(bin.getTerm2(), inOutCtx);
             return;
         }
 
         if (exp instanceof ListTupleReference) {
             ListTupleReference ref = (ListTupleReference)exp;
             // We just separately init. list variable and the index expression:
             initLambdaContext(ref.getListVar(), inOutCtx);
             initLambdaContext(ref.getIndex(), inOutCtx);
             return;
         }
 
         if (exp instanceof ListTupleExpression) {
             for (Expression member : ((ListTupleExpression)exp).getMembers()) {
                 initLambdaContext(member, inOutCtx);
             }
             return;
         }
 
         if (exp instanceof ListComprehension) {
             int lineNum = ((ListComprehension)exp).getLineNum();
             throw new InterpreterRuntimeError(lineNum, LIST_COMP_NOT_ALLOWED_IN_LAMBDA);
         }
 
         if (exp instanceof LambdaDef) {
             // Due to all the pain in the butt that it causes, the nested
             // Lambdas are currently not allowed:
             return;
         }
 
         if (exp instanceof Call) {
             // Just process the call's arguments:
             for (Expression arg : ((Call)exp).getArgs()) {
                 initLambdaContext(arg, inOutCtx);
             }
             return;
         }
 
         throw new UnsupportedOperationException(
                 "Found invalid expression class: " + exp.getClass());
     }
 
     
     private Value runListComprehension(ListComprehension lc)
         throws InterpreterRuntimeError
     {
         List<Value> result = new ArrayList<Value>();
         
         List<Generator> gens = lc.getGenerators();
         // First, we need to evaluate all the Generator expressions:
         List<?> [] listsRaw = new List<?>[gens.size()];
         
         @SuppressWarnings("unchecked")
         List<Value> [] lists = (List<Value>[])listsRaw;
         
         for (int i = 0; i < lists.length; i++)  {
             Generator gen = gens.get(i);
             Value val = runExpression(gen.getList());
             if (val.getType() != BaseType.LIST && val.getType() != BaseType.TUPLE) {
                 throw new InterpreterRuntimeError(gen.getList().getLineNum(), 
                         GENERATOR_EXPECTING_LIST, val.getType());
             }
             lists[i] = val.subListValue();
         }
         
         // Next, iterate over these evaluated lists:
         MultiIterator<Value> mit = new MultiIterator<Value>(lists);
         
         MAIN_LOOP: for (; mit.isValid(); mit.nextState()) {
             // Now, for each iteration we need to set the Generator Variables:
             for (int i = 0; i < lists.length; i++) {
                 setGeneratorVariables(gens.get(i), mit.getValue(i));
             }
             // And now we need to evaluate the conditionals:
             for (Expression exp : lc.getConditionals()) {
                 Value val = runExpression(exp);
                 if (val.getType() != BaseType.BOOL) {
                     throw new InterpreterRuntimeError(exp.getLineNum(),
                             LIST_COMP_CONDITIONAL, val.getType());
                 }
                 // If any conditional evaluates to false, abort the 
                 // current iteration of the loop:
                 if (val.boolValue() == false) {
                     continue MAIN_LOOP;
                 }
             }
             
             // Conditionals passed. Evaluate the main expression
             // and stick it into the result:
             Value mainVal = runExpression(lc.getTarget());
             result.add(mainVal);
         }
         
         return new ListValue(result);
     }
 
     
     private class ListIndexPair
     {
         private Value value;
         private int index;
         public ListIndexPair(Value listValue, int index)
         {
             this.index = index;
             this.value = listValue;
         }
         
         public Value getValueOfListOrTuple() {
             return value;
         }
         public int getIndex() {
             return index;
         }
         
         public Value getValueOfElement()
         {
             return value.subListValue().get(index);
         }
         
         public void setValueOfElement(Value element)
         {
             value.subListValue().set(index, element);
         }
     }
 
     private ListIndexPair verifyListTupleRefGetIndex(ListTupleReference listRef)
             throws InterpreterRuntimeError {
         Variable var = listRef.getListVar();
         Value val = getVarValue(var);
 
         if (val.getType() != BaseType.LIST && val.getType() != BaseType.TUPLE) {
             throw new InterpreterRuntimeError(listRef.getLineNum(), WRONG_LIST_TYPE, var.getName(),
                     val.getType());
         }
 
         Value indexVal = runExpression(listRef.getIndex());
         if (indexVal.getType() != BaseType.INT) {
             throw new InterpreterRuntimeError(listRef.getLineNum(), INDEX_MUST_BE_INTEGER, 
                     indexVal.getType(), BaseType.INT);
         }
         
         int index = indexVal.intValue();
 
         if (index < 0 || index >= val.subListValue().size()) {
             throw new InterpreterRuntimeError(listRef.getLineNum(), INDEX_OUT_OF_BOUNDS, index, var
                     .getName(), val.subListValue().size());
         }
 
         // Finally, we can access the value:
         return new ListIndexPair(val, index);
     }
 
     private List<Value> evaluateExpList(List<Expression> members)
             throws InterpreterRuntimeError {
         List<Value> result = new ArrayList<Value>(members.size());
 
         for (Expression exp : members) {
             result.add(runExpression(exp));
         }
         return result;
     }
     
     /**
      * Assigns the values of the variables that are defined
      * by the given generator.
      * 
      * @param gen
      * @param index
      */
     public void setGeneratorVariables(Generator gen, Value val)
         throws InterpreterRuntimeError
     {
         if (gen.getVar() != null) {
             setVarValue(gen.getVar(), val);
             return;
         }
         
         // Now, the complicated case:
         List<Variable> vars = gen.getVarTuple();
         
         if (val.getType() != BaseType.TUPLE && val.getType() != BaseType.LIST) {
             throw new InterpreterRuntimeError(gen.getList().getLineNum(), 
                     GENERATOR_EXP_LIST_TUPLE, val);
         }
         
         List<Value> members = val.subListValue();
         
         if (members.size() != vars.size()) {
             throw new InterpreterRuntimeError(gen.getList().getLineNum(), 
                     GENERATOR_INV_NUM, vars.size(), members.size());
         }
         
         for (int i = 0, size = vars.size(); i < size; i++) {
             Variable var = vars.get(i);
             if (var == null) {
                 continue;
             }
             
             setVarValue(var, members.get(i));
         }
     }
 
     public void setVarValue(Variable var, Value value) {
         int address = var.getAddress();    
         
         switch (var.getVarType()) {
         case GLOBAL:
             if (debug) {
                 debugStack[address] = var;
             }
             stack[address] = value;
             return;
         case LOCAL:
             if (debug) {
                 debugStack[basePtr + address] = var;
             }
             stack[basePtr + address] = value;
 
             //jz new stack stuff here
             ActivationRecord currentRecord = runtimeStack.getRecord();
             if(currentRecord == null){
                 System.out.println("try to add local to ar, stack returned null");
                 System.out.println(var.toString() + value.toString());
             }
             currentRecord.addVarValue(var, value);

            
            //System.out.println("stack looks like:");
            //runtimeStack.printStack();
             //jz new stack stuff here
             return;
 
         // added later by MST - to take care of instance variables 
 	case INSTANCE: 
 	    stack[basePtr + address] = value; 
             return;
         case LAMBDA:
             LambdaValue lastLambda = lambdaStack.get(lambdaStack.size() - 1);
             lastLambda.setCtxValue(address, var,  value);
             break;
         default:
             throw new UnsupportedOperationException("Invalid Var Type: "
                     + var.getVarType());
         }
     }
     
 
     public Value getVarValue(Variable var) 
         throws InterpreterRuntimeError
     {
         int address = var.getAddress();
         Value val;
 
         switch (var.getVarType()) {
         case GLOBAL:
             val =  stack[address];
             break;
         case LOCAL:
             val =  stack[basePtr + address];
             break;
         case LAMBDA:
             LambdaValue lastLambda = lambdaStack.get(lambdaStack.size() - 1);
             val = lastLambda.getCtxValue(address);
             break;
         // added later by MST - to take care of instance variables
         case INSTANCE:
             val = stack[basePtr + address];
             break;
         default:
             throw new UnsupportedOperationException("Invalid Var Type: "
                     + var.getVarType());
         }
         
         if (val == null) {
             throw new InterpreterRuntimeError(var.getLineNum(), USED_NOT_INIT, var.getName());
         }
         
         return val;
     }
 
     
     public static class LambdaValue extends Value {
         private LambdaDef def;
         private Value[] context;
         private Variable[] dbgContext;
         
         public LambdaValue(LambdaDef def, boolean debug)
         {
             this.def = def;
             context = new Value[def.getContextSize()];
             if (debug) {
                 dbgContext = new Variable[def.getContextSize()];
             }
         }
 
         public Value getCtxValue(int address) {
             return context[address];
         }
         
         public void setCtxValue(int address, Variable var, Value value)
         {
             context[address] = value;
             if (dbgContext != null) {
                 dbgContext[address] = var;
             }
         }
         
         public LambdaDef getLambda()
         {
             return def;
         }
         
 
         @Override
         public Type getType() {
             return def.getProtoType();
         }
     }
 }
