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
 package org.eclipse.jubula.client.core.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderColumn;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 
 /**
  * @author BREDEX GmbH
  * @created 22.01.2010
  */
 @Entity
 @Table(name = "TESTRESULT")
 class TestResultPO implements ITestResultPO {
     /** Persistence (JPA / EclipseLink) OID */
     private transient Long m_id = null;
     
     /** Persistence (JPA / EclipseLink) version id */
     private transient Integer m_version = null;
     
     /** testresult summary id*/
     private Long m_testResultSummaryId;
     
     /** keyword type */
     private int m_internalKeywordType;
     
     /** keyword type string representation*/
     private String m_keywordType;
     
     /** keyword name */
     private String m_keywordName;
     
     /** keyword comment */
     private String m_keywordComment;
     
     /** keyword guid */
     private String m_keywordGuid;
     
     /** keyword status */
     private int m_internalKeywordStatus;
     
     /** keyword status string representation*/
     private String m_keywordStatus;
     
     /** keyword level */
     private int m_keywordLevel;
     
     /** keyword sequence */
     private int m_keywordSequence;
     
     /** timestamp */
     private Date m_timestamp;
     
     /** component name guid of cap*/
     private String m_componentNameGuid;
     
     /** component name of cap */
     private String m_componentName;
     
     /** internal component type of cap */
     private String m_internalComponentType;
     
     /** component type of cap */
     private String m_componentType;
     
     /** internal action name of cap */
     private String m_internalActionName;
     
     /** action name of cap */
     private String m_actionName;
     
     /** parameter details of cap */
     private List<IParameterDetailsPO> m_parameterList = 
         new ArrayList<IParameterDetailsPO>();
     
     /** status type */
     private String m_statusType;
     
     /** status description */
     private String m_statusDescription;
     
     /** status operator */
     private String m_statusOperator;
     
     /** expected value */
     private String m_expectedValue;
     
     /** actual value */
     private String m_actualValue;
     
     /** parent keyword id */
     private Long m_parentKeywordID;
     
     /** the image data */
     private byte[] m_imageData;
     
     /**
      * <code>m_omHeuristicEquivalence</code>
      */
     private double m_omHeuristicEquivalence = -1.0d;
     
     /**
      * <code>m_noOfSimiliarComponents</code>
      */
     private int m_noOfSimilarComponents = -1;
     
     /**
      * only for Persistence (JPA / EclipseLink)
      */
     TestResultPO() {
         //default
     }
     
     /**
      * @param parameterList List of IParameterDetailsPO
      * only for Persistence (JPA / EclipseLink)
      */
     TestResultPO(List<IParameterDetailsPO> parameterList) {
         m_parameterList = parameterList;
     }
 
     /**
      * 
      * only for Persistence (JPA / EclipseLink)
      * @return Returns the id.
      */
     @Id
     @GeneratedValue
     @Column(name = "ID")
     public Long getId()  {
         return m_id;
     }
     /**
      * only for Persistence (JPA / EclipseLink)
      * @param id The id to set.
      */
     void setId(Long id) {
         m_id = id;
     }
     
     /** 
      * 
      * {@inheritDoc}
      */
     @Version
     @Column(name = "INTERNAL_VERSION")
     public Integer getVersion() {        
         return m_version;
     }
 
     /**
      * @param version version
      */
     @SuppressWarnings("unused")
     private void setVersion(Integer version) {
         m_version = version;
     }
     
     /**
      * 
      * @return the m_testResultSummaryId
      */
     @Basic
     @Column(name = "INTERNAL_TESTRUN_ID")
     public Long getInternalTestResultSummaryID() {
         return m_testResultSummaryId;
     }
     
     /**
      * @param testResultSummaryId the testResultSummaryId to set
      */
     public void setInternalTestResultSummaryID(Long testResultSummaryId) {
         m_testResultSummaryId = testResultSummaryId;
     }
 
     /**
      * 
      * @return the internalKeywordType
      */
     @Basic
     @Column(name = "INTERNAL_KEYWORD_TYPE", nullable = false)
     public int getInternalKeywordType() {
         return m_internalKeywordType;
     }
 
     /**
      * @param internalKeywordType the internalKeywordType to set
      */
     public void setInternalKeywordType(int internalKeywordType) {
         m_internalKeywordType = internalKeywordType;
     }
     
     /**
      * 
      * @return the keywordTypeString
      */
     @Basic
     @Column(name = "KEYWORD_TYPE", length = 4000)
     public String getKeywordType() {
         return m_keywordType;
     }
 
     /**
      * @param keywordType the keywordType to set
      */
     public void setKeywordType(String keywordType) {
         m_keywordType = keywordType;
     }
 
     /**
      * 
      * @return the keywordName
      */
     @Basic
     @Column(name = "KEYWORD_NAME", length = 4000)
     public String getKeywordName() {
         return m_keywordName;
     }
 
     /**
      * @param keywordName the keywordName to set
      */
     public void setKeywordName(String keywordName) {
         m_keywordName = keywordName;
     }
     
     /**
      * 
      * @return the keywordComment
      */
     @Basic
     @Column(name = "KEYWORD_COMMENT", length = 4000)
     public String getKeywordComment() {
         return m_keywordComment;
     }
     
     /**
      * @param keywordComment the keywordComment to set
      */
     public void setKeywordComment(String keywordComment) {
         m_keywordComment = keywordComment;
     }
 
     /**
      * 
      * @return the keywordGuid
      */
     @Basic
     @Column(name = "INTERNAL_KEYWORD_GUID", length = 4000)
     public String getInternalKeywordGuid() {
         return m_keywordGuid;
     }
 
     /**
      * @param keywordGuid the keywordGuid to set
      */
     public void setInternalKeywordGuid(String keywordGuid) {
         m_keywordGuid = keywordGuid;
     }
 
     /**
      * 
      * @return the keywordStatus
      */
     @Basic
     @Column(name = "INTERNAL_KEYWORD_STATUS", nullable = false)
     public int getInternalKeywordStatus() {
         return m_internalKeywordStatus;
     }
 
     /**
      * @param keywordStatus the keywordStatus to set
      */
     public void setInternalKeywordStatus(int keywordStatus) {
         m_internalKeywordStatus = keywordStatus;
     }
     
     /**
      * 
      * @return the keywordStatus
      */
     @Basic
     @Column(name = "KEYWORD_STATUS", length = 4000)
     public String getKeywordStatus() {
         return m_keywordStatus;
     }
 
     /**
      * @param keywordStatus the keywordStatus to set
      */
     public void setKeywordStatus(String keywordStatus) {
         m_keywordStatus = keywordStatus;
     }
 
     /**
      * 
      * @return the keywordLevel
      */
     @Basic
     @Column(name = "KEYWORD_LEVEL", nullable = false)
     public int getKeywordLevel() {
         return m_keywordLevel;
     }
 
     /**
      * @param keywordLevel the keywordLevel to set
      */
     public void setKeywordLevel(int keywordLevel) {
         m_keywordLevel = keywordLevel;
     }
 
     /**
      * 
      * @return the keywordSequence
      */
     @Basic
     @Column(name = "KEYWORD_SEQUENCE", nullable = false)
     public int getKeywordSequence() {
         return m_keywordSequence;
     }
 
     /**
      * @param keywordSequence the keywordSequence to set
      */
     public void setKeywordSequence(int keywordSequence) {
         m_keywordSequence = keywordSequence;
     }
 
     /**
      * 
      * @return the timestamp
      */
     @Basic
     @Column(name = "TIMESTAMP")
     @Temporal(value = TemporalType.TIMESTAMP)
     public Date getTimestamp() {
         return m_timestamp;
     }
 
     /**
      * @param timestamp the timestamp to set
      */
     public void setTimestamp(Date timestamp) {
         m_timestamp = timestamp;
     }
 
     /**
      * 
      * @return the componentNameGuid
      */
     @Basic
     @Column(name = "INTERNAL_COMPONENT_NAME", length = 4000)
     public String getInternalComponentNameGuid() {
         return m_componentNameGuid;
     }
 
     /**
      * @param componentNameGuid the componentNameGuid to set
      */
     public void setInternalComponentNameGuid(String componentNameGuid) {
         m_componentNameGuid = componentNameGuid;
     }
 
     /**
      * 
      * @return the componentName
      */
     @Basic
     @Column(name = "COMPONENT_NAME", length = 4000)
     public String getComponentName() {
         return m_componentName;
     }
 
     /**
      * @param componentName the componentName to set
      */
     public void setComponentName(String componentName) {
         m_componentName = componentName;
     }
 
     /**
      * 
      * @return the internalComponentType
      */
     @Basic
     @Column(name = "INTERNAL_COMPONENT_TYPE", length = 4000)
     public String getInternalComponentType() {
         return m_internalComponentType;
     }
 
     /**
      * @param internalComponentType the internalComponentType to set
      */
     public void setInternalComponentType(String internalComponentType) {
         m_internalComponentType = internalComponentType;
     }
     
     /**
      * 
      * @return the componentType
      */
     @Basic
     @Column(name = "COMPONENT_TYPE", length = 4000)
     public String getComponentType() {
         return m_componentType;
     }
 
     /**
      * @param componentType the componentType to set
      */
     public void setComponentType(String componentType) {
         m_componentType = componentType;
     }
 
     /**
      * 
      * @return the internalActionName
      */
     @Basic
     @Column(name = "INTERNAL_ACTION_NAME", length = 4000)
     public String getInternalActionName() {
         return m_internalActionName;
     }
 
     /**
      * @param internalActionName the internalActionName to set
      */
     public void setInternalActionName(String internalActionName) {
         m_internalActionName = internalActionName;
     }
     
     /**
      * 
      * @return the actionName
      */
     @Basic
     @Column(name = "ACTION_NAME", length = 4000)
     public String getActionName() {
         return m_actionName;
     }
 
     /**
      * @param actionName the actionName to set
      */
     public void setActionName(String actionName) {
         m_actionName = actionName;
     }
     
     
     /**
      * adds a parameter to the parameter list
      * @param parameter IParameterDetailsPO
      */
     public void addParameter(IParameterDetailsPO parameter) {
         getHbmParameterList().add(parameter);
     }
     
     /**
      * 
      * Access method for the m_nodeList property.
      * only to use for Persistence (JPA / EclipseLink)
      * 
      * @return the current value of the m_parameterList property
      */
     @OneToMany(cascade = CascadeType.ALL, 
                 targetEntity = ParameterDetailsPO.class, 
                 fetch = FetchType.LAZY)
     @OrderColumn(name = "IDX")
     @JoinColumn(name = "FK_TESTRESULT")
     List<IParameterDetailsPO> getHbmParameterList() {
         return m_parameterList;
     }
     
     /**
      * @param parameterList The parameterList to set.
      */
     void setHbmParameterList(List<IParameterDetailsPO> parameterList) {
         m_parameterList = parameterList;
     }
     
     /**
      * @return the unmodifiable node list.
      */
     @Transient
     public List<IParameterDetailsPO> getUnmodifiableParameterList() {
         return Collections.unmodifiableList(getHbmParameterList());
     }
     
     
     /**
      * 
      * @return the statusType
      */
     @Basic
     @Column(name = "STATUS_TYPE", length = 4000)
     public String getStatusType() {
         return m_statusType;
     }
 
     /**
      * @param statusType the statusType to set
      */
     public void setStatusType(String statusType) {
         m_statusType = statusType;
     }
 
     /**
      * 
      * @return the statusDescription
      */
     @Basic
     @Column(name = "STATUS_DESCRIPTION", length = 4000)
     public String getStatusDescription() {
         return m_statusDescription;
     }
 
     /**
      * @param statusDescription the statusDescription to set
      */
     public void setStatusDescription(String statusDescription) {
         m_statusDescription = statusDescription;
     }
 
     /**
      * 
      * @return the statusOperator
      */
     @Basic
     @Column(name = "STATUS_OPERATOR", length = 4000)
     public String getStatusOperator() {
         return m_statusOperator;
     }
 
     /**
      * @param statusOperator the statusOperator to set
      */
     public void setStatusOperator(String statusOperator) {
         m_statusOperator = statusOperator;
     }
 
     /**
      * 
      * @return the expectedValue
      */
     @Basic
     @Column(name = "STATUS_EXPECTED_VALUE", length = 4000)
     public String getExpectedValue() {
         return m_expectedValue;
     }
 
     /**
      * @param expectedValue the expectedValue to set
      */
     public void setExpectedValue(String expectedValue) {
         m_expectedValue = expectedValue;
     }
 
     /**
      * 
      * @return the actualValue
      */
     @Basic
     @Column(name = "STATUS_ACTUAL_VALUE", length = 4000)
     public String getActualValue() {
         return m_actualValue;
     }
 
     /**
      * @param actualValue the actualValue to set
      */
     public void setActualValue(String actualValue) {
         m_actualValue = actualValue;
     }
 
     /**
      * 
      * @return the parentKeywordID
      */
     @Basic
     @Column(name = "INTERNAL_PARENT_KEYWORD_ID")
     public Long getInternalParentKeywordID() {
         return m_parentKeywordID;
     }
     
     /**
      * @param parentKeywordID the parentKeywordID to set
      */
     public void setInternalParentKeywordID(Long parentKeywordID) {
         m_parentKeywordID = parentKeywordID;
     }
 
     /**
      * @param image the image to set
      */
     public void setImage(byte[] image) {
         m_imageData = image;
     }
 
     /**
      * @return the image
      */
     @Basic
     @Lob
     @Column(name = "SCREENSHOT")
     public byte[] getImage() {
         return m_imageData;
     }
 
 
     /**
      * @param omHeuristicEquivalence the omHeuristicEquivalence to set
      */
     public void setOmHeuristicEquivalence(double omHeuristicEquivalence) {
         m_omHeuristicEquivalence = omHeuristicEquivalence;
     }
 
     /**
      * @return the omHeuristicEquivalence
      */
     @Basic
     @Column(name = "OM_HEURISTIC_EQUIVALENCE", 
             nullable = false, 
             precision = 10, 
             scale = 5)
     public double getOmHeuristicEquivalence() {
         return m_omHeuristicEquivalence;
     }
 
     /**
      * @param noOfSimilarComponents the noOfSimilarComponents to set
      */
     public void setNoOfSimilarComponents(int noOfSimilarComponents) {
         m_noOfSimilarComponents = noOfSimilarComponents;
     }
 
     /**
      * @return the noOfSimilarComponents
      */
     @Basic
     @Column(name = "OM_NO_SIMILAR_COMPONENTS", nullable = false)
     public int getNoOfSimilarComponents() {
         return m_noOfSimilarComponents;
     }
 }
