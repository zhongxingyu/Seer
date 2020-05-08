 /* $Id$
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
 package org.hampelratte.svdrp.commands;
 
 import org.hampelratte.svdrp.Command;
 
 /**
  * Command to list all EPG data of all channels, or the data of one channel, or
  * the data of one channel at a time
  * 
  * @author <a href="mailto:hampelratte@users.sf.net">hampelratte@users.sf.net</a>
  * 
  */
 public class LSTE extends Command {
     private static final long serialVersionUID = 1L;
     
     private String channel = "";
 
     private String time = "";
 
     /**
      * Command to list all EPG data of all channels
      */
     public LSTE() {
     }
 
     /**
      * Command to list all data of one channel
      * 
      * @param channel
      *            The number or the ID of the channel
      */
     public LSTE(String channel) {
         this.channel = channel;
     }
     
     /**
      * Command to list all data of one channel
      * 
     * @param channel
      *            The number of the channel
      */
     public LSTE(int number) {
         this.channel = Integer.toString(number);
     }
 
     /**
      * Command to list the data of one channel at a given time
      * 
      * @param channel
      *            The number of the channel
      * @param time
      *            "now", "next" or "at &lt;time&gt;" , where &lt;time&gt; is in
      *            time_t format, which is equal to the unix time stamp without
      *            the milliseconds -> unix time stamp / 1000, e.g. 1115484780
      */
     public LSTE(int channel, String time) {
         this.channel = Integer.toString(channel);
         this.time = time;
     }
     
     /**
      * Command to list the data of one channel at a given time
      * 
      * @param channel
      *            The number or the ID of the channel
      * @param time
      *            "now", "next" or "at &lt;time&gt;" , where &lt;time&gt; is in
      *            time_t format, which is equal to the unix time stamp without
      *            the milliseconds -> unix time stamp / 1000, e.g. 1115484780
      */
     public LSTE(String channel, String time) {
         this.channel = channel;
         this.time = time;
     }
     
     /**
      * Command to list the data of one channel at a given time
      * 
      * @param number
      *            The number of the channel
      * @param time_t
      *            The time in time_t format, which is equal to the unix time stamp
      *            without the milliseconds -> unix time stamp / 1000, e.g.
      *            1115484780
      */
     public LSTE(int number, long time_t) {
         this(Integer.toString(number), "at " + time_t);
     }
 
     @Override
     public String getCommand() {
         String cmd = "LSTE " + channel + " " + time;
         return cmd.trim();
     }
 
     @Override
     public String toString() {
         return "LSTE";
     }
 
     /**
      * Returns the channel
      * 
      * @return The channel
      */
     public String getChannel() {
         return channel;
     }
 
     /**
      * Sets the channel
      * 
      * @param channel
      *            The ID or number of the channel
      */
     public void setChannel(String channel) {
         this.channel = channel;
     }
 
     /**
      * Returns the time
      * 
      * @return The time
      * @see #setTime(String time)
      */
     public String getTime() {
         return time;
     }
 
     /**
      * Sets the time
      * 
      * @param time
      *            "now", "next" or "at &lt;time&gt;" , where &lt;time&gt; is in
      *            time_t format, which is equal to the unix time stamp without
      *            the milliseconds -> unix time stamp / 1000, e.g. 1115484780
      */
     public void setTime(String time) {
         this.time = time;
     }
 }
