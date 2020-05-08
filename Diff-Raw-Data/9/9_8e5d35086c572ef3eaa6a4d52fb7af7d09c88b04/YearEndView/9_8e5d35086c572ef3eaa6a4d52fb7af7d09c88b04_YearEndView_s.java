 package no.knubo.accounting.client.views;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 import no.knubo.accounting.client.cache.PosttypeCache;
 import no.knubo.accounting.client.misc.AuthResponder;
 import no.knubo.accounting.client.misc.ServerResponse;
 import no.knubo.accounting.client.ui.NamedButton;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 
 public class YearEndView extends Composite implements ClickHandler {
 
     private static YearEndView me;
 
     private final Constants constants;
 
     private final I18NAccount messages;
 
     private FlexTable table;
 
     private final ViewCallback callback;
 
     private final Elements elements;
 
     private NamedButton endYearButton;
 
     private HTML endYearHtmlText;
 
     public static YearEndView getInstance(Constants constants, I18NAccount messages, ViewCallback callback,
             Elements elements) {
         if (me == null) {
             me = new YearEndView(constants, messages, callback, elements);
         }
         return me;
     }
 
     public YearEndView(Constants constants, I18NAccount messages, ViewCallback callback, Elements elements) {
         this.constants = constants;
         this.messages = messages;
         this.callback = callback;
         this.elements = elements;
 
         DockPanel dockPanel = new DockPanel();
 
         HTML header = new HTML();
         header.setHTML("<h2>" + elements.end_year() + "<h2>");
         dockPanel.add(header, DockPanel.NORTH);
 
         HTML info = new HTML();
         info.setHTML("<p>" + elements.end_year_heading() + "</p>");
         dockPanel.add(info, DockPanel.NORTH);
 
         table = new FlexTable();
         table.setStyleName("tableborder");
         table.setHTML(0, 0, elements.post());
         table.setHTML(0, 1, elements.description());
         table.setHTML(0, 2, elements.debet());
         table.setHTML(0, 3, elements.kredit());
         table.getRowFormatter().setStyleName(0, "header");
 
         endYearButton = new NamedButton("endyear", elements.end_year());
         endYearButton.addClickHandler(this);
 
         endYearHtmlText = new HTML();
 
         dockPanel.add(table, DockPanel.NORTH);
         dockPanel.add(endYearButton, DockPanel.NORTH);
         dockPanel.add(endYearHtmlText, DockPanel.NORTH);
         initWidget(dockPanel);
     }
 
     public void init() {
 
         ServerResponse rh = new ServerResponse() {
 
             public void serverResponse(JSONValue responseObj) {
                 JSONObject root = responseObj.isObject();
 
                 boolean readonly = Util.getBoolean(root.get("readonly"));
 
                 if (readonly) {
                     endYearHtmlText.setText(elements.end_year_only_last_month());
                     endYearButton.setEnabled(false);
                 } else {
                     endYearHtmlText.setText("");
                     endYearButton.setEnabled(true);
                 }
                 fillPosts(root.get("data").isArray());
             }
         };
         AuthResponder.get(constants, messages, rh, "accounting/endyear.php?action=status");
     }
 
     protected void fillPosts(JSONArray posts) {
         while (table.getRowCount() > 1) {
             table.removeRow(1);
         }
         PosttypeCache posttypeCache = PosttypeCache.getInstance(constants, messages);
 
         String rowStyle = "showlineposts1";
         for (int i = 0; i < posts.size(); i++) {
             JSONObject onePost = posts.get(i).isObject();
 
             if (i % 3 == 0) {
                 rowStyle = (rowStyle.equals("showlineposts1")) ? "showlineposts2" : "showlineposts1";
             }
             table.getRowFormatter().setStyleName(i + 1, rowStyle);
             table.getCellFormatter().setStyleName(i + 1, 1, "desc");
             table.getCellFormatter().setStyleName(i + 1, 2, "right");
             table.getCellFormatter().setStyleName(i + 1, 3, "right");
 
             table.setText(i + 1, 0, Util.str(onePost.get("post")));
             if ("1".equals(Util.str(onePost.get("DEBET")))) {
                 table.setText(i + 1, 2, Util.money(onePost.get("value")));
             } else {
                 table.setText(i + 1, 3, Util.money(onePost.get("value")));
             }
 
             table.setText(i + 1, 1, posttypeCache.getDescription(Util.str(onePost.get("post"))));
 
         }
     }
 
     public void onClick(ClickEvent event) {
 
         boolean okContinue = Window.confirm(messages.end_year_confirm());
 
         if (!okContinue) {
             return;
         }
 
         ServerResponse rh = new ServerResponse() {
 
             public void serverResponse(JSONValue responseObj) {
                 callback.viewMonth();
             }
         };
        AuthResponder.get(constants, messages, rh, constants.baseurl() + "accounting/endyear.php?action=endyear");
 
     }
 }
