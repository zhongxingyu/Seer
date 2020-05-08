 package org.ow2.mindEd.adl.editor.graphic.ui.custom.tools;
 
 import java.util.List;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.impl.TransactionImpl;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.requests.CreateConnectionRequest;
 import org.eclipse.gef.requests.TargetRequest;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.tools.UnspecifiedTypeConnectionTool;
 import org.eclipse.gmf.runtime.notation.View;
 import org.ow2.mindEd.adl.custom.util.CreationUtil;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.parts.generic.MindBodyCompartmentEditPart;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.parts.generic.MindBodyEditPart;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.parts.generic.MindGenericEditPartFactory;
 
 import org.ow2.mindEd.adl.ArchitectureDefinition;
 import org.ow2.mindEd.adl.Body;
 import org.ow2.mindEd.adl.InterfaceDefinition;
 import org.ow2.mindEd.adl.Role;
 import org.ow2.mindEd.adl.editor.graphic.ui.edit.parts.InterfaceDefinitionEditPart;
 import org.ow2.mindEd.adl.editor.graphic.ui.part.MindDiagramEditorPlugin;
 
 @SuppressWarnings("rawtypes")
 public class BindingCreationTool extends UnspecifiedTypeConnectionTool {
 
 
 	protected void updateTargetRequest()
     {
 		CreateConnectionRequest request = (CreateConnectionRequest)getTargetRequest();
 		request.setType(getCommandName());
 		request.setLocation(getLocation());
 		handleGeneratedItfsRoles(request);
     }
 
 	
 
 	protected List bindingConnectionTypes;
 	protected EditPart customTargetEditPart;
 	
 	protected InterfaceDefinition generatedSource = null;
 	protected InterfaceDefinition generatedTarget = null;
 
 	public BindingCreationTool(List connectionTypes) {
 		super(connectionTypes);
 		bindingConnectionTypes = connectionTypes;
 	}
 	
 	protected TransactionalEditingDomain domain = null;
 	
 	protected TransactionalEditingDomain getEditingDomain() {
 		if ((domain == null) && (customTargetEditPart instanceof GraphicalEditPart)) {
 			domain = ((GraphicalEditPart)customTargetEditPart).getEditingDomain();
 		}
 		return domain;
 	}
 	
 	
 	protected void setTargetEditPart(EditPart editpart)
 	{
 	    if (editpart != this.customTargetEditPart) {
 		    if (this.customTargetEditPart != null)
 		    	 handleExitingEditPart();
 		    
 		    EditPart genEditPart = generateItfs(editpart);
 		    if (genEditPart != null)
 		    	editpart = genEditPart;
 		    	
 		    this.customTargetEditPart = editpart;
 		    
 		    if (getTargetRequest() instanceof TargetRequest) {
 		    	 ((TargetRequest)getTargetRequest()).setTargetEditPart(this.customTargetEditPart);
 		    }
 		    handleEnteredEditPart();
 	    }
 	}
 	
 	/**
 	 * Tries to set roles of generated interfaces in order to have a valid request
 	 * @param request
 	 */
 	protected void handleGeneratedItfsRoles(CreateConnectionRequest request) {
 
 		EditPart sourceEditPart = request.getSourceEditPart();
 		EditPart targetEditPart = request.getTargetEditPart();
 		// If souce or target is not set, we don't know which role to assign
 		// Do nothing
 		if (targetEditPart == null || sourceEditPart == null) 
 			return;
 		if (!(sourceEditPart instanceof InterfaceDefinitionEditPart) || !(targetEditPart instanceof InterfaceDefinitionEditPart))
 			return;
 		
 		InterfaceDefinition source = null;
 		InterfaceDefinition target = null;
 		
 		ArchitectureDefinition sourceParent = null;
 		ArchitectureDefinition targetParent = null;
 		
 		boolean sourceIsGenerated = false;
 		boolean targetIsGenerated = false;
 		if (generatedSource != null && generatedTarget != null){
 			// Two generated interfaces
 			source = generatedSource;
 			target = generatedTarget;
 			sourceIsGenerated = true;
 			targetIsGenerated = true;
 		}
 		else if (generatedSource != null && generatedTarget == null) {
 			// Only source is generated, target is existing itf
 			source = generatedSource;
 			sourceIsGenerated = true;
 			target = (InterfaceDefinition)((View)(targetEditPart.getModel())).getElement();
 		}
 		else if (generatedSource == null && generatedTarget != null) {
 			// Only target is generated, source is existing itf
 			source = (InterfaceDefinition)((View)(sourceEditPart.getModel())).getElement();
 			target = generatedTarget;
 			targetIsGenerated = true;
 		}
 		else {
 			// None of the interfaces are generated, do nothing
 			return;
 		}
 		
 		// Here either source or target, or both are generated
 		
 		sourceParent = source.getParentBody().getParentComponent();
 		targetParent = target.getParentBody().getParentComponent();
 		
 		if (sourceParent == targetParent) {
 			// Two interfaces of the same component
 			// It is not allowed whatever the roles so do nothing
 			return;
 		}
 		if (sourceParent.eContainer() == targetParent.eContainer()) {
 			// Two subcomponents of the same composite
 			// REQUIRES to PROVIDES
 			if (source.getRole() == target.getRole()) {
 				if (targetIsGenerated) {
 					// target is generated, set its role depending on source
 					if (source.getRole() == Role.REQUIRES)
 						setRole(target,Role.PROVIDES);
 					else
 						setRole(target,Role.REQUIRES);
 				}
 				else {
 					// source is generated and target is not
 					// set its role depending on target
 					if (target.getRole() == Role.REQUIRES)
 						setRole(source,Role.PROVIDES);
 					else
 						setRole(source,Role.REQUIRES);
 				}
 			}
 		}
 		if (sourceParent.eContainer().eContainer() == targetParent
 				|| sourceParent == targetParent.eContainer().eContainer()) {
 			// Two components on different levels
 			// Roles must be PROVIDES
 			if (sourceIsGenerated && targetIsGenerated) {
 				// target is generated, set its role depending on source
 				setRole(source,Role.PROVIDES);
 				setRole(target,Role.PROVIDES);
 			}
 			else if (sourceIsGenerated) {
 				// source is generated and target is not
 				// set its role depending on target
 				setRole(source,Role.PROVIDES);
 			}
 			else {
 				setRole(target,Role.PROVIDES);
 			}
 		}
 	}
 	
 	protected EditPart generateItfs(EditPart editpart) {
 
 		if ((MindGenericEditPartFactory.INSTANCE.getMindEditPartFor(editpart)
 				instanceof MindBodyCompartmentEditPart)) {
 			// Delegate to body
 			editpart = editpart.getParent();
 		}
 		// Creation
 		if (isDragging()) {
 			// Button is down, generate target if there is none
 			if ((generatedTarget == null) &&
 					(MindGenericEditPartFactory.INSTANCE.getMindEditPartFor(editpart)
 					instanceof MindBodyEditPart)) {
 				// Create an interface to delegate the binding creation
 				editpart = createTarget(editpart);
 			}
 		} else {
 			// Button is up, generate source if there is none
 			if ((generatedSource == null) &&
 					(MindGenericEditPartFactory.INSTANCE.getMindEditPartFor(editpart)
 					instanceof MindBodyEditPart)) {
 				// Create an interface to delegate the binding creation
 				editpart = createSource(editpart);
 			}
 		}
 		
 		return editpart;
 	}
 	
 	/**
 	 * create an InterfaceDefinitionEditPart on the given BodyEditPart,
 	 * set it to be the source of the connection and returns it
 	 * @param bodyEditPart
 	 * @return
 	 */
 	protected InterfaceDefinitionEditPart createSource(EditPart bodyEditPart){
 				
 		if (!(MindGenericEditPartFactory.INSTANCE.getMindEditPartFor(bodyEditPart)
 				instanceof MindBodyEditPart))
 			return null;
 		
 		InterfaceDefinitionEditPart newPart = null;
 		Body body = (Body)((View)bodyEditPart.getModel()).getElement();
 		InterfaceDefinition newSource = createInterfaceDefinition(body,"generatedItf");
 
 		bodyEditPart.refresh();
 		
 		// Find the newly created edit part
 		GraphicalEditPart part = (GraphicalEditPart)bodyEditPart;
 		if (newSource != null) {
 			generatedSource = newSource;
 			newPart = (InterfaceDefinitionEditPart) part.findEditPart(part.getParent(), newSource);
 		}
 		return newPart;
 	}
 	
 	
 	/**
 	 * create an InterfaceDefinitionEditPart on the given BodyEditPart,
 	 * set it to be the target of the connection and returns it
 	 * @param bodyEditPart
 	 * @return
 	 */
 	protected InterfaceDefinitionEditPart createTarget(EditPart bodyEditPart){
 				
 		if (!(MindGenericEditPartFactory.INSTANCE.getMindEditPartFor(bodyEditPart)
 				instanceof MindBodyEditPart))
 			return null;
 		
 		InterfaceDefinitionEditPart newPart = null;
 		Body body = (Body)((View)bodyEditPart.getModel()).getElement();
 		InterfaceDefinition newTarget = createInterfaceDefinition(body,"generatedItf");
 
 		// Activate semantic refresh to let the canonical edit policy create the view and edit part
 		bodyEditPart.refresh();
 		
 		// Find the newly created edit part
 		GraphicalEditPart part = (GraphicalEditPart)bodyEditPart;
 		if (newTarget != null) {
 			generatedTarget = newTarget;
 			newPart = (InterfaceDefinitionEditPart) part.findEditPart(part.getParent(), newTarget);
 		}
 		return newPart;
 	}
 	
 	
 	/**
 	 * create and InterfaceDefinition in the given Body and returns it
 	 * @param body
 	 * @return
 	 */
 	protected InterfaceDefinition createInterfaceDefinition(Body body, String name) {
 		
		// Do not generate an interface in merged body
 		if (body.isMerged()) {
 			return null;
 		}
 		
		// If editor is in an instable state
		if (getEditingDomain() == null)
			return null;
		
 		InterfaceDefinition newInterface = null;
 		try {
 			TransactionImpl transaction = new TransactionImpl(getEditingDomain(), false);
 			transaction.start();
 			newInterface = CreationUtil.createInterface(body,null,name);
 			transaction.commit();
 		}
 		catch(Exception e) {
 			MindDiagramEditorPlugin.getInstance().logError("Error generating an interface to bind", e);
 		}
 			
 		return newInterface;
 	}
 	
 	
 	protected void setRole(InterfaceDefinition inter, Role newRole) {
 		try {
 			TransactionImpl transaction = new TransactionImpl(getEditingDomain(), false);
 			transaction.start();
 			inter.setRole(newRole);
 			transaction.commit();
 		}
 		catch(Exception e) {
 			MindDiagramEditorPlugin.getInstance().logError("Error modifying generated interface's role", e);
 		}
 	}
 	
 	
 	/**
 	 * delete the generated source
 	 */
 	protected void deleteSource() {
 		deleteInterface(generatedSource);
 		generatedSource = null;
 	}
 	
 	
 	/**
 	 * delete the generated target
 	 */
 	protected void deleteTarget() {
 		deleteInterface(generatedTarget);
 		generatedTarget = null;
 	}
 	
 	
 	/**
 	 * Delete the given itf from model
 	 * @param itf the interface to be deleted
 	 */
 	protected void deleteInterface(InterfaceDefinition itf) {
 		if (itf != null) {
 			try {
 				TransactionImpl transaction = new TransactionImpl(getEditingDomain(), false);
 				transaction.start();
 				itf.getParentBody().getElements().remove(itf);
 				transaction.commit();
 	    	}
 	    	catch(Exception e) {
 	    		MindDiagramEditorPlugin.getInstance().logError("Error deleting generated interface", e);
 	    	}
 		}
 	}
 	
 	protected boolean handleExitingEditPart()
 	{
 		resetHover();
 		eraseTargetFeedback();
 		handleDeleteGeneratedItfs();
 		return true;
 	}
 	
 	protected void handleDeleteGeneratedItfs() {
 		if (isDragging()) {
 			// Button is down, delete target but not source
 			deleteTarget();
 		} else {
 			// Button is up, delete source
 			deleteSource();
 		}
 	}
 
 
 	@Override
 	protected EditPart getTargetEditPart() {
 		return customTargetEditPart;
 	}
 	
 	
 	@Override
 	protected void setConnectionSource(EditPart source) {
 		if ((generatedSource != null) && (source instanceof InterfaceDefinitionEditPart)) {
 			getCurrentViewer().deselectAll();
 			getCurrentViewer().select(source);
 			getCurrentViewer().setFocus(source);
 			
 			IFigure fig = ((InterfaceDefinitionEditPart)source).getFigure();
 			Rectangle interfaceBounds = fig.getBounds().getCopy();
 			fig.translateToAbsolute(interfaceBounds);
 			
 			getCurrentInput().setMouseLocation(interfaceBounds.x,interfaceBounds.y);
 		}
 		
 		super.setConnectionSource(source);
 	}
 	
 	
 	protected boolean isDragging() {
 		return (!(isInState(STATE_INITIAL) || isInState(STATE_TERMINAL)));
 	}
 	
 }
