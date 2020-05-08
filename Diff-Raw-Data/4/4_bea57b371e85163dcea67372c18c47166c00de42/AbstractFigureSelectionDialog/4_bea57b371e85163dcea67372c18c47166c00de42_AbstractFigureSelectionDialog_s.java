 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 323034 - Aligned vertical gaps between groups
  *    mgorning - Bug 352874 - Exports of Diagrams > 3000x3000 px 
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.util.ui.print;
 
 import java.util.List;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.SWTGraphics;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.LayerConstants;
 import org.eclipse.gef.editparts.LayerManager;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.ui.internal.Messages;
 import org.eclipse.graphiti.ui.internal.editor.GFFigureCanvas;
 import org.eclipse.graphiti.ui.internal.fixed.FixedScaledGraphics;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.graphiti.ui.internal.util.ui.DefaultPreferences;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * This is an abstract dialog, where the user can choose between the whole
  * figure or a selection.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class AbstractFigureSelectionDialog extends Dialog implements SelectionListener {
 
 	// initial values
 	protected GraphicalViewer _graphicalViewer;
 
 	/**
 	 * <code>_allFigure</code> represents a figure that contains all printable
 	 * layers
 	 */
 	protected IFigure _allFigure;
 
 	/**
 	 * <code>_selectionFigure</code> represents a figure which corresponds to
 	 * the selected EditPart
 	 */
 	protected IFigure _selectionFigure;
 
 	protected boolean _insideInternalModify = false;
 
 	private Button _allFigureButton;
 
 	private Button _selectionFigureButton;
 
 	// selected values
 
 	/**
 	 * <code>_figure</code> corresponds either to the value of
 	 * <code>_allFigure</code> or to the value of <code>_selectionFigure</code>
 	 */
 	protected IFigure _figure;
 
 	private Image _imageSelection;
 
 	/**
 	 * Image corresponding to the whole diagram (unscaled) or <b>null</b> if the
 	 * diagram is too large
 	 */
 	private Image _imageAll;
 
 	/**
 	 * Image corresponding to either one selected part of the diagram or the
 	 * whole diagram (non-scaled version) - can be <b>null</b> if the diagram is
 	 * too large and nothing is selected
 	 */
 	protected Image _image;
 
 	/**
 	 * Image corresponding to either one selected part of the diagram or the
 	 * whole diagram (scaled version)
 	 */
 	private Image _scaledImage;
 
 	protected DefaultPreferences _preferences;
 
 	private GraphicalEditPart _selectedEditPart;
 
 	/**
 	 * Creates a new AbstractPrintFigureDialog.
 	 * 
 	 * @param shell
 	 *            The Shell of this dialog.
 	 * @param graphicalViewer
 	 *            The GraphicalViewer, which to print.
 	 */
 	public AbstractFigureSelectionDialog(Shell shell, GraphicalViewer graphicalViewer) {
 		super(shell);
 		_graphicalViewer = graphicalViewer;
 		_preferences = new DefaultPrintPreferences();
 		determinePossibleFigures();
 	}
 
 	@Override
 	protected void configureShell(Shell newShell) {
 		newShell.setText(Messages.AbstractFigureSelectionDialog_0_xtxt);
 		super.configureShell(newShell);
 	}
 
 	protected Group createChooseFigureGroup(Composite composite) {
 		Group figureGroup = new Group(composite, SWT.NONE);
 		figureGroup.setText(Messages.AbstractFigureSelectionDialog_1_xtxt);
 		GridLayout layout = new GridLayout(1, false);
 		figureGroup.setLayout(layout);
 		figureGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 
 		boolean singleSelection = isFigureSelected();
 
 		_allFigureButton = new Button(figureGroup, SWT.RADIO);
 		_allFigureButton.setText(Messages.AbstractFigureSelectionDialog_2_xbut);
 		_allFigureButton.setSelection(!singleSelection);
 		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
 		_allFigureButton.setLayoutData(data);
 		_allFigureButton.addSelectionListener(this);
 
 		_selectionFigureButton = new Button(figureGroup, SWT.RADIO);
 		_selectionFigureButton.setText(Messages.AbstractFigureSelectionDialog_3_xbut);
 		_selectionFigureButton.setSelection(singleSelection);
 		_selectionFigureButton.setEnabled(_selectionFigure != null);
 		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
 		_selectionFigureButton.setLayoutData(data);
 		_selectionFigureButton.addSelectionListener(this);
 
 		return figureGroup;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
 	 * .swt.events.SelectionEvent)
 	 */
 	public void widgetDefaultSelected(SelectionEvent e) {
 		widgetSelected(e);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
 	 * .events.SelectionEvent)
 	 */
 	public void widgetSelected(SelectionEvent e) {
 		if (_insideInternalModify) // avoid endless-loops
 			return;
 
 		if (e.getSource() == _allFigureButton || e.getSource() == _selectionFigureButton) {
 			_figure = _allFigureButton.getSelection() ? _allFigure : _selectionFigure;
 			_image = _allFigureButton.getSelection() ? _imageAll : _imageSelection;
 			updateControls();
 		}
 	}
 
 	public void updateControls() {
 		// not used
 	}
 
 	public final IFigure getFigure() {
 		return _figure;
 	}
 
 	public final GraphicalViewer getGraphicalViewer() {
 		return _graphicalViewer;
 	}
 
 	// determine _allFigure, _selectionFigure and _figure
 	private void determinePossibleFigures() {
 		_allFigure = null;
 		_selectionFigure = null;
 		_figure = null;
 
 		// shrink canvas to minimal necessary dimensions
 		GraphicalViewer viewer = getGraphicalViewer();
 		org.eclipse.swt.widgets.Control control = viewer.getControl();
 		if (control instanceof GFFigureCanvas) {
 			GFFigureCanvas canvas = (GFFigureCanvas) control;
 			canvas.regainSpace();
 		}
 
 		EditPart rootEditPart = viewer.getRootEditPart();
 		if (!(rootEditPart instanceof GraphicalEditPart))
 			return;
 
 		// determine _allFigure
 		GraphicalEditPart graphicalRootEditPart = (GraphicalEditPart) rootEditPart;
 		IFigure rootFigure = ((LayerManager) graphicalRootEditPart).getLayer(LayerConstants.PRINTABLE_LAYERS);
 		if (rootFigure == null)
 			return;
 
 		_allFigure = rootFigure;
 
 		// determine _selectionFigure
 		@SuppressWarnings("unchecked")
 		List<EditPart> selectedEditParts = viewer.getSelectedEditParts();
 		for (EditPart selectedEditPart : selectedEditParts) {
 			if (!(selectedEditPart instanceof GraphicalEditPart)
 					|| (selectedEditPart == getGraphicalViewer().getContents())) {
 				_selectionFigure = null;
 				break;
 			}
 			_selectedEditPart = (GraphicalEditPart) selectedEditPart;
 			_selectionFigure = _selectedEditPart.getFigure();
 			break;
 		}
 
 		// determine _figure
 		_figure = (_selectionFigure == null) ? _allFigure : _selectionFigure;
 		// _allFigureButton.setSelection(_figure == _allFigure);
 	}
 
 	public void setScaledImage(double scaleFactor, double upperBoundPixels) {
 		cleanUp();
 		_imageAll = null;
 		_scaledImage = null;
 		_image = null;
 
 		// create _imageAll based on _allFigure
 		initImageAll(upperBoundPixels);
 
 		// create _scaledImage based either on _allFigure or on _selectionFigure
 		// use scaleFactor to determine scaled version
 		initScaledImage(scaleFactor, upperBoundPixels);
 
 		initImage();
 	}
 
 	public void setScaledImage(double scaleFactor) {
 		setScaledImage(scaleFactor, 3000.0d);
 	}
 
 	private void initImage() {
 		{
 			if (_selectionFigure != null) {
 				_imageSelection = new Image(Display.getDefault(), _selectionFigure.getBounds().width,
 						_selectionFigure.getBounds().height);
 				GC gc = new GC(_imageSelection);
 				SWTGraphics graphics = new SWTGraphics(gc);
 
 				graphics.translate(-_selectionFigure.getBounds().x, -_selectionFigure.getBounds().y);
 
 				_selectionFigure.paint(graphics);
 				addRelatedEditParts(graphics, _selectedEditPart);
 
 				if (gc != null)
 					gc.dispose();
 				if (graphics != null)
 					graphics.dispose();
 				_image = _imageSelection;
 			} else {
 				if (_imageAll != null) {
 					_image = _imageAll;
 				} else {
 					_image = null;
 				}
 			}
 		}
 	}
 
 	private void initImageAll(double upperBoundPixels) {
 		{
 			int width = _allFigure.getBounds().width;
 			int height = _allFigure.getBounds().height;
 
 			// check whether the dimensions of the image to be created would
 			// be small enough to prevent runtime exceptions
 			if (width <= upperBoundPixels && height <= upperBoundPixels) {
 				_imageAll = new Image(Display.getDefault(), width, height);
 				GC gc = new GC(_imageAll);
 				SWTGraphics graphics = new SWTGraphics(gc);
 
 				/* move all figures into the positive region */
 				EditPart contents = getGraphicalViewer().getContents();
 				if (contents instanceof GraphicalEditPart) {
 					IFigure contentsFigure = ((GraphicalEditPart) contents).getFigure();
 					Rectangle contentBounds = contentsFigure.getBounds();
 					graphics.translate(-contentBounds.x, -contentBounds.y);
 				}
 
 				_allFigure.paint(graphics);
 
 				if (gc != null)
 					gc.dispose();
 				if (graphics != null)
 					graphics.dispose();
 			} else {
 				_imageAll = null;
 			}
 		}
 	}
 
 	private void initScaledImage(double scaleFactor, double upperBoundPixels) {
 		{
 			GC gc = null;
 			FixedScaledGraphics graphics = null;
 
 			if (!isFigureSelected()) {
 				// if the scale factor is too high, the operating system will
 				// not be able to provide a handle,
 				// because the Image would require too much space. "no more
 				// Handles"-Exception or "out of Memory" Error
 				// will be thrown
 				if (scaleFactor * _allFigure.getBounds().width > upperBoundPixels
 						|| scaleFactor * _allFigure.getBounds().height > upperBoundPixels) {
 					scaleFactor = Math.min(upperBoundPixels / _allFigure.getBounds().width, upperBoundPixels
 							/ _allFigure.getBounds().height);
 				}
 
 				_scaledImage = new Image(Display.getDefault(), (int) (_allFigure.getBounds().width * scaleFactor),
 						(int) (scaleFactor * _allFigure.getBounds().height));
 				gc = new GC(_scaledImage);
 				graphics = new FixedScaledGraphics(new SWTGraphics(gc));
 
 				graphics.scale(scaleFactor);
 
 				/* move all figures into the positive region */
 				EditPart contents = getGraphicalViewer().getContents();
 				if (contents instanceof GraphicalEditPart) {
 					IFigure contentsFigure = ((GraphicalEditPart) contents).getFigure();
 					Rectangle contentBounds = contentsFigure.getBounds();
 					graphics.translate(-contentBounds.x, -contentBounds.y);
 				}
 
 				_allFigure.paint(graphics);
 			} else {
 				if (scaleFactor * _selectionFigure.getBounds().width > upperBoundPixels
 						|| scaleFactor * _selectionFigure.getBounds().height > upperBoundPixels) {
 					scaleFactor = Math.min(upperBoundPixels / _selectionFigure.getBounds().width, upperBoundPixels
 							/ _selectionFigure.getBounds().height);
 				}
 
 				_scaledImage = new Image(null, (int) (_selectionFigure.getBounds().width * scaleFactor),
 						(int) (scaleFactor * _selectionFigure.getBounds().height));
 				gc = new GC(_scaledImage);
 				graphics = new FixedScaledGraphics(new SWTGraphics(gc));
 
 				graphics.scale(scaleFactor);
 				graphics.translate(-_selectionFigure.getBounds().x, -_selectionFigure.getBounds().y);
 
 				_selectionFigure.paint(graphics);
 				addRelatedEditParts(graphics, _selectedEditPart);
 
 			}
 
 			if (gc != null)
 				gc.dispose();
 			if (graphics != null)
 				graphics.dispose();
 		}
 	}
 
 	private void addRelatedEditParts(Graphics graphics, GraphicalEditPart ep) {
 		List<EditPart> relatedEditParts = GraphitiUiInternal.getGefService().getConnectionsContainedInEditPart(ep);
 		for (Object conn : relatedEditParts) {
 			if (conn instanceof GraphicalEditPart) {
 				GraphicalEditPart conn2 = (GraphicalEditPart) conn;
 				if (conn2.getModel() instanceof PictogramElement) {
 					PictogramElement pe = (PictogramElement) conn2.getModel();
 					if (!pe.isVisible())
 						continue;
 
 					IFigure figure = conn2.getFigure();
 					figure.paint(graphics);
 				}
 			}
 		}
 	}
 
 	public Image getImage() {
 		return _image;
 	}
 
 	public Image getScaledImage() {
 		return _scaledImage;
 	}
 
 	private boolean isFigureSelected() {
 		return _selectedEditPart != null && (_selectionFigureButton == null || _selectionFigureButton.getSelection());
 	}
 
 	public void cleanUp() {
 		if (_scaledImage != null)
 			_scaledImage.dispose();
 		if (_image != null)
 			_image.dispose();
 		if (_imageAll != null)
 			_imageAll.dispose();
 		if (_imageSelection != null)
 			_imageSelection.dispose();
 	}
 }
