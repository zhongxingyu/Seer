 /*******************************************************************************
  * Copyright (c) 2009 Tobias Jaehnel.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Tobias Jaehnel - Bug#241385
  *******************************************************************************/
 
 package org.eclipse.emf.compare.ui.gmf.mergeviewer;
 
 import java.io.IOException;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.IStreamContentAccessor;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.ResourceNode;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.ui.gmf.viewmodel.ViewModelCreator;
 import org.eclipse.emf.compare.util.ModelUtils;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
 import org.eclipse.gef.editparts.ZoomManager;
 import org.eclipse.gmf.runtime.diagram.core.DiagramEditingDomainFactory;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramRootEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramGraphicalViewer;
 import org.eclipse.gmf.runtime.diagram.ui.services.editpart.EditPartService;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 
 /**
  * 
  * @author Tobias Jaehnel <tjaehnel@gmail.com>
  */
 public class ContentMergeViewer extends org.eclipse.compare.contentmergeviewer.ContentMergeViewer {
 
 	Diagram diag_left;
 
 	Diagram diag_right;
 
 	DiagramGraphicalViewer viewer_left;
 
 	DiagramGraphicalViewer viewer_right;
 
 	DiagramGraphicalViewer viewer_anc = null;
 
 	protected ContentMergeViewer(Composite parent, CompareConfiguration cc) {
 		super(SWT.NONE, null, cc);
 		buildControl(parent);
 	}
 
 	@Override
 	protected void copy(boolean leftToRight) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void createControls(Composite composite) {
 		viewer_left = new DiagramGraphicalViewer();
 		viewer_left.createControl(composite);
 		viewer_left.getControl().setBackground(ColorConstants.listBackground);
 		viewer_right = new DiagramGraphicalViewer();
 		viewer_right.createControl(composite);
 		viewer_right.getControl().setBackground(ColorConstants.listBackground);
 	}
 
 	@Override
 	protected byte[] getContents(boolean left) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected void handleResizeAncestor(int x, int y, int width, int height) {
 		if (viewer_anc != null) {
 			if (width > 0) {
 				viewer_anc.getControl().setVisible(true);
 				viewer_anc.getControl().setBounds(x, y, width, height);
 			} else {
 				viewer_anc.getControl().setVisible(false);
 			}
 		}
 	}
 
 	@Override
 	protected void handleResizeLeftRight(int x, int y, int leftWidth, int centerWidth, int rightWidth,
 			int height) {
 		viewer_left.getControl().setBounds(x, y, leftWidth, height);
 		viewer_right.getControl().setBounds(x + leftWidth + centerWidth, y, rightWidth, height);
 		if (viewer_left.getRootEditPart() instanceof ScalableFreeformRootEditPart) {
 			ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart)viewer_left
 					.getRootEditPart();
 			rootEditPart.getZoomManager().setZoomAnimationStyle(ZoomManager.ANIMATE_NEVER);
 			rootEditPart.getZoomManager().setZoom(0.7);
 		}
 		if (viewer_right.getRootEditPart() instanceof ScalableFreeformRootEditPart) {
 			ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart)viewer_right
 					.getRootEditPart();
 			rootEditPart.getZoomManager().setZoomAnimationStyle(ZoomManager.ANIMATE_NEVER);
 			rootEditPart.getZoomManager().setZoom(0.7);
 		}
 	}
 
 	@Override
 	protected void updateContent(Object ancestor, Object left, Object right) {
 		diag_left = null;
 		diag_right = null;
 		Resource left_resource = null;
 		Resource right_resource = null;
 		ResourceSetImpl resourceset = new ResourceSetImpl();
 		
 		String right_name;
 		String left_name;
 		// resolve the absolute url if the left or right file is local
 		// this is needed to resolve and load the semantic model elements
 		if(left instanceof ResourceNode)
 			left_name =
 				URI.createPlatformResourceURI(((ResourceNode)left).getResource().getFullPath().toString(),true).toString();
 		else
 			left_name = ((ITypedElement)left).getName();
 		if(right instanceof ResourceNode)
 			right_name =
 				URI.createPlatformResourceURI(((ResourceNode)right).getResource().getFullPath().toString(),true).toString();
 		else
 			right_name = ((ITypedElement)right).getName();
 
 		// load the models
 		try {
 			left_resource = ModelUtils.load(((IStreamContentAccessor)left).getContents(),
 					left_name,resourceset).eResource();
 			right_resource = ModelUtils.load(((IStreamContentAccessor)right).getContents(),
 					right_name,resourceset).eResource();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (CoreException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		for (Object rootElement : left_resource.getContents())
 			if (rootElement instanceof Diagram)
 				diag_left = (Diagram)rootElement;
 		for (Object rootElement : right_resource.getContents())
 			if (rootElement instanceof Diagram)
 				diag_right = (Diagram)rootElement;
 
 		// make annotations and show them
 		ViewModelCreator modelCreator = ViewModelCreator.getCreator(getCompareConfiguration(), diag_left, diag_right);
 		diag_left = modelCreator.getLeft();
 		diag_right = modelCreator.getRight();
 		
 		DiagramEditingDomainFactory.getInstance().createEditingDomain(resourceset);
 		viewer_left.setEditDomain(new DefaultEditDomain(null));
 		viewer_left.setEditPartFactory(EditPartService.getInstance());
 		viewer_left.setRootEditPart(new DiagramRootEditPart(diag_left.getMeasurementUnit()));
 		viewer_left.setContents(diag_left);
 
 		viewer_right.setEditDomain(new DefaultEditDomain(null));
 		viewer_right.setEditPartFactory(EditPartService.getInstance());
 		viewer_right.setRootEditPart(new DiagramRootEditPart(diag_right.getMeasurementUnit()));
 		viewer_right.setContents(diag_right);
 	}
 
 }
