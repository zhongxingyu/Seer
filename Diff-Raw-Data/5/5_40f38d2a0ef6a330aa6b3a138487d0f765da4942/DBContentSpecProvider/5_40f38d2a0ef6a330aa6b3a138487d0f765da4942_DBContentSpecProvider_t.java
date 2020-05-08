 package org.jboss.pressgang.ccms.provider;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaQuery;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.pressgang.ccms.contentspec.utils.CSTransformer;
import org.jboss.pressgang.ccms.filter.ContentSpecFieldFilter;
 import org.jboss.pressgang.ccms.filter.builder.ContentSpecFilterQueryBuilder;
 import org.jboss.pressgang.ccms.filter.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.filter.utils.FilterUtilities;
 import org.jboss.pressgang.ccms.model.Filter;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.contentspec.CSNode;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpecToPropertyTag;
 import org.jboss.pressgang.ccms.model.contentspec.TranslatedContentSpec;
 import org.jboss.pressgang.ccms.provider.listener.ProviderListener;
 import org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.DBContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.DBWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public class DBContentSpecProvider extends DBDataProvider implements ContentSpecProvider {
     private final DBProviderFactory providerFactory;
 
     protected DBContentSpecProvider(DBProviderFactory providerFactory, EntityManager entityManager, DBWrapperFactory wrapperFactory,
             List<ProviderListener> listeners) {
         super(entityManager, wrapperFactory, listeners);
         this.providerFactory = providerFactory;
     }
 
     @Override
     public ContentSpecWrapper getContentSpec(int id) {
         return getWrapperFactory().create(getEntity(ContentSpec.class, id), false, ContentSpecWrapper.class);
     }
 
     @Override
     public ContentSpecWrapper getContentSpec(int id, Integer revision) {
         if (revision == null) {
             return getContentSpec(id);
         } else {
             return getWrapperFactory().create(getRevisionEntity(ContentSpec.class, id, revision), true, ContentSpecWrapper.class);
         }
     }
 
     @Override
     public CollectionWrapper<ContentSpecWrapper> getContentSpecsWithQuery(final String query) {
         final String fixedQuery = query.replace("query;", "");
         final String[] queryValues = fixedQuery.split(";");
         final Map<String, String> queryParameters = new HashMap<String, String>();
         for (final String value : queryValues) {
             if (value.trim().isEmpty()) continue;
             String[] keyValue = value.split("=", 2);
             try {
                 queryParameters.put(keyValue[0], URLDecoder.decode(keyValue[1], "UTF-8"));
             } catch (UnsupportedEncodingException e) {
                 // Should support UTF-8, if not throw a runtime error.
                 throw new RuntimeException(e);
             }
         }
 
         final Filter filter = EntityUtilities.populateFilter(getEntityManager(), queryParameters, CommonFilterConstants.FILTER_ID,
                 CommonFilterConstants.MATCH_TAG, CommonFilterConstants.GROUP_TAG, CommonFilterConstants.CATEORY_INTERNAL_LOGIC,
                CommonFilterConstants.CATEORY_EXTERNAL_LOGIC, CommonFilterConstants.MATCH_LOCALE, new ContentSpecFieldFilter());
 
         final ContentSpecFilterQueryBuilder queryBuilder = new ContentSpecFilterQueryBuilder(getEntityManager());
         final CriteriaQuery<ContentSpec> criteriaQuery = FilterUtilities.buildQuery(filter, queryBuilder);
 
         return getWrapperFactory().createCollection(executeQuery(criteriaQuery), ContentSpec.class, false, ContentSpecWrapper.class);
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getContentSpecTags(int id, Integer revision) {
         final DBContentSpecWrapper contentSpec = (DBContentSpecWrapper) getContentSpec(id, revision);
         if (contentSpec == null) {
             return null;
         } else {
             return getWrapperFactory().createCollection(contentSpec.unwrap().getTags(), Tag.class, revision != null);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<CSNodeWrapper> getContentSpecNodes(int id, Integer revision) {
         final DBContentSpecWrapper contentSpec = (DBContentSpecWrapper) getContentSpec(id, revision);
         if (contentSpec == null) {
             return null;
         } else {
             final CollectionWrapper<CSNodeWrapper> collection = getWrapperFactory().createCollection(contentSpec.unwrap().getChildrenList(),
                     CSNode.class, revision != null);
             return (UpdateableCollectionWrapper<CSNodeWrapper>) collection;
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> getContentSpecTranslations(int id, Integer revision) {
         final DBContentSpecWrapper contentSpec = (DBContentSpecWrapper) getContentSpec(id, revision);
         if (contentSpec == null) {
             return null;
         } else {
             return getWrapperFactory().createCollection(contentSpec.unwrap().getTranslatedContentSpecs(getEntityManager(), revision),
                     TranslatedContentSpec.class, revision != null);
         }
     }
 
     @Override
     public CollectionWrapper<ContentSpecWrapper> getContentSpecRevisions(int id, Integer revision) {
         final List<ContentSpec> revisions = getRevisionList(ContentSpec.class, id);
         return getWrapperFactory().createCollection(revisions, ContentSpec.class, revision != null, ContentSpecWrapper.class);
     }
 
     @Override
     public String getContentSpecAsString(int id) {
         return getContentSpecAsString(id, null);
     }
 
     @Override
     public String getContentSpecAsString(int id, Integer revision) {
         final ContentSpecWrapper contentSpecWrapper = getContentSpec(id, revision);
         if (contentSpecWrapper == null) return null;
 
         final org.jboss.pressgang.ccms.contentspec.ContentSpec contentSpec = CSTransformer.transform(contentSpecWrapper, providerFactory);
         return contentSpec.toString();
     }
 
     @Override
     public ContentSpecWrapper createContentSpec(ContentSpecWrapper contentSpec) {
         return createContentSpec(contentSpec, null);
     }
 
     @Override
     public ContentSpecWrapper createContentSpec(ContentSpecWrapper contentSpec, LogMessageWrapper logMessage) {
         // Send the notification events
         notifyCreateEntity(contentSpec);
         notifyLogMessage(logMessage);
 
         // Merge the new entity
         getEntityManager().persist(contentSpec.unwrap());
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return contentSpec;
     }
 
     @Override
     public ContentSpecWrapper updateContentSpec(ContentSpecWrapper contentSpec) {
         return updateContentSpec(contentSpec, null);
     }
 
     @Override
     public ContentSpecWrapper updateContentSpec(ContentSpecWrapper contentSpec, LogMessageWrapper logMessage) {
         // Send the notification events
         notifyUpdateEntity(contentSpec);
         notifyLogMessage(logMessage);
 
         // Persist the changes
         getEntityManager().persist(contentSpec.unwrap());
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return contentSpec;
     }
 
     @Override
     public boolean deleteContentSpec(Integer id) {
         return deleteContentSpec(id, null);
     }
 
     @Override
     public boolean deleteContentSpec(Integer id, LogMessageWrapper logMessage) {
         // Send the notification events
         notifyDeleteEntity(ContentSpec.class, id);
         notifyLogMessage(logMessage);
 
         // Remove the entity
         final ContentSpec contentSpec = getEntityManager().find(ContentSpec.class, id);
         getEntityManager().remove(contentSpec);
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return true;
     }
 
     @Override
     public ContentSpecWrapper newContentSpec() {
         return getWrapperFactory().create(new ContentSpec(), false, ContentSpecWrapper.class);
     }
 
     @Override
     public CollectionWrapper<ContentSpecWrapper> newContentSpecCollection() {
         return getWrapperFactory().createCollection(new ArrayList<ContentSpec>(), ContentSpec.class, false, ContentSpecWrapper.class);
     }
 
     @Override
     public UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> getContentSpecProperties(int id, Integer revision) {
         final DBContentSpecWrapper contentSpec = (DBContentSpecWrapper) getContentSpec(id, revision);
         if (contentSpec == null) {
             return null;
         } else {
             final CollectionWrapper<PropertyTagInContentSpecWrapper> collection = getWrapperFactory().createCollection(
                     contentSpec.unwrap().getProperties(), ContentSpecToPropertyTag.class, revision != null);
             return (UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper>) collection;
         }
     }
 }
