 // Copyright (C) 2004 Philip Aston
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
 
 import net.grinder.common.Logger;
 import net.grinder.engine.common.EngineException;
 import net.grinder.communication.CommunicationException;
 import net.grinder.communication.Message;
 import net.grinder.communication.Sender;
 import net.grinder.engine.messages.ClearCacheMessage;
 import net.grinder.engine.messages.DistributeFileMessage;
 import net.grinder.util.Directory;
 import net.grinder.util.FileContents;
 
 
 /**
  * Process {@link ClearCacheMessage}s and {@link
  * DistributeFileMessage}s received from the console.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class FileStore {
 
   private final Logger m_logger;
 
   private final Directory m_incomingDirectory;
   private final Directory m_currentDirectory;
   private boolean m_incremental;
 
   public FileStore(File directory, Logger logger) throws FileStoreException {
 
     final File rootDirectory = directory.getAbsoluteFile();
     m_logger = logger;
 
     if (rootDirectory.exists()) {
       if (!rootDirectory.isDirectory()) {
         throw new FileStoreException(
           "Could not write to directory '" + rootDirectory +
           "' as file with that name already exists");
       }
 
       if (!rootDirectory.canWrite()) {
         throw new FileStoreException(
           "Could not write to directory '" + rootDirectory + "'");
       }
     }
 
     try {
       m_incomingDirectory = new Directory(new File(rootDirectory, "incoming"));
       m_currentDirectory = new Directory(new File(rootDirectory, "current"));
     }
     catch (Directory.DirectoryException e) {
       throw new FileStoreException(e.getMessage(), e);
     }
 
     m_incremental = false;
   }
 
   public Directory getDirectory() throws FileStoreException {
     try {
       synchronized (m_incomingDirectory) {
         m_incomingDirectory.copyTo(m_currentDirectory, m_incremental);
       }
 
       m_incremental = true;
 
       return m_currentDirectory;
     }
    catch (Directory.DirectoryException e) {
       throw new FileStoreException("Could not create file store directory", e);
     }
   }
 
   public Sender getSender(final Sender delegate) {
 
     return new Sender() {
         public void send(Message message) throws CommunicationException {
           if (message instanceof ClearCacheMessage) {
             m_logger.output("Clearing file store");
 
             try {
               synchronized (m_incomingDirectory) {
                 m_incomingDirectory.create();
                 m_incomingDirectory.deleteContents();
               }
             }
             catch (Directory.DirectoryException e) {
               throw new CommunicationException(e.getMessage(), e);
             }
 
             m_incremental = false;
           }
           else if (message instanceof DistributeFileMessage) {
             try {
               synchronized (m_incomingDirectory) {
                 m_incomingDirectory.create();
 
                 final FileContents fileContents =
                   ((DistributeFileMessage)message).getFileContents();
 
                 m_logger.output("Updating file store: " + fileContents);
 
                 fileContents.create(m_incomingDirectory);
               }
             }
             catch (FileContents.FileContentsException e) {
               m_logger.error(e.getMessage());
               throw new CommunicationException(e.getMessage(), e);
             }
             catch (Directory.DirectoryException e) {
               m_logger.error(e.getMessage());
               throw new CommunicationException(e.getMessage(), e);
             }
           }
           else {
             delegate.send(message);
           }
         }
 
         public void shutdown() {
           delegate.shutdown();
         }
       };
   }
 
   /**
    * Exception that indicates a <code>FileStore</code> related
    * problem.
    */
   public static final class FileStoreException extends EngineException {
     FileStoreException(String message) {
       super(message);
     }
 
     FileStoreException(String message, Throwable e) {
       super(message, e);
     }
   }
 }
