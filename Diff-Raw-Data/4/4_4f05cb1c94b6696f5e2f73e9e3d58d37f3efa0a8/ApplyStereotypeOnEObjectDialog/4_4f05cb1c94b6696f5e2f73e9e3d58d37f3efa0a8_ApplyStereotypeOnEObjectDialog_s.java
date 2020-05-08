 /**
  * Copyright (c) 2012 modelversioning.org
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  */
 package org.modelversioning.emfprofile.application.registry.ui.dialogs;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.dialogs.ISelectionStatusValidator;
 import org.modelversioning.emfprofile.application.registry.ProfileApplicationDecorator;
 import org.modelversioning.emfprofile.application.registry.ui.EMFProfileApplicationRegistryUIPlugin;
 import org.modelversioning.emfprofile.application.registry.ui.observer.ActiveEditorObserver;
 import org.modelversioning.emfprofile.application.registry.ui.providers.ProfileProviderLabelAdapter;
 import org.modelversioning.emfprofile.application.registry.ui.views.EMFProfileApplicationsView;
 import org.modelversioning.emfprofileapplication.StereotypeApplicability;
 
 /**
  * @author <a href="mailto:becirb@gmail.com">Becir Basic</a>
  *
  */
 public class ApplyStereotypeOnEObjectDialog {
 
 	private ProfileProviderLabelAdapter labelAdapter = new ProfileProviderLabelAdapter(EMFProfileApplicationsView.getAdapterFactory());
 	
 	private final Map<ProfileApplicationDecorator, Collection<StereotypeApplicability>> profileToStereotypeApplicabilityForEObjectMap;
 	public ApplyStereotypeOnEObjectDialog(Map<ProfileApplicationDecorator, Collection<StereotypeApplicability>> profileToStereotypeApplicabilityForEObjectMap) {
 		this.profileToStereotypeApplicabilityForEObjectMap = profileToStereotypeApplicabilityForEObjectMap;
 	}
 	
 	/**
 	 * Opens this dialog, in which the stereotypes that can be applied
 	 * on the given {@link EObject} can be selected.
 	 * @param eObject in question.
 	 */
 	public void openApplyStereotypeDialog(EObject eObject) {
 		Collection<TreeParent> parents = new ArrayList<>();
 		for(ProfileApplicationDecorator profileApplication : profileToStereotypeApplicabilityForEObjectMap.keySet()){
 			TreeParent parent = new TreeParent(profileApplication);
 			for(StereotypeApplicability stereotypeApplicability : profileToStereotypeApplicabilityForEObjectMap.get(profileApplication)){
 				parent.addChild(new TreeObject(stereotypeApplicability));
 			}
 			parents.add(parent);
 		}
 		
 		StereotypeTreeSelectionDialog dialog = new StereotypeTreeSelectionDialog(
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
 				new ViewLabelProvider(), new ViewContentProvider());
 		dialog.setTitle("Stereotype Selection");
 		dialog.setMessage("Select one or more Stereotypes to apply");
 		
 		dialog.setInput(parents);
 		dialog.setDoubleClickSelects(true);
 		dialog.setValidator(new ISelectionStatusValidator() {
 			
 			@Override
 			public IStatus validate(Object[] selection) {
 				if(selection.length==0)
 					return new Status(IStatus.ERROR, EMFProfileApplicationRegistryUIPlugin.PLUGIN_ID, "No Stereotype selected yet.");
 				for (Object object : selection) {
 					if(! (object instanceof TreeParent))
 						return new Status(IStatus.OK, EMFProfileApplicationRegistryUIPlugin.PLUGIN_ID, "");
 				}
 				return new Status(IStatus.ERROR, EMFProfileApplicationRegistryUIPlugin.PLUGIN_ID, "No Stereotype selected yet.");
 			}
 		});
 		int result = dialog.open();
 		if (Dialog.OK == result) {
 			Object[] treeObjects = dialog.getResult();
 			StringBuilder strBuilder = new StringBuilder();
 			boolean hasNotApplicableStereotypes = false;
 			Collection<ProfileApplicationDecorator> profileApplicationDecoratorToBeRefreshedInView = new ArrayList<>();
 			for (Object object : treeObjects) {
 				if(!(object instanceof TreeParent)){
 					TreeObject child = (TreeObject) object;
 					StereotypeApplicability stereotypeApplicability = ((StereotypeApplicability)child.getElement());
 					ProfileApplicationDecorator profileApplicationDecorator = (ProfileApplicationDecorator)child.getParent().getElement();
 					try {
 						profileApplicationDecorator.applyStereotype(stereotypeApplicability, eObject);
 						profileApplicationDecoratorToBeRefreshedInView.add(profileApplicationDecorator);
 					} catch (IllegalArgumentException e) {
 						hasNotApplicableStereotypes = true;
 						strBuilder.append(stereotypeApplicability.getStereotype().getName() + ", from profile: " + profileApplicationDecorator.getProfileName() + "\n");
 					}
 				}
 			}
 			if( ! profileApplicationDecoratorToBeRefreshedInView.isEmpty()){
 				ActiveEditorObserver.INSTANCE.refreshViewer(profileApplicationDecoratorToBeRefreshedInView);
 				ActiveEditorObserver.INSTANCE.refreshDecoration(eObject);
 			}
 			if(hasNotApplicableStereotypes){
				strBuilder.insert(0, "Not applicable stereotype(s)  to object: "+ (((ENamedElement)eObject == null) ? "" : ((ENamedElement)eObject).getName())+"\n");
 				MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
 				messageBox.setText("Could not be applied!");
 				messageBox.setMessage(strBuilder.toString());
 				messageBox.open();
 			}
 		}
 		
 	}
 	class TreeObject implements IAdaptable {
 		private TreeParent parent;
 		private Object element;
 		
 		public final Object getElement() {
 			return element;
 		}
 		
 		public TreeObject(Object element){
 			this.element = element;
 		}
 		public void setParent(TreeParent parent) {
 			this.parent = parent;
 		}
 		public TreeParent getParent() {
 			return parent;
 		}
 		
 		public Object getAdapter(Class key) {
 			return null;
 		}
 	}
 	
 	class TreeParent extends TreeObject {
 		private ArrayList children;
 		
 		public TreeParent(String name) {
 			super(name);
 			children = new ArrayList();
 		}
 		public TreeParent(Object element) {
 			super(element);
 			children = new ArrayList();
 		}
 		public void addChild(TreeObject child) {
 			children.add(child);
 			child.setParent(this);
 		}
 		public void removeChild(TreeObject child) {
 			children.remove(child);
 			child.setParent(null);
 		}
 		public TreeObject [] getChildren() {
 			return (TreeObject [])children.toArray(new TreeObject[children.size()]);
 		}
 		public boolean hasChildren() {
 			return children.size()>0;
 		}
 	}
 
 	class ViewContentProvider implements IStructuredContentProvider, 
 										   ITreeContentProvider {
 		private TreeParent invisibleRoot;
 
 		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
 		}
 		public void dispose() {
 		}
 		public Object[] getElements(Object parent) {
 			if (parent instanceof Collection<?>) {
 				return ((Collection<?>) parent).toArray();
 			}
 			return getChildren(parent);
 		}
 		public Object getParent(Object child) {
 			if (child instanceof TreeObject) {
 				return ((TreeObject)child).getParent();
 			}
 			return null;
 		}
 		public Object [] getChildren(Object parent) {
 			if (parent instanceof TreeParent) {
 				return ((TreeParent)parent).getChildren();
 			}
 			return new Object[0];
 		}
 		public boolean hasChildren(Object parent) {
 			if (parent instanceof TreeParent)
 				return ((TreeParent)parent).hasChildren();
 			return false;
 		}
 	}
 	
 	final class ViewLabelProvider extends LabelProvider {
 
 		public String getText(Object obj) {
 			if(((TreeObject)obj).getElement() instanceof ProfileApplicationDecorator)
 				return ((ProfileApplicationDecorator)((TreeObject)obj).getElement()).getName();
 			return labelAdapter.getText(((TreeObject)obj).getElement());
 		}
 		public Image getImage(Object obj) {
 			return labelAdapter.getImage(((TreeObject)obj).getElement());
 //			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
 		}
 	}
 
 	final class StereotypeTreeSelectionDialog extends ElementTreeSelectionDialog{
 
 		public StereotypeTreeSelectionDialog(Shell parent,
 				ILabelProvider labelProvider,
 				ITreeContentProvider contentProvider) {
 			super(parent, labelProvider, contentProvider);
 		}
 		
 		@Override
 		protected Control createDialogArea(Composite parent) {
 			Control control = super.createDialogArea(parent);
 			// setting auto expand was the reason why a sub-type was needed :)
 			getTreeViewer().setAutoExpandLevel(2);
 			getTreeViewer().setInput(getTreeViewer().getInput());
 			return control;
 		}
 	}
 }
