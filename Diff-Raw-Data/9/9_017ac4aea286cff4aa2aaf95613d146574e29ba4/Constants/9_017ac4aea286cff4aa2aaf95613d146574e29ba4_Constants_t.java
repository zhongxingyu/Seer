 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: Constants.java,v 1.4 2006-12-13 20:53:50 beomsuk Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.shared;
 
 /**
  * This interface contains all the property names defined in in
  * product configurations and may be expanded with other constant
  * values that are used for Access and Federation Manager development.
  */
 public interface Constants {
     /**
      * Property string for debug level.
      */
     String SERVICES_DEBUG_LEVEL = "com.iplanet.services.debug.level";
 
     /**
      * Property string for debug directory.
      */
     String SERVICES_DEBUG_DIRECTORY = "com.iplanet.services.debug.directory";
 
     /**
      * Property string for interval of <code>Stats</code> service.
      */
     String AM_STATS_INTERVAL = "com.iplanet.am.stats.interval";
 
     /**
      * Property string for state of <code>Stats</code> service.
      */
     String SERVICES_STATS_STATE = "com.iplanet.services.stats.state";
 
     /**
      * Property string for directory of <code>Stats</code> service.
      */
     String SERVICES_STATS_DIRECTORY = "com.iplanet.services.stats.directory";
 
     /**
      * Property string for SDK caching size.
      */
     String AM_SDK_CACHE_MAXSIZE = 
         "com.iplanet.am.sdk.cache.maxSize";
 
     /**
      * Property string for module for processing user handling.
      */
     String AM_SDK_USER_ENTRY_PROCESSING_IMPL = 
         "com.iplanet.am.sdk.userEntryProcessingImpl";
 
     /**
      * Property string for default organization.
      */
     String AM_DEFAULT_ORG = 
         "com.iplanet.am.defaultOrg";
 
     /**
      * Property string for SSL enabled.
      */
     String AM_DIRECTORY_SSL_ENABLED = 
         "com.iplanet.am.directory.ssl.enabled";
 
     /**
      * Property string for directory host.
      */
     String AM_DIRECTORY_HOST = 
         "com.iplanet.am.directory.host";
 
     /**
      * Property string for directory port.
      */
     String AM_DIRECTORY_PORT = 
         "com.iplanet.am.directory.port";
 
     /**
      * Property string for server protocol.
      */
     String AM_SERVER_PROTOCOL = 
         "com.iplanet.am.server.protocol";
 
     /**
      * Property string for server host.
      */
     String AM_SERVER_HOST = 
         "com.iplanet.am.server.host";
 
     /**
      * Property string for server port.
      */
     String AM_SERVER_PORT = 
         "com.iplanet.am.server.port";
 
     /**
      * Property string for Distributed Authentication server protocol.
      */
     String DISTAUTH_SERVER_PROTOCOL = 
         "com.iplanet.distAuth.server.protocol";
 
     /**
      * Property string for Distributed Authentication server host.
      */
     String DISTAUTH_SERVER_HOST = 
         "com.iplanet.distAuth.server.host";
 
     /**
      * Property string for Distributed Authentication server port.
      */
     String DISTAUTH_SERVER_PORT = 
         "com.iplanet.distAuth.server.port";
 
     /**
      * Property string for console protocol.
      */
     String AM_CONSOLE_PROTOCOL = 
         "com.iplanet.am.console.protocol";
 
     /**
      * Property string for console host.
      */
     String AM_CONSOLE_HOST = 
         "com.iplanet.am.console.host";
 
     /**
      * Property string for console port.
      */
     String AM_CONSOLE_PORT = 
         "com.iplanet.am.console.port";
 
     /**
      * Property string for profile host.
      */
     String AM_PROFILE_HOST = 
         "com.iplanet.am.profile.host";
 
     /**
      * Property string for profile port.
      */
     String AM_PROFILE_PORT = 
         "com.iplanet.am.profile.port";
 
     /**
      * Property string for naming URL.
      */
     String AM_NAMING_URL = 
         "com.iplanet.am.naming.url";
 
     /**
      * Property string for notification URL.
      */
     String AM_NOTIFICATION_URL = "com.iplanet.am.notification.url";
 
     /**
      * Property string for load balancer.
      */
     String AM_REDIRECT = "com.sun.identity.url.redirect";
 
     /**
      * Property string for daemon process.
      */
     String AM_DAEMONS = "com.iplanet.am.daemons";
 
     /**
      * Property string for cookie name.
      */
     String AM_COOKIE_NAME = "com.iplanet.am.cookie.name";
 
     /**
      * Property string for load balancer cookie name.
      */
     String AM_LB_COOKIE_NAME = "com.iplanet.am.lbcookie.name";
 
     /**
      * Property string for load balancer cookie value.
      */
     String AM_LB_COOKIE_VALUE = "com.iplanet.am.lbcookie.value";
 
     /**
      * Property string for secure cookie.
      */
     String AM_COOKIE_SECURE = "com.iplanet.am.cookie.secure";
 
     /**
      * Property string for cookie encoding.
      */
     String AM_COOKIE_ENCODE = "com.iplanet.am.cookie.encode";
 
     /**
      * Property string for <code>pcookie</code> name.
      */
     String AM_PCOOKIE_NAME = "com.iplanet.am.pcookie.name";
 
     /**
      * Property string for locale.
      */
     String AM_LOCALE = "com.iplanet.am.locale";
 
     /**
      * Property string for log status.
      */
     String AM_LOGSTATUS = "com.iplanet.am.logstatus";
 
     /**
      * Property string for directory root suffix.
      */
     String AM_ROOT_SUFFIX = "com.iplanet.am.rootsuffix";
 
     /**
      * Property string for domain component.
      */
     String AM_DOMAIN_COMPONENT = "com.iplanet.am.domaincomponent";
 
     /**
      * Property string for version number.
      */
     String AM_VERSION = "com.iplanet.am.version";
 
     /**
      * Property string for <code>CertDB</code> directory.
      */
     String AM_ADMIN_CLI_CERTDB_DIR = "com.iplanet.am.admin.cli.certdb.dir";
 
     /**
      * Property string for <code>CertDB</code> prefix.
      */
     String AM_ADMIN_CLI_CERTDB_PREFIX ="com.iplanet.am.admin.cli.certdb.prefix";
 
     /**
      * Property string for <code>CertDB</code> password file.
      */
     String AM_ADMIN_CLI_CERTDB_PASSFILE = 
         "com.iplanet.am.admin.cli.certdb.passfile";
 
     /**
      * Property string for OCSP responder URL.
      */
     String AUTHENTICATION_OCSP_RESPONDER_URL = 
         "com.sun.identity.authentication.ocsp.responder.url";
 
     /**
      * Property string for OCSP responder nickname.
      */
     String AUTHENTICATION_OCSP_RESPONDER_NICKNAME = 
         "com.sun.identity.authentication.ocsp.responder.nickname";
 
     /**
      * Property string for SAML XML signature key store file.
      */
     String SAML_XMLSIG_KEYSTORE =
         "com.sun.identity.saml.xmlsig.keystore";
 
     /**
      * Property string for SAML XML signature key store password file.
      */
     String SAML_XMLSIG_STORE_PASS = 
         "com.sun.identity.saml.xmlsig.storepass";
 
     /**
      * Property string for SAML XML signature key password file.
      */
     String SAML_XMLSIG_KEYPASS = "com.sun.identity.saml.xmlsig.keypass";
 
     /**
      * Property string for SAML XML signature CERT alias.
      */
     String SAML_XMLSIG_CERT_ALIAS = "com.sun.identity.saml.xmlsig.certalias";
 
     /**
      * Property string for authentication super user.
      */
     String AUTHENTICATION_SUPER_USER = 
         "com.sun.identity.authentication.super.user";
 
     /**
      * Property string for authentication super user.
      */
     String AUTHENTICATION_SPECIAL_USERS = 
         "com.sun.identity.authentication.special.users";
 
     /**
      * Property string for replica retry number.
      */
     String AM_REPLICA_NUM_RETRIES = "com.iplanet.am.replica.num.retries";
 
     /**
      * Property string for delay between replica retries.
      */
     String AM_REPLICA_DELAY_BETWEEN_RETRIES =
         "com.iplanet.am.replica.delay.between.retries";
 
     /**
      * Property string for retry number for event connection.
      */
     String AM_EVENT_CONNECTION_NUM_RETRIES = 
         "com.iplanet.am.event.connection.num.retries";
 
     /**
      * Property string for delay time between retries for event connection.
      */
     String AM_EVENT_CONNECTION_DELAY_BETWEEN_RETRIES = 
         "com.iplanet.am.event.connection.delay.between.retries";
 
     /**
      * Property string for <code>LDAPException</code> error codes that retries
      * will happen for event connection.
      */
     String AM_EVENT_CONNECCTION_LDAP_ERROR_CODES_RETRIES =
         "com.iplanet.am.event.connection.ldap.error.codes.retries";
 
     /**
      * Property string for number of time to retry for LDAP connection.
      */
     String AM_LDAP_CONNECTION_NUM_RETRIES = 
         "com.iplanet.am.ldap.connection.num.retries";
 
     /**
      * Property string for delay time between retries for LDAP connection.
      */
     String AM_LDAP_CONNECTION_DELAY_BETWEEN_RETRIES = 
         "com.iplanet.am.ldap.connection.delay.between.retries";
 
     /**
      * Property string for <code>LDAPException</code> error codes that retries
      * will happen for LDAP connection.
      */
     String AM_LDAP_CONNECTION_LDAP_ERROR_CODES_RETRIES = 
         "com.iplanet.am.ldap.connection.ldap.error.codes.retries";
 
     /**
      * Property string for installation directory
      */
     String AM_INSTALL_DIR = "com.iplanet.am.installdir";
 
     /**
      * Property string for installation base directory
      */
     String AM_INSTALL_BASEDIR = 
         "com.iplanet.am.install.basedir";
 
     /**
      * Property string for new configuraton file in case of single war
      * deployment
      */
     String AM_NEW_CONFIGFILE_PATH = 
         "com.sun.identity.configFilePath";
 
     /**
      * Property string for installation config directory
      */
     String AM_INSTALL_VARDIR = 
         "com.iplanet.am.install.vardir";
 
     /**
      * Property string for shared secret for application authentication module
      */
     String AM_SERVICES_SECRET = 
         "com.iplanet.am.service.secret";
 
     /**
      * Property string for service deployment descriptor
      */
     String AM_SERVICES_DEPLOYMENT_DESCRIPTOR =
         "com.iplanet.am.services.deploymentDescriptor";
 
     /**
      * Property string for console deployment descriptor
      */
     String AM_CONSOLE_DEPLOYMENT_DESCRIPTOR =
         "com.iplanet.am.console.deploymentDescriptor";
 
     /**
      * Property string for agent URL deployment descriptor
      */
     String AM_POLICY_AGENTS_URL_DEPLOYMENT_DESCRIPTOR =
         "com.iplanet.am.policy.agents.url.deploymentDescriptor";
 
     /**
      * property string which contains the name of HTTP session tracking cookie
      */
     String AM_SESSION_HTTP_SESSION_TRACKING_COOKIE_NAME = 
         "com.iplanet.am.session.failover.httpSessionTrackingCookieName";
 
     /**
      * property string to choose whether local or remote saving method is used
      */
     String AM_SESSION_FAILOVER_USE_REMOTE_SAVE_METHOD = 
         "com.iplanet.am.session.failover.useRemoteSaveMethod";
 
     /**
      * property string to choose whether we rely on app server load balancer to
      * do the request routing or use our own
      */
     String 
         AM_SESSION_FAILOVER_USE_INTERNAL_REQUEST_ROUTING = 
             "com.iplanet.am.session.failover.useInternalRequestRouting";
 
     /**
      * Property string for failover cluster state check timeout
      */
     String AM_SESSION_FAILOVER_CLUSTER_SERVER_LIST =
         "com.iplanet.am.session.failover.cluster.serverList";
 
     /**
      * Property string for failover cluster state check timeout
      */
     String 
         AM_SESSION_FAILOVER_CLUSTER_STATE_CHECK_TIMEOUT = 
             "com.iplanet.am.session.failover.cluster.stateCheck.timeout";
 
     /**
      * Property string for failover cluster state check period
      */
     String AM_SESSION_FAILOVER_CLUSTER_STATE_CHECK_PERIOD = 
         "com.iplanet.am.session.failover.cluster.stateCheck.period";
 
     /**
      * Property string for naming <code>failover</code> URL
      */
     String AM_NAMING_FAILOVER_URL = 
         "com.iplanet.am.naming.failover.url";
 
     /**
      * Property string for max number of sessions
      */
     String AM_SESSION_MAX_SESSIONS = 
         "com.iplanet.am.session.maxSessions";
 
     /**
      * Property string for checking if HTTP session is enabled
      */
     String AM_SESSION_HTTP_SESSION_ENABLED = 
         "com.iplanet.am.session.httpSession.enabled";
 
     /**
      * Property string for max session time for invalid session
      */
     String AM_SESSION_INVALID_SESSION_MAXTIME = 
         "com.iplanet.am.session.invalidsessionmaxtime";
 
     /**
      * Property string for checking if session client polling is enabled
      */
     String AM_SESSION_CLIENT_POLLING_ENABLED = 
         "com.iplanet.am.session.client.polling.enable";
 
     /**
      * Property string for session client polling period.
      */
     String AM_SESSION_CLIENT_POLLING_PERIOD = 
         "com.iplanet.am.session.client.polling.period";
 
     /**
      * Property string for security provider package.
      */
     String SECURITY_PROVIDER_PKG = 
         "com.sun.identity.security.x509.pkg";
     
     /**
      * Property string for iplanet security provider package.
      */
     String IPLANET_SECURITY_PROVIDER_PKG = 
         "com.iplanet.security.x509.impl";
 
     /**
      * Property string for sun security provider package.
      */
     String SUN_SECURITY_PROVIDER_PKG = 
         "com.sun.identity.security.x509.impl";
 
     /**
      * Property string for UNIX helper port.
      */
     String UNIX_HELPER_PORT = 
         "unixHelper.port";
 
     /**
      * Property string for <code>securid</code> authentication module and
      * helper port.
      */
     String SECURID_HELPER_PORTS = "securidHelper.ports";
 
     /**
      * Property string for SMTP host.
      */
     String AM_SMTP_HOST = "com.iplanet.am.smtphost";
 
     /**
      * Property string for SMTP port.
      */
     String SM_SMTP_PORT = "com.iplanet.am.smtpport";
 
     /**
      * Property string for CDSSO URL.
      */
     String SERVICES_CDSSO_CDCURL = "com.iplanet.services.cdsso.CDCURL";
 
     /**
      * Property string for CDSSO cookie domain.
      */
     String SERVICES_CDSSO_COOKIE_DOMAIN = 
         "com.iplanet.services.cdsso.cookiedomain";
 
     /**
      * Property string for CDC auth login URL.
      */
     String SERVICES_CDC_AUTH_LOGIN_URL = 
         "com.iplanet.services.cdc.authLoginUrl";
 
     /**
      * Property string for maximum content-length accepted in HttpRequest.
      */
     String 
         SERVICES_COMM_SERVER_PLLREQUEST_MAX_CONTENT_LENGTH = 
             "com.iplanet.services.comm.server.pllrequest.maxContentLength";
 
     /**
      * Property string for factory class name for
      * <code>SecureRandomFactory</code>.
      */
     String SECURITY_SECURE_RANDOM_FACTORY_IMPL = 
         "com.iplanet.security.SecureRandomFactoryImpl";
 
     /**
      * Property string for factory class name for <code>LDAPSocketFactory</code>
      */
     String SECURITY_SSL_SOCKET_FACTORY_IMPL = 
         "com.iplanet.security.SSLSocketFactoryImpl";
 
     /**
      * Property string for encrypting class implementation.
      */
     String SECURITY_ENCRYPTOR = "com.iplanet.security.encryptor";
 
     /**
      * Property string for checking if console is remote.
      */
     String AM_CONSOLE_REMOTE = "com.iplanet.am.console.remote";
 
     /**
      * Property string for checking if client IP check is enabled.
      */
     String AM_CLIENT_IP_CHECK_ENABLED = "com.iplanet.am.clientIPCheckEnabled";
 
     /**
      * Property string for user name for application authentication module.
      */
     String AGENTS_APP_USER_NAME = "com.sun.identity.agents.app.username";
 
     /**
      * Property string for log file name for logging messages.
      */
     String AGENTS_SERVER_LOG_FILE_NAME = 
         "com.sun.identity.agents.server.log.file.name";
 
     /**
      * Property string for resource result cache size.
      */
     String AGENTS_CACHE_SIZE = "com.sun.identity.agents.cache.size";
 
     /**
      * Property string for agent polling interval.
      */
     String AGENTS_POLLING_INTERVAL = "com.sun.identity.agents.polling.interval";
 
     /**
      * Property string for checking if agent notification is enabled.
      */
     String AGENTS_NOTIFICATION_ENABLED = 
         "com.sun.identity.agents.notification.enabled";
 
     /**
      * Property string for agent notification URL.
      */
     String AGENTS_NOTIFICATION_URL = "com.sun.identity.agents.notification.url";
 
     /**
      * Property string for checking whether to use wildcard for resource name
      * comparison.
      */
     String AGENTS_USE_WILDCARD = "com.sun.identity.agents.use.wildcard";
 
     /**
      * Property string for attributes to be returned by policy evaluator.
      */
     String AGENTS_HEADER_ATRIBUTES = 
         "com.sun.identity.agents.header.attributes";
 
     /**
      * Property string for resource comparator class name.
      */
     String AGENTS_RESOURCE_COMPARATOR_CLASS = 
         "com.sun.identity.agents.resource.comparator.class";
 
     /**
      * Property string for resource comparator class name.
      */
     String AGENTS_RESOURCE_WILDCARD = 
         "com.sun.identity.agents.resource.wildcard";
 
     /**
      * Property string for resource name's delimiter.
      */
     String AGENTS_RESOURCE_DELIMITER = 
         "com.sun.identity.agents.resource.delimiter";
 
     /**
      * Property string for indicator if case sensitive is on during policy
      * evaluation.
      */
     String AGENTS_RESOURCE_CASE_SENSITIVE = 
         "com.sun.identity.agents.resource.caseSensitive";
 
     /**
      * Property string for true value of policy action.
      */
     String AGENTS_TRUE_VALUE = "com.sun.identity.agents.true.value";
 
     /**
      * Property string for federation service cookie.
      */
     String FEDERATION_FED_COOKIE_NAME = 
         "com.sun.identity.federation.fedCookieName";
 
     /**
      * Property string for federation signing on indicator.
      */
     String FEDERATION_SERVICES_SIGNING_ON = 
         "com.sun.identity.federation.services.signingOn";
 
     /**
      * Property string for session notification thread pool size.
      */
     String NOTIFICATION_THREADPOOL_SIZE = 
         "com.iplanet.am.notification.threadpool.size";
 
     /**
      * Property string for name of the webcontainer.
      */
     String IDENTITY_WEB_CONTAINER = "com.sun.identity.webcontainer";
 
     /**
      * Property string for session notification thread pool queue size.
      */
     String NOTIFICATION_THREADPOOL_THRESHOLD = 
         "com.iplanet.am.notification.threadpool.threshold";
 
     /**
      * Property string for fully qualified host name map.
      */
     String AM_FQDN_MAP = "com.sun.identity.server.fqdnMap";
 
     /**
      * Client detection module content type property name.
      */
     String CDM_CONTENT_TYPE_PROPERTY_NAME = "contentType";
 
     /**
      * Default charset to be used in case the client detection has failed.
      */
     String CONSOLE_UI_DEFAULT_CHARSET = "UTF-8";
 
     /**
      * Attribute name of the user preferred locale located in amUser service.
      */
     String USER_LOCALE_ATTR = "preferredlocale";
 
     /**
      * Property string for checking if <code>HostLookUp</code> is enabled.
      */
     String ENABLE_HOST_LOOKUP = "com.sun.am.session.enableHostLookUp";
 
     /**
      * Property string for checking if <code>HostLookUp</code> is enabled.
      */
     String WEBCONTAINER = "com.sun.identity.webcontainer";
 
     /**
      * Property string for determining if cookie needs to be written in the URL
      * as a path info.
      */
     String REWRITE_AS_PATH = 
         "com.sun.identity.cookieRewritingInPath";
 
     /**
      * Property string for Application session max-caching-time.
      */
     String APPLICATION_SESSION_MAX_CACHING_TIME = 
         "com.sun.identity.session.application.maxCacheTime";
 
     /**
      * Property string to enable Session/Cookie hijacking mode in Access
      * Manager.
      */
     String IS_ENABLE_UNIQUE_COOKIE = 
         "com.sun.identity.enableUniqueSSOTokenCookie";
 
     /**
      * Property string for 'HostUrl' Cookie name in Session/Cookie hijacking
      * mode.
      */
     String AUTH_UNIQUE_COOKIE_NAME = 
         "com.sun.identity.authentication.uniqueCookieName";
 
     /**
      * Property string for unique Cookie domain in Session/Cookie hijacking
      * mode.
      */
     String AUTH_UNIQUE_COOKIE_DOMAIN =
         "com.sun.identity.authentication.uniqueCookieDomain";
 
     /**
      * Property string for checking if remote method
      * <code>AddListenerOnAllSessions</code> is enabled.
      */
     String ENABLE_ADD_LISTENER_ON_ALL_SESSIONS =
         "com.sun.am.session.enableAddListenerOnAllSessions";
 
     /**
      * Property string for list of IP address of remote clients which are
      * considered trusted to forward the context used to check <code>restricted
      * token usage</code> is enabled.
      */
     String TRUSTED_SOURCE_LIST = "com.sun.am.session.trustedSourceList";
 
     /**
      * Property string to Identify the Http Header which returns the Client IP
      * address when running in loadbalancer configuration.
      */
     String HTTP_CLIENT_IP_HEADER = 
         "com.sun.identity.session.httpClientIPHeader";
 
     /**
      * User object type.
      */
     String OBJECT_TYPE_USER = "user";
 
     /**
      * Agent object type.
      */
     String OBJECT_TYPE_AGENT = "Agent";
 
     /**
      * Property string to ensure more stringent (security-wise) check If enabled
      * the <code>DN is converted to lowercase</code> for comparison.
      */
     String CASE_INSENSITIVE_DN = "com.sun.am.session.caseInsensitiveDN";
 
     /**
      * Property string to determine if validation is required when parsing XML
      * documents using Access Manager XMLUtils class.
      */
     String XML_VALIDATING = "com.iplanet.am.util.xml.validating";
 
     /**
      * Property string to determine if authentication enforces using seperate
      * JAAS thread or not.
      */
     String ENFORCE_JAAS_THREAD = 
         "com.sun.identity.authentication.usingJaasThread";
 
     /**
      * Property string to list all the Session properties that should be
      * protected.
      */
     String PROTECTED_PROPERTIES_LIST = 
         "com.iplanet.am.session.protectedPropertiesList";
 
     /**
      * Property for Login URL.
      */
     String LOGIN_URL = "com.sun.identity.loginurl";
 
     /**
      * System property name that is a list of package name prefixes is used to
      * resolve protocol names into actual handler class names. 
      */
     String PROTOCOL_HANDLER = "java.protocol.handler.pkgs";
 
     /**
      * The package name prefix for JSS based protocol implementations.
      */
     String JSS_HANDLER = "com.iplanet.services.comm";
 
     /**
      * The package name prefix for JSSE based protocol implementations.
      */
     String JSSE_HANDLER = "com.sun.identity.protocol";
 
     /**
      * Property for passing the organization name when retrieving attribute
      * choice values.
      */
     String ORGANIZATION_NAME = "organization_name";
 
     /**
      * Organization name in Session/SSOToken Properties.
      */
     String ORGANIZATION = "Organization";
 
     /**
      * Property for auth cookie name.
      */
     String AM_AUTH_COOKIE_NAME = "com.sun.identity.auth.cookieName";
 
     /**
      * Unique Id set as a session property which is used for logging.
      */
     String AM_CTX_ID = "AMCtxId";
 
     /**
      * Global schema property name in Session Service.
      */
     String PROPERTY_CHANGE_NOTIFICATION = 
         "iplanet-am-session-property-change-notification";
 
     /**
      * Global schema property name in Session Service.
      */
     String NOTIFICATION_PROPERTY_LIST =
         "iplanet-am-session-notification-property-list";
 
     /**
      * The session property name of the universal identifier used for IDRepo.
      */
     String UNIVERSAL_IDENTIFIER = "sun.am.UniversalIdentifier";
 
     /**
      * Property string for session polling thread pool size.
      */
     String POLLING_THREADPOOL_SIZE =
         "com.sun.identity.session.polling.threadpool.size";
 
     /**
      * Property string for session polling thread pool queue size.
      */
     String POLLING_THREADPOOL_THRESHOLD = 
         "com.sun.identity.session.polling.threadpool.threshold";
 
     /**
      * Property for enabling or disabling encryption for Session Repository.
      */
     String SESSION_REPOSITORY_ENCRYPTION = 
         "com.sun.identity.session.repository.enableEncryption";
 
     /**
      * Property string for determining whether or not appplication sessions
      * should be returned via the getValidSessions() call.
      */
     String SESSION_RETURN_APP_SESSION = 
         "com.sun.identity.session.returnAppSession";
 
     /**
      * HTTP Form Parameter name used by PEP for posting policy advices to
      * Access Manager.
      */
     String COMPOSITE_ADVICE = "sunamcompositeadvice";
 
     /**
      * XML tag name used for Advices message.
      */
     String ADVICES_TAG_NAME = "Advices";
 
     /**
      * Key that is used to identify the advice messages from
      * <code>AuthSchemeCondition</code>.
      */
     String AUTH_SCHEME_CONDITION_ADVICE = "AuthSchemeConditionAdvice";
 
     /** Key that is used to identify the advice messages from
      * <code>AuthLevelCondition</code>.
      */   
     String AUTH_LEVEL_CONDITION_ADVICE = "AuthLevelConditionAdvice";
 
     /**
      * Property string for determining whether server mode or client mode.
      */
     String SERVER_MODE = "com.iplanet.am.serverMode";
 
     /**
      * Key name for platform server list in naming table.
      */
     String PLATFORM_LIST = "iplanet-am-platform-server-list";
 
     /**
      * Key name for site list in naming table.
      */
     String SITE_LIST = "iplanet-am-platform-site-list";
 
     /**
      * Key name for site ID list in naming table.
      */
     String SITE_ID_LIST = "iplanet-am-platform-site-id-list";
 
     /**
      * Key name for site ID list in naming table.
      */
     String CLUSTER_SERVER_LIST = "iplanet-am-session-cluster-serverlist";
 
     /**
      * Default organization location properties name.
      */
     String DEFAULT_ORGANIZATION = "com.iplanet.am.defaultOrg";
 
     /**
      * This value is used by LDAP connection pool to reap connections
      * if they are idle for the number of seconds specified by the
      * value of this property.  If the value is set at 0, the connection
      * will not be reaped.
      */
     String LDAP_CONN_IDLE_TIME_IN_SECS =
         "com.sun.am.ldap.connnection.idle.seconds";
 
     /**
      * Constant for file separator.
      */
     String FILE_SEPARATOR = "/";
 
     /**
      * Install Time System property key.
      */
     String SYS_PROPERTY_INSTALL_TIME = "installTime";
 
     /**
      * This is a HTTP parameter to indicate to the authentication component
      * to either forward the request or redirect it after authentication
      * succeed.
      */
     String FORWARD_PARAM = "forwardrequest";
                                                                                 
     /**
      * Value is for <code>FORWARD_PARAM</code> to indicate that the 
      * authentication component should forward request.
      */
     String FORWARD_YES_VALUE = "yes";
    
    /**
     * Attribute name for the load balancer cookie in the
     * Naming Response.
     */
    public static final String NAMING_AM_LB_COOKIE = "am_load_balancer_cookie";
 }
