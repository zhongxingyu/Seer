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
 
 package org.amanzi.neo.wizards;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.LinkedHashMap;
 
 import org.amanzi.neo.loader.core.preferences.DataLoadPreferences;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.loader.internal.NeoLoaderPluginMessages;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Traverser;
 
 
 /**
  * <p>
  * Wizard page for import dataset from url
  * </p>
  * 
  * @author NiCK
  * @since 1.0.0
  */
 public class DatasetImportUrlWizardPage extends WizardPage {
 
     /** The c dataset. */
     private Combo cDataset;
 
     /** The url. */
     private Text fUrl;
 
     /** The url. */
     private String url;
 
     /** The l url. */
     private Label lUrl;
     
     private DateTime startTimeWidget;
     private DateTime endTimeWidget;
     protected Date startDate;
     protected String startTime;
     protected String endTime;
     
     private Shell shell;
     
     private String imsi;
     private String imei;
     
     public LinkedHashMap<String, Node> dataset = new LinkedHashMap<String, Node>();
     
 
 
     /**
      * Instantiates a new dataset import url wizard page.
      * 
      * @param pageTitle the page title
      * @param pageDescr the page descr
      */
     public DatasetImportUrlWizardPage(String pageTitle, String pageDescr) {
         super(pageTitle);
         setTitle(pageTitle);
         setDescription(pageDescr);
     }
 
     /**
      * Creates the control.
      * 
      * @param parent the parent
      */
     @Override
     public void createControl(Composite parent) {
     	shell = parent.getShell();
     	imei = NeoLoaderPlugin.getDefault().getPreferenceStore().getString(DataLoadPreferences.USER_IMEI);
     	imsi = NeoLoaderPlugin.getDefault().getPreferenceStore().getString(DataLoadPreferences.USER_IMSI);
     	
         Composite main = new Composite(parent, SWT.FILL);
         main.setLayout(new GridLayout(2, false));
 
         lUrl = new Label(main, SWT.NONE);
         lUrl.setText("URL:");
         lUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 
         fUrl = new Text(main, SWT.NONE);
         fUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         fUrl.setEditable(true);
 
         loadUrl();
         if (url != null && !url.isEmpty())
             fUrl.setText(url);
         else
             fUrl.setText("not specified");
 
         fUrl.addModifyListener(new ModifyListener(){
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 				url = fUrl.getText();
 				validateFinish();
 			}
         	
         });
         
         Label ldataset = new Label(main, SWT.NONE);
         ldataset.setText(NeoLoaderPluginMessages.DriveDialog_DatasetLabel);
         ldataset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 
         cDataset = new Combo(main, SWT.NONE);
         GridData lData = new GridData(SWT.FILL, SWT.CENTER, false, false);
         lData.widthHint = 150;
         cDataset.setLayoutData(lData);
 
         Traverser allDatasetTraverser = NeoUtils.getAllDatasetTraverser(
                 NeoServiceProviderUi.getProvider().getService().getReferenceNode());
         
 
         for (Node node : allDatasetTraverser) {
             dataset.put((String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME), node);
         }
         
         String[] items = dataset.keySet().toArray(new String[0]);
         
         	
         Arrays.sort(items);
         cDataset.setItems(items);
         cDataset.addModifyListener(new ModifyListener() {
         	@Override
             public void modifyText(ModifyEvent e) {
         		validateFinish();
         	}
         });
         
         cDataset.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 setPageComplete(isValidPage());
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
 
         Label startLabel = new Label(main, SWT.NONE);
         startLabel.setText("Start date");
         
         /** The start time. */
         startTimeWidget = new DateTime(main, SWT.BORDER | SWT.DATE | SWT.LONG);
         startTimeWidget.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, true, false));
         startTimeWidget.setEnabled(true);
         startTimeWidget.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 setPageComplete(isValidPage());
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         
         startTimeWidget.setDay(startTimeWidget.getDay() - 6);
 
         Label endLabel = new Label(main, SWT.NONE);
         endLabel.setText("End date (inclusive)");
         
         /** The end time. */
         endTimeWidget = new DateTime(main, SWT.BORDER | SWT.DATE | SWT.LONG);
         endTimeWidget.setLayoutData(new GridData(SWT.NONE, SWT.CENTER, true, false));
         
         endTimeWidget.setEnabled(true);
         endTimeWidget.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
             	setPageComplete(isValidPage());
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         
 
         setControl(main);
         validateFinish();
     }
 
     /**
      * Load url.
      */
     private void loadUrl() {
         url = NeoLoaderPlugin.getDefault().getPreferenceStore().getString(DataLoadPreferences.REMOTE_SERVER_URL);
     }
 
     /**
      * Validate finish.
      */
     private void validateFinish() {
         setPageComplete(isValidPage());
     }
 
     /**
      * Checks if is valid page.
      * 
      * @return true, if is valid page
      */
     private boolean isValidPage() {
     	if (imei == null || imei.trim().isEmpty() || imsi == null || imsi.trim().isEmpty()){
     		MessageDialog.openInformation(shell, "Imei/Imsi not set", "Please set your imei and imsi in the preferences");
     		return false;
     	}
     	
     	if (cDataset.getText() == null)
     		return false;
     	
     	if (startTimeWidget.getYear() > endTimeWidget.getYear() ||
     		(startTimeWidget.getYear() == endTimeWidget.getYear() && startTimeWidget.getMonth() > endTimeWidget.getMonth()) ||
     		(startTimeWidget.getYear() == endTimeWidget.getYear() && startTimeWidget.getMonth() == endTimeWidget.getMonth() && startTimeWidget.getDay() >= endTimeWidget.getDay())){
 //    		MessageDialog.openInformation(shell, "Start and end date incorrect", "Start date must be gretaer than the end time");
     		return false;
     	}
     	
     	int startMonth =  startTimeWidget.getMonth() + 1;
    	int endMonth =  endTimeWidget.getMonth() + 1;
    	startTime = startTimeWidget.getYear() + "-" + (startMonth<10?"0"+startMonth:startMonth) + "-" + startTimeWidget.getDay();
		endTime = endTimeWidget.getYear() + "-" + (endMonth<10?"0"+endMonth:endMonth) + "-" + endTimeWidget.getDay();
 		
 		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 
 		
 		startDate = new Date();
 		try {
 			startDate = format.parse(startTime);
 		} catch (ParseException pe) {
 			pe.printStackTrace();
 		}
 
     	
         if (url != null && !url.isEmpty()) {
             try {
                 new URL(url);
             } catch (MalformedURLException e) {
                 lUrl.setForeground(new Color(null, 255, 0, 0));
                 return false;
             }
             return true;
         }
         return false;
     }
 
     /**
      * Gets the url.
      * 
      * @return the url
      */
     public String getUrl(boolean getTotalCount) {
     	if (getTotalCount){
     		return url + "/event/getEventsCount?dataset=" + imsi.trim().substring(0,5) + "&start=" + startTime + "&end=" + endTime;
     	}
     	//String completeUrl = url + "/event/extract.csv?dataset=" + imsi.trim().substring(0, 5) + "&imsi=" + imsi.trim() + "&imei=" + imei.trim();
     	String completeUrl = url + "/event/extractNew.csv?dataset=" + imsi.trim().substring(0, 5);
         return completeUrl;
     }
 
     /**
      * Gets the dataset.
      * 
      * @return the dataset
      */
     public String getDataset() {
         return cDataset.getText();
     }
     
     public Node getDatasetNode(String datasetName){
     	if (!dataset.containsKey(datasetName)){
     		return null;
     	}
     	
     	return dataset.get(datasetName);
     }
 
 }
