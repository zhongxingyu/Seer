 /**
  * ******************************************************************************************
  * Copyright (c) 2013 Food and Agriculture Organization of the United Nations
  * (FAO) and the Lesotho Land Administration Authority (LAA). All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this
  * list of conditions and the following disclaimer. 2. Redistributions in binary
  * form must reproduce the above copyright notice,this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3. Neither the names of FAO, the LAA nor the names of
  * its contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
  * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 /**
  *
  * LAA Addition thoriso
  */
 package org.sola.clients.beans.administrative;
 
 import org.sola.webservices.transferobjects.EntityAction;
 import java.util.Date;
 import org.sola.clients.beans.AbstractTransactionedBean;
 import org.sola.clients.beans.cache.CacheManager;
 import org.sola.clients.beans.converters.TypeConverters;
 import org.sola.services.boundary.wsclients.WSManager;
 import org.sola.webservices.transferobjects.administrative.DisputeTO;
 import org.sola.clients.beans.referencedata.DisputeCategoryBean;
 import org.sola.clients.beans.referencedata.DisputeTypeBean;
 import org.sola.clients.beans.cadastre.CadastreObjectBean;
 import org.sola.clients.beans.source.SourceBean;
 import org.sola.clients.beans.controls.SolaList;
 import org.jdesktop.observablecollections.ObservableList;
 import org.sola.clients.beans.administrative.validation.DisputeCheck;
 
 /**
  *
  * LAA Additions thoriso
  */
 @DisputeCheck
 public class DisputeBean extends AbstractTransactionedBean {
 
     public static final String DISP_ID_PROPERTY = "id";
     public static final String NR_PROPERTY = "nr";
     public static final String LODGEMENT_DATE_PROPERTY = "lodgementDate";
     public static final String COMPLETION_DATE_PROPERTY = "completiondate";
     public static final String DISPUTE_CATEGORY_PROPERTY = "disputeCategory";
     public static final String DISPUTE_CATEGORY_CODE_PROPERTY = "disputeCategoryCode";
     public static final String DISPUTE_TYPE_PROPERTY = "disputeType";
     public static final String DISPUTE_TYPE_CODE_PROPERTY = "disputeTypeCode";
     public static final String STATUS_CODE_PROPERTY = "statusCode";
     public static final String LEASE_NUMBER_PROPERTY = "leaseNumber";
     public static final String PLOT_LOCATION_PROPERTY = "plotLocation";
     public static final String PLOT_NUMBER_PROPERTY = "plotNumber";
     public static final String CASE_TYPE_PROPERTY = "caseType";
     public static final String DISPUTE_DESCRIPTION_PROPERTY = "disputeDescription";
     public static final String PRIMARY_RESPONDENT_PROPERTY = "primaryRespondent";
     public static final String ACTION_REQUIRED_PROPERTY = "actionRequired";
     public static final String SELECTED_CATEGORY_PROPERTY = "selectedCategory";
     public static final String SELECTED_TYPE_PROPERTY = "selectedType";
     public static final String USER_ID_PROPERTY = "userid";
     public static final String BEAN_PROPERTY = "bean";
     public static final String SELECTED_COMMENTS_PROPERTY = "selectedComments";
     public static final String SELECTED_PARTY_PROPERTY = "selectedParty";
     public static final String SELECTED_SOURCE_PROPERTY = "selectedSource";
     private String id;
     private String nr;
     private Date lodgementDate;
     private Date completionDate;
     private DisputeTypeBean disputeType;
     private DisputeCategoryBean disputeCategoryCode;
     //private String disputeCategoryCode;
     private String statusCode;
     private String leaseNumber;
     private String plotLocation;
     private String plotNumber;
     private String disputeDescription;
     //@NotNull(message = ClientMessage.CHECK_NOTNULL_MORTGAGEAMOUNT, payload = Localized.class)
     private String caseType;
     private boolean primaryRespondent;
     private String actionRequired;
     private String userId;
     private SolaList<DisputesCommentsBean> disputeCommentsList;
     private SolaList<DisputePartyBean> disputePartyList;
     private SolaList<SourceBean> sourceList;
     private transient DisputesCommentsBean selectedComment;
     private transient DisputePartyBean selectedParty;
    private transient SourceBean selectedSource;
 
     public DisputeBean() {
         super();
         disputeCommentsList = new SolaList<DisputesCommentsBean>();
         disputePartyList = new SolaList<DisputePartyBean>();
         sourceList = new SolaList();
 
     }
 
     public void clean() {
         this.setPlotNumber(null);
         this.setLodgementDate(null);
         this.setCompletiondate(null);
         this.setDisputeType(null);
         this.setStatusCode(null);
         this.setNr(null);
         this.setLeaseNumber(null);
         this.setPlotLocation(null);
         this.setId(null);
         this.setDisputeCommentsList(null);
         this.setDisputePartyList(null);
         //this.setDisputeSourceList(null);
         this.setDisputeDescription(null);
         this.setDisputeCategory(new DisputeCategoryBean());
     }
 
     public SolaList<DisputesCommentsBean> getDisputeCommentsList() {
         return disputeCommentsList;
     }
 
     public ObservableList<DisputesCommentsBean> getFilteredDisputeCommentsList() {
         return disputeCommentsList.getFilteredList();
     }
 
     public void setDisputeCommentsList(SolaList<DisputesCommentsBean> disputeCommentsList) {
         this.disputeCommentsList = disputeCommentsList;
     }
 
     public void setSelectedComment(DisputesCommentsBean selectedComment) {
         this.selectedComment = selectedComment;
         propertySupport.firePropertyChange(SELECTED_COMMENTS_PROPERTY, null, this.selectedComment);
     }
 
     public DisputesCommentsBean getSelectedComment() {
         return selectedComment;
     }
 
     public void addDisputeComment(DisputesCommentsBean disputeComment) {
         if (!this.updateListItem(disputeComment, disputeCommentsList, false)) {
             int i = 0;
             // Search by dispute number
             i = 0;
             for (DisputesCommentsBean bean : disputeCommentsList.getFilteredList()) {
                 if (bean.getDisputeNr() != null && disputeComment.getDisputeNr() != null
                         && bean.getDisputeNr().equals(disputeComment.getDisputeNr())) {
                     disputeCommentsList.getFilteredList().add(i + 1, disputeComment);
                     return;
                 }
                 i += 1;
             }
 
             // If comment is new
             disputeCommentsList.add(disputeComment);
         }
     }
 
     public void removeSelectedComment() {
         if (selectedComment != null && disputeCommentsList != null) {
             disputeCommentsList.safeRemove(selectedComment, EntityAction.DISASSOCIATE);
         }
     }
 
     public SolaList<DisputePartyBean> getDisputePartyList() {
         return disputePartyList;
     }
 
     public ObservableList<DisputePartyBean> getFilteredDisputePartyList() {
         if (disputePartyList == null) {
             disputePartyList = new SolaList();
         }
         return disputePartyList.getFilteredList();
     }
 
     public void setDisputePartyList(SolaList<DisputePartyBean> disputePartyList) {
         this.disputePartyList = disputePartyList;
     }
 
     public void setSelectedParty(DisputePartyBean selectedParty) {
         this.selectedParty = selectedParty;
         propertySupport.firePropertyChange(SELECTED_PARTY_PROPERTY, null, this.selectedParty);
     }
 
     public DisputePartyBean getSelectedParty() {
         return selectedParty;
     }
 
     public void addDisputeParty(DisputePartyBean disputeParty) {
         if (!this.updateListItem(disputeParty, disputePartyList, false)) {
             int i = 0;
             // Search by dispute number
             i = 0;
             for (DisputePartyBean bean : disputePartyList.getFilteredList()) {
                 if (bean.getDisputeNr() != null && disputeParty.getDisputeNr() != null
                         && bean.getDisputeNr().equals(disputeParty.getDisputeNr())) {
                     disputePartyList.getFilteredList().add(i + 1, disputeParty);
                     return;
                 }
                 i += 1;
             }
 
             // If Party is new
             disputePartyList.add(disputeParty);
         }
     }
 
     public void removeSelectedParty() {
         if (selectedParty != null && disputePartyList != null) {
             disputePartyList.safeRemove(selectedParty, EntityAction.DISASSOCIATE);
         }
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String value) {
         String old = id;
         id = value;
         propertySupport.firePropertyChange(DISP_ID_PROPERTY, old, id);
 
     }
 
     public String getPlotNumber() {
         return plotNumber;
     }
 
     public void setPlotNumber(String value) {
         String old = plotNumber;
         plotNumber = value;
         propertySupport.firePropertyChange(PLOT_NUMBER_PROPERTY, old, plotNumber);
 
     }
 
     public Date getCompletiondate() {
         return completionDate;
     }
 
     public void setCompletiondate(Date value) {
         Date old = completionDate;
         completionDate = value;
         propertySupport.firePropertyChange(COMPLETION_DATE_PROPERTY, old, completionDate);
     }
 
     public String getDisputeCategoryCode() {
         if (disputeCategoryCode != null) {
             return disputeCategoryCode.getCode();
         } else {
             return null;
         }
     }
 
     public void setDisputeCategoryCode(String disputeCategoryCode) {
         String oldValue = null;
         if (this.disputeCategoryCode != null) {
             oldValue = this.disputeCategoryCode.getCode();
         }
 
         setDisputeCategory(CacheManager.getBeanByCode(CacheManager.getDisputeCategory(), disputeCategoryCode));
         propertySupport.firePropertyChange(DISPUTE_CATEGORY_CODE_PROPERTY, oldValue, disputeCategoryCode);
     }
 
     public DisputeCategoryBean getDisputeCategory() {
         if (this.disputeCategoryCode == null) {
             this.disputeCategoryCode = new DisputeCategoryBean();
         }
         return disputeCategoryCode;
     }
 
     public void setDisputeCategory(DisputeCategoryBean disputeCategory) {
         this.disputeCategoryCode = disputeCategory;
         propertySupport.firePropertyChange(DISPUTE_CATEGORY_PROPERTY, null, this.disputeCategoryCode);
     }
 
     public String getUserId() {
         return userId;
     }
 
     public void setUserId(String userId) {
         String oldValue = this.userId;
         this.userId = userId;
         propertySupport.firePropertyChange(USER_ID_PROPERTY, oldValue, this.userId);
     }
 
     public String getDisputeTypeCode() {
         if (disputeType != null) {
             return disputeType.getCode();
         } else {
             return null;
         }
     }
 
     public void setDisputeTypeCode(String disputeTypeCode) {
         String oldValue = null;
         if (disputeType != null) {
             oldValue = this.disputeType.getCode();
         }
 
         setDisputeType(CacheManager.getBeanByCode(CacheManager.getDisputeType(), disputeTypeCode));
         propertySupport.firePropertyChange(DISPUTE_TYPE_CODE_PROPERTY, oldValue, disputeTypeCode);
     }
 
     public DisputeTypeBean getDisputeType() {
         if (this.disputeType == null) {
             this.disputeType = new DisputeTypeBean();
         }
         return disputeType;
     }
 
     public void setDisputeType(DisputeTypeBean disputeType) {
         this.disputeType = disputeType;
         propertySupport.firePropertyChange(DISPUTE_TYPE_PROPERTY, null, this.disputeType);
     }
 
     public Date getLodgementDate() {
         return lodgementDate;
     }
 
     public void setLodgementDate(Date value) {
         Date old = lodgementDate;
         lodgementDate = value;
         propertySupport.firePropertyChange(LODGEMENT_DATE_PROPERTY, old, lodgementDate);
     }
 
     public String getNr() {
         return nr;
     }
 
     public void setNr(String value) {
         String old = nr;
         nr = value;
         propertySupport.firePropertyChange(NR_PROPERTY, old, nr);
     }
 
     public String getCaseType() {
         return caseType;
     }
 
     public void setCaseType(String value) {
         String old = caseType;
         caseType = value;
         propertySupport.firePropertyChange(CASE_TYPE_PROPERTY, old, caseType);
     }
 
     public String getActionRequired() {
         return actionRequired;
     }
 
     public void setActionRequired(String value) {
         String old = actionRequired;
         actionRequired = value;
         propertySupport.firePropertyChange(ACTION_REQUIRED_PROPERTY, old, actionRequired);
     }
 
     public boolean isPrimaryRespondent() {
         return primaryRespondent;
     }
 
     public void setPrimaryRespondent(boolean value) {
         boolean old = primaryRespondent;
         primaryRespondent = value;
         propertySupport.firePropertyChange(PRIMARY_RESPONDENT_PROPERTY, old, primaryRespondent);
     }
 
     public String getDisputeDescription() {
         return disputeDescription;
     }
 
     public void setDisputeDescription(String value) {
         String old = disputeDescription;
         disputeDescription = value;
         propertySupport.firePropertyChange(DISPUTE_DESCRIPTION_PROPERTY, old, disputeDescription);
     }
 
     public String getPlotLocation() {
         return plotLocation;
     }
 
     public void setPlotLocation(String value) {
         String old = plotLocation;
         plotLocation = value;
         propertySupport.firePropertyChange(PLOT_LOCATION_PROPERTY, old, plotLocation);
     }
 
     public String getLeaseNumber() {
         return leaseNumber;
     }
 
     public void setLeaseNumber(String value) {
         String old = leaseNumber;
         leaseNumber = value;
         propertySupport.firePropertyChange(LEASE_NUMBER_PROPERTY, old, leaseNumber);
     }
 
     public String getStatusCode() {
         return statusCode;
     }
 
     public void setStatusCode(String value) {
         String old = statusCode;
         statusCode = value;
         propertySupport.firePropertyChange(STATUS_CODE_PROPERTY, old, statusCode);
     }
 
     public SolaList<SourceBean> getSourceList() {
         return sourceList;
     }
 
     public ObservableList<SourceBean> getFilteredSourceList() {
         return sourceList.getFilteredList();
     }
 
     public void setSourceList(SolaList<SourceBean> sourceList) {
         this.sourceList = sourceList;
     }
 
     public void addDisputeSource(SourceBean sourceBean) {
         if (!this.updateListItem(sourceBean, sourceList, false)) {
             int i = 0;
             // Search by id number
             i = 0;
             for (SourceBean bean : sourceList.getFilteredList()) {
                 if (bean.getId() != null && sourceBean.getId() != null
                         && bean.getId().equals(sourceBean.getId())) {
                     sourceList.getFilteredList().add(i + 1, sourceBean);
                     return;
                 }
                 i += 1;
             }
 
             // If source is new
             sourceList.add(sourceBean);
         }
     }
 
     public void addChosenPlot(CadastreObjectBean cadastreObjectBean) {
         if (cadastreObjectBean != null) {
             setPlotNumber(cadastreObjectBean.getNameFirstpart() + "-" + cadastreObjectBean.getNameLastpart());
         }
     }
 
     public void assignDispute(String id) {
         if (id != null) {
             DisputeTO dispute = WSManager.getInstance().getAdministrative().getDispute(id);
             TypeConverters.TransferObjectToBean(dispute, DisputeBean.class, this);
         }
     }
 
     public boolean createDispute() {
         DisputeTO dispute = TypeConverters.BeanToTrasferObject(this, DisputeTO.class);
         dispute = WSManager.getInstance().getAdministrative().createDispute(dispute);
         TypeConverters.TransferObjectToBean(dispute, DisputeBean.class, this);
         return true;
     }
 
     public boolean saveDispute() {
         DisputeTO dispute = TypeConverters.BeanToTrasferObject(this, DisputeTO.class);
         dispute = WSManager.getInstance().getAdministrative().saveDispute(dispute);
         TypeConverters.TransferObjectToBean(dispute, DisputeBean.class, this);
         return true;
     }
 
     /**
      * Returns Dispute by ID.
      *
      * @param disputeId The ID of Dispute to return.
      */
     public static DisputeBean getDisputeById(String disputeId) {
         return TypeConverters.TransferObjectToBean(
                 WSManager.getInstance().getAdministrative().getDisputeById(disputeId),
                 DisputeBean.class, null);
     }
 
     /**
      * Returns Dispute by NR.
      *
      * @param disputeNr The Nr of Dispute to return.
      */
     public static DisputeBean getDisputeByNr(String disputeNr) {
         return TypeConverters.TransferObjectToBean(
                 WSManager.getInstance().getAdministrative().getDisputeByNr(disputeNr),
                 DisputeBean.class, null);
     }
 
     /**
      * Returns Dispute by User.
      *
      * @param disputeUser The User of Dispute to return.
      */
     public static DisputeBean getDisputeByUser(String user) {
         return TypeConverters.TransferObjectToBean(
                 WSManager.getInstance().getAdministrative().getDisputeByUser(user),
                 DisputeBean.class, null);
     }
 
     /**
      * Returns client bean, related to lodged user.
      */
     public static DisputeBean getDispute(String disputeId) {
         if (disputeId == null || disputeId.length() < 1) {
             return null;
         }
         DisputeTO disputeTO = WSManager.getInstance().getAdministrative().getDispute(disputeId);
         return TypeConverters.TransferObjectToBean(disputeTO, DisputeBean.class, null);
     }
 }
