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
 
 import javax.faces.FacesException;
 import javax.faces.application.Application;
 
 import org.apache.shale.test.mock.MockApplication;
 
 public class Ajax4JsfMockApplicationFactory extends org.apache.shale.test.mock.MockApplicationFactory {
 
 	private Application application;
 
 	public Ajax4JsfMockApplicationFactory() {
 		super();
 	}
 
 	@Override
 	public Application getApplication() {
 		if (application != null) {
 			return application;
 		}
 		
 		Class<? extends Application> clazz = null;
 		try {
 			clazz = this.getClass().getClassLoader().loadClass("org.ajax4jsf.tests.MockApplication20").
 				asSubclass(Application.class);
 			
 			//force loading classes for fields
 			clazz.getDeclaredFields();
 			
 			application = (MockApplication) clazz.newInstance();
 
 			return application;
 		} catch (NoClassDefFoundError e) {
 			clazz = null; // We are not running in a JSF 2.0 environment
 		} catch (ClassNotFoundException e) {
 			clazz = null; // Same as above
 		} catch (RuntimeException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new FacesException(e);
 		}
 
 		return super.getApplication();
 	}
 
 	@Override
 	public void setApplication(Application application) {
 		this.application = application;
 	}
 }
