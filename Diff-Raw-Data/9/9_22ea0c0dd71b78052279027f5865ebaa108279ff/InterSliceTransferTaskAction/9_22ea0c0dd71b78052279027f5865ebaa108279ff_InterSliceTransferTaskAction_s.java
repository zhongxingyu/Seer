 package de.zib.gndms.taskflows.interslicetransfer.server.logic;
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 import de.zib.gndms.common.dspace.service.SliceInformation;
 import de.zib.gndms.common.kit.security.CustomSSLContextRequestFactory;
 import de.zib.gndms.common.kit.security.SetupSSL;
 import de.zib.gndms.common.rest.Specifier;
 import de.zib.gndms.common.rest.UriFactory;
 import de.zib.gndms.gndmc.dspace.SliceClient;
 import de.zib.gndms.gndmc.dspace.SubspaceClient;
 import de.zib.gndms.infra.GridConfig;
 import de.zib.gndms.logic.model.gorfx.TaskFlowAction;
 import de.zib.gndms.logic.model.gorfx.taskflow.UnsatisfiableOrderException;
 import de.zib.gndms.model.gorfx.types.DelegatingOrder;
 import de.zib.gndms.model.gorfx.types.TaskState;
 import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.common.Session;
 import de.zib.gndms.neomodel.gorfx.Task;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import de.zib.gndms.taskflows.filetransfer.client.model.FileTransferResult;
 import de.zib.gndms.taskflows.filetransfer.server.logic.FileTransferTaskAction;
 import de.zib.gndms.taskflows.interslicetransfer.client.InterSliceTransferMeta;
 import de.zib.gndms.taskflows.interslicetransfer.client.model.InterSliceTransferOrder;
 import de.zib.gndms.taskflows.interslicetransfer.client.model.InterSliceTransferResult;
 import org.jetbrains.annotations.NotNull;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.web.client.RestTemplate;
 
 import javax.inject.Inject;
 import javax.net.ssl.SSLContext;
 import javax.persistence.EntityManager;
 import java.util.LinkedList;
 
 /**
  * @author  try ma ik jo rr a zib
  * @version  $Id$
  * <p/>
  * User: mjorra, Date: 04.11.2008, Time: 17:45:47
  */
 public class InterSliceTransferTaskAction extends TaskFlowAction<InterSliceTransferOrder> {
 
 
     private GridConfig config;
     private HttpMessageConverter messageConverter;
     
     private SetupSSL setupSSL;
     private RestTemplate restTemplate;
 
     private SliceClient sliceClient;
     private SubspaceClient subspaceClient;
 
 
     public InterSliceTransferTaskAction() {
         super( InterSliceTransferMeta.INTER_SLICE_TRANSFER_KEY );
     }
 
 
     public InterSliceTransferTaskAction(@NotNull EntityManager em, @NotNull Dao dao, @NotNull Taskling model) {
         super(InterSliceTransferMeta.INTER_SLICE_TRANSFER_KEY, em, dao, model);
     }
 
 
     @Override
     public Class<InterSliceTransferOrder> getOrderBeanClass() {
 
         return InterSliceTransferOrder.class;
     }
 
 
     @Override
     protected void onCreated( @NotNull final String wid, @NotNull final TaskState state,
                               final boolean isRestartedTask, final boolean altTaskState )
             throws Exception
     {
 
         final Session session = getDao().beginSession();
         try {
             final Task task = getTask( session );
             task.setPayload( new InterSliceTransferResult() );
             session.success();
         } finally { session.finish(); } 
         super.onCreated( wid, state, isRestartedTask,
                 altTaskState );    // overriden method implementation
     }
 
 
     // TODO: make this method resumable
     @Override
     protected void onInProgress(@NotNull String wid, @NotNull TaskState state,
                                 boolean isRestartedTask, boolean altTaskState) throws Exception {
 
         ensureOrder();
         InterSliceTransferQuoteCalculator.prepareSourceUrl( getOrder(), sliceClient );
         prepareDestination( );
         
         // check for quotas
         {
             final InterSliceTransferOrder order = getOrderBean();
             if( null != order.getSourceSlice() && null != order.getDestinationSpecifier() ) {
 
                 final ResponseEntity< SliceInformation > sourceSliceConfigResponse
                         = sliceClient.getSliceInformation( order.getSourceSlice(), getOrder().getDNFromContext() );
                 final ResponseEntity< SliceInformation > destinationSliceConfigResponse
                         = sliceClient.getSliceInformation( order.getDestinationSpecifier(), getOrder().getDNFromContext() );
                final long sliceSize = Long.parseLong(
                        destinationSliceConfigResponse.getHeaders().get( "totalStorageSize" ).get( 0 ) );
                final long sliceUsage = Long.parseLong(
                        destinationSliceConfigResponse.getHeaders().get( "DiskUsage" ).get( 0 ) );
                final long needSize = Long.parseLong(
                        sourceSliceConfigResponse.getHeaders().get( "DiskUsage" ).get( 0 ) );
 
                 if( sliceUsage + needSize > sliceSize )
                     throw new IllegalStateException(
                             "Transfer would exceed slice size: Need "
                                     + String.valueOf( needSize )
                                     + " Bytes but have only "
                                     + String.valueOf( sliceSize - sliceUsage )
                                     + " Bytes left." );
             }
         }
 
         Session session = getDao().beginSession();
         final String subTaskId = getUUIDGen().nextUUID();
         try {
             final Task task = getTask(session);
 
             final Task st = task.createSubTask();
             st.setId(subTaskId);
             st.setPayload( null );
 
             st.setTerminationTime( task.getTerminationTime() );
             session.success();
         }
         finally { session.finish(); }
 
         session = getDao().beginSession();
         final FileTransferTaskAction fta;
         try {
             final Task task = getTask(session);
 
             final Task st = session.findTask( subTaskId );
 	        fta = new FileTransferTaskAction( getEmf().createEntityManager(),
                     getDao(), st.getTaskling() );
             getInjector().injectMembers( fta );
 
             fta.setCredentialProvider( getCredentialProvider() );
             fta.setEmf( getEmf( ) );
 
             session.success();
         }
         finally { session.finish(); }
 
         fta.call( );
 
         session = getDao().beginSession();
         try {
             final Task task = getTask(session);
 
             final Task st = session.findTask(subTaskId);
             if( st.getTaskState().equals( TaskState.FINISHED ) ){
                 //noinspection ConstantConditions
                 final InterSliceTransferResult payload =
                         ( InterSliceTransferResult ) task.getPayload();
                 payload.populate( ( FileTransferResult ) st.getPayload() );
                 task.setPayload( payload );
                 task.setTaskState(TaskState.FINISHED);
                 if (altTaskState)
                     task.setAltTaskState(null);
             }
             else
                 throw new RuntimeException( "FileTransfer did not finish: ", st.getCause().getFirst() );
             session.success();
         }
         finally { session.finish(); }
     }
 
 
     private void prepareDestination() {
 
         final DelegatingOrder<InterSliceTransferOrder> order = getOrder();
         final InterSliceTransferOrder orderBean = order.getOrderBean();
         
         if( orderBean.getDestinationURI() != null )
             return;
 
         Specifier<Void> sliceSpecifier;
         final Specifier<Void> specifier = orderBean.getDestinationSpecifier();
 
         if( null == specifier.getUriMap() )
             throw new UnsatisfiableOrderException( "No UriMap given in destination specifier" );
 
         if( specifier.getUriMap().containsKey( UriFactory.SLICE ) ) {
             // we got a slice specifier
             sliceSpecifier = specifier;
         } else {
             // must be a slice kind specifier
             // lets create a new slice
             ResponseEntity<Specifier<Void>> sliceSpecifierResponse =
                     subspaceClient.createSlice( specifier, order.getDNFromContext() );
 
             if( HttpStatus.CREATED.equals( sliceSpecifierResponse.getStatusCode() ) ) {
                 sliceSpecifier = sliceSpecifierResponse.getBody();
                 orderBean.setDestinationSpecifier( sliceSpecifier );
             } else
                 throw new IllegalStateException( "Can't create slice in: " + specifier.getUrl() );
         }
 
         // fetch grid-ftp uri
         orderBean.setDestinationURI( getGsiFtpUrl( order, sliceSpecifier ) );
 
         // update task in data-base
         Session  session = getDao().beginSession();
         try {
             Task task = getTask( session );
             task.setOrder(order);
             //noinspection ConstantConditions
             final InterSliceTransferResult payload = (InterSliceTransferResult) task.getPayload();
             payload.setSliceSpecifier( sliceSpecifier );
             task.setPayload( payload );
             session.success();
         } finally { session.finish(); }
     }
 
 
 
     private String getGsiFtpUrl( final DelegatingOrder<InterSliceTransferOrder> order,
                                  final Specifier<Void> specifier )
     {
 
         final ResponseEntity<String> responseEntity =
                 sliceClient.getGridFtpUrl( specifier,  order.getDNFromContext() );
         if( HttpStatus.OK.equals( responseEntity.getStatusCode() ) )
             return  responseEntity.getBody();
         else 
             throw new IllegalStateException( "Can't fetch gridFTP URL for slice: " +
                                              specifier.getUrl() );
     }
 
 
     public SliceClient getSliceClient() {
 
         return sliceClient;
     }
 
 
     public void prepareSliceClient( ) {
 
         if( null != sliceClient )
             return;
 
         try {
             sliceClient = new SliceClient( config.getBaseUrl() );
         } catch( Exception e ) {
             logger.error( "Could not get service URL. Please contact system administrator.", e );
         }
         
         sliceClient.setRestTemplate( restTemplate );
     }
 
 
     public SubspaceClient getSubspaceClient() {
 
         return subspaceClient;
     }
 
 
     public void prepareSubspaceClient( ) {
 
         if( null != subspaceClient )
             return;
 
         try {
             subspaceClient = new SubspaceClient( config.getBaseUrl() );
         } catch( Exception e ) {
             logger.error( "Could not get service URL. Please contact system administrator.", e );
         }
 
         subspaceClient.setRestTemplate( restTemplate );
     }
 
 
     @SuppressWarnings("SpringJavaAutowiringInspection")
     @Inject
     public void setConfig( GridConfig config ) {
         this.config = config;
     }
 
 
     public void setMessageConverter( HttpMessageConverter messageConverter ) {
         this.messageConverter = messageConverter;
     }
 
 
     @SuppressWarnings("SpringJavaAutowiringInspection")
     @Inject
     public void setSetupSSL( SetupSSL setupSSL ) {
         this.setupSSL = setupSSL;
     }
 
     public void prepareRestTemplate( String keyPassword ) {
         SSLContext sslContext = null;
 
         try {
             sslContext = setupSSL.setupSSLContext( keyPassword );
         } catch (Exception e) {
             throw new IllegalStateException( "Could not setup SSL context.", e );
         }
 
         CustomSSLContextRequestFactory requestFactory = new CustomSSLContextRequestFactory( sslContext );
         restTemplate = new RestTemplate( requestFactory );
 
         restTemplate.setMessageConverters( new LinkedList< HttpMessageConverter<?> >(){{ add( messageConverter ); }} );
     }
 }
