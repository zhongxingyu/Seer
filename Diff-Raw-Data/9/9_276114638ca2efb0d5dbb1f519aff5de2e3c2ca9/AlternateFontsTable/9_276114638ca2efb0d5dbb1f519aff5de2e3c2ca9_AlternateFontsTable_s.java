 package com.maton.tools.stiletto.view.table;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Table;
 
 import com.maton.tools.stiletto.model.AlternateFont;
 import com.maton.tools.stiletto.model.Font;
 import com.maton.tools.stiletto.model.Resolution;
 import com.maton.tools.stiletto.view.dnd.IDropReceiver;
 import com.maton.tools.stiletto.view.dnd.TargetTransferDefault;
 import com.maton.tools.stiletto.view.dnd.TransferType;
 
 public class AlternateFontsTable extends DefaultTable<AlternateFont> {
 
 	protected Font font;
 
 	public AlternateFontsTable(Composite parent, int style, Font font) {
 		super(parent, style);
 
 		this.font = font;
 		font.getAlternates().addModelListener(this);
 
 		build();
 	}
 
 	public void dispose() {
 		font.getAlternates().removeModelListener(this);
 	}
 
 	@Override
 	protected Table createTable(Composite parent, int style) {
 		table = new Table(parent, style);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		new TargetTransferDefault(table,
 				new Transfer[] { TransferType.RESOLUTION },
 				new IDropReceiver() {
 
 					@Override
 					public void drop(Control source, Object data, int idx) {
 						if (data instanceof Resolution) {
 							Resolution res = (Resolution) data;
 
 							AlternateFont alt = font.getAlternates()
 									.getElement(res.getName());
 							if (alt == null) {
 								alt = font.getAlternates().newElement(
 										res.getName());
 								alt.setShadowY((int) (res.getScale() * font
 										.getShadowY()));
 								alt.setShadowX((int) (res.getScale() * font
 										.getShadowX()));
 								alt.setSize((int) (res.getScale() * font
 										.getSize()));
 								alt.setStroke((int) (res.getScale() * font
 										.getStrokeWidth()));
 							}
 
 							table.select(font.getAlternates().getList()
 									.indexOf(alt));
 						}
 					}
 
 					@Override
 					public void move(Object data, int idx) {
 
 					}
 				});
 
 		return table;
 	}
 
 	@Override
 	protected ViewerSorter getDefaultSorter() {
 		return null;
 	}
 
 	@Override
 	public Object[] getElements() {
 		return font.getAlternates().toArray();
 	}
 
 	@Override
 	protected DefaultColumn<AlternateFont>[] getColumns() {
 		return new BaseColumn[] { new ResolutionColumn(), new SizeColumn(),
 				new StrokeColumn(), new ShadowXColumn(), new ShadowYColumn() };
 	}
 
 	static org.eclipse.swt.graphics.Image IMAGE;
 
 	static {
 		IMAGE = ImageDescriptor.createFromFile(AlternateFontsTable.class,
 				"image.png").createImage();
 	}
 
 	abstract class BaseColumn extends DefaultColumn<AlternateFont> {
 		public BaseColumn(String property, String title, int width, int style,
 				boolean editable, CellEditor editor) {
 			super(property, title, width, style, editable, editor);
 		}
 	}
 
 	class ResolutionColumn extends BaseColumn {
 
 		public ResolutionColumn() {
 			super("name", "Resolution", 100, SWT.LEFT, false,
 					new TextCellEditor(table));
 		}
 
 		@Override
 		public Object getValue(AlternateFont element) {
 			return element.getName();
 		}
 
 		@Override
 		public String getText(AlternateFont element) {
 			return element.getName();
 		}
 
 		@Override
 		public void modify(AlternateFont element, Object value) {
 			element.setName((String) value);
 		}
 
 	}
 
 	class SizeColumn extends BaseColumn {
 
 		public SizeColumn() {
 			super("size", "Size", 30, SWT.RIGHT, true,
 					new TextCellEditor(table));
 		}
 
 		@Override
 		public Object getValue(AlternateFont element) {
 			return String.valueOf(element.getSize());
 		}
 
 		@Override
 		public String getText(AlternateFont element) {
 			return String.valueOf(element.getSize());
 		}
 
 		@Override
 		public void modify(AlternateFont element, Object value) {
 			try {
 				int x = Integer.parseInt((String) value);
 				element.setSize(x);
 			} catch (Throwable e) {
 			}
 		}
 
 	}
 
 	class StrokeColumn extends BaseColumn {
 
 		public StrokeColumn() {
 			super("stroke", "Stroke", 30, SWT.RIGHT, true, new TextCellEditor(
 					table));
 		}
 
 		@Override
 		public Object getValue(AlternateFont element) {
 			return String.valueOf(element.getStroke());
 		}
 
 		@Override
 		public String getText(AlternateFont element) {
 			return String.valueOf(element.getStroke());
 		}
 
 		@Override
 		public void modify(AlternateFont element, Object value) {
 			try {
 				int x = Integer.parseInt((String) value);
 				element.setStroke(x);
 			} catch (Throwable e) {
 			}
 		}
 
 	}
 
 	class ShadowXColumn extends BaseColumn {
 
 		public ShadowXColumn() {
 			super("shadowX", "X Shdw.", 30, SWT.RIGHT, true,
 					new TextCellEditor(table));
 		}
 
 		@Override
 		public Object getValue(AlternateFont element) {
 			return String.valueOf(element.getShadowX());
 		}
 
 		@Override
 		public String getText(AlternateFont element) {
 			return String.valueOf(element.getShadowX());
 		}
 
 		@Override
 		public void modify(AlternateFont element, Object value) {
 			try {
 				int x = Integer.parseInt((String) value);
 				element.setShadowX(x);
 			} catch (Throwable e) {
 			}
 		}
 
 	}
 
 	class ShadowYColumn extends BaseColumn {
 
 		public ShadowYColumn() {
 			super("shadowY", "Y Shdw.", 30, SWT.RIGHT, true,
 					new TextCellEditor(table));
 		}
 
 		@Override
 		public Object getValue(AlternateFont element) {
 			return String.valueOf(element.getShadowY());
 		}
 
 		@Override
 		public String getText(AlternateFont element) {
 			return String.valueOf(element.getShadowY());
 		}
 
 		@Override
 		public void modify(AlternateFont element, Object value) {
 			try {
 				int x = Integer.parseInt((String) value);
 				element.setShadowY(x);
 			} catch (Throwable e) {
 			}
 		}
 
 	}
 
 	public Font getFont() {
 		return font;
 	}
 
 }
