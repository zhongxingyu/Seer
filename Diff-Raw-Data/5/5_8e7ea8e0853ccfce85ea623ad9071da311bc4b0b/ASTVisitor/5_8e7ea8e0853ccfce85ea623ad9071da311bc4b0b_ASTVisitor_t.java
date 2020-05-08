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
 
 package org.eclipse.dltk.javascript.ast;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.dltk.ast.ASTNode;
 
 public abstract class ASTVisitor<E> {
 
 	public E visit(Collection<? extends ASTNode> nodes) {
 		E result = null;
 		for (ASTNode node : nodes) {
 			result = visit(node);
 		}
 		return result;
 	}
 
 	private static interface Handler {
 		<E> E handle(ASTVisitor<E> visitor, ASTNode node);
 	}
 
 	private static final Map<Class<? extends ASTNode>, Handler> HANDLERS = new HashMap<Class<? extends ASTNode>, Handler>();
 
 	static {
 		HANDLERS.put(ArrayInitializer.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitArrayInitializer((ArrayInitializer) node);
 			}
 		});
 		HANDLERS.put(BinaryOperation.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitBinaryOperation((BinaryOperation) node);
 			}
 		});
 		HANDLERS.put(BooleanLiteral.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitBooleanLiteral((BooleanLiteral) node);
 			}
 		});
 		HANDLERS.put(BreakStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitBreakStatement((BreakStatement) node);
 			}
 		});
 		HANDLERS.put(CallExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitCallExpression((CallExpression) node);
 			}
 		});
 		HANDLERS.put(CommaExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitCommaExpression((CommaExpression) node);
 			}
 		});
 		HANDLERS.put(ConditionalOperator.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor
 						.visitConditionalOperator((ConditionalOperator) node);
 			}
 		});
 		HANDLERS.put(ConstStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitConstDeclaration((ConstStatement) node);
 			}
 		});
 		HANDLERS.put(ContinueStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitContinueStatement((ContinueStatement) node);
 			}
 		});
 		HANDLERS.put(DecimalLiteral.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitDecimalLiteral((DecimalLiteral) node);
 			}
 		});
 		HANDLERS.put(DeleteStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitDeleteStatement((DeleteStatement) node);
 			}
 		});
 		HANDLERS.put(DoWhileStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitDoWhileStatement((DoWhileStatement) node);
 			}
 		});
 		HANDLERS.put(EmptyExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitEmptyExpression((EmptyExpression) node);
 			}
 		});
 		HANDLERS.put(ForStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitForStatement((ForStatement) node);
 			}
 		});
 		HANDLERS.put(Script.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitScript((Script) node);
 			}
 		});
 		HANDLERS.put(ForInStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitForInStatement((ForInStatement) node);
 			}
 		});
 		HANDLERS.put(ForEachInStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor
 						.visitForEachInStatement((ForEachInStatement) node);
 			}
 		});
 		HANDLERS.put(FunctionStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitFunctionStatement((FunctionStatement) node);
 			}
 		});
 		HANDLERS.put(Identifier.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitIdentifier((Identifier) node);
 			}
 		});
 		HANDLERS.put(SimpleType.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitSimpleType((SimpleType) node);
 			}
 		});
 		HANDLERS.put(IfStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitIfStatement((IfStatement) node);
 			}
 		});
 		HANDLERS.put(NewExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitNewExpression((NewExpression) node);
 			}
 		});
 		HANDLERS.put(NullExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitNullExpression((NullExpression) node);
 			}
 		});
 		HANDLERS.put(ObjectInitializer.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitObjectInitializer((ObjectInitializer) node);
 			}
 		});
 		HANDLERS.put(ParenthesizedExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor
 						.visitParenthesizedExpression((ParenthesizedExpression) node);
 			}
 		});
 		HANDLERS.put(RegExpLiteral.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitRegExpLiteral((RegExpLiteral) node);
 			}
 		});
 		HANDLERS.put(ReturnStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitReturnStatement((ReturnStatement) node);
 			}
 		});
 		HANDLERS.put(StringLiteral.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitStringLiteral((StringLiteral) node);
 			}
 		});
 		HANDLERS.put(SwitchStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitSwitchStatement((SwitchStatement) node);
 			}
 		});
 		HANDLERS.put(ThrowStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitThrowStatement((ThrowStatement) node);
 			}
 		});
 		HANDLERS.put(GetArrayItemExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor
 						.visitGetArrayItemExpression((GetArrayItemExpression) node);
 			}
 		});
 		HANDLERS.put(LabelledStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitLabelledStatement((LabelledStatement) node);
 			}
 		});
 		HANDLERS.put(PropertyExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor
 						.visitPropertyExpression((PropertyExpression) node);
 			}
 		});
 		HANDLERS.put(StatementBlock.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitStatementBlock((StatementBlock) node);
 			}
 		});
 		HANDLERS.put(ThisExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitThisExpression((ThisExpression) node);
 			}
 		});
 		HANDLERS.put(TryStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitTryStatement((TryStatement) node);
 			}
 		});
 		HANDLERS.put(TypeOfExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitTypeOfExpression((TypeOfExpression) node);
 			}
 		});
 		HANDLERS.put(UnaryOperation.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitUnaryOperation((UnaryOperation) node);
 			}
 		});
 		HANDLERS.put(VariableStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
				return visitor.visitVariableStatement((VariableStatement) node);
 			}
 		});
 		HANDLERS.put(VoidExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitVoidExpression((VoidExpression) node);
 			}
 		});
 		HANDLERS.put(WhileStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitWhileStatement((WhileStatement) node);
 			}
 		});
 		HANDLERS.put(WithStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitWithStatement((WithStatement) node);
 			}
 		});
 		HANDLERS.put(VoidOperator.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitVoidOperator((VoidOperator) node);
 			}
 		});
 		HANDLERS.put(XmlLiteral.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitXmlLiteral((XmlLiteral) node);
 			}
 		});
 		HANDLERS.put(DefaultXmlNamespaceStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor
 						.visitDefaultXmlNamespace((DefaultXmlNamespaceStatement) node);
 			}
 		});
 		HANDLERS.put(XmlAttributeIdentifier.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor
 						.visitXmlPropertyIdentifier((XmlAttributeIdentifier) node);
 			}
 		});
 		HANDLERS.put(AsteriskExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor
 						.visitAsteriskExpression((AsteriskExpression) node);
 			}
 		});
 		HANDLERS.put(GetAllChildrenExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor
 						.visitGetAllChildrenExpression((GetAllChildrenExpression) node);
 			}
 		});
 		HANDLERS.put(GetLocalNameExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor
 						.visitGetLocalNameExpression((GetLocalNameExpression) node);
 			}
 		});
 		HANDLERS.put(YieldOperator.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitYieldOperator((YieldOperator) node);
 			}
 		});
 		HANDLERS.put(ErrorExpression.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitErrorExpression((ErrorExpression) node);
 			}
 		});
 		HANDLERS.put(EmptyStatement.class, new Handler() {
 			public <E> E handle(ASTVisitor<E> visitor, ASTNode node) {
 				return visitor.visitEmptyStatement((EmptyStatement) node);
 			}
 		});
 	}
 
 	public E visit(ASTNode node) {
 		final Handler handler = HANDLERS.get(node.getClass());
 		if (handler != null) {
 			return handler.handle(this, node);
 		} else {
 			return visitUnknownNode(node);
 		}
 	}
 
 	public abstract E visitArrayInitializer(ArrayInitializer node);
 
 	public abstract E visitBinaryOperation(BinaryOperation node);
 
 	public abstract E visitBooleanLiteral(BooleanLiteral node);
 
 	public abstract E visitBreakStatement(BreakStatement node);
 
 	public abstract E visitCallExpression(CallExpression node);
 
 	@Deprecated
 	public final E visitCaseClause(CaseClause node) {
 		return null;
 	}
 
 	@Deprecated
 	public final E visitCatchClause(CatchClause node) {
 		return null;
 	}
 
 	public abstract E visitCommaExpression(CommaExpression node);
 
 	public abstract E visitConditionalOperator(ConditionalOperator node);
 
 	public abstract E visitConstDeclaration(ConstStatement node);
 
 	public abstract E visitContinueStatement(ContinueStatement node);
 
 	public abstract E visitDecimalLiteral(DecimalLiteral node);
 
 	@Deprecated
 	public final E visitDefaultClause(DefaultClause node) {
 		return null;
 	}
 
 	public abstract E visitDeleteStatement(DeleteStatement node);
 
 	public abstract E visitDoWhileStatement(DoWhileStatement node);
 
 	public abstract E visitEmptyExpression(EmptyExpression node);
 
 	@Deprecated
 	public final E visitFinallyClause(FinallyClause node) {
 		return null;
 	}
 
 	public abstract E visitForStatement(ForStatement node);
 
 	public abstract E visitForInStatement(ForInStatement node);
 
 	public abstract E visitForEachInStatement(ForEachInStatement node);
 
 	public abstract E visitFunctionStatement(FunctionStatement node);
 
 	@Deprecated
 	public final E visitArgument(Argument argument) {
 		return null;
 	}
 
 	public abstract E visitGetArrayItemExpression(GetArrayItemExpression node);
 
 	@Deprecated
 	public final E visitGetMethod(GetMethod node) {
 		return null;
 	}
 
 	public abstract E visitIdentifier(Identifier node);
 
 	public abstract E visitSimpleType(SimpleType node);
 
 	public abstract E visitIfStatement(IfStatement node);
 
 	@Deprecated
 	public final E visitKeyword(Keyword node) {
 		return null;
 	}
 
 	public abstract E visitLabelledStatement(LabelledStatement node);
 
 	public abstract E visitNewExpression(NewExpression node);
 
 	public abstract E visitNullExpression(NullExpression node);
 
 	public abstract E visitObjectInitializer(ObjectInitializer node);
 
 	public abstract E visitParenthesizedExpression(ParenthesizedExpression node);
 
 	public abstract E visitPropertyExpression(PropertyExpression node);
 
 	@Deprecated
 	public final E visitPropertyInitializer(PropertyInitializer node) {
 		return null;
 	}
 
 	public abstract E visitRegExpLiteral(RegExpLiteral node);
 
 	public abstract E visitReturnStatement(ReturnStatement node);
 
 	public abstract E visitScript(Script node);
 
 	@Deprecated
 	public final E visitSetMethod(SetMethod node) {
 		return null;
 	}
 
 	public abstract E visitStatementBlock(StatementBlock node);
 
 	public abstract E visitStringLiteral(StringLiteral node);
 
 	public abstract E visitSwitchStatement(SwitchStatement node);
 
 	public abstract E visitThisExpression(ThisExpression node);
 
 	public abstract E visitThrowStatement(ThrowStatement node);
 
 	public abstract E visitTryStatement(TryStatement node);
 
 	public abstract E visitTypeOfExpression(TypeOfExpression node);
 
 	public abstract E visitUnaryOperation(UnaryOperation node);
 
	public abstract E visitVariableStatement(VariableStatement node);
 
 	@Deprecated
 	public final E visitVariableDeclaration(VariableDeclaration node) {
 		return null;
 	}
 
 	public abstract E visitVoidExpression(VoidExpression node);
 
 	public abstract E visitVoidOperator(VoidOperator node);
 
 	public abstract E visitYieldOperator(YieldOperator node);
 
 	public abstract E visitWhileStatement(WhileStatement node);
 
 	public abstract E visitWithStatement(WithStatement node);
 
 	public abstract E visitXmlLiteral(XmlLiteral node);
 
 	public abstract E visitDefaultXmlNamespace(DefaultXmlNamespaceStatement node);
 
 	public abstract E visitXmlPropertyIdentifier(XmlAttributeIdentifier node);
 
 	public abstract E visitAsteriskExpression(AsteriskExpression node);
 
 	public abstract E visitGetAllChildrenExpression(
 			GetAllChildrenExpression node);
 
 	public abstract E visitGetLocalNameExpression(GetLocalNameExpression node);
 
 	public E visitErrorExpression(ErrorExpression node) {
 		return null;
 	}
 
 	public abstract E visitEmptyStatement(EmptyStatement node);
 
 	/**
 	 * @since 2.0
 	 */
 	public E visitUnknownNode(ASTNode node) {
 		throw new UnsupportedOperationException("Unknown node type: "
 				+ node.getClass().getName());
 	}
 
 }
