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
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.Query;
 import com.dmdirc.events.ChannelUserAwayEvent;
 import com.dmdirc.events.ChannelUserBackEvent;
 import com.dmdirc.events.FrameClosingEvent;
 import com.dmdirc.interfaces.GroupChat;
 import com.dmdirc.interfaces.NicklistListener;
 import com.dmdirc.parser.interfaces.ChannelClientInfo;
 
 import java.util.Collection;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Listens for contact list related events.
  */
 public class ContactListListener implements NicklistListener {
 
     /** The group chat this listener is for. */
     private final GroupChat groupChat;
     /** Event bus to register listeners with. */
     private final DMDircMBassador eventBus;
 
     /**
      * Creates a new ContactListListener for the specified group chat.
      *
      * @param groupChat The group chat to show a contact list for
      */
     public ContactListListener(final GroupChat groupChat) {
         this.groupChat = groupChat;
         this.eventBus = groupChat.getEventBus();
     }
 
     /**
      * Adds all necessary listeners for this contact list listener to function.
      */
     public void addListeners() {
         groupChat.addNicklistListener(this);
         eventBus.subscribe(this);
     }
 
     /**
      * Removes the listeners added by {@link #addListeners()}.
      */
     public void removeListeners() {
         groupChat.removeNicklistListener(this);
         eventBus.unsubscribe(this);
     }
 
     @Override
     public void clientListUpdated(final Collection<ChannelClientInfo> clients) {
         for (ChannelClientInfo client : clients) {
             clientAdded(client);
         }
     }
 
     @Override
     public void clientListUpdated() {
         // Do nothing
     }
 
     @Override
     public void clientAdded(final ChannelClientInfo client) {
        final Query query = groupChat.getOptionalConnection().get()
                .getQuery(client.getClient().getNickname(), false);
 
         query.setIcon("query-" + client.getClient().getAwayState().name().toLowerCase());
     }
 
     @Override
     public void clientRemoved(final ChannelClientInfo client) {
         // Do nothing
     }
 
     @Handler
     public void handleUserAway(final ChannelUserAwayEvent event) {
         clientAdded(event.getUser());
     }
 
     @Handler
     public void handleUserBack(final ChannelUserBackEvent event) {
         clientAdded(event.getUser());
     }
 
     @Handler
     public void windowClosing(final FrameClosingEvent event) {
         removeListeners();
     }
 
 }
