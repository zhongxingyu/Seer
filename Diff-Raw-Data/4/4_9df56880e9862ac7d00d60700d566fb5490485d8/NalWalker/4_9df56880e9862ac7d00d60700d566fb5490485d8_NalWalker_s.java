 /*
  * Copyright (c) 2013 Patrick Fromberg
  * See BSD 3 clause license in LICENSE.txt or
  * LICENSE.txt or http://opensource.org/licenses/BSD-3-Clause
  */
 
 package nal.antlr;
 
 import org.antlr.v4.runtime.ParserRuleContext;
 import org.antlr.v4.runtime.tree.ParseTree;
 import org.antlr.v4.runtime.tree.TerminalNode;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /*
  * The NalWalker visits all nodes recognized by ANTLR and create a second
  * Nal specific tree of NalNode objects.
  * This NalNode tree can then be visited by interpreters (not implemented)
  * or translators (see also NalTranslator).
  *
  * The NalWalker extends <a href="http://www.antlr.org/api/Java/org/antlr/v4/runtime/tree/AbstractParseTreeVisitor
  * .html">AbstractParseTreeVisitor<a>
  * from the ANTLR project.
  *
  * Note that Nal has not type definition language and must infer all types
  * taking the data sources as starting point. i.e. the compiler must read
  * the contents of the data sources, at least the schema information.
  *
  * Attention, children can have more then one parent. But each child will
  * only record one parent, If a child has more then one parent, then the
  * recorded parent will often be the root node.
  */
 
 public class NalWalker extends NalBaseVisitor<NalNode>
 {
 	final public Map<Integer, NalNode> pre_comment_nodes = new HashMap<Integer, NalNode>();
 	final public Map<Integer, NalNode> post_comment_nodes = new HashMap<Integer, NalNode>();
 	final public Map<Integer, NalNode> intra_comment_nodes = new HashMap<Integer, NalNode>();
 	final private Map<Integer, String> tokenNames_;
 	//public List<NalNode> node_sequence = new ArrayList<NalNode>();
 	private NalNode currentNode;
 	private NalNode rootNode_ = null;
 	//private Parser parser_;
 
 	public NalWalker(Map<Integer, String> tokenNames)
 	{
 		tokenNames_ = tokenNames;
 	}
 
 	private NalNode getCurrentNode()
 	{
 		return currentNode;
 	}
 
 	public NalNode visit(ParseTree tree, NalNode parent)
 	{
 		// create node and set parent child relationship
 		// NalNode oldParent = currParentNode;
 		NalNode oldCurrent = currentNode;
 		ParserRuleContext ctx = (ParserRuleContext) tree;
 		try {
 			if (ctx == null) {
 				throw new RuntimeException("Programming error");
 			}
 			currentNode = new NalNode();
 			//node_sequence.add(currentNode);
 
 			if (parent != null) {
 				currentNode.parent = parent;
 				currentNode.parent.children.add(currentNode);
 			} else {
 				rootNode_ = currentNode;
 			}
 
 			currentNode.line = ctx.start.getLine();
 			currentNode.lastline = ctx.stop.getLine();
 			currentNode.position = ctx.start.getCharPositionInLine();
 			pre_comment_nodes.put(ctx.start.getTokenIndex() - 1, currentNode);
 			visit(tree);
 			post_comment_nodes.put(ctx.stop.getTokenIndex() + 1, currentNode);
 			return currentNode;
 		} catch (NalException e) {
 			throw e;
 		} catch (Exception e) {
 			NalException x = new NalException("error " + currentNode.nodeType + ":" + currentNode.id +
 					". ", e.getMessage(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
 			x.setStackTrace(e.getStackTrace());
 			throw x;
 		} finally {
 			currentNode = oldCurrent;
 		}
 	}
 
 	@Override
 	public NalNode visitSimpleAttribute(
 			NalParser.SimpleAttributeContext ctx)
 	{
 		NalNode n = getCurrentNode();
 		n.nodeType = ctx.IDENT().getText();
 		n.ruleType = "obj";
 		visit(ctx.expr(), n);
 		return n;
 	}
 
 	@Override
 	public NalNode visitStructuredObject(
 			NalParser.StructuredObjectContext ctx)
 	{
 		NalNode n = getCurrentNode();
 		n.nodeType = ctx.IDENT(0).getText();
 		n.ruleType = "obj";
 
 		int ident_size = ctx.IDENT().size();
 		// first ident is node type second the node name if existent.
 		// further idents are the parameters if existent.
 		if (ident_size > 1) {
 			n.id = ctx.IDENT(1).getText();
 		}
 
 		for (TerminalNode e : ctx.BINDVAR()) {
 			n.parameters.add(e.getText());
 		}
 
 		// obj index -1 is '{' and obj index -2 is the comment if it exists
 		intra_comment_nodes.put(ctx.obj(0).getStart().getTokenIndex() - 2, n);
 
 		for (NalParser.ObjContext e : ctx.obj()) {
 			visit(e, n);
 		}
 		if (n.nodeType.equals("function")) {
 			rootNode_.nal_functions.put(n.id, n); //fix patrick, _ root_n?
 		}
 		return n;
 	}
 
 	@Override
 	public NalNode visitFunctionCall(NalParser.FunctionCallContext ctx)
 	{
 		// also supports foo.bar(x) instead of bar(foo, x)
 		NalNode n = getCurrentNode();
 		n.id = ctx.IDENT().getText();
 		n.ruleType = "expr";
 		if (!ctx.expr().isEmpty()) {
 			intra_comment_nodes.put(ctx.IDENT().getSymbol().getTokenIndex() + 1, n);
 		}
 
 		if (ctx.expr().size() > 0) { //fix patrick, this distinction will not be valid in future
 			n.nodeType = "library_funccall";
 
 		} else {
 			n.nodeType = "funccall";
 		}
 
 		for (NalParser.ExprContext e : ctx.expr()) {
 			visit(e, n);
 		}
 		return n;
 	}
 
 	@Override
 	public NalNode visitAtOperatorCall(NalParser.AtOperatorCallContext ctx)
 	{
 		// operator []
 		NalNode n = getCurrentNode();
 		n.id = "AT_FUNCTION";
 		n.nodeType = "at_operatorcall";
 		n.ruleType = "expr";
 
 		for (NalParser.ExprContext e : ctx.expr()) {
 			visit(e, n);
 		}
 		return n;
 	}
 
 	@Override
 	public NalNode visitOperatorCall(NalParser.OperatorCallContext ctx)
 	{
 		NalNode n = getCurrentNode();
 		n.id = tokenNames_.get(ctx.op.getType());
 		n.nodeType = "operatorcall";
 		n.ruleType = "expr";
 
 		for (NalParser.ExprContext e : ctx.expr()) {
 			visit(e, n);
 		}
 		return n;
 	}
 
 	@Override
 	public NalNode visitBooleanLiteral(NalParser.BooleanLiteralContext ctx)
 	{
 		NalNode n = getCurrentNode();
 		n.nodeType = "literal";
 		n.ruleType = "expr";
 		n.value = ctx.BOOLEAN().getText();
 		n.elementType = "bool";
 		n.containerType = null;
 		n.size1 = "0";
 		n.size2 = "0";
 		n.isTimeDependent = false;
 		n.isSeedDependent = false;
 		n.hasType = true;
 		return n;
 	}
 
 	@Override
 	public NalNode visitStringLiteral(NalParser.StringLiteralContext ctx)
 	{
 		NalNode n = getCurrentNode();
 		n.nodeType = "literal";
 		n.ruleType = "expr";
 		n.value = ctx.STRING().getText();
 		n.elementType = "charptr";
 		n.containerType = null;
 		n.size1 = "0";
 		n.size2 = "0";
 		n.isTimeDependent = false;
 		n.isSeedDependent = false;
 		n.hasType = true;
 		return n;
 	}
 
 	@Override
 	public NalNode visitIntLiteral(NalParser.IntLiteralContext ctx)
 	{
 		NalNode n = getCurrentNode();
 		n.nodeType = "literal";
 		n.ruleType = "expr";
 		n.value = ctx.INT().getText();
 		n.elementType = "long";
 		n.containerType = null;
 		n.size1 = "0";
 		n.size2 = "0";
 		n.isTimeDependent = false;
 		n.isSeedDependent = false;
 		n.hasType = true;
 		return n;
 	}
 
 	@Override
 	public NalNode visitFloatLiteral(NalParser.FloatLiteralContext ctx)
 	{
 		NalNode n = getCurrentNode();
 		n.nodeType = "literal";
 		n.ruleType = "expr";
 		n.value = ctx.FLOAT().getText();
 		n.elementType = "double";
 		n.containerType = null;
 		n.size1 = "0";
 		n.size2 = "0";
 		n.isTimeDependent = false;
 		n.isSeedDependent = false;
 		n.hasType = true;
 		return n;
 	}
 
 	@Override
 	public NalNode visitBindvarDeclaration(NalParser.BindvarDeclarationContext ctx)
 	{
 		NalNode n = getCurrentNode();
 		NalNode clone = n;
 
 		for (TerminalNode e : ctx.BINDVAR()) {
 			if (clone == null) {
 				clone = new NalNode(n);
 				n.parent.children.add(clone);
 			}
 			clone.value = null;
 			clone.id = e.getText();
 			clone.nodeType = "bindvar";
 			clone.ruleType = "expr";
 
 			// check for shadowing
 			NalNode p = n;
 			while (p != null) {
 				if (p.nodeType.endsWith("funccall")) {
 					for (NalNode b : p.children) {
 						if (!b.nodeType.equals("bindvar")) {
 							break;
 						}
 						if (b != clone && b.id.equals(clone.id)) {
 							throw new RuntimeException(" bindvar already declared: '" +
 									clone.id + "'");
 						}
 					}
 				}
 				p = p.parent;
 			}
 			clone = null;
 		}
 

 		return n;
 	}
 
 	@Override
 	public NalNode visitBindvar(NalParser.BindvarContext ctx)
 	{
 		NalNode n = getCurrentNode();
 		n.value = null;
 		n.id = ctx.BINDVAR().getText();
 		n.nodeType = "bindvar_ref";
 		n.ruleType = "expr";
 
 		// check if bindvar was already declared.
 		NalNode p = n.parent;
 		search_for_declaration:
 		while (p != null) {
 			if (p.nodeType.endsWith("funccall")) {
 				for (NalNode e : p.children) {
					if (e.nodeType.startsWith("bindvar") && e.id.equals(n.id)) {
 						e.children.add(n);
 						break search_for_declaration;
 					}
 				}
 			}
 			if (NalNode.findById(p.bind_parameters, n.id) == null) {
 				p.bind_parameters.add(n);
 			}
 			p = p.parent;
 		}
 		if (p == null) {
 			throw new RuntimeException("using undeclared bindvar: '" + n.id + "'");
 		}
 
 		return n;
 	}
 
 	public NalNode visitParenthesis(NalParser.ParenthesisContext ctx)
 	{
 		NalNode n = getCurrentNode();
 		n.nodeType = "parenthesis";
 		n.ruleType = "expr";
 		n.id = "parenthesis";
 		NalParser.ExprContext expr = ctx.expr();
 
 		if (expr != null) {
 			visit(expr, n);
 		}
 		return n;
 	}
 }
