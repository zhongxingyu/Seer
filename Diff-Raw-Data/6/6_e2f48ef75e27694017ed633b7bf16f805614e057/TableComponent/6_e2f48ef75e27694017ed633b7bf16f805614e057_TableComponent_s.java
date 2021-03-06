 /*
  * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
  * This cross-platform GIS is developed at French IRSTV institute and is able to
  * manipulate and create vector and raster spatial information. OrbisGIS is
  * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
  * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
  *
  *
  *  Team leader Erwan BOCHER, scientific researcher,
  *
  *
  *
  * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
  *
  * Copyright (C) 2010 Erwan BOCHER,  Alexis GUEGANNO, Antoine GOURLAY, Adelin PIAU, Gwendall PETIT
  *
  * This file is part of OrbisGIS.
  *
  * OrbisGIS is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult: <http://www.orbisgis.org/>
  *
  * or contact directly:
  * info _at_ orbisgis.org
  */
 package org.orbisgis.core.ui.editors.table;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.ListSelectionModel;
 import javax.swing.Spring;
 import javax.swing.SpringLayout;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 import org.gdms.data.DataSource;
 import org.gdms.data.FilterDataSourceDecorator;
 import org.gdms.data.edition.EditionEvent;
 import org.gdms.data.edition.EditionListener;
 import org.gdms.data.edition.FieldEditionEvent;
 import org.gdms.data.edition.MetadataEditionListener;
 import org.gdms.data.edition.MultipleEditionEvent;
 import org.gdms.data.metadata.Metadata;
 import org.gdms.data.types.Constraint;
 import org.gdms.data.types.Type;
 import org.gdms.data.values.Value;
 import org.gdms.data.values.ValueFactory;
 import org.gdms.driver.DriverException;
 import org.gdms.sql.strategies.SortComparator;
 import org.orbisgis.core.Services;
 import org.orbisgis.core.background.BackgroundJob;
 import org.orbisgis.core.background.BackgroundManager;
 import org.orbisgis.core.errorManager.ErrorManager;
 import org.orbisgis.core.sif.CRFlowLayout;
 import org.orbisgis.core.sif.SQLUIPanel;
 import org.orbisgis.core.sif.UIFactory;
 import org.orbisgis.core.ui.components.sif.AskValue;
 import org.orbisgis.core.ui.components.text.JButtonTextField;
 import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchContext;
 import org.orbisgis.core.ui.pluginSystem.workbench.WorkbenchFrame;
 import org.orbisgis.core.ui.plugins.views.TableEditorPlugIn;
 import org.orbisgis.core.ui.plugins.views.sqlConsole.syntax.SQLCompletionProvider;
 import org.orbisgis.core.ui.preferences.lookandfeel.OrbisGISIcon;
 import org.orbisgis.core.ui.preferences.lookandfeel.UIColorPreferences;
 import org.orbisgis.core.ui.preferences.lookandfeel.images.IconLoader;
 import org.orbisgis.progress.IProgressMonitor;
 import org.orbisgis.progress.NullProgressMonitor;
 import org.orbisgis.utils.I18N;
 
 public class TableComponent extends JPanel implements WorkbenchFrame {
 
 	private static final String OPTIMALWIDTH = "OPTIMALWIDTH";
 	private static final String SETWIDTH = "SETWIDTH";
 	private static final String SORTUP = "SORTUP";
 	private static final String SORTDOWN = "SORTDOWN";
 	private static final String NOSORT = "NOSORT";
 
 	private static final Color NUMERIC_COLOR = new Color(205, 197, 191);
 	private static final Color DEFAULT_COLOR = new Color(238, 229, 222);
 
 	// Swing components
 	private javax.swing.JScrollPane jScrollPane = null;
 	private JTable table = null;
 	private JLabel nbRowsSelectedLabel = null;
 	private SQLCompletionProvider cpl;
 
 	// Model
 	private int selectedColumn = -1;
 	private DataSourceDataModel tableModel;
 	private DataSource dataSource;
 	private ArrayList<Integer> indexes = null;
 	private Selection selection;
 	private TableEditableElement element;
 	private int selectedRowsCount;
 
 	// listeners
 	private ActionListener menuListener = new PopupActionListener();
 	private ModificationListener listener = new ModificationListener();
 	private SelectionListener selectionListener = new SyncSelectionListener();
 	// flags
 	private boolean managingSelection;
 	private TableEditorPlugIn editor;
 
 	private org.orbisgis.core.ui.pluginSystem.menu.MenuTree menuTree;
 
 	private int patternCaseOption = Pattern.CASE_INSENSITIVE;
 
 	@Override
 	public org.orbisgis.core.ui.pluginSystem.menu.MenuTree getMenuTreePopup() {
 		return menuTree;
 	}
 
 	/**
 	 * This is the default constructor
 	 * 
 	 * @throws DriverException
 	 */
 	public TableComponent(TableEditorPlugIn editor) {
 		this.editor = editor;
 		initialize();
 	}
 
 	/**
 	 * This method initializes this
 	 */
 	private void initialize() {
 		menuTree = new org.orbisgis.core.ui.pluginSystem.menu.MenuTree();
 		this.setLayout(new BorderLayout());
 		add(getJScrollPane(), BorderLayout.CENTER);
 		add(getTableToolBar(), BorderLayout.NORTH);
 		add(getWhereTextField(), BorderLayout.SOUTH);
 	}
 
 	/**
 	 * This method initializes table
 	 * 
 	 * @return javax.swing.JTable
 	 */
 	public javax.swing.JTable getTable() {
 		if (table == null) {
 			table = new JTable();
 			table
 					.setSelectionBackground(UIColorPreferences.TABLE_EDITOR_SELECTION_BACKGROUND);
 			table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
 			table.getSelectionModel().setSelectionMode(
 					ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 			table.setDragEnabled(true);
 			table.getSelectionModel().addListSelectionListener(
 					new ListSelectionListener() {
 						@Override
 						public void valueChanged(ListSelectionEvent e) {
 							if (!e.getValueIsAdjusting()) {
 								if (!managingSelection && (selection != null)) {
 									managingSelection = true;
 									int[] selectedRows = table
 											.getSelectedRows();
 									if (indexes != null) {
 										for (int i = 0; i < selectedRows.length; i++) {
 											selectedRows[i] = indexes
 													.get(selectedRows[i]);
 										}
 									}
 									selectedRowsCount = selectedRows.length;
 									selection.setSelectedRows(selectedRows);
 									managingSelection = false;
 									updateRowsMessage();
 
 								}
 							}
 						}
 					});
 			table.getTableHeader().setReorderingAllowed(false);
 			table.getTableHeader().addMouseListener(
 					new HeaderPopupMouseAdapter());
 			table.addMouseListener(new CellPopupMouseAdapter());
 			table.setColumnSelectionAllowed(true);
 			table.getColumnModel().setSelectionModel(
 					new DefaultListSelectionModel());
 		}
 
 		return table;
 	}
 
 	private Component getTableToolBar() {
 		JToolBar toolBar = new JToolBar();
 		toolBar.setFloatable(false);
 		toolBar.add(getPanelInformation(), BorderLayout.WEST);
 		toolBar.add(getRegexTextField(), BorderLayout.EAST);
 		return toolBar;
 	}
 
 	public JPanel getPanelInformation() {
 		final JPanel informationPanel = new JPanel();
 		final CRFlowLayout flowLayout = new CRFlowLayout();
 		flowLayout.setAlignment(CRFlowLayout.LEFT);
 		informationPanel.setLayout(flowLayout);
 		informationPanel.add(getNbRowsInformation());
 		return informationPanel;
 
 	}
 
 	private JPanel getRegexTextField() {
 		final JPanel regexPanel = new JPanel();
 //		final FlowLayout flowLayout = new FlowLayout();
 //		flowLayout.setAlignment(FlowLayout.TRAILING);
 //		regexPanel.setLayout(flowLayout);
 		final BoxLayout horizontalLayout = new BoxLayout(regexPanel,BoxLayout.LINE_AXIS);
 		regexPanel.setLayout(horizontalLayout);
 		JLabel label = new JLabel(
 				I18N.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.search"));
 		final JButtonTextField regexTxtFilter = new JButtonTextField(20);
 		regexTxtFilter.setBackground(Color.WHITE);
 		regexTxtFilter.setText(I18N.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.put_a_text"));
 		regexTxtFilter.setToolTipText(I18N
 						.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.searchEnter"));
 
 		regexTxtFilter.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 
 			}
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					final String whereText = regexTxtFilter.getText();
 					if (whereText.length() == 0) {
 						if (selectedRowsCount > 0) {
 							selection.clearSelection();
 							updateRowsMessage();
 						}
 
 					} else {
 						findTextPattern(whereText);
 					}
 
 				}
 			}
 		});
 
 		regexTxtFilter.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				if (regexTxtFilter
 						.getText()
 						.equals(I18N	
 						.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.put_a_text"))){
 					regexTxtFilter.setText("");
 				}
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				if (regexTxtFilter.getText().equals("")) {
 					regexTxtFilter.setText(I18N.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.put_a_text"));
 				}
 			}
 		});
 		regexPanel.add(label);
 		regexPanel.add(regexTxtFilter);
 		return regexPanel;
 	}
 
 	public void findTextPattern(final String text) {
 
 		String quote = "\\Q";
 		String endQuote = "\\E";
 
 		String regex = quote + text + endQuote;
 		final Pattern pattern = Pattern.compile(regex, patternCaseOption);
 		BackgroundManager bm = Services.getService(BackgroundManager.class);
 		bm.backgroundOperation(new BackgroundJob() {
 
 			@Override
 			public void run(IProgressMonitor pm) {
 				try {
 					ArrayList<Integer> filtered = new ArrayList<Integer>();
 					pm.startTask("Searching...");
 					for (int i = 0; i < tableModel.getRowCount(); i++) {
 						if (i / 100 == i / 100.0) {
 							if (pm.isCancelled()) {
 								break;
 							} else {
 								pm.progressTo((int) (100 * i / tableModel
 										.getRowCount()));
 							}
 						}
 						Value[] values = dataSource.getRow(i);
 						boolean select = false;
 						for (int j = 0; j < values.length; j++) {
 							Value value = values[j];
 							if (value.getType() == Type.GEOMETRY) {
 								continue;
 							}
 							String valueString = value.toString();
 							pattern.matcher(valueString).reset();
 							select = select
 									|| (pattern.matcher(valueString).find());
 						}
 						pm.endTask();
 
 						if (select) {
 							filtered.add(i);
 						}
 
 					}
 
 					int[] sel = new int[filtered.size()];
 
 					for (int i = 0; i < sel.length; i++) {
 						sel[i] = filtered.get(i);
 					}
 					selection.setSelectedRows(sel);
 					updateTableSelection();
 
 				} catch (DriverException e1) {
 					e1.printStackTrace();
 				}
 
 			}
 
 			@Override
 			public String getTaskName() {
 				return I18N
 						.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.searching");
 			}
 		});
 
 	}
 
 	private JLabel getNbRowsInformation() {
 		JLabel nbRowsMessage = new JLabel();
 		nbRowsMessage
 				.setText(I18N
 						.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.rowNumber"));
 		nbRowsSelectedLabel = nbRowsMessage;
 		nbRowsMessage.setVerticalAlignment(JLabel.CENTER);
 		nbRowsMessage.setPreferredSize(new Dimension(230, 19));
 		return nbRowsMessage;
 	}
 
 	private JTextField getWhereTextField() {
 		final JButtonTextField txtFilter = new JButtonTextField(20);
 		cpl = new SQLCompletionProvider(txtFilter);
 		cpl.install();
 		txtFilter.setBackground(Color.WHITE);
 		txtFilter
 				.setText(I18N
 						.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.put_a_sqlwhere"));
 		txtFilter
 				.setToolTipText(I18N
 						.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.searchCtrlEnter"));
 		txtFilter.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && e.isControlDown()) {
 					final String whereText = txtFilter.getText();
 					if (whereText.length() == 0) {
 						if (selectedRowsCount > 0) {
 							selection.clearSelection();
 							updateRowsMessage();
 						}
 
 					} else {
 
 						try {
 							FilterDataSourceDecorator filterDataSourceDecorator = new FilterDataSourceDecorator(
 									dataSource);
 							filterDataSourceDecorator.setFilter(whereText);
 
 							long dsRowCount = filterDataSourceDecorator
 									.getRowCount();
 
 							List<Integer> map = filterDataSourceDecorator
 									.getIndexMap();
 							int[] sel = new int[map.size()];
 							for (int i = 0; i < dsRowCount; i++) {
 								sel[i] = (int) filterDataSourceDecorator
 										.getOriginalIndex(i);
 							}
 
 							selection.setSelectedRows(sel);
 
 						} catch (DriverException e1) {
 							e1.printStackTrace();
 						}
 
 					}
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 			}
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 			}
 
 		});
 
 		txtFilter.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				if (txtFilter
 						.getText()
 						.equals(
 								I18N
 										.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.put_a_sqlwhere")))
 					txtFilter.setText("");
 			}
 
 			public void mouseExited(MouseEvent e) {
 				if (txtFilter.getText().equals(""))
 					txtFilter
 							.setText(I18N
 									.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.put_a_sqlwhere"));
 			}
 		});
 
 		return txtFilter;
 
 	}
 
 	/**
 	 * This method initializes jScrollPane
 	 * 
 	 * @return javax.swing.JScrollPane
 	 */
 	private javax.swing.JScrollPane getJScrollPane() {
 		if (jScrollPane == null) {
 			jScrollPane = new javax.swing.JScrollPane();
 			jScrollPane.setViewportView(getTable());
 		}
 
 		return jScrollPane;
 	}
 
 	/**
 	 * Shows a dialog with the error type
 	 * 
 	 * @param msg
 	 */
 	private void inputError(String msg, Exception e) {
 		Services.getService(ErrorManager.class).error(msg, e);
 		getTable().requestFocus();
 	}
 
 	public boolean tableHasFocus() {
 		return table.hasFocus() || table.isEditing();
 	}
 
 	public String[] getSelectedFieldNames() {
 		int[] selected = table.getSelectedColumns();
 		String[] ret = new String[selected.length];
 
 		for (int i = 0; i < ret.length; i++) {
 			ret[i] = tableModel.getColumnName(selected[i]);
 		}
 
 		return ret;
 	}
 
 	public void setElement(TableEditableElement element) {
 		if (this.dataSource != null) {
 			this.dataSource.removeEditionListener(listener);
 			this.dataSource.removeMetadataEditionListener(listener);
 			this.selection.removeSelectionListener(selectionListener);
 		}
 		this.element = element;
 		if (this.element == null) {
 			this.dataSource = null;
 			this.selection = null;
 			table.setModel(new DefaultTableModel());
 		} else {
 			this.dataSource = element.getDataSource();
 			this.dataSource.addEditionListener(listener);
 			this.dataSource.addMetadataEditionListener(listener);
 			this.cpl.setRootText("SELECT * FROM " + dataSource.getName()
 					+ " WHERE");
 
 			tableModel = new DataSourceDataModel();
 			table.setModel(tableModel);
 			table.setBackground(DEFAULT_COLOR);
 			autoResizeColWidth(Math.min(5, tableModel.getRowCount()),
 					new HashMap<String, Integer>(),
 					new HashMap<String, TableCellRenderer>());
 			this.selection = element.getSelection();
 			this.selection.setSelectionListener(selectionListener);
 			selectedRowsCount = selection.getSelectedRows().length;
 			updateTableSelection();
 			updateRowsMessage();
 
 		}
 	}
 
 	private void autoResizeColWidth(int rowsToCheck,
 			HashMap<String, Integer> widths,
 			HashMap<String, TableCellRenderer> renderers) {
 		DefaultTableColumnModel colModel = new DefaultTableColumnModel();
 		int maxWidth = 200;
 		for (int i = 0; i < tableModel.getColumnCount(); i++) {
 			TableColumn col = new TableColumn(i);
 			String columnName = tableModel.getColumnName(i);
 			int columnType = tableModel.getColumnType(i).getTypeCode();
 
 			col.setHeaderValue(columnName);
 			TableCellRenderer tableCellRenderer = renderers.get(columnName);
 
 			if (tableCellRenderer == null) {
 				tableCellRenderer = new ButtonHeaderRenderer();
 			}
 			col.setHeaderRenderer(tableCellRenderer);
 
 			Integer width = widths.get(columnName);
 			if (width == null) {
 				width = getColumnOptimalWidth(rowsToCheck, maxWidth, i,
 						new NullProgressMonitor());
 			}
 			col.setPreferredWidth(width);
 			colModel.addColumn(col);
 			switch (columnType) {
 			case Type.DOUBLE:
 			case Type.INT:
 			case Type.LONG:
 				NumberFormat formatter = NumberFormat.getCurrencyInstance();
 				FormatRenderer formatRenderer = new FormatRenderer(formatter);
 				formatRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
 				formatRenderer.setBackground(NUMERIC_COLOR);
 				col.setCellRenderer(formatRenderer);
 				break;
 			default:
 				break;
 			}
 
 		}
 		table.setColumnModel(colModel);
 	}
 
 	private int getColumnOptimalWidth(int rowsToCheck, int maxWidth,
 			int column, IProgressMonitor pm) {
 		TableColumn col = table.getColumnModel().getColumn(column);
 		int margin = 5;
 		int width = 0;
 
 		// Get width of column header
 		TableCellRenderer renderer = col.getHeaderRenderer();
 
 		if (renderer == null) {
 			renderer = table.getTableHeader().getDefaultRenderer();
 		}
 
 		Component comp = renderer.getTableCellRendererComponent(table, col
 				.getHeaderValue(), false, false, 0, 0);
 
 		width = comp.getPreferredSize().width;
 
 		// Check header
 		comp = renderer.getTableCellRendererComponent(table, col
 				.getHeaderValue(), false, false, 0, column);
		width = Math.max(width, comp.getPreferredSize().width);
 		// Get maximum width of column data
 		for (int r = 0; r < rowsToCheck; r++) {
 			if (r / 100 == r / 100.0) {
 				if (pm.isCancelled()) {
 					break;
 				} else {
 					pm.progressTo(100 * r / rowsToCheck);
 				}
 			}
 			renderer = table.getCellRenderer(r, column);
 			comp = renderer.getTableCellRendererComponent(table, table
 					.getValueAt(r, column), false, false, r, column);
 			width = Math.max(width, comp.getPreferredSize().width);
 		}
 
 		// limit
 		width = Math.min(width, maxWidth);
 
 		// Add margin
 		width += 2 * margin;
 
 		return width;
 	}
 
 	private void refreshTableStructure() {
 		TableColumnModel columnModel = table.getColumnModel();
 		HashMap<String, Integer> widths = new HashMap<String, Integer>();
 		HashMap<String, TableCellRenderer> renderers = new HashMap<String, TableCellRenderer>();
 		try {
 			for (int i = 0; i < dataSource.getMetadata().getFieldCount(); i++) {
 				String columnName = null;
 				try {
 					columnName = dataSource.getMetadata().getFieldName(i);
 				} catch (DriverException e) {
 				}
 				int columnIndex = -1;
 				if (columnName != null) {
 					try {
 						columnIndex = columnModel.getColumnIndex(columnName);
 					} catch (IllegalArgumentException e) {
 						columnIndex = -1;
 					}
 					if (columnIndex != -1) {
 						TableColumn column = columnModel.getColumn(columnIndex);
 						widths.put(columnName, column.getPreferredWidth());
 						renderers.put(columnName, column.getHeaderRenderer());
 					}
 				}
 			}
 		} catch (DriverException e) {
 			Services
 					.getService(ErrorManager.class)
 					.warning(
 							I18N
 									.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.refreshTableStructure"),
 							e);
 		}
 		tableModel.fireTableStructureChanged();
 		autoResizeColWidth(Math.min(5, tableModel.getRowCount()), widths,
 				renderers);
 	}
 
 	/**
 	 * Retrieve the index of the data in the data source shown at the index row
 	 * in the displayed table.
 	 * @param row
 	 * @return
 	 */
 	public int getRowIndex(int row) {
 		if (indexes != null) {
 			row = indexes.get(row);
 		}
 		return row;
 	}
 
 	private void updateTableSelection() {
 		if (!managingSelection) {
 			managingSelection = true;
 			ListSelectionModel model = table.getSelectionModel();
 			model.setValueIsAdjusting(true);
 			model.clearSelection();
 			for (int i : selection.getSelectedRows()) {
 				if (indexes != null) {
 					Integer sortedIndex = indexes.indexOf(i);
 					model.addSelectionInterval(sortedIndex, sortedIndex);
 				} else {
 					model.addSelectionInterval(i, i);
 				}
 			}
 			selectedRowsCount = selection.getSelectedRows().length;
 			model.setValueIsAdjusting(false);
 			managingSelection = false;
 			updateRowsMessage();
 		}
 	}
 
 	private void fireTableDataChanged() {
 		Rectangle r = table.getVisibleRect();
 		// to avoid losing the selection
 		managingSelection = true;
 
 		tableModel.fireTableDataChanged();
 
 		managingSelection = false;
 		updateTableSelection();
 
 		table.scrollRectToVisible(r);
 	}
 
 	public void updateRowsMessage() {
 
 		if (selectedRowsCount > 0) {
 			nbRowsSelectedLabel
 					.setText(I18N
 							.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.rowSelected")
 							+ selectedRowsCount + " / " + table.getRowCount());
 		} else {
 			nbRowsSelectedLabel
 					.setText(I18N
 							.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.rowNumber")
 							+ tableModel.getRowCount());
 		}
 	}
 
 	public void moveSelectionUp() {
 		int[] selectedRows = selection.getSelectedRows();
 		HashSet<Integer> selectedRowSet = new HashSet<Integer>();
 		indexes = new ArrayList<Integer>();
 		for (int i : selectedRows) {
 			indexes.add(i);
 			selectedRowSet.add(i);
 		}
 		for (int i = 0; i < tableModel.getRowCount(); i++) {
 			if (!selectedRowSet.contains(i)) {
 				indexes.add(i);
 			}
 		}
 		fireTableDataChanged();
 	}
 
 	private class SyncSelectionListener implements SelectionListener {
 
 		@Override
 		public void selectionChanged() {
 			updateTableSelection();
 		}
 
 	}
 
 	private final class PopupActionListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (OPTIMALWIDTH.equals(e.getActionCommand())) {
 				BackgroundManager bm = Services
 						.getService(BackgroundManager.class);
 				bm.backgroundOperation(new BackgroundJob() {
 
 					@Override
 					public void run(IProgressMonitor pm) {
 						final int width = getColumnOptimalWidth(table
 								.getRowCount(), Integer.MAX_VALUE,
 								selectedColumn, pm);
 						final TableColumn col = table.getColumnModel()
 								.getColumn(selectedColumn);
 						SwingUtilities.invokeLater(new Runnable() {
 
 							@Override
 							public void run() {
 								col.setPreferredWidth(width);
 							}
 						});
 					}
 
 					@Override
 					public String getTaskName() {
 						return I18N.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.columnOptimalWidth");
 					}
 				});
 			} else if (SETWIDTH.equals(e.getActionCommand())) {
 				TableColumn selectedTableColumn = table.getTableHeader()
 						.getColumnModel().getColumn(selectedColumn);
 				AskValue av = new AskValue(
 						I18N.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.newColumnWidth"),
 						null, null, Integer.toString(selectedTableColumn
 								.getPreferredWidth()));
 				av.setType(SQLUIPanel.INT);
 				if (UIFactory.showDialog(av)) {
 					selectedTableColumn.setPreferredWidth(Integer.parseInt(av
 							.getValue()));
 				}
 			} else if (SORTUP.equals(e.getActionCommand())) {
 				BackgroundManager bm = Services
 						.getService(BackgroundManager.class);
 				bm.backgroundOperation(new SortJob(true));
 			} else if (SORTDOWN.equals(e.getActionCommand())) {
 				BackgroundManager bm = Services
 						.getService(BackgroundManager.class);
 				bm.backgroundOperation(new SortJob(false));
 			} else if (NOSORT.equals(e.getActionCommand())) {
 				indexes = null;
 				fireTableDataChanged();
 			}
 			table.getTableHeader().repaint();
 		}
 	}
 
 	private abstract class PopupMouseAdapter extends MouseAdapter {
 		WorkbenchContext wbContext = Services
 				.getService(WorkbenchContext.class);
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			updateContext(e);
 			popup(e);
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			popup(e);
 		}
 
 		/**
 		 * This method is used to update the popup context. Used by plugins to
 		 * determine when it's showing.
 		 * 
 		 * @param e
 		 */
 		private void updateContext(MouseEvent e) {
 			boolean oneColumnHeaderIsSelected = table.getTableHeader()
 					.contains(e.getPoint());
 			selectedColumn = table.columnAtPoint(e.getPoint());
 			int clickedRow = table.rowAtPoint(e.getPoint());
 			if (oneColumnHeaderIsSelected) {
 				if ("ColumnAction".equals(getExtensionPointId())) {
 					wbContext.setHeaderSelected(selectedColumn);
 				} else {
 					wbContext.setRowSelected(e);
 					if (!table.isRowSelected(clickedRow)) {
 						selection.setSelectedRows(new int[] { clickedRow });
 						updateTableSelection();
 					}
 				}
 			} else {
 				wbContext.setRowSelected(e);
 				if (!table.isRowSelected(clickedRow)) {
 					selection.setSelectedRows(new int[] { clickedRow });
 					updateTableSelection();
 				}
 			}
 		}
 
 		private void popup(final MouseEvent e) {
 
 			final Component component = getComponent();
 			component.repaint();
 			if (e.isPopupTrigger()) {
 				JComponent[] menus = null;
 				final JPopupMenu pop = getPopupMenu();				
 				menus = wbContext.getWorkbench().getFrame()
 						.getMenuTableTreePopup().getJMenus();
 				for (JComponent menu : menus) {
 					pop.add(menu);
 				}
 				pop.show(component, e.getX(), e.getY());
 			}
 
 		}
 
 		protected void addMenu(JPopupMenu pop, String text, Icon icon,
 				String actionCommand) {
 			JMenuItem menu = new JMenuItem(text);
 			menu.setIcon(icon);
 			menu.setActionCommand(actionCommand);
 			menu.addActionListener(menuListener);
 			pop.add(menu);
 		}
 
 		protected abstract Component getComponent();
 
 		protected abstract String getExtensionPointId();
 
 		protected abstract JPopupMenu getPopupMenu();
 	}
 
 	private class HeaderPopupMouseAdapter extends PopupMouseAdapter {
 
 		@Override
 		protected Component getComponent() {
 			return table.getTableHeader();
 		}
 
 		@Override
 		protected String getExtensionPointId() {
 			return "ColumnAction";
 		}
 
 		@Override
 		protected JPopupMenu getPopupMenu() {
 			JPopupMenu pop = new JPopupMenu();
 			addMenu(
 					pop,
 					I18N.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.optimalWidth"),
 					IconLoader.getIcon("text_letterspacing.png"), OPTIMALWIDTH);
 			addMenu(
 					pop,
 					I18N.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.setWidth"),
 					null, SETWIDTH);
 			pop.addSeparator();
 			if (tableModel.getColumnType(selectedColumn).getTypeCode() != Type.GEOMETRY) {
 				addMenu(
 						pop,
 						I18N.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.sortAscending"),
 						IconLoader.getIcon("thumb_up.png"), SORTUP);
 				addMenu(
 						pop,
 						I18N.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.sortDescending"),
 						IconLoader.getIcon("thumb_down.png"), SORTDOWN);
 				addMenu(
 						pop,
 						I18N.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.noSort"),
 						OrbisGISIcon.TABLE_REFRESH, NOSORT);
 			}
 			return pop;
 		}
 	}
 
 	private class CellPopupMouseAdapter extends PopupMouseAdapter {
 
 		@Override
 		protected Component getComponent() {
 			return table;
 		}
 
 		@Override
 		protected String getExtensionPointId() {
 			return "CellAction";
 		}
 
 		@Override
 		protected JPopupMenu getPopupMenu() {
 			return new JPopupMenu();
 		}
 	}
 
 	private class ModificationListener implements EditionListener,
 			MetadataEditionListener {
 
 		@Override
 		public void multipleModification(MultipleEditionEvent e) {
 			tableModel.fireTableDataChanged();
 		}
 
 		@Override
 		public void singleModification(EditionEvent e) {
 			if (e.getType() != EditionEvent.RESYNC) {
 				int row = (int) e.getRowIndex();
 				if (indexes != null) {
 					row = indexes.indexOf(new Integer(row));
 				}
 				int column = e.getFieldIndex();
 				if ((e.getType() == EditionEvent.DELETE)
 						|| (e.getType() == EditionEvent.INSERT)) {
 					refreshTableStructure();
 
 				} else {
 					tableModel.fireTableCellUpdated(row, column);
 				}
 				if (row != -1) {
 					table.scrollRectToVisible(table.getCellRect(row, column,
 							true));
 				}
 			} else {
 				refreshTableStructure();
 			}
 		}
 
 		@Override
 		public void fieldAdded(FieldEditionEvent event) {
 			fieldRemoved(null);
 		}
 
 		@Override
 		public void fieldModified(FieldEditionEvent event) {
 			fieldRemoved(null);
 		}
 
 		@Override
 		public void fieldRemoved(FieldEditionEvent event) {
 			refreshTableStructure();
 		}
 
 	}
 
 	public class DataSourceDataModel extends AbstractTableModel {
 		private Metadata metadata;
 
 		private Metadata getMetadata() throws DriverException {
 			if (metadata == null) {
 				metadata = dataSource.getMetadata();
 
 			}
 
 			return metadata;
 		}
 
 		/**
 		 * Returns the name of the field.
 		 * 
 		 * @param col
 		 *            index of field
 		 * 
 		 * @return Name of field
 		 */
 		@Override
 		public String getColumnName(int col) {
 			try {
 				return getMetadata().getFieldName(col);
 			} catch (DriverException e) {
 				return null;
 			}
 		}
 
 		/**
 		 * Returns the type of field
 		 * 
 		 * @param col
 		 *            index of field
 		 * @return Type of field
 		 */
 		public Type getColumnType(int col) {
 			try {
 				return getMetadata().getFieldType(col);
 			} catch (DriverException e) {
 				return null;
 			}
 		}
 
 		/**
 		 * Returns the number of fields.
 		 * 
 		 * @return number of fields
 		 */
 		@Override
 		public int getColumnCount() {
 			try {
 				return getMetadata().getFieldCount();
 			} catch (DriverException e) {
 				return 0;
 			}
 		}
 
 		/**
 		 * Returns number of rows.
 		 * 
 		 * @return number of rows.
 		 */
 		@Override
 		public int getRowCount() {
 			try {
 				return (int) dataSource.getRowCount();
 			} catch (DriverException e) {
 				return 0;
 			}
 		}
 
 		/**
 		 * @see javax.swing.table.TableModel#getValueAt(int, int)
 		 */
 		@Override
 		public Object getValueAt(int row, int col) {
 			try {
 				return dataSource.getFieldValue(getRowIndex(row), col)
 						.toString();
 			} catch (DriverException e) {
 				return "";
 			}
 		}
 
 		/**
 		 * @see javax.swing.table.TableModel#isCellEditable(int, int)
 		 */
 		@Override
 		public boolean isCellEditable(int rowIndex, int columnIndex) {
 			if (element.isEditable()) {
 				try {
 					Type fieldType = getMetadata().getFieldType(columnIndex);
 					Constraint c = fieldType.getConstraint(Constraint.READONLY);
 					return (fieldType.getTypeCode() != Type.RASTER)
 							&& (c == null);
 				} catch (DriverException e) {
 					return false;
 				}
 			} else {
 				return false;
 			}
 		}
 
 		/**
 		 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int,
 		 *      int)
 		 */
 		@Override
 		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 			try {
 				Type type = getMetadata().getFieldType(columnIndex);
 				String strValue = aValue.toString().trim();
 				Value v = ValueFactory.createValueByType(strValue, type
 						.getTypeCode());
 				dataSource.setFieldValue(getRowIndex(rowIndex), columnIndex, v);
 			} catch (DriverException e1) {
 				throw new RuntimeException(e1);
 			} catch (NumberFormatException e) {
 				inputError("Cannot parse number: " + e.getMessage(), e);
 			} catch (ParseException e) {
 				inputError(e.getMessage(), e);
 			}
 		}
 	}
 
 	private final class SortJob implements BackgroundJob {
 
 		private boolean ascending;
 
 		public SortJob(boolean ascending) {
 			this.ascending = ascending;
 		}
 
 		@Override
 		public void run(IProgressMonitor pm) {
 			try {
 				int rowCount = (int) dataSource.getRowCount();
 				Value[][] cache = new Value[rowCount][1];
 				for (int i = 0; i < rowCount; i++) {
 					cache[i][0] = dataSource.getFieldValue(i, selectedColumn);
 				}
 				ArrayList<Boolean> order = new ArrayList<Boolean>();
 				order.add(ascending);
 				TreeSet<Integer> sortset = new TreeSet<Integer>(
 						new SortComparator(cache, order));
 				for (int i = 0; i < rowCount; i++) {
 					if (i / 100 == i / 100.0) {
 						if (pm.isCancelled()) {
 							break;
 						} else {
 							pm.progressTo(100 * i / rowCount);
 						}
 					}
 					sortset.add(new Integer(i));
 				}
 				ArrayList<Integer> indexes = new ArrayList<Integer>();
 				Iterator<Integer> it = sortset.iterator();
 				while (it.hasNext()) {
 					Integer integer = (Integer) it.next();
 					indexes.add(integer);
 				}
 				TableComponent.this.indexes = indexes;
 				SwingUtilities.invokeLater(new Runnable() {
 
 					@Override
 					public void run() {
 						fireTableDataChanged();
 					}
 				});
 			} catch (DriverException e) {
 				Services.getService(ErrorManager.class).error("Cannot sort", e);
 			}
 		}
 
 		@Override
 		public String getTaskName() {
 			return I18N
 					.getText("orbisgis.org.orbisgis.core.ui.editors.table.TableComponent.sorting");
 		}
 	}
 
 	public class ButtonHeaderRenderer extends JButton implements
 			TableCellRenderer {
 
 		public ButtonHeaderRenderer() {
 			setMargin(new Insets(0, 0, 0, 0));
 		}
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 			setText((value == null) ? "" : value.toString());
 			boolean isPressed = (column == selectedColumn);
 			if (isPressed) {
 				setPressedColumn(column);
 			}
 			getModel().setPressed(isPressed);
 			getModel().setArmed(isPressed);
 			return this;
 		}
 
 		public void setPressedColumn(int col) {
 			selectedColumn = col;
 		}
 	}
 
 	public int getSelectedColumn() {
 		return selectedColumn;
 	}
 }
