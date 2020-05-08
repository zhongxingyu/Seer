 /*
  * Copyright 2013 John Ahlroos
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fi.jasoft.qrcode.client.ui;
 
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.SimplePanel;
 
 /**
  * Client side widget which communicates with the server. Messages from the
  * server are shown as HTML and mouse clicks are sent to the server.
  */
 public class VQRCode extends SimplePanel {
 
     /** Set the CSS class name to allow styling. */
     public static final String CLASSNAME = "v-qrcode";
 
     /** The qr encoded image */
     private Image qrcode;
     
     private SizeListener listener;
 
     /**
      * The constructor should first call super() to initialize the component and
      * then handle any initialization relevant to Vaadin.
      */
     public VQRCode() {
         setStyleName(CLASSNAME);
         qrcode = new Image();
         qrcode.setStyleName(CLASSNAME + "-img");
         setWidget(qrcode);
     }
     
     /**
      * Set the URL of the qr-code
      * 
      * @param url
      * 		The url of the image 
      */
     public void setUrl(String url){
    	if(url == null){
    		qrcode.setVisible(false);
    	} else {
    		qrcode.setVisible(true);
    		qrcode.setUrl(url);
    	}
     }
     
     /*
      * (non-Javadoc)
      * @see com.google.gwt.user.client.ui.Widget#onLoad()
      */
     @Override
     protected void onLoad() {
     	super.onLoad();
     	listener.sizeChanged(getOffsetWidth(), getOffsetHeight());
     }
     
     /*
      * (non-Javadoc)
      * @see com.google.gwt.user.client.ui.UIObject#setWidth(java.lang.String)
      */
     @Override
     public void setWidth(String width) {
     	super.setWidth(width);
     	listener.sizeChanged(getOffsetWidth(), getOffsetHeight());
     }
     
     /*
      * (non-Javadoc)
      * @see com.google.gwt.user.client.ui.UIObject#setHeight(java.lang.String)
      */
     @Override
     public void setHeight(String height) {
     	super.setHeight(height);
     	listener.sizeChanged(getOffsetWidth(), getOffsetHeight());
     }
     
     /**
      * Set listener for listening to size changes
      * @param listener
      */
     public void setSizeListener(SizeListener listener) {
     	this.listener = listener;
     }
 }
