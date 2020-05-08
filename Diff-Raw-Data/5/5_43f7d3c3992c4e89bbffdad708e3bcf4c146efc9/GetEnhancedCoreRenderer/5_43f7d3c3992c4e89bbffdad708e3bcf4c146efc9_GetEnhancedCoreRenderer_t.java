 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.renderkit;
 
 import static org.icemobile.util.HTML.ANCHOR_ELEM;
 import static org.icemobile.util.HTML.CLASS_ATTR;
 import static org.icemobile.util.HTML.DISABLED_ATTR;
 import static org.icemobile.util.HTML.HREF_ATTR;
 import static org.icemobile.util.HTML.ID_ATTR;
 import static org.icemobile.util.HTML.ONCLICK_ATTR;
 import static org.icemobile.util.HTML.DIV_ELEM;
 import static org.icemobile.util.HTML.STYLE_ATTR;
 
 import java.io.IOException;
 
import org.icefaces.mobi.utils.HTML;
 import org.icemobile.component.IGetEnhanced;
 import org.icemobile.util.CSSUtils;
 import org.icemobile.util.ClientDescriptor;
 
 public class GetEnhancedCoreRenderer {
     
     public void encode(IGetEnhanced component, IResponseWriter writer)
             throws IOException {
         
         String clientId = component.getClientId();
         ClientDescriptor client = component.getClient();
         
         if( !client.isICEmobileContainer() && !client.isSXRegistered()){
             writer.startElement(DIV_ELEM, component);
             writer.writeAttribute(ID_ATTR, clientId);
             String styleClass = component.getStyleClass();
             if( styleClass != null ){
                 styleClass = IGetEnhanced.CSS_CLASS + " " + styleClass;
             }
             else{
                 styleClass = IGetEnhanced.CSS_CLASS;
             }
             writer.writeAttribute(CLASS_ATTR, styleClass);
             String style = component.getStyle();
             if( style != null ){
                 writer.writeAttribute(STYLE_ATTR, style);
             }
             boolean disabled = component.isDisabled();
             if( disabled ){
                 writer.writeAttribute(DISABLED_ATTR, "disabled");
             }
             
             String msg = IGetEnhanced.INFO_MSG; //default msg
             String link = null;
             if( client.isAndroidOS()){
                 link = IGetEnhanced.ANDROID_LINK; 
                 String androidMsg = component.getAndroidMsg();
                 if( androidMsg != null ){
                     msg = androidMsg;
                 }
             }
             else if( client.isIOS()){
                 link = IGetEnhanced.IOS_LINK; 
                 String iosMsg = component.getIosMsg();
                 if( iosMsg != null ){
                     msg = iosMsg;
                 }
             }
             else if( client.isBlackBerry10OS()){
                 link = IGetEnhanced.BLACKBERRY10_LINK; 
                 String blackBerryMsg = component.getBlackberryMsg();
                 if( blackBerryMsg != null ){
                     msg = blackBerryMsg;
                 }
             }
             else if( client.isBlackBerryOS()){
                 link = IGetEnhanced.BLACKBERRY_LINK; 
                 String blackBerryMsg = component.getBlackberryMsg();
                 if( blackBerryMsg != null ){
                     msg = blackBerryMsg;
                 }
             }
             else{
                 link = IGetEnhanced.ICEMOBILE_LINK;
             }
            writer.startElement(HTML.SPAN_ELEM);
             writer.writeText(msg);
            writer.endElement(HTML.SPAN_ELEM);
 
             if( client.isIOS() || client.isAndroidOS() || client.isBlackBerry10OS() ){
                 writer.startElement(ANCHOR_ELEM, null);
                 writer.writeAttribute(HREF_ATTR,"#");
                 writer.writeAttribute(CLASS_ATTR, CSSUtils.STYLECLASS_BUTTON + " mobi-button-important");
                 writer.writeAttribute(ONCLICK_ATTR, component.getICEmobileRegisterSXScript());
                 writer.writeText("Enable ICEmobile SX");
                 writer.endElement(ANCHOR_ELEM);
             }
             
             if( component.isIncludeLink() ){
                 writer.startElement(ANCHOR_ELEM);
                 writer.writeAttribute(HREF_ATTR, link);
                 writer.writeAttribute(CLASS_ATTR, CSSUtils.STYLECLASS_BUTTON + " mobi-button-important");
                 writer.writeText(IGetEnhanced.DOWNLOAD);
                 writer.endElement(ANCHOR_ELEM);
             }
             
             writer.endElement(DIV_ELEM);
         }
     }
 
 }
