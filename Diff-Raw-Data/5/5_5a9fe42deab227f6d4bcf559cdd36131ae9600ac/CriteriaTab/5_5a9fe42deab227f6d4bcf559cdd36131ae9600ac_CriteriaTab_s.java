 package edu.mayo.phenoportal.client.phenotype.report;
 
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.types.Overflow;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.Button;
 import com.smartgwt.client.widgets.HTMLPane;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 import com.smartgwt.client.widgets.tab.Tab;
 
 import edu.mayo.phenoportal.client.Htp;
 import edu.mayo.phenoportal.client.core.AlgorithmData;
 import edu.mayo.phenoportal.client.events.PhenotypeExecuteCompletedEvent;
 import edu.mayo.phenoportal.client.events.PhenotypeExecuteCompletedEventHandler;
 import edu.mayo.phenoportal.client.phenotype.PhenotypeService;
 import edu.mayo.phenoportal.client.phenotype.PhenotypeServiceAsync;
 import edu.mayo.phenoportal.client.utils.ModalWindow;
 import edu.mayo.phenoportal.shared.Execution;
 import edu.mayo.phenoportal.shared.ValueSet;
 
 /**
  * Tab for displaying the Criteria info for the selected phenotype.
  */
 public class CriteriaTab extends Tab implements ReportTab {
 
     private static final String INITIAL_VERSION = "Set Value Sets to Original Version";
     private static final String LAST_EXECUTED_VERSION = "Set Value Sets to Last Exectued Version";
 
     private AlgorithmData i_algorithmData;
     private final VLayout i_criteriaLayout;
     private HLayout i_buttonLayout;
     private Button i_versionToggleButton;
     // private CriteriaPanel i_criteriaPanel;
     private final CriteriaHTMLPage i_criteriaHTMLPage;
     private final DataCriteriaListGrid i_dataCriteriaListGrid;
     private final SupplementalDataListGrid i_supplementalDataListGrid;
     private Execution i_lastExecution;
     private List<ValueSet> i_lastExecutionValueSets;
     private boolean userPermitted = false;
 
     public CriteriaTab(String title) {
         super(title);
 
         userPermitted = Htp.getLoggedInUser() != null && Htp.getLoggedInUser().getRole() <= 2;
 
         // overall layout that holds everything in the criteria tab.
         i_criteriaLayout = new VLayout();
         i_criteriaLayout.setWidth100();
         i_criteriaLayout.setHeight100();
 
         i_buttonLayout = createButtonLayout();
 
         i_criteriaHTMLPage = new CriteriaHTMLPage();
         i_dataCriteriaListGrid = new DataCriteriaListGrid();
         i_supplementalDataListGrid = new SupplementalDataListGrid();
 
         HTMLPane dataCriteriaTitle = getHTMLTitle("Data Criteria (QDM Data Elements)");
         HTMLPane supplementalDataTitle = getHTMLTitle("Supplemental Data Elements");
 
         i_criteriaLayout.addMember(i_criteriaHTMLPage);
         i_criteriaLayout.addMember(i_buttonLayout);
         i_criteriaLayout.addMember(dataCriteriaTitle);
         i_criteriaLayout.addMember(i_dataCriteriaListGrid);
         i_criteriaLayout.addMember(supplementalDataTitle);
         i_criteriaLayout.addMember(i_supplementalDataListGrid);
 
         setPane(i_criteriaLayout);
 
         createPhenotypeExecuteCompletedEventHandler();
     }
 
     private HLayout createButtonLayout() {
 
         i_buttonLayout = new HLayout();
         i_buttonLayout.setWidth100();
         i_buttonLayout.setHeight(40);
         i_buttonLayout.setMargin(25);
         i_buttonLayout.setAlign(Alignment.CENTER);
 
         i_versionToggleButton = new Button();
         i_versionToggleButton.setTitle(INITIAL_VERSION);
         i_versionToggleButton.setWidth(300);
 
         // show button if user is logged in.
         i_versionToggleButton.setVisible(userPermitted);
 
         i_versionToggleButton.addClickHandler(new ClickHandler() {
 
             @Override
             public void onClick(ClickEvent event) {
                 if (i_versionToggleButton.getTitle().equals(INITIAL_VERSION)) {
                     displayInitalVersion();
                 } else {
                     displayLastExecutedVersion();
                 }
             }
         });
 
         i_buttonLayout.addMember(i_versionToggleButton);
 
         return i_buttonLayout;
     }
 
     /**
      * Update value set tables. Display the last executed versions.
      */
     private void displayLastExecutedVersion() {
 
         // Set the busy indicator to show while updating the value sets.
         final ModalWindow busyIndicator = new ModalWindow(i_criteriaLayout, 40, "#dedede");
         busyIndicator.setLoadingIcon("loading_circle.gif");
         busyIndicator.show("Updating Value Set versions...", true);
 
         try {
             i_versionToggleButton.setDisabled(true);
 
             i_dataCriteriaListGrid.updateToLastExecution(i_lastExecutionValueSets);
             i_supplementalDataListGrid.updateToLastExecution(i_lastExecutionValueSets);
 
             // toggle to display initial (first) version
             i_versionToggleButton.setTitle(INITIAL_VERSION);
             i_versionToggleButton.setDisabled(false);
         } finally {
             busyIndicator.hide();
         }
     }
 
     /**
      * Update value set tables. Display the initial versions.
      */
     private void displayInitalVersion() {
 
         // Set the busy indicator to show while updating the value sets.
         final ModalWindow busyIndicator = new ModalWindow(i_criteriaLayout, 40, "#dedede");
         busyIndicator.setLoadingIcon("loading_circle.gif");
         busyIndicator.show("Updating Value Set versions...", true);
 
         try {
             i_versionToggleButton.setDisabled(true);
 
             // update tables with initial version
             i_dataCriteriaListGrid.update(i_algorithmData);
             i_supplementalDataListGrid.update(i_algorithmData);
 
             // toggle to display the last executed version
             i_versionToggleButton.setTitle(LAST_EXECUTED_VERSION);
             i_versionToggleButton.setDisabled(false);
         } finally {
             busyIndicator.hide();
         }
 
     }
 
     /**
      * Retrieve the new algorithm criteria info from the DB and display it.
      * 
      * @param algorithmData
      */
     public void updateSelection(AlgorithmData algorithmData) {
         i_algorithmData = algorithmData;
         i_criteriaHTMLPage.udpateHTMLPage(i_algorithmData);
         i_dataCriteriaListGrid.update(i_algorithmData);
         i_supplementalDataListGrid.update(i_algorithmData);
     }
 
     @Override
     public void clearTab() {
         // i_criteriaPanel.getHtmlPane().setContents("");
     }
 
     private HTMLPane getHTMLTitle(String title) {
         HTMLPane htmlTitle = new HTMLPane();
         htmlTitle.setContents("</hr><div></br> <b>" + title + "<b></div>");
         htmlTitle.setWidth100();
         htmlTitle.setHeight100();
         htmlTitle.setOverflow(Overflow.VISIBLE);
 
         return htmlTitle;
     }
 
     /**
      * Listen for when a phenotype execution completes. Get the execution and
      * update that last run value sets.
      */
 
     private void createPhenotypeExecuteCompletedEventHandler() {
         Htp.EVENT_BUS.addHandler(PhenotypeExecuteCompletedEvent.TYPE,
                 new PhenotypeExecuteCompletedEventHandler() {
 
                     @Override
                     public void onPhenotypeExecuteCompleted(
                             PhenotypeExecuteCompletedEvent phenotypeExecuteCompletedEvent) {
                         if (phenotypeExecuteCompletedEvent.getExecuteSuccess()) {
 
                             updateValueSetsToLastExecuted();
                         }
                     }
 
                 });
     }
 
     /**
      * Update/default the versions to the versions that were last executed by
      * this user.
      * 
      * @param execution
      */
     public void setVersionsFromLastExecution(Execution execution) {
         System.out.println("Criteria Tab --- setVersionsFromLastExecution() called.");
         i_lastExecution = execution;
 
        if (i_lastExecution != null) {
             PhenotypeServiceAsync phenotypeService = GWT.create(PhenotypeService.class);
             phenotypeService.getExecutionValueSets(i_lastExecution.getId(),
                     new AsyncCallback<List<ValueSet>>() {
                         @Override
                         public void onFailure(Throwable caught) {
                            SC.warn("Failed to retrieve value sets for the last executed algorithm.");
                         }
 
                         @Override
                         public void onSuccess(List<ValueSet> valueSets) {
                             i_lastExecutionValueSets = valueSets;
 
                             i_dataCriteriaListGrid.updateToLastExecution(valueSets);
                             i_supplementalDataListGrid.updateToLastExecution(valueSets);
                         }
                     });
 
         }
 
     }
 
     private void updateValueSetsToLastExecuted() {
         if (Htp.getLoggedInUser() != null) {
             PhenotypeServiceAsync phenotypeService = GWT.create(PhenotypeService.class);
             phenotypeService.getLatestExecution(i_algorithmData.getAlgorithmName(), i_algorithmData
                     .getAlgorithmVersion(), i_algorithmData.getParentId(), Htp.getLoggedInUser()
                     .getUserName(), new AsyncCallback<Execution>() {
                 @Override
                 public void onFailure(Throwable caught) {
                     // do nothing.
                 }
 
                 @Override
                 public void onSuccess(Execution execution) {
                     setVersionsFromLastExecution(execution);
                 }
             });
         }
     }
 }
