 package org.vaadin.smartgwt.client.ui.form.fields;
 
 import org.vaadin.smartgwt.client.core.VDataClass;
 import org.vaadin.smartgwt.client.ui.form.VDynamicForm;
 
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.smartgwt.client.widgets.form.fields.FormItemIcon;
 import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
 import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.UIDL;
 
 public class VFormItemIcon extends VDataClass<FormItemIcon> {
 	private ApplicationConnection client;
 	private String pid;
 	private HandlerRegistration formItemClickRegistration;
 
 	public VFormItemIcon() {
 		super(new FormItemIcon());
 	}
 
 	@Override
 	protected void postAttributeUpdateFromUIDL(UIDL uidl, ApplicationConnection client) {
 		if (this.pid == null) {
 			this.pid = uidl.getId();
 			this.client = client;
 		}
 		
		if (uidl.hasAttribute("*hasFormItemClickHandlers") && formItemClickRegistration == null) {
 			formItemClickRegistration = getJSObject().addFormItemClickHandler(new FormItemClickHandler() {
 				@Override
 				public void onFormItemClick(FormItemIconClickEvent event) {
 					final ApplicationConnection client = VFormItemIcon.this.client;
 					client.updateVariable(pid, "formItemIconClickEvent.form", (VDynamicForm) event.getForm(), false);
 					client.updateVariable(pid, "formItemIconClickEvent.item", VDataClass.getVDataClass(client, event.getItem()), false);
 					client.updateVariable(pid, "formItemIconClickEvent.icon", VDataClass.getVDataClass(client, event.getIcon()), true);
 				}
 			});
 		}
 
 		super.postAttributeUpdateFromUIDL(uidl, client);
 	}
 }
