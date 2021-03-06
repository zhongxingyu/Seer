 package org.eclipse.jdt.internal.debug.ui.actions;
 
 /**********************************************************************
 Copyright (c) 2002 IBM Corp.  All rights reserved.
 This file is made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.jdt.core.IMember;
 import org.eclipse.jdt.core.ISourceRange;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
 import org.eclipse.jdt.debug.core.JDIDebugModel;
 import org.eclipse.jdt.internal.debug.ui.BreakpointUtils;
 import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
 import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
 import org.eclipse.jdt.internal.debug.ui.snippeteditor.JavaSnippetEditor;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * Action for adding/removing breakpoints at a line in a type represented
  * by the source shown in the active Java Editor.
  */
 public class ManageBreakpointActionDelegate implements IWorkbenchWindowActionDelegate, IPartListener {
 	
 	private boolean fInitialized= false;
 	private IAction fAction= null;
 	private int fLineNumber;
 	private IType fType= null;
 	private ITextEditor fTextEditor= null;
 	private IWorkbenchWindow fWorkbenchWindow= null;
 
 	public ManageBreakpointActionDelegate() {
 	}
 	
 	/**
 	 * Manages a breakpoint.
 	 */
 	protected void manageBreakpoint(IEditorInput editorInput) {
 		ISelectionProvider sp= getTextEditor().getSelectionProvider();
 		if (sp == null || getType() == null) {
 			beep();
 			return;
 		}
 		ISelection selection= sp.getSelection();
 		if (selection instanceof ITextSelection) {
 			IDocument document= getTextEditor().getDocumentProvider().getDocument(editorInput);
 			BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
 			int lineNumber = bv.getValidBreakpointLocation(document, ((ITextSelection)selection).getStartLine());		
 			if (lineNumber > -1) {
 				try {
 					IJavaLineBreakpoint breakpoint= JDIDebugModel.lineBreakpointExists(getType().getFullyQualifiedName(), lineNumber);
 					if (breakpoint != null) {
 						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
 					} else {
 						Map attributes = new HashMap(10);
 						BreakpointUtils.addJavaBreakpointAttributes(attributes, getType());
 						JDIDebugModel.createLineBreakpoint(BreakpointUtils.getBreakpointResource(getType()), getType().getFullyQualifiedName(), lineNumber, -1, -1, 0, true, attributes);
 					}
 				} catch (CoreException ce) {
 					ExceptionHandler.handle(ce, ActionMessages.getString("ManageBreakpointActionDelegate.error.title1"), ActionMessages.getString("ManageBreakpointActionDelegate.error.message1")); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Determines if a breakpoint exists on the line of the current selection.
 	 */
 	protected boolean breakpointExists(IEditorInput editorInput) {
 		IType type= getType(editorInput);
 		if (type != null) {
 			try {
 				return JDIDebugModel.lineBreakpointExists(type.getFullyQualifiedName(), getLineNumber()) == null;
 			} catch (CoreException ce) {
 				JDIDebugUIPlugin.log(ce);
 			}
 		}
 	
 		return false;
 	}
 	
 	protected IType getType(IEditorInput editorInput) {
 		IType type= null;
 		ISelectionProvider sp= getTextEditor().getSelectionProvider();
 		if (sp != null) {
 			ISelection s= sp.getSelection();
 			if (s instanceof ITextSelection) {
 				ITextSelection selection= (ITextSelection) s;
 				setLineNumber(selection.getStartLine() + 1);
 				type= getType();
 				if (type != null && type.exists()) {
 					try {
 						ISourceRange sourceRange= type.getSourceRange();
 						if (selection.getOffset() >= sourceRange.getOffset() && selection.getOffset() <= (sourceRange.getOffset() + sourceRange.getLength() - 1)) {
 							return type;
 						}
 					} catch(JavaModelException e) {
 						JDIDebugUIPlugin.log(e);
 					}	
 				}
 				type= getType0(selection, editorInput);
 			}
 		}
 		return type;
 	}
 	
 	protected IType getType0(ITextSelection selection, IEditorInput editorInput) {
 		IMember member= ActionDelegateHelper.getDefault().getCurrentMember(selection);
 		IType type= null;
 		if (member instanceof IType) {
 			type = (IType)member;
 		} else if (member != null) {
 			type= member.getDeclaringType();
 		}
 	
 		setType(type);
 		return type;
 	}
 	
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	public void run(IAction action) {
 		if (getTextEditor() != null) {
 			update();
 			manageBreakpoint(getTextEditor().getEditorInput());
 		}
 	}
 	
 	/**
 	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (!fInitialized) {
 			setAction(action);
 			if (getWorkbenchWindow() != null) {
 				IWorkbenchPage page= getWorkbenchWindow().getActivePage();
 				if (page != null) {
 					IEditorPart part= page.getActiveEditor();
 					if (part instanceof ITextEditor) {
 						if (!(part instanceof JavaSnippetEditor)) {
 							setTextEditor((ITextEditor)part);
							ISelectionProvider sp= getTextEditor().getSelectionProvider();
							if (sp != null) {
								update(sp.getSelection());
							}
 						}
 					}
 				}
 			}
 			fInitialized= true;
 		} 
 		
 		update(selection);
 	}
 		
 	protected void update(ISelection selection) {
 		setEnabledState(getTextEditor());
 	}
 	
 	protected void update() {
 		IAction action= getAction();
 		if (action != null) {
 			if (getTextEditor() != null) {
 				breakpointExists(getTextEditor().getEditorInput());
 			}
 			action.setEnabled(getTextEditor()!= null && getType() != null);
 		}
 	}
 	
 	protected int getLineNumber() {
 		return fLineNumber;
 	}
 	
 	protected void setLineNumber(int lineNumber) {
 		fLineNumber = lineNumber;
 	}
 	
 	protected IType getType() {
 		return fType;
 	}
 	
 	protected void setType(IType type) {
 		fType = type;
 	}
 	
 	protected IAction getAction() {
 		return fAction;
 	}
 
 	protected void setAction(IAction action) {
 		fAction = action;
 	}
 	
 	/**
 	 * @see IPartListener#partActivated(IWorkbenchPart)
 	 */
 	public void partActivated(IWorkbenchPart part) {
 		if (part instanceof ITextEditor) {
 			if (part instanceof JavaSnippetEditor) {
 				setTextEditor(null);
 			} else {
 				setTextEditor((ITextEditor)part);
 			}	
 		}
 	}
 
 	/**
 	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
 	 */
 	public void partBroughtToTop(IWorkbenchPart part) {
 	}
 
 	/**
 	 * @see IPartListener#partClosed(IWorkbenchPart)
 	 */
 	public void partClosed(IWorkbenchPart part) {
 		if (part == getTextEditor()) {
 			setTextEditor(null);
 			if (getAction() != null) {
 				getAction().setEnabled(false);
 			}
 		}
 	}
 
 	/**
 	 * @see IPartListener#partDeactivated(IWorkbenchPart)
 	 */
 	public void partDeactivated(IWorkbenchPart part) {
 	}
 
 	/**
 	 * @see IPartListener#partOpened(IWorkbenchPart)
 	 */
 	public void partOpened(IWorkbenchPart part) {
 		if (part instanceof ITextEditor) {
 			if (getTextEditor() == null) {
 				if (!(part instanceof JavaSnippetEditor)) {
 					setTextEditor((ITextEditor)part);
 				}
 			}
 		}
 	}
 	
 	protected ITextEditor getTextEditor() {
 		return fTextEditor;
 	}
 
 	protected void setTextEditor(ITextEditor editor) {
 		fTextEditor = editor;
 		setType(null);
 		setEnabledState(editor);
 	}
 
 	protected void setEnabledState(ITextEditor editor) {
 		if (getAction() != null) {
 			getAction().setEnabled(editor != null  
 			&& (editor.getSite().getId().equals(JavaUI.ID_CF_EDITOR)
 			|| editor.getSite().getId().equals(JavaUI.ID_CU_EDITOR)));
 		} 
 	}
 	
 	/**
 	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
 	 */
 	public void init(IWorkbenchWindow window) {
 		setWorkbenchWindow(window);
 		window.getPartService().addPartListener(this);
 	}
 
 	/**
 	 * @see IWorkbenchWindowActionDelegate#dispose()
 	 */
 	public void dispose() {
 		getWorkbenchWindow().getPartService().removePartListener(this);
 	}
 
 	protected IWorkbenchWindow getWorkbenchWindow() {
 		return fWorkbenchWindow;
 	}
 
 	protected void setWorkbenchWindow(IWorkbenchWindow workbenchWindow) {
 		fWorkbenchWindow = workbenchWindow;
 	}
 
 	protected void beep() {
 		if (JDIDebugUIPlugin.getActiveWorkbenchShell() != null) {
 			JDIDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
 		}
 	}
 
 }
