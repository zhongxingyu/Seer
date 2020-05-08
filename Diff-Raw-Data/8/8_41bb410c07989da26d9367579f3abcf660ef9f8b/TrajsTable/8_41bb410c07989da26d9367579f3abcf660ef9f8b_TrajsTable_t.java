 //FILE:          TrajsTable.java
 //PROJECT:       Octane
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Ji Yu, jyu@uchc.edu, 1/16/11
 //
 // LICENSE:      This file is distributed under the BSD license.
 //               License text is included with the source distribution.
 //
 //               This file is distributed in the hope that it will be useful,
 //               but WITHOUT ANY WARRANTY; without even the implied warranty
 //               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 //
 //               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 //               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 //               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES./**
 //
 package edu.uchc.octane;
 
 import java.awt.Font;
 import java.util.Arrays;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableRowSorter;
 import javax.swing.RowFilter;
 
 /**
  * A table listing trajectories.
  */
 public class TrajsTable extends JTable {
 
 	private static String[] ColumnNames_ = { "Frame", "Len", "Mobility", "Marked","Notes"};
	private static boolean [] ColumnEditable_ = {false, false, false, true, true};
 	private static Class<?> [] ColumnClasses_ = {Integer.class, Integer.class, Double.class, Boolean.class, String.class};
 	
 	private TrajDataset data_ = null;
 //	private Vector<Trajectory> data_ = null;
 	private boolean [] isVisible_;
 	private Model model_;
 
 	protected class Model extends AbstractTableModel {
 
 		@Override
 		public int getColumnCount() {
 			return ColumnNames_.length;
 		}
 
 		@Override
 		public int getRowCount() {
 			return data_.getSize();
 		}
 
 		@Override
 		public Object getValueAt(int rowIndex, int colIndex) {
 			Trajectory traj = data_.getTrajectoryByIndex(rowIndex);
 			switch (colIndex) {
 			case 0:
 				return traj.get(0).frame;
 			case 1:
 				return traj.getLength();
 			case 2:
 				return traj.getAvgSquareStepSize();
 			case 3:
 				return traj.marked;
 			case 4:
 				return traj.note;
 			}
 			return null;
 		}
 
 		@Override
 		public String getColumnName(int columnIndex) {
 			return ColumnNames_[columnIndex];
 		}
 
 		@Override
 		public Class<?> getColumnClass(int columnIndex) {
 			return ColumnClasses_[columnIndex];
 		}
 		
 		@Override
 		public boolean isCellEditable(int row, int col) {
			return ColumnEditable_[col];
 		}
 
 		@Override
 		public void setValueAt(Object value, int row, int col) {
 			if (ColumnNames_[col] == "Notes") {
 				data_.getTrajectoryByIndex(row).note = (String) value;
 			} else if (ColumnNames_[col] == "Marked") {
 				data_.getTrajectoryByIndex(row).marked = (Boolean) value;
 			}
 		}
 
 	}
 
 	/**
 	 * Constructor.
 	 *
 	 * @param data the associated dataset
 	 */
 	public TrajsTable(TrajDataset data) {
 		super();
 
 		setData(data);
 		
 		model_ = new Model();
 		setModel(model_);
 		setColumnSelectionAllowed(false);
 		setFont(new Font("", Font.PLAIN, 10));
 		setRowSelectionAllowed(true);
 		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
 		//setAutoCreateRowSorter(true);
 		TableRowSorter<Model> sorter = new TableRowSorter<Model>(model_);
 		setRowSorter(sorter);
 		sorter.setRowFilter(new RowFilter<Model, Integer> () {
 			@Override
 			public boolean include(
 					javax.swing.RowFilter.Entry<? extends Model, ? extends Integer> entry) {
 				return isVisible_[entry.getIdentifier()] && !data_.getTrajectoryByIndex(entry.getIdentifier()).deleted;
 			}
 		});
 
 		getColumnModel().getColumn(0).setPreferredWidth(30);
 		getColumnModel().getColumn(1).setPreferredWidth(30);
 		getColumnModel().getColumn(2).setPreferredWidth(30);
 	}
 
 	/**
 	 * Sets the data.
 	 *
 	 * @param data the new dataset
 	 */
 	public void setData(TrajDataset data) {
 		data_ = data;
 		isVisible_ = new boolean[data.getSize()];
 		Arrays.fill(isVisible_, true);
 		clearSelection();
 		tableDataChanged();
 	}
 
 	/**
 	 * Update table to reflect new data.
 	 */
 	public void tableDataChanged() {
 		if (model_ != null ) {
 			model_.fireTableDataChanged();
 		}		
 	}
 
 	/**
 	 * Reverse marks of all selected trajectories.
 	 */
 	public void reverseMarkOfSelected() {
 		boolean acted = false;
 		int [] rows = getSelectedRows();
 		for (int i = 0; i < rows.length; i++) {
 			rows[i] = convertRowIndexToModel(rows[i]);
 		}
 		for (int i = 0; i < rows.length; i++) {		
 			Trajectory v = data_.getTrajectoryByIndex(rows[i]);
 			if (v.marked == false) {
 				v.marked = true;
 				acted = true;
 			}
 		}
 		if (!acted) {
 			for (int i = 0; i < rows.length; i++) {
 				data_.getTrajectoryByIndex(rows[i]).marked = false;
 			}
 		}
 		model_.fireTableRowsUpdated(0,model_.getRowCount()-1);
 	}
 
 	/**
 	 * Hide all unmarked trajectories.
 	 */
 	public void hideUnmarked() {
 		int cnt = 0;
 		for (int i=0; i < data_.getSize(); i++) {
 			isVisible_[cnt ++] = data_.getTrajectoryByIndex(i).marked; 
 		}
 		model_.fireTableDataChanged();
 	}
 
 	/**
 	 * Show all trajectories.
 	 */
 	public void showAll() {
 		Arrays.fill(isVisible_, true);
 		model_.fireTableDataChanged();
 	}
 }
