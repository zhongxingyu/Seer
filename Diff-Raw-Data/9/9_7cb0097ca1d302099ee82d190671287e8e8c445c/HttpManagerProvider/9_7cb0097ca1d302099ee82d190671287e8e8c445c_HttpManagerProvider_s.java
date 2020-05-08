 package polly.core.http;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 
 import de.skuzzle.polly.sdk.Configuration;
 import de.skuzzle.polly.sdk.ConfigurationProvider;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.constraints.AttributeConstraint;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 
 
 import polly.configuration.ConfigurationProviderImpl;
 import polly.core.ShutdownManagerImpl;
 import polly.core.http.actions.IRCPageHttpAction;
 import polly.core.http.actions.LoginHttpAction;
 import polly.core.http.actions.LogoutHttpAction;
 import polly.core.http.actions.RoleHttpAction;
 import polly.core.http.actions.RootHttpAction;
 import polly.core.http.actions.SessionPageHttpAction;
 import polly.core.http.actions.UserInfoPageHttpAction;
 import polly.core.http.actions.UserPageHttpAction;
 import polly.core.http.actions.ShutdownHttpAction;
 import polly.core.mypolly.MyPollyImpl;
 import polly.core.users.UserManagerImpl;
 import polly.moduleloader.AbstractProvider;
 import polly.moduleloader.ModuleLoader;
 import polly.moduleloader.SetupException;
 import polly.moduleloader.annotations.Module;
 import polly.moduleloader.annotations.Provide;
 import polly.moduleloader.annotations.Require;
 
 
 
 @Module(
     requires = {
         @Require(component = ConfigurationProviderImpl.class),
         @Require(component = ShutdownManagerImpl.class)
     },
     provides = @Provide(component = HttpManagerImpl.class)
 )
 public class HttpManagerProvider extends AbstractProvider {
 
     private final static Logger logger = Logger.getLogger(HttpManagerProvider.class
         .getName());
     
     public final static String HTTP_CONFIG = "http.cfg";
     public final static String HOME_PAGE = "HOME_PAGE";
     
     private HttpManagerImpl httpManager;
     private Configuration serverCfg;
     
     
     public HttpManagerProvider(ModuleLoader loader) {
         super("HTTP_SERVER_PROVIDER", loader, false);
     }
 
     
     
     @Override
     public void setup() throws SetupException {
         ConfigurationProvider configProvider = this.requireNow(
                 ConfigurationProviderImpl.class, true);
         
         try {
             this.serverCfg = configProvider.open(HTTP_CONFIG);
         } catch (IOException e) {
             throw new SetupException(e);
         }
         
         
         File templateRoot = new File(
                 this.serverCfg.readString(Configuration.HTTP_TEMPLATE_ROOT));
         int sessionTimeOut = this.serverCfg.readInt(Configuration.HTTP_SESSION_TIMEOUT);
         int port = this.serverCfg.readInt(Configuration.HTTP_PORT);
         String publicHost = this.serverCfg.readString(Configuration.HTTP_PUBLIC_HOST);
         String encoding = configProvider.getRootConfiguration().readString(
                 Configuration.ENCODING);
         int cacheThreshold = serverCfg.readInt(
                 Configuration.HTTP_SESSION_CACHE_THRESHOLD);
         
         this.httpManager = new HttpManagerImpl(
             templateRoot, publicHost,
             port, sessionTimeOut, encoding, cacheThreshold);
         
         this.provideComponent(this.httpManager);
         ShutdownManagerImpl shutdownManager = this.requireNow(
                 ShutdownManagerImpl.class, true);
         shutdownManager.addDisposable(this.httpManager);
     }
     
     
     
     @Override
     public void run() throws Exception {
         
         // Add HOME_PAGE attribute
         AttributeConstraint constraint = new AttributeConstraint() {
             @Override
             public boolean accept(String value) {
                 return httpManager.actionExists(value);
             }
         };
         
         
         UserManagerImpl userManager = this.requireNow(UserManagerImpl.class, false);
         try {
             userManager.addAttribute(HOME_PAGE, "/", constraint);
         } catch (DatabaseException e) {
            throw new SetupException(e);
         }
         
         
         MyPolly myPolly = this.requireNow(MyPollyImpl.class, false);
         // HACK: this avoids cyclic dependency
         this.httpManager.setMyPolly(myPolly);
         
         this.httpManager.addHttpAction(new RootHttpAction(myPolly));
         this.httpManager.addHttpAction(new LoginHttpAction(myPolly));
         this.httpManager.addHttpAction(new LogoutHttpAction(myPolly));
         this.httpManager.addHttpAction(new UserPageHttpAction(myPolly));
         this.httpManager.addHttpAction(new UserInfoPageHttpAction(myPolly));
         this.httpManager.addHttpAction(new IRCPageHttpAction(myPolly));
         this.httpManager.addHttpAction(new RoleHttpAction(myPolly));
         this.httpManager.addHttpAction(new SessionPageHttpAction(myPolly));
         this.httpManager.addHttpAction(new ShutdownHttpAction(myPolly));
         
         this.httpManager.addMenuUrl("Admin", "Users");
         this.httpManager.addMenuUrl("Admin", "IRC");
         this.httpManager.addMenuUrl("Admin", "Logs");
         this.httpManager.addMenuUrl("Admin", "Roles");
         this.httpManager.addMenuUrl("Admin", "Sessions");
 
         
         boolean start = this.serverCfg.readBoolean(Configuration.HTTP_START_SERVER);
         if (!start) {
             return;
         }
         
         try {
             this.httpManager.startWebServer();
             logger.info("Webserver started");
         } catch (IOException e) {
             logger.error("Error while starting webserver: ", e);
             throw new SetupException(e);
         }
     }
 
 }
