 /**
  * 
  */
 package org.mdkt.maildist.client.view;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.mdkt.maildist.client.MailDistAppController;
 
 import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author trung
  *
  */
 public class MailDistHome extends Composite {
 
 	private static MailDistHomeUiBinder uiBinder = GWT
 			.create(MailDistHomeUiBinder.class);
 	
 	private static final String[] TAB_NAMES = {MailDistAppController.MD_HOME, MailDistAppController.MD_ALIAS};
 	
 	private final Map<String, HasWidgets> tabContainers;
 
 	interface MailDistHomeUiBinder extends UiBinder<Widget, MailDistHome> {
 	}
 
 	/**
 	 * Because this class has a default constructor, it can
 	 * be used as a binder template. In other words, it can be used in other
 	 * *.ui.xml files as follows:
 	 * <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
 	 *   xmlns:g="urn:import:**user's package**">
 	 *  <g:**UserClassName**>Hello!</g:**UserClassName>
 	 * </ui:UiBinder>
 	 * Note that depending on the widget that is used, it may be necessary to
 	 * implement HasHTML instead of HasText.
 	 */
 	public MailDistHome() {
		tabBar = new TabLayoutPanel(1.5, Unit.EM);
 		tabContainers = new HashMap<String, HasWidgets>();
 		for (String tabName : TAB_NAMES) {
 			VerticalPanel container = new VerticalPanel();
 			container.setSize("100%", "100%");
 			container.setSpacing(10);
 			container.add(new HTML(tabName));
 			tabContainers.put(tabName, container);
 			tabBar.add(container, tabName);
 		}
 		tabBar.selectTab(0);
		initWidget(uiBinder.createAndBindUi(this));
 	}
 
	@UiField (provided = true) TabLayoutPanel tabBar;
 	
 	@UiHandler("tabBar")
 	public void addSelectionHandler(SelectionEvent<Integer> event) {
 		History.newItem(TAB_NAMES[event.getSelectedItem()]);
 	}
 	
 	public HasWidgets selectTab(String tabName) {
 		int index = getIndexByTabName(tabName);
 		tabBar.selectTab(index);
 		return tabContainers.get(tabName);
 	}
 	
 	private int getIndexByTabName(String tabName) {
 		for (int i = 0; i < TAB_NAMES.length; i++) {
 			if (TAB_NAMES[i].equals(tabName)) return i;
 		}
 		return -1;
 	}
 }
