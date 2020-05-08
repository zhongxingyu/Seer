 /**
  *
  * Copyright 2010 Laurent Desgrange
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package net.desgrange.pwad.functional;
 
 import static org.uispec4j.assertion.UISpecAssert.assertTrue;
 import net.desgrange.pwad.functional.utils.PwadTestCase;
 
 import org.junit.Test;
 import org.uispec4j.MenuItem;
 import org.uispec4j.Trigger;
 import org.uispec4j.Window;
 import org.uispec4j.interception.WindowHandler;
 import org.uispec4j.interception.WindowInterceptor;
 
 public class DisplayAboutDialogTest extends PwadTestCase {
     @Test
     public void testUserCanSeeSomeInformationsAboutTheApplication() {
         final Window window = getMainWindow();
         final MenuItem fileMenu = window.getMenuBar().getMenu("File");
         WindowInterceptor.init(fileMenu.getSubMenu("About pwad").triggerClick()).process(new WindowHandler() {
             @Override
             public Trigger process(final Window dialog) throws Exception {
                assertTrue(dialog.getTextBox("aboutLabel").textContains("<b>pwad</b>", "Version ", "pwad homepage", "© Laurent Desgrange, 2010"));
                 dialog.dispose();
                 return Trigger.DO_NOTHING;
             }
         }).run();
     }
 }
