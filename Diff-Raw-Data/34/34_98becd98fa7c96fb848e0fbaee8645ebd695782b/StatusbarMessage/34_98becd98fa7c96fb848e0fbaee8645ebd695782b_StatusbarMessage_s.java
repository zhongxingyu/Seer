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
 
 package com.dmdirc.addons.debug.commands;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.debug.Debug;
 import com.dmdirc.addons.debug.DebugCommand;
 import com.dmdirc.commandparser.CommandArguments;
 import com.dmdirc.commandparser.commands.context.CommandContext;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.ui.StatusMessage;
import com.dmdirc.ui.core.components.StatusBarManager;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 
 /**
  * Outputs a test message to the status bar.
  */
 public class StatusbarMessage extends DebugCommand {
 
    /** The status bar manager to show messages in. */
    private final StatusBarManager statusBarManager;
     /** The global configuration. */
     private final AggregateConfigProvider globalConfig;
 
     /**
      * Creates a new instance of the command.
      *
      * @param commandProvider  The provider to use to access the main debug command.
     * @param statusBarManager The status bar manager to show messages in.
      * @param globalConfig     The global configuration to use.
      */
     @Inject
     public StatusbarMessage(
             final Provider<Debug> commandProvider,
            final StatusBarManager statusBarManager,
             @GlobalConfig final AggregateConfigProvider globalConfig) {
         super(commandProvider);
 
        this.statusBarManager = statusBarManager;
         this.globalConfig = globalConfig;
     }
 
     @Override
     public String getName() {
         return "statusmessage";
     }
 
     @Override
     public String getUsage() {
         return "message - Sets the status bar message";
     }
 
     @Override
     public void execute(final FrameContainer origin,
             final CommandArguments args, final CommandContext context) {
        statusBarManager.setMessage(new StatusMessage(null,
                "Test: " + args.getArgumentsAsString(), null, 5, globalConfig));
     }
 
 }
