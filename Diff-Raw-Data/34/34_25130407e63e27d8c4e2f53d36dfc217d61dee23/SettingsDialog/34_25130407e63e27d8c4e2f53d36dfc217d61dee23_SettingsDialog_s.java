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
 package org.nuxeo.ide.webengine.server;
 
 import java.io.File;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.nuxeo.ide.webengine.Nuxeo;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class SettingsDialog extends TitleAreaDialog {
 
     protected ServerView view;
     protected Text text;
     
     public SettingsDialog(ServerView view) {
         super (Display.getCurrent().getActiveShell());
         this.view = view;
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
      */
     @Override
     protected void configureShell(Shell newShell) {
         super.configureShell(newShell);
         newShell.setText("Settings");
         setTitle("Server Configuration");
     }
     
     @Override
     protected Control createDialogArea(Composite parent) {
         Composite control = (Composite)super.createDialogArea(parent);
         
         setTitle("Server Configuration");
         setMessage("Specify an already installed WebEngine");
         setTitleImage(Nuxeo.getImage("icons/defcon_wiz.png"));
         
         Composite panel = new Composite(control, SWT.NONE);
         GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(
                 panel);
         GridLayout grid =  new GridLayout(2, false);
         grid.marginWidth = 10;
         grid.marginHeight = 10;
         panel.setLayout(grid);
         
         Label label = new Label(panel, SWT.NONE);
         label.setText("Server Home: ");
 
         text = new Text(panel, SWT.BORDER);
         if (view.config.launcher != null) {
             text.setText(view.config.launcher.getParentFile().getAbsolutePath());
         }
 
         //FileDialog dlg = new FileDialog();
         GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(text);        
         
         
         return control;        
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.jface.dialogs.Dialog#okPressed()
      */
     @Override
     protected void okPressed() {
         String path = text.getText().trim();
         File file = new File(path);
         if (file.isDirectory()) {
             for (String name : file.list()) {
                 if (name.endsWith(".jar") && name.startsWith("nuxeo-runtime-launcher")) {
                     view.config.launcher = new File(file, name);
                    view.getViewSite().getActionBars().updateActionBars();
                     super.okPressed(); 
                     return;
                 }
             }
         }        
         Status s = new Status(IStatus.ERROR, Nuxeo.PLUGIN_ID, "The specified file is not a valid WebEngine installation directory");
         ErrorDialog dialog = new ErrorDialog(getShell(), "Error",
                 "Invalid WebEngine home directory", s, IStatus.ERROR
                 | IStatus.WARNING | IStatus.INFO);
         dialog.open();
     }
 
 }
