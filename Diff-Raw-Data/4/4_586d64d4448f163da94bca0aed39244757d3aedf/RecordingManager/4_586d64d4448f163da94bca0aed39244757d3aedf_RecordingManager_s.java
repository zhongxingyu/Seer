/* $Id: RecordingManager.java,v 1.12 2010-09-29 17:44:45 hampelratte Exp $
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
 
 import java.awt.Cursor;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 import lazybones.actions.ListRecordingsAction;
 import lazybones.actions.VDRAction;
 
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.commands.LSTR;
 import org.hampelratte.svdrp.commands.PLAY;
 import org.hampelratte.svdrp.responses.highlevel.EPGEntry;
 import org.hampelratte.svdrp.responses.highlevel.Recording;
 import org.hampelratte.svdrp.util.EPGParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to manage all recordings.
  * 
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net</a>
  */
 public class RecordingManager extends Observable {
 
     private static transient Logger logger = LoggerFactory.getLogger(RecordingManager.class);
     
     private static RecordingManager instance;
 
     /**
      * Stores all recordings as Recording objects
      */
     private List<Recording> recordings;
     
     private RecordingManager() {
         recordings = new ArrayList<Recording>();
     }
 
     public synchronized static RecordingManager getInstance() {
         if (instance == null) {
             instance = new RecordingManager();
         }
         return instance;
     }
 
     public void removeAll() {
         recordings.clear();
         setChanged();
         notifyObservers(recordings);
     }
 
     /**
      * @return an List of Recording objects
      */
     public List<Recording> getRecordings() {
         return recordings;
     }
     
     /**
      * Fetches the recording list from vdr
      */
     public synchronized void synchronize() {
         synchronize(null);
     }
     
     /**
      * Fetches the recording list from vdr
      * @param callback will be called after the synchronization has finished
      */
     public synchronized void synchronize(final Runnable callback) {
         logger.debug("Getting recordings from VDR");
         
         // fetch current recording list from vdr
         VDRCallback _callback = new VDRCallback() {
             public void receiveResponse(VDRAction cmd, Response response) {
                 ListRecordingsAction lstr = (ListRecordingsAction) cmd;
 
                 if(lstr.isSuccess()) {
                     // clear recording list
                    removeAll();
                     recordings = lstr.getRecordings();
                     boolean loadInfos = Boolean.TRUE.toString().equals(LazyBones.getProperties().getProperty("loadRecordInfos"));
                     if(loadInfos) {
                         for (Recording rec : recordings) {
                             logger.trace("Getting info for recording {}", rec.getNumber());
                             Response resp = VDRConnection.send(new LSTR(rec.getNumber()));
                             if(resp != null && resp.getCode() == 215) {
                                 // workaround for the epg parser, because LSTR does not send an 'e' as entry terminator
                                 String[] lines = resp.getMessage().split("\n");
                                 StringBuffer mesg = new StringBuffer();
                                 for (int i = 0; i < lines.length; i++) {
                                     if(i == lines.length -1) {
                                         mesg.append("e\n");
                                     }
                                     mesg.append(lines[i]+"\n");
                                 }
                                 
                                 // parse epg information
                                 List<EPGEntry> epg = EPGParser.parse(mesg.toString());
                                 if(epg.size() > 0) {
                                     rec.setEpgInfo(epg.get(0));
                                 }
                             }
                         }
                     }
                 }
                 
                 setChanged();
                 notifyObservers(recordings);
                 
                 if(callback != null) {
                     callback.run();
                 }
             }
         };
         ListRecordingsAction lstr = new ListRecordingsAction(_callback);
         lstr.enqueue();
     }
     
     public void loadInfo(Recording rec) {
         LazyBones.getInstance().getMainDialog().setCursor(new Cursor(Cursor.WAIT_CURSOR));
         Response response = VDRConnection.send(new LSTR(rec.getNumber()));
         if(response != null && response.getCode() == 215) {
             // workaround for the epg parser, because LSTR does not send an 'e' as entry terminator
             String[] lines = response.getMessage().split("\n");
             StringBuffer mesg = new StringBuffer();
             for (int i = 0; i < lines.length; i++) {
                 if(i == lines.length -1) {
                     mesg.append("e\n");
                 }
                 mesg.append(lines[i]+"\n");
             }
             
             // parse epg information
             List<EPGEntry> epg = EPGParser.parse(mesg.toString());
             if(epg.size() > 0) {
                 rec.setEpgInfo(epg.get(0));
             }
         }
         LazyBones.getInstance().getMainDialog().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
     }
 
     public void playOnVdr(Recording rec) {
         Response res = VDRConnection.send(new PLAY(rec.getNumber()));
         if(res.getCode() != 250) {
             logger.error(res.getMessage());
         }
     }
 }
