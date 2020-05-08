 /******************************************************************************* 
  * Copyright (c) 2008 xored software, Inc.  
  * 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  * 
  * Contributors: 
  *     xored software, Inc. - initial API and Implementation (Yuri Strot) 
  *******************************************************************************/
 package org.eclipse.dltk.ui.formatter.internal;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.core.DLTKContributionExtensionManager;
 import org.eclipse.dltk.core.IDLTKContributedExtension;
 import org.eclipse.dltk.core.IPreferencesSaveDelegate;
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
 import org.eclipse.dltk.internal.ui.formatter.profiles.CustomProfile;
 import org.eclipse.dltk.internal.ui.formatter.profiles.Profile;
 import org.eclipse.dltk.internal.ui.formatter.profiles.ProfileManager;
 import org.eclipse.dltk.internal.ui.formatter.profiles.ProfileStore;
 import org.eclipse.dltk.internal.ui.util.SWTUtil;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.dialogs.PropertyLinkArea;
 import org.eclipse.dltk.ui.formatter.AlreadyExistsDialog;
 import org.eclipse.dltk.ui.formatter.CreateProfileDialog;
 import org.eclipse.dltk.ui.formatter.FormatterMessages;
 import org.eclipse.dltk.ui.formatter.IFormatterModifyDialog;
 import org.eclipse.dltk.ui.formatter.IFormatterModifyDialogOwner;
 import org.eclipse.dltk.ui.formatter.IProfile;
 import org.eclipse.dltk.ui.formatter.IProfileVersioner;
 import org.eclipse.dltk.ui.formatter.IScriptFormatterFactory;
 import org.eclipse.dltk.ui.formatter.ScriptFormatterManager;
 import org.eclipse.dltk.ui.preferences.AbstractOptionsBlock;
 import org.eclipse.dltk.ui.preferences.PreferenceKey;
 import org.eclipse.dltk.ui.util.IStatusChangeListener;
 import org.eclipse.dltk.ui.util.PixelConverter;
 import org.eclipse.dltk.ui.util.SWTFactory;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.dialogs.PreferenceLinkArea;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 
 public abstract class AbstractFormatterSelectionBlock extends
 		AbstractOptionsBlock {
 
 	protected abstract IFormatterModifyDialogOwner createDialogOwner();
 
 	/**
 	 * Returns the extension manager for the contributed extension.
 	 */
 	protected abstract DLTKContributionExtensionManager getExtensionManager();
 
 	/**
 	 * Returns the message that will be used to create the link to the
 	 * preference or property page.
 	 */
 	protected abstract String getPreferenceLinkMessage();
 
 	/**
 	 * Returns the preference key that will be used to store the contribution
 	 * preference.
 	 */
 	protected abstract PreferenceKey getSavedContributionKey();
 
 	protected abstract void updatePreview();
 
 	protected abstract SourceViewer createPreview(Composite parent);
 
 	public AbstractFormatterSelectionBlock(IStatusChangeListener context,
 			IProject project, PreferenceKey formatterKey, String natureId,
 			IWorkbenchPreferenceContainer container) {
 		super(context, project, collectPreferenceKeys(TEMP_LIST, natureId,
 				formatterKey), container);
 		factories = (IScriptFormatterFactory[]) TEMP_LIST
 				.toArray(new IScriptFormatterFactory[TEMP_LIST.size()]);
 		TEMP_LIST = new ArrayList();
 	}
 
 	protected ProfileManager getProfileManager() {
 		return getProfileManager(getSelectedExtension());
 	}
 
 	protected ProfileManager getProfileManager(IScriptFormatterFactory factory) {
 		ProfileManager manager = (ProfileManager) profileByFactory.get(factory);
 		if (manager == null) {
 			List allProfiles = new ArrayList();
 			List buitinProfiles = factory.getBuiltInProfiles();
 			if (buitinProfiles != null && buitinProfiles.size() > 0) {
 				allProfiles.addAll(buitinProfiles);
 			} else {
 				DLTKUIPlugin
 						.logErrorMessage(NLS
 								.bind(
 										FormatterMessages.AbstractFormatterSelectionBlock_noBuiltInProfiles,
 										factory.getId()));
 			}
 
 			if (factory.getProfilesKey() != null) {
 				try {
 					String profilesSource = getValue(factory.getProfilesKey());
 					if (profilesSource != null && profilesSource.length() > 0) {
 						ProfileStore store = getProfileStore(factory);
 						List profiles = store
 								.readProfilesFromString(profilesSource);
 						allProfiles.addAll(profiles);
 					}
 				} catch (Exception e) {
 					DLTKUIPlugin.log(e);
 				}
 			}
 
 			String profileId = null;
 			if (factory.getActiveProfileKey() != null) {
 				profileId = getValue(factory.getActiveProfileKey());
 				if (profileId == null || profileId.length() == 0) {
 					profileId = findProfileId(allProfiles, factory);
 				}
 			}
 			manager = new ProfileManager(allProfiles, (Profile) allProfiles
 					.get(0), profileId);
 			profileByFactory.put(factory, manager);
 		}
 		return manager;
 	}
 
 	protected String findProfileId(List profiles,
 			IScriptFormatterFactory factory) {
 		Map preferences = factory
 				.retrievePreferences(new PreferencesLookupDelegate(getProject()));
 		preferences.remove(factory.getActiveProfileKey().getName());
 		preferences.remove(factory.getProfilesKey().getName());
 		if (!preferences.isEmpty()) {
 			activeProfileChanged = true;
 			IProfile needToSelect = findProfile(preferences, profiles);
 			if (needToSelect != null) {
 				return needToSelect.getID();
 			} else {
 				String name = getProfileName(
 						profiles,
 						FormatterMessages.AbstractFormatterSelectionBlock_activeProfileName);
 				CustomProfile customProfile = new CustomProfile(name,
 						preferences, factory.getId(), factory
 								.getProfileVersioner().getCurrentVersion());
 				profiles.add(customProfile);
 				profilesChanged = true;
 				return customProfile.getID();
 			}
 		}
 		return null;
 	}
 
 	protected String getProfileName(List profiles, String prefix) {
 		HashSet names = new HashSet(profiles.size());
 		Iterator it = profiles.iterator();
 		while (it.hasNext()) {
 			Profile profile = (Profile) it.next();
 			names.add(profile.getName());
 		}
 		if (!names.contains(prefix))
 			return prefix;
 		for (int i = 2;; i++) {
 			String name = prefix + " " + i;
 			if (!names.contains(name))
 				return name;
 		}
 	}
 
 	protected IProfile findProfile(Map preferences, List profiles) {
 		for (Iterator it = profiles.iterator(); it.hasNext();) {
 			IProfile profile = (IProfile) it.next();
 			if (profile.equalsTo(preferences))
 				return profile;
 		}
 		return null;
 	}
 
 	protected ProfileStore getProfileStore() {
 		return getProfileStore(getSelectedExtension());
 	}
 
 	protected ProfileStore getProfileStore(IScriptFormatterFactory factory) {
 		ProfileStore store = (ProfileStore) storeByFactory.get(factory);
 		if (store == null) {
 			IProfileVersioner versioner = factory.getProfileVersioner();
 			store = new ProfileStore(versioner);
 			storeByFactory.put(factory, store);
 		}
 		return store;
 	}
 
 	protected void applyPreferences() {
 		if (activeProfileChanged || profilesChanged) {
 			IScriptFormatterFactory factory = getSelectedExtension();
 			ProfileManager manager = getProfileManager(factory);
 			IProfile profile = manager.getSelected();
 			Map settings = new HashMap(profile.getSettings());
 			String activeKey = factory.getActiveProfileKey().getName();
 			String profilesKey = factory.getProfilesKey().getName();
 
 			if (activeProfileChanged) {
 				manager.getSelected().getID();
 				settings.put(activeKey, profile.getID());
 			} else {
 				settings.remove(activeKey);
 			}
 
 			if (profilesChanged) {
 				try {
 					ProfileStore store = getProfileStore(factory);
 					String profiles = store.writeProfiles(manager
 							.getSortedProfiles());
 					settings.put(profilesKey, profiles);
 				} catch (CoreException e) {
 					DLTKUIPlugin.log(e);
 				}
 			} else {
 				settings.remove(profilesKey);
 			}
 			IPreferencesSaveDelegate delegate = new SaveDelegate();
 			factory.savePreferences(settings, delegate);
 
 			activeProfileChanged = false;
 			profilesChanged = false;
 		}
 		updatePreview();
 	}
 
 	protected static PreferenceKey[] collectPreferenceKeys(List factories,
 			String natureId, PreferenceKey formatterKey) {
 		List result = new ArrayList();
 		result.add(formatterKey);
 		IDLTKContributedExtension[] extensions = ScriptFormatterManager
 				.getInstance().getContributions(natureId);
 		for (int i = 0; i < extensions.length; ++i) {
 			IScriptFormatterFactory factory = (IScriptFormatterFactory) extensions[i];
 			factories.add(factory);
 			final PreferenceKey[] keys = factory.getPreferenceKeys();
 			if (keys != null) {
 				for (int j = 0; j < keys.length; ++j) {
 					final PreferenceKey prefKey = keys[j];
 					result.add(prefKey);
 				}
 			}
 		}
 		return (PreferenceKey[]) result
 				.toArray(new PreferenceKey[result.size()]);
 	}
 
 	// ~ Methods
 
 	public final Control createOptionsBlock(Composite parent) {
 		return createSelectorBlock(parent);
 	}
 
 	protected Composite createDescription(Composite parent,
 			IDLTKContributedExtension contrib) {
 		Composite composite = SWTFactory.createComposite(parent, parent
 				.getFont(), 1, 1, GridData.FILL);
 
 		String desc = contrib.getDescription();
 		if (desc == null) {
 			desc = Util.EMPTY_STRING;
 		}
 		SWTFactory.createLabel(composite, desc, 1);
 
 		String prefPageId = contrib.getPreferencePageId();
 		String propPageId = contrib.getPropertyPageId();
 
 		// we're a property page
 		if (isProjectPreferencePage() && hasValidId(propPageId)) {
 			new PropertyLinkArea(composite, SWT.NONE, propPageId, fProject,
 					getPreferenceLinkMessage(), getPreferenceContainer());
 		}
 
 		// we're a preference page
 		if (!isProjectPreferencePage() && hasValidId(prefPageId)) {
 			new PreferenceLinkArea(composite, SWT.NONE, prefPageId,
 					getPreferenceLinkMessage(), getPreferenceContainer(), null);
 		}
 
 		return composite;
 	}
 
 	protected Composite createSelectorBlock(Composite parent) {
 		final int numColumns = 5;
 
 		PixelConverter fPixConv = new PixelConverter(parent);
 		fComposite = createComposite(parent, numColumns);
 
 		createFormatterSection(fComposite, numColumns, fPixConv);
 
 		final Group group = SWTFactory
 				.createGroup(
 						fComposite,
 						FormatterMessages.AbstractFormatterSelectionBlock_profilesGroup,
 						numColumns, numColumns, GridData.FILL_BOTH);
 
 		Label profileLabel = new Label(group, SWT.NONE);
 		profileLabel
 				.setText(FormatterMessages.AbstractFormatterSelectionBlock_activeProfile);
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
 		data.horizontalSpan = numColumns;
 		profileLabel.setLayoutData(data);
 
 		fProfileCombo = createProfileCombo(group, 3, fPixConv
 				.convertWidthInCharsToPixels(20));
 		updateComboFromProfiles();
 		fProfileCombo.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent e) {
 				updateSelection();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				updateSelection();
 			}
 		});
 
 		fEditButton = createButton(group,
 				FormatterMessages.AbstractFormatterSelectionBlock_editProfile,
 				GridData.HORIZONTAL_ALIGN_BEGINNING);
 		fEditButton.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent e) {
 				editButtonPressed();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				editButtonPressed();
 			}
 		});
 		fDeleteButton = createButton(
 				group,
 				FormatterMessages.AbstractFormatterSelectionBlock_removeProfile,
 				GridData.HORIZONTAL_ALIGN_BEGINNING);
 		fDeleteButton.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent e) {
 				doDelete();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				doDelete();
 			}
 
 			protected void doDelete() {
 				if (MessageDialog
 						.openQuestion(
 								group.getShell(),
 								FormatterMessages.AbstractFormatterSelectionBlock_confirmRemoveLabel,
 								NLS
 										.bind(
 												FormatterMessages.AbstractFormatterSelectionBlock_confirmRemoveMessage,
 												getProfileManager()
 														.getSelected()
 														.getName()))) {
 					getProfileManager().deleteSelected();
 					updateComboFromProfiles();
 					activeProfileChanged = true;
 					profilesChanged = true;
 					applyPreferences();
 				}
 			}
 		});
 
 		fNewButton = createButton(group,
 				FormatterMessages.AbstractFormatterSelectionBlock_newProfile,
 				GridData.HORIZONTAL_ALIGN_BEGINNING);
 		fNewButton.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent e) {
 				createNewProfile();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				createNewProfile();
 			}
 
 			protected void createNewProfile() {
 				final CreateProfileDialog p = new CreateProfileDialog(group
 						.getShell(), getProfileManager(), getProfileStore()
 						.getVersioner());
 				if (p.open() != Window.OK)
 					return;

				profilesChanged = true;
				activeProfileChanged = true;
				applyPreferences();

 				updateComboFromProfiles();
 				if (!p.openEditDialog())
 					return;
 				editButtonPressed();
 			}
 		});
 
 		fLoadButton = createButton(
 				group,
 				FormatterMessages.AbstractFormatterSelectionBlock_importProfile,
 				GridData.HORIZONTAL_ALIGN_END);
 		fLoadButton.addSelectionListener(new SelectionListener() {
 
 			public void widgetSelected(SelectionEvent e) {
 				doImport();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				doImport();
 			}
 
 			protected void doImport() {
 				final FileDialog dialog = new FileDialog(group.getShell(),
 						SWT.OPEN);
 				dialog
 						.setText(FormatterMessages.AbstractFormatterSelectionBlock_importProfileLabel);
 				dialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$
 				final String path = dialog.open();
 				if (path == null)
 					return;
 
 				final File file = new File(path);
 				Collection profiles = null;
 				ProfileStore store = getProfileStore();
 				try {
 					profiles = store.readProfilesFromFile(file);
 				} catch (CoreException e) {
 					DLTKUIPlugin
 							.logErrorMessage(
 									FormatterMessages.AbstractFormatterSelectionBlock_notValidProfile,
 									e);
 				}
 				if (profiles == null || profiles.isEmpty())
 					return;
 
 				final CustomProfile profile = (CustomProfile) profiles
 						.iterator().next();
 
 				IProfileVersioner versioner = store.getVersioner();
 
 				if (!versioner.getFormatterId()
 						.equals(profile.getFormatterId())) {
 					final String title = FormatterMessages.AbstractFormatterSelectionBlock_importProfileLabel;
 					final String message = NLS
 							.bind(
 									FormatterMessages.AbstractFormatterSelectionBlock_notValidFormatter,
 									versioner.getFormatterId(), profile
 											.getFormatterId());
 					MessageDialog.openError(group.getShell(), title, message);
 					return;
 				}
 
 				if (profile.getVersion() > versioner.getCurrentVersion()) {
 					final String title = FormatterMessages.AbstractFormatterSelectionBlock_importingProfile;
 					final String message = FormatterMessages.AbstractFormatterSelectionBlock_moreRecentVersion;
 					MessageDialog.openWarning(group.getShell(), title, message);
 				}
 
 				if (getProfileManager().containsName(profile.getName())) {
 					final AlreadyExistsDialog aeDialog = new AlreadyExistsDialog(
 							group.getShell(), profile, getProfileManager());
 					if (aeDialog.open() != Window.OK)
 						return;
 				}
 				profile.setVersion(1);
 				getProfileManager().addProfile(profile);
 				updateComboFromProfiles();
 				activeProfileChanged = true;
 				profilesChanged = true;
 				applyPreferences();
 			}
 		});
 		createLabel(group, "", 3); //$NON-NLS-1$
 
 		configurePreview(group, numColumns);
 		updateButtons();
 		applyPreferences();
 
 		return fComposite;
 	}
 
 	protected void createFormatterSection(Composite composite, int numColumns,
 			PixelConverter fPixConv) {
 		String id = getValue(getSavedContributionKey());
 		int index = -1;
 		for (int i = 0; i < factories.length; i++) {
 			IScriptFormatterFactory factory = factories[i];
 			if (factory.getId().equals(id)) {
 				index = i;
 				break;
 			}
 		}
 		if (index == -1 && factories.length != 0) {
 			index = 0;
 			for (int i = 1; i < factories.length; i++) {
 				if (factories[i].getPriority() > factories[index].getPriority()) {
 					index = i;
 				}
 			}
 			// doSetFactory(index);
 		}
 
 		if (factories.length > 1) {
 			createLabel(
 					composite,
 					FormatterMessages.AbstractFormatterSelectionBlock_formatterLabel,
 					numColumns);
 			fFactoryCombo = createProfileCombo(composite, numColumns, fPixConv
 					.convertWidthInCharsToPixels(20));
 
 			for (int i = 0; i < factories.length; i++) {
 				fFactoryCombo.add(factories[i].getName());
 			}
 
 			fFactoryCombo.addSelectionListener(new SelectionListener() {
 
 				public void widgetSelected(SelectionEvent e) {
 					doSetFactory(fFactoryCombo.getSelectionIndex());
 				}
 
 				public void widgetDefaultSelected(SelectionEvent e) {
 					doSetFactory(fFactoryCombo.getSelectionIndex());
 				}
 			});
 			fFactoryCombo.select(index);
 		}
 
 		fFactoryDescription = createLabel(composite, "", numColumns);
 		doSetFactory(index);
 	}
 
 	protected void doSetFactory(int index) {
 		selectedFactory = index;
 		setValue(getSavedContributionKey(), factories[index].getId());
 		fFactoryDescription.setText(getSelectedExtension().getDescription());
 		updateComboFromProfiles();
 		applyPreferences();
 	}
 
 	protected void configurePreview(Composite composite, int numColumns) {
 		createLabel(composite,
 				FormatterMessages.AbstractFormatterSelectionBlock_preview,
 				numColumns);
 		fPreviewViewer = createPreview(composite);
 
 		final GridData gd = new GridData(GridData.FILL_VERTICAL
 				| GridData.HORIZONTAL_ALIGN_FILL);
 		gd.horizontalSpan = numColumns;
 		gd.verticalSpan = 7;
 		gd.widthHint = 0;
 		gd.heightHint = 0;
 		fPreviewViewer.getControl().setLayoutData(gd);
 	}
 
 	protected IScriptFormatterFactory getSelectedExtension() {
 		return factories[selectedFactory];
 	}
 
 	protected final void updateSelection() {
 		Profile selected = (Profile) getProfileManager().getSortedProfiles()
 				.get(fProfileCombo.getSelectionIndex());
 		getProfileManager().setSelected(selected);
 		updateButtons();
 		activeProfileChanged = true;
 		applyPreferences();
 		updatePreview();
 	}
 
 	protected void editButtonPressed() {
 		IScriptFormatterFactory factory = getSelectedExtension();
 		if (factory != null) {
 			ProfileManager manager = getProfileManager();
 			final IFormatterModifyDialog dialog = factory
 					.createDialog(createDialogOwner());
 			if (dialog != null) {
 				IProfile profile = manager.getSelected();
 				Map settings = profile.getSettings();
 				dialog.setPreferences(settings);
 				if (dialog.open() == Window.OK) {
 					profile = (Profile) manager.getSelected();
 					updateComboFromProfiles();
 					final Map newSettings = dialog.getPreferences();
 					if (!profile.getSettings().equals(newSettings)) {
 						profile.setSettings(newSettings);
 						profilesChanged = true;
 						activeProfileChanged = true;
 						applyPreferences();
 					}
 				}
 			}
 		}
 	}
 
 	protected void updateComboFromProfiles() {
 		if (fProfileCombo != null && !fProfileCombo.isDisposed()) {
 			fProfileCombo.removeAll();
 
 			List profiles = getProfileManager().getSortedProfiles();
 			IProfile selected = getProfileManager().getSelected();
 			Iterator it = profiles.iterator();
 			int selection = 0, index = 0;
 			while (it.hasNext()) {
 				Profile profile = (Profile) it.next();
 				fProfileCombo.add(profile.getName());
 				if (profile.equals(selected))
 					selection = index;
 				index++;
 			}
 			fProfileCombo.select(selection);
 			updateButtons();
 		}
 	}
 
 	protected void updateButtons() {
 		if (fDeleteButton != null && !fDeleteButton.isDisposed()) {
 			IProfile selected = getProfileManager().getSelected();
 			fDeleteButton.setEnabled(!selected.isBuiltInProfile());
 		}
 	}
 
 	private class SaveDelegate implements IPreferencesSaveDelegate {
 
 		public void setBoolean(String qualifier, String key, boolean value) {
 			setValue(new PreferenceKey(qualifier, key), value);
 		}
 
 		public void setInt(String qualifier, String key, int value) {
 			setValue(new PreferenceKey(qualifier, key), String.valueOf(value));
 		}
 
 		public void setString(String qualifier, String key, String value) {
 			setValue(new PreferenceKey(qualifier, key), value);
 		}
 
 	}
 
 	private Composite createComposite(Composite parent, int numColumns) {
 		final Composite composite = new Composite(parent, SWT.NONE);
 		composite.setFont(parent.getFont());
 
 		final GridLayout layout = new GridLayout(numColumns, false);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		composite.setLayout(layout);
 		return composite;
 	}
 
 	private static Combo createProfileCombo(Composite composite, int span,
 			int widthHint) {
 		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = span;
 		gd.widthHint = widthHint;
 
 		final Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
 		combo.setFont(composite.getFont());
 		combo.setLayoutData(gd);
 
 		return combo;
 	}
 
 	private static Button createButton(Composite composite, String text,
 			final int style) {
 		final Button button = new Button(composite, SWT.PUSH);
 		button.setFont(composite.getFont());
 		button.setText(text);
 
 		final GridData gd = new GridData(style);
 		gd.widthHint = SWTUtil.getButtonWidthHint(button);
 		button.setLayoutData(gd);
 		return button;
 	}
 
 	protected static Label createLabel(Composite composite, String text,
 			int numColumns) {
 		final GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd.horizontalSpan = numColumns;
 		gd.widthHint = 0;
 
 		final Label label = new Label(composite, SWT.WRAP);
 		label.setFont(composite.getFont());
 		label.setText(text);
 		label.setLayoutData(gd);
 		return label;
 	}
 
 	protected void initialize() {
 		super.initialize();
 	}
 
 	public void performDefaults() {
 		super.performDefaults();
 	}
 
 	private boolean hasValidId(String id) {
 		return (id != null && !"".equals(id)); //$NON-NLS-1$
 	}
 
 	private Composite fComposite;
 	private Combo fProfileCombo;
 	private Combo fFactoryCombo;
 	private Label fFactoryDescription;
 	private Button fEditButton;
 	private Button fDeleteButton;
 	private Button fNewButton;
 	private Button fLoadButton;
 
 	private boolean activeProfileChanged;
 	private boolean profilesChanged;
 
 	private int selectedFactory;
 	private IScriptFormatterFactory[] factories;
 	private Map storeByFactory = new HashMap();
 	private Map profileByFactory = new HashMap();
 	protected SourceViewer fPreviewViewer;
 
 	private static List TEMP_LIST = new ArrayList();
 
 }
