 /*
  * Copyright (c) 2010 BlipIt Committers
  * All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package com.thoughtworks.blipit.overlays;
 
 import android.graphics.drawable.Drawable;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import com.thoughtworks.blipit.utils.BlipItUtils;
 import com.thoughtworks.contract.subscribe.Blip;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class BlipOverlay extends BalloonItemizedOverlay<OverlayItem> {
     private List<OverlayItem> blips = new ArrayList<OverlayItem>();
 
     public BlipOverlay(Drawable drawable, MapView mapView) {
         super(boundCenter(drawable), mapView);
        populate();
     }
 
     @Override
     protected OverlayItem createItem(int index) {
         return blips.get(index);
     }
 
     @Override
     public int size() {
         return blips.size();
     }
 
     public void addBlip(OverlayItem blip) {
         blips.add(blip);
         populate();
     }
 
     public void addBlips(List<Blip> blips) {
         if (blips == null) return;
         this.blips.clear();
         for (Blip blip : blips) {
             addBlip(BlipItUtils.getOverlayItem(blip));
         }
         mapView.invalidate();
     }
 }
