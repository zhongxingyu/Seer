 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.faces.plugin;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.beans.PluginRegistryBean;
 import com.flexive.faces.javascript.tree.TreeNodeWriter;
 
 /**
  * Demo plugin factory.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class DemoPluginFactory implements PluginFactory {
     /**
      * Plugin to extend the administration main menu
      */
     private static class MainMenuPlugin implements Plugin<TreePluginExecutor> {
         /**
          * Add main menu items
          *
          * @param executor the tree plugin executor
          */
         public void apply(TreePluginExecutor executor) {
             // add a main menu entry in the "system" folder
             executor.addNode("system", new TreeNodeWriter.Node("demoPluginNode",
                     "System plugin node", TreeNodeWriter.Node.TITLE_CLASS_NODE, "find",
                     "javascript:alert('Plugin node clicked')"));
             // now add a child to the previously created node
             executor.addNode("demoPluginNode", new TreeNodeWriter.Node("demoSubNode",
                     "Nested plugin node", TreeNodeWriter.Node.TITLE_CLASS_LEAF, "workflows",
                     FxJsfUtils.getRequest().getContextPath() + "/adm/main/systemInfo.jsf"));
         }
     }
 
     private static class SystemInfoToolbarPlugin implements Plugin<ToolbarPluginExecutor> {
         private static final ToolbarPluginExecutor.Button LOGOUT_BUTTON = new ToolbarPluginExecutor.Button("pluginLogoutButton")
                 .setBean("fxAuthenticationBean")
                 .setAction("logout")
                 .setLabel("Logout from plugin button")
                 .setIcon("cancel");
 
         public void apply(ToolbarPluginExecutor executor) {
             executor.addToolbarButton("main/systemInfo", LOGOUT_BUTTON);
         }
     }
 
     public void initialize(PluginRegistryBean registry) {
//        registry.registerPlugin(AdmExtensionPoints.ADM_MAIN_NAVIGATION, new MainMenuPlugin());
//        registry.registerPlugin(AdmExtensionPoints.ADM_TOOLBAR_PLUGINS, new SystemInfoToolbarPlugin());
     }
 
 }
