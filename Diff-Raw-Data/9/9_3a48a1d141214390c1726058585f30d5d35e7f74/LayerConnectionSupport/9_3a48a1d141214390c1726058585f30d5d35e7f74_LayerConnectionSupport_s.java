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
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.LayerConnection;
 import org.eclipse.etrice.core.room.RefSAPoint;
 import org.eclipse.etrice.core.room.RelaySAPoint;
 import org.eclipse.etrice.core.room.RoomFactory;
 import org.eclipse.etrice.core.room.SAPoint;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.SPPoint;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.validation.ValidationUtil;
 import org.eclipse.etrice.ui.common.support.DeleteWithoutConfirmFeature;
 import org.eclipse.etrice.ui.structure.ImageProvider;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.IReconnectionFeature;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IAddConnectionContext;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICreateConnectionContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IReconnectionContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
 import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
 import org.eclipse.graphiti.features.impl.DefaultReconnectionFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.ManhattanConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.PictogramsFactory;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 
 public class LayerConnectionSupport {
 
 	private static final IColorConstant LINE_COLOR = new ColorConstant(0, 0, 0);
 	private static final int LINE_WIDTH = 2;
 	
 	class FeatureProvider extends DefaultFeatureProvider {
 		
 		private class CreateFeature extends AbstractCreateConnectionFeature {
 			
 			public CreateFeature(IFeatureProvider fp) {
 				super(fp, "LayerConnection", "create LayerConnection");
 			}
 			
 			@Override
 			public String getCreateImageId() {
 				return ImageProvider.IMG_LAYER_CONNECTION;
 			}
 	
 			@Override
 			public boolean canCreate(ICreateConnectionContext context) {
 				IFeatureProvider featureProvider = getFeatureProvider();
 				SPPRef src = SupportUtil.getSPPRef(context.getSourceAnchor(), featureProvider);
 				ActorContainerRef srcRef = SupportUtil.getRef(context.getSourceAnchor(), featureProvider);
 				SPPRef tgt = SupportUtil.getSPPRef(context.getTargetAnchor(), featureProvider);
 				ActorContainerRef tgtRef = SupportUtil.getRef(context.getTargetAnchor(), featureProvider);
 				StructureClass sc = SupportUtil.getParent(context, featureProvider);
 				
 				return ValidationUtil.isConnectable(src, srcRef, tgt, tgtRef, sc).isOk();
 			}
 			
 			public boolean canStartConnection(ICreateConnectionContext context) {
 				IFeatureProvider featureProvider = getFeatureProvider();
 				SPPRef src = SupportUtil.getSPPRef(context.getSourceAnchor(), featureProvider);
 				ActorContainerRef ref = SupportUtil.getRef(context.getSourceAnchor(), featureProvider);
 				StructureClass sc = SupportUtil.getParent(context, featureProvider);
 				return ValidationUtil.isConnectableSrc(src, ref, sc);
 			}
 			
 			@Override
 			public Connection create(ICreateConnectionContext context) {
 				IFeatureProvider featureProvider = getFeatureProvider();
 				SPPRef src = SupportUtil.getSPPRef(context.getSourceAnchor(), featureProvider);
 				ActorContainerRef srcRef = SupportUtil.getRef(context.getSourceAnchor(), featureProvider);
 				SPPRef dst = SupportUtil.getSPPRef(context.getTargetAnchor(), featureProvider);
 				ActorContainerRef dstRef = SupportUtil.getRef(context.getTargetAnchor(), featureProvider);
 				StructureClass sc = SupportUtil.getParent(context, featureProvider);
 				
 				LayerConnection lc = RoomFactory.eINSTANCE.createLayerConnection();
 				SAPoint sapt = null;
 				if (src!=null) {
 					sapt = RoomFactory.eINSTANCE.createRelaySAPoint();
 					((RelaySAPoint)sapt).setRelay(src);
 				}
 				else if (srcRef!=null) {
 					sapt = RoomFactory.eINSTANCE.createRefSAPoint();
 					((RefSAPoint)sapt).setRef(srcRef);
 				}
 				lc.setFrom(sapt);
 				
 				SPPoint sppt = RoomFactory.eINSTANCE.createSPPoint();
 				sppt.setRef(dstRef);
 				sppt.setService(dst);
 				lc.setTo(sppt);
 				
 				sc.getConnections().add(lc);
 				
 				AddConnectionContext addContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
 				addContext.setNewObject(lc);
 				return (Connection) getFeatureProvider().addIfPossible(addContext);
 			}
 		}
 		
 		private class AddFeature extends AbstractAddFeature {
 
 			public AddFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public boolean canAdd(IAddContext context) {
 				if (context instanceof IAddConnectionContext && context.getNewObject() instanceof LayerConnection) {
 					return true;
 				}
 				return false;
 			}
 
 			@Override
 			public PictogramElement add(IAddContext context) {
 				IAddConnectionContext addConContext = (IAddConnectionContext) context;
 				LayerConnection lc = (LayerConnection) context.getNewObject();
 
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 
 				Connection connection = createConnection();
 				connection.setStart(addConContext.getSourceAnchor());
 				connection.setEnd(addConContext.getTargetAnchor());
 				
 				Graphiti.getPeService().setPropertyValue(connection, Constants.TYPE_KEY, Constants.CONN_TYPE);
 
 				IGaService gaService = Graphiti.getGaService();
 				Polyline polyline = gaService.createPolyline(connection);
 				polyline.setForeground(manageColor(LINE_COLOR));
 				polyline.setLineWidth(LINE_WIDTH);
 
 		        ConnectionDecorator cd = peCreateService
 		              .createConnectionDecorator(connection, false, 1.0, true);
 		        createArrow(cd);
 
 				// create link and wire it
 				link(connection, lc);
 
 				return connection;
 			}
 
 			private Connection createConnection() {
 //				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 //				Connection ret = peCreateService.createManhattanConnection(getDiagram());
 				//peCreateService.createFreeFormConnection(getDiagram());
 				ManhattanConnection ret = PictogramsFactory.eINSTANCE.createManhattanConnection();
 				ret.setVisible(true);
 				ret.setActive(true);
 				ret.setParent(getDiagram());
 				return ret;
 			}
 			
 			private Polyline createArrow(GraphicsAlgorithmContainer gaContainer) {
 
 				IGaService gaService = Graphiti.getGaService();
 				Polyline polyline =
 					gaService.createPolyline(gaContainer, new int[] { -15, 10, 0, 0, -15, -10 });
 
 				polyline.setForeground(manageColor(LINE_COLOR));
 				polyline.setLineWidth(LINE_WIDTH);
 
 				return polyline;
 
 			}
 
 		}
 		
 		private class RemoveFeature extends DefaultRemoveFeature {
 
 			public RemoveFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canRemove(IRemoveContext context) {
 				return false;
 			}
 		}
 		
 		private class DeleteFeature extends DeleteWithoutConfirmFeature {
 
 			public DeleteFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canDelete(IDeleteContext context) {
 				EObject bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(context.getPictogramElement());
 				if (bo instanceof LayerConnection) {
 					LayerConnection lc = (LayerConnection) bo;
 					if (isInherited(getDiagram(), lc))
 						return false;
 				}
 				
 				return true;
 			}
 		}
 		
 		private class ReconnectionFeature extends DefaultReconnectionFeature {
 
 			private boolean doneChanges = false;
 
 			public ReconnectionFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canReconnect(IReconnectionContext context) {
 				if (!super.canReconnect(context))
 					return false;
 
 				LayerConnection lc = (LayerConnection) getBusinessObjectForPictogramElement(context.getConnection());
 				if (isInherited(getDiagram(), lc))
 					return false;
 				
 				Anchor asrc = context.getConnection().getStart();
 				Anchor atgt = context.getConnection().getEnd();
 				if (context.getReconnectType().equals(ReconnectionContext.RECONNECT_SOURCE))
 					asrc = context.getNewAnchor();
 				else
 					atgt = context.getNewAnchor();
 
 				IFeatureProvider featureProvider = getFeatureProvider();
 				SPPRef src = SupportUtil.getSPPRef(asrc, featureProvider);
 				ActorContainerRef srcRef = SupportUtil.getRef(asrc, featureProvider);
 				SPPRef tgt = SupportUtil.getSPPRef(atgt, featureProvider);
 				ActorContainerRef tgtRef = SupportUtil.getRef(atgt, featureProvider);
 				StructureClass sc = SupportUtil.getParent(getDiagram(), featureProvider);
 
 				return ValidationUtil.isConnectable(src, srcRef, tgt, tgtRef, sc).isOk();
 			}
 			
 			@Override
 			public void postReconnect(IReconnectionContext context) {
 				super.postReconnect(context);
 
 				IFeatureProvider featureProvider = getFeatureProvider();
 				SPPRef src = SupportUtil.getSPPRef(context.getConnection().getStart(), featureProvider);
 				ActorContainerRef srcRef = SupportUtil.getRef(context.getConnection().getStart(), featureProvider);
 				SPPRef dst = SupportUtil.getSPPRef(context.getConnection().getEnd(), featureProvider);
 				ActorContainerRef dstRef = SupportUtil.getRef(context.getConnection().getEnd(), featureProvider);
 				
 				doneChanges = true;
 				
 				LayerConnection lc = (LayerConnection) getBusinessObjectForPictogramElement(context.getConnection());
 				SAPoint sapt = null;
 				if (src!=null) {
 					sapt = RoomFactory.eINSTANCE.createRelaySAPoint();
 					((RelaySAPoint)sapt).setRelay(src);
 				}
 				else if (srcRef!=null) {
 					sapt = RoomFactory.eINSTANCE.createRefSAPoint();
 					((RefSAPoint)sapt).setRef(srcRef);
 				}
 				lc.setFrom(sapt);
 				
 				SPPoint sppt = RoomFactory.eINSTANCE.createSPPoint();
 				sppt.setRef(dstRef);
 				sppt.setService(dst);
 				lc.setTo(sppt);
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return doneChanges ;
 			}
 		}
 		
 		private class UpdateFeature extends AbstractUpdateFeature {
 
 			public UpdateFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public boolean canUpdate(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy())
 					return true;
 				
 				return false;
 			}
 
 			@Override
 			public IReason updateNeeded(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
 					return Reason.createTrueReason("LayerConnection deleted from model");
 				}
 				return Reason.createFalseReason();
 			}
 
 			@Override
 			public boolean update(IUpdateContext context) {
				ContainerShape containerShape = (ContainerShape)context.getPictogramElement();
				Object bo = getBusinessObjectForPictogramElement(containerShape);
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
					IRemoveContext rc = new RemoveContext(containerShape);
 					IFeatureProvider featureProvider = getFeatureProvider();
 					IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
 					if (removeFeature != null) {
 						removeFeature.remove(rc);
 					}
 					EcoreUtil.delete((EObject) bo);
 					return true;
 				}
 				return false;
 			}
 		}
 		
 		private IFeatureProvider fp;
 		
 		public FeatureProvider(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 			super(dtp);
 			this.fp = fp;
 		}
 
 		private boolean isInherited(Diagram diag, LayerConnection lc) {
 			ActorContainerClass acc = (ActorContainerClass) Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(diag.getChildren().get(0));
 			return (lc.eContainer()!=acc);
 		}
 
 		@Override
 		public ICreateConnectionFeature[] getCreateConnectionFeatures() {
 			return new ICreateConnectionFeature[] { new CreateFeature(fp) };
 		}
 		
 		@Override
 		public IAddFeature getAddFeature(IAddContext context) {
 			return new AddFeature(fp);
 		}
 		
 		@Override
 		public IRemoveFeature getRemoveFeature(IRemoveContext context) {
 			return new RemoveFeature(fp);
 		}
 		
 		@Override
 		public IDeleteFeature getDeleteFeature(IDeleteContext context) {
 			return new DeleteFeature(fp);
 		}
 		
 		@Override
 		public IUpdateFeature getUpdateFeature(IUpdateContext context) {
 			return new UpdateFeature(fp);
 		}
 
 		@Override
 		public IReconnectionFeature getReconnectionFeature(IReconnectionContext context) {
 			return new ReconnectionFeature(fp);
 		}
 	}
 	
 	class BehaviorProvider extends DefaultToolBehaviorProvider {
 
 		public BehaviorProvider(IDiagramTypeProvider dtp) {
 			super(dtp);
 		}
 	}
 	
 	private FeatureProvider pfp;
 	private BehaviorProvider tbp;
 	
 	public LayerConnectionSupport(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 		pfp = new FeatureProvider(dtp,fp);
 		tbp = new BehaviorProvider(dtp);
 	}
 	
 	public IFeatureProvider getFeatureProvider() {
 		return pfp;
 	}
 	
 	public IToolBehaviorProvider getToolBehaviorProvider() {
 		return tbp;
 	}
 }
