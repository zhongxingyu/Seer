 /*
  * SOLMIX PROJECT
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.gnu.org/licenses/ 
  * or see the FSF site: http://www.fsf.org. 
  */
 
 package com.solmix.sgt.client.event;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.TokenFormatter;
 import com.solmix.sgt.client.Action;
 import com.solmix.sgt.client.EviewType;
 
 /**
  * 
  * @author Administrator
  * @version 110035 2013-1-14
  */
 
 public class UrlClickEvent extends GwtEvent<UrlClickHandler>
 {
 
     private static Type<UrlClickHandler> TYPE;
 
     @Inject
     private  TokenFormatter tokenformatter;
 
     private final Map<String, String> parameters;
 
     private final String action;
 
     public UrlClickEvent(String urlParams)
     {
         PlaceRequest place = tokenformatter.toPlaceRequest(urlParams);
         action = place.getNameToken();
         parameters = new HashMap<String, String>();
         Set<String> params = place.getParameterNames();
         for (String param : params) {
             if (param.equals(EviewType.P_VIEW_TYPE) || param.equals(Action.P_MODULE) || param.equals(Action.ACTION))
                 continue;
             parameters.put(param, place.getParameter(param, null));
         }
 
     }
 
     public Map<String, String> getParameters() {
         if (this.parameters == null) {
             return Collections.emptyMap();
         } else {
             return this.parameters;
         }
     }
 
     public String getAction() {
         return this.action;
     }
 
     public String getParameter(String key, String defaultValue) {
         String value = null;
 
         if (parameters != null) {
             value = parameters.get(key);
         }
 
         if (value == null) {
             value = defaultValue;
         }
         return value;
     }
 
     public Set<String> getParameterNames() {
         if (parameters != null) {
             return parameters.keySet();
         } else {
             return Collections.emptySet();
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
      */
     @Override
     protected void dispatch(UrlClickHandler handler) {
         handler.onUrlClick(this);
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
      */
     @Override
     public com.google.gwt.event.shared.GwtEvent.Type<UrlClickHandler> getAssociatedType() {
         return getType();
     }
 
     public static Type<UrlClickHandler> getType() {
         if (TYPE == null) {
             TYPE = new Type<UrlClickHandler>();
         }
         return TYPE;
     }
 }
