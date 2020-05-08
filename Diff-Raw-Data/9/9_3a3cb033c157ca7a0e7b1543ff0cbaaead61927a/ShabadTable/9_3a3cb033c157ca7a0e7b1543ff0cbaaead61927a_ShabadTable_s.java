 package gsingh.learnkirtan.ui.shabadeditor.tableeditor;
 
 import gsingh.learnkirtan.Keys;
 import gsingh.learnkirtan.ui.WindowTitleManager;
 import gsingh.learnkirtan.ui.action.ActionFactory;
 import gsingh.learnkirtan.validation.Validator;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.EventObject;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.swing.AbstractAction;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.text.JTextComponent;
 
 @SuppressWarnings("serial")
 public class ShabadTable extends JTable {
 
 	private Set<Point> invalidCells = new HashSet<Point>();
 
 	private static final Font font = new Font("Arial", Font.PLAIN, 20);
 
 	private boolean isSelectAllForMouseEvent = false;
 	private boolean isSelectAllForActionEvent = false;
 	private boolean isSelectAllForKeyEvent = false;
 
 	public ShabadTable(int rows, int cols, WindowTitleManager titleManager,
 			ActionFactory actionFactory) {
 		super(new UndoTableModel());
 
 		UndoTableModel model = (UndoTableModel) getModel();
 		model.setRowCount(rows);
 		model.setColumnCount(cols);
 
 		Integer[] headers = new Integer[cols];
 		for (int i = 0; i < 16; i++) {
 			headers[i] = i + 1;
 		}
 
 		setSelectAllForEdit(true);
 		setCellSelectionEnabled(true);
 		setRowHeight(20);
 		setFont(new Font("Arial", Font.PLAIN, 20));
 		model.setColumnIdentifiers(headers);
 		getTableHeader().setReorderingAllowed(false);
 
 		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
 		renderer.setHorizontalAlignment(SwingConstants.CENTER);
 
 		for (int i = 0; i < 16; i++) {
 			TableColumn column = getColumnModel().getColumn(i);
 			column.setCellRenderer(renderer);
 		}
 
 		setActions(actionFactory);
 	}
 
 	private void setActions(ActionFactory actionFactory) {
 		getInputMap().put(Keys.UNDO_KEY, "undo");
 		getInputMap().put(Keys.REDO_KEY, "redo");
 		getInputMap().put(Keys.SAVE_KEY, "save");
 		getInputMap().put(Keys.OPEN_KEY, "open");
 		getInputMap().put(Keys.NEW_KEY, "create");
 		getInputMap().put(Keys.CUT_KEY, "cut");
 		getInputMap().put(Keys.COPY_KEY, "copy");
 		getInputMap().put(Keys.PASTE_KEY, "paste");
 
 		getActionMap().put("save", actionFactory.newSaveAction());
 		getActionMap().put("open", actionFactory.newOpenAction());
 		getActionMap().put("create", actionFactory.newCreateAction());
 		getActionMap().put("cut", new CutAction());
 		getActionMap().put("copy", new CopyAction());
 		getActionMap().put("paste", new PasteAction());
 	}
 
 	/*
 	 * Override to provide Select All editing functionality
 	 */
 	@Override
 	public boolean editCellAt(int row, int column, EventObject e) {
 		boolean result = super.editCellAt(row, column, e);
 
 		if (isSelectAllForMouseEvent || isSelectAllForActionEvent
 				|| isSelectAllForKeyEvent) {
 			selectAll(e);
 		}
 
 		return result;
 	}
 
 	/*
 	 * Select the text when editing on a text related cell is started
 	 */
 	private void selectAll(EventObject e) {
 		final Component editor = getEditorComponent();
 
 		if (editor == null || !(editor instanceof JTextComponent))
 			return;
 
 		if (e == null) {
 			((JTextComponent) editor).selectAll();
 			return;
 		}
 
 		// Typing in the cell was used to activate the editor
 
 		if (e instanceof KeyEvent && isSelectAllForKeyEvent) {
 			((JTextComponent) editor).selectAll();
 			return;
 		}
 
 		// F2 was used to activate the editor
 
 		if (e instanceof ActionEvent && isSelectAllForActionEvent) {
 			((JTextComponent) editor).selectAll();
 			return;
 		}
 
 		// A mouse click was used to activate the editor.
 		// Generally this is a double click and the second mouse click is
 		// passed to the editor which would remove the text selection unless
 		// we use the invokeLater()
 
 		if (e instanceof MouseEvent && isSelectAllForMouseEvent) {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					((JTextComponent) editor).selectAll();
 				}
 			});
 		}
 	}
 
 	//
 	// Newly added methods
 	//
 	/*
 	 * Sets the Select All property for for all event types
 	 */
 	public void setSelectAllForEdit(boolean isSelectAllForEdit) {
 		setSelectAllForMouseEvent(isSelectAllForEdit);
 		setSelectAllForActionEvent(isSelectAllForEdit);
 		setSelectAllForKeyEvent(isSelectAllForEdit);
 	}
 
 	/*
 	 * Set the Select All property when editing is invoked by the mouse
 	 */
 	public void setSelectAllForMouseEvent(boolean isSelectAllForMouseEvent) {
 		this.isSelectAllForMouseEvent = isSelectAllForMouseEvent;
 	}
 
 	/*
 	 * Set the Select All property when editing is invoked by the "F2" key
 	 */
 	public void setSelectAllForActionEvent(boolean isSelectAllForActionEvent) {
 		this.isSelectAllForActionEvent = isSelectAllForActionEvent;
 	}
 
 	/*
 	 * Set the Select All property when editing is invoked by typing directly
 	 * into the cell
 	 */
 	public void setSelectAllForKeyEvent(boolean isSelectAllForKeyEvent) {
 		this.isSelectAllForKeyEvent = isSelectAllForKeyEvent;
 	}
 
 	@Override
 	public Component prepareRenderer(TableCellRenderer renderer, int row,
 			int col) {
 		Component c = super.prepareRenderer(renderer, row, col);
 		if (!c.getBackground().equals(getSelectionBackground())) {
 			if (row % 2 == 1) {
 				if (isEnabled()) {
 					c.setBackground(Color.LIGHT_GRAY);
 				} else {
 					c.setBackground(Color.LIGHT_GRAY);
 				}
 			} else {
 				if (isEnabled()) {
 					c.setBackground(Color.WHITE);
 				} else {
 					c.setBackground(new Color(0xE5, 0xE5, 0xE5));
 				}
 			}
 		}
 
 		if (row % 2 == 1) {
 			String value = (String) getValueAt(row, col);
 			Point point = new Point(row, col);
 			if (value != null && !value.equals("")
 					&& !Validator.validate(value)) {
 				if (!c.getBackground().equals(getSelectionBackground())) {
 					c.setBackground(new Color(0xFF, 0x30, 0x30)); // Red
 				} else {
 					c.setBackground(new Color(0xFF, 0x70, 0x70)); // Light
 																	// Red
 				}
 				invalidCells.add(point);
 					} else {
 				if (invalidCells.contains(point)) {
 					invalidCells.remove(point);
 				}
 			}
 		}
 
 		return c;
 	}
 
 	@Override
 	public Component prepareEditor(TableCellEditor editor, int row, int col) {
 		Component c = super.prepareEditor(editor, row, col);
 		c.setFont(font);
 		return c;
 	}
 
 	public boolean isValidShabad() {
 		return invalidCells.isEmpty();
 	}
 
 	public class CutAction extends AbstractAction {
 
 		private CopyAction copyAction = new CopyAction();
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JTable jTable = ShabadTable.this;
 			copyAction.actionPerformed(e);
 			if (copyAction.success) {
 				int[] rowsselected = jTable.getSelectedRows();
 				int[] colsselected = jTable.getSelectedColumns();
 
 				for (int row : rowsselected) {
 					for (int col : colsselected) {
 						jTable.setValueAt("", row, col);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @see "http://www.javaworld.com/javatips/jw-javatip77.html"
 	 */
 	public class CopyAction extends AbstractAction {
 
 		public boolean success = false;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JTable jTable = ShabadTable.this;
 			StringBuffer sbf = new StringBuffer();
 			// Check to ensure we have selected only a contiguous block of cells
 			int numcols = jTable.getSelectedColumnCount();
 			int numrows = jTable.getSelectedRowCount();
 			int[] rowsselected = jTable.getSelectedRows();
 			int[] colsselected = jTable.getSelectedColumns();
 			if (!((numrows - 1 == rowsselected[rowsselected.length - 1]
 					- rowsselected[0] && numrows == rowsselected.length) && (numcols - 1 == colsselected[colsselected.length - 1]
 					- colsselected[0] && numcols == colsselected.length))) {
 
 				JOptionPane.showMessageDialog(null, "Invalid Copy Selection",
 						"Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
 				success = false;
 				return;
 			}
 			for (int i = 0; i < numrows; i++) {
 				for (int j = 0; j < numcols; j++) {
 					sbf.append(jTable.getValueAt(rowsselected[i],
 							colsselected[j]));
 					if (j < numcols - 1)
 						sbf.append("\t");
 				}
 				sbf.append("\n");
 			}
 			StringSelection stsel = new StringSelection(sbf.toString());
 			Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
 			system.setContents(stsel, stsel);
 			success = true;
 		}
 	}
 
 	public class PasteAction extends AbstractAction {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JTable jTable = ShabadTable.this;
 			Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
 
 			int startRow = (jTable.getSelectedRows())[0];
 			int startCol = (jTable.getSelectedColumns())[0];
 			try {
 				String trstring = (String) (system.getContents(this)
 						.getTransferData(DataFlavor.stringFlavor));
 				StringTokenizer st1 = new StringTokenizer(trstring, "\n");
 				for (int i = 0; st1.hasMoreTokens(); i++) {
 					String rowstring = st1.nextToken();
 					StringTokenizer st2 = new StringTokenizer(rowstring, "\t ");
 					for (int j = 0; st2.hasMoreTokens(); j++) {
 						String value = st2.nextToken();
						if (startRow + i < jTable.getRowCount()
								&& startCol + j < jTable.getColumnCount())
							jTable.setValueAt(value, startRow + i, startCol + j);
 					}
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 }
