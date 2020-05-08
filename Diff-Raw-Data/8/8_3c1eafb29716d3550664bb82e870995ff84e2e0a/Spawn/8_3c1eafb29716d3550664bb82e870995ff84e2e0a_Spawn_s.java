 // Copyright 2007-2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.server;
 
 import java.io.PrintStream;
 
 import org.joe_e.Token;
 import org.ref_send.log.Anchor;
 import org.ref_send.promise.Volatile;
 import org.ref_send.promise.eventual.Eventual;
 import org.ref_send.var.Receiver;
 import org.waterken.net.http.HTTPD;
 import org.waterken.project.ProjectTracer;
 import org.waterken.remote.Remote;
 import org.waterken.remote.Remoting;
 import org.waterken.remote.http.AMP;
 import org.waterken.uri.Hostname;
 import org.waterken.uri.URI;
 import org.waterken.vat.Creator;
 import org.waterken.vat.Root;
 import org.waterken.vat.Tracer;
 import org.waterken.vat.Transaction;
 import org.waterken.vat.Vat;
 
 /**
  * Command line program to create a new database.
  */
 final class
 Spawn {
 
     private
     Spawn() {}
     
     /**
      * @param args  command line arguments
      */
     static public void
     main(String[] args) throws Exception {
 
         // extract the arguments
         if (args.length < 2) {
             final PrintStream log = System.err;
             log.println("Creates a new persistent object folder.");
             log.println("use: java -jar spawn.jar " +
                 "<project-name> <factory-typename> <database-label>");
             System.exit(-1);
             return;
         }
         final String projectValue = args[0];
         final String typename = args[1];
         final String label = 2 < args.length ? args[2] : null;
 
         // load configured values
         final String vatURIPathPrefix =
             Config.read(String.class, "vatURIPathPrefix");
 
         // determine the local address
         final String hereValue;
         final Credentials credentials = Proxy.init();
         if (null != credentials) {
             final String host = credentials.getHostname();
             Hostname.vet(host);
             final int portN = Config.read(HTTPD.class, "https").port;
             final String port = 443 == portN ? "" : ":" + portN;
             hereValue = "https://" + host + port + "/" + vatURIPathPrefix;
         } else {
             final int portN = Config.read(HTTPD.class, "http").port;
             final String port = 80 == portN ? "" : ":" + portN;
             hereValue = "http://localhost" + port + "/" + vatURIPathPrefix;
         }
         final Proxy clientValue = new Proxy();
         
         // create the database
         final String r=Config.vat().enter(Vat.change,new Transaction<String>() {
             public String
             run(final Root local) throws Exception {
                 final Token deferredValue = new Token();
                 final Eventual _Value = new Eventual(deferredValue, null, null);
                 final Creator creator = local.fetch(null, Root.creator);
                 final ClassLoader code = creator.load(projectValue);
                 final Tracer tracerValue =
                     ProjectTracer.make(code, code.getParent());
                 final Root synthetic = new Root() {
                     
                     public String
                     getVatName() { return local.getVatName(); }
 
                     public Anchor
                     anchor() { return local.anchor(); }
 
                    public <T> T
                     fetch(final Object otherwise, final String name) {
                        return (T)(Root.project.equals(name)
                             ? projectValue
                         : Root.here.equals(name)
                             ? hereValue
                         : Root.events.equals(name)
                             ? ReadConfig.make(Receiver.class, "events")
                         : Root.tracer.equals(name)
                             ? tracerValue
                         : Remoting.client.equals(name)
                             ? clientValue
                         : Remoting.deferred.equals(name)
                             ? deferredValue
                         : Remoting._.equals(name)
                             ? _Value
                        : local.fetch(otherwise, name));
                     }
 
                     public void
                     link(final String name,
                          final Object value) { throw new AssertionError(); }
 
                     public String
                     export(final Object value) { throw new AssertionError(); }
 
                     public String
                     pipeline(final String m) { throw new AssertionError(); }
 
                     public String
                     getTransactionTag() { throw new AssertionError(); }
                 };
                 final Class<?> factory = code.loadClass(typename);
                 final Volatile<?> p = Eventual.promised(
                         AMP.publish(synthetic).spawn(label, factory));
                 return Remote.bind(synthetic, null).run(p.cast());
             }
         }).cast();
         System.out.println(URI.resolve(hereValue, r));
     }
 }
