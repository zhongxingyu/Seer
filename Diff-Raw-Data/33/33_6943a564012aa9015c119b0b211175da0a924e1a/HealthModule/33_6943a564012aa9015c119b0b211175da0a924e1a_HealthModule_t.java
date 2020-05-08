 package com.github.drashid.status.health;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.inject.Singleton;
 
 import org.reflections.Reflections;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.google.inject.AbstractModule;
 import com.google.inject.Injector;
 import com.google.inject.Provides;
 import com.yammer.metrics.core.HealthCheck;
 
 public class HealthModule extends AbstractModule {
 
   private static final Logger LOG = LoggerFactory.getLogger(HealthModule.class);
   
   private static final String HEALTH_CHECK_PACKAGE = "com.github.drashid.status.health";
 
   @Override
   protected void configure() {
   }
 
   @Provides
   @Singleton
   List<HealthCheck> getChecks(Injector inject) {
     Reflections refs = new Reflections(HEALTH_CHECK_PACKAGE);
     Set<Class<? extends HealthCheck>> clsz = refs.getSubTypesOf(HealthCheck.class);
 
     List<HealthCheck> checks = Lists.newArrayList();
     for (Class<? extends HealthCheck> cls : clsz) {
      LOG.info("Creating Health Check {}", cls.getSimpleName());
       checks.add(inject.getInstance(cls));
     }
     return checks;
   }
 }
