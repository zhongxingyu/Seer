 /*******************************************************************************
  *  Copyright (c) 2011 University Of Moratuwa
  *                                                                      
  * All rights reserved. This program and the accompanying materials     
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at             
  * http://www.eclipse.org/legal/epl-v10.html                            
  *                                                                      
  * Contributors:                                                        
  *    Isuru Udana - UI Integration in the Workbench
  *******************************************************************************/
 package org.eclipse.ecf.salvo.ui.external.provider;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.expressions.EvaluationContext;
 import org.eclipse.core.expressions.EvaluationResult;
 import org.eclipse.core.expressions.Expression;
 import org.eclipse.core.expressions.ExpressionConverter;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.ecf.protocol.nntp.core.Debug;
 import org.eclipse.ecf.protocol.nntp.core.NewsgroupFactory;
 import org.eclipse.ecf.protocol.nntp.core.ServerFactory;
 import org.eclipse.ecf.protocol.nntp.core.ServerStoreFactory;
 import org.eclipse.ecf.protocol.nntp.model.AbstractCredentials;
 import org.eclipse.ecf.protocol.nntp.model.INewsgroup;
 import org.eclipse.ecf.protocol.nntp.model.IServer;
 import org.eclipse.ecf.protocol.nntp.model.IServerConnection;
 import org.eclipse.ecf.protocol.nntp.model.IServerStoreFacade;
 import org.eclipse.ecf.protocol.nntp.model.NNTPException;
 import org.eclipse.ecf.salvo.ui.internal.Activator;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPerspectiveDescriptor;
 import org.eclipse.ui.IWorkbenchPart;
 
 /**
  * This class handles the other projects hooked into salvo by the
  * newsgroupProvider extension point.
  * 
  * @author isuru Please note that this is under construction
  * 
  */
 public class HookedNewsgroupProvider {
 
 	private static final String EXTENSIONPOINT_ID = "org.eclipse.ecf.salvo.ui.newsgroupProvider";
 
 	private static HookedNewsgroupProvider INSTANCE;
 
 	/**
 	 * 
 	 * @return a instance of HookedNewsgroupProvider
 	 */
 	public static HookedNewsgroupProvider instance() {
 		if (INSTANCE == null) {
 			INSTANCE = new HookedNewsgroupProvider();
 		}
 		return INSTANCE;
 	}
 
 	/**
 	 * 
 	 * @return Evaluation Context to evaluate core expression
 	 */
 	private EvaluationContext getEvaluationContext() {
 
 		// the active part
 		IWorkbenchPart activePart = Activator.getDefault().getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage().getActivePart();
 
 		// the active editor part
 		IEditorPart activeEditorPart = Activator.getDefault().getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 
 		// the active perspective
 		IPerspectiveDescriptor activePerspective = Activator.getDefault()
 				.getWorkbench().getActiveWorkbenchWindow().getActivePage()
 				.getPerspective();
 
 		EvaluationContext context = new EvaluationContext(null, activePart
 				.getSite().getId());
 
 		// Adding context variables
 		context.addVariable("activePartId", activePart.getSite().getId());
 
 		if (activeEditorPart != null) {
 			context.addVariable("activeEditorId", activeEditorPart.getSite()
 					.getId());
		} else { // when no editor is active
			context.addVariable("activeEditorId", "NO_ACTIVE_EDITOR"); // assign a dummy value to activeEditorId 
 		}
 
		context.addVariable("activePerspectiveId",
 					activePerspective.getId());
 
 		return context;
 	}
 
 	/**
 	 * Check whether server defined by the provider is already subscribed
 	 * 
 	 * @param provider
 	 *            Newsgroup Provider
 	 * @return whether server defined by the provider is already subscribed
 	 */
 	public boolean isServerSubscribed(INewsGroupProvider provider) {
 		IServerStoreFacade storeFacade = ServerStoreFactory.instance()
 				.getServerStoreFacade();
 
 		try {
 			for (IServer currentServer : storeFacade.getServers()) {
 				if ((currentServer.getAddress().equals(provider
 						.getServerAddress()))
 						&& (currentServer.getPort() == provider.getServerPort())) {
 					return true;
 				}
 			}
 		} catch (NNTPException e) {
 			Debug.log(getClass(), e);
 		}
 		return false;
 	}
 
 	/**
 	 * Get the Newsgroup from the Newsgroup Provider
 	 * 
 	 * @param provider
 	 *            INewsGroupProvider
 	 * @return the INewsgroup of the INewsGroupProvider
 	 */
 	public INewsgroup getNewsgroup(INewsGroupProvider provider) {
 
 		IServerStoreFacade storeFacade = ServerStoreFactory.instance()
 				.getServerStoreFacade();
 
 		INewsgroup group = null;
 
 		try {
 			// server
 			IServer server = null;
 
 			for (IServer currentServer : storeFacade.getServers()) {
 				if ((currentServer.getAddress().equals(provider
 						.getServerAddress()))
 						&& (currentServer.getPort() == provider.getServerPort())) {
 					server = currentServer;
 				}
 			}
 
 			if (server == null) {
 
 				// credentials
 				String password = provider.getPassword();
 
 				final AbstractCredentials credentials = new AbstractCredentials(
 						provider.getUser(), provider.getEmail(),
 						provider.getLogin(), password);
 
 				// create server
 				server = ServerFactory.getCreateServer(
 						provider.getServerAddress(), provider.getServerPort(),
 						credentials, provider.isSecure());
 				IServerConnection connection = server.getServerConnection();
 				connection.disconnect();
 				connection.connect();
 				connection.setModeReader(server);
 				connection.getOverviewHeaders(server);
 
 				// Subscribe Server
 				try {
 					storeFacade.subscribeServer(server, password);
 				} catch (NNTPException e1) {
 					Debug.log(getClass(), e1);
 				}
 			}
 
 			// Newsgroup
 
 			// check whether newsgroup is already subscribed
 			INewsgroup[] subscribedNewsgroups = storeFacade
 					.getSubscribedNewsgroups(server);
 			boolean isProviderNewsgroupSubscribed = false;
 			for (INewsgroup subscribedNewsgroup : subscribedNewsgroups) {
 				if (subscribedNewsgroup.getNewsgroupName().equals(
 						provider.getNewsgroupName())) {
 					isProviderNewsgroupSubscribed = true;
 					group = subscribedNewsgroup;
 					break;
 				}
 			}
 
 			// If newsgroup is not already subscribed create a new newsgroup
 			if (!isProviderNewsgroupSubscribed) {
 				// Attach a newsgroup to the server
 				group = NewsgroupFactory.createNewsGroup(server,
 						provider.getNewsgroupName(),
 						provider.getNewsgroupDescription());
 			}
 
 			// Subscribing to the newsgroup is done in performFinish() of
 			// the wizard.
 
 		} catch (NNTPException e) {
 			Debug.log(getClass(), e);
 		}
 
 		return group;
 	}
 
 	/**
 	 * Get the newsgroup providers matches the current context - Suggested
 	 * newsgroup providers
 	 * 
 	 * @return NewsGroup Providers
 	 */
 	public INewsGroupProvider[] getProviders() {
 
 		final ArrayList<INewsGroupProvider> newsgroupProvider = new ArrayList<INewsGroupProvider>();
 
 		IConfigurationElement[] config = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor(EXTENSIONPOINT_ID);
 
 		for (final IConfigurationElement newsgroup : config) {
 
 			IConfigurationElement enablement = newsgroup
 					.getChildren("enablement")[0];
 
 			try {
 
 				Expression expression = ExpressionConverter.getDefault()
 						.perform(enablement);
 
 				final EvaluationResult result = expression
 						.evaluate(getEvaluationContext());
 
 				if (result == EvaluationResult.TRUE) {
 					final Object provider = newsgroup
 							.createExecutableExtension("class");
 					newsgroupProvider.add((INewsGroupProvider) provider);
 				}
 
 			} catch (CoreException e) {
 				Debug.log(getClass(), e);
 			}
 
 		}
 		return (INewsGroupProvider[]) newsgroupProvider
 				.toArray(new INewsGroupProvider[0]);
 	}
 
 }
