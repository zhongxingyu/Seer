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
 * $Id: AbstractSourcePage.java,v 1.1 2009/08/19 14:54:21 bcabe Exp $
  */
 package org.eclipse.pde.emfforms.internal.editor;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Collections;
 import java.util.EventObject;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.pde.emfforms.editor.AbstractEmfFormPage;
 import org.eclipse.pde.emfforms.editor.EmfFormEditor;
 import org.eclipse.pde.emfforms.internal.Activator;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 
 public abstract class AbstractSourcePage extends AbstractEmfFormPage {
 	private SourceViewer _sourceViewer;
 
 	public static final String ID = "emfforms.source"; //$NON-NLS-1$
 
 	protected static final int VERTICAL_RULER_WIDTH = 12;
 
 	private EditingDomain _editingDomain;
 	private CommandStackListener _commandStackListener;
 
 	/**
 	 * @param editor
 	 */
 	public AbstractSourcePage(EmfFormEditor<?> editor) {
 		super(editor);
 	}
 
 	public void bind(DataBindingContext bindingContext) {
 		_editingDomain = ((EmfFormEditor<?>) getEditor()).getEditingDomain();
 
 		_commandStackListener = new CommandStackListener() {
 			public void commandStackChanged(EventObject event) {
 				refreshSourceContent();
 			}
 		};
 		_editingDomain.getCommandStack().addCommandStackListener(_commandStackListener);
 
 		// initialize content
 		refreshSourceContent();
 	}
 
 	private void refreshSourceContent() {
 		EObject obj = (EObject) getObservedValue().getValue();
 
 		// Copies the root to avoid modifying it
 		final StringWriter writer = new StringWriter();
 		try {
 			((XMLResource) obj.eResource()).save(writer, Collections.EMPTY_MAP);
 		} catch (IOException e) {
 			Activator.log(e);
 		}
 		final String result = writer.toString();
 		writer.flush();
 		if (_sourceViewer != null)
 			_sourceViewer.getDocument().set(result);
 	}
 
 	public void createContents(Composite parent) {
 		GridLayout gl = new GridLayout(getNumColumns(), true);
 		gl.verticalSpacing = 0;
 		parent.setLayout(gl);
 
 		_sourceViewer = createSourceViewer(parent);
 
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(_sourceViewer.getControl());
 	}
 
 	public abstract SourceViewer createSourceViewer(Composite parent);
 
 	@Override
 	public String getId() {
 		return ID;
 	}
 
 	@Override
 	public String getPartName() {
 		return "Source"; //$NON-NLS-1$
 	}
 
 	@Override
 	public Viewer getViewer() {
 		return null;
 	}
 
 	private IObservableValue getObservedValue() {
 		return ((EmfFormEditor<?>) getEditor()).getInputObservable();
 	}
 
 	@Override
 	public void dispose() {
		_editingDomain.getCommandStack().removeCommandStackListener(_commandStackListener);
 
 		super.dispose();
 	}
 }
