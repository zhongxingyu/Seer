 /*
  * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
  * This cross-platform GIS is developed at French IRSTV institute and is able
  * to manipulate and create vector and raster spatial information. OrbisGIS
  * is distributed under GPL 3 license. It is produced  by the geo-informatic team of
  * the IRSTV Institute <http://www.irstv.cnrs.fr/>, CNRS FR 2488:
  *    Erwan BOCHER, scientific researcher,
  *    Thomas LEDUC, scientific researcher,
  *    Fernando GONZALEZ CORTES, computer engineer.
  *
  * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
  *
  * This file is part of OrbisGIS.
  *
  * OrbisGIS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OrbisGIS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult:
  *    <http://orbisgis.cerma.archi.fr/>
  *    <http://sourcesup.cru.fr/projects/orbisgis/>
  *
  * or contact directly:
  *    erwan.bocher _at_ ec-nantes.fr
  *    fergonco _at_ gmail.com
  *    thomas.leduc _at_ cerma.archi.fr
  */
 package org.orbisgis.views.editor;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetAdapter;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import javax.swing.BorderFactory;
 import javax.swing.JOptionPane;
 
 import net.infonode.docking.DockingWindow;
 import net.infonode.docking.OperationAbortedException;
 import net.infonode.docking.RootWindow;
 import net.infonode.docking.View;
 import net.infonode.gui.panel.SimplePanel;
 
 import org.apache.log4j.Logger;
 import org.orbisgis.Services;
 import org.orbisgis.editor.EditorDecorator;
 import org.orbisgis.editor.IEditor;
 import org.orbisgis.errorManager.ErrorManager;
 import org.orbisgis.geocognition.Geocognition;
 import org.orbisgis.geocognition.GeocognitionElement;
 import org.orbisgis.geocognition.GeocognitionElementListener;
 import org.orbisgis.geocognition.GeocognitionListener;
 import org.orbisgis.geocognition.mapContext.GeocognitionException;
 import org.orbisgis.pluginManager.background.BackgroundManager;
 import org.orbisgis.progress.NullProgressMonitor;
 import org.orbisgis.views.geocognition.TransferableGeocognitionElement;
 import org.orbisgis.views.geocognition.actions.OpenGeocognitionElementJob;
 
 public class EditorPanel extends Container {
 
 	private static final Logger logger = Logger.getLogger(EditorPanel.class);
 	private RootWindow root;
 	private ArrayList<EditorInfo> editorsInfo = new ArrayList<EditorInfo>();
 	private EditorDecorator lastEditor = null;
 	private EditorView editorView;
 	private ChangeNameListener changeNameListener = new ChangeNameListener();
 	private DocumentRemovalListener removalListener;
 
 	public EditorPanel(EditorView editorView) {
 		this.setLayout(new BorderLayout());
 		root = new RootWindow(null);
 		root.getRootWindowProperties().getSplitWindowProperties()
 				.setContinuousLayoutEnabled(false);
 		root.getRootWindowProperties().getTabWindowProperties()
 				.getCloseButtonProperties().setVisible(false);
 		root.getRootWindowProperties().getTabWindowProperties()
 				.getUndockButtonProperties().setVisible(false);
 		root.getRootWindowProperties().getTabWindowProperties()
 				.getRestoreButtonProperties().setVisible(false);
 		root.getRootWindowProperties().getTabWindowProperties()
 				.getMinimizeButtonProperties().setVisible(false);
 		root.getRootWindowProperties().getTabWindowProperties()
 				.getMaximizeButtonProperties().setVisible(false);
 		root.getRootWindowProperties().getWindowAreaProperties().setBorder(
 				BorderFactory.createEmptyBorder());
 		root.getRootWindowProperties().getTabWindowProperties()
 				.getTabProperties().getFocusedProperties()
 				.getComponentProperties().setBackgroundColor(
 						new Color(100, 140, 190));
 		root.getRootWindowProperties().getWindowAreaProperties()
 				.setBackgroundColor(new Color(238, 238, 238));
 
 		this.add(root, BorderLayout.CENTER);
 
 		this.editorView = editorView;
 
 		this.addComponentListener(new ComponentAdapter() {
 
 			@Override
 			public void componentShown(ComponentEvent e) {
 				if ((editorsInfo.size() > 0) && (lastEditor == null)) {
 					EditorInfo ei = editorsInfo.get(0);
 					ei.getView().requestFocus();
 					ei.getView().makeVisible();
 				}
 			}
 
 		});
 
 		this.setDropTarget(new DropTarget(this, new DropTargetAdapter() {
 
 			@Override
 			public void drop(DropTargetDropEvent dtde) {
 				BackgroundManager backgroundManager = (BackgroundManager) Services
 						.getService("org.orbisgis.BackgroundManager");
 				Transferable trans = dtde.getTransferable();
 				if (trans
 						.isDataFlavorSupported(TransferableGeocognitionElement.geocognitionFlavor)) {
 					try {
 						GeocognitionElement[] elements = (GeocognitionElement[]) trans
 								.getTransferData(TransferableGeocognitionElement.geocognitionFlavor);
 						backgroundManager
 								.backgroundOperation(new OpenGeocognitionElementJob(
 										elements));
 					} catch (UnsupportedFlavorException e) {
 						Services.getErrorManager().error(
 								"Cannot open this type of element", e);
 					} catch (IOException e) {
 						Services.getErrorManager().error("Cannot open element",
 								e);
 					}
 				}
 			}
 
 		}));
 	}
 
 	public GeocognitionElement getCurrentDocument() {
 		if (lastEditor != null) {
 			return lastEditor.getElement();
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * @param doc
 	 * @param editorClass
 	 * @return True if the specified document is currently being edited by an
 	 *         editor of the specified class
 	 */
 	public boolean isBeingEdited(GeocognitionElement doc,
 			Class<? extends IEditor> editorClass) {
 		return findViewWithEditor(root, doc, editorClass) != null;
 	}
 
 	/**
 	 * Makes the editor visible
 	 * 
 	 * @param element
 	 * @param editorClass
 	 */
 	public void showEditor(GeocognitionElement document,
 			Class<? extends IEditor> editorClass) {
 		View existingView = findViewWithEditor(root, document, editorClass);
 		if (existingView != null) {
 			existingView.makeVisible();
 			existingView.requestFocus();
 		}
 	}
 
 	public EditorInfo getEditorByComponent(Component component) {
 		for (EditorInfo editorInfo : editorsInfo) {
 			if (editorInfo.getEditorComponent() == component) {
 				return editorInfo;
 			}
 		}
 
 		return null;
 	}
 
 	private EditorInfo[] getEditorsByDocument(GeocognitionElement document) {
 		ArrayList<EditorInfo> ret = new ArrayList<EditorInfo>();
 		for (EditorInfo editorInfo : editorsInfo) {
 			if (editorInfo.getDocument() == document) {
 				ret.add(editorInfo);
 			}
 		}
 
 		return ret.toArray(new EditorInfo[0]);
 	}
 
 	/**
 	 * Adds and shows a new editor
 	 * 
 	 * @param editor
 	 */
 	public void addEditor(EditorDecorator editor) {
 		Component comp = editor.getComponent();
 		View view = new View(editor.getTitle(), editor.getIcon(), comp);
 		view.addListener(new ClosingListener());
 
 		DockingWindowUtil.addNewView(root, view);
 		view.requestFocus();
 		editorsInfo
 				.add(new EditorInfo(view, editor.getElement(), editor, comp));
 
 		editor.getElement().addElementListener(changeNameListener);
 
 		if (removalListener == null) {
 			removalListener = new DocumentRemovalListener();
 			Geocognition gc = Services.getService(Geocognition.class);
 			gc.addGeocognitionListener(removalListener);
 		}
 	}
 
 	private View findViewWithEditor(DockingWindow wnd, GeocognitionElement doc,
 			Class<? extends IEditor> editorClass) {
 		for (int i = 0; i < wnd.getChildWindowCount(); i++) {
 			DockingWindow child = wnd.getChildWindow(i);
 			if (child instanceof View) {
 				Component comp = ((View) child).getComponent();
 				EditorDecorator existingEditor = getEditorByComponent(comp)
 						.getEditorDecorator();
 				if ((existingEditor.getElement() == doc)
 						&& (existingEditor.getEditor().getClass() == editorClass)) {
 					return (View) child;
 				}
 			} else {
 				View ret = findViewWithEditor(child, doc, editorClass);
 				if (ret != null) {
 					return ret;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public EditorDecorator getCurrentEditor() {
 		return lastEditor;
 	}
 
 	public void saveAllDocuments() {
 		HashSet<GeocognitionElement> done = new HashSet<GeocognitionElement>();
 		Iterator<EditorInfo> it = editorsInfo.iterator();
 		while (it.hasNext()) {
 			EditorInfo editorInfo = it.next();
 			GeocognitionElement document = editorInfo.getDocument();
 			if (!done.contains(document)) {
 				try {
 					document.save();
 				} catch (GeocognitionException e) {
 					Services.getService(ErrorManager.class).error(
 							"Problem saving", e);
 				}
 				done.add(document);
 			}
 		}
 	}
 
 	private final class ClosingListener extends DockingWindowAdapter {
 		private View nextFocus;
 
 		@Override
 		public void windowClosing(DockingWindow arg0)
 				throws OperationAbortedException {
 			if (arg0 instanceof View) {
 				View closedView = (View) arg0;
 				EditorInfo editorInfo = getEditorByComponent(closedView
 						.getComponent());
 				try {
 					if (editorInfo.element.isModified()) {
 						int res = JOptionPane.showConfirmDialog(
 								EditorPanel.this, "There are unsaved "
 										+ "changes in "
 										+ editorInfo.element.getId()
 										+ ", save them before closing?",
 								"Close editor",
 								JOptionPane.YES_NO_CANCEL_OPTION);
 						if (res == JOptionPane.CANCEL_OPTION) {
 							throw new OperationAbortedException();
 						} else if (res == JOptionPane.YES_OPTION) {
 							editorInfo.element.save();
 						}
 					}
 				} catch (OperationAbortedException e) {
 					throw e;
 				} catch (Exception e) {
 					logger.error("Problem closing editor", e);
 				}
 			}
 
 			// Focus another view
 			DockingWindow parent = arg0.getWindowParent();
 			HashSet<DockingWindow> visited = new HashSet<DockingWindow>();
 			visited.add(arg0);
 			nextFocus = getNextFocus(parent, visited);
 		}
 
 		public void windowClosed(DockingWindow arg0) {
 			if (arg0 instanceof View) {
 				View closedView = (View) arg0;
 				EditorInfo editorInfo = getEditorByComponent(closedView
 						.getComponent());
 				editorsInfo.remove(editorInfo);
 				IEditor closedEditor = editorInfo.getEditorDecorator();
 
 				// Remove document listener
 				closedEditor.getElement().removeElementListener(
 						changeNameListener);
 
 				// Focus next editor
 				if (nextFocus != null) {
 					nextFocus.requestFocus();
 					nextFocus.requestFocusInWindow();
 					root.restoreFocus();
 					lastEditor = null;
 				} else {
 					lastEditor = null;
 					editorView.fireActiveEditorChanged(lastEditor, null);
 				}
 
 				editorView.fireEditorClosed(closedEditor, editorInfo
 						.getEditorDecorator().getId());
 				freeView(closedView, editorInfo.editorDecorator);
 
 				editorInfo.element.close(new NullProgressMonitor());
 			}
 		}
 
 		private void freeView(View v1, EditorDecorator editorDecorator) {
 			root.removeView(v1);
 			Component panel = v1.getComponent();
 			SimplePanel simplePanel = (SimplePanel) panel.getParent();
 			if (simplePanel != null) {
 				simplePanel.remove(panel); // here we can call removeAll()
 				simplePanel.setComponent(null); // very important
 			}
 
 			editorDecorator.delete();
 		}
 
 		private View getNextFocus(DockingWindow parent,
 				HashSet<DockingWindow> visited) {
 			// Find a view at the same level
 			for (int i = 0; i < parent.getChildWindowCount(); i++) {
 				DockingWindow child = parent.getChildWindow(i);
 				if (visited.contains(child)) {
 					continue;
 				} else {
 					visited.add(child);
 					// If it's a view return it
 					if (child instanceof View) {
 						return (View) child;
 					} else {
 						// Otherwise go deeper
 						View ret = getNextFocus(child, visited);
 						if (ret != null) {
 							return ret;
 						}
 					}
 				}
 			}
 
 			// Search in the upper level
 			if ((parent.getWindowParent() != null)
 					&& (parent.getWindowParent() != parent)) {
 				return getNextFocus(parent.getWindowParent(), visited);
 			} else {
 				return null;
 			}
 		}
 
 		@Override
 		public void viewFocusChanged(View arg0, View arg1) {
 			View focusedView = root.getFocusedView();
 			if (focusedView != null) {
 				EditorDecorator nextEditor = getEditorByComponent(
 						focusedView.getComponent()).getEditorDecorator();
 				if (nextEditor != lastEditor) {
 					IEditor previous = null;
 					if (lastEditor != null) {
 						previous = lastEditor.getEditor();
 					}
 					lastEditor = nextEditor;
 					editorView.fireActiveEditorChanged(previous, lastEditor
 							.getEditor());
 				}
 			}
 		}
 	}
 
 	private class ChangeNameListener implements GeocognitionElementListener {
 
 		@Override
 		public void idChanged(GeocognitionElement element) {
 			setTitle(element);
 		}
 
 		private void setTitle(GeocognitionElement element) {
 			EditorInfo[] infos = getEditorsByDocument(element);
 			for (EditorInfo editorInfo : infos) {
 				View view = editorInfo.getView();
 				EditorDecorator editor = editorInfo.getEditorDecorator();
 				String title = editor.getTitle();
 				if (editor.getElement().isModified()) {
 					title = "*" + title;
 				}
 				view.getViewProperties().setTitle(title);
 			}
 		}
 
 		@Override
 		public void contentChanged(GeocognitionElement element) {
 			setTitle(element);
 		}
 
 		@Override
 		public void saved(GeocognitionElement element) {
 			setTitle(element);
 		}
 
 	}
 
 	private class DocumentRemovalListener implements GeocognitionListener {
 
 		@Override
 		public void elementAdded(Geocognition geocognition,
 				GeocognitionElement parent, GeocognitionElement newElement) {
 
 		}
 
 		@Override
 		public void elementRemoved(Geocognition geocognition,
 				GeocognitionElement element) {
 
 		}
 
 		@Override
 		public boolean elementRemoving(Geocognition geocognition,
 				GeocognitionElement element) {
 			EditorInfo[] infos = getEditorsByDocument(element);
 			for (EditorInfo editorInfo : infos) {
 				View view = editorInfo.getView();
 				return closeEditorView(view);
 			}
 
 			return true;
 		}
 
 		@Override
 		public void elementMoved(Geocognition geocognition,
 				GeocognitionElement element, GeocognitionElement oldParent) {
 		}
 	}
 
 	private boolean closeEditorView(View view) {
 		try {
 			view.closeWithAbort();
 			return true;
 		} catch (OperationAbortedException e) {
 			return false;
 		} catch (Exception e) {
 			Services.getErrorManager().error("Cannot close editor", e);
 			return false;
 		}
 	}
 
 	private class EditorInfo {
 		private View view;
 		private GeocognitionElement element;
 		private EditorDecorator editorDecorator;
 		private Component editorComponent;
 
 		public EditorInfo(View view, GeocognitionElement element,
 				EditorDecorator editorDecorator, Component editorComponent) {
 			super();
 			this.view = view;
 			this.element = element;
 			this.editorDecorator = editorDecorator;
 			this.editorComponent = editorComponent;
 		}
 
 		public View getView() {
 			return view;
 		}
 
 		public GeocognitionElement getDocument() {
 			return element;
 		}
 
 		public EditorDecorator getEditorDecorator() {
 			return editorDecorator;
 		}
 
 		public Component getEditorComponent() {
 			return editorComponent;
 		}
 	}
 
 	public boolean closeEditor(IEditor editor) {
 		for (EditorInfo editorInfo : editorsInfo) {
 			if (editor == editorInfo.getEditorDecorator()) {
 				return closeEditorView(editorInfo.getView());
 			}
 		}
 
 		throw new IllegalArgumentException("The editor does not exist");
 	}
 
 	public IEditor[] getEditors() {
 		IEditor[] ret = new IEditor[editorsInfo.size()];
 		for (int i = 0; i < editorsInfo.size(); i++) {
 			ret[i] = editorsInfo.get(i).getEditorDecorator();
 		}
 
 		return ret;
 	}
 
 }
