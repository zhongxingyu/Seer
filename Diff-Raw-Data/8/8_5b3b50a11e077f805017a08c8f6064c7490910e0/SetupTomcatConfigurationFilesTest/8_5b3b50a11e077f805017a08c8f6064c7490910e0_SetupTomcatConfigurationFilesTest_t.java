 /*
  * Copyright 2010-2013, the original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.cloudbees.clickstack.tomcat;
 
 import com.cloudbees.clickstack.domain.metadata.Metadata;
 import com.cloudbees.clickstack.util.XmlUtils;
 import com.cloudbees.clickstack.domain.metadata.Database;
 import com.cloudbees.clickstack.domain.metadata.Email;
 import com.cloudbees.clickstack.domain.metadata.SessionStore;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertThat;
 import static org.xmlmatchers.XmlMatchers.isEquivalentTo;
 import static org.xmlmatchers.transform.XmlConverters.the;
 
 /**
  * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
  */
 public class SetupTomcatConfigurationFilesTest {
 
     private Document serverXml;
     private Document contextXml;
 
     @Before
     public void before() throws Exception {
         serverXml = XmlUtils.loadXmlDocumentFromStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("server.xml"));
         contextXml = XmlUtils.loadXmlDocumentFromStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("context.xml"));
     }
 
     @Test
     public void add_private_app_valve_success_basic_config() throws Exception {
         // prepare
         String json = "{ \n" +
                 "'privateApp': { \n" +
                 "    'secretKey': 'ze-supper-secret' \n" +
                 "}\n" +
                 "}";
 
         String xml = "" +
                 "<Valve className='com.cloudbees.tomcat.valves.PrivateAppValve' \n" +
                 "   secretKey='ze-supper-secret'/>";
         test_private_app_valve(json, xml);
 
     }
 
     @Test
     public void add_private_app_valve_success_basic_auth_config() throws Exception {
         // prepare
         String json = "{ \n" +
                 "'privateApp': { \n" +
                 "    'secretKey': 'ze-supper-secret', \n" +
                 "    'authenticationEntryPointName': 'BASIC_AUTH', \n" +
                 "    'realmName': 'ze_realm' \n" +
                 "}\n" +
                 "}";
         String xml = "" +
                 "<Valve className='com.cloudbees.tomcat.valves.PrivateAppValve' \n" +
                 "   authenticationEntryPointName='BASIC_AUTH' \n" +
                 "   realmName='ze_realm' \n" +
                 "   secretKey='ze-supper-secret'/>";
 
         test_private_app_valve(json, xml);
 
 
     }
 
     private void test_private_app_valve(String metadataDotJson, String expectedXml) throws IOException {
         Metadata metadata = Metadata.Builder.fromJsonString(metadataDotJson, true);
         SetupTomcatConfigurationFiles setupTomcatConfigurationFiles = new SetupTomcatConfigurationFiles(metadata);
         // run
         setupTomcatConfigurationFiles.addPrivateAppValve(metadata, serverXml, contextXml);
 
 
         // verify
         Element privateAppValve = XmlUtils.getUniqueElement(serverXml, "//Valve[@className='com.cloudbees.tomcat.valves.PrivateAppValve']");
 
         // XmlUtils.flush(serverXml, System.out);
 
 
         assertThat(the(privateAppValve), isEquivalentTo(the(expectedXml)));
     }
 
     @Test
     public void test_remote_addr_valve_allow() throws Exception {
         // prepare
         String json = "{ \n" +
                 "'remoteAddress': { \n" +
                 "    'allow': '82\\\\.66\\\\.240\\\\.18' \n" +
                 "}\n" +
                 "}";
         String xml = "" +
                 "<Valve className='org.apache.catalina.valves.RemoteAddrValve' \n" +
                 "   allow='82\\.66\\.240\\.18' />";
 
         test_remote_addr_valve(json, xml);
 
 
     }
 
     private void test_remote_addr_valve(String metadataDotJson, String expectedXml) throws IOException {
         Metadata metadata = Metadata.Builder.fromJsonString(metadataDotJson, true);
         SetupTomcatConfigurationFiles setupTomcatConfigurationFiles = new SetupTomcatConfigurationFiles(metadata);
         // run
         setupTomcatConfigurationFiles.addRemoteAddrValve(metadata, serverXml, contextXml);
 
 
         // verify
         Element privateAppValve = XmlUtils.getUniqueElement(serverXml, "//Valve[@className='org.apache.catalina.valves.RemoteAddrValve']");
 
         // XmlUtils.flush(serverXml, System.out);
 
 
         assertThat(the(privateAppValve), isEquivalentTo(the(expectedXml)));
     }
 
     @Test
     public void add_data_source_success_basic_config() throws Exception {
 
         // prepare
         String json = "{ \n" +
                 "'cb-db': { \n" +
                 "    'DATABASE_PASSWORD': 'test', \n" +
                 "    'DATABASE_URL': 'mysql://mysql.mycompany.com:3306/test', \n" +
                 "    'DATABASE_USERNAME': 'test', \n" +
                 "    '__resource_name__': 'mydb', \n" +
                 "    '__resource_type__': 'database' \n" +
                 "}\n" +
                 "}";
         Metadata metadata = Metadata.Builder.fromJsonString(json, true);
         SetupTomcatConfigurationFiles setupTomcatConfigurationFiles = new SetupTomcatConfigurationFiles(metadata);
 
         Database database = metadata.getResource("mydb");
 
         // run
         setupTomcatConfigurationFiles.addDatabase(database, serverXml, contextXml);
 
         // XmlUtils.flush(contextXml, System.out);
 
         // verify
         Element dataSource = XmlUtils.getUniqueElement(contextXml, "//Resource[@name='jdbc/mydb']");
 
         String xml = "" +
                 "<Resource auth='Container' \n" +
                 "   driverClassName='com.mysql.jdbc.Driver' \n" +
                 "   factory='org.apache.tomcat.jdbc.pool.DataSourceFactory' \n" +
                 "   maxActive='20' \n" +
                 "   maxIdle='10' \n" +
                 "   minIdle='1' \n" +
                 "   name='jdbc/mydb' \n" +
                 "   password='test' \n" +
                 "   testOnBorrow='true' \n" +
                 "   testWhileIdle='true' \n" +
                 "   type='javax.sql.DataSource' \n" +
                 "   url='jdbc:mysql://mysql.mycompany.com:3306/test' \n" +
                 "   username='test' \n" +
                 "   validationInterval='5000' \n" +
                 "   validationQuery='select 1'/>";
         assertThat(the(dataSource), isEquivalentTo(the(xml)));
     }
 
     @Test
     public void add_session_store_success_basic_config() throws Exception {
 
         // prepare
         String json = "{ \n" +
                 "'memcache-session-store': { \n" +
                 "    'servers': 'memcache1.mycompany.com,server2.mycompany.com', \n" +
                 "    'username': 'my_acount', \n" +
                 "    'password': '09876543', \n" +
                 "    '__resource_name__': 'memcache-session-store', \n" +
                 "    '__resource_type__': 'session-store' \n" +
                 "}\n" +
                 "}";
         Metadata metadata = Metadata.Builder.fromJsonString(json, true);
         SetupTomcatConfigurationFiles setupTomcatConfigurationFiles = new SetupTomcatConfigurationFiles(metadata);
 
         SessionStore sessionStore = metadata.getResource("memcache-session-store");
 
         // run
         setupTomcatConfigurationFiles.addSessionStore(sessionStore, serverXml, contextXml, metadata);
 
         // XmlUtils.flush(contextXml, System.out);
 
         // verify
         Element sessionManager = XmlUtils.getUniqueElement(contextXml, "//Manager");
 
         String xml = "" +
                 "<Manager className='de.javakaffee.web.msm.MemcachedBackupSessionManager' \n" +
                 "   memcachedNodes='http://memcache1.mycompany.com:8091/pools,http://server2.mycompany.com:8091/pools' \n" +
                 "   memcachedProtocol='binary' \n" +
                 "   password='09876543' \n" +
                 "   requestUriIgnorePattern='.*\\.(ico|png|gif|jpg|css|js)$' \n" +
                 "   sessionBackupAsync='false' \n" +
                 "   sticky='false' \n" +
                 "   transcoderFactoryClass='de.javakaffee.web.msm.serializer.kryo.KryoTranscoderFactory' \n" +
                 "   username='my_acount' />";
         assertThat(the(sessionManager), isEquivalentTo(the(xml)));
     }
 
     @Test
     public void add_session_store_success_enhanced_config() throws Exception {
 
         // prepare
         String json = "{ \n" +
                 "'memcache-session-store': { \n" +
                 "    'servers': 'memcache1.mycompany.com,server2.mycompany.com', \n" +
                 "    'username': 'my_acount', \n" +
                 "    'password': '09876543', \n" +
                 "    'transcoderFactoryClass': 'de.javakaffee.web.msm.JavaSerializationTranscoderFactory', \n" +
                "    'region': 'us', \n" +
                 "    '__resource_name__': 'memcache-session-store', \n" +
                 "    '__resource_type__': 'session-store' \n" +
                 "}\n" +
                 "}";
         Metadata metadata = Metadata.Builder.fromJsonString(json, true);
         SetupTomcatConfigurationFiles setupTomcatConfigurationFiles = new SetupTomcatConfigurationFiles(metadata);
 
         SessionStore sessionStore = metadata.getResource("memcache-session-store");
 
         // run
         setupTomcatConfigurationFiles.addSessionStore(sessionStore, serverXml, contextXml, metadata);
 
         // XmlUtils.flush(contextXml, System.out);
 
         // verify
         Element sessionManager = XmlUtils.getUniqueElement(contextXml, "//Manager");
 
         String xml = "" +
                 "<Manager className='de.javakaffee.web.msm.MemcachedBackupSessionManager' \n" +
                 "   memcachedNodes='http://memcache1.mycompany.com:8091/pools,http://server2.mycompany.com:8091/pools' \n" +
                 "   memcachedProtocol='binary' \n" +
                 "   password='09876543' \n" +
                 "   requestUriIgnorePattern='.*\\.(ico|png|gif|jpg|css|js)$' \n" +
                 "   sessionBackupAsync='false' \n" +
                 "   sticky='false' \n" +
                 "   transcoderFactoryClass='de.javakaffee.web.msm.JavaSerializationTranscoderFactory' \n" +
                 "   username='my_acount' />";
         assertThat(the(sessionManager), isEquivalentTo(the(xml)));
     }
 
 
     @Test
     public void add_mail_session_success_basic_config() throws IOException {
         // prepare
 
         String json = "{ \n" +
                 " 'sendgrid': { \n" +
                 " 'SENDGRID_PASSWORD': '12345', \n" +
                 " 'SENDGRID_SMTP_HOST': 'smtp.sendgrid.net', \n" +
                 " 'SENDGRID_USERNAME': 'my_account', \n" +
                 " '__resource_name__': 'mail/SendGrid', \n" +
                 " '__resource_type__': 'email' \n" +
                 " }\n" +
                 "}";
         Metadata metadata = Metadata.Builder.fromJsonString(json, true);
         Email email = metadata.getResource("mail/SendGrid");
 
         SetupTomcatConfigurationFiles setupTomcatConfigurationFiles = new SetupTomcatConfigurationFiles(metadata);
 
         // run
         setupTomcatConfigurationFiles.addEmail(email, serverXml, contextXml);
 
         // XmlUtils.flush(contextXml, System.out);
 
         // verify
         Element emailSession = XmlUtils.getUniqueElement(contextXml, "//Resource[@name='mail/SendGrid']");
 
         String xml = "" +
                 "<Resource auth='Container' \n" +
                 "   mail.smtp.auth='true' \n" +
                 "   mail.smtp.host='smtp.sendgrid.net' \n" +
                 "   password='12345' \n" +
                 "   mail.smtp.user='my_account' \n" +
                 "   name='mail/SendGrid' \n" +
                 "   type='javax.mail.Session' />";
         assertThat(the(emailSession), isEquivalentTo(the(xml)));
     }
 
     @Test
     public void add_http_connector_config() throws IOException {
         // prepare
 
         String json = "{ \n" +
                 " 'tomcat': { \n" +
                 " 'connector.maxPostSize': '10485760', \n" +
                 " 'ignore.me': 'anyvalue' \n" +
                 " }\n" +
                 "}";
         Metadata metadata = Metadata.Builder.fromJsonString(json, true);
 
         SetupTomcatConfigurationFiles setupTomcatConfigurationFiles = new SetupTomcatConfigurationFiles(metadata);
 
         // run
         setupTomcatConfigurationFiles.updateConnectorConfiguration(metadata, serverXml);
 
         // XmlUtils.flush(serverXml, System.out);
 
         // verify
         Element connector = XmlUtils.getUniqueElement(serverXml, "/Server/Service/Connector");
 
         String xml = "" +
                 "<Connector port='${port.http}' protocol='HTTP/1.1' \n" +
                 "    URIEncoding='UTF-8' \n" +
                 "    maxPostSize='10485760' \n" +
                 "    connectionTimeout='20000' \n" +
                 "    redirectPort='8443'/>";
         assertThat(the(connector), isEquivalentTo(the(xml)));
     }
 }
