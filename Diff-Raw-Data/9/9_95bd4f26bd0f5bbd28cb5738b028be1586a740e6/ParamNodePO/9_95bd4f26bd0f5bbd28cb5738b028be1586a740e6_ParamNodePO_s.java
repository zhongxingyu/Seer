 
 /*******************************************************************************
  * Copyright (c) 2005, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Locale;
 
 import javax.persistence.CascadeType;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.client.core.utils.ModelParamValueConverter;
 import org.eclipse.jubula.client.core.utils.ParamValueConverter;
 
 
 
 /**
  * base class for all nodes with parameters
  * 
  *
  * @created 06.06.2005
  */
 @Entity
 @DiscriminatorValue("A")
 abstract class ParamNodePO extends NodePO implements IParamNodePO {
     
     /** 
      * delegate for managing the parameter interface and test data for 
      * this node 
      */
     private TestDataCubePO m_parameterInterface;
     
     /**
      * @param name name of node
      * @param isGenerated indicates whether this node has been generated
      */
     public ParamNodePO(String name, boolean isGenerated) {
         super(name, isGenerated);
         setParameterInterface(new TestDataCubePO(null));
     }
 
     /**
      * @param name name of node
      * @param guid The GUID of the param node.
      * @param isGenerated indicates whether this node has been generated
      */
     public ParamNodePO(String name, String guid, boolean isGenerated) {
         super(name, guid, isGenerated);
         setParameterInterface(new TestDataCubePO(null));
     }
 
     /**
      * only for hibernate
      */
     ParamNodePO() {
        // only for hibernate
     }
     
     /**
      * 
      * @return the object responsible for maintaining the receiver's parameter
      *         interface as well as test data.
      */
     @ManyToOne(cascade = CascadeType.ALL, 
                targetEntity = TestDataCubePO.class, 
                fetch = FetchType.EAGER)
     @JoinColumn(name = "FK_PARAM_INTERFACE", unique = true)
     private TestDataCubePO getParameterInterface() {
         return m_parameterInterface;
     }
 
     /**
      * 
      * @param parameterInterface The new parameter interface for the receiver.
      */
     private void setParameterInterface(TestDataCubePO parameterInterface) {
         m_parameterInterface = parameterInterface;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public boolean getCompleteTdFlag(Locale loc) {
         return getParameterInterface().getCompleteTdFlag(loc);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public String getDataFile() {
         return getParameterInterface().getDataFile();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public ITDManager getDataManager() {
         return getParameterInterface().getDataManager();
     }
 
     /**
      * 
      * @return the data manager.
      */
     @Transient
     protected ITDManager getHbmDataManager() {
         return getParameterInterface().getHbmDataManager();
     }
 
     /**
      * Clears the parameter list.
      */
     protected final void clearParameterList() {
         getParameterInterface().clearParameterList();
     }
 
     /**
      * Add a parameter description to the list of descriptions
      * @param p <code>ParamDescriptionPO</code> to be added
      */
     protected void addParameter(IParamDescriptionPO p) {
         getParameterInterface().addParameter(p);
     }
     
     /**
      * 
      * @return parameters instance
      */
     @Transient
     protected List<IParamDescriptionPO> getHbmParameterList() {
         return getParameterInterface().getHbmParameterList();
     }
     
     /**
      * Remove a parameter description from the list of descriptions. This
      * is a method used by derived classes to work with the list.
      * @param p <code>ParamDescriptionPO</code> to be removed
      */
     protected void removeParameter(IParamDescriptionPO p) {
         getParameterInterface().removeParameter(p);
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public IParamDescriptionPO getParameterForName(String paramName) {
         return getParameterInterface().getParameterForName(paramName);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public IParamDescriptionPO getParameterForUniqueId(String uniqueId) {
         return getParameterInterface().getParameterForUniqueId(uniqueId);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public List<IParamDescriptionPO> getParameterList() {
         return getParameterInterface().getParameterList();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public ListIterator<IParamDescriptionPO> getParameterListIter() {
         return getParameterInterface().getParameterListIter();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public int getParameterListSize() {
         return getParameterInterface().getParameterListSize();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public List<String> getParamNames() {
         return getParameterInterface().getParamNames();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Transient
     public IParameterInterfacePO getReferencedDataCube() {
         return getParameterInterface().getReferencedDataCube();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void resetCompleteTdFlag() {
         getParameterInterface().resetCompleteTdFlag();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setCompleteTdFlag(Locale loc, boolean flag) {
         getParameterInterface().setCompleteTdFlag(loc, flag);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setDataFile(String pathToExternalDataFile) {
         getParameterInterface().setDataFile(pathToExternalDataFile);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setDataManager(ITDManager dataManager) {
         getParameterInterface().setDataManager(dataManager);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setReferencedDataCube(IParameterInterfacePO dataCube) {
         getParameterInterface().setReferencedDataCube(dataCube);
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public boolean isTestDataComplete(Locale locale) {
         
         if (StringUtils.isEmpty(getDataFile())) {
             // Excel files are ignored. Other data is checked.
             final int paramListSize = getParameterListSize();
             ITDManager testDataManager = getDataManager();
             if ((testDataManager.getDataSetCount() == 0) 
                     && (paramListSize > 0)) {
                 return false;
             }
 
             if (getParameterListSize() > testDataManager.getColumnCount()) {
                 return false;
             }
 
             List<IParamDescriptionPO> requiredParameters = 
                 new ArrayList<IParamDescriptionPO>(getParameterList());
 
             IParameterInterfacePO refDataCube = getReferencedDataCube();
            for (IDataSetPO dataSet : testDataManager.getDataSets()) {
                 for (IParamDescriptionPO paramDesc : requiredParameters) {
                     int column = 
                         testDataManager.findColumnForParam(
                                 paramDesc.getUniqueId());
                     
                     if (refDataCube != null) {
                         IParamDescriptionPO dataCubeParam = 
                             refDataCube.getParameterForName(
                                 paramDesc.getName());
                         if (dataCubeParam != null) {
                             column = testDataManager.findColumnForParam(
                                     dataCubeParam.getUniqueId());
                         }
                     }
                    
                     if (column == -1) {
                         return false;
                     }
                     
                    ITestDataPO testData = dataSet.getColumn(column);
                     if (testData == null || testData.getValue(locale) == null) {
                         return false;
                     }
                 }
             }
                 
         }
 
         return true;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public Iterator<TDCell> getParamReferencesIterator(Locale locale) {
         List <TDCell> references = new ArrayList <TDCell> ();
         int row = 0;
         for (IDataSetPO dataSet : getDataManager().getDataSets()) {
             addParamReferences(references, dataSet, row, locale);
             row++;
         }
         return references.iterator();
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public Iterator<TDCell> getParamReferencesIterator(
             int dataSetRow, Locale locale) {
         
         IDataSetPO row = getDataManager().getDataSet(dataSetRow);
         List <TDCell> references = new ArrayList <TDCell> ();
         addParamReferences(references, row, dataSetRow, locale);
         return references.iterator();
     }
 
     /**
      * @param references
      *            The references
      * @param row
      *            The row representation
      * @param dataSetRow
      *            The row index
      * @param locale 
      *            currently used locale
      */
     private void addParamReferences(List <TDCell> references, 
             IDataSetPO row, int dataSetRow, Locale locale) {
         int col = 0;
         for (ITestDataPO testData : row.getList()) {
             String uniqueId = getDataManager().getUniqueIds().get(col);
             IParamDescriptionPO desc = getParameterForUniqueId(
                     uniqueId);
             ParamValueConverter conv = new ModelParamValueConverter(
                     testData, this, locale, desc);
             if (conv.containsReferences()) {
                 references.add(new TDCell(testData, dataSetRow, col));
             }
             col++;
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void setParentProjectId(Long projectId) {
         super.setParentProjectId(projectId);
         if (getParameterInterface() != null) {
             getParameterInterface().setParentProjectId(projectId);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Transient
     public INodePO getSpecificationUser() {
         return getParentNode();
     }
     
     /**
      * {@inheritDoc}
      */
     public void clearTestData() {
         if (!hasReferencedTestData()) {
             getDataManager().clear();
         }
         resetCompleteTdFlag();
     }
     
     /**
      * 
      * @return <code>true</code> if the receiver references Test Data (for 
      *         example, by referencing a Central Test Data instance), rather 
      *         than having Test Data of its own.
      */
     public boolean hasReferencedTestData() {
         return getReferencedDataCube() != null;
     }
 }
