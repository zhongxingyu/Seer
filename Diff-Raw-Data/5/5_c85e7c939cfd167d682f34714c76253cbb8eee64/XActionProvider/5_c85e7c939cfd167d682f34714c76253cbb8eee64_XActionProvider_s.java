 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.navigator;
 
 import java.util.Properties;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.action.ContributionItem;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.actions.ActionContext;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.navigator.CommonActionProvider;
 import org.jboss.tools.common.meta.action.XAction;
 import org.jboss.tools.common.meta.action.XActionList;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.ui.action.XModelObjectAction;
 import org.jboss.tools.common.model.ui.action.XModelObjectActionList;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 
 public class XActionProvider extends CommonActionProvider {
 
 	public void setContext(ActionContext context) {
 		super.setContext(context);
 	}
 
     public void fillActionBars(IActionBars actionBars) {
 		ActionContext c = getContext();
 		ISelection s = c.getSelection();
 		if(s == null || s.isEmpty() || !(s instanceof IStructuredSelection)) return;
 		Object e = ((IStructuredSelection)s).getFirstElement();
 		if(e instanceof XModelObject) {
 			XModelObject o = (XModelObject)e;
 			registerAction(actionBars, o, "DeleteActions.Delete", ActionFactory.DELETE.getId());
 			registerAction(actionBars, o, "CopyActions.Copy", ActionFactory.COPY.getId());
 			registerAction(actionBars, o, "CopyActions.Paste", ActionFactory.PASTE.getId());
 			registerAction(actionBars, o, "CopyActions.Cut", ActionFactory.CUT.getId());
 		}
     }
     
     private void registerAction(IActionBars actionBars, XModelObject o, String path, String id) {
 		XAction xa = o.getModelEntity().getActionList().getAction(path);
 		if(xa != null) {
 			XModelObjectAction a = new XModelObjectAction(xa, o, null, new Object[]{o, getRunningProperties()});
 			actionBars.setGlobalActionHandler(id, a.getEclipseAction());
 		}
     }
     
     public void updateActionBars() {
     }
 
     public void fillContextMenu(IMenuManager menu) {
 		ActionContext c = getContext();
 		ISelection s = c.getSelection();
 		if(s == null || s.isEmpty() || !(s instanceof IStructuredSelection)) return;
 		Object e = ((IStructuredSelection)s).getFirstElement();
 		if(e instanceof IFile) {
 			IFile f = (IFile)e;
 			XModelObject o = EclipseResourceUtil.getObjectByResource(f);
 			add(o, menu, false);
 		} else if((e instanceof XModelObject)) {
 			XModelObject o = (XModelObject)e;
 			add(o, menu, true);
 		}
     }
     
     void add(XModelObject o, IMenuManager menu, boolean removeAll) {
 		IContributionItem[] is = menu.getItems();
 		for (int i = 0; i < is.length; i++) {
 			if(is[i] instanceof XContributionItem) return;
 		}
 //		if(removeAll) menu.removeAll();
 		XContributionItem item = new XContributionItem(removeAll);
 		item.setModelObject(o);		
 		menu.add(item);
     }
 
     class XContributionItem extends ContributionItem {
     	XModelObject o;
     	boolean removeAll = false;
     	
     	XContributionItem(boolean removeAll) {
     		this.removeAll = removeAll;
     	}
     	
     	public void setModelObject(XModelObject o) {
     		this.o = o;
     	}
 
         public void fill(Menu menu, int index) {
         	if(o.getAdapter(IResource.class) != null && !removeAll) {
     			MenuItem item = new MenuItem(menu, SWT.CASCADE);
     			item.setText("Red Hat");
     			menu = new Menu(item);
     			item.setMenu(menu);
         	}
         	XModelObject[] os = null;
         	XModelObjectActionList l = new XModelObjectActionList(getActionList(o), o, os, new Object[]{o, getRunningProperties()});
         	l.fillMenu(menu);
         	l.removeLastSeparator(menu);
         }
 
     	protected XActionList getActionList(XModelObject o) {
     		return o.getModelEntity().getActionList();
     	}
 
     }
 
 	private Properties getRunningProperties() {
 		Properties p = new Properties();
 		fillRunningProperties(p);
 		return p;
 	}
 	
 	protected void fillRunningProperties(Properties p) {
 		p.setProperty("actionSourceGUIComponentID", "navigator");
 	}
 }
 
