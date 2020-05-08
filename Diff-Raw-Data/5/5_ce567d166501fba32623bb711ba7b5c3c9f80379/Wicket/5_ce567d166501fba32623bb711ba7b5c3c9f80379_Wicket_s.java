 package com.wideplay.warp.servlet;
 
 import com.google.inject.Injector;
import com.google.inject.util.Objects;
 import org.apache.wicket.Application;
 import org.apache.wicket.guice.GuiceComponentInjector;
 import org.apache.wicket.protocol.http.WicketFilter;
 
 /**
  * @author Dhanji R. Prasanna (dhanji gmail com)
  */
 public final class Wicket {
     private Wicket() {
     }
 
     public static GuiceComponentInjector integrate(Application wicketApplication) {
        Objects.nonNull(wicketApplication, "Must provide a valid " + Application.class.getName()
                 + " to integrate with warp-servlet (was null)");
         final Injector injector = ContextManager.getInjector();
 
         if (null == injector)
             throw new IllegalStateException(
                     "Warp-servlet was not active, no Guice Injector context could be found. " +
                     "Did you forget to register " + WebFilter.class.getName() + " in web.xml? Or did you register " +
                     WicketFilter.class.getName() + " *above* it in web.xml, instead of inside Warp-servlet? Ideally, " +
                     "your filters and servlets should should be in Warp-servlet and not appear web.xml at all.");
 
         return new GuiceComponentInjector(wicketApplication, injector);
     }
 
 }
