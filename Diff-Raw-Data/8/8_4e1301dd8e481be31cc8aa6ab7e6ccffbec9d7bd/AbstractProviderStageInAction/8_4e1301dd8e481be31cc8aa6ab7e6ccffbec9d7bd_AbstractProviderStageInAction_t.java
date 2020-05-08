 package de.zib.gndms.taskflows.staging.server.logic;
 
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
 
 
 import de.zib.gndms.common.dspace.service.SubspaceService;
 import de.zib.gndms.common.rest.Specifier;
 import de.zib.gndms.common.rest.UriFactory;
 import de.zib.gndms.kit.config.ConfigProvider;
 import de.zib.gndms.kit.config.MandatoryOptionMissingException;
 import de.zib.gndms.kit.config.MapConfig;
 import de.zib.gndms.kit.security.AsFileCredentialInstaller;
 import de.zib.gndms.kit.security.CredentialProvider;
 import de.zib.gndms.kit.security.GetCredentialProviderFor;
 import de.zib.gndms.logic.action.ProcessBuilderAction;
 import de.zib.gndms.logic.model.ModelIdHoldingOrder;
 import de.zib.gndms.logic.model.dspace.ChownSliceConfiglet;
 import de.zib.gndms.logic.model.dspace.DeleteSliceTaskAction;
 import de.zib.gndms.logic.model.dspace.SliceConfiguration;
 import de.zib.gndms.logic.model.gorfx.TaskFlowAction;
 import de.zib.gndms.model.common.PersistentContract;
 import de.zib.gndms.model.dspace.Slice;
 import de.zib.gndms.model.gorfx.types.DelegatingOrder;
 import de.zib.gndms.model.gorfx.types.TaskState;
 import de.zib.gndms.model.util.TxFrame;
 import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.common.Session;
 import de.zib.gndms.neomodel.gorfx.Task;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import de.zib.gndms.taskflows.staging.client.ProviderStageInMeta;
 import de.zib.gndms.taskflows.staging.client.model.ProviderStageInOrder;
 import de.zib.gndms.taskflows.staging.client.model.ProviderStageInResult;
 import org.jetbrains.annotations.NotNull;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import java.io.File;
 import java.io.Serializable;
 
 
 /**
  * ThingAMagic.
  *
  * @author  try ste fan pla nti kow zib
  * @version $Id$
  *
  *          User: stepn Date: 02.10.2008 Time: 15:04:39
  */
 @SuppressWarnings({ "FeatureEnvy" })
 public abstract class AbstractProviderStageInAction extends TaskFlowAction<ProviderStageInOrder> {
 
     public static final String PROXY_FILE_NAME = File.separator + "x509_proxy.pem";
     public static final long DEFAULT_SLICE_SIZE = 50*1000*1024;
 
 	protected StagingIOFormatHelper stagingIOHelper = new StagingIOFormatHelper();
     private SubspaceService subspaceService;
 
 
     protected AbstractProviderStageInAction( String offerTypeId ) {
         super( offerTypeId );
     }
 
 
     public AbstractProviderStageInAction(@NotNull EntityManager em, @NotNull Dao dao, @NotNull Taskling model) {
         super(em, dao, model);
     }
 
 
     @Override
     protected void onCreated(@NotNull String wid,
                              @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState) throws Exception {
         ensureOrder();
         MapConfig config = getOfferTypeConfig();
 	    getScriptFileByParam(config, "stagingCommand");
         createNewSlice();
         super.onCreated(wid, state, isRestartedTask, altTaskState);
     }
 
 
     @Override
     protected void onInProgress(@NotNull String wid, @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState) throws Exception {
         ensureOrder();
         final Slice slice = findSlice();
         doStaging(getOfferTypeConfig(), getOrderBean(), slice);
       //  changeSliceOwner( slice ) ;
         transitWithPayload( createResult(), TaskState.FINISHED );
         //super.onInProgress(wid, state, isRestartedTask, altTaskState);
     }
 
 
     protected abstract void doStaging(
         final MapConfig offerTypeConfigParam, final ProviderStageInOrder orderParam,
         final Slice sliceParam );
 
 
     private ProviderStageInResult createResult() {
 
         Session session = getDao().beginSession();
         Specifier<Void> sliceSpec;
         try {
             sliceSpec = ( Specifier<Void> ) getTask(session).getPayload();
             session.success();
         } finally {
             session.finish();
         }
 
         return new ProviderStageInResult( sliceSpec );
     }
 
 
     @Override
     protected void onFailed(@NotNull String wid, @NotNull TaskState state, boolean isRestartedTask, boolean altTaskState) throws Exception {
         try {
             cancelStaging();
         }
         finally {
             killSlice();
             super.onFailed(wid, state, isRestartedTask, altTaskState);
         }
     }
 
 
 	@SuppressWarnings({ "NoopMethodInAbstractClass" })
 	protected void callCancel(final MapConfig offerTypeConfigParam, final ProviderStageInOrder orderParam,
             final File sliceParam) {
 		// Implement in subclass
 	}
 
 	protected void cancelStaging() {
 		final Slice slice;
 		final File sliceDir;
 
 		final EntityManager em = getEntityManager();
 		final TxFrame txf = new TxFrame(em);
 		try {
 			slice = findSlice();
 			if (slice == null)
 				// MAYBE log this somewhere?
 				return;
 			sliceDir = new File(slice.getSubspace().getPathForSlice(slice));
 			txf.commit();
 		}
 		finally { txf.finish(); }
 		// potentially unsafe but do not want to keep the transaction open
 		// we just dont change the slice dir after creation
 		callCancel(getOfferTypeConfig(), getOrderBean(), sliceDir);
 	}
 
 
     protected void prepareProxy( Slice slice ) {
         //final Slice slice = findSlice();
        try {
            File sd = new File( slice.getSubspace().getPathForSlice( slice ) + PROXY_FILE_NAME );
            getCredentialProvider().installCredentials( sd );
        } catch ( Exception e ) {
            logger.debug( "couldn't deploy credentials: ", e );
        }
     }
 
 
     protected boolean removeProxy( final String s ) {
         File f = new File( s );
         return f.exists() && f.delete();
     }
 
 
     protected  void changeSliceOwner( Slice slice ) {
 
         ChownSliceConfiglet csc = getConfigletProvider().getConfiglet( ChownSliceConfiglet.class, "sliceChown" );
 
         if( csc == null )
             throw new IllegalStateException( "chown configlet is null!");
 
         final DelegatingOrder<?> order = getOrder();
         String dn = order.getDNFromContext();
         getLogger().debug( "cso DN: " + dn );
         getLogger().debug( "changing owner of " + slice.getId() + " to " + order.getLocalUser() );
         ProcessBuilderAction chownAct = csc.createChownSliceAction( order.getLocalUser(),
             slice.getSubspace().getPath() + File.separator + slice.getKind().getSliceDirectory(),
             slice.getDirectoryId() );
         chownAct.call();
     }
 
 
     private void createNewSlice() throws MandatoryOptionMissingException {
 
         final ConfigProvider config = getOfferTypeConfig();
 
         final String subspaceUrl = config.getOption( "subspace" );
         String sliceKindKey = config.getOption( "sliceKind" );
 
 
         SliceConfiguration sconf = new SliceConfiguration();
         sconf.setTerminationTime( getContract().getResultValidity() );
         sconf.setSize( DEFAULT_SLICE_SIZE );
 
         ResponseEntity<Specifier<Void>> sliceSpec =
                 subspaceService.createSlice( subspaceUrl, sliceKindKey, sconf.getStringRepresentation(),
                         getOrder().getDNFromContext() );
 
         if (! HttpStatus.CREATED.equals( sliceSpec.getStatusCode() ) )
             throw new IllegalStateException( "Slice creation failed" );
 
         setSliceSpecifier( sliceSpec.getBody() );
 
         // to provoke nasty test condition uncomment the following line
         //throw new NullPointerException( );
 	    getLogger().info( "createNewSlice() = " + getSliceId() );
     }
 
 
     private Slice findSlice() {
 	    final String sliceId = getSliceId();
 	    getLogger().info( "findSlice(" + ( sliceId == null ? "null" : '"' + sliceId + '"' ) + ')' );
 	    if (sliceId == null)
 		    return null;
 
 
 	    final EntityManager em = getEntityManager();
 	    final TxFrame txf = new TxFrame(em);
 	    try {
 		    final Slice slice = em.find(Slice.class, sliceId);
 	        txf.commit();
 	        return slice;
 	    }
 	    finally { txf.finish();  }
     }
 
 
     protected void deleteSlice( final String sliceId ) {
 
         final DeleteSliceTaskAction deleteSliceTaskAction = new DeleteSliceTaskAction();
         getInjector().injectMembers( deleteSliceTaskAction );
         getService().submitTaskAction( deleteSliceTaskAction,
                 new ModelIdHoldingOrder( sliceId ), getWid() );
     }
 
 
 	private void killSlice() {
         deleteSlice( getSliceId() );
 	}
 
 
     private String getWid() {
 
         Session session = getDao().beginSession();
         try {
             String wid = getTask( session ).getWID();
             session.success();
             return wid;
         } finally {session.finish();}
     }
 
 
 	protected ProcessBuilder createProcessBuilder(String name, File dir) {
        try {
            MapConfig opts = new MapConfig( getTaskFlowTypeConfigMapData());
 		   if (opts.getOption(name, "").trim().length() == 0)
 			   return null;
 		   final File fileOption = getScriptFileByParam(opts, name);
 		   ProcessBuilder builder = new ProcessBuilder();
 			   builder.directory(dir);
 		   builder.command(fileOption.getPath());
 			   return builder;
        }
        catch (MandatoryOptionMissingException e) {
            throw new RuntimeException("createProcessBuilder " + (name==null?"null":name) + " " + (dir==null?"(null)":dir), e);
        }
    }
 
 
 	@SuppressWarnings({ "ThrowableInstanceNeverThrown" })
 	protected @NotNull File getScriptFileByParam(final MapConfig configParam, String scriptParam)
             throws MandatoryOptionMissingException {
 
 		final @NotNull File scriptFile = configParam.getFileOption( scriptParam );
 		if (! isValidScriptFile(scriptFile))
 		    throw new IllegalArgumentException("Invalid " + scriptParam + " script: " + scriptFile.getPath());
 		return scriptFile;
 	}
 
 	@Override
     @NotNull
     public Class<ProviderStageInOrder> getOrderBeanClass( ) {
         return ProviderStageInOrder.class;
     }
 
 
     @Override
     public CredentialProvider getCredentialProvider() {
 
         CredentialProvider credentialProvider = new GetCredentialProviderFor( getOrder(),
                 ProviderStageInMeta.REQUIRED_AUTHORIZATION.get( 0 ), getMyProxyFactoryProvider()
         ).invoke();
         credentialProvider.setInstaller( new AsFileCredentialInstaller() );
         return  credentialProvider;
     }
 
 
     protected @NotNull
     MapConfig getOfferTypeConfig() {
         return new MapConfig( getTaskFlowTypeConfigMapData());
     }
 
 
 
     protected void setSliceSpecifier( Specifier<Void> sliceId ) {
         final Session session = getDao().beginSession();
         try {
             setSliceSpecifier( sliceId, session );
             session.success();
         }
         finally {
             session.finish();
         }
     }
 
 
     private void setSliceSpecifier( final Specifier<Void> sliceId, final Session session ) {
 
         final Task task = getTask( session );
         task.setPayload( sliceId );
     }
 
 
     protected String getSliceId( ) {
 
         // maybe cache the slice id
         final Session session = getDao().beginSession();
         String sliceId;
         try {
             final Task task = getTask(session);
             Specifier<Void> sliceSpec = ( Specifier<Void> ) task.getPayload();
             sliceId = sliceSpec.getUriMap().get( UriFactory.SLICE );
             session.success();
         }
         finally {
             session.finish();
         }
 
         return sliceId;
     }
 
 	@SuppressWarnings({ "MethodWithMoreThanThreeNegations" })
 	public static boolean isValidScriptFile(final File scriptFile) {
 		// do we need "r"?
 		return scriptFile != null &&
 			   scriptFile.exists() && scriptFile.canRead() && scriptFile.isFile();
 	}
 
     public PersistentContract getContract() {
         final Session session = getDao().beginSession();
         try {
             final Task task = getTask(session);
             final PersistentContract ret = task.getContract();
             session.finish();
             return ret;
         }
         finally {
             session.success();
         }
     }
 
 
     public SubspaceService getSubspaceService() {
 
         return subspaceService;
     }
 
 
     @Inject
     public void setSubspaceService( final SubspaceService subspaceService ) {
 
         this.subspaceService = subspaceService;
     }
 }
 
