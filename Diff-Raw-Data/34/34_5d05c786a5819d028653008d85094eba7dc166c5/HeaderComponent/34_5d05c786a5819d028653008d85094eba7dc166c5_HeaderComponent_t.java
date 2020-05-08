 package md.frolov.legume.client.ui.components;
 
 import org.vectomatic.file.File;
 import org.vectomatic.file.FileList;
 import org.vectomatic.file.FileReader;
 import org.vectomatic.file.FileUploadExt;
 import org.vectomatic.file.events.LoadEndEvent;
 import org.vectomatic.file.events.LoadEndHandler;
 
 import com.github.gwtbootstrap.client.ui.Button;
 import com.github.gwtbootstrap.client.ui.NavLink;
 import com.github.gwtbootstrap.client.ui.SplitDropdownButton;
 import com.github.gwtbootstrap.client.ui.TextBox;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.place.shared.PlaceHistoryMapper;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.web.bindery.event.shared.EventBus;
 
 import md.frolov.legume.client.Application;
 import md.frolov.legume.client.activities.stream.StreamPlace;
 import md.frolov.legume.client.events.UpdateSearchQuery;
 import md.frolov.legume.client.events.UpdateSearchQueryHandler;
 import md.frolov.legume.client.gin.WidgetInjector;
 import md.frolov.legume.client.model.Search;
 import md.frolov.legume.client.service.ConfigurationService;
 import md.frolov.legume.client.ui.modals.ConfigurationDialog;
 
 /** @author Ivan Frolov (ifrolov@tacitknowledge.com) */
 public class HeaderComponent extends Composite implements UpdateSearchQueryHandler
 {
     private WidgetInjector injector = WidgetInjector.INSTANCE;
 
 
     interface HeaderUiBinder extends UiBinder<Widget, HeaderComponent>
     {
     }
 
     private static HeaderUiBinder binder = GWT.create(HeaderUiBinder.class);
 
     @UiField
     TextBox searchQuery;
     @UiField
     Button openInNewWindow;
     @UiField
     SplitDropdownButton configureButton;
     @UiField
     NavLink exportButton;
     @UiField
     NavLink importButton;
     @UiField
     FileUploadExt uploadFile;
 
     private EventBus eventBus = WidgetInjector.INSTANCE.eventBus();
     private Application application = WidgetInjector.INSTANCE.application();
     private ConfigurationService configurationService = WidgetInjector.INSTANCE.configurationService();
     private PlaceHistoryMapper historyMapper = WidgetInjector.INSTANCE.historyMapper();
 
     public HeaderComponent()
     {
         initWidget(binder.createAndBindUi(this));
         eventBus.addHandler(UpdateSearchQuery.TYPE, this);
     }
 
     @UiHandler("submitButton")
     public void onSubmitButtonClick(ClickEvent event) {
         submitSearch();
     }
 
     @UiHandler("searchQuery")
     public void handleKeyPress(final KeyPressEvent event)
     {
         if(event.getCharCode()==13 || event.getCharCode() == 10){
             submitSearch();
         }
     }
 
     private void submitSearch()
     {
         Search query = application.getCurrentSearch().clone();
         query.setQuery(searchQuery.getText());;
         eventBus.fireEvent(new UpdateSearchQuery(query));
     }
 
     @Override
     public void onUpdateSearchQuery(final UpdateSearchQuery event)
     {
         Search query = event.getSearchQuery();
         searchQuery.setText(query.getQuery());
     }
 
     @UiHandler("openInNewWindow")
     public void onOpenInNewWindowClick(final ClickEvent event)
     {
         String place = historyMapper.getToken(new StreamPlace(application.getCurrentSearch()));
        openInNewWindow.setTargetHistoryToken(History.encodeHistoryToken(place));
     }
 
     @UiHandler("configureButton")
     public void onConfigureClick(final ClickEvent event)
     {
         new ConfigurationDialog().show();
     }
 
     @UiHandler("importButton")
     public void onImportButtonClick(final ClickEvent event)
     {
         uploadFile.click();
     }
 
     @UiHandler("uploadFile")
     public void onUploadFile(final ChangeEvent event)
     {
         FileList files = uploadFile.getFiles();
         if(files.getLength()>0) {
             File file = files.getItem(0);
             final FileReader reader = new FileReader();
 
             reader.addLoadEndHandler(new LoadEndHandler()
             {
                 @Override
                 public void onLoadEnd(final LoadEndEvent event)
                 {
                     String text = reader.getStringResult();
                     configurationService.importConfig(text);
                     Window.Location.reload();
                 }
             });
 
             reader.readAsText(file);
         }
     }
 
     @UiHandler("exportButton")
     public void onExportButtonClick(final ClickEvent event)
     {
         exportButton.setHref(configurationService.exportConfig());
         DOM.setElementAttribute((Element) exportButton.getElement().getFirstChildElement(), "download", "settings.json");
     }
 }
