 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.web.controller.component.items;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import org.jtalks.poulpe.model.dao.ComponentDao.ComponentDuplicateField;
 import org.jtalks.poulpe.model.dao.DuplicatedField;
 import org.jtalks.poulpe.model.entity.Component;
 import org.jtalks.poulpe.model.entity.ComponentType;
 import org.jtalks.poulpe.web.controller.component.ListPresenter;
 import org.jtalks.poulpe.web.controller.component.ListView;
 import org.zkoss.util.resource.Labels;
 import org.zkoss.zk.ui.Components;
 import org.zkoss.zk.ui.WrongValueException;
 import org.zkoss.zk.ui.WrongValuesException;
 import org.zkoss.zk.ui.ext.AfterCompose;
 import org.zkoss.zul.Combobox;
 import org.zkoss.zul.Longbox;
 import org.zkoss.zul.Textbox;
 import org.zkoss.zul.Window;
 
 /**
  * The class which manages actions and represents information about component
  * displayed in administrator panel.
  * 
  * @author Dmitriy Sukharev
  * @author Alexey Grigorev
  */
 public class ZkItemView extends Window implements ItemView, AfterCompose {
 
     public final static String ITEM_ALREADY_EXISTS = "item.already.exist";
 
     private static final long serialVersionUID = -3927090308078350369L;
 
     private transient ItemPresenter presenter;
     private Longbox cid;
     private Textbox name;
     private Textbox description;
     private Combobox componentType;
 
     /** {@inheritDoc} */
     @Override
     public void afterCompose() {
         Components.wireVariables(this, this);
         Components.addForwards(this, this);
         presenter.setView(this);
     }
 
     // ==== ItemView methods ====
 
     /** {@inheritDoc} */
     @Override
     public void wrongFields(Set<DuplicatedField> set) {
         // TODO: or not TODO? Let it be for a while
         
         ArrayList<WrongValueException> exceptions = new ArrayList<WrongValueException>();
         
         if (set.contains(ComponentDuplicateField.NAME)) {
             exceptions.add(new WrongValueException(name, Labels.getLabel(ITEM_ALREADY_EXISTS)));
         }
         
         if (set.contains(ComponentDuplicateField.TYPE)) {
             exceptions.add(new WrongValueException(componentType, Labels.getLabel(ITEM_ALREADY_EXISTS)));
         }
 
         throw new WrongValuesException(exceptions.toArray(new WrongValueException[1]));
     }
 
     /** {@inheritDoc} */
     @Override
     public void show(Component component) {
         presenter.edit(component);
         showForm();
     }
 
     /** {@inheritDoc} */
     @Override
     public void showEmpty() {
         presenter.create();
         showForm();
     }
 
     private void showForm() {
         setVisible(true);
         name.setFocus(true);
     }
 
     /** {@inheritDoc} */
     @Override
     public void hide() {
         setValidationConstraints(null); // TODO: null vs "no empty" ???
         setVisible(false);
         ListView listView = (ListView) getAttribute("backWin");
         if (null != listView) {
             listView.updateList();
         }
     }
 
     /**
      * Sets the constrains for name and component type fields.
      * @param constraints the constraints to be set
      */
     private void setValidationConstraints(String constraints) {
         componentType.setConstraint(constraints);
         name.setConstraint(constraints);
     }
     
     
     // ==== UI events ====
     
     /**
      * Performs validation and tells to presenter that user want to save created
      * or edited component in the component list.
      */
     public void onClick$saveCompButton() {
         setValidationConstraints("no empty"); // TODO: find out what "no empty" means
         presenter.saveComponent();
     }
 
     /**
      * Hides the window for adding or editing component.
      */
     public void onClick$cancelButton() {
         hide();
     }
 
     /**
      * Tells to presenter to check if name of created or edited component is a
      * duplicate.
      * @see ListPresenter
      */
     public void onBlur$name() {
         name.setConstraint("no empty");
         presenter.checkComponent();
     }
     
 
     // ==== ItemDataView methods ====
 
     /** {@inheritDoc} */
     @Override
     public long getComponentId() {
         return cid.getValue();
     }
 
     /** {@inheritDoc} */
     @Override
     public void setComponentId(long cid) {
         this.cid.setValue(cid);
     }
 
     /** {@inheritDoc} */
     @Override
     public String getName() {
         return name.getText();
     }
 
     /** {@inheritDoc} */
     @Override
     public void setName(String compName) {
         this.name.setText(compName);
     }
 
     /** {@inheritDoc} */
     @Override
     public String getDescription() {
         return description.getText();
     }
 
     /** {@inheritDoc} */
     @Override
     public void setDescription(String description) {
         this.description.setText(description);
     }
 
     /** {@inheritDoc} */
     @Override
     public ComponentType getComponentType() {
         return ComponentType.valueOf(componentType.getText());
     }
 
     /** {@inheritDoc} */
     @Override
     public void setComponentType(ComponentType type) {
        this.componentType.setText(type.toString());
     }
 
     /** {@inheritDoc} */
     @Override
     public void setComponentTypes(Set<ComponentType> types) {
         componentType.getChildren().clear();
         componentType.appendItem(componentType.getText());
         for (ComponentType type : types) {
             componentType.appendItem(type.toString());
         }
     }
 
     // ==== getters and setters ====
 
     /**
      * @return presenter linked to this view
      */
     public ItemPresenter getPresenter() {
         return presenter;
     }
 
     /**
      * @param presenter to be linked with this view
      */
     public void setPresenter(ItemPresenter presenter) {
         this.presenter = presenter;
     }
 }
