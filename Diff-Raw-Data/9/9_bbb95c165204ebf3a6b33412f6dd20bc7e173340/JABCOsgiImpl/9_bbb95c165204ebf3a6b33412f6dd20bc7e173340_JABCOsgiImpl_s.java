 /*
  * Copyright 2013 Bill La Forge
  *
  * This file is part of AgileWiki and is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License (LGPL) as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
  *
  * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
  * All other files are covered by the Common Public License (CPL).
  * A copy of this license is also included and can be
  * found as well at http://www.opensource.org/licenses/cpl1.0.txt
  */
 package org.agilewiki.jid.jaosgi;
 
 import org.osgi.framework.*;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.List;
 
 public class JABCOsgiImpl extends JABundleContext {
     private BundleContext bundleContext;
     private JAServiceTracker jaServiceTracker;
     private List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();
 
     public void setBundleContext(BundleContext bundleContext) {
         if (this.bundleContext != null)
             throw new IllegalStateException("duplicate bundleContext");
         this.bundleContext = bundleContext;
     }
 
     public void setJAServiceTracker(JAServiceTracker jaServiceTracker) {
         if (this.jaServiceTracker != null)
             throw new IllegalStateException("duplicate jaServiceTracker");
         this.jaServiceTracker = jaServiceTracker;
     }
 
     @Override
     public List<ServiceRegistration> getServiceRegistrations() {
         return serviceRegistrations;
     }
 
     @Override
     public ServiceRegistration registerService(String clazz, Object service, Dictionary properties) {
         ServiceRegistration serviceRegistration = bundleContext.registerService(clazz, service, properties);
         serviceRegistrations.add(serviceRegistration);
         return serviceRegistration;
     }
 
     @Override
     public ServiceReference getServiceReference(String clazz) {
         return bundleContext.getServiceReference(clazz);
     }
 
     @Override
     public Object getService(ServiceReference reference) {
         return jaServiceTracker.getService(reference);
     }
 
     @Override
     public File getDataFile(String filename) {
         return bundleContext.getDataFile(filename);
     }
 
     @Override
     public Filter createFilter(String filter) throws InvalidSyntaxException {
         return bundleContext.createFilter(filter);
     }
 
     @Override
     public Bundle getBundle(String location) {
         return bundleContext.getBundle(location);
     }
 
     @Override
     public ServiceReference getServiceReference(Class clazz) {
         return bundleContext.getServiceReference(clazz);
     }
 
     @Override
     public String getProperty(String key) {
         return bundleContext.getProperty(key);
     }
 
     @Override
     public Bundle getBundle() {
         return bundleContext.getBundle();
     }
 
     @Override
     public Bundle installBundle(String location, InputStream input) throws BundleException {
         return bundleContext.installBundle(location, input);
     }
 
     @Override
     public Bundle installBundle(String location) throws BundleException {
         return bundleContext.installBundle(location);
     }
 
     @Override
     public Bundle getBundle(long id) {
         return bundleContext.getBundle(id);
     }
 
     @Override
     public Bundle[] getBundles() {
         return bundleContext.getBundles();
     }
 
     @Override
     public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
         bundleContext.addServiceListener(listener, filter);
     }
 
     @Override
     public void addServiceListener(ServiceListener listener) {
         bundleContext.addServiceListener(listener);
     }
 
     @Override
     public void removeServiceListener(ServiceListener listener) {
         bundleContext.removeServiceListener(listener);
     }
 
     @Override
     public void addBundleListener(BundleListener listener) {
         bundleContext.addBundleListener(listener);
     }
 
     @Override
     public void removeBundleListener(BundleListener listener) {
         bundleContext.removeBundleListener(listener);
     }
 
     @Override
     public void addFrameworkListener(FrameworkListener listener) {
         bundleContext.addFrameworkListener(listener);
     }
 
     @Override
     public void removeFrameworkListener(FrameworkListener listener) {
         bundleContext.removeFrameworkListener(listener);
     }
 
     @Override
     public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
         return bundleContext.registerService(clazzes, service, properties);
     }
 
     @Override
     public ServiceRegistration registerService(Class clazz, Object service, Dictionary properties) {
         return bundleContext.registerService(clazz, service, properties);
     }
 
     @Override
     public ServiceReference[] getServiceReferences(String clazz, String filter)
             throws InvalidSyntaxException {
         return bundleContext.getServiceReferences(clazz, filter);
     }
 
     @Override
     public ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
         return new ServiceReference[0];  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter)
             throws InvalidSyntaxException {
         return bundleContext.getServiceReferences(clazz, filter);
     }
 
     @Override
     public boolean ungetService(ServiceReference serviceReference) {
         return jaServiceTracker.ungetService(serviceReference);
     }
 
     @Override
     public void stop(int options) throws BundleException {
         bundleContext.getBundle().stop(options);
     }
 }
