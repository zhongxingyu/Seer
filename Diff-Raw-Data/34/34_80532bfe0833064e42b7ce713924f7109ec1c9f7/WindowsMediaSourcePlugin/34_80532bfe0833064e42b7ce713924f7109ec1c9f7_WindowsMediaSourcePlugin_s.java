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
 
 package com.dmdirc.addons.mediasource_windows;
 
 import com.dmdirc.addons.nowplaying.MediaSource;
 import com.dmdirc.addons.nowplaying.MediaSourceManager;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.implementations.BasePlugin;
 import com.dmdirc.plugins.implementations.PluginFilesHelper;
 import com.dmdirc.util.io.StreamReader;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Manages all Windows based media sources.
  */
 public class WindowsMediaSourcePlugin extends BasePlugin
         implements MediaSourceManager {
 
     /** Media sources. */
     private final List<MediaSource> sources;
     /** Plugin files helper. */
     private final PluginFilesHelper filesHelper;
 
     /**
      * Creates a new instance of DcopMediaSourcePlugin.
      *
      * @param pluginInfo This plugin's plugin info
      */
     public WindowsMediaSourcePlugin(final PluginInfo pluginInfo) {
         super();
         this.filesHelper = new PluginFilesHelper(pluginInfo);
         sources = new ArrayList<>();
         sources.add(new DllSource(this, "Winamp", true));
         sources.add(new DllSource(this, "iTunes", false));
     }
 
     @Override
     public List<MediaSource> getSources() {
         return sources;
     }
 
     /**
      * Get the output from GetMediaInfo.exe for the given player and method
      *
      * @param player Player to ask about
      * @param method Method to call
      *
      * @return a MediaInfoOutput with the results
      */
     protected MediaInfoOutput getOutput(final String player, final String method) {
         try {
             final Process myProcess = Runtime.getRuntime().exec(new String[]{
                 filesHelper.getFilesDirString() + "GetMediaInfo.exe",
                 player,
                 method});
             final StringBuffer data = new StringBuffer();
             new StreamReader(myProcess.getErrorStream()).start();
             new StreamReader(myProcess.getInputStream(), data).start();
             try {
                 myProcess.waitFor();
             } catch (InterruptedException e) {
             }
 
             return new MediaInfoOutput(myProcess.exitValue(), data.toString());
         } catch (SecurityException | IOException e) {
         }
 
         return new MediaInfoOutput(-1, "Error executing GetMediaInfo.exe");
     }
 
     @Override
     public void onLoad() {
         // Extract the .dlls and .exe
         try {
            filesHelper.extractResoucesEndingWith(".dll");
            filesHelper.extractResoucesEndingWith(".exe");
         } catch (IOException ex) {
             Logger.userError(ErrorLevel.MEDIUM, "Unable to extract files for windows media source: "
                     + ex.getMessage(), ex);
         }
     }
 
 }
