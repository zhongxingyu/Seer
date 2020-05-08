 package test;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
 import java.lang.UnsupportedOperationException;
 
 import org.junit.BeforeClass;
 import org.junit.AfterClass;
 import org.junit.Before;
 
 import play.test.FakeApplication;
 import play.test.Helpers;
 
 import play.mvc.Content;
 import play.mvc.Http;
 
 import play.i18n.Lang;
 
 /**
  * If you want to run your class as if the application was actually running,
  * this may be interesting.
  * @author Felix Van der Jeugt
  */
 public abstract class ContextTest {
 
     private static FakeApplication app;
 
     /**
      * Set up the application.
      */
     @BeforeClass public static void setupApplication() {
         Map<String, String> settings = new HashMap<String, String>();
         settings.put("db.default.driver", "org.h2.Driver");
         settings.put("db.default.user", "sa");
         settings.put("db.default.password", "");
         settings.put("db.default.url", "jdbc:h2:mem:play");
         settings.put("evolutionplugin","enabled");
         app = Helpers.fakeApplication(settings);
         Helpers.start(app);
     }
 
     /**
      * Set up the context.
      */
     @Before public void setupContext() {
         Http.Context.current = new ThreadLocal<Http.Context>() {
             @Override protected Http.Context initialValue() {
                 return makeContext();
             }
         };
     }
 
     /**
      * Default context creation, override to customize.
      */
     protected Http.Context makeContext() {
         return new Http.Context(
             makeRequest(),
             makeSessionData(),
             makeFlashData()
         );
     }
 
     /**
      * Default Session creation. You can override this class and add this to the
      * session.
      * @return A Map mapping the Session keys on their values.
      */
     protected Map<String, String> makeSessionData() {
         return new HashMap<String, String>();
     }
 
     /**
      * Default FlashData creation.
      * @return The FlashData for in the context.
      */
     protected Map<String, String> makeFlashData() {
         return new HashMap<String, String>();
     }
 
     /**
      * Default Request stub. Override to implement request functions, and thus
      * mimic the kind of user request you want.
      * @return The Request for the Context.
      */
     protected Http.Request makeRequest() {
         return new StubRequest();
     }
 
     /**
      * Breaks down the application.
      */
     @AfterClass public static void breakdownApplication() {
         Helpers.stop(app);
     }
 
     protected class StubRequest extends Http.Request {
 
         @Override public Http.RequestBody body() {
             throw new UnsupportedOperationException();
         }
 
         @Override public List<String> accept() {
             throw new UnsupportedOperationException();
         }
 
         @Override public List<Lang> acceptLanguages() {
            List<Lang> l = new ArrayList<Lang>();
            l.add(Lang.forCode("en-US"));
            return l;
         }
 
         @Override public boolean accepts(String mediaType) {
             throw new UnsupportedOperationException();
         }
 
         @Override public Http.Cookies cookies() {
            return new Http.Cookies() {
                @Override public Http.Cookie get(String name) {
                    return null;
                }
            };
         }
 
         @Override public Map<String,String[]> headers() {
             throw new UnsupportedOperationException();
         }
 
         @Override public String host() {
             throw new UnsupportedOperationException();
         }
 
         @Override public String method() {
             throw new UnsupportedOperationException();
         }
 
         @Override public String path() {
             throw new UnsupportedOperationException();
         }
 
         @Override public Map<String,String[]> queryString() {
             throw new UnsupportedOperationException();
         }
 
         @Override public String remoteAddress() {
             throw new UnsupportedOperationException();
         }
 
         @Override public String uri() {
             throw new UnsupportedOperationException();
         }
 
     }
 }
