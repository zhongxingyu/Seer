 /*
  * Copyright 2012 jccastrejon
  *  
  * This file is part of ExSchema.
  *
  * ExSchema is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * ExSchema is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with ExSchema. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.imag.exschema.popup.actions;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ITreeSelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 
 import fr.imag.exschema.Util;
 
 /**
  * Discover schemas action
  * 
  * @author jccastrejon
  * 
  */
 public class DiscoverAction implements IObjectActionDelegate {
 
     /**
      * Eclipse shell.
      */
     private Shell shell;
 
     /**
      * Current selected project.
      */
     private IJavaProject project;
 
     /**
      * Default constructor.
      */
     public DiscoverAction() {
         super();
     }
 
     /**
      * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
      */
     public void setActivePart(IAction action, IWorkbenchPart targetPart) {
         this.shell = targetPart.getSite().getShell();
     }
 
     /**
      * @see IActionDelegate#run(IAction)
      */
     public void run(IAction action) {
         String outputMessage;
 
         if (project != null) {
             try {
                 Util.discoverSchemas(project);
                 outputMessage = "The project schemas have been discovered";
             } catch (Exception e) {
                 e.printStackTrace();
                outputMessage = "An error ocurred while discovering the schemas";
             }
         } else {
            outputMessage = "An error ocurred while discovering the schemas";
         }
         MessageDialog.openInformation(shell, "ExSchema", outputMessage);
     }
 
     /**
      * @see IActionDelegate#selectionChanged(IAction, ISelection)
      */
     public void selectionChanged(IAction action, ISelection selection) {
         Object selectedElement;
         IProject selectedProject;
 
         this.project = null;
         selectedElement = ((ITreeSelection) selection).getFirstElement();
         if (IProject.class.isInstance(selectedElement)) {
             selectedProject = (IProject) selectedElement;
             try {
                 if (selectedProject.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                     this.project = JavaCore.create(selectedProject);
                 }
             } catch (CoreException e) {
             }
         }
     }
 
 }
