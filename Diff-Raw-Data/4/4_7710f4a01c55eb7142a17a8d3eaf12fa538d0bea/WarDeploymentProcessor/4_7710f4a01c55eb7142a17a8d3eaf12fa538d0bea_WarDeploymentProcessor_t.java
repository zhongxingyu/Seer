 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2010, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.as.undertow.deployment;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.Filter;
 import javax.servlet.Servlet;
 import javax.servlet.ServletContainerInitializer;
 
 import io.undertow.jsp.JspServletBuilder;
 import io.undertow.server.handlers.blocking.BlockingHttpServerExchange;
 import io.undertow.servlet.api.ClassIntrospecter;
 import io.undertow.servlet.api.DeploymentInfo;
 import io.undertow.servlet.api.ErrorPage;
 import io.undertow.servlet.api.FilterInfo;
 import io.undertow.servlet.api.InstanceFactory;
 import io.undertow.servlet.api.InstanceHandle;
 import io.undertow.servlet.api.ListenerInfo;
 import io.undertow.servlet.api.MimeMapping;
 import io.undertow.servlet.api.ServletContainerInitializerInfo;
 import io.undertow.servlet.api.ServletInfo;
 import io.undertow.servlet.api.ThreadSetupAction;
 import io.undertow.servlet.util.ConstructorInstanceFactory;
 import io.undertow.servlet.util.ImmediateInstanceFactory;
 import org.apache.jasper.deploy.FunctionInfo;
 import org.apache.jasper.deploy.JspPropertyGroup;
 import org.apache.jasper.deploy.TagAttributeInfo;
 import org.apache.jasper.deploy.TagFileInfo;
 import org.apache.jasper.deploy.TagInfo;
 import org.apache.jasper.deploy.TagLibraryInfo;
 import org.apache.jasper.deploy.TagLibraryValidatorInfo;
 import org.apache.jasper.deploy.TagVariableInfo;
 import org.apache.jasper.servlet.JspServlet;
 import org.jboss.annotation.javaee.Icon;
 import org.jboss.as.controller.PathElement;
 import org.jboss.as.ee.component.EEModuleDescription;
 import org.jboss.as.naming.ManagedReference;
 import org.jboss.as.server.deployment.Attachments;
 import org.jboss.as.server.deployment.DeploymentPhaseContext;
 import org.jboss.as.server.deployment.DeploymentUnit;
 import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
 import org.jboss.as.server.deployment.DeploymentUnitProcessor;
 import org.jboss.as.server.deployment.SetupAction;
 import org.jboss.as.server.deployment.reflect.DeploymentClassIndex;
 import org.jboss.as.undertow.extension.HttpListenerService;
 import org.jboss.as.undertow.extension.WebSubsystemServices;
 import org.jboss.as.web.deployment.WarMetaData;
 import org.jboss.as.web.deployment.WebAttachments;
 import org.jboss.as.web.deployment.component.ComponentInstantiator;
 import org.jboss.dmr.ModelNode;
 import org.jboss.metadata.ear.jboss.JBossAppMetaData;
 import org.jboss.metadata.ear.spec.EarMetaData;
 import org.jboss.metadata.javaee.spec.DescriptionGroupMetaData;
 import org.jboss.metadata.javaee.spec.ParamValueMetaData;
 import org.jboss.metadata.web.jboss.JBossServletMetaData;
 import org.jboss.metadata.web.jboss.JBossWebMetaData;
 import org.jboss.metadata.web.spec.AttributeMetaData;
 import org.jboss.metadata.web.spec.DispatcherType;
 import org.jboss.metadata.web.spec.ErrorPageMetaData;
 import org.jboss.metadata.web.spec.FilterMappingMetaData;
 import org.jboss.metadata.web.spec.FilterMetaData;
 import org.jboss.metadata.web.spec.FunctionMetaData;
 import org.jboss.metadata.web.spec.JspConfigMetaData;
 import org.jboss.metadata.web.spec.JspPropertyGroupMetaData;
 import org.jboss.metadata.web.spec.ListenerMetaData;
 import org.jboss.metadata.web.spec.MimeMappingMetaData;
 import org.jboss.metadata.web.spec.ServletMappingMetaData;
 import org.jboss.metadata.web.spec.TagFileMetaData;
 import org.jboss.metadata.web.spec.TagMetaData;
 import org.jboss.metadata.web.spec.TldMetaData;
 import org.jboss.metadata.web.spec.VariableMetaData;
 import org.jboss.modules.Module;
 import org.jboss.msc.service.ServiceBuilder;
 import org.jboss.msc.service.ServiceController.Mode;
 import org.jboss.msc.service.ServiceName;
 import org.jboss.msc.service.ServiceRegistryException;
 import org.jboss.msc.service.ServiceTarget;
 import org.jboss.security.SecurityConstants;
 import org.jboss.security.SecurityUtil;
 import org.jboss.vfs.VirtualFile;
 
 import static org.jboss.as.web.WebMessages.MESSAGES;
 
 public class WarDeploymentProcessor implements DeploymentUnitProcessor {
 
     private final String defaultHost;
 
     public WarDeploymentProcessor(final String defaultHost) {
         this.defaultHost = defaultHost;
     }
 
     @Override
     public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
         final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
         final WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
         if (warMetaData == null) {
             return;
         }
         String hostName = hostNameOfDeployment(warMetaData, defaultHost);
         processDeployment(hostName, warMetaData, deploymentUnit, phaseContext.getServiceTarget());
     }
 
     static String hostNameOfDeployment(final WarMetaData metaData, final String defaultHost) {
         Collection<String> hostNames = null;
         if (metaData.getMergedJBossWebMetaData() != null) {
             hostNames = metaData.getMergedJBossWebMetaData().getVirtualHosts();
         }
         if (hostNames == null || hostNames.isEmpty()) {
             hostNames = Collections.singleton(defaultHost);
         }
         String hostName = hostNames.iterator().next();
         if (hostName == null) {
             throw MESSAGES.nullHostName();
         }
         return hostName;
     }
 
     @Override
     public void undeploy(final DeploymentUnit context) {
         //AbstractSecurityDeployer<?> deployer = new WarSecurityDeployer();
         //deployer.undeploy(context);
     }
 
     private void processDeployment(final String hostName, final WarMetaData warMetaData, final DeploymentUnit deploymentUnit, final ServiceTarget serviceTarget)
             throws DeploymentUnitProcessingException {
         final VirtualFile deploymentRoot = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot();
         final Module module = deploymentUnit.getAttachment(Attachments.MODULE);
         if (module == null) {
             throw new DeploymentUnitProcessingException(MESSAGES.failedToResolveModule(deploymentRoot));
         }
         final DeploymentClassIndex deploymentClassIndex = deploymentUnit.getAttachment(Attachments.CLASS_INDEX);
         final JBossWebMetaData metaData = warMetaData.getMergedJBossWebMetaData();
         final List<SetupAction> setupActions = deploymentUnit.getAttachmentList(org.jboss.as.ee.component.Attachments.WEB_SETUP_ACTIONS);
         Map<String, ComponentInstantiator> components = deploymentUnit.getAttachment(WebAttachments.WEB_COMPONENT_INSTANTIATORS);
 
         ScisMetaData scisMetaData = deploymentUnit.getAttachment(ScisMetaData.ATTACHMENT_KEY);
 
         // see AS7-2077
         // basically we want to ignore components that have failed for whatever reason
         // if they are important they will be picked up when the web deployment actually starts
         if (components != null) {
             final Set<ServiceName> failed = deploymentUnit.getAttachment(org.jboss.as.ee.component.Attachments.FAILED_COMPONENTS);
             Iterator<Map.Entry<String, ComponentInstantiator>> it = components.entrySet().iterator();
             while (it.hasNext()) {
                 Map.Entry<String, ComponentInstantiator> entry = it.next();
                 boolean skip = false;
                 for (final ServiceName serviceName : entry.getValue().getServiceNames()) {
                     if (failed.contains(serviceName)) {
                         skip = true;
                         break;
                     }
                 }
                 if (skip) {
                     it.remove();
                 }
             }
         } else {
             components = new HashMap<String, ComponentInstantiator>();
         }
 
         DeploymentInfo deploymentInfo = createServletConfig(metaData, deploymentUnit, module, deploymentClassIndex, components, scisMetaData, deploymentRoot);
 
         final String pathName = pathNameOfDeployment(deploymentUnit, metaData);
         deploymentInfo.setContextPath(pathName);
 
         String metaDataSecurityDomain = metaData.getSecurityDomain();
         if (metaDataSecurityDomain == null) {
             metaDataSecurityDomain = getJBossAppSecurityDomain(deploymentUnit);
         }
         if (metaDataSecurityDomain != null) {
             metaDataSecurityDomain = metaDataSecurityDomain.trim();
         }
 
         String securityDomain = metaDataSecurityDomain == null ? SecurityConstants.DEFAULT_APPLICATION_POLICY : SecurityUtil
                 .unprefixSecurityDomain(metaDataSecurityDomain);
 
         // Setup an deployer configured ServletContext attributes
         final List<ServletContextAttribute> attributes = deploymentUnit.getAttachment(ServletContextAttribute.ATTACHMENT_KEY);
 
         try {
             final ServiceName deploymentServiceName = ServiceName.JBOSS.append("undertow", deploymentInfo.getContextPath());
             UndertowDeploymentService service = new UndertowDeploymentService(deploymentInfo);
             final ServiceBuilder<UndertowDeploymentService> builder = serviceTarget.addService(deploymentServiceName, service)
                     .addDependency(WebSubsystemServices.LISTENER.append(defaultHost), HttpListenerService.class, service.getConnector());
 
             deploymentUnit.addToAttachmentList(Attachments.DEPLOYMENT_COMPLETE_SERVICES, deploymentServiceName);
 
             for (Map.Entry<String, ComponentInstantiator> entry : components.entrySet()) {
                 builder.addDependencies(entry.getValue().getServiceNames());
             }
             // add any dependencies required by the setup action
             for (final SetupAction action : setupActions) {
                 builder.addDependencies(action.dependencies());
                 deploymentInfo.addThreadSetupAction(new ThreadSetupAction() {
 
                     @Override
                     public Handle setup(final BlockingHttpServerExchange exchange) {
                         action.setup(Collections.<String, Object>emptyMap());
                         return new Handle() {
                             @Override
                             public void tearDown() {
                                 action.teardown(Collections.<String, Object>emptyMap());
                             }
                         };
                     }
                 });
             }
             /*
             if (metaData.getDistributable() != null) {
                 DistributedCacheManagerFactoryService factoryService = new DistributedCacheManagerFactoryService();
                 DistributedCacheManagerFactory factory = factoryService.getValue();
                 if (factory != null) {
                     ServiceName factoryServiceName = webappServiceName.append("session");
                     webappBuilder.addDependency(DependencyType.OPTIONAL, factoryServiceName, DistributedCacheManagerFactory.class, config.getDistributedCacheManagerFactoryInjector());
 
                     ServiceBuilder<DistributedCacheManagerFactory> factoryBuilder = serviceTarget.addService(factoryServiceName, factoryService);
                     boolean enabled = factory.addDeploymentDependencies(webappServiceName, deploymentUnit.getServiceRegistry(), serviceTarget, factoryBuilder, metaData);
                     factoryBuilder.setInitialMode(enabled ? Mode.ON_DEMAND : Mode.NEVER).install();
                 }
             }*/
 
             // OSGi web applications are activated in {@link WebContextActivationProcessor} according to bundle lifecycle changes
             //if (deploymentUnit.hasAttachment(Attachments.OSGI_MANIFEST)) {
             //    webappBuilder.setInitialMode(Mode.NEVER);
             //    ContextActivator activator = new ContextActivator(webappBuilder.install());
             //    deploymentUnit.putAttachment(ContextActivator.ATTACHMENT_KEY, activator);
             // } else {
             builder.setInitialMode(Mode.ACTIVE);
             builder.install();
             // }
 
         } catch (ServiceRegistryException e) {
             throw new DeploymentUnitProcessingException(MESSAGES.failedToAddWebDeployment(), e);
         }
 
         // Process the web related mgmt information
         //final ModelNode node = deploymentUnit.getDeploymentSubsystemModel("web");
         //node.get(WebDeploymentDefinition.CONTEXT_ROOT.getName()).set("".equals(pathName) ? "/" : pathName);
         //node.get(WebDeploymentDefinition.VIRTUAL_HOST.getName()).set(hostName);
         //processManagement(deploymentUnit, metaData);
     }
 
     static String pathNameOfDeployment(final DeploymentUnit deploymentUnit, final JBossWebMetaData metaData) {
         String pathName;
         if (metaData.getContextRoot() == null) {
             final EEModuleDescription description = deploymentUnit.getAttachment(org.jboss.as.ee.component.Attachments.EE_MODULE_DESCRIPTION);
             if (description != null) {
                 // if there is a EEModuleDescription we need to take into account that the module name may have been overridden
                 pathName = "/" + description.getModuleName();
             } else {
                 pathName = "/" + deploymentUnit.getName().substring(0, deploymentUnit.getName().length() - 4);
             }
         } else {
             pathName = metaData.getContextRoot();
             if ("/".equals(pathName)) {
                 pathName = "";
             } else if (pathName.length() > 0 && pathName.charAt(0) != '/') {
                 pathName = "/" + pathName;
             }
         }
         return pathName;
     }
 
     void processManagement(final DeploymentUnit unit, JBossWebMetaData metaData) {
         for (final JBossServletMetaData servlet : metaData.getServlets()) {
             try {
                 final String name = servlet.getName();
                 final ModelNode node = unit.createDeploymentSubModel("web", PathElement.pathElement("servlet", name));
                 node.get("servlet-class").set(servlet.getServletClass());
                 node.get("servlet-name").set(servlet.getServletName());
             } catch (Exception e) {
                 // Should a failure in creating the mgmt view also make to the deployment to fail?
                 continue;
             }
         }
 
     }
 
     private DeploymentInfo createServletConfig(final JBossWebMetaData mergedMetaData, final DeploymentUnit deploymentUnit, final Module module, final DeploymentClassIndex classReflectionIndex, final Map<String, ComponentInstantiator> components, final ScisMetaData scisMetaData, final VirtualFile deploymentRoot) throws DeploymentUnitProcessingException {
         try {
             final DeploymentInfo d = new DeploymentInfo();
             d.setContextPath(mergedMetaData.getContextRoot());
             if (mergedMetaData.getDescriptionGroup() != null) {
                 d.setDisplayName(mergedMetaData.getDescriptionGroup().getDisplayName());
             }
             d.setDeploymentName(deploymentUnit.getName());
             d.setResourceLoader(new DeploymentResourceLoader(deploymentRoot));
             d.setClassLoader(module.getClassLoader());
             d.setMajorVersion(Integer.parseInt(mergedMetaData.getServletVersion().charAt(0) + ""));
             d.setMinorVersion(Integer.parseInt(mergedMetaData.getServletVersion().charAt(2) + ""));
 
             HashMap<String, TagLibraryInfo> tldInfo = createTldsInfo(deploymentUnit, classReflectionIndex, components, d);
             HashMap<String, JspPropertyGroup> propertyGroups = createJspConfig(mergedMetaData);
 
             JspServletBuilder.setupDeployment(d, propertyGroups, tldInfo, new HackInstanceManager());
 
 
             //default JSP servlet
             final ServletInfo jspServlet = new ServletInfo("Default JSP Servlet", JspServlet.class)
                     .addMapping("*.jsp")
                     .addMapping("*.jspx");
             d.addServlet(jspServlet);
 
             for (final String mapping : propertyGroups.keySet()) {
                 jspServlet.addMapping(mapping);
             }
 
             //TODO: do this properly
             d.setClassIntrospecter(new ClassIntrospecter() {
                 @Override
                 public <T> InstanceFactory<T> createInstanceFactory(final Class<T> clazz) {
                     try {
                         return new ConstructorInstanceFactory<T>(clazz.getDeclaredConstructor());
                     } catch (NoSuchMethodException e) {
                         throw new RuntimeException(e);
                     }
                 }
             });
 
             final Map<String, List<ServletMappingMetaData>> servletMappings = new HashMap<String, List<ServletMappingMetaData>>();
 
             if (mergedMetaData.getServletMappings() != null) {
                 for (final ServletMappingMetaData mapping : mergedMetaData.getServletMappings()) {
                     List<ServletMappingMetaData> list = servletMappings.get(mapping.getServletName());
                     if (list == null) {
                         servletMappings.put(mapping.getServletName(), list = new ArrayList<ServletMappingMetaData>());
                     }
                     list.add(mapping);
                 }
             }
             if (mergedMetaData.getServlets() != null) {
                 for (final JBossServletMetaData servlet : mergedMetaData.getServlets()) {
                     final ServletInfo s;
                     final ComponentInstantiator creator = components.get(servlet.getServletClass());
 
                     if (servlet.getJspFile() != null) {
                         //TODO: real JSP support
                         s = new ServletInfo(servlet.getName(), JspServlet.class);
                     } else if (creator != null) {
                         //TODO: fix this once we have web-common
                         InstanceFactory<Servlet> factory = createInstanceFactory(creator);
                         s = new ServletInfo(servlet.getName(), (Class<? extends Servlet>) classReflectionIndex.classIndex(servlet.getServletClass()).getModuleClass(), factory);
                     } else {
                         s = new ServletInfo(servlet.getName(), (Class<? extends Servlet>) classReflectionIndex.classIndex(servlet.getServletClass()).getModuleClass());
                     }
                     s.setAsyncSupported(servlet.isAsyncSupported())
                             .setJspFile(servlet.getJspFile())
                             .setEnabled(servlet.isEnabled());
                     if (servlet.getRunAs() != null) {
                         s.setRunAs(servlet.getRunAs().getRoleName());
                     }
 
                     List<ServletMappingMetaData> mappings = servletMappings.get(servlet.getName());
                     if (mappings != null) {
                         for (ServletMappingMetaData mapping : mappings) {
                             s.addMappings(mapping.getUrlPatterns());
                         }
                     }
                     if (servlet.getInitParam() != null) {
                         for (ParamValueMetaData initParam : servlet.getInitParam()) {
                            if (!s.getInitParams().containsKey(initParam.getParamName())) {
                                s.addInitParam(initParam.getParamName(), initParam.getParamValue());
                            }
                         }
                     }
                     d.addServlet(s);
                 }
             }
 
             if (mergedMetaData.getFilters() != null) {
                 for (final FilterMetaData filter : mergedMetaData.getFilters()) {
                     ComponentInstantiator creator = components.get(filter.getFilterClass());
                     FilterInfo f;
                     if (creator != null) {
                         InstanceFactory<Filter> instanceFactory = createInstanceFactory(creator);
                         f = new FilterInfo(filter.getName(), (Class<? extends Filter>) classReflectionIndex.classIndex(filter.getFilterClass()).getModuleClass(), instanceFactory);
                     } else {
                         f = new FilterInfo(filter.getName(), (Class<? extends Filter>) classReflectionIndex.classIndex(filter.getFilterClass()).getModuleClass());
                     }
                     f.setAsyncSupported(filter.isAsyncSupported());
                     d.addFilter(f);
 
                     if (filter.getInitParam() != null) {
                         for (ParamValueMetaData initParam : filter.getInitParam()) {
                             f.addInitParam(initParam.getParamName(), initParam.getParamValue());
                         }
                     }
                 }
             }
             if (mergedMetaData.getFilterMappings() != null) {
                 for (final FilterMappingMetaData mapping : mergedMetaData.getFilterMappings()) {
                     if (mapping.getUrlPatterns() != null) {
                         for (final String url : mapping.getUrlPatterns()) {
                             if (mapping.getDispatchers() != null && !mapping.getDispatchers().isEmpty()) {
                                 for (DispatcherType dispatcher : mapping.getDispatchers()) {
 
                                     d.addFilterUrlMapping(mapping.getFilterName(), url, javax.servlet.DispatcherType.valueOf(dispatcher.name()));
                                 }
                             } else {
                                 d.addFilterUrlMapping(mapping.getFilterName(), url, javax.servlet.DispatcherType.REQUEST);
                             }
                         }
                     }
                     if (mapping.getServletNames() != null) {
                         for (String servletName : mapping.getServletNames()) {
                             if (mapping.getDispatchers() != null && !mapping.getDispatchers().isEmpty()) {
                                 for (DispatcherType dispatcher : mapping.getDispatchers()) {
                                     d.addFilterServletNameMapping(mapping.getFilterName(), servletName, javax.servlet.DispatcherType.valueOf(dispatcher.name()));
                                 }
                             } else {
                                 d.addFilterServletNameMapping(mapping.getFilterName(), servletName, javax.servlet.DispatcherType.REQUEST);
                             }
                         }
                     }
                 }
             }
 
             for (final Map.Entry<ServletContainerInitializer, Set<Class<?>>> sci : scisMetaData.getHandlesTypes().entrySet()) {
                 final ImmediateInstanceFactory<ServletContainerInitializer> instanceFactory = new ImmediateInstanceFactory<ServletContainerInitializer>(sci.getKey());
                 d.addServletContainerInitalizer(new ServletContainerInitializerInfo(sci.getKey().getClass(), instanceFactory, sci.getValue()));
             }
 
             if (mergedMetaData.getListeners() != null) {
                 for (ListenerMetaData listener : mergedMetaData.getListeners()) {
                     addListener(classReflectionIndex, components, d, listener);
                 }
 
             }
             if (mergedMetaData.getContextParams() != null) {
                 for (ParamValueMetaData param : mergedMetaData.getContextParams()) {
                     d.addInitParameter(param.getParamName(), param.getParamValue());
                 }
             }
 
             if (mergedMetaData.getWelcomeFileList() != null &&
                     mergedMetaData.getWelcomeFileList().getWelcomeFiles() != null) {
                 d.addWelcomePages(mergedMetaData.getWelcomeFileList().getWelcomeFiles());
             } else {
                 d.addWelcomePages("index.html", "index.htm", "index.jsp");
             }
 
             if (mergedMetaData.getErrorPages() != null) {
                 for (final ErrorPageMetaData page : mergedMetaData.getErrorPages()) {
                     final ErrorPage errorPage;
                     if (page.getExceptionType() == null || page.getExceptionType().isEmpty()) {
                         errorPage = new ErrorPage(page.getLocation(), Integer.parseInt(page.getErrorCode()));
                     } else {
                         errorPage = new ErrorPage(page.getLocation(), (Class<? extends Throwable>) classReflectionIndex.classIndex(page.getExceptionType()).getModuleClass());
                     }
                     d.addErrorPages(errorPage);
                 }
             }
 
             if (mergedMetaData.getMimeMappings() != null) {
                 for (final MimeMappingMetaData mapping : mergedMetaData.getMimeMappings()) {
                     d.addMimeMapping(new MimeMapping(mapping.getExtension(), mapping.getMimeType()));
                 }
             }
 
 
             return d;
         } catch (ClassNotFoundException e) {
             throw new DeploymentUnitProcessingException(e);
         }
     }
 
     private HashMap<String, JspPropertyGroup> createJspConfig(JBossWebMetaData metaData) {
         final HashMap<String, JspPropertyGroup> ret = new HashMap<String, JspPropertyGroup>();
         // JSP Config
         JspConfigMetaData config = metaData.getJspConfig();
         if (config != null) {
             // JSP Property groups
             List<JspPropertyGroupMetaData> groups = config.getPropertyGroups();
             if (groups != null) {
                 for (JspPropertyGroupMetaData group : groups) {
                     org.apache.jasper.deploy.JspPropertyGroup jspPropertyGroup = new org.apache.jasper.deploy.JspPropertyGroup();
                     for (String pattern : group.getUrlPatterns()) {
                         jspPropertyGroup.addUrlPattern(pattern);
                     }
                     jspPropertyGroup.setElIgnored(group.getElIgnored());
                     jspPropertyGroup.setPageEncoding(group.getPageEncoding());
                     jspPropertyGroup.setScriptingInvalid(group.getScriptingInvalid());
                     jspPropertyGroup.setIsXml(group.getIsXml());
                     if (group.getIncludePreludes() != null) {
                         for (String includePrelude : group.getIncludePreludes()) {
                             jspPropertyGroup.addIncludePrelude(includePrelude);
                         }
                     }
                     if (group.getIncludeCodas() != null) {
                         for (String includeCoda : group.getIncludeCodas()) {
                             jspPropertyGroup.addIncludeCoda(includeCoda);
                         }
                     }
                     jspPropertyGroup.setDeferredSyntaxAllowedAsLiteral(group.getDeferredSyntaxAllowedAsLiteral());
                     jspPropertyGroup.setTrimDirectiveWhitespaces(group.getTrimDirectiveWhitespaces());
                     jspPropertyGroup.setDefaultContentType(group.getDefaultContentType());
                     jspPropertyGroup.setBuffer(group.getBuffer());
                     jspPropertyGroup.setErrorOnUndeclaredNamespace(group.getErrorOnUndeclaredNamespace());
                     for (String pattern : jspPropertyGroup.getUrlPatterns()) {
                         // Split off the groups to individual mappings
                         ret.put(pattern, jspPropertyGroup);
                     }
                 }
             }
         }
         return ret;
     }
 
     private HashMap<String, TagLibraryInfo> createTldsInfo(final DeploymentUnit deploymentUnit, final DeploymentClassIndex classReflectionIndex, final Map<String, ComponentInstantiator> components, final DeploymentInfo d) throws ClassNotFoundException {
 
         TldsMetaData tldsMetaData = deploymentUnit.getAttachment(TldsMetaData.ATTACHMENT_KEY);
         final HashMap<String, TagLibraryInfo> ret = new HashMap<String, TagLibraryInfo>();
         if (tldsMetaData != null) {
             if (tldsMetaData.getTlds() != null) {
                 for (Map.Entry<String, TldMetaData> tld : tldsMetaData.getTlds().entrySet()) {
                     createTldInfo(tld.getKey(), tld.getValue(), ret, classReflectionIndex, components, d);
                 }
             }
             List<TldMetaData> sharedTlds = tldsMetaData.getSharedTlds(deploymentUnit);
             if (sharedTlds != null) {
                 for (TldMetaData metaData : sharedTlds) {
 
                     createTldInfo(null, metaData, ret, classReflectionIndex, components, d);
                 }
             }
         }
 
         return ret;
     }
 
     private TagLibraryInfo createTldInfo(final String location, final TldMetaData tldMetaData, final HashMap<String, TagLibraryInfo> ret, final DeploymentClassIndex classReflectionIndex, final Map<String, ComponentInstantiator> components, final DeploymentInfo d) throws ClassNotFoundException {
         String relativeLocation = location;
         String jarPath = null;
         if (relativeLocation != null && relativeLocation.startsWith("/WEB-INF/lib/")) {
             int pos = relativeLocation.indexOf('/', "/WEB-INF/lib/".length());
             if (pos > 0) {
                 jarPath = relativeLocation.substring(pos);
                 if (jarPath.startsWith("/")) {
                     jarPath = jarPath.substring(1);
                 }
                 relativeLocation = relativeLocation.substring(0, pos);
             }
         }
 
         TagLibraryInfo tagLibraryInfo = new TagLibraryInfo();
         tagLibraryInfo.setTlibversion(tldMetaData.getTlibVersion());
         if (tldMetaData.getJspVersion() == null)
             tagLibraryInfo.setJspversion(tldMetaData.getVersion());
         else
             tagLibraryInfo.setJspversion(tldMetaData.getJspVersion());
         tagLibraryInfo.setShortname(tldMetaData.getShortName());
         tagLibraryInfo.setUri(tldMetaData.getUri());
         if (tldMetaData.getDescriptionGroup() != null) {
             tagLibraryInfo.setInfo(tldMetaData.getDescriptionGroup().getDescription());
         }
         // Listener
         if (tldMetaData.getListeners() != null) {
             for (ListenerMetaData listener : tldMetaData.getListeners()) {
                 tagLibraryInfo.addListener(listener.getListenerClass());
                 addListener(classReflectionIndex, components, d, listener);
             }
         }
         // Validator
         if (tldMetaData.getValidator() != null) {
             TagLibraryValidatorInfo tagLibraryValidatorInfo = new TagLibraryValidatorInfo();
             tagLibraryValidatorInfo.setValidatorClass(tldMetaData.getValidator().getValidatorClass());
             if (tldMetaData.getValidator().getInitParams() != null) {
                 for (ParamValueMetaData paramValueMetaData : tldMetaData.getValidator().getInitParams()) {
                     tagLibraryValidatorInfo.addInitParam(paramValueMetaData.getParamName(), paramValueMetaData.getParamValue());
                 }
             }
             tagLibraryInfo.setValidator(tagLibraryValidatorInfo);
         }
         // Tag
         if (tldMetaData.getTags() != null) {
             for (TagMetaData tagMetaData : tldMetaData.getTags()) {
                 TagInfo tagInfo = new TagInfo();
                 tagInfo.setTagName(tagMetaData.getName());
                 tagInfo.setTagClassName(tagMetaData.getTagClass());
                 tagInfo.setTagExtraInfo(tagMetaData.getTeiClass());
                 if (tagMetaData.getBodyContent() != null)
                     tagInfo.setBodyContent(tagMetaData.getBodyContent().toString());
                 tagInfo.setDynamicAttributes(tagMetaData.getDynamicAttributes());
                 // Description group
                 if (tagMetaData.getDescriptionGroup() != null) {
                     DescriptionGroupMetaData descriptionGroup = tagMetaData.getDescriptionGroup();
                     if (descriptionGroup.getIcons() != null && descriptionGroup.getIcons().value() != null
                             && (descriptionGroup.getIcons().value().length > 0)) {
                         Icon icon = descriptionGroup.getIcons().value()[0];
                         tagInfo.setLargeIcon(icon.largeIcon());
                         tagInfo.setSmallIcon(icon.smallIcon());
                     }
                     tagInfo.setInfoString(descriptionGroup.getDescription());
                     tagInfo.setDisplayName(descriptionGroup.getDisplayName());
                 }
                 // Variable
                 if (tagMetaData.getVariables() != null) {
                     for (VariableMetaData variableMetaData : tagMetaData.getVariables()) {
                         TagVariableInfo tagVariableInfo = new TagVariableInfo();
                         tagVariableInfo.setNameGiven(variableMetaData.getNameGiven());
                         tagVariableInfo.setNameFromAttribute(variableMetaData.getNameFromAttribute());
                         tagVariableInfo.setClassName(variableMetaData.getVariableClass());
                         tagVariableInfo.setDeclare(variableMetaData.getDeclare());
                         if (variableMetaData.getScope() != null)
                             tagVariableInfo.setScope(variableMetaData.getScope().toString());
                         tagInfo.addTagVariableInfo(tagVariableInfo);
                     }
                 }
                 // Attribute
                 if (tagMetaData.getAttributes() != null) {
                     for (AttributeMetaData attributeMetaData : tagMetaData.getAttributes()) {
                         TagAttributeInfo tagAttributeInfo = new TagAttributeInfo();
                         tagAttributeInfo.setName(attributeMetaData.getName());
                         tagAttributeInfo.setType(attributeMetaData.getType());
                         tagAttributeInfo.setReqTime(attributeMetaData.getRtexprvalue());
                         tagAttributeInfo.setRequired(attributeMetaData.getRequired());
                         tagAttributeInfo.setFragment(attributeMetaData.getFragment());
                         if (attributeMetaData.getDeferredValue() != null) {
                             tagAttributeInfo.setDeferredValue("true");
                             tagAttributeInfo.setExpectedTypeName(attributeMetaData.getDeferredValue().getType());
                         } else {
                             tagAttributeInfo.setDeferredValue("false");
                         }
                         if (attributeMetaData.getDeferredMethod() != null) {
                             tagAttributeInfo.setDeferredMethod("true");
                             tagAttributeInfo.setMethodSignature(attributeMetaData.getDeferredMethod().getMethodSignature());
                         } else {
                             tagAttributeInfo.setDeferredMethod("false");
                         }
                         tagInfo.addTagAttributeInfo(tagAttributeInfo);
                     }
                 }
                 tagLibraryInfo.addTagInfo(tagInfo);
             }
         }
         // Tag files
         if (tldMetaData.getTagFiles() != null) {
             for (TagFileMetaData tagFileMetaData : tldMetaData.getTagFiles()) {
                 TagFileInfo tagFileInfo = new TagFileInfo();
                 tagFileInfo.setName(tagFileMetaData.getName());
                 tagFileInfo.setPath(tagFileMetaData.getPath());
                 tagLibraryInfo.addTagFileInfo(tagFileInfo);
             }
         }
         // Function
         if (tldMetaData.getFunctions() != null) {
             for (FunctionMetaData functionMetaData : tldMetaData.getFunctions()) {
                 FunctionInfo functionInfo = new FunctionInfo();
                 functionInfo.setName(functionMetaData.getName());
                 functionInfo.setFunctionClass(functionMetaData.getFunctionClass());
                 functionInfo.setFunctionSignature(functionMetaData.getFunctionSignature());
                 tagLibraryInfo.addFunctionInfo(functionInfo);
             }
         }
 
         if (jarPath == null && relativeLocation == null) {
             ret.put(tagLibraryInfo.getUri(), tagLibraryInfo);
         } else if (jarPath == null) {
             tagLibraryInfo.setLocation("");
             tagLibraryInfo.setPath(relativeLocation);
             ret.put(tagLibraryInfo.getUri(), tagLibraryInfo);
             ret.put(relativeLocation, tagLibraryInfo);
         } else {
             tagLibraryInfo.setLocation(relativeLocation);
             tagLibraryInfo.setPath(jarPath);
             ret.put(tagLibraryInfo.getUri(), tagLibraryInfo);
             if (jarPath.equals("META-INF/taglib.tld")) {
                 ret.put(relativeLocation, tagLibraryInfo);
             }
         }
         return tagLibraryInfo;
     }
 
     private void addListener(final DeploymentClassIndex classReflectionIndex, final Map<String, ComponentInstantiator> components, final DeploymentInfo d, final ListenerMetaData listener) throws ClassNotFoundException {
         ComponentInstantiator creator = components.get(listener.getListenerClass());
         ListenerInfo l;
         if (creator != null) {
             InstanceFactory<EventListener> factory = createInstanceFactory(creator);
             l = new ListenerInfo((Class<? extends EventListener>) classReflectionIndex.classIndex(listener.getListenerClass()).getModuleClass(), factory);
         } else {
             l = new ListenerInfo((Class<? extends EventListener>) classReflectionIndex.classIndex(listener.getListenerClass()).getModuleClass());
         }
         d.addListener(l);
     }
 
     private <T> InstanceFactory<T> createInstanceFactory(final ComponentInstantiator creator) {
         return new InstanceFactory<T>() {
             @Override
             public InstanceHandle<T> createInstance() throws InstantiationException {
                 final ManagedReference instance = creator.getReference();
                 return new InstanceHandle<T>() {
                     @Override
                     public T getInstance() {
                         return (T) instance.getInstance();
                     }
 
                     @Override
                     public void release() {
                         instance.release();
                     }
                 };
             }
         };
     }
 
     /**
      * Try to obtain the security domain configured in jboss-app.xml at the ear level if available
      */
     private String getJBossAppSecurityDomain(final DeploymentUnit deploymentUnit) {
         String securityDomain = null;
         DeploymentUnit parent = deploymentUnit.getParent();
         if (parent != null) {
             final EarMetaData jbossAppMetaData = parent.getAttachment(org.jboss.as.ee.structure.Attachments.EAR_METADATA);
             if (jbossAppMetaData instanceof JBossAppMetaData) {
                 securityDomain = ((JBossAppMetaData) jbossAppMetaData).getSecurityDomain();
             }
         }
         return securityDomain;
     }
 }
