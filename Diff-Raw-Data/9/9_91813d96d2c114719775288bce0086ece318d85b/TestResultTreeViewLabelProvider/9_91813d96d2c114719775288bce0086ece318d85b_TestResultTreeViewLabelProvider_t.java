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
 package org.eclipse.jubula.client.ui.provider.labelprovider;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.core.persistence.Hibernator;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.swt.graphics.Image;
 
 
 /**
  * 
  * 
  * @author BREDEX GmbH
  * @created 18.10.2004
  *
  */
 public class TestResultTreeViewLabelProvider extends LabelProvider {
     /** ImageCache */
     private static Map < ImageDescriptor, Image > imageCache = 
         new HashMap < ImageDescriptor, Image > ();
 
     /**
      *  
      */
     public TestResultTreeViewLabelProvider() {
         super();
     }
 
     /**
      * dispose images
      */
     public void dispose() {
         for (Iterator i = imageCache.values().iterator(); i.hasNext();) {
             ((Image)i.next()).dispose();
         }
         imageCache.clear();
     }
 
     /**
      * 
      * @param element
      *            Object
      * @return Image
      */
     public Image getImage(Object element) {
         TestResultNode resultTest = (TestResultNode)element;
         INodePO node = resultTest.getNode();
         Image image = null;
         Class nodePoClass = Hibernator.getClass(node);
        if (Hibernator.isPoClassSubclass(nodePoClass, ITestSuitePO.class)) {
            image = IconConstants.TS_IMAGE;
        } else if (Hibernator.isPoClassSubclass(nodePoClass,
                ISpecTestCasePO.class)) {
            image = IconConstants.TC_IMAGE;
         }
         if (Hibernator.isPoClassSubclass(nodePoClass, IExecTestCasePO.class)) {
             if (Hibernator.isPoClassSubclass(nodePoClass,
                     IEventExecTestCasePO.class)) {
                 image = IconConstants.RESULT_EH_IMAGE;
             } else {
                 image = IconConstants.TC_IMAGE;
             }
         }
         if (Hibernator.isPoClassSubclass(nodePoClass, ICapPO.class)) {
             TestResultNode parent = resultTest.getParent();
             if (Hibernator.isPoSubclass(parent.getNode(),
                     IEventExecTestCasePO.class)) {
                 image = IconConstants.EH_CAP_IMAGE;
             } else {
                 image = IconConstants.CAP_IMAGE;
             }
         }
         return image;
     }
     /**
      * @param element
      *            Object
      * @return name String
      */
     public String getText(Object element) {
         if (element instanceof TestResultNode) {
             TestResultNode resultTest = (TestResultNode)element;
             String name = resultTest.getName();
             if (name != null) {
                 return name;
             }
             INodePO node = resultTest.getNode();
             Class nodePoClass = Hibernator.getClass(node);
             if (Hibernator.isPoClassSubclass(
                     nodePoClass, ITestSuitePO.class)) {
                 return "ResultTestSuite"; //$NON-NLS-1$
             }
             if (Hibernator.isPoClassSubclass(
                     nodePoClass, IExecTestCasePO.class)) {
                 return "ResultTestCase"; //$NON-NLS-1$
             } 
             if (Hibernator.isPoClassSubclass(
                     nodePoClass, ICapPO.class)) {
                 return "ResultCap"; //$NON-NLS-1$
             }
         }
         throw unknownElement(element);
     }
 
     /**
      * 
      * @param element
      *            Object
      * @return RuntimeException Unknown type
      */
     public RuntimeException unknownElement(Object element) {
         return new RuntimeException(Messages.UnknownTypeOfElementInTreeOfType
             + element.getClass().getName());
     }
 }
