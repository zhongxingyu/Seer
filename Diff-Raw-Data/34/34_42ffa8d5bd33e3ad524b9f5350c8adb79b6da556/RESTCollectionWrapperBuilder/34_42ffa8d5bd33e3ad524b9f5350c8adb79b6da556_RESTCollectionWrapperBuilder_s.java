 package org.jboss.pressgang.ccms.wrapper.collection;
 
 import java.util.Collection;
 
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTBlobConstantCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTCategoryCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTFileCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTImageCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTLanguageFileCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTLanguageImageCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTServerUndefinedEntityCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTServerUndefinedSettingCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTStringConstantCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicSourceUrlCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicStringCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTUserCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedCSNodeStringCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.join.RESTCSRelatedNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTCategoryInTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTPropertyTagInPropertyCategoryCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTTagInCategoryCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTCategoryV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTFileV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTImageV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTPropertyCategoryV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedCSNodeV1;
 import org.jboss.pressgang.ccms.utils.RESTWrapperCache;
 import org.jboss.pressgang.ccms.utils.RESTWrapperKey;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInTagWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.base.BaseWrapper;
 
 public class RESTCollectionWrapperBuilder<T extends BaseWrapper<T>> {
     private static final RESTWrapperCache wrapperCache = new RESTWrapperCache();
     private static final String GENERIC_ERROR = "Failed to create a Collection Wrapper instance as there is no wrapper available for the " +
             "Collection.";
 
     private RESTProviderFactory providerFactory;
     private RESTBaseCollectionV1<?, ?> collection;
     private boolean isRevisionCollection = false;
     private Class<T> entityWrapperInterface;
     private RESTBaseEntityV1<?, ?, ?> parent;
     private Collection<String> expandedEntityMethods;
 
     public static <T extends BaseWrapper<T>> RESTCollectionWrapperBuilder<T> newBuilder() {
         return new RESTCollectionWrapperBuilder<T>();
     }
 
     protected RESTCollectionWrapperBuilder() {
     }
 
     public RESTCollectionWrapperBuilder<T> providerFactory(final RESTProviderFactory providerFactory) {
         this.providerFactory = providerFactory;
         return this;
     }
 
     public <V extends RESTBaseCollectionV1<?, ?>> RESTCollectionWrapperBuilder<T> collection(final V collection) {
         this.collection = collection;
         return this;
     }
 
     public RESTCollectionWrapperBuilder<T> isRevisionCollection() {
         isRevisionCollection = true;
         return this;
     }
 
     public RESTCollectionWrapperBuilder<T> isRevisionCollection(boolean isRevisionCollection) {
         this.isRevisionCollection = isRevisionCollection;
         return this;
     }
 
     public RESTCollectionWrapperBuilder<T> entityWrapperInterface(final Class<T> wrapperClass) {
         this.entityWrapperInterface = wrapperClass;
         return this;
     }
 
     public RESTCollectionWrapperBuilder<T> parent(final RESTBaseEntityV1<?, ?, ?> parent) {
         this.parent = parent;
         return this;
     }
 
     public RESTCollectionWrapperBuilder<T> expandedEntityMethods(final Collection<String> methodNames) {
         expandedEntityMethods = methodNames;
         return this;
     }
 
     public CollectionWrapper<T> build() {
         if (collection == null) {
             return null;
         }
 
         // Make sure we have a provider factory
         if (providerFactory == null) {
             throw new IllegalStateException("The Provider Factory has not been registered.");
         }
 
         // Create the key
         final RESTWrapperKey key = new RESTWrapperKey(collection);
 
         // Check to see if a wrapper has already been cached for the key
         final RESTCollectionWrapper cachedWrapper = wrapperCache.getCollection(key);
         if (cachedWrapper != null) {
             return cachedWrapper;
         }
 
         final RESTCollectionWrapper wrapper;
 
         if (collection instanceof RESTServerUndefinedSettingCollectionV1) {
             // UNDEFINED SERVER SETTING
             wrapper = new RESTServerUndefinedSettingCollectionV1Wrapper(providerFactory,
                     (RESTServerUndefinedSettingCollectionV1) collection);
         } else if (collection instanceof RESTServerUndefinedEntityCollectionV1) {
             // UNDEFINED SERVER ENTITY
             wrapper = new RESTServerUndefinedEntityCollectionV1Wrapper(providerFactory, (RESTServerUndefinedEntityCollectionV1) collection);
         } else if (collection instanceof RESTTopicCollectionV1) {
             // TOPIC
             wrapper = new RESTTopicCollectionV1Wrapper(providerFactory, (RESTTopicCollectionV1) collection, isRevisionCollection,
                     expandedEntityMethods);
         } else if (collection instanceof RESTTopicSourceUrlCollectionV1) {
             // TOPIC SOURCE URL
             wrapper = getTopicSourceUrlCollectionWrapper();
         } else if (collection instanceof RESTTranslatedTopicCollectionV1) {
             // TRANSLATED TOPIC
             wrapper = new RESTTranslatedTopicCollectionV1Wrapper(providerFactory, (RESTTranslatedTopicCollectionV1) collection,
                     isRevisionCollection, expandedEntityMethods);
         } else if (collection instanceof RESTTranslatedTopicStringCollectionV1) {
             // TRANSLATED TOPIC STRING
             wrapper = getTranslatedTopicStringCollectionWrapper();
         } else if (collection instanceof RESTTagCollectionV1) {
             // TAG
             wrapper = new RESTTagCollectionV1Wrapper(providerFactory, (RESTTagCollectionV1) collection, isRevisionCollection,
                     expandedEntityMethods);
         } else if (collection instanceof RESTTagInCategoryCollectionV1) {
             // TAG TO CATEGORY
             wrapper = getTagInCategoryCollectionWrapper();
         } else if (collection instanceof RESTCategoryCollectionV1) {
             // CATEGORY
             wrapper = new RESTCategoryCollectionV1Wrapper(providerFactory, (RESTCategoryCollectionV1) collection, isRevisionCollection,
                     expandedEntityMethods);
         } else if (collection instanceof RESTCategoryInTagCollectionV1) {
             // CATEGORY TO TAG
             wrapper = getCategoryInTagCollectionWrapper();
         } else if (collection instanceof RESTPropertyTagInPropertyCategoryCollectionV1) {
             // PROPERTY TAGS
             wrapper = getPropertyTagInPropertyCategoryCollectionWrapper();
         } else if (collection instanceof RESTPropertyTagCollectionV1) {
             wrapper = new RESTPropertyTagCollectionV1Wrapper(providerFactory, (RESTPropertyTagCollectionV1) collection,
                     isRevisionCollection, expandedEntityMethods);
         } else if (collection instanceof RESTAssignedPropertyTagCollectionV1) {
             // ASSIGNED PROPERTY TAGS
             wrapper = getPropertyTagCollectionWrapper();
         } else if (collection instanceof RESTBlobConstantCollectionV1) {
             // BLOB CONSTANT
             wrapper = new RESTBlobConstantCollectionV1Wrapper(providerFactory, (RESTBlobConstantCollectionV1) collection,
                     isRevisionCollection, expandedEntityMethods);
         } else if (collection instanceof RESTStringConstantCollectionV1) {
             // STRING CONSTANT
             wrapper = new RESTStringConstantCollectionV1Wrapper(providerFactory, (RESTStringConstantCollectionV1) collection,
                     isRevisionCollection, expandedEntityMethods);
         } else if (collection instanceof RESTFileCollectionV1) {
             // FILE
             wrapper = new RESTFileCollectionV1Wrapper(providerFactory, (RESTFileCollectionV1) collection, isRevisionCollection,
                     expandedEntityMethods);
         } else if (collection instanceof RESTLanguageFileCollectionV1) {
             // LANGUAGE FILE
             wrapper = getLanguageFileCollectionWrapper();
         } else if (collection instanceof RESTImageCollectionV1) {
             // IMAGE
             wrapper = new RESTImageCollectionV1Wrapper(providerFactory, (RESTImageCollectionV1) collection, isRevisionCollection,
                     expandedEntityMethods);
         } else if (collection instanceof RESTLanguageImageCollectionV1) {
             // LANGUAGE IMAGE
             wrapper = getLanguageImageCollectionWrapper();
         } else if (collection instanceof RESTUserCollectionV1) {
             // USER
             wrapper = new RESTUserCollectionV1Wrapper(providerFactory, (RESTUserCollectionV1) collection, isRevisionCollection,
                     expandedEntityMethods);
         } else if (collection instanceof RESTContentSpecCollectionV1) {
             // CONTENT SPEC
             wrapper = new RESTContentSpecCollectionV1Wrapper(providerFactory, (RESTContentSpecCollectionV1) collection,
                     isRevisionCollection, expandedEntityMethods);
         } else if (collection instanceof RESTCSNodeCollectionV1) {
             // CONTENT SPEC NODE
             wrapper = new RESTCSNodeCollectionV1Wrapper(providerFactory, (RESTCSNodeCollectionV1) collection, isRevisionCollection);
         } else if (collection instanceof RESTCSRelatedNodeCollectionV1) {
             wrapper = new RESTCSRelatedNodeCollectionV1Wrapper(providerFactory, (RESTCSRelatedNodeCollectionV1) collection,
                     isRevisionCollection, expandedEntityMethods);
         } else if (collection instanceof RESTTranslatedContentSpecCollectionV1) {
             // TRANSLATED CONTENT SPEC
             wrapper = new RESTTranslatedContentSpecCollectionV1Wrapper(providerFactory, (RESTTranslatedContentSpecCollectionV1) collection,
                     isRevisionCollection, expandedEntityMethods);
         } else if (collection instanceof RESTTranslatedCSNodeCollectionV1) {
             // CONTENT SPEC TRANSLATED NODE
             wrapper = new RESTTranslatedCSNodeCollectionV1Wrapper(providerFactory, (RESTTranslatedCSNodeCollectionV1) collection,
                     isRevisionCollection, expandedEntityMethods);
         } else if (collection instanceof RESTTranslatedCSNodeStringCollectionV1) {
             // CONTENT SPEC TRANSLATED NODE STRING
             wrapper = getTranslatedCSNodeStringCollectionWrapper();
         } else {
             throw new IllegalArgumentException(GENERIC_ERROR);
         }
 
         wrapperCache.putCollection(key, wrapper);
 
         return wrapper;
     }
 
     protected RESTTopicSourceURLCollectionV1Wrapper getTopicSourceUrlCollectionWrapper() {
         if (parent == null) {
             throw new UnsupportedOperationException("A parent is needed to get Topic Source URLs using V1 of the REST Interface.");
         } else if (parent instanceof RESTBaseTopicV1) {
             return new RESTTopicSourceURLCollectionV1Wrapper(providerFactory, (RESTTopicSourceUrlCollectionV1) collection,
                     isRevisionCollection, (RESTBaseTopicV1<?, ?, ?>) parent, expandedEntityMethods);
         } else {
             throw new IllegalArgumentException(GENERIC_ERROR);
         }
     }
 
     protected RESTTranslatedTopicStringCollectionV1Wrapper getTranslatedTopicStringCollectionWrapper() {
         if (parent == null) {
             throw new UnsupportedOperationException("A parent is needed to get Translated Topic Strings using V1 of the REST Interface.");
         } else if (parent instanceof RESTTranslatedTopicV1) {
             return new RESTTranslatedTopicStringCollectionV1Wrapper(providerFactory, (RESTTranslatedTopicStringCollectionV1) collection,
                     isRevisionCollection, (RESTTranslatedTopicV1) parent, expandedEntityMethods);
         } else {
             throw new IllegalArgumentException(GENERIC_ERROR);
         }
     }
 
     protected RESTTranslatedCSNodeStringCollectionV1Wrapper getTranslatedCSNodeStringCollectionWrapper() {
         if (parent == null) {
             throw new UnsupportedOperationException("A parent is needed to get Translated CSNode Strings using V1 of the REST Interface.");
         } else if (parent instanceof RESTTranslatedCSNodeV1) {
             return new RESTTranslatedCSNodeStringCollectionV1Wrapper(providerFactory, (RESTTranslatedCSNodeStringCollectionV1) collection,
                     isRevisionCollection, (RESTTranslatedCSNodeV1) parent, expandedEntityMethods);
         } else {
             throw new IllegalArgumentException(GENERIC_ERROR);
         }
     }
 
     protected RESTLanguageFileCollectionV1Wrapper getLanguageFileCollectionWrapper() {
         if (parent == null) {
             throw new UnsupportedOperationException("A parent is needed to get Language Files using V1 of the REST Interface.");
         } else if (parent instanceof RESTFileV1) {
             return new RESTLanguageFileCollectionV1Wrapper(providerFactory, (RESTLanguageFileCollectionV1) collection, isRevisionCollection,
                     (RESTFileV1) parent, expandedEntityMethods);
         } else {
             throw new IllegalArgumentException(GENERIC_ERROR);
         }
     }
 
     protected RESTLanguageImageCollectionV1Wrapper getLanguageImageCollectionWrapper() {
         if (parent == null) {
             throw new UnsupportedOperationException("A parent is needed to get Language Images using V1 of the REST Interface.");
         } else if (parent instanceof RESTImageV1) {
             return new RESTLanguageImageCollectionV1Wrapper(providerFactory, (RESTLanguageImageCollectionV1) collection,
                     isRevisionCollection, (RESTImageV1) parent, expandedEntityMethods);
         } else {
             throw new IllegalArgumentException(GENERIC_ERROR);
         }
     }
 
     protected RESTCategoryInTagCollectionV1Wrapper getCategoryInTagCollectionWrapper() {
         if (parent == null) {
             throw new UnsupportedOperationException("A parent is needed to get CategoryInTags using V1 of the REST Interface.");
         } else if (parent instanceof RESTTagV1) {
             return new RESTCategoryInTagCollectionV1Wrapper(providerFactory, (RESTCategoryInTagCollectionV1) collection,
                     isRevisionCollection, (RESTTagV1) parent, expandedEntityMethods);
         } else {
             throw new IllegalArgumentException(GENERIC_ERROR);
         }
     }
 
     protected RESTTagInCategoryCollectionV1Wrapper getTagInCategoryCollectionWrapper() {
         if (parent == null) {
             throw new UnsupportedOperationException("A parent is needed to get TagInCategories using V1 of the REST Interface.");
         } else if (parent instanceof RESTCategoryV1) {
             return new RESTTagInCategoryCollectionV1Wrapper(providerFactory, (RESTTagInCategoryCollectionV1) collection,
                     isRevisionCollection, (RESTCategoryV1) parent, expandedEntityMethods);
         } else {
             throw new IllegalArgumentException(GENERIC_ERROR);
         }
     }
 
     protected RESTPropertyTagInPropertyCategoryCollectionV1Wrapper getPropertyTagInPropertyCategoryCollectionWrapper() {
         if (parent == null) {
             throw new UnsupportedOperationException(
                     "A parent is needed to get PropertyTagInPropertyCategories using V1 of the REST Interface.");
         } else if (parent instanceof RESTPropertyCategoryV1) {
             return new RESTPropertyTagInPropertyCategoryCollectionV1Wrapper(providerFactory,
                     (RESTPropertyTagInPropertyCategoryCollectionV1) collection, isRevisionCollection, (RESTPropertyCategoryV1) parent,
                     expandedEntityMethods);
         } else {
             throw new IllegalArgumentException(GENERIC_ERROR);
         }
     }
 
     protected RESTCollectionWrapper<?, ?, ?> getPropertyTagCollectionWrapper() {
         if (entityWrapperInterface == null) {
             throw new UnsupportedOperationException("The wrapper return class needs to be specified as the type can't be determined.");
         } else if (entityWrapperInterface == PropertyTagInTopicWrapper.class) {
             return new RESTPropertyTagInTopicCollectionV1Wrapper(providerFactory, (RESTAssignedPropertyTagCollectionV1) collection,
                     isRevisionCollection, (RESTBaseTopicV1<?, ?, ?>) parent, expandedEntityMethods);
         } else if (entityWrapperInterface == PropertyTagInTagWrapper.class) {
             return new RESTPropertyTagInTagCollectionV1Wrapper(providerFactory, (RESTAssignedPropertyTagCollectionV1) collection,
                     isRevisionCollection, (RESTBaseTagV1<?, ?, ?>) parent, expandedEntityMethods);
         } else if (entityWrapperInterface == PropertyTagInContentSpecWrapper.class) {
             return new RESTPropertyTagInContentSpecCollectionV1Wrapper(providerFactory,
                     (RESTAssignedPropertyTagCollectionV1) collection, isRevisionCollection, (RESTContentSpecV1) parent,
                     expandedEntityMethods);
         } else {
             throw new IllegalArgumentException(GENERIC_ERROR);
         }
     }
 }
