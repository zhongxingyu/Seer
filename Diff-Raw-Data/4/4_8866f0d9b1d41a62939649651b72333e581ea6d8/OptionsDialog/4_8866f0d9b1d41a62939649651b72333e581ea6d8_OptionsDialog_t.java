 package cc.explain.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class OptionsDialog extends CafaWidget implements Dialog {
 
     public static final String MIN = "min";
     public static final String MAX = "max";
     public static final String TEXT_TEMPLATE = "textTemplate";
     public static final String SUBTITLE_TEMPLATE = "subtitleTemplate";
     @UiField
     TextBox min;
 
     @UiField
     TextBox max;
 
     @UiField
     Button preview;
 //
 //    @UiField
 //    Button restore;
 
 //    @UiField
 //    TextBox textTemplate;
 
     @UiField
     TextBox subtitleTemplate;
 
     @UiField
     Button save;
 
     @UiTemplate("OptionsDialog.ui.xml")
     interface OptionsDialogUiBinder extends UiBinder<Widget, OptionsDialog> {
     }
 
     private static OptionsDialogUiBinder uiBinder = GWT.create(OptionsDialogUiBinder.class);
 
     public OptionsDialog() {
         super();
         initWidget(uiBinder.createAndBindUi(this));
     }
 
     public void init() {
         loadOptions();
         min.getElement().setAttribute("type", "range");
         min.getElement().setAttribute("min", "1");
         min.getElement().setAttribute("max", "100");
     }
 
     @UiHandler("min")
     public void changeSlider(ChangeEvent e) {
         max.setValue(min.getValue());
     }
 
     @UiHandler("preview")
     public void previewClick(ClickEvent e) {
         String exampleSubtitle = "<div style='color:white; background-color: black;'>This is example## subtitle.</div>";
         String pattern = subtitleTemplate.getText().replace("@@TRANSLATED_TEXT@@","przyklad");
         exampleSubtitle = exampleSubtitle.replace("##",pattern);
         getController().getMainDialog().handleError(exampleSubtitle);
     }
 //
     @UiHandler("restore")
     public void restore(ClickEvent e) {
         max.setValue(String.valueOf(89));
         min.setValue(String.valueOf(89));
         subtitleTemplate.setText("<font color=\"yellow\">(@@TRANSLATED_TEXT@@)</font>");
     }
 
     @UiHandler("max")
     public void changeMax(ChangeEvent e) {
         min.setValue(max.getValue());
     }
 
     @UiHandler("save")
     public void registerClick(ClickEvent e) {
         JSONObject jsonObject = new JSONObject();
 //       jsonObject.put(TEXT_TEMPLATE, new JSONString(textTemplate.getText()));
         jsonObject.put(SUBTITLE_TEMPLATE, new JSONString(subtitleTemplate.getText()));
         jsonObject.put(MIN, new JSONString(String.valueOf(5)));
         jsonObject.put(MAX, new JSONString(max.getText()));
         saveOptions(jsonObject.toString());
     }
 
     public void initOptions(JavaScriptObject jsObject) {
         JSONObject jsonObject = new JSONObject(jsObject);
        min.setValue(jsonObject.get(MAX).isNumber().toString());
        max.setValue(jsonObject.get(MAX).isNumber().toString());
 //        textTemplate.setText(jsonObject.get(TEXT_TEMPLATE).isString().stringValue());
         subtitleTemplate.setText(jsonObject.get(SUBTITLE_TEMPLATE).isString().stringValue());
     }
 
     public void afterSave() {
         Window.alert("SAVED :)");
         loadOptions();
     }
 
     public native void loadOptions() /*-{
         var instance = this;
         $wnd.ajaxExecutor.loadOptions(function(optionsData) {
             instance.@cc.explain.client.OptionsDialog::initOptions(Lcom/google/gwt/core/client/JavaScriptObject;)(optionsData);
         });
     }-*/;
 
 
     public native void saveOptions(String json) /*-{
         var instance = this;
         console.log("saveOptions");
         $wnd.console.log(json);
         console.log(json);
         $wnd.ajaxExecutor.saveOptions(json, function(data) {
             instance.@cc.explain.client.OptionsDialog::afterSave()();
         });
     }-*/;
 
 
 }
