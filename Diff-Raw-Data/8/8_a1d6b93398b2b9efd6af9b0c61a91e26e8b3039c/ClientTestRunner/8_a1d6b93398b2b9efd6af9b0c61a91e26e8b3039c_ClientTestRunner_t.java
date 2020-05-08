 package cryptocast.client;
 
 import java.io.File;
 import java.lang.reflect.Method;
 
 import org.junit.runners.model.InitializationError;
 
 import android.app.Application;
 
 import com.xtremelabs.robolectric.Robolectric;
 import com.xtremelabs.robolectric.RobolectricConfig;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 
 public class ClientTestRunner extends RobolectricTestRunner {
     public static ClientApplication application;
 
     public ClientTestRunner(Class<?> testClass) throws InitializationError {
         // use the client project root as the working directory for
         // out client tests
        super(testClass, new RobolectricConfig(new File("../client")));
     }
 
     // for some reasons, Robolectric doesn't call onCreate() on
     // a created application instance. However, we rely on this.
     @Override
     protected Application createApplication() {
         Application app = super.createApplication();
         app.onCreate();
         return app;
     }
     
     @Override
     public void beforeTest(Method method) {
         application = (ClientApplication) Robolectric.application;
     }
 }
