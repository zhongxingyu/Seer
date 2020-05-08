 package org.vaadin.smartgwt.client.ui.grid;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.vaadin.smartgwt.client.ui.tree.VTreeGrid;
 
 import com.google.gwt.core.client.GWT;
 import com.smartgwt.client.data.Record;
 import com.smartgwt.client.types.ListGridEditEvent;
 import com.smartgwt.client.types.Overflow;
 import com.smartgwt.client.types.SelectionStyle;
 import com.smartgwt.client.util.JSOHelper;
 import com.smartgwt.client.util.JSON;
 import com.smartgwt.client.widgets.form.fields.FormItem;
 import com.smartgwt.client.widgets.form.fields.SelectItem;
 import com.smartgwt.client.widgets.form.fields.TextItem;
 import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
 import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
 import com.smartgwt.client.widgets.grid.CellFormatter;
 import com.smartgwt.client.widgets.grid.HoverCustomizer;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.grid.events.CellClickEvent;
 import com.smartgwt.client.widgets.grid.events.CellClickHandler;
 import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
 import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
 import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
 import com.smartgwt.client.widgets.grid.events.SelectionEvent;
 import com.smartgwt.client.widgets.tree.Tree;
 import com.smartgwt.client.widgets.tree.TreeGridField;
 import com.smartgwt.client.widgets.tree.TreeNode;
 
 public class VPropertyGrid extends VTreeGrid
 {
 	/**
 	 * addSelectionChangedHandler fires several times for the same record and we don't want to set the editor repeatedly for the same record, this tracks the
 	 * last record that has the editor set up
 	 */
 	protected Record lastEditorChangedRecord;
 	protected Record selectedRecord;
 	private SelectItem selectItem;
 
 	private FormItem getSpecificEditor(Record record)
 	{
 		selectItem = null;
 		
 		if (record.getAttributeAsBoolean("readOnly"))
 		{
 			getField(1).setCanEdit(false);
 			return null;
 		}
 
 		if (record.getAttribute("type").equalsIgnoreCase("boolean"))
 		{
 			getField(1).setCanEdit(false);
 		}
 		else if (record.getAttribute("type").equalsIgnoreCase("enum"))
 		{
 			getField(1).setCanEdit(true);
 			selectItem = new SelectItem();
 			prepareComboBox(selectItem, record.getAttributeAsString("selections"));
 			return selectItem;
 		}
 		else
 		{
 			TextItem textItem = new TextItem();
 			getField(1).setCanEdit(true);
 			return textItem;
 		}
 		return null;
 	}
 
 	/**
 	 * Create a new {@link VPropertyGrid} with two column fields (first for names, second for values)
 	 */
 	public VPropertyGrid()
 	{
 		// some property grid default values
 		setShowAllRecords(true);
 		setBodyOverflow(Overflow.VISIBLE);
 		setOverflow(Overflow.VISIBLE);
 		setLeaveScrollbarGap(false);
 		setSelectionType(SelectionStyle.SINGLE);
 		setEditEvent(ListGridEditEvent.CLICK);
 		setShowHeaderContextMenu(false);
 		setCanResizeFields(true);
 		setCanReorderFields(false);
 		setEditByCell(true);
 		setShowHeader(true);
 		setAutoSaveEdits(true);
 		setHoverWidth(200);
 
 		createFields();
 
 		addEditCompleteHandler(new EditCompleteHandler()
 			{
 				@Override
 				public void onEditComplete(EditCompleteEvent event)
 				{
 					updateData(event.getOldRecord(), event.getNewValues());
 				}
 			});
 
 		// setBaseStyle("myBoxedGridCell");
 
 		addCellClickHandler(new CellClickHandler()
 			{
 				@Override
 				public void onCellClick(CellClickEvent event)
 				{
 					if (event.getColNum() == 1 && event.getRecord().getAttribute("type").equalsIgnoreCase("boolean"))
 					{
 						boolean val = Boolean.parseBoolean(event.getRecord().getAttributeAsString("value"));
 						Map map = new HashMap();
 						map.put("value", !val);
 						updateData(event.getRecord(), map);
 					}
 				}
 			});
 		
 		addSelectionChangedHandler(new SelectionChangedHandler()
 		{
 			public void onSelectionChanged(SelectionEvent event)
 			{
 				Record record = event.getRecord();
 				selectedRecord = record;
 
 				if (event.getState() && !record.equals(lastEditorChangedRecord))
 				{
 					lastEditorChangedRecord = record;
 					
 					FormItem editor = getSpecificEditor(record);
 					
 					if (editor != null)
 					{
 						getField(1).setEditorType(editor);
 					}
 
 					if (selectItem != null)
 					{
 						selectItem.showPicker();
 					}
 				}
 			}
 		});
 		
 	}
 
 	public void updateData(Record updatedRecord, Map<?, ?> changes)
 	{
 		Object value = null;
 
 		if (changes.get("value") != null)
 		{
 			client.updateVariable(pid, "id", updatedRecord.getAttributeAsString("id"), false);
 			client.updateVariable(pid, "value", changes.get("value").toString(), true);
 		}
 	}
 
 	private void createFields()
 	{
 		TreeGridField nameField = new TreeGridField("name");
 		nameField.setCanEdit(false);
 		nameField.setCanSort(false);
 		nameField.setCanFilter(false);
 		nameField.setCanGroupBy(false);
 		nameField.setCanFreeze(false);
 
 //		nameField.setHoverCustomizer(new HoverCustomizer()
 //			{
 //				public String hoverHTML(Object value, ListGridRecord record, int rowNum, int colNum)
 //				{
 //					return record.getAttributeAsString("binding");
 //				}
 //			});
 //		nameField.setShowHover(true);
 
 		TreeGridField valueField = new TreeGridField("value");
 		valueField.setCellFormatter(new CellFormatter()
 			{
 				@Override
 				public String format(Object value, ListGridRecord record, int rowNum, int colNum)
 				{
 					if (colNum == 1)
 					{
 						if (record.getAttribute("type").equalsIgnoreCase("boolean"))
 						{
 							value = Boolean.parseBoolean((String) value);
 							if ((Boolean) value == true)
							return "<img src=\"" + GWT.getHostPageBaseURL() + "img/checked.png\" />";
 							else
							return "<img src=\"" + GWT.getHostPageBaseURL() + "img/unchecked.png\" />";
 						}
 						else if (record.getAttribute("type").equalsIgnoreCase("enum"))
 						{
 							if (record.getAttribute("imagePath") != null && record.getAttribute("imagePath").length() > 0)
 							{
 								return "<img height=16 align=top width=24 src=\"" + GWT.getHostPageBaseURL() + "imageFetcher?type=ressource&name="
 										+ record.getAttribute("imagePath") + "\"/> " + value;
 							}
 						}
 
 						String path = GWT.getHostPageBaseURL();
 						Integer severity = record.getAttributeAsInt("severity");
 
 						if (severity == null)
 							severity = 0;
 
 						if (severity == 2)
 							return "<img src=\"" + path + "img/showerr_tsk.gif\" align=\"top\"/> " + (value == null ? "" : value);
 						else if (severity == 1)
 							return "<img src=\"" + path + "img/showwarn_tsk.gif\" align=\"top\"/> " + (value == null ? "" : value);
 					}
 
 					return value == null ? "" : value.toString();
 				}
 			});
 
 		valueField.setHoverCustomizer(new HoverCustomizer()
 			{
 				public String hoverHTML(Object value, ListGridRecord record, int rowNum, int colNum)
 				{
 					if (record.getAttribute("messages") != null)
 					{
 						List liste = JSOHelper.convertToList(JSON.decode(record.getAttribute("messages")));
 
 						String html = "";
 
 						for (Object o : liste)
 						{
 							Map map = (Map) o;
 
 							String current = map.get("message").toString();
 							if (current != null)
 							{
 								if (html.length() > 0)
 									html = html + "<br>";
 								html = html + current;
 							}
 						}
 
 						return html;
 					}
 					else
 						return null;
 				}
 			});
 		valueField.setShowHover(true);
 
 		valueField.setCanEdit(true);
 		valueField.setCanSort(false);
 		valueField.setCanFilter(false);
 		valueField.setCanGroupBy(false);
 		valueField.setCanFreeze(false);
 
 		TreeGridField priceField = new TreeGridField("price");
 		priceField.setCanEdit(false);
 		priceField.setCanSort(false);
 		priceField.setCanFilter(false);
 		priceField.setCanGroupBy(false);
 		priceField.setCanFreeze(false);
 		priceField.setWidth("60");
 
 		setFields(nameField, valueField, priceField);
 	}
 
 	@Override
 	public void setData(Tree data)
 	{
 		super.setData(data);
 		refresh();
 	}
 
 	@Override
 	protected String getCellCSSText(ListGridRecord record, int rowNum, int colNum)
 	{
 		Tree tree = getTree();
 		TreeNode node = tree.getAllNodes()[rowNum];
 		TreeNode parent = tree.getParent(node);
 		
 		String css = "";
 		try
 		{
 			if (colNum == 1 && record.getAttributeAsBoolean("overridden"))
 			{
 				css += "color:#FF00FF; ";
 			}
 
 			if (node != null && tree.isFolder(node) && parent.getAttribute("id") == null)
 			{
 				css += "font-weight:bold; background-color:#C0C0C0; ";
 			}
 			else
 			{
 				if (record.getAttributeAsBoolean("readOnly"))
 					css += "color:#C0C0C0; ";
 
 				css += "border-bottom:1px solid #F0F0F0; border-right:1px solid #F0F0F0; ";
 			}
 			
 			if (css.length() > 0)
 				return css;
 			else
 				return super.getCellCSSText(record, rowNum, colNum);
 
 		}
 		catch (Exception e)
 		{
 			GWT.log("exception", e);
 		}
 		return super.getCellCSSText(record, rowNum, colNum);
 	}
 
 	private void openFolders(TreeNode path)
 	{
 		Tree tree = getData();
 
 		for (TreeNode record : tree.getFolders(path))
 		{
 			if (record.getAttributeAsBoolean("expanded"))
 			{
 				tree.openFolder((TreeNode) record);
 				openFolders(record);
 			}
 			else
 			{
 				tree.closeFolder((TreeNode) record);
 			}
 		}
 	}
 
 	public void refresh()
 	{
 		openFolders(getData().getRoot());
 	}
 
 	protected String getIcon(Record node, boolean defaultState)
 	{
 		return null;
 	}
 
 	/**
 	 * The name column field
 	 * 
 	 * @return
 	 */
 	public ListGridField getNameField()
 	{
 		return getField("name");
 	}
 
 	/**
 	 * The values column field
 	 * 
 	 * @return
 	 */
 	public ListGridField getValuesField()
 	{
 		return getField("value");
 	}
 
 	private void prepareComboBox(SelectItem item, String selectionsString)
 	{
 		if (selectionsString == null)
 		{
 			return;
 		}
 
 		LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
 		LinkedHashMap<String, String> imgMap = new LinkedHashMap<String, String>();
 
 		List liste = JSOHelper.convertToList(JSON.decode(selectionsString));
 
 		for (Object o : liste)
 		{
 			Map map = (Map) o;
 
 			if (map.containsKey("label"))
 			{
 				String label = map.get("label").toString();
 				valueMap.put(label, label);
 
 				if (map.containsKey("imagePath"))
 				{
 					String imagePath = map.get("imagePath").toString();
 					if (imagePath != null && imagePath.length() > 0)
 					{
 						imgMap.put(label, GWT.getHostPageBaseURL() + "imageFetcher?type=ressource&name=" + imagePath);
 					}
 				}
 			}
 		}
 
 		item.setValueMap(valueMap);
 		item.setValueIcons(imgMap);
 
 		item.addChangedHandler(new ChangedHandler()
 			{
 				@Override
 				public void onChanged(ChangedEvent event)
 				{
 					saveAllEdits();
 				}
 			});
 
 	}
 
 }
