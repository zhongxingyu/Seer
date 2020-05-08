 package org.iplantc.core.tito.client.services;
 
 import org.iplantc.core.uicommons.client.DEServiceFacade;
 import org.iplantc.core.uicommons.client.models.DEProperties;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 
import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class DeployedComponentSearchServiceFacade {
 
     public void searchDeployedComponents(String searchTerm, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getUnproctedMuleServiceBaseUrl()
                + "search-deployed-components/" + URL.encodeQueryString(searchTerm);
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 }
