 /*
 	cursus - Race series management program
 	Copyright 2011  Simon Arlott
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.lp0.cursus.ui.component;
 
 import java.awt.Component;
 
 import javax.swing.JCheckBox;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 
 import eu.lp0.cursus.db.data.AbstractEntity;
 
 public class BooleanDatabaseTableCellRenderer<T extends AbstractEntity> extends DatabaseTableCellRenderer<T> {
 	private final Column<T, Boolean> column;
 	private final Border EMPTY_BORDER = new EmptyBorder(1, 1, 1, 1);
 	private final JCheckBox checkBox = new JCheckBox();
 
 	public BooleanDatabaseTableCellRenderer(Column<T, Boolean> column) {
 		this.column = column;
 		checkBox.setHorizontalAlignment(SwingConstants.CENTER);
 		checkBox.setBorderPainted(true);
 		checkBox.setOpaque(true);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int vRow, int vCol) {
 		if (value instanceof AbstractEntity) {
 			value = column.loadValue((T)value, false);
 		} else {
 			// DefaultCellEditor may try to be smart and obtain the renderer by passing us a converted Boolean value
 			value = false;
 		}
 		checkBox.setSelected((Boolean)value);
		checkBox.setEnabled(table.isEnabled());
 		if (isSelected) {
 			checkBox.setForeground(table.getSelectionForeground());
 			checkBox.setBackground(table.getSelectionBackground());
 		} else {
 			checkBox.setForeground(table.getForeground());
 			checkBox.setBackground(table.getBackground());
 		}
 		if (hasFocus) {
 			checkBox.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder")); //$NON-NLS-1$
 		} else {
 			checkBox.setBorder(EMPTY_BORDER);
 		}
 		return checkBox;
 	}
 }
