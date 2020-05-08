 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.communication.core.proxyselector;
 
 import java.net.ProxySelector;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.wire.WireWith;
 import org.eclipse.riena.internal.communication.core.Activator;
import org.osgi.service.log.LogService;
 
 /**
  * Configure the {@code ProxySelector} used by the {@code UrlConnection}.
  */
 @WireWith(ProxySelectorConfigurationWiring.class)
 public class ProxySelectorConfiguration {
 
 	private final ProxySelector previousProxySlector = ProxySelector.getDefault();
 
 	private final static Logger LOGGER = Log4r.getLogger(Activator.getDefault(), ProxySelectorConfiguration.class);
 
 	public void configure(IProxySelectorExtension[] proxySelectorExtensions) {
 		if (proxySelectorExtensions == null || proxySelectorExtensions.length == 0) {
 			ProxySelector.setDefault(previousProxySlector);
 			return;
 		}
 
 		Arrays.sort(proxySelectorExtensions, new Comparator<IProxySelectorExtension>() {
 			public int compare(IProxySelectorExtension e1, IProxySelectorExtension e2) {
 				return e1.getOrder() < e2.getOrder() ? -1 : e1.getOrder() > e2.getOrder() ? 1 : 0;
 			}
 		});
 
 		LOGGER.log(LogService.LOG_DEBUG, "Configured proxy selectors:"); //$NON-NLS-1$
 		List<ProxySelector> proxySelectors = new ArrayList<ProxySelector>(proxySelectorExtensions.length);
 		for (IProxySelectorExtension extension : proxySelectorExtensions) {
 			ProxySelector proxySelector = extension.createProxySelector();
 			LOGGER.log(LogService.LOG_DEBUG, "  - " + extension.getName() + " with order=" + extension.getOrder() //$NON-NLS-1$ //$NON-NLS-2$
 					+ " implemented by " + proxySelector.getClass().getName()); //$NON-NLS-1$
			proxySelectors.add(extension.createProxySelector());
 		}
 		ProxySelector.setDefault(new CompoundProxySelector(proxySelectors));
 	}
 
 }
