 package uk.org.smithfamily.mslogger.dialog;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.dialog.EditRequiredFuel.OnReqFuelResult;
 import uk.org.smithfamily.mslogger.ecuDef.Constant;
 import uk.org.smithfamily.mslogger.ecuDef.CurveEditor;
 import uk.org.smithfamily.mslogger.ecuDef.DialogField;
 import uk.org.smithfamily.mslogger.ecuDef.DialogPanel;
 import uk.org.smithfamily.mslogger.ecuDef.MSDialog;
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import uk.org.smithfamily.mslogger.ecuDef.TableEditor;
 import uk.org.smithfamily.mslogger.log.DebugLogManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.text.method.DigitsKeyListener;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 
 /**
  *
  */
 public class EditDialog extends Dialog implements android.view.View.OnClickListener
 {
     private MSDialog dialog;
     private RelativeLayout content;
     private Megasquirt ecu;
     private int nbPanels = 0;
     
     // Used on label in table row with no field beside
     // Those are usually used as separator so add top and bottom margins
     private LayoutParams lpSpanWithMargins;
     
     // Used on label in table row with field beside label, add a margin right
     // so the label and field are separated
     private LayoutParams lpWithMargins;
     
     // Regular layout params for dialog row with label and constant
     private LayoutParams lp;
     
     private List<CurveHelper> curveHelpers = new ArrayList<CurveHelper>();
     private List<TableHelper> tableHelpers = new ArrayList<TableHelper>();
     
     /**
      * Constructor for dialog which set the current dialog and ECU object
      * 
      * @param context
      * @param dialog
      */
     public EditDialog(Context context, MSDialog dialog)
     {
         super(context);
         
         this.ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         this.dialog = dialog;
         
         // Initialise some layout params
         lpSpanWithMargins = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         lpSpanWithMargins.setMargins(0, 10, 0, 15);
         lpSpanWithMargins.span = 2;
         
         lpWithMargins = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         lpWithMargins.setMargins(0, 0, 8, 0);
         
         lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
     }
     
     /**
      * @param savedInstanceState
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.editdialog);
         
         content = (RelativeLayout) findViewById(R.id.content);
         
         setTitle(dialog.getLabel());
         
         Button buttonBurn = (Button) findViewById(R.id.burn);
         buttonBurn.setOnClickListener(this);
         
         Button buttonCancel = (Button) findViewById(R.id.cancel);
         buttonCancel.setOnClickListener(this);
         
         drawDialogFields(null, dialog, dialog, false, "", null);
         
         // Hide keyboard
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
     }
 
     /**
      * Add the panel to the layout by looking at the orientation to insert the panel at the right place
      * 
      * @param parentLayout The parent layout the panel to add will be inserted into
      * @param relativeLayoutToAdd The layout to be added with all the dialog fields already there
      * @param orientation The orientation of the new panel
      * @param dialogName The name of the dialog (for debugging purpose only)
      * @param previousPanelLayout An instance of the previous layout added since the new one will be added in relation to the previous one
      */
     private void addPanel(RelativeLayout parentLayout, RelativeLayout relativeLayoutToAdd, String orientation, String dialogName, String dialogAxis, RelativeLayout previousPanelLayout)
     {
         RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
         
         DebugLogManager.INSTANCE.log("PANEL tableLayoutToAdd id (" + dialogName + ") is " + relativeLayoutToAdd.getId(), Log.DEBUG);
         
         // This is not the first panel we add on this dialog
         if (previousPanelLayout != null)
         {
             if (orientation.equals("North"))
             {
                 tlp.addRule(RelativeLayout.ABOVE, previousPanelLayout.getId());
                 relativeLayoutToAdd.setPadding(0, 0, 0, 15);
 
                 DebugLogManager.INSTANCE.log("PANEL " + dialogName + " above " + previousPanelLayout.getTag() + " (" + previousPanelLayout.getId() + ")", Log.DEBUG);
             }
             else if (orientation.equals("South"))
             {
                 tlp.addRule(RelativeLayout.BELOW, previousPanelLayout.getId());
                 relativeLayoutToAdd.setPadding(0, 15, 0, 0);
 
                 DebugLogManager.INSTANCE.log("PANEL " + dialogName + " below " + previousPanelLayout.getTag() + " (" + previousPanelLayout.getId() + ")", Log.DEBUG);
             }
             else if (orientation.equals("West"))
             {
                 tlp.addRule(RelativeLayout.LEFT_OF, previousPanelLayout.getId());
                 relativeLayoutToAdd.setPadding(0, 0, 15, 0);
                 DebugLogManager.INSTANCE.log("PANEL " + dialogName + " at the left of " + previousPanelLayout.getTag() + " (" + previousPanelLayout.getId() + ")", Log.DEBUG);
             }
             else
             {
                 // For yAxis orientation, add panel one under the other
                 if (dialogAxis.equals("yAxis"))
                 {
                     tlp.addRule(RelativeLayout.BELOW, previousPanelLayout.getId());
                     relativeLayoutToAdd.setPadding(0, 15, 0, 0);
                     DebugLogManager.INSTANCE.log("PANEL " + dialogName + " (Dialog axis: " + dialogAxis + ") below " + previousPanelLayout.getTag() + " (" + previousPanelLayout.getId() + ")", Log.DEBUG);
                 }
                 // For xAxis orientation, add panel at the right of the last one
                 else
                 {
                     tlp.addRule(RelativeLayout.RIGHT_OF, previousPanelLayout.getId());
                     relativeLayoutToAdd.setPadding(15, 0, 0, 0);
                     DebugLogManager.INSTANCE.log("PANEL " + dialogName + " (Dialog axis: " + dialogAxis + ") at the right of " + previousPanelLayout.getTag() + " (" + previousPanelLayout.getId() + ")", Log.DEBUG);
                 }
             }
 
             relativeLayoutToAdd.setLayoutParams(tlp);
         }
         else
         {
             DebugLogManager.INSTANCE.log("PANEL " + dialogName + " previousPanelLayout was null!", Log.DEBUG);
         }
         
         // Panel to add have a parent layout, add view to it
         if (parentLayout != null)
         {
             DebugLogManager.INSTANCE.log("PANEL parentLayout is " + parentLayout.getTag(), Log.DEBUG);
             parentLayout.addView(relativeLayoutToAdd);
         }
         // No parent layout, default to main layout
         else
         {
 
             DebugLogManager.INSTANCE.log("PANEL parentLayout is null, adding to main layout", Log.DEBUG);
             content.addView(relativeLayoutToAdd);
         }
         
         nbPanels++;
     }
     
     /**
      * Helper function to wrap a table layout into a relative layout
      * 
      * @param tl The table layout that will be wrapped into a relative layout
      * @param dialogName The dialog name of the panel we want to wrap
      */
     private RelativeLayout wrapTableLayoutIntoRelativeLayout(TableLayout tl, String dialogName)
     {
         RelativeLayout containerPanelLayout = new RelativeLayout(getContext());
         RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
         containerPanelLayout.setLayoutParams(tlp);
         containerPanelLayout.setId(nbPanels);
         containerPanelLayout.setTag(dialogName);
         containerPanelLayout.addView(tl);
         
         return containerPanelLayout;
     }
     
     /**
      * Recursive function that will draw fields and panels of a dialog.
      * It's called recursively until all fields and panels were drawn.
      * 
      * @param parentLayout The parent layout in which the dialog fields should be added
      * @param dialog The dialog object
      * @param parentDialog If it's a panel, this is the instance of the parent dialog
      * @param isPanel true if the dialog object is a panel, false otherwise
      * @param orientation The orientation of the panel
      * @param previousDialogPanelLayout An instance of the previous layout added since the new one will be added in relation to the previous one
      * 
      * @return A relative layout with all fields in it
      */
     private RelativeLayout drawDialogFields(RelativeLayout parentLayout, MSDialog dialog, MSDialog parentDialog, boolean isPanel, String orientation, RelativeLayout previousDialogPanelLayout)
     {
         TableLayout panelLayout = new TableLayout(getContext());
         
         // If it's a panel or at least a dialog with one field or more, we can display its label
         if (isPanel || dialog.getFieldsList().size() > 0)
         {
             showPanelLabel(dialog.getLabel(), panelLayout);
         }
         
         // For each dialog field, add a row in the table layout
         for (DialogField df : dialog.getFieldsList())
         {
             Constant constant = ecu.getConstantByName(df.getName());
 
             if (constant == null && !df.getName().equals("null"))
             {
                 showConstantDoesntExists(df.getName());
             }
             else 
             {
                 TableRow tableRow = new TableRow(getContext());
                 
                 // For empty label or empty field name, we just insert an empty text view as second column of the row
                 if ((df.getLabel().equals("") && df.getName().equals("null")) || df.getName().equals("null"))
                 {
                     // Special label used to identify hard coded required fuel panel in dialog
                     if (df.getLabel().equals("std_required_fuel"))
                     {
                        RelativeLayout requiredFuel = getRequiredFuelPanel();
                        requiredFuel.setLayoutParams(lpSpanWithMargins);
                        tableRow.addView(requiredFuel);
                     }
                     // Special label used to identify hard coded seek bar in std_accel dialog
                     else if (df.getLabel().equals("std_accel_seek_bar"))
                     {
                         RelativeLayout accelSeekBar = getAccelSeekBar();
                         accelSeekBar.setLayoutParams(lpSpanWithMargins);
                         tableRow.addView(accelSeekBar);
                     }
                     else
                     {
                         TextView label = getLabel(df, constant);
                         tableRow.addView(label);
                         
                         // No second column so label is used to separate so make it bold and merge columns
                         label.setTypeface(null, Typeface.BOLD);
                         
                         // If it's not an empty label and not , add some top and bottom margins
                         if (!df.getLabel().equals(""))
                         {
                             label.setLayoutParams(lpSpanWithMargins);
                         }
                     }
                 }
                 // Regular row with label and constant fields
                 else 
                 {
                     TextView label = getLabel(df, constant);
                     tableRow.addView(label);
                     
                     tableRow.setLayoutParams(lp);
                     
                     // Multi-choice constant
                     if (constant.getClassType().equals("bits"))
                     {
                         Spinner spin = buildMultiValuesConstantField(df, constant);
                         
                         tableRow.addView(spin);
                     }
                     // Single value constant
                     else
                     {
                         EditText edit = buildSingleValueConstantField(df, constant);
 
                         tableRow.addView(edit);
                     }
                 }
                 
                 panelLayout.addView(tableRow);
             }           
         }        
         
         // Wrap panel layout into a relative layout so it can be used as parent
         RelativeLayout containerPanelLayout = wrapTableLayoutIntoRelativeLayout(panelLayout, dialog.getName());        
         addPanel(parentLayout, containerPanelLayout, orientation, dialog.getName(), parentDialog.getAxis(), previousDialogPanelLayout); 
         
         RelativeLayout sameDialogPreviousLayoutPanel = null;
         
         // For each dialog panel, add a layout to the dialog
         for (DialogPanel dp : dialog.getPanelsList())
         {
             MSDialog dialogPanel = ecu.getDialogByName(dp.getName());
             
             if (dialogPanel != null)
             {
                 sameDialogPreviousLayoutPanel = drawDialogFields(containerPanelLayout, dialogPanel, dialog, true, dp.getOrientation(), sameDialogPreviousLayoutPanel);
             }
             else
             {
                 // Not a regular dialog, but maybe it's an std_* dialog
                 dialogPanel = DialogHelper.getStdDialog(getContext(), dp.getName());
                 
                 if (dialogPanel != null)
                 {
                     sameDialogPreviousLayoutPanel = drawDialogFields(containerPanelLayout, dialogPanel, dialog, true, dp.getOrientation(), sameDialogPreviousLayoutPanel);
                 }
                 else
                 {
                     // Maybe it's a curve panel
                     CurveEditor curvePanel = ecu.getCurveEditorByName(dp.getName());
                     if (curvePanel != null)
                     {
                         sameDialogPreviousLayoutPanel = createCurvePanel(containerPanelLayout, curvePanel, dp.getOrientation(), sameDialogPreviousLayoutPanel);
                     }
                     else
                     {
                         // Maybe it's a table panel
                         TableEditor tablePanel = ecu.getTableEditorByName(dp.getName());
                         
                         if (tablePanel != null)
                         {
                             sameDialogPreviousLayoutPanel = createTablePanel(containerPanelLayout, tablePanel, dp.getOrientation(), sameDialogPreviousLayoutPanel);
                         }
                         else
                         {
                             DebugLogManager.INSTANCE.log("Invalid panel name " + dp.getName(), Log.DEBUG);
                         }
                     }
                 }
             }
         }
         
         return containerPanelLayout;
     }
     
     /**
      * Inflate the layout for the required fuel panel, do the initialization work and return the layout
      * 
      * @return The relative layout containing the required fuel stuff
      */
     private RelativeLayout getRequiredFuelPanel()
     {
         LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);       
         RelativeLayout requiredFuelLayout = (RelativeLayout) inflater.inflate(R.layout.required_fuel_panel, null);
         
         Button requiredFuelButton = (Button) requiredFuelLayout.findViewById(R.id.bt_required_fuel);
         
         double reqFuel = 0;
         
         // MS1
         if (ecu.isConstantExists("reqFuel1"))
         {
             reqFuel = ecu.getField("reqFuel1");
         }
         else 
         {
             reqFuel = ecu.getField("reqFuel");
         }
         
         final EditText reqFuelEdit = (EditText) requiredFuelLayout.findViewById(R.id.req_fuel);
         reqFuelEdit.setText(String.valueOf(reqFuel));
         
         final EditText reqFuelDownloadedEdit = (EditText) requiredFuelLayout.findViewById(R.id.req_fuel_downloaded);
        reqFuelDownloadedEdit.setText(String.valueOf(reqFuel));
         
         requiredFuelButton.setOnClickListener(new Button.OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 EditRequiredFuel dialog = new EditRequiredFuel(getContext());
                 dialog.setDialogResult(new OnReqFuelResult()
                 {
                     public void finish(double reqFuel, double dReqFuel)
                     {
                         reqFuelEdit.setText(String.valueOf(reqFuel));
                         reqFuelDownloadedEdit.setText(String.valueOf(dReqFuel));
                     }
                 });
                 dialog.show();
             }
         });
         
         return requiredFuelLayout;
     }
     
     /** 
      * Build a seek bar used to choose between MAP/TPS based accel enrichement
      * 
      * @return The relative layout containing the seek bar
      */
     private RelativeLayout getAccelSeekBar()
     {
         RelativeLayout seekBarLayout = new RelativeLayout(getContext());
         /*
         SeekBar sb = new SeekBar(getContext());
         sb.setMax(100);
         sb.setProgress(50);
         sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
         {
             @Override
             public void onProgressChanged(SeekBar v, int progress, boolean fromUser)
             {
                 if (fromUser)
                 {
                 
                 }
             }
             
             @Override
             public void onStartTrackingTouch(SeekBar seekBar) {}
             
             @Override
             public void onStopTrackingTouch(SeekBar seekBar) {}
         });       
         
         seekBarLayout.addView(sb);
         */
         
         TextView blah = new TextView(getContext());
         blah.setText("fdafadsfj fjdaskl fjklsajflksda jfdsa");
         
         seekBarLayout.addView(blah);
         
         return seekBarLayout;
     }
     
     /**
      * Create a curve panel to insert in a dialog
      * 
      * @param parentLayout The parent layout the panel to add will be inserted into
      * @param curvePanel The curve panel itself
      * @param orientation The orientation of the panel
      * @param previousPanelLayout An instance of the previous layout added since the new one will be added in relation to the previous one
      * 
      * @return The relative layout with the curve panel in it
      */
     private RelativeLayout createCurvePanel(RelativeLayout parentLayout, CurveEditor curvePanel, String orientation, RelativeLayout previousPanelLayout)
     {
         CurveHelper curveHelper = new CurveHelper(getContext(), curvePanel, false);
         
         // Wrap panel layout into a relative layout so it can be used as parent   
         RelativeLayout containerPanelLayout = new RelativeLayout(getContext());
 
         // Convert to density independent pixels so it hopefully looks the same on every screen size
         Resources r = getContext().getResources();
         int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 800, r.getDisplayMetrics());
         RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(px, px);
         containerPanelLayout.setLayoutParams(tlp);
         containerPanelLayout.setId(nbPanels);
         containerPanelLayout.setTag(curvePanel.getName());
         
         if (nbPanels > 0) 
         {
             addPanel(parentLayout, containerPanelLayout, orientation, curvePanel.getName(), "", previousPanelLayout); 
         }
 
         LinearLayout curveLayout = curveHelper.getLayout();
         curveLayout.setLayoutParams(lpWithMargins);
         
         containerPanelLayout.addView(curveLayout);
 
         curveHelpers.add(curveHelper);
         
         return containerPanelLayout;
     }
     
     /**
      * Create a table panel to insert in a dialog
      * 
      * @param parentLayout The parent layout the panel to add will be inserted into
      * @param tablePanel The table panel itself
      * @param orientation The orientation of the panel
      * @param previousPanelLayout An instance of the previous layout added since the new one will be added in relation to the previous one
      * 
      * @return The relative layout with the table panel in it
      */
     private RelativeLayout createTablePanel(RelativeLayout parentLayout, TableEditor tablePanel, String orientation, RelativeLayout previousPanelLayout)
     {
         TableHelper tableHelper = new TableHelper(getContext(), tablePanel, false);
         
         RelativeLayout panelLayout = new RelativeLayout(getContext());
         
         panelLayout.setId(nbPanels);
         panelLayout.setTag(tablePanel.getName());  
         
         if (nbPanels > 0) 
         {
             addPanel(parentLayout, panelLayout, orientation, tablePanel.getName(), "", previousPanelLayout);
         }
         
         LinearLayout tableLayout = tableHelper.getLayout();
         tableLayout.setLayoutParams(lpWithMargins);
         
         panelLayout.addView(tableLayout);
         
         tableHelpers.add(tableHelper);
         
         return panelLayout;
     }
     
     /**
      * Take information from a dialog field and constant and build the label for the field
      * 
      * @param df The DialogField to build the label for
      * @param constant The Constant to build the label for
      * 
      * @return The TextView object
      */
     private TextView getLabel(DialogField df, Constant constant)
     {
         String labelText = df.getLabel();
         
         // Add units to label
         if (constant != null && !constant.getUnits().equals(""))
         {
             labelText += " (" + constant.getUnits() + ")";
         }
         
         TextView label = new TextView(getContext());
         label.setText(labelText);
         
         // If the first character of the label is a #, we need to highlight that label
         // It means it is used as a section separator
         if (labelText.length() > 0 && labelText.substring(0,1).equals("#"))
         {
             label.setText(" " + label.getText().toString().substring(1)); // Replace the # by a space
             label.setBackgroundColor(Color.rgb(110, 110, 110));
         }
         
         label.setLayoutParams(lpWithMargins);
         
         return label;
     }
     
     /**
      * Build an EditText for displaying single value constant
      * 
      * @param df The dialog field to build the display for
      * @param constant The constant associated with the dialog field
      * 
      * @return The EditText that can be displayed
      */
     private EditText buildSingleValueConstantField(DialogField df, Constant constant)
     {
         double constantValue = ecu.roundDouble(ecu.getField(df.getName()),constant.getDigits());
         String displayedValue = "";
         
         if (constant.getDigits() == 0)
         {
             displayedValue = String.valueOf((int) constantValue);
         }
         else
         {
             displayedValue = String.valueOf(constantValue);
         }
         
         EditText edit = new EditText(getContext());
         edit.setText(displayedValue);
         edit.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
         edit.setSingleLine(true);
         edit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
         edit.setPadding(8, 5, 8, 5);
         edit.setTag(df.getName());
         edit.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
         edit.setOnFocusChangeListener(new OnFocusChangeListener()
         {
             public void onFocusChange(View v, boolean hasFocus)
             {
                 if (!hasFocus)
                 {
                     Constant constant = ecu.getConstantByName(((EditText) v).getTag().toString());
                     DialogHelper.verifyOutOfBoundValue(getContext(), constant, (EditText) v);
                 }
             }
         });
         edit.addTextChangedListener(new TextWatcher()
         {
             /**
              * Set the constant to modified when value is changed
              * 
              * @param s
              */
             @Override
             public void afterTextChanged(Editable s)
             {
                 EditText edit = (EditText) getCurrentFocus();
                 String constantName = edit.getTag().toString();
                 
                 Constant constant = ecu.getConstantByName(constantName);
                 constant.setModified(true);
             }
 
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after){}
             
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count){}
         });
         
         // Field is ready only or disabled
         if (df.isDisplayOnly() || !ecu.getUserDefinedVisibilityFlagsByName(df.getExpression()))
         {
             edit.setEnabled(false);
         }
         
         return edit;
     }
     
     /**
      * Helper class for multi values spinner which specify an id and text for each row of the spinner
      */
     class MultiValuesSpinnerData
     {
         private int id = 0;
         private String text = "";
         
         public MultiValuesSpinnerData(int id, String text)
         {
             this.id = id;
             this.text = text;
         }
         
         public int getId()
         {
             return id;
         }
         
         public String getText()
         {
             return text;
         }
         
         public String toString()
         {
             return text;
         }
     }
     
     /**
      *  Build a Spinner for displaying multi values constant
      *
      * @param df The dialog field to build the display for
      * @param constant The constant associated with the dialog field
      * @return The Spinner that can be displayed
      */    
     private Spinner buildMultiValuesConstantField(DialogField df, Constant constant)
     {
         Spinner spin = new Spinner(getContext());
         
         // Field is ready only or disabled
         if (df.isDisplayOnly() || !ecu.getUserDefinedVisibilityFlagsByName(df.getExpression()))
         {
             spin.setEnabled(false);
         }
 
         final List<MultiValuesSpinnerData> spinnerData = new ArrayList<MultiValuesSpinnerData>();
         
         int selectedValue = (int) ecu.getField(df.getName());
         int selectedIndex = 0;
         int invalidCount = 0;
         
         // Remove INVALID from values
         for (int i = 0; i < constant.getValues().length; i++)
         {
             String value = constant.getValues()[i];
             
             if (value.equals("INVALID"))
             {
                 invalidCount++;
             }
             else
             {
                 spinnerData.add(new MultiValuesSpinnerData(i + 1, value));
             }
             
             /*
              *  When we reach the currently selected valid, we need to keep track of how many
              *  invalid value there was before that, because those won't be displayed in the
              *  spinner and we need to know which index to select
              */
             if (selectedValue == i)
             {
                 selectedIndex = i - invalidCount;
             }
         }
  
         ArrayAdapter<MultiValuesSpinnerData> spinAdapter = new ArrayAdapter<MultiValuesSpinnerData>(getContext(), android.R.layout.simple_spinner_item, spinnerData);
         
         // Specify the layout to use when the list of choices appears
         spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
         spin.setAdapter(spinAdapter);
         spin.setSelection(selectedIndex);
         spin.setTag(df.getName());
         
         final MSDialog msDialog = this.dialog;
         
         spin.setOnItemSelectedListener(new OnItemSelectedListener()
         {
             boolean ignoreEvent = true;
             
             @Override
             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
             {
                 // First onItemSelected event of the spinner come from populating it, ignore it!
                 if (ignoreEvent) {
                     ignoreEvent = false;
                 }
                 else 
                 {
                     String constantName = parentView.getTag().toString();
                     
                     int value = spinnerData.get(position).getId();
                     
                     // Value changed, update field in ECU class
                     if (ecu.getField(constantName) != value)
                     {
                         // Constant has been modified and will need to be burn to ECU
                         Constant constant = ecu.getConstantByName(constantName);
                         constant.setModified(true);
                         
                         // Update ecu field with new value
                         ecu.setField(constantName, value); 
                         
                         // Re-evaluate the expressions with the data updated
                         ecu.setUserDefinedVisibilityFlags();
     
                         // Refresh the UI
                         refreshFieldsVisibility(msDialog);
                     }
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> parentView){}
         });
         
         return spin;
     }
     
     /**
      * Add a label at the top of a panel
      * 
      * @param title
      * @param tl
      */
     private void showPanelLabel(String title, TableLayout tl)
     {
         LayoutParams lpSpan = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         lpSpan.span = 2;
         
         TableRow tableRow = new TableRow(getContext());
         tableRow.setLayoutParams(lpSpan);
         
         TextView label = new TextView(getContext());
         label.setText(title);
         label.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
         label.setPadding(0, 0, 0, 10);
         label.setLayoutParams(lpSpan);
         
         tableRow.addView(label);
         
         tl.addView(tableRow);
     }
     
     /**
      * Show the constant doesn't exists alert dialog
      * 
      * @param constantName The name of the constant that doesn't exists
      */
     private void showConstantDoesntExists(String constantName)
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
         builder.setMessage("Uh oh, It looks like constant \"" + constantName + "\" is missing!")
                 .setIcon(android.R.drawable.ic_dialog_info)
                 .setTitle("Missing constant")
                 .setCancelable(true)
                 .setPositiveButton("OK", new DialogInterface.OnClickListener()
                 {
                     public void onClick(DialogInterface dialog, int id){}
                 });
         
         AlertDialog alert = builder.create();
         alert.show();  
     }
         
     /**
      * When value are changed, it's possible dialog fields change state
      * so we need to refresh fields visibility and re-apply them recursivly 
      * on all the panels
      * 
      * @param dialog The dialog to refresh fields visibility for
      */
     private void refreshFieldsVisibility(MSDialog dialog)
     {
         for (DialogField df : dialog.getFieldsList())
         {
             Constant constant = ecu.getConstantByName(df.getName());
             
             if (constant != null)
             {
                 // Field is not ready only and not disabled
                 boolean isFieldEnabled = !df.isDisplayOnly() && ecu.getUserDefinedVisibilityFlagsByName(df.getExpression());
                 
                 if (constant.getClassType().equals("bits"))
                 {
                     Spinner spin = (Spinner) content.findViewWithTag(df.getName());
                     spin.setEnabled(isFieldEnabled);
                 }
                 else
                 {
                     EditText edit = (EditText) content.findViewWithTag(df.getName());
                     edit.setEnabled(isFieldEnabled);
                 }
             }
         }
         
         for (DialogPanel dp : dialog.getPanelsList())
         {
             MSDialog dialogPanel = ecu.getDialogByName(dp.getName());
             
             if (dialogPanel != null)
             {
                 refreshFieldsVisibility(dialogPanel);
             }
             else
             {
                 // Not a regular dialog, but maybe it's an std_* dialog
                 dialogPanel = DialogHelper.getStdDialog(getContext(), dp.getName());
                 
                 if (dialogPanel != null)
                 {
                     refreshFieldsVisibility(dialogPanel);
                 }
             }
         }
     }
     
     /**
      * Burn the change to the ECU
      */
     private void burnToECU()
     {
         // Burn all constant
         for (String constantName : ecu.getAllConstantsNamesForDialog(dialog))
         {
             Constant constant = ecu.getConstantByName(constantName);
             
             if (constant.isModified())
             {
                 System.out.println("Constant \"" + constantName + "\" was modified, need to write change to ECU");
                 
                 constant.setModified(false);
             }
         }
         
         // Burn all tables
         for (CurveHelper curveHelper : curveHelpers)
         {
             curveHelper.getCurveEditor();
         }
         
         // Burn all curves
         for (TableHelper tableHelper : tableHelpers)
         {
             tableHelper.getTableEditor();
         }
     }
     
     /**
      * Triggered when one of the two bottoms button are clicked ("Burn" and "Cancel")
      * 
      * @param v The view that was clicked on
      */
     @Override
     public void onClick(View v)
     {
         int which = v.getId();
         
         if (which == R.id.burn)
         {
             burnToECU();
         }
         else if (which == R.id.cancel)
         {
             cancel();
         }
     }
 }
