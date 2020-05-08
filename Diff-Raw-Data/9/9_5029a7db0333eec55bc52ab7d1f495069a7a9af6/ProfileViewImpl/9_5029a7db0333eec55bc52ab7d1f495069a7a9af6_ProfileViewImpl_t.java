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
 
 import org.eastway.echarts.client.common.ColumnDefinition;
 import org.eastway.echarts.style.client.GlobalResources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ProfileViewImpl<T> extends Composite implements ProfileView<T> {
 
 	private static ProfileViewUiBinder uiBinder = GWT.create(ProfileViewUiBinder.class);
 
 	@UiTemplate("ProfileView.ui.xml")
 	interface ProfileViewUiBinder extends UiBinder<Widget, ProfileViewImpl> { }
 
 	@UiField FlexTable table;
 	private List<ColumnDefinition<T>> columnDefinitions;
 	private Presenter<T> presenter;
 	private T rowData;
 
 	public ProfileViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 		table.addStyleName(GlobalResources.styles().table());
 	}
 
 	@Override
 	public void setColumnDefinitions(List<ColumnDefinition<T>> columnDefinitions) {
 		this.columnDefinitions = columnDefinitions;
 	}
 
 	@Override
 	public void setPresenter(Presenter<T> presenter) {
 		this.presenter = presenter;
 	}
 
 	@Override
 	public Widget asWidget() {
 		return this;
 	}
 
 	@Override
 	public void setRowData(T rowData) {
 		this.rowData = rowData;
 		table.clear();
 		for (int i = 0; i < columnDefinitions.size(); i++) {
 			table.setHTML(i, 0, columnDefinitions.get(i).getHeader(rowData));
 			ColumnDefinition<T> data = columnDefinitions.get(i);
 			if (data.isList()) {
 				ListBox listBox = new ListBox();
 				for (int j = 0; j < data.getList(rowData).size(); j++) {
 					String s = data.getList(rowData).get(j);
 					listBox.addItem(s);
 					if (s.matches(data.getData(rowData)))
 						listBox.setSelectedIndex(j);
 				}
 				table.setWidget(i, 1, listBox);
 			} else if (data.isEditable()) {
 				TextBox textBox = new TextBox();
 				textBox.setValue(data.getData(rowData));
 				table.setWidget(i, 1, textBox);
 			} else {
 				StringBuilder sb = new StringBuilder();
 				data.render(rowData, sb);
 				table.setHTML(i, 1, sb.toString());
 			}
 		}
 	}
 
 	@UiHandler("save")
 	public void handleSave(ClickEvent event) {
 		for (int i = 0; i < columnDefinitions.size(); i++) {
 			try {
 				if (columnDefinitions.get(i).isList()) {
 					int selected = ((ListBox) table.getWidget(i, 1)).getSelectedIndex();
 					columnDefinitions.get(i).setData(rowData, ((ListBox) table.getWidget(i, 1)).getItemText(selected));
				} else if (columnDefinitions.get(i).isEditable()) {
					columnDefinitions.get(i).setData(rowData, ((TextBox) table.getWidget(i, 1)).getValue());
 				} else {
					columnDefinitions.get(i).setData(rowData, table.getText(i, 1));
 				}
 			} catch (NullPointerException e) {
 				columnDefinitions.get(i).setData(rowData, "");
 			}
 		}
 		presenter.save(rowData);
 	}
 }
