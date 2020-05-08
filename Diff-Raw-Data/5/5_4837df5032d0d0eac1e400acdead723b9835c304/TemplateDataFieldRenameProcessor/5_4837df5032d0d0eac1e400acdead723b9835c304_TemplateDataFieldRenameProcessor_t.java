 /*
  * Copyright 2013 Red Hat, Inc.
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package org.jboss.errai.idea.plugin.ui.refactoring;
 
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiVariable;
 import com.intellij.psi.xml.XmlAttribute;
 import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.refactoring.rename.RenameXmlAttributeProcessor;
 import org.jboss.errai.idea.plugin.ui.TemplateUtil;
 import org.jboss.errai.idea.plugin.ui.model.ConsolidateDataFieldElementResult;
 import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * @author Mike Brock
  */
public class TemplateDataFieldRenameProcessor extends RenameXmlAttributeProcessor {
   @Override
   public boolean canProcessElement(@NotNull PsiElement element) {
     if (element instanceof XmlAttributeValue) {
       final XmlAttribute attribute = (XmlAttribute) element.getParent();
       if (attribute.getName().equals("data-field")) {
         return true;
       }
     }
     return false;
   }
 
   @Override
   public void prepareRenaming(PsiElement element, String newName, Map<PsiElement, String> allRenames) {
     final XmlAttributeValue attributeValue = (XmlAttributeValue) element;
     final PsiFile templateFile = TemplateUtil.getFileFromElement(attributeValue);
 
     final Collection<TemplateMetaData> templateOwners = TemplateUtil.getTemplateOwners(templateFile);
 
     for (TemplateMetaData metaData : templateOwners) {
       final Map<String,ConsolidateDataFieldElementResult> consolidatedDataFields = metaData.getConsolidatedDataFields();
       final ConsolidateDataFieldElementResult result = consolidatedDataFields.get(attributeValue.getValue());
       if (result != null && result.isDataFieldInClass()) {
         PsiVariable variable = (PsiVariable) result.getElement();
         allRenames.put(variable, newName);
       }
     }
   }
 }
