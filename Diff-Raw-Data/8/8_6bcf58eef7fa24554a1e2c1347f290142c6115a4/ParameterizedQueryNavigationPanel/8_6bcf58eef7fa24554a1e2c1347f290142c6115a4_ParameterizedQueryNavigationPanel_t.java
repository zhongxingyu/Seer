 package edu.wustl.cab2b.client.ui.parameterizedQuery;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 
 import javax.swing.JOptionPane;
 
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.RiverLayout;
 import edu.wustl.cab2b.client.ui.main.AbstractTypePanel;
 import edu.wustl.cab2b.client.ui.mainframe.UserValidator;
 import edu.wustl.cab2b.client.ui.mainframe.stackbox.SavedQueryLinkPanel;
 import edu.wustl.cab2b.client.ui.searchDataWizard.SearchNavigationPanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.ejb.queryengine.QueryEngineBusinessInterface;
 import edu.wustl.cab2b.common.ejb.queryengine.QueryEngineHome;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 import edu.wustl.common.querysuite.queryobject.IParameterizedQuery;
 
 /**
  * Panel situated at bottom side of ParameterizedQueryMainPanel
  * 
  * @author deepak_shingan
  * 
  */
 public class ParameterizedQueryNavigationPanel extends Cab2bPanel {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Button for ordering parameterized conditions 
      */
     private Cab2bButton orderViewButton;
 
     /**
      * Button for saving conditions
      */
     private Cab2bButton saveButton;
 
     /**
      * Button for Canceling 
      */
     private Cab2bButton cancelButton;
 
     /**
      * ParameterizedQueryMainPanel referance used as parent component for exception handling
      */
     private ParameterizedQueryMainPanel parameterizedQueryMainPanel;
 
     /**
      * Constructor
      * @param ParameterizedQueryMainPanel panel
      */
     public ParameterizedQueryNavigationPanel(ParameterizedQueryMainPanel panel) {
         parameterizedQueryMainPanel = panel;
         iniGUI();
     }
 
     /**
      * @return IParameterizedQuery
      */
     public IParameterizedQuery getQuery() {
         return parameterizedQueryMainPanel.getParameterizedQueryDataModel().getQuery();
     }
 
     /**
      * Method to generate UI
      */
     private void iniGUI() {
         this.setLayout(new RiverLayout(5, 10));
         this.setBackground(new Color(240, 240, 240));
         orderViewButton = new Cab2bButton("Order View");
         orderViewButton.addActionListener(new OrderViewButtonActionListener());
         orderViewButton.setPreferredSize(new Dimension(120, 22));
         saveButton = new Cab2bButton("Save");
         saveButton.addActionListener(new SaveButtonActionListener());
         cancelButton = new Cab2bButton("Cancel");
         cancelButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg0) {
                 parameterizedQueryMainPanel.getDialog().dispose();
             }
         });
 
         this.add(" right ", orderViewButton);
         this.add(" right ", saveButton);
         this.add(" right ", cancelButton);
         this.add("br ", new Cab2bLabel());
     }
 
     /**
      * Action class for order button
      * 
      * @author deepak_shingan
      * 
      */
     private class OrderViewButtonActionListener implements ActionListener {
         public void actionPerformed(ActionEvent arg0) {
             if (parameterizedQueryMainPanel.getParameterConditionPanel().getCheckedAttributePanels(
                                                                                                    parameterizedQueryMainPanel.getParameterConditionPanel().getConditionPanel()).size() > 1) {
                 ParameterizedQueryOrderPanel panel = new ParameterizedQueryOrderPanel(parameterizedQueryMainPanel);
                 parameterizedQueryMainPanel.getDialog().dispose();
                 panel.showInDialog("Unsaved Conditions");
             } else {
                 JOptionPane.showMessageDialog(parameterizedQueryMainPanel,
                                               "Please select atleast two conditions for order view.", "Warning",
                                               JOptionPane.ERROR_MESSAGE);
                 return;
             }
         }
     }
 
     /**
      * Action listener for save button
      * @author deepak_shingan
      *
      */
     private class SaveButtonActionListener implements ActionListener {
         public void actionPerformed(ActionEvent arg0) {
             Cab2bPanel condtionPanel = parameterizedQueryMainPanel.getParameterConditionPanel().getConditionPanel();
             ParameterizedQueryDataModel parameterizedQueryDataModel = parameterizedQueryMainPanel.getParameterizedQueryDataModel();
             boolean validCondition = false;
             for (int index = 0; index < condtionPanel.getComponentCount(); index++) {
                 if (condtionPanel.getComponent(index) instanceof AbstractTypePanel) {
                     AbstractTypePanel panel = (AbstractTypePanel) condtionPanel.getComponent(index);
                     int conditionStatus = panel.isConditionValidBeforeSaving(parameterizedQueryMainPanel);
                     if (conditionStatus == 0) {
                         validCondition = true;
                         parameterizedQueryDataModel.addCondition(
                                                                  panel.getExpressionId(),
                                                                  panel.getCondition(
                                                                                     index,
                                                                                     ParameterizedQueryNavigationPanel.this));
                     } else if (conditionStatus == 1) {
                         parameterizedQueryDataModel.removeCondition(
                                                                     panel.getExpressionId(),
                                                                     panel.getCondition(
                                                                                        index,
                                                                                        ParameterizedQueryNavigationPanel.this));
                     } else
                         return;
                 }
             }
 
             if (!validCondition) {
                 JOptionPane.showMessageDialog(parameterizedQueryMainPanel,
                                               "Please add atleast one condition before saving.", "Error",
                                               JOptionPane.ERROR_MESSAGE);
                 return;
             }
             ParameterizedQueryInfoPanel parameterizedQueryInfoPanel = parameterizedQueryMainPanel.getInformationQueryPanel();
             parameterizedQueryDataModel.setQueryDescription(parameterizedQueryInfoPanel.getQueryDecription());
 
             if (!parameterizedQueryInfoPanel.getQueryName().equals(""))
                 parameterizedQueryDataModel.setQueryName(parameterizedQueryInfoPanel.getQueryName());
             else {
                 parameterizedQueryDataModel.setQueryName((new Date().toString()));
             }
 
             saveQuery();
         }
 
         /**
          * Method to save parameterized query
          */
         private void saveQuery() {
             ICab2bQuery cab2bParameterizedQuery = parameterizedQueryMainPanel.getParameterizedQueryDataModel().getQuery();
             try {
                 QueryEngineBusinessInterface queryEngineBusinessInterface = (QueryEngineBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                                             EjbNamesConstants.QUERY_ENGINE_BEAN,
                                                                                                                                             QueryEngineHome.class);
 
                 String message = null;
 
                /*if (cab2bParameterizedQuery.getId() != null) {
                     queryEngineBusinessInterface.updateQuery(cab2bParameterizedQuery);
 
                     message = "Query updated successfully.";
                } else {*/
                     if (queryEngineBusinessInterface.isQueryNameDuplicate(cab2bParameterizedQuery.getName())) {
                         JOptionPane.showMessageDialog(
                                                       parameterizedQueryMainPanel,
                                                       "Query with same name already exist. Please try saving with different name.",
                                                       "Error", JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                     queryEngineBusinessInterface.saveQuery(cab2bParameterizedQuery,
                                                            UserValidator.getSerializedDCR(),
                                                            UserValidator.getIdP());
                     message = "Query saved successfully.";
                //}
                 SearchNavigationPanel.getMessageLabel().setText(message);
                 new SavedQueryLinkPanel().updateQueryLinkPanel();
                 updateUI();
                 parameterizedQueryMainPanel.getDialog().dispose();
             } catch (Exception e) {
                 CommonUtils.handleException(new RuntimeException(e.getMessage(), ErrorCodeConstants.DB_0005),
                                             parameterizedQueryMainPanel, true, true, true, false);
                 parameterizedQueryMainPanel.getDialog().dispose();
             }
         }
     }
 }
