 package org.hackystat.projectbrowser.page.projectportfolio.configurationpanel;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.extensions.markup.html.form.select.Select;
 import org.apache.wicket.extensions.markup.html.form.select.SelectOption;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.basic.MultiLineLabel;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.StatelessForm;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.hackystat.projectbrowser.page.popupwindow.PopupWindowPanel;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.MeasureConfiguration;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.ProjectPortfolioDataModel;
 
 /**
  * Form in Project Portfolio configuration panel to input data.
  * 
  * @author Shaoxuan Zhang
  * 
  */
 public class ProjectPortfolioConfigurationForm extends StatelessForm {
   /** Support serialization. */
   private static final long serialVersionUID = 447730202032199770L;
   /** The colors to choose from. */
   private static final String[] colors = { "ffff00", // yellow
       "ff6600", // orange
       //"ff8080", // pink
       "ff00ff", // magenta
       "ff0000", // red
       "6600ff", //purple
       //"808080", // gray
       "00ffff", // cyan
       "00ff00", // green
       "0066ff", 
       //"0000ff", // blue
       //"000000", // black
   };
   /** The introductions. */
   private static final String introductions = 
     "Time phase: the time phase to analyse."
     + "Good color: the color for good values and trends."
     + "Soso color: the color for middle values and unstable trends."
     + "Bad color: the color for bad values and trends."
     + "Enabled : Only enabled measure will be shown in detail panel\n\n"
     + "Colorable : Colorable measure's chart and value will be colored according to the following"
     + " setting. Noncolorable measure's chart and value will be black.\n\n"
     + "Is Higher Better: If higher value means better."
     + "Higher Threshold : The threshold of high value. \n\n"
     + "Lower Threshold : The threshold of low value.\n\n";
   /** The word style. */
   private static final String STYLE_KEY = "style";
   /** THe preceding of HTTP background color style setting. */
   private static final String BACKGROUND_COLOR_PRECEDING = "background-color:#";
   /** The validators for measures' higher and lower thresholds. */
   private Map<String, ConfigurationValueValidator> validators = 
     new HashMap<String, ConfigurationValueValidator>();
   
   /**
    * @param id the wicket component id.
    * @param dataModel the data model that will be configure here.
    */
   public ProjectPortfolioConfigurationForm(String id, ProjectPortfolioDataModel dataModel) {
     super(id);
     
     add(new FeedbackPanel("configurationFeedback")); 
 
     add(new TextField("timePhrase", new PropertyModel(dataModel, "timePhrase")));
     add(new DropDownChoice("granularity", 
                            new PropertyModel(dataModel, "telemetryGranularity"), 
                            dataModel.getGranularities()));
     
     final Select goodColorSelect = new Select("goodColorSelect", new PropertyModel(dataModel,
     "goodColor"));
     goodColorSelect.add(new ColorSelectOptionList("goodColorOptionList", Arrays.asList(colors)));
     goodColorSelect.add(new AttributeModifier(STYLE_KEY, true, new Model(
         BACKGROUND_COLOR_PRECEDING + dataModel.getGoodColor())));
     goodColorSelect.setOutputMarkupId(true);
     goodColorSelect.add(new AjaxColorSelectChangeBackgroundColorBehavior(goodColorSelect));
     add(goodColorSelect);
 
     final Select sosoColorSelect = new Select("sosoColorSelect", new PropertyModel(dataModel,
     "sosoColor"));
     sosoColorSelect.add(new ColorSelectOptionList("sosoColorOptionList", Arrays.asList(colors)));
     sosoColorSelect.add(new AttributeModifier(STYLE_KEY, true, new Model(
         BACKGROUND_COLOR_PRECEDING + dataModel.getSosoColor())));
     sosoColorSelect.setOutputMarkupId(true);
     sosoColorSelect.add(new AjaxColorSelectChangeBackgroundColorBehavior(goodColorSelect));
     add(sosoColorSelect);
 
     final Select badColorSelect = new Select("badColorSelect", new PropertyModel(dataModel,
     "badColor"));
     badColorSelect.add(new ColorSelectOptionList("badColorOptionList", Arrays.asList(colors)));
     badColorSelect.add(new AttributeModifier(STYLE_KEY, true, new Model(
         BACKGROUND_COLOR_PRECEDING + dataModel.getBadColor())));
     badColorSelect.setOutputMarkupId(true);
     badColorSelect.add(new AjaxColorSelectChangeBackgroundColorBehavior(goodColorSelect));
     add(badColorSelect);
     
     final Form form = this;
     
     ListView measureList = new ListView("measureList", dataModel.getMeasures()) {
       /** Support serialization. */
       public static final long serialVersionUID = 1L;
 
       @Override
       protected void populateItem(ListItem item) {
         final MeasureConfiguration measure = (MeasureConfiguration) item.getModelObject();
 
         item.add(new Label("measureNameLabel", new PropertyModel(measure, "measureName")));
 
         item.add(new AjaxCheckBox("enableCheckBox", new PropertyModel(measure, "enabled")) {
           /** Support serialization. */
           public static final long serialVersionUID = 1L;
 
           @Override
           protected void onUpdate(AjaxRequestTarget arg0) {
             arg0.addComponent(this.getForm());
           }
         });
 
         item.add(new AjaxCheckBox("colorableCheckBox", new PropertyModel(measure, "colorable")) {
           /** Support serialization. */
           public static final long serialVersionUID = 1L;
 
           @Override
           protected void onUpdate(AjaxRequestTarget arg0) {
             arg0.addComponent(this.getForm());
           }
 
           @Override
           public boolean isEnabled() {
             return measure.isEnabled();
           }
         });
 
         item.add(new AjaxCheckBox("isHigherBetterCheckBox", 
             new PropertyModel(measure, "higherBetter")) {
           /** Support serialization. */
           public static final long serialVersionUID = 1L;
 
           @Override
           protected void onUpdate(AjaxRequestTarget arg0) {
             arg0.addComponent(this.getForm());
           }
 
           @Override
           public boolean isEnabled() {
             return measure.isEnabled();
           }
         });
         
         final TextField higherThresholdTextField = 
          new TextField("higherThreshold", new PropertyModel(measure, "higherThreshold")) {
           /** Support serialization. */
           private static final long serialVersionUID = -7434510173892738329L;
 
           @Override
           public boolean isEnabled() {
             return measure.isEnabled() && measure.isColorable();
           }
         };
         item.add(higherThresholdTextField);
         
         final TextField lowerThresholdTextField = 
          new TextField("lowerThreshold", new PropertyModel(measure, "lowerThreshold")) {
           /** Support serialization. */
           private static final long serialVersionUID = -1675316116473661403L;
 
           @Override
           public boolean isEnabled() {
             return measure.isEnabled() && measure.isColorable();
           }
         };
         item.add(lowerThresholdTextField);
         
         if (measure.isEnabled() && measure.isColorable()) {
           ConfigurationValueValidator validator = new ConfigurationValueValidator(measure.getName(),
               higherThresholdTextField, lowerThresholdTextField);
           form.add(validator);
           //add the validator to the validators map and remove the old existed one from the form.
           ConfigurationValueValidator oldValidator = validators.put(measure.getName(), validator);
           if (oldValidator != null) {
             form.remove(oldValidator);
           }
         }
         
       }
     };
     add(measureList);
     PopupWindowPanel parameterPopup = new PopupWindowPanel("instructionPopup",
         "Configuration instruction", "Configuration instruction");
     parameterPopup.getModalWindow().setContent(
         new MultiLineLabel(parameterPopup.getModalWindow().getContentId(), introductions));
     add(parameterPopup);
   }
 
   /**
    * Set the panel to be invisible after form submitted.
    */
   @Override
   public void onSubmit() {
     this.getParent().setVisible(false);
   }
 
   /**
    * The list of select options for the color select field.
    * 
    * @author Shaoxuan Zhang
    */
   public static class ColorSelectOptionList extends ListView {
 
     /**
      * @param id the wicket component id.
      * @param list the list of options.
      */
     public ColorSelectOptionList(final String id, final List<?> list) {
       super(id, list);
     }
 
     /** Support serialization. */
     private static final long serialVersionUID = -3842069135751729472L;
 
     /**
      * display the list item.
      * 
      * @param item the ListItem
      */
     @Override
     protected void populateItem(final ListItem item) {
       item.setRenderBodyOnly(true);
       final String colorHex = item.getModelObjectAsString();
       final SelectOption colorSelectOption = new SelectOption("option", new Model(
           (Serializable) item.getModelObject())) {
         /** Support serialization. */
         private static final long serialVersionUID = 4298345446185051771L;
 
         protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
           replaceComponentTagBody(markupStream, openTag, "");
         }
       };
       colorSelectOption.add(new SimpleAttributeModifier(STYLE_KEY, BACKGROUND_COLOR_PRECEDING
           + colorHex));
       item.add(colorSelectOption);
     }
   }
 
   /**
    * Change the component's background color to the color value within the component.
    * 
    * @author Shaoxuan Zhang
    */
   public static class AjaxColorSelectChangeBackgroundColorBehavior extends OnChangeAjaxBehavior {
     /** Support serialization. */
     private static final long serialVersionUID = -5326547000439295241L;
     /** The color select field component. */
     Select select;
 
     /**
      * @param select the color select field component
      */
     public AjaxColorSelectChangeBackgroundColorBehavior(Select select) {
       this.select = select;
     }
 
     /**
      * Action take place on Update. Impelement the interface.
      * 
      * @param target the AjaxRequestTarget
      */
     @Override
     protected void onUpdate(AjaxRequestTarget target) {
       select.add(new AttributeModifier(STYLE_KEY, true, new Model(BACKGROUND_COLOR_PRECEDING
           + select.getModelObjectAsString())));
       target.addComponent(select);
     }
   }
 
 }
