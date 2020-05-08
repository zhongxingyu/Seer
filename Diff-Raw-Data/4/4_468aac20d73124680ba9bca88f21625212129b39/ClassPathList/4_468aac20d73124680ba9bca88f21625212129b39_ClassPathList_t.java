 package org.kalisen.classpathdoctor.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceAdapter;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceListener;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetAdapter;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetListener;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListModel;
 
 import org.kalisen.classpathdoctor.EmptyPathEntry;
 import org.kalisen.classpathdoctor.PathEntry;
 import org.kalisen.classpathdoctor.gui.ClassPathPanel.PathEntriesTransferable;
 
 public class ClassPathList extends JList {
 
 	private final DragGestureListener listDragGestureListener = new DragGestureListener() {
 
 		public void dragGestureRecognized(DragGestureEvent dge) {
 			JList list = (JList) dge.getComponent();
 			Object[] selection = list.getSelectedValues();
 			PathEntry[] selectedEntries = new PathEntry[selection.length];
 			System
 					.arraycopy(selection, 0, selectedEntries, 0,
 							selection.length);
 			PathEntriesTransferable transferable = new PathEntriesTransferable(
 					selectedEntries);
 			dge.startDrag(DragSource.DefaultMoveDrop, transferable);
 		}
 	};
 
 	private final DragSourceListener listDragSourceListener = new DragSourceAdapter() {
 
 		public void dragDropEnd(DragSourceDropEvent dsde) {
 			if (dsde.getDropSuccess()) {
 				JList list = (JList) dsde.getDragSourceContext().getComponent();
 				int[] selectedIndices = list.getSelectedIndices();
 				((ClassPathListModel) list.getModel())
 						.removeElementAt(selectedIndices[0]);
 				list.repaint();
 			}
 		}
 	};
 
 	private final DropTargetListener listDropTargetListener = new DropTargetAdapter() {
 
 		public void drop(DropTargetDropEvent dtde) {
 			boolean acceptDrop = dtde.isLocalTransfer()
 					&& dtde
 							.isDataFlavorSupported(PathEntriesTransferable.PATHENTRIES_DATAFLAVOR)
 					&& dtde.getDropAction() == DnDConstants.ACTION_MOVE;
 			if (acceptDrop) {
 				dtde.acceptDrop(DnDConstants.ACTION_MOVE);
 				dtde.dropComplete(doDrop(dtde));
 			} else {
 				dtde.rejectDrop();
 			}
 		}
 
 		public boolean doDrop(DropTargetDropEvent dtde) {
 			boolean dropSucceeded = true;
 			Transferable transferable = dtde.getTransferable();
 			PathEntry[] droppedEntries;
 			try {
 				droppedEntries = (PathEntry[]) transferable
 						.getTransferData(PathEntriesTransferable.PATHENTRIES_DATAFLAVOR);
 				Point dropLocation = dtde.getLocation();
 				JList list = (JList) dtde.getDropTargetContext().getComponent();
 				int dropIndex = list.locationToIndex(dropLocation);
 				if (list.getSelectedIndex() < dropIndex) {
 					((ClassPathListModel) list.getModel()).insertElementAt(
 							dropIndex + 1, droppedEntries[0]);
 				} else {
 					((ClassPathListModel) list.getModel()).insertElementAt(
 							dropIndex, droppedEntries[0]);
 				}
 			} catch (Exception e) {
 				dropSucceeded = false;
 			}
 			return dropSucceeded;
 		}
 	};
 
 	/**
 	 * Constructs a <code>ClassPathList</code> that displays the elements in the
 	 * specified, non-<code>null</code> model. All <code>JList</code>
 	 * constructors delegate to this one.
 	 * 
 	 * @param dataModel
 	 *            the data model for this list
 	 * @exception IllegalArgumentException
 	 *                if <code>dataModel</code> is <code>null</code>
 	 */
 	public ClassPathList(ClassPathListModel dataModel) {
 		super(dataModel);
 		setCellRenderer(new ClassPathListCellRenderer());
 
 		// set Drag enable to false as it is related to swing DnD support
 		setDragEnabled(false);
 		DragSource dSource = new DragSource();
 		dSource.addDragSourceListener(this.listDragSourceListener);
 		dSource.createDefaultDragGestureRecognizer(this,
 				DnDConstants.ACTION_MOVE, this.listDragGestureListener);
 		DropTarget dt = new DropTarget(this, this.listDropTargetListener);
 		setDropTarget(dt);
 	}
 
 	/**
 	 * Constructs a <code>ClassPathList</code> that displays the elements in the
 	 * specified array. This constructor just delegates to the
 	 * <code>ListModel</code> constructor.
 	 * 
 	 * @param listData
 	 *            the array of Objects to be loaded into the data model
 	 */
 	public ClassPathList(final PathEntry[] listData) {
 		this(new ClassPathListModel() {
 			public int getSize() {
 				return listData.length;
 			}
 
 			public PathEntry getElementAt(int i) {
 				return listData[i];
 			}
 		});
 	}
 
 	/**
 	 * Constructs a <code>ClassPathList</code> that displays the elements in the
 	 * specified <code>Vector</code>. This constructor just delegates to the
 	 * <code>ListModel</code> constructor.
 	 * 
 	 * @param listData
 	 *            the <code>Vector</code> to be loaded into the data model
 	 */
 	public ClassPathList(final Vector<PathEntry> listData) {
 		this(new ClassPathListModel() {
 			public int getSize() {
 				return listData.size();
 			}
 
 			public PathEntry getElementAt(int i) {
 				return listData.elementAt(i);
 			}
 		});
 	}
 
 	/**
 	 * Constructs a <code>ClassPathList</code> with an empty model.
 	 */
 	public ClassPathList() {
 		this(new ClassPathListModel());
 	}
 
 	@Override
 	public ClassPathListModel getModel() {
 		return (ClassPathListModel) super.getModel();
 	}
 
 	@Override
 	public void setModel(ListModel model) {
 		if (model instanceof ClassPathListModel) {
 			super.setModel(model);
 		} else {
 			throw new IllegalArgumentException("Expected an instance of "
 					+ ClassPathListModel.class.getName() + " but was "
 					+ (model == null ? null : model.getClass().getName()));
 		}
 	}
 
 	@Override
 	public void setListData(Object[] listData) {
 		if (listData instanceof PathEntry[]) {
 			super.setListData(listData);
 		} else {
 			throw new IllegalArgumentException("Expected an instance of "
 					+ PathEntry[].class.getName() + " but was "
 					+ (listData == null ? null : listData.getClass().getName()));
 		}
 	}
 
 	public void setListData(final PathEntry[] listData) {
 		setModel(new ClassPathListModel() {
 			public int getSize() {
 				return listData.length;
 			}
 
 			public PathEntry getElementAt(int i) {
 				return listData[i];
 			}
 		});
 	}
 
 	public void setListData(final Vector<?> listData) {
 		for (Object object : listData) {
 			if (!(object instanceof PathEntry)) {
 				throw new IllegalArgumentException("Expected a Vector of "
 						+ PathEntry.class.getName()
 						+ " but contained instance of "
 						+ (object == null ? null : object.getClass().getName()));
 			}
 		}
 		setModel(new ClassPathListModel() {
 			public int getSize() {
 				return listData.size();
 			}
 
 			public PathEntry getElementAt(int i) {
 				return (PathEntry) listData.get(i);
 			}
 		});
 	}
 
 	public void setListData(final List<PathEntry> listData) {
 		setModel(new ClassPathListModel() {
 			public int getSize() {
 				return listData.size();
 			}
 
 			public PathEntry getElementAt(int i) {
 				return listData.get(i);
 			}
 		});
 	}
 
 	private class ClassPathListCellRenderer extends DefaultListCellRenderer {
 
 		private final Color ODD_COLOR = new Color(0xEEEFFF);
 		private final Color NONEXIST_COLOR = new Color(0xFF0000);
 
 		public ClassPathListCellRenderer() {
 			super();
 		}
 
 		public Component getListCellRendererComponent(JList list, Object value,
 				int index, boolean isSelected, boolean cellHasFocus) {
 			ClassPathList cpList = (ClassPathList) list;
 			Component result = super.getListCellRendererComponent(list, value,
 					index, isSelected, cellHasFocus);
 			PathEntry entry = cpList.getModel().getElementAt(index);
 			setComponentBackground(entry, index, isSelected, result);
 			setComponentText(entry, result);
 			return result;
 		}
 		
 		protected void setComponentText(PathEntry entry, Component toBeModified) {
 			if (EmptyPathEntry.INSTANCE.equals(entry)) {
 				((JLabel) toBeModified).setText(" ");
 			}
 		}
 		
 		protected void setComponentBackground(PathEntry entry,
 				int index, boolean isSelected, Component toBeModified) {
 			if (entry.exists() || EmptyPathEntry.INSTANCE.equals(entry)) {
 				if (!isSelected && index % 2 == 1
 						&& !toBeModified.getBackground().equals(this.ODD_COLOR)) {
 					toBeModified.setBackground(this.ODD_COLOR);
 				}
 			} else if (!isSelected && !entry.exists()) {
 				toBeModified.setBackground(this.NONEXIST_COLOR);
 			}
 		}
 	}
 
 }
