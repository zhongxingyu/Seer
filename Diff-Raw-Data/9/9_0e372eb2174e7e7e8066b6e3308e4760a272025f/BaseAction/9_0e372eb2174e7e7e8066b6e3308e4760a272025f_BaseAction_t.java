 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009-2010  Trejkaz, Hex Project
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
 
 package org.trypticon.hex.util.swingsupport;
 
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
 import org.trypticon.hex.util.LoggerUtils;
 
 import java.awt.event.ActionEvent;
 import java.awt.Component;
 import java.awt.Window;
 import java.util.logging.Level;
 import javax.swing.AbstractAction;
 import javax.swing.JOptionPane;
 
 /**
  * A convenience action class with some facility for catching errors.
  *
  * @author trejkaz
  */
 public abstract class BaseAction extends AbstractAction {
 
     public void actionPerformed(ActionEvent event) {
         Window sourceWindow = new SourceWindowFinder().findSourceWindow(event);
 
         try {
             doAction(event);
         } catch (ActionException e) {
             JOptionPane.showMessageDialog(sourceWindow, e.getMessage(), "Hex: Error", JOptionPane.ERROR_MESSAGE);
         } catch (Throwable t) {
             handleError(sourceWindow, t);
         }
     }
 
     protected void handleError(Component owner, Throwable t) {
         LoggerUtils.get().log(Level.SEVERE, "Unexpected error in UI action", t);
 
        JXErrorPane.showDialog(owner, new ErrorInfo("Unexpected Error",
                                                    "Unexpected error in UI action.",
                                                    null, null, t, Level.SEVERE, null));
     }
 
     protected abstract void doAction(ActionEvent event) throws Exception;
 }
