 package org.springframework.security.config;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Test;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.mock.web.MockHttpSession;
 import org.springframework.security.ConfigAttributeDefinition;
 import org.springframework.security.MockAuthenticationEntryPoint;
 import org.springframework.security.MockFilterChain;
 import org.springframework.security.SecurityConfig;
 import org.springframework.security.concurrent.ConcurrentLoginException;
 import org.springframework.security.concurrent.ConcurrentSessionControllerImpl;
 import org.springframework.security.concurrent.ConcurrentSessionFilter;
 import org.springframework.security.context.HttpSessionContextIntegrationFilter;
 import org.springframework.security.intercept.web.FilterInvocation;
 import org.springframework.security.intercept.web.FilterInvocationDefinitionSource;
 import org.springframework.security.intercept.web.FilterSecurityInterceptor;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 import org.springframework.security.providers.anonymous.AnonymousProcessingFilter;
 import org.springframework.security.securechannel.ChannelProcessingFilter;
 import org.springframework.security.ui.ExceptionTranslationFilter;
 import org.springframework.security.ui.SessionFixationProtectionFilter;
 import org.springframework.security.ui.WebAuthenticationDetails;
 import org.springframework.security.ui.basicauth.BasicProcessingFilter;
 import org.springframework.security.ui.logout.LogoutFilter;
 import org.springframework.security.ui.preauth.x509.X509PreAuthenticatedProcessingFilter;
 import org.springframework.security.ui.rememberme.PersistentTokenBasedRememberMeServices;
 import org.springframework.security.ui.rememberme.RememberMeProcessingFilter;
 import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
 import org.springframework.security.ui.webapp.DefaultLoginPageGeneratingFilter;
 import org.springframework.security.util.FieldUtils;
 import org.springframework.security.util.FilterChainProxy;
 import org.springframework.security.util.InMemoryXmlApplicationContext;
 import org.springframework.security.util.PortMapperImpl;
 import org.springframework.security.wrapper.SecurityContextHolderAwareRequestFilter;
 import org.springframework.util.ReflectionUtils;
 
 /**
  * @author Luke Taylor
  * @version $Id$
  */
 public class HttpSecurityBeanDefinitionParserTests {
     private AbstractXmlApplicationContext appContext;
     static final String AUTH_PROVIDER_XML =
             "    <authentication-provider>" +
             "        <user-service>" +
             "            <user name='bob' password='bobspassword' authorities='ROLE_A,ROLE_B' />" +
             "            <user name='bill' password='billspassword' authorities='ROLE_A,ROLE_B,AUTH_OTHER' />" +
             "        </user-service>" +
             "    </authentication-provider>";
 
     @After
     public void closeAppContext() {
         if (appContext != null) {
             appContext.close();
             appContext = null;
         }
     }
 
     @Test
     public void httpAutoConfigSetsUpCorrectFilterList() throws Exception {
         setContext("<http auto-config='true' />" + AUTH_PROVIDER_XML);
 
         List filterList = getFilters("/anyurl");
 
         checkAutoConfigFilters(filterList);
     }
 
    private void checkAutoConfigFilters(List filterList) {
         assertEquals("Expected 11 filters in chain", 11, filterList.size());
 
         Iterator filters = filterList.iterator();
 
         assertTrue(filters.next() instanceof HttpSessionContextIntegrationFilter);
         assertTrue(filters.next() instanceof SessionFixationProtectionFilter);        
         assertTrue(filters.next() instanceof LogoutFilter);
        assertTrue(filters.next() instanceof AuthenticationProcessingFilter);
         assertTrue(filters.next() instanceof DefaultLoginPageGeneratingFilter);
         assertTrue(filters.next() instanceof BasicProcessingFilter);
         assertTrue(filters.next() instanceof SecurityContextHolderAwareRequestFilter);
         assertTrue(filters.next() instanceof RememberMeProcessingFilter);
         assertTrue(filters.next() instanceof AnonymousProcessingFilter);
         assertTrue(filters.next() instanceof ExceptionTranslationFilter);
         assertTrue(filters.next() instanceof FilterSecurityInterceptor);
     }
 
     @Test
     public void filterListShouldBeEmptyForUnprotectedUrl() throws Exception {
         setContext(
                 "    <http auto-config='true'>" +
                 "        <intercept-url pattern='/unprotected' filters='none' />" +
                 "    </http>" + AUTH_PROVIDER_XML);
 
         List filters = getFilters("/unprotected");
 
         assertTrue(filters.size() == 0);
     }
 
     @Test
     public void regexPathsWorkCorrectly() throws Exception {
         setContext(
                 "    <http auto-config='true' path-type='regex'>" +
                 "        <intercept-url pattern='\\A\\/[a-z]+' filters='none' />" +
                 "    </http>" + AUTH_PROVIDER_XML);
         assertEquals(0, getFilters("/imlowercase").size());
         // This will be matched by the default pattern ".*"
         checkAutoConfigFilters(getFilters("/ImCaughtByTheUniversalMatchPattern"));
     }
 
     @Test
     public void lowerCaseComparisonAttributeIsRespectedByFilterChainProxy() throws Exception {
         setContext(
                 "    <http auto-config='true' path-type='ant' lowercase-comparisons='false'>" +
                 "        <intercept-url pattern='/Secure*' filters='none' />" +
                 "    </http>" + AUTH_PROVIDER_XML);
         assertEquals(0, getFilters("/Secure").size());
         // These will be matched by the default pattern "/**"
         checkAutoConfigFilters(getFilters("/secure"));
         checkAutoConfigFilters(getFilters("/ImCaughtByTheUniversalMatchPattern"));
 
     }
 
     @Test
     public void formLoginWithNoLoginPageAddsDefaultLoginPageFilter() throws Exception {
         setContext(
                 "<http auto-config='true' path-type='ant' lowercase-comparisons='false'>" +
                 "   <form-login />" +
                 "</http>" + AUTH_PROVIDER_XML);
         // These will be matched by the default pattern "/**"
         checkAutoConfigFilters(getFilters("/anything"));
     }
 
     @Test
     public void formLoginAlwaysUseDefaultSetsCorrectProperty() throws Exception {
         setContext(
                 "<http>" +
                 "   <form-login default-target-url='/default' always-use-default-target='true' />" +
                 "</http>" + AUTH_PROVIDER_XML);
         // These will be matched by the default pattern "/**"
         AuthenticationProcessingFilter filter = (AuthenticationProcessingFilter) getFilters("/anything").get(2);
         assertEquals("/default", filter.getDefaultTargetUrl());
         assertEquals(Boolean.TRUE, FieldUtils.getFieldValue(filter, "alwaysUseDefaultTargetUrl"));
     }
     
     @Test
     public void lowerCaseComparisonIsRespectedBySecurityFilterInvocationDefinitionSource() throws Exception {
         setContext(
                 "    <http auto-config='true' path-type='ant' lowercase-comparisons='false'>" +
                 "        <intercept-url pattern='/Secure*' access='ROLE_A,ROLE_B' />" +
                 "        <intercept-url pattern='/**' access='ROLE_C' />" +
                 "    </http>" + AUTH_PROVIDER_XML);
 
         FilterSecurityInterceptor fis = (FilterSecurityInterceptor) appContext.getBean(BeanIds.FILTER_SECURITY_INTERCEPTOR);
 
         FilterInvocationDefinitionSource fids = fis.getObjectDefinitionSource();
         ConfigAttributeDefinition attrDef = fids.getAttributes(createFilterinvocation("/Secure", null));
         assertEquals(2, attrDef.getConfigAttributes().size());
         assertTrue(attrDef.contains(new SecurityConfig("ROLE_A")));
         assertTrue(attrDef.contains(new SecurityConfig("ROLE_B")));
         attrDef = fids.getAttributes(createFilterinvocation("/secure", null));
         assertEquals(1, attrDef.getConfigAttributes().size());
         assertTrue(attrDef.contains(new SecurityConfig("ROLE_C")));
     }
 
     @Test
     public void httpMethodMatchIsSupported() throws Exception {
         setContext(
                 "    <http auto-config='true'>" +
                 "        <intercept-url pattern='/**' access='ROLE_C' />" +
                 "        <intercept-url pattern='/secure*' method='DELETE' access='ROLE_SUPERVISOR' />" +
                 "        <intercept-url pattern='/secure*' method='POST' access='ROLE_A,ROLE_B' />" +
                 "    </http>" + AUTH_PROVIDER_XML);
 
         FilterSecurityInterceptor fis = (FilterSecurityInterceptor) appContext.getBean(BeanIds.FILTER_SECURITY_INTERCEPTOR);
         FilterInvocationDefinitionSource fids = fis.getObjectDefinitionSource();
         ConfigAttributeDefinition attrs = fids.getAttributes(createFilterinvocation("/secure", "POST"));
         assertEquals(2, attrs.getConfigAttributes().size());
         assertTrue(attrs.contains(new SecurityConfig("ROLE_A")));
         assertTrue(attrs.contains(new SecurityConfig("ROLE_B")));
     }
 
     @Test
     public void minimalConfigurationParses() {
         setContext("<http><http-basic /></http>" + AUTH_PROVIDER_XML);
     }
 
     @Test
     public void oncePerRequestAttributeIsSupported() throws Exception {
         setContext("<http once-per-request='true'><http-basic /></http>" + AUTH_PROVIDER_XML);
         List filters = getFilters("/someurl");
         
         FilterSecurityInterceptor fsi = (FilterSecurityInterceptor) filters.get(filters.size() - 1);
         
         assertTrue(fsi.isObserveOncePerRequest());
     }
     
     @Test
     public void accessDeniedPageAttributeIsSupported() throws Exception {
         setContext("<http access-denied-page='/access-denied'><http-basic /></http>" + AUTH_PROVIDER_XML);
         List filters = getFilters("/someurl");
         
         ExceptionTranslationFilter etf = (ExceptionTranslationFilter) filters.get(filters.size() - 2);
         
         assertEquals("/access-denied", FieldUtils.getFieldValue(etf, "accessDeniedHandler.errorPage"));
     }    
     
     @Test
     public void interceptUrlWithRequiresChannelAddsChannelFilterToStack() throws Exception {
         setContext(
                 "    <http auto-config='true'>" +
                 "        <intercept-url pattern='/**' requires-channel='https' />" +
                 "    </http>" + AUTH_PROVIDER_XML);
         List filters = getFilters("/someurl");
 
         assertEquals("Expected 12 filters in chain", 12, filters.size());
 
         assertTrue(filters.get(0) instanceof ChannelProcessingFilter);
     }
 
     @Test
     public void portMappingsAreParsedCorrectly() throws Exception {
         setContext(
                 "    <http auto-config='true'>" +
                 "        <port-mappings>" +
                 "            <port-mapping http='9080' https='9443'/>" +
                 "        </port-mappings>" +
                 "    </http>" + AUTH_PROVIDER_XML);
 
         PortMapperImpl pm = (PortMapperImpl) appContext.getBean(BeanIds.PORT_MAPPER);
         assertEquals(1, pm.getTranslatedPortMappings().size());
         assertEquals(Integer.valueOf(9080), pm.lookupHttpPort(9443));
         assertEquals(Integer.valueOf(9443), pm.lookupHttpsPort(9080));
     }
 
     @Test
     public void externalFiltersAreTreatedCorrectly() throws Exception {
         // Decorated user-filter should be added to stack. The other one should be ignored
         setContext(
                 "<http auto-config='true'/>" + AUTH_PROVIDER_XML +
                 "<b:bean id='userFilter' class='org.springframework.security.util.MockFilter'>" +
                 "    <custom-filter after='SESSION_CONTEXT_INTEGRATION_FILTER'/>" +
                 "</b:bean>" +
                 "<b:bean id='userFilter2' class='org.springframework.security.util.MockFilter'>" +
                 "    <custom-filter position='FIRST'/>" +
                 "</b:bean>" +                
                 "<b:bean id='userFilter3' class='org.springframework.security.util.MockFilter'/>");
         List filters = getFilters("/someurl");
 
         assertEquals(13, filters.size());
         assertTrue(filters.get(0) instanceof OrderedFilterBeanDefinitionDecorator.OrderedFilterDecorator);        
         assertTrue(filters.get(2) instanceof OrderedFilterBeanDefinitionDecorator.OrderedFilterDecorator);
         assertEquals("userFilter", ((OrderedFilterBeanDefinitionDecorator.OrderedFilterDecorator)filters.get(2)).getBeanName());
         assertEquals("userFilter2", ((OrderedFilterBeanDefinitionDecorator.OrderedFilterDecorator)filters.get(0)).getBeanName());        
     }
 
     @Test
     public void rememberMeServiceWorksWithTokenRepoRef() {
         setContext(
                 "<http auto-config='true'>" +
                 "    <remember-me key='doesntmatter' token-repository-ref='tokenRepo'/>" +
                 "</http>" +
                 "<b:bean id='tokenRepo' " +
                         "class='org.springframework.security.ui.rememberme.InMemoryTokenRepositoryImpl'/> " + AUTH_PROVIDER_XML);
         Object rememberMeServices = appContext.getBean(BeanIds.REMEMBER_ME_SERVICES);
 
         assertTrue(rememberMeServices instanceof PersistentTokenBasedRememberMeServices);
     }
 
     @Test
     public void rememberMeServiceConfigurationParsesWithCustomUserService() {
         setContext(
                 "<http auto-config='true'>" +
                 "    <remember-me key='doesntmatter' user-service-ref='userService'/>" +
                 "</http>" +
                 "<b:bean id='userService' " +
                         "class='org.springframework.security.userdetails.MockUserDetailsService'/> " + AUTH_PROVIDER_XML);
 //        AbstractRememberMeServices rememberMeServices = (AbstractRememberMeServices) appContext.getBean(BeanIds.REMEMBER_ME_SERVICES);
     }    
     
     @Test
     public void x509SupportAddsFilterAtExpectedPosition() throws Exception {
         setContext(
                 "<http auto-config='true'>" +
                 "    <x509 />" +
                 "</http>"  + AUTH_PROVIDER_XML);
         List filters = getFilters("/someurl");
 
         assertTrue(filters.get(3) instanceof X509PreAuthenticatedProcessingFilter);
     }
 
     @Test
     public void concurrentSessionSupportAddsFilterAndExpectedBeans() throws Exception {
         setContext(
                 "<http auto-config='true'>" +
                 "    <concurrent-session-control session-registry-alias='seshRegistry' expired-url='/expired'/>" +
                 "</http>"  + AUTH_PROVIDER_XML);
         List filters = getFilters("/someurl");
         
         assertTrue(filters.get(0) instanceof ConcurrentSessionFilter);        
         assertNotNull(appContext.getBean("seshRegistry"));
         assertNotNull(appContext.getBean(BeanIds.CONCURRENT_SESSION_CONTROLLER));
     }    
 
     @Test(expected=ConcurrentLoginException.class)
     public void concurrentSessionMaxSessionsIsCorrectlyConfigured() throws Exception {
         setContext(
                 "<http auto-config='true'>" +
                 "    <concurrent-session-control max-sessions='2' exception-if-maximum-exceeded='true' />" +
                 "</http>"  + AUTH_PROVIDER_XML);
         ConcurrentSessionControllerImpl seshController = (ConcurrentSessionControllerImpl) appContext.getBean(BeanIds.CONCURRENT_SESSION_CONTROLLER);
         UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("bob", "pass");
         // Register 2 sessions and then check a third
         MockHttpServletRequest req = new MockHttpServletRequest();
         req.setSession(new MockHttpSession());
         auth.setDetails(new WebAuthenticationDetails(req));
         try {
         	seshController.checkAuthenticationAllowed(auth);
         } catch (ConcurrentLoginException e) {
         	fail("First login should be allowed");
         }        
         seshController.registerSuccessfulAuthentication(auth);
         req.setSession(new MockHttpSession());
         try {
         	seshController.checkAuthenticationAllowed(auth);
         } catch (ConcurrentLoginException e) {
         	fail("Second login should be allowed");
         }
         auth.setDetails(new WebAuthenticationDetails(req));
         seshController.registerSuccessfulAuthentication(auth);
         req.setSession(new MockHttpSession());
         auth.setDetails(new WebAuthenticationDetails(req));
         seshController.checkAuthenticationAllowed(auth);
     }
     
     @Test
     public void customEntryPointIsSupported() throws Exception {
         setContext(
                 "<http auto-config='true' entry-point-ref='entryPoint'/>" +
                 "<b:bean id='entryPoint' class='org.springframework.security.MockAuthenticationEntryPoint'>" +
                 "    <b:constructor-arg value='/customlogin'/>" +
                 "</b:bean>" + AUTH_PROVIDER_XML);
         ExceptionTranslationFilter etf = (ExceptionTranslationFilter) getFilters("/someurl").get(9);
         assertTrue("ExceptionTranslationFilter should be configured with custom entry point", 
                 etf.getAuthenticationEntryPoint() instanceof MockAuthenticationEntryPoint);
     }
     
     @Test
     /** SEC-742 */
     public void rememberMeServicesWorksWithoutBasicProcessingFilter() {
         setContext(
                 "    <http>" +
                 "        <form-login login-page='/login.jsp' default-target-url='/messageList.html'/>" +
                 "        <logout logout-success-url='/login.jsp'/>" +
                 "        <anonymous username='guest' granted-authority='guest'/>" +
                 "        <remember-me />" +
                 "    </http>" + AUTH_PROVIDER_XML);
     }
 
     @Test
     public void disablingSessionProtectionRemovesFilter() throws Exception {
         setContext(
                 "<http auto-config='true' session-fixation-protection='none'/>" + AUTH_PROVIDER_XML);
         List filters = getFilters("/someurl");
 
         assertFalse(filters.get(1) instanceof SessionFixationProtectionFilter);
     }
     
     /**
      * See SEC-750. If the http security post processor causes beans to be instantiated too eagerly, they way miss
      * additional processing. In this method we have a UserDetailsService which is referenced from the namespace
      * and also has a post processor registered which will modify it.
      */
     @Test
     public void httpElementDoesntInterfereWithBeanPostProcessing() {
         setContext(
                 "<http auto-config='true'/>" +
                 "<authentication-provider user-service-ref='myUserService'/>" +
                 "<b:bean id='myUserService' class='org.springframework.security.config.PostProcessedMockUserDetailsService'/>" +
                 "<b:bean id='beanPostProcessor' class='org.springframework.security.config.MockUserServiceBeanPostProcessor'/>"
         );
 
         PostProcessedMockUserDetailsService service = (PostProcessedMockUserDetailsService)appContext.getBean("myUserService");
 
         assertEquals("Hello from the post processor!", service.getPostProcessorWasHere());
     }
     
     private void setContext(String context) {
         appContext = new InMemoryXmlApplicationContext(context);
     }
 
     private List getFilters(String url) throws Exception {
         FilterChainProxy fcp = (FilterChainProxy) appContext.getBean(BeanIds.FILTER_CHAIN_PROXY);
         Method getFilters = fcp.getClass().getDeclaredMethod("getFilters", String.class);
         getFilters.setAccessible(true);
         return (List) ReflectionUtils.invokeMethod(getFilters, fcp, new Object[] {url});
     }
 
     private FilterInvocation createFilterinvocation(String path, String method) {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.setMethod(method);
         request.setRequestURI(null);
 
         request.setServletPath(path);
 
         return new FilterInvocation(request, new MockHttpServletResponse(), new MockFilterChain());
     }
 }
