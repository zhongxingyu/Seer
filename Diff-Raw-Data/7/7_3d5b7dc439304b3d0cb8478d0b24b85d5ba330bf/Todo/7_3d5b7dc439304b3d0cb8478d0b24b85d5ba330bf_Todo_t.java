 package fr.vergne.taskmanager.gui.todo;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 
 import javax.swing.AbstractAction;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 
 import fr.vergne.taskmanager.gui.TaskUpdateDialog;
 import fr.vergne.taskmanager.gui.gantt.UpdateListener;
 import fr.vergne.taskmanager.task.Task;
 import fr.vergne.taskmanager.task.TaskList;
 import fr.vergne.taskmanager.task.TaskStatus;
 
 @SuppressWarnings("serial")
 public class Todo extends JPanel {
 	private final TodoListModel model = new TodoListModel();
 	private final JTable table = new JTable(model);
	private final JScrollPane tablePane = new JScrollPane(table);
 
 	public Todo() {
 		setLayout(new GridLayout(1, 1));
		add(tablePane);
 		table.getSelectionModel().setSelectionMode(
 				ListSelectionModel.SINGLE_SELECTION);
 
 		final JPopupMenu menu = new JPopupMenu();
 		table.setComponentPopupMenu(menu);
 
 		table.getSelectionModel().addListSelectionListener(
 				new ListSelectionListener() {
 
 					@Override
 					public void valueChanged(ListSelectionEvent arg0) {
 						updateMenu(menu);
 					}
 
 				});
 		table.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// nothing to do
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// nothing to do
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// nothing to do
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				// nothing to do
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				if (event.getClickCount() > 1) {
 					openUpdateDialogForSelection();
 				}
 			}
 		});
 	}
 
 	UpdateListener listener = new UpdateListener() {
 
 		@Override
 		public void update() {
			table.revalidate();
 		}
 	};
 
 	public void applyTaskList(TaskList list) {
 		TaskList old = model.getList();
 		if (old != null) {
 			old.removeUpdateListener(listener);
 		}
 		list.addUpdateListener(listener);
 		model.setList(list);
 		table.revalidate();
 	}
 
 	class TodoListModel implements TableModel {
 		private TaskList list = new TaskList();
 
 		@Override
 		public void setValueAt(Object value, int row, int col) {
 			Task task = getTask(row);
 			switch (col) {
 			case 0:
 				task.setTitle((String) value);
 				break;
 			case 1:
 				task.setStatus((TaskStatus) value);
 				break;
 			case 2:
 				task.setDeadline((Date) value);
 				break;
 			default:
 				throw new RuntimeException("The column " + col
 						+ " is not managed");
 			}
 			for (TableModelListener listener : listeners) {
 				listener.tableChanged(new TableModelEvent(this, row, row, col,
 						TableModelEvent.UPDATE));
 			}
 		}
 
 		public Task getTask(int row) {
 			return list.get(row);
 		}
 
 		@Override
 		public boolean isCellEditable(int arg0, int arg1) {
 			return false;
 		}
 
 		@Override
 		public Object getValueAt(int row, int col) {
 			Task task = getTask(row);
 			switch (col) {
 			case 0:
 				return task.getTitle();
 			case 1:
 				return task.getStatus();
 			case 2:
 				return task.getDeadline();
 			default:
 				throw new RuntimeException("The column " + col
 						+ " is not managed");
 			}
 		}
 
 		@Override
 		public int getRowCount() {
 			return list.size();
 		}
 
 		@Override
 		public String getColumnName(int col) {
 			switch (col) {
 			case 0:
 				return "Title";
 			case 1:
 				return "Status";
 			case 2:
 				return "Deadline";
 			default:
 				throw new RuntimeException("The column " + col
 						+ " is not managed");
 			}
 		}
 
 		@Override
 		public int getColumnCount() {
 			return 3;
 		}
 
 		@Override
 		public Class<?> getColumnClass(int col) {
 			switch (col) {
 			case 0:
 				return String.class;
 			case 1:
 				return TaskStatus.class;
 			case 2:
 				return Date.class;
 			default:
 				throw new RuntimeException("The column " + col
 						+ " is not managed");
 			}
 		}
 
 		private final Collection<TableModelListener> listeners = new LinkedList<TableModelListener>();
 
 		@Override
 		public void addTableModelListener(TableModelListener listener) {
 			listeners.add(listener);
 		}
 
 		@Override
 		public void removeTableModelListener(TableModelListener listener) {
 			listeners.remove(listener);
 		}
 
 		public TaskList getList() {
 			return list;
 		}
 
 		public void setList(TaskList list) {
 			if (list != this.list) {
 				this.list = list;
 				for (TableModelListener listener : listeners) {
 					listener.tableChanged(new TableModelEvent(this));
 				}
 			}
 		}
 	}
 
 	private void updateMenu(final JPopupMenu menu) {
 		menu.removeAll();
 		menu.add(new AbstractAction("Edit...") {
 
 			@Override
 			public boolean isEnabled() {
 				return !table.getSelectionModel().isSelectionEmpty();
 			}
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				openUpdateDialogForSelection();
 			}
 
 		});
 		menu.add(new AbstractAction("Remove") {
 
 			@Override
 			public boolean isEnabled() {
 				return !table.getSelectionModel().isSelectionEmpty();
 			}
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				removeSelection();
 			}
 
 		});
 	}
 
 	private void openUpdateDialogForSelection() {
 		ListSelectionModel selection = table.getSelectionModel();
 		if (!selection.isSelectionEmpty()) {
 			int index = selection.getMinSelectionIndex();
 			Task task = model.getTask(index);
 			new TaskUpdateDialog(task).setVisible(true);
 			table.repaint();
 		}
 	}
 
 	private void removeSelection() {
 		ListSelectionModel selection = table.getSelectionModel();
 		if (!selection.isSelectionEmpty()) {
 			int index = selection.getMinSelectionIndex();
 			Task task = model.getTask(index);
 			model.getList().remove(task);
 			selection.clearSelection();
 		}
 	}
 
 	@Override
 	public synchronized void addKeyListener(KeyListener l) {
 		table.addKeyListener(l);
 	}
 
 	@Override
 	public synchronized KeyListener[] getKeyListeners() {
 		return table.getKeyListeners();
 	}
 }
