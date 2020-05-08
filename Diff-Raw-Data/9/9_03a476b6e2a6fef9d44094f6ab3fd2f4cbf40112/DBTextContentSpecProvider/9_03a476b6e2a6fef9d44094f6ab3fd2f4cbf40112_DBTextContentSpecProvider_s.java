 package org.jboss.pressgang.ccms.provider;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaQuery;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.pressgang.ccms.filter.TopicFieldFilter;
 import org.jboss.pressgang.ccms.filter.builder.ContentSpecFilterQueryBuilder;
 import org.jboss.pressgang.ccms.filter.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.filter.utils.FilterUtilities;
 import org.jboss.pressgang.ccms.model.Filter;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.provider.listener.ProviderListener;
 import org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants;
 import org.jboss.pressgang.ccms.wrapper.DBTextCSProcessingOptionsWrapper;
 import org.jboss.pressgang.ccms.wrapper.DBWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.TextCSProcessingOptionsWrapper;
 import org.jboss.pressgang.ccms.wrapper.TextContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 
 public class DBTextContentSpecProvider extends DBDataProvider implements TextContentSpecProvider {
     private final DBProviderFactory providerFactory;
 
     protected DBTextContentSpecProvider(DBProviderFactory providerFactory, EntityManager entityManager, DBWrapperFactory wrapperFactory,
             List<ProviderListener> listeners) {
         super(entityManager, wrapperFactory, listeners);
         this.providerFactory = providerFactory;
     }
 
     @Override
     public TextContentSpecWrapper getTextContentSpec(int id) {
        return getWrapperFactory().create(getEntity(ContentSpec.class, id), false);
     }
 
     @Override
     public TextContentSpecWrapper getTextContentSpec(int id, Integer revision) {
         if (revision == null) {
             return getTextContentSpec(id);
         } else {
             return getWrapperFactory().create(getRevisionEntity(ContentSpec.class, id, revision), true, TextContentSpecWrapper.class);
         }
     }
 
     @Override
     public CollectionWrapper<TextContentSpecWrapper> getTextContentSpecsWithQuery(final String query) {
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
                 CommonFilterConstants.CATEORY_EXTERNAL_LOGIC, CommonFilterConstants.MATCH_LOCALE, new TopicFieldFilter());
 
         final ContentSpecFilterQueryBuilder queryBuilder = new ContentSpecFilterQueryBuilder(getEntityManager());
         final CriteriaQuery<ContentSpec> criteriaQuery = FilterUtilities.buildQuery(filter, queryBuilder);
 
         return getWrapperFactory().createCollection(executeQuery(criteriaQuery), ContentSpec.class, false, TextContentSpecWrapper.class);
     }
 
     @Override
     public CollectionWrapper<TextContentSpecWrapper> getTextContentSpecRevisions(int id, Integer revision) {
         final List<ContentSpec> revisions = getRevisionList(ContentSpec.class, id);
         return getWrapperFactory().createCollection(revisions, ContentSpec.class, revision != null, TextContentSpecWrapper.class);
     }
 
     @Override
     public TextContentSpecWrapper createTextContentSpec(TextContentSpecWrapper contentSpec) {
         return createTextContentSpec(contentSpec, null, null);
     }
 
     @Override
     public TextContentSpecWrapper createTextContentSpec(TextContentSpecWrapper contentSpec,
             TextCSProcessingOptionsWrapper processingOptions) {
         return createTextContentSpec(contentSpec, processingOptions, null);
     }
 
     @Override
     public TextContentSpecWrapper createTextContentSpec(TextContentSpecWrapper contentSpec,
             TextCSProcessingOptionsWrapper processingOptions, LogMessageWrapper logMessage) {
         throw new UnsupportedOperationException("Creating via text has no implementation");
     }
 
     @Override
     public TextContentSpecWrapper updateTextContentSpec(TextContentSpecWrapper contentSpec) {
         return updateTextContentSpec(contentSpec, null, null);
     }
 
     @Override
     public TextContentSpecWrapper updateTextContentSpec(TextContentSpecWrapper contentSpec,
             TextCSProcessingOptionsWrapper processingOptions) {
         return updateTextContentSpec(contentSpec, processingOptions, null);
     }
 
     @Override
     public TextContentSpecWrapper updateTextContentSpec(TextContentSpecWrapper contentSpec,
             TextCSProcessingOptionsWrapper processingOptions, LogMessageWrapper logMessage) {
         throw new UnsupportedOperationException("Updating via text has no implementation");
     }
 
     @Override
     public TextContentSpecWrapper newTextContentSpec() {
         return getWrapperFactory().create(new ContentSpec(), false, TextContentSpecWrapper.class);
     }
 
     @Override
     public TextCSProcessingOptionsWrapper newTextProcessingOptions() {
         return new DBTextCSProcessingOptionsWrapper();
     }
 }
