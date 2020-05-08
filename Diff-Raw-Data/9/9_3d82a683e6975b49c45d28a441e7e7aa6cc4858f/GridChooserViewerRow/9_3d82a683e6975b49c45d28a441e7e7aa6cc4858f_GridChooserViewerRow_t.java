 package net.karlmartens.ui.viewer;
 
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.ViewerRow;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Widget;
 
 import net.karlmartens.ui.widget.GridChooserItem;
 
 final class GridChooserViewerRow extends ViewerRow {
 
 	private GridChooserItem _item;
 
 	GridChooserViewerRow(GridChooserItem item) {
 		_item = item;
 	}
 	
 	void setItem(GridChooserItem item) {
 		_item = item;
 	}
 
 	@Override
 	public Rectangle getBounds(int columnIndex) {
 		return _item.getBounds(columnIndex);
 	}
 
 	@Override
 	public Rectangle getBounds() {
 		return _item.getBounds();
 	}
 
 	@Override
 	public Widget getItem() {
 		return _item;
 	}
 
 	@Override
 	public int getColumnCount() {
 		return _item.getParent().getColumnCount();
 	}
 
 	@Override
 	public Image getImage(int columnIndex) {
 		return _item.getImage(columnIndex);
 	}
 
 	@Override
 	public void setImage(int columnIndex, Image image) {
 		_item.setImage(columnIndex, image);
 	}
 
 	@Override
 	public String getText(int columnIndex) {
 		return _item.getText(columnIndex);
 	}
 
 	@Override
 	public void setText(int columnIndex, String text) {
 		_item.setText(columnIndex, text);
 	}
 
 	@Override
 	public Color getBackground(int columnIndex) {
 		return _item.getBackground(columnIndex);
 	}
 
 	@Override
 	public void setBackground(int columnIndex, Color color) {
 		_item.setBackground(columnIndex, color);
 	}
 
 	@Override
 	public Color getForeground(int columnIndex) {
 		return _item.getForeground(columnIndex);
 	}
 
 	@Override
 	public void setForeground(int columnIndex, Color color) {
 		_item.setForeground(columnIndex, color);
 	}
 
 	@Override
 	public Font getFont(int columnIndex) {
 		return _item.getFont(columnIndex);
 	}
 
 	@Override
 	public void setFont(int columnIndex, Font font) {
 		_item.setFont(columnIndex, font);
 	}
 
 	@Override
 	public Control getControl() {
 		return _item.getParent();
 	}
 
 	@Override
 	public ViewerRow getNeighbor(int direction, boolean sameLevel) {
 		if (ViewerRow.ABOVE == direction) {
 			return getRowAbove();
 		}
 		
 		if (ViewerRow.BELOW == direction) {
 			return getRowBelow();
 		}
 		
 		throw new IllegalArgumentException();
 	}
 
 	private GridChooserViewerRow getRowAbove() {
 		final int index = _item.getParent().indexOf(_item) - 1;
 		if (index >= 0) {
 			return new GridChooserViewerRow(_item.getParent().getItem(index));
 		}
 		return null;
 	}
 	
 	private GridChooserViewerRow getRowBelow() {
 		final int index = _item.getParent().indexOf(_item) + 1;
		if (index < _item.getParent().getItemCount()) {
			final GridChooserItem item = _item.getParent().getItem(index);
			if (item != null) {
				return new GridChooserViewerRow(item);
			}
 		}
		
 		return null;
 	}
 
 	@Override
 	public TreePath getTreePath() {
 		return new TreePath(new Object[] {_item.getData()});
 	}
 
 	@Override
 	public Object clone() {
 		return new GridChooserViewerRow(_item);
 	}
 
 	@Override
 	public Object getElement() {
 		return _item.getData();
 	}
 }
