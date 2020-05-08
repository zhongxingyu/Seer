 /*-----------------------------------------------------------------------
   
 Copyright (c) 2007-2010, The University of Manchester, United Kingdom.
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without 
 modification, are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, 
       this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright 
       notice, this list of conditions and the following disclaimer in the 
       documentation and/or other materials provided with the distribution.
  * Neither the name of The University of Manchester nor the names of 
       its contributors may be used to endorse or promote products derived 
       from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 POSSIBILITY OF SUCH DAMAGE.
 
 -----------------------------------------------------------------------*/
 package uk.ac.manchester.rcs.corypha.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.naming.NameClassPair;
 import javax.naming.NameNotFoundException;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.NoInitialContextException;
 
 import org.eclipse.jetty.xml.XmlConfiguration;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.restlet.Application;
 import org.restlet.Context;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.Restlet;
 import org.restlet.data.CharacterSet;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.Directory;
 import org.restlet.routing.Filter;
 import org.restlet.routing.Router;
import org.restlet.routing.Template;
 import org.restlet.security.Authenticator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.manchester.rcs.corypha.authn.AuthenticatorConfig;
 
 import freemarker.template.Configuration;
 
 /**
  * This is the root Restlet {@link Application} of Corypha, which sets up all
  * the sub-applications and the configuration.
  * 
  * @author Bruno Harbulot
  * 
  */
 public class CoryphaRootApplication extends Application {
     private final static Logger LOGGER = LoggerFactory
             .getLogger(CoryphaRootApplication.class);
 
     public final static String MODULE_CLASSES_CTX_PARAM = "corypha_modules";
 
     public final static String MENU_PROVIDERS_CTX_ATTRIBUTE = "corypha_menu_providers";
 
     public final static String BASE_URL_CTX_PARAM = "corypha_base_url";
 
     public final static String BASE_URL_REQUEST_ATTR = "corypha_base_url";
 
     public final static String TEMPLATE_CONFIG_XML_URL_CTX_PARAM = "corypha_template_config_xml_url";
 
     public final static String AUTHN_CONFIG_XML_URL_CTX_PARAM = "corypha_authn_config_xml_url";
 
     private final CopyOnWriteArrayList<CoryphaModule> modules = new CopyOnWriteArrayList<CoryphaModule>();
 
     private final CopyOnWriteArrayList<IMenuProvider> menuProviders = new CopyOnWriteArrayList<IMenuProvider>();
 
     private final AnnotationConfiguration hibernateConfiguration = new AnnotationConfiguration();
 
     /**
      * Initialises the FreeMarker {@link Configuration} from the URI of a
      * configuration file (in Jetty's XML configuration format).
      * 
      * @param templateConfigXmlUrl
      *            a URI to be loaded from a {@link ClientResource} (can be CLAP
      *            URI).
      */
     private void loadTemplateConfig(String templateConfigXmlUrl) {
         if (templateConfigXmlUrl != null) {
             try {
                 ClientResource configResource = new ClientResource(
                         templateConfigXmlUrl);
                 Representation entity = configResource.get();
                 try {
                     if (configResource.getStatus().isSuccess()
                             && (entity != null)) {
                         Configuration freemarkerConfig = CoryphaTemplateUtil
                                 .getConfiguration(getContext());
 
                         XmlConfiguration xmlConfig = new XmlConfiguration(
                                 entity.getStream());
                         xmlConfig.configure(freemarkerConfig);
                     } else {
                         LOGGER.error(String.format(
                                 "Unable to load config file %s.",
                                 templateConfigXmlUrl));
                     }
                 } finally {
                     if (entity != null) {
                         entity.release();
                     }
                 }
             } catch (Exception e) {
                 LOGGER.error(String.format(
                         "Error while loading config from %s.",
                         templateConfigXmlUrl), e);
             }
         }
     }
 
     /**
      * Initialises the Authenticator via an {@link AuthenticatorConfig} loaded
      * from the URI of a configuration file (in Jetty's XML configuration
      * format).
      * 
      * @param authnConfigXmlUrl
      *            a URI to be loaded from a {@link ClientResource} (can be CLAP
      *            URI).
      */
     private Authenticator loadAuthnConfig(String authnConfigXmlUrl) {
         if (authnConfigXmlUrl != null) {
             try {
                 ClientResource configResource = new ClientResource(
                         authnConfigXmlUrl);
                 Representation entity = configResource.get();
                 try {
                     if (configResource.getStatus().isSuccess()
                             && (entity != null)) {
 
                         XmlConfiguration xmlConfig = new XmlConfiguration(
                                 entity.getStream());
                         AuthenticatorConfig authnConfig = new AuthenticatorConfig(
                                 getContext());
                         xmlConfig.configure(authnConfig);
                         return authnConfig.getAuthenticator();
                     } else {
                         LOGGER.error(String.format(
                                 "Unable to load config file %s.",
                                 authnConfigXmlUrl));
                     }
                 } finally {
                     if (entity != null) {
                         entity.release();
                     }
                 }
             } catch (Exception e) {
                 LOGGER.error(String.format(
                         "Error while loading config from %s.",
                         authnConfigXmlUrl), e);
             }
         }
         return null;
     }
 
     /**
      * Loads parameters passed via JNDI into the Restlet's {@link Context}.
      * 
      * @param prefix
      *            Prefix to use in java:comp/env/prefix/name.
      */
     private void loadJndiParameters(String prefix) {
         try {
             javax.naming.Context ctx = new javax.naming.InitialContext();
             javax.naming.Context env = (javax.naming.Context) ctx
                     .lookup("java:comp/env");
 
             NamingEnumeration<NameClassPair> nameClassPairs = null;
             try {
                 nameClassPairs = env.list(prefix);
             } catch (NameNotFoundException e) {
                 LOGGER.info(String
                         .format("NameNotFoundException in loadJndiParameters(%s) for %s.",
                                 prefix, e.getRemainingName()));
             }
             if (nameClassPairs != null) {
                 while (nameClassPairs.hasMore()) {
                     NameClassPair nameClassPair = nameClassPairs.next();
                     StringBuffer sb = new StringBuffer(prefix);
                     Object object = env.lookup(sb.append("/")
                             .append(nameClassPair.getName()).toString());
                     if (object != null) {
                         getContext().getParameters().add(
                                 nameClassPair.getName(), object.toString());
                     } else {
                         LOGGER.warn(String.format(
                                 "Null object for java:comp/env/%s/%s", prefix,
                                 nameClassPair.getName()));
                     }
                 }
             }
         } catch (NoInitialContextException e) {
             LOGGER.warn("No Initial context, unable to use loadJndiParameters().");
         } catch (NameNotFoundException e) {
             if ("env".equals(e.getRemainingName().toString())) {
                 LOGGER.warn("Unable to load java:comp/env, unable to use loadJndiParameters().");
             } else {
                 LOGGER.error(
                         String.format(
                                 "NameNotFoundException in loadJndiParameters(%s) for %s.",
                                 prefix, e.getRemainingName()), e);
                 throw new RuntimeException(e);
             }
         } catch (NamingException e) {
             LOGGER.error(String.format(
                     "NamingException in loadJndiParameters(%s) for %s.",
                     prefix, e.getRemainingName()), e);
             throw new RuntimeException(e);
         }
     }
 
     private void loadJndiParameters() {
         loadJndiParameters("parameters");
     }
 
     /**
      * Loads attributes passed via JNDI into the Restlet's {@link Context}.
      * 
      * @param prefix
      *            Prefix to use in java:comp/env/prefix/name.
      */
     private void loadJndiAttributes(String prefix) {
         try {
             javax.naming.Context ctx = new javax.naming.InitialContext();
             javax.naming.Context env = (javax.naming.Context) ctx
                     .lookup("java:comp/env");
 
             NamingEnumeration<NameClassPair> nameClassPairs = null;
             try {
                 nameClassPairs = env.list(prefix);
             } catch (NameNotFoundException e) {
                 LOGGER.info(String
                         .format("NameNotFoundException in loadJndiAttributes(%s) for %s.",
                                 prefix, e.getRemainingName()));
             }
             if (nameClassPairs != null) {
                 while (nameClassPairs.hasMore()) {
                     NameClassPair nameClassPair = nameClassPairs.next();
                     StringBuffer sb = new StringBuffer(prefix);
                     Object object = env.lookup(sb.append("/")
                             .append(nameClassPair.getName()).toString());
                     getContext().getAttributes().put(nameClassPair.getName(),
                             object);
                 }
             }
         } catch (NoInitialContextException e) {
             LOGGER.warn("No Initial context, unable to use loadJndiAttributes().");
         } catch (NameNotFoundException e) {
             if ("env".equals(e.getRemainingName().toString())) {
                 LOGGER.warn("Unable to load java:comp/env, unable to use loadJndiAttributes().");
             } else {
                 LOGGER.error(
                         String.format(
                                 "NameNotFoundException in loadJndiAttributes(%s) for %s.",
                                 prefix, e.getRemainingName()), e);
                 throw new RuntimeException(e);
             }
         } catch (NamingException e) {
             LOGGER.error(String.format(
                     "NamingException in loadJndiAttributes(%s) for %s.",
                     prefix, e.getRemainingName()), e);
             throw new RuntimeException(e);
         }
     }
 
     private void loadJndiAttributes() {
         loadJndiAttributes("attributes");
     }
 
     /**
      * Configures Hibernate, first by trying to load from hibernate.cfg.xml on
      * the classpath, then by using JNDI parameters (e.g.
      * java:comp/env/hibernate/connection).
      */
     private void configureHibernate() {
         InputStream hibernateCfgInputStream = AnnotationConfiguration.class
                 .getResourceAsStream("/hibernate.cfg.xml");
         if (hibernateCfgInputStream != null) {
             try {
                 hibernateCfgInputStream.close();
             } catch (IOException e) {
                 LOGGER.error("Error while trying to close the hibernate.cfg.xml input stream");
             }
             this.hibernateConfiguration.configure();
         }
 
         String prefix = "hibernate";
         try {
             javax.naming.Context ctx = new javax.naming.InitialContext();
             javax.naming.Context env = (javax.naming.Context) ctx
                     .lookup("java:comp/env");
 
             NamingEnumeration<NameClassPair> nameClassPairs = null;
             try {
                 nameClassPairs = env.list(prefix);
             } catch (NameNotFoundException e) {
                 LOGGER.info("NameNotFoundException in configureHibernate().");
             }
             if (nameClassPairs != null) {
                 while (nameClassPairs.hasMore()) {
                     NameClassPair nameClassPair = nameClassPairs.next();
                     StringBuffer sb = new StringBuffer(prefix);
                     Object object = env.lookup(sb.append("/")
                             .append(nameClassPair.getName()).toString());
                     if (object != null) {
                         this.hibernateConfiguration.setProperty(
                                 String.format("hibernate.%s",
                                         nameClassPair.getName()),
                                 object.toString());
                         LOGGER.info(String.format(
                                 "Setting hibernate property: %s = %s",
                                 nameClassPair.getName(), object.toString()));
                     } else {
                         LOGGER.warn(String.format(
                                 "Null object for java:comp/env/%s/%s", prefix,
                                 nameClassPair.getName()));
                     }
                 }
             }
         } catch (NoInitialContextException e) {
             LOGGER.warn("No Initial context, unable to use JNDI for configureHibernate().");
         } catch (NameNotFoundException e) {
             if ("env".equals(e.getRemainingName().toString())) {
                 LOGGER.warn("Unable to load java:comp/env, unable to use configureHibernate().");
             } else {
                 LOGGER.error("NameNotFoundException in configureHibernate()).",
                         e);
                 throw new RuntimeException(e);
             }
         } catch (NamingException e) {
             LOGGER.error("NamingException in configureHibernate()).", e);
             throw new RuntimeException(e);
         }
 
         getContext().getAttributes().put(
                 HibernateFilter.HIBERNATE_CONFIGURATION_ATTRIBUTE,
                 this.hibernateConfiguration);
     }
 
     @Override
     public Restlet createInboundRoot() {
         loadJndiParameters();
         loadJndiAttributes();
 
         getMetadataService().setDefaultCharacterSet(CharacterSet.UTF_8);
 
         String templateConfigXmlUrl = getContext().getParameters()
                 .getFirstValue(TEMPLATE_CONFIG_XML_URL_CTX_PARAM,
                         "clap://thread/corypha-template.cfg.xml");
         loadTemplateConfig(templateConfigXmlUrl);
 
         String authnConfigXmlUrl = getContext().getParameters().getFirstValue(
                 AUTHN_CONFIG_XML_URL_CTX_PARAM,
                 "clap://thread/corypha-authn.cfg.xml");
         Authenticator authenticator = loadAuthnConfig(authnConfigXmlUrl);
 
         final String baseUrl = getContext().getParameters().getFirstValue(
                 BASE_URL_CTX_PARAM);
         if (baseUrl == null) {
             LOGGER.info(String
                     .format("No base url defined (%s context parameter): will be inferred from the requests.",
                             BASE_URL_CTX_PARAM));
         } else {
             LOGGER.info(String.format("Using base reference: %s", baseUrl));
         }
 
         Router router = new BaseSetterRouter(getContext(), baseUrl);
        router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
 
         Map<String, CoryphaApplication> prefixToCmsApps = new HashMap<String, CoryphaApplication>();
 
         String[] cmsApplicationProviderClassNames = getContext()
                 .getParameters().getValuesArray(MODULE_CLASSES_CTX_PARAM);
 
         getContext().getAttributes().put(MENU_PROVIDERS_CTX_ATTRIBUTE,
                 this.menuProviders);
 
         Router htdocsRouter = new Router(getContext());
         router.attach("/htdocs", htdocsRouter);
         loadModule(router, htdocsRouter, prefixToCmsApps, new DefaultModule());
 
         for (String cmsModuleClassName : cmsApplicationProviderClassNames) {
             try {
                 Class<?> cmsModuleClass = Class.forName(cmsModuleClassName);
                 if (CoryphaModule.class.isAssignableFrom(cmsModuleClass)) {
                     CoryphaModule cmsModule = (CoryphaModule) cmsModuleClass
                             .newInstance();
 
                     loadModule(router, htdocsRouter, prefixToCmsApps, cmsModule);
                 } else {
                     LOGGER.error(String.format(
                             "Cannot load %s since it is not a subclass of %s",
                             cmsModuleClassName, CoryphaModule.class));
                 }
             } catch (ClassNotFoundException e) {
                 LOGGER.error(String.format("Cannot load %s: class not found.",
                         cmsModuleClassName), e);
             } catch (InstantiationException e) {
                 LOGGER.error(String.format(
                         "Cannot load %s: cannot instantiate.",
                         cmsModuleClassName), e);
             } catch (IllegalAccessException e) {
                 LOGGER.error(String.format("Cannot load %s: illegal access.",
                         cmsModuleClassName), e);
             }
         }
 
         // TODO remove hard-coding of path.
         Directory htdocsCoreDirectory = new Directory(getContext(),
                 "clap://thread/uk/ac/manchester/rcs/corypha/core/htdocs/");
         htdocsRouter.attach("/core/", htdocsCoreDirectory);
         Directory htdocsJqueryDatatablesDirectory = new Directory(getContext(),
                 "clap://thread/uk/ac/manchester/rcs/corypha/external/jquery-datatables/htdocs/");
         htdocsRouter.attach("/jquery-datatables/",
                 htdocsJqueryDatatablesDirectory);
         Directory htdocsJqueryUiDirectory = new Directory(getContext(),
                 "clap://thread/uk/ac/manchester/rcs/corypha/external/jquery-ui/htdocs/");
         htdocsRouter.attach("/jquery-ui/", htdocsJqueryUiDirectory);
         Directory htdocsJqueryDirectory = new Directory(getContext(),
                 "clap://thread/uk/ac/manchester/rcs/corypha/external/jquery/htdocs/");
         htdocsRouter.attach("/jquery/", htdocsJqueryDirectory);
 
         configureHibernate();
 
         if (authenticator != null) {
             Filter lastAuthenticator = authenticator;
             while (lastAuthenticator.getNext() != null) {
                 if (lastAuthenticator.getNext() instanceof Filter) {
                     lastAuthenticator = (Filter) lastAuthenticator.getNext();
                 } else {
                     LOGGER.error("The authenticator chain contains Restlets that are not Filters: "
                             + lastAuthenticator.getClass());
                 }
             }
             lastAuthenticator.setNext(router);
             return authenticator;
         } else {
             return router;
         }
     }
 
     /**
      * @param router
      * @param prefixToCmsApps
      * @param cmsModuleClassName
      * @param cmsModule
      */
     private void loadModule(Router router, Router htdocsRouter,
             Map<String, CoryphaApplication> prefixToCmsApps,
             CoryphaModule cmsModule) {
         this.modules.add(cmsModule);
 
         if (cmsModule instanceof IApplicationProvider) {
             CoryphaApplication cmsApplication = ((IApplicationProvider) cmsModule)
                     .getApplication();
             if (cmsApplication != null) {
                 cmsApplication.setContext(getContext());
                 String autoPrefix = cmsApplication.getAutoPrefix();
                 if (!prefixToCmsApps.containsKey(autoPrefix)) {
                     if (autoPrefix.length() == 0) {
                         router.attachDefault(cmsApplication);
                     } else {
                         router.attach("/" + autoPrefix, cmsApplication);
                         Restlet htdocsRestlet = cmsApplication
                                 .getHtdocsRestlet();
                         if (htdocsRestlet != null) {
                             htdocsRouter
                                     .attach("/" + autoPrefix, htdocsRestlet);
                         }
                     }
                     prefixToCmsApps.put(autoPrefix, cmsApplication);
                     LOGGER.info(String.format(
                             "Loaded application from %s at prefix %s.",
                             cmsModule.getClass(), autoPrefix));
                 } else {
                     LOGGER.error(String
                             .format("Cannot load application from %s: prefix %s already in use.",
                                     cmsModule.getClass(), autoPrefix));
                 }
             } else {
                 LOGGER.warn(String.format(
                         "No application in this application provider: %s.",
                         cmsModule.getClass()));
             }
         }
 
         if (cmsModule instanceof IMenuProvider) {
             this.menuProviders.add((IMenuProvider) cmsModule);
         }
 
         if (cmsModule instanceof IHibernateConfigurationContributor) {
             ((IHibernateConfigurationContributor) cmsModule)
                     .configureHibernate(this.hibernateConfiguration);
         }
     }
 
     private static final class BaseSetterRouter extends Router {
         private final String baseUrl;
 
         private BaseSetterRouter(Context context, String baseUrl) {
             super(context);
             this.baseUrl = baseUrl;
         }
 
         @Override
         public void handle(Request request, Response response) {
             if (baseUrl == null) {
                 request.getAttributes().put(BASE_URL_REQUEST_ATTR,
                         request.getRootRef().toString() + "/");
             }
             super.handle(request, response);
         }
     }
 }
