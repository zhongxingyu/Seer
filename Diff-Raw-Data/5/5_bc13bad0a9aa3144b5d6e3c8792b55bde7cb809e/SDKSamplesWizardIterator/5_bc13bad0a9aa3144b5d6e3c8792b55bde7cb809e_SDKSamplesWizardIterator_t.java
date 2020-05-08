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
 
 package org.netbeans.modules.javafx.sdksamples;
 
 import java.awt.Component;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.MessageFormat;
 import java.util.Enumeration;
 import java.util.LinkedHashSet;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import javax.swing.JComponent;
 import javax.swing.event.ChangeListener;
 import org.netbeans.api.project.ProjectManager;
 import org.netbeans.spi.project.ui.support.ProjectChooser;
 import org.netbeans.spi.project.ui.templates.support.Templates;
 import org.openide.WizardDescriptor;
 import org.openide.filesystems.FileLock;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.NbBundle;
 
 /**
  *
  * @author Michal Skvor
  */
 public class SDKSamplesWizardIterator implements WizardDescriptor.InstantiatingIterator {
 
     private int index;
     private WizardDescriptor.Panel[] panels;
     private WizardDescriptor wizard;
 
     private FileObject file;
 
     public SDKSamplesWizardIterator( FileObject file ) {
         this.file = file;
     }
 
     public static SDKSamplesWizardIterator createIterator( FileObject file ) {
         return new SDKSamplesWizardIterator( file );
     }
 
     private WizardDescriptor.Panel[] createPanels() {
         return new WizardDescriptor.Panel[] { new SDKSamplesWizardPanel( file ) };
     }
 
     private String[] createSteps() {
         return new String[]{ NbBundle.getMessage( SDKSamplesWizardIterator.class, "LBL_CreateProjectStep" )};
     }
 
     public Set instantiate() throws IOException {
         Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
 
         File dirF = FileUtil.normalizeFile((File) wizard.getProperty( "projdir" ));
         dirF.mkdirs();
 
         FileObject template = Templates.getTemplate( wizard );
         FileObject dir = FileUtil.toFileObject( dirF );
         unZipFile( template.getInputStream(), dir, (String) wizard.getProperty( "name" ), file.getName());
 
         resultSet.add(dir);
         // Look for nested projects to open as well:
         Enumeration<? extends FileObject> e = dir.getFolders( true );
         while( e.hasMoreElements()) {
             FileObject subfolder = e.nextElement();
             if( ProjectManager.getDefault().isProject( subfolder )) {
                 resultSet.add(subfolder);
             }
         }
 
         File parent = dirF.getParentFile();
         if( parent != null && parent.exists()) {
             ProjectChooser.setProjectsFolder( parent );
         }
 
         return resultSet;
     }
 
     public void initialize(WizardDescriptor wizard) {
         this.wizard = wizard;
         index = 0;
         panels = createPanels();
         // Make sure list of steps is accurate.
         String[] steps = createSteps();
         for( int i = 0; i < panels.length; i++ ) {
             Component c = panels[i].getComponent();
             if( steps[i] == null ) {
                 // Default step name to component name of panel.
                 // Mainly useful for getting the name of the target
                 // chooser to appear in the list of steps.
                 steps[i] = c.getName();
             }
             if( c instanceof JComponent ) {
                 // assume Swing components
                 JComponent jc = (JComponent) c;
                 // Step #.
                 jc.putClientProperty( "WizardPanel_contentSelectedIndex", new Integer( i ));
                 // Step name (actually the whole list for reference).
                 jc.putClientProperty( "WizardPanel_contentData", steps );
             }
         }
     }
 
     public void uninitialize(WizardDescriptor wizard) {
         this.wizard = null;
     }
 
     public String name() {
         return MessageFormat.format( "{0} of {1}", new Object[]{ new Integer(index + 1), new Integer( panels.length )});
     }
 
     public boolean hasNext() {
         return index < panels.length - 1;
     }
 
     public boolean hasPrevious() {
         return index > 0;
     }
 
     public void nextPanel() {
         if (!hasNext()) {
             throw new NoSuchElementException();
         }
         index++;
     }
 
     public void previousPanel() {
         if (!hasPrevious()) {
             throw new NoSuchElementException();
         }
         index--;
     }
 
     public WizardDescriptor.Panel current() {
         return panels[index];
     }
 
     public void addChangeListener(ChangeListener l) {
     }
 
     public void removeChangeListener(ChangeListener l) {
     }
 
     private static void unZipFile( InputStream source, FileObject projectRoot, String name, String filename ) throws IOException {
         try {
             ZipInputStream str = new ZipInputStream(source);
             ZipEntry entry;
             // Get root entry
             String rootFileName = "";
             ZipEntry root = str.getNextEntry();
             if( root.isDirectory()) {
                 rootFileName = root.getName();
             }
 
             if( rootFileName == null ) return;
             
             while(( entry = str.getNextEntry()) != null ) {
                 if( entry.isDirectory()) {
                     if( entry.getName().equals( filename )) continue;
                     FileUtil.createFolder( projectRoot, entry.getName().substring( rootFileName.length()));
                } else if( entry.getName().toLowerCase().endsWith( ".png" ) || entry.getName().toLowerCase().endsWith( ".jpg" ) ||
                        entry.getName().toLowerCase().endsWith( ".gif" )) {
                    FileObject fo = FileUtil.createData( projectRoot, entry.getName().substring( rootFileName.length()));
                     FileLock lock = fo.lock();
                     try {
                         OutputStream out = fo.getOutputStream(lock);
                         try {
                             FileUtil.copy(str, out);
                         } finally {
                             out.close();
                         }
                     } finally {
                         lock.releaseLock();
                     }
                 } else {
                     FileObject fo = FileUtil.createData(projectRoot, entry.getName().substring( rootFileName.length()));
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     FileUtil.copy(str, baos);
                     String content = baos.toString( "UTF-8" ).
 //                            replaceAll("\n|\r|\r\n", System.getProperty("line.separator")).
                             replaceAll( "@NAME@", name );
                     FileLock lock = fo.lock();
                     try {
                         OutputStream out = fo.getOutputStream( lock );
                         try {
                             FileUtil.copy( new ByteArrayInputStream( content.getBytes( "UTF-8" )), out );
                         } finally {
                             out.close();
                         }
                     } finally {
                         lock.releaseLock();
                     }
                 }
             }
         } finally {
             source.close();
         }
     }
 }
