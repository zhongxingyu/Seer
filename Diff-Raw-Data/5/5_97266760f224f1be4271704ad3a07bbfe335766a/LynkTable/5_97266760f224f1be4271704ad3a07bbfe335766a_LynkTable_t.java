 package com.lynk.swing.component;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.event.RowSorterEvent;
 import javax.swing.event.RowSorterListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 
 import org.jdesktop.swingx.JXTable;
 import org.jdesktop.swingx.decorator.AlignmentHighlighter;
 import org.jdesktop.swingx.decorator.HighlighterFactory;
 import org.jdesktop.swingx.table.TableColumnExt;
 
 import com.lynk.swing.common.Constants;
 import com.lynk.swing.component.table.FilterTableHeaderRenderer;
 import com.lynk.swing.component.table.LynkColumnControlButton;
 import com.lynk.swing.component.table.TableColumnFilterPopup;
 import com.lynk.swing.component.table.TableFilter;
 
 /**
  * 自定义
  * AUTO_RESIZE_OFF, ColumnSelection, CellSelection, RowHeight 24, 
  * TableHeader CENTER, TableHeader bg blue, 
  * ColumnControlVisible true, Highlighter RowHighlighter
  * @author Administrator
  *
  */
 public class LynkTable extends JXTable implements Constants {
 	private static final long serialVersionUID = 1L;
 
 	private static final String DEFAULT_SELECT_ALL_TEXT = "全选";
 	private static final String DEFAULT_ADD_TEXT = "新增";
 	private static final String DEFAULT_DELETE_TEXT = "删除";
 	private static final String DEFAULT_RESTORE_TEXT = "还原删除";
 	
 	private JPopupMenu uiPopMenu;
 	
 	private String selectAllText = DEFAULT_SELECT_ALL_TEXT;
 	private String addText = DEFAULT_ADD_TEXT;
 	private String deleteText = DEFAULT_DELETE_TEXT;
 	private String restoreText = DEFAULT_RESTORE_TEXT;
 	
 	private JMenuItem uiSelectAll;
 	private JMenuItem uiAdd;
 	private JMenuItem uiDelete;
 	private JMenuItem uiRestore;
 	
 	private MouseDoubleClick mouseDoubleClick;
 	private MenuAddAction menuAddAction;
 	private MenuDeleteAction menuDeleteAction;
 	private MenuRestoreAction menuRestoreAction;
 	
 	private IModelOrSorterChanged modelOrSorterChanged;
 	
 	private TableColumnFilterPopup popup;
 	
 	private boolean initHighLighter = true;
 	
 	public JPopupMenu getUiPopMenu() {
 		return uiPopMenu;
 	}
 
 	/**
 	 * 不筛选
 	 * @param dm
 	 */
 	public LynkTable(TableModel dm) {
 		this(dm, false);
 	}
 	
 	public LynkTable(TableModel dm, final boolean showFilter) {
 		super(dm);
 		init();
 		if(showFilter) {
 			dm.addTableModelListener(new TableModelListener() {
 				
 				@Override
 				public void tableChanged(TableModelEvent e) {
 					if(popup.getFilter() != null) {
 //						RowSorter<?> sorter = LynkTable.this.getRowSorter();
 //						if(sorter instanceof DefaultRowSorter<?, ?>) {
 //							((DefaultRowSorter<?, ?>) sorter).setRowFilter(null);
 //						}
 						popup.getFilter().setFilter(LynkTable.this);
 						popup.refreshUiFilterList();
 					}
 				}
 			});
 		}
 	}
 	
 	public LynkTable(TableModel dm, final boolean showFilter, boolean initHighLighter) {
 		super(dm);
 		this.initHighLighter = initHighLighter;
 		init();
 		if(showFilter) {
 			dm.addTableModelListener(new TableModelListener() {
 				
 				@Override
 				public void tableChanged(TableModelEvent e) {
 					if(popup.getFilter() != null) {
 //						RowSorter<?> sorter = LynkTable.this.getRowSorter();
 //						if(sorter instanceof DefaultRowSorter<?, ?>) {
 //							((DefaultRowSorter<?, ?>) sorter).setRowFilter(null);
 //						}
 						popup.getFilter().setFilter(LynkTable.this);
 						popup.refreshUiFilterList();
 					}
 				}
 			});
 		}
 	}
 	
 	/**
 	 * 全选文本
 	 * @param selectAllText
 	 */
 	public void setSelectAllText(String selectAllText) {
 		this.selectAllText = selectAllText;
 	}
 
 	/**
 	 * 新增文本
 	 * @param addText
 	 */
 	public void setAddText(String addText) {
 		this.addText = addText;
 	}
 
 	/**
 	 * 删除文本
 	 * @param deleteText
 	 */
 	public void setDeleteText(String deleteText) {
 		this.deleteText = deleteText;
 	}
 
 	/**
 	 * 恢复文本
 	 * @param restoreText
 	 */
 	public void setRestoreText(String restoreText) {
 		this.restoreText = restoreText;
 	}
 
 	/**
 	 * 设置列宽度
 	 * @param sizes, 小于0表示该列长度不可调整
 	 */
 	public void setColumnSize(int... sizes) {
 		TableColumnModel cm = getColumnModel();
 		for (int i = 0; i < (cm.getColumnCount() < sizes.length ? cm
 				.getColumnCount() : sizes.length); i++) {
 			if(sizes[i] < 0) {
 				cm.getColumn(i).setResizable(false);
 			}
 			cm.getColumn(i).setPreferredWidth(Math.abs(sizes[i]));
 		}
 	}
 	
 	/**
 	 * 设置列宽度
 	 * @param sizeStr, 逗号隔开
 	 */
 	public void setColumnSize(String sizeStr) {
 		String[] sizeStrs = sizeStr.split(",");
 		TableColumnModel cm = getColumnModel();
 		for (int i = 0; i < (cm.getColumnCount() < sizeStrs.length ? cm
 				.getColumnCount() : sizeStrs.length); i++) {
			if(Integer.parseInt(sizeStrs[i].trim()) < 0) {
 				cm.getColumn(i).setResizable(false);
 			}
			cm.getColumn(i).setPreferredWidth(Math.abs(Integer.parseInt(sizeStrs[i].trim())));
 		}
 	}
 	
 	/**
 	 * 设置列是否显示
 	 * @param visibleColumnNameStr, 逗号隔开
 	 */
 	public void setColumnVisible(String visibleColumnNameStr) {
 		List<String> visibleColumnNames = new ArrayList<>(Arrays.asList(visibleColumnNameStr.split(",")));
 		int columnCount = getColumnCount(true);
 		for(int i = columnCount - 1; i >= 0 ; i--) {
 			TableColumnExt columnExt =  getColumnExt(i);
 			String columnName = columnExt.getTitle();
 			if(visibleColumnNames.contains(columnName)) {
 				columnExt.setVisible(true);
 			} else {
 				columnExt.setVisible(false);
 			}
 		}
 	}
 	
 	/**
 	 * 设置列是否显示
 	 * @param visibleColumnNameStr, 逗号隔开
 	 */
 	public void setColumnVisible(String[] visibleColumnNamesArray) {
 		List<String> visibleColumnNames = Arrays.asList(visibleColumnNamesArray);
 		int columnCount = getColumnCount(true);
 		for(int i = columnCount - 1; i >= 0 ; i--) {
 			TableColumnExt columnExt =  getColumnExt(i);
 			String columnName = columnExt.getTitle();
 			if(visibleColumnNames.contains(columnName)) {
 				columnExt.setVisible(true);
 			} else {
 				columnExt.setVisible(false);
 			}
 		}
 	}
 	
 	/**
 	 * 设置table显示样式
 	 * @param visibleColumnNames
 	 * @param sizeStr
 	 */
 	public void customizeTable(String[] visibleColumnNames, String sizeStr) {
 		setColumnVisible(visibleColumnNames);
 		if(sizeStr != null && sizeStr.length() > 0) {
 			setColumnSize(sizeStr);
 		}
 	}
 	
 	/**
 	 * 设置table显示样式
 	 * @param visibleColumnNameStr
 	 * @param sizeStr
 	 */
 	public void customizeTable(String visibleColumnNameStr, String sizeStr) {
 		if(visibleColumnNameStr != null && visibleColumnNameStr.length() > 0) {
 			setColumnVisible(visibleColumnNameStr);
 		}
 		if(sizeStr != null && sizeStr.length() > 0) {
 			setColumnSize(sizeStr);
 		}
 	}
 	
 	/**
 	 * 得表格显示的列宽字符串, 用逗号隔开
 	 * @return
 	 */
 	public String getVisibleColumnWidthStr() {
 		String sizeStr = "";
 		int count = getColumnCount();
 		for(int i = 0; i < count; i++) {
 			TableColumnExt columnExt = getColumnExt(i);
 			String Size = Integer.toString(columnExt.getWidth());
 			sizeStr = sizeStr + Size + ",";
 		}
 		if(sizeStr.endsWith(",")) {
 			sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
 		}
 		return sizeStr;
 	}
 	
 	/**
 	 * 获得表格显示的列名字符串, 用,隔开
 	 * @param jTable
 	 * @return
 	 */
 	public String getVisibleColumnNameStr() {
 		String titleStr = "";
 		int count = getColumnCount();
 		for(int i = 0; i < count; i++) {
 			TableColumnExt columnExt = getColumnExt(i);
 			String title = columnExt.getTitle();
 			titleStr = titleStr + title + ",";
 		}
 		if(titleStr.endsWith(",")) {
 			titleStr = titleStr.substring(0, titleStr.length() - 1);
 		}
 		return titleStr;
 	}
 	
 	
 	
 	@Override
 	protected JComponent createDefaultColumnControl() {
 		return new LynkColumnControlButton(this);
 	}
 
 	/**
 	 * 鼠标双击事件
 	 * @param mouseDoubleClick
 	 */
 	public void setMouseDoubleClick(MouseDoubleClick mouseDoubleClick) {
 		this.mouseDoubleClick = mouseDoubleClick;
 	}
 
 	/**
 	 * popmenu 新增事件
 	 * @param menuAddAction
 	 */
 	public void setMenuAddAction(MenuAddAction menuAddAction) {
 		this.menuAddAction = menuAddAction;
 		uiAdd = new JMenuItem(addText, new ImageIcon(this.getClass().getResource("/resources/images/add.png")));
 		uiAdd.setFont(APP_FONT);
 		uiAdd.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent evt) {
 				LynkTable.this.menuAddAction.add();
 			}
 		});
 		uiPopMenu.add(uiAdd);
 	}
 
 	/**
 	 * popmenu 删除事件
 	 * @param menuDeleteAction
 	 */
 	public void setMenuDeleteAction(MenuDeleteAction menuDeleteAction) {
 		this.menuDeleteAction = menuDeleteAction;
 		uiDelete = new JMenuItem(deleteText, new ImageIcon(this.getClass().getResource("/resources/images/disable.png")));
 		uiDelete.setFont(APP_FONT);
 		uiDelete.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent evt) {
 				LynkTable.this.menuDeleteAction.delete(getSelectedRows());
 			}
 		});
 		uiPopMenu.add(uiDelete);
 	}
 	
 	/**
 	 * popmenu 还原事件
 	 * @param menuRestoreAction
 	 */
 	public void setMenuRestoreAction(MenuRestoreAction menuRestoreAction) {
 		this.menuRestoreAction = menuRestoreAction;
 		uiRestore = new JMenuItem(restoreText, new ImageIcon(this.getClass().getResource("/resources/images/enable.png")));
 		uiRestore.setFont(APP_FONT);
 		uiRestore.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent evt) {
 				LynkTable.this.menuRestoreAction.restore(getSelectedRows());
 			}
 		});
 		uiPopMenu.add(uiRestore);
 	}
 
 	private void init() {
 		TableFilter filter = new TableFilter(this);
 		popup = new TableColumnFilterPopup(true, filter);
 		FilterTableHeaderRenderer renderer = new FilterTableHeaderRenderer(filter);
 		for(TableColumn column : Collections.list(getColumnModel().getColumns())) {
 			column.setHeaderRenderer(renderer);
 		}
 		
 		uiPopMenu = new JPopupMenu();
 		
 		uiSelectAll = new JMenuItem(selectAllText, new ImageIcon(this.getClass().getResource("/resources/images/select-all.png")));
 		uiSelectAll.setFont(APP_FONT);
 		uiSelectAll.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent evt) {
 				if(getRowCount() == 0) {
 					return;
 				}
 				if(getRowCount() == getSelectedRowCount()) {
 					clearSelection();
 					return;
 				}
 				selectAll();
 			}
 		});
 		uiPopMenu.add(uiSelectAll);
 		
 		addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseClicked(MouseEvent evt) {
 				if(SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2 && mouseDoubleClick != null) {
 					int index = rowAtPoint(evt.getPoint());
 					if(index != -1) {
 						setRowSelectionInterval(index, index);
 						setColumnSelectionInterval(0, getColumnCount() - 1);
 						mouseDoubleClick.doubleClick(index);
 					}
 				}
 				if(SwingUtilities.isRightMouseButton(evt)) {
 					boolean bAdd;
 					boolean bDelete;
 					boolean bSelectAll;
 					
 					if(getRowCount() == 0) {
 						bAdd = true;
 						bDelete = false;
 						bSelectAll = false;
 					} else {
 						Point p = evt.getPoint();
 						int rowIndex = rowAtPoint(p);
 						int columnIndex = columnAtPoint(p);
 						if(rowIndex == -1 && columnIndex == -1) {
 							clearSelection();
 							bAdd = true;
 							bDelete = false;
 							bSelectAll = true;
 						} else {//pointIndex != -1
 							if(!isRowSelected(rowIndex)) {//鼠标处选中
 								setRowSelectionInterval(rowIndex, rowIndex);
 								setColumnSelectionInterval(columnIndex, columnIndex);
 							}
 							bAdd = true;
 							bDelete = true;
 							bSelectAll = true;
 						} 
 					}
 					if(uiAdd != null) {
 						uiAdd.setEnabled(bAdd);
 					}
 					if(uiDelete != null) {
 						uiDelete.setEnabled(bDelete);
 					}
 					if(uiSelectAll != null) {
 						uiSelectAll.setEnabled(bSelectAll);
 					}
 					uiPopMenu.show(LynkTable.this, evt.getX(), evt.getY());
 				}
 			}
 		});
 		setTableProperties();
 	}
 	
 	/**
 	 * 设置table样式
 	 */
 	private void setTableProperties() {
 		setAutoResizeMode(AUTO_RESIZE_OFF);
 		setColumnSelectionAllowed(true);
 		setCellSelectionEnabled(true);
 		setRowHeight(24);
 		((DefaultTableCellRenderer) getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
 		getTableHeader().setForeground(Color.BLUE);
 		setColumnControlVisible(true);
 		setFont(APP_FONT);
 		if(initHighLighter) {
 			setHighlighters(HighlighterFactory.createAlternateStriping(new Color(255,251,191), new Color(191,255,222)));
 		}
 	}
 
 	public void setRowAlignCenter() {
 		addHighlighter(new AlignmentHighlighter(SwingConstants.CENTER));
 	}
 	
 	public void addModelOrSorterChanged(IModelOrSorterChanged evt) {
 		modelOrSorterChanged = evt;
 		getModel().addTableModelListener(new TableModelListener() {
 			
 			@Override
 			public void tableChanged(TableModelEvent e) {
 				if(modelOrSorterChanged != null) {
 					modelOrSorterChanged.modelOrSorterChanged();
 				}
 			}
 		});
 
 		getRowSorter().addRowSorterListener(new RowSorterListener() {
 			
 			@Override
 			public void sorterChanged(RowSorterEvent e) {
 				if(modelOrSorterChanged != null) {
 					modelOrSorterChanged.modelOrSorterChanged();
 				}
 			}
 		});
 	}
 	
 	public interface MouseDoubleClick {
 		void doubleClick(int index);
 	}
 	
 	public interface MenuAddAction {
 		void add();
 	}
 	
 	public interface MenuDeleteAction {
 		void delete(int indexes[]);
 	}
 	
 	public interface MenuRestoreAction {
 		void restore(int indexes[]);
 	}
 	
 	public interface IModelOrSorterChanged {
 		void modelOrSorterChanged();
 	}
 }
