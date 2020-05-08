 /*******************************************************************************
  * Copyright (c) 2010 Bolton University, UK.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the License
  * which accompanies this distribution in the file LICENSE.txt
  *******************************************************************************/
 package org.rulez.magwas.styledhtml;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Enumeration;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.eclipse.ui.PlatformUI;
 import org.rulez.magwas.styledhtml.Widgets;
 
 /**
  * General Preferences Page
  * 
  * @author Phillip Beauvoir
  */
 public class StyledPreferencePage extends PreferencePage
 implements IWorkbenchPreferencePage, IPreferenceConstants {
 	private int BUFSIZE = 4096; //I love arbitrary constants
 	
 	private static StyledPreferencePage self = null;
 	
     public static String HELPID = "org.rulez.magwas.styledhtml.StyledEditor"; //$NON-NLS-1$
     
     private Button fStyleDirButton;
     private Label fStyleDirLabel;
     private CLabel fStyleDirPathLabel;
     private File styleDir = null;
     
     private Button fOutDirButton;
     private Label fOutDirLabel;
     private Button fOutDirCheckBox;
     private Label fOutDirCBLabel;
     private CLabel fOutDirPathLabel;
     private File outDir = null;
     
     
 	public StyledPreferencePage() {
 		setPreferenceStore(StyledHtmlPlugin.INSTANCE.getPreferenceStore());
 		
 		self = this;
 	}
 	
 	public static StyledPreferencePage getTools() {
 		if(null == self) {
 			self = new StyledPreferencePage();
 		}
 		return self;
 	}
 	
     @Override
     protected Control createContents(Composite parent) {
         // Help
         PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, HELPID);
 
         Composite client = new Composite(parent, SWT.NULL);
         client.setLayout(new GridLayout());
         
         GridData gd;
         
         Group styleGroup = new Group(client, SWT.NULL);
         styleGroup.setText("Style Dir");
         styleGroup.setLayout(new GridLayout(2, false));
         styleGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         fStyleDirLabel =  new Label(styleGroup, SWT.NULL);
         fStyleDirLabel.setText("Stylesheet Location:");
         fStyleDirPathLabel = new CLabel(styleGroup, SWT.BORDER);
         gd = new GridData(GridData.FILL_BOTH);
         gd.horizontalSpan = 2;
         gd.widthHint = 400;
         fStyleDirPathLabel.setLayoutData(gd);
         
         fStyleDirButton = new Button(styleGroup,SWT.PUSH);
         fStyleDirButton.setText("modify");
         fStyleDirButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
             	styleDir = askTemplateDir(styleDir);
             	if(null != styleDir) {
             		fStyleDirPathLabel.setText(styleDir.getAbsolutePath());
             	}
             }
         });
 
         Group outGroup = new Group(client, SWT.NULL);
         outGroup.setText("Output Dir");
         outGroup.setLayout(new GridLayout(2, false));
         outGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         fOutDirCBLabel = new Label(outGroup, SWT.NULL);
         fOutDirCBLabel.setText("Don't ask for location");
         fOutDirCheckBox = new Button(outGroup,SWT.CHECK);
         fOutDirLabel = new Label(outGroup, SWT.NULL);
         fOutDirLabel.setText("Use this location:");
         fOutDirPathLabel = new CLabel(outGroup, SWT.BORDER);
         gd = new GridData(GridData.FILL_BOTH);
         gd.horizontalSpan = 2;
         gd.widthHint = 400;
         fOutDirPathLabel.setLayoutData(gd);
         
         fOutDirButton = new Button(outGroup,SWT.PUSH);
         fOutDirButton.setText("modify");
         fOutDirButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
            	outDir = Widgets.askSaveFile(IPreferenceConstants.OUT_PATH,null);
             	if(null != outDir) {
             		fOutDirPathLabel.setText(outDir.getAbsolutePath());
             	}
             }
         });
             
         Group templateGroup = new Group(client, SWT.NULL);
         templateGroup.setText("Template install");
         templateGroup.setLayout(new GridLayout(2, false));
         templateGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         setValues();
         Button installButton = new Button(templateGroup,SWT.PUSH);
         installButton.setText("Install provided styles");
         installButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
             	File tdir = Widgets.askSaveFile(IPreferenceConstants.STYLE_PATH,null);
             	if(null != tdir) {
             		bringPackagedStyles(tdir);
             	}
             }
         });
         
         
         
         return client;
     }
 
     private void setValues() {
     	String path = getPreferenceStore().getString(STYLE_PATH);
     	styleDir = new File(path);
         fStyleDirPathLabel.setText(styleDir.getAbsolutePath());
         
         Boolean ask = getPreferenceStore().getBoolean(OUT_ASK);
         fOutDirCheckBox.setSelection(ask);
         path = getPreferenceStore().getString(OUT_PATH);
         outDir = new File(path);
         fOutDirPathLabel.setText(outDir.getAbsolutePath());
     }
     
     
     @Override
     public boolean performOk() {
     	if (null != styleDir ) {
     		getPreferenceStore().setValue(STYLE_PATH, styleDir.getAbsolutePath());
     	}
         getPreferenceStore().setValue(OUT_ASK, fOutDirCheckBox.getSelection());
         getPreferenceStore().setValue(OUT_PATH, outDir.getAbsolutePath());
         return true;
     }
     
     @Override
     protected void performDefaults() {
     	String path = getPreferenceStore().getDefaultString(STYLE_PATH);
     	styleDir = new File(path);
     	fStyleDirPathLabel.setText(path);
     	path = getPreferenceStore().getDefaultString(OUT_PATH);
     	outDir = new File(path);
     	fOutDirPathLabel.setText(path);
     	fOutDirCheckBox.setSelection(getPreferenceStore().getDefaultBoolean(OUT_ASK));
         super.performDefaults();
     }
 
     public void init(IWorkbench workbench) {
     }
 
     private void bringPackagedStyles(File targetdir) {
     	//System.out.println("targetdir="+ targetdir.getAbsolutePath());
     	
 		Enumeration<URL> e = Platform.getBundle("org.rulez.magwas.styledhtml").findEntries("/styles/", "*", true);
     	for (; e.hasMoreElements() ;) {
     		Boolean isdir = false;
     		URL p = e.nextElement();
             String[] ss = p.getFile().split("/",-1);
             File now = targetdir;
             for(int i=2;i<ss.length;i++) {
             	if(!now.isDirectory()) {
             		now.mkdir();
             	}
                 isdir = (0 == ss[i].compareTo(""));
             	now = new File(now,ss[i]);
             }
     		try {
     			if (!isdir) {
     				copy(p,now);
     			}
 			} catch (IOException ex) {
 				Widgets.tellProblem("Could not extract contents",ex.toString());
 				ex.printStackTrace();
 			}
         }
 
     }
     
     private void copy(URL from, File to) throws IOException {
         BufferedInputStream urlin = new BufferedInputStream(from.openConnection().getInputStream());
         BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(to));
         int read = -1;
         byte[] buf = new byte[BUFSIZE];
         while ((read = urlin.read(buf, 0, BUFSIZE)) >= 0) {
             fout.write(buf, 0, read);
         }
         fout.flush();
         fout.close();
     }
         
     /**
      * Ask user for template directory to use
      */
     public File askTemplateDir(File styleDir) {
     	Shell myshell = Display.getCurrent().getActiveShell();
     	//actually we look at style.xslt
         FileDialog dialog = new FileDialog(myshell, SWT.OPEN);
         if(null != styleDir) {
         	String opath = styleDir.getAbsolutePath();
             dialog.setFileName(opath);
         }
         dialog.setText("Template Directory");
         String path = dialog.open();
         
         if(path == null) {
             return null;
         }    
         
         File file = new File(path);
         return file;
     }
     
 
 
 }
 
 
