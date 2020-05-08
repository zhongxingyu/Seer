 package md.frolov.legume.client.ui.controls;
 
 import com.github.gwtbootstrap.client.ui.Dropdown;
 import com.github.gwtbootstrap.client.ui.NavLink;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.place.shared.PlaceHistoryMapper;
 import com.google.gwt.regexp.shared.RegExp;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 
 import md.frolov.legume.client.Application;
 import md.frolov.legume.client.activities.stream.StreamPlace;
 import md.frolov.legume.client.activities.terms.TermsPlace;
 import md.frolov.legume.client.gin.WidgetInjector;
 import md.frolov.legume.client.model.Search;
 import md.frolov.legume.client.ui.modals.ColorizeDialog;
 import md.frolov.legume.client.ui.modals.CopyToClipboardDialog;
 
 /** @author Ivan Frolov (ifrolov@tacitknowledge.com) */
 public class FieldActionsDropdown extends Composite
 {
     private static final RegExp SOLR_SPECIAL_CHARS = RegExp.compile("[-+&|!(){}\\[\\]^\"~*?:\\\\]", "g");
     private static final int MAX_TEXT_LENGTH = 100;
 
     interface FieldActionsDropdownUiBinder extends UiBinder<Widget, FieldActionsDropdown>
     {
     }
 
     private static FieldActionsDropdownUiBinder binder = GWT.create(FieldActionsDropdownUiBinder.class);
     @UiField
     Dropdown actionsDropdown;
     @UiField
     NavLink includeFilter;
     @UiField
     NavLink includeOnlyFilter;
     @UiField
     NavLink excludeFilter;
     @UiField
     NavLink excludeOnlyFilter;
     @UiField
     NavLink copyToClipboard;
     @UiField
     NavLink score;
     @UiField
     NavLink assignColor;
 
     private final String fieldName;
     private final String valueText;
 
     private Application application = WidgetInjector.INSTANCE.application();
     private PlaceHistoryMapper historyMapper = WidgetInjector.INSTANCE.historyMapper();
 
     public FieldActionsDropdown(String fieldName, String valueText, long focusdate)
     {
         initWidget(binder.createAndBindUi(this));
 
         this.fieldName = fieldName;
         this.valueText = valueText;
         initLinks(focusdate);
     }
 
 
     private void initLinks(long focusdate) {
         if (valueText != null && valueText.length() > 0 && valueText.length() < MAX_TEXT_LENGTH)
         {
             String filter = getFilter();
             Search search = application.getCurrentSearch().clone();
             search.setFocusDate(focusdate);
             String originalQueryString = search.getQuery();
 
            score.setTargetHistoryToken(historyMapper.getToken(new TermsPlace(fieldName, search)));
 
             StreamPlace place = new StreamPlace(search);
 
             search.setQuery(getQueryString(originalQueryString, filter));
            includeFilter.setTargetHistoryToken(historyMapper.getToken(place));
             search.setQuery(getQueryString(originalQueryString, "NOT " + filter));
            excludeFilter.setTargetHistoryToken(historyMapper.getToken(place));
 
             search.setQuery(filter);
            includeOnlyFilter.setTargetHistoryToken(historyMapper.getToken(place));
             search.setQuery(("NOT "+filter));
            excludeOnlyFilter.setTargetHistoryToken(historyMapper.getToken(place));
 
         }
         else
         {
             includeFilter.setVisible(false);
             excludeFilter.setVisible(false);
             includeOnlyFilter.setVisible(false);
             excludeOnlyFilter.setVisible(false);
             assignColor.setVisible(false);
         }
     }
 
     private String getQueryString(String originalQuery, String postfix)
     {
         if (originalQuery.length() != 0)
         {
             return originalQuery + " AND " + postfix;
         }
         else
         {
             return postfix;
         }
     }
 
     private String getFilter()
     {
         StringBuilder sb = new StringBuilder();
         sb.append(fieldName).append(":\"");
         sb.append(SOLR_SPECIAL_CHARS.replace(valueText, "\\$&"));
         sb.append('\"');
 
         return sb.toString();
     }
 
     @UiHandler("copyToClipboard")
     public void handleCopyToClipboard(final ClickEvent event)
     {
         event.stopPropagation();
         new CopyToClipboardDialog(fieldName, valueText).show();
     }
 
     @UiHandler("assignColor")
     public void onAssignColorClick(final ClickEvent event)
     {
         event.stopPropagation();
         new ColorizeDialog(fieldName, valueText).show();
     }
 }
