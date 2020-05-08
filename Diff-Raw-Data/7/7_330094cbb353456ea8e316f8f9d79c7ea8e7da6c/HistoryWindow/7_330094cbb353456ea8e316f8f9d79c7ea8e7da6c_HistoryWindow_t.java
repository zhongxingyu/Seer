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
 
 package com.dmdirc.addons.logging;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.FrameContainer;
import com.dmdirc.events.UserErrorEvent;
 import com.dmdirc.interfaces.Connection;
import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.ui.core.components.WindowComponent;
 import com.dmdirc.ui.messages.ColourManagerFactory;
 import com.dmdirc.util.URLBuilder;
 import com.dmdirc.util.io.ReverseFileReader;
 
 import com.google.common.base.Optional;
 
 import java.io.IOException;
 import java.nio.file.Path;
 import java.util.Collections;
 
 /**
  * Displays an extended history of a window.
  */
 public class HistoryWindow extends FrameContainer {
 
     /**
      * Creates a new HistoryWindow.
      *
      * @param title      The title of the window
      * @param logFile    The logfile to read and display
      * @param parent     The window this history window was opened from
      * @param urlBuilder The URL builder to use when finding icons.
      * @param eventBus   The bus to dispatch events on.
      * @param numLines   The number of lines to show
      */
     public HistoryWindow(
             final String title,
             final Path logFile,
             final FrameContainer parent,
             final URLBuilder urlBuilder,
             final DMDircMBassador eventBus,
             final ColourManagerFactory colourManagerFactory,
             final int numLines) {
         super(parent, "raw", title, title, parent.getConfigManager(), colourManagerFactory,
                 urlBuilder, eventBus,
                 Collections.singletonList(WindowComponent.TEXTAREA.getIdentifier()));
 
         final int frameBufferSize = parent.getConfigManager().getOptionInt(
                 "ui", "frameBufferSize");
         try (final ReverseFileReader reader = new ReverseFileReader(logFile)) {
             addLine(reader.getLinesAsString(Math.min(frameBufferSize, numLines)), false);
         } catch (IOException | SecurityException ex) {
            eventBus.publishAsync(
                    new UserErrorEvent(ErrorLevel.MEDIUM, ex, "Unable to read log file.", ""));
         }
 
     }
 
     @Override
     public Connection getConnection() {
         final Optional<FrameContainer> parent = getParent();
         return parent.isPresent() ? parent.get().getConnection() : null;
     }
 
 }
