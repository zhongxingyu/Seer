 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.testtool.sketch.features;
 
 import org.eclipse.graphiti.IName;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 
 public class SwitchModeFeature extends AbstractCustomFeature {
 
 	public SwitchModeFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	private int calculateNextTBPIndex() {
 		IDiagramTypeProvider dtp = getFeatureProvider().getDiagramTypeProvider();
 		IToolBehaviorProvider[] availableToolBehaviorProviders = dtp.getAvailableToolBehaviorProviders();
 		int providerCount = availableToolBehaviorProviders.length;
		int newIndex = (dtp.getCurrentToolBahaviorIndex() + 1) % providerCount;
 		return newIndex;
 	}
 
 	@Override
 	public boolean canExecute(ICustomContext context) {
 		if (getDiagramEditor().isDirty()) {
 			return false;
 		}
 
 		IToolBehaviorProvider[] availableToolBehaviorProviders = getFeatureProvider().getDiagramTypeProvider()
 				.getAvailableToolBehaviorProviders();
 		return availableToolBehaviorProviders.length > 1;
 	}
 
 	public void execute(ICustomContext context) {
 		IDiagramTypeProvider dtp = getFeatureProvider().getDiagramTypeProvider();
 		int newIndex = calculateNextTBPIndex();
		dtp.setCurrentToolBahaviorIndex(newIndex);
 	}
 
 	@Override
 	public String getDescription() {
 		return getName();
 	}
 
 	@Override
 	public String getName() {
 		IDiagramTypeProvider dtp = getFeatureProvider().getDiagramTypeProvider();
 		IToolBehaviorProvider nextTBP = dtp.getAvailableToolBehaviorProviders()[calculateNextTBPIndex()];
 		String name = nextTBP.getClass().getSimpleName();
 		if (nextTBP instanceof IName) {
 			name = ((IName) nextTBP).getName();
 
 		}
 		return "Switch to " + name;
 	}
 
 	@Override
 	public boolean isAvailable(IContext context) {
 		if (context instanceof ICustomContext) {
 			ICustomContext cContext = (ICustomContext) context;
 			PictogramElement[] pes = cContext.getPictogramElements();
 			if (pes.length == 1) {
 				if (pes[0] instanceof Diagram) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
