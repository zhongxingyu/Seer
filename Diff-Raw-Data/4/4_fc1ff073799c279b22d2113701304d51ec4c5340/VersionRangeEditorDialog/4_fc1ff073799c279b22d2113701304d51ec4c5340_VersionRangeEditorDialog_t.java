 /*******************************************************************************
  * Copyright (c) 2006-2007, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 
 package org.eclipse.b3.aggregator.presentation;
 
 import org.eclipse.equinox.internal.p2.metadata.VersionFormat;
 import org.eclipse.equinox.p2.metadata.IVersionFormat;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionFormatException;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.resource.JFaceColors;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
 
 /**
  * @author Karel Brezina
  * 
  */
 public class VersionRangeEditorDialog extends Dialog {
 
 	// VersionRange.OSGi_versionMax is not visible
 	private static final Version OSGi_versionMax = Version.createOSGi(
 		Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
 
 	private static final int VERSION_TEXT_WIDTH_HINT = 160;
 
 	private static final String VERSION_TYPE_OSGI = "OSGi";
 
 	private static final String VERSION_TYPE_STRING = "String";
 
 	private static final String VERSION_TYPE_TIMESTAMP = "Timestamp";
 
 	private static final String VERSION_TYPE_TRIPLET = "Triplet";
 
 	private static final String VERSION_TYPE_USER_DEFINED = "User Defined";
 
 	private static final int VERSION_TYPE_USER_DEFINED_IDX = 4;
 
 	private static final String[] VERSION_TYPES = {
 			VERSION_TYPE_OSGI, VERSION_TYPE_STRING, VERSION_TYPE_TIMESTAMP, VERSION_TYPE_TRIPLET,
 			VERSION_TYPE_USER_DEFINED };
 
 	private static final IVersionFormat VERSION_FORMAT_OSGI;
 
 	private static final IVersionFormat VERSION_FORMAT_STRING;
 
 	private static final IVersionFormat VERSION_FORMAT_TIMESTAMP;
 
 	private static final IVersionFormat VERSION_FORMAT_TRIPLET;
 
 	private static final IVersionFormat[] VERSION_FORMATS;
 
 	static {
 		try {
 			VERSION_FORMAT_OSGI = VersionFormat.compile("n[.n=0;[.n=0;[.S=\"\";=[A-Za-z0-9_-];]]]");
 
 			VERSION_FORMAT_STRING = VersionFormat.compile("S");
 
 			VERSION_FORMAT_TIMESTAMP = VersionFormat.compile("S=[0-9];={8};[.S=[0-9];={6};=\"000000\";]");
 
 			VERSION_FORMAT_TRIPLET = VersionFormat.compile("n[.n=0;[.n=0;]][d?S=M;]");
 		}
 		catch(VersionFormatException e) {
 			throw new IllegalArgumentException(e.getMessage());
 		}
 
 		VERSION_FORMATS = new IVersionFormat[] {
 				VERSION_FORMAT_OSGI, VERSION_FORMAT_STRING, VERSION_FORMAT_TIMESTAMP, VERSION_FORMAT_TRIPLET };
 	}
 
 	private static String getString(String key) {
 		return AggregatorEditorPlugin.INSTANCE.getString(key);
 	}
 
 	protected ILabelProvider labelProvider;
 
 	protected VersionRange versionRange;
 
 	private Composite topComposite;
 
 	private Text minVersionText;
 
 	private Combo minVersionInclusiveCombo;
 
 	private Text maxVersionText;
 
 	private Combo maxVersionInclusiveCombo;
 
 	private Label statusLabel;
 
 	private Button okButton;
 
 	private VersionRange result;
 
 	private int labelWidth;
 
 	private Composite advancedComposite;
 
 	private Button advancedButton;
 
 	private Composite advancedFieldsComposite;
 
 	private int advancedFieldsHeight = -1;
 
 	private Combo versionTypeCombo;
 
 	private Text formatStringText;
 
 	private int currentVersionTypeIdx;
 
 	private IVersionFormat currentFormat;
 
 	private Exception formatStringException;
 
 	protected VersionRangeEditorDialog(Shell parent, ILabelProvider labelProvider, Object object) {
 		super(parent);
 		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
 		this.labelProvider = labelProvider;
 		this.versionRange = (VersionRange) object;
 		IVersionFormat versionFormat = versionRange.getMinimum().getFormat();
 
 		currentVersionTypeIdx = 0;
 		currentFormat = VERSION_FORMAT_OSGI;
 
 		if(versionFormat != null) {
 			currentVersionTypeIdx = VERSION_TYPE_USER_DEFINED_IDX;
 			currentFormat = versionFormat;
 
 			int i = 0;
 			for(IVersionFormat formatString : VERSION_FORMATS) {
 				if(versionFormat.equals(formatString))
 					currentVersionTypeIdx = i;
 				i++;
 			}
 		}
 	}
 
 	public VersionRange getResult() {
 		return result;
 	}
 
 	@Override
 	protected void buttonPressed(int buttonId) {
 		switch(buttonId) {
 			case IDialogConstants.CANCEL_ID:
 				close();
 				break;
 			default:
 				if(performAction(buttonId))
 					close();
 		}
 	}
 
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 		shell.setText(getString("_UI_VersionRangeEditor_windowTitle"));
 	}
 
 	@Override
 	protected Control createButtonBar(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 0; // create
 		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
 		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
 		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
 		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
 
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Label filler = new Label(composite, SWT.NONE);
 		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
 		layout.numColumns++;
 
 		okButton = createButton(composite, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
 		okButton.setEnabled(isOKEnabled());
 		createButton(composite, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
 
 		return composite;
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite result = (Composite) super.createDialogArea(parent);
 
 		Control fContents = createPageArea(result);
 		fContents.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		return result;
 	}
 
 	protected Control createPageArea(Composite parent) {
 		final String[] inclusiveExclusive = new String[] {
 				getString("_UI_VersionRangeEditor_inclusiveChoice"),
 				getString("_UI_VersionRangeEditor_exclusiveChoice") };
 
 		topComposite = new Composite(parent, SWT.NONE);
 		topComposite.setFont(parent.getFont());
 		topComposite.setLayout(new GridLayout(3, false));
 		topComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		new Label(topComposite, SWT.NONE).setText(getString("_UI_VersionRangeEditor_minimumVersionLabel"));
 		minVersionText = new Text(topComposite, SWT.BORDER);
 		minVersionText.setText(versionRange.getMinimum().getOriginal() != null
 				? versionRange.getMinimum().getOriginal()
 				: versionRange.getMinimum().toString());
 		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.widthHint = VERSION_TEXT_WIDTH_HINT;
 		minVersionText.setLayoutData(gridData);
 		minVersionInclusiveCombo = new Combo(topComposite, SWT.READ_ONLY);
 		minVersionInclusiveCombo.setItems(inclusiveExclusive);
 		minVersionInclusiveCombo.select(versionRange.getIncludeMinimum()
 				? 0
 				: 1);
 
 		Label label = new Label(topComposite, SWT.NONE);
 		label.setText(getString("_UI_VersionRangeEditor_maximumVersionLabel"));
 		labelWidth = label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
 
 		maxVersionText = new Text(topComposite, SWT.BORDER);
 
 		Version maxVersion = versionRange.getMaximum();
 		maxVersionText.setText((Version.MAX_VERSION.equals(maxVersion) || OSGi_versionMax.equals(maxVersion))
 				? ""
 				: maxVersion.getOriginal() != null
 						? maxVersion.getOriginal()
 						: maxVersion.toString());
 		maxVersionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		maxVersionInclusiveCombo = new Combo(topComposite, SWT.READ_ONLY);
 		maxVersionInclusiveCombo.setItems(inclusiveExclusive);
 		maxVersionInclusiveCombo.select(versionRange.getIncludeMaximum()
 				? 0
 				: 1);
 
 		ModifyListener modifyListener = new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				okButton.setEnabled(isOKEnabled());
 			}
 		};
 
 		minVersionText.addModifyListener(modifyListener);
 		minVersionInclusiveCombo.addModifyListener(modifyListener);
 		maxVersionText.addModifyListener(modifyListener);
 		maxVersionInclusiveCombo.addModifyListener(modifyListener);
 
 		statusLabel = new Label(topComposite, SWT.LEFT);
 		GridData layoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);
 		layoutData.horizontalSpan = 3;
 		statusLabel.setLayoutData(layoutData);
 
 		advancedComposite = new Composite(topComposite, SWT.NONE);
 		advancedComposite.setFont(topComposite.getFont());
 		layoutData = new GridData(GridData.FILL_HORIZONTAL);
 		layoutData.horizontalSpan = 3;
 		advancedComposite.setLayoutData(layoutData);
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		advancedComposite.setLayout(layout);
 
 		advancedButton = new Button(advancedComposite, SWT.PUSH);
 		advancedButton.setFont(advancedComposite.getFont());
 		advancedButton.setText(IDEWorkbenchMessages.showAdvanced);
 
 		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		int widthHint = IDialogConstants.BUTTON_WIDTH;
 		Point minSize = advancedButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 		data.widthHint = Math.max(widthHint, minSize.x);
 		data.horizontalAlignment = GridData.BEGINNING;
 		advancedButton.setLayoutData(data);
 
 		advancedButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleAdvancedButtonSelect();
 			}
 		});
 
 		return topComposite;
 	}
 
 	/**
 	 * Shows/hides the advanced option widgets.
 	 */
 	protected void handleAdvancedButtonSelect() {
 		Shell shell = getShell();
 		Point shellSize = shell.getSize();
 
 		if(advancedFieldsComposite != null) {
 			advancedFieldsComposite.dispose();
 			advancedFieldsComposite = null;
 			topComposite.layout();
 			shell.setSize(shellSize.x, shellSize.y - advancedFieldsHeight);
 			advancedButton.setText(IDEWorkbenchMessages.showAdvanced);
 		}
 		else {
 			advancedFieldsComposite = createAdvancedContent();
 			if(advancedFieldsHeight == -1) {
 				Point groupSize = advancedFieldsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 				advancedFieldsHeight = groupSize.y;
 			}
 			shell.setSize(shellSize.x, shellSize.y + advancedFieldsHeight);
 			topComposite.layout();
 			advancedButton.setText(IDEWorkbenchMessages.hideAdvanced);
 		}
 	}
 
 	protected boolean performAction(int actionID) {
 		result = null;
 
 		switch(actionID) {
 			case CANCEL:
 				return true;
 			case OK:
 				try {
 					result = createVersionRange();
 				}
 				catch(IllegalArgumentException e) {
 					statusMessage(true, e.getMessage());
 					return false;
 				}
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	private Composite createAdvancedContent() {
 		Composite composite = new Composite(advancedComposite, SWT.NONE);
 		GridLayout layout = new GridLayout(2, false);
 		layout.marginHeight = layout.marginWidth = 0;
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
 		composite.setFont(advancedComposite.getFont());
 
 		Label label = new Label(composite, SWT.NONE);
 		label.setText("Version Type:");
 		GridData layoutData = new GridData();
 		layoutData.widthHint = labelWidth;
 		label.setLayoutData(layoutData);
 
 		versionTypeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
 		versionTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		versionTypeCombo.setItems(VERSION_TYPES);
 		versionTypeCombo.select(currentVersionTypeIdx);
 		versionTypeCombo.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				currentVersionTypeIdx = versionTypeCombo.getSelectionIndex();
 
 				if(VERSION_TYPE_USER_DEFINED.equals(versionTypeCombo.getText())) {
 					formatStringText.setEnabled(true);
 				}
 				else {
 					currentFormat = VERSION_FORMATS[currentVersionTypeIdx];
 					formatStringText.setText(extractFormat(currentFormat.toString()));
 					formatStringText.setEnabled(false);
 				}
 			}
 		});
 
 		label = new Label(composite, SWT.NONE);
 		label.setText("Format String:");
 		layoutData = new GridData();
 		layoutData.widthHint = labelWidth;
 		label.setLayoutData(layoutData);
 
 		formatStringText = new Text(composite, SWT.BORDER);
 		formatStringText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		UIUtils.safeSetText(formatStringText, extractFormat(currentFormat.toString()));
 		formatStringText.setEnabled(VERSION_TYPE_USER_DEFINED.equals(versionTypeCombo.getText()));
 		formatStringText.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent e) {
 				try {
 					currentFormat = VersionFormat.compile(UIUtils.trimmedValue(formatStringText));
 					formatStringException = null;
 				}
 				catch(VersionFormatException ex) {
 					formatStringException = ex;
 				}
 				finally {
 					okButton.setEnabled(isOKEnabled());
 				}
 			}
 		});
 
 		return composite;
 	}
 
 	private VersionRange createVersionRange() throws IllegalArgumentException {
 
 		if(formatStringException != null)
 			throw new IllegalArgumentException(formatStringException.getMessage());
 
 		String minVersionString = UIUtils.trimmedValue(minVersionText);
 		String maxVersionString = UIUtils.trimmedValue(maxVersionText);
 
 		if(minVersionString == null)
 			throw new IllegalArgumentException(getString("_UI_VersionRangeEditor_minimumVersionMessage") + " " +
 					getString("_UI_VersionRangeEditor_notSpecifiedMessage"));
 
 		Version minVersion = null;
 		Version maxVersion = null;
 
 		try {
 			if(VERSION_FORMAT_OSGI.equals(currentFormat))
 				minVersion = Version.create(minVersionString);
 			else
 				minVersion = currentFormat.parse(minVersionString);
 		}
 		catch(IllegalArgumentException e) {
 			throw new IllegalArgumentException(getString("_UI_VersionRangeEditor_minimumVersionMessage") + " " +
 					e.getMessage());
 		}
 
 		if(maxVersionString != null) {
 			try {
 				if(VERSION_FORMAT_OSGI.equals(currentFormat))
 					maxVersion = Version.create(maxVersionString);
 				else
 					maxVersion = currentFormat.parse(maxVersionString);
 			}
 			catch(IllegalArgumentException e) {
 				throw new IllegalArgumentException(getString("_UI_VersionRangeEditor_maximumVersionMessage") + " " +
 						e.getMessage());
 			}
 		}
 
 		return new VersionRange(
			minVersion, minVersionInclusiveCombo.getSelectionIndex() == 0, maxVersion, maxVersion == null ||
					maxVersionInclusiveCombo.getSelectionIndex() == 0);
 	}
 
 	// "format(S)" -> "S"
 	private String extractFormat(String string) {
 		return string.substring(7, string.length() - 1);
 	}
 
 	private boolean isOKEnabled() {
 		try {
 			createVersionRange();
 		}
 		catch(IllegalArgumentException e) {
 			statusMessage(true, e.getMessage());
 			return false;
 		}
 
 		statusMessage(true, "");
 		return true;
 	}
 
 	private void statusMessage(boolean error, String message) {
 		statusLabel.setText(message);
 
 		if(error)
 			statusLabel.setForeground(JFaceColors.getErrorText(statusLabel.getDisplay()));
 		else
 			statusLabel.setForeground(null);
 
 		if(UIUtils.trimmedValue(message) != null)
 			getShell().getDisplay().beep();
 	}
 }
