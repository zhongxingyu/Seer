 /*******************************************************************************
  * Copyright (c) 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.widgets.imageviewer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.eef.runtime.EEFRuntimePlugin;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 
 /**
  * @author <a href="mailto:glefur@obeo.fr">glefur</a>
  *
  */
 public class EEFImageViewer extends Composite implements ISelectionProvider {
 
 	private static final String[] EXTENSIONS = new String[] { "*.png", "*.bmp", "*.jpeg", "*.jpg", "*.gif", "*.*" };
 	
 	private Canvas viewerCanvas;
 	
 	private String key;
 
 	private String imagePath;
 	
 	private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();
 	
 	/**
 	 * @param parent
 	 * @param style
 	 */
 	public EEFImageViewer(Composite parent, int style) {
 		super(parent, style);
 		setLayout(new FillLayout());
 		viewerCanvas = new Canvas(this, SWT.NONE);
 		viewerCanvas.addMouseListener(new MouseAdapter() {
 
 			/**
 			 * {@inheritDoc}
 			 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
 			 */
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				if (isEnabled()) {
 					FileDialog dialog = new FileDialog(getParent().getShell(), SWT.OPEN);
 					dialog.setFilterExtensions(EXTENSIONS);
 					String fn = dialog.open();
 					if (fn != null) {
 						updateImage(fn);
 						SelectionChangedEvent event = new SelectionChangedEvent(EEFImageViewer.this, new StructuredSelection(fn));
 						for (ISelectionChangedListener listener : listeners) {
 							listener.selectionChanged(event);
 						}
 					}
 				}
 			}
 			
 		});
 	}
 	
 	/**
 	 * @param key the key to set
 	 */
 	public void initViewer(String key, String imagePath) {
 		this.key = key;
 		updateImage(imagePath);
 	}
 	
 	/**
 	 * 
 	 */
 	private void updateImage(String imagePath) {
 		this.imagePath = imagePath;
 		EEFRuntimePlugin.getDefault().registerImage(key, imagePath);
 		viewerCanvas.setBackgroundImage(EEFRuntimePlugin.getDefault().getRegisteredImage(key));
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.add(listener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
 	 */
 	public ISelection getSelection() {
		return new StructuredSelection(imagePath);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.remove(listener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setSelection(ISelection selection) {
 		if (selection instanceof StructuredSelection) {
 			StructuredSelection sSelection = (StructuredSelection) selection;
 			if (sSelection.getFirstElement() instanceof String) {
 				String path = (String) sSelection.getFirstElement();
 				updateImage(path);
 			}
 		}
 	}
 	
 	
 
 }
