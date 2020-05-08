 /*
  * Copyright 2012 Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.hadoop.yarn.server.resourcemanager.recovery;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import com.mysql.clusterj.ClusterJHelper;
 import com.mysql.clusterj.Query;
 import com.mysql.clusterj.Session;
 import com.mysql.clusterj.SessionFactory;
 import com.mysql.clusterj.annotation.PersistenceCapable;
 import com.mysql.clusterj.annotation.PrimaryKey;
 import com.mysql.clusterj.annotation.Lob;
 import com.mysql.clusterj.query.QueryBuilder;
 import com.mysql.clusterj.query.QueryDomainType;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
 import org.apache.hadoop.yarn.api.records.ApplicationId;
 import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
 import org.apache.hadoop.yarn.api.records.Container;
 import org.apache.hadoop.yarn.api.records.impl.pb.ApplicationAttemptIdPBImpl;
 import org.apache.hadoop.yarn.api.records.impl.pb.ApplicationIdPBImpl;
 import org.apache.hadoop.yarn.api.records.impl.pb.ApplicationSubmissionContextPBImpl;
 import org.apache.hadoop.yarn.api.records.impl.pb.ContainerPBImpl;
 import org.apache.hadoop.yarn.event.Dispatcher;
 import org.apache.hadoop.yarn.proto.YarnProtos;
 import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMApp;
 import org.apache.hadoop.yarn.server.resourcemanager.rmapp.attempt.RMAppAttempt;
 
 /**
  *
  * @author aknahs
  */
 public class NdbRMStateStore implements RMStateStore {
 
     Dispatcher dispatcher;
     private SessionFactory factory;
     private Session session;
 
     public NdbRMStateStore() {
         // Load the properties from the clusterj.properties file
 
         File propsFile = new File("src/test/java/org/apache/hadoop/yarn/server/resourcemanager/clusterj.properties");
         InputStream inStream;
         try {
             inStream = new FileInputStream(propsFile);
             Properties props = new Properties();
             props.load(inStream);
             // Create a session (connection to the database)
             factory = ClusterJHelper.getSessionFactory(props);
             session = factory.getSession();
         } catch (FileNotFoundException ex) {
             //TODO : Do better log
             Logger.getLogger(NdbRMStateStore.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(NdbRMStateStore.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     @Override
     public void setDispatcher(Dispatcher dispatcher) {
         this.dispatcher = dispatcher;
     }
 
     @Override
     public RMState loadState() {
         return new NdbRMState(); //TODO : needs arguments?
         //return state;
     }
 
     public class NdbRMState implements RMState {
 
         private HashMap<ApplicationId, ApplicationState> appState = null;
         //new HashMap<ApplicationId, ApplicationState>();
 
         public NdbRMState() {
             appState = new HashMap<ApplicationId, ApplicationState>();
             QueryDomainType<NdbApplicationStateCJ> domainApp;
             QueryDomainType<NdbAttemptStateCJ> domainAttempt;
             Query<NdbApplicationStateCJ> queryApp;
             Query<NdbAttemptStateCJ> queryAttempt;
             List<NdbApplicationStateCJ> resultsApp;
             List<NdbAttemptStateCJ> resultsAttempt;
 
 
             //Retrieve applicationstate table
             QueryBuilder builder = session.getQueryBuilder();
             domainApp = builder.createQueryDefinition(NdbApplicationStateCJ.class);
             domainAttempt = builder.createQueryDefinition(NdbAttemptStateCJ.class);
             queryApp = session.createQuery(domainApp);
             resultsApp = queryApp.getResultList();
 
             //Populate appState
             for (NdbApplicationStateCJ storedApp : resultsApp) {
                 try {
                     ApplicationId id = new ApplicationIdPBImpl();
                     id.setId(storedApp.getId());
                     id.setClusterTimestamp(storedApp.getClusterTimeStamp());
 
                     NdbApplicationState state = new NdbApplicationState();
                     state.appId = id;
                     state.submitTime = storedApp.getSubmitTime();
                     state.applicationSubmissionContext =
                             new ApplicationSubmissionContextPBImpl(
                             YarnProtos.ApplicationSubmissionContextProto.parseFrom(
                             storedApp.getAppContext()));
                     state.attempts = new HashMap<ApplicationAttemptId, NdbApplicationAttemptState>();
 
                     //Populate AppAttempState in each appState
                     //TODO : make sure name is case sensitive
                    domainAttempt.where(domainAttempt.get("ApplicationId").equal(domainAttempt.param("ApplicationId")));
                     queryAttempt = session.createQuery(domainAttempt);
                    queryAttempt.setParameter("ApplicationId",storedApp.getId());
                     resultsAttempt = queryAttempt.getResultList();
                     
                     for (NdbAttemptStateCJ storedAttempt : resultsAttempt) {
                         ApplicationAttemptId attemptId = new ApplicationAttemptIdPBImpl();
                         attemptId.setApplicationId(id);
                         attemptId.setAttemptId(storedAttempt.getAttemptId());
                         NdbApplicationAttemptState attemptState = new NdbApplicationAttemptState();
                         attemptState.masterContainer = new ContainerPBImpl(
                                 YarnProtos.ContainerProto.parseFrom(
                                 storedAttempt.getMasterContainer()));
                         state.attempts.put(attemptId, attemptState);
                     }
                     
                     appState.put(id, state);
                 } catch (InvalidProtocolBufferException ex) {
                     //TODO : Make a more beatiful exception!
                     Logger.getLogger(NdbRMStateStore.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
 
         @Override
         public Map<ApplicationId, ApplicationState> getApplicationState() {
             return appState; 
         }
     }
 
     @Override
     public void storeApplication(RMApp app) {
         // Create and initialise an NdbApplicationState
         NdbApplicationStateCJ storedApp =
                 session.newInstance(NdbApplicationStateCJ.class);
         storedApp.setId(app.getApplicationId().getId());
         storedApp.setClusterTimeStamp(app.getApplicationId().getClusterTimestamp());
         storedApp.setSubmitTime(app.getSubmitTime());
         byte[] context = ((ApplicationSubmissionContextPBImpl) app.getApplicationSubmissionContext()).getProto().toByteArray();
         storedApp.setAppContext(context);
 
         //Write NdbApplicationState to ndb database
         session.persist(storedApp);
     }
 
     @Override
     public void storeApplicationAttempt(RMAppAttempt appAttempt) {
         //TODO : check if the app is already in the ndb applicationstate table
         NdbAttemptStateCJ storedAttempt =
                 session.newInstance(NdbAttemptStateCJ.class);
         storedAttempt.setAttemptId(appAttempt.getAppAttemptId().getAttemptId());
         storedAttempt.setApplicationId(
                 appAttempt.getAppAttemptId().getApplicationId().getId());
         byte[] container = ((ContainerPBImpl) appAttempt.getMasterContainer()).getProto().toByteArray();
         storedAttempt.setMasterContainer(container);
 
         //Write NdbAttemptState to ndb database
         session.persist(storedAttempt);
     }
 
     public class NdbApplicationAttemptState implements ApplicationAttemptState {
 
         Container masterContainer;
 
         @Override
         public Container getMasterContainer() {
             return masterContainer;
         }
     }
 
     public class NdbApplicationState implements ApplicationState {
 
         ApplicationId appId;
         long submitTime;
         ApplicationSubmissionContext applicationSubmissionContext;
         HashMap<ApplicationAttemptId, NdbApplicationAttemptState> attempts =
                 new HashMap<ApplicationAttemptId, NdbApplicationAttemptState>();
 
         @Override
         public ApplicationId getId() {
             return appId;
         }
 
         @Override
         public long getSubmitTime() {
             return submitTime;
         }
 
         @Override
         public ApplicationSubmissionContext getApplicationSubmissionContext() {
             return applicationSubmissionContext;
         }
 
         @Override
         public int getAttemptCount() {
             return attempts.size();
         }
 
         @Override
         public ApplicationAttemptState getAttempt(ApplicationAttemptId attemptId) {
             NdbApplicationAttemptState attemptState = attempts.get(attemptId);
             return attemptState;
         }
     }
 
     @PersistenceCapable(table = "applicationstate")
     public interface NdbApplicationStateCJ {
 
         @PrimaryKey
         int getId();
         void setId(int id);
 
         long getClusterTimeStamp();
         void setClusterTimeStamp(long time);
 
         long getSubmitTime();
         void setSubmitTime(long time);
 	
 	@Lob
         byte[] getAppContext();
         void setAppContext(byte[] context);
     }
 
     @PersistenceCapable(table = "attemptstate")
     public interface NdbAttemptStateCJ {
 
         @PrimaryKey
         int getAttemptId();
         void setAttemptId(int id);
 
         @PrimaryKey
         int getApplicationId();
         void setApplicationId(int id);
         
         @Lob  
         byte[] getMasterContainer();
         void setMasterContainer(byte[] state);
     }
 }
