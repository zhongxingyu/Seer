 // ISOKeyPicker.java
 package org.eclipse.stem.ui.widgets;
 
 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.stem.data.geography.GeographicNames;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.FilteredList;
 
 /**
  * This class is a SWT Composite that allows a user to specify an ISO key by
  * picking from an expandable hierarchy of country names and their
  * sub-divisions.
  */
 public class ISOKeyPicker extends Composite {
 
 	private final List<ISOKeyPickedEventListener> listeners = new CopyOnWriteArrayList<ISOKeyPickedEventListener>();
 
 	private final Label isoKeyLevelDescription;
 	private final FilteredList filteredList;
	private final Text text;
 	private int isoKeyLevel;
 
 	/**
 	 * Create the composite
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	public ISOKeyPicker(final Composite parent, final int style,
 			final int isoKeyLevel) {
 		super(parent, style);
 		this.isoKeyLevel = isoKeyLevel;
 		setLayout(new FormLayout());
 
 		text = new Text(this, SWT.BORDER);
 		final FormData fd_text = new FormData();
 		fd_text.top = new FormAttachment(0, 0);
 		fd_text.left = new FormAttachment(25, 0);
 		fd_text.right = new FormAttachment(100, 0);
 		text.setLayoutData(fd_text);
 		text.addListener(SWT.Modify, new Listener() {
 			public void handleEvent(final Event event) {
 				filteredList.setFilter(text.getText());
 			}
 		});
 		isoKeyLevelDescription = new Label(this, SWT.NONE);
 		isoKeyLevelDescription.setText("Label");
 		FormData fd_isoKeyLevelDescription;
 		fd_isoKeyLevelDescription = new FormData();
 		fd_isoKeyLevelDescription.top = new FormAttachment(text, 2, SWT.TOP);
 		fd_isoKeyLevelDescription.right = new FormAttachment(25, 0);
 		fd_isoKeyLevelDescription.left = new FormAttachment(0, 0);
 		this.isoKeyLevelDescription.setLayoutData(fd_isoKeyLevelDescription);
 
 		filteredList = new FilteredList(this, SWT.BORDER, null, false, false,
 				true);
 		final FormData fd_filteredList = new FormData();
 		fd_filteredList.top = new FormAttachment(text, 5, SWT.DEFAULT);
 		fd_filteredList.bottom = new FormAttachment(100, 0);
 		fd_filteredList.left = new FormAttachment(text, 0, SWT.LEFT);
 		fd_filteredList.right = new FormAttachment(text, 0, SWT.RIGHT);
 		filteredList.setLayoutData(fd_filteredList);
 		filteredList.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				final Object[] selected = filteredList.getSelection();
 				// Anything selected?
 				if (selected.length == 1) {
 					final String isoKey = (String) selected[0];
 					fireISOKeyPicked(isoKey);
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(final SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 
 		filteredList.setIgnoreCase(true);
 		filteredList.setFilter("");
 		filteredList.setLabelProvider(new LabelProvider() {
 			@Override
 			public String getText(final Object element) {
 				return getName((String) element, isoKeyLevel);
 			}
 		});
 		filteredList.setElements(new Object[] {});
 
 		pack();
 	} // ISOKeyPicker
 
 	public void setISOKeys(final Object[] isoKeys) {
 		filteredList.setElements(isoKeys);
		text.setText("");
 	} // setISOKeys
 
 	public void setISOKeyLevelDescription(final String isoKeyLevelDescription) {
 		this.isoKeyLevelDescription.setText(isoKeyLevelDescription);
 	} // setISOKeyLevelDescription
 
 	/**
 	 * @return the iso key level
 	 */
 	public int getISOKeyLevel() {
 		return isoKeyLevel;
 	}
 
 	public void setISOKeyLevel(final int isoKeyLevel) {
 		this.isoKeyLevel = isoKeyLevel;
 	} // setLevel
 
 	private String getName(final String isoKey, final int level) {
 		return GeographicNames.getName(isoKey, level);
 	} // getName
 
 	public void addISOKeyPickedListener(final ISOKeyPickedEventListener listener) {
 		listeners.add(listener);
 	} // addISOKeyPickedListener
 
 	public void removeISOKeyPickedListener(
 			final ISOKeyPickedEventListener listener) {
 		listeners.remove(listener);
 	} // removeISOKeyPickedListener
 
 	private void fireISOKeyPicked(final String isoKey) {
 		final ISOKeyPickedEvent isoKeyPickedEvent = new ISOKeyPickedEvent(this,
 				isoKey);
 		for (final ISOKeyPickedEventListener listener : listeners) {
 			listener.isoKeyPicked(isoKeyPickedEvent);
 		} // for each ISOKeyPickedEventListener
 	}
 } // ISOKeyPicker
