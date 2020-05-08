 package de.flower.rmt.ui.app ;
 
import de.flower.rmt.ui.manager.page.player.PlayerPage;
 import org.apache.wicket.devutils.inspector.RenderPerformanceListener;
 import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
 import org.springframework.context.ApplicationContext;
 
 /**
  * @author flowerrrr
  */
 public class TestApplication extends RMTApplication {
 
     private final ApplicationContext appCtx;
 
     /**
      * Invokes the super class' default constructor and stores the given application context for looking up and
      * injecting Spring managed beans into Wicket components. This constructor is mainly intended to be used for testing
      * purposes since normally the application context will be determined using the standard Spring way via the servlet
      * context.
      *
      * @param ctx the context
      */
     public TestApplication(final ApplicationContext ctx) {
         appCtx = ctx;
     }
 
     @Override
     protected void init() {
         super.init();
         // override default settings. no problem cause we don't worry about layout bugs in unit-tests.
         getMarkupSettings().setStripWicketTags(false);
         // trace rendering time of components.
         getComponentInstantiationListeners().add(new RenderPerformanceListener());

        // required for RMT-464
        mountPage("foobar", PlayerPage.class);

     }
 
     @Override
     protected SpringComponentInjector getSpringComponentInjector() {
         return new SpringComponentInjector(this, appCtx);
     }
 
 
 }
