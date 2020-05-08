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
 
 package org.napile.idea.plugin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.Icon;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.asm.lib.NapileAnnotationPackage;
 import org.napile.asm.lib.NapileLangPackage;
 import org.napile.compiler.analyzer.AnalyzeExhaust;
 import org.napile.compiler.lang.descriptors.MutableClassDescriptor;
 import org.napile.compiler.lang.descriptors.SimpleMethodDescriptor;
 import org.napile.compiler.lang.psi.*;
 import org.napile.compiler.lang.resolve.AnnotationUtils;
 import org.napile.compiler.lang.resolve.BindingContext;
 import org.napile.compiler.lang.resolve.DescriptorUtils;
 import org.napile.compiler.lang.lexer.NapileTokens;
 import org.napile.compiler.lang.psi.NapileTypeParameter;
 import org.napile.compiler.util.RunUtil;
 import org.napile.idea.plugin.project.WholeProjectAnalyzerFacade;
 import com.intellij.ide.IconProvider;
 import com.intellij.openapi.util.IconLoader;
 import com.intellij.openapi.util.Iconable;
 import com.intellij.psi.PsiElement;
 import com.intellij.ui.LayeredIcon;
 import com.intellij.ui.RowIcon;
 import com.intellij.util.BitUtil;
 import com.intellij.util.PlatformIcons;
 
 /**
  * @author yole
  */
 public class JetIconProvider extends IconProvider
 {
 	private static final Icon FINAL_MARK_ICON = IconLoader.getIcon("/nodes/finalMark.png");
 	private static final Icon RUNNABLE_MARK = IconLoader.getIcon("/nodes/runnableMark.png");
 
 	public static JetIconProvider INSTANCE = new JetIconProvider();
 
 	@Override
 	public Icon getIcon(@NotNull PsiElement psiElement, int flags)
 	{
 		Icon icon = null;
 		boolean isRunnable = false;
 
 		if(psiElement instanceof NapileFile)
 		{
 			NapileFile file = (NapileFile) psiElement;
 
 			icon = file.getDeclarations().length == 1 ? getIcon(file.getDeclarations()[0], flags) : JetIcons.FILE;
 		}
 		else if(psiElement instanceof NapileNamedMethod)
 			icon = JetIcons.METHOD;
 		else if(psiElement instanceof NapileConstructor)
 			icon = JetIcons.CONSTRUCTOR;
 		else if(psiElement instanceof NapileTypeParameter)
 			icon = JetIcons.TYPE_PARAMETER;
 		else if(psiElement instanceof NapileClass)
 		{
 			NapileClass napileClass = (NapileClass) psiElement;
 
 			AnalyzeExhaust analyzeExhaust = WholeProjectAnalyzerFacade.analyzeProjectWithCacheOnAFile(napileClass.getContainingFile());
 			MutableClassDescriptor descriptor = (MutableClassDescriptor) analyzeExhaust.getBindingContext().get(BindingContext.DECLARATION_TO_DESCRIPTOR, napileClass);
 
 			icon = napileClass.hasModifier(NapileTokens.ABSTRACT_KEYWORD) ? JetIcons.ABSTRACT_CLASS : JetIcons.CLASS;
 
 			if(descriptor != null)
 			{
 				if(AnnotationUtils.isAnnotation(descriptor))
 				{
 					icon = JetIcons.ANNOTATION;
 					if(AnnotationUtils.hasAnnotation(descriptor, NapileAnnotationPackage.REPEATABLE))
 						icon = JetIcons.REPEATABLE_ANNOTATION;
 				}
 				else if(DescriptorUtils.isSubclassOf(descriptor, NapileLangPackage.THROWABLE))
 					icon = napileClass.hasModifier(NapileTokens.ABSTRACT_KEYWORD) ? JetIcons.ABSTRACT_THROWABLE : JetIcons.THROWABLE;
 
 				for(SimpleMethodDescriptor m : descriptor.getFunctions())
 					if(RunUtil.isRunPoint(m))
 					{
 						isRunnable = true;
 						break;
 					}
 			}
 		}
 		else if(psiElement instanceof NapileVariable || psiElement instanceof NapilePropertyParameter)
 			icon = JetIcons.VARIABLE;
 
		return icon == null ? null : modifyIcon(psiElement instanceof NapileModifierListOwner ? ((NapileModifierListOwner) psiElement) : null, icon, flags, isRunnable);
 	}
 
	public static Icon modifyIcon(@Nullable NapileModifierListOwner modifierList, Icon baseIcon, int flags, boolean isRunnable)
 	{
 		RowIcon icon = new RowIcon(2);
 
 		if(baseIcon != null)
 		{
			boolean isFinal = modifierList != null && modifierList.hasModifier(NapileTokens.FINAL_KEYWORD);
 			if(isFinal || isRunnable)
 			{
 				List<Icon> icons = new ArrayList<Icon>(2);
 				icons.add(baseIcon);
 				if(isFinal)
 					icons.add(FINAL_MARK_ICON);
 				if(isRunnable)
 					icons.add(RUNNABLE_MARK);
 
 				icon.setIcon(new LayeredIcon(icons.toArray(new Icon[icons.size()])), 0);
 			}
 			else
 				icon.setIcon(baseIcon, 0);
 		}
 
 		if(modifierList != null && BitUtil.isSet(flags, Iconable.ICON_FLAG_VISIBILITY))
 		{
 			if(modifierList.hasModifier(NapileTokens.LOCAL_KEYWORD))
 				icon.setIcon(PlatformIcons.PRIVATE_ICON, 1);
 			else if(modifierList.hasModifier(NapileTokens.COVERED_KEYWORD))
 				icon.setIcon(PlatformIcons.PROTECTED_ICON, 1);
 			else if(modifierList.hasModifier(NapileTokens.HERITABLE_KEYWORD))
 				icon.setIcon(JetIcons.C_HERITABLE, 1);
 			else
 				icon.setIcon(PlatformIcons.PUBLIC_ICON, 1);
 		}
 
 		return icon;
 	}
 }
