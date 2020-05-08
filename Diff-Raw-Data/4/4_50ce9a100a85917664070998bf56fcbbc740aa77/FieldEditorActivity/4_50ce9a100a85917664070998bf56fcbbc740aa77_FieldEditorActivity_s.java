 package com.radicaldynamic.gcmobile.android.build;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.text.method.DigitsKeyListener;
 import android.text.method.QwertyKeyListener;
 import android.text.method.TextKeyListener;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.xform.Field;
 import com.radicaldynamic.groupinform.xform.XForm;
 
 public class FieldEditorActivity extends Activity
 {
     private static final String t = "FormBuilderElementEditor: ";
     
     public static final String KEY_FIELDTYPE = "fieldtype";
     public static final String KEY_SELECTDEFAULT = "selectinstancedefault";
     
     private static final int REQUEST_ITEMLIST = 1;
     private static final int REQUEST_TRANSLATIONS = 2;
     
     private static final int MENU_ADVANCED = Menu.FIRST;
     private static final int MENU_ITEMS = Menu.FIRST + 1;
     private static final int MENU_HELP = Menu.FIRST + 2;
     
     private AlertDialog mAlertDialog;
     
     private Field mField = null;
     private String mFieldType = null;
     
     // Header
     private TextView mHeaderType;
     private ImageView mHeaderIcon;
     
     // Common input elements
     private EditText mLabel;
     private Button   mLabelI18n;
     private EditText mHint;
     private Button   mHintI18n;
     private EditText mDefaultValue;
     private CheckBox mReadonly;
     private CheckBox mRequired;
     
     // Special hack to deal with the added complexity of select fields
     private String mSelectInstanceDefault = "";
     private ArrayAdapter<CharSequence> mSelectAppearanceSingleOptions;
     private ArrayAdapter<CharSequence> mSelectAppearanceMultipleOptions;
     
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.fb_field_editor);  
         
         // Retrieve field (if any)
         mField = Collect.getInstance().getFormBuilderState().getField();
 
         // Create a new field if one is needed (further init will occur in the field-specific method)
         if (mField == null)
             mField = new Field();
         
         // If there is no instance state (e.g., this activity was loaded by another/this is not a flip)
         if (savedInstanceState == null) {           
             Intent i = getIntent();
             mFieldType = i.getStringExtra(KEY_FIELDTYPE);
             
             // We store off the instance default string for select types (this is special)
             if (mFieldType.equals("select"))
                 mSelectInstanceDefault = mField.getInstance().getDefaultValue();
         } else {
             if (savedInstanceState.containsKey(KEY_FIELDTYPE))
                 mFieldType = savedInstanceState.getString(KEY_FIELDTYPE);
             
             if (savedInstanceState.containsKey(KEY_SELECTDEFAULT))
                 mSelectInstanceDefault = savedInstanceState.getString(KEY_SELECTDEFAULT);
         }
 
         // Set up header
         mHeaderType = (TextView) findViewById(R.id.headerType);
         mHeaderIcon = (ImageView) findViewById(R.id.headerIcon);
         
         // Get a handle on common input elements
         mLabel          = (EditText) findViewById(R.id.label);
         mLabelI18n      = (Button)   findViewById(R.id.labelI18n);
         mHint           = (EditText) findViewById(R.id.hint);
         mHintI18n       = (Button)   findViewById(R.id.hintI18n);
         mDefaultValue   = (EditText) findViewById(R.id.defaultValue);
         mReadonly       = (CheckBox) findViewById(R.id.readonly);
         mRequired       = (CheckBox) findViewById(R.id.required);
         
         // New strings in either the label or hint should begin with a capital by default
         mLabel.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.SENTENCES, false));
         mHint.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.SENTENCES, false));
         
         // Access translations for label & hints
         mLabelI18n.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 Intent i = new Intent(FieldEditorActivity.this, I18nList.class);
                 i.putExtra(I18nList.KEY_FIELDTEXT_TYPE, I18nList.KEY_LABEL);
                 i.putExtra(I18nList.KEY_TRANSLATION_ID, mField.getLabel().getRef());                
                 startActivityForResult(i, REQUEST_TRANSLATIONS);
             }
         });
         
         mHintI18n.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 Intent i = new Intent(new Intent(FieldEditorActivity.this, I18nList.class));
                 i.putExtra(I18nList.KEY_FIELDTEXT_TYPE, I18nList.KEY_DESCRIPTION);
                 i.putExtra(I18nList.KEY_TRANSLATION_ID, mField.getHint().getRef());                
                 startActivityForResult(i, REQUEST_TRANSLATIONS);             
             }
         });
         
         // Set up listener to detect changes to read-only input element
         mReadonly.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {                
                 if (((CheckBox) v).isChecked())
                     mRequired.setChecked(false);                 
             }
         });
         
         // Set up listener to detect changes to required input element
         mRequired.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {                
                 if (((CheckBox) v).isChecked())
                     mReadonly.setChecked(false);                 
             }
         });
         
         // Prepare adapters for select appearance options
         mSelectAppearanceSingleOptions = 
             ArrayAdapter.createFromResource(this, R.array.tf_select_appearance_single_options, android.R.layout.simple_spinner_item);        
         mSelectAppearanceSingleOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
         mSelectAppearanceMultipleOptions = 
             ArrayAdapter.createFromResource(this, R.array.tf_select_appearance_multiple_options, android.R.layout.simple_spinner_item);        
         mSelectAppearanceMultipleOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
         if (mFieldType.equals("barcode"))         loadBarcodeElement();
         else if (mFieldType.equals("date"))       loadDateElement();                
         else if (mFieldType.equals("dateTime"))   loadDateElement();
         else if (mFieldType.equals("draw"))       loadDrawElement();        // Note: draw is a virtual type - it will be turned into media once created
         else if (mFieldType.equals("geopoint"))   loadGeopointElement();                  
         else if (mFieldType.equals("group"))      loadGroupElement();    
        else if (mFieldType.equals("media") && mField.getAttributes().get(XForm.Attribute.MEDIA_TYPE).contains("draw"))
                                                   loadDrawElement();
         else if (mFieldType.equals("media"))      loadMediaElement();                    
         else if (mFieldType.equals("number"))     loadNumberElement();                    
         else if (mFieldType.equals("select"))     loadSelectElement();
         else if (mFieldType.equals("time"))       loadDateElement();
         else if (mFieldType.equals("text"))       loadTextElement();                    
         else {            
             Toast.makeText(getApplicationContext(), getString(R.string.tf_unable_to_edit_unknown_field_type), Toast.LENGTH_LONG).show();
             if (Collect.Log.WARN) Log.w(Collect.LOGTAG, t + "unhandled field type");
             finish();
         }
 
         /*
          * Set header icon
          * 
          * TODO: figure out a better way to do with without duplicating code from FormBuilderFieldListAdapter
          */        
         if (mField.getType().equals("group")) {
             mHeaderIcon.setImageDrawable(getDrawable(R.drawable.element_group));            
         } else if (mField.getType().equals("input")) {
             Drawable icon = getDrawable(R.drawable.element_string);
             
             try {
                 String specificType = mField.getBind().getType();
                 
                 if (specificType.equals("barcode"))     icon = getDrawable(R.drawable.element_barcode);     else
                 if (specificType.equals("date"))        icon = getDrawable(R.drawable.element_calendar);    else
                 if (specificType.equals("dateTime"))    icon = getDrawable(R.drawable.element_calendar);    else
                 if (specificType.equals("decimal"))     icon = getDrawable(R.drawable.element_number);      else
                 if (specificType.equals("geopoint"))    icon = getDrawable(R.drawable.element_location);    else
                 if (specificType.equals("int"))         icon = getDrawable(R.drawable.element_number);      else
                 if (specificType.equals("time"))        icon = getDrawable(R.drawable.element_calendar);
             } catch (NullPointerException e){
                 // TODO: is this really a problem?    
             } finally {
                 mHeaderIcon.setImageDrawable(icon);
             }            
         } else if (mField.getType().equals("repeat")) { 
             mHeaderIcon.setImageDrawable(getDrawable(R.drawable.element_group));          
         } else if (mField.getType().equals("select")) {
             mHeaderIcon.setImageDrawable(getDrawable(R.drawable.element_selectmulti));
         } else if (mField.getType().equals("select1")) {
             mHeaderIcon.setImageDrawable(getDrawable(R.drawable.element_selectsingle));         
         } else if (mField.getType().equals("upload")) {
             if (mField.getAttributes().get(XForm.Attribute.MEDIA_TYPE).contains("draw"))
                 mHeaderIcon.setImageDrawable(getDrawable(R.drawable.element_draw));        
             else
                 mHeaderIcon.setImageDrawable(getDrawable(R.drawable.element_media));
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         switch (requestCode) {
         case REQUEST_ITEMLIST:
             if (resultCode == RESULT_OK) {
                 // Preselected list items may have changed, store off this list
                 ArrayList<String> defaults = new ArrayList<String>();
                 
                 Iterator<Field> it = Collect.getInstance().getFormBuilderState().getField().getChildren().iterator();
                 
                 while (it.hasNext()) {
                     Field item = it.next();
 
                     // If an item is marked as a default (preselected)
                     if (item.isItemDefault()) {
                         defaults.add(item.getItemValue());
                         item.setItemDefault(false);
                     }
                 }
                 
                 /* 
                  * This will either be processed by saveSelectElement() or sent back to 
                  * FormBuilderSelectItemList by way of onOptionsItemSelected() 
                  */
                 mSelectInstanceDefault = defaults.toString().replaceAll(",\\s", " ").replaceAll("[\\[\\]]", "");
             }
             
             break;
             
         // User may have adjusted translations or removed them altogether, refresh accordingly
         case REQUEST_TRANSLATIONS:
             if (resultCode == RESULT_OK) {
                 if (mField.getLabel().isTranslated())
                     toggleEditText(mLabel, false);
                 else
                     toggleEditText(mLabel, true);
 
                 if (mField.getHint().isTranslated())
                     toggleEditText(mHint, false);
                 else
                     toggleEditText(mHint, true);
 
                 mLabel.setText(mField.getLabel().toString());        
                 mHint.setText(mField.getHint().toString());
             }
             
             break;
         }
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         super.onCreateOptionsMenu(menu);
         
         menu.add(0, MENU_ITEMS, 0, getString(R.string.tf_list_items))
             .setIcon(R.drawable.ic_menu_mark)
             .setEnabled(mFieldType.equals("select") ? true : false);        
 
         return true;
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         switch (keyCode) {
         case KeyEvent.KEYCODE_BACK:
             // Save and exit
             if (saveChanges()) {
                 setResult(RESULT_OK);
                 finish();
             } else {
 	        Toast.makeText(getApplicationContext(), getString(R.string.tf_unable_to_save_unknown_field_type), Toast.LENGTH_LONG).show();
 	        finish();
             }
             
             return true;
         }
 
         return super.onKeyDown(keyCode, event);
     }    
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId()) {
         // TODO: launch the advanced properties editor
         case MENU_ADVANCED:
             break;
          
         // Launch the form builder select item editor
         case MENU_ITEMS:
             Intent i = new Intent(this, SelectFieldList.class);            
             /* 
              * Use the state of the select radio option to determine whether to indicate to
              * the select item list which mode the select list is operating in. 
              * 
              * This makes sense because the user may have switched select modes but may
              * not have saved the field yet, so we cannot determine this from the field itself.
              */
             final CheckBox optionMultiple = (CheckBox) findViewById(R.id.selectFieldMultiple);
             i.putExtra(SelectFieldList.KEY_SINGLE, !optionMultiple.isChecked());            
             i.putExtra(SelectFieldList.KEY_DEFAULT, mSelectInstanceDefault);
             startActivityForResult(i, REQUEST_ITEMLIST);
             break;
             
          // TODO: display field-specific help text
         case MENU_HELP:
             break;
         }
     
         return super.onOptionsItemSelected(item);
     }    
 
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
         super.onSaveInstanceState(outState);
         outState.putString(KEY_FIELDTYPE, mFieldType);
         outState.putString(KEY_SELECTDEFAULT, mSelectInstanceDefault);
         
         // Save this specific field state for orientation changes & select item editor
         Collect.getInstance().getFormBuilderState().setField(mField);
     }
     
     // See loadSelectElement() for further information on this dialog
     private void createSelectChangeDialog()
     {        
         mAlertDialog = new AlertDialog.Builder(this)
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_alert)
             .setTitle(R.string.tf_change_select_type)
             .setMessage(R.string.tf_change_select_type_msg)            
             .setPositiveButton(R.string.tf_yes, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     mHeaderIcon.setImageDrawable(getDrawable(R.drawable.element_selectsingle));
                     Spinner optionAppearance = (Spinner) findViewById(R.id.selectFieldAppearance);
                     optionAppearance.setAdapter(mSelectAppearanceSingleOptions);
                     mSelectInstanceDefault = "";
                 }
             })
             .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     // Return to a multiple select field type
                     CheckBox optionMultiple = (CheckBox) findViewById(R.id.selectFieldMultiple);
                     optionMultiple.setChecked(true);
                     dialog.cancel();
                 }
             }).create();
         
         mAlertDialog.show();
     }
     
     /*
      * The field editor layout file includes all possible input elements for all field types.
      * Since this will not make sense to the end user our approach is to simply hide unneeded
      * fields based on the human field type that was passed to this activity when it was started.
      * 
      * This method exists to make the hiding of input elements easier.
      */
     private void disableFormComponent(int componentResource)
     {
         ViewGroup component = (ViewGroup) findViewById(componentResource);
         component.setVisibility(View.GONE);
     }
     
     // Convenience method
     private Drawable getDrawable(int image)
     {
         return getResources().getDrawable(image);
     }
     
     /*
      * Initialize input elements that are likely to appear for all field types.
      * This should be done AFTER any primary initialization of newly created fields.
      */
     private void loadCommonAttributes()
     {
         if (mField.getLabel().isTranslated())
             toggleEditText(mLabel, false);
 
         if (mField.getHint().isTranslated())
             toggleEditText(mHint, false);
         
         mLabel.setText(mField.getLabel().toString());        
         mHint.setText(mField.getHint().toString());
         
         mDefaultValue.setText(mField.getInstance().getDefaultValue());
 
         if (mField.getBind().isReadonly())
             mReadonly.setChecked(true);
 
         if (mField.getBind().isRequired())
             mRequired.setChecked(true);
     }
     
     private void loadBarcodeElement()
     {
         updateTitle(getString(R.string.tf_element_barcode));
         
         // Further initialize newly created fields
         if (mField.isEmpty()) {
             mField.setType("input");                    
             mField.getBind().setType(mFieldType);
             mField.setEmpty(false);
         }
         
         loadCommonAttributes();
         
         disableFormComponent(R.id.dateFieldTypeSelection);
         disableFormComponent(R.id.drawFieldTypeSelection);
         disableFormComponent(R.id.geopointFieldTypeSelection);
         disableFormComponent(R.id.groupFieldTypeSelection);
         disableFormComponent(R.id.mediaFieldTypeSelection);
         disableFormComponent(R.id.numberFieldTypeSelection);
         disableFormComponent(R.id.selectFieldTypeSelection);
         disableFormComponent(R.id.readonlyLayout);
     }
     
     private void loadDateElement()
     {
         updateTitle(getString(R.string.tf_element_date));
         
         // Further initialize newly created fields
         if (mField.isEmpty()) {
             mField.setType("input");                    
             mField.getBind().setType(mFieldType);
             mField.setEmpty(false);
         }
         
         loadCommonAttributes();
      
         disableFormComponent(R.id.drawFieldTypeSelection);
         disableFormComponent(R.id.geopointFieldTypeSelection);
         disableFormComponent(R.id.groupFieldTypeSelection);
         disableFormComponent(R.id.mediaFieldTypeSelection);
         disableFormComponent(R.id.numberFieldTypeSelection);
         disableFormComponent(R.id.selectFieldTypeSelection);
         
         final RadioButton dateOnly = (RadioButton) findViewById(R.id.dateTypeDateOnly);
         final RadioButton timeOnly = (RadioButton) findViewById(R.id.dateTypeTimeOnly);
         final RadioButton dateAndTime = (RadioButton) findViewById(R.id.dateTypeDateAndTime);
 
         if (mField.getBind().getType().equals("date"))
             dateOnly.setChecked(true);
         else if (mField.getBind().getType().equals("time"))
             timeOnly.setChecked(true);
         else if (mField.getBind().getType().equals("dateTime"))
             dateAndTime.setChecked(true);
 
         // TODO: we should probably display a "default" date using the date widget
     }
     
     private void loadDrawElement()
     {
         updateTitle(getString(R.string.tf_element_draw));
         
         // "draw" is really a special kind of media field for which we want a different interface
         mFieldType = "media";
         
         // Further initialize newly created fields
         if (mField.isEmpty()) {
             mField.setType("upload");
             mField.getAttributes().put(XForm.Attribute.MEDIA_TYPE, "draw/*");
             mField.getBind().setType("binary");            
             mField.setEmpty(false);
         }
         
         loadCommonAttributes();
         
         disableFormComponent(R.id.dateFieldTypeSelection);
         disableFormComponent(R.id.defaultValueInput);
         disableFormComponent(R.id.geopointFieldTypeSelection);
         disableFormComponent(R.id.groupFieldTypeSelection);
         disableFormComponent(R.id.mediaFieldTypeSelection);
         disableFormComponent(R.id.numberFieldTypeSelection);
         disableFormComponent(R.id.selectFieldTypeSelection);
         
 //        final RadioButton drawAnnotate = (RadioButton) findViewById(R.id.drawTypeAnnotate);
         final RadioButton drawSignature = (RadioButton) findViewById(R.id.drawTypeSignature);
         final RadioButton drawSketch = (RadioButton) findViewById(R.id.drawTypeSketch);
         
         if (mField.getAttributes().get(XForm.Attribute.APPEARANCE) == null) {
             drawSketch.setChecked(true);
         } else {
             if (mField.getAttributes().get(XForm.Attribute.APPEARANCE).equals("annotate")) {
 //                drawAnnotate.setChecked(true);
             } else if (mField.getAttributes().get(XForm.Attribute.APPEARANCE).equals("signature")) {
                 drawSignature.setChecked(true);
             } else if (mField.getAttributes().get(XForm.Attribute.APPEARANCE).equals("sketch")) {
                 drawSketch.setChecked(true);
             }
         }
     }
     
     private void loadGeopointElement()
     {
         updateTitle(getString(R.string.tf_element_geopoint));
         
         // Further initialize newly created fields
         if (mField.isEmpty()) {
             mField.setType("input");
             mField.getBind().setType(mFieldType);
             mField.setEmpty(false);
         }
         
         if (mField.getAttributes().containsKey(XForm.Attribute.APPEARANCE) &&
                 mField.getAttributes().get(XForm.Attribute.APPEARANCE).equals(XForm.Value.MAP)) {
             CheckBox mapsOption = (CheckBox) findViewById(R.id.geopointMaps);
             mapsOption.setChecked(true);
         }
         
         loadCommonAttributes();
         
         disableFormComponent(R.id.dateFieldTypeSelection);
         disableFormComponent(R.id.defaultValueInput);
         disableFormComponent(R.id.drawFieldTypeSelection);
         disableFormComponent(R.id.groupFieldTypeSelection);        
         disableFormComponent(R.id.mediaFieldTypeSelection);
         disableFormComponent(R.id.numberFieldTypeSelection);
         disableFormComponent(R.id.selectFieldTypeSelection);
         disableFormComponent(R.id.readonlyLayout);
     }
     
     private void loadGroupElement()
     {
         updateTitle(getString(R.string.tf_element_group));
         
         // Further initialize newly created fields
         if (mField.isEmpty()) {
             mField.setType("group");
             mField.setEmpty(false);
         }
         
         loadCommonAttributes();
     
         disableFormComponent(R.id.dateFieldTypeSelection);
         disableFormComponent(R.id.defaultValueInput);
         disableFormComponent(R.id.drawFieldTypeSelection);
         disableFormComponent(R.id.geopointFieldTypeSelection);
         disableFormComponent(R.id.hintInput);
         disableFormComponent(R.id.mediaFieldTypeSelection);
         disableFormComponent(R.id.numberFieldTypeSelection);
         disableFormComponent(R.id.selectFieldTypeSelection);
         disableFormComponent(R.id.readonlyLayout);
         disableFormComponent(R.id.requiredLayout);
         
         final RadioButton groupRegular = (RadioButton) findViewById(R.id.groupTypeRegular);
         final RadioButton groupRepeated = (RadioButton) findViewById(R.id.groupTypeRepeated);
         final RadioButton groupScreen = (RadioButton) findViewById(R.id.groupTypeScreen);
         
         // Initialize group type selection
         if (Field.isRepeatedGroup(mField)) {
             groupRepeated.setChecked(true);
         } else {
             if (mField.getAttributes().containsKey(XForm.Attribute.APPEARANCE) &&
                     mField.getAttributes().get(XForm.Attribute.APPEARANCE).equals(XForm.Value.FIELD_LIST))
             {
                 groupScreen.setChecked(true);
             } else {
                 groupRegular.setChecked(true);
             }
         }
     }
     
     private void loadMediaElement()
     {
         updateTitle(getString(R.string.tf_element_media));
         
         // Further initialize newly created fields
         if (mField.isEmpty()) {
             mField.setType("upload");
             mField.getBind().setType("binary");
             mField.getAttributes().put(XForm.Attribute.MEDIA_TYPE, "image/*");
             mField.setEmpty(false);
         }
         
         loadCommonAttributes();
 
         disableFormComponent(R.id.dateFieldTypeSelection);
         disableFormComponent(R.id.defaultValueInput);
         disableFormComponent(R.id.drawFieldTypeSelection);
         disableFormComponent(R.id.geopointFieldTypeSelection);
         disableFormComponent(R.id.groupFieldTypeSelection);        
         disableFormComponent(R.id.numberFieldTypeSelection);
         disableFormComponent(R.id.selectFieldTypeSelection);
         disableFormComponent(R.id.readonlyLayout);        
         
         // Set up listener for radio buttons so that they influence the field type
         OnClickListener radioListener = new OnClickListener() {
             public void onClick(View v) {
                 RadioButton rb = (RadioButton) v;
                 
                 switch (rb.getId()) {
                 case R.id.mediaTypeAudio: break;                    
                 case R.id.mediaTypeImage: break;
                 case R.id.mediaTypeVideo: break;
                 }
             }
         };
         
         final RadioButton radioAudio = (RadioButton) findViewById(R.id.mediaTypeAudio);
         final RadioButton radioImage = (RadioButton) findViewById(R.id.mediaTypeImage);
         final RadioButton radioVideo = (RadioButton) findViewById(R.id.mediaTypeVideo);
         
         radioAudio.setOnClickListener(radioListener);
         radioImage.setOnClickListener(radioListener);
         radioVideo.setOnClickListener(radioListener);
         
         // Initialize media type selection
         if (mField.getAttributes().get(XForm.Attribute.MEDIA_TYPE).equals("audio/*"))
             radioAudio.setChecked(true);
         else if (mField.getAttributes().get(XForm.Attribute.MEDIA_TYPE).equals("image/*"))
             radioImage.setChecked(true);
         else if (mField.getAttributes().get(XForm.Attribute.MEDIA_TYPE).equals("video/*"))
             radioVideo.setChecked(true);
     }
     
     private void loadNumberElement()
     {
         updateTitle(getString(R.string.tf_element_number));
         
         // Further initialize newly created fields
         if (mField.isEmpty()) {
             mField.setType("input");
             mField.getBind().setType("int");            
             mField.setEmpty(false);
         }
         
         loadCommonAttributes();
         
         disableFormComponent(R.id.dateFieldTypeSelection);
         disableFormComponent(R.id.drawFieldTypeSelection);
         disableFormComponent(R.id.geopointFieldTypeSelection);
         disableFormComponent(R.id.groupFieldTypeSelection);
         disableFormComponent(R.id.mediaFieldTypeSelection);
         disableFormComponent(R.id.selectFieldTypeSelection);
         
         final EditText defaultValue = (EditText) findViewById(R.id.defaultValue);
         
         // Set up listener for radio buttons so that they influence the field type
         OnClickListener radioListener = new OnClickListener() {
             public void onClick(View v) {
                 RadioButton rb = (RadioButton) v;
                 
                 switch (rb.getId()) {
                 case R.id.numberTypeInteger:
                     defaultValue.setKeyListener(new DigitsKeyListener(false, false));
                     
                     // Remove any occurrences of a decimal
                     if (defaultValue.getText().toString().contains(".")) {
                         String txt = defaultValue.getText().toString();                        
                         defaultValue.setText(txt.replace(".", ""));
                     }
                     
                     break;
                     
                 case R.id.numberTypeDecimal:                                   
                     defaultValue.setKeyListener(new DigitsKeyListener(false, true)); 
                     break;                    
                 }
             }
         };
         
         final RadioButton radioInteger = (RadioButton) findViewById(R.id.numberTypeInteger);
         final RadioButton radioDecimal = (RadioButton) findViewById(R.id.numberTypeDecimal);
         
         radioInteger.setOnClickListener(radioListener);
         radioDecimal.setOnClickListener(radioListener);
         
         // Initialize number type selection
         if (mField.getBind().getType().equals("int")) { 
             radioInteger.setChecked(true);
             // false, false supports only integer input
             defaultValue.setKeyListener(new DigitsKeyListener(false, false));
         } else {
             radioDecimal.setChecked(true);
             // false, true supports decimal input
             defaultValue.setKeyListener(new DigitsKeyListener(false, true));
         }
     }
     
     private void loadSelectElement()
     {
         updateTitle(getString(R.string.tf_element_select));
         
         // Further initialize newly created fields
         if (mField.isEmpty()) {
             mField.setType(mFieldType);
             mField.getBind().setType(mFieldType);     
             mField.setEmpty(false);
         }
         
         loadCommonAttributes();
         
         disableFormComponent(R.id.dateFieldTypeSelection);
         disableFormComponent(R.id.defaultValueInput);
         disableFormComponent(R.id.drawFieldTypeSelection);
         disableFormComponent(R.id.geopointFieldTypeSelection);
         disableFormComponent(R.id.groupFieldTypeSelection);        
         disableFormComponent(R.id.mediaFieldTypeSelection);
         disableFormComponent(R.id.numberFieldTypeSelection);
         disableFormComponent(R.id.readonlyLayout);
         
         final CheckBox optionMultiple = (CheckBox) findViewById(R.id.selectFieldMultiple);
         final Spinner optionAppearance = (Spinner) findViewById(R.id.selectFieldAppearance);
         
         // Set up listener to detect changes to read-only input element
         optionMultiple.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {                
                 if (((CheckBox) v).isChecked()) {
                     mHeaderIcon.setImageDrawable(getDrawable(R.drawable.element_selectmulti));
                     optionAppearance.setAdapter(mSelectAppearanceMultipleOptions);
                 } else {
                     /* 
                      * Single selects may only have one preselected default.  This presents a problem
                      * if the user is switching from a multiple to a single default and requires user
                      * intervention.
                      */
                     if (mSelectInstanceDefault.split("\\s+").length > 1) {
                         createSelectChangeDialog();
                     } else {
                         mHeaderIcon.setImageDrawable(getDrawable(R.drawable.element_selectsingle));
                         optionAppearance.setAdapter(mSelectAppearanceSingleOptions);
                     }
                 }
             }
         });
 
         // Initialize select type 
         if (mField.getType().equals("select")) {
             optionMultiple.setChecked(true);
             optionAppearance.setAdapter(mSelectAppearanceMultipleOptions);
         } else {
             optionMultiple.setChecked(false);
             optionAppearance.setAdapter(mSelectAppearanceSingleOptions);
         }
         
         // Default appearance
         optionAppearance.setSelection(0);
 
         // Switch to selected appearance option
         if (mField.getAttributes().get(XForm.Attribute.APPEARANCE) != null) {
             if (mField.getAttributes().get(XForm.Attribute.APPEARANCE).equals(XForm.Value.MINIMAL)) {
                 optionAppearance.setSelection(1);
             } else if (mField.getAttributes().get(XForm.Attribute.APPEARANCE).equals(XForm.Value.LIST)) {
                 optionAppearance.setSelection(2);
             } else if (mField.getAttributes().get(XForm.Attribute.APPEARANCE).equals(XForm.Value.LIST_NOLABEL)) {
                 optionAppearance.setSelection(3);
             } else if (mField.getAttributes().get(XForm.Attribute.APPEARANCE).equals(XForm.Value.QUICK)) {
                 optionAppearance.setSelection(4);
             } else if (mField.getAttributes().get(XForm.Attribute.APPEARANCE).equals(XForm.Value.AUTOCOMPLETE)) {
                 optionAppearance.setSelection(5);
             }
         }
     }
     
     private void loadTextElement()
     {
         updateTitle(getString(R.string.tf_element_text));
         
         // Further initialize newly created fields
         if (mField.isEmpty()) {
             mField.setType("input");
             mField.getBind().setType("string"); 
             mField.setEmpty(false);
         }
         
         loadCommonAttributes();
         
         disableFormComponent(R.id.dateFieldTypeSelection);
         disableFormComponent(R.id.drawFieldTypeSelection);
         disableFormComponent(R.id.geopointFieldTypeSelection);
         disableFormComponent(R.id.groupFieldTypeSelection);
         disableFormComponent(R.id.mediaFieldTypeSelection);
         disableFormComponent(R.id.numberFieldTypeSelection);
         disableFormComponent(R.id.selectFieldTypeSelection);
     } 
     
     /*
      * Save any changes that the user has made to the form field
      */
     private boolean saveChanges()
     {
         // Ensure that a label is present
         if (mLabel.getText().toString().replaceAll("\\s+", "").length() == 0) {            
             mLabel.setText("No label for " + mFieldType);
         }
         
         // Save common attributes
         if (mField.getLabel().isTranslated()) {
             // Clear out any value that may have been stored
             mField.getLabel().setValue(null);
         } else {
             mField.setLabel(mLabel.getText().toString().trim());   
         }
         
         if (mField.getHint().isTranslated()) {
             // Clear out any value that may have been stored
             mField.getHint().setValue(null);
         } else {
             mField.setHint(mHint.getText().toString().trim());    
         }
                 
         mField.getInstance().setDefaultValue(mDefaultValue.getText().toString().trim());        
         
         if (mReadonly.isChecked())
             mField.getBind().setReadonly(true);
         else 
             mField.getBind().setReadonly(false);
             
         if (mRequired.isChecked())
             mField.getBind().setRequired(true);
         else 
             mField.getBind().setRequired(false);
         
         // Save (control) field-specific properties 
         if (mFieldType.equals("barcode"))         saveBarcodeElement();                    
         else if (mFieldType.equals("date"))       saveDateElement();
         else if (mFieldType.equals("dateTime"))   saveDateElement();
         else if (mFieldType.equals("geopoint"))   saveGeopointElement();                  
         else if (mFieldType.equals("group"))      saveGroupElement();
         else if (mFieldType.equals("media") && mField.getAttributes().get(XForm.Attribute.MEDIA_TYPE).contains("draw"))
                                                   saveDrawElement();
         else if (mFieldType.equals("media"))      saveMediaElement();                    
         else if (mFieldType.equals("number"))     saveNumberElement();                    
         else if (mFieldType.equals("select"))     saveSelectElement();                    
         else if (mFieldType.equals("text"))       saveTextElement();
         else if (mFieldType.equals("time"))       saveDateElement();
         else {
 	    if (Collect.Log.WARN) Log.w(Collect.LOGTAG, t + "unhandled field type");
             return false;
         }
         
         // Mark the field as having been saved
         mField.setSaved(true);
         
         Collect.getInstance().getFormBuilderState().setField(mField);
         
         return true;
     }
     
     private void saveBarcodeElement() 
     {
 
     }
     
     private void saveDateElement()
     {
         final RadioButton dateOnly = (RadioButton) findViewById(R.id.dateTypeDateOnly);
         final RadioButton timeOnly = (RadioButton) findViewById(R.id.dateTypeTimeOnly);
         final RadioButton dateAndTime = (RadioButton) findViewById(R.id.dateTypeDateAndTime);
         
         if (dateOnly.isChecked()) {
             mField.getBind().setType("date");
         } else if (timeOnly.isChecked()) {
             mField.getBind().setType("time");
         } else if (dateAndTime.isChecked()) {
             mField.getBind().setType("dateTime");
         }
     }
     
     private void saveDrawElement()
     {
 //      final RadioButton drawAnnotate = (RadioButton) findViewById(R.id.drawTypeAnnotate);
         final RadioButton drawSignature = (RadioButton) findViewById(R.id.drawTypeSignature);
         final RadioButton drawSketch = (RadioButton) findViewById(R.id.drawTypeSketch);
         
         if (drawSignature.isChecked()) {
             mField.getAttributes().put(XForm.Attribute.APPEARANCE, "signature");
         } else if (drawSketch.isChecked()) {
             mField.getAttributes().put(XForm.Attribute.APPEARANCE, "sketch");
         }
     }
     
     private void saveGeopointElement()
     {
         CheckBox mapsOption = (CheckBox) findViewById(R.id.geopointMaps);
         
         if (mapsOption.isChecked()) {
             mField.getAttributes().put(XForm.Attribute.APPEARANCE, XForm.Value.MAP);
         } else {
             mField.getAttributes().remove(XForm.Attribute.APPEARANCE);
         }
     }
 
     /*
      * FIXME: update children references after changing between group types
      */
     private void saveGroupElement()
     {
         final RadioButton groupRegular = (RadioButton) findViewById(R.id.groupTypeRegular);
         final RadioButton groupRepeated = (RadioButton) findViewById(R.id.groupTypeRepeated);
         final RadioButton groupScreen = (RadioButton) findViewById(R.id.groupTypeScreen);
         
         if (groupRegular.isChecked()) {
             // Changing from a repeated group to a regular group involves work
             if (Field.isRepeatedGroup(mField)) {
                 // Move any children of the repeated group to the regular group
                 mField.getChildren().addAll(mField.getRepeat().getChildren());
                 
                 // Remove the repeat tag itself
                 if (mField.getChildren().size() > 0)
                     mField.getChildren().remove(0);
             }
         } else if (groupRepeated.isChecked()) {
             // Changing from a regular group to a repeated group involves work
             if (!Field.isRepeatedGroup(mField)) {
                 ArrayList<Field> regularGroupChildren = new ArrayList<Field>();
                 
                 // Store off the children and remove them from the group
                 if (!mField.getChildren().isEmpty()) {                                        
                     regularGroupChildren = mField.getChildren();
                     mField.getChildren().clear();
                 }
                     
                 // Create the repeat field and add it to the group
                 Field repeat = new Field();
                 repeat.setType("repeat");
 
                 mField.getChildren().add(repeat);
                 mField.getRepeat().setParent(mField);
 
                 if (!regularGroupChildren.isEmpty())
                     mField.getRepeat().getChildren().addAll(regularGroupChildren);
             }
         }
 
         if (groupScreen.isChecked()) {
             mField.getAttributes().put(XForm.Attribute.APPEARANCE, XForm.Value.FIELD_LIST);
         } else {
             // Make sure the multiple questions per screen attribute is cleared
             if (mField.getAttributes().containsKey(XForm.Attribute.APPEARANCE)) {
                 mField.getAttributes().remove(XForm.Attribute.APPEARANCE);
             }
         }
             
     }
     
     private void saveMediaElement()
     {
         final RadioButton radioAudio = (RadioButton) findViewById(R.id.mediaTypeAudio);
         final RadioButton radioImage = (RadioButton) findViewById(R.id.mediaTypeImage);
         final RadioButton radioVideo = (RadioButton) findViewById(R.id.mediaTypeVideo);
         
         if (radioAudio.isChecked()) {
             mField.getAttributes().put(XForm.Attribute.MEDIA_TYPE, "audio/*");
         } else if (radioImage.isChecked()) {
             mField.getAttributes().put(XForm.Attribute.MEDIA_TYPE, "image/*"); 
         } else if (radioVideo.isChecked()) {
             mField.getAttributes().put(XForm.Attribute.MEDIA_TYPE, "video/*"); 
         }        
     }
     
     private void saveNumberElement()
     {
         final RadioButton radioInteger = (RadioButton) findViewById(R.id.numberTypeInteger);
         
         if (radioInteger.isChecked()) {
             mField.getBind().setType("int");
         } else {
             mField.getBind().setType("decimal");
         }
     }
     
     private void saveSelectElement()
     {
         final CheckBox optionMultiple = (CheckBox) findViewById(R.id.selectFieldMultiple);
         final Spinner optionAppearance = (Spinner) findViewById(R.id.selectFieldAppearance);
         
         if (optionMultiple.isChecked()) {
             mField.setType("select");
             mField.getBind().setType("select");
         } else {
             mField.setType("select1");
             mField.getBind().setType("select1");
         }
         
         switch (optionAppearance.getSelectedItemPosition()) {
         case 0: mField.getAttributes().remove(XForm.Attribute.APPEARANCE); break;
         case 1: mField.getAttributes().put(XForm.Attribute.APPEARANCE, XForm.Value.MINIMAL); break;
         case 2: mField.getAttributes().put(XForm.Attribute.APPEARANCE, XForm.Value.LIST); break;
         case 3: mField.getAttributes().put(XForm.Attribute.APPEARANCE, XForm.Value.LIST_NOLABEL); break;
         case 4: mField.getAttributes().put(XForm.Attribute.APPEARANCE, XForm.Value.QUICK); break;
         case 5: mField.getAttributes().put(XForm.Attribute.APPEARANCE, XForm.Value.AUTOCOMPLETE); break;
         }
                 
         mField.getInstance().setDefaultValue(mSelectInstanceDefault);
     }
     
     private void saveTextElement()
     {
         
     }
     
     private void toggleEditText(EditText v, Boolean b)
     {
         v.setEnabled(b);                    
         v.setFocusable(b);
         v.setFocusableInTouchMode(b);
     }
     
     private void updateTitle(String title)
     {
         mHeaderType.setText(title);
         
         if (mField.isNewField())
             setTitle(getString(R.string.tf_add_new) + " " + title);                   
         else
             setTitle(getString(R.string.tf_edit) + " " + title);                                
     }
 }
