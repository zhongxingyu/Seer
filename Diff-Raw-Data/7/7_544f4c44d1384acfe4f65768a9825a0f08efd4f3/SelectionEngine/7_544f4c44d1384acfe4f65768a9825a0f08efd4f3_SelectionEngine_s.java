 package org.rubypeople.rdt.internal.codeassist;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.jruby.ast.ArgumentNode;
 import org.jruby.ast.CallNode;
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.ClassVarAsgnNode;
 import org.jruby.ast.ClassVarDeclNode;
 import org.jruby.ast.ClassVarNode;
 import org.jruby.ast.Colon2Node;
 import org.jruby.ast.ConstDeclNode;
 import org.jruby.ast.ConstNode;
 import org.jruby.ast.DAsgnNode;
 import org.jruby.ast.DVarNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.DefsNode;
 import org.jruby.ast.FCallNode;
 import org.jruby.ast.InstAsgnNode;
 import org.jruby.ast.InstVarNode;
 import org.jruby.ast.LocalAsgnNode;
 import org.jruby.ast.LocalVarNode;
 import org.jruby.ast.ModuleNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.VCallNode;
 import org.jruby.ast.types.INameNode;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.rdt.core.IMethod;
 import org.rubypeople.rdt.core.IParent;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.IType;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.core.search.IRubySearchConstants;
 import org.rubypeople.rdt.core.search.IRubySearchScope;
 import org.rubypeople.rdt.core.search.SearchEngine;
 import org.rubypeople.rdt.core.search.SearchMatch;
 import org.rubypeople.rdt.core.search.SearchParticipant;
 import org.rubypeople.rdt.core.search.SearchPattern;
 import org.rubypeople.rdt.internal.core.RubyScript;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 import org.rubypeople.rdt.internal.core.search.BasicSearchEngine;
 import org.rubypeople.rdt.internal.core.search.CollectingSearchRequestor;
 import org.rubypeople.rdt.internal.core.util.ASTUtil;
 import org.rubypeople.rdt.internal.core.util.Util;
 import org.rubypeople.rdt.internal.ti.DefaultTypeInferrer;
 import org.rubypeople.rdt.internal.ti.ITypeGuess;
 import org.rubypeople.rdt.internal.ti.ITypeInferrer;
 import org.rubypeople.rdt.internal.ti.util.ClosestSpanningNodeLocator;
 import org.rubypeople.rdt.internal.ti.util.FirstPrecursorNodeLocator;
 import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;
 import org.rubypeople.rdt.internal.ti.util.OffsetNodeLocator;
 
 import com.sun.corba.se.impl.io.FVDCodeBaseImpl;
 
 public class SelectionEngine {
 
 	private HashSet<IType> fVisitedTypes;
 
 	public IRubyElement[] select(IRubyScript script, int start, int end)
 			throws RubyModelException {
 		String source = script.getSource();
 		Node root;
 		try {
 			RubyParser parser = new RubyParser();
 			root = parser.parse((IFile) script.getResource(),
 					new StringReader(source));
 		} catch (SyntaxException e) {
 			root = ((RubyScript)script).lastGoodAST;
 		}
 		Node selected = OffsetNodeLocator.Instance().getNodeAtOffset(root,
 				start);
 
 		if (selected instanceof Colon2Node) {
 			String simpleName = ((Colon2Node)selected).getName();
 			String fullyQualifiedName = ASTUtil.getFullyQualifiedName((Colon2Node) selected);
 			IRubyElement element = findChild(simpleName, IRubyElement.TYPE, script);
 			if (element != null && Util.parentsMatch((IType)element, fullyQualifiedName)) {
 			  return new IRubyElement[] { element };	
 			}
 			RubyElementRequestor completer = new RubyElementRequestor(script);
 			return completer.findType(fullyQualifiedName);
 		} 
 		if (selected instanceof DVarNode) {
 			final String name = ((DVarNode) selected).getName();
 			Node assignment = FirstPrecursorNodeLocator.Instance().findFirstPrecursor(root, start, new INodeAcceptor() {
 			
 				public boolean doesAccept(Node node) {
 					// TODO Auto-generated method stub
 					return (node instanceof DAsgnNode) && ((DAsgnNode) node).getName().equals(name);
 				}
 			
 			});
 			return new IRubyElement[] { script.getElementAt(assignment.getPosition().getStartOffset()) };
 		}
 		if (selected instanceof ConstNode) {			
 			ConstNode constNode = (ConstNode) selected;
 			String name = constNode.getName();
 			// Try to find a matching constant in this script
 			// TODO Use convention of all caps versus camelcase to decided which to search for first?
 			IRubyElement element = findChild(name, IRubyElement.CONSTANT, script);
 			if (element != null) {
 			  return new IRubyElement[] { element };	
 			}
 			// Now search for a type in this script
 			element = findChild(name, IRubyElement.TYPE, script);
 			if (element != null) {
 			  return new IRubyElement[] { element };	
 			}
 			RubyElementRequestor completer = new RubyElementRequestor(script);
 			return completer.findType(name);
 		}
 		if (isLocalVarRef(selected)) {
 			// TODO Try the local namespace first!			
 			List<IRubyElement> possible = getChildrenWithName(script
 					.getChildren(), IRubyElement.LOCAL_VARIABLE,
 					getName(selected));
 			return possible.toArray(new IRubyElement[possible.size()]);
 		}
 		if (isInstanceVarRef(selected)) {
 			List<IRubyElement> possible = getChildrenWithName(script
 					.getChildren(), IRubyElement.INSTANCE_VAR,
 					getName(selected));
 			return possible.toArray(new IRubyElement[possible.size()]);
 		}
 		if (isClassVarRef(selected)) {
 			List<IRubyElement> possible = getChildrenWithName(script
 					.getChildren(), IRubyElement.CLASS_VAR, getName(selected));
 			return possible.toArray(new IRubyElement[possible.size()]);
 		}
 		// We're already on the declaration, just return it
 		if ((selected instanceof DefnNode) || (selected instanceof DefsNode) ||
 				(selected instanceof ConstDeclNode) || (selected instanceof ClassNode) || 
 				(selected instanceof ModuleNode) || (selected instanceof ClassVarDeclNode)) {
 			IRubyElement element = ((RubyScript)script).getElementAt(start);
 			return new IRubyElement[] {element};
 		}
 		if (isMethodCall(selected)) {
 			String methodName = getName(selected);
 			Set<IRubyElement> possible = new HashSet<IRubyElement>();
 			IType[] types = getReceiver(script, source, selected, root, start);
 			for (int i = 0; i < types.length; i++) {
 				IType type = types[i];
 				if (fVisitedTypes == null) { fVisitedTypes = new HashSet<IType>(); } // keep track of types so we don't get into infinite loop
 				Collection<IMethod> methods = suggestMethods(type);
 				fVisitedTypes.clear();
 				for (IMethod method : methods) {
 					if (method.getElementName().equals(methodName))
 						possible.add(method);
 				}
 			}		
 			return possible.toArray(new IRubyElement[possible.size()]);
 		}
 		return new IRubyElement[0];
 	}
 	
 	private IType[] getReceiver(IRubyScript script, String source, Node selected, Node root, int start) {
 		List<IType> types = new ArrayList<IType>();
 		if ((selected instanceof FCallNode) || (selected instanceof VCallNode)) {
 			Node receiver = null;
 			IRubySearchScope scope = null;
 
 				receiver = ClosestSpanningNodeLocator.Instance()
 				.findClosestSpanner(root, start, new INodeAcceptor() {
 					public boolean doesAccept(Node node) {
 						return (node instanceof ClassNode || node instanceof ModuleNode);
 					}
 				});
 				scope = SearchEngine.createRubySearchScope(new IRubyElement[] { script });
 			
 			
 			String typeName = ASTUtil.getNameReflectively(receiver);
 			CollectingSearchRequestor requestor = new CollectingSearchRequestor();
 			SearchPattern pattern = SearchPattern.createPattern(
 					IRubyElement.TYPE, typeName,
 					IRubySearchConstants.DECLARATIONS,
 					SearchPattern.R_EXACT_MATCH);
 			SearchParticipant[] participants = { BasicSearchEngine.getDefaultSearchParticipant() };
 			try {
 				new BasicSearchEngine().search(pattern, participants, scope, requestor, null);
 			} catch (CoreException e) {
 				RubyCore.log(e);
 			}
 			List<SearchMatch> matches = requestor.getResults();
 			if (matches == null || matches.isEmpty()) return new IType[0]; // TODO Check up the type hierarchy!
 			for (SearchMatch match : matches) {
 				types.add((IType) match.getElement());
 			}
 		} else {
 			ITypeInferrer inferrer = new DefaultTypeInferrer();
 			List<ITypeGuess> guesses = inferrer.infer(source, start);
 			// TODO If guesses are empty, just do a global search for this method?
 			if (guesses.isEmpty()) {
 				String methodName = ASTUtil.getNameReflectively(selected);
 				IRubySearchScope scope = SearchEngine.createRubySearchScope(new IRubyElement[] { script.getRubyProject() });
 				CollectingSearchRequestor requestor = new CollectingSearchRequestor();
 				SearchPattern pattern = SearchPattern.createPattern(
 						IRubyElement.METHOD, methodName,
 						IRubySearchConstants.DECLARATIONS,
 						SearchPattern.R_EXACT_MATCH);
 				SearchParticipant[] participants = { BasicSearchEngine.getDefaultSearchParticipant() };
 				try {
 					new BasicSearchEngine().search(pattern, participants, scope, requestor, null);
 				} catch (CoreException e) {
 					RubyCore.log(e);
 				}
 				List<SearchMatch> matches = requestor.getResults();
 				if (matches == null || matches.isEmpty()) return new IType[0];
 				for (SearchMatch match : matches) {
 					IMethod method = (IMethod) match.getElement();
 					types.add(method.getDeclaringType());
 				}
 			} else {
 			RubyElementRequestor requestor = new RubyElementRequestor(
 					script);
 			for (ITypeGuess guess : guesses) {
 				String name = guess.getType();
 				IType[] tmpTypes = requestor.findType(name);
 				for (int i = 0; i < tmpTypes.length; i++) {
 					types.add(tmpTypes[i]);
 				}
 			}
 			}
 		}
 		return types.toArray(new IType[types.size()]);
 	}
 
 	// TODO This is all copy-pasted and modified from CompletionEngine. Move this out into a util class and call it from both.
 	private Collection<IMethod> suggestMethods(IType type) throws RubyModelException {
 		List<IMethod> proposals = new ArrayList<IMethod>();
 		if (type == null) return proposals;		
 		if (fVisitedTypes.contains(type)) return proposals;
 		fVisitedTypes.add(type);
 		IMethod[] methods = type.getMethods();
 		for (int k = 0; k < methods.length; k++) {
 			proposals.add(methods[k]);
 		}		
 		proposals.addAll(addModuleMethods(type)); // Decrement confidence by one as a hack to make sure as we move up the inheritance chain we suggest "closer" parents methods first
 		if (!type.isModule()) proposals.addAll(addSuperClassMethods(type));		
 		return proposals;
 	}
 
 	private Collection<IMethod> addModuleMethods(IType type) {
 		List<IMethod> proposals = new ArrayList<IMethod>();
 		String[] modules = null;
 		try {
 			modules = type.getIncludedModuleNames();
 		} catch (RubyModelException e) {
 			// ignore
 		}
 		if (modules == null || modules.length == 0) return proposals;
 		RubyElementRequestor requestor = new RubyElementRequestor(type.getRubyScript());
 		for (int i = 0; i < modules.length; i++) {			
 			IType[] moduleTypes = requestor.findType(modules[i]);
 			for (int j = 0; j < moduleTypes.length; j++) {
 				try {
 					IType moduleType = moduleTypes[j];
 					proposals.addAll(suggestMethods(moduleType));
 				} catch (RubyModelException e) {
 					// ignore
 				}
 			}
 		}
 		return proposals;
 	}
 	
 	private Collection<IMethod> addSuperClassMethods(IType type) throws RubyModelException {
 		List<IMethod> proposals = new ArrayList<IMethod>();
 		String superClass = type.getSuperclassName();
 		if (superClass == null) return proposals;
 		RubyElementRequestor requestor = new RubyElementRequestor(type.getRubyScript());
 		IType[] supers = requestor.findType(superClass);
 		for (int i = 0; i < supers.length; i++) {
 			IType superType = supers[i];
 			if (superType.equals(type)) continue;
 			proposals.addAll(suggestMethods(superType));
 		}
 		return proposals;
 	}
 
 	private IRubyElement findChild(String name, int type, IParent parent) {
 		try {
 			IRubyElement[] children = parent.getChildren();
 			for (int j = 0; j < children.length; j++) {
 				IRubyElement child = children[j];
 				if (child.getElementName().equals(name) && child.isType(type))
 				  return child;
 			    if (child instanceof IParent) {
 			    	IRubyElement found = findChild(name, type, (IParent) child);
 			    	if (found != null) return found;
 			    }
 			}
 		} catch (RubyModelException e) {
 			RubyCore.log(e);
 		}
 		return null;
 	}
 
 	private boolean isMethodCall(Node selected) {
 		return (selected instanceof VCallNode) || (selected instanceof FCallNode) || (selected instanceof CallNode);
 	}
 
 	private List<IRubyElement> getChildrenWithName(IRubyElement[] children,
 			int type, String name) throws RubyModelException {
 		List<IRubyElement> possible = new ArrayList<IRubyElement>();
 		for (int i = 0; i < children.length; i++) {
 			IRubyElement child = children[i];
 			if (child.getElementType() == type) {
 				if (child.getElementName().equals(name))
 					possible.add(child);
 			}
 			if (child instanceof IParent) {
 				possible.addAll(getChildrenWithName(((IParent) child)
 						.getChildren(), type, name));
 			}
 		}
 		return possible;
 	}
 
 	private String getName(Node node) {
 		if (node instanceof INameNode) {
 			return ((INameNode) node).getName();
 		}
 		if (node instanceof ClassVarNode) {
 			return ((ClassVarNode) node).getName();
 		}
 		return "";
 	}
 
 	private boolean isInstanceVarRef(Node node) {
 		return ((node instanceof InstAsgnNode) || (node instanceof InstVarNode));
 	}
 
 	private boolean isClassVarRef(Node node) {
 		return ((node instanceof ClassVarAsgnNode) || (node instanceof ClassVarNode));
 	}
 
 	private boolean isLocalVarRef(Node node) {
 		return ((node instanceof LocalAsgnNode)
 				|| (node instanceof ArgumentNode) || (node instanceof LocalVarNode));
 	}
 }
