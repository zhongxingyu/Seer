 // Copyright (C) 2004, 2005, 2006 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.engine.agent;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.Random;
 
 import net.grinder.common.Logger;
 import net.grinder.common.LoggerStubFactory;
 import net.grinder.communication.CommunicationException;
 import net.grinder.communication.Message;
 import net.grinder.communication.MessageDispatchSender;
 import net.grinder.communication.SimpleMessage;
 import net.grinder.engine.messages.ClearCacheMessage;
 import net.grinder.engine.messages.DistributeFileMessage;
 import net.grinder.testutility.AbstractFileTestCase;
 import net.grinder.testutility.FileUtilities;
 import net.grinder.util.Directory;
 import net.grinder.util.FileContents;
 
 
 /**
  *  Unit tests for <code>FileStore</code>.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestFileStore extends AbstractFileTestCase {
 
   public void testConstruction() throws Exception {
 
     File.createTempFile("file", "", getDirectory());
     assertEquals(1, getDirectory().list().length);
 
     final FileStore fileStore = new FileStore(getDirectory(), null);
     final File currentDirectory = fileStore.getDirectory().getFile();
     assertNotNull(currentDirectory);
 
     assertTrue(
       currentDirectory.getPath().startsWith(getDirectory().getPath()));
 
     // No messages have been received, so no physical directories will
     // have been created yet.
 
     assertEquals(1, getDirectory().list().length);
     assertTrue(!currentDirectory.exists());
 
     // Can't use a plain file.
     final File file1 = File.createTempFile("file", "", getDirectory());
 
     try {
       new FileStore(file1, null);
       fail("Expected FileStoreException");
     }
     catch (FileStore.FileStoreException e) {
     }
 
     // Nor a directory that contains a plain file clashing with one
     // of the subdirectory names.
     file1.delete();
     file1.mkdir();
     new File(file1, "current").createNewFile();
 
     try {
       new FileStore(file1, null);
       fail("Expected FileStoreException");
     }
     catch (FileStore.FileStoreException e) {
     }
 
     // Can't use a read-only directory.
     final File readOnlyDirectory = new File(getDirectory(), "directory");
     readOnlyDirectory.mkdir();
     readOnlyDirectory.setReadOnly();
 
     try {
       new FileStore(readOnlyDirectory, null);
       fail("Expected FileStoreException");
     }
     catch (FileStore.FileStoreException e) {
     }
 
     // Perfectly fine to create a FileStore around a directory that
     // doens't yet exist.
     final File notThere = new File(getDirectory(), "notThere");
     new FileStore(notThere, null);
   }
 
   public void testSender() throws Exception {
 
     final LoggerStubFactory loggerStubFactory = new LoggerStubFactory();
     final Logger logger = loggerStubFactory.getLogger();
 
     final FileStore fileStore = new FileStore(getDirectory(), logger);
 
     final MessageDispatchSender messageDispatcher = new MessageDispatchSender();
     fileStore.registerMessageHandlers(messageDispatcher);
 
     // Other Messages get ignored.
     final Message message0 = new SimpleMessage();
     messageDispatcher.send(message0);
     loggerStubFactory.assertNoMoreCalls();
 
     // Shutdown does nothing.
     messageDispatcher.shutdown();
     loggerStubFactory.assertNoMoreCalls();
 
     // Test with a good message.
     final File sourceDirectory = new File(getDirectory(), "source");
     sourceDirectory.mkdirs();
 
     final File file0 = new File(sourceDirectory, "dir/file0");
     file0.getParentFile().mkdirs();
     final OutputStream outputStream = new FileOutputStream(file0);
     final byte[] bytes = new byte[500];
     new Random().nextBytes(bytes);
     outputStream.write(bytes);
     outputStream.close();
 
     final FileContents fileContents0 =
       new FileContents(sourceDirectory, new File("dir/file0"));
 
     final File readmeFile = new File(getDirectory(), "README.txt");
     final File incomingDirectoryFile = new File(getDirectory(), "incoming");
     final File currentDirectoryFile = new File(getDirectory(), "current");
     assertEquals(currentDirectoryFile, fileStore.getDirectory().getFile());
 
     // Before message sent, none of our files or directories exist.
     assertTrue(!readmeFile.exists());
     assertTrue(!incomingDirectoryFile.exists());
     assertTrue(!currentDirectoryFile.exists());
 
     final Message message1 = new DistributeFileMessage(fileContents0);
 
     // Can't receive a DFM if the incoming directory can't be created.
     FileUtilities.setCanAccess(getDirectory(), false);
 
     try {
       messageDispatcher.send(message1);
       fail("Expected CommunicationException");
     }
     catch (CommunicationException e) {
     }
 
     FileUtilities.setCanAccess(getDirectory(), true);
 
     //loggerStubFactory.assertSuccess("output", new Class[] { String.class });
     loggerStubFactory.assertSuccess("error", new Class[] { String.class });
     loggerStubFactory.assertNoMoreCalls();
 
     incomingDirectoryFile.delete();
 
     messageDispatcher.send(message1);
     loggerStubFactory.assertSuccess("output", new Class[] { String.class });
     loggerStubFactory.assertNoMoreCalls();
 
     // Message has been sent, the incoming directory and the read me exist.
     assertTrue(readmeFile.exists());
     assertTrue(incomingDirectoryFile.exists());
     assertTrue(!currentDirectoryFile.exists());
 
     final File targetFile = new File(incomingDirectoryFile, "dir/file0");
     assertTrue(targetFile.canRead());
 
     assertEquals(currentDirectoryFile, fileStore.getDirectory().getFile());
 
     // Now getDirectory() has been called, both directories exist.
     assertTrue(readmeFile.exists());
     assertTrue(incomingDirectoryFile.exists());
     assertTrue(currentDirectoryFile.exists());
 
     // Frig with currentDirectory so that getDirectory() fails.
     new Directory(currentDirectoryFile).deleteContents();
     currentDirectoryFile.delete();
     currentDirectoryFile.createNewFile();
 
     try {
       fileStore.getDirectory();
       fail("Expected FileStoreException");
     }
     catch (FileStore.FileStoreException e) {
     }
 
     // Put things back again.
     currentDirectoryFile.delete();
     fileStore.getDirectory();
 
     // Test with a bad message.
     targetFile.setReadOnly();
 
     try {
       messageDispatcher.send(message1);
       fail("Expected CommunicationException");
     }
     catch (CommunicationException e) {
     }
 
     loggerStubFactory.assertSuccess("output", new Class[] { String.class });
     loggerStubFactory.assertSuccess("error", new Class[] { String.class });
     loggerStubFactory.assertNoMoreCalls();
 
     final Message message2 = new ClearCacheMessage();
 
     FileUtilities.setCanAccess(targetFile, false);
    // UNIX: Permission to remove a file is set on directory.
    FileUtilities.setCanAccess(targetFile.getParentFile(), false);
 
     try {
       messageDispatcher.send(message2);
       fail("Expected CommunicationException");
     }
     catch (CommunicationException e) {
     }
 
    FileUtilities.setCanAccess(targetFile.getParentFile(), true);
     FileUtilities.setCanAccess(targetFile, true);
 
     loggerStubFactory.assertSuccess("output", new Class[] { String.class });
     loggerStubFactory.assertSuccess("error", new Class[] { String.class });
     loggerStubFactory.assertNoMoreCalls();
 
     messageDispatcher.send(message2);
     loggerStubFactory.assertSuccess("output", new Class[] { String.class });
     loggerStubFactory.assertNoMoreCalls();
 
     assertTrue(!targetFile.canRead());
 
     assertEquals(currentDirectoryFile, fileStore.getDirectory().getFile());
   }
 
   public void testFileStoreException() throws Exception {
     final Exception nested = new Exception("");
     final FileStore.FileStoreException e =
       new FileStore.FileStoreException("bite me", nested);
 
     assertEquals(nested, e.getCause());
   }
 }
