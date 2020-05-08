 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
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
  */
 package org.netbeans.modules.javafx.source.tasklist;
 
 import com.sun.tools.mjavac.util.JCDiagnostic;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
import java.util.WeakHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.tools.Diagnostic;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.spi.tasklist.PushTaskScanner;
 import org.netbeans.spi.tasklist.Task;
 import org.netbeans.spi.tasklist.TaskScanningScope;
 import org.openide.filesystems.FileChangeAdapter;
 import org.openide.filesystems.FileChangeListener;
 import org.openide.filesystems.FileEvent;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.URLMapper;
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 import org.openide.util.RequestProcessor;
 
 /**
  *
  * @author Karol Harezlak
  */
 
 //TODO Currrently not in use!
 public class JavaFXTaskListProvider extends PushTaskScanner {
 
     private static final String TASK_LIST_NAME = NbBundle.getMessage(JavaFXTaskListProvider.class, "LABEL_TL_JAVAFX_ISSUES");//NOI18N
     private static final String FX_EXT = "fx"; //NOI18N
     private final HashMap<FileObject, FileChangeListener> projectDirs = new HashMap<FileObject, FileChangeListener>();
     private final HashMap<FileObject, RequestProcessor.Task> taskMap = new HashMap<FileObject, RequestProcessor.Task>();
     private AtomicBoolean active = new AtomicBoolean();
 
     private JavaFXTaskListProvider() {
         super(TASK_LIST_NAME, TASK_LIST_NAME, TASK_LIST_NAME);
     }
 
     public static final PushTaskScanner create() {
         return new JavaFXTaskListProvider();
     }
 
     @Override
     public void setScope(TaskScanningScope taskScanningScope, final Callback callback) {
 
         if (callback == null) {
             active.set(false);
             for (FileObject fileObject : projectDirs.keySet()) {
                 fileObject.removeRecursiveListener(projectDirs.get(fileObject));
             }
             for (RequestProcessor.Task task : taskMap.values()) {
                 if (!task.isFinished()) {
                     task.cancel();
                 }
             }
             projectDirs.clear();
             taskMap.clear();
 
             return;
         }
 
         active.set(true);
 
         Iterator<FileObject> iterator = taskScanningScope.iterator();
         while (iterator.hasNext()) {
             FileObject fileObject = iterator.next();
 
             if (!fileObject.getExt().equals(FX_EXT)) {
                 continue;
             }
 
             final FileObject projectDir = FileOwnerQuery.getOwner(fileObject).getProjectDirectory();
 
 
             if (!projectDirs.keySet().contains(projectDir)) {
                 FileChangeListener listener = new FileChangeAdapter() {
 
                     @Override
                     public void fileDataCreated(FileEvent fe) {
                         if (!active.get()) {
                             return;
                         }
                         //System.out.println("File created: " + fe.getFile().getPath());
                         if (fe.getFile().getExt().equals(FX_EXT)) {
                             updateTasks(fe.getFile(), callback, 5000);
                         }
 
                         super.fileDataCreated(fe);
                     }
 
                     @Override
                     public void fileFolderCreated(FileEvent fe) {
                         if (!active.get()) {
                             return;
                         }
                         //System.out.println("Folder created: " + fe.getFile().getPath());
                         for (FileObject child : fe.getFile().getChildren()) {
                             if (child.getExt().equals(FX_EXT)) {
                                 updateTasks(child, callback, 5000);
                             }
                         }
 
                         super.fileFolderCreated(fe);
                     }
 
                     @Override
                     public void fileChanged(FileEvent fe) {
                         if (!active.get()) {
                             return;
                         }
                         //System.out.println("File changed: " + fe.getFile().getPath());
                         if (fe.getFile().getExt().equals(FX_EXT)) {
                             updateTasks(fe.getFile(), callback, 5000);
                         }
                         super.fileChanged(fe);
                     }
 
                     @Override
                     public void fileDeleted(FileEvent fe) {
                         if (!active.get()) {
                             return;
                         }
                         if (fe.getFile() == projectDir) {
                             fe.getFile().removeRecursiveListener(this);
                         } else if (fe.getFile().getExt().equals(FX_EXT)) {
                             synchronized (taskMap) {
                                 RequestProcessor.Task task = taskMap.get(fe.getFile());
                                 if (task != null && !task.isFinished()) {
                                     task.cancel();
                                     //System.out.println("TASK CANCELED FOR: " + fe.getFile().getPath());
                                 }
                             }
                             //System.out.println("File deleted: " + fe.getFile().getPath());
                             callback.setTasks(fe.getFile(), Collections.EMPTY_LIST);
                         }
                         super.fileDeleted(fe);
                     }
                 };
                 projectDirs.put(projectDir, listener);
                //projectDir.addRecursiveListener(listener);
             }
 
             updateTasks(fileObject, callback, 20000);
         }
 
     }
 
     private void updateTasks(final FileObject fileObject, final Callback callback, int delay) {
 
         RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {
 
             public void run() {
                 if (!active.get()) {
                     return;
                 }
                 if (!fileObject.isValid()) {
                     //System.out.println("NOT VALID ANY MORE " + fileObject.getPath());
                     callback.setTasks(fileObject, Collections.EMPTY_LIST);
                 }
                 JavaFXSource jfxs = JavaFXSource.forFileObject(fileObject);
 
                 if (jfxs == null) {
                     return;
                 } else {
                     System.out.println(">>>>>>>>>>>>>  TASK CREATED  FOR " + fileObject.getPath());
                 }
                 try {
                     jfxs.runWhenScanFinished(new ScannerTask(callback), true);
                 } catch (IOException ex) {
                     Exceptions.printStackTrace(ex);
                 }
             }
         });
         synchronized (taskMap) {
             if (taskMap.get(fileObject) != null && !taskMap.get(fileObject).isFinished()) {
                 taskMap.get(fileObject).cancel();
             }
             taskMap.put(fileObject, task);
         }
         task.setPriority(Thread.MIN_PRIORITY);
         task.schedule(delay);
     }
 
     private class ScannerTask
             implements org.netbeans.api.javafx.source.Task<CompilationController> {
 
         private final Callback callback;
 
         public ScannerTask(Callback callback) {
             this.callback = callback;
         }
 
         public void run(final CompilationController compilationController) throws Exception {
             //System.out.println("TASK RAN FOR " + compilationController.getFileObject().getPath());
             if (!active.get()) {
                 return;
             }
             refreshTasks(compilationController);
         }
 
         private void refreshTasks(CompilationController compilationController) {
             //StringBuilder builder = new StringBuilder();
             if (compilationController.getDiagnostics().isEmpty()) {
                 callback.setTasks(compilationController.getFileObject(), Collections.EMPTY_LIST);
                 return;
             }
             Map<FileObject, List<Task>> tasksMap = new HashMap<FileObject, List<Task>>();
             for (Diagnostic diagnostic : compilationController.getDiagnostics()) {
                 if (!(diagnostic instanceof JCDiagnostic)) {
                     continue;
                 }
                 JCDiagnostic jcd = ((JCDiagnostic) diagnostic);
                 FileObject currentFileObject = null;
                 try {
                     currentFileObject = URLMapper.findFileObject(jcd.getSource().toUri().toURL());
                 } catch (MalformedURLException ex) {
                     Exceptions.printStackTrace(ex);
                 }
                 if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                     Task task = Task.create(currentFileObject, "nb-tasklist-error", diagnostic.getMessage(Locale.getDefault())/* + " " + currentFileObject.getName() + " " + diagnostic.getLineNumber()*/, (int) diagnostic.getLineNumber()); //NOI18N
                     tasksMap.put(currentFileObject, addTask(tasksMap.get(currentFileObject), task));
                     //builder.append("ERROR TASK - File: " + compilationController.getFileObject().getName()  + " Source File: " + currentFileObject.getName() + " Message " + diagnostic.getMessage(Locale.getDefault()) + " Line " + diagnostic.getLineNumber() + "\n");
                 }
             }
             for (FileObject key : tasksMap.keySet()) {
                 List<Task> list = tasksMap.get(key);
                 if (list != null) {
                     callback.setTasks(key, list);
                 }
             }
 
         }
 
         private List<Task> addTask(List<Task> list, Task task) {
             if (list == null) {
                 list = new ArrayList<Task>();
             }
             list.add(task);
 
             return list;
         }
     }
 }
