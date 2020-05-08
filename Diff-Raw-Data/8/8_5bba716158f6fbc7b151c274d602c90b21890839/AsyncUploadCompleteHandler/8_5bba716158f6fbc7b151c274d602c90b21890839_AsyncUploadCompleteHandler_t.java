 package org.iplantc.de.client.events;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.utils.NotificationHelper;
 import org.iplantc.de.client.utils.NotifyInfo;
 
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class AsyncUploadCompleteHandler extends UploadCompleteHandler implements AsyncCallback<String> {
 
     private String idParentFolder;
 
     /**
      * Instantiate from a parent id.
      * 
      * @param idParent unique id for parent folder.
      */
     public AsyncUploadCompleteHandler(String idParent, String sourceUrl) {
         super(idParent);
         if (idParent == null || idParent.isEmpty()) {
             throw new IllegalArgumentException(I18N.DISPLAY.idParentInvalid());
         }
 
         this.idParentFolder = idParent;
     }
 
     /**
      * Retrieve parent id.
      * 
      * @return parent folder's unique id.
      */
     public String getParentId() {
         return idParentFolder;
     }
 
     @Override
     public void onFailure(Throwable caught) {
        JSONObject obj = JsonUtil.getObject(caught.getMessage());
        if (obj != null) {
            String err = JsonUtil.getString(obj, "error_code");
            if (err.equalsIgnoreCase("ERR_INVALID_URL")) {
                ErrorHandler.post(I18N.ERROR.importFailed(JsonUtil.getString(obj, "url")), caught);
                return;
            }
        }
         ErrorHandler.post(caught);
     }
 
     @Override
     public void onSuccess(String result) {
         JSONObject obj = JsonUtil.getObject(result);
         String filename = JsonUtil.getString(obj, DiskResource.LABEL);
         NotifyInfo.notify(NotificationHelper.Category.DATA, I18N.DISPLAY.urlImport(),
                 I18N.DISPLAY.importRequestSubmit(filename), null);
         onAfterCompletion();
     }
 
     @Override
     public void onCompletion(String sourceUrl, String response) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void onAfterCompletion() {
         // TODO Auto-generated method stub
 
     }
 
 }
