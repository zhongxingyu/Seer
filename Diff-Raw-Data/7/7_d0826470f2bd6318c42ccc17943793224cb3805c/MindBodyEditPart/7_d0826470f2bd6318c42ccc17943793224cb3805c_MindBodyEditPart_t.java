 package org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.parts.generic;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.gef.DragTracker;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.AbstractBorderedShapeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.eclipse.swt.graphics.Color;
 
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.policies.MindSubCreationEditPolicy;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.policies.NoDragDropEditPolicy;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.figures.AbstractComponentShape;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.figures.IFractalShape;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.layouts.InterfaceBorderItemLocator;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.providers.NoDragTracker;
 import org.ow2.mindEd.adl.editor.graphic.ui.edit.parts.CompositeBodyCompartmentEditPart;
 import org.ow2.mindEd.adl.editor.graphic.ui.edit.parts.InterfaceDefinitionEditPart;
 
 public class MindBodyEditPart extends MindEditPart {
 	
 	protected MindComponentEditPart parentComponent;
 
 	public MindBodyEditPart(GraphicalEditPart editPart, int vID) {
 		super(editPart, vID, TYPE_BODY);
 		setParentComponent();
 	}
 	
 	public MindBodyEditPart(GraphicalEditPart editPart, int vID, int mindType) {
 		super(editPart, vID, mindType);
 		setParentComponent();
 	}
 	
 	protected void setParentComponent() {
 		MindEditPart parent = getMindEditPartFor(realEditPart.getParent());
 		if (parent instanceof MindComponentEditPart)
 			parentComponent = (MindComponentEditPart) parent;
 	}
 
 	@Override
 	public void createDefaultEditPolicies() {
 		super.createDefaultEditPolicies();
 		// Extended creation features
 		realEditPart.installEditPolicy(
 				EditPolicyRoles.CREATION_ROLE,
 				new MindSubCreationEditPolicy());
 		setCreationMode(CREATION_MODE_PARENT);
 		realEditPart.removeEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE);
 		realEditPart.installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
 				new NoDragDropEditPolicy());
 	}
 	
	@Override
	public DragTracker getDragTracker(Request request) {
		return new NoDragTracker(realEditPart);
 	}
 	
 	@Override
 	public boolean addFixedChild(EditPart childEditPart) {
 		if (getMindEditPartFor(childEditPart) instanceof MindCompartmentEditPart) {
 			IFigure compartment = getCompartmentFigure();
 			if (compartment == null) return false;
 			// Set the layout
 			setupCompartment(compartment);
 			compartment.add(((GraphicalEditPart) childEditPart)
 							.getFigure());
 			return true;
 		}
 		if(getMindEditPartFor(childEditPart) instanceof MindInterfaceEditPart)
 		{
 			//Make interfaces stick to the component's border
 			//Use InterfaceBorderItemLocator
 			if (getParentComponent() == null) return false;
 			AbstractBorderedShapeEditPart parentBorderedEditPart = parentComponent.borderedEditPart;
 			if (parentBorderedEditPart == null) return false;
 			InterfaceBorderItemLocator locator = new InterfaceBorderItemLocator(
 					parentBorderedEditPart.getMainFigure()); 
 			parentBorderedEditPart.getBorderedFigure().getBorderItemContainer().add(
 					((InterfaceDefinitionEditPart) childEditPart).getFigure(), locator);
 			
 			return true;
 		}
 		return false;
 	}
 	
 	
 	public boolean removeFixedChild(EditPart childEditPart) {
 		if (childEditPart instanceof InterfaceDefinitionEditPart) {
 			if (getParentComponent() == null) return false;
 			AbstractBorderedShapeEditPart parentBorderedEditPart = parentComponent.borderedEditPart;
 			if (parentBorderedEditPart == null) return false;
 			parentBorderedEditPart.getBorderedFigure().getBorderItemContainer().remove(
 					((InterfaceDefinitionEditPart) childEditPart).getFigure());
 			return true;
 		}
 		return false;
 	}
 	
 	
 	public boolean setLayoutConstraint(EditPart child, IFigure childFigure,
 			Object constraint) {
 		parentComponent.setLayoutConstraint(child, childFigure, constraint);
 		return true;
 	}
 	
 	
 	public IFigure getCompartmentFigure() {
 		try {
 			
 			Method meth = realEditPart.getClass().getMethod("getPrimaryShape", null);
 			AbstractComponentShape result = (AbstractComponentShape) meth.invoke(realEditPart, null);
 			return result.getCompartment();
 			
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (ClassCastException e) {
 			e.printStackTrace();
 		} 
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Call this for compartment edit parts instead of generated setupContentPane, to keep
 	 * the default layout but without the annoying 5 pixels spacing
 	 * @param body
 	 * @return
 	 */
 	public IFigure setupCompartment(IFigure compartment) {
 		if (compartment.getLayoutManager() == null) {
 			ConstrainedToolbarLayout layout = new ConstrainedToolbarLayout() ;
 			compartment.setLayoutManager(layout);
 		}
 		return compartment; // use compartment itself as contentPane
 	}
 	
 	public MindComponentEditPart getParentComponent() {
 		if (parentComponent == null) {
 			MindEditPart parent = getMindEditPartFor(realEditPart.getParent());
 			if (parent instanceof MindComponentEditPart)
 				parentComponent = (MindComponentEditPart) parent;
 		}
 		return parentComponent;
 	}
 
 	public Color getMindBorderColor() {
 		if (getParentComponent() == null)
 			return IFractalShape.GRAY;
 		switch(getParentComponent().getComponentType()) {
 		case COMPONENT_COMPOSITE:
 			return IFractalShape.BLUE;
 		case COMPONENT_SUB_COMPOSITE:
 			return IFractalShape.PURPLE;
 		case COMPONENT_PRIMITIVE:
 			return IFractalShape.RED;
 		case COMPONENT_SUB_PRIMITIVE:
 			return IFractalShape.ORANGE;
 		case COMPONENT_TYPE:
 			return IFractalShape.GRAY;
 		}
 		return IFractalShape.GRAY;
 	}
 	
 	public Color getMindBackgroundColor() {
 		if (getParentComponent() == null)
 			return IFractalShape.GRAY;
 		switch(getParentComponent().getComponentType()) {
 		case COMPONENT_COMPOSITE:
 			return IFractalShape.LIGHT_BLUE;
 		case COMPONENT_SUB_COMPOSITE:
 			return IFractalShape.LIGHT_PURPLE;
 		case COMPONENT_PRIMITIVE:
 			return IFractalShape.LIGHT_RED;
 		case COMPONENT_SUB_PRIMITIVE:
 			return IFractalShape.LIGHT_ORANGE;
 		case COMPONENT_TYPE:
 			return IFractalShape.LIGHT_GRAY;
 		}
 		return IFractalShape.LIGHT_GRAY;
 	}
 	
 }
