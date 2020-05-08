 package org.jboss.pressgang.ccms.proxy;
 
 import java.lang.reflect.Method;
 
 import javassist.util.proxy.MethodFilter;
 import javassist.util.proxy.ProxyFactory;
 import javassist.util.proxy.ProxyObject;
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTBlobConstantV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTCategoryV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTImageV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTLanguageImageV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTPropertyCategoryV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTStringConstantV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicSourceUrlV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicStringV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTUserV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseCategoryV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedCSNodeStringV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.join.RESTCSRelatedNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTCategoryInTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTPropertyTagInPropertyCategoryV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTTagInCategoryV1;
 
 public class RESTEntityProxyFactory {
     @SuppressWarnings("unchecked")
     public static <T extends RESTBaseEntityV1<T, ?, ?>> T createProxy(final RESTProviderFactory providerFactory, final T entity,
             boolean isRevision) {
         return createProxy(providerFactory, entity, isRevision, null);
     }
 
     @SuppressWarnings("unchecked")
     public static <T extends RESTBaseEntityV1<T, ?, ?>> T createProxy(final RESTProviderFactory providerFactory, final T entity,
             boolean isRevision, final RESTBaseEntityV1<?, ?, ?> parent) {
         final Class<?> clazz = entity.getClass();
 
         final ProxyFactory factory = new ProxyFactory();
         factory.setFilter(new MethodFilter() {
             @Override
             public boolean isHandled(Method method) {
                 return method.getName().startsWith("get");
             }
         });
         factory.setSuperclass(clazz);
 
         Class<?> cl = factory.createClass();
         T proxy = null;
         try {
             proxy = (T) cl.newInstance();
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
 
         final RESTBaseEntityV1ProxyHandler<T> proxyHandler = findProxyHandler(providerFactory, entity, isRevision, parent);
         ((ProxyObject) proxy).setHandler(proxyHandler);
         proxyHandler.setProxyEntity(proxy);
 
         return proxy;
     }
 
     private static <T extends RESTBaseEntityV1<?, ?, ?>> RESTBaseEntityV1ProxyHandler findProxyHandler(
             final RESTProviderFactory providerFactory, final T entity, boolean isRevision, final RESTBaseEntityV1<?, ?, ?> parent) {
 
         if (entity instanceof RESTTopicV1) {
             // TOPIC
             return new RESTTopicV1ProxyHandler(providerFactory, (RESTTopicV1) entity, isRevision);
         } else if (entity instanceof RESTTopicSourceUrlV1) {
             // TOPIC SOURCE URL
             return new RESTTopicSourceUrlV1ProxyHandler(providerFactory, (RESTTopicSourceUrlV1) entity, isRevision,
                     (RESTBaseTopicV1<?, ?, ?>) parent);
         } else if (entity instanceof RESTTranslatedTopicV1) {
             // TRANSLATED TOPIC
             return new RESTTranslatedTopicV1ProxyHandler(providerFactory, (RESTTranslatedTopicV1) entity, isRevision);
         } else if (entity instanceof RESTTranslatedTopicStringV1) {
             // TRANSLATED TOPIC STRING
             return new RESTTranslatedTopicStringV1ProxyHandler(providerFactory, (RESTTranslatedTopicStringV1) entity, isRevision,
                     (RESTTranslatedTopicV1) parent);
         } else if (entity instanceof RESTAssignedPropertyTagV1) {
             // PROPERTY TAGS
             return new RESTAssignedPropertyTagV1ProxyHandler(providerFactory, (RESTAssignedPropertyTagV1) entity, isRevision, parent);
         } else if (entity instanceof RESTPropertyTagV1) {
             return new RESTPropertyTagV1ProxyHandler(providerFactory, (RESTPropertyTagV1) entity, isRevision);
         } else if (entity instanceof RESTPropertyTagInPropertyCategoryV1) {
             return new RESTPropertyTagInPropertyCategoryV1ProxyHandler(providerFactory, (RESTPropertyTagInPropertyCategoryV1) entity,
                     isRevision, (RESTPropertyCategoryV1) parent);
         } else if (entity instanceof RESTBlobConstantV1) {
             // BLOB CONSTANT
             return new RESTBlobConstantV1ProxyHandler(providerFactory, (RESTBlobConstantV1) entity, isRevision);
         } else if (entity instanceof RESTCategoryV1) {
             // CATEGORIES
             return new RESTCategoryV1ProxyHandler<RESTCategoryV1>(providerFactory, (RESTCategoryV1) entity, isRevision);
         } else if (entity instanceof RESTCategoryInTagV1) {
             return new RESTCategoryInTagV1ProxyHandler<RESTCategoryInTagV1>(providerFactory, (RESTCategoryInTagV1) entity, isRevision,
                     (RESTBaseTagV1<?, ?, ?>) parent);
         } else if (entity instanceof RESTImageV1) {
             // IMAGE
             return new RESTImageV1ProxyHandler(providerFactory, (RESTImageV1) entity, isRevision);
         } else if (entity instanceof RESTLanguageImageV1) {
             // LANGUAGE IMAGE
             return new RESTLanguageImageV1ProxyHandler(providerFactory, (RESTLanguageImageV1) entity, isRevision, (RESTImageV1) parent);
         } else if (entity instanceof RESTUserV1) {
             // USER
             return new RESTUserV1ProxyHandler(providerFactory, (RESTUserV1) entity, isRevision);
         } else if (entity instanceof RESTTagV1) {
             // TAGS
             return new RESTTagV1ProxyHandler(providerFactory, (RESTTagV1) entity, isRevision);
         } else if (entity instanceof RESTTagInCategoryV1) {
             return new RESTTagInCategoryV1ProxyHandler(providerFactory, (RESTTagInCategoryV1) entity, isRevision,
                     (RESTBaseCategoryV1<?, ?, ?>) parent);
         } else if (entity instanceof RESTStringConstantV1) {
             // STRING CONSTANT
             return new RESTStringConstantV1ProxyHandler(providerFactory, (RESTStringConstantV1) entity, isRevision);
         } else if (entity instanceof RESTContentSpecV1) {
             // CONTENT SPEC
             return new RESTContentSpecV1ProxyHandler(providerFactory, (RESTContentSpecV1) entity, isRevision);
         } else if (entity instanceof RESTCSNodeV1) {
             // CONTENT SPEC NODE
             return new RESTCSNodeV1ProxyHandler(providerFactory, (RESTCSNodeV1) entity, isRevision);
         } else if (entity instanceof RESTCSRelatedNodeV1) {
             return new RESTCSRelatedNodeV1ProxyHandler(providerFactory, (RESTCSRelatedNodeV1) entity, isRevision, (RESTCSNodeV1) parent);
         } else if (entity instanceof RESTTranslatedContentSpecV1) {
             // TRANSLATED CONTENT SPEC
             return new RESTTranslatedContentSpecV1ProxyHandler(providerFactory, (RESTTranslatedContentSpecV1) entity, isRevision);
         } else if (entity instanceof RESTTranslatedCSNodeV1) {
             // TRANSLATED CONTENT SPEC NODE
             return new RESTTranslatedCSNodeV1ProxyHandler(providerFactory, (RESTTranslatedCSNodeV1) entity, isRevision);
        } else if (entity instanceof RESTCSNodeV1) {
             // TRANSLATED CONTENT SPEC NODE STRING
             return new RESTTranslatedCSNodeStringV1ProxyHandler(providerFactory, (RESTTranslatedCSNodeStringV1) entity, isRevision,
                     (RESTTranslatedCSNodeV1) parent);
         }
 
         throw new IllegalArgumentException("Unable to find a proxy handler for the specified entity.");
     }
 }
