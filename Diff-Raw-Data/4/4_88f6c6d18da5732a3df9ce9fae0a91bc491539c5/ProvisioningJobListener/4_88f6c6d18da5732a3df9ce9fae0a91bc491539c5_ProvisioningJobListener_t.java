 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.epp.internal.mpc.core.service.Node;
 import org.eclipse.epp.internal.mpc.ui.util.ConcurrentTaskManager;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 
 /**
  * A job listener that produces notifications of a successful install.
  * 
  * @author David Green
  */
 class ProvisioningJobListener extends JobChangeAdapter {
 	private final Set<CatalogItem> installItems;
 
 	public ProvisioningJobListener(Set<CatalogItem> installItems) {
 		this.installItems = installItems;
 	}
 
 	@Override
 	public void done(IJobChangeEvent event) {
 		if (event.getResult().isOK()) {
 			Job job = new Job(Messages.ProvisioningJobListener_notificationTaskName) {
 				{
 					setPriority(Job.LONG);
 					setSystem(true);
 					setUser(true);
 				}
 
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
 					ConcurrentTaskManager taskManager = new ConcurrentTaskManager(installItems.size(),
 							Messages.ProvisioningJobListener_notificationTaskName);
 					for (final CatalogItem item : installItems) {
 						taskManager.submit(new Runnable() {
 							public void run() {
 								Node node = (Node) item.getData();
 								String url = node.getUrl();
 								if (!url.endsWith("/")) { //$NON-NLS-1$
 									url += "/"; //$NON-NLS-1$
 								}
 								url += "success"; //$NON-NLS-1$
 								try {
 									InputStream stream = org.eclipse.equinox.internal.p2.repository.RepositoryTransport.getInstance()
 											.stream(new URI(url), new NullProgressMonitor());
 									try {
 										while (stream.read() != -1) {
 											// nothing to do
 										}
 									} finally {
 										stream.close();
 									}
 								} catch (Throwable e) {
									//per bug 314028 logging this error is not useful.
 								}
 							}
 						});
 					}
 					try {
 						taskManager.waitUntilFinished(monitor);
 					} catch (CoreException e) {
 						return e.getStatus();
 					}
 					return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
 				}
 
 			};
 			job.schedule();
 		}
 	}
 }
