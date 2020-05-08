 package org.jtheque.views.impl.controllers;
 
 import org.jtheque.collections.CollectionListener;
 import org.jtheque.collections.CollectionsService;
 import org.jtheque.core.Core;
 import org.jtheque.core.lifecycle.LifeCycle;
 import org.jtheque.errors.ErrorService;
 import org.jtheque.errors.Errors;
 import org.jtheque.modules.Module;
 import org.jtheque.modules.ModuleException;
 import org.jtheque.modules.ModuleException.ModuleOperation;
 import org.jtheque.modules.ModuleService;
 import org.jtheque.modules.ModuleState;
 import org.jtheque.ui.Action;
 import org.jtheque.ui.Controller;
 import org.jtheque.ui.UIUtils;
 import org.jtheque.ui.utils.AbstractController;
 import org.jtheque.ui.utils.BetterSwingWorker;
 import org.jtheque.updates.InstallationResult;
 import org.jtheque.updates.UpdateService;
 import org.jtheque.utils.StringUtils;
 import org.jtheque.utils.io.SimpleFilter;
 import org.jtheque.utils.ui.SwingUtils;
 import org.jtheque.views.ViewService;
 import org.jtheque.views.panel.ModuleView;
 import org.jtheque.views.panel.RepositoryView;
 
 import javax.annotation.Resource;
 
 import java.io.File;
 
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
  * A controller for the module view.
  *
  * @author Baptiste Wicht
  */
 public class ModuleController extends AbstractController<ModuleView> {
     @Resource
     private UIUtils uiUtils;
 
     @Resource
     private ModuleService moduleService;
 
     @Resource
     private LifeCycle lifeCycle;
 
     @Resource
     private CollectionsService collectionsService;
 
     @Resource
     private ErrorService errorService;
 
     @Resource
     private ViewService viewService;
 
     @Resource
     private UpdateService updateService;
 
     @Resource
     private Controller<RepositoryView> repositoryController;
 
     /**
      * Create a new ModuleController.
      */
     public ModuleController() {
         super(ModuleView.class);
     }
 
     /**
      * Disable the selected module.
      */
    @Action("modules.actions.disable")
     public void disableModule() {
         Module module = getView().getSelectedModule();
 
         String error = moduleService.canBeDisabled(module);
 
         if (StringUtils.isEmpty(error)) {
             try {
                 moduleService.disableModule(module);
             } catch (ModuleException e) {
                 addModuleError(e, "error.module.cannot.stop");
             }
 
             getView().refreshList();
         } else {
             uiUtils.displayI18nText(error);
         }
     }
 
     /**
      * Enable the selected module.
      */
    @Action("modules.actions.enable")
     public void enableModule() {
         Module module = getView().getSelectedModule();
 
         if (module.getState() == ModuleState.DISABLED) {
             if (module.getCoreVersion().isGreaterThan(Core.VERSION)) {
                 uiUtils.displayI18nText("modules.message.version.problem");
             } else {
                 moduleService.enableModule(module);
                 getView().refreshList();
             }
         } else {
             uiUtils.displayI18nText("error.module.not.disabled");
         }
     }
 
     /**
      * Install a module from a file.
      */
     @Action("modules.actions.file.new")
     public void installFile() {
         File file = SwingUtils.chooseFile(new SimpleFilter("JAR File (*.jar)", "jar"));
 
         if (file != null) {
             try {
                 moduleService.installModule(file);
 
                 uiUtils.displayI18nText("message.module.installed");
             } catch (ModuleException e) {
                 addModuleError(e, "error.module.cannot.install");
             }
         }
     }
 
     private void addModuleError(ModuleException e, String defaultMessage) {
         if (e.hasI18nMessage()) {
             errorService.addError(Errors.newI18nError(e.getI18nMessage(), e));
         } else {
             errorService.addError(Errors.newI18nError(defaultMessage, e));
         }
     }
 
     /**
      * Install a module from an URL.
      */
     @Action("modules.actions.url.new")
     public void installURL() {
         String url = uiUtils.askI18nText("dialogs.modules.installFromRepository.url");
 
         if (StringUtils.isNotEmpty(url)) {
             InstallationResult result = updateService.installModule(url);
 
             if (result.isInstalled()) {
                 try {
                     moduleService.installFromRepository(result.getJarFile());
                 } catch (ModuleException e) {
                     addModuleError(e, "error.repository.module.not.installed");
                 }
             } else {
                 uiUtils.displayI18nText("error.repository.module.not.installed");
             }
         }
     }
 
     /**
      * Uninstall the given module.
      */
     @Action("modules.actions.uninstall")
     public void uninstallModule() {
         Module module = getView().getSelectedModule();
 
         String error = moduleService.canBeUninstalled(module);
 
         if (StringUtils.isEmpty(error)) {
             boolean confirm = uiUtils.askI18nUserForConfirmation(
                     "dialogs.confirm.uninstall",
                     "dialogs.confirm.uninstall.title");
 
             if (confirm) {
                 try {
                     moduleService.uninstallModule(module);
                 } catch (ModuleException e) {
                     if (e.getOperation() == ModuleOperation.STOP) {
                         addModuleError(e, "error.module.cannot.stop");
                     } else if (e.getOperation() == ModuleOperation.UNINSTALL) {
                         addModuleError(e, "error.module.cannot.uninstall");
                     }
                 }
 
                 getView().refreshList();
             }
         } else {
             uiUtils.displayI18nText(error);
         }
     }
 
     /**
      * Stop the selected module.
      */
     @Action("modules.actions.stop")
     public void stopModule() {
         final Module module = getView().getSelectedModule();
 
         String error = moduleService.canBeStopped(module);
 
         if (StringUtils.isEmpty(error)) {
             new StopModuleWorker(module).execute();
         } else {
             uiUtils.displayI18nText(error);
         }
     }
 
     /**
      * Start the selected module.
      */
     @Action("modules.actions.start")
     public void startModule() {
         Module module = getView().getSelectedModule();
 
         String error = moduleService.canBeStarted(module);
 
         if (StringUtils.isEmpty(error)) {
             if (moduleService.needTwoPhasesLoading(module)) {
                 getView().closeDown();
                 viewService.displayCollectionView();
                 collectionsService.addCollectionListener(new StartModuleWorker(module));
             } else {
                 new StartModuleWorker(module).execute();
             }
         } else {
             uiUtils.displayI18nText(error);
         }
     }
 
     /**
      * Update the selected module.
      */
     @Action("modules.actions.update")
     public void updateModule() {
         final Module module = getView().getSelectedModule();
 
         if (updateService.isUpToDate(module)) {
             uiUtils.displayI18nText("message.update.no.version");
         } else {
             new UpdateModuleWorker(module).execute();
         }
     }
 
     /**
      * Update the core.
      */
     @Action("modules.actions.update.kernel")
     public void updateCore() {
         if (updateService.isCurrentVersionUpToDate()) {
             uiUtils.displayI18nText("message.update.no.version");
         } else {
             new UpdateCoreWorker().execute();
         }
     }
 
     /**
      * Display the repository.
      */
     @Action("modules.actions.repository")
     public void repository() {
         repositoryController.getView().display();
     }
 
     /**
      * A simple swing worker to make work in the module view. The module view is waiting during the operations.
      *
      * @author Baptiste Wicht
      */
     private abstract class ModuleWorker extends BetterSwingWorker {
         @Override
         protected final void before() {
             getView().getWindowState().startWait();
         }
 
         @Override
         protected final void done() {
             getView().refreshList();
             getView().getWindowState().stopWait();
         }
     }
 
     /**
      * A simple swing worker to start the module.
      *
      * @author Baptiste Wicht
      */
     private final class StartModuleWorker extends ModuleWorker implements CollectionListener {
         private final Module module;
 
         /**
          * Construct a new StartModuleWorker for the given module.
          *
          * @param module The module to start.
          */
         private StartModuleWorker(Module module) {
             this.module = module;
         }
 
         @Override
         protected void doInBackground() {
             try {
                 moduleService.startModule(module);
             } catch (ModuleException e) {
                 addModuleError(e, "error.module.cannot.start");
             }
         }
 
         @Override
         public void collectionChosen() {
             collectionsService.removeCollectionListener(this);
 
             viewService.closeCollectionView();
 
             execute();
 
             getView().display();
         }
     }
 
     /**
      * A simple swing worker to stop the module.
      *
      * @author Baptiste Wicht
      */
     private final class StopModuleWorker extends ModuleWorker {
         private final Module module;
 
         /**
          * Construct a new StopModuleWorker for the given module.
          *
          * @param module The module to start.
          */
         private StopModuleWorker(Module module) {
             this.module = module;
         }
 
         @Override
         protected void doInBackground() {
             try {
                 moduleService.stopModule(module);
             } catch (ModuleException e) {
                 addModuleError(e, "error.module.cannot.stop");
             }
         }
     }
 
     /**
      * A simple swing worker to update the core.
      *
      * @author Baptiste Wicht
      */
     private final class UpdateCoreWorker extends ModuleWorker {
         @Override
         protected void doInBackground() {
             updateService.updateCore();
             lifeCycle.restart();
         }
     }
 
     /**
      * A simple swing worker to update a module.
      *
      * @author Baptiste Wicht
      */
     private final class UpdateModuleWorker extends ModuleWorker {
         private final Module module;
 
         /**
          * Construct a new UpdateModuleWorker for the given module.
          *
          * @param module The module to start.
          */
         private UpdateModuleWorker(Module module) {
             this.module = module;
         }
 
         @Override
         protected void doInBackground() {
             boolean restart = false;
 
             if (module.getState() == ModuleState.STARTED) {
                 try {
                     moduleService.stopModule(module);
                 } catch (ModuleException e) {
                     addModuleError(e, "error.module.cannot.stop");
 
                     return;
                 }
 
                 restart = true;
             }
 
             updateService.update(module);
 
             if (restart) {
                 try {
                     moduleService.startModule(module);
                 } catch (ModuleException e) {
                     addModuleError(e, "error.module.cannot.start");
                 }
             }
         }
     }
 }
