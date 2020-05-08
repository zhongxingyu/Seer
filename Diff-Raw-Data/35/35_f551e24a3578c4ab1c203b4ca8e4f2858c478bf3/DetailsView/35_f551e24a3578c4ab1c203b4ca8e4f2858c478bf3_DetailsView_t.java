 package org.vaadin.demo.crm.ui;
 
 import org.vaadin.demo.crm.data.Record;
 
 import com.vaadin.addon.touchkit.ui.NavigationView;
 import com.vaadin.addon.touchkit.ui.Switch;
 import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.DefaultFieldFactory;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.FormFieldFactory;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 public class DetailsView extends NavigationView implements FormFieldFactory {
 
 	public void setRecord(Record record) {
 		getNavigationBar().setCaption(record.getRecordType());
 
 		VerticalLayout lo = new VerticalLayout();
 		setContent(lo);
 
 		VerticalComponentGroup formGroup = new VerticalComponentGroup();
 		formGroup.setCaption(record.getRecordName());
 		lo.addComponent(formGroup);
 		final Form form = new Form();
 		formGroup.addComponent(form);
 		form.getLayout().setMargin(true);
 		form.setFormFieldFactory(this);
 		form.setItemDataSource(new BeanItem<Object>(record));
 
 		getNavigationBar().setRightComponent(
 				new Button("Save", new Button.ClickListener() {
 					public void buttonClick(ClickEvent event) {
 						form.commit();
 					}
 				}));
 	}
 
 	/* Custom mobile field types */
 	public Field createField(Item item, Object propertyId, Component uiContext) {
 		Property p = item.getItemProperty(propertyId);
 		if (propertyId.equals("recordName")
 				|| propertyId.equals("recordTypePlural")
 				|| propertyId.equals("id") || propertyId.equals("recordType"))
 			return null;
 		if (p.getType() == Boolean.class)
 			return new Switch("" + propertyId, p);
 		if (p.getType() == String.class && propertyId.equals("description")) {
 			TextArea a = new TextArea("" + propertyId, p);
 			a.setNullRepresentation("");
 			a.setWidth("100%");
 			a.setRows(10);
 			return a;
 		}
 		Field defaultField = DefaultFieldFactory.get().createField(item,
 				propertyId, uiContext);
 		if (defaultField instanceof TextField) {
 			((TextField) defaultField).setNullRepresentation("");
 			defaultField.setWidth("100%");
 		}
 		return defaultField;
 	}
 }
