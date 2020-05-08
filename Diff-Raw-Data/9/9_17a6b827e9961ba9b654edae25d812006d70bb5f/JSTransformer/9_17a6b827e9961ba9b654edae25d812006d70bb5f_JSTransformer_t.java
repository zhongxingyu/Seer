 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Vladimir Belov)
  *******************************************************************************/
 
 package org.eclipse.dltk.javascript.parser;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.antlr.runtime.RuleReturnScope;
 import org.antlr.runtime.Token;
 import org.antlr.runtime.tree.Tree;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.javascript.ast.ArrayInitializer;
 import org.eclipse.dltk.javascript.ast.AsteriskExpression;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.BooleanLiteral;
 import org.eclipse.dltk.javascript.ast.BreakStatement;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.CaseClause;
 import org.eclipse.dltk.javascript.ast.CatchClause;
 import org.eclipse.dltk.javascript.ast.CommaExpression;
 import org.eclipse.dltk.javascript.ast.Comment;
 import org.eclipse.dltk.javascript.ast.ConditionalOperator;
 import org.eclipse.dltk.javascript.ast.ConstDeclaration;
 import org.eclipse.dltk.javascript.ast.ContinueStatement;
 import org.eclipse.dltk.javascript.ast.DecimalLiteral;
 import org.eclipse.dltk.javascript.ast.DefaultClause;
 import org.eclipse.dltk.javascript.ast.DefaultXmlNamespaceStatement;
 import org.eclipse.dltk.javascript.ast.DeleteStatement;
 import org.eclipse.dltk.javascript.ast.DoWhileStatement;
 import org.eclipse.dltk.javascript.ast.EmptyExpression;
 import org.eclipse.dltk.javascript.ast.ExceptionFilter;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.FinallyClause;
 import org.eclipse.dltk.javascript.ast.ForEachInStatement;
 import org.eclipse.dltk.javascript.ast.ForInStatement;
 import org.eclipse.dltk.javascript.ast.ForStatement;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.GetAllChildrenExpression;
 import org.eclipse.dltk.javascript.ast.GetArrayItemExpression;
 import org.eclipse.dltk.javascript.ast.GetLocalNameExpression;
 import org.eclipse.dltk.javascript.ast.GetMethod;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.IfStatement;
 import org.eclipse.dltk.javascript.ast.Keyword;
 import org.eclipse.dltk.javascript.ast.Keywords;
 import org.eclipse.dltk.javascript.ast.Label;
 import org.eclipse.dltk.javascript.ast.LabelledStatement;
 import org.eclipse.dltk.javascript.ast.MultiLineComment;
 import org.eclipse.dltk.javascript.ast.NewExpression;
 import org.eclipse.dltk.javascript.ast.NullExpression;
 import org.eclipse.dltk.javascript.ast.ObjectInitializer;
 import org.eclipse.dltk.javascript.ast.ParenthesizedExpression;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.RegExpLiteral;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.SetMethod;
 import org.eclipse.dltk.javascript.ast.SingleLineComment;
 import org.eclipse.dltk.javascript.ast.Statement;
 import org.eclipse.dltk.javascript.ast.StatementBlock;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.ast.SwitchComponent;
 import org.eclipse.dltk.javascript.ast.SwitchStatement;
 import org.eclipse.dltk.javascript.ast.ThisExpression;
 import org.eclipse.dltk.javascript.ast.ThrowStatement;
 import org.eclipse.dltk.javascript.ast.TryStatement;
 import org.eclipse.dltk.javascript.ast.TypeOfExpression;
 import org.eclipse.dltk.javascript.ast.UnaryOperation;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.ast.VoidExpression;
 import org.eclipse.dltk.javascript.ast.VoidOperator;
 import org.eclipse.dltk.javascript.ast.WhileStatement;
 import org.eclipse.dltk.javascript.ast.WithStatement;
 import org.eclipse.dltk.javascript.ast.XmlAttributeIdentifier;
 import org.eclipse.dltk.javascript.ast.XmlLiteral;
 import org.eclipse.dltk.javascript.ast.YieldOperator;
 
 public class JSTransformer extends JSVisitor {
 
 	private Tree root;
 	private List<Token> tokens;
 	private String source;
 
 	private int[] tokenOffsets = null;
 
 	private ASTNode parent;
 	private ASTNode result = null;
 	private List<ASTNode> resultList = null;
 
 	private int currentRecursionDeep;
 
 	private static int MAX_RECURSION_DEEP = 512;
 
 	public JSTransformer(RuleReturnScope root, List<Token> tokens) {
 		this((Tree) root.getTree(), tokens, null, 0);
 	}
 
 	private void checkRecursionDeep() {
 		if (currentRecursionDeep > MAX_RECURSION_DEEP)
 			throw new IllegalArgumentException("Too many AST deep");
 	}
 
 	private JSTransformer(Tree tree, List<Token> tokens, ASTNode parent,
 			int recursionDeep) {
 		super(MAX_RECURSION_DEEP);
 
 		Assert.isNotNull(tokens);
 		Assert.isTrue(tokens.size() > 0);
 
 		this.root = tree;
 		this.parent = parent;
 		this.tokens = tokens;
 		this.currentRecursionDeep = recursionDeep;
 
 		checkRecursionDeep();
 
 		prepareOffsetMap();
 	}
 
 	private JSTransformer(Tree tree, List<Token> tokens, int[] tokenOffsets,
 			String source, ASTNode parent, int recursionDeep) {
 		super(MAX_RECURSION_DEEP);
 
 		Assert.isNotNull(tokens);
 		Assert.isTrue(tokens.size() > 0);
 
 		this.root = tree;
 		this.parent = parent;
 		this.tokens = tokens;
 		this.tokenOffsets = tokenOffsets;
 		this.source = source;
 		this.currentRecursionDeep = recursionDeep;
 
 		checkRecursionDeep();
 	}
 
 	public Script transform() {
 
 		if (this.root == null)
 			return null;
 
 		if (this.root.getType() != 0) {
 			Script script = new Script();
 
 			script.addStatement(transformStatementNode(root, script));
 			addComments(script);
 
 			script.setStart(0);
 			script.setEnd(source.length());
 
 			return script;
 		} else
 			return (Script) transformNode();
 	}
 
 	private ASTNode transformNode() {
 		visitNode(root);
 		Assert.isNotNull(result, root.toString());
 		return result;
 	}
 
 	private List<ASTNode> transformListNode() {
 		visitNode(root);
 		Assert.isTrue(resultList != null);
 		return resultList;
 	}
 
 	private ASTNode transformNode(Tree node, ASTNode parent) {
 		return new JSTransformer(node, tokens, tokenOffsets, source, parent,
 				currentRecursionDeep + 1).transformNode();
 	}
 
 	private void prepareOffsetMap() {
 
 		tokenOffsets = new int[tokens.size() + 1];
 
 		int offset = 0;
 		StringBuffer buffer = new StringBuffer();
 
 		for (int i = 0; i < tokens.size(); i++) {
 			String tokenText = tokens.get(i).getText();
 
 			tokenOffsets[i] = offset;
 
 			offset += tokenText.length();
 			buffer.append(tokenText);
 		}
 		tokenOffsets[tokens.size()] = offset;
 		source = buffer.toString();
 	}
 
 	private int getTokenOffset(int tokenIndex) {
 		Assert.isTrue(tokenOffsets != null);
 		Assert.isTrue(tokenIndex >= -1 && tokenIndex < tokenOffsets.length);
 
 		return tokenOffsets[tokenIndex];
 	}
 
 	private int getTokenOffset(int tokenType, int startTokenIndex,
 			int endTokenIndex) {
 
 		Assert.isTrue(startTokenIndex >= 0);
 		Assert.isTrue(endTokenIndex > 0);
 		Assert.isTrue(startTokenIndex <= endTokenIndex);
 
 		Token token = null;
 
 		for (int i = startTokenIndex; i <= endTokenIndex; i++) {
 			Token item = tokens.get(i);
 			if (item.getType() == tokenType) {
 				token = item;
 				break;
 			}
 		}
 
 		if (token == null)
 			return -1;
 		else
 			return getTokenOffset(token.getTokenIndex());
 	}
 
 	private int getTokenOffset(int tokenType, int startTokenIndex,
 			int endTokenIndex, int skipCount) {
 
 		Assert.isTrue(startTokenIndex > 0);
 		Assert.isTrue(endTokenIndex > 0);
 		Assert.isTrue(startTokenIndex <= endTokenIndex);
 
 		Token token = null;
 
 		int skipped = 0;
 
 		for (int i = startTokenIndex; i <= endTokenIndex; i++) {
 			Token item = tokens.get(i);
 			if (item.getType() == tokenType) {
 				if (skipped == skipCount) {
 					token = item;
 					break;
 				} else {
 					skipped++;
 				}
 			}
 		}
 
 		if (token == null)
 			return -1;
 		else
 			return getTokenOffset(token.getTokenIndex());
 
 	}
 
 	private Statement transformStatementNode(Tree node, ASTNode parent) {
 
 		ASTNode expression = new JSTransformer(node, tokens, tokenOffsets,
 				source, parent, currentRecursionDeep + 1).transformNode();
 
 		if (expression instanceof Statement)
 			return (Statement) expression;
 		else {
 			VoidExpression voidExpression = new VoidExpression(parent);
 			voidExpression.setExpression((Expression) expression);
 
 			Token token = tokens.get(node.getTokenStopIndex());
 
 			if (token.getType() == JSParser.SEMIC) {
 				voidExpression.setSemicolonPosition(getTokenOffset(token
 						.getTokenIndex()));
 				voidExpression.getExpression().setEnd(
 						Math.min(voidExpression.getSemicolonPosition(),
 								expression.sourceEnd()));
 			}
 
 			Assert.isTrue(expression.sourceStart() >= 0);
 			Assert.isTrue(expression.sourceEnd() > 0);
 
 			voidExpression.setStart(expression.sourceStart());
 			voidExpression.setEnd(Math.max(expression.sourceEnd(),
 					voidExpression.getSemicolonPosition() + 1));
 
 			return voidExpression;
 		}
 	}
 
 	private List<ASTNode> transformListNode(Tree node, ASTNode parent) {
 		return new JSTransformer(node, tokens, tokenOffsets/* offsetMap */,
 				source, parent, currentRecursionDeep + 1).transformListNode();
 	}
 
 	protected boolean visitArguments(Tree node) {
 
 		List<ASTNode> nodes = new ArrayList<ASTNode>(node.getChildCount());
 
 		for (int i = 0; i < node.getChildCount(); i++) {
 			nodes.add(transformNode(node.getChild(i), this.parent));
 		}
 
 		this.resultList = nodes;
 		return true;
 	}
 
 	protected boolean visitBinaryOperation(Tree node) {
 
 		if (node.getType() == JSParser.MUL) {
 			switch (node.getChildCount()) {
 			case 0:
 				return visitAsterisk(node);
 			case 1:
 				// HACK
 				return visit(node.getChild(0));
 			}
 
 		}
 
 		Assert.isNotNull(node.getChild(0));
 		Assert.isNotNull(node.getChild(1));
 
 		BinaryOperation operation = new BinaryOperation(this.parent);
 
 		operation.setOperation(node.getType());
 
 		operation.setLeftExpression((Expression) transformNode(
 				node.getChild(0), this.parent));
 
 		operation.setRightExpression((Expression) transformNode(node
 				.getChild(1), this.parent));
 
 		operation.setOperationPosition(getTokenOffset(node.getType(),
 				getRealTokenStopIndex(node.getChild(0)) + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		Assert.isTrue(operation.getOperationPosition() >= operation
 				.getLeftExpression().sourceEnd());
 		Assert.isTrue(operation.getOperationPosition()
 				+ operation.getOperationText().length() <= operation
 				.getRightExpression().sourceStart());
 
 		operation.setStart(operation.getLeftExpression().sourceStart());
 		operation.setEnd(operation.getRightExpression().sourceEnd());
 
 		this.result = operation;
 		return true;
 	}
 
 	protected boolean visitBlock(Tree node) {
 
 		StatementBlock block = new StatementBlock(this.parent);
 
 		for (int i = 0; i < node.getChildCount(); i++) {
 			block.getStatements().add(
 					transformStatementNode(node.getChild(i), this.parent));
 		}
 
 		block.setLC(getTokenOffset(JSParser.LBRACE, node.getTokenStartIndex(),
 				node.getTokenStopIndex()));
 
 		block.setRC(getTokenOffset(JSParser.RBRACE, node.getTokenStopIndex(),
 				node.getTokenStopIndex()));
 
 		if (block.getLC() > -1 && block.getRC() > -1) {
 			block.setStart(block.getLC());
 			block.setEnd(block.getRC() + 1);
 		} else {
 			block.setStart(((ASTNode) block.getStatements().get(0))
 					.sourceStart());
 			block.setEnd(((ASTNode) block.getStatements().get(
 					block.getStatements().size() - 1)).sourceStart());
 		}
 
 		this.result = block;
 		return true;
 	}
 
 	protected boolean visitBreak(Tree node) {
 
 		BreakStatement statement = new BreakStatement(this.parent);
 
 		Keyword breakKeyword = new Keyword(statement, Keywords.BREAK);
 		breakKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		breakKeyword.setEnd(breakKeyword.sourceStart()
 				+ Keywords.BREAK.length());
 		statement.setBreakKeyword(breakKeyword);
 
 		if (node.getChildCount() > 0) {
 			Label label = new Label(statement);
 			label.setText(node.getChild(0).getText());
 			label
 					.setStart(getTokenOffset(node.getChild(0)
 							.getTokenStartIndex()));
 			label
 					.setEnd(getTokenOffset(node.getChild(0).getTokenStopIndex() + 1));
 
 			statement.setLabel(label);
 		}
 
 		statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		statement.setStart(statement.getBreakKeyword().sourceStart());
 
 		if (statement.getLabel() != null)
 			statement.setEnd(Math.max(statement.getSemicolonPosition(),
 					statement.getLabel().sourceEnd()));
 		else
 			statement.setEnd(Math.max(statement.getSemicolonPosition(),
 					statement.getBreakKeyword().sourceEnd()));
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitCall(Tree node) {
 
 		CallExpression call = new CallExpression(this.parent);
 
 		Assert.isNotNull(node.getChild(0));
 		Assert.isNotNull(node.getChild(1));
 
 		call.setExpression(transformNode(node.getChild(0), call));
 		call.setArguments(transformListNode(node.getChild(1), call));
 
 		List<Integer> commas = new ArrayList<Integer>();
 		Tree args = node.getChild(1);
 		for (int i = 1 /* miss the first */; i < args.getChildCount(); i++) {
 			commas.add(new Integer(getTokenOffset(JSParser.COMMA, args
 					.getChild(i - 1).getTokenStopIndex() + 1, args.getChild(i)
 					.getTokenStartIndex())));
 		}
 		call.setCommas(commas);
 
 		call.setLP(getTokenOffset(JSParser.LPAREN, node.getChild(1)
 				.getTokenStartIndex(), node.getChild(1).getTokenStartIndex()));
 		call.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(1)
 				.getTokenStopIndex(), node.getChild(1).getTokenStopIndex()));
 
 		call.setStart(call.getExpression().sourceStart());
 		call.setEnd(call.getRP() + 1);
 
 		this.result = call;
 		return true;
 	}
 
 	protected boolean visitCase(Tree node) {
 
 		CaseClause caseClause = new CaseClause(this.parent);
 
 		Keyword caseKeyword = new Keyword(caseClause, Keywords.CASE);
 		caseKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		caseKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		caseClause.setCaseKeyword(caseKeyword);
 
 		caseClause.setCondition((Expression) transformNode(node.getChild(0),
 				caseClause));
 
 		caseClause
 				.setColonPosition(getTokenOffset(JSParser.COLON, node.getChild(
 						0).getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		// miss condition
 		for (int i = 1; i < node.getChildCount(); i++) {
 			caseClause.getStatements().add(
 					transformStatementNode(node.getChild(i), caseClause));
 		}
 
 		caseClause.setStart(caseClause.getCaseKeyword().sourceStart());
 		caseClause.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = caseClause;
 		return true;
 	}
 
 	protected boolean visitDecimalLiteral(Tree node) {
 
 		DecimalLiteral number = new DecimalLiteral(this.parent);
 		number.setText(node.getText());
 		number.setStart(getTokenOffset(node.getTokenStartIndex()));
 		number.setEnd(number.sourceStart() + number.getText().length());
 
 		this.result = number;
 		return true;
 	}
 
 	protected boolean visitDefault(Tree node) {
 
 		DefaultClause defaultClause = new DefaultClause(this.parent);
 
 		Keyword defaultKeyword = new Keyword(this.parent, Keywords.DEFAULT);
 		defaultKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		defaultKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		defaultClause.setDefaultKeyword(defaultKeyword);
 
 		defaultClause.setColonPosition(getTokenOffset(JSParser.COLON, node
 				.getTokenStartIndex() + 1, node.getTokenStopIndex() + 1));
 
 		for (int i = 0; i < node.getChildCount(); i++) {
 			defaultClause.getStatements().add(
 					transformStatementNode(node.getChild(i), defaultClause));
 		}
 
 		defaultClause.setStart(defaultClause.getDefaultKeyword().sourceStart());
 		defaultClause.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = defaultClause;
 		return true;
 	}
 
 	protected boolean visitExpression(Tree node) {
 
 		if (node.getChildCount() > 0)
 			this.result = transformNode(node.getChild(0), this.parent);
 		else
 			this.result = new EmptyExpression(this.parent);
 
 		return true;
 	}
 
 	protected boolean visitFor(Tree node) {
 
 		switch (node.getChild(0).getType()) {
 		case JSParser.FORSTEP:
 			return visitForStatement(node);
 
 		case JSParser.FORITER:
 			return visitForInStatement(node);
 
 		default:
 			throw new IllegalArgumentException("FORSTEP or FORITER expected");
 		}
 	}
 
 	private boolean visitForStatement(Tree node) {
 		ForStatement statement = new ForStatement(this.parent);
 
 		Keyword forKeyword = new Keyword(this.parent, Keywords.FOR);
 		forKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		forKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		statement.setForKeyword(forKeyword);
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getTokenStopIndex()));
 
 		statement.setInitial((Expression) transformNode(node.getChild(0)
 				.getChild(0), statement));
 
 		statement.setCondition((Expression) transformNode(node.getChild(0)
 				.getChild(1), statement));
 
 		statement.setStep((Expression) transformNode(node.getChild(0).getChild(
 				2), statement));
 
 		if (statement.getInitial() instanceof EmptyExpression) {
 			statement.setInitialSemicolonPosition(getTokenOffset(
 					JSParser.SEMIC, node.getTokenStartIndex() + 2, node
 							.getTokenStopIndex()));
 
 			statement.getInitial().setStart(
 					statement.getInitialSemicolonPosition());
 			statement.getInitial().setEnd(
 					statement.getInitialSemicolonPosition());
 
 			if (statement.getCondition() instanceof EmptyExpression) {
 				statement.setConditionalSemicolonPosition((getTokenOffset(
 						JSParser.SEMIC, node.getTokenStartIndex(), node
 								.getTokenStopIndex(), 1)));
 
 				statement.getCondition().setStart(
 						statement.getConditionalSemicolonPosition());
 				statement.getCondition().setEnd(
 						statement.getConditionalSemicolonPosition());
 
 			} else {
 				statement.setConditionalSemicolonPosition((getTokenOffset(
 						JSParser.SEMIC, node.getChild(0).getChild(1)
 								.getTokenStopIndex() + 1, node
 								.getTokenStopIndex())));
 
 			}
 
 		} else {
 			statement.setInitialSemicolonPosition(getTokenOffset(
 					JSParser.SEMIC, getRealTokenStopIndex(node.getChild(0)
 							.getChild(0)) + 1, node.getTokenStopIndex()));
 
 			if (statement.getCondition() instanceof EmptyExpression) {
 				statement.setConditionalSemicolonPosition((getTokenOffset(
 						JSParser.SEMIC, node.getChild(0).getChild(0)
 								.getTokenStopIndex() + 1, node
 								.getTokenStopIndex(), 1)));
 
 				statement.getCondition().setStart(
 						statement.getConditionalSemicolonPosition());
 				statement.getCondition().setEnd(
 						statement.getConditionalSemicolonPosition());
 
 			} else {
 				statement.setConditionalSemicolonPosition((getTokenOffset(
 						JSParser.SEMIC, getRealTokenStopIndex(node.getChild(0)
 								.getChild(1)) + 1, node.getTokenStopIndex())));
 
 			}
 
 		}
 
 		if (statement.getStep() instanceof EmptyExpression) {
 			statement.setStart(statement.getConditionalSemicolonPosition() + 1);
 			statement.setEnd(statement.getConditionalSemicolonPosition() + 1);
 		}
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		if (node.getChildCount() > 1)
 			statement.setBody(transformStatementNode(node.getChild(1),
 					statement));
 
 		if (statement.getBody() == null
 				|| !(statement.getBody() instanceof VoidExpression)
 				|| ((VoidExpression) statement.getBody())
 						.getSemicolonPosition() == -1) {
 
 			statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 					.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		}
 
 		statement.setStart(statement.getForKeyword().sourceStart());
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	private boolean visitForInStatement(Tree node) {
 		ForInStatement statement = new ForInStatement(this.parent);
 
 		Keyword forKeyword = new Keyword(statement, Keywords.FOR);
 		forKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		forKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		statement.setForKeyword(forKeyword);
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getChild(0)
 				.getTokenStartIndex()));
 
 		statement.setItem((Expression) transformNode(node.getChild(0).getChild(
 				0), statement));
 
 		Keyword inKeyword = new Keyword(statement, Keywords.IN);
 
 		int iteratorStart = node.getChild(0).getChild(1).getTokenStartIndex();
 
 		if (iteratorStart == -1
 				&& node.getChild(0).getChild(1).getType() == JSParser.EXPR
 				&& node.getChild(0).getChild(1).getChildCount() > 0)
 			iteratorStart = node.getChild(0).getChild(1).getChild(0)
 					.getTokenStartIndex();
 
 		inKeyword.setStart(getTokenOffset(JSParser.IN,
 				getRealTokenStopIndex(node.getChild(0).getChild(0)) + 1,
 				iteratorStart));
 		inKeyword.setEnd(inKeyword.sourceStart() + Keywords.IN.length());
 		statement.setInKeyword(inKeyword);
 
 		statement.setIterator((Expression) transformNode(node.getChild(0)
 				.getChild(1), statement));
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		if (node.getChildCount() > 1)
 			statement.setBody(transformStatementNode(node.getChild(1),
 					statement));
 
 		if (statement.getBody() == null
 				|| !(statement.getBody() instanceof VoidExpression)
 				|| ((VoidExpression) statement.getBody())
 						.getSemicolonPosition() == -1) {
 
 			statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 					.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		}
 
 		statement.setStart(statement.getForKeyword().sourceStart());
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitFunction(Tree node) {
 
 		FunctionStatement fn = new FunctionStatement(this.parent);
 
 		Keyword functionKeyword = new Keyword(fn, Keywords.FUNCTION);
 		functionKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		functionKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		fn.setFunctionKeyword(functionKeyword);
 
 		int argsIndex = 0;
 
 		if (node.getChild(0).getType() != JSParser.ARGS) {
 			fn.setName((Identifier) transformNode(node.getChild(0), fn));
 			argsIndex++;
 		}
 
 		Tree argsNode = node.getChild(argsIndex);
 
 		fn.setLP(getTokenOffset(JSParser.LPAREN, node.getTokenStartIndex() + 1,
 				argsNode.getTokenStartIndex()));
 
 		fn.setArguments(transformListNode(argsNode, fn));
 
 		List<Integer> commas = new ArrayList<Integer>();
 
 		if (argsNode.getChildCount() > 1) {
 			for (int i = 1; i < argsNode.getChildCount(); i++) {
 				commas.add(new Integer(getTokenOffset(JSParser.COMMA, argsNode
 						.getChild(i - 1).getTokenStopIndex() + 1, argsNode
 						.getChild(i).getTokenStartIndex())));
 			}
 		}
 		fn.setArgumentCommas(commas);
 
 		fn.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(argsIndex)
 				.getTokenStopIndex(), node.getChild(argsIndex + 1)
 				.getTokenStartIndex()));
 
 		fn.setBody((StatementBlock) transformNode(node.getChild(argsIndex + 1),
 				fn));
 
 		fn.setStart(fn.getFunctionKeyword().sourceStart());
 		fn.setEnd(fn.getBody().sourceEnd());
 
 		this.result = fn;
 		return true;
 	}
 
 	protected boolean visitIdentifier(Tree node) {
 
 		Identifier id = new Identifier(parent);
 		id.setName(node.getText());
 
 		id.setStart(getTokenOffset(node.getTokenStartIndex()));
 		id.setEnd(id.sourceStart() + id.getName().length());
 
 		this.result = id;
 		return true;
 	}
 
 	protected boolean visitReturn(Tree node) {
 
 		ReturnStatement returnStatement = new ReturnStatement(this.parent);
 
 		Keyword keyword = new Keyword(returnStatement, Keywords.RETURN);
 		keyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		keyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		returnStatement.setReturnKeyword(keyword);
 
 		if (node.getChildCount() > 0) {
 			returnStatement.setValue((Expression) transformNode(node
 					.getChild(0), returnStatement));
 		}
 
 		Token token = tokens.get(node.getTokenStopIndex());
 		if (token.getType() == JSParser.SEMIC) {
 			returnStatement.setSemicolonPosition(getTokenOffset(node
 					.getTokenStopIndex()));
 
 			returnStatement.setEnd(returnStatement.getSemicolonPosition() + 1);
 		} else if (returnStatement.getValue() != null) {
			returnStatement.setEnd(returnStatement.getValue().sourceEnd());
 		} else {
 			returnStatement.setEnd(returnStatement.getReturnKeyword()
 					.sourceEnd());
 		}
 
 		returnStatement.setStart(returnStatement.getReturnKeyword()
 				.sourceStart());
 
 		this.result = returnStatement;
 		return true;
 	}
 
 	protected boolean visitStringLiteral(Tree node) {
 
 		StringLiteral literal = new StringLiteral(this.parent);
 		literal.setText(node.getText());
 
 		literal.setStart(getTokenOffset(node.getTokenStartIndex()));
 		literal.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = literal;
 		return true;
 	}
 
 	protected boolean visitSwitch(Tree node) {
 
 		SwitchStatement statement = new SwitchStatement(this.parent);
 
 		Keyword switchKeyword = new Keyword(statement, Keywords.SWITCH);
 		switchKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		switchKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		statement.setSwitchKeyword(switchKeyword);
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getChild(0)
 				.getTokenStartIndex()));
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		statement.setCondition((Expression) transformNode(node.getChild(0),
 				statement));
 
 		statement.setLC(getTokenOffset(JSParser.LBRACE, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		List<Tree> caseNodes = new ArrayList<Tree>(node.getChildCount() - 1);
 		for (int i = 1; i < node.getChildCount(); i++) {
 			caseNodes.add(node.getChild(i));
 		}
 		Collections.sort(caseNodes, new Comparator<Tree>() {
 			public int compare(Tree o1, Tree o2) {
 				return o1.getTokenStartIndex() - o2.getTokenStartIndex();
 			}
 		});
 
 		for (Tree child : caseNodes) {
 			switch (child.getType()) {
 			case JSParser.CASE:
 			case JSParser.DEFAULT:
 				statement.addCase((SwitchComponent) transformNode(child,
 						statement));
 				break;
 
 			default:
 				throw new UnsupportedOperationException();
 			}
 		}
 
 		statement.setRC(getTokenOffset(JSParser.RBRACE, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		statement.setStart(statement.getSwitchKeyword().sourceStart());
 		statement.setEnd(statement.getRC() + 1);
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitUnaryOperation(Tree node) {
 
 		UnaryOperation operation = new UnaryOperation(this.parent);
 
 		operation.setOperation(node.getType());
 
 		int operationType = node.getType();
 
 		if (operation.isPostfix())
 			operation.setOperationPosition(getTokenOffset(operationType, node
 					.getChild(0).getTokenStopIndex() + 1, node
 					.getTokenStopIndex()));
 		else
 			operation.setOperationPosition(getTokenOffset(operationType, node
 					.getTokenStartIndex(), node.getTokenStopIndex()));
 
 		if (operation.getOperationPosition() == -1) {
 
 			// use compatible operations
 			switch (operationType) {
 			case JSParser.PINC:
 				operationType = JSParser.INC;
 				break;
 
 			case JSParser.PDEC:
 				operationType = JSParser.DEC;
 				break;
 
 			case JSParser.NEG:
 				operationType = JSParser.SUB;
 				break;
 			}
 
 			if (operation.isPostfix())
 				operation.setOperationPosition(getTokenOffset(operationType,
 						node.getChild(0).getTokenStopIndex() + 1, node
 								.getTokenStopIndex()));
 			else
 				operation.setOperationPosition(getTokenOffset(operationType,
 						node.getTokenStartIndex(), node.getTokenStopIndex()));
 
 		}
 
 		Assert.isTrue(operation.getOperationPosition() > -1);
 
 		operation.setExpression((Expression) transformNode(node.getChild(0),
 				operation));
 
 		operation.setStart(getTokenOffset(node.getTokenStartIndex()));
 		operation.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = operation;
 		return true;
 	}
 
 	protected boolean visitContinue(Tree node) {
 
 		ContinueStatement statement = new ContinueStatement(this.parent);
 
 		Keyword continueKeyword = new Keyword(statement, Keywords.CONTINUE);
 		continueKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		continueKeyword.setEnd(continueKeyword.sourceStart()
 				+ Keywords.CONTINUE.length());
 		statement.setContinueKeyword(continueKeyword);
 
 		if (node.getChildCount() > 0) {
 			Label label = new Label(statement);
 			label.setText(node.getChild(0).getText());
 			label
 					.setStart(getTokenOffset(node.getChild(0)
 							.getTokenStartIndex()));
 			label
 					.setEnd(getTokenOffset(node.getChild(0).getTokenStopIndex() + 1));
 
 			statement.setLabel(label);
 		}
 
 		statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitVarDeclaration(Tree node) {
 
 		VariableDeclaration var = new VariableDeclaration(this.parent);
 
 		Keyword varKeyword = new Keyword(this.parent, Keywords.VAR);
 		varKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		varKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		var.setVarKeyword(varKeyword);
 
 		List<Integer> commas = new ArrayList<Integer>();
 		List<ASTNode> variables = new ArrayList<ASTNode>(node.getChildCount());
 		for (int i = 0; i < node.getChildCount(); i++) {
 			variables.add(transformNode(node.getChild(i), var));
 
 			if (i > 0)
 				commas.add(new Integer(getTokenOffset(JSParser.COMMA, node
 						.getChild(i - 1).getTokenStopIndex() + 1, node
 						.getChild(i).getTokenStartIndex())));
 		}
 		var.setVariables(variables);
 		var.setCommas(commas);
 
 		var.setStart(getTokenOffset(node.getTokenStartIndex()));
 		var.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = var;
 		return true;
 	}
 
 	protected boolean visitObjectInitializer(Tree node) {
 
 		ObjectInitializer initializer = new ObjectInitializer(this.parent);
 
 		List<ASTNode> properties = new ArrayList<ASTNode>(node.getChildCount());
 		List<Integer> commas = new ArrayList<Integer>();
 
 		for (int i = 0; i < node.getChildCount(); i++) {
 			properties.add(transformNode(node.getChild(i), initializer));
 
 			if (i > 0)
 				commas.add(new Integer(getTokenOffset(JSParser.COMMA, node
 						.getChild(i - 1).getTokenStopIndex() + 1, node
 						.getChild(i).getTokenStartIndex())));
 		}
 
 		initializer.setInitializers(properties);
 		initializer.setCommas(commas);
 
 		initializer.setLC(getTokenOffset(node.getTokenStartIndex()));
 		initializer.setRC(getTokenOffset(node.getTokenStopIndex()));
 
 		Token LC = tokens.get(node.getTokenStartIndex());
 		Token RC = tokens.get(node.getTokenStopIndex());
 
 		initializer.setMultiline(LC.getLine() != RC.getLine());
 
 		initializer.setStart(initializer.getLC());
 		initializer.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = initializer;
 		return true;
 	}
 
 	protected boolean visitPropertyInitializer(Tree node) {
 
 		PropertyInitializer initializer = new PropertyInitializer(this.parent);
 
 		initializer.setName((Expression) transformNode(node.getChild(0),
 				initializer));
 
 		initializer
 				.setColon(getTokenOffset(JSParser.COLON, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		initializer.setValue((Expression) transformNode(node.getChild(1),
 				initializer));
 
 		initializer.setStart(getTokenOffset(node.getTokenStartIndex()));
 		initializer.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = initializer;
 		return true;
 	}
 
 	protected boolean visitForEachInStatement(Tree node) {
 		ForEachInStatement statement = new ForEachInStatement(this.parent);
 
 		Keyword forKeyword = new Keyword(statement, Keywords.FOR);
 		forKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		forKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		statement.setForKeyword(forKeyword);
 
 		Keyword eachKeyword = new Keyword(statement, Keywords.EACH);
 		eachKeyword.setStart(getTokenOffset(JSParser.EACH, node
 				.getTokenStartIndex(), node.getTokenStopIndex()));
 		eachKeyword.setEnd(eachKeyword.sourceStart() + Keywords.EACH.length());
 		statement.setEachKeyword(eachKeyword);
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getChild(0)
 				.getTokenStartIndex()));
 
 		statement.setItem((Expression) transformNode(node.getChild(0).getChild(
 				0), statement));
 
 		Keyword inKeyword = new Keyword(statement, Keywords.IN);
 		int iteratorStart = node.getChild(0).getChild(1).getTokenStartIndex();
 		if (iteratorStart == -1
 				&& node.getChild(0).getChild(1).getType() == JSParser.EXPR
 				&& node.getChild(0).getChild(1).getChildCount() > 0)
 			iteratorStart = node.getChild(0).getChild(1).getChild(0)
 					.getTokenStartIndex();
 
 		inKeyword.setStart(getTokenOffset(JSParser.IN,
 				getRealTokenStopIndex(node.getChild(0).getChild(0)) + 1,
 				iteratorStart));
 		inKeyword.setEnd(inKeyword.sourceStart() + Keywords.IN.length());
 		statement.setInKeyword(inKeyword);
 
 		statement.setIterator((Expression) transformNode(node.getChild(0)
 				.getChild(1), statement));
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		if (node.getChildCount() > 1)
 			statement.setBody(transformStatementNode(node.getChild(1),
 					statement));
 
 		if (statement.getBody() == null
 				|| !(statement.getBody() instanceof VoidExpression)
 				|| ((VoidExpression) statement.getBody())
 						.getSemicolonPosition() == -1) {
 
 			statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 					.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		}
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	private static int getRealTokenStopIndex(Tree node) {
 
 		if (node.getTokenStopIndex() == -1)
 			return getRealTokenStopIndex(node
 					.getChild(node.getChildCount() - 1));
 
 		if (node.getChildCount() > 0) {
 			return Math.max(node.getTokenStopIndex(),
 					getRealTokenStopIndex(node
 							.getChild(node.getChildCount() - 1)));
 		}
 
 		return node.getTokenStopIndex();
 	}
 
 	protected boolean visitByField(Tree node) {
 
 		PropertyExpression property = new PropertyExpression(this.parent);
 
 		property.setObject((Expression) transformNode(node.getChild(0),
 				property));
 
 		property.setProperty((Expression) transformNode(node.getChild(1),
 				property));
 
 		property.setDotPosition(getTokenOffset(JSParser.DOT,
 				getRealTokenStopIndex(node.getChild(0)) + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		Assert.isTrue(property.getObject().sourceStart() >= 0);
 		Assert.isTrue(property.getProperty().sourceEnd() > 0);
 
 		property.setStart(property.getObject().sourceStart());
 		property.setEnd(property.getProperty().sourceEnd());
 
 		this.result = property;
 		return true;
 	}
 
 	protected boolean visitWhile(Tree node) {
 
 		WhileStatement statement = new WhileStatement(this.parent);
 
 		Keyword whileKeyword = new Keyword(statement, Keywords.WHILE);
 		whileKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		whileKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		statement.setWhileKeyword(whileKeyword);
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex(), node.getChild(0).getTokenStartIndex()));
 
 		statement.setCondition((Expression) transformNode(node.getChild(0),
 				statement));
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		if (node.getChildCount() > 1)
 			statement.setBody(transformStatementNode(node.getChild(1),
 					statement));
 
 		if (statement.getBody() == null
 				|| !(statement.getBody() instanceof VoidExpression)
 				|| ((VoidExpression) statement.getBody())
 						.getSemicolonPosition() == -1) {
 
 			statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 					.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		}
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitIf(Tree node) {
 
 		IfStatement ifStatement = new IfStatement(this.parent);
 
 		Keyword ifKeyword = new Keyword(ifStatement, Keywords.IF);
 		ifKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		ifKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		ifStatement.setIfKeyword(ifKeyword);
 
 		ifStatement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getChild(0)
 				.getTokenStartIndex()));
 		ifStatement.setCondition((Expression) transformNode(node.getChild(0),
 				ifStatement));
 
 		ifStatement
 				.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		ifStatement.setThenStatement(transformStatementNode(node.getChild(1),
 				ifStatement));
 
 		if (node.getChildCount() > 2) {
 			Keyword elseKeyword = new Keyword(ifStatement, Keywords.ELSE);
 			elseKeyword.setStart(getTokenOffset(JSParser.ELSE, node.getChild(1)
 					.getTokenStopIndex() + 1, node.getChild(2)
 					.getTokenStartIndex()));
 			elseKeyword.setEnd(elseKeyword.sourceStart()
 					+ Keywords.ELSE.length());
 			ifStatement.setElseKeyword(elseKeyword);
 
 			ifStatement.setElseStatement(transformStatementNode(node
 					.getChild(2), ifStatement));
 		}
 
 		ifStatement.setStart(ifStatement.getIfKeyword().sourceStart());
 		ifStatement.setEnd(node.getTokenStopIndex() + 1);
 
 		this.result = ifStatement;
 		return true;
 	}
 
 	protected boolean visitDoWhile(Tree node) {
 		DoWhileStatement statement = new DoWhileStatement(this.parent);
 
 		Keyword doKeyword = new Keyword(this.parent, Keywords.DO);
 		doKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		doKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		statement.setDoKeyword(doKeyword);
 
 		statement.setBody(transformStatementNode(node.getChild(0), statement));
 
 		Keyword whileKeyword = new Keyword(this.parent, Keywords.WHILE);
 		whileKeyword
 				.setStart(getTokenOffset(JSParser.WHILE, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 		whileKeyword.setEnd(whileKeyword.sourceStart()
 				+ Keywords.WHILE.length());
 		statement.setWhileKeyword(whileKeyword);
 
 		statement
 				.setLP(getTokenOffset(JSParser.LPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		statement.setCondition((Expression) transformNode(node.getChild(1),
 				statement));
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(1)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		statement
 				.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 						.getChild(1).getTokenStopIndex() + 1, node
 						.getTokenStopIndex()));
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitConditional(Tree node) {
 
 		ConditionalOperator operator = new ConditionalOperator(this.parent);
 
 		operator.setCondition((Expression) transformNode(node.getChild(0),
 				operator));
 		operator.setTrueValue((Expression) transformNode(node.getChild(1),
 				operator));
 		operator.setFalseValue((Expression) transformNode(node.getChild(2),
 				operator));
 
 		operator.setQuestionPosition(getTokenOffset(JSParser.QUE, node
 				.getChild(0).getTokenStopIndex() + 1, node.getChild(1)
 				.getTokenStartIndex()));
 
 		operator.setColonPosition(getTokenOffset(JSParser.COLON, node.getChild(
 				1).getTokenStopIndex() + 1, node.getChild(2)
 				.getTokenStartIndex()));
 
 		operator.setStart(getTokenOffset(node.getTokenStartIndex()));
 		operator.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = operator;
 		return true;
 	}
 
 	protected boolean visitParenthesizedExpression(Tree node) {
 
 		ParenthesizedExpression expression = new ParenthesizedExpression(
 				this.parent);
 
 		expression.setExpression((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setLP(getTokenOffset(node.getTokenStartIndex()));
 		expression.setRP(getTokenOffset(node.getTokenStopIndex()));
 
 		expression.setStart(expression.getLP());
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = expression;
 		return true;
 	}
 
 	protected boolean visitTry(Tree node) {
 
 		TryStatement statement = new TryStatement(this.parent);
 
 		Keyword tryKeyword = new Keyword(this.parent, Keywords.TRY);
 		tryKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		tryKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		statement.setTryKeyword(tryKeyword);
 
 		statement.setBody((StatementBlock) transformStatementNode(node
 				.getChild(0), statement));
 
 		for (int i = 1 /* miss body */; i < node.getChildCount(); i++) {
 
 			Tree child = node.getChild(i);
 
 			switch (child.getType()) {
 			case JSParser.CATCH:
 				statement.getCatches().add(
 						(CatchClause) transformNode(child, statement));
 				break;
 
 			case JSParser.FINALLY:
 				statement.setFinally((FinallyClause) transformNode(child,
 						statement));
 				break;
 
 			default:
 				throw new UnsupportedOperationException(
 						"CATCH or FINALLY expected");
 
 			}
 
 		}
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitThrow(Tree node) {
 
 		ThrowStatement statement = new ThrowStatement(this.parent);
 
 		Keyword throwKeyword = new Keyword(statement, Keywords.THROW);
 		throwKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		throwKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		statement.setThrowKeyword(throwKeyword);
 
 		if (node.getChildCount() > 0) {
 			statement.setException((Expression) transformNode(node.getChild(0),
 					statement));
 		}
 
 		statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitNew(Tree node) {
 
 		NewExpression expression = new NewExpression(this.parent);
 
 		Keyword newKeyword = new Keyword(expression, Keywords.NEW);
 		newKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		newKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		expression.setNewKeyword(newKeyword);
 
 		expression.setObjectClass((Identifier) transformNode(node.getChild(0),
 				expression));
 
 		expression.setStart(getTokenOffset(node.getTokenStartIndex()));
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = expression;
 		return true;
 	}
 
 	protected boolean visitCatch(Tree node) {
 
 		CatchClause catchClause = new CatchClause(this.parent);
 
 		Keyword catchKeyword = new Keyword(catchClause, Keywords.CATCH);
 		catchKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		catchKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		catchClause.setCatchKeyword(catchKeyword);
 
 		catchClause.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex() + 1, node.getChild(0)
 				.getTokenStartIndex()));
 
 		catchClause.setException((Identifier) transformNode(node.getChild(0),
 				catchClause));
 
 		int statementIndex = 1;
 
 		if (node.getChild(1).getType() == JSParser.IF) {
 			statementIndex++;
 
 			ExceptionFilter filter = new ExceptionFilter(catchClause);
 
 			Tree filterNode = node.getChild(1);
 
 			Keyword ifKeyword = new Keyword(catchClause, Keywords.IF);
 			ifKeyword.setStart(getTokenOffset(filterNode.getTokenStartIndex()));
 			ifKeyword
 					.setEnd(getTokenOffset(filterNode.getTokenStartIndex() + 1));
 			filter.setIfKeyword(ifKeyword);
 
 			filter.setExpression((Expression) transformNode(filterNode
 					.getChild(0), filter));
 
 			filter.setStart(getTokenOffset(filterNode.getTokenStartIndex()));
 			filter.setEnd(getTokenOffset(filterNode.getTokenStopIndex() + 1));
 
 			catchClause.setExceptionFilter(filter);
 		}
 
 		catchClause.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(
 				statementIndex - 1).getTokenStopIndex() + 1, node.getChild(
 				statementIndex).getTokenStartIndex()));
 
 		catchClause.setStatement(transformStatementNode(node
 				.getChild(statementIndex), catchClause));
 
 		catchClause.setStart(getTokenOffset(node.getTokenStartIndex()));
 		catchClause.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = catchClause;
 		return true;
 	}
 
 	protected boolean visitFinally(Tree node) {
 
 		FinallyClause finallyClause = new FinallyClause(this.parent);
 
 		Keyword finallyKeyword = new Keyword(finallyClause, Keywords.FINALLY);
 		finallyKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		finallyKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		finallyClause.setFinallyKeyword(finallyKeyword);
 
 		finallyClause.setStatement(transformStatementNode(node.getChild(0),
 				finallyClause));
 
 		finallyClause.setStart(getTokenOffset(node.getTokenStartIndex()));
 		finallyClause.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = finallyClause;
 		return true;
 	}
 
 	protected boolean visitArray(Tree node) {
 
 		ArrayInitializer array = new ArrayInitializer(this.parent);
 
 		array.setLB(getTokenOffset(JSParser.LBRACK, node.getTokenStartIndex(),
 				node.getTokenStartIndex()));
 
 		List<ASTNode> items = new ArrayList<ASTNode>(node.getChildCount());
 		List<Integer> commas = new ArrayList<Integer>();
 
 		for (int i = 0; i < node.getChildCount(); i++) {
 			Tree child = node.getChild(i);
 
 			if (child.getType() != JSParser.ITEM)
 				throw new UnsupportedOperationException("ITEM expected");
 
 			items.add(transformNode(child.getChild(0), array));
 
 			if (i > 0) {
 				commas.add(new Integer(getTokenOffset(JSParser.COMMA, node
 						.getChild(i - 1).getTokenStopIndex() + 1, child
 						.getTokenStartIndex())));
 			}
 		}
 
 		array.setItems(items);
 		array.setCommas(commas);
 
 		array.setRB(getTokenOffset(JSParser.RBRACK, node.getTokenStopIndex(),
 				node.getTokenStopIndex()));
 
 		array.setStart(array.getLB());
 		array.setEnd(array.getRB() + 1);
 
 		this.result = array;
 		return true;
 	}
 
 	protected boolean visitByIndex(Tree node) {
 
 		GetArrayItemExpression item = new GetArrayItemExpression(this.parent);
 
 		item.setArray((Expression) transformNode(node.getChild(0), item));
 		item.setIndex((Expression) transformNode(node.getChild(1), item));
 
 		item.setLB(getTokenOffset(JSParser.LBRACK, getRealTokenStopIndex(node
 				.getChild(0)) + 1, node.getChild(1).getTokenStartIndex()));
 
 		item.setRB(getTokenOffset(JSParser.RBRACK, node.getChild(1)
 				.getTokenStopIndex() + 1, tokens.size() + 1));
 
 		item.setStart(item.getArray().sourceStart());
 		item.setEnd(item.getRB() + 1);
 
 		this.result = item;
 		return true;
 	}
 
 	protected boolean visitCommaExpression(Tree node) {
 
 		CommaExpression expression = new CommaExpression(this.parent);
 
 		List<ASTNode> items = new ArrayList<ASTNode>(node.getChildCount());
 		List<Integer> commas = new ArrayList<Integer>();
 
 		for (int i = 0; i < node.getChildCount(); i++) {
 			items.add(transformNode(node.getChild(i), expression));
 
 			if (i > 0)
 				commas.add(new Integer(getTokenOffset(JSParser.COMMA, node
 						.getChild(i - 1).getTokenStopIndex(), node.getChild(i)
 						.getTokenStartIndex())));
 		}
 
 		expression.setItems(items);
 		expression.setCommas(commas);
 
 		expression.setStart(getTokenOffset(node.getTokenStartIndex()));
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = expression;
 		return true;
 	}
 
 	protected boolean visitRegExp(Tree node) {
 
 		RegExpLiteral regexp = new RegExpLiteral(this.parent);
 		regexp.setText(node.getText());
 
 		regexp.setStart(getTokenOffset(node.getTokenStartIndex()));
 		regexp.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = regexp;
 		return true;
 	}
 
 	protected boolean visitWith(Tree node) {
 
 		WithStatement statement = new WithStatement(this.parent);
 
 		Keyword withKeyword = new Keyword(statement, Keywords.WITH);
 		withKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		withKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		statement.setWithKeyword(withKeyword);
 
 		statement.setLP(getTokenOffset(JSParser.LPAREN, node
 				.getTokenStartIndex(), node.getChild(0).getTokenStartIndex()));
 
 		statement.setExpression((Expression) transformNode(node.getChild(0),
 				statement));
 
 		statement.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 				.getTokenStopIndex() + 1, node.getTokenStopIndex()));
 
 		if (node.getChildCount() > 1)
 			statement.setStatement(transformStatementNode(node.getChild(1),
 					statement));
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitThis(Tree node) {
 
 		ThisExpression expression = new ThisExpression(this.parent);
 
 		Keyword thisKeyword = new Keyword(expression, Keywords.THIS);
 		thisKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		thisKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		expression.setThisKeyword(thisKeyword);
 
 		expression.setStart(getTokenOffset(node.getTokenStartIndex()));
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = expression;
 		return true;
 	}
 
 	protected boolean visitLabelled(Tree node) {
 
 		LabelledStatement statement = new LabelledStatement(this.parent);
 
 		Label label = new Label(statement);
 		label.setText(node.getChild(0).getText());
 		label.setStart(getTokenOffset(node.getChild(0).getTokenStartIndex()));
 		label.setEnd(getTokenOffset(node.getChild(0).getTokenStopIndex() + 1));
 		statement.setLabel(label);
 
 		statement.setColonPosition(getTokenOffset(JSParser.COLON, node
 				.getChild(0).getTokenStopIndex() + 1,
 				node.getTokenStopIndex() + 1));
 
 		if (node.getChildCount() > 1) {
 			statement.setStatement(transformStatementNode(node.getChild(1),
 					statement));
 		}
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitDelete(Tree node) {
 
 		DeleteStatement statement = new DeleteStatement(this.parent);
 
 		Keyword deleteKeyword = new Keyword(statement, Keywords.DELETE);
 		deleteKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		deleteKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		statement.setDeleteKeyword(deleteKeyword);
 
 		statement.setExpression((Expression) transformNode(node.getChild(0),
 				statement));
 
 		statement.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		statement.setStart(getTokenOffset(node.getTokenStartIndex()));
 		statement.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitGet(Tree node) {
 
 		GetMethod method = new GetMethod(this.parent);
 
 		Keyword getKeyword = new Keyword(method, Keywords.GET);
 		getKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		getKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		method.setGetKeyword(getKeyword);
 
 		method.setName((Identifier) transformNode(node.getChild(0), method));
 
 		method
 				.setLP(getTokenOffset(JSParser.LPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		method
 				.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		method.setBody((StatementBlock) transformStatementNode(
 				node.getChild(1), method));
 
 		method.setStart(getTokenOffset(node.getTokenStartIndex()));
 		method.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = method;
 		return true;
 	}
 
 	protected boolean visitSet(Tree node) {
 		SetMethod method = new SetMethod(this.parent);
 
 		Keyword setKeyword = new Keyword(method, Keywords.SET);
 		setKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		setKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		method.setSetKeyword(setKeyword);
 
 		method.setName((Identifier) transformNode(node.getChild(0), method));
 
 		method
 				.setLP(getTokenOffset(JSParser.LPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		method
 				.setArgument((Identifier) transformNode(node.getChild(1),
 						method));
 
 		method
 				.setRP(getTokenOffset(JSParser.RPAREN, node.getChild(0)
 						.getTokenStopIndex() + 1, node.getChild(2)
 						.getTokenStartIndex()));
 
 		method.setBody((StatementBlock) transformStatementNode(
 				node.getChild(2), method));
 
 		method.setStart(getTokenOffset(node.getTokenStartIndex()));
 		method.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = method;
 		return true;
 	}
 
 	protected boolean visitNull(Tree node) {
 
 		NullExpression expression = new NullExpression(this.parent);
 
 		expression.setStart(getTokenOffset(node.getTokenStartIndex()));
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = expression;
 		return true;
 	}
 
 	protected boolean visitTypeOf(Tree node) {
 
 		TypeOfExpression expression = new TypeOfExpression(this.parent);
 
 		Keyword typeofKeyword = new Keyword(this.parent, Keywords.TYPEOF);
 		typeofKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		typeofKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		expression.setTypeOfKeyword(typeofKeyword);
 
 		expression.setExpression((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setStart(getTokenOffset(node.getTokenStartIndex()));
 		expression.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = expression;
 		return true;
 	}
 
 	protected boolean visitConst(Tree node) {
 		ConstDeclaration declaration = new ConstDeclaration(this.parent);
 
 		Keyword constKeyword = new Keyword(this.parent, Keywords.CONST);
 		constKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		constKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		declaration.setConstKeyword(constKeyword);
 
 		List<Integer> commas = new ArrayList<Integer>();
 		List<ASTNode> consts = new ArrayList<ASTNode>(node.getChildCount());
 		for (int i = 0; i < node.getChildCount(); i++) {
 			consts.add(transformNode(node.getChild(i), declaration));
 			if (i > 0)
 				commas.add(new Integer(getTokenOffset(JSParser.COMMA, node
 						.getChild(i - 1).getTokenStopIndex() + 1, node
 						.getChild(i).getTokenStartIndex())));
 		}
 		declaration.setConsts(consts);
 		declaration.setCommas(commas);
 
 		declaration.setSemicolonPosition(getTokenOffset(JSParser.SEMIC, node
 				.getTokenStopIndex(), node.getTokenStopIndex()));
 
 		declaration.setStart(getTokenOffset(node.getTokenStartIndex()));
 		declaration.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = declaration;
 		return true;
 	}
 
 	protected boolean visitScript(Tree node) {
 		Script script = new Script();
 		for (int i = 0; i < node.getChildCount(); i++) {
 			script
 					.addStatement(transformStatementNode(node.getChild(i),
 							script));
 		}
 		addComments(script);
 		script.setStart(0);
 		script.setEnd(source.length());
 		this.result = script;
 		return true;
 	}
 
 	private void addComments(Script script) {
 		for (int i = 0; i < tokens.size(); i++) {
 			Token token = tokens.get(i);
 			switch (token.getType()) {
 			case JSParser.MultiLineComment:
 				script.addComment(visitMultiLineComment(token));
 				break;
 			case JSParser.SingleLineComment:
 				script.addComment(visitSingleLineComment(token));
 				break;
 			}
 		}
 	}
 
 	private Comment visitMultiLineComment(Token token) {
 		Comment comment = new MultiLineComment();
 		comment.setText(token.getText());
 		comment.setStart(getTokenOffset(token.getTokenIndex()));
 		comment.setEnd(comment.sourceStart() + token.getText().length());
 
 		return comment;
 	}
 
 	private Comment visitSingleLineComment(Token token) {
 		Comment comment = new SingleLineComment();
 		comment.setText(token.getText());
 		comment.setStart(getTokenOffset(token.getTokenIndex()));
 		comment.setEnd(comment.sourceStart() + token.getText().length());
 
 		return comment;
 	}
 
 	protected boolean visitBooleanLiteral(Tree node) {
 
 		BooleanLiteral bool = new BooleanLiteral(this.parent);
 		bool.setText(node.getText());
 
 		bool.setStart(getTokenOffset(node.getTokenStartIndex()));
 		bool.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 
 		this.result = bool;
 		return true;
 	}
 
 	protected boolean visitVoid(Tree node) {
 		VoidOperator expression = new VoidOperator(this.parent);
 
 		Keyword voidKeyword = new Keyword(expression, Keywords.VOID);
 		voidKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		voidKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		expression.setVoidKeyword(voidKeyword);
 
 		expression.setExpression((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setStart(expression.getVoidKeyword().sourceStart());
 		expression.setEnd(expression.getExpression().sourceEnd());
 
 		this.result = expression;
 		return true;
 	}
 
 	protected boolean visitXmlLiteral(Tree node) {
 		XmlLiteral xml = new XmlLiteral(this.parent);
 
 		xml.setXml(node.getText());
 
 		xml.setStart(getTokenOffset(node.getTokenStartIndex()));
 		xml.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 
 		this.result = xml;
 		return false;
 	}
 
 	protected boolean visitNamespace(Tree node) {
 
 		DefaultXmlNamespaceStatement statement = new DefaultXmlNamespaceStatement(
 				this.parent);
 
 		Keyword defaultKeyword = new Keyword(statement, Keywords.DEFAULT);
 		defaultKeyword.setStart(getTokenOffset(node.getChild(0)
 				.getTokenStartIndex()));
 		defaultKeyword.setEnd(defaultKeyword.sourceStart()
 				+ Keywords.DEFAULT.length());
 		statement.setDefaultKeyword(defaultKeyword);
 
 		Keyword xmlKeyword = new Keyword(statement, Keywords.XML);
 		xmlKeyword.setStart(getTokenOffset(node.getChild(1)
 				.getTokenStartIndex()));
 		xmlKeyword.setEnd(xmlKeyword.sourceStart() + Keywords.XML.length());
 		statement.setXmlKeyword(xmlKeyword);
 
 		Keyword namespaceKeyword = new Keyword(statement, Keywords.NAMESPACE);
 		namespaceKeyword.setStart(getTokenOffset(JSParser.NAMESPACE, node
 				.getTokenStartIndex(), node.getTokenStopIndex()));
 		namespaceKeyword.setEnd(namespaceKeyword.sourceStart()
 				+ Keywords.NAMESPACE.length());
 		statement.setNamespaceKeyword(namespaceKeyword);
 
 		statement.setAssignOperation(getTokenOffset(node.getChild(2)
 				.getTokenStartIndex()));
 
 		StringLiteral value = new StringLiteral(statement);
 		value.setStart(getTokenOffset(node.getChild(3).getTokenStartIndex()));
 		value.setEnd(getTokenOffset(node.getChild(3).getTokenStartIndex()) + 1);
 		value.setText(node.getChild(3).getText());
 		statement.setValue(value);
 
 		Token token = tokens.get(node.getTokenStopIndex());
 		if (token.getType() == JSParser.SEMIC) {
 			statement.setSemicolonPosition(getTokenOffset(node
 					.getTokenStopIndex()));
 
 			statement.setEnd(statement.getSemicolonPosition() + 1);
 		} else {
 			statement.setEnd(statement.getValue().sourceEnd());
 		}
 		statement.setStart(statement.getDefaultKeyword().sourceStart());
 
 		this.result = statement;
 		return true;
 	}
 
 	protected boolean visitXmlAttribute(Tree node) {
 
 		XmlAttributeIdentifier id = new XmlAttributeIdentifier(parent);
 		id.setName(node.getText());
 
 		id.setStart(getTokenOffset(node.getTokenStartIndex()));
 		id.setEnd(id.sourceStart() + id.getName().length());
 
 		this.result = id;
 		return true;
 	}
 
 	protected boolean visitAsterisk(Tree node) {
 		AsteriskExpression asterisk = new AsteriskExpression(parent);
 
 		asterisk.setStart(getTokenOffset(node.getTokenStartIndex()));
 		asterisk.setEnd(asterisk.sourceStart() + node.getText().length());
 
 		this.result = asterisk;
 		return true;
 	}
 
 	protected boolean visitGetAllChildren(Tree node) {
 		GetAllChildrenExpression expression = new GetAllChildrenExpression(
 				this.parent);
 
 		expression.setObject((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setProperty((Expression) transformNode(node.getChild(1),
 				expression));
 
 		expression.setDotDotPosition(getTokenOffset(JSParser.DOTDOT,
 				getRealTokenStopIndex(node.getChild(0)) + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		Assert.isTrue(expression.getObject().sourceStart() >= 0);
 		Assert.isTrue(expression.getProperty().sourceEnd() > 0);
 
 		expression.setStart(expression.getObject().sourceStart());
 		expression.setEnd(expression.getProperty().sourceEnd());
 
 		this.result = expression;
 		return true;
 	}
 
 	protected boolean visitGetLocalName(Tree node) {
 		GetLocalNameExpression expression = new GetLocalNameExpression(
 				this.parent);
 
 		expression.setNamespace((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setLocalName((Expression) transformNode(node.getChild(1),
 				expression));
 
 		expression.setColonColonPosition(getTokenOffset(JSParser.COLONCOLON,
 				getRealTokenStopIndex(node.getChild(0)) + 1, node.getChild(1)
 						.getTokenStartIndex()));
 
 		Assert.isTrue(expression.getNamespace().sourceStart() >= 0);
 		Assert.isTrue(expression.getLocalName().sourceEnd() > 0);
 
 		expression.setStart(expression.getNamespace().sourceStart());
 		expression.setEnd(expression.getLocalName().sourceEnd());
 
 		this.result = expression;
 		return true;
 	}
 
 	protected boolean visitHexIntegerLiteral(Tree node) {
 		DecimalLiteral number = new DecimalLiteral(this.parent);
 		number.setText(node.getText());
 		number.setStart(getTokenOffset(node.getTokenStartIndex()));
 		number.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = number;
 		return true;
 	}
 
 	protected boolean visitOctalIntegerLiteral(Tree node) {
 		DecimalLiteral number = new DecimalLiteral(this.parent);
 		number.setText(node.getText());
 		number.setStart(getTokenOffset(node.getTokenStartIndex()));
 		number.setEnd(getTokenOffset(node.getTokenStopIndex() + 1));
 
 		this.result = number;
 		return true;
 	}
 
 	protected boolean visitYield(Tree node) {
 		YieldOperator expression = new YieldOperator(this.parent);
 
 		Keyword yieldKeyword = new Keyword(expression, Keywords.YIELD);
 		yieldKeyword.setStart(getTokenOffset(node.getTokenStartIndex()));
 		yieldKeyword.setEnd(getTokenOffset(node.getTokenStartIndex() + 1));
 		expression.setVoidKeyword(yieldKeyword);
 
 		expression.setExpression((Expression) transformNode(node.getChild(0),
 				expression));
 
 		expression.setStart(expression.getVoidKeyword().sourceStart());
 		expression.setEnd(expression.getExpression().sourceEnd());
 
 		this.result = expression;
 		return true;
 	}
 
 }
