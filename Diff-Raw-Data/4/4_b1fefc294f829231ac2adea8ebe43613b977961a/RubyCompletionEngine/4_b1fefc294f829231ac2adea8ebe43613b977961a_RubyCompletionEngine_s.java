 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.core.codeassist;
 
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.declarations.ISourceParser;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.codeassist.IAssistParser;
 import org.eclipse.dltk.codeassist.ScriptCompletionEngine;
 import org.eclipse.dltk.compiler.env.ISourceModule;
 import org.eclipse.dltk.compiler.problem.DefaultProblem;
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.mixin.IMixinElement;
 import org.eclipse.dltk.core.mixin.MixinModel;
 import org.eclipse.dltk.evaluation.types.AmbiguousType;
 import org.eclipse.dltk.evaluation.types.IClassType;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.ruby.ast.RubyBlock;
 import org.eclipse.dltk.ruby.ast.RubyColonExpression;
 import org.eclipse.dltk.ruby.ast.RubyDAssgnExpression;
 import org.eclipse.dltk.ruby.ast.RubyDVarExpression;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.core.RubyPlugin;
 import org.eclipse.dltk.ruby.core.model.FakeField;
 import org.eclipse.dltk.ruby.core.text.RubyKeyword;
 import org.eclipse.dltk.ruby.core.utils.RubySyntaxUtils;
 import org.eclipse.dltk.ruby.internal.parser.mixin.IRubyMixinElement;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinClass;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinElementInfo;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinMethod;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinModel;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinVariable;
 import org.eclipse.dltk.ruby.internal.parsers.jruby.ASTUtils;
 import org.eclipse.dltk.ruby.typeinference.IMixinSearchRequestor;
 import org.eclipse.dltk.ruby.typeinference.RubyClassType;
 import org.eclipse.dltk.ruby.typeinference.RubyModelUtils;
 import org.eclipse.dltk.ruby.typeinference.RubyTypeInferencingUtils;
 import org.eclipse.dltk.ti.BasicContext;
 import org.eclipse.dltk.ti.DLTKTypeInferenceEngine;
 import org.eclipse.dltk.ti.goals.ExpressionTypeGoal;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 import org.jruby.util.collections.WeakHashSet;
 
 public class RubyCompletionEngine extends ScriptCompletionEngine {
 
 	private final static int RELEVANCE_FREE_SPACE = 10000000;
 
 	private final static int RELEVANCE_KEYWORD = 1000000;
 
 	private final static int RELEVANCE_METHODS = 100000;
 
 	private final static String[] globalVars = { "$DEBUG", "$$", "$-i",
 			"$deferr", "$/", "$'", "$stdout", "$-l", "$-I", "$.", "$KCODE",
 			"$binding", "$-w", "$FILENAME", "$defout", "$,", "$`", "$-F", "$*",
 			"$LOADED_FEATURES", "$stdin", "$-p", "$:", "$\\", "$=", "$!",
 			"$-v", "$>", "$&", "$;", "$SAFE", "$PROGRAM_NAME", "$\"", "$-d",
 			"$?", "$-0", "$+", "$@", "$-a", "$VERBOSE", "$stderr", "$~", "$0",
 			"$LOAD_PATH", "$<", "$_", "$-K" };
 
 	private DLTKTypeInferenceEngine inferencer;
 	private ISourceParser parser = null;
 	private MixinModel model;
 	private HashSet completedNames = new HashSet();
 	private WeakHashSet intresting = new WeakHashSet();
 
 	private ASTNode completionNode;
 	
 	private final Comparator modelElementComparator = new Comparator() {
 
 		Collator collator = Collator.getInstance(Locale.ENGLISH);
 
 		public int compare(Object arg0, Object arg1) {
 			if (arg0 instanceof IModelElement && arg1 instanceof IModelElement) {
 				IModelElement me1 = (IModelElement) arg1;
 				IModelElement me2 = (IModelElement) arg0;
 				int r = -collator.compare(me1.getElementName(), me2
 						.getElementName());
 				if (r == 0) {
 					// prefer elements from current module
 					boolean cur1 = me1.getAncestor(IModelElement.SOURCE_MODULE)
 							.equals(currentModule);
 					boolean cur2 = me2.getAncestor(IModelElement.SOURCE_MODULE)
 							.equals(currentModule);
 					if (cur1 == cur2) {
 						return 0;
 					} else
 						return cur1 ? -1 : 1;
 				} else
 					return r;
 			}
 			return 0;
 		}
 
 		/*
 		 * private int calcVisibility(IMethod method1) throws ModelException {
 		 * return (method1.getFlags() & Modifiers.AccPrivate) +
 		 * (method1.getFlags() & Modifiers.AccProtected) + (method1.getFlags() &
 		 * Modifiers.AccPublic); }
 		 */
 
 	};
 
 	private ISourceModule currentModule;;
 
 	public RubyCompletionEngine() {
 		this.inferencer = new DLTKTypeInferenceEngine();
 		this.model = RubyMixinModel.getRawInstance();
 		try {
 			this.parser = DLTKLanguageManager
 					.getSourceParser(RubyNature.NATURE_ID);
 		} catch (CoreException e) {
 			throw new RuntimeException(
 					"Failed to initialize RubyCompletionEngine", e);
 		}
 	}
 
 	protected int getEndOfEmptyToken() {
 		return 0;
 	}
 
 	protected String processMethodName(IMethod method, String token) {
 		return null;
 	}
 
 	protected String processTypeName(IType method, String token) {
 		return null;
 	}
 
 	public IAssistParser getParser() {
 		return null;
 	}
 
 	private boolean afterColons(String content, int position) {
 		if (position < 2)
 			return false;
 		if (content.charAt(position - 1) == ':'
 				&& content.charAt(position - 2) == ':')
 			return true;
 		return false;
 	}
 
 	private boolean afterDollar(String content, int position) {
 		if (position < 1)
 			return false;
 		if (content.charAt(position - 1) == '$')
 			return true;
 		return false;
 	}
 
 	private boolean afterAt(String content, int position) {
 		if (position < 1)
 			return false;
 		if (content.charAt(position - 1) == '@')
 			return true;
 		return false;
 	}
 
 	private boolean afterAt2(String content, int position) {
 		if (position < 2)
 			return false;
 		if (content.charAt(position - 1) == '@'
 				&& content.charAt(position - 2) == '@')
 			return true;
 		return false;
 	}
 
 	private String getWordStarting(String content, int position, int maxLen) {
 		int original = position;
 		if (position <= 0)
 			return "";
 		if (position >= content.length())
 			position = content.length();
 		position--;
 		int len = 0;
 		while (position >= 0
 				&& len < maxLen
 				&& RubySyntaxUtils.isStrictIdentifierCharacter(content
 						.charAt(position))) {
 			position--;
 		}
 		if (position + 1 > original)
 			return "";
 		if (position >= 0 && Character.isWhitespace(content.charAt(position)))
 			return content.substring(position + 1, original);
 		return null;
 	}
 
 	public void complete(ISourceModule module, int position, int i) {
 		this.currentModule = module;
 		if (Job.getJobManager().find(ResourcesPlugin.FAMILY_AUTO_BUILD).length > 0) {
 			this.requestor.completionFailure(new DefaultProblem(null,
 					"Please wait until building is ready...", 0, null,
 					IStatus.WARNING, startPosition, endPosition, -1));
 			return;
 		}
 		if (!RubyPlugin.initialized) {
 			this.requestor.completionFailure(new DefaultProblem(null,
 					"Please wait while DLTK Ruby being initialized...", 0,
 					null, IStatus.WARNING, startPosition, endPosition, -1));
 			return;
 		}
 
 		completedNames.clear();
 		this.actualCompletionPosition = position;
 		this.requestor.beginReporting();
 		org.eclipse.dltk.core.ISourceModule modelModule = (org.eclipse.dltk.core.ISourceModule) module;
 		try {
 			String content = module.getSourceContents();
 
 			String wordStarting = getWordStarting(content, position, 10);
 
 			if (wordStarting != null) {
 				this.setSourceRange(position - wordStarting.length(), position);
 				String[] keywords = RubyKeyword.findByPrefix(wordStarting);
 				for (int j = 0; j < keywords.length; j++) {
 					reportKeyword(keywords[j]);
 				}
 			}
 
 			ModuleDeclaration moduleDeclaration = parser.parse(content
 					.toCharArray(), null);
 
 			if (afterDollar(content, position)) {
 				completeGlobalVar((org.eclipse.dltk.core.ISourceModule) module,
 						moduleDeclaration, "$", position);
 			} else if (afterAt2(content, position)) {
 				completeSimpleRef((org.eclipse.dltk.core.ISourceModule) module,
 						moduleDeclaration, "@@", position);
 			} else if (afterAt(content, position)) {
 				completeSimpleRef((org.eclipse.dltk.core.ISourceModule) module,
 						moduleDeclaration, "@", position);
 			} else if (afterColons(content, position)) {
 
 				ASTNode node = ASTUtils.findMaximalNodeEndingAt(
 						moduleDeclaration, position - 2);
 				this.setSourceRange(position, position);
 				if (node != null) {
 					BasicContext basicContext = new BasicContext(modelModule,
 							moduleDeclaration);
 					ExpressionTypeGoal goal = new ExpressionTypeGoal(
 							basicContext, node);
 					IEvaluatedType type = inferencer.evaluateType(goal, 3000);
 					reportSubElements(modelModule, type, "");
 				} else {
 					completeConstant(modelModule, moduleDeclaration, "",
 							position, true);
 				}
 			} else {
 				ASTNode minimalNode = ASTUtils.findMinimalNode(
 						moduleDeclaration, position, position);
 				if (minimalNode != null) {
 					this.completionNode = minimalNode;
 					if (minimalNode instanceof CallExpression) {
 						completeCall(modelModule, moduleDeclaration,
 								(CallExpression) minimalNode, position);
 					} else if (minimalNode instanceof ConstantReference) {
 						completeConstant(modelModule, moduleDeclaration,
 								(ConstantReference) minimalNode, position);
 					} else if (minimalNode instanceof RubyColonExpression) {
 						completeColonExpression(modelModule, moduleDeclaration,
 								(RubyColonExpression) minimalNode, position);
 					} else if (minimalNode instanceof SimpleReference) {
 						completeSimpleRef(modelModule, moduleDeclaration,
 								((SimpleReference) minimalNode).getName(),
 								position);
 					} else if (minimalNode instanceof RubyDVarExpression) {
 						completeSimpleRef(modelModule, moduleDeclaration,
 								((RubyDVarExpression) minimalNode).getName(),
 								position);
 					} else { // worst case
 						if (wordStarting == null || wordStarting.length() == 0) {
 							int rel = RELEVANCE_FREE_SPACE;
 							try {
 								IModelElement[] children = modelModule
 										.getChildren();
 								if (children != null)
 									for (int j = 0; j < children.length; j++) {
 										if (children[j] instanceof IField)
 											reportField((IField) children[j],
 													rel);
 										if (children[j] instanceof IMethod) {
 											IMethod method = (IMethod) children[j];
 											if ((method.getFlags() & Modifiers.AccStatic) == 0)
 												reportMethod(method, rel);
 										}
 										if (children[j] instanceof IType
 												&& !children[j]
 														.getElementName()
 														.trim()
 														.startsWith("<<"))
 											reportType((IType) children[j], rel);
 									}
 							} catch (ModelException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 				}
 
 			}
 
 		} finally {
 			this.requestor.endReporting();
 		}
 	}
 
 	private class CompletionMixinMethodRequestor implements
 			IMixinSearchRequestor {
 		private String lastParent = null;
 		private List group = new ArrayList();
 		private int relevance = 0;
 		private final RubyMixinClass klass;
 
 		public CompletionMixinMethodRequestor(RubyMixinClass klass) {
 			super();
 			this.klass = klass;
 		}
 
 		public void acceptResult(IRubyMixinElement element) {
 			if (element instanceof RubyMixinMethod) {
 				RubyMixinMethod method = (RubyMixinMethod) element;
 				String parent = method.getSelfType().getKey();
 				if (lastParent == null || !lastParent.equals(parent)) {
 					this.flush();
 					lastParent = parent;
 				}
 				group.add(method);
 			}
 		}
 
 		public void flush() {
 			if (group.size() > 0) {
 				RubyMixinMethod[] mixinMethods = (RubyMixinMethod[]) group
 						.toArray(new RubyMixinMethod[group.size()]);
 				List allSourceMethods = RubyModelUtils.getAllSourceMethods(
 						mixinMethods, klass);
 				IMethod[] methods = (IMethod[]) allSourceMethods
 						.toArray(new IMethod[allSourceMethods.size()]);
 				Arrays.sort(methods, modelElementComparator);
 				for (int j = 0; j < methods.length; j++) {
 					reportMethod(methods[j], RELEVANCE_METHODS + relevance);
 				}
 				group.clear();
 			}
 			relevance--;
 		}
 
 	}
 
 	private void completeClassMethods(
 			org.eclipse.dltk.core.ISourceModule modelModule,
 			ModuleDeclaration moduleDeclaration, RubyMixinClass rubyClass,
 			String prefix) {
 		CompletionMixinMethodRequestor mixinSearchRequestor = new CompletionMixinMethodRequestor(
 				rubyClass);
 		rubyClass.findMethods(prefix, true, mixinSearchRequestor);
 		mixinSearchRequestor.flush();
 	}
 
 	private void completeClassMethods(
 			org.eclipse.dltk.core.ISourceModule modelModule,
 			ModuleDeclaration moduleDeclaration, IEvaluatedType type,
 			String prefix) {
 		if (type instanceof RubyClassType) {
 			RubyClassType rubyClassType = (RubyClassType) type;
 			RubyMixinClass rubyClass = RubyMixinModel.getInstance()
 					.createRubyClass(rubyClassType);
 			if (rubyClass != null) {
 				completeClassMethods(modelModule, moduleDeclaration, rubyClass,
 						prefix);
 			}
 
 		} else if (type instanceof AmbiguousType) {
 			AmbiguousType type2 = (AmbiguousType) type;
 			IEvaluatedType[] possibleTypes = type2.getPossibleTypes();
 			for (int i = 0; i < possibleTypes.length; i++) {
 				completeClassMethods(modelModule, moduleDeclaration,
 						possibleTypes[i], prefix);
 			}
 		}
 	}
 
 	private void completeClassMethods(
 			org.eclipse.dltk.core.ISourceModule modelModule,
 			ModuleDeclaration moduleDeclaration, ASTNode receiver,
 			String pattern) {
 		ExpressionTypeGoal goal = new ExpressionTypeGoal(new BasicContext(
 				modelModule, moduleDeclaration), receiver);
 		IEvaluatedType type = inferencer.evaluateType(goal, 2000);
 		completeClassMethods(modelModule, moduleDeclaration, type, pattern);
 	}
 
 	private void completeGlobalVar(org.eclipse.dltk.core.ISourceModule module,
 			ModuleDeclaration moduleDeclaration, String prefix, int position) {
 		int relevance = 424242;
 		this.setSourceRange(position - prefix.length(), position);
 
 		IMixinElement[] elements = RubyMixinModel.getRawInstance().find(
 				prefix + "*");
 
 // String[] findKeys = RubyMixinModel.getRawInstance().findKeys(
 // prefix + "*");
 		for (int i = 0; i < elements.length; i++) {
 			IRubyMixinElement rubyElement = RubyMixinModel.getInstance()
 					.createRubyElement(elements[i]);
 			if (rubyElement instanceof RubyMixinVariable) {
 				RubyMixinVariable variable = (RubyMixinVariable) rubyElement;
 				IField[] sourceFields = variable.getSourceFields();
 				for (int j = 0; j < sourceFields.length; j++) {
 					if (sourceFields[j] != null) {
 						reportField(sourceFields[j], relevance--);
 						break;
 					}
 				}
 			}
 		}
 
 		for (int i = 0; i < globalVars.length; i++) {
 			if (globalVars[i].startsWith(prefix))
 				reportField(new FakeField((ModelElement) module, globalVars[i],
 						0, 0), relevance--);
 		}
 	}
 
 	private void completeSimpleRef(org.eclipse.dltk.core.ISourceModule module,
 			ModuleDeclaration moduleDeclaration, String prefix, int position) {
 		int relevance = 424242;
 
 		this.setSourceRange(position - prefix.length(), position);
 		ASTNode[] wayToNode = ASTUtils.restoreWayToNode(moduleDeclaration,
 				this.completionNode);
 		for (int i = wayToNode.length - 1; i > 0; i--) {
 			if (wayToNode[i] instanceof RubyBlock) {
 				RubyBlock rubyBlock = (RubyBlock) wayToNode[i];
 				Set vars = rubyBlock.getVars();
 				for (Iterator iterator = vars.iterator(); iterator.hasNext();) {
 					ASTNode n = (ASTNode) iterator.next();
 					if (n instanceof RubyDAssgnExpression) {
 						RubyDAssgnExpression rd = (RubyDAssgnExpression) n;
 						if (rd.getName().startsWith(prefix)) {
 							reportField(new FakeField((ModelElement) module, rd
 									.getName(), 0, 0), relevance--);
 						}
 					}
 				}
 			}
 		}
 
 		if (prefix.startsWith("$")) { // globals
 			completeGlobalVar(module, moduleDeclaration, prefix, position);
 		} else { // class & instance & locals
 			IField[] fields = RubyModelUtils.findFields(module,
 					moduleDeclaration, prefix, position);
 			for (int i = 0; i < fields.length; i++) {
 				reportField(fields[i], relevance--);
 			}
 		}
 
 	}
 
 	private void reportSubElements(org.eclipse.dltk.core.ISourceModule module,
 			IEvaluatedType type, String prefix) {
 		int relevance = 424242;
 
 		ArrayList types = new ArrayList();
 		ArrayList methods = new ArrayList();
 		ArrayList fields = new ArrayList();
 
 		if (type instanceof RubyClassType) {
 			RubyClassType rubyClassType = (RubyClassType) type;
 			IMixinElement mixinElement = model.get(rubyClassType.getModelKey());
 			if (mixinElement != null) {
 				IMixinElement[] children = mixinElement.getChildren();
 				for (int i = 0; i < children.length; i++) {
 					Object[] infos = children[i].getAllObjects();
 					for (int j = 0; j < infos.length; j++) {
 						RubyMixinElementInfo obj = (RubyMixinElementInfo) infos[j];
 						if (obj.getObject() == null)
 							continue;
 						if (obj.getKind() == RubyMixinElementInfo.K_CLASS
 								|| obj.getKind() == RubyMixinElementInfo.K_MODULE) {
 							IType type2 = (IType) obj.getObject();
 							if (type2 != null
 									&& type2.getElementName()
 											.startsWith(prefix)) {
 								// reportType(type2, relevance--);
 								types.add(type2);
 							}
 						} else if (obj.getKind() == RubyMixinElementInfo.K_METHOD) {
 							IMethod method2 = (IMethod) obj.getObject();
 							if (method2 != null
 									&& method2.getElementName().startsWith(
 											prefix)) {
 								// reportMethod(method2, relevance--);
 								methods.add(method2);
 							}
 						}
 						if (obj.getKind() == RubyMixinElementInfo.K_VARIABLE) {
 							IField fff = (IField) obj.getObject();
 							if (fff != null
 									&& fff.getElementName().startsWith(prefix)) {
 								// reportField(fff, relevance--);
 								fields.add(fff);
 							}
 						}
 						break;
 					}
 
 				}
 			}
 		} else {
 			// never should be here
 		}
 
 		Collections.sort(fields, modelElementComparator);
 		for (Iterator iterator = fields.iterator(); iterator.hasNext();) {
 			IField t = (IField) iterator.next();
 			reportField(t, relevance--);
 		}
 
 		Collections.sort(types, modelElementComparator);
 		for (Iterator iterator = types.iterator(); iterator.hasNext();) {
 			IType t = (IType) iterator.next();
 			reportType(t, relevance--);
 		}
 
 		Collections.sort(methods, modelElementComparator);
 		for (Iterator iterator = methods.iterator(); iterator.hasNext();) {
 			IMethod t = (IMethod) iterator.next();
 			reportMethod(t, relevance--);
 		}
 
 	}
 
 	private void completeColonExpression(
 			org.eclipse.dltk.core.ISourceModule module,
 			ModuleDeclaration moduleDeclaration, RubyColonExpression node,
 			int position) {
 		String content;
 		try {
 			content = module.getSource();
 		} catch (ModelException e) {
 			return;
 		}
 		int pos = (node.getLeft() != null) ? (node.getLeft().sourceEnd() + 2)
 				: (node.sourceStart());
 		String starting = null;
 		try {
 			starting = content.substring(pos, position).trim();
 		} catch (IndexOutOfBoundsException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		if (starting.startsWith("::")) {
 			this.setSourceRange(position - starting.length() + 2, position);
 			completeConstant(module, moduleDeclaration, starting.substring(2),
 					position, true);
 			return;
 		}
 
 		this.setSourceRange(position - starting.length(), position);
 
 		ExpressionTypeGoal goal = new ExpressionTypeGoal(new BasicContext(
 				module, moduleDeclaration), node.getLeft());
 		IEvaluatedType type = inferencer.evaluateType(goal, 3000);
 		reportSubElements(module, type, starting);
 	}
 
 	private void completeConstant(org.eclipse.dltk.core.ISourceModule module,
 			ModuleDeclaration moduleDeclaration, String prefix, int position,
 			boolean topLevelOnly) {
 
 		if (!topLevelOnly) {
 			IMixinElement[] modelStaticScopes = RubyTypeInferencingUtils
 					.getModelStaticScopes(model, moduleDeclaration, position);
 			for (int i = modelStaticScopes.length - 1; i >= 0; i--) {
 				IMixinElement scope = modelStaticScopes[i];
 				if (scope == null)
 					continue;
 				reportSubElements(module, new RubyClassType(scope.getKey()),
 						prefix);
 			}
 		}
 
 		int relevance = 4242;
 
 		// try {
 		if (prefix.length() > 0) {
 			String varkey = "Object" + MixinModel.SEPARATOR + prefix;
 			RubyMixinModel rubyModel = RubyMixinModel.getInstance();
 			String[] keys2 = rubyModel.getRawModel().findKeys(varkey + "*");
 			for (int i = 0; i < keys2.length; i++) {
 				IRubyMixinElement element = rubyModel
 						.createRubyElement(keys2[i]);
 				if (element instanceof RubyMixinVariable) {
 					RubyMixinVariable variable = (RubyMixinVariable) element;
 					IField[] sourceFields = variable.getSourceFields();
 					for (int j = 0; j < sourceFields.length; j++) {
 						if (sourceFields[j] != null) {
 							reportField(sourceFields[j], relevance--);
 							break;
 						}
 					}
 				}
 			}
 		}
 		// } catch (ModelException e) {
 		// e.printStackTrace();
 		// }
 
 		HashSet names = new HashSet();
 		IType[] allTypes = RubyTypeInferencingUtils.getAllTypes(module, prefix);
 		for (int i = 0; i < allTypes.length; i++) {
 			String elementName = allTypes[i].getElementName();
 			if (names.contains(elementName))
 				continue;
 			names.add(elementName);
 			reportType(allTypes[i], relevance--);
 		}
 
 	}
 
 	private void completeConstant(org.eclipse.dltk.core.ISourceModule module,
 			ModuleDeclaration moduleDeclaration, ConstantReference node,
 			int position) {
 		String content;
 		try {
 			content = module.getSource();
 		} catch (ModelException e) {
 			return;
 		}
 
 		String prefix = content.substring(node.sourceStart(), position);
 		this.setSourceRange(position - prefix.length(), position);
 		completeConstant(module, moduleDeclaration, prefix, position, false);
 	}
 
 	private void completeCall(org.eclipse.dltk.core.ISourceModule module,
 			ModuleDeclaration moduleDeclaration, CallExpression node,
 			int position) {
 		ASTNode receiver = node.getReceiver();
 
 		String content;
 		try {
 			content = module.getSource();
 		} catch (ModelException e) {
 			return;
 		}
 
 		int pos = (receiver != null) ? (receiver.sourceEnd() + 1) : (node
 				.sourceStart());
 
 		for (int t = 0; t < 2; t++) { // correct not more 2 chars
 			if (pos < position
 					&& !RubySyntaxUtils.isStrictIdentifierCharacter(content
 							.charAt(pos))) // for (...).name and Foo::name
 				// calls
 				pos++;
 		}
 
 		String starting = content.substring(pos, position).trim();
 
 		if (receiver == null)
 			completeSimpleRef(module, moduleDeclaration, starting, position);
 
 		this.setSourceRange(position - starting.length(), position);
 
 		if (starting.startsWith("__")) {
 			String[] keywords = RubyKeyword.findByPrefix("__");
 			for (int j = 0; j < keywords.length; j++) {
 				reportKeyword(keywords[j]);
 			}
 		}
 
 		if (receiver != null) {
 			completeClassMethods(module, moduleDeclaration, receiver, starting);
 		} else {
 			IClassType self = RubyTypeInferencingUtils.determineSelfClass(
 					module, moduleDeclaration, position);
 			completeClassMethods(module, moduleDeclaration, self, starting);
 		}
 
 	}
 
 	protected String processFieldName(IField field, String token) {
 		return field.getElementName();
 	}
 
 	private void reportMethod(IMethod method, int rel) {
 		this.intresting.add(method);
 		String elementName = method.getElementName();
 		if (completedNames.contains(elementName)) {
 			return;
 		}
 		completedNames.add(elementName);
 		if (elementName.indexOf('.') != -1) {
 			elementName = elementName.substring(elementName.indexOf('.') + 1);
 		}
 		char[] name = elementName.toCharArray();
 		char[] compl = name;
 
 		int relevance = rel;
 
 		// accept result
 		noProposal = false;
 		if (!requestor.isIgnored(CompletionProposal.METHOD_DECLARATION)) {
 			CompletionProposal proposal = createProposal(
 					CompletionProposal.METHOD_DECLARATION,
 					actualCompletionPosition);
 
 			String[] params = null;
 			try {
 				params = method.getParameters();
 			} catch (ModelException e) {
 				e.printStackTrace();
 			}
 
 			if (params != null && params.length > 0) {
 				char[][] args = new char[params.length][];
 				for (int i = 0; i < params.length; ++i) {
 					args[i] = params[i].toCharArray();
 				}
 				proposal.setParameterNames(args);
 			}
 
 			proposal.setModelElement(method);
 			proposal.setName(name);
 			proposal.setCompletion(compl);
 			try {
 				proposal.setFlags(method.getFlags());
 			} catch (ModelException e) {
 				RubyPlugin.log(e);
 			}
 			proposal.setReplaceRange(this.startPosition - this.offset,
 					this.endPosition - this.offset);
 			proposal.setRelevance(relevance);
 			this.requestor.accept(proposal);
 			if (DEBUG) {
 				this.printDebug(proposal);
 			}
 		}
 
 	}
 
 	private void reportType(IType type, int rel) {
 		this.intresting.add(type);
 		String elementName = type.getElementName();
 		if (completedNames.contains(elementName)) {
 			return;
 		}
 		completedNames.add(elementName);
 		char[] name = elementName.toCharArray();
 		if (name.length == 0)
 			return;
 
 		int relevance = rel;
 
 		// accept result
 		noProposal = false;
 		if (!requestor.isIgnored(CompletionProposal.TYPE_REF)) {
 			CompletionProposal proposal = createProposal(
 					CompletionProposal.TYPE_REF, actualCompletionPosition);
 
 			proposal.setModelElement(type);
 			proposal.setName(name);
 			proposal.setCompletion(elementName.toCharArray());
 			// proposal.setFlags(Flags.AccDefault);
 			proposal.setReplaceRange(this.startPosition - this.offset,
 					this.endPosition - this.offset);
 			proposal.setRelevance(relevance);
 			this.requestor.accept(proposal);
 			if (DEBUG) {
 				this.printDebug(proposal);
 			}
 		}
 
 	}
 
 	private void reportField(IField field, int rel) {
 		this.intresting.add(field);
 		String elementName = field.getElementName();
 		if (completedNames.contains(elementName)) {
 			return;
 		}
 		completedNames.add(elementName);
 		char[] name = elementName.toCharArray();
 		if (name.length == 0)
 			return;
 
 		int relevance = rel;
 
 		// accept result
 		noProposal = false;
 		if (!requestor.isIgnored(CompletionProposal.FIELD_REF)) {
 			CompletionProposal proposal = createProposal(
 					CompletionProposal.FIELD_REF, actualCompletionPosition);
 
 			proposal.setModelElement(field);
 			proposal.setName(name);
 			proposal.setCompletion(elementName.toCharArray());
 			// proposal.setFlags(Flags.AccDefault);
 			proposal.setReplaceRange(this.startPosition - this.offset,
 					this.endPosition - this.offset);
 			proposal.setRelevance(relevance);
 			this.requestor.accept(proposal);
 			if (DEBUG) {
 				this.printDebug(proposal);
 			}
 		}
 
 	}
 
 	private void reportKeyword(String name) {
 		// accept result
 		noProposal = false;
 		if (!requestor.isIgnored(CompletionProposal.FIELD_REF)) {
 			CompletionProposal proposal = createProposal(
 					CompletionProposal.KEYWORD, actualCompletionPosition);
 
 			proposal.setName(name.toCharArray());
 			proposal.setCompletion(name.toCharArray());
 			// proposal.setFlags(Flags.AccDefault);
 			proposal.setReplaceRange(this.startPosition - this.offset,
 					this.endPosition - this.offset);
 			proposal.setRelevance(RELEVANCE_KEYWORD);
 			this.requestor.accept(proposal);
 			if (DEBUG) {
 				this.printDebug(proposal);
 			}
 		}
 
 	}
 
 }
