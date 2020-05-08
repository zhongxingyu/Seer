 /*
  * Copyright (c) 2013 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.dawnsci.common.richbeans.components.file;
 
 import java.io.File;
 
 import org.dawnsci.common.richbeans.beans.IFieldWidget;
 import org.dawnsci.common.richbeans.components.EventManagerDelegate;
 import org.dawnsci.common.richbeans.components.FieldComposite;
 import org.dawnsci.common.richbeans.event.ValueEvent;
 import org.dawnsci.common.richbeans.event.ValueListener;
 import org.dawnsci.common.widgets.content.FileContentProposalProvider;
 import org.dawnsci.common.widgets.content.IFilterExtensionProvider;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.fieldassist.ContentProposalAdapter;
 import org.eclipse.jface.fieldassist.TextContentAdapter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.ui.part.ResourceTransfer;
 
 /**
  *
  */
 public class FileBox extends FieldComposite implements IFieldWidget, IFilterExtensionProvider {
 
 	public enum ChoiceType {
 		FULL_PATH, NAME_ONLY
 	}
 
 	private ChoiceType choiceType = ChoiceType.FULL_PATH;
 	private final Text text;
 
 	private String[] filterExtensions;
 	private String fileTitle;
 	private File selectedFolder;
 	private ModifyListener modifyListener;
 	private SelectionAdapter selectionListener;
 	private Button button;
 	private boolean isFolder=false;
 	private Dialog  dialog;
 
 	public FileBox(final Composite parent, final int style) {
 
 		super(parent, style);
 		setLayout(new GridLayout(3, false));
 		this.eventDelegate = new EventManagerDelegate(this);
 
 
 		final int textStyle = style==SWT.NONE?SWT.BORDER:style;
 		this.text = new Text(this, textStyle);
 		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		mainControl = text;
 
		this.button = new Button(this, SWT.PUSH);
		button.setText("..");
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

 		this.modifyListener = new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				final ValueEvent evt = new ValueEvent(FileBox.this, getFieldName());
 				evt.setValue(getValue());
 				eventDelegate.notifyValueListeners(evt);
 			}
 		};
 		text.addModifyListener(modifyListener);
 
 		FileContentProposalProvider prov = new FileContentProposalProvider();
 		prov.setFilterExtensionProv(this);
 		ContentProposalAdapter ad = new ContentProposalAdapter(text, new TextContentAdapter(), prov, null, null);
 		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
 
 
 		this.selectionListener = new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (dialog == null) {
 					dialog = isFolder()
 					       ? new DirectoryDialog(getShell(), SWT.OPEN)
 					       : new FileDialog(getShell(), SWT.OPEN);
 					final String title = getFileTitle();
 					if (title != null) dialog.setText(title);
 				}
 				if (dialog instanceof FileDialog) { 
 					((FileDialog)dialog).setFilterExtensions(getFilterExtensions());
 					if (text.getText() != null) {
 						try {
 							if (selectedFolder != null) {
 								((FileDialog)dialog).setFilterPath(selectedFolder.getAbsolutePath());
 							}
 							((FileDialog)dialog).setFileName(text.getText());
 						} catch (Exception ignored) {
 							// Do nothing
 						}
 					}
 				}
 				
 				final String path  = isFolder()
 							       ? ((DirectoryDialog)dialog).open()
 							       : ((FileDialog)dialog).open();
 				if (path == null) return;
 				setValue(path);
 			}
 		};
 		button.addSelectionListener(selectionListener);
 		
 		// We try to add a DND lister to allow files to be dragged and dropped here
 		DropTarget dt = new DropTarget(text, DND.DROP_MOVE| DND.DROP_DEFAULT| DND.DROP_COPY);
 		dt.setTransfer(new Transfer[] { TextTransfer.getInstance (), FileTransfer.getInstance(), ResourceTransfer.getInstance()});
 		dt.addDropListener(new DropTargetAdapter() {
 			@Override
 			public void drop(DropTargetEvent event) {
                 final Object data = event.data;
                 if (data instanceof String[]) {
                 	setValue(((String[])data)[0]);
                 	
                 } else if (data instanceof IResource[]) {
                 	final IResource[] res = (IResource[])data;
                 	setValue(res[0].getLocation().toOSString());
                 	
                 } else if (data instanceof File[]) {
                 	setValue(((File[])data)[0].getAbsolutePath());
                 }
 			}			
 		});
 	}
 	
 	@Override
 	public void closeDialog() {
 		// How to close the file dialog? If you hide the shell, it is the application shell!!
 		// However since it is modal, there is not an immediate need.
 	}
 
 	@Override
 	public void dispose() {
 		if (text != null && !text.isDisposed()) {
 			text.removeModifyListener(modifyListener);
 			text.dispose();
 		}
 		if (button != null && !button.isDisposed()) {
 			button.removeSelectionListener(selectionListener);
 			button.dispose();
 		}
 		super.dispose();
 	}
 
 	@Override
 	public Object getValue() {
 		if (getChoiceType() == ChoiceType.FULL_PATH) {
 			return (new File(selectedFolder, text.getText())).getAbsolutePath();
 		}
 		return text.getText();
 
 	}
 
 	@Override
 	public void setValue(Object value) {
 		if (value == null) {
 			text.setText("");
 			return;
 		}
 
 		if (getChoiceType() == ChoiceType.FULL_PATH || value.toString().contains(System.getProperty("file.separator"))) {
 			final File sel = new File(value.toString());
 			if (!sel.getName().equals(this.text.getText())){
 				this.selectedFolder = sel.getParentFile();
 				this.text.setToolTipText(selectedFolder.getAbsolutePath());
 				this.text.setText(sel.getName());
 				this.text.setSelection(sel.getName().length());
 			}
 		} else {
 			if (selectedFolder != null){
 				this.text.setToolTipText(selectedFolder.getAbsolutePath());
 			}
 			this.text.setText((String) value);
 			this.text.setSelection(((String) value).length());
 		}
 
 	}
 
 	/**
 	 * Method for setting the box into an error state.
 	 * 
 	 * @param isError
 	 * @param reason
 	 */
 	public void setError(final boolean isError, final String reason) {
 		if (isError) {
 			final Color red = getDisplay().getSystemColor(SWT.COLOR_RED);
 			if (!red.isDisposed())
 				text.setForeground(red);
 			this.text.setToolTipText(reason);
 		} else {
 			final Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
 			if (!black.isDisposed())
 				text.setForeground(black);
 			this.text.setToolTipText(selectedFolder.getAbsolutePath());
 		}
 	}
 
 	/*******************************************************************/
 	/** This section will be the same for many wrappers. **/
 	/*******************************************************************/
 	@Override
 	protected void checkSubclass() {
 	}
 
 	/**
 	 * @return the active
 	 */
 	@Override
 	public boolean isActivated() {
 		return active;
 	}
 
 	/**
 	 * @param active
 	 *            the active to set
 	 */
 	@Override
 	public void setActive(boolean active) {
 		this.active = active;
 		setVisible(active);
 	}
 
 	@Override
 	public void addValueListener(ValueListener l) {
 		eventDelegate.addValueListener(l);
 	}
 
 	@Override
 	public void removeValueListener(ValueListener l) {
 		eventDelegate.removeValueListener(l);
 	}
 	
 	/**
 	 * @return Returns the filterExtensions.
 	 */
 	@Override
 	public String[] getFilterExtensions() {
 		return filterExtensions;
 	}
 
 	/**
 	 * @param filterExtensions
 	 *            The filterExtensions to set.
 	 */
 	public void setFilterExtensions(String[] filterExtensions) {
 		this.filterExtensions = filterExtensions;
 	}
 
 	/*******************************************************************/
 
 	/**
 	 * @return Returns the fileTitle.
 	 */
 	public String getFileTitle() {
 		return fileTitle;
 	}
 
 	/**
 	 * @param fileTitle
 	 *            The fileTitle to set.
 	 */
 	public void setFileTitle(String fileTitle) {
 		this.fileTitle = fileTitle;
 	}
 
 	/**
 	 * @param selectedFolder
 	 */
 	public void setFolder(File selectedFolder) {
 		this.selectedFolder = selectedFolder;
 		this.text.setToolTipText(selectedFolder.getAbsolutePath());
 	}
 
 	/**
 	 * Just sets the file name not the folder.
 	 * 
 	 * @param fileName
 	 */
 	public void setText(final String fileName) {
 		if (fileName == null) {
 			text.setText("");
 			return;
 		}
 		text.setText(fileName);
 	}
 
 	/**
 	 * @return the file text
 	 */
 	public String getText() {
 		return text.getText();
 	}
 
 	/**
 	 * @return Returns the choiceType.
 	 */
 	public ChoiceType getChoiceType() {
 		return choiceType;
 	}
 
 	/**
 	 * @param choiceType
 	 *            The choiceType to set.
 	 */
 	public void setChoiceType(ChoiceType choiceType) {
 		this.choiceType = choiceType;
 	}
 
 	public File getFolder() {
 		return this.selectedFolder;
 	}
 
 	public boolean isFolder() {
 		return isFolder;
 	}
 
 	public void setFolder(boolean isFolder) {
 		dialog = null;
 		this.isFolder = isFolder;
 	}
 
 }
