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
 
 package com.dmdirc.addons.contactlist;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.commandparser.BaseCommandInfo;
 import com.dmdirc.commandparser.CommandArguments;
 import com.dmdirc.commandparser.CommandInfo;
 import com.dmdirc.commandparser.CommandType;
 import com.dmdirc.commandparser.commands.Command;
 import com.dmdirc.commandparser.commands.IntelligentCommand;
 import com.dmdirc.commandparser.commands.context.ChannelCommandContext;
 import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.events.NickListClientsChangedEvent;
 import com.dmdirc.interfaces.CommandController;
 import com.dmdirc.ui.input.AdditionalTabTargets;
 
 import javax.annotation.Nonnull;
 import javax.inject.Inject;
 
 /**
  * Generates a contact list for the channel the command is used in.
  */
 public class ContactListCommand extends Command implements IntelligentCommand {
 
     /** A command info object for this command. */
     public static final CommandInfo INFO = new BaseCommandInfo("contactlist",
             "contactlist - show a contact list for the current channel",
             CommandType.TYPE_CHANNEL);
 
     /**
      * Creates a new instance of this command.
      *
      * @param controller The controller to use for command information.
      */
     @Inject
     public ContactListCommand(final CommandController controller) {
         super(controller);
     }
 
     @Override
     public void execute(@Nonnull final FrameContainer origin,
             final CommandArguments args, final CommandContext context) {
         final ChannelCommandContext chanContext = (ChannelCommandContext) context;
 
         final ContactListListener listener = new ContactListListener(chanContext.getChannel());
         listener.addListeners();
        listener.handleClientsUpdated(new NickListClientsChangedEvent(chanContext.getChannel(),
                chanContext.getChannel().getUsers()));
     }
 
     @Override
     public AdditionalTabTargets getSuggestions(final int arg,
             final IntelligentCommandContext context) {
         return new AdditionalTabTargets().excludeAll();
     }
 
 }
