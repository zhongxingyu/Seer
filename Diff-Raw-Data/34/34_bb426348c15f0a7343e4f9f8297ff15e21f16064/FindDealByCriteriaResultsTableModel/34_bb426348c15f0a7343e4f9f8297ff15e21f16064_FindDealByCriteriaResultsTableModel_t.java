 /**
  * 
  */
 package org.diveintojee.poc.ui.unittesting.deals.model;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.diveintojee.poc.ui.unittesting.Constants;
 import org.diveintojee.poc.ui.unittesting.domain.Deal;
 import org.diveintojee.poc.ui.unittesting.domain.Entity;
 import org.diveintojee.poc.ui.unittesting.domain.ProductType;
 
 /**
  * @author louis.gueye@gmail.com
  * 
  */
 public class FindDealByCriteriaResultsTableModel extends AbstractTableModel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private List<Deal> rows;
 
	private String[] columns = null;
 
 	/**
 	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
 	 */
 	@Override
 	public Class<?> getColumnClass(final int columnIndex) {
 		switch (columnIndex) {
 		case 0:
 			// Date
 			return String.class;
 		case 1:
 			// Entity
 			return Entity.class;
 		case 2:
 			// Product type
 			return ProductType.class;
 		case 3:
 			// Amount
 			return BigDecimal.class;
 		case 4:
 			// Currency
 			return String.class;
 		default:
 			return Object.class;
 		}
 	}
 
 	/**
 	 * @see javax.swing.table.TableModel#getColumnCount()
 	 */
 	@Override
 	public int getColumnCount() {
 		return ArrayUtils.isEmpty(getColumns()) ? 0 : getColumns().length;
 	}
 
 	/**
 	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
 	 */
 	@Override
 	public String getColumnName(final int columnIndex) {
 		return columns[columnIndex];
 	}
 
 	/**
 	 * @return the columns
 	 */
 	public String[] getColumns() {
 		return columns;
 	}
 
 	/**
 	 * @see javax.swing.table.TableModel#getRowCount()
 	 */
 	@Override
 	public int getRowCount() {
 		return CollectionUtils.isEmpty(getRows()) ? 0 : getRows().size();
 	}
 
 	/**
 	 * @return the rows
 	 */
 	public List<Deal> getRows() {
 		return rows;
 	}
 
 	/**
 	 * @see javax.swing.table.TableModel#getValueAt(int, int)
 	 */
 	@Override
 	public Object getValueAt(final int rowIndex, final int columnIndex) {
 
 		if (getRowCount() <= 0)
 			return null;
 
 		if (rowIndex >= getRows().size())
 			return null;
 
 		if (getColumnCount() <= 0)
 			return null;
 
 		if (columnIndex >= getColumnCount())
 			return null;
 
 		Deal deal = getRows().get(rowIndex);
 
 		if (deal == null)
 			return null;
 
 		switch (columnIndex) {
 		case 0:
 			return Constants.DATE_FORMAT.format(deal.getAsOfDate());
 		case 1:
 			return deal.getEntity();
 		case 2:
 			return (deal.getProduct() == null ? null : deal.getProduct()
 					.getType());
 		case 3:
 			return deal.getAmount().setScale(Constants.AMOUNTS_SCALE);
 		case 4:
 			return deal.getCurrencyCode();
 		default:
 			return null;
 		}
 
 	}
 
 	/**
 	 * @param columns
 	 *            the columns to set
 	 */
 	public void setColumns(final String[] columns) {
 		this.columns = columns;
 	}
 
 	/**
 	 * @param rows
 	 *            the rows to set
 	 */
 	public void setRows(final List<Deal> rows) {
 		this.rows = rows;
 	}
 
 }
