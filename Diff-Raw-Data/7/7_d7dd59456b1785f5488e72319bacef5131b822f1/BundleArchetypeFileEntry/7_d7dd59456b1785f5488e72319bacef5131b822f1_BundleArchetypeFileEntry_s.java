 /*
  * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     stan
  */
 package org.nuxeo.ide.archetype.ui.providers;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.osgi.framework.Bundle;
 
 /**
  * A Archetype file entry that is being stored in a osgi bundle.
  * 
  * @author stan
  * 
  */
 public class BundleArchetypeFileEntry implements IArchetypeFileEntry {
 
     Bundle bundle;
 
     IPath path;
 
     String title;
 
     public BundleArchetypeFileEntry(Bundle bundle, IPath path, String title) {
         this.bundle = bundle;
         this.path = path;
         this.title = title;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.nuxeo.ide.archetype.ui.providers.IArchetypeFileEntry#getInputStream()
      */
     public InputStream getInputStream() throws IOException {
         return FileLocator.openStream(bundle, path, false);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.nuxeo.ide.archetype.ui.providers.IArchetypeFileEntry#getTitle()
      */
     public String getTitle() {
         return title;
     }
 
    @Override
     public File getArchetypeFile() throws Exception {
         URL url = FileLocator.find(bundle, path, null);
         return new File(FileLocator.toFileURL(url).toURI());
     }
 
 }
