 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 329523 - Add notification of DiagramTypeProvider after saving a diagram
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.dt;
 
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.internal.services.GraphitiInternal;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.notification.DefaultNotificationService;
 import org.eclipse.graphiti.notification.INotificationService;
 import org.eclipse.graphiti.platform.AbstractExtension;
 import org.eclipse.graphiti.platform.IDiagramEditor;
 import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRendererFactory;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 
 /**
  * The Class AbstractDiagramTypeProvider.
 * 
  */
 public abstract class AbstractDiagramTypeProvider extends AbstractExtension implements IDiagramTypeProvider {
 
 	private IToolBehaviorProvider[] availableToolBehaviorProviders = null;
 
 	private Diagram diagram;
 
 	private IDiagramEditor diagramEditor;
 
 	private IFeatureProvider featureProvider;
 
 	private INotificationService notificationService;
 
 	private int currentToolBehaviorIndex = 0;
 
 	/**
 	 * Creates a new {@link AbstractDiagramTypeProvider}.
 	 */
 	public AbstractDiagramTypeProvider() {
 		super();
 	}
 
 	/**
 	 * Returns all available tool behavior providers
 	 * 
 	 * @return An array of all registered tool behavior providers
 	 */
 	@Override
 	public IToolBehaviorProvider[] getAvailableToolBehaviorProviders() {
 		if (this.availableToolBehaviorProviders == null) {
 			this.availableToolBehaviorProviders = new IToolBehaviorProvider[] { new DefaultToolBehaviorProvider(this) };
 		}
 		return this.availableToolBehaviorProviders;
 	}
 
 	@Override
 	public IToolBehaviorProvider getCurrentToolBehaviorProvider() {
 		IToolBehaviorProvider ret = null;
 		if (getAvailableToolBehaviorProviders().length > 0) {
 			ret = getAvailableToolBehaviorProviders()[getCurrentToolBehaviorIndex()];
 		}
 		return ret;
 	}
 
 	@Override
 	public int getCurrentToolBehaviorIndex() {
 		return this.currentToolBehaviorIndex;
 	}
 
 	@Override
 	public void setCurrentToolBehaviorIndex(int index) {
 		if (this.currentToolBehaviorIndex != index) {
 			if (index < 0 || index >= getAvailableToolBehaviorProviders().length) {
 				throw new IllegalArgumentException("Index not valid"); //$NON-NLS-1$
 			}
 			this.currentToolBehaviorIndex = index;
 
 			IDiagramEditor de = getDiagramEditor();
 			de.refresh();
 			de.refreshPalette();
 		}
 	}
 
 	@Override
 	public Diagram getDiagram() {
 		return this.diagram;
 	}
 
 	@Override
 	public String getDiagramTitle() {
 		String name = ""; //$NON-NLS-1$
 		if (getDiagram() != null) {
 			name = getDiagram().getName();
 		}
 		return name;
 	}
 
 	@Override
 	public IDiagramEditor getDiagramEditor() {
 		return this.diagramEditor;
 	}
 
 	@Override
 	public IFeatureProvider getFeatureProvider() {
 		if (this.featureProvider == null) {
 			T.racer().error("featureProvider is null"); //$NON-NLS-1$
 		}
 		return this.featureProvider;
 	}
 
 	@Override
 	public void init(Diagram diagram, IDiagramEditor diagramEditor) {
 		setDiagram(diagram);
 		GraphitiInternal.getEmfService().wireDTPToDiagram(diagram, this);
 		setDiagramEditor(diagramEditor);
 	}
 
 	private void setDiagramEditor(IDiagramEditor diagramEditor) {
 		this.diagramEditor = diagramEditor;
 	}
 
 	/**
 	 * @param diagram
 	 *            The diagram to set.
 	 */
 	private void setDiagram(Diagram diagram) {
 		this.diagram = diagram;
 	}
 
 	/**
 	 * Sets the feature provider.
 	 * 
 	 * @param featureProvider
 	 *            The featureProvider to set.
 	 */
 	protected void setFeatureProvider(IFeatureProvider featureProvider) {
 		this.featureProvider = featureProvider;
 	}
 
 	@Override
 	public boolean isAutoUpdateAtRuntime() {
 		return true;
 	}
 
 	@Override
 	public boolean isAutoUpdateAtStartup() {
 		return false;
 	}
 
 	@Override
 	public boolean isAutoUpdateAtReset() {
 		return true;
 	}
 
 	@Override
 	public void dispose() {
 		if (getCurrentToolBehaviorProvider() != null) {
 			getCurrentToolBehaviorProvider().dispose();
 		}
 		if (getFeatureProvider() != null) {
 			getFeatureProvider().dispose();
 		}
 	}
 
 	@Override
 	public INotificationService getNotificationService() {
 		if (this.notificationService == null) {
 			this.notificationService = new DefaultNotificationService(this);
 		}
 		return this.notificationService;
 	}
 
 	@Override
 	public Object[] getRelatedBusinessObjects(Object[] bos) {
 		return new Object[0];
 	}
 
 	@Override
 	public IGraphicsAlgorithmRendererFactory getGraphicsAlgorithmRendererFactory() {
 		return null;
 	}
 
 	@Override
 	public void postInit() {
 	}
 
 	@Override
 	public void resourceReloaded(Diagram diagram) {
 		setDiagram(diagram);
 	}
 
 	@Override
 	public void resourcesSaved(Diagram diagram, Resource[] savedResources) {
 	}
 }
