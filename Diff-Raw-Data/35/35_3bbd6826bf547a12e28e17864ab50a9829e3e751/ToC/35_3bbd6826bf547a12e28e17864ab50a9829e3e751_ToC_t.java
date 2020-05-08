 /**
  * 
  */
 package interpreter;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import node.*;
 import analysis.DepthFirstAdapter;
 
 /**
  * @author Benjamin Arnold, Felix Hoeborn
  * 
  */
 public class ToC extends DepthFirstAdapter {
 	public StringBuffer output = new StringBuffer();
 
 	private StructState currentStruct = null;
 
 	Set<String> includes = new HashSet<String>();
 	Map<String, AFunctionFunction> functions = new HashMap<String, AFunctionFunction>();
 	Map<String, AStructStruct> classes = new HashMap<String, AStructStruct>();
 	// Map<String, AVardef> globalVariables = new HashMap<String, V>
 
 	private static final String CONST_INCLUDE = "#include";
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAIncludeProgram(node.AIncludeProgram)
 	 */
 	@Override
 	public void caseAIncludeProgram(AIncludeProgram node) {
 		includes.add(node.getId().getText());
 
 		// generate Code
 		output.append(CONST_INCLUDE);
 		output.append(" ");
 		output.append("<");
 		output.append(node.getId().getText());
 		output.append(".h>\n");
 
 		// consume the rest
 		super.caseAIncludeProgram(node);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseADefineProgram(node.ADefineProgram)
 	 */
 	@Override
 	public void caseADefineProgram(ADefineProgram node) {
 		// TODO Auto-generated method stub
 		super.caseADefineProgram(node);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAStructProgram(node.AStructProgram)
 	 */
 	@Override
 	public void caseAStructProgram(AStructProgram node) {
 		// TODO Auto-generated method stub
 		super.caseAStructProgram(node);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * analysis.DepthFirstAdapter#caseAFunctionProgram(node.AFunctionProgram)
 	 */
 	@Override
 	public void caseAFunctionProgram(AFunctionProgram node) {
 		// TODO Auto-generated method stub
 		super.caseAFunctionProgram(node);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAVarDefine(node.AVarDefine)
 	 */
 	@Override
 	public void caseAVarDefine(AVarDefine node) {
 		super.caseAVarDefine(node);
 		output.append(' ');
 		output.append(node.getId().getText());
 		output.append(';');
 
 		if (currentStruct != null) {
 			output.append("\n};");
 		}
 		output.append('\n');
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAVarSetDefine(node.AVarSetDefine)
 	 */
 	@Override
 	public void caseAVarSetDefine(AVarSetDefine node) {
		node.getType().apply(this);
		output.append(' ');
		node.getSet().apply(this);
 	}
 
 	private static final String CONST_STRUCT = "struct";
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAStructStruct(node.AStructStruct)
 	 */
 	@Override
 	public void caseAStructStruct(AStructStruct node) {
 		String id = node.getId().getText();
 		currentStruct = new StructState(node);
 
 		this.classes.put(id, node);
 
 		// generate Code
 		output.append(CONST_STRUCT);
 		output.append(" ");
 		output.append(id);
 		output.append("\n{\n");
 
 		node.getStructBody().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAEndStructBody(node.AEndStructBody)
 	 */
 	@Override
 	public void caseAEndStructBody(AEndStructBody node) {
 		if (currentStruct == null)
 			throw new SemanticException("No Struct to end here!");
 
 		String currentID = currentStruct.getStruct().getId().getText();

		if (!currentStruct.isConstrCreated()) {

 		}

 		output.append(CONST_VOID);
 		output.append(" destroy_");
 		output.append(currentID);
 		output.append('(');
 		output.append(currentID);
 		output.append("* this) {\nfree(this);\n}\n");

 		output.append("// END STRUCT ");
 		output.append(currentID);
 		output.append('\n');
 
 		currentStruct = null;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * analysis.DepthFirstAdapter#caseADefineStructBody(node.ADefineStructBody)
 	 */
 	@Override
 	public void caseADefineStructBody(ADefineStructBody node) {
 		if (currentStruct == null)
 			throw new SemanticException(
 					"No beginning Struct, why should there be a body? There are no bodies hidden here...");
 
 		// process rest
 		node.getDefine().apply(this);
 		node.getStructBody().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAConstructorStructBody(node.
 	 * AConstructorStructBody)
 	 */
 	@Override
 	public void caseAConstructorStructBody(AConstructorStructBody node) {
 		node.getConstructor().apply(this);
 
 		super.caseAConstructorStructBody(node);
 	}
 
 	private final static String CONST_VOID = "void";
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * analysis.DepthFirstAdapter#caseAConsConstructor(node.AConsConstructor)
 	 */
 	@Override
 	public void caseAConsConstructor(AConsConstructor node) {
 		if (currentStruct == null)
 			throw new SemanticException("No parent struct!");
 
 		String currentID = currentStruct.getStruct().getId().getText();
 
 		output.append(currentID);
 		output.append("* ");
 		output.append("new_");
 		output.append(currentID);
 		output.append('(');
 		node.getParam().apply(this);
 		output.append(")\n{\n");
 
 		// Allocation
 		output.append(currentID);
 		output.append("* this = (");
 		output.append(currentID);
 		output.append("*) malloc(sizeof(");
 		output.append(currentID);
 		output.append(");\n");
 
 		node.getImpl().apply(this);
 		output.append("return this;");
 		output.append("\n}\n");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * analysis.DepthFirstAdapter#caseAFunctionFunction(node.AFunctionFunction)
 	 */
 	@Override
 	public void caseAFunctionFunction(AFunctionFunction node) {
 		this.functions.put(node.getId().getText(), node);
 
 		// generate code
 		node.getType().apply(this);
 		output.append(' ');
 		output.append(node.getId().getText());
 		output.append('(');
 		node.getParam().apply(this);
 		output.append(")\n{\n");
		node.getImpl().apply(this);
		output.append("\n}\n");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAEndParam(node.AEndParam)
 	 */
 	@Override
 	public void caseAEndParam(AEndParam node) {
 		// TODO Auto-generated method stub
 		super.caseAEndParam(node);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAOneParam(node.AOneParam)
 	 */
 	@Override
 	public void caseAOneParam(AOneParam node) {
 		node.getType().apply(this);
 		output.append(' ');
 		output.append(node.getId().getText());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAAnotherParam(node.AAnotherParam)
 	 */
 	@Override
	public void caseAAnotherParam(AAnotherParam node) {		
		node.getType().apply(this);
		output.append(' ');
 		output.append(node.getId().getText());
		output.append(", ");
 		node.getParam().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAEndImpl(node.AEndImpl)
 	 */
 	@Override
 	public void caseAEndImpl(AEndImpl node) {
 		// TODO Auto-generated method stub
 		super.caseAEndImpl(node);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAReturnImpl(node.AReturnImpl)
 	 */
 	@Override
 	public void caseAReturnImpl(AReturnImpl node) {
 		// TODO Auto-generated method stub
 		super.caseAReturnImpl(node);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAExprImpl(node.AExprImpl)
 	 */
 	@Override
 	public void caseAExprImpl(AExprImpl node) {
 		// TODO Auto-generated method stub
 		super.caseAExprImpl(node);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseADefineImpl(node.ADefineImpl)
 	 */
 	@Override
 	public void caseADefineImpl(ADefineImpl node) {
 		node.getDefine().apply(this);
 		node.getImpl().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAOperationExpr(node.AOperationExpr)
 	 */
 	@Override
 	public void caseAOperationExpr(AOperationExpr node) {
 		// TODO Auto-generated method stub
 		super.caseAOperationExpr(node);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAFuncExpr(node.AFuncExpr)
 	 */
 	@Override
 	public void caseAFuncExpr(AFuncExpr node) {
 		node.getFunc().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseALogicExpr(node.ALogicExpr)
 	 */
 	@Override
 	public void caseALogicExpr(ALogicExpr node) {
 		node.getLogic().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseATermExpr(node.ATermExpr)
 	 */
 	@Override
 	public void caseATermExpr(ATermExpr node) {
 		node.getTerm().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseASetExpr(node.ASetExpr)
 	 */
 	@Override
 	public void caseASetExpr(ASetExpr node) {
 		node.getSet().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseASetSet(node.ASetSet)
 	 */
 	@Override
 	public void caseASetSet(ASetSet node) {
 		if (currentStruct != null) {
 			output.append("this.");
 		}
 
 		output.append(node.getId().getText());
 		output.append(" = ");
 		node.getTerm().apply(this);
 		output.append(";\n");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAFuncFunc(node.AFuncFunc)
 	 */
 	@Override
 	public void caseAFuncFunc(AFuncFunc node) {
 		output.append(node.getId().getText());
 		output.append('(');
 		node.getFuncPara().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAEndFuncPara(node.AEndFuncPara)
 	 */
 	@Override
 	public void caseAEndFuncPara(AEndFuncPara node) {
 		output.append(')');
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAOneFuncPara(node.AOneFuncPara)
 	 */
 	@Override
 	public void caseAOneFuncPara(AOneFuncPara node) {
 		node.getTerm().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * analysis.DepthFirstAdapter#caseAAnotherFuncPara(node.AAnotherFuncPara)
 	 */
 	@Override
 	public void caseAAnotherFuncPara(AAnotherFuncPara node) {
 		output.append(", ");
 		node.getTerm().apply(this);
 		node.getFuncPara().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAIfLogic(node.AIfLogic)
 	 */
 	@Override
 	public void caseAIfLogic(AIfLogic node) {
 		// TODO Auto-generated method stub
 		super.caseAIfLogic(node);
 	}
 
 	private static final String CONST_IF = "if";
 	private static final String CONST_ELSE = "else";
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAIfelseLogic(node.AIfelseLogic)
 	 */
 	@Override
 	public void caseAIfelseLogic(AIfelseLogic node) {
 		output.append(CONST_IF);
 		output.append('(');
 		node.getQuery().apply(this);
 		output.append(')');
 		output.append("\n{\n");
 		node.getIf().apply(this);
 		output.append("\n}\n");
 		output.append(CONST_ELSE);
 		output.append("\n{\n");
 		node.getElse().apply(this);
 		output.append("\n}\n");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseASameOperation(node.ASameOperation)
 	 */
 	@Override
 	public void caseASameOperation(ASameOperation node) {
 		node.getLeft().apply(this);
 		output.append(" == ");
 		node.getRight().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAPlusOperation(node.APlusOperation)
 	 */
 	@Override
 	public void caseAPlusOperation(APlusOperation node) {
 		node.getLeft().apply(this);
 		output.append(" + ");
 		node.getRight().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAMinusOperation(node.AMinusOperation)
 	 */
 	@Override
 	public void caseAMinusOperation(AMinusOperation node) {
 		node.getLeft().apply(this);
 		output.append(" - ");
 		node.getRight().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAMultOperation(node.AMultOperation)
 	 */
 	@Override
 	public void caseAMultOperation(AMultOperation node) {
 		node.getLeft().apply(this);
 		output.append(" * ");
 		node.getRight().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseADivOperation(node.ADivOperation)
 	 */
 	@Override
 	public void caseADivOperation(ADivOperation node) {
 		node.getLeft().apply(this);
 		output.append(" / ");
 		node.getRight().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAModOperation(node.AModOperation)
 	 */
 	@Override
 	public void caseAModOperation(AModOperation node) {
 		node.getLeft().apply(this);
 		output.append(" % ");
 		node.getRight().apply(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAExprTerm(node.AExprTerm)
 	 */
 	@Override
 	public void caseAExprTerm(AExprTerm node) {
 		output.append(node.getLPar().getText());
 		node.getExpr().apply(this);
 		output.append(node.getRPar().getText());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAIdTerm(node.AIdTerm)
 	 */
 	@Override
 	public void caseAIdTerm(AIdTerm node) {
 		output.append(node.getId().getText());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseANumberTerm(node.ANumberTerm)
 	 */
 	@Override
 	public void caseANumberTerm(ANumberTerm node) {
 		output.append(node.getNumber());
 	}
 
 	private static final String CONST_NULL = "NULL";
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseANullTerm(node.ANullTerm)
 	 */
 	@Override
 	public void caseANullTerm(ANullTerm node) {
 		output.append(CONST_NULL);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseABoolTerm(node.ABoolTerm)
 	 */
 	@Override
 	public void caseABoolTerm(ABoolTerm node) {
 		TBool value = node.getBool();
 		int compiledValue;
 		if (value.getText().equals("false")) {
 			compiledValue = 0;
 		} else {
 			compiledValue = 1;
 		}
 
 		output.append(compiledValue);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseAVoidType(node.AVoidType)
 	 */
 	@Override
 	public void caseAVoidType(AVoidType node) {
 		output.append(CONST_VOID);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see analysis.DepthFirstAdapter#caseATypeType(node.ATypeType)
 	 */
 	@Override
 	public void caseATypeType(ATypeType node) {
 		output.append(node.getId().getText());
 	}
 
 	//
 	//
 	//
 	// /**
 	// * @param program
 	// */
 	// private void casePProgram(PProgram program) {
 	// if (program instanceof ADefineProgram) {
 	// caseADefineProgram((ADefineProgram) program);
 	// } else if (program instanceof AEofProgram) {
 	// caseAEofProgram((AEofProgram) program);
 	// } else if (program instanceof AFunctionProgram) {
 	// caseAFunctionProgram((AFunctionProgram) program);
 	// } else {
 	//
 	// }
 	//
 	// }
 	//
 	// /**
 	// * @param define
 	// */
 	// private void casePDefine(PDefine define) {
 	// if (define instanceof AConsDefine) {
 	// caseAConsDefine((AConsDefine) define);
 	// } else if (define instanceof ADefineDefine) {
 	// caseADefineDefine((ADefineDefine) define);
 	// } else if (define instanceof AFunctionDefine) {
 	// caseAFunctionDefine((AFunctionDefine) define);
 	// } else if (define instanceof ANilDefine) {
 	// caseANilDefine((ANilDefine) define);
 	// } else {
 	//
 	// }
 	// }
 	//
 	// /**
 	// * @param function
 	// */
 	// private void casePFunction(PFunction function) {
 	// if (function instanceof AFuncCallFunction) {
 	// caseAFuncCallFunction((AFuncCallFunction) function);
 	// } else {
 	//
 	// }
 	// }
 	//
 	// /**
 	// * @param expr
 	// */
 	// private void casePExpr(PExpr expr) {
 	// if (expr instanceof AConcatExpr) {
 	// caseAConcatExpr((AConcatExpr) expr);
 	// } else if (expr instanceof ADivExpr) {
 	// caseADivExpr((ADivExpr) expr);
 	// } else if (expr instanceof AEndExpr) {
 	// caseAEndExpr((AEndExpr) expr);
 	// } else if (expr instanceof AFunctionExpr) {
 	// caseAFunctionExpr((AFunctionExpr) expr);
 	// } else if (expr instanceof AIfExpr) {
 	// caseAIfExpr((AIfExpr) expr);
 	// } else if (expr instanceof AMinusExpr) {
 	// caseAMinusExpr((AMinusExpr) expr);
 	// } else if (expr instanceof AModExpr) {
 	// caseAModExpr((AModExpr) expr);
 	// } else if (expr instanceof AMultExpr) {
 	// caseAMultExpr((AMultExpr) expr);
 	// } else if (expr instanceof APlusExpr) {
 	// caseAPlusExpr((APlusExpr) expr);
 	// } else if (expr instanceof ATermExpr) {
 	// caseATermExpr((ATermExpr) expr);
 	// } else if (expr instanceof ANativcExpr) {
 	// caseANativcExpr((ANativcExpr) expr);
 	// } else {
 	//
 	// }
 	// }
 	//
 	// /**
 	// * @param term
 	// */
 	// private void casePTerm(PTerm term) {
 	// if (term instanceof AExprTerm) {
 	// caseAExprTerm((AExprTerm) term);
 	// } else if (term instanceof AIdTerm) {
 	// caseAIdTerm((AIdTerm) term);
 	// } else if (term instanceof ANullTerm) {
 	// caseANullTerm((ANullTerm) term);
 	// } else if (term instanceof ANumberTerm) {
 	// caseANumberTerm((ANumberTerm) term);
 	// } else if (term instanceof ABoolTerm) {
 	// caseABoolTerm((ABoolTerm)term);
 	// } else {
 	//
 	// }
 	// }
 	//
 	// /**
 	// * @param impl
 	// */
 	// private void casePImpl(PImpl impl) {
 	// if (impl instanceof AEndImpl) {
 	// caseAEndImpl((AEndImpl) impl);
 	// } else if (impl instanceof ADefineImpl) {
 	// caseADefineImpl((ADefineImpl) impl);
 	// } else if (impl instanceof AExprImpl) {
 	// caseAExprImpl((AExprImpl) impl);
 	// } else if (impl instanceof AReturnImpl) {
 	// caseAReturnImpl((AReturnImpl) impl);
 	// } else {
 	//
 	// }
 	// }
 	//
 	// //////Production//////
 	//
 	//
 	// /* (non-Javadoc)
 	// * @see analysis.DepthFirstAdapter#caseANativcExpr(node.ANativcExpr)
 	// */
 	// @Override
 	// public void caseANativcExpr(ANativcExpr node) {
 	// // TODO Auto-generated method stub
 	// super.caseANativcExpr(node);
 	// }
 	//
 	//
 	// /* (non-Javadoc)
 	// * @see analysis.DepthFirstAdapter#caseABoolTerm(node.ABoolTerm)
 	// */
 	// @Override
 	// public void caseABoolTerm(ABoolTerm node) {
 	// System.out.print(node.getBool().getText());;
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseADefineProgram(node.ADefineProgram)
 	// */
 	// @Override
 	// public void caseADefineProgram(ADefineProgram node) {
 	// casePDefine(node.getDefine());
 	// casePProgram(node.getProgram());
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see
 	// * analysis.DepthFirstAdapter#caseAFunctionProgram(node.AFunctionProgram)
 	// */
 	// @Override
 	// public void caseAFunctionProgram(AFunctionProgram node) {
 	// casePFunction(node.getFunction());
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseADefineDefine(node.ADefineDefine)
 	// */
 	// @Override
 	// public void caseADefineDefine(ADefineDefine node) {
 	// System.out.print(node.getId().getText() + " = ");
 	// casePTerm(node.getTerm());
 	// System.out.println(";");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see
 	// analysis.DepthFirstAdapter#caseAFunctionDefine(node.AFunctionDefine)
 	// */
 	// @Override
 	// public void caseAFunctionDefine(AFunctionDefine node) {
 	// System.out.print(node.getReturn().getText() + " "
 	// + node.getName().getText() + "(");
 	// casePTerm(node.getTerm());
 	// System.out.println("){");
 	// casePImpl(node.getImpl());
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAConsDefine(node.AConsDefine)
 	// */
 	// @Override
 	// public void caseAConsDefine(AConsDefine node) {
 	// System.out.print(node.getId().getText() + "(");
 	// casePTerm(node.getTerm());
 	// System.out.println("){");
 	// casePImpl(node.getImpl());
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseANilDefine(node.ANilDefine)
 	// */
 	// @Override
 	// public void caseANilDefine(ANilDefine node) {
 	// System.out.println(node.getId().getText() + "(){");
 	// casePImpl(node.getImpl());
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAEndImpl(node.AEndImpl)
 	// */
 	// @Override
 	// public void caseAEndImpl(AEndImpl node) {
 	// System.out.println("}");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAReturnImpl(node.AReturnImpl)
 	// */
 	// @Override
 	// public void caseAReturnImpl(AReturnImpl node) {
 	// System.out.print("return ");
 	// casePExpr(node.getExpr());
 	// System.out.println(" ;\n}");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAExprImpl(node.AExprImpl)
 	// */
 	// @Override
 	// public void caseAExprImpl(AExprImpl node) {
 	// casePExpr(node.getExpr());
 	// System.out.println(";");
 	// casePImpl(node.getImpl());
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseADefineImpl(node.ADefineImpl)
 	// */
 	// @Override
 	// public void caseADefineImpl(ADefineImpl node) {
 	// casePDefine(node.getDefine());
 	// System.out.println(";");
 	// casePImpl(node.getImpl());
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAEndExpr(node.AEndExpr)
 	// */
 	// @Override
 	// public void caseAEndExpr(AEndExpr node) {
 	//
 	// System.out.println("");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseATermExpr(node.ATermExpr)
 	// */
 	// @Override
 	// public void caseATermExpr(ATermExpr node) {
 	// casePTerm(node.getTerm());
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAFunctionExpr(node.AFunctionExpr)
 	// */
 	// @Override
 	// public void caseAFunctionExpr(AFunctionExpr node) {
 	// casePFunction(node.getFunction());
 	// System.out.println("");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAPlusExpr(node.APlusExpr)
 	// */
 	// @Override
 	// public void caseAPlusExpr(APlusExpr node) {
 	// System.out.print("( ");
 	// casePExpr(node.getExpr());
 	// System.out.print(" + ");
 	// casePTerm(node.getTerm());
 	// System.out.print(" )");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAMinusExpr(node.AMinusExpr)
 	// */
 	// @Override
 	// public void caseAMinusExpr(AMinusExpr node) {
 	// System.out.print("( ");
 	// casePExpr(node.getExpr());
 	// System.out.print(" - ");
 	// casePTerm(node.getTerm());
 	// System.out.print(" )");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAMultExpr(node.AMultExpr)
 	// */
 	// @Override
 	// public void caseAMultExpr(AMultExpr node) {
 	// System.out.print("( ");
 	// casePExpr(node.getExpr());
 	// System.out.print(" * ");
 	// casePTerm(node.getTerm());
 	// System.out.print(" )");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseADivExpr(node.ADivExpr)
 	// */
 	// @Override
 	// public void caseADivExpr(ADivExpr node) {
 	// System.out.print("( ");
 	// casePExpr(node.getExpr());
 	// System.out.print(" / ");
 	// casePTerm(node.getTerm());
 	// System.out.print(" )");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAModExpr(node.AModExpr)
 	// */
 	// @Override
 	// public void caseAModExpr(AModExpr node) {
 	// System.out.print("( ");
 	// casePExpr(node.getExpr());
 	// System.out.print(" % ");
 	// casePTerm(node.getTerm());
 	// System.out.print(" )");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAIfExpr(node.AIfExpr)
 	// */
 	// @Override
 	// public void caseAIfExpr(AIfExpr node) {
 	// System.out.print("if( ");
 	// casePExpr(node.getIf());
 	// System.out.println(" ){");
 	// casePImpl(node.getThen());
 	// System.out.println("\n else { ");
 	// casePImpl(node.getElse());
 	// System.out.println("");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAConcatExpr(node.AConcatExpr)
 	// */
 	// @Override
 	// public void caseAConcatExpr(AConcatExpr node) {
 	// System.out.print("( ");
 	// casePExpr(node.getLeft());
 	// System.out.print(" , ");
 	// casePTerm(node.getRight());
 	// System.out.print(" )");
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see
 	// *
 	// analysis.DepthFirstAdapter#caseAFuncCallFunction(node.AFuncCallFunction)
 	// */
 	// @Override
 	// public void caseAFuncCallFunction(AFuncCallFunction node) {
 	// System.out.print(node.getId().getText()+"( ");
 	// casePExpr(node.getExpr());
 	// System.out.println(" )");
 	// }
 	//
 	// //////Token//////
 	//
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAIdTerm(node.AIdTerm)
 	// */
 	// @Override
 	// public void caseAIdTerm(AIdTerm node) {
 	// // TODO Auto-generated method stub
 	// super.caseAIdTerm(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseANumberTerm(node.ANumberTerm)
 	// */
 	// @Override
 	// public void caseANumberTerm(ANumberTerm node) {
 	// // TODO Auto-generated method stub
 	// super.caseANumberTerm(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseANullTerm(node.ANullTerm)
 	// */
 	// @Override
 	// public void caseANullTerm(ANullTerm node) {
 	// // TODO Auto-generated method stub
 	// super.caseANullTerm(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAExprTerm(node.AExprTerm)
 	// */
 	// @Override
 	// public void caseAExprTerm(AExprTerm node) {
 	// // TODO Auto-generated method stub
 	// super.caseAExprTerm(node);
 	// }
 	//
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTCall(node.TCall)
 	// */
 	// @Override
 	// public void caseTCall(TCall node) {
 	// // TODO Auto-generated method stub
 	// super.caseTCall(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseAEofProgram(node.AEofProgram)
 	// */
 	// @Override
 	// public void caseAEofProgram(AEofProgram node) {
 	// System.out.println("Hier endet der Code");
 	// super.caseAEofProgram(node);
 	// }
 	//
 	// /* (non-Javadoc)
 	// * @see analysis.AnalysisAdapter#caseTCcode(node.TCcode)
 	// */
 	// @Override
 	// public void caseTCcode(TCcode node) {
 	// // TODO Auto-generated method stub
 	// super.caseTCcode(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTDefine(node.TDefine)
 	// */
 	// @Override
 	// public void caseTDefine(TDefine node) {
 	//
 	// super.caseTDefine(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTFunc(node.TFunc)
 	// */
 	// @Override
 	// public void caseTFunc(TFunc node) {
 	// System.out.print("func ");
 	// super.caseTFunc(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTBegin(node.TBegin)
 	// */
 	// @Override
 	// public void caseTBegin(TBegin node) {
 	// System.out.println("begin");
 	// super.caseTBegin(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTEnd(node.TEnd)
 	// */
 	// @Override
 	// public void caseTEnd(TEnd node) {
 	// System.out.println("end");
 	// super.caseTEnd(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTCons(node.TCons)
 	// */
 	// @Override
 	// public void caseTCons(TCons node) {
 	// System.out.print("cons ");
 	// super.caseTCons(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTWhere(node.TWhere)
 	// */
 	// @Override
 	// public void caseTWhere(TWhere node) {
 	// System.out.print(" where ");
 	// super.caseTWhere(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTBreak(node.TBreak)
 	// */
 	// @Override
 	// public void caseTBreak(TBreak node) {
 	// System.out.println("break");
 	// super.caseTBreak(node);
 	// }
 	//
 	//
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTReturnt(node.TReturnt)
 	// */
 	// @Override
 	// public void caseTReturnt(TReturnt node) {
 	// System.out.print("return ");
 	// super.caseTReturnt(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTNull(node.TNull)
 	// */
 	// @Override
 	// public void caseTNull(TNull node) {
 	// System.out.print("null");
 	// super.caseTNull(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTId(node.TId)
 	// */
 	// @Override
 	// public void caseTId(TId node) {
 	// System.out.print(node.getText());
 	// super.caseTId(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTNumber(node.TNumber)
 	// */
 	// @Override
 	// public void caseTNumber(TNumber node) {
 	// System.out.print(node.getText());
 	// super.caseTNumber(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTPlus(node.TPlus)
 	// */
 	// @Override
 	// public void caseTPlus(TPlus node) {
 	// System.out.print(" + ");
 	// super.caseTPlus(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTHyphen(node.THyphen)
 	// */
 	// @Override
 	// public void caseTHyphen(THyphen node) {
 	// System.out.print(" - ");
 	// super.caseTHyphen(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTAsterisk(node.TAsterisk)
 	// */
 	// @Override
 	// public void caseTAsterisk(TAsterisk node) {
 	// System.out.print("*");
 	// super.caseTAsterisk(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTPercent(node.TPercent)
 	// */
 	// @Override
 	// public void caseTPercent(TPercent node) {
 	// System.out.print("%");
 	// super.caseTPercent(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTDot(node.TDot)
 	// */
 	// @Override
 	// public void caseTDot(TDot node) {
 	// System.out.print(".");
 	// super.caseTDot(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTComma(node.TComma)
 	// */
 	// @Override
 	// public void caseTComma(TComma node) {
 	// System.out.print(", ");
 	// super.caseTComma(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTQuery(node.TQuery)
 	// */
 	// @Override
 	// public void caseTQuery(TQuery node) {
 	// System.out.print(" ? ");
 	// super.caseTQuery(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTColon(node.TColon)
 	// */
 	// @Override
 	// public void caseTColon(TColon node) {
 	// System.out.print(" : ");
 	// super.caseTColon(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTSemicolon(node.TSemicolon)
 	// */
 	// @Override
 	// public void caseTSemicolon(TSemicolon node) {
 	// System.out.println(";");
 	// super.caseTSemicolon(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTLBox(node.TLBox)
 	// */
 	// @Override
 	// public void caseTLBox(TLBox node) {
 	// System.out.print("[");
 	// super.caseTLBox(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTRBox(node.TRBox)
 	// */
 	// @Override
 	// public void caseTRBox(TRBox node) {
 	// System.out.print("]");
 	// super.caseTRBox(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTLPar(node.TLPar)
 	// */
 	// @Override
 	// public void caseTLPar(TLPar node) {
 	// System.out.print("(");
 	// super.caseTLPar(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTRPar(node.TRPar)
 	// */
 	// @Override
 	// public void caseTRPar(TRPar node) {
 	// System.out.println(")");
 	// super.caseTRPar(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTLBrace(node.TLBrace)
 	// */
 	// @Override
 	// public void caseTLBrace(TLBrace node) {
 	// System.out.print("{");
 	// super.caseTLBrace(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTRBrace(node.TRBrace)
 	// */
 	// @Override
 	// public void caseTRBrace(TRBrace node) {
 	// System.out.println("}");
 	// super.caseTRBrace(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTLArrow(node.TLArrow)
 	// */
 	// @Override
 	// public void caseTLArrow(TLArrow node) {
 	// System.out.print("<");
 	// super.caseTLArrow(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTRArrow(node.TRArrow)
 	// */
 	// @Override
 	// public void caseTRArrow(TRArrow node) {
 	// System.out.print(">");
 	// super.caseTRArrow(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTBlank(node.TBlank)
 	// */
 	// @Override
 	// public void caseTBlank(TBlank node) {
 	// System.out.print(" ");
 	// super.caseTBlank(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTDash(node.TDash)
 	// */
 	// @Override
 	// public void caseTDash(TDash node) {
 	// System.out.print("-");
 	// super.caseTDash(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTAmpersand(node.TAmpersand)
 	// */
 	// @Override
 	// public void caseTAmpersand(TAmpersand node) {
 	// System.out.print("&");
 	// super.caseTAmpersand(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTSlash(node.TSlash)
 	// */
 	// @Override
 	// public void caseTSlash(TSlash node) {
 	// System.out.print("/");
 	// super.caseTSlash(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTQuestion(node.TQuestion)
 	// */
 	// @Override
 	// public void caseTQuestion(TQuestion node) {
 	// System.out.print("?");
 	// super.caseTQuestion(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTExclamation(node.TExclamation)
 	// */
 	// @Override
 	// public void caseTExclamation(TExclamation node) {
 	// System.out.print("!");
 	// super.caseTExclamation(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTAt(node.TAt)
 	// */
 	// @Override
 	// public void caseTAt(TAt node) {
 	// System.out.print("@");
 	// super.caseTAt(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTTilde(node.TTilde)
 	// */
 	// @Override
 	// public void caseTTilde(TTilde node) {
 	// System.out.print("");
 	// super.caseTTilde(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTSection(node.TSection)
 	// */
 	// @Override
 	// public void caseTSection(TSection node) {
 	// System.out.print("");
 	// System.out.print("");
 	// super.caseTSection(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTUnderscore(node.TUnderscore)
 	// */
 	// @Override
 	// public void caseTUnderscore(TUnderscore node) {
 	// System.out.print("_");
 	// super.caseTUnderscore(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTPipe(node.TPipe)
 	// */
 	// @Override
 	// public void caseTPipe(TPipe node) {
 	// System.out.print("|");
 	// super.caseTPipe(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTHash(node.THash)
 	// */
 	// @Override
 	// public void caseTHash(THash node) {
 	// System.out.println("");
 	// super.caseTHash(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTCaret(node.TCaret)
 	// */
 	// @Override
 	// public void caseTCaret(TCaret node) {
 	// System.out.print("");
 	// super.caseTCaret(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTDegree(node.TDegree)
 	// */
 	// @Override
 	// public void caseTDegree(TDegree node) {
 	// System.out.print("");
 	// super.caseTDegree(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTBlackslash(node.TBlackslash)
 	// */
 	// @Override
 	// public void caseTBlackslash(TBlackslash node) {
 	// System.out.print("\\");
 	// super.caseTBlackslash(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTEquals(node.TEquals)
 	// */
 	// @Override
 	// public void caseTEquals(TEquals node) {
 	// System.out.print("=");
 	// super.caseTEquals(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTQuote(node.TQuote)
 	// */
 	// @Override
 	// public void caseTQuote(TQuote node) {
 	// System.out.print("'");
 	// super.caseTQuote(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTReturn(node.TReturn)
 	// */
 	// @Override
 	// public void caseTReturn(TReturn node) {
 	// System.out.println("");
 	// super.caseTReturn(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTTab(node.TTab)
 	// */
 	// @Override
 	// public void caseTTab(TTab node) {
 	// System.out.print("	");
 	// super.caseTTab(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTComment(node.TComment)
 	// */
 	// @Override
 	// public void caseTComment(TComment node) {
 	// System.out.print("//");
 	// super.caseTComment(node);
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseTEndoffile(node.TEndoffile)
 	// */
 	// @Override
 	// public void caseTEndoffile(TEndoffile node) {
 	//
 	// super.caseTEndoffile(node);
 	// }
 	//
 	// /* (non-Javadoc)
 	// * @see analysis.AnalysisAdapter#caseTBool(node.TBool)
 	// */
 	// @Override
 	// public void caseTBool(TBool node) {
 	// // TODO Auto-generated method stub
 	// super.caseTBool(node);
 	// }
 	//
 	//
 	//
 	// //////Start and EOF//////
 	//
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.DepthFirstAdapter#caseStart(node.Start)
 	// */
 	// @Override
 	// public void caseStart(Start node) {
 	// System.out.println("Hier beginnt der Code");
 	// super.caseStart(node);
 	// }
 	//
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see analysis.AnalysisAdapter#caseEOF(node.EOF)
 	// */
 	// @Override
 	// public void caseEOF(EOF node) {
 	// System.out.println("Datei zu ende");
 	// super.caseEOF(node);
 	// }
 
 }
