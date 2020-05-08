 package org.docear.syncdaemon.fileindex;
 
 import java.util.List;
 
 import org.docear.syncdaemon.fileactors.Messages.FileChangedLocally;
 import org.docear.syncdaemon.indexdb.IndexDbService;
 import org.docear.syncdaemon.indexdb.PersistenceException;
 import org.docear.syncdaemon.indexdb.h2.H2IndexDbService;
 import org.docear.syncdaemon.projects.Project;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import akka.actor.ActorRef;
 import akka.actor.UntypedActor;
 
 public class FileIndexServiceImpl extends UntypedActor implements
         FileIndexService {
     private static final Logger logger = LoggerFactory.getLogger(FileIndexServiceImpl.class);
 
     private ActorRef recipient;
     private Project project;
     private final IndexDbService indexDbService;
 
     public FileIndexServiceImpl() {
         indexDbService = new H2IndexDbService();
     }
 
     public void onReceive(Object message) throws Exception {
         if (message instanceof StartScanMessage) {
             this.project = ((StartScanMessage) message).getProject();
             this.recipient = ((StartScanMessage) message).getFileChangeActor();
             scanProject();
         }
     }
 
     @Override
     public void scanProject() {
         try {
             long localRev = indexDbService.getProjectRevision(project.getId());
 
             if (localRev != project.getRevision()) {
                 List<FileMetaData> files = FileReceiver.receiveFiles(project);
 
                 // TODO user credentials required to use clientService
                 for (FileMetaData fmdFromScan : files) {
                     final FileMetaData fmdFromIndexDb = indexDbService.getFileMetaData(fmdFromScan);
                     if (fmdFromScan.isChanged(fmdFromIndexDb)) {
                        // TODO is this case relevant?
                    	//sendConflictMesage(fmdFromScan);
                     } else {
                        sendFileChangedMessage(fmdFromScan);
                     }
                 }
             }
         } catch (PersistenceException e) {
             logger.error("can't scan projects", e);
         }
     }
 
     private void sendFileChangedMessage(final FileMetaData fmd) {
     	final FileChangedLocally message = new FileChangedLocally(this.project, fmd);
         recipient.tell(message, recipient);
     }
 
 }
