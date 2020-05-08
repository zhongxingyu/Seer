 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.jpa;
 
 import java.lang.annotation.Annotation;
 import java.util.Properties;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Key;
 import com.google.inject.Singleton;
 import com.google.inject.name.Names;
 
 import de.cosmocode.palava.core.inject.AbstractRebindModule;
 import de.cosmocode.palava.core.inject.Config;
 import de.cosmocode.palava.scope.UnitOfWork;
 
 /**
  * A variation of the {@link JpaModule} which allows binding using an annotation.
  *
  * @since 3.2
  * @author Willi Schoenborn
  */
 public final class AnnotatedRebindingJpaModule extends AbstractRebindModule {
 
     private final Class<? extends Annotation> annotation;
     private final Config config;
     
     public AnnotatedRebindingJpaModule(Class<? extends Annotation> annotation, String prefix) {
         this.annotation = Preconditions.checkNotNull(annotation, "Annotation");
         this.config = new Config(prefix);
     }
 
     @Override
     protected void configuration() {
         bind(String.class).annotatedWith(Names.named(PersistenceConfig.UNIT_NAME)).to(
             Key.get(String.class, Names.named(config.prefixed(PersistenceConfig.UNIT_NAME))));
     }
 
     @Override
     protected void optionals() {
         bind(Properties.class).annotatedWith(Names.named(PersistenceConfig.PROPERTIES)).to(
             Key.get(Properties.class, Names.named(config.prefixed(PersistenceConfig.PROPERTIES))));
     }
 
     @Override
     protected void bindings() {
         bind(PersistenceService.class).annotatedWith(annotation).
             to(DefaultPersistenceService.class).in(Singleton.class);
         bind(EntityManagerFactory.class).annotatedWith(annotation).
            to(Key.get(PersistenceService.class, annotation)).in(Singleton.class);
         bind(EntityManager.class).annotatedWith(annotation).
             toProvider(Key.get(PersistenceService.class, annotation)).in(UnitOfWork.class);
     }
 
     @Override
     protected void expose() {
         expose(PersistenceService.class).annotatedWith(annotation);
         expose(EntityManagerFactory.class).annotatedWith(annotation);
         expose(EntityManager.class).annotatedWith(annotation);
     }
 
 }
