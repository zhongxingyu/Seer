 /**
  * Get more info at : www.jrebirth.org .
  * Copyright JRebirth.org © 2011-2013
  * Contact : sebastien.bordes@jrebirth.org
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
  */
 package org.jrebirth.demo.masteringtables.ui.splash;
 
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.control.ProgressBar;
 import javafx.scene.layout.BorderPane;
 
 import org.jrebirth.af.api.ui.annotation.RootNodeId;
 import org.jrebirth.af.core.ui.simple.DefaultSimpleModel;
 import org.jrebirth.af.core.wave.Builders;
 import org.jrebirth.af.core.wave.JRebirthWaves;
 import org.jrebirth.demo.masteringtables.command.DisplayGameMenu;
 import org.jrebirth.demo.masteringtables.service.ExpressionBuilderService;
import org.jrebirth.demo.masteringtables.service.ExpressionBuilderServiceImpl;
 
 /**
  * The Class SplashModel used to display the Mastering GTables Splash Screen with progress bar.
  */
 @RootNodeId("SplashPanel")
 public class SplashModel extends DefaultSimpleModel<BorderPane> {
 
     /** The loading bar related to expression calculation. */
     private ProgressBar loadingBar;
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void showView() {
 
         // Patch for webstart since module.xml is not correctly read
        getLocalFacade().getGlobalFacade().getComponentFactory().register(ExpressionBuilderService.class, ExpressionBuilderServiceImpl.class);
         getService(ExpressionBuilderService.class);
 
         // Call a command that will perform a service call to update the progress bar
         callCommand(DisplayGameMenu.class, Builders.waveData(JRebirthWaves.PROGRESS_BAR, getLoadingBar()));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void initSimpleView() {
 
         this.loadingBar = new ProgressBar(0.0);
         this.loadingBar.setMinSize(400, 40);
         BorderPane.setAlignment(this.loadingBar, Pos.CENTER);
         BorderPane.setMargin(this.loadingBar, new Insets(40, 0, 30, 0));
 
         getRootNode().setBottom(this.loadingBar);
     }
 
     /**
      * @return Returns the loadingBar.
      */
     public ProgressBar getLoadingBar() {
         return this.loadingBar;
     }
 
 }
