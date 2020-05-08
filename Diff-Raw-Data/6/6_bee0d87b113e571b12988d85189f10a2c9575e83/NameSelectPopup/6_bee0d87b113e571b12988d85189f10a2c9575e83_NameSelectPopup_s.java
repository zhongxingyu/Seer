 package org.percepta.mgrankvi.floorplanner.gwt.client.floorgrid;
 
 import java.util.List;
 
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.PopupPanel;
 
public class NameSelectPopup extends Composite {
 
 	public NameSelectPopup(final List<String> possibilities, final CFloorGrid grid) {
 		final PopupPanel select = new PopupPanel();
		initWidget(select);
 
 		final ListBox listselect = new ListBox();
 		for (final String name : possibilities) {
 			listselect.addItem(name);
 		}
 		listselect.setVisibleItemCount(5);
 		listselect.setWidth("100%");
 
 		final FlowPanel layout = new FlowPanel();
 		layout.add(new Label("Found " + possibilities.size() + " possible matches."));
 		layout.add(new Label("Select person:"));
 		layout.add(listselect);
 
 		final Button ok = new Button("Ok", new ClickHandler() {
 
 			@Override
 			public void onClick(final ClickEvent event) {
 				grid.markTableOfSelectedPerson(listselect.getValue(listselect.getSelectedIndex()));
 				select.hide();
 			}
 		});
 		ok.getElement().getStyle().setProperty("float", "right");
 		final Button cancel = new Button("Cancel", new ClickHandler() {
 
 			@Override
 			public void onClick(final ClickEvent event) {
 				select.hide();
 			}
 		});
 		cancel.getElement().getStyle().setProperty("float", "right");
 
 		layout.add(ok);
 		layout.add(cancel);
 
 		final Style style = layout.getElement().getStyle();
 		style.setBackgroundColor("burlywood");
 		style.setPadding(10.0, Unit.PX);
 		style.setProperty("border-radius", "4px");
 		style.setProperty("-moz-border-radius", "4px");
 		style.setProperty("-webkit-border-radius", "4px");
 		style.setPaddingBottom(25, Unit.PX);
 
 		select.add(layout);
 		select.setPopupPosition(Window.getClientWidth() / 2, Window.getClientHeight() / 2);
 		select.show();
 	}
 
 }
