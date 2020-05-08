 /*===========================================================================
   Copyright (C) 2010-2012 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.ui;
 
 import java.nio.charset.Charset;
 import java.util.List;
 
 import net.sf.okapi.common.FileUtil;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilterConfigurationListEditor;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.ui.Dialogs;
 import net.sf.okapi.common.ui.filters.FilterConfigurationEditor;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 public class InputDocumentPanel extends Composite {
 
 	private Label stDocument;
 	private Text edDocument;
 	private Label stConfigId;
 	private Text edConfigId;
 	private Button btGetFile;
 	private Button btGetConfigId;
 	private Label stEncoding;
 	private Text edEncoding;
 	private Label stSourceLocale;
 	private Text edSourceLocale;
 	private Label stTargetLocale;
 	private Text edTargetLocale;
 	private String title;
 	private String filterNames;
 	private String filterExtensions;
 	private IFilterConfigurationMapper fcMapper;
 
 	public InputDocumentPanel (Composite parent,
 		int flags,
 		int horizontalSpan,
 		String documentLabel,
 		String getDocumentLabel,
 		IFilterConfigurationMapper fcMapper)
 	{
 		super(parent, flags);
 		createContent(horizontalSpan, documentLabel, getDocumentLabel, fcMapper);
 	}
 	
 	private void createContent (int horizontalSpan,
 		String docLabel,
 		String getDocLabel,
 		IFilterConfigurationMapper fcMapper)
 	{
 		this.fcMapper = fcMapper;
 		title = "Select a File";
 
 		GridLayout layTmp = new GridLayout(3, false);
 		layTmp.marginHeight = 0;
 		layTmp.marginWidth = 0;
 		setLayout(layTmp);
 		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.horizontalSpan = horizontalSpan;
 		setLayoutData(gdTmp);
 
 		//--- Document path
 		
 		if ( docLabel != null ) {
 			stDocument = new Label(this, SWT.NONE);
 			stDocument.setText(docLabel);
 			gdTmp = new GridData();
 			gdTmp.horizontalSpan = 3;
 			stDocument.setLayoutData(gdTmp);
 		}
 		
 		edDocument = new Text(this, SWT.BORDER);
 		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
 		gdTmp.horizontalSpan = 2;
 		edDocument.setLayoutData(gdTmp);
 		
 		btGetFile = new Button(this, SWT.PUSH);
 		if ( getDocLabel == null ) btGetFile.setText("...");
 		else btGetFile.setText(getDocLabel);
 		btGetFile.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				browsePath();
 			};
 		});
 		
 		//--- Configuration id
 		
 		stConfigId = new Label(this, SWT.NONE);
 		stConfigId.setText("Configuration:");
 		
 		edConfigId = new Text(this, SWT.BORDER);
 		edConfigId.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		
 		btGetConfigId = new Button(this, SWT.PUSH);
 		btGetConfigId.setText("...");
 		btGetConfigId.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				browseConfiguration();
 			};
 		});
 		
 		//--- Encoding
 		
 		stEncoding = new Label(this, SWT.NONE);
 		stEncoding.setText("Encoding:");
 		
 		edEncoding = new Text(this, SWT.BORDER);
 		edEncoding.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		new Label(this, SWT.NONE); // Fill extra cell rather than stretching edit field
 
 		//--- Source locale
 		
 		stSourceLocale = new Label(this, SWT.NONE);
 		stSourceLocale.setText("Source locale:");
 		
 		edSourceLocale = new Text(this, SWT.BORDER);
 		edSourceLocale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		new Label(this, SWT.NONE); // Fill extra cell rather than stretching edit field
 		
 		//--- Target locale
 		
 		stTargetLocale = new Label(this, SWT.NONE);
 		stTargetLocale.setText("Target locale:");
 		
 		edTargetLocale = new Text(this, SWT.BORDER);
 		edTargetLocale.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		new Label(this, SWT.NONE); // Fill extra cell rather than stretching edit field
 	}
 
 	public String getDocumentPath () {
 		return edDocument.getText();
 	}
 
 	public void setDocumentPath (String path) {
 		edDocument.setText(path == null ? "" : path);
 	}
 	
 	public String getFilterConfigurationId () {
 		return edConfigId.getText();
 	}
 
 	public void setFilterConfigurationId (String configId) {
 		edConfigId.setText(configId == null ? "" : configId);
 	}
 	
 	public String getEncoding () {
 		return edEncoding.getText();
 	}
 
 	public void setEncoding (String encoding) {
 		edEncoding.setText(encoding == null ? "" : encoding);
 	}
 	
 	public LocaleId getSourceLocale () {
 		if ( edSourceLocale.getText().isEmpty() ) return LocaleId.EMPTY;
 		return LocaleId.fromString(edSourceLocale.getText());
 	}
 
 	public void setSourceLocale (LocaleId sourceLocale) {
 		edSourceLocale.setText(sourceLocale == null ? "" : sourceLocale.toString());
 	}
 	
 	public LocaleId getTargetLocale () {
 		if ( edTargetLocale.getText().isEmpty() ) return LocaleId.EMPTY;
 		return LocaleId.fromString(edTargetLocale.getText());
 	}
 
 	public void setTargetLocale (LocaleId targetLocale) {
 		edTargetLocale.setText(targetLocale == null ? "" :targetLocale.toString());
 	}
 	
 	public void guessConfiguration () {
 		String ext = Util.getExtension(edDocument.getText());
 		FilterConfiguration fc = fcMapper.getDefaultConfigurationFromExtension(ext);
		// Special case for TTX: not the normal default
		if ( ext.equalsIgnoreCase(".ttx") ) {
			fc = fcMapper.getConfiguration("okf_ttx-noForcedTuv");
		}
 		if ( fc != null ) {
 			edConfigId.setText(fc.configId);
 		}
 	}
 	
 	public void guessLocales () {
 		// Guess the languages if possible
 		if ( edSourceLocale.getEditable() ) {
 			List<String> list = FileUtil.guessLanguages(edDocument.getText());
 			if ( list.size() > 0 ) {
 				edSourceLocale.setText(list.get(0));
 			}
 			if ( list.size() == 2 ) {
 				edTargetLocale.setText(list.get(1));
 			}
 			else if ( list.size() > 2 ) {
 				StringBuilder tmp = new StringBuilder(list.get(1));
 				for ( int i=2; i<list.size(); i++ ) {
 					tmp.append(" or "+list.get(i));
 				}
 				edTargetLocale.setText(tmp.toString());
 			}
 		}
 	}
 	
 	@Override
 	public void setEnabled (boolean enabled) {
 		super.setEnabled(enabled);
 		if ( stDocument != null ) stDocument.setEnabled(enabled);
 		edDocument.setEnabled(enabled);
 		btGetFile.setEnabled(enabled);
 		edConfigId.setEnabled(enabled);
 		btGetConfigId.setEnabled(enabled);
 		edEncoding.setEnabled(enabled);
 		edSourceLocale.setEnabled(enabled);
 		edTargetLocale.setEnabled(enabled);
 	}
 	
 	public void setLocalesEditable (boolean editable) {
 		edSourceLocale.setEditable(editable);
 		edTargetLocale.setEditable(editable);
 	}
 
 	public void setEditable (boolean editable) {
 		if ( stDocument != null ) stDocument.setEnabled(editable);
 		edDocument.setEditable(editable);
 		btGetFile.setEnabled(editable);
 		edConfigId.setEditable(editable);
 		btGetConfigId.setEnabled(editable);
 		edEncoding.setEditable(editable);
 		edSourceLocale.setEditable(editable);
 		edTargetLocale.setEditable(editable);
 	}
 
 	public String getTitle () {
 		return title;
 	}
 	
 	public void setTitle (String title) {
 		this.title = title;
 	}
 	
 	public void setBrowseFilters (String filterNames,
 		String filterExtensions)
 	{
 		this.filterNames = filterNames;
 		this.filterExtensions = filterExtensions;
 	}
 	
 	private void browseConfiguration () {
 		try {
 			IFilterConfigurationListEditor fcEditor = new FilterConfigurationEditor();
 			String res = fcEditor.editConfigurations(fcMapper, edConfigId.getText());
 			if ( res != null ) {
 				edConfigId.setText(res);
 				edConfigId.selectAll();
 				edConfigId.setFocus();
 			}
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), e.getLocalizedMessage(), null);
 		}
 	}
 
 	private void browsePath () {
 		try {
 			// Get the extension of the current document
 			String oldExt = Util.getExtension(edDocument.getText());
 			
 			// Browse a new document
 			String[] paths = Dialogs.browseFilenames(getShell(), title, false,
 				null, filterNames, filterExtensions);
 			if ( paths == null ) return;
 			
 			// Set the new document
 			edDocument.setText(paths[0]);
 			edDocument.selectAll();
 			edDocument.setFocus();
 			
 			// Guess the configuration if needed
 			String newExt = Util.getExtension(paths[0]);
 			if ( edConfigId.getText().isEmpty() || !oldExt.equalsIgnoreCase(newExt) ) {
 				FilterConfiguration fc = fcMapper.getDefaultConfigurationFromExtension(newExt);
 				if ( fc != null ) {
 					edConfigId.setText(fc.configId);
 				}
 			}
 
 			// Guess the languages if possible
 			guessLocales();
 		}
 		catch ( Throwable e ) {
 			Dialogs.showError(getShell(), e.getLocalizedMessage(), null);
 		}
 	}
 	
 	public boolean validate (boolean showError) {
 		// Check document
 		if ( edDocument.getText().trim().isEmpty() ) {
 			// Empty document path
 			if ( showError ) {
 				Dialogs.showError(getShell(), "The document path must not be empty.", null);
 				edDocument.setFocus();
 			}
 			return false;
 		}
 		
 		// Check configuration ID
 		String tmp = edConfigId.getText();
 		if ( fcMapper.getConfiguration(tmp) == null ) {
 			// Unknown configuration ID
 			if ( showError ) {
 				Dialogs.showError(getShell(),
 					String.format("The string '%s' is not a known configuration ID.", tmp), null);
 				edConfigId.setFocus();
 			}
 			return false;
 		}
 		
 		// check encoding
 		tmp = edEncoding.getText();
 		try {
 			Charset.forName(tmp);
 		}
 		catch ( Throwable e ) {
 			// Invalid encoding
 			if ( showError ) {
 				Dialogs.showError(getShell(),
 					String.format("The encoding '%s' is either unknown or not supported.", tmp), null);
 				edEncoding.setFocus();
 			}
 			return false;
 		}
 		
 		// Check source locale
 		tmp = edSourceLocale.getText();
 		try {
 			LocaleId.fromString(tmp);
 		}
 		catch ( Throwable e ) {
 			if ( showError ) {
 				Dialogs.showError(getShell(),
 					String.format("The source locale '%s' is not a valid locale.", tmp), null);
 				edSourceLocale.setFocus();
 			}
 			return false;
 		}
 		
 		// Check target locale
 		tmp = edTargetLocale.getText();
 		try {
 			LocaleId.fromString(tmp);
 		}
 		catch ( Throwable e ) {
 			// Invalid BCP-47 tag
 			if ( showError ) {
 				Dialogs.showError(getShell(),
 					String.format("The target locale '%s' is not a valid locale.", tmp), null);
 				edTargetLocale.setFocus();
 			}
 			return false;
 		}
 		return true;
 	}
 	
 }
