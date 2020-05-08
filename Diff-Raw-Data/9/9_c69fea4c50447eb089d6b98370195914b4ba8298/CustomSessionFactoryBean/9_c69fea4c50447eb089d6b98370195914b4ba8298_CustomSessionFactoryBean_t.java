 /*
  * Copyright (c) 2012-2013 Veniamin Isaias.
  *
  * This file is part of web4thejob.
  *
  * Web4thejob is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * Web4thejob is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.web4thejob.orm;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.hibernate.engine.jdbc.spi.JdbcServices;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.service.ServiceRegistryBuilder;
 import org.hibernate.tool.hbm2ddl.ConnectionHelper;
 import org.hibernate.tool.hbm2ddl.MyDatabaseExporter;
 import org.hibernate.tool.hbm2ddl.MyManagedProviderConnectionHelper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.ApplicationContext;
 import org.springframework.core.io.Resource;
 import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
 import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
 import org.springframework.util.StringUtils;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.module.Joblet;
 import org.web4thejob.util.CoreUtil;
 import org.web4thejob.util.converter.JobletScanner;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Veniamin Isaias
  * @since 1.0.0
  */
 
 public class CustomSessionFactoryBean extends LocalSessionFactoryBean implements HibernateConfiguration {
 
     private static final Logger LOG = Logger.getLogger(CustomSessionFactoryBean.class);
     private static final String SCHEMA_FILE = "classpath:org/web4thejob/conf/Schema.sql";
     @Autowired
     @Qualifier(CoreUtil.BEAN_ROOT_CONTEXT)
     private ApplicationContext applicationContext;
 
 // -------------------------- OTHER METHODS --------------------------
 
     private void createSchemata(LocalSessionFactoryBuilder sfb) {
 
         if (!applicationContext.getResource(SCHEMA_FILE).exists()) {
             return;
         }
 
         try {
             final ServiceRegistry serviceRegistry = new ServiceRegistryBuilder()
                     .applySettings(sfb.getProperties())
                     .buildServiceRegistry();
 
 
             ConnectionHelper connectionHelper = new MyManagedProviderConnectionHelper(
                     sfb.getProperties());
             connectionHelper.prepare(true);
 
             MyDatabaseExporter myDatabaseExporter = new MyDatabaseExporter(
                     connectionHelper, serviceRegistry
                     .getService(JdbcServices.class).getSqlExceptionHelper());
 
 
             for (String schema : FileUtils.readLines(applicationContext.getResource(SCHEMA_FILE).getFile())) {
                 if (StringUtils.hasText(schema)) {
                     myDatabaseExporter.export(schema);
                 }
             }
 
             LOG.info("SCHEMA creation completed successfully.");
 
         } catch (Exception e) {
             e.printStackTrace();
             LOG.error("Rename or remove file " + SCHEMA_FILE + " so that you don't get previous error.");
         }
     }
 
     private void applyInterceptor(LocalSessionFactoryBuilder sfb) {
         sfb.setInterceptor(new HibernateInterceptor());
     }
 
     @Override
     protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
         createSchemata(sfb);
         applyInterceptor(sfb);
         applyResources(sfb);
         return super.buildSessionFactory(sfb);
     }
 
     private void applyResources(LocalSessionFactoryBuilder sfb) {
 
         if (ContextUtil.isInitialized()) {
             List<Joblet> joblets = new ArrayList<Joblet>();
             joblets.add(ContextUtil.getSystemJoblet());
             joblets.addAll(ContextUtil.getJoblets());
 
             for (Joblet joblet : joblets) {
                 for (Resource resource : joblet.getResources()) {
                     try {
                         sfb.addInputStream(resource.getInputStream());
                     } catch (IOException e) {
                         e.printStackTrace();
                         throw new RuntimeException(e);
                     }
                 }
                 LOG.info("Joblet " + joblet.getName() + " has been loaded automatically.");
             }
         } else {
             LOG.warn("Root context not initialized yet. Falling back to classpath scanning...");
             JobletScanner scanner = new JobletScanner();
            for (Object clazz : scanner.getComponentClasses("org.web4thejob")) {
                 try {
                     Joblet joblet = (Joblet) ((Class) clazz).newInstance();
                     for (Resource resource : joblet.getResources()) {
                         try {
                             sfb.addInputStream(resource.getInputStream());
                         } catch (IOException e) {
                             e.printStackTrace();
                             throw new RuntimeException(e);
                         }
                     }
                     LOG.info("Joblet " + joblet.getName() + " has been loaded automatically.");
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
 
     }
 }
