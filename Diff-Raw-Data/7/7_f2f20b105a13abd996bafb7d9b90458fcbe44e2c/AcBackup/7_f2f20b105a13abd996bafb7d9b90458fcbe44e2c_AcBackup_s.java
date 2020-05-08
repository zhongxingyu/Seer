 package org.jtheque.views.impl.actions.backup;
 
 import org.jtheque.core.ICore;
 import org.jtheque.core.utils.ImageType;
 import org.jtheque.errors.IErrorService;
 import org.jtheque.file.IFileService;
 import org.jtheque.io.XMLException;
 import org.jtheque.resources.IResourceService;
 import org.jtheque.ui.able.IUIUtils;
 import org.jtheque.ui.utils.actions.JThequeAction;
 import org.jtheque.views.ViewsServices;
 import org.jtheque.utils.io.SimpleFilter;
 import org.slf4j.LoggerFactory;
 
 import java.awt.event.ActionEvent;
 import java.io.File;
 
 /*
  * This file is part of JTheque.
  *
  * JTheque is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License.
  *
  * JTheque is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with JTheque.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * An abstract action to backup.
  *
  * @author Baptiste Wicht
  */
 public class AcBackup extends JThequeAction {
     /**
      * Construct a new AcBackup.
      */
     public AcBackup() {
         super("menu.backup");
 
         setIcon(ViewsServices.get(IResourceService.class).getIcon(ICore.IMAGES_BASE_NAME, "xml", ImageType.PNG));
     }
 
     @Override
     public final void actionPerformed(ActionEvent e) {
         final boolean yes = ViewsServices.get(IUIUtils.class).askI18nUserForConfirmation(
                     "dialogs.confirm.backup", "dialogs.confirm.backup.title");
 
         if (yes) {
             File file = new File(ViewsServices.get(IUIUtils.class).getDelegate().chooseFile(new SimpleFilter("XML(*.xml)", ".xml")));
 
            try {
                ViewsServices.get(IFileService.class).backup(file);
            } catch (XMLException e1) {
                LoggerFactory.getLogger(getClass()).error(e1.getMessage(), e1);
                ViewsServices.get(IErrorService.class).addInternationalizedError("error.backup.error");
            }
         }
     }
 }
