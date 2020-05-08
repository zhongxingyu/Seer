 /*
  * Copyright (c) Henrik Niehaus
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
 package org.hampelratte.svdrp.responses.highlevel;
 
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Represents a recording of VDR.
  * 
  * @author <a href="mailto:hampelratte@users.sf.net">hampelratte@users.sf.net</a>
  * 
  */
 public class Recording extends EPGEntry implements Comparable<Recording>, TreeNode {
     private static final long serialVersionUID = 2L;
 
     private int number;
 
     private boolean isNew = false;
 
     private String display;
     private String folder;
 
     private int priority = 0;
 
     private int lifetime = 0;
 
     private int duration = 0;
 
     public Recording() {
     }
 
     public Recording(EPGEntry entry) {
         copyFrom(entry);
     }
 
     public void copyFrom(EPGEntry entry) {
         super.setChannelID(entry.getChannelID());
         super.setChannelName(entry.getChannelName());
         super.setDescription(entry.getDescription());
         super.setEndTime(entry.getEndTime());
         super.setEventID(entry.getEventID());
         super.setShortText(entry.getShortText());
         super.setStartTime(entry.getStartTime());
         super.setStreams(entry.getStreams());
         super.setTableID(entry.getTableID());
         display = entry.getTitle();
         super.setVersion(entry.getVersion());
         super.setVpsTime(entry.getVpsTime());
     }
 
     public int getNumber() {
         return number;
     }
 
     public void setNumber(int number) {
         this.number = number;
     }
 
     public void setStartTime(Date date) {
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(date.getTime());
         setStartTime(cal);
     }
 
     @Override
     public String getDisplayTitle() {
         if(display == null) {
             display = getTitle();
 
             if (display.contains("~")) {
                display = display.substring(display.lastIndexOf('~') + 1);
             }
 
            while(display.charAt(0) == ('%')) {
                 display = display.substring(1);
             }
         }
         return display;
     }
 
     public String getFolder() {
         if (folder == null) {
             if (getTitle().contains("~")) {
                 folder = getTitle().substring(0, getTitle().lastIndexOf('~'));
                 while (folder.charAt(0) == ('%')) {
                     folder = folder.substring(1);
                 }
 
                 folder = folder.replaceAll("~%", "/");
                 folder = folder.replaceAll("~", "/");
             }
         }
         return folder;
     }
 
     public void setDisplayTitle(String display) {
         this.display = display;
     }
 
     @Override
     public void setTitle(String title) {
         super.setTitle(title);
         this.display = null; // reset the display name, so that it gets recalculated
         this.folder = null; // reset the calculated folder, so that it gets recalculated
     }
 
     public boolean isNew() {
         return isNew;
     }
 
     public void setNew(boolean isNew) {
         this.isNew = isNew;
     }
 
     public boolean isCut() {
         return getTitle().startsWith("%") || getTitle().contains("~%");
     }
 
     public int getPriority() {
         return priority;
     }
 
     public void setPriority(int priority) {
         this.priority = priority;
     }
 
     public int getLifetime() {
         return lifetime;
     }
 
     public void setLifetime(int lifetime) {
         this.lifetime = lifetime;
     }
 
     public int getDuration() {
         return duration;
     }
 
     public void setDuration(int duration) {
         this.duration = duration;
     }
 
     @Override
     public String toString() {
         return getNumber() + " "+
                 DateFormat.getDateTimeInstance().format(getStartTime().getTime()) +
                 (isNew() ? "*" : "") + " " +
                 super.toString();
     }
 
     @Override
     public int compareTo(Recording other) {
         return this.getTitle().compareTo(other.getTitle());
     }
 }
