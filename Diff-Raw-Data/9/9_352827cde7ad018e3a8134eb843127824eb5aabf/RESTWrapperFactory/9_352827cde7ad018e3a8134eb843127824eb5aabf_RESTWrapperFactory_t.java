 package org.jboss.pressgang.ccms.wrapper;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javassist.util.proxy.MethodHandler;
 import javassist.util.proxy.ProxyObject;
 import org.jboss.pressgang.ccms.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.proxy.RESTBaseEntityV1ProxyHandler;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTBlobConstantCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTCategoryCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTImageCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTLanguageImageCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTStringConstantCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicSourceUrlCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicStringCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTUserCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedCSNodeStringCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.join.RESTCSRelatedNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTCategoryInTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTPropertyTagInPropertyCategoryCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTTagInCategoryCollectionV1;
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
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedCSNodeStringV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.join.RESTCSRelatedNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTCategoryInTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTPropertyTagInPropertyCategoryV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTTagInCategoryV1;
 import org.jboss.pressgang.ccms.wrapper.base.EntityWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTBlobConstantCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTCSNodeCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTCSRelatedNodeCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTranslatedCSNodeCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTranslatedCSNodeStringCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTCategoryCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTCategoryInTagCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTContentSpecCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTImageCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTLanguageImageCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTPropertyTagCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTPropertyTagInContentSpecCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTPropertyTagInPropertyCategoryCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTPropertyTagInTagCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTPropertyTagInTopicCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTStringConstantCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTagCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTagInCategoryCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTopicCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTopicSourceURLCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTranslatedContentSpecCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTranslatedTopicCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTranslatedTopicStringCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTUserCollectionV1Wrapper;
 
 public class RESTWrapperFactory extends WrapperFactory {
 
     public RESTWrapperFactory(final DataProviderFactory providerFactory) {
         super(providerFactory);
     }
 
     @Override
     protected RESTProviderFactory getProviderFactory() {
         return (RESTProviderFactory) super.getProviderFactory();
     }
 
     @Override
     @SuppressWarnings({"unchecked", "rawtypes"})
     public <T extends EntityWrapper<T>> T create(final Object entity, boolean isRevision) {
         if (entity == null) {
             return null;
         }
 
         final Object unwrappedEntity = getEntity(entity);
         final EntityWrapper wrapper;
 
         if (entity instanceof RESTTopicV1) {
             // TOPIC
             wrapper = new RESTTopicV1Wrapper(getProviderFactory(), (RESTTopicV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTTopicSourceUrlV1) {
             // TOPIC SOURCE URL
             throw new UnsupportedOperationException("A parent is needed to get Topic Source URLs using V1 of the REST Interface.");
         } else if (entity instanceof RESTTranslatedTopicV1) {
             // TRANSLATED TOPIC
             wrapper = new RESTTranslatedTopicV1Wrapper(getProviderFactory(), (RESTTranslatedTopicV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTTranslatedTopicStringV1) {
             // TRANSLATED TOPIC STRING
             throw new UnsupportedOperationException("A parent is needed to get Translated Topic Strings using V1 of the REST Interface.");
         } else if (entity instanceof RESTTagV1) {
             // TAG
             wrapper = new RESTTagV1Wrapper(getProviderFactory(), (RESTTagV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTTagInCategoryV1) {
             throw new UnsupportedOperationException("A parent is needed to get TagInCategories using V1 of the REST Interface.");
         } else if (entity instanceof RESTCategoryV1) {
             // CATEGORY
             wrapper = new RESTCategoryV1Wrapper(getProviderFactory(), (RESTCategoryV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTCategoryInTagV1) {
             throw new UnsupportedOperationException("A parent is needed to get CategoryInTags using V1 of the REST Interface.");
         } else if (entity instanceof RESTPropertyTagV1) {
             // PROPERTY TAGS
             wrapper = new RESTPropertyTagV1Wrapper(getProviderFactory(), (RESTPropertyTagV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTAssignedPropertyTagV1) {
             throw new UnsupportedOperationException("The wrapper return class needs to be specified as the type can't be determined.");
         } else if (entity instanceof RESTPropertyTagInPropertyCategoryV1) {
             throw new UnsupportedOperationException(
                     "A parent is needed to get PropertyTagInPropertyCategories using V1 of the REST Interface.");
         } else if (entity instanceof RESTBlobConstantV1) {
             // BLOB CONSTANT
             wrapper = new RESTBlobConstantV1Wrapper(getProviderFactory(), (RESTBlobConstantV1) entity, isRevision);
         } else if (entity instanceof RESTStringConstantV1) {
             // STRING CONSTANT
             wrapper = new RESTStringConstantV1Wrapper(getProviderFactory(), (RESTStringConstantV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTImageV1) {
             // IMAGE
             wrapper = new RESTImageV1Wrapper(getProviderFactory(), (RESTImageV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTLanguageImageV1) {
             // LANGUAGE IMAGE
             throw new UnsupportedOperationException("A parent is needed to get Language Images using V1 of the REST Interface.");
         } else if (entity instanceof RESTUserV1) {
             // USER
             wrapper = new RESTUserV1Wrapper(getProviderFactory(), (RESTUserV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTContentSpecV1) {
             // CONTENT SPEC
             wrapper = new RESTContentSpecV1Wrapper(getProviderFactory(), (RESTContentSpecV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTCSNodeV1) {
             // CONTENT SPEC NODE
             wrapper = new RESTCSNodeV1Wrapper(getProviderFactory(), (RESTCSNodeV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTCSRelatedNodeV1) {
             wrapper = new RESTCSRelatedNodeV1Wrapper(getProviderFactory(), (RESTCSRelatedNodeV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTTranslatedContentSpecV1) {
             // TRANSLATED CONTENT SPEC
             wrapper = new RESTTranslatedContentSpecV1Wrapper(getProviderFactory(), (RESTTranslatedContentSpecV1) unwrappedEntity,
                     isRevision);
         } else if (entity instanceof RESTTranslatedCSNodeV1) {
             // CONTENT SPEC TRANSLATED NODE
             wrapper = new RESTTranslatedCSNodeV1Wrapper(getProviderFactory(), (RESTTranslatedCSNodeV1) unwrappedEntity, isRevision);
         } else if (entity instanceof RESTTranslatedCSNodeStringV1) {
             // CONTENT SPEC TRANSLATED NODE STRING
             throw new UnsupportedOperationException("A parent is needed to get CSTranslatedNodeStrings using V1 of the REST Interface.");
         } else {
             throw new IllegalArgumentException("Failed to create a Wrapper instance as there is no wrapper available for the Entity.");
         }
 
         return (T) wrapper;
     }
 
     @SuppressWarnings({"unchecked", "rawtypes"})
     @Override
     public <T extends EntityWrapper<T>> CollectionWrapper<T> createCollection(final Object collection, final Class<?> entityClass,
             boolean isRevisionCollection) {
         if (collection == null) {
             return null;
         }
 
         final CollectionWrapper wrapper;
 
         if (collection instanceof RESTTopicCollectionV1) {
             // TOPIC
             wrapper = new RESTTopicCollectionV1Wrapper(getProviderFactory(), (RESTTopicCollectionV1) collection, isRevisionCollection);
         } else if (collection instanceof RESTTopicSourceUrlCollectionV1) {
             // TOPIC SOURCE URL
             throw new UnsupportedOperationException("A parent is needed to get Topic Source URLs using V1 of the REST Interface.");
         } else if (collection instanceof RESTTranslatedTopicCollectionV1) {
             // TRANSLATED TOPIC
             wrapper = new RESTTranslatedTopicCollectionV1Wrapper(getProviderFactory(), (RESTTranslatedTopicCollectionV1) collection,
                     isRevisionCollection);
         } else if (collection instanceof RESTTranslatedTopicStringCollectionV1) {
             // TRANSLATED TOPIC STRING
             throw new UnsupportedOperationException("A parent is needed to get Translated Topic Strings using V1 of the REST Interface.");
         } else if (collection instanceof RESTTagCollectionV1) {
             // TAG
             wrapper = new RESTTagCollectionV1Wrapper(getProviderFactory(), (RESTTagCollectionV1) collection, isRevisionCollection);
         } else if (collection instanceof RESTTagInCategoryCollectionV1) {
             throw new UnsupportedOperationException("A parent is needed to get TagInCategories using V1 of the REST Interface.");
         } else if (collection instanceof RESTCategoryCollectionV1) {
             // CATEGORY
             wrapper = new RESTCategoryCollectionV1Wrapper(getProviderFactory(), (RESTCategoryCollectionV1) collection,
                     isRevisionCollection);
         } else if (collection instanceof RESTCategoryInTagCollectionV1) {
             throw new UnsupportedOperationException("A parent is needed to get CategoryInTags using V1 of the REST Interface.");
         } else if (collection instanceof RESTPropertyTagInPropertyCategoryCollectionV1) {
             // PROPERTY TAGS
             throw new UnsupportedOperationException(
                     "A parent is needed to get PropertyTagInPropertyCategories using V1 of the REST Interface.");
         } else if (collection instanceof RESTPropertyTagCollectionV1) {
             wrapper = new RESTPropertyTagCollectionV1Wrapper(getProviderFactory(), (RESTPropertyTagCollectionV1) collection,
                     isRevisionCollection);
         } else if (collection instanceof RESTAssignedPropertyTagCollectionV1) {
             throw new UnsupportedOperationException("The wrapper return class needs to be specified as the type can't be determined.");
         } else if (collection instanceof RESTBlobConstantCollectionV1) {
             // BLOB CONSTANT
             wrapper = new RESTBlobConstantCollectionV1Wrapper(getProviderFactory(), (RESTBlobConstantCollectionV1) collection,
                     isRevisionCollection);
         } else if (collection instanceof RESTStringConstantCollectionV1) {
             // STRING CONSTANT
             wrapper = new RESTStringConstantCollectionV1Wrapper(getProviderFactory(), (RESTStringConstantCollectionV1) collection,
                     isRevisionCollection);
         } else if (collection instanceof RESTImageCollectionV1) {
             // IMAGE
             wrapper = new RESTImageCollectionV1Wrapper(getProviderFactory(), (RESTImageCollectionV1) collection, isRevisionCollection);
         } else if (collection instanceof RESTLanguageImageCollectionV1) {
             // LANGUAGE IMAGE
             throw new UnsupportedOperationException("A parent is needed to get Language Images using V1 of the REST Interface.");
         } else if (collection instanceof RESTUserCollectionV1) {
             // USER
             wrapper = new RESTUserCollectionV1Wrapper(getProviderFactory(), (RESTUserCollectionV1) collection, isRevisionCollection);
         } else if (collection instanceof RESTContentSpecCollectionV1) {
             // CONTENT SPEC
             wrapper = new RESTContentSpecCollectionV1Wrapper(getProviderFactory(), (RESTContentSpecCollectionV1) collection,
                     isRevisionCollection);
         } else if (collection instanceof RESTCSNodeCollectionV1) {
             // CONTENT SPEC NODE
             wrapper = new RESTCSNodeCollectionV1Wrapper(getProviderFactory(), (RESTCSNodeCollectionV1) collection, isRevisionCollection);
         } else if (collection instanceof RESTCSRelatedNodeCollectionV1) {
             wrapper = new RESTCSRelatedNodeCollectionV1Wrapper(getProviderFactory(), (RESTCSRelatedNodeCollectionV1) collection,
                     isRevisionCollection);
         } else if (collection instanceof RESTTranslatedContentSpecCollectionV1) {
             // TRANSLATED CONTENT SPEC
             wrapper = new RESTTranslatedContentSpecCollectionV1Wrapper(getProviderFactory(),
                     (RESTTranslatedContentSpecCollectionV1) collection, isRevisionCollection);
         } else if (collection instanceof RESTTranslatedCSNodeCollectionV1) {
             // CONTENT SPEC TRANSLATED NODE
             wrapper = new RESTTranslatedCSNodeCollectionV1Wrapper(getProviderFactory(), (RESTTranslatedCSNodeCollectionV1) collection,
                     isRevisionCollection);
         } else if (collection instanceof RESTTranslatedCSNodeStringCollectionV1) {
             // CONTENT SPEC TRANSLATED NODE STRING
             throw new UnsupportedOperationException("A parent is needed to get Translated Node Strings using V1 of the REST Interface.");
         } else {
             throw new IllegalArgumentException(
                     "Failed to create a Collection Wrapper instance as there is no wrapper available for the Collection.");
         }
 
         return wrapper;
     }
 
     /**
      * Create a list of wrapped entities from a REST Collection.
      *
      * @param entities       The collection of entities to wrap.
      * @param isRevisionList Whether or not the collection is a collection of revision entities.
      * @param <T>            The wrapper class that is returned.
      * @param <U>            The class of the collection entities.
      * @param <V>            The class of the collection.
      * @return An ArrayList of wrapped entities.
      */
     public <T extends EntityWrapper<T>, U extends RESTBaseEntityV1<U, V, ?>, V extends RESTBaseCollectionV1<U, V, ?>> List<T> createList(
             final V entities, boolean isRevisionList) {
         if (entities == null) {
             return null;
         }
 
         final List<U> entityList = entities.returnItems();
         if (entityList == null) {
             return null;
         } else {
             return createList(entityList, isRevisionList);
         }
     }
 
     /**
      * Create a list of wrapped entities from a REST Collection.
      *
      * @param entities       The collection of entities to wrap.
      * @param isRevisionList Whether or not the collection is a collection of revision entities.
      * @param parent         The parent entity that the list of entities comes from.
      * @param <T>            The wrapper class that is returned.
      * @param <U>            The class of the collection entities.
      * @param <V>            The class of the collection.
      * @return An ArrayList of wrapped entities.
      */
     @SuppressWarnings("unchecked")
     public <T extends EntityWrapper<T>, U extends RESTBaseEntityV1<U, V, ?>, V extends RESTBaseCollectionV1<U, V, ?>> List<T> createList(
             final V entities, boolean isRevisionList, final RESTBaseEntityV1<?, ?, ?> parent) {
         if (entities == null) {
             return null;
         }
 
         final List<U> entityList = entities.returnItems();
         if (entityList == null) {
             return null;
         } else {
             final List<T> retValue = new ArrayList<T>();
             for (final U object : entityList) {
                 retValue.add((T) create(object, isRevisionList, parent));
             }
 
             return retValue;
         }
     }
 
     /**
      * Create a wrapper around a specific entity.
      *
      * @param entity     The entity to be wrapped.
      * @param isRevision Whether the entity is a revision or not.
      * @param parent     The parent entity that the entity comes from.
      * @param <T>        The wrapper class that is returned.
      * @return The Wrapper around the entity.
      */
     @SuppressWarnings({"unchecked", "rawtypes"})
     public <T extends EntityWrapper<T>> T create(final Object entity, boolean isRevision, final RESTBaseEntityV1<?, ?, ?> parent) {
         if (entity == null) {
             return null;
         }
 
         final Object unwrappedEntity = getEntity(entity);
         final EntityWrapper wrapper;
 
         if (entity instanceof RESTTopicSourceUrlV1 && parent instanceof RESTBaseTopicV1) {
             // TOPIC SOURCE URL
             wrapper = new RESTTopicSourceURLV1Wrapper(getProviderFactory(), (RESTTopicSourceUrlV1) unwrappedEntity, isRevision,
                     (RESTBaseTopicV1<?, ?, ?>) parent);
         } else if (entity instanceof RESTTranslatedTopicStringV1 && parent instanceof RESTTranslatedTopicV1) {
             // TRANSLATED TOPIC STRING
             wrapper = new RESTTranslatedTopicStringV1Wrapper(getProviderFactory(), (RESTTranslatedTopicStringV1) unwrappedEntity,
                     isRevision, (RESTTranslatedTopicV1) parent);
         } else if (entity instanceof RESTLanguageImageV1 && parent instanceof RESTImageV1) {
             // LANGUAGE IMAGE
             wrapper = new RESTLanguageImageV1Wrapper(getProviderFactory(), (RESTLanguageImageV1) unwrappedEntity, isRevision,
                     (RESTImageV1) parent);
         } else if (entity instanceof RESTCategoryInTagV1 && parent instanceof RESTBaseTagV1) {
             // CATEGORY TO TAG
             wrapper = new RESTCategoryInTagV1Wrapper(getProviderFactory(), (RESTCategoryInTagV1) unwrappedEntity, isRevision,
                     (RESTBaseTagV1<?, ?, ?>) parent);
         } else if (entity instanceof RESTTagInCategoryV1 && parent instanceof RESTBaseCategoryV1) {
             // TAG TO CATEGORY
             wrapper = new RESTTagInCategoryV1Wrapper(getProviderFactory(), (RESTTagInCategoryV1) unwrappedEntity, isRevision,
                     (RESTBaseCategoryV1<?, ?, ?>) parent);
         } else if (entity instanceof RESTPropertyTagInPropertyCategoryV1 && parent instanceof RESTPropertyCategoryV1) {
             // PROPERTY TAG TO CATEGORY
             wrapper = new RESTPropertyTagInPropertyCategoryV1Wrapper(getProviderFactory(),
                     (RESTPropertyTagInPropertyCategoryV1) unwrappedEntity, isRevision, (RESTPropertyCategoryV1) parent);
         } else if (entity instanceof RESTTranslatedCSNodeStringV1 && parent instanceof RESTTranslatedCSNodeV1) {
             // CONTENT SPEC TRANSLATED NODE STRING
             wrapper = new RESTTranslatedCSNodeStringV1Wrapper(getProviderFactory(), (RESTTranslatedCSNodeStringV1) unwrappedEntity,
                     isRevision, (RESTTranslatedCSNodeV1) parent);
         } else {
             wrapper = create(unwrappedEntity, isRevision);
         }
 
         return (T) wrapper;
     }
 
     /**
      * Create a wrapper around a specific entity.
      *
      * @param entity       The entity to be wrapped.
      * @param isRevision   Whether the entity is a revision or not.
      * @param parent       The parent entity that the entity comes from.
      * @param wrapperClass The return wrapper class, incase the entities wrapper can't be determined by it's class.
      * @param <T>          The wrapper class that is returned.
      * @return The Wrapper around the entity.
      */
     @SuppressWarnings({"unchecked", "rawtypes"})
     public <T extends EntityWrapper<T>> T create(final Object entity, boolean isRevision, final RESTBaseEntityV1<?, ?, ?> parent,
             final Class<T> wrapperClass) {
         if (entity == null) {
             return null;
         }
 
         final Object unwrappedEntity = getEntity(entity);
         final EntityWrapper wrapper;
 
         if (wrapperClass == PropertyTagInTopicWrapper.class && entity instanceof RESTAssignedPropertyTagV1) {
             wrapper = new RESTPropertyTagInTopicV1Wrapper(getProviderFactory(), (RESTAssignedPropertyTagV1) unwrappedEntity, isRevision,
                     (RESTBaseTopicV1<?, ?, ?>) parent);
         } else if (wrapperClass == PropertyTagInTagWrapper.class && entity instanceof RESTAssignedPropertyTagV1) {
             wrapper = new RESTPropertyTagInTagV1Wrapper(getProviderFactory(), (RESTAssignedPropertyTagV1) unwrappedEntity, isRevision,
                     (RESTBaseTagV1<?, ?, ?>) parent);
         } else if (wrapperClass == PropertyTagInContentSpecWrapper.class && entity instanceof RESTAssignedPropertyTagV1) {
             wrapper = new RESTPropertyTagInContentSpecV1Wrapper(getProviderFactory(), (RESTAssignedPropertyTagV1) unwrappedEntity,
                     isRevision, (RESTContentSpecV1) parent);
         } else {
             wrapper = create(unwrappedEntity, isRevision);
         }
 
         return (T) wrapper;
     }
 
     /**
      * Create a wrapper around a collection of entities.
      *
      * @param collection           The collection to be wrapped.
      * @param entityClass          The class of the entity that the collection contains.
      * @param isRevisionCollection Whether or not the collection is a collection of revision entities.
      * @param parent               The parent entity that the collection comes from.
      * @param <T>                  The wrapper class that is returned.
      * @return The Wrapper around the collection of entities.
      */
     @SuppressWarnings({"unchecked", "rawtypes"})
     public <T extends EntityWrapper<T>> CollectionWrapper<T> createCollection(final Object collection, final Class<?> entityClass,
             boolean isRevisionCollection, final RESTBaseEntityV1<?, ?, ?> parent) {
         if (collection == null) {
             return null;
         }
 
         final CollectionWrapper wrapper;
 
         if (collection instanceof RESTTopicSourceUrlCollectionV1 && parent instanceof RESTBaseTopicV1) {
             // TOPIC SOURCE URL
             wrapper = new RESTTopicSourceURLCollectionV1Wrapper(getProviderFactory(), (RESTTopicSourceUrlCollectionV1) collection,
                     isRevisionCollection, (RESTBaseTopicV1<?, ?, ?>) parent);
         } else if (collection instanceof RESTTranslatedTopicStringCollectionV1 && parent instanceof RESTTranslatedTopicV1) {
             // TRANSLATED TOPIC STRING
             wrapper = new RESTTranslatedTopicStringCollectionV1Wrapper(getProviderFactory(),
                     (RESTTranslatedTopicStringCollectionV1) collection, isRevisionCollection, (RESTTranslatedTopicV1) parent);
         } else if (collection instanceof RESTLanguageImageCollectionV1 && parent instanceof RESTImageV1) {
             // LANGUAGE IMAGE
             wrapper = new RESTLanguageImageCollectionV1Wrapper(getProviderFactory(), (RESTLanguageImageCollectionV1) collection,
                     isRevisionCollection, (RESTImageV1) parent);
         } else if (collection instanceof RESTCategoryInTagCollectionV1 && parent instanceof RESTTagV1) {
             // CATEGORY TO TAG
             wrapper = new RESTCategoryInTagCollectionV1Wrapper(getProviderFactory(), (RESTCategoryInTagCollectionV1) collection,
                     isRevisionCollection, (RESTTagV1) parent);
         } else if (collection instanceof RESTTagInCategoryCollectionV1 && parent instanceof RESTCategoryV1) {
             // TAG TO CATEGORY
             wrapper = new RESTTagInCategoryCollectionV1Wrapper(getProviderFactory(), (RESTTagInCategoryCollectionV1) collection,
                     isRevisionCollection, (RESTCategoryV1) parent);
         } else if (collection instanceof RESTPropertyTagInPropertyCategoryCollectionV1 && parent instanceof RESTPropertyCategoryV1) {
             // PROPERTY TAG TO CATEGORY
             wrapper = new RESTPropertyTagInPropertyCategoryCollectionV1Wrapper(getProviderFactory(),
                     (RESTPropertyTagInPropertyCategoryCollectionV1) collection, isRevisionCollection, (RESTPropertyCategoryV1) parent);
        } else if (collection instanceof RESTTranslatedCSNodeStringCollectionV1 && parent instanceof RESTTranslatedCSNodeV1) {
             // CONTENT SPEC TRANSLATED NODE STRINGS
             wrapper = new RESTTranslatedCSNodeStringCollectionV1Wrapper(getProviderFactory(),
                     (RESTTranslatedCSNodeStringCollectionV1) collection, isRevisionCollection, (RESTTranslatedCSNodeV1) parent);
         } else {
             wrapper = createCollection(collection, entityClass, isRevisionCollection);
         }
 
         return wrapper;
     }
 
     /**
      * Create a wrapper around a collection of entities.
      *
      * @param collection           The collection to be wrapped.
      * @param entityClass          The class of the entity that the collection contains.
      * @param isRevisionCollection Whether or not the collection is a collection of revision entities.
      * @param parent               The parent entity that the collection comes from.
      * @param wrapperClass         The return wrapper class, incase the entities wrapper can't be determined by it's class.
      * @param <T>                  The wrapper class that is returned.
      * @return The Wrapper around the collection of entities.
      */
     @SuppressWarnings({"unchecked", "rawtypes"})
     public <T extends EntityWrapper<T>> CollectionWrapper<T> createCollection(final Object collection, final Class<?> entityClass,
             boolean isRevisionCollection, final RESTBaseEntityV1<?, ?, ?> parent, final Class<T> wrapperClass) {
         if (collection == null) {
             return null;
         }
 
         final CollectionWrapper wrapper;
 
         if (wrapperClass == PropertyTagInTopicWrapper.class && entityClass == RESTAssignedPropertyTagV1.class) {
             wrapper = new RESTPropertyTagInTopicCollectionV1Wrapper(getProviderFactory(), (RESTAssignedPropertyTagCollectionV1) collection,
                     isRevisionCollection, (RESTBaseTopicV1<?, ?, ?>) parent);
         } else if (wrapperClass == PropertyTagInTagWrapper.class && entityClass == RESTAssignedPropertyTagV1.class) {
             wrapper = new RESTPropertyTagInTagCollectionV1Wrapper(getProviderFactory(), (RESTAssignedPropertyTagCollectionV1) collection,
                     isRevisionCollection, (RESTBaseTagV1<?, ?, ?>) parent);
         } else if (wrapperClass == PropertyTagInContentSpecWrapper.class && entityClass == RESTAssignedPropertyTagV1.class) {
             wrapper = new RESTPropertyTagInContentSpecCollectionV1Wrapper(getProviderFactory(),
                     (RESTAssignedPropertyTagCollectionV1) collection, isRevisionCollection, (RESTContentSpecV1) parent);
         } else {
             wrapper = createCollection(collection, entityClass, isRevisionCollection);
         }
 
         return wrapper;
     }
 
     /**
      * Create a wrapper around a specific entity.
      *
      * @param entity       The entity to be wrapped.
      * @param isRevision   Whether the entity is a revision or not.
      * @param wrapperClass The return wrapper class, incase the entities wrapper can't be determined by it's class.
      * @param <T>          The wrapper class that is returned.
      * @return The Wrapper around the entity.
      */
     @SuppressWarnings("unchecked")
     public <T extends EntityWrapper<T>> T create(final Object entity, boolean isRevision, final Class<T> wrapperClass) {
         if (entity == null) {
             return null;
         }
 
         // Currently entities need their wrapper class defined and also don't need a parent
         return (T) create(entity, isRevision);
     }
 
     /**
      * Create a wrapper around a collection of entities.
      *
      * @param collection           The collection to be wrapped.
      * @param entityClass          The class of the entity that the collection contains.
      * @param isRevisionCollection Whether or not the collection is a collection of revision entities.
      * @param wrapperClass         The return wrapper class, incase the entities wrapper can't be determined by it's class.
      * @param <T>                  The wrapper class that is returned.
      * @return The Wrapper around the collection of entities.
      */
     @SuppressWarnings("unchecked")
     public <T extends EntityWrapper<T>> CollectionWrapper<T> createCollection(final Object collection, final Class<?> entityClass,
             boolean isRevisionCollection, final Class<T> wrapperClass) {
         if (collection == null) {
             return null;
         }
 
         // Current no collections require the wrapper class to be defined that also don't need parents
         return createCollection(collection, entityClass, isRevisionCollection);
     }
 
     /**
      * Create a list of wrapped entities.
      *
      * @param entities       The collection of entities to wrap.
      * @param isRevisionList Whether or not the collection is a collection of revision entities.
      * @param wrapperClass   The return wrapper class, incase the entities wrapper can't be determined by it's class.
      * @param <T>            The wrapper class that is returned.
      * @return An ArrayList of wrapped entities.
      */
     @SuppressWarnings("unchecked")
     public <T extends EntityWrapper<T>> List<T> createList(final Collection<?> entities, boolean isRevisionList,
             final Class<T> wrapperClass) {
         final List<T> retValue = new ArrayList<T>();
         for (final Object object : entities) {
             retValue.add((T) create(object, isRevisionList, wrapperClass));
         }
 
         return retValue;
     }
 
     /**
      * Create a list of wrapped entities.
      *
      * @param entities       The collection of entities to wrap.
      * @param isRevisionList Whether or not the collection is a collection of revision entities.
      * @param parent         The parent entity that the entities comes from.
      * @param wrapperClass   The return wrapper class, incase the entities wrapper can't be determined by it's class.
      * @param <T>            The wrapper class that is returned.
      * @return An ArrayList of wrapped entities.
      */
     @SuppressWarnings("unchecked")
     public <T extends EntityWrapper<T>> List<T> createList(final Collection<?> entities, boolean isRevisionList,
             final RESTBaseEntityV1<?, ?, ?> parent, final Class<T> wrapperClass) {
         final List<T> retValue = new ArrayList<T>();
         for (final Object object : entities) {
             retValue.add((T) create(object, isRevisionList, parent, wrapperClass));
         }
 
         return retValue;
     }
 
     /**
      * Create a list of wrapped entities from a REST Collection.
      *
      * @param entities       The collection of entities to wrap.
      * @param isRevisionList Whether or not the collection is a collection of revision entities.
      * @param wrapperClass   The return wrapper class, incase the entities wrapper can't be determined by it's class.
      * @param <T>            The wrapper class that is returned.
      * @param <U>            The class of the collection entities.
      * @param <V>            The class of the collection.
      * @return An ArrayList of wrapped entities.
      */
     @SuppressWarnings("unchecked")
     public <T extends EntityWrapper<T>, U extends RESTBaseEntityV1<U, V, ?>, V extends RESTBaseCollectionV1<U, V, ?>> List<T> createList(
             final V entities, boolean isRevisionList, final Class<T> wrapperClass) {
         if (entities == null) {
             return null;
         }
 
         final List<U> entityList = entities.returnItems();
         if (entityList == null) {
             return null;
         } else {
             return createList(entityList, isRevisionList, wrapperClass);
         }
     }
 
     /**
      * Create a list of wrapped entities from a REST Collection.
      *
      * @param entities       The collection of entities to wrap.
      * @param isRevisionList Whether or not the collection is a collection of revision entities.
      * @param parent         The parent entity that the list of entities comes from.
      * @param wrapperClass   The return wrapper class, incase the entities wrapper can't be determined by it's class.
      * @param <T>            The wrapper class that is returned.
      * @param <U>            The class of the collection entities.
      * @param <V>            The class of the collection.
      * @return An ArrayList of wrapped entities.
      */
     @SuppressWarnings("unchecked")
     public <T extends EntityWrapper<T>, U extends RESTBaseEntityV1<U, V, ?>, V extends RESTBaseCollectionV1<U, V, ?>> List<T> createList(
             final V entities, boolean isRevisionList, final RESTBaseEntityV1<?, ?, ?> parent, final Class<T> wrapperClass) {
         if (entities == null) {
             return null;
         }
 
         final List<U> entityList = entities.returnItems();
         if (entityList == null) {
             return null;
         } else {
             final List<T> retValue = new ArrayList<T>();
             for (final U object : entityList) {
                 retValue.add((T) create(object, isRevisionList, parent, wrapperClass));
             }
 
             return retValue;
         }
     }
 
     /**
      * Get the non proxied version of a REST Entity.
      *
      * @param entity The possible proxied entity.
      * @return The unwrapped/non-proxy entity, or the original entity if it wasn't a proxy.
      */
     @SuppressWarnings("rawtypes")
     protected Object getEntity(final Object entity) {
         if (entity instanceof ProxyObject) {
             final MethodHandler handler = ((ProxyObject) entity).getHandler();
             if (handler instanceof RESTBaseEntityV1ProxyHandler) {
                 return ((RESTBaseEntityV1ProxyHandler) handler).getEntity();
             }
         }
 
         return entity;
     }
 }
