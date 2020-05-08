 /*
  * Created on Feb 17, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.j2ee.navigator.internal;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jst.j2ee.internal.J2EEEditModel;
 import org.eclipse.jst.j2ee.internal.project.J2EENature;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModelEvent;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModelListener;
 
 /**
  * @author Admin
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class NonFlexibleEMFModelManager extends EMFModelManager implements EditModelListener{
 
 	private J2EEEditModel editModel;
 	private Object rootObject;
 	private static final Object[] EMPTY_OBJECT = new Object[0];
 	/**
 	 * @param aProject
 	 * @param provider
 	 */
 	public NonFlexibleEMFModelManager(IProject aProject, EMFRootObjectProvider provider) {
 		super(aProject, provider);
 		// TODO Auto-generated constructor stub
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.navigator.internal.EMFModelManager#getModels()
 	 */
 	public Object[] getModels() {
 		// TODO Auto-generated method stub
 		if (getProject() == null || !getProject().isAccessible())
 			return EMPTY_OBJECT;
 		
 		//synchronized (rootObject) {
 			if (rootObject == null || ((EObject) rootObject).eResource() == null) {
 				J2EEEditModel editModel = getEditModel();
 				if (editModel != null) {
 					rootObject = editModel.getPrimaryRootObject();
 				}
 			}
 		//}
 		if (rootObject==null) 
 			return EMPTY_OBJECT;
 		else {
 			Object[] objects = new Object[1];
 			objects[0] = rootObject;
 			return objects;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.navigator.internal.EMFModelManager#dispose()
 	 */
 	public void dispose() {
 		if (editModel != null) {
 			editModel.removeListener(this);
 			editModel.releaseAccess(this);
			editModel = null;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.common.internal.emfworkbench.integration.EditModelListener#editModelChanged(org.eclipse.wst.common.internal.emfworkbench.integration.EditModelEvent)
 	 */
 	public void editModelChanged(EditModelEvent anEvent) {
 		IProject affectedProject = anEvent.getEditModel().getProject();
 		switch (anEvent.getEventCode()) {
 			case EditModelEvent.UNLOADED_RESOURCE :
 			case EditModelEvent.REMOVED_RESOURCE : 
 				if (rootObject != null){
 					notifyListeners(affectedProject);
 			     }
 				break;
 			case EditModelEvent.SAVE : {
 				if (rootObject == null)
 					notifyListeners(affectedProject);
 				}
 				break;
 			case EditModelEvent.PRE_DISPOSE :
 					dispose();
 				break;
 			default :
 				break;
 		}
 		
 	}
 	
 	
 
 	protected J2EEEditModel getEditModel() {
 		IProject project = getProject();
 		if (project == null)
 			return null;
 		
 		//synchronized (editModel) {
 			if (editModel == null && project.isAccessible()) {
 				//System.out.println("getEditModelForProject " + project.getName());
 				J2EENature nature = J2EENature.getRegisteredRuntime(project);
 				if (nature != null) {
 					editModel = nature.getJ2EEEditModelForRead(this);
 					if (editModel != null) {
 						editModel.addListener(this);
 					}
 				}
 			}
 		//}
 		return editModel;
 	}
 	
 	protected void disposeCache(IProject affectedProject) {
 		//synchronized (editModel) {
 			if (editModel != null) {
 				editModel.removeListener(this);
 				editModel.releaseAccess(this);
				editModel = null;
 			}
 		//}
 	}
 
 
 }
