 package com.spreadthesource.tapestry.spring.hibernate;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 
import org.apache.tapestry5.ioc.internal.util.Defense;
 import org.hibernate.HibernateException;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.cfg.Configuration;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
 import org.springframework.core.type.classreading.MetadataReader;
 import org.springframework.core.type.classreading.MetadataReaderFactory;
 import org.springframework.core.type.filter.TypeFilter;
 import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
 
 public class TapestryLocalSessionFactoryBean extends LocalSessionFactoryBean
 {
     /**
      * List of root packages containing entity to load.
      */
     private List<String> packageNames;
 
     @Override
     protected void postProcessConfiguration(Configuration config) throws HibernateException
     {
         
         config.setProperty("hibernate.connection.autocommit", "false");
         config.setProperty("hibernate.connection.pool_size", "1");
         config.setProperty("hibernate.show_sql", "true");
         config.setProperty("current_session_context_class", "thread");
         
        Defense.cast(config, AnnotationConfiguration.class, "configuration");
         AnnotationConfiguration cfg = (AnnotationConfiguration) config;
 
         if (this.getPackageNames() != null)
         {
             for (String packageName : this.getPackageNames())
             {
                 cfg.addPackage(packageName);
 
                 ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                         true);
                 provider.addIncludeFilter(new TypeFilter()
                 {
 
                     public boolean match(MetadataReader metadataReader,
                             MetadataReaderFactory metadataReaderFactory) throws IOException
                     {
                         return metadataReader.getAnnotationMetadata().hasAnnotation(
                                 Entity.class.getName());
                     }
                 });
 
                 // scan in org.example.package
                 Set<BeanDefinition> components = provider.findCandidateComponents(packageName
                         .replace(".", "/"));
 
                 for (BeanDefinition component : components)
                 {
                     try
                     {
                         Class<?> entityClass = Class.forName(component.getBeanClassName());
                         cfg.addAnnotatedClass(entityClass);
                     }
                     catch (ClassNotFoundException ex)
                     {
                         throw new RuntimeException(ex);
                     }
                 }
 
             }
         }
     }
 
     public List<String> getPackageNames()
     {
         return packageNames;
     }
 
     public void setPackageNames(List<String> packageNames)
     {
         this.packageNames = packageNames;
     }
 
 }
