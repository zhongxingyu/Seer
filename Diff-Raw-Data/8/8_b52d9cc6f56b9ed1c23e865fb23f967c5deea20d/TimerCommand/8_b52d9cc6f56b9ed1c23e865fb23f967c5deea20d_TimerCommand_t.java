 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.time;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.commandparser.CommandArguments;
 import com.dmdirc.commandparser.CommandInfo;
 import com.dmdirc.commandparser.CommandType;
 import com.dmdirc.commandparser.commands.Command;
 import com.dmdirc.commandparser.commands.IntelligentCommand;
 import com.dmdirc.commandparser.commands.context.CommandContext;
 import com.dmdirc.ui.input.AdditionalTabTargets;
 import com.dmdirc.ui.input.TabCompleter;
 
 /**
  * The timer command allows users to schedule commands to occur after a certain
  * interval, or to repeatedly occur with a specified delay.
  * @author chris
  */
 public final class TimerCommand extends Command implements IntelligentCommand,
         CommandInfo {
     
     /**
      * Creates a new instance of TimerCommand.
      */
     public TimerCommand() {
         super();
     }
     
     /** {@inheritDoc} */
     @Override
     public void execute(final FrameContainer<?> origin,
             final CommandArguments args, final CommandContext context) {
         if (args.getArguments().length < 3) {
             doUsage(origin, args.isSilent());
         } else {
             int repetitions = 0;
             int interval = 0;
             final String command = args.getArgumentsAsString(2);
             
             try {
                 repetitions = Integer.parseInt(args.getArguments()[0]);
                 interval = Integer.parseInt(args.getArguments()[1]);
             } catch (NumberFormatException ex) {
                 doUsage(origin, args.isSilent());
                 return;
             }

            if (interval < 1) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR, "Cannot use" +
                        " intervals below 1");
                return;
            }

             new TimedCommand(repetitions, interval, command, origin, context.getSource());
             sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Command scheduled.");
         }
     }
     
     /**
      * Displays usage information for this command.
      * @param origin The window that the command was entered in
      * @param isSilent Whether this command is being silenced or not
      */
     private void doUsage(final FrameContainer<?> origin, final boolean isSilent) {
         showUsage(origin, isSilent, "timer", "<repetitions> <interval> <command>");
     }
     
     /** {@inheritDoc} */
     @Override
     public String getName() {
         return "timer";
     }
     
     /** {@inheritDoc} */
     @Override
     public boolean showInHelp() {
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public CommandType getType() {
         return CommandType.TYPE_GLOBAL;
     }
     
     /** {@inheritDoc} */
     @Override
     public String getHelp() {
         return "timer <repetitions> <interval> <command> " +
                 "- schedules a command to be executed after a certain time";
     }
 
     /** {@inheritDoc} */
     @Override
     public AdditionalTabTargets getSuggestions(final int arg,
             final IntelligentCommandContext context) {
         AdditionalTabTargets res;
         
         if (arg <= 1) {
             res = new AdditionalTabTargets().excludeAll();
         } else {
             res = TabCompleter.getIntelligentResults(arg, context, 2);
         }
         
         return res;
     } 
     
 }
