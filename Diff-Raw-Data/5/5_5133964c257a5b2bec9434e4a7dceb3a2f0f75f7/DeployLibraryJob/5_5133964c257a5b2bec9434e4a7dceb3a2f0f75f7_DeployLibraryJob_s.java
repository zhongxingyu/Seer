 /*******************************************************************************
  * Copyright (c) 2013 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.php.library.core.deploy;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.zend.php.library.core.LibraryUtils;
 import org.zend.php.library.internal.core.LibraryCore;
 import org.zend.php.zendserver.deployment.core.descriptor.DescriptorContainerManager;
 import org.zend.php.zendserver.deployment.core.sdk.EclipseMappingModelLoader;
 import org.zend.php.zendserver.deployment.core.sdk.EclipseVariableResolver;
 import org.zend.php.zendserver.deployment.core.sdk.SdkStatus;
 import org.zend.php.zendserver.deployment.core.sdk.StatusChangeListener;
 import org.zend.php.zendserver.deployment.debug.core.Activator;
 import org.zend.php.zendserver.deployment.debug.core.Messages;
 import org.zend.sdklib.application.ZendLibrary;
 import org.zend.webapi.internal.core.connection.exception.UnexpectedResponseCode;
 import org.zend.webapi.internal.core.connection.exception.WebApiCommunicationError;
 
 /**
  * Job responsible for deploying PHP Library to selected Zend Target.
  * 
  * @author Wojciech Galanciak, 2013
  * 
  */
 public class DeployLibraryJob extends AbstractLibraryJob {
 
 	public DeployLibraryJob(LibraryDeployData data) {
 		super(Messages.deploymentJob_Title, data);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
 	 * IProgressMonitor)
 	 */
 	public IStatus run(IProgressMonitor monitor) {
 		StatusChangeListener listener = new StatusChangeListener(monitor);
 		ZendLibrary lib = new ZendLibrary(new EclipseMappingModelLoader());
 		lib.addStatusChangeListener(listener);
 		lib.setVariableResolver(new EclipseVariableResolver());
 		if (data.getRoot().getName().endsWith(".zpk") //$NON-NLS-1$
 				|| new File(data.getRoot(),
 						DescriptorContainerManager.DESCRIPTOR_PATH).exists()) {
			lib.deploy(data.getRoot().getAbsolutePath(), data.getTargetId());
 		} else {
 			try {
 				File root = LibraryUtils.getTemporaryDescriptor(data.getName(),
 						data.getVersion());
 				lib.deploy(data.getRoot().getAbsolutePath(),
						root.getAbsolutePath(), data.getTargetId());
 			} catch (IOException e) {
 				LibraryCore.log(e);
 			}
 		}
 		if (monitor.isCanceled()) {
 			return Status.CANCEL_STATUS;
 		}
 		Throwable exception = listener.getStatus().getThrowable();
 		if (exception instanceof UnexpectedResponseCode) {
 			UnexpectedResponseCode codeException = (UnexpectedResponseCode) exception;
 			responseCode = codeException.getResponseCode();
 			switch (responseCode) {
 			// TODO change it to a different error code
 			case INTERNAL_SERVER_ERROR:
 				return Status.OK_STATUS;
 				// case INTERNAL_SERVER_ERROR:
 				// return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 				// codeException.getMessage(), codeException);
 			default:
 				break;
 			}
 		} else if (exception instanceof WebApiCommunicationError) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					Messages.DeploymentLaunchJob_ConnectionRefusedMessage);
 		}
 		return new SdkStatus(listener.getStatus());
 	}
 
 }
