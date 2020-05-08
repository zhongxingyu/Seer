 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.validation;
 
 import java.util.List;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.javascript.ast.ASTVisitor;
 import org.eclipse.dltk.javascript.ast.ArrayInitializer;
 import org.eclipse.dltk.javascript.ast.AsteriskExpression;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.BooleanLiteral;
 import org.eclipse.dltk.javascript.ast.BreakStatement;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.CatchClause;
 import org.eclipse.dltk.javascript.ast.CommaExpression;
 import org.eclipse.dltk.javascript.ast.ConditionalOperator;
 import org.eclipse.dltk.javascript.ast.ConstStatement;
 import org.eclipse.dltk.javascript.ast.ContinueStatement;
 import org.eclipse.dltk.javascript.ast.DecimalLiteral;
 import org.eclipse.dltk.javascript.ast.DefaultXmlNamespaceStatement;
 import org.eclipse.dltk.javascript.ast.DeleteStatement;
 import org.eclipse.dltk.javascript.ast.DoWhileStatement;
 import org.eclipse.dltk.javascript.ast.EmptyExpression;
 import org.eclipse.dltk.javascript.ast.EmptyStatement;
 import org.eclipse.dltk.javascript.ast.Expression;
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
 import org.eclipse.dltk.javascript.ast.LabelledStatement;
 import org.eclipse.dltk.javascript.ast.Method;
 import org.eclipse.dltk.javascript.ast.NewExpression;
 import org.eclipse.dltk.javascript.ast.NullExpression;
 import org.eclipse.dltk.javascript.ast.ObjectInitializer;
 import org.eclipse.dltk.javascript.ast.ObjectInitializerPart;
 import org.eclipse.dltk.javascript.ast.ParenthesizedExpression;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.RegExpLiteral;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.SetMethod;
 import org.eclipse.dltk.javascript.ast.SimpleType;
 import org.eclipse.dltk.javascript.ast.Statement;
 import org.eclipse.dltk.javascript.ast.StatementBlock;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.ast.SwitchStatement;
 import org.eclipse.dltk.javascript.ast.ThisExpression;
 import org.eclipse.dltk.javascript.ast.ThrowStatement;
 import org.eclipse.dltk.javascript.ast.TryStatement;
 import org.eclipse.dltk.javascript.ast.TypeOfExpression;
 import org.eclipse.dltk.javascript.ast.UnaryOperation;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.ast.VariableStatement;
 import org.eclipse.dltk.javascript.ast.VoidExpression;
 import org.eclipse.dltk.javascript.ast.VoidOperator;
 import org.eclipse.dltk.javascript.ast.WhileStatement;
 import org.eclipse.dltk.javascript.ast.WithStatement;
 import org.eclipse.dltk.javascript.ast.XmlAttributeIdentifier;
 import org.eclipse.dltk.javascript.ast.XmlLiteral;
 import org.eclipse.dltk.javascript.ast.YieldOperator;
 
 public class AbstractNavigationVisitor<E> extends ASTVisitor<E> {
 
 	@Override
 	public E visitArrayInitializer(ArrayInitializer node) {
 		for (ASTNode item : node.getItems()) {
 			visit(item);
 		}
 		return null;
 	}
 
 	@Override
 	public E visitAsteriskExpression(AsteriskExpression node) {
 		return null;
 	}
 
 	@Override
 	public E visitBinaryOperation(BinaryOperation node) {
 		visit(node.getLeftExpression());
 		visit(node.getRightExpression());
 		return null;
 	}
 
 	@Override
 	public E visitBooleanLiteral(BooleanLiteral node) {
 		return null;
 	}
 
 	@Override
 	public E visitBreakStatement(BreakStatement node) {
 		return null;
 	}
 
 	@Override
 	public E visitCallExpression(CallExpression node) {
 		visit(node.getExpression());
 		for (ASTNode argument : node.getArguments()) {
 			visit(argument);
 		}
 		return null;
 	}
 
 	@Override
 	public E visitCommaExpression(CommaExpression node) {
 		for (ASTNode item : node.getItems()) {
 			visit(item);
 		}
 		return null;
 	}
 
 	@Override
 	public E visitConditionalOperator(ConditionalOperator node) {
 		visit(node.getCondition());
 		visit(node.getTrueValue());
 		visit(node.getFalseValue());
 		return null;
 	}
 
 	@Override
 	public E visitConstDeclaration(ConstStatement node) {
 		processVariables(node.getVariables());
 		return null;
 	}
 
 	private void processVariables(List<VariableDeclaration> variables) {
 		for (VariableDeclaration declaration : variables) {
 			if (declaration.getInitializer() != null) {
 				visit(declaration.getInitializer());
 			}
 		}
 	}
 
 	@Override
 	public E visitContinueStatement(ContinueStatement node) {
 		return null;
 	}
 
 	@Override
 	public E visitDecimalLiteral(DecimalLiteral node) {
 		return null;
 	}
 
 	@Override
 	public E visitDefaultXmlNamespace(DefaultXmlNamespaceStatement node) {
 		return null;
 	}
 
 	@Override
 	public E visitDeleteStatement(DeleteStatement node) {
 		visit(node.getExpression());
 		return null;
 	}
 
 	@Override
 	public E visitDoWhileStatement(DoWhileStatement node) {
 		final E result = visit(node.getBody());
 		visitCondition(node.getCondition());
 		return result;
 	}
 
 	protected void visitCondition(Expression condition) {
 		visit(condition);
 	}
 
 	@Override
 	public E visitEmptyExpression(EmptyExpression node) {
 		return null;
 	}
 
 	@Override
 	public E visitEmptyStatement(EmptyStatement node) {
 		return null;
 	}
 
 	@Override
 	public E visitForEachInStatement(ForEachInStatement node) {
 		visit(node.getItem());
 		visit(node.getIterator());
 		return visit(node.getBody());
 	}
 
 	@Override
 	public E visitForInStatement(ForInStatement node) {
 		visit(node.getItem());
 		visit(node.getIterator());
 		return visit(node.getBody());
 	}
 
 	@Override
 	public E visitForStatement(ForStatement node) {
 		visit(node.getInitial());
 		visit(node.getCondition());
 		visit(node.getStep());
 		return visit(node.getBody());
 	}
 
 	@Override
 	public E visitFunctionStatement(FunctionStatement node) {
 		return visit(node.getBody());
 	}
 
 	@Override
 	public E visitGetAllChildrenExpression(GetAllChildrenExpression node) {
 		visit(node.getObject());
 		visit(node.getProperty());
 		return null;
 	}
 
 	@Override
 	public E visitGetArrayItemExpression(GetArrayItemExpression node) {
 		visit(node.getArray());
 		visit(node.getIndex());
 		return null;
 	}
 
 	@Override
 	public E visitGetLocalNameExpression(GetLocalNameExpression node) {
 		visit(node.getNamespace());
 		visit(node.getLocalName());
 		return null;
 	}
 
 	@Override
 	public E visitIdentifier(Identifier node) {
 		return null;
 	}
 
 	@Override
 	public E visitIfStatement(IfStatement node) {
 		visitCondition(node.getCondition());
 		if (node.getThenStatement() != null) {
 			visit(node.getThenStatement());
 		}
 		if (node.getElseStatement() != null) {
 			visit(node.getElseStatement());
 		}
 		return null;
 	}
 
 	@Override
 	public E visitLabelledStatement(LabelledStatement node) {
 		visit(node.getStatement());
 		return null;
 	}
 
 	@Override
 	public E visitNewExpression(NewExpression node) {
 		visit(node.getObjectClass());
 		return null;
 	}
 
 	@Override
 	public E visitNullExpression(NullExpression node) {
 		return null;
 	}
 
 	@Override
 	public E visitObjectInitializer(ObjectInitializer node) {
 		for (ObjectInitializerPart part : node.getInitializers()) {
 			if (part instanceof GetMethod) {
 				visitMethod((GetMethod) part);
 			} else if (part instanceof SetMethod) {
 				visitMethod((SetMethod) part);
 			} else if (part instanceof PropertyInitializer) {
 				final PropertyInitializer pi = (PropertyInitializer) part;
 				visit(pi.getName());
 				visit(pi.getValue());
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param part
 	 */
 	protected E visitMethod(Method method) {
 		return visit(method.getBody());
 	}
 
 	@Override
 	public E visitParenthesizedExpression(ParenthesizedExpression node) {
 		visit(node.getExpression());
 		return null;
 	}
 
 	@Override
 	public E visitPropertyExpression(PropertyExpression node) {
 		visit(node.getObject());
 		visit(node.getProperty());
 		return null;
 	}
 
 	@Override
 	public E visitRegExpLiteral(RegExpLiteral node) {
 		return null;
 	}
 
 	@Override
 	public E visitReturnStatement(ReturnStatement node) {
 		if (node.getValue() != null) {
 			visit(node.getValue());
 		}
 		return null;
 	}
 
 	@Override
 	public E visitScript(Script node) {
 		for (Statement statement : node.getStatements()) {
 			visit(statement);
 		}
 		return null;
 	}
 
 	@Override
 	public E visitSimpleType(SimpleType node) {
 		return null;
 	}
 
 	@Override
 	public E visitStatementBlock(StatementBlock node) {
 		for (Statement statement : node.getStatements()) {
 			visit(statement);
 		}
 		return null;
 	}
 
 	@Override
 	public E visitStringLiteral(StringLiteral node) {
 		return null;
 	}
 
 	@Override
 	public E visitSwitchStatement(SwitchStatement node) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public E visitThisExpression(ThisExpression node) {
 		return null;
 	}
 
 	@Override
 	public E visitThrowStatement(ThrowStatement node) {
 		if (node.getException() != null) {
 			visit(node.getException());
 		}
 		return null;
 	}
 
 	@Override
 	public E visitTryStatement(TryStatement node) {
 		visit(node.getBody());
 		for (CatchClause catchClause : node.getCatches()) {
 			// TODO
 			final Statement catchStatement = catchClause.getStatement();
 			if (catchStatement != null) {
 				visit(catchStatement);
 			}
 		}
 		if (node.getFinally() != null) {
 			final Statement finallyStatement = node.getFinally().getStatement();
 			if (finallyStatement != null) {
 				visit(finallyStatement);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public E visitTypeOfExpression(TypeOfExpression node) {
 		visit(node.getExpression());
 		return null;
 	}
 
 	@Override
 	public E visitUnaryOperation(UnaryOperation node) {
 		visit(node.getExpression());
 		return null;
 	}
 
 	@Override
	public E visitVariableStatement(VariableStatement node) {
 		processVariables(node.getVariables());
 		return null;
 	}
 
 	@Override
 	public E visitVoidExpression(VoidExpression node) {
 		visit(node.getExpression());
 		return null;
 	}
 
 	@Override
 	public E visitVoidOperator(VoidOperator node) {
 		visit(node.getExpression());
 		return null;
 	}
 
 	@Override
 	public E visitWhileStatement(WhileStatement node) {
 		visitCondition(node.getCondition());
 		return visit(node.getBody());
 	}
 
 	@Override
 	public E visitWithStatement(WithStatement node) {
 		visit(node.getExpression());
 		visit(node.getStatement());
 		return null;
 	}
 
 	@Override
 	public E visitXmlLiteral(XmlLiteral node) {
 		return null;
 	}
 
 	@Override
 	public E visitXmlPropertyIdentifier(XmlAttributeIdentifier node) {
 		visit(node.getExpression());
 		return null;
 	}
 
 	@Override
 	public E visitYieldOperator(YieldOperator node) {
 		visit(node.getParent());
 		return null;
 	}
 
 }
