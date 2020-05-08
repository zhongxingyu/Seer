 package org.iplantc.core.uidiskresource.client.services.callbacks;
 
 import java.util.Collection;
 
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.views.IsMaskable;
 import org.iplantc.core.uidiskresource.client.events.DiskResourcesDeletedEvent;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 import org.iplantc.core.uidiskresource.client.services.errors.DiskResourceErrorAutoBeanFactory;
 import org.iplantc.core.uidiskresource.client.services.errors.ErrorDiskResourceDelete;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 
 public class DiskResourceDeleteCallback extends DiskResourceServiceCallback {
 
     private final Collection<DiskResource> resources;
     private final Folder parentFolder;
 
     public DiskResourceDeleteCallback(Collection<DiskResource> resources, Folder parentFolder, IsMaskable maskedCaller) {
         super(maskedCaller);
         this.resources = resources;
         this.parentFolder = parentFolder;
     }
 
     @Override
     public void onSuccess(String result) {
         unmaskCaller();
 
         EventBus.getInstance().fireEvent(new DiskResourcesDeletedEvent(resources, parentFolder));
     }
 
     @Override
     public void onFailure(Throwable caught) {
         unmaskCaller();
         DiskResourceErrorAutoBeanFactory factory = GWT.create(DiskResourceErrorAutoBeanFactory.class);
         AutoBean<ErrorDiskResourceDelete> errorBean = AutoBeanCodex.decode(factory,
                 ErrorDiskResourceDelete.class, caught.getMessage());
 
         ErrorHandler.post(errorBean.as(), caught);
 
     }
 
     @Override
     protected String getErrorMessageDefault() {
        return I18N.ERROR.deleteFailed();
     }
 
 }
