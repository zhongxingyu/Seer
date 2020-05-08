 package org.jtheque.views.impl.components.menu;
 
 import org.jtheque.features.able.IFeature;
import org.jtheque.i18n.impl.LanguageService;
 import org.jtheque.ui.able.IController;
 import org.jtheque.undo.able.IUndoRedoService;
 import org.jtheque.views.able.windows.IMainView;
 import org.jtheque.views.impl.ViewsResources;
 import org.jtheque.views.impl.actions.undo.RedoAction;
 import org.jtheque.views.impl.actions.undo.UndoAction;
 import org.jtheque.views.utils.OSGIMenu;
 
 import java.awt.event.KeyEvent;
 import java.util.List;
 
 /*
  * Copyright JTheque (Baptiste Wicht)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /**
  * The menu of the core.
  *
  * @author Baptiste Wicht
  */
 public final class CoreMenu extends OSGIMenu {
     @Override
     protected List<IFeature> getFileMenuSubFeatures() {
         IController<IMainView> controller = getBean("generalController");
 
         return features(
                 createSeparatedSubFeature(200, createControllerAction("menu.backup", controller), ViewsResources.XML_ICON),
                 createSubFeature(201, createControllerAction("menu.restore", controller), ViewsResources.XML_ICON),
                 createSeparatedSubFeature(1000, createControllerAction("menu.exit", controller), ViewsResources.EXIT_ICON, KeyEvent.VK_E)
         );
     }
 
     @Override
     protected List<IFeature> getEditMenuSubFeatures() {
         IUndoRedoService undoRedoService = getService(IUndoRedoService.class);
        LanguageService languageService = getService(LanguageService.class);
 
         return features(
                createSubFeature(1, new UndoAction(undoRedoService, languageService), ViewsResources.UNDO_ICON),
                createSubFeature(2, new RedoAction(undoRedoService, languageService), ViewsResources.REDO_ICON)
         );
     }
 
     @Override
     protected List<IFeature> getAdvancedMenuSubFeatures() {
         IController<IMainView> controller = getBean("generalController");
 
         return features(
                 createSeparatedSubFeature(500, createControllerAction("menu.config", controller), ViewsResources.OPTIONS_ICON),
                 createSeparatedSubFeature(750, createControllerAction("menu.modules", controller), ViewsResources.UPDATE_ICON)
         );
     }
 
     @Override
     protected List<IFeature> getHelpMenuSubFeatures() {
         IController<IMainView> controller = getBean("generalController");
 
         return features(
                 createSeparatedSubFeature(1, createControllerAction("menu.help", controller), ViewsResources.HELP_ICON, KeyEvent.VK_F1),
                 createSeparatedSubFeature(2, createControllerAction("menu.bug", controller), ViewsResources.MAIL_ICON),
                 createSeparatedSubFeature(4, createControllerAction("menu.improvement", controller), ViewsResources.IDEA_ICON),
                 createSeparatedSubFeature(6, createControllerAction("menu.messages", controller)),
                 createSeparatedSubFeature(6, createControllerAction("menu.events", controller)),
                 createSeparatedSubFeature(6, createControllerAction("menu.errors", controller)),
                 createSeparatedSubFeature(6, createControllerAction("menu.about", controller), ViewsResources.MAIL_ICON)
         );
     }
 }
