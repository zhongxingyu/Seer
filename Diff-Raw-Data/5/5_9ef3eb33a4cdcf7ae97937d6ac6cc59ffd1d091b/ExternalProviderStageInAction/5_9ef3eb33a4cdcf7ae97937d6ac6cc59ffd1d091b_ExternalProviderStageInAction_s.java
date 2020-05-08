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
 
 
 
 import de.zib.gndms.kit.config.MapConfig;
 import de.zib.gndms.logic.action.ProcessBuilderAction;
 import de.zib.gndms.model.dspace.Slice;
 import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import de.zib.gndms.stuff.Sleeper;
 import de.zib.gndms.taskflows.staging.client.ProviderStageInMeta;
 import de.zib.gndms.taskflows.staging.client.model.ProviderStageInOrder;
 import org.jetbrains.annotations.NotNull;
 
 import javax.persistence.EntityManager;
 import java.io.File;
 
 import static de.zib.gndms.taskflows.staging.server.logic.ExternalProviderStageInQuoteCalculator.GLOBUS_DEATH_DURATION;
 
 
 /**
  * ThingAMagic.
  *
  * @author  try ste fan pla nti kow zib
  * @version $Id$
  *
  *          User: stepn Date: 27.10.2008 Time: 13:13:09
  */
 @SuppressWarnings({ "ThrowableInstanceNeverThrown" })
 public class ExternalProviderStageInAction extends AbstractProviderStageInAction {
 	private static final int INITIAL_STRING_BUILDER_CAPACITY = 4096;
 
 
 	public ExternalProviderStageInAction() {
         super( ProviderStageInMeta.PROVIDER_STAGING_KEY );
 	    stagingIOHelper = new StagingIOFormatHelper();
     }
 
 
     public ExternalProviderStageInAction(@NotNull EntityManager em, @NotNull Dao dao, @NotNull Taskling model) {
         super( ProviderStageInMeta.PROVIDER_STAGING_KEY, em, dao, model);
         stagingIOHelper = new StagingIOFormatHelper();
     }
 
 
     @SuppressWarnings({ "HardcodedLineSeparator", "MagicNumber" })
     @Override
     protected void doStaging(
             final MapConfig offerTypeConfigParam, final ProviderStageInOrder orderParam,
             final Slice sliceParam) {
 
         stagingIOHelper.formatFromMap( getOfferTypeConfig() );
 
         prepareProxy( sliceParam );
 	    final File sliceDir = new File(sliceParam.getSubspace().getPathForSlice(sliceParam));
         final ProcessBuilder procBuilder = createProcessBuilder("stagingCommand", sliceDir);
 	    if (procBuilder == null) {
 	        fail(new IllegalStateException("No stagingCommand configured"));
             return;
         }
 
         procBuilder.environment().put( "X509_USER_PROXY", sliceDir + PROXY_FILE_NAME );
 
         final StringBuilder outRecv = new StringBuilder(INITIAL_STRING_BUILDER_CAPACITY);
         final StringBuilder errRecv = new StringBuilder(INITIAL_STRING_BUILDER_CAPACITY);
 
         final ProcessBuilderAction action = stagingIOHelper.createPBAction( orderParam, null, actualPermissions() );
         action.setProcessBuilder(procBuilder);
         action.setOutputReceiver(outRecv);
         action.setErrorReceiver(errRecv);
 
         int result = action.call();
         removeProxy( sliceDir + PROXY_FILE_NAME );
         switch (result) {
             case 0:
                 getLogger().debug("Staging completed: " + outRecv.toString());
             //  this is now done in super.inProgress
             //    transitWithPayload(new ProviderStageInResult(sliceParam.getId()),
             //        TaskState.FINISHED);
                 break;
             default:
                 if (result > 127) {
                     getLogger().debug( "Waiting for potential death of container..." );
                     Sleeper.sleepUninterruptible(GLOBUS_DEATH_DURATION);
                 }
                String log = "Staging failed! Staging script returned unexpected exit code: " + result +
                         "\nScript output was:\n" + errRecv.toString();
 
                 // trace( log, null ) ;
                 throw new IllegalStateException( log );
         }
     }
 
 
     @Override
 	protected void callCancel(final MapConfig offerTypeConfigParam,
 	                          final ProviderStageInOrder orderParam,
                               final File sliceDir) {
 		final ProcessBuilder procBuilder = createProcessBuilder("cancelCommand", sliceDir);
 		if (procBuilder == null)
 			// MAYBE log this somewhere
 			return;
 
 		final StringBuilder outRecv = new StringBuilder(INITIAL_STRING_BUILDER_CAPACITY);
 		final StringBuilder errRecv = new StringBuilder(INITIAL_STRING_BUILDER_CAPACITY);
 		final ProcessBuilderAction action = stagingIOHelper.createPBAction( orderParam, null, actualPermissions() );
 		action.setProcessBuilder(procBuilder);
 		action.setOutputReceiver(outRecv);
 		action.setErrorReceiver(errRecv);
 		int result = action.call();
 		switch (result) {
 			case 0:
 				getLogger().debug( "Finished calling cancel: " + outRecv.toString() );
 				break;
 			default:
 				getLogger().info( "Failure during cancel: " + errRecv.toString() );
 		}
     }
 }
