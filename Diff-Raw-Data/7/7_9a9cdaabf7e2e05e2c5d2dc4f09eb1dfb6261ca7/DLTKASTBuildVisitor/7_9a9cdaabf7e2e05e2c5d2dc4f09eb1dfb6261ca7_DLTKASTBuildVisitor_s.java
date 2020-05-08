 package org.eclipse.dltk.ruby.internal.parsers.jruby;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.declarations.Argument;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.Assignment;
 import org.eclipse.dltk.ast.expressions.CallArgumentsList;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.expressions.ExpressionList;
 import org.eclipse.dltk.ast.expressions.NumericLiteral;
 import org.eclipse.dltk.ast.expressions.StringLiteral;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.ast.statements.Block;
 import org.eclipse.dltk.ast.statements.IfStatement;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.ruby.ast.ColonExpression;
 import org.eclipse.dltk.ruby.ast.ConstantDeclaration;
 import org.eclipse.dltk.ruby.ast.OrExpression;
 
 import org.eclipse.dltk.ruby.ast.ReturnStatement;
 import org.eclipse.dltk.ruby.ast.RubyArrayExpression;
 import org.eclipse.dltk.ruby.ast.RubyMethodArgument;
 import org.eclipse.dltk.ruby.ast.RubySingletonClassDeclaration;
 import org.eclipse.dltk.ruby.ast.RubySingletonMethodDeclaration;
 import org.eclipse.dltk.ruby.ast.RubyVariableKind;
 import org.eclipse.dltk.ruby.ast.SelfReference;
 import org.eclipse.dltk.ruby.ast.SymbolReference;
 import org.eclipse.dltk.ruby.core.RubyPlugin;
 import org.eclipse.dltk.ruby.core.utils.RubySyntaxUtils;
 import org.jruby.ast.AliasNode;
 import org.jruby.ast.AndNode;
 import org.jruby.ast.ArgsCatNode;
 import org.jruby.ast.ArgsNode;
 import org.jruby.ast.ArgumentNode;
 import org.jruby.ast.ArrayNode;
 import org.jruby.ast.BackRefNode;
 import org.jruby.ast.BeginNode;
 import org.jruby.ast.BignumNode;
 import org.jruby.ast.BlockArgNode;
 import org.jruby.ast.BlockNode;
 import org.jruby.ast.BlockPassNode;
 import org.jruby.ast.BreakNode;
 import org.jruby.ast.CallNode;
 import org.jruby.ast.CaseNode;
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.ClassVarAsgnNode;
 import org.jruby.ast.ClassVarDeclNode;
 import org.jruby.ast.ClassVarNode;
 import org.jruby.ast.Colon2Node;
 import org.jruby.ast.Colon3Node;
 import org.jruby.ast.ConstDeclNode;
 import org.jruby.ast.ConstNode;
 import org.jruby.ast.DAsgnNode;
 import org.jruby.ast.DRegexpNode;
 import org.jruby.ast.DStrNode;
 import org.jruby.ast.DSymbolNode;
 import org.jruby.ast.DVarNode;
 import org.jruby.ast.DXStrNode;
 import org.jruby.ast.DefinedNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.DefsNode;
 import org.jruby.ast.DotNode;
 import org.jruby.ast.EnsureNode;
 import org.jruby.ast.EvStrNode;
 import org.jruby.ast.FCallNode;
 import org.jruby.ast.FalseNode;
 import org.jruby.ast.FixnumNode;
 import org.jruby.ast.FlipNode;
 import org.jruby.ast.FloatNode;
 import org.jruby.ast.ForNode;
 import org.jruby.ast.GlobalAsgnNode;
 import org.jruby.ast.GlobalVarNode;
 import org.jruby.ast.HashNode;
 import org.jruby.ast.IfNode;
 import org.jruby.ast.InstAsgnNode;
 import org.jruby.ast.InstVarNode;
 import org.jruby.ast.IterNode;
 import org.jruby.ast.ListNode;
 import org.jruby.ast.LocalAsgnNode;
 import org.jruby.ast.LocalVarNode;
 import org.jruby.ast.Match2Node;
 import org.jruby.ast.Match3Node;
 import org.jruby.ast.MatchNode;
 import org.jruby.ast.ModuleNode;
 import org.jruby.ast.MultipleAsgnNode;
 import org.jruby.ast.NewlineNode;
 import org.jruby.ast.NextNode;
 import org.jruby.ast.NilNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.NotNode;
 import org.jruby.ast.NthRefNode;
 import org.jruby.ast.OpAsgnAndNode;
 import org.jruby.ast.OpAsgnNode;
 import org.jruby.ast.OpAsgnOrNode;
 import org.jruby.ast.OpElementAsgnNode;
 import org.jruby.ast.OptNNode;
 import org.jruby.ast.OrNode;
 import org.jruby.ast.PostExeNode;
 import org.jruby.ast.RedoNode;
 import org.jruby.ast.RegexpNode;
 import org.jruby.ast.RescueBodyNode;
 import org.jruby.ast.RescueNode;
 import org.jruby.ast.RetryNode;
 import org.jruby.ast.ReturnNode;
 import org.jruby.ast.SClassNode;
 import org.jruby.ast.SValueNode;
 import org.jruby.ast.ScopeNode;
 import org.jruby.ast.SelfNode;
 import org.jruby.ast.SplatNode;
 import org.jruby.ast.StrNode;
 import org.jruby.ast.SuperNode;
 import org.jruby.ast.SymbolNode;
 import org.jruby.ast.ToAryNode;
 import org.jruby.ast.TrueNode;
 import org.jruby.ast.UndefNode;
 import org.jruby.ast.UntilNode;
 import org.jruby.ast.VAliasNode;
 import org.jruby.ast.VCallNode;
 import org.jruby.ast.WhenNode;
 import org.jruby.ast.WhileNode;
 import org.jruby.ast.XStrNode;
 import org.jruby.ast.YieldNode;
 import org.jruby.ast.ZArrayNode;
 import org.jruby.ast.ZSuperNode;
 import org.jruby.ast.visitor.NodeVisitor;
 import org.jruby.evaluator.Instruction;
 import org.jruby.lexer.yacc.ISourcePosition;
 import org.jruby.lexer.yacc.SourcePosition;
 import org.jruby.runtime.Arity;
 import org.jruby.runtime.Visibility;
 
 public class DLTKASTBuildVisitor implements NodeVisitor {
 
 	private static final boolean VISITOR_COMPLETE = false;
 
 	private static final boolean TRACE_RECOVERING = Boolean
 			.valueOf(
 					Platform
 							.getDebugOption("org.eclipse.dltk.ruby.core/parsing/traceRecoveryWhenInterpretingAST"))
 			.booleanValue();
 
 	private ModuleDeclaration module;
 
 	private ArrayList currentLevel = new ArrayList(5);
 
 	private final CharSequence content;
 
 	private static class State {
 		public void add(Statement statement) {
 		}
 	}
 
 	private static class CollectingState extends State {
 		private final ArrayList list;
 
 		public CollectingState() {
 			list = new ArrayList();
 		}
 
 		public void add(Statement s) {
 			list.add(s);
 		}
 
 		public ArrayList getList() {
 			return list;
 		}
 
 		public void reset() {
 			list.clear();
 		}
 
 	}
 
 	private static class ArgumentsState extends State {
 		private final CallArgumentsList list;
 
 		public ArgumentsState(CallArgumentsList list) {
 			this.list = list;
 		}
 
 		public void add(Statement s) {
 			// XXX FIXME TODO !!! don't differentiate statements and expressions
 			// in DLTK AST !!!
 			if (s instanceof Expression)
 				list.addExpression((Expression) s);
 		}
 
 	}
 
 	private static class TopLevelState extends State {
 		private final ModuleDeclaration module;
 
 		public TopLevelState(ModuleDeclaration module) {
 			this.module = module;
 		}
 
 		public void add(Statement statement) {
 			module.getStatements().add(statement);
 		}
 	}
 
 	private static class ClassLikeState extends State {
 		public int visibility;
 		public final TypeDeclaration type;
 
 		public ClassLikeState(TypeDeclaration type) {
 			this.type = type;
 			visibility = Modifiers.AccPublic;
 		}
 
 		public void add(Statement statement) {
 			type.getStatements().add(statement);
 		}
 
 	}
 
 	private static class ClassState extends ClassLikeState {
 
 		public ClassState(TypeDeclaration type) {
 			super(type);
 		}
 
 	}
 
 	private static class ModuleState extends ClassLikeState {
 
 		public ModuleState(TypeDeclaration type) {
 			super(type);
 		}
 
 	}
 
 	private static class MethodState extends State {
 
 		private final MethodDeclaration method;
 
 		public MethodState(MethodDeclaration method) {
 			this.method = method;
 		}
 
 		public void add(Statement statement) {
 			method.getStatements().add(statement);
 		}
 
 	}
 
 	private static class BlockState extends State {
 
 		public final Block block;
 
 		public BlockState(Block block) {
 			this.block = block;
 		}
 
 		public void add(Statement statement) {
 			block.getStatements().add(statement);
 		}
 
 	}
 
 	public DLTKASTBuildVisitor(ModuleDeclaration module, CharSequence content) {
 		this.module = module;
 		this.content = content;
 		pushState(new TopLevelState(this.module));
 	}
 
 	public Instruction visitAliasNode(AliasNode iVisited) {
 		return null;
 	}
 
 	public Instruction visitAndNode(AndNode iVisited) {
 		iVisited.getFirstNode().accept(this);
 
 		iVisited.getSecondNode().accept(this);
 		return null;
 	}
 
 	public Instruction visitArgsNode(ArgsNode iVisited) {
 //		System.out.println("DLTKASTBuildVisitor.visitArgsNode()");
 		if (iVisited.getOptArgs() != null) {
 			iVisited.getOptArgs().accept(this);
 		}
 		return null;
 	}
 
 	// XXXEnebo - Just guessed.
 	public Instruction visitArgsCatNode(ArgsCatNode iVisited) {
 //		System.out.println("DLTKASTBuildVisitor.visitArgsCatNode()");
 		if (iVisited.getFirstNode() != null) {
 			iVisited.getFirstNode().accept(this);
 		}
 		if (iVisited.getSecondNode() != null) {
 			iVisited.getSecondNode().accept(this);
 		}
 		return null;
 	}
 
 	public Instruction visitArrayNode(ArrayNode iVisited) {
 		CollectingState coll = new CollectingState();
 
 		pushState(coll);
 		Iterator iterator = iVisited.iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		popState();
 
 		ISourcePosition position = iVisited.getPosition();
 		RubyArrayExpression arr = new RubyArrayExpression();
 		arr.setEnd(position.getEndOffset());
 		arr.setStart(position.getStartOffset());
 		for (Iterator iter = coll.getList().iterator(); iter.hasNext();) {
 			Object obj = iter.next();
 			if (obj instanceof Expression) {
 				Expression ex = (Expression) obj;
 				arr.addExpression(ex);
 			}
 
 		}
 		peekState().add(arr);
 
 		return null;
 	}
 
 	public Instruction visitBackRefNode(BackRefNode iVisited) {
 //		System.out.println("DLTKASTBuildVisitor.visitBackRefNode()");
 		return null;
 	}
 
 	public Instruction visitBeginNode(BeginNode iVisited) {
 		// TODO: construct a node (?)
 		iVisited.getBodyNode().accept(this);
 		return null;
 	}
 
 	public Instruction visitBlockArgNode(BlockArgNode iVisited) {
 //		System.out.println("DLTKASTBuildVisitor.visitBlockArgNode()");
 		return null;
 	}
 
 	public Instruction visitBlockNode(BlockNode iVisited) {
 		Iterator iterator = iVisited.iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		return null;
 	}
 
 	public Instruction visitBlockPassNode(BlockPassNode iVisited) {
 //		System.out.println("DLTKASTBuildVisitor.visitBlockPassNode()");
 		Iterator iterator = iVisited.childNodes().iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		return null;
 	}
 
 	public Instruction visitBreakNode(BreakNode iVisited) {
 		return null;
 	}
 
 	public Instruction visitConstDeclNode(ConstDeclNode iVisited) {
 		Node pathNode = iVisited.getPathNode();
 		Expression pathResult = null;
 		if (pathNode != null)
 			pathResult = (Expression) collectSingleStatement(pathNode);
 		Statement value = collectSingleStatement(iVisited.getValueNode());
 		ISourcePosition position = iVisited.getPosition();
 		int start = position.getStartOffset();
 		int end = start;
 		while (RubySyntaxUtils.isWhitespace(content.charAt(end))) {
 			end++;
 		}
 		while (RubySyntaxUtils.isNameChar(content.charAt(end))) {
 			end++;
 		}
 		SimpleReference name = new SimpleReference(start, end, iVisited
 				.getName());
 		ConstantDeclaration node = new ConstantDeclaration(pathResult, name,
 				value);
 		peekState().add(node);
 		return null;
 	}
 
 	private Statement collectSingleStatement(Node pathNode) {
 		if (pathNode == null)
 			return null;
 		CollectingState state = new CollectingState();
 		pushState(state);
 		pathNode.accept(this);
 		popState();
 		ArrayList list = state.getList();
 //		if (list.size() > 1)
 //			System.out.println();
 		if (list.size() > 1) {
 			if (TRACE_RECOVERING)
 				RubyPlugin
 						.log("DLTKASTBuildVisitor.collectSingleStatement(): JRuby node "
 								+ pathNode.getClass().getName()
 								+ " turned into multiple DLTK AST nodes");
 		}
 		if (!list.isEmpty())
 			return (Statement) list.iterator().next();
 		else
 			return null;
 	}
 
 	public Instruction visitClassVarAsgnNode(ClassVarAsgnNode iVisited) {
 		processVariableAssignment(iVisited, iVisited.getName(),
 				RubyVariableKind.CLASS, iVisited.getValueNode());
 		return null;
 	}
 
 	public Instruction visitClassVarDeclNode(ClassVarDeclNode iVisited) {
 		processVariableAssignment(iVisited, iVisited.getName(),
 				RubyVariableKind.CLASS, iVisited.getValueNode());
 		return null;
 	}
 
 	public Instruction visitClassVarNode(ClassVarNode iVisited) {
 		processVariableReference(iVisited, iVisited.getName(),
 				RubyVariableKind.CLASS);
 		return null;
 	}
 
 	private void fixCallOffsets(CallExpression callNode, String nameNode,
 			int possibleDotPosition, int firstArgStart, int lastArgEnd) {
 		int dotPosition = RubySyntaxUtils.skipWhitespaceForward(content,
 				possibleDotPosition);
 		if (dotPosition >= 0 && dotPosition < content.length()
 				&& content.charAt(dotPosition) == '.') {
 			fixFunctionCallOffsets(callNode, nameNode, dotPosition + 1,
 					firstArgStart, lastArgEnd);
 			return;
 		}
 		String methodName = nameNode;
 		if (methodName == RubySyntaxUtils.ARRAY_GET_METHOD) {
 			// TODO
 		} else if (methodName == RubySyntaxUtils.ARRAY_PUT_METHOD) {
 			// TODO
 		} else {
 			// WTF?
 			if (TRACE_RECOVERING)
 				RubyPlugin
 						.log("Ruby AST: non-dot-call not recognized, non-dot found at "
 								+ dotPosition + ", function name " + methodName);
 		}
 	}
 
 	private void fixFunctionCallOffsets(CallExpression callNode,
 			String nameNode, int possibleNameStart, int firstArgStart,
 			int lastArgEnd) {
 		String methodName = nameNode;
 		int nameStart = RubySyntaxUtils.skipWhitespaceForward(content,
 				possibleNameStart);
 		int nameEnd = nameStart + methodName.length();
 		CharSequence nameSequence = content.subSequence(nameStart, nameEnd);
 		// Assert.isLegal(nameSequence.toString().equals(methodName)); //XXX
 		// mega fourdman fix
 		// nameNode.setStart(nameStart);
 		// nameNode.setEnd(nameEnd);
 
 		if (firstArgStart < 0) {
 			int lParenOffset = RubySyntaxUtils.skipWhitespaceForward(content,
 					nameEnd);
 			if (lParenOffset >= 0 && content.charAt(lParenOffset) == '(') {
 				int rParenOffset = RubySyntaxUtils.skipWhitespaceForward(
 						content, lParenOffset + 1);
 				if (rParenOffset >= 0 && content.charAt(rParenOffset) == ')')
 					callNode.setEnd(rParenOffset + 1);
 				else {
 					if (TRACE_RECOVERING)
 						RubyPlugin
 								.log("Ruby AST: function call, empty args, no closing paren; "
 										+ "opening paren at "
 										+ lParenOffset
 										+ ", function name " + methodName);
 					callNode.setEnd(lParenOffset - 1); // don't include these
 														// parens
 				}
 			}
 		} else {
 			if (nameEnd > firstArgStart) {
 				if (TRACE_RECOVERING)
 					RubyPlugin
 							.log("DLTKASTBuildVisitor.fixFunctionCallOffsets("
 									+ methodName + "): nameEnd > firstArgStart");
 				return; // /XXX: it's a kind of magic, please, FIXME!!!
 			}
 			int lParenOffset = RubySyntaxUtils.skipWhitespaceForward(content,
 					nameEnd, firstArgStart);
 			if (lParenOffset >= 0 && content.charAt(lParenOffset) == '(') {
 				if (lastArgEnd <= lParenOffset) {
 					if (TRACE_RECOVERING)
 						RubyPlugin
 								.log("DLTKASTBuildVisitor.fixFunctionCallOffsets("
 										+ methodName
 										+ "): lastArgEnd <= lParenOffset");
 					return;
 				}
 				int rParenOffset = RubySyntaxUtils.skipWhitespaceForward(
 						content, lastArgEnd);
 				if (rParenOffset >= 0 && content.charAt(rParenOffset) == ')')
 					callNode.setEnd(rParenOffset + 1);
 				else {
 					if (TRACE_RECOVERING)
 						RubyPlugin
 								.log("Ruby AST: function call, non-empty args, no closing paren; "
 										+ "opening paren at "
 										+ lParenOffset
 										+ ", "
 										+ "last argument ending at "
 										+ lastArgEnd
 										+ ", function name "
 										+ methodName);
 					callNode.setEnd(lastArgEnd); // probably no closing paren
 				}
 			}
 		}
 
 		if (lastArgEnd >= 0 && callNode.sourceEnd() < lastArgEnd)
 			callNode.setEnd(lastArgEnd);
 	}
 
 	/**
 	 * @fixme iteration not correctly defined
 	 */
 	public Instruction visitCallNode(CallNode iVisited) {
 		String methodName = iVisited.getName();
 		CollectingState collector = new CollectingState();
 
 		Assert.isTrue(iVisited.getReceiverNode() != null);
 		pushState(collector);
 		iVisited.getReceiverNode().accept(this);
 		popState();
 		// TODO: uncomment when visitor is done
 		if (collector.getList().size() > 1) {
 			if (TRACE_RECOVERING)
 				RubyPlugin.log("DLTKASTBuildVisitor.visitCallNode("
 						+ methodName + "): receiver "
 						+ iVisited.getReceiverNode().getClass().getName()
 						+ " turned into multiple nodes");
 		}
 		Statement recv;
 		if (collector.getList().size() < 1) {
 			recv = new NumericLiteral(new DLTKToken(0, "")); // FIXME
 			recv.setStart(iVisited.getPosition().getStartOffset());
 			recv.setEnd(iVisited.getPosition().getEndOffset() + 1);
 		} else
 			recv = (Statement) collector.getList().get(0);
 
 		collector.reset();
 
 		Statement args = null;
 		int argsStart = -1, argsEnd = -1;
 		CallArgumentsList argList = new CallArgumentsList();
 		Node argsNode = iVisited.getArgsNode();
 		if (argsNode != null) {
 			pushState(new ArgumentsState(argList));
 			if (argsNode instanceof ListNode) {
 				ListNode arrayNode = (ListNode) argsNode;
 				List list = arrayNode.childNodes();
 				for (Iterator iter = list.iterator(); iter.hasNext();) {
 					Node node = (Node) iter.next();
 					node.accept(this);
 				}
 			} else {
 				if (TRACE_RECOVERING)
 					RubyPlugin.log("DLTKASTBuildVisitor.visitCallNode("
 							+ methodName + ") - unknown args node type: "
 							+ argsNode.getClass().getName());
 				argsNode.accept(this);
 			}
 			popState();
 			List children = argsNode.childNodes();
 			if (children.size() > 0) {
 				argsStart = ((Node) children.get(0)).getPosition()
 						.getStartOffset();
 				argsEnd = ((Node) children.get(children.size() - 1))
 						.getPosition().getEndOffset() + 1;
 				// correction for nodes with incorrect positions
 				List argListExprs = argList.getExpressions();
 				if (!argListExprs.isEmpty())
 					argsEnd = Math.max(argsEnd, ((ASTNode) argListExprs
 							.get(argListExprs.size() - 1)).sourceEnd());
 			}
 		}
 
 		CallExpression c = new CallExpression(recv, methodName, argList);
 		int receiverStart = recv.sourceStart();
 		int receiverEnd = recv.sourceEnd();
 
 		// FIXME handle whitespace and special functions like []= (which are
 		// called without dot)
 		int funcNameStart = receiverEnd + 1 /* skip dot */;
 		int funcNameEnd = funcNameStart + methodName.length();
 		// Assert.isTrue(iVisited.getPosition().getStartOffset() ==
 		// receiverStart);
 
 		c.setStart(receiverStart);
 		c.setEnd(argsEnd >= 0 ? argsEnd : funcNameEnd); // just in case, should
 														// be overriden
 		fixCallOffsets(c, methodName, receiverEnd, argsStart, argsEnd);
 
 		this.peekState().add(c);
 
 		return null;
 	}
 
 	public Instruction visitCaseNode(CaseNode iVisited) {
 
 		return null;
 	}
 
 	private static String colons2Name(Node cpathNode) {
 		String name = "";
 		while (cpathNode instanceof Colon2Node) {
 			Colon2Node colon2Node = (Colon2Node) cpathNode;
 			if (name.length() > 0)
 				name = "::" + name;
 			name = colon2Node.getName() + name;
 			cpathNode = colon2Node.getLeftNode();
 		}
 		if (cpathNode instanceof Colon3Node) {
 			Colon3Node colon3Node = (Colon3Node) cpathNode;
 			if (name.length() > 0)
 				name = "::" + name;
 			name = "::" + colon3Node.getName() + name;
 		} else if (cpathNode instanceof ConstNode) {
 			ConstNode constNode = (ConstNode) cpathNode;
 			if (name.length() > 0)
 				name = "::" + name;
 			name = constNode.getName() + name;
 		}
 		return name;
 	}
 
 	private ISourcePosition fixNamePosition(ISourcePosition pos) {
 		int start = pos.getStartOffset();
 		int end = pos.getEndOffset();
 
 		while (end - 1 >= 0 && (end - 1) > start
 				&& !RubySyntaxUtils.isNameChar(content.charAt(end - 1))) {
 			end--;
 		}
 		if (end >= 0) {
 			while (end < content.length()
 					&& RubySyntaxUtils.isNameChar(content.charAt(end)))
 				end++;
 		}
 		return new SourcePosition(pos.getFile(), pos.getStartLine(), pos
 				.getEndLine(), start, end);
 	}
 
 	private ISourcePosition fixBorders(ISourcePosition pos) {
 		int start = pos.getStartOffset();
 		int end = pos.getEndOffset();
 		while (end - 1 >= 0
 				&& RubySyntaxUtils.isWhitespace(content.charAt(end - 1))) {
 			end--;
 		}
 		if (end >= 0) {
 			while (end < content.length()
 					&& RubySyntaxUtils.isNameChar(content.charAt(end)))
 				end++;
 		}
 		return new SourcePosition(pos.getFile(), pos.getStartLine(), pos
 				.getEndLine(), start, end);
 	}
 
 	public Instruction visitClassNode(ClassNode iVisited) {
 		String name = "";
 		Node cpathNode = iVisited.getCPath();
 		if (cpathNode instanceof Colon2Node || cpathNode instanceof ConstNode) {
 			name = colons2Name(cpathNode);
 		}
 		ISourcePosition pos = iVisited.getCPath().getPosition();
 		ISourcePosition cPos = iVisited.getPosition();
 		cPos = fixNamePosition(cPos);
 		pos = fixNamePosition(pos);
 		TypeDeclaration type = new TypeDeclaration(name, pos.getStartOffset(),
 				pos.getEndOffset(), cPos.getStartOffset(), cPos.getEndOffset());
 		peekState().add(type);
 		pushState(new ClassState(type));
 		// superclass
 		Node superNode = iVisited.getSuperNode();
 		if (superNode != null) {
 			CollectingState collect = new CollectingState();
 			pushState(collect);
 			superNode.accept(this);
 			popState();
 			if (collect.getList().size() == 1) {
 				Object superclass = collect.getList().get(0);
 				if (superclass instanceof Expression) {
 					type.addSuperClass((Expression) superclass);
 				}
 			}
 		}
 		// body
 		pos = iVisited.getBodyNode().getPosition();
 		if (iVisited.getBodyNode().getBodyNode() != null) {
 			Node bodyNode = iVisited.getBodyNode().getBodyNode();
 			int end = -1;
 			while (bodyNode instanceof NewlineNode)
 				bodyNode = ((NewlineNode) bodyNode).getNextNode();
 			if (bodyNode instanceof BlockNode) {
 				BlockNode blockNode = (BlockNode) bodyNode;
 				end = blockNode.getLast().getPosition().getEndOffset() + 1; // /XXX!!!!
 			} else {
 				if (TRACE_RECOVERING)
 					RubyPlugin.log("DLTKASTBuildVisitor.visitClassNode(" + name
 							+ "): unknown body type "
 							+ bodyNode.getClass().getName());
 			}
 			pos = fixBorders(pos);
 			Block bl = new Block(pos.getStartOffset(), (end == -1) ? pos
 					.getEndOffset() + 1 : end);
 			type.setBody(bl);
 			iVisited.getBodyNode().accept(this);
 		}
 		popState();
 		return null;
 	}
 
 	private State peekState() {
 		return (State) currentLevel.get(currentLevel.size() - 1);
 	}
 
 	private void pushState(State state) {
 		currentLevel.add(state);
 	}
 
 	private void popState() {
 		currentLevel.remove(currentLevel.size() - 1);
 	}
 
 	public Instruction visitColon2Node(Colon2Node iVisited) {
 
 		CollectingState collector = new CollectingState();
 		pushState(collector);
 		iVisited.getLeftNode().accept(this);
 		popState();
 
 		int start = iVisited.getPosition().getStartOffset();
 		int end = iVisited.getPosition().getEndOffset() + 1;
 
 		Expression left = null;
 		if (collector.list.size() == 1) {
 			if (collector.list.get(0) instanceof Expression) {
 				left = (Expression) collector.list.get(0);
 			}
 		}
 
 		String right = iVisited.getName();
 
 		ColonExpression colon = new ColonExpression(right, left, false);
 		colon.setStart(start);
 		colon.setEnd(end);
 		peekState().add(colon);
 
 		return null;
 	}
 
 	public Instruction visitColon3Node(Colon3Node iVisited) {
 		ISourcePosition position = iVisited.getPosition();
 		ColonExpression colon = new ColonExpression(iVisited.getName(), null,
 				true);
 		colon.setStart(position.getStartOffset());
 		colon.setEnd(position.getEndOffset() + 1);
 		peekState().add(colon);
 		return null;
 	}
 
 	public Instruction visitConstNode(ConstNode iVisited) {
 		String name = iVisited.getName();
 		ISourcePosition pos = iVisited.getPosition();
 		pos = fixBorders(pos);
 		this.peekState().add(
 				new ConstantReference(pos.getStartOffset(), pos.getEndOffset(),
 						name));
 		return null;
 	}
 
 	public Instruction visitDAsgnNode(DAsgnNode iVisited) {
 		Iterator iterator = iVisited.childNodes().iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		return null;
 	}
 
 	public Instruction visitDRegxNode(DRegexpNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitDStrNode(DStrNode iVisited) {
 
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitDSymbolNode(DSymbolNode)
 	 */
 	public Instruction visitDSymbolNode(DSymbolNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitDVarNode(DVarNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitDXStrNode(DXStrNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitDefinedNode(DefinedNode iVisited) {
 //		System.out.println("DLTKASTBuildVisitor.visitDefinedNode()");
 		return null;
 	}
 
 	private List processMethodArguments(ArgsNode args) {
 		List arguments = new ArrayList();
 		Arity arity = args.getArity();
 		int endPos = args.getPosition().getStartOffset() - 1;
 		if (arity.required() > 0) {
 			ListNode argsList = args.getArgs();
 			if (argsList != null) {
 				Iterator i = argsList.iterator();
 				while (i.hasNext()) {
 					Node nde = (Node) i.next();
 					if (nde instanceof ArgumentNode) {
 						ArgumentNode a = (ArgumentNode) nde;
 						Argument aa = new RubyMethodArgument();
 						ISourcePosition argPos = fixNamePosition(a
 								.getPosition());
 
 						if (argPos.getEndOffset() > endPos)
 							endPos = argPos.getEndOffset();
 
 						aa.set(new SimpleReference(argPos.getStartOffset(),
 								argPos.getEndOffset(), a.getName()), null);
 						aa.setModifier(RubyMethodArgument.SIMPLE);
 						arguments.add(aa);
 					}
 				}
 			}
 			ListNode optArgs = args.getOptArgs();
 			if (optArgs != null) {
 				Iterator iterator = optArgs.iterator();
 				while (iterator.hasNext()) {
 					Object obj = iterator.next();
 					if (obj instanceof LocalAsgnNode) {
 						LocalAsgnNode a = (LocalAsgnNode) obj;
 						Argument aa = new RubyMethodArgument();
 						ISourcePosition argPos = a.getPosition();
 
 						if (argPos.getEndOffset() > endPos)
 							endPos = argPos.getEndOffset();
 
 						CollectingState coll = new CollectingState();
 						pushState(coll);
 						a.getValueNode().accept(this);
 						popState();
 
 						Expression defaultVal = null;
 
 						if (coll.list.size() == 1) {
 							Object object = coll.list.get(0);
 							if (object instanceof Expression)
 								defaultVal = (Expression) object;
 						}
 
 						aa
 								.set(new SimpleReference(argPos
 										.getStartOffset(), argPos
 										.getEndOffset(), a.getName()),
 										defaultVal);
 						aa.setModifier(RubyMethodArgument.SIMPLE);
 						arguments.add(aa);
 					} else {
 						System.err.println("unknown argument type!");
 					}
 				}
 			}
 		}
 		if (!arity.isFixed()) {
 
 			// restore vararg name and position
 			int vaStart = 0, vaEnd = 0;
 			if (endPos >= 0 && endPos < content.length()) {
 				while (endPos < content.length()
 						&& content.charAt(endPos) != '*')
 					endPos++;
 				endPos++;
 				if (endPos < content.length()) {
 					vaStart = endPos - 1;
 					while (RubySyntaxUtils.isIdentifierCharacter(content
 							.charAt(endPos)))
 						endPos++;
 					vaEnd = endPos;
 				}
 
 			}
 
 			Argument aa = new RubyMethodArgument();
 			aa.set(new SimpleReference(vaStart + 1, vaEnd, content.subSequence(
 					vaStart, vaEnd).toString()), null);
 			aa.setModifier(RubyMethodArgument.VARARG);
 			arguments.add(aa);
 		}
 		BlockArgNode blockArgNode = args.getBlockArgNode();
 		if (blockArgNode != null) {
 			ISourcePosition position = fixNamePosition(blockArgNode
 					.getPosition());
 			String baName = content.subSequence(position.getStartOffset() - 1,
 					position.getEndOffset()).toString();
 			Argument aa = new RubyMethodArgument();
 			aa.set(new SimpleReference(position.getStartOffset(), position
 					.getEndOffset(), baName), null); // XXX:
 			aa.setModifier(RubyMethodArgument.BLOCK);
 			arguments.add(aa);
 		}
 		return arguments;
 	}
 
 	private void setMethodVisibility(MethodDeclaration method,
 			Visibility visibility) {
 		if (visibility.isPrivate())
 			ASTUtils.setVisibility(method, Modifiers.AccPrivate);
 
 		if (visibility.isPublic())
 			ASTUtils.setVisibility(method, Modifiers.AccPublic);
 
 		if (visibility.isProtected())
 			ASTUtils.setVisibility(method, Modifiers.AccProtected);
 	}
 
 	// method
 	public Instruction visitDefnNode(DefnNode iVisited) {
 		ArgumentNode nameNode = iVisited.getNameNode();
 
 		ISourcePosition pos = fixNamePosition(nameNode.getPosition());
 		ISourcePosition cPos = fixNamePosition(iVisited.getPosition());
 		MethodDeclaration method = new MethodDeclaration(iVisited.getName(),
 				pos.getStartOffset(), pos.getEndOffset(),
 				cPos.getStartOffset(), cPos.getEndOffset());
 
 		setMethodVisibility(method, iVisited.getVisibility());
 		if (peekState() instanceof ClassLikeState) {
 			ClassLikeState classState = (ClassLikeState) peekState();
 			ASTUtils.setVisibility(method, classState.visibility);
 		}
 
 		peekState().add(method);
 		pushState(new MethodState(method));
 		ArgsNode args = (ArgsNode) iVisited.getArgsNode();
 		if (args != null) {
 			List arguments = processMethodArguments(args);
 			method.acceptArguments(arguments);
 		}
 		iVisited.getBodyNode().accept(this);
 		popState();
 		return null;
 	}
 
 	private ISourcePosition restoreMethodNamePosition(DefsNode node, int recvEnd) {
 		ISourcePosition recvPos = node.getReceiverNode().getPosition();
 		int pos = recvEnd;
 		if (pos >= 0) {
 			while (pos < content.length() && content.charAt(pos) != '.'
 					&& content.charAt(pos) != ':')
 				pos++;
 			if (content.charAt(pos) == ':')
 				pos++;
 		}
 		if (pos >= content.length() || pos < 0)
 			return recvPos;
 		int nameStart = RubySyntaxUtils.skipWhitespaceForward(content, pos + 1);
 		int nameEnd = nameStart;
 		while (RubySyntaxUtils.isIdentifierCharacter(content.charAt(nameEnd)))
 			nameEnd++;
 		return new SourcePosition(recvPos.getFile(), recvPos.getStartLine(),
 				recvPos.getEndLine(), nameStart, nameEnd);
 	}
 
 	// singleton method
 	public Instruction visitDefsNode(DefsNode iVisited) {
 		Expression receiverExpression = null;
 		Node receiverNode = iVisited.getReceiverNode();
 		CollectingState collectingState = new CollectingState();
 
 		pushState(collectingState);
 		receiverNode.accept(this);
 		popState();
 		if (collectingState.list.size() == 1) {
 			Object obj = collectingState.list.get(0);
 			if (obj instanceof Expression) {
 				receiverExpression = (Expression) obj;
 			}
 		}
 
 		ISourcePosition cPos = iVisited.getPosition();
 //		if (receiverExpression == null)
 //			System.out.println();
 		ISourcePosition namePos = restoreMethodNamePosition(iVisited,
 				receiverExpression.sourceEnd());
 		String name = iVisited.getName();
 		if (receiverNode instanceof SelfNode) {
 			name = "self." + name;
 		} else if (receiverNode instanceof ConstNode) {
 			name = ((ConstNode)receiverNode).getName() + "." + name;
 		}		
 		RubySingletonMethodDeclaration method = new RubySingletonMethodDeclaration(
 				name, namePos.getStartOffset(), namePos
 						.getEndOffset(), cPos.getStartOffset(), cPos
 						.getEndOffset(), receiverExpression);
 		method.setModifier(Modifiers.AccStatic);
 		ASTUtils.setVisibility(method, Modifiers.AccPublic);
 		if (peekState() instanceof ClassLikeState) {
 			ClassLikeState classState = (ClassLikeState) peekState();
 			ASTUtils.setVisibility(method, classState.visibility);
 		}
 		peekState().add(method);
 		pushState(new MethodState(method));
 		ArgsNode args = (ArgsNode) iVisited.getArgsNode();
 		if (args != null) {
 			List list = processMethodArguments(args);
 			method.acceptArguments(list);
 		}
 		iVisited.getBodyNode().accept(this);
 		popState();
 		return null;
 	}
 
 	public Instruction visitDotNode(DotNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitEnsureNode(EnsureNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitEvStrNode(EvStrNode iVisited) {
 //		System.out.println("DLTKASTBuildVisitor.visitEvStrNode()");
 		return null;
 	}
 
 	/** @fixme iteration not correctly defined */
 	public Instruction visitFCallNode(FCallNode iVisited) {
 		// System.out.println("DLTKASTBuildVisitor.visitFCallNode(" +
 		// iVisited.getName() + ")");
 		String methodName = iVisited.getName();
 
 //		System.out.println("== (AST) Method name: " + methodName);
 
 		State state = peekState();
 		if (state instanceof ClassLikeState) {
 			if (methodName.equals("private"))
 				handleVisibilitySetter(iVisited, Modifiers.AccPrivate);
 			else if (methodName.equals("protected"))
 				handleVisibilitySetter(iVisited, Modifiers.AccProtected);
 			else if (methodName.equals("public"))
 				handleVisibilitySetter(iVisited, Modifiers.AccPublic);
 		}
 
 		int argsStart = -1, argsEnd = -1;
 		CallArgumentsList argList = new CallArgumentsList();
 		Node argsNode = iVisited.getArgsNode();
 		if (argsNode != null) {
 			pushState(new ArgumentsState(argList));
 			if (argsNode instanceof ListNode) {
 				ListNode arrayNode = (ListNode) argsNode;
 				List list = arrayNode.childNodes();
 				for (Iterator iter = list.iterator(); iter.hasNext();) {
 					Node node = (Node) iter.next();
 					node.accept(this);
 				}
 			} else {
 				if (TRACE_RECOVERING)
 					RubyPlugin.log("DLTKASTBuildVisitor.visitFCallNode("
 							+ methodName + ") - unknown args node type: "
 							+ argsNode.getClass().getName());
 				argsNode.accept(this);
 			}
 			popState();
 			List children = argsNode.childNodes();
 			if (children.size() > 0) {
 				argsStart = ((Node) children.get(0)).getPosition()
 						.getStartOffset();
 				argsEnd = ((Node) children.get(children.size() - 1))
 						.getPosition().getEndOffset() + 1;
 			}
 		}
 
 		CallExpression c = new CallExpression(null, methodName, argList);
 
 		int funcNameStart = iVisited.getPosition().getStartOffset();
 		c.setStart(funcNameStart);
 		fixFunctionCallOffsets(c, methodName, funcNameStart, argsStart, argsEnd);
 
 		peekState().add(c);
 
 		return null;
 	}
 
 	private void handleVisibilitySetter(FCallNode node, int newVisibility) {
 		State state = peekState();
 		if (state instanceof ClassLikeState) {
 			ClassLikeState classState = (ClassLikeState) state;
 			Node argsNode = node.getArgsNode();
 			if (argsNode instanceof ArrayNode) {
 				ArrayNode argsArrayNode = (ArrayNode) argsNode;
 				List args = argsArrayNode.childNodes();
 				for (Iterator iter = args.iterator(); iter.hasNext();) {
 					Node arg = (Node) iter.next();
 					if (arg instanceof SymbolNode) {
 						SymbolNode symbolNode = (SymbolNode) arg;
 						String xmethodName = symbolNode.getName();
 						List statements = classState.type.getStatements();
 						for (Iterator statIter = statements.iterator(); statIter
 								.hasNext();) {
 							Statement statement = (Statement) statIter.next();
 							if (statement instanceof MethodDeclaration) {
 								MethodDeclaration methodDeclaration = (MethodDeclaration) statement;
 								if (methodDeclaration.getName().equals(
 										xmethodName))
 									ASTUtils.setVisibility(methodDeclaration,
 											newVisibility);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public Instruction visitFalseNode(FalseNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitFlipNode(FlipNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitForNode(ForNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitGlobalAsgnNode(GlobalAsgnNode iVisited) {
 
 		iVisited.getValueNode().accept(this);
 		return null;
 	}
 
 	public Instruction visitGlobalVarNode(GlobalVarNode iVisited) {
 		processVariableReference(iVisited, iVisited.getName(),
 				RubyVariableKind.GLOBAL);
 		return null;
 	}
 
 	public Instruction visitHashNode(HashNode iVisited) {
 		return null;
 	}
 
 	public Instruction visitInstAsgnNode(InstAsgnNode iVisited) {
 		processVariableAssignment(iVisited, iVisited.getName(),
 				RubyVariableKind.INSTANCE, iVisited.getValueNode());
 		return null;
 	}
 
 	public Instruction visitInstVarNode(InstVarNode iVisited) {
 		processVariableReference(iVisited, iVisited.getName(),
 				RubyVariableKind.INSTANCE);
 		return null;
 	}
 
 	public Instruction visitIfNode(IfNode iVisited) {
 		Statement condition = collectSingleStatement(iVisited.getCondition());
 		Statement thenPart = collectSingleStatement(iVisited.getThenBody());
 		Statement elsePart = collectSingleStatement(iVisited.getElseBody());
 		IfStatement res = new IfStatement(condition, thenPart, elsePart);
 		res.setStart(iVisited.getPosition().getStartOffset());
 		res.setEnd(iVisited.getPosition().getEndOffset() + 1);
 		peekState().add(res);
 		return null;
 	}
 
 	public Instruction visitIterNode(IterNode iVisited) {
 //		System.out.println("DLTKASTBuildVisitor.visitIterNode()");
 		Iterator iterator = iVisited.childNodes().iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		return null;
 	}
 
 	public Instruction visitLocalAsgnNode(LocalAsgnNode iVisited) {
 		processVariableAssignment(iVisited, iVisited.getName(),
 				RubyVariableKind.LOCAL, iVisited.getValueNode());
 		return null;
 	}
 
 	private void processVariableAssignment(Node iVisited, String name,
 			RubyVariableKind varKind, Node valueNode) {
 		ISourcePosition pos = iVisited.getPosition();
 		Expression left = new VariableReference(pos.getStartOffset(), pos
 				.getStartOffset()
 				+ name.length(), name, varKind);
 		Statement right = collectSingleStatement(valueNode);
 		if (right == null) {
 			if (TRACE_RECOVERING)
 				RubyPlugin.log("DLTKASTBuildVisitor.processVariableAssignment("
 						+ name + "): cannot parse rhs, skipped");
 			return;
 		}
 		Assignment assgn = new Assignment(left, right);
 		copyOffsets(assgn, iVisited);
 		peekState().add(assgn);
 	}
 
 	public Instruction visitLocalVarNode(LocalVarNode iVisited) {
 		ISourcePosition pos = fixNamePosition(iVisited.getPosition());
 		String varName = content.subSequence(pos.getStartOffset(),
 				pos.getEndOffset()).toString();
 		processVariableReference(iVisited, varName, RubyVariableKind.LOCAL);
 		return null;
 	}
 
 	private void processVariableReference(Node iVisited, String varName,
 			RubyVariableKind varKind) {
 		ISourcePosition pos2 = fixNamePosition(iVisited.getPosition());
 		if (varName.endsWith("\r"))
 			varName = varName.substring(0, varName.length() - 1);
 		VariableReference node = new VariableReference(pos2.getStartOffset(),
 				pos2.getEndOffset(), varName, varKind);
 		peekState().add(node);
 	}
 
 	private void copyOffsets(ASTNode target, Node source) {
 		ISourcePosition pos = source.getPosition();
 		target.setStart(pos.getStartOffset());
 		target.setEnd(pos.getEndOffset() + 1);
 	}
 
 	public Instruction visitMultipleAsgnNode(MultipleAsgnNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitMatch2Node(Match2Node iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitMatch3Node(Match3Node iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitMatchNode(MatchNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitModuleNode(ModuleNode iVisited) {
 		String name = "";
 		Node cpathNode = iVisited.getCPath();
 		if (cpathNode instanceof Colon2Node || cpathNode instanceof ConstNode) {
 			name = colons2Name(cpathNode);
 		}
 		ISourcePosition pos = iVisited.getCPath().getPosition();
 		ISourcePosition cPos = iVisited.getPosition();
 		cPos = fixNamePosition(cPos);
 		pos = fixNamePosition(pos);
 		TypeDeclaration type = new TypeDeclaration(name, pos.getStartOffset(),
 				pos.getEndOffset(), cPos.getStartOffset(), cPos.getEndOffset());
 		type.setModifier(Modifiers.AccModule);
 		peekState().add(type);
 		pushState(new ModuleState(type));
 		// body
 		pos = iVisited.getBodyNode().getPosition();
 		if (iVisited.getBodyNode().getBodyNode() != null) {
 			Node bodyNode = iVisited.getBodyNode().getBodyNode();
 			int end = -1;
 			while (bodyNode instanceof NewlineNode)
 				bodyNode = ((NewlineNode) bodyNode).getNextNode();
 			if (bodyNode instanceof BlockNode) {
 				BlockNode blockNode = (BlockNode) bodyNode;
 				end = blockNode.getLast().getPosition().getEndOffset(); // /XXX!!!!
 			} else {
 				if (TRACE_RECOVERING)
 					RubyPlugin.log("DLTKASTBuildVisitor.visitModuleNode("
 							+ name + "): unknown body type "
 							+ bodyNode.getClass().getName());
 			}
 			pos = fixBorders(pos);
 			Block bl = new Block(pos.getStartOffset(), (end == -1) ? pos
 					.getEndOffset() : end);
 			type.setBody(bl);
 			iVisited.getBodyNode().accept(this);
 		}
 		popState();
 		return null;
 	}
 
 	public Instruction visitNewlineNode(NewlineNode iVisited) {
 
 		iVisited.getNextNode().accept(this);
 		return null;
 	}
 
 	public Instruction visitNextNode(NextNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitNilNode(NilNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitNotNode(NotNode iVisited) {
 
 		iVisited.getConditionNode().accept(this);
 		return null;
 	}
 
 	public Instruction visitNthRefNode(NthRefNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitOpElementAsgnNode(OpElementAsgnNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitOpAsgnNode(OpAsgnNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitOpAsgnAndNode(OpAsgnAndNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitOpAsgnOrNode(OpAsgnOrNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitOptNNode(OptNNode iVisited) {
 //		System.out.println("DLTKASTBuildVisitor.visitOptNNode()");
 		iVisited.getBodyNode().accept(this);
 		return null;
 	}
 
 	public Instruction visitOrNode(OrNode iVisited) {
 		CollectingState state = new CollectingState();
 		pushState(state);
 		iVisited.getFirstNode().accept(this);
 		popState();
 
 		Expression left = null;
 		if (state.list.size() == 1 && state.list.get(0) instanceof Expression) {
 			left = (Expression) state.list.get(0);
 		}
 
 		state.list.clear();
 		pushState(state);
 		iVisited.getSecondNode().accept(this);
 		popState();
 
 		Statement right = null;
 		if (state.list.size() == 1 && state.list.get(0) instanceof Statement) {
 			right = (Statement) state.list.get(0);
 		}
 
 		OrExpression or = new OrExpression(left, right);
 
 		peekState().add(or);
 
 		return null;
 	}
 
 	public Instruction visitPostExeNode(PostExeNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitRedoNode(RedoNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitRescueBodyNode(RescueBodyNode iVisited) {
 
 		// XXX iVisited.getBodyNode().accept(this);
 		return null;
 	}
 
 	public Instruction visitRescueNode(RescueNode iVisited) {
 
 		/*
 		 * XXX iVisited.getHeadNode().accept(this); Node lElseNode =
 		 * iVisited.getElseNode(); if (lElseNode != null)
 		 * lElseNode.accept(this); for (Node body = iVisited.getResqNode(); body !=
 		 * null; body = iVisited.getHeadNode()) { Node lArgsNode =
 		 * body.getArgsNode(); for (; lArgsNode != null; lArgsNode =
 		 * lArgsNode.getNextNode()) lArgsNode.getHeadNode().accept(this);
 		 * body.accept(this); }
 		 */
 		return null;
 	}
 
 	public Instruction visitRetryNode(RetryNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitReturnNode(ReturnNode iVisited) {
 		ISourcePosition position = iVisited.getPosition();
 		ASTNode value = null;
 		if (iVisited.getValueNode() != null) {
 			CollectingState state = new CollectingState();
 			pushState(state);
 			iVisited.getValueNode().accept(this);
 			popState();
 			if (state.list.size() == 1 && state.list.get(0) instanceof ASTNode) {
 				value = (ASTNode) state.list.get(0);
 			}
 		}
 		peekState().add(
 				new ReturnStatement(value, position.getStartOffset(), position
 						.getEndOffset()));
 		return null;
 	}
 
 	public Instruction visitSClassNode(SClassNode iVisited) {
 		String name = "";
 		Node receiver = iVisited.getReceiverNode();
 		if (receiver instanceof ConstNode) {
 			name = "<< " + ((ConstNode) iVisited.getReceiverNode()).getName();
 		} else if (receiver instanceof SelfNode) {
 			name = "<< self";
 		} 
 		ISourcePosition pos = iVisited.getReceiverNode().getPosition();
 		ISourcePosition cPos = iVisited.getPosition();
 		RubySingletonClassDeclaration type = new RubySingletonClassDeclaration(name, pos.getStartOffset(),
 				pos.getEndOffset() + 1, cPos.getStartOffset(), cPos
 						.getEndOffset() + 1);
 		peekState().add(type);
 		
 		CollectingState coll = new CollectingState();
 		pushState(coll);
 		receiver.accept(this);
 		popState();
 		if (coll.list.size() == 1 && coll.list.get(0) instanceof Expression) {
 			Object obj = coll.list.get(0);
 			type.setReceiver((Expression) obj);
 			if (obj instanceof SimpleReference) {
 				SimpleReference reference = (SimpleReference) obj;
 				type.setName("<< " + reference.getName());
 			}
 		}		
 		pushState(new ClassState(type));
 		pos = iVisited.getBodyNode().getPosition();
 		Block bl = new Block(pos.getStartOffset(), pos.getEndOffset() + 1);
 		type.setBody(bl);
 		iVisited.getBodyNode().accept(this);
 		popState();
 		return null;
 	}
 
 	public Instruction visitScopeNode(ScopeNode iVisited) {
 
 		if (iVisited.getBodyNode() != null) {
 			iVisited.getBodyNode().accept(this);
 		}
 		return null;
 	}
 
 	public Instruction visitSelfNode(SelfNode iVisited) {
 		ISourcePosition position = fixNamePosition(iVisited.getPosition());
 		peekState().add(
 				new SelfReference(position.getStartOffset(), position
 						.getEndOffset()));
 		return null;
 	}
 
 	public Instruction visitSplatNode(SplatNode iVisited) {
 		Iterator iterator = iVisited.childNodes().iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 
 		return null;
 	}
 
 	public Instruction visitStrNode(StrNode iVisited) {
 		String value = iVisited.getValue();
 		ISourcePosition position = iVisited.getPosition();
 		peekState().add(
 				new StringLiteral(position.getStartOffset(), position
 						.getEndOffset(), value));
 		return null;
 	}
 
 	public Instruction visitSValueNode(SValueNode iVisited) {
 		Iterator iterator = iVisited.childNodes().iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		return null;
 	}
 
 	public Instruction visitSuperNode(SuperNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitToAryNode(ToAryNode iVisited) {
 		Iterator iterator = iVisited.childNodes().iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		return null;
 	}
 
 	public Instruction visitTrueNode(TrueNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitUndefNode(UndefNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitUntilNode(UntilNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitVAliasNode(VAliasNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitVCallNode(VCallNode iVisited) {
 		String methodName = iVisited.getMethodName();
 		// System.out.println("DLTKASTBuildVisitor.visitVCallNode(" + methodName
 		// + ")");
 
 		State state = peekState();
 		if (state instanceof ClassLikeState) {
 			ClassLikeState classState = (ClassLikeState) state;
 			if (methodName.equals("private"))
 				classState.visibility = Modifiers.AccPrivate;
 			else if (methodName.equals("protected"))
 				classState.visibility = Modifiers.AccProtected;
 			else if (methodName.equals("public"))
 				classState.visibility = Modifiers.AccPublic;
 		}
 
 		ISourcePosition pos = iVisited.getPosition();
 		int funcNameStart = pos.getStartOffset();
 		int funcNameEnd = pos.getEndOffset() + 1;
 		// Assert.isTrue(funcNameStart + methodName.length() == funcNameEnd);
 		CallExpression c = new CallExpression(null, methodName,
 				CallArgumentsList.EMPTY);
 		c.setStart(funcNameStart);
 		c.setEnd(funcNameEnd);
 		this.peekState().add(c);
 
 		return null;
 	}
 
 	public Instruction visitWhenNode(WhenNode iVisited) {
 
 		/*
 		 * XXX iVisited.getConditionNode().accept(this);
 		 * iVisited.getBodyNode().accept(this);
 		 */
 		return null;
 	}
 
 	public Instruction visitWhileNode(WhileNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitXStrNode(XStrNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitYieldNode(YieldNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitZArrayNode(ZArrayNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitZSuperNode(ZSuperNode iVisited) {
 
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitBignumNode(BignumNode)
 	 */
 	public Instruction visitBignumNode(BignumNode iVisited) {
 
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitFixnumNode(FixnumNode)
 	 */
 	public Instruction visitFixnumNode(FixnumNode iVisited) {
 		NumericLiteral node = new NumericLiteral(new DLTKToken(0, String
 				.valueOf(iVisited.getValue())));
 		ISourcePosition pos = iVisited.getPosition();
 		node.setStart(pos.getStartOffset());
 		node.setEnd(pos.getEndOffset() + 1);
 		peekState().add(node);
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitFloatNode(FloatNode)
 	 */
 	public Instruction visitFloatNode(FloatNode iVisited) {
 
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitRegexpNode(RegexpNode)
 	 */
 	public Instruction visitRegexpNode(RegexpNode iVisited) {
 
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitSymbolNode(SymbolNode)
 	 */
 	public Instruction visitSymbolNode(SymbolNode iVisited) {
 		ISourcePosition position = iVisited.getPosition();
 
 		SymbolReference sr = new SymbolReference(position.getStartOffset(),
 				position.getEndOffset(), iVisited.getName());
 		peekState().add(sr);
 		
 		return null;
 	}
 }
