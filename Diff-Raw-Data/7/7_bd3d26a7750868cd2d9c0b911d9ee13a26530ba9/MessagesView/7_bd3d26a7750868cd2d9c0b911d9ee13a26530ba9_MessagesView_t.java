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
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.eastway.echarts.client.presenter.MessagesPresenter;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.Widget;
 
 public class MessagesView extends Composite implements MessagesPresenter.Display {
 	private static MessagesViewUiBinder uiBinder = GWT.create(MessagesViewUiBinder.class);
 
 	interface MessagesViewUiBinder extends
 			UiBinder<Widget, MessagesView> {}
 
 	@UiField FlowPanel messages;
 	@UiField Button add;
 	@UiField DialogBox db;
 	@UiField Button saveButton, closeButton;
 	@UiField ListBox messageTypes;
 	@UiField RichTextArea message;
 
 	public MessagesView() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@Override
 	public Widget asWidget() {
 		return this;
 	}
 
 	@Override
 	public void setData(ArrayList<String[]> data) {
 		messages.clear();
 		if (data == null)
 			messages.add(new HTML("No messages"));
 		else
 			for (String[] s : data)
 				messages.add(formatMessage(s));
 	}
 
 	public HTML formatMessage(String[] m) {
 		HTML message = new HTML(m[3]);
 		return new HTML("<strong>"
 				+ DateTimeFormat
 					.getFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
 					.format(new Date(new Long(m[0])))
 				+ "</strong>&mdash;"
 				+ m[1] + "&mdash;"
 				+ m[2] + "<div class='home-PatientMessage'>"
 				+ message + "</div>");
 	}
 
 	@Override
 	public HasClickHandlers getAddButton() {
 		return add;
 	}
 
 	@Override
 	public void close() {
		db.setVisible(false);
 	}
 
 	@Override
 	public HasClickHandlers getCloseButton() {
 		return closeButton;
 	}
 
 	@Override
 	public String getMessage() {
 		return message.getHTML();
 	}
 
 	@Override
 	public String getMessageType() {
 		int idx = messageTypes.getSelectedIndex();
 		return messageTypes.getItemText(idx);
 	}
 
 	@Override
 	public HasClickHandlers getSaveButton() {
 		return saveButton;
 	}
 
 	@Override
 	public void saved() {
 		com.google.gwt.user.client.Window.alert("Message saved");
		close();
 	}
 
 	@Override
 	public void setMessageTypes(ArrayList<String> types) {
 		messageTypes.clear();
 		if (types == null)
 			return;
 		for (String s : types)
 			messageTypes.addItem(s);
 	}
 
 	@Override
 	public void setText(String patientId) {
 		db.setText("Message for " + patientId);
 	}
 
 	@Override
 	public void show() {
 		db.setVisible(true);
 		db.center();
 	}
 }
