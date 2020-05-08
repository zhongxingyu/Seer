 /*
  *  Copyright 2008 University of Prince Edward Island
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package ca.upei.ic.timetable.client;
 
 import java.util.Date;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SemesterModelView implements Model, View {
 	
 	private FindCourseViewController controller_;
 	private VerticalPanel panel_;
 	
 	public SemesterModelView(FindCourseViewController controller) {
 		controller_ = controller;
 		panel_ = GWT.create(VerticalPanel.class);
 		panel_.setSpacing(3);
 	}
 	
 	public void loadJSON(JSONValue value) {
 		JSONArray array = (JSONArray) value;
 		
 		// get the current month
 		int month = new Date().getMonth() + 1;
 		
 		// iterate all json results
 		for (int i=0; i<array.size(); i++) {
 			JSONString s = (JSONString) array.get(i);
 			String name = s.stringValue();
 			
 			CheckBox box = new CheckBox(name);
 			box.setName(name);
			if (name.equals("FIRST SEMESTER") && month > 6 && month < 11) {
 				box.setChecked(true);
 				controller_.setSemesterCriteria(name, true);
 			}
			if (name.equals("SECOND SEMESTER") && (month > 10 || month < 4)) {
 				box.setChecked(true);
 				controller_.setSemesterCriteria(name, true);
 			}
 			// add the click listener
 			box.addClickListener(new ClickListener() {
 
 				public void onClick(Widget sender) {
 					final CheckBox box = (CheckBox) sender;
 					final String name = box.getName();
 					controller_.setSemesterCriteria(name, box.isChecked());
 				}
 				
 			});
 			panel_.add(box);
 		}
 	}
 	
 	public void clear() {
 		panel_.clear();
 	}
 
 	public void addSubView(View subView) {
 		throw new RuntimeException("Not Implemented.");
 	}
 
 	public ViewController getController() {
 		return controller_;
 	}
 
 	public Widget getWidget() {
 		return panel_;
 	}
 
 	public void hide() {
 		panel_.setVisible(false);
 	}
 
 	public void show() {
 		panel_.setVisible(true);
 	}
 
 }
