 /**
  * Copyright (c) 2009 Anyware Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Anyware Technologies - initial API and implementation
  *
 * $Id: PDEFormToolkit.java,v 1.5 2009/08/21 16:57:04 bcabe Exp $
  */
 package org.eclipse.pde.emfforms.editor;
 
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.forms.ManagedForm;
 import org.eclipse.ui.forms.widgets.*;
 
 /**
  * TODO remove crap in this class (static widget factories is definitely not a good idea) 
  */
 public class PDEFormToolkit extends FormToolkit {
 	public static class Pair<U, V> {
 		public U left;
 
 		public V right;
 
 		public Pair(U u, V v) {
 			left = u;
 			right = v;
 		}
 	}
 
 	private static final String KEEP_CONTROL_FOREGROUND = "KEEPFOREGROUND"; //$NON-NLS-1$
 
 	private static final String KEEP_CONTROL_BACKGROUND = "KEEPBACKGROUND"; //$NON-NLS-1$
 
 	public PDEFormToolkit(Display display) {
 		super(display);
 	}
 
 	private void super$adapt(Composite composite) {
 		composite.setBackground(getColors().getBackground());
 		composite.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				((Control) e.widget).setFocus();
 			}
 		});
 		// composite.setMenu(composite.getParent().getMenu());
 	}
 
 	public void adapt(Composite composite) {
 		// adapt every subcomposite & subcontrol
 		if (composite == null)
 			return;
 
 		super$adapt(composite);
 
 		// Recursively set the enabled state of all children
 		Control[] children = composite.getChildren();
 		for (int i = 0; i < children.length; i++) {
 			if (children[i] instanceof Composite) {
 				adapt((Composite) children[i]);
 			} else if (!(composite instanceof Section)) {
 				Boolean keepForeground = (Boolean) children[i].getData(KEEP_CONTROL_FOREGROUND);
 				Boolean keepBackground = (Boolean) children[i].getData(KEEP_CONTROL_BACKGROUND);
 
 				Color c = null;
 				if (keepForeground != null && keepForeground.booleanValue()) {
 					c = children[i].getForeground();
 				}
 
 				Color c2 = null;
 				if (keepBackground != null && keepBackground.booleanValue()) {
 					c2 = children[i].getBackground();
 				}
 
 				super.adapt(children[i], true, true);
 
 				if (c != null)
 					children[i].setForeground(c);
 
 				if (c2 != null)
 					children[i].setBackground(c2);
 			}
 		}
 	}
 
 	public Combo createCombo(Composite parent, int style) {
 		Combo combo = new Combo(parent, style | SWT.FLAT);
 		adapt(combo, false, false);
 		// hookDeleteListener(table);
 		return combo;
 	}
 
 	public List createList(Composite parent, int style) {
 		List list = new List(parent, style | SWT.FLAT);
 		adapt(list, false, false);
 		// hookDeleteListener(table);
 		return list;
 	}
 
 	public ManagedForm createManagedForm(FormToolkit toolkit, ScrolledForm sForm) {
 		return new ManagedForm(toolkit, sForm);
 	}
 
 	public ManagedForm createManagedForm(Composite parent) {
 		return new ManagedForm(parent);
 	}
 
 	public StyledText createStyledText(Composite parent, int style) {
 		StyledText styledText = new StyledText(parent, style);
 		adapt(styledText, false, false);
 		return styledText;
 	}
 
 	public Group createGroup(Composite parent, String text) {
 		Group group = new Group(parent, SWT.NONE);
 		group.setText(text);
 		adapt(group, false, false);
 		return group;
 	}
 
 	public ComboViewer createComboViewer(Composite parent, int style) {
 		ComboViewer cviewer = new ComboViewer(parent, style);
 		adapt(cviewer.getCombo(), false, false);
 
 		return cviewer;
 	}
 
 	public TableViewer createTableViewer(Composite parent, int style) {
 		TableViewer tviewer = new TableViewer(parent, style);
 		adapt(tviewer.getTable(), false, false);
 
 		return tviewer;
 	}
 
 	public DateTime createDateTime(Composite parent, int style) {
 		DateTime dateTime = new DateTime(parent, style);
 		adapt(dateTime, false, false);
 		return dateTime;
 	}
 
 	public static Pair<Text, Button> createLabelAndBrowseText(String label, Composite composite) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
 		Label labelName = new Label(composite, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelName);
 		labelName.setAlignment(SWT.LEFT);
 
 		Composite browseComposite = new Composite(composite, SWT.NONE);
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(browseComposite);
 		Text text = new Text(browseComposite, SWT.BORDER | SWT.READ_ONLY);
 		text.setEditable(false);
 		text.setEnabled(false);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
 		Button browseButton = new Button(browseComposite, SWT.FLAT | SWT.PUSH);
 		browseButton.setText("..."); //$NON-NLS-1$
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(browseComposite);
 		return new Pair<Text, Button>(text, browseButton);
 	}
 
 	public static Pair<ListViewer, Pair<Button, Button>> createLabelAndListAddRemove(String label, Composite composite) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
 		Label labelName = new Label(composite, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(labelName);
 		labelName.setAlignment(SWT.LEFT);
 
 		Composite browseComposite = new Composite(composite, SWT.NONE);
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(browseComposite);
 		ListViewer listViewer = new ListViewer(browseComposite, SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).span(1, 2).hint(SWT.DEFAULT, 80).applyTo(listViewer.getList());
 		Button addButton = new Button(browseComposite, SWT.FLAT | SWT.PUSH);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addButton);
 		addButton.setText("+"); //$NON-NLS-1$
 		Button removeButton = new Button(browseComposite, SWT.FLAT | SWT.PUSH);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(removeButton);
 		removeButton.setText("-"); //$NON-NLS-1$
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(browseComposite);
 		return new Pair<ListViewer, Pair<Button, Button>>(listViewer, new Pair<Button, Button>(addButton, removeButton));
 	}
 
 	public static ListViewer createLabelAndList(String label, Composite composite) {
 		return createLabelAndList(label, composite, SWT.DEFAULT);
 	}
 
 	public static ListViewer createLabelAndList(String label, Composite composite, int labelHorizontalWidthHint) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
 		Label labelName = new Label(composite, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().span(2, 1).align(SWT.FILL, SWT.BEGINNING).hint(labelHorizontalWidthHint, SWT.DEFAULT).applyTo(labelName);
 		labelName.setAlignment(SWT.LEFT);
 
 		ListViewer listViewer = new ListViewer(composite, SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(SWT.DEFAULT, 80).applyTo(listViewer.getList());
 
 		return listViewer;
 	}
 
 	/**
 	 * @param label
 	 * @param generalInfoComposite
 	 * @return
 	 */
 	public static Text createLabelAndText(String label, Composite composite, int labelHorizontalWidthHint) {
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
 		Label labelName = new Label(composite, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(labelHorizontalWidthHint, SWT.DEFAULT).applyTo(labelName);
 		labelName.redraw();
 
 		labelName.setAlignment(SWT.LEFT);
 
 		Text text = new Text(composite, SWT.BORDER);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
 		return text;
 	}
 
 	/**
 	 * @param label
 	 * @param generalInfoComposite
 	 * @return
 	 */
 	public static Text createLabelAndText(String label, Composite composite) {
 		return createLabelAndText(label, composite, SWT.DEFAULT);
 	}
 
 	/**
 	 * @param label
 	 * @param generalInfoComposite
 	 * @return
 	 */
 	public static Text createLabelAndTextArea(String label, Composite composite) {
 		return createLabelAndTextArea(label, composite, false);
 	}
 
 	public static Text createLabelAndTextArea(String label, Composite composite, int labelHorizontalWidthHint) {
 		return createLabelAndTextArea(label, composite, false, labelHorizontalWidthHint);
 	}
 
 	/**
 	 * @param label
 	 * @param generalInfoComposite
 	 * @return
 	 */
 	public static Text createLabelAndTextArea(String label, Composite composite, boolean scrollable) {
 		return createLabelAndTextArea(label, composite, scrollable, SWT.DEFAULT);
 	}
 
 	public static Text createLabelAndTextArea(String label, Composite composite, boolean scrollable, int labelHorizontalWidthHint) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
 		Label labelName = new Label(composite, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).hint(labelHorizontalWidthHint, SWT.DEFAULT).applyTo(labelName);
 		labelName.setAlignment(SWT.LEFT);
 
 		int sytle = SWT.BORDER | SWT.MULTI;
 		if (scrollable) {
 			sytle = sytle | SWT.WRAP | SWT.V_SCROLL;
 		}
 		Text text = new Text(composite, sytle);
 		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 120).applyTo(text);
 		return text;
 	}
 
 	/**
 	 * @param label
 	 * @param generalInfoComposite
 	 * @return
 	 */
 	public static ComboViewer createLabelAndComboViewer(String label, Composite composite) {
 		return createLabelAndComboViewer(label, composite, SWT.DEFAULT);
 	}
 
 	public static ComboViewer createLabelAndComboViewer(String label, Composite composite, int labelHorizontalWidthHint) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
 		Label labelName = new Label(composite, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(labelHorizontalWidthHint, SWT.DEFAULT).applyTo(labelName);
 		labelName.setAlignment(SWT.LEFT);
 
 		ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
 		comboViewer.setContentProvider(new ArrayContentProvider());
 		comboViewer.setLabelProvider(new LabelProvider());
 		// sort the viewer using labels
 		comboViewer.setSorter(new ViewerSorter());
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboViewer.getControl());
 		return comboViewer;
 	}
 
 	/**
 	 * @param label
 	 * @param composite
 	 * @param comboStyle
 	 * @return
 	 */
 	public static ComboViewer createLabelAndNonReadOnlyComboViewer(String label, Composite composite) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
 		Label labelName = new Label(composite, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelName);
 		labelName.setAlignment(SWT.LEFT);
 		ComboViewer comboViewer;
 		comboViewer = new ComboViewer(composite, SWT.BORDER);
 		comboViewer.setContentProvider(new ArrayContentProvider());
 		comboViewer.setLabelProvider(new LabelProvider());
 		// sort the viewer using labels
 		comboViewer.setSorter(new ViewerSorter());
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboViewer.getControl());
 		return comboViewer;
 	}
 
 	/**
 	 * @param label
 	 * @param generalInfoComposite
 	 * @return
 	 */
 	public static DateTime createLabelAndCalendar(String label, Composite composite) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
 		Label labelName = new Label(composite, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelName);
 		labelName.setAlignment(SWT.LEFT);
 
 		DateTime dateTime = new DateTime(composite, SWT.CALENDAR);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(dateTime);
 		return dateTime;
 	}
 
 	/**
 	 * @param string
 	 * @param composite
 	 * @return a checkBox
 	 */
 	public static Button createLabelAndCheckBox(String label, Composite composite) {
 		return createLabelAndButton(label, composite, SWT.CHECK, SWT.DEFAULT);
 	}
 
 	public static Button createLabelAndCheckBox(String label, Composite composite, int labelHorizontalWidthHint) {
 		return createLabelAndButton(label, composite, SWT.CHECK, labelHorizontalWidthHint);
 	}
 
 	/**
 	 * @param string
 	 * @param composite
 	 * @return a checkBox
 	 */
 	public static Button createCheckBoxAndLabel(String label, Composite composite) {
 		return createButtonAndLabel(label, composite, SWT.CHECK);
 	}
 
 	/**
 	 * 
 	 * @param label
 	 * @param composite
 	 * @return a radio button
 	 */
 	public static Button createRadioAndLabel(String label, Composite composite) {
 		return createButtonAndLabel(label, composite, SWT.RADIO);
 	}
 
 	/**
 	 * @param label
 	 * @param composite
 	 * @param type
 	 * @return a button of type "type"
 	 */
 	private static Button createButtonAndLabel(String label, Composite composite, int type) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
 		Button button = new Button(composite, type | SWT.FLAT);
 		button.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).span(2, 1).applyTo(button);
 		return button;
 	}
 
 	/**
 	 * @param label
 	 * @param composite
 	 * @param type
 	 * @return a button of type "type"
 	 */
 	private static Button createLabelAndButton(String label, Composite parent, int type, int labelHorizontalWidthHint) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);
 
 		Label labelName = new Label(parent, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(labelHorizontalWidthHint, SWT.DEFAULT).applyTo(labelName);
 		labelName.setAlignment(SWT.LEFT);
 
 		Button button = new Button(parent, type | SWT.FLAT);
 		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(button);
 		return button;
 	}
 
 	/**
 	 * @param label
 	 * @param generalInfoComposite
 	 * @return
 	 */
 	public static Spinner createLabelAndSpinner(String label, Composite parent) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);
 
 		Label labelName = new Label(parent, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelName);
 		labelName.setAlignment(SWT.LEFT);
 
 		Spinner spinner = new Spinner(parent, SWT.WRAP);
 		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(spinner);
 		return spinner;
 	}
 
 	/**
 	 * For readonly
 	 * 
 	 * @param label
 	 * @param generalInfoComposite
 	 * @return
 	 */
 	public static Label createLabelAndLabel(String label, Composite parent) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);
 
 		Label labelName = new Label(parent, SWT.NONE);
 		labelName.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelName);
 		labelName.setAlignment(SWT.LEFT);
 
 		Label labelValue = new Label(parent, SWT.NONE);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelValue);
 		return labelValue;
 	}
 
 	/**
 	 * @param section
 	 * @return a toolbar manager for a given {@link Section} with a sexy cursor
 	 */
 	public static ToolBarManager createSectionToolBarManager(Composite section) {
 		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
 		ToolBar toolbar = toolBarManager.createControl(section);
 		final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
 		toolbar.setCursor(handCursor);
 		// Cursor needs to be explicitly disposed
 		toolbar.addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				if ((handCursor != null) && (handCursor.isDisposed() == false)) {
 					handCursor.dispose();
 				}
 			}
 		});
 		return toolBarManager;
 	}
 
 	public static Link createLink(String label, Composite composite) {
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
 		Link link = new Link(composite, SWT.NONE);
 		link.setText(label);
 		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(link);
 
 		return link;
 	}
 }
