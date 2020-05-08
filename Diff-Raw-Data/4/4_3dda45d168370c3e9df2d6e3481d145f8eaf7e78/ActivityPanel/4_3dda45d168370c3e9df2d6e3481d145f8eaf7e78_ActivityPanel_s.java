 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.repository.client.activity;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.mule.galaxy.repository.client.util.ItemPathOracle;
 import org.mule.galaxy.repository.rpc.RegistryServiceAsync;
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.ui.panel.AbstractFlowComposite;
 import org.mule.galaxy.web.client.ui.panel.ErrorPanel;
 import org.mule.galaxy.web.client.ui.panel.InlineFlowPanel;
 import org.mule.galaxy.web.client.ui.panel.InlineHelpPanel;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.WActivity;
 import org.mule.galaxy.web.rpc.WUser;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.form.DateField;
 import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.SuggestBox;
 
 public class ActivityPanel extends AbstractFlowComposite {
 
     private ListBox userLB;
     private ListBox eventLB;
     private ListBox resultsLB;
     private final Galaxy galaxy;
     private int resultStart;
     private FlowPanel resultsPanel;
     private FlexTable table;
     private int maxResults;
     private SuggestBox itemSB;
     private TextField<String> textTB;
     private DateField startDate;
     private DateField endDate;
     private final ErrorPanel errorPanel;
     private final RegistryServiceAsync service;
 
     public ActivityPanel(ErrorPanel errorPanel, final Galaxy galaxy, RegistryServiceAsync service) {
         super();
         this.errorPanel = errorPanel;
         this.galaxy = galaxy;
         this.service = service;
     }
 
     public void initialize() {
 
 
         ContentPanel cp = new ContentPanel(new FormLayout());
         cp.setHeading("Activity Log");
         cp.setBodyBorder(false);
         cp.setAutoWidth(true);
         cp.addStyleName("x-panel-container-full");
         cp.setBodyStyleName("padded-panel");
 
         // add inline help string and widget
         cp.setTopComponent(
                 new InlineHelpPanel(galaxy.getAdministrationConstants().activityTip(), 22));
 
         FlowPanel searchContainer = new FlowPanel();
         cp.add(searchContainer);
 
         InlineFlowPanel searchPanel = new InlineFlowPanel();
         searchContainer.add(searchPanel);
 
         FlexTable searchTable = new FlexTable();
         searchTable.setCellSpacing(3);
         searchPanel.add(searchTable);
 
         startDate = new DateField();
         startDate.setValue(new Date());
         startDate.setPropertyEditor(new DateTimePropertyEditor("yyyy-MM-dd"));
         endDate = new DateField();
         endDate.setPropertyEditor(new DateTimePropertyEditor("yyyy-MM-dd"));
 
         searchTable.setWidget(0, 0, new Label("From:"));
         searchTable.setWidget(0, 1, startDate);
 
         searchTable.setWidget(1, 0, new Label("To:"));
         searchTable.setWidget(1, 1, endDate);
 
         userLB = new ListBox();
         userLB.addItem("All");
         userLB.addItem("System", "system");
         searchPanel.add(userLB);
         galaxy.getSecurityService().getUsers(new AbstractCallback(errorPanel) {
             public void onCallSuccess(Object result) {
                 initUsers((Collection) result);
             }
         });
 
         searchTable.setWidget(0, 2, new Label("User:"));
         searchTable.setWidget(0, 3, userLB);
 
         searchTable.setWidget(1, 2, new Label("Type:"));
         eventLB = new ListBox();
         eventLB.addItem("All");
         eventLB.addItem("Info");
         eventLB.addItem("Error");
         eventLB.addItem("Warning");
         searchTable.setWidget(1, 3, eventLB);
 
         searchTable.setWidget(0, 4, new Label("Text Contains:"));
         textTB = new TextField<String>();
         searchTable.setWidget(0, 5, textTB);
 
         searchTable.setWidget(1, 4, new Label("Relating to:"));
         itemSB = new SuggestBox(new ItemPathOracle(service, errorPanel));
         itemSB.setStyleName("x-form-text");
         itemSB.setText("[All Items]");
         searchTable.setWidget(1, 5, itemSB);
 
 
         searchTable.setWidget(0, 6, new Label("Max Results:"));
         resultsLB = new ListBox();
         resultsLB.addItem("10");
         resultsLB.addItem("25");
         resultsLB.addItem("50");
         resultsLB.addItem("100");
         resultsLB.addItem("200");
         searchTable.setWidget(0, 7, resultsLB);
 
         Button search = new Button("Search");
         search.addSelectionListener(new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 doShowPage();
             }
         });
 
         Button reset = new Button("Reset");
         reset.addSelectionListener(new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 reset();
                 doShowPage();
             }
         });
 
         ButtonBar bb = new ButtonBar();
         bb.add(search);
         bb.add(reset);
         searchContainer.add(bb);
 
         resultsPanel = new FlowPanel();
 
         cp.add(resultsPanel);
 
         panel.add(cp);
 
         // set form widgets to default values
         reset();
     }
 
     protected void initUsers(Collection result) {
         for (Iterator itr = result.iterator(); itr.hasNext();) {
             WUser user = (WUser) itr.next();
 
             userLB.addItem(user.getName() + " (" + user.getUsername() + ")", user.getId());
         }
     }
 
 
     @Override
     public void doShowPage() {
 
 
         if (panel.getWidgetCount() == 0) {
             initialize();
         }
         errorPanel.clearErrorMessage();
 
         resultsPanel.clear();
 
         String user = userLB.getValue(userLB.getSelectedIndex());
         String eventType = eventLB.getItemText(eventLB.getSelectedIndex());
         maxResults = new Integer(resultsLB.getItemText(resultsLB.getSelectedIndex())).intValue();
 
         boolean ascending = false;
         AbstractCallback callback = new AbstractCallback(errorPanel) {
 
             public void onCallSuccess(Object o) {
                 loadResults((Collection) o);
             }
 
         };
 
         Date fromDate = startDate.getValue();
         Date toDate = endDate.getValue();
 
         if(fromDate != null && endDate != null) {
             service.getActivities(fromDate, toDate, user,
                                   itemSB.getText(),
                                   textTB.getValue(),
                                   eventType, resultStart, maxResults,
                                   ascending, callback);
 
         }
 
 
     }
 
     protected void loadResults(Collection o) {
         resultsPanel.clear();
 
         if (o.size() == maxResults || resultStart > 0) {
             FlowPanel activityNavPanel = new FlowPanel();
             //activityNavPanel.setStyleName("activity-nav-panel");
             Hyperlink hl = null;
 
             if (o.size() == maxResults) {
                 hl = new Hyperlink("Next", "next");
                 //hl.setStyleName("activity-nav-next");
                 hl.addClickHandler(new ClickHandler() {
 
                     public void onClick(ClickEvent event) {
                         resultStart += maxResults;
 
                         doShowPage();
                     }
 
                 });
                 activityNavPanel.add(hl);
             }
 
             if (resultStart > 0) {
                 hl = new Hyperlink("Previous", "previous");
                 //hl.setStyleName("activity-nav-previous");
                 hl.addClickHandler(new ClickHandler() {
 
                     public void onClick(ClickEvent event) {
                         resultStart = resultStart - maxResults;
                         if (resultStart < 0) resultStart = 0;
 
                         doShowPage();
                     }
 
                 });
                 activityNavPanel.add(hl);
             }
             SimplePanel spacer = new SimplePanel();
             spacer.add(new HTML("&nbsp;"));
             activityNavPanel.add(spacer);
 
             resultsPanel.insert(activityNavPanel, 0);
         }
 
         table = createRowTable();
         resultsPanel.add(table);
 
         table.setText(0, 0, "Date");
         table.setText(0, 1, "User");
         table.setText(0, 2, "Type");
         table.setText(0, 3, "Activity");
 
         int i = 1;
         for (Iterator itr = o.iterator(); itr.hasNext();) {
             WActivity act = (WActivity) itr.next();
 
             table.setText(i, 0, act.getDate());
             //table.getCellFormatter().setStyleName(i, 0, "activityTableDate");
 
             if (act.getName() == null) {
                 table.setText(i, 1, "System");
             } else {
                 table.setText(i, 1, act.getName() + " (" + act.getUsername() + ")");
             }
             table.setText(i, 2, act.getEventType());
             table.setText(i, 3, act.getMessage());
             i++;
         }
     }
 
 
     // reset search params to default values
     private void reset() {
         userLB.setSelectedIndex(0);
         eventLB.setSelectedIndex(0);
         resultsLB.setSelectedIndex(2);
     }
 
 }
