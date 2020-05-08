 /**
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.waveprotocol.wave.client.wavepanel.impl.edit;
 
 import com.google.gwt.user.client.Window;
 
 import org.waveprotocol.wave.client.common.util.WaveRefConstants;
 import org.waveprotocol.wave.client.wavepanel.impl.focus.FocusFramePresenter;
 import org.waveprotocol.wave.client.wavepanel.view.BlipView;
 import org.waveprotocol.wave.client.wavepanel.view.ThreadView;
 import org.waveprotocol.wave.client.wavepanel.view.dom.ModelAsViewProvider;
 import org.waveprotocol.wave.client.wavepanel.view.dom.full.BlipQueueRenderer;
 import org.waveprotocol.wave.model.conversation.ConversationBlip;
 import org.waveprotocol.wave.model.conversation.ConversationThread;
 import org.waveprotocol.wave.model.id.InvalidIdException;
import org.waveprotocol.wave.model.id.ModernIdSerialiser;
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.id.WaveletId;
 import org.waveprotocol.wave.model.waveref.WaveRef;
 import org.waveprotocol.wave.util.escapers.GwtWaverefEncoder;
 
 /**
  * Defines the UI actions that can be performed as part of the editing feature.
  * This includes editing, replying, and deleting blips in a conversation.
  *
  */
 public final class Actions {
   enum Action {
     EDIT_BLIP,
     REPLY_TO_BLIP,
     CONTINUE_THREAD,
     DELETE_BLIP,
     DELETE_THREAD,
   }
 
   private final ModelAsViewProvider views;
   private final BlipQueueRenderer blipQueue;
   private final FocusFramePresenter focus;
   private final EditSession edit;
 
   /**
    * Implements the wave panel's editing UI actions.
    *
    * @param views view provider
    * @param blipQueue blip renderer
    * @param focus focus-frame feature
    * @param edit blip-content editing feature
    */
   Actions(ModelAsViewProvider views, BlipQueueRenderer blipQueue, FocusFramePresenter focus,
       EditSession edit) {
     this.views = views;
     this.blipQueue = blipQueue;
     this.focus = focus;
     this.edit = edit;
   }
 
   /**
    * Starts editing a blip.
    */
   public void startEditing(BlipView blipUi) {
     focusAndEdit(blipUi);
   }
 
   /**
    * Stops editing a blip.
    */
   public void stopEditing() {
     edit.stopEditing();
   }
 
   /**
    * Replies to a blip.
    */
   public void reply(BlipView blipUi) {
     ConversationBlip blip = views.getBlip(blipUi);
     ConversationBlip reply =
         blip.appendInlineReplyThread(blip.getContent().size() - 1).appendBlip();
     blipQueue.flush();
     focusAndEdit(views.getBlipView(reply));
   }
 
   /**
    * Adds a continuation to a thread.
    */
   public void addContinuation(ThreadView threadUi) {
     ConversationThread thread = views.getThread(threadUi);
     ConversationBlip continuation = thread.appendBlip();
     blipQueue.flush();
     focusAndEdit(views.getBlipView(continuation));
   }
 
   /**
    * Deletes a blip.
    */
   public void delete(BlipView blipUi) {
     // If focus is on the blip that is being deleted, move focus somewhere else.
     // If focus is on a blip inside the blip being deleted, don't worry about it
     // (checking could get too expensive).
     if (blipUi.equals(focus.getFocusedBlip())) {
       // Move to next blip in thread if there is one, otherwise previous blip in
       // thread, otherwise previous blip in traversal order.
       ThreadView parentUi = blipUi.getParent();
       BlipView nextUi = parentUi.getBlipAfter(blipUi);
       if (nextUi == null) {
         nextUi = parentUi.getBlipBefore(blipUi);
       }
       if (nextUi != null) {
         focus.focus(nextUi);
       } else {
         focus.moveUp();
       }
     }
 
     views.getBlip(blipUi).deleteRecursive();
   }
 
   /**
    * Deletes a thread.
    */
   public void delete(ThreadView threadUi) {
     views.getThread(threadUi).delete();
   }
 
   /**
    * Moves focus to a blip, and starts editing it.
    */
   private void focusAndEdit(BlipView blipUi) {
     edit.stopEditing();
     focus.focus(blipUi);
     edit.startEditing(blipUi);
   }
 
   /**
    * Focus on a blip.
    */
   public void focus(BlipView blipUi) {
     focus.focus(blipUi);
   }
 
   public void popupLink(BlipView blipUi) {
     ConversationBlip blip = views.getBlip(blipUi);
     // TODO(Yuri Z.) Change to use the conversation model when the Conversation
     // exposes a reference to its ConversationView.
     WaveId waveId = blip.hackGetRaw().getWavelet().getWaveId();
     WaveletId waveletId;
     try {
      waveletId = ModernIdSerialiser.INSTANCE.deserialiseWaveletId(blip.getConversation().getId());
     } catch (InvalidIdException e) {
       Window.alert("Unable to link to this blip, invalid conversation id "
           + blip.getConversation().getId());
       return;
     }
     WaveRef waveRef = WaveRef.of(waveId, waveletId, blip.getId());
     String waveRefStringValue =
         WaveRefConstants.WAVE_URI_PREFIX + GwtWaverefEncoder.encodeToUriPathSegment(waveRef);
     // TODO(Yuri Z.) replace with custom popup.
     Window.prompt("Link to this blip:", waveRefStringValue);
   }
 
   //
   // All the same actions as above, but bound to the context of the focus frame.
   //
 
   void startEditing() {
     BlipView blipUi = getBlipContext();
     if (blipUi != null) {
       startEditing(blipUi);
     }
   }
 
   void reply() {
     BlipView blipUi = getBlipContext();
     if (blipUi != null) {
       reply(blipUi);
     }
   }
 
   void addContinuation() {
     ThreadView threadUi = getThreadContext();
     if (threadUi != null) {
       addContinuation(threadUi);
     }
   }
 
   void deleteBlip() {
     BlipView blipUi = getBlipContext();
     if (blipUi != null) {
       delete(blipUi);
     }
   }
 
   void deleteThread() {
     ThreadView threadUi = getThreadContext();
     if (threadUi != null) {
       delete(threadUi);
     }
   }
 
   BlipView getBlipContext() {
     return focus.getFocusedBlip();
   }
 
   ThreadView getThreadContext() {
     return parentOf(getBlipContext());
   }
 
   private static ThreadView parentOf(BlipView blip) {
     return blip != null ? blip.getParent() : null;
   }
 
 }
