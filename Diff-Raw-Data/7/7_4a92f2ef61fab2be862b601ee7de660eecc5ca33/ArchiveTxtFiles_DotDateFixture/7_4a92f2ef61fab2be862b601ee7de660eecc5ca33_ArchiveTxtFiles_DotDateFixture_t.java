 package info.mikaelsvensson.ftpbackup.testfixture;
 
 import info.mikaelsvensson.ftpbackup.command.fileprocessing.FileProcessingStrategy;
 import info.mikaelsvensson.ftpbackup.command.jobactiongenerator.ArchivedMirrorJobActionsGeneratorStrategy;
 import info.mikaelsvensson.ftpbackup.command.jobactiongenerator.JobActionsGeneratorStrategy;
 import info.mikaelsvensson.ftpbackup.model.ArchiveFileNameTemplate;
 import info.mikaelsvensson.ftpbackup.model.ExpressionType;
 import info.mikaelsvensson.ftpbackup.model.JobType;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemBaseFolder;
 import info.mikaelsvensson.ftpbackup.model.impl.FileArchivingExpressionImpl;
 import info.mikaelsvensson.ftpbackup.util.FTPSession;
 import info.mikaelsvensson.ftpbackup.util.LocalFileSystemFolderReader;
 
 import java.io.File;
 import java.util.Calendar;
 
 public class ArchiveTxtFiles_DotDateFixture extends AbstractJobFixture {
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public ArchiveTxtFiles_DotDateFixture() {
         localBase = new FileSystemBaseFolder("",
                 File.separatorChar,
                 folder("home",
                         folder("alice"),
                         folder("bob",
                                 file("conf.properties")/* new */,
                                 file("notes.txt", SIZE_31)/* updated */,
                                 folder("folder",
                                         file("wishlist.txt", SIZE_11),
                                         file("todo.txt", SIZE_19)/* updated */))));
         FileSystemBaseFolder remoteBase = new FileSystemBaseFolder("",
                 FTPSession.FTP_PATH_SEPARATOR_CHAR,
                 folder("home",
                         folder("bob",
                                 file("notes.txt", SIZE_2),
                                 folder("folder",
                                         file("wishlist.txt", SIZE_11),
                                         file("todo.txt", SIZE_17)))));
         FileSystemBaseFolder expectedRemoteBase = new FileSystemBaseFolder("",
                 FTPSession.FTP_PATH_SEPARATOR_CHAR,
                 folder("home",
                         folder("bob",
                                 file("conf.properties"),
                                 file("notes.txt", SIZE_31),
                                 file("notes.txt.20121231-234559", SIZE_2),
                                 folder("folder",
                                         file("wishlist.txt", SIZE_11),
                                         file("todo.txt", SIZE_19),
                                         file("todo.txt.20121231-234559", SIZE_17)))));
         destinationFixtures = new JobDestinationFixture[]{
                 new JobDestinationFixtureImpl(
                         mockFtpDestination(new String[]{"home", "bob"}, null),
                         remoteBase,
                         expectedRemoteBase,
                         null,
                         moveFileAction("/home/bob/folder/todo.txt", "/home/bob/folder/todo.txt.20121231-234559"),
                        uploadFileAction(localBase, "" + File.separatorChar + "home" + File.separatorChar + "bob" + File.separatorChar + "folder" + File.separatorChar + "todo.txt", remoteBase, "/home/bob/folder"),
                         moveFileAction("/home/bob/notes.txt", "/home/bob/notes.txt.20121231-234559"),
                        uploadFileAction(localBase, "" + File.separatorChar + "home" + File.separatorChar + "bob" + File.separatorChar + "notes.txt", remoteBase, "/home/bob"),
                        uploadFileAction(localBase, "" + File.separatorChar + "home" + File.separatorChar + "bob" + File.separatorChar + "conf.properties", remoteBase, "/home/bob")
                         )};
         job = mockJob(
                 new String[]{"home", "bob"},
                 null,
                 null,
                 new FileArchivingExpressionImpl(ExpressionType.REGULAR_EXPRESSION, ".*\\.txt", ArchiveFileNameTemplate.DOT_DATE, 1, 1),
                 null,
                 JobType.ARCHIVED_MIRROR,
                 destinationFixtures[0].getDestination());
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface JobFixture ---------------------
 
     @Override
     public Calendar getTestingDate() {
         return cal("2012-12-31 23:45:59");
     }
 
     @Override
     public JobActionsGeneratorStrategy getJobActionGenerator(final FileProcessingStrategy fps, final LocalFileSystemFolderReader localFileSystemFolderReader) {
         return new ArchivedMirrorJobActionsGeneratorStrategy(job.getFileSet().getFileArchivingExpressions(), fps, localFileSystemFolderReader);
     }
 }
