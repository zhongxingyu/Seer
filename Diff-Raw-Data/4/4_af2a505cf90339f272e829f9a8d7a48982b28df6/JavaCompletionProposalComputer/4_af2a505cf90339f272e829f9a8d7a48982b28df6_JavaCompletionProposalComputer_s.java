 /*-
  * Copyright (C) 2011-2012 by Iwao AVE!
  * This program is made available under the terms of the MIT License.
  */
 
 package org.eclipselabs.stlipse.javaeditor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.CompletionContext;
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.IAnnotatable;
 import org.eclipse.jdt.core.IAnnotation;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMemberValuePair;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.ISourceRange;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.Signature;
 import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
 import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
 import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipselabs.stlipse.Activator;
 import org.eclipselabs.stlipse.cache.BeanPropertyCache;
 import org.eclipselabs.stlipse.cache.BeanPropertyVisitor;
 
 /**
  * @author Iwao AVE!
  */
 public class JavaCompletionProposalComputer implements IJavaCompletionProposalComputer
 {
 	private static String VALIDATE_NESTED = "ValidateNestedProperties";
 
 	private static String VALIDATE = "Validate";
 
 	private static String STRICT_BINDING = "StrictBinding";
 
 	private static String AFTER = "After";
 
 	private static String BEFORE = "Before";
 
 	private static String VALIDATION_METHOD = "ValidationMethod";
 
 	private static String WIZARD = "Wizard";
 
 	public void sessionStarted()
 	{
 		// Nothing todo for now.
 	}
 
 	public List<ICompletionProposal> computeCompletionProposals(
 		ContentAssistInvocationContext context, IProgressMonitor monitor)
 	{
 		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
 		if (context instanceof JavaContentAssistInvocationContext)
 		{
 			JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext)context;
 			CompletionContext coreContext = javaContext.getCoreContext();
 			ICompilationUnit unit = javaContext.getCompilationUnit();
 			try
 			{
 				if (unit != null && unit.isStructureKnown())
 				{
 					int offset = javaContext.getInvocationOffset();
 					IJavaElement element = unit.getElementAt(offset);
 					if (element != null && element instanceof IAnnotatable)
 					{
 						ValueInfo valueInfo = scanAnnotation(offset, (IAnnotatable)element);
 						if (valueInfo != null)
 						{
 							int replacementLength = valueInfo.getValueLength();
 							IJavaProject project = javaContext.getProject();
 							String beanFqn = unit.getType(element.getParent().getElementName())
 								.getFullyQualifiedName();
 							if (valueInfo.isField())
 							{
 								if (valueInfo.isPropertyOmmitted())
 								{
 									StringBuilder matchStr = resolveBeanPropertyName(element);
 									if (matchStr.length() > 0)
 									{
 										char[] token = coreContext.getToken();
 										matchStr.append('.').append(token);
 										Map<String, String> fields = BeanPropertyCache.searchFields(project,
 											beanFqn, matchStr.toString(), false, -1, false, unit);
 										proposals.addAll(BeanPropertyCache.buildFieldNameProposal(fields,
 											String.valueOf(token), coreContext.getTokenStart() + 1, replacementLength));
 									}
 								}
 								else
 								{
 									String input = String.valueOf(coreContext.getToken());
 									Map<String, String> fields = BeanPropertyCache.searchFields(project, beanFqn,
 										input, false, -1, false, unit);
 									proposals.addAll(BeanPropertyCache.buildFieldNameProposal(fields, input,
 										coreContext.getTokenStart() + 1, replacementLength));
 								}
 							}
 							else if (valueInfo.isEventHandler())
 							{
 								String input = String.valueOf(coreContext.getToken());
 								boolean isNot = false;
 								if (input.length() > 0 && input.startsWith("!"))
 								{
 									isNot = true;
 									input = input.length() > 1 ? input.substring(1) : "";
 								}
 								List<String> events = BeanPropertyCache.searchEventHandler(project, beanFqn,
 									input, false, false);
 								int relevance = events.size();
 								for (String event : events)
 								{
 									String replaceStr = isNot ? "!" + event : event;
 									ICompletionProposal proposal = new JavaCompletionProposal(replaceStr,
 										coreContext.getTokenStart() + 1, replacementLength, replaceStr.length(),
 										Activator.getIcon(), event, null, null, relevance--);
 									proposals.add(proposal);
 								}
 							}
 						}
 					}
 				}
 			}
 			catch (JavaModelException e)
 			{
 				Activator.log(Status.ERROR, "Something went wrong.", e);
 			}
 		}
 		return proposals;
 	}
 
 	private StringBuilder resolveBeanPropertyName(IJavaElement element) throws JavaModelException
 	{
 		StringBuilder result = new StringBuilder();
 		int elementType = element.getElementType();
 		String elementName = element.getElementName();
 		if (elementType == IJavaElement.FIELD)
 		{
 			result.append(elementName);
 		}
 		else if (elementType == IJavaElement.METHOD)
 		{
 			IMethod method = (IMethod)element;
 			if (Flags.isPublic(method.getFlags()) && (isSetter(method) || isGetter(method)))
 			{
 				result.append(BeanPropertyVisitor.getFieldNameFromAccessor(elementName));
 			}
 		}
 		return result;
 	}
 
 	private boolean isSetter(IMethod method) throws JavaModelException
 	{
 		String name = method.getElementName();
 		return isVoid(method) && name.startsWith("set") && name.length() > 3;
 	}
 
 	private boolean isGetter(IMethod method) throws JavaModelException
 	{
 		String name = method.getElementName();
 		return !isVoid(method)
 			&& ((name.startsWith("get") && name.length() > 3) || name.startsWith("is")
 				&& name.length() > 2);
 	}
 
 	private boolean isVoid(IMethod method) throws JavaModelException
 	{
 		return String.valueOf(Signature.C_VOID).equals(method.getReturnType());
 	}
 
 	private ValueInfo scanAnnotation(int offset, IAnnotatable annotatable)
 		throws JavaModelException
 	{
 		IAnnotation[] annotations = annotatable.getAnnotations();
 		for (IAnnotation annotation : annotations)
 		{
 			ISourceRange sourceRange = annotation.getSourceRange();
 			if (isInRange(sourceRange, offset))
 			{
 				String annotationName = annotation.getElementName();
 				if (VALIDATE_NESTED.equals(annotationName))
 				{
 					// parse nested @Validate
 					IMemberValuePair[] valuePairs = annotation.getMemberValuePairs();
 					for (IMemberValuePair valuePair : valuePairs)
 					{
 						if ("value".equals(valuePair.getMemberName())
 							&& valuePair.getValueKind() == IMemberValuePair.K_ANNOTATION)
 						{
 							// the value is an array of IAnnotation
 							Object[] validates = (Object[])valuePair.getValue();
 							for (Object validate : validates)
 							{
 								IAnnotation validateAnnotation = (IAnnotation)validate;
 								ISourceRange validateSourceRange = validateAnnotation.getSourceRange();
 								if (isInRange(validateSourceRange, offset))
 									return parseAnnotation(offset, validateAnnotation, validateSourceRange);
 							}
 						}
 					}
 				}
 				else if (isNonNestedSupportedAnnotation(annotationName))
 				{
 					return parseAnnotation(offset, annotation, sourceRange);
 				}
 			}
 		}
 		return null;
 	}
 
 	private static boolean isNonNestedSupportedAnnotation(String annotationName)
 	{
 		final List<String> annotations = Arrays.asList(VALIDATE, STRICT_BINDING, BEFORE, AFTER,
 			VALIDATION_METHOD, WIZARD);
 		return annotations.contains(annotationName);
 	}
 
 	private ValueInfo parseAnnotation(int offset, IAnnotation annotation, ISourceRange sourceRange)
 		throws JavaModelException
 	{
 		int annotationOffset = sourceRange.getOffset();
 		String source = annotation.getSource();
 		int index = source.indexOf('(', annotation.getElementName().length() + 1);
 		if (index > -1)
 		{
 			int stringValueLength = 0;
 			boolean scanningName = true;
 			boolean scanningValue = false;
 			boolean inStringValue = false;
 			boolean inArrayValue = false;
 			boolean escaped = false;
 			StringBuilder optionName = new StringBuilder();
 			for (index++; index < source.length(); index++)
 			{
 				char c = source.charAt(index);
 				if (scanningName)
 				{
 					if (c == '=')
 						scanningName = false;
 					else if (c != ' ')
 						optionName.append(c);
 				}
 				else if (!scanningValue)
 				{
 					if (c != ' ')
 					{
 						scanningValue = true;
 						if (c == '"')
 						{
 							inStringValue = true;
 							stringValueLength = 0;
 						}
 						else if (c == '{')
 							inArrayValue = true;
 					}
 				}
 				else
 				{
 					// scanning value part
 					if (inStringValue)
 					{
 						if (escaped)
 							; // ignore the escaped character
 						else if (c == '\\')
 							escaped = true;
 						else if (c == '"')
 						{
 							if (annotationOffset + index >= offset)
 								break;
 							inStringValue = false;
 							stringValueLength = 0;
 						}
 						if (inStringValue)
 							stringValueLength++;
 					}
 					else if (inArrayValue)
 					{
 						if (c == '"')
 							inStringValue = true;
 						else if (c == '}')
 							inArrayValue = false;
 						else
 							; // ignore
 					}
 					else if (c == ',')
 					{
 						scanningValue = false;
 						scanningName = true;
 						optionName.setLength(0);
 					}
 				}
 			}
 			if (optionName.length() > 0)
 			{
 				return new ValueInfo(annotation.getElementName(), optionName.toString(),
 					stringValueLength);
 			}
 		}
 		return null;
 	}
 
 	private boolean isInRange(ISourceRange sourceRange, int offset)
 	{
 		int start = sourceRange.getOffset();
 		int end = start + sourceRange.getLength();
 		return start <= offset && offset <= end;
 	}
 
 	public List<IContextInformation> computeContextInformation(
 		ContentAssistInvocationContext context, IProgressMonitor monitor)
 	{
		return null;
 	}
 
 	public String getErrorMessage()
 	{
 		return null;
 	}
 
 	public void sessionEnded()
 	{
 		// Nothing todo for now.
 	}
 
 	static class ValueInfo
 	{
 		private String annotationName;
 
 		private String attributeName;
 
 		private int valueLength;
 
 		public ValueInfo(String annotationName, String attributeName, int valueLength)
 		{
 			super();
 			this.annotationName = annotationName;
 			this.attributeName = attributeName;
 			this.valueLength = valueLength;
 		}
 
 		public boolean isField()
 		{
 			return (STRICT_BINDING.equals(annotationName) && ("allow".equals(attributeName) || "deny".equals(attributeName)))
 				|| (VALIDATE.equals(annotationName) && "field".equals(attributeName));
 		}
 
 		public boolean isPropertyOmmitted()
 		{
 			return !STRICT_BINDING.equals(annotationName);
 		}
 
 		public boolean isEventHandler()
 		{
 			return (("on".equals(attributeName) && (VALIDATE.equals(annotationName)
 				|| AFTER.equals(annotationName) || BEFORE.equals(annotationName) || VALIDATION_METHOD.equals(annotationName))))
 				|| ("startEvents".equals(attributeName) && WIZARD.equals(annotationName));
 		}
 
 		public String getAnnotationName()
 		{
 			return annotationName;
 		}
 
 		public String getAttributeName()
 		{
 			return attributeName;
 		}
 
 		public int getValueLength()
 		{
 			return valueLength;
 		}
 	}
 }
