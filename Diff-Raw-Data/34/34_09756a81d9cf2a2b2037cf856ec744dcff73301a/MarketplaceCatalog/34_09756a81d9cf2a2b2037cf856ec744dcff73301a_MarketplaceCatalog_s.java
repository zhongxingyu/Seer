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
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.epp.internal.mpc.core.service.Category;
 import org.eclipse.epp.internal.mpc.core.service.Market;
 import org.eclipse.epp.internal.mpc.core.service.Node;
 import org.eclipse.epp.internal.mpc.ui.MarketplaceClientUi;
 import org.eclipse.epp.internal.mpc.ui.util.ConcurrentTaskManager;
 import org.eclipse.equinox.internal.p2.discovery.AbstractDiscoveryStrategy;
 import org.eclipse.equinox.internal.p2.discovery.Catalog;
 import org.eclipse.equinox.internal.p2.discovery.DiscoveryCore;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogCategory;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 import org.eclipse.equinox.internal.p2.discovery.model.Certification;
 import org.eclipse.equinox.internal.p2.discovery.model.Tag;
 import org.eclipse.equinox.p2.core.ProvisionException;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.operations.ProvisioningSession;
 import org.eclipse.equinox.p2.query.IQuery;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.query.QueryUtil;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
 import org.eclipse.equinox.p2.ui.ProvisioningUI;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * @author David Green
  */
 public class MarketplaceCatalog extends Catalog {
 
 	private final Map<String, Boolean> updateAvailableByNodeId = new HashMap<String, Boolean>();
 
 	private interface DiscoveryOperation {
 		public void run(MarketplaceDiscoveryStrategy strategy, IProgressMonitor monitor) throws CoreException;
 	}
 
 	public IStatus performQuery(final Market market, final Category category, final String queryText,
 			IProgressMonitor monitor) {
 		return performDiscovery(new DiscoveryOperation() {
 			public void run(MarketplaceDiscoveryStrategy strategy, IProgressMonitor monitor) throws CoreException {
 				strategy.performQuery(market, category, queryText, monitor);
 			}
 		}, monitor);
 	}
 
 	public IStatus recent(IProgressMonitor monitor) {
 		return performDiscovery(new DiscoveryOperation() {
 			public void run(MarketplaceDiscoveryStrategy strategy, IProgressMonitor monitor) throws CoreException {
 				strategy.recent(monitor);
 			}
 		}, monitor);
 	}
 
 	public IStatus popular(IProgressMonitor monitor) {
 		return performDiscovery(new DiscoveryOperation() {
 			public void run(MarketplaceDiscoveryStrategy strategy, IProgressMonitor monitor) throws CoreException {
 				strategy.popular(monitor);
 			}
 		}, monitor);
 	}
 
 	public IStatus featured(IProgressMonitor monitor, final Market market, final Category category) {
 		return performDiscovery(new DiscoveryOperation() {
 			public void run(MarketplaceDiscoveryStrategy strategy, IProgressMonitor monitor) throws CoreException {
 				strategy.featured(monitor, market, category);
 			}
 		}, monitor);
 	}
 
 	public IStatus installed(IProgressMonitor monitor) {
 		return performDiscovery(new DiscoveryOperation() {
 			public void run(MarketplaceDiscoveryStrategy strategy, IProgressMonitor monitor) throws CoreException {
 				strategy.installed(monitor);
 			}
 		}, monitor);
 	}
 
 	public IStatus checkForUpdates(final IProgressMonitor monitor) {
 		int remainingWork = 10000000;
 		monitor.beginTask(Messages.MarketplaceCatalog_checkingForUpdates, remainingWork);
 		try {
 			Map<URI, List<MarketplaceNodeCatalogItem>> installedCatalogItemsByUpdateUri = new HashMap<URI, List<MarketplaceNodeCatalogItem>>();
 			for (CatalogItem item : getItems()) {
 				if (!(item instanceof MarketplaceNodeCatalogItem)) {
 					continue;
 				}
 				MarketplaceNodeCatalogItem catalogItem = (MarketplaceNodeCatalogItem) item;
 				if (catalogItem.isInstalled()) {
 					Node node = catalogItem.getData();
 					Boolean updateAvailable = updateAvailableByNodeId.get(node.getId());
 					if (updateAvailable != null) {
 						catalogItem.setUpdateAvailable(updateAvailable);
 					} else {
 						try {
 							URI uri = new URI(node.getUpdateurl());
 							List<MarketplaceNodeCatalogItem> catalogItemsThisSite = installedCatalogItemsByUpdateUri.get(uri);
 							if (catalogItemsThisSite == null) {
 								catalogItemsThisSite = new ArrayList<MarketplaceNodeCatalogItem>();
 								installedCatalogItemsByUpdateUri.put(uri, catalogItemsThisSite);
 							}
 							catalogItemsThisSite.add(catalogItem);
 						} catch (URISyntaxException e) {
 							MarketplaceClientUi.error(e);
 							catalogItem.setAvailable(false);
 						}
 					}
 				}
 			}
 			if (!installedCatalogItemsByUpdateUri.isEmpty()) {
 				final Map<String, IInstallableUnit> installedIUs = MarketplaceClientUi.computeInstalledIUsById(new SubProgressMonitor(
 						monitor, remainingWork / 20));
 				remainingWork -= remainingWork / 20;
 
 				ConcurrentTaskManager executor = new ConcurrentTaskManager(installedCatalogItemsByUpdateUri.size(),
 						Messages.MarketplaceCatalog_checkingForUpdates);
 				try {
 					final IProgressMonitor pm = new NullProgressMonitor() {
 						@Override
 						public boolean isCanceled() {
 							return super.isCanceled() || monitor.isCanceled();
 						}
 					};
 					for (Map.Entry<URI, List<MarketplaceNodeCatalogItem>> entry : installedCatalogItemsByUpdateUri.entrySet()) {
 						final URI uri = entry.getKey();
 						final List<MarketplaceNodeCatalogItem> catalogItemsThisSite = entry.getValue();
 						executor.submit(new Runnable() {
 							public void run() {
 								ProvisioningSession session = ProvisioningUI.getDefaultUI().getSession();
 								IMetadataRepositoryManager manager = (IMetadataRepositoryManager) session.getProvisioningAgent()
 										.getService(IMetadataRepositoryManager.SERVICE_NAME);
 								try {
 									IMetadataRepository repository = manager.loadRepository(uri, pm);
 									IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery( //
 									"id ~= /*.feature.group/ && " + //$NON-NLS-1$
 											"properties['org.eclipse.equinox.p2.type.group'] == true ");//$NON-NLS-1$
 									IQueryResult<IInstallableUnit> result = repository.query(query, pm);
 
 									// compute highest version for all available IUs.
 									Map<String, Version> repositoryIuVersionById = new HashMap<String, Version>();
 									for (Iterator<IInstallableUnit> iter = result.iterator(); iter.hasNext();) {
 										IInstallableUnit iu = iter.next();
 										String id = iu.getId();
 										Version version = iu.getVersion();
 										Version priorVersion = repositoryIuVersionById.put(id, version);
 										if (priorVersion != null && priorVersion.compareTo(version) > 0) {
 											repositoryIuVersionById.put(id, priorVersion);
 										}
 									}
 
 									for (MarketplaceNodeCatalogItem item : catalogItemsThisSite) {
 										item.setUpdateAvailable(false);
 										List<String> installableUnits = item.getInstallableUnits();
 										if (!repositoryIuVersionById.keySet().containsAll(installableUnits)) {
 											item.setAvailable(false);
 										} else {
 											for (String iu : installableUnits) {
 												Version availableVersion = repositoryIuVersionById.get(iu);
 												if (availableVersion != null) {
 													IInstallableUnit installedIu = installedIUs.get(iu);
 													if (installedIu != null
 															&& installedIu.getVersion().compareTo(availableVersion) < 0) {
 														item.setUpdateAvailable(true);
 														break;
 													}
 												}
 											}
 										}
 									}
 								} catch (ProvisionException e) {
 									MarketplaceClientUi.error(e);
 									for (MarketplaceNodeCatalogItem item : catalogItemsThisSite) {
 										item.setAvailable(false);
 									}
 								} catch (OperationCanceledException e) {
 									// nothing to do
 								}
 							}
 						});
 					}
 					try {
 						executor.waitUntilFinished(new SubProgressMonitor(monitor, remainingWork));
 					} catch (CoreException e) {
 						MarketplaceClientUi.error(e);
 						return e.getStatus();
 					}
 				} finally {
 					executor.shutdownNow();
 				}
 			}
 
 			if (!monitor.isCanceled() && !installedCatalogItemsByUpdateUri.isEmpty()) {
 				for (List<MarketplaceNodeCatalogItem> items : installedCatalogItemsByUpdateUri.values()) {
 					for (MarketplaceNodeCatalogItem item : items) {
 						if (item.getUpdateAvailable() != null) {
 							updateAvailableByNodeId.put(item.getData().getId(), item.getUpdateAvailable());
 						}
 					}
 				}
 			}
 			return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
 		} finally {
 			monitor.done();
 		}
 	}
 
 	protected IStatus performDiscovery(DiscoveryOperation operation, IProgressMonitor monitor) {
 		MultiStatus status = new MultiStatus(MarketplaceClientUi.BUNDLE_ID, 0, Messages.MarketplaceCatalog_queryFailed,
 				null);
 		if (getDiscoveryStrategies().isEmpty()) {
 			throw new IllegalStateException();
 		}
 
 		// reset, keeping no items but the same tags, categories and certifications
 		List<CatalogItem> items = new ArrayList<CatalogItem>();
 		List<CatalogCategory> categories = new ArrayList<CatalogCategory>(getCategories());
 		List<Certification> certifications = new ArrayList<Certification>(getCertifications());
 		List<Tag> tags = new ArrayList<Tag>(getTags());
 		for (CatalogCategory catalogCategory : categories) {
 			catalogCategory.getItems().clear();
 		}
 
 		final int totalTicks = 100000;
 		final int discoveryTicks = totalTicks - (totalTicks / 10);
 		monitor.beginTask(Messages.MarketplaceCatalog_queryingMarketplace, totalTicks);
 		try {
 			int strategyTicks = discoveryTicks / getDiscoveryStrategies().size();
 			for (AbstractDiscoveryStrategy discoveryStrategy : getDiscoveryStrategies()) {
 				if (monitor.isCanceled()) {
 					status.add(Status.CANCEL_STATUS);
 					break;
 				}
 				if (discoveryStrategy instanceof MarketplaceDiscoveryStrategy) {
 					discoveryStrategy.setCategories(categories);
 					discoveryStrategy.setItems(items);
 					discoveryStrategy.setCertifications(certifications);
 					discoveryStrategy.setTags(tags);
 					try {
 						MarketplaceDiscoveryStrategy marketplaceStrategy = (MarketplaceDiscoveryStrategy) discoveryStrategy;
 						operation.run(marketplaceStrategy, new SubProgressMonitor(monitor, strategyTicks));
 
 					} catch (CoreException e) {
 						status.add(new Status(IStatus.ERROR, DiscoveryCore.ID_PLUGIN, NLS.bind(
 								Messages.MarketplaceCatalog_failedWithError, discoveryStrategy.getClass()
 										.getSimpleName()), e));
 					}
 				}
 			}
 
 			update(categories, items, certifications, tags);
 		} finally {
 			monitor.done();
 		}
 		return status;
 	}
 
 }
