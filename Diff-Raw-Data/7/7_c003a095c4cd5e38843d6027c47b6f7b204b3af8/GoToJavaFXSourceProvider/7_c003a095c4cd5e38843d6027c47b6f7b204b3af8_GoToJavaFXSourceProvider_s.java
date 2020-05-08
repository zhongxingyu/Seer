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
 
 package org.netbeans.modules.javafx.profiler.utilities;
 
 import com.sun.tools.javac.code.Symbol;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicBoolean;
 import org.netbeans.api.javafx.source.Task;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.project.Project;
 import org.netbeans.modules.profiler.spi.GoToSourceProvider;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.TypeElement;
 import javax.swing.SwingUtilities;
 import org.openide.filesystems.FileObject;
 import org.netbeans.api.javafx.source.ElementHandle;
 import org.netbeans.modules.javafx.project.JavaFXProject;
 import org.netbeans.lib.profiler.ProfilerLogger;
 import org.netbeans.api.javafx.editor.ElementOpen;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 
         
 public class GoToJavaFXSourceProvider implements GoToSourceProvider {
     // field to indicate whether source find was successfull
     final AtomicBoolean result = new AtomicBoolean(false);
 
     public boolean openSource(final Project project, final String className, final String methodName, String signature) {
         if (!(project instanceof JavaFXProject))
             return false;
 
         Iterator<FileObject> files = JavaFXProjectUtilities.getSourceFiles((JavaFXProject)project).iterator();
         JavaFXSource source = null;
         while(files.hasNext()) {
             FileObject fo = files.next();
            if(fo.getName().equals(className.substring(className.lastIndexOf('.') + 1, className.length()))) {
                 source = JavaFXSource.forFileObject(fo);
             }
         }
 
         if (source == null) {
             return false;
         }
 
         final JavaFXSource js = source;
         final CountDownLatch latch = new CountDownLatch(1);
 
         // cut interface suffix out of the signature
         final String sig = JavaFXProjectUtilities.cutIntfSuffix(signature);
 
         try {
             // use the prepared javasource repository and perform a task
             js.runUserActionTask(new Task<CompilationController>() {
                     public void run(CompilationController controller)
                              throws Exception {
                         if (controller.toPhase(Phase.ANALYZED).lessThan(Phase.ANALYZED)) {
                            return;
                         }
 
                         Element destinationElement = null;
 
                         // resolve the class by name
                         TypeElement classElement = JavaFXProjectUtilities.resolveClassByName(className, controller);
                         // only fx classes supported; others to be opened via GoToJavaSourceProvider
                         if (!controller.getJavafxTypes().isJFXClass((Symbol) classElement)) {
                             return;
                         }
                             
                         if ((methodName != null) && (methodName.length() > 0)) {
                             // if a method name has been specified try to resolve the method
                             if (classElement != null) {
                                 ElementHandle eh = new ElementHandle(ElementKind.METHOD, new String[] {className, methodName, sig});
                                 destinationElement = eh.resolve(controller);
                             }
                         }
 
                         if (destinationElement == null) {
                             // unsuccessful attempt to resolve a method -> use the class instead
                             destinationElement = classElement;
                         }
                         if (destinationElement != null) {
                             ProfilerLogger.debug("Opening element: " + destinationElement); // NOI18N
 
                             final Element openElement = destinationElement;
                             final CompilationController cc = controller;
 
                             SwingUtilities.invokeLater(new Runnable() {
                                     // manipulates the TopComponent - must be executed in EDT
                                     public void run() {
                                         // opens the source code on the found method position
                                         try {                                            
                                             result.set(ElementOpen.open(cc, openElement));
                                             latch.countDown();
                                         } catch (Exception e) {
                                             ProfilerLogger.log(e);
                                         }
                                     }
                                 });
                         } else {
                             latch.countDown();
                         }
                     }
                 }, false);
         } catch (IOException ex) {
             ProfilerLogger.log(ex);
         }
 
         try {
             latch.await();
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
         return result.get();
     }
 }
