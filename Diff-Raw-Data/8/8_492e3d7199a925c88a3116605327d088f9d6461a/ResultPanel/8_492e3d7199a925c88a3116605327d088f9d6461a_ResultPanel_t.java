 package de.kuei.metafora.client;
 
 import java.util.HashMap;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Element;
 import com.google.gwt.xml.client.XMLParser;
 
 public class ResultPanel extends LayoutPanel {
 
 	private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE = "demo-InsertPanelExample";
 	private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_COLUMN_COMPOSITE = "demo-InsertPanelExample-column-composite";
 	private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_CONTAINER = "demo-InsertPanelExample-container";
 	private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_HEADING = "demo-InsertPanelExample-heading";
 //	private static final String CSS_DEMO_INSERT_PANEL_EXAMPLE_WIDGET = "demo-InsertPanelExample-widget";
 	private static final String CSS_RESULT_PANEL = "result-Panel";
 	private static final int SPACING = 0;
 	
 	HashMap<String, VerticalPanel> categoriesMap;
 	
 	public ResultPanel() {
 		super();
     addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE);
 
 		categoriesMap = new HashMap<String, VerticalPanel>();
 		setSize("100%", "100%");
 		HTML topic = new HTML("<h2>Save Visual Language</h2>");
 		topic.setSize("100%", "100%");
 		add(topic);
     setWidgetTopHeight(topic, 0, Unit.PCT, 10, Unit.PCT);
 
 		final LayoutPanel categories = new LayoutPanel();
 		categories.addStyleName(CSS_RESULT_PANEL);
 		categories.setSize("100%", "100%");
 //		categories.setSpacing(SPACING);
 		for (String name : CAVilLag.instance.getCategories()) {
 			LayoutPanel selectedColumnCompositePanel = new LayoutPanel();
 			selectedColumnCompositePanel.setSize("100%", "100%");
 			selectedColumnCompositePanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_COLUMN_COMPOSITE);
 			VerticalPanel selectedCardsPanel = new VerticalPanel();
 			selectedCardsPanel.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_CONTAINER);
 			selectedCardsPanel.setSize("100%", "100%");
 			selectedCardsPanel.setSpacing(SPACING);
 			Label selectedHeading = new Label(name);
 			selectedHeading.addStyleName(CSS_DEMO_INSERT_PANEL_EXAMPLE_HEADING);
 			selectedColumnCompositePanel.add(selectedHeading);
 			selectedColumnCompositePanel.setWidgetTopHeight(selectedHeading, 0, Unit.PCT, 10, Unit.PCT);
 			ScrollPanel selectedScroller = new ScrollPanel();
 			selectedScroller.setSize("100%", "100%");
 //			ScrollPanel selectedScroller = new ScrollPanel() {
 //				
 //				@Override
 //				public void onResize() {
 //					super.onResize();
 //					setHeight(categories.getOffsetHeight()-80+"px");
 //				}
 //				
 //			};
 //			selectedScroller.setHeight("400px");
 			selectedScroller.add(selectedCardsPanel);
 			LayoutPanel auxPanel = new LayoutPanel();
 			auxPanel.setSize("100%", "100%");
 			auxPanel.add(selectedScroller);
 			auxPanel.setWidgetTopBottom(selectedScroller, 5, Unit.PCT, 5, Unit.PCT);
 			selectedColumnCompositePanel.add(auxPanel);
 			selectedColumnCompositePanel.setWidgetTopHeight(auxPanel, 10, Unit.PCT, 90, Unit.PCT);
 			int cpCount = categories.getWidgetCount();
 			categories.add(selectedColumnCompositePanel);
 			categories.setWidgetLeftWidth(selectedColumnCompositePanel, cpCount*12, Unit.PCT, 10, Unit.PCT);
 			categoriesMap.put(name, selectedCardsPanel);
 		}
 		add(categories);
     setWidgetTopHeight(categories, 10, Unit.PCT, 80, Unit.PCT);
 		
 		Button saveButton = new Button("save", new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				CAVilLag.instance.saveTemplate();
				CAVilLag.instance.updateChallengeTab();
 			}
 		});
 		
 		saveButton.setSize("60px", "40px");
 
 		FlowPanel fp = new FlowPanel();
 		fp.add(saveButton);
     add(fp);
     setWidgetTopHeight(fp, 90, Unit.PCT, 10, Unit.PCT);
     		
 	}
 	
 	protected void refreshView() {
 		for (String name : categoriesMap.keySet()) {
 			VerticalPanel panel = categoriesMap.get(name);
 			panel.clear();
 			for (String[] node : CAVilLag.instance.getSelectedNodes(name)) {
 				Card n = new Card(node[0], node[3], node[2], node[5]);
 				panel.add(n);
 			}
 		}
 	}
 	
 	protected Document getXml() {
 		Document doc = XMLParser.createDocument();
 		doc.appendChild(doc.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\""));
 		
 		Element cardNode;
     Element categoryNode;
     Element root = doc.createElement("template");
     root.setAttribute("time", Long.toString(System.currentTimeMillis()));
     doc.appendChild(root);
     
 		for (String name : categoriesMap.keySet()) {
 			categoryNode = doc.createElement("category");
 			categoryNode.setAttribute("id", CAVilLag.instance.getCategoryID(name));
 			root.appendChild(categoryNode);
 			for (String[] node : CAVilLag.instance.getSelectedNodes(name)) {
 				cardNode = doc.createElement("card");
 				cardNode.setAttribute("id", node[5]);
 				categoryNode.appendChild(cardNode);
 			}
 		}
     return doc;
 		
 	}
 
 }
