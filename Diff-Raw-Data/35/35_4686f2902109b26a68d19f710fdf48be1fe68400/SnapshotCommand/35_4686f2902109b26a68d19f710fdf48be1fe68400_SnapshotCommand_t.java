 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.FutureTask;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommandImpl;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.processor.SnapshotProcessor;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.SnapshotOptions;
 import org.jboss.pressgang.ccms.contentspec.utils.CSTransformer;
 import org.jboss.pressgang.ccms.contentspec.utils.ContentSpecUtilities;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.LogMessageProvider;
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.provider.TextContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.exception.ProviderException;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTLogDetailsV1;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.TextCSProcessingOptionsWrapper;
 import org.jboss.pressgang.ccms.wrapper.TextContentSpecWrapper;
 
 @Parameters(
         commandDescription = "Pull a revision of a content specification that represents a snapshot in time and push it back to the " +
                 "server.")
 public class SnapshotCommand extends BaseCommandImpl {
     @Parameter(metaVar = "[ID]")
     private List<Integer> ids = new ArrayList<Integer>();
 
     @Parameter(names = {Constants.REVISION_LONG_PARAM, Constants.REVISION_SHORT_PARAM})
     private Integer revision = null;
 
     @Parameter(names = {Constants.UPDATE_LONG_PARAM}, description = "Update all current revisions when pulling down the snapshot.")
     private Boolean update = false;
 
     @Parameter(names = {Constants.NEW_LONG_PARAM}, description = "Create the snapshot as a new content specification")
     private Boolean createNew = false;
 
     @Parameter(names = {Constants.MESSAGE_LONG_PARAM, Constants.MESSAGE_SHORT_PARAM},
             description = "A commit message about what was " + "changed.")
     private String message = null;
 
     @Parameter(names = Constants.REVISION_MESSAGE_FLAG_LONG_PARAMETER,
             description = "The commit message should be set to be included in " + "the Revision History.")
     private Boolean revisionHistoryMessage = false;
 
     private SnapshotProcessor processor = null;
 
     public SnapshotCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig) {
         super(parser, cspConfig, clientConfig);
     }
 
     protected SnapshotProcessor getProcessor() {
         return processor;
     }
 
     protected void setProcessor(final SnapshotProcessor processor) {
         this.processor = processor;
     }
 
     @Override
     public String getCommandName() {
         return Constants.SNAPSHOT_COMMAND_NAME;
     }
 
     public List<Integer> getIds() {
         return ids;
     }
 
     public void setIds(final List<Integer> ids) {
         this.ids = ids;
     }
 
     public Integer getRevision() {
         return revision;
     }
 
     public void setRevision(final Integer revision) {
         this.revision = revision;
     }
 
     public Boolean getUpdate() {
         return update;
     }
 
     public void setUpdate(final Boolean update) {
         this.update = update;
     }
 
     public Boolean getCreateNew() {
         return createNew;
     }
 
     public void setCreateNew(Boolean createNew) {
         this.createNew = createNew;
     }
 
     public Boolean getRevisionHistoryMessage() {
         return revisionHistoryMessage;
     }
 
     public void setRevisionHistoryMessage(Boolean revisionHistoryMessage) {
         this.revisionHistoryMessage = revisionHistoryMessage;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     @Override
     public void process() {
         final ContentSpecProvider contentSpecProvider = getProviderFactory().getProvider(ContentSpecProvider.class);
 
         // Load the ids and validate that one and only one exists
         ClientUtilities.prepareAndValidateIds(this, getCspConfig(), getIds());
 
         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         boolean success = false;
 
         // Get the topic from the rest interface
         final ContentSpecWrapper contentSpecEntity = contentSpecProvider.getContentSpec(getIds().get(0), getRevision());
         if (contentSpecEntity == null) {
             printErrorAndShutdown(Constants.EXIT_FAILURE,
                     getRevision() == null ? Constants.ERROR_NO_ID_FOUND_MSG : Constants.ERROR_NO_REV_ID_FOUND_MSG, false);
         }
 
        // Check that the content spec isn't a failed one
        if (contentSpecEntity.getFailed() != null) {
            printErrorAndShutdown(Constants.EXIT_FAILURE, Constants.ERROR_INVALID_CONTENT_SPEC, false);
        }

         // Good point to check for a shutdown
         allowShutdownToContinueIfRequested();
 
         // Transform the content spec
         final ContentSpec contentSpec = CSTransformer.transform(contentSpecEntity, getProviderFactory());
         final String originalChecksum = ContentSpecUtilities.getContentSpecChecksum(contentSpec);
 
         // If we want to create it as a new spec then remove the checksum and id
         if (getCreateNew()) {
             contentSpec.setId(null);
             contentSpec.setChecksum(null);
         }
 
         // Setup the processing options
         final SnapshotOptions snapshotOptions = new SnapshotOptions();
         snapshotOptions.setAddRevisions(true);
         snapshotOptions.setUpdateRevisions(getUpdate());
         snapshotOptions.setRevision(getRevision());
 
         // Create the snapshot
         setProcessor(new SnapshotProcessor(getProviderFactory()));
         getProcessor().processContentSpec(contentSpec, snapshotOptions);
 
         // Process and save the updated content spec.
         final TextContentSpecWrapper output = processAndSaveContentSpec(getProviderFactory(), contentSpec, getUsername(), originalChecksum);
         success = output != null && output.getFailed() == null;
 
         if (!success) {
             JCommander.getConsole().println(output.getErrors());
             JCommander.getConsole().println(Constants.ERROR_CREATE_SNAPSHOT_INVALID);
             JCommander.getConsole().println("");
             shutdown(Constants.EXIT_TOPIC_INVALID);
         } else {
             JCommander.getConsole().println(Constants.SUCCESSFUL_PUSH_SNAPSHOT_MSG);
             JCommander.getConsole().println(String.format(Constants.SUCCESSFUL_PUSH_MSG, output.getId(), output.getRevision()));
         }
 
     }
 
     /**
      * Process a content specification and save it to the server.
      *
      * @param providerFactory  The provider factory to create providers to lookup entity details.
      * @param contentSpec      The content spec to be processed and saved.
      * @param username         The user who requested the content spec be processed and saved.
      * @param originalChecksum The content specs original checksum value.
      * @return True if the content spec was processed and saved successfully, otherwise false.
      */
     protected TextContentSpecWrapper processAndSaveContentSpec(final RESTProviderFactory providerFactory, final ContentSpec contentSpec,
             final String username, final String originalChecksum) {
         final TextContentSpecProvider textContentSpecProvider = providerFactory.getProvider(TextContentSpecProvider.class);
         final TextCSProcessingOptionsWrapper processingOptions = textContentSpecProvider.newTextProcessingOptions();
         processingOptions.setStrictTitles(true);
 
         // Create the task to update the content spec on the server
         final FutureTask<TextContentSpecWrapper> task = new FutureTask<TextContentSpecWrapper>(new Callable<TextContentSpecWrapper>() {
             @Override
             public TextContentSpecWrapper call() throws Exception {
                 int flag = 0;
                 if (getRevisionHistoryMessage()) {
                     flag = 0 | RESTLogDetailsV1.MAJOR_CHANGE_FLAG_BIT;
                 } else {
                     flag = 0 | RESTLogDetailsV1.MINOR_CHANGE_FLAG_BIT;
                 }
                 final LogMessageWrapper logMessage = providerFactory.getProvider(LogMessageProvider.class).createLogMessage();
                 logMessage.setFlags(flag);
                 logMessage.setMessage(getMessage());
                 logMessage.setUser(username);
 
                 // Create the input object to be saved
 
                 TextContentSpecWrapper output = null;
                 try {
                     final TextContentSpecWrapper input = textContentSpecProvider.newTextContentSpec();
                     if (getCreateNew()) {
                         contentSpec.setId(null);
                         contentSpec.setChecksum(null);
                         input.setText(contentSpec.toString());
                         output = textContentSpecProvider.createTextContentSpec(input, processingOptions, logMessage);
                     } else {
                         input.setText(ContentSpecUtilities.replaceChecksum(contentSpec.toString(), originalChecksum));
                         input.setId(contentSpec.getId());
                         output = textContentSpecProvider.updateTextContentSpec(input, processingOptions, logMessage);
                     }
                 } catch (ProviderException e) {
                     output = textContentSpecProvider.newTextContentSpec();
                     output.setErrors(e.getMessage());
                     output.setFailed(contentSpec.toString());
                 }
 
                 return output;
             }
         });
 
         return ClientUtilities.saveContentSpec(this, task);
     }
 
     @Override
     public boolean loadFromCSProcessorCfg() {
         return getIds().size() == 0;
     }
 
     @Override
     public boolean requiresExternalConnection() {
         return true;
     }
 
     @Override
     public void shutdown() {
         // No need to wait as the ShutdownInterceptor is waiting
         // on the whole program.
         if (getProcessor() != null) {
             getProcessor().shutdown();
         }
 
         super.shutdown();
     }
 }
