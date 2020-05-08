 /*
  * Author: C.Williams
  * 
  * Copyright (c) 2004 RubyPeople.
  * 
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. You
  * can get copy of the GPL along with further information about RubyPeople and
  * third party software bundled with RDT in the file
  * org.rubypeople.rdt.core_x.x.x/RDT.license or otherwise at
  * http://www.rubypeople.org/RDT.license.
  * 
  * RDT is free software; you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * RDT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * RDT; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  * Suite 330, Boston, MA 02111-1307 USA
  */
 package org.rubypeople.rdt.internal.core;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.jruby.ast.AliasNode;
 import org.jruby.ast.AndNode;
 import org.jruby.ast.ArgsCatNode;
 import org.jruby.ast.ArgsNode;
 import org.jruby.ast.ArgsPushNode;
 import org.jruby.ast.ArrayNode;
 import org.jruby.ast.AttrAssignNode;
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
 import org.jruby.ast.RootNode;
 import org.jruby.ast.SClassNode;
 import org.jruby.ast.SValueNode;
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
 import org.jruby.runtime.Visibility;
 import org.rubypeople.rdt.core.IMethod;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 import org.rubypeople.rdt.internal.core.util.ASTUtil;
 
 /**
  * @author Chris
  * 
  */
 public class RubyScriptStructureBuilder implements NodeVisitor {
 
 	private InfoStack infoStack = new InfoStack();
 	private HandleStack modelStack = new HandleStack();
 	private RubyScriptElementInfo scriptInfo;
 	private IRubyScript script;
 	private Visibility currentVisibility = Visibility.PUBLIC;
 	private Map newElements;
 	private RubyElementInfo importContainerInfo;
 	private boolean DEBUG = false;
 
 	/**
 	 * 
 	 * @param script
 	 *            The RubyScript whose contents we're parsing
 	 * @param info
 	 *            The RubyElementInfo of the RubyScript
 	 * @param newElements
 	 *            a Map passed in. It is actually a temporarcy cache from the
 	 *            RubyModelManager. It holds elements below the level of a
 	 *            RubyScript in our hierarchy.
 	 */
 	public RubyScriptStructureBuilder(IRubyScript script,
 			RubyScriptElementInfo info, Map newElements) {
 		this.script = script;
 		this.scriptInfo = info;
 		this.newElements = newElements;
 		modelStack.push(script);
 		infoStack.push(scriptInfo);
 		DEBUG = RubyParser.isDebugging();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitAliasNode(org.jruby.ast.AliasNode)
 	 */
 	public Instruction visitAliasNode(AliasNode iVisited) {
 		handleNode(iVisited);
 
 		String name = iVisited.getNewName();
 
 		// TODO Use the visibility for the original method that this is aliasing
 		Visibility visibility = currentVisibility;
 		if (name.equals("initialize"))
 			visibility = Visibility.PROTECTED;
 
 		// TODO Find the existing method and steal it's parameter names
 		String[] parameterNames = new String[0];
 		RubyMethod method = new RubyMethod(getCurrentType(), name,
 				parameterNames);
 		modelStack.push(method);
 
 		RubyElementInfo parentInfo = infoStack.peek();
 		parentInfo.addChild(method);
 
 		RubyMethodElementInfo info = new RubyMethodElementInfo();
 		info.setVisibility(convertVisibility(visibility));
 		ISourcePosition pos = iVisited.getPosition();
 		setKeywordRange("alias", pos, info, ":" + name);
 		infoStack.push(info);
 		newElements.put(method, info);
 
 		modelStack.pop();
 		infoStack.pop();
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitAndNode(org.jruby.ast.AndNode)
 	 */
 	public Instruction visitAndNode(AndNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getSecondNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitArgsNode(org.jruby.ast.ArgsNode)
 	 */
 	public Instruction visitArgsNode(ArgsNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBlockArgNode());
 		if (iVisited.getOptArgs() != null) {
 			visitIter(iVisited.getOptArgs().iterator());
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitArgsCatNode(org.jruby.ast.ArgsCatNode)
 	 */
 	public Instruction visitArgsCatNode(ArgsCatNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getSecondNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitArrayNode(org.jruby.ast.ArrayNode)
 	 */
 	public Instruction visitArrayNode(ArrayNode iVisited) {
 		handleNode(iVisited);
 		visitIter(iVisited.iterator());
 		return null;
 	}
 
 	/**
 	 * @param iterator
 	 */
 	private Instruction visitIter(Iterator iterator) {
 		while (iterator.hasNext()) {
 			visitNode((Node) iterator.next());
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBackRefNode(org.jruby.ast.BackRefNode)
 	 */
 	public Instruction visitBackRefNode(BackRefNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBeginNode(org.jruby.ast.BeginNode)
 	 */
 	public Instruction visitBeginNode(BeginNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBodyNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBignumNode(org.jruby.ast.BignumNode)
 	 */
 	public Instruction visitBignumNode(BignumNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBlockArgNode(org.jruby.ast.BlockArgNode)
 	 */
 	public Instruction visitBlockArgNode(BlockArgNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBlockNode(org.jruby.ast.BlockNode)
 	 */
 	public Instruction visitBlockNode(BlockNode iVisited) {
 		handleNode(iVisited);
 		visitIter(iVisited.iterator());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBlockPassNode(org.jruby.ast.BlockPassNode)
 	 */
 	public Instruction visitBlockPassNode(BlockPassNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getBodyNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBreakNode(org.jruby.ast.BreakNode)
 	 */
 	public Instruction visitBreakNode(BreakNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitConstDeclNode(org.jruby.ast.ConstDeclNode)
 	 */
 	public Instruction visitConstDeclNode(ConstDeclNode iVisited) {
 		handleNode(iVisited);
 		String name = iVisited.getName();
 		RubyElement type = getCurrentType();
 		RubyConstant handle = new RubyConstant(type, name);
 		modelStack.push(handle);
 		RubyElementInfo parentInfo = getCurrentTypeInfo();
 		parentInfo.addChild(handle);
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		ISourcePosition pos = iVisited.getPosition();
 		setTokenRange(pos, info, name);
 		// TODO Add more information about the variable
 		infoStack.push(info);
 		newElements.put(handle, info);
 		visitNode(iVisited.getValueNode());
 		modelStack.pop();
 		infoStack.pop();
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassVarAsgnNode(org.jruby.ast.ClassVarAsgnNode)
 	 */
 	public Instruction visitClassVarAsgnNode(ClassVarAsgnNode iVisited) {
 		handleNode(iVisited);
 		String name = iVisited.getName();
 		RubyElement type = getCurrentType();
 		RubyClassVar handle = new RubyClassVar(type, name);
 		modelStack.push(handle);
 
 		RubyElementInfo parentInfo = getCurrentTypeInfo();
 		parentInfo.addChild(handle);
 
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		setTokenRange(iVisited.getPosition(), info, name);
 		// TODO Add more information about the variable
 		infoStack.push(info);
 
 		newElements.put(handle, info);
 
 		visitNode(iVisited.getValueNode());
 
 		modelStack.pop();
 		infoStack.pop();
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassVarDeclNode(org.jruby.ast.ClassVarDeclNode)
 	 */
 	public Instruction visitClassVarDeclNode(ClassVarDeclNode iVisited) {
 		handleNode(iVisited);
 		String name = iVisited.getName();
 		RubyElement type = getCurrentType();
 		RubyClassVar var = new RubyClassVar(type, iVisited.getName());
 
 		RubyElementInfo parentInfo = infoStack.peek();
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		setTokenRange(iVisited.getPosition(), info, name);
 
 		parentInfo.addChild(var);
 
 		newElements.put(var, info);
 
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassVarNode(org.jruby.ast.ClassVarNode)
 	 */
 	public Instruction visitClassVarNode(ClassVarNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/**
 	 * @return
 	 */
 	private RubyElementInfo getCurrentTypeInfo() {
 		List extras = new ArrayList();
 		RubyElementInfo element = infoStack.peek();
 		while (!(element instanceof RubyTypeElementInfo)) {
 			extras.add(infoStack.pop());
 			element = infoStack.peek();
 			if (element == null)
 				break;
 		}
 		for (Iterator iter = extras.iterator(); iter.hasNext();) {
 			infoStack.push((RubyElementInfo) iter.next());
 		}
 		if (element == null)
 			return scriptInfo;
 		return element;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitCallNode(org.jruby.ast.CallNode)
 	 */
 	public Instruction visitCallNode(CallNode iVisited) {
 		handleNode(iVisited);
 		// FIXME Evaluate the receiver and check to see if the method exists!
 		if (DEBUG)
 			System.out.println(iVisited.getName());
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getIterNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitCaseNode(org.jruby.ast.CaseNode)
 	 */
 	public Instruction visitCaseNode(CaseNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getCaseNode());
 		visitNode(iVisited.getFirstWhenNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassNode(org.jruby.ast.ClassNode)
 	 */
 	public Instruction visitClassNode(ClassNode iVisited) {
 		handleNode(iVisited);
 
 		// This resets the visibility when opening or declaring a class to
 		// public
 		currentVisibility = Visibility.PUBLIC;
 
 		String name = getFullyQualifiedName(iVisited.getCPath());
 		RubyType handle = new RubyType(modelStack.peek(), name);
 		RubyElement parent = modelStack.peek();
 		RubyType existing = (RubyType) findChild(parent, IRubyElement.TYPE, name);
 		if (existing != null) {
 		// FIXME Should we just increment the occurence count like I do here, or should we conglomerate the types into one LogicalType?
 			handle.occurrenceCount = existing.occurrenceCount + 1;
 		}
 		modelStack.push(handle);
 
 		RubyElementInfo parentInfo = infoStack.peek();
 		parentInfo.addChild(handle);
 
 		RubyTypeElementInfo info = new RubyTypeElementInfo();
 		info.setHandle(handle);
 		ISourcePosition pos = iVisited.getPosition();
 		setKeywordRange("class", pos, info, name);
 
 		String superClass = getSuperClassName(iVisited.getSuperNode());
 		info.setSuperclassName(superClass);
 		
 		info.setIncludedModuleNames(new String[] {});
 		infoStack.push(info);
 
 		newElements.put(handle, info);
 
 		visitNode(iVisited.getSuperNode());
 		visitNode(iVisited.getBodyNode());
 
 		// TODO Collect the included modules and set them here!
 		modelStack.pop();
 		infoStack.pop();
 		return null;
 	}
 
 	private RubyType findChild(RubyElement parent, int type, String name) {
 		try {
 			ArrayList<IRubyElement> children = parent.getChildrenOfType(type);
 			for (IRubyElement element : children) {
 				if (element.getElementName().equals(name)) return (RubyType) element;
 			}
 		} catch (RubyModelException e) {
 			RubyCore.log(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Build up the fully qualified name of the super class for a class
 	 * declaration
 	 * 
 	 * @param superNode
 	 * @return
 	 */
 	private String getSuperClassName(Node superNode) {
 		if (superNode == null)
 			return "Object";
 		return getFullyQualifiedName(superNode);
 	}
 
 	private String getFullyQualifiedName(Node node) {
 		if (node == null)
 			return "";
 		if (node instanceof ConstNode) {
 			ConstNode constNode = (ConstNode) node;
 			return constNode.getName();
 		}
 		if (node instanceof Colon2Node) {
 			Colon2Node colonNode = (Colon2Node) node;
 			String prefix = getFullyQualifiedName(colonNode.getLeftNode());
 			if (prefix.length() > 0)
 				prefix = prefix + "::";
 			return prefix + colonNode.getName();
 		}
 		return "";
 	}
 
 	/**
 	 * @param keyword
 	 * @param pos
 	 * @param info
 	 * @param name
 	 */
 	private void setKeywordRange(String keyword, ISourcePosition pos,
 			MemberElementInfo info, String name) {
 		// TODO Actually check nodes which make up the name for their position!
 		int nameStart = pos.getStartOffset() + keyword.length() + 1; // the
 		// extra
 		// 1 is
 		// for a
 		// space
 		// after
 		// the
 		// keyword
 		info.setNameSourceStart(nameStart);
 		info.setNameSourceEnd(nameStart + name.length() - 1);
 		info.setSourceRangeStart(pos.getStartOffset());
 		info.setSourceRangeEnd(pos.getEndOffset() - 1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitColon2Node(org.jruby.ast.Colon2Node)
 	 */
 	public Instruction visitColon2Node(Colon2Node iVisited) {
 		handleNode(iVisited);
 		if (DEBUG)
 			System.out.println(iVisited.getName());
 		visitNode(iVisited.getLeftNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitColon3Node(org.jruby.ast.Colon3Node)
 	 */
 	public Instruction visitColon3Node(Colon3Node iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitConstNode(org.jruby.ast.ConstNode)
 	 */
 	public Instruction visitConstNode(ConstNode iVisited) {
 		handleNode(iVisited);
 		if (DEBUG)
 			System.out.println(iVisited.getName());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDAsgnNode(org.jruby.ast.DAsgnNode)
 	 */
 	public Instruction visitDAsgnNode(DAsgnNode iVisited) {
 		handleNode(iVisited);
 		RubyDynamicVar var = new RubyDynamicVar(modelStack.peek(), iVisited
 				.getName());
 		modelStack.push(var);
 
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		infoStack.push(info);
 
 		newElements.put(var, info);
 
 		if (DEBUG)
 			System.out.println(iVisited.getName());
 		visitNode(iVisited.getValueNode());
 
 		modelStack.pop();
 		infoStack.pop();
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDRegxNode(org.jruby.ast.DRegexpNode)
 	 */
 	public Instruction visitDRegxNode(DRegexpNode iVisited) {
 		handleNode(iVisited);
 		visitIter(iVisited.iterator());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDStrNode(org.jruby.ast.DStrNode)
 	 */
 	public Instruction visitDStrNode(DStrNode iVisited) {
 		handleNode(iVisited);
 		visitIter(iVisited.iterator());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDSymbolNode(org.jruby.ast.DSymbolNode)
 	 */
 	public Instruction visitDSymbolNode(DSymbolNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDVarNode(org.jruby.ast.DVarNode)
 	 */
 	public Instruction visitDVarNode(DVarNode iVisited) {
 		handleNode(iVisited);
 		if (DEBUG)
 			System.out.println(iVisited.getName());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDXStrNode(org.jruby.ast.DXStrNode)
 	 */
 	public Instruction visitDXStrNode(DXStrNode iVisited) {
 		handleNode(iVisited);
 		visitIter(iVisited.iterator());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDefinedNode(org.jruby.ast.DefinedNode)
 	 */
 	public Instruction visitDefinedNode(DefinedNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getExpressionNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDefnNode(org.jruby.ast.DefnNode)
 	 */
 	public Instruction visitDefnNode(DefnNode iVisited) {
 		handleNode(iVisited);
 
 		String name = iVisited.getName();
 
 		Visibility visibility = currentVisibility;
 		if (name.equals("initialize"))
 			visibility = Visibility.PROTECTED;
 
 		RubyElement type = getCurrentType();
 		String[] parameterNames = ASTUtil.getArgs(iVisited.getArgsNode(), iVisited.getScope());
 		RubyMethod method = new RubyMethod(type, name, parameterNames);
 		modelStack.push(method);
 
 		RubyElementInfo parentInfo = infoStack.peek();
 		parentInfo.addChild(method);
 
 		RubyMethodElementInfo info = new RubyMethodElementInfo();
 		info.setArgumentNames(parameterNames);
 		// TODO Set more information
 		info.setVisibility(convertVisibility(visibility));
 		ISourcePosition pos = iVisited.getPosition();
 		setKeywordRange("def", pos, info, name);
 		infoStack.push(info);
 
 		newElements.put(method, info);
 
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getBodyNode());
 
 		modelStack.pop();
 		infoStack.pop();
 		return null;
 	}
 
 	/**
 	 * @param visibility
 	 * @return
 	 */
 	private int convertVisibility(Visibility visibility) {
 		// FIXME What about the module function and public-protected
 		// visibilities?
 		if (visibility == Visibility.PUBLIC)
 			return IMethod.PUBLIC;
 		if (visibility == Visibility.PROTECTED)
 			return IMethod.PROTECTED;
 		return IMethod.PRIVATE;
 	}
 
 	/**
 	 * @return
 	 */
 	private RubyElement getCurrentType() {
 		List extras = new ArrayList();
 		IRubyElement element = modelStack.peek();
 		while (!element.isType(IRubyElement.TYPE)) {
 			extras.add(modelStack.pop());
 			element = modelStack.peek();
 			if (element == null)
 				break;
 		}
 		for (Iterator iter = extras.iterator(); iter.hasNext();) {
 			modelStack.push((RubyElement) iter.next());
 		}
 		if (element == null)
 			return (RubyScript) script;
 		return (RubyElement) element;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDefsNode(org.jruby.ast.DefsNode)
 	 */
 	public Instruction visitDefsNode(DefsNode iVisited) {
 		handleNode(iVisited);
 
 		// Get the information no the current parent of this method
 		RubyElementInfo parentInfo = infoStack.peek();
 
 		/*
 		 * Get the name of the current static method and add the name of the
 		 * class or module to the beginning of it. This aInstructions instance
 		 * method naming conflicts. e.g.: class A def self.method; end def
 		 * method; end end will give us: A.method method in the Outline View.
 		 */
 		String fullName;
 		String receiver = ASTUtil.stringRepresentation(iVisited.getReceiverNode());
 		if (receiver != null && receiver.trim().length() > 0) {
 			fullName = receiver + "." + iVisited.getName();
 		} else {
 			fullName = iVisited.getName();
 		}
 
 		// Get the visibility of the current static method
 		Visibility visibility = currentVisibility;
 
 		String[] parameterNames = ASTUtil.getArgs(iVisited.getArgsNode(), iVisited.getScope());
 
 		// Get the type of the current parent element
 		RubyElement type = getCurrentType();
 		RubyMethod method = new RubySingletonMethod(type, iVisited.getName(), parameterNames);
 		modelStack.push(method);
 
 		parentInfo.addChild(method);
 
 		RubyMethodElementInfo info = new RubyMethodElementInfo();
 
 		// TODO Set more info!
 		infoStack.push(info);
 		ISourcePosition pos = iVisited.getPosition();
 		setKeywordRange("def", pos, info, fullName);
 
 		info.setArgumentNames(parameterNames);
 		info.setVisibility(convertVisibility(visibility));
 
 		newElements.put(method, info);
 
 		// FIXME Evaluate the receiver!
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getBodyNode());
 
 		modelStack.pop();
 		infoStack.pop();
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDotNode(org.jruby.ast.DotNode)
 	 */
 	public Instruction visitDotNode(DotNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBeginNode());
 		visitNode(iVisited.getEndNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitEnsureNode(org.jruby.ast.EnsureNode)
 	 */
 	public Instruction visitEnsureNode(EnsureNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getEnsureNode());
 		visitNode(iVisited.getBodyNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitEvStrNode(org.jruby.ast.EvStrNode)
 	 */
 	public Instruction visitEvStrNode(EvStrNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBody());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitFCallNode(org.jruby.ast.FCallNode)
 	 */
 	public Instruction visitFCallNode(FCallNode iVisited) {
 		handleNode(iVisited);
 		// FIXME Evaluate self and check to see if the method exists!
 		if (DEBUG)
 			System.out.println(iVisited.getName());
 		String functionName = iVisited.getName();
 		if (functionName.equals("require") || functionName.equals("load")) {
 			ArrayNode node = (ArrayNode) iVisited.getArgsNode();
 			String arg = getString(node);
 			if (arg != null) {
 				ImportContainer importContainer = (ImportContainer) script
 						.getImportContainer();
 				// create the import container and its info
 				if (this.importContainerInfo == null) {
 					this.importContainerInfo = new RubyElementInfo();
 					scriptInfo.addChild(importContainer);
 					this.newElements.put(importContainer,
 							this.importContainerInfo);
 				}
 				RubyImport handle = new RubyImport(importContainer, arg);
 
 				ImportDeclarationElementInfo info = new ImportDeclarationElementInfo();
 				setKeywordRange(functionName, node.getPosition(), info, arg);
 				info.name = arg; // no trailing * if onDemand
 
 				this.importContainerInfo.addChild(handle);
 				this.newElements.put(handle, info);
 			}
 		}
 		
 		// Collect included mixins
 		if ( functionName.equals("include") ) {
 			List<String> mixins = new LinkedList<String>();
 			Node argsNode = iVisited.getArgsNode();
 			Iterator iter = null;
 			if (argsNode instanceof SplatNode) {
 				SplatNode splat = (SplatNode) argsNode;
 				iter = splat.childNodes().iterator();
 			}
 			else if (argsNode instanceof ArrayNode) {
 				ArrayNode arrayNode = (ArrayNode) iVisited.getArgsNode();
 				iter = arrayNode.iterator();
 			}
 			for (; iter.hasNext();) {
 				Node mixinNameNode = (Node) iter.next();
 				if ( mixinNameNode instanceof StrNode ) {
 					mixins.add( ((StrNode)mixinNameNode).getValue() );
 				}
 				if ( mixinNameNode instanceof DStrNode ) {
 					Node next = (Node)((DStrNode)mixinNameNode).iterator().next();
 					if ( next instanceof StrNode ) {
 						mixins.add( ((StrNode)next).getValue() );
 					}
 				}
 				if (mixinNameNode instanceof ConstNode) {
 					mixins.add( ((ConstNode)mixinNameNode).getName() );
 				}
 			}
 			
 			// Push mixins into parent type, if available
 			if ( infoStack.peek() instanceof  RubyTypeElementInfo ) {
 				
 				// Get parent type
 				RubyTypeElementInfo parentType = (RubyTypeElementInfo)infoStack.peek();
 
 				// Get existing imported module names
 				String[] importedModuleNames = parentType.getIncludedModuleNames();
 				List<String> mergedModuleNames = new LinkedList<String>();
 				
 				// Merge newly found module name(s)
 				if ( importedModuleNames != null ) {
 					mergedModuleNames.addAll( (Arrays.asList( importedModuleNames )));
 				}
 				mergedModuleNames.addAll( mixins );
 				
 				// Apply included module names back to parent type info
 				String[] newIncludedModuleNames = mergedModuleNames.toArray(new String[]{});
 				parentType.setIncludedModuleNames( newIncludedModuleNames );
 			}
 
 			
 		}
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getIterNode());
 		return null;
 	}
 
 	/**
 	 * @param node
 	 * @return
 	 */
 	private String getString(ArrayNode node) {
 		Object tmp = node.iterator().next();
 		if (tmp instanceof DStrNode) {
 			DStrNode dstrNode = (DStrNode) tmp;
 			tmp = dstrNode.iterator().next();
 		}
 		if (tmp instanceof StrNode) {
 			StrNode strNode = (StrNode) tmp;
 			return strNode.getValue();
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitFalseNode(org.jruby.ast.FalseNode)
 	 */
 	public Instruction visitFalseNode(FalseNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitFixnumNode(org.jruby.ast.FixnumNode)
 	 */
 	public Instruction visitFixnumNode(FixnumNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitFlipNode(org.jruby.ast.FlipNode)
 	 */
 	public Instruction visitFlipNode(FlipNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBeginNode());
 		visitNode(iVisited.getEndNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitFloatNode(org.jruby.ast.FloatNode)
 	 */
 	public Instruction visitFloatNode(FloatNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitForNode(org.jruby.ast.ForNode)
 	 */
 	public Instruction visitForNode(ForNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getVarNode());
 		visitNode(iVisited.getIterNode());
 		visitNode(iVisited.getBodyNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitGlobalAsgnNode(org.jruby.ast.GlobalAsgnNode)
 	 */
 	public Instruction visitGlobalAsgnNode(GlobalAsgnNode iVisited) {
 		handleNode(iVisited);
 
 		String name = iVisited.getName();
 		RubyGlobal global = new RubyGlobal(script, name);
 
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		ISourcePosition pos = iVisited.getPosition();
 		setTokenRange(pos, info, name);
 		// TODO Set more info!
 
 		scriptInfo.addChild(global);
 
 		newElements.put(global, info);
 
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/**
 	 * @param pos
 	 * @param info
 	 */
 	private void setTokenRange(ISourcePosition pos, RubyFieldElementInfo info,
 			String name) {
 		int realEnd = pos.getStartOffset() + name.length() - 1;
 		info.setNameSourceStart(pos.getStartOffset());
 		info.setNameSourceEnd(realEnd);
 		info.setSourceRangeStart(pos.getStartOffset());
 		info.setSourceRangeEnd(realEnd);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitGlobalVarNode(org.jruby.ast.GlobalVarNode)
 	 */
 	public Instruction visitGlobalVarNode(GlobalVarNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitHashNode(org.jruby.ast.HashNode)
 	 */
 	public Instruction visitHashNode(HashNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getListNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitInstAsgnNode(org.jruby.ast.InstAsgnNode)
 	 */
 	public Instruction visitInstAsgnNode(InstAsgnNode iVisited) {
 		handleNode(iVisited);
 
 		String name = iVisited.getName();
 		RubyElement type = getCurrentType();
 		RubyInstVar var = new RubyInstVar(type, name);
 
 		RubyElementInfo parentInfo = getCurrentTypeInfo();
 		parentInfo.addChild(var);
 
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		// TODO Add more information to the info object!
 		ISourcePosition pos = iVisited.getPosition();
 		setTokenRange(pos, info, name);
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 
 		newElements.put(var, info);
 
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/**
 	 * @param value
 	 * @return
 	 */
 	private String estimateValueType(Node value) {
 		if (value instanceof CallNode) {
 			CallNode call = (CallNode) value;
 			if (call.getName().equals("new")) {
 				Node receiver = call.getReceiverNode();
 				if (receiver instanceof ConstNode) {
 					ConstNode constNode = (ConstNode) receiver;
 					return constNode.getName();
 				}
 			}
 		} else if (value instanceof DStrNode) {
 			return "String";
 
 		} else if (value instanceof FixnumNode) {
 			return "Fixnum";
 
 		} else if (value instanceof BignumNode) {
 			return "Bignum";
 		}
 		return "Object";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitInstVarNode(org.jruby.ast.InstVarNode)
 	 */
 	public Instruction visitInstVarNode(InstVarNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitIfNode(org.jruby.ast.IfNode)
 	 */
 	public Instruction visitIfNode(IfNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getCondition());
 		visitNode(iVisited.getThenBody());
 		visitNode(iVisited.getElseBody());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitIterNode(org.jruby.ast.IterNode)
 	 */
 	public Instruction visitIterNode(IterNode iVisited) {
 		handleNode(iVisited);
 
 		RubyBlock block = new RubyBlock(modelStack.peek());
 		modelStack.push(block);
 
 		visitNode(iVisited.getVarNode());
 		visitNode(iVisited.getBodyNode());
 		if (DEBUG)
 			System.out.println("Iter Node ended");
 
 		modelStack.pop();
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitLocalAsgnNode(org.jruby.ast.LocalAsgnNode)
 	 */
 	public Instruction visitLocalAsgnNode(LocalAsgnNode iVisited) {
 		handleNode(iVisited);
 
 		int start = iVisited.getPosition().getStartOffset()
 				- iVisited.getName().length() + 1;
 		int end = start + iVisited.getName().length();
 		RubyLocalVar var = new RubyLocalVar(modelStack.peek(), iVisited
 				.getName(), start, end);
 		modelStack.push(var);
 
 		RubyElementInfo parentInfo = infoStack.peek();
 		parentInfo.addChild(var);
 
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 
 		ISourcePosition pos = iVisited.getPosition();
 		setTokenRange(pos, info, iVisited.getName());
 		infoStack.push(info);
 
 		newElements.put(var, info);
 
 		visitNode(iVisited.getValueNode());
 
 		modelStack.pop();
 		infoStack.pop();
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitLocalVarNode(org.jruby.ast.LocalVarNode)
 	 */
 	public Instruction visitLocalVarNode(LocalVarNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitMultipleAsgnNode(org.jruby.ast.MultipleAsgnNode)
 	 */
 	public Instruction visitMultipleAsgnNode(MultipleAsgnNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getHeadNode());
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitMatch2Node(org.jruby.ast.Match2Node)
 	 */
 	public Instruction visitMatch2Node(Match2Node iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitMatch3Node(org.jruby.ast.Match3Node)
 	 */
 	public Instruction visitMatch3Node(Match3Node iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitMatchNode(org.jruby.ast.MatchNode)
 	 */
 	public Instruction visitMatchNode(MatchNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getRegexpNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitModuleNode(org.jruby.ast.ModuleNode)
 	 */
 	public Instruction visitModuleNode(ModuleNode iVisited) {
 		handleNode(iVisited);
 		String name = getFullyQualifiedName(iVisited.getCPath());
 		RubyModule module = new RubyModule(modelStack.peek(), name);
		RubyElement parent = modelStack.peek();
		RubyType existing = (RubyType) findChild(parent, IRubyElement.TYPE, name);
		if (existing != null) {
		// FIXME Should we just increment the occurence count like I do here, or should we conglomerate the types into one LogicalType?
			module.occurrenceCount = existing.occurrenceCount + 1;
		}
 		modelStack.push(module);
 
 		RubyElementInfo parentInfo = infoStack.peek();
 		parentInfo.addChild(module);
 
 		RubyTypeElementInfo info = new RubyTypeElementInfo();
 		info.setHandle(module);
 		ISourcePosition pos = iVisited.getPosition();
 		setKeywordRange("module", pos, info, name);
 		info.setSuperclassName("Module");
 		// TODO Set more info!
 		infoStack.push(info);
 
 		newElements.put(module, info);
 
 		visitNode(iVisited.getBodyNode());
 
 		modelStack.pop();
 		infoStack.pop();
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitNewlineNode(org.jruby.ast.NewlineNode)
 	 */
 	public Instruction visitNewlineNode(NewlineNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getNextNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitNextNode(org.jruby.ast.NextNode)
 	 */
 	public Instruction visitNextNode(NextNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitNilNode(org.jruby.ast.NilNode)
 	 */
 	public Instruction visitNilNode(NilNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitNotNode(org.jruby.ast.NotNode)
 	 */
 	public Instruction visitNotNode(NotNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getConditionNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitNthRefNode(org.jruby.ast.NthRefNode)
 	 */
 	public Instruction visitNthRefNode(NthRefNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpElementAsgnNode(org.jruby.ast.OpElementAsgnNode)
 	 */
 	public Instruction visitOpElementAsgnNode(OpElementAsgnNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpAsgnNode(org.jruby.ast.OpAsgnNode)
 	 */
 	public Instruction visitOpAsgnNode(OpAsgnNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpAsgnAndNode(org.jruby.ast.OpAsgnAndNode)
 	 */
 	public Instruction visitOpAsgnAndNode(OpAsgnAndNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getSecondNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpAsgnOrNode(org.jruby.ast.OpAsgnOrNode)
 	 */
 	public Instruction visitOpAsgnOrNode(OpAsgnOrNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getSecondNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOptNNode(org.jruby.ast.OptNNode)
 	 */
 	public Instruction visitOptNNode(OptNNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBodyNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOrNode(org.jruby.ast.OrNode)
 	 */
 	public Instruction visitOrNode(OrNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getSecondNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitPostExeNode(org.jruby.ast.PostExeNode)
 	 */
 	public Instruction visitPostExeNode(PostExeNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitRedoNode(org.jruby.ast.RedoNode)
 	 */
 	public Instruction visitRedoNode(RedoNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitRegexpNode(org.jruby.ast.RegexpNode)
 	 */
 	public Instruction visitRegexpNode(RegexpNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitRescueBodyNode(org.jruby.ast.RescueBodyNode)
 	 */
 	public Instruction visitRescueBodyNode(RescueBodyNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getExceptionNodes());
 		visitNode(iVisited.getOptRescueNode());
 		visitNode(iVisited.getBodyNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitRescueNode(org.jruby.ast.RescueNode)
 	 */
 	public Instruction visitRescueNode(RescueNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getRescueNode());
 		visitNode(iVisited.getBodyNode());
 		visitNode(iVisited.getElseNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitRetryNode(org.jruby.ast.RetryNode)
 	 */
 	public Instruction visitRetryNode(RetryNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitReturnNode(org.jruby.ast.ReturnNode)
 	 */
 	public Instruction visitReturnNode(ReturnNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValueNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSClassNode(org.jruby.ast.SClassNode)
 	 */
 	public Instruction visitSClassNode(SClassNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getBodyNode());
 		return null;
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSelfNode(org.jruby.ast.SelfNode)
 	 */
 	public Instruction visitSelfNode(SelfNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSplatNode(org.jruby.ast.SplatNode)
 	 */
 	public Instruction visitSplatNode(SplatNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValue());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitStrNode(org.jruby.ast.StrNode)
 	 */
 	public Instruction visitStrNode(StrNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSuperNode(org.jruby.ast.SuperNode)
 	 */
 	public Instruction visitSuperNode(SuperNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getArgsNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSValueNode(org.jruby.ast.SValueNode)
 	 */
 	public Instruction visitSValueNode(SValueNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValue());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSymbolNode(org.jruby.ast.SymbolNode)
 	 */
 	public Instruction visitSymbolNode(SymbolNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitToAryNode(org.jruby.ast.ToAryNode)
 	 */
 	public Instruction visitToAryNode(ToAryNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValue());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitTrueNode(org.jruby.ast.TrueNode)
 	 */
 	public Instruction visitTrueNode(TrueNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitUndefNode(org.jruby.ast.UndefNode)
 	 */
 	public Instruction visitUndefNode(UndefNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitUntilNode(org.jruby.ast.UntilNode)
 	 */
 	public Instruction visitUntilNode(UntilNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getConditionNode());
 		visitNode(iVisited.getBodyNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitVAliasNode(org.jruby.ast.VAliasNode)
 	 */
 	public Instruction visitVAliasNode(VAliasNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitVCallNode(org.jruby.ast.VCallNode)
 	 */
 	public Instruction visitVCallNode(VCallNode iVisited) {
 		handleNode(iVisited);
 		// XXX If the call has arguments, we need to find the method athcing he symbols and mark their visibility differently
 		String functionName = iVisited.getName();
 		if (functionName.equals("public")) {
 			currentVisibility = Visibility.PUBLIC;
 		} else if (functionName.equals("private")) {
 			currentVisibility = Visibility.PRIVATE;
 		} else if (functionName.equals("protected")) {
 			currentVisibility = Visibility.PROTECTED;
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitWhenNode(org.jruby.ast.WhenNode)
 	 */
 	public Instruction visitWhenNode(WhenNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getExpressionNodes());
 		visitNode(iVisited.getBodyNode());
 		visitNode(iVisited.getNextCase());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitWhileNode(org.jruby.ast.WhileNode)
 	 */
 	public Instruction visitWhileNode(WhileNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getConditionNode());
 		visitNode(iVisited.getBodyNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitXStrNode(org.jruby.ast.XStrNode)
 	 */
 	public Instruction visitXStrNode(XStrNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitYieldNode(org.jruby.ast.YieldNode)
 	 */
 	public Instruction visitYieldNode(YieldNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getArgsNode());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitZArrayNode(org.jruby.ast.ZArrayNode)
 	 */
 	public Instruction visitZArrayNode(ZArrayNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitZSuperNode(org.jruby.ast.ZSuperNode)
 	 */
 	public Instruction visitZSuperNode(ZSuperNode iVisited) {
 		handleNode(iVisited);
 		return null;
 	}
 
 	private Instruction visitNode(Node iVisited) {
 		if (iVisited != null)
 			iVisited.accept(this);
 		return null;
 	}
 
 	/**
 	 * @param visited
 	 */
 	private Instruction handleNode(Node visited) {
 		// Uncomment for logging
 		if (DEBUG)
 		System.out.println(visited.toString() + ", position -> "
 				+ visited.getPosition());
 		return null;
 	}
 
 	public Instruction visitArgsPushNode(ArgsPushNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getSecondNode());
 		return null;
 	}
 
 	public Instruction visitAttrAssignNode(AttrAssignNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getArgsNode());
 		return null;
 	}
 
 	public Instruction visitRootNode(RootNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBodyNode());
 		return null;
 	}
 
 }
