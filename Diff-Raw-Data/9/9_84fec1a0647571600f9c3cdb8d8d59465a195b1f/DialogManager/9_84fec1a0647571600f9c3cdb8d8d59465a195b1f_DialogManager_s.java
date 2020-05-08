 /*
  * Copyright (c) 2006-2013 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs;
 
 import com.dmdirc.ServerManager;
 import com.dmdirc.actions.ActionFactory;
 import com.dmdirc.actions.ActionSubstitutorFactory;
 import com.dmdirc.actions.wrappers.PerformWrapper;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.SwingWindowFactory;
 import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
 import com.dmdirc.interfaces.LifecycleController;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigProvider;
 import com.dmdirc.interfaces.config.IdentityController;
 import com.dmdirc.interfaces.ui.StatusBar;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.core.util.URLHandler;
 import com.dmdirc.util.SimpleInjector;
 
 import java.awt.Window;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import lombok.extern.slf4j.Slf4j;
 
 /**
  * Manages the DMDirc dialogs, creates and disposes as required to ensure only
  * the required number exist at any one time.
  */
 @Slf4j
 public class DialogManager {
 
     /** Controller used for standard parameters for dependency injection. */
     private final SwingController controller;
     /** Stores all known dialogs in this manager. */
     private final Map<Class<? extends StandardDialog>, StandardDialog> dialogs;
 
     /**
      * Creates a new instance of the dialog manager.
      *
      * @param controller Parent Swing controller
      */
     public DialogManager(final SwingController controller) {
         this.controller = controller;
         dialogs = Collections.synchronizedMap(
                 new HashMap<Class<? extends StandardDialog>, StandardDialog>());
     }
 
     /**
      * Creates, or retrieves, an instance of the specified dialog and displays
      * this dialog to the user. For more details on what parameters might be
      * required see {@link DialogManager#getDialog(Class, Object...)}
      *
      * @param <T> Dialog type
      *
      * @see DialogManager#getDialog(Class, Object...) getDialog
      *
      * @param klass The class of the dialog to show
      * @param params Any non standard parameters required
      *
      * @return Dialog instance
      */
     public <T extends StandardDialog> T showDialog(final Class<T> klass,
             final Object... params) {
         final T dialog = getDialog(klass, params);
         if (dialog.isVisible()) {
             log.trace("Requesting focus for dialog: {}", klass);
             dialog.requestFocus();
         } else {
             log.trace("Display new dialog: {}", klass);
             dialog.display();
         }
         return dialog;
     }
 
     /**
      * Creates, or retrieves, an instance of the specified dialog.
      *
      * The following parameters will automatically be provided when creating a
      * dialog, any extra parameters will need to be provided.
      * <ul>
      *   <li>SwingController</li>
      *   <li>IconManager</li>
      *   <li>IdentityManager</li>
      *   <li>ConfigManager</li>
      *   <li>MainFrame</li>
      *   <li>Window</li>
      *   <li>SwingStatusBar</li>
      *   <li>StatusBar</li>
      *   <li>SwingWindowFactory</li>
      *   <li>URLHandler</li>
      *   <li>DialogManager</li>
      * </ul>
      *
      * @param <T> Dialog type
      *
      * @param klass The class of the dialog to show
      * @param params Any non standard parameters required
      *
      * @return Dialog instance
      */
     @SuppressWarnings("unchecked")
     public <T extends StandardDialog> T getDialog(final Class<T> klass,
             final Object... params) {
         final T instance;
         if (dialogs.containsKey(klass)) {
             log.trace("Getting pre-existing dialog: {}", klass);
             instance = (T) dialogs.get(klass);
         } else {
             log.trace("Creating new dialog: {}", klass);
             final SimpleInjector injector = getInjector(params);
             instance = injector.createInstance(klass);
 
             dialogs.put(instance.getClass(), instance);
         }
         return instance;
     }
 
     /**
      * Creates and initialises the a dependency injector that can be used to
      * create a dialog.
      *
      * The following parameters will automatically be provided when creating a
      * dialog, any extra parameters will need to be provided.
      * <ul>
      *   <li>SwingController</li>
      *   <li>IconManager</li>
      *   <li>IdentityManager</li>
      *   <li>ConfigManager</li>
      *   <li>MainFrame</li>
      *   <li>Window</li>
      *   <li>SwingStatusBar</li>
      *   <li>StatusBar</li>
      *   <li>SwingWindowFactory</li>
      *   <li>URLHandler</li>
      *   <li>DialogManager</li>
      * </ul>
      *
      * @param params Extra parameters to inject.
      *
      * @return Injector with parameters added
      */
     private SimpleInjector getInjector(final Object... params) {
         final SimpleInjector injector = new SimpleInjector();
 
         injector.addParameter(SwingController.class, controller);
         injector.addParameter(IconManager.class, controller.getIconManager());
         injector.addParameter(IdentityController.class, controller.getIdentityManager());
         injector.addParameter(ConfigProvider.class, controller.getGlobalIdentity());
         injector.addParameter(AggregateConfigProvider.class, controller.getGlobalConfig());
         injector.addParameter(MainFrame.class, controller.getMainFrame());
         injector.addParameter(LifecycleController.class, controller.getLifecycleController());
         injector.addParameter(ActionSubstitutorFactory.class, controller.getActionSubstitutorFactory());
         injector.addParameter(Window.class, controller.getMainFrame());
         injector.addParameter(SwingStatusBar.class, controller.getSwingStatusBar());
         injector.addParameter(StatusBar.class, controller.getSwingStatusBar());
         injector.addParameter(SwingWindowFactory.class, controller.getWindowFactory());
         injector.addParameter(URLHandler.class, controller.getUrlHandler());
         injector.addParameter(DialogManager.class, this);
         injector.addParameter(PerformWrapper.class, controller.getPerformWrapper());
         injector.addParameter(ServerManager.class, controller.getServerManager());
         injector.addParameter(PrefsComponentFactory.class, controller.getPrefsComponentFactory());
         injector.addParameter(ActionFactory.class, controller.getActionFactory());
 
         for (final Object param : params) {
             injector.addParameter(param);
         }
 
         return injector;
     }
 
     /**
      * Closes the previously created instance of a dialog.
      *
      * @param klass The class of the dialog to show
      */
     public void close(final Class<? extends StandardDialog> klass) {
         final StandardDialog dialog = dispose(klass);
         if (dialog != null) {
             dialog.dispose();
         }
     }
 
     /**
      * Removes a previously created instance of a dialog from the dialog
      * manager.
      *
      * @param klass The class of the dialog to show
      *
      * @return Instance of the dialog that has been removed or null if there
      * was no dialog created
      */
     public StandardDialog dispose(final Class<? extends StandardDialog> klass) {
         if (dialogs.containsKey(klass)) {
             return dialogs.remove(klass);
         }
         return null;
     }
 
     /**
      * Removes a previously created instance of a dialog from the dialog
      * manager.
      *
      * @param dialog Dialog to dispose of
      *
      * @return Instance of the dialog that has been removed or null if there was no dialog created
      */
     public StandardDialog dispose(final StandardDialog dialog) {
         if (dialogs.containsKey(dialog.getClass())) {
             log.trace("Disposing of known dialog: {}", dialog);
             return dispose(dialog.getClass());
         }
         log.trace("Unknown dialog, not disposing: {}", dialog);
         return null;
     }
 }
