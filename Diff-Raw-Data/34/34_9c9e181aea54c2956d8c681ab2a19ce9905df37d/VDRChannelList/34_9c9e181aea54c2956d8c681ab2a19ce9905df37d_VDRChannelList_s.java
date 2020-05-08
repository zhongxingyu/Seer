/* $Id: VDRChannelList.java,v 1.1 2006-12-29 23:34:13 hampelratte Exp $
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its 
  *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package lazybones;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import de.hampelratte.svdrp.Response;
 import de.hampelratte.svdrp.commands.LSTC;
 import de.hampelratte.svdrp.responses.highlevel.Channel;
 import de.hampelratte.svdrp.util.ChannelParser;
 
 // TODO berall benutzen
 public class VDRChannelList {
 
     private static VDRChannelList instance;
 
     private ArrayList<Channel> channels = null;
 
     private VDRChannelList() {
         update();
     }
 
     private void update() {
         Response res = VDRConnection.send(new LSTC());
         if (res != null && res.getCode() == 250) {
             channels = ChannelParser.parse(res.getMessage());
         }
     }
 
     public static VDRChannelList getInstance() {
         if (instance == null) {
             instance = new VDRChannelList();
         }
         return instance;
     }
     
     public ArrayList<Channel> getChannels() {
         return channels;
     }
     
     public Channel getChannelByNumber(int channelNumber) {
         for (Iterator<Channel> iter = channels.iterator(); iter.hasNext();) {
             Channel chan = iter.next();
             if(chan.getChannelNumber() == channelNumber) {
                 return chan;
             }
         }
         
         return null;
     }
 }
