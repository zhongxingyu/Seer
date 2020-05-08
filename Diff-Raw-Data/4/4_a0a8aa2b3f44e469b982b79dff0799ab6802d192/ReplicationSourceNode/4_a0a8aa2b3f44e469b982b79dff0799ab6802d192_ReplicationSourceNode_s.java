 /*
  * (C) Copyright 2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
  */
 
 package org.nuxeo.ecm.platform.replication.importer;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
 import org.nuxeo.ecm.platform.importer.source.SourceNode;
 
 /**
  * Implementation for source node interface. For replication, it is important to
  * have the entire File path and also to select children only the folders: the
  * files are in fact document parts.
  *
  * @author rux
  *
  */
 public class ReplicationSourceNode implements SourceNode {
 
     protected final File file;
 
     protected List<SourceNode> children;
 
     public ReplicationSourceNode(File file) {
         children = null;
         this.file = file;
     }
 
     public ReplicationSourceNode(String filePath) {
         this(new File(filePath));
     }
 
     public BlobHolder getBlobHolder() {
         // not interesting, any way blob files are imported from files
         return null;
     }
 
     public List<SourceNode> getChildren() {
         // the documents are exported as folders, look if other folders under
         children = new ArrayList<SourceNode>();
         for (File child : file.listFiles()) {
             if (child.isDirectory()) {
                 children.add(new ReplicationSourceNode(child.getPath()));
             }
         }
         return children;
     }
 
     public String getName() {
         return file.getPath();
     }
 
     public boolean isFolderish() {
         // in fact return if there are any children here
         if (children == null) {
             getChildren();
         }
         return !children.isEmpty();
     }
 
 }
