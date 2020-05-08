 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations
  * (FAO). All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,this
  * list of conditions and the following disclaimer. 2. Redistributions in binary
  * form must reproduce the above copyright notice,this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3. Neither the name of FAO nor the names of its
  * contributors may be used to endorse or promote products derived from this
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
  * LAA Additions thoriso
  */
 package org.sola.clients.swing.desktop.administrative;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import org.sola.clients.beans.administrative.DisputeBean;
 import org.sola.clients.beans.administrative.DisputePartyBean;
 import org.sola.clients.beans.administrative.DisputesCommentsBean;
 import org.sola.clients.swing.desktop.cadastre.SearchParcelDialog;
 import org.sola.clients.beans.referencedata.DisputeCategoryListBean;
 import org.sola.clients.beans.referencedata.DisputeTypeListBean;
 import org.sola.clients.beans.digitalarchive.DocumentBean;
 import org.sola.clients.beans.party.PartyBean;
 import org.sola.clients.beans.source.SourceBean;
 import org.sola.clients.swing.common.tasks.SolaTask;
 import org.sola.clients.swing.common.tasks.TaskManager;
 import org.sola.clients.swing.desktop.MainForm;
 import org.sola.clients.swing.ui.ContentPanel;
 import org.sola.common.messaging.ClientMessage;
 import org.sola.common.messaging.MessageUtility;
 import org.sola.common.WindowUtility;
 import org.sola.clients.beans.administrative.*;
 import org.sola.clients.swing.common.LafManager;
 import org.sola.clients.swing.desktop.party.PartyPanelForm;
 import org.sola.clients.beans.cadastre.CadastreObjectBean;
 import org.sola.clients.swing.ui.reports.ReportViewerForm;
 import net.sf.jasperreports.engine.JasperPrint;
 import org.sola.clients.beans.application.ApplicationBean;
 import org.sola.clients.reports.ReportManager;
 import org.sola.clients.swing.ui.party.DispPartyType;
 import org.sola.clients.swing.desktop.source.DocumentsManagementExtPanel;
 import org.sola.clients.swing.ui.MainContentPanel;
 import org.sola.common.DateUtility;
 
 public class DisputePanelForm extends ContentPanel {
 
     public static final String SELECT_PARTY_PROPERTY = "selectParty";
     public static final String CREATE_NEW_PARTY_PROPERTY = "createNewParty";
     public static final String EDIT_PARTY_PROPERTY = "editParty";
     public static final String REMOVE_PARTY_PROPERTY = "removeParty";
     public static final String VIEW_PARTY_PROPERTY = "viewParty";
     public static final String VIEW_DOCUMENT = "viewDocument";
     public static final String EDIT_DOCUMENT = "editDocument";
     static String disputeString = "Dispute";
     static String courtProcessString = "Court Process";
     static String disputeResolvedStatusString = "Resolved";
     static String disputePendingStatusString = "Pending";
     private String typeofCase;
     private DisputeSearchResultBean disputeSearchResultBean;
     public DocumentBean archiveDocument;
     private String disputeID;
     private SourceBean document;
     private DisputeSearchDialog disputeSearchDialog;
     private ApplicationBean applicationBean;
     private DisputesCommentsBean disputesCommentsBean;
 
     public DisputePanelForm() {
         initComponents();
         setupDisputeBean(disputeBean1);
         RefreshScreen();
     }
 
     private void setupDisputeBean(DisputeBean disputeBean) {
         if (disputeBean != null) {
             this.disputeBean1 = disputeBean;
         } else {
             this.disputeBean1 = new DisputeBean();
         }
     }
 
     private void customizeScreen() {
         if (disputeBean1 != null && disputeBean1.getCaseType() != null) {
             if (disputeBean1.getCaseType().equals(courtProcessString)) {
                 btnCourtProcess.setSelected(true);
                 btnDisputeMode.setSelected(false);
                 switchModeRole(false);
             } else if (disputeBean1.getCaseType().equals(disputeString)) {
                 btnCourtProcess.setSelected(false);
                 btnDisputeMode.setSelected(true);
                 switchModeRole(true);
             }
             checkViewStatus(disputeBean1.getStatusCode());
             btnCourtProcess.setEnabled(false);
             btnDisputeMode.setEnabled(false);
         }
 
     }
 
     private void switchModeRole(boolean isDispute) {
         typeofCase = null;
         if (isDispute) {
             typeofCase = disputeString;
             btnCourtProcess.setSelected(false);
             jLabel1.setText("Dispute Number");
             disputeBean1.setCaseType(typeofCase);
             txtdisputeNumber.setEnabled(!isDispute);
             txtdisputeNumber.setEditable(!isDispute);
             jTabbedPane1.setSelectedIndex(0);
             jTabbedPane1.setEnabledAt(0, true);
             jTabbedPane1.setEnabledAt(3, false);
 
         } else {
             typeofCase = courtProcessString;
             btnDisputeMode.setSelected(false);
             jLabel1.setText("Case Number");
             jLabel1.setEnabled(true);
             disputeBean1.setCaseType(typeofCase);
             txtdisputeNumber.setEnabled(!isDispute);
             txtdisputeNumber.setEditable(!isDispute);
             jTabbedPane1.setSelectedIndex(3);
             jTabbedPane1.setEnabledAt(3, true);
             jTabbedPane1.setEnabledAt(0, false);
         }
         if (disputeID == null || disputeID.equals("")) {
             txtdisputeNumber.setText(null);
         }
     }
 
     private void createNewDispute() {
         if (MainForm.checkSaveBeforeClose(disputeBean1)) {
             saveDispute(true, false);
         } else {
             cleanDisputeScreen();
             customizeScreen();
             setDisputesToNormal();
             saveDisputeState();
         }
     }
 
     private void RefreshScreen() {
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sola/clients/swing/desktop/administrative/Bundle");
         disputeID = disputeBean1.getNr();
         if (disputeID != null) {
             pnlHeader.setTitleText(bundle.getString("DisputePanelForm.pnlHeader.titleText") + " #" + disputeID);
         }
         customizeScreen();
     }
 
     private DisputeBean createDisputeBean() {
         if (disputeBean1 == null) {
             disputeBean1 = new DisputeBean();
         }
         return disputeBean1;
     }
 
     private void saveDisputeState() {
         MainForm.saveBeanState(disputeBean1);
     }
 
     private void completeDispute() {
         if (disputeID != null && disputeBean1.getId() != null) {
             if (MessageUtility.displayMessage(ClientMessage.DISPUTE_COMPLETE_WARNING)
                     == MessageUtility.BUTTON_ONE) {
                 disputeBean1.setStatusCode(disputeResolvedStatusString);
                 disputeBean1.setCompletiondate(DateUtility.now());
                 disputeBean1.saveDispute();
                 checkViewStatus(disputeBean1.getStatusCode());
                 MessageUtility.displayMessage(ClientMessage.DISPUTE_CLOSED);
             }
         }
     }
 
     private void saveDispute(final boolean showMessage, final boolean closeOnSave) {
 
         if (disputeBean1.validate(true).size() > 0) {
             return;
         }
 
         if (btnCourtProcess.isSelected() && (disputeBean1.getNr() == null || disputeBean1.getNr().equals(""))) {
             MessageUtility.displayMessage(ClientMessage.DISPUTE_CAPTURE_COURT_CASE_NUMBER);
             return;
         }
         SolaTask<Void, Void> t = new SolaTask<Void, Void>() {
 
             @Override
             public Void doTask() {
                 setMessage(MessageUtility.getLocalizedMessageText(ClientMessage.PROGRESS_MSG_SAVING));
 
                 if (disputeID != null && !disputeID.equals("")) {
                     disputeBean1.saveDispute();
                 } else {
                     disputeBean1.createDispute();
                 }
                 if (closeOnSave) {
                     close();
                 }
                 return null;
             }
 
             @Override
             public void taskDone() {
                 if (showMessage) {
                     MessageUtility.displayMessage(ClientMessage.DISPUTE_SAVED);
                 }
                 RefreshScreen();
                 saveDisputeState();
             }
         };
         TaskManager.getInstance().runTask(t);
     }
 
     /**
      * Opens {@link ReportViewerForm} to display report.
      */
     private void showReport(JasperPrint report) {
         ReportViewerForm form = new ReportViewerForm(report);
         form.setLocationRelativeTo(this);
         form.setVisible(true);
     }
 
     private void printConfirmation() {
         if (disputeID != null && !disputeID.equals("")) {
             showReport(ReportManager.getDisputeConfirmationReport(disputeBean1));
         }
     }
 
     private void removeComment() {
         disputeBean1.removeSelectedComment();
     }
 
     public SourceBean getDocument() {
         if (document == null) {
             document = new SourceBean();
         }
         return document;
     }
 
     public boolean validateDocument(boolean showMessage) {
         return getDocument().validate(showMessage).size() < 1;
     }
 
     private void SearchPlot() {
         SearchParcelDialog form = new SearchParcelDialog(null, true);
         WindowUtility.centerForm(form);
         form.addPropertyChangeListener(new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals(SearchParcelDialog.SELECTED_PARCEL)) {
 
                     CadastreObjectBean cadastreObject = (CadastreObjectBean) evt.getNewValue();
 
                     disputeBean1.addChosenPlot(cadastreObject);
                 }
             }
         });
         form.setVisible(true);
     }
 
     private void searchParty() {
         SolaTask t = new SolaTask<Void, Void>() {
 
             @Override
             public Void doTask() {
                 setMessage(MessageUtility.getLocalizedMessageText(ClientMessage.PROGRESS_MSG_PERSON_SEARCHING));
                 partySearchResult.search(partySearchParams);
                 return null;
             }
 
             @Override
             public void taskDone() {
                 lblSearchResultNumber.setText(Integer.toString(partySearchResult.getPartySearchResults().size()));
                 if (partySearchResult.getPartySearchResults().size() > 100) {
                     MessageUtility.displayMessage(ClientMessage.SEARCH_TOO_MANY_RESULTS, new String[]{"100"});
                 } else if (partySearchResult.getPartySearchResults().size() < 1) {
                     MessageUtility.displayMessage(ClientMessage.SEARCH_NO_RESULTS);
                 }
             }
         };
         TaskManager.getInstance().runTask(t);
     }
 
     private void selectParty(PartyBean partyBean) {
 
         DisputePartyBean disPartyBean = new DisputePartyBean();
 
         if (partySearchResult.getSelectedPartySearchResult() != null && partyBean == null) {
             disPartyBean.addChosenParty(partySearchResult.getSelectedPartySearchResult(), disputeID);
         } else if (partySearchResult.getSelectedPartySearchResult() == null && partyBean != null) {
             disPartyBean.addNewParty(partyBean, disputeID);
         }
 
 
         DispPartyType form = new DispPartyType(disPartyBean, false, null, true, disputeBean1.getNr());
         WindowUtility.centerForm(form);
         form.addPropertyChangeListener(new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals(DispPartyType.PARTY_TYPE_SAVED)) {
                     disputeBean1.addDisputeParty((DisputePartyBean) evt.getNewValue());
                 }
             }
         });
         form.setVisible(true);
 
     }
 
     private void viewParty() {
         if (disputeBean1.getSelectedParty() != null) {
             PartyPanelForm partyForm;
             partyForm = new PartyPanelForm(false, PartyBean.getParty(disputeBean1.getSelectedParty().getPartyId()), true, false);
             getMainContentPanel().addPanel(partyForm, MainContentPanel.CARD_PERSON, true);
         }
     }
 
     private class DisputePartyFormListener implements PropertyChangeListener {
 
         @Override
         public void propertyChange(PropertyChangeEvent evt) {
             if (evt.getPropertyName().equals(PartyPanelForm.PARTY_SAVED)) {
                 selectParty((PartyBean) ((PartyPanelForm) evt.getSource()).getParty());
             }
         }
     }
 
     private void addParty() {
         final DisputePartyFormListener listener = new DisputePartyFormListener();
         SolaTask t = new SolaTask<Void, Void>() {
 
             @Override
             public Void doTask() {
                 setMessage(MessageUtility.getLocalizedMessageText(ClientMessage.PROGRESS_MSG_OPEN_PERSON));
                 PartyPanelForm partyForm;
                 partyForm = new PartyPanelForm(true, null, false, true);
                 partyForm.addPropertyChangeListener(listener);
                 getMainContentPanel().addPanel(partyForm, MainContentPanel.CARD_PERSON, true);
                 return null;
             }
         };
         TaskManager.getInstance().runTask(t);
     }
 
     private void removeParty() {
         disputeBean1.removeSelectedParty();
     }
 
     private void searchDispute() {
         if (disputeSearchDialog != null) {
             disputeSearchDialog.dispose();
         }
         disputeSearchDialog = new DisputeSearchDialog();
         disputeSearchDialog.setLocationRelativeTo(this);
         disputeSearchDialog.addPropertyChangeListener(new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals(DisputeSearchDialog.SELECTED_DISPUTE_SEARCH_RESULT)) {
                     disputeSearchResultBean = (DisputeSearchResultBean) evt.getNewValue();
                     disputeBean1.assignDispute(disputeSearchResultBean.getId());
                     RefreshScreen();
                 }
             }
         });
         disputeSearchDialog.setVisible(true);
     }
 
     private DisputeCategoryListBean createDisputeCategory() {
         if (disputeCategory == null) {
             String categoryCode = null;
             if (disputeBean1 != null && disputeBean1.getDisputeCategoryCode() != null) {
                 categoryCode = disputeBean1.getDisputeCategoryCode();
             }
             disputeCategory = new DisputeCategoryListBean(true, categoryCode);
         }
         return disputeCategory;
     }
 
     private DisputeTypeListBean createDisputeType() {
         if (disputeType == null) {
             String typeCode = null;
             if (disputeBean1 != null && disputeBean1.getDisputeTypeCode() != null) {
                 typeCode = disputeBean1.getDisputeTypeCode();
             }
             disputeType = new DisputeTypeListBean(true, typeCode);
         }
         return disputeType;
     }
 
     private DocumentsManagementExtPanel createDocumentsPanel() {
         if (disputeBean1 == null) {
             disputeBean1 = new DisputeBean();
         }
         if (applicationBean == null) {
             applicationBean = new ApplicationBean();
         }
         boolean allowEdit = true;
 
         DocumentsManagementExtPanel panel = new DocumentsManagementExtPanel(
                 disputeBean1.getSourceList(), disputeBean1, null, allowEdit);
         return panel;
     }
 
     private void addDisputeComments() {
         DisputeCommentsDialog form = new DisputeCommentsDialog(disputesCommentsBean, false, null, true, disputeBean1.getNr());
         WindowUtility.centerForm(form);
         form.addPropertyChangeListener(new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals(DisputeCommentsDialog.COMMENT_SAVED)) {
                     disputeBean1.addDisputeComment((DisputesCommentsBean) evt.getNewValue());
                 }
             }
         });
         form.setVisible(true);
     }
 
     private void viewDisputeComments() {
         DisputeCommentsDialog form = null;
         if (disputeBean1.getStatusCode().equals(disputeResolvedStatusString)) {
             form = new DisputeCommentsDialog(disputeBean1.getSelectedComment(), true, null, true, disputeBean1.getNr());
         } else if (disputeBean1.getStatusCode().equals(disputePendingStatusString)) {
             form = new DisputeCommentsDialog(disputeBean1.getSelectedComment(), false, null, true, disputeBean1.getNr());
         }
         WindowUtility.centerForm(form);
         form.setVisible(true);
     }
 
     private void cleanDisputeScreen() {
         disputeBean1.setNr(null);
         disputeBean1.setLodgementDate(null);
         disputeBean1.setStatusCode(null);
         disputeBean1.setPlotNumber(null);
         disputeBean1.setPlotLocation(null);
         disputeBean1.setLeaseNumber(null);
         disputeBean1.setDisputeCategory(null);
         disputeBean1.setDisputeType(null);
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sola/clients/swing/desktop/administrative/Bundle");
         pnlHeader.setTitleText(bundle.getString("DisputePanelForm.pnlHeader.titleText"));
         disputeBean1.setDisputeDescription(null);
         disputeBean1.getDisputeCommentsList().clear();
         disputeBean1.getDisputePartyList().clear();
         partySearchResult.getPartySearchResults().clear();
         disputeBean1.getSourceList().clear();
 
     }
 
     private void checkViewStatus(String state) {
         if (state != null) {
             if (state.equals(disputeResolvedStatusString)) {
                 setDisputesToReadOnly();
             } else if (state.equals(disputePendingStatusString)) {
                 setDisputesToNormal();
             }
         }
     }
 
     private void setDisputesToReadOnly() {
         btnDisputeMode.setEnabled(false);
         btnCourtProcess.setEnabled(false);
         btnSearchPlot.setEnabled(false);
         txtplotLocation.setEnabled(false);
         dbxdisputeCategory.setEnabled(false);
         dbxdisputeType.setEnabled(false);
         txtrrrId.setEnabled(false);
         txtDisputeDescription.setEnabled(false);
         btnAddComment1.setEnabled(false);
         btnRemoveDisputeComment.setEnabled(false);
         txtName1.setEnabled(false);
         btnClear.setEnabled(false);
         btnSearch.setEnabled(false);
         btnAddParty.setEnabled(false);
         btnSelect.setEnabled(false);
         btnView.setEnabled(false);
         btnRemoveParty.setEnabled(false);
         btnSaveDispute.setEnabled(false);
         btnCompleteDispute.setEnabled(false);
         btnPrintConfirmation.setEnabled(false);
     }
 
     private void setDisputesToNormal() {
         btnDisputeMode.setEnabled(true);
         btnCourtProcess.setEnabled(true);
         btnSearchPlot.setEnabled(true);
         txtplotLocation.setEnabled(true);
         dbxdisputeCategory.setEnabled(true);
         dbxdisputeType.setEnabled(true);
         txtrrrId.setEnabled(true);
         txtDisputeDescription.setEnabled(true);
         btnAddComment1.setEnabled(true);
         btnRemoveDisputeComment.setEnabled(true);
         txtName1.setEnabled(true);
         btnClear.setEnabled(true);
         btnSearch.setEnabled(true);
         btnAddParty.setEnabled(true);
         btnSelect.setEnabled(true);
         btnView.setEnabled(true);
         btnRemoveParty.setEnabled(true);
         btnSaveDispute.setEnabled(true);
         btnCompleteDispute.setEnabled(true);
         btnPrintConfirmation.setEnabled(true);
     }
 
     @Override
     protected boolean panelClosing() {
         if (disputeID != null && !disputeID.equals("") && disputeBean1.getStatusCode() != null) {
             if (disputeBean1.getStatusCode().equals(disputePendingStatusString)) {
                 if (MainForm.checkSaveBeforeClose(disputeBean1)) {
                     saveDispute(true, true);
                     return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         disputeBean1 = createDisputeBean();
         disputeType = createDisputeType();
         cadastreObjectBean1 = new org.sola.clients.beans.cadastre.CadastreObjectBean();
         partySearchParams = new org.sola.clients.beans.party.PartySearchParamsBean();
         partySearchResult = new org.sola.clients.beans.party.PartySearchResultListBean();
         cadastreObjectSearch = new org.sola.clients.beans.cadastre.CadastreObjectSearchResultListBean();
         disputeCategory = createDisputeCategory();
         jToolBar1 = new javax.swing.JToolBar();
         btnnewDispute = new javax.swing.JButton();
         jSeparator1 = new javax.swing.JToolBar.Separator();
         btnSaveDispute = new javax.swing.JButton();
         jSeparator3 = new javax.swing.JToolBar.Separator();
         btnsearchDispute = new javax.swing.JButton();
         jSeparator4 = new javax.swing.JToolBar.Separator();
         btnCompleteDispute = new javax.swing.JButton();
         jSeparator5 = new javax.swing.JToolBar.Separator();
         btnPrintConfirmation = new javax.swing.JButton();
         pnlHeader = new org.sola.clients.swing.ui.HeaderPanel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         tabGeneralInfo = new javax.swing.JPanel();
         groupPanel1 = new org.sola.clients.swing.ui.GroupPanel();
         jToolBar3 = new javax.swing.JToolBar();
         btnAddComment1 = new javax.swing.JButton();
         jButton1 = new javax.swing.JButton();
         btnRemoveDisputeComment = new javax.swing.JButton();
         jPanel8 = new javax.swing.JPanel();
         jPanel23 = new javax.swing.JPanel();
         lblPlotNumber = new javax.swing.JLabel();
         txtcadastreId = new javax.swing.JTextField();
         btnSearchPlot = new javax.swing.JButton();
         jPanel3 = new javax.swing.JPanel();
         lblPlotLocation = new javax.swing.JLabel();
         txtplotLocation = new javax.swing.JTextField();
         jPanel39 = new javax.swing.JPanel();
         jPanel40 = new javax.swing.JPanel();
         dbxdisputeCategory = new javax.swing.JComboBox();
         lblLeaseCategory1 = new javax.swing.JLabel();
         jPanel41 = new javax.swing.JPanel();
         jLabel3 = new javax.swing.JLabel();
         dbxdisputeType = new javax.swing.JComboBox();
         jPanel42 = new javax.swing.JPanel();
         jLabel5 = new javax.swing.JLabel();
         txtrrrId = new javax.swing.JTextField();
         jLabel4 = new javax.swing.JLabel();
         jPanel38 = new javax.swing.JPanel();
         groupPanel2 = new org.sola.clients.swing.ui.GroupPanel();
         jScrollPane4 = new javax.swing.JScrollPane();
         jTable2 = new javax.swing.JTable();
         txtDisputeDescription = new javax.swing.JTextField();
         jPanel9 = new javax.swing.JPanel();
         jPanel27 = new javax.swing.JPanel();
         pnlSearch = new javax.swing.JPanel();
         jPanel11 = new javax.swing.JPanel();
         jPanel14 = new javax.swing.JPanel();
         jPanel17 = new javax.swing.JPanel();
         jLabel11 = new javax.swing.JLabel();
         txtName1 = new javax.swing.JTextField();
         jPanel15 = new javax.swing.JPanel();
         btnClear = new javax.swing.JButton();
         btnSearch = new javax.swing.JButton();
         jPanel16 = new javax.swing.JPanel();
         jLabel13 = new javax.swing.JLabel();
         jToolBar4 = new javax.swing.JToolBar();
         btnAddParty = new javax.swing.JButton();
         jSeparator2 = new javax.swing.JToolBar.Separator();
         btnSelect = new javax.swing.JButton();
         jLabel9 = new javax.swing.JLabel();
         lblSearchResultNumber = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
         groupPanel3 = new org.sola.clients.swing.ui.GroupPanel();
         groupPanel4 = new org.sola.clients.swing.ui.GroupPanel();
         jToolBar6 = new javax.swing.JToolBar();
         btnView = new javax.swing.JButton();
         jSeparator6 = new javax.swing.JToolBar.Separator();
         btnRemoveParty = new javax.swing.JButton();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTable3 = new javax.swing.JTable();
         jPanel5 = new javax.swing.JPanel();
         documentsManagementPanel = createDocumentsPanel();
         jPanel18 = new javax.swing.JPanel();
         cbxLAAPrimary = new javax.swing.JCheckBox();
         jScrollPane5 = new javax.swing.JScrollPane();
         txtActionRequired = new javax.swing.JTextPane();
         jLabel10 = new javax.swing.JLabel();
         jPanel2 = new javax.swing.JPanel();
         jPanel4 = new javax.swing.JPanel();
         jPanel12 = new javax.swing.JPanel();
         btnDisputeMode = new javax.swing.JRadioButton();
         jPanel13 = new javax.swing.JPanel();
         btnCourtProcess = new javax.swing.JRadioButton();
         jPanel19 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         txtdisputeNumber = new javax.swing.JTextField();
         jPanel20 = new javax.swing.JPanel();
         lblLodgementDate = new javax.swing.JLabel();
         txtlodgementDate = new javax.swing.JFormattedTextField();
         jPanel1 = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         txtstatus = new javax.swing.JTextField();
 
         setHeaderPanel(pnlHeader);
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sola/clients/swing/desktop/administrative/Bundle"); // NOI18N
         setName(bundle.getString("DisputePanelForm.name")); // NOI18N
 
         jToolBar1.setBorder(null);
         jToolBar1.setFloatable(false);
         jToolBar1.setRollover(true);
         jToolBar1.setName(bundle.getString("DisputePanelForm.jToolBar1.name")); // NOI18N
 
         btnnewDispute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/new.png"))); // NOI18N
         btnnewDispute.setText(bundle.getString("DisputePanelForm.btnnewDispute.text")); // NOI18N
         btnnewDispute.setFocusable(false);
         btnnewDispute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnnewDispute.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnnewDisputeActionPerformed(evt);
             }
         });
         jToolBar1.add(btnnewDispute);
 
         jSeparator1.setName(bundle.getString("DisputePanelForm.jSeparator1.name")); // NOI18N
         jToolBar1.add(jSeparator1);
 
         btnSaveDispute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/save.png"))); // NOI18N
         btnSaveDispute.setText(bundle.getString("DisputePanelForm.btnSaveDispute.text")); // NOI18N
         btnSaveDispute.setToolTipText(bundle.getString("DisputePanelForm.btnSaveDispute.toolTipText")); // NOI18N
         btnSaveDispute.setFocusable(false);
         btnSaveDispute.setName(bundle.getString("DisputePanelForm.btnSaveDispute.name")); // NOI18N
         btnSaveDispute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnSaveDispute.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveDisputeActionPerformed(evt);
             }
         });
         jToolBar1.add(btnSaveDispute);
 
         jSeparator3.setName(bundle.getString("DisputePanelForm.jSeparator3.name")); // NOI18N
         jToolBar1.add(jSeparator3);
 
         btnsearchDispute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/search.png"))); // NOI18N
         btnsearchDispute.setText(bundle.getString("DisputePanelForm.btnsearchDispute.text")); // NOI18N
         btnsearchDispute.setFocusable(false);
         btnsearchDispute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnsearchDispute.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnsearchDisputeActionPerformed(evt);
             }
         });
         jToolBar1.add(btnsearchDispute);
 
         jSeparator4.setName(bundle.getString("DisputePanelForm.jSeparator4.name")); // NOI18N
         jToolBar1.add(jSeparator4);
 
         btnCompleteDispute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/lock.png"))); // NOI18N
         btnCompleteDispute.setText(bundle.getString("DisputePanelForm.btnCompleteDispute.text")); // NOI18N
         btnCompleteDispute.setFocusable(false);
         btnCompleteDispute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnCompleteDispute.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCompleteDisputeActionPerformed(evt);
             }
         });
         jToolBar1.add(btnCompleteDispute);
         jToolBar1.add(jSeparator5);
 
         btnPrintConfirmation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/print.png"))); // NOI18N
         btnPrintConfirmation.setText(bundle.getString("DisputePanelForm.btnPrintConfirmation.text_2")); // NOI18N
         btnPrintConfirmation.setFocusable(false);
         btnPrintConfirmation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnPrintConfirmation.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnPrintConfirmationActionPerformed(evt);
             }
         });
         jToolBar1.add(btnPrintConfirmation);
 
         pnlHeader.setName(bundle.getString("DisputePanelForm.pnlHeader.name")); // NOI18N
         pnlHeader.setTitleText(bundle.getString("DisputePanelForm.pnlHeader.titleText")); // NOI18N
 
         jTabbedPane1.setName(bundle.getString("DisputePanelForm.jTabbedPane1.name")); // NOI18N
 
         tabGeneralInfo.setName(bundle.getString("DisputePanelForm.tabGeneralInfo.name")); // NOI18N
 
         groupPanel1.setTitleText(bundle.getString("DisputePanelForm.groupPanel1.titleText")); // NOI18N
 
         jToolBar3.setFloatable(false);
         jToolBar3.setRollover(true);
         jToolBar3.setName(bundle.getString("DisputePanelForm.jToolBar3.name_1")); // NOI18N
 
         btnAddComment1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/add.png"))); // NOI18N
         btnAddComment1.setText(bundle.getString("DisputePanelForm.btnAddComment1.text_1")); // NOI18N
         btnAddComment1.setFocusable(false);
         btnAddComment1.setName(bundle.getString("DisputePanelForm.btnAddComment1.name_1")); // NOI18N
         btnAddComment1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnAddComment1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddComment1ActionPerformed(evt);
             }
         });
         jToolBar3.add(btnAddComment1);
 
         jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/view.png"))); // NOI18N
         jButton1.setText(bundle.getString("DisputePanelForm.jButton1.text_2")); // NOI18N
         jButton1.setFocusable(false);
         jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
         jToolBar3.add(jButton1);
 
         btnRemoveDisputeComment.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/remove.png"))); // NOI18N
         btnRemoveDisputeComment.setText(bundle.getString("DisputePanelForm.btnRemoveDisputeComment.text_1")); // NOI18N
         btnRemoveDisputeComment.setFocusable(false);
         btnRemoveDisputeComment.setName(bundle.getString("DisputePanelForm.btnRemoveDisputeComment.name_1")); // NOI18N
         btnRemoveDisputeComment.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnRemoveDisputeComment.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveDisputeCommentActionPerformed(evt);
             }
         });
         jToolBar3.add(btnRemoveDisputeComment);
 
         jPanel8.setLayout(new java.awt.GridLayout(2, 2, 10, 5));
 
         lblPlotNumber.setText(bundle.getString("DisputePanelForm.lblPlotNumber.text")); // NOI18N
         lblPlotNumber.setName(bundle.getString("DisputePanelForm.lblPlotNumber.name")); // NOI18N
 
         txtcadastreId.setEnabled(false);
         txtcadastreId.setName(bundle.getString("DisputePanelForm.txtcadastreId.name")); // NOI18N
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${plotNumber}"), txtcadastreId, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         txtcadastreId.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 txtcadastreIdActionPerformed(evt);
             }
         });
 
         btnSearchPlot.setText(bundle.getString("DisputePanelForm.btnSearchPlot.text")); // NOI18N
         btnSearchPlot.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSearchPlotActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
         jPanel23.setLayout(jPanel23Layout);
         jPanel23Layout.setHorizontalGroup(
             jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel23Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel23Layout.createSequentialGroup()
                         .addComponent(lblPlotNumber)
                         .addGap(0, 306, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                         .addGap(0, 277, Short.MAX_VALUE)
                         .addComponent(btnSearchPlot)))
                 .addContainerGap())
             .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel23Layout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(txtcadastreId, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                     .addGap(101, 101, 101)))
         );
         jPanel23Layout.setVerticalGroup(
             jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel23Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblPlotNumber)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(btnSearchPlot)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel23Layout.createSequentialGroup()
                     .addGap(37, 37, 37)
                     .addComponent(txtcadastreId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(13, Short.MAX_VALUE)))
         );
 
         jPanel8.add(jPanel23);
 
         lblPlotLocation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         lblPlotLocation.setText(bundle.getString("DisputePanelForm.lblPlotLocation.text")); // NOI18N
         lblPlotLocation.setName(bundle.getString("DisputePanelForm.lblPlotLocation.name")); // NOI18N
 
         txtplotLocation.setName(bundle.getString("DisputePanelForm.txtplotLocation.name")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${plotLocation}"), txtplotLocation, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txtplotLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(lblPlotLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addGap(6, 6, 6)
                 .addComponent(lblPlotLocation)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(txtplotLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(18, Short.MAX_VALUE))
         );
 
         jPanel8.add(jPanel3);
 
         jPanel39.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
 
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${disputeCategoryListBean}");
         org.jdesktop.swingbinding.JComboBoxBinding jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeCategory, eLProperty, dbxdisputeCategory);
         bindingGroup.addBinding(jComboBoxBinding);
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${disputeCategory}"), dbxdisputeCategory, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"), "");
         bindingGroup.addBinding(binding);
 
         dbxdisputeCategory.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 dbxdisputeCategoryActionPerformed(evt);
             }
         });
 
         lblLeaseCategory1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         lblLeaseCategory1.setText(bundle.getString("DisputePanelForm.lblLeaseCategory1.text")); // NOI18N
         lblLeaseCategory1.setName(bundle.getString("DisputePanelForm.lblLeaseCategory1.name")); // NOI18N
 
         javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
         jPanel40.setLayout(jPanel40Layout);
         jPanel40Layout.setHorizontalGroup(
             jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel40Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(dbxdisputeCategory, 0, 169, Short.MAX_VALUE)
                     .addComponent(lblLeaseCategory1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel40Layout.setVerticalGroup(
             jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel40Layout.createSequentialGroup()
                 .addComponent(lblLeaseCategory1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(dbxdisputeCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 29, Short.MAX_VALUE))
         );
 
         jPanel39.add(jPanel40);
 
         jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel3.setText(bundle.getString("DisputePanelForm.jLabel3.text")); // NOI18N
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${disputeTypeListBean}");
         jComboBoxBinding = org.jdesktop.swingbinding.SwingBindings.createJComboBoxBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeType, eLProperty, dbxdisputeType);
         bindingGroup.addBinding(jComboBoxBinding);
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${disputeType}"), dbxdisputeType, org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
         bindingGroup.addBinding(binding);
 
         dbxdisputeType.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 dbxdisputeTypeActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel41Layout = new javax.swing.GroupLayout(jPanel41);
         jPanel41.setLayout(jPanel41Layout);
         jPanel41Layout.setHorizontalGroup(
             jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel41Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(dbxdisputeType, 0, 169, Short.MAX_VALUE)
                     .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel41Layout.setVerticalGroup(
             jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel41Layout.createSequentialGroup()
                 .addComponent(jLabel3)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(dbxdisputeType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 29, Short.MAX_VALUE))
         );
 
         jPanel39.add(jPanel41);
 
         jPanel8.add(jPanel39);
 
         jLabel5.setText(bundle.getString("DisputePanelForm.jLabel5.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${leaseNumber}"), txtrrrId, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel42Layout = new javax.swing.GroupLayout(jPanel42);
         jPanel42.setLayout(jPanel42Layout);
         jPanel42Layout.setHorizontalGroup(
             jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel42Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                     .addComponent(txtrrrId))
                 .addContainerGap())
         );
         jPanel42Layout.setVerticalGroup(
             jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel42Layout.createSequentialGroup()
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtrrrId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 29, Short.MAX_VALUE))
         );
 
         jPanel8.add(jPanel42);
 
         jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel4.setText(bundle.getString("DisputePanelForm.jLabel4.text_2")); // NOI18N
 
         groupPanel2.setTitleText(bundle.getString("DisputePanelForm.groupPanel2.titleText_1")); // NOI18N
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${filteredDisputeCommentsList}");
         org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, eLProperty, jTable2);
         org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${updateDate}"));
         columnBinding.setColumnName("Update Date");
         columnBinding.setColumnClass(java.util.Date.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${comments}"));
         columnBinding.setColumnName("Comments");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         bindingGroup.addBinding(jTableBinding);
         jTableBinding.bind();binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${selectedComment}"), jTable2, org.jdesktop.beansbinding.BeanProperty.create("selectedElement"));
         bindingGroup.addBinding(binding);
 
         jScrollPane4.setViewportView(jTable2);
         jTable2.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("DisputePanelForm.jTable2.columnModel.title0_2")); // NOI18N
         jTable2.getColumnModel().getColumn(1).setMinWidth(650);
         jTable2.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("DisputePanelForm.jTable2.columnModel.title1_2")); // NOI18N
         jTable2.getColumnModel().getColumn(1).setCellRenderer(new org.sola.clients.swing.ui.renderers.CellDelimitedListRenderer());
 
         javax.swing.GroupLayout jPanel38Layout = new javax.swing.GroupLayout(jPanel38);
         jPanel38.setLayout(jPanel38Layout);
         jPanel38Layout.setHorizontalGroup(
             jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane4)
             .addComponent(groupPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel38Layout.setVerticalGroup(
             jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel38Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(groupPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
         );
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${disputeDescription}"), txtDisputeDescription, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout tabGeneralInfoLayout = new javax.swing.GroupLayout(tabGeneralInfo);
         tabGeneralInfo.setLayout(tabGeneralInfoLayout);
         tabGeneralInfoLayout.setHorizontalGroup(
             tabGeneralInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(groupPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 779, Short.MAX_VALUE)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabGeneralInfoLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(tabGeneralInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txtDisputeDescription, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(tabGeneralInfoLayout.createSequentialGroup()
                         .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
             .addComponent(jPanel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         tabGeneralInfoLayout.setVerticalGroup(
             tabGeneralInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabGeneralInfoLayout.createSequentialGroup()
                 .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel4)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtDisputeDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(groupPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab(bundle.getString("DisputePanelForm.tabGeneralInfo.TabConstraints.tabTitle"), tabGeneralInfo); // NOI18N
 
         jPanel27.setMinimumSize(new java.awt.Dimension(300, 200));
         jPanel27.setPreferredSize(new java.awt.Dimension(768, 440));
 
         pnlSearch.setMinimumSize(new java.awt.Dimension(300, 300));
         pnlSearch.setPreferredSize(new java.awt.Dimension(766, 430));
 
         jPanel14.setLayout(new java.awt.GridLayout(1, 3, 10, 5));
 
         jLabel11.setText(bundle.getString("DisputePanelForm.jLabel11.text_2")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, partySearchParams, org.jdesktop.beansbinding.ELProperty.create("${name}"), txtName1, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
         jPanel17.setLayout(jPanel17Layout);
         jPanel17Layout.setHorizontalGroup(
             jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel17Layout.createSequentialGroup()
                 .addComponent(jLabel11)
                 .addContainerGap(182, Short.MAX_VALUE))
             .addComponent(txtName1)
         );
         jPanel17Layout.setVerticalGroup(
             jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel17Layout.createSequentialGroup()
                 .addComponent(jLabel11)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(13, Short.MAX_VALUE))
         );
 
         jPanel14.add(jPanel17);
 
         btnClear.setText(bundle.getString("DisputePanelForm.btnClear.text_2")); // NOI18N
         btnClear.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnClearActionPerformed(evt);
             }
         });
 
         btnSearch.setText(bundle.getString("DisputePanelForm.btnSearch.text_2")); // NOI18N
         btnSearch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSearchActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
         jPanel15.setLayout(jPanel15Layout);
         jPanel15Layout.setHorizontalGroup(
             jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel15Layout.createSequentialGroup()
                 .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         jPanel15Layout.setVerticalGroup(
             jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel15Layout.createSequentialGroup()
                 .addContainerGap(19, Short.MAX_VALUE)
                 .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnClear)
                     .addComponent(btnSearch))
                 .addContainerGap())
         );
 
         jPanel14.add(jPanel15);
 
         jLabel13.setText(bundle.getString("DisputePanelForm.jLabel13.text_1")); // NOI18N
 
         javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
         jPanel16.setLayout(jPanel16Layout);
         jPanel16Layout.setHorizontalGroup(
             jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel16Layout.createSequentialGroup()
                 .addContainerGap(236, Short.MAX_VALUE)
                 .addComponent(jLabel13)
                 .addContainerGap())
         );
         jPanel16Layout.setVerticalGroup(
             jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                 .addComponent(jLabel13)
                 .addContainerGap(39, Short.MAX_VALUE))
         );
 
         jPanel14.add(jPanel16);
 
         jToolBar4.setFloatable(false);
         jToolBar4.setRollover(true);
 
         btnAddParty.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/create.png"))); // NOI18N
         btnAddParty.setText(bundle.getString("DisputePanelForm.btnAddParty.text")); // NOI18N
         btnAddParty.setFocusable(false);
         btnAddParty.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         btnAddParty.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnAddParty.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddPartyActionPerformed(evt);
             }
         });
         jToolBar4.add(btnAddParty);
         jToolBar4.add(jSeparator2);
 
         btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/select.png"))); // NOI18N
         btnSelect.setText(bundle.getString("DisputePanelForm.btnSelect.text")); // NOI18N
         btnSelect.setFocusable(false);
         btnSelect.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         btnSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnSelect.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSelectActionPerformed(evt);
             }
         });
         jToolBar4.add(btnSelect);
 
         jLabel9.setText(bundle.getString("DisputePanelForm.jLabel9.text")); // NOI18N
         jToolBar4.add(jLabel9);
 
         lblSearchResultNumber.setFont(LafManager.getInstance().getLabFontBold());
         lblSearchResultNumber.setText(bundle.getString("DisputePanelForm.lblSearchResultNumber.text")); // NOI18N
         jToolBar4.add(lblSearchResultNumber);
 
         javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
         jPanel11.setLayout(jPanel11Layout);
         jPanel11Layout.setHorizontalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, 769, Short.MAX_VALUE)
             .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel11Layout.setVerticalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel11Layout.createSequentialGroup()
                 .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(jToolBar4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(31, 31, 31))
         );
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${filteredDisputePartyList}");
         jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, eLProperty, jTable1);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${partyName}"));
         columnBinding.setColumnName("Party Name");
         columnBinding.setColumnClass(String.class);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${partyRole}"));
         columnBinding.setColumnName("Party Role");
         columnBinding.setColumnClass(String.class);
         bindingGroup.addBinding(jTableBinding);
         jTableBinding.bind();binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${selectedParty}"), jTable1, org.jdesktop.beansbinding.BeanProperty.create("selectedElement"));
         bindingGroup.addBinding(binding);
 
         jScrollPane1.setViewportView(jTable1);
 
         groupPanel3.setTitleText(bundle.getString("DisputePanelForm.groupPanel3.titleText")); // NOI18N
 
         groupPanel4.setTitleText(bundle.getString("DisputePanelForm.groupPanel4.titleText")); // NOI18N
 
         jToolBar6.setFloatable(false);
         jToolBar6.setRollover(true);
 
         btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/view.png"))); // NOI18N
         btnView.setText(bundle.getString("DisputePanelForm.btnView.text_2")); // NOI18N
         btnView.setFocusable(false);
         btnView.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnView.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnViewActionPerformed(evt);
             }
         });
         jToolBar6.add(btnView);
         jToolBar6.add(jSeparator6);
 
         btnRemoveParty.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/remove.png"))); // NOI18N
         btnRemoveParty.setText(bundle.getString("DisputePanelForm.btnRemoveParty.text_2")); // NOI18N
         btnRemoveParty.setFocusable(false);
         btnRemoveParty.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnRemoveParty.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemovePartyActionPerformed(evt);
             }
         });
         jToolBar6.add(btnRemoveParty);
 
         eLProperty = org.jdesktop.beansbinding.ELProperty.create("${partySearchResults}");
         jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, partySearchResult, eLProperty, jTable3);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${name}"));
         columnBinding.setColumnName("Name");
         columnBinding.setColumnClass(String.class);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${rightHolder}"));
         columnBinding.setColumnName("Right Holder");
         columnBinding.setColumnClass(Boolean.class);
         bindingGroup.addBinding(jTableBinding);
         jTableBinding.bind();binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, partySearchResult, org.jdesktop.beansbinding.ELProperty.create("${selectedPartySearchResult}"), jTable3, org.jdesktop.beansbinding.BeanProperty.create("selectedElement"));
         bindingGroup.addBinding(binding);
 
         jScrollPane2.setViewportView(jTable3);
 
         javax.swing.GroupLayout pnlSearchLayout = new javax.swing.GroupLayout(pnlSearch);
         pnlSearch.setLayout(pnlSearchLayout);
         pnlSearchLayout.setHorizontalGroup(
             pnlSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jScrollPane1)
             .addComponent(groupPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(groupPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jToolBar6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jScrollPane2)
         );
         pnlSearchLayout.setVerticalGroup(
             pnlSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlSearchLayout.createSequentialGroup()
                 .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(groupPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jToolBar6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(groupPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
         jPanel27.setLayout(jPanel27Layout);
         jPanel27Layout.setHorizontalGroup(
             jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                 .addComponent(pnlSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 769, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel27Layout.setVerticalGroup(
             jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(pnlSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
 
         javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel27, javax.swing.GroupLayout.DEFAULT_SIZE, 779, Short.MAX_VALUE)
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel9Layout.createSequentialGroup()
                 .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab(bundle.getString("DisputePanelForm.jPanel9.TabConstraints.tabTitle"), jPanel9); // NOI18N
 
         jPanel5.setName(bundle.getString("DisputePanelForm.jPanel5.name")); // NOI18N
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(documentsManagementPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 779, Short.MAX_VALUE)
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addComponent(documentsManagementPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 232, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab(bundle.getString("DisputePanelForm.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N
 
         cbxLAAPrimary.setText(bundle.getString("DisputePanelForm.cbxLAAPrimary.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${primaryRespondent}"), cbxLAAPrimary, org.jdesktop.beansbinding.BeanProperty.create("selected"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${actionRequired}"), txtActionRequired, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         jScrollPane5.setViewportView(txtActionRequired);
 
         jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel10.setText(bundle.getString("DisputePanelForm.jLabel10.text_1")); // NOI18N
 
         javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
         jPanel18.setLayout(jPanel18Layout);
         jPanel18Layout.setHorizontalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel18Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane5)
                     .addGroup(jPanel18Layout.createSequentialGroup()
                         .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(cbxLAAPrimary, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(0, 534, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel18Layout.setVerticalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel18Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(cbxLAAPrimary)
                 .addGap(11, 11, 11)
                 .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(151, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab(bundle.getString("DisputePanelForm.jPanel18.TabConstraints.tabTitle"), jPanel18); // NOI18N
 
         jPanel2.setName(bundle.getString("DisputePanelForm.jPanel2.name_1")); // NOI18N
         jPanel2.setLayout(new java.awt.GridLayout(1, 4, 10, 5));
 
         jPanel4.setLayout(new java.awt.GridLayout(1, 2, 10, 5));
 
         btnDisputeMode.setText(bundle.getString("DisputePanelForm.btnDisputeMode.text_1")); // NOI18N
         btnDisputeMode.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDisputeModeActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
         jPanel12.setLayout(jPanel12Layout);
         jPanel12Layout.setHorizontalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnDisputeMode, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel12Layout.setVerticalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel12Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnDisputeMode)
                 .addContainerGap(21, Short.MAX_VALUE))
         );
 
         jPanel4.add(jPanel12);
 
         btnCourtProcess.setText(bundle.getString("DisputePanelForm.btnCourtProcess.text_1")); // NOI18N
         btnCourtProcess.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCourtProcessActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
         jPanel13.setLayout(jPanel13Layout);
         jPanel13Layout.setHorizontalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnCourtProcess)
                 .addContainerGap(27, Short.MAX_VALUE))
         );
         jPanel13Layout.setVerticalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(btnCourtProcess)
                 .addContainerGap(21, Short.MAX_VALUE))
         );
 
         jPanel4.add(jPanel13);
 
         jPanel2.add(jPanel4);
 
         jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel1.setText(bundle.getString("DisputePanelForm.jLabel1.text_1")); // NOI18N
         jLabel1.setName(bundle.getString("DisputePanelForm.jLabel1.name_1")); // NOI18N
 
         txtdisputeNumber.setEditable(false);
         txtdisputeNumber.setEnabled(false);
         txtdisputeNumber.setName(bundle.getString("DisputePanelForm.txtdisputeNumber.name_1")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${nr}"), txtdisputeNumber, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
         jPanel19.setLayout(jPanel19Layout);
         jPanel19Layout.setHorizontalGroup(
             jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel19Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txtdisputeNumber, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                     .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel19Layout.setVerticalGroup(
             jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel19Layout.createSequentialGroup()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtdisputeNumber)
                 .addContainerGap())
         );
 
         jPanel2.add(jPanel19);
 
         lblLodgementDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         lblLodgementDate.setText(bundle.getString("DisputePanelForm.lblLodgementDate.text_1")); // NOI18N
         lblLodgementDate.setName(bundle.getString("DisputePanelForm.lblLodgementDate.name_1")); // NOI18N
 
         txtlodgementDate.setEditable(false);
         txtlodgementDate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(new java.text.SimpleDateFormat("dd-MMM-yyyy"))));
         txtlodgementDate.setEnabled(false);
         txtlodgementDate.setName(bundle.getString("DisputePanelForm.txtlodgementDate.name_1")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${lodgementDate}"), txtlodgementDate, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
         jPanel20.setLayout(jPanel20Layout);
         jPanel20Layout.setHorizontalGroup(
             jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel20Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblLodgementDate, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                     .addComponent(txtlodgementDate))
                 .addContainerGap())
         );
         jPanel20Layout.setVerticalGroup(
             jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel20Layout.createSequentialGroup()
                 .addComponent(lblLodgementDate)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtlodgementDate)
                 .addContainerGap())
         );
 
         jPanel2.add(jPanel20);
 
         jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/red_asterisk.gif"))); // NOI18N
         jLabel2.setText(bundle.getString("DisputePanelForm.jLabel2.text_1")); // NOI18N
         jLabel2.setName(bundle.getString("DisputePanelForm.jLabel2.name_1")); // NOI18N
 
         txtstatus.setEditable(false);
         txtstatus.setEnabled(false);
         txtstatus.setName(bundle.getString("DisputePanelForm.txtstatus.name_1")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, disputeBean1, org.jdesktop.beansbinding.ELProperty.create("${statusCode}"), txtstatus, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txtstatus, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                     .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtstatus)
                 .addContainerGap())
         );
 
         jPanel2.add(jPanel1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(pnlHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jTabbedPane1)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addContainerGap())))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(pnlHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(6, 6, 6)
                 .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTabbedPane1))
         );
 
         bindingGroup.bind();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnSaveDisputeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveDisputeActionPerformed
         saveDispute(true, false);
     }//GEN-LAST:event_btnSaveDisputeActionPerformed
 
     private void txtcadastreIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtcadastreIdActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_txtcadastreIdActionPerformed
 
     private void btnSearchPlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchPlotActionPerformed
         SearchPlot();
     }//GEN-LAST:event_btnSearchPlotActionPerformed
 
     private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
         if (disputeID != null && !disputeID.equals("")) {
             selectParty(null);
         } else {
             MessageUtility.displayMessage(ClientMessage.DISPUTE_SAVE_FIRST);
         }
     }//GEN-LAST:event_btnSelectActionPerformed
 
     private void btnAddPartyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPartyActionPerformed
         addParty();
     }//GEN-LAST:event_btnAddPartyActionPerformed
 
     private void btnsearchDisputeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnsearchDisputeActionPerformed
         searchDispute();
     }//GEN-LAST:event_btnsearchDisputeActionPerformed
 
     private void btnnewDisputeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnnewDisputeActionPerformed
         createNewDispute();
     }//GEN-LAST:event_btnnewDisputeActionPerformed
 
     private void btnCompleteDisputeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompleteDisputeActionPerformed
         completeDispute();
     }//GEN-LAST:event_btnCompleteDisputeActionPerformed
 
     private void btnDisputeModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisputeModeActionPerformed
         switchModeRole(true);
     }//GEN-LAST:event_btnDisputeModeActionPerformed
 
     private void btnCourtProcessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCourtProcessActionPerformed
         switchModeRole(false);
     }//GEN-LAST:event_btnCourtProcessActionPerformed
 
     private void btnRemoveDisputeCommentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveDisputeCommentActionPerformed
         removeComment();
     }//GEN-LAST:event_btnRemoveDisputeCommentActionPerformed
 
     private void btnAddComment1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddComment1ActionPerformed
         if (disputeID != null && !disputeID.equals("")) {
             addDisputeComments();
         } else {
             MessageUtility.displayMessage(ClientMessage.DISPUTE_SAVE_FIRST);
         }
     }//GEN-LAST:event_btnAddComment1ActionPerformed
 
     private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
         txtName1.setText(null);
         partySearchResult.getPartySearchResults().clear();
    }//GEN-LAST:event_btnClearActionPerformed
 
     private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
         searchParty();
     }//GEN-LAST:event_btnSearchActionPerformed
 
     private void dbxdisputeTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbxdisputeTypeActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_dbxdisputeTypeActionPerformed
 
     private void btnPrintConfirmationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintConfirmationActionPerformed
         if (btnDisputeMode.isSelected() && (disputeID != null || !disputeID.equals(""))) {
             printConfirmation();
         }
     }//GEN-LAST:event_btnPrintConfirmationActionPerformed
 
     private void btnRemovePartyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemovePartyActionPerformed
         removeParty();
     }//GEN-LAST:event_btnRemovePartyActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         if (disputeID != null && !disputeID.equals("")) {
             viewDisputeComments();
         } else {
             MessageUtility.displayMessage(ClientMessage.DISPUTE_SAVE_FIRST);
         }
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
         viewParty();
     }//GEN-LAST:event_btnViewActionPerformed
 
     private void dbxdisputeCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbxdisputeCategoryActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_dbxdisputeCategoryActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAddComment1;
     private javax.swing.JButton btnAddParty;
     private javax.swing.JButton btnClear;
     private javax.swing.JButton btnCompleteDispute;
     private javax.swing.JRadioButton btnCourtProcess;
     private javax.swing.JRadioButton btnDisputeMode;
     private javax.swing.JButton btnPrintConfirmation;
     private javax.swing.JButton btnRemoveDisputeComment;
     private javax.swing.JButton btnRemoveParty;
     private javax.swing.JButton btnSaveDispute;
     private javax.swing.JButton btnSearch;
     private javax.swing.JButton btnSearchPlot;
     private javax.swing.JButton btnSelect;
     private javax.swing.JButton btnView;
     private javax.swing.JButton btnnewDispute;
     private javax.swing.JButton btnsearchDispute;
     private org.sola.clients.beans.cadastre.CadastreObjectBean cadastreObjectBean1;
     private org.sola.clients.beans.cadastre.CadastreObjectSearchResultListBean cadastreObjectSearch;
     private javax.swing.JCheckBox cbxLAAPrimary;
     public javax.swing.JComboBox dbxdisputeCategory;
     public javax.swing.JComboBox dbxdisputeType;
     private org.sola.clients.beans.administrative.DisputeBean disputeBean1;
     private org.sola.clients.beans.referencedata.DisputeCategoryListBean disputeCategory;
     private org.sola.clients.beans.referencedata.DisputeTypeListBean disputeType;
     private org.sola.clients.swing.ui.source.DocumentsManagementPanel documentsManagementPanel;
     private org.sola.clients.swing.ui.GroupPanel groupPanel1;
     private org.sola.clients.swing.ui.GroupPanel groupPanel2;
     private org.sola.clients.swing.ui.GroupPanel groupPanel3;
     private org.sola.clients.swing.ui.GroupPanel groupPanel4;
     private javax.swing.JButton jButton1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel11;
     private javax.swing.JPanel jPanel12;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel14;
     private javax.swing.JPanel jPanel15;
     private javax.swing.JPanel jPanel16;
     private javax.swing.JPanel jPanel17;
     private javax.swing.JPanel jPanel18;
     private javax.swing.JPanel jPanel19;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel20;
     private javax.swing.JPanel jPanel23;
     private javax.swing.JPanel jPanel27;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel38;
     private javax.swing.JPanel jPanel39;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel40;
     private javax.swing.JPanel jPanel41;
     private javax.swing.JPanel jPanel42;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JToolBar.Separator jSeparator2;
     private javax.swing.JToolBar.Separator jSeparator3;
     private javax.swing.JToolBar.Separator jSeparator4;
     private javax.swing.JToolBar.Separator jSeparator5;
     private javax.swing.JToolBar.Separator jSeparator6;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTable jTable1;
     private javax.swing.JTable jTable2;
     private javax.swing.JTable jTable3;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JToolBar jToolBar3;
     private javax.swing.JToolBar jToolBar4;
     private javax.swing.JToolBar jToolBar6;
     private javax.swing.JLabel lblLeaseCategory1;
     private javax.swing.JLabel lblLodgementDate;
     private javax.swing.JLabel lblPlotLocation;
     private javax.swing.JLabel lblPlotNumber;
     private javax.swing.JLabel lblSearchResultNumber;
     private org.sola.clients.beans.party.PartySearchParamsBean partySearchParams;
     private org.sola.clients.beans.party.PartySearchResultListBean partySearchResult;
     private org.sola.clients.swing.ui.HeaderPanel pnlHeader;
     private javax.swing.JPanel pnlSearch;
     private javax.swing.JPanel tabGeneralInfo;
     private javax.swing.JTextPane txtActionRequired;
     private javax.swing.JTextField txtDisputeDescription;
     private javax.swing.JTextField txtName1;
     private javax.swing.JTextField txtcadastreId;
     private javax.swing.JTextField txtdisputeNumber;
     private javax.swing.JFormattedTextField txtlodgementDate;
     private javax.swing.JTextField txtplotLocation;
     private javax.swing.JTextField txtrrrId;
     private javax.swing.JTextField txtstatus;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 }
