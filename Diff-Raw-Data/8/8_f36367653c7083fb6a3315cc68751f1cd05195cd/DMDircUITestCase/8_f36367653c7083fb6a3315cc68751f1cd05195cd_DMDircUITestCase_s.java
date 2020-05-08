 /*
  * Copyright (c) 2006-2011 DMDirc Developers
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
 
 package com.dmdirc.harness.ui;
 
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.PluginMetaData;
 import com.dmdirc.Main;
 import com.dmdirc.WritableFrameContainer;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.SwingWindowFactory;
 import com.dmdirc.config.ConfigManager;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.config.InvalidIdentityFileException;
 import com.dmdirc.plugins.PluginManager;
 import com.dmdirc.ui.messages.IRCDocument;
 import com.dmdirc.updater.Version;
 
 import org.uispec4j.UISpecTestCase;
 import static org.mockito.Mockito.*;
 
 /**
  * Provides a basic test environment for testing the DMDirc Swing UI.
  */
 public class DMDircUITestCase extends UISpecTestCase {
 
     static {
         try {
             IdentityManager.load();
         } catch (InvalidIdentityFileException ex) {
             //Ignore
         }
 
         IdentityManager.getAddonIdentity().setOption("test", "windowMenuScrollInterval", "1");
         IdentityManager.getAddonIdentity().setOption("test", "desktopbackground", "");
         IdentityManager.getAddonIdentity().setOption("test", "desktopbackgroundoption", "SCALE");
         IdentityManager.getAddonIdentity().setOption("test", "windowMenuItems", "1");
         IdentityManager.getAddonIdentity().setOption("test", "windowMenuScrollInterval", "1");
         Main.ensureExists(PluginManager.getPluginManager(), "tabcompletion");
     }
 
     /**
      * Returns a mocked SwingController.
      *
      * @return SwingController mock
      */
     public static SwingController getMockedController() {
         final PluginInfo pluginInfo = mock(PluginInfo.class);
         final PluginMetaData metaData = mock(PluginMetaData.class);
        SwingController controller = mock(SwingController.class);
         final SwingWindowFactory windowFactory = mock(SwingWindowFactory.class);
         when(controller.getDomain()).thenReturn("test");
         when(controller.getWindowFactory()).thenReturn(windowFactory);
         when(controller.getVersion()).thenReturn(new Version(1));
         when(pluginInfo.getMetaData()).thenReturn(metaData);
         when(metaData.getVersion()).thenReturn(new Version(1));
 
         return controller;
     }
 
     /**
      * Returns a mocked SwingController that contains a mocked Main Frame.
      *
      * @return SwingController + MainFrame mock
      */
     public static SwingController getMockedControllerAndMainFrame() {
         SwingController controller = getMockedController();
         final MainFrame mainFrame = new MainFrame(controller);
         when(controller.getMainFrame()).thenReturn(mainFrame);
 
         return controller;
     }
 
     /**
      * Returns a mocked WritableFrameContainer.
      *
      * @return FrameContainer mock
      */
     public static WritableFrameContainer getMockedContainer() {
         final IRCDocument document = mock(IRCDocument.class);
         @SuppressWarnings("unchecked")
         final WritableFrameContainer container = mock(
                 WritableFrameContainer.class);
         final ConfigManager config = mock(ConfigManager.class);
         when(container.getDocument()).thenReturn(document);
         when(container.getConfigManager()).thenReturn(config);
         when(config.getOption(anyString(), anyString())).thenReturn("mirc");
 
         return container;
     }
 }
