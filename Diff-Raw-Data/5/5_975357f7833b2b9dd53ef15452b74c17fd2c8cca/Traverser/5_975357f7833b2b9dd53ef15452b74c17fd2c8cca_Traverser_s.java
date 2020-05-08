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
 package org.eclipse.jubula.client.core.utils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Stack;
 import java.util.Vector;
 
 import org.apache.commons.collections.IteratorUtils;
 import org.apache.commons.collections.list.UnmodifiableList;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.eclipse.jubula.client.core.businessprocess.ExternalTestDataBP;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution.PauseMode;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.IDataSetPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IEventStackModificationListener;
 import org.eclipse.jubula.client.core.model.IExecStackModificationListener;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParamNodePO;
 import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
 import org.eclipse.jubula.client.core.model.ITDManager;
 import org.eclipse.jubula.client.core.model.ITestDataPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.ReentryProperty;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.exception.IncompleteDataException;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created 11.04.2005
  */
 public class Traverser {  
     
     /**
      * Represents the exact location of a test step within the execution
      * hierarchy. 
      *
      * @author BREDEX GmbH
      * @created 06.03.2008
      */
     private static final class ExecStackMarker {
 
         /** the execution stack at the time the marker was created */
         private Vector<ExecObject> m_execStack;
     
         /** the test step represented by the marker */
         private ICapPO m_step;
         
         /**
          * Constructor
          * 
          * @param execStack The current execution stack.
          * @param step The current test step.
          */
         ExecStackMarker(Stack<ExecObject> execStack, ICapPO step) {
             m_execStack = new Vector<ExecObject>(execStack);
             m_step = step;
         }
 
         /**
          * 
          * @return the execution stack at the time this marker was created.
          */
         @SuppressWarnings("unchecked")
         List<ExecObject> getExecStack() {
             return UnmodifiableList.decorate(m_execStack);
         }
 
         /**
          * 
          * @return the test step represented by this marker.
          */
         ICapPO getStep() {
             return m_step;
         }
         
         /**
          * 
          * {@inheritDoc}
          */
         public boolean equals(Object obj) {
             if (obj instanceof ExecStackMarker) {
                 ExecStackMarker marker = (ExecStackMarker)obj;
                 return new EqualsBuilder()
                     .append(m_execStack, marker.getExecStack())
                     .append(m_step, marker.getStep()).isEquals();
             }
             
             return super.equals(obj);
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         public int hashCode() {
             return new HashCodeBuilder().append(m_execStack).append(m_step)
                 .toHashCode();
         }
     }
 
     /**
      * <code>NO_DATASET</code> to use for node operations without
      * consideration of datasets
      */
     public static final int NO_DATASET = -1;
 
     /**
      * <code>NO_INDEX</code> placeholder for index to use, if childrenlist of
      * a node isn't yet proceeded
      */
     public static final int NO_INDEX = -1;
 
     /** The logger */
     private static final Logger LOG = LoggerFactory.getLogger(Traverser.class);
     
     /**
      * <code>m_root</code> root node
      */
     private INodePO m_root;
     
     /**
      * <code>m_execStack</code> stack for executed objects
      */
     private Stack<ExecObject> m_execStack = new Stack<ExecObject>();
     
     /**
      * list of execution stack listeners
      */
     private List<IExecStackModificationListener> m_execListenerList = 
         new ArrayList<IExecStackModificationListener>();
     
     /**
      * list of event stack listeners
      */
     private List<IEventStackModificationListener> m_eventListenerList = 
         new ArrayList<IEventStackModificationListener>();
 
     /**
      * <code>m_eventStack</code> stack for actual executed eventHandler
      * stackobjects are from type EventObject
      */
     private Stack<EventObject> m_eventStack = new Stack<EventObject>();
 
     /**
      * locale for testexecution
      */
     private Locale m_locale = null;
 
     /** 
      * mapping from positions in the execution hierarchy to the number of times
      * the test step at that position has been retried
      */
     private Map<ExecStackMarker, Integer> m_markerToNumRetriesMap =
         new HashMap<ExecStackMarker, Integer>();
     
     /** business process for retrieving test data */
     private ExternalTestDataBP m_externalTestDataBP;
     
     /**
      * @param root root node from tree
      * @param locale language for testexecution
      */
     public Traverser(INodePO root, Locale locale) {
         m_root = root;
         m_locale  = locale;
         m_externalTestDataBP = new ExternalTestDataBP();
         m_execStack.push(new ExecObject(root, NO_INDEX, 0));
         executeLogging();
     }
 
     /**
      * @return the next Cap, regarding to the actual position in tree
      * @throws JBException in case of missing testdata
      */
     @SuppressWarnings("unchecked")
     public ICapPO next() throws JBException {
         if (!m_execStack.isEmpty()) {
             ExecObject stackObj = m_execStack.peek();
             INodePO node = stackObj.getExecNode();
             // next index
             ITDManager tdManager = null;
             if (Persistor.isPoSubclass(node, IParamNodePO.class)) {
                 tdManager = 
                     m_externalTestDataBP.getExternalCheckedTDManager(
                             (IParamNodePO)node);
             }
             if (stackObj.getIndex() < node.getNodeListSize() - 1) {
                 stackObj.incrementIndex();
                 List<INodePO> nodeList = 
                     IteratorUtils.toList(node.getNodeListIterator());
                 INodePO childNode = nodeList.get(stackObj.getIndex());
                 if (!childNode.isActive()) {
                     return next();
                 }
                 if (Persistor.isPoSubclass(childNode, ICapPO.class)) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug(Messages.ActualExecutedCap 
                             + StringConstants.COLON + StringConstants.SPACE 
                             + childNode.getName());
                     }
                     fireNextCap((ICapPO)childNode);
                     return (ICapPO)childNode;
                 }
                 if (Persistor.isPoSubclass(
                         childNode, IExecTestCasePO.class)) {
                     if (((IExecTestCasePO)childNode).getSpecTestCase() 
                             == null) {
                         throw new IncompleteDataException(
                             Messages.ExecTestCasePOMissingReference,
                             MessageIDs.E_DATASOURCE_CONTAIN_EMPTY_DATA);
                     }
                     processExecTestCase(stackObj, (IExecTestCasePO)childNode);
                     return next();
                 }
                 Assert.notReached(Messages.ErrorInTestExecutionTree);
                 return null;
                 // next dataset
             } else if (!(Persistor.isPoSubclass(stackObj.getExecNode(),
                 ITestSuitePO.class)) && tdManager != null) {
                 int maxDsNumber = tdManager.getDataSetCount();
                if (stackObj.getNumberDs() < maxDsNumber - 1) {
                     stackObj.incrementDataSetNumber();
                     stackObj.setIndex(NO_INDEX);
                     fireNextDataSetIteration();
                     return next();
                 }
                 ReentryProperty prop = decrementStack(node);
                 return (prop == null) ? next() : next(prop);
             } else {
                 ReentryProperty prop = decrementStack(node);
                 return (prop == null) ? next() : next(prop);
             }
         } 
         // end
         return null;
     }
 
     /**
      * processes a childnode which is an ExecTestCasePO
      * @param stackObj the stack object
      * @param childNode the child node
      * @throws JBException an exception.
      */
     private void processExecTestCase(ExecObject stackObj,
         IExecTestCasePO childNode) 
         throws JBException {
         
         IExecTestCasePO exTc = childNode;
         ITDManager tdManager = null;
         tdManager = m_externalTestDataBP.getExternalCheckedTDManager(exTc);
         
         ITestDataPO td = null;
         IDataSetPO dataSet = null;
         
         if (tdManager.getDataSetCount() > 0) {
             dataSet = tdManager.getDataSet(0);
         }
         if (dataSet != null && dataSet.getColumnCount() > 0) {
             td = dataSet.getColumn(0);
         }
         // own data sets - > always begin at 0
         if (tdManager.getDataSetCount() > 1) {
             m_execStack.push(new ExecObject(childNode, NO_INDEX, 
                 getFirstDataSetNumber(childNode)));
         } else if (tdManager.getDataSetCount() == 1) {
             // exact one dataset with a reference -> 
             // begin at data set index of parent
             String uniqueId = tdManager.getUniqueIds().get(0);
             IParamDescriptionPO desc = exTc.getParameterForUniqueId(uniqueId);
             ParamValueConverter conv = new ModelParamValueConverter(
                 td, exTc, m_locale, desc);
             if (td != null && conv.containsReferences()) {
                 m_execStack.push(new ExecObject(childNode, NO_INDEX,
                     stackObj.getNumberDs()));
             // exact one dataset with a value or no value in runIncomplete-mode
             // always use dataset number 0
             } else if (td != null) {
                 m_execStack.push(new ExecObject(childNode, NO_INDEX, 0));
             }
         // no data sets or fixed value
         } else {
             m_execStack.push(new ExecObject(childNode, NO_INDEX, NO_DATASET));
         }
         executeLogging();
         fireExecStackIncremented(childNode);
     }
 
     /**
      * @param node node to pop from stack
      * @return the associated reentryProperty in case of eventExecTestCaseNode 
      * or null in case of execTestCaseNode
      */
     private ReentryProperty decrementStack(INodePO node) {
         ReentryProperty prop = null;
         if (isEventHandler(node)) {
             IEventExecTestCasePO eventExec = (IEventExecTestCasePO)node; 
             prop = eventExec.getReentryProp();
         }
         if (!m_execStack.isEmpty()) {
             m_execStack.pop();
             executeLogging();
             fireExecStackDecremented();
         }
         return prop;
     }
 
     /**
      * write information to log
      */
     private void executeLogging() {
         if (LOG.isDebugEnabled() && !m_execStack.isEmpty()) {
             LOG.debug(Messages.ActualPeekObjectOnStack + StringConstants.COLON
                 + StringConstants.SPACE 
                 + m_execStack.peek().getExecNode().getName());
         }
     }
 
     /**
      * @param node for which the number of dataset to find
      * @return number of datasets for given node
      * @throws JBException in case of missing testdata
      * hint: each execTestCase with a dataManager has parameter(s) and must
      * have at least one dataset
      */
     private int getFirstDataSetNumber(INodePO node) 
         throws JBException {
         int firstDs = NO_DATASET;
 
         if (Persistor.isPoSubclass(node, IParamNodePO.class) 
                 && ((IParamNodePO)node).getDataManager() != null) {
             IParamNodePO paramNode = (IParamNodePO)node;
             ITDManager tdManager = 
                 m_externalTestDataBP.getExternalCheckedTDManager(paramNode);
             int ds = tdManager.getDataSetCount();
             if (ds > 0) {
                 firstDs = 0;
             }         
         }
         return firstDs;
     }
 
     /**
      * @return Returns the execStack as unmodifiable List
      */
     public List <ExecObject> getExecStackAsList() {
         return Collections.unmodifiableList(new ArrayList<ExecObject>(
                 m_execStack));
     }
     /**
      * @return The nodes of the current execution stack in a list
      */
     public List<INodePO> getExecStackAsNodeList() {
         List<INodePO> nodes = new ArrayList<INodePO>(m_execStack.size());
         
         for (Iterator<ExecObject> it = m_execStack.iterator(); it.hasNext();) {
             ExecObject execObject = it.next();
             nodes.add(execObject.getExecNode());
         }
         return nodes;
     }
     /**
      * @return Returns the root.
      */
     public INodePO getRoot() {
         return m_root;
     }
     
     /**
      * register listener for stackModificationListener
      * @param listener listener to register
      */
     public void addExecStackModificationListener(
         IExecStackModificationListener listener) {
         
         if (!m_execListenerList.contains(listener)) {
             m_execListenerList.add(listener);
         }        
     }
     
     /**
      * remove listener from stackModificationListenerList
      * @param listener listener to remove
      */
     public void removeExecStackModificationListener(
         IExecStackModificationListener listener) {
         m_execListenerList.remove(listener);    
     }
     
     /**
      * register listener for stackModificationListener
      * @param listener listener to register
      */
     public void addEventStackModificationListener(
         IEventStackModificationListener listener) {
         
         if (!m_eventListenerList.contains(listener)) {
             m_eventListenerList.add(listener);
         }        
     }
     
     /**
      * remove listener from stackModificationListenerList
      * @param listener listener to remove
      */
     public void removeEventStackModificationListener(
             IEventStackModificationListener listener) {
         m_eventListenerList.remove(listener);    
     }
 
     /**
      *  event for push-operation on execStack
      *  @param node node, which was added to stack
      */
     private void fireExecStackIncremented(INodePO node) {
         addParameters(m_execStack.peek());
         
         Iterator<IExecStackModificationListener> it = 
             m_execListenerList.iterator();
         while (it.hasNext()) {
             IExecStackModificationListener l = it.next();
             try {
                 l.stackIncremented(node);
             } catch (Throwable t) {
                 LOG.error(Messages.ErrorWhileNotifyingListeners, t);
             }
         }
     }
     
     /**
      *  event for pop-operation on execStack
      */
     private void fireExecStackDecremented() {
         Iterator<IExecStackModificationListener> it = 
             m_execListenerList.iterator();
         while (it.hasNext()) {
             IExecStackModificationListener l = it.next();
             try {
                 l.stackDecremented();
             } catch (Throwable t) {
                 LOG.error(Messages.ErrorWhileNotifyingListeners, t);
             }
         }
     }
     
     /**
      *  event for push-operation on execStack
      */
     private void fireEventStackIncremented() {
         Iterator<IEventStackModificationListener> it = 
             m_eventListenerList.iterator();
         while (it.hasNext()) {
             IEventStackModificationListener l = it.next();
             try {
                 l.eventStackIncremented();
             } catch (Throwable t) {
                 LOG.error(Messages.ErrorWhileNotifyingListeners, t);
             }
         }
     }
     
     /**
      *  event for pop-operation on execStack
      */
     private void fireEventStackDecremented() {
         Iterator<IEventStackModificationListener> it = 
             m_eventListenerList.iterator();
         while (it.hasNext()) {
             IEventStackModificationListener l = it.next();
             try {
                 l.eventStackDecremented();
             } catch (Throwable t) {
                 LOG.error(Messages.ErrorWhileNotifyingListeners, t);
             }
         }
     }
 
     /**
      *  event for next dataset iteration
      */
     private void fireNextDataSetIteration() {
         addParameters(m_execStack.peek());
         
         Iterator<IExecStackModificationListener> it = 
             m_execListenerList.iterator();
         while (it.hasNext()) {
             IExecStackModificationListener l = it.next();
             try {
                 l.nextDataSetIteration();
             } catch (Throwable t) {
                 LOG.error(Messages.ErrorWhileNotifyingListeners, t);
             }
         }
     }
     
     /**
      * event for proceeding of next cap
      * 
      * @param cap
      *            actual proceeded cap
      */
     private void fireNextCap(ICapPO cap) {
         Iterator<IExecStackModificationListener> it = 
             m_execListenerList.iterator();
         while (it.hasNext()) {
             IExecStackModificationListener l = it.next();
             try {
                 l.nextCap(cap);
             } catch (Throwable t) {
                 LOG.error(Messages.ErrorWhileNotifyingListeners, t);
             }
         }
     }
     
     /**
      * @return Returns the dataSetNumber.
      */
     public int getDataSetNumber() {
         int ds = 0;
         if (!m_execStack.isEmpty()) {
             ExecObject obj = m_execStack.peek();
             ds = obj.getNumberDs();
         }
         return ds;
     }
     
     /**
      * @param reentryProp reentryProperty
      * @return next Cap to execute
      * @throws JBException in case of incomplete testdata
      */
     private ICapPO next(ReentryProperty reentryProp) 
         throws JBException {
         if (LOG.isDebugEnabled()) {
             LOG.debug("ReentryProperty: " + String.valueOf(reentryProp)); //$NON-NLS-1$
         }
         if (!m_execStack.isEmpty()) {
             if (reentryProp.equals(ReentryProperty.CONTINUE)) {
                 popEventStack();
             } else if (reentryProp.equals(ReentryProperty.REPEAT)) {
                 ExecObject obj = m_execStack.peek();
                 obj.decrementIndex();
                 popEventStack();
             } else if (reentryProp.equals(ReentryProperty.BREAK)) {
                 for (int i = m_eventStack.size(); i > 0 
                     && !m_execStack.isEmpty(); i--) {
                     m_execStack.pop();
                     executeLogging();
                     fireExecStackDecremented();
                 }
 
                 popEventStackNested();
 
             } else if (reentryProp.equals(ReentryProperty.RETURN)) {
                 int stackPos = popEventStackNested();
                 
                 while (m_execStack.size() > stackPos) {
                     m_execStack.pop();
                     executeLogging();
                     fireExecStackDecremented();
                 }
                 
             } else if (reentryProp.equals(ReentryProperty.EXIT)) {
                 for (int i = m_execStack.size() - 1; i >= 0; i--) {
                     m_execStack.pop();
                     executeLogging();
                     fireExecStackDecremented();
                 }
                 for (int i = m_eventStack.size() - 1; i >= 0; i--) {
                     popEventStack();
                 }
             } else if (reentryProp.equals(ReentryProperty.STOP)) {
                 TestExecution.getInstance().pauseExecution(PauseMode.PAUSE);
                 popEventStack();
             } else if (reentryProp.equals(ReentryProperty.RETRY)) {
                 popEventStack();
                 return retryStep();
                 
             // default is EXIT
             } else {
                 for (int i = m_execStack.size() - 1; i >= 0; i--) {
                     m_execStack.pop();
                     executeLogging();
                     fireExecStackDecremented();
                 }
                 for (int i = m_eventStack.size() - 1; i >= 0; i--) {
                     popEventStack();
                 }
             }
             return next();
         }
         // end of testexecution
         return null;           
     }
 
     /**
      * Repeatedly pops the event stack until either the event stack is empty
      * or the top event on the stack has a lower execution stack position than
      * the top event on the stack at the time this method was called.
      * 
      * @return the execution stack position of the event object at the top of 
      *         the event stack at the time this method is called.
      */
     private int popEventStackNested() {
         int stackPos = 0;
         if (!m_eventStack.isEmpty()) {
             stackPos = m_eventStack.peek().getStackPos();
         }
         
         popEventStack();
 
         // Remove all events that occured higher on the execution stack
         // than the currently processed event
         while (!m_eventStack.isEmpty() 
                 && m_eventStack.peek().getStackPos() >= stackPos) {
             popEventStack();
         }
 
         return stackPos;
     }
     
     /**
      * Calls pop() on the event stack and fires a corresponding event.
      */
     private void popEventStack() {
         m_eventStack.pop();
         fireEventStackDecremented();
     }
 
     /**
      * @return the cap to be retried, or <code>null</code> if the cap cannot be
      *         determined. A return value of <code>null</code> indicates an
      *         error.
      */
     @SuppressWarnings("unchecked")
     private ICapPO retryStep() {
         ExecObject execObj = m_execStack.peek();
 
         // inform listeners that we are retrying the step
         List<INodePO> nodeList = 
             IteratorUtils.toList(execObj.getExecNode().getNodeListIterator());
         INodePO childNode = nodeList.get(execObj.getIndex());
 
         if (childNode instanceof ICapPO) {
             ICapPO cap = (ICapPO)childNode;
             ExecStackMarker marker = new ExecStackMarker(m_execStack, cap);
             if (m_markerToNumRetriesMap.containsKey(marker)) {
                 // increment number of retries
                 m_markerToNumRetriesMap.put(
                         marker, m_markerToNumRetriesMap.get(marker) + 1);
             } else {
                 m_markerToNumRetriesMap.put(marker, 1);
             }
             fireRetryStep(cap);
             return cap;
         }
         
         return null;
     }
 
     /**
      * event for retrying a test step
      * 
      * @param toRetry The step to retry.
      */
     private void fireRetryStep(ICapPO toRetry) {
         Iterator<IExecStackModificationListener> it = 
             m_execListenerList.iterator();
         while (it.hasNext()) {
             IExecStackModificationListener l = it.next();
             try {
                 l.retryCap(toRetry);
             } catch (Throwable t) {
                 LOG.error(Messages.ErrorWhileNotifyingListeners, t);
             }
         }
     }
 
     /**
      * determines, if node of type EventExecTestCase
      * @param node node to validate
      * @return result of validation
      */
     private boolean isEventHandler(INodePO node) {
         return (Persistor.isPoSubclass(node, IEventExecTestCasePO.class));
     }
 
     /**
      * 
      * @param eventType The event type to be handled.
      * @return the reentry property of the event handler for the given
      *         event type.
      */
     public ReentryProperty getEventHandlerReentry(String eventType) {
         return getEventObject(eventType, false)
             .getEventExecTc().getReentryProp();
     }
     
     /**
      * @param eventType eventType of eventhandler, which is to execute in next
      * step
      * @return next cap to execute
      * @throws JBException in case of incomplete testdata
      */
     public ICapPO next(String eventType) 
         throws JBException {
         ExecObject execObj = m_execStack.peek();
         EventObject eventObj = getEventObject(eventType, true);
         IEventExecTestCasePO eventExecTC = eventObj.getEventExecTc();
         
         int startIndex = 0;
         final ITDManager mgr = eventExecTC.getDataManager();
         if (mgr.getDataSetCount() > 0) {
             IDataSetPO row = mgr.getDataSet(0);
             for (int col = 0; col < row.getColumnCount(); col++) {
                 ITestDataPO td = row.getColumn(col);
                 String uniqueId = mgr.getUniqueIds().get(col);
                 IParamDescriptionPO desc = 
                     eventExecTC.getParameterForUniqueId(uniqueId);
                 // if EH uses params of parent, start at the iteration which failed!
                 ParamValueConverter conv = new ModelParamValueConverter(
                     td, eventExecTC, m_locale, desc);
                 if (conv.containsReferences()) {
                     startIndex = execObj.getNumberDs();
                     break;
                 }
             }
         }
      
         m_execStack.push(new ExecObject(eventExecTC, NO_INDEX, startIndex));
         m_eventStack.push(eventObj);
         fireEventStackIncremented();
         fireExecStackIncremented(eventExecTC);
         executeLogging();
         return next();
     }
     
     /**
      * Tells whether an error is currently being handled. This is the case if 
      * any event handler for the test case is currently on the event stack.
      * 
      * @return <code>true</code> if an error is currently being handled. 
      *         Otherwise <code>false</code>.
      */
     public boolean isHandlingError() {
         return !m_eventStack.isEmpty();
     }
     
     /**
      * find the next eventHandler for given eventType
      * 
      * @param eventType
      *            eventType for eventHandler to find
      * @param resetRetryCount
      *            if this is set to <code>true</code> the retry count of the
      *            EventHandler will be reset, if the EventHandler was
      *            unsuccessful and has reached his max retry count.
      *            This is necessary for http://bugs.eclipse.org/347275
      * @return the next eventHandler for given eventType
      */
     @SuppressWarnings("unchecked")
     private EventObject getEventObject(String eventType,
             boolean resetRetryCount) {
         
         List<INodePO> nodeList = IteratorUtils.toList(
             m_execStack.peek().getExecNode().getNodeListIterator());
         ICapPO cap = (ICapPO)nodeList.get(m_execStack.peek().getIndex());
         ExecStackMarker marker = new ExecStackMarker(m_execStack, cap);
 
         EventObject eventObj = null;
         int startIndex = m_execStack.size() - 1;
         for (int i = startIndex; i > 0 && i < m_execStack.size(); --i) {
             ExecObject obj = m_execStack.get(i);
             IExecTestCasePO execTc = (IExecTestCasePO)obj.getExecNode();
             
             IEventExecTestCasePO eventExecTc = execTc.getEventExecTC(eventType);
             if (!isHandlingError(i) && eventExecTc !=  null) {
                 if (!(eventExecTc.getReentryProp().equals(ReentryProperty.RETRY)
                         && m_markerToNumRetriesMap.containsKey(marker) 
                         && m_markerToNumRetriesMap.get(marker)
                             >= eventExecTc.getMaxRetries())) {
                     
                     eventObj = new EventObject(eventExecTc, i);
                     break;
                 }
                 eventExecTc = null;
             }           
         }
         // nothing found --> call defaultEventHandler
         if (eventObj == null) {
             // reset the marker of the EventHandler
             if (m_markerToNumRetriesMap.containsKey(marker)
                     && resetRetryCount) {
                 m_markerToNumRetriesMap.put(marker, 0);
             }
             IEventExecTestCasePO eventExecTc = 
                 DefaultEventHandler.getDefaultEventHandler(eventType, m_root);
             Validate.notNull(eventExecTc, 
                 Messages.MissingDefaultEventHandlerForEventType + eventType 
                 + StringConstants.DOT);
             eventObj = new EventObject(eventExecTc, 0);
         }
         return eventObj;      
     }
 
     /**
      * Tells whether the test case at the given index in the execution stack
      * is currently handling an error. A test case is defined as "currently
      * handling an error" if any event handler for the test case is currently
      * on the execution stack.
      * 
      * @param execStackIndex The index at which the test case to check can be
      *                       found in the execution stack.
      * @return <code>true</code> if the test case at the specified index in the
      *         execution stack is currently handling an error. Otherwise 
      *         <code>false</code>.
      */
     private boolean isHandlingError(int execStackIndex) {
         for (EventObject event : m_eventStack) {
             if (event.getStackPos() == execStackIndex) {
                 return true;
             }
         }
      
         return false;
     }
 
     /**
      * 
      * class to manage information about executed eventHandler
      * @author BREDEX GmbH
      * @created 29.04.2005
      */
     private static class EventObject {        
         /** <code>m_eventExecTc</code> managed eventExecTestCase */
         private IEventExecTestCasePO m_eventExecTc;
         /** <code>m_stackPos</code> place of discovery of eventExecTestCase in execStack */
         private int m_stackPos;
 
         /**
          * @param eventExecTc managed eventExecTestCase
          * @param stackPos place of discovery of eventExecTestCase in execStack
          */
         private EventObject(IEventExecTestCasePO eventExecTc, int stackPos) {
             m_eventExecTc = eventExecTc;
             m_stackPos = stackPos;
         }
         /**
          * @return Returns the eventExecTc.
          */
         public IEventExecTestCasePO getEventExecTc() {
             return m_eventExecTc;
         }
         /**
          * @return Returns the stackPos.
          */
         public int getStackPos() {
             return m_stackPos;
         }
     }
     
     /**
      * 
      * @return the result value to use when the most recently executed cap was
      *         successful. This may depend on previous events within the test,
      *         such as whether the step has been retried.
      */
     public int getSuccessResult() {
         INodePO currentNode = getCurrentNode();
         if (currentNode instanceof ICapPO) {
             ExecStackMarker marker = 
                 new ExecStackMarker(m_execStack, (ICapPO)currentNode);
             if (m_markerToNumRetriesMap.containsKey(marker)) {
                 m_markerToNumRetriesMap.put(marker, 0);
                 return TestResultNode.SUCCESS_RETRY;
             }
         }
         return TestResultNode.SUCCESS;
     }
 
 
     
     /**
      * 
      * @return the currently executing node.
      */
     @SuppressWarnings("unchecked")
     private INodePO getCurrentNode() {
         List<INodePO> nodeList = IteratorUtils.toList(
                 m_execStack.peek().getExecNode().getNodeListIterator());
         return nodeList.get(m_execStack.peek().getIndex());
 
     }
 
     /**
      * Adds parameter values to the given execution object. If 
      * <code>execObject</code> already has parameters assigned, this method
      * may overwrite them. 
      * 
      * @param execObject The execution object to which the parameters will
      *                   be added.
      */
     private void addParameters(ExecObject execObject) {
         INodePO execNode = execObject.getExecNode();
         if (execNode instanceof IParamNodePO) {
             IParamNodePO paramNode = (IParamNodePO)execNode;
             List<IParamDescriptionPO> parameterList = 
                     paramNode.getParameterList();
             String value = null;
             for (IParamDescriptionPO desc : parameterList) {
                 String descriptionId = desc.getUniqueId();
                 ITDManager tdManager = null;
                 try {
                     tdManager = 
                         m_externalTestDataBP.getExternalCheckedTDManager(
                                 paramNode);
                 } catch (JBException e) {
                     LOG.error(
                         Messages.TestDataNotAvailable + StringConstants.DOT, e);
                 }
                 TestExecution te = TestExecution.getInstance();
 
                 List <ExecObject> stackList = 
                     new ArrayList<ExecObject>(getExecStackAsList());
                 int dataSetIndex = getDataSetNumber();
 
                 // Special handling for Test Case References that are repeated 
                 // via Data Set. The test data manager for such References only has 
                 // information about a single Data Set, so we need to ignore the 
                 // actual current Data Set number.
                 if (tdManager.getDataSetCount() <= 1) {
                     dataSetIndex = 0;
                 }
                 
                 // Special handling for Test Steps. Their test data manager has 
                 // information about multiple Data Sets, but we are only interested 
                 // in the first one.
                 if (paramNode instanceof ICapPO) {
                     dataSetIndex = 0;
                 }
 
                 if (tdManager.findColumnForParam(desc.getUniqueId()) == -1) {
                     IParameterInterfacePO referencedDataCube = paramNode
                             .getReferencedDataCube();
                     if (referencedDataCube != null) {
                         desc = referencedDataCube.getParameterForName(desc
                                 .getName());
                     }
                 }
                 ITestDataPO date = tdManager.getCell(dataSetIndex, desc);
                 ParamValueConverter conv = new ModelParamValueConverter(
                         date.getValue(te.getLocale()), paramNode, 
                         te.getLocale(), desc);
                 try {
                     value = conv.getExecutionString(
                             stackList, te.getLocale());
                 } catch (InvalidDataException e) {
                     LOG.info(e.getMessage());
                     value = MessageIDs.getMessageObject(e.getErrorId()).
                         getMessage(new String[] {e.getLocalizedMessage()});
                 }
 
                 // It's important to use 'descriptionId' here instead of 
                 // 'desc.getUniqueId()', as 'desc' may have changed between
                 // its definition and here.
                 execObject.addParameter(descriptionId, 
                         StringUtils.defaultString(value));
             }
         }
     }
 }
