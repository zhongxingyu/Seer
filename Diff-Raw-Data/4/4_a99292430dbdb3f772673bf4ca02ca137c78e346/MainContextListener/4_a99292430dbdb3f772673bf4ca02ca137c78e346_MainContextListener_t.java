 package edu.gatech.oad.rocket.findmythings.server;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.Locale;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 
import edu.gatech.oad.rocket.findmythings.server.web.ForgotServlet;
 import org.apache.shiro.authc.credential.CredentialsMatcher;
 import org.apache.shiro.authc.credential.PasswordMatcher;
 import org.apache.shiro.cache.CacheManager;
 import org.apache.shiro.config.Ini;
 import org.apache.shiro.guice.web.ShiroWebModule;
 import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
 import org.apache.shiro.session.mgt.eis.SessionDAO;
 
 import com.google.common.base.Charsets;
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.binder.ConstantBindingBuilder;
 import com.google.inject.name.Names;
 import com.google.inject.servlet.GuiceServletContextListener;
 import com.google.inject.servlet.ServletModule;
 import com.googlecode.objectify.ObjectifyFilter;
 
 import edu.gatech.oad.rocket.findmythings.server.api.AuthTestEndpoint;
 import edu.gatech.oad.rocket.findmythings.server.api.ForgotEndpoint;
 import edu.gatech.oad.rocket.findmythings.server.api.LoginEndpoint;
 import edu.gatech.oad.rocket.findmythings.server.api.RegisterEndpoint;
 import edu.gatech.oad.rocket.findmythings.server.db.DatabaseService.DatabaseFactory;
 import edu.gatech.oad.rocket.findmythings.server.db.MemcacheManager;
 import edu.gatech.oad.rocket.findmythings.server.security.BearerTokenAuthenticatingRealm;
 import edu.gatech.oad.rocket.findmythings.server.security.BearerTokenAuthenticatingFilter;
 import edu.gatech.oad.rocket.findmythings.server.security.BearerTokenRevokeFilter;
 import edu.gatech.oad.rocket.findmythings.server.security.DatabaseRealm;
 import edu.gatech.oad.rocket.findmythings.server.security.ProfileIniRealm;
 import edu.gatech.oad.rocket.findmythings.server.security.WebAuthenticationFilter;
 import edu.gatech.oad.rocket.findmythings.server.service.MailboxServlet;
 import edu.gatech.oad.rocket.findmythings.server.service.MailmanServlet;
 import edu.gatech.oad.rocket.findmythings.server.util.Config;
 import edu.gatech.oad.rocket.findmythings.server.util.Envelope;
 import edu.gatech.oad.rocket.findmythings.server.web.ActivateServlet;
 import edu.gatech.oad.rocket.findmythings.server.web.RegisterServlet;
 
 public class MainContextListener extends GuiceServletContextListener {
 
 	public static final Key<WebAuthenticationFilter> FORMAUTHC = Key.get(WebAuthenticationFilter.class);
 	public static final Key<BearerTokenAuthenticatingFilter> TOKENAUTHC = Key.get(BearerTokenAuthenticatingFilter.class);
 	public static final Key<BearerTokenRevokeFilter> TOKENLOGOUT = Key.get(BearerTokenRevokeFilter.class);
 
     private ServletContext servletContext = null;
 
 	public MainContextListener() {}
 
 	@Override
 	public void contextInitialized(ServletContextEvent servletContextEvent) {
 		servletContext = servletContextEvent.getServletContext();
 		super.contextInitialized(servletContextEvent);
 	}
 
 	@Override
 	protected Injector getInjector() {
 		return Guice.createInjector(new MainShiroWebModule(servletContext), ShiroWebModule.guiceFilterModule(), new MainServletModule(), new MainModule());
 	}
 
 	private class MainServletModule extends ServletModule {
 	    private <T> void bindNamed(String key, Class<T> clazz, T value) {
 	        bind(clazz).annotatedWith(Names.named(key)).toInstance(value);
 	    }
 		
 		private ConstantBindingBuilder bindConstant(String key) {
 			return bindConstant().annotatedWith(Names.named(key));
 		}
 
 		@Override protected void configureServlets() {
 			filter("/*").through(ObjectifyFilter.class);
 			
 			try {
 				bindNamed(PageGenerator.TEMPLATES, URL.class, getServletContext().getResource("/WEB-INF/templates/"));
 				bindNamed(PageGenerator.LOCALE, Locale.class, Locale.getDefault());
 				bindNamed(PageGenerator.CHARSET, Charset.class, Charsets.UTF_8);
 			} catch (MalformedURLException e) {
 				throw new RuntimeException(e);
 			}
 	        
 			serve("/sendMail").with(MailboxServlet.class);
 
 			serve("/api/login").with(LoginEndpoint.class);
 			serve("/api/register").with(RegisterEndpoint.class);
 			serve("/api/forgot").with(ForgotEndpoint.class);
 
 			serve("/login").with(SimpleTemplateServlet.class);
 			serve("/register").with(RegisterServlet.class);
			serve("/forgot").with(ForgotServlet.class);
 			serve("/activate").with(ActivateServlet.class);
 
 			serve("/api/authtest").with(AuthTestEndpoint.class);
 			serve("/authtest").with(SimpleTemplateServlet.class);
 
 			serve("/").with(SimpleTemplateServlet.class);
 			serve("/about").with(SimpleTemplateServlet.class);
 			serve("/contact").with(SimpleTemplateServlet.class);
 
 			serve("/_ah/mail/*").with(MailmanServlet.class);
 
 			// set the login redirect URLs
 			bindConstant(Envelope.SENDER).to(Config.APP_EMAIL);
 									
 			bindConstant(Config.Keys.LOGIN_URL).to(Config.LOGIN_URL);
 			bindConstant(Config.Keys.LOGIN_SUCCESS_URL).to(Config.SUCCESS_URL);
 			bindConstant(Config.Keys.LOGIN_API_URL).to(Config.LOGIN_API_URL);
 			
 			bindConstant(Config.Keys.USERNAME).to(Config.USERNAME_PARAM);
 			bindConstant(Config.Keys.PASSWORD).to(Config.PASSWORD_PARAM);
 			bindConstant(Config.Keys.REMEMBER_ME).to(Config.REMEMBER_ME_PARAM);
 		}
 
 	}
 
 	private static class MainModule extends AbstractModule {
 		@Override
 		protected void configure() {
 			requestStaticInjection(DatabaseFactory.class);
 
 			// External things that don't have Guice annotations
 			bind(ObjectifyFilter.class).in(Singleton.class);
 		}
 	}
 
 	private class MainShiroWebModule extends ShiroWebModule {
 
 		public MainShiroWebModule(ServletContext servletContext) {
 			super(servletContext);
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void configureShiroWeb() {
             bind(SessionDAO.class).to(EnterpriseCacheSessionDAO.class);
             bind(CacheManager.class).to(MemcacheManager.class);
 
 			try {
 				bindRealm().to(BearerTokenAuthenticatingRealm.class);
 				bindRealm().to(DatabaseRealm.class);
 				bindRealm().toConstructor(ProfileIniRealm.class.getConstructor(Ini.class));
             } catch (NoSuchMethodException e) {
                 addError(e);
             }
 
 			// Always remember to define your filter chains based on a FIRST MATCH WINS policy!
 			addFilterChain("/login", FORMAUTHC);
 			addFilterChain("/account", FORMAUTHC);
 			addFilterChain("/logout", LOGOUT);
 			
 			addFilterChain("/api/login", NO_SESSION_CREATION, TOKENAUTHC);
 			addFilterChain("/api/register", NO_SESSION_CREATION, ANON);
 			addFilterChain("/api/forgot", NO_SESSION_CREATION, ANON);
 			addFilterChain("/api/logout", NO_SESSION_CREATION, TOKENLOGOUT);
 			
 			addFilterChain("/admin/**", FORMAUTHC, config(ROLES, "admin"));
 			addFilterChain("/api/user/**", NO_SESSION_CREATION, TOKENAUTHC);
 			addFilterChain("/api/admin/**", NO_SESSION_CREATION, TOKENAUTHC, config(ROLES, "admin"));
 			addFilterChain("/api/**", NO_SESSION_CREATION, config(TOKENAUTHC, "permissive"));
 			addFilterChain("/**", ANON);
 
 			// bind all password matching to the secure password hash
 		    bind(CredentialsMatcher.class).to(PasswordMatcher.class);
 		}
 
 		@Provides @Singleton
 		Ini loadShiroIni() throws MalformedURLException {
 			URL iniUrl = servletContext.getResource("/WEB-INF/shiro.ini");
 			return Ini.fromResourcePath("url:" + iniUrl.toExternalForm());
 		}
 
 	}
 
 }
