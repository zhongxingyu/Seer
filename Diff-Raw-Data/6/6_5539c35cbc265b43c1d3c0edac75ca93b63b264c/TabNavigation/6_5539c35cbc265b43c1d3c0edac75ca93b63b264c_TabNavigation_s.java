 package com.data2semantics.mockup.client.view.patientinfo;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.data2semantics.mockup.client.view.MockupInterfaceView;
 import com.data2semantics.mockup.client.view.patientinfo.tabs.GuidelineAnnotations;
 import com.data2semantics.mockup.client.view.patientinfo.tabs.RelevantLiterature;
 import com.data2semantics.mockup.client.view.patientinfo.tabs.WidgetsContainer;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class TabNavigation extends TabPanel {
 	private MockupInterfaceView view;
 	private int currentTabId = 0;
 	
 	public TabNavigation(MockupInterfaceView view, int patientId) {
 		this.view = view;
 		setWidth(PatientInfo.RHS_WIDTH);
 		addSelectionHandler(new SelectionHandler<Integer>() {
 			public void onSelection(SelectionEvent<Integer> event) {
 				currentTabId = event.getSelectedItem();
 			}
 		});
 		//fill first tab
 		add(new WidgetsContainer(view, patientId),  "Overview");
 		selectTab(0);
 	}
 
 
 	public MockupInterfaceView getView() {
 		return view;
 	}
 	
 	/**
 	 * Add a tab to the TabPanel. Stores which tab is currently selected, and removes any necessary tabs
 	 * 
 	 * @param widget Widget to add
 	 * @param title Title of the tab
 	 */
 	private void addTab(Widget widget, String title) {
		if ((currentTabId + 1) < getWidgetCount()) {
 			//We want to add a tab, but there are already other following tabs. 
 			//Remove these
			for (int i = currentTabId; i < getWidgetCount(); i++) {
 				if (getWidget(i) != null) {
 					remove(i);
 				}
 			}
 		}
 		
 		add(widget, title);
 		int addedIndex = getWidgetIndex(widget);
 		selectTab(addedIndex);
 		currentTabId = addedIndex;
 	}
 	
 	public void addGuidelineAnnotations() {
 		addTab(new GuidelineAnnotations(getView()), "Guideline Annotations");
 	}
 	public void addRelevantLiterature() {
 		addTab(new RelevantLiterature(getView()), "Relevant Literature");
 	}
 }
