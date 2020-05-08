 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
  *     bstefanescu
  */
 package org.nuxeo.build.ant.artifact;
 
 import org.apache.maven.model.Dependency;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.nuxeo.build.maven.MavenClientFactory;
 import org.nuxeo.build.maven.filter.DependencyFilter;
 import org.nuxeo.build.maven.graph.Graph;
 import org.nuxeo.build.maven.graph.Node;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class NuxeoExpandTask extends Task {
 
     protected DependencyFilter filter = new DependencyFilter() {
         public boolean accept(Node parent, Dependency dep) {
             if (!parent.getArtifact().getGroupId().startsWith("org.nuxeo")) {
                 return false;
             }
             String depScope = dep.getScope();
            if ("compile".equals(depScope) || "runtime".equals(depScope)) {
                 return true;
             }
             return false;
         }
     };
     
     @Override
     public void execute() throws BuildException {
         Graph graph = MavenClientFactory.getInstance().getGraph();
         for (Node node : graph.getRoots()) {
             if (node.getArtifact().getGroupId().startsWith("org.nuxeo")) {
                node.expandAll(filter);                
             }
         }
     }
         
 }
