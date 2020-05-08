 package com.wixpress.testapp.domain;
 
 import com.google.appengine.api.datastore.*;
 
 import java.util.UUID;
 
 /**
  * Created by : doron
  * Since: 8/27/12
  */
 
 public class SampleAppDB extends SampleApp {
 
     private SampleAppDigester digester = new SampleAppDigester();
     private DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
 
     public SampleAppInstance addAppInstance(SampleAppInstance sampleAppInstance) {
         Entity entity = new Entity(SAMPLE_APP_INSTANCE, sampleAppInstance.getInstanceId().toString());
         entity.setProperty(BAGGAGE, digester.serializeSampleAppInstance(sampleAppInstance));
 
         Transaction transaction = dataStore.beginTransaction();
         try {
             dataStore.put(entity);
             transaction.commit();
         } finally {
             if (transaction.isActive()) {
                 transaction.rollback();
             }
         }
         return sampleAppInstance;
     }
 
     public SampleAppInstance addAppInstance(WixSignedInstance wixSignedInstance) {
         return addAppInstance(new SampleAppInstance(wixSignedInstance));
     }
 
     public SampleAppInstance getAppInstance(UUID instanceId) {
         if (instanceId == null)
             return null;
         else {
             final Key key = KeyFactory.createKey(SAMPLE_APP_INSTANCE, instanceId.toString());
             try {
                 final String baggage = dataStore.get(key).getProperty(BAGGAGE).toString();
                 return digester.deserializeSampleAppInstance(baggage);
             } catch (EntityNotFoundException e) {
                 e.printStackTrace();
                 return null;
             }
         }
     }
 
     public SampleAppInstance getAppInstance(WixSignedInstance wixSignedInstance) {
         return getAppInstance(wixSignedInstance.getInstanceId());
     }
 
     public void update(SampleAppInstance appInstance) {
         addAppInstance(appInstance);
     }
 }
