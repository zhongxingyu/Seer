 package topshelf.gwt.editor.client;
 
 import java.util.List;
 
 import topshelf.gwt.common.client.AbstractBaseList;
 import topshelf.gwt.common.client.ListItem;
 
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.editor.client.IsEditor;
 import com.google.gwt.editor.client.LeafValueEditor;
 import com.google.gwt.editor.client.adapters.EditorSource;
 import com.google.gwt.editor.client.adapters.ListEditor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Base {@link ListEditor<LI,Editor<LI>} for implementations
  * {@link OrderedListEditor} and {@link UnorderedListEditor}.
  * 
  * @author bloo
  *
  * @param <LI> the data type represented by the list item
  */
 public abstract class AbstractBaseListEditor<LT extends AbstractBaseList,LI>
 	extends Composite implements IsEditor<ListEditor<LI,Editor<LI>>>{
 	
 	public static abstract class ListItemRenderer<LI> {
 		AbstractBaseListEditor<?,LI> backingEditor;
 		protected void setBackingList(AbstractBaseListEditor<?,LI> backingEditor) {
 			this.backingEditor = backingEditor;
 		}
 		protected List<LI> getList() {
 			return backingEditor.asEditor().getList();
 		}
 		public abstract Widget renderListItemContent(int index, LI value);
 	}
 	
 	private LT listWidget;
 	private ListItemRenderer<LI> listItemRenderer;
 	private ListEditor<LI,Editor<LI>> listEditor;
 
 	protected AbstractBaseListEditor(LT list) {
 		init(list, null);
 	}
 	
 	protected AbstractBaseListEditor(LT list, ListItemRenderer<LI> renderer) {
 		init(list, renderer);
 	}
 	
 	private void init(final LT list, ListItemRenderer<LI> renderer) {
		if (null != renderer) {
			renderer.setBackingList(this);
		}
 		this.listWidget = list;
 		this.listItemRenderer = renderer;
 		this.listEditor = ListEditor.of(new EditorSource<Editor<LI>>() {
 
 			@Override
 			public ListItemEditor create(int index) {
 				ListItemEditor lie = new ListItemEditor(index);
 				list.insert(lie, index);
 				return lie;
 			}
 			
 			@Override
 			public void dispose(Editor<LI> listItemEditor) {
 				list.remove((ListItemEditor)listItemEditor);
 			}			
 		});
 		initWidget(list);
 	}
 	
 	/**
 	 * set whether this list's ListItems should be displayed inline
 	 * 
 	 * @param inline
 	 */
 	public void setInline(boolean inline) {
 		listWidget.setInline(inline);
 	}
 	
 	public void setListItemRenderer(ListItemRenderer<LI> renderer) {
 		this.listItemRenderer = renderer;
 	}
 	
 	@Override
 	public ListEditor<LI, Editor<LI>> asEditor() {
 		return listEditor;
 	}
 
 	private class ListItemEditor extends ListItem implements LeafValueEditor<LI> {
 
 		private int index;
 		private LI value;
 		
 		ListItemEditor(int index) {
 			this.index = index;
 		}
 		
 		@Override
 		public void setValue(LI value) {
 			this.value = value;
 			super.clear();
 			Widget w = getRenderer().renderListItemContent(index, value);
 			if (null != w) {
 				super.add(w);
 			}
 		}
 		
 		@Override
 		public LI getValue() {
 			return value;
 		}
 	}
 	
 	/**
 	 * If we haven't set a ListItemRenderer either by constructor or the
 	 * setter, create a default, Label based renderer on the fly and return.
 	 * 
 	 * @return renderer responsible for producing a Widget given the leaf
 	 * value being edited.
 	 */
 	private ListItemRenderer<LI> getRenderer() {
 		if (null == listItemRenderer) {
 			listItemRenderer = new ListItemRenderer<LI>() {
 				@Override public Widget renderListItemContent(int index, LI value) {
 					return null == value ? null : new Label(value.toString());
 				}
 			};
 		}
 		return listItemRenderer;
 	}
 	
 	public LT getListWidget() {
 		return listWidget;
 	}
 	
 	public List<LI> getList() {
 		return asEditor().getList();
 	}
 }
