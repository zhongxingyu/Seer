 package com.pragprog.hello;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
 public class HelloWorld implements BundleActivator {
     public void start(BundleContext bundleContext) throws Exception {
         HelloService helloService = getHelloService(bundleContext);
     }
 
     private HelloService getHelloService(BundleContext bundleContext) {
         ServiceReference ref = bundleContext.getServiceReference(HelloService.class.getName());
         HelloService service = (HelloService) bundleContext.getService(ref);
         return service;
     }
 
     public void stop(BundleContext bundleContext) throws Exception {
        
     }
 }
