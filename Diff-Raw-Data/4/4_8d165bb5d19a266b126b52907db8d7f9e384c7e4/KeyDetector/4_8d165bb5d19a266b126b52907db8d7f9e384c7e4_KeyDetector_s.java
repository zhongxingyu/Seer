 /*
  * Copyright (C) 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.android.inputmethod.keyboard;
 
 import android.util.Log;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class KeyDetector {
     private static final String TAG = KeyDetector.class.getSimpleName();
     private static final boolean DEBUG = false;
 
     public static final int NOT_A_CODE = -1;
     public static final int NOT_A_KEY = -1;
 
     private Keyboard mKeyboard;
     private int mCorrectionX;
     private int mCorrectionY;
     private boolean mProximityCorrectOn;
     private int mProximityThresholdSquare;
 
     // working area
     private static final int MAX_NEARBY_KEYS = 12;
     private final int[] mDistances = new int[MAX_NEARBY_KEYS];
     private final int[] mIndices = new int[MAX_NEARBY_KEYS];
 
     public void setKeyboard(Keyboard keyboard, float correctionX, float correctionY) {
         if (keyboard == null)
             throw new NullPointerException();
         mCorrectionX = (int)correctionX;
         mCorrectionY = (int)correctionY;
         mKeyboard = keyboard;
     }
 
     protected int getTouchX(int x) {
         return x + mCorrectionX;
     }
 
     protected int getTouchY(int y) {
         return y + mCorrectionY;
     }
 
     protected List<Key> getKeys() {
         if (mKeyboard == null)
             throw new IllegalStateException("keyboard isn't set");
         // mKeyboard is guaranteed not to be null at setKeybaord() method if mKeys is not null
         return mKeyboard.getKeys();
     }
 
     public void setProximityCorrectionEnabled(boolean enabled) {
         mProximityCorrectOn = enabled;
     }
 
     public boolean isProximityCorrectionEnabled() {
         return mProximityCorrectOn;
     }
 
     public void setProximityThreshold(int threshold) {
         mProximityThresholdSquare = threshold * threshold;
     }
 
     /**
      * Computes maximum size of the array that can contain all nearby key indices returned by
      * {@link #getKeyIndexAndNearbyCodes}.
      *
      * @return Returns maximum size of the array that can contain all nearby key indices returned
      *         by {@link #getKeyIndexAndNearbyCodes}.
      */
     protected int getMaxNearbyKeys() {
         return MAX_NEARBY_KEYS;
     }
 
     /**
      * Allocates array that can hold all key indices returned by {@link #getKeyIndexAndNearbyCodes}
      * method. The maximum size of the array should be computed by {@link #getMaxNearbyKeys}.
      *
      * @return Allocates and returns an array that can hold all key indices returned by
      *         {@link #getKeyIndexAndNearbyCodes} method. All elements in the returned array are
      *         initialized by {@link #NOT_A_KEY} value.
      */
     public int[] newCodeArray() {
         int[] codes = new int[getMaxNearbyKeys()];
         Arrays.fill(codes, NOT_A_KEY);
         return codes;
     }
 
     private void initializeNearbyKeys() {
         Arrays.fill(mDistances, Integer.MAX_VALUE);
         Arrays.fill(mIndices, NOT_A_KEY);
     }
 
     /**
      * Insert the key into nearby keys buffer and sort nearby keys by ascending order of distance.
      *
      * @param keyIndex index of the key.
      * @param distance distance between the key's edge and user touched point.
      * @return order of the key in the nearby buffer, 0 if it is the nearest key.
      */
     private int sortNearbyKeys(int keyIndex, int distance) {
         final int[] distances = mDistances;
         final int[] indices = mIndices;
         for (int insertPos = 0; insertPos < distances.length; insertPos++) {
             if (distance < distances[insertPos]) {
                 final int nextPos = insertPos + 1;
                 if (nextPos < distances.length) {
                     System.arraycopy(distances, insertPos, distances, nextPos,
                             distances.length - nextPos);
                     System.arraycopy(indices, insertPos, indices, nextPos,
                             indices.length - nextPos);
                 }
                 distances[insertPos] = distance;
                 indices[insertPos] = keyIndex;
                 return insertPos;
             }
         }
         return distances.length;
     }
 
     private void getNearbyKeyCodes(final int[] allCodes) {
         final List<Key> keys = getKeys();
         final int[] indices = mIndices;
 
         // allCodes[0] should always have the key code even if it is a non-letter key.
         if (indices[0] == NOT_A_KEY) {
             allCodes[0] = NOT_A_CODE;
             return;
         }
 
         int numCodes = 0;
         for (int j = 0; j < indices.length && numCodes < allCodes.length; j++) {
             final int index = indices[j];
             if (index == NOT_A_KEY)
                 break;
             final int code = keys.get(index).mCode;
             // filter out a non-letter key from nearby keys
             if (code < Keyboard.CODE_SPACE)
                 continue;
             allCodes[numCodes++] = code;
         }
     }
 
     /**
      * Finds all possible nearby key indices around a touch event point and returns the nearest key
      * index. The algorithm to determine the nearby keys depends on the threshold set by
      * {@link #setProximityThreshold(int)} and the mode set by
      * {@link #setProximityCorrectionEnabled(boolean)}.
      *
      * @param x The x-coordinate of a touch point
      * @param y The y-coordinate of a touch point
      * @param allCodes All nearby key code except functional key are returned in this array
      * @return The nearest key index
      */
     public int getKeyIndexAndNearbyCodes(int x, int y, final int[] allCodes) {
         final List<Key> keys = getKeys();
         final int touchX = getTouchX(x);
         final int touchY = getTouchY(y);
 
         initializeNearbyKeys();
         int primaryIndex = NOT_A_KEY;
         for (final int index : mKeyboard.getNearestKeys(touchX, touchY)) {
             final Key key = keys.get(index);
             final boolean isInside = mKeyboard.isInside(key, touchX, touchY);
             final int distance = key.squaredDistanceToEdge(touchX, touchY);
             if (isInside || (mProximityCorrectOn && distance < mProximityThresholdSquare)) {
                 final int insertedPosition = sortNearbyKeys(index, distance);
                if (insertedPosition == 0 && isInside)
                     primaryIndex = index;
             }
         }
 
         if (allCodes != null && allCodes.length > 0) {
             getNearbyKeyCodes(allCodes);
             if (DEBUG) {
                 Log.d(TAG, "x=" + x + " y=" + y
                         + " primary="
                         + (primaryIndex == NOT_A_KEY ? "none" : keys.get(primaryIndex).mCode)
                         + " codes=" + Arrays.toString(allCodes));
             }
         }
 
         return primaryIndex;
     }
 }
