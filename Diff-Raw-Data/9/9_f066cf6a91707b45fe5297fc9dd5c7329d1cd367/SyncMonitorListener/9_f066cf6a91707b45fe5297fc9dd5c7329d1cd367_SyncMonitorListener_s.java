 /*
  * Funambol is a mobile platform developed by Funambol, Inc. 
  * Copyright (C) 2010 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission 
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE 
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License 
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite 
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  * 
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  * 
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably 
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 
 package com.funambol.client.test.util;
 
 import com.funambol.syncml.spds.SyncStatus;
 import com.funambol.sync.SyncListener;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.BasicSyncListener;
 
 import com.funambol.util.Log;
 
 public class SyncMonitorListener extends BasicSyncListener {
 
     private static final String TAG_LOG = "SyncMonitorListener";
 
     public static final String SENDING_PHASE_NAME   = "Sending";
     public static final String RECEIVING_PHASE_NAME = "Receiving";
 
     protected int receivingPhaseCounter = 0;
     protected int sendingPhaseCounter   = 0;
 
     protected long currentItemSize = 0;
     protected int currentItemProgress = 0;
 
     protected SyncListener lis;
 
     protected String interruptOnPhase = null;
     protected int    interruptOnPhaseNumber = -1;
     protected int    interruptOnPhaseProgress = -1;
     protected String interruptReason = null;
 
     public SyncMonitorListener(SyncListener lis) {
         this.lis = lis;
     }
 
     /**
      * Tells the SyncMonitorListener to interrupt the synchronization after the
      * given phase
      * @param phaseName SENDING_PHASE_NAME or RECEIVING_PHASE_NAME
      * @param num the number of items (e.g. 2)
      * @param progress the perecentage of the item (e.g. 90)
      * @param reason
      */
     public void interruptAfterPhase(String phaseName, int num, int progress,
             String reason) {
         if (Log.isLoggable(Log.TRACE)) {
            Log.trace(TAG_LOG, "interrupt after phase: " + phaseName + "," + num);
         }
         interruptOnPhase = phaseName;
         interruptOnPhaseNumber = num;
         interruptOnPhaseProgress = progress;
         interruptReason = reason;
     }
 
     public void startSession() {
         lis.startSession();
         receivingPhaseCounter = 0;
         sendingPhaseCounter = 0;
        interruptOnPhaseNumber = -1;
        interruptOnPhaseProgress = -1;
     }
 
     public void itemDeleted(SyncItem item) {
         lis.itemDeleted(item);
     }
 
     public void itemAddSendingStarted(String key, String parent, long size) {
         lis.itemAddSendingStarted(key, parent, size);
         currentItemSize = size;
     }
 
     public void itemAddSendingProgress(String key, String parent, long size) {
         lis.itemAddSendingProgress(key, parent, size);
         if (SENDING_PHASE_NAME.equals(interruptOnPhase)) {
             if(interruptOnPhaseProgress > 0) {
                 if (sendingPhaseCounter == (interruptOnPhaseNumber - 1)) {
                    currentItemProgress = (int)(currentItemSize / size) * 100;
                     if(currentItemProgress >= interruptOnPhaseProgress) {
                         interruptSync();
                     }
                 }
             }
         }
     }
 
     public void itemAddSendingEnded(String key, String parent) {
         ++sendingPhaseCounter;
         if (SENDING_PHASE_NAME.equals(interruptOnPhase) &&
                 sendingPhaseCounter == interruptOnPhaseNumber) {
             interruptSync();
         }
         lis.itemAddSendingEnded(key, parent);
     }
 
     public void itemAddReceivingStarted(String key, String parent, long size) {
         lis.itemAddReceivingStarted(key, parent, size);
         currentItemSize = size;
     }
 
     public void itemAddReceivingProgress(String key, String parent, long size) {
         lis.itemAddReceivingProgress(key, parent, size);
         if (RECEIVING_PHASE_NAME.equals(interruptOnPhase)) {
             if(interruptOnPhaseProgress > 0) {
                 if (sendingPhaseCounter == (interruptOnPhaseNumber - 1)) {
                     currentItemProgress = (int)(currentItemSize / size) * 100;
                     if(currentItemProgress >= interruptOnPhaseProgress) {
                         interruptSync();
                     }
                 }
             }
         }
     }
 
     public void itemAddReceivingEnded(String key, String parent) {
         ++receivingPhaseCounter;
         if (RECEIVING_PHASE_NAME.equals(interruptOnPhase) &&
                 receivingPhaseCounter == interruptOnPhaseNumber) {
             interruptSync();
         }
         lis.itemAddReceivingEnded(key, parent);
     }
 
     public void itemReplaceSendingEnded(String key, String parent) {
         lis.itemReplaceSendingEnded(key, parent);
     }
 
     public void itemDeleteSent(SyncItem item) {
         lis.itemDeleteSent(item);
     }
 
     public void endSession(SyncStatus report) {
         lis.endSession(report);
     }
 
     public void startConnecting() {
         lis.startConnecting();
     }
 
     public void endConnecting(int action) {
         lis.endConnecting(action);
     }
 
     public void syncStarted(int alertCode) {
         lis.syncStarted(alertCode);
     }
 
     public void endSyncing() {
         lis.endSyncing();
     }
 
     public void startReceiving(int number) {
         lis.startReceiving(number);
     }
 
     public void endReceiving() {
         lis.endReceiving();
     }
 
     public void startSending(int numNewItems, int numUpdItems, int numDelItems) {
         lis.startSending(numNewItems, numUpdItems, numDelItems);
     }
 
     public void itemReplaceSendingStarted(String key, String parent, long size) {
         lis.itemReplaceSendingStarted(key, parent, size);
     }
 
     public void itemReplaceSendingProgress(String key, String parent, long size) {
         lis.itemReplaceSendingProgress(key, parent, size);
     }
 
     public void endSending() {
         lis.endSending();
     }
 
     public void startMapping() {
         lis.startFinalizing();
     }
 
     public void endMapping() {
         lis.endFinalizing();
     }
 
     public boolean startSyncing(int alertCode, Object devInf) {
         return lis.startSyncing(alertCode, devInf);
     }
 
     protected void interruptSync() {
         interruptOnPhase = null;
         receivingPhaseCounter = 0;
         sendingPhaseCounter = 0;
         interruptOnPhaseNumber = -1;
         interruptOnPhaseProgress = -1;
         interruptReason = null;
         throw new IllegalArgumentException("Simulating sync error " + interruptReason);
     }
 }
 
