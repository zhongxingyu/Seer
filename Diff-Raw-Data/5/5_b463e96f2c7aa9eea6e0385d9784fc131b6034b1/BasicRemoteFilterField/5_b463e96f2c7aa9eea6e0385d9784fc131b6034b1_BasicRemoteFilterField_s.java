 /*
  * Copyright (c) 2008 TouK.pl
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package pl.touk.tola.gwt.client.widgets;
 
 import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
 import com.extjs.gxt.ui.client.data.LoadEvent;
 import com.extjs.gxt.ui.client.data.Loader;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 
 import com.extjs.gxt.ui.client.widget.form.TriggerField;
 import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.KeyboardListener;
 
 
 /**
  */
 public class BasicRemoteFilterField extends TriggerField implements Listener {
     protected PagingToolBar pager;
     protected Loader loader;
     protected String currentFilterValue = "";
     protected String filteredPropertyName;
     protected String valueRegex;
 
     public BasicRemoteFilterField(PagingToolBar pager, Loader loader) {
         this.pager = pager;
         this.loader = loader;
         loader.addListener(Loader.BeforeLoad, this);
         setAutoValidate(true);
         setValidateOnBlur(false);
         this.setTriggerStyle("x-form-clear-trigger");
         setWidth(112);
     }
 
     public BasicRemoteFilterField(PagingToolBar pager, Loader loader,
         String filterPropertyName) {
         this(pager, loader);
         this.filteredPropertyName = filterPropertyName;
     }
 
     /**
      * @param pager
      * @param loader
      * @param filterPropertyName
      * @param valueRegex         - regex used for value validation
      */
     public BasicRemoteFilterField(PagingToolBar pager, Loader loader,
         String filterPropertyName, String valueRegex) {
         this(pager, loader);
         this.filteredPropertyName = filterPropertyName;
         this.valueRegex = valueRegex;
     }
 
     @Override
     protected void onTriggerClick(ComponentEvent ce) {
        super.onTriggerClick(ce);
         clear();
        onFilter();
     }
 
     public void clear() {
         setValue("");
         clearInvalid();
         applyEmptyText();
         onFilter();
     }
 
     @Override
     protected boolean validateValue(String value) {
         if ((valueRegex != null) && (value != null) && (value.length() != 0)) {
             if (!value.matches(valueRegex)) {
                 markInvalid("Niepoprawna wartość filtru");
 
                 return false;
             }
         }
 
         boolean ret = super.validateValue(value);
 
         if (ret) {
             clearInvalid();
 
             //onFilter();
         } else {
             markInvalid("Niepoprawna wartość filtru");
         }
 
         return ret;
     }
 
     protected void onFilter() {
         GWT.log("on filter " + getValue() + " cfv: " + currentFilterValue, null);
 
         if (!(((getValue() != null) && getValue().equals(currentFilterValue)) ||
                 ((getValue() == null) && "".equals(currentFilterValue)))) {
             currentFilterValue = "" + getValue();
             pager.first();
         }
     }
 
     //added to filter when user ends editing the field
     @Override
     protected void onBlur(ComponentEvent arg0) {
         super.onBlur(arg0);
 
         if (this.validate()) {
             onFilter();
         }
     }
 
     //added to filter when user hit ENTER key
     @Override
     protected void onKeyPress(FieldEvent arg0) {
         super.onKeyPress(arg0);
 
         if (arg0.getKeyCode() == KeyboardListener.KEY_ENTER) {
             if (validate()) {
                 onFilter();
             }
         }
     }
 
     public void handleEvent(BaseEvent be) {
         GWT.log("Handle event before load", null);
 
         if ((be != null) && (be.getType() == Loader.BeforeLoad)) {
             LoadEvent loadEvent = (LoadEvent) be;
             BasePagingLoadConfig config = (BasePagingLoadConfig) loadEvent.getConfig();
 
             //        Map filterMap = config.getFilterMap();
             if ((this.getValue() == null) ||
                     this.getValue().toString().trim().equals("")) {
                 config.remove(filteredPropertyName);
             } else {
                 config.set(filteredPropertyName, this.getValue());
             }
         }
     }
 
     public String getFilteredPropertyName() {
         return filteredPropertyName;
     }
 
     public void setFilteredPropertyName(String filteredPropertyName) {
         this.filteredPropertyName = filteredPropertyName;
     }
 
     public String getValueRegex() {
         return valueRegex;
     }
 
     public void setValueRegex(String valueRegex) {
         this.valueRegex = valueRegex;
     }
 }
