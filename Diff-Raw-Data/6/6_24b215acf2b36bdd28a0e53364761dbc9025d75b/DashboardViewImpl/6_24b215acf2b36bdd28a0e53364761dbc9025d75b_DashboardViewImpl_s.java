 /*
  * Copyright 2010 Ian Hilt
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.eastway.echarts.client.view;
 
 import java.util.List;
 
 import com.google.gwt.app.client.NotificationMole;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.dom.client.TableCellElement;
 import com.google.gwt.dom.client.TableElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiFactory;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class DashboardViewImpl<T> extends Composite implements DashboardView<T> {
 	@SuppressWarnings("unchecked")
 	@UiTemplate("Dashboard.ui.xml")
 	interface DashboardViewUiBinder extends UiBinder<Widget, DashboardViewImpl> { }
 
 	private static DashboardViewUiBinder uiBinder = GWT.create(DashboardViewUiBinder.class);
 
 	@UiField Anchor logoutButton;
 	@UiField Style style;
 	@UiField FlexTable currentPatientData;
 	@UiField Button patientSearchButton;
 	@UiField SuggestBox patientIdBox;
 	@UiField NotificationMole mole;
 	@UiField SpanElement productivity;
 	@UiField SpanElement bonusProjection;
 	@UiField TableCellElement graphColor;
 	@UiField TableElement graph;
 	@UiField Hyperlink tickler;
 
 	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
 	private double productivityUnit = 0.83;
 
 	public DashboardViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 		bind();
 	}
 
 	private Presenter<T> presenter;
 
 	@Override
 	public Widget asWidget() {
 		return this;
 	}
 
 	@Override
 	public void setPresenter(Presenter<T> presenter) {
 		this.presenter = presenter;
 	}
 
 	@UiField TabLayoutPanel tabLayoutPanel;
 
 	@Override
 	public void addTab(final Widget widget, String string) {
 		Label closeTab = new Label();
 		HorizontalPanel tabHeader = new HorizontalPanel();
 		closeTab.setTitle("Close");
 		closeTab.addStyleName("close-Tab");
 		closeTab.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				Integer idx = tabLayoutPanel.getWidgetIndex(widget);
 				if (tabLayoutPanel.remove(idx))
 					tabLayoutPanel.selectTab(idx - 1);
 			}
 		});
 		tabHeader.add(new Label(string));
 		tabHeader.add(closeTab);
 		tabLayoutPanel.add(widget, tabHeader);
 		setSelectedTab(tabLayoutPanel.getWidgetIndex(widget));
 	}
 
 	@Override
 	public void setSelectedTab(int idx) {
 		tabLayoutPanel.selectTab(idx);
 	}
 
 	@UiFactory SuggestBox initSuggestBox() {
 		return new SuggestBox(oracle);
 	}
 
 	@Override
 	public void setPatientSearchData(List<String> list) {
 		for (String s : list)
 			oracle.add(s);
 	}
 
 	@Override
 	public void addPatientSearchData(String str) {
 		oracle.add(str);
 	}
 
 	@Override
 	public void showEhrStub(boolean visible) {
 		currentPatientData.setVisible(visible);
 	}
 
 	private void bind() {
 		tabLayoutPanel.addSelectionHandler(new SelectionHandler<Integer>() {
 			@Override
 			public void onSelection(SelectionEvent<Integer> event) {
 				Widget w = tabLayoutPanel.getWidget(event.getSelectedItem());
 				if (w instanceof EHRViewImpl<?>) {
 					presenter.changeCurrentEhr(((EHRViewImpl<?>) w).getPresenter().getEhr());
 				} else {
 					presenter.changeCurrentEhr(null);
 				}
 			}
 		});
 	}
 
 	@UiHandler("patientSearchButton")
 	void handleSearchButtonClicked(ClickEvent event) {
 		String str = patientIdBox.getText();
 		if (str.isEmpty())
 			return;
 		presenter.openEhr(str);
 		patientIdBox.setText("");
 	}
 
 	@Override
 	public NotificationMole getMole() {
 		return mole;
 	}
 
 	@Override
 	public void reset() {
 		oracle.clear();
 	}
 
 	@Override
 	public void setBonusProjection(String greenNumber) {
 		bonusProjection.setInnerText(greenNumber);
 	}
 
 	@Override
 	public void setProductivity(String productivity, String color) {
 		this.productivity.setInnerText(productivity);
 		if (color.equals("red"))
 			graph.addClassName(style.red());
 		else if (color.equals("yellow"))
 			graph.addClassName(style.yellow());
 		else
 			graph.addClassName(style.green());
		graph.setWidth(new Double(new Double(productivity) * productivityUnit).toString() + "%");
 	}
 
 	@UiHandler(value="tickler")
 	public void onTicklerClicked(ClickEvent event) {
 		presenter.openTickler();
 	}
 
 	@Override
 	public void setName(String name) {
 		currentPatientData.setText(0, 0, "Name: " + name);
 		currentPatientData.getFlexCellFormatter().setColSpan(0, 0, 2);
 	}
 
 	@Override
 	public void setCaseStatus(String descriptor) {
 		currentPatientData.setText(2, 1, "Case Status: " + descriptor);
 	}
 
 	@Override
 	public void setAge(String age) {
 		currentPatientData.setText(1, 1, "Age: " + age);
 	}
 
 	@Override
 	public void setDob(String dob) {
 		currentPatientData.setText(1, 0, "DOB: " + dob);
 	}
 
 	@Override
 	public void setProvider(String provider) {
 		currentPatientData.setText(2, 0, "Provider: " + provider);
 	}
 
 	@Override
 	public void setSsn(String ssn) {
 		currentPatientData.setText(1, 1, "SSN: " + ssn);
 	}
 
 	@UiHandler("logoutButton")
 	public void handleLogoutButton(ClickEvent event) {
 		presenter.logout();
 	}
 }
