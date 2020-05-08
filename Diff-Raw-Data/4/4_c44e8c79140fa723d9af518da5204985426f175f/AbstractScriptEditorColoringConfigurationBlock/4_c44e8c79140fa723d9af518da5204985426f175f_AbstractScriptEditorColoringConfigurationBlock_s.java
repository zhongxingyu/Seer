 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.ui.preferences;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.dltk.internal.ui.preferences.ScriptSourcePreviewerUpdater;
 import org.eclipse.dltk.internal.ui.text.DLTKColorManager;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.dltk.ui.PreferencesAdapter;
 import org.eclipse.dltk.ui.text.IColorManager;
 import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
 import org.eclipse.dltk.ui.util.PixelConverter;
 import org.eclipse.jface.preference.ColorSelector;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.IOverviewRuler;
 import org.eclipse.jface.text.source.IVerticalRuler;
 import org.eclipse.jface.text.source.projection.ProjectionViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.swt.widgets.Scrollable;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.editors.text.EditorsUI;
 import org.eclipse.ui.texteditor.ChainedPreferenceStore;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 /**
  * Configures Editor hover preferences. TODO: We need to add support for user
  * categories here...
  */
 public abstract class AbstractScriptEditorColoringConfigurationBlock extends
 		AbstractConfigurationBlock {
 
 	/**
 	 * Item in the highlighting color list.
 	 */
 	private static class HighlightingColorListItem {
 		/** Display name */
 		private String fDisplayName;
 
 		/** Color preference key */
 		private String fColorKey;
 
 		/** Bold preference key */
 		private String fBoldKey;
 
 		/** Italic preference key */
 		private String fItalicKey;
 
 		/**
 		 * Strikethrough preference key.
 		 */
 		private String fStrikethroughKey;
 
 		/**
 		 * Underline preference key.
 		 */
 		private String fUnderlineKey;
 
 		private String fCategory;
 
 		/**
 		 * Initialize the item with the given values.
 		 * 
 		 * @param displayName
 		 *            the display name
 		 * @param colorKey
 		 *            the color preference key
 		 * @param boldKey
 		 *            the bold preference key
 		 * @param italicKey
 		 *            the italic preference key
 		 * @param strikethroughKey
 		 *            the strikethrough preference key
 		 * @param underlineKey
 		 *            the underline preference key
 		 */
 		public HighlightingColorListItem(String displayName, String colorKey,
 				String boldKey, String italicKey, String strikethroughKey,
 				String underlineKey, String category) {
 			fDisplayName = displayName;
 			fColorKey = colorKey;
 			fBoldKey = boldKey;
 			fItalicKey = italicKey;
 			fStrikethroughKey = strikethroughKey;
 			fUnderlineKey = underlineKey;
 			fCategory = category;
 		}
 
 		/**
 		 * @return the bold preference key
 		 */
 		public String getBoldKey() {
 			return fBoldKey;
 		}
 
 		/**
 		 * @return the bold preference key
 		 */
 		public String getItalicKey() {
 			return fItalicKey;
 		}
 
 		/**
 		 * @return the strikethrough preference key
 		 * 
 		 */
 		public String getStrikethroughKey() {
 			return fStrikethroughKey;
 		}
 
 		/**
 		 * @return the underline preference key
 		 * 
 		 */
 		public String getUnderlineKey() {
 			return fUnderlineKey;
 		}
 
 		/**
 		 * @return the color preference key
 		 */
 		public String getColorKey() {
 			return fColorKey;
 		}
 
 		/**
 		 * @return the display name
 		 */
 		public String getDisplayName() {
 			return fDisplayName;
 		}
 
 		/**
 		 * @return the category name
 		 */
 		public String getCategory() {
 			return fCategory;
 		}
 	}
 
 	private static class SemanticHighlightingColorListItem extends
 			HighlightingColorListItem {
 
 		/** Enablement preference key */
 		private final String fEnableKey;
 
 		/**
 		 * Initialize the item with the given values.
 		 * 
 		 * @param displayName
 		 *            the display name
 		 * @param colorKey
 		 *            the color preference key
 		 * @param boldKey
 		 *            the bold preference key
 		 * @param italicKey
 		 *            the italic preference key
 		 * @param strikethroughKey
 		 *            the strikethroughKey preference key
 		 * @param underlineKey
 		 *            the underlineKey preference key
 		 * @param enableKey
 		 *            the enable preference key
 		 */
 		public SemanticHighlightingColorListItem(String displayName,
 				String colorKey, String boldKey, String italicKey,
 				String strikethroughKey, String underlineKey, String category,
 				String enableKey) {
 			super(displayName, colorKey, boldKey, italicKey, strikethroughKey,
 					underlineKey, category);
 			fEnableKey = enableKey;
 		}
 
 		/**
 		 * @return the enablement preference key
 		 */
 		public String getEnableKey() {
 			return fEnableKey;
 		}
 	}
 
 	/**
 	 * Color list label provider.
 	 * 
 	 * 
 	 */
 	private class ColorListLabelProvider extends LabelProvider {
 		/*
 		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
 		 */
 		public String getText(Object element) {
 			if (element instanceof String)
 				return (String) element;
 			return ((HighlightingColorListItem) element).getDisplayName();
 		}
 	}
 
 	/**
 	 * Color list content provider.
 	 * 
 	 * 
 	 */
 	protected String[] getCategories() {
 		return new String[] { sCoreCategory, sDocumentationCategory,
 				sCommentsCategory };
 	}
 
 	private class ColorListContentProvider implements ITreeContentProvider {
 
 		/*
 		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
 		 */
 		public Object[] getElements(Object inputElement) {
 			List categorys = new ArrayList();
 			String[] cats = getCategories();
 			for (int a = 0; a < cats.length; a++) {
 				if (getElementsForCategory(cats[a]).length > 0) {
 					categorys.add(cats[a]);
 				}
 			}
 			return categorys.toArray();
 		}
 
 		/*
 		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 		 */
 		public void dispose() {
 		}
 
 		/*
 		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
 		 *      java.lang.Object, java.lang.Object)
 		 */
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 
 		public Object[] getChildren(Object parentElement) {
 			if (parentElement instanceof String) {
 				String entry = (String) parentElement;
 				return getElementsForCategory(entry);
 			}
 			return new Object[0];
 		}
 
 		public Object getParent(Object element) {
 			if (element instanceof String)
 				return null;
 			int index = fListModel.indexOf(element);
 			if (index < 4)
 				return sDocumentationCategory;
 			if (index >= 7)
 				return sCoreCategory;
 			return sCommentsCategory;
 		}
 
 		public boolean hasChildren(Object element) {
 			return element instanceof String;
 		}
 	}
 
 	private static final String BOLD = PreferenceConstants.EDITOR_BOLD_SUFFIX;
 
 	/**
 	 * Preference key suffix for italic preferences.
 	 * 
 	 * 
 	 */
 	private static final String ITALIC = PreferenceConstants.EDITOR_ITALIC_SUFFIX;
 
 	/**
 	 * Preference key suffix for strikethrough preferences.
 	 * 
 	 * 
 	 */
 	private static final String STRIKETHROUGH = PreferenceConstants.EDITOR_STRIKETHROUGH_SUFFIX;
 
 	/**
 	 * Preference key suffix for underline preferences.
 	 * 
 	 * 
 	 */
 	private static final String UNDERLINE = PreferenceConstants.EDITOR_UNDERLINE_SUFFIX;
 
 	protected final static String sCoreCategory = PreferencesMessages.DLTKEditorPreferencePage_coloring_category_DLTK;
 
 	protected final static String sDocumentationCategory = PreferencesMessages.DLTKEditorPreferencePage_coloring_category_DLTKdoc;
 
 	protected final static String sCommentsCategory = PreferencesMessages.DLTKEditorPreferencePage_coloring_category_comments;
 
 	private ColorSelector fSyntaxForegroundColorEditor;
 
 	private Label fColorEditorLabel;
 
 	private Button fBoldCheckBox;
 
 	private Button fEnableCheckbox;
 
 	/**
 	 * Check box for italic preference.
 	 * 
 	 * 
 	 */
 	private Button fItalicCheckBox;
 
 	/**
 	 * Check box for strikethrough preference.
 	 * 
 	 * 
 	 */
 	private Button fStrikethroughCheckBox;
 
 	/**
 	 * Check box for underline preference.
 	 * 
 	 * 
 	 */
 	private Button fUnderlineCheckBox;
 
 	/**
 	 * Highlighting color list
 	 * 
 	 * 
 	 */
 	private final java.util.List fListModel = new ArrayList();
 
 	/**
 	 * Highlighting color list viewer
 	 * 
 	 * 
 	 */
 	private StructuredViewer fListViewer;
 
 	/**
 	 * Semantic highlighting manager
 	 * 
 	 * 
 	 */
 	// private SemanticHighlightingManager fSemanticHighlightingManager;
 	/**
 	 * The previewer.
 	 * 
 	 * 
 	 */
 	private ProjectionViewer fPreviewViewer;
 	/**
 	 * The color manager.
 	 * 
 	 * 
 	 */
 	private IColorManager fColorManager;
 
 	protected abstract String[][] getSyntaxColorListModel();
 
 	protected abstract ProjectionViewer createPreviewViewer(Composite parent,
 			IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
 			boolean showAnnotationsOverview, int styles, IPreferenceStore store);
 
 	protected abstract ScriptSourceViewerConfiguration createSimpleSourceViewerConfiguration(
 			IColorManager colorManager, IPreferenceStore preferenceStore,
 			ITextEditor editor, boolean configureFormatter);
 
 	protected abstract void setDocumentPartitioning(IDocument document);
 
 	public Object[] getElementsForCategory(String entry) {
 		List elements = new ArrayList();
 
 		Iterator i = this.fListModel.iterator();
 		while (i.hasNext()) {
 			HighlightingColorListItem item = (HighlightingColorListItem) i
 					.next();
 			if (item.getCategory().equals(entry)) {
 				elements.add(item);
 			}
 		}
 		return elements.toArray();
 	}
 
 	public AbstractScriptEditorColoringConfigurationBlock(
 			OverlayPreferenceStore store) {
 		super(store);
 
 		fColorManager = new DLTKColorManager(false);
 
 		String[][] model = getSyntaxColorListModel();
 
 		for (int i = 0, n = model.length; i < n; i++)
 			fListModel.add(new HighlightingColorListItem(model[i][0],
 					model[i][1], model[i][1] + BOLD, model[i][1] + ITALIC,
 					model[i][1] + STRIKETHROUGH, model[i][1] + UNDERLINE,
 					model[i][2]));
 
 		// SemanticHighlighting[] semanticHighlightings = SemanticHighlightings
 		// .getSemanticHighlightings();
 		// for (int i = 0, n = semanticHighlightings.length; i < n; i++)
 		// fListModel
 		// .add(new SemanticHighlightingColorListItem(
 		// semanticHighlightings[i].getDisplayName(),
 		// SemanticHighlightings
 		// .getColorPreferenceKey(semanticHighlightings[i]),
 		// SemanticHighlightings
 		// .getBoldPreferenceKey(semanticHighlightings[i]),
 		// SemanticHighlightings
 		// .getItalicPreferenceKey(semanticHighlightings[i]),
 		// SemanticHighlightings
 		// .getStrikethroughPreferenceKey(semanticHighlightings[i]),
 		// SemanticHighlightings
 		// .getUnderlinePreferenceKey(semanticHighlightings[i]),
 		// SemanticHighlightings
 		// .getEnabledPreferenceKey(semanticHighlightings[i])));
 
 		store.addKeys(createOverlayStoreKeys());
 	}
 
 	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
 
 		ArrayList overlayKeys = new ArrayList();
 
 		for (int i = 0, n = fListModel.size(); i < n; i++) {
 			HighlightingColorListItem item = (HighlightingColorListItem) fListModel
 					.get(i);
 			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
 					OverlayPreferenceStore.STRING, item.getColorKey()));
 			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
 					OverlayPreferenceStore.BOOLEAN, item.getBoldKey()));
 			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
 					OverlayPreferenceStore.BOOLEAN, item.getItalicKey()));
 			overlayKeys
 					.add(new OverlayPreferenceStore.OverlayKey(
 							OverlayPreferenceStore.BOOLEAN, item
 									.getStrikethroughKey()));
 			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
 					OverlayPreferenceStore.BOOLEAN, item.getUnderlineKey()));
 
 			if (item instanceof SemanticHighlightingColorListItem)
 				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
 						OverlayPreferenceStore.BOOLEAN,
 						((SemanticHighlightingColorListItem) item)
 								.getEnableKey()));
 		}
 
 		OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys
 				.size()];
 		overlayKeys.toArray(keys);
 		return keys;
 	}
 
 	/**
 	 * Creates page for hover preferences.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 * @return the control for the preference page
 	 */
 	public Control createControl(Composite parent) {
 		initializeDialogUnits(parent);
 		return createSyntaxPage(parent);
 	}
 
 	public void initialize() {
 		super.initialize();
 
 		fListViewer.setInput(fListModel);
 		fListViewer.setSelection(new StructuredSelection(sCoreCategory));
 	}
 
 	public void performDefaults() {
 		super.performDefaults();
 
 		handleSyntaxColorListSelection();
 
 		uninstallSemanticHighlighting();
 		installSemanticHighlighting();
 
 		fPreviewViewer.invalidateTextPresentation();
 	}
 
 	public void dispose() {
 		uninstallSemanticHighlighting();
 		fColorManager.dispose();
 
 		super.dispose();
 	}
 
 	private void handleSyntaxColorListSelection() {
 		HighlightingColorListItem item = getHighlightingColorListItem();
 		if (item == null) {
 			fEnableCheckbox.setEnabled(false);
 			fSyntaxForegroundColorEditor.getButton().setEnabled(false);
 			fColorEditorLabel.setEnabled(false);
 			fBoldCheckBox.setEnabled(false);
 			fItalicCheckBox.setEnabled(false);
 			fStrikethroughCheckBox.setEnabled(false);
 			fUnderlineCheckBox.setEnabled(false);
 			return;
 		}
 		RGB rgb = PreferenceConverter.getColor(getPreferenceStore(), item
 				.getColorKey());
 		fSyntaxForegroundColorEditor.setColorValue(rgb);
 		fBoldCheckBox.setSelection(getPreferenceStore().getBoolean(
 				item.getBoldKey()));
 		fItalicCheckBox.setSelection(getPreferenceStore().getBoolean(
 				item.getItalicKey()));
 		fStrikethroughCheckBox.setSelection(getPreferenceStore().getBoolean(
 				item.getStrikethroughKey()));
 		fUnderlineCheckBox.setSelection(getPreferenceStore().getBoolean(
 				item.getUnderlineKey()));
 		if (item instanceof SemanticHighlightingColorListItem) {
 			fEnableCheckbox.setEnabled(true);
 			boolean enable = getPreferenceStore().getBoolean(
 					((SemanticHighlightingColorListItem) item).getEnableKey());
 			fEnableCheckbox.setSelection(enable);
 			fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
 			fColorEditorLabel.setEnabled(enable);
 			fBoldCheckBox.setEnabled(enable);
 			fItalicCheckBox.setEnabled(enable);
 			fStrikethroughCheckBox.setEnabled(enable);
 			fUnderlineCheckBox.setEnabled(enable);
 		} else {
 			fSyntaxForegroundColorEditor.getButton().setEnabled(true);
 			fColorEditorLabel.setEnabled(true);
 			fBoldCheckBox.setEnabled(true);
 			fItalicCheckBox.setEnabled(true);
 			fStrikethroughCheckBox.setEnabled(true);
 			fUnderlineCheckBox.setEnabled(true);
 			fEnableCheckbox.setEnabled(false);
 			fEnableCheckbox.setSelection(true);
 		}
 	}
 
 	private Control createSyntaxPage(final Composite parent) {
 
 		Composite colorComposite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		colorComposite.setLayout(layout);
 
 		Link link = new Link(colorComposite, SWT.NONE);
 		link
 				.setText(PreferencesMessages.DLTKEditorColoringConfigurationBlock_link);
 		link.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				PreferencesUtil.createPreferenceDialogOn(parent.getShell(),
 						e.text, null, null);
 			}
 		});
 		// TODO replace by link-specific tooltips when
 		// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=88866 gets fixed
 		// link.setToolTipText(PreferencesMessages.EditorColoringConfigurationBlock_link_tooltip);
 
 		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
 		gridData.widthHint = 150; // only expand further if anyone else
 		// requires it
 		gridData.horizontalSpan = 2;
 		link.setLayoutData(gridData);
 
 		addFiller(colorComposite, 1);
 
 		Label label;
 		label = new Label(colorComposite, SWT.LEFT);
 		label
 				.setText(PreferencesMessages.DLTKEditorPreferencePage_coloring_element);
 		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Composite editorComposite = new Composite(colorComposite, SWT.NONE);
 		layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		editorComposite.setLayout(layout);
 		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
 		editorComposite.setLayoutData(gd);
 
 		fListViewer = new TreeViewer(editorComposite, SWT.SINGLE | SWT.BORDER);
 		fListViewer.setLabelProvider(new ColorListLabelProvider());
 		fListViewer.setContentProvider(new ColorListContentProvider());
 		fListViewer.setSorter(new ViewerSorter() {
 			public int category(Object element) {
 				// don't sort the top level categories
 				if (sCoreCategory.equals(element))
 					return 0;
 				if (sDocumentationCategory.equals(element))
 					return 1;
 				if (sCommentsCategory.equals(element))
 					return 2;
 				// to sort semantic settings after partition based ones:
 				// if (element instanceof SemanticHighlightingColorListItem)
 				// return 1;
 				return 0;
 			}
 		});
 		gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true);
 		gd.heightHint = convertHeightInCharsToPixels(9);
 		int maxWidth = 0;
 		for (Iterator it = fListModel.iterator(); it.hasNext();) {
 			HighlightingColorListItem item = (HighlightingColorListItem) it
 					.next();
 			maxWidth = Math.max(maxWidth, convertWidthInCharsToPixels(item
 					.getDisplayName().length()));
 		}
 		ScrollBar vBar = ((Scrollable) fListViewer.getControl())
 				.getVerticalBar();
 		if (vBar != null)
 			maxWidth += vBar.getSize().x * 3; // scrollbars and tree
 		// indentation guess
 		gd.widthHint = maxWidth;
 
 		fListViewer.getControl().setLayoutData(gd);
 
 		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
 		layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.numColumns = 2;
 		stylesComposite.setLayout(layout);
 		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		fEnableCheckbox = new Button(stylesComposite, SWT.CHECK);
 		fEnableCheckbox
 				.setText(PreferencesMessages.DLTKEditorPreferencePage_enable);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalAlignment = GridData.BEGINNING;
 		gd.horizontalSpan = 2;
 		fEnableCheckbox.setLayoutData(gd);
 
 		fColorEditorLabel = new Label(stylesComposite, SWT.LEFT);
 		fColorEditorLabel
 				.setText(PreferencesMessages.DLTKEditorPreferencePage_color);
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		gd.horizontalIndent = 20;
 		fColorEditorLabel.setLayoutData(gd);
 
 		fSyntaxForegroundColorEditor = new ColorSelector(stylesComposite);
 		Button foregroundColorButton = fSyntaxForegroundColorEditor.getButton();
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		foregroundColorButton.setLayoutData(gd);
 
 		fBoldCheckBox = new Button(stylesComposite, SWT.CHECK);
 		fBoldCheckBox
 				.setText(PreferencesMessages.DLTKEditorPreferencePage_bold);
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		gd.horizontalIndent = 20;
 		gd.horizontalSpan = 2;
 		fBoldCheckBox.setLayoutData(gd);
 
 		fItalicCheckBox = new Button(stylesComposite, SWT.CHECK);
 		fItalicCheckBox
 				.setText(PreferencesMessages.DLTKEditorPreferencePage_italic);
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		gd.horizontalIndent = 20;
 		gd.horizontalSpan = 2;
 		fItalicCheckBox.setLayoutData(gd);
 
 		fStrikethroughCheckBox = new Button(stylesComposite, SWT.CHECK);
 		fStrikethroughCheckBox
 				.setText(PreferencesMessages.DLTKEditorPreferencePage_strikethrough);
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		gd.horizontalIndent = 20;
 		gd.horizontalSpan = 2;
 		fStrikethroughCheckBox.setLayoutData(gd);
 
 		fUnderlineCheckBox = new Button(stylesComposite, SWT.CHECK);
 		fUnderlineCheckBox
 				.setText(PreferencesMessages.DLTKEditorPreferencePage_underline);
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		gd.horizontalIndent = 20;
 		gd.horizontalSpan = 2;
 		fUnderlineCheckBox.setLayoutData(gd);
 
 		label = new Label(colorComposite, SWT.LEFT);
 		label.setText(PreferencesMessages.DLTKEditorPreferencePage_preview);
 		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Control previewer = createPreviewer(colorComposite);
 		if (previewer != null) {
 			gd = new GridData(GridData.FILL_BOTH);
 			gd.widthHint = convertWidthInCharsToPixels(20);
 			gd.heightHint = convertHeightInCharsToPixels(5);
 			previewer.setLayoutData(gd);
 		} else {
 			label.dispose();
 		}
 
 		fListViewer
 				.addSelectionChangedListener(new ISelectionChangedListener() {
 					public void selectionChanged(SelectionChangedEvent event) {
 						handleSyntaxColorListSelection();
 					}
 				});
 
 		foregroundColorButton.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// do nothing
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				HighlightingColorListItem item = getHighlightingColorListItem();
 				PreferenceConverter.setValue(getPreferenceStore(), item
 						.getColorKey(), fSyntaxForegroundColorEditor
 						.getColorValue());
 			}
 		});
 
 		fBoldCheckBox.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// do nothing
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				HighlightingColorListItem item = getHighlightingColorListItem();
 				getPreferenceStore().setValue(item.getBoldKey(),
 						fBoldCheckBox.getSelection());
 			}
 		});
 
 		fItalicCheckBox.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// do nothing
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				HighlightingColorListItem item = getHighlightingColorListItem();
 				getPreferenceStore().setValue(item.getItalicKey(),
 						fItalicCheckBox.getSelection());
 			}
 		});
 		fStrikethroughCheckBox.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// do nothing
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				HighlightingColorListItem item = getHighlightingColorListItem();
 				getPreferenceStore().setValue(item.getStrikethroughKey(),
 						fStrikethroughCheckBox.getSelection());
 			}
 		});
 
 		fUnderlineCheckBox.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// do nothing
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				HighlightingColorListItem item = getHighlightingColorListItem();
 				getPreferenceStore().setValue(item.getUnderlineKey(),
 						fUnderlineCheckBox.getSelection());
 			}
 		});
 
 		fEnableCheckbox.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// do nothing
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				HighlightingColorListItem item = getHighlightingColorListItem();
 				if (item instanceof SemanticHighlightingColorListItem) {
 					boolean enable = fEnableCheckbox.getSelection();
 					getPreferenceStore().setValue(
 							((SemanticHighlightingColorListItem) item)
 									.getEnableKey(), enable);
 					fEnableCheckbox.setSelection(enable);
 					fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
 					fColorEditorLabel.setEnabled(enable);
 					fBoldCheckBox.setEnabled(enable);
 					fItalicCheckBox.setEnabled(enable);
 					fStrikethroughCheckBox.setEnabled(enable);
 					fUnderlineCheckBox.setEnabled(enable);
 					uninstallSemanticHighlighting();
 					installSemanticHighlighting();
 				}
 			}
 		});
 
 		colorComposite.layout(false);
 
 		return colorComposite;
 	}
 
 	private void addFiller(Composite composite, int horizontalSpan) {
 		PixelConverter pixelConverter = new PixelConverter(composite);
 		Label filler = new Label(composite, SWT.LEFT);
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd.horizontalSpan = horizontalSpan;
 		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(1) / 2;
 		filler.setLayoutData(gd);
 	}
 
 	private Control createPreviewer(Composite parent) {
 
 		IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
 		IPreferenceStore store = new ChainedPreferenceStore(
 				new IPreferenceStore[] {
 						getPreferenceStore(),
 						new PreferencesAdapter(
 								createTemporaryCorePreferenceStore()),
 						generalTextStore });
 		fPreviewViewer = this.createPreviewViewer(parent, null, null, false,
 				SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER, store);
 
 		if (fPreviewViewer == null) {
 			return null;
 		}
 		ScriptSourceViewerConfiguration configuration = createSimpleSourceViewerConfiguration(
 				fColorManager, store, null, false);
 		fPreviewViewer.configure(configuration);
 
 		// Font font = JFaceResources
 		// .getFont(PreferenceConstants.EDITOR_TEXT_FONT);
 		// fPreviewViewer.getTextWidget().setFont(font);
 		new ScriptSourcePreviewerUpdater(fPreviewViewer, configuration, store);
 		fPreviewViewer.setEditable(false);
 
 		String content = getPreviewContent();
 		IDocument document = new Document(content);
 		this.setDocumentPartitioning(document);
 
 		fPreviewViewer.setDocument(document);
 
 		installSemanticHighlighting();
 
 		return fPreviewViewer.getControl();
 	}
 
 	private Preferences createTemporaryCorePreferenceStore() {
 		Preferences result = new Preferences();
 
 		// result.setValue(COMPILER_TASK_TAGS, "TASK,TODO"); //$NON-NLS-1$
 
 		return result;
 	}
 
 	/**
 	 * Install Semantic Highlighting on the previewer
 	 * 
 	 * 
 	 */
 	private void installSemanticHighlighting() {
 		// if (fSemanticHighlightingManager == null) {
 		// fSemanticHighlightingManager = new SemanticHighlightingManager();
 		// fSemanticHighlightingManager.install(fPreviewViewer, fColorManager,
 		// getPreferenceStore(), createPreviewerRanges());
 		// }
 	}
 
 	/**
 	 * Uninstall Semantic Highlighting from the previewer
 	 * 
 	 * 
 	 */
 	private void uninstallSemanticHighlighting() {
 		// if (fSemanticHighlightingManager != null) {
 		// fSemanticHighlightingManager.uninstall();
 		// fSemanticHighlightingManager = null;
 		// }
 	}
 
 	/**
 	 * Returns the current highlighting color list item.
 	 * 
 	 * @return the current highlighting color list item
 	 * 
 	 */
 	private HighlightingColorListItem getHighlightingColorListItem() {
 		IStructuredSelection selection = (IStructuredSelection) fListViewer
 				.getSelection();
 		Object element = selection.getFirstElement();
 		if (element instanceof String)
 			return null;
 		return (HighlightingColorListItem) element;
 	}
 
 	protected String getPreviewContent() {
 		String separator = System.getProperty("line.separator"); //$NON-NLS-1$
 
 		StringBuffer buffer = new StringBuffer(512);
 
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new InputStreamReader(
 					getPreviewContentReader()));
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				buffer.append(line);
 				buffer.append(separator);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		return buffer.toString();
 	}
 
	protected abstract InputStream getPreviewContentReader();
 }
