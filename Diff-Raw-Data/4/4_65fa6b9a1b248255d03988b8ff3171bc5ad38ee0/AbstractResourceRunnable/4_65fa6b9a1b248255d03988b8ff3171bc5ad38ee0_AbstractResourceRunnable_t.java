 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.catalog;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.epp.internal.mpc.core.util.TransportFactory;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * A runnable that downloads a resource from an URL
  * 
  * @author David Green
  */
 abstract class AbstractResourceRunnable implements IRunnableWithProgress, Callable<Object> {
 
 	protected ResourceProvider resourceProvider;
 
 	protected String resourceUrl;
 
 	private final IProgressMonitor cancellationMonitor;
 
 	public AbstractResourceRunnable(IProgressMonitor cancellationMonitor, ResourceProvider resourceProvider,
 			String resourceUrl) {
 		this.cancellationMonitor = cancellationMonitor;
 		this.resourceProvider = resourceProvider;
 		this.resourceUrl = resourceUrl;
 	}
 
 	public Object call() throws Exception {
 		run(new NullProgressMonitor() {
 			@Override
 			public boolean isCanceled() {
 				return cancellationMonitor.isCanceled();
 			}
 		});
 		return this;
 	}
 
 	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 		try {
 			URL imageUrl = new URL(resourceUrl);
 
 			InputStream in = TransportFactory.instance().getTransport().stream(imageUrl.toURI(), monitor);
 			try {
 				resourceProvider.putResource(resourceUrl, in);
 			} finally {
				if (in != null) {
					in.close();
				}
 			}
 		} catch (URISyntaxException e) {
 			MarketplaceClientUi.error(NLS.bind(Messages.AbstractResourceRunnable_badUri, resourceUrl), e);
 		} catch (FileNotFoundException e) {
 			//MarketplaceClientUi.error(NLS.bind(Messages.AbstractResourceRunnable_resourceNotFound, resourceUrl), e);
 		} catch (IOException e) {
 			MarketplaceClientUi.error(e);
 		} catch (CoreException e) {
 			MarketplaceClientUi.error(e);
 		}
 		if (resourceProvider.containsResource(resourceUrl)) {
 			resourceRetrieved();
 		}
 	}
 
 	protected abstract void resourceRetrieved();
 
 }
