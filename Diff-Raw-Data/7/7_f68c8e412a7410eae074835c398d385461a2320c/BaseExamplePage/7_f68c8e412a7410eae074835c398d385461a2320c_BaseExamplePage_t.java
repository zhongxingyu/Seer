 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.wicketstuff.objectautocomplete.example;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.markup.repeater.data.ListDataProvider;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.util.lang.PropertyResolver;
 import org.wicketstuff.objectautocomplete.AutoCompletionChoicesProvider;
 import org.wicketstuff.objectautocomplete.ObjectAutoCompleteBuilder;
 import org.wicketstuff.objectautocomplete.ObjectAutoCompleteField;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * @author roland
  * @since May 26, 2008
  */
abstract public class BaseExamplePage<T extends Serializable,I extends Serializable> extends WebPage<I> implements AutoCompletionChoicesProvider<T> {
 
     private ObjectAutoCompleteField<T,I> acField;
 
     protected BaseExamplePage() {
        this(new Model<I>());
     }
 
     public BaseExamplePage(IModel<I> pModel) {
         super(pModel);
         initExample();
     }
 
     private void initExample() {
         ObjectAutoCompleteBuilder<T> builder =
                 new ObjectAutoCompleteBuilder<T>(this);
         initBuilder(builder);
         acField = builder.build("acField", getModel());
 
         final Form form = new Form("form");
         add(form);
         form.add(acField);
         form.add(new Label("acLabel",getAutoCompleteFieldLabel()));
 
        // Add code sample and list of sample data
         add(new Label("acCodeSample",getCodeSample()));
 
         add(new DataView<T>("acData",new ListDataProvider<T>(getAllChoices())) {
             @Override
             protected void populateItem(Item<T> item) {
                 T object = item.getModelObject();
                 item.add(new Label("id",
                         PropertyResolver.getValue(getIdProperty(),object).toString()));
                 item.add(new Label("name",
                         PropertyResolver.getValue(getNameProperty(),object).toString()));
             }
         });
     }
 
     /**
      * Override to initialize the builder. Does nothing
      * in this base class.
      *
      * @param pBuilder builder to initialize.
      */
     protected void initBuilder(ObjectAutoCompleteBuilder pBuilder) {
         // intentionally empty
     }
 
     /**
      * Add a component to the list of components to update during
      * an ajax update of the autocomplete field
      *
      * @param pComponent component to update
      */
     protected void registerForUpdateOnModelChange(Component<I> pComponent) {
         acField.registerForUpdateOnModelChange(pComponent);
     }
 
     protected String getAutoCompleteFieldLabel() {
        return "Search:";
     }
 
     // id-property used for presenting the list of alternatives
     protected  String getIdProperty() {
         return "id";
     }
 
     // name property used to present the list of alternatives
     protected String getNameProperty() {
         return "name";
     }
 
     // used to get the list of all possible choices
     abstract List<T> getAllChoices();
 
     // a sample of the usage for this code
     abstract String getCodeSample();
 }
