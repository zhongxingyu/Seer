 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.debug;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.commandparser.CommandArguments;
 import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandManager;
 import com.dmdirc.commandparser.CommandType;
 import com.dmdirc.commandparser.commands.Command;
 import com.dmdirc.commandparser.commands.IntelligentCommand;
 import com.dmdirc.commandparser.commands.context.CommandContext;
 import com.dmdirc.ui.input.AdditionalTabTargets;
 
 import java.util.Arrays;
 
 /**
  * Provides various handy ways to test or debug the client.
  */
 public class Debug extends Command implements IntelligentCommand, CommandInfo {
 
     /** Parent debug plugin. */
     private final DebugPlugin plugin;
 
     /**
      * Creates a new debug command with the specified parent plugin.
      *
      * @param plugin Parent debug plugin
      */
     public Debug(final DebugPlugin plugin) {
         super();
 
         this.plugin = plugin;
     }
 
     /** {@inheritDoc} */
     @Override
     public void execute(final FrameContainer<?> origin,
             final CommandArguments args, final CommandContext context) {
         if (args.getArguments().length == 0) {
             showUsage(origin, args.isSilent(), "debug",
                     "<debug command> [options]");
         } else {
             final DebugCommand command = plugin.getCommand(
                     args.getArguments()[0]);
             if (command == null) {
                 sendLine(origin, args.isSilent(), FORMAT_ERROR,
                         "Unknown debug action.");
             } else {
                 final CommandArguments newArgs = new CommandArguments(
                        Arrays.asList((CommandManager.getCommandChar()
                        + command.getName() + " "
                         + args.getArgumentsAsString(1)).split(" ")));
                 command.execute(origin, newArgs, context);
             }
         }
     }
 
     /**
      * Sends a line, if appropriate, to the specified target.
      *
      * @param target The command window to send the line to
      * @param isSilent Whether this command is being silenced or not
      * @param type The type of message to send
      * @param args The arguments of the message
      */
     public void proxySendLine(final FrameContainer<?> target,
             final boolean isSilent, final String type, final Object ... args) {
         sendLine(target, isSilent, type, args);
     }
 
     /**
      * Sends a usage line, if appropriate, to the specified target.
      *
      * @param target The command window to send the line to
      * @param isSilent Whether this command is being silenced or not
      * @param name The name of the command that's raising the error
      * @param args The arguments that the command accepts or expects
      */
     public void proxyShowUsage(final FrameContainer<?> target,
             final boolean isSilent, final String name, final String args) {
         showUsage(target, isSilent, getName(), name + " " + args);
     }
 
     /**
      * Formats the specified data into a table suitable for output in the
      * textpane. It is expected that each String[] in data has the same number
      * of elements as the headers array.
      *
      * @param headers The headers of the table.
      * @param data The contents of the table.
      * @return A string containing an ASCII table
      */
     public String proxyDoTable(final String[] headers, final String[][] data) {
         return doTable(headers, data);
     }
 
     /** {@inheritDoc} */
     @Override
     public String getName() {
         return "debug";
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean showInHelp() {
         return false;
     }
 
     /** {@inheritDoc} */
     @Override
     public CommandType getType() {
         return CommandType.TYPE_GLOBAL;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getHelp() {
         return null;
     }
 
     /** {@inheritDoc} */
     @Override
     public AdditionalTabTargets getSuggestions(final int arg,
             final IntelligentCommandContext context) {
         AdditionalTabTargets res = new AdditionalTabTargets();
 
         res.excludeAll();
 
         if (arg == 0) {
             res.addAll(plugin.getCommandNames());
         } else {
             final DebugCommand command = plugin.getCommand(
                     context.getPreviousArgs().get(0));
             if (command instanceof IntelligentCommand) {
                 final IntelligentCommandContext newContext =
                         new IntelligentCommandContext(context.getWindow(),
                         context.getPreviousArgs().subList(1,
                         context.getPreviousArgs().size()),
                         context.getPartial());
                 res = ((IntelligentCommand) command).getSuggestions(
                         arg, newContext);
             }
         }
 
         return res;
     }
 
 }
