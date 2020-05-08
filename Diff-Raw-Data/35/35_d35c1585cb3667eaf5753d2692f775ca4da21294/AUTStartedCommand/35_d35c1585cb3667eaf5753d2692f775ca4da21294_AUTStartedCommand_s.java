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
 package org.eclipse.jubula.client.core.commands;
 
 import java.util.Iterator;
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.client.core.AUTEvent;
 import org.eclipse.jubula.client.core.ClientTestFactory;
 import org.eclipse.jubula.client.core.IAUTInfoListener;
 import org.eclipse.jubula.client.core.businessprocess.ObjectMappingEventDispatcher;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.IObjectMappingCategoryPO;
 import org.eclipse.jubula.client.core.model.IObjectMappingPO;
 import org.eclipse.jubula.communication.ICommand;
 import org.eclipse.jubula.communication.message.AUTStartStateMessage;
 import org.eclipse.jubula.communication.message.AUTStateMessage;
 import org.eclipse.jubula.communication.message.Message;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.jubula.tools.xml.businessmodell.Component;
 import org.eclipse.jubula.tools.xml.businessmodell.ConcreteComponent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * The command object for <code>AUTComponentsMessage</code>. <br>
  * 
  * Execute() notifies the listener which was given at construction time.<br>
  * Timeout() notifies the same listener with error(ERROR_TIMEOUT)<br>.
  *
  * @author BREDEX GmbH
  * @created 05.10.2004
  * 
  */
 public class AUTStartedCommand implements ICommand {
     
     /** the logger */
     private static Logger log = 
         LoggerFactory.getLogger(AUTStartedCommand.class);
 
     /** the listener to notify */
     private IAUTInfoListener m_listener;
     
     /** the message */
     private AUTStartStateMessage m_message;
 
     // HERE for the exhibition
     /** the state of the AUT */
     private AUTStateMessage m_stateMessage;
     
     /** flag that is set at the end of execution */
     private boolean m_wasExecuted = false;
     
     /**
      * Used by communication framework
      * 
      * @deprecated
      */
     public AUTStartedCommand() {
         // Nothing to initialize
     }
 
     /**
      * public constructor
      * 
      * @param listener
      *            the listener to callback, must not be null
      * @throws IllegalArgumentException
      *             if <code>listener</code> is null
      */
     public AUTStartedCommand(IAUTInfoListener listener)
         throws IllegalArgumentException {
         super();
 
         Validate.notNull(listener, "listener must not be null"); //$NON-NLS-1$
         
         m_listener = listener;
     }
 
     // HERE for the exhibition START
     /**
      * @param stateMessage The stateMessage to set.
      */
     public void setStateMessage(AUTStateMessage stateMessage) {
         m_stateMessage = stateMessage;
     }
     
     /**
      * analyse the state of the message and fire appropriate events.
      */
     private void fireAutStateChanged() {
         int state = m_stateMessage.getState();
         switch (state) { 
             case AUTStateMessage.RUNNING:
                 log.info(Messages.AUTIsRunning);
                 ClientTestFactory.getClientTest().fireAUTStateChanged(
                     new AUTEvent(AUTEvent.AUT_STARTED));
                 break;
             case AUTStateMessage.START_FAILED:
                 log.error(Messages.AUTCouldNotStarted + StringConstants.COLON
                     + StringConstants.SPACE + m_stateMessage.getDescription());
                 ClientTestFactory.getClientTest().fireAUTStateChanged(
                     new AUTEvent(AUTEvent.AUT_START_FAILED));
                 break;
             default:
                 // nothing here
         }
     }
     
     // HERE for the exhibition END
     
     /**
      * {@inheritDoc}
      */
     public Message getMessage() {
         return m_message;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setMessage(Message message) {
         m_message = (AUTStartStateMessage)message;
     }
 
     /**
      * Determines the logical name of the passed component. To get a valid
      * logical name, the component has to be a <code>ConcreteComponent</code>
      * with a default mapping. If not, the method returns <code>null</code>.
      * The name of the default mapping is localized. If there is no I18N value,
      * <code>DefaultMapping.getLogicalName()</code> is returned.
      * 
      * {@inheritDoc}
      * 
      * @param comp
      *            The component
      * @return The logical name or <code>null</code>
      */
     private String getLogicalName(Component comp) {
         String logicalName = null;
         if (comp.isConcrete()) {
             ConcreteComponent cc = (ConcreteComponent)comp;
             if (cc.hasDefaultMapping()) {
                 String logicalNameKey = cc.getDefaultMapping().getLogicalName();
                 String name = CompSystemI18n.getString(logicalNameKey);
                 logicalName = name != null ? name : logicalNameKey;
             }
         }
         return logicalName;
     }
     /**
      * notify the listener <br>
      * always return null (no response to send) <br>
      * 
      * {@inheritDoc}
      */
     public Message execute() {
         // The component identifiers returned by the message contain
         // information about all components with a default mapping.
         // So, their technical and logical names are added to the
         // object mapping. This happens whenever the AUT is being started.
         ObjectMappingEventDispatcher.clearObjMapTransient();
         IObjectMappingPO transientObjMap = ObjectMappingEventDispatcher.
             getObjMapTransient();
         IObjectMappingCategoryPO oldCategory = 
             ObjectMappingEventDispatcher.getCategoryToCreateIn();
         // sets Category
         ObjectMappingEventDispatcher.setCategoryToCreateIn(
                 transientObjMap.getMappedCategory());
         for (Iterator it = m_message.getCompIds().iterator(); it.hasNext();) {
             
             IComponentIdentifier id = (IComponentIdentifier)it.next();
             Component comp = ComponentBuilder.getInstance().getCompSystem()
                 .findComponent(id.getComponentClassName());
            String logicalName = getLogicalName(comp);
            if (logicalName != null) {
                // adds new mapping
                transientObjMap.addObjectMappingAssoziation(
                    logicalName, id);
            } else {
                if (log.isErrorEnabled()) {
                    log.error(Messages.NoLogicalNameForDefaultMapping
                        + StringConstants.SPACE
                        + Messages.DefinedByComponent + StringConstants.COLON
                        + StringConstants.SPACE + comp);
                 }
             }
         }
         ObjectMappingEventDispatcher.setCategoryToCreateIn(oldCategory);
 
         // HERE for the exhibition
         // do this after the OM was build
         fireAutStateChanged();
         m_wasExecuted = true;
         return null;
     }
 
     /**
      * 
      * @return <code>true</code> if the command has been succesfully executed.
      *         Otherwise, <code>false</code>.
      */
     public boolean wasExecuted() {
         return m_wasExecuted;
     }
     
     /**
      * {@inheritDoc}
      */
     public void timeout() {
         log.warn(this.getClass().getName() + ".timeout() called"); //$NON-NLS-1$
         m_listener.error(IAUTInfoListener.ERROR_TIMEOUT);
     }
 }
