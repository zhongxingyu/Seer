 /*
  * (C) Copyright 2006-2007 Nuxeo SAS <http://nuxeo.com> and others
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Jean-Marc Orliaguet, Chalmers
  *
  * $Id$
  */
 
 package org.nuxeo.theme.webengine.negotiation.perspective;
 
 import org.nuxeo.ecm.webengine.model.WebContext;
 import org.nuxeo.theme.ApplicationType;
 import org.nuxeo.theme.Manager;
 import org.nuxeo.theme.ViewDef;
 import org.nuxeo.theme.negotiation.Scheme;
 import org.nuxeo.theme.perspectives.PerspectiveManager;
 import org.nuxeo.theme.types.TypeFamily;
 import org.nuxeo.theme.types.TypeRegistry;
 
 public final class ViewId implements Scheme {
 
     public String getOutcome(final Object context) {
         WebContext webContext = (WebContext) context;
         final String applicationPath = webContext.getModulePath();
         final TypeRegistry typeRegistry = Manager.getTypeRegistry();
         final ApplicationType application = (ApplicationType) typeRegistry.lookup(
                 TypeFamily.APPLICATION, applicationPath);
         if (application == null) {
             return null;
         }
        for (String path : webContext.getUriInfo().getMatchedURIs()) {
            if (path.equals(applicationPath)) {
                return null;
            }
            final String viewId = path.substring(applicationPath.length() + 1,
                    path.length());
            final ViewDef view = application.getViewById(viewId);
            if (view == null) {
                continue;
            }
            final String perspectiveName = view.getPerspective();
            if (PerspectiveManager.hasPerspective(perspectiveName)) {
                return perspectiveName;
            }
         }
         return null;
     }
 }
