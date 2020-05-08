 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.structure.support;
 
 import org.eclipse.etrice.core.validation.ValidationUtil;
 import org.eclipse.etrice.ui.common.support.NoResizeFeature;
 import org.eclipse.etrice.ui.structure.ImageProvider;
 import org.eclipse.etrice.ui.structure.dialogs.SPPPropertyDialog;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDoubleClickContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IPictogramElementContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.mm.algorithms.Ellipse;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.tb.ContextButtonEntry;
 import org.eclipse.graphiti.tb.IContextButtonPadData;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 import org.eclipse.etrice.core.naming.RoomNameProvider;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.InterfaceItem;
 import org.eclipse.etrice.core.room.RoomFactory;
 import org.eclipse.etrice.core.room.SPPRef;
 
 public class SPPSupport extends InterfaceItemSupport {
 	
 	private static class FeatureProvider extends InterfaceItemSupport.FeatureProvider {
 		
 		private static class CreateFeature extends InterfaceItemSupport.FeatureProvider.CreateFeature {
 	
 			private boolean doneChanges = false;
 	
 			public CreateFeature(IFeatureProvider fp) {
 				super(fp, false, "SPP", "create SPP");
 			}
 			
 			@Override
 			public String getCreateImageId() {
 				return ImageProvider.IMG_SPP;
 			}
 	
 			@Override
 			public Object[] create(ICreateContext context) {
 				ActorContainerClass acc = (ActorContainerClass) context.getTargetContainer().getLink().getBusinessObjects().get(0);
 
 				// create SPP
 		        SPPRef spp = RoomFactory.eINSTANCE.createSPPRef();
 		        spp.setName(RoomNameProvider.getUniqueInterfaceItemName("spp", acc));
 				
 				acc.getIfSPPs().add(spp);
 		        
 		        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 		        SPPPropertyDialog dlg = new SPPPropertyDialog(shell, spp, acc, true, false);
 				if (dlg.open()!=Window.OK)
 					// find a method to abort creation
 					//throw new RuntimeException();
 					return EMPTY;
 				
 				doneChanges = true;
 		        
 		        // do the add
 		        addGraphicalRepresentation(context, spp);
 	
 		        // return newly created business object(s)
 		        return new Object[] { spp };
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return doneChanges;
 			}
 		}
 		
 		private class AddFeature extends InterfaceItemSupport.FeatureProvider.AddFeature {
 	
 			public AddFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			protected String getItemKind(InterfaceItem item) {
 				if (item instanceof SPPRef)
 					return getSPPKind((SPPRef)item);
 				
 				return "";
 			}
 
 			@Override
 			protected void createItemFigure(InterfaceItem item,
 					boolean refitem, ContainerShape containerShape,
 					GraphicsAlgorithm invisibleRectangle, Color darkColor,
 					Color brightDolor) {
 				
 				if (item instanceof SPPRef)
 					createSPPFigure((SPPRef) item, refitem, containerShape, invisibleRectangle, darkColor, brightDolor);
 			}
 	
 		}
 	
 		private static class PropertyFeature extends InterfaceItemSupport.FeatureProvider.PropertyFeature {
 
 			public PropertyFeature(IFeatureProvider fp) {
 				super(fp, "Edit SPP...", "Edit SPP Properties");
 			}
 
 			@Override
 			public boolean canExecute(ICustomContext context) {
 				if (!super.canExecute(context))
 					return false;
 				
 				PictogramElement[] pes = context.getPictogramElements();
 				if (pes != null && pes.length == 1 && pes[0] instanceof ContainerShape) {
 					Object bo = getBusinessObjectForPictogramElement(pes[0]);
 					return (bo instanceof SPPRef);
 				}
 				return false;
 			}
 
 			@Override
 			public void execute(ICustomContext context) {
 				SPPRef spp = (SPPRef) getBusinessObjectForPictogramElement(context.getPictogramElements()[0]);
 				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 				ActorContainerClass acc = (ActorContainerClass)spp.eContainer();
 				boolean refport = isRefItem(context.getPictogramElements()[0]);
 				
 				SPPPropertyDialog dlg = new SPPPropertyDialog(shell, spp, acc, false, refport);
 				if (dlg.open()!=Window.OK)
 					// TODOHRR: introduce a method to revert changes, does hasDoneChanges=false roll back changes?
 					//throw new RuntimeException();
 					return;
 				
 				updateSPPFigure(spp, context.getPictogramElements()[0], manageColor(DARK_COLOR), manageColor(BRIGHT_COLOR));
 			}
 			
 		}
 		
 		private class UpdateFeature extends InterfaceItemSupport.FeatureProvider.UpdateFeature {
 
 			public UpdateFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			protected String getItemKind(InterfaceItem item) {
 				if (item instanceof SPPRef)
 					return getSPPKind((SPPRef)item);
 				
 				return "";
 			}
 
 			@Override
 			protected void updateFigure(InterfaceItem item,
 					PictogramElement pe, Color dark, Color bright) {
 				updateSPPFigure((SPPRef)item, pe, dark, bright);
 			}
 			
 		}
 		
 		public FeatureProvider(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 			super(dtp, fp);
 		}
 		
 		@Override
 		public ICreateFeature[] getCreateFeatures() {
 			return new ICreateFeature[] { new CreateFeature(fp) };
 		}
 		
 		@Override
 		public IAddFeature getAddFeature(IAddContext context) {
 			return new AddFeature(fp);
 		}
 	
 		@Override
 		public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
 			return new MoveShapeFeature(fp);
 		}
 		
 		@Override
 		public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
 			return new NoResizeFeature(fp);
 		}
 	
 		@Override
 		public ICustomFeature[] getCustomFeatures(ICustomContext context) {
 			return new ICustomFeature[] { new PropertyFeature(fp) };
 		}
 		
 		@Override
 		public IUpdateFeature getUpdateFeature(IUpdateContext context) {
 			return new UpdateFeature(fp);
 		}
 
 		protected static void createSPPFigure(SPPRef spp, boolean refspp,
 				ContainerShape containerShape,
 				GraphicsAlgorithm invisibleRectangle, Color darkColor, Color brightDolor) {
 
 			boolean relay = ValidationUtil.isRelay(spp);
 			
 			int size = refspp?ITEM_SIZE_SMALL:ITEM_SIZE;
 			int line = refspp?LINE_WIDTH/2:LINE_WIDTH;
 			
 			Color bg = brightDolor;
 			if (refspp) {
 				bg = darkColor;
 			}
 			else {
 				if (relay)
 					bg = brightDolor;
 				else
 					bg = darkColor;
 			}
 
 			IGaService gaService = Graphiti.getGaService();
 			
 			// TODOHRR: depicting SPPs as diamond using polygon didn't work
 //			int s2 = size/2;
 //			int xy[] = new int[] { s2, 0, size, s2, s2, size, 0, s2};
 //			Polygon rect = gaService.createPolygon(invisibleRectangle, xy);
 //			rect.setForeground(darkColor);
 //			rect.setBackground(bg);
 //			rect.setLineWidth(line);
 //			gaService.setLocation(rect, s2, s2);
 			//Rectangle rect = gaService.createRectangle(invisibleRectangle);
 			
 			Ellipse rect = gaService.createEllipse(invisibleRectangle);
 			rect.setForeground(darkColor);
 			rect.setBackground(bg);
 			rect.setLineWidth(line);
 			gaService.setLocationAndSize(rect, size/2, size/2, size, size);
 
 			if (containerShape.getAnchors().isEmpty()) {
 				// here we place our anchor
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 //				FixPointAnchor anchor = peCreateService.createFixPointAnchor(containerShape);
 //				anchor.setLocation(gaService.createPoint(xy[0], xy[1]));
 //				anchor = peCreateService.createFixPointAnchor(containerShape);
 //				anchor.setLocation(gaService.createPoint(xy[2], xy[3]));
 //				anchor = peCreateService.createFixPointAnchor(containerShape);
 //				anchor.setLocation(gaService.createPoint(xy[4], xy[5]));
 //				anchor = peCreateService.createFixPointAnchor(containerShape);
 //				anchor.setLocation(gaService.createPoint(xy[6], xy[7]));
 				// TODOHRR:  EllipseAnchor would be nice
 				ChopboxAnchor anchor = peCreateService.createChopboxAnchor(containerShape);
 				anchor.setReferencedGraphicsAlgorithm(rect);
 			}
 			else {
 				// we just set the referenced GA
 				//containerShape.getAnchors().get(0).setReferencedGraphicsAlgorithm(rect);
 			}
 		}
 
 		private static void updateSPPFigure(SPPRef spp, PictogramElement pe, Color dark, Color bright) {
 			ContainerShape container = (ContainerShape)pe;
 			
 			// we clear the figure and rebuild it
 			GraphicsAlgorithm invisibleRect = pe.getGraphicsAlgorithm();
 			invisibleRect.getGraphicsAlgorithmChildren().clear();
 			
 			createSPPFigure(spp, false, container, invisibleRect, dark, bright);
 			
 			GraphicsAlgorithm ga = container.getChildren().get(0).getGraphicsAlgorithm();
 			if (ga instanceof Text) {
 				((Text)ga).setValue(spp.getName());
 			}
 
 		}
 		
 	}
 
 	private class BehaviorProvider extends InterfaceItemSupport.BehaviorProvider {
 
 		public BehaviorProvider(IDiagramTypeProvider dtp) {
 			super(dtp);
 		}
 		
 		@Override
 		public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
 			return new FeatureProvider.PropertyFeature(getDiagramTypeProvider().getFeatureProvider());
 		}
 		
 		@Override
 		public IContextButtonPadData getContextButtonPad(
 				IPictogramElementContext context) {
 			
 			IContextButtonPadData data = super.getContextButtonPad(context);
 			PictogramElement pe = context.getPictogramElement();
 
 			CreateConnectionContext ccc = new CreateConnectionContext();
 			ccc.setSourcePictogramElement(pe);
 			Anchor anchor = null;
 			if (pe instanceof AnchorContainer) {
 				// our spp has four fixed point anchor - we choose the first one
 				anchor = ((ContainerShape)pe).getAnchors().get(0);
 			}
 			ccc.setSourceAnchor(anchor);
 			
 			ContextButtonEntry button = new ContextButtonEntry(null, context);
			button.setText("Create Layer Connection");
			button.setIconId(ImageProvider.IMG_LAYER_CONNECTION);
 			ICreateConnectionFeature[] features = getFeatureProvider().getCreateConnectionFeatures();
 			for (ICreateConnectionFeature feature : features) {
 				if (feature.isAvailable(ccc) && feature.canStartConnection(ccc))
 					button.addDragAndDropFeature(feature);
 			}
 
 			if (button.getDragAndDropFeatures().size() > 0) {
 				data.getDomainSpecificContextButtons().add(button);
 			}
 
 			return data;
 		}
 	}
 	
 	private FeatureProvider pfp;
 	private BehaviorProvider tbp;
 	
 	public SPPSupport(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 		pfp = new FeatureProvider(dtp,fp);
 		tbp = new BehaviorProvider(dtp);
 	}
 	
 	public IFeatureProvider getFeatureProvider() {
 		return pfp;
 	}
 	
 	public IToolBehaviorProvider getToolBehaviorProvider() {
 		return tbp;
 	}
 	
 	protected static String getSPPKind(SPPRef spp) {
 		String kind = "";
 		if (ValidationUtil.isRelay(spp))
 			kind += "R";
 		return kind;
 	}
 }
