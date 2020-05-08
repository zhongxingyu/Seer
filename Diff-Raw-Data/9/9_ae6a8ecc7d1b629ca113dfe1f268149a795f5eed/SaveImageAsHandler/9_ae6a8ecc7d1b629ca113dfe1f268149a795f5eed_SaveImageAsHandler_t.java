 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.handlers;
 
import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.ui.handlers.AbstractSelectionBasedHandler;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 /**
  * @author BREDEX GmbH
  * @created 11.09.2013
  */
 public class SaveImageAsHandler extends AbstractSelectionBasedHandler {
 
     /**
      * suggest only 255 character long file names
      */
     private static final int MAX_FILE_NAME_LENGTH = 255;
 
     /**
      * {@inheritDoc}
      */
     protected Object executeImpl(ExecutionEvent event) {
         IStructuredSelection structuredSelection = getSelection();
         Object selectedObject = structuredSelection.getFirstElement();
         if (selectedObject instanceof TestResultNode) {
             TestResultNode result = (TestResultNode) selectedObject;
 
             // necessary to get test suite name
             TestResultNode parent = result;
             while (parent.getParent() != null) {
                 parent = parent.getParent();
             }
 
             // get the date of test from time stamp
             DateFormat format = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
             String date = format.format(result.getTimeStamp());
 
             FileDialog saveDialog = new FileDialog(HandlerUtil
                     .getActiveWorkbenchWindow(event).getShell(), SWT.SAVE);
             String fileName = "ErrorInTest_" + parent.getName() + "_" //$NON-NLS-1$ //$NON-NLS-2$
                                 + result.getNode().getName();
             // eliminate whitespaces and characters which are illegal in a file name
             fileName = fileName.replaceAll("[\\s\\?\\\\/:|<>\\*\"]", ""); //$NON-NLS-1$ //$NON-NLS-2$
 
             String fileEnding = "_" + date + ".png"; //$NON-NLS-1$ //$NON-NLS-2$
            fileName = StringUtils.substring(fileName, 0,
                    MAX_FILE_NAME_LENGTH
                    - fileEnding.length()
                    - saveDialog.getFilterPath().length());
            fileName = fileName + fileEnding;
             saveDialog.setFileName(fileName);
             saveDialog.setFilterExtensions(new String[] { "*.png" }); //$NON-NLS-1$
             saveDialog.setOverwrite(true);
             String path = saveDialog.open();
 
             if (path != null) {
                 if (result.getScreenshot() != null) {
                     try {
                         File file = new File(path);
                         OutputStream out = new FileOutputStream(file);
                         out.write(result.getScreenshot());
                         out.flush();
                         out.close();
                     } catch (FileNotFoundException e) {
                         ErrorHandlingUtil.createMessageDialog(MessageIDs.
                                 E_FILE_NO_PERMISSION);
                     } catch (IOException e) {
                         ErrorHandlingUtil.createMessageDialog(MessageIDs.
                                 E_IO_EXCEPTION);
                     }
                 }
             }
         }
         return null;
     }
 
 }
