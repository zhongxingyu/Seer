 /*
  *    Copyright (c) 2006 LiXiao.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.thoughtworks.fireworks.adapters;
 
 import com.intellij.psi.PsiElement;
 import com.intellij.refactoring.listeners.RefactoringElementListener;
 import com.thoughtworks.fireworks.core.TestShadowMap;
 import com.thoughtworks.shadow.Sunshine;
 
 import java.io.FileNotFoundException;
 
 public class RefactoringTestShadowListener implements RefactoringElementListener {
    private final Sunshine sunshine;
    private final String className;
     private final ProjectAdapter project;
     private final TestShadowMap testShadowMap;
 
     public RefactoringTestShadowListener(TestShadowMap testShadowMap,
                                          Sunshine sunshine,
                                          String className,
                                          ProjectAdapter project) {
         this.testShadowMap = testShadowMap;
         this.sunshine = sunshine;
         this.className = className;
         this.project = project;
     }
 
     public void elementMoved(PsiElement newElement) {
         try {
             refactorTestShadowMap(getFile(newElement));
         } catch (FileNotFoundException e) {
         }
     }
 
     public void elementRenamed(PsiElement newElement) {
         try {
             refactorTestShadowMap(getFile(newElement));
         } catch (FileNotFoundException e) {
         }
     }
 
     public void refactorTestShadowMap(VirtualFileAdaptee file) {
         if (file.isInSourceOrTestContent()) {
             Sunshine newSunshine = file.createSunshine();
             String newClassName = file.getJavaFileClassName();
             testShadowMap.removeTestShadow(sunshine, className);
             testShadowMap.addTestShadow(newSunshine, newClassName);
         }
     }
 
     private VirtualFileAdaptee getFile(PsiElement newElement) throws FileNotFoundException {
         return VirtualFileAdapter.getFile(newElement, project);
     }
 }
