 /*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
 *    rfrost@bea.com
 *    tyip@bea.com
 *    
 *    Based on GenericServerLaunchConfigurationDelegate by Gorkem Ercan
  *******************************************************************************/
 
 package org.eclipse.jst.server.generic.core.internal;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IVMConnector;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * <p>Extension of <code>AbstractJavaLaunchConfigurationDelegate</code> that supports 
  * the connection to remote JVMs for external servers. Used for debugging.</p>
  * 
  * <p>Based on JavaRemoteApplicationLaunchConfigurationDelegate</p>
  */
 public class ExternalDebugLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {
 
     /* (non-Javadoc)
      * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
      */
     public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
 
         if (monitor == null) {
             monitor = new NullProgressMonitor();
         }
 
         monitor.beginTask(NLS.bind(GenericServerCoreMessages.attachingToExternalGenericServer,new String[]{configuration.getName()}), 3);
         
         // check for cancellation
         if (monitor.isCanceled()) {
             return;
         }                       
                     
         monitor.subTask(GenericServerCoreMessages.verifyingExternalServerDebuggingLaunchAttributes);
                         
         String connectorId = getVMConnectorId(configuration);
         IVMConnector connector = null;
         if (connectorId == null) {
             connector = JavaRuntime.getDefaultVMConnector();
         } else {
             connector = JavaRuntime.getVMConnector(connectorId);
         }
         if (connector == null) {
             abort(GenericServerCoreMessages.externalServerDebugConnectorNotSpecified,
                     null, IJavaLaunchConfigurationConstants.ERR_CONNECTOR_NOT_AVAILABLE); 
         }
         
         Map argMap = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, (Map)null);
         
         int connectTimeout = JavaRuntime.getPreferences().getInt(JavaRuntime.PREF_CONNECT_TIMEOUT);
         argMap.put("timeout", ""+connectTimeout);  //$NON-NLS-1$//$NON-NLS-2$
 
         // check for cancellation
         if (monitor.isCanceled()) {
             return;
         }
         
         monitor.worked(1);
         
         monitor.subTask(GenericServerCoreMessages.creatingExternalServerDebuggingSourceLocator);
         // set the default source locator if required
         setDefaultSourceLocator(launch, configuration);
         monitor.worked(1);      
         
         // connect to remote VM
         connector.connect(argMap, monitor, launch);
         
         // check for cancellation
         if (monitor.isCanceled()) {
             IDebugTarget[] debugTargets = launch.getDebugTargets();
             for (int i = 0; i < debugTargets.length; i++) {
                 IDebugTarget target = debugTargets[i];
                 if (target.canDisconnect()) {
                     target.disconnect();
                 }
             }
             return;
         }
         
         monitor.done();
     }
 }
