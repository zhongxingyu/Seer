 /*******************************************************************************
  * Copyright (c) 2004, 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.sourceprovider.ActiveProjectSourceProvider;
 import org.eclipse.jubula.client.ui.rcp.views.TestCaseBrowser;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author BREDEX GmbH
  */
 public class MultipleTCBTracker implements IPartListener2 {
     /**
      * singleton instance
      */
     private static MultipleTCBTracker instance = null;
 
     /**
      * the list of open test case browsers
      */
     private List<IWorkbenchPartReference> m_tcb = 
             new ArrayList<IWorkbenchPartReference>();
 
     /**
      * the main test case browser
      */
     private TestCaseBrowser m_mainTCB = null;
     
     /**
      * the source provider
      */
     private ActiveProjectSourceProvider m_provider = null;
     
     /**
      * private Constructor
      */
     private MultipleTCBTracker() {
         // empty
     }
 
     /**
      * get single instance
      * 
      * @return single instance of ImportFileBP
      */
     public static MultipleTCBTracker getInstance() {
         if (instance == null) {
             instance = new MultipleTCBTracker();
         }
         return instance;
     }
 
     /** {@inheritDoc} */
     public void partActivated(IWorkbenchPartReference partRef) {
         // nothing to be done in here
     }
 
     /** {@inheritDoc} */
     public void partBroughtToTop(IWorkbenchPartReference partRef) {
         // nothing to be done in here
     }
 
     /** {@inheritDoc} */
     public void partClosed(IWorkbenchPartReference partRef) {
        if (Constants.TC_BROWSER_ID.equals(partRef.getId())) {
             m_tcb.remove(partRef);
             setMainTCB(getOpenTCBs().size() > 0 ? getOpenTCBs().get(0) : null);
             fireStateChanged();
         }
     }
 
     /** {@inheritDoc} */
     public void partDeactivated(IWorkbenchPartReference partRef) {
         // nothing to be done in here
     }
 
     /** {@inheritDoc} */
     public void partOpened(IWorkbenchPartReference partRef) {
        if (Constants.TC_BROWSER_ID.equals(partRef.getId())) {
             m_tcb.add(partRef);
             if (getOpenTCBs().size() > 0) {
                 setMainTCB(getOpenTCBs().get(0));
             }
             fireStateChanged();
         }
     }
 
     /** {@inheritDoc} */
     public void partHidden(IWorkbenchPartReference partRef) {
         // nothing to be done in here
     }
 
     /** {@inheritDoc} */
     public void partVisible(IWorkbenchPartReference partRef) {
         // nothing to be done in here
     }
 
     /** {@inheritDoc} */
     public void partInputChanged(IWorkbenchPartReference partRef) {
         // nothing to be done in here
     }
 
     /**
      * @return the currently open instances of the TCB view
      */
     public List<TestCaseBrowser> getOpenTCBs() {
         List<TestCaseBrowser> tcbs = new ArrayList<TestCaseBrowser>();
 
         for (IWorkbenchPartReference ref : m_tcb) {
             IWorkbenchPart part = ref.getPart(false);
             if (part != null) {
                 tcbs.add((TestCaseBrowser) part);
             }
         }
 
         return tcbs;
     }
 
     /**
      * @return the currently main TCB or <code>null</code> if there is none
      */
     public TestCaseBrowser getMainTCB() {
         if (m_mainTCB == null) {
             m_mainTCB = getOpenTCBs().size() > 0 ? getOpenTCBs().get(0) : null;
         }
         return m_mainTCB;
     }
 
     /**
      * @param mainTCB
      *            the mainTCB to set; may be <code>null</code>.
      */
     public void setMainTCB(TestCaseBrowser mainTCB) {
         TestCaseBrowser oldMainTCB = m_mainTCB;
         TestCaseBrowser newMainTCB = mainTCB;
 
         m_mainTCB = mainTCB;
         
         if (newMainTCB == null) {
             return;
         }
 
         final String tcbTitle = Messages.TestCaseBrowser;
         final String mainTcbTitle = Messages.TestCaseBrowserMainPrefix
                 + tcbTitle;
         
         // reset old name
         if (oldMainTCB != null) {
             oldMainTCB.setViewTitle(tcbTitle);
         }
         
         // set new TCB name
         final int tcbCount = getOpenTCBs().size();
         if (tcbCount == 1) {
             newMainTCB.setViewTitle(tcbTitle);
         } else if (tcbCount > 1) {
             newMainTCB.setViewTitle(mainTcbTitle);
         }
     }
     
     /**
      * fire data changed events
      */
     private void fireStateChanged() {
         if (getProvider() != null) {
             getProvider().handleProjectLoaded();
         }
     }
     
     /**
      * register listener
      */
     public void registerListener() {
         IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
                 .getActiveWorkbenchWindow();
         if (activeWorkbenchWindow != null) {
             activeWorkbenchWindow.getPartService().addPartListener(this);
         }
     }
 
     /**
      * @return the provider
      */
     public ActiveProjectSourceProvider getProvider() {
         return m_provider;
     }
 
     /**
      * @param provider the provider to set
      */
     public void setProvider(ActiveProjectSourceProvider provider) {
         m_provider = provider;
     }
 }
