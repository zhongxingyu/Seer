 /**
  * Copyright (c) 2008, Damian Carrillo
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted 
  * provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice, this list of 
  *     conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
  *     conditions and the following disclaimer in the documentation and/or other materials 
  *     provided with the distribution.
  *   * Neither the name of the copyright holder's organization nor the names of its contributors 
  *     may be used to endorse or promote products derived from this software without specific 
  *     prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package agave.samples.pastebin.snippet;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Arrays;
 
 import org.apache.commons.lang.RandomStringUtils;
 
 /**
  * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
  */
 public class FilesystemSnippetRepository implements SnippetRepository {
 
     private static final int ID_LENGTH = 7;
     private File repositoryDir;
 
     public FilesystemSnippetRepository(File repositoryDir) {
        if (!repositoryDir.exists()) {
            repositoryDir.mkdirs();
        }
        
         if (!repositoryDir.canRead()) {
             throw new IllegalArgumentException("Can not read from repository directory: "
                     + repositoryDir.getPath());
         }
         if (!repositoryDir.canWrite()) {
             throw new IllegalArgumentException("Can not write to repository directory: "
                     + repositoryDir.getPath());
         }
         if (!repositoryDir.isDirectory()) {
             throw new IllegalArgumentException("Repository directory, " + repositoryDir.getPath()
                     + " is not an actual directory");
         }
 
         this.repositoryDir = repositoryDir;
     }
 
     public String generateUniqueId(Timeframe expiration) {
         String attempt = null;
         synchronized (repositoryDir) {
             do {
                 attempt = RandomStringUtils.randomAlphanumeric(ID_LENGTH);
             } while (Arrays.asList(repositoryDir.list()).contains(attempt));
         }
         return attempt;
     }
 
     public String determineUniqueId(Timeframe expiration) {
         String uniqueId = null;
         uniqueId = generateUniqueId(expiration);
         File snippetDir = new File(repositoryDir, uniqueId);
         snippetDir.mkdirs();
         return uniqueId;
     }
 
     public void storeSnippet(Snippet snippet) throws IOException {
         File snippetDir = new File(repositoryDir, snippet.getUniqueId());
         
         long currentRevision = getCurrentRevision(snippetDir);
         currentRevision++;
         
         File snippetFile = new File(snippetDir, String.valueOf(currentRevision));
         snippetFile.createNewFile();
         ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(snippetFile));
         out.writeObject(snippet);
         out.close();
     }
     
     private long getCurrentRevision(File snippetDir) {
         Long currentRevision = 0l;
         for (String entry : snippetDir.list()) {
             if (Long.parseLong(entry) > currentRevision) {
                 currentRevision = Long.parseLong(entry);
             }
         }
         return currentRevision;
     }
 
     public Snippet retrieveSnippet(String snippetId) throws IOException, ClassNotFoundException {
         File snippetDir = new File(repositoryDir, snippetId);
         long currentRevision = getCurrentRevision(snippetDir);
         return retrieveSnippet(snippetId, currentRevision);
     }
     
     public Snippet retrieveSnippet(String snippetId, long revision) throws IOException, ClassNotFoundException {
         File snippetDir = new File(repositoryDir, snippetId);
         File snippetFile = new File(snippetDir, String.valueOf(revision));
         ObjectInputStream in = new ObjectInputStream(new FileInputStream(snippetFile));
         Snippet snippet = (Snippet)in.readObject();
         return snippet;
     }
 
 }
