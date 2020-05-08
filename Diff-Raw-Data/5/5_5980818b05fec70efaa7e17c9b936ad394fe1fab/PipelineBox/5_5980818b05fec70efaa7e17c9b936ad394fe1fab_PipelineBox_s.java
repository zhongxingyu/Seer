 /*OsmUi is a user interface for Osmosis
     Copyright (C) 2011  Verena Käfer, Peter Vollmer, Niklas Schnelle
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or 
     any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.osmui.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.AbstractAction;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import com.mxgraph.model.mxCell;
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.view.mxGraph;
 
 import de.osmui.model.osm.TTask;
 import de.osmui.model.pipelinemodel.AbstractTask;
 import de.osmui.model.pipelinemodel.JGPipelineModel;
 import de.osmui.ui.events.TaskSelectedEvent;
 import de.osmui.ui.events.TaskSelectedEventListener;
 import de.osmui.util.TaskManager;
 import de.osmui.util.exceptions.TaskNameUnknownException;
 
 /**
  * @author Niklas Schnelle, Peter Vollmer, Verena Käfer
  * 
  *         will be tested by system-tests
  * 
  */
 
 public class PipelineBox extends mxGraphComponent implements Observer,
 		MouseListener, MouseWheelListener, TaskSelectedEventListener {
 
 	private static final long serialVersionUID = -2865210986243818496L;
 
 	private final ArrayList<TaskSelectedEventListener> selectedListeners;
 	private AbstractTask selectedTask;
 	private JPopupMenu popupMenu;
 	private ActionListener popupActionListener;
 	private double zoomPos;
 
 	public PipelineBox(mxGraph graph) {
 		super(graph);
 		this.selectedListeners = new ArrayList<TaskSelectedEventListener>();
 		this.zoomPos = 1.0;
 
 		this.graph.setAllowDanglingEdges(false);
 		this.graph.setAllowLoops(false);
 		this.graph.setAutoSizeCells(true);
 		this.graph.setCellsBendable(false);
 		this.graph.setCellsMovable(true);
 		this.graph.setCellsResizable(false);
 		this.graph.setEdgeLabelsMovable(false);
 		this.graph.setDropEnabled(false);
 
 		this.getGraphControl().addMouseListener(this);
 
 		this.setAutoExtend(true);
 		this.setAntiAlias(true);
 		this.setAutoScroll(true);
 		this.setAutoscrolls(true);
 		this.setFoldingEnabled(false);
 		this.setDoubleBuffered(true);
 		this.setImportEnabled(false);
 		this.setExportEnabled(false);
 		// Register ourselves as listener
 		registerTaskSelectedListener(this);
 		addMouseWheelListener(this);
 
 		this.popupMenu = new JPopupMenu();
 		popupActionListener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				JMenuItem item;
 				if (event.getSource() instanceof JMenuItem) {
 					item = (JMenuItem) event.getSource();
 					AbstractTask newTask;
 					try {
 						newTask = TaskManager.getInstance().createTask(
 								item.getText());
 						MainFrame.getInstance().getTaskBox()
 								.addTaskToModel(newTask);
 					} catch (TaskNameUnknownException e) {
 						// Do nothing
 					}
 
 				}
 			}
 
 		};
 
 		// Register Keyboard Actions
 		this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
 				KeyStroke.getKeyStroke("DELETE"), "deleteCell");
 
 		this.getActionMap().put("deleteCell", new AbstractAction() {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				final mxGraph graph = getGraph();
 				// Removes the selected Cells
 				graph.removeCells();
 
 			}
 		});
 
 	}
 
 
 	public void registerTaskSelectedListener(TaskSelectedEventListener l) {
 		selectedListeners.add(l);
 	}
 
 	public void removeTaskSelectedListener(TaskSelectedEventListener l) {
 		selectedListeners.remove(l);
 	}
 
 	public void fireTaskSelected(TaskSelectedEvent e) {
 		for (TaskSelectedEventListener listener : selectedListeners) {
 			listener.TaskSelected(e);
 		}
 	}
 
 	/**
 	 * This is from the Observer interface we react to model changes here the
 	 * model notifies it's observers with AbstractTask objects when they are
 	 * added
 	 */
 	@Override
 	public void update(Observable arg0, Object arg1) {
 
 		if (arg1 instanceof AbstractTask) {
 			AbstractTask task = (AbstractTask) arg1;
 			// If the model is null the task was removed
 			if (task.getModel() != null && !task.equals(selectedTask)) {
 				this.graph.setSelectionCell(((JGPipelineModel) arg0)
 						.getCellForTask(task));
 				fireTaskSelected(new TaskSelectedEvent(this,
 						(AbstractTask) task));
 				selectedTask = task;
 			} else if (task.getModel() == null) {
 				selectedTask = null;
 				fireTaskSelected(new TaskSelectedEvent(this,
 						(AbstractTask) null));
 			}
 		}
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		//checkEvent(arg0);
 	}
 
 	// Need to specify the following methods but don't care
 	// for the events so do nothing
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 	}
 
 	private void checkEvent(MouseEvent event) {
 		// According to java swing doku need to do this in mousePressed
 		// and mouseReleased
 		if (event.isPopupTrigger()) {
 			popupMenu.show(event.getComponent(), event.getX(), event.getY());
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		
 		mxCell cell = (mxCell) getCellAt(arg0.getX(), arg0.getY());
 		if ( cell == null) {
 			fireTaskSelected(new TaskSelectedEvent(this, (AbstractTask) null));
 		} else if (cell.isVertex()){
 			fireTaskSelected(new TaskSelectedEvent(this, (AbstractTask) cell.getValue()));
 		}
 		checkEvent(arg0);		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		checkEvent(arg0);
 	}
 
 	@Override
 	public void TaskSelected(TaskSelectedEvent e) {
 		String taskName = (e.getTask() != null && e.getTask().isConnectable()) ? e
 				.getTask().getName() : "";
 		List<TTask> desc = TaskManager.getInstance().getCompatibleTasks(
 				taskName);
 		popupMenu.removeAll();
 		for (TTask currTask : desc) {
 			popupMenu.add(currTask.getName()).addActionListener(
 					popupActionListener);
 		}
 
 	}
 
 	/**
 	 * Wee need to add saving the zoom position to the super class
 	 */
 	@Override
 	public void zoomIn() {
 		super.zoomIn();
 		zoomPos *= zoomFactor;
 	}
 
 	/**
 	 * Wee need to add saving the zoom position to the super class
 	 */
 	@Override
 	public void zoomOut() {
 		// JGprahx bug fails zooming in after having zoomed out too much, don't
 		// allow this
		if (zoomPos > 0.4) {
 			super.zoomOut();
 			zoomPos /= zoomFactor;
 		}
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent evt) {
 		if (evt.isControlDown()
 				&& evt.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
 			int scrolled = evt.getWheelRotation();
 			if (scrolled < 0) {
 				for (int s = 0; s > scrolled; --s) {
 					this.zoomIn();
 
 				}
 
			} else if (zoomPos > 0.3) {
 				for (int s = 0; s < scrolled; ++s) {
 					this.zoomOut();
 				}
 			}
 
 		}
 	}
 
 }
