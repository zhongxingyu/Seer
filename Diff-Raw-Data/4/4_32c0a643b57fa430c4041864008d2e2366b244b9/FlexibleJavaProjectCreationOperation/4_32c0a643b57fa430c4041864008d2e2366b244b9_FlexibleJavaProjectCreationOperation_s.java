 /*******************************************************************************
  * Copyright (c) 2003, 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jem.internal.plugin.JavaEMFNature;
import org.eclipse.jem.util.emf.workbench.WorkbenchResourceHelperBase;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.project.datamodel.properties.IFlexibleJavaProjectCreationDataModelProperties;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 
 public class FlexibleJavaProjectCreationOperation extends FlexibleProjectCreationOperation implements IFlexibleJavaProjectCreationDataModelProperties{
 
     public FlexibleJavaProjectCreationOperation(IDataModel model) {
         super(model);
     }
     private void addServerTarget(IProgressMonitor monitor)  throws CoreException, InvocationTargetException, InterruptedException, ExecutionException{
         IDataModel serverModel = model.getNestedModel(NESTED_MODEL_SERVER_TARGET);
         IDataModelOperation op = serverModel.getDefaultOperation();
         op.execute(monitor, null);
     }
     
     public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
         IStatus status = super.execute(monitor, info);
         try {
         	createJavaEMFNature();
             addServerTarget(monitor);
         } catch (ExecutionException e) {
         	Logger.getLogger().log(e);
         } catch (CoreException e) {
         	Logger.getLogger().log(e);
         } catch (InvocationTargetException e) {
         	Logger.getLogger().log(e);
         } catch (InterruptedException e) {
         	Logger.getLogger().log(e);
         }
         return status;
     }
 	protected void createJavaEMFNature() throws CoreException {
 		JavaEMFNature.createRuntime(getProject());
 		JavaEMFNature nature = JavaEMFNature.getRuntime(getProject());
		nature.primaryContributeToContext(WorkbenchResourceHelperBase.getEMFContext(getProject()));
 	}
 }
