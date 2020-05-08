 /**
  * 
  */
 package ecologylab.appframework.types.prefs.gui;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.Action;
 import javax.swing.JCheckBox;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import ecologylab.appframework.types.prefs.Choice;
 import ecologylab.appframework.types.prefs.MetaPref;
 import ecologylab.appframework.types.prefs.MetaPrefBoolean;
 import ecologylab.appframework.types.prefs.MetaPrefFloat;
 import ecologylab.appframework.types.prefs.MetaPrefInt;
 import ecologylab.appframework.types.prefs.MetaPrefSet;
 import ecologylab.appframework.types.prefs.Pref;
 import ecologylab.appframework.types.prefs.PrefSet;
 import ecologylab.appframework.types.prefs.ValueChangedListener;
 import ecologylab.collections.Scope;
 import ecologylab.generic.Debug;
 import ecologylab.net.ParsedURL;
 import ecologylab.xml.XMLTranslationException;
 import ecologylab.xml.types.element.ArrayListState;
 
 /**
  * Contains methods for creating widgets associated with Prefs.
  * 
  * @author Zachary O. Toups (toupsz@cs.tamu.edu)
  */
 public class PrefEditorWidgets extends Debug implements ChangeListener
 {
     /**
      * The string that will be added to the metapref's id, to create a unique
      * identifier for a jComponent.
      */
     static final String                                                          IDENTIFIER_CHECK_BOX     = "checkBox";
 
     /**
      * The string that will be added to the metapref's id, to create a unique
      * identifier for a jComponent.
      */
     static final String                                                          IDENTIFIER_BOOLEAN_NO    = "No";
 
     /**
      * The string that will be added to the metapref's id, to create a unique
      * identifier for a jComponent.
      */
     static final String                                                          IDENTIFIER_BOOLEAN_YES   = "Yes";
 
     /**
      * The string that will be added to the metapref's id, to create a unique
      * identifier for a jComponent.
      */
     static final String                                                          IDENTIFIER_FILE_CHOOSER  = "fileChooser";
 
     /**
      * The string that will be added to the metapref's id, to create a unique
      * identifier for a jComponent.
      */
     static final String                                                          IDENTIFIER_COLOR_CHOOSER = "colorChooser";
 
     /**
      * The string that will be added to the metapref's id, to create a unique
      * identifier for a jComponent.
      */
     public static final String                                                   IDENTIFIER_SPINNER       = "spinner";
 
     /**
      * The string that will be added to the metapref's id, to create a unique
      * identifier for a jComponent.
      */
     static final String                                                          IDENTIFIER_SLIDER        = "slider";
 
     /**
      * The string that will be added to the metapref's id, to create a unique
      * identifier for a jComponent.
      */
     static final String                                                          IDENTIFIER_DROPDOWN      = "dropdown";
 
     /**
      * The string that will be added to the metapref's id, to create a unique
      * identifier for a jComponent.
      */
     static final String                                                          IDENTIFIER_TEXT_FIELD    = "textField";
 
     /**
      * The amount by which floats should be multiplied for use with sliders. If
      * this number is 10, you will be able to see the 1 digit after the decimal
      * when the number is changed back (10.1). For 100, you will have 2 digits
      * after the decimal (10.11). Etc.
      */
     static final int                                                             FLOAT_SLIDER_MODIFIER    = 10;
 
     /**
      * The inset between the right side of the gui panel and the right side of
      * values.
      */
     protected static final int                                                   RIGHT_GUI_INSET          = 20;
 
     /**
      * The padding between the default value in a text field and either side of
      * the text field.
      */
     protected static final int                                                   TEXT_FIELD_PADDING       = 50;
 
     /**
      * Okay, this is really, really important. The map goes like this:
      * CategoryName to (MetaPrefName to (Array of JComponents)) So for each
      * category, you get a sorted map of the metaprefs in that category. From
      * that map you can get a list of all the components for a metapref by name.
      */
     protected HashMap<String, LinkedHashMap<String, Scope<JComponent>>> jCatComponentsMap        = new HashMap<String, LinkedHashMap<String, Scope<JComponent>>>();
 
     /** Set of MetaPrefs */
     protected MetaPrefSet                                                        metaPrefSet;
 
     /** Set of Prefs */
     protected PrefSet                                                            prefSet;
 
     /** PURL to save prefs.xml to */
     protected ParsedURL                                                          savePrefsPURL;
 
 	/**
 	 * Content pane within base window for GUI
 	 */
 	protected JPanel 															jContentPane;
 
     /**
      * The base function that you call to construct the prefs editor GUI. This
      * requires that the MetaPrefSet and PrefSet be instantiated and populated
      * prior to call. This function creates the entire GUI and handles all
      * actions for it.
      * 
      * @param metaPrefSet
      *            Set of MetaPrefs
      * @param prefSet
      *            Set of Prefs
      * @param savePrefsPURL
      *            ParsedURL to save prefs.xml to
      * @param isStandalone
      *            Whether or not we're calling this standalone
      */
     public PrefEditorWidgets(MetaPrefSet metaPrefSet, PrefSet prefSet,
             ParsedURL savePrefsPURL)
     {
         this.metaPrefSet = metaPrefSet;
         this.prefSet = prefSet;
         this.savePrefsPURL = savePrefsPURL;
     }
 
     /**
      * Creates a text field.
      * 
      * @param panel
      *            JPanel this field will be associated with.
      * @param mp
      *            MetaPref this field is for.
      * 
      * @return JTextField with properties initialized to parameters.
      */
     protected JTextField createTextField(JPanel panel, MetaPref mp)
     {
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         c.anchor = GridBagConstraints.FIRST_LINE_START;
 
         JTextField textField = new JTextField();
         textField.setHorizontalAlignment(JTextField.CENTER);
         textField.setText(mp.getDefaultValue().toString());
         textField.setName(IDENTIFIER_TEXT_FIELD);
         c.gridx = 0;
         c.gridy = 0;
         c.insets = new Insets(0, 0, 0, RIGHT_GUI_INSET); // top,left,bottom,right
         c.ipadx = TEXT_FIELD_PADDING;
 
         panel.add(textField, c);
 
         // add metapref's component to array
         Scope<JComponent> mpComponents = jCatComponentsMap.get(
                 mp.getCategory()).get(mp.getID());
         if (mpComponents != null)
         {
             registerComponent(mp, IDENTIFIER_TEXT_FIELD, textField);
         }
 
         return textField;
     }
 
     /**
      * Creates a check box.
      * 
      * @param panel
      *            JPanel this button will be associated with.
      * @param mp
      *            MetaPref this checkbox is being created for.
      * 
      * @return JCheckBox with properties initialized to parameters.
      */
     protected JCheckBox createCheckBox(JPanel panel, MetaPref mp)
     {
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         c.anchor = GridBagConstraints.LINE_START;
 
         JCheckBox checkBox = new JCheckBox();
 
         checkBox.setSelected((Boolean) mp.getDefaultValue());
         checkBox.setName(IDENTIFIER_CHECK_BOX);
         c.gridx = 0;
         c.gridy = 0;
         c.insets = new Insets(0, 0, 0, RIGHT_GUI_INSET); // top,left,bottom,right
 
         panel.add(checkBox, c);
 
         // add metapref's component to array
         Scope<JComponent> mpComponents = jCatComponentsMap.get(
                 mp.getCategory()).get(mp.getID());
         if (mpComponents != null)
         {
             registerComponent(mp, IDENTIFIER_CHECK_BOX, checkBox);
         }
 
         return checkBox;
     }
 
     /**
      * Creates a drop down menu; combo box.
      * 
      * @param panel
      *            JPanel this field will be associated with.
      * @param mp
      *            MetaPref this combo box is being created for.
      * 
      * @return JComboBox with properties initialized to parameters.
      */
     protected<T> JComboBox createDropDown(JPanel panel, MetaPref<T> mp)
     {
         ArrayListState<Choice<T>> choices = mp.getChoices();
         T defValue = mp.getDefaultValue();
         if (choices != null)
         {
             String[] choiceLabels = new String[choices.size()];
             int i = 0;
             int selected = 0;
             for (Choice<T> choice : choices)
             {
                 choiceLabels[i] = choice.getLabel();
                 if (choice.getValue() == defValue)
                     selected = i;
                 i++;
             }
 
             GridBagConstraints c = new GridBagConstraints();
             c.fill = GridBagConstraints.BOTH;
             c.anchor = GridBagConstraints.FIRST_LINE_START;
 
             JComboBox comboBox = new JComboBox(choiceLabels);
             comboBox.setSelectedIndex(selected);
             comboBox.setName(IDENTIFIER_DROPDOWN);
             c.gridx = 0;
             c.gridy = 0;
             c.insets = new Insets(0, 0, 0, RIGHT_GUI_INSET); // top,left,bottom,right
             c.ipadx = TEXT_FIELD_PADDING;
 
             panel.add(comboBox, c);
 
             // add metapref's component to array
             Scope<JComponent> mpComponents = jCatComponentsMap.get(
                     mp.getCategory()).get(mp.getID());
             if (mpComponents != null)
             {
                 registerComponent(mp, IDENTIFIER_DROPDOWN, comboBox);
             }
 
             return comboBox;
         }
         return null;
     }
 
     /**
      * Creates a slider.
      * 
      * @param panel
      *            JPanel this slider will be associated with.
      * @param mp
      *            MetaPref this slider is being created for.
      * @param labelAndName
      *            Name of slider
      * @param valuesAreAltered
      *            Whether or not the values of the metapref/pref need to be
      *            altered for the slider.
      * @param valueAlteredBy
      *            How much the metapref/pref values are multiplied/ divided by.
      *            Only used if valuesAreAltered=true.
      * 
      * @return JSlider with properties initialized to parameters.
      */
     protected JSlider createSlider(JPanel panel, MetaPref mp,
             String labelAndName, boolean valuesAreAltered, int valueAlteredBy)
     {
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         c.anchor = GridBagConstraints.LINE_START;
 
         JSlider jSlider = new JSlider();
         if (valuesAreAltered)
         {
             // values must be altered by the modifier parameter
             int newMin = (int) ((Float) mp.getMinValue() * FLOAT_SLIDER_MODIFIER);
             int newMax = (int) ((Float) mp.getMaxValue() * FLOAT_SLIDER_MODIFIER);
             int defVal = (int) ((Float) mp.getDefaultValue() * FLOAT_SLIDER_MODIFIER);
             jSlider.setMinimum(newMin);
             jSlider.setMaximum(newMax);
             jSlider.setValue(defVal);
             jSlider.setMajorTickSpacing(FLOAT_SLIDER_MODIFIER * 5);
             jSlider.setMinorTickSpacing(FLOAT_SLIDER_MODIFIER / 5);
             Float curValue = new Float((float) (jSlider.getValue())
                     / FLOAT_SLIDER_MODIFIER);
             jSlider.setToolTipText(curValue.toString());
 
             // we need to mess with the labels so they display the correct
             // values.
             Hashtable<Integer, JComponent> labelTable = jSlider
                     .createStandardLabels(FLOAT_SLIDER_MODIFIER * 5, newMin);
             // add a label for the max value to the table if one doesn't exist
             // already
             if (!labelTable.containsKey(newMax))
             {
                 Hashtable<Integer, JComponent> maxLabelTable = jSlider
                         .createStandardLabels(FLOAT_SLIDER_MODIFIER * 5, newMax);
                 labelTable.putAll(maxLabelTable);
             }
             for (Map.Entry<Integer, JComponent> entry : labelTable.entrySet())
             {
                 // changes here ARE reflected in labelTable
 
                 // NOTE: this is the way you would change/remove entries from
                 // list of labels.
                 // you cannot add entries here. see the Java API and:
                 // http://java.sun.com/docs/books/tutorial/uiswing/components/slider.html
                 // for more details
                 JLabel label = (JLabel) entry.getValue();
                 Float value = new Float(label.getText());
                 value = value / FLOAT_SLIDER_MODIFIER;
                 label.setText(value.toString());
             }
             jSlider.setLabelTable(labelTable);
             jSlider.setName(labelAndName + "thisisafloat");
         }
         else
         {
             // default behavior
             jSlider.setMinimum((Integer) mp.getMinValue());
             jSlider.setMaximum((Integer) mp.getMaxValue());
             jSlider.setValue((Integer) mp.getDefaultValue());
             jSlider.setMajorTickSpacing(10);
             jSlider.setMinorTickSpacing(1);
             Integer curValue = jSlider.getValue();
             jSlider.setToolTipText(curValue.toString());
             jSlider.setName(labelAndName);
         }
 
         jSlider.setPaintTicks(true);
         jSlider.setPaintLabels(true);
         jSlider.addChangeListener(this);
         c.gridx = 0;
         c.gridy = 0;
         c.insets = new Insets(0, 0, 0, RIGHT_GUI_INSET); // top,left,bottom,right
         panel.add(jSlider, c);
 
         // add metapref's component to array
         Scope<JComponent> mpComponents = jCatComponentsMap.get(
                 mp.getCategory()).get(mp.getID());
         if (mpComponents != null)
         {
             registerComponent(mp, labelAndName, jSlider);
         }
 
         return jSlider;
     }
 
     /**
      * Creates a spinner. This is a text field with up/down arrows that allow
      * you to increase/decrease the value in the text box.
      * 
      * @param panel
      *            JPanel this spinner will be associated with.
      * @param mp
      *            MetaPref this spinner is being created for.
      * 
      * @return JSpinner with properties initialized to parameters.
      */
     protected JSpinner createSpinner(JPanel panel, MetaPref mp)
     {
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         c.anchor = GridBagConstraints.LINE_START;
 
         double stepSize = 1.0;
         if (mp instanceof MetaPrefFloat)
             stepSize = 0.1;
 
         SpinnerNumberModel numModel = new SpinnerNumberModel();
         numModel.setMinimum((Float) mp.getMinValue());
         numModel.setMaximum((Float) mp.getMaxValue());
         numModel.setValue(mp.getDefaultValue());
         numModel.setStepSize(stepSize);
 
         JSpinner jSpinner = new JSpinner();
         jSpinner.setModel(numModel);
         jSpinner.setValue(mp.getDefaultValue());
         jSpinner.setName(IDENTIFIER_SPINNER);
         c.gridx = 0;
         c.gridy = 0;
         c.insets = new Insets(0, 0, 0, RIGHT_GUI_INSET); // top,left,bottom,right
         panel.add(jSpinner, c);
 
         // add metapref's component to array
         Scope<JComponent> mpComponents = jCatComponentsMap.get(
                 mp.getCategory()).get(mp.getID());
         if (mpComponents != null)
         {
             registerComponent(mp, IDENTIFIER_SPINNER, jSpinner);
         }
 
         return jSpinner;
     }
 
     /**
      * Returns the ObjectRegistry for this MetaPref's jComponents.
      * 
      * @param mp
      *            MetaPref whose jComponents we want.
      * 
      * @return ObjectRegistry for MetaPref's jComponents.
      */
     protected Scope<JComponent> jCatComponentsMap(MetaPref mp)
     {
         LinkedHashMap<String, Scope<JComponent>> categoryMap = this.jCatComponentsMap
                 .get(mp.getCategory());
 
         if (categoryMap == null)
         {
             categoryMap = new LinkedHashMap<String, Scope<JComponent>>();
             this.jCatComponentsMap.put(mp.getCategory(), categoryMap);
         }
 
         Scope<JComponent> result = categoryMap.get(mp.getID());
         if (result == null)
         {
             LinkedHashMap<String, Scope<JComponent>> catHash = jCatComponentsMap
                     .get(mp.getCategory());
             result = new Scope<JComponent>();
             catHash.put(mp.getID(), result);
         }
         return result;
     }
 
     /**
      * Registers a JComponent with the ObjectRegistry
      * 
      * @param mp
      *            metapref we want to register
      * @param labelAndName
      *            label/name to add to the metapref's id to identify a
      *            particular jComponent. must be UNIQUE.
      * @param jComponent
      *            jComponent to register
      */
     public void registerComponent(MetaPref mp, String labelAndName,
             JComponent jComponent)
     {
         // println("Registering: " + this.id+labelAndName);
         jCatComponentsMap(mp).put(mp.getID() + labelAndName,
                 jComponent);
     }
 
     /**
      * This is for sliders; when we move the slider, we need to update the
      * tooltip and force it to display. TODO: change placement of tooltip
      */
     public void stateChanged(ChangeEvent e)
     {
         JSlider source = (JSlider) e.getSource();
         String name = source.getName();
         Float curVal = new Float(source.getValue());
         if (name.indexOf("thisisafloat") != -1)
         {
             curVal = curVal / FLOAT_SLIDER_MODIFIER;
             source.setToolTipText(curVal.toString());
         }
         else
         {
             source.setToolTipText(curVal.toString());
         }
         Action action = source.getActionMap().get("postTip");
         if (action != null)
         {
             // show me the tooltip, NOW
             action.actionPerformed(new ActionEvent(source,
                     ActionEvent.ACTION_PERFORMED, "postTip"));
         }
     }
 
     /**
      * Queries all widgets and updates their associated Prefs; this will cascade
      * and cause each Pref to notify all PrefChangedListeners associated with
      * Pref.
      */
     public void updatePrefsFromWidgets()
     {
         /*
          * we do this with metaprefs because we will always have all metaprefs.
          * we may not always have a prefs file to start with.
          */
         // this iterator organizes them by category
         for (String cat : metaPrefSet.getCategories())
         {
             for (MetaPref mp : metaPrefSet.getMetaPrefListByCategory(cat))
             {
                 Pref pref = mp.getAssociatedPref();
                 Object editedValue = getPrefValue(mp);
                Object previousValue = pref.value();
                pref.setValue(editedValue);

                // TODO probably take this code out; now setValue will notify
                // all PrefChangedListeners that are registered with it.
                ValueChangedListener valueChangedListener = mp
                        .getValueChangedListener();
                if ((valueChangedListener != null) && (editedValue != null))
                 {
                    if (!editedValue.equals(previousValue))
                        valueChangedListener.valueChanged(pref);
                 }

                if (!prefSet.containsKey(pref.key()))
                    prefSet.add(pref);
             }
         }
     }
 
     /**
      * Get the pref value (if applicable) for a given metapref.
      * 
      * @param mp
      *            metapref to fetch pref value for
      * @return pref value or null
      */
     <T> Object getPrefValue(MetaPref<T> mp)
     {
         if (mp.widgetIsTextField())
         {
             JTextField textField = (JTextField) lookupComponent(mp, mp.getID()
                     + IDENTIFIER_TEXT_FIELD);
             // TODO 1 -- why doesnt this line work -- check flow for MetaPrefInt
             // first!
             String textFieldText = textField.getText();
             return mp.getInstance(textFieldText);
             /*
              * if (mp instanceof MetaPrefString) { return new
              * String(textField.getText()); } else if (mp instanceof
              * MetaPrefInt) { return new Integer(textField.getText()); } else if
              * (mp instanceof MetaPrefFloat) { return new
              * Float(textField.getText()); }
              */
         }
         else if (mp.widgetIsRadio())
         {
             if (mp instanceof MetaPrefBoolean && !mp.hasChoices())
             {
                 JRadioButton yesButton = (JRadioButton) lookupComponent(mp, mp
                         .getID()
                         + IDENTIFIER_BOOLEAN_YES);
                 MetaPref<Boolean> mpb = (MetaPrefBoolean) mp;
                 return mpb.getInstance(yesButton.isSelected());
             }
             else
             {
                 // TODO: if we could fetch the ButtonGroup, we could do
                 // this more efficiently.
                 // find the selected one and return it
                 ArrayListState<Choice<T>> choices = mp.getChoices();
                 for (Choice<T> choice : choices)
                 {
                     String regName = mp.getID() + choice.getName();
                     JRadioButton choiceButton = (JRadioButton) lookupComponent(
                             mp, regName);
                     if (choiceButton.isSelected())
                     {
                         if (mp instanceof MetaPrefFloat)
                             return choice.getValue();
                         else if (mp instanceof MetaPrefInt)
                             return choice.getValue();
                         else if(mp instanceof MetaPrefBoolean)
                         	return choice.getValue();
                         else
                             return null;
                     }
                 }
             }
         }
         else if (mp.widgetIsDropDown())
         {
             if (mp instanceof MetaPrefFloat)
             {
                 MetaPrefFloat mpf = (MetaPrefFloat) mp;
                 ArrayListState<Choice<Float>> choices = mpf.getChoices();
                 JComboBox comboBox = (JComboBox) lookupComponent(mp, mp.getID()
                         + IDENTIFIER_DROPDOWN);
                 int selectedIndex = comboBox.getSelectedIndex();
                 return new Float(choices.get(selectedIndex).getValue());
             }
             else if (mp instanceof MetaPrefInt)
             {
                 JComboBox comboBox = (JComboBox) lookupComponent(mp, mp.getID()
                         + IDENTIFIER_DROPDOWN);
                 return new Integer(comboBox.getSelectedIndex());
             }
         }
         else if (mp.widgetIsCheckBox())
         {
             JCheckBox checkBox = (JCheckBox) lookupComponent(mp, mp.getID()
                     + IDENTIFIER_CHECK_BOX);
             return new Boolean(checkBox.isSelected());
         }
         else if (mp.widgetIsSlider())
         {
             if (mp instanceof MetaPrefInt)
             {
                 JSlider jSlider = (JSlider) lookupComponent(mp, mp.getID()
                         + IDENTIFIER_SLIDER);
                 return new Integer(jSlider.getValue());
             }
             else if (mp instanceof MetaPrefFloat)
             {
                 JSlider jSlider = (JSlider) lookupComponent(mp, mp.getID()
                         + IDENTIFIER_SLIDER);
                 int sliderValue = jSlider.getValue();
                 return new Float(((float) sliderValue) / FLOAT_SLIDER_MODIFIER);
             }
         }
         else if (mp.widgetIsSpinner())
         {
             JSpinner jSpinner = (JSpinner) lookupComponent(mp, mp.getID()
                     + IDENTIFIER_SPINNER);
             if (mp instanceof MetaPrefInt)
                 return new Integer((Integer) jSpinner.getValue());
             else if (mp instanceof MetaPrefFloat)
                 return new Float((Float) jSpinner.getValue());
         }
         else if (mp.widgetIsColorChooser())
         {
             JColorChooser colorChooser = (JColorChooser) lookupComponent(mp, mp
                     .getID()
                     + IDENTIFIER_COLOR_CHOOSER);
             return colorChooser.getColor();
         }
         else if (mp.widgetIsFileChooser())
         {
             JFileChooser fileChooser = (JFileChooser) lookupComponent(mp, mp
                     .getID()
                     + IDENTIFIER_FILE_CHOOSER);
             return fileChooser.getSelectedFile();
         }
         return null;
     }
 
     /**
      * Returns a JComponent from the ObjectRegistry by name
      * 
      * @param mp
      *            metapref to look up
      * @param labelAndName
      *            the label/name that was added to the metapref's id to identify
      *            a particular jComponent
      * @return JComponent matching labelAndName from ObjectRegistry
      */
     protected JComponent lookupComponent(MetaPref mp, String labelAndName)
     {
         // println("Trying to fetch: " + labelAndName);
         JComponent jComponent = jCatComponentsMap(mp)
                 .get(labelAndName);
         return jComponent;
     }
 
     protected void savePrefs()
     {
         if (savePrefsPURL == null)
         {
             // TODO provide better feedback to the user here!!!
             warning("Not saving Prefs persistently cause savePrefsURL == null.");
         }
         else
         {
             try
             {
                 prefSet.writePrettyXML(savePrefsPURL.file());
             }
             catch (XMLTranslationException e)
             {
                 // TODO auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Save the preferences; called by {@link #createApplyButton()}
      * and {@link #createSaveButton()}.
      * Saves the prefs to {@link #savePrefsPURL}.
      */
     public void actionSavePreferences()
     {
         // update prefs
     	updatePrefsFromWidgets();
         
         // save prefs back to the file
     	savePrefs();
     }
 
     /**
      * Get the jContentPane for the editor.
      * 
      * @return
      */
 	public JPanel getJContentPane()
 	{
 		return jContentPane;
 	}
 }
