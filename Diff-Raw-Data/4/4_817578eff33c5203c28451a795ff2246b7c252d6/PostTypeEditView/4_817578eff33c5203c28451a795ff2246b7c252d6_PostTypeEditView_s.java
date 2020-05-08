 package no.knubo.accounting.client.views.registers;
 
 import java.util.HashMap;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 import no.knubo.accounting.client.cache.AccountPlanCache;
 import no.knubo.accounting.client.cache.MonthHeaderCache;
 import no.knubo.accounting.client.help.HelpPanel;
 import no.knubo.accounting.client.misc.AuthResponder;
 import no.knubo.accounting.client.misc.IdHolder;
 import no.knubo.accounting.client.misc.ImageFactory;
 import no.knubo.accounting.client.misc.ServerResponse;
 import no.knubo.accounting.client.ui.ListBoxWithErrorText;
 import no.knubo.accounting.client.ui.NamedButton;
 import no.knubo.accounting.client.ui.TextBoxWithErrorText;
 import no.knubo.accounting.client.validation.MasterValidator;
 
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Widget;
 
 public class PostTypeEditView extends Composite implements ClickListener {
 
     private static PostTypeEditView me;
     private final I18NAccount messages;
     private final Constants constants;
     private final HelpPanel helpPanel;
     private FlexTable inUseTable;
     private FlexTable notInUseTable;
     private IdHolder<String, Image> idHolderInUse;
     private IdHolder<String, Image> idHolderNotInUse;
     private IdHolder<String, Image> idHolderEdit;
     private NamedButton button;
     private HashMap<String, JSONObject> objectPerId;
     private PostTypeEditFields editFields;
     private Elements elements;
 
     public static PostTypeEditView show(I18NAccount messages, Constants constants,
             HelpPanel helpPanel, Elements elements) {
         if (me == null) {
             me = new PostTypeEditView(messages, constants, helpPanel, elements);
         }
         me.setVisible(true);
         return me;
     }
 
     public PostTypeEditView(I18NAccount messages, Constants constants, HelpPanel helpPanel,
             Elements elements) {
         this.messages = messages;
         this.constants = constants;
         this.helpPanel = helpPanel;
         this.elements = elements;
 
         DockPanel dp = new DockPanel();
 
         button = new NamedButton("new_account", elements.new_account());
         button.addClickListener(this);
         dp.add(button, DockPanel.NORTH);
 
         idHolderInUse = new IdHolder<String, Image>();
         idHolderNotInUse = new IdHolder<String, Image>();
         idHolderEdit = new IdHolder<String, Image>();
         objectPerId = new HashMap<String, JSONObject>();
         inUseTable = createTable(elements.title_posttype_edit_in_use());
         notInUseTable = createTable(elements.title_posttype_edit_not_in_use());
 
         dp.add(inUseTable, DockPanel.NORTH);
         dp.add(notInUseTable, DockPanel.NORTH);
 
         initWidget(dp);
     }
 
     public FlexTable createTable(String title) {
         FlexTable table = new FlexTable();
         table.setStyleName("tableborder");
         table.setHTML(0, 0, title);
         table.getRowFormatter().setStyleName(0, "header");
         table.getFlexCellFormatter().setColSpan(0, 0, 6);
 
         table.setText(1, 0, elements.account());
         table.setText(1, 1, elements.description());
         table.setText(1, 2, elements.account_collection_month());
         table.setText(1, 3, elements.account_collection_accountplan());
         table.setText(1, 4, "");
         table.setText(1, 5, "");
 
         table.getRowFormatter().setStyleName(1, "header");
 
         return table;
     }
 
     public void init() {
         idHolderInUse.init();
         idHolderNotInUse.init();
         idHolderEdit.init();
         objectPerId.clear();
         while (inUseTable.getRowCount() > 2) {
             inUseTable.removeRow(2);
         }
 
         while (notInUseTable.getRowCount() > 2) {
             notInUseTable.removeRow(2);
         }
 
         final MonthHeaderCache monthHeaderCache = MonthHeaderCache.getInstance(constants, messages);
         final AccountPlanCache accountPlanCache = AccountPlanCache.getInstance(constants, messages);
 
         ServerResponse callback = new ServerResponse() {
 
             public void serverResponse(JSONValue parse) {
                 JSONArray array = parse.isArray();
 
                 for (int i = 0; i < array.size(); i++) {
                     JSONValue value = array.get(i);
                     JSONObject object = value.isObject();
                     String id = Util.str(object.get("PostType"));
                     String description = Util.str(object.get("Description"));
                     String inUse = Util.str(object.get("InUse"));
                     String colAccMonth = Util.str(object.get("CollPost"));
                     String colAccAccountPlan = Util.str(object.get("DetailPost"));
 
                     objectPerId.put(id, object);
 
                     String colAccMonthDesc = monthHeaderCache.getDescription(colAccMonth);
 
                     String colAccAccountPlanDesc = accountPlanCache.idGivesName(colAccAccountPlan);
                     if ("1".equals(inUse)) {
                         addRow(inUseTable, idHolderInUse, id, description, inUse, colAccMonthDesc,
                                 colAccAccountPlanDesc);
                     } else {
                         addRow(notInUseTable, idHolderNotInUse, id, description, inUse,
                                 colAccMonthDesc, colAccAccountPlanDesc);
                     }
 
                 }
                 helpPanel.resize(me);
             }
 
         };
 
         AuthResponder.get(constants, messages, callback,
                 "registers/posttypes.php?action=all&disableFilter=1");
 
     }
 
     private void addRow(FlexTable table, IdHolder<String, Image> idHolder, String id, String description,
             String inUse, String colAccMonth, String colAccAccountPlan) {
         int row = table.getRowCount();
 
         table.setText(row, 0, id);
         table.setText(row, 1, description);
         table.setText(row, 2, colAccMonth);
         table.setText(row, 3, colAccAccountPlan);
 
         for (int i = 1; i < 4; i++) {
             table.getCellFormatter().setStyleName(row, i, "desc");
         }
         Image actionImage = null;
         if ("1".equals(inUse)) {
             actionImage = ImageFactory.removeImage("postTypeEditView.removeImage");
         } else {
             actionImage = ImageFactory.chooseImage("postTypeEditView.chooseImage");
         }
         actionImage.addClickListener(me);
         idHolder.add(id, actionImage);
 
         Image editImage = ImageFactory.editImage("postTypeEditView.editImage");
         editImage.addClickListener(me);
         idHolderEdit.add(id, editImage);
 
         table.setWidget(row, 4, editImage);
         table.setWidget(row, 5, actionImage);
 
         String style = (((row + 1) % 6) < 3) ? "line2" : "line1";
         table.getRowFormatter().setStyleName(row, style);
     }
 
     public void onClick(Widget sender) {
         String id = null;
 
         if (sender == button) {
             edit(sender, "");
             return;
         }
 
         id = idHolderEdit.findId(sender);
         if (id != null) {
             edit(sender, id);
             return;
         }
 
         id = idHolderInUse.findId(sender);
         if (id != null) {
             changeUse(id, 0, idHolderInUse, idHolderNotInUse);
             return;
         }
 
         id = idHolderNotInUse.findId(sender);
         if (id != null) {
             changeUse(id, 1, idHolderNotInUse, idHolderInUse);
             return;
         }
     }
 
     private void edit(Widget sender, String id) {
         if (editFields == null) {
             editFields = new PostTypeEditFields();
         }
 
         int left = 0;
         if (sender == button) {
             left = sender.getAbsoluteLeft() + 20;
         } else {
             left = sender.getAbsoluteLeft() - 700;
         }
 
         int top = sender.getAbsoluteTop() + 10;
         editFields.setPopupPosition(left, top);
 
         if (sender == button) {
             editFields.init();
         } else {
             editFields.init(id);
         }
         editFields.show();
     }
 
     private void changeUse(final String id, final int use, final IdHolder<String, Image> outofHolder,
             final IdHolder<String, Image> intoHolder) {
 
         ServerResponse callback = new ServerResponse() {
 
             public void serverResponse(JSONValue parse) {
                 JSONObject respObj = parse.isObject();
                 if ("1".equals(Util.str(respObj.get("result")))) {
                     int line = outofHolder.remove(id);
 
                     JSONObject object = objectPerId.get(id);
                     String id = Util.str(object.get("PostType"));
                     String description = Util.str(object.get("Description"));
                     String colAccMonth = Util.str(object.get("CollPost"));
                     String colAccAccountPlan = Util.str(object.get("DetailPost"));
 
                     final MonthHeaderCache monthHeaderCache = MonthHeaderCache.getInstance(
                             constants, messages);
                     final AccountPlanCache accountPlanCache = AccountPlanCache.getInstance(
                             constants, messages);
 
                     FlexTable table = null;
                     if (use == 0) {
                         table = notInUseTable;
                         inUseTable.removeRow(line + 2);
                     } else {
                         table = inUseTable;
                         notInUseTable.removeRow(line + 2);
                     }
 
                     addRow(table, intoHolder, id, description, String.valueOf(use),
                             monthHeaderCache.getDescription(colAccMonth), accountPlanCache
                                     .idGivesName(colAccAccountPlan));
 
                 } else {
                     Window.alert(messages.bad_server_response());
                 }
 
             }
 
         };
 
         AuthResponder.get(constants, messages, callback, "registers/posttypes.php?action=use&use="
                 + use + "&posttype=" + id);
     }
 
     class PostTypeEditFields extends DialogBox implements ClickListener {
 
         private NamedButton saveButton;
         private NamedButton cancelButton;
         private HTML mainErrorLabel;
         private String currentId;
         private TextBoxWithErrorText accountBox;
         private TextBoxWithErrorText descBox;
         private ListBoxWithErrorText colMonthListBox;
         private ListBoxWithErrorText colAccountPlanListBox;
 
         PostTypeEditFields() {
             setText(elements.title_edit_posttype());
             FlexTable edittable = new FlexTable();
             edittable.setStyleName("edittable");
 
             edittable.setHTML(0, 0, elements.account());
             edittable.setHTML(1, 0, elements.description());
             edittable.setText(2, 0, elements.account_collection_month());
             edittable.setText(3, 0, elements.account_collection_accountplan());
 
             accountBox = new TextBoxWithErrorText("account");
             edittable.setWidget(0, 1, accountBox);
 
             descBox = new TextBoxWithErrorText("description");
             descBox.setVisibleLength(100);
             descBox.setMaxLength(100);
             edittable.setWidget(1, 1, descBox);
 
             colMonthListBox = new ListBoxWithErrorText("account_collection_month");
             edittable.setWidget(2, 1, colMonthListBox);
 
             colAccountPlanListBox = new ListBoxWithErrorText("account_collection_accountplan");
             edittable.setWidget(3, 1, colAccountPlanListBox);
 
             MonthHeaderCache.getInstance(constants, messages).fill(colMonthListBox.getListbox());
             AccountPlanCache.getInstance(constants, messages).fill(
                     colAccountPlanListBox.getListbox());
 
             DockPanel dp = new DockPanel();
             dp.add(edittable, DockPanel.NORTH);
 
             saveButton = new NamedButton("HappeningsView.saveButton", elements.save());
             saveButton.addClickListener(this);
             cancelButton = new NamedButton("HappeningsView.cancelButton", elements.cancel());
             cancelButton.addClickListener(this);
 
             mainErrorLabel = new HTML();
             mainErrorLabel.setStyleName("error");
 
             HorizontalPanel buttonPanel = new HorizontalPanel();
             buttonPanel.add(saveButton);
             buttonPanel.add(cancelButton);
             buttonPanel.add(mainErrorLabel);
             dp.add(buttonPanel, DockPanel.NORTH);
             setWidget(dp);
         }
 
         public void init() {
             currentId = null;
             mainErrorLabel.setText("");
             accountBox.setText("");
             descBox.setText("");
             accountBox.setEnabled(true);
             colMonthListBox.setSelectedIndex(0);
             colAccountPlanListBox.setSelectedIndex(0);
         }
 
         public void init(String id) {
             init();
             currentId = id;
 
             JSONObject obj = objectPerId.get(id);
 
             accountBox.setText(Util.str(obj.get("PostType")));
             descBox.setText(Util.str(obj.get("Description")));
 
             Util.setIndexByValue(colMonthListBox.getListbox(), Util.str(obj.get("CollPost")));
 
             Util.setIndexByValue(colAccountPlanListBox.getListbox(), Util
                     .str(obj.get("DetailPost")));
 
             accountBox.setEnabled(false);
         }
 
         public void onClick(Widget sender) {
             if (sender == cancelButton) {
                 hide();
             } else if (sender == saveButton && validateFields()) {
                 doSave();
             }
         }
 
         private void doSave() {
             StringBuffer sb = new StringBuffer();
             sb.append("action=save");
            final String sendId = currentId;
 
             Util.addPostParam(sb, "posttype", sendId);
             Util.addPostParam(sb, "desc", descBox.getText());
             Util.addPostParam(sb, "collpost", colMonthListBox.getText());
             Util.addPostParam(sb, "detailpost", colAccountPlanListBox.getText());
 
             ServerResponse callback = new ServerResponse() {
 
                 public void serverResponse(JSONValue parse) {
                     JSONObject object = parse.isObject();
 
                     String result = Util.str(object.get("result"));
 
                     if ("1".equals(result)) {
                         hide();
                         me.init();
                     } else {
                         mainErrorLabel.setText(messages.save_failed());
                         Util.timedMessage(mainErrorLabel, "", 5);
                     }
 
                 }
             };
 
             AuthResponder.post(constants, messages, callback, sb, "registers/posttypes.php");
 
         }
 
         private boolean validateFields() {
             MasterValidator mv = new MasterValidator();
 
             mv.mandatory(messages.required_field(), new Widget[] { accountBox, descBox,
                     colMonthListBox });
             return mv.validateStatus();
         }
 
     }
 }
