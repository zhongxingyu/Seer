 /**
  * 
  */
 package de.osmui.model.pipelinemodel;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.SwingConstants;
 
 import de.osmui.model.exceptions.TasksNotCompatibleException;
 import de.osmui.model.exceptions.TasksNotInModelException;
 
 import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
 import com.mxgraph.model.mxCell;
 import com.mxgraph.view.mxGraph;
 
 /**
  * This class implements a pipeline model using an mxGraph as a backing store
  * 
  * @author Niklas Schnelle
  * 
  */
 public class JGPipelineModel extends AbstractPipelineModel implements
 		Serializable {
 
 	private static final long serialVersionUID = -4609328085880199933L;
 
 	protected ArrayList<JGTaskDecorator> tasks;
 	protected transient mxGraph graph;
 	protected transient mxHierarchicalLayout lay;
 
 	public JGPipelineModel() {
 		tasks = new ArrayList<JGTaskDecorator>();
 		graph = new mxGraph() {
 			// Overrides method to disallow editting
 			@Override
 			public boolean isCellEditable(Object cell) {
 				return false;
 			}
 
 			@Override
 			public boolean isCellConnectable(Object cell) {
 				mxCell mxcell = (mxCell) cell;
 				if (mxcell.isVertex()) {
 					AbstractTask task = (AbstractTask) mxcell.getValue();
 					return task.isConnectable();
 				} else {
 					return false;
 				}
 			}
 
 			@Override
 			public Object addCell(Object cell, Object parent, Integer index,
 					Object source, Object target) {
 				Object ret;
 				ret = super.addCell(cell, parent, index, source, target);
 
 				if (source != null && target != null && cell != null) {
 					// Check the cell, which should be an edge to find out
 					// whether the
 					// tasks are already connected, then it has a pipe user
 					// object
 					mxCell mxcell = (mxCell) cell;
 					if (mxcell.getValue() instanceof AbstractPipe) {
 						// It's already set we are done here
 						return ret;
 					}
 					mxCell mxsource = (mxCell) source;
 					mxCell mxtarget = (mxCell) target;
 					if (mxtarget.getValue() instanceof AbstractTask
 							&& mxsource.getValue() instanceof AbstractTask) {
 						AbstractTask sourceTask = (AbstractTask) mxsource
 								.getValue();
 						AbstractTask targetTask = (AbstractTask) mxtarget
 								.getValue();
 						
 						try {
							JGPipeDecorator jgpipe = (JGPipeDecorator) rawConnectTasks(sourceTask, targetTask);
							mxcell.setValue(jgpipe);
							jgpipe.setCell(mxcell);
 						} catch (TasksNotInModelException e) {
 							// shouldn't happen
 						} catch (TasksNotCompatibleException e) {
 							// Too bad, that shouldn't happen BUG
 							System.err.println("BUG: Tried connection incomatible tasks");
 						}
 
 					}
 				}
 				return ret;
 			}
 		};
 		lay = new mxHierarchicalLayout(graph, SwingConstants.NORTH);
 		lay.setLayoutFromSinks(false);
 	}
 
 	public mxGraph getGraph() {
 		return this.graph;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.osmui.model.pipelinemodel.AbstractModel#getSourceTasks()
 	 */
 	@Override
 	public List<AbstractTask> getSourceTasks() {
 
 		ArrayList<AbstractTask> sourceTasks = new ArrayList<AbstractTask>();
 		// Remember we always add sourceTasks to the front so we can break after
 		// finding the first non sourceTask
 		for (JGTaskDecorator task : tasks) {
 			if (task.getInputPorts().isEmpty()) {
 				// We return the Task objects without their decorator here so
 				// that
 				// subclass functionality might be accessed
 				sourceTasks.add(task);
 			} else {
 				break;
 			}
 		}
 
 		return sourceTasks;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.osmui.model.pipelinemodel.AbstractModel#addTask(de.osmui.model.
 	 * pipelinemodel.AbstractTask)
 	 */
 	@Override
 	public void addTask(AbstractTask task) {
 		// Let's decorate the task very christmas like so they can be used by
 		// this JGPipelineModel
 		JGTaskDecorator jgtask = new JGTaskDecorator(task);
 
 		// Add to list of tasks, if it's a sourceTasks add to the beginning,
 		// this speeds up getting sourceTasks
 		if (jgtask.getInputPorts().isEmpty()) {
 			tasks.add(0, jgtask);
 		} else {
 			tasks.add(jgtask);
 		}
 		jgtask.setModel(this);
 		// Add the task to the underling mxGraph model
 		Object parent = graph.getDefaultParent();
 
 		graph.getModel().beginUpdate();
 		try {
 			jgtask.setCell((mxCell) graph.insertVertex(parent, null, jgtask,
 					10, 10, 100, 20));
 		} finally {
 			graph.getModel().endUpdate();
 		}
 		notifyObservers(jgtask);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.osmui.model.pipelinemodel.AbstractModel#addTask(de.osmui.model.
 	 * pipelinemodel.AbstractTask, de.osmui.model.pipelinemodel.AbstractTask)
 	 */
 	@Override
 	public void addTask(AbstractTask parent, AbstractTask child)
 			throws TasksNotCompatibleException, TasksNotInModelException {
 
 		if (parent.getModel() != this) {
 			throw new TasksNotInModelException("parent not in model");
 		}
 
 		// First add the child and then use our internal connect method to wire
 		// things up
 		addTask(child);
 		// We need to connect the now decorated task in the model
 		JGTaskDecorator jgchild = null;
 		for (JGTaskDecorator task : tasks) {
 			if (task.equals(child)) {
 				jgchild = task;
 				break;
 			}
 		}
 		connectTasks(parent, jgchild);
 
 		notifyObservers(child);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.osmui.model.pipelinemodel.AbstractModel#removeTask(de.osmui.model.
 	 * pipelinemodel.AbstractTask)
 	 */
 	@Override
 	public boolean removeTask(AbstractTask task)
 			throws TasksNotInModelException {
 		if (task.getModel() != this) {
 			throw new TasksNotInModelException(
 					"The task to remove is not in the model");
 		}
 
 		JGTaskDecorator jgtask = (JGTaskDecorator) task;
 		// Disconnect all connected pipes
 		for (AbstractPipe out : jgtask.getOutputPipes()) {
 			if (out.isConnected()) {
 				disconnectTasks(jgtask, out.getTarget().getParent());
 			}
 		}
 
 		Object[] cellArray = new Object[1];
 		cellArray[0] = jgtask.getCell();
 
 		graph.removeCells(cellArray);
 		task.setModel(null);
 		return tasks.remove(task);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.osmui.model.pipelinemodel.AbstractModel#connectTasks(de.osmui.model
 	 * .pipelinemodel.AbstractTask, de.osmui.model.pipelinemodel.AbstractTask)
 	 */
 	@Override
 	public AbstractPipe connectTasks(AbstractTask parent, AbstractTask child)
 			throws TasksNotCompatibleException, TasksNotInModelException {
 
 		// Make the normal connection, the cast here is legal because otherwise
 		// TasksNotInModel would be thrown
 		JGPipeDecorator jgpipe = (JGPipeDecorator) super.connectTasks(parent,
 				child);
 		// The tasks are in the model therefore we can cast them
 		JGTaskDecorator jgparent = (JGTaskDecorator) parent;
 		JGTaskDecorator jgchild = (JGTaskDecorator) child;
 
 		// Setup the jgraphx madness
 		Object graphparent = graph.getDefaultParent();
 
 		graph.getModel().beginUpdate();
 		try {
 			jgpipe.setCell((mxCell) graph.insertEdge(graphparent, null, jgpipe,
 					jgparent.getCell(), jgchild.getCell()));
 		} finally {
 			graph.getModel().endUpdate();
 		}
 		return jgpipe;
 	}
 
 	/**
 	 * This helper method connects tasks without adding their connection to the mxGraph
 	 * 
 	 * @param parent
 	 * @param child
 	 * @return
 	 * @throws TasksNotCompatibleException
 	 * @throws TasksNotInModelException
 	 */
 	public AbstractPipe rawConnectTasks(AbstractTask parent, AbstractTask child)
 			throws TasksNotCompatibleException, TasksNotInModelException {
 		return super.connectTasks(parent, child);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.osmui.model.pipelinemodel.AbstractPipelineModel#connectTasks(de.osmui
 	 * .model.pipelinemodel.AbstractPipe,
 	 * de.osmui.model.pipelinemodel.AbstractPort)
 	 */
 	public AbstractPipe connectTasks(AbstractPipe output, AbstractPort input)
 			throws TasksNotCompatibleException, TasksNotInModelException {
 		// Make the normal connection, the cast here is legal because otherwise
 		// TasksNotInModel would be thrown
 		JGPipeDecorator jgpipe = (JGPipeDecorator) super.connectTasks(output,
 				input);
 		// We need to get the corresponding tasks from our task list to make
 		// sure we get the decorated version
 		AbstractTask parent = output.getSource();
 		AbstractTask child = input.getParent();
 		JGTaskDecorator jgparent = null;
 		JGTaskDecorator jgchild = null;
 		for (JGTaskDecorator task : tasks) {
 			if (task.equals(parent)) {
 				jgparent = task;
 			} else if (task.equals(child)) {
 				jgchild = task;
 			}
 		}
 
 		// Setup the jgraphx madness
 		Object graphparent = graph.getDefaultParent();
 
 		graph.getModel().beginUpdate();
 		try {
 			jgpipe.setCell((mxCell) graph.insertEdge(graphparent, null, jgpipe,
 					jgparent.getCell(), jgchild.getCell()));
 		} finally {
 			graph.getModel().endUpdate();
 		}
 		return jgpipe;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * de.osmui.model.pipelinemodel.AbstractModel#disconnectTasks(de.osmui.model
 	 * .pipelinemodel.AbstractTask, de.osmui.model.pipelinemodel.AbstractTask)
 	 */
 	@Override
 	public AbstractPipe disconnectTasks(AbstractTask parent, AbstractTask child)
 			throws TasksNotInModelException {
 		AbstractPipe removedPipe = super.disconnectTasks(parent, child);
 		Object[] cellArray = new Object[1];
 		cellArray[0] = ((JGPipeDecorator) removedPipe).getCell();
 		graph.removeCells(cellArray);
 
 		return removedPipe;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.osmui.model.pipelinemodel.AbstractModel#isExecutable()
 	 */
 	@Override
 	public boolean isExecutable() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void layout() {
 		Object graphparent = graph.getDefaultParent();
 		graph.getModel().beginUpdate();
 		try {
 			lay.execute(graphparent);
 		} finally {
 			graph.getModel().endUpdate();
 		}
 	}
 
 }
