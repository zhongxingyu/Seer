 package org.iucn.sis.client.panels.definitions;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.iucn.sis.client.api.caches.DefinitionCache;
 
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.VerticalAlignment;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MessageBoxEvent;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public class DefinitionEditorPanel extends ContentPanel {
 
 	protected String defColumnWidth = "150px";
 	protected String definitionsColumnWidth = "400px";
 
 //	private final LayoutContainer definitionsPanel;
 	private final Map<TextField<String>, TextArea> fields;
 	private boolean drawn = false;
 
 	public DefinitionEditorPanel() {
 //		definitionsPanel = new LayoutContainer();
 		fields = new HashMap<TextField<String>, TextArea>();
 		setScrollMode(Scroll.AUTO);
 		setHeaderVisible(false);
 		
		TableLayout defLayout = new TableLayout(3);
		defLayout.setRenderHidden(true);
		setLayout(defLayout);
 	}
 
 	private TextField<String> addDefinition(String definable, String definition) {
 		TableData deleteColumn = new TableData();
 		deleteColumn.setWidth("20px");
 		deleteColumn.setVerticalAlign(VerticalAlignment.TOP);
 
 		TableData defColumn = new TableData();
 		defColumn.setWidth(defColumnWidth);
 		defColumn.setVerticalAlign(VerticalAlignment.TOP);
 
 		TableData definitionsColumn = new TableData();
 		definitionsColumn.setWidth(definitionsColumnWidth);
 		definitionsColumn.setVerticalAlign(VerticalAlignment.TOP);
 
 		final TextField<String> defText = new TextField<String>();
 		defText.setValue(definable);
 		defText.setWidth(defColumnWidth);
 
 		final TextArea definitionText = new TextArea();
 		definitionText.setValue(definition);
 		definitionText.setWidth(definitionsColumnWidth);
 
 		fields.put(defText, definitionText);
 		final Image image = new Image("images/icon-delete.png");
 		image.addClickListener(new ClickListener() {
 
 			public void onClick(Widget sender) {
 				WindowUtils.confirmAlert("Delete?", "Are you sure you want to delete this definition?", new Listener<MessageBoxEvent>() {
 					public void handleEvent(MessageBoxEvent be) {
 						if( be.getButtonClicked().getText().equalsIgnoreCase("yes") ) {
 							remove(defText);
 							remove(definitionText);
 							remove(image);
 							fields.remove(defText);
 						}
 					};
 				});
 			}
 		});
 
 		add(image, deleteColumn);
 		add(defText, defColumn);
 		add(definitionText, definitionsColumn);
 		
 		return defText;
 	}
 
 	public void draw() {
 
 		if (!drawn) {
 			for (String definable : DefinitionCache.impl.getDefinables())
 				addDefinition(definable, DefinitionCache.impl.getDefinition(definable));
 
 
 			Button save = new Button();
 			save.setText("Save");
 			save.setIconStyle("icon-save");
 			save.setTitle("Save");
 			save.addListener(Events.Select, new Listener<BaseEvent>() {
 				public void handleEvent(BaseEvent be) {
 					save();
 				}
 			});
 
 			Button add = new Button();
 			add.setText("Add new definition");
 			add.setIconStyle("icon-add");
 			add.setTitle("Add new definition");
 			add.addListener(Events.Select, new Listener<BaseEvent>() {
 				public void handleEvent(BaseEvent be) {
 					TextField<String> f = addDefinition("", "");
 					layout();
 					
 					scrollIntoView(f);
 				}
 			});
 
 			ToolBar toolbar = new ToolBar();
 			toolbar.add(save);
 			toolbar.add(add);
 			setTopComponent(toolbar);
 			
 			drawn = true;
 		}
 	}
 
 	protected boolean isSaveable() {
 
 		List<String> strings = new ArrayList<String>();
 		for (Entry<TextField<String>, TextArea> entry : fields.entrySet()) {
 			if (entry.getKey() != null) {
 				if (strings.contains(entry.getKey().getValue().toLowerCase())) {
 					Window
 							.alert("Unable to save as there are multiple entries for "
 									+ entry.getKey().getValue());
 					return false;
 				} else if (entry.getKey().getValue().trim()
 						.equalsIgnoreCase("")) {
 					Window
 							.alert("Unable to save as there are empty definitions");
 					return false;
 				} else {
 					strings.add(entry.getKey().getValue().toLowerCase());
 				}
 			}
 
 		}
 		return true;
 
 	}
 
 	protected void save() {
 		if (isSaveable()) {
 
 			Map<String, String> definitionsMap = new HashMap<String, String>();
 			for (Entry<TextField<String>, TextArea> entry : fields.entrySet()) {
 				definitionsMap.put(entry.getKey().getValue().toLowerCase(), entry.getValue()
 						.getValue());
 			}
 			DefinitionCache.impl.saveDefinitions(definitionsMap,
 					new GenericCallback<String>() {
 
 						public void onFailure(Throwable caught) {
 							WindowUtils
 									.errorAlert("Failure saving definitions.");
 						}
 
 						public void onSuccess(String result) {
 							Info.display("", "Saved definitions");
 
 						}
 					});
 		}
 	}
 
 }
