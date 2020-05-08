 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.report.grid.wizards;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.amanzi.awe.report.grid.wizards.GridReportWizard.AnalysisType;
 import org.amanzi.awe.report.grid.wizards.GridReportWizard.CategoryType;
 import org.amanzi.awe.report.grid.wizards.GridReportWizard.Scope;
 import org.amanzi.awe.report.pdf.PDFPrintingEngine;
 import org.amanzi.awe.statistic.CallTimePeriods;
 import org.amanzi.neo.db.manager.NeoServiceProvider;
 import org.amanzi.neo.loader.ui.utils.LoaderUiUtils;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.utils.Pair;
 import org.amanzi.neo.services.utils.Utils;
 import org.eclipse.jface.preference.DirectoryFieldEditor;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Group;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 
 import com.google.protobuf.DescriptorProtos.FieldOptions.CType;
 
 /**
  * Page for Grid Report Wizard
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public class SelectAnalysisTypePage extends WizardPage {
 
     private Button btnAll;
     private Button btnIndividual;
     private Button btnTop10;
     private Button btnTop20;
     private Button btnTop30;
     private Button btnKPI;
     private Button btnSystem;
     private Button btnSystemEvents;
 
     public SelectAnalysisTypePage() {
         super(SelectAnalysisTypePage.class.getName());
     }
 
     @Override
     public void createControl(Composite parent) {
         
         
         Composite container = new Composite(parent, SWT.NONE);
         container.setLayout(new GridLayout());
 
         Group groupContainer = new Group(container, SWT.NONE);
         groupContainer.setText("Select type of analysis:");
         groupContainer.setLayout(new GridLayout());
 
         btnKPI = new Button(groupContainer, SWT.RADIO);
         btnKPI.setText("KPI");
         btnKPI.setLayoutData(new GridData());
         btnKPI.setSelection(true);
 
         btnSystem = new Button(groupContainer, SWT.RADIO);
         btnSystem.setText("System summary");
         btnSystem.setLayoutData(new GridData());
 
         btnSystemEvents = new Button(groupContainer, SWT.RADIO);
         btnSystemEvents.setText("System events");
         btnSystemEvents.setLayoutData(new GridData());
         btnSystemEvents.setEnabled(false);
 
 //        Group resultType = new Group(container, SWT.NONE);
 //        resultType.setText("Select which results to export:");
 //        resultType.setLayout(new GridLayout());
 //
 //        btnAll = new Button(resultType, SWT.RADIO);
 //        btnAll.setText("all sites/cells");
 //        btnAll.setLayoutData(new GridData());
 //        btnAll.setSelection(true);
 //
 //        btnIndividual = new Button(resultType, SWT.RADIO);
 //        btnIndividual.setText("selected sites/cells");
 //        btnIndividual.setLayoutData(new GridData());
 
 //        btnTop10 = new Button(resultType, SWT.RADIO);
 //        btnTop10.setText("worst 10 sites/cells");
 //        btnTop10.setLayoutData(new GridData());
 //
 //        btnTop20 = new Button(resultType, SWT.RADIO);
 //        btnTop20.setText("worst 20 sites/cells");
 //        btnTop20.setLayoutData(new GridData());
 //
 //        btnTop30 = new Button(resultType, SWT.RADIO);
 //        btnTop30.setText("worst 30 sites/cells");
 //        btnTop30.setLayoutData(new GridData());
 
         setPageComplete(true);
         setControl(container);
     }
 
     @Override
     public IWizardPage getNextPage() {
 
         updateAnalysisType();
 //        updateScope();
         GridReportWizard gridReportWizard = ((GridReportWizard)getWizard());
         System.out.println("Analysis type "+gridReportWizard.getAnalysisType()+"\t scope "+gridReportWizard.getScope());
         if (btnKPI.getSelection()) {
             return gridReportWizard.getSelectCategoryPage();
         } else if (btnSystem.getSelection()) {
             gridReportWizard.setCategoryType(CategoryType.SYSTEM);
             gridReportWizard.setSelection(Arrays.asList("unknown"));
             return gridReportWizard.getSystemSummaryPage();
         }
         return super.getNextPage();
     }
 
     /**
      * @param gridReportWizard
      */
     private void updateAnalysisType() {
         GridReportWizard gridReportWizard = ((GridReportWizard)getWizard());
         if (btnKPI.getSelection()) {
             gridReportWizard.setAnalysisType(AnalysisType.KPI);
         } else if (btnSystem.getSelection()) {
             gridReportWizard.setAnalysisType(AnalysisType.SYSTEM_SUMMARY);
         } else if (btnSystemEvents.getSelection()) {
             gridReportWizard.setAnalysisType(AnalysisType.SYSTEM_EVENTS);
         }
     }
 
     /**
      * @param gridReportWizard
      */
     private void updateScope() {
         GridReportWizard gridReportWizard = ((GridReportWizard)getWizard());
         if (btnAll.getSelection()) {
             gridReportWizard.setScope(Scope.ALL);
         } else if (btnIndividual.getSelection()) {
             gridReportWizard.setScope(Scope.SELECTED);
         } /*else if (btnTop10.getSelection()) {
             gridReportWizard.setScope(Scope.WORST_10);
         } else if (btnTop10.getSelection()) {
             gridReportWizard.setScope(Scope.WORST_20);
         } else if (btnTop30.getSelection()) {
             gridReportWizard.setScope(Scope.WORST_30);
         }*/
     }
 
     @Override
     public void setVisible(boolean visible) {
         GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
         Collection<Node> allOss = Utils.getAllOss(service);
         System.out.println("oss: "+allOss);
         if (!allOss.isEmpty()){
             Node node = allOss.iterator().next();
            Pair<Long, Long> time = Utils.getMinMaxTimeOfDataset(node, service);
             System.out.println("time: "+time.l()+" - "+time.r());
         }
         super.setVisible(visible);
     }
 
 }
