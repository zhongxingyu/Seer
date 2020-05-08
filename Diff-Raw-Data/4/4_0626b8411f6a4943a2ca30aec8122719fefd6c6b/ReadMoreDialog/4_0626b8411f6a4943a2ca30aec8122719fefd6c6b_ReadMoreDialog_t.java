 /*******************************************************************************
  * Copyright (c) 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.core.notifications.ui.dialogs;
 
 import java.net.URL;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.PlatformUI;
 import org.zend.core.notifications.Activator;
 import org.zend.core.notifications.internal.ui.Messages;
 
 /**
  * Dialog which allows to display long notification description.
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public class ReadMoreDialog extends Dialog {
 
 	private String message;
 	private String title;
 	private String helpLink;
 
 	public ReadMoreDialog(Shell parentShell, String title, String message,
 			String helpLink) {
 		super(parentShell);
 		this.title = title;
 		this.message = message;
 		this.helpLink = helpLink;
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Control control = super.createDialogArea(parent);
 
 		final ScrolledComposite scrollComposite = new ScrolledComposite(
 				(Composite) control, SWT.V_SCROLL | SWT.BORDER);
 
 		final Text text = new Text(scrollComposite, SWT.WRAP | SWT.READ_ONLY);
 		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		text.setText(message);
 		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				true));
 		scrollComposite.setLayout(new GridLayout(1, true));
 		scrollComposite.setContent(text);
 		scrollComposite.setExpandVertical(true);
 		scrollComposite.setExpandHorizontal(true);
 		scrollComposite.addControlListener(new ControlAdapter() {
 			public void controlResized(ControlEvent e) {
 				Rectangle r = scrollComposite.getClientArea();
 				scrollComposite.setMinSize(text.computeSize(r.width,
 						SWT.DEFAULT));
 			}
 		});
		if (title != null) {
			parent.getShell().setText(title);
		}
 		return control;
 	}
 
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setSize(500, 500);
 		Rectangle monitorArea = newShell.getDisplay().getPrimaryMonitor()
 				.getBounds();
 		Rectangle shellArea = newShell.getBounds();
 		int x = monitorArea.x + (monitorArea.width - shellArea.width) / 2;
 		int y = monitorArea.y + (monitorArea.height - shellArea.height) / 3;
 		newShell.setLocation(x, y);
 	}
 
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
 		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		createHelpControl(parent);
 		createButton(parent, OK, Messages.ReadMoreDialog_CloseLabel, true);
 	}
 
 	protected void createHelpControl(Composite parent) {
 		Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
 		if (helpImage != null) {
 			createHelpImageButton(parent, helpImage);
 		}
 	}
 
 	private ToolBar createHelpImageButton(Composite parent, Image image) {
 		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
 		((GridLayout) parent.getLayout()).numColumns++;
 		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
 		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
 		toolBar.setCursor(cursor);
 		toolBar.addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				cursor.dispose();
 			}
 		});
 		final ToolItem helpButton = new ToolItem(toolBar, SWT.CHECK);
 		helpButton.setImage(image);
 		helpButton.setToolTipText(Messages.ReadMoreDialog_HelpTooltip);
 		helpButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				try {
 					PlatformUI.getWorkbench().getBrowserSupport()
 							.getExternalBrowser().openURL(new URL(helpLink));
 					helpButton.setSelection(false);
 				} catch (Exception e) {
 					Activator.log(e);
 				}
 			}
 		});
 		if (helpLink == null || helpLink.isEmpty()) {
 			helpButton.setEnabled(false);
 		}
 		return toolBar;
 	}
 
 }
