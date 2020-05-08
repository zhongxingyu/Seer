 /*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
 *******************************************************************************/
 
 package org.eclipse.imp.releng.dialogs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.imp.releng.ReleaseTool;
 import org.eclipse.imp.releng.WorkbenchReleaseTool;
 import org.eclipse.imp.releng.metadata.FeatureInfo;
 import org.eclipse.imp.releng.metadata.UpdateSiteInfo;
 import org.eclipse.imp.releng.metadata.UpdateSiteInfo.FeatureRef;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class RetrieveUpdateSiteDialog extends Dialog {
 	private Combo fProviderCombo;
 	private Text fRepoServerText;
 	private Text fRepoPathText;
 	private Text fProjectNameText;
 	private String fProvider;
 	private String fRepoServer;
 	private String fRepoPath;
 	private String fUpdateSiteProjectName;
 	private boolean fRetrieveFeatures;
 	private boolean fRetrievePlugins;
 
 	public RetrieveUpdateSiteDialog(Shell shell) {
         super(shell);
     }
 
     private final static Map<String,String> sProviderMap= new HashMap<String, String>();
 
     static {
     	sProviderMap.put("CVS", ReleaseTool.CVS_UPDATE_SITE_PROVIDER);
     	sProviderMap.put("SVN", ReleaseTool.SVN_UPDATE_SITE_PROVIDER);
     }
 
     private static class PrefabDescriptor {
     	public String providerType;
     	public String repoServer;
     	public String repoPath;
     	public String updateSiteProject;
 
     	public PrefabDescriptor(String pt, String rs, String rp, String usp) {
     	    providerType= pt;
     	    repoServer= rs;
     	    repoPath= rp;
     	    updateSiteProject= usp;
     	}
     }
 
     private final static Map<String, PrefabDescriptor> sUpdateSiteMap= new HashMap<String, PrefabDescriptor>();
 
     static {
     	sUpdateSiteMap.put("IMP", new PrefabDescriptor("SVN", "dev.eclipse.org", "/svnroot/technology/org.eclipse.imp", "org.eclipse.imp.update"));
     	sUpdateSiteMap.put("LPG", new PrefabDescriptor("CVS", "lpg.cvs.sourceforge.net", "/cvsroot/lpg", "lpg.update"));
     	sUpdateSiteMap.put("X10DT", new PrefabDescriptor("CVS", "eclipse-imp.cvs.sourceforge.net", "/cvsroot/eclipse-imp", "org.eclipse.imp.x10dt.update"));
         sUpdateSiteMap.put("RelEng", new PrefabDescriptor("SVN", "dev.eclipse.org", "/svnroot/technology/org.eclipse.imp", "org.eclipse.imp.releng.update"));
     	sUpdateSiteMap.put("Custom", new PrefabDescriptor("CVS", "my.cvs.server.org", "/cvsroot/myrepo", "my.update.project"));
     }
 
     @Override
     protected void createButtonsForButtonBar(Composite parent) {
         // create OK and Cancel buttons by default
     	Button proceedButton= createButton(parent, IDialogConstants.OK_ID, "Proceed", true);
         createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
 
         proceedButton.addSelectionListener(new SelectionListener() {
             public void widgetDefaultSelected(SelectionEvent e) { }
             public void widgetSelected(SelectionEvent e) {
                 String providerRef= sProviderMap.get(fProvider);
                 String updateSiteProjectRef= buildProjectRef(fProvider, fRepoServer, fRepoPath, fUpdateSiteProjectName);
                 WorkbenchReleaseTool releaseTool = new WorkbenchReleaseTool();
 
                 releaseTool.retrieveProject(updateSiteProjectRef, providerRef);
 
                 releaseTool.collectMetaData(true); // Now that we have the update site, rescan the metadata to collect the set of features
                 if (fRetrieveFeatures) {
                     List<UpdateSiteInfo> siteInfos= new ArrayList<UpdateSiteInfo>();
                     UpdateSiteInfo siteInfo= releaseTool.findSiteByName(fUpdateSiteProjectName);
                     siteInfos.add(siteInfo);
 
                     if (!releaseTool.retrieveFeatures(siteInfos)) {
                         return;
                     }
 
                     releaseTool.collectMetaData(true); // Now that we have the features, rescan the metadata to collect the set of plugins
                     if (fRetrievePlugins) {
                         List<FeatureInfo> featureInfos= new ArrayList<FeatureInfo>();
 
                         for(FeatureRef featureRef: siteInfo.fFeatureRefs) {
                             featureInfos.add(releaseTool.findFeatureInfo(featureRef.getID()));
                         }
                         releaseTool.retrievePlugins(featureInfos);
                     }
                 }
             }
             private String buildProjectRef(String providerType, String repoServer, String repoPath, String projectName) {
                 if (providerType.equals("CVS")) {
                     // 1.0,:extssh:orquesta.watson.ibm.com:/usr/src/cvs/JDT,com.ibm.watson.demo.ecoop2005.JLex,com.ibm.watson.demo.ecoop2005.JLex"
                     return "1.0,:extssh:" + repoServer + ":" + repoPath + "," + projectName + "," + projectName;
                 } else if (providerType.equals("SVN")) {
                     // "0.9.3,https://dev.eclipse.org/svnroot/technology/org.eclipse.imp/org.eclipse.imp.update/trunk,org.eclipse.imp.update"
                     return "0.9.3,https://" + repoServer + repoPath + "/" + projectName + "/trunk," + projectName;
                 }
                 throw new IllegalArgumentException("huh?");
             }
         });
     }
 
     private void fillPrefabValues(String updateSite) {
     	PrefabDescriptor pfd= sUpdateSiteMap.get(updateSite);
 
     	fProviderCombo.select(fProviderCombo.indexOf(pfd.providerType));
     	fProvider= pfd.providerType;
     	fRepoServerText.setText(pfd.repoServer);
     	fRepoPathText.setText(pfd.repoPath);
     	fProjectNameText.setText(pfd.updateSiteProject);
     }
 
     @Override
     protected Control createDialogArea(Composite parent) {
         final Composite area= (Composite) super.createDialogArea(parent);
         GridLayout grid= new GridLayout(2, true);
         area.setLayout(grid);
 
         Label prefabLabel= new Label(area, SWT.NONE);
         final Combo prefabCombo= new Combo(area, SWT.DROP_DOWN);
         prefabLabel.setText("Update site:");
         prefabCombo.add("IMP");
         prefabCombo.add("LPG");
         prefabCombo.add("X10DT");
         prefabCombo.add("RelEng");
         prefabCombo.add("Custom");
 
         prefabCombo.addSelectionListener(new SelectionListener() {
             public void widgetDefaultSelected(SelectionEvent e) { }
 
             public void widgetSelected(SelectionEvent e) {
                 fillPrefabValues(prefabCombo.getItem(prefabCombo.getSelectionIndex()));
                 area.pack();
             }
         });
 
         Label providerLabel= new Label(area, SWT.NONE);
         fProviderCombo= new Combo(area, SWT.DROP_DOWN);
         providerLabel.setText("Provider type:");
         fProviderCombo.add("CVS");
         fProviderCombo.add("SVN");
         fProviderCombo.addSelectionListener(new SelectionListener() {
             public void widgetDefaultSelected(SelectionEvent e) { }
 
             public void widgetSelected(SelectionEvent e) {
                 fProvider= fProviderCombo.getItem(fProviderCombo.getSelectionIndex());
             }
         });
 
         Label repoURLLabel= new Label(area, SWT.NONE);
         repoURLLabel.setText("Repository server:");
         fRepoServerText= new Text(area, SWT.NONE);
         fRepoServerText.setText("dev.eclipse.org");
         fRepoServerText.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 fRepoServer= fRepoServerText.getText();
             }
         });
 
         Label repoPathLabel= new Label(area, SWT.NONE);
         repoPathLabel.setText("Repository path:");
         fRepoPathText= new Text(area, SWT.NONE);
         fRepoPathText.setText("/svnroot/technology/org.eclipse.imp");
         fRepoPathText.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 fRepoPath= fRepoPathText.getText();
             }
         });
 
         Label updateProjectLabel= new Label(area, SWT.NONE);
         updateProjectLabel.setText("Update site project:");
         fProjectNameText= new Text(area, SWT.NONE);
         fProjectNameText.setText("org.eclipse.imp.update");
         fProjectNameText.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 fUpdateSiteProjectName= fProjectNameText.getText();
             }
         });
 
         final Button retrieveFeaturesCkbox= new Button(area, SWT.CHECK);
         retrieveFeaturesCkbox.setText("Also retrieve feature projects");
         retrieveFeaturesCkbox.addSelectionListener(new SelectionListener() {
             public void widgetDefaultSelected(SelectionEvent e) { }
             public void widgetSelected(SelectionEvent e) {
                 setRetrieveFeatures(retrieveFeaturesCkbox.getSelection());
             }
         });
 
         /*Label dummy=*/ new Label(area, SWT.NONE);
 
         final Button retrievePluginsCkbox= new Button(area, SWT.CHECK);
         retrievePluginsCkbox.setText("Also retrieve plugin projects");
         retrievePluginsCkbox.addSelectionListener(new SelectionListener() {
             public void widgetDefaultSelected(SelectionEvent e) { }
             public void widgetSelected(SelectionEvent e) {
                 setRetrievePlugins(retrievePluginsCkbox.getSelection());
             }
         });
 
         prefabCombo.select(0);
         fillPrefabValues("IMP");
         area.pack();
         return area;
     }
 
     protected void setRetrieveFeatures(boolean yesNo) {
         fRetrieveFeatures= yesNo;
     }
 
     protected void setRetrievePlugins(boolean yesNo) {
         fRetrievePlugins= yesNo;
     }
 }
