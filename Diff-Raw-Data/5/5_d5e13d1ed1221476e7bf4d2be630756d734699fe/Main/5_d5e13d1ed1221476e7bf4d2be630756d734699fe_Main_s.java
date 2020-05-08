 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.gui;
 
 import java.awt.event.ActionEvent;
 import java.io.File;
 
 import javax.swing.SwingUtilities;
 
 import org.trypticon.hex.gui.notebook.Notebook;
 import org.trypticon.hex.gui.notebook.NotebookStorage;
 import org.trypticon.hex.gui.sample.OpenSampleNotebookAction;
 import org.trypticon.hex.util.swingsupport.PLAFUtils;
 
 /**
  * Main entry point.
  *
  * @author trejkaz
  */
 public class Main {
     public static void main(final String[] args) throws Exception {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 try {
                     new Main().execute(args);
                 } catch (RuntimeException e) {
                     throw e;
                 } catch (Exception e) {
                     // TODO: Generic error dialog.  Need a utility for this.
                     throw new RuntimeException(e);
                 }
             }
         });
     }
 
     public void execute(Object[] args) throws Exception {
        PLAFBootstrap.init();

         // If not running on Quaqua, it is impossible to start up without at least one document open.
         // For now, we will resolve this by opening the sample but another way would be supporting the
         // frame having no documents open (which would be bad on Mac...)
         if (!PLAFUtils.isQuaqua()) {
             HexFrame.ensureFrameOpen();
         }
 
         final WorkspaceStateTracker stateTracker = new WorkspaceStateTracker();
         boolean openSample = !stateTracker.restore();
 
         if (args.length == 1 && args[0] instanceof String) {
             // TODO: Support a URL here too.
             // TODO: Support binary here too. Find a way to distinguish this in this context.
             File file = new File((String) args[0]);
             Notebook notebook = new NotebookStorage().read(file.toURI().toURL());
             HexFrame.openNotebook(notebook);
             openSample = false;
         }
 
         if (openSample) {
             new OpenSampleNotebookAction().actionPerformed(
                     new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Open Sample Notebook"));
         }
 
     }
 }
