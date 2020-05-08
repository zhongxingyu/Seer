 // Copyright (C) 2004, 2005 Philip Aston
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
 
 package net.grinder.console.editor;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import net.grinder.console.common.ConsoleException;
 import net.grinder.console.common.Resources;
 import net.grinder.console.distribution.AgentCacheState;
 import net.grinder.console.distribution.FileChangeWatcher;
 import net.grinder.console.distribution.FileChangeWatcher.FileChangedListener;
 import net.grinder.util.ListenerSupport;
 
 
 /**
  * Editor model.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public final class EditorModel {
 
   private final Resources m_resources;
   private final TextSource.Factory m_textSourceFactory;
   private final AgentCacheState m_agentCacheState;
   private final Buffer m_defaultBuffer;
 
   private final ListenerSupport m_listeners = new ListenerSupport();
 
   private final LinkedList m_bufferList = new LinkedList();
   private final Map m_fileBuffers = Collections.synchronizedMap(new HashMap());
 
   private int m_nextNewBufferNameIndex = 0;
 
   private Buffer m_selectedBuffer;
   private File m_markedScript;
 
   /**
    * Constructor.
    *
    * @param resources ResourcesImplementation.
    * @param textSourceFactory Factory for {@link TextSource}s.
    * @param agentCacheState Notified when the model updates a file.
    * @param fileChangeWatcher A FileDistribution.
    */
   public EditorModel(Resources resources,
                      TextSource.Factory textSourceFactory,
                      AgentCacheState agentCacheState,
                      FileChangeWatcher fileChangeWatcher) {
     m_resources = resources;
     m_textSourceFactory = textSourceFactory;
     m_agentCacheState = agentCacheState;
 
     m_defaultBuffer = new BufferImplementation(m_resources,
                                                m_textSourceFactory.create(),
                                                createNewBufferName());
     addBuffer(m_defaultBuffer);
 
     m_defaultBuffer.getTextSource().setText(
       m_resources.getStringFromFile(
         "scriptSupportUnderConstruction.text", true));
 
     fileChangeWatcher.addFileChangedListener(new FileChangedListener() {
       public void filesChanged(File[] file) {
         synchronized (m_fileBuffers) {
           for (int i = 0; i < file.length; ++i) {
             final Buffer buffer = getBufferForFile(file[i]);
 
             if (buffer != null && !buffer.isUpToDate()) {
               fireBufferNotUpToDate(buffer);
             }
           }
         }
       }
     });
   }
 
   /**
    * Get the currently active buffer.
    *
    * @return The active buffer.
    */
   public Buffer getSelectedBuffer() {
     return m_selectedBuffer;
   }
 
   /**
    * Select the default buffer.
    */
   public void selectDefaultBuffer() {
     selectBuffer(m_defaultBuffer);
   }
 
   /**
    * Select a new buffer.
    */
   public void selectNewBuffer() {
     final Buffer buffer = new BufferImplementation(m_resources,
                                                    m_textSourceFactory.create(),
                                                    createNewBufferName());
     addBuffer(buffer);
 
     selectBuffer(buffer);
   }
 
   /**
    * Select the buffer for the given file.
    *
    * @param file
    *          The file.
    * @return The buffer.
    * @throws ConsoleException
    *           If a buffer could not be selected for the file.
    */
   public Buffer selectBufferForFile(File file) throws ConsoleException {
     final Buffer existingBuffer = getBufferForFile(file);
     final Buffer buffer;
 
     if (existingBuffer != null) {
       buffer = existingBuffer;
 
       selectBuffer(buffer);
 
       if (!buffer.isUpToDate()) {
         // The user's edits conflict with a file system change.
         // We ensure the buffer is selected before firing this event because
         // the UI might only raise out of date warnings for selected buffers.
         fireBufferNotUpToDate(buffer);
       }
     }
     else {
      buffer = new BufferImplementation(m_resources,
                                        m_textSourceFactory.create(),
                                        file);
       buffer.load();
       addBuffer(buffer);
 
       m_fileBuffers.put(file, buffer);
 
       selectBuffer(buffer);
     }
 
     return buffer;
   }
 
   /**
    * Get the buffer for the given file.
    *
    * @param file
    *          The file.
    * @return The buffer; <code>null</code> => there is no buffer for the file.
    */
   public Buffer getBufferForFile(File file) {
     return (Buffer)m_fileBuffers.get(file);
   }
 
   /**
    * Return a copy of the current buffer list.
    *
    * @return The buffer list.
    */
   public Buffer[] getBuffers() {
     return (Buffer[])m_bufferList.toArray(new Buffer[m_bufferList.size()]);
   }
 
   /**
    * Return whether one of our buffers is dirty.
    *
    * @return <code>true</code> => a buffer is dirty.
    */
   public boolean isABufferDirty() {
     final Buffer[] buffers = getBuffers();
 
     for (int i = 0; i < buffers.length; ++i) {
       if (buffers[i].isDirty()) {
         return true;
       }
     }
 
     return false;
   }
 
   /**
    * Select a buffer.
    *
    * @param buffer The buffer.
    */
   public void selectBuffer(Buffer buffer) {
     if (buffer == null || !buffer.equals(m_selectedBuffer)) {
 
       final Buffer oldBuffer = m_selectedBuffer;
 
       m_selectedBuffer = buffer;
 
       if (oldBuffer != null) {
         fireBufferStateChanged(oldBuffer);
       }
 
       if (buffer != null) {
         fireBufferStateChanged(buffer);
       }
     }
   }
 
   /**
    * Close a buffer.
    *
    * @param buffer The buffer.
    */
   public void closeBuffer(final Buffer buffer) {
     if (m_bufferList.remove(buffer)) {
       final File file = buffer.getFile();
 
       if (buffer.equals(getBufferForFile(file))) {
         m_fileBuffers.remove(file);
       }
 
       if (buffer.equals(getSelectedBuffer())) {
         final int numberOfBuffers = m_bufferList.size();
 
         if (numberOfBuffers > 0) {
           selectBuffer((Buffer)m_bufferList.get(numberOfBuffers - 1));
         }
         else {
           selectBuffer(null);
         }
       }
 
       m_listeners.apply(
         new ListenerSupport.Informer() {
           public void inform(Object listener) {
             ((Listener)listener).bufferRemoved(buffer);
           }
         });
     }
   }
 
   /**
    * Get the currently marked script.
    *
    * @return The active buffer.
    */
   public File getMarkedScript() {
     return m_markedScript;
   }
 
   /**
    * Get the currently marked script.
    *
    * @param markedScript The marked script.
    */
   public void setMarkedScript(File markedScript) {
     m_markedScript = markedScript;
   }
 
   private void addBuffer(final Buffer buffer) {
     buffer.getTextSource().addListener(new TextSource.Listener() {
         public void textSourceChanged(boolean dirtyStateChanged) {
           if (dirtyStateChanged) {
             fireBufferStateChanged(buffer);
           }
         }
       });
 
     buffer.addListener(
       new BufferImplementation.Listener() {
         public void bufferSaved(Buffer buffer, File oldFile) {
           final File newFile = buffer.getFile();
 
           m_agentCacheState.setOutOfDate(newFile.lastModified());
 
           if (!newFile.equals(oldFile)) {
             if (oldFile != null) {
               m_fileBuffers.remove(oldFile);
             }
 
             m_fileBuffers.put(newFile, buffer);
 
             // Fire that bufferChanged because it is associated with a new
             // file.
             fireBufferStateChanged(buffer);
           }
         }
       }
       );
 
     m_bufferList.add(buffer);
 
     m_listeners.apply(
       new ListenerSupport.Informer() {
         public void inform(Object listener) {
           ((Listener)listener).bufferAdded(buffer);
         }
       });
   }
 
   private void fireBufferStateChanged(final Buffer buffer) {
     m_listeners.apply(
       new ListenerSupport.Informer() {
         public void inform(Object listener) {
           ((Listener)listener).bufferStateChanged(buffer);
         }
       });
   }
 
   /**
    * The UI doesn't currently listen to this event, but might want to in the
    * future.
    */
   private void fireBufferNotUpToDate(final Buffer buffer) {
     m_listeners.apply(
       new ListenerSupport.Informer() {
         public void inform(Object listener) {
           ((Listener)listener).bufferNotUpToDate(buffer);
         }
       });
   }
 
   private String createNewBufferName() {
 
     final String prefix = m_resources.getString("newBuffer.text");
 
     try {
       if (m_nextNewBufferNameIndex == 0) {
         return prefix;
       }
       else {
         return prefix + " " + m_nextNewBufferNameIndex;
       }
     }
     finally {
       ++m_nextNewBufferNameIndex;
     }
   }
 
   /**
    * Add a new listener.
    *
    * @param listener The listener.
    */
   public void addListener(Listener listener) {
     m_listeners.add(listener);
   }
 
   /**
    * Return whether the given file should be considered to be a Python
    * file. For now this is just based on name.
    *
    * @param f The file.
    * @return <code>true</code> => its a Python file.
    */
   public boolean isPythonFile(File f) {
     return f != null && f.getName().toLowerCase().endsWith(".py");
   }
 
   /**
    * Return whether the given file should be marked as boring.
    *
    * @param f The file.
    * @return a <code>true</code> => its boring.
    */
   public boolean isBoringFile(File f) {
     if (f == null) {
       return false;
     }
 
     final String name = f.getName().toLowerCase();
 
     return
       f.isHidden() ||
       name.endsWith(".class") ||
       name.startsWith("~") ||
       name.endsWith("~") ||
       name.startsWith("#") ||
       name.endsWith(".exe") ||
       name.endsWith(".gif") ||
       name.endsWith(".jpeg") ||
       name.endsWith(".jpg") ||
       name.endsWith(".tiff");
   }
 
   /**
    * Interface for listeners.
    */
   public interface Listener extends EventListener {
 
     /**
      * Called when a buffer has been added.
      *
      * @param buffer The buffer.
      */
     void bufferAdded(Buffer buffer);
 
     /**
      * Called when a buffer's state has changed. I.e. the buffer has
      * become dirty, or become clean, or has been selected, or has
      * been unselected, or has become associated with a new file.
      *
      * @param buffer The buffer.
      */
     void bufferStateChanged(Buffer buffer);
 
     /**
      * Called when an independent modification to a buffer's associated
      * file has been detected.
      *
      * @param buffer The buffer.
      */
     void bufferNotUpToDate(Buffer buffer);
 
     /**
      * Called when a buffer has been removed.
      *
      * @param buffer The buffer.
      */
     void bufferRemoved(Buffer buffer);
   }
 
   /**
    * Base {@link Listener} implementation that does nothing.
    */
   public abstract static class AbstractListener implements Listener {
 
     /**
      * @see Listener#bufferAdded
      * @param buffer The buffer.
      */
     public void bufferAdded(Buffer buffer) { }
 
     /**
      * @see Listener#bufferStateChanged
      * @param buffer The buffer.
      */
     public void bufferStateChanged(Buffer buffer) { }
 
     /**
      * @see Listener#bufferNotUpToDate
      * @param buffer The buffer.
      */
     public void bufferNotUpToDate(Buffer buffer) { }
 
     /**
      * @see Listener#bufferRemoved
      * @param buffer The buffer.
      */
     public void bufferRemoved(Buffer buffer) { }
   }
 }
