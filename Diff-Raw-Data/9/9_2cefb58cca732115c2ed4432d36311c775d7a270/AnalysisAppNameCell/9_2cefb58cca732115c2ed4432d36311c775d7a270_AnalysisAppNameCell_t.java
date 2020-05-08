 package org.iplantc.de.client.analysis.views.cells;
 
 import static com.google.gwt.dom.client.BrowserEvents.CLICK;
 import static com.google.gwt.dom.client.BrowserEvents.MOUSEOUT;
 import static com.google.gwt.dom.client.BrowserEvents.MOUSEOVER;
 
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.de.client.analysis.models.Analysis;
 import org.iplantc.de.client.events.WindowShowRequestEvent;
 import org.iplantc.de.client.views.windows.configs.AppWizardConfig;
 import org.iplantc.de.client.views.windows.configs.ConfigFactory;
 
 import com.google.common.base.Strings;
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.dom.client.Style.TextDecoration;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.client.Event;
 
 /**
  * @author sriram, jstroot
  * 
  */
 public class AnalysisAppNameCell extends AbstractCell<Analysis> {
 
     interface Resources extends ClientBundle {
 
         @Source("AnalysisNameCell.css")
         Style css();
     }
 
     interface Style extends CssResource {
         String hasResultFolder();
 
         String noResultFolder();
     }
 
     interface Templates extends SafeHtmlTemplates {
 
        @SafeHtmlTemplates.Template("<span name=\"{0}\" title=\"Click here to relaunch this analysis.\" class=\"{1}\">{2}</span>")
         SafeHtml cell(String elementName, String className, SafeHtml analysisAppName);
     }
 
     private final Resources res = GWT.create(Resources.class);
     private final Templates templates = GWT.create(Templates.class);
     private final EventBus eventBus;
     private static final String ELEMENT_NAME = "analysisAppName";
 
     public AnalysisAppNameCell(EventBus eventBus) {
         super(CLICK, MOUSEOVER, MOUSEOUT);
         res.css().ensureInjected();
         this.eventBus = eventBus;
     }
 
     @Override
     public void render(Cell.Context context, Analysis model, SafeHtmlBuilder sb) {
         if (model == null)
             return;
 
         String style = Strings.isNullOrEmpty(model.getResultFolderId()) ? res.css().noResultFolder()
                 : res.css().hasResultFolder();
         sb.append(templates.cell(ELEMENT_NAME, style, SafeHtmlUtils.fromString(model.getAppName())));
     }
 
     @Override
     public void onBrowserEvent(Cell.Context context, Element parent, Analysis value, NativeEvent event,
             ValueUpdater<Analysis> valueUpdater) {
         if (value == null) {
             return;
         }
         // Call the super handler, which handlers the enter key.
         super.onBrowserEvent(context, parent, value, event, valueUpdater);
 
         Element eventTarget = Element.as(event.getEventTarget());
         if (parent.isOrHasChild(eventTarget)) {
 
             switch (Event.as(event).getTypeInt()) {
                 case Event.ONCLICK:
                     doOnClick(eventTarget, value, valueUpdater);
                     break;
                 case Event.ONMOUSEOVER:
                     doOnMouseOver(eventTarget, value);
                     break;
                 case Event.ONMOUSEOUT:
                     doOnMouseOut(eventTarget, value);
                     break;
                 default:
                     break;
             }
         }
 
     }
 
     private void doOnMouseOut(Element eventTarget, Analysis value) {
         if (eventTarget.getAttribute("name").equalsIgnoreCase(ELEMENT_NAME)
                 && !Strings.isNullOrEmpty(value.getResultFolderId())) {
             eventTarget.getStyle().setTextDecoration(TextDecoration.NONE);
         }
     }
 
     private void doOnMouseOver(Element eventTarget, Analysis value) {
         if (eventTarget.getAttribute("name").equalsIgnoreCase(ELEMENT_NAME)
                 && !Strings.isNullOrEmpty(value.getResultFolderId())) {
             eventTarget.getStyle().setTextDecoration(TextDecoration.UNDERLINE);
         }
     }
 
     private void doOnClick(Element eventTarget, Analysis value, ValueUpdater<Analysis> valueUpdater) {
         if (eventTarget.getAttribute("name").equalsIgnoreCase(ELEMENT_NAME)
                 && !Strings.isNullOrEmpty(value.getResultFolderId())) {
             AppWizardConfig config = ConfigFactory.appWizardConfig(value.getAppId());
             config.setAnalysisId(value);
             config.setRelaunchAnalysis(true);
             eventBus.fireEvent(new WindowShowRequestEvent(config));
         }
     }
 
 }
