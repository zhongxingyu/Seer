 package edu.wustl.cab2b.client.ui.searchDataWizard;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.rmi.RemoteException;
 import java.util.Date;
 
 import javax.swing.JDialog;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import org.apache.log4j.Logger;
 
 import edu.wustl.cab2b.client.cache.UserCache;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTextField;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.mainframe.UserValidator;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.client.ui.util.WindowUtilities;
 import edu.wustl.cab2b.common.datalist.DataListBusinessInterface;
 import edu.wustl.cab2b.common.datalist.DataListHomeInterface;
 import edu.wustl.cab2b.common.datalist.IDataRow;
 import edu.wustl.cab2b.common.domain.DataListMetadata;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 
 /**
  * Panel used to display Save Data list dialog
  * @author deepak_shingan
  *
  */
 public class SaveDatalistPanel extends Cab2bPanel {
     private static final long serialVersionUID = 1L;
 
     private static final Logger logger = edu.wustl.common.util.logger.Logger.getLogger(SaveDatalistPanel.class);
 
     /**
      * Panel title label
      */
     private Cab2bLabel titleLabel;
 
     /**
      * Description label
      */
     private Cab2bLabel descLabel;
 
     /**
      * Text field for datalist name
      */
     private Cab2bTextField txtTitle;
 
     /**
      * Text area for datalist description
      */
     private JTextArea txtDesc;
 
     /**
      * Cancel button
      */
     private Cab2bButton cancelButton;
 
     /**
      * Save button
      */
     private Cab2bButton saveButton;
 
     /**
      * Center panel
      */
     private Cab2bPanel centerPanel;
 
     /**
      * Bottom panel
      */
     private Cab2bPanel bottomPanel;
 
     /**
      * Scrollpane for panel 
      */
     private JScrollPane srollPane;
 
     /**
      * Container dialog box
      */
     JDialog dialog;
 
     /**
      * MainSearch panel as parent
      */
     MainSearchPanel mainSearchPanel;
 
     /**
      * Flag  to indicate status of data list 
      */
     private static boolean isDataListSaved = false;
 
     /**
      * Creates SaveDatalistPanel with MainSearchPanel as parent panel
      * @param mainSearchpanel
      */
     SaveDatalistPanel(MainSearchPanel mainSearchpanel) {
         this.mainSearchPanel = mainSearchpanel;
         initGUIWithGBL();
     }
 
     /**
      * Initilizing GUI
      */
     private void initGUIWithGBL() {
         titleLabel = new Cab2bLabel("Title");
         descLabel = new Cab2bLabel("Description");
         txtTitle = new Cab2bTextField();
 
         txtDesc = new JTextArea();
         srollPane = new JScrollPane(txtDesc);
 
         cancelButton = new Cab2bButton("Cancel");
         cancelButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent arg0) {
                 logger.debug("disposing save data list dialog... ");
                 dialog.dispose();
             }
         });
 
         saveButton = new Cab2bButton("Save");
         saveButton.addActionListener(new SaveDataListActionListener());
 
         GridBagLayout gbl = new GridBagLayout();
         centerPanel = new Cab2bPanel();
         centerPanel.setLayout(gbl);
 
         this.setLayout(new BorderLayout());
         GridBagConstraints gbc = new GridBagConstraints();
 
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.insets = new Insets(5, 5, 5, 5);
         centerPanel.add(titleLabel, gbc);
 
         gbc.gridx = 1;
         gbc.gridy = 0;
         gbc.gridwidth = 4;
         gbc.gridheight = 1;
         gbc.insets = new Insets(5, 5, 5, 5);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         centerPanel.add(txtTitle, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 1;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
 
         gbc.insets = new Insets(5, 5, 5, 5);
         gbc.fill = GridBagConstraints.NONE;
         centerPanel.add(descLabel, gbc);
 
         gbc.gridx = 1;
         gbc.gridy = 1;
         gbc.gridwidth = 4;
         gbc.gridheight = 3;
         gbc.insets = new Insets(5, 5, 5, 5);
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.fill = GridBagConstraints.BOTH;
         centerPanel.add(srollPane, gbc);
 
         bottomPanel = new Cab2bPanel();
         FlowLayout flowLayout = new FlowLayout();
         flowLayout.setAlignment(FlowLayout.RIGHT);
         flowLayout.setHgap(10);
         bottomPanel.setLayout(flowLayout);
         bottomPanel.add(saveButton);
         bottomPanel.add(cancelButton);
 
         this.add(centerPanel, BorderLayout.CENTER);
         this.add(bottomPanel, BorderLayout.SOUTH);
     }
 
     /**
      * Shows panel in Dialog box container
      * @return
      */
     public JDialog showInDialog() {
         Dimension dimension = MainFrame.getScreenDimesion();
         dialog = WindowUtilities.setInDialog(NewWelcomePanel.getMainFrame(), this, "Save Data List",
                                              new Dimension((int) (dimension.width * 0.35),
                                                      (int) (dimension.height * 0.30)), true, false);
 
         logger.debug("dialog initialized ########## " + dialog);
         dialog.setVisible(true);
         return dialog;
     }
 
     /**
      * @return the isDataListSaved
      */
     public static boolean isDataListSaved() {
         return isDataListSaved;
     }
 
     /**
      * @param isDataListSaved the isDataListSaved to set
      */
     public static void setDataListSaved(boolean isDataListSaved) {
         SaveDatalistPanel.isDataListSaved = isDataListSaved;
     }
 
     class SaveDataListActionListener implements ActionListener {
         public void actionPerformed(ActionEvent arg0) {
             String dataListName = txtTitle.getText();
             if (dataListName == null || dataListName.equals("")) {
                 dataListName = "" + new Date();
             }
             String dataListDesc = txtDesc.getText();
 
             final DataListMetadata dataListAnnotation = new DataListMetadata();
             dataListAnnotation.setName(dataListName);
             dataListAnnotation.setDescription(dataListDesc);
             dataListAnnotation.setCreatedOn(new Date());
             dataListAnnotation.setLastUpdatedOn(new Date());
             dataListAnnotation.setUserId(UserCache.getInstance().getCurrentUser().getUserId());
 
             final IDataRow newRootDataRow = processNode(MainSearchPanel.getDataList().getRootDataRow());
             MainSearchPanel.getDataList().setDataListAnnotation(dataListAnnotation);
 
             isDataListSaved = false;
             CustomSwingWorker sw = new CustomSwingWorker(SaveDatalistPanel.this) {
                 @Override
                 protected void doNonUILogic() throws RuntimeException {
                     DataListBusinessInterface dataListBI = (DataListBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                         EjbNamesConstants.DATALIST_BEAN,
                                                                                                                         DataListHomeInterface.class,
                                                                                                                         SaveDatalistPanel.this);
                     try {
                         MainSearchPanel.savedDataListMetadata = dataListBI.saveDataList(
                                                                                         newRootDataRow,
                                                                                         dataListAnnotation,
                                                                                         UserValidator.getSerializedDelegatedCredReference(),
                                                                                         UserValidator.getIdP());
                         logger.debug("data list saved successfully (in entity with id) : "
                                 + MainSearchPanel.savedDataListMetadata.getId());
 
                         isDataListSaved = true;
                     } catch (RemoteException e) {
                         CommonUtils.handleException(e, NewWelcomePanel.getMainFrame(), true, true, true, false);
                     } finally {
                         dialog.dispose();
                     }
                 }
 
                 @Override
                 protected void doUIUpdateLogic() throws RuntimeException {
                     if (isDataListSaved) {
                        SearchNavigationPanel.getMessageLabel().setText("* Data list saved successfully *.");
                         updateUI();
                     }
                 }
             };
             sw.start();
         }
 
         /**
          * This method takes a data row and makes copy of it.
          * It then calls itself recursively on each child. If both (data row) and (child node) is not a title node then 
          * it adds a title node between itself and child.
          *   
          * so final  structure of data list is always such that each node has title node.
          * @param dataRow
          * @return
          */
         private IDataRow processNode(IDataRow dataRow) {
             IDataRow newDataRow = dataRow.getCopy();
 
             for (IDataRow childRow : dataRow.getChildren()) {
                 IDataRow newChildRow = processNode(childRow);
                 if ((newDataRow.getEntity() == null && newChildRow.isData())
                         || (newDataRow.isData() && newChildRow.isData())) {
                     IDataRow newTitleNode = newChildRow.getTitleNode();
                     newTitleNode.addChild(newChildRow);
                     newDataRow.addChild(newTitleNode);
                 } else {
                     newDataRow.addChild(newChildRow);
                 }
             }
 
             return newDataRow;
         }
     }
 }
