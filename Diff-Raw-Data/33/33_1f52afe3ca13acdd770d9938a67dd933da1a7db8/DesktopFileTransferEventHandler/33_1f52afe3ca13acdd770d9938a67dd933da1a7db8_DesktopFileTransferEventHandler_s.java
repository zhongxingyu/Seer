 package org.iplantc.de.client.desktop.widget;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.services.DiskResourceServiceFacade;
 import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uidiskresource.client.events.RequestBulkDownloadEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestBulkDownloadEvent.RequestBulkDownloadEventHandler;
 import org.iplantc.core.uidiskresource.client.events.RequestBulkUploadEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestBulkUploadEvent.RequestBulkUploadEventHandler;
 import org.iplantc.core.uidiskresource.client.events.RequestImportFromUrlEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestImportFromUrlEvent.RequestImportFromUrlEventHandler;
 import org.iplantc.core.uidiskresource.client.events.RequestSimpleDownloadEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestSimpleDownloadEvent.RequestSimpleDownloadEventHandler;
 import org.iplantc.core.uidiskresource.client.events.RequestSimpleUploadEvent;
 import org.iplantc.core.uidiskresource.client.events.RequestSimpleUploadEvent.RequestSimpleUploadEventHandler;
 import org.iplantc.core.uidiskresource.client.views.dialogs.FileUploadByUrlDialog;
 import org.iplantc.core.uidiskresource.client.views.dialogs.SimpleFileUploadDialog;
 import org.iplantc.de.client.Constants;
 import org.iplantc.de.client.I18N;
 import org.iplantc.de.client.Services;
 import org.iplantc.de.client.idroplite.util.IDropLiteUtil;
 import org.iplantc.de.client.views.windows.configs.ConfigFactory;
 import org.iplantc.de.client.views.windows.configs.IDropLiteWindowConfig;
 import org.iplantc.de.client.views.windows.configs.SimpleDownloadWindowConfig;
 
 import com.google.common.collect.Lists;
 import com.google.gwt.safehtml.shared.UriUtils;
 import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
 
 class DesktopFileTransferEventHandler implements RequestBulkDownloadEventHandler,
         RequestBulkUploadEventHandler, RequestImportFromUrlEventHandler,
         RequestSimpleDownloadEventHandler, RequestSimpleUploadEventHandler {
 
     private final Desktop desktop;
     private final DiskResourceServiceFacade drService = Services.DISK_RESOURCE_SERVICE;
 
     DesktopFileTransferEventHandler(Desktop desktop) {
         this.desktop = desktop;
     }
 
     @Override
     public void onRequestSimpleUpload(RequestSimpleUploadEvent event) {
         Folder uploadDest = event.getDestinationFolder();
         if (canUpload(uploadDest)) {
             SimpleFileUploadDialog dlg = new SimpleFileUploadDialog(uploadDest, drService,
                     EventBus.getInstance(), UriUtils.fromTrustedString(Constants.CLIENT
                             .fileUploadServlet()), UserInfo.getInstance().getUsername());
             dlg.show();
         }
     }
 
     @Override
     public void onRequestUploadFromUrl(RequestImportFromUrlEvent event) {
         Folder uploadDest = event.getDestinationFolder();
 
         if (canUpload(uploadDest)) {
             String userName = UserInfo.getInstance().getUsername();
             FileUploadByUrlDialog dlg = new FileUploadByUrlDialog(uploadDest, drService, userName);
             dlg.show();
         }
     }
 
     @Override
     public void onRequestBulkUpload(RequestBulkUploadEvent event) {
         Folder uploadDest = event.getDestinationFolder();
         if (canUpload(uploadDest)) {
             // Build window config
            IDropLiteWindowConfig idlwc = ConfigFactory.iDropLiteWindowConfig();
             idlwc.setDisplayMode(IDropLiteUtil.DISPLAY_MODE_UPLOAD);
             idlwc.setUploadFolderDest(uploadDest);
             idlwc.setCurrentFolder(uploadDest);
             desktop.showWindow(idlwc);
         }
     }
 
     @Override
     public void onRequestSimpleDownload(RequestSimpleDownloadEvent event) {
         List<DiskResource> resources = Lists.newArrayList(event.getRequestedResources());
         if (isDownloadable(resources)) {
             if (resources.size() == 1) {
                 // Download now. No folders possible here....
                 Services.DISK_RESOURCE_SERVICE.simpleDownload(resources.get(0).getId());
             } else {
                 SimpleDownloadWindowConfig sdwc = ConfigFactory.simpleDownloadWindowConfig();
                 sdwc.setResourcesToDownload(filterFolders(resources));
                 desktop.showWindow(sdwc);
             }
         } else {
             showErrorMsg();
         }
 
     }
 
     // remove folders from list to be displayed for simple download
     private List<DiskResource> filterFolders(List<DiskResource> listToFilter) {
         List<DiskResource> filteredList = new ArrayList<DiskResource>();
         for (DiskResource dr : listToFilter) {
             if (!(dr instanceof Folder)) {
                 filteredList.add(dr);
             }
         }
 
         return filteredList;
     }
 
     @Override
     public void onRequestBulkDownload(RequestBulkDownloadEvent event) {
         List<DiskResource> resources = Lists.newArrayList(event.getRequestedResources());
         if (isDownloadable(resources)) {
 
             // Build window config
            IDropLiteWindowConfig idlwc = ConfigFactory.iDropLiteWindowConfig();
             idlwc.setDisplayMode(IDropLiteUtil.DISPLAY_MODE_DOWNLOAD);
             idlwc.setResourcesToDownload(resources);
             idlwc.setCurrentFolder(event.getCurrentFolder());
             desktop.showWindow(idlwc);
         } else {
             showErrorMsg();
         }
     }
 
     private boolean isDownloadable(List<DiskResource> resources) {
         if ((resources == null) || resources.isEmpty()) {
             return false;
         }
 
         for (DiskResource dr : resources) {
             if (!dr.getPermissions().isReadable()) {
                 return false;
             }
         }
         return true;
     }
 
     private boolean canUpload(Folder uploadDest) {
         if (uploadDest != null && DiskResourceUtil.canUploadTo(uploadDest)) {
             return true;
         } else {
             showErrorMsg();
             return false;
         }
     }
 
     private void showErrorMsg() {
         new AlertMessageBox(I18N.DISPLAY.permissionErrorTitle(), I18N.DISPLAY.permissionErrorMessage())
                 .show();
     }
 }
