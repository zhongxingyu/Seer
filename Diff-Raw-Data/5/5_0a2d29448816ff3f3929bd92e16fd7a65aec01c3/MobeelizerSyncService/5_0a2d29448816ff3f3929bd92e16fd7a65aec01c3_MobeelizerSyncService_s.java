 package com.mobeelizer.java;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mobeelizer.java.api.MobeelizerFile;
 import com.mobeelizer.java.api.MobeelizerModel;
 import com.mobeelizer.java.connection.MobeelizerConnectionService;
 import com.mobeelizer.java.definition.MobeelizerErrorsHolder;
 import com.mobeelizer.java.model.MobeelizerModelImpl;
 import com.mobeelizer.java.sync.MobeelizerInputData;
 import com.mobeelizer.java.sync.MobeelizerJsonEntity;
 import com.mobeelizer.java.sync.MobeelizerOutputData;
 
 class MobeelizerSyncService {
 
     private static final Logger logger = LoggerFactory.getLogger(MobeelizerSyncService.class);
 
     private final MobeelizerConnectionService connectionService;
 
     private final Map<Class<?>, MobeelizerModelImpl> definitionByClass = new HashMap<Class<?>, MobeelizerModelImpl>();
 
     private final Map<String, MobeelizerModelImpl> definitionByName = new HashMap<String, MobeelizerModelImpl>();
 
     public MobeelizerSyncService(final Set<MobeelizerModel> definition, final MobeelizerConnectionService connectionService) {
         for (MobeelizerModel model : definition) {
             definitionByClass.put(model.getMappingClass(), (MobeelizerModelImpl) model);
             definitionByName.put(model.getName(), (MobeelizerModelImpl) model);
         }
         this.connectionService = connectionService;
     }
 
     public void syncAll(final MobeelizerSyncCallback callback) {
         sync(null, null, callback, true);
     }
 
     public void sync(final Iterable<Object> entities, final Iterable<MobeelizerFile> files, final MobeelizerSyncCallback callback) {
         sync(entities, files, callback, false);
     }
 
     private void sync(final Iterable<Object> outputEntities, final Iterable<MobeelizerFile> outputFiles,
             final MobeelizerSyncCallback callback, final boolean isAllSynchronization) {
         final MobeelizerInputData inputData;
 
         File outputFile = null;
         File inputFile = null;
 
         try {
             final String ticket;
 
             if (isAllSynchronization) {
                 ticket = connectionService.sendSyncAllRequest();
             } else {
                 outputFile = File.createTempFile("mobeelizer", "sync");
 
                 MobeelizerErrorsHolder errors = new MobeelizerErrorsHolder();
 
                 prepareOutputFile(outputFile, outputEntities, outputFiles, errors);
 
                 if (!errors.isValid()) {
                     callback.onSyncFinishedWithError(errors);
                     return;
                 }
 
                 ticket = connectionService.sendSyncDiffRequest(outputFile);
             }
 
             if (!connectionService.waitUntilSyncRequestComplete(ticket)) {
                 callback.onSyncFinishedWithError(new IllegalStateException("Cannot get status of synchronization."));
                 return;
             }
 
             inputFile = connectionService.getSyncData(ticket);
 
             inputData = new MobeelizerInputData(new FileInputStream(inputFile), File.createTempFile("mobeelizer", "input"));
 
             callback.onSyncFinishedWithSuccess(prepareInputEntitiesIterator(inputData), prepareInputFileIterator(inputData),
                     inputData.getDeletedFiles(), new MobeelizerConfirmSyncCallback() {
 
                         @Override
                         public void confirm() {
                             try {
                                 connectionService.confirmTask(ticket);
                             } catch (IOException e) {
                                 logger.warn("Cannot confirm task: " + e.getMessage(), e);
                             } finally {
                                 if (inputData != null) {
                                     inputData.close();
                                 }
                             }
                         }
 
                     });
         } catch (IOException e) {
             callback.onSyncFinishedWithError(e);
         } finally {
             if (outputFile != null && !outputFile.delete()) {
                 logger.warn("Cannot delete file " + outputFile.getAbsolutePath());
             }
             if (inputFile != null && !inputFile.delete()) {
                 logger.warn("Cannot delete file " + inputFile.getAbsolutePath());
             }
         }
     }
 
     private Iterable<MobeelizerFile> prepareInputFileIterator(final MobeelizerInputData inputData) {
         List<MobeelizerFile> files = new ArrayList<MobeelizerFile>();
 
         for (final String file : inputData.getFiles()) {
             files.add(new MobeelizerFile() {
 
                 @Override
                 public String getName() {
                     return null;
                 }
 
                 @Override
                 public InputStream getInputStream() {
                     try {
                         return inputData.getFile(file);
                     } catch (IOException e) {
                         throw new IllegalStateException(e.getMessage(), e);
                     }
                 }
 
                 @Override
                 public String getGuid() {
                     return file;
                 }
 
             });
         }
 
         return files;
     }
 
     private MobeelizerModelImpl getModel(final Class<?> clazz) {
         if (!definitionByClass.containsKey(clazz)) {
             throw new IllegalStateException("Class " + clazz.getCanonicalName() + " not mapped.");
         }
         return definitionByClass.get(clazz);
     }
 
     private MobeelizerModelImpl getModel(final String name) {
         if (!definitionByName.containsKey(name)) {
             throw new IllegalStateException("Class for model " + name + " not mapped.");
         }
         return definitionByName.get(name);
     }
 
     private Iterable<Object> prepareInputEntitiesIterator(final MobeelizerInputData inputData) {
         List<Object> entities = new ArrayList<Object>();
 
         for (MobeelizerJsonEntity entity : inputData.getInputData()) {
             entities.add(getModel(entity.getModel()).getEntityFromJsonEntity(entity));
         }
 
         return entities;
     }
 
     private void prepareOutputFile(final File outputFile, final Iterable<Object> entities, final Iterable<MobeelizerFile> files,
             final MobeelizerErrorsHolder errors) {
         MobeelizerOutputData outputData = null;
 
         try {
             outputData = new MobeelizerOutputData(outputFile, File.createTempFile("mobeelizer", "output"));
 
             for (Object entity : entities) {
                 outputData.writeEntity(getModel(entity.getClass()).getJsonEntityFromEntity(entity, errors));
             }
 
             for (MobeelizerFile file : files) {
                 outputData.writeFile(file.getGuid(), file.getInputStream());
             }
 
         } catch (IOException e) {
             throw new IllegalStateException(e.getMessage(), e);
         } finally {
             if (outputData != null) {
                 outputData.close();
             }
         }
     }
 
 }
