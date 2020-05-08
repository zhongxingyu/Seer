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
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OrderColumn;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.objects.ComponentIdentifier;
 
 /**
  * @author BREDEX GmbH
  * @created 20.06.2005
  */
 @Entity
 @Table(name = "COMP_ID")
 class CompIdentifierPO extends ComponentIdentifier implements
     ICompIdentifierPO {
     
     
     /** hibernate OID */
     private transient Long m_id = null;
 
     /** hibernate version id */
     private transient Integer m_version;
     
     /** The ID of the parent project */
     private Long m_parentProjectId = null;
 
     /**
      * only for hibernate
      */
     CompIdentifierPO() {
         super();
     }
     
     /**
      * 
      * @return Returns the neighbours.
      */
     @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
     @CollectionTable(name = "NEIGHBOURS")
     @Column(name = "NEIGHBOUR", length = 4000)
     @OrderColumn(name = "IDX")
     @JoinColumn(name = "FK_COMP_ID")
     public List getNeighbours() { // NOPMD by al on 3/19/07 1:26 PM
         return super.getNeighbours();
     }
 
     /**
      * @param neighbours The neighbours to set.
      */
     public void setNeighbours(List neighbours) { // NOPMD by al on 3/19/07 1:26 PM
         super.setNeighbours(neighbours);
     }
 
     /**
      *  
      * @return Returns the componentClassName.
      */
     @Basic
     @Column(name = "COMP_CLASS_NAME", length = 4000)
     public String getComponentClassName() { // NOPMD by al on 3/19/07 1:26 PM
         return super.getComponentClassName();
     }
     
     /**
      * @param componentClassName The componentClassName to set.
      */
     public void setComponentClassName(String componentClassName) { // NOPMD by al on 3/19/07 1:26 PM
         super.setComponentClassName(componentClassName);
     }
     
     /**
      * 
      * @return Returns the supportedClassName.
      */
     @Basic
     @Column(name = "SUPP_CLASS_NAME", length = 4000)
     public String getSupportedClassName() { // NOPMD by al on 3/19/07 1:26 PM
         return super.getSupportedClassName();
     }
     
     /**
      * @param supportedClassName The supportedClassName to set.
      */
     public void setSupportedClassName(String supportedClassName) { // NOPMD by al on 3/19/07 1:26 PM
         super.setSupportedClassName(supportedClassName);
     }
     
     /**
      * 
      * @return Returns the hierarchyNames.
      */
    @ElementCollection(fetch = FetchType.EAGER)
     @CollectionTable(name = "HIERARCHY_NAMES")
     @Column(name = "HIERARCHY_NAME", length = 4000)
     @OrderColumn(name = "IDX")
     @JoinColumn(name = "FK_COMP_ID")
    public List<String> getHierarchyNames() { // NOPMD by al on 3/19/07 1:26 PM
         return super.getHierarchyNames();
     }
     
     /**
      * @param hierarchyNames
      *            The hierarchyNames to set. if null, the list will be cleared.
      */
     public void setHierarchyNames(List hierarchyNames) { // NOPMD by al on 3/19/07 1:26 PM
         super.setHierarchyNames(hierarchyNames);
     }
     
     /**
      * @return Clone of object
      */
     public ICompIdentifierPO makePoClone() {
         ICompIdentifierPO clone = new CompIdentifierPO();
         clone.setHierarchyNames(new ArrayList(
             getHierarchyNames()));
         clone.setComponentClassName(getComponentClassName());
         if (getComponentClassName() != null) {
             clone.setSupportedClassName(getComponentClassName());
         }
         if (getNeighbours() != null) {
             clone.setNeighbours(new ArrayList(getNeighbours()));
         }
         if (getParentProjectId() != null) {
             clone.setParentProjectId(getParentProjectId());
         }
         clone.setAlternativeDisplayName(getAlternativeDisplayName());
         return clone;
     }
     
     /**
      * 
      * @return Returns the id.
      */
     @Id
     @GeneratedValue
     public Long getId() {
         return m_id;
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public void setId(Long id) {
         m_id = id;
     }
     
     /**
      *    
      * {@inheritDoc}
      */
     @Transient
     public Long getParentProjectId() {
         return getHbmParentProjectId();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setParentProjectId(Long projectId) {
         setHbmParentProjectId(projectId);
     }
 
     /**
      *    
      * {@inheritDoc}
      */
     @Basic
     @Column(name = "PARENT_PROJ")
     Long getHbmParentProjectId() {
         return m_parentProjectId;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     void setHbmParentProjectId(Long projectId) {
         m_parentProjectId = projectId;
     }
 
     /** 
      * 
      * {@inheritDoc}
      */
     @Version
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
      * {@inheritDoc}
      * @return empty string
      */
     @Transient
     public String getName() {
         return StringConstants.EMPTY;
     }
 
     /**
      *    
      * {@inheritDoc}
      */
     @Basic
    @Column(name = "ALTERNATIVE_DISPLAY_NAME", length = 4000)
     public String getAlternativeDisplayName() {
         return super.getAlternativeDisplayName();
     }
 
     /**
      * {@inheritDoc}
      */
     public void setAlternativeDisplayName(String alternativeDisplayName) {
         super.setAlternativeDisplayName(alternativeDisplayName);
     }
 
 }
