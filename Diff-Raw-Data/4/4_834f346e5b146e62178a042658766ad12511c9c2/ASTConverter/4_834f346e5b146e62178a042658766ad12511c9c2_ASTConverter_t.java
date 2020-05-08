 package org.eclipse.dltk.javascript.core.dom.rewrite;
 
 import java.util.Iterator;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.javascript.ast.ASTVisitor;
 import org.eclipse.dltk.javascript.ast.Argument;
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
 import org.eclipse.dltk.javascript.ast.ConstStatement;
 import org.eclipse.dltk.javascript.ast.ContinueStatement;
 import org.eclipse.dltk.javascript.ast.DecimalLiteral;
 import org.eclipse.dltk.javascript.ast.DefaultXmlNamespaceStatement;
 import org.eclipse.dltk.javascript.ast.DeleteStatement;
 import org.eclipse.dltk.javascript.ast.DoWhileStatement;
 import org.eclipse.dltk.javascript.ast.EmptyExpression;
 import org.eclipse.dltk.javascript.ast.EmptyStatement;
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
 import org.eclipse.dltk.javascript.ast.Label;
 import org.eclipse.dltk.javascript.ast.LabelledStatement;
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
 import org.eclipse.dltk.javascript.ast.VariableStatement;
 import org.eclipse.dltk.javascript.ast.VoidExpression;
 import org.eclipse.dltk.javascript.ast.VoidOperator;
 import org.eclipse.dltk.javascript.ast.WhileStatement;
 import org.eclipse.dltk.javascript.ast.WithStatement;
 import org.eclipse.dltk.javascript.ast.XmlAttributeIdentifier;
 import org.eclipse.dltk.javascript.ast.XmlExpressionFragment;
 import org.eclipse.dltk.javascript.ast.XmlFragment;
 import org.eclipse.dltk.javascript.ast.XmlLiteral;
 import org.eclipse.dltk.javascript.ast.XmlTextFragment;
 import org.eclipse.dltk.javascript.ast.YieldOperator;
 import org.eclipse.dltk.javascript.core.dom.ArrayAccessExpression;
 import org.eclipse.dltk.javascript.core.dom.ArrayLiteral;
 import org.eclipse.dltk.javascript.core.dom.AttributeIdentifier;
 import org.eclipse.dltk.javascript.core.dom.BinaryExpression;
 import org.eclipse.dltk.javascript.core.dom.BinaryOperator;
 import org.eclipse.dltk.javascript.core.dom.BlockStatement;
 import org.eclipse.dltk.javascript.core.dom.ConditionalExpression;
 import org.eclipse.dltk.javascript.core.dom.DescendantAccessExpression;
 import org.eclipse.dltk.javascript.core.dom.DoStatement;
 import org.eclipse.dltk.javascript.core.dom.DomFactory;
 import org.eclipse.dltk.javascript.core.dom.Expression;
 import org.eclipse.dltk.javascript.core.dom.ExpressionStatement;
 import org.eclipse.dltk.javascript.core.dom.FunctionExpression;
 import org.eclipse.dltk.javascript.core.dom.GetterAssignment;
 import org.eclipse.dltk.javascript.core.dom.IArrayElement;
 import org.eclipse.dltk.javascript.core.dom.IForInitializer;
 import org.eclipse.dltk.javascript.core.dom.IProperty;
 import org.eclipse.dltk.javascript.core.dom.IPropertyName;
 import org.eclipse.dltk.javascript.core.dom.IPropertySelector;
 import org.eclipse.dltk.javascript.core.dom.ISelector;
 import org.eclipse.dltk.javascript.core.dom.IUnqualifiedSelector;
 import org.eclipse.dltk.javascript.core.dom.LabeledStatement;
 import org.eclipse.dltk.javascript.core.dom.Node;
 import org.eclipse.dltk.javascript.core.dom.NumericLiteral;
 import org.eclipse.dltk.javascript.core.dom.ObjectLiteral;
 import org.eclipse.dltk.javascript.core.dom.Parameter;
 import org.eclipse.dltk.javascript.core.dom.PropertyAccessExpression;
 import org.eclipse.dltk.javascript.core.dom.PropertyAssignment;
 import org.eclipse.dltk.javascript.core.dom.QualifiedIdentifier;
 import org.eclipse.dltk.javascript.core.dom.RegularExpressionLiteral;
 import org.eclipse.dltk.javascript.core.dom.SetterAssignment;
 import org.eclipse.dltk.javascript.core.dom.SimplePropertyAssignment;
 import org.eclipse.dltk.javascript.core.dom.Source;
 import org.eclipse.dltk.javascript.core.dom.Statement;
 import org.eclipse.dltk.javascript.core.dom.SwitchElement;
 import org.eclipse.dltk.javascript.core.dom.Type;
 import org.eclipse.dltk.javascript.core.dom.UnaryExpression;
 import org.eclipse.dltk.javascript.core.dom.UnaryOperator;
 import org.eclipse.dltk.javascript.core.dom.VariableReference;
 import org.eclipse.dltk.javascript.core.dom.XmlInitializer;
 import org.eclipse.dltk.javascript.parser.JSParser;
 
 public class ASTConverter extends ASTVisitor<Node> {
 	private static final DomFactory DOM_FACTORY = DomFactory.eINSTANCE;
 
 	@Override
 	public Node visit(ASTNode node) {
 		if (node == null)
 			return null;
 		Node res = super.visit(node);
 		if (res.getBegin() == -1)
 			res.setBegin(node.sourceStart());
 		res.setEnd(node.sourceEnd());
 		return res;
 	}
 
 	public static Node convert(ASTNode node) {
 		ASTConverter converter = new ASTConverter();
 		return converter.visit(node);
 	}
 	
 	private org.eclipse.dltk.javascript.core.dom.Label visitLabel(Label label) {
 		if (label == null)
 			return null;
 		org.eclipse.dltk.javascript.core.dom.Label res = DOM_FACTORY
 				.createLabel();
 		res.setBegin(label.sourceStart());
 		res.setEnd(label.sourceEnd());
 		res.setName(label.getText());
 		return res;
 	}
 
 	@Override
 	public Node visitArrayInitializer(ArrayInitializer node) {
 		ArrayLiteral res = DOM_FACTORY.createArrayLiteral();
 		for (ASTNode item : node.getItems())
 			res.getElements().add((IArrayElement) visit(item));
 		return res;
 	}
 
 	@Override
 	public Node visitBinaryOperation(BinaryOperation node) {
 		BinaryExpression res = DOM_FACTORY.createBinaryExpression();
 		BinaryOperator r = null;
 		switch (node.getOperation()) {
 		case JSParser.ADD:
 			r = BinaryOperator.ADD;
 			break;
 		case JSParser.ADDASS:
 			r = BinaryOperator.ADD_ASSIGN;
 			break;
 		case JSParser.ANDASS:
 			r = BinaryOperator.AND_ASSIGN;
 			break;
 		case JSParser.ASSIGN:
 			r = BinaryOperator.ASSIGN;
 			break;
 		case JSParser.AND:
 			r = BinaryOperator.BW_AND;
 			break;
 		case JSParser.COMMA:
 			r = BinaryOperator.COMMA;
 			break;
 		case JSParser.OR:
 			r = BinaryOperator.BW_OR;
 			break;
 		case JSParser.XOR:
 			r = BinaryOperator.BW_XOR;
 			break;
 		case JSParser.DIV:
 			r = BinaryOperator.DIV;
 			break;
 		case JSParser.DIVASS:
 			r = BinaryOperator.DIV_ASSIGN;
 			break;
 		case JSParser.EQ:
 			r = BinaryOperator.EQ;
 			break;
 		case JSParser.GTE:
 			r = BinaryOperator.GEQ;
 			break;
 		case JSParser.GT:
 			r = BinaryOperator.GREATER;
 			break;
 		case JSParser.IN:
 			r = BinaryOperator.IN;
 			break;
 		case JSParser.INSTANCEOF:
 			r = BinaryOperator.INSTANCEOF;
 			break;
 		case JSParser.LTE:
 			r = BinaryOperator.LEQ;
 			break;
 		case JSParser.LT:
 			r = BinaryOperator.LESS;
 			break;
 		case JSParser.LAND:
 			r = BinaryOperator.LOG_AND;
 			break;
 		case JSParser.LOR:
 			r = BinaryOperator.LOG_OR;
 			break;
 		case JSParser.SHL:
 			r = BinaryOperator.LSH;
 			break;
 		case JSParser.SHLASS:
 			r = BinaryOperator.LSH_ASSIGN;
 			break;
 		case JSParser.MOD:
 			r = BinaryOperator.MOD;
 			break;
 		case JSParser.MODASS:
 			r = BinaryOperator.MOD_ASSIGN;
 			break;
 		case JSParser.MUL:
 			r = BinaryOperator.MUL;
 			break;
 		case JSParser.MULASS:
 			r = BinaryOperator.MUL_ASSIGN;
 			break;
 		case JSParser.NEQ:
 			r = BinaryOperator.NEQ;
 			break;
 		case JSParser.NSAME:
 			r = BinaryOperator.NSAME;
 			break;
 		case JSParser.ORASS:
 			r = BinaryOperator.OR_ASSIGN;
 			break;
 		case JSParser.SHR:
 			r = BinaryOperator.RSH;
 			break;
 		case JSParser.SHRASS:
 			r = BinaryOperator.RSH_ASSIGN;
 			break;
 		case JSParser.SAME:
 			r = BinaryOperator.SAME;
 			break;
 		case JSParser.SUB:
 			r = BinaryOperator.SUB;
 			break;
 		case JSParser.SUBASS:
 			r = BinaryOperator.SUB_ASSIGN;
 			break;
 		case JSParser.SHU:
 			r = BinaryOperator.URSH;
 			break;
 		case JSParser.SHUASS:
 			r = BinaryOperator.URSH_ASSIGN;
 			break;
 		case JSParser.XORASS:
 			r = BinaryOperator.XOR_ASSIGN;
 			break;
 		default:
 			throw new IllegalStateException("Unknown binary operator");
 		}
 		res.setOperation(r);
 		res.setOperatorPosition(node.getOperationPosition());
 		res.setLeft((Expression) visit(node.getLeftExpression()));
 		res.setRight((Expression) visit(node.getRightExpression()));
 		return res;
 	}
 
 	@Override
 	public Node visitBooleanLiteral(BooleanLiteral node) {
 		org.eclipse.dltk.javascript.core.dom.BooleanLiteral res = DOM_FACTORY
 				.createBooleanLiteral();
 		res.setText(node.getText());
 		return res;
 	}
 
 	@Override
 	public Node visitBreakStatement(BreakStatement node) {
 		org.eclipse.dltk.javascript.core.dom.BreakStatement res = DOM_FACTORY
 				.createBreakStatement();
 		res.setLabel(visitLabel(node.getLabel()));
 		return res;
 	}
 
 	@Override
 	public Node visitCallExpression(CallExpression node) {
 		org.eclipse.dltk.javascript.core.dom.CallExpression res = DOM_FACTORY
 				.createCallExpression();
 		res.setApplicant((Expression) visit(node.getExpression()));
 		for (ASTNode arg : node.getArguments())
 			res.getArguments().add((Expression) visit(arg));
 		return res;
 	}
 
 	@Override
 	public Node visitCommaExpression(CommaExpression node) {
 		Iterator<ASTNode> it = node.getItems().iterator();
 		Expression res = (Expression) visit(it.next());
 		while (it.hasNext()) {
 			BinaryExpression tmp = DOM_FACTORY.createBinaryExpression();
 			;
 			tmp.setOperation(BinaryOperator.COMMA);
 			tmp.setLeft(res);
 			tmp.setRight((Expression) visit(it.next()));
 			if (it.hasNext()) {
 				tmp.setBegin(res.getBegin());
 				tmp.setEnd(tmp.getRight().getEnd());
 			}
 			res = tmp;
 		}
 		return res;
 	}
 
 	@Override
 	public Node visitConditionalOperator(ConditionalOperator node) {
 		ConditionalExpression res = DOM_FACTORY.createConditionalExpression();
 		res.setPredicate((Expression) visit(node.getCondition()));
 		res.setConsequent((Expression) visit(node.getTrueValue()));
 		res.setAlternative((Expression) visit(node.getFalseValue()));
 		return res;
 	}
 
 	@Override
 	public Node visitConstDeclaration(ConstStatement node) {
 		org.eclipse.dltk.javascript.core.dom.ConstStatement res = DOM_FACTORY
 				.createConstStatement();
 		for (VariableDeclaration decl : node.getVariables())
 			res.getDeclarations().add(createVariableDeclaration(decl));
 		return res;
 	}
 
 	@Override
 	public Node visitContinueStatement(ContinueStatement node) {
 		org.eclipse.dltk.javascript.core.dom.ContinueStatement res = DOM_FACTORY
 				.createContinueStatement();
 		res.setLabel(visitLabel(node.getLabel()));
 		return res;
 	}
 
 	@Override
 	public Node visitDecimalLiteral(DecimalLiteral node) {
 		NumericLiteral res = DOM_FACTORY.createNumericLiteral();
 		res.setText(node.getText());
 		return res;
 	}
 
 	@Override
 	public Node visitDeleteStatement(DeleteStatement node) {
 		UnaryExpression expr = DOM_FACTORY.createUnaryExpression();
 		expr.setBegin(node.sourceStart());
 		expr.setEnd(node.sourceEnd());
 		expr.setOperation(UnaryOperator.DELETE);
 		expr.setArgument((Expression) visit(node.getExpression()));
 		return expr;
 	}
 
 	@Override
 	public Node visitDoWhileStatement(DoWhileStatement node) {
 		DoStatement res = DOM_FACTORY.createDoStatement();
 		res.setBody((Statement) visit(node.getBody()));
 		res.setCondition((Expression) visit(node.getCondition()));
 		return res;
 	}
 
 	@Override
 	public Node visitEmptyExpression(EmptyExpression node) {
 		return DOM_FACTORY.createElision();
 	}
 
 	@Override
 	public Node visitForStatement(ForStatement node) {
 		org.eclipse.dltk.javascript.core.dom.ForStatement res = DOM_FACTORY
 				.createForStatement();
 		if (!isEmpty(node.getInitial())) {
 			res.setInitialization((IForInitializer) visit(node.getInitial()));
 		}
 		if (!isEmpty(node.getCondition())) {
 			res.setCondition((Expression) visit(node.getCondition()));
 		}
 		if (!isEmpty(node.getStep())) {
 			res.setIncrement((Expression) visit(node.getStep()));
 		}
 		res.setBody((Statement) visit(node.getBody()));
 		return res;
 	}
 
 	private boolean isEmpty(
 			org.eclipse.dltk.javascript.ast.Expression expression) {
 		return expression == null || expression instanceof EmptyExpression;
 	}
 
 	@Override
 	public Node visitForInStatement(ForInStatement node) {
 		org.eclipse.dltk.javascript.core.dom.ForInStatement res = DOM_FACTORY
 				.createForInStatement();
 		res.setItem((IForInitializer) visit(node.getItem()));
 		res.setCollection((Expression) visit(node.getIterator()));
 		res.setBody((Statement) visit(node.getBody()));
 		return res;
 	}
 
 	@Override
 	public Node visitForEachInStatement(ForEachInStatement node) {
 		org.eclipse.dltk.javascript.core.dom.ForEachInStatement res = DOM_FACTORY
 				.createForEachInStatement();
		res.setItem((IForInitializer) visit(node.getItem()));
		res.setCollection((Expression) visit(node.getIterator()));
 		res.setBody((Statement) visit(node.getBody()));
 		return res;
 	}
 
 	private static org.eclipse.dltk.javascript.core.dom.Identifier createIdentifier(
 			Identifier id) {
 		org.eclipse.dltk.javascript.core.dom.Identifier res = DOM_FACTORY
 				.createIdentifier();
 		res.setName(id.getName());
 		res.setBegin(id.sourceStart());
 		res.setEnd(id.sourceEnd());
 		return res;
 	}
 
 	@Override
 	public Node visitFunctionStatement(FunctionStatement node) {
 		FunctionExpression res = DOM_FACTORY.createFunctionExpression();
 		if (node.getName() != null)
 			res.setIdentifier(createIdentifier(node.getName()));
 		Comment docs = node.getDocumentation();
 		if (docs != null) {
 			org.eclipse.dltk.javascript.core.dom.Comment comment = DOM_FACTORY.createComment();
 			comment.setText(docs.getText());
 			comment.setBegin(docs.sourceStart());
 			comment.setEnd(docs.sourceEnd());
 			res.setDocumentation(comment);
 			res.setBegin(comment.getBegin());
 		}
 		for (Argument arg : node.getArguments()) {
 			Parameter prm = DOM_FACTORY.createParameter();
 			prm.setName(createIdentifier(arg.getIdentifier()));
 			prm.setType((Type) visit(arg.getType()));
 			prm.setBegin(arg.sourceStart());
 			prm.setEnd(arg.sourceEnd());
 			res.getParameters().add(prm);
 		}
 		res.setReturnType((Type) visit(node.getReturnType()));
 		res.setBody((org.eclipse.dltk.javascript.core.dom.BlockStatement) visit(node
 				.getBody()));
 		res.setParametersPosition(node.getLP() + 1);
 		return res;
 	}
 
 	@Override
 	public Node visitGetArrayItemExpression(GetArrayItemExpression node) {
 		ArrayAccessExpression res = DOM_FACTORY.createArrayAccessExpression();
 		res.setArray((Expression) visit(node.getArray()));
 		res.setIndex((Expression) visit(node.getIndex()));
 		return res;
 	}
 
 	@Override
 	public Node visitIdentifier(Identifier node) {
 		VariableReference res = DOM_FACTORY.createVariableReference();
 		res.setVariable(createIdentifier(node));
 		return res;
 	}
 
 	@Override
 	public Node visitSimpleType(SimpleType node) {
 		Type res = DOM_FACTORY.createType();
 		res.setName(node.getName());
 		return res;
 	}
 
 	@Override
 	public Node visitIfStatement(IfStatement node) {
 		org.eclipse.dltk.javascript.core.dom.IfStatement res = DOM_FACTORY
 				.createIfStatement();
 		res.setPredicate((Expression) visit(node.getCondition()));
 		res.setConsequent((Statement) visit(node.getThenStatement()));
 		res.setAlternative((Statement) visit(node.getElseStatement()));
 		return res;
 	}
 
 	@Override
 	public Node visitLabelledStatement(LabelledStatement node) {
 		LabeledStatement res = DOM_FACTORY.createLabeledStatement();
 		res.setLabel(visitLabel(node.getLabel()));
 		res.setStatement((Statement) visit(node.getStatement()));
 		return res;
 	}
 
 	@Override
 	public Node visitNewExpression(NewExpression node) {
 		org.eclipse.dltk.javascript.core.dom.NewExpression res = DOM_FACTORY
 				.createNewExpression();
 		res.setConstructor((Expression) visit(node.getObjectClass()));
 		return res;
 	}
 
 	@Override
 	public Node visitNullExpression(NullExpression node) {
 		return DOM_FACTORY.createNullLiteral();
 	}
 
 	@Override
 	public Node visitObjectInitializer(ObjectInitializer node) {
 		ObjectLiteral res = DOM_FACTORY.createObjectLiteral();
 		for (ObjectInitializerPart part : node.getInitializers()) {
 			PropertyAssignment cur = null;
 			if (part instanceof PropertyInitializer) {
 				PropertyInitializer pi = (PropertyInitializer) part;
 				SimplePropertyAssignment elem = DOM_FACTORY
 						.createSimplePropertyAssignment();
 				elem.setName(createPropertyName(pi.getName()));
 				elem.setInitializer((Expression) visit(pi.getValue()));
 				cur = elem;
 			} else if (part instanceof GetMethod) {
 				GetMethod gm = (GetMethod) part;
 				GetterAssignment elem = DOM_FACTORY.createGetterAssignment();
 				elem.setName(createPropertyName(gm.getName()));
 				elem.setBody((BlockStatement) visit(gm.getBody()));
 				cur = elem;
 				res.getProperties().add(elem);
 			} else if (part instanceof SetMethod) {
 				SetMethod sm = (SetMethod) part;
 				SetterAssignment elem = DOM_FACTORY.createSetterAssignment();
 				elem.setName(createPropertyName(sm.getName()));
 				elem.setParameter(createIdentifier(sm.getArgument()));
 				elem.setBody((BlockStatement) visit(sm.getBody()));
 				cur = elem;
 				res.getProperties().add(elem);
 			} else
 				throw new UnsupportedOperationException(
 						"Unknown initializer type");
 			cur.setBegin(part.sourceStart());
 			cur.setEnd(part.sourceEnd());
 			res.getProperties().add(cur);
 		}
 		return res;
 	}
 
 	private IPropertyName createPropertyName(
 			org.eclipse.dltk.javascript.ast.Expression name) {
 		if (name instanceof Identifier)
 			return createIdentifier((Identifier) name);
 		return (IPropertyName) visit(name);
 	}
 
 	@Override
 	public Node visitParenthesizedExpression(ParenthesizedExpression node) {
 		org.eclipse.dltk.javascript.core.dom.ParenthesizedExpression res = DOM_FACTORY
 				.createParenthesizedExpression();
 		res.setEnclosed((Expression) visit(node.getExpression()));
 		return res;
 	}
 
 	@Override
 	public Node visitPropertyExpression(PropertyExpression node) {
 		PropertyAccessExpression res = DOM_FACTORY
 				.createPropertyAccessExpression();
 		res.setObject((Expression) visit(node.getObject()));
 		if (node.getProperty() instanceof Identifier)
 			res.setProperty(createIdentifier((Identifier) node.getProperty()));
 		else
 			res.setProperty((IProperty) visit(node.getProperty()));
 		return res;
 	}
 
 	@Override
 	public Node visitRegExpLiteral(RegExpLiteral node) {
 		RegularExpressionLiteral res = DOM_FACTORY
 				.createRegularExpressionLiteral();
 		res.setText(node.getText());
 		return res;
 	}
 
 	@Override
 	public Node visitReturnStatement(ReturnStatement node) {
 		org.eclipse.dltk.javascript.core.dom.ReturnStatement res = DOM_FACTORY
 				.createReturnStatement();
 		res.setExpression((Expression) visit(node.getValue()));
 		return res;
 	}
 
 	@Override
 	public Node visitScript(Script node) {
 		Source res = DOM_FACTORY.createSource();
 		for (org.eclipse.dltk.javascript.ast.Statement stmt : node
 				.getStatements())
 			res.getStatements().add((Statement) visit(stmt));
 		return res;
 	}
 
 	@Override
 	public Node visitStatementBlock(StatementBlock node) {
 		BlockStatement res = DOM_FACTORY.createBlockStatement();
 		for (org.eclipse.dltk.javascript.ast.Statement stmt : node
 				.getStatements())
 			res.getStatements().add((Statement) visit(stmt));
 		return res;
 	}
 
 	@Override
 	public Node visitStringLiteral(StringLiteral node) {
 		org.eclipse.dltk.javascript.core.dom.StringLiteral res = DOM_FACTORY
 				.createStringLiteral();
 		res.setText(node.getText());
 		return res;
 	}
 
 	@Override
 	public Node visitSwitchStatement(SwitchStatement node) {
 		org.eclipse.dltk.javascript.core.dom.SwitchStatement res = DOM_FACTORY
 				.createSwitchStatement();
 		res.setSelector((Expression) visit(node.getCondition()));
 		for (SwitchComponent elem : node.getCaseClauses()) {
 			final SwitchElement element;
 			if (elem instanceof CaseClause) {
 				final org.eclipse.dltk.javascript.core.dom.CaseClause caseClause = DOM_FACTORY
 						.createCaseClause();
 				caseClause.setExpression((Expression) visit(((CaseClause) elem)
 						.getCondition()));
 				element = caseClause;
 			} else {
 				element = DOM_FACTORY.createDefaultClause();
 			}
 			element.setBegin(elem.sourceStart());
 			element.setEnd(elem.sourceEnd());
 			for (org.eclipse.dltk.javascript.ast.Statement statement : elem
 					.getStatements()) {
 				element.getStatements().add((Statement) visit(statement));
 			}
 			res.getElements().add(element);
 		}
 		return res;
 	}
 
 	@Override
 	public Node visitThisExpression(ThisExpression node) {
 		return DOM_FACTORY.createThisExpression();
 	}
 
 	@Override
 	public Node visitThrowStatement(ThrowStatement node) {
 		org.eclipse.dltk.javascript.core.dom.ThrowStatement res = DOM_FACTORY
 				.createThrowStatement();
 		res.setException((Expression) visit(node.getException()));
 		return res;
 	}
 
 	@Override
 	public Node visitTryStatement(TryStatement node) {
 		org.eclipse.dltk.javascript.core.dom.TryStatement res = DOM_FACTORY
 				.createTryStatement();
 		for (CatchClause cc : node.getCatches()) {
 			org.eclipse.dltk.javascript.core.dom.CatchClause ccr = DOM_FACTORY
 					.createCatchClause();
 			ccr.setException(createIdentifier(cc.getException()));
 			ccr.setFilter((Expression) visit(cc.getFilterExpression()));
 			ccr.setBody((BlockStatement) visit(cc.getStatement()));
 			ccr.setBegin(cc.sourceStart());
 			ccr.setEnd(cc.sourceEnd());
 			res.getCatches().add(ccr);
 		}
 		if (node.getFinally() != null) {
 			FinallyClause fc = node.getFinally();
 			org.eclipse.dltk.javascript.core.dom.FinallyClause fcr = DOM_FACTORY
 					.createFinallyClause();
 			fcr.setBody((BlockStatement) visit(fc.getStatement()));
 			fcr.setBegin(fc.sourceStart());
 			fcr.setEnd(fc.sourceEnd());
 			res.setFinallyClause(fcr);
 		}
 		return res;
 	}
 
 	@Override
 	public Node visitTypeOfExpression(TypeOfExpression node) {
 		UnaryExpression res = DOM_FACTORY.createUnaryExpression();
 		res.setOperation(UnaryOperator.TYPEOF);
 		res.setArgument((Expression) visit(node.getExpression()));
 		return res;
 	}
 
 	@Override
 	public Node visitUnaryOperation(UnaryOperation node) {
 		UnaryExpression res = DOM_FACTORY.createUnaryExpression();
 		UnaryOperator r = null;
 		switch (node.getOperation()) {
 		case JSParser.INV:
 			r = UnaryOperator.BW_NOT;
 			break;
 		case JSParser.DELETE:
 			r = UnaryOperator.DELETE;
 			break;
 		case JSParser.NOT:
 			r = UnaryOperator.NOT;
 			break;
 		case JSParser.NEG:
 		case JSParser.SUB:
 			r = UnaryOperator.NUM_NEG;
 			break;
 		case JSParser.PDEC:
 			r = UnaryOperator.POSTFIX_DEC;
 			break;
 		case JSParser.PINC:
 			r = UnaryOperator.POSTFIX_INC;
 			break;
 		case JSParser.DEC:
 			r = UnaryOperator.PREFIX_DEC;
 			break;
 		case JSParser.INC:
 			r = UnaryOperator.PREFIX_INC;
 			break;
 		case JSParser.TYPEOF:
 			r = UnaryOperator.TYPEOF;
 			break;
 		case JSParser.POS:
 		case JSParser.ADD:
 			r = UnaryOperator.UNARY_PLUS;
 			break;
 		case JSParser.VOID:
 			r = UnaryOperator.VOID;
 			break;
 		default:
 			throw new IllegalStateException("Unknown binary operator");
 		}
 		res.setOperation(r);
 		res.setArgument((Expression) visit(node.getExpression()));
 		return res;
 	}
 
 	@Override
 	public Node visitVariableStatement(VariableStatement node) {
 		org.eclipse.dltk.javascript.core.dom.VariableStatement res = DOM_FACTORY
 				.createVariableStatement();
 		for (VariableDeclaration decl : node.getVariables())
 			res.getDeclarations().add(createVariableDeclaration(decl));
 		return res;
 	}
 
 	private org.eclipse.dltk.javascript.core.dom.VariableDeclaration createVariableDeclaration(
 			VariableDeclaration decl) {
 		org.eclipse.dltk.javascript.core.dom.VariableDeclaration res = DOM_FACTORY
 				.createVariableDeclaration();
 		res.setIdentifier(createIdentifier(decl.getIdentifier()));
 		res.setType((Type) visit(decl.getType()));
 		res.setInitializer((Expression) visit(decl.getInitializer()));
 		res.setBegin(decl.sourceStart());
 		res.setEnd(decl.sourceEnd());
 		return res;
 	}
 
 	@Override
 	public Node visitVoidExpression(VoidExpression node) {
 		if (node.getExpression() instanceof VariableStatement)
 			return visit(node.getExpression());
 		ExpressionStatement res = DOM_FACTORY.createExpressionStatement();
 		res.setExpression((Expression) visit(node.getExpression()));
 		res.setBegin(res.getExpression().getBegin());
 		return res;
 	}
 
 	@Override
 	public Node visitVoidOperator(VoidOperator node) {
 		UnaryExpression res = DOM_FACTORY.createUnaryExpression();
 		res.setOperation(UnaryOperator.VOID);
 		res.setArgument((Expression) visit(node.getExpression()));
 		return res;
 	}
 
 	@Override
 	public Node visitYieldOperator(YieldOperator node) {
 		UnaryExpression res = DOM_FACTORY.createUnaryExpression();
 		res.setOperation(UnaryOperator.YIELD);
 		res.setArgument((Expression) visit(node.getExpression()));
 		return res;
 	}
 
 	@Override
 	public Node visitWhileStatement(WhileStatement node) {
 		org.eclipse.dltk.javascript.core.dom.WhileStatement res = DOM_FACTORY
 				.createWhileStatement();
 		res.setCondition((Expression) visit(node.getCondition()));
 		res.setBody((Statement) visit(node.getBody()));
 		return res;
 	}
 
 	@Override
 	public Node visitWithStatement(WithStatement node) {
 		org.eclipse.dltk.javascript.core.dom.WithStatement res = DOM_FACTORY
 				.createWithStatement();
 		res.setExpression((Expression) visit(node.getExpression()));
 		res.setStatement((Statement) visit(node.getStatement()));
 		return res;
 	}
 
 	@Override
 	public Node visitXmlLiteral(XmlLiteral node) {
 		XmlInitializer res = DOM_FACTORY.createXmlInitializer();
 		for (XmlFragment fragment : node.getFragments()) {
 			final org.eclipse.dltk.javascript.core.dom.XmlFragment cur;
 			if (fragment instanceof XmlTextFragment) {
 				XmlTextFragment text = (XmlTextFragment) fragment;
 				org.eclipse.dltk.javascript.core.dom.XmlTextFragment elem = DOM_FACTORY
 						.createXmlTextFragment();
 				elem.setText(text.getXml());
 				cur = elem;
 			} else {
 				// fragment instanceof XmlExpressionFragment;
 				XmlExpressionFragment expr = (XmlExpressionFragment) fragment;
 				org.eclipse.dltk.javascript.core.dom.XmlExpressionFragment elem = DOM_FACTORY
 						.createXmlExpressionFragment();
 				elem.setExpression((Expression) visit(expr.getExpression()));
 				cur = elem;
 			}
 			cur.setBegin(fragment.sourceStart());
 			cur.setEnd(fragment.sourceEnd());
 			res.getFragments().add(cur);
 		}
 		return res;
 	}
 
 	@Override
 	public Node visitDefaultXmlNamespace(DefaultXmlNamespaceStatement node) {
 		org.eclipse.dltk.javascript.core.dom.DefaultXmlNamespaceStatement res = DOM_FACTORY
 				.createDefaultXmlNamespaceStatement();
 		res.setExpression((Expression) visit(node.getValue()));
 		return res;
 	}
 
 	@Override
 	public Node visitXmlPropertyIdentifier(XmlAttributeIdentifier node) {
 		AttributeIdentifier res = DOM_FACTORY.createAttributeIdentifier();
 		res.setSelector((ISelector) visit(node.getExpression()));
 		return res;
 	}
 
 	@Override
 	public Node visitAsteriskExpression(AsteriskExpression node) {
 		return DOM_FACTORY.createWildcardIdentifier();
 	}
 
 	@Override
 	public Node visitGetAllChildrenExpression(GetAllChildrenExpression node) {
 		DescendantAccessExpression res = DOM_FACTORY
 				.createDescendantAccessExpression();
 		res.setObject((Expression) visit(node.getObject()));
 		res.setProperty((IProperty) visit(node.getProperty()));
 		return res;
 	}
 
 	@Override
 	public Node visitGetLocalNameExpression(GetLocalNameExpression node) {
 		QualifiedIdentifier res = DOM_FACTORY.createQualifiedIdentifier();
 		res.setNamespace((IPropertySelector) visit(node.getNamespace()));
 		res.setMember((IUnqualifiedSelector) visit(node.getLocalName()));
 		return res;
 	}
 
 	@Override
 	public Node visitEmptyStatement(EmptyStatement node) {
 		return DOM_FACTORY.createEmptyStatement();
 	}
 }
