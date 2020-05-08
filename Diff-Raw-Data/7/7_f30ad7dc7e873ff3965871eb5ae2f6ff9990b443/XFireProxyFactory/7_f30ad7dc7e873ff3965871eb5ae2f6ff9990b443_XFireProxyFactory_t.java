 package org.codehaus.xfire.client;
 
 import java.lang.reflect.Proxy;
 import java.net.MalformedURLException;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.codehaus.xfire.XFire;
 import org.codehaus.xfire.XFireFactory;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.service.Binding;
 import org.codehaus.xfire.service.Endpoint;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.transport.Transport;
 
 /**
  * Factory for creating XFire SOAP client stubs.  The returned stub will call the remote object for all methods.
  * <pre>
 * Service serviceModel = new ObjectServiceFactory().create(Echo.class);
  * String url = "http://localhost:8080/services/Echo");
 * Echo echo = (Echo) factory.create(serviceModel, url);
  * </pre>
  * After creation, the stub can be like a regular Java class.  Because it makes remote calls, it can throw more
  * exceptions than a Java class. In particular, it may throw protocol exceptions, and <code>XFireFaults</code>
 * 
  * @author <a href="mailto:poutsma@mac.com">Arjen Poutsma</a>
  * @see org.codehaus.xfire.fault.XFireFault
  */
 public class XFireProxyFactory
 {
     private XFire xfire;
     
     public XFireProxyFactory()
     {
         this.xfire = XFireFactory.newInstance().getXFire();
     }
     
     public XFireProxyFactory(XFire xfire)
     {
         this.xfire = xfire;
     }
     
     /**
      * Creates a new proxy with the specified URL. The returned object is a proxy with the interface specified by the
      * given service interface.
      * <pre>
      * String url = "http://localhost:8080/services/Echo");
      * Echo echo = (Echo) factory.create(myService, url);
      * </pre>
      *
      * @param service the service to create a client for.
      * @param url              the URL where the client object is located.
      * @return a proxy to the object with the specified interface.
      */
     public Object create(Service service, String url)
             throws MalformedURLException
     {
         Collection transports = xfire.getTransportManager().getTransportsForUri(url);
 
         if (transports.size() == 0)
             throw new XFireRuntimeException("No Transport is available for url " + url);
         
         Binding binding = null;
         Transport transport = null;
         for (Iterator itr = transports.iterator(); itr.hasNext() && binding == null;)
         {
             transport = (Transport) itr.next();
             
             for (int i = 0; i < transport.getSupportedBindings().length; i++)
             {
                 binding = service.getBinding(transport.getSupportedBindings()[i]);
                 
                 if (binding != null)
                     break;
             }
         }
 
         Client client = new Client(transport, binding, url);
         return create(client);
     }
     
     /**
      * Creates a new proxy with the specified URL. The returned object is a proxy with the interface specified by the
      * given service interface.
      * <pre>
      * String url = "http://localhost:8080/services/Echo");
      * Echo echo = (Echo) factory.create(transport, myService, url);
      * </pre>
      * @param transport        The transport to use.
      * @param url              the URL where the client object is located.
      * @param serviceInterface the service to create a client for.
      *
      * @return a proxy to the object with the specified interface.
      */
     public Object create(Service service, Transport transport, String url)
             throws MalformedURLException
     {
         return create(new Client(transport, service, url));
     }
     
     public Object create(Client client)
     {
         client.setXFire(xfire);
         
         XFireProxy handler = new XFireProxy(client);
         Class serviceClass = client.getService().getServiceInfo().getServiceClass();
         
         return Proxy.newProxyInstance(serviceClass.getClassLoader(), 
                                       new Class[]{serviceClass}, 
                                       handler);
     }
 
     public Object create(Endpoint endpoint)
         throws MalformedURLException
     {
         Binding binding = endpoint.getBinding();
         Transport t = xfire.getTransportManager().getTransport(binding.getBindingId());
         
         if (t == null)
         {
             throw new XFireRuntimeException("Could not find transport for binding " + 
                                             binding.getBindingId());
         }
         
         return create(new Client(t, endpoint));
     }
         
     public Object create(Binding binding, String address)
         throws MalformedURLException
     {
         Transport t = xfire.getTransportManager().getTransport(binding.getBindingId());
         
         if (t == null)
         {
             throw new XFireRuntimeException("Could not find transport for binding " + 
                                             binding.getBindingId());
         }
         
         Client client = new Client(t, binding, address);
         return create(client);
     }
 }
