 package org.jboss.pressgang.ccms.server.rest.v1;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.model.PropertyTag;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.contentspec.CSNode;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpecToPropertyTag;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTextContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTTextContentSpecCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTAssignedPropertyTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTextContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums.RESTContentSpecTypeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgang.ccms.server.ejb.EnversLoggingBean;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectCollectionFactory;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectFactory;
 import org.jboss.pressgang.ccms.server.rest.v1.utils.RESTv1Utilities;
 import org.jboss.pressgang.ccms.server.utils.ContentSpecUtilities;
 import org.jboss.pressgang.ccms.server.utils.EnversUtilities;
 import org.jboss.resteasy.spi.BadRequestException;
 
 @ApplicationScoped
 public class TextContentSpecV1Factory extends RESTDataObjectFactory<RESTTextContentSpecV1, ContentSpec, RESTTextContentSpecCollectionV1,
         RESTTextContentSpecCollectionItemV1> {
     @Inject
     protected ContentSpecPropertyTagV1Factory contentSpecPropertyTagFactory;
     @Inject
     protected TranslatedContentSpecV1Factory translatedContentSpecFactory;
     @Inject
     protected TagV1Factory tagFactory;
     @Inject
     protected EnversLoggingBean enversLoggingBean;
 
     @Override
     public RESTTextContentSpecV1 createRESTEntityFromDBEntityInternal(final ContentSpec entity, final String baseUrl, final String dataType,
             final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences) {
         assert entity != null : "Parameter entity can not be null";
         assert baseUrl != null : "Parameter baseUrl can not be null";
 
         final RESTTextContentSpecV1 retValue = new RESTTextContentSpecV1();
 
         final List<String> expandOptions = new ArrayList<String>();
         expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
         expandOptions.add(RESTTextContentSpecV1.PROPERTIES_NAME);
         expandOptions.add(RESTTextContentSpecV1.TAGS_NAME);
         expandOptions.add(RESTTextContentSpecV1.TEXT_NAME);
        expandOptions.add(RESTTextContentSpecV1.TRANSLATED_CONTENT_SPECS_NAME);
         if (revision == null) expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
         retValue.setExpand(expandOptions);
 
         retValue.setId(entity.getId());
         retValue.setLocale(entity.getLocale());
         retValue.setType(RESTContentSpecTypeV1.getContentSpecType(entity.getContentSpecType()));
         retValue.setLastPublished(entity.getLastPublished());
         retValue.setLastModified(entity.getLastModified());
         retValue.setErrors(entity.getErrors());
         retValue.setFailedContentSpec(ContentSpecUtilities.fixFailedContentSpec(entity));
         final CSNode titleNode = entity.getContentSpecTitle();
         retValue.setTitle(titleNode == null ? null : titleNode.getAdditionalText());
         final CSNode productNode = entity.getContentSpecProduct();
         retValue.setProduct(productNode == null ? null : productNode.getAdditionalText());
         final CSNode versionNode = entity.getContentSpecVersion();
         retValue.setVersion(versionNode == null ? null : versionNode.getAdditionalText());
 
         // REVISIONS
         if (revision == null && expand != null && expand.contains(RESTBaseEntityV1.REVISIONS_NAME)) {
             retValue.setRevisions(RESTDataObjectCollectionFactory.create(RESTTextContentSpecCollectionV1.class, this, entity,
                     EnversUtilities.getRevisions(entityManager, entity), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl,
                     entityManager));
         }
 
         // TEXT
         if (expand != null && expand.contains(RESTTextContentSpecV1.TEXT_NAME)) {
             final String text = ContentSpecUtilities.getContentSpecText(entity.getId(), (Integer) revision, entityManager);
             retValue.setText(text);
         }
 
         // PROPERTY TAGS
         if (expand != null && expand.contains(RESTContentSpecV1.PROPERTIES_NAME)) {
             retValue.setProperties(
                     RESTDataObjectCollectionFactory.create(RESTAssignedPropertyTagCollectionV1.class, contentSpecPropertyTagFactory,
                             entity.getPropertyTagsList(), RESTTextContentSpecV1.PROPERTIES_NAME, dataType, expand, baseUrl,
                             revision, entityManager));
         }
 
         // TAGS
         if (expand != null && expand.contains(RESTContentSpecV1.TAGS_NAME)) {
             retValue.setTags(RESTDataObjectCollectionFactory.create(RESTTagCollectionV1.class, tagFactory, entity.getTags(),
                     RESTTextContentSpecV1.TAGS_NAME, dataType, expand, baseUrl, revision, entityManager));
         }
 
         // TRANSLATIONS
         if (expand != null && expand.contains(RESTContentSpecV1.TRANSLATED_CONTENT_SPECS_NAME)) {
             retValue.setTranslatedContentSpecs(
                     RESTDataObjectCollectionFactory.create(RESTTranslatedContentSpecCollectionV1.class, translatedContentSpecFactory,
                             entity.getTranslatedContentSpecs(entityManager, revision), RESTTextContentSpecV1.TRANSLATED_CONTENT_SPECS_NAME,
                             dataType, expand, baseUrl, entityManager));
         }
 
         retValue.setLinks(baseUrl, RESTv1Constants.CONTENT_SPEC_URL_NAME, dataType, retValue.getId());
 
         return retValue;
     }
 
     @Override
     public void syncDBEntityWithRESTEntityFirstPass(final ContentSpec entity, final RESTTextContentSpecV1 dataObject) {
         if (dataObject.hasParameterSet(RESTTextContentSpecV1.LOCALE_NAME)) entity.setLocale(dataObject.getLocale());
 
         // Many to Many
         if (dataObject.hasParameterSet(
                 RESTTextContentSpecV1.PROPERTIES_NAME) && dataObject.getProperties() != null && dataObject.getProperties().getItems() !=
                 null) {
             dataObject.getProperties().removeInvalidChangeItemRequests();
 
             for (final RESTAssignedPropertyTagCollectionItemV1 restEntityItem : dataObject.getProperties().getItems()) {
                 final RESTAssignedPropertyTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final ContentSpecToPropertyTag dbEntity = entityManager.find(ContentSpecToPropertyTag.class,
                             restEntity.getRelationshipId());
                     if (dbEntity == null) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());
 
                     entity.removePropertyTag(dbEntity);
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final ContentSpecToPropertyTag dbEntity = entityManager.find(ContentSpecToPropertyTag.class,
                             restEntity.getRelationshipId());
                     if (dbEntity == null) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());
                     if (!entity.getContentSpecToPropertyTags().contains(dbEntity)) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId() + " for" +
                                     " ContentSpec " + entity.getId());
 
                     contentSpecPropertyTagFactory.updateDBEntityFromRESTEntity(dbEntity, restEntity);
                 }
             }
         }
     }
 
     @Override
     public void syncDBEntityWithRESTEntitySecondPass(final ContentSpec entity, final RESTTextContentSpecV1 dataObject) {
         // Many to Many
         if (dataObject.hasParameterSet(
                 RESTContentSpecV1.PROPERTIES_NAME) && dataObject.getProperties() != null && dataObject.getProperties().getItems() != null) {
             dataObject.getProperties().removeInvalidChangeItemRequests();
 
             for (final RESTAssignedPropertyTagCollectionItemV1 restEntityItem : dataObject.getProperties().getItems()) {
                 final RESTAssignedPropertyTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem()) {
                     final PropertyTag dbEntity = RESTv1Utilities.findEntity(entityManager, entityCache, restEntity, PropertyTag.class);
                     if (dbEntity == null)
                         throw new BadRequestException("No PropertyTag entity was found with the primary key " + restEntity.getId());
 
                     entity.addPropertyTag(dbEntity, restEntity.getValue());
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final ContentSpecToPropertyTag dbEntity = entityManager.find(ContentSpecToPropertyTag.class,
                             restEntity.getRelationshipId());
                     if (dbEntity == null) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());
                     if (!entity.getContentSpecToPropertyTags().contains(dbEntity)) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId() + " for" +
                                     " ContentSpec " + entity.getId());
 
                     contentSpecPropertyTagFactory.syncDBEntityWithRESTEntitySecondPass(dbEntity, restEntity);
                 }
             }
         }
 
         // Many to Many
         if (dataObject.hasParameterSet(
                 RESTContentSpecV1.TAGS_NAME) && dataObject.getTags() != null && dataObject.getTags().getItems() != null) {
             dataObject.getTags().removeInvalidChangeItemRequests();
 
             // Remove Tags first to ensure mutual exclusion is done correctly
             for (final RESTTagCollectionItemV1 restEntityItem : dataObject.getTags().getItems()) {
                 final RESTTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final Tag dbEntity = entityManager.find(Tag.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     entity.removeTag(dbEntity);
                 }
             }
 
             for (final RESTTagCollectionItemV1 restEntityItem : dataObject.getTags().getItems()) {
                 final RESTTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem()) {
                     final Tag dbEntity = RESTv1Utilities.findEntity(entityManager, entityCache, restEntity, Tag.class);
                     if (dbEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     entity.addTag(dbEntity);
                 }
             }
         }
     }
 
     @Override
     protected Class<ContentSpec> getDatabaseClass() {
         return ContentSpec.class;
     }
 }
