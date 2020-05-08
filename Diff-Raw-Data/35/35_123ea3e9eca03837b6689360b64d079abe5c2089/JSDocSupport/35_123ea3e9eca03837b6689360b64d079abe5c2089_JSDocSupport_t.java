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
 package org.eclipse.dltk.internal.javascript.ti;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.Comment;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.VariableStatement;
 import org.eclipse.dltk.javascript.parser.JSParser;
 import org.eclipse.dltk.javascript.parser.jsdoc.JSDocTag;
 import org.eclipse.dltk.javascript.parser.jsdoc.JSDocTags;
 import org.eclipse.dltk.javascript.parser.jsdoc.SimpleJSDocParser;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder;
 import org.eclipse.dltk.javascript.typeinfo.ITypeInfoContext;
 import org.eclipse.dltk.javascript.typeinfo.model.Property;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelFactory;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelLoader;
 
 /**
  * Implements support for javadocs tags .
  * 
  * @see http://jsdoc.sourceforge.net/
  * @see http://code.google.com/p/jsdoc-toolkit/wiki/TagType
  * @see http://code.google.com/p/jsdoc-toolkit/wiki/TagParam
  */
 public class JSDocSupport implements IModelBuilder {
 
 	private static final String DOTS = "...";
 
 	private static final String ARRAY_SUFFIX = "[]";
 
 	public static String[] getTags() {
 		return new String[] { JSDocTag.DEPRECATED, JSDocTag.PARAM,
 				JSDocTag.TYPE, JSDocTag.RETURN, JSDocTag.RETURNS,
 				JSDocTag.PRIVATE, JSDocTag.CONSTRUCTOR };
 	}
 
 	public String getFeatureId() {
 		return JSDocSupport.class.getName();
 	}
 
 	public int priorityFor(ITypeInfoContext context) {
 		return PRIORITY_DEFAULT;
 	}
 
 	public JSDocTags parse(Comment comment) {
 		return new SimpleJSDocParser().parse(comment.getText(),
 				comment.sourceStart());
 	}
 
 	public void processMethod(FunctionStatement statement, IMethod method,
 			IProblemReporter reporter) {
 		Comment comment = getFunctionComment(statement);
 		if (comment == null) {
 			return;
 		}
 		final JSDocTags tags = parse(comment);
 		if (method.getType() == null) {
 			parseType(method, tags, RETURN_TAGS, reporter);
 		}
 		parseParams(method, tags, reporter);
 		parseDeprecation(method, tags, reporter);
 		parsePrivate(method, tags, reporter);
 		parseConstructor(method, tags, reporter);
 	}
 
 	public static Comment getFunctionComment(FunctionStatement statement) {
 		Comment documentation = statement.getDocumentation();
 		if (documentation != null) {
 			return documentation;
 		}
 		if (statement.getParent() instanceof BinaryOperation) {
 			final BinaryOperation binary = (BinaryOperation) statement
 					.getParent();
 			if (binary.getOperation() == JSParser.ASSIGN
 					&& binary.getRightExpression() == statement) {
 				documentation = binary.getLeftExpression().getDocumentation();
 				if (documentation != null) {
 					return documentation;
 				}
 			}
 		} else if (statement.getParent() instanceof PropertyInitializer) {
 			final PropertyInitializer property = (PropertyInitializer) statement
 					.getParent();
 			if (property.getValue() == statement) {
 				documentation = property.getName().getDocumentation();
 				if (documentation != null) {
 					return documentation;
 				}
 			}
 		}
 		return null;
 	}
 
 	private void parseConstructor(IMethod method, JSDocTags tags,
 			IProblemReporter reporter) {
 		if (tags.get(JSDocTag.CONSTRUCTOR) != null) {
 			method.setConstructor(true);
 			validateSingleTag(tags, JSDocTag.CONSTRUCTOR, reporter);
 		}
 	}
 
 	/**
 	 * @param method
 	 * @param tags
 	 */
 	private void parsePrivate(IMember member, final JSDocTags tags,
 			IProblemReporter reporter) {
 		if (tags.get(JSDocTag.PRIVATE) != null) {
 			member.setPrivate(true);
 			validateSingleTag(tags, JSDocTag.PRIVATE, reporter);
 		}
 	}
 
 	private void validateSingleTag(JSDocTags tags, String tagName,
 			IProblemReporter reporter) {
 		if (reporter != null && tags.count(tagName) > 1) {
 			final List<JSDocTag> t = tags.list(tagName);
 			for (JSDocTag tag : t.subList(1, t.size())) {
 				reportProblem(reporter, JSDocProblem.REPEATED_TAG, tag);
 			}
 		}
 	}
 
 	public void processVariable(VariableStatement statement,
 			IVariable variable, IProblemReporter reporter) {
 		final Comment comment = statement.getDocumentation();
 		if (comment == null) {
 			return;
 		}
 		final JSDocTags tags = parse(comment);
 		if (variable.getType() == null) {
 			parseType(variable, tags, TYPE_TAGS, reporter);
 		}
 		parseDeprecation(variable, tags, reporter);
 		parsePrivate(variable, tags, reporter);
 	}
 
 	private void parseDeprecation(IMember member, JSDocTags tags,
 			IProblemReporter reporter) {
 		if (tags.get(JSDocTag.DEPRECATED) != null) {
 			member.setDeprecated(true);
 			validateSingleTag(tags, JSDocTag.DEPRECATED, reporter);
 		}
 	}
 
 	protected void parseParams(IMethod method, JSDocTags tags,
 			IProblemReporter reporter) {
 		final Map<String, Type> objectPropertiesTypes = new HashMap<String, Type>();
 		final Set<String> processedParams = new HashSet<String>();
 		for (JSDocTag tag : tags.list(JSDocTag.PARAM)) {
 			String type = null;
 			boolean varargs = false;
 			boolean optional = false;
 			final Tokenizer st = new Tokenizer(tag.getValue());
 			if (st.hasMoreTokens()) {
 				final String token = st.peek();
 				if (token.startsWith("{") && token.endsWith("}")) {
 					type = token.substring(1, token.length() - 1);
 					if (type.startsWith(DOTS)) {
 						varargs = true;
 						type = type.substring(DOTS.length());
 					} else if (type.endsWith("=")) {
 						type = type.substring(type.length() - 1);
 						optional = true;
 					}
 					st.nextToken();
 				}
 			}
 			if (st.hasMoreTokens()) {
 				String paramName = st.nextToken();
 				if (paramName.startsWith("[") && paramName.endsWith("]")) {
 					optional = true;
 					paramName = paramName.substring(1, paramName.length() - 1);
 					int defaultValueSeperatorIndex = paramName.indexOf('=');
 					if (defaultValueSeperatorIndex != -1) {
 						paramName = paramName.substring(0,
 								defaultValueSeperatorIndex);
 					}
 				} else {
 					String propertyName = null;
 					int propertiesObjectIndex = paramName.indexOf('.');
 					if (propertiesObjectIndex != -1) {
 						// http://code.google.com/p/jsdoc-toolkit/wiki/TagParam
 						// = Parameters With Properties =
 						propertyName = paramName
 								.substring(propertiesObjectIndex + 1);
 						paramName = paramName.substring(0,
 								propertiesObjectIndex);
 						Type propertiesType = objectPropertiesTypes
 								.get(paramName);
 						if (propertiesType == null) {
 							propertiesType = TypeInfoModelFactory.eINSTANCE
 									.createType();
 							objectPropertiesTypes
 									.put(paramName, propertiesType);
 							final IParameter param = method
 									.getParameter(paramName);
 							if (param != null) {
 								param.setPropertiesType(propertiesType);
 							}
 						}
 						Property property = TypeInfoModelFactory.eINSTANCE
 								.createProperty();
 						property.setName(propertyName);
 						propertiesType.getMembers().add(property);
 					}
 					if (propertyName == null) {
 						if (!processedParams.add(paramName)) {
 							reportProblem(reporter,
 									JSDocProblem.DUPLICATE_PARAM, tag,
 									paramName);
 							continue;
 						}
 					}
				}
				final IParameter parameter = method.getParameter(paramName);
				if (parameter != null) {
					if (type != null && parameter.getType() == null) {
						parameter.setType(translateTypeName(type));
 					}
					if (!optional && st.hasMoreTokens()
							&& st.nextToken().equals("optional"))
						optional = true;
					parameter.setOptional(optional);
					parameter.setVarargs(varargs);
				} else {
					reportProblem(reporter, JSDocProblem.UNKNOWN_PARAM, tag,
							paramName);
 				}
 			} else {
 				reportProblem(reporter, JSDocProblem.MISSING_PARAMETER_NAME,
 						tag);
 			}
 		}
 	}
 
 	private static final String[] RETURN_TAGS = { JSDocTag.RETURNS,
 			JSDocTag.RETURN, JSDocTag.TYPE };
 
 	public static final String[] TYPE_TAGS = { JSDocTag.TYPE };
 
 	private static class Tokenizer {
 
 		private final StringTokenizer st;
 
 		public Tokenizer(String content) {
 			st = new StringTokenizer(content);
 		}
 
 		private boolean hasCurrent = false;
 		private String current;
 
 		public boolean hasMoreTokens() {
 			if (hasCurrent) {
 				return true;
 			}
 			return st.hasMoreTokens();
 		}
 
 		public String peek() {
 			if (!hasCurrent) {
 				if (st.hasMoreElements()) {
 					hasCurrent = true;
 					current = st.nextToken();
 				} else {
 					return null;
 				}
 			}
 			return current;
 		}
 
 		public String nextToken() {
 			if (hasCurrent) {
 				hasCurrent = false;
 				return current;
 			}
 			return st.nextToken();
 		}
 
 	}
 
 	/**
 	 * @see St
 	 * @param context
 	 * @param member
 	 * @param comment
 	 */
 	public void parseType(IElement member, JSDocTags tags, String[] tagNames,
 			IProblemReporter reporter) {
 		final JSDocTag tag = tags.get(tagNames);
 		if (tag != null) {
 			if (reporter != null) {
 				final int count = tags.count(tagNames);
 				if (count > 1) {
 					for (JSDocTag t : tags.list(tagNames).subList(1, count)) {
 						reportProblem(reporter, JSDocProblem.DUPLICATE_TYPE, t);
 					}
 				}
 			}
 			final Tokenizer st = new Tokenizer(tag.getValue());
 			if (st.hasMoreTokens()) {
 				member.setType(translateTypeName(st.nextToken()));
 			}
 		}
 	}
 
 	private void reportProblem(IProblemReporter reporter,
 			IProblemIdentifier problemIdentifier, JSDocTag tag, Object... args) {
 		if (reporter != null) {
 			reporter.reportProblem(problemIdentifier,
 					problemIdentifier.formatMessage(args), tag.getStart(),
 					tag.getEnd());
 		}
 	}
 
 	private String translateTypeName(String typeName) {
 		final int length = typeName.length();
 		if (length > 2 && typeName.charAt(0) == '{'
 				&& typeName.charAt(length - 1) == '}') {
 			typeName = typeName.substring(1, length - 1);
 		}
 		if (typeName.endsWith(ARRAY_SUFFIX)) {
 			return "Array<"
 					+ TypeInfoModelLoader.getInstance().translateTypeName(
 							typeName.substring(0, typeName.length()
 									- ARRAY_SUFFIX.length())) + ">";
 		}
 		return TypeInfoModelLoader.getInstance().translateTypeName(typeName);
 	}
 }
