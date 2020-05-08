 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 package com.dmdirc.addons.scriptplugin;
 
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.implementations.BaseCommandPlugin;
 import com.dmdirc.util.validators.ValidationResponse;
 
 import javax.script.ScriptEngineManager;
 
 import dagger.ObjectGraph;
 
 /**
  * This allows javascript scripts to be used in DMDirc.
  */
 public class ScriptPlugin extends BaseCommandPlugin {
 
    private ScriptEngineManager scriptEngineManager;
     private ScriptPluginManager scriptPluginManager;
 
     @Override
     public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
         super.load(pluginInfo, graph);
 
         setObjectGraph(graph.plus(new ScriptModule(pluginInfo)));
         registerCommand(ScriptCommand.class, ScriptCommand.INFO);
 
        scriptEngineManager = getObjectGraph().get(ScriptEngineManager.class);
         scriptPluginManager = getObjectGraph().get(ScriptPluginManager.class);
     }
 
     /** {@inheritDoc} */
     @Override
     public void onLoad() {
         scriptPluginManager.onLoad();
         super.onLoad();
     }
 
     @Override
     public void onUnload() {
         scriptPluginManager.onUnLoad();
         super.onUnload();
     }
 
     @Override
     public ValidationResponse checkPrerequisites() {
        if (scriptEngineManager.getEngineByName("JavaScript") == null) {
             return new ValidationResponse("JavaScript Scripting Engine not found.");
         } else {
             return new ValidationResponse();
         }
     }
 
     /**
      * Get the reason for checkPrerequisites failing.
      *
      * @return Human-Readable reason for checkPrerequisites failing.
      */
     public String checkPrerequisitesReason() {
        if (scriptEngineManager.getEngineByName("JavaScript") == null) {
             return "JavaScript Scripting Engine not found.";
         } else {
             return "";
         }
     }
 
 }
