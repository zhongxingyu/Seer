 package md.frolov.legume.client.ui.modals;
 
 import java.util.Map;
 
 import com.github.gwtbootstrap.client.ui.Button;
 import com.github.gwtbootstrap.client.ui.Modal;
 import com.github.gwtbootstrap.client.ui.TextBox;
 import com.google.common.collect.Maps;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 
 import md.frolov.legume.client.Constants;
 import md.frolov.legume.client.elastic.model.ModelFactory;
 import md.frolov.legume.client.gin.WidgetInjector;
 import md.frolov.legume.client.model.ConnectionsConf;
 import md.frolov.legume.client.service.ConfigurationService;
 
 /** @author Ivan Frolov (ifrolov@tacitknowledge.com) */
public class AddNewConnectionModal extends Composite implements Constants
 {
 
     interface AddNewConnectionModalUiBinder extends UiBinder<Widget, AddNewConnectionModal>
     {
     }
 
     private static AddNewConnectionModalUiBinder binder = GWT.create(AddNewConnectionModalUiBinder.class);
 
     @UiField
     Modal addModal;
     @UiField
     Button addButton;
     @UiField
     TextBox serviceName;
     @UiField
     TextBox serviceAddress;
 
     private ConfigurationService configurationService = WidgetInjector.INSTANCE.configurationService();
     private ModelFactory modelFactory = WidgetInjector.INSTANCE.modelFactory();
 
     public AddNewConnectionModal()
     {
        initWidget(binder.createAndBindUi(this));
     }
 
     @UiHandler("addButton")
     public void onAddButtonClick(final ClickEvent event)
     {
         ConnectionsConf conf = configurationService.getObject(ELASTICSEARCH_CONNECTIONS, ConnectionsConf.class);
         if (conf == null)
         {
             conf = modelFactory.connectionsConf().as();
         }
         Map<String, String> connections = conf.getConnections();
         if (connections == null)
         {
             connections = Maps.newTreeMap();
         }
         connections.put(serviceName.getText(), serviceAddress.getText());
         conf.setConnections(connections);
         configurationService.put(ELASTICSEARCH_CONNECTIONS, conf);
         addModal.hide();
 
         selectServer(serviceAddress.getText());
     }
 
     public void show()
     {
         addModal.show();
     }
 
     private void selectServer(String serverAddress) {
         configurationService.put(ELASTICSEARCH_SERVER, serverAddress);
         Window.Location.reload();
     }
 }
