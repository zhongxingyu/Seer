 package org.eclipse.dltk.internal.javascript.parser;
 
 import static org.eclipse.dltk.internal.javascript.validation.JavaScriptValidations.reportValidationStatus;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.annotations.Nullable;
 import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
 import org.eclipse.dltk.compiler.problem.IValidationStatus;
 import org.eclipse.dltk.compiler.problem.ValidationMultiStatus;
 import org.eclipse.dltk.compiler.problem.ValidationStatus;
 import org.eclipse.dltk.core.ISourceNode;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencer2;
 import org.eclipse.dltk.internal.javascript.validation.ValidationMessages;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.parser.JSProblemReporter;
 import org.eclipse.dltk.javascript.parser.jsdoc.JSDocTag;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinfo.IRType;
 import org.eclipse.dltk.javascript.typeinfo.ITypeCheck;
 import org.eclipse.dltk.javascript.typeinfo.ITypeChecker;
 import org.eclipse.dltk.javascript.typeinfo.ITypeCheckerExtension;
 import org.eclipse.dltk.javascript.typeinfo.ITypeInfoContext;
 import org.eclipse.dltk.javascript.typeinfo.RTypes;
 import org.eclipse.dltk.javascript.typeinfo.TypeUtil;
 import org.eclipse.dltk.javascript.typeinfo.model.AnyType;
 import org.eclipse.dltk.javascript.typeinfo.model.ArrayType;
 import org.eclipse.dltk.javascript.typeinfo.model.ClassType;
 import org.eclipse.dltk.javascript.typeinfo.model.FunctionType;
 import org.eclipse.dltk.javascript.typeinfo.model.GenericType;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 import org.eclipse.dltk.javascript.typeinfo.model.MapType;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.Parameter;
 import org.eclipse.dltk.javascript.typeinfo.model.ParameterizedType;
 import org.eclipse.dltk.javascript.typeinfo.model.RecordType;
 import org.eclipse.dltk.javascript.typeinfo.model.SimpleType;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeVariable;
 import org.eclipse.dltk.javascript.typeinfo.model.UndefinedType;
 import org.eclipse.dltk.javascript.typeinfo.model.UnionType;
 import org.eclipse.dltk.javascript.validation.IValidatorExtension;
 import org.eclipse.dltk.javascript.validation.IValidatorExtension2;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.osgi.util.NLS;
 
 public class JSDocValidatorFactory {
 
 	public static abstract class AbstractTypeChecker implements ITypeChecker {
 
 		private int defaults = DEFAULT;
 
 		public int getDefaults() {
 			return defaults;
 		}
 
 		public void setDefaults(int defaults) {
 			this.defaults = defaults;
 		}
 
 		public void checkType(JSType type, ISourceNode node) {
 			checkType(type, node, getDefaults());
 		}
 
 		public void checkType(JSType type, ISourceNode tag, int flags) {
 			if (type == null) {
 				return;
 			}
 			if (type instanceof UnionType) {
 				for (JSType targetType : ((UnionType) type).getTargets()) {
 					checkType(targetType, tag, flags);
 				}
 			} else if (type instanceof RecordType) {
 				for (Member member : ((RecordType) type).getMembers()) {
 					checkType(member.getType(), tag, flags);
 				}
 			} else if (type instanceof FunctionType) {
 				final FunctionType func = (FunctionType) type;
 				checkType(((FunctionType) type).getReturnType(), tag, flags);
 				for (Parameter parameter : func.getParameters()) {
 					checkType(parameter.getType(), tag, flags);
 				}
 			} else if (type instanceof AnyType || type instanceof UndefinedType) {
 				// OK
 			} else if (type instanceof ArrayType) {
 				checkType(((ArrayType) type).getItemType(), tag, flags);
 			} else if (type instanceof MapType) {
 				checkType(((MapType) type).getKeyType(), tag, flags);
 				checkType(((MapType) type).getValueType(), tag, flags);
 			} else if (type instanceof SimpleType) {
 				if (type instanceof ParameterizedType) {
 					final ParameterizedType parameterized = (ParameterizedType) type;
 					for (JSType param : parameterized.getActualTypeArguments()) {
 						checkType(param, tag, flags);
 					}
 					checkType(((SimpleType) type).getTarget(), tag, flags,
 							new ITypeCheck[] { new ParameterizedTypeCheck(
 									parameterized) });
 				} else {
 					checkType(((SimpleType) type).getTarget(), tag, flags, null);
 				}
 			} else if (type instanceof ClassType) {
 				final Type t = ((ClassType) type).getTarget();
 				if (t == null) {
 					return;
 				}
 				checkType(t, tag, flags, null);
 			}
 		}
 
 		public abstract void checkType(Type type, ISourceNode tag, int flags,
 				@Nullable ITypeCheck[] checks);
 
 	}
 
 	private static class ParameterizedTypeCheck implements ITypeCheck {
 		private final ParameterizedType parameterizedType;
 
 		public ParameterizedTypeCheck(ParameterizedType parameterizedType) {
 			this.parameterizedType = parameterizedType;
 		}
 
 		public IValidationStatus checkType(ITypeInfoContext context, Type type) {
 			if (type instanceof GenericType) {
 				final GenericType genericType = (GenericType) type;
 				final EList<TypeVariable> typeVariables = genericType
 						.getTypeParameters();
 				final int typeVariableCount = typeVariables.size();
 				if (typeVariableCount != parameterizedType
 						.getActualTypeArguments().size()) {
 					return new ValidationStatus(
 							JavaScriptProblems.PARAMETERIZED_TYPE_INCORRECT_ARGUMENTS,
 							NLS.bind(
 									ValidationMessages.IncorrectNumberOfTypeArguments,
 									type.getName()));
 				}
 				List<ValidationStatus> statuses = null;
 				for (int i = 0; i < typeVariableCount; ++i) {
 					final TypeVariable variable = typeVariables.get(i);
 					if (variable.getBound() != null) {
 						final IRType bound = RTypes.create(context,
 								variable.getBound());
						final IRType actual = RTypes.create(
								context,
								parameterizedType.getActualTypeArguments().get(
										i));
 						if (!bound.isAssignableFrom(actual).ok()) {
 							if (statuses == null) {
 								statuses = new ArrayList<ValidationStatus>();
 							}
 							statuses.add(new ValidationStatus(
 									JavaScriptProblems.PARAMETERIZED_TYPE_INCORRECT_ARGUMENTS,
 									NLS.bind(
 											ValidationMessages.ParameterizedBoundMismatch,
 											new Object[] { actual,
 													variable.getName(), bound,
 													genericType.getName() })));
 						}
 					}
 				}
 				return ValidationMultiStatus.of(statuses);
 			} else {
 				return new ValidationStatus(
 						JavaScriptProblems.NOT_GENERIC_TYPE, NLS.bind(
 								ValidationMessages.NotGenericType,
 								type.getName()));
 			}
 		}
 	}
 
 	public static class TypeChecker extends AbstractTypeChecker implements
 			ITypeCheckerExtension {
 
 		private final List<QueueItem> queue = new ArrayList<QueueItem>();
 		private final TypeInferencer2 context;
 		private final JSProblemReporter reporter;
 
 		public TypeChecker(TypeInferencer2 context, JSProblemReporter reporter) {
 			this.context = context;
 			this.reporter = reporter;
 		}
 
 		@Override
 		public void checkType(JSType type, ISourceNode tag, int flags) {
 			if (extensions != null) {
 				for (IValidatorExtension extension : extensions) {
 					if (extension instanceof IValidatorExtension2) {
 						final IValidationStatus status = ((IValidatorExtension2) extension)
 								.validateTypeExpression(type);
 						if (status != null && status != ValidationStatus.OK) {
 							reportValidationStatus(reporter, status, tag,
 									JavaScriptProblems.INACCESSIBLE_TYPE,
 									ValidationMessages.InaccessibleType,
 									type.getName());
 							return;
 						}
 					}
 				}
 			}
 			super.checkType(type, tag, flags);
 		}
 
 		@Override
 		public void checkType(Type type, ISourceNode tag, int flags,
 				@Nullable ITypeCheck[] checks) {
 			if (type.getKind() == TypeKind.UNKNOWN
 					|| type.getKind() == TypeKind.UNRESOLVED) {
 				queue.add(new QueueItem(type, tag, context.currentCollection(),
 						flags, checks));
 			} else {
 				checkDeprecatedType(type, tag);
 				if (checks != null) {
 					doChecks(type, tag, checks);
 				}
 			}
 		}
 
 		public void validate() {
 			for (QueueItem item : queue) {
 				doCheckType(context.resolveType(item.type), item.tag,
 						item.flags, item.checks, item.collection);
 			}
 		}
 
 		private void doCheckType(Type type, ISourceNode tag, int flags,
 				@Nullable ITypeCheck[] checks, IValueCollection collection) {
 			if (type.eIsProxy()) {
 				Assert.isTrue(!type.eIsProxy(), "Type \"" + type.getName()
 						+ "\" is a proxy");
 			}
 			if (type.getKind() == TypeKind.UNKNOWN) {
 				if ((flags & LOCAL_TYPES) != 0 && collection != null) {
 					if (collection.getChild(type.getName()).exists()) {
 						return;
 					}
 					// if it still is not found, test if it is a
 					// "package type"
 					// an try to resolve that to a existing child.
 					String className = type.getName();
 					if (className.indexOf('.') != -1) {
 						StringTokenizer st = new StringTokenizer(className, ".");
 						IValueReference child = null;
 						while (st.hasMoreTokens()) {
 							String token = st.nextToken();
 							if (child == null)
 								child = collection.getChild(token);
 							else {
 								if (child.getKind() == ReferenceKind.FUNCTION) {
 									IValueCollection function = (IValueCollection) child
 											.getAttribute(IReferenceAttributes.FUNCTION_SCOPE);
 									if (function != null)
 										child = function.getThis();
 								}
 								child = child.getChild(token);
 							}
 							if (!child.exists()) {
 								child = null;
 								break;
 							}
 						}
 						// if the child is not null, it was found.
 						if (child != null)
 							return;
 					}
 				}
 				reportUnknownType(tag, TypeUtil.getName(type));
 			} else {
 				checkDeprecatedType(type, tag);
 				if (checks != null) {
 					doChecks(type, tag, checks);
 				}
 			}
 		}
 
 		private void doChecks(Type type, ISourceNode tag, ITypeCheck[] checks) {
 			for (ITypeCheck check : checks) {
 				final IValidationStatus status = check.checkType(context, type);
 				if (status != null && status != ValidationStatus.OK) {
 					reportValidationStatus(reporter, status, tag,
 							JavaScriptProblems.INACCESSIBLE_TYPE,
 							ValidationMessages.InaccessibleType, type.getName());
 				}
 			}
 		}
 
 		private void checkDeprecatedType(Type type, ISourceNode tag) {
 			if (type.isDeprecated()) {
 				reporter.reportProblem(
 						JavaScriptProblems.DEPRECATED_TYPE,
 						NLS.bind(ValidationMessages.DeprecatedType,
 								TypeUtil.getName(type)), tag.start(), tag.end());
 				return;
 			} else if (extensions != null) {
 				for (IValidatorExtension extension : extensions) {
 					if (extension instanceof IValidatorExtension2) {
 						final IValidationStatus status = ((IValidatorExtension2) extension)
 								.validateAccessibility(type);
 						if (status != null && status != ValidationStatus.OK) {
 							reportValidationStatus(reporter, status, tag,
 									JavaScriptProblems.INACCESSIBLE_TYPE,
 									ValidationMessages.InaccessibleType,
 									type.getName());
 							return;
 						}
 					}
 				}
 			}
 		}
 
 		public void reportUnknownType(ISourceNode tag, String name) {
 			reportUnknownType(JavaScriptProblems.UNKNOWN_TYPE, tag, name);
 		}
 
 		public void reportUnknownType(IProblemIdentifier identifier,
 				ISourceNode node, String name) {
 			int start;
 			int end;
 			if (node instanceof JSDocTag) {
 				final JSDocTag tag = (JSDocTag) node;
 				end = tag.end();
 				start = end - tag.value().length();
 				int index = tag.value().indexOf('{');
 				if (index != -1) {
 					int index2 = tag.value().indexOf('}', index);
 					if (index2 != -1) {
 						start = start + index + 1;
 						end = start + index2 - 1;
 					}
 				}
 			} else {
 				start = node.start();
 				end = node.end();
 			}
 			reporter.reportProblem(identifier,
 					NLS.bind(ValidationMessages.UnknownType, name), start, end);
 		}
 
 		private IValidatorExtension[] extensions;
 
 		public void setExtensions(IValidatorExtension[] extensions) {
 			this.extensions = extensions;
 		}
 	}
 
 	private static class QueueItem {
 
 		final Type type;
 		final ISourceNode tag;
 		final IValueCollection collection;
 		final int flags;
 		@Nullable
 		final ITypeCheck[] checks;
 
 		public QueueItem(Type type, ISourceNode tag,
 				IValueCollection collection, int flags,
 				@Nullable ITypeCheck[] checks) {
 			this.type = type;
 			this.tag = tag;
 			this.collection = collection;
 			this.flags = flags;
 			this.checks = checks;
 		}
 
 	}
 
 }
