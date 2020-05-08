 /*
  * Copyright 2010-2012 JetBrains s.r.o.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.napile.idea.plugin.parameterInfo;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.jet.cli.jvm.compiler.TipsManager;
 import org.napile.compiler.lang.descriptors.ClassDescriptor;
 import org.napile.compiler.lang.descriptors.ConstructorDescriptor;
 import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
 import org.napile.compiler.lang.descriptors.FunctionDescriptor;
 import org.napile.compiler.lang.descriptors.ValueParameterDescriptor;
 import org.napile.compiler.lang.psi.*;
 import org.napile.compiler.lang.resolve.BindingContext;
 import org.napile.compiler.lang.resolve.BindingContextUtils;
 import org.napile.compiler.lang.resolve.JetVisibilityChecker;
 import org.napile.compiler.lang.resolve.name.Name;
 import org.napile.compiler.lang.resolve.scopes.JetScope;
 import org.napile.compiler.lang.types.JetType;
 import org.napile.compiler.lang.types.checker.JetTypeChecker;
 import org.napile.compiler.lexer.JetTokens;
 import org.napile.idea.plugin.project.AnalyzeSingleFileUtil;
 import org.napile.compiler.resolve.DescriptorRenderer;
 import com.intellij.codeInsight.CodeInsightBundle;
 import com.intellij.codeInsight.lookup.LookupElement;
 import com.intellij.lang.ASTNode;
 import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
 import com.intellij.lang.parameterInfo.ParameterInfoContext;
 import com.intellij.lang.parameterInfo.ParameterInfoHandlerWithTabActionSupport;
 import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
 import com.intellij.lang.parameterInfo.ParameterInfoUtils;
 import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiReference;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.util.ArrayUtil;
 
 /**
  * User: Alefas
  * Date: 17.01.12
  */
 public class JetFunctionParameterInfoHandler implements ParameterInfoHandlerWithTabActionSupport<NapileValueArgumentList, Object, NapileValueArgument>
 {
 	public final static Color GREEN_BACKGROUND = new Color(231, 254, 234);
 
 	@NotNull
 	@Override
 	public NapileValueArgument[] getActualParameters(@NotNull NapileValueArgumentList arguments)
 	{
 		List<NapileValueArgument> argumentList = arguments.getArguments();
 		return argumentList.toArray(new NapileValueArgument[argumentList.size()]);
 	}
 
 	@NotNull
 	@Override
 	public IElementType getActualParameterDelimiterType()
 	{
 		return JetTokens.COMMA;
 	}
 
 	@NotNull
 	@Override
 	public IElementType getActualParametersRBraceType()
 	{
 		return JetTokens.RBRACE;
 	}
 
 	@NotNull
 	@Override
 	public Set<Class> getArgumentListAllowedParentClasses()
 	{
 		return Collections.singleton((Class) NapileCallElement.class);
 	}
 
 	@NotNull
 	@Override
 	public Set<? extends Class> getArgListStopSearchClasses()
 	{
 		return Collections.singleton(NapileMethod.class);
 	}
 
 	@NotNull
 	@Override
 	public Class<NapileValueArgumentList> getArgumentListClass()
 	{
 		return NapileValueArgumentList.class;
 	}
 
 	@Override
 	public boolean couldShowInLookup()
 	{
 		return true;
 	}
 
 	@Override
 	public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context)
 	{
 		return ArrayUtil.EMPTY_OBJECT_ARRAY; //todo: ?
 	}
 
 	@Override
 	public Object[] getParametersForDocumentation(Object p, ParameterInfoContext context)
 	{
 		return ArrayUtil.EMPTY_OBJECT_ARRAY; //todo: ?
 	}
 
 	@Override
 	public NapileValueArgumentList findElementForParameterInfo(CreateParameterInfoContext context)
 	{
 		return findCall(context);
 	}
 
 	@Override
 	public void showParameterInfo(@NotNull NapileValueArgumentList element, CreateParameterInfoContext context)
 	{
 		context.showHint(element, element.getTextRange().getStartOffset(), this);
 	}
 
 	@Override
 	public NapileValueArgumentList findElementForUpdatingParameterInfo(UpdateParameterInfoContext context)
 	{
 		return findCallAndUpdateContext(context);
 	}
 
 	@Override
 	public void updateParameterInfo(@NotNull NapileValueArgumentList argumentList, UpdateParameterInfoContext context)
 	{
 		if(context.getParameterOwner() != argumentList)
 			context.removeHint();
 		int offset = context.getOffset();
 		ASTNode child = argumentList.getNode().getFirstChildNode();
 		int i = 0;
 		while(child != null && child.getStartOffset() < offset)
 		{
 			if(child.getElementType() == JetTokens.COMMA)
 				++i;
 			child = child.getTreeNext();
 		}
 		context.setCurrentParameter(i);
 	}
 
 	@Override
 	public String getParameterCloseChars()
 	{
 		return ParameterInfoUtils.DEFAULT_PARAMETER_CLOSE_CHARS;
 	}
 
 	@Override
 	public boolean tracksParameterIndex()
 	{
 		return true;
 	}
 
 	private static String renderParameter(ValueParameterDescriptor descriptor, boolean named, BindingContext bindingContext)
 	{
 		StringBuilder builder = new StringBuilder();
 		if(named)
 			builder.append("[");
 		if(descriptor.getVarargElementType() != null)
 		{
 			builder.append("vararg ");
 		}
 		builder.append(descriptor.getName()).append(": ").
 				append(DescriptorRenderer.TEXT.renderType(getActualParameterType(descriptor)));
 		if(descriptor.hasDefaultValue())
 		{
 			PsiElement element = BindingContextUtils.descriptorToDeclaration(bindingContext, descriptor);
 			String defaultExpression = "?";
 			if(element instanceof NapileParameter)
 			{
 				NapileParameter parameter = (NapileParameter) element;
 				NapileExpression defaultValue = parameter.getDefaultValue();
 				if(defaultValue != null)
 				{
 					if(defaultValue instanceof NapileConstantExpression)
 					{
 						NapileConstantExpression constantExpression = (NapileConstantExpression) defaultValue;
 						defaultExpression = constantExpression.getText();
 						if(defaultExpression.length() > 10)
 						{
 							if(defaultExpression.startsWith("\""))
 							{
 								defaultExpression = "\"...\"";
 							}
 							else if(defaultExpression.startsWith("\'"))
 							{
 								defaultExpression = "\'...\'";
 							}
 							else
 							{
 								defaultExpression = defaultExpression.substring(0, 7) + "...";
 							}
 						}
 					}
 				}
 			}
 			builder.append(" = ").append(defaultExpression);
 		}
 		if(named)
 			builder.append("]");
 		return builder.toString();
 	}
 
 	private static JetType getActualParameterType(ValueParameterDescriptor descriptor)
 	{
 		JetType paramType = descriptor.getType();
 		if(descriptor.getVarargElementType() != null)
 			paramType = descriptor.getVarargElementType();
 		return paramType;
 	}
 
 	@Override
 	public void updateUI(Object descriptor, ParameterInfoUIContext context)
 	{
 		//todo: when we will have ability to pass Array as vararg, implement such feature here too?
 		if(context == null || context.getParameterOwner() == null || !context.getParameterOwner().isValid())
 		{
 			return;
 		}
 		PsiElement parameterOwner = context.getParameterOwner();
 		if(parameterOwner instanceof NapileValueArgumentList)
 		{
 			NapileValueArgumentList argumentList = (NapileValueArgumentList) parameterOwner;
 			if(descriptor instanceof FunctionDescriptor)
 			{
 				NapileFile file = (NapileFile) argumentList.getContainingFile();
 				BindingContext bindingContext = AnalyzeSingleFileUtil.getContextForSingleFile(file);
 				FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
 				StringBuilder builder = new StringBuilder();
 				List<ValueParameterDescriptor> valueParameters = functionDescriptor.getValueParameters();
 				List<NapileValueArgument> valueArguments = argumentList.getArguments();
 				int currentParameterIndex = context.getCurrentParameterIndex();
 				int boldStartOffset = -1;
 				int boldEndOffset = -1;
 				boolean isGrey = false;
 				boolean isDeprecated = false; //todo: add deprecation check
 				Color color = context.getDefaultParameterColor();
 				PsiElement parent = argumentList.getParent();
 				if(parent instanceof NapileCallElement)
 				{
 					NapileCallElement callExpression = (NapileCallElement) parent;
 					NapileExpression calleeExpression = callExpression.getCalleeExpression();
 					NapileSimpleNameExpression refExpression = null;
 					if(calleeExpression instanceof NapileSimpleNameExpression)
 					{
 						refExpression = (NapileSimpleNameExpression) calleeExpression;
 					}
 					else if(calleeExpression instanceof NapileConstructorCalleeExpression)
 					{
 						NapileConstructorCalleeExpression constructorCalleeExpression = (NapileConstructorCalleeExpression) calleeExpression;
 						if(constructorCalleeExpression.getConstructorReferenceExpression() instanceof NapileSimpleNameExpression)
 						{
 							refExpression = (NapileSimpleNameExpression) constructorCalleeExpression.getConstructorReferenceExpression();
 						}
 					}
 					if(refExpression != null)
 					{
 						DeclarationDescriptor declarationDescriptor = bindingContext.get(BindingContext.REFERENCE_TARGET, refExpression);
 						if(declarationDescriptor != null)
 						{
 							if(declarationDescriptor == functionDescriptor)
 							{
 								color = GREEN_BACKGROUND;
 							}
 						}
 					}
 				}
 
 				boolean[] usedIndexes = new boolean[valueParameters.size()];
 				boolean namedMode = false;
 				Arrays.fill(usedIndexes, false);
 				if((currentParameterIndex >= valueParameters.size() && (valueParameters.size() > 0 || currentParameterIndex > 0)) && (valueParameters.size() == 0 || valueParameters.get(valueParameters.size() - 1).getVarargElementType() == null))
 				{
 					isGrey = true;
 				}
 				if(valueParameters.size() == 0)
 					builder.append(CodeInsightBundle.message("parameter.info.no.parameters"));
 				for(int i = 0; i < valueParameters.size(); ++i)
 				{
 					if(i != 0)
 						builder.append(", ");
 					boolean highlightParameter = i == currentParameterIndex || (!namedMode && i < currentParameterIndex &&
 							valueParameters.get(valueParameters.size() - 1).
 									getVarargElementType() != null);
 					if(highlightParameter)
 						boldStartOffset = builder.length();
 					if(!namedMode)
 					{
 						if(valueArguments.size() > i)
 						{
 							NapileValueArgument argument = valueArguments.get(i);
 							if(argument.isNamed())
 							{
 								namedMode = true;
 							}
 							else
 							{
 								ValueParameterDescriptor param = valueParameters.get(i);
 								builder.append(renderParameter(param, false, bindingContext));
 								if(i < currentParameterIndex)
 								{
 									if(argument.getArgumentExpression() != null)
 									{
 										//check type
 										JetType paramType = getActualParameterType(param);
 										JetType exprType = bindingContext.get(BindingContext.EXPRESSION_TYPE, argument.getArgumentExpression());
 										if(exprType != null && !JetTypeChecker.INSTANCE.isSubtypeOf(exprType, paramType))
 											isGrey = true;
 									}
 									else
 									{
 										isGrey = true;
 									}
 								}
 								usedIndexes[i] = true;
 							}
 						}
 						else
 						{
 							ValueParameterDescriptor param = valueParameters.get(i);
 							builder.append(renderParameter(param, false, bindingContext));
 						}
 					}
 					if(namedMode)
 					{
 						boolean takeAnyArgument = true;
 						if(valueArguments.size() > i)
 						{
 							NapileValueArgument argument = valueArguments.get(i);
 							if(argument.isNamed())
 							{
 								for(int j = 0; j < valueParameters.size(); ++j)
 								{
 									NapileSimpleNameExpression referenceExpression = argument.getArgumentName().getReferenceExpression();
 									ValueParameterDescriptor param = valueParameters.get(j);
 									if(referenceExpression != null && !usedIndexes[j] &&
 											param.getName().equals(referenceExpression.getReferencedNameAsName()))
 									{
 										takeAnyArgument = false;
 										usedIndexes[j] = true;
 										builder.append(renderParameter(param, true, bindingContext));
 										if(i < currentParameterIndex)
 										{
 											if(argument.getArgumentExpression() != null)
 											{
 												//check type
 												JetType paramType = getActualParameterType(param);
 												JetType exprType = bindingContext.get(BindingContext.EXPRESSION_TYPE, argument.getArgumentExpression());
 												if(exprType != null && !JetTypeChecker.INSTANCE.isSubtypeOf(exprType, paramType))
 												{
 													isGrey = true;
 												}
 											}
 											else
 											{
 												isGrey = true;
 											}
 										}
 										break;
 									}
 								}
 							}
 						}
 
 						if(takeAnyArgument)
 						{
 							if(i < currentParameterIndex)
 								isGrey = true;
 
 							for(int j = 0; j < valueParameters.size(); ++j)
 							{
 								ValueParameterDescriptor param = valueParameters.get(j);
 								if(!usedIndexes[j])
 								{
 									usedIndexes[j] = true;
 									builder.append(renderParameter(param, true, bindingContext));
 									break;
 								}
 							}
 						}
 					}
 					if(highlightParameter)
 						boldEndOffset = builder.length();
 				}
 				if(builder.toString().isEmpty())
 				{
 					context.setUIComponentEnabled(false);
 				}
 				else
 				{
 					context.setupUIComponentPresentation(builder.toString(), boldStartOffset, boldEndOffset, isGrey, isDeprecated, false, color);
 				}
 			}
 			else
 			{
 				context.setUIComponentEnabled(false);
 			}
 		}
 	}
 
 	private static NapileValueArgumentList findCall(CreateParameterInfoContext context)
 	{
 		//todo: calls to this constructors, when we will have auxiliary constructors
 		PsiFile file = context.getFile();
 		if(!(file instanceof NapileFile))
 			return null;
 		PsiElement element = file.findElementAt(context.getOffset());
 		while(element != null && !(element instanceof NapileValueArgumentList))
 		{
 			element = element.getParent();
 		}
 		if(element == null)
 			return null;
 		NapileValueArgumentList argumentList = (NapileValueArgumentList) element;
 		NapileCallElement callExpression;
 		if(element.getParent() instanceof NapileCallElement)
 		{
 			callExpression = (NapileCallElement) element.getParent();
 		}
 		else
 		{
 			return null;
 		}
 		BindingContext bindingContext = AnalyzeSingleFileUtil.getContextForSingleFile((NapileFile) file);
 		NapileExpression calleeExpression = callExpression.getCalleeExpression();
 		if(calleeExpression == null)
 			return null;
 		NapileSimpleNameExpression refExpression = null;
 		if(calleeExpression instanceof NapileSimpleNameExpression)
 		{
 			refExpression = (NapileSimpleNameExpression) calleeExpression;
 		}
 		else if(calleeExpression instanceof NapileConstructorCalleeExpression)
 		{
 			NapileConstructorCalleeExpression constructorCalleeExpression = (NapileConstructorCalleeExpression) calleeExpression;
 			if(constructorCalleeExpression.getConstructorReferenceExpression() instanceof NapileSimpleNameExpression)
 			{
 				refExpression = (NapileSimpleNameExpression) constructorCalleeExpression.getConstructorReferenceExpression();
 			}
 		}
 		if(refExpression != null)
 		{
 			JetScope scope = bindingContext.get(BindingContext.RESOLUTION_SCOPE, refExpression);
 			DeclarationDescriptor placeDescriptor = null;
 			if(scope != null)
 			{
 				placeDescriptor = scope.getContainingDeclaration();
 			}
 			Collection<DeclarationDescriptor> variants = TipsManager.getReferenceVariants(refExpression, bindingContext);
 			Name refName = refExpression.getReferencedNameAsName();
 			PsiReference[] references = refExpression.getReferences();
 			if(references.length == 0)
 				return null;
 			ArrayList<DeclarationDescriptor> itemsToShow = new ArrayList<DeclarationDescriptor>();
 			for(DeclarationDescriptor variant : variants)
 			{
 				if(variant instanceof FunctionDescriptor)
 				{
 					FunctionDescriptor functionDescriptor = (FunctionDescriptor) variant;
 					if(functionDescriptor.getName().equals(refName))
 					{
 						//todo: renamed functions?
 						if(placeDescriptor != null && !JetVisibilityChecker.isVisible(placeDescriptor, functionDescriptor))
 							continue;
 						itemsToShow.add(functionDescriptor);
 					}
 				}
 				else if(variant instanceof ClassDescriptor)
 				{
 					ClassDescriptor classDescriptor = (ClassDescriptor) variant;
 					if(classDescriptor.getName().equals(refName))
 					{
 						//todo: renamed classes?
						for(ConstructorDescriptor constructorDescriptor : classDescriptor.getConstructors())
 						{
 							if(placeDescriptor != null && !JetVisibilityChecker.isVisible(placeDescriptor, constructorDescriptor))
 							{
 								continue;
 							}
 							itemsToShow.add(constructorDescriptor);
 						}
 					}
 				}
 			}
 			context.setItemsToShow(ArrayUtil.toObjectArray(itemsToShow));
 			return argumentList;
 		}
 		return null;
 	}
 
 	private static NapileValueArgumentList findCallAndUpdateContext(UpdateParameterInfoContext context)
 	{
 		PsiFile file = context.getFile();
 		PsiElement element = file.findElementAt(context.getOffset());
 		if(element == null)
 			return null;
 		PsiElement parent = element.getParent();
 		while(parent != null && !(parent instanceof NapileValueArgumentList))
 		{
 			element = element.getParent();
 			parent = parent.getParent();
 		}
 		if(parent == null)
 			return null;
 		NapileValueArgumentList argumentList = (NapileValueArgumentList) parent;
 		if(element instanceof NapileValueArgument)
 		{
 			NapileValueArgument arg = (NapileValueArgument) element;
 			int i = argumentList.getArguments().indexOf(arg);
 			context.setCurrentParameter(i);
 			context.setHighlightedParameter(arg);
 		}
 		return argumentList;
 	}
 }
