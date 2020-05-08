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
 
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 
 import org.amanzi.awe.report.grid.wizards.GridReportWizard.PeriodType;
 import org.amanzi.awe.report.pdf.PDFPrintingEngine;
 import org.amanzi.awe.statistic.CallTimePeriods;
 import org.amanzi.neo.db.manager.NeoServiceProvider;
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
 import org.eclipse.swt.widgets.Label;
 import org.jfree.data.time.Hour;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 
 /**
  * Page for Grid Report Wizard
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public class SelectPeriodPage extends WizardPage {
 
     private DateTime dateE;
     private DateTime timeE;
     private DateTime time;
     private DateTime date;
     private Button btnDaily;
     private Button btnHourly;
 
     public SelectPeriodPage() {
         super(SelectPeriodPage.class.getName());
     }
 
     @Override
     public void createControl(Composite parent) {
         Composite container = new Composite(parent, SWT.NONE);
         container.setLayout(new GridLayout(4, false));
 
         Group groupContainer = new Group(container, SWT.NONE);
         groupContainer.setText("Period selection:");
         groupContainer.setLayout(new GridLayout(2, false));
 
         btnDaily = new Button(groupContainer, SWT.RADIO);
         btnDaily.setText("daily");
         btnDaily.setLayoutData(new GridData());
         btnDaily.setSelection(true);
 
         btnHourly = new Button(groupContainer, SWT.RADIO);
         btnHourly.setText("hourly");
         btnHourly.setLayoutData(new GridData());
         btnHourly.setEnabled(true);
 
         Label lblStartDate = new Label(groupContainer, SWT.NONE);
         lblStartDate.setText("Start date:");
         lblStartDate.setLayoutData(new GridData());
 
         date = new DateTime(groupContainer, SWT.DATE | SWT.LONG);
         date.setLayoutData(new GridData());
         date.setEnabled(true);
 
         Label lblEmpty1 = new Label(groupContainer, SWT.NONE);
         lblEmpty1.setText("");
         lblEmpty1.setLayoutData(new GridData());
 
         time = new DateTime(groupContainer, SWT.TIME | SWT.LONG);
         time.setLayoutData(new GridData());
         time.setEnabled(true);
 
         Label lblEndDate = new Label(groupContainer, SWT.NONE);
         lblEndDate.setText("End date:");
         lblEndDate.setLayoutData(new GridData());
 
         dateE = new DateTime(groupContainer, SWT.DATE | SWT.LONG);
         dateE.setLayoutData(new GridData());
         dateE.setEnabled(true);
 
         Label lblEmpty2 = new Label(groupContainer, SWT.NONE);
         lblEmpty2.setText("");
         lblEmpty2.setLayoutData(new GridData());
 
         timeE = new DateTime(groupContainer, SWT.TIME | SWT.LONG);
         timeE.setLayoutData(new GridData());
         timeE.setEnabled(true);
 
         setPageComplete(true);
         setControl(container);
     }
 
     @Override
     public void setVisible(boolean visible) {
         if (visible) {
             GridReportWizard gridReportWizard = ((GridReportWizard)getWizard());
             gridReportWizard.loadData();
 
            Pair<Long, Long> dsTime = Utils.getMinMaxTimeOfDataset(gridReportWizard.getDatasetNode());
             Long start = dsTime.l();
             Long end = dsTime.r();
             GregorianCalendar cal = new GregorianCalendar();
             cal.setTimeInMillis(start);
             System.out.println("time: " + start + " - " + end);
             date.setDay(cal.get(Calendar.DAY_OF_MONTH));
             date.setMonth(cal.get(Calendar.MONTH));
             date.setYear(cal.get(Calendar.YEAR));
 
             time.setHours(cal.get(Calendar.HOUR_OF_DAY));
             time.setMinutes(cal.get(Calendar.MINUTE));
             time.setSeconds(0);
 
             cal.setTimeInMillis(end);
             dateE.setDay(cal.get(Calendar.DAY_OF_MONTH));
             dateE.setMonth(cal.get(Calendar.MONTH));
             dateE.setYear(cal.get(Calendar.YEAR));
 
             timeE.setHours(cal.get(Calendar.HOUR_OF_DAY));
             timeE.setMinutes(cal.get(Calendar.MINUTE));
             timeE.setSeconds(0);
         }
         super.setVisible(visible);
     }
 
     @Override
     public IWizardPage getNextPage() {
         GridReportWizard gridReportWizard = ((GridReportWizard)getWizard());
         if (btnDaily.getSelection()) {
             gridReportWizard.setAggregation(CallTimePeriods.DAILY);
 
         } else if (btnHourly.getSelection()) {
             gridReportWizard.setAggregation(CallTimePeriods.HOURLY);
         }
         GregorianCalendar cal = new GregorianCalendar();
         cal.set(Calendar.YEAR, date.getYear());
         cal.set(Calendar.MONTH, date.getMonth());
         cal.set(Calendar.DAY_OF_MONTH, date.getDay());
         cal.set(Calendar.HOUR_OF_DAY, time.getHours());
         cal.set(Calendar.MINUTE, time.getMinutes());
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         long start=cal.getTimeInMillis();
         cal.set(Calendar.YEAR, dateE.getYear());
         cal.set(Calendar.MONTH, dateE.getMonth());
         cal.set(Calendar.DAY_OF_MONTH, dateE.getDay());
         cal.set(Calendar.HOUR_OF_DAY, timeE.getHours());
         cal.set(Calendar.MINUTE, timeE.getMinutes());
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         long end=cal.getTimeInMillis();
         gridReportWizard.setStartTime(start);
         gridReportWizard.setEndTime(end);
         System.out.println("Start date: "+start+"\tEnd date: "+end);
         return super.getNextPage();
     }
 
 }
