 package org.jboss.pressgang.ccms.server.rest.v1.base;
 
 import javax.annotation.Resource;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.naming.NamingException;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityNotFoundException;
 import javax.persistence.PersistenceException;
 import javax.persistence.PersistenceUnit;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import javax.transaction.RollbackException;
 import javax.transaction.Status;
 import javax.transaction.TransactionManager;
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 import javax.validation.ValidationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import java.io.IOException;
 import java.net.URI;
 import java.sql.BatchUpdateException;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.hibernate.envers.AuditReader;
 import org.hibernate.envers.AuditReaderFactory;
 import org.hibernate.envers.query.AuditEntity;
 import org.hibernate.envers.query.AuditQuery;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecParser;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecProcessor;
 import org.jboss.pressgang.ccms.contentspec.processor.constants.ProcessorConstants;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.ParserResults;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.ProcessingOptions;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLogger;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.filter.base.IFieldFilter;
 import org.jboss.pressgang.ccms.filter.base.IFilterQueryBuilder;
 import org.jboss.pressgang.ccms.filter.base.ITagFilterQueryBuilder;
 import org.jboss.pressgang.ccms.filter.utils.FilterUtilities;
 import org.jboss.pressgang.ccms.model.Filter;
 import org.jboss.pressgang.ccms.model.Topic;
 import org.jboss.pressgang.ccms.model.TopicSourceUrl;
 import org.jboss.pressgang.ccms.model.TopicToPropertyTag;
 import org.jboss.pressgang.ccms.model.User;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 import org.jboss.pressgang.ccms.model.config.ApplicationConfig;
 import org.jboss.pressgang.ccms.model.config.EntitiesConfig;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.model.exceptions.CustomConstraintViolationException;
 import org.jboss.pressgang.ccms.provider.DBProviderFactory;
 import org.jboss.pressgang.ccms.provider.exception.ProviderException;
 import org.jboss.pressgang.ccms.provider.exception.UnauthorisedException;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseEntityCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseEntityCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.CommonFilterConstants;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTUserV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTLogDetailsV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTextCSProcessingOptionsV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTextContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgang.ccms.server.ejb.EnversLoggingBean;
 import org.jboss.pressgang.ccms.server.envers.LoggingRevisionEntity;
 import org.jboss.pressgang.ccms.server.rest.BaseREST;
 import org.jboss.pressgang.ccms.server.rest.DatabaseOperation;
 import org.jboss.pressgang.ccms.server.rest.v1.BlobConstantV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.CSNodeV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.CategoryV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.ContentSpecV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.FileV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.FilterV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.ImageV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.IntegerConstantV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.ProjectV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.PropertyCategoryV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.PropertyTagV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.RoleV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.ServerSettingsV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.StringConstantV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.TagV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.TextContentSpecV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.TopicV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.TranslatedCSNodeV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.TranslatedContentSpecV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.TranslatedTopicV1Factory;
 import org.jboss.pressgang.ccms.server.rest.v1.UserV1Factory;
 import org.jboss.pressgang.ccms.server.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.server.utils.EnversUtilities;
 import org.jboss.pressgang.ccms.server.utils.ProviderUtilities;
 import org.jboss.pressgang.ccms.server.utils.TopicSourceURLTitleThread;
 import org.jboss.resteasy.plugins.providers.atom.Content;
 import org.jboss.resteasy.plugins.providers.atom.Entry;
 import org.jboss.resteasy.plugins.providers.atom.Feed;
 import org.jboss.resteasy.spi.BadRequestException;
 import org.jboss.resteasy.spi.Failure;
 import org.jboss.resteasy.spi.InternalServerErrorException;
 import org.jboss.resteasy.spi.NotFoundException;
 import org.jboss.resteasy.spi.UnauthorizedException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class provides the functions that retrieve, update, create and delete entities. It is expected that other classes will
  * extend BaseRESTv1 to provide expose REST functions.
  */
 @RequestScoped
 public class BaseRESTv1 extends BaseREST {
     private static final Logger log = LoggerFactory.getLogger(BaseRESTv1.class);
 
     public static final String TRANSACTION_MANAGER_NAME = "java:jboss/TransactionManager";
     public static final String USER_TRANSACTION_NAME = "java:jboss/UserTransaction";
     public static final String PERSISTENCE_UNIT_NAME = "PressGangCCMS";
 
     /**
      * The format for dates passed and returned by the REST Interface
      */
     protected static final String REST_DATE_FORMAT = "dd-MMM-yyyy";
 
     /**
      * An mapper to map Objects to JSON or vice-versa
      */
     private final ObjectMapper mapper = new ObjectMapper();
     /**
      * The Java Bean used for logging information to Envers
      */
     @Inject
     private EnversLoggingBean enversLoggingBean;
     /**
      * The JBoss Transaction Manager
      */
     @Resource(lookup = TRANSACTION_MANAGER_NAME)
     protected TransactionManager transactionManager;
     @PersistenceUnit(unitName = PERSISTENCE_UNIT_NAME)
     protected EntityManagerFactory entityManagerFactory;
     /**
      * The EntityManager to use for this request
      */
     @Inject
     protected EntityManager entityManager;
     /**
      * The cache used to store new and updated entities.
      */
     @Inject
     protected EntityCache entityCache;
     @Inject
     protected XMLEchoCache xmlEchoCache;
     @Inject
     protected ServerSettingsV1Factory applicationSettingsFactory;
 
     /* START ENTITY FACTORIES */
     @Inject
     protected BlobConstantV1Factory blobConstantFactory;
     @Inject
     protected CategoryV1Factory categoryFactory;
     @Inject
     protected ContentSpecV1Factory contentSpecFactory;
     @Inject
     protected CSNodeV1Factory csNodeFactory;
     @Inject
     protected FileV1Factory fileFactory;
     @Inject
     protected FilterV1Factory filterFactory;
     @Inject
     protected ImageV1Factory imageFactory;
     @Inject
     protected IntegerConstantV1Factory integerConstantFactory;
     @Inject
     protected ProjectV1Factory projectFactory;
     @Inject
     protected PropertyCategoryV1Factory propertyCategoryFactory;
     @Inject
     protected PropertyTagV1Factory propertyTagFactory;
     @Inject
     protected RoleV1Factory roleFactory;
     @Inject
     protected StringConstantV1Factory stringConstantFactory;
     @Inject
     protected TagV1Factory tagFactory;
     @Inject
     protected TopicV1Factory topicFactory;
     @Inject
     protected TranslatedTopicV1Factory translatedTopicFactory;
     @Inject
     protected TranslatedContentSpecV1Factory translatedContentSpecFactory;
     @Inject
     protected TranslatedCSNodeV1Factory translatedCSNodeFactory;
     @Inject
     protected TextContentSpecV1Factory textContentSpecFactory;
     @Inject
     protected UserV1Factory userFactory;
     /* END ENTITY FACTORIES */
 
     /**
      * Converts a Collection of Topics into an ATOM Feed.
      *
      * @param topics The collection of topics that should be transformed into the Feed.
      * @param title  The Title for the Feed.
      * @return A RESTEasy ATOM Feed Object containing the topic information.
      */
     protected Feed convertTopicsIntoFeed(final RESTTopicCollectionV1 topics, final String title) {
         try {
             final Feed feed = new Feed();
 
             feed.setId(new URI(getRequestUrl()));
             feed.setTitle(title);
             feed.setUpdated(new Date());
 
             final String docBuilderUrl = ApplicationConfig.getInstance().getDocBuilderUrl();
             final String uiUrl = ApplicationConfig.getInstance().getUIUrl();
 
             if (topics.getItems() != null) {
                 for (final RESTTopicV1 topic : topics.returnItems()) {
                     final Topic topicEntity = getEntity(Topic.class, topic.getId(), topic.getRevision());
                     final TopicToPropertyTag fixedUrlPropertyTag = topicEntity.getProperty(
                             EntitiesConfig.getInstance().getFixedUrlPropertyTagId());
                     final List<Number> topicRevisions = EnversUtilities.getRevisions(entityManager, Topic.class, topic.getId());
 
                     final Entry entry = new Entry();
                     entry.setId(new URI(topic.getSelfLink()));
                     entry.setTitle(topic.getTitle());
                     entry.setUpdated(topic.getLastModified());
                     entry.setPublished(topic.getCreated());
 
                     final StringBuilder contentString = new StringBuilder("<html><head></head><body>");
 
                     // Add the rendered link
                     if (uiUrl != null) {
                         final String renderedUrl = uiUrl + (uiUrl.endsWith("/") ? "" : "/") + "#TopicRenderedView;" + topic.getId();
                         contentString.append("<p>Rendered Link: <a href=\"").append(renderedUrl).append("\">").append(renderedUrl).append(
                                 "</a></p>");
                     }
 
                     // Add the docbuilder links
                     if (docBuilderUrl != null) {
                         contentString.append("<p>DocBuilder Link(s):</p><ul>");
 
                         final List<ContentSpec> contentSpecs = topicEntity.getContentSpecs(entityManager);
                         for (final ContentSpec contentSpec : contentSpecs) {
                             final StringBuilder url = new StringBuilder(docBuilderUrl + (docBuilderUrl.endsWith("/") ? "" : "/") +
                                     +contentSpec.getId());
 
                             if (fixedUrlPropertyTag != null) {
                                 url.append("#").append(fixedUrlPropertyTag.getValue());
                             }
 
                             contentString.append("<li><a href=\"").append(url).append("\">").append(url).append("</a></li>");
                         }
 
                         contentString.append("</ul>");
                     }
 
                     // Add the last 5 changes
                     contentString.append("<p>Changes:</p><ul>\n");
 
                     int count = 0;
                     for (final Number revision : topicRevisions) {
                         if (topicEntity.getRevision() != null && revision.intValue() > topicEntity.getRevision().intValue()) {
                             continue;
                         } else {
                             count++;
                             final LoggingRevisionEntity logEntity = EnversUtilities.getRevisionEntity(entityManager, topicEntity, revision);
                             final Date logDate = new Date(logEntity.getTimestamp());
                             contentString.append("<li>").append(new SimpleDateFormat(REST_DATE_FORMAT).format(logDate)).append(
                                     " - ").append(logEntity.getLogMessage() == null ? "" : logEntity.getLogMessage()).append("</li>");
                         }
 
                         if (count >= 5) break;
                     }
 
                     contentString.append("</ul></body></html>");
 
                     final Content content = new Content();
                     content.setType(MediaType.TEXT_HTML_TYPE);
                     content.setText(contentString.toString());
                     entry.setContent(content);
 
                     feed.getEntries().add(entry);
                 }
             }
 
             return feed;
         } catch (final Exception ex) {
             log.error("There was an error creating the ATOM feed", ex);
             throw new InternalServerErrorException("There was an error creating the ATOM feed");
         }
     }
 
     /**
      * @param collectionClass
      * @param type
      * @param idProperty
      * @param dataObjectFactory
      * @param expandName
      * @param expand            The expand parameters to determine what fields should be expanded.
      * @param date
      * @return
      */
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V getJSONEntitiesUpdatedSince(
             final Class<V> collectionClass, final Class<U> type, final String idProperty,
             final RESTDataObjectFactory<T, U, V, W> dataObjectFactory, final String expandName, final String expand, final Date date) {
         return getEntitiesUpdatedSince(collectionClass, type, idProperty, dataObjectFactory, expandName, expand, RESTv1Constants.JSON_URL,
                 date);
     }
 
     /**
      * @param collectionClass
      * @param type
      * @param idProperty
      * @param dataObjectFactory
      * @param expandName
      * @param expand            The expand parameters to determine what fields should be expanded.
      * @param dataType
      * @param date
      * @return
      */
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V getEntitiesUpdatedSince(
             final Class<V> collectionClass, final Class<U> type, final String idProperty,
             final RESTDataObjectFactory<T, U, V, W> dataObjectFactory, final String expandName, final String expand, final String dataType,
             final Date date) {
         assert date != null : "The date parameter can not be null";
 
         try {
             // Unmarshall the expand string into the ExpandDataTrunk object.
             final ExpandDataTrunk expandDataTrunk = unmarshallExpand(expand);
 
             // Get the list of entity ids that were edited after the selected date
             final AuditReader reader = AuditReaderFactory.get(entityManager);
             final AuditQuery query = reader.createQuery().forRevisionsOfEntity(type, true, false).addOrder(
                     AuditEntity.revisionProperty("timestamp").asc()).add(
                     AuditEntity.revisionProperty("timestamp").ge(date.getTime())).addProjection(
                     AuditEntity.property("originalId." + idProperty).distinct());
 
             @SuppressWarnings("rawtypes")
             final List entityIds = query.getResultList();
 
             // Create the query to get the actual entities using the ids from the previous query
             final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
             final CriteriaQuery<U> criteriaQuery = criteriaBuilder.createQuery(type);
             final Root<U> root = criteriaQuery.from(type);
             criteriaQuery.where(root.get(idProperty).in(entityIds));
 
             // Get the Entities from the database.
             final TypedQuery<U> jpaQuery = entityManager.createQuery(criteriaQuery);
             final List<U> entities = jpaQuery.getResultList();
 
             // Create and initialise the Collection using the specified REST Object Factory
             final V retValue = RESTDataObjectCollectionFactory.create(collectionClass, dataObjectFactory, entities, expandName, dataType,
                     expandDataTrunk, getBaseUrl(), entityManager);
 
             return retValue;
         } catch (final Exception ex) {
             log.error("Probably an issue querying Envers", ex);
             throw new InternalServerErrorException("There was an error running the query");
         }
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T deleteJSONEntity(
             final Class<U> type, final RESTDataObjectFactory<T, U, V, W> factory, final Integer id, final String expand,
             final RESTLogDetailsV1 logDetails) {
         return deleteEntity(type, factory, id, RESTv1Constants.JSON_URL, expand, logDetails);
     }
 
     /**
      * Delete a Entity from the database specified by the entities Primary Key.
      *
      * @param type       The Database Entity type to be deleted.
      * @param factory    The REST Object Factory to be used to generate a REST Entity return.
      * @param id         The ID of the Database Entity to be deleted.
      * @param dataType   The data type for the returned REST Entity response. (XML or JSON)
      * @param expand     The expand parameters to determine what fields should be expanded.
      * @param logDetails The details about the changes that need to be logged.
      * @return
      */
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T deleteEntity(
             final Class<U> type, final RESTDataObjectFactory<T, U, V, W> factory, final Integer id, final String dataType,
             final String expand, final RESTLogDetailsV1 logDetails) {
         assert id != null : "id should not be null";
 
         try {
             // Unmarshall the expand string into the ExpandDataTrunk object.
             final ExpandDataTrunk expandDataTrunk = unmarshallExpand(expand);
 
             // Start a Transaction
             transactionManager.begin();
 
             // Join the transaction we just started
             entityManager.joinTransaction();
 
             // Store the log details into the Logging Java Bean
             setLogDetails(entityManager, logDetails);
 
             // Find the specified entity and make sure that it exists
             final U entity = entityManager.find(type, id);
             if (entity == null) throw new NotFoundException("No entity was found with the id " + id);
 
             // Remove the entity from the persistence context
             entityManager.remove(entity);
 
             // Flush and commit the changes to the database
             entityManager.flush();
             transactionManager.commit();
 
             return factory.createRESTEntityFromDBEntity(entity, getBaseUrl(), dataType, expandDataTrunk);
         } catch (final Throwable e) {
             throw processError(transactionManager, e);
         }
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T createJSONEntity(
             final Class<U> type, final T restEntity, final RESTDataObjectFactory<T, U, V, W> factory, final String expand,
             final RESTLogDetailsV1 logDetails) {
         return createEntity(type, restEntity, factory, RESTv1Constants.JSON_URL, expand, logDetails);
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T updateJSONEntity(
             final Class<U> type, final T restEntity, final RESTDataObjectFactory<T, U, V, W> factory, final String expand,
             final RESTLogDetailsV1 logDetails) {
         return updateEntity(type, restEntity, factory, RESTv1Constants.JSON_URL, expand, logDetails);
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T createEntity(
             final Class<U> type, final T restEntity, final RESTDataObjectFactory<T, U, V, W> factory, final String dataType,
             final String expand, final RESTLogDetailsV1 logDetails) {
         return createOrUpdateEntity(type, restEntity, factory, DatabaseOperation.CREATE, dataType, expand, logDetails);
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T updateEntity(
             final Class<U> type, final T restEntity, final RESTDataObjectFactory<T, U, V, W> factory, final String dataType,
             final String expand, final RESTLogDetailsV1 logDetails) {
         return createOrUpdateEntity(type, restEntity, factory, DatabaseOperation.UPDATE, dataType, expand, logDetails);
     }
 
     /**
      * Create or update a Database Entity using the data given from a REST Entity.
      *
      * @param type       The Database entity type.
      * @param restEntity The REST Entity to create/update the database with.
      * @param factory    The type of REST Object Factory to use to generate the returned entity.
      * @param operation  The Database Operation type (CREATE or UPDATE).
      * @param dataType
      * @param expand     The expand parameters to determine what fields should be expanded.
      * @param logDetails The details about the changes that need to be logged.
      * @return The updated/created REST Entity representation of the database entity.
      */
     private <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T createOrUpdateEntity(
             final Class<U> type, final T restEntity, final RESTDataObjectFactory<T, U, V, W> factory, final DatabaseOperation operation,
             final String dataType, final String expand, final RESTLogDetailsV1 logDetails) {
         assert restEntity != null : "restEntity should not be null";
         assert factory != null : "factory should not be null";
 
         T retValue = null;
 
         try {
             // Unmarshall the expand string into the ExpandDataTrunk object.
             final ExpandDataTrunk expandDataTrunk = unmarshallExpand(expand);
 
             // Start a Transaction
             transactionManager.begin();
 
             // Join the transaction we just started
             entityManager.joinTransaction();
 
             // Store the log details into the Logging Java Bean
             setLogDetails(entityManager, logDetails);
 
             /*
              * The difference between creating or updating an entity is that we create a new instance of U, or find an existing
              * instance of U.
              */
             U entity = null;
             if (operation == DatabaseOperation.UPDATE) {
                 /* Have to have an ID for the entity we are deleting or updating */
                 if (restEntity.getId() == null) throw new BadRequestException("An id needs to be set for update operations");
 
                 // Find the entity and check that it actually exists
                 entity = entityManager.find(type, restEntity.getId());
                 if (entity == null) throw new BadRequestException("No entity was found with the primary key " + restEntity.getId());
 
                 // Sync the changes from the REST Entity to the Database.
                 factory.updateDBEntityFromRESTEntity(entity, restEntity);
                 factory.syncDBEntityWithRESTEntitySecondPass(entity, restEntity);
 
             } else if (operation == DatabaseOperation.CREATE) {
                 // Create a new Database Entity using the REST Entity.
                 entity = factory.createDBEntityFromRESTEntity(restEntity);
                 factory.syncDBEntityWithRESTEntitySecondPass(entity, restEntity);
 
                 // Check that a entity was able to be successfully created.
                 if (entity == null) throw new BadRequestException("The entity could not be created");
             }
 
             assert entity != null : "entity should not be null";
 
             // Persist the changes
             entityManager.persist(entity);
 
             // Flush the changes to the database and commit the transaction
             entityManager.flush();
             transactionManager.commit();
 
             retValue = factory.createRESTEntityFromDBEntity(entity, getBaseUrl(), dataType, expandDataTrunk, null, true);
         } catch (final Throwable e) {
             throw processError(transactionManager, e);
         }
 
         // Do Post create or update actions
         doPostCreateOrUpdateActions();
 
         return retValue;
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V createJSONEntities(
             final Class<V> collectionClass, final Class<U> type, final RESTBaseEntityCollectionV1<T, V, W> entities,
             final RESTDataObjectFactory<T, U, V, W> factory, final String expandName, final String expand,
             final RESTLogDetailsV1 logDetails) {
         return createEntities(collectionClass, type, entities, factory, expandName, RESTv1Constants.JSON_URL, expand, logDetails);
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V updateJSONEntities(
             final Class<V> collectionClass, final Class<U> type, final RESTBaseEntityCollectionV1<T, V, W> entities,
             final RESTDataObjectFactory<T, U, V, W> factory, final String expandName, final String expand,
             final RESTLogDetailsV1 logDetails) {
         return updateEntities(collectionClass, type, entities, factory, expandName, RESTv1Constants.JSON_URL, expand, logDetails);
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V createEntities(
             final Class<V> collectionClass, final Class<U> type, final RESTBaseEntityCollectionV1<T, V, W> entities,
             final RESTDataObjectFactory<T, U, V, W> factory, final String expandName, final String dataType, final String expand,
             final RESTLogDetailsV1 logDetails) {
         return createOrUpdateEntities(collectionClass, type, factory, entities, DatabaseOperation.CREATE, expandName, dataType, expand,
                 logDetails);
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V updateEntities(
             final Class<V> collectionClass, final Class<U> type, final RESTBaseEntityCollectionV1<T, V, W> entities,
             final RESTDataObjectFactory<T, U, V, W> factory, final String expandName, final String dataType, final String expand,
             final RESTLogDetailsV1 logDetails) {
         return createOrUpdateEntities(collectionClass, type, factory, entities, DatabaseOperation.UPDATE, expandName, dataType, expand,
                 logDetails);
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V deleteJSONEntities(
             final Class<V> collectionClass, final Class<U> type, final RESTDataObjectFactory<T, U, V, W> factory, final Set<String> ids,
             final String expandName, final String expand, final RESTLogDetailsV1 logDetails) {
         return deleteEntities(collectionClass, type, factory, ids, expandName, RESTv1Constants.JSON_URL, expand, logDetails);
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V deleteEntities(
             final Class<V> collectionClass, final Class<U> type, final RESTDataObjectFactory<T, U, V, W> factory, final Set<String> ids,
             final String expandName, final String dataType, final String expand, final RESTLogDetailsV1 logDetails) {
         assert type != null : "type should not be null";
         assert ids != null : "ids should not be null";
         assert factory != null : "factory should not be null";
 
         try {
             // Unmarshall the expand string into the ExpandDataTrunk object.
             final ExpandDataTrunk expandDataTrunk = unmarshallExpand(expand);
 
             // Start a Transaction
             transactionManager.begin();
 
             // Join the transaction we just started
             entityManager.joinTransaction();
 
             // Store the log details into the Logging Java Bean
             setLogDetails(entityManager, logDetails);
 
             final List<U> retValue = new ArrayList<U>();
             for (final String id : ids) {
                 /*
                  * The ids are passed as strings into a PathSegment. We need to change these into Integers
                  */
                 Integer idInt = null;
                 try {
                     idInt = Integer.parseInt(id);
                 } catch (final Exception ex) {
                     throw new BadRequestException("The id " + id + " was not a valid Integer");
                 }
 
                 // Get the entity from the database and check it exists.
                 final U entity = entityManager.find(type, idInt);
                 if (entity == null) throw new BadRequestException("No entity was found with the primary key " + id);
 
                 // Delete the entity from the persistence context
                 entityManager.remove(entity);
 
                 retValue.add(entity);
             }
 
             // Flush the changes and commit the changes
             entityManager.flush();
             transactionManager.commit();
 
             return RESTDataObjectCollectionFactory.create(collectionClass, factory, retValue, expandName, dataType, expandDataTrunk,
                     getBaseUrl(), true, entityManager);
         } catch (final Throwable e) {
             throw processError(transactionManager, e);
         }
     }
 
     /**
      * Takes a collection of REST entities, updates or creates the corresponding database entities, and returns those database
      * entities in a collection
      *
      * @param collectionClass The Class of the collection that should be returned.
      * @param type
      * @param factory
      * @param entities
      * @param operation       The Database Operation type (CREATE or UPDATE).
      * @param expandName
      * @param dataType
      * @param expand          The expand parameters to determine what fields should be expanded.
      * @param logDetails      The details about the changes that need to be logged.
      * @return
      */
     private <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V createOrUpdateEntities(
             final Class<V> collectionClass, final Class<U> type, final RESTDataObjectFactory<T, U, V, W> factory,
             final RESTBaseEntityCollectionV1<T, V, W> entities, final DatabaseOperation operation, final String expandName, final String dataType,
             final String expand, final RESTLogDetailsV1 logDetails) {
         assert entities != null : "dataObject should not be null";
         assert factory != null : "factory should not be null";
 
         V retValue = null;
 
         try {
             // Unmarshall the expand string into the ExpandDataTrunk object.
             final ExpandDataTrunk expandDataTrunk = unmarshallExpand(expand);
 
             // Start a Transaction
             transactionManager.begin();
 
             // Join the transaction we just started
             entityManager.joinTransaction();
 
             // Store the log details into the Logging Java Bean
             setLogDetails(entityManager, logDetails);
 
             final List<U> returnEntities = new ArrayList<U>();
             for (final T restEntity : entities.returnItems()) {
 
                 /*
                  * The difference between creating or updating an entity is that we create a new instance of U, or find an
                  * existing instance of U.
                  */
                 U entity = null;
                 if (operation == DatabaseOperation.UPDATE) {
                     // Have to have an ID for the entity we are deleting or updating
                     if (restEntity.getId() == null) throw new BadRequestException("An id needs to be set for update operations");
 
                     // Load the entity from the database and verify it exists
                     entity = entityManager.find(type, restEntity.getId());
                     if (entity == null) throw new BadRequestException("No entity was found with the primary key " + restEntity.getId());
 
                     // Sync the database entity with the REST Entity
                     factory.updateDBEntityFromRESTEntity(entity, restEntity);
                     factory.syncDBEntityWithRESTEntitySecondPass(entity, restEntity);
                 } else if (operation == DatabaseOperation.CREATE) {
                     // Create a Database Entity using the information from the REST Entity.
                     entity = factory.createDBEntityFromRESTEntity(restEntity);
                     factory.syncDBEntityWithRESTEntitySecondPass(entity, restEntity);
 
                     // Check that the entity was successfully created
                     if (entity == null) throw new BadRequestException("The entity could not be created");
                 }
 
                 // Save the created/updated entity
                 entityManager.persist(entity);
 
                 returnEntities.add(entity);
             }
 
             // Flush and commit the changes to the database.
             entityManager.flush();
             transactionManager.commit();
 
             retValue = RESTDataObjectCollectionFactory.create(collectionClass, factory, returnEntities, expandName, dataType,
                     expandDataTrunk, getBaseUrl(), true, entityManager);
         } catch (final Throwable e) {
             throw processError(transactionManager, e);
         }
 
         // Do Post create or update actions
         doPostCreateOrUpdateActions();
 
         return retValue;
     }
 
     /**
      * Convert a POJO Object to JSON.
      *
      * @param object The Object to be Converted to JSON.
      * @return The JSON representation of the Object.
      * @throws IOException
      */
     protected <T> String convertObjectToJSON(final T object) throws IOException {
         return mapper.writeValueAsString(object);
     }
 
     /**
      * Wrap JSON content in a callback function that can be used for JSONP responses.
      *
      * @param padding The Callback Function.
      * @param json    The json to be wrapped in the callback function.
      * @return The JSON wrapped in the callback function.
      */
     protected String wrapJsonInPadding(final String padding, final String json) {
         return padding + "(" + json + ")";
     }
 
     /**
      * This method is just a wrapper for the {@link #getResource(Class, RESTDataObjectFactory, Integer, Number, String, String)
      * getResource()} method that will specify that the response dataType will be JSON.
      *
      * @param type              The matching Database Entity type for the REST Entity.
      * @param dataObjectFactory The REST Object Factory to generate the REST Entity.
      * @param id                The ID of the database entity to generate the REST Entity for.
      * @param expand            The expand parameters to determine what fields should be expanded.
      * @return The REST Entity containing the information from the database entity.
      */
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T getJSONResource(
             final Class<U> type, final RESTDataObjectFactory<T, U, V, W> dataObjectFactory, final Integer id, final String expand) {
         return getJSONResource(type, dataObjectFactory, id, null, expand);
     }
 
     /**
      * This method is just a wrapper for the {@link #getResource(Class, RESTDataObjectFactory, Integer, Number, String, String)
      * getResource()} method that will specify that the response dataType will be JSON.
      *
      * @param type              The matching Database Entity type for the REST Entity.
      * @param dataObjectFactory The REST Object Factory to generate the REST Entity.
      * @param id                The ID of the database entity to generate the REST Entity for.
      * @param revision          The Revision of the entity to use to get the database entity.
      * @param expand            The expand parameters to determine what fields should be expanded.
      * @return The REST Entity containing the information from the database entity.
      */
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T getJSONResource(
             final Class<U> type, final RESTDataObjectFactory<T, U, V, W> dataObjectFactory, final Integer id, final Number revision,
             final String expand) {
         return getResource(type, dataObjectFactory, id, revision, expand, RESTv1Constants.JSON_URL);
     }
 
     /**
      * This method is just a wrapper for the {@link #getResource(Class, RESTDataObjectFactory, Integer, Number, String, String)
      * getResource()} method that will specify that the response dataType will be XML.
      *
      * @param type              The matching Database Entity type for the REST Entity.
      * @param dataObjectFactory The REST Object Factory to generate the REST Entity.
      * @param id                The ID of the database entity to generate the REST Entity for.
      * @param expand            The expand parameters to determine what fields should be expanded.
      * @return The REST Entity containing the information from the database entity.
      */
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T getXMLResource(
             final Class<U> type, final RESTDataObjectFactory<T, U, V, W> dataObjectFactory, final Integer id, final String expand) {
         return getXMLResource(type, dataObjectFactory, id, null, expand);
     }
 
     /**
      * This method is just a wrapper for the {@link #getResource(Class, RESTDataObjectFactory, Integer, Number, String, String)
      * getResource()} method that will specify that the response dataType will be XML.
      *
      * @param type              The matching Database Entity type for the REST Entity.
      * @param dataObjectFactory The REST Object Factory to generate the REST Entity.
      * @param id                The ID of the database entity to generate the REST Entity for.
      * @param revision          The Revision of the entity to use to get the database entity.
      * @param expand            The expand parameters to determine what fields should be expanded.
      * @return The REST Entity containing the information from the database entity.
      */
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T getXMLResource(
             final Class<U> type, final RESTDataObjectFactory<T, U, V, W> dataObjectFactory, final Integer id, final Number revision,
             final String expand) {
         return getResource(type, dataObjectFactory, id, revision, expand, RESTv1Constants.XML_URL);
     }
 
     /**
      * Generate the a REST Entity using an Entity resource from the database.
      *
      * @param type              The matching Database Entity type for the REST Entity.
      * @param dataObjectFactory The REST Object Factory to generate the REST Entity.
      * @param id                The ID of the database entity to generate the REST Entity for.
      * @param revision          The Revision of the entity to use to get the database entity.
      * @param expand            The expand parameters to determine what fields should be expanded.
      * @param dataType          The output data type. eg JSON or XML.
      * @return The REST Entity containing the information from the database entity.
      */
     private <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> T getResource(
             final Class<U> type, final RESTDataObjectFactory<T, U, V, W> dataObjectFactory, final Integer id, final Number revision,
             final String expand, final String dataType) {
         assert type != null : "The type parameter can not be null";
         assert id != null : "The id parameter can not be null";
         assert dataObjectFactory != null : "The dataObjectFactory parameter can not be null";
 
         boolean usingRevisions = revision != null;
 
         try {
             // Unmarshall the expand string into the ExpandDataTrunk object.
             final ExpandDataTrunk expandDataTrunk = unmarshallExpand(expand);
 
             /*
              * Load the Entity from the Database. If we aren't getting revision information then we can just use a normal
              * lookup. If we are getting a revision entity then we need to first do a Envers query to find the closest relevant
              * revision and then get that instance of the Entity.
              */
             final U entity;
             if (usingRevisions) {
                 entity = getEntity(type, id, revision);
             } else {
                 entity = getEntity(type, id);
             }
 
             // Create the REST representation of the topic
             final T restRepresentation = dataObjectFactory.createRESTEntityFromDBEntity(entity, getBaseUrl(), dataType, expandDataTrunk,
                     entity.getRevision(), true);
 
             return restRepresentation;
         } catch (final Throwable e) {
             throw processError(null, e);
         }
     }
 
     /**
      * Gets the latest Entity for a specific ID
      *
      * @param type The Entity type.
      * @param id   The Entity ID.
      * @param <U>  The Entity class.
      * @return The Entity that matches the type and ID
      */
     protected <U> U getEntity(final Class<U> type, final Object id) {
         try {
             final U entity = entityManager.find(type, id);
             if (entity == null) throw new NotFoundException("No entity was found with the primary key " + id);
 
             return entity;
         } catch (Throwable e) {
             throw processError(null, e);
         }
     }
 
     /**
      * Gets an Entity for a specific ID and Revision.
      *
      * @param type     The Entity type.
      * @param id       The Entity ID.
      * @param revision The entities revision, or null to get the latest version.
      * @param <U>      The Entity class.
      * @return The Entity that matches the type, ID and Revision
      */
     protected <U extends AuditedEntity> U getEntity(final Class<U> type, final Integer id, final Number revision) {
         try {
             final U entity;
 
             if (revision != null) {
                 final AuditReader reader = AuditReaderFactory.get(entityManager);
                 final Number closestRevision = EnversUtilities.getClosestRevision(reader, type, id, revision);
 
                 if (closestRevision == null)
                     throw new NotFoundException("No entity was found with the primary key " + id + ", revision " + revision);
 
                 // Get the Revision Entity using an envers lookup.
                 entity = reader.find(type, id, closestRevision);
 
                 if (entity == null)
                     throw new NotFoundException("No entity was found with the primary key " + id + ", revision " + revision);
 
                 // Set the entities last modified date to the information assoicated with the revision.
                 final Date revisionLastModified = reader.getRevisionDate(closestRevision);
                 entity.setLastModifiedDate(revisionLastModified);
                 entity.setRevision(closestRevision);
             } else {
                 entity = entityManager.find(type, id);
                 if (entity == null) throw new NotFoundException("No entity was found with the primary key " + id);
             }
             return entity;
         } catch (Throwable e) {
             throw processError(null, e);
         }
     }
 
     /**
      * Gets a Query that can be used to get all of entities for a specified entity type.
      *
      * @param type The type to get all Entities for.
      * @param <U>  The Entity class.
      * @return A CriteriaQuery that can be used to get all of the Entities for an Entity type.
      */
     protected <U extends AuditedEntity> CriteriaQuery<U> getAllEntitiesQuery(final Class<U> type) {
         // Create the select all query
         final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
         final CriteriaQuery<U> criteriaQuery = criteriaBuilder.createQuery(type);
         criteriaQuery.from(type);
 
         return criteriaQuery;
     }
 
     /**
      * Gets a List of all entities for a specified entity type.
      *
      * @param type The type to get all Entities for.
      * @param <U>  The Entity class.
      * @return A list of all the Entities for an Entity type.
      */
     protected <U extends AuditedEntity> List<U> getAllEntities(final Class<U> type) {
         // Get the query to be used
         final CriteriaQuery<U> criteriaQuery = getAllEntitiesQuery(type);
 
         // Execute the query and retrieve the results from the database
         final TypedQuery<U> query = entityManager.createQuery(criteriaQuery);
         return query.getResultList();
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V getXMLResources(
             final Class<V> collectionClass, final Class<U> type, final RESTDataObjectFactory<T, U, V, W> dataObjectFactory,
             final String expandName, final String expand) {
         return getResources(collectionClass, type, dataObjectFactory, expandName, expand, RESTv1Constants.XML_URL);
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V getJSONResources(
             final Class<V> collectionClass, final Class<U> type, final RESTDataObjectFactory<T, U, V, W> dataObjectFactory,
             final String expandName, final String expand) {
         return getResources(collectionClass, type, dataObjectFactory, expandName, expand, RESTv1Constants.JSON_URL);
     }
 
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V getResources(
             final Class<V> collectionClass, final Class<U> type, final RESTDataObjectFactory<T, U, V, W> dataObjectFactory,
             final String expandName, final String expand, final String dataType) {
         assert type != null : "The type parameter can not be null";
         assert dataObjectFactory != null : "The dataObjectFactory parameter can not be null";
 
         try {
             // Unmarshall the expand string into the ExpandDataTrunk object.
             final ExpandDataTrunk expandDataTrunk = unmarshallExpand(expand);
 
             // Get all the entities
             final CriteriaQuery<U> query = getAllEntitiesQuery(type);
 
             // Create and initialise the Collection using the specified REST Object Factory
             final V retValue = RESTDataObjectCollectionFactory.create(collectionClass, dataObjectFactory, query, expandName, dataType,
                     expandDataTrunk, getBaseUrl(), true, entityManager);
 
             return retValue;
         } catch (Throwable e) {
             throw processError(null, e);
         }
     }
 
     /**
      * Get a set of entity resources that will be JSON Encoded using a URL Query Parameter Map.
      *
      * @param collectionClass         The Class of the collection that should be returned.
      * @param queryParams             The map of URL Query Parameters to use when searching.
      * @param filterQueryBuilderClass The Class of the query builder to be used.
      * @param entityFieldFilter       A custom Field filter to be used by the Filter Query Builder.
      * @param dataObjectFactory       The Collection Factory object to be used to generate the contents of the collection.
      * @param expandName              The name that should be used to expand the collection.
      * @param expand                  The Expand Object that contains details about what should be expanded.
      * @return A Collection of Entities represented as the passed collectionClass.
      */
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V getJSONResourcesFromQuery(
             final Class<V> collectionClass, final MultivaluedMap<String, String> queryParams,
             final Class<? extends IFilterQueryBuilder<U>> filterQueryBuilderClass, final IFieldFilter entityFieldFilter,
             final RESTDataObjectFactory<T, U, V, W> dataObjectFactory, final String expandName, final String expand) {
         return getResourcesFromQuery(collectionClass, queryParams, filterQueryBuilderClass, entityFieldFilter, dataObjectFactory,
                 expandName, expand, RESTv1Constants.JSON_URL);
     }
 
     /**
      * Get a set of entity resources using a URL Query Parameter Map.
      *
      * @param collectionClass         The Class of the collection that should be returned.
      * @param queryParams             The map of URL Query Parameters to use when searching.
      * @param filterQueryBuilderClass The Class of the query builder to be used.
      * @param entityFieldFilter       A custom Field filter to be used by the Filter Query Builder.
      * @param dataObjectFactory       The Collection Factory object to be used to generate the contents of the collection.
      * @param expandName              The name that should be used to expand the collection.
      * @param expand                  The Expand Object that contains details about what should be expanded.
      * @param dataType                The MIME data type that should be returned and used for entity URL links.
      * @return A Collection of Entities represented as the passed collectionClass.
      */
     protected <T extends RESTBaseEntityV1<T, V, W>, U extends AuditedEntity, V extends RESTBaseEntityCollectionV1<T, V, W>,
             W extends RESTBaseEntityCollectionItemV1<T, V, W>> V getResourcesFromQuery(
             final Class<V> collectionClass, final MultivaluedMap<String, String> queryParams,
             final Class<? extends IFilterQueryBuilder<U>> filterQueryBuilderClass, final IFieldFilter entityFieldFilter,
             final RESTDataObjectFactory<T, U, V, W> dataObjectFactory, final String expandName, final String expand,
             final String dataType) {
         assert dataObjectFactory != null : "The dataObjectFactory parameter can not be null";
         assert uriInfo != null : "uriInfo can not be null";
 
         try {
             // Unmarshall the expand string into the ExpandDataTrunk object.
             final ExpandDataTrunk expandDataTrunk = unmarshallExpand(expand);
 
             // Get the Filter Entities
             final IFilterQueryBuilder<U> filterQueryBuilder = filterQueryBuilderClass.getConstructor(EntityManager.class).newInstance(
                     entityManager);
             final CriteriaQuery<U> query = getEntitiesFromQuery(queryParams, filterQueryBuilder, entityFieldFilter);
 
             // Create the Collection Class and populate it with data using the query result data
             final V retValue = RESTDataObjectCollectionFactory.create(collectionClass, dataObjectFactory, query, expandName, dataType,
                     expandDataTrunk, getBaseUrl(), true, entityManager);
 
             return retValue;
         } catch (Throwable e) {
             throw processError(null, e);
         }
     }
 
     /**
      * Gets the resulting Entities from a Filter Query.
      *
      * @param queryParams        The Query Parameters for the filter.
      * @param filterQueryBuilder The Filter Query Builder to create the SQL query.
      * @param entityFieldFilter  The entity field filter, to filter out incorrect fields.
      * @return A list of entities for the filter query.
      */
     protected <U extends AuditedEntity> CriteriaQuery<U> getEntitiesFromQuery(final MultivaluedMap<String, String> queryParams,
             final IFilterQueryBuilder<U> filterQueryBuilder, final IFieldFilter entityFieldFilter) {
         // build up a Filter object from the URL variables
         final Filter filter;
         if (filterQueryBuilder instanceof ITagFilterQueryBuilder) {
             filter = EntityUtilities.populateFilter(entityManager, queryParams, CommonFilterConstants.FILTER_ID,
                     CommonFilterConstants.MATCH_TAG, CommonFilterConstants.GROUP_TAG, CommonFilterConstants.CATEORY_INTERNAL_LOGIC,
                     CommonFilterConstants.CATEORY_EXTERNAL_LOGIC, CommonFilterConstants.MATCH_LOCALE, entityFieldFilter);
         } else {
             filter = EntityUtilities.populateFilter(entityManager, queryParams, CommonFilterConstants.MATCH_LOCALE, entityFieldFilter);
         }
 
         // Build the query to be used to get the resources
         final CriteriaQuery<U> query = FilterUtilities.buildQuery(filter, filterQueryBuilder);
         return query;
     }
 
     /**
      * Creates a content spec from a String representation of a content specification.
      *
      * @param dataObject The REST Entity to create/update the database with.
      * @param logDetails The details about the changes that need to be logged.
      * @param expand     The Expand Object that contains details about what should be expanded.
      * @return
      */
     protected RESTTextContentSpecV1 createJSONContentSpecFromString(final RESTTextContentSpecV1 dataObject,
             final RESTLogDetailsV1 logDetails, final String expand) {
         return createOrUpdateJSONContentSpecFromString(dataObject, DatabaseOperation.CREATE, logDetails, expand);
     }
 
     /**
      * Updates a content spec from a String representation of a content specification.
      *
      * @param dataObject The REST Entity to create/update the database with.
      * @param logDetails The details about the changes that need to be logged.
      * @param expand     The Expand Object that contains details about what should be expanded.
      * @return
      */
     protected RESTTextContentSpecV1 updateJSONContentSpecFromString(final RESTTextContentSpecV1 dataObject,
             final RESTLogDetailsV1 logDetails, final String expand) {
         return createOrUpdateJSONContentSpecFromString(dataObject, DatabaseOperation.UPDATE, logDetails, expand);
     }
 
     /**
      * Creates a content spec from a String representation of a content specification.
      *
      * @param contentSpecString The content spec string representation.
      * @param strictTitles
      * @param logDetails        The details about the changes that need to be logged.
      * @return
      */
     protected String createTEXTContentSpecFromString(final String contentSpecString, final Boolean strictTitles,
             final RESTLogDetailsV1 logDetails) {
         final ErrorLoggerManager loggerManager = new ErrorLoggerManager();
         final RESTTextContentSpecV1 contentSpec = new RESTTextContentSpecV1();
         contentSpec.explicitSetText(contentSpecString);
 
         final RESTTextCSProcessingOptionsV1 processingOptions = new RESTTextCSProcessingOptionsV1();
         processingOptions.setStrictTitles(strictTitles);
         contentSpec.setProcessingOptions(processingOptions);
 
         createOrUpdateJSONContentSpecFromString(contentSpec, DatabaseOperation.CREATE, logDetails, "", RESTv1Constants.TEXT_URL,
                 loggerManager, false);
 
         return loggerManager.generateLogs();
     }
 
     /**
      * Updates a content spec from a String representation of a content specification.
      *
      * @param id                The content spec id being updated.
      * @param contentSpecString The content spec string representation.
      * @param strictTitles
      * @param logDetails        The details about the changes that need to be logged.
      * @return
      */
     protected String updateTEXTContentSpecFromString(final Integer id, final String contentSpecString, final Boolean strictTitles,
             final RESTLogDetailsV1 logDetails) {
         final ErrorLoggerManager loggerManager = new ErrorLoggerManager();
         final RESTTextContentSpecV1 contentSpec = new RESTTextContentSpecV1();
         contentSpec.setId(id);
         contentSpec.explicitSetText(contentSpecString);
 
         final RESTTextCSProcessingOptionsV1 processingOptions = new RESTTextCSProcessingOptionsV1();
         processingOptions.setStrictTitles(strictTitles);
         contentSpec.setProcessingOptions(processingOptions);
 
         createOrUpdateJSONContentSpecFromString(contentSpec, DatabaseOperation.UPDATE, logDetails, "", RESTv1Constants.TEXT_URL,
                 loggerManager, true);
 
         return loggerManager.generateLogs();
     }
 
     /**
      * Creates or Updates a content spec from a String representation of a content specification.
      *
      * @param restEntity The content spec string representation object.
      * @param operation  The Database Operation type (CREATE or UPDATE).
      * @param logDetails The details about the changes that need to be logged.
      * @param expand     The Expand Object that contains details about what should be expanded.
      * @return
      */
     private RESTTextContentSpecV1 createOrUpdateJSONContentSpecFromString(final RESTTextContentSpecV1 restEntity,
             final DatabaseOperation operation, final RESTLogDetailsV1 logDetails, final String expand) {
         return createOrUpdateJSONContentSpecFromString(restEntity, operation, logDetails, expand, RESTv1Constants.JSON_URL,
                 new ErrorLoggerManager(), true);
     }
 
     /**
      * Creates or Updates a content spec from a String representation of a content specification.
      *
      * @param restEntity      The content spec string representation object.
      * @param operation       The Database Operation type (CREATE or UPDATE).
      * @param logDetails      The details about the changes that need to be logged.
      * @param expand          The Expand Object that contains details about what should be expanded.
      * @param dataType
      * @param loggerManager   The Content Spec Logging manager to capture logs messages.
      * @param saveWhenInvalid If the Content Specification should be saved even if the text isn't valid.
      * @return
      */
     private RESTTextContentSpecV1 createOrUpdateJSONContentSpecFromString(final RESTTextContentSpecV1 restEntity,
             final DatabaseOperation operation, final RESTLogDetailsV1 logDetails, final String expand, final String dataType,
             final ErrorLoggerManager loggerManager, boolean saveWhenInvalid) {
         assert restEntity != null;
 
         final Integer id = restEntity.getId();
         final String contentSpecString = restEntity.getText();
 
         boolean success = true;
         boolean textProcessed = false;
         RuntimeException exception = null;
         Integer csId = id;
 
         try {
             // Start a Transaction
             transactionManager.begin();
 
             // Join the transaction we just started
             entityManager.joinTransaction();
 
             // Check the current entity exists
             if (id != null) {
                 if (entityManager.find(ContentSpec.class, id) == null) {
                     throw new NotFoundException("No entity was found with the primary key " + id);
                 }
             }
 
             // Store the log details into the Logging Java Bean
             setLogDetails(entityManager, logDetails);
 
             // Apply the text separately
             if (restEntity.hasParameterSet(RESTTextContentSpecV1.TEXT_NAME)) {
                 textProcessed = true;
                 final DBProviderFactory providerFactory = ProviderUtilities.getDBProviderFactory(entityManager, transactionManager,
                         enversLoggingBean);
                 final ProcessingOptions processingOptions = new ProcessingOptions();
                 processingOptions.setIgnoreChecksum(true);
                 if (restEntity.getProcessingOptions() != null && restEntity.getProcessingOptions().getStrictTitles() != null) {
                     processingOptions.setStrictTitles(restEntity.getProcessingOptions().getStrictTitles());
                 }
 
                 final ContentSpecParser parser = new ContentSpecParser(providerFactory, loggerManager);
                 final ContentSpecProcessor processor = new ContentSpecProcessor(providerFactory, loggerManager, processingOptions);
 
                 // Process the content spec
                 final ParserResults results = processContentSpecString(id, contentSpecString, parser, processor,
                         enversLoggingBean.getUsername(), operation, dataType);
 
                 success = results.parsedSuccessfully();
                if (success) {
                    csId = results.getContentSpec().getId();
                }
             }
 
             // If the content spec processed correctly then commit the changes, otherwise roll them back.
             if (!success) {
                 final int status = transactionManager.getStatus();
                 if (status != Status.STATUS_ROLLING_BACK && status != Status.STATUS_ROLLEDBACK && status != Status.STATUS_NO_TRANSACTION) {
                     transactionManager.rollback();
                 }
             } else {
                 // Get the updated or created entity
                 final ContentSpec entity = entityManager.find(ContentSpec.class, csId);
 
                 if (entity != null) {
                     // Process any additional changes
                     textContentSpecFactory.updateDBEntityFromRESTEntity(entity, restEntity);
                     textContentSpecFactory.syncDBEntityWithRESTEntitySecondPass(entity, restEntity);
 
                     // Remove any errors that occurred previously
                     if (textProcessed) {
                         entity.setErrors(loggerManager.generateLogs());
                         entity.setFailedContentSpec(null);
                     }
 
                     entityManager.persist(entity);
                 }
                 transactionManager.commit();
 
                 // Get the revision and log a message
                 final Integer revision = (Integer) EnversUtilities.getLatestRevision(entityManager, ContentSpec.class, csId);
                 final ErrorLogger logger = loggerManager.getLogger(ContentSpecProcessor.class);
                 logger.info(String.format(ProcessorConstants.SUCCESSFUL_PUSH_ID_MSG, csId));
                 logger.info(String.format(ProcessorConstants.SUCCESSFUL_PUSH_REV_MSG, revision));
             }
         } catch (final Throwable e) {
             exception = processError(transactionManager, e);
         } finally {
             // Check if the processing succeeded, if not set the error fields and throw an error
             final String log = loggerManager.generateLogs();
             if (saveWhenInvalid) {
                 if (!success) {
                     csId = setContentSpecErrors(restEntity, contentSpecString, log, logDetails);
                 }
             } else {
                 throw new BadRequestException(log);
             }
         }
 
         if (exception != null) {
             throw exception;
         } else {
             return getJSONResource(ContentSpec.class, textContentSpecFactory, csId, expand);
         }
     }
 
     /**
      * Parse and process a Content Specification as a String representation.
      *
      * @param id                The id that the content spec should be, or null if it's being created.
      * @param contentSpecString The Content Spec string representation.
      * @param parser            The parser to use to parse the String representation.
      * @param processor         The processor to use, to valid and save the parsed content spec.
      * @param username          The username of the user who sent the request.
      * @param operation         Whether the content spec should be created or updated.
      * @param dataType
      * @return True if the Content Spec was parsed and processed successfully, otherwise false.
      */
     private ParserResults processContentSpecString(final Integer id, final String contentSpecString, final ContentSpecParser parser,
             final ContentSpecProcessor processor, final String username, final DatabaseOperation operation, final String dataType) {
         final ContentSpecParser.ParsingMode mode;
         if (dataType.equals(RESTv1Constants.TEXT_URL)) {
             if (operation == DatabaseOperation.CREATE) {
                 mode = ContentSpecParser.ParsingMode.NEW;
             } else {
                 mode = ContentSpecParser.ParsingMode.EDITED;
             }
         } else {
             mode = ContentSpecParser.ParsingMode.EITHER;
         }
 
         // Parse the spec
         ParserResults retValue = parser.parse(contentSpecString, mode, true);
         if (retValue.parsedSuccessfully()) {
             final org.jboss.pressgang.ccms.contentspec.ContentSpec contentSpec = retValue.getContentSpec();
             // Check that the id matches
             if (id != null && contentSpec != null) {
                 if (contentSpec.getId() == null) {
                     throw new BadRequestException("The Content Spec has no ID, but the request was to update an existing Content Spec.");
                 } else if (!id.equals(contentSpec.getId())) {
                     throw new BadRequestException("The Content Spec ID doesn't match the request ID.");
                 }
             } else if (contentSpec != null) {
                 if (contentSpec.getId() != null) {
                     throw new BadRequestException("The Content Spec has an ID, but the request was to create a new Content Spec.");
                 }
             }
 
             // Process and save the spec
             boolean success = processor.processContentSpec(contentSpec, username, mode);
             retValue = new ParserResults(success, contentSpec);
         }
 
         return retValue;
     }
 
     /**
      * Set a Content Spec to include any errors messages from processing and the failed content spec.
      *
      * @param restEntity        The rest entity the request failed for.
      * @param contentSpecString The failed Content Spec string.
      * @param errors            The error messages.
      * @param logDetails        The log details for the failed Content Spec.
      * @return The ID of the Content Spec.
      */
     private Integer setContentSpecErrors(final RESTTextContentSpecV1 restEntity, final String contentSpecString, final String errors,
             final RESTLogDetailsV1 logDetails) {
         final Integer id = restEntity.getId();
 
         try {
             // Start a Transaction
             transactionManager.begin();
 
             // Join the transaction we just started
             entityManager.joinTransaction();
 
             // Store the log details into the Logging Java Bean
             setLogDetails(entityManager, logDetails);
 
             // Get the current entity
             final ContentSpec entity;
             if (id != null) {
                 entity = entityManager.find(ContentSpec.class, id);
             } else {
                 entity = new ContentSpec();
             }
 
             if (entity == null) throw new BadRequestException("No entity was found with the primary key " + id);
 
             entity.setErrors(errors);
             entity.setFailedContentSpec(contentSpecString);
 
             // Process any additional changes
             textContentSpecFactory.updateDBEntityFromRESTEntity(entity, restEntity);
             textContentSpecFactory.syncDBEntityWithRESTEntitySecondPass(entity, restEntity);
 
             // Save the error messages
             entityManager.persist(entity);
             transactionManager.commit();
 
             return entity.getId();
         } catch (final Throwable e) {
             throw processError(transactionManager, e);
         }
     }
 
     /**
      * Set the log details for the current request. This will use the injected EnversLoggingBean object to set the logging
      * details for Envers.
      *
      * @param entityManager An EntityManager Object that can be used to look up database entities.
      * @param dataObject    The LogDetails object that contains the details to be associated with in the log.
      */
     private void setLogDetails(final EntityManager entityManager, final RESTLogDetailsV1 dataObject) {
         if (dataObject == null) return;
 
         if (dataObject.hasParameterSet(RESTLogDetailsV1.MESSAGE_NAME)) enversLoggingBean.setLogMessage(dataObject.getMessage());
         if (dataObject.hasParameterSet(RESTLogDetailsV1.FLAG_NAME)) enversLoggingBean.setFlag(dataObject.getFlag());
         if (dataObject.hasParameterSet(RESTLogDetailsV1.USERNAME_NAME)) {
             if (dataObject.getUser() != null && dataObject.getUser().getId() != null) {
                 final User user = entityManager.find(User.class, dataObject.getUser().getId());
 
                 if (user == null)
                     throw new BadRequestException("No user entity was found with the primary key " + dataObject.getUser().getId());
 
                 enversLoggingBean.setUsername(user.getUserName());
             }
         }
     }
 
     /**
      * Generate a RESTLogDetails object from a set of URL passed parameters.
      *
      * @param message The message to be associated with the log.
      * @param flag    The Message Flags. (ie Minor or Major change).
      * @param userId  The ID of the user who has made the changes.
      * @return A pre-populated RESTLogDetailsV1 object.
      */
     protected RESTLogDetailsV1 generateLogDetails(final String message, final Integer flag, final String userId) {
         final RESTLogDetailsV1 logDetails = new RESTLogDetailsV1();
 
         // Flag
         if (flag != null) {
             logDetails.explicitSetFlag(flag);
         }
 
         // Message
         if (message != null) {
             logDetails.explicitSetMessage(message);
         }
 
         // Username/id
         if (userId != null) {
             if (userId.matches("^\\d+$")) {
                 final RESTUserV1 user = new RESTUserV1();
                 user.setId(Integer.parseInt(userId));
                 logDetails.explicitSetUser(user);
             } else {
                 try {
                     final User userEntity = EntityUtilities.getUserFromUsername(entityManager, userId);
                     if (userEntity == null) throw new BadRequestException("No user was found with the username " + userId);
 
                     final RESTUserV1 user = new RESTUserV1();
                     user.setId(userEntity.getId());
                     logDetails.explicitSetUser(user);
                 } catch (Throwable e) {
                     throw processError(null, e);
                 }
             }
         }
 
         return logDetails;
     }
 
     /**
      * Convert a String Expand representation into an ExpandDataTrunk object.
      *
      * @param expand The String representation for the expand.
      * @return An ExpandDataTrunk object containing the converted data.
      */
     protected ExpandDataTrunk unmarshallExpand(final String expand) {
         try {
             /*
              * convert the expand string from JSON to an instance of ExpandDataTrunk
              */
             ExpandDataTrunk expandDataTrunk = new ExpandDataTrunk();
             if (expand != null && !expand.trim().isEmpty()) {
                 expandDataTrunk = mapper.readValue(expand, ExpandDataTrunk.class);
             }
 
             return expandDataTrunk;
         } catch (final IOException ex) {
             throw new BadRequestException("Could not convert expand data from JSON to an instance of ExpandDataTrunk");
         }
     }
 
     /**
      * Process an Error/Exception and generate a RESTEasy Exception based on the error/exception produced.
      *
      * @param transactionManager The transaction manager to handle rolling back changes.
      * @param ex                 The Error/Exception to be processed.
      * @return A RESTEasy Exception containing the details of the Error.
      */
     public Failure processError(final TransactionManager transactionManager, final Throwable ex) {
         log.error("Failed to process REST request", ex);
 
         // Rollback if a transaction is active
         try {
             if (transactionManager != null) {
                 /*
                     Rolling back only active transactions leads to "Error checking for a transaction" and
                     "Transaction is not active" errors.
 
                     From http://techblogs.agiledigital.com.au/2013/01/03/jboss-as-7-1-transaction-reaping/
 
                         Transaction information is stored in the thread context. Each connector can potentially share the
                         same transaction – even across requests. The transaction is removed from the thread context when it
                         is committed or rolled back.
 
                         When the transaction is reaped, it is marked as rollback only. This changes the status of the
                         transaction to STATUS_ROLLING_BACK – and then shortly thereafter STATUS_ROLLED_BACK. However, the
                         transaction is not actually rolled back or removed from the context of the thread. It will not be
                         removed until utx.commit() or utx.rollback() is called.
 
                         The base servlet would never attempt to commit or rollback the transaction because:
 
                         (utx.getStatus() == Status.STATUS_ACTIVE)
 
                         will always be false after the transaction reaper has caused the status to change to ROLLED_BACK.
                         Consequently, the transaction was never removed from the thread context and an attempt would be made
                         by TxConnectionManagerImpl (seen in the stack trace above) to re-use it. Since it had been marked
                         ROLLED_BACK it could not be re-used and an exception was thrown.
                  */
                 final int status = transactionManager.getStatus();
                 if (status != Status.STATUS_NO_TRANSACTION) {
                     transactionManager.rollback();
                 }
             }
         } catch (Throwable e) {
             return new InternalServerErrorException(e);
         }
 
         // We need to do some unwrapping of exception first
         Throwable cause = ex;
         while (cause != null) {
             if (cause == cause.getCause()) {
                 // sometimes this can be an circular reference
                 break;
             } else if (cause instanceof Failure) {
                 return (Failure) cause;
             } else if (cause instanceof EntityNotFoundException) {
                 return new NotFoundException(cause);
             } else if (cause instanceof ValidationException || cause instanceof CustomConstraintViolationException || cause instanceof
                     org.hibernate.exception.ConstraintViolationException || cause instanceof RollbackException) {
                 break;
             } else if (cause instanceof PersistenceException) {
                 if (cause.getCause() != null && cause.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                     cause = cause.getCause();
                 } else {
                     break;
                 }
             } else if (cause instanceof ProviderException) {
                 if (cause != null && (cause instanceof ValidationException || cause instanceof PersistenceException || cause instanceof
                         CustomConstraintViolationException)) {
                     cause = cause.getCause();
                 } else {
                     break;
                 }
             } else if (cause instanceof BatchUpdateException) {
                 cause = ((SQLException) cause).getNextException();
             } else {
                 cause = cause.getCause();
             }
         }
 
         // This is a Persistence exception with information
         if (cause instanceof ConstraintViolationException) {
             final ConstraintViolationException e = (ConstraintViolationException) cause;
             final StringBuilder stringBuilder = new StringBuilder();
 
             // Construct a "readable" message outlining the validation errors
             for (ConstraintViolation invalidValue : e.getConstraintViolations())
                 stringBuilder.append(invalidValue.getMessage()).append("\n");
 
             return new BadRequestException(stringBuilder.toString(), cause);
         } else if (cause instanceof EntityNotFoundException) {
             return new NotFoundException(cause);
         } else if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
             return new BadRequestException(cause.getMessage());
         } else if (cause instanceof ValidationException || cause instanceof CustomConstraintViolationException) {
             return new BadRequestException(cause);
         } else if (cause instanceof RollbackException) {
             return new BadRequestException(
                     "This is most likely caused by the fact that two users are trying to save the same entity at the same time.\n" + "You" +
                             " can try saving again, or reload the entity to see if there were any changes made in the background.", cause);
         } else if (cause instanceof ProviderException) {
             if (cause instanceof org.jboss.pressgang.ccms.provider.exception.NotFoundException) {
                 throw new NotFoundException(cause);
             } else if (cause instanceof org.jboss.pressgang.ccms.provider.exception.InternalServerErrorException) {
                 throw new InternalServerErrorException(cause);
             } else if (cause instanceof org.jboss.pressgang.ccms.provider.exception.BadRequestException) {
                 throw new BadRequestException(cause);
             } else if (cause instanceof UnauthorisedException) {
                 throw new UnauthorizedException(cause);
             }
         }
 
         // If it's not some validation error then it must be an internal error.
         return new InternalServerErrorException(ex);
     }
 
     /**
      * Do any actions that are required after a create or update event.
      */
     protected void doPostCreateOrUpdateActions() {
         // Process the topic source urls and set their titles
         final List<TopicSourceUrl> topicSourceUrls = entityCache.getEntities(TopicSourceUrl.class);
         if (!topicSourceUrls.isEmpty()) {
             try {
                 final TopicSourceURLTitleThread workerThread = new TopicSourceURLTitleThread(topicSourceUrls);
                 workerThread.start();
             } catch (NamingException e) {
                 throw new InternalServerErrorException(e);
             }
         }
     }
 }
