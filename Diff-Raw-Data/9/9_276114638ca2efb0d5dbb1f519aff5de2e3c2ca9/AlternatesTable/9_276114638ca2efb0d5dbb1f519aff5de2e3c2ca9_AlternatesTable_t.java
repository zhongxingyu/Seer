 package com.maton.tools.stiletto.view.table;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Table;
 
 import com.maton.tools.stiletto.model.Alternate;
 import com.maton.tools.stiletto.model.Bundle;
 import com.maton.tools.stiletto.model.Image;
 import com.maton.tools.stiletto.model.Resolution;
 import com.maton.tools.stiletto.view.dnd.IDropReceiver;
 import com.maton.tools.stiletto.view.dnd.TargetTransferDefault;
 import com.maton.tools.stiletto.view.dnd.TransferType;
 
 public class AlternatesTable extends DefaultTable<Alternate> {
 
 	protected Image image;
 	protected String[] alts;
 
 	public AlternatesTable(Composite parent, int style, Image image, Bundle bndl) {
 		super(parent, style);
 
 		this.image = image;
 		alts = bndl.getResolutions().getAlternates();
 		image.getAlternates().addModelListener(this);
 
 		build();
 	}
 
 	public void dispose() {
 		image.getAlternates().removeModelListener(this);
 	}
 
 	@Override
 	protected Table createTable(Composite parent, int style) {
 		table = new Table(parent, style);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		new TargetTransferDefault(table, new Transfer[] { TransferType.RESOLUTION }, new IDropReceiver() {
 
 			@Override
 			public void drop(Control source, Object data, int idx) {
 				if (data instanceof Resolution) {
 					Resolution res = (Resolution) data;
 
 					Alternate alt = image.getAlternates().getElement(res.getName());
 					if (alt == null) {
 						alt = image.getAlternates().newElement(res.getName());
 						alt.setScaleX(res.getScale());
 						alt.setScaleY(res.getScale());
						
						image.getAlternates().notifyChange(alt);
 					}
 					
 					table.select(image.getAlternates().getList().indexOf(alt));
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
 		return image.getAlternates().toArray();
 	}
 
 	@Override
 	protected DefaultColumn<Alternate>[] getColumns() {
 		return new BaseColumn[] { new ResolutionColumn(), new AlternateColumn(alts), new XColumn(),
 				new YColumn() };
 	}
 
 	static org.eclipse.swt.graphics.Image IMAGE;
 
 	static {
 		IMAGE = ImageDescriptor.createFromFile(AlternatesTable.class,
 				"image.png").createImage();
 	}
 
 	abstract class BaseColumn extends DefaultColumn<Alternate> {
 		public BaseColumn(String property, String title, int width, int style,
 				boolean editable, CellEditor editor) {
 			super(property, title, width, style, editable, editor);
 		}
 	}
 
 	class ResolutionColumn extends BaseColumn {
 
 		public ResolutionColumn() {
 			super("name", "Resolution", 100, SWT.LEFT, false, new TextCellEditor(table));
 		}
 
 		@Override
 		public Object getValue(Alternate element) {
 			return element.getName();
 		}
 
 		@Override
 		public String getText(Alternate element) {
 			return element.getName();
 		}
 
 		@Override
 		public void modify(Alternate element, Object value) {
 			element.setName((String) value);
 		}
 
 	}
 
 	class XColumn extends BaseColumn {
 
 		public XColumn() {
 			super("scaleX", "Scale X", 50, SWT.RIGHT, true, new TextCellEditor(table));
 		}
 
 		@Override
 		public Object getValue(Alternate element) {
 			return String.valueOf(element.getScaleX());
 		}
 
 		@Override
 		public String getText(Alternate element) {
 			return String.valueOf(element.getScaleX());
 		}
 
 		@Override
 		public void modify(Alternate element, Object value) {
 			try {
 				float x = Float.parseFloat((String) value);
 				element.setScaleX(x);
 			} catch (Throwable e) {
 			}
 		}
 
 	}
 
 	class YColumn extends BaseColumn {
 
 		public YColumn() {
 			super("scaleY", "Scale Y", 50, SWT.RIGHT, true, new TextCellEditor(table));
 		}
 
 		@Override
 		public Object getValue(Alternate element) {
 			return String.valueOf(element.getScaleY());
 		}
 
 		@Override
 		public String getText(Alternate element) {
 			return String.valueOf(element.getScaleY());
 		}
 
 		@Override
 		public void modify(Alternate element, Object value) {
 			try {
 				float y = Float.parseFloat((String) value);
 				element.setScaleY(y);
 			} catch (Throwable e) {
 			}
 		}
 
 	}
 	
 	class AlternateColumn extends BaseColumn {
 		
 		private String[] alts;
 
 		public AlternateColumn(String[] alts) {
 			super("imageName", "Alternate", 100, SWT.LEFT, true, new ComboBoxCellEditor(table, alts));
 			this.alts = alts;
 		}
 
 		@Override
 		public Object getValue(Alternate element) {
 			for (int i = 0; i < alts.length; i++) {
 				if (element.getImageName().equals(alts[i])) {
 					return new Integer(i);
 				}
 			}
 			return new Integer(0);
 		}
 
 		@Override
 		public String getText(Alternate element) {
 			return element.getImageName();
 		}
 
 		@Override
 		public void modify(Alternate element, Object value) {
 			int v = ((Integer)value).intValue();
 			if (v >= alts.length) {
 				element.setImageName("");
 			} else {
 				element.setImageName(alts[v]);
 			}
 		}
 
 	}
 
 	public Image getImage() {
 		return image;
 	}
 
 }
