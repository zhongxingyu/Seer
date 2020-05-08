 package org.rubypeople.rdt.internal.codeassist;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.ClassVarAsgnNode;
 import org.jruby.ast.ClassVarDeclNode;
 import org.jruby.ast.ClassVarNode;
 import org.jruby.ast.Colon2Node;
 import org.jruby.ast.ConstDeclNode;
 import org.jruby.ast.ConstNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.DefsNode;
 import org.jruby.ast.InstAsgnNode;
 import org.jruby.ast.InstVarNode;
 import org.jruby.ast.MethodDefNode;
 import org.jruby.ast.ModuleNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.RootNode;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.jruby.parser.StaticScope;
 import org.rubypeople.rdt.core.CompletionProposal;
 import org.rubypeople.rdt.core.CompletionRequestor;
 import org.rubypeople.rdt.core.Flags;
 import org.rubypeople.rdt.core.IMember;
 import org.rubypeople.rdt.core.IMethod;
 import org.rubypeople.rdt.core.IOpenable;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyModel;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.ISourceRange;
 import org.rubypeople.rdt.core.IType;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.core.search.IRubySearchConstants;
 import org.rubypeople.rdt.core.search.IRubySearchScope;
 import org.rubypeople.rdt.core.search.SearchMatch;
 import org.rubypeople.rdt.core.search.SearchParticipant;
 import org.rubypeople.rdt.core.search.SearchPattern;
 import org.rubypeople.rdt.internal.core.RubyElement;
 import org.rubypeople.rdt.internal.core.RubyScript;
 import org.rubypeople.rdt.internal.core.RubyType;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 import org.rubypeople.rdt.internal.core.search.BasicSearchEngine;
 import org.rubypeople.rdt.internal.core.search.CollectingSearchRequestor;
 import org.rubypeople.rdt.internal.core.util.ASTUtil;
 import org.rubypeople.rdt.internal.core.util.Util;
 import org.rubypeople.rdt.internal.ti.BasicTypeGuess;
 import org.rubypeople.rdt.internal.ti.DefaultTypeInferrer;
 import org.rubypeople.rdt.internal.ti.ITypeGuess;
 import org.rubypeople.rdt.internal.ti.ITypeInferrer;
 import org.rubypeople.rdt.internal.ti.util.AttributeLocator;
 import org.rubypeople.rdt.internal.ti.util.ClosestSpanningNodeLocator;
 import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;
 import org.rubypeople.rdt.internal.ti.util.ScopedNodeLocator;
 
 public class CompletionEngine {
 	private static final String OBJECT = "Object";
 	private static final String CONSTRUCTOR_INVOKE_NAME = "new";
 	private static final String CONSTRUCTOR_DEFINITION_NAME = "initialize";
 	
 	private CompletionRequestor fRequestor;
 	private CompletionContext fContext;
 	private Set<IType> fVisitedTypes;
 
 	public CompletionEngine(CompletionRequestor requestor) {
 		this.fRequestor = requestor;
 	}
 
 	public void complete(IRubyScript script, int offset) throws RubyModelException {
 		this.fRequestor.beginReporting();		
 		fContext = new CompletionContext(script, offset);
 		if (fContext.emptyPrefix()) { // no prefix, so we could suggest anything
 			suggestMethodsForEnclosingType(script);
 			getDocumentsRubyElementsInScope();
 		} else {
 			if (fContext.isDoubleSemiColon()) {				
 				String prefix = fContext.getFullPrefix();
 				String typeName = prefix.substring(0, prefix.lastIndexOf("::"));
 				RubyElementRequestor requestor = new RubyElementRequestor(script);
 				IType[] types = requestor.findType(typeName);
 				Map<String, CompletionProposal> proposals = new HashMap<String, CompletionProposal>();
 				for (int i = 0; i < types.length; i++) {
 					IType type = types[i];
 					proposals.putAll(suggestTypesConstants(type));
 					// Suggest nested types
 					proposals.putAll(suggestNestedTypes(type));
 					// Suggest class level methods
 					proposals.putAll(suggestMethods(100, type, false));
 				}
 				List<CompletionProposal> list = new ArrayList<CompletionProposal>(proposals.values());
 				Collections.sort(list, new CompletionProposalComparator());
 				for (CompletionProposal proposal : list) {
 					if (proposal.getCompletion().startsWith(fContext.getPartialPrefix()))
 						fRequestor.accept(proposal);
 				}				
 			}
 			if (fContext.isConstant()) { // type or constant
 				suggestTypeNames();
 				suggestConstantNames();
 				return;
 			} 
 			if (fContext.isExplicitMethodInvokation()) {
 				ITypeInferrer inferrer = new DefaultTypeInferrer();
 				List<ITypeGuess> guesses = inferrer.infer(fContext.getCorrectedSource(), fContext.getOffset());
 				if (guesses.isEmpty()) {
 					guesses.add(new BasicTypeGuess(OBJECT, 100));
 				}
 				List<CompletionProposal> list = new ArrayList<CompletionProposal>();
 				RubyElementRequestor requestor = new RubyElementRequestor(script);
 				for (ITypeGuess guess : guesses) {
 					final String name = guess.getType();
 					if (fContext.isBroken()) {
 						Node rootNode = new RubyParser().parse(fContext.getCorrectedSource());
 						List<Node> typeNodes = ScopedNodeLocator.Instance().findNodesInScope(rootNode, new INodeAcceptor() {
 						
 							public boolean doesAccept(Node node) {
 								if ((node instanceof ClassNode) || (node instanceof ModuleNode)) {
 									return ASTUtil.getNameReflectively(node).equals(name);
 								}
 								return false;
 							}
 						
 						});
 						for (Node typeNode : typeNodes) {
 							List<Node> methods = ScopedNodeLocator.Instance().findNodesInScope(typeNode, new INodeAcceptor() {
 								
 								public boolean doesAccept(Node node) {
 									return (node instanceof DefnNode) || (node instanceof DefsNode);
 								}
 							
 							});
 							for (Node methodNode : methods) {
 								MethodDefNode methodDef = (MethodDefNode) methodNode;
 								NodeMethod method = new NodeMethod(methodDef);
 								list.add(suggestMethod(method, name, 100));
 							}
 						}
 					}					
 					IType[] types = requestor.findType(name);
 					for (int i = 0; i < types.length; i++) {
						Map<String, CompletionProposal> map = doSuggestMethods(guess.getConfidence(), types[i], true);
 						list.addAll(map.values());						
 					}
 				}
 				list.addAll(suggestAllMethodsMatchingPrefix(script));
 				Collections.sort(list, new CompletionProposalComparator());
 				for (CompletionProposal proposal : list) {
 					fRequestor.accept(proposal);
 				}				
 			} else {
 				// FIXME If we're invoked on the class declaration (it's super class) don't do this!
 				// FIXME Traverse the IRubyElement model, not nodes (and don't reparse)?
 				if (fContext.isMethodInvokationOrLocal()) {
 					suggestMethodsForEnclosingType(script);				
 				}
 				getDocumentsRubyElementsInScope();
 			}
 			if (fContext.isGlobal()) { // looks like a global
 				suggestGlobals();
 			}
 		}
 		this.fRequestor.endReporting();
 		fContext = null;
 	}
 
 	private Map<String, CompletionProposal> suggestTypesConstants(IType type) throws RubyModelException {
 		Map<String, CompletionProposal> proposals = new HashMap<String, CompletionProposal>();
 		SearchPattern pattern = SearchPattern.createPattern(IRubyElement.CONSTANT, "*", IRubySearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
 		IRubySearchScope scope = BasicSearchEngine.createRubySearchScope(new IRubyElement[] {type});
 		List<SearchMatch> results = search(pattern, scope);
 		for (SearchMatch match: results) {
 			IRubyElement element = (IRubyElement) match.getElement();
 			if (element.getElementType() != IRubyElement.CONSTANT) continue; // XXX we shouldn't have to do this
 			// Add proposal
 			CompletionProposal proposal = createProposal(fContext.getReplaceStart(), CompletionProposal.FIELD_REF, element.getElementName());
 			proposal.setType(type.getFullyQualifiedName());
 			proposal.setName(element.getElementName());
 			proposals.put(element.getElementName(), proposal);
 		}
 		return proposals;
 	}
 	
 	private Map<String, CompletionProposal> suggestNestedTypes(IType type) throws RubyModelException {
 		Map<String, CompletionProposal> proposals = new HashMap<String, CompletionProposal>();
 		SearchPattern pattern = SearchPattern.createPattern(IRubyElement.TYPE, "*", IRubySearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
 		IRubySearchScope scope = BasicSearchEngine.createRubySearchScope(new IRubyElement[] {type});
 		List<SearchMatch> results = search(pattern, scope);
 		for (SearchMatch match: results) {
 			IType aType = (IType) match.getElement();
 			String fullname = aType.getFullyQualifiedName();
 			if (fullname.equals(type.getFullyQualifiedName())) continue; // don't return exact match to prefix
 			if (!fullname.startsWith(type.getFullyQualifiedName())) continue; // only return those nested underneath prefix
 			String[] parts = Util.getTypeNameParts(fullname);
 //			 Don't add if it's not the directly nested child (and is instead the grandchild)
 			if (parts.length != Util.getTypeNameParts(type.getFullyQualifiedName()).length + 1) continue;
 			// Add proposal						
 			CompletionProposal proposal = createProposal(fContext.getReplaceStart(), CompletionProposal.TYPE_REF, aType.getElementName());
 			proposal.setType(aType.getFullyQualifiedName());
 			proposal.setName(aType.getElementName());
 			proposals.put(aType.getElementName(), proposal);
 		}
 		return proposals;
 	}
 
 	private List<CompletionProposal> suggestAllMethodsMatchingPrefix(IRubyScript script) {
 		List< CompletionProposal> list = new ArrayList<CompletionProposal>();
 		if (fContext.getPartialPrefix() == null || fContext.getPartialPrefix().trim().length() == 0) return list;
 		IRubySearchScope scope = BasicSearchEngine.createRubySearchScope(new IRubyElement[] {script.getRubyProject()});
 		SearchParticipant participant = BasicSearchEngine.getDefaultSearchParticipant();
 		CollectingSearchRequestor searchRequestor = new CollectingSearchRequestor();
 		SearchPattern pattern = SearchPattern.createPattern(IRubyElement.METHOD, fContext.getPartialPrefix(), IRubySearchConstants.DECLARATIONS, SearchPattern.R_PREFIX_MATCH);
 		try {
 			new BasicSearchEngine().search(pattern, new SearchParticipant[] {participant}, scope, searchRequestor, null);
 		} catch (CoreException e) {
 			RubyCore.log(e);
 		}
 		List<SearchMatch> matches = searchRequestor.getResults();
 		for (SearchMatch match : matches) {
 			IMethod element = (IMethod) match.getElement();
 			IType type = element.getDeclaringType();
 			String typeName = "";
 			if (type != null)
 				typeName = type.getElementName();
 			CompletionProposal proposal = suggestMethod(element, typeName, 100); // TODO Base confidence on accuracy in match?
 		    if (proposal != null) {
 		    	list.add(proposal);
 		    }
 		}
 		return list;
 	}
 
 	private void suggestMethodsForEnclosingType(IRubyScript script) throws RubyModelException {
 		IMember element = (IMember) script.getElementAt(fContext.getOffset());
 		IType type = null;
 		if (element == null) {
 			// We're in the top level, so we're in "Object"
 		    RubyElementRequestor requestor = new RubyElementRequestor(script);
 		    IType[] types = requestor.findType(OBJECT);
 		    if (types != null && types.length > 0) type = types[0];
 		} else if (element instanceof IType) {
 			type = (IType) element;
 		} else {
 			type = element.getDeclaringType();
 		}
 		if (type == null) return;
 		List<CompletionProposal> list = sort(suggestMethods(100, type, true));
 		for (CompletionProposal proposal : list) {
 			fRequestor.accept(proposal);
 		}
 	}
 	
 	/**
 	 * Wrap beginning of recursion to suggest methods for a type. We keep track of types visited so that we can avoid inifnite loops.
 	 * 
 	 * @param confidence
 	 * @param type
 	 * @param includeInstanceMethods
 	 * @return
 	 * @throws RubyModelException
 	 */
 	private Map<String, CompletionProposal> suggestMethods(int confidence, IType type, boolean includeInstanceMethods) throws RubyModelException {
 		if (fVisitedTypes == null) fVisitedTypes = new HashSet<IType>();
 		Map<String, CompletionProposal> list = doSuggestMethods(100, type, true);
 		fVisitedTypes.clear();
 		return list;
 	}
 
 	private List<CompletionProposal> sort(Map<String, CompletionProposal> proposals) {
 		List<CompletionProposal> list = new ArrayList<CompletionProposal>(proposals.values());
 		Collections.sort(list, new CompletionProposalComparator());
 		return list;
 	}
 	
 	private void suggestGlobals() {
 		SearchPattern pattern = SearchPattern.createPattern(IRubyElement.GLOBAL, "*", IRubySearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
 		IRubySearchScope scope = BasicSearchEngine.createRubySearchScope(new IRubyElement[] {fContext.getScript().getRubyProject()});
 		List<SearchMatch> results = search(pattern, scope);		
 		for (SearchMatch match: results) {
 			IRubyElement element = (IRubyElement) match.getElement();
 			String name = element.getElementName();
 			if (!fContext.prefixStartsWith(name))
 				continue;
 			CompletionProposal proposal = createProposal(fContext.getReplaceStart(), CompletionProposal.FIELD_REF, name);
 			proposal.setType(name);
 			fRequestor.accept(proposal);
 		}
 	}
 
 	private void suggestTypeNames() {	
 		SearchPattern pattern = SearchPattern.createPattern(IRubyElement.TYPE, "*", IRubySearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
 		IRubySearchScope scope = BasicSearchEngine.createRubySearchScope(new IRubyElement[] {fContext.getScript().getRubyProject()});
 		List<SearchMatch> results = search(pattern, scope);		
 		for (SearchMatch match: results) {
 			IRubyElement element = (IRubyElement) match.getElement();
 			String name = element.getElementName();
 			if (!fContext.prefixStartsWith(name))
 				continue;
 			CompletionProposal proposal = createProposal(fContext.getReplaceStart(), CompletionProposal.TYPE_REF, name);
 			proposal.setType(name);
 			fRequestor.accept(proposal);
 		}
 	}
 	
 	private List<SearchMatch> search(SearchPattern pattern, IRubySearchScope scope) {
 		BasicSearchEngine engine = new BasicSearchEngine();
 		SearchParticipant[] participants = new SearchParticipant[] { BasicSearchEngine.getDefaultSearchParticipant() };
 		CollectingSearchRequestor requestor = new CollectingSearchRequestor();
 		try {
 			engine.search(pattern, participants, scope, requestor, null);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return requestor.getResults();
 	}
 
 	private CompletionProposal createProposal(int replaceStart, int type, String name) {
 		CompletionProposal proposal = new CompletionProposal(type, name, 100);
 		proposal.setReplaceRange(replaceStart, replaceStart + name.length());
 		return proposal;
 	}
 
 	private void suggestConstantNames() {
 		SearchPattern pattern = SearchPattern.createPattern(IRubyElement.CONSTANT, "*", IRubySearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
 		IRubySearchScope scope = BasicSearchEngine.createRubySearchScope(new IRubyElement[] {fContext.getScript()});
 		List<SearchMatch> results = search(pattern, scope);
 		for (SearchMatch match: results) {
 			IRubyElement element = (IRubyElement) match.getElement();
 			String name = element.getElementName();
 			if (!fContext.prefixStartsWith(name))
 				continue;
 			CompletionProposal proposal = createProposal(fContext.getReplaceStart(), CompletionProposal.FIELD_REF, name);
 			proposal.setType(name);
 			fRequestor.accept(proposal);
 		}
 	}
 
 	private Map<String, CompletionProposal> doSuggestMethods(int confidence, IType type, boolean includeInstanceMethods) throws RubyModelException {
 		Map<String, CompletionProposal> proposals = new HashMap<String, CompletionProposal>();
 		if (type == null)
 			return proposals;		
 		if (fVisitedTypes.contains(type)) return proposals;
 		fVisitedTypes.add(type);
 		IMethod[] methods = type.getMethods();
 		for (int k = 0; k < methods.length; k++) {
 			if (!includeInstanceMethods && !methods[k].isSingleton()) {
 				continue;
 			}
 			CompletionProposal proposal = suggestMethod(methods[k], type.getElementName(), confidence);
 		    if (proposal != null && !proposals.containsKey(proposal.getName())) {
 		    	proposals.put(proposal.getName(), proposal); // If a method name matches an existing suggestion (i.e. its overriden in the subclass), don't suggest it again!
 		    }
 		}		
 		proposals.putAll(addModuleMethods(confidence - 1, type)); // Decrement confidence by one as a hack to make sure as we move up the inheritance chain we suggest "closer" parents methods first
 		if (!type.isModule()) proposals.putAll(addSuperClassMethods(confidence - 1, type, includeInstanceMethods));
 		return proposals;
 	}
 
 	private Map<String, CompletionProposal> addModuleMethods(int confidence, IType type) {
 		Map<String, CompletionProposal> proposals = new HashMap<String, CompletionProposal>();
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
 					proposals.putAll(doSuggestMethods(confidence, moduleType, true));
 				} catch (RubyModelException e) {
 					// ignore
 				}
 			}
 		}
 		return proposals;
 	}
 
 	private Map<String, CompletionProposal> addSuperClassMethods(int confidence, IType type, boolean includeInstanceMethods) throws RubyModelException {
 		Map<String, CompletionProposal> proposals = new HashMap<String, CompletionProposal>();
 		String superClass = type.getSuperclassName();
 		if (superClass == null) return proposals;
 		RubyElementRequestor requestor = new RubyElementRequestor(type.getRubyScript());
 		IType[] supers = requestor.findType(superClass);
 		for (int i = 0; i < supers.length; i++) {
 			IType superType = supers[i];
 			proposals.putAll(doSuggestMethods(confidence, superType, includeInstanceMethods));
 		}
 		return proposals;
 	}
 
 	private CompletionProposal suggestMethod(IMethod method, String typeName, int confidence) {
 		int start = fContext.getReplaceStart();
 		String name = method.getElementName();
 		int flags = Flags.AccDefault;
 		if (method.isSingleton()) {
 			flags |= Flags.AccStatic;
 			if (method.isConstructor())
 				name = CONSTRUCTOR_INVOKE_NAME;
 			else {
 				if (name.startsWith(typeName)) {
 					name = name.substring(typeName.length() + 1);
 				}
 			}
 		} else {
 			// Don't show instance methods if the thing we're working on is a class' name!
 			// FIXME We do want to show if it is a constant, but not a class name
 			if (fContext.fullPrefixIsConstant()) return null;
 		}
 		if (!fContext.prefixStartsWith(name))
 			return null;
 		
 		try {
 			switch (method.getVisibility()) {
 			case IMethod.PRIVATE:
 				flags |= Flags.AccPrivate;
 				if (fContext.hasReceiver()) return null; // can't invoke a private method on a receiver
 				break;
 			case IMethod.PUBLIC:
 				flags |= Flags.AccPublic; // FIXME Check if receiver is of same class as method's declaring type, if not, skip this method. (so we can invoke with no receiver inside same class, with explicit self as receiver, or with receiver who has same class).
 				break;
 			case IMethod.PROTECTED:
 				flags |= Flags.AccProtected;
 				break;
 			default:
 				break;
 			}
 		} catch (RubyModelException e) {
 			RubyCore.log(e);
 			flags |= Flags.AccPublic;
 		}
 		CompletionProposal proposal = new CompletionProposal(CompletionProposal.METHOD_REF, name, confidence);
 		proposal.setReplaceRange(start, start + name.length());
 		proposal.setFlags(flags);
 		proposal.setName(name);
 		IType declaringType = method.getDeclaringType();
 		String declaringName = typeName;
 		if (declaringType != null)
 			declaringName = declaringType.getFullyQualifiedName();
 		proposal.setDeclaringType(declaringName);
 		return proposal;
 	}
 
 	/**
 	 * Gets all the distinct elements in the current RubyScript
 	 * 
 	 * @param offset
 	 * @param replaceStart
 	 * 
 	 * @return a List of the names of all the elements in the current RubyScript
 	 */
 	private void getDocumentsRubyElementsInScope() {
 		try {
 			// FIXME Try to stop all the multiple re-parsing of the source! Can
 			// we parse once and pass the root node around?
 			// Parse
 			Node rootNode = ((RubyScript) fContext.getScript()).lastGoodAST;
 			if (rootNode == null) {
 				return;
 			}
 
 			// XXX Just find enclosing scope and grab variables?
 			Node enclosingNode = ClosestSpanningNodeLocator.Instance().findClosestSpanner(rootNode, fContext.getOffset(), new INodeAcceptor() {
 				public boolean doesAccept(Node node) {
 					return (node instanceof DefnNode || node instanceof DefsNode || node instanceof ClassNode || node instanceof ModuleNode || node instanceof RootNode);
 				}
 			});
 			
 			Collection<String> variables = addVariablesinScope(getScope(enclosingNode));
 			for (String variable : variables) {
 				CompletionProposal proposal = new CompletionProposal(CompletionProposal.LOCAL_VARIABLE_REF, variable, 100);
 				proposal.setReplaceRange(fContext.getReplaceStart(), fContext.getReplaceStart() + variable.length());
 				fRequestor.accept(proposal);
 			}			
 
 
 			// Find the enclosing type (class or module) to get instance and
 			// classvars from
 			Node enclosingTypeNode = ClosestSpanningNodeLocator.Instance().findClosestSpanner(rootNode, fContext.getOffset(), new INodeAcceptor() {
 				public boolean doesAccept(Node node) {
 					return (node instanceof ClassNode || node instanceof ModuleNode);
 				}
 			});
 
 			// Add members from enclosing type
 			if (enclosingTypeNode != null) {
 				getMembersAvailableInsideType(enclosingTypeNode);
 			}
 		} catch (RubyModelException rme) {
 			RubyCore.log(rme);
 			RubyCore.log("RubyModelException in CompletionEngine::getElementsInScope()");
 		} catch (SyntaxException se) {
 			RubyCore.log(se);
 			RubyCore.log("SyntaxError in CompletionEngine::getElementsInScope()");
 		}
 	}
 
 	private Set<String> addVariablesinScope(StaticScope scope) {
 		Set<String> matches = new HashSet<String>();
 		if (scope == null) return matches;
 		String[] variables = scope.getVariables();
 		for(int i = 0; i < variables.length; i++) {
 			String local = variables[i];
 			if (!fContext.prefixStartsWith(local))
 				continue;
 			matches.add(local);
 		}
 		matches.addAll(addVariablesinScope(scope.getEnclosingScope()));
 		return matches;
 	}
 
 	private StaticScope getScope(Node enclosingNode) {
 		if (enclosingNode instanceof RootNode) {
 			RootNode root = (RootNode) enclosingNode;
 			return root.getStaticScope();
 		}
 		try {
 			Method getScopeMethod = enclosingNode.getClass().getMethod("getScope", new Class[] {});
 			Object scope = getScopeMethod.invoke(enclosingNode, new Object[0]);
 			return (StaticScope) scope;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Gets the members available inside a type node (ModuleNode, ClassNode): -
 	 * Instance variables - Class variables - Methods
 	 * 
 	 * @param typeNode
 	 * @return
 	 */
 	private void getMembersAvailableInsideType(Node typeNode) throws RubyModelException {
 		if (typeNode == null) {
 			return;
 		}
 
 		String typeName = getTypeName(typeNode);
 		if (typeName == null) {
 			return;
 		}
 
 		// Get superclass and add its public members
 		List<Node> superclassNodes = getSuperclassNodes(typeNode);
 		for (Node superclassNode : superclassNodes) {
 			getMembersAvailableInsideType(superclassNode);
 		}
 
 		// Get public members of mixins
 		List<String> mixinNames = getIncludedMixinNames(typeName);
 		for (String mixinName : mixinNames) {
 			List<Node> mixinDeclarations = getTypeDeclarationNodes(mixinName);
 			for (Node mixinDeclaration : mixinDeclarations) {
 				getMembersAvailableInsideType(mixinDeclaration);
 			}
 		}
 
 		// Get method names defined by DefnNodes and DefsNodes
 		List<Node> methodDefinitions = ScopedNodeLocator.Instance().findNodesInScope(typeNode, new INodeAcceptor() {
 			public boolean doesAccept(Node node) {
 				return (node instanceof DefnNode) || (node instanceof DefsNode);
 			}
 		});
 		for (Node methodDefinition : methodDefinitions) {
 			String name = null;
 			if (methodDefinition instanceof DefnNode) {
 				name = ((DefnNode) methodDefinition).getName();
 			}
 			if (methodDefinition instanceof DefsNode) {
 				name = ((DefsNode) methodDefinition).getName();
 			}
 			if (!fContext.prefixStartsWith(name))
 				continue;
 			NodeMethod method = new NodeMethod((MethodDefNode)methodDefinition);
 			suggestMethod(method, typeName, 100);
 		}
 		addTypesVariables(typeNode);
 	}
 
 	private String getTypeName(Node typeNode) {
 		// Get type name
 		String typeName = null;
 		if (typeNode instanceof ClassNode) {
 			typeName = ((Colon2Node) ((ClassNode) typeNode).getCPath()).getName();
 		}
 		if (typeNode instanceof ModuleNode) {
 			typeName = ((Colon2Node) ((ModuleNode) typeNode).getCPath()).getName();
 		}
 		return typeName;
 	}
 
 	private void addTypesVariables(Node typeNode) {
 		// Get instance and class variables available in the enclosing type
 		List<Node> instanceAndClassVars = ScopedNodeLocator.Instance().findNodesInScope(typeNode, new INodeAcceptor() {
 			public boolean doesAccept(Node node) {
 				return (node instanceof ConstDeclNode || node instanceof InstVarNode || node instanceof InstAsgnNode || node instanceof ClassVarNode || node instanceof ClassVarDeclNode || node instanceof ClassVarAsgnNode);
 			}
 		});
 		Set<String> fields = new HashSet<String>();
 		if (instanceAndClassVars != null) {
 			// Get the unique names of instance and class variables
 			for (Node varNode : instanceAndClassVars) {
 				String name = ASTUtil.getNameReflectively(varNode);
 				if (!fContext.prefixStartsWith(name))
 					continue;
 				fields.add(name);
 			}
 		}
 		// Get instance and class vars defined by [c]attr_* calls
 		List<String> attrs = AttributeLocator.Instance().findInstanceAttributesInScope(typeNode);
 		for (Iterator iter = attrs.iterator(); iter.hasNext();) {
 			String attr = (String) iter.next();
 			if (!fContext.prefixStartsWith(attr))
 				continue;
 			fields.add(attr);
 		}
 		for (String field : fields) {
 			CompletionProposal proposal = new CompletionProposal(CompletionProposal.FIELD_REF, field, 100);
 			proposal.setReplaceRange(fContext.getReplaceStart(), fContext.getReplaceStart() + field.length());
 			fRequestor.accept(proposal);
 		}
 	}
 
 	/**
 	 * Finds all nodes that declare a type that is a superclass of the specified
 	 * node. Example:
 	 * 
 	 * """ class Klass;def meth_1;1;end;end class Klass;def meth_2;2;end;end
 	 * 
 	 * class SubKlass < Klass;end """
 	 * 
 	 * Issuing getSuperClassNodes() on the ClassNode declaring SubKlass would
 	 * return two ClassNodes; one for each definition of Klass.
 	 * 
 	 * @param typeNode
 	 *            Node to find superclass nodes of
 	 * @return List of ClassNode or ModuleNode
 	 */
 	private List<Node> getSuperclassNodes(Node typeNode) {
 		if (typeNode instanceof ClassNode) {
 			Node superNode = ((ClassNode) typeNode).getSuperNode();
 			if (superNode instanceof ConstNode) {
 				String superclassName = ((ConstNode) superNode).getName();
 				return getTypeDeclarationNodes(superclassName);
 			}
 		}
 		return new ArrayList<Node>();
 	}
 
 	/** Lookup type declaration nodes */
 	private List<Node> getTypeDeclarationNodes(String typeName) {
 		// Find the named type
 		RubyElementRequestor requestor = new RubyElementRequestor(fContext.getScript());
 		IType[] types = requestor.findType(typeName);
 		if (types == null || types.length == 0) return new ArrayList<Node>(0);
 		IType type = types[0];
 
 		try {
 			if (type instanceof RubyType) {
 
 				// FIXME This feels a little hacky and backwards -
 				// RubyType.getSource() and then parse... consider reworking the
 				// clients to this method to accept RubyTypes or something
 				// similar?
 				// Find source and parse
 				RubyType rubyType = (RubyType) type;
 				String source = rubyType.getSource();
 				if (source == null) return new ArrayList<Node>(0);
 
 				// FIXME Why does the parser balk on \r chars?
 				source = source.replace('\r', ' ');
 				Node rootNode = (new RubyParser()).parse(source);
 
 				// Bail if the parse fails
 				if (rootNode == null) {
 					return new ArrayList<Node>();
 				}
 
 				// Return any type declaration nodes in included source
 				return ScopedNodeLocator.Instance().findNodesInScope(rootNode, new INodeAcceptor() {
 					public boolean doesAccept(Node node) {
 						return (node instanceof ClassNode) || (node instanceof ModuleNode);
 					}
 				});
 			}
 
 		} catch (RubyModelException rme) {
 			rme.printStackTrace();
 		}
 
 		return new ArrayList<Node>(0);
 	}
 
 	private List<String> getIncludedMixinNames(String typeName) {
 		IType rubyType = new RubyType((RubyElement)fContext.getScript(), typeName);
 
 		try {
 			String[] includedModuleNames = rubyType.getIncludedModuleNames();
 			if (includedModuleNames != null) {
 				return Arrays.asList(rubyType.getIncludedModuleNames());
 			} 
 			return new ArrayList<String>(0);
 		} catch (RubyModelException e) {
 			return new ArrayList<String>(0);
 		}
 	}
 	
 	private class NodeMethod implements IMethod {
 		private MethodDefNode node;
 
 		public NodeMethod(MethodDefNode methodDefinition) {
 			this.node = methodDefinition;
 		}
 
 		public String[] getParameterNames() throws RubyModelException {
 			return ASTUtil.getArgs(node.getArgsNode(), node.getScope());
 		}
 
 		public int getVisibility() throws RubyModelException {
 			return IMethod.PUBLIC;
 		}
 
 		public boolean isConstructor() {
 			return node.getName().equals(CONSTRUCTOR_DEFINITION_NAME);
 		}
 
 		public boolean isSingleton() {
 			return isConstructor() || node instanceof DefsNode;
 		}
 
 		public boolean exists() {
 			return false;
 		}
 
 		public IRubyElement getAncestor(int ancestorType) {
 			return null;
 		}
 
 		public IResource getCorrespondingResource() throws RubyModelException {
 			return null;
 		}
 
 		public String getElementName() {
 			return node.getName();
 		}
 
 		public int getElementType() {
 			return IRubyElement.METHOD;
 		}
 
 		public IOpenable getOpenable() {
 			return null;
 		}
 
 		public IRubyElement getParent() {
 			return null;
 		}
 
 		public IPath getPath() {
 			return null;
 		}
 
 		public IRubyElement getPrimaryElement() {
 			return null;
 		}
 
 		public IResource getResource() {
 			return null;
 		}
 
 		public IRubyModel getRubyModel() {
 			return null;
 		}
 
 		public IRubyProject getRubyProject() {
 			return null;
 		}
 
 		public IResource getUnderlyingResource() throws RubyModelException {
 			return null;
 		}
 
 		public boolean isReadOnly() {
 			return false;
 		}
 
 		public boolean isStructureKnown() throws RubyModelException {
 			return false;
 		}
 
 		public boolean isType(int type) {
 			return type == IRubyElement.METHOD;
 		}
 
 		public Object getAdapter(Class adapter) {
 			return null;
 		}
 
 		public IType getDeclaringType() {
 			return null;
 		}
 
 		public ISourceRange getNameRange() throws RubyModelException {
 			return null;
 		}
 
 		public IRubyScript getRubyScript() {
 			return null;
 		}
 
 		public IType getType(String name, int occurrenceCount) {
 			return null;
 		}
 
 		public String getSource() throws RubyModelException {
 			return null;
 		}
 
 		public ISourceRange getSourceRange() throws RubyModelException {
 			return null;
 		}
 
 		public IRubyElement[] getChildren() throws RubyModelException {
 			return null;
 		}
 
 		public boolean hasChildren() throws RubyModelException {
 			return false;
 		}
 
 		public String getHandleIdentifier() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 	}
 }
