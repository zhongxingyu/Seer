 package org.iplantc.de.client.services;
 
 import org.iplantc.de.client.models.DEProperties;
 import org.iplantc.de.shared.SharedDataApiServiceFacade;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 
 import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * Facade for file editors.
  */
 public class FileEditorServiceFacade {
     /**
      * Call service to retrieve the manifest for a requested file
      * 
      * @param idFile desired manifest's file ID (path).
      * @param callback executes when RPC call is complete.
      */
     public void getManifest(String idFile, AsyncCallback<String> callback) {
         String address = "org.iplantc.services.de-data-mgmt.file-manifest?path=" //$NON-NLS-1$
                 + URL.encodeQueryString(idFile);
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         SharedDataApiServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     /**
      * Call service to retrieve data for a requested file
      * 
      * @param idFile file to retrieve raw data from.
      * @param callback executes when RPC call is complete.
      */
     public void getData(String url, AsyncCallback<String> callback) {
         String address = DEProperties.getInstance().getDataMgmtBaseUrl() + url;
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
        DEServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 
     /**
      * Get Tree URLs for the given tree's file ID.
      * 
      * @param idFile file ID (path) of the tree.
      * @param callback executes when RPC call is complete.
      */
     public void getTreeUrl(String idFile, AsyncCallback<String> callback) {
         String address = "org.iplantc.services.buggalo.baseUrl?path=" + URL.encodeQueryString(idFile); //$NON-NLS-1$
 
         ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
         SharedDataApiServiceFacade.getInstance().getServiceData(wrapper, callback);
     }
 }
