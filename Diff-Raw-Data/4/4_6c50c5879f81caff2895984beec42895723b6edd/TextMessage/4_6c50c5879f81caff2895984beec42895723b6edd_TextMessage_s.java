 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.model;
 
 import net.rptools.maptool.client.MapTool;
 
 
 public class TextMessage {
 
     // Not an enum so that it can be hessian serialized
     public interface Channel {
         public static final int ALL = 0;        // General message channel
         public static final int SAY = 1;        // Player/character speach
         public static final int GM = 2;         // GM visible only
         public static final int ME = 3;         // Targeted to the current maptool client
         public static final int GROUP = 4;      // All in the group
         public static final int WHISPER = 5;    // To a specific player/character
     }
 
     private int channel;
     private String target;
     private String message;
     private String source;
     
     ////
     // CONSTRUCTION
     public TextMessage(int channel, String target, String source, String message) {
         this.channel = channel;
         this.target = target;
         this.message = message;
         this.source = source;
     }
     
     public static TextMessage say(String message) {
         return new TextMessage(Channel.SAY, null, MapTool.getPlayer().getName(), message);
     }
     
     public static TextMessage gm(String message) {
         return new TextMessage(Channel.GM, null, MapTool.getPlayer().getName(), message);
     }
     
     public static TextMessage me(String message) {
         return new TextMessage(Channel.ME, null, MapTool.getPlayer().getName(), message);
     }
     
     public static TextMessage group(String target, String message) {
         return new TextMessage(Channel.GROUP, target, MapTool.getPlayer().getName(), message);
     }
     
     public static TextMessage whisper(String target, String message) {
         return new TextMessage(Channel.WHISPER, target, MapTool.getPlayer().getName(), message);
     }
     
     ////
     // PROPERTIES
     public int getChannel() {
         return channel;
     }
     
     public String getTarget() {
         return target;
     }
     
     public String getMessage() {
         return message;
     }
 
     public String getSource() {
     	return source;
     }
     
     ////
     // CONVENIENCE
     public boolean isGM () {
         return channel == Channel.GM;
     }
     
     public boolean isMessage () {
         return channel == Channel.ALL;
     }
     
     public boolean isSay() {
         return channel == Channel.SAY;
     }
     
     public boolean isMe() {
         return channel == Channel.ME;
     }
     
     public boolean isGroup() {
         return channel == Channel.GROUP;
     }
     
     public boolean isWhisper() {
         return channel == Channel.WHISPER;
     }
     
 }
