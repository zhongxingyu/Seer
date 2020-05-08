 package org.vaadin.demo.crm.ui;
 
 import java.util.HashMap;
 
 import org.vaadin.demo.crm.Backend;
 import org.vaadin.demo.crm.data.Account;
 import org.vaadin.demo.crm.data.Record;
 
 import com.vaadin.addon.touchkit.ui.NavigationView;
 import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
 import com.vaadin.event.LayoutEvents.LayoutClickEvent;
 import com.vaadin.event.LayoutEvents.LayoutClickListener;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.VerticalLayout;
 
 public class AccountView extends NavigationView {
 
 	HashMap<Component, Record> relatedRecord = new HashMap<Component, Record>();
 
 	public AccountView(final Account account) {
 		getNavigationBar().setCaption(account.getRecordName());
 
 		getNavigationBar().setRightComponent(new Button("Edit", new ClickListener() {
 			public void buttonClick(ClickEvent event) {
 				((CrmApp) getApplication()).showDetails(account);
 			}
 		}));
 
 		VerticalLayout lo = new VerticalLayout();
 		setContent(lo);
 		
		@SuppressWarnings("rawtypes")
 		HashMap<Class, VerticalComponentGroup> groups = new HashMap<Class, VerticalComponentGroup>();
 		for (final Record r : Backend.getRecords(account)) {
 			VerticalComponentGroup g = groups.get(r.getClass());
 			if (g == null) {
 				g = new VerticalComponentGroup();
 				g.setCaption(r.getRecordTypePlural());
 				groups.put(r.getClass(), g);
 				lo.addComponent(g);
 				g.addListener(new LayoutClickListener() {
 					public void layoutClick(LayoutClickEvent event) {
 						((CrmApp) getApplication()).showDetails(relatedRecord
 								.get(event.getChildComponent()));
 					}
 				});
 			}
 			Label b = new Label(r.getRecordName());
			b.setSizeUndefined();
 			relatedRecord.put(b, r);
 			g.addComponent(b);
			b.setStyleName("v-touchkit-navbutton");
 		}
 	}
 }
