 /*
  * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl-2.1.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Benjamin JALON<bjalon@nuxeo.com>
  */
 package org.nuxeo.ide.sdk.projects.marketplace;
 
import org.nuxeo.ide.common.forms.Form;
 import org.nuxeo.ide.sdk.projects.NuxeoProjectPage1;
import org.nuxeo.ide.sdk.ui.widgets.ProjectChooserWidget;
 
 /**
  * @since 1.2
  */
 public class EclipseProjectPage extends NuxeoProjectPage1 {
 
     public EclipseProjectPage() {
        super("marketplace", "Create a Nuxeo Marketplace Package", null);
     }

    @Override
    public Form createForm() {
        Form form = super.createForm();
        form.addWidgetType(ProjectChooserWidget.class);
        return form;
    }

 }
