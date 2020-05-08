 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2013, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 
 package com.janrain.android.capture;
 
 import com.janrain.android.utils.JsonUtils;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import static com.janrain.android.capture.Capture.InvalidApidChangeException;
 import static com.janrain.android.utils.CollectionUtils.sortedSetFromIterator;
 
 public class CaptureJsonUtils {
     private CaptureJsonUtils() {}
 
     /**
      * Takes two JSONObjects representing Capture records, performs a deep 'diff', returning the result as a
      * set of ApidChanges to convert the original into the current.
      *
      * @param original the original copy of the record
      * @param current the current version of the record
      * @return A set of ApidChanges to effect the diff
      * @throws InvalidApidChangeException
      *  If JSON value types mismatch between original and current (I.e. if current is updated in a way that
      *      is not supported by the record schema)
      *  If ids are assigned to new plural elements
      */
     public static Set<ApidChange> compileChangeSet(JSONObject original, JSONObject current)
             throws InvalidApidChangeException {
         return compileChangeSet(original, current, "/");
     }
 
     private static Set<ApidChange> compileChangeSet(JSONArray original, JSONArray current,
                                                               String arrayAttrPath)
             throws InvalidApidChangeException {
         if (hasIds(original)) {
             return compileChangeSetForArrayWithIds(original, current, arrayAttrPath);
         } else {
             // original array must've been from a JSON blob
             Set<ApidChange> changeSet = new HashSet<ApidChange>();
             if (JsonUtils.compareJsonVals(original, current) != 0) {
                 changeSet.add(new ApidUpdate(current, arrayAttrPath));
             }
             return changeSet;
         }
     }
 
     /**
      * Inspects a JSONArray for Capture plural element IDs
      * @param array the array to inspect
      * @return true if the array has elements which are objects which have attributes of name "id"
      */
     public static boolean hasIds(JSONArray array) {
         for (Object o : JsonUtils.jsonArrayToList(array)) {
             if (o instanceof JSONObject) {
                 Object maybeId = ((JSONObject) o).opt("id");
                 if (maybeId instanceof Integer || maybeId instanceof Long) return true;
             }
         }
         return false;
     }
 
     private static Set<ApidChange> compileChangeSetForArrayWithIds(JSONArray original,
                                                                    JSONArray current,
                                                                    String arrayAttrPath)
             throws InvalidApidChangeException {
         Set<ApidChange> changeSet = new HashSet<ApidChange>();
         String arrayAttrName = CaptureStringUtils.getLastPathElement(arrayAttrPath);
         String relativePath = arrayAttrPath.substring(0, arrayAttrPath.length() - arrayAttrName.length());
 
         JSONArray sortedOriginal = sortPlurEltsById(original);
         JSONArray sortedCurrent = sortPlurEltsById(current);
 
         int originalIndex = 0;
         for (int currentIndex = 0; currentIndex < sortedCurrent.length(); currentIndex++) {
             Integer currentId = getIdForPlurEltAtIndex(sortedCurrent, currentIndex);
             Object currentElt = sortedCurrent.opt(currentIndex);
 
             if (currentId == null) {
                 // new element
                 JSONArray wrapperArray = new JSONArray(Arrays.asList(new Object[]{currentElt}));
                 JSONObject wrapperObject = new JSONObject();
                 JsonUtils.jsonObjectUnsafePut(wrapperObject, arrayAttrName, wrapperArray);
                 changeSet.add(new ApidUpdate(wrapperObject, relativePath));
             } else {
                 // update to existing id
                 Integer originalId = null;
                 while (originalIndex < sortedOriginal.length()) { // try to find a matching id in original
                     originalId = getIdForPlurEltAtIndex(sortedOriginal, originalIndex);
                     if (currentId <= originalId) break;
                     changeSet.add(new ApidDelete(relativePath + "#" + originalId));
                     originalIndex++;
                 }
 
                 if (currentId.equals(originalId)) {
                     JSONObject originalElt;
                     originalElt = (JSONObject) sortedOriginal.opt(originalIndex++);
                     changeSet.addAll(compileChangeSet(originalElt, (JSONObject) currentElt,
                             relativePath + "/" + arrayAttrName + "#" + currentId));
                 } else {
                     throw new InvalidApidChangeException("Cannot assign ID to new plural elements");
                 }
             }
         }
 
         while (originalIndex < sortedOriginal.length()) {
             Integer idForPlurEltAtIndex = getIdForPlurEltAtIndex(sortedOriginal, originalIndex++);
             changeSet.add(new ApidDelete(relativePath + "#" + idForPlurEltAtIndex));
         }
 
         return changeSet;
     }
 
     private static void inPlaceSortByEltIds(JSONArray original, int start, int len) {
         if (len <= 1) return;
 
         int halfLen = len / 2;
         int left = start;
         int right = start + halfLen;
 
         inPlaceSortByEltIds(original, left, halfLen);
         inPlaceSortByEltIds(original, right, len - halfLen);
 
         Object[] temp = new Object[len];
         int tempIndex = 0;
 
         while (left < start + halfLen || right < start + len) {
             Object leftVal = original.opt(left);
             Object rightVal = original.opt(right);
             if (left < start + halfLen && right < start + len) {
                 Integer leftId = getIdForPlurEltAtIndex(original, left);
                 Integer rightId = getIdForPlurEltAtIndex(original, right);
                 boolean bothIdsNull = leftId == null && rightId == null;
                 leftId = leftId == null ? 0 : leftId;
                 rightId = rightId == null ? 0 : rightId;
                 if (bothIdsNull && JsonUtils.compareJsonVals(leftVal, rightVal) < 0 || leftId < rightId) {
                     temp[tempIndex++] = leftVal;
                     left++;
                 } else  {
                     temp[tempIndex++] = rightVal;
                     right++;
                 }
             } else if (left < start + halfLen) {
                 temp[tempIndex++] = leftVal;
                 left++;
             } else {
                 temp[tempIndex++] = rightVal;
                 right++;
             }
         }
 
         for (int i=0; i < temp.length; i++) JsonUtils.jsonArrayUnsafePut(original, start + i, temp[i]);
     }
 
     private static JSONArray sortPlurEltsById(JSONArray original) {
         JSONArray copy = (JSONArray) JsonUtils.copyJsonVal(original);
         inPlaceSortByEltIds(copy, 0, original.length());
         return copy;
     }
 
     private static Integer getIdForPlurEltAtIndex(JSONArray array, int index) {
         Object element = array.opt(index);
         if (element instanceof JSONObject) return (Integer) ((JSONObject) element).opt("id");
         return null;
     }
 
     private static Set<ApidChange> compileChangeSet(JSONObject original, JSONObject current,
                                                     String relativePath)
             throws InvalidApidChangeException {
         SortedSet<String> originalKeys = sortedSetFromIterator((Iterator<String>) original.keys());
         SortedSet<String> currentKeys = sortedSetFromIterator((Iterator<String>) current.keys());
 
         SortedSet<String> newKeys = new TreeSet<String>(currentKeys);
         newKeys.removeAll(originalKeys);
 
         SortedSet<String> goneKeys = new TreeSet<String>(originalKeys);
         goneKeys.removeAll(currentKeys);
 
         if (newKeys.size() > 0) {
             throw new InvalidApidChangeException("Can't add new keys to JSONObjects. New keys: " +
                     newKeys.toString());
         }
 
         if (goneKeys.size() > 0) {
             throw new InvalidApidChangeException("Cannot delete keys from JSONObjects. Removed " +
                     "keys: " + goneKeys.toString());
         }
 
         SortedSet<String> intersectionKeys = new TreeSet<String>(originalKeys);
         intersectionKeys.removeAll(goneKeys);
 
         return compileChangeSet(original, current, relativePath, intersectionKeys);
     }
 
     private static Set<ApidChange> compileChangeSet(JSONObject original, JSONObject current,
                                                     String relativePath, Set<String> forKeys)
             throws InvalidApidChangeException {
         Set<ApidChange> changeSet = new HashSet<ApidChange>();
         for (String k : forKeys) {
             Object curVal = current.opt(k);
             Object oldVal = original.opt(k);
             if (curVal instanceof JSONObject && oldVal instanceof JSONObject) {
                 changeSet.addAll(compileChangeSet(((JSONObject) oldVal), ((JSONObject) curVal),
                         relativePath + k + "/"));
             } else if (curVal instanceof JSONArray && oldVal instanceof JSONArray) {
                 changeSet.addAll(compileChangeSet(((JSONArray) oldVal), ((JSONArray) curVal),
                         relativePath + k));
             } else if (curVal instanceof String || curVal instanceof Boolean || curVal instanceof Double ||
                     curVal instanceof Integer || curVal instanceof Long || curVal.equals(JSONObject.NULL)) {
                 if (!JSONObject.NULL.equals(oldVal) &&
                         !oldVal.getClass().isAssignableFrom(curVal.getClass())) {
                     throw createInvalidTypeException(curVal, oldVal);
                 }
 
                 if (JsonUtils.compareJsonVals(curVal, oldVal) != 0) {
                     changeSet.add(new ApidUpdate(curVal, relativePath + k));
                 }
             } else {
                 throw createInvalidTypeException(curVal, oldVal);
             }
         }
 
         return changeSet;
     }
 
     private static InvalidApidChangeException createInvalidTypeException(Object curVal, Object oldVal) {
         return new InvalidApidChangeException("Unexpected type(s). Old type: " +
                 oldVal.getClass().getSimpleName() + " New type: " + curVal.getClass().getSimpleName());
     }
 
     public static String valueForAttrByDotPath(JSONObject user, String attrDothPath) {
         String[] pathComponents = attrDothPath.split("\\.");
 
         return valueForAttrByDotPathComponents(user, new LinkedList<String>(Arrays.asList(pathComponents)));
     }
 
     private static String valueForAttrByDotPathComponents(Object user,
                                                           LinkedList<String> dotPathComponents) {
         if (dotPathComponents.size() == 0) return user.toString();
 
         if (user == null) return null;
 
         String head = dotPathComponents.remove(0);
         String[] pluralSplit = head.split("#");
         if (pluralSplit.length > 1) {
             Object val = ((JSONObject) user).opt(pluralSplit[0]);
             if (!(val instanceof JSONArray)) return null;
 
             List val_ = JsonUtils.jsonArrayToList((JSONArray) val);
             for (Object elt : val_) {
                 if (!(elt instanceof JSONObject)) return null;
                 if (((JSONObject) elt).opt("id").equals(pluralSplit[1])) {
                     return valueForAttrByDotPathComponents(elt, dotPathComponents);
                 }
             }
             return null;
         } else {
             Object val = ((JSONObject) user).opt(head);
             return valueForAttrByDotPathComponents(val, dotPathComponents);
         }
     }
 }
