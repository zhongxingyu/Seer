 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.dns.editor.redirectory;
 
 import java.io.Serializable;
 
 import org.joe_e.Struct;
 import org.ref_send.promise.Promise;
 import org.waterken.dns.editor.DomainMaster;
 import org.waterken.dns.editor.Registrar;
 import org.waterken.dns.editor.RegistrarMaker;
 import org.waterken.uri.Label;
 import org.web_send.graph.Framework;
 import org.web_send.graph.Unavailable;
 
 /**
  * A {@link Redirectory} implementation.
  */
 public final class
 Main extends Struct implements Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * model framework
      */
     private final Framework framework;
 
     private
     Main(final Framework framework) {
         this.framework = framework;
     }
     
     /**
      * Constructs an instance.
      * @param framework model framework
      */
     static public Main
     build(final Framework framework) {
         return new Main(framework);
     }
     
     // org.waterken.dns.editor.redirectory.Main interface
     
     static private final String prefix = "y-";
     static private final int minChars = prefix.length() + 80 / 5;
     
     /**
     * Constructs a {@link Registrar} and publishes the redirectory interface.
      * @param suffix    hostname suffix
      */
     public Registrar
     publish(final String suffix) {
         final Registrar registrar = RegistrarMaker.build(framework);
         class RedirectoryX extends Struct implements Redirectory, Serializable {
             static private final long serialVersionUID = 1L;
 
             public Promise<DomainMaster>
             register(final String fingerprint) {
                 if (!fingerprint.startsWith(prefix)) {throw new Unavailable();}
                 if (fingerprint.length() < minChars) {throw new Unavailable();}
                 Label.vet(fingerprint);
                 return registrar.claim(fingerprint + suffix);
             }
         }
         framework.publisher.bind("redirectory", new RedirectoryX());
         return registrar;
     }
 }
