 package com.tda.presentation.client.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.smartgwt.client.types.Side;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.layout.VLayout;
 import com.smartgwt.client.widgets.tab.Tab;
 import com.smartgwt.client.widgets.tab.TabSet;
 import com.tda.presentation.client.presenter.AdminHomePresenter;
 import com.tda.presentation.client.presenter.Presenter;
 
 public class AdminHomeView extends Composite implements AdminHomePresenter.Display {
 
 	@UiTemplate("AdminHomeView.ui.xml")
 	interface AdminHomeViewUiBinder extends UiBinder<Widget, AdminHomeView> {
 	}
 
 	private static AdminHomeViewUiBinder uiBinder = GWT.create(AdminHomeViewUiBinder.class);
 
 	@UiField
 	VerticalPanel tabsContainer;
 
 	@UiField
 	VerticalPanel mainContainer;
 
 	private TabSet topTabSet;
 	private List<Tab> tabs;  
	private VLayout layout;
 	
 	public AdminHomeView() {
 		initWidget(uiBinder.createAndBindUi(this));
 		tabs = new ArrayList<Tab>();
 		createTabs();
 //		tabsContainer.add(layout);
//		layout.draw();
 	}
 
 	private void createTabs() {
		layout = new VLayout();
		layout.setWidth100();
		layout.setHeight100();
		
 		topTabSet = new TabSet();  
         topTabSet.setTabBarPosition(Side.TOP);
         topTabSet.setWidth100();
         topTabSet.setHeight100();
         
 		Tab userTab = new Tab("Usuarios");
 		Tab itemsTab = new Tab("Items");
 		
 		Canvas c = new Canvas("c1");
 		c.setWidth100();
 		c.setHeight100();
 		c.addChild(new Label("tab 1"));
 		userTab.setPane(c);
 		
 		Canvas d = new Canvas("c2");
 		d.setWidth100();
 		d.setHeight100();
		d.addChild(new Label("tab 2"));
 		itemsTab.setPane(d);
 		
 		tabs.add(userTab);
 		tabs.add(itemsTab);
 
		topTabSet.addTab(userTab);
		topTabSet.addTab(itemsTab);
		
		layout.addMember(topTabSet);
 	}
 
 	public Widget asWidget() {
 		return this;
 	}
 
 	public Panel getTabsContainer() {
 		return tabsContainer;
 	}
 
 	public Panel getMainContainer() {
 		return mainContainer;
 	}
 
 	public Tab getTab(int index) {
 		return tabs.get(index);
 	}
 	
 	public Canvas getTabCanvas(int index){
 		return tabs.get(index).getPane();
 	}
 
 	public void draw() {
 		topTabSet.draw();
 	}
 
 }
