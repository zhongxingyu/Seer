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
 
 package com.dmdirc.addons.dcc;
 
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.addons.dcc.actions.DCCActions;
 import com.dmdirc.addons.dcc.io.DCCChat;
 import com.dmdirc.ui.WindowManager;
 import com.dmdirc.ui.interfaces.InputWindow;
 
 /**
  * This class links DCC Chat objects to a window.
  */
 public class ChatContainer extends DCCFrameContainer<InputWindow> implements DCCChatHandler {
 
     /** The DCCChat object we are a window for. */
     private final DCCChat dccChat;
     /** My Nickname. */
     private final String nickname;
     /** Other Nickname. */
     private final String otherNickname;
 
     /**
      * Creates a new instance of DCCChatWindow with a given DCCChat object.
      *
      * @param plugin the DCC Plugin responsible for this window
      * @param dcc The DCCChat object this window wraps around
      * @param title The title of this window
      * @param nick My Current Nickname
      * @param targetNick Nickname of target
      */
     public ChatContainer(final DCCPlugin plugin, final DCCChat dcc,
             final String title, final String nick, final String targetNick) {
         super(title, "dcc-chat-inactive", InputWindow.class,
                 DCCCommandParser.getDCCCommandParser());
         this.dccChat = dcc;
         dcc.setHandler(this);
         nickname = nick;
         otherNickname = targetNick;
 
         WindowManager.addWindow(plugin.getContainer(), this);
     }
 
     /**
      * Get the DCCChat Object associated with this window.
      *
      * @return The DCCChat Object associated with this window
      */
     public DCCChat getDCC() {
         return dccChat;
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendLine(final String line) {
         if (dccChat.isWriteable()) {
             final StringBuffer buff = new StringBuffer("DCCChatSelfMessage");
             ActionManager.processEvent(DCCActions.DCC_CHAT_SELFMESSAGE, buff,
                     this, line);
            addLine(buff, nickname, getTranscoder().encode(line));
             dccChat.sendLine(line);
         } else {
             final StringBuffer buff = new StringBuffer("DCCChatError");
            addLine(buff, "Socket is closed.", getTranscoder().encode(line));
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void handleChatMessage(final DCCChat dcc, final String message) {
         final StringBuffer buff = new StringBuffer("DCCChatMessage");
         ActionManager.processEvent(DCCActions.DCC_CHAT_MESSAGE, buff, this,
                 otherNickname, message);
        addLine(buff, otherNickname, getTranscoder().encode(message));
     }
 
     /** {@inheritDoc} */
     @Override
     public void socketClosed(final DCCChat dcc) {
         final StringBuffer buff = new StringBuffer("DCCChatInfo");
         ActionManager.processEvent(DCCActions.DCC_CHAT_SOCKETCLOSED, buff, this);
         addLine(buff, "Socket closed");
         if (!isWindowClosing()) {
             setIcon("dcc-chat-inactive");
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void socketOpened(final DCCChat dcc) {
         final StringBuffer buff = new StringBuffer("DCCChatInfo");
         ActionManager.processEvent(DCCActions.DCC_CHAT_SOCKETOPENED, buff, this);
         addLine(buff, "Socket opened");
         setIcon("dcc-chat-active");
     }
 
     /** {@inheritDoc} */
     @Override
     public void windowClosing() {
         super.windowClosing();
         dccChat.close();
     }
 
 }
