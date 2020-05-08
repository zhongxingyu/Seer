 package com.justdavis.karl.rpstourney.webservice;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.servlet.ServletContainerInitializer;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletRegistration;
 import javax.ws.rs.core.Application;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.ext.RuntimeDelegate;
 
 import org.apache.cxf.bus.spring.SpringBus;
 import org.apache.cxf.endpoint.Server;
 import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
 import org.apache.cxf.transport.servlet.CXFServlet;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
 import org.springframework.web.SpringServletContainerInitializer;
 import org.springframework.web.WebApplicationInitializer;
 import org.springframework.web.context.ContextLoaderListener;
 import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
 
 import com.justdavis.karl.rpstourney.webservice.auth.AccountSecurityContext.AccountSecurityContextProvider;
 import com.justdavis.karl.rpstourney.webservice.auth.AccountService;
 import com.justdavis.karl.rpstourney.webservice.auth.AuthenticationFilter;
 import com.justdavis.karl.rpstourney.webservice.auth.AuthorizationFilter.AuthorizationFilterFeature;
 import com.justdavis.karl.rpstourney.webservice.auth.game.GameAuthService;
 import com.justdavis.karl.rpstourney.webservice.auth.game.InternetAddressReader;
 import com.justdavis.karl.rpstourney.webservice.auth.guest.GuestAuthService;
 import com.justdavis.karl.rpstourney.webservice.demo.HelloWorldServiceImpl;
 
 /**
  * <p>
  * Initializes the JAX-RS game web service application via Spring.
  * </p>
  * <p>
  * If deployed in a Servlet 3.0 container, this application will be found and
  * loaded automagically, as this project includes Spring's
  * {@link SpringServletContainerInitializer} in its dependencies. This class is
  * a registered {@link ServletContainerInitializer} SPI and will in turn search
  * for and enable any {@link WebApplicationInitializer} implementations (such as
  * this class).
  * </p>
  * 
  * @see AppConfig
  */
public final class GameApplicationInitializer implements
		WebApplicationInitializer {
 	/**
 	 * @see org.springframework.web.WebApplicationInitializer#onStartup(javax.servlet.ServletContext)
 	 */
 	@Override
 	public void onStartup(ServletContext container) {
 		// Create the 'root' Spring application context
 		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
 		rootContext.register(AppConfig.class);
 
 		// Manage the lifecycle of the root application context
 		container.addListener(new ContextLoaderListener(rootContext));
 
 		// Register the Servlet that will handle the Apache CXF JAX-RS services.
 		CXFServlet cxfServlet = new CXFServlet();
 		ServletRegistration.Dynamic cxfServletReg = container.addServlet(
 				"CXFServlet", cxfServlet);
 		cxfServletReg.setLoadOnStartup(1);
 		cxfServletReg.addMapping("/*");
 	}
 
 	/**
 	 * Provides the primary Spring {@link Configuration} for the JAX-RS
 	 * application.
 	 */
 	@Configuration
 	static class AppConfig {
 		/**
 		 * @return Returns the {@link SpringBus} that the CXF application uses.
 		 *         Such a {@link SpringBus} instance <strong>must</strong> be
 		 *         provided in the application's {@link Configuration}.
 		 */
 		@Bean(destroyMethod = "shutdown")
 		SpringBus cxf() {
 			return new SpringBus();
 		}
 
 		/**
 		 * @return Returns the {@link Application} instance that the JAX-RS
 		 *         application uses. Such an {@link Application} instance
 		 *         <strong>must</strong> be provided in the application's
 		 *         {@link Configuration}.
 		 */
 		@Bean
 		GameApplication jaxRsApiApplication() {
 			return new GameApplication();
 		}
 
 		/**
 		 * @return Returns the {@link Server} instance that the CXF application
 		 *         uses. Such an {@link Server} instance <strong>must</strong>
 		 *         be provided in the application's {@link Configuration}. This
 		 *         largely replaces the resource/provider declarations that
 		 *         would be included in an {@link Application} instance for
 		 *         non-Spring JAX-RS applications.
 		 */
 		@Bean
		@DependsOn("cxf")
 		Server jaxRsServer() {
 			JAXRSServerFactoryBean factory = RuntimeDelegate.getInstance()
 					.createEndpoint(jaxRsApiApplication(),
 							JAXRSServerFactoryBean.class);
 
 			factory.setProviders(getProviders());
 
 			/*
 			 * JAXRSServerFactoryBean.setResourceClasses(...) is analogous to
 			 * JAX-RS's Application.getClasses(), but only for web service
 			 * resources.
 			 */
 			factory.setResourceClasses(getResourceClasses());
 
 			/*
 			 * JAXRSServerFactoryBean.setServiceBeans(...) is analogous to
 			 * JAX-RS's Application.getSingletons(), but only for web service
 			 * resources.
 			 */
 			factory.setServiceBeans(Arrays
 					.<Object> asList(helloWorldResource()));
 
 			return factory.create();
 		}
 
 		/**
 		 * @return The {@link List} of JAX-RS resource classes that Spring/CXF
 		 *         should register to handle webservice requests via
 		 *         {@link JAXRSServerFactoryBean#setResourceClasses(Class...)}.
 		 *         Any services specified here will be instantiated once per
 		 *         request. This should be used for any resource classes that
 		 *         get request-specific data injected, e.g. {@link UriInfo}
 		 *         {@link Context} objects.
 		 */
 		private static List<Class<?>> getResourceClasses() {
 			List<Class<?>> classes = new LinkedList<>();
 
 			// Register the filters.
 			classes.add(AuthenticationFilter.class);
 
 			// Register the resources.
 			// classes.add(HelloWorldServiceImpl.class);
 			classes.add(AccountService.class);
 			classes.add(GuestAuthService.class);
 			classes.add(GameAuthService.class);
 
 			// Register any custom context providers.
 			classes.add(AccountSecurityContextProvider.class);
 
 			return classes;
 		}
 
 		/**
 		 * @return The {@link List} of JAX-RS/CXF providers that Spring/CXF
 		 *         should register via
 		 *         {@link JAXRSServerFactoryBean#setProviders(List)}. Basically,
 		 *         this would be anything else that can be a singleton that
 		 *         might have been specified in {@link Application#getClasses()}
 		 *         or {@link Application#getSingletons()}.
 		 */
 		private static List<Object> getProviders() {
 			List<Object> providers = new LinkedList<>();
 
 			/*
 			 * Right now, these instances are all being created manually.
 			 * However, if any of them require injection, they'd need to be
 			 * injected into this class as annotated @Bean instances.
 			 */
 
 			// Register the entity translators.
 			providers.add(new InternetAddressReader());
 
 			// Register the filters.
 			providers.add(new AuthenticationFilter());
 
 			// Register any custom context providers.
 			providers.add(new AccountSecurityContextProvider());
 
 			// Register any custom context providers.
 			providers.add(new AuthorizationFilterFeature());
 
 			return providers;
 		}
 
 		@Bean
 		HelloWorldServiceImpl helloWorldResource() {
 			return new HelloWorldServiceImpl();
 		}
 	}
 }
