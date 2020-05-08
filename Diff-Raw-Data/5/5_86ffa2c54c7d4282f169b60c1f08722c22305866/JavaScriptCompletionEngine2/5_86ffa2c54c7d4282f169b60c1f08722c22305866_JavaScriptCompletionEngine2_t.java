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
 package org.eclipse.dltk.javascript.internal.core.codeassist;
 
 import static org.eclipse.dltk.javascript.ast.Keywords.THIS;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.codeassist.ScriptCompletionEngine;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.compiler.problem.IValidationStatus;
 import org.eclipse.dltk.compiler.problem.ValidationStatus;
 import org.eclipse.dltk.core.CompletionContext;
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IAccessRule;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.internal.javascript.ti.FunctionMethod;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.ITypeInferenceContext;
 import org.eclipse.dltk.internal.javascript.ti.PositionReachedException;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencer2;
 import org.eclipse.dltk.internal.javascript.typeinference.CompletionPath;
 import org.eclipse.dltk.internal.javascript.validation.MemberValidationEvent;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.core.JavaScriptKeywords;
 import org.eclipse.dltk.javascript.core.JavaScriptPlugin;
 import org.eclipse.dltk.javascript.core.NodeFinder;
 import org.eclipse.dltk.javascript.core.Types;
 import org.eclipse.dltk.javascript.internal.core.codeassist.JavaScriptCompletionUtil.ExpressionContext;
 import org.eclipse.dltk.javascript.internal.core.codeassist.JavaScriptCompletionUtil.ExpressionType;
 import org.eclipse.dltk.javascript.parser.JavaScriptParserUtil;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueParent;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinfo.IRArrayType;
 import org.eclipse.dltk.javascript.typeinfo.IRClassType;
 import org.eclipse.dltk.javascript.typeinfo.IRElement;
 import org.eclipse.dltk.javascript.typeinfo.IRFunctionType;
 import org.eclipse.dltk.javascript.typeinfo.IRMember;
 import org.eclipse.dltk.javascript.typeinfo.IRMethod;
 import org.eclipse.dltk.javascript.typeinfo.IRParameter;
 import org.eclipse.dltk.javascript.typeinfo.IRRecordMember;
 import org.eclipse.dltk.javascript.typeinfo.IRRecordType;
 import org.eclipse.dltk.javascript.typeinfo.IRSimpleType;
 import org.eclipse.dltk.javascript.typeinfo.IRType;
 import org.eclipse.dltk.javascript.typeinfo.IRTypeDeclaration;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.typeinfo.ITypeSystem;
 import org.eclipse.dltk.javascript.typeinfo.JSTypeSet;
 import org.eclipse.dltk.javascript.typeinfo.MemberPredicate;
 import org.eclipse.dltk.javascript.typeinfo.MemberPredicates;
 import org.eclipse.dltk.javascript.typeinfo.RTypeMemberQuery;
 import org.eclipse.dltk.javascript.typeinfo.RTypes;
 import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
 import org.eclipse.dltk.javascript.typeinfo.TypeMode;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.ParameterKind;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.dltk.javascript.validation.IValidatorExtension;
 
 public class JavaScriptCompletionEngine2 extends ScriptCompletionEngine
 		implements JSCompletionEngine {
 
 	private int globalOptions = JSCompletionEngine.OPTION_ALL;
 
 	public int getGlobalOptions() {
 		return globalOptions;
 	}
 
 	public void setGlobalOptions(int value) {
 		this.globalOptions = value;
 	}
 
 	public void complete(final IModuleSource cu, final int position, int i) {
 		this.requestor.beginReporting();
 		final String content = cu.getSourceContents();
 		if (position < 0 || position > content.length()) {
 			return;
 		}
 
 		final TypeInferencer2 inferencer2 = new TypeInferencer2();
 		inferencer2.setModelElement(cu.getModelElement());
 
 		final Script script = JavaScriptParserUtil.parse(cu, null);
 		final NodeFinder nodeFinder = new NodeFinder(position, position);
 		nodeFinder.locate(script);
 		if (nodeFinder.getNode() instanceof StringLiteral) {
 			// don't complete inside string literals
 			return;
 		}
 		final PositionCalculator calculator = new PositionCalculator(content,
 				position, false);
 		final CompletionVisitor visitor = new CompletionVisitor(inferencer2,
 				position);
 		inferencer2.setVisitor(visitor);
 		if (cu instanceof org.eclipse.dltk.core.ISourceModule) {
 			inferencer2
 					.setModelElement((org.eclipse.dltk.core.ISourceModule) cu);
 		}
 		try {
 			inferencer2.doInferencing(script);
 		} catch (PositionReachedException e) {
 			// e.printStackTrace();
 		}
 		ITypeSystem.CURRENT.runWith(inferencer2, new Runnable() {
 			public void run() {
 				final CompletionPath path = new CompletionPath(calculator
 						.getCompletion());
 				final ASTNode node = nodeFinder.getNode();
 				if (node instanceof Identifier) {
 					setSourceRange(node.start(), node.end());
 				} else {
					String lastSegment = path.lastSegment();
					if (lastSegment == null)
						lastSegment = "";
					setSourceRange(position - lastSegment.length(),
 							position);
 				}
 				final Reporter reporter = new Reporter(inferencer2, path
 						.lastSegment(), position, TypeInfoManager
 						.createExtensions(inferencer2,
 								IValidatorExtension.class, null));
 				if (calculator.isMember() && !path.isEmpty()
 						&& path.lastSegment() != null) {
 					doCompletionOnMember(inferencer2, visitor.getCollection(),
 							path, reporter);
 				} else {
 					doGlobalCompletion(visitor.getCollection(), reporter,
 							JavaScriptCompletionUtil.evaluateExpressionContext(
 									script, content, position));
 				}
 			}
 		});
 		this.requestor.endReporting();
 	}
 
 	public void completeTypes(ISourceModule module, TypeMode mode,
 			String prefix, int offset) {
 		final TypeInferencer2 inferencer2 = new TypeInferencer2();
 		inferencer2.setModelElement(module);
 		setSourceRange(offset - prefix.length(), offset);
 		doCompletionOnType(mode, new Reporter(inferencer2, prefix, offset,
 				Collections.<IValidatorExtension> emptyList()));
 	}
 
 	public void completeGlobals(ISourceModule module, final String prefix,
 			final int offset, boolean jsdoc) {
 		final CompletionContext completionContext = new CompletionContext();
 		completionContext.setOffset(offset);
 		completionContext.setDoc(jsdoc);
 		requestor.acceptContext(completionContext);
 		setSourceRange(offset - prefix.length(), offset);
 		final TypeInferencer2 inferencer2 = new TypeInferencer2();
 		inferencer2.setModelElement(module);
 		final CompletionVisitor visitor = new CompletionVisitor(inferencer2,
 				Integer.MAX_VALUE);
 		inferencer2.setVisitor(visitor);
 		final Script script = JavaScriptParserUtil.parse(module, null);
 		try {
 			inferencer2.doInferencing(script);
 		} catch (PositionReachedException e) {
 			// e.printStackTrace();
 		}
 		ITypeSystem.CURRENT.runWith(inferencer2, new Runnable() {
 			public void run() {
 				final Reporter reporter = new Reporter(inferencer2, prefix,
 						offset, TypeInfoManager.createExtensions(inferencer2,
 								IValidatorExtension.class, null));
 				doGlobalCompletion(visitor.getCollection(), reporter, null);
 			}
 		});
 	}
 
 	/**
 	 * Generate completion proposals for the matching members from the specified
 	 * {@link Iterable}.
 	 */
 	public void completeMembers(ISourceModule module, String prefix,
 			int offset, boolean jsdoc, Iterable<IRMember> memers) {
 		final CompletionContext completionContext = new CompletionContext();
 		completionContext.setOffset(offset);
 		completionContext.setDoc(jsdoc);
 		requestor.acceptContext(completionContext);
 		setSourceRange(offset - prefix.length(), offset);
 		final TypeInferencer2 inferencer2 = new TypeInferencer2();
 		inferencer2.setModelElement(module);
 		final Reporter reporter = new Reporter(inferencer2, prefix, offset,
 				TypeInfoManager.createExtensions(inferencer2,
 						IValidatorExtension.class, null));
 		for (IRMember member : memers) {
 			final String name = member.getName();
 			if (reporter.matches(name)) {
 				reporter.report(name, member);
 			}
 		}
 	}
 
 	private void doCompletionOnType(TypeMode mode, Reporter reporter) {
 		final ITypeInferenceContext context = reporter.context;
 		Set<String> typeNames = context.listTypes(mode, reporter.getPrefix());
 		for (String typeName : typeNames) {
 			final Type type = context.getType(typeName);
 			if (type != null && type.isVisible()) {
 				reporter.reportTypeRef(type);
 			}
 		}
 	}
 
 	private static boolean exists(IValueParent item) {
 		if (item instanceof IValueReference) {
 			return ((IValueReference) item).exists();
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * @param context
 	 * @param collection
 	 * @param startPart
 	 */
 	private void doCompletionOnMember(ITypeInferenceContext context,
 			IValueCollection collection, CompletionPath path, Reporter reporter) {
 		IValueParent item = collection;
 		for (int i = 0; i < path.segmentCount() - 1; ++i) {
 			if (path.isName(i)) {
 				final String segment = path.segment(i);
 				if (THIS.equals(segment) && item instanceof IValueCollection) {
 					item = ((IValueCollection) item).getThis();
 				} else {
 					item = item.getChild(segment);
 				}
 				if (!exists(item))
 					break;
 			} else if (path.isFunction(i)) {
 				item = item.getChild(IValueReference.FUNCTION_OP);
 				if (!exists(item))
 					break;
 			} else if (path.isObject(i)) {
 				item = item.getChild(ITypeNames.OBJECT).getChild(
 						IValueReference.FUNCTION_OP);
 				break;
 			} else {
 				assert path.isArray(i);
 				item = item.getChild(IValueReference.ARRAY_OP);
 				if (!exists(item))
 					break;
 			}
 		}
 
 		if (item != null && exists(item)) {
 			reportItems(reporter, item);
 		}
 	}
 
 	protected void reportItems(Reporter reporter, IValueParent item) {
 		reporter.report(item);
 		if (item instanceof IValueCollection) {
 			IValueCollection coll = (IValueCollection) item;
 			for (;;) {
 				coll = coll.getParent();
 				if (coll == null)
 					break;
 				reporter.report(coll);
 			}
 		} else if (item instanceof IValueReference) {
 			reporter.reportValueTypeMembers((IValueReference) item);
 		}
 	}
 
 	protected void reportGlobals(Reporter reporter) {
 		final ITypeInferenceContext context = reporter.context;
 		final Set<String> globals = context.listGlobals(reporter.getPrefix());
 		for (String global : globals) {
 			if (reporter.canReport(global)) {
 				IRMember element = context.resolve(global);
 				if (element != null && element.isVisible()) {
 					reporter.report(global, element);
 				}
 			} else if (global.lastIndexOf('.') != -1) {
 				Type type = context.getType(global);
 				if (type != null && type.isVisible()
 						&& type.getKind() != TypeKind.UNKNOWN) {
 					reporter.reportTypeRef(type);
 				}
 
 			}
 		}
 	}
 
 	private class Reporter {
 		final ITypeInferenceContext context;
 		final char[] prefix;
 		private final String prefixStr;
 		final int position;
 		final Set<String> processed = new HashSet<String>();
 		final boolean camelCase = DLTKCore.ENABLED.equals(DLTKCore
 				.getOption(DLTKCore.CODEASSIST_CAMEL_CASE_MATCH));
 		final boolean visibilityCheck = DLTKCore.ENABLED.equals(Platform
 				.getPreferencesService().getString(JavaScriptPlugin.PLUGIN_ID,
 						DLTKCore.CODEASSIST_VISIBILITY_CHECK, null, null));
 		final IValidatorExtension[] extensions;
 
 		public Reporter(ITypeInferenceContext context, String prefix,
 				int position, List<IValidatorExtension> extensions) {
 			this.context = context;
 			this.prefixStr = prefix != null ? prefix : "";
 			this.prefix = prefixStr.toCharArray();
 			this.position = position;
 			if (!extensions.isEmpty()) {
 				this.extensions = extensions
 						.toArray(new IValidatorExtension[extensions.size()]);
 			} else {
 				this.extensions = null;
 			}
 		}
 
 		public void ignore(String generatedIdentifier) {
 			processed.add(generatedIdentifier);
 		}
 
 		public String getPrefix() {
 			return prefixStr;
 		}
 
 		public int getPosition() {
 			return position;
 		}
 
 		public void report(String name, IRElement element) {
 			if (element instanceof IRMember && processed.add(name)) {
 				reportMember((IRMember) element, name, false);
 			}
 		}
 
 		public boolean canReport(String name) {
 			return matches(name) && !processed.contains(name);
 		}
 
 		boolean matches(String name) {
 			return CharOperation.prefixEquals(prefix, name, false) || camelCase
 					&& CharOperation.camelCaseMatch(prefix, name.toCharArray());
 		}
 
 		private MemberValidationEvent memberValidationEvent;
 
 		public void report(IValueParent item) {
 			final Set<String> deleted = item.getDeletedChildren();
 			CHILDREN: for (String childName : item.getDirectChildren()) {
 				if (childName.equals(IValueReference.FUNCTION_OP))
 					continue;
 				if (!deleted.contains(childName) && matches(childName)
 						&& processed.add(childName)) {
 					IValueReference child = item.getChild(childName);
 					if (child.exists()) {
 						if (visibilityCheck && extensions != null) {
 							if (memberValidationEvent == null) {
 								memberValidationEvent = new MemberValidationEvent();
 							}
 							memberValidationEvent.set(child, null);
 							for (IValidatorExtension extension : extensions) {
 								final IValidationStatus status = extension
 										.validateAccessibility(memberValidationEvent);
 								if (status != null) {
 									if (status == ValidationStatus.OK) {
 										break;
 									} else {
 										continue CHILDREN;
 									}
 								}
 							}
 						}
 						reportReference(child);
 					}
 				}
 			}
 		}
 
 		public void reportValueTypeMembers(IValueReference valueRef) {
 			final RTypeMemberQuery typeQuery = new RTypeMemberQuery();
 			final Set<IRMember> members = new HashSet<IRMember>();
 			collectTypes(valueRef.getDeclaredTypes(), typeQuery, members,
 					valueRef);
 			collectTypes(valueRef.getTypes(), typeQuery, members, valueRef);
 			for (IRMember member : members) {
 				if (member.isVisible() && matches(member.getName())) {
 					reportMember(member, member.getName(), true);
 				}
 			}
 			for (IRMember member : typeQuery.ignoreDuplicates(processed)) {
 				if (member.isVisible() && matches(member.getName())) {
 					reportMember(member, member.getName(),
 							typeQuery.contains(member.getDeclaringType()));
 				}
 			}
 		}
 
 		protected void collectTypes(final JSTypeSet types,
 				final RTypeMemberQuery typeQuery,
 				final Collection<IRMember> members, IValueReference valueRef) {
 			for (IRType type : types) {
 				if (type instanceof IRClassType) {
 					final IRTypeDeclaration t = ((IRClassType) type)
 							.getDeclaration();
 					if (t != null) {
 						final MemberPredicate predicate = t.getSource()
 								.memberPredicateFor(type,
 										MemberPredicates.STATIC);
 						typeQuery.add(t, predicate);
 						if (t.getSource().hasPrototype()
 								&& predicate
 										.isCompatibleWith(MemberPredicates.STATIC)) {
 							Type prototypeType = t.getSource()
 									.getPrototypeType();
 							if (prototypeType == Types.FUNCTION
 									&& !canInstantiate(t.getSource(), valueRef)) {
 								prototypeType = Types.OBJECT;
 							}
 							typeQuery.add(context.convert(prototypeType),
 									MemberPredicates.NON_STATIC);
 						}
 					} else {
 						typeQuery.add(RTypes.FUNCTION.getDeclaration(),
 								MemberPredicates.NON_STATIC);
 					}
 				} else if (type instanceof IRSimpleType) {
 					final IRTypeDeclaration t = ((IRSimpleType) type)
 							.getDeclaration();
 					if (t != null) {
 						typeQuery.add(
 								t,
 								t.getSource().memberPredicateFor(type,
 										MemberPredicates.NON_STATIC));
 					}
 				} else if (type instanceof IRRecordType) {
 					members.addAll(((IRRecordType) type).getMembers());
 					typeQuery.add(RTypes.OBJECT.getDeclaration(),
 							MemberPredicates.NON_STATIC);
 				} else if (type instanceof IRFunctionType) {
 					final IRFunctionType functionType = (IRFunctionType) type;
 					members.add(FunctionMethod.apply.create(functionType));
 					members.add(FunctionMethod.call.create(functionType));
 					typeQuery.add(RTypes.FUNCTION.getDeclaration(),
 							new MemberPredicate() {
 								public boolean isCompatibleWith(
 										MemberPredicate predicate) {
 									return MemberPredicates.NON_STATIC == predicate;
 								}
 
 								public boolean evaluate(Member member) {
 									return MemberPredicates.NON_STATIC
 											.evaluate(member)
 											&& !FunctionMethod.apply
 													.test(member.getName())
 											&& !FunctionMethod.call.test(member
 													.getName());
 								}
 
 								public boolean evaluate(IRMember member) {
 									return member.getSource() instanceof Member
 											&& evaluate((Member) member
 													.getSource());
 								}
 							});
 				} else if (type instanceof IRArrayType) {
 					final IRArrayType arrayType = (IRArrayType) type;
 					typeQuery.add(
 							context.parameterize(Types.ARRAY, Collections
 									.singletonList(arrayType.getItemType())),
 							MemberPredicates.NON_STATIC);
 				} else if (type.isJavaScriptObject()) {
 					typeQuery.add(RTypes.OBJECT.getDeclaration(),
 							MemberPredicates.NON_STATIC);
 				}
 			}
 		}
 
 		private boolean canInstantiate(Type type, IValueReference ref) {
 			if (extensions != null) {
 				for (IValidatorExtension extension : extensions) {
 					final IValidationStatus status = extension.canInstantiate(
 							type, ref);
 					if (status != null && status != ValidationStatus.OK) {
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 
 		/**
 		 * @param member
 		 */
 		private void reportMember(IRMember member, String memberName,
 				boolean important) {
 			if (visibilityCheck && extensions != null
 					&& member.getSource() instanceof Member) {
 				final Member source = (Member) member.getSource();
 				for (IValidatorExtension extension : extensions) {
 					final IValidationStatus status = extension
 							.validateAccessibility(source);
 					if (status != null) {
 						if (status == ValidationStatus.OK) {
 							break;
 						} else {
 							return;
 						}
 					}
 				}
 			}
 			boolean isFunction = member instanceof IRMethod
 					|| (member instanceof IRRecordMember && member.getType() instanceof IRFunctionType);
 			CompletionProposal proposal = CompletionProposal.create(
 					isFunction ? CompletionProposal.METHOD_REF
 							: CompletionProposal.FIELD_REF, position);
 
 			int relevance = computeBaseRelevance();
 			// if (important) {
 			// relevance += RelevanceConstants.R_NON_INHERITED;
 			// }
 			relevance += computeRelevanceForInterestingProposal();
 			relevance += computeRelevanceForCaseMatching(prefix, memberName);
 			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
 			proposal.setRelevance(relevance);
 
 			proposal.setCompletion(isFunction ? memberName + "()" : memberName);
 			proposal.setName(memberName);
 			proposal.setExtraInfo(member.getSource());
 			proposal.setReplaceRange(startPosition - offset, endPosition
 					- offset);
 			if (isFunction) {
 				List<IRParameter> parameters = null;
 				if (member.getType() instanceof IRFunctionType) {
 					parameters = ((IRFunctionType) member.getType())
 							.getParameters();
 				} else {
 					parameters = ((IRMethod) member).getParameters();
 				}
 				int paramCount = parameters.size();
 				if (paramCount > 0) {
 					final String[] params = new String[paramCount];
 					for (int i = 0; i < paramCount; ++i) {
 						params[i] = parameters.get(i).getName();
 					}
 					proposal.setParameterNames(params);
 					if (parameters.get(paramCount - 1).getKind() != ParameterKind.NORMAL) {
 						int requiredCount = parameters.size();
 						while (requiredCount > 0
 								&& parameters
 										.get(requiredCount - 1).getKind() != ParameterKind.NORMAL) {
 							--requiredCount;
 						}
 						if (requiredCount == 0
 								&& parameters.get(requiredCount)
 										.getKind() == ParameterKind.VARARGS) {
 							++requiredCount; // heuristic...
 						}
 						if (requiredCount != paramCount) {
 							proposal.setAttribute(
 									CompletionProposal.ATTR_REQUIRED_PARAM_COUNT,
 									requiredCount);
 						}
 					}
 				}
 			}
 			requestor.accept(proposal);
 		}
 
 		/**
 		 * @param reference
 		 */
 		private void reportReference(IValueReference reference) {
 			int proposalKind = CompletionProposal.FIELD_REF;
 			final ReferenceKind kind = reference.getKind();
 			if (reference.getAttribute(IReferenceAttributes.PHANTOM, true) == null
 					&& (kind == ReferenceKind.FUNCTION || reference
 							.hasChild(IValueReference.FUNCTION_OP))) {
 				proposalKind = CompletionProposal.METHOD_REF;
 			} else if (kind == ReferenceKind.LOCAL) {
 				proposalKind = CompletionProposal.LOCAL_VARIABLE_REF;
 			}
 			CompletionProposal proposal = CompletionProposal.create(
 					proposalKind, position);
 
 			int relevance = computeBaseRelevance();
 			relevance += computeRelevanceForInterestingProposal();
 			relevance += computeRelevanceForCaseMatching(prefix,
 					reference.getName());
 			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
 			proposal.setRelevance(relevance);
 
 			proposal.setCompletion(proposalKind == CompletionProposal.METHOD_REF ? reference
 					.getName() + "()"
 					: reference.getName());
 			proposal.setName(reference.getName());
 			proposal.setExtraInfo(reference);
 			proposal.setReplaceRange(startPosition - offset, endPosition
 					- offset);
 			if (proposalKind == CompletionProposal.METHOD_REF) {
 				final IRMethod method = (IRMethod) reference.getAttribute(
 						IReferenceAttributes.R_METHOD, true);
 				if (method != null) {
 					int paramCount = method.getParameterCount();
 					if (paramCount > 0) {
 						final String[] params = new String[paramCount];
 						for (int i = 0; i < paramCount; ++i) {
 							params[i] = method.getParameters().get(i).getName();
 						}
 						proposal.setParameterNames(params);
 						if (method.getParameters().get(paramCount - 1)
 								.getKind() != ParameterKind.NORMAL) {
 							int requiredCount = method.getParameters().size();
 							while (requiredCount > 0
 									&& method.getParameters()
 											.get(requiredCount - 1).getKind() != ParameterKind.NORMAL) {
 								--requiredCount;
 							}
 							if (requiredCount == 0
 									&& method.getParameters()
 											.get(requiredCount).getKind() == ParameterKind.VARARGS) {
 								++requiredCount; // heuristic...
 							}
 							if (requiredCount != paramCount) {
 								proposal.setAttribute(
 										CompletionProposal.ATTR_REQUIRED_PARAM_COUNT,
 										requiredCount);
 							}
 						}
 					}
 				}
 			}
 			requestor.accept(proposal);
 		}
 
 		public void reportTypeRef(Type type) {
 			if (!processed.add(type.getName())) {
 				return;
 			}
 			CompletionProposal proposal = CompletionProposal.create(
 					CompletionProposal.TYPE_REF, position);
 			int relevance = computeBaseRelevance();
 			relevance += computeRelevanceForInterestingProposal();
 			relevance += computeRelevanceForCaseMatching(prefix, type.getName());
 			relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE);
 			proposal.setRelevance(relevance);
 			proposal.setCompletion(type.getName());
 			proposal.setName(type.getName());
 			proposal.setExtraInfo(type);
 			proposal.setReplaceRange(startPosition - offset, endPosition
 					- offset);
 			requestor.accept(proposal);
 		}
 
 	}
 
 	/**
 	 * @param context
 	 * @param collection
 	 * @param reporter
 	 * @param expressionContext
 	 */
 	private void doGlobalCompletion(IValueCollection collection,
 			Reporter reporter, ExpressionContext expressionContext) {
 		reportItems(reporter, collection);
 		if ((globalOptions & OPTION_GLOBALS) != 0) {
 			if (!requestor.isIgnored(CompletionProposal.TYPE_REF)) {
 				doCompletionOnType(TypeMode.CODE, reporter);
 			}
 			reportGlobals(reporter);
 		}
 		if ((globalOptions & OPTION_KEYWORDS) != 0) {
 			if (!requestor.isIgnored(CompletionProposal.KEYWORD)) {
 				doCompletionOnKeyword(reporter.getPrefix(),
 						reporter.getPosition(), expressionContext);
 			}
 		}
 	}
 
 	private void doCompletionOnKeyword(String prefix, int position,
 			ExpressionContext expressionContext) {
 		final String[] keywords;
 		if (expressionContext != null) {
 			if (expressionContext.expressionType == ExpressionType.PROPERTY_INITIALIZER_VALUE) {
 				keywords = JavaScriptKeywords.getJavaScriptValueKeywords();
 			} else {
 				keywords = JavaScriptKeywords.getJavaScriptExpressionKeywords();
 			}
 		} else {
 			keywords = JavaScriptKeywords.getJavaScriptKeywords();
 		}
 		findKeywords(prefix.toCharArray(), keywords, true);
 	}
 
 }
