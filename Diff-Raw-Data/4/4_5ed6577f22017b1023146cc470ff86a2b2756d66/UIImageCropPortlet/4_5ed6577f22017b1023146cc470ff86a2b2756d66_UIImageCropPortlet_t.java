 /*
  * Copyright (C) 2003-2012 eXo Platform SAS.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.portal.webui.image.crop;
 
 
 
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.core.UIPopupWindow;
 import org.exoplatform.webui.core.UIPortletApplication;
 import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
 import org.exoplatform.webui.event.Event;
 import org.exoplatform.webui.event.EventListener;
 import org.exoplatform.webui.config.annotation.EventConfig;
 
 /**
  * Created by The eXo Platform SAS
  * Author : An Bao NGUYEN
  *          annb@exoplatform.com
  * Oct 31, 2012  
  */
 @ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/imagecrop/portlet/UIProfile.gtmpl",
    events = {
      @EventConfig(listeners = UIImageCropPortlet.ChangePictureActionListener.class)
    }
   )
 public class UIImageCropPortlet extends UIPortletApplication{
   private final String POPUP_AVATAR_UPLOADER = "UIPopupAvatarUploader";
   
   
  //create a popup 
   public UIImageCropPortlet() throws Exception {
     UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, null, POPUP_AVATAR_UPLOADER);
//    uiPopup.setWindowSize(340, 0);
     addChild(uiPopup);
   }
 
   /**
    * Action trigger for editting picture. An UIAvatarUploader popup should be displayed.
    * @since 1.2.2
    */
   public static class ChangePictureActionListener extends EventListener<UIImageCropPortlet> {
 
     @Override
     public void execute(Event<UIImageCropPortlet> event) throws Exception {
       UIImageCropPortlet uiProfileNavigation = event.getSource();
       UIPopupWindow uiPopup = uiProfileNavigation.getChild(org.exoplatform.webui.core.UIPopupWindow.class);
       UIImageCroppingUploader uiAvatarUploader = uiProfileNavigation.createUIComponent(UIImageCroppingUploader.class, null, null);
       uiPopup.setUIComponent(uiAvatarUploader);
       uiPopup.setShow(true);
     }
   }
 }
