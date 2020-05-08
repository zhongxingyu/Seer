 /**
  * Copyright 2011 The PlayN Authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package playn.html;
 
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 
 import playn.core.PlayN;
 import playn.core.Touch;
 
 class HtmlTouch extends HtmlInput implements Touch {
   private Listener listener;
  // keep our touch counter to check touch sequence. event.getTouches() don't given full info about current touches. May be gwt bug
  int currentTouches = 0;
 
   /**
    * Special implementation of Event.Impl for keeping track of changes to preventDefault
    */
   static class HtmlTouchEventImpl extends Event.Impl {
     final boolean[] preventDefault;
 
     public HtmlTouchEventImpl(double time, float x, float y, int id, boolean[] preventDefault) {
       super(time, x, y, id);
       this.preventDefault = preventDefault;
     }
 
     @Override
     public void setPreventDefault(boolean preventDefault) {
       this.preventDefault[0] = preventDefault;
     }
 
     @Override
     public boolean getPreventDefault() {
       return preventDefault[0];
     }
   }
 
   HtmlTouch(final Element rootElement) {
     // capture touch start on the root element, only.
     captureEvent(rootElement, "touchstart", new EventHandler() {
       @Override
       public void handleEvent(NativeEvent nativeEvent) {
         if (listener != null) {
           if (nativeEvent.getChangedTouches().length() != 0) {
             listener.onTouchStart(extrudeToucheEvents(nativeEvent,rootElement));
            incTouches(nativeEvent.getChangedTouches().length());
           } else {
             listener.onTouchStart(new Event[0]);
           }
         }
       }
     });
 
     // capture touch end anywhere on the page as long as we are in a touch sequence
     capturePageEvent("touchend", new EventHandler() {
       @Override
       public void handleEvent(NativeEvent nativeEvent) {
        if (listener != null && isInTouchSequence()) {
           listener.onTouchEnd(extrudeToucheEvents(nativeEvent,rootElement));
          decTouches(nativeEvent.getChangedTouches().length());
         }
       }
     });
 
     // capture touch move anywhere on the page as long as we are in a touch sequence
     capturePageEvent("touchmove", new EventHandler() {
       @Override
       public void handleEvent(NativeEvent nativeEvent) {
        if (listener != null && isInTouchSequence()) {
           listener.onTouchMove(extrudeToucheEvents(nativeEvent,rootElement));
         }
       }
     });
 
     // capture touch move anywhere on the page as long as we are in a touch sequence
     capturePageEvent("touchcancel", new EventHandler() {
       @Override
       public void handleEvent(NativeEvent nativeEvent) {
        if (listener != null && isInTouchSequence()) {
           listener.onTouchCancel(extrudeToucheEvents(nativeEvent,rootElement));
          decTouches(nativeEvent.getChangedTouches().length());
         }
       }
     });
   }
 
  // check if we have one or more current touches
  private boolean isInTouchSequence() { return currentTouches != 0; }

  // increment touch counter
  private void incTouches(int on) { currentTouches += on; }

  // decrement touch counter
  private void decTouches(int on) { currentTouches = Math.max(currentTouches - on,0); }

   // extrude touche events from native event
   private Event[] extrudeToucheEvents(final NativeEvent fromNativeEvent, final Element rootElement) {
     JsArray<com.google.gwt.dom.client.Touch> nativeTouches = fromNativeEvent.getChangedTouches();
     int nativeTouchesLen = nativeTouches.length();
 
     boolean[] preventDefault = {false};
 
     // Convert the JsArray<Native Touch> to an array of Touch.Events
     Event[] touches = new Event[nativeTouchesLen];
     for (int t = 0; t < nativeTouchesLen; t++) {
       com.google.gwt.dom.client.Touch touch = nativeTouches.get(t);
       float x = touch.getRelativeX(rootElement);
       float y = touch.getRelativeY(rootElement);
       int id = getTouchIdentifier(fromNativeEvent, t);
       touches[t] = new HtmlTouchEventImpl(PlayN.currentTime(), x, y, id, preventDefault);
     }
     return touches;
   }
 
   @Override
   public void setListener(Listener listener) {
     this.listener = listener;
   }
 
   /**
    * Return the unique identifier of a touch, or 0
    *
    * @return return the unique identifier of a touch, or 0
    */
   private static native int getTouchIdentifier(NativeEvent evt, int index) /*-{
     return evt.changedTouches[index].identifier || 0;
   }-*/;
 }
