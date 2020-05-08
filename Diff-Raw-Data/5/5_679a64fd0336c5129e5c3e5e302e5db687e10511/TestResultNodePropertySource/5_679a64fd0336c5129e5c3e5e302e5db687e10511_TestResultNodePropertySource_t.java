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
 package org.eclipse.jubula.client.ui.controllers.propertysources;
 
 import java.util.Date;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.ITestCasePO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.core.model.TestResultParameter;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.views.properties.PropertyDescriptor;
 
 
 /**
  * @author Markus Tiede
  * @created 22.11.2011
  */
 @SuppressWarnings("synthetic-access")
 public class TestResultNodePropertySource extends AbstractPropertySource {
     /** Constant for Category Component */
     public static final String P_TESTSTEP_CAT = 
         Messages.TestResultNodeGUIPropertySourceResultStep;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_STEPNAME = 
         Messages.TestResultNodeGUIPropertySourceStepName;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_STEPTYPE = 
         Messages.TestResultNodeGUIPropertySourceStepType;
     /** Constant for Category Component */
     public static final String P_TESTRESULT_CAT = 
         Messages.TestResultNodeGUIPropertySourceTestResult;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_TIMESTAMP = 
         Messages.TestResultNodeGUIPropertySourceTimeStamp;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_STEPSTATUS = 
         Messages.TestResultNodeGUIPropertySourceStepStatus;
     /** Constant for Category Component */
     public static final String P_TESTERROR_CAT = 
         Messages.TestResultNodeGUIPropertySourceErrorDetail;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_ERRORTYPE = 
         Messages.TestResultNodeGUIPropertySourceErrorType;        
 
     /** Constant for Category Test Data */
     public static final String P_DATA_CAT = 
         Messages.TestResultNodeGUIPropertySourceDataCat;
     
     // CAP Details
     /** Constant for Category Component */
     public static final String P_CAP_CAT = 
         Messages.TestResultNodeGUIPropertySourceCapDetail;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_CAPCOMMENT = 
         Messages.TestResultNodeGUIPropertySourceComment;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_COMPNAME = 
         Messages.CapGUIPropertySourceComponentName;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_ACTIONTYPE = 
         Messages.CapGUIPropertySourceActionType;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_COMPTYPE = 
         Messages.CapGUIPropertySourceCompType;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_PARAMETERNAME = 
         Messages.CapGUIPropertySourceParamName;
     /** Property m_text on display */
     public static final String P_ELEMENT_DISPLAY_PARAMETERVALUE = 
         Messages.CapGUIPropertySourceParamValue;
     /** the node to display the properties for */
     private TestResultNode m_node;
 
     /**
      * Constructor 
      * @param node
      *      TestResultNodeGUI
      */
     public TestResultNodePropertySource(TestResultNode node) {
         this.m_node = node;
         initPropDescriptor();
     }
 
     /**
      * Inits the PropertyDescriptors
      */
     protected void initPropDescriptor() {
         clearPropertyDescriptors();
 
         final INodePO node = m_node.getNode();        
         PropertyDescriptor propDes = new PropertyDescriptor(
             new ComponentController() {
                 public Object getProperty() {
                     final TestResultNode cap = m_node;
                     Date time = cap.getTimeStamp();
                     if (time != null) {
                         String timeStamp = time.toString();
                         return timeStamp;
                     }
                     return StringConstants.EMPTY;
                 }
             }, P_ELEMENT_DISPLAY_TIMESTAMP);
         propDes.setCategory(P_TESTSTEP_CAT);
         addPropertyDescriptor(propDes);
         propDes = new PropertyDescriptor(
             new ComponentController() {
                 public Object getProperty() {
                     return m_node.getName();
                 } 
             } , P_ELEMENT_DISPLAY_STEPNAME);
         propDes.setCategory(P_TESTSTEP_CAT);
         addPropertyDescriptor(propDes);
         propDes = new PropertyDescriptor(new ComponentController() {
             public Object getProperty() {
                 if (Persistor.isPoSubclass(
                         node, IEventExecTestCasePO.class)) {
                     return
                         Messages.TestResultNodeGUIPropertySourceEventTestCase; 
                 } else if (Persistor.isPoSubclass(node, ITestCasePO.class)) {
                     return Messages.TestResultNodeGUIPropertySourceTestCase;
                 } else if (Persistor.isPoSubclass(node, ICapPO.class)) {
                     return Messages.TestResultNodeGUIPropertySourceTestStep; 
                 } else if (Persistor.isPoSubclass(
                         node, ITestSuitePO.class)) {
                     return Messages.TestResultNodeGUIPropertySourceTestSuite; 
                 }
                 return Messages.TestResultNodeGUIPropertySourceUnknownElement; 
             }
             public Image getImage() {
                 return getImageForNode(node);
             }
             
         }, P_ELEMENT_DISPLAY_STEPTYPE);
         propDes.setCategory(P_TESTSTEP_CAT);
         addPropertyDescriptor(propDes);
         propDes = new PropertyDescriptor(
             new ComponentController() {
                 public Object getProperty() {
                     return node == null || node.getComment() == null 
                         ? StringUtils.EMPTY : node.getComment();
                 }
             } , P_ELEMENT_DISPLAY_CAPCOMMENT);
         propDes.setCategory(P_TESTSTEP_CAT);
         addPropertyDescriptor(propDes);
 
         initResultDetailsPropDesc();
         
         if (Persistor.isPoSubclass(node, IEventExecTestCasePO.class)) {
             initEventTestCasePropDescriptor(node);
 
         }
 
         if (m_node.getEvent() != null) { 
             initErrorEventPropDescriptor();
         }
 
         initComponentNameDetailsPropDescriptor(m_node);
         initActionDetailsPropDescriptor(m_node);
         initParameterDescriptor(m_node);
     }
     
     /**
      * 
      * @param testResult The Test Result for which to display the Parameters.
      */
     private void initParameterDescriptor(TestResultNode testResult) {
         for (final TestResultParameter parameter : testResult.getParameters()) {
             PropertyDescriptor propDesc = new PropertyDescriptor(
                     new ComponentController() {
                         public Object getProperty() {
                             return parameter.getValue();
                         }
                     },
                     NLS.bind(
                             Messages.TestResultNodePropertySourceDataParameter,
                             parameter.getName()));
             propDesc.setCategory(P_DATA_CAT);
             addPropertyDescriptor(propDesc);
         }
     }
 
     /**
      * retrieves the Image for NodePO
      * @param node
      *      NodePO
      * @return
      *      Image
      */
     private Image getImageForNode(final INodePO node) {
         Image image = null;
         if (Persistor.isPoSubclass(node, ITestSuitePO.class)) {
             image = IconConstants.TS_IMAGE; 
         }
         if (Persistor.isPoSubclass(node, IExecTestCasePO.class)) {
             if (Persistor.isPoSubclass(node, 
                 IEventExecTestCasePO.class)) {
                 image = IconConstants.RESULT_EH_IMAGE; 
             } else {
                 image = IconConstants.TC_IMAGE; 
             }
         } 
         if (Persistor.isPoSubclass(node, ICapPO.class)) {
             TestResultNode parent = m_node.getParent();
             if (Persistor.isPoSubclass(parent.getNode(),
                 IEventExecTestCasePO.class)) {
                 image = IconConstants.EH_CAP_IMAGE;
             } else {
                 image = IconConstants.CAP_IMAGE; 
             }
         }
         return image;
     }
 
     /**
      */
     private void initResultDetailsPropDesc() {
         PropertyDescriptor propDes = new PropertyDescriptor(
             new ComponentController() {
                 public Object getProperty() {
                     return m_node.getStatusString();
                 }
                 public Image getImage() {
                     int status = m_node.getStatus();
                     switch (status) {
                         case TestResultNode.SUCCESS:
                             return IconConstants.STEP_OK_IMAGE;
                         case TestResultNode.ERROR:
                         case TestResultNode.ERROR_IN_CHILD:
                             return IconConstants.STEP_NOT_OK_IMAGE;
                         case TestResultNode.RETRYING:
                             return IconConstants.STEP_RETRY_IMAGE;
                         case TestResultNode.SUCCESS_RETRY:
                             return IconConstants.STEP_RETRY_OK_IMAGE;
                         case TestResultNode.ABORT:
                             return IconConstants.STEP_NOT_OK_IMAGE;
                         case TestResultNode.NOT_YET_TESTED:
                         case TestResultNode.NOT_TESTED:
                         case TestResultNode.TESTING:
                         case TestResultNode.NO_VERIFY:
                         default:
                             return null;
                     }
 
                 }
             } , P_ELEMENT_DISPLAY_STEPSTATUS);
         propDes.setCategory(P_TESTRESULT_CAT);
         addPropertyDescriptor(propDes);
     }
 
     /**
      * 
      * @param testResult The Test Result for which to display the 
      *                   Component Name information.
      */
     private void initComponentNameDetailsPropDescriptor(
             TestResultNode testResult) {
 
         PropertyDescriptor propDes;
 
         final String componentName = testResult.getComponentName();
         if (componentName != null) {
             propDes = new PropertyDescriptor(
                     new ComponentController() {
                         public Object getProperty() {
                             return componentName;
                         }
                     } , P_ELEMENT_DISPLAY_COMPNAME);
             propDes.setCategory(P_CAP_CAT);
             addPropertyDescriptor(propDes);
         }
         
         final String componentType = testResult.getComponentType();
         if (componentType != null) {
             propDes = new PropertyDescriptor(
                     new ComponentController() {
                         public Object getProperty() {
                             return componentType;
                         }
                     } , P_ELEMENT_DISPLAY_COMPTYPE);
             propDes.setCategory(P_CAP_CAT);
             addPropertyDescriptor(propDes);
         }
 
     }
 
     /**
      * @param testResult The Test Result for which to display the Action 
      *                   details.
      */
     private void initActionDetailsPropDescriptor(TestResultNode testResult) {
 
         PropertyDescriptor propDes;
 
         final String actionName = testResult.getActionName();
         if (actionName != null) {
             propDes = new PropertyDescriptor(
                     new ComponentController() {
                         public Object getProperty() {
                             return actionName;
                         }
                     } , P_ELEMENT_DISPLAY_ACTIONTYPE);
             propDes.setCategory(P_CAP_CAT);
             addPropertyDescriptor(propDes);
         }
     }
 
     /**
      * 
      */
     private void initErrorEventPropDescriptor() {
         PropertyDescriptor propDes = new PropertyDescriptor(
             new ComponentController() {
                 public Object getProperty() {
                     return I18n.getString(m_node
                         .getEvent().getId());
                 }
             } , P_ELEMENT_DISPLAY_ERRORTYPE);
         propDes.setCategory(P_TESTERROR_CAT);
         addPropertyDescriptor(propDes);
         final TestErrorEvent event = m_node.getEvent();
         if (event.getId().equals(TestErrorEvent.ID.VERIFY_FAILED)) {
             Set keys = event.getProps().keySet();
             for (final Object key : keys) {
                 propDes = new PropertyDescriptor(new ComponentController() {
                     public Object getProperty() {
                         return event.getProps().get(key);
                     }
                 }, I18n.getString((String)key));
                 propDes.setCategory(P_TESTERROR_CAT);
                 addPropertyDescriptor(propDes);
             }
         } else if (event.getId().equals(
                 TestErrorEvent.ID.IMPL_CLASS_ACTION_ERROR)
             && event.getProps().keySet().contains(
                 TestErrorEvent.Property.DESCRIPTION_KEY)) {
             
             propDes = new PropertyDescriptor(new ComponentController() {
                 public Object getProperty() {
                     String key = (String)event.getProps().get(
                         TestErrorEvent.Property.DESCRIPTION_KEY);
                     Object[] args = (Object[])event.getProps().get(
                         TestErrorEvent.Property.PARAMETER_KEY);
                     if (args != null) {
                         return I18n.getString(key, args);
                     }
                     return I18n.getString(key, true);
                 }
             }, I18n.getString(TestErrorEvent.Property.DESCRIPTION_KEY));
             propDes.setCategory(P_TESTERROR_CAT);
             addPropertyDescriptor(propDes);
         }
     }
 
     /**
      * @param node
      *            NodePO
      */
     private void initEventTestCasePropDescriptor(final INodePO node) {
         
         final IEventExecTestCasePO tc = (IEventExecTestCasePO) node;
         PropertyDescriptor propDes = new PropertyDescriptor(
             new ComponentController() {
                 public Object getProperty() {
                     return I18n.getString(tc.getEventType());
                 }
             } , Messages.TestResultNodeGUIPropertySourceErrorType);
         propDes.setCategory(
                 Messages.TestResultNodeGUIPropertySourceEventhandler);
         addPropertyDescriptor(propDes);
         propDes = new PropertyDescriptor(
             new ComponentController() {
                 public Object getProperty() {
                     return tc.getReentryProp();
                 }
             } , Messages.TestResultNodeGUIPropertySourceReentry);
         propDes.setCategory(
                 Messages.TestResultNodeGUIPropertySourceEventhandler);
         addPropertyDescriptor(propDes);
     }
 
     /**
      * Class to control component name.
      *
      * @author BREDEX GmbH
      * @created 07.01.2005
      */
     private abstract class ComponentController implements IPropertyController {
         /** {@inheritDoc} */
         public boolean setProperty(Object value) {
             return true; // readonly
         }
         
         /** {@inheritDoc} */
         public abstract Object getProperty();
         
         /** {@inheritDoc} */
         public Image getImage() {
             return null;
         }
     }
 
     /** {@inheritDoc} */
     public Object getEditableValue() {
         return "noEditableValues"; //$NON-NLS-1$
     }
 
     /** {@inheritDoc} */
     public boolean isPropertySet(Object arg0) {
         // Do nothing
         return false;
     }
 
     /** {@inheritDoc} */
     public void resetPropertyValue(Object arg0) {
         // Do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     public void setPropertyValue(Object arg0, Object arg1) {
         // Do nothing
     }
 }
