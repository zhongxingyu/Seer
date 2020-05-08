 package org.eclipse.dltk.ui.preferences;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.dltk.internal.ui.preferences.OptionsConfigurationBlock;
 import org.eclipse.dltk.ui.dialogs.PropToPrefLinkArea;
 import org.eclipse.dltk.ui.util.IStatusChangeListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.PreferenceLinkArea;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 
 public abstract class AbstractOptionsBlock extends OptionsConfigurationBlock
 		implements IPreferenceDelegate {
 
 	private ControlBindingManager bindManager;
 
 	public AbstractOptionsBlock(IStatusChangeListener context,
 			IProject project, PreferenceKey[] allKeys,
 			IWorkbenchPreferenceContainer container) {
 		super(context, project, allKeys, container);
 
 		this.bindManager = new ControlBindingManager(this, context);
 	}
 
 	public Control createContents(Composite parent) {
 		setShell(parent.getShell());
 		Control control = createOptionsBlock(parent);
 		initialize();
 
 		return control;
 	}
 
 	protected void initialize() {
 		bindManager.initialize();
 	}
 
 	protected abstract Control createOptionsBlock(Composite parent);
 
 	protected final void bindControl(Button button, PreferenceKey key,
 			Control[] dependencies) {
 		bindManager.bindControl(button, key, dependencies);
 	}
 
 	protected final void bindControl(Text textBox, PreferenceKey key,
 			IFieldValidator validator) {
 		bindManager.bindControl(textBox, key, validator);
 	}
 
 	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
 		String title = getBuildDialogTitle();
 		String message;
 		if (workspaceSettings) {
 			message = getFullBuildDialogMessage();
 		} else {
 			message = getProjectBuildDialogMessage();
 		}
 
 		if (title == null || message == null) {
 			return null;
 		}
 
 		return new String[] { title, message };
 	}
 
 	/**
 	 * Returns the string that should be used as the title in the popup box that
 	 * indicates a build needs to occur.
 	 * 
 	 * <p>
 	 * Default implementation returns null. Clients should override to return
	 * context approprite title. Clients must also override
 	 * <code>getFullBuildDialogMessage()</code> and
 	 * <code>getProjectBuildDialogMessage()</code> in order to trigger the popup
 	 * box.
 	 * </p>
 	 */
 	protected String getBuildDialogTitle() {
 		return null;
 	}
 
 	/**
 	 * Returns the string that should be used in the popup box that indicates a
 	 * full build needs to occur.
 	 * 
 	 * <p>
 	 * Default implementation returns null. Clients should override to return
	 * context approprite message. Clients must also override
 	 * <code>getBuildDialogTitle()</code> and
 	 * <code>getProjectBuildDialogMessage()</code> in order to trigger the popup
 	 * box.
 	 * </p>
 	 */
 	protected String getFullBuildDialogMessage() {
 		return null;
 	}
 
 	/**
 	 * Returns the string that should be used in the popup box that indicates a
 	 * project build needs to occur.
 	 * 
 	 * <p>
 	 * Default implementation returns null. Clients should override to return
	 * context approprite message. Clients must also override
 	 * <code>getBuildDialogTitle()</code> and
 	 * <code>getFullBuildDialogMessage()</code> in order to trigger the popup
 	 * box.
 	 * </p>
 	 */
 	protected String getProjectBuildDialogMessage() {
 		return null;
 	}
 
 	protected final boolean isProjectPreferencePage() {
 		return fProject != null;
 	}
 
 	/*
 	 * @seeorg.eclipse.dltk.internal.ui.preferences.OptionsConfigurationBlock#
 	 * performDefaults()
 	 */
 	public void performDefaults() {
 		super.performDefaults();
 		bindManager.initialize();
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.dltk.ui.preferences.IPreferenceDelegate#getBoolean(java.lang
 	 * .Object)
 	 */
 	public final boolean getBoolean(Object key) {
 		return getBooleanValue((PreferenceKey) key);
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.dltk.ui.preferences.IPreferenceDelegate#getString(java.lang
 	 * .Object)
 	 */
 	public final String getString(Object key) {
 		return getValue((PreferenceKey) key);
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.dltk.ui.preferences.IPreferenceDelegate#setBoolean(java.lang
 	 * .Object, boolean)
 	 */
 	public final void setBoolean(Object key, boolean value) {
 		super.setValue((PreferenceKey) key, value);
 	}
 
 	/*
 	 * @see
 	 * org.eclipse.dltk.ui.preferences.IPreferenceDelegate#setString(java.lang
 	 * .Object, java.lang.String)
 	 */
 	public final void setString(Object key, String value) {
 		setValue((PreferenceKey) key, value);
 	}
 
 	protected final IProject getProject() {
 		return fProject;
 	}
 
 	protected final void updateStatus(IStatus status) {
 		bindManager.updateStatus(status);
 	}
 
 	protected void createPrefLink(Composite composite, String message,
 			final String prefPageId, final Object data) {
 		PreferenceLinkArea area = new PreferenceLinkArea(composite, SWT.NONE,
 				prefPageId, message, getPreferenceContainer(), data);
 
 		area.getControl().setLayoutData(
 				new GridData(SWT.FILL, SWT.FILL, false, false));
 	}
 
 	protected void createPropToPrefLink(Composite composite, String message,
 			final String prefPageId, final Object data) {
 		PropToPrefLinkArea area = new PropToPrefLinkArea(composite, SWT.NONE,
 				prefPageId, message, getShell(), data);
 
 		area.getControl().setLayoutData(
 				new GridData(SWT.FILL, SWT.FILL, false, false));
 	}
 
 	/*
 	 * Override getShell() method as public API.
 	 * 
 	 * @see OptionsConfigurationBlock#getShell()
 	 */
 	protected Shell getShell() {
 		return super.getShell();
 	}
 
 	/*
 	 * Override dispose() method as public API.
 	 * 
 	 * @see OptionsConfigurationBlock#dispose()
 	 */
 	public void dispose() {
 		super.dispose();
 	}
 
 }
