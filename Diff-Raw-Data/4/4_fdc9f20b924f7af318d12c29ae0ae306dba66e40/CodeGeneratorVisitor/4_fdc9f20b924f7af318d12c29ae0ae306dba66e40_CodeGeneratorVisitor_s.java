 package wci.backend.compiler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import wci.frontend.*;
 import wci.intermediate.LOLCodeParserVisitorAdapter;
 import wci.intermediate.SymTabEntry;
 import wci.intermediate.icodeimpl.*;
 import static wci.backend.compiler.CodeGenerator.*;
 
 public class CodeGeneratorVisitor extends LOLCodeParserVisitorAdapter implements
 		LOLCodeParserTreeConstants {
 	public Object visit(ASTintegerConstant node, Object data) {
 		int value = (int) node.getAttribute(ICodeKeyImpl.VALUE);
 		pln(jasminLongVariant(value));
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTrealConstant node, Object data) {
 		float value = (float) node.getAttribute(ICodeKeyImpl.VALUE);
 		pln(jasminDoubleVariant(value));
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	// todo: this gets called for "MAEK 5 A TROOF". it prob shouldnt.
 	public Object visit(ASTbooleanValue node, Object data) {
 		boolean value = node.getAttribute(ICodeKeyImpl.VALUE).equals("true");
 
 		pln(jasminBooleanVariant(value));
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTstringLiteral node, Object data) {
 		// TODO
 		// litteral strings might contain a double quote, or a new line char.
 		// this will break our jasmin, so the way we will handle it is to encode
 		// the value into a safe format. once its safely into jasmins memory, we
 		// call
 		// a meth in our runtime lib to decode it.
 		// im gonna use base64 encoding.
 
 		String value = (String) node.getAttribute(ICodeKeyImpl.VALUE);
 		pln(jasminStringVariant(value));
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTvisible node, Object data) {
 		// put the printVariant() arg onto the stack
 		putChildrenDirectlyOntoStack(node, data);
 
 		// print the variant
 		pln("invokestatic Util/printVariant(LVariant;)V");
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTgimmeh node, Object data) {
 		// put the printVariant() arg onto the stack
 		putChildrenDirectlyOntoStack(node, data);
 
 		// print the variant
 		pln("invokestatic Util/readLineFromStdin()LVariant;");
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTconcatenate node, Object data) {
 		// put the concat() args onto the stack
 		putChildrenIntoVarArgsArray(node, data);
 
 		// call the add method using the array on the stack as the argument
 		pln("invokestatic Util/concat([LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTadd node, Object data) {
 		// put the args onto the stack
 		putChildrenDirectlyOntoStack(node, data);
 
 		// call the add method, letting it pick up its 2 args from top of stack
 		pln("invokestatic Util/add(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTsubtract node, Object data) {
 		// put the args onto the stack
 		putChildrenDirectlyOntoStack(node, data);
 
 		// call the sub method, letting it pick up its 2 args from top of stack
 		pln("invokestatic Util/subtract(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTmultiply node, Object data) {
 		// put the args onto the stack
 		putChildrenDirectlyOntoStack(node, data);
 
 		// call the mult method, letting it pick up its 2 args from top of stack
 		pln("invokestatic Util/multiply(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTdivide node, Object data) {
 		// put the args onto the stack
 		putChildrenDirectlyOntoStack(node, data);
 
 		// call the mult method, letting it pick up its 2 args from top of stack
 		pln("invokestatic Util/divide(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTmodulo node, Object data) {
 		// put the args onto the stack
 		putChildrenDirectlyOntoStack(node, data);
 
 		// call the mult method, letting it pick up its 2 args from top of stack
 		pln("invokestatic Util/mod(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTmax node, Object data) {
 		// put the args onto the stack
 		putChildrenDirectlyOntoStack(node, data);
 
 		// call the max method, letting it pick up its 2 args from top of stack
 		pln("invokestatic Util/max(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTmin node, Object data) {
 		// put the args onto the stack
 		putChildrenDirectlyOntoStack(node, data);
 
 		// call the min method, letting it pick up its 2 args from top of stack
 		pln("invokestatic Util/min(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTxor node, Object data) {
 		// put the args onto the stack
 		putChildrenDirectlyOntoStack(node, data);
 
 		// call the min method, letting it pick up its 2 args from top of stack
 		pln("invokestatic Util/xor(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTcast node, Object data) {
 		putChildrenDirectlyOntoStack(node, data);
 		pln("invokestatic Util/typeCast(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTequals node, Object data) {
 		putChildrenDirectlyOntoStack(node, data);
 		pln("invokestatic Util/equal(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTnot node, Object data) {
 		putChildrenDirectlyOntoStack(node, data);
 		pln("invokestatic Util/negate(LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTand node, Object data) {
 		putChildrenDirectlyOntoStack(node, data);
 		pln("invokestatic Util/and(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTor node, Object data) {
 		putChildrenDirectlyOntoStack(node, data);
 		pln("invokestatic Util/or(LVariant;LVariant;)LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTtype node, Object data) {
 		String value = (String) node.getAttribute(ICodeKeyImpl.VALUE);
 		pln(jasminStringVariant(value));
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTassign node, Object data) {
 		// only emit a store instruction if there is a value to assign
 		if (node.jjtGetNumChildren() == 1)
 		{
 			int slot = (int) node.getAttribute(ICodeKeyImpl.ID);
 			putChildrenDirectlyOntoStack(node, data);
 			pln("astore "+ slot);
 			flush();
 		}
 		return data;
 	}
 
 	public Object visit(ASTidentifier node, Object data) {		
 		String name = node.getAttribute(ICodeKeyImpl.ID).toString();
 		SymTabEntry entry = symTabStack.lookupLocal(name);
 		// assign loop variables to slot 1 (reserved)
 		if (entry == null)
 		{
 			entry = symTabStack.enterLocal(name);
 			entry.setIndex(1);			
 		}
 		int slot = entry.getIndex();
 		pln("aload " + slot);
 		flush();
		
 		return data;
 	}
 
 	public Object visit(ASTfunctionCall node, Object data) {
 		String functionName = node.getAttribute(ICodeKeyImpl.ID).toString();
 		putChildrenDirectlyOntoStack(node, data);
 		
 		p("invokestatic	" + functionName + "(");
 		int numChildren = node.jjtGetNumChildren();
 		for (int i = 0; i < numChildren; ++i)
 			p("LVariant;");
 		pln(")LVariant;");
 		pln(setMostRecentExpression());
 		flush();
 
 		return data;
 	}
 
 	/**
 	 * if/else statement
 	 */
 	public Object visit(ASTtest node, Object data) {
 		// think of this label as the next line of code after the entire if
 		// statement
 		String labelAfterEntireIfStructure = jumpLabel("after_if");
 		String labelAtStartOfElseBlockCode = jumpLabel("else");
 		SimpleNode literalNode;
 
 		// get the bool value of the expression
 		pln("invokestatic Util/getMostRecentExpressionAsBoolean()Z");
 
 		// if the bool is 0/false, we will jump over the "if_true" codez into
 		// the else block codez
 		pln("ifeq " + labelAtStartOfElseBlockCode);
 
 		// recursively visit child nodes, which prints the if_true codez
 		literalNode = (SimpleNode) node.jjtGetChild(0);
 		literalNode.jjtAccept(this, data);
 
 		// jump over the rest of the if structure, which is the else block code
 		pln("goto " + labelAfterEntireIfStructure);
 		// we always print the else label even if we dont have an else block,
 		// because we will still jump to it when the if is false
 		pln(labelAtStartOfElseBlockCode + ":");
 
 		// check for an else statement
 		if (node.jjtGetNumChildren() > 1) {
 			// recursively visit child nodes, which prints the if_false codez
 			literalNode = (SimpleNode) node.jjtGetChild(1);
 			literalNode.jjtAccept(this, data);
 		}
 
 		pln(labelAfterEntireIfStructure + ":");
 		flush();
 
 		return data;
 	}
 
 	public Object visit(ASTswitchStatement node, Object data) {
 		// think of this label as the next line of code after the entire switch
 		// statement
 		String labelAfterEntireSwitchStructure = jumpLabel("after_switch");
 		SimpleNode literalNode;
 
 		// ASTcaseStatement has the value as first child, and a codeblock as 2nd
 		// child
 		List<ASTcaseStatement> caseStatements = new ArrayList<>();
 		ASTdefaultCase defaultCase = null;
 		String defaultCodeLocationLabel = jumpLabel("case_default");
 
 		// a list of the case values
 		List<SimpleNode> caseComparisonValueNodes = new ArrayList<>();
 		List<SimpleNode> caseCodeBlockNodes = new ArrayList<>();
 		List<String> caseJumpLabels = new ArrayList<>();
 		List<Boolean> hasBreakStatement = new ArrayList<>();
 		for (SimpleNode n : getChildrenOf(node)) {
 			if (n instanceof ASTcaseStatement) {
 				caseStatements.add((ASTcaseStatement) n);
 				caseComparisonValueNodes.add((SimpleNode) n.jjtGetChild(0));
 				caseCodeBlockNodes.add((SimpleNode) n.jjtGetChild(1));
 				hasBreakStatement.add(n.jjtGetNumChildren() > 2);
 				caseJumpLabels.add(jumpLabel("case"));
 			} else if (n instanceof ASTdefaultCase) {
 				defaultCase = (ASTdefaultCase) n;
 			}
 		}
 
 		// put the switch value on the stack
 		pln("invokestatic Util/getMostRecentExpression()LVariant;");
 
 		for (int i = 0; i < caseComparisonValueNodes.size(); i++) {
 			SimpleNode caseValNode = caseComparisonValueNodes.get(i);
 			String labelLocationOfCode = caseJumpLabels.get(i);
 			// each case statement comparison will eat up the orig switch val,
 			// so we need to dup it
 			pln("dup");
 			caseValNode.jjtAccept(this, data);
 
 			// do the comparison
 			pln("invokestatic Util/equal(LVariant;LVariant;)LVariant;");
 			pln(setMostRecentExpression());
 			// we dont want the variant that results because we will get it from
 			// the runtime lib, so clean up our stack by popping it off
 			pln("pop");
 			// get the bool value of the comparison
 			pln("invokestatic Util/getMostRecentExpressionAsBoolean()Z");
 			// test the cmp result, and maybe jump
 			pln("ifne " + labelLocationOfCode);
 		}
 
 		if (defaultCase != null) {
 			pln("goto " + defaultCodeLocationLabel);
 		}
 
 		pln("goto " + labelAfterEntireSwitchStructure);
 
 		for (int i = 0; i < caseCodeBlockNodes.size(); i++) {
 			SimpleNode caseCodeBlockNode = caseCodeBlockNodes.get(i);
 			String labelLocationOfCode = caseJumpLabels.get(i);
 			pln(labelLocationOfCode + ":");
 			caseCodeBlockNode.jjtAccept(this, data);
 			if (hasBreakStatement.get(i) == true) {
 				pln("goto " + labelAfterEntireSwitchStructure);
 			}
 		}
 
 		if (defaultCase != null) {
 			pln(defaultCodeLocationLabel + ":");
 			SimpleNode caseCodeBlockNode = (SimpleNode) defaultCase
 					.jjtGetChild(0);
 			caseCodeBlockNode.jjtAccept(this, data);
 		}
 
 		pln(labelAfterEntireSwitchStructure + ":");
 		pln("pop");
 
 		flush();
 
 		return data;
 	}
 
 	/**
 	 * This will emit the jasmin code to package all Variants which result from
 	 * the child nodes, into a Variant[] array. This is used for calling a
 	 * var-args method from within jasmin.
 	 * 
 	 * @param node
 	 *            THe node whos children will be recursively visited
 	 * @param data
 	 */
 	public void putChildrenIntoVarArgsArray(SimpleNode node, Object data) {
 		int numChildren = node.jjtGetNumChildren();
 
 		// init a java array like Variant[] varArgs = new Variant[numChildren];
 		// we have to do this in order to pass the arguments to the
 		// Util.concat() method, because its a var-args method
 		pln(initVariantArray(numChildren));
 
 		for (int i = 0; i < numChildren; i++) {
 
 			// we dup the array reference because the aastore instruction will
 			// eat it
 			pln("dup");
 
 			// in a moment when we store the Variant into the array, this int
 			// will be used as its array index
 			pln(iconst(i));
 
 			// recursively let a visitor put some value onto the stack for us.
 			// this should be a variant.
 			SimpleNode literalNode = (SimpleNode) node.jjtGetChild(i);
 			literalNode.jjtAccept(this, data);
 
 			// stores whatever variant is on top of the stack into our array at
 			// index i
 			pln("aastore");
 		}
 	}
 
 	/**
 	 * This will recursively visit the child nodes, leaving their resulting
 	 * Variant directly objects on the stack. This is useful to put the
 	 * arguments to a method onto the stack before calling the method.
 	 * 
 	 * @param node
 	 *            THe node whos children will be recursively visited
 	 * @param data
 	 */
 	public void putChildrenDirectlyOntoStack(SimpleNode node, Object data) {
 		int numChildren = node.jjtGetNumChildren();
 
 		// loop over all the children
 		for (int i = 0; i < numChildren; i++) {
 			// recursively let a visitor put some value onto the stack for us.
 			// this should be a variant.
 			SimpleNode literalNode = (SimpleNode) node.jjtGetChild(i);
 			literalNode.jjtAccept(this, data);
 		}
 	}
 
 	List<SimpleNode> getChildrenOf(SimpleNode node) {
 		List<SimpleNode> nodes = new ArrayList<>();
 		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
 			nodes.add((SimpleNode) node.jjtGetChild(i));
 		}
 		return nodes;
 	}
 
 }
