 package gsingh.learnkirtan.component.shabadeditor;
 
 import gsingh.learnkirtan.WindowTitleManager;
 import gsingh.learnkirtan.parser.Parser;
 import gsingh.learnkirtan.shabad.Shabad;
 
 import java.awt.GridLayout;
 
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 
 @SuppressWarnings("serial")
 public class TableShabadEditor extends SwingShabadEditor {
 
 	private JTable table = new AlternatingRowColorTable(16, 16);
 
 	private DefaultTableModel model = (DefaultTableModel) table.getModel();
 
 	public TableShabadEditor(WindowTitleManager titleManager) {
 		super(titleManager);
 
 		Integer[] headers = new Integer[16];
 		for (int i = 0; i < 16; i++) {
 			headers[i] = i + 1;
 		}
 
 		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
 		renderer.setHorizontalAlignment(SwingConstants.CENTER);
 
 		table.setCellSelectionEnabled(true);
 		table.setRowHeight(20);
 		model.setColumnIdentifiers(headers);
 		table.getTableHeader().setReorderingAllowed(false);
 
 		for (int i = 0; i < 16; i++) {
 			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
 		}
 
 		setLayout(new GridLayout());
 		add(new JScrollPane(table));
 	}
 
 	@Override
 	public Shabad getShabad() {
 		Parser parser = new Parser();
 		Shabad shabad = parser.parse(getText());
 		shabad.setWords(getWords());
 		return shabad;
 	}
 
 	@Override
 	public void setText(String text) {
 		System.out.println(text);
 		String[] words = text.split("\\s+");
 		int numRows = model.getRowCount();
 		for (int i = 1; i < numRows; i += 2) {
 			for (int j = 0; j < 16; j++) {
 				int index = i / 2 * 16 + j;
 				if (index >= words.length)
 					return;
 				String word = words[index];
 				if (!word.equals("NULL")) {
 					// TODO: Titlecase word
 					table.setValueAt(word, i, j);
				} else {
					table.setValueAt(null, i, j);
 				}
 			}
 		}
 	}
 
 	@Override
 	public String getText() {
 		int numRows = model.getRowCount();
 		StringBuilder sb = new StringBuilder();
 		for (int i = 1; i < numRows; i += 2) {
 			for (int j = 0; j < 16; j++) {
 				String value = (String) model.getValueAt(i, j);
 				// if (value != null) {
 				sb.append(model.getValueAt(i, j)).append(" ");
 				// }
 			}
 		}
 		return sb.toString();
 	}
 
 	private String getWords() {
 		int numRows = model.getRowCount();
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < numRows; i += 2) {
 			for (int j = 0; j < 16; j++) {
 				String value = (String) model.getValueAt(i, j);
 				// if (value != null) {
 				sb.append(model.getValueAt(i, j)).append(" ");
 				// }
 			}
 		}
 		return sb.toString();
 	}
 
 	@Override
 	public void setEditable(boolean bool) {
 		setEditable(bool);
 	}
 
 	@Override
 	public void reset() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Inserts a pair of rows into the table
 	 * 
 	 * @param row
 	 *            the row to insert the pair above
 	 */
 	public void addRowAbove(int row) {
 		model.insertRow(row, new Object[] {});
 		model.insertRow(row, new Object[] {});
 	}
 
 	public void addRowBelow(int row) {
 		// TODO
 	}
 
 	/**
 	 * Deletes the specified row from the table. Also deletes the other row in
 	 * the pair
 	 * 
 	 * @param row
 	 *            the row number of the row to delete
 	 */
 	public void deleteRow(int row) {
 		model.removeRow(row);
 		if (row % 2 == 0) {
 			model.removeRow(row);
 		} else {
 			model.removeRow(row - 1);
 		}
 	}
 
 	@Override
 	public void setEnabled(boolean bool) {
 		// TODO Auto-generated method stub
 	}
 
 	public int getSelectedRow() {
 		return table.getSelectedRow();
 	}
 }
