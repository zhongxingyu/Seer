 /*******************************************************************************
  * Copyright (c) 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.php.zendserver.deployment.core.targets;
 
 import java.net.MalformedURLException;
 
 import org.zend.php.zendserver.deployment.core.DeploymentCore;
 import org.zend.sdklib.internal.target.SSLContextInitializer;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.WebApiClient;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.auth.BasicCredentials;
 import org.zend.webapi.core.connection.auth.WebApiCredentials;
 import org.zend.webapi.core.service.IRequestListener;
 import org.zend.webapi.internal.core.connection.exception.InvalidResponseException;
 
 /**
  * Phpcloud container listener which allows to detect if container is in
  * sleeping mode and if it is, it wakes it up.
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public class PhpcloudContainerListener implements IRequestListener {
 
 	private IZendTarget target;
 
 	public PhpcloudContainerListener(String targetId) {
 		this.target = TargetsManagerService.INSTANCE.getTargetManager()
 				.getTargetById(targetId);
 	}
 
 	public PhpcloudContainerListener(IZendTarget target) {
 		super();
 		this.target = target;
 	}
 
 	public boolean perform() {
 		try {
 			WebApiClient client = getClient(target);
 
 			while (true) {
 				try {
 					client.getSystemInfo();
 					return true;
 				} catch (InvalidResponseException e) {
 					// container is sleeping
 					continue;
 				} catch (WebApiException e) {
 					DeploymentCore.log(e);
 					break;
 				}
 			}
 		} catch (MalformedURLException e) {
 			DeploymentCore.log(e);
 		}
 		return true;
 	}
 
 	/*
 	 * public boolean statusChanged() { ZendDevCloud cloud = new ZendDevCloud();
 	 * try { if (target.getProperty(ZendDevCloud.TARGET_PASSWORD) == null) {
 	 * Display.getDefault().syncExec(new Runnable() {
 	 * 
 	 * public void run() { PhpcloudPasswordDialog dialog = new
 	 * PhpcloudPasswordDialog( Display.getDefault().getActiveShell(), target);
 	 * dialog.open(); } });
 	 * 
 	 * } if (target.getProperty(ZendDevCloud.TARGET_PASSWORD) == null) { return
 	 * false; } if (!cloud.isSleeping(target)) { return true; }
 	 * monitor.beginTask("Container is waking up...", IProgressMonitor.UNKNOWN);
 	 * while (cloud.isSleeping(target)) { monitor.worked(1); } monitor.done(); }
 	 * catch (SdkException e) { return false; } catch (IOException e) { return
 	 * false; } return true; }
 	 */
 
 	public WebApiClient getClient(IZendTarget target)
 			throws MalformedURLException {
 		WebApiCredentials credentials = new BasicCredentials(target.getKey(),
 				target.getSecretKey());
 		String hostname = target.getHost().toString();
 		WebApiClient client = new WebApiClient(credentials, hostname,
 				SSLContextInitializer.instance.getRestletContext());
 		client.disableListeners();
 		return client;
 	}
 
 }
