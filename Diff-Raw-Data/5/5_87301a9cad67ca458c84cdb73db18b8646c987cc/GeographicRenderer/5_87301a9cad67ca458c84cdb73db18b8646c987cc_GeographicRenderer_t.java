 // GeographicRenderer.java
 package org.eclipse.stem.ui.views.geographic;
 
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
 
 import java.awt.BasicStroke;
 import java.awt.Stroke;
 import java.text.ParseException;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.swing.text.NumberFormatter;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.stem.ui.adapters.color.ColorProviderAdapter;
 import org.eclipse.stem.ui.views.geographic.map.Messages;
 import org.eclipse.stem.ui.views.geographic.map.StemPolygonsList;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 
 /**
  * This class is extended by classes that render geographic (lat/long) data into
  * a visual representation.
  */
 public abstract class GeographicRenderer extends Composite implements
 		ISelectionProvider {
 	protected static final float INITIAL_GAIN_FACTOR = 1.0f;
 	protected static final boolean INITIAL_DRAW_POLYGON_BORDERS = true;
 	protected static final boolean INITIAL_USE_LOG_SCALING = true;
 	protected static final int MARGIN_HEIGHT = 5;
 	protected static final int MARGIN_WIDTH = 5;
 
 	private static NumberFormatter formatter = new NumberFormatter();
 
 	protected boolean drawPolygonBorders = INITIAL_DRAW_POLYGON_BORDERS;
 	protected Stroke polygonStroke = new BasicStroke(1f);
 	protected boolean useLogScaling = INITIAL_USE_LOG_SCALING;
 	protected float gainFactor = INITIAL_GAIN_FACTOR;
 
 	private GainFactorAction defaultGainFactorAction;
 	GainFactorAction lastGainFactorAction;
 	private LogarithmicAction logarithmicAction;
 	private DrawPolygonBordersAction drawPolygonBordersAction;
 	
 	protected ColorProviderAdapter colorProviderAdapter = null;
 
 	/**
 	 * The collection of ISelectionChangedListener waiting to be told about
 	 * selections.
 	 */
 	protected final List<ISelectionChangedListener> listeners = new CopyOnWriteArrayList<ISelectionChangedListener>();
 
 	private ISelection selection;
 
 	/**
 	 * @param parent
 	 * @param style
 	 */
 	public GeographicRenderer(final Composite parent, final int style) {
 		super(parent, style);
 		this.addDisposeListener(new DisposeListener() {
 			/**
 			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
 			 */
 			public void widgetDisposed(
 					@SuppressWarnings("unused") final DisposeEvent e) {
 				if (!isDisposed()) {
 					dispose();
 				}
 			}
 		});
 	} // GeographicRenderer
 
 	/**
 	 * @param polygonsToRender
 	 */
 	abstract public void render(StemPolygonsList polygonsToRender);
 
 	protected java.awt.Color getColorForRelativeValue(
 			@SuppressWarnings("unused")
 			final double relativeValue) {
 		return null;
 	}
 
 	/**
 	 * @see org.eclipse.swt.widgets.Widget#dispose()
 	 */
 	@Override
 	public void dispose() {
 		//Nothing to do
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void addSelectionChangedListener(
 			final ISelectionChangedListener listener) {
 		listeners.add(listener);
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void removeSelectionChangedListener(
 			final ISelectionChangedListener listener) {
 		listeners.remove(listener);
 	}
 
 	/**
 	 * @return the selection
 	 */
 	public final ISelection getSelection() {
 		return selection;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setSelection(final ISelection selection) {
 		this.selection = selection;
 		fireSelection(selection);
 	}
 
 	/**
 	 * Pass a {@link SelectionChangedEvent} along to listeners.
 	 * 
 	 * @param selection
 	 *            the {@link ISelection} to be giving to the listeners.
 	 */
 	public void fireSelection(final ISelection selection) {
 
 		final SelectionChangedEvent event = new SelectionChangedEvent(this,
 				selection);
 		for (final ISelectionChangedListener listener : listeners) {
 			listener.selectionChanged(event);
 		} // for each ISelectionChangedListener
 	} // fireSelection
 
 	/**
 	 * Reset the renderer.
 	 */
 	public void reset() {
 		// Reset the gain factor and update the checks in the context menu
 		lastGainFactorAction.setChecked(false);
 		defaultGainFactorAction.run();
 		defaultGainFactorAction.setChecked(true);
 
 		useLogScaling = INITIAL_USE_LOG_SCALING;
 		logarithmicAction.setChecked(useLogScaling);
 
 		drawPolygonBorders = INITIAL_DRAW_POLYGON_BORDERS;
 		drawPolygonBordersAction.setChecked(drawPolygonBorders);
 
 		// drawPolygonCenterConnections =
 		// DEFAULT_DRAW_POLYGON_CENTER_CONNECTIONS;
 		// connectPolygonCentersAction.setChecked(drawPolygonCenterConnections);
 	} // reset
 
 	protected void setGainFactor(final float gainFactor) {
 		this.gainFactor = gainFactor;
 	} // setGainFactor
 
 	/**
 	 * Switch the option of drawing the borders of polygons on the map.
 	 */
 	protected void toggleDrawPolygonBordersChoice() {
 		drawPolygonBorders = !drawPolygonBorders;
 		redraw();
 	} // toggleDrawPolygonBordersChoice
 
 	/**
 	 * Switch the option of scaling the display values logrithmically.
 	 */
 	protected void toggleUseLogScaling() {
 		useLogScaling = !useLogScaling;
 		redraw();
 	} // toggleUseLogScaling
 
 	/**
 	 * Create the view's context menu and add the action handlers to it.
 	 * @return the MenuManager
 	 */
 	public MenuManager createContextMenuManager() {
 
 		// Context Menu
 		final MenuManager contextMenuManager = new org.eclipse.jface.action.MenuManager();
 
 		contextMenuManager.add(createGainFactorMenu());
 
 		logarithmicAction = new LogarithmicAction();
 		contextMenuManager.add(logarithmicAction);
 
 		contextMenuManager.add(new Separator());
 		drawPolygonBordersAction = new DrawPolygonBordersAction();
 		contextMenuManager.add(drawPolygonBordersAction);
 
 		// connectPolygonCentersAction = new ConnectPolygonCentersAction();
 		// contextMenuManager.add(connectPolygonCentersAction);
 
 		contextMenuManager.add(new Separator());

// Defect 302663
//		contextMenuManager.add(createReportsSelectionMenu());
 		
 		contextMenuManager.add(new Separator());
 
 		// ---------------------------------------------------------------------
 
 		contextMenuManager.add(new ResetMapCanvasAction());
 
 		// Place Holder for Menu Additions
 		contextMenuManager.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 
 		// ---------------------------------------------------------------------
 
 		return contextMenuManager;
 		
 	} // createContextMenu
 
 	/**
 	 * @return
 	 */
 	private MenuManager createGainFactorMenu() {
 		final MenuManager gainFactorMenu = new MenuManager(Messages
 				.getString("MapMenu.GainFactor"));
 		gainFactorMenu.add(new GainFactorAction(0.001));
 		gainFactorMenu.add(new GainFactorAction(0.01));
 		gainFactorMenu.add(new GainFactorAction(0.1));
 
 		defaultGainFactorAction = new GainFactorAction(1.0); // Default
 		lastGainFactorAction = defaultGainFactorAction;
 		gainFactorMenu.add(defaultGainFactorAction);
 
 		gainFactorMenu.add(new GainFactorAction(10.0));
 		gainFactorMenu.add(new GainFactorAction(100.0));
 		gainFactorMenu.add(new GainFactorAction(1000.0));
 		gainFactorMenu.add(new GainFactorAction(10000.0));
 		gainFactorMenu.add(new GainFactorAction(100000.0));
 		gainFactorMenu.add(new GainFactorAction(1000000.0));
 		gainFactorMenu.add(new GainFactorAction(10000000.0));
 		return gainFactorMenu;
 	}
 	
 	/**
 	 * @return
 	 */
 	private MenuManager createReportsSelectionMenu() {
 		final MenuManager reportsSelectionMenu = new MenuManager(Messages
 				.getString("MapMenu.Reports_Select"), "reports");
 		reportsSelectionMenu.add(new Separator(
 				IWorkbenchActionConstants.MB_ADDITIONS));
 		return reportsSelectionMenu;
 	} // createReportsSelectionMenu
 
 	protected class GainFactorAction extends Action {
 
 		private final double factor;
 
 		GainFactorAction(final double factor) {
 			super(GeographicRenderer.this.getText(factor),
 					IAction.AS_RADIO_BUTTON);
 			this.factor = factor;
 			setChecked(factor == INITIAL_GAIN_FACTOR);
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return GeographicRenderer.this.getText(factor);
 		} // getText
 
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			setGainFactor((float)factor);
 			lastGainFactorAction = this;
 			Composite parent = GeographicRenderer.this.getParent();
 			((GeographicControl)parent).refresh(); 
 		}
 
 	} // GainFactorAction
 
 	String getText(final double factor) {
 		String retValue = "";
 		try {
 			retValue = formatter.valueToString(new Double(factor));
 		} catch (final ParseException e) {
 			Activator.logError("Problem parsing gain factor value \"" + factor
 					+ "\"", e);
 		}
 		return retValue;
 	} // getText
 
 	protected class LogarithmicAction extends Action {
 
 		/**
 		 * Default Constructor
 		 */
 		public LogarithmicAction() {
 			super(Messages.getString("MapMenu.Logrithmic_Scaling"),
 					IAction.AS_CHECK_BOX);
 			setChecked(useLogScaling);
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return Messages.getString("MapMenu.Logrithmic_Scaling");
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			toggleUseLogScaling();
 			Composite parent = GeographicRenderer.this.getParent();
 			((GeographicControl)parent).refresh(); 
 		}
 	} // LogarighmicAction
 
 	protected class DrawPolygonBordersAction extends Action {
 
 		/**
 		 * Default Constructor
 		 */
 		public DrawPolygonBordersAction() {
 			super(Messages.getString("MapMenu.Polygon_Borders"),
 					IAction.AS_CHECK_BOX);
 			// Checked in the menu?
 			if (drawPolygonBorders) {
 				// Yes
 				setChecked(true);
 			} // if
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return Messages.getString("MapMenu.Polygon_Borders");
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			toggleDrawPolygonBordersChoice();
 			Composite parent = GeographicRenderer.this.getParent();
 			((GeographicControl)parent).refresh(); 
 		}
 	} // DrawPolygonBordersAction
 
 	// protected class ConnectPolygonCentersAction extends Action {
 	//
 	// /**
 	// * Default Constructor
 	// */
 	// public ConnectPolygonCentersAction() {
 	// super(Messages.getString("MapMenu.Polygon_Centers"),
 	// IAction.AS_CHECK_BOX);
 	// setEnabled(false);
 	// setChecked(drawPolygonCenterConnections);
 	// }
 	//
 	// /**
 	// * @see org.eclipse.jface.action.Action#getText()
 	// */
 	// @Override
 	// public String getText() {
 	// return Messages.getString("MapMenu.Polygon_Centers");
 	// }
 	//
 	// /**
 	// * @see org.eclipse.jface.action.Action#run()
 	// */
 	// @Override
 	// public void run() {
 	// toggleDrawPolygonsCentersConnections();
 	// }
 	// } // ConnectPolygonCentersAction
 
 	protected class ResetMapCanvasAction extends Action {
 
 		/**
 		 * @see org.eclipse.jface.action.Action#getText()
 		 */
 		@Override
 		public String getText() {
 			return Messages.getString("MapMenu.Reset");
 		}
 
 		/**
 		 * @see org.eclipse.jface.action.Action#run()
 		 */
 		@Override
 		public void run() {
 			reset();
 		}
 	} // ResetMapCanvasAction
 
 	/**
 	 * @param colorProvider the colorProvider to set
 	 */
 	public void setColorProviderAdapter(ColorProviderAdapter colorProvider) {
 		this.colorProviderAdapter = colorProvider;
 	}
 } // GeographicRenderer
