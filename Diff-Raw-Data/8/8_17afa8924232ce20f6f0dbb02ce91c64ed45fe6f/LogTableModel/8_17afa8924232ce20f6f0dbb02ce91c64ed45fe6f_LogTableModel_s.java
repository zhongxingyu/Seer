 package pleocmd.itfc.gui.log;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 import pleocmd.Log;
 
 public final class LogTableModel extends AbstractTableModel {
 
 	private static final long serialVersionUID = 4577491604077043435L;
 
 	private static final Color CLR_MARK = new Color(1.0f, 1.0f, 0.8f, 1.0f);
 
 	private final List<Log> list = new ArrayList<Log>();
 
 	private final LogTable table;
 
	private String mark = "XXXXXXXXXX";
 
 	public LogTableModel(final LogTable table) {
 		this.table = table;
 	}
 
 	@Override
 	public int getColumnCount() {
 		return 4;
 	}
 
 	@Override
 	public int getRowCount() {
 		return list.size();
 	}
 
 	private String getCell(final int rowIndex, final int columnIndex) {
 		switch (columnIndex) {
 		case 0:
 			return list.get(rowIndex).getFormattedTime();
 		case 1:
 			return list.get(rowIndex).getType().toString();
 		case 2:
 			return list.get(rowIndex).getFormattedCaller();
 		case 3:
 			return list.get(rowIndex).getMsg();
 		default:
 			return "???";
 		}
 	}
 
 	@Override
 	public LogTableStyledCell getValueAt(final int rowIndex,
 			final int columnIndex) {
 		return new LogTableStyledCell(getCell(rowIndex, columnIndex),
 				columnIndex == 3, list.get(rowIndex).getTypeColor(),
 				getBackgroundColor(rowIndex, columnIndex), false, false);
 	}
 
 	private Color getBackgroundColor(final int rowIndex, final int columnIndex) {
 		if (columnIndex != 2) return null;
 		if (list.get(rowIndex).getFormattedCaller().startsWith(mark))
 			return CLR_MARK;
 		return null;
 	}
 
 	public void addLog(final Log log) {
 		list.add(log);
 		fireTableRowsInserted(list.size() - 1, list.size() - 1);
 	}
 
 	public void clear() {
 		list.clear();
 		fireTableDataChanged();
 	}
 
 	public void refresh() {
 		// remove all currently not processed logs from our list
 		final int minType = Log.getMinLogType().ordinal();
 		final List<Log> newList = new ArrayList<Log>();
 		for (final Log log : list)
 			if (log.getType().ordinal() >= minType) newList.add(log);
 		if (list.size() != newList.size()) {
 			list.clear();
 			list.addAll(newList);
 			fireTableDataChanged();
 			table.updateRowHeights();
 		}
 	}
 
 	public void writeToFile(final File file) throws IOException {
 		final FileWriter out = new FileWriter(file);
 		for (final Log log : list) {
 			out.write(log.toString());
 			out.write("\n");
 		}
 		out.close();
 	}
 
 	public Log getLogAt(final int index) {
 		return list.get(index);
 	}
 
 	public void setMark(final int index) {
		mark = list.get(index).getFormattedCaller();
 		table.repaint();
 	}
 
 }
