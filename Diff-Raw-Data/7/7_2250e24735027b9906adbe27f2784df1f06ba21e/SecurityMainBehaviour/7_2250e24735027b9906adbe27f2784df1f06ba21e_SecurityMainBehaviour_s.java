 package net.codjo.security.gui.plugin;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import javax.swing.SwingUtilities;
 import net.codjo.gui.toolkit.util.ErrorDialog;
 import net.codjo.i18n.common.TranslationManager;
 import net.codjo.i18n.gui.TranslationNotifier;
 import net.codjo.plugin.common.ApplicationCore;
 import net.codjo.plugin.common.ApplicationCore.MainBehaviour;
 import net.codjo.plugin.common.CommandLineArguments;
 import net.codjo.security.client.plugin.SecurityClientPlugin.LoginFailureException;
 import net.codjo.security.client.plugin.SecurityClientPluginConfiguration;
 import net.codjo.security.common.AccountLockedException;
 import net.codjo.security.common.SecurityLevel;
 import net.codjo.security.common.SecurityLevelHelper;
 import net.codjo.security.gui.login.ExternalAccountLockedDialog;
 import net.codjo.security.gui.login.LoginConfig;
 import net.codjo.security.gui.login.LoginConfig.Ldap;
 import net.codjo.security.gui.login.LoginConfig.Server;
 import net.codjo.security.gui.login.LoginWindow;
 import net.codjo.security.gui.login.LoginWindow.LoginCallback;
 import org.apache.log4j.Logger;
 /**
  *
  */
 class SecurityMainBehaviour implements MainBehaviour {
     private static final Logger LOG = Logger.getLogger(SecurityMainBehaviour.class);
     private static final String USER_ENVIRONMENT = "user.environment";
     private final SecurityGuiConfiguration configuration;
     private final ApplicationCore applicationCore;
     private final LoginConfigLoader loginConfigLoader;
     private TranslationManager translationManager;
     private TranslationNotifier translationNotifier;
     private final Lock lock = new ReentrantLock();
     private final Condition authenticationSuccess = lock.newCondition();
     private LoginConfig loginConfig;
     private LoginWindow loginWindow;
 
 
     SecurityMainBehaviour(SecurityGuiConfiguration configuration,
                           ApplicationCore applicationCore,
                           LoginConfigLoader loginConfigLoader,
                           TranslationManager translationManager,
                           TranslationNotifier translationNotifier) {
         this.configuration = configuration;
         this.applicationCore = applicationCore;
         this.loginConfigLoader = loginConfigLoader;
         this.translationManager = translationManager;
         this.translationNotifier = translationNotifier;
     }
 
 
     public void execute(String... args) throws Exception {
         loginConfig = loginConfigLoader.load("/conf/application.properties");
 
         if (args.length >= 4) {
             autologin(args);
         }
         else {
             showLoginWindow();
         }
     }
 
 
     private void autologin(String... args) {
         try {
             String login = args[0];
             String password = args[1];
             String host = args[2];
             int port = Integer.parseInt(args[3]);
             tryToStart(login, password, host, port, null, null);
         }
         catch (Exception e) {
             LOG.warn("Autologin failed", e);
             showLoginWindow();
         }
     }
 
 
     private void showLoginWindow() {
         loginWindow = new LoginWindow(loginConfig,
                                       new MyLoginCallback(),
                                       configuration.getLoginExtraComponent(),
                                       translationManager,
                                       translationNotifier);
 
         lock.lock();
         try {
             showLoginWindowInEventDispatchThread();
             authenticationSuccess.awaitUninterruptibly();
         }
         finally {
             lock.unlock();
         }
     }
 
 
     private void showLoginWindowInEventDispatchThread() {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 loginWindow.resetPasswordAndFocus();
                 loginWindow.setVisible(true);
             }
         });
     }
 
 
     private void authenticate(String login,
                               String password,
                               String host,
                               int port,
                               String environment,
                               String domain) {
         lock.lock();
         try {
             doAuthenticate(login, password, host, port, environment, domain);
         }
         catch (Exception e) {
             ErrorDialog.show(loginWindow, "Echec de connexion", e);
             showLoginWindowInEventDispatchThread();
         }
         finally {
             lock.unlock();
         }
     }
 
 
     private void doAuthenticate(String login,
                                 String password,
                                 String host,
                                 int port,
                                 String environment,
                                 String domain) throws Exception {
         try {
             tryToStart(login, password, host, port, environment, domain);
             loginWindow.dispose();
             authenticationSuccess.signal();
         }
         catch (LoginFailureException e) {
             Throwable cause = e.getCause();
             if (AccountLockedException.class.isInstance(cause)
                 && ((AccountLockedException)e.getCause()).getUrlToUnlock() != null) {
                 new ExternalAccountLockedDialog(((AccountLockedException)cause).getUrlToUnlock())
                       .show(loginWindow);
                 return;
             }
 
             throw e;
         }
     }
 
 
     private void tryToStart(String login,
                             String password,
                             String host,
                             int port,
                             String environment,
                             String domain) throws Exception {
         try {
             if (environment != null) {
                 applicationCore.getContainerConfig().setParameter(USER_ENVIRONMENT, environment);
                 setupEnvironmentForLegacyCompatibility(environment);
             }
             switch (configuration.getLoginCaseType()) {
                 case LOWER_CASE:
                     login = login.toLowerCase();
                     break;
                 case UPPER_CASE:
                     login = login.toUpperCase();
                     break;
                 case UNDEFINED:
                     break;
             }
             initContainerConfiguration(login, password, host, port, domain);
             applicationCore.start(new CommandLineArguments(new String[0]));
         }
         catch (Exception e) {
             try {
                 applicationCore.stop();
             }
             catch (Exception e1) {
                 LOG.warn("Extinction des plugins en erreur", e1);
             }
             throw e;
         }
     }
 
 
     private void setupEnvironmentForLegacyCompatibility(String environment) {
         System.setProperty(USER_ENVIRONMENT, environment);
     }
 
 
     private void initContainerConfiguration(String login,
                                             String password,
                                             String host,
                                             int port,
                                             String domain) {
         applicationCore.getContainerConfig().setHost(host);
         applicationCore.getContainerConfig().setPort(port);
         applicationCore.getContainerConfig()
               .setParameter(SecurityClientPluginConfiguration.LOGIN_PARAMETER, login);
         applicationCore.getContainerConfig()
               .setParameter(SecurityClientPluginConfiguration.PASSWORD_PARAMETER, password);
         applicationCore.getContainerConfig()
               .setParameter(SecurityClientPluginConfiguration.LDAP_PARAMETER, domain);
         SecurityLevelHelper.setSecurityLevel(applicationCore.getContainerConfig(), SecurityLevel.USER);
     }
 
 
     private void quit() {
         System.exit(0);
     }
 
 
     private class MyLoginCallback implements LoginCallback {
 
         public void doConnect(String login, String password, Server server, Ldap ldap) {
             String[] splitServer = server.getUrl().split(":");
             authenticate(
                   login,
                   password,
                   splitServer[0],
                   Integer.parseInt(splitServer[1]),
                   server.getName(),
                   ldap == null ? null : ldap.getName());
         }
 
 
         public void doQuit() {
             quit();
         }
     }
 }
