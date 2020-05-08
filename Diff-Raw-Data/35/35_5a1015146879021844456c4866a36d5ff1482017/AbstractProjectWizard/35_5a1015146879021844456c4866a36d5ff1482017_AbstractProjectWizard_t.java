 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 package org.nuxeo.ide.sdk.projects;
 
 import org.eclipse.jface.wizard.WizardPage;
 import org.nuxeo.ide.common.wizards.AbstractWizard;
 import org.nuxeo.ide.sdk.NuxeoSDK;
 import org.nuxeo.ide.sdk.templates.Constants;
 import org.nuxeo.ide.sdk.templates.TemplateRegistry;
 import org.nuxeo.ide.sdk.ui.SDKClassPathContainer;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public abstract class AbstractProjectWizard extends
         AbstractWizard<ProjectTemplateContext> {
 
     protected String defaultTemplate;
 
     public AbstractProjectWizard() {
         this(TemplateRegistry.DEFAULT_TEMPLATE);
     }
 
     public AbstractProjectWizard(String templateName) {
         this.defaultTemplate = templateName;
     }
 
     protected abstract WizardPage[] createPages();
 
     public void addPages() {
         if (NuxeoSDK.getDefault() != null) {
             for (WizardPage page : createPages()) {
                 addPage(page);
             }
         } else {
             addPage(new UndefinedNuxeoSDKPage());
         }
     }
 
     @Override
     protected ProjectTemplateContext createExecutionContext() {
         ProjectTemplateContext ctx = new ProjectTemplateContext();
         // initialize defaults
         ctx.setTemplate(defaultTemplate);
         String version = NuxeoSDK.getDefault().getVersion();
        String osgiVersion = toOsgiVersion(version);
         ctx.put(Constants.TARGET_VERSION, version);
         ctx.put(Constants.PARENT_VERSION, version);
         ctx.put(Constants.BUNDLE_VERSION, osgiVersion);
         ctx.put(Constants.CLASSPATH_CONTAINER, SDKClassPathContainer.ID);
         ctx.put(Constants.TEST_CLASSPATH_CONTAINER,
                 SDKClassPathContainer.ID_TESTS);
         return ctx;
     }
 
    protected static String toOsgiVersion(String version) {
        if (!version.endsWith("-SNAPSHOT")) {
            return version;
        }
        return version.substring(0,
                    version.length() - "-SNAPSHOT".length())
                    + ".qualifier";
    }

     @Override
     protected boolean execute(ProjectTemplateContext ctx) {
         String v = (String) ctx.get(Constants.PROJECT_PACKAGE);
         if (v != null) {
             ctx.put(Constants.PROJECT_PACKAGE_PATH, v.replace('.', '/'));
         }
         CreateProjectFromTemplate op = new CreateProjectFromTemplate(ctx);
         return CreateProjectFromTemplate.run(getShell(), getContainer(), op);
     }
 }
