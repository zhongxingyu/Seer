 package com.orange.groupbuy.web.client.component;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.uibinder.client.UiConstructor;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.LayoutPanel;
 
 public class CollpaseBox extends Composite {
 
	// private static CollpaseBoxUiBinder uiBinder = GWT
	// .create(CollpaseBoxUiBinder.class);
	//
	// interface CollpaseBoxUiBinder extends UiBinder<Widget, CollpaseBox> {
	// }

	// @UiField
 	Label name;
 
	// @UiField
 	LayoutPanel content;
 
 	@UiConstructor
 	public CollpaseBox(int size) {
 		DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
 		 name = new Label();
 		dockPanel.addNorth(name, 2);
 
 		content = new LayoutPanel();
 		dockPanel.addSouth(content, size);
 		initWidget(dockPanel);
		// initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	public Label getName() {
 		return name;
 	}
 
 	public LayoutPanel getContent() {
 		return content;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> CellTable<T> getContentCellTable() {
 		return (CellTable<T>) content.getWidget(0);
 	}
 }
