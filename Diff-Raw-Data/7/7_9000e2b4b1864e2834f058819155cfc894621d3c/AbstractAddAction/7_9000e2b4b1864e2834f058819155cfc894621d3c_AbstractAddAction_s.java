 package org.eclipse.pde.emfforms.editor.actions;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.pde.emfforms.editor.EmfMasterDetailBlock;
 import org.eclipse.pde.emfforms.internal.Activator;
 import org.eclipse.pde.emfforms.internal.editor.IEmfFormsImages;
 
 public abstract class AbstractAddAction extends Action {
 
 	protected EmfMasterDetailBlock masterDetail;
 
 	public AbstractAddAction(EmfMasterDetailBlock masterDetail) {
 		super();
 		this.masterDetail = masterDetail;
 		setText("Add");
 		setToolTipText("Add a element to current model");
 	}
 
 	@Override
 	public void run() {
 
 		Object createdObject = createNewObject(masterDetail);
 
 		// select the created method
 		masterDetail.getTreeViewer().refresh();
 		masterDetail.getTreeViewer().setSelection(new StructuredSelection(createdObject), true);
 		masterDetail.getTreeViewer().getTree().setFocus();
 
 	}
 
 	/**
 	 * Create and return the new object to add to the masterDetail. The new object will be selected in the tree.
 	 * 
 	 * @param masterDetail where the new object will be add
	 * @return the created object
 	 */
 	protected abstract Object createNewObject(EmfMasterDetailBlock masterDetail);
 
 	@Override
 	public ImageDescriptor getImageDescriptor() {
 		return ImageDescriptor.createFromURL(Activator.getDefault().getBundle().getResource(IEmfFormsImages.ADD_TOOLBAR_BUTTON));
 	}
 
 }
