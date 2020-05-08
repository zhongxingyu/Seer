 /*******************************************************************************
  * Copyright (c) May 3, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  *
  * Contributors:
  *    Christophe Bouhier - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.data.cdo;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.cdo.common.util.CDOException;
 import org.eclipse.emf.cdo.eresource.CDOResource;
 import org.eclipse.emf.cdo.eresource.CDOResourceFolder;
 import org.eclipse.emf.cdo.eresource.CDOResourceNode;
 import org.eclipse.emf.cdo.session.CDOSession;
 import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CDOUtil;
 import org.eclipse.emf.cdo.view.CDOInvalidationPolicy;
 import org.eclipse.emf.cdo.view.CDOView;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.net4j.util.event.IListener;
 import org.eclipse.net4j.util.security.IPasswordCredentialsProvider;
 import org.eclipse.net4j.util.security.PasswordCredentials;
 import org.eclipse.net4j.util.security.PasswordCredentialsProvider;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.NetxstudioPackage;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.data.internal.DataActivator;
 import com.netxforge.netxstudio.generics.GenericsPackage;
 import com.netxforge.netxstudio.geo.GeoPackage;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.protocols.ProtocolsPackage;
 import com.netxforge.netxstudio.scheduling.SchedulingPackage;
 import com.netxforge.netxstudio.services.ServicesPackage;
 
 /**
  * A CDO Data provider, which gets data from a CDO Server. The CDO provider has
  * some special requirements. - It require to initialize a connection.
  * 
  * Note: One single data provider, is associated with one single user/session.
  * The session is static.
  * 
  * Typical usage is to get a CDOResource using a Resourceset (Which will have a
  * CDOView associated). The provider can also use a one of transaction which is
  * not associated with a resource set.
  * 
  * 
  * @author Christophe Bouhier christophe.bouhier@netxforge.com
  */
 public abstract class CDODataProvider implements IDataProvider {
 
 	private List<EPackage> ePackages = new ArrayList<EPackage>();
 	private ICDOConnection connection;
 	private boolean doGetResourceFromOwnTransaction = true;
 
 	public static final int COMMIT_TIMEOUT = 500; // seconds.
 
 	@Inject
 	public CDODataProvider(ICDOConnection conn) {
 		this.connection = conn;
 
 		ePackages.add(GeoPackage.eINSTANCE);
 		ePackages.add(GenericsPackage.eINSTANCE);
 		ePackages.add(NetxstudioPackage.eINSTANCE);
 		ePackages.add(LibraryPackage.eINSTANCE);
 		ePackages.add(MetricsPackage.eINSTANCE);
 		ePackages.add(OperatorsPackage.eINSTANCE);
 		ePackages.add(ProtocolsPackage.eINSTANCE);
 		ePackages.add(SchedulingPackage.eINSTANCE);
 		ePackages.add(ServicesPackage.eINSTANCE);
 	}
 
 	public String getServer() {
 		if (connection instanceof CDODataConnection) {
 			return ((CDODataConnection) connection).getCurrentServer();
 		}
 		return null;
 	}
 
 	public String getSessionUserID() {
 		return this.getSession().getUserID();
 	}
 
 	public void openSession(String uid, String passwd) throws SecurityException {
 		openSession(uid, passwd, null);
 	}
 
 	public void openSession(String uid, String passwd, String server)
 			throws SecurityException {
 		this.openSession(uid, passwd, server, false);
 	}
 
 	public void openSession(String uid, String passwd, String server,
 			boolean reset) throws SecurityException {
 
 		if (connection.getConfig() == null) {
 			connection.initialize(server);
 		} else if (reset) {
 			connection.initialize(server);
 		}
 
 		// connection.getConfig().setSignalTimeout(SIGNAL_TIME_OUT);
 
 		// Session Config and Sessions go hand in hand.
 		// Recover our session, if some reason we try to reopen for the same
 		// user and
 		// this very same config.
 		if (connection.getConfig().isSessionOpen()) {
 
 			// note:
 			// Passive updates are enabled by default, invalidations could
 			// be turned off to have finer control.
 
 			final CDOSession openSession = connection.getConfig().openSession();
 			setSession(openSession);
 			return;
 		}
 
 		final IPasswordCredentialsProvider credentialsProvider = new PasswordCredentialsProvider(
 				new PasswordCredentials(uid, passwd.toCharArray()));
 
 		connection.getConfig().getAuthenticator()
 				.setCredentialsProvider(credentialsProvider);
 
 		try {
 			final CDOSession cdoSession = connection.getConfig().openSession();
 
 			((org.eclipse.emf.cdo.net4j.CDOSession.Options) cdoSession
 					.options()).setCommitTimeout(COMMIT_TIMEOUT);
 			setSession(cdoSession);
 
 			//
 			// for (final EPackage ePackage : ePackages) {
 			// getSession().getPackageRegistry().putEPackage(ePackage);
 			// }
 		} catch (final SecurityException se) {
 			throw new SecurityException(se);
 		}
 	}
 
 	public abstract CDOSession getSession();
 
 	public abstract CDOTransaction getTransaction();
 
 	public abstract CDOView getView();
 
 	protected abstract boolean isTransactionSet();
 
 	protected abstract void setSession(CDOSession session);
 
 	protected abstract void setTransaction(CDOTransaction transaction);
 
 	/**
 	 * Close the session.
 	 */
 	public void closeSession() {
 		if (getSession() != null && !getSession().isClosed()) {
 			// Remove all listeners, before closing
 			CDOView[] views = this.getSession().getViews();
 			for (int i = 0; i < views.length; i++) {
 				CDOView cdoView = views[i];
 				if (cdoView.hasListeners()) {
 					ImmutableList<IListener> of = ImmutableList.of(cdoView
 							.getListeners());
 					for (IListener iListener : of) {
 						cdoView.removeListener(iListener);
 					}
 				}
 			}
 
 			getSession().close();
 		}
 		setSession(null);
 	}
 
 	/**
 	 * Opens a CDOSession without credentials unless the connection already
 	 * handles this.
 	 */
 	public CDOSession openSession() {
 		if (connection.getConfig() == null) {
 			connection.initialize();
 		}
 		final CDOSession cdoSession = connection.getConfig().openSession();
 
 		// do not load collection objects initially, load 300 objects when
 		// needed.
		cdoSession.options().setCollectionLoadingPolicy(
				CDOUtil.createCollectionLoadingPolicy(0, 300));
 
 		((org.eclipse.emf.cdo.net4j.CDOSession.Options) cdoSession.options())
 				.setCommitTimeout(COMMIT_TIMEOUT);
 
 		for (final EPackage ePackage : ePackages) {
 			cdoSession.getPackageRegistry().putEPackage(ePackage);
 		}
 		setSession(cdoSession);
 		return cdoSession;
 	}
 
 	public Resource getResource(ResourceSet set, EClass feature) {
 		final String res = resolveResourceName(feature);
 		return getResource(set, res);
 	}
 
 	public Resource getResource(ResourceSet set, int feature) {
 		final String res = resolveResourceName(feature);
 		return getResource(set, res);
 	}
 
 	public List<Resource> getResources(ResourceSet set, String resourcePath) {
 		// NOTE: We don't create resources for this function, as we assume the
 		// path is a folder.
 
 		final CDOView[] views = this.getSession().getViews();
 		for (int i = 0; i < views.length; i++) {
 			final CDOView view = views[i];
 			if (view.getResourceSet().equals(set)) {
 				if (view.hasResource(resourcePath)) {
 					CDOResourceNode node = view.getResourceNode(resourcePath);
 					if (node instanceof CDOResourceFolder) {
 						return getResourcesFromNode((CDOResourceFolder) node);
 					}
 				}
 			}
 		}
 		// create a transaction for all sub resources from this path.
 		final CDOTransaction transaction = getSession().openTransaction(set);
 
 		if (transaction.hasResource(resourcePath)) {
 			CDOResourceNode node = transaction.getResourceNode(resourcePath);
 			if (node instanceof CDOResourceFolder) {
 				return getResourcesFromNode((CDOResourceFolder) node);
 			}
 		}
 		return Collections.emptyList();
 	}
 
 	public List<Resource> getResources(String resourcePath) {
 		// NOTE: We don't create resources for this function, as we assume the
 		// path is a folder.
 
 		final CDOView[] views = this.getSession().getViews();
 		for (int i = 0; i < views.length; i++) {
 			final CDOView view = views[i];
 			if (view.hasResource(resourcePath)) {
 				CDOResourceNode node = view.getResourceNode(resourcePath);
 				if (node instanceof CDOResourceFolder) {
 					return getResourcesFromNode((CDOResourceFolder) node);
 				}
 			}
 		}
 		// create a transaction for all sub resources from this path, let CDO
 		// specify the resource set.
 		final CDOTransaction transaction = getSession().openTransaction();
 
 		if (transaction.hasResource(resourcePath)) {
 			CDOResourceNode node = transaction.getResourceNode(resourcePath);
 			if (node instanceof CDOResourceFolder) {
 				return getResourcesFromNode((CDOResourceFolder) node);
 			}
 		}
 		return Collections.emptyList();
 	}
 
 	private List<Resource> getResourcesFromNode(CDOResourceFolder cdoFolder) {
 		List<Resource> resources = Lists.newArrayList();
 		EList<CDOResourceNode> nodes = cdoFolder.getNodes();
 		for (CDOResourceNode n : nodes) {
 
 			if (n instanceof CDOResourceFolder) {
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_DATA_DETAILS_OPTION,
 							"CDOFolder uri: " + n.getURI());
 				}
 
 				resources.addAll(getResourcesFromNode((CDOResourceFolder) n));
 			} else if (n instanceof CDOResource) {
 
 				CDOResource res = (CDOResource) n;
 
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_DATA_DETAILS_OPTION,
 							"CDOResource uri:" + res.getURI()
 									+ " ResourceSet: "
 									+ res.getResourceSet().hashCode());
 				}
 				resources.add((Resource) n);
 			}
 
 		}
 		return resources;
 	}
 
 	/*
 	 * If any of the views has this resource.
 	 */
 	public boolean hasResource(URI uri) {
 		final String fragment = '/' + uri.lastSegment();
 		return hasResource(fragment);
 	}
 
 	/*
 	 * If any of the views has this resource.
 	 */
 	public boolean hasResource(String resourcePath) {
 		final CDOView[] views = this.getSession().getViews();
 		for (int i = 0; i < views.length; i++) {
 			final CDOView view = views[i];
 			if (view.hasResource(resourcePath)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public Resource getResource(ResourceSet set, String resourcePath) {
 		assert resourcePath != null && resourcePath.length() > 0;
 
 		// Before attempting to open a new CDOView, we want to know what is
 		// already
 		// in our session and resource set.
 
 		final CDOView[] views = this.getSession().getViews();
 		for (int i = 0; i < views.length; i++) {
 			final CDOView view = views[i];
 			if (view.getResourceSet().equals(set)) {
 
 				if (view.hasResource(resourcePath)) {
 					final CDOResource resource = view.getResource(resourcePath);
 					return resource;
 				}
 				// We haven't found this resource in the current view's set,
 				// but we can't create a new getTransaction(), so we have to see
 				// if
 				// the
 				// the CDOView has a getTransaction().
 				if (view instanceof CDOTransaction) {
 					final CDOTransaction transaction = (CDOTransaction) view;
 					view.options().setInvalidationPolicy(
 							CDOInvalidationPolicy.RELAXED);
 					final CDOResource resource = transaction
 							.getOrCreateResource(resourcePath);
 					return resource;
 				}
 			}
 		}
 
 		// We don't have a view, so let's open one.
 		final CDOTransaction transaction = getSession().openTransaction(set);
 		final CDOResource resource = transaction
 				.getOrCreateResource(resourcePath);
 		return resource;
 	}
 
 	public Resource getResource(CDOView view, EClass feature) {
 		final String resName = resolveResourceName(feature);
 		return getResource(view, resName);
 	}
 
 	public Resource getResource(CDOView view, String res) {
 
 		assert res != null && res.length() > 0;
 
 		// Before attempting to open a new CDOView, we want to know what is
 		// already
 		// in our session and resource set.
 		if (view.getResourceSet() != null) {
 			if (view.hasResource(res)) {
 				final CDOResource resource = view.getResource(res);
 				return resource;
 			}
 		} else {
 			return null; // We need a resourceset.
 		}
 
 		// We haven't found this resource in the current view's and set's,
 		// but we can't create a new getTransaction(), so we have to see if the
 		// the CDOView has a getTransaction().
 		if (view instanceof CDOTransaction) {
 			final CDOTransaction transaction = (CDOTransaction) view;
 			// Should create in the set.
 			final CDOResource resource = transaction.getOrCreateResource(res);
 			return resource;
 		}
 		return null;
 	}
 
 	private String resolveResourceName(EClass clazz) {
 		return "/" + clazz.getName();
 	}
 
 	/**
 	 * Dispatcher to break-up which objects go into which resources.
 	 * 
 	 * @deprecated
 	 * @param feature
 	 * @return
 	 */
 	@Deprecated
 	private String resolveResourceName(int feature) {
 		String resource = "/";
 		// We switch on the resource to use.
 		switch (feature) {
 		case NetxstudioPackage.NETXSTUDIO: {
 			resource += "netxstudio";
 		}
 			break;
 		case LibraryPackage.LIBRARY: {
 			resource += "library";
 		}
 			break;
 		}
 		return resource;
 	}
 
 	private String resolveResourceCommitInfo(String userID) {
 		return "CDOCommitInfo_" + userID;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.data.IDataProvider#getResource(int)
 	 */
 	public Resource getResource(int feature) {
 		final String res = resolveResourceName(feature);
 		assert res != null && res.length() > 0;
 		return this.getResource(res);
 	}
 
 	/**
 	 * This call will potentially create a transaction.
 	 */
 	public Resource getResource(EClass feature) {
 		final String resPath = resolveResourceName(feature);
 		assert resPath != null && resPath.length() > 0;
 		return this.getResource(resPath);
 	}
 
 	public Resource getCommitInfoResource(String userID) {
 		final String resourceName = this.resolveResourceCommitInfo(userID);
 		CDOResource resource = this.resolveInCurrentView(resourceName);
 		if (resource == null) {
 			final CDOTransaction transaction = getSession().openTransaction();
 			resource = transaction.getOrCreateResource(resourceName);
 		}
 		return resource;
 	}
 
 	/*
 	 * Get the resource from a URI.
 	 */
 	public Resource getResource(URI uri) {
 		// Strip the repo etc we only need the name prepend with a slash.
 		// String schema = uri.scheme();
 		final String fragment = '/' + uri.lastSegment();
 		return this.getResource(fragment);
 	}
 
 	/*
 	 * Get the resource from a URI.
 	 */
 	public Resource getResource(ResourceSet set, URI uri) {
 		// Strip the repo etc we only need the name prepend with a slash.
 		// String schema = uri.scheme();
 		final String fragment = '/' + uri.lastSegment();
 		return this.getResource(set, fragment);
 	}
 
 	public Resource getResource(String resourcePath) {
 		if (doGetResourceFromOwnTransaction()) {
 			final CDOResource resource = resolveInCurrentView(resourcePath);
 
 			if (resource == null) {
 				final CDOTransaction transaction = getSession()
 						.openTransaction();
 				return transaction.getOrCreateResource(resourcePath);
 			}
 			return resource;
 		} else {
 
 			try {
 
 				return getTransaction().getOrCreateResource(resourcePath);
 
 			} catch (CDOException ce) {
 				System.out.println("DATAPROVIDER: error creating resource: "
 						+ resourcePath);
 				ce.printStackTrace();
 				return this.createResourceWithFolderFirst(resourcePath);
 				// return null;
 			}
 		}
 	}
 
 	/**
 	 * To avoid the CDO Exception "Not a resource folder" when creating a long
 	 * path resource.
 	 * 
 	 * @param path
 	 * @return
 	 */
 	private Resource createResourceWithFolderFirst(String path) {
 
 		CDOResourceNode resolveLastExistingNode = this
 				.resolveLastExistingNode(path);
 
 		if (resolveLastExistingNode instanceof CDOResourceFolder) {
 			String existsPath = ((CDOResourceFolder) resolveLastExistingNode)
 					.getPath();
 			int lastIndexOf = path.lastIndexOf(existsPath);
 			String substring = path.substring(lastIndexOf);
 			return ((CDOResourceFolder) resolveLastExistingNode)
 					.addResource(substring);
 		} else if (resolveLastExistingNode instanceof CDOResourceNode) {
 			// not sure.
 			CDOResource orCreateResource = this.getTransaction()
 					.getOrCreateResource(path);
 			return orCreateResource;
 
 		}
 		return null;
 	}
 
 	private CDOResourceNode resolveLastExistingNode(String path) {
 
 		CDOResourceNode lastExistingNode = null;
 
 		URI pathURI = URI.createURI(path);
 		List<String> segmentsList = pathURI.segmentsList();
 		String incrementalSegment = "";
 		for (String segment : segmentsList) {
 			incrementalSegment = incrementalSegment.concat("/" + segment);
 			CDOResourceNode resourceNode = this.getTransaction()
 					.getResourceNode(incrementalSegment);
 			if (resourceNode != null) {
 				lastExistingNode = resourceNode;
 
 			} else {
 				// We failed return last valid.
 				break;
 			}
 
 		}
 
 		return lastExistingNode;
 
 	}
 
 	/**
 	 * Forces to resolve the resource from
 	 * 
 	 * @return
 	 */
 	protected boolean doGetResourceFromOwnTransaction() {
 		return doGetResourceFromOwnTransaction;
 	}
 
 	private CDOResource resolveInCurrentView(String resourceName) {
 		final CDOView[] views = this.getSession().getViews();
 		for (int i = 0; i < views.length; i++) {
 			final CDOView view = views[i];
 			if (view.hasResource(resourceName)) {
 				final CDOResource resource = view.getResource(resourceName);
 				if (resource != null) {
 					return resource;
 				}
 			}
 		}
 		return null;
 	}
 
 	public void commitTransaction() {
 		commitTransaction(CLIENT_COMMIT_COMMENT);
 	}
 
 	public void rollbackTransaction() {
 		try {
 			if (isTransactionSet()) {
 				getTransaction().rollback();
 			}
 		} catch (final Exception e) {
 			throw new IllegalStateException(e);
 		} finally {
 			getTransaction().close();
 			setTransaction(null);
 		}
 	}
 
 	public void setDoGetResourceFromOwnTransaction(
 			boolean doGetResourceFromOwnTransaction) {
 		this.doGetResourceFromOwnTransaction = doGetResourceFromOwnTransaction;
 	}
 
 	public void commitTransaction(String commitComment) {
 		try {
 			if (isTransactionSet()) {
 				if (commitComment != null && commitComment.length() > 0) {
 					this.getTransaction().setCommitComment(commitComment);
 					getTransaction().commit();
 				}
 			}
 		} catch (final Exception e) {
 			throw new IllegalStateException(e);
 		} finally {
 
 			CDOTransaction transaction = getTransaction();
 
 			if (!transaction.isClosed()) {
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_DATA_OPTION,
 							"Closing transaction with ID: "
 									+ transaction.getViewID());
 				}
 				transaction.close();
 			}
 			setTransaction(null);
 		}
 	}
 
 }
