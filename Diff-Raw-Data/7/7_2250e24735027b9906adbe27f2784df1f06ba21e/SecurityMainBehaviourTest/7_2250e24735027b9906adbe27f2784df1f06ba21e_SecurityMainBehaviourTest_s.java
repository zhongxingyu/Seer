 package net.codjo.security.gui.plugin;
 import javax.swing.JTextField;
 import net.codjo.agent.ContainerConfiguration;
 import net.codjo.agent.test.AgentContainerFixture;
 import net.codjo.gui.toolkit.i18n.InternationalizationTestUtil;
 import net.codjo.i18n.common.Language;
 import net.codjo.i18n.common.TranslationManager;
 import net.codjo.i18n.gui.TranslationNotifier;
 import net.codjo.plugin.common.ApplicationCoreMock;
 import net.codjo.security.client.plugin.SecurityClientPluginConfiguration;
 import net.codjo.security.gui.login.LoginConfig;
 import net.codjo.security.gui.login.LoginConfig.Ldap;
 import net.codjo.security.gui.login.LoginConfig.Server;
 import net.codjo.security.gui.plugin.SecurityGuiConfiguration.LoginCaseType;
 import net.codjo.test.common.LogString;
 import net.codjo.test.common.fixture.SystemExitFixture;
 import org.uispec4j.Trigger;
 import org.uispec4j.UISpecTestCase;
 import org.uispec4j.Window;
 import org.uispec4j.interception.WindowHandler;
 import org.uispec4j.interception.WindowInterceptor;
 /**
  *
  */
 public class SecurityMainBehaviourTest extends UISpecTestCase {
     private LogString log = new LogString();
     private AgentContainerFixture agentContainerFixture = new AgentContainerFixture();
     private ApplicationCoreMock applicationCoreMock = new ApplicationCoreMock(new LogString("Core", log),
                                                                               agentContainerFixture);
     private DefaultSecurityGuiConfiguration securityGuiConfiguration = new DefaultSecurityGuiConfiguration();
     private LoginConfigLoaderMock loginConfigLoaderMock = new LoginConfigLoaderMock();
     private SecurityMainBehaviour securityMainBehaviour;
     private SystemExitFixture systemExitFixture = new SystemExitFixture();
 
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         TranslationManager translationManager = new TranslationManager();
         TranslationNotifier translationNotifier = new TranslationNotifier(Language.FR, translationManager);
 
         registerLanguageBundles(translationManager);
 
        InternationalizationTestUtil.initErrorDialogTranslationBackpack();
         loginConfigLoaderMock.mockLoad(createLoginConfig());
         applicationCoreMock.mockStart();
         securityMainBehaviour = new SecurityMainBehaviour(securityGuiConfiguration,
                                                           applicationCoreMock,
                                                           loginConfigLoaderMock,
                                                           translationManager,
                                                           translationNotifier);
         systemExitFixture.doSetUp(log);
     }
 
 
     private void registerLanguageBundles(TranslationManager translationManager) {
         translationManager.addBundle("net.codjo.security.gui.i18n", Language.FR);
         translationManager.addBundle("net.codjo.security.gui.i18n", Language.EN);
     }
 
 
     @Override
     protected void tearDown() throws Exception {
         systemExitFixture.doTearDown();
         super.tearDown();
     }
 
 
     public void test_login() throws Exception {
         interceptSplashScreen(
               executeLogin("login", "password", "Intgration", "gdoLdap", false,
                            executeMainBehaviour()))
               .run();
 
         assertContainerConfiguration("martinique", 35700, "login", "password", "gdo2");
 
         log.assertContent("Core.start()");
     }
 
 
     public void test_login_exceptionInStart() throws Exception {
         interceptSplashScreen(
               executeLogin("login", "password", "Intgration", "gdoLdap", false,
                            interceptErrorDialog(
                                  interceptSplashScreen(
                                        executeLogin("login", "password", "Intgration", "gdoLdap", true,
                                                     executeMainBehaviour()))))).run();
 
         assertContainerConfiguration("martinique", 35700, "login", "password", "gdo2");
 
         log.assertContent("Core.start()", "Core.stop()", "Core.start()");
     }
 
 
     public void test_login_extraComponentCustomisation() throws Exception {
         securityGuiConfiguration.setLoginExtraComponent(new JTextField("my extra component"));
 
         interceptSplashScreen(
               new Trigger() {
                   public void run() throws Exception {
                       WindowInterceptor.init(executeMainBehaviour())
                             .process(new WindowHandler() {
                                 @Override
                                 public Trigger process(Window window) throws Exception {
                                     applicationCoreMock.mockStartException(null);
 
                                     window.getTextBox("login").setText("login");
                                     window.getPasswordField("password").setPassword("password");
                                     window.getComboBox("server").select("Intgration");
                                     window.getComboBox("ldap").select("gdoLdap");
 
                                     assertTrue(window.getTextBox("my extra component").isVisible());
 
                                     Trigger trigger = window.getButton("OK").triggerClick();
                                     window.getAwtComponent().setVisible(false);
                                     return trigger;
                                 }
                             })
                             .run();
                   }
               })
               .run();
 
         assertContainerConfiguration("martinique", 35700, "login", "password", "gdo2");
 
         log.assertContent("Core.start()");
     }
 
 
     public void test_autologin() throws Exception {
         securityMainBehaviour.execute("anotherLogin", "anotherPassword", "anotherHostname", "77777");
 
         assertContainerConfiguration("anotherHostname", 77777, "anotherLogin", "anotherPassword", null);
 
         log.assertContent("Core.start()");
     }
 
 
     public void test_autologin_exceptionInStart() throws Exception {
         interceptSplashScreen(
               executeLogin("login", "password", "Intgration", "gdoLdap", false,
                            new Trigger() {
                                public void run() throws Exception {
                                    applicationCoreMock.mockStartException(new RuntimeException(
                                          "Exception during start !!!"));
                                    securityMainBehaviour.execute("anotherLogin",
                                                                  "anotherPassword",
                                                                  "anotherHostname",
                                                                  "77777");
                                }
                            }))
               .run();
 
         assertContainerConfiguration("martinique", 35700, "login", "password", "gdo2");
 
         log.assertContent("Core.start()", "Core.stop()", "Core.start()");
     }
 
 
     public void test_loginCaseType_upperCase() throws Exception {
         securityGuiConfiguration.setLoginCaseType(LoginCaseType.UPPER_CASE);
 
         interceptSplashScreen(
               executeLogin("LOgin", "password", "Intgration", "gdoLdap", false,
                            executeMainBehaviour()))
               .run();
 
         assertContainerConfiguration("martinique", 35700, "LOGIN", "password", "gdo2");
 
         log.assertContent("Core.start()");
     }
 
 
     public void test_loginCaseType_lowerCase() throws Exception {
         securityGuiConfiguration.setLoginCaseType(LoginCaseType.LOWER_CASE);
 
         interceptSplashScreen(
               executeLogin("LOgin", "password", "Intgration", "gdoLdap", false,
                            executeMainBehaviour()))
               .run();
 
         assertContainerConfiguration("martinique", 35700, "login", "password", "gdo2");
 
         log.assertContent("Core.start()");
     }
 
 
     private Trigger executeMainBehaviour(final String... args) {
         return new Trigger() {
             public void run() throws Exception {
                 securityMainBehaviour.execute(args);
             }
         };
     }
 
 
     private Trigger executeLogin(final String login,
                                  final String password,
                                  final String server,
                                  final String ldap,
                                  final boolean authenticationException,
                                  final Trigger trigger) {
         return new Trigger() {
             public void run() throws Exception {
                 WindowInterceptor.init(trigger)
                       .process(login(login, password, server, ldap, authenticationException))
                       .run();
             }
         };
     }
 
 
     private WindowHandler login(final String login,
                                 final String password,
                                 final String server,
                                 final String ldap,
                                 final boolean authenticationException) {
         return new WindowHandler() {
             @Override
             public Trigger process(Window window) throws Exception {
                 if (authenticationException) {
                     applicationCoreMock.mockStartException(new RuntimeException("Authentication failed !!!"));
                 }
                 else {
                     applicationCoreMock.mockStartException(null);
                 }
 
                 window.getTextBox("login").setText(login);
                 window.getPasswordField("password").setPassword(password);
                 window.getComboBox("server").select(server);
                 window.getComboBox("ldap").select(ldap);
                 Trigger trigger = window.getButton("OK").triggerClick();
                 window.getAwtComponent().setVisible(false);
                 return trigger;
             }
         };
     }
 
 
     private Trigger interceptSplashScreen(final Trigger trigger) {
         return new Trigger() {
             public void run() throws Exception {
                 WindowInterceptor.run(trigger);
             }
         };
     }
 
 
     private Trigger interceptErrorDialog(final Trigger trigger) {
         return new Trigger() {
             public void run() throws Exception {
                 WindowInterceptor.init(trigger).process(new WindowHandler() {
                     @Override
                     public Trigger process(Window window) throws Exception {
                         return window.getButton("OK").triggerClick();
                     }
                 }).run();
             }
         };
     }
 
 
     private static LoginConfig createLoginConfig() {
         LoginConfig loginConfig = new LoginConfig();
         loginConfig.setApplicationName("GABI");
         loginConfig.setApplicationVersion("1.05.00.00");
         loginConfig.setApplicationIcon(null);
         loginConfig.setApplicationSplashImage("/images/security.logo.jpg");
         loginConfig.addServer(new Server("Dveloppement", "localhost:35700"));
         loginConfig.addServer(new Server("Intgration", "martinique:35700"));
         loginConfig.addLdap(new Ldap("default", "AM"));
         loginConfig.addLdap(new Ldap("gdo2", "gdoLdap"));
         return loginConfig;
     }
 
 
     private void assertContainerConfiguration(String host,
                                               int port,
                                               String login,
                                               String password,
                                               String ldap) {
         ContainerConfiguration containerConfiguration = applicationCoreMock.getContainerConfig();
         assertEquals(host, containerConfiguration.getHost());
         assertEquals(port, containerConfiguration.getPort());
         assertEquals(login,
                      containerConfiguration.getParameter(SecurityClientPluginConfiguration.LOGIN_PARAMETER));
         assertEquals(password,
                      containerConfiguration.getParameter(SecurityClientPluginConfiguration.PASSWORD_PARAMETER));
         assertEquals(ldap,
                      containerConfiguration.getParameter(SecurityClientPluginConfiguration.LDAP_PARAMETER));
     }
 }
