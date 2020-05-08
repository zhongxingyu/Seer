 package org.jboss.pressgang.ccms.server.rest.v1;
 
 import javax.persistence.EntityManager;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.pressgang.ccms.model.File;
 import org.jboss.pressgang.ccms.model.LanguageFile;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTFileCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTLanguageFileCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTFileCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTLanguageFileCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTFileV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTLanguageFileV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectCollectionFactory;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectFactory;
 import org.jboss.pressgang.ccms.server.rest.v1.utils.RESTv1Utilities;
 import org.jboss.pressgang.ccms.server.utils.EnversUtilities;
 import org.jboss.resteasy.spi.BadRequestException;
 
 public class FileV1Factory extends RESTDataObjectFactory<RESTFileV1, File, RESTFileCollectionV1, RESTFileCollectionItemV1> {
     private final LanguageFileV1Factory languageFileFactory = new LanguageFileV1Factory();
 
     public FileV1Factory() {
         super(File.class);
     }
 
     @Override
     public RESTFileV1 createRESTEntityFromDBEntityInternal(final File entity, final String baseUrl, final String dataType,
             final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences, final EntityManager entityManager) {
         assert entity != null : "Parameter entity can not be null";
         assert baseUrl != null : "Parameter baseUrl can not be null";
 
         final RESTFileV1 retValue = new RESTFileV1();
 
         final List<String> expandOptions = new ArrayList<String>();
         expandOptions.add(RESTFileV1.LANGUAGE_FILES_NAME);
         expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
         if (revision == null) {
             expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
         }
         retValue.setExpand(expandOptions);
 
         retValue.setId(entity.getFileId());
         retValue.setDescription(entity.getDescription());
         retValue.setFileName(entity.getFileName());
         retValue.setFilePath(entity.getFilePath());
 
         // REVISIONS
         if (revision == null && expand != null && expand.contains(RESTBaseEntityV1.REVISIONS_NAME)) {
             retValue.setRevisions(
                     new RESTDataObjectCollectionFactory<RESTFileV1, File, RESTFileCollectionV1, RESTFileCollectionItemV1>().create(
                             RESTFileCollectionV1.class, new FileV1Factory(), entity, EnversUtilities.getRevisions(entityManager, entity),
                             RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl, entityManager));
         }
 
         // LANGUAGE IMAGES
         if (expand != null && expand.contains(RESTFileV1.LANGUAGE_FILES_NAME)) {
             retValue.setLanguageFiles_OTM(
                     new RESTDataObjectCollectionFactory<RESTLanguageFileV1, LanguageFile, RESTLanguageFileCollectionV1,
                             RESTLanguageFileCollectionItemV1>().create(
                             RESTLanguageFileCollectionV1.class, new LanguageFileV1Factory(), entity.getLanguageFilesArray(),
                             RESTFileV1.LANGUAGE_FILES_NAME, dataType, expand, baseUrl, false, entityManager));
         }
 
         retValue.setLinks(baseUrl, RESTv1Constants.FILE_URL_NAME, dataType, retValue.getId());
 
         return retValue;
     }
 
     @Override
     public void syncDBEntityWithRESTEntityFirstPass(final EntityManager entityManager,
             Map<RESTBaseEntityV1<?, ?, ?>, AuditedEntity> newEntityCache, final File entity, final RESTFileV1 dataObject) {
         if (dataObject.hasParameterSet(RESTFileV1.DESCRIPTION_NAME)) entity.setDescription(dataObject.getDescription());
 
         /* One To Many - Add will create a child entity */
         if (dataObject.hasParameterSet(
                 RESTFileV1.LANGUAGE_FILES_NAME) && dataObject.getLanguageFiles_OTM() != null && dataObject.getLanguageFiles_OTM()
                 .getItems() != null) {
             dataObject.getLanguageFiles_OTM().removeInvalidChangeItemRequests();
 
             for (final RESTLanguageFileCollectionItemV1 restEntityItem : dataObject.getLanguageFiles_OTM().getItems()) {
                 final RESTLanguageFileV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem()) {
                     final LanguageFile dbEntity = languageFileFactory.createDBEntityFromRESTEntity(entityManager, newEntityCache,
                             restEntity);
                     entity.addLanguageFile(dbEntity);
                 } else if (restEntityItem.returnIsRemoveItem()) {
                     final LanguageFile dbEntity = entityManager.find(LanguageFile.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No LanguageFile entity was found with the primary key " + restEntity.getId());
 
                     entity.removeLanguageFile(dbEntity);
                     entityManager.remove(dbEntity);
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final LanguageFile dbEntity = entityManager.find(LanguageFile.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No LanguageFile entity was found with the primary key " + restEntity.getId());
                     if (!entity.getLanguageFiles().contains(dbEntity)) throw new BadRequestException(
                             "No LanguageFile entity was found with the primary key " + restEntity.getId() + " for File " + entity.getId());
 
                     languageFileFactory.syncDBEntityWithRESTEntityFirstPass(entityManager, newEntityCache, dbEntity, restEntity);
                 }
             }
         }
 
         entityManager.persist(entity);
     }
 
     @Override
     public void syncDBEntityWithRESTEntitySecondPass(final EntityManager entityManager,
             Map<RESTBaseEntityV1<?, ?, ?>, AuditedEntity> newEntityCache, final File entity, final RESTFileV1 dataObject) {
 
         // One To Many - Do the second pass on update or added items
         if (dataObject.hasParameterSet(
                 RESTFileV1.LANGUAGE_FILES_NAME) && dataObject.getLanguageFiles_OTM() != null && dataObject.getLanguageFiles_OTM()
                 .getItems() != null) {
             dataObject.getLanguageFiles_OTM().removeInvalidChangeItemRequests();
 
             for (final RESTLanguageFileCollectionItemV1 restEntityItem : dataObject.getLanguageFiles_OTM().getItems()) {
                 final RESTLanguageFileV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem() || restEntityItem.returnIsUpdateItem()) {
                     final LanguageFile dbEntity = RESTv1Utilities.findEntity(entityManager, newEntityCache, restEntity, LanguageFile.class);
                     if (dbEntity == null)
                         throw new BadRequestException("No LanguageFile entity was found with the primary key " + restEntity.getId());
 
                     languageFileFactory.syncDBEntityWithRESTEntitySecondPass(entityManager, newEntityCache, dbEntity, restEntity);
                 }
             }
         }
     }
 }
