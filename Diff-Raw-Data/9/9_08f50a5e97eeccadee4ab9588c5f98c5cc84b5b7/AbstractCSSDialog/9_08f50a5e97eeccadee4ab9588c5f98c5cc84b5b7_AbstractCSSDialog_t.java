 /*******************************************************************************
  * Copyright (c) 2007-2009 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.css.dialog;
 
 import java.util.Map;
 
 import org.eclipse.core.databinding.AggregateValidationStatus;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.observable.ChangeEvent;
 import org.eclipse.core.databinding.observable.IChangeListener;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.jboss.tools.common.model.ui.ModelUIImages;
 import org.jboss.tools.common.model.ui.widgets.Split;
 import org.jboss.tools.jst.css.browser.CSSBrowser;
 import org.jboss.tools.jst.css.dialog.common.StyleAttributes;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.jsp.util.Constants;
 
 /**
  * @author Sergey Dzmitrovich
  * 
  */
 public abstract class AbstractCSSDialog extends TitleAreaDialog {
 
 	private CSSBrowser browser;
 
 	private Text previewText;
 
 	private String previewContent;
 
 	private StyleAttributes styleAttributes;
 
 	private StyleComposite styleComposite;
 
 	private DataBindingContext bindingContext = new DataBindingContext();
 	
 	private AggregateValidationStatus aggregateStatus;
 
 	private IStatus status = Status.OK_STATUS;
 
 	final public static int DEFAULT_DIALOG_WIDTH = 500;
 	final public static int DEFAULT_DIALOG_HEIGHT = 500;
 	final public static int DEFAULT_BROWTHER_WEIGHT = 15;
 	final public static int DEFAULT_CONTROLS_WEIGHT = 85;
 
 	/**
 	 * 
 	 */
 	public AbstractCSSDialog(Shell shell) {
 		super(shell);
 
 		previewContent = JstUIMessages.DEFAULT_TEXT_FOR_BROWSER_PREVIEW;
 		styleAttributes = new StyleAttributes();
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		/*
 		 * Set the dialog image
 		 */
 		setTitleImage(ModelUIImages.getImageDescriptor(
 				ModelUIImages.WIZARD_DEFAULT).createImage());
 
 		Composite parentComposite = (Composite) super.createDialogArea(parent);
 		GridData gridData = (GridData) parentComposite.getLayoutData();
 		gridData.heightHint = DEFAULT_DIALOG_HEIGHT;
 		gridData.widthHint = DEFAULT_DIALOG_WIDTH;
 
 		createControlPane(parentComposite);
 
 		return parentComposite;
 	}
 
 	public Composite createControlPane(Composite parent) {
 		
 		getStyleAttributes().addChangeListener(new IChangeListener() {
 
 			public void handleChange(ChangeEvent event) {
 				handleStyleChanged();
 
 			}
 
 		});
 
 		aggregateStatus = new AggregateValidationStatus(
 				getBindingContext().getValidationStatusProviders(),
 				AggregateValidationStatus.MAX_SEVERITY);
 		aggregateStatus.addValueChangeListener(new IValueChangeListener() {
 			public void handleValueChange(ValueChangeEvent event) {
 
 				handleStatusChanged((IStatus) event.diff.getNewValue());
 			}
 		});
 		
 		// Create split component that separates dialog on 2 parts
 		Split dialogContainer = new Split(parent, SWT.VERTICAL);
 
 		createBrowserComposite(dialogContainer);
 		createControlComposite(dialogContainer);
 
 		dialogContainer.setWeights(new int[] { DEFAULT_BROWTHER_WEIGHT,
 				DEFAULT_CONTROLS_WEIGHT });
 		dialogContainer.setLayoutData(new GridData(GridData.FILL,
 				GridData.BEGINNING, true, true));
 
 		return dialogContainer;
 	}
 
 	protected Composite createControlComposite(Composite parent) {
 
 		// Create down splitter container
 		Composite controlsContainer = new Composite(parent, SWT.None);
 		controlsContainer.setLayout(new GridLayout());
 		controlsContainer.setLayoutData(new GridData(GridData.FILL,
 				GridData.FILL, true, true));
 
 		createExtensionComposite(controlsContainer);
 		styleComposite = createStyleComposite(controlsContainer);
 
 		return controlsContainer;
 
 	}
 
 	protected void createExtensionComposite(Composite parent) {
 
 	}
 
 	/**
 	 * 
 	 * @param parent
 	 */
 	protected Composite createBrowserComposite(final Composite parent) {
 
 		final SashForm previewComposite = new SashForm(parent, SWT.None);
 		previewComposite.setLayout(new GridLayout());
 		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true,
 				true);
 		previewComposite.setLayoutData(gridData);
 
 		browser = CSSBrowser.createCSSBrowser(previewComposite, SWT.BORDER | SWT.MOZILLA);
 		browser.setText(generateBrowserPage());
 		browser.setLayoutData(gridData);
		
 		browser.addMouseListener(new MouseAdapter() {
			/* TODO: yradtsevich: Deduplicate code. Method
			 * org.jboss.tools.jst.css.view.CSSPreview.createPartControl(Composite)
			 * has the same MouseListener in it.
			 * This duplicate code may cause issues like JBIDE-8414  */
 			public void mouseDoubleClick(MouseEvent e) {
				if (browser.isBrowserEvent(e)) {
 					browser.setEnabled(false);
 					previewComposite.setMaximizedControl(previewText);
 					previewText.setFocus();
 				}
 			}
 		});
 
 		previewText = new Text(previewComposite, SWT.NONE | SWT.H_SCROLL);
 		previewText.setLayoutData(gridData);
 		previewText.setText(getPreviewContent());
 		previewText.addFocusListener(new FocusAdapter() {
 			public void focusLost(FocusEvent e) {
 				if (e.widget == previewText) {
 					String text = previewText.getText();
 					if (text == null || text.equals(Constants.EMPTY)) {
 						setPreviewContent(JstUIMessages.DEFAULT_TEXT_FOR_BROWSER_PREVIEW);
 					} else {
 						setPreviewContent(text);
 					}
 
 					browser.setEnabled(true);
 					browser.setText(generateBrowserPage());
 					previewComposite.setMaximizedControl(browser);
 				}
 			}
 		});
 
 		previewComposite.setMaximizedControl(browser);
 		return previewComposite;
 	}
 
 	protected StyleComposite createStyleComposite(Composite parent) {
 		return new StyleComposite(parent, getStyleAttributes(),
 				getBindingContext());
 	}
 
 	protected void handleStyleChanged() {
 		browser.setText(generateBrowserPage());
 	}
 
 	protected void handleStatusChanged(IStatus newStatus) {
 		if (newStatus.isOK() && !status.isOK()) {
 			setErrorMessage(null);
 		} else if (newStatus.getSeverity() == IStatus.ERROR) {
 			setErrorMessage(newStatus.getMessage());
 		}
 		if (newStatus.getSeverity() != status.getSeverity()) {
 			getButton(OK).setEnabled(newStatus.isOK());
 		}
 
 		status = newStatus;
 	}
 
 	public IStatus getStatus() {
 		return status;
 	}
 
 	/**
 	 * /** Method is used to build html body that is appropriate to browse.
 	 * 
 	 * @return String html text representation
 	 */
 	public String generateBrowserPage() {
 		StringBuffer html = new StringBuffer("<style>span{"); //$NON-NLS-1$
 
 		for (Map.Entry<String, String> styleItem : getStyleAttributes()
 				.entrySet()) {
 
 			html.append(styleItem.getKey() + Constants.COLON
 					+ styleItem.getValue() + Constants.SEMICOLON);
 		}
 
 		html.append("}</style><span>" + getPreviewContent() + "</span>"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		return html.toString();
 	}
 	
 	public void releaseResources() {
 		aggregateStatus.dispose();
 	}
 
 	@Override
 	public boolean close() {
 		releaseResources();
 		return super.close();
 	}
 
 	public StyleAttributes getStyleAttributes() {
 		return styleAttributes;
 	}
 
 	public String getPreviewContent() {
 		return previewContent;
 	}
 
 	public void setPreviewContent(String previewContent) {
 		this.previewContent = previewContent;
 	}
 
 	public DataBindingContext getBindingContext() {
 		return bindingContext;
 	}
 
 	public StyleComposite getStyleComposite() {
 		return styleComposite;
 	}
 
 	public void setStatus(IStatus status) {
 		this.status = status;
 	}
 
 }
