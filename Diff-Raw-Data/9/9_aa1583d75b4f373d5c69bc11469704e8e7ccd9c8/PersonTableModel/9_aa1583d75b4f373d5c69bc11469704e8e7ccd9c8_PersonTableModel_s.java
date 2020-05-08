 /**
  * 
  */
 package admin.panel.person;
 
 import java.util.List;
 
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableRowSorter;
 
 import admin.Utils;
 import data.Contestant;
 import data.GameData;
 import data.InvalidFieldException;
 import data.InvalidFieldException.Field;
 import data.Person;
 import data.User;
 
 /**
  * @author kevin
  * 
  */
 public abstract class PersonTableModel<P extends Person> extends AbstractTableModel {
 	private static final long serialVersionUID = 1L;
 
 	protected String[] columnNames;
 	//protected List<P> data;
 
 	protected int sortColumn;
 	protected List<P> globalData;
 
 	protected JTable parent;
 	
 	public static final int INDEX_ID = 0;
 	public static final int INDEX_LASTNAME = 1;
 	public static final int INDEX_FIRSTNAME = 2;
 	
 	/**
 	 * Creates the table model which controls the table's actions and data.
 	 * 
 	 * @param _globaldata
 	 *            The global data stored in GameData, this is done to maintain
 	 *            data persistance with the two, while allowing order
 	 *            manipulation.
 	 */
 	public PersonTableModel(JTable table, List<P> _globaldata) {
 		globalData = _globaldata;
 		//data = new ArrayList<P>(globalData);
 		
 		parent = table;
 	}
 
 	@Override
 	public String getColumnName(int col) {
 		return columnNames[col].toString();
 	}
 
 	@Override
 	public int getRowCount() {
 		return globalData.size();
 	}
 
 	@Override
 	public int getColumnCount() {
 		return columnNames.length;
 	}
 
 	@Override
 	public abstract Object getValueAt(int row, int col);
 
 	@Override
 	public boolean isCellEditable(int row, int col) {
 		// Always uneditable
 		return false;
 	}
 
 	@Override
 	public abstract void setValueAt(Object value, int row, int col);
 
 	/**
 	 * Gets a person based on the row passed, this is used to read clicks.
 	 * 
 	 * @param row
 	 *            The row to gather from
 	 * @return Data contained in the Row in the Generic specified form
 	 */
 	public P getByRow(int row) {
 		return (row > -1 ? globalData.get(row) : null);
 	}
 
 	/**
 	 * Gets the row number of a User's ID, returns the index in the Model.
 	 * 
 	 * @param u
 	 *            The user to find
 	 * @return Row number, -1 if not found
 	 */
 	public int getRowByPerson(P p) {
 		if (p == null)
 			return -1;
 
 		return globalData.indexOf(p);
 	}
 
 	/**
 	 * Adds a person, resorts the table. Updates the stored game data.
 	 * 
 	 * @param c
 	 * @throws InvalidFieldException
 	 *             Thrown if ID already in use.
 	 */
 	protected void addPerson(P p) throws InvalidFieldException {
 		GameData g = GameData.getCurrentGame();
 
 		if (p instanceof Contestant)
 			g.addContestant((Contestant) p);
 		else
 			g.addUser((User) p);
 		
 		fireTableDataChanged();
 	}
 
 	/**
 	 * Removes a person from the table and GameData.
 	 * 
 	 * @param p
 	 *            Person to remove.
 	 */
 	public void removePerson(P p) {
 		GameData g = GameData.getCurrentGame();
 
 		if (p instanceof Contestant)
 			g.removeContestant((Contestant) p);
 		else if (p instanceof User)
 			g.removeUser((User) p);
 		
 		fireTableDataChanged();
 	}
 
 	/**
 	 * Updates the stored person with the same ID to hold the currently stored
 	 * information. Overwrites everything in the current position, also will
 	 * resort the data table. <br>
 	 * If the ID is not present, it WILL create a new entry.
 	 * 
 	 * @param c
 	 *            New contestant data.
 	 * @throws InvalidFieldException
 	 *             Thrown if attempting to add a contestant which has an ID
 	 *             already present
 	 */
 	public void updatePerson(P p) throws InvalidFieldException {
 		// is the ID in use in the game data?
 		GameData g = GameData.getCurrentGame();
 
 		
 		boolean valid;
 		InvalidFieldException ie;
 		if (p instanceof Contestant) {
 			valid = g.checkID((Contestant)p, Contestant.class);
 			ie = new InvalidFieldException(Field.CONT_ID_DUP,
 					"Invalid ID (in use)");
 		} else {
 			valid = g.checkID((User)p, User.class);
 			ie = new InvalidFieldException(Field.USER_ID_DUP,
 					"Invalid ID (in use)");
 		}
 		
 		if (!valid)
 			throw ie;
 
 		//sortTable();
 		fireTableDataChanged();
 	}
 	
 	private RowSelector curRowSelect;
 	
 	/**
 	 * Helper method that forces only ONE row select per event call. 
 	 * Thus, if multiple sources ask it to change rows, it maintains the old,
 	 * but will invalidate them, thus <i>not</i> allowing them to run. This
 	 * breaks nothing as all those calls do is use up time and memory.
 	 * @param row Row to select
 	 * @param viewIndex TODO
 	 * @throws IndexOutOfBoundsException On row < -1.
 	 */
 	public void setRowSelect(int row, boolean viewIndex) {
 		if (row < 0)
 			return;
 		
 		if (curRowSelect != null) {
 			curRowSelect.valid = false;
 		}
 		
 		if (viewIndex) {
 			row = parent.getRowSorter().convertRowIndexToModel(row);
 		}
 		
 		curRowSelect = new RowSelector(row);
 		
 		SwingUtilities.invokeLater(curRowSelect);
 	}
 	
 	/**
 	 * Helper method for {@link PersonTableModel.setRowSelect(int)} for passing
 	 * a Person in. 
 	 * @param p
 	 */
 	public void setRowSelect(P p) {
 		int r = getRowByPerson(p);
 		
 		setRowSelect(r, false);
 	}
 	
 	protected abstract void setComparators(TableRowSorter<PersonTableModel<P>> sort);
 	
 	private class RowSelector implements Runnable {
 
 		protected int row;
 		protected boolean hasRun = false;
 		protected boolean valid = true;
 		
 		public RowSelector(int row) {
 			this.row = row;
 		}
 		
 		@Override
 		public void run() {
 			if (hasRun || !valid) {
 				return;
 			}
 			
 			int parentRow = parent.getSelectedRow();
 			row = parent.getRowSorter().convertRowIndexToView(row);
 			
 			if (row > -1 && parentRow != row) {
 				parent.setRowSelectionInterval(row, row);
 			}
 			
 			hasRun = true;
 		}
 		
 	}
 }
