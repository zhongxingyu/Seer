 /*
  * JBoss, Home of Professional Open Source
  * Copyright ${year}, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */ 
 
 package org.ajax4jsf.tests;
 
 import javax.el.ValueExpression;
 import javax.faces.application.Resource;
 import javax.faces.application.ResourceHandler;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.SystemEvent;
 import javax.faces.event.SystemEventListener;
 
 
 public class MockApplication20 extends MockApplication {
 
	private ResourceHandler resourceHandler = new MockResourceHandler();
 
 	public MockApplication20() {
 		super();
 	}
 
 	@Override
 	public ResourceHandler getResourceHandler() {
 		return resourceHandler;
 	}
 
 	@Override
 	public void setResourceHandler(ResourceHandler resourceHandler) {
 		this.resourceHandler = resourceHandler;
 	}
 
 	@Override
 	public void publishEvent(FacesContext context,
 			Class<? extends SystemEvent> systemEventClass,
 			Class<?> sourceBaseType, Object source) {
 
 		//do nothing
 	}
 	
 	@Override
 	public void publishEvent(FacesContext context,
 			Class<? extends SystemEvent> systemEventClass, Object source) {
 
 		//do nothing
 	}
 	
 	@Override
 	public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass,
 			Class<?> sourceClass, SystemEventListener listener) {
 
 		//do nothing
 	}
 	
 	@Override
 	public void subscribeToEvent(Class<? extends SystemEvent> systemEventClass,
 			SystemEventListener listener) {
 
 		//do nothing
 	}
 	
 	@Override
 	public void unsubscribeFromEvent(
 			Class<? extends SystemEvent> systemEventClass,
 			Class<?> sourceClass, SystemEventListener listener) {
 
 		//do nothing
 	}
 	
 	@Override
 	public void unsubscribeFromEvent(
 			Class<? extends SystemEvent> systemEventClass,
 			SystemEventListener listener) {
 
 		//do nothing
 	}
 	
 	@Override
     public UIComponent createComponent(ValueExpression componentExpression,
             FacesContext context,
             String componentType,
             String rendererType) {
 
 		//do nothing
 		return null;
     }
 	
 	@Override
     public UIComponent createComponent(FacesContext context,
             String componentType,
             String rendererType) {
 
 		//do nothing
 		return null;
     }
     
 	@Override
     public UIComponent createComponent(FacesContext context,
             Resource componentResource) {
 
 		//do nothing
 		return null;
 	}
 
 }
