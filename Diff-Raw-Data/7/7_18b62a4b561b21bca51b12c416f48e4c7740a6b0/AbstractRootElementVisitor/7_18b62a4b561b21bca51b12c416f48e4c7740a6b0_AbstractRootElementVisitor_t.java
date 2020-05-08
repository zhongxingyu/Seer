 /*******************************************************************************
  * Copyright (c) 2010 BSI Business Systems Integration AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     BSI Business Systems Integration AG - initial API and implementation
  ******************************************************************************/
 package org.eclipse.scout.sdk.saml.importer.operation;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.scout.saml.saml.ModuleElement;
 import org.eclipse.scout.sdk.workspace.IScoutProject;
 
 /**
  * <h3>{@link AbstractRootElementVisitor}</h3> ...
  * 
  * @author mvi
  * @since 3.8.0 21.11.2012
  */
 public abstract class AbstractRootElementVisitor<T extends EObject> implements IRootElementVisitor {
 
   private final Class<T> m_type;
 
   protected AbstractRootElementVisitor(Class<T> type) {
     m_type = type;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public void visit(EObject o, SamlContext context) throws CoreException, IllegalArgumentException {
     if (o instanceof ModuleElement) {
       ModuleElement mod = (ModuleElement) o;
       String moduleName = mod.getName();
       IScoutProject curModule = findScoutProjectRec(context.getRootProject(), moduleName);
       if (curModule == null) {
         throw new IllegalArgumentException("module '" + moduleName + "' could not be found.");
       }
       else {
        if (curModule.getClientBundle() == null || curModule.getSharedBundle() == null || curModule.getServerBundle() == null) {
          throw new IllegalArgumentException("module '" + moduleName + "' is not complete. It must contain a client, server & shared bundle.");
        }
        else {
          context.setCurrentScoutModule(curModule);
        }
       }
     }
     else if (m_type.isAssignableFrom(o.getClass())) {
       ISamlElementImportOperation op = prepareOperation((T) o);
       if (op != null) {
         op.setSamlContext(context);
         op.validate();
         op.run(context.getMonitor(), context.getWorkingCopyManager());
       }
     }
   }
 
   protected abstract ISamlElementImportOperation prepareOperation(T element);
 
   private IScoutProject findScoutProjectRec(IScoutProject parent, String searchName) {
     if (parent.getProjectName().equals(searchName)) {
       return parent;
     }
     for (IScoutProject child : parent.getSubProjects()) {
       IScoutProject result = findScoutProjectRec(child, searchName);
       if (result != null) {
         return result;
       }
     }
     return null;
   }
 }
