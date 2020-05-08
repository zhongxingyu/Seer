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
 import com.google.gwt.user.client.ui.*;
 
 public class OptionsDialog extends CafaWidget implements Dialog {
 
     public static final String MIN = "min";
     public static final String MAX = "max";
     public static final String TEXT_TEMPLATE = "textTemplate";
     public static final String SUBTITLE_TEMPLATE = "subtitleTemplate";
     public static final String PHRASAL_VERB_TEMPLATE = "phrasalVerbTemplate";
     public static final String PHRASAL_VERB = "phrasalVerbAdded";
     public static final String SUBTITLE_TYPE = "subtitleProcessor";
 
     @UiField
     TextBox min;
 
     @UiField
     TextBox max;
 
     @UiField
     Button subtitlePreview;
 
     @UiField
     Button phrasalVerbPreview;
 
     @UiField
     TextBox subtitleTemplate;
 
     @UiField
     CheckBox phrasalVerb;
 
     @UiField
     TextBox phrasalVerbTemplate;
 
     @UiField
     Button save;
 
     @UiField
     RadioButton subtitleTypeInText;
 
     @UiField
     RadioButton subtitleTypeOnlyTranslation;
 
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
         onSubtitleTypeInTextClick(null);
 
     }
 
 
     @UiHandler("subtitleTypeInText")
     public void onSubtitleTypeInTextClick(ClickEvent e) {
         subtitleTypeInText.setValue(true);
         subtitleTypeOnlyTranslation.setValue(false);
     }
 
     @UiHandler("subtitleTypeOnlyTranslation")
     public void onSubtitleTypeOnlyTranlsationClick(ClickEvent e) {
         subtitleTypeInText.setValue(false);
         subtitleTypeOnlyTranslation.setValue(true);
     }
 
     private String getSubtitleType(){
         if(subtitleTypeInText.getValue()){  //TODO refactor powinno czytac te wartoci z ENUMA
             return "IN_TEXT";
         }
         return "ONLY_TRANSLATION";
     }
 
      private void setSubtitleType(String value){
         consoleLog(value);
         if("IN_TEXT".equals(value)){  //TODO refactor powinno czytac te wartoci z ENUMA
             consoleLog("in if intext");
             onSubtitleTypeInTextClick(null);
         }else{
             consoleLog("in else intext");
             onSubtitleTypeOnlyTranlsationClick(null);
         }
     }
 
 
     native void consoleLog( String message) /*-{
       console.log( "me:" + message );
     }-*/;
 
 
     @UiHandler("min")
     public void changeSlider(ChangeEvent e) {
         max.setValue(min.getValue());
     }
 
     @UiHandler("subtitlePreview")
     public void subtitlePreviewClick(ClickEvent e) {
        String exampleSubtitle = "<div style='color:white; background-color: black;'>This is example## subtitle.</div>";
         String pattern = subtitleTemplate.getText().replace("@@TRANSLATED_TEXT@@","przykład");
         exampleSubtitle = exampleSubtitle.replace("##",pattern);
         getController().getMainDialog().infoPopup("Preview", exampleSubtitle);
     }
 
     @UiHandler("phrasalVerbPreview")
     public void phrasalVerbPreviewClick(ClickEvent e) {
        String example = "<div style='color:white; background-color: black;'>I give up!<br /> ##</div>";
         String pattern = phrasalVerbTemplate.getText().replace("@@TRANSLATED_TEXT@@","give up = poddawać się");
         example = example.replace("##",pattern);
         getController().getMainDialog().infoPopup("Preview", example);
     }
 //
     @UiHandler("restore")
     public void restore(ClickEvent e) {
         max.setValue(String.valueOf(89));
         min.setValue(String.valueOf(89));
         subtitleTemplate.setText("<font color=\"yellow\">(@@TRANSLATED_TEXT@@)</font>");
         phrasalVerbTemplate.setText("<font color=\"red\">@@TRANSLATED_TEXT@@</font>");
     }
 
     @UiHandler("max")
     public void changeMax(ChangeEvent e) {
         min.setValue(max.getValue());
     }
 
     @UiHandler("save")
     public void saveClick(ClickEvent e) {
         JSONObject jsonObject = new JSONObject();
 //       jsonObject.put(TEXT_TEMPLATE, new JSONString(textTemplate.getText()));
         jsonObject.put(SUBTITLE_TEMPLATE, new JSONString(subtitleTemplate.getText()));
         jsonObject.put(PHRASAL_VERB_TEMPLATE, new JSONString(phrasalVerbTemplate.getText()));
         jsonObject.put(PHRASAL_VERB, new JSONString(phrasalVerb.getValue().toString()));
 
         jsonObject.put(MIN, new JSONString(String.valueOf(5)));
         jsonObject.put(MAX, new JSONString(max.getText()));
         jsonObject.put(SUBTITLE_TYPE, new JSONString(getSubtitleType()));
 
         saveOptions(jsonObject.toString());
     }
 
     public void initOptions(JavaScriptObject jsObject) {
         JSONObject jsonObject = new JSONObject(jsObject);
         min.setValue(jsonObject.get(MAX).isNumber().toString());
         max.setValue(jsonObject.get(MAX).isNumber().toString());
 //        textTemplate.setText(jsonObject.get(TEXT_TEMPLATE).isString().stringValue());
         subtitleTemplate.setText(jsonObject.get(SUBTITLE_TEMPLATE).isString().stringValue());
         phrasalVerbTemplate.setText(jsonObject.get(PHRASAL_VERB_TEMPLATE).isString().stringValue());
         phrasalVerb.setValue(jsonObject.get(PHRASAL_VERB).isBoolean().booleanValue());
 
         setSubtitleType(jsonObject.get(SUBTITLE_TYPE).isString().stringValue());
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
