 /*
  * Copyright 2010-2013 napile.org
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
 
 package org.napile.playJava4idea.template.base.editor;
 
 import java.util.Collection;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.playJava4idea.PlayJavaUtil;
 import org.napile.playJava4idea.icons.PlayJavaIcons;
 import org.napile.playJava4idea.template.base.psi.PlayBaseTemplateFile;
 import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
 import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
 import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleUtil;
 import com.intellij.openapi.roots.ModuleRootManager;
 import com.intellij.openapi.util.text.StringUtil;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiClass;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiIdentifier;
 import com.intellij.psi.PsiManager;
 import com.intellij.psi.PsiMethod;
 import com.intellij.psi.PsiModifier;
 
 /**
  * @author VISTALL
  * @since 23:21/18.03.13
  */
 public class PlayBaseTemplateLineMarkerProvider extends RelatedItemLineMarkerProvider
 {
 	@Override
 	protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result)
 	{
 		if(element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod)
 		{
 			PsiMethod method = (PsiMethod) element.getParent();
 
 			final PsiClass containingClass = method.getContainingClass();
 
 			if(PlayJavaUtil.isSuperController(containingClass) && method.hasModifierProperty(PsiModifier.STATIC) && method.hasModifierProperty(PsiModifier.PUBLIC))
 			{
 				assert containingClass != null;
 
 				String qName = StringUtil.notNullize(containingClass.getQualifiedName());
 				qName = qName.replace(".", "/");
 
 				if(qName.startsWith("controllers"))
 				{
 					qName = qName.replace("controllers", "views");
 				}
 
 				final Module moduleForPsiElement = ModuleUtil.findModuleForPsiElement(element);
				assert moduleForPsiElement != null;
 
 				ModuleRootManager rootManager = ModuleRootManager.getInstance(moduleForPsiElement);
 				PsiManager manager = PsiManager.getInstance(element.getProject());
 				for(VirtualFile file : rootManager.getSourceRoots())
 				{
 					VirtualFile virtualFile = file.findFileByRelativePath(qName);
 					if(virtualFile == null)
 					{
 						continue;
 					}
 
 					for(VirtualFile child : virtualFile.getChildren())
 					{
 						if(child.getNameWithoutExtension().equalsIgnoreCase(method.getName()))
 						{
 							PsiFile psiFile = manager.findFile(child);
 							if(psiFile instanceof PlayBaseTemplateFile)
 							{
 								NavigationGutterIconBuilder builder = NavigationGutterIconBuilder.create(PlayJavaIcons.ICON_16x16).setTargets(psiFile);
 
 								result.add(builder.createLineMarkerInfo(element));
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 }
