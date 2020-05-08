 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.jsf.facesconfig.ui.pageflow.command;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jst.jsf.facesconfig.common.IFileFolderConstants;
 import org.eclipse.jst.jsf.facesconfig.common.dialogs.CommonResourceDialog;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorPlugin;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.PageflowMessages;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.Pageflow;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowNode;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowPage;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.util.PageflowValidation;
 import org.eclipse.jst.jsf.facesconfig.ui.util.WebrootUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchWindow;
 
 /**
  * This is the creation command for pageflow editpart
  * 
  * @author Xiao-guang Zhang
  */
 public class CreateNodeCommand extends Command implements IPreExecuteCommand {
 	private static final String UNNAMED_NODE = "unnamed";
 
 	/** new pageflow node */
 	private PageflowNode child;
 
 	/** size of pageflow node figure */
 	private Rectangle rect;
 
 	/** parent pageflow */
 	private Pageflow parent;
 
 	/** index of pageflow nodes */
 	// private int _index = -1;
 	public CreateNodeCommand() {
 		// Pageflow.Commands.CreateNodeCommand.Label = Create new node
 		super(PageflowMessages.Pageflow_Commands_CreateNodeCommand_Label);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.commands.Command#canExecute()
 	 */
 	public boolean canExecute() {
 		return child != null && parent != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see Command#execute()
 	 */
 	public void execute() {
 		if (rect != null) {
 			child.setX(rect.x);
 			child.setY(rect.y);
 			if (!rect.isEmpty()) {
 				child.setHeight(rect.getSize().height);
 				child.setWidth(rect.getSize().width);
 			}
 		}
 
 		if (child.getName() == UNNAMED_NODE) {
 			child.setName(parent.getDefaultNodeName(child.getClass()));
 		}
 
 		parent.getNodes().add(child);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see Command#redo()
 	 */
 	public void redo() {
 		if (rect != null) {
 			child.setX(rect.x);
 			child.setY(rect.y);
 			if (!rect.isEmpty()) {
 				child.setHeight(rect.getSize().height);
 				child.setWidth(rect.getSize().width);
 			}
 		}
 
 		parent.getNodes().add(child);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see Command#undo()
 	 */
 	public void undo() {
 		parent.getNodes().remove(child);
 	}
 
 	/**
 	 * select a jsp page in current project.
 	 * 
 	 * @return
 	 */
 	private boolean selectJSPPage() {
 
 		Shell shell = null;
 		IWorkbenchWindow workbenchWindow = EditorPlugin
 				.getActiveWorkbenchWindow();
 		if (workbenchWindow.getShell() != null) {
 			shell = workbenchWindow.getShell();
 		} else {
 			shell = new Shell();
 		}
 		IProject project = WebrootUtil.getProject(getParent());
 
 		CommonResourceDialog dialog = new CommonResourceDialog(shell, project,
 				SWT.NONE);
 
 		dialog
 				.setTitle(PageflowMessages.Pageflow_Commands_CreateNodeCommand_SelectJSPDialog_Title);
		dialog.setSuffixs(WebrootUtil.getJSPFileExtensions());
 		dialog
 				.setResourceDescription(PageflowMessages.Pageflow_Commands_CreateNodeCommand_SelectJSPDialog_Description);
 
 		if (dialog.open() == Window.OK) {
 			Object[] result = dialog.getResult();
 			if (result != null) {
 				IFile jspFile = (IFile) result[0];
 				if (jspFile != null) {
 					// get the project path for the new created file, i.e.,
 					// /project/webroot/*.jsp
 					String jsfSelection = WebrootUtil.getWebPath(jspFile
 							.getFullPath());
 					if (jsfSelection != null && jsfSelection.length() > 0) {
 						if (PageflowValidation.getInstance().isExistedPage(
 								this.parent, jsfSelection)) {
 							// Pageflow.PageflowEditor.Alert.DNDResourceTitle =
 							// Pageflow Creation Error
 							// Pageflow.PageflowEditor.Alert.ExistingPage = The
 							// web page {0} is already existed in current
 							// PageFlow.
 							EditorPlugin
 									.getAlerts()
 									.error(
 											"Pageflow.PageflowEditor.Alert.DNDResourceTitle",
 											"Pageflow.PageflowEditor.Alert.ExistingPage",
 											jsfSelection);
 							return false;
 						}
 
 						((PageflowPage) (child)).setPath(jsfSelection);
 						((PageflowPage) (child)).setName(WebrootUtil
 								.getPageNameFromWebPath(jsfSelection));
 					}
 
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * call the JSF wizard to create a new jsf page.
 	 * 
 	 * 
 	 */
 	public Pageflow getParent() {
 		return parent;
 	}
 
 	/**
 	 * set the child node
 	 * 
 	 * @param subpart -
 	 *            new child pageflow node
 	 */
 	public void setChild(PageflowNode subpart) {
 		child = subpart;
 	}
 
 	/**
 	 * set the location of the new pageflow node
 	 * 
 	 * @param r -
 	 *            location of the new pageflow node
 	 */
 	public void setLocation(Rectangle r) {
 		rect = r;
 	}
 
 	/**
 	 * set the parent pageflow
 	 * 
 	 * @param newParent -
 	 *            the new parent pageflow
 	 */
 	public void setParent(Pageflow newParent) {
 		parent = newParent;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.sybase.stf.jmt.editors.pageflow.commands.IPreExecuteCommand#preExecute()
 	 */
 	public boolean preExecute() {
 		// note that the model adds the ports to the node in this call
 		// pop up the new the wizard to create the new jsf file
 		if (child instanceof PageflowPage) {
 			// if the new page's file path is not empty, i.e., drag a file from
 			// resource navigator view
 			if (null != ((PageflowPage) child).getPath()) {
 				if (PageflowValidation.getInstance().isExistedPage(parent,
 						(PageflowPage) child)) {
 					// Pageflow.PageflowEditor.Alert.DNDResourceTitle = Pageflow
 					// Creation Error
 					// Pageflow.PageflowEditor.Alert.ExistingPage = The web page
 					// {0} is already existed in current PageFlow.
 					EditorPlugin.getAlerts().error(
 							"Pageflow.PageflowEditor.Alert.DNDResourceTitle",
 							"Pageflow.PageflowEditor.Alert.ExistingPage",
 							((PageflowPage) child).getPath());
 					return false;
 				}
 			} else
 			// create a new jsf page from scratch
 			{
 				// return createNewJSFPage();
 				return selectJSPPage();
 			}
 		}
 		return true;
 	}
 }
