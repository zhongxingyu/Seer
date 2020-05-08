 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.dialogs;
 
 import org.eclipse.jface.preference.PreferenceManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.internal.dialogs.PropertyDialog;
 
 
 /**
  * @author BREDEX GmbH
  * @created 31.01.2007
  */
 public class JBPropertyDialog extends PropertyDialog {
 
     /** true, if the started aut was changed */
     private boolean m_startedAutChanged = false;
     
     /**
      * Create an instance of the receiver.
      * @param parentShell the parent shell
      * @param mng the current preferenceManager
      * @param selection the current selection
      */
     public JBPropertyDialog(Shell parentShell, PreferenceManager mng,
         ISelection selection) {
         
         super(parentShell, mng, selection);
     }
 
     /**
      * @param startedAutChanged the startedAutChanged to set
      */
     public void setStartedAutChanged(boolean startedAutChanged) {
         m_startedAutChanged = startedAutChanged;
     }
     
     /**
      * {@inheritDoc}
      */
     public int open() {
         int returnCode = super.open();
         if (returnCode == Window.OK) {
             checkStartedAUT();
         }
         return returnCode;
     }
 
     /**
      * checks changes in started aut
      */
     private void checkStartedAUT() {
        if (m_startedAutChanged) {
             ErrorHandlingUtil.createMessageDialog(
                 MessageIDs.I_STARTED_AUT_CHANGED, 
                new Object[]{TestExecution.getInstance().getConnectedAut()
                    .getName()}, null);
         }
     }
 }
