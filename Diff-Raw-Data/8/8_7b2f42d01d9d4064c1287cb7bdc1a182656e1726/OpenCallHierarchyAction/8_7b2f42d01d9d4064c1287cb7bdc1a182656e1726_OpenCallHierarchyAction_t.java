 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *          (report 36180: Callers/Callees view)
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.callhierarchy;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.ui.IDLTKStatusConstants;
 import org.eclipse.dltk.internal.ui.actions.ActionMessages;
 import org.eclipse.dltk.internal.ui.actions.ActionUtil;
 import org.eclipse.dltk.internal.ui.actions.SelectionConverter;
 import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.actions.SelectionDispatchAction;
 import org.eclipse.dltk.ui.util.ExceptionHandler;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IWorkbenchSite;
 
 
 /**
  * This action opens a call hierarchy on the selected method.
  * <p>
  * The action is applicable to selections containing elements of type
  * <code>IMethod</code>.
  */
 public class OpenCallHierarchyAction extends SelectionDispatchAction {
     
     private ScriptEditor fEditor;
     
     /**
      * Creates a new <code>OpenCallHierarchyAction</code>. The action requires
      * that the selection provided by the site's selection provider is of type <code>
      * org.eclipse.jface.viewers.IStructuredSelection</code>.
      * 
      * @param site the site providing context information for this action
      */
     public OpenCallHierarchyAction(IWorkbenchSite site) {
         super(site);
         setText(CallHierarchyMessages.OpenCallHierarchyAction_label); 
         setToolTipText(CallHierarchyMessages.OpenCallHierarchyAction_tooltip); 
         setDescription(CallHierarchyMessages.OpenCallHierarchyAction_description); 
 //        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_OPEN_ACTION);
         if (DLTKCore.DEBUG) {
 			System.err.println("Add help support here...");
 		}		
 
     }
     
     /**
      * Creates a new <code>OpenCallHierarchyAction</code>. The action requires
      * that the selection provided by the given selection provider is of type <code>
      * org.eclipse.jface.viewers.IStructuredSelection</code>.
      * 
      * @param site the site providing context information for this action
 	 * @param provider a special selection provider which is used instead 
 	 *  of the site's selection provider or <code>null</code> to use the site's
 	 *  selection provider
      * 
 	 *
 	 * @deprecated Use {@link #setSpecialSelectionProvider(ISelectionProvider)} instead. This API will be
 	 * removed after 3.2 M5.
      */
     public OpenCallHierarchyAction(IWorkbenchSite site, ISelectionProvider provider) {
         this(site);
         setSpecialSelectionProvider(provider);
     }
     
     /**
      * Note: This constructor is for internal use only. Clients should not call this constructor.
      */
     public OpenCallHierarchyAction(ScriptEditor editor) {
         this(editor.getEditorSite());
         fEditor= editor;
         setEnabled(SelectionConverter.canOperateOn(fEditor));
     }
     
     /* (non-Javadoc)
      * Method declared on SelectionDispatchAction.
      */
 	public void selectionChanged(ITextSelection selection) {
         // Do nothing
     }
 
     /* (non-Javadoc)
      * Method declared on SelectionDispatchAction.
      */
 	public void selectionChanged(IStructuredSelection selection) {
         setEnabled(isEnabled(selection));
     }
     
     private boolean isEnabled(IStructuredSelection selection) {
         if (selection.size() != 1)
             return false;
         Object input= selection.getFirstElement();
         if (!(input instanceof IModelElement))
             return false;
         switch (((IModelElement)input).getElementType()) {
             case IModelElement.METHOD:
                 return true;
             default:
                 return false;
         }
     }
     
     /* (non-Javadoc)
      * Method declared on SelectionDispatchAction.
      */
 	public void run(ITextSelection selection) {
         IModelElement input= SelectionConverter.getInput(fEditor);
         if (!ActionUtil.isProcessable(getShell(), input))
             return;     
         
         try {
 			IModelElement[] elements= SelectionConverter.codeResolveOrInputForked(fEditor);
 			if (elements == null)
 			    return;
 			List candidates= new ArrayList(elements.length);
 			for (int i= 0; i < elements.length; i++) {
 			    IModelElement[] resolvedElements= CallHierarchyUI.getCandidates(elements[i]);
 			    if (resolvedElements != null)   
 			        candidates.addAll(Arrays.asList(resolvedElements));
 			}
 			if (candidates.isEmpty()) {
 			    IModelElement enclosingMethod= getEnclosingMethod(input, selection);
 			    if (enclosingMethod != null) {
 			        candidates.add(enclosingMethod);
 			    }
 			}
 			run((IModelElement[])candidates.toArray(new IModelElement[candidates.size()]));
 		} catch (InvocationTargetException e) {
 			ExceptionHandler.handle(e, getShell(), getErrorDialogTitle(), ActionMessages.SelectionConverter_codeResolve_failed);
 		} catch (InterruptedException e) {
 			// cancelled
 		}
     }
     
     private IModelElement getEnclosingMethod(IModelElement input, ITextSelection selection) {
         IModelElement enclosingElement= null;
         try {
             switch (input.getElementType()) {
 //                case IModelElement.CLASS_FILE :
 //                    IClassFile classFile= (IClassFile) input.getAncestor(IModelElement.CLASS_FILE);
 //                    if (classFile != null) {
 //                        enclosingElement= classFile.getElementAt(selection.getOffset());
 //                    }
 //                    break;
                 case IModelElement.SOURCE_MODULE :
                     ISourceModule cu= (ISourceModule) input.getAncestor(IModelElement.SOURCE_MODULE);
                     if (cu != null) {
                         enclosingElement= cu.getElementAt(selection.getOffset());
                     }
                     break;
             }
             if (enclosingElement != null && enclosingElement.getElementType() == IModelElement.METHOD) {
                 return enclosingElement;
             }
         } catch (ModelException e) {
             DLTKUIPlugin.log(e);
         }
 
         return null;
     }
 
     /* (non-Javadoc)
      * Method declared on SelectionDispatchAction.
      */
 	public void run(IStructuredSelection selection) {
         if (selection.size() != 1)
             return;
         Object input= selection.getFirstElement();
 
         if (!(input instanceof IModelElement)) {
             IStatus status= createStatus(CallHierarchyMessages.OpenCallHierarchyAction_messages_no_java_element); 
             openErrorDialog(status);
             return;
         }
         IModelElement element= (IModelElement) input;
         if (!ActionUtil.isProcessable(getShell(), element))
             return;
 
         List result= new ArrayList(1);
         IStatus status= compileCandidates(result, element);
         if (status.isOK()) {
             run((IModelElement[]) result.toArray(new IModelElement[result.size()]));
         } else {
             openErrorDialog(status);
         }
     }
     
     private int openErrorDialog(IStatus status) {
         String message= CallHierarchyMessages.OpenCallHierarchyAction_messages_title; 
         String dialogTitle= getErrorDialogTitle();
         return ErrorDialog.openError(getShell(), dialogTitle, message, status);
 	}
 
     private static String getErrorDialogTitle() {
         return CallHierarchyMessages.OpenCallHierarchyAction_dialog_title; 
     }
     
 	public void run(IModelElement[] elements) {
         if (elements.length == 0) {
             getShell().getDisplay().beep();
             return;
         }
        CallHierarchyUI.open(elements, getSite().getWorkbenchWindow(), getCallHierarchyID());
     }
     
     private static IStatus compileCandidates(List result, IModelElement elem) {
         IStatus ok= new Status(IStatus.OK, DLTKUIPlugin.getPluginId(), 0, "", null); //$NON-NLS-1$        
         switch (elem.getElementType()) {
             case IModelElement.METHOD:
                 result.add(elem);
                 return ok;
         }
         return createStatus(CallHierarchyMessages.OpenCallHierarchyAction_messages_no_valid_java_element); 
     }
     
     private static IStatus createStatus(String message) {
         return new Status(IStatus.INFO, DLTKUIPlugin.getPluginId(), IDLTKStatusConstants.INTERNAL_ERROR, message, null);
    }         
    public String getCallHierarchyID() {
    	return "org.eclipse.dltk.callhierarchy.view";
    }
 }
