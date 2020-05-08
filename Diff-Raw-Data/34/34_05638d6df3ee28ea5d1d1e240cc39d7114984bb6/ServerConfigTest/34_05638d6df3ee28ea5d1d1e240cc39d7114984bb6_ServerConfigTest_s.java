 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.core.config;
 
 import org.lafayette.server.core.config.ServerConfig;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class ServerConfigTest {
 
     private ServerConfig sut;
 
     @Before
     public void initSubject() throws IOException {
         final Properties prop = new Properties();
        final InputStream input = getClass().getResourceAsStream("/org/lafayette/server/config/server.properties");
         prop.load(input);
         input.close();
         sut = new ServerConfig(prop);
     }
 
     @Test
     public void getDbHost() {
         assertThat(sut.getDbHost(), is("//localhost"));
     }
 
     @Test
     public void getDbPort() {
         assertThat(sut.getDbPort(), is(3306));
     }
 
     @Test
     public void getDbName() {
         assertThat(sut.getDbName(), is("lafayette"));
     }
 
     @Test
     public void getDbUser() {
         assertThat(sut.getDbUser(), is("root"));
     }
 
     @Test
     public void getDbPassword() {
         assertThat(sut.getDbPassword(), is("lkwe23r"));
     }
 
     @Test
     public void getDbDriver() {
         assertThat(sut.getDbDriver(), is("mysql"));
     }
 
     @Test
     public void getSecurotyRealm() {
         assertThat(sut.getSecurotyRealm(), is("Lafayette restricted area."));
     }
 
     @Test public void getEmptyStringsOrZeroByDefault() {
         final Properties prop = new Properties();
         final ServerConfig config = new ServerConfig(prop);
         assertThat(config.getDbDriver(), is(""));
         assertThat(config.getDbHost(), is(""));
         assertThat(config.getDbName(), is(""));
         assertThat(config.getDbPassword(), is(""));
         assertThat(config.getDbUser(), is(""));
         assertThat(config.getDbPort(), is(0));
         assertThat(config.getSecurotyRealm(), is(""));
     }
 
     @Test
     public void generateJdbcUri_withHost() {
         final Properties prop = new Properties();
         prop.setProperty(ServerConfig.DB_DRIVER, "mysql");
         prop.setProperty(ServerConfig.DB_HOST, "//localhost");
         prop.setProperty(ServerConfig.DB_NAME, "lafayette");
         final ServerConfig config = new ServerConfig(prop);
         assertThat(config.generateJdbcUri(), is("jdbc:mysql://localhost/lafayette"));
     }
 
     @Test
     public void generateJdbcUri_withHostAndPort() {
         final Properties prop = new Properties();
         prop.setProperty(ServerConfig.DB_DRIVER, "mysql");
         prop.setProperty(ServerConfig.DB_HOST, "//localhost");
         prop.setProperty(ServerConfig.DB_PORT, "3306");
         prop.setProperty(ServerConfig.DB_NAME, "lafayette");
         final ServerConfig config = new ServerConfig(prop);
         assertThat(config.generateJdbcUri(), is("jdbc:mysql://localhost:3306/lafayette"));
     }
 
     @Test
     public void generateJdbcUri_withFile() {
         final Properties prop = new Properties();
         prop.setProperty(ServerConfig.DB_DRIVER, "hsqldb");
         prop.setProperty(ServerConfig.DB_HOST, "file:/opt/db");
         prop.setProperty(ServerConfig.DB_NAME, "lafayette");
         final ServerConfig config = new ServerConfig(prop);
         assertThat(config.generateJdbcUri(), is("jdbc:hsqldb:file:/opt/db/lafayette"));
     }
 
 }
