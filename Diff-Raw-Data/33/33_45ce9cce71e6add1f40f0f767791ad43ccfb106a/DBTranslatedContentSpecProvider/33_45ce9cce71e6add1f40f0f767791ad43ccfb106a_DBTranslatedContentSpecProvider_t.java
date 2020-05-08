 package org.jboss.pressgang.ccms.provider;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaQuery;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.pressgang.ccms.filter.ContentSpecFieldFilter;
 import org.jboss.pressgang.ccms.filter.builder.ContentSpecFilterQueryBuilder;
 import org.jboss.pressgang.ccms.filter.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.filter.utils.FilterUtilities;
 import org.jboss.pressgang.ccms.model.Filter;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.model.contentspec.TranslatedCSNode;
 import org.jboss.pressgang.ccms.model.contentspec.TranslatedContentSpec;
 import org.jboss.pressgang.ccms.provider.listener.ProviderListener;
 import org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants;
 import org.jboss.pressgang.ccms.wrapper.DBTranslatedContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.DBWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.TranslatedCSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public class DBTranslatedContentSpecProvider extends DBDataProvider implements TranslatedContentSpecProvider {
     protected DBTranslatedContentSpecProvider(EntityManager entityManager, DBWrapperFactory wrapperFactory, List<ProviderListener> listeners) {
         super(entityManager, wrapperFactory, listeners);
     }
 
     @Override
     public TranslatedContentSpecWrapper getTranslatedContentSpec(int id) {
         return getWrapperFactory().create(getEntity(TranslatedContentSpec.class, id), false);
     }
 
     @Override
     public TranslatedContentSpecWrapper getTranslatedContentSpec(int id, Integer revision) {
         if (revision == null) {
             return getTranslatedContentSpec(id);
         } else {
             return getWrapperFactory().create(getRevisionEntity(TranslatedContentSpec.class, id, revision), true);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<TranslatedCSNodeWrapper> getTranslatedNodes(int id, Integer revision) {
         final DBTranslatedContentSpecWrapper translatedContentSpec = (DBTranslatedContentSpecWrapper) getTranslatedContentSpec(id,
                 revision);
         if (translatedContentSpec == null) {
             return null;
         } else {
             final CollectionWrapper<TranslatedCSNodeWrapper> collection = getWrapperFactory().createCollection(
                     translatedContentSpec.unwrap().getTranslatedCSNodes(), TranslatedCSNode.class, revision != null);
             return (UpdateableCollectionWrapper<TranslatedCSNodeWrapper>) collection;
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> getTranslatedContentSpecRevisions(int id, Integer revision) {
         final List<TranslatedContentSpec> revisions = getRevisionList(TranslatedContentSpec.class, id);
         return getWrapperFactory().createCollection(revisions, TranslatedContentSpec.class, revision != null);
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> getTranslatedContentSpecsWithQuery(String query) {
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
 
         return getWrapperFactory().createCollection(executeQuery(criteriaQuery), ContentSpec.class, false);
     }
 
     @Override
     public TranslatedContentSpecWrapper createTranslatedContentSpec(TranslatedContentSpecWrapper translatedContentSpec) {
         // Send the notification events
         notifyCreateEntity(translatedContentSpec);
 
         // Persist the new entity
        getEntityManager().persist(translatedContentSpec.unwrap());
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return translatedContentSpec;
     }
 
     @Override
     public TranslatedContentSpecWrapper updateTranslatedContentSpec(TranslatedContentSpecWrapper translatedContentSpec) {
         // Send the notification events
         notifyUpdateEntity(translatedContentSpec);
 
         // Persist the changes
         getEntityManager().merge(translatedContentSpec.unwrap());
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return translatedContentSpec;
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> createTranslatedContentSpecs(
             CollectionWrapper<TranslatedContentSpecWrapper> translatedContentSpecs) {
         // Send the notification events
         notifyCreateEntityCollection(translatedContentSpecs);
 
         // Persist the new entities
         for (final TranslatedContentSpecWrapper translatedContentSpec : translatedContentSpecs.getItems()) {
             getEntityManager().merge(translatedContentSpec.unwrap());
         }
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return translatedContentSpecs;
     }
 
     @Override
     public TranslatedContentSpecWrapper newTranslatedContentSpec() {
         return getWrapperFactory().create(new TranslatedContentSpec(), false, TranslatedContentSpecWrapper.class);
     }
 
     @Override
     public UpdateableCollectionWrapper<TranslatedContentSpecWrapper> newTranslatedContentSpecCollection() {
         final CollectionWrapper<TranslatedContentSpecWrapper> collection = getWrapperFactory().createCollection(
                 new ArrayList<TranslatedContentSpec>(), TranslatedContentSpec.class, false, TranslatedContentSpecWrapper.class);
         return (UpdateableCollectionWrapper<TranslatedContentSpecWrapper>) collection;
     }
 }
