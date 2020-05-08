 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  * 
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * 
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.api.javafx.source;
 
 import java.io.IOException;
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.concurrent.Future;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.text.Document;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.modules.javafx.source.parsing.JavaFXParserFactory;
 import org.netbeans.modules.javafx.source.parsing.LegacyUserTask;
 import org.netbeans.modules.parsing.api.ParserManager;
 import org.netbeans.modules.parsing.spi.ParseException;
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileStateInvalidException;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataObject;
 import org.openide.loaders.DataObjectNotFoundException;
 import org.openide.util.Exceptions;
 import org.netbeans.modules.parsing.api.Source;
 
 
 /**
  * A class representing JavaFX source.
  * 
  * @author nenik
  * @author David Strupl
  */
 public final class JavaFXSource {
 
     static {
         JavaFXSourceTaskFactoryManager.register();
     }
 
     public static enum Phase {
         MODIFIED,
         PARSED,
         ANALYZED,
         UP_TO_DATE,
         CODE_GENERATED;
         
         public boolean lessThan(Phase p) {
             return compareTo(p) < 0;
         }
 
         public CompilationPhase toCompilationPhase() {
             return CompilationPhase.values()[ordinal()];
         }
 
         public static Phase from(CompilationPhase compilationPhase) {
             return values()[compilationPhase.ordinal()];
         }
 
     };
     public static enum Priority {
         MAX,
         HIGH,
         ABOVE_NORMAL,
         NORMAL,
         BELOW_NORMAL,
         LOW,
         MIN
     };
 
     private static Map<FileObject, Reference<JavaFXSource>> file2Source = new WeakHashMap<FileObject, Reference<JavaFXSource>>();
     static final Logger LOGGER = Logger.getLogger(JavaFXSource.class.getName());
     
     //Source files being processed, may be empty
     private final List<Source> sources;
     //Files being processed, may be empty
     public final List<? extends FileObject> files;
     
     private final ClasspathInfo cpInfo;
 
     public FileObject getFileObject() {
         return files.iterator().next();
     }
 
     private JavaFXSource(ClasspathInfo cpInfo, Collection<? extends FileObject> files) throws IOException {
         this.cpInfo = cpInfo;
         List<FileObject> filesList = new LinkedList<FileObject>();
         List<Source> sourcesList = new LinkedList<Source>();
         boolean multipleSources = files.size() > 1;
         for (Iterator<? extends FileObject> it = files.iterator(); it.hasNext();) {
             FileObject file = it.next();
             Logger.getLogger("TIMER").log(Level.FINE, "JavaFXSource",
                 new Object[] {file, this});
             if (!file.isValid()) {
                 if (multipleSources) {
                     LOGGER.warning("Ignoring non existent file: " + FileUtil.getFileDisplayName(file));     //NOI18N
                 }
                 else {
                     DataObject.find(file);  //throws IOE
                 }
             }
             else {
                 filesList.add (file);
                 sourcesList.add(Source.create(file));
             }
         }
         this.files = Collections.unmodifiableList(filesList);
         this.sources = Collections.unmodifiableList(sourcesList);
         
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine("Created JavaFXSource for " + files); // NOI18N
         }
     }
     
     
     /**
      * Returns a {@link JavaFXSource} instance associated with given
      * {@link org.openide.filesystems.FileObject}.
      * It returns null if the file doesn't represent JavaFX source file.
      * 
      * @param fileObject for which the {@link JavaFXSource} should be found/created.
      * @return {@link JavaFXSource} or null
      * @throws {@link IllegalArgumentException} if fileObject is null
      */
     public static JavaFXSource forFileObject(FileObject fileObject) throws IllegalArgumentException {
         if (fileObject == null) {
             throw new IllegalArgumentException ("fileObject == null");  //NOI18N
         }
         if (!fileObject.isValid()) {
             return null;
         }
 
         try {
             if (   fileObject.getFileSystem().isDefault()
                 && fileObject.getAttribute("javax.script.ScriptEngine") != null // NOI18N
                 && fileObject.getAttribute("template") == Boolean.TRUE) { // NOI18N
                 return null;
             }
             DataObject od = DataObject.find(fileObject);
             
             EditorCookie ec = od.getLookup().lookup(EditorCookie.class);           
         } catch (FileStateInvalidException ex) {
             LOGGER.log(Level.FINE, null, ex);
             return null;
         } catch (DataObjectNotFoundException ex) {
             LOGGER.log(Level.FINE, null, ex);
             return null;
         }
         
         Reference<JavaFXSource> ref = file2Source.get(fileObject);
         JavaFXSource source = ref != null ? ref.get() : null;
         if (source == null) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.fine("Not found in cache: " + fileObject); // NOI18N
             }
 
             if (!JavaFXParserFactory.MIME_TYPE.equals(FileUtil.getMIMEType(fileObject)) && !"fx".equals(fileObject.getExt())) {  //NOI18N
                 return null;
             }
             source = create(ClasspathInfo.create(fileObject), Collections.singletonList(fileObject));
             file2Source.put(fileObject, new WeakReference<JavaFXSource>(source));
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.fine("Put into the cache: " + fileObject); // NOI18N
             }
         }
         return source;
     }
 
     public static JavaFXSource create(final ClasspathInfo cpInfo, final Collection<? extends FileObject> files) throws IllegalArgumentException {
         try {
             return new JavaFXSource(cpInfo, files);
         } catch (DataObjectNotFoundException donf) {
             Logger.getLogger("global").warning("Ignoring non existent file: " + FileUtil.getFileDisplayName(donf.getFileObject()));     //NOI18N
         } catch (IOException ex) {            
             Exceptions.printStackTrace(ex);
         }        
         return null;
     }
 
     public void runUserActionTask( final Task<? super CompilationController> task, final boolean shared) throws IOException {
         if (task == null) {
             throw new IllegalArgumentException ("Task cannot be null");     //NOI18N
         }
 
         if (this.files.size()<=1) {
             try {
                 ParserManager.parse(sources, new LegacyUserTask(getClasspathInfo(), task));
             } catch (Exception ex) {
               // XXX better handling
               Exceptions.printStackTrace(ex);
             } finally {
             }
         }
     }
 
     /**
      * Performs the given task when the scan finished. When no background scan is running
      * it performs the given task synchronously. When the background scan is active it queues
      * the given task and returns, the task is performed when the background scan completes by
      * the thread doing the background scan.
      * @param task to be performed
      * @param shared if true the java compiler may be reused by other {@link org.netbeans.api.java.source.CancellableTasks},
      * the value false may have negative impact on the IDE performance.
      * @return {@link Future} which can be used to find out the sate of the task {@link Future#isDone} or {@link Future#isCancelled}.
      * The caller may cancel the task using {@link Future#cancel} or wait until the task is performed {@link Future#get}.
      * @throws IOException encapsulating the exception thrown by {@link CancellableTasks#run}
      * @since 0.12
      */
     public Future<Void> runWhenScanFinished (final Task<CompilationController> task, final boolean shared)
             throws IOException
     {
         try {
             return ParserManager.parseWhenScanFinished(sources,
                     new LegacyUserTask(getClasspathInfo(), task));
         } catch (ParseException e) {
            throw new IOException(e.getMessage());
         }
     }
 
     public TokenHierarchy getTokenHierarchy() {
         Document doc = source().getDocument(true);
         TokenHierarchy th = TokenHierarchy.get(doc);
         return th;
     }
     
     public ClasspathInfo getClasspathInfo() {
         return cpInfo;
     }
     
     /**
      * Returns a {@link JavaSource} instance associated to the given {@link javax.swing.Document},
      * it returns null if the {@link Document} is not
      * associated with data type providing the {@link JavaSource}.
      * @param doc {@link Document} for which the {@link JavaSource} should be found/created.
      * @return {@link JavaSource} or null
      * @throws {@link IllegalArgumentException} if doc is null
      */
     public static JavaFXSource forDocument(Document doc) throws IllegalArgumentException {
         if (doc == null) {
             throw new IllegalArgumentException ("doc == null");  //NOI18N
         }
         Reference<?> ref = (Reference<?>) doc.getProperty(JavaFXSource.class);
         JavaFXSource js = ref != null ? (JavaFXSource) ref.get() : null;
         if (js == null) {
             Object source = doc.getProperty(Document.StreamDescriptionProperty);
             
             if (source instanceof DataObject) {
                 DataObject dObj = (DataObject) source;
                 if (dObj != null) {
                     js = forFileObject(dObj.getPrimaryFile());
                 }
             }
         }
         return js;
     }
 
     public Collection<Source> sources() {
         return sources;
     }
 
     private Source source() {
         return sources.iterator().next();
     }
 
}

