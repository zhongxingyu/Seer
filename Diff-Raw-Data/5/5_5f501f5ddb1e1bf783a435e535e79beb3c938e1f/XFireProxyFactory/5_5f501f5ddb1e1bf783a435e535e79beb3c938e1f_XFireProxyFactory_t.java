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
  * String url = "http://localhost:8080/services/Echo");
  * Echo echo = (Echo) factory.create(HelloHome.class, url);
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
         
         Binding lastChoice = null;
         Binding firstChoice = null;
         Transport t = null;
         for (Iterator itr = service.getBindings().iterator(); itr.hasNext();)
         {
             Binding binding = (Binding) itr.next();
             if (binding.getTransport() == null) 
             {
                 lastChoice = binding;
             }
             else if (transports.contains(binding.getTransport()))
             {
                 firstChoice = binding;
                 t = binding.getTransport();
                 break;
             }
         }
         
        if (firstChoice == null && lastChoice == null)
        {
            throw new XFireRuntimeException("Could not find binding for url " + url);
        }
        
         if (firstChoice == null)
         {
             firstChoice = lastChoice;
             t = (Transport) transports.iterator().next();
         }
 
         Client client = new Client(firstChoice, url);
         client.setTransport(t);
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
         return create(new Client(endpoint));
     }
         
     public Object create(Binding binding, String address)
         throws MalformedURLException
     {
         Transport transport = binding.getTransport();
         
         if (transport == null)
         {
             throw new IllegalStateException("No such transport for binding.");
         }
         
         return create(binding.getService(), transport, address);
     }
 }
