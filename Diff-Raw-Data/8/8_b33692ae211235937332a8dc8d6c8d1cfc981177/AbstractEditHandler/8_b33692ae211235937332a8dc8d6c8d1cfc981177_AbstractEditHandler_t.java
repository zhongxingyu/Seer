 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.commands.handlers;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.ui.RoomUiModule;
 import org.eclipse.etrice.ui.behavior.editor.BehaviorEditor;
 import org.eclipse.etrice.ui.structure.editor.StructureEditor;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.views.contentoutline.ContentOutline;
 import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
 import org.eclipse.xtext.resource.IFragmentProvider;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.ui.editor.XtextEditor;
 import org.eclipse.xtext.ui.editor.model.IXtextDocument;
 import org.eclipse.xtext.ui.editor.outline.impl.EObjectNode;
 import org.eclipse.xtext.ui.editor.utils.EditorUtils;
 import org.eclipse.xtext.util.CancelIndicator;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 import org.eclipse.xtext.validation.CheckMode;
 import org.eclipse.xtext.validation.IResourceValidator;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 /**
  * description
  *
  * @author Henrik Rentz-Reichert initial contribution and API
  *
  */
 public abstract class AbstractEditHandler extends AbstractHandler {
 
 	@Inject
 	protected IResourceValidator resourceValidator;
 	
 	@Inject
 	IFragmentProvider fragmentProvider;
 	
 	private EObjectAtOffsetHelper helper = new EObjectAtOffsetHelper();
 	
 	public AbstractEditHandler() {
 		super();
 		
 		Injector injector = RoomUiModule.getInjector();
 		injector.injectMembers(this);
 	}
 
 	abstract protected boolean prepare(XtextEditor xtextEditor, final String fragment);
 	abstract protected void openEditor(EObject object);
 	abstract protected boolean isEnabled(String fragment);
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
 		IEditorPart editor = window.getActivePage().getActiveEditor();
 		if (editor instanceof XtextEditor) {
 			ISelection selection = HandlerUtil.getCurrentSelection(event);
 			if (selection instanceof IStructuredSelection) {
 				// event from the xtext editor's outline view
 				IStructuredSelection ss = (IStructuredSelection) selection;
 				Object sel = ss.getFirstElement();
 				if (sel instanceof EObjectNode) {
 					XtextEditor xtextEditor = EditorUtils.getActiveXtextEditor(event);
 					IXtextDocument document = xtextEditor.getDocument();
 					final String fragment = ((EObjectNode) sel).getEObjectURI().fragment();
 					if (checkPrerequisites(xtextEditor, document, fragment)) {
 						openEditor(document, fragment);
 					}
 				}
 			}
 			else if (selection instanceof ITextSelection) {
 				// event from the xtext editor itself
 				final ITextSelection ss = (ITextSelection) selection;
 				XtextEditor xed = (XtextEditor) editor;
 				IXtextDocument document = xed.getDocument();
 				String fragment = document.readOnly(new IUnitOfWork<String, XtextResource>() {
 					@Override
 					public String exec(XtextResource resource) throws Exception {
 						EObject obj = helper.resolveElementAt(resource, ss.getOffset());
 						while (obj!=null) {
 							if (obj instanceof StructureClass) {
 								return fragmentProvider.getFragment(obj, null);
 							}
 							obj = obj.eContainer();
 						}
 						return "";
 					}
 				});
 				if (checkPrerequisites(xed, document, fragment)) {
 					openEditor(document, fragment);
 				}
 			}
 		}
 		else if (editor instanceof StructureEditor) {
 			StructureClass sc = ((StructureEditor)editor).getStructureClass();
 			if (sc instanceof ActorClass) {
 				openEditor(sc);
 			}
 		}
 		else if (editor instanceof BehaviorEditor) {
 			ActorClass ac = ((BehaviorEditor)editor).getActorClass();
 			openEditor(ac);
 		}
 		return null;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		IWorkbench wb = PlatformUI.getWorkbench();
 		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
 		IWorkbenchPage page = win.getActivePage();
 		IWorkbenchPart part = page.getActivePart();
 		if (part instanceof ContentOutline) {
 			ISelection selection = ((ContentOutline)part).getSelection();
 			if (selection instanceof IStructuredSelection) {
 				IStructuredSelection ss = (IStructuredSelection) selection;
 				Object sel = ss.getFirstElement();
 				if (sel instanceof EObjectNode) {
 					EObjectNode node = (EObjectNode) sel;
 					String fragment = node.getEObjectURI().fragment();
 					return isEnabled(fragment);
 				}
 			}
 		}
 		IEditorPart editor = page.getActiveEditor();
 		if (editor instanceof XtextEditor) {
 			ISelection selection = ((XtextEditor) editor).getSelectionProvider().getSelection();
 			if (selection instanceof ITextSelection) {
 				// event from the xtext editor itself
 				final ITextSelection ss = (ITextSelection) selection;
 				XtextEditor xed = (XtextEditor) editor;
 				IXtextDocument document = xed.getDocument();
 				String fragment = document.readOnly(new IUnitOfWork<String, XtextResource>() {
 					@Override
 					public String exec(XtextResource resource) throws Exception {
 						EObject obj = helper.resolveElementAt(resource, ss.getOffset());
 						while (obj!=null) {
 							if (obj instanceof StructureClass) {
 								return fragmentProvider.getFragment(obj, null);
 							}
 							obj = obj.eContainer();
 						}
 						return "";
 					}
 				});
 				return isEnabled(fragment);
 			}
 		}
		else if (editor instanceof StructureEditor) {
			StructureClass sc = ((StructureEditor)editor).getStructureClass();
			return isEnabled(fragmentProvider.getFragment(sc, null));
		}
		else if (editor instanceof BehaviorEditor) {
			ActorClass ac = ((BehaviorEditor)editor).getActorClass();
			return isEnabled(fragmentProvider.getFragment(ac, null));
		}
 		return false;
 	}
 
 	protected void openEditor(IXtextDocument document, final String fragment) {
 		document.readOnly(new IUnitOfWork.Void<XtextResource>() {
 			@Override
 			public void process(XtextResource resource) throws Exception {
 				if (resource != null) {
 					EObject object = resource.getEObject(fragment);
 					openEditor(object);
 				}
 			}
 		});
 	}
 
 	protected boolean checkPrerequisites(XtextEditor xtextEditor,
 			IXtextDocument document, final String fragment) {
 		if (hasIssues(document, new NullProgressMonitor())) {
 			MessageDialog.openError(xtextEditor.getSite().getShell(), "Validation Errors", "The editor has validation errors.\nCannot open diagram!");
 			return false;
 		}
 		if (xtextEditor.isDirty()) {
 			if (!MessageDialog.openQuestion(xtextEditor.getSite().getShell(), "Save model file", "The editor will be saved before opening the diagram editor.\nProceed?"))
 				return false;
 			// postpone save to avoid doing it twice
 		}
 		if (!prepare(xtextEditor, fragment))
 			return false;
 		if (xtextEditor.isDirty()) {
 			xtextEditor.doSave(new NullProgressMonitor());
 		}
 		return true;
 	}
 
 	public boolean hasIssues(IXtextDocument xtextDocument, final IProgressMonitor monitor) {
 		final boolean issues = xtextDocument
 				.readOnly(new IUnitOfWork<Boolean, XtextResource>() {
 					public Boolean exec(XtextResource resource) throws Exception {
 						if (resource == null)
 							return false;
 						return !resourceValidator.validate(resource, CheckMode.NORMAL_AND_FAST, new CancelIndicator() {
 							public boolean isCanceled() {
 								return monitor.isCanceled();
 							}
 						}).isEmpty();
 					}
 				});
 		return issues;
 	}
 }
